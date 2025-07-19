package com.TNTStudios.tntcorelib.client.modulo.tablist;

import com.mojang.authlib.GameProfile;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.PlayerSkinDrawer;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.client.render.entity.PlayerModelPart;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.ScoreboardObjective;
import net.minecraft.scoreboard.Team;
import net.minecraft.text.MutableText;
import net.minecraft.text.StringVisitable;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.MathHelper;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Mi clase para renderizar el nuevo y elegante Tablist.
 * Está diseñada para ser súper eficiente y soportar cientos de jugadores sin lag.
 */
public class CustomPlayerListHud {

    private static final MinecraftClient client = MinecraftClient.getInstance();

    // Defino las constantes de mi diseño para hacer ajustes fáciles.
    private static final int PLAYERS_PER_ROW = 8; // Jugadores por cada fila.
    private static final int ROWS_PER_PAGE = 12;  // Filas por cada página.
    private static final int PLAYERS_PER_PAGE = PLAYERS_PER_ROW * ROWS_PER_PAGE;

    private static final int CELL_WIDTH = 32;
    private static final int CELL_HEIGHT = 42;
    private static final int CELL_PADDING = 5;
    private static final int HEAD_SIZE = 24;

    // Comparador para ordenar a los jugadores como lo hace el juego base.
    private static final Comparator<PlayerListEntry> ENTRY_ORDERING = Comparator
            .comparingInt((PlayerListEntry entry) -> entry.getGameMode() == net.minecraft.world.GameMode.SPECTATOR ? 1 : 0)
            .thenComparing(entry -> entry.getScoreboardTeam() != null ? entry.getScoreboardTeam().getName() : "")
            .thenComparing(entry -> entry.getProfile().getName(), String::compareToIgnoreCase);

    // Variables para controlar la paginación.
    private static int currentPage = 0;
    private static int totalPages = 0;

    /**
     * El corazón del renderizador. Este método se llama desde mi Mixin.
     */
    public static void render(DrawContext context, int scaledWindowWidth, Scoreboard scoreboard, ScoreboardObjective objective) {
        // Recolecto la lista completa de jugadores, sin el límite de 80 de vanilla.
        List<PlayerListEntry> allPlayers = client.player.networkHandler.getListedPlayerListEntries().stream()
                .sorted(ENTRY_ORDERING)
                .collect(Collectors.toList());

        if (allPlayers.isEmpty()) {
            return;
        }

        // Calculo el número de páginas necesarias.
        totalPages = (allPlayers.size() - 1) / PLAYERS_PER_PAGE + 1;
        currentPage = MathHelper.clamp(currentPage, 0, totalPages - 1);

        // Obtengo solo la lista de jugadores que deben mostrarse en la página actual.
        int startIndex = currentPage * PLAYERS_PER_PAGE;
        int endIndex = Math.min(startIndex + PLAYERS_PER_PAGE, allPlayers.size());
        List<PlayerListEntry> playersToShow = allPlayers.subList(startIndex, endIndex);

        // Calculo las dimensiones y la posición de la cuadrícula para centrarla.
        int gridColumns = Math.min(playersToShow.size(), PLAYERS_PER_ROW);
        int gridRows = (int) Math.ceil((double) playersToShow.size() / PLAYERS_PER_ROW);

        int gridWidth = gridColumns * (CELL_WIDTH + CELL_PADDING) - CELL_PADDING;
        int gridHeight = gridRows * (CELL_HEIGHT + CELL_PADDING) - CELL_PADDING;

        int startX = (scaledWindowWidth - gridWidth) / 2;
        int startY = 30; // Un pequeño margen superior.

        // Dibujo un fondo semi-transparente para toda la cuadrícula, para mejorar la legibilidad.
        int backgroundColor = client.options.getTextBackgroundColor(0.5f);
        context.fill(startX - 5, startY - 5, startX + gridWidth + 5, startY + gridHeight + 5, backgroundColor);

        // Ahora, itero sobre los jugadores de esta página y los dibujo.
        for (int i = 0; i < playersToShow.size(); i++) {
            PlayerListEntry entry = playersToShow.get(i);
            int row = i / PLAYERS_PER_ROW;
            int col = i % PLAYERS_PER_ROW;

            int cellX = startX + col * (CELL_WIDTH + CELL_PADDING);
            int cellY = startY + row * (CELL_HEIGHT + CELL_PADDING);

            // Dibujo la cabeza del jugador.
            renderPlayerHead(context, entry, cellX + (CELL_WIDTH - HEAD_SIZE) / 2, cellY);

            // Dibujo el nombre del jugador debajo de la cabeza.
            renderPlayerName(context, entry, cellX, cellY + HEAD_SIZE + 2, CELL_WIDTH);
        }

        // Finalmente, dibujo la información de la paginación.
        if (totalPages > 1) {
            String pageText = "Página " + (currentPage + 1) + " / " + totalPages + " (Scroll)";
            int textWidth = client.textRenderer.getWidth(pageText);
            context.drawTextWithShadow(client.textRenderer, pageText, (scaledWindowWidth - textWidth) / 2, startY + gridHeight + 10, -1);
        }
    }

    private static void renderPlayerHead(DrawContext context, PlayerListEntry entry, int x, int y) {
        GameProfile profile = entry.getProfile();
        boolean hat = client.world.getPlayerByUuid(profile.getId()) != null &&
                client.world.getPlayerByUuid(profile.getId()).isPartVisible(PlayerModelPart.HAT);

        PlayerSkinDrawer.draw(context, entry.getSkinTexture(), x, y, HEAD_SIZE, hat, false);
    }

    private static void renderPlayerName(DrawContext context, PlayerListEntry entry, int x, int y, int maxWidth) {
        Text displayName = getPlayerName(entry);

        // Trunco el nombre si es demasiado largo para la celda.
        // El método trimToWidth devuelve un StringVisitable, no directamente un Text.
        // Un casteo directo causaría un ClassCastException, como vimos en el error.
        // La forma correcta es obtener el texto truncado como un StringVisitable...
        StringVisitable truncatedVisitable = client.textRenderer.trimToWidth(displayName, maxWidth - 2);

        // ...luego, crear un nuevo objeto Text a partir del string truncado,
        // pero manteniendo el estilo del texto original para no perder colores o formatos.
        Text textToRender = Text.literal(truncatedVisitable.getString()).setStyle(displayName.getStyle());

        // Calculo la posición para centrar el nombre en su celda.
        int nameWidth = client.textRenderer.getWidth(textToRender);
        int nameX = x + (maxWidth - nameWidth) / 2;

        context.drawTextWithShadow(client.textRenderer, textToRender, nameX, y, -1);
    }

    private static Text getPlayerName(PlayerListEntry entry) {
        MutableText name = entry.getDisplayName() != null ?
                entry.getDisplayName().copy() :
                Team.decorateName(entry.getScoreboardTeam(), Text.literal(entry.getProfile().getName()));

        return entry.getGameMode() == net.minecraft.world.GameMode.SPECTATOR ? name.formatted(Formatting.ITALIC) : name;
    }

    /**
     * Este método lo llamaré desde un mixin en la clase Mouse para cambiar de página.
     * @param amount El valor del scroll, positivo o negativo.
     */
    public static void onScroll(double amount) {
        if (amount > 0) {
            currentPage = Math.max(0, currentPage - 1);
        } else if (amount < 0) {
            currentPage = Math.min(totalPages - 1, currentPage + 1);
        }
    }
}