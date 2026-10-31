package net.smok.om.spell.logic;

import com.mojang.serialization.MapCodec;
import net.smok.om.spell.SpellAction;
import net.smok.om.spell.SpellActionType;
import net.smok.om.spell.SpellRunner;

public class EmptyAction implements SpellAction {

    public static final EmptyAction INSTANCE = new EmptyAction();
    public static final MapCodec<EmptyAction> MAP_CODEC = MapCodec.unit(INSTANCE);

    @Override
    public SpellActionType<?> getType() {
        return SpellActionType.EMPTY;
    }

    @Override
    public SpellRunner.ProcessStatus run(SpellRunner spellRunner) {
        return SpellRunner.ProcessStatus.SUCCESS;
    }
}
