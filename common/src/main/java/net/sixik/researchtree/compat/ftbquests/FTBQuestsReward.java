package net.sixik.researchtree.compat.ftbquests;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.ftb.mods.ftblibrary.icon.ItemIcon;
import dev.ftb.mods.ftbquests.quest.TeamData;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import net.sixik.researchtree.research.BaseResearch;
import net.sixik.researchtree.research.rewards.Reward;

import java.time.Instant;
import java.util.Date;

public class FTBQuestsReward extends Reward {

    public static final Codec<FTBQuestsReward> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(Codec.LONG.fieldOf("questId").forGetter(FTBQuestsReward::getQuestsId)).apply(instance, FTBQuestsReward::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, FTBQuestsReward> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_LONG, FTBQuestsReward::getQuestsId, FTBQuestsReward::new
    );

    protected long questsId;

    public FTBQuestsReward(Void v) {
        super(v);
    }

    public FTBQuestsReward(long questsId) {
        super(null);
        this.questsId = questsId;
    }

    public long getQuestsId() {
        return questsId;
    }

    @Override
    public void giveReward(Player player, BaseResearch research) {
        TeamData.get(player).setCompleted(questsId, Date.from(Instant.from(Instant.now())));
    }

    @Override
    public void draw(GuiGraphics graphics, int x, int y, int w, int h) {
        ItemIcon.getItemIcon(Items.BEDROCK).draw(graphics, x, y, w, h);
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
    protected Reward copy() {
        return new FTBQuestsReward(questsId);
    }

    @Override
    public String getId() {
        return "ftbquests_reward";
    }
}
