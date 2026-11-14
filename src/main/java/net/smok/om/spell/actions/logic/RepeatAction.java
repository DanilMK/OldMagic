package net.smok.om.spell.actions.logic;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.smok.om.spell.actions.SpellAction;
import net.smok.om.spell.actions.SpellActionType;
import net.smok.om.spell.SpellRunner;

public record RepeatAction(Holder<SpellAction> action, int delay, int count, SpellRunner.StatusHandle handleStatus) implements SpellAction {

    public static final MapCodec<RepeatAction> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            SpellAction.HOLDER_CODEC.fieldOf("action").forGetter(RepeatAction::action),
            Codec.INT.fieldOf("delay").forGetter(RepeatAction::delay),
            Codec.INT.fieldOf("count").forGetter(RepeatAction::count),
            SpellRunner.StatusHandle.CODEC.fieldOf("handle_status").forGetter(RepeatAction::handleStatus)
    ).apply(instance, RepeatAction::new));

    @Override
    public SpellActionType<?> actionType() {
        return SpellActionType.REPEAT_ACTION;
    }

    @Override
    public SpellRunner.ProcessStatus run(SpellRunner spellRunner) {
        for (int i = 0; i < count; i++) {
            if (delay > 0) {
                SpellRunner.ProcessStatus waitStatus = spellRunner.executeWait(delay);
                if (waitStatus == SpellRunner.ProcessStatus.RUNNING) return SpellRunner.ProcessStatus.RUNNING;
            }

            SpellRunner.ProcessStatus run = action.value().run(spellRunner);
            if (run == SpellRunner.ProcessStatus.RUNNING) return SpellRunner.ProcessStatus.RUNNING;
            SpellRunner.ProcessStatus result = handleStatus.handle(run);
            if (result != SpellRunner.ProcessStatus.RUNNING) return result;
        }
        return SpellRunner.ProcessStatus.SUCCESS;
    }
}
