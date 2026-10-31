package net.smok.om;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public interface ScreenOpener {

    void old_magic$openScreen(String key, ItemStack itemStack, InteractionHand hand);
}
