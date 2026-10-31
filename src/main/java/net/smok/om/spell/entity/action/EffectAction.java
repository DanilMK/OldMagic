package net.smok.om.spell.entity.action;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.alchemy.PotionContents;
import net.smok.om.spell.SpellAction;
import net.smok.om.spell.SpellActionType;
import net.smok.om.spell.SpellRunner;

public record EffectAction(PotionContents potionContents, float durationScale) implements SpellAction {

    public static final MapCodec<EffectAction> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            PotionContents.CODEC.fieldOf("potion").forGetter(EffectAction::potionContents),
            Codec.FLOAT.fieldOf("duration_scale").forGetter(EffectAction::durationScale)
    ).apply(instance, EffectAction::new));

    @Override
    public SpellActionType<?> getType() {
        return SpellActionType.POTION_EFFECT;
    }

    @Override
    public SpellRunner.ProcessStatus run(SpellRunner spellRunner) {
        Iterable<MobEffectInstance> mobEffects = potionContents.getAllEffects();
        return spellRunner.executeForAnyEntity((level, entity) -> {
            boolean check = false;

            if (entity instanceof LivingEntity livingEntity) {
                for (MobEffectInstance effectInstance : mobEffects) {

                    check = true;
                    Holder<MobEffect> effect = effectInstance.getEffect();
                    if (effect.value().isInstantenous()) {
                        effect.value().applyInstantenousEffect(level, spellRunner.getActor(), spellRunner.caster(), livingEntity, effectInstance.getAmplifier(), 1);
                    } else {
                        int duration = effectInstance.mapDuration(d -> (int) (d * durationScale));
                        MobEffectInstance newEffect = new MobEffectInstance(
                                effect, duration, effectInstance.getAmplifier(), effectInstance.isAmbient(), effectInstance.isVisible()
                        );
                        if (!newEffect.endsWithin(20)) {
                            livingEntity.addEffect(newEffect, spellRunner.caster());
                        }
                    }
                }
            }
            return check;
        });
    }
}
