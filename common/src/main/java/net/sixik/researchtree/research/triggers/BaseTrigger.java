package net.sixik.researchtree.research.triggers;

import com.mojang.serialization.Codec;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.sixik.researchtree.api.interfaces.FunctionSupport;
import net.sixik.researchtree.research.BaseResearch;
import net.sixik.researchtree.research.functions.BaseFunction;
import net.sixik.researchtree.research.manager.PlayerResearchData;
import net.sixik.researchtree.research.manager.ServerResearchManager;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public abstract class BaseTrigger implements FunctionSupport {

    private int index;

    protected List<BaseFunction> onTriggerComplete = new ArrayList<>();

    public BaseTrigger(Void nul) {}

    public abstract boolean checkComplete(Player player, BaseResearch research, Object[] args);

    public abstract <T extends BaseTrigger> Codec<T> codec();

    public abstract <T extends BaseTrigger> StreamCodec<RegistryFriendlyByteBuf, T> streamCodec();

    public EventType getEventType() {
        return EventType.PLAYER_TICK;
    }

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

    public final void executeInternal(ServerResearchManager manager, PlayerResearchData playerResearchData, BaseResearch research, Player player, Object... args) {
        if(checkComplete(player, research, args)) {
            executeFunction(BaseFunction.ExecuteStage.NONE, (ServerPlayer) player, research);
            playerResearchData.getTriggerDataOrCreate(research.getId()).ifPresent(triggerResearchData -> {
                triggerResearchData.addComplete(getIndex());
            });
        }
    }

    @Override
    public Collection<BaseFunction> getFunctions() {
        return onTriggerComplete;
    }
}
