package net.sixik.researchtree.client;

import dev.ftb.mods.ftblibrary.icon.Color4I;
import dev.ftb.mods.ftblibrary.icon.Icons;
import dev.ftb.mods.ftblibrary.ui.*;
import dev.ftb.mods.ftblibrary.ui.input.MouseButton;
import dev.ftb.mods.ftblibrary.util.TooltipList;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.sixik.researchtree.client.widgets.BaseModalPanel;
import net.sixik.researchtree.utils.ResearchUtils;
import net.sixik.researchtree.utils.TextUtils;
import net.sixik.sdmuilibrary.client.utils.DrawDirection;
import net.sixik.sdmuilibrary.client.utils.renders.ShapesRenderHelper;

public class ResearchInfoModalPanel extends BaseModalPanel {

    public ResearchInfoModalPanel(Panel panel) {
        super(panel);
        setPos(-1, -1);
        setOnlyRenderWidgetsInside(true);
        setOnlyInteractWithWidgetsInside(true);
    }

    @Override
    public void addWidgets() {

    }

    @Override
    public void alignWidgets() {
        this.setX((getGui().getScreen().getGuiScaledWidth() - this.width) / 2);
        this.setY((getGui().getScreen().getGuiScaledHeight() - this.height) / 2);
    }

    @Override
    public void drawBackground(GuiGraphics graphics, Theme theme, int x, int y, int w, int h) {
        UIColors.MODAL_BACKGROUND.drawRoundFill(graphics,x,y,w,h, 6);
    }

    public static class CancelResearch extends ResearchInfoModalPanel {

        protected Button acceptButton;
        protected Button cancelButton;
        protected TextField titleField;
        protected TextField descriptionField;
        protected BlankPanel descriptionPanel;

        public CancelResearch(Panel panel) {
            super(panel);
        }

        @Override
        public void addWidgets() {
            add(titleField = new TextField(this));
            titleField.setText(Component.translatable("research.ui.info.modal.warning.label"));
            titleField.setScale(1.5f);
            titleField.setColor(UIColors.WARNING_FTB);

            add(descriptionPanel = new BlankPanel(this) {

                @Override
                public void drawBackground(GuiGraphics graphics, Theme theme, int x, int y, int w, int h) {
                    UIColors.MODAL_OVERLAY.drawRoundFill(graphics,x,y,w,h, 6);
                }
            });

            descriptionPanel.add(descriptionField = new TextField(descriptionPanel));
            updateText();

            add(acceptButton = new SimpleTextButton(this, Component.translatable("research.ui.info.accept_button"), Icons.ACCEPT) {
                @Override
                public void onClicked(MouseButton mouseButton) {

                }

                @Override
                public void addMouseOverText(TooltipList list) {}

                @Override
                public void drawBackground(GuiGraphics graphics, Theme theme, int x, int y, int w, int h) {
                    ShapesRenderHelper.drawRoundedRect(graphics,x,y,w,h, 6, UIColors.MODAL_OVERLAY, DrawDirection.DOWN);
                }
            });
            add(cancelButton = new SimpleTextButton(this, Component.translatable("research.ui.info.cancel_button"), Icons.CANCEL) {
                @Override
                public void onClicked(MouseButton mouseButton) {
                    ResearchScreen.Instance.removeAnyModalPanel();
                }

                @Override
                public void drawBackground(GuiGraphics graphics, Theme theme, int x, int y, int w, int h) {
                    ShapesRenderHelper.drawRoundedRect(graphics,x,y,w,h, 6, UIColors.MODAL_OVERLAY, DrawDirection.DOWN);
                }

                @Override
                public void addMouseOverText(TooltipList list) {}
            });
        }

        protected void updateText() {
            double percent = ResearchUtils.getRefundPercent(Minecraft.getInstance().player, ResearchScreen.getResearch().research);

            Component component = Component.translatable("research.ui.info.cancel_research_title");
            if(percent < 100)
                component = component.copy().append(Component.translatable("research.ui.info.cancel_research_title.addition",  percent));
            descriptionField.setText(component);
        }

        @Override
        public void alignWidgets() {
            this.setWidth(Math.max(this.titleField.width, Math.max(this.cancelButton.width + this.acceptButton.width, this.width - this.descriptionField.width / 6)));

            this.descriptionPanel.setY(this.titleField.height + 8);
            this.descriptionPanel.setX(2);
            this.descriptionPanel.setWidth(this.width - 4);
            this.descriptionPanel.setHeight(descriptionField.height * 4);
            this.setHeight(this.cancelButton.height + this.descriptionPanel.height + titleField.height * 2 + 4);

            this.descriptionField.setMaxWidth(this.width);
            updateText();
            descriptionField.addFlags(Theme.CENTERED);

            descriptionField.setX((this.descriptionPanel.width - descriptionField.width) / 2);
            descriptionField.setY((this.descriptionPanel.height - descriptionField.height) / 2);

            this.cancelButton.setX(0);
            this.cancelButton.setY(this.height - cancelButton.height);
            this.cancelButton.setWidth(this.cancelButton.width - 1);
            this.acceptButton.setX(this.width - acceptButton.width + 1);
            this.acceptButton.setY(this.height - acceptButton.height);
            this.acceptButton.setWidth(this.acceptButton.width - 1);


            super.alignWidgets();

            this.titleField.setY(2);
            this.titleField.setX((int) ((this.width - this.titleField.width * this.titleField.scale) / 2));
        }
    }
}
