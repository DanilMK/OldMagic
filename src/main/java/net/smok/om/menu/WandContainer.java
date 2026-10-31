package net.smok.om.menu;

import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.smok.om.item.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Consumer;

public class WandContainer extends SimpleContainer implements ItemDrivedContainer {

    @NotNull
    private ItemStack holder;
    @Nullable
    private WandContents.Mutable wand;
    private final Consumer<ItemStack> resultConsumer;
    private int offsetIndex;


    public WandContainer(int size, Consumer<ItemStack> resultConsumer) {
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
        WandContents wandContents = holder.get(OMDataComponents.WAND_CONTENTS);
        wand = wandContents != null ? wandContents.mutable() : null;


        resetItems();
    }

    private void resetItems() {
        List<ItemStack> list = wand != null ? wand.toItems() : List.of();
        for (int i = 0; i < getContainerSize(); i++) {
            items.set(i, i + offsetIndex < list.size() ? list.get(i + offsetIndex) : ItemStack.EMPTY);
        }
    }

    @Override
    public void setChanged() {
        super.setChanged();
        //if (wand != null) holder.set(OMDataComponents.WAND_CONTENTS, wand);
        if (wand != null) {
            holder.set(OMDataComponents.WAND_CONTENTS, wand.toImmutable());
            resultConsumer.accept(holder);
        }
    }

    public void changeLock(int slot) {
        if (wand != null && slot < wand.size()) {
            wand.changeLocked(slot);
            setChanged();
        }
    }

    public boolean expand() {
        if (wand != null) {
            wand.expand();
            setChanged();
            return true;
        }
        return false;
    }

    public boolean shrink() {
        if (wand != null) {
            wand.shrink();
            setChanged();
            return true;
        }
        return false;
    }

    public boolean left() {
        if (canLeft()) {
            offsetIndex--;
            resetItems();
            return true;
        }
        return false;
    }

    public boolean canLeft() {
        return wand != null && offsetIndex > 0;
    }

    public boolean right() {
        if (canRight()) {
            offsetIndex++;
            resetItems();
            return true;
        }
        return false;
    }

    public boolean canRight() {
        return wand != null && offsetIndex + items.size() < wand.size();
    }



    public static class SpellSlot extends Slot {


        private final WandContainer spellContainer;

        public SpellSlot(WandContainer container, int slot, int x, int y) {
            super(container, slot, x, y);
            this.spellContainer = container;
        }

        @Override
        public boolean mayPlace(ItemStack itemStack) {
            if (!isActive() || !itemStack.is(OMItems.SCROLL) || !itemStack.has(OMDataComponents.SCROLL_SPELL))
                return false;
            assert spellContainer.wand != null;
            return !spellContainer.wand.isLocked(getContainerSlot() + spellContainer.offsetIndex);
        }

        @Override
        public boolean mayPickup(Player player) {
            if (!isActive()) return false;
            assert spellContainer.wand != null;
            return !spellContainer.wand.isLocked(getContainerSlot() + spellContainer.offsetIndex);
        }

        @Override
        public boolean isActive() {
            return spellContainer.wand != null && spellContainer.wand.size() > getContainerSlot() + spellContainer.offsetIndex;
        }

        @Override
        public ItemStack safeInsert(ItemStack inputStack, int inputAmount) {
            setByPlayer(inputStack.copyWithCount(1));
            if (spellContainer.wand != null) {
                Scroll scroll = inputStack.get(OMDataComponents.SCROLL_SPELL);
                spellContainer.wand.setSpell(getContainerSlot() + spellContainer.offsetIndex, scroll != null ? scroll.spell() : null);
            }
            return inputStack;
        }

        @Override
        public ItemStack remove(int amount) {
            super.remove(amount);
            if (spellContainer.wand != null) spellContainer.wand.setSpell(getContainerSlot() + spellContainer.offsetIndex, null);
            return ItemStack.EMPTY;
        }

        public boolean isLocked() {
            return spellContainer.wand != null && spellContainer.wand.isLocked(getContainerSlot() + spellContainer.offsetIndex);
        }

    }
}
