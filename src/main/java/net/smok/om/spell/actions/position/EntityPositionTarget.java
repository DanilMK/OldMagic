package net.smok.om.spell.actions.position;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.phys.Vec3;
import net.smok.om.spell.actions.SpellAction;
import net.smok.om.spell.actions.SpellActionType;
import net.smok.om.spell.SpellRunner;

public record EntityPositionTarget(Vec3 offset) implements SpellAction {
    public static final MapCodec<EntityPositionTarget> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Vec3.CODEC.optionalFieldOf("offset", Vec3.ZERO).forGetter(EntityPositionTarget::offset)
    ).apply(instance, EntityPositionTarget::new));

    @Override
    public SpellActionType<?> actionType() {
        return SpellActionType.ENTITY_POSITION_TARGET;
    }

    @Override
    public SpellRunner.ProcessStatus run(SpellRunner spellRunner) {
        return spellRunner.executeForAnyEntity((_, entity) -> {
            spellRunner.addPositionTarget(entity.position().add(offset));
            return true;
        });
    }
}
