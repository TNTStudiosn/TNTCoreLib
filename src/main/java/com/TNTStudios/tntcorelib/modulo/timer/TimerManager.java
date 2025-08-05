// Ubicación: src/main/java/com/TNTStudios/tntcorelib/modulo/timer/TimerManager.java
package com.TNTStudios.tntcorelib.modulo.timer;

import com.TNTStudios.tntcorelib.api.timer.TimerApi;
import com.google.common.collect.Sets;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.entity.boss.BossBar;
import net.minecraft.entity.boss.ServerBossBar;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;

/**
 * Mi implementación de la API del temporizador.
 * He refactorizado esta clase para desacoplar la lógica del temporizador de su visibilidad.
 * Ahora controlo exactamente qué jugadores ven la Boss Bar, solucionando el problema
 * de que aparezca al unirse y permitiendo un control mucho más fino.
 */
public class TimerManager implements TimerApi {

    private final MinecraftServer server;
    private final ServerBossBar bossBar;

    // ✅ NUEVO: Un conjunto para rastrear a los jugadores que actualmente ven la barra. Uso UUIDs para persistir a través de desconexiones.
    private final Set<UUID> playersWithVisibleBar = Sets.newConcurrentHashSet();
    // ✅ NUEVO: Un flag para saber si el temporizador debe mostrarse a todos los que se unan.
    private boolean isGlobalVisible = false;

    private boolean isRunning = false;
    private int maxCountdown = 300;
    private int countdown = 300;
    private int tickCounter = 0;
    private Runnable onFinishAction;

    public TimerManager(MinecraftServer server) {
        this.server = server;
        this.bossBar = new ServerBossBar(Text.literal("Temporizador"), BossBar.Color.WHITE, BossBar.Style.PROGRESS);
        // ✅ CORREGIDO: Me aseguro de que la barra esté oculta al crearse. Este era el origen del bug.
        this.bossBar.setVisible(false);

        this.onFinishAction = () -> server.getPlayerManager().broadcast(Text.of("¡El tiempo ha terminado!"), false);
        this.registerServerEvents();
    }

    @Override
    public void start() {
        if (this.isRunning) return;
        this.isRunning = true;
    }

    @Override
    public void pause() {
        this.isRunning = false;
    }

    @Override
    public void stop() {
        this.isRunning = false;
        this.countdown = this.maxCountdown;
        this.hideFromAll(); // Al detener, me aseguro de ocultarlo para todos.
        updateBossBar(); // Actualizo el texto aunque no sea visible.
    }

    @Override
    public void setTime(int seconds) {
        this.maxCountdown = Math.max(1, seconds);
        this.countdown = this.maxCountdown;
        updateBossBar();
    }

    @Override
    public void addTime(int seconds) {
        this.countdown = Math.max(0, this.countdown + seconds);
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
        updateBossBar();
    }

    @Override
    public void setOnFinishAction(Runnable onFinish) {
        this.onFinishAction = onFinish;
    }

    // --- NUEVOS MÉTODOS DE VISIBILIDAD ---

    @Override
    public void show(ServerPlayerEntity player) {
        if (player == null) return;
        if (this.playersWithVisibleBar.add(player.getUuid())) {
            this.bossBar.addPlayer(player);
        }
        // Si la barra no era visible, la activo porque ahora al menos un jugador la ve.
        if (!this.bossBar.isVisible() && !this.playersWithVisibleBar.isEmpty()) {
            this.bossBar.setVisible(true);
        }
    }

    @Override
    public void hide(ServerPlayerEntity player) {
        if (player == null) return;
        if (this.playersWithVisibleBar.remove(player.getUuid())) {
            this.bossBar.removePlayer(player);
        }
        // Si ya no quedan jugadores viendo la barra, la desactivo globalmente para optimizar.
        if (this.bossBar.isVisible() && this.playersWithVisibleBar.isEmpty()) {
            this.bossBar.setVisible(false);
            this.isGlobalVisible = false; // Si se vacía, desactivamos el modo global.
        }
    }

    @Override
    public void showToAll() {
        this.isGlobalVisible = true;
        this.server.getPlayerManager().getPlayerList().forEach(this::show);
    }

    @Override
    public void hideFromAll() {
        this.isGlobalVisible = false;
        // Limpio la lista de jugadores que ven la barra y luego la oculto.
        this.bossBar.clearPlayers();
        this.playersWithVisibleBar.clear();
        this.bossBar.setVisible(false);
    }

    @Override
    public boolean isVisibleTo(ServerPlayerEntity player) {
        return player != null && this.playersWithVisibleBar.contains(player.getUuid());
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
                if (this.onFinishAction != null) {
                    this.onFinishAction.run();
                }
                stop();
            }
        }
    }

    private void updateBossBar() {
        String timeFormatted = String.format("%d:%02d", countdown / 60, countdown % 60);
        this.bossBar.setName(Text.of("Tiempo restante: " + timeFormatted));
        this.bossBar.setPercent(this.maxCountdown > 0 ? (float) this.countdown / this.maxCountdown : 0);
    }

    private void registerServerEvents() {
        ServerTickEvents.START_SERVER_TICK.register(this::onServerTick);

        // ✅ MEJORA: Cuando un jugador se une, compruebo si el modo "global" está activo para mostrarle la barra.
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            if (this.isGlobalVisible) {
                this.show(handler.getPlayer());
            }
        });

        // ✅ MEJORA: Limpio la referencia del jugador que se desconecta para evitar memory leaks.
        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> {
            // No necesito llamar a hide(), solo quitarlo de mi lista de seguimiento.
            // La BossBar de Minecraft ya lo elimina de sus listeners automáticamente.
            this.playersWithVisibleBar.remove(handler.getPlayer().getUuid());
        });
    }
}