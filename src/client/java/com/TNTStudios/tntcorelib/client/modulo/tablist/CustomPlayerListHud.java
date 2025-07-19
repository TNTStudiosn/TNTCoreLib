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
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.GameMode;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Mi clase para renderizar el nuevo y elegante Tablist.
 * Ha sido rediseñada para ser más atractiva, con paginación automática y un modo de prueba.
 */
public class CustomPlayerListHud {

    private static final MinecraftClient client = MinecraftClient.getInstance();

    // Defino las constantes de mi nuevo diseño para hacer ajustes fáciles.
    private static final int PLAYERS_PER_ROW = 5;
    private static final int ROWS_PER_PAGE = 6;
    private static final int PLAYERS_PER_PAGE = PLAYERS_PER_ROW * ROWS_PER_PAGE;

    private static final int CELL_WIDTH = 110;
    private static final int CELL_HEIGHT = 30;
    private static final int CELL_PADDING_X = 8;
    private static final int CELL_PADDING_Y = 8;
    private static final int HEAD_SIZE = 24;

    // Constantes para el layout general.
    private static final int HEADER_HEIGHT = 22;
    private static final int FOOTER_HEIGHT = 20;
    private static final int BORDER_PADDING = 8;

    // Comparador para ordenar a los jugadores como lo hace el juego base.
    private static final Comparator<PlayerListEntry> ENTRY_ORDERING = Comparator
            .comparingInt((PlayerListEntry entry) -> entry.getGameMode() == GameMode.SPECTATOR ? 1 : 0)
            .thenComparing(entry -> entry.getScoreboardTeam() != null ? entry.getScoreboardTeam().getName() : "")
            .thenComparing(entry -> entry.getProfile().getName(), String::compareToIgnoreCase);

    // Variables para la paginación automática.
    private static int currentPage = 0;
    private static int totalPages = 0;
    private static long lastPageChangeTime = 0L;
    private static final long PAGE_SWITCH_DELAY_MS = 5000; // 5 segundos por página.

    // Variables para el testeo de jugadores.
    private static boolean isTesting = false;
    private static final List<GameProfile> fakeProfiles = new ArrayList<>();

    /**
     * Prepara una lista de perfiles falsos para probar el tablist.
     * Esto lo llamaré desde un comando temporal.
     * @param count El número de jugadores falsos a crear. Si es 0, desactiva el modo test.
     */
    public static void setupFakePlayers(int count) {
        fakeProfiles.clear();
        if (count <= 0) {
            isTesting = false;
            return;
        }
        isTesting = true;
        for (int i = 0; i < count; i++) {
            // Creo perfiles con nombres variados para probar el escalado de texto.
            String name = "TestPlayer" + (i + 1);
            if (i % 10 == 0) name += "_ConNombreLargo";
            if (i % 25 == 0) name = "UnNombreSuperExtremadamenteLargoParaTestear";
            fakeProfiles.add(new GameProfile(UUID.randomUUID(), name));
        }
    }

    /**
     * El corazón del renderizador. Este método se llama desde mi Mixin.
     * Ahora decide si renderizar la lista real o la de prueba.
     */
    public static void render(DrawContext context, int scaledWindowWidth, Scoreboard scoreboard, ScoreboardObjective objective) {
        if (isTesting) {
            renderFromProfiles(context, scaledWindowWidth, fakeProfiles);
        } else {
            List<PlayerListEntry> allPlayers = client.player.networkHandler.getListedPlayerListEntries().stream()
                    .sorted(ENTRY_ORDERING)
                    .collect(Collectors.toList());
            renderFromEntries(context, scaledWindowWidth, allPlayers);
        }
    }

    /**
     * Lógica de renderizado para la lista de jugadores reales (PlayerListEntry).
     */
    private static void renderFromEntries(DrawContext context, int scaledWindowWidth, List<PlayerListEntry> allPlayers) {
        if (allPlayers.isEmpty()) return;

        updatePaging(allPlayers.size());

        int startIndex = currentPage * PLAYERS_PER_PAGE;
        int endIndex = Math.min(startIndex + PLAYERS_PER_PAGE, allPlayers.size());
        List<PlayerListEntry> playersToShow = allPlayers.subList(startIndex, endIndex);

        LayoutInfo layout = calculateLayout(scaledWindowWidth, playersToShow.size());
        drawBackgroundAndHeader(context, layout);

        for (int i = 0; i < playersToShow.size(); i++) {
            PlayerListEntry entry = playersToShow.get(i);
            int row = i / PLAYERS_PER_ROW;
            int col = i % PLAYERS_PER_ROW;
            int cellX = layout.gridStartX + col * (CELL_WIDTH + CELL_PADDING_X);
            int cellY = layout.gridStartY + row * (CELL_HEIGHT + CELL_PADDING_Y);

            renderPlayerHead(context, entry, cellX + (CELL_WIDTH - HEAD_SIZE) / 2, cellY);
            renderPlayerName(context, entry, cellX, cellY + HEAD_SIZE, CELL_WIDTH);
        }
        drawFooter(context, layout, allPlayers.size());
    }

