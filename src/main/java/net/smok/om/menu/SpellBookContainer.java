package net.smok.om.menu;

import net.minecraft.world.SimpleContainer;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.smok.om.item.BookContents;
import net.smok.om.item.OMDataComponents;
import net.smok.om.item.OMItems;
import net.smok.om.item.Scroll;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class SpellBookContainer extends SimpleContainer implements ItemDrivedContainer {

    @NotNull
    private ItemStack holder;
    @Nullable
    private BookContents book;
    private final Consumer<ItemStack> resultConsumer;
    private int offsetIndex;


    public SpellBookContainer(int size, Consumer<ItemStack> resultConsumer) {
        super(size);
        this.resultConsumer = resultConsumer;
        holder = ItemStack.EMPTY;
    }

    @NotNull
    public ItemStack getHolder() {
        return holder;
    }

    @Override
    public void setHolder(@NotNull ItemStack holder) {
        this.holder = holder.copy();
        book = holder.get(OMDataComponents.BOOK_CONTENTS);

        resetItems();
    }

    private void resetItems() {
        List<ItemStack> list = book != null ? book.toItems() : List.of();
        for (int i = 0; i < getContainerSize(); i++) {
            items.set(i, i + offsetIndex * items.size() < list.size() ? list.get(i + offsetIndex * items.size()) : ItemStack.EMPTY);
        }
    }

    @Override
    public void setChanged() {
        super.setChanged();
        if (book != null) {
            List<ItemStack> items1 = new ArrayList<>(book.toItems());
            for (int i = 0, j = offsetIndex * getContainerSize(); i < items.size(); i++, j++) {
                 if (j >= items1.size()) items1.add(items.get(i));
                 else items1.set(j, items.get(i));
            }
            book = book.setItems(items1);
            holder.set(OMDataComponents.BOOK_CONTENTS, book);
            resultConsumer.accept(holder);
            resetItems();
        }
    }

    public void addOver(ItemStack itemStack) {
        if (book == null) return;
        ArrayList<BookContents.ScrollStack> bookScrolls = new ArrayList<>(book.scrolls());
        bookScrolls.replaceAll(bookScroll -> {
            if (bookScroll.scroll().equals(itemStack.get(OMDataComponents.SCROLL_SPELL)) && bookScroll.amount() < itemStack.getMaxStackSize()) {
                int putAmount = Math.min(itemStack.count(), itemStack.getMaxStackSize() - bookScroll.amount());
                itemStack.shrink(putAmount);
                return new BookContents.ScrollStack(bookScroll.scroll(), bookScroll.amount() + putAmount);
            }
            return bookScroll;
        });
        if (!itemStack.isEmpty()) {
            Scroll scroll = itemStack.get(OMDataComponents.SCROLL_SPELL);
            if (scroll != null) {
                bookScrolls.add(new BookContents.ScrollStack(scroll, itemStack.count()));
                itemStack.setCount(0);
            }
        }
        book = book.setScrolls(bookScrolls);
        holder.set(OMDataComponents.BOOK_CONTENTS, book);
        resultConsumer.accept(holder);
        resetItems();
    }

    public boolean canNext() {
        return page() < maxPage();
    }

    public boolean canPrevious() {
        return offsetIndex > 0;
    }

    public boolean next() {
        if (canNext()) {
            offsetIndex++;
            resetItems();
            return true;
        }
        return false;
    }

    public boolean previous() {
        if (canPrevious()) {
            offsetIndex--;
            resetItems();
            return true;
        }
        return false;
    }

    public int page() {
        return offsetIndex;
    }

    public int maxPage() {
        return book != null ? book.scrolls().size() / items.size() : 0;
    }

    public static class SpellSlot extends Slot {


        private final SpellBookContainer spellContainer;

        public SpellSlot(SpellBookContainer container, int slot, int x, int y) {
            super(container, slot, x, y);
            this.spellContainer = container;
        }

        @Override
        public boolean mayPlace(ItemStack itemStack) {
            return isActive() && itemStack.is(OMItems.SCROLL) && itemStack.has(OMDataComponents.SCROLL_SPELL);
        }

        @Override
        public boolean isActive() {
            return spellContainer.book != null;
        }

    }
}
