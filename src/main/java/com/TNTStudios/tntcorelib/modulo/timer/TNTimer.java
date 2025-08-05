package com.TNTStudios.tntcorelib.modulo.timer;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.entity.boss.ServerBossBar;
import net.minecraft.entity.boss.BossBar.Color;
import net.minecraft.entity.boss.BossBar.Style;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;

public class TNTimer {

    private static final Logger LOGGER = LogManager.getLogger("TNTimer");
    private static int countdown;
    private static boolean isRunning;
    private static ServerBossBar bossBar;
    private static List<ServerPlayerEntity> players = new ArrayList<>();
    private static MinecraftServer serverInstance;
    private static int tickCounter = 0; // To manage seconds

    public static void initialize(MinecraftServer server) {
        serverInstance = server;
        countdown = 300; // Default to 5 minutes
        isRunning = false;
        ensureBossBarInitialized();
        LOGGER.info("TNTimer linked to MinecraftServer instance.");

        // Register tick event
        ServerTickEvents.START_SERVER_TICK.register(serverTick -> onServerTick());
    }

    public static void registerCommands(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(
                CommandManager.literal("tt")
                        .then(CommandManager.literal("TNTimer")
                                .requires(source -> source.hasPermissionLevel(4))
                                .then(CommandManager.literal("start").executes(TNTimer::startCountdown))
                                .then(CommandManager.literal("pause").executes(TNTimer::pauseCountdown))
                                .then(CommandManager.literal("stop").executes(TNTimer::stopCountdown))
                                .then(CommandManager.literal("time")
                                        .then(CommandManager.argument("seconds", IntegerArgumentType.integer(0))
                                                .executes(TNTimer::setTime)))
                        )
        );
        LOGGER.info("Commands registered successfully.");
    }

    private static int startCountdown(CommandContext<ServerCommandSource> context) {
        try {
            if (isRunning) {
                context.getSource().sendError(Text.of("El temporizador ya está en marcha."));
                return 0;
            }

            ensureBossBarInitialized();
            isRunning = true;
            bossBar.setVisible(true);

            players = new ArrayList<>(context.getSource().getServer().getPlayerManager().getPlayerList());
            for (ServerPlayerEntity player : players) {
                bossBar.addPlayer(player);
            }

            context.getSource().sendFeedback(() -> Text.of("Temporizador iniciado."), true);
            LOGGER.info("Timer started successfully.");
            return 1;
        } catch (Exception e) {
            LOGGER.error("Error starting the timer: ", e);
            context.getSource().sendError(Text.of("Error al iniciar el temporizador."));
            return 0;
        }
    }

    private static int pauseCountdown(CommandContext<ServerCommandSource> context) {
        try {
            if (!isRunning) {
                context.getSource().sendError(Text.of("El temporizador no está en marcha."));
                return 0;
            }
            isRunning = false;
            context.getSource().sendFeedback(() -> Text.of("Temporizador pausado."), true);
            LOGGER.info("Timer paused successfully.");
            return 1;
        } catch (Exception e) {
            LOGGER.error("Error pausing the timer: ", e);
            context.getSource().sendError(Text.of("Error al pausar el temporizador."));
            return 0;
        }
    }

    private static int stopCountdown(CommandContext<ServerCommandSource> context) {
        try {
            isRunning = false;
            bossBar.clearPlayers();
            bossBar.setVisible(false);
            countdown = 300; // Reset default countdown
            context.getSource().sendFeedback(() -> Text.of("Temporizador detenido y reiniciado."), true);
            LOGGER.info("Timer stopped and reset successfully.");
            return 1;
        } catch (Exception e) {
            LOGGER.error("Error stopping the timer: ", e);
            context.getSource().sendError(Text.of("Error al detener el temporizador."));
            return 0;
        }
    }

    private static int setTime(CommandContext<ServerCommandSource> context) {
        try {
            int time = IntegerArgumentType.getInteger(context, "seconds");
            countdown = time;
            updateBossBar();
            context.getSource().sendFeedback(() -> Text.of("El tiempo del temporizador ha sido actualizado a " + time + " segundos."), true);
            LOGGER.info("Timer time updated to {} seconds.", time);
            return 1;
        } catch (Exception e) {
            LOGGER.error("Error setting the timer time: ", e);
            context.getSource().sendError(Text.of("Error al actualizar el tiempo del temporizador."));
            return 0;
        }
    }

    private static void updateBossBar() {
        try {
            ensureBossBarInitialized();
            String timeFormatted = String.format("%d:%02d", countdown / 60, countdown % 60);
            bossBar.setName(Text.of("Tiempo restante: " + timeFormatted));
            bossBar.setPercent(countdown > 0 ? (float) countdown / 300 : 0);
        } catch (Exception e) {
            LOGGER.error("Error updating the BossBar: ", e);
        }
    }

    private static void onServerTick() {
        try {
            if (isRunning && countdown > 0) {
                tickCounter++;
                if (tickCounter >= 20) { // 20 ticks = 1 second
                    countdown--;
                    tickCounter = 0;
                }
                updateBossBar();

                if (countdown <= 0) {
                    isRunning = false;
                    bossBar.clearPlayers();
                    bossBar.setVisible(false);

                    if (serverInstance != null) {
                        serverInstance.getPlayerManager().broadcast(Text.of("El temporizador ha terminado."), false);
                        LOGGER.info("Timer ended successfully.");
                    }
                }
            }
        } catch (Exception e) {
            LOGGER.error("Error during server tick handling: ", e);
        }
    }

    public static void ensureBossBarInitialized() {
        if (bossBar == null) {
            bossBar = new ServerBossBar(Text.of("Temporizador"), Color.WHITE, Style.PROGRESS);
            LOGGER.warn("BossBar was not initialized. Reinitializing now.");
        }
    }
}
