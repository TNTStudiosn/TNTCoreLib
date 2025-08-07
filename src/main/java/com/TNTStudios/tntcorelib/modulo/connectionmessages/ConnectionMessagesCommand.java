// Ubicación: src/main/java/com/TNTStudios/tntcorelib/modulo/connectionmessages/ConnectionMessagesCommand.java
package com.TNTStudios.tntcorelib.modulo.connectionmessages;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

/**
 * Mi comando para gestionar los mensajes de conexión.
 * Permite a los administradores activar o desactivar los mensajes de join/leave.
 */
public class ConnectionMessagesCommand {

    public void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager.literal("tntconnection")
                .requires(source -> source.hasPermissionLevel(4))
                .then(CommandManager.literal("enable")
                        .executes(context -> {
                            ConnectionMessagesHandler.getConfig().messagesEnabled = true;
                            ConnectionMessagesHandler.getConfig().save();
                            context.getSource().sendFeedback(() -> Text.literal("Los mensajes de conexión han sido activados."), true);
                            return 1;
                        }))
                .then(CommandManager.literal("disable")
                        .executes(context -> {
                            ConnectionMessagesHandler.getConfig().messagesEnabled = false;
                            ConnectionMessagesHandler.getConfig().save();
                            context.getSource().sendFeedback(() -> Text.literal("Los mensajes de conexión han sido desactivados."), true);
                            return 1;
                        }))
        );
    }
}