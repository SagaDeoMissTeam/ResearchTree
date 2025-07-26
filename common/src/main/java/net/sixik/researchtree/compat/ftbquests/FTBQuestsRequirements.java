package net.sixik.researchtree.compat.ftbquests;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.ftb.mods.ftblibrary.icon.ItemIcon;
import dev.ftb.mods.ftbquests.FTBQuestsAPIImpl;
import dev.ftb.mods.ftbquests.quest.Quest;
import dev.ftb.mods.ftbquests.quest.TeamData;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import net.sixik.researchtree.research.BaseResearch;
import net.sixik.researchtree.research.requirements.Requirements;

public class FTBQuestsRequirements extends Requirements {

    public static final Codec<FTBQuestsRequirements> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(Codec.LONG.fieldOf("questId").forGetter(FTBQuestsRequirements::getQuestsId)).apply(instance, FTBQuestsRequirements::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, FTBQuestsRequirements> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_LONG, FTBQuestsRequirements::getQuestsId, FTBQuestsRequirements::new
    );

    protected long questsId;

    public FTBQuestsRequirements(Void v) {
        super(v);
    }

    public FTBQuestsRequirements(long questsId) {
        super(null);
        this.questsId = questsId;
    }

    public long getQuestsId() {
        return questsId;
    }

    @Override
    public boolean execute(Player player, BaseResearch research) {
        return true;
    }

    @Override
    public boolean canExecute(Player player, BaseResearch research) {
        Quest quest = FTBQuestsAPIImpl.INSTANCE.getQuestFile(!(player instanceof ServerPlayer)).getQuest(questsId);
        if(quest == null) return false;
        return TeamData.get(player).isCompleted(quest);
    }

    @Override
    public void refund(Player player, BaseResearch research, double percentageOfReturn) {

    }

    @Override
    public void draw(GuiGraphics graphics, int x, int y, int w, int h) {
        ItemIcon.getItemIcon(Items.BEDROCK).draw(graphics, x, y, w, h);
    }

    @Override
    public void plus(Requirements requirements) {

    }

    @Override
    public void minus(Requirements requirements) {

    }

    @Override
    public int getCount() {
        return 0;
    }

    @Override
    public void setCount(int count) {

    }

    @Override
    protected Requirements copy() {
        return new FTBQuestsRequirements(questsId);
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
        return "ftbquests_requirement";
    }
}
