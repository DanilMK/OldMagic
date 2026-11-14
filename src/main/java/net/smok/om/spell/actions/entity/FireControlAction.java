package net.smok.om.spell.actions.entity;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.entity.Entity;
import net.smok.om.spell.actions.SpellAction;
import net.smok.om.spell.actions.SpellActionType;
import net.smok.om.spell.SpellRunner;
import net.smok.om.utils.StoredValueType;

public record FireControlAction(int fireTicks, String target) implements SpellAction {

    public static final MapCodec<FireControlAction> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codec.INT.fieldOf("fire_ticks").forGetter(FireControlAction::fireTicks),
            Codec.STRING.optionalFieldOf("target", "entity").forGetter(FireControlAction::target)
    ).apply(instance, FireControlAction::new));

    @Override
    public SpellActionType<?> actionType() {
        return SpellActionType.FIRE_CONTROL;
    }

    @Override
    public SpellRunner.ProcessStatus run(SpellRunner spellRunner) {
        return spellRunner.executeFor(target, StoredValueType.ENTITY, (_, entityHolder) -> {
            Entity entity = entityHolder.getEntity();
            if (fireTicks < 1) entity.extinguishFire();
            else if (!entity.fireImmune()) entity.setRemainingFireTicks(entity.getRemainingFireTicks() + fireTicks);
            else return false;
            return true;
        });
    }
}
