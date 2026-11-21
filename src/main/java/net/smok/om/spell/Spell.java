package net.smok.om.spell;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.syncher.EntityDataSerializer;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.RegistryFixedCodec;
import net.smok.om.Values;
import net.smok.om.spell.actions.SpellAction;
import net.smok.om.spell.actions.SpellActionType;
import net.smok.om.spell.cast.Casting;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public record Spell(@NotNull Casting casting, @NotNull Holder<SpellAction> main, @NotNull Holder<SpellAction> broken) {

    public static final Codec<Spell> DIRECT_CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Casting.CODEC.fieldOf("castings").forGetter(Spell::casting),
            SpellAction.HOLDER_CODEC.fieldOf("main").forGetter(Spell::main),
            SpellAction.HOLDER_CODEC.optionalFieldOf("broken", SpellActionType.EMPTY.holder()).forGetter(Spell::broken)
    ).apply(instance, Spell::new));

    public static final Codec<Holder<Spell>> HOLDER_CODEC = RegistryFixedCodec.create(Values.SPELLS_KEY);

    public static final StreamCodec<RegistryFriendlyByteBuf, Holder<Spell>> HOLDER_STREAM_CODEC =
            ByteBufCodecs.holderRegistry(Values.SPELLS_KEY);

    public static final EntityDataSerializer<Holder<Spell>> DATA_SERIALIZER =
            EntityDataSerializer.forValueType(HOLDER_STREAM_CODEC);

    public static final Codec<List<Holder<Spell>>> NULLABLE_LIST_CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.INT.fieldOf("size").forGetter(List::size),
            SpellSlot.CODEC.listOf().fieldOf("list").forGetter(Spell::getAsSlots)
    ).apply(instance, Spell::fromSlots));

    public static final StreamCodec<RegistryFriendlyByteBuf, List<Holder<Spell>>> NULLABLE_LIST_STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.INT, List::size,
            SpellSlot.STREAM_CODEC.apply(ByteBufCodecs.list()), Spell::getAsSlots,
            Spell::fromSlots
    );


    @Contract(pure = true)
    public static @NotNull String languageKey(@NotNull Holder<Spell> spellHolder) {
        return spellHolder.unwrapKey().map(spellResourceKey -> languageKey(spellResourceKey.identifier())).orElse("spell.unknown");
    }

    @Contract(pure = true)
    public static @NotNull String languageKey(@NotNull Identifier id) {
        return id.toLanguageKey("spell");
    }

    @Contract("_ -> new")
    public static @NotNull Identifier icon(@NotNull Identifier id) {
        return Identifier.fromNamespaceAndPath(id.getNamespace(), "textures/spell/" + id.getPath() + ".png");
    }



    private static List<SpellSlot> getAsSlots(List<Holder<Spell>> spells) {
        ArrayList<SpellSlot> spellSlots = new ArrayList<>();
        for (int i = 0; i < spells.size(); i++) {
            if (spells.get(i) != null)
                spellSlots.add(new SpellSlot(i, spells.get(i)));
        }
        return spellSlots;
    }

    private static List<Holder<Spell>> fromSlots(int size, List<SpellSlot> slots) {
        @SuppressWarnings("unchecked") Holder<Spell>[] holders = new Holder[size];
        for (SpellSlot slot : slots) {
            holders[slot.slot] = slot.spell;
        }

        return List.of(holders);
    }


    private record SpellSlot(int slot, @NotNull Holder<Spell> spell) {
        public static final Codec<SpellSlot> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.INT.fieldOf("slot").forGetter(SpellSlot::slot),
                Spell.HOLDER_CODEC.fieldOf("spell").forGetter(SpellSlot::spell)
        ).apply(instance, SpellSlot::new));

        public static final StreamCodec<RegistryFriendlyByteBuf, SpellSlot> STREAM_CODEC = StreamCodec.composite(
                ByteBufCodecs.INT, SpellSlot::slot,
                Spell.HOLDER_STREAM_CODEC, SpellSlot::spell,
                SpellSlot::new
        );
    }
}
