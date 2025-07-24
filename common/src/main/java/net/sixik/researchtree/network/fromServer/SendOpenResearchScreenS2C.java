package net.sixik.researchtree.network.fromServer;

import dev.architectury.networking.NetworkManager;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.sixik.researchtree.network.ResearchTreeNetwork;
import net.sixik.researchtree.utils.ResearchUtils;

public record SendOpenResearchScreenS2C(boolean isOpen) implements CustomPacketPayload {

    public static final Type<SendOpenResearchScreenS2C> TYPE =
            new Type<>(ResearchTreeNetwork.nameOf("send_open_research_screen"));

    public static final StreamCodec<FriendlyByteBuf, SendOpenResearchScreenS2C> STREAM_CODEC =
            StreamCodec.composite(ByteBufCodecs.BOOL, SendOpenResearchScreenS2C::isOpen, SendOpenResearchScreenS2C::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(SendOpenResearchScreenS2C message, NetworkManager.PacketContext context) {
        ResearchUtils.openResearchScreen(message.isOpen());
    }
}
