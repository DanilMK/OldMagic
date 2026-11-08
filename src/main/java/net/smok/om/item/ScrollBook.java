package net.smok.om.item;

import net.minecraft.core.Holder;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.smok.om.utils.ScreenOpener;
import net.smok.om.Values;
import net.smok.om.utils.SlotButton;
import net.smok.om.spell.Spell;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;


public class ScrollBook extends Item {


    public ScrollBook(Properties properties) {
        super(properties);
    }

    @Override
    public @NotNull InteractionResult use(@NotNull Level level, @NotNull Player player, @NotNull InteractionHand hand) {
        ItemStack itemStack = player.getItemInHand(hand);


        boolean isCreative = Boolean.TRUE.equals(itemStack.get(OMDataComponents.IS_CREATIVE_BOOK));

        if (player.isShiftKeyDown()) {
            if (isCreative) {
                List<Holder<Spell>> references = level.holderLookup(Values.SPELLS_KEY).listElements()
                                .map(spellReference -> (Holder<Spell>)spellReference).toList();

                BookContents container = itemStack.get(OMDataComponents.BOOK_CONTENTS);
                itemStack.set(OMDataComponents.BOOK_CONTENTS, Objects.requireNonNullElseGet(container, BookContents::new).setSpells(references));
            }
            if (player instanceof ScreenOpener screenOpener) screenOpener.old_magic$openScreen("spell_book", itemStack, hand);
            return InteractionResult.SUCCESS;
        }

        BookContents bookContents = itemStack.get(OMDataComponents.BOOK_CONTENTS);
        if (bookContents != null) bookContents.startCast(level, player, itemStack, () -> player.startUsingItem(hand), SlotButton.RM);


        return InteractionResult.CONSUME;
    }

    @Override
    public boolean isFoil(ItemStack itemStack) {
        return super.isFoil(itemStack) || Boolean.TRUE.equals(itemStack.get(OMDataComponents.IS_CREATIVE_BOOK));
    }
}
