package net.sixik.researchtree.research.triggers;

import com.mojang.serialization.Codec;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stat;
import net.minecraft.stats.StatType;
import net.minecraft.stats.Stats;
import net.minecraft.world.entity.player.Player;
import net.sixik.researchtree.research.BaseResearch;

public class PlayerStatsTrigger extends BaseTrigger{

    protected Stat<ResourceLocation> stat;
    protected StatType<Object> statType;
    protected int value;
    protected Object check;

    public PlayerStatsTrigger(Void nul) {
        super(nul);
    }

    public PlayerStatsTrigger(ResourceLocation statId, int count) {
        this(statId, count, null);
    }

    public PlayerStatsTrigger(ResourceLocation statId, int count, Object value) {
        super(null);
        this.stat = Stats.CUSTOM.get(statId);
        if(this.stat == null)
            this.statType = (StatType<Object>) BuiltInRegistries.STAT_TYPE.get(statId);

        this.value = count;
        this.check = value;
    }

    @Override
    public boolean checkComplete(Player player, BaseResearch research, Object[] args) {
        if(player instanceof ServerPlayer serverPlayer) {

            if(this.stat == null)
                return serverPlayer.getStats().getValue(statType, check) >= value;
            return serverPlayer.getStats().getValue(stat) >= value;
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
        return "player_stat_trigger";
    }
}
