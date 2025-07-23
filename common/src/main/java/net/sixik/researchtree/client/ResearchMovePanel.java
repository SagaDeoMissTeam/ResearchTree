package net.sixik.researchtree.client;

import dev.ftb.mods.ftblibrary.ui.GuiHelper;
import dev.ftb.mods.ftblibrary.ui.Panel;
import dev.ftb.mods.ftblibrary.ui.Theme;
import dev.ftb.mods.ftblibrary.ui.Widget;
import dev.ftb.mods.ftblibrary.ui.input.MouseButton;
import net.minecraft.client.gui.GuiGraphics;
import net.sixik.researchtree.client.debug.ClientDebugUtils;
import net.sixik.researchtree.client.render.SelectRender;
import net.sixik.researchtree.mixin.PanelAccessor;
import net.sixik.researchtree.research.BaseResearch;
import net.sixik.researchtree.research.DebugResearchData;
import net.sixik.researchtree.research.ResearchData;
import net.sixik.researchtree.research.manager.ClientResearchManager;
import net.sixik.researchtree.utils.ResearchUtils;
import net.sixik.sdmuilibrary.client.utils.math.Vector2;
import net.sixik.sdmuilibrary.client.utils.renders.GLRenderHelper;

import java.util.*;

public class ResearchMovePanel extends Panel {

    public static final int START_POS_X = 0;
    public static final int START_POS_Y = 0;

    protected int minX, minY, maxX, maxY;
    protected boolean isDragging = false;
    protected int dragStartX, dragStartY;
    protected int widgetOffsetX, widgetOffsetY;
    protected boolean boundsDirty = true;

    protected List<ResearchWidget> researchWidgets = new ArrayList<>();
    protected List<ConnectionRenderData> renderData = new ArrayList<>();
    protected ResearchData researchData;

    public ResearchMovePanel(Panel panel) {
        super(panel);
        this.researchData = ((ClientResearchManager) ResearchUtils.getManagerCast(true))
                .getResearchData().get();
    }


    @Override
    public void addWidgets() {
        for (BaseResearch research : researchData.getResearchList()) {
            ResearchWidget widget = new ResearchWidget(this, research);
            widget.setPosAndSize(0,0, 100, 20);
            add(widget);
        }

        boundsDirty = true;
        researchWidgets = getWidgets().stream().filter(w -> w instanceof ResearchWidget).map(v -> (ResearchWidget)v).toList();
//        renderData.add(new ConnectionRenderData(researchWidgets.get(0)));
    }

    @Override
    public void add(Widget widget) {
        super.add(widget);
        boundsDirty = true;
    }

    protected void updateBounds() {
        minX = Integer.MAX_VALUE;
        minY = Integer.MAX_VALUE;
        maxX = Integer.MIN_VALUE;
        maxY = Integer.MIN_VALUE;

        if (researchWidgets.isEmpty()) {
            minX = minY = 0;
            maxX = getWidth();
            maxY = getHeight();
            boundsDirty = false;
            return;
        }

        for (Widget widget : researchWidgets) {
            // Используем исходные позиции виджетов (без widgetOffsetX/Y)
            minX = Math.min(minX, widget.posX);
            minY = Math.min(minY, widget.posY);
            maxX = Math.max(maxX, widget.posX + widget.getWidth());
            maxY = Math.max(maxY, widget.posY + widget.getHeight());
        }
        boundsDirty = false;
    }

    @Override
    public void alignWidgets() {
        boundsDirty = true;
        researchWidgets = getWidgets().stream().filter(w -> w instanceof ResearchWidget).map(v -> (ResearchWidget)v).toList();
        calculatePosition(researchWidgets);
    }

    @Override
    public boolean mousePressed(MouseButton button) {
        if (button == MouseButton.LEFT && ResearchScreen.isMouseOverWidget(this)) {
            isDragging = true;
            dragStartX = getMouseX();
            dragStartY = getMouseY();
            super.mousePressed(button);
            return true;
        }
        return super.mousePressed(button);
    }



    @Override
    public void mouseReleased(MouseButton button) {
        if (button == MouseButton.LEFT) {
            isDragging = false;
        }
        super.mouseReleased(button);
    }

