package net.sixik.researchtree.compat.ftbquests;

import com.mojang.serialization.Codec;
import dev.ftb.mods.ftbquests.FTBQuestsAPIImpl;
import dev.ftb.mods.ftbquests.quest.Quest;
import dev.ftb.mods.ftbquests.quest.TeamData;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.player.Player;
import net.sixik.researchtree.research.BaseResearch;
import net.sixik.researchtree.research.triggers.BaseTrigger;

public class FTBQuestsTrigger extends BaseTrigger {

    protected long questsId;

    public FTBQuestsTrigger(Void nul) {
        super(nul);
    }

    public FTBQuestsTrigger(long questsId) {
        super(null);
        this.questsId = questsId;
    }

    @Override
    public boolean checkComplete(Player player, BaseResearch research, Object[] args) {
        Quest quest = FTBQuestsAPIImpl.INSTANCE.getQuestFile(false).getQuest(questsId);
        if(quest == null)
            throw new RuntimeException("Can't find quest with id " + questsId);

        return TeamData.get(player).isCompleted(quest);
    }

    @Override
    public <T extends BaseTrigger> Codec<T> codec() {
        return null;
    }

    @Override
    public <T extends BaseTrigger> StreamCodec<RegistryFriendlyByteBuf, T> streamCodec() {
        return null;
    }

    @Override
    public String getId() {
        return "ftbquests_trigger";
    }
}
