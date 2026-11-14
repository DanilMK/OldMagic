package net.smok.om.utils;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.Vec3;
import net.smok.om.Values;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

public record StoredValueType<T>(@NotNull Codec<T> codec, @Nullable BiConsumer<T, ServerLevel> levelInitHandler, @Nullable Supplier<T> defaultValue, @NotNull List<Converter<?, T>> converters) {

    public static final Codec<StoredValueType<?>> TYPE_CODEC = Codec.STRING.flatXmap(string -> {
        Identifier id = Identifier.tryParse(string);
        StoredValueType<?> value = Values.STORED_VALUE_TYPES.getValue(id);
        if (value == null) return DataResult.error(() -> "No component with type: '" + id + "'");
        else return DataResult.success(value);
    }, key -> {
        Identifier id = Values.STORED_VALUE_TYPES.getKey(key);
        return id == null ? DataResult.error(() -> "Unregistered component: " + key) : DataResult.success(id.toString());
    });

    public static void init() {}

    public static final StoredValueType<HitHolder> HIT = register("hit", builder(HitHolder.CODEC).setLevelInitHandler(HitHolder::init));

    public static final StoredValueType<EntityHolder> ENTITY = register("entity",
            builder(EntityHolder.CODEC).setLevelInitHandler(EntityHolder::init).addConverter(HIT, HitHolder::entity)
    );

    public static final StoredValueType<BlockPos> BLOCK = register("block",
            builder(BlockPos.CODEC)
                    .addConverter(HIT, HitHolder::blockPos)
                    .addConverter(ENTITY, entityHolder -> entityHolder.getEntity().blockPosition())
    );

    public static final StoredValueType<Vec3> POSITION = register("position",
            builder(Vec3.CODEC)
                    .addConverter(BLOCK, BlockPos::getCenter)
                    .addConverter(ENTITY, entityHolder -> entityHolder.getEntity().position())
                    .addConverter(HIT, HitHolder::position)
    );

    public Value<T> value(T value) {
        return new Value<>(this, value);
    }

    public ListValue<T> listValue(List<T> list) {
        return new ListValue<>(this, new ArrayList<>(list));
    }

    public MapCodec<Value<T>> valueMapCodec() {
        return codec.xmap(this::value, Value::value).fieldOf("value");
    }

    public MapCodec<ListValue<T>> listValueMapCodec() {
        return codec.listOf().xmap(this::listValue, ListValue::list).fieldOf("values");
    }


    private static <V> StoredValueType<V> register(final String id, Builder<V> builder) {
        return Registry.register(Values.STORED_VALUE_TYPES, Values.id(id), builder.build());
    }

    public static <V> StoredValueType<V> register(final Identifier id, Builder<V> builder) {
        return Registry.register(Values.STORED_VALUE_TYPES, id, builder.build());
    }


    public static <V> Builder<V> builder(Codec<V> codec) {
        return new Builder<>(codec);
    }

    public static class Builder<T> {
        @NotNull private Codec<T> codec;
        @Nullable private BiConsumer<T, ServerLevel> levelInitHandler;
        @Nullable private Supplier<T> defaultValue;
        @NotNull private final ImmutableList.Builder<Converter<?, T>> converters = new ImmutableList.Builder<>();


        public Builder(Codec<T> codec) {
            this.codec = codec;
        }

        public Builder<T> setCodec(Codec<T> codec) {
            this.codec = codec;
            return this;
        }

        public Builder<T> setLevelInitHandler(@Nullable BiConsumer<T, ServerLevel> levelInitHandler) {
            this.levelInitHandler = levelInitHandler;
            return this;
        }

        public Builder<T> setDefaultValue(@Nullable Supplier<T> defaultValue) {
            this.defaultValue = defaultValue;
            return this;
        }

        public <A> Builder<T> addConverter(StoredValueType<A> type, Function<A, T> function) {
            converters.add(new Converter<>(type, function));
            return this;
        }

        public StoredValueType<T> build() {
            return new StoredValueType<>(codec, levelInitHandler, defaultValue, converters.build());
        }
    }

    public record Converter<A, B>(StoredValueType<A> from, Function<A, B> function) {

        private B apply(Object from) {
            //noinspection unchecked
            return function().apply((A) from);
        }
    }

    public record Value<T>(StoredValueType<T> type, T value) {
        public void init(ServerLevel level) {
            if (type.levelInitHandler != null) type.levelInitHandler.accept(value, level);
        }

        public <V> Optional<V> toType(StoredValueType<V> type) {
            for (Converter<?, V> converter : type.converters()) {
                if (converter.from().equals(this.type)) return Optional.ofNullable(converter.apply(value));
            }
            return Optional.empty();
        }
    }

    public record ListValue<T>(StoredValueType<T> type, ArrayList<T> list) {
        public void init(ServerLevel level) {
            if (type.levelInitHandler != null) list.forEach(value ->  type.levelInitHandler.accept(value, level));
        }

        public <V> List<V> toTypeImmutable(StoredValueType<V> type) {
            for (Converter<?, V> converter : type.converters()) {
                if (converter.from().equals(this.type)) return list.stream().map(converter::apply).toList();
            }
            return List.of();
        }
    }
}
