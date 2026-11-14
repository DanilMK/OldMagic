package net.smok.om.spell.actions.logic;

import com.mojang.serialization.MapCodec;
import net.smok.om.spell.actions.SpellAction;
import net.smok.om.spell.actions.SpellActionType;
import net.smok.om.spell.SpellRunner;

@Deprecated
public class EmptyAction implements SpellAction {

    public static final EmptyAction INSTANCE = new EmptyAction();
    public static final MapCodec<EmptyAction> MAP_CODEC = MapCodec.unit(INSTANCE);

    @Override
    public SpellActionType<?> actionType() {
        return SpellActionType.EMPTY;
    }

    @Override
    public SpellRunner.ProcessStatus run(SpellRunner spellRunner) {
        return SpellRunner.ProcessStatus.SUCCESS;
    }
}
