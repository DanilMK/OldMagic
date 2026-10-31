package net.smok.om.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.ItemOwner;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;
import net.smok.om.block.OMBlocks;
import net.smok.om.item.OMDataComponents;
import net.smok.om.menu.WandCraftTableMenu;
import org.jspecify.annotations.Nullable;

public class WandCraftTableEntity extends BaseContainerBlockEntity implements WorldlyContainer {


    public static final MutableComponent DEFAULT_NAME = Component.translatable("container.old_magic.wandcraft_table");
    public static final int[] UP_SLOTS = {0};
    public static final int[] OTHER_SLOTS = {1};
    private NonNullList<ItemStack> items = NonNullList.withSize(2, ItemStack.EMPTY);

    public WandCraftTableEntity(BlockPos worldPosition, BlockState blockState) {
        super(OMBlocks.WANDCRARFT_TABLE_ENTITY, worldPosition, blockState);
    }

    @Override
    protected Component getDefaultName() {
        return DEFAULT_NAME;
    }

    @Override
    public NonNullList<ItemStack> getItems() {
        return items;
    }

    @Override
    protected void setItems(NonNullList<ItemStack> items) {
        this.items = items;
    }

    @Override
    public int getContainerSize() {
        return items.size();
    }

    @Override
    protected AbstractContainerMenu createMenu(int containerId, Inventory inventory) {
        return new WandCraftTableMenu(containerId, inventory, this);
    }

    @Override
    public int[] getSlotsForFace(Direction direction) {
        if (direction == Direction.UP)
            return UP_SLOTS;
        return OTHER_SLOTS;
    }

    @Override
    public boolean canPlaceItemThroughFace(int slot, ItemStack itemStack, @Nullable Direction direction) {
        return canPlaceItem(slot, itemStack);
    }

    @Override
    public boolean canTakeItemThroughFace(int slot, ItemStack itemStack, Direction direction) {
        return true;
    }

    @Override
    public boolean canPlaceItem(int slot, ItemStack itemStack) {
        return itemStack.has(OMDataComponents.WAND_CONTENTS) || itemStack.has(OMDataComponents.BOOK_CONTENTS);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        ContainerHelper.loadAllItems(input, items);
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        ContainerHelper.saveAllItems(output, items);
    }

}
