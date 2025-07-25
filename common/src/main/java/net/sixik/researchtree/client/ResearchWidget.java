package net.sixik.researchtree.client;

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
}
