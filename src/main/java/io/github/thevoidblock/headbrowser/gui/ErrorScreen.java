package io.github.thevoidblock.headbrowser.gui;

import io.wispforest.owo.ui.base.BaseUIModelScreen;
import io.wispforest.owo.ui.component.ButtonComponent;
import io.wispforest.owo.ui.component.UIComponents;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.core.Color;
import net.minecraft.client.gui.screens.ConfirmLinkScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import com.mojang.blaze3d.Blaze3D;
import java.net.URI;

import static io.github.thevoidblock.headbrowser.HeadBrowser.*;
import static java.lang.String.format;

public class ErrorScreen extends BaseUIModelScreen<FlowLayout> {

    public static final String SCREEN_ID = "error_screen";

    private final String message;
    private final String error;

    public ErrorScreen(String message, String error) {
        super(FlowLayout.class, DataSource.asset(Identifier.fromNamespaceAndPath(MOD_ID, SCREEN_ID)));
        this.message = message;
        this.error = error;
    }

    @Override
    protected void build(FlowLayout rootComponent) {
        FlowLayout lines = rootComponent.childById(FlowLayout.class, "lines");
        for(String line : message.lines().toList()) {
            lines.child(
                    UIComponents.label(Component.nullToEmpty(line))
            );
        }
        for(String line : error.lines().toList()) {
            lines.child(
                    UIComponents.label(Component.nullToEmpty(line)).color(Color.RED)
            );
        }

        rootComponent.childById(ButtonComponent.class, "copy").onPress(_ ->
                CLIENT.keyboardHandler.setClipboard(format("%s%n%s", this.message, this.error))
        );

        rootComponent.childById(ButtonComponent.class, "issues").onPress(_ ->
                CLIENT.gui.setScreen(new ConfirmLinkScreen(
                        confirmed -> {
                            if(confirmed) Blaze3D.openUri(URI.create(ISSUES_URL));
                            CLIENT.gui.setScreen(this);
                        },
                        Component.literal("confirm.headbrowser.open-issues"),
                        URI.create(ISSUES_URL),
                        true
        )));
    }
}
