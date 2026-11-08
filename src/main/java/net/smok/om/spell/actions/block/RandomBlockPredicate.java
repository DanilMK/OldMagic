package net.smok.om.spell.actions.block;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.levelgen.blockpredicates.BlockPredicate;
import net.minecraft.world.level.levelgen.blockpredicates.BlockPredicateType;
import net.smok.om.Values;

public record RandomBlockPredicate(float probability) implements BlockPredicate {
    public static final MapCodec<RandomBlockPredicate> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codec.floatRange(0, 1).fieldOf("probability").forGetter(RandomBlockPredicate::probability)
    ).apply(instance, RandomBlockPredicate::new));

    @Override
    public BlockPredicateType<?> type() {
        return Values.RANDOM_BLOCK_PREDICATE_TYPE;
    }

    @Override
    public boolean test(WorldGenLevel worldGenLevel, BlockPos blockPos) {
        return worldGenLevel.getRandom().nextFloat() < probability;
    }

}
