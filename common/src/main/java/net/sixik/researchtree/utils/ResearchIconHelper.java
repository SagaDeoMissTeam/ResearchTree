package net.sixik.researchtree.utils;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.DataResult;
import dev.ftb.mods.ftblibrary.icon.Icon;
import dev.ftb.mods.ftblibrary.icon.ItemIcon;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.Optional;

public class ResearchIconHelper {

    public static final String ITEM_ICON_KEY = "item_icon";

    public static Optional<Icon> getIcon(CompoundTag nbt) {
        return getItemIcon(nbt);
    }

    @Environment(EnvType.CLIENT)
    public static Optional<Icon> getItemIcon(CompoundTag nbt) {
        if(!nbt.contains(ITEM_ICON_KEY)) return Optional.empty();
        DataResult<Pair<ItemStack, Tag>> decode = ExternCodecs.ITEM_STACK_CODEC.decode(NbtOps.INSTANCE, nbt.get(ITEM_ICON_KEY));
        Optional<Pair<ItemStack, Tag>> opt = decode.result();
        return opt.map(itemStackTagPair -> ItemIcon.getItemIcon(itemStackTagPair.getFirst()));
    }

    public static void putItemIcon(CompoundTag nbt, Item item) {
        putItemIcon(nbt, item.getDefaultInstance());
    }

    public static void putItemIcon(CompoundTag nbt, ItemStack itemStack) {
        ExternCodecs.ITEM_STACK_CODEC.encodeStart(NbtOps.INSTANCE, itemStack).result().ifPresent(tag -> {
            nbt.put(ITEM_ICON_KEY, tag);
        });
    }
}
