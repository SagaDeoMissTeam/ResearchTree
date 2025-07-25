package net.sixik.researchtree.compat.kubejs;

import dev.latvian.mods.kubejs.core.PlayerKJS;
import dev.latvian.mods.kubejs.stages.Stages;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.sixik.researchtree.api.managers.StageManager;

import java.util.Collection;

public class KubeJSStageManager extends StageManager {

    protected Stages getStage(Player player) {
        return ((PlayerKJS)player).kjs$getStages();
    }

    @Override
    public boolean hasStage(Player player, String stage) {
        return getStage(player).has(stage);
    }

    @Override
    public boolean hasStages(Player player, Collection<String> stages) {
        Stages data = getStage(player);
        boolean has = true;
        for (String stage : stages) {
            if(!data.has(stage)) {
                has = false;
                break;
            }
        }

        return has;
    }

    @Override
    public Collection<String> getStages(Player player) {
        return getStage(player).getAll();
    }

    @Override
    public boolean addStage(ServerPlayer player, String stage) {
        return getStage(player).add(stage);
    }

    @Override
    public boolean addStage(ServerPlayer player, Collection<String> stages) {
        Stages data = getStage(player);
        stages.forEach(data::add);
        return true;
    }

    @Override
    public boolean removeStage(ServerPlayer player, String stage) {
        return getStage(player).remove(stage);
    }

    @Override
    public boolean removeStage(ServerPlayer player, Collection<String> stages) {
        Stages data = getStage(player);
        stages.forEach(data::remove);
        return true;
    }
}
