package net.smok.om.spell.entity.target;

import com.mojang.serialization.MapCodec;
import net.smok.om.spell.SpellAction;
import net.smok.om.spell.SpellActionType;
import net.smok.om.spell.SpellRunner;

public class EntityHitTarget implements SpellAction {

    public static final EntityHitTarget INSTANCE = new EntityHitTarget();

    public static final MapCodec<EntityHitTarget> MAP_CODEC = MapCodec.unit(INSTANCE);


    @Override
    public SpellActionType<?> getType() {
        return SpellActionType.TARGET_BY_HIT_ENTITY;
    }

    @Override
    public SpellRunner.ProcessStatus run(SpellRunner spellRunner) {
        return SpellRunner.ProcessStatus.FAILURE;
    }
}
