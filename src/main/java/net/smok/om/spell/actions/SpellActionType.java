package net.smok.om.spell.actions;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.Registry;
import net.minecraft.resources.Identifier;
import net.smok.om.Values;
import net.smok.om.spell.SpellRunner;
import net.smok.om.spell.actions.block.*;
import net.smok.om.spell.actions.entity.*;
import net.smok.om.spell.actions.logic.*;
import net.smok.om.spell.actions.position.EntityPositionTarget;
import net.smok.om.spell.actions.position.PositionBlockTarget;
import net.smok.om.spell.actions.position.RelativeEntityPositionTarget;
import net.smok.om.utils.StoredValueType;

import java.util.function.Function;

public interface SpellActionType<A extends SpellAction> {

    SpellActionType<ShapeBlockTarget> TARGET_BY_SHAPE_BLOCKS = register("target_by_shape_blocks", ShapeBlockTarget.MAP_CODEC);
    SpellActionType<BlockTransformation> BLOCKS_TRANSFORMATION = register("blocks_transformation", BlockTransformation.MAP_CODEC);
    SpellActionType<BreakAction> BLOCKS_BREAK = register("blocks_break", BreakAction.MAP_CODEC);
    SpellActionType<FeatureAction> PLACE_FEATURE = register("place_feature", FeatureAction.MAP_CODEC);
    SpellActionType<BonemealAction> BONEMEAL_BLOCKS = register("bonemeal_blocks", BonemealAction.MAP_CODEC);
    SpellActionType<ForEachBlockTarget> FOR_EACH_BLOCK_TARGET = register("for_each_block_target", ForEachBlockTarget.MAP_CODEC);
    SpellActionType<?> BLOCK_PLACE = register("block_place", BlockPlaceAction.MAP_CODEC);
    SpellActionType<?> BLOCKS_TRANSFORM = register("block_transform", BlockTransform.MAP_CODEC);
    SpellActionType<?> BLOCK_STATE_TRANSFORM = register("block_state_transform", BlockStateTransform.MAP_CODEC);
    SpellActionType<?> TARGET_HIT_BLOCK = register("target_hit_block", TargetHitBlock.MAP_CODEC);


    SpellActionType<NearbyEntitiesTarget> TARGET_NEARBY_ENTITIES = register("target_by_nearby_entities", NearbyEntitiesTarget.MAP_CODEC);
    SpellActionType<DamageEntityAction> DAMAGE_ENTITIES = register("damage_entities", DamageEntityAction.MAP_CODEC);
    SpellActionType<DiscardProjectile> DISCARD_PROJECTILE = register("discard_projectile", DiscardProjectile.MAP_CODEC);
    SpellActionType<EffectAction> POTION_EFFECT = register("potion_effect", EffectAction.MAP_CODEC);
    SpellActionType<ProjectileSpawn> PROJECTILE = register("projectile", ProjectileSpawn.MAP_CODEC);
    SpellActionType<CasterEntityTarget> CASTER_ENTITY_TARGET = register("caster_entity_target", CasterEntityTarget.MAP_CODEC);
    SpellActionType<TeleportEntityToPosition> TELEPORT_ENTITY_TO_POSITION = register("teleport_entity_to_position", TeleportEntityToPosition.MAP_CODEC);
    SpellActionType<MoveEntityToPosition> MOVE_ENTITY_TO_POSITION = register("move_entity_to_position", MoveEntityToPosition.MAP_CODEC);
    SpellActionType<KnockbackEntity> KNOCKBACK_ENTITY = register("knockback_entity", KnockbackEntity.MAP_CODEC);

    SpellActionType<UnitAction> TARGET_HIT_ENTITY = register("target_hit_entity", spellRunner -> spellRunner.getOptional(StoredValueType.HIT).map(hitHolder -> {
        if (hitHolder.entity() == null) return SpellRunner.ProcessStatus.FAILURE;
        spellRunner.put(StoredValueType.ENTITY, hitHolder.entity());
        return SpellRunner.ProcessStatus.SUCCESS;
    }).orElse(SpellRunner.ProcessStatus.FAILURE));


    SpellActionType<ListAction> LIST = register("list", ListAction.MAP_CODEC);
    SpellActionType<ChainListAction> CHAIN = register("chain", ChainListAction.MAP_CODEC);
    SpellActionType<EmptyAction> EMPTY = register("empty", EmptyAction.MAP_CODEC);
    SpellActionType<MoveEntityAction> MOVE_ENTITY = register("move_entity", MoveEntityAction.MAP_CODEC);
    SpellActionType<FirstEnterAction> FIRST_ENTER = register("first_enter", FirstEnterAction.MAP_CODEC);
    SpellActionType<WaitAction> WAIT = register("wait", WaitAction.MAP_CODEC);
    SpellActionType<?> FIRE_CONTROL = register("fire_control", FireControlAction.MAP_CODEC);
    SpellActionType<?> RANDOM_SELECT = register("random_select", RandomSelectAction.MAP_CODEC);
    SpellActionType<RepeatAction> REPEAT_ACTION = register("repeat", RepeatAction.MAP_CODEC);

    SpellActionType<PositionBlockTarget> POSITION_BLOCK_TARGET = register("position_block_target", PositionBlockTarget.MAP_CODEC);
    SpellActionType<RelativeEntityPositionTarget> RELATIVE_ENTITY_POSITION_TARGET = register("relative_entity_position_target", RelativeEntityPositionTarget.MAP_CODEC);
    SpellActionType<EntityPositionTarget> ENTITY_POSITION_TARGET = register("entity_position_target", EntityPositionTarget.MAP_CODEC);

    MapCodec<A> codec();

    default A instance() {
        return null;
    }

    private static SpellActionType<UnitAction> register(final String id, Function<SpellRunner, SpellRunner.ProcessStatus> action) {
        return Registry.register(Values.SPELL_ACTION_TYPES, Values.id(id), new UnitAction(action).actionType());
    }

    static SpellActionType<UnitAction> register(final Identifier id, Function<SpellRunner, SpellRunner.ProcessStatus> action) {
        return Registry.register(Values.SPELL_ACTION_TYPES, id, new UnitAction(action).actionType());
    }

    private static <A extends SpellAction> SpellActionType<A> register(final String id, final MapCodec<A> codec) {
        return Registry.register(Values.SPELL_ACTION_TYPES, Values.id(id), () -> codec);
    }

    static <A extends SpellAction> SpellActionType<A> register(final Identifier id, final MapCodec<A> codec) {
        return Registry.register(Values.SPELL_ACTION_TYPES, id, () -> codec);
    }

    static void init() {}
}
