package net.smok.om.client.render.screens;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.smok.om.Values;
import net.smok.om.client.render.RenderUtils;
import net.smok.om.item.OMDataComponents;
import net.smok.om.item.Scroll;
import net.smok.om.menu.SpellBookContainer;
import net.smok.om.menu.WandCraftTableMenu;
import net.smok.om.menu.WandContainer;

public class WandCraftTableScreen extends AbstractContainerScreen<WandCraftTableMenu> {
    public static final Identifier BACKGROUND = Values.id("textures/gui/container/wandcraft_table.png");
    public static final Identifier BACKGROUND_BOOK = Values.id("textures/gui/container/spell_book.png");
    public static final Identifier LOCKED_SLOT = Values.id("container/wandcraft_table/slot_locked");
    public static final Identifier UNLOCKED_SLOT = Identifier.withDefaultNamespace("container/slot");

    private static final WidgetSprites PAGE_FORWARD_SPRITES = new WidgetSprites(
            Identifier.withDefaultNamespace("recipe_book/page_forward"), Identifier.withDefaultNamespace("recipe_book/page_forward_highlighted")
    );

    private static final WidgetSprites PAGE_BACKWARD_SPRITES = new WidgetSprites(
            Identifier.withDefaultNamespace("recipe_book/page_backward"), Identifier.withDefaultNamespace("recipe_book/page_backward_highlighted")
    );

    private static final WidgetSprites SLOT_ADD_SPRITES = new WidgetSprites(
            Values.id("container/wandcraft_table/add"), Values.id("container/wandcraft_table/add_highlighted")
    );

    private static final WidgetSprites SLOT_REMOVE_SPRITES = new WidgetSprites(
            Values.id("container/wandcraft_table/remove"), Values.id("container/wandcraft_table/remove_highlighted")
    );

    private static final Component NEXT_PAGE_TEXT = Component.translatable("gui.recipebook.next_page");
    private static final Component PREVIOUS_PAGE_TEXT = Component.translatable("gui.recipebook.previous_page");

    private static final Component NEXT_SLOT_TEXT = Component.translatable("gui.wandcraft_table.next_slot");
    private static final Component PREVIOUS_SLOT_TEXT = Component.translatable("gui.wandcraft_table.previous_slot");

    private static final Component ADD_SLOT_TEXT = Component.translatable("gui.wandcraft_table.add_slot");
    private static final Component REMOVE_SLOT_TEXT = Component.translatable("gui.wandcraft_table.remove_slot");

    public static final int BOOK_WIDTH = 104;
    public static final int INVENTORY_WIDTH = 176;

    private boolean bookEnabled;
    private int inventoryPosX;
    private ImageButton leftButton;
    private ImageButton rightButton;
    private ImageButton nextButton;
    private ImageButton previousButton;
    private ImageButton addButton;
    private ImageButton removeButton;


