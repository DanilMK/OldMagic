package net.smok.om.spell.actions.block;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.StateHolder;
import net.minecraft.world.level.block.state.properties.Property;
import net.smok.om.spell.SpellRunner;
import net.smok.om.spell.actions.SpellAction;
import net.smok.om.spell.actions.SpellActionType;
import net.smok.om.utils.StoredValueType;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Optional;

public record BlockStateTransform(List<PropertyChanger> properties) implements SpellAction {

    private static final RandomProperty RANDOM_PROPERTY = new RandomProperty();
    private static final CycleProperty CYCLE_PROPERTY = new CycleProperty();

    public static final MapCodec<BlockStateTransform> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            PropertyChanger.CODEC.listOf().fieldOf("properties").forGetter(BlockStateTransform::properties)
    ).apply(instance, BlockStateTransform::new));

    @Contract(" -> new")
    public static @NotNull Builder builder() {
        return new Builder();
    }

    @Override
    public SpellActionType<?> actionType() {
        return SpellActionType.BLOCK_STATE_TRANSFORM;
    }

    @Override
    public SpellRunner.ProcessStatus run(SpellRunner spellRunner) {
        return spellRunner.executeFor(StoredValueType.BLOCK, (level, blockPos) -> {
            BlockState actualState = level.getBlockState(blockPos);
            BlockState blockState = actualState;
            StateDefinition<Block, BlockState> definition = blockState.getBlock().getStateDefinition();
            for (PropertyChanger property : properties) {
                blockState = property.changeProperty(definition, blockState, spellRunner.getRandom());
            }
            boolean b = actualState != blockState;
            if (b) level.setBlock(blockPos, blockState, 2);
            return b;
        });
    }


    public record PropertyChanger(String name, ValueChanger valueChanger) {
        public static final Codec<PropertyChanger> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.fieldOf("name").forGetter(PropertyChanger::name),
            ValueChanger.CODEC.fieldOf("valueChanger").forGetter(PropertyChanger::valueChanger)
        ).apply(instance, PropertyChanger::new));


        public <S extends StateHolder<?, S>> S changeProperty(final StateDefinition<?, S> definition, final S state, RandomSource random) {
            Property<?> property = definition.getProperty(name);
            if (property != null) return valueChanger().changeProperty(state, property, random);
            return state;
        }
    }

    private static class CycleProperty implements ValueChanger {

        @Override
        public <T extends Comparable<T>, S extends StateHolder<?, S>> S changeProperty(S state, Property<T> property, RandomSource random) {
            return state.cycle(property);
        }
    }

    private static class RandomProperty implements ValueChanger {

        @Override
        public <T extends Comparable<T>, S extends StateHolder<?, S>> S changeProperty(S state, Property<T> property, RandomSource random) {
            T actualValue = state.getValue(property);
            List<T> possibleValues = property.getPossibleValues();
            possibleValues.remove(actualValue);
            T value = possibleValues.get(random.nextInt(possibleValues.size()));
            return state.setValue(property, value);
        }
    }

    private record SetProperty(String value) implements ValueChanger {

        @Override
        public <T extends Comparable<T>, S extends StateHolder<?, S>> S changeProperty(S state, Property<T> property, RandomSource random) {
            T actualValue = state.getValue(property);
            Optional<T> typedValue = property.getValue(value);
            boolean b = typedValue.isPresent() && actualValue.compareTo(typedValue.get()) != 0;
            if (b) return state.setValue(property, typedValue.get());
            return state;
        }
    }

    private interface ValueChanger {

        Codec<ValueChanger> CODEC = Codec.STRING.xmap(string -> {
            if ("cycle".equals(string)) return CYCLE_PROPERTY;
            if ("random".equals(string)) return RANDOM_PROPERTY;
            return new SetProperty(string);
        }, valueChanger -> {
            if (valueChanger instanceof CycleProperty) return "cycle";
            if (valueChanger instanceof RandomProperty) return "random";
            if (valueChanger instanceof SetProperty(String value)) return value;
            throw new UnsupportedOperationException();
        });

        <T extends Comparable<T>, S extends StateHolder<?, S>> S changeProperty(S state, Property<T> property, RandomSource random);
    }

    public static class Builder {
        private final ImmutableList.Builder<PropertyChanger> list = new ImmutableList.Builder<>();

        public <T extends Comparable<T>> Builder set(Property<T> property, T value) {
            list.add(new PropertyChanger(property.getName(), new SetProperty(value.toString())));
            return this;
        }

        public Builder cycle(Property<?> property) {
            list.add(new PropertyChanger(property.getName(), CYCLE_PROPERTY));
            return this;
        }

        public Builder random(Property<?> property) {
            list.add(new PropertyChanger(property.getName(), RANDOM_PROPERTY));
            return this;
        }

        public BlockStateTransform build() {
            return new BlockStateTransform(list.build());
        }
    }
}
