package net.sixik.researchtree.research.functions;

import net.minecraft.server.level.ServerPlayer;
import net.sixik.researchtree.compat.ScriptContext;
import net.sixik.researchtree.research.BaseResearch;

import java.util.function.BiConsumer;

public class ScriptFunction extends BaseFunction{

    protected BiConsumer<ServerPlayer, BaseResearch> consumer;

    public ScriptFunction() {}

    public ScriptFunction(BiConsumer<ServerPlayer, BaseResearch> consumer, int stage) {
        this.consumer = consumer;
        this.executeStage = executeStageFromInt(stage);
    }

    @Override
    public void setArgs(Object... objects) {
        this.consumer = (BiConsumer<ServerPlayer, BaseResearch>) objects[0];
        this.executeStage = (ExecuteStage) objects[1];
    }

    @Override
    public int argsLength() {
        return 1;
    }

    @Override
    public boolean checkErrors(ScriptContext context, Object... args) {
        boolean errors = true;
        if(!(args[0] instanceof BiConsumer)) {
            context.error("Wrong argument 1. Expected 'BiConsumer<Player, BaseResearch>' and got '" + args[0].getClass().getName() + "'");
            errors = false;
        }

        return errors;
    }

    @Override
    public void execute(ServerPlayer player, BaseResearch research) {
        consumer.accept(player, research);
    }

    @Override
    public String getId() {
        return "custom_function";
    }
}