    @Override
    public boolean mouseDragged(int button, double dragX, double dragY) {
        if (button == MouseButton.LEFT.id && ResearchScreen.isMouseOverWidget(this)) {
            int deltaX = getMouseX() - dragStartX;
            int deltaY = getMouseY() - dragStartY;

            int newOffsetX = widgetOffsetX + deltaX;
            int newOffsetY = widgetOffsetY + deltaY;

            // Проверяем границы
            if (getParent() != null) {
                int parentWidth = getParent().getWidth();
                int parentHeight = getParent().getHeight();

                newOffsetX = Math.min(parentWidth - minX, Math.max(0 - maxX, newOffsetX));
                newOffsetY = Math.min(parentHeight - minY, Math.max(0 - maxY, newOffsetY));
            }

            widgetOffsetX = newOffsetX;
            widgetOffsetY = newOffsetY;

            dragStartX = getMouseX();
            dragStartY = getMouseY();

            if (boundsDirty) {
                updateBounds();
            }

            return true;
        }
        return super.mouseDragged(button, dragX, dragY);
    }

    @Override
    public void tick() {
        super.tick();
        if (boundsDirty) {
            updateBounds();
        }
    }

    @Override
    public void setOffset(boolean flag) {
        PanelAccessor accessor = ClientUtils.accessor(this);
        if (flag) {
            accessor.setOffsetX(widgetOffsetX);
            accessor.setOffsetY(widgetOffsetY);
        } else {
            accessor.setOffsetX(0);
            accessor.setOffsetY(0);
        }
    }

    @Override
    public void draw(GuiGraphics graphics, Theme theme, int x, int y, int w, int h) {
        super.draw(graphics, theme, x, y, w, h);
        GLRenderHelper.enableScissorFor(graphics, new Vector2(x, y), new Vector2(w,h), () -> {
            drawSelected(graphics);
        });


        if(Theme.renderDebugBoxes) {
            int pY = y + h - 10;
            int pX = x + w;

            theme.drawString(graphics, "Count: " + widgets.size(), pX- 100, pY);
            theme.drawString(graphics, "X: " + widgetOffsetX, pX - 150, pY);
            theme.drawString(graphics, "Y: " + widgetOffsetY, pX - 200, pY);
            theme.drawString(graphics, "MOver: " + (ResearchScreen.getMouseOverWidget().isEmpty() ? "NUL" : ResearchScreen.getMouseOverWidget().get().getClass().getSimpleName()), x + 20, pY);
        }
    }

    @Override
    public void drawBackground(GuiGraphics graphics, Theme theme, int x, int y, int w, int h) {
        GLRenderHelper.enableScissorFor(graphics, new Vector2(x, y), new Vector2(w,h), () -> {
            drawConnection(graphics, theme, x, y, w, h);
        });
    }

    public void drawConnection(GuiGraphics graphics, Theme theme, int x, int y, int w, int h) {
        for (ConnectionRenderData renderDatum : renderData) {
            renderDatum.draw(graphics, widgetOffsetX, widgetOffsetY, w,h);
        }
    }

    public void focusOnResearch(ResearchWidget research) {
        focusOnResearch(research.research);
    }

    public void focusOnResearch(BaseResearch research) {
        int index = researchWidgets.indexOf(
                researchWidgets.stream()
                        .filter(w -> w.research == research)
                        .findFirst()
                        .orElse(null)
        );
        if (index >= 0) {
            focusOnResearch(index);
        } else {
            System.err.println("Research not found: " + research.getId());
        }
    }

    public void drawSelected(GuiGraphics graphics) {
        Iterator<SelectRender> iter = ResearchScreen.Instance.selectRenders.iterator();
        while (iter.hasNext()) {
            SelectRender render = iter.next();
            if(render.isEnd())  iter.remove();
            else {
                render.draw(graphics, widgetOffsetX, widgetOffsetY);
            }
        }
    }

    /**
     * Перемещает камеру так, чтобы исследование с индексом index оказалось в центре области просмотра.
     */
    public void focusOnResearch(int index) {
        if (index < 0 || index >= researchWidgets.size()) {
            System.err.println("Invalid research index: " + index);
            return;
        }

        if (getParent() == null) {
            System.err.println("Parent panel is null");
            return;
        }

        ResearchWidget targetWidget = researchWidgets.get(index);
        int parentWidth = getParent().getWidth();
        int parentHeight = getParent().getHeight();

        // Вычисляем центр виджета
        int widgetCenterX = targetWidget.posX + targetWidget.getWidth() / 2;
        int widgetCenterY = targetWidget.posY + targetWidget.getHeight() / 2;

        // Вычисляем смещение, чтобы центр виджета совпал с центром области просмотра
        widgetOffsetX = (parentWidth / 2) - widgetCenterX;
        widgetOffsetY = (parentHeight / 2) - widgetCenterY;

        // Применяем ограничения, чтобы содержимое не выходило за границы
        widgetOffsetX = Math.min(0 - minX, Math.max(parentWidth - maxX, widgetOffsetX));
        widgetOffsetY = Math.min(parentHeight - minY, Math.max(0 - maxY, widgetOffsetY));

        boundsDirty = true; // Требуется обновление границ
    }

