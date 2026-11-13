package io.github.thevoidblock.headbrowser.gui.widget;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.NonNull;

import java.util.function.Supplier;

public class BrowseHeadsButton extends Button.Plain {
    private final static int HEAD_OFFSET_X = 2;
    private final static int HEAD_OFFSET_Y = 1;

    private final Supplier<ItemStack> headSupplier;
    private ItemStack head;

    /**
     * The head is created when the button is first drawn, not when the screen is built: the title
     * screen can be built before item components are bound (first start, no heads cache), and
     * creating an ItemStack then throws "Components not bound yet".
     */
    public BrowseHeadsButton(int x, int y, int width, MutableComponent message, OnPress onPress, Supplier<ItemStack> headSupplier) {
        super(x, y, width, DEFAULT_HEIGHT, message, onPress, DEFAULT_NARRATION);
        this.headSupplier = headSupplier;
    }

    @Override
    public void extractContents(final @NonNull GuiGraphicsExtractor graphics, int mouseX, int mouseY, final float a) {
        super.extractContents(graphics, mouseX, mouseY, a);
        if (head == null) {
            try {
                head = headSupplier.get();
            } catch (RuntimeException e) {
                return; // components not bound yet, try again next frame
            }
        }
        graphics.item(head, this.getX() + HEAD_OFFSET_X, this.getY() + HEAD_OFFSET_Y);
    }
}
