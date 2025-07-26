package net.sixik.researchtree.utils;

import net.sixik.researchtree.client.HideRender;
import net.sixik.researchtree.research.BaseResearch;

public class ResearchLockUtils {

    public static HideRender getRender(BaseResearch research) {
        switch (research.hideTypeRender) {
            case HIDE_STYLE -> {
                return HideRender.HIDDEN;
            }
            case BLACKOUT_STYLE -> {
                return HideRender.ALPHA;
            }
            default -> {
                return HideRender.NORMAL;
            }
        }
    }
}
