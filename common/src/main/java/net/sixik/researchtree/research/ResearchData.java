package net.sixik.researchtree.research;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.sixik.researchtree.ResearchTree;
import net.sixik.researchtree.api.FullCodecSerializer;
import net.sixik.researchtree.network.fromServer.SendResearchesS2C;
import net.sixik.researchtree.research.listener.ResearchEvent;
import net.sixik.researchtree.research.listener.ResearchEventType;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class ResearchData implements FullCodecSerializer<ResearchData> {

    public static final ResourceLocation DEFAULT = ResourceLocation.tryBuild(ResearchTree.MODID, "default");
    public static final Function<String, ResourceLocation> ADDITION_DEFAULT = (s) -> ResourceLocation.tryBuild(ResearchTree.MODID, "default_" + s);
    public static final int MaxResearchesSendByNetwork = 20;

    public static final Codec<ResearchData> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    ResourceLocation.CODEC.fieldOf("id").forGetter(ResearchData::getId),
                    BaseResearch.CODEC.listOf().fieldOf("researches").forGetter(ResearchData::getResearchList)
            ).apply(instance, ResearchData::new));

    public static final StreamCodec<FriendlyByteBuf, ResearchData> STREAM_CODEC = StreamCodec.composite(
            ResourceLocation.STREAM_CODEC, ResearchData::getId,
            BaseResearch.STREAM_CODEC.apply(ByteBufCodecs.list()), ResearchData::getResearchList,
            ResearchData::new
    );

    protected final ResourceLocation id;
    protected List<BaseResearch> researchList = new ArrayList<>();
    protected List<ResearchEvent> listeners = new ArrayList<>();

    public ResearchData() {
        this(ADDITION_DEFAULT.apply(UUID.randomUUID().toString()));
    }

    public ResearchData(final ResourceLocation id) {
        this(id, new ArrayList<>());
    }

    public ResearchData(final ResourceLocation id, List<BaseResearch> list) {
        this.id = id;
        this.researchList = list;
    }

    public ResourceLocation getId() {
        return id;
    }

    public List<BaseResearch> getResearchList() {
        return researchList;
    }

    public void addResearch(BaseResearch research) {
        research.setResearchData(this);
        researchList.add(research);
        for (ResearchEvent listener : listeners) {
            listener.onResearchEvent(research, ResearchEventType.ADD);
        }
    }

    public void addResearch(Collection<BaseResearch> researchesCollection) {
        researchesCollection.forEach(this::addResearch);
    }

    public boolean containsResearch(BaseResearch research) {
        return researchList.contains(research);
    }

    public boolean removeResearch(BaseResearch research) {
        if(researchList.remove(research)) {
            for (ResearchEvent listener : listeners) {
                listener.onResearchEvent(research, ResearchEventType.REMOVE);
            }
            return true;
        }

        return false;
    }

    public boolean changeResearch(Function<BaseResearch, Boolean> researchConsumer) {
        for (BaseResearch baseResearch : researchList) {
            if(researchConsumer.apply(baseResearch)) {
                triggerChangeEvent(baseResearch);
                return true;
            }
        }

        return false;
    }

    public List<BaseResearch> findResearches(Collection<ResourceLocation> ids) {
        return getResearchList().stream().filter(research ->  ids.contains(research.getId())).toList();
    }

    public void triggerChangeEvent(BaseResearch research) {
        for (ResearchEvent listener : listeners) {
            listener.onResearchEvent(research, ResearchEventType.CHANGE);
        }
    }

    @Override
    public Codec<ResearchData> codec() {
        return CODEC;
    }

    @Override
    public StreamCodec<FriendlyByteBuf, ResearchData> streamCodec() {
        return STREAM_CODEC;
    }

    public void sendTo(ServerPlayer player) {
        for (List<BaseResearch> splitResearchToPacket : splitResearchToPackets()) {
            SendResearchesS2C.sendTo(player, splitResearchToPacket);
        }
    }

    public void sendToAll(MinecraftServer server) {
        for (List<BaseResearch> splitResearchToPacket : splitResearchToPackets()) {
            SendResearchesS2C.sendToAll(server, splitResearchToPacket);
        }
    }

    public List<List<BaseResearch>> splitResearchToPackets() {
        return splitResearchToPackets(MaxResearchesSendByNetwork);
    }

    public List<List<BaseResearch>> splitResearchToPackets(int countInPacket) {
        if (countInPacket <= 0) {
            throw new IllegalArgumentException("countInPacket must be positive");
        }

        List<BaseResearch> list = getResearchList();
        return IntStream.range(0, (list.size() + countInPacket - 1) / countInPacket)
                .mapToObj(i -> list.subList(i * countInPacket, Math.min((i + 1) * countInPacket, list.size())))
                .map(ArrayList::new)
                .collect(Collectors.toList());
    }

}