    /**
     * Lógica de renderizado para la lista de prueba (GameProfile).
     */
    private static void renderFromProfiles(DrawContext context, int scaledWindowWidth, List<GameProfile> allProfiles) {
        if (allProfiles.isEmpty()) return;

        updatePaging(allProfiles.size());

        int startIndex = currentPage * PLAYERS_PER_PAGE;
        int endIndex = Math.min(startIndex + PLAYERS_PER_PAGE, allProfiles.size());
        List<GameProfile> profilesToShow = allProfiles.subList(startIndex, endIndex);

        LayoutInfo layout = calculateLayout(scaledWindowWidth, profilesToShow.size());
        drawBackgroundAndHeader(context, layout);

        for (int i = 0; i < profilesToShow.size(); i++) {
            GameProfile profile = profilesToShow.get(i);
            int row = i / PLAYERS_PER_ROW;
            int col = i % PLAYERS_PER_ROW;
            int cellX = layout.gridStartX + col * (CELL_WIDTH + CELL_PADDING_X);
            int cellY = layout.gridStartY + row * (CELL_HEIGHT + CELL_PADDING_Y);

            renderDefaultHead(context, cellX + (CELL_WIDTH - HEAD_SIZE) / 2, cellY);
            renderPlayerName(context, profile, cellX, cellY + HEAD_SIZE, CELL_WIDTH);
        }
        drawFooter(context, layout, allProfiles.size());
    }

    /**
     * Actualiza la página actual basado en el tiempo transcurrido.
     * @param totalItems El número total de jugadores en la lista.
     */
    private static void updatePaging(int totalItems) {
        totalPages = (totalItems + PLAYERS_PER_PAGE - 1) / PLAYERS_PER_PAGE;
        if (totalPages > 1) {
            long currentTime = System.currentTimeMillis();
            if (lastPageChangeTime == 0L) lastPageChangeTime = currentTime; // Inicializo el timer.
            if (currentTime - lastPageChangeTime > PAGE_SWITCH_DELAY_MS) {
                currentPage = (currentPage + 1) % totalPages; // Avanzo a la siguiente página.
                lastPageChangeTime = currentTime; // Reseteo el timer.
            }
        } else {
            currentPage = 0;
            lastPageChangeTime = 0L; // Reseteo si solo hay una página.
        }
        currentPage = MathHelper.clamp(currentPage, 0, Math.max(0, totalPages - 1));
    }

    private record LayoutInfo(int totalWidth, int totalHeight, int startX, int startY, int gridStartX, int gridStartY) {}

    private static LayoutInfo calculateLayout(int scaledWindowWidth, int playersOnPage) {
        int gridColumns = Math.min(playersOnPage, PLAYERS_PER_ROW);
        int gridRows = (playersOnPage + PLAYERS_PER_ROW - 1) / PLAYERS_PER_ROW;
        if (playersOnPage == 0) gridRows = 0;

        int gridWidth = gridColumns > 0 ? gridColumns * (CELL_WIDTH + CELL_PADDING_X) - CELL_PADDING_X : 0;
        int gridHeight = gridRows > 0 ? gridRows * (CELL_HEIGHT + CELL_PADDING_Y) - CELL_PADDING_Y : 0;

        int totalWidth = Math.max(gridWidth, 300) + BORDER_PADDING * 2;
        int headerSpace = HEADER_HEIGHT + (gridRows > 0 ? BORDER_PADDING / 2 : 0);
        int footerSpace = FOOTER_HEIGHT + BORDER_PADDING;
        int totalHeight = headerSpace + gridHeight + footerSpace;

        int startX = (scaledWindowWidth - totalWidth) / 2;
        int startY = 20;
        int gridStartX = startX + BORDER_PADDING + (totalWidth - BORDER_PADDING * 2 - gridWidth) / 2;
        int gridStartY = startY + headerSpace;

        return new LayoutInfo(totalWidth, totalHeight, startX, startY, gridStartX, gridStartY);
    }

