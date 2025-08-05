// Ubicación: src/main/java/com/TNTStudios/tntcorelib/modulo/voicechat/VoiceChatCommand.java
package com.TNTStudios.tntcorelib.modulo.voicechat;

import com.TNTStudios.tntcorelib.Tntcorelib;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException; // Importo la excepción necesaria.
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import java.util.Collection;

/**
 * Mi comando para gestionar el muteo de jugadores a través de PlasmoVoice.
 * Lo he diseñado para ser sencillo y potente, permitiendo silenciar a uno o varios jugadores a la vez.
 * Requiere un nivel de permiso 4 para asegurar que solo los administradores puedan usarlo.
 */
public class VoiceChatCommand {

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager.literal("tntvoice")
                .requires(source -> source.hasPermissionLevel(4))
                .then(CommandManager.literal("mute")
                        .then(CommandManager.argument("targets", EntityArgumentType.players())
                                .executes(VoiceChatCommand::executeMute)
                        )
                )
                .then(CommandManager.literal("unmute")
                        .then(CommandManager.argument("targets", EntityArgumentType.players())
                                .executes(VoiceChatCommand::executeUnmute)
                        )
                )
        );
    }

    // Añado 'throws CommandSyntaxException' a la firma del método para manejar la excepción que puede lanzar getPlayers.
    private static int executeMute(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        Collection<ServerPlayerEntity> targets = EntityArgumentType.getPlayers(context, "targets");
        // Uso mi propia API para mantener el comando limpio y separado de la lógica interna.
        targets.forEach(player -> Tntcorelib.getVoiceChatApi().mutePlayer(player));

        context.getSource().sendFeedback(() -> Text.literal(
                String.format("Se ha silenciado a %d jugador(es) en el chat de voz.", targets.size())
        ), true);
        return targets.size();
    }

    // Hago lo mismo aquí para el comando de desmutear.
    private static int executeUnmute(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        Collection<ServerPlayerEntity> targets = EntityArgumentType.getPlayers(context, "targets");
        targets.forEach(player -> Tntcorelib.getVoiceChatApi().unmutePlayer(player));

        context.getSource().sendFeedback(() -> Text.literal(
                String.format("Se ha quitado el silencio a %d jugador(es) en el chat de voz.", targets.size())
        ), true);
        return targets.size();
    }
}