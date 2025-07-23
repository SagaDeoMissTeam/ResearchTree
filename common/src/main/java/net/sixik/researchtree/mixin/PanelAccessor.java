package net.sixik.researchtree.mixin;

import dev.ftb.mods.ftblibrary.ui.Panel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value = Panel.class, remap = false)
public interface PanelAccessor {

    @Accessor("offsetX")
    void setOffsetX(int x);

    @Accessor("offsetY")
    void setOffsetY(int y);

    @Accessor("offsetX")
    int getOffsetX();

    @Accessor("offsetY")
    int getOffsetY();
}
