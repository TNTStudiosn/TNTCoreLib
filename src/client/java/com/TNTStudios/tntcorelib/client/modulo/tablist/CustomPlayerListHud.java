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
 * AHORA la paginación está sincronizada con los ticks del cliente para un rendimiento estable.
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
    private static final int HEADER_HEIGHT = 22;
    private static final int FOOTER_HEIGHT = 20;
    private static final int BORDER_PADDING = 8;

    // Comparador para ordenar a los jugadores como lo hace el juego base.
    private static final Comparator<PlayerListEntry> ENTRY_ORDERING = Comparator
            .comparingInt((PlayerListEntry entry) -> entry.getGameMode() == GameMode.SPECTATOR ? 1 : 0)
            .thenComparing(entry -> entry.getScoreboardTeam() != null ? entry.getScoreboardTeam().getName() : "")
            .thenComparing(entry -> entry.getProfile().getName(), String::compareToIgnoreCase);

    // --- MI NUEVA LÓGICA DE TIEMPO BASADA EN TICKS ---
    // He reemplazado el sistema de tiempo basado en milisegundos por uno de ticks
    // para asegurar que la paginación esté perfectamente sincronizada con el juego.
    private static final int PAGE_SWITCH_DELAY_TICKS = 100; // 5 segundos ($20 \text{ ticks/segundo} \times 5 \text{ segundos}$)
    private static int ticksSinceLastSwitch = 0;

    // Variables para la paginación.
    private static int currentPage = 0;
    private static int totalPages = 0;

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
            String name = "TestPlayer" + (i + 1);
            if (i % 10 == 0) name += "_ConNombreLargo";
            if (i % 25 == 0) name = "UnNombreSuperExtremadamenteLargoParaTestear";
            fakeProfiles.add(new GameProfile(UUID.randomUUID(), name));
        }
    }

    /**
     * Nuevo método que se debe registrar en el evento de tick del cliente.
     * Se encarga de actualizar la página de forma sincronizada con el juego.
     * Lo registraré en mi clase ClientInitializer.
     */
    public static void tick() {
        // Solo proceso la lógica si hay más de una página.
        if (totalPages > 1) {
            ticksSinceLastSwitch++;
            if (ticksSinceLastSwitch >= PAGE_SWITCH_DELAY_TICKS) {
                currentPage = (currentPage + 1) % totalPages; // Avanzo a la siguiente página.
                ticksSinceLastSwitch = 0; // Reseteo el contador.
            }
        } else {
            // Si no hay paginación, me aseguro de que el contador esté reseteado.
            ticksSinceLastSwitch = 0;
        }
    }

    /**
     * El corazón del renderizador. Ahora solo se encarga de dibujar el estado actual.
     * La lógica de paginación se actualiza en el método tick().
     */
    public static void render(DrawContext context, int scaledWindowWidth, Scoreboard scoreboard, ScoreboardObjective objective) {
        // Primero actualizo el estado de la paginación con la información más reciente
        if (isTesting) {
            updatePagingState(fakeProfiles.size());
            renderFromProfiles(context, scaledWindowWidth, fakeProfiles);
        } else {
            List<PlayerListEntry> allPlayers = client.player.networkHandler.getListedPlayerListEntries().stream()
                    .sorted(ENTRY_ORDERING)
                    .collect(Collectors.toList());
            updatePagingState(allPlayers.size());
            renderFromEntries(context, scaledWindowWidth, allPlayers);
        }
    }

    /**
     * Actualiza el estado de la paginación (número total de páginas y página actual).
     * Ya no se encarga de la lógica de tiempo, solo de los cálculos.
     * @param totalItems El número total de jugadores en la lista.
     */
    private static void updatePagingState(int totalItems) {
        if (totalItems <= 0) {
            totalPages = 0;
        } else {
            totalPages = (totalItems + PLAYERS_PER_PAGE - 1) / PLAYERS_PER_PAGE;
        }

        if (totalPages <= 1) {
            currentPage = 0; // Si solo hay una página (o ninguna), siempre es la primera.
        }

        // Me aseguro de que la página actual no se quede fuera de rango si la lista de jugadores cambia.
        currentPage = MathHelper.clamp(currentPage, 0, Math.max(0, totalPages - 1));
    }

    private static void renderFromEntries(DrawContext context, int scaledWindowWidth, List<PlayerListEntry> allPlayers) {
        if (allPlayers.isEmpty()) return;

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

    private static void renderFromProfiles(DrawContext context, int scaledWindowWidth, List<GameProfile> allProfiles) {
        if (allProfiles.isEmpty()) return;

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
        // AQUÍ he modificado la posición Y para bajar todo el cuadro.
        int startY = 35;
        int gridStartX = startX + BORDER_PADDING + (totalWidth - BORDER_PADDING * 2 - gridWidth) / 2;
        int gridStartY = startY + headerSpace;

        return new LayoutInfo(totalWidth, totalHeight, startX, startY, gridStartX, gridStartY);
    }

    private static void drawBackgroundAndHeader(DrawContext context, LayoutInfo layout) {
        context.fill(layout.startX, layout.startY, layout.startX + layout.totalWidth, layout.startY + layout.totalHeight, 0xCC000000); // Fondo oscuro semi-transparente.
        context.drawBorder(layout.startX, layout.startY, layout.totalWidth, layout.totalHeight, 0xFFFFFFFF); // Borde blanco.

        Text title = Text.literal("㙿").formatted(Formatting.WHITE);
        int titleWidth = client.textRenderer.getWidth(title);
        // AQUÍ he ajustado la Y del título para compensar el desplazamiento del cuadro.
        // Originalmente era layout.startY + 7. Como bajé el layout 20px, ahora resto esos 20px.
        // La nueva posición es layout.startY - 13, que resulta en la misma coordenada visual de antes.
        context.drawTextWithShadow(client.textRenderer, title, layout.startX + (layout.totalWidth - titleWidth) / 2, layout.startY - 13, -1);
    }

    private static void drawFooter(DrawContext context, LayoutInfo layout, int totalPlayerCount) {
        int footerY = layout.startY + layout.totalHeight - FOOTER_HEIGHT + 6;

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
        Identifier defaultSkin = new Identifier("textures/entity/player/wide/steve.png");
        PlayerSkinDrawer.draw(context, defaultSkin, x, y, HEAD_SIZE, true, false);
    }

    private static void renderPlayerName(DrawContext context, PlayerListEntry entry, int x, int y, int maxWidth) {
        renderNameText(context, getPlayerName(entry), x, y, maxWidth);
    }

    private static void renderPlayerName(DrawContext context, GameProfile profile, int x, int y, int maxWidth) {
        renderNameText(context, Text.literal(profile.getName()), x, y, maxWidth);
    }

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