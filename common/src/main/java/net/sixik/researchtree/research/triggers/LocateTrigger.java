package net.sixik.researchtree.research.triggers;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.sixik.researchtree.research.BaseResearch;

import java.util.Optional;

public class LocateTrigger extends BaseTrigger{

    protected LocateType locateType;
    protected Object arg;

    public LocateTrigger(Void nul) {
        super(nul);
    }

    public LocateTrigger(LocateType locateType, Object arg) {
        super(null);
        this.locateType = locateType;
        this.arg = arg;
    }

    @Override
    public boolean checkComplete(Player player, BaseResearch research, Object[] args) {
        Optional<Object> opt = checkArguments(locateType, arg);
        if(opt.isEmpty()) return false;

        switch (locateType) {
            case BIOME -> {
                BlockPos pos = (BlockPos) args[0];
                return  player.level().getBiome(pos).is((ResourceLocation) opt.get());
            }
            case STRUCTURE -> {
                if(player instanceof ServerPlayer serverPlayer) {
                    ServerLevel serverLevel = serverPlayer.serverLevel();
                    StructureManager mgr = serverLevel.structureManager();
                    Structure structure = mgr.registryAccess().registryOrThrow(Registries.STRUCTURE).get((ResourceKey<Structure>) opt.get());
                    return structure != null && mgr.getStructureWithPieceAt(player.blockPosition(), structure).isValid();
                }
            }
            case DIMENSION -> {
                return player.level().dimension().registry().equals(opt.get());
            }
            case POSITION -> {
                BlockPos pos = player.blockPosition();
                BlockPos owner = (BlockPos) opt.get();
                return pos.getX() == owner.getX() && pos.getY() == owner.getY() && pos.getZ() == owner.getZ();
            }
//            case FEATURE -> {
//                if(player instanceof ServerPlayer serverPlayer) {
////                    serverPlayer.serverLevel().getPoiManager().f
//                }
//            }
        }


        return false;
    }

    public static Optional<Object> checkArguments(LocateType type, Object arg) {
        switch (type) {
            case BIOME, DIMENSION -> {
                if(arg instanceof ResourceLocation)
                    return Optional.of(arg);
            }
            case STRUCTURE -> {
                if(arg instanceof ResourceLocation rs) {
                    return Optional.of(ResourceKey.create(Registries.STRUCTURE, rs));
                }
            }
//            case FEATURE -> {
//                if(arg instanceof ResourceLocation rs) {
//                    return Optional.of(ResourceKey.create(Registries.FEATURE, rs));
//                }
//            }
            case POSITION -> {
                if(arg instanceof BlockPos)
                    return Optional.of(arg);
            }
        }

        return Optional.empty();
    }

    @Override
    public <T extends BaseTrigger> Codec<T> codec() {
        return null;
    }

    @Override
    public <T extends BaseTrigger> StreamCodec<RegistryFriendlyByteBuf, T> streamCodec() {
        return null;
    }

    @Override
    public String getId() {
        return "locate_trigger";
    }
}
