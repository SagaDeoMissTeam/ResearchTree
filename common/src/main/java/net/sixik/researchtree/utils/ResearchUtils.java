package net.sixik.researchtree.utils;

import dev.ftb.mods.ftblibrary.ui.ScreenWrapper;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.sixik.researchtree.ResearchTree;
import net.sixik.researchtree.client.ResearchScreen;
import net.sixik.researchtree.research.BaseResearch;
import net.sixik.researchtree.research.ResearchStage;
import net.sixik.researchtree.research.manager.ClientResearchManager;
import net.sixik.researchtree.research.manager.PlayerResearchData;
import net.sixik.researchtree.research.manager.ResearchManager;
import net.sixik.researchtree.research.manager.ServerResearchManager;
import net.sixik.researchtree.research.requirements.Requirements;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class ResearchUtils {

    public static ResearchManager getManager(boolean isClient) {
        return isClient ? ClientResearchManager.getInstance() : ServerResearchManager.getInstance();
    }

    public static Optional<ResearchManager> getManagerOptional(boolean isClient) {
        return Optional.ofNullable(getManager(isClient));
    }

    @SuppressWarnings("unchecked")
    public static <T extends ResearchManager> T getManagerCast(boolean isClient) {
        return isClient ? (T)ClientResearchManager.getInstance() : (T)ServerResearchManager.getInstance();
    }

    public static boolean hasResearch(Player player, BaseResearch research, boolean isClient) {
        return getManager(isClient).getOrCreatePlayerData(player).hasResearch(research.getId());
    }

    public static boolean isStartResearch(Player player, BaseResearch research, boolean isClient) {
        return getResearchStage(player, research, isClient) == ResearchStage.START_RESEARCH;
    }

    public static boolean isResearched(Player player, BaseResearch research, boolean isClient) {
        return getResearchStage(player, research, isClient) == ResearchStage.RESEARCHED;
    }

    public static double getPercentResearch(Player player, BaseResearch research, boolean isClient) {
        PlayerResearchData playerData = getManager(isClient).getOrCreatePlayerData(player);
        Optional<PlayerResearchData.ResearchProgressData> researchOptional = playerData.getProgressResearch(research.getId());
        return researchOptional.map(PlayerResearchData.ResearchProgressData::getProgressPercentDouble).orElse(0.0d);
    }

    public static Optional<PlayerResearchData.ResearchProgressData> getResearchData(Player player, BaseResearch research, boolean isClient) {
        PlayerResearchData playerData = getManager(isClient).getOrCreatePlayerData(player);
        return playerData.getProgressResearch(research.getId());
    }

    public static double getRefundPercent(Player player, BaseResearch research) {
        double per = research.getRefundPercent();
        return per <= -1 ? ResearchTree.MOD_CONFIG.getDefaultRefundPercent() : per;
    }

    public static ResearchStage getResearchStage(Player player, BaseResearch research, boolean isClient) {
        return getResearchStage(player, research, getManager(isClient));
    }

    public static ResearchStage getResearchStage(Player player, BaseResearch research, ResearchManager researchManager) {
        return getResearchStage(player,research, researchManager.getOrCreatePlayerData(player));
    }

    public static ResearchStage getResearchStage(Player player, BaseResearch research, PlayerResearchData playerData) {
        if(playerData.containsInProgress(research.getId())) return ResearchStage.START_RESEARCH;
        if(playerData.containsInUnlockedResearch(research.getId())) return ResearchStage.RESEARCHED;
        if(research.isLocked(player)) return ResearchStage.LOCKED;
        return ResearchStage.UN_RESEARCHED;
    }

    public static boolean canStartResearch(Player player, BaseResearch research, ResearchManager manager) {
        if(getResearchStage(player, research, manager) != ResearchStage.UN_RESEARCHED) return false;

        for (Requirements requirement : research.getRequirements()) {
            if(!requirement.canExecute(player, research)) return false;
        }

        return true;
    }

    public static boolean canStartResearch(Player player, BaseResearch research, boolean isClient) {
        return canStartResearch(player, research, getManager(isClient));
    }

    public static boolean isResearchParentsResearched(Player player, BaseResearch research, boolean isClient) {
        return isResearchParentsResearched(player, research, research.getCountParentsToResearch(), isClient);
    }

    public static boolean isResearchParentsResearched(Player player, BaseResearch research, int count, boolean isClient) {
        return countResearchedParents(player, research, isClient) >= count;
    }

    public static int countResearchedParents(Player player, BaseResearch research, boolean isClient) {
        int count = 0;

        ResearchManager manger = getManager(isClient);
        for (BaseResearch parentResearch : research.getParentResearch()) {
            if(getResearchStage(player, parentResearch, manger) == ResearchStage.RESEARCHED) count++;
        }

        return count;
    }

    public static List<UUID> getPlayerTeammates(UUID playerGameProfile) {
        return new ArrayList<>();
    }

    @Environment(EnvType.CLIENT)
    public static void openResearchScreen(boolean isOpen) {
        if(isOpen) {
            new ResearchScreen().openGui();
        } else {
            if(Minecraft.getInstance().screen instanceof ScreenWrapper wrapper && wrapper.getGui() instanceof ResearchScreen screen)
                screen.closeGui();
        }
    }

    public static int countFromPercents(int count, double percent) {
        int refundCount = (int) Math.floor(count * (percent / 100.0));
        if (refundCount == 0 && percent > 0 && count > 0) {
            refundCount = 1;
        }
        return refundCount;
    }
}
