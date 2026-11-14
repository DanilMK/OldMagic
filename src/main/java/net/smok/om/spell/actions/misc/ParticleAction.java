package net.smok.om.spell.actions.misc;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.particles.ParticleOptions;
import net.smok.om.spell.SpellRunner;
import net.smok.om.spell.actions.SpellAction;
import net.smok.om.spell.actions.SpellActionType;
import net.smok.om.utils.BlockParticleTemplate;
import net.smok.om.utils.StoredValueType;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.function.UnaryOperator;

public record ParticleAction(String target, BlockParticleTemplate particleTemplate) implements SpellAction {

    public static final MapCodec<ParticleAction> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codec.STRING.optionalFieldOf("target", "position").forGetter(ParticleAction::target),
            BlockParticleTemplate.CODEC.fieldOf("particleTemplate").forGetter(ParticleAction::particleTemplate)
    ).apply(instance, ParticleAction::new));

    @Contract("_, _, _ -> new")
    public static @NotNull SpellAction builder(@NotNull String target, @NotNull ParticleOptions particleOptions, @NotNull UnaryOperator<BlockParticleTemplate.Builder> builder) {
        BlockParticleTemplate.Builder builder1 = new BlockParticleTemplate.Builder(particleOptions);
        return new ParticleAction(target, builder.apply(builder1).build());
    }

    @Override
    public SpellActionType<?> actionType() {
        return SpellActionType.PARTICLE;
    }

    @Override
    public SpellRunner.ProcessStatus run(SpellRunner spellRunner) {
        return spellRunner.executeFor(target, StoredValueType.POSITION,
                (level, position) -> level.players().stream().anyMatch(
                        serverPlayer -> particleTemplate.addParticlesServer(level, serverPlayer, position)));
    }

}
