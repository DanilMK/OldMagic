package net.smok.om.utils;

import com.mojang.serialization.Codec;
import net.minecraft.server.level.ServerLevel;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class StoredValues {

    public static final Codec<StoredValues> CODEC = Codec.<StoredValueType<?>, Object>dispatchedMap(
            StoredValueType.TYPE_CODEC,
            StoredValueType::valueCodec
    ).xmap(StoredValues::new, storedValues -> storedValues.map);

    private final Map<StoredValueType<?>, Object> map;

    public StoredValues() {
        map = new HashMap<>();
    }

    public StoredValues(Map<StoredValueType<?>, Object> map) {
        this.map = new HashMap<>(map);
    }

    public void init(ServerLevel level) {
        for (Map.Entry<? extends StoredValueType<?>, ?> entry : map.entrySet()) {
            //noinspection unchecked
            StoredValueType<Object> storedValueType = (StoredValueType<Object>) entry.getKey();
            Object o = entry.getValue();
            if (storedValueType.levelInitHandler() != null)
                storedValueType.levelInitHandler().accept(o, level);
        }

    }

    public <T> void put(StoredValueType<T> type, T value) {
        if (value == null) map.remove(type);
        else map.put(type, value);
    }

    public <T> void putIfAbsent(StoredValueType<T> type, T value) {
        if (value == null) map.remove(type);
        else map.putIfAbsent(type, value);
    }

    public <T> T get(StoredValueType<T> type) {
        //noinspection unchecked
        return (T) map.get(type);
    }

    public <T> T getOrCreateDefault(StoredValueType<T> type) {
        if (map.containsKey(type)) //noinspection unchecked
            return (T) map.get(type);
        if (type.defaultValue() == null) return null;

        T t = type.defaultValue().get();
        if (t != null) put(type, t);
        return t;
    }

    public <T> Optional<T> getOptional(StoredValueType<T> type) {
        if (map.containsKey(type)) //noinspection unchecked
            return Optional.of((T) map.get(type));
        if (type.defaultValue() != null) return Optional.ofNullable(type.defaultValue().get());
        return Optional.empty();
    }


}
