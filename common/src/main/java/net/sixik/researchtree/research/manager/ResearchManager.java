package net.sixik.researchtree.research.manager;

import com.mojang.authlib.GameProfile;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.sixik.researchtree.api.FullCodecSerializer;
import net.sixik.researchtree.research.ResearchData;
import net.sixik.researchtree.utils.ResearchUtils;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class ResearchManager implements FullCodecSerializer<ResearchManager> {

    public static final Codec<ResearchManager> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    PlayerResearchData.CODEC.listOf().fieldOf("playersData").forGetter(ResearchManager::getPlayersResearchData)
            ).apply(instance, ResearchManager::new));

    public static final StreamCodec<FriendlyByteBuf, ResearchManager> STREAM_CODEC = StreamCodec.composite(
            PlayerResearchData.STREAM_CODEC.apply(ByteBufCodecs.list()), ResearchManager::getPlayersResearchData, ResearchManager::new
    );

    protected long lastTickTime = 0;

    protected final Object syncDataObject = new Object();
    protected final List<PlayerResearchData> playersResearchData = new ArrayList<>();


    protected @Nullable ResearchData researchData;

    public ResearchManager() {}

    protected ResearchManager(List<PlayerResearchData> playersResearchData) {
        this.playersResearchData.addAll(playersResearchData);
    }

    protected List<PlayerResearchData> getPlayersResearchData() {
        return playersResearchData;
    }

    public PlayerResearchData getOrCreatePlayerData(Player player) {
        return getOrCreatePlayerData(player.getGameProfile());
    }

    public PlayerResearchData getOrCreatePlayerData(GameProfile profile) {
        return getOrCreatePlayerData(profile.getId());
    }

    public PlayerResearchData getOrCreatePlayerData(UUID playerGameProfile) {
        synchronized (syncDataObject) {
            Optional<PlayerResearchData> optional = playersResearchData.stream().filter(data -> Objects.equals(playerGameProfile, data.getPlayerId())).findFirst();
            if (optional.isPresent()) return optional.get();
            PlayerResearchData playerData = new PlayerResearchData(playerGameProfile);
            playersResearchData.add(playerData);
            return playerData;
        }
    }

    public Optional<PlayerResearchData> getPlayerDataOptional(Player player) {
        return getPlayerDataOptional(player.getGameProfile());
    }

    public Optional<PlayerResearchData> getPlayerDataOptional(GameProfile profile) {
        return getPlayerDataOptional(profile.getId());
    }

    public Optional<PlayerResearchData> getPlayerDataOptional(UUID playerGameProfile) {
        synchronized (syncDataObject) {
            return playersResearchData.stream()
                    .filter(data -> Objects.equals(playerGameProfile, data.getPlayerId()))
                    .findFirst();
        }
    }

    public List<UUID> syncPlayerDataWithTeammates(UUID playerGameProfile) {
        TeammatesResearch teammatesResearch = getUnlockedTeammatesResearch(playerGameProfile);
        PlayerResearchData mainPlayerData = getOrCreatePlayerData(playerGameProfile);
        mainPlayerData.addResearch(teammatesResearch.researches);
        return teammatesResearch.teammates;
    }

    public void syncPlayerTeamData(UUID playerGameProfile) {
        for (UUID playerTeammate : ResearchUtils.getPlayerTeammates(playerGameProfile)) {
            syncPlayerDataWithTeammates(playerTeammate);
        }
    }

    public TeammatesResearch getUnlockedTeammatesResearch(UUID playerGameProfile) {
        TeammatesResearch teammatesResearch = new TeammatesResearch(new ArrayList<>(), new ArrayList<>());
        PlayerResearchData mainPlayerData = getOrCreatePlayerData(playerGameProfile);
        List<UUID> teammatesIds = ResearchUtils.getPlayerTeammates(playerGameProfile);

        for (UUID teammatesId : teammatesIds) {
            boolean founded = false;
            PlayerResearchData teammateData = getOrCreatePlayerData(teammatesId);

            for (ResourceLocation unlockedResearch : teammateData.getUnlockedResearch()) {

                if(!mainPlayerData.hasResearch(unlockedResearch) && !teammatesResearch.researches.contains(unlockedResearch)) {
                    teammatesResearch.researches.add(unlockedResearch);
                    founded = true;
                }
            }

            if(founded)
                teammatesResearch.teammates.add(teammatesId);
        }

        return teammatesResearch;
    }

    public final void tickResearchData() {
        long deltaTimeMs = System.currentTimeMillis() - lastTickTime;

        synchronized (syncDataObject) {
            for (PlayerResearchData data : playersResearchData) {
                data.tick(deltaTimeMs);
            }
        }

        lastTickTime = System.currentTimeMillis();
    }

    @Override
    public Codec<ResearchManager> codec() {
        return CODEC;
    }

    @Override
    public StreamCodec<FriendlyByteBuf, ResearchManager> streamCodec() {
        return STREAM_CODEC;
    }

    public void shutdown() {}

    public record TeammatesResearch(List<UUID> teammates, List<ResourceLocation> researches) {}
}
