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
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public record BlockPlaceAction(String blockTarget, BlockState blockState, BlockPredicate filter) implements SpellAction {
    public static final MapCodec<BlockPlaceAction> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codec.STRING.optionalFieldOf("blockTarget", "block").forGetter(BlockPlaceAction::blockTarget),
            BlockState.CODEC.fieldOf("block_state").forGetter(BlockPlaceAction::blockState),
            BlockPredicate.CODEC.fieldOf("filter").forGetter(BlockPlaceAction::filter)
    ).apply(instance, BlockPlaceAction::new));

    @Contract("_, _, _ -> new")
    public static @NotNull BlockPlaceAction place(String blockTarget, BlockState blockState, BlockPredicate filter) {
        return new BlockPlaceAction(blockTarget, blockState, filter);
    }

    @Contract("_, _ -> new")
    public static @NotNull BlockPlaceAction place(BlockState blockState, BlockPredicate filter) {
        return new BlockPlaceAction("block", blockState, filter);
    }

    @Contract("_ -> new")
    public static @NotNull BlockPlaceAction placeOnAir(BlockState blockState) {
        return new BlockPlaceAction("block", blockState, BlockPredicate.ONLY_IN_AIR_PREDICATE);
    }

    @Contract("_ -> new")
    public static @NotNull BlockPlaceAction placeOnReplaceable(BlockState blockState) {
        return new BlockPlaceAction("block", blockState, BlockPredicate.replaceable());
    }

    @Contract("_, _ -> new")
    public static @NotNull BlockPlaceAction placeOnAir(String blockTarget, BlockState blockState) {
        return new BlockPlaceAction(blockTarget, blockState, BlockPredicate.ONLY_IN_AIR_PREDICATE);
    }

    @Contract("_, _ -> new")
    public static @NotNull BlockPlaceAction placeOnReplaceable(String blockTarget, BlockState blockState) {
        return new BlockPlaceAction(blockTarget, blockState, BlockPredicate.replaceable());
    }

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
