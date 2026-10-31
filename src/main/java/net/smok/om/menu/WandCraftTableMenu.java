package net.smok.om.menu;

import net.minecraft.resources.Identifier;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.smok.om.Values;
import net.smok.om.item.OMDataComponents;
import org.jspecify.annotations.Nullable;

public class WandCraftTableMenu extends AbstractContainerMenu {

    public static final int tableSlots = 2;
    public static final int playerSlots = tableSlots + 36;
    public static final int bookSlots = playerSlots + 5*6;
    public static final int wandSlots = bookSlots + 5;

    private final Container table;
    private final SpellBookContainer spellsBook;
    private final WandContainer wand;
    private final Player player;



    public WandCraftTableMenu(int containerId, Inventory inventory) {
        this(containerId, inventory, new SimpleContainer(2));
    }

    public WandCraftTableMenu(int containerId, Inventory inventory, Container container) {
        super(OMMenus.WANDCRAFT_TABLE, containerId);
        player = inventory.player;
        checkContainerSize(container, 2);
        table = container;

        spellsBook = new SpellBookContainer(5*6, itemStack -> table.setItem(0, itemStack));
        wand = new WandContainer(5, itemStack -> table.setItem(1, itemStack));

        addSlot(new BookSlot(table, 0, 26, 17, spellsBook));
        addSlot(new WandSlot(table, 1, 26, 53, wand));


        addStandardInventorySlots(inventory, 8, 84);

        for (int y = 0; y < 6; y++) {
            for (int x = 0; x < 5; x++) {
                this.addSlot(new SpellBookContainer.SpellSlot(spellsBook, x + y * 5, 8 - 104 + x * 18, 34 + y * 18));
            }
        }

        for (int x = 0; x < 5; x++) {
            this.addSlot(new WandContainer.SpellSlot(wand, x, 62 + x * 18, 53));
        }

        spellsBook.setHolder(table.getItem(0));
        wand.setHolder(table.getItem(1));
    }

