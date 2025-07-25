package net.sixik.researchtree.research.requirements;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.ftb.mods.ftblibrary.icon.Icon;
import dev.ftb.mods.ftblibrary.icon.ItemIcon;
import dev.ftb.mods.ftblibrary.ui.GuiHelper;
import dev.ftb.mods.ftblibrary.util.TooltipList;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.sixik.researchtree.research.BaseResearch;
import net.sixik.researchtree.utils.ExternCodecs;
import net.sixik.researchtree.utils.ResearchUtils;
import net.sixik.researchtree.utils.SDMItemHelper;

import java.util.ArrayList;
import java.util.List;

public class ItemRequirements extends Requirements {

    public static final Codec<ItemRequirements> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    ExternCodecs.ITEM_STACK_CODEC.fieldOf("item").forGetter(ItemRequirements::getItemStack)
            ).apply(instance, ItemRequirements::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, ItemRequirements> STREAM_CODEC = StreamCodec.composite(
            ItemStack.STREAM_CODEC, ItemRequirements::getItemStack, ItemRequirements::new
    );

    protected ItemStack itemStack;
    protected Icon icon;

    public ItemRequirements(Void v) {
        super(v);
    }

    public ItemRequirements(ItemStack itemStack) {
        super(null);
        this.itemStack = itemStack;
        this.icon = ItemIcon.getItemIcon(itemStack);
    }

    public ItemStack getItemStack() {
        return itemStack;
    }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof ItemRequirements requirements)
            return SDMItemHelper.equals(this.itemStack, requirements.itemStack);
        return super.equals(obj);
    }

    @Override
    public boolean execute(Player player, BaseResearch research) {
        return SDMItemHelper.shrinkItem(player.getInventory(), itemStack, itemStack.getCount(), itemStack.getComponents().isEmpty());
    }

    @Override
    public boolean canExecute(Player player, BaseResearch research) {
        return SDMItemHelper.countItem(player.getInventory(), itemStack, itemStack.getComponents().isEmpty()) >= itemStack.getCount();
    }

    @Override
    public void refund(Player player, BaseResearch research, double percentageOfReturn) {
        if(percentageOfReturn < 100 && itemStack.getCount() == 1) {
            SDMItemHelper.giveItems(player, itemStack, player.level().random.nextBoolean() ? 1 : 0);
        } else {
            SDMItemHelper.giveItems(player, itemStack, ResearchUtils.countFromPercents(itemStack.getCount(), percentageOfReturn));
        }
    }

    @Override
    @Environment(EnvType.CLIENT)
    public void draw(GuiGraphics graphics, int x, int y, int w, int h) {
        icon.draw(graphics, x, y, w, h);
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
    public void plus(Requirements requirements) {
        ItemRequirements another = (ItemRequirements) requirements;
        this.itemStack.setCount(itemStack.getCount() + another.itemStack.getCount());
    }

    @Override
    public void minus(Requirements requirements) {
        ItemRequirements another = (ItemRequirements) requirements;
        this.itemStack.setCount(itemStack.getCount() - another.itemStack.getCount());
    }

    @Override
    public int getCount() {
        return this.itemStack.getCount();
    }

    @Override
    public void setCount(int count) {
        this.itemStack.setCount(count);
    }

    @Override
    public boolean canMathOperation(Requirements requirements) {
        return requirements instanceof ItemRequirements r && SDMItemHelper.equals(this.itemStack, r.itemStack, this.itemStack.getComponents().isEmpty());
    }

    @Override
    public Requirements copy() {
        return new ItemRequirements(itemStack);
    }

    @Override
    public Codec<ItemRequirements> codec() {
        return CODEC;
    }

    @Override
    public StreamCodec<RegistryFriendlyByteBuf, ItemRequirements> streamCodec() {
        return STREAM_CODEC;
    }

    @Override
    public String getId() {
        return "item_requirement";
    }
}
