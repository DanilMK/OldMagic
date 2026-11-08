package net.smok.om.spell.actions.position;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.phys.Vec3;
import net.smok.om.spell.actions.SpellAction;
import net.smok.om.spell.actions.SpellActionType;
import net.smok.om.spell.SpellRunner;

public record PositionBlockTarget(Vec3 offset) implements SpellAction {
    public static final MapCodec<PositionBlockTarget> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Vec3.CODEC.optionalFieldOf("center_offset", Vec3.ZERO).forGetter(PositionBlockTarget::offset)
    ).apply(instance, PositionBlockTarget::new));

    @Override
    public SpellActionType<?> actionType() {
        return SpellActionType.POSITION_BLOCK_TARGET;
    }

    @Override
    public SpellRunner.ProcessStatus run(SpellRunner spellRunner) {
        return spellRunner.executeForAnyBlock((_, blockPos, _) -> {
            spellRunner.addPositionTarget(blockPos.getCenter());
            return true;
        });
    }
}
