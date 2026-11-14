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
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public record MoveEntityToPosition(float power, boolean ignoreKnockbackResistance, String entityTarget, String positionTarget) implements SpellAction {

    public static final MapCodec<MoveEntityToPosition> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codec.FLOAT.fieldOf("power").forGetter(MoveEntityToPosition::power),
            Codec.BOOL.optionalFieldOf("ignore_knockback_resistance", false).forGetter(MoveEntityToPosition::ignoreKnockbackResistance),
            Codec.STRING.optionalFieldOf("entityTarget", "entity").forGetter(MoveEntityToPosition::entityTarget),
            Codec.STRING.optionalFieldOf("positionTarget", "position").forGetter(MoveEntityToPosition::positionTarget)
    ).apply(instance, MoveEntityToPosition::new));




    @Contract("_, _ -> new")
    public static @NotNull MoveEntityToPosition move(float power, boolean ignoreKnockbackResistance) {
        return new MoveEntityToPosition(power, ignoreKnockbackResistance, "entity", "position");
    }

    @Contract("_, _, _, _ -> new")
    public static @NotNull MoveEntityToPosition move(float power, boolean ignoreKnockbackResistance, String entityTarget, String positionTarget) {
        return new MoveEntityToPosition(power, ignoreKnockbackResistance, entityTarget, positionTarget);
    }

    @Override
    public SpellActionType<?> actionType() {
        return SpellActionType.MOVE_ENTITY_TO_POSITION;
    }

    @Override
    public SpellRunner.ProcessStatus run(SpellRunner spellRunner) {
        return spellRunner.executeFor(entityTarget, StoredValueType.ENTITY, (_, entityHolder) -> {
            Entity entity = entityHolder.getEntity();
            Vec3 position = spellRunner.get(positionTarget, StoredValueType.POSITION);
            if (position == null) return false;
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
