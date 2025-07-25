package net.sixik.researchtree.research.manager;

import dev.architectury.event.EventResult;
import dev.architectury.event.events.common.BlockEvent;
import dev.architectury.event.events.common.EntityEvent;
import dev.architectury.event.events.common.TickEvent;
import dev.architectury.utils.value.IntValue;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.LevelResource;
import net.sixik.researchtree.ResearchTree;
import net.sixik.researchtree.api.ResearchTreeBuilder;
import net.sixik.researchtree.network.ask.SyncResearchASK;
import net.sixik.researchtree.network.fromServer.SendPlayerResearchDataS2C;
import net.sixik.researchtree.registers.ModRegisters;
import net.sixik.researchtree.research.BaseResearch;
import net.sixik.researchtree.research.ResearchData;
import net.sixik.researchtree.api.managers.TeamManager;
import net.sixik.researchtree.research.triggers.BaseTrigger;
import net.sixik.researchtree.research.triggers.EventType;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import oshi.util.tuples.Pair;

import java.util.*;
import java.util.concurrent.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

public class ServerResearchManager extends ResearchManager {

    private static boolean registered = false;
    static ServerResearchManager INSTANCE;

    private static final Logger LOGGER = LoggerFactory.getLogger(ServerResearchManager.class);

    private volatile boolean shutdown = false;
    private final ExecutorService executor;
    private final ExecutorService triggerExecutor;
    private final MinecraftServer server;

    protected ConcurrentHashMap<ResourceLocation, ResearchData> researchesData;
    protected CopyOnWriteArrayList<PlayerResearchData.PlayerOfflineData> offlineData = new CopyOnWriteArrayList<>();
    protected List<TeamManager> teamManagers = new ArrayList<>();

    public ServerResearchManager(MinecraftServer server) {
        super(LOGGER);
        this.researchesData = new ConcurrentHashMap<>();
        this.server = server;
        this.executor = Executors.newSingleThreadExecutor(r -> {
            Thread thread = new Thread(r, "ServerResearchManager");
            thread.setDaemon(true);
            return thread;
        });
        this.triggerExecutor = Executors.newSingleThreadExecutor(r -> {
            Thread thread = new Thread(r, "ServerResearchManagerTrigger");
            thread.setDaemon(true);
            return thread;
        });
        INSTANCE = this;

        if(ResearchTree.MOD_CONFIG.getAsyncResearchManager())
            startTaskProcessing();
        else {
            if(!registered) {
                TickEvent.SERVER_POST.register(s -> {
                    if(INSTANCE != null)  INSTANCE.tickResearchData();
                });

                Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                    if(INSTANCE != null)
                        INSTANCE.savePlayerData(server.getWorldPath(LevelResource.ROOT).resolve("research_tree"), "player_data.nbt");
                }));

