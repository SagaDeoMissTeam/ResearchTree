package net.sixik.researchtree.research.triggers;

import com.mojang.serialization.Codec;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.sixik.researchtree.research.BaseResearch;
import net.sixik.researchtree.utils.SDMItemHelper;

public class ItemInHandTrigger extends BaseTrigger{

    protected ItemStack itemStack;

    public ItemInHandTrigger(Void nul) {
        super(nul);
    }

    public ItemInHandTrigger(ItemStack itemStack) {
        super(null);
        this.itemStack = itemStack;
    }

    @Override
    public boolean checkComplete(Player player, BaseResearch research, Object[] args) {
        return SDMItemHelper.equals(player.getItemInHand(InteractionHand.MAIN_HAND), itemStack) || SDMItemHelper.equals(player.getItemInHand(InteractionHand.OFF_HAND), itemStack);
    }

    @Override
    public <T extends BaseTrigger> Codec<T> codec() {
        return null;
    }

    @Override
    public <T extends BaseTrigger> StreamCodec<RegistryFriendlyByteBuf, T> streamCodec() {
        return null;
    }

    @Override
    public String getId() {
        return "item_trigger";
    }
}
