package net.sixik.researchtree.api;

import net.minecraft.nbt.Tag;

public interface TagSerializer<T extends Tag> {

    T serializer();
    void deserialize(T nbt);
}
