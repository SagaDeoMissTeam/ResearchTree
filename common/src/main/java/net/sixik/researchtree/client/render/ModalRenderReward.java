package net.sixik.researchtree.client.render;

import dev.ftb.mods.ftblibrary.ui.Panel;

public class ModalRenderReward extends BaseRenderReward{
    public ModalRenderReward(Panel panel) {
        super(panel);
    }

//    @Override
//    public void drawBackground(GuiGraphics graphics, Theme theme, int x, int y, int w, int h) {
//        ResearchScreen.DEFAULT_BACKGROUND.drawRoundFill(graphics, x, y, w, h, 4);
//        setOffset(true);
//        int currentY = y + 2;
//        boolean isOver = isMouseOver();
//
//        record TooltipData(RenderBase<?> renderable, int mouseX, int mouseY, int xOffset, int yOffset, int yPos, RenderTooltip tooltip) {}
//        List<TooltipData> tooltips = new ArrayList<>(2);
//
//        // Рендеринг requirements
//        if (requirements != null && !requirements.renderData.isEmpty()) {
//            requirements.draw(graphics, 0, currentY, 0, accessor.getOffsetY());
//            if (isOver) {
//                tooltips.add(new TooltipData(requirements, getMouseX(), getMouseY(), 0, accessor.getOffsetY(), currentY, new RenderTooltip(200, 100)));
//            }
//            currentY += requirements.getSize().y + 4;
//        }
//
//
//        // Рендеринг rewards
//        if (rewards != null && !rewards.renderData.isEmpty()) {
//            rewards.draw(graphics, 0, currentY, 0, accessor.getOffsetY());
//            if (isOver) {
//                tooltips.add(new TooltipData(rewards, getMouseX(), getMouseY(), 0, accessor.getOffsetY(), currentY, new RenderTooltip(200, 100)));
//            }
//        }
//
//        for (TooltipData tooltip : tooltips) {
//            tooltip.renderable.drawTooltip(graphics, tooltip.mouseX, tooltip.mouseY, tooltip.xOffset, tooltip.yPos, tooltip.xOffset, tooltip.yOffset, tooltip.tooltip);
//        }
//
//        setOffset(false);
//    }
}
