package net.smok.om.client.data;

import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricAdvancementProvider;
import net.minecraft.advancements.*;
import net.minecraft.advancements.criterion.DataComponentMatchers;
import net.minecraft.advancements.criterion.InventoryChangeTrigger;
import net.minecraft.advancements.criterion.ItemPredicate;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentExactPredicate;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import net.smok.om.Values;
import net.smok.om.block.OMBlocks;
import net.smok.om.item.OMDataComponents;
import net.smok.om.item.OMItems;
import net.smok.om.item.Scroll;
import net.smok.om.spell.Spell;
import net.smok.om.spell.Spells;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

import static net.minecraft.advancements.Advancement.Builder.advancement;

public class AdvancementGenerator extends FabricAdvancementProvider {
    protected AdvancementGenerator(FabricPackOutput output, CompletableFuture<HolderLookup.Provider> registryLookup) {
        super(output, registryLookup);
    }

    @Override
    public void generateAdvancement(HolderLookup.Provider registryLookup, Consumer<AdvancementHolder> consumer) {
        HolderGetter<Item> items = registryLookup.lookupOrThrow(Registries.ITEM);

        AdvancementHolder modRoot = advancement()
                .display(
                        OMItems.SPELL_BOOK,
                        Component.translatable("advancements.old_magic.root.title"),
                        Component.translatable("advancements.old_magic.root.description"),
                        Identifier.withDefaultNamespace("gui/advancements/backgrounds/adventure"),
                        AdvancementType.TASK,
                        true,
                        true,
                        false
                )
                .addCriterion("has_items", InventoryChangeTrigger.TriggerInstance.hasItems(Items.PAPER))
                .save(consumer, Values.id("old_magic/root").toString());

        AdvancementHolder scroll = advancement()
                .display(displayTask(OMItems.SCROLL, "old_magic.scroll"))
                .parent(modRoot)
                .addCriterion("has_scroll", InventoryChangeTrigger.TriggerInstance.hasItems(OMItems.SCROLL))
                .save(consumer, Values.id("old_magic/scroll").toString());

        AdvancementHolder table = advancement()
                .display(displayTask(OMBlocks.WANDCRAFT_TABLE, "old_magic.wandcraft_table"))
                .parent(scroll)
                .addCriterion("has_table", InventoryChangeTrigger.TriggerInstance.hasItems(OMBlocks.WANDCRAFT_TABLE))
                .save(consumer, Values.id("old_magic/wandcraft_table").toString());

        AdvancementHolder copperWand = advancement()
                .display(displayTask(OMItems.COPPER_WAND, "old_magic.copper_wand"))
                .parent(table)
                .addCriterion("has_wand", InventoryChangeTrigger.TriggerInstance.hasItems(OMItems.COPPER_WAND))
                .rewards(AdvancementRewards.Builder.function(Values.id("unlock_basic")))
                .save(consumer, Values.id("old_magic/copper_wand").toString());

        AdvancementHolder ironWand = advancement()
                .display(displayTask(OMItems.IRON_WAND, "old_magic.iron_wand"))
                .parent(copperWand)
                .addCriterion("has_wand", InventoryChangeTrigger.TriggerInstance.hasItems(OMItems.IRON_WAND))
                .save(consumer, Values.id("old_magic/iron_wand").toString());

        AdvancementHolder goldenWand = advancement()
                .display(displayTask(OMItems.GOLDEN_WAND, "old_magic.golden_wand"))
                .parent(ironWand)
                .addCriterion("has_wand", InventoryChangeTrigger.TriggerInstance.hasItems(OMItems.GOLDEN_WAND))
                .save(consumer, Values.id("old_magic/golden_wand").toString());


        AdvancementHolder fire = advancement()
                .display(displayScroll(registryLookup.getOrThrow(Spells.FIRE), "old_magic", false))
                .parent(scroll)
                .addCriterion("has_scroll", InventoryChangeTrigger.TriggerInstance.hasItems(
                                ItemPredicate.Builder.item().of(items, OMItems.SCROLL)
                                        .withComponents(DataComponentMatchers.Builder.components()
                                                .exact(DataComponentExactPredicate
                                                        .expect(OMDataComponents.SCROLL_SPELL, new Scroll(registryLookup.getOrThrow(Spells.FIRE)))
                                                ).build()
                                        )
                        )
                )
                .rewards(AdvancementRewards.Builder.function(Values.id("unlock_fire")))
                .save(consumer, Values.id("old_magic/fire_scroll").toString());

        AdvancementHolder ice = advancement()
                .display(displayScroll(registryLookup.getOrThrow(Spells.ICE), "old_magic", false))
                .parent(fire)
                .addCriterion("has_scroll", InventoryChangeTrigger.TriggerInstance.hasItems(
                                ItemPredicate.Builder.item().of(items, OMItems.SCROLL)
                                        .withComponents(DataComponentMatchers.Builder.components()
                                                .exact(DataComponentExactPredicate
                                                        .expect(OMDataComponents.SCROLL_SPELL, new Scroll(registryLookup.getOrThrow(Spells.ICE)))
                                                ).build()
                                        )
                        )
                )
                .rewards(AdvancementRewards.Builder.function(Values.id("unlock_ice")))
                .save(consumer, Values.id("old_magic/ice_scroll").toString());

        AdvancementHolder push = advancement()
                .display(displayScroll(registryLookup.getOrThrow(Spells.PUSH), "old_magic", false))
                .parent(scroll)
                .addCriterion("has_scroll", InventoryChangeTrigger.TriggerInstance.hasItems(
                                ItemPredicate.Builder.item().of(items, OMItems.SCROLL)
                                        .withComponents(DataComponentMatchers.Builder.components()
                                                .exact(DataComponentExactPredicate
                                                        .expect(OMDataComponents.SCROLL_SPELL, new Scroll(registryLookup.getOrThrow(Spells.PUSH)))
                                                ).build()
                                        )
                        )
                )
                .rewards(AdvancementRewards.Builder.function(Values.id("unlock_push")))
                .save(consumer, Values.id("old_magic/push_scroll").toString());

        AdvancementHolder collect_items = advancement()
                .display(displayScroll(registryLookup.getOrThrow(Spells.COLLECT_ITEMS), "old_magic", false))
                .parent(push)
                .addCriterion("has_scroll", InventoryChangeTrigger.TriggerInstance.hasItems(
                                ItemPredicate.Builder.item().of(items, OMItems.SCROLL)
                                        .withComponents(DataComponentMatchers.Builder.components()
                                                .exact(DataComponentExactPredicate
                                                        .expect(OMDataComponents.SCROLL_SPELL, new Scroll(registryLookup.getOrThrow(Spells.COLLECT_ITEMS)))
                                                ).build()
                                        )
                        )
                )
                .rewards(AdvancementRewards.Builder.function(Values.id("unlock_collect_items")))
                .save(consumer, Values.id("old_magic/collect_items_scroll").toString());

        AdvancementHolder growPlants = advancement()
                .display(displayScroll(registryLookup.getOrThrow(Spells.GROW_PLANTS), "old_magic", false))
                .parent(scroll)
                .addCriterion("has_scroll", InventoryChangeTrigger.TriggerInstance.hasItems(
                                ItemPredicate.Builder.item().of(items, OMItems.SCROLL)
                                        .withComponents(DataComponentMatchers.Builder.components()
                                                .exact(DataComponentExactPredicate
                                                        .expect(OMDataComponents.SCROLL_SPELL, new Scroll(registryLookup.getOrThrow(Spells.GROW_PLANTS)))
                                                ).build()
                                        )
                        )
                )
                .rewards(AdvancementRewards.Builder.function(Values.id("unlock_grow_plants")))
                .save(consumer, Values.id("old_magic/grow_plants_scroll").toString());

        advancement()
                .display(displayScroll(registryLookup.getOrThrow(Spells.FLOWER_FIELD), "old_magic", false))
                .parent(growPlants)
                .addCriterion("has_scroll", InventoryChangeTrigger.TriggerInstance.hasItems(
                        ItemPredicate.Builder.item().of(items, OMItems.SCROLL)
                                .withComponents(DataComponentMatchers.Builder.components()
                                        .exact(DataComponentExactPredicate
                                                .expect(OMDataComponents.SCROLL_SPELL, new Scroll(registryLookup.getOrThrow(Spells.FLOWER_FIELD)))
                                        ).build()
                                )
                        )
                )
                .rewards(AdvancementRewards.Builder.function(Values.id("unlock_flower_field")))
                .save(consumer, Values.id("old_magic/flower_field_scroll").toString());

    }

