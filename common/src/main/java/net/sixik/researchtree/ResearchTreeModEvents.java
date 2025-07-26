package net.sixik.researchtree;

import dev.architectury.event.EventResult;
import dev.architectury.event.events.common.*;
import net.minecraft.server.level.ServerPlayer;
import net.sixik.researchtree.config.ModConfig;
import net.sixik.researchtree.network.fromServer.SendPlayerResearchDataS2C;
import net.sixik.researchtree.research.manager.ServerResearchManager;

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

        TickEvent.PLAYER_POST.register(player -> {
            if(ServerResearchManager.getInstance() != null)
                ServerResearchManager.getInstance().tickTriggersLimit(player);
        });

        EntityEvent.LIVING_DEATH.register((livingEntity, damageSource) -> {
            if(damageSource.getDirectEntity() instanceof ServerPlayer serverPlayer) {
                
            }

            return EventResult.interruptDefault();
        });
    }
}
