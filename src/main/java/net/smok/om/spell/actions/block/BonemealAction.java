package net.smok.om.spell.actions.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.smok.om.spell.actions.SpellAction;
import net.smok.om.spell.actions.SpellActionType;
import net.smok.om.spell.SpellRunner;
import net.smok.om.utils.StoredValueType;

public class BonemealAction implements SpellAction {

    public static final BonemealAction INSTANCE = new BonemealAction();
    public static final MapCodec<BonemealAction> MAP_CODEC = MapCodec.unit(INSTANCE);

    @Override
    public SpellActionType<?> actionType() {
        return SpellActionType.BONEMEAL_BLOCKS;
    }

    @Override
    public SpellRunner.ProcessStatus run(SpellRunner spellRunner) {
        return spellRunner.executeFor(StoredValueType.BLOCK, (level, blockPos) -> {
            BlockState blockState = level.getBlockState(blockPos);
            if (blockState.getBlock() instanceof BonemealableBlock block &&
                    block.isValidBonemealTarget(level, blockPos, blockState) &&
                    block.isBonemealSuccess(level, level.getRandom(), blockPos, blockState)) {

                block.performBonemeal(level, level.getRandom(), blockPos, blockState);
                return true;
            }
            return false;
        });
    }
}
