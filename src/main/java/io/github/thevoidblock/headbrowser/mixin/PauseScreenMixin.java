package io.github.thevoidblock.headbrowser.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static io.github.thevoidblock.headbrowser.HeadBrowser.*;

@Mixin(PauseScreen.class)
public class PauseScreenMixin extends Screen {
    protected PauseScreenMixin(Component title) {
        super(title);
    }

    // Right after Minecraft's own icons (report bugs, send feedback, friends, player reporting: the
    // 4th LinearLayout.addChild), so the button comes before the icons other mods add (Mod Menu,
    // Screenshot Gallery), as on the title screen.
    @Inject(
            method = "createPauseMenu",
            at = @At(
                    target = "Lnet/minecraft/client/gui/layouts/LinearLayout;addChild(Lnet/minecraft/client/gui/layouts/LayoutElement;)Lnet/minecraft/client/gui/layouts/LayoutElement;",
                    value = "INVOKE",
                    ordinal = 3,
                    shift = At.Shift.AFTER
            )
    )
    private void addHeadBrowserButton(CallbackInfo ci, @Local(name = "iconButtonRow") LinearLayout iconButtonRow) {
        if(CONFIG.modEnabled()  && CONFIG.pauseButton()) {
            iconButtonRow.addChild(createBrowseButton());
        }
    }
}
