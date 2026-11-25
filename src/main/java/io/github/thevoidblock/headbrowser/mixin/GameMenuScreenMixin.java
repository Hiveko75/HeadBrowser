package io.github.thevoidblock.headbrowser.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.gui.screen.GameMenuScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.GridWidget;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static io.github.thevoidblock.headbrowser.HeadBrowser.CONFIG;
import static io.github.thevoidblock.headbrowser.HeadBrowser.createWideBrowseButton;
import static io.github.thevoidblock.headbrowser.HeadBrowser.usableHere;

@Mixin(GameMenuScreen.class)
public class GameMenuScreenMixin extends Screen {

    protected GameMenuScreenMixin(Text title) {
        super(title);
    }

    @Inject(
            method = "initWidgets",
            at = @At(
                    target = "Lnet/minecraft/client/gui/widget/GridWidget;refreshPositions()V",
                    value = "INVOKE"
            )
    )
    private void initWidgets(CallbackInfo ci, @Local GridWidget gridWidget, @Local GridWidget.Adder adder) {
        // Vanilla Finest port: only in creative
        if(CONFIG.modEnabled()  && CONFIG.pauseButton() && usableHere()) {
            int returnToGameButtonWidth = 204;
            adder.add(createWideBrowseButton(returnToGameButtonWidth), 2);
        }
    }
}
