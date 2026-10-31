package net.smok.om.spell.block.target;

import com.mojang.serialization.MapCodec;
import net.smok.om.spell.SpellAction;
import net.smok.om.spell.SpellActionType;
import net.smok.om.spell.SpellRunner;

public class HitBlockTarget implements SpellAction {

    public static final HitBlockTarget INSTANCE = new HitBlockTarget();

    public static final MapCodec<HitBlockTarget> MAP_CODEC = MapCodec.unit(INSTANCE);

    @Override
    public SpellActionType<?> getType() {
        return SpellActionType.TARGET_BY_BLOCK_HIT;
    }

    @Override
    public SpellRunner.ProcessStatus run(SpellRunner spellRunner) {
        return SpellRunner.ProcessStatus.FAILURE;
    }
}
