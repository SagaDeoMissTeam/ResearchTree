package net.sixik.researchtree;

import dev.architectury.event.events.common.CommandRegistrationEvent;
import dev.architectury.event.events.common.LifecycleEvent;
import dev.architectury.event.events.common.PlayerEvent;
import net.sixik.researchtree.config.ModConfig;
import net.sixik.researchtree.network.ask.SyncResearchASK;
import net.sixik.researchtree.network.fromServer.SendPlayerResearchDataChangeS2C;
import net.sixik.researchtree.network.fromServer.SendPlayerResearchDataS2C;
import net.sixik.researchtree.research.DebugResearchData;
import net.sixik.researchtree.research.ResearchData;
import net.sixik.researchtree.research.manager.ServerResearchManager;
import net.sixik.researchtree.utils.ResearchUtils;

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
            SendPlayerResearchDataS2C.sendTo(serverPlayer);
        });

        PlayerEvent.PLAYER_QUIT.register(serverPlayer -> {
            ServerResearchManager.getInstance().getPlayerDataOptional(serverPlayer).ifPresent(playerData -> {
                playerData.updatePlayerOnline(ResearchTree.MOD_CONFIG.getResearchWhenPlayerOffline());
            });
        });
    }
}
