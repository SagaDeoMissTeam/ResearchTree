package net.sixik.researchtree.utils;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import dev.ftb.mods.ftblibrary.ui.Theme;
import dev.ftb.mods.ftblibrary.util.TooltipList;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.sixik.researchtree.client.ResearchInfoPanel;
import net.sixik.sdmuilibrary.client.utils.misc.RGBA;

import java.util.Optional;
import java.util.Random;

public class ResearchRenderUtils {

    public static void drawScrollHeadBackground(GuiGraphics graphics, int x, int y, int w, int h) {
        RGBA.create(30,144,255).draw(graphics, x, y, w, h);
    }

    public static void drawScrollBackground(GuiGraphics graphics, int x, int y, int w, int h) {
        ResearchInfoPanel.LINE_COLOR.draw(graphics, x, y, w, h);
    }

    public static void drawTooltip(GuiGraphics graphics, int mouseX, int mouseY, TooltipList tooltipList) {
        drawTooltip(graphics,mouseX,mouseY,tooltipList, 200);
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

    public static void drawLineTC(GuiGraphics poseStack, int x, int y, int x2, int y2, float instability, float op, float lineSize) {
        if (instability > 0.01F) {
            double dist = (double)(Mth.sqrt((float)((x - x2) * (x - x2) + (y - y2) * (y - y2))) * instability);
            double xd = (double)(x2 - x) / dist;
            double yd = (double)(y2 - y) / dist;
            Random rand = new Random();
            int xr = 0;
            int yr = 0;
            int inc = (int)Math.floor(dist - (double)1.0F);

            int a;
            for(a = 0; a < inc; ++a) {
                int xrn = rand.nextInt(2) - rand.nextInt(2);
                int yrn = rand.nextInt(2) - rand.nextInt(2);
                drawLineTCSize(poseStack, (int)((double)x + xd * (double)a) + xr, (int)((double)y + yd * (double)a) + yr, (int)((double)x + xd * (double)(a + 1)) + xrn, (int)((double)y + yd * (double)(a + 1)) + yrn, op, lineSize);
                xr = xrn;
                yr = yrn;
            }

            drawLineTCSize(poseStack, (int)((double)x + xd * (double)a) + xr, (int)((double)y + yd * (double)a) + yr, x2, y2, op, lineSize);
        } else {
            drawLineTCSize(poseStack, x, y, x2, y2, op, lineSize);
        }

    }

    public static void drawLineTCSize(GuiGraphics poseStack, int x, int y, int x2, int y2, float op, float lineSize) {
        Minecraft mc = Minecraft.getInstance();
        long count = mc.level.getGameTime();
//        float bob = Mth.sin(((float)count + (float)x2) / 10.0F) * 0.15F + 0.15F;
//        float bob2 = Mth.sin(((float)count + (float)x + (float)y2) / 11.0F) * 0.15F + 0.15F;
//        float bob3 = Mth.sin(((float)count + (float)y) / 12.0F) * 0.15F + 0.15F;
        GlStateManager._depthMask(false);
        GlStateManager._disableCull();
        RenderSystem.setShader(GameRenderer::getRendertypeLinesShader);
        Tesselator tesselator = RenderSystem.renderThreadTesselator();
        BufferBuilder buffer = tesselator.begin(VertexFormat.Mode.LINES, DefaultVertexFormat.POSITION_COLOR_NORMAL);
        RenderSystem.lineWidth(lineSize);
        buffer.addVertex((float)x, (float)y, 0.0F).setColor(1, 1, 1, op).setNormal(1.0F, 1.0F, 1.0F);
        buffer.addVertex((float)x2, (float)y2, 0.0F).setColor(1, 1, 1, op).setNormal(1.0F, 1.0F, 1.0F);
        BufferUploader.drawWithShader(buffer.buildOrThrow());
        RenderSystem.lineWidth(1.0F);
        GlStateManager._enableCull();
        GlStateManager._depthMask(true);
    }
}
