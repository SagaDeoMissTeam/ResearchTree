package net.sixik.researchtree.research.rewards;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.ftb.mods.ftblibrary.icon.Icon;
import dev.ftb.mods.ftblibrary.icon.ItemIcon;
import dev.ftb.mods.ftblibrary.ui.GuiHelper;
import dev.ftb.mods.ftblibrary.util.TooltipList;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.sixik.researchtree.research.BaseResearch;
import net.sixik.researchtree.utils.ExternCodecs;
import net.sixik.researchtree.utils.SDMItemHelper;

import java.util.ArrayList;
import java.util.List;

public class ItemReward extends Reward{

    public static final Codec<ItemReward> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    ExternCodecs.ITEM_STACK_CODEC.fieldOf("item").forGetter(ItemReward::getItemStack)
            ).apply(instance, ItemReward::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, ItemReward> STREAM_CODEC = StreamCodec.composite(
            ItemStack.STREAM_CODEC, ItemReward::getItemStack, ItemReward::new
    );

    protected ItemStack itemStack;
    protected Icon icon;

    public ItemReward(Void v) {
        super(v);
    }

    public ItemReward(ItemStack itemStack) {
        super(null);
        this.itemStack = itemStack;
        this.icon = ItemIcon.getItemIcon(itemStack);
    }

    public ItemStack getItemStack() {
        return itemStack;
    }

    @Override
    public void giveReward(Player player, BaseResearch research) {
        SDMItemHelper.giveItems(player, itemStack, itemStack.getCount());
    }

    @Override
    public void draw(GuiGraphics graphics, int x, int y, int w, int h) {
        icon.draw(graphics, x, y, w, h);
    }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof ItemReward requirements)
            return SDMItemHelper.equals(this.itemStack, requirements.itemStack);
        return super.equals(obj);
    }

    @Override
    public Codec<? extends Reward> codec() {
        return CODEC;
    }

    @Override
    public StreamCodec<RegistryFriendlyByteBuf, ? extends Reward> streamCodec() {
        return STREAM_CODEC;
    }

    @Override
    public void addTooltip(TooltipList list) {
        List<Component> list1 = new ArrayList();
        GuiHelper.addStackTooltip(itemStack, list1);
        list1.forEach(list::add);
        list.add(Component.empty());
        list.add(Component.literal("Count: " + itemStack.getCount()));
    }

    @Override
    public String getId() {
        return "item_reward";
    }
}
