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
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.Stream;

public record NearbyEntitiesTarget(float range, HolderSet<EntityType<?>> filter) implements SpellAction {

    public static final MapCodec<NearbyEntitiesTarget> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codec.FLOAT.fieldOf("range").forGetter(NearbyEntitiesTarget::range),
            HolderSetCodec.create(Registries.ENTITY_TYPE, BuiltInRegistries.ENTITY_TYPE.holderByNameCodec(), false).fieldOf("filter").forGetter(NearbyEntitiesTarget::filter)
    ).apply(instance, NearbyEntitiesTarget::new));

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


    public NearbyEntitiesTarget(float range, EntityType<?> entity) {
        this(range, HolderSet.direct(BuiltInRegistries.ENTITY_TYPE.wrapAsHolder(entity)));
    }

    public NearbyEntitiesTarget(float range, EntityType<?>... entities) {
        this(range, Arrays.stream(entities));
    }

    public NearbyEntitiesTarget(float range, @NotNull Stream<? extends EntityType<?>> stream) {
        this(range, HolderSet.direct(stream.map(BuiltInRegistries.ENTITY_TYPE::createIntrusiveHolder).toList()));
    }


    @Override
    public SpellActionType<?> actionType() {
        return SpellActionType.TARGET_NEARBY_ENTITIES;
    }

    @Override
    public SpellRunner.ProcessStatus run(SpellRunner spellRunner) {
        return spellRunner.execute(level -> {
            ArrayList<Entity> entities = new ArrayList<>();
            level.getEntities(ANY_ENTITY, AABB.unitCubeFromLowerCorner(spellRunner.getActor().position()).inflate(range, range, range), e -> e.is(filter), entities);
            spellRunner.replaceEntityTargets(entities);
            return !entities.isEmpty();
        });
    }
}
