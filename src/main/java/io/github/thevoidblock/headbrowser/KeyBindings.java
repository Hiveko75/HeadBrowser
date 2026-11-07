package io.github.thevoidblock.headbrowser;

import io.github.thevoidblock.headbrowser.gui.BrowseScreen;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.resources.Identifier;

import static io.github.thevoidblock.headbrowser.HeadBrowser.CLIENT;
import static io.github.thevoidblock.headbrowser.HeadBrowser.MOD_ID;
import static java.lang.String.format;

import com.mojang.blaze3d.platform.InputConstants;

public class KeyBindings {
    private static final KeyMapping BROWSE_HEADS = KeyMappingHelper.registerKeyMapping(new KeyMapping(
            format("key.%s.%s", MOD_ID, "browse"),
            InputConstants.KEY_NUMPAD1,
            new KeyMapping.Category(Identifier.fromNamespaceAndPath(MOD_ID, "main"))
    ));

    public static void registerBindFunctions() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if(BROWSE_HEADS.consumeClick()) BrowseScreen.open(client);
        });
    }

    public static boolean isKeyPressed(int keyCode) {
        return InputConstants.isKeyDown(CLIENT.getWindow(), keyCode);
    }
}
