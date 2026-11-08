package net.smok.om.spell.cast;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.smok.om.item.Wand;
import org.apache.logging.log4j.util.TriConsumer;

public interface PlayerCaster {


    default boolean oldMagic$isCasting(int slot) {
        return oldMagic$getCastHolder().isCasting(slot);
    }

    void oldMagic$releaseCast(int slot);

    void oldMagic$startCast(int slot);


    Player oldMagic$getAsPlayer();

    CastHolder oldMagic$getCastHolder();


    default void forWandItem(TriConsumer<Player, InteractionHand, Wand> consumer) {
        if (oldMagic$getAsPlayer().getItemInHand(InteractionHand.MAIN_HAND).getItem() instanceof Wand wand)
            consumer.accept(oldMagic$getAsPlayer(), InteractionHand.MAIN_HAND, wand);
        else if (oldMagic$getAsPlayer().getItemInHand(InteractionHand.OFF_HAND).getItem() instanceof Wand wand)
            consumer.accept(oldMagic$getAsPlayer(), InteractionHand.OFF_HAND, wand);
    }

}
