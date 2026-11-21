package net.smok.om.utils;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import net.smok.om.Values;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.BiPredicate;
import java.util.function.Function;

public record StoredValueType<T>(@NotNull Codec<T> codec, @Nullable BiPredicate<T, ServerLevel> levelInitHandler, Function<Object, Optional<T>> convertor) {

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
            builder(EntityHolder.CODEC).setLevelInitHandler(EntityHolder::init).setConvertor(o -> {
                if (o instanceof HitHolder hitHolder) return hitHolder.optionalEntityHolder();
                return Optional.empty();
            })
    );

    public static final StoredValueType<BlockPos> BLOCK = register("block",
            builder(BlockPos.CODEC)
                    .setConvertor(o -> {
                        if (o instanceof HitHolder hitHolder) return Optional.of(hitHolder.blockPos());
                        if (o instanceof EntityHolder entity) return Optional.ofNullable(entity.getEntity()).map(Entity::blockPosition);
                        return Optional.empty();
                    })
    );

    public static final StoredValueType<Vec3> POSITION = register("position",
            builder(Vec3.CODEC)
                    .setConvertor(o -> {
                        if (o instanceof BlockPos blockPos) return Optional.of(blockPos.getCenter());
                        if (o instanceof EntityHolder entityHolder) return Optional.ofNullable(entityHolder.getEntity()).map(Entity::position);
                        if (o instanceof HitHolder hit) return Optional.of(hit.position());
                        return Optional.empty();
                    })
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
        @Nullable private BiPredicate<T, ServerLevel> levelInitHandler;
        @NotNull private Function<Object, Optional<T>> convertor = _ -> Optional.empty();


        public Builder(Codec<T> codec) {
            this.codec = codec;
        }

        public Builder<T> setCodec(Codec<T> codec) {
            this.codec = codec;
            return this;
        }

        public Builder<T> setLevelInitHandler(@Nullable BiPredicate<T, ServerLevel> levelInitHandler) {
            this.levelInitHandler = levelInitHandler;
            return this;
        }

        public Builder<T> setConvertor(@NotNull Function<Object, Optional<T>> convertor) {
            this.convertor = convertor;
            return this;
        }

        public StoredValueType<T> build() {
            return new StoredValueType<>(codec, levelInitHandler, convertor);
        }
    }

    public record Value<T>(StoredValueType<T> type, T value) {
        public boolean init(ServerLevel level) {
            if (type.levelInitHandler != null) return type.levelInitHandler.test(value, level);
            return false;
        }

        public <V> Optional<V> toType(StoredValueType<V> type) {
            return type.convertor.apply(value);
        }
    }

    public record ListValue<T>(StoredValueType<T> type, ArrayList<T> list) {
        public void init(ServerLevel level) {
            if (type.levelInitHandler != null) list.removeIf(value ->  type.levelInitHandler.test(value, level));
        }

        public <V> List<V> toTypeImmutable(StoredValueType<V> type) {
            return list.stream().map(type.convertor).filter(Optional::isPresent).map(Optional::get).toList();
        }
    }
}
