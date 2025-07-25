package net.sixik.researchtree.client.render;

import dev.ftb.mods.ftblibrary.ui.Widget;
import net.minecraft.client.gui.GuiGraphics;
import net.sixik.researchtree.client.ClientUtils;
import net.sixik.researchtree.client.ResearchWidget;
import net.sixik.researchtree.utils.ResearchRenderUtils;
import net.sixik.sdmuilibrary.client.utils.renders.ShapesRenderHelper;

import java.util.ArrayList;
import java.util.List;

public class ConnectionRenderData {

    public final ResearchWidget mainWidget;
    public final List<ResearchWidget> connected = new ArrayList<>();

    public ConnectionRenderData(ResearchWidget mainWidget) {
        this.mainWidget = mainWidget;
    }

    public ConnectionRenderData addConnection(ResearchWidget connected) {
        this.connected.add(connected);
        return this;
    }

    /**
     * Рендер с права на лево
     */
    public void draw(GuiGraphics graphics, int offsetX, int offsetY, int w, int h) {
        int startPosX = ClientUtils.getPositionForConnectionX(mainWidget, offsetX, true);
        int startPosY = ClientUtils.getPositionForConnectionY(mainWidget, offsetY);

        for (ResearchWidget widget : connected) {
            int endPosX = ClientUtils.getPositionForConnectionX(widget, offsetX, false);
            int endPosY = ClientUtils.getPositionForConnectionY(widget, offsetY);

            boolean researched = widget.researched && mainWidget.researched;

            ResearchRenderUtils.drawLineTC(graphics, startPosX, startPosY, endPosX, endPosY,
                    researched ? 0 : 0.5f,
                    1F,
                    researched ? 3f : 2f
            );
        }

    }
}
