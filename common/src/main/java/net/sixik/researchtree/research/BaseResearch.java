package net.sixik.researchtree.research;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.sixik.researchtree.ResearchTree;
import net.sixik.researchtree.api.FullCodecSerializer;
import net.sixik.researchtree.client.ClientUtils;
import net.sixik.researchtree.network.fromServer.SendCompleteResearchingS2C;
import net.sixik.researchtree.network.fromServer.SendPlayerResearchDataChangeS2C;
import net.sixik.researchtree.registers.ModRegisters;
import net.sixik.researchtree.research.functions.BaseFunction;
import net.sixik.researchtree.research.manager.PlayerResearchData;
import net.sixik.researchtree.research.manager.ServerResearchManager;
import net.sixik.researchtree.research.requirements.Requirements;
import net.sixik.researchtree.research.rewards.Reward;
import net.sixik.researchtree.research.triggers.BaseTrigger;
import net.sixik.researchtree.utils.NbtUtils;
import net.sixik.researchtree.utils.ResearchUtils;
import net.sixik.researchtree.utils.TextUtils;
import org.jetbrains.annotations.Nullable;
import oshi.util.tuples.Pair;

import java.util.*;
import java.util.stream.Collectors;

public class BaseResearch implements FullCodecSerializer<BaseResearch> {

