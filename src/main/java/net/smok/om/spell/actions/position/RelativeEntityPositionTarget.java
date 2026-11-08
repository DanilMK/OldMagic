package net.smok.om.spell.actions.position;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import net.smok.om.spell.actions.SpellAction;
import net.smok.om.spell.actions.SpellActionType;
import net.smok.om.spell.SpellRunner;

public record RelativeEntityPositionTarget(RelativeType relativeType, RelativeValue relativeValue) implements SpellAction {
    public static final MapCodec<RelativeEntityPositionTarget> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            StringRepresentable.fromEnum(RelativeType::values).fieldOf("relative_type").forGetter(RelativeEntityPositionTarget::relativeType),
            RelativeValue.CODEC.fieldOf("relative_value").forGetter(RelativeEntityPositionTarget::relativeValue)
    ).apply(instance, RelativeEntityPositionTarget::new));

    @Override
    public SpellActionType<?> actionType() {
        return SpellActionType.RELATIVE_ENTITY_POSITION_TARGET;
    }

    @Override
    public SpellRunner.ProcessStatus run(SpellRunner spellRunner) {
        return spellRunner.executeForAnyEntity((_, entity) -> {
            switch (relativeType) {
                case PROJECTILE_POSITION -> {
                    Vec3 dist = entity.position().subtract(spellRunner.getActor().position());
                    Vec3 position = relativeValue.applyPosition(dist.rotation());
                    spellRunner.addPositionTarget(position);
                }
                case CASTER_LOOK -> {
                    Vec3 position = relativeValue.applyPosition(spellRunner.caster().getRotationVector());
                    spellRunner.addPositionTarget(position);
                }
                case CASTER_POSITON -> {
                    Vec3 dist = entity.position().subtract(spellRunner.caster().position());
                    Vec3 position = relativeValue.applyPosition(dist.rotation());
                    spellRunner.addPositionTarget(position);
                }
            }
            return true;
        });
    }

    public enum RelativeType implements StringRepresentable {
        PROJECTILE_POSITION("projectile_position"),
        CASTER_LOOK("caster_look"),
        CASTER_POSITON("caster_position");

        private final String name;

        RelativeType(String name) {
            this.name = name;
        }

        @Override
        public String getSerializedName() {
            return name;
        }
    }


    public record RelativeValue(Vec3 vector) {

        public static final Codec<RelativeValue> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.DOUBLE.fieldOf("left").forGetter((RelativeValue vec3) -> vec3.vector.x()),
                Codec.DOUBLE.fieldOf("up").forGetter((RelativeValue vec3) -> vec3.vector.y()),
                Codec.DOUBLE.fieldOf("forwards").forGetter((RelativeValue vec3) -> vec3.vector.z())
        ).apply(instance, (aDouble, aDouble2, aDouble3) -> new RelativeValue(new Vec3(aDouble, aDouble2, aDouble3))));

        public Vec3 applyPosition(Vec2 rotation) {
            return Vec3.applyLocalCoordinatesToRotation(rotation, vector);
        }
    }



}
