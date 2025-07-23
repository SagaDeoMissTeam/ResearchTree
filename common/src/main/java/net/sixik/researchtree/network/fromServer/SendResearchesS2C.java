package net.sixik.researchtree.network.fromServer;

import dev.architectury.networking.NetworkManager;
import dev.ftb.mods.ftblibrary.util.NetworkHelper;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.sixik.researchtree.network.ResearchTreeNetwork;
import net.sixik.researchtree.research.BaseResearch;
import net.sixik.researchtree.research.ResearchData;
import net.sixik.researchtree.research.manager.ClientResearchManager;
import net.sixik.researchtree.utils.ResearchUtils;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public record SendResearchesS2C(List<BaseResearch> researchList) implements CustomPacketPayload {

    public static final Type<SendResearchesS2C> TYPE =
            new Type<>(ResearchTreeNetwork.nameOf("send_researches"));

    public static final StreamCodec<FriendlyByteBuf, SendResearchesS2C> STREAM_CODEC =
            StreamCodec.composite(BaseResearch.STREAM_CODEC.apply(ByteBufCodecs.list()), SendResearchesS2C::researchList, SendResearchesS2C::new);

    public static void sendTo(ServerPlayer player, List<BaseResearch> researchList) {
        NetworkHelper.sendTo(player, new SendResearchesS2C(researchList));
    }

    public static void sendToAll(MinecraftServer server, List<BaseResearch> researchList) {
        NetworkHelper.sendToAll(server, new SendResearchesS2C(researchList));
    }

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(SendResearchesS2C message, NetworkManager.PacketContext context) {
        ClientResearchManager manager = ResearchUtils.getManagerCast(true);
        ResearchData optionalResearchData = manager.getResearchDataOrDefault(new ResearchData());
        optionalResearchData.addResearch(message.researchList);
    }
}
