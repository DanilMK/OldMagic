package net.smok.om.spell.actions.entity;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.smok.om.spell.SpellRunner;
import net.smok.om.spell.actions.SpellAction;
import net.smok.om.spell.actions.SpellActionType;
import net.smok.om.utils.EntityHolder;
import net.smok.om.utils.StoredValueType;

public record MobEffectAction(Holder<MobEffect> effect, int duration, int amplifier, boolean showParticle, String target, String effectSource) implements SpellAction {

    public static final MapCodec<MobEffectAction> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            MobEffect.CODEC.fieldOf("effect").forGetter(MobEffectAction::effect),
            Codec.INT.fieldOf("duration").forGetter(MobEffectAction::duration),
            Codec.INT.fieldOf("amplifier").forGetter(MobEffectAction::amplifier),
            Codec.BOOL.fieldOf("show_particle").forGetter(MobEffectAction::showParticle),
            Codec.STRING.optionalFieldOf("target", "entity").forGetter(MobEffectAction::target),
            Codec.STRING.optionalFieldOf("effect_source", "caster").forGetter(MobEffectAction::effectSource)
    ).apply(instance, MobEffectAction::new));

    @Override
    public SpellActionType<?> actionType() {
        return SpellActionType.MOB_EFFECT;
    }

    @Override
    public SpellRunner.ProcessStatus run(SpellRunner spellRunner) {
        return spellRunner.executeFor(target, StoredValueType.ENTITY, (_, entityHolder) -> {
            if (entityHolder.getEntity() instanceof LivingEntity livingEntity) {
                if (duration > 0) {
                    MobEffectInstance instance = new MobEffectInstance(effect, effect.value().isInstantenous() ? 1 : duration, amplifier, false, showParticle);
                    return livingEntity.addEffect(instance, spellRunner.getOptional(effectSource, StoredValueType.ENTITY).map(EntityHolder::getEntity).orElse(null));
                } else return livingEntity.removeEffect(effect);
            }
            return false;
        });
    }
}
