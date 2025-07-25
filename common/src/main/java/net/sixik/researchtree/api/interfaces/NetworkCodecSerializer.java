package net.sixik.researchtree.api.interfaces;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

public interface NetworkCodecSerializer<T> {

    StreamCodec<FriendlyByteBuf, T> streamCodec();
}
