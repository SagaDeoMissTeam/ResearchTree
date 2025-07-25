package net.sixik.researchtree.api.interfaces;

import com.mojang.serialization.Codec;

public interface CodecSerializer<T> {

    Codec<T> codec();
}