    private DisplayInfo displayScroll(Holder.Reference<Spell> spell, String namePath, boolean hidden) {
        ItemStackTemplate template = new ItemStackTemplate(OMItems.SPELL_PLACEHOLDER, DataComponentPatch.builder().set(OMDataComponents.SPELL_HOLDER, spell).build());

        return new DisplayInfo(
                template,
                Component.translatable("advancements." + namePath + "." + getSpellName(spell) + ".title"),
                Component.translatable("advancements." + namePath + "." + getSpellName(spell) + ".description"),
                Optional.empty(), AdvancementType.TASK, true, true, hidden);
    }

    private String getSpellName(Holder.Reference<Spell> spell) {
        return spell.unwrapKey().map(spellResourceKey -> spellResourceKey.identifier().getPath()).orElse("");
    }

    @Contract("_, _, _ -> new")
    private @NotNull DisplayInfo displayTask(@NotNull ItemLike icon, Component title, Component description) {
        return new DisplayInfo(new ItemStackTemplate(icon.asItem()), title, description, Optional.empty(), AdvancementType.TASK, true, true, false);
    }

    @Contract("_, _, _ -> new")
    private @NotNull DisplayInfo displayGoal(@NotNull ItemLike icon, Component title, Component description) {
        return new DisplayInfo(new ItemStackTemplate(icon.asItem()), title, description, Optional.empty(), AdvancementType.GOAL, true, true, false);
    }

