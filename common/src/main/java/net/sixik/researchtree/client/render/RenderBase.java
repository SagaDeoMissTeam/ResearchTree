package net.sixik.researchtree.client.render;

import dev.ftb.mods.ftblibrary.util.TooltipList;
import net.minecraft.client.gui.GuiGraphics;
import net.sixik.researchtree.client.ResearchWidget;
import net.sixik.researchtree.utils.ResearchRenderUtils;
import net.sixik.sdmuilibrary.client.utils.RenderHelper;
import net.sixik.sdmuilibrary.client.utils.math.Vector2;
import net.sixik.sdmuilibrary.client.utils.misc.RGB;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public abstract class RenderBase<T> {
    protected final int x;
    protected final int y;
    protected final int width;
    protected final int height;
    protected final int gapX;
    protected final int gapY;
    protected final boolean centerVertically;
    public List<DrawData<T>> renderData = new ArrayList<>();
    protected Vector2 size = new Vector2(0, 0);
    protected ResearchWidget researchWidget;

    protected static final int ELEMENT_SIZE = 16;

    public RenderBase(ResearchWidget researchWidget, int x, int y, int width, int height, int gapX, int gapY, boolean centerVertically) {
        this.researchWidget = researchWidget;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.gapX = gapX;
        this.gapY = gapY;
        this.centerVertically = centerVertically;
        calculatePosition();
    }

    public Vector2 getSize() {
        return size;
    }

    protected void calculatePosition() {
        renderData.clear();
        List<T> elements = getElements();
        if (elements.isEmpty()) {
            size = new Vector2(0, 0);
            return;
        }

        final int maxPerRow = Math.max(1, width / (ELEMENT_SIZE + gapX));
        final int totalElementHeight = ELEMENT_SIZE + gapY;
        final int totalRows = (int) Math.ceil((double) elements.size() / maxPerRow);

        // Рассчитываем размеры области
        size = new Vector2(
                Math.min(elements.size(), maxPerRow) * (ELEMENT_SIZE + gapX) - gapX,
                totalRows * totalElementHeight - gapY
        );

        int startY = y;
        if (centerVertically && size.y < height) {
            startY = y + (height - size.y) / 2;
        }

        int currentX = x;
        int currentY = startY;
        int elementsInRow = 0;

        for (int i = 0; i < elements.size(); i++) {
            if (currentY + ELEMENT_SIZE > y + height) {
                break;
            }

            if (elementsInRow >= maxPerRow) {
                currentY += totalElementHeight;
                currentX = x;
                elementsInRow = 0;
            }

            int remainingElements = Math.min(maxPerRow, elements.size() - i);
            if (elementsInRow == 0 && remainingElements > 0) {
                int rowWidth = remainingElements * ELEMENT_SIZE + (remainingElements - 1) * gapX;
                currentX = x + (width - rowWidth) / 2;
            }

            T element = elements.get(i);
            renderData.add(createDrawData(element, currentX, currentY, ELEMENT_SIZE, ELEMENT_SIZE));

            currentX += ELEMENT_SIZE + gapX;
            elementsInRow++;
        }
    }

    public void draw(GuiGraphics graphics, int additionX, int additionY, int offsetX, int offsetY) {
        for (DrawData<T> data : renderData) {
            data.draw(graphics, additionX + offsetX, additionY + offsetY);
        }
    }

    public void drawTooltip(GuiGraphics graphics, int mouseX, int mouseY, int posX, int posY, int offsetX, int offsetY, RenderTooltip renderTooltip) {
        for (DrawData<T> data : renderData) {
            int elementX = data.x + posX + offsetX;
            int elementY = data.y + posY + offsetY;
            if (ResearchRenderUtils.isMouseOver(mouseX, mouseY, elementX, elementY, data.w, data.h)) {
                RenderHelper.drawHollowRect(graphics, elementX, elementY, data.w, data.h, RGB.DEFAULT, false);
                data.drawTooltip(graphics, mouseX, mouseY, renderTooltip, new TooltipList());
            }
        }
    }


    protected abstract List<T> getElements();
    protected abstract DrawData<T> createDrawData(T element, int x, int y, int w, int h);

    protected final class DrawData<T> {
        private final T element;
        private final int x;
        private final int y;
        private final int w;
        private final int h;

        DrawData(T element, int x, int y, int w, int h) {
            this.element = element;
            this.x = x;
            this.y = y;
            this.w = w;
            this.h = h;
        }

        public void draw(GuiGraphics graphics, int offsetX, int offsetY) {
            RenderBase.this.drawElement(graphics, element, x + offsetX, y + offsetY, w, h);
        }

        public void drawTooltip(GuiGraphics graphics, int x, int y, RenderTooltip renderTooltip, TooltipList list) {
            RenderBase.this.drawElementTooltip(graphics, element, x,y, renderTooltip, list);
        }

        public T element() {
            return element;
        }

        public int x() {
            return x;
        }

        public int y() {
            return y;
        }

        public int w() {
            return w;
        }

        public int h() {
            return h;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this) return true;
            if (obj == null || obj.getClass() != this.getClass()) return false;
            var that = (DrawData) obj;
            return Objects.equals(this.element, that.element) &&
                    this.x == that.x &&
                    this.y == that.y &&
                    this.w == that.w &&
                    this.h == that.h;
        }

        @Override
        public int hashCode() {
            return Objects.hash(element, x, y, w, h);
        }

        @Override
        public String toString() {
            return "DrawData[" +
                    "element=" + element + ", " +
                    "x=" + x + ", " +
                    "y=" + y + ", " +
                    "w=" + w + ", " +
                    "h=" + h + ']';
        }

        }

    protected void drawElement(GuiGraphics graphics, Object element, int x, int y, int w, int h) {
        // Реализация в подклассах
    }

    protected void drawElementTooltip(GuiGraphics graphics, Object element, int x, int y, RenderTooltip tooltip, TooltipList list) {
        // Реализация в подклассах
    }
}
