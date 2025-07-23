package net.sixik.researchtree.client.widgets;

import dev.ftb.mods.ftblibrary.ui.Panel;
import dev.ftb.mods.ftblibrary.ui.Widget;

public class EmptyWidget extends Widget {
    public EmptyWidget(Panel p) {
        super(p);
    }

    @Override
    public boolean shouldDraw() {
        return false;
    }
}
