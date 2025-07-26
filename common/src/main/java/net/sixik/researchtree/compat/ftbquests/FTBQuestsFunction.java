package net.sixik.researchtree.compat.ftbquests;

import dev.ftb.mods.ftbquests.quest.TeamData;
import net.minecraft.server.level.ServerPlayer;
import net.sixik.researchtree.compat.ScriptContext;
import net.sixik.researchtree.research.BaseResearch;
import net.sixik.researchtree.research.functions.BaseFunction;

import java.time.Instant;
import java.util.Date;

public class FTBQuestsFunction extends BaseFunction {

    protected long questsId;
    protected boolean isAdd;

    @Override
    public void setArgs(Object... objects) {
        this.questsId = (long) objects[0];
        this.isAdd = (boolean) objects[1];
    }

    @Override
    public int argsLength() {
        return 2;
    }

    @Override
    public boolean checkErrors(ScriptContext context, Object... args) {
        if(!(args[0] instanceof Long)) {
            context.error("Wrong argument 1. Expected 'Long' and got '" + args[0].getClass().getName() + "'");
            return false;
        }
        if(!(args[1] instanceof Boolean)) {
            context.error("Wrong argument 2. Expected 'Boolean' and got '" + args[0].getClass().getName() + "'");
            return false;
        }
        return true;
    }

    @Override
    public void execute(ServerPlayer player, BaseResearch research) {
        if(isAdd)
            TeamData.get(player).setCompleted(questsId, Date.from(Instant.from(Instant.now())));
        else TeamData.get(player).setCompleted(questsId, null);
    }

    @Override
    public String getId() {
        return "ftbquests_function";
    }
}
