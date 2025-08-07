// Ubicación: src/main/java/com/TNTStudios/tntcorelib/modulo/freeze/FreezeConfig.java
package com.TNTStudios.tntcorelib.modulo.freeze;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import net.fabricmc.loader.api.FabricLoader;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Path;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Mi configuración para el módulo de Freeze.
 * Se encarga de guardar y cargar la lista de jugadores congelados.
 * AHORA: Usa un "dirty flag" para evitar guardados innecesarios y lo hace de forma asíncrona.
 */
public class FreezeConfig {

    public Set<UUID> frozenPlayers = ConcurrentHashMap.newKeySet();

    // transient para que Gson no lo guarde en el JSON.
    // AtomicBoolean es ideal para la seguridad entre hilos (thread-safety).
    private transient final AtomicBoolean dirty = new AtomicBoolean(false);

    private static final Path CONFIG_PATH = FabricLoader.getInstance().getConfigDir().resolve("TNTCore/Freeze/frozen_players.json");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Type TYPE = new TypeToken<FreezeConfig>() {}.getType();

    /**
     * Marco la configuración como "sucia", indicando que hay cambios sin guardar.
     */
    public void markDirty() {
        this.dirty.set(true);
    }

    /**
     * Mi nuevo método de guardado. Puede ejecutarse de forma síncrona o asíncrona.
     * Solo escribe en disco si hay cambios pendientes (dirty == true).
     * @param async Si es true, la operación de guardado se ejecutará en un hilo separado
     * para no bloquear el hilo principal del servidor.
     */
    public void save(boolean async) {
        // Si no hay cambios, no hago nada. ¡Primera optimización clave!
        if (!dirty.getAndSet(false)) {
            return;
        }

        // Copio el estado actual para evitar problemas de concurrencia si se modifica mientras se guarda.
        Set<UUID> playersToSave = Set.copyOf(frozenPlayers);

        Runnable saveTask = () -> {
            try {
                File configFile = CONFIG_PATH.toFile();
                if (configFile.getParentFile() != null) {
                    configFile.getParentFile().mkdirs();
                }
                try (FileWriter writer = new FileWriter(configFile)) {
                    // Uso la copia segura para escribir en el archivo.
                    GSON.toJson(Map.of("frozenPlayers", playersToSave), writer);
                }
            } catch (IOException e) {
                System.err.println("[TNTCoreLib] No pude guardar la configuración de jugadores congelados: " + e.getMessage());
                // Si falla el guardado, marco como sucio de nuevo para reintentar más tarde.
                dirty.set(true);
            }
        };

        if (async) {
            // Ejecuto la tarea en un nuevo hilo para no causar lag.
            new Thread(saveTask, "TNTCoreLib-FreezeConfig-Saver").start();
        } else {
            // Ejecuto la tarea en el hilo actual (usado para el apagado del servidor).
            saveTask.run();
        }
    }

    public static FreezeConfig load() {
        File configFile = CONFIG_PATH.toFile();
        if (!configFile.exists()) {
            return new FreezeConfig(); // No es necesario guardar un archivo vacío al inicio.
        }

        try (FileReader reader = new FileReader(configFile)) {
            // Gson deserializa a un tipo genérico de Set (como LinkedHashSet), que no es thread-safe.
            FreezeConfig config = GSON.fromJson(reader, TYPE);

            if (config == null || config.frozenPlayers == null) {
                return new FreezeConfig();
            }

            // Mi corrección: Aseguro que la colección sea thread-safe después de cargarla.
            // 1. Creo un nuevo Set thread-safe vacío.
            Set<UUID> threadSafeSet = ConcurrentHashMap.newKeySet();
            // 2. Añado todos los jugadores cargados desde el archivo JSON al nuevo Set.
            threadSafeSet.addAll(config.frozenPlayers);
            // 3. Asigno el nuevo Set thread-safe a mi instancia de configuración.
            config.frozenPlayers = threadSafeSet;

            return config;
        } catch (IOException e) {
            System.err.println("[TNTCoreLib] No pude leer la configuración de jugadores congelados, usando valores por defecto: " + e.getMessage());
            return new FreezeConfig();
        }
    }
}