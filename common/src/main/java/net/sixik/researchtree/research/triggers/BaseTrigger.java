package net.sixik.researchtree.research.triggers;

import com.mojang.serialization.Codec;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.player.Player;
import net.sixik.researchtree.research.BaseResearch;

public abstract class BaseTrigger {

    private int index;

    public BaseTrigger(Void nul) {}

    public abstract boolean checkComplete(Player player, BaseResearch research, Object[] args);

    public abstract <T extends BaseTrigger> Codec<T> codec();

    public abstract <T extends BaseTrigger> StreamCodec<RegistryFriendlyByteBuf, T> streamCodec();

    public final boolean hasCodec() {
        return codec() != null;
    }

    public final boolean hasStreamCodec() {
        return streamCodec() != null;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public int getIndex() {
        return index;
    }

    public String getId() {
        return this.getClass().getSimpleName().toLowerCase();
    }
}
