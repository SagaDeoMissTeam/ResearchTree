package net.sixik.researchtree.client;

import dev.ftb.mods.ftblibrary.ui.Panel;
import dev.ftb.mods.ftblibrary.ui.Widget;
import net.minecraft.client.Minecraft;
import net.minecraft.core.HolderLookup;
import net.sixik.researchtree.mixin.PanelAccessor;

public class ClientUtils {

    public static PanelAccessor accessor(Panel panel) {
        return (PanelAccessor)panel;
    }

    public static int getPositionForConnectionX(Widget widget, int offsetX, boolean isLeft) {
        return (widget.getX() + offsetX) + (isLeft ? 1 : widget.getWidth());
    }

    public static int getPositionForConnectionY(Widget widget, int offsetY) {
        return (widget.getY() + offsetY) + (widget.getHeight() / 2);
    }

    public static HolderLookup.Provider holder() {
        return Minecraft.getInstance().level.registryAccess();
    }

}
