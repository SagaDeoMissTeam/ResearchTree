package net.sixik.researchtree.research.rewards;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.ftb.mods.ftblibrary.icon.ItemIcon;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import net.sixik.researchtree.research.BaseResearch;
import net.sixik.researchtree.utils.ResearchUtils;

public class StageReward extends Reward{

    public static final Codec<StageReward> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(Codec.STRING.fieldOf("stage").forGetter(StageReward::getStage)
            ).apply(instance, StageReward::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, StageReward> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8, StageReward::getStage, StageReward::new
    );

    protected String stage;

    public StageReward(Void v) {
        super(v);
    }

    public StageReward(String stage) {
        super(null);
        this.stage = stage;
    }

    protected String getStage() {
        return stage;
    }

    @Override
    public void giveReward(Player player, BaseResearch research) {
        if(player instanceof ServerPlayer serverPlayer) {
            ResearchUtils.addStage(serverPlayer, stage);
        }
    }

    @Override
    @Environment(EnvType.CLIENT)
    public void draw(GuiGraphics graphics, int x, int y, int w, int h) {
        ItemIcon.getItemIcon(Items.BARRIER).draw(graphics, x, y, w, h);
    }

    @Override
    public Codec<? extends Reward> codec() {
        return CODEC;
    }

    @Override
    public StreamCodec<RegistryFriendlyByteBuf, ? extends Reward> streamCodec() {
        return STREAM_CODEC;
    }

    @Override
    public String getId() {
        return "stage_reward";
    }

    @Override
    protected Reward copy() {
        return new StageReward(stage);
    }
}
