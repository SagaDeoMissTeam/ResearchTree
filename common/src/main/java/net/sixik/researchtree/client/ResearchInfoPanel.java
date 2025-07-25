package net.sixik.researchtree.client;

import dev.ftb.mods.ftblibrary.icon.Icon;
import dev.ftb.mods.ftblibrary.ui.*;
import dev.ftb.mods.ftblibrary.ui.input.MouseButton;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.sixik.researchtree.client.render.BaseRenderReward;
import net.sixik.researchtree.client.render.RenderRequirements;
import net.sixik.researchtree.client.render.RenderRewards;
import net.sixik.researchtree.client.widgets.ProgressBarWidget;
import net.sixik.researchtree.research.ResearchStage;
import net.sixik.researchtree.utils.RenderIcon;
import net.sixik.researchtree.utils.ResearchRenderUtils;
import net.sixik.researchtree.utils.ResearchUtils;
import net.sixik.sdmuilibrary.client.utils.DrawDirection;
import net.sixik.sdmuilibrary.client.utils.misc.RGBA;
import net.sixik.sdmuilibrary.client.utils.renders.ShapesRenderHelper;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class ResearchInfoPanel extends Panel {

    public static final RGBA LINE_COLOR = RGBA.create(128,128,128, 255);
    public static final int WIDGET_SPACE_Y = 2;
    public static final int ICON_SIZE = 16;

    protected Button startResearchButton;
    protected ResearchWidget researchWidget;

    protected ProgressBarWidget researchProgressBar;
    protected Button stopResearchButton;

    protected TextField researchedLabel;
    protected TextField titleLabel;
    protected TextField subtitleLabel;
    protected DescriptionPanel descriptionPanel;
    protected PanelScrollBar descriptionScrollPanel;

    protected RewardPanel rewardPanel;
    protected PanelScrollBar rewardScrollPanel;

    protected ResearchStage researchStage = ResearchStage.LOCKED;

    private List<TextField> descriptionFields = new ArrayList<>();

    public ResearchInfoPanel(Panel panel) {
        super(panel);
        setOnlyRenderWidgetsInside(false);
    }

    public void updateProgressBar(double value) {
        researchProgressBar.setValue(value);
    }

    @Override
    public boolean checkMouseOver(int mouseX, int mouseY) {
        return ResearchScreen.Instance.currentModalPanel == null && super.checkMouseOver(mouseX, mouseY);
    }

    @Override
    public boolean mouseScrolled(double scroll) {
        return ResearchScreen.Instance.currentModalPanel == null && super.mouseScrolled(scroll);
    }

    @Override
    public void tick() {
        if(researchWidget != null && !researchWidget.researched && researchWidget.canResearch && Minecraft.getInstance().gui.getGuiTicks() % 10 == 0) {
            updateProgressBar(researchWidget.progressBar.getValue());
        }
    }

    public void setResearch(@Nullable ResearchWidget widget) {
        this.researchWidget = widget;
        update();
    }

    public void updateStage(ResearchStage stage) {
        this.researchStage = stage;
    }

    public void updateStage() {
        if(this.researchWidget == null) return;

        this.researchStage = ResearchUtils.getResearchStage(Minecraft.getInstance().player, this.researchWidget.research, true);
    }

    public void update() {
        if(this.researchWidget != null) {
            this.titleLabel.setText(this.researchWidget.research.getTranslate());

            if(this.researchWidget.research.hasSubtitle() || ResearchScreen.isEditMode())
                this.subtitleLabel.setText(this.researchWidget.research.getSubtitleTranslate());

            updateStage();
            this.researchProgressBar.setValue(ResearchUtils.getPercentResearch(Minecraft.getInstance().player, this.researchWidget.research, true));
        } else {
            this.titleLabel.setText(Component.empty());
            this.subtitleLabel.setText(Component.empty());
        }

        calculateProperties();
        addDescription();

        this.descriptionScrollPanel.setPosAndSize(
                this.descriptionPanel.getPosX() + descriptionPanel.getWidth() - ResearchScreen.SCROLL_BAR_SIZE,
                this.descriptionPanel.getPosY(),
                ResearchScreen.SCROLL_BAR_SIZE,
                this.descriptionPanel.getHeight()
        );

        this.rewardScrollPanel.setPosAndSize(
                this.rewardPanel.getPosX() + rewardPanel.getWidth() - ResearchScreen.SCROLL_BAR_SIZE,
                this.rewardPanel.getPosY(),
                ResearchScreen.SCROLL_BAR_SIZE,
                this.rewardPanel.getHeight()
        );

        if(researchWidget == null) return;

        this.rewardPanel.setRenders(
                new RenderRequirements(researchWidget,
                        this.rewardPanel.posX, 0,
                        this.rewardPanel.width, this.rewardPanel.height,
                        4, 4, false),
                new RenderRewards(researchWidget,
                        this.rewardPanel.posX, 0,
                        this.rewardPanel.width, this.rewardPanel.height,
                        4, 4, false)
        );

        this.titleLabel.setX(((this.width - 16) - this.titleLabel.width) / 2);
        this.subtitleLabel.setX((this.width - subtitleLabel.width) / 2);
    }

    protected void calculateProperties() {
        int f1 = this.titleLabel.width + 16;
        f1 = Math.max(f1, this.subtitleLabel.width);
        setWidth(Math.max(f1, 210));

        this.descriptionPanel.setWidth(this.width - 4);
        this.descriptionPanel.setHeight(this.height / 3);

        this.rewardPanel.setWidth(descriptionPanel.width);
        this.rewardPanel.setHeight(descriptionPanel.height);

        this.startResearchButton.setX((this.width - this.startResearchButton.width) / 2);
        this.startResearchButton.setY(this.height - this.startResearchButton.height - 1);

        this.researchProgressBar.setWidth(this.width - 8);
        this.researchProgressBar.setX((this.width - researchProgressBar.width) / 2);

        this.researchProgressBar.setY(this.rewardPanel.getPosY() + this.rewardPanel.getHeight() + 1);

        this.stopResearchButton.setX((this.width - this.stopResearchButton.width) / 2);
        this.stopResearchButton.setY(this.height - this.stopResearchButton.height - 1);
        this.researchedLabel.setX((this.width - this.researchedLabel.width) / 2);
        this.researchedLabel.setY(this.height - this.researchedLabel.height * 2);
    }

    @Override
    public void addWidgets() {
        add(this.titleLabel = new TextField(this));
        add(this.subtitleLabel = new TextField(this) {
            @Override
            public boolean shouldDraw() {
                return researchWidget.research.hasSubtitle() || ResearchScreen.isEditMode();
            }
        });

        add(this.researchedLabel = new TextField(this) {
            @Override
            public boolean shouldDraw() {
                return researchWidget != null && researchWidget.researched;
            }
        });
        researchedLabel.setText(Component.translatable("research.ui.info.researched_label"));

        add(this.descriptionPanel = new DescriptionPanel(this));
        add(this.descriptionScrollPanel = new PanelScrollBar(this, descriptionPanel) {

            @Override
            public void drawScrollBar(GuiGraphics graphics, Theme theme, int x, int y, int w, int h) {
                ResearchRenderUtils.drawScrollHeadBackground(graphics, x, y, w, h);
            }

            @Override
            public void drawBackground(GuiGraphics graphics, Theme theme, int x, int y, int w, int h) {
                ResearchRenderUtils.drawScrollBackground(graphics, x, y, w, h);
            }

            @Override
            public int getScrollBarSize() {
                return ResearchScreen.SCROLL_BAR_SIZE;
            }
        });

        add(this.rewardPanel = new RewardPanel(this));
        add(this.rewardScrollPanel = new PanelScrollBar(this, rewardPanel) {

            @Override
            public void drawScrollBar(GuiGraphics graphics, Theme theme, int x, int y, int w, int h) {
                ResearchRenderUtils.drawScrollHeadBackground(graphics, x, y, w, h);
            }

            @Override
            public void drawBackground(GuiGraphics graphics, Theme theme, int x, int y, int w, int h) {
                ResearchRenderUtils.drawScrollBackground(graphics, x, y, w, h);
            }

            @Override
            public double getScrollStep() {
                return ResearchScreen.SCROLL_BAR_SIZE;
            }
        });

        add(this.startResearchButton = new SimpleTextButton(this, Component.translatable("research.ui.info.research_button"), RenderIcon.OPEN_BOOK) {
            @Override
            public void onClicked(MouseButton mouseButton) {
                if(ResearchUtils.canStartResearch(Minecraft.getInstance().player, researchWidget.research, true)){
                    ResearchScreen.Instance.setModalPanel(new ResearchInfoModalPanel.StartResearch(getGui()));
                } else {
                    ResearchScreen.Instance.setModalPanel(new ResearchInfoModalPanel.CantStartResearch(getGui()));
                }
            }

            @Override
            public boolean checkMouseOver(int mouseX, int mouseY) {
                return researchStage == ResearchStage.UN_RESEARCHED && super.checkMouseOver(mouseX, mouseY);
            }

            @Override
            public boolean shouldDraw() {
                return researchWidget.canResearch && researchStage == ResearchStage.UN_RESEARCHED;
            }

            @Override
            public void drawBackground(GuiGraphics graphics, Theme theme, int x, int y, int w, int h) {
                ResearchScreen.DEFAULT_BACKGROUND.drawRoundFill(graphics,x,y,w,h, 6);
            }
        });

        add(this.researchProgressBar = new ProgressBarWidget(this) {
            @Override
            public boolean shouldDraw() {
                return researchStage == ResearchStage.START_RESEARCH;
            }
        });

        add(this.stopResearchButton = new SimpleTextButton(this, Component.translatable("research.ui.info.stop_research_button"), RenderIcon.CANCEL) {

            @Override
            public void onClicked(MouseButton mouseButton) {
                ResearchScreen.Instance.setModalPanel(new ResearchInfoModalPanel.CancelResearch(getGui()));
            }

            @Override
            public boolean checkMouseOver(int mouseX, int mouseY) {
                return researchStage == ResearchStage.START_RESEARCH && super.checkMouseOver(mouseX, mouseY);
            }

            @Override
            public boolean isEnabled() {
                return researchStage == ResearchStage.START_RESEARCH && researchWidget.research.canStopResearch();
            }

            @Override
            public boolean shouldDraw() {
                return researchStage == ResearchStage.START_RESEARCH && researchWidget.research.canStopResearch();
            }

            @Override
            public void drawBackground(GuiGraphics graphics, Theme theme, int x, int y, int w, int h) {
                ResearchScreen.DEFAULT_BACKGROUND.drawRoundFill(graphics,x,y,w,h, 6);
            }
        });
    }

    protected void addDescription() {
        for (TextField descriptionField : descriptionFields) {
            descriptionPanel.getWidgets().remove(descriptionField);
        }

        descriptionFields.clear();

        if(researchWidget == null) return;

        for (Component component : researchWidget.research.getDescription()) {
            TextField field = new TextField(descriptionPanel).setMaxWidth(descriptionPanel.width - ResearchScreen.SCROLL_BAR_SIZE).setSpacing(9).setText(component);
            field.setWidth(descriptionPanel.width - ResearchScreen.SCROLL_BAR_SIZE);
            descriptionPanel.add(field);
            descriptionFields.add(field);
        }

        int sY = 0;

        for (TextField descriptionField : descriptionFields) {
            descriptionField.setY(sY);
            sY += descriptionField.height + 2;
        }
    }

    @Override
    public void alignWidgets() {
        this.titleLabel.setPos(ICON_SIZE,ICON_SIZE / 3);
        this.titleLabel.setWidth(this.width - 16);
        this.subtitleLabel.setY(ICON_SIZE + WIDGET_SPACE_Y);
        this.subtitleLabel.setWidth(this.width);
        this.descriptionPanel.setPos(2, subtitleLabel.posY + Minecraft.getInstance().font.lineHeight + 2);
        this.rewardPanel.setPos(2, descriptionPanel.posY + descriptionPanel.height + 9);
        this.researchProgressBar.setHeight(8);
        calculateProperties();
    }


    @Override
    public boolean isEnabled() {
        return this.researchWidget != null;
    }

    @Override
    public boolean shouldDraw() {
        return this.researchWidget != null;
    }

    @Override
    public void draw(GuiGraphics graphics, Theme theme, int x, int y, int w, int h) {
        graphics.pose().pushPose();
        graphics.pose().translate(0,0, 400);
        super.draw(graphics, theme, x, y, w, h);
        graphics.pose().popPose();
    }

    @Override
    public void drawBackground(GuiGraphics graphics, Theme theme, int x, int y, int w, int h) {
        ShapesRenderHelper.drawRoundedRect(graphics, x,y,w,h, 6, ResearchScreen.DEFAULT_BACKGROUND, DrawDirection.RIGHT);
//        ResearchScreen.DEFAULT_BACKGROUND.drawRoundFill(graphics, x,y,w,h, 6);

        if(researchWidget == null) return;

        researchWidget.research.getIcon().draw(graphics,x + this.titleLabel.getX() - ICON_SIZE - 1 ,y,ICON_SIZE,ICON_SIZE);

        ResearchRenderUtils.drawLine(LINE_COLOR, graphics, x, y + descriptionPanel.getPosY() + descriptionPanel.getHeight() + 4, w, 1, 4);
    }


    protected class DescriptionPanel extends Panel {

        public DescriptionPanel(Panel panel) {
            super(panel);
        }

        @Override
        public void addWidgets() {}

        @Override
        public void alignWidgets() {}

        @Override
        public void drawBackground(GuiGraphics graphics, Theme theme, int x, int y, int w, int h) {
            ResearchScreen.DEFAULT_BACKGROUND.drawRoundFill(graphics, x,y,w,h, 4);
        }
    }

    public class RewardPanel extends BaseRenderReward {

        public RewardPanel(Panel panel) {
            super(panel);
        }


        public void setRenders(RenderRequirements requirements, RenderRewards rewards) {
            super.setRenders(requirements, rewards);
            ResearchInfoPanel.this.rewardScrollPanel.setValue(0);
        }
    }
}
