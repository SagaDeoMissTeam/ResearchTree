package net.sixik.researchtree.network.fromServer;

import dev.architectury.networking.NetworkManager;
import dev.ftb.mods.ftblibrary.ui.ScreenWrapper;
import dev.ftb.mods.ftblibrary.util.NetworkHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.sixik.researchtree.client.ResearchScreen;
import net.sixik.researchtree.network.ResearchTreeNetwork;
import net.sixik.researchtree.research.ResearchChangeType;
import net.sixik.researchtree.research.manager.ClientResearchManager;
import net.sixik.researchtree.utils.ResearchUtils;

public class SendPlayerResearchDataChangeS2C implements CustomPacketPayload {

    public static void sendAddResearch(ServerPlayer player, ResourceLocation researchId) {
        NetworkHelper.sendTo(player, new SendPlayerResearchDataChangeS2C(researchId, ResearchChangeType.ADD_RESEARCH, new CompoundTag()));
    }

    public static void sendRemoveResearch(ServerPlayer player, ResourceLocation researchId) {
        NetworkHelper.sendTo(player, new SendPlayerResearchDataChangeS2C(researchId, ResearchChangeType.REMOVE_RESEARCH, new CompoundTag()));
    }

    public static void sendAddProgress(ServerPlayer player, ResourceLocation researchId, long time) {
        NetworkHelper.sendTo(player, new SendPlayerResearchDataChangeS2C(researchId, ResearchChangeType.ADD_PROGRESS, createProgressData(time)));
    }

    public static void sendRemoveProgress(ServerPlayer player, ResourceLocation researchId) {
        NetworkHelper.sendTo(player, new SendPlayerResearchDataChangeS2C(researchId, ResearchChangeType.REMOVE_PROGRESS, new CompoundTag()));
    }

    public static void sendChangeProgress(ServerPlayer player, ResourceLocation researchId, long time) {
        NetworkHelper.sendTo(player, new SendPlayerResearchDataChangeS2C(researchId, ResearchChangeType.CHANGE_PROGRESS, createProgressData(time)));
    }

    protected static CompoundTag createProgressData(long time) {
        CompoundTag nbt = new CompoundTag();
        nbt.putLong(PROGRESS_TIME_KEY, time);
        return nbt;
    }


    public static final String PROGRESS_TIME_KEY = "progress_time";

    public static final Type<SendPlayerResearchDataChangeS2C> TYPE =
            new Type<>(ResearchTreeNetwork.nameOf("send_change_player_data"));

    public static final StreamCodec<FriendlyByteBuf, SendPlayerResearchDataChangeS2C> STREAM_CODEC =
            StreamCodec.composite(
                    ResourceLocation.STREAM_CODEC, SendPlayerResearchDataChangeS2C::getResearchId,
                    ByteBufCodecs.INT, SendPlayerResearchDataChangeS2C::getType,
                    ByteBufCodecs.COMPOUND_TAG, SendPlayerResearchDataChangeS2C::getNbt,
                    SendPlayerResearchDataChangeS2C::new);

    private final ResourceLocation researchId;
    private final ResearchChangeType type;
    private final CompoundTag nbt;

    public SendPlayerResearchDataChangeS2C(ResourceLocation researchId, int type, CompoundTag nbt) {
        this(researchId, ResearchChangeType.values()[type], nbt);
    }

    public SendPlayerResearchDataChangeS2C(ResourceLocation researchId, ResearchChangeType type, CompoundTag nbt) {
        this.researchId = researchId;
        this.type = type;
        this.nbt = nbt;
    }

    public ResourceLocation getResearchId() {
        return researchId;
    }

    public int getType() {
        return type.ordinal();
    }

    public CompoundTag getNbt() {
        return nbt;
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(SendPlayerResearchDataChangeS2C message, NetworkManager.PacketContext context) {
        ClientResearchManager researchManager = ResearchUtils.getManagerCast(true);
        switch (message.type) {
            case ADD_RESEARCH -> {
                researchManager.getPlayerData().addUnlockedResearch(message.researchId);
            }
            case REMOVE_RESEARCH -> {
                researchManager.getPlayerData().removeUnlockedResearch(message.researchId);
            }
            case ADD_PROGRESS -> {
                researchManager.getPlayerData().addProgressResearch(message.researchId, message.getNbt().getLong(PROGRESS_TIME_KEY));
            }
            case CHANGE_PROGRESS -> {
                researchManager.getPlayerData().getProgressResearch(message.researchId).ifPresent(researchProgressData -> {
                    researchProgressData.setDurationMs(message.getNbt().getLong(PROGRESS_TIME_KEY));
                });
            }
            case REMOVE_PROGRESS -> {
                researchManager.getPlayerData().removeProgressResearch(message.researchId);
            }
        }

        if(Minecraft.getInstance().screen instanceof ScreenWrapper wrapper && wrapper.getGui() instanceof ResearchScreen researchScreen) {
            researchScreen.onResearchChange(message.researchId, message.type);
        }
    }
}
