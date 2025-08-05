// Ubicación: src/main/java/com/TNTStudios/tntcorelib/modulo/tntalert/TNTAlertCommand.java
package com.TNTStudios.tntcorelib.modulo.tntalert;

import com.TNTStudios.tntcorelib.Tntcorelib;
import com.TNTStudios.tntcorelib.api.tntalert.AlertType;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.command.CommandSource;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * Mi comando para enviar alertas desde el servidor.
 * Solo los operadores de nivel 4 pueden usarlo, asegurando
 * que solo personal autorizado pueda enviar notificaciones globales.
 */
public class TNTAlertCommand {

    public void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager.literal("tntalert")
                .requires(source -> source.hasPermissionLevel(4))
                .then(CommandManager.argument("type", StringArgumentType.word())
                        .suggests(this::getAlertTypeSuggestions)
                        .then(CommandManager.argument("targets", EntityArgumentType.players())
                                .then(CommandManager.argument("title", StringArgumentType.string())
                                        .then(CommandManager.argument("subtitle", StringArgumentType.greedyString())
                                                .executes(this::executeSendAlert)
                                        )
                                )
                        )
                )
        );
    }

    private int executeSendAlert(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        String typeStr = StringArgumentType.getString(context, "type").toUpperCase();
        AlertType type;
        try {
            type = AlertType.valueOf(typeStr);
        } catch (IllegalArgumentException e) {
            context.getSource().sendError(Text.of("Tipo de alerta inválido. Usa uno de: " + getAlertTypesAsString()));
            return 0;
        }

        Collection<ServerPlayerEntity> players = EntityArgumentType.getPlayers(context, "targets");
        String title = StringArgumentType.getString(context, "title");
        String subtitle = StringArgumentType.getString(context, "subtitle");

        Tntcorelib.getTNTAlertApi().showAlert(players, type, title, subtitle);

        context.getSource().sendFeedback(() -> Text.of("Alerta tipo '" + type + "' enviada a " + players.size() + " jugador(es)."), true);
        return players.size();
    }

    private CompletableFuture<Suggestions> getAlertTypeSuggestions(CommandContext<ServerCommandSource> context, SuggestionsBuilder builder) {
        return CommandSource.suggestMatching(Arrays.stream(AlertType.values()).map(Enum::name), builder);
    }

    private String getAlertTypesAsString() {
        return Arrays.stream(AlertType.values()).map(Enum::name).collect(Collectors.joining(", "));
    }
}