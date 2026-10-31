package net.smok.om.client.data;

import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricRecipeProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import net.smok.om.block.OMBlocks;
import net.smok.om.item.OMDataComponents;
import net.smok.om.item.OMItems;
import net.smok.om.item.Scroll;
import net.smok.om.spell.Spells;

import java.util.concurrent.CompletableFuture;

public class RecipeGenerator extends FabricRecipeProvider {

    public RecipeGenerator(FabricPackOutput output, CompletableFuture<HolderLookup.Provider> registriesFuture) {
        super(output, registriesFuture);
    }

    @Override
    protected RecipeProvider createRecipeProvider(HolderLookup.Provider registries, RecipeOutput output) {
        return new MyRecipeProvider(registries, output);
    }

    private ItemStackTemplate scrollTemplate(Scroll scroll) {
        return new ItemStackTemplate(
                OMItems.SCROLL, DataComponentPatch.builder().set(OMDataComponents.SCROLL_SPELL, scroll).build()
        );
    }

    @Override
    public String getName() {
        return "Old Magic Recipe Generator";
    }

    private class MyRecipeProvider extends RecipeProvider {
        public MyRecipeProvider(HolderLookup.Provider registries, RecipeOutput output) {
            super(registries, output);
        }

        @Override
        public void buildRecipes() {
            shapeless(RecipeCategory.MISC, OMItems.SCROLL)
                    .requires(Items.PAPER)
                    .requires(Items.PAPER)
                    .requires(Items.PAPER)
                    .requires(Items.INK_SAC)
                    .unlockedBy(getHasName(Items.PAPER), has(Items.PAPER))
                    .save(output);

            shaped(RecipeCategory.MISC, OMBlocks.WANDCRAFT_TABLE)
                    .pattern("glg")
                    .pattern("ppp")
                    .define('g', Items.GOLD_INGOT)
                    .define('l', Items.LEATHER)
                    .define('p', ItemTags.PLANKS)
                    .unlockedBy(getHasName(OMItems.SCROLL), has(OMItems.SCROLL))
                    .save(output);

            shaped(RecipeCategory.MISC, OMItems.COPPER_WAND)
                    .pattern("  r")
                    .pattern(" c ")
                    .pattern("c  ")
                    .define('r', Items.REDSTONE_BLOCK)
                    .define('c', Items.COPPER_INGOT)
                    .unlockedBy(getHasName(OMItems.SCROLL), has(OMItems.SCROLL))
                    .save(output);

            shaped(RecipeCategory.MISC, OMItems.IRON_WAND)
                    .pattern("  d")
                    .pattern(" i ")
                    .pattern("i  ")
                    .define('d', Items.DIAMOND)
                    .define('i', Items.IRON_INGOT)
                    .unlockedBy(getHasName(OMItems.SCROLL), has(OMItems.SCROLL))
                    .save(output);

            shaped(RecipeCategory.MISC, OMItems.GOLDEN_WAND)
                    .pattern("  e")
                    .pattern(" g ")
                    .pattern("g  ")
                    .define('e', Items.EMERALD)
                    .define('g', Items.GOLD_INGOT)
                    .unlockedBy(getHasName(OMItems.SCROLL), has(OMItems.SCROLL))
                    .save(output);

            shapeless(RecipeCategory.MISC, OMItems.SPELL_BOOK)
                    .requires(OMItems.SCROLL)
                    .requires(OMItems.SCROLL)
                    .requires(Items.LEATHER)
                    .requires(Items.LEATHER)
                    .unlockedBy(getHasName(OMItems.SCROLL), has(OMItems.SCROLL))
                    .save(output);

            scroll(new Scroll(registries.getOrThrow(Spells.FIRE)), Items.BLAZE_POWDER);
            scroll(new Scroll(registries.getOrThrow(Spells.ICE)), Items.BLUE_ICE);
            scroll(new Scroll(registries.getOrThrow(Spells.PUSH)), Items.PISTON);
            scroll(new Scroll(registries.getOrThrow(Spells.COLLECT_ITEMS)), ItemTags.BUNDLES);
            scroll(new Scroll(registries.getOrThrow(Spells.GROW_PLANTS)), Items.BONE_MEAL);
            scroll(new Scroll(registries.getOrThrow(Spells.FLOWER_FIELD)), ItemTags.FLOWERS);

        }

        public void scroll(Scroll scroll, ItemLike ingredient) {
            shapeless(RecipeCategory.MISC, scrollTemplate(scroll))
                    .requires(OMItems.SCROLL)
                    .requires(ingredient)
                    .group("scroll")
                    .unlockedBy(getHasName(ingredient), has(ingredient))
                    .save(output, getItemName(OMItems.SCROLL) + "_" + scroll.getSpellName());
        }

        public void scroll(Scroll scroll, TagKey<Item> ingredient) {
            shapeless(RecipeCategory.MISC, scrollTemplate(scroll))
                    .requires(OMItems.SCROLL)
                    .requires(ingredient)
                    .group("scroll")
                    .unlockedBy("has" + ingredient.location().getPath(), has(ingredient))
                    .save(output, getItemName(OMItems.SCROLL) + "_" + scroll.getSpellName());
        }
    }
}
