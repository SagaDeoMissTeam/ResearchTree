package net.sixik.researchtree.client.render;

import dev.ftb.mods.ftblibrary.ui.Widget;
import net.minecraft.client.gui.GuiGraphics;
import net.sixik.researchtree.client.ClientUtils;
import net.sixik.sdmuilibrary.client.utils.renders.ShapesRenderHelper;

import java.util.ArrayList;
import java.util.List;

public class ConnectionRenderData {

    public final Widget mainWidget;
    public final List<Widget> connected = new ArrayList<>();

    public ConnectionRenderData(Widget mainWidget) {
        this.mainWidget = mainWidget;
    }

    public ConnectionRenderData addConnection(Widget connected) {
        this.connected.add(connected);
        return this;
    }

    /**
     * Рендер с права на лево
     */
    public void draw(GuiGraphics graphics, int offsetX, int offsetY, int w, int h) {
        int startPosX = ClientUtils.getPositionForConnectionX(mainWidget, offsetX, true);
        int startPosY = ClientUtils.getPositionForConnectionY(mainWidget, offsetY);

        for (Widget widget : connected) {
            int endPosX = ClientUtils.getPositionForConnectionX(widget, offsetX, false);
            int endPosY = ClientUtils.getPositionForConnectionY(widget, offsetY);

            ShapesRenderHelper.drawLineTC(graphics, startPosX, startPosY, endPosX, endPosY, 0.2f, 1F);
        }

    }
}
