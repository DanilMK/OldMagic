package net.smok.om.client.render.screens;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.PageButton;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.smok.om.Values;
import net.smok.om.client.render.RenderUtils;
import net.smok.om.item.BookContents;
import net.smok.om.item.OMDataComponents;
import net.smok.om.network.SpellBookSelectPayload;
import net.smok.om.spell.Spell;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

public class SpellBookScreen extends Screen {

    public static final Identifier BACKGROUND = Values.id("textures/gui/spell_book.png");
    private static final int ROWS = 5;
    private static final int COLUMNS = 2;

    private final int slot;
    private final BookContents spells;
    private final List<SpellWidget> spellWidgets = new ArrayList<>();
    private final ItemStack book;

    private int currentPage;
    private PageButton backButton;
    private PageButton forwardButton;




    public SpellBookScreen(Player player, ItemStack itemStack, InteractionHand hand) {
        super(Component.literal("Spell book"));
        slot = hand == InteractionHand.MAIN_HAND ? player.getInventory().getSelectedSlot() : 40;
        spells = itemStack.get(OMDataComponents.BOOK_CONTENTS);
        book = itemStack;
    }

    @Override
    protected void init() {
        super.init();
        spellWidgets.clear();

        int left = width / 2 - 278 / 2;
        int top = height / 2 - 178 / 2;

        int x = left + 22;
        int y = top + 16;

        int h = 28;

        for (int i = 0; i < ROWS; i++) {
            spellWidgets.add(addRenderableWidget(new SpellWidget(x, y + i * h, 112, 18, minecraft.font, i, this::selectSpell)));
        }

        x += 124;

        for (int i = 0; i < ROWS; i++) {
            spellWidgets.add(addRenderableWidget(new SpellWidget(x, y + i * h, 112, 18, minecraft.font, i + ROWS, this::selectSpell)));
        }

        this.backButton = this.addRenderableWidget(new PageButton(left + 28, top + 155, false, button -> this.pageBack(), true));
        this.forwardButton = this.addRenderableWidget(new PageButton(left + 227, top + 155, true, button -> this.pageForward(), true));

        addRenderableWidget(BookTabWidget.top(left + 35, top - 30, new Color(0xC84141).getRGB(), Component.translatable("gui.done"), button -> onClose(),
                (graphics, mouseX, mouseY, a) -> graphics.text(font, "X", left + 45, top - 12, 0xFFFFFFFF)));

        updateButtonVisibility();
        updatePageContent();


    }

    private void pageForward() {
        if (this.currentPage * ROWS * COLUMNS < spells.scrolls().size() - 1) {
            this.currentPage++;
            this.updatePageContent();
        }

        this.updateButtonVisibility();
    }

    private void pageBack() {
        if (this.currentPage > 0) {
            this.currentPage--;
            this.updatePageContent();
        }

        this.updateButtonVisibility();
    }

    private void updateButtonVisibility() {
        this.backButton.visible = this.currentPage > 0;
        this.forwardButton.visible = spells.scrolls().size() - currentPage * ROWS * COLUMNS > ROWS * COLUMNS;
    }

    private void updatePageContent() {
        spellWidgets.forEach(spellWidget -> spellWidget.updateWidget(spellId(spellWidget.getSlot())));
    }

    private @Nullable Holder<Spell> spellId(int i) {
        i += currentPage * ROWS * COLUMNS;
        if (i >= spells.scrolls().size()) return null;
        return spells.scrolls().get(i).scroll().spell();
    }

    private void selectSpell(int i, int button) {
        i += currentPage * ROWS * COLUMNS;
        book.set(OMDataComponents.SELECTED_SPELL, i);
        ClientPlayNetworking.send(new SpellBookSelectPayload(slot, i));
        if (button == 0) onClose();
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
        super.extractBackground(graphics, mouseX, mouseY, a);
        graphics.blit(RenderPipelines.GUI_TEXTURED, BACKGROUND, width / 2 - 278 / 2, height / 2 - 178 / 2, 0, 4, 278, 178, 278, 256);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        if (this.minecraft.options.keyInventory.matches(event)) {
            this.onClose();
            return true;
        }
        return super.keyPressed(event);
    }

    public static class SpellWidget extends AbstractWidget {

        private static final int SELECT_COLOR = new Color(0xFF858585).getRGB();
        private static final int TEXT_COLOR = new Color(0xff000000).getRGB();

        private final int slot;
        private final net.minecraft.client.gui.Font font;
        private final BiConsumer<Integer, Integer> execution;
        private Holder<Spell> spell;

        public SpellWidget(int x, int y, int width, int height, Font font, int slot, BiConsumer<Integer, Integer> execution) {
            super(x, y, width, height, Component.empty());
            this.slot = slot;
            this.font = font;
            this.execution = execution;
        }

        @Override
        protected void extractWidgetRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
            if (spell == null) return;
            if (isHoveredOrFocused()) graphics.outline(getX(), getY(), getWidth(), getHeight(), SELECT_COLOR);
            graphics.textWithWordWrap(font, Component.translatable(Spell.languageKey(spell)), getX() + 22, getY(), getWidth() - 22, TEXT_COLOR, false);
            RenderUtils.renderSpell(graphics, spell, getX() + 1, getY() + 1);
        }

        @Override
        protected void updateWidgetNarration(NarrationElementOutput output) {
            if (spell != null)
                output.add(NarratedElementType.USAGE, Component.translatable(Spell.languageKey(spell)));
        }

        public void updateWidget(Holder<Spell> spell) {
            this.spell = spell;
            visible = spell != null;
        }

        @Override
        public void onClick(MouseButtonEvent event, boolean doubleClick) {
            execution.accept(slot, event.button());
        }

        public int getSlot() {
            return slot;
        }
    }
}
