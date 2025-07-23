package net.sixik.researchtree.client.edit_menu;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

import java.util.List;

public record EditPresets(String id, List<EditData> editData) {

    public static final Codec<EditPresets> CODEC = RecordCodecBuilder.create(instance ->
        instance.group(
                Codec.STRING.fieldOf("id").forGetter(EditPresets::id),
                EditData.CODEC.listOf().fieldOf("data").forGetter(EditPresets::editData)
        ).apply(instance, EditPresets::new));

    public static final StreamCodec<ByteBuf, EditPresets> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8, EditPresets::id,
            EditData.STREAM_CODEC.apply(ByteBufCodecs.list()), EditPresets::editData,
            EditPresets::new
    );
}
