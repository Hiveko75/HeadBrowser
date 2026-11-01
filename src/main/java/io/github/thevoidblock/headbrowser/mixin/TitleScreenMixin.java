package io.github.thevoidblock.headbrowser.mixin;

import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.ref.LocalIntRef;
import io.github.thevoidblock.headbrowser.HeadBrowser;
import net.fabricmc.fabric.api.client.screen.v1.Screens;
import net.minecraft.client.gui.components.Button;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.gui.screens.TitleScreen;

import static io.github.thevoidblock.headbrowser.HeadBrowser.CONFIG;

@Mixin(TitleScreen.class)
public abstract class TitleScreenMixin {
    @Shadow
    protected abstract int getHorizontalPosition(int currentButton, int numberOfButtons, int buttonWidth);

    @Definition(id = "numberOfButtons", local = @Local(type = int.class, name = "numberOfButtons"))
    @Expression("numberOfButtons = ?")
    @Inject(method = "init", at = @At(value = "MIXINEXTRAS:EXPRESSION", shift = At.Shift.AFTER))
    private void updateNumberOfButtons(CallbackInfo ci, @Local(name = "numberOfButtons") LocalIntRef numberOfButtons) {
        if(CONFIG.modEnabled() && CONFIG.titleButton()) {
            numberOfButtons.set(numberOfButtons.get() + 1);
        }
    }

    @WrapOperation(method = "init", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screens/TitleScreen;getHorizontalPosition(III)I"))
    private int useNumberOfButtons(TitleScreen instance, int currentButton, int numberOfButtons, int buttonWidth, Operation<Integer> original, @SuppressWarnings("NameDoesntMatchTargetClass") @Local(name = "numberOfButtons") int realNumberOfButtons) {
        if(CONFIG.modEnabled() && CONFIG.titleButton()) {
            return original.call(instance, currentButton, realNumberOfButtons, buttonWidth);
        } else {
            return original.call(instance, currentButton, numberOfButtons, buttonWidth);
        }
    }

    @Inject(method = "init", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/components/SpriteIconButton;setPosition(II)V", ordinal = 1))
    private void addHeadBrowserButton(CallbackInfo ci,  @Local(name = "currentButton") LocalIntRef currentButton, @Local(name = "topPos") int topPos, @Local(name = "numberOfButtons") int numberOfButtons) {
        if(CONFIG.modEnabled() && CONFIG.titleButton()) {
            currentButton.set(currentButton.get() + 1);
            Button headBrowserButton = HeadBrowser.createBrowseButton();

            TitleScreen self = (TitleScreen) (Object) this;
            Screens.getWidgets(self).add(headBrowserButton);

            headBrowserButton.setPosition(getHorizontalPosition(currentButton.get(), numberOfButtons, HeadBrowser.SQUARE_BUTTON_SIDE_LENGTH), topPos);
        }
    }
}
