package net.smok.om.spell.actions.misc;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.smok.om.spell.SpellRunner;
import net.smok.om.spell.actions.SpellAction;
import net.smok.om.spell.actions.SpellActionType;
import net.smok.om.utils.StoredValueType;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public record ResaveTarget(String getTargetName, String setTargetName, @Nullable StoredValueType<?> castTo) implements SpellAction {

    public static final MapCodec<ResaveTarget> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codec.STRING.fieldOf("get_target_name").forGetter(ResaveTarget::getTargetName),
            Codec.STRING.fieldOf("set_target_name").forGetter(ResaveTarget::setTargetName),
            StoredValueType.TYPE_CODEC.optionalFieldOf("cast_to").forGetter(resaveTarget -> Optional.ofNullable(resaveTarget.castTo()))
    ).apply(instance, (s, s2, s3) -> new ResaveTarget(s, s2, s3.orElse(null))));

    @Contract("_, _ -> new")
    public static @NotNull Holder<SpellAction> create(String get, String set) {
        return new ResaveTarget(get, set, null).inline();
    }

    @Contract("_, _, _ -> new")
    public static @NotNull Holder<SpellAction> create(String get, String set, StoredValueType<?> castTo) {
        return new ResaveTarget(get, set, castTo).inline();
    }

    @Override
    public SpellActionType<?> actionType() {
        return SpellActionType.RESAVE_TARGET;
    }

    @Override
    public SpellRunner.ProcessStatus run(SpellRunner spellRunner) {
        if (castTo != null) return cast(spellRunner, castTo);
        return any(spellRunner);
    }

    private <T> @NotNull SpellRunner.ProcessStatus cast(SpellRunner spellRunner, StoredValueType<T> type) {
        T single = spellRunner.get(getTargetName, type);
        if (single != null) {
            spellRunner.put(setTargetName, type, single);
            return SpellRunner.ProcessStatus.SUCCESS;
        }

        List<T> list = spellRunner.getOrCreateList(getTargetName, type);
        if (!list.isEmpty()) {
            spellRunner.replaceList(setTargetName, type, new ArrayList<>(list));
            return SpellRunner.ProcessStatus.SUCCESS;
        }

        return SpellRunner.ProcessStatus.FAILURE;
    }

    private @NotNull SpellRunner.ProcessStatus any(SpellRunner spellRunner) {
        StoredValueType.Value<?> single = spellRunner.getAny(getTargetName);
        if (single != null) {
            spellRunner.put(setTargetName, single);
            return SpellRunner.ProcessStatus.SUCCESS;
        }

        StoredValueType.ListValue<?> anyList = spellRunner.getAnyList(getTargetName);
        if (anyList != null) {
            spellRunner.replaceList(setTargetName, anyList);
            return SpellRunner.ProcessStatus.SUCCESS;
        }

        return SpellRunner.ProcessStatus.FAILURE;
    }
}
