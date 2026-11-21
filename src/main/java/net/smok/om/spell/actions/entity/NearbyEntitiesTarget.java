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
import net.smok.om.spell.actions.SpellAction;
import net.smok.om.spell.actions.SpellActionType;
import net.smok.om.spell.SpellRunner;
import net.smok.om.utils.EntityHolder;
import net.smok.om.utils.SelectionMode;
import net.smok.om.utils.Shape;
import net.smok.om.utils.StoredValueType;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;

public record NearbyEntitiesTarget(
        float range,
        @Nullable HolderSet<EntityType<?>> filter,
        Shape shape,
        SelectionMode mode,
        String entityTargets,
        String centerTarget,
        String setPreviousTarget
) implements SpellAction {

    public static final MapCodec<NearbyEntitiesTarget> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codec.FLOAT.fieldOf("range").forGetter(NearbyEntitiesTarget::range),
            HolderSetCodec.create(Registries.ENTITY_TYPE, BuiltInRegistries.ENTITY_TYPE.holderByNameCodec(), false).optionalFieldOf("filter").forGetter(NearbyEntitiesTarget::optionalFilter),
            Shape.SHAPE_CODEC.fieldOf("shape").forGetter(NearbyEntitiesTarget::shape),
            SelectionMode.CODEC.optionalFieldOf("mode", SelectionMode.REPLACE).forGetter(NearbyEntitiesTarget::mode),
            Codec.STRING.optionalFieldOf("entity_list_target", "entities").forGetter(NearbyEntitiesTarget::entityTargets),
            Codec.STRING.optionalFieldOf("center_position_target", "actor").forGetter(NearbyEntitiesTarget::centerTarget),
            Codec.STRING.optionalFieldOf("set_previous_target", "previous_entities").forGetter(NearbyEntitiesTarget::setPreviousTarget)

    ).apply(instance, (range, filter, shape, mode, entitiesTarget, centerTarget, setPreviousTarget) ->
            new NearbyEntitiesTarget(range, filter.orElse(null), shape, mode, entitiesTarget, centerTarget, setPreviousTarget)));

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

    @Contract
    public static NearbyEntitiesTarget builder(@NotNull UnaryOperator<Builder> builder) {
        return builder.apply(new Builder()).build();
    }



    @Override
    public SpellActionType<?> actionType() {
        return SpellActionType.TARGET_NEARBY_ENTITIES;
    }

    @Override
    public SpellRunner.ProcessStatus run(SpellRunner spellRunner) {
        return spellRunner.executeFor(centerTarget, StoredValueType.POSITION, (level, center) -> {
            ArrayList<Entity> entities = new ArrayList<>();
            level.getEntities(ANY_ENTITY, AABB.unitCubeFromLowerCorner(center).inflate(range, range, range),
                    e -> (filter == null || e.is(filter)) && shape.isInRange(center, e.position(), range), entities);


            List<EntityHolder> list = new ArrayList<>(entities.stream().map(EntityHolder::new).toList());
            List<EntityHolder> previous = mode.shouldSavePrevious() ? spellRunner.getOrCreateList(setPreviousTarget, StoredValueType.ENTITY) : List.of();
            List<EntityHolder> selection = spellRunner.getOrCreateList(entityTargets, StoredValueType.ENTITY);
            mode.apply(selection, list, previous);
            return !selection.isEmpty();
        });
    }

    public static class Builder {
        private HolderSet<EntityType<?>> filter;
        private float range = 1;
        private Shape shape = Shape.CUBE;
        private SelectionMode mode = SelectionMode.REPLACE;
        private String entityTarget = "entities";
        private String centerTarget = "actor";
        private String setPreviousTarget = "previous_blocks";


        public Builder filter(HolderSet<EntityType<?>> filter) {
            this.filter = filter;
            return this;
        }

        public Builder filter(EntityType<?> entity) {
            filter(HolderSet.direct(BuiltInRegistries.ENTITY_TYPE.wrapAsHolder(entity)));
            return this;
        }

        public Builder filter(@NotNull Stream<? extends EntityType<?>> stream) {
            filter(HolderSet.direct(stream.map(BuiltInRegistries.ENTITY_TYPE::createIntrusiveHolder).toList()));
            return this;
        }

        public Builder filter(EntityType<?>... entities) {
            filter(Arrays.stream(entities));
            return this;
        }

        public Builder range(float range) {
            this.range = range;
            return this;
        }

        public Builder shape(Shape shape) {
            this.shape = shape;
            return this;
        }

        public Builder setMode(SelectionMode mode) {
            this.mode = mode;
            return this;
        }

        public Builder entityTarget(String entityTarget) {
            this.entityTarget = entityTarget;
            return this;
        }

        public Builder centerTarget(String centerTarget) {
            this.centerTarget = centerTarget;
            return this;
        }

        public Builder setSetPreviousTarget(String setPreviousTarget) {
            this.setPreviousTarget = setPreviousTarget;
            return this;
        }

        public NearbyEntitiesTarget build() {
            return new NearbyEntitiesTarget(range, filter, shape, mode, entityTarget, centerTarget, setPreviousTarget);
        }
    }

}
