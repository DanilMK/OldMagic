package net.smok.om.spell.block.action;

import com.mojang.serialization.MapCodec;
import net.minecraft.world.level.block.BonemealableBlock;
import net.smok.om.spell.SpellAction;
import net.smok.om.spell.SpellActionType;
import net.smok.om.spell.SpellRunner;

public class BonemealAction implements SpellAction {

    public static final BonemealAction INSTANCE = new BonemealAction();
    public static final MapCodec<BonemealAction> MAP_CODEC = MapCodec.unit(INSTANCE);

    @Override
    public SpellActionType<?> getType() {
        return SpellActionType.BONEMEAL_BLOCKS;
    }

    @Override
    public SpellRunner.ProcessStatus run(SpellRunner spellRunner) {
        return spellRunner.executeForAnyBlock((level, blockPos, blockState) -> {

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