    /**
     * Располагает виджеты на основе их зависимостей.
     * - Исследования без родителей — в столбце 0 (START_POS_X).
     * - Исследования с родителями — в столбце START_POS_X + SPACE_X * уровень, где уровень — максимальная глубина родителей + 1.
     * - Исследования с shouldRenderConnection = false — в столбце, следующем за максимальным столбцом родителей.
     */
    protected void calculatePosition(List<ResearchWidget> widgets) {
        if (widgets.isEmpty()) return;

        // Карта для хранения уровня (глубины) каждого исследования
        Map<ResearchWidget, Integer> levels = new HashMap<>();
        // Карта для хранения максимальной y-координаты для каждого уровня
        Map<Integer, Integer> levelMaxY = new HashMap<>();
        // Карта для хранения виджетов с shouldRenderConnection = false
        Map<ResearchWidget, Integer> noConnectionLevels = new HashMap<>();

        // Шаг 1: Вычисляем уровень для каждого исследования
        for (ResearchWidget widget : widgets) {
            calculateLevel(widget, levels, new HashMap<>());
        }

        // Шаг 2: Корректируем уровни для исследований с shouldRenderConnection = false
        for (ResearchWidget widget : widgets) {
            if (!widget.research.shouldRenderConnection) {
                int maxParentLevel = -1;
                for (BaseResearch parent : widget.research.getParentResearch()) {
                    ResearchWidget parentWidget = widgets.stream()
                            .filter(w -> w.research == parent)
                            .findFirst()
                            .orElse(null);
                    if (parentWidget != null) {
                        maxParentLevel = Math.max(maxParentLevel, levels.getOrDefault(parentWidget, 0));
                    }
                }
                // Устанавливаем уровень как максимальный уровень родителя + 1
                noConnectionLevels.put(widget, maxParentLevel + 1);
                levels.put(widget, maxParentLevel + 1);
            }
        }

        // Шаг 3: Располагаем виджеты по уровням
        for (ResearchWidget widget : widgets) {
            int level = levels.getOrDefault(widget, 0);
            int x = START_POS_X + ResearchScreen.SPACE_X * level;
            int y = levelMaxY.getOrDefault(level, START_POS_Y);

            widget.setPos(x, y);
            // Обновляем максимальную y-координату для уровня
            levelMaxY.put(level, y + widget.getHeight() + ResearchScreen.SPACE_Y);
        }

        // Шаг 4: Обновляем связи для renderData
        renderData.clear();
        for (ResearchWidget widget : widgets) {
            if (widget.research.shouldRenderConnection) {
                ConnectionRenderData connection = new ConnectionRenderData(widget);
                for (BaseResearch parent : widget.research.getParentResearch()) {
                    widgets.stream()
                            .filter(w -> w.research == parent)
                            .findFirst()
                            .ifPresent(connection::addConnection);
                }
                renderData.add(connection);
            }
        }

        boundsDirty = true;
    }

    /**
     * Рекурсивно вычисляет уровень (глубину) исследования на основе его родителей.
     */
    private int calculateLevel(ResearchWidget widget, Map<ResearchWidget, Integer> levels, Map<ResearchWidget, Integer> visited) {
        if (levels.containsKey(widget)) {
            return levels.get(widget);
        }

        // Проверяем на циклические зависимости
        if (visited.containsKey(widget)) {
            System.err.println("Обнаружен цикл в зависимостях для исследования: " + widget.research.getId());
            return visited.get(widget);
        }

        visited.put(widget, 0); // Начальный уровень для предотвращения циклов
        int maxParentLevel = -1;

        // Находим максимальный уровень среди родителей
        for (BaseResearch parent : widget.research.getParentResearch()) {
            ResearchWidget parentWidget = researchWidgets.stream()
                    .filter(w -> w.research == parent)
                    .findFirst()
                    .orElse(null);
            if (parentWidget != null) {
                int parentLevel = calculateLevel(parentWidget, levels, visited);
                maxParentLevel = Math.max(maxParentLevel, parentLevel);
            }
        }

        // Уровень = максимальный уровень родителя + 1, или 0, если нет родителей
        int level = widget.research.hasParents() ? maxParentLevel + 1 : 0;
        levels.put(widget, level);
        visited.put(widget, level);
        return level;
    }

}
