package net.sixik.researchtree.research.rewards;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.sixik.researchtree.research.BaseResearch;

public class CommandReward extends Reward{

    public static final Codec<CommandReward> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(Codec.STRING.fieldOf("command").forGetter(CommandReward::getCommand)).apply(instance, CommandReward::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, CommandReward> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8, CommandReward::getCommand, CommandReward::new
    );

    protected String command;

    public CommandReward(Void v) {
        super(v);
    }

    public CommandReward(String command) {
        super(null);
        this.command = command;
    }

    public String getCommand() {
        return command;
    }

    @Override
    public void giveReward(Player player, BaseResearch research) {
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
    public void draw(GuiGraphics graphics, int x, int y, int w, int h) {

    }

    @Override
    public Codec<CommandReward> codec() {
        return CODEC;
    }

    @Override
    public StreamCodec<RegistryFriendlyByteBuf, CommandReward> streamCodec() {
        return STREAM_CODEC;
    }

    @Override
    public String getId() {
        return "command_reward";
    }

    @Override
    protected Reward copy() {
        return new CommandReward(command);
    }
}
