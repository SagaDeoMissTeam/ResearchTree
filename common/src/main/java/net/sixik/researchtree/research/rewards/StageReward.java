package net.sixik.researchtree.research.rewards;

import com.mojang.serialization.Codec;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.player.Player;
import net.sixik.researchtree.research.BaseResearch;

public class StageReward extends Reward{

    public StageReward(Void v) {
        super(v);
    }

    @Override
    public void giveReward(Player player, BaseResearch research) {

    }

    @Override
    public void draw(GuiGraphics graphics, int x, int y, int w, int h) {

    }

    @Override
    public Codec<? extends Reward> codec() {
        return null;
    }

    @Override
    public StreamCodec<RegistryFriendlyByteBuf, ? extends Reward> streamCodec() {
        return null;
    }
}