    public WandCraftTableScreen(WandCraftTableMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title, INVENTORY_WIDTH + BOOK_WIDTH, 166);
    }

    @Override
    protected void containerTick() {
        super.containerTick();
    }

    @Override
    protected void init() {
        super.init();
        inventoryPosX = leftPos;
        leftPos = inventoryPosX + BOOK_WIDTH / 2;

        recreateButtons();
    }

    private void recreateButtons() {
        clearWidgets();

        nextButton = addRenderableWidget(new ImageButton(leftPos - 104 + 77, topPos + 144, 12, 17, PAGE_FORWARD_SPRITES, _ -> clickPageButton(1), NEXT_PAGE_TEXT));
        nextButton.setTooltip(Tooltip.create(NEXT_PAGE_TEXT));
        previousButton = addRenderableWidget(new ImageButton(leftPos - 104 + 15, topPos + 144, 12, 17, PAGE_BACKWARD_SPRITES, _ -> clickPageButton(0), PREVIOUS_PAGE_TEXT));
        previousButton.setTooltip(Tooltip.create(PREVIOUS_PAGE_TEXT));

        leftButton = addRenderableWidget(new ImageButton(leftPos + 65, topPos + 34, 12, 17, PAGE_BACKWARD_SPRITES, _ -> clickMenuButton(2), PREVIOUS_SLOT_TEXT));
        leftButton.setTooltip(Tooltip.create(PREVIOUS_SLOT_TEXT));
        rightButton = addRenderableWidget(new ImageButton(leftPos + 135, topPos + 34, 12, 17, PAGE_FORWARD_SPRITES, _ -> clickMenuButton(3), NEXT_SLOT_TEXT));
        rightButton.setTooltip(Tooltip.create(NEXT_SLOT_TEXT));

        removeButton = addRenderableWidget(new ImageButton(leftPos + 80, topPos + 34, 17, 17, SLOT_REMOVE_SPRITES, _ -> clickMenuButton(4), REMOVE_SLOT_TEXT));
        removeButton.setTooltip(Tooltip.create(REMOVE_SLOT_TEXT));
        addButton = addRenderableWidget(new ImageButton(leftPos + 115, topPos + 34, 17, 17, SLOT_ADD_SPRITES, _ -> clickMenuButton(5), ADD_SLOT_TEXT));
        addButton.setTooltip(Tooltip.create(ADD_SLOT_TEXT));
    }

    private void clickMenuButton(int button) {
        assert minecraft.player != null;
        if (menu.clickMenuButton(minecraft.player, button)) {
            assert minecraft.gameMode != null;
            minecraft.gameMode.handleInventoryButtonClick(this.menu.containerId, button);
        }
    }

    private void clickPageButton(int page) {
        clickMenuButton(page);
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
        updateBookPos();
        updateWand();
        super.extractBackground(graphics, mouseX, mouseY, a);
        graphics.blit(RenderPipelines.GUI_TEXTURED, BACKGROUND, leftPos, topPos, 0, 0, INVENTORY_WIDTH, imageHeight, BACKGROUND_TEXTURE_WIDTH, BACKGROUND_TEXTURE_HEIGHT);
        if (bookEnabled) {
            graphics.blit(RenderPipelines.GUI_TEXTURED, BACKGROUND_BOOK, leftPos - BOOK_WIDTH, topPos, 0, 0, BOOK_WIDTH, 166, BACKGROUND_TEXTURE_WIDTH, BACKGROUND_TEXTURE_HEIGHT);
            if (menu.maxPage() > 0 || menu.bookPage() > 0)
                graphics.centeredText(font, (menu.bookPage() + 1) + "/" + (menu.maxPage() + 1), leftPos - 104 + 51, topPos + 147, 0xFFFFFFFF);
        }

    }

    private void updateBookPos() {
        if (bookEnabled == menu.hashContainer(0)) return;
        bookEnabled = menu.hashContainer(0);
        leftPos = bookEnabled ? inventoryPosX + BOOK_WIDTH : inventoryPosX + BOOK_WIDTH / 2;
        recreateButtons();
    }

    private void updateWand() {
        leftButton.visible = menu.canLeft();
        rightButton.visible = menu.canRight();
        removeButton.visible = menu.canShrinkOrExpand();
        addButton.visible = menu.canShrinkOrExpand();
        if (bookEnabled) {
            nextButton.visible = menu.canNext();
            previousButton.visible = menu.canPrevious();
        } else {
            nextButton.visible = false;
            previousButton.visible = false;
        }
    }

    @Override
    protected boolean hasClickedOutside(double mx, double my, int xo, int yo) {
        if (bookEnabled) xo = leftPos - BOOK_WIDTH;
        return mx < xo || my < yo || mx >= xo + (bookEnabled ? this.imageWidth : INVENTORY_WIDTH) || my >= yo + this.imageHeight;
    }

    @Override
    protected void extractSlot(GuiGraphicsExtractor graphics, Slot slot, int mouseX, int mouseY) {
        if (menu.hashContainer(1) && slot instanceof WandContainer.SpellSlot wandSlot) {
            graphics.blitSprite(RenderPipelines.GUI_TEXTURED, wandSlot.isLocked() ? LOCKED_SLOT : UNLOCKED_SLOT, slot.x - 1, slot.y - 1, 18, 18);
        }

        if (slot.getItem().has(OMDataComponents.SCROLL_SPELL) && (slot.container instanceof WandContainer || slot.container instanceof SpellBookContainer))
        {
            Scroll scroll = slot.getItem().get(OMDataComponents.SCROLL_SPELL);
            assert scroll != null;
            RenderUtils.renderSpell(graphics, scroll.spell(), slot.x, slot.y);
            graphics.itemDecorations(font, slot.getItem(), slot.x, slot.y);

        } else {
            super.extractSlot(graphics, slot, mouseX, mouseY);
        }
    }
}
