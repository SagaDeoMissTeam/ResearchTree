package net.sixik.researchtree.api;

import com.mojang.serialization.Codec;

public interface CodecSerializer<T> {

    Codec<T> codec();
}
