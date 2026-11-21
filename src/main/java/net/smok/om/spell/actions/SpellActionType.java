package net.smok.om.spell.actions;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.Registry;
import net.minecraft.resources.Identifier;
import net.minecraft.world.phys.Vec3;
import net.smok.om.Values;
import net.smok.om.entity.SpellProjectile;
import net.smok.om.spell.SpellRunner;
import net.smok.om.spell.actions.block.*;
import net.smok.om.spell.actions.entity.*;
import net.smok.om.spell.actions.logic.*;
import net.smok.om.spell.actions.misc.*;
import net.smok.om.spell.cast.PlayerCaster;

import java.util.List;
import java.util.function.Function;

public interface SpellActionType<A extends SpellAction> {

    SpellActionType<ShapeBlockTarget> TARGET_BY_SHAPE_BLOCKS = register("target_by_shape_blocks", ShapeBlockTarget.MAP_CODEC);
    SpellActionType<BlockTransformation> BLOCKS_TRANSFORMATION = register("blocks_transformation", BlockTransformation.MAP_CODEC);
    SpellActionType<BreakAction> BLOCKS_BREAK = register("blocks_break", BreakAction.MAP_CODEC);
    SpellActionType<FeatureAction> PLACE_FEATURE = register("place_feature", FeatureAction.MAP_CODEC);
    SpellActionType<BonemealAction> BONEMEAL_BLOCKS = register("bonemeal_blocks", BonemealAction.MAP_CODEC);
    SpellActionType<ForEachBlockTarget> FOR_EACH_BLOCK_TARGET = register("for_each_block_target", ForEachBlockTarget.MAP_CODEC);
    SpellActionType<BlockPlaceAction> BLOCK_PLACE = register("block_place", BlockPlaceAction.MAP_CODEC);
    SpellActionType<BlockTransform> BLOCKS_TRANSFORM = register("block_transform", BlockTransform.MAP_CODEC);
    SpellActionType<BlockStateTransform> BLOCK_STATE_TRANSFORM = register("block_state_transform", BlockStateTransform.MAP_CODEC);
    SpellActionType<TargetHitBlock> TARGET_HIT_BLOCK = register("target_hit_block", TargetHitBlock.MAP_CODEC);


    SpellActionType<NearbyEntitiesTarget> TARGET_NEARBY_ENTITIES = register("target_by_nearby_entities", NearbyEntitiesTarget.MAP_CODEC);
    SpellActionType<DamageEntityAction> DAMAGE_ENTITIES = register("damage_entities", DamageEntityAction.MAP_CODEC);
    StaticActionType DISCARD_PROJECTILE = register("discard_projectile", spellRunner -> {
        if (spellRunner.getActor() instanceof SpellProjectile spellProjectile) {
            spellProjectile.discard();
            return SpellRunner.ProcessStatus.SUCCESS;
        }
        return SpellRunner.ProcessStatus.FAILURE;
    });
    SpellActionType<ProjectileSpawn> PROJECTILE = register("projectile", ProjectileSpawn.MAP_CODEC);
    StaticActionType CASTER_ENTITY_TARGET = register("caster_entity_target", spellRunner -> spellRunner.execute(_ -> {
        spellRunner.addEntityTarget(spellRunner.caster());
        return true;
    }));
    StaticActionType TELEPORT_ENTITY_TO_POSITION = register("teleport_entity_to_position", spellRunner -> spellRunner.executeForAnyEntity((_, entity) -> {
        List<Vec3> positionTargets = spellRunner.getPositionTargets();
        if (positionTargets.isEmpty()) return false;
        Vec3 position = positionTargets.getFirst();
        entity.teleportTo(position.x, position.y, position.z);
        return true;
    }));
    SpellActionType<MoveEntityToPosition> MOVE_ENTITY_TO_POSITION = register("move_entity_to_position", MoveEntityToPosition.MAP_CODEC);
    SpellActionType<KnockbackEntity> KNOCKBACK_ENTITY = register("knockback_entity", KnockbackEntity.MAP_CODEC);

    /*
    UnitAction.Type TARGET_HIT_ENTITY = register("target_hit_entity", spellRunner -> spellRunner.getOptional(StoredValueType.HIT).map(hitHolder -> {
        if (hitHolder.entity() == null) return SpellRunner.ProcessStatus.FAILURE;
        spellRunner.put(StoredValueType.ENTITY, hitHolder.entity());
        return SpellRunner.ProcessStatus.SUCCESS;
    }).orElse(SpellRunner.ProcessStatus.FAILURE));*/


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
    SpellActionType<ParticleAction> PARTICLE = register("particle", ParticleAction.MAP_CODEC);
    SpellActionType<?> REPOSITION_TARGET = register("reposition_target", RepositionTarget.MAP_CODEC);
    SpellActionType<?> FOR_EACH_ENTITY_TARGET = register("for_each_entity_target", ForEachEntityTarget.MAP_CODEC);
    SpellActionType<?> MOB_EFFECT = register("mob_effect", MobEffectAction.MAP_CODEC);
    SpellActionType<?> SOUND = register("sound", SoundAction.MAP_CODEC);
    SpellActionType<?> TOGGLE = register("toggle", ToggleAction.MAP_CODEC);
    SpellActionType<?> RESAVE_TARGET = register("resave_target", ResaveTarget.MAP_CODEC);
    SpellActionType<?> ADD_TO_LIST = register("add_to_list", AddToList.MAP_CODEC);
    StaticActionType DISABLE_TOGGLE = register("disable_toggle", spellRunner -> {
        if (spellRunner.caster() instanceof PlayerCaster caster && caster.oldMagic$isToggled(spellRunner.getSpell())) {
            caster.oldMagic$disableToggle(spellRunner.getSpell());
            return SpellRunner.ProcessStatus.SUCCESS;
        }
        return SpellRunner.ProcessStatus.FAILURE;
    });

    MapCodec<A> codec();


    private static StaticActionType register(final String id, Function<SpellRunner, SpellRunner.ProcessStatus> action) {
        return Registry.register(Values.SPELL_ACTION_TYPES, Values.id(id), new StaticActionType(action));
    }

    static StaticActionType register(final Identifier id, Function<SpellRunner, SpellRunner.ProcessStatus> action) {
        return Registry.register(Values.SPELL_ACTION_TYPES, id, new StaticActionType(action));
    }

    private static <A extends SpellAction> SpellActionType<A> register(final String id, final MapCodec<A> codec) {
        return Registry.register(Values.SPELL_ACTION_TYPES, Values.id(id), () -> codec);
    }

    static <A extends SpellAction> SpellActionType<A> register(final Identifier id, final MapCodec<A> codec) {
        return Registry.register(Values.SPELL_ACTION_TYPES, id, () -> codec);
    }

    static void init() {}
}
