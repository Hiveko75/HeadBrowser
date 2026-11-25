package io.github.thevoidblock.headbrowser;

import io.github.thevoidblock.headbrowser.gui.AlertScreen;
import io.github.thevoidblock.headbrowser.gui.BrowseScreen;
import io.github.thevoidblock.headbrowser.gui.ErrorScreen;
import io.github.thevoidblock.headbrowser.gui.widget.BrowseHeadsButton;
import io.github.thevoidblock.headbrowser.util.ClientTickScheduler;
import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.github.thevoidblock.headbrowser.HeadBrowserConfig;

import java.awt.*;

import static java.lang.String.format;
import static java.lang.System.currentTimeMillis;

public class HeadBrowser implements ClientModInitializer {

    public static final String MOD_ID = "headbrowser";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    public static final MinecraftClient CLIENT = MinecraftClient.getInstance();
    public static final String ISSUES_URL = "https://github.com/TheVoidBlock/HeadBrowser/issues/new";
    public static final HeadBrowserConfig CONFIG = HeadBrowserConfig.createAndLoad();

    public static final int BROWSE_BUTTON_OFFSET = 4;
    public static final Dimension BROWSE_BUTTON_DIMENSIONS = new Dimension(20, 20);

    @Override
    public void onInitializeClient() {

        if(CONFIG.modEnabled()) {
            if (!MinecraftHeadsAPI.readHeads()) {
                MinecraftHeadsAPI.downloadAndSaveHeads();
            }

            if (currentTimeMillis() - MinecraftHeadsAPI.HEADS.downloadTime > CONFIG.cacheExpirationTime()*1000L) {
                LOGGER.info("Heads cache expired. Downloading new heads");
                MinecraftHeadsAPI.downloadAndSaveHeads();
            }
        }

        KeyBindings.registerBindFunctions();
        ClientTickScheduler.register();
    }

    public static void presentError(String message, String error) {
        CLIENT.setScreen(new ErrorScreen(message, error));
    }

    /**
     * Vanilla Finest port: in a world the browser is only offered in creative (pause-screen button, key), and
     * heads are only handed out there. Outside a world (title screen) it is always available.
     */
    public static boolean usableHere() {
        return CLIENT.player == null || CLIENT.player.isCreative();
    }

    public static void getItem(ItemStack item) {
        if(CLIENT.player != null && !CLIENT.player.isCreative()) return; // Vanilla Finest port: creative only
        if(CLIENT.player != null) {
            CLIENT.setScreen(new InventoryScreen(CLIENT.player));
            CLIENT.player.currentScreenHandler.setCursorStack(item);
        } else {
            CLIENT.setScreen(new AlertScreen(Text.translatable("alert.headbrowser.head-category-tags")));
        }
    }

    public static ButtonWidget createSquareBrowseButton(int anchorButtonX, int anchorButtonWidth, int y) {
        return new BrowseHeadsButton(
                anchorButtonX + anchorButtonWidth + BROWSE_BUTTON_OFFSET + CONFIG.titleButtonHorizontalOffset()*CONFIG.offsetMultiplier(),
                y + CONFIG.titleButtonVerticalOffset()*CONFIG.offsetMultiplier(),
                BROWSE_BUTTON_DIMENSIONS.width,
                Text.empty(),
                button -> CLIENT.setScreen(new BrowseScreen()),
                MinecraftHeadsAPI.HEADS.getRandomHead()
        );
    }

    public static ButtonWidget createWideBrowseButton(int width) {
        return new BrowseHeadsButton(
                0,
                0,
                width,
                Text.translatable(format("menu.%s.wide-browse-button", MOD_ID)),
                button -> CLIENT.setScreen(new BrowseScreen()),
                MinecraftHeadsAPI.HEADS.getRandomHead()
        );
    }
}
