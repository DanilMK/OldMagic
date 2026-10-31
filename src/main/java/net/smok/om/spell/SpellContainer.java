package net.smok.om.spell;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.smok.om.item.OMDataComponents;
import net.smok.om.item.OMItems;
import net.smok.om.item.Scroll;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.BiConsumer;

/**
 * @param buttonMap G, H, J, K, RM
 */
public record SpellContainer(List<Holder<Spell>> spells, Set<Integer> locked, Map<Integer, Integer> buttonMap,
                             boolean freeSpells,
                             boolean expandable) {

    public static final Codec<SpellContainer> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Spell.NULLABLE_LIST_CODEC.fieldOf("spells").forGetter(SpellContainer::spells),
            Codec.INT.listOf().fieldOf("locked").forGetter(spellContainer -> List.copyOf(spellContainer.getLockedSlots())),
            Codec.unboundedMap(Codec.INT, Codec.INT).fieldOf("buttonMap").forGetter(SpellContainer::buttonMap),
            Codec.BOOL.optionalFieldOf("free_spells", false).forGetter(SpellContainer::freeSpells),
            Codec.BOOL.optionalFieldOf("expandable", false).forGetter(SpellContainer::freeSpells)
    ).apply(instance, SpellContainer::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, SpellContainer> STREAM_CODEC = StreamCodec.composite(
            Spell.NULLABLE_LIST_STREAM_CODEC, SpellContainer::spells,
            ByteBufCodecs.INT.apply(ByteBufCodecs.list()), spellContainer -> List.copyOf(spellContainer.getLockedSlots()),
            ByteBufCodecs.map(HashMap::new, ByteBufCodecs.VAR_INT, ByteBufCodecs.VAR_INT), SpellContainer::buttonMap,
            ByteBufCodecs.BOOL, SpellContainer::freeSpells,
            ByteBufCodecs.BOOL, SpellContainer::expandable,
            SpellContainer::new
    );

    public SpellContainer(List<Holder<Spell>> spells, List<Integer> locked, Map<Integer, Integer> buttonMap,boolean freeSpells, boolean expandable) {
        this(spells, Set.copyOf(locked), buttonMap, freeSpells, expandable);
    }

    public static SpellContainer createBook() {
        return new SpellContainer(List.of(), Set.of(), Map.of(), false, true);
    }

    public static SpellContainer createWand(@Nullable Holder<Spell> basicSpell, int modifiedSize) {
        ArrayList<Holder<Spell>> list = new ArrayList<>();
        HashMap<Integer, Integer> buttonMap = new HashMap<>();
        list.add(basicSpell);
        buttonMap.put(SlotButton.RM, 0);
        for (int i = 0; i < modifiedSize; i++) {
            list.add(null);
            if (i < SlotButton.EXTRA.size())
                buttonMap.put(SlotButton.EXTRA.get(i), i + 1);
        }
        return createWand(list, buttonMap);
    }

    public static SpellContainer createWand(List<Holder<Spell>> spellSlots, Map<Integer, Integer> buttonMap) {
        return new SpellContainer(spellSlots, List.of(), buttonMap, true, false);
    }

    public @Nullable Holder<Spell> get(int index) {
        return index < spells.size() && index > 0 ? spells.get(index) : null;
    }

    public @Nullable Holder<Spell> getByButton(int button) {
        Integer slot = buttonMap.get(button);
        if (slot != null && slot >= 0 && slot < spells.size())
            return spells.get(slot);
        return null;
    }

    public void mapButtons(BiConsumer<Integer, Holder<Spell>> consumer) {
        buttonMap.forEach((button, slot) -> {
            if (slot >= 0 && slot < spells.size()) consumer.accept(button, spells.get(slot));
            else consumer.accept(button, null);
        });
    }


    public SpellContainer changeButton(int button, int slot) {
        HashMap<Integer, Integer> map = new HashMap<>(buttonMap);
        map.put(button, slot);
        return new SpellContainer(spells, locked, map, freeSpells, expandable);
    }

    public List<ItemStack> getScrolls() {
        return spells.stream().map(spell -> spell != null ? OMItems.SCROLL.of(spell) : ItemStack.EMPTY).toList();
    }

    public SpellContainer setScrolls(List<ItemStack> scrolls) {
        List<Holder<Spell>> list = scrolls.stream().map(itemStack -> {
            Scroll instance = itemStack.get(OMDataComponents.SCROLL_SPELL);
            return instance == null ? null : instance.spell();
        }).toList();
        return new SpellContainer(list, locked, buttonMap, freeSpells, expandable);
    }

    public @NotNull SpellContainer setSpells(List<Holder<Spell>> spells) {
        return new SpellContainer(spells, locked, buttonMap, freeSpells, expandable);
    }

    public Set<Integer> getLockedSlots() {
        return locked;
    }

/*
    public static boolean startCast(@NotNull Level level, @NotNull Player player, @NotNull InteractionHand hand, int slotButton, Runnable startCasting, boolean creative) {
        ItemStack itemStack = player.getItemInHand(hand);

        SpellContainer container = itemStack.get(OMDataComponents.SPELL_CONTAINER1);
        if (container == null) return false;


        CastHolder castHolder = ((PlayerCaster) player).oldMagic$getCastHolder();
        Holder<Spell> spellStack = container.getByButton(slotButton);
        if (spellStack == null || castHolder.cantUse(spellStack)) return false;

        castHolder.startCast(level, player, itemStack, startCasting, spellStack, slotButton, creative);
        player.awardStat(Stats.ITEM_USED.get(itemStack.getItem()));

        return true;
    }*/

    public int size() {
        return spells.size();
    }

    public boolean isEmpty() {
        return spells.isEmpty();
    }

}