    public static final Codec<BaseResearch> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    ResourceLocation.CODEC.fieldOf("id").forGetter(BaseResearch::getId),
                    ResourceLocation.CODEC.fieldOf("icon").forGetter(BaseResearch::getIconPath),
                    Codec.BOOL.fieldOf("shouldRenderConnection").forGetter(BaseResearch::isShouldRenderConnection),
                    CompoundTag.CODEC.fieldOf("data").forGetter(BaseResearch::serialize)
            ).apply(instance, BaseResearch::new));

    public static final StreamCodec<FriendlyByteBuf, BaseResearch> STREAM_CODEC = StreamCodec.composite(
            ResourceLocation.STREAM_CODEC, BaseResearch::getId,
            ResourceLocation.STREAM_CODEC, BaseResearch::getIconPath,
            ByteBufCodecs.BOOL, BaseResearch::isShouldRenderConnection,
            ByteBufCodecs.COMPOUND_TAG, BaseResearch::serialize,
            BaseResearch::new
    );

    public static final String PARENTS_KEY = "parents_list";
    public static final String REQUIREMENTS_KEY = "requirements_list";
    public static final String REWARDS_KEY = "rewards_list";
    public static final String DESCRIPTION_KEY = "descriptions_list";

    public boolean shouldRenderConnection = true;

    protected final ResourceLocation id;
    protected ResourceLocation iconPath;

    protected List<ResourceLocation> parentResearch = new ArrayList<>();
    protected HashSet<Requirements> requirements = new HashSet<>();
    protected HashSet<Reward> rewards = new HashSet<>();
    protected List<String> descriptions = new ArrayList<>();

    public boolean researchStopping = true;
    public long researchTime = -1L;
    public double refundPercent = -1;
    public int countParentsToResearch = 0;
    public ResearchShowType showType = ResearchShowType.SHOW;
    public ResearchHideTypeRender hideTypeRender = ResearchHideTypeRender.NON_STYLE;

    private final String cachedTranslate;
    private HashSet<Requirements> cachedRequirements = new HashSet<>();
    private List<Component> cachedDescriptions = new ArrayList<>();
    private List<BaseResearch> cachedParents = new ArrayList<>();
    private List<BaseFunction> onStartFunctions = new ArrayList<>();
    private List<BaseFunction> onEndFunctions = new ArrayList<>();
    private List<BaseTrigger> baseTriggers = new ArrayList<>();

    private boolean cacheParentsDirty = true;

    public BaseResearch() {
        this(ResourceLocation.tryBuild(ResearchTree.MODID, "default"));
    }

    public BaseResearch(ResourceLocation id) {
        this(id, null);
    }

    private @Nullable ResearchData researchData;

    public BaseResearch(ResourceLocation id, @Nullable ResourceLocation iconPath) {
        this.id = id;

        if(iconPath == null) {
            this.iconPath = ResourceLocation.tryBuild(this.id.getNamespace(), "textures/icon/" + this.id.getPath() + ".png");


        } else this.iconPath = iconPath;


        String str = id.getPath();
        if(str.contains("/"))
            str = str.replace('/', '.');
        cachedTranslate = "research." + id.getNamespace() + "." + str;
    }

    private BaseResearch(ResourceLocation id, ResourceLocation iconPath, boolean shouldRenderConnection, CompoundTag compoundTag) {
        this(id, iconPath);
        this.shouldRenderConnection = shouldRenderConnection;
        deserialize(compoundTag);
    }

    public BaseResearch setResearchData(ResearchData data) {
        this.researchData = data;
        return this;
    }

    public void setFunctions(List<BaseFunction> start, List<BaseFunction> end) {
        onStartFunctions = start;
        onEndFunctions = end;
    }

    public boolean isShouldRenderConnection() {
        return shouldRenderConnection;
    }

    public Optional<ResearchData> getResearchData() {
        return Optional.ofNullable(researchData);
    }

    public BaseResearch addParent(ResourceLocation researchId) {
        this.parentResearch.add(researchId);
        this.cacheParentsDirty = true;
        return this;
    }

    public BaseResearch addParent(BaseResearch research) {
        this.parentResearch.add(research.getId());
        this.cacheParentsDirty = true;
        return this;
    }

    public boolean removeParent(BaseResearch research) {
        this.cacheParentsDirty = true;
        return this.parentResearch.remove(research.getId());
    }

    public BaseResearch addRequirements(Requirements requirements) {
        this.requirements.add(requirements);
        return this;
    }

    public boolean removeRequirements(Requirements requirements) {
        return this.requirements.remove(requirements);
    }

    public BaseResearch addReward(Reward reward) {
        this.rewards.add(reward);
        return this;
    }

    public boolean removeReward(Reward reward) {
        return this.rewards.remove(reward);
    }

    public List<ResourceLocation> getParentResearchesIds() {
        return parentResearch;
    }

    public List<BaseResearch> getParentResearch() {
        if(this.cacheParentsDirty) {
            cachedParents = researchData == null ? new ArrayList<>() : researchData.findResearches(parentResearch);
            cacheParentsDirty = false;
        }

        return cachedParents;
    }

    public boolean hasParents() {
        return !getParentResearch().isEmpty();
    }

    public ResourceLocation getIconPath() {
        return iconPath;
    }

    public ResourceLocation getId() {
        return id;
    }

    public Component getTranslate() {
        return Component.translatable(cachedTranslate);
    }

    public Component getSubtitleTranslate() {
        return Component.translatable(cachedTranslate + ".subtitle");
    }

    public Component getDescriptionTranslate() {
        return Component.translatable(cachedTranslate + ".description");
    }

    public int getCountParentsToResearch() {
        return Math.max(countParentsToResearch, getParentResearch().size());
    }

    public void setCountParentsToResearch(int countParentsToResearch) {
        this.countParentsToResearch = countParentsToResearch;
    }

    public boolean hasSubtitle() {
        String str = cachedTranslate + ".subtitle";
        return !Objects.equals(I18n.get(str), str);
    }

    public HashSet<Requirements> getRequirements() {
        return requirements;
    }

    public HashSet<Reward> getRewards() {
        return rewards;
    }

    public HashSet<Requirements> getRequirementsWithParents(Player player) {
        if (!cachedRequirements.isEmpty()) {
            return new HashSet<>(cachedRequirements);
        }

        cachedRequirements = collectRequirements(player);
        return new HashSet<>(cachedRequirements);
    }

    protected HashSet<Requirements> collectRequirements(Player player) {
        HashSet<Requirements> result = new HashSet<>();

        for (Requirements requirement : getRequirements()) {
            result.add(requirement.copy());
        }

        for (BaseResearch parentResearch : getParentResearch()) {
            for (Requirements parentReq : parentResearch.collectRequirements(player)) {
                boolean merged = false;

                for (Requirements existingReq : result) {
                    if (existingReq.canMathOperation(parentReq)) {
                        existingReq.plus(parentReq);
                        merged = true;
                        break;
                    }
                }

                if (!merged) {
                    result.add(parentReq.copy());
                }
            }
        }

        return result;
    }

    public void setDescriptionRaw(List<String> str) {
        this.descriptions = str;
        cachedDescriptions.clear();
    }

    public List<String> getDescriptionRaw() {
        return descriptions;
    }

    public List<Component> getDescription() {
        if(cachedDescriptions.isEmpty()) {
            cachedDescriptions = getDescriptionRaw().stream().map(str -> TextUtils.parseRawText(str, ClientUtils.holder())).collect(Collectors.toList());
        }

        return cachedDescriptions;
    }

    public void invalidateCache() {
        invalidateCacheRequirements();
        this.cachedDescriptions.clear();
        this.cacheParentsDirty = true;
    }

    public void invalidateCacheRequirements() {
        this.cachedRequirements.clear();
    }

    public boolean canStopResearch() {
        return researchStopping;
    }

    public long getResearchTime() {
        return researchTime;
    }

    public double getRefundPercent() {
        return refundPercent;
    }

    public void onResearchStart(Player player) {
        onStartFunctions.stream().filter(BaseFunction::isBeforeStage).forEach(s -> s.execute((ServerPlayer) player, this));

        ServerResearchManager manager = ResearchUtils.getManagerCast(false);
        if(!ResearchUtils.canStartResearch(player, this, manager)) return;

        List<Requirements> completed = new ArrayList<>();

        for (Requirements requirement : getRequirements()) {
            if(requirement.execute(player, this))
                completed.add(requirement);
            else {
                for (Requirements requirements1 : completed) {
                    requirements1.refund(player, this, 100);
                }
                return;
            }
        }

        PlayerResearchData playerData = manager.getOrCreatePlayerData(player);

        long researchTime = this.getResearchTime();

        if(researchTime == -1)
            researchTime = ResearchTree.MOD_CONFIG.getDefaultResearchTimeMs();

        if(researchTime <= 0) {
            this.onResearchEnd(player);
        } else {
            playerData.addProgressResearch(this.getId(), researchTime);

            if (player instanceof ServerPlayer serverPlayer) {
                SendPlayerResearchDataChangeS2C.sendAddProgress(serverPlayer, this.getId(), researchTime);
            }
        }
        onStartFunctions.stream().filter(BaseFunction::isAfterStage).forEach(s -> s.execute((ServerPlayer) player, this));
    }

    public void onResearchCancel(Player player) {
        ServerResearchManager manager = ResearchUtils.getManagerCast(false);
        manager.getPlayerDataOptional(player).ifPresent(playerData -> {

            double percentRefund = getRefundPercent();

            for (Requirements requirement : getRequirements()) {
                requirement.refund(player, this, percentRefund);
            }

            playerData.removeProgressResearch(this.getId());
            SendPlayerResearchDataChangeS2C.sendRemoveProgress((ServerPlayer) player, this.getId());
        });
    }

    public void onResearchEnd(Player player) {
        onResearchEnd(player, true, true);
    }

    public void onResearchEnd(Player player, boolean sendPacket, boolean team) {
        onEndFunctions.stream().filter(BaseFunction::isBeforeStage).forEach(s -> s.execute((ServerPlayer) player, this));
        ServerResearchManager manager = ResearchUtils.getManagerCast(false);
        manager.getPlayerDataOptional(player).ifPresent(playerData -> {

            if(playerData.containsInProgress(this.getId()))
                playerData.removeProgressResearch(this.getId());

            if(!playerData.containsInUnlockedResearch(this.getId())) {
                playerData.addUnlockedResearch(this.getId());
            } else return;

            for (Reward reward : getRewards()) {
                reward.giveReward(player, this);
            }

            final ServerPlayer serverPlayer = (ServerPlayer)player;

            if(team) {
                manager.invokeTeamManagers(teamManager -> {
                    Collection<ServerPlayer> onlinePlayers = teamManager.getTeamOnlineMembers(serverPlayer);
                    Collection<UUID> offlinePlayers = teamManager.getOfflineMembers(serverPlayer);

                    onlinePlayers.forEach(teamPlayer -> this.onResearchEnd(teamPlayer, true, false));

                    if(offlinePlayers.isEmpty()) return;
                    Optional<Pair<ResearchData, BaseResearch>> researchData = manager.findResearchAndDataById(this.getId());
                    if(researchData.isEmpty())
                        throw new RuntimeException("Can't find ResearchData for " + this.getId());

                    ResourceLocation researchDataId = researchData.get().getA().getId();
                    offlinePlayers.forEach(offlinePlayer -> manager.addOfflineData(offlinePlayer, researchDataId, this.getId()));
                });
            }

            sendNotify(player);

            if(sendPacket) SendCompleteResearchingS2C.sendTo((ServerPlayer) player, this.getId());
        });
        onEndFunctions.stream().filter(BaseFunction::isAfterStage).forEach(s -> s.execute((ServerPlayer) player, this));
    }

    public void sendNotify(Player player) {
        player.sendSystemMessage(Component.literal("Research Completed!"));
    }

    public boolean canResearch(Player player) {
        return true;
    }

    public boolean isLocked(Player player) {
        return false;
    }

    @Override
    public Codec<BaseResearch> codec() {
        return CODEC;
    }

    @Override
    public StreamCodec<FriendlyByteBuf, BaseResearch> streamCodec() {
        return STREAM_CODEC;
    }

    protected CompoundTag serialize() {
        CompoundTag nbt = new CompoundTag();

        NbtUtils.putList(nbt, REQUIREMENTS_KEY, getRequirements(), requirement -> {
            CompoundTag d1 = new CompoundTag();
            d1.put(ModRegisters.ID_KEY, Codec.STRING.write(NbtOps.INSTANCE, requirement.getId()));
            d1.put(ModRegisters.DATA_KEY, requirement.codec().encodeStart(NbtOps.INSTANCE, requirement).getOrThrow());
            return d1;
        });
        NbtUtils.putList(nbt, REWARDS_KEY, getRewards(), (reward) -> {
            CompoundTag d1 = new CompoundTag();
            d1.put(ModRegisters.ID_KEY, Codec.STRING.write(NbtOps.INSTANCE, reward.getId()));
            d1.put(ModRegisters.DATA_KEY,reward.codec().encodeStart(NbtOps.INSTANCE, reward).getOrThrow());
            return d1;
        });
        NbtUtils.putList(nbt, PARENTS_KEY, getParentResearchesIds(), (parentResearchesId) -> ResourceLocation.CODEC.encodeStart(NbtOps.INSTANCE, parentResearchesId).getOrThrow());
        NbtUtils.putList(nbt, DESCRIPTION_KEY, getDescriptionRaw(), StringTag::valueOf);

        nbt.putDouble("refund_dat", refundPercent);
        nbt.putLong("researchTime", researchTime);
        nbt.putBoolean("researchStopping", researchStopping);
        nbt.putInt("countParentsToResearch", countParentsToResearch);
        nbt.putInt("showType", showType.ordinal());
        nbt.putInt("hideTypeRender", hideTypeRender.ordinal());

        return nbt;
    }

    protected void deserialize(CompoundTag nbt) {
        invalidateCache();
        getRequirements().clear();
        getRewards().clear();
        getParentResearchesIds().clear();
        getDescription().clear();

        if(nbt.contains("refund_dat"))
            refundPercent = nbt.getDouble("refund_dat");
        if(nbt.contains("researchTime"))
            researchTime = nbt.getLong("researchTime");
        if(nbt.contains("researchStopping"))
            researchStopping = nbt.getBoolean("researchStopping");
        if(nbt.contains("countParentsToResearch"))
            countParentsToResearch = nbt.getInt("countParentsToResearch");
        if(nbt.contains("showType"))
            showType = ResearchShowType.values()[nbt.getInt("showType")];
        if(nbt.contains("hideTypeRender"))
            hideTypeRender = ResearchHideTypeRender.values()[nbt.getInt("hideTypeRender")];


        NbtUtils.getList(nbt, REQUIREMENTS_KEY, tag -> {
            CompoundTag d1 = (CompoundTag) (tag);
            String key = Codec.STRING.decode(NbtOps.INSTANCE, d1.get(ModRegisters.ID_KEY)).getOrThrow().getFirst();
            return ModRegisters.getRequirements(key).map(s -> s.decode(NbtOps.INSTANCE, d1.get(ModRegisters.DATA_KEY))).orElseThrow().getOrThrow().getFirst();
        }, getRequirements());

        NbtUtils.getListWithClear(nbt, REWARDS_KEY, tag -> {
            CompoundTag d1 = (CompoundTag) (tag);
            String key = Codec.STRING.decode(NbtOps.INSTANCE, d1.get(ModRegisters.ID_KEY)).getOrThrow().getFirst();
            return ModRegisters.getReward(key).map(s -> s.decode(NbtOps.INSTANCE, d1.get(ModRegisters.DATA_KEY))).orElseThrow().getOrThrow().getFirst();
        }, getRewards());

        NbtUtils.getListWithClear(nbt, PARENTS_KEY,
                tag -> ResourceLocation.CODEC.decode(NbtOps.INSTANCE, tag).getOrThrow().getFirst(),
                getParentResearchesIds()
        );

        NbtUtils.getListWithClear(nbt, DESCRIPTION_KEY, Tag::getAsString, getDescriptionRaw());
    }
}
