package net.sixik.researchtree.client;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.ftb.mods.ftblibrary.icon.Color4I;
import dev.ftb.mods.ftblibrary.icon.Icon;
import dev.ftb.mods.ftblibrary.ui.Panel;
import dev.ftb.mods.ftblibrary.ui.SimpleTextButton;
import dev.ftb.mods.ftblibrary.ui.Theme;
import dev.ftb.mods.ftblibrary.ui.input.MouseButton;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.sixik.researchtree.client.widgets.ProgressBarWidget;
import net.sixik.researchtree.research.BaseResearch;
import net.sixik.researchtree.research.ResearchHideTypeRender;
import net.sixik.researchtree.research.ResearchShowType;
import net.sixik.researchtree.research.ResearchStage;
import net.sixik.researchtree.research.manager.PlayerResearchData;
import net.sixik.researchtree.utils.RenderIcon;
import net.sixik.researchtree.utils.ResearchUtils;
import net.sixik.sdmuilibrary.client.utils.DrawDirection;
import net.sixik.sdmuilibrary.client.utils.renders.ShapesRenderHelper;

import java.util.List;
import java.util.Optional;

public class ResearchWidget extends SimpleTextButton {

    public static final int RESEARCHED_ICON_SIZE = 8;

    public BaseResearch research;
    public boolean researched;
    public boolean canResearch;
    protected ProgressBarWidget progressBar;
    public ResearchStage researchStage;

    public HideRender hideRender = HideRender.NORMAL;

    public ResearchWidget(Panel panel, BaseResearch research) {
        this(panel, Component.empty(), Icon.empty());
        setResearch(research);
    }

    public ResearchWidget(Panel panel, Component text, Icon icon) {
        super(panel, text, icon);
        progressBar = new ProgressBarWidget(panel);
    }

    public ResearchWidget setResearch(BaseResearch research) {
        this.research = research;
        this.title = research.getTranslate();
        this.icon = research.getIcon();
        this.progressBar.setMaxValue(100);
        this.progressBar.setValue(ResearchUtils.getPercentResearch(Minecraft.getInstance().player, research, true));
        update();
        return this;
    }

    public void update() {
        this.researched = isResearchedUnCached();
        this.canResearch = ResearchUtils.isResearchParentsResearched(Minecraft.getInstance().player, research, true);
        this.researchStage = ResearchUtils.getResearchStage(Minecraft.getInstance().player, research, true);

        if(this.researchStage == ResearchStage.LOCKED) {

            switch (research.hideTypeRender) {
                case QUESTION_STYLE -> {
                    this.icon = RenderIcon.UNKNOWN;
                    this.title = Component.literal("???");
                }
                case BLACKOUT_STYLE -> {
                    this.icon = this.icon.withColor(Color4I.BLACK);
                    this.title = Component.literal("???");
                }
                case HIDE_STYLE -> {
                   this.hideRender = HideRender.HIDDEN;
                }
                case EMPTY -> {
                    this.hideRender = HideRender.ALPHA;
                }
            }
        } else {
            this.title = research.getTranslate();
            this.icon = research.getIcon();
            this.hideRender = HideRender.NORMAL;
        }
    }

    @Override
    public void draw(GuiGraphics graphics, Theme theme, int x, int y, int w, int h) {
        if(hideRender == HideRender.ALPHA) {
//            RenderSystem.enableBlend();
//            RenderSystem.defaultBlendFunc();
            RenderSystem.setShaderColor(1,1,1, 0.5f);
            super.draw(graphics, theme, x, y, w, h);
            RenderSystem.setShaderColor(1,1,1,1);
//            RenderSystem.disableBlend();
        } else {
            super.draw(graphics, theme, x, y, w, h);
        }


    }

    public boolean isResearchedUnCached() {
        return ResearchScreen.Instance.movePanel.playerResearchData.containsInUnlockedResearch(research.getId());
    }

    public ResearchWidget updateProgress(double value) {
        this.progressBar.setValue(value);
        return this;
    }

    public boolean hasParents() {
        return research.hasParents();
    }

    @Override
    public void drawBackground(GuiGraphics graphics, Theme theme, int x, int y, int w, int h) {
        if(ResearchUtils.isStartResearch(Minecraft.getInstance().player, research, true)) {
            ShapesRenderHelper.drawRoundedRect(graphics, x, y, w, h, 4, ResearchScreen.DEFAULT_WIDGET_COLOR, DrawDirection.UP);
            progressBar.draw(graphics, theme, x,y + h,w,8);

        }
        else ResearchScreen.DEFAULT_WIDGET_COLOR.drawRoundFill(graphics, x,y,w,h, 4);

        if(researched) {
            RenderIcon.CONFIRM.draw(graphics, x + w - RESEARCHED_ICON_SIZE, y,RESEARCHED_ICON_SIZE, RESEARCHED_ICON_SIZE);
        }

    }

    @Override
    public void tick() {
        if(!researched && canResearch) {
            Optional<PlayerResearchData.ResearchProgressData> optional = ResearchScreen.Instance.movePanel.playerResearchData.getProgressResearch(research.getId());
            optional.ifPresent(data -> updateProgress(data.getProgressPercentDouble()));
        }
    }

    @Override
    public boolean checkMouseOver(int mouseX, int mouseY) {
        if(researchStage == ResearchStage.LOCKED) return false;

        return ResearchScreen.getMouseOverWidget()
                .filter(value -> value == parent && super.checkMouseOver(mouseX, mouseY))
                .isPresent();
    }

    @Override
    public void onClicked(MouseButton mouseButton) {
        if(!mouseButton.isLeft()) return;

        if(ResearchScreen.getResearch() == this)
            ResearchScreen.setResearch(null);
        else ResearchScreen.setResearch(this);
    }

    public List<ResearchWidget> getParentWidgets() {
       return ResearchScreen.Instance.getResearchWidgets().stream().filter(s -> research.getParentResearch().contains(s.research)).toList();
    }

    @Override
    public boolean shouldDraw() {
        return hideRender.isRender();
    }
}
