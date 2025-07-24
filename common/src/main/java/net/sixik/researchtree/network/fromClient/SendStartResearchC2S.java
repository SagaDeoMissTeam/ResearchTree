package net.sixik.researchtree.network.fromClient;

import dev.architectury.networking.NetworkManager;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.sixik.researchtree.client.debug.ClientDebugUtils;
import net.sixik.researchtree.network.ResearchTreeNetwork;
import net.sixik.researchtree.research.BaseResearch;
import net.sixik.researchtree.research.ResearchData;
import net.sixik.researchtree.research.manager.ServerResearchManager;
import net.sixik.researchtree.utils.ResearchUtils;

import java.util.Optional;

public record SendStartResearchC2S(ResourceLocation researchId, ResourceLocation researchDataId) implements CustomPacketPayload {

    public static void send(ResourceLocation researchId, ResourceLocation researchDataId) {
        NetworkManager.sendToServer(new SendStartResearchC2S(researchId, researchDataId));
    }

    public static final Type<SendStartResearchC2S> TYPE =
            new Type<>(ResearchTreeNetwork.nameOf("send_start_research"));

    public static final StreamCodec<FriendlyByteBuf, SendStartResearchC2S> STREAM_CODEC = StreamCodec.composite(
                    ResourceLocation.STREAM_CODEC, SendStartResearchC2S::researchId,
                    ResourceLocation.STREAM_CODEC, SendStartResearchC2S::researchDataId,
                    SendStartResearchC2S::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(SendStartResearchC2S message, NetworkManager.PacketContext context) {
        ServerResearchManager manager = ResearchUtils.getManagerCast(false);
        Optional<ResearchData> researchDataOpt = manager.getResearchData(message.researchDataId);

        if(researchDataOpt.isEmpty()) {
            ClientDebugUtils.log("Research Data [{}] Not Found!", message.researchDataId);
            return;
        }
        ResearchData researchData = researchDataOpt.get();
        Optional<BaseResearch> researchBaseOnServer = researchData.getResearchList().stream().filter(data -> data.getId().equals(message.researchId)).findFirst();
        if(researchBaseOnServer.isEmpty()) {
            ClientDebugUtils.log("Research [{}] Not found!", message.researchId);
            return;
        }

        BaseResearch baseResearch = researchBaseOnServer.get();
        if(!ResearchUtils.canStartResearch(context.getPlayer(), baseResearch, false)) {
            ClientDebugUtils.log("Can't start Research [{}]", message.researchId);
            return;
        }

        baseResearch.onResearchStart(context.getPlayer());
    }
}
