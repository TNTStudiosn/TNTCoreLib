// Ubicación: src/client/java/com/TNTStudios/tntcorelib/client/modulo/custommodels/CustomModelsClientHandler.java
package com.TNTStudios.tntcorelib.client.modulo.custommodels;

import com.TNTStudios.tntcorelib.modulo.custommodels.CustomModelsManager;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;

/**
 * Mi manejador del lado cliente para los modelos personalizados.
 * Recibe los paquetes del servidor y ejecuta los comandos de CPM.
 */
public class CustomModelsClientHandler {

    public static void registerReceivers() {
        // Registro el receptor para el paquete "set_model".
        ClientPlayNetworking.registerGlobalReceiver(CustomModelsManager.SET_MODEL_PACKET_ID, (client, handler, buf, responseSender) -> {
            // Leo el nombre del modelo que me envía el servidor.
            String modelName = buf.readString();

            client.execute(() -> {
                if (client.player != null) {
                    // Le digo al mod 'Customizable Player Models' que cambie el modelo de mi jugador.
                    // Esta es una integración indirecta pero efectiva.
                    executeClientCommand(String.format("cpmclient set_model %s.cpmmodel", modelName));
                }
            });
        });

        // Registro el receptor para el paquete "reset_model".
        ClientPlayNetworking.registerGlobalReceiver(CustomModelsManager.RESET_MODEL_PACKET_ID, (client, handler, buf, responseSender) -> {
            client.execute(() -> {
                if (client.player != null) {
                    // Le digo a CPM que restablezca mi modelo al original.
                    executeClientCommand("cpmclient reset_model");
                }
            });
        });
    }

    /**
     * Ejecuta un comando de chat en el cliente.
     * Es la forma en que interactúo con la API de comandos de CPM.
     * @param command El comando a ejecutar.
     */
    private static void executeClientCommand(String command) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.getNetworkHandler() != null) {
            mc.getNetworkHandler().sendChatCommand(command);
        }
    }
}