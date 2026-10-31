package net.smok.om.spell.entity.action;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.LivingEntity;
import net.smok.om.spell.SpellAction;
import net.smok.om.spell.SpellActionType;
import net.smok.om.spell.SpellRunner;

public record DamageEntityAction(ResourceKey<DamageType> damageType, float damageAmount) implements SpellAction {

    public static final MapCodec<DamageEntityAction> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            ResourceKey.codec(Registries.DAMAGE_TYPE).fieldOf("damage_type").forGetter(DamageEntityAction::damageType),
            Codec.FLOAT.fieldOf("amount").forGetter(DamageEntityAction::damageAmount)
    ).apply(instance, DamageEntityAction::new));


    @Override
    public SpellActionType<?> getType() {
        return SpellActionType.DAMAGE_ENTITIES;
    }

    @Override
    public SpellRunner.ProcessStatus run(SpellRunner spellRunner) {
        return spellRunner.executeForAnyEntity((level, entity) -> {
            if (entity instanceof LivingEntity livingEntity)
                return livingEntity.hurtServer(level, level.damageSources().source(damageType, spellRunner.getActor(), spellRunner.caster()), damageAmount);
            return false;
        });
    }
}
