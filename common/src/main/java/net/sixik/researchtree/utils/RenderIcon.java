package net.sixik.researchtree.utils;

import dev.ftb.mods.ftblibrary.icon.Icon;
import net.sixik.researchtree.ResearchTree;

public interface RenderIcon {

    Icon CONTRACT = getIcon("contract");
    Icon FAMILY_TREE = getIcon("family-tree");
    Icon CANCEL = getIcon("cancel");
    Icon WIREFRAME_GLOBE = getIcon("wireframe-globe");
    Icon OPEN_BOOK = getIcon("open-book");
    Icon CONFIRM = getIcon("confirmed");
    Icon UNKNOWN = getIcon("uncertainty");


    static Icon getIcon(String id) {
        return Icon.getIcon(ResearchTree.MODID + ":textures/icons/" + id + ".png");
    }
}
