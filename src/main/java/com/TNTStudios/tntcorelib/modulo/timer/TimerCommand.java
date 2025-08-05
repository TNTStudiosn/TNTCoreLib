// Ubicación: src/main/java/com/TNTStudios/tntcorelib/modulo/timer/TimerCommand.java
package com.TNTStudios.tntcorelib.modulo.timer;

import com.TNTStudios.tntcorelib.Tntcorelib;
import com.TNTStudios.tntcorelib.api.timer.TimerApi;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import java.util.Collection;

/**
 * Mi clase de comandos para el temporizador.
 * La he mejorado para incluir control de visibilidad por jugador,
 * alineándose con la nueva funcionalidad de la API.
 */
public class TimerCommand {

    public void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager.literal("tntimer")
                .requires(source -> source.hasPermissionLevel(4))
                .then(CommandManager.literal("start")
                        .executes(context -> {
                            TimerApi api = Tntcorelib.getTimerApi();
                            // Ahora 'start' no solo inicia el conteo, sino que lo muestra a todos.
                            api.start();
                            api.showToAll();
                            context.getSource().sendFeedback(() -> Text.of("Temporizador iniciado y mostrado a todos los jugadores."), true);
                            return 1;
                        }))
                .then(CommandManager.literal("pause")
                        .executes(context -> {
                            TimerApi api = Tntcorelib.getTimerApi();
                            api.pause();
                            context.getSource().sendFeedback(() -> Text.of("Temporizador pausado."), true);
                            return 1;
                        }))
                .then(CommandManager.literal("stop")
                        .executes(context -> {
                            TimerApi api = Tntcorelib.getTimerApi();
                            // 'stop' ahora también se encarga de ocultar la barra a todos.
                            api.stop();
                            context.getSource().sendFeedback(() -> Text.of("Temporizador detenido y ocultado."), true);
                            return 1;
                        }))
                .then(CommandManager.literal("time")
                        .then(CommandManager.argument("seconds", IntegerArgumentType.integer(1))
                                .executes(context -> {
                                    int seconds = IntegerArgumentType.getInteger(context, "seconds");
                                    Tntcorelib.getTimerApi().setTime(seconds);
                                    context.getSource().sendFeedback(() -> Text.of("Tiempo del temporizador establecido a " + seconds + " segundos."), true);
                                    return 1;
                                })))
                .then(CommandManager.literal("add")
                        .then(CommandManager.argument("seconds", IntegerArgumentType.integer())
                                .executes(context -> {
                                    int seconds = IntegerArgumentType.getInteger(context, "seconds");
                                    Tntcorelib.getTimerApi().addTime(seconds);
                                    context.getSource().sendFeedback(() -> Text.of(seconds + " segundos añadidos al temporizador."), true);
                                    return 1;
                                })))
                // ✅ NUEVO: Subcomando para controlar la visibilidad.
                .then(CommandManager.literal("visibility")
                        .then(CommandManager.literal("show")
                                .then(CommandManager.argument("targets", EntityArgumentType.players())
                                        .executes(context -> {
                                            Collection<ServerPlayerEntity> players = EntityArgumentType.getPlayers(context, "targets");
                                            TimerApi api = Tntcorelib.getTimerApi();
                                            players.forEach(api::show);
                                            context.getSource().sendFeedback(() -> Text.of("Mostrando temporizador a " + players.size() + " jugador(es)."), true);
                                            return players.size();
                                        })))
                        .then(CommandManager.literal("hide")
                                .then(CommandManager.argument("targets", EntityArgumentType.players())
                                        .executes(context -> {
                                            Collection<ServerPlayerEntity> players = EntityArgumentType.getPlayers(context, "targets");
                                            TimerApi api = Tntcorelib.getTimerApi();
                                            players.forEach(api::hide);
                                            context.getSource().sendFeedback(() -> Text.of("Ocultando temporizador a " + players.size() + " jugador(es)."), true);
                                            return players.size();
                                        })))
                )
        );
    }
}