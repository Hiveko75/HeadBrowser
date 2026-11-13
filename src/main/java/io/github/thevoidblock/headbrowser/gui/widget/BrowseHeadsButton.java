package io.github.thevoidblock.headbrowser.gui.widget;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.NonNull;

import java.util.function.Supplier;

public class BrowseHeadsButton extends Button.Plain {
    private final static int HEAD_OFFSET_X = 2;
    private final static int HEAD_OFFSET_Y = 1;

    private final Supplier<ItemStack> headSupplier;
    private final Identifier fallbackSprite;
    private ItemStack head;

    /**
     * The head is created when the button is first drawn, not when the screen is built: the title
     * screen can be built before item components are bound (first start, no heads cache), and
     * creating an ItemStack then throws "Components not bound yet". Until the supplier gives a head
     * (it returns null while the head list is not downloaded yet), the fallback sprite is shown.
     */
    public BrowseHeadsButton(int x, int y, int width, MutableComponent message, OnPress onPress, Supplier<ItemStack> headSupplier, Identifier fallbackSprite) {
        super(x, y, width, DEFAULT_HEIGHT, message, onPress, DEFAULT_NARRATION);
        this.headSupplier = headSupplier;
        this.fallbackSprite = fallbackSprite;
    }

    @Override
    public void extractContents(final @NonNull GuiGraphicsExtractor graphics, int mouseX, int mouseY, final float a) {
        super.extractContents(graphics, mouseX, mouseY, a);
        if (head == null) {
            try {
                head = headSupplier.get();
            } catch (RuntimeException e) {
                head = null; // components not bound yet, try again next frame
            }
        }
        if (head != null) {
            graphics.item(head, this.getX() + HEAD_OFFSET_X, this.getY() + HEAD_OFFSET_Y);
        } else {
            graphics.blitSprite(RenderPipelines.GUI_TEXTURED, fallbackSprite, this.getX(), this.getY(), this.width, this.height);
        }
    }
}
