package net.smok.om.utils;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.Vec3;
import net.smok.om.Values;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

public record StoredValueType<T>(@NotNull Codec<T> valueCodec, @Nullable BiConsumer<T, ServerLevel> levelInitHandler, @Nullable Supplier<T> defaultValue) {

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

    public static final StoredValueType<BlockPos> BLOCK = register("block", builder(BlockPos.CODEC));
    public static final StoredValueType<ArrayList<BlockPos>> BLOCKS = register("blocks",
            builder(BlockPos.CODEC.listOf().xmap(ArrayList::new, list -> list))
                    .setDefaultValue(ArrayList::new)
    );

    public static final StoredValueType<EntityHolder> ENTITY = register("entity", builder(EntityHolder.CODEC).setLevelInitHandler(EntityHolder::init));
    public static final StoredValueType<ArrayList<EntityHolder>> ENTITIES = register("entities",
            builder(EntityHolder.CODEC.listOf().xmap(ArrayList::new, list -> list))
                    .setLevelInitHandler((entityHolders, level) -> entityHolders.forEach(entityHolder -> entityHolder.init(level)))
                    .setDefaultValue(ArrayList::new)
    );

    public static final StoredValueType<Vec3> POSITION = register("position", builder(Vec3.CODEC));
    public static final StoredValueType<ArrayList<Vec3>> POSITIONS = register("positions",
            builder(Vec3.CODEC.listOf().xmap(ArrayList::new, list -> list))
                    .setDefaultValue(ArrayList::new)
    );

    public static final StoredValueType<HitHolder> HIT = register("hit", builder(HitHolder.CODEC).setLevelInitHandler(HitHolder::init));



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

        public StoredValueType<T> build() {
            return new StoredValueType<>(codec, levelInitHandler, defaultValue);
        }
    }
}
