package net.smok.om.spell.actions.entity;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.HolderSetCodec;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.entity.EntityTypeTest;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.smok.om.spell.actions.SpellAction;
import net.smok.om.spell.actions.SpellActionType;
import net.smok.om.spell.SpellRunner;
import net.smok.om.utils.EntityHolder;
import net.smok.om.utils.Shape;
import net.smok.om.utils.StoredValueType;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.Stream;

public record NearbyEntitiesTarget(float range, @Nullable HolderSet<EntityType<?>> filter, Shape shape, String entityTargets, String centerTarget) implements SpellAction {

    public static final MapCodec<NearbyEntitiesTarget> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codec.FLOAT.fieldOf("range").forGetter(NearbyEntitiesTarget::range),
            HolderSetCodec.create(Registries.ENTITY_TYPE, BuiltInRegistries.ENTITY_TYPE.holderByNameCodec(), false).optionalFieldOf("filter").forGetter(NearbyEntitiesTarget::optionalFilter),
            Shape.SHAPE_CODEC.fieldOf("shape").forGetter(NearbyEntitiesTarget::shape),
            Codec.STRING.optionalFieldOf("entity_list_target", "entities").forGetter(NearbyEntitiesTarget::entityTargets),
            Codec.STRING.optionalFieldOf("center_position_target", "actor").forGetter(NearbyEntitiesTarget::entityTargets)

    ).apply(instance, (range, filter, shape, entitiesTarget, centerTarget) ->
            new NearbyEntitiesTarget(range, filter.orElse(null), shape, entitiesTarget, centerTarget)));

    private Optional<HolderSet<EntityType<?>>> optionalFilter() {
        return Optional.ofNullable(filter);
    }

    private static final EntityTypeTest<Entity, ?> ANY_ENTITY = new EntityTypeTest<>() {
        @Override
        public @Nullable Entity tryCast(Entity entity) {
            return entity;
        }

        @Override
        public Class<? extends Entity> getBaseClass() {
            return Entity.class;
        }
    };


    public NearbyEntitiesTarget(float range, EntityType<?> entity, Shape shape) {
        this(range, HolderSet.direct(BuiltInRegistries.ENTITY_TYPE.wrapAsHolder(entity)), shape);
    }

    public NearbyEntitiesTarget(float range, Shape shape, EntityType<?>... entities) {
        this(range, Arrays.stream(entities), shape);
    }

    public NearbyEntitiesTarget(float range, @NotNull Stream<? extends EntityType<?>> stream, Shape shape) {
        this(range, HolderSet.direct(stream.map(BuiltInRegistries.ENTITY_TYPE::createIntrusiveHolder).toList()), shape);
    }


    @Override
    public SpellActionType<?> actionType() {
        return SpellActionType.TARGET_NEARBY_ENTITIES;
    }

    @Override
    public SpellRunner.ProcessStatus run(SpellRunner spellRunner) {
        return spellRunner.executeFor(centerTarget, StoredValueType.POSITION, (level, center) -> {
            ArrayList<Entity> entities = new ArrayList<>();
            level.getEntities(ANY_ENTITY, AABB.unitCubeFromLowerCorner(spellRunner.getActor().position()).inflate(range, range, range),
                    e -> (filter == null || e.is(filter)) && shape.isInRange(center, e.position(), range), entities);
            spellRunner.replaceList("entities", StoredValueType.ENTITY, new ArrayList<>(entities.stream().map(EntityHolder::new).toList()));
            return !entities.isEmpty();
        });
    }
}
