package net.smok.om.spell.actions.block;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.templatesystem.RuleTest;
import net.smok.om.spell.actions.SpellAction;
import net.smok.om.spell.actions.SpellActionType;
import net.smok.om.spell.SpellRunner;

import java.util.List;
import java.util.Map;

@Deprecated
public record BlockTransformation(List<TransformEntry> transformList, int delay, int combineAmount) implements SpellAction {

    public static final Codec<TransformEntry> TRANSFORM_ENTRY_CODEC = RecordCodecBuilder.create(instance -> instance.group(
            RuleTest.CODEC.fieldOf("ruleTest").forGetter(TransformEntry::ruleTest),
            BlockState.CODEC.fieldOf("block").forGetter(TransformEntry::blockState)
    ).apply(instance, TransformEntry::new));

    public static final MapCodec<BlockTransformation> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            TRANSFORM_ENTRY_CODEC.listOf().fieldOf("transformList").forGetter(BlockTransformation::transformList),
            Codec.INT.optionalFieldOf("delay", 0).forGetter(BlockTransformation::delay),
            Codec.intRange(1, 1000).optionalFieldOf("combineAmount", 1).forGetter(BlockTransformation::combineAmount)
    ).apply(instance, BlockTransformation::new));


    @Override
    public SpellActionType<?> actionType() {
        return SpellActionType.BLOCKS_TRANSFORMATION;
    }

    @Override
    public SpellRunner.ProcessStatus run(SpellRunner spellRunner) {
        int combined = 0;
        for (Pair<BlockPos, BlockState> target : spellRunner.getBlockTargetStates()) {
            for (TransformEntry transform : transformList) {
                if (transform.ruleTest.test(target.getSecond(), spellRunner.getRandom())) {

                    if (delay > 0 && combined > combineAmount) {
                        SpellRunner.ProcessStatus waitStatus = spellRunner.executeWait(delay);
                        if (waitStatus == SpellRunner.ProcessStatus.RUNNING) return SpellRunner.ProcessStatus.RUNNING;
                    }

                    SpellRunner.ProcessStatus status = spellRunner.execute((level) -> {
                        level.setBlock(target.getFirst(), transform.blockState, 2);
                        return true;
                    });
                    combined++;

                    if (status != SpellRunner.ProcessStatus.SUCCESS) return status;
                }
            }
        }

        return SpellRunner.ProcessStatus.SUCCESS;
    }

    public record TransformEntry(RuleTest ruleTest, BlockState blockState) {}

    public BlockTransformation(List<TransformEntry> transformList) {
        this(transformList, 0, 1);
    }

    public BlockTransformation(TransformEntry... transformList) {
        this(List.of(transformList));
    }

    public BlockTransformation(Map<RuleTest, BlockState> transformMap) {
        this(transformMap.entrySet().stream().map(entry -> new TransformEntry(entry.getKey(), entry.getValue())).toList());
    }

    public BlockTransformation(int delay, TransformEntry... transformList) {
        this(List.of(transformList), delay, 1);
    }

    public BlockTransformation(Map<RuleTest, BlockState> transformMap, int delay) {
        this(transformMap.entrySet().stream().map(entry -> new TransformEntry(entry.getKey(), entry.getValue())).toList(), delay, 1);
    }
}
