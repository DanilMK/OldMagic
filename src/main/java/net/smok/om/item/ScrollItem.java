package net.smok.om.item;

import net.minecraft.core.Holder;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.smok.om.spell.*;
import net.smok.om.utils.SlotButton;

public class ScrollItem extends Item {

    public ScrollItem(Properties properties) {
        super(properties);
    }

    public ItemStack of(Holder<Spell> spell) {
        ItemStack instance = getDefaultInstance();
        instance.set(OMDataComponents.SCROLL_SPELL, new Scroll(spell, false, false));
        return instance;
    }

    public ItemStack of(Scroll scroll) {
        ItemStack instance = getDefaultInstance();
        instance.set(OMDataComponents.SCROLL_SPELL, scroll);
        return instance;
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack itemStack = player.getItemInHand(hand);

        Scroll scroll = itemStack.get(OMDataComponents.SCROLL_SPELL);

        if (scroll != null) {
            if (scroll.startCast(level, player, itemStack, () -> player.startUsingItem(hand), SlotButton.RM)) {
                player.awardStat(Stats.ITEM_USED.get(this));
                return InteractionResult.SUCCESS;
            }
            return InteractionResult.FAIL;
        }
        return InteractionResult.PASS;
    }

}
