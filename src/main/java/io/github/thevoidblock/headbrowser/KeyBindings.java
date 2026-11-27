package io.github.thevoidblock.headbrowser;

import io.github.thevoidblock.headbrowser.gui.BrowseScreen;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;

import static io.github.thevoidblock.headbrowser.HeadBrowser.CLIENT;
import static io.github.thevoidblock.headbrowser.HeadBrowser.MOD_ID;
import static java.lang.String.format;

public class KeyBindings {
    private static final KeyBinding BROWSE_HEADS = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            format("key.%s.%s", MOD_ID, "browse"),
            GLFW.GLFW_KEY_KP_1,
            new KeyBinding.Category(Identifier.of(MOD_ID, "main"))
    ));

    public static void registerBindFunctions() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            // Vanilla Finest port: in a world only in creative (the press is still consumed, so it doesn't pile up)
            if(BROWSE_HEADS.wasPressed() && HeadBrowser.usableHere()) BrowseScreen.open(client);
        });
    }

    public static boolean isKeyPressed(int keyCode) {
        return InputUtil.isKeyPressed(CLIENT.getWindow(), keyCode);
    }
}
