package net.smok.om.item;

import com.mojang.serialization.Codec;
import net.fabricmc.fabric.api.item.v1.ItemComponentTooltipProviderRegistry;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.smok.om.Values;
import net.smok.om.spell.Spell;

import java.util.function.UnaryOperator;

public interface OMDataComponents {

    static void init() {
        ItemComponentTooltipProviderRegistry.addLast(OMDataComponents.SCROLL_SPELL);
    }


    DataComponentType<Integer> SELECTED_SPELL = Registry.register(
            BuiltInRegistries.DATA_COMPONENT_TYPE,
            Values.id("selected_spell"),
            DataComponentType.<Integer>builder().persistent(Codec.INT).networkSynchronized(ByteBufCodecs.INT).build());

    DataComponentType<Boolean> IS_CREATIVE_BOOK = register("is_creative_book",
            builder -> builder.persistent(Codec.BOOL).networkSynchronized(ByteBufCodecs.BOOL));


    DataComponentType<Holder<Spell>> SPELL_HOLDER = register("spell", builder ->
            builder.persistent(Spell.HOLDER_CODEC).networkSynchronized(Spell.HOLDER_STREAM_CODEC)
    );

    DataComponentType<Scroll> SCROLL_SPELL = register("scroll_spell", builder ->
            builder.persistent(Scroll.SPELL_CODEC).networkSynchronized(Scroll.SPELL_STREAM_CODEC).cacheEncoding()
    );
    


    DataComponentType<BookContents> BOOK_CONTENTS = register("scroll_book_contents", builder ->
            builder.persistent(BookContents.CODEC).networkSynchronized(BookContents.STREAM_CODEC).cacheEncoding());

    DataComponentType<WandContents> WAND_CONTENTS = register("wand_contents", builder ->
            builder.persistent(WandContents.CODEC).networkSynchronized(WandContents.STREAM_CODEC).cacheEncoding());

    private static  <E> DataComponentType<E> register(String path, UnaryOperator<DataComponentType.Builder<E>> builder) {
        return Registry.register(BuiltInRegistries.DATA_COMPONENT_TYPE, Values.id(path), builder.apply(DataComponentType.builder()).build());
    }

}
