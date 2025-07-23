package net.sixik.researchtree;

import dev.architectury.event.events.common.LifecycleEvent;
import dev.architectury.event.events.common.PlayerEvent;
import net.sixik.researchtree.network.ask.SyncResearchASK;
import net.sixik.researchtree.network.fromServer.SendPlayerResearchDataS2C;
import net.sixik.researchtree.research.DebugResearchData;
import net.sixik.researchtree.research.ResearchData;
import net.sixik.researchtree.research.manager.ServerResearchManager;
import net.sixik.researchtree.utils.ResearchUtils;

public class ResearchTreeModEvents {

    public static void init() {
        LifecycleEvent.SERVER_STARTED.register(ServerResearchManager::new);
        LifecycleEvent.SERVER_STOPPED.register(server -> {

            if(ServerResearchManager.getInstance() != null) {
                ServerResearchManager.getInstance().shutdown();
            }
        });

        PlayerEvent.PLAYER_JOIN.register(serverPlayer -> {
            SendPlayerResearchDataS2C.sendTo(serverPlayer);

            ServerResearchManager researchManager = ResearchUtils.getManagerCast(false);
            researchManager.createResearchData(ResearchData.DEFAULT, DebugResearchData::new).ifPresent(data -> {
                new SyncResearchASK(null).startRequest(serverPlayer, data.getId());
            });
        });
    }
}
