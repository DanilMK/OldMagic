package net.smok.om.spell.actions.misc;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.smok.om.spell.SpellRunner;
import net.smok.om.spell.actions.SpellAction;
import net.smok.om.spell.actions.SpellActionType;
import net.smok.om.spell.cast.PlayerCaster;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public record ToggleAction(@Nullable Holder<SpellAction> onStart, @Nullable Holder<SpellAction> onEnd, @Nullable Holder<SpellAction> onTick, int delay) implements SpellAction{

    public static final MapCodec<ToggleAction> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            SpellAction.HOLDER_CODEC.optionalFieldOf("onStart").forGetter(toggleAction -> Optional.ofNullable(toggleAction.onStart)),
            SpellAction.HOLDER_CODEC.optionalFieldOf("onEnd").forGetter(toggleAction -> Optional.ofNullable(toggleAction.onEnd)),
            SpellAction.HOLDER_CODEC.optionalFieldOf("onTick").forGetter(toggleAction -> Optional.ofNullable(toggleAction.onTick)),
            Codec.INT.fieldOf("delay").forGetter(ToggleAction::delay)
    ).apply(instance, (onStart, onEnd, onTick, delay) ->
            new ToggleAction(onStart.orElse(null), onEnd.orElse(null), onTick.orElse(null), delay)));

    @Contract("_, _, _, _ -> new")
    public static @NotNull ToggleAction all(@Nullable Holder<SpellAction> onStart, @Nullable Holder<SpellAction> onEnd, @Nullable Holder<SpellAction> onTick, int delay) {
        return new ToggleAction(onStart, onEnd, onTick, delay);
    }

    @Contract("_ -> new")
    public static @NotNull ToggleAction end(Holder<SpellAction> onEnd) {
        return new ToggleAction(null, onEnd, null, 0);
    }

    @Contract("_ -> new")
    public static @NotNull ToggleAction start(Holder<SpellAction> onStart) {
        return new ToggleAction(onStart, null, null, 0);
    }

    @Contract("_, _ -> new")
    public static @NotNull ToggleAction startEnd(Holder<SpellAction> onStart, Holder<SpellAction> onEnd) {
        return new ToggleAction(onStart, onEnd, null, 0);
    }

    @Contract("_, _ -> new")
    public static @NotNull ToggleAction tick(Holder<SpellAction> onTick, int delay) {
        return new ToggleAction(null, null, onTick, delay);
    }

    @Override
    public SpellActionType<?> actionType() {
        return SpellActionType.TOGGLE;
    }

    @Override
    public SpellRunner.ProcessStatus run(@NotNull SpellRunner spellRunner) {
        if (spellRunner.caster() instanceof PlayerCaster caster) {
            if (caster.oldMagic$isToggled(spellRunner.getSpell())) {
                caster.oldMagic$disableToggle(spellRunner.getSpell());
                return SpellRunner.ProcessStatus.SUCCESS;
            } else {
                boolean b;
                if (onStart != null) b = onStart.value().run(spellRunner) == SpellRunner.ProcessStatus.SUCCESS;
                else b = true;
                return spellRunner.execute(_ -> b && caster
                        .oldMagic$enableToggleServer(BuiltInRegistries.ITEM.wrapAsHolder(spellRunner.getWand().getItem()), spellRunner.getSpell(), onEnd, onTick, delay, spellRunner.getStoredValues()));
            }
        }
        return SpellRunner.ProcessStatus.FAILURE;
    }
}
