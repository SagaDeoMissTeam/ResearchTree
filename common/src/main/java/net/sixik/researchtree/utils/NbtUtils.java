package net.sixik.researchtree.utils;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

public class NbtUtils {

    public static <T> void putList(CompoundTag nbt, String id, Collection<T> collection, Function<T, Tag> func) {
        if(collection.isEmpty()) return;

        ListTag tags = new ListTag();
        for (T t : collection) {
            tags.add(func.apply(t));
        }
        nbt.put(id, tags);
    }

    public static <T> List<T> getList(CompoundTag nbt, String id, Function<Tag, T> func) {
        if(!nbt.contains(id)) return new ArrayList<>();
        List<T> list = new ArrayList<>();

        ListTag tags = (ListTag) nbt.get(id);

        for (Tag t : tags) {
            list.add(func.apply(t));
        }

        return list;
    }

    public static <T> void getList(CompoundTag nbt, String id, Function<Tag, T> func, Collection<T> toAdd) {
        toAdd.addAll(getList(nbt, id, func));
    }

    public static <T> void getListWithClear(CompoundTag nbt, String id, Function<Tag, T> func, Collection<T> toAdd) {
        toAdd.clear();
        toAdd.addAll(getList(nbt, id, func));
    }

    public static <T> Optional<T> get(CompoundTag nbt, String id, Function<Tag, T> func) {
        if(!nbt.contains(id)) return Optional.empty();
        return Optional.ofNullable(func.apply(nbt.get(id)));
    }
}
