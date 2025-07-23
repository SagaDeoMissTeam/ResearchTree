package net.sixik.researchtree.client.edit_menu;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.ftb.mods.ftblibrary.ui.Widget;
import io.netty.buffer.ByteBuf;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.codec.StreamCodec;
import net.sixik.researchtree.utils.ExternCodecs;
import net.sixik.sdmuilibrary.client.utils.math.Vector2;

public class TransformData {

    public static final Codec<TransformData> CODEC = RecordCodecBuilder.create(instance ->
        instance.group(
                ExternCodecs.VECTOR_2_CODEC.fieldOf("position").forGetter(TransformData::getPosition),
                ExternCodecs.VECTOR_2_CODEC.fieldOf("rotation").forGetter(TransformData::getRotation),
                ExternCodecs.VECTOR_2_CODEC.fieldOf("scale").forGetter(TransformData::getScale)
        ).apply(instance, TransformData::new));

    public static final StreamCodec<ByteBuf, TransformData> STREAM_CODEC = StreamCodec.composite(
            ExternCodecs.VECTOR_2_STREAM_CODEC, TransformData::getPosition,
            ExternCodecs.VECTOR_2_STREAM_CODEC, TransformData::getRotation,
            ExternCodecs.VECTOR_2_STREAM_CODEC, TransformData::getScale,
            TransformData::new
    );

    protected Vector2 position;
    protected Vector2 rotation;
    protected Vector2 scale;

    public TransformData() {
        this(new Vector2(0,0), new Vector2(0,0), new Vector2(0,0));
    }

    @Environment(EnvType.CLIENT)
    public TransformData(Widget widget) {
        this(new Vector2(widget.getX(), widget.getY()), new Vector2(0,0), new Vector2(widget.getWidth(), widget.getHeight()));
    }

    public TransformData(Vector2 position, Vector2 rotation, Vector2 scale) {
        this.position = position;
        this.rotation = rotation;
        this.scale = scale;
    }

    public Vector2 getPosition() {
        return position;
    }

    public Vector2 getRotation() {
        return rotation;
    }

    public Vector2 getScale() {
        return scale;
    }

    public void setPosition(Vector2 position) {
        this.position = position;
    }

    public void setRotation(Vector2 rotation) {
        this.rotation = rotation;
    }

    public void setScale(Vector2 scale) {
        this.scale = scale;
    }
}
