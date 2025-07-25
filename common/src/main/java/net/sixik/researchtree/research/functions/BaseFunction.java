package net.sixik.researchtree.research.functions;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.sixik.researchtree.compat.ScriptContext;
import net.sixik.researchtree.research.BaseResearch;
import net.sixik.researchtree.research.rewards.Reward;
import org.apache.commons.lang3.NotImplementedException;

public abstract class BaseFunction {

    protected ExecuteStage executeStage;

    @Deprecated
    public final <T extends Reward> StreamCodec<RegistryFriendlyByteBuf, T> streamCodec() {
        throw new NotImplementedException();
    }

    public void setStage(int stage) {
        this.executeStage = executeStageFromInt(stage);
    }

    public abstract void setArgs(Object... objects);

    public abstract int argsLength();

    public abstract boolean checkErrors(ScriptContext context, Object... args);

    public final boolean internalCheckErrors(ScriptContext context, Object... args) {
        if(args.length != argsLength()) {
            context.error("Incorrect number of arguments. Requires " +  argsLength() + " received " + args.length);
            return false;
        }

        return checkErrors(context, args);
    }

    public abstract void execute(ServerPlayer player, BaseResearch research);

    public String getId() {
        return this.getClass().getName();
    }

    public enum ExecuteStage {
        BEFORE,
        AFTER
    }

    public ExecuteStage getExecuteStage() {
        return executeStage;
    }

    public final boolean isBeforeStage() {
        return getExecuteStage() == ExecuteStage.BEFORE;
    }

    public final boolean isAfterStage() {
        return getExecuteStage() == ExecuteStage.AFTER;
    }

    public static ExecuteStage executeStageFromInt(int index) {
        return ExecuteStage.values()[Mth.clamp(index, 0, ExecuteStage.values().length)];
    }
}
