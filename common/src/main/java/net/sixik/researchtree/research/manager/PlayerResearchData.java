package net.sixik.researchtree.research.manager;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.sixik.researchtree.ResearchTree;
import net.sixik.researchtree.api.FullCodecSerializer;

import java.util.*;
import java.util.function.Consumer;

public class PlayerResearchData implements FullCodecSerializer<PlayerResearchData> {

    public static final Codec<PlayerResearchData> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.STRING                        .fieldOf("playerId"        ).forGetter(PlayerResearchData::getPlayerIdString),
                    ResourceLocation.CODEC.listOf()     .fieldOf("unlockedResearch").forGetter(PlayerResearchData::getUnlockedResearch),
                    ResearchProgressData.CODEC.listOf() .fieldOf("progressResearch").forGetter(PlayerResearchData::getProgressData)
            ).apply(instance, PlayerResearchData::new));

    public static final StreamCodec<FriendlyByteBuf, PlayerResearchData> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8,                                      PlayerResearchData::getPlayerIdString,
            ResourceLocation.STREAM_CODEC.apply(ByteBufCodecs.list()),      PlayerResearchData::getUnlockedResearch,
            ResearchProgressData.STREAM_CODEC.apply(ByteBufCodecs.list()),  PlayerResearchData::getProgressData,
            PlayerResearchData::new
    );

    protected final UUID playerId;
    protected final List<ResourceLocation> unlockedResearch;
    protected final List<ResearchProgressData> progressData;

    protected final Object syncResearch = new Object();
    protected final Object syncProgress = new Object();

    public PlayerResearchData(UUID playerId) {
        this(playerId, new ArrayList<>(), new ArrayList<>());
    }

    public PlayerResearchData(UUID playerId, List<ResourceLocation> unlockedResearch, List<ResearchProgressData> progressData) {
        this.playerId = playerId;
        this.unlockedResearch = unlockedResearch;
        this.progressData = progressData;
    }

    protected PlayerResearchData(String playerId, List<ResourceLocation> resourceLocations, List<ResearchProgressData> researchProgressData) {
        this(UUID.fromString(playerId), resourceLocations, researchProgressData);
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

    public int getProgressResearchSize() {
        return progressData.size();
    }

    public void changeProgressResearch(ResourceLocation id, Consumer<ResearchProgressData> consumer) {
        synchronized (syncProgress) {
            progressData.stream().filter(data -> Objects.equals(id, data.getId())).findFirst().ifPresent(consumer);
        }
    }

    public void addResearch(ResourceLocation researchId) {
        synchronized (syncResearch) {
            unlockedResearch.add(researchId);
        }
    }

    public void addResearch(Collection<ResourceLocation> researchIds) {
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
                ResourceLocation.STREAM_CODEC,  ResearchProgressData::getId,
                ByteBufCodecs.VAR_LONG,        ResearchProgressData::getDurationMs,
                ByteBufCodecs.VAR_LONG,        ResearchProgressData::getElapsedMs,
                ByteBufCodecs.BOOL,            ResearchProgressData::isPaused,
                ResearchProgressData::new
        );

        private final ResourceLocation researchId;
        private final long durationMs;
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

        public long getElapsedMs() {
            return elapsedMs;
        }

        public boolean isPaused() {
            return isPaused;
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
}
