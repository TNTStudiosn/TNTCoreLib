package com.TNTStudios.tntcorelib.modulo.custommodels;

import com.TNTStudios.tntcorelib.Tntcorelib;
import com.TNTStudios.tntcorelib.api.custommodels.CustomModelsApi;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.command.CommandSource;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import java.util.Collection;
import java.util.concurrent.CompletableFuture;

/**
 * Mi comando para administrar los modelos personalizados.
 * Ahora los operadores de nivel 2 o superior pueden usarlo.
 */
public class CustomModelsCommand {

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager.literal("tntmodels")
                .requires(source -> source.hasPermissionLevel(4))
                .then(CommandManager.literal("set")
                        .then(CommandManager.argument("targets", EntityArgumentType.players())
                                .then(CommandManager.argument("modelName", StringArgumentType.string())
                                        .suggests(CustomModelsCommand::getModelSuggestions)
                                        .executes(context -> {
                                            Collection<ServerPlayerEntity> players = EntityArgumentType.getPlayers(context, "targets");
                                            String modelName = StringArgumentType.getString(context, "modelName");
                                            Tntcorelib.getCustomModelsApi().setModel(players, modelName);
                                            context.getSource().sendFeedback(() -> Text.literal("Aplicando modelo '" + modelName + "' a " + players.size() + " jugador(es)."), true);
                                            return players.size();
                                        })
                                )
                        )
                )
                .then(CommandManager.literal("reset")
                        .then(CommandManager.argument("targets", EntityArgumentType.players())
                                .executes(context -> {
                                    Collection<ServerPlayerEntity> players = EntityArgumentType.getPlayers(context, "targets");
                                    Tntcorelib.getCustomModelsApi().resetModel(players);
                                    context.getSource().sendFeedback(() -> Text.literal("Restableciendo modelo para " + players.size() + " jugador(es)."), true);
                                    return players.size();
                                })
                        )
                )
        );
    }

    private static CompletableFuture<Suggestions> getModelSuggestions(com.mojang.brigadier.context.CommandContext<ServerCommandSource> context, SuggestionsBuilder builder) {
        CustomModelsApi api = Tntcorelib.getCustomModelsApi();
        return CommandSource.suggestMatching(api.getAvailableModels(), builder);
    }
}