package net.smok.om.spell.actions.entity;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.phys.Vec3;
import net.smok.om.spell.actions.SpellAction;
import net.smok.om.spell.actions.SpellActionType;
import net.smok.om.spell.SpellRunner;
import org.jetbrains.annotations.NotNull;

public record MoveEntityAction(Direction direction, float power, boolean ignoreKnockbackResist) implements SpellAction {

    public static final Codec<Direction> DIRECTION_CODEC = StringRepresentable.fromEnum(Direction::values);

    public static final MapCodec<MoveEntityAction> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            DIRECTION_CODEC.fieldOf("direction").forGetter(MoveEntityAction::direction),
            Codec.FLOAT.fieldOf("power").forGetter(MoveEntityAction::power),
            Codec.BOOL.optionalFieldOf("ignoreKnockbackResist", true).forGetter(MoveEntityAction::ignoreKnockbackResist)
    ).apply(instance, MoveEntityAction::new));

    private Vec3 getMovement(Entity mob, Entity caster, Entity actor, double knockbackResistance) {
        switch (direction) {
            case PULL_TO_CASTER -> {
                return getMovement(caster.position(), mob, knockbackResistance);
            }
            case PULL_TO_PROJECTILE -> {
                return getMovement(actor.position(), mob, knockbackResistance);
            }
            case PUSH_FROM_CASTER -> {
                return getMovement(mob.position(), caster, knockbackResistance);
            }
            case PUSH_FROM_PROJECTILE -> {
                return getMovement(mob.position(), actor, knockbackResistance);
            }
            case KNOCKBACK -> {
                Vec3 movement = actor.getDeltaMovement().multiply(1.0, 0.0, 1.0).normalize()
                        .scale(power * 0.6 * knockbackResistance);
                return new Vec3(movement.x, 0.1, movement.z);
            }
        }
        return Vec3.ZERO;
    }

    private @NotNull Vec3 getMovement(Vec3 A, Entity B, double knockbackResistance) {
        Vec3 direction = A.subtract(B.position()).normalize();
        return direction.scale(power * knockbackResistance);
    }

    @Override
    public SpellActionType<?> actionType() {
        return SpellActionType.MOVE_ENTITY;
    }

    @Override
    public SpellRunner.ProcessStatus run(SpellRunner spellRunner) {
        if (!(power > 0)) return SpellRunner.ProcessStatus.FAILURE;

        return spellRunner.executeForAnyEntity((level, entity) -> {

            double knockbackResistance = !ignoreKnockbackResist && entity instanceof LivingEntity mob ? Math.max(0.0, 1.0 - mob.getAttributeValue(Attributes.KNOCKBACK_RESISTANCE)) : 1;

            Vec3 movement = getMovement(entity, spellRunner.caster(), spellRunner.getActor(), knockbackResistance);
            if (movement.lengthSqr() > 0) {
                entity.push(movement);
                return true;
            }
            return false;
        });
    }

    public enum Direction implements StringRepresentable {
        PULL_TO_CASTER("pull_to_caster"),
        PULL_TO_PROJECTILE("pull_to_projectile"),
        PUSH_FROM_CASTER("push_from_caster"),
        PUSH_FROM_PROJECTILE("push_from_projectile"),
        KNOCKBACK("knockback");

        public final String name;

        Direction(String name) {
            this.name = name;
        }

        @Override
        public String getSerializedName() {
            return name;
        }
    }
}
