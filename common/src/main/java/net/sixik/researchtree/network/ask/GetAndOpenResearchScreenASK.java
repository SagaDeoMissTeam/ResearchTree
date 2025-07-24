package net.sixik.researchtree.network.ask;

import dev.architectury.networking.NetworkManager;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.sixik.researchtree.ResearchTree;
import net.sixik.researchtree.network.ResearchTreeNetwork;
import net.sixik.researchtree.research.ResearchData;
import net.sixik.researchtree.research.manager.ServerResearchManager;
import net.sixik.researchtree.utils.ResearchUtils;
import net.sixik.sdmeconomy.api.AbstractASKRequest;

import java.util.Optional;

public class GetAndOpenResearchScreenASK extends AbstractASKRequest {

    private static final String RESEARCH_TREE_ID_KEY = "research_tree_id";
    private static final String RESEARCH_TREE_ID_KEY_RESOURCE = "research_tree_id_resource";

    public GetAndOpenResearchScreenASK(Void empty) {
        super(empty);
    }

    @Override
    public void onServerTakeRequest(CompoundTag compoundTag, NetworkManager.PacketContext packetContext) {
        if(!ResearchTree.MOD_CONFIG.getEnableKeyBinding()) return;

        if(compoundTag.contains(RESEARCH_TREE_ID_KEY))
            new SyncAndOpenResearchScreenASK(null).startRequest((ServerPlayer) packetContext.getPlayer(), ResourceLocation.tryParse(compoundTag.getString(RESEARCH_TREE_ID_KEY)));
        else {
            ResourceLocation id = ResourceLocation.tryParse(compoundTag.getString(RESEARCH_TREE_ID_KEY_RESOURCE));

            ServerResearchManager manager = ResearchUtils.getManagerCast(false);
            Optional<ResearchData> opt = manager.getResearchData(id);
            if(opt.isEmpty()) {
                packetContext.getPlayer().sendSystemMessage(Component.literal("Can't open ResearchTree. Because not found! [" + id + "]").withStyle(ChatFormatting.RED));
            } else {
                new SyncAndOpenResearchScreenASK(null).startRequest((ServerPlayer) packetContext.getPlayer(), id);
            }
        }

    }

    @Override
    public void onClientTakeRequest(CompoundTag compoundTag, NetworkManager.PacketContext packetContext) {}

    public void execute(ResourceLocation researchDataId) {
        sendToServer(task(researchDataId));
    }

    protected CompoundTag task(ResourceLocation researchDataId) {
        CompoundTag nbt = new CompoundTag();
        nbt.putString(RESEARCH_TREE_ID_KEY, researchDataId.toString());
        return nbt;
    }


    @Override
    public String getId() {
        return ResearchTreeNetwork.GET_AND_OPEN_RESEARCH_SCREEN;
    }
}
