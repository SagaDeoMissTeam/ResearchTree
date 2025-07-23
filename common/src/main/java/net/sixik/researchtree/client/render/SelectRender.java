package net.sixik.researchtree.client.render;

import dev.ftb.mods.ftblibrary.icon.Color4I;
import dev.ftb.mods.ftblibrary.ui.GuiHelper;
import dev.ftb.mods.ftblibrary.ui.Widget;
import net.minecraft.client.gui.GuiGraphics;

import java.util.Arrays;
import java.util.List;

public class SelectRender {

    protected List<Widget> widget;
    protected final long endTime;
    protected boolean end = false;

    protected Color4I renderColor = Color4I.rgb(16777215);
    protected int flipSpeed = 15;

    protected boolean reverse = false;

    public SelectRender(long time, Widget... widgets) {
        this(Arrays.stream(widgets).toList(), time);
    }


    public SelectRender(List<Widget> widget, long time) {
        this.widget = widget;
        endTime = System.currentTimeMillis() + time;
    }

    public boolean isEnd() {
        return end;
    }

    public void draw(GuiGraphics graphics, int offsetX, int offsetY) {
        if(!end) {

            if(endTime != -1 && System.currentTimeMillis() >= endTime)
                end = true;

            if(reverse) {
                renderColor = renderColor.withAlpha(Math.clamp(renderColor.alphai() - flipSpeed, 0, 255));
                if(renderColor.alphai() == 0)
                    reverse = false;
            } else {
                renderColor = renderColor.withAlpha(Math.clamp(renderColor.alphai() + flipSpeed, 0, 255));
                if(renderColor.alphai() == 255)
                    reverse = true;
            }
        }

        for (Widget w : widget) {
            GuiHelper.drawHollowRect(graphics, w.getX() + offsetX, w.getY() + offsetY, w.getWidth(), w.getHeight(), renderColor, false);
        }

    }

    public void setEnd() {
        end = true;
    }

}
