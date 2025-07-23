package net.sixik.researchtree.network.ask;

import dev.architectury.networking.NetworkManager;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.sixik.researchtree.network.ResearchTreeNetwork;
import net.sixik.researchtree.research.BaseResearch;
import net.sixik.researchtree.research.ResearchData;
import net.sixik.researchtree.research.manager.ClientResearchManager;
import net.sixik.researchtree.research.manager.ServerResearchManager;
import net.sixik.researchtree.utils.ResearchUtils;
import net.sixik.sdmeconomy.api.AbstractASKRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class SyncAndOpenResearchScreenASK extends AbstractASKRequest {

    private static final Logger LOGGER = LoggerFactory.getLogger(SyncAndOpenResearchScreenASK.class);

    public static final String STAGE_KEY = "current_stage";
    public static final String RESEARCH_DATA_ID_KEY = "research_data_id";
    public static final String DATA_KEY = "content_data";
    private static final String SYNC_KEY = "sync_size";

    public static final int CLEAR_STAGE = 1;
    public static final int SEND_DATA_STAGE = 2;
    public static final int OPEN_STAGE = 3;

    public SyncAndOpenResearchScreenASK(Void empty) {
        super(empty);
    }

    public void startRequest(ServerPlayer player, ResourceLocation researchDataId) {
        executePerSend(() -> task(researchDataId), player);
    }

    public void startRequest(MinecraftServer server, ResourceLocation researchDataId) {
        executePerSend(() -> task(researchDataId), server);
    }

    protected CompoundTag[] task(ResourceLocation researchDataId) {
        CompoundTag nbt = new CompoundTag();
        nbt.putInt(STAGE_KEY, CLEAR_STAGE);
        nbt.putString(RESEARCH_DATA_ID_KEY, researchDataId.toString());
        return new CompoundTag[] { nbt };
    }

    @Override
    public void onServerTakeRequest(CompoundTag compoundTag, NetworkManager.PacketContext packetContext) {
        if(!hasStageKey(compoundTag)) {
            logError("Missing stage key in server request");
            return;
        }

        if (!(packetContext.getPlayer() instanceof ServerPlayer player)) {
            logError("Invalid player in server request");
            return;
        }

        ResourceLocation researchDataId = ResourceLocation.tryParse(compoundTag.getString(RESEARCH_DATA_ID_KEY));
        CompoundTag responseData = new CompoundTag();
        responseData.merge(compoundTag);

        if(hasStage(compoundTag, CLEAR_STAGE))
            handleClearStage(player, researchDataId, compoundTag, responseData, false);
        else if(hasStage(compoundTag, SEND_DATA_STAGE))
            handleSendDataStage(player, researchDataId, compoundTag, responseData, false);
        else if(hasStage(compoundTag, OPEN_STAGE)) {
            handleOpenStage(player, researchDataId, compoundTag, responseData, false);
        } else logError("Unknown stage: " + getStage(compoundTag));

    }

    @Override
    public void onClientTakeRequest(CompoundTag compoundTag, NetworkManager.PacketContext packetContext) {
        if (!compoundTag.contains(STAGE_KEY)) {
            logError("Missing stage key in client request");
            return;
        }

        Player player = packetContext.getPlayer();
        ResourceLocation researchDataId = ResourceLocation.tryParse(compoundTag.getString(RESEARCH_DATA_ID_KEY));
        CompoundTag responseData = compoundTag.copy();

        if(hasStage(compoundTag, CLEAR_STAGE))
            handleClearStage(player, researchDataId, compoundTag, responseData, true);
        else if(hasStage(compoundTag, SEND_DATA_STAGE))
            handleSendDataStage(player, researchDataId, compoundTag, responseData, true);
        else if(hasStage(compoundTag, OPEN_STAGE)) {
            handleOpenStage(player, researchDataId, compoundTag, responseData, true);
        } else logError("Unknown stage: " + getStage(compoundTag));

        Minecraft.getInstance().execute(() -> sendToServer(responseData));
    }

    protected void handleClearStage(Player player, ResourceLocation researchDataId, CompoundTag data, CompoundTag responseData, boolean isClient) {
        if(isClient) {
            ClientResearchManager clientResearchManager = ResearchUtils.getManagerCast(true);
            clientResearchManager.setResearchData(new ResearchData(researchDataId));
        } else {
            ServerResearchManager serverResearchManager = ResearchUtils.getManagerCast(false);
            Optional<ResearchData> optData = serverResearchManager.getResearchData(researchDataId);
            if(optData.isEmpty()) {
                logError("Can't found ResearchData [" + researchDataId + "]");
                return;
            }

            responseData.putInt(STAGE_KEY, SEND_DATA_STAGE);

            ResearchData researchData = optData.get();

            List<List<BaseResearch>> splitData = researchData.splitResearchToPackets();
            List<CompoundTag> packetsToSend = new ArrayList<>();

            int remainingPackets = splitData.size();
            for (List<BaseResearch> splitDatum : splitData) {
                CompoundTag packetData = new CompoundTag();
                packetData.merge(responseData);

                ListTag listTag = new ListTag();
                for (BaseResearch baseResearch : splitDatum) {
                    listTag.add(baseResearch.codec().encodeStart(NbtOps.INSTANCE, baseResearch).getOrThrow());
                }
                packetData.put(DATA_KEY, listTag);
                packetData.putInt(SYNC_KEY, --remainingPackets);
                packetsToSend.add(packetData);
            }

            if(packetsToSend.isEmpty()) {
                CompoundTag packetData = new CompoundTag();
                packetData.merge(responseData);
                packetData.putInt(SYNC_KEY, 0);
                packetsToSend.add(packetData);
            }

            sendTo((ServerPlayer) player, packetsToSend.toArray(new CompoundTag[0]));
        }
    }

    protected void handleSendDataStage(Player player, ResourceLocation resourceLocation, CompoundTag data, CompoundTag responseData, boolean isClient) {
        if(isClient) {
            if (!data.contains(DATA_KEY)) {
                logWarn("Missing data key in SEND_DATA_STAGE");
                return;
            }

            ClientResearchManager clientResearchManager = ResearchUtils.getManagerCast(true);
            ResearchData researchData = clientResearchManager.getResearchDataOrDefault(new ResearchData());


            ListTag researchDataNbt = (ListTag) data.get(DATA_KEY);
            for (Tag tag : researchDataNbt) {
                researchData.addResearch(BaseResearch.CODEC.decode(NbtOps.INSTANCE, tag).getOrThrow().getFirst());
            }
        } else {
            if(!data.contains(SYNC_KEY) || data.getInt(SYNC_KEY) <= 0) {
                writeStage(responseData, OPEN_STAGE);
                sendTo((ServerPlayer) player, responseData);
            }
        }
    }

    protected void handleOpenStage(Player player, ResourceLocation resourceLocation, CompoundTag data, CompoundTag responseData, boolean isClient) {
        if(isClient) {
            ResearchUtils.openResearchScreen(true);
        }
    }

    protected void writeStage(CompoundTag compoundTag, int stage) {
        compoundTag.putInt(STAGE_KEY, stage);
    }

    protected boolean hasStageKey(CompoundTag compoundTag) {
        return compoundTag.contains(STAGE_KEY);
    }

    protected boolean hasStage(CompoundTag compoundTag, int stage) {
        return hasStageKey(compoundTag) && getStage(compoundTag) == stage;
    }

    protected int getStage(CompoundTag compoundTag) {
        return compoundTag.getInt(STAGE_KEY);
    }

    @Override
    public String getId() {
        return ResearchTreeNetwork.SYNC_AND_OPEN_RESEARCH_SCREEN;
    }

    private void logError(String message) {
        LOGGER.error("SyncAndOpenShopASK: {}", message);
    }

    private void logWarn(String message) {
        LOGGER.warn("SyncAndOpenShopASK: {}", message);
    }
}
