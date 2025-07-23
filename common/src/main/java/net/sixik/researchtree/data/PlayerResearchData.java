package net.sixik.researchtree.data;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.StringTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.player.Player;
import net.sixik.researchtree.ResearchTree;
import net.sixik.researchtree.research.BaseResearch;

import java.io.File;
import java.nio.file.Path;
import java.util.*;

public class PlayerResearchData {

    public static final String FOLDER = "ResearchTree/";
    public static final String PLAYERS_FOLDER = "players/";

    public static final PlayerResearchData CLIENT = new PlayerResearchData();
    public static final PlayerResearchData SERVER = new PlayerResearchData();

    protected Map<UUID, List<ResourceLocation>> RESEARCH_DATA = new HashMap<>();

    public List<ResourceLocation> getResearch(Player player){
        return getResearch(player.getGameProfile().getId());
    }

    public List<ResourceLocation> getResearch(UUID playerId) {
        return RESEARCH_DATA.computeIfAbsent(playerId, (s) -> new ArrayList<>());
    }

    public boolean addResearch(Player player, BaseResearch research) {
        return RESEARCH_DATA.computeIfAbsent(player.getGameProfile().getId(), s -> new ArrayList<>()).add(research.getId());
    }

    public boolean addResearch(UUID playerId, BaseResearch research) {
        return RESEARCH_DATA.computeIfAbsent(playerId, s -> new ArrayList<>()).add(research.getId());
    }

    public boolean addResearch(Player player, ResourceLocation research) {
        return RESEARCH_DATA.computeIfAbsent(player.getGameProfile().getId(), s -> new ArrayList<>()).add(research);
    }

    public boolean addResearch(UUID playerId, ResourceLocation research) {
        return RESEARCH_DATA.computeIfAbsent(playerId, s -> new ArrayList<>()).add(research);
    }

    public boolean removeResearch(Player player, BaseResearch research) {
        return RESEARCH_DATA.computeIfAbsent(player.getGameProfile().getId(), s -> new ArrayList<>()).remove(research.getId());
    }

    public boolean removeResearch(UUID playerId, BaseResearch research) {
        return RESEARCH_DATA.computeIfAbsent(playerId, s -> new ArrayList<>()).remove(research.getId());
    }

    public boolean removeResearch(Player player, ResourceLocation research) {
        return RESEARCH_DATA.computeIfAbsent(player.getGameProfile().getId(), s -> new ArrayList<>()).remove(research);
    }

    public boolean removeResearch(UUID playerId, ResourceLocation research) {
        return RESEARCH_DATA.computeIfAbsent(playerId, s -> new ArrayList<>()).remove(research);
    }

    public void loadData(MinecraftServer server) {

    }

    public static void savePlayersData(Path path, Map<UUID, List<ResourceLocation>> playerData) {
        Path pathFolder = path.resolve(FOLDER).resolve(PLAYERS_FOLDER);
        File fileFolder = pathFolder.toFile();

        if (!fileFolder.exists()) {
            fileFolder.mkdirs();
        }

        for (Map.Entry<UUID, List<ResourceLocation>> entry : playerData.entrySet()) {
            UUID uuid = entry.getKey();
            try {
                File file = new File(fileFolder, uuid.toString() + ".data");
                CompoundTag nbt = new CompoundTag();
                ListTag researchNBTList = new ListTag();
                for (ResourceLocation resourceLocation : entry.getValue()) {
                    researchNBTList.add(StringTag.valueOf(resourceLocation.toString()));
                }

                nbt.put("research", researchNBTList);
                NbtIo.write(nbt, file.toPath());
            } catch (Exception e) {}
        }
    }

    
}
