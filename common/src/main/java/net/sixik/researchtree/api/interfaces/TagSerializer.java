package net.sixik.researchtree.api.interfaces;

import net.minecraft.nbt.Tag;

public interface TagSerializer<T extends Tag> {

    T serializer();
    void deserialize(T nbt);
}
