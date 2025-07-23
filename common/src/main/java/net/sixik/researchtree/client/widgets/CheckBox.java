package net.sixik.researchtree.client.widgets;

import dev.ftb.mods.ftblibrary.icon.Icon;
import dev.ftb.mods.ftblibrary.icon.Icons;
import dev.ftb.mods.ftblibrary.ui.Panel;
import dev.ftb.mods.ftblibrary.ui.SimpleTextButton;
import dev.ftb.mods.ftblibrary.ui.input.MouseButton;
import net.minecraft.network.chat.Component;

import java.util.function.BiConsumer;

public class CheckBox extends SimpleTextButton {

    protected BiConsumer<CheckBox, Boolean> onClick;

    protected boolean checked;
    protected Icon checkIcon = Icons.ACCEPT;
    protected Icon uncheckIcon = Icons.CANCEL;

    public CheckBox(Panel panel, Component txt, BiConsumer<CheckBox, Boolean> onClick) {
        this(panel, txt, false, onClick);
    }

    public CheckBox(Panel panel, Component txt, boolean checked, BiConsumer<CheckBox, Boolean> onClick) {
        super(panel, txt, Icon.empty());
        this.onClick = onClick;
        this.checked = checked;
        icon = checked ? checkIcon : uncheckIcon;
    }

    @Override
    public void onClicked(MouseButton mouseButton) {
        checked = !checked;

        icon = checked ? checkIcon : uncheckIcon;
        onClick.accept(this, checked);
    }
}
