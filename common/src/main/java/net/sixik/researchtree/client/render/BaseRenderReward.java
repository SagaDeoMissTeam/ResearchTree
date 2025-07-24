package net.sixik.researchtree.client.render;

import dev.ftb.mods.ftblibrary.ui.Panel;
import dev.ftb.mods.ftblibrary.ui.Theme;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.resources.language.I18n;
import net.sixik.researchtree.client.ResearchInfoPanel;
import net.sixik.researchtree.client.ResearchScreen;
import net.sixik.researchtree.mixin.PanelAccessor;
import net.sixik.sdmuilibrary.client.utils.renders.GLRenderHelper;

import java.util.ArrayList;
import java.util.List;

public class BaseRenderReward extends Panel {

    protected RenderRequirements requirements;
    protected RenderRewards rewards;
    protected PanelAccessor accessor;
    protected int contentHeightCache;

    public BaseRenderReward(Panel panel) {
        super(panel);
        this.accessor = (PanelAccessor) this;
    }

    @Override
    public boolean getOnlyRenderWidgetsInside() {
        return false;
    }

    public void setRenders(RenderRequirements requirements, RenderRewards rewards) {
        this.requirements = requirements;
        this.rewards = rewards;


        contentHeightCache = this.requirements.getSize().y
                + this.rewards.getSize().y
                + Minecraft.getInstance().font.lineHeight * 3 + 4;
    }

    @Override
    public int getContentHeight() {
        return contentHeightCache;
    }

    @Override
    public void addWidgets() {

    }

    @Override
    public void alignWidgets() {

    }

    @Override
    public void drawBackground(GuiGraphics graphics, Theme theme, int x, int y, int w, int h) {
        ResearchScreen.DEFAULT_BACKGROUND.drawRoundFill(graphics, x, y, w, h, 4);
        setOffset(true);
        int currentY = y + 2;
        boolean isOver = isMouseOver();

        // Храним данные для отложенного рендеринга тултипов
        record TooltipData(RenderBase<?> renderable, int mouseX, int mouseY, int xOffset, int yOffset, int yPos, RenderTooltip tooltip) {}
        List<TooltipData> tooltips = new ArrayList<>(2);

        // Рендеринг requirements
        if (requirements != null && !requirements.renderData.isEmpty()) {
            GLRenderHelper.enableScissor(graphics, x, y, w, h);
            String text = I18n.get("research.ui.info.requirements");
            int textW = theme.getStringWidth(text);
            int textPos = x + (w - textW) / 2;
            ResearchInfoPanel.LINE_COLOR.draw(graphics, textPos - 2, currentY - 2 + accessor.getOffsetY(), textW + 2, theme.getFontHeight() + 2);
            theme.drawString(graphics, text, textPos, currentY + accessor.getOffsetY());
            currentY += theme.getFontHeight() + 2;
            requirements.draw(graphics, 0, currentY, 0, accessor.getOffsetY());
            GLRenderHelper.disableScissor(graphics);
            if (isOver) {
                tooltips.add(new TooltipData(requirements, getMouseX(), getMouseY(), 0, accessor.getOffsetY(), currentY, new RenderTooltip(200, 100)));
            }
            currentY += requirements.getSize().y + 4;
        }

        // Рендеринг rewards
        if (rewards != null && !rewards.renderData.isEmpty()) {
            GLRenderHelper.enableScissor(graphics, x, y, w, h);
            String text = I18n.get("research.ui.info.rewards");
            int textW = theme.getStringWidth(text);
            int textPos = x + (w - textW) / 2;
            ResearchInfoPanel.LINE_COLOR.draw(graphics, textPos - 2, currentY - 2 + accessor.getOffsetY(), textW + 2, theme.getFontHeight() + 2);
            theme.drawString(graphics, text, textPos, currentY + accessor.getOffsetY());
            currentY += theme.getFontHeight() + 2;
            rewards.draw(graphics, 0, currentY, 0, accessor.getOffsetY());
            GLRenderHelper.disableScissor(graphics);
            if (isOver) {
                tooltips.add(new TooltipData(rewards, getMouseX(), getMouseY(), 0, accessor.getOffsetY(), currentY, new RenderTooltip(200, 100)));
            }
        }

        // Отложенный рендеринг тултипов после всех операций с scissor
        for (TooltipData tooltip : tooltips) {
            tooltip.renderable.drawTooltip(graphics, tooltip.mouseX, tooltip.mouseY, tooltip.xOffset, tooltip.yPos, tooltip.xOffset, tooltip.yOffset, tooltip.tooltip);
        }

        setOffset(false);
    }
}
