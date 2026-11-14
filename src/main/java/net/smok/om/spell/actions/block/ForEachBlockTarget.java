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
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public record ForEachBlockTarget(Holder<SpellAction> action, int delay, int combineAmount, String blocksListTarget, String setBlockTarget) implements SpellAction {

    public static final MapCodec<ForEachBlockTarget> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            SpellAction.HOLDER_CODEC.fieldOf("action").forGetter(ForEachBlockTarget::action),
            Codec.INT.fieldOf("delay").forGetter(ForEachBlockTarget::delay),
            Codec.INT.fieldOf("combine_amount").forGetter(ForEachBlockTarget::combineAmount),
            Codec.STRING.optionalFieldOf("blocks_list_target", "blocks").forGetter(ForEachBlockTarget::blocksListTarget),
            Codec.STRING.optionalFieldOf("set_block_target", "block").forGetter(ForEachBlockTarget::setBlockTarget)
    ).apply(instance, ForEachBlockTarget::new));


    @Contract("_, _, _, _, _ -> new")
    public static @NotNull ForEachBlockTarget target(Holder<SpellAction> action, int delay, int combineAmount, String blocksListTarget, String setBlockTarget) {
        return new ForEachBlockTarget(action, delay, combineAmount, blocksListTarget, setBlockTarget);
    }

    @Contract("_, _, _ -> new")
    public static @NotNull ForEachBlockTarget target(Holder<SpellAction> action, int delay, int combineAmount) {
        return new ForEachBlockTarget(action, delay, combineAmount, "blocks", "block");
    }

    @Contract("_ -> new")
    public static @NotNull ForEachBlockTarget target(Holder<SpellAction> action) {
        return new ForEachBlockTarget(action, 0, 1, "blocks", "block");
    }

    @Contract("_, _, _ -> new")
    public static @NotNull ForEachBlockTarget target(Holder<SpellAction> action, String blocksListTarget, String setBlockTarget) {
        return new ForEachBlockTarget(action, 0, 1, blocksListTarget, setBlockTarget);
    }

    @Override
    public SpellActionType<?> actionType() {
        return SpellActionType.FOR_EACH_BLOCK_TARGET;
    }

    @Override
    public SpellRunner.ProcessStatus run(SpellRunner spellRunner) {
        int combined = 0;
        List<BlockPos> blockTargets = spellRunner.getOrCreateList(blocksListTarget, StoredValueType.BLOCK);
        for (BlockPos blockTarget : blockTargets) {

            if (delay > 0 && combined > combineAmount) {
                SpellRunner.ProcessStatus waitStatus = spellRunner.executeWait(delay);
                if (waitStatus == SpellRunner.ProcessStatus.RUNNING) return SpellRunner.ProcessStatus.RUNNING;
            }

            spellRunner.put(setBlockTarget, StoredValueType.BLOCK, blockTarget);
            SpellRunner.ProcessStatus run = action.value().run(spellRunner);
            if (run == SpellRunner.ProcessStatus.SUCCESS) combined++;

            if (run == SpellRunner.ProcessStatus.RUNNING) return SpellRunner.ProcessStatus.RUNNING;
        }
        return SpellRunner.ProcessStatus.SUCCESS;
    }
}
