package net.smok.om.spell.actions.misc;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import net.smok.om.spell.SpellRunner;
import net.smok.om.spell.actions.SpellAction;
import net.smok.om.spell.actions.SpellActionType;
import net.smok.om.utils.StoredValueType;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.function.BiFunction;

public record RepositionTarget(String getPositionField, String setPositionField, Offset offset) implements SpellAction {
    private static final Offset IDENTITY = (_, vec3) -> vec3;

    public static final MapCodec<RepositionTarget> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codec.STRING.fieldOf("getPositionField").forGetter(RepositionTarget::getPositionField),
            Codec.STRING.optionalFieldOf("setPositionField", "position").forGetter(RepositionTarget::setPositionField),
            Codec.withAlternative(AddOffset.OFFSET_CODEC, RelativeOffset.OFFSET_CODEC).optionalFieldOf("offset").forGetter(repositionTarget -> {
                if (repositionTarget.offset instanceof AddOffset || repositionTarget.offset instanceof RelativeOffset) return Optional.of(repositionTarget.offset);
                return Optional.empty();
            })
    ).apply(instance, (s, s2, offset1) -> new RepositionTarget(s, s2, offset1.orElse(IDENTITY))));


    @Contract("_, _ -> new")
    public static @NotNull RepositionTarget identity(String getPositionField, String setPositionField) {
        return new RepositionTarget(getPositionField, setPositionField, IDENTITY);
    }

    @Contract("_, _, _ -> new")
    public static @NotNull RepositionTarget withOffset(String getPositionField, String setPositionField, Vec3 offset) {
        return new RepositionTarget(getPositionField, setPositionField, new AddOffset(offset));
    }

    @Contract("_, _, _, _, _, _ -> new")
    public static @NotNull RepositionTarget withLookRelative(String getPositionField, String setPositionField, String entityTarget, double left, double up, double forwards) {
        return new RepositionTarget(getPositionField, setPositionField, new RelativeOffset(entityTarget, new Vec3(left, up, forwards), true));
    }

    @Contract("_, _, _, _, _, _ -> new")
    public static @NotNull RepositionTarget withRayRelative(String getPositionField, String setPositionField, String entityTarget, double left, double up, double forwards) {
        return new RepositionTarget(getPositionField, setPositionField, new RelativeOffset(entityTarget, new Vec3(left, up, forwards), false));
    }

    @Override
    public SpellActionType<?> actionType() {
        return SpellActionType.REPOSITION_TARGET;
    }

    @Override
    public SpellRunner.ProcessStatus run(SpellRunner spellRunner) {
        return spellRunner.executeFor(getPositionField, StoredValueType.POSITION, (_, vec3) -> {
            spellRunner.put(setPositionField, StoredValueType.POSITION, offset.apply(spellRunner, vec3));
            return true;
        });
    }

    protected interface Offset extends BiFunction<SpellRunner, Vec3, Vec3> {

    }


    protected record AddOffset(Vec3 vector) implements Offset {

        public static final Codec<AddOffset> CODEC = Vec3.CODEC.xmap(AddOffset::new, AddOffset::vector);
        public static final Codec<Offset> OFFSET_CODEC = CODEC.xmap(addOffset -> addOffset, offset -> {
            if (offset instanceof AddOffset addOffset) return addOffset;
            throw new IllegalArgumentException();
        });

        @Override
        public Vec3 apply(SpellRunner spellRunner, Vec3 vec3) {
            return vec3.add(vector);
        }
    }

    protected record RelativeOffset(String target, Vec3 vector, boolean useEntityLook) implements Offset {

        public static final Codec<RelativeOffset> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.STRING.fieldOf("target").forGetter(RelativeOffset::target),
                Codec.DOUBLE.fieldOf("left").forGetter((RelativeOffset vec3) -> vec3.vector.x()),
                Codec.DOUBLE.fieldOf("up").forGetter((RelativeOffset vec3) -> vec3.vector.y()),
                Codec.DOUBLE.fieldOf("forwards").forGetter((RelativeOffset vec3) -> vec3.vector.z()),
                Codec.BOOL.optionalFieldOf("useEntityLook", false).forGetter(RelativeOffset::useEntityLook)
        ).apply(instance, (string, aDouble, aDouble2, aDouble3, bool) ->
                new RelativeOffset(string, new Vec3(aDouble, aDouble2, aDouble3), bool)));

        public static final Codec<Offset> OFFSET_CODEC = CODEC.xmap(relativeOffset -> relativeOffset, offset -> {
            if (offset instanceof RelativeOffset relativeOffset) return relativeOffset;
            throw new IllegalArgumentException();
        });

        @Override
        public Vec3 apply(SpellRunner spellRunner, Vec3 vec3) {
            Vec2 rotation = useEntityLook ?
                    spellRunner.get(target, StoredValueType.ENTITY).getEntity().getRotationVector() :
                    vec3.subtract(spellRunner.get(target, StoredValueType.POSITION)).rotation();
            return Vec3.applyLocalCoordinatesToRotation(rotation, vector);
        }
    }


}
