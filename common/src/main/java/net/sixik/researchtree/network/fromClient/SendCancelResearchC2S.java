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

public record SendCancelResearchC2S(ResourceLocation researchId, ResourceLocation researchDataId) implements CustomPacketPayload {

    public static final Type<SendCancelResearchC2S> TYPE =
            new Type<>(ResearchTreeNetwork.nameOf("send_cancel_research"));

    public static final StreamCodec<FriendlyByteBuf, SendCancelResearchC2S> STREAM_CODEC = StreamCodec.composite(
            ResourceLocation.STREAM_CODEC, SendCancelResearchC2S::researchId,
            ResourceLocation.STREAM_CODEC, SendCancelResearchC2S::researchDataId,
            SendCancelResearchC2S::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(SendCancelResearchC2S message, NetworkManager.PacketContext context) {
        ServerResearchManager manager = ResearchUtils.getManagerCast(false);
        Optional<ResearchData> researchDataOpt = manager.getResearchData(message.researchDataId);

        if(researchDataOpt.isEmpty()) {
            ClientDebugUtils.log("Research Data [%s%] Not Found!", message.researchDataId);
            return;
        }
        ResearchData researchData = researchDataOpt.get();
        Optional<BaseResearch> researchBaseOnServer = researchData.getResearchList().stream().filter(data -> data.getId().equals(message.researchId)).findFirst();
        if(researchBaseOnServer.isEmpty()) {
            ClientDebugUtils.log("Research [%s%] Not found!", message.researchId);
            return;
        }

        BaseResearch baseResearch = researchBaseOnServer.get();
        baseResearch.onResearchCancel(context.getPlayer());
    }
}
