package net.sixik.researchtree.client.edit_menu;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public record EditData(String id, EditType editType, TransformData transformData) {

    public static final Codec<EditData> CODEC = RecordCodecBuilder.create(instance ->
        instance.group(
                Codec.STRING.fieldOf("id").forGetter(EditData::id),
                Codec.INT.fieldOf("editType").forGetter(s -> s.editType.ordinal()),
                TransformData.CODEC.fieldOf("data").forGetter(EditData::transformData)
        ).apply(instance, (d1,d2,d3) -> new EditData(d1, EditType.values()[d2], d3)));

    public static final StreamCodec<ByteBuf, EditData> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8, EditData::id,
            ByteBufCodecs.INT, s -> s.editType.ordinal(),
            TransformData.STREAM_CODEC, EditData::transformData,
            (d1,d2,d3) -> new EditData(d1, EditType.values()[d2], d3));
}
