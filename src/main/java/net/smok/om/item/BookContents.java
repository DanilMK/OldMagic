package net.smok.om.item;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.smok.om.spell.Spell;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;

public record BookContents(@NotNull List<ScrollStack> scrolls, int selected) {
    public static final int MAX_TOTAL_SIZE = 1024;

    public static final Codec<BookContents> CODEC = ScrollStack.CODEC.listOf(0, MAX_TOTAL_SIZE).xmap(BookContents::new, BookContents::scrolls);

    public static final StreamCodec<RegistryFriendlyByteBuf, BookContents> STREAM_CODEC = StreamCodec.composite(
            ScrollStack.STREAM_CODEC.apply(ByteBufCodecs.list()), BookContents::scrolls,
            ByteBufCodecs.INT, BookContents::selected,
            BookContents::new
    );

    public BookContents(List<ScrollStack> scrolls) {
        this(scrolls, -1);
    }

    public BookContents() {
        this(List.of(), -1);
    }

    public InteractionResult startCast(Level level, Player player, ItemStack itemStack, Runnable onStart, int slotButton) {
        if (selected >= 0 && selected < scrolls.size()) return scrolls.get(selected).scroll().startCast(level, player, itemStack, onStart, slotButton) ? InteractionResult.SUCCESS : InteractionResult.FAIL;
        return InteractionResult.PASS;
    }

    public List<ItemStack> toItems() {
        return scrolls.stream().map(ScrollStack::toItem).toList();
    }

    public BookContents setItems(List<ItemStack> itemStack) {
        return setScrolls(fromItems(itemStack));
    }

    public BookContents setSelected(int selected) {
        return new BookContents(scrolls, selected);
    }

    public static List<ScrollStack> fromItems(List<ItemStack> items) {
        return items.stream().map(ScrollStack::fromItem).filter(Objects::nonNull).toList();
    }

    public @NotNull BookContents setSpells(List<Holder<Spell>> holders) {
        return setScrolls(holders.stream().map(spellHolder -> new ScrollStack(new Scroll(spellHolder, false, true), 1)).toList());
    }

    public @NotNull BookContents setScrolls(List<ScrollStack> scrolls) {
        return new BookContents(scrolls, selected);
    }

    public @Nullable Scroll getSelected() {
        if (scrolls.isEmpty() || selected < 0 || selected >= scrolls.size()) return null;
        return scrolls.get(selected).scroll;
    }


    public record ScrollStack(@NotNull Scroll scroll, int amount) {
        public static final Codec<ScrollStack> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Scroll.SPELL_CODEC.fieldOf("scroll").forGetter(ScrollStack::scroll),
                Codec.intRange(1, 64).fieldOf("amount").forGetter(ScrollStack::amount)
        ).apply(instance, ScrollStack::new));

        public static final StreamCodec<RegistryFriendlyByteBuf, ScrollStack> STREAM_CODEC = StreamCodec.composite(
                Scroll.SPELL_STREAM_CODEC, ScrollStack::scroll,
                ByteBufCodecs.INT, ScrollStack::amount,
                ScrollStack::new
        );

        public @NotNull ItemStack toItem() {
            ItemStack itemStack = OMItems.SCROLL.getDefaultInstance();
            itemStack.set(OMDataComponents.SCROLL_SPELL, scroll);
            itemStack.setCount(amount);
            return itemStack;
        }

        public static @Nullable ScrollStack fromItem(@NotNull ItemStack itemStack) {
            Scroll scroll1 = itemStack.get(OMDataComponents.SCROLL_SPELL);
            if (scroll1 != null) return new ScrollStack(scroll1, itemStack.count());
            return null;
        }

    }

}
