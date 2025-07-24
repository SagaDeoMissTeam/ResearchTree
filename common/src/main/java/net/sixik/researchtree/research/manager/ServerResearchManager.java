package net.sixik.researchtree.research.manager;

import dev.architectury.event.events.common.TickEvent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.sixik.researchtree.ResearchTree;
import net.sixik.researchtree.network.ask.SyncResearchASK;
import net.sixik.researchtree.network.fromServer.SendPlayerResearchDataChangeS2C;
import net.sixik.researchtree.research.BaseResearch;
import net.sixik.researchtree.research.ResearchData;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
    private final MinecraftServer server;
    private final BlockingQueue<Runnable> tasks;

    protected ConcurrentHashMap<ResourceLocation, ResearchData> researchesData;

    public ServerResearchManager(MinecraftServer server) {
        this.server = server;
        this.tasks = new LinkedBlockingQueue<>();
        this.executor = Executors.newSingleThreadExecutor(r -> {
            Thread thread = new Thread(r, "ResearchManager");
            thread.setDaemon(true);
            return thread;
        });
        this.researchesData = new ConcurrentHashMap<>();
        INSTANCE = this;

        if(ResearchTree.MOD_CONFIG.getAsyncResearchManager())
            startTaskProcessing();
        else {
            if(!registered) {
                TickEvent.SERVER_POST.register(s -> INSTANCE.tickResearchData());
                registered = true;
            }
        }
    }

    private void startTaskProcessing() {
        executor.submit(() -> {
            while (!shutdown) {
                try {
                    Runnable task = tasks.take();
                    if (!shutdown) {
                        task.run();
                    }
                    tickResearchData();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    LOGGER.info("ResearchManager thread interrupted, shutting down");
                    break;
                } catch (Exception e) {
                    LOGGER.error("Error processing research task", e);
                }
            }

            tasks.clear();
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

    public Optional<ResearchData> getResearchData(ResourceLocation resourceId) {
        return Optional.ofNullable(researchesData.get(resourceId));
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

    public void addStartResearch(Player player, BaseResearch research) {
        PlayerResearchData playerData = getOrCreatePlayerData(player);

        long researchTime = research.getResearchTime();

        if(researchTime == -1)
            researchTime = ResearchTree.MOD_CONFIG.getDefaultResearchTimeMs();
        if(researchTime <= 0) {
            research.onResearchEnd(player);
        } else {
            playerData.addProgressResearch(research.getId(), research.getResearchTime());

            if (player instanceof ServerPlayer serverPlayer) {
                SendPlayerResearchDataChangeS2C.sendAddProgress(serverPlayer, research.getId(), research.getResearchTime());
            }
        }
    }

    public Optional<ServerPlayer> getPlayer(UUID player) {
        return Optional.ofNullable(server.getPlayerList().getPlayer(player));
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

    public CompletableFuture<Optional<BaseResearch>> findResearchById(ResourceLocation researchId, Executor executor) {
        return CompletableFuture.supplyAsync( () -> findResearchById(researchId), executor);
    }

    public void syncResearchDataWithAll(ResourceLocation researchDataid) {
        new SyncResearchASK(null).startRequest(server, researchDataid);
    }
}
