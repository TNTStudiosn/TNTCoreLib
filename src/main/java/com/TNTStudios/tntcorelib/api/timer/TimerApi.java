// Ubicación: src/main/java/com/TNTStudios/tntcorelib/api/timer/TimerApi.java
package com.TNTStudios.tntcorelib.api.timer;

import net.minecraft.entity.boss.BossBar;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.function.Consumer;

/**
 * Mi API para controlar un temporizador en el servidor.
 * La he extendido para permitir controlar la visibilidad por jugador o globalmente,
 * dándole mucho más poder y flexibilidad a otros módulos.
 */
public interface TimerApi {

    /**
     * Inicia o reanuda la cuenta atrás del temporizador.
     * OJO: Esto no muestra la Boss Bar por sí solo. Usa show() o showToAll().
     */
    void start();

    /**
     * Pausa la cuenta atrás del temporizador. No afecta la visibilidad.
     */
    void pause();

    /**
     * Detiene el temporizador, reinicia el tiempo y lo oculta de todos los jugadores.
     */
    void stop();

    /**
     * Establece la duración total del temporizador en segundos.
     * @param seconds La nueva duración en segundos.
     */
    void setTime(int seconds);

    /**
     * Añade o resta segundos al tiempo actual.
     * @param seconds Segundos para añadir (puede ser negativo para restar).
     */
    void addTime(int seconds);

    /**
     * Obtiene el tiempo restante en segundos.
     * @return El número de segundos restantes.
     */
    int getTimeRemaining();

    /**
     * Comprueba si el temporizador está actualmente en funcionamiento (no pausado).
     * @return true si el temporizador está activo, false en caso contrario.
     */
    boolean isRunning();

    /**
     * Permite personalizar el estilo de la Boss Bar.
     * @param styleConsumer Un consumidor que recibe la instancia de BossBar para modificarla.
     */
    void styleBossBar(Consumer<BossBar> styleConsumer);

    /**
     * Permite definir una acción que se ejecutará cuando el temporizador llegue a cero.
     * @param onFinish La acción a ejecutar.
     */
    void setOnFinishAction(Runnable onFinish);

    // --- NUEVOS MÉTODOS DE LA API DE VISIBILIDAD ---

    /**
     * Muestra la Boss Bar del temporizador a un jugador específico.
     * @param player El jugador que verá el temporizador.
     */
    void show(ServerPlayerEntity player);

    /**
     * Oculta la Boss Bar del temporizador a un jugador específico.
     * @param player El jugador que dejará de ver el temporizador.
     */
    void hide(ServerPlayerEntity player);

    /**
     * Muestra la Boss Bar a todos los jugadores conectados actualmente
     * y a cualquiera que se conecte mientras este modo esté activo.
     */
    void showToAll();

    /**
     * Oculta la Boss Bar de todos los jugadores y desactiva el modo global.
     */
    void hideFromAll();

    /**
     * Comprueba si un jugador específico está viendo la Boss Bar del temporizador.
     * @param player El jugador a comprobar.
     * @return true si el jugador está viendo la barra, false en caso contrario.
     */
    boolean isVisibleTo(ServerPlayerEntity player);
}