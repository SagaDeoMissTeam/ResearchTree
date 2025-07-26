package net.sixik.researchtree.utils;

import dev.ftb.mods.ftblibrary.ui.ScreenWrapper;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.sixik.researchtree.ResearchTree;
import net.sixik.researchtree.api.managers.StageManager;
import net.sixik.researchtree.client.ResearchScreen;
import net.sixik.researchtree.registers.ModRegisters;
import net.sixik.researchtree.research.BaseResearch;
import net.sixik.researchtree.research.ResearchStage;
import net.sixik.researchtree.research.manager.ClientResearchManager;
import net.sixik.researchtree.research.manager.PlayerResearchData;
import net.sixik.researchtree.research.manager.ResearchManager;
import net.sixik.researchtree.research.manager.ServerResearchManager;
import net.sixik.researchtree.research.requirements.Requirements;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public class ResearchUtils {

    public static ResearchManager getManager(boolean isClient) {
        return isClient ? ClientResearchManager.getInstance() : ServerResearchManager.getInstance();
    }

    public static Optional<ResearchManager> getManagerOptional(boolean isClient) {
        return Optional.ofNullable(getManager(isClient));
    }

    public static ResearchManager getFirstManager() {
        return ServerResearchManager.getInstance() != null ? ServerResearchManager.getInstance() : ClientResearchManager.getInstance();
    }

    @SuppressWarnings("unchecked")
    public static <T extends ResearchManager> T getManagerCast(boolean isClient) {
        return isClient ? (T)ClientResearchManager.getInstance() : (T)ServerResearchManager.getInstance();
    }

    public static boolean hasResearch(Player player, BaseResearch research, boolean isClient) {
        return getManager(isClient).getOrCreatePlayerData(player).hasResearch(research.getId());
    }

    public static boolean isLockedResearch(Player player, BaseResearch research, boolean isClient) {
        return getResearchStage(player,research,isClient) == ResearchStage.LOCKED;
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

        if(!isResearchParentsResearched(player, research, manager instanceof ClientResearchManager)) return false;

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

    public static boolean addStage(ServerPlayer player, String stage) {
        boolean valueAny = false;

        for (StageManager stageManager : ModRegisters.getStageManagers()) {
            if(stageManager.addStage(player, stage))
                valueAny = true;
        }

        return valueAny;
    }

    public static boolean addStage(ServerPlayer player, Collection<String> stages) {
        boolean valueAny = false;

        for (StageManager stageManager : ModRegisters.getStageManagers()) {
            if(stageManager.addStage(player, stages))
                valueAny = true;

        }

        return valueAny;
    }

    public static boolean removeStage(ServerPlayer player, String stage) {
        boolean valueAny = false;

        for (StageManager stageManager : ModRegisters.getStageManagers()) {
            if(stageManager.removeStage(player, stage))
                valueAny = true;
        }

        return valueAny;
    }

    public static boolean removeStage(ServerPlayer player, Collection<String> stages) {
        boolean valueAny = false;

        for (StageManager stageManager : ModRegisters.getStageManagers()) {
            if(stageManager.removeStage(player, stages))
                valueAny = true;
        }

        return valueAny;
    }

    public static boolean hasStage(Player player, String stage) {
        for (StageManager stageManager : ModRegisters.getStageManagers()) {
            if(stageManager.hasStage(player, stage)) return true;
        }

        return false;
    }

    public static boolean hasStages(Player player, Collection<String> stage) {
        for (StageManager stageManager : ModRegisters.getStageManagers()) {
            if(stageManager.hasStages(player, stage)) return true;
        }

        return false;
    }

    public static Collection<String> getStages(Player player) {
        List<String> stages = new ArrayList<>();
        for (StageManager stageManager : ModRegisters.getStageManagers()) {
            stages.addAll(stageManager.getStages(player));
        }

        return stages;
    }
}
