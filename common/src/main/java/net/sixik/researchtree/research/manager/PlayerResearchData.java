package net.sixik.researchtree.research.manager;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.sixik.researchtree.ResearchTree;
import net.sixik.researchtree.api.interfaces.FullCodecSerializer;
import net.sixik.researchtree.research.BaseResearch;
import net.sixik.researchtree.research.ResearchChangeType;
import net.sixik.researchtree.utils.ResearchUtils;
import org.apache.commons.lang3.NotImplementedException;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

public class PlayerResearchData implements FullCodecSerializer<PlayerResearchData> {

    public static final Codec<PlayerResearchData> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.STRING                        .fieldOf("playerId"        ).forGetter(PlayerResearchData::getPlayerIdString),
                    ResourceLocation.CODEC.listOf()     .fieldOf("unlockedResearch").forGetter(PlayerResearchData::getUnlockedResearch),
                    ResearchProgressData.CODEC.listOf() .fieldOf("progressResearch").forGetter(PlayerResearchData::getProgressData),
                    TriggerResearchData.CODEC.listOf()  .fieldOf("triggerResearch").forGetter(PlayerResearchData::getTriggerResearchData)
            ).apply(instance, PlayerResearchData::new));

    public static final StreamCodec<FriendlyByteBuf, PlayerResearchData> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8,                                      PlayerResearchData::getPlayerIdString,
            ResourceLocation.STREAM_CODEC.apply(ByteBufCodecs.list()),      PlayerResearchData::getUnlockedResearch,
            ResearchProgressData.STREAM_CODEC.apply(ByteBufCodecs.list()),  PlayerResearchData::getProgressData,
            TriggerResearchData.STREAM_CODEC.apply(ByteBufCodecs.list()),   PlayerResearchData::getTriggerResearchData,
            PlayerResearchData::new
    );

    protected final UUID playerId;
    protected final List<ResourceLocation> unlockedResearch;
    protected final List<ResearchProgressData> progressData;
    protected final List<TriggerResearchData> triggerResearchData;
    protected volatile boolean playerOnline = true;


    protected final Object syncResearch = new Object();
    protected final Object syncProgress = new Object();
    protected final Object syncTriggers = new Object();

    public PlayerResearchData(UUID playerId) {
        this(playerId, new ArrayList<>(), new ArrayList<>(), new ArrayList<>());
    }

    public PlayerResearchData(UUID playerId, List<ResourceLocation> unlockedResearch, List<ResearchProgressData> progressData, List<TriggerResearchData> triggerResearchData) {
        this.playerId = playerId;
        this.unlockedResearch = new ArrayList<>(unlockedResearch);
        this.progressData = new ArrayList<>(progressData);
        this.triggerResearchData = new ArrayList<>(triggerResearchData);
    }

    protected PlayerResearchData(String playerId, List<ResourceLocation> resourceLocations, List<ResearchProgressData> researchProgressData, List<TriggerResearchData> triggerResearchData) {
        this(UUID.fromString(playerId), resourceLocations, researchProgressData, triggerResearchData);
    }

    public UUID getPlayerId() {
        return playerId;
    }

    public String getPlayerIdString() {
        return playerId.toString();
    }

    protected List<ResourceLocation> getUnlockedResearch() {
        return unlockedResearch;
    }

    protected List<ResearchProgressData> getProgressData() {
        return progressData;
    }

    public void updatePlayerOnline(boolean value) {
        this.playerOnline = value;
    }

    public void clearResearches() {
        synchronized (syncResearch) {
            unlockedResearch.clear();
        }
    }

    public void clearProgress() {
        synchronized (syncProgress) {
            progressData.clear();
        }
    }

    public void addUnlockedResearch(ResourceLocation researchId) {
        synchronized (syncResearch) {
            unlockedResearch.add(researchId);
        }
    }

    public boolean removeUnlockedResearch(ResourceLocation researchId) {
        synchronized (syncResearch) {
            return unlockedResearch.remove(researchId);
        }
    }

    public boolean containsInUnlockedResearch(ResourceLocation researchId) {
        synchronized (syncResearch) {
            return unlockedResearch.stream().anyMatch(s -> s.equals(researchId));
        }
    }

    public ResourceLocation getUnlockedResearch(int index) {
        synchronized (syncResearch) {
            return unlockedResearch.get(index);
        }
    }

    public int getUnlockedResearchSize() {
        return unlockedResearch.size();
    }

    public void addProgressResearch(ResourceLocation id, long time) {
        synchronized (syncProgress) {
            progressData.add(new ResearchProgressData(id, time));
        }
    }

    public boolean removeProgressResearch(ResourceLocation id) {
        synchronized (syncProgress) {
            return progressData.removeIf(data -> Objects.equals(data.getId(), id));
        }
    }

    public boolean containsInProgress(ResourceLocation researchId) {
        synchronized (syncProgress) {
            return progressData.stream().anyMatch(d1 -> d1.getId().equals(researchId));
        }
    }

    public ResearchProgressData getProgressResearch(int index) {
        synchronized (syncProgress) {
            return progressData.get(index);
        }
    }

    public List<ResearchProgressData> getPausedResearch() {
        synchronized (syncProgress) {
           return progressData.stream().filter(ResearchProgressData::isPaused).toList();
        }
    }

    public List<ResearchProgressData> getUnPausedResearch() {
        synchronized (syncProgress) {
            return progressData.stream().filter(s -> !s.isPaused()).toList();
        }
    }

    public Optional<ResearchProgressData> getProgressResearch(ResourceLocation researchId) {
        synchronized (syncProgress) {
            return progressData.stream().filter(s -> s.getId().equals(researchId)).findFirst();
        }
    }

    public int getProgressResearchSize() {
        return progressData.size();
    }

    public void changeProgressResearch(ResourceLocation id, Consumer<ResearchProgressData> consumer) {
        synchronized (syncProgress) {
            progressData.stream().filter(data -> Objects.equals(id, data.getId())).findFirst().ifPresent(consumer);
        }
    }

    public void addUnlockedResearch(Collection<ResourceLocation> researchIds) {
        synchronized (syncResearch) {
            for (ResourceLocation researchId : researchIds) {
                if(unlockedResearch.contains(researchId)) continue;
                unlockedResearch.add(researchId);
            }
        }
    }

    public boolean removeResearch(ResourceLocation researchId) {
        synchronized (syncResearch) {
            return unlockedResearch.remove(researchId);
        }
    }

    public boolean hasResearch(ResourceLocation researchId) {
        synchronized (syncResearch) {
            return unlockedResearch.contains(researchId);
        }
    }

    public Optional<TriggerResearchData> getTriggerDataOrCreate(ResourceLocation triggerId) {
        synchronized (syncTriggers) {
            if(hasResearch(triggerId) || containsInProgress(triggerId)) return Optional.empty();
            return getTriggerDataUnSafe(triggerId);
        }
    }

    public Optional<TriggerResearchData> getTriggerData(ResourceLocation triggerId) {
        synchronized (syncTriggers) {
            return getTriggerDataUnSafe(triggerId);
        }
    }

    protected Optional<TriggerResearchData> getTriggerDataUnSafe(ResourceLocation triggerId) {
        return triggerResearchData.stream().filter(s -> s.getResearchId().equals(triggerId)).findFirst();
    }

    public List<TriggerResearchData> getTriggerResearchData() {
        synchronized (syncTriggers) {
            return triggerResearchData;
        }
    }

    public List<TriggerResearchData> getTriggerResearchDataCopy() {
        synchronized (syncTriggers) {
            return new ArrayList<>(triggerResearchData);
        }
    }

    public void tick(long deltaTimeMs) {
        synchronized (syncProgress) {
            Iterator<ResearchProgressData> iterator = progressData.iterator();
            while (iterator.hasNext()) {
                var research = iterator.next();
                if (research == null) break;
                research.update(deltaTimeMs);

                if (research.isResearched()) {
                    synchronized (syncResearch) {
                        unlockedResearch.add(research.getId());

                        Optional<ResearchManager> optManager = ResearchUtils.getManagerOptional(false);
                        if(optManager.isPresent()) {
                            ServerResearchManager manager = (ServerResearchManager) optManager.get();

                            var playerOpt = manager.getPlayer(playerId);

                            manager.findResearchAndDataById(research.getId()).ifPresent(findData -> {
                                if(playerOpt.isEmpty()) {
                                    manager.addOfflineData(playerId, findData.getA().getId(), findData.getB().getId());
                                } else {
                                    playerOpt.ifPresent(findData.getB()::onResearchEnd);
                                }
                            });
                        }
                    }
                    iterator.remove();
                }
            }
        }
    }

    public final void replace(PlayerResearchData data) {
        synchronized (syncResearch) {
            this.unlockedResearch.clear();
            this.unlockedResearch.addAll(data.unlockedResearch);
        }
        synchronized (syncProgress) {
            this.progressData.clear();
            this.progressData.addAll(data.progressData);
        }
    }

    @Override
    public Codec<PlayerResearchData> codec() {
        return CODEC;
    }

    @Override
    public StreamCodec<FriendlyByteBuf, PlayerResearchData> streamCodec() {
        return STREAM_CODEC;
    }

    public static class ResearchProgressData implements FullCodecSerializer<ResearchProgressData> {
        public static final Codec<ResearchProgressData> CODEC = RecordCodecBuilder.create(instance ->
                instance.group(
                        ResourceLocation.CODEC  .fieldOf("id"          ).forGetter(ResearchProgressData::getId),
                        Codec.LONG              .fieldOf("durationMs"  ).forGetter(ResearchProgressData::getDurationMs),
                        Codec.LONG              .fieldOf("elapsedMs"   ).forGetter(ResearchProgressData::getElapsedMs),
                        Codec.BOOL              .fieldOf("isPaused"    ).forGetter(ResearchProgressData::isPaused)
                ).apply(instance, ResearchProgressData::new));

        public static final StreamCodec<FriendlyByteBuf, ResearchProgressData> STREAM_CODEC = StreamCodec.composite(
                ResourceLocation.STREAM_CODEC, ResearchProgressData::getId,
                ByteBufCodecs.VAR_LONG,        ResearchProgressData::getDurationMs,
                ByteBufCodecs.VAR_LONG,        ResearchProgressData::getElapsedMs,
                ByteBufCodecs.BOOL,            ResearchProgressData::isPaused,
                ResearchProgressData::new
        );

        private final ResourceLocation researchId;
        private long durationMs;
        private long elapsedMs;
        private boolean isPaused;

        public ResearchProgressData(ResourceLocation researchId) {
            this(researchId, ResearchTree.MOD_CONFIG.getDefaultResearchTimeMs(), 0, false);
        }

        public ResearchProgressData(ResourceLocation researchId, long durationMs) {
            this(researchId, durationMs, 0, false);
        }

        private ResearchProgressData(ResourceLocation researchId, long durationMs, long elapsedMs, boolean isPaused) {
            this.researchId = researchId;
            this.durationMs = durationMs;
            this.elapsedMs = elapsedMs;
            this.isPaused = isPaused;
        }

        public void update(long deltaTimeMs) {
            if (!isPaused) {
                elapsedMs = Math.min(durationMs, elapsedMs + deltaTimeMs);
            }
        }

        public void pause() {
            isPaused = true;
        }

        public void resume() {
            isPaused = false;
        }

        public boolean isResearched() {
            return elapsedMs >= durationMs;
        }

        public long getRemainingMs() {
            return durationMs - elapsedMs;
        }

        public ResourceLocation getId() {
            return researchId;
        }

        public long getDurationMs() {
            return durationMs;
        }

        public void setDurationMs(long durationMs) {
            this.durationMs = durationMs;
        }

        public long getElapsedMs() {
            return elapsedMs;
        }

        public boolean isPaused() {
            return isPaused;
        }

        public double getProgressPercentDouble() {
            if (durationMs == 0) {
                return elapsedMs > 0 ? 100.0 : 0.0;
            }
            return Math.min(100.0, (elapsedMs * 100.0) / durationMs);
        }



        public ResearchProgressData copy() {
            return new ResearchProgressData(researchId, durationMs, elapsedMs, isPaused);
        }

        @Override
        public Codec<ResearchProgressData> codec() {
            return CODEC;
        }

        @Override
        public StreamCodec<FriendlyByteBuf, ResearchProgressData> streamCodec() {
            return STREAM_CODEC;
        }
    }

    public static class PlayerOfflineData implements FullCodecSerializer<PlayerOfflineData> {

        private static final Codec<Map<ResourceLocation, List<ResourceLocation>>> MAP_CODEC = RecordCodecBuilder.create(instance ->
                instance.group(
                        ResourceLocation.CODEC.listOf().fieldOf("keys").forGetter(s -> s.keySet().stream().toList()),
                        ResourceLocation.CODEC.listOf().listOf().fieldOf("values").forGetter(s -> s.values().stream().toList())
                ).apply(instance, (s1, s2) -> {
                    HashMap<ResourceLocation, List<ResourceLocation>> map = new HashMap<>();
                    for (int i = 0; i < s1.size(); i++) {
                        map.put(s1.get(i), s2.get(i));
                    }
                    return map;
                }));

        public static final Codec<PlayerOfflineData> CODEC = RecordCodecBuilder.create(instance ->
                instance.group(
                        Codec.STRING.fieldOf("ownerId").forGetter(s -> s.getPlayerOwnerId().toString()),
                        Codec.BOOL.fieldOf("useTeam").forGetter(PlayerOfflineData::isUseTeam),
                        MAP_CODEC.fieldOf("unlockedResearches").forGetter(PlayerOfflineData::getUnlockedResearches)
                ).apply(instance, PlayerOfflineData::new));

        private final UUID playerOwnerId;
        private final boolean useTeam;
        private final ConcurrentHashMap<ResourceLocation, List<ResourceLocation>> unlockedResearches = new ConcurrentHashMap<>();

        protected PlayerOfflineData(String playerOwnerId, boolean useTeam, Map<ResourceLocation, List<ResourceLocation>> map) {
            this(UUID.fromString(playerOwnerId), useTeam, map);
        }

        public PlayerOfflineData(UUID playerOwnerId, boolean useTeam, Map<ResourceLocation, List<ResourceLocation>> map) {
            this.playerOwnerId = playerOwnerId;
            this.useTeam = useTeam;
            this.unlockedResearches.putAll(map);
        }

        protected ConcurrentHashMap<ResourceLocation, List<ResourceLocation>> getUnlockedResearches() {
            return unlockedResearches;
        }

        public void addResearchData(ResourceLocation researchDataId, ResourceLocation researchId) {
            unlockedResearches.computeIfAbsent(researchDataId, s -> new ArrayList<>()).add(researchDataId);
        }

        public boolean removeResearchData(ResourceLocation researchDataId, ResourceLocation researchId) {
            return unlockedResearches.getOrDefault(researchDataId, new ArrayList<>()).removeIf(s -> s.equals(researchId));
        }

        public boolean isUseTeam() {
            return useTeam;
        }

        public void execute(Player player) {
            ServerResearchManager manager = ResearchUtils.getManagerCast(false);

            record Data(ResourceLocation id, List<ResourceLocation> resear) {}

            List<Data> researchData = new ArrayList<>();

            for (Map.Entry<ResourceLocation, List<ResourceLocation>> entry : this.unlockedResearches.entrySet()) {
                manager.getResearchData(entry.getKey()).ifPresent(data -> {
                    List<BaseResearch> res = data.findResearches(entry.getValue());
                    Data data1 = new Data(entry.getKey(), new ArrayList<>());

                    for (BaseResearch research : res) {
                        research.onResearchEnd(player);
                        data1.resear().add(research.getId());
                    }
                    researchData.add(data1);
                });
            }

            if(!researchData.isEmpty())
                researchData.forEach(s -> s.resear.forEach(s1 -> removeResearchData(s.id, s1)));
        }

        public UUID getPlayerOwnerId() {
            return playerOwnerId;
        }

        @Override
        public Codec<PlayerOfflineData> codec() {
            return CODEC;
        }

        @Override
        public StreamCodec<FriendlyByteBuf, PlayerOfflineData> streamCodec() {
            throw new NotImplementedException();
        }
    }

    public static class TriggerResearchData implements FullCodecSerializer<TriggerResearchData> {

        public static final Codec<TriggerResearchData> CODEC = RecordCodecBuilder.create(instance ->
                instance.group(
                        ResourceLocation.CODEC.fieldOf("id").forGetter(TriggerResearchData::getResearchId),
                        Codec.INT.listOf().fieldOf("complete").forGetter(TriggerResearchData::getTriggerComplete)
                ).apply(instance, TriggerResearchData::new));

        public static final StreamCodec<FriendlyByteBuf, TriggerResearchData> STREAM_CODEC = StreamCodec.composite(
                ResourceLocation.STREAM_CODEC, TriggerResearchData::getResearchId,
                ByteBufCodecs.INT.apply(ByteBufCodecs.list()), TriggerResearchData::getTriggerComplete,
                TriggerResearchData::new
        );

        private final ResourceLocation researchId;
        private final List<Integer> triggerComplete;

        public TriggerResearchData(ResourceLocation researchId) {
            this(researchId, new ArrayList<>());
        }

        public TriggerResearchData(ResourceLocation researchId, List<Integer> triggerComplete) {
            this.researchId = researchId;
            this.triggerComplete = new ArrayList<>(triggerComplete);
        }

        public ResourceLocation getResearchId() {
            return researchId;
        }

        protected List<Integer> getTriggerComplete() {
            return triggerComplete;
        }

        public boolean addComplete(int index) {
            if(triggerComplete.contains(index)) return false;

            return triggerComplete.add(index);
        }

        public boolean removeComplete(int index) {
            return triggerComplete.remove(index) != null;
        }

        public boolean isComplete(int index) {
            return triggerComplete.contains(index);
        }

        @Override
        public Codec<TriggerResearchData> codec() {
            return CODEC;
        }

        @Override
        public StreamCodec<FriendlyByteBuf, TriggerResearchData> streamCodec() {
            return STREAM_CODEC;
        }
    }

    public interface Event {
        void onEvent(ResourceLocation researchId, ResearchChangeType type);
    }
}
