package net.sixik.researchtree.research.requirements;

import com.mojang.serialization.Codec;
import dev.ftb.mods.ftblibrary.util.TooltipList;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.player.Player;
import net.sixik.researchtree.api.interfaces.TooltipSupport;
import net.sixik.researchtree.client.render.RenderTooltip;
import net.sixik.researchtree.research.BaseResearch;
import net.sixik.researchtree.utils.ResearchRenderUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public abstract class Requirements implements TooltipSupport {

    protected List<String> tooltips = new ArrayList<>();
    private List<Component> cachedTooltips;


    public Requirements(Void v) {}

    public abstract boolean execute(Player player, BaseResearch research);
    public abstract boolean canExecute(Player player, BaseResearch research);
    public abstract void refund(Player player, BaseResearch research, double percentageOfReturn);


    @Environment(EnvType.CLIENT)
    public abstract void draw(GuiGraphics graphics, int x, int y, int w, int h);

    public void addTooltip(TooltipList list) {}

    public abstract void plus(Requirements requirements);
    public abstract void minus(Requirements requirements);

    public abstract int getCount();
    public abstract void setCount(int count);

    public boolean canMathOperation(Requirements requirements) {
        return Objects.equals(this.getClass(), requirements.getClass());
    }

    @Environment(EnvType.CLIENT)
    public void drawCustomTooltip(GuiGraphics graphics, int x, int y, RenderTooltip tooltip, TooltipList list) {
        addTooltip(list);
        for (Component component : getTooltipReady(Minecraft.getInstance().level.registryAccess())) {
            list.add(component);
        }

        ResearchRenderUtils.drawTooltip(graphics, x,y, list);
    }

    public abstract Requirements copy();

    public abstract <T extends Requirements> Codec<T> codec();
    public abstract <T extends Requirements> StreamCodec<RegistryFriendlyByteBuf, T> streamCodec();



    public String getId() {
        return this.getClass().getName();
    }

    @Override
    public List<String> getTooltipList() {
        return tooltips;
    }

    @Override
    @Environment(EnvType.CLIENT)
    public List<Component> getCachedTooltip() {
        if(cachedTooltips == null)
            cachedTooltips = getTooltipReady(Minecraft.getInstance().level.registryAccess());
        return cachedTooltips;
    }
}


