package net.sixik.researchtree;

import dev.architectury.event.EventResult;
import dev.architectury.event.events.common.*;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.sixik.researchtree.config.ModConfig;
import net.sixik.researchtree.network.fromServer.SendPlayerResearchDataS2C;
import net.sixik.researchtree.research.manager.ServerResearchManager;
import net.sixik.researchtree.research.triggers.EventType;

public class ResearchTreeModEvents {

    public static void init() {
        CommandRegistrationEvent.EVENT.register(ResearchTreeModCommands::registerCommands);

        LifecycleEvent.SERVER_BEFORE_START.register(ServerResearchManager::new);
        LifecycleEvent.SERVER_STOPPED.register(server -> {

            if(ServerResearchManager.getInstance() != null) {
                ServerResearchManager.getInstance().shutdown();
            }
        });

        PlayerEvent.PLAYER_JOIN.register(serverPlayer -> {
            ServerResearchManager.getInstance().getPlayerDataOptional(serverPlayer).ifPresent(playerData -> {
                playerData.updatePlayerOnline(true);
            });

            ServerResearchManager.getInstance().executeOfflineData(serverPlayer);

            if(!ServerResearchManager.getInstance().synchronizePlayerDataWithTeammates(serverPlayer))
                SendPlayerResearchDataS2C.sendTo(serverPlayer);
        });

        PlayerEvent.PLAYER_QUIT.register(serverPlayer -> {
            ServerResearchManager.getInstance().getPlayerDataOptional(serverPlayer).ifPresent(playerData -> {
                playerData.updatePlayerOnline(ResearchTree.MOD_CONFIG.getResearchWhenPlayerOffline());
            });
        });
    }
}
