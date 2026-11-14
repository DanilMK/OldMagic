package net.smok.om.spell.actions.block;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.blockpredicates.BlockPredicate;
import net.smok.om.spell.actions.SpellAction;
import net.smok.om.spell.actions.SpellActionType;
import net.smok.om.spell.SpellRunner;
import net.smok.om.utils.StoredValueType;

public record BonemealAction(String blockTarget, BlockPredicate filter) implements SpellAction {

    public static final BonemealAction INSTANCE = new BonemealAction("block", BlockPredicate.alwaysTrue());

    public static final MapCodec<BonemealAction> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codec.STRING.fieldOf("blockTarget").forGetter(BonemealAction::blockTarget),
            BlockPredicate.CODEC.fieldOf("filter").forGetter(BonemealAction::filter)
    ).apply(instance, BonemealAction::new));


    @Override
    public SpellActionType<?> actionType() {
        return SpellActionType.BONEMEAL_BLOCKS;
    }

    @Override
    public SpellRunner.ProcessStatus run(SpellRunner spellRunner) {
        return spellRunner.executeFor(blockTarget, StoredValueType.BLOCK, (level, blockPos) -> {
            BlockState blockState = level.getBlockState(blockPos);
            if (filter.test(level, blockPos) && blockState.getBlock() instanceof BonemealableBlock block &&
                    block.isValidBonemealTarget(level, blockPos, blockState) &&
                    block.isBonemealSuccess(level, level.getRandom(), blockPos, blockState)) {

                block.performBonemeal(level, level.getRandom(), blockPos, blockState);
                return true;
            }
            return false;
        });
    }
}
