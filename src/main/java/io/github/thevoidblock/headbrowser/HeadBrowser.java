package io.github.thevoidblock.headbrowser;

import io.github.thevoidblock.headbrowser.gui.BrowseScreen;
import io.github.thevoidblock.headbrowser.gui.BrowserChildCreativeInventoryScreen;
import io.github.thevoidblock.headbrowser.gui.ErrorScreen;
import io.github.thevoidblock.headbrowser.gui.widget.BrowseHeadsButton;
import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.SpriteIconButton;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.gui.screens.worldselection.WorldCreationContext;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.WorldLoader;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.server.packs.repository.ServerPacksSource;
import net.minecraft.server.permissions.LevelBasedPermissionSet;
import net.minecraft.util.Util;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.WorldDataConfiguration;
import com.mojang.blaze3d.platform.InputConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.io.File;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import static java.lang.String.format;
import static java.lang.System.currentTimeMillis;

public class HeadBrowser implements ClientModInitializer {
    public static final String MOD_ID = "headbrowser";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    public static final Minecraft CLIENT = Minecraft.getInstance();
    public static final String ISSUES_URL = "https://github.com/TheVoidBlock/HeadBrowser/issues/new";
    public static final io.github.thevoidblock.headbrowser.HeadBrowserConfig CONFIG = io.github.thevoidblock.headbrowser.HeadBrowserConfig.createAndLoad();
    public static final File MOD_FOLDER = new File(CLIENT.gameDirectory, MOD_ID);

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

    public static boolean componentsBound() {
        try {
            new ItemStack(() -> Items.PLAYER_HEAD);
        } catch (NullPointerException e) {
            return false;
        }

        return true;
    }

    public static CompletableFuture<Optional<WorldCreationContext>> loadResources() {
        PackRepository vanillaOnlyPackRepository = new PackRepository(new ServerPacksSource(CLIENT.directoryValidator()));
        WorldLoader.PackConfig packConfig = new WorldLoader.PackConfig(vanillaOnlyPackRepository, WorldDataConfiguration.DEFAULT, false, true);
        WorldLoader.InitConfig loadConfig = new WorldLoader.InitConfig(packConfig, Commands.CommandSelection.INTEGRATED, LevelBasedPermissionSet.GAMEMASTER);
        return WorldLoader.load(loadConfig, (context) -> new WorldLoader.DataLoadOutput<>(Optional.empty(), context.datapackDimensions()), (resources, _, _, _) -> {
            resources.close();
            return Optional.empty();
        }, Util.backgroundExecutor(), CLIENT);
    }

    private static void presentError(String message, String error) {
        CLIENT.gui.setScreen(new ErrorScreen(message, error));
    }

    public static void error(String message, Exception e) {
        LOGGER.error(message, e);
        if(CLIENT.isGameLoadFinished()) presentError(message, e.toString());
    }

    public static boolean canGetHeadItem() {
        return CLIENT.player != null && (CLIENT.player.gameMode() == GameType.CREATIVE || CLIENT.hasSingleplayerServer());
    }

    public static void getItem(ItemStack item) {
        LocalPlayer player = CLIENT.player;
        if(player == null) return;
        if(KeyBindings.isKeyPressed(InputConstants.KEY_LALT)) CLIENT.gui.setScreen(new BrowserChildCreativeInventoryScreen(CLIENT, CLIENT.gui.screen()));
        else CLIENT.gui.setScreen(new InventoryScreen(player));
        player.containerMenu.setCarried(item);

        if(CLIENT.hasSingleplayerServer()) {
            MinecraftServer server = CLIENT.getSingleplayerServer();
            assert server != null;
            ServerPlayer serverPlayer = server.getPlayerList().getPlayer(player.getUUID());
            assert serverPlayer != null;
            serverPlayer.containerMenu.setCarried(item);
        }
    }

    public static Button createTitleButton(int anchorButtonX, int anchorButtonWidth, int y) {
        int width = BROWSE_BUTTON_DIMENSIONS.width;
        int height = BROWSE_BUTTON_DIMENSIONS.height;

        Button button = SpriteIconButton.builder(Component.empty(), _ -> BrowseScreen.open(CLIENT), true)
                .sprite(Identifier.fromNamespaceAndPath(MOD_ID, "icon/browse"), width, height)
                .size(width, height)
                .build();

        button.setPosition(
                anchorButtonX + anchorButtonWidth + BROWSE_BUTTON_OFFSET + CONFIG.titleButtonHorizontalOffset() * CONFIG.offsetMultiplier(),
                y + CONFIG.titleButtonVerticalOffset() * CONFIG.offsetMultiplier()
        );

        return button;
    }

    public static Button createWideBrowseButton(int width) {
        return new BrowseHeadsButton(
                0,
                0,
                width,
                Component.translatable(format("menu.%s.wide-browse-button", MOD_ID)),
                _ -> BrowseScreen.open(CLIENT),
                MinecraftHeadsAPI.HEADS.getRandomHead()
        );
    }
}
