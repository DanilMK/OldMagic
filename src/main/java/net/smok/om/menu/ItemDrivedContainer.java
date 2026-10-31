package net.smok.om.menu;

import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public interface ItemDrivedContainer {
    void setHolder(@NotNull ItemStack holder);
}
