package net.sixik.researchtree.utils;

import dev.ftb.mods.ftblibrary.ui.Theme;
import dev.ftb.mods.ftblibrary.util.TooltipList;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.sixik.researchtree.client.ResearchInfoPanel;
import net.sixik.sdmuilibrary.client.utils.misc.RGBA;

import java.util.Optional;

public class ResearchRenderUtils {

    public static void drawScrollHeadBackground(GuiGraphics graphics, int x, int y, int w, int h) {
        RGBA.create(30,144,255).draw(graphics, x, y, w, h);
    }

    public static void drawScrollBackground(GuiGraphics graphics, int x, int y, int w, int h) {
        ResearchInfoPanel.LINE_COLOR.draw(graphics, x, y, w, h);
    }

    public static void drawTooltip(GuiGraphics graphics, int mouseX, int mouseY, TooltipList tooltipList, int zBuffer) {
        graphics.pose().translate(0.0F, 0.0F, zBuffer);
        graphics.setColor(1.0F, 1.0F, 1.0F, 0.8F);
        graphics.renderTooltip(Theme.DEFAULT.getFont(), tooltipList.getLines(), Optional.empty(), mouseX, Math.max(mouseY, 18));
        graphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
        tooltipList.reset();
    }

    public static boolean isMouseOver(int mouseX, int mouseY, int x, int y, int w, int h) {
        return mouseX >= x && mouseX <= x + w && mouseY >= y && mouseY <= y + h;
    }

    public static void drawLine(RGBA rgba, GuiGraphics graphics, int x, int y, int w, int h) {
        rgba.draw(graphics, x, y, w, h);
    }

    public static void drawLine(RGBA rgba, GuiGraphics graphics, int x, int y, int w, int h, int space) {
        rgba.draw(graphics, x + space, y, w - space * 2, h);
    }

    public static void drawTextWithCenterLine(RGBA rgba, GuiGraphics graphics, int x, int y, int w, int h, int space, Object text) {
        // Получаем строку из текста (Component или I18n)
        String str = text instanceof Component component ? component.getString() : I18n.get(text.toString());
        Theme theme = Theme.DEFAULT;

        // Вычисляем ширину текста и его позицию
        int textWidth = theme.getStringWidth(str);
        int textPosX = x + (w - textWidth) / 2;

        // Отрисовываем текст
        theme.drawString(graphics, str, textPosX, y, Theme.CENTERED);

        // Вычисляем позицию и размеры линии
        int lineWidth = Math.min(textWidth, w); // Линия не шире текста или области
        int linePosX = x + (w - lineWidth) / 2; // Центрируем линию
        int linePosY = y + theme.getFontHeight() + space; // Линия под текстом с учетом отступа

        // Проверяем, помещается ли линия в область по высоте
        if (linePosY + 1 <= y + h) {
            rgba.draw(graphics, linePosX, linePosY, lineWidth, 1);
        }
    }
}
