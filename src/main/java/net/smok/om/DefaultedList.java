package net.smok.om;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class DefaultedList<E> extends AbstractList<E> {
    private final List<E> list;
    private final @Nullable E defaultValue;

    public static <E> Codec<DefaultedList<E>> dynamicCodec(Codec<E> elementCodec, E defaultValue) {
        return RecordCodecBuilder.create(instance -> instance.group(
                Codec.INT.fieldOf("size").forGetter(DefaultedList::size),
                Codec.unboundedMap(Codec.STRING, elementCodec).fieldOf("elements").forGetter(list -> {
                    HashMap<String, E> stringMap = new HashMap<>();
                    list.toMap().forEach((integer, e) -> {
                        if (e != null) {
                            stringMap.put(String.valueOf(integer), e);
                        }
                    });
                    return stringMap;
                })
        ).apply(instance, (size, map) -> {
            HashMap<Integer, E> intMap = new HashMap<>();
            map.forEach((s, e) -> {
                try {
                    intMap.put(Integer.parseInt(s), e);
                } catch (NumberFormatException _) {}
            });
            return fromMap(defaultValue, intMap, size);
        }));
    }

    public static <E> Codec<DefaultedList<E>> codec(Codec<E> elementCodec, E defaultValue, int size) {
        return Codec.unboundedMap(Codec.STRING, elementCodec).xmap(
                map -> {
                    HashMap<Integer, E> intMap = new HashMap<>();
                    map.forEach((s, e) -> {
                        try {
                            intMap.put(Integer.parseInt(s), e);
                        } catch (NumberFormatException _) {}
                    });
                    return fromMap(defaultValue, intMap, size);
                },
                list -> {
                    HashMap<String, E> stringMap = new HashMap<>();
                    list.toMap().forEach((integer, e) -> stringMap.put(String.valueOf(integer), e));
                    return stringMap;
                }
        );
    }


    public static <B extends ByteBuf, E>StreamCodec<B, DefaultedList<E>> dynamicStreamCodec(StreamCodec<B, E> elementCodec, E defaultValue) {
        return new StreamCodec<>() {
            private final StreamCodec<B, Map<Integer, E>> mapCodec = ByteBufCodecs.map(
                    HashMap::new,
                    ByteBufCodecs.VAR_INT,
                    elementCodec
            );

            @Override
            public DefaultedList<E> decode(B buffer) {
                int size = ByteBufCodecs.VAR_INT.decode(buffer);
                Map<Integer, E> map = mapCodec.decode(buffer);
                return fromMap(defaultValue, map, size);
            }

            @Override
            public void encode(B buffer, DefaultedList<E> list) {
                ByteBufCodecs.VAR_INT.encode(buffer, list.size());
                mapCodec.encode(buffer, list.toMap());
            }
        };
    }

    public static <B extends ByteBuf, E>StreamCodec<B, DefaultedList<E>> streamCodec(StreamCodec<B, E> elementCodec, E defaultValue, int size) {
        StreamCodec<B, Map<Integer, E>> mapCodec = ByteBufCodecs.map(
                HashMap::new,
                ByteBufCodecs.VAR_INT,
                elementCodec
        );

        return mapCodec.map(
                map -> fromMap(defaultValue, map, size),
                DefaultedList::toMap
        );
    }

    protected Map<Integer, E> toMap() {
        Map<Integer, E> map = new HashMap<>();
        for (int i = 0; i < list.size(); i++) {
            E element = list.get(i);
            if (!Objects.equals(element, defaultValue)) {
                map.put(i, element);
            }
        }
        return map;
    }

    protected static <E> DefaultedList<E> fromMap(E defaultValue, Map<Integer, E> map, int size) {
        DefaultedList<E> list = new DefaultedList<>(size, defaultValue);
        map.forEach((index, element) -> {
            if (index >= 0 && index < size) list.set(index, element);
        });
        return list;
    }


    public DefaultedList<E> copy() {
        DefaultedList<E> cloned = new DefaultedList<>(this.size(), this.defaultValue);
        for (int i = 0; i < this.list.size(); i++) {
            cloned.list.set(i, this.list.get(i));
        }
        return cloned;
    }


    public static <E> DefaultedList<E> create(int size) {
        return new DefaultedList<>(size, null);
    }

    public static <E> DefaultedList<E> create(int size, E defaultElement) {
        return new DefaultedList<>(size, defaultElement);
    }

    protected DefaultedList(int size, @Nullable E defaultValue) {
        this.list = new ArrayList<>(size);
        this.defaultValue = defaultValue;
        for (int i = 0; i < size; i++) list.add(defaultValue);
    }

    @Override
    public E get(int index) {
        return list.size() > index && list.get(index) != null ? list.get(index) : defaultValue;
    }

    @Override
    public int size() {
        return list.size();
    }

    @Override
    public E set(int index, E element) {
        return list.set(index, element);
    }

    @Override
    public void add(int index, E element) {
        list.add(index, element);
    }

    @Override
    public E remove(int index) {
        return list.remove(index);
    }
}
