package net.sixik.researchtree.client;

import dev.ftb.mods.ftblibrary.ui.*;
import dev.ftb.mods.ftblibrary.ui.input.MouseButton;
import dev.ftb.mods.ftblibrary.util.TooltipList;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.sixik.researchtree.utils.RenderIcon;

import java.util.List;

public class ResearchInfoToolPanel extends Panel {

    protected ResearchInfoPanel infoPanel;

    protected Button closeButton;
    protected Button focusButton;
    protected Button showParensButton;


    public ResearchInfoToolPanel(Panel panel, ResearchInfoPanel infoPanel) {
        super(panel);
        this.infoPanel = infoPanel;
    }

    @Override
    public boolean checkMouseOver(int mouseX, int mouseY) {
        return ResearchScreen.Instance.currentModalPanel == null && super.checkMouseOver(mouseX, mouseY);
    }

    @Override
    public void addWidgets() {

        add(this.focusButton = new SimpleTextButton(this, Component.empty(), RenderIcon.CONTRACT) {

            @Override
            public void addMouseOverText(TooltipList list) {
                list.add(Component.translatable("research.ui.info.tool.focus_button"));
            }

            @Override
            public void onClicked(MouseButton mouseButton) {
                ResearchScreen.Instance.moveCameraToResearch(infoPanel.researchWidget);
                ResearchScreen.Instance.addSelected(infoPanel.researchWidget);
            }

            @Override
            public void drawBackground(GuiGraphics graphics, Theme theme, int x, int y, int w, int h) {
                ResearchScreen.DEFAULT_BACKGROUND.drawRoundFill(graphics,x,y,w,h, 6);
            }

            @Override
            public boolean checkMouseOver(int mouseX, int mouseY) {
                return infoPanel.researchWidget != null && super.checkMouseOver(mouseX, mouseY);
            }
        });

        add(this.showParensButton = new SimpleTextButton(this, Component.empty(), RenderIcon.FAMILY_TREE) {

            @Override
            public void addMouseOverText(TooltipList list) {
                list.add(Component.translatable("research.ui.info.tool.show_parent_button"));
            }

            @Override
            public boolean shouldDraw() {
                return infoPanel.researchWidget != null && infoPanel.researchWidget.hasParents();
            }

            @Override
            public boolean shouldAddMouseOverText() {
                return infoPanel.researchWidget != null && infoPanel.researchWidget.hasParents();
            }

            @Override
            public void onClicked(MouseButton mouseButton) {
                ResearchScreen.Instance.addSelected(infoPanel.researchWidget.getParentWidgets().toArray(new ResearchWidget[0]));
            }

            @Override
            public void drawBackground(GuiGraphics graphics, Theme theme, int x, int y, int w, int h) {
                ResearchScreen.DEFAULT_BACKGROUND.drawRoundFill(graphics,x,y,w,h, 6);
            }
        });

        add(this.closeButton = new SimpleTextButton(this, Component.empty(), RenderIcon.CANCEL) {

            @Override
            public void addMouseOverText(TooltipList list) {
                list.add(Component.translatable("research.ui.info.close_button"));
            }

            @Override
            public void onClicked(MouseButton mouseButton) {
                ResearchScreen.setResearch(null);
            }

            @Override
            public boolean checkMouseOver(int mouseX, int mouseY) {
                return infoPanel.researchWidget != null && super.checkMouseOver(mouseX, mouseY);
            }

            @Override
            public void drawBackground(GuiGraphics graphics, Theme theme, int x, int y, int w, int h) {
                ResearchScreen.DEFAULT_BACKGROUND.drawRoundFill(graphics,x,y,w,h, 6);
            }
        });


    }

    @Override
    public void alignWidgets() {
        List<Widget> list = this.getWidgets().stream().filter(Widget::shouldDraw).toList();

        this.setHeight(16 * list.size());
        int sY = 0;
        for (Widget widget : list) {
            widget.setSize(14,14);
            widget.setY(sY);
            sY += 16;
        }}

    @Override
    public boolean shouldDraw() {
        return infoPanel.shouldDraw();
    }
}
