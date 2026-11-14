package net.smok.om.spell.actions.logic;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.smok.om.spell.actions.SpellAction;
import net.smok.om.spell.actions.SpellActionType;
import net.smok.om.spell.SpellRunner;

public record WaitAction(int waitTicks) implements SpellAction {
    public static final MapCodec<WaitAction> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codec.INT.fieldOf("wait_ticks").forGetter(WaitAction::waitTicks)
    ).apply(instance, WaitAction::new));


    @Override
    public SpellActionType<?> actionType() {
        return SpellActionType.WAIT;
    }

    @Override
    public SpellRunner.ProcessStatus run(SpellRunner spellRunner) {
        return spellRunner.executeWait(waitTicks) ? SpellRunner.ProcessStatus.RUNNING : SpellRunner.ProcessStatus.SUCCESS;
    }
}
