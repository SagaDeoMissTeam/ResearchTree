package net.sixik.researchtree.research.manager;

import com.mojang.authlib.GameProfile;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.sixik.researchtree.api.interfaces.FullCodecSerializer;
import net.sixik.researchtree.network.fromServer.SendPlayerResearchDataS2C;
import net.sixik.researchtree.research.BaseResearch;
import net.sixik.researchtree.research.ResearchData;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
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

    protected final Object syncPlayerDataObject = new Object();
    protected final List<PlayerResearchData> playersResearchData = new ArrayList<>();

    protected @Nullable ResearchData researchData;
    protected Logger logger;

    public ResearchManager(Logger logger) {
        this.logger = logger;
    }

    protected ResearchManager(List<PlayerResearchData> playersResearchData) {
        this.playersResearchData.addAll(playersResearchData);
    }

    protected List<PlayerResearchData> getPlayersResearchData() {
        return playersResearchData;
    }

    public final boolean updateTriggerDataAndSync(Player player) {
        if(player instanceof ServerPlayer serverPlayer) {

            boolean value = updateTriggerData(player);
            if(value)
                SendPlayerResearchDataS2C.sendTo(serverPlayer);

            return value;
        }

        return false;
    }

    public final boolean updateTriggerData(Player player) {
        UUID playerGameProfile = player.getGameProfile().getId();
        Optional<PlayerResearchData> optional;

        synchronized (syncPlayerDataObject) {
            optional = getPlayerDataOptionalUnSafe(playerGameProfile);
        }

        return optional.filter(data -> updateTriggerData(data, player)).isPresent();

    }

    public final boolean updateTriggerData(PlayerResearchData data, Player player) {
        if(this instanceof ServerResearchManager serverResearchManager) {
            for (BaseResearch allResearch : serverResearchManager.getAllResearches()) {
                if(!allResearch.hasTrigger()) continue;
                if(allResearch.isResearched(player, false)) {
                    data.removeTriggerData(allResearch.getId());
                } else {
                    if (!allResearch.isCanResearch(player, false)) continue;

                    if (!allResearch.isResearched(player, false))
                        data.getTriggerDataOrCreate(allResearch.getId());
                }
            }

            return true;
        }
        return false;
    }

    public PlayerResearchData getOrCreatePlayerData(Player player) {
        synchronized (syncPlayerDataObject) {
            UUID playerGameProfile = player.getGameProfile().getId();
            Optional<PlayerResearchData> optional = getPlayerDataOptionalUnSafe(playerGameProfile);
            if (optional.isPresent()) return optional.get();
            PlayerResearchData playerData = new PlayerResearchData(playerGameProfile);
            updateTriggerData(playerData, player);
            playersResearchData.add(playerData);
            return playerData;
        }
    }

    public PlayerResearchData getOrCreatePlayerData(GameProfile profile) {
        return getOrCreatePlayerData(profile.getId());
    }

    public PlayerResearchData getOrCreatePlayerData(UUID playerGameProfile) {
        synchronized (syncPlayerDataObject) {
            Optional<PlayerResearchData> optional = getPlayerDataOptionalUnSafe(playerGameProfile);
            if (optional.isPresent()) return optional.get();
            PlayerResearchData playerData = new PlayerResearchData(playerGameProfile);
            playersResearchData.add(playerData);
            return playerData;
        }
    }

    protected Optional<PlayerResearchData> getPlayerDataOptionalUnSafe(UUID gameProfileId) {
        return playersResearchData.stream().filter(data -> Objects.equals(gameProfileId, data.getPlayerId())).findFirst();
    }

    public Optional<PlayerResearchData> getPlayerDataOptional(Player player) {
        return getPlayerDataOptional(player.getGameProfile());
    }

    public Optional<PlayerResearchData> getPlayerDataOptional(GameProfile profile) {
        return getPlayerDataOptional(profile.getId());
    }

    public Optional<PlayerResearchData> getPlayerDataOptional(UUID playerGameProfile) {
        synchronized (syncPlayerDataObject) {
            return playersResearchData.stream()
                    .filter(data -> Objects.equals(playerGameProfile, data.getPlayerId()))
                    .findFirst();
        }
    }

    public final void tickResearchData() {
        long deltaTimeMs = System.currentTimeMillis() - lastTickTime;

        synchronized (syncPlayerDataObject) {
            for (PlayerResearchData data : playersResearchData) {
                if(data.playerOnline)
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

    public final void loadPlayerData(Path path, String fileName) {
        try {
            CompoundTag nbt = NbtIo.read(path.resolve(fileName));
            if(nbt == null || !nbt.contains("player_data")) return;
            Tag listNbtData = nbt.get("player_data");

            Pair<List<PlayerResearchData>, Tag> value = PlayerResearchData.CODEC.listOf().decode(NbtOps.INSTANCE, listNbtData).getOrThrow();

            synchronized (syncPlayerDataObject) {
                this.playersResearchData.clear();
                this.playersResearchData.addAll(value.getFirst());
            }
        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    public final void savePlayerData(Path path, String fileName) {
        Tag nbt;

        synchronized (syncPlayerDataObject) {
            nbt = PlayerResearchData.CODEC.listOf().encodeStart(NbtOps.INSTANCE, getPlayersResearchData()).getOrThrow();
        }

        CompoundTag dataNbt = new CompoundTag();
        dataNbt.put("player_data", nbt);
        try {
            File file = path.toFile();
            if(!file.exists()) {
                file.mkdirs();
            }
            Path pt = path.resolve(fileName);
            file = pt.toFile();
            if(!file.exists())
                file.createNewFile();

            NbtIo.write(dataNbt, pt);
        } catch (IOException e) {
            logger.error(e.toString());
        }
    }
}
