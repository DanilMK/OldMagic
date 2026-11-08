package net.smok.om.spell.actions.entity;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.phys.Vec3;
import net.smok.om.spell.actions.SpellAction;
import net.smok.om.spell.actions.SpellActionType;
import net.smok.om.spell.SpellRunner;

import java.util.List;

public record MoveEntityToPosition(float power, boolean ignoreKnockbackResistance) implements SpellAction {

    public static final MapCodec<MoveEntityToPosition> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codec.FLOAT.fieldOf("power").forGetter(MoveEntityToPosition::power),
            Codec.BOOL.optionalFieldOf("ignore_knockback_resistance", false).forGetter(MoveEntityToPosition::ignoreKnockbackResistance)
    ).apply(instance, MoveEntityToPosition::new));


    @Override
    public SpellActionType<?> actionType() {
        return SpellActionType.MOVE_ENTITY_TO_POSITION;
    }

    @Override
    public SpellRunner.ProcessStatus run(SpellRunner spellRunner) {
        return spellRunner.executeForAnyEntity((_, entity) -> {
            List<Vec3> positions = spellRunner.getPositionTargets();
            if (positions.isEmpty()) return false;
            Vec3 position = positions.getFirst();
            double knockbackResistance = !ignoreKnockbackResistance && entity instanceof LivingEntity mob ? Math.max(0.0, 1.0 - mob.getAttributeValue(Attributes.KNOCKBACK_RESISTANCE)) : 1;

            Vec3 movement = position.subtract(entity.position()).scale(power * knockbackResistance);
            if (movement.lengthSqr() > 0) {
                entity.push(movement);
                return true;
            }
            return false;
        });
    }
}
