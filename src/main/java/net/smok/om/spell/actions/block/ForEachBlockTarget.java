package net.smok.om.spell.actions.block;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.smok.om.spell.SpellRunner;
import net.smok.om.spell.actions.SpellAction;
import net.smok.om.spell.actions.SpellActionType;
import net.smok.om.utils.StoredValueType;

import java.util.List;

public record ForEachBlockTarget(int delay, int combineAmount, Holder<SpellAction> action) implements SpellAction {

    public static final MapCodec<ForEachBlockTarget> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
        Codec.INT.fieldOf("delay").forGetter(ForEachBlockTarget::delay),
        Codec.INT.fieldOf("combineAmount").forGetter(ForEachBlockTarget::combineAmount),
        SpellAction.HOLDER_CODEC.fieldOf("action").forGetter(ForEachBlockTarget::action)
    ).apply(instance, ForEachBlockTarget::new));


    @Override
    public SpellActionType<?> actionType() {
        return SpellActionType.FOR_EACH_BLOCK_TARGET;
    }

    @Override
    public SpellRunner.ProcessStatus run(SpellRunner spellRunner) {
        int combined = 0;
        List<BlockPos> blockTargets = spellRunner.getBlockTargets();
        for (BlockPos blockTarget : blockTargets) {

            if (delay > 0 && combined > combineAmount) {
                SpellRunner.ProcessStatus waitStatus = spellRunner.executeWait(delay);
                if (waitStatus == SpellRunner.ProcessStatus.RUNNING) return SpellRunner.ProcessStatus.RUNNING;
            }

            spellRunner.put(StoredValueType.BLOCK, blockTarget);
            SpellRunner.ProcessStatus run = action.value().run(spellRunner);
            if (run == SpellRunner.ProcessStatus.SUCCESS) combined++;

            if (run == SpellRunner.ProcessStatus.RUNNING) return SpellRunner.ProcessStatus.RUNNING;
        }
        return SpellRunner.ProcessStatus.SUCCESS;
    }
}
