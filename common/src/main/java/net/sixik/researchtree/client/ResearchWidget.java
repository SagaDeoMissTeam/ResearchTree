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
import net.sixik.researchtree.utils.ResearchUtils;
import net.sixik.sdmuilibrary.client.utils.DrawDirection;
import net.sixik.sdmuilibrary.client.utils.renders.ShapesRenderHelper;

import java.util.List;

public class ResearchWidget extends SimpleTextButton {

    public BaseResearch research;
    protected ProgressBarWidget widget;

    public ResearchWidget(Panel panel, BaseResearch research) {
        this(panel, Component.empty(), Icon.empty());
        setResearch(research);
    }

    public ResearchWidget(Panel panel, Component text, Icon icon) {
        super(panel, text, icon);
        widget = new ProgressBarWidget(panel);
        widget.setMaxValue(100);
        widget.setValue(ResearchUtils.getPercentResearch(Minecraft.getInstance().player, research));
    }

    public ResearchWidget setResearch(BaseResearch research) {
        this.research = research;
        this.title = research.getTranslate();
        this.icon = Icon.getIcon(research.getIconPath());
        return this;
    }

    public boolean hasParents() {
        return research.hasParents();
    }

    @Override
    public void drawBackground(GuiGraphics graphics, Theme theme, int x, int y, int w, int h) {

        if(ResearchUtils.isStartResearch(Minecraft.getInstance().player, research)) {
            ShapesRenderHelper.drawRoundedRect(graphics, x, y, w, h, 4, ResearchScreen.DEFAULT_WIDGET_COLOR, DrawDirection.UP);
            widget.draw(graphics, theme, x,y + h,w,8);

        }
        else ResearchScreen.DEFAULT_WIDGET_COLOR.drawRoundFill(graphics, x,y,w,h, 4);

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
