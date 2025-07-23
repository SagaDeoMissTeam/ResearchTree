package net.sixik.researchtree.utils;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.sixik.sdmuilibrary.client.utils.math.Vector2;

public class ExternCodecs {

    public static final Codec<Vector2> VECTOR_2_CODEC = RecordCodecBuilder.create(instance ->
        instance.group(
                Codec.INT.fieldOf("x").forGetter(s -> s.x),
                Codec.INT.fieldOf("y").forGetter(s -> s.y)
        ).apply(instance, Vector2::new));

    public static final StreamCodec<ByteBuf, Vector2> VECTOR_2_STREAM_CODEC =
            StreamCodec.composite(ByteBufCodecs.INT, s -> s.x, ByteBufCodecs.INT, s -> s.y, Vector2::new);

    public static final Codec<Holder<Item>> ITEM_HOLDER =
            BuiltInRegistries.ITEM.holderByNameCodec().validate(DataResult::success);

    public static final Codec<ItemStack> ITEM_STACK_CODEC = Codec.lazyInitialized(() ->
            RecordCodecBuilder.create((instance) ->
                    instance.group(
                            ITEM_HOLDER.fieldOf("id").forGetter(ItemStack::getItemHolder),
                            Codec.INT.fieldOf("count").orElse(1).forGetter(ItemStack::getCount),
                            DataComponentPatch.CODEC.optionalFieldOf("components", DataComponentPatch.EMPTY).forGetter(ItemStack::getComponentsPatch)
                    ).apply(instance, ItemStack::new)));
}
