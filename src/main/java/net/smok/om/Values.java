package net.smok.om;

import net.fabricmc.fabric.api.event.registry.DynamicRegistries;
import net.fabricmc.fabric.api.event.registry.FabricRegistryBuilder;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityDataRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.levelgen.blockpredicates.BlockPredicateType;
import net.smok.om.block.OMBlocks;
import net.smok.om.entity.OMEntities;
import net.smok.om.entity.ProjectileProps;
import net.smok.om.item.OMDataComponents;
import net.smok.om.item.OMItems;
import net.smok.om.menu.OMMenus;
import net.smok.om.spell.*;
import net.smok.om.spell.actions.SpellAction;
import net.smok.om.spell.actions.SpellActionType;
import net.smok.om.utils.RandomBlockPredicate;
import net.smok.om.utils.StoredValueType;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public class Values {
    public static final String MOD_ID = "old_magic";



    public static final ResourceKey<Registry<SpellActionType<?>>> SPELL_ACTION_TYPE =
            ResourceKey.createRegistryKey(id("spell_action_type"));

    public static final Registry<SpellActionType<?>> SPELL_ACTION_TYPES =
            FabricRegistryBuilder.create(SPELL_ACTION_TYPE).buildAndRegister();

    public static final ResourceKey<Registry<SpellAction>> SPELL_ACTIONS_KEY =
            ResourceKey.createRegistryKey(id("spell_action"));

    public static final ResourceKey<Registry<Spell>> SPELLS_KEY =
            ResourceKey.createRegistryKey(id("spell"));

    public static final ResourceKey<Registry<StoredValueType<?>>> STORED_VALUES_TYPE =
            ResourceKey.createRegistryKey(id("stored_value_type"));

    public static final Registry<StoredValueType<?>> STORED_VALUE_TYPES =
            FabricRegistryBuilder.create(STORED_VALUES_TYPE).buildAndRegister();


    public static final BlockPredicateType<RandomBlockPredicate> RANDOM_BLOCK_PREDICATE_TYPE =
            Registry.register(BuiltInRegistries.BLOCK_PREDICATE_TYPE, ResourceKey.create(Registries.BLOCK_PREDICATE_TYPE, Values.id("random")), () -> RandomBlockPredicate.MAP_CODEC);





    @Contract("_ -> new")
    public static @NotNull Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(MOD_ID, path);
    }

    public static void init() {

        OMDataComponents.init();
        OMEntities.init();
        OMBlocks.init();
        OMItems.init();
        OMMenus.init();

        DynamicRegistries.registerSynced(SPELLS_KEY, Spell.DIRECT_CODEC);
        DynamicRegistries.registerSynced(SPELL_ACTIONS_KEY, SpellAction.DIRECT_CODEC);
        FabricEntityDataRegistry.register(id("projectile"), ProjectileProps.DATA_SERIALIZER); // todo maybe it can be unsynced


        StoredValueType.init();
        SpellActionType.init();
    }
}