    private static void drawBackgroundAndHeader(DrawContext context, LayoutInfo layout) {
        context.fill(layout.startX, layout.startY, layout.startX + layout.totalWidth, layout.startY + layout.totalHeight, 0xCC000000); // Fondo oscuro semi-transparente.
        context.drawBorder(layout.startX, layout.startY, layout.totalWidth, layout.totalHeight, 0xFFFFFFFF); // Borde blanco.

        Text title = Text.literal("JUGADORES CONECTADOS").formatted(Formatting.BOLD, Formatting.WHITE);
        int titleWidth = client.textRenderer.getWidth(title);
        context.drawTextWithShadow(client.textRenderer, title, layout.startX + (layout.totalWidth - titleWidth) / 2, layout.startY + 7, -1);
    }

    private static void drawFooter(DrawContext context, LayoutInfo layout, int totalPlayerCount) {
        int footerY = layout.startY + layout.totalHeight - FOOTER_HEIGHT + 6;

        // CORRECCIÓN: El método getMaxPlayers() no existe en ClientPlayNetworkHandler.
        // He eliminado la parte que muestra el máximo de jugadores para evitar el error.
        String playerCountText = isTesting ? "Jugadores (Test): " + totalPlayerCount
                : "Jugadores: " + totalPlayerCount;
        context.drawTextWithShadow(client.textRenderer, playerCountText, layout.startX + BORDER_PADDING, footerY, -1);

        if (totalPages > 1) {
            String pageText = "Página " + (currentPage + 1) + " / " + totalPages;
            int pageTextWidth = client.textRenderer.getWidth(pageText);
            context.drawTextWithShadow(client.textRenderer, pageText, layout.startX + layout.totalWidth - pageTextWidth - BORDER_PADDING, footerY, -1);
        }
    }

    private static void renderPlayerHead(DrawContext context, PlayerListEntry entry, int x, int y) {
        GameProfile profile = entry.getProfile();
        boolean hat = client.world.getPlayerByUuid(profile.getId()) != null &&
                client.world.getPlayerByUuid(profile.getId()).isPartVisible(PlayerModelPart.HAT);
        PlayerSkinDrawer.draw(context, entry.getSkinTexture(), x, y, HEAD_SIZE, hat, false);
    }

    private static void renderDefaultHead(DrawContext context, int x, int y) {
        // CORRECCIÓN: Usamos el Identifier directo de la skin de Steve.
        // Esta es la forma más compatible y evita problemas entre versiones.
        Identifier defaultSkin = new Identifier("textures/entity/player/wide/steve.png");
        PlayerSkinDrawer.draw(context, defaultSkin, x, y, HEAD_SIZE, true, false);
    }

    private static void renderPlayerName(DrawContext context, PlayerListEntry entry, int x, int y, int maxWidth) {
        renderNameText(context, getPlayerName(entry), x, y, maxWidth);
    }

    private static void renderPlayerName(DrawContext context, GameProfile profile, int x, int y, int maxWidth) {
        renderNameText(context, Text.literal(profile.getName()), x, y, maxWidth);
    }

    /**
     * Renderiza el texto del nombre. Si es muy ancho, lo escala para que quepa.
     * Así me aseguro de que el nombre completo siempre sea visible.
     */
    private static void renderNameText(DrawContext context, Text displayName, int x, int y, int maxWidth) {
        float scale = 1.0f;
        int nameWidth = client.textRenderer.getWidth(displayName);
        if (nameWidth > maxWidth) {
            scale = (float) maxWidth / nameWidth;
        }

        int centerX = x + maxWidth / 2;
        int centerY = y + 4;

        context.getMatrices().push();
        context.getMatrices().translate(centerX, centerY, 0);
        context.getMatrices().scale(scale, scale, 1.0f);
        context.drawTextWithShadow(client.textRenderer, displayName, -nameWidth / 2, 0, -1);
        context.getMatrices().pop();
    }

    private static Text getPlayerName(PlayerListEntry entry) {
        MutableText name = entry.getDisplayName() != null ?
                Text.literal("").append(entry.getDisplayName()) :
                Team.decorateName(entry.getScoreboardTeam(), Text.literal(entry.getProfile().getName()));

        return entry.getGameMode() == GameMode.SPECTATOR ? name.formatted(Formatting.ITALIC) : name;
    }
}