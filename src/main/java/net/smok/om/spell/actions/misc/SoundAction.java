package net.smok.om.spell.actions.misc;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.resources.Identifier;
import net.smok.om.spell.SpellRunner;
import net.smok.om.spell.actions.SpellAction;
import net.smok.om.spell.actions.SpellActionType;
import net.smok.om.utils.EntityHolder;
import net.smok.om.utils.SoundTemplate;
import net.smok.om.utils.StoredValueType;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.function.UnaryOperator;

public record SoundAction(SoundTemplate template, String target, String source) implements SpellAction {
    public static final MapCodec<SoundAction> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            SoundTemplate.CODEC.fieldOf("template").forGetter(SoundAction::template),
            Codec.STRING.fieldOf("target").forGetter(SoundAction::target),
            Codec.STRING.optionalFieldOf("source", "actor").forGetter(SoundAction::source)
    ).apply(instance, SoundAction::new));

    @Contract("_, _, _, _ -> new")
    public static @NotNull SoundAction create(@NotNull Identifier sound, @NotNull String target, @NotNull String source, @NotNull UnaryOperator<SoundTemplate.Builder> builder) {
        return new SoundAction(builder.apply(new SoundTemplate.Builder(sound)).build(), target, source);
    }

    @Contract("_, _, _ -> new")
    public static @NotNull SoundAction create(@NotNull Identifier sound, @NotNull String target, @NotNull UnaryOperator<SoundTemplate.Builder> builder) {
        return new SoundAction(builder.apply(new SoundTemplate.Builder(sound)).build(), target, "actor");
    }

    @Override
    public SpellActionType<?> actionType() {
        return SpellActionType.SOUND;
    }

    @Override
    public SpellRunner.ProcessStatus run(@NotNull SpellRunner spellRunner) {
        return spellRunner.executeFor(target, StoredValueType.POSITION, (level, position) ->
                template.playSound(level, position, spellRunner.getOptional(source, StoredValueType.ENTITY)
                        .map(EntityHolder::getEntity).orElse(spellRunner.getActor()).getSoundSource()));
    }
}
