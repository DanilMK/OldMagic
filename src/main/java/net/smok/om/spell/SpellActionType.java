package net.smok.om.spell;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.Registry;
import net.minecraft.resources.Identifier;
import net.smok.om.Values;
import net.smok.om.spell.block.action.BlockTransformation;
import net.smok.om.spell.block.action.BonemealAction;
import net.smok.om.spell.block.action.BreakAction;
import net.smok.om.spell.block.action.FeatureAction;
import net.smok.om.spell.block.target.HitBlockTarget;
import net.smok.om.spell.block.target.ShapeBlockTarget;
import net.smok.om.spell.entity.action.*;
import net.smok.om.spell.entity.target.EntityHitTarget;
import net.smok.om.spell.entity.target.FilterEntities;
import net.smok.om.spell.entity.target.NearbyEntitiesTarget;
import net.smok.om.spell.logic.*;

public interface SpellActionType<A extends SpellAction> {

    SpellActionType<HitBlockTarget> TARGET_BY_BLOCK_HIT = register("target_by_block_hit", HitBlockTarget.MAP_CODEC);
    SpellActionType<ShapeBlockTarget> TARGET_BY_SHAPE_BLOCKS = register("target_by_shape_blocks", ShapeBlockTarget.MAP_CODEC);
    SpellActionType<BlockTransformation> BLOCKS_TRANSFORMATION = register("blocks_transformation", BlockTransformation.MAP_CODEC);
    SpellActionType<BreakAction> BLOCKS_BREAK = register("blocks_break", BreakAction.MAP_CODEC);
    SpellActionType<FeatureAction> PLACE_FEATURE = register("place_feature", FeatureAction.MAP_CODEC);
    SpellActionType<BonemealAction> BONEMEAL_BLOCKS = register("bonemeal_blocks", BonemealAction.MAP_CODEC);

    SpellActionType<EntityHitTarget> TARGET_BY_HIT_ENTITY = register("target_by_hit_entity", EntityHitTarget.MAP_CODEC);
    SpellActionType<NearbyEntitiesTarget> TARGET_NEARBY_ENTITIES = register("target_by_nearby_entities", NearbyEntitiesTarget.MAP_CODEC);
    SpellActionType<DamageEntityAction> DAMAGE_ENTITIES = register("damage_entities", DamageEntityAction.MAP_CODEC);
    SpellActionType<DiscardProjectile> DISCARD_PROJECTILE = register("discard_projectile", DiscardProjectile.MAP_CODEC);
    SpellActionType<EffectAction> POTION_EFFECT = register("potion_effect", EffectAction.MAP_CODEC);
    SpellActionType<ProjectileSpawn> PROJECTILE = register("projectile", ProjectileSpawn.MAP_CODEC);


    SpellActionType<ListAction> LIST = register("list", ListAction.MAP_CODEC);
    SpellActionType<ChainListAction> CHAIN = register("chain", ChainListAction.MAP_CODEC);
    SpellActionType<EmptyAction> EMPTY = register("empty", EmptyAction.MAP_CODEC);
    SpellActionType<MoveEntityAction> MOVE_ENTITY = register("move_entity", MoveEntityAction.MAP_CODEC);
    SpellActionType<FilterEntities> FILTER_ENTITIES = register("filter_entities", FilterEntities.MAP_CODEC);
    SpellActionType<FirstEnterAction> FIRST_ENTER = register("first_enter", FirstEnterAction.MAP_CODEC);
    SpellActionType<WaitAction> WAIT = register("wait", WaitAction.MAP_CODEC);
    SpellActionType<?> FIRE_CONTROL = register("fire_control", FireControlAction.MAP_CODEC);
    SpellActionType<?> RANDOM_SELECT = register("random_select", RandomSelectAction.MAP_CODEC);

    MapCodec<A> codec();


    private static <A extends SpellAction> SpellActionType<A> register(final String id, final MapCodec<A> codec) {
        return Registry.register(Values.SPELL_ACTION_TYPES, Values.id(id), () -> codec);
    }

    static <A extends SpellAction> SpellActionType<A> register(final Identifier id, final MapCodec<A> codec) {
        return Registry.register(Values.SPELL_ACTION_TYPES, id, () -> codec);
    }

    static void init() {
    }
}
