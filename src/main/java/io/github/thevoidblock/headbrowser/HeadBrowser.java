package io.github.thevoidblock.headbrowser;

import io.github.thevoidblock.headbrowser.gui.BrowseScreen;
import io.github.thevoidblock.headbrowser.gui.BrowserChildCreativeInventoryScreen;
import io.github.thevoidblock.headbrowser.gui.ErrorScreen;
import io.github.thevoidblock.headbrowser.gui.widget.BrowseHeadsButton;
import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.world.GameMode;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.io.File;

import static java.lang.String.format;
import static java.lang.System.currentTimeMillis;

public class HeadBrowser implements ClientModInitializer {
    public static final String MOD_ID = "headbrowser";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    public static final MinecraftClient CLIENT = MinecraftClient.getInstance();
    public static final String ISSUES_URL = "https://github.com/TheVoidBlock/HeadBrowser/issues/new";
    public static final io.github.thevoidblock.headbrowser.HeadBrowserConfig CONFIG = io.github.thevoidblock.headbrowser.HeadBrowserConfig.createAndLoad();
    public static final File MOD_FOLDER = new File(CLIENT.runDirectory, MOD_ID);

    public static final int BROWSE_BUTTON_OFFSET = 4;
    public static final Dimension BROWSE_BUTTON_DIMENSIONS = new Dimension(20, 20);

    @Override
    public void onInitializeClient() {
        if(CONFIG.modEnabled()) {
            if(!MinecraftHeadsAPI.readHeads()) {
                MinecraftHeadsAPI.downloadAndSaveHeads();
            } else if(currentTimeMillis() - MinecraftHeadsAPI.HEADS.downloadTime > CONFIG.cacheExpirationTime() * 1000L) {
                LOGGER.info("Heads cache expired. Downloading new heads");
                MinecraftHeadsAPI.downloadAndSaveHeads();
            }
        }

        KeyBindings.registerBindFunctions();
        ClientTickScheduler.register();
    }

    private static void presentError(String message, String error) {
        CLIENT.setScreen(new ErrorScreen(message, error));
    }

    public static void error(String message, Exception e) {
        LOGGER.error(message, e);
        if(CLIENT.isFinishedLoading()) presentError(message, e.toString());
    }

    /**
     * Vanilla Finest port: heads can only be taken in creative, also in singleplayer (upstream allows it in
     * every game mode there, which made the browser a way to cheat in survival worlds).
     */
    public static boolean canGetHeadItem() {
        return CLIENT.player != null && CLIENT.player.getGameMode() == GameMode.CREATIVE;
    }

    /**
     * Vanilla Finest port: in a world the browser is only offered in creative (pause-screen button, key).
     * Outside a world (title screen) it is always available.
     */
    public static boolean usableHere() {
        return CLIENT.player == null || CLIENT.player.getGameMode() == GameMode.CREATIVE;
    }

    public static void getItem(ItemStack item) {
        ClientPlayerEntity player = CLIENT.player;
        if(player == null) return;
        if(KeyBindings.isKeyPressed(GLFW.GLFW_KEY_LEFT_ALT)) CLIENT.setScreen(new BrowserChildCreativeInventoryScreen(CLIENT, CLIENT.currentScreen));
        else CLIENT.setScreen(new InventoryScreen(player));
        player.currentScreenHandler.setCursorStack(item);

        if(CLIENT.isConnectedToLocalServer()) {
            MinecraftServer server = CLIENT.getServer();
            assert server != null;
            ServerPlayerEntity serverPlayer = server.getPlayerManager().getPlayer(player.getUuid());
            assert serverPlayer != null;
            serverPlayer.currentScreenHandler.setCursorStack(item);
        }
    }

    public static ButtonWidget createSquareBrowseButton(int anchorButtonX, int anchorButtonWidth, int y) {
        return new BrowseHeadsButton(
                anchorButtonX + anchorButtonWidth + BROWSE_BUTTON_OFFSET + CONFIG.titleButtonHorizontalOffset() * CONFIG.offsetMultiplier(),
                y + CONFIG.titleButtonVerticalOffset() * CONFIG.offsetMultiplier(),
                BROWSE_BUTTON_DIMENSIONS.width,
                Text.empty(),
                button -> BrowseScreen.open(CLIENT),
                MinecraftHeadsAPI.HEADS.getRandomHead()
        );
    }

    public static ButtonWidget createWideBrowseButton(int width) {
        return new BrowseHeadsButton(
                0,
                0,
                width,
                Text.translatable(format("menu.%s.wide-browse-button", MOD_ID)),
                button -> BrowseScreen.open(CLIENT),
                MinecraftHeadsAPI.HEADS.getRandomHead()
        );
    }
}
