package net.smok.om.client.render;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.item.properties.select.SelectItemModelProperty;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.smok.om.Values;
import net.smok.om.item.BookContents;
import net.smok.om.item.OMDataComponents;
import net.smok.om.item.Scroll;
import net.smok.om.spell.Spell;
import org.jspecify.annotations.Nullable;

public record SpellProperty() implements SelectItemModelProperty<ResourceKey<Spell>> {
    public static final Codec<ResourceKey<Spell>> VALUE_CODEC = ResourceKey.codec(Values.SPELLS_KEY);
    public static final SelectItemModelProperty.Type<SpellProperty, ResourceKey<Spell>> TYPE =
            SelectItemModelProperty.Type.create(
                    MapCodec.unit(new SpellProperty()), VALUE_CODEC
            );


    @Override
    public @Nullable ResourceKey<Spell> get(ItemStack itemStack, @Nullable ClientLevel level, @Nullable LivingEntity owner, int seed, ItemDisplayContext displayContext) {
        Holder<Spell> spellHolder = itemStack.get(OMDataComponents.SPELL_HOLDER);
        if (spellHolder != null) return spellHolder.unwrapKey().orElse(null);

        Scroll scroll = itemStack.get(OMDataComponents.SCROLL_SPELL);
        if (scroll != null) return scroll.spell().unwrapKey().orElse(null);

        BookContents bookContents = itemStack.get(OMDataComponents.BOOK_CONTENTS);
        if (bookContents != null) {
            Scroll selected = bookContents.getSelected();
            if (selected != null) return selected.spell().unwrapKey().orElse(null);
        }
        return null;
    }

    @Override
    public Codec<ResourceKey<Spell>> valueCodec() {
        return VALUE_CODEC;
    }

    @Override
    public Type<? extends SelectItemModelProperty<ResourceKey<Spell>>, ResourceKey<Spell>> type() {
        return TYPE;
    }
}
