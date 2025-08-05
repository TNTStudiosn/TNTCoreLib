// Ubicación: src/main/java/com/TNTStudios/tntcorelib/modulo/voicechat/VoiceChatCommand.java
package com.TNTStudios.tntcorelib.modulo.voicechat;

import com.TNTStudios.tntcorelib.Tntcorelib; // Importo la clase principal para obtener la API
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import java.util.Collection;

/**
 * Mi comando para gestionar el muteo, ahora totalmente independiente.
 * No almacena ninguna API, simplemente la solicita a TNTCoreLib cuando la necesita.
 * Esto lo hace robusto y desacoplado del ciclo de vida de PlasmoVoice.
 */
public class VoiceChatCommand {

    // Ya no necesito un constructor ni una variable para la API.

    public void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager.literal("tntvoice")
                .requires(source -> source.hasPermissionLevel(4))
                .then(CommandManager.literal("mute")
                        .then(CommandManager.argument("targets", EntityArgumentType.players())
                                .executes(this::executeMute)
                        )
                )
                .then(CommandManager.literal("unmute")
                        .then(CommandManager.argument("targets", EntityArgumentType.players())
                                .executes(this::executeUnmute)
                        )
                )
        );
    }

    private int executeMute(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        Collection<ServerPlayerEntity> targets = EntityArgumentType.getPlayers(context, "targets");
        ServerCommandSource source = context.getSource();

        // Obtengo la API directamente de la clase principal.
        // Si PlasmoVoice no está, obtendré la implementación "dummy".
        targets.forEach(player -> Tntcorelib.getVoiceChatApi().mutePlayer(player));

        // El feedback sigue siendo el mismo. Si la API es la "dummy", cada jugador recibirá un mensaje de error individual.
        source.sendFeedback(() -> Text.literal(
                String.format("Se ha silenciado a %d jugador(es) en el chat de voz.", targets.size())
        ), true);
        return targets.size();
    }

    private int executeUnmute(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        Collection<ServerPlayerEntity> targets = EntityArgumentType.getPlayers(context, "targets");
        ServerCommandSource source = context.getSource();

        targets.forEach(player -> Tntcorelib.getVoiceChatApi().unmutePlayer(player));

        source.sendFeedback(() -> Text.literal(
                String.format("Se ha quitado el silencio a %d jugador(es) en el chat de voz.", targets.size())
        ), true);
        return targets.size();
    }
}