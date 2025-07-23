package net.sixik.researchtree.client;

import dev.ftb.mods.ftblibrary.ui.BaseScreen;
import dev.ftb.mods.ftblibrary.ui.Theme;
import dev.ftb.mods.ftblibrary.ui.Widget;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.sixik.researchtree.DebugConstants;
import net.sixik.researchtree.client.render.SelectRender;
import net.sixik.researchtree.client.widgets.BaseModalPanel;
import net.sixik.sdmuilibrary.client.utils.math.Vector2;
import net.sixik.sdmuilibrary.client.utils.misc.RGBA;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class ResearchScreen extends BaseScreen {

    public static ResearchScreen Instance;

    public static final RGBA DEFAULT_WIDGET_COLOR = RGBA.create(0,0,0,255 / 2);
    public static final RGBA DEFAULT_BACKGROUND = RGBA.create(0,0,0,255 / 3);

    public static final int UP_PANEL_HEIGHT = Minecraft.getInstance().font.lineHeight + 3;
    public static final int DOWN_PANEL_HEIGHT = UP_PANEL_HEIGHT;
    public static final int SCROLL_BAR_SIZE = 2;

    public static final int SPACE_X = 114;
    public static final int SPACE_Y = 10;

    protected ResearchMovePanel movePanel;
    protected ResearchInfoPanel infoPanel;
    protected ResearchInfoToolPanel infoToolPanel;
    protected ResearchToolPanel toolPanel;

    protected List<SelectRender> selectRenders = new ArrayList<>();

    protected @Nullable BaseModalPanel currentModalPanel;


    @Override
    public boolean onInit() {
        setSize(getScreen().getGuiScaledWidth(),  getScreen().getGuiScaledHeight() - getScreen().getGuiScaledHeight() / 8);
        Instance = this;
        return super.onInit();
    }

    @Override
    public void onClosed() {
        Instance = null;
        super.onClosed();
    }

    @Override
    public void addWidgets() {
        add(movePanel = new ResearchMovePanel(this));
        add(infoPanel = new ResearchInfoPanel(this));
        add(infoToolPanel = new ResearchInfoToolPanel(this, infoPanel));
        add(toolPanel = new ResearchToolPanel(this));
    }

    public void setModalPanel(@Nullable BaseModalPanel currentModalPanel) {
        if(this.currentModalPanel != null && this.currentModalPanel != currentModalPanel) {
            removeModalPanel(this.currentModalPanel);
        }

        this.currentModalPanel = currentModalPanel;

        if(this.currentModalPanel != null) {
            add(this.currentModalPanel);
            this.currentModalPanel.addWidgets();
            this.currentModalPanel.alignWidgets();
        }
    }

    public boolean removeModalPanel(@NotNull BaseModalPanel currentModalPanel) {
        this.currentModalPanel = null;
        return getWidgets().removeIf(s -> Objects.equals(s, currentModalPanel));
    }

    public boolean removeAnyModalPanel() {
        boolean value = getWidgets().removeIf(s -> s instanceof BaseModalPanel);
        currentModalPanel = null;
        return value;
    }

    public void addSelected(ResearchWidget widget) {
        selectRenders.add(new SelectRender(5_000, widget));
    }

    public void addSelected(ResearchWidget... widget) {
        selectRenders.add(new SelectRender(5_000, widget));
    }

    public static void setResearch(@Nullable ResearchWidget widget) {
        Instance.setResearchInternal(widget);
    }

    protected void setResearchInternal(@Nullable ResearchWidget widget) {
        infoPanel.setResearch(widget);

        infoToolPanel.setPos(infoPanel.width, infoPanel.posY);
        infoToolPanel.setWidth(16);
        infoToolPanel.alignWidgets();
    }

    public static ResearchWidget getResearch() {
        return Instance.infoPanel.researchWidget;
    }

    public static Optional<Widget> getMouseOverWidget() {

        if(Instance.currentModalPanel != null) {
            return Optional.of(Instance.currentModalPanel);
        }

        List<Widget> list = Instance.getWidgets().stream().filter(s -> s.isMouseOver() && s.shouldDraw()).toList();
        if(list.isEmpty()) return Optional.empty();
        return Optional.ofNullable(list.getLast());
    }

    public static boolean isMouseOverWidget() {
        return getMouseOverWidget().isPresent();
    }

    public static boolean isEditMode() {
        return DebugConstants.hasResearch;
    }

    public static boolean isMouseOverWidget(Widget widget) {
        var m = getMouseOverWidget();
        return m.isPresent() && m.get() == widget;
    }

    public static boolean isMouseOverAdvanced(Widget widget) {
       return Instance.isMouseOver(widget.posX, widget.posX, widget.width, widget.height);
    }

    @Override
    public void alignWidgets() {
        movePanel.setSize(this.width, this.height - UP_PANEL_HEIGHT - DOWN_PANEL_HEIGHT);
        movePanel.setY(UP_PANEL_HEIGHT);
        movePanel.alignWidgets();

        infoPanel.setPos(0, UP_PANEL_HEIGHT);
        infoPanel.setSize(100, movePanel.height);
        infoPanel.alignWidgets();


        toolPanel.setSize(this.width / 6, UP_PANEL_HEIGHT);
        toolPanel.setPos(this.width - toolPanel.width, 0);
        toolPanel.alignWidgets();

    }


    public List<ResearchWidget> getResearchWidgets() {
        return movePanel.getWidgets().stream()
                .filter(w -> w instanceof ResearchWidget)
                .map(v -> (ResearchWidget)v)
                .toList();
    }

    @Override
    public void drawBackground(GuiGraphics graphics, Theme theme, int x, int y, int w, int h) {
        DEFAULT_BACKGROUND.draw(graphics, x,y,w,h);
    }

    public Optional<ResearchWidget> getSelectedResearch() {
        return Optional.ofNullable(infoPanel.researchWidget);
    }

    public void moveCameraToResearch(@NotNull ResearchWidget widget) {
        movePanel.focusOnResearch(widget);
    }

    public Vector2 getModalPanelSize() {
        return new Vector2(getScreen().getGuiScaledWidth() / 4, getScreen().getGuiScaledHeight() / 6);
    }

    //    @Override
//    public boolean mousePressed(MouseButton button) {
//
//        if(super.mousePressed(button)) {
//            if (this.currentModalPanel != null && !isMouseOverWidget(this.currentModalPanel)) {
//                removeAnyModalPanel();
//            }
//            return true;
//        }
//
//        return false;
//    }




    @Override
    public void drawForeground(GuiGraphics graphics, Theme theme, int x, int y, int w, int h) {
        if(this.currentModalPanel != null) {
            graphics.pose().pushPose();
            graphics.pose().translate(0,0,400);
            RGBA.create(0,0,0,255/2).draw(graphics,x,y,w,h);
            graphics.pose().popPose();
        }
    }
}
