package net.smok.om.spell.actions.entity;

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

public record ForEachEntityTarget(int delay, int combineAmount, String entitiesListTarget, String setEntityTarget, Holder<SpellAction> action) implements SpellAction {

    public static final MapCodec<ForEachEntityTarget> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codec.INT.fieldOf("delay").forGetter(ForEachEntityTarget::delay),
            Codec.INT.fieldOf("combine_amount").forGetter(ForEachEntityTarget::combineAmount),
            Codec.STRING.optionalFieldOf("entities_list_target", "entities").forGetter(ForEachEntityTarget::entitiesListTarget),
            Codec.STRING.optionalFieldOf("set_entity_target", "entity").forGetter(ForEachEntityTarget::setEntityTarget),
            SpellAction.HOLDER_CODEC.fieldOf("action").forGetter(ForEachEntityTarget::action)
    ).apply(instance, ForEachEntityTarget::new));


    @Override
    public SpellActionType<?> actionType() {
        return SpellActionType.FOR_EACH_ENTITY_TARGET;
    }

    @Override
    public SpellRunner.ProcessStatus run(SpellRunner spellRunner) {
        int combined = 0;
        List<BlockPos> blockTargets = spellRunner.getOrCreateList(entitiesListTarget, StoredValueType.BLOCK);
        for (BlockPos blockTarget : blockTargets) {

            if (delay > 0 && combined > combineAmount) {
                SpellRunner.ProcessStatus waitStatus = spellRunner.executeWait(delay);
                if (waitStatus == SpellRunner.ProcessStatus.RUNNING) return SpellRunner.ProcessStatus.RUNNING;
            }

            spellRunner.put(setEntityTarget, StoredValueType.BLOCK, blockTarget);
            SpellRunner.ProcessStatus run = action.value().run(spellRunner);
            if (run == SpellRunner.ProcessStatus.SUCCESS) combined++;

            if (run == SpellRunner.ProcessStatus.RUNNING) return SpellRunner.ProcessStatus.RUNNING;
        }
        return SpellRunner.ProcessStatus.SUCCESS;
    }
}
