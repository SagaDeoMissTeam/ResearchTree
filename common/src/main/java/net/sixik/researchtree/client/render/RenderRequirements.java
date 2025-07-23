package net.sixik.researchtree.client.render;

import dev.ftb.mods.ftblibrary.ui.Theme;
import dev.ftb.mods.ftblibrary.util.TooltipList;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.sixik.researchtree.client.ResearchWidget;
import net.sixik.researchtree.research.requirements.Requirements;

import java.util.ArrayList;
import java.util.List;

public class RenderRequirements extends RenderBase<Requirements> {

    public RenderRequirements(ResearchWidget researchWidget, int x, int y, int w, int h, int gapX, int gapY, boolean centerVertically) {
        super(researchWidget, x, y, w, h, gapX, gapY, centerVertically);
    }

    @Override
    protected List<Requirements> getElements() {
        return new ArrayList<>(researchWidget.research.getRequirementsWithParents(Minecraft.getInstance().player));
    }

    @Override
    protected DrawData<Requirements> createDrawData(Requirements requirement, int x, int y, int w, int h) {
        return new DrawData<Requirements>(requirement, x, y, w, h);
    }

    @Override
    protected void drawElement(GuiGraphics graphics, Object element, int x, int y, int w, int h) {
        ((Requirements) element).draw(graphics, x, y, w, h);
    }

    @Override
    protected void drawElementTooltip(GuiGraphics graphics, Object element, int x, int y, RenderTooltip tooltip, TooltipList list) {
        ((Requirements) element).drawCustomTooltip(graphics, x, y, tooltip, list);
        Theme.DEFAULT.drawString(graphics, " ", x,y);
    }
}
