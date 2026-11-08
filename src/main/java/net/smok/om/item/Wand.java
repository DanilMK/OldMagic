package net.smok.om.item;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.smok.om.utils.SlotButton;
import org.jetbrains.annotations.NotNull;


public class Wand extends Item {

    public Wand(Properties properties) {
        super(properties);
    }

    public InteractionResult castSpell(@NotNull Level level, @NotNull Player player, @NotNull InteractionHand hand, int slotButton, Runnable startCasting) {

        ItemStack itemStack = player.getItemInHand(hand);
        WandContents wandContents = itemStack.get(OMDataComponents.WAND_CONTENTS);
        if (wandContents != null) {
            if (wandContents.startCast(level, player, itemStack, slotButton, startCasting, false)) return InteractionResult.SUCCESS;
            return InteractionResult.FAIL;
        }
        return InteractionResult.FAIL;
    }


    @Override
    public @NotNull InteractionResult use(@NotNull Level level, @NotNull Player player, @NotNull InteractionHand hand) {

        ItemStack itemStack = player.getItemInHand(hand);
        WandContents wandContents = itemStack.get(OMDataComponents.WAND_CONTENTS);
        if (wandContents != null) {
            if (wandContents.startCast(level, player, itemStack, SlotButton.RM, () -> {}, false)) return InteractionResult.SUCCESS;
            return InteractionResult.FAIL;
        }
        return InteractionResult.FAIL;
    }


}
