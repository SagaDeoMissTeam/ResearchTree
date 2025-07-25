package net.sixik.researchtree.api.managers;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

import java.util.Collection;

public abstract class StageManager {

    public abstract boolean hasStage(Player player, String stage);
    public abstract boolean hasStages(Player player, Collection<String> stages);

    public abstract Collection<String> getStages(Player player);

    public abstract boolean addStage(ServerPlayer player, String stage);
    public abstract boolean addStage(ServerPlayer player, Collection<String> stage);
    public abstract boolean removeStage(ServerPlayer player, String stage);

    public abstract boolean removeStage(ServerPlayer player, Collection<String> stages);
}
