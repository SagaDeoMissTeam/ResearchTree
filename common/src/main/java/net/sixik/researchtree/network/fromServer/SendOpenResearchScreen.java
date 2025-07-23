package net.sixik.researchtree.network.fromServer;

import dev.architectury.networking.NetworkManager;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.sixik.researchtree.network.ResearchTreeNetwork;
import net.sixik.researchtree.utils.ResearchUtils;

public record SendOpenResearchScreen(boolean isOpen) implements CustomPacketPayload {

    public static final Type<SendOpenResearchScreen> TYPE =
            new Type<>(ResearchTreeNetwork.nameOf("send_open_research_screen"));

    public static final StreamCodec<FriendlyByteBuf, SendOpenResearchScreen> STREAM_CODEC =
            StreamCodec.composite(ByteBufCodecs.BOOL, SendOpenResearchScreen::isOpen, SendOpenResearchScreen::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(SendOpenResearchScreen message, NetworkManager.PacketContext context) {
        ResearchUtils.openResearchScreen(message.isOpen());
    }
}
