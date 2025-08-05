// Ubicación: src/main/java/com/TNTStudios/tntcorelib/modulo/timer/TimerManager.java
package com.TNTStudios.tntcorelib.modulo.timer;

import com.TNTStudios.tntcorelib.api.timer.TimerApi;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.entity.boss.BossBar;
import net.minecraft.entity.boss.ServerBossBar;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import java.util.function.Consumer;

/**
 * Mi implementación de la API del temporizador.
 * Esta clase es la que hace todo el trabajo pesado: maneja el estado,
 * actualiza la Boss Bar cada tick y se asegura de que los jugadores que se conectan
 * o desconectan sean manejados correctamente.
 */
public class TimerManager implements TimerApi {

    private final MinecraftServer server;
    private final ServerBossBar bossBar;
    private boolean isRunning = false;
    private int maxCountdown = 300; // El tiempo total para calcular el porcentaje.
    private int countdown = 300; // El tiempo restante.
    private int tickCounter = 0;
    private Runnable onFinishAction;

    public TimerManager(MinecraftServer server) {
        this.server = server;
        this.bossBar = new ServerBossBar(Text.literal("Temporizador"), BossBar.Color.WHITE, BossBar.Style.PROGRESS);
        this.onFinishAction = () -> server.getPlayerManager().broadcast(Text.of("¡El tiempo ha terminado!"), false);

        // Registro los eventos para que todo funcione de forma automática.
        this.registerServerEvents();
    }

    @Override
    public void start() {
        if (this.isRunning) return; // Si ya está corriendo, no hago nada.
        this.isRunning = true;
        this.bossBar.setVisible(true);
        // Me aseguro de que todos los jugadores actuales vean la barra.
        for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
            this.bossBar.addPlayer(player);
        }
    }

    @Override
    public void pause() {
        this.isRunning = false;
    }

    @Override
    public void stop() {
        this.isRunning = false;
        this.countdown = this.maxCountdown; // Lo reseteo al último tiempo configurado.
        this.bossBar.setVisible(false);
        this.bossBar.clearPlayers();
    }

    @Override
    public void setTime(int seconds) {
        this.maxCountdown = Math.max(1, seconds); // Me aseguro de que el tiempo máximo nunca sea menor a 1.
        this.countdown = this.maxCountdown;
        updateBossBar(); // Actualizo la barra inmediatamente.
    }

    @Override
    public void addTime(int seconds) {
        this.countdown = Math.max(0, this.countdown + seconds);
        // Si el tiempo añadido supera el máximo, actualizo el máximo también.
        if (this.countdown > this.maxCountdown) {
            this.maxCountdown = this.countdown;
        }
        updateBossBar();
    }

    @Override
    public int getTimeRemaining() {
        return this.countdown;
    }

    @Override
    public boolean isRunning() {
        return this.isRunning;
    }

    @Override
    public void styleBossBar(Consumer<BossBar> styleConsumer) {
        styleConsumer.accept(this.bossBar);
        updateBossBar(); // Aplico los cambios visuales inmediatamente.
    }

    @Override
    public void setOnFinishAction(Runnable onFinish) {
        this.onFinishAction = onFinish;
    }

    private void onServerTick(MinecraftServer server) {
        if (!this.isRunning || this.countdown <= 0) {
            return;
        }

        tickCounter++;
        if (tickCounter >= 20) { // 20 ticks = 1 segundo
            this.countdown--;
            tickCounter = 0;
            updateBossBar();

            if (this.countdown <= 0) {
                // El temporizador ha terminado.
                if (this.onFinishAction != null) {
                    this.onFinishAction.run();
                }
                stop(); // Detengo y reseteo el temporizador.
            }
        }
    }

    private void updateBossBar() {
        String timeFormatted = String.format("%d:%02d", countdown / 60, countdown % 60);
        this.bossBar.setName(Text.of("Tiempo restante: " + timeFormatted));
        // ✅ CORREGIDO: El porcentaje ahora se calcula sobre el tiempo máximo configurado.
        this.bossBar.setPercent(this.maxCountdown > 0 ? (float) this.countdown / this.maxCountdown : 0);
    }

    /**
     * Aquí registro todos los eventos necesarios para el funcionamiento del módulo.
     * Esto me asegura que la lógica se ejecute cuando debe, de forma limpia.
     */
    private void registerServerEvents() {
        // Evento principal para la cuenta atrás.
        ServerTickEvents.START_SERVER_TICK.register(this::onServerTick);

        // ✅ MEJORA: Añado al jugador a la Boss Bar cuando se conecta si está activa.
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            if (this.bossBar.isVisible()) {
                this.bossBar.addPlayer(handler.getPlayer());
            }
        });

        // La Boss Bar ya gestiona la desconexión de jugadores automáticamente,
        // así que no necesito manejar el evento DISCONNECT.
    }
}