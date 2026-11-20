package net.smok.om.spell.cast;

import net.minecraft.core.Holder;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.smok.om.item.Wand;
import net.smok.om.spell.Spell;
import net.smok.om.spell.actions.SpellAction;
import net.smok.om.utils.StoredValues;
import org.apache.logging.log4j.util.TriConsumer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface PlayerCaster {


    default boolean oldMagic$isCasting(int slot) {
        return oldMagic$getCastHolder().isCasting(slot);
    }

    void oldMagic$releaseCast(int slot);

    void oldMagic$startCast(int slot);


    Player oldMagic$getAsPlayer();

    CastHolder oldMagic$getCastHolder();

    default boolean oldMagic$enableToggleServer(@NotNull Holder<Item> wand,
                                                @Nullable Holder<Spell> spell,
                                                @Nullable Holder<SpellAction> onEnd,
                                                @Nullable Holder<SpellAction> onTick,
                                                int tickDelay,
                                                @Nullable StoredValues storedValues) {
        return false;
    }

    boolean oldMagic$isToggled(Holder<Spell> spell);

    default void oldMagic$enableToggle(@NotNull Holder<Spell> spell, int tickDelay, @NotNull Holder<Item> wand) {}
    void oldMagic$disableToggle(@Nullable Holder<Spell> spell);

    default void forWandItem(TriConsumer<Player, InteractionHand, Wand> consumer) {
        if (oldMagic$getAsPlayer().getItemInHand(InteractionHand.MAIN_HAND).getItem() instanceof Wand wand)
            consumer.accept(oldMagic$getAsPlayer(), InteractionHand.MAIN_HAND, wand);
        else if (oldMagic$getAsPlayer().getItemInHand(InteractionHand.OFF_HAND).getItem() instanceof Wand wand)
            consumer.accept(oldMagic$getAsPlayer(), InteractionHand.OFF_HAND, wand);
    }

}
