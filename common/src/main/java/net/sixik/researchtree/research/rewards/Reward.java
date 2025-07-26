package net.sixik.researchtree.research.rewards;

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
import java.util.Collection;
import java.util.List;

public abstract class Reward implements TooltipSupport {

    protected List<String> tooltips = new ArrayList<>();
    private List<Component> cachedTooltips;

    public static final String ID_KEY = "object_id";

    public Reward(Void v) {}

    public abstract void giveReward(Player player, BaseResearch research);

    @Environment(EnvType.CLIENT)
    public abstract void draw(GuiGraphics graphics, int x, int y, int w, int h);

    @Environment(EnvType.CLIENT)
    public void drawCustomTooltip(GuiGraphics graphics, int x, int y, RenderTooltip tooltip, TooltipList list) {
        addTooltip(list);
        getCachedTooltip().forEach(list::add);

        ResearchRenderUtils.drawTooltip(graphics,x,y,list);
    }

    @Override
    public void addTooltip(Collection<String> str) {
        tooltips.addAll(str);
        cachedTooltips = null;
    }

    @Override
    public void addTooltip(String str) {
        tooltips.add(str);
        cachedTooltips = null;
    }

    public void addTooltip(TooltipList list) {}

    public abstract <T extends Reward> Codec<T> codec();
    public abstract <T extends Reward> StreamCodec<RegistryFriendlyByteBuf, T> streamCodec();

    public String getId() {
        return this.getClass().getName();
    }

    protected abstract Reward copy();

    public final Reward copyInternal() {
        Reward copy = copy();
        copy.addTooltip(tooltips);
        return copy;
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

