package net.sixik.researchtree.client;

import dev.ftb.mods.ftblibrary.ui.Panel;
import dev.ftb.mods.ftblibrary.util.TooltipList;
import net.minecraft.network.chat.Component;
import net.sixik.researchtree.DebugConstants;
import net.sixik.researchtree.client.widgets.CheckBox;

public class ResearchToolPanel extends Panel {

    protected CheckBox editMode;

    public ResearchToolPanel(Panel panel) {
        super(panel);
    }

    @Override
    public void addWidgets() {
        add(this.editMode = new CheckBox(this, Component.empty(), DebugConstants.hasResearch, (d1, d2) -> DebugConstants.hasResearch = !DebugConstants.hasResearch) {
            @Override
            public boolean shouldDraw() {
                return true;
            }

            @Override
            public void addMouseOverText(TooltipList list) {
                list.add(Component.literal("Edit Mode: " + DebugConstants.hasResearch));
            }
        });
    }

    @Override
    public void alignWidgets() {
        this.editMode.setSize(this.height, this.height);
    }
}
