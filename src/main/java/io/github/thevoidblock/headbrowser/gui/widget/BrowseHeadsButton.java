package io.github.thevoidblock.headbrowser.gui.widget;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.SpriteIconButton;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.NonNull;

import java.util.function.Supplier;

/**
 * Square browse button for the icon rows of the title and pause screens.
 * <p>
 * It is a {@link SpriteIconButton} like the other icons in those rows, so mods that lay the row out
 * again after the screen is built (Screenshot Gallery re-spaces every SpriteIconButton) keep it in
 * the row. The sprite (the strawberry) is drawn until a head is available; then a head is drawn
 * instead.
 * <p>
 * The head is created when the button is first drawn, not when the screen is built: the title
 * screen can be built before item components are bound (first start, no heads cache), and creating
 * an ItemStack then throws "Components not bound yet". The supplier returns null while the head
 * list is not downloaded yet.
 */
public class BrowseHeadsButton extends SpriteIconButton.CenteredIcon {
    private final static int HEAD_OFFSET_X = 2;
    private final static int HEAD_OFFSET_Y = 1;

    private final Supplier<ItemStack> headSupplier;
    private ItemStack head;

    public BrowseHeadsButton(int size, Component message, OnPress onPress, Supplier<ItemStack> headSupplier, Identifier fallbackSprite) {
        super(size, size, message, size, size, 0, 0,
                new WidgetSprites(fallbackSprite, fallbackSprite, fallbackSprite),
                onPress, null, DEFAULT_NARRATION, false);
        this.headSupplier = headSupplier;
    }

    @Override
    public void extractContents(final @NonNull GuiGraphicsExtractor graphics, int mouseX, int mouseY, final float a) {
        if (head == null) {
            try {
                head = headSupplier.get();
            } catch (RuntimeException e) {
                head = null; // components not bound yet, try again next frame
            }
        }
        if (head == null) {
            super.extractContents(graphics, mouseX, mouseY, a);
            return;
        }
        extractDefaultSprite(graphics);
        graphics.item(head, this.getX() + HEAD_OFFSET_X, this.getY() + HEAD_OFFSET_Y);
    }
}
