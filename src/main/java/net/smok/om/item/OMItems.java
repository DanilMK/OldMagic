package net.smok.om.item;

import net.fabricmc.fabric.api.creativetab.v1.FabricCreativeModeTab;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.smok.om.Values;
import net.smok.om.block.OMBlocks;
import net.smok.om.spell.Spells;

import java.util.function.Function;

public interface OMItems {

    static void init() {}


    ScrollBook SPELL_BOOK = register("spell_book", ScrollBook::new, new Item.Properties().stacksTo(1).component(OMDataComponents.BOOK_CONTENTS, new BookContents()));

    Wand COPPER_WAND = register("copper_wand", Wand::new, new Item.Properties().delayedComponent(OMDataComponents.WAND_CONTENTS, context -> WandContents.createBasicAndExtra(context.getOrThrow(Spells.BASIC), 4)));
    Wand IRON_WAND = register("iron_wand", Wand::new, new Item.Properties().delayedComponent(OMDataComponents.WAND_CONTENTS, context -> WandContents.createBasicAndExtra(context.getOrThrow(Spells.BASIC), 4)));
    Wand GOLDEN_WAND = register("golden_wand", Wand::new, new Item.Properties().delayedComponent(OMDataComponents.WAND_CONTENTS, context -> WandContents.createBasicAndExtra(context.getOrThrow(Spells.BASIC), 4)));

    ScrollItem SCROLL = register("scroll", ScrollItem::new, new Item.Properties());
    Item SPELL_PLACEHOLDER = register("spell_placeholder", Item::new, new Item.Properties().delayedComponent(OMDataComponents.SPELL_HOLDER, context -> context.getOrThrow(Spells.BASIC)));

    CreativeModeTab CREATIVE_MODE_TAB = Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB, ResourceKey.create(BuiltInRegistries.CREATIVE_MODE_TAB.key(), Values.id("creative_tab")), FabricCreativeModeTab.builder()
            .icon(() -> new ItemStack(SPELL_BOOK))
            .title(Component.translatable("creative_tab.old_magic"))
            .displayItems((parameters, output) -> {
                output.accept(SPELL_BOOK);
                ItemStack creativeBook = SPELL_BOOK.getDefaultInstance();
                creativeBook.set(OMDataComponents.IS_CREATIVE_BOOK, true);
                output.accept(creativeBook);
                output.accept(COPPER_WAND);
                output.accept(IRON_WAND);
                output.accept(GOLDEN_WAND);
                output.accept(SCROLL);
                output.accept(SPELL_PLACEHOLDER);
                output.accept(OMBlocks.WANDCRAFT_TABLE.asItem());
            }).build());

    private static <T extends Item> T register(String id, Function<Item.Properties, T> itemBuilder, Item.Properties properties) {
        ResourceKey<Item> resourceKey = ResourceKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(Values.MOD_ID, id));
        T item = itemBuilder.apply(properties.setId(resourceKey));
        return Registry.register(BuiltInRegistries.ITEM, resourceKey, item);
    }
}
