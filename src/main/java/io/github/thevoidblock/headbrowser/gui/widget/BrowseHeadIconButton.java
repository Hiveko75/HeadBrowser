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
 * Vanilla Finest port: square browse button next to Singleplayer on the title screen that shows a
 * random head from the downloaded list (as in Head Browser up to 2.6.1). The sprite (the strawberry)
 * is drawn until a head is available.
 * <p>
 * The head is created when the button is first drawn, not when the screen is built: the title
 * screen can be built before item components are bound (first start, no heads cache), and creating
 * an ItemStack then throws "Components not bound yet". The supplier returns null while the head
 * list is not downloaded yet.
 */
public class BrowseHeadIconButton extends SpriteIconButton.CenteredIcon {
    private final static int HEAD_OFFSET_X = 2;
    private final static int HEAD_OFFSET_Y = 1;

    private final Supplier<ItemStack> headSupplier;
    private ItemStack head;

    public BrowseHeadIconButton(int size, Component message, OnPress onPress, Supplier<ItemStack> headSupplier, Identifier fallbackSprite) {
        super(size, size, message, size, size,
                new WidgetSprites(fallbackSprite, fallbackSprite, fallbackSprite),
                onPress, null, DEFAULT_NARRATION);
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
