// Ubicación: src/main/java/com/TNTStudios/tntcorelib/modulo/chatcontrol/ChatControlCommand.java
package com.TNTStudios.tntcorelib.modulo.chatcontrol;

import com.TNTStudios.tntcorelib.Tntcorelib;
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

/**
 * Mi comando para gestionar el estado del chat global.
 * Permite a los administradores (nivel 4) activar o desactivar el chat para el resto de jugadores.
 */
public class ChatControlCommand {

    public void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager.literal("tntchat")
                .requires(source -> source.hasPermissionLevel(4))
                .then(CommandManager.literal("on")
                        .executes(context -> {
                            Tntcorelib.getChatControlApi().setChatMuted(false);
                            context.getSource().sendFeedback(() -> Text.literal("§aEl chat global ha sido activado."), true);
                            return 1;
                        }))
                .then(CommandManager.literal("off")
                        .executes(context -> {
                            Tntcorelib.getChatControlApi().setChatMuted(true);
                            context.getSource().sendFeedback(() -> Text.literal("§cEl chat global ha sido desactivado."), true);
                            return 1;
                        }))
        );
    }
}