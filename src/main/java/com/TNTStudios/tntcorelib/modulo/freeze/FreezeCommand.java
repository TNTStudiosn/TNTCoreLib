// Ubicación: src/main/java/com/TNTStudios/tntcorelib/modulo/freeze/FreezeCommand.java
package com.TNTStudios.tntcorelib.modulo.freeze;

import com.TNTStudios.tntcorelib.Tntcorelib;
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
 * Mi comando para congelar y descongelar jugadores.
 * Requiere nivel 4 de OP.
 */
public class FreezeCommand {

    public void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager.literal("tntfreeze")
                .requires(source -> source.hasPermissionLevel(4))
                .then(CommandManager.literal("on")
                        .then(CommandManager.argument("targets", EntityArgumentType.players())
                                .executes(this::executeFreeze)))
                .then(CommandManager.literal("off")
                        .then(CommandManager.argument("targets", EntityArgumentType.players())
                                .executes(this::executeUnfreeze)))
        );
    }

    private int executeFreeze(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        Collection<ServerPlayerEntity> targets = EntityArgumentType.getPlayers(context, "targets");
        Tntcorelib.getFreezeApi().freezePlayer(targets);
        context.getSource().sendFeedback(() -> Text.literal("Congelando a " + targets.size() + " jugador(es)."), true);
        return targets.size();
    }

    private int executeUnfreeze(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        Collection<ServerPlayerEntity> targets = EntityArgumentType.getPlayers(context, "targets");
        Tntcorelib.getFreezeApi().unfreezePlayer(targets);
        context.getSource().sendFeedback(() -> Text.literal("Descongelando a " + targets.size() + " jugador(es)."), true);
        return targets.size();
    }
}