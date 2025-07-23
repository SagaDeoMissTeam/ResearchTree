package net.sixik.researchtree.utils;

import dev.ftb.mods.ftblibrary.ui.ScreenWrapper;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.sixik.researchtree.DebugConstants;
import net.sixik.researchtree.ResearchTree;
import net.sixik.researchtree.client.ResearchScreen;
import net.sixik.researchtree.research.BaseResearch;
import net.sixik.researchtree.research.ResearchStage;
import net.sixik.researchtree.research.manager.ClientResearchManager;
import net.sixik.researchtree.research.manager.ResearchManager;
import net.sixik.researchtree.research.manager.ServerResearchManager;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ResearchUtils {

    public static ResearchManager getManager(boolean isClient) {
        return isClient ? ClientResearchManager.getInstance() : ServerResearchManager.getInstance();
    }

    @SuppressWarnings("unchecked")
    public static <T extends ResearchManager> T getManagerCast(boolean isClient) {
        return isClient ? (T)ClientResearchManager.getInstance() : (T)ServerResearchManager.getInstance();
    }

    public static boolean hasResearch(Player player, BaseResearch research) {
        if(DebugConstants.debug)
            return DebugConstants.hasResearch;

        return true;
    }

    public static boolean isStartResearch(Player player, BaseResearch research) {
        return getResearchStage(player, research) == ResearchStage.START_RESEARCH;
    }

    public static boolean isResearched(Player player, BaseResearch research) {
        return getResearchStage(player, research) == ResearchStage.RESEARCHED;
    }

    public static double getPercentResearch(Player player, BaseResearch research) {
        return 100;
    }

    public static double getRefundPercent(Player player, BaseResearch research) {
        double per = research.getRefundPercent();
        return per <= -1 ? ResearchTree.MOD_CONFIG.getDefaultRefundPercent() : per;
    }

    public static ResearchStage getResearchStage(Player player, BaseResearch research) {
        return ResearchStage.START_RESEARCH;
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
}
