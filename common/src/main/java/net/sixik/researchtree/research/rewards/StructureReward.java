package net.sixik.researchtree.research.rewards;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.ftb.mods.ftblibrary.icon.ItemIcon;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.SectionPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.sixik.researchtree.research.BaseResearch;

import java.util.Optional;

public class StructureReward extends Reward{

    public static final Codec<StructureReward> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    ResourceLocation.CODEC.fieldOf("structureId").forGetter(StructureReward::getStructureId),
                    Codec.INT.fieldOf("offsetY").forGetter(StructureReward::getOffsetY)
            ).apply(instance, StructureReward::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, StructureReward> STREAM_CODEC = StreamCodec.composite(
            ResourceLocation.STREAM_CODEC, StructureReward::getStructureId, ByteBufCodecs.INT, StructureReward::getOffsetY, StructureReward::new
    );

    protected ResourceLocation structureId;
    protected int offsetY;

    public StructureReward(Void v) {
        super(v);
    }

    public StructureReward(ResourceLocation structureId) {
        this(structureId, 0);
    }

    public StructureReward(ResourceLocation structureId, int offsetY) {
        super(null);
        this.structureId = structureId;
        this.offsetY = offsetY;
    }

    public ResourceLocation getStructureId() {
        return structureId;
    }

    public int getOffsetY() {
        return offsetY;
    }

    @Override
    public void giveReward(Player player, BaseResearch research) {
        if(player instanceof ServerPlayer serverPlayer) {
            BlockPos blockPos = player.blockPosition();
            blockPos = new BlockPos(blockPos.getX(), blockPos.getY() + offsetY, blockPos.getZ());
            ServerLevel serverLevel = serverPlayer.serverLevel();
            Optional<Registry<Structure>> optStruct = serverLevel.registryAccess().registry(Registries.STRUCTURE);
            if(optStruct.isEmpty()) return;
            Structure structure = optStruct.get().get(structureId);
            if(structure == null) return;
            ChunkGenerator chunkGenerator = serverLevel.getChunkSource().getGenerator();
            StructureStart structureStart = structure.generate(serverLevel.registryAccess(), chunkGenerator, chunkGenerator.getBiomeSource(), serverLevel.getChunkSource().randomState(), serverLevel.getStructureManager(), serverLevel.getSeed(), new ChunkPos(blockPos), 0, serverLevel, (holder) -> true);
            if (!structureStart.isValid()) {
                throw new RuntimeException(I18n.get("commands.place.structure.failed"));
            } else {
                BoundingBox boundingBox = structureStart.getBoundingBox();
                ChunkPos chunkPos = new ChunkPos(SectionPos.blockToSectionCoord(boundingBox.minX()), SectionPos.blockToSectionCoord(boundingBox.minZ()));
                ChunkPos chunkPos2 = new ChunkPos(SectionPos.blockToSectionCoord(boundingBox.maxX()), SectionPos.blockToSectionCoord(boundingBox.maxZ()));
                ChunkPos.rangeClosed(chunkPos, chunkPos2).forEach((chunkPosx) -> structureStart.placeInChunk(serverLevel, serverLevel.structureManager(), chunkGenerator, serverLevel.getRandom(), new BoundingBox(chunkPosx.getMinBlockX(), serverLevel.getMinBuildHeight(), chunkPosx.getMinBlockZ(), chunkPosx.getMaxBlockX(), serverLevel.getMaxBuildHeight(), chunkPosx.getMaxBlockZ()), chunkPosx));
            }
        }
    }

    @Override
    public void draw(GuiGraphics graphics, int x, int y, int w, int h) {
        ItemIcon.getItemIcon(Items.COMMAND_BLOCK).draw(graphics, x, y, w, h);
    }

    @Override
    public Codec<? extends Reward> codec() {
        return CODEC;
    }

    @Override
    public StreamCodec<RegistryFriendlyByteBuf, ? extends Reward> streamCodec() {
        return STREAM_CODEC;
    }

    @Override
    protected Reward copy() {
        return new StructureReward(structureId, offsetY);
    }

    @Override
    public String getId() {
        return "structure_reward";
    }
}
