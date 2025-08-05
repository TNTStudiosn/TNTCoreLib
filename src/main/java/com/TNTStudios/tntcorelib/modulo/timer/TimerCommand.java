// Ubicación: src/main/java/com/TNTStudios/tntcorelib/modulo/timer/TimerCommand.java
package com.TNTStudios.tntcorelib.modulo.timer;

import com.TNTStudios.tntcorelib.Tntcorelib;
import com.TNTStudios.tntcorelib.api.timer.TimerApi;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

/**
 * Mi clase de comandos para el temporizador.
 * Ahora está completamente desacoplada de la lógica interna y solo interactúa
 * a través de la TimerApi, lo que la hace mucho más limpia y mantenible.
 */
public class TimerCommand {

    public void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager.literal("TNTimer")
                .requires(source -> source.hasPermissionLevel(4))
                .then(CommandManager.literal("start")
                        .executes(context -> {
                            TimerApi api = Tntcorelib.getTimerApi();
                            api.start();
                            context.getSource().sendFeedback(() -> Text.of("Temporizador iniciado."), true);
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
                            api.stop();
                            context.getSource().sendFeedback(() -> Text.of("Temporizador detenido y reiniciado."), true);
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
        );
    }
}