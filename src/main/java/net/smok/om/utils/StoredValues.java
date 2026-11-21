package net.smok.om.utils;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.server.level.ServerLevel;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class StoredValues {

    public static final Codec<StoredValues> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.unboundedMap(Codec.STRING,
                    StoredValueType.TYPE_CODEC.<StoredValueType.Value<?>>
                            dispatch(StoredValueType.Value::type, StoredValueType::valueMapCodec))
                    .fieldOf("singles").forGetter(values -> values.singles),

            Codec.unboundedMap(Codec.STRING, StoredValueType.TYPE_CODEC.<StoredValueType.ListValue<?>>
                            dispatch(StoredValueType.ListValue::type, StoredValueType::listValueMapCodec))
                    .fieldOf("lists").forGetter(values -> values.lists)
    ).apply(instance, StoredValues::new));


    private final Map<String, StoredValueType.Value<?>> singles;
    private final Map<String, StoredValueType.ListValue<?>> lists;

    public StoredValues() {
        singles = new HashMap<>();
        lists = new HashMap<>();
    }

    public StoredValues(Map<String, StoredValueType.Value<?>> singles, Map<String, StoredValueType.ListValue<?>> lists) {
        this.singles = new HashMap<>(singles);
        this.lists = new HashMap<>(lists);
    }

    public void init(ServerLevel level) {
        singles.values().forEach(value -> value.init(level));
        lists.values().forEach(listValue -> listValue.init(level));
    }


    public void put(String key, StoredValueType.Value<?> value) {
        singles.put(key, value);
    }

    public <T> void put(String key, StoredValueType<T> type, T value) {
        if (value == null) singles.remove(key);
        else singles.put(key, new StoredValueType.Value<>(type, value));
    }

    public <T> void putIfAbsent(String key, StoredValueType<T> type, T value) {
        if (value != null) singles.putIfAbsent(key, new StoredValueType.Value<>(type, value));
    }

    public void putAll(@NotNull StoredValues values) {
        this.singles.putAll(values.singles);
        this.lists.putAll(values.lists);
    }

    public <T> T get(@NotNull String key, @NotNull StoredValueType<T> type) {
        StoredValueType.Value<?> value = singles.get(key);
        if (value == null) return null;
        if (value.type().equals(type)) //noinspection unchecked
            return (T) value.value();
        return value.toType(type).orElse(null);
    }

    public @Nullable StoredValueType.Value<?> getAny(@NotNull String key) {
        return singles.get(key);
    }

    public <T> T getOrCreateDefault(String key, StoredValueType<T> type) {
        if (singles.containsKey(key)) return get(key, type);
        if (type.defaultValue() == null) return null;
        T result = type.defaultValue().get();
        if (result != null) put(key, type, result);
        return result;
    }

    public <T> List<T> getOrCreateList(String key, StoredValueType<T> type) {
        if (lists.containsKey(key)) {
            StoredValueType.ListValue<?> listValue = lists.get(key);
            if (listValue.type().equals(type)) {
                //noinspection unchecked
                return (List<T>) listValue.list();
            }
        }

        StoredValueType.ListValue<T> list = new StoredValueType.ListValue<>(type, new ArrayList<>());
        lists.put(key, list);
        return list.list();
    }

    public @Nullable StoredValueType.ListValue<?> getAnyList(@NotNull String key) {
        return lists.get(key);
    }

    public <T> void replaceList(String key, StoredValueType<T> type, ArrayList<T> list) {
        lists.put(key, new StoredValueType.ListValue<>(type, list));
    }

    public void replaceList(String key, StoredValueType.ListValue<?> list) {
        lists.put(key, list);
    }


    public <T> Optional<T> getOptional(String key, StoredValueType<T> type) {
        StoredValueType.Value<?> value = singles.get(key);
        if (value == null) {
            if (type.defaultValue() == null) return Optional.empty();
            return Optional.ofNullable(type.defaultValue().get());
        }
        if (value.type().equals(type)) //noinspection unchecked
            return Optional.ofNullable((T) value.value());
        return value.toType(type);
    }

    public int singlesSize() {
        return singles.size();
    }

    public int listsSize() {
        return lists.size();
    }

}