    @Contract("_, _, _ -> new")
    private @NotNull DisplayInfo displayChallenge(@NotNull ItemLike icon, Component title, Component description) {
        return new DisplayInfo(new ItemStackTemplate(icon.asItem()), title, description, Optional.empty(), AdvancementType.CHALLENGE, true, true, false);
    }

    @Contract("_, _ -> new")
    private @NotNull DisplayInfo displayTask(@NotNull ItemLike icon, String namePath) {
        return new DisplayInfo(
                new ItemStackTemplate(icon.asItem()),
                Component.translatable("advancements." + namePath + ".title"),
                Component.translatable("advancements." + namePath + ".description"),
                Optional.empty(), AdvancementType.TASK, true, true, false);
    }

    @Contract("_, _ -> new")
    private @NotNull DisplayInfo displayGoal(@NotNull ItemLike icon, String namePath) {
        return new DisplayInfo(
                new ItemStackTemplate(icon.asItem()),
                Component.translatable("advancements." + namePath + ".title"),
                Component.translatable("advancements." + namePath + ".description"),
                Optional.empty(), AdvancementType.GOAL, true, true, false);
    }

    @Contract("_, _ -> new")
    private @NotNull DisplayInfo displayChallenge(@NotNull ItemLike icon, String namePath) {
        return new DisplayInfo(
                new ItemStackTemplate(icon.asItem()), 
                Component.translatable("advancements." + namePath + ".title"),
                Component.translatable("advancements." + namePath + ".description"),
                Optional.empty(), AdvancementType.CHALLENGE, true, true, false);
    }
}
