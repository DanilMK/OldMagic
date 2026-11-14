package net.smok.om.spell.actions.entity;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.phys.Vec3;
import net.smok.om.spell.actions.SpellAction;
import net.smok.om.spell.actions.SpellActionType;
import net.smok.om.spell.SpellRunner;
import net.smok.om.utils.StoredValueType;

public record KnockbackEntity(float power, boolean ignoreKnockbackResistance, String target) implements SpellAction {

    public static final MapCodec<KnockbackEntity> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codec.FLOAT.fieldOf("power").forGetter(KnockbackEntity::power),
            Codec.BOOL.optionalFieldOf("ignore_knockback_resistance", false).forGetter(KnockbackEntity::ignoreKnockbackResistance),
            Codec.STRING.optionalFieldOf("target", "entity").forGetter(KnockbackEntity::target)
    ).apply(instance, KnockbackEntity::new));

    @Override
    public SpellActionType<?> actionType() {
        return SpellActionType.KNOCKBACK_ENTITY;
    }

    @Override
    public SpellRunner.ProcessStatus run(SpellRunner spellRunner) {
        return spellRunner.executeFor(target, StoredValueType.ENTITY, (_, entityHolder) -> {
            Entity entity = entityHolder.getEntity();
            double knockbackResistance = !ignoreKnockbackResistance && entity instanceof LivingEntity mob ? Math.max(0.0, 1.0 - mob.getAttributeValue(Attributes.KNOCKBACK_RESISTANCE)) : 1;

            Vec3 movement = spellRunner.getActor().getDeltaMovement().multiply(1.0, 0.0, 1.0).normalize()
                    .scale(power * 0.6 * knockbackResistance);
            movement = new Vec3(movement.x, 0.1, movement.z);

            if (movement.lengthSqr() > 0) {
                entity.push(movement);
                return true;
            }
            return false;
        });
    }
}
