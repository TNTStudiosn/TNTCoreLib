// Ubicación: src/main/java/com/TNTStudios/tntcorelib/api/timer/TimerApi.java
package com.TNTStudios.tntcorelib.api.timer;

import net.minecraft.entity.boss.BossBar;
import net.minecraft.text.Text;

import java.util.function.Consumer;

/**
 * Mi API para controlar un temporizador global en el servidor.
 * Permite a otros mods iniciar, pausar, detener y configurar el temporizador
 * de una manera segura y controlada.
 */
public interface TimerApi {

    /**
     * Inicia o reanuda el temporizador con el tiempo restante actual.
     * Muestra la Boss Bar a todos los jugadores conectados.
     */
    void start();

    /**
     * Pausa el temporizador. La Boss Bar permanecerá visible pero dejará de avanzar.
     */
    void pause();

    /**
     * Detiene el temporizador, lo oculta y reinicia el tiempo a su último valor configurado.
     */
    void stop();

    /**
     * Establece la duración total del temporizador en segundos.
     * Si el temporizador está en marcha, continuará con el nuevo tiempo.
     *
     * @param seconds La nueva duración en segundos.
     */
    void setTime(int seconds);

    /**
     * Añade segundos al tiempo actual.
     *
     * @param seconds Segundos para añadir (puede ser negativo para restar).
     */
    void addTime(int seconds);

    /**
     * Obtiene el tiempo restante en segundos.
     *
     * @return El número de segundos restantes.
     */
    int getTimeRemaining();

    /**
     * Comprueba si el temporizador está actualmente en funcionamiento (no pausado).
     *
     * @return true si el temporizador está activo, false en caso contrario.
     */
    boolean isRunning();

    /**
     * Permite personalizar la Boss Bar.
     * Por ejemplo, para cambiar el título, color o estilo.
     *
     * @param consumer Un consumidor que recibe la instancia de ServerBossBar para modificarla.
     */
    void styleBossBar(Consumer<BossBar> consumer);

    /**
     * Permite definir una acción que se ejecutará cuando el temporizador llegue a cero.
     *
     * @param onFinish La acción a ejecutar.
     */
    void setOnFinishAction(Runnable onFinish);
}