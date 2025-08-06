// Ubicación: src/main/java/com/TNTStudios/tntcorelib/modulo/playerstats/PlayerStatsCommand.java
package com.TNTStudios.tntcorelib.modulo.playerstats;

import com.TNTStudios.tntcorelib.Tntcorelib;
import com.TNTStudios.tntcorelib.api.playerstats.PlayerStatsApi;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import java.util.Collection;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

/**
 * Mi comando para controlar las estadísticas de los jugadores desde el servidor.
 * Requiere nivel 4 de OP para su uso.
 */
public class PlayerStatsCommand {

    public void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(literal("tntstats")
                .requires(source -> source.hasPermissionLevel(4))

                // --- Comandos de Vida ---
                .then(literal("health")
                        .then(literal("pause")
                                .then(argument("targets", EntityArgumentType.players())
                                        .executes(ctx -> execute(ctx.getSource(), EntityArgumentType.getPlayers(ctx, "targets"), "health_pause", 0f))))
                        .then(literal("resume")
                                .then(argument("targets", EntityArgumentType.players())
                                        .executes(ctx -> execute(ctx.getSource(), EntityArgumentType.getPlayers(ctx, "targets"), "health_resume", 0f))))
                        .then(literal("set")
                                .then(argument("targets", EntityArgumentType.players())
                                        .then(argument("amount", FloatArgumentType.floatArg(0.1f))
                                                .executes(ctx -> execute(ctx.getSource(), EntityArgumentType.getPlayers(ctx, "targets"), "health_set", FloatArgumentType.getFloat(ctx, "amount")))))))

                // --- Comandos de Hambre ---
                .then(literal("food")
                        .then(literal("pause")
                                .then(argument("targets", EntityArgumentType.players())
                                        .executes(ctx -> execute(ctx.getSource(), EntityArgumentType.getPlayers(ctx, "targets"), "food_pause", 0f))))
                        .then(literal("resume")
                                .then(argument("targets", EntityArgumentType.players())
                                        .executes(ctx -> execute(ctx.getSource(), EntityArgumentType.getPlayers(ctx, "targets"), "food_resume", 0f))))
                        .then(literal("set")
                                .then(argument("targets", EntityArgumentType.players())
                                        .then(argument("amount", IntegerArgumentType.integer(0, 20))
                                                .executes(ctx -> execute(ctx.getSource(), EntityArgumentType.getPlayers(ctx, "targets"), "food_set", (float)IntegerArgumentType.getInteger(ctx, "amount")))))))
        );
    }

    private int execute(ServerCommandSource source, Collection<ServerPlayerEntity> targets, String action, float value) {
        PlayerStatsApi api = Tntcorelib.getPlayerStatsApi();

        switch (action) {
            case "health_pause" -> {
                api.pauseHealth(targets);
                source.sendFeedback(() -> Text.literal("Vida pausada para " + targets.size() + " jugador(es)."), true);
            }
            case "health_resume" -> {
                api.resumeHealth(targets);
                source.sendFeedback(() -> Text.literal("Vida reanudada para " + targets.size() + " jugador(es)."), true);
            }
            case "health_set" -> {
                api.setHealth(targets, value);
                source.sendFeedback(() -> Text.literal("Vida establecida a " + value + " para " + targets.size() + " jugador(es)."), true);
            }
            case "food_pause" -> {
                api.pauseFood(targets);
                source.sendFeedback(() -> Text.literal("Hambre pausada para " + targets.size() + " jugador(es)."), true);
            }
            case "food_resume" -> {
                api.resumeFood(targets);
                source.sendFeedback(() -> Text.literal("Hambre reanudada para " + targets.size() + " jugador(es)."), true);
            }
            case "food_set" -> {
                api.setFoodLevel(targets, (int) value);
                source.sendFeedback(() -> Text.literal("Hambre establecida a " + (int) value + " para " + targets.size() + " jugador(es)."), true);
            }
        }
        return targets.size();
    }
}