    public WandContainer getWand() {
        return wand;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int slotIndex) {
        ItemStack clicked = ItemStack.EMPTY;
        Slot slot = slots.get(slotIndex);
        if (slot.hasItem()) {
            ItemStack itemStack = slot.getItem();
            clicked = itemStack.copy();
            if (slotIndex < tableSlots) {
                if (!moveItemStackTo(itemStack, tableSlots, playerSlots, true))
                    return ItemStack.EMPTY;
            } else {
                if (slotIndex < playerSlots) {
                    if (itemStack.has(OMDataComponents.SCROLL_SPELL)) {
                        if (!insertInWand(itemStack))
                            if (!insertInBook(itemStack)) return ItemStack.EMPTY;

                    } else if (itemStack.has(OMDataComponents.BOOK_CONTENTS) || itemStack.has(OMDataComponents.WAND_CONTENTS)) {
                        if (!moveItemStackTo(itemStack, 0, 2, false))
                            return ItemStack.EMPTY;
                    }
                } else if (slotIndex < bookSlots) {
                    if (!insertInWand(itemStack))
                        if (!moveItemStackTo(itemStack, tableSlots, playerSlots, true))
                            return ItemStack.EMPTY;
                } else {
                    if (!insertInBook(itemStack))
                        if (!moveItemStackTo(itemStack, tableSlots, playerSlots, true))
                            return ItemStack.EMPTY;
                }
            }


            if (itemStack.isEmpty()) {
                slot.setByPlayer(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }

            if (itemStack.getCount() == clicked.getCount()) {
                return ItemStack.EMPTY;
            }

            slot.onTake(player, clicked);
        }

        return clicked;
    }

    @Override
    public boolean clickMenuButton(Player player, int buttonId) {
        return switch (buttonId) {
            case 0 -> bookPrevious();
            case 1 -> bookNext();
            case 2 -> wandLeft();
            case 3 -> wandRight();
            case 4 -> wandShrink();
            case 5 -> wandExpand();
            default -> false;
        };
    }

    /*
    private boolean addToContainer(ItemStack itemStack, int containerSlot, int start, int end) {
        return !getSpellContainer(containerSlot).addItem(itemStack).isEmpty();
    }*/

    private boolean insertInWand(ItemStack itemStack) {
        if (!hashContainer(1)) return false;
        for (int i = WandCraftTableMenu.bookSlots; i < WandCraftTableMenu.wandSlots; i++) {
            Slot slot = getSlot(i);
            if (slot.mayPlace(itemStack) && slot.getItem().isEmpty()) {
                slot.safeInsert(itemStack);
                return true;
            }
        }
        return false;
    }

    private boolean insertInBook(ItemStack itemStack) {
        if (!hashContainer(0)) return false;
        if (moveItemStackTo(itemStack, WandCraftTableMenu.playerSlots, WandCraftTableMenu.bookSlots, false)) return true;
        spellsBook.addOver(itemStack);
        return true;
    }

    @Override
    public boolean stillValid(Player player) {
        return table.stillValid(player);
    }


    public boolean hashContainer(int slot) {
        return !table.getItem(slot).isEmpty();
    }

    public boolean containerIsEmpty(int slot) {
        return table.getItem(slot).isEmpty();
    }

    public boolean wandLeft() {
        return wand.left();
    }

    public boolean wandRight() {
        return wand.right();
    }

    public boolean wandShrink() {
        return player.isCreative() && wand.shrink();
    }

    public boolean wandExpand() {
        return player.isCreative() && wand.expand();
    }

    public boolean bookPrevious() {
        return spellsBook.previous();
    }

    public boolean bookNext() {
        return spellsBook.next();
    }

    public boolean canLeft() {
        return wand.canLeft();
    }

    public boolean canRight() {
        return wand.canRight();
    }

    public boolean canShrinkOrExpand() {
        return hashContainer(1) && player.isCreative();
    }

    public boolean canPrevious() {
        return spellsBook.canPrevious();
    }

    public boolean canNext() {
        return spellsBook.canNext();
    }

    public int bookPage() {
        return spellsBook.page();
    }

    public int maxPage() {
        return spellsBook.maxPage();
    }

/*
    private SpellItemContainer getSpellContainer(int i) {
        return i == 0 ? spellsTop : spellsDown;
    }*/

    /*
    private boolean isAWand(int spellContainer1) {
        ItemStack itemStack = spellTable.getItem(spellContainer1);
        return itemStack.has(OMDataComponents.SPELL_CONTAINER) || itemStack.getItem() instanceof Wand;
    }

    public void updateContainer(int i) {
        if (i > 1) return;
        ItemStack itemStack = spellTable.getItem(i);
        if (itemStack.has(OMDataComponents.SPELL_LIST) || itemStack.is(OMItems.SPELL_BOOK)) {
            spellTable.setItem(i, updateList(itemStack, getSpellContainer(i)));
            //updateContents(i);
        } else if (itemStack.has(OMDataComponents.SPELL_CONTAINER)) {
            spellTable.setItem(i, updateContainer(itemStack, getSpellContainer(i), itemStack.get(OMDataComponents.SPELL_CONTAINER).size()));
            //updateContents(i);
        } else if (itemStack.getItem() instanceof Wand wand) {
            spellTable.setItem(i, updateContainer(itemStack, getSpellContainer(i), wand.getMaxSpells()));
            //updateContents(i);
        }

    }

    private ItemStack updateContainer(ItemStack origin, Container spellContainer, int size) {
        ItemStack result = origin.copy();
        DefaultedList<Holder<Spell>> spellList = DefaultedList.create(size, null);
        int i = 0;
        for (ItemStack next : spellContainer) {
            Scroll.SpellInstance spellHolder = next.get(OMDataComponents.SCROLL_SPELL);
            if (i < size) spellList.set(i, spellHolder != null ? spellHolder.spell() : null);
            i++;
        }

        result.set(OMDataComponents.SPELL_CONTAINER, spellList);
        return result;
    }

    private ItemStack updateList(ItemStack origin, Container spellContainer) {
        ItemStack result = origin.copy();

        ArrayList<Holder<Spell>> spellList = new ArrayList<>();
        for (ItemStack next : spellContainer) {
            Scroll.SpellInstance spellHolder = next.get(OMDataComponents.SCROLL_SPELL);
            if (spellHolder != null) spellList.add(spellHolder.spell());
        }

        result.set(OMDataComponents.SPELL_LIST, spellList);
        return result;
    }

    private void updateContents(int containerIndex) {
        if (containerIndex > 1) return;
        ItemStack itemStack = spellTable.getItem(containerIndex);
        List<Holder<Spell>> list = itemStack.get(OMDataComponents.SPELL_LIST);
        Container spellContainer = getSpellContainer(containerIndex);
        if (list != null) {

            for (int i = 0; i < spellContainer.getContainerSize(); i++) {
                if (i < list.size()) {
                    ItemStack scroll = OMItems.SCROLL.getDefaultInstance();
                    scroll.set(OMDataComponents.SCROLL_SPELL, new Scroll.SpellInstance(list.get(i)));
                    spellContainer.setItem(i, scroll);
                } else spellContainer.setItem(i, ItemStack.EMPTY);
            }
        } else {
            DefaultedList<Holder<Spell>> spellList = itemStack.get(OMDataComponents.SPELL_CONTAINER);
            if (spellList != null) {

                for (int i = 0; i < spellContainer.getContainerSize(); i++) {
                    if (i < spellList.size() && spellList.get(i) != null) {
                        ItemStack scroll = OMItems.SCROLL.getDefaultInstance();
                        scroll.set(OMDataComponents.SCROLL_SPELL, new Scroll.SpellInstance(spellList.get(i)));
                        spellContainer.setItem(i, scroll);
                    } else spellContainer.setItem(i, ItemStack.EMPTY);
                }
            } else {
                for (int i = 0; i < spellContainer.getContainerSize(); i++)
                    spellContainer.setItem(i, ItemStack.EMPTY);
            }
        }
    }*/


    private static class BookSlot extends Slot {

        public static final Identifier ICON_ID = Values.id("container/wandcraft_table/book");
        private final SpellBookContainer bookContainer;
        public BookSlot(Container container, int slot, int x, int y, SpellBookContainer bookContainer) {
            super(container, slot, x, y);
            this.bookContainer = bookContainer;
        }

        @Override
        public int getMaxStackSize() {
            return 1;
        }

        @Override
        public boolean mayPlace(ItemStack itemStack) {
            return itemStack.has(OMDataComponents.BOOK_CONTENTS);
        }

        @Override
        public @Nullable Identifier getNoItemIcon() {
            return ICON_ID;
        }

        @Override
        public void set(ItemStack itemStack) {
            super.set(itemStack);
            bookContainer.setHolder(itemStack);
        }
    }

    private static class WandSlot extends Slot {

        public static final Identifier ICON_ID = Values.id("container/wandcraft_table/wand");
        private final WandContainer bookContainer;
        public WandSlot(Container container, int slot, int x, int y, WandContainer bookContainer) {
            super(container, slot, x, y);
            this.bookContainer = bookContainer;
        }

        @Override
        public int getMaxStackSize() {
            return 1;
        }

        @Override
        public boolean mayPlace(ItemStack itemStack) {
            return itemStack.has(OMDataComponents.WAND_CONTENTS);
        }

        @Override
        public @Nullable Identifier getNoItemIcon() {
            return ICON_ID;
        }

        @Override
        public void set(ItemStack itemStack) {
            super.set(itemStack);
            bookContainer.setHolder(itemStack);
        }
    }


}
