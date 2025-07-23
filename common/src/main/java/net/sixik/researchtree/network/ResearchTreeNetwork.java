package net.sixik.researchtree.network;

import dev.ftb.mods.ftblibrary.util.NetworkHelper;
import net.minecraft.resources.ResourceLocation;
import net.sixik.researchtree.ResearchTree;
import net.sixik.researchtree.network.ask.SyncAndOpenResearchScreenASK;
import net.sixik.researchtree.network.ask.SyncResearchASK;
import net.sixik.researchtree.network.fromServer.SendOpenResearchScreen;
import net.sixik.researchtree.network.fromServer.SendPlayerResearchDataS2C;
import net.sixik.researchtree.network.fromServer.SendResearchesS2C;
import net.sixik.sdmeconomy.network.SDMEconomyNetwork;

public class ResearchTreeNetwork {

    public static final String SYNC_AND_OPEN_RESEARCH_SCREEN =
            SDMEconomyNetwork.registerRequest("sync_and_open_research_screen", SyncAndOpenResearchScreenASK::new);

    public static final String SYNC_RESEARCH =
            SDMEconomyNetwork.registerRequest("sync_research", SyncResearchASK::new);

    public static void init() {
        NetworkHelper.registerS2C(SendPlayerResearchDataS2C.TYPE, SendPlayerResearchDataS2C.STREAM_CODEC, SendPlayerResearchDataS2C::handle);
        NetworkHelper.registerS2C(SendResearchesS2C.TYPE, SendResearchesS2C.STREAM_CODEC, SendResearchesS2C::handle);
        NetworkHelper.registerS2C(SendOpenResearchScreen.TYPE, SendOpenResearchScreen.STREAM_CODEC, SendOpenResearchScreen::handle);



    }

    public static ResourceLocation nameOf(String name) {
        return ResourceLocation.tryBuild(ResearchTree.MODID, name);
    }
}
