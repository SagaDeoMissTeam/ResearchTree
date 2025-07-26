package net.sixik.researchtree.research.triggers;

import com.mojang.serialization.Codec;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.sixik.researchtree.research.BaseResearch;

public class MobKillTrigger extends BaseTrigger{

    protected ResourceLocation modId;

    public MobKillTrigger(Void nul) {
        super(nul);
    }

    public MobKillTrigger(ResourceLocation modId) {
        super(null);
        this.modId = modId;
    }

    @Override
    public boolean checkComplete(Player player, BaseResearch research, Object[] args) {
        if(args[0] instanceof Entity entity) {
            return BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType()).equals(modId);
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
        return "entity_kill_trigger";
    }

    @Override
    public EventType getEventType() {
        return EventType.MOB_KILL;
    }
}
