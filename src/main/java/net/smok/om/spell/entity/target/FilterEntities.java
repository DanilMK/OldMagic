package net.smok.om.spell.entity.target;


import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.HolderSetCodec;
import net.minecraft.world.entity.EntityType;
import net.smok.om.spell.SpellAction;
import net.smok.om.spell.SpellActionType;
import net.smok.om.spell.SpellRunner;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.stream.Stream;

public record FilterEntities(HolderSet<EntityType<?>> values) implements SpellAction {

    public static final Codec<HolderSet<EntityType<?>>> HOLDER_SET_CODEC =
            HolderSetCodec.create(Registries.ENTITY_TYPE, BuiltInRegistries.ENTITY_TYPE.holderByNameCodec(), false);

    public static final MapCodec<FilterEntities> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            HOLDER_SET_CODEC.fieldOf("entities").forGetter(FilterEntities::values)
    ).apply(instance, FilterEntities::new));


    @Contract("_ -> new")
    public static @NotNull FilterEntities of(EntityType<?> entity) {
        return new FilterEntities(HolderSet.direct(BuiltInRegistries.ENTITY_TYPE.wrapAsHolder(entity)));
    }

    @Contract("_ -> new")
    public static @NotNull FilterEntities of(EntityType<?>... entities) {
        return of(Arrays.stream(entities));
    }

    @Contract("_ -> new")
    public static @NotNull FilterEntities of(HolderSet<EntityType<?>> tag) {
        return new FilterEntities(tag);
    }

    @Contract("_ -> new")
    public static @NotNull FilterEntities of(Stream< ? extends EntityType<?>> stream) {
        return new FilterEntities(HolderSet.direct(stream.map(BuiltInRegistries.ENTITY_TYPE::createIntrusiveHolder).toList()));
    }


    @Override
    public SpellActionType<?> getType() {
        return SpellActionType.FILTER_ENTITIES;
    }

    @Override
    public SpellRunner.ProcessStatus run(SpellRunner spellRunner) {
        return spellRunner.replaceEntityTargets(spellRunner.getEntityTargets().stream().filter(entity -> entity.is(values)).toList());
    }
}
