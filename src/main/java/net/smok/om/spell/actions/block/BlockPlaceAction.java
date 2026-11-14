package net.smok.om.spell.actions.block;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.blockpredicates.BlockPredicate;
import net.smok.om.spell.SpellRunner;
import net.smok.om.spell.actions.SpellAction;
import net.smok.om.spell.actions.SpellActionType;
import net.smok.om.utils.StoredValueType;

public record BlockPlaceAction(String blockTarget, BlockState blockState, BlockPredicate filter) implements SpellAction {
    public static final MapCodec<BlockPlaceAction> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codec.STRING.optionalFieldOf("blockTarget", "block").forGetter(BlockPlaceAction::blockTarget),
            BlockState.CODEC.fieldOf("block_state").forGetter(BlockPlaceAction::blockState),
            BlockPredicate.CODEC.fieldOf("filter").forGetter(BlockPlaceAction::filter)
    ).apply(instance, BlockPlaceAction::new));

    @Override
    public SpellActionType<?> actionType() {
        return SpellActionType.BLOCK_PLACE;
    }

    @Override
    public SpellRunner.ProcessStatus run(SpellRunner spellRunner) {
        return spellRunner.executeFor(blockTarget, StoredValueType.BLOCK, (level, blockPos) -> {
            if (filter.test(level, blockPos)) {
                return level.setBlock(blockPos, blockState, 2);
            }
            return false;
        });
    }
}
