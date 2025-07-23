package net.sixik.researchtree.network.fromServer;

import dev.architectury.networking.NetworkManager;
import dev.ftb.mods.ftblibrary.util.NetworkHelper;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.sixik.researchtree.network.ResearchTreeNetwork;
import net.sixik.researchtree.research.manager.PlayerResearchData;
import net.sixik.researchtree.utils.ResearchUtils;
import org.jetbrains.annotations.NotNull;

public record SendPlayerResearchDataS2C(PlayerResearchData researchData) implements CustomPacketPayload {

    public static final Type<SendPlayerResearchDataS2C> TYPE =
            new Type<>(ResearchTreeNetwork.nameOf("send_player_research_data"));

    public static final StreamCodec<FriendlyByteBuf, SendPlayerResearchDataS2C> STREAM_CODEC =
            StreamCodec.composite(PlayerResearchData.STREAM_CODEC, SendPlayerResearchDataS2C::researchData, SendPlayerResearchDataS2C::new);

    public static void sendTo(ServerPlayer player) {
        NetworkHelper.sendTo(player, new SendPlayerResearchDataS2C(ResearchUtils.getManager(false).getOrCreatePlayerData(player)));
    }

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(SendPlayerResearchDataS2C message, NetworkManager.PacketContext context) {
        ResearchUtils.getManager(true).getOrCreatePlayerData(context.getPlayer()).replace(message.researchData);
    }
}
