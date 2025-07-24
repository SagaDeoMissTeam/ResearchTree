package net.sixik.researchtree.network;

import dev.ftb.mods.ftblibrary.util.NetworkHelper;
import net.minecraft.resources.ResourceLocation;
import net.sixik.researchtree.ResearchTree;
import net.sixik.researchtree.network.ask.GetAndOpenResearchScreenASK;
import net.sixik.researchtree.network.ask.SyncAndOpenResearchScreenASK;
import net.sixik.researchtree.network.ask.SyncResearchASK;
import net.sixik.researchtree.network.fromClient.SendStartResearchC2S;
import net.sixik.researchtree.network.fromServer.*;
import net.sixik.sdmeconomy.network.SDMEconomyNetwork;

public class ResearchTreeNetwork {

    public static final String SYNC_AND_OPEN_RESEARCH_SCREEN =
            SDMEconomyNetwork.registerRequest("sync_and_open_research_screen", SyncAndOpenResearchScreenASK::new);

    public static final String SYNC_RESEARCH =
            SDMEconomyNetwork.registerRequest("sync_research", SyncResearchASK::new);

    public static final String GET_AND_OPEN_RESEARCH_SCREEN =
            SDMEconomyNetwork.registerRequest("get_and_open_research_screen", GetAndOpenResearchScreenASK::new);

    public static void init() {
        NetworkHelper.registerS2C(SendPlayerResearchDataS2C.TYPE, SendPlayerResearchDataS2C.STREAM_CODEC, SendPlayerResearchDataS2C::handle);
        NetworkHelper.registerS2C(SendPlayerResearchDataChangeS2C.TYPE, SendPlayerResearchDataChangeS2C.STREAM_CODEC, SendPlayerResearchDataChangeS2C::handle);
        NetworkHelper.registerS2C(SendResearchesS2C.TYPE, SendResearchesS2C.STREAM_CODEC, SendResearchesS2C::handle);
        NetworkHelper.registerS2C(SendOpenResearchScreenS2C.TYPE, SendOpenResearchScreenS2C.STREAM_CODEC, SendOpenResearchScreenS2C::handle);
        NetworkHelper.registerS2C(SendCompleteResearchingS2C.TYPE, SendCompleteResearchingS2C.STREAM_CODEC, SendCompleteResearchingS2C::handle);

        NetworkHelper.registerC2S(SendStartResearchC2S.TYPE, SendStartResearchC2S.STREAM_CODEC, SendStartResearchC2S::handle);
    }

    public static ResourceLocation nameOf(String name) {
        return ResourceLocation.tryBuild(ResearchTree.MODID, name);
    }
}
