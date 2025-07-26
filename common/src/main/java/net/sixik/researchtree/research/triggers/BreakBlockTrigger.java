package net.sixik.researchtree.research.triggers;

import com.mojang.serialization.Codec;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.sixik.researchtree.research.BaseResearch;

import java.util.Objects;

public class BreakBlockTrigger extends BaseTrigger {

    protected BlockState blockState;

    public BreakBlockTrigger(Void nul) {
        super(nul);
    }

    public BreakBlockTrigger(BlockState blockState) {
        super(null);
        this.blockState = blockState;
    }

    @Override
    public boolean checkComplete(Player player, BaseResearch research, Object[] args) {
        if(args[0] instanceof BlockState state) {
            return Objects.equals(this.blockState, state);
        } else if(args[0] instanceof Block block) {
            return Objects.equals(this.blockState, block.defaultBlockState());
        }
        return false;
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
        return "block_break_trigger";
    }
}
