package net.sixik.researchtree.research.triggers;

import com.mojang.serialization.Codec;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.sixik.researchtree.research.BaseResearch;

import java.util.function.BiFunction;

public class CustomTrigger extends BaseTrigger{

    protected BiFunction<ServerPlayer, BaseResearch, Boolean> consumer;

    public CustomTrigger(Void nul) {
        super(nul);
    }

    public CustomTrigger(BiFunction<ServerPlayer, BaseResearch, Boolean> consumer) {
        super(null);
        this.consumer = consumer;
    }

    @Override
    public boolean checkComplete(Player player, BaseResearch research, Object[] args) {
        if(player instanceof ServerPlayer serverPlayer)
            return consumer.apply(serverPlayer, research);
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
        return "custom_trigger";
    }
}
