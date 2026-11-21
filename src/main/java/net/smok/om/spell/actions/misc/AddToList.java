package net.smok.om.spell.actions.misc;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.smok.om.spell.SpellRunner;
import net.smok.om.spell.actions.SpellAction;
import net.smok.om.spell.actions.SpellActionType;
import net.smok.om.utils.StoredValueType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;

public record AddToList(String target, String list, @Nullable StoredValueType<?> castTo) implements SpellAction {

    public static final MapCodec<AddToList> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codec.STRING.fieldOf("target").forGetter(AddToList::target),
            Codec.STRING.fieldOf("list").forGetter(AddToList::list),
            StoredValueType.TYPE_CODEC.optionalFieldOf("cast_to").forGetter(addToList -> Optional.ofNullable(addToList.castTo))
    ).apply(instance, (s, s2, storedValueType) -> new AddToList(s, s2, storedValueType.orElse(null))));

    public static Holder<SpellAction> create(String targetName, String listName) {
        return new AddToList(targetName, listName, null).inline();
    }

    public static Holder<SpellAction> create(String targetName, String listName, StoredValueType<?> castTo) {
        return new AddToList(targetName, listName, castTo).inline();
    }

    @Override
    public SpellActionType<?> actionType() {
        return SpellActionType.ADD_TO_LIST;
    }

    @Override
    public SpellRunner.ProcessStatus run(SpellRunner spellRunner) {
        if (castTo != null) return addToList(spellRunner, castTo);

        StoredValueType.Value<?> single = spellRunner.getAny(target);
        if (single != null) {
            addToList(spellRunner, single);
            return SpellRunner.ProcessStatus.SUCCESS;
        }

        return SpellRunner.ProcessStatus.FAILURE;
    }

    private <T> SpellRunner.ProcessStatus addToList(SpellRunner spellRunner, StoredValueType<T> type) {
        T single = spellRunner.get(target, type);
        if (single != null) {
            List<T> list = spellRunner.getOrCreateList(this.list, type);
            list.add(single);
            return SpellRunner.ProcessStatus.SUCCESS;
        }
        return SpellRunner.ProcessStatus.FAILURE;
    }

    private <T> void addToList(@NotNull SpellRunner spellRunner, @NotNull StoredValueType.Value<T> value) {
        List<T> orCreateList = spellRunner.getOrCreateList(list, value.type());
        orCreateList.add(value.value());
    }
}
