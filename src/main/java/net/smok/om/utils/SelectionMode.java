package net.smok.om.utils;

import com.mojang.serialization.Codec;
import net.minecraft.util.StringRepresentable;
import org.jetbrains.annotations.NotNull;

import java.util.Iterator;
import java.util.List;

public enum SelectionMode implements StringRepresentable {
    ADD("add", false),
    REPLACE("replace", false),
    SUBTRACT("subtract", false),
    INTERSECTION("intersection", false),
    REPLACE_WITHOUT_PREVIOUS("replace_without_previous", true);

    public static final Codec<SelectionMode> CODEC = StringRepresentable.fromEnum(SelectionMode::values);

    private final String name;
    private final boolean shouldSavePrevious;

    SelectionMode(String name, boolean shouldSavePrevious) {
        this.name = name;
        this.shouldSavePrevious = shouldSavePrevious;
    }

    public <T> void apply(@NotNull List<T> selection, @NotNull List<T> newList, @NotNull List<T> previousList) {
        switch (this) {
            case ADD -> newList.forEach(value -> {
                if (!selection.contains(value)) selection.add(value);
            });
            case REPLACE -> {
                selection.clear();
                selection.addAll(newList);
            }
            case REPLACE_WITHOUT_PREVIOUS -> {
                previousList.addAll(selection);
                selection.clear();
                newList.forEach(value -> {
                    if (!previousList.contains(value)) selection.add(value);
                });
            }
            case SUBTRACT -> selection.removeAll(newList);
            case INTERSECTION -> {
                Iterator<T> iterator = selection.iterator();
                while (iterator.hasNext()) {
                    T next = iterator.next();
                    if (newList.contains(next)) {
                        iterator.remove();
                        newList.remove(next);
                    }
                }
                selection.addAll(newList);
            }
        }
    }

    public boolean shouldSavePrevious() {
        return shouldSavePrevious;
    }

    @Override
    public String getSerializedName() {
        return name;
    }
}
