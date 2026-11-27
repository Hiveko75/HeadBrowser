package io.github.thevoidblock.headbrowser.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.gui.layouts.GridLayout;
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static io.github.thevoidblock.headbrowser.HeadBrowser.*;

@Mixin(PauseScreen.class)
public class GameMenuScreenMixin extends Screen {
    protected GameMenuScreenMixin(Component title) {
        super(title);
    }

    @Inject(
            method = "createPauseMenu",
            at = @At(
                    target = "Lnet/minecraft/client/gui/layouts/GridLayout;arrangeElements()V",
                    value = "INVOKE"
            )
    )
    private void initWidgets(CallbackInfo ci, @Local(name = "helper") GridLayout.RowHelper helper) {
        // Vanilla Finest port: only in creative
        if(CONFIG.modEnabled()  && CONFIG.pauseButton() && usableHere()) {
            int returnToGameButtonWidth = 204;
            helper.addChild(createWideBrowseButton(returnToGameButtonWidth), 2);
        }
    }
}
