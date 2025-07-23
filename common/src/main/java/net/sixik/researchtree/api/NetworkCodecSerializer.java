package net.sixik.researchtree.api;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

public interface NetworkCodecSerializer<T> {

    StreamCodec<FriendlyByteBuf, T> streamCodec();
}
