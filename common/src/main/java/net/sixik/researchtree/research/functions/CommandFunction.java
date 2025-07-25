package net.sixik.researchtree.research.functions;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.level.ServerPlayer;
import net.sixik.researchtree.compat.ScriptContext;
import net.sixik.researchtree.research.BaseResearch;

public class CommandFunction extends BaseFunction{

    protected String command;

    public CommandFunction() {}

    @Override
    public void setArgs(Object... objects) {
        this.command = (String) objects[0];
    }

    @Override
    public int argsLength() {
        return 1;
    }

    @Override
    public boolean checkErrors(ScriptContext context, Object... args) {
        if(!(args[0] instanceof String)) {
            context.error("Wrong argument 1. Expected 'String and got '" + args[0].getClass().getName() + "'");
            return false;
        }

        return true;
    }

    @Override
    public void execute(ServerPlayer player, BaseResearch research) {
        if(player instanceof ServerPlayer serverPlayer) {
            if(command.isEmpty()) return;

            String copyCommand = command;

            if(copyCommand.contains("{player}"))
                copyCommand = copyCommand.replace("{player}", serverPlayer.getName().getString());

            CommandSourceStack commandSource = serverPlayer.createCommandSourceStack();
            commandSource = commandSource.withPermission(2);
            commandSource = commandSource.withSuppressedOutput();

            player.getServer().getCommands().performPrefixedCommand(commandSource, copyCommand);
        }
    }

    @Override
    public String getId() {
        return "command";
    }
}
