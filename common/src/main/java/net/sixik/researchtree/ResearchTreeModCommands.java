package net.sixik.researchtree;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.sixik.researchtree.network.fromServer.SendPlayerResearchDataS2C;
import net.sixik.researchtree.research.manager.PlayerResearchData;
import net.sixik.researchtree.research.manager.ServerResearchManager;
import net.sixik.researchtree.utils.ResearchUtils;

import java.util.Collection;
import java.util.concurrent.CompletableFuture;

public class ResearchTreeModCommands {

    public static void registerCommands(CommandDispatcher<CommandSourceStack> commandSourceStackCommandDispatcher, CommandBuildContext commandBuildContext, Commands.CommandSelection commandSelection) {
        registerCommands(commandSourceStackCommandDispatcher);
    }

    public static void registerCommands(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("research_tree")
                .then(Commands.literal("clear_data")
                        .requires(source -> source.hasPermission(2))
                        .then(Commands.argument("player", EntityArgument.players())
                                .executes(source -> clearData(source.getSource(), EntityArgument.getPlayers(source, "player")))
                        )
                )
                .then(Commands.literal("complete_research")
                        .requires(source -> source.hasPermission(2))
                        .then(Commands.argument("player", EntityArgument.players())
                                .then(Commands.argument("researchDataId", ResourceLocationArgument.id()).suggests((ResearchTreeModCommands::completeResearchGetSuggestions1))
                                                .then(Commands.argument("researchId", ResourceLocationArgument.id()).suggests((ResearchTreeModCommands::completeResearchGetSuggestions2))
                                                                .executes(source -> completeResearch(source.getSource(), EntityArgument.getPlayers(source, "player"), ResourceLocationArgument.getId(source, "researchId"), ResourceLocationArgument.getId(source, "researchId")))
                                                )
                                )
                        )));
    }

    private static int completeResearch(CommandSourceStack source, Collection<ServerPlayer> profiles, ResourceLocation researchDataId, ResourceLocation researchId) {
        ServerResearchManager manager = ResearchUtils.getManagerCast(false);

        for (ServerPlayer profile : profiles) {
            manager.findResearchAndDataById(researchId).ifPresent(findData -> {
                findData.getB().onResearchEnd(profile);
            });
        }

        return 1;
    }

    private static int clearData(CommandSourceStack source, Collection<ServerPlayer> profiles) {
        ServerResearchManager manager = ResearchUtils.getManagerCast(false);

        for (ServerPlayer profile : profiles) {
            manager.getPlayerDataOptional(profile).ifPresent(data -> {
                data.clearResearches();
                data.clearProgress();
                SendPlayerResearchDataS2C.sendTo(profile);
                profile.sendSystemMessage(Component.literal("Research Data Cleared!").withStyle(ChatFormatting.GREEN));
            });
        }

        return 1;
    }

    private static CompletableFuture<Suggestions> completeResearchGetSuggestions1(CommandContext<CommandSourceStack> commandContext, SuggestionsBuilder suggestionsBuilder) {
        ServerResearchManager manager = ResearchUtils.getManagerCast(false);
        return SharedSuggestionProvider.suggest(manager.getAllResearchesData().stream().map(s -> s.getId().toString()), suggestionsBuilder);
    }

    private static CompletableFuture<Suggestions> completeResearchGetSuggestions2(CommandContext<CommandSourceStack> commandContext, SuggestionsBuilder builder) {
        ServerResearchManager manager = ResearchUtils.getManagerCast(false);

        if (commandContext.getSource().getPlayer() != null) {
            PlayerResearchData playerData = manager.getOrCreatePlayerData(commandContext.getSource().getPlayer());
            return SharedSuggestionProvider.suggest(manager.getAllResearches(
                    ResourceLocationArgument.getId(commandContext, "researchDataId")).stream().filter(s -> !playerData.containsInUnlockedResearch(s.getId())).map(s -> s.getId().toString()), builder);
        }

        return SharedSuggestionProvider.suggest(manager.getAllResearches(ResourceLocationArgument.getId(commandContext, "researchDataId")).stream().map(s -> s.getId().toString()), builder);
    }
}
