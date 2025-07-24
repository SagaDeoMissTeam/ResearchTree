package net.sixik.researchtree.network.fromServer;

import dev.architectury.networking.NetworkManager;
import dev.ftb.mods.ftblibrary.ui.ScreenWrapper;
import dev.ftb.mods.ftblibrary.util.NetworkHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.sixik.researchtree.client.ResearchScreen;
import net.sixik.researchtree.network.ResearchTreeNetwork;
import net.sixik.researchtree.research.ResearchChangeType;
import net.sixik.researchtree.research.manager.ClientResearchManager;
import net.sixik.researchtree.research.manager.PlayerResearchData;
import net.sixik.researchtree.utils.ResearchUtils;

public record SendCompleteResearchingS2C(ResourceLocation researchId) implements CustomPacketPayload {

    public static void sendTo(ServerPlayer player, ResourceLocation researchId) {
        NetworkHelper.sendTo(player, new SendCompleteResearchingS2C(researchId));
    }

    public static final Type<SendCompleteResearchingS2C> TYPE = new Type<>(
            ResearchTreeNetwork.nameOf("send_complete_researching"));

    public static final StreamCodec<FriendlyByteBuf, SendCompleteResearchingS2C> STREAM_CODEC = StreamCodec.composite(
            ResourceLocation.STREAM_CODEC, SendCompleteResearchingS2C::researchId, SendCompleteResearchingS2C::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(SendCompleteResearchingS2C message, NetworkManager.PacketContext context) {
        ClientResearchManager manager = ResearchUtils.getManagerCast(true);
        PlayerResearchData playerData = manager.getPlayerData();
        playerData.removeProgressResearch(message.researchId);
        playerData.addUnlockedResearch(message.researchId);

        if(Minecraft.getInstance().screen instanceof ScreenWrapper wrapper && wrapper.getGui() instanceof ResearchScreen researchScreen) {
            researchScreen.onResearchChange(message.researchId, ResearchChangeType.ADD_RESEARCH);
        }
    }
}
