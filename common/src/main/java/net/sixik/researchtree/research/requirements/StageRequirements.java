package net.sixik.researchtree.research.requirements;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.ftb.mods.ftblibrary.icon.ItemIcon;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import net.sixik.researchtree.research.BaseResearch;
import net.sixik.researchtree.utils.ResearchUtils;

public class StageRequirements extends Requirements{

    public static final Codec<StageRequirements> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(Codec.STRING.fieldOf("stage").forGetter(StageRequirements::getStage)).apply(instance, StageRequirements::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, StageRequirements> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8, StageRequirements::getStage, StageRequirements::new
    );

    protected String stage;

    public StageRequirements(Void v) {
        super(v);
    }

    public String getStage() {
        return stage;
    }

    public StageRequirements(String stage) {
        super(null);
        this.stage = stage;
    }

    @Override
    public boolean execute(Player player, BaseResearch research) {
        return ResearchUtils.hasStage(player, stage);
    }

    @Override
    public boolean canExecute(Player player, BaseResearch research) {
        return ResearchUtils.hasStage(player, stage);
    }

    @Override
    public void refund(Player player, BaseResearch research, double percentageOfReturn) {}

    @Override
    public void draw(GuiGraphics graphics, int x, int y, int w, int h) {
        ItemIcon.getItemIcon(Items.BARRIER).draw(graphics, x, y, w, h);
    }

    @Override
    public void plus(Requirements requirements) {}

    @Override
    public void minus(Requirements requirements) {}

    @Override
    public int getCount() {
        return 0;
    }

    @Override
    public void setCount(int count) {}

    @Override
    public Requirements copy() {
        return new StageRequirements(stage);
    }

    @Override
    public Codec<? extends Requirements> codec() {
        return CODEC;
    }

    @Override
    public StreamCodec<RegistryFriendlyByteBuf, ? extends Requirements> streamCodec() {
        return STREAM_CODEC;
    }

    @Override
    public String getId() {
        return "stage_requirement";
    }
}
