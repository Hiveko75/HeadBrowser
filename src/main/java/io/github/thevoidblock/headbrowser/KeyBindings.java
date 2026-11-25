package io.github.thevoidblock.headbrowser;

import io.github.thevoidblock.headbrowser.gui.BrowseScreen;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import org.lwjgl.glfw.GLFW;

import static io.github.thevoidblock.headbrowser.HeadBrowser.MOD_ID;
import static java.lang.String.format;

public class KeyBindings {
    private static final KeyBinding BROWSE_HEADS = registerKeyBinding("screens", "browse", GLFW.GLFW_KEY_KP_1);

    public static void registerBindFunctions() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            // Vanilla Finest port: in a world only in creative
            if (BROWSE_HEADS.wasPressed() && HeadBrowser.usableHere()) client.setScreen(new BrowseScreen());
        });
    }

    private static KeyBinding registerKeyBinding(String keyCategory, String keyID, int keyCode) {
        return KeyBindingHelper.registerKeyBinding(
                new KeyBinding(
                    format("key.%s.%s", MOD_ID, keyID),
                    keyCode,
                    format("category.%s.%s", MOD_ID, keyCategory)
                )
        );
    }
}