                registered = true;
            }
        }

        registerEvents();
        loadTeamManagers();
        loadPlayerData(server.getWorldPath(LevelResource.ROOT).resolve("research_tree"), "player_data.nbt");
    }

    public void registerEvents() {
        if(!ResearchTree.MOD_CONFIG.getEnableTriggers()) return;
        if(ResearchTree.MOD_CONFIG.getAsyncTrigger()) {
            TickEvent.PLAYER_POST.register(this::playerTickAsync);
            EntityEvent.LIVING_DEATH.register(this::entityDieAsync);
            BlockEvent.BREAK.register(this::breakBlockAsync);
        } else {
            TickEvent.PLAYER_POST.register(this::playerTick);
            EntityEvent.LIVING_DEATH.register(this::entityDie);
            BlockEvent.BREAK.register(this::breakBlock);
        }

    }

    public void unRegisterEvents() {
        if(!ResearchTree.MOD_CONFIG.getEnableTriggers()) return;
        if(ResearchTree.MOD_CONFIG.getAsyncTrigger()) {
            TickEvent.PLAYER_POST.unregister(this::playerTickAsync);
            EntityEvent.LIVING_DEATH.unregister(this::entityDieAsync);
            BlockEvent.BREAK.unregister(this::breakBlockAsync);
        } else {
            TickEvent.PLAYER_POST.unregister(this::playerTick);
            EntityEvent.LIVING_DEATH.unregister(this::entityDie);
            BlockEvent.BREAK.unregister(this::breakBlock);
        }
    }



    public void loadTeamManagers() {
        ModRegisters.getTeamManagers().forEach(s -> teamManagers.add(s.get()));
    }

    private void startTaskProcessing() {
        executor.submit(() -> {
            while (!shutdown) {
                try {
                    if(!ResearchTreeBuilder.DATA_BUILDER.isEmpty()) {
                        researchesData.clear();
                        researchesData.putAll(ResearchTreeBuilder.DATA_BUILDER);
                        ResearchTreeBuilder.DATA_BUILDER.clear();
                    }
                    tickResearchData();
                } catch (Exception e) {
                    LOGGER.error("Error processing research task", e);
                }
            }
        });
    }

    @Override
    public void shutdown() {
        if (shutdown) return;
        shutdown = true;
        executor.shutdown();
        try {
            if (!executor.awaitTermination(5, TimeUnit.SECONDS)) {
                executor.shutdownNow();
                LOGGER.warn("ResearchManager executor did not terminate gracefully");
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
            Thread.currentThread().interrupt();
            LOGGER.error("Interrupted during ResearchManager shutdown", e);
        }
        triggerExecutor.shutdown();
        try {
            if (!triggerExecutor.awaitTermination(5, TimeUnit.SECONDS)) {
                triggerExecutor.shutdownNow();
                LOGGER.warn("ResearchManagerTrigger executor did not terminate gracefully");
            }
        } catch (InterruptedException e) {
            triggerExecutor.shutdownNow();
            Thread.currentThread().interrupt();
            LOGGER.error("Interrupted during ResearchManagerTrigger shutdown", e);
        }

        savePlayerData(server.getWorldPath(LevelResource.ROOT).resolve("research_tree"), "player_data.nbt");
        unRegisterEvents();
        INSTANCE = null;
    }

    @Nullable
    public CompletableFuture<Void> submitToServer(Runnable runnable) {
        if (shutdown) {
            LOGGER.warn("Attempted to submit task to server after shutdown");
            return null;
        }
        try {
            return server.submit(() -> {
                try {
                    runnable.run();
                } catch (Exception e) {
                    LOGGER.error("Error executing task in server thread", e);
                }
            });
        } catch (Exception e) {
            LOGGER.error("Failed to submit task to server", e);
            return null;
        }
    }

    public static ServerResearchManager getInstance() {
        return INSTANCE;
    }

    public Optional<ResearchData> getResearchData(ResourceLocation researchDataId) {
        return Optional.ofNullable(researchesData.get(researchDataId));
    }

    public void addResearchData(ResearchData data) {
        if(researchesData.containsKey(data.getId())) {
            return;
        }
        researchesData.put(data.getId(), data);
    }

    public void addResearchDataWithReplace(ResearchData data) {
        researchesData.put(data.getId(), data);
    }

    public Optional<ResearchData> createResearchData() {
        return createResearchData(ResearchData.ADDITION_DEFAULT.apply(UUID.randomUUID().toString()));
    }

    public Optional<ResearchData> createResearchData(ResourceLocation resourceId) {
        return createResearchData(resourceId, ResearchData::new);
    }

    public Optional<ResearchData> createResearchData(ResourceLocation resourceId, Function<ResourceLocation, @Nullable ResearchData> constructor) {
        ResearchData data = constructor.apply(resourceId);
        if(data == null) return Optional.empty();
        researchesData.put(data.getId(), data);
        return Optional.of(data);
    }

    public boolean removeResearchData(ResourceLocation resourceId) {
        return researchesData.remove(resourceId) != null;
    }

    public boolean removeResearchDataIf(Predicate<ResearchData> dataPredicate) {
        List<ResourceLocation> removes = new ArrayList<>();
        for (Map.Entry<ResourceLocation, ResearchData> entry : researchesData.entrySet()) {
            if(dataPredicate.test(entry.getValue()))
                removes.add(entry.getKey());
        }

        boolean ret = false;
        for (ResourceLocation remove : removes) {
            if(researchesData.remove(remove) != null)
                ret = true;
        }
        return ret;
    }

    public Optional<ServerPlayer> getPlayer(UUID player) {
        return Optional.ofNullable(server.getPlayerList().getPlayer(player));
    }

    public List<BaseResearch> findResearchesByIds(Collection<ResourceLocation> resourceLocations) {
         List<BaseResearch> baseResearches = new ArrayList<>();
         for (Map.Entry<ResourceLocation, ResearchData> entry : researchesData.entrySet()) {
            List<BaseResearch> copyLst = new ArrayList<>(entry.getValue().getResearchList());
            for (BaseResearch baseResearch : copyLst) {
                if(resourceLocations.contains(baseResearch.getId())) {
                    baseResearches.add(baseResearch);
                }
            }
        }
        return baseResearches;
    }

    public List<BaseResearch> findResearchesByIds(ResourceLocation researchDataId, Collection<ResourceLocation> resourceLocations) {
        List<BaseResearch> baseResearches = new ArrayList<>();
        for (BaseResearch baseResearch : researchesData.getOrDefault(researchDataId, new ResearchData()).getResearchList()) {
            if(resourceLocations.contains(baseResearch.getId())) {
                baseResearches.add(baseResearch);
            }
        }
        return baseResearches;
    }

    public Optional<BaseResearch> findResearchById(ResourceLocation researchId) {
        for (Map.Entry<ResourceLocation, ResearchData> entry : researchesData.entrySet()) {
            List<BaseResearch> copyLst = new ArrayList<>(entry.getValue().getResearchList());
            for (BaseResearch baseResearch : copyLst) {
                if(baseResearch.getId().equals(researchId)) return Optional.of(baseResearch);
            }
        }
        return Optional.empty();
    }

    public Optional<Pair<ResearchData, BaseResearch>> findResearchAndDataById(ResourceLocation researchId) {
        for (Map.Entry<ResourceLocation, ResearchData> entry : researchesData.entrySet()) {
            List<BaseResearch> copyLst = new ArrayList<>(entry.getValue().getResearchList());
            for (BaseResearch baseResearch : copyLst) {
                if(baseResearch.getId().equals(researchId)) return Optional.of(new Pair<>(entry.getValue(), baseResearch));
            }
        }
        return Optional.empty();
    }

    public List<BaseResearch> getAllResearches(ResourceLocation researchDataId) {
        return researchesData.getOrDefault(researchDataId, new ResearchData()).getResearchList().stream().toList();
    }

    public List<ResearchData> getAllResearchesData() {
        return researchesData.values().stream().toList();
    }

    public List<BaseResearch> getAllResearches() {
        List<BaseResearch> researchIds = new ArrayList<>();
        for (Map.Entry<ResourceLocation, ResearchData> entry : researchesData.entrySet()) {
            researchIds.addAll(entry.getValue().getResearchList());
        }
        return researchIds;
    }

    public CompletableFuture<Optional<BaseResearch>> findResearchById(ResourceLocation researchId, Executor executor) {
        return CompletableFuture.supplyAsync( () -> findResearchById(researchId), executor);
    }

    public void syncResearchDataWithAll(ResourceLocation researchDataid) {
        new SyncResearchASK(null).startRequest(server, researchDataid);
    }

    public void addOfflineData(UUID playerId, ResourceLocation researchDataId, ResourceLocation researchId) {
        PlayerResearchData.PlayerOfflineData playerOfflineData = null;

        boolean exists = false;
        for (PlayerResearchData.PlayerOfflineData offlineDatum : offlineData) {
            if(offlineDatum.getPlayerOwnerId().equals(playerId)) {
                playerOfflineData = offlineDatum;
                exists = true;
                break;
            }
        }

        if(!exists) {
            playerOfflineData = new PlayerResearchData.PlayerOfflineData(playerId, false, new HashMap<>());
            offlineData.add(playerOfflineData);
        }

        playerOfflineData.addResearchData(researchDataId, researchId);
    }

    public void removeOfflineData(UUID playerId, ResourceLocation researchDataId, ResourceLocation researchId) {
        offlineData.stream().filter(s -> s.getPlayerOwnerId().equals(playerId)).findFirst().ifPresent(data -> {
            data.removeResearchData(researchDataId, researchId);
        });
    }

    public void executeOfflineData(Player player) {
        offlineData.stream().filter(s -> s.getPlayerOwnerId().equals(player.getGameProfile().getId())).findFirst().ifPresent(data -> {
            data.execute(player);
        });
    }

    public void invokeTeamManagers(Consumer<TeamManager> function) {
        for (TeamManager teamManager : teamManagers) {
            function.accept(teamManager);
        }
    }

    public <T> Optional<T> invokeTeamManagers(Function<TeamManager, Optional<T>> function) {
        for (TeamManager teamManager : teamManagers) {
            Optional<T> apply = function.apply(teamManager);
            if(apply.isPresent()) return apply;
        }

        return Optional.empty();
    }

    public boolean synchronizePlayerDataWithTeammates(ServerPlayer player) {
        return invokeTeamManagers(teamManager -> {
            PlayerResearchData mainPlayerManager = getOrCreatePlayerData(player);
            List<ResourceLocation> researches = new ArrayList<>();
            if(!teamManager.haveTeam(player)) return Optional.of(false);


            for (UUID memberUUID : teamManager.getTeamMembers(player)) {
                if(memberUUID.equals(player.getGameProfile().getId())) continue;

                Optional<PlayerResearchData> playerResearchData = getPlayerDataOptional(memberUUID);
                if(playerResearchData.isEmpty()) continue;

                researches.addAll(playerResearchData.get().unlockedResearch);
            }

            if(researches.isEmpty()) return Optional.of(false);
            findResearchesByIds(researches).forEach(s -> s.onResearchEnd(player, false, false));
            updateTriggerData(mainPlayerManager, player);
            SendPlayerResearchDataS2C.sendTo(player, mainPlayerManager);

            return Optional.of(true);
        }).isPresent();
    }

    public void tickTriggersLimit(EventType eventType, Player player, Object... args) {
        if(player.level().getGameTime() % ResearchTree.MOD_CONFIG.getTickCheck() == 0) {
            tickTriggers(eventType, player, args);
        }
    }

    public void tickTriggers(EventType eventType, Player player, Object... args) {
        getPlayerDataOptional(player).ifPresent(playerResearchData -> {
            for (BaseResearch baseResearch : playerResearchData.getCachedUnlockedResearchesOrCreate(this, player)) {
                for (BaseTrigger trigger : baseResearch.getTriggers()) {
                    if(trigger.getEventType() != eventType) continue;
                    if(baseResearch.isTriggerComplete(player, trigger)) continue;
                    trigger.executeInternal(this, playerResearchData, baseResearch, player, args);
                }
            }
        });
    }

    private EventResult entityDie(LivingEntity livingEntity, DamageSource damageSource) {
        if (damageSource.getDirectEntity() instanceof ServerPlayer serverPlayer) {
            tickTriggers(EventType.MOB_KILL, serverPlayer, (Entity) livingEntity);
        }
        return EventResult.interruptDefault();
    }

    private EventResult breakBlock(Level level, BlockPos blockPos, BlockState blockState, ServerPlayer serverPlayer, @Nullable IntValue intValue) {
        tickTriggers(EventType.BLOCK_BREAK, serverPlayer, blockState);
        return EventResult.interruptDefault();
    }

    private void playerTick(Player player) {
        tickTriggersLimit(EventType.PLAYER_TICK, player);
    }

    private EventResult entityDieAsync(LivingEntity livingEntity, DamageSource damageSource) {
        executor.submit(() -> {
            if (damageSource.getDirectEntity() instanceof ServerPlayer serverPlayer) {
                tickTriggers(EventType.MOB_KILL, serverPlayer, (Entity) livingEntity);
            }
        });
        return EventResult.interruptDefault();
    }

    private EventResult breakBlockAsync(Level level, BlockPos blockPos, BlockState blockState, ServerPlayer serverPlayer, @Nullable IntValue intValue) {
        executor.submit(() -> {
            tickTriggers(EventType.BLOCK_BREAK, serverPlayer, blockState);
        });
        return EventResult.interruptDefault();
    }

    private void playerTickAsync(Player player) {
        executor.submit(() -> {
            tickTriggersLimit(EventType.PLAYER_TICK, player);
        });
    }
}
