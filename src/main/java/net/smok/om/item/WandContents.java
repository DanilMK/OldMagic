package net.smok.om.item;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.smok.om.spell.CastHolder;
import net.smok.om.spell.PlayerCaster;
import net.smok.om.spell.SlotButton;
import net.smok.om.spell.Spell;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.util.*;
import java.util.function.Predicate;

public record WandContents(List<SpellSlot> spells) {

    public static final Codec<WandContents> CODEC = SpellSlot.CODEC.listOf().xmap(WandContents::new, WandContents::spells);
    public static final StreamCodec<RegistryFriendlyByteBuf, WandContents> STREAM_CODEC = SpellSlot.STREAM_CODEC.apply(ByteBufCodecs.list()).map(WandContents::new, WandContents::spells);

    @Contract("_, _ -> new")
    public static @NotNull WandContents createBasicAndExtra(Holder<Spell> basic, int extra) {
        ArrayList<SpellSlot> list = new ArrayList<>(extra + 1);
        list.add(new SpellSlot(basic, true, SlotButton.RM));
        for (int i = 0; i < extra; i++) {
            list.add(new SpellSlot(null, false, i < SlotButton.EXTRA.size() ? SlotButton.EXTRA.get(i) : -1));
        }
        return new WandContents(list);
    }

    public boolean startCast(@NotNull Level level, @NotNull Player player, @NotNull ItemStack itemStack, int slotButton, Runnable startCasting, boolean creative) {
        CastHolder castHolder = ((PlayerCaster) player).oldMagic$getCastHolder();
        for (SpellSlot spell : spells) {
            if (spell.button == slotButton && spell.spell != null) {
                castHolder.startCast(level, player, itemStack, startCasting, spell.spell, slotButton, creative);
                return true;
            }
        }
        return false;
    }

    public WandContents setSize(int size) {
        if (size > spells.size()) {
            ArrayList<SpellSlot> list = new ArrayList<>(spells);
            for (int i = 0; i < size - list.size(); i++) {
                list.add(new SpellSlot(null, false, -1));
            }
            return new WandContents(list);
        } else if (size < spells.size()) {
            ArrayList<SpellSlot> list = new ArrayList<>(spells);
            for (int i = 0; i < spells().size() - size; i++) {
                list.removeLast();
            }
            return new WandContents(list);
        }
        return this;
    }

    public @NotNull @Unmodifiable List<ItemStack> toItems() {
        return spells.stream().map(spellSlot -> spellSlot.spell != null ? OMItems.SCROLL.of(spellSlot.spell) : ItemStack.EMPTY).toList();
    }

    public Mutable mutable() {
        return new Mutable(spells);
    }



    public record SpellSlot(@Nullable Holder<Spell> spell, boolean locked, int button) {
        public static final Codec<SpellSlot> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Spell.HOLDER_CODEC.optionalFieldOf("spell").forGetter(spellSlot -> Optional.ofNullable(spellSlot.spell())),
                Codec.BOOL.fieldOf("locked").forGetter(SpellSlot::locked),
                Codec.INT.fieldOf("button").forGetter(SpellSlot::button)
        ).apply(instance, (spellHolder, locked, button) -> new SpellSlot(spellHolder.orElse(null), locked, button)));

        public static final StreamCodec<RegistryFriendlyByteBuf, SpellSlot> STREAM_CODEC = StreamCodec.composite(
                ByteBufCodecs.optional(Spell.HOLDER_STREAM_CODEC), slot -> Optional.ofNullable(slot.spell),
                ByteBufCodecs.BOOL, SpellSlot::locked,
                ByteBufCodecs.VAR_INT, SpellSlot::button,
                (spell1, locked1, button1) -> new SpellSlot(spell1.orElse(null), locked1, button1)
        );

        private MutableSlot mutable() {
            return new MutableSlot(spell, locked, button);
        }
    }

    private static class MutableSlot {
        @Nullable protected Holder<Spell> spell;
        protected boolean locked;
        protected int button;

        public MutableSlot(@Nullable Holder<Spell> spell, boolean locked, int button) {
            this.spell = spell;
            this.locked = locked;
            this.button = button;
        }

        public SpellSlot immutable() {
            return new SpellSlot(spell, locked, button);
        }
    }

    public static final class Mutable {
        private final ArrayList<MutableSlot> slots;

        private Mutable(List<SpellSlot> slots) {
            this.slots = new ArrayList<>(slots.stream().map(SpellSlot::mutable).toList());
        }

        private ArrayList<MutableSlot> slots() {
            return slots;
        }

        public void setSpell(int slot, @Nullable Holder<Spell> spellHolder) {
            slots.get(slot).spell = spellHolder;
        }

        public void changeLocked(int slot) {
            slots.get(slot).locked = !slots.get(slot).locked;
        }

        public void expand() {
            slots.add(new MutableSlot(null, false, -1));
        }

        public void shrink() {
            slots.removeLast();
        }

        public boolean isLocked(int slot) {
            return slots.get(slot).locked;
        }

        public @NotNull @Unmodifiable List<ItemStack> toItems() {
            return slots.stream().map(spellSlot -> spellSlot.spell != null ? OMItems.SCROLL.of(spellSlot.spell) : ItemStack.EMPTY).toList();
        }

        public WandContents toImmutable() {
            return new WandContents(slots.stream().map(MutableSlot::immutable).toList());
        }

        public int size() {
            return slots().size();
        }

        public int removeIf(Predicate<SpellSlot> predicate) {
            int count = 0;
            for (MutableSlot slot : slots) {
                if (predicate.test(slot.immutable())) {
                    slot.spell = null;
                    count++;
                }
            }
            return count;
        }

        public Holder<Spell> getSpell(int slot) {
            return slots.get(slot).spell;
        }
    }
}
