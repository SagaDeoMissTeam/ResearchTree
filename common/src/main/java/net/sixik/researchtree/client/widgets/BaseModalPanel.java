package net.sixik.researchtree.client.widgets;

import dev.ftb.mods.ftblibrary.ui.Panel;
import dev.ftb.mods.ftblibrary.ui.Theme;
import net.minecraft.client.gui.GuiGraphics;

public abstract class BaseModalPanel extends Panel {

    public BaseModalPanel(Panel panel) {
        super(panel);
    }

    @Override
    public void draw(GuiGraphics graphics, Theme theme, int x, int y, int w, int h) {
        graphics.pose().pushPose();
        graphics.pose().translate(0,0,800);
        super.draw(graphics, theme, x, y, w, h);
        graphics.pose().popPose();
    }
}
