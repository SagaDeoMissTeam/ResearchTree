package net.sixik.researchtree.client.widgets;

import dev.ftb.mods.ftblibrary.ui.Panel;
import dev.ftb.mods.ftblibrary.ui.Theme;
import dev.ftb.mods.ftblibrary.ui.Widget;
import net.minecraft.client.gui.GuiGraphics;
import net.sixik.researchtree.client.ResearchScreen;
import net.sixik.researchtree.client.debug.ClientDebugUtils;
import net.sixik.researchtree.utils.ResearchRenderUtils;
import net.sixik.sdmuilibrary.client.utils.misc.RGBA;

public class ProgressBarWidget extends Widget {

    public static final RGBA BACKGROUND = ResearchScreen.DEFAULT_WIDGET_COLOR;

    private double minValue = 0.0;
    private double maxValue = 100.0;
    private double currentValue = 0.0;
    private float progress = 0.0f;

    public ProgressBarWidget(Panel p) {
        super(p);
    }

    public void setMaxValue(Number maxValue) {
        this.maxValue = maxValue.doubleValue();
        if (this.maxValue <= this.minValue) {
            this.maxValue = this.minValue + 1.0; // Предотвращаем деление на ноль
        }
        updateProgress();
    }

    public double getMaxValue() {
        return maxValue;
    }

    public double getMinValue() {
        return minValue;
    }

    public void setValue(Number value) {
        this.currentValue = value.doubleValue();
        updateProgress();
    }

    public void setValue(boolean value) {
        this.currentValue = value ? maxValue : minValue;
        updateProgress();
    }

    private void updateProgress() {
        double clampedValue = Math.clamp(currentValue, minValue, maxValue);
        this.progress = (float) ((clampedValue - minValue) / (maxValue - minValue));
    }

    @Override
    public void draw(GuiGraphics graphics, Theme theme, int x, int y, int w, int h) {
        // Рендеринг границы
        if (w > 0) {
            BACKGROUND.draw(graphics, x, y, w,h);
        }

        ResearchRenderUtils.drawScrollBackground(graphics, x + 2, y + 2, w - 4, h - 4);

        // Рендеринг заполненной части
        int fillWidth = (int) ((w - 4) * progress);
        if (fillWidth > 0) {
            ResearchRenderUtils.drawScrollHeadBackground(graphics,
                    x + 2, y + 2,
                    fillWidth,
                    h - 4
            );
        }

        String percentageText = String.format("%d%%", (int) (progress * 100));
        int textWidth = theme.getStringWidth(percentageText);
        int textX = x + (w - textWidth) / 2;
        int textY = y + (h - theme.getFontHeight()) / 2;
        theme.drawString(graphics, percentageText, textX, textY, 0xFF_FFFFFF);


//        double animationDuration = 2.0;
//        double step = (maxValue - minValue) / (60.0 * animationDuration);
//
//        if (count <= minValue) {
//            reverse = false;
//            count = minValue;
//        } else if (count >= maxValue) {
//            reverse = true;
//            count = maxValue;
//        }
//
//        count += reverse ? -step : step;
//        setValue(count);
    }

    protected double count = 0;
    protected boolean reverse = false;
}
