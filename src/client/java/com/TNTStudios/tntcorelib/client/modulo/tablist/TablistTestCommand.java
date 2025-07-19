package com.TNTStudios.tntcorelib.client.modulo.tablist;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.command.CommandSource;
import net.minecraft.text.Text;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.argument;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;

/**
 * Mi comando de cliente temporal para testear el tablist.
 * Me permite simular una gran cantidad de jugadores para ver el diseño y rendimiento.
 * Uso: /tabtest <cantidad>
 * Para desactivar: /tabtest 0
 */
public class TablistTestCommand {

    public static void register() {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            dispatcher.register(literal("tabtest")
                    .then(argument("cantidad", IntegerArgumentType.integer(0, 500))
                            .executes(context -> {
                                int count = IntegerArgumentType.getInteger(context, "cantidad");
                                CustomPlayerListHud.setupFakePlayers(count);

                                if (count > 0) {
                                    context.getSource().sendFeedback(Text.literal("Tablist en modo de prueba con " + count + " jugadores."));
                                } else {
                                    context.getSource().sendFeedback(Text.literal("Modo de prueba del Tablist desactivado."));
                                }
                                return 1;
                            }))
            );
        });
    }
}