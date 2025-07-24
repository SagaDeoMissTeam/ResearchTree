package net.sixik.researchtree.network.fromClient;

import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public class SendCancelResearchC2S implements CustomPacketPayload {
    @Override
    public Type<? extends CustomPacketPayload> type() {
        return null;
    }
}
