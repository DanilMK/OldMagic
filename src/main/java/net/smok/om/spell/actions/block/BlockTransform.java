package net.smok.om.spell.actions.block;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.levelgen.blockpredicates.BlockPredicate;
import net.smok.om.spell.SpellRunner;
import net.smok.om.spell.actions.SpellAction;
import net.smok.om.spell.actions.SpellActionType;
import net.smok.om.utils.StoredValueType;

public record BlockTransform(Block block, boolean keepProperties, BlockPredicate filter) implements SpellAction {

    public static final MapCodec<BlockTransform> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            BuiltInRegistries.BLOCK.byNameCodec().fieldOf("block").forGetter(BlockTransform::block),
            Codec.BOOL.optionalFieldOf("keepProperties", true).forGetter(BlockTransform::keepProperties),
            BlockPredicate.CODEC.optionalFieldOf("filter", BlockPredicate.alwaysTrue()).forGetter(BlockTransform::filter)
    ).apply(instance, BlockTransform::new));


    @Override
    public SpellActionType<?> actionType() {
        return SpellActionType.BLOCKS_TRANSFORM;
    }

    @Override
    public SpellRunner.ProcessStatus run(SpellRunner spellRunner) {
        return spellRunner.executeFor(StoredValueType.BLOCK, (level, blockPos) -> {
            if (filter.test(level, blockPos)) {
                if (keepProperties) {
                    BlockState existing = level.getBlockState(blockPos);
                    BlockState blockState = block.defaultBlockState();
                    for (Property<?> property : block.getStateDefinition().getProperties())
                        replaceProperty(property, existing, blockState);

                    level.setBlock(blockPos, blockState, 2);
                } else {
                    level.setBlock(blockPos, block.defaultBlockState(), 2);
                }
                return true;
            }
            return false;
        });
    }

    private static <T extends Comparable<T>> void replaceProperty(Property<T> property, BlockState from, BlockState to) {
        if (from.hasProperty(property)) to.setValue(property, from.getValue(property));
    }
}
