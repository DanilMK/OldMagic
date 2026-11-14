package net.smok.om.spell.actions.block;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.levelgen.blockpredicates.BlockPredicate;
import net.smok.om.spell.actions.SpellAction;
import net.smok.om.spell.actions.SpellActionType;
import net.smok.om.spell.SpellRunner;
import net.smok.om.utils.Shape;
import net.smok.om.utils.StoredValueType;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.UnaryOperator;

public record ShapeBlockTarget(Shape shape, int range, BlockPredicate filter, boolean continuesPick, boolean shuffle, String setBlocksListTarget, String centerBlockTarget) implements SpellAction {

    public static final MapCodec<ShapeBlockTarget> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Shape.SHAPE_CODEC.fieldOf("shape").forGetter(ShapeBlockTarget::shape),
            Codec.INT.fieldOf("range").forGetter(ShapeBlockTarget::range),
            BlockPredicate.CODEC.optionalFieldOf("available_blocks", BlockPredicate.alwaysTrue()).forGetter(ShapeBlockTarget::filter),
            Codec.BOOL.optionalFieldOf("continues_pick", false).forGetter(ShapeBlockTarget::continuesPick),
            Codec.BOOL.optionalFieldOf("shuffle", false).forGetter(ShapeBlockTarget::shuffle),
            Codec.STRING.optionalFieldOf("blocks_list_target", "blocks").forGetter(ShapeBlockTarget::setBlocksListTarget),
            Codec.STRING.optionalFieldOf("center_block_target", "actor").forGetter(ShapeBlockTarget::centerBlockTarget)
    ).apply(instance, ShapeBlockTarget::new));

    public static ShapeBlockTarget sphere(@NotNull UnaryOperator<Builder> builder) {
        return builder.apply(new Builder(Shape.SPHERE)).build();
    }

    public static ShapeBlockTarget cube(@NotNull UnaryOperator<Builder> builder) {
        return builder.apply(new Builder(Shape.CUBE)).build();
    }

    public static ShapeBlockTarget diamond(@NotNull UnaryOperator<Builder> builder) {
        return builder.apply(new Builder(Shape.DIAMOND)).build();
    }

    @Override
    public SpellActionType<?> actionType() {
        return SpellActionType.TARGET_BY_SHAPE_BLOCKS;
    }

    @Override
    public SpellRunner.ProcessStatus run(SpellRunner spellRunner) {
        return spellRunner.executeFor(centerBlockTarget, StoredValueType.BLOCK, (level, center) -> {
            ArrayList<BlockPos> list = continuesPick ?
                    collectContinuesBlocks(level, center) :
                    collectSimpleBlocks(level, center);
            if (shuffle) {
                RandomSource random = spellRunner.getRandom();
                for (int i = 0; i < list.size(); i++) {
                    int j = random.nextInt(list.size());
                    BlockPos a = list.get(i);
                    BlockPos b = list.get(j);

                    list.set(i, b);
                    list.set(j, a);
                }
            }
            spellRunner.addBlockTargets(list);
            return !spellRunner.getBlockTargets().isEmpty();
        });
    }

    public @NotNull ArrayList<BlockPos> collectContinuesBlocks(ServerLevel level, BlockPos center) {
        ArrayList<BlockPos> result = new ArrayList<>();
        Queue<BlockPos> queue = new ArrayDeque<>();
        queue.add(center);
        for (int i = 0; i < 1000000 && !queue.isEmpty(); i++) {
            BlockPos next = queue.poll();
            if (result.contains(next) || !shape.isInRange(center, next, range)) continue;
            if (!filter.test(level, next)) continue;

            result.add(next);
            for (Direction dir : Direction.values()) {
                queue.add(next.relative(dir));
            }
        }
        return result;
    }

    public @NotNull ArrayList<BlockPos> collectSimpleBlocks(ServerLevel level, BlockPos center) {
        return new ArrayList<>(BlockPos.betweenClosedStream(center.offset(-range, -range, -range), center.offset(range, range, range))
                .filter(blockPos -> shape.isInRange(center, blockPos, range) && filter.test(level, blockPos)).map(BlockPos::immutable).toList());
    }


    public static class Builder {
        public static final BlockPredicate SURFACE_FILTER = BlockPredicate.allOf(BlockPredicate.solid(), BlockPredicate.anyOf(
                BlockPredicate.not(BlockPredicate.solid(new Vec3i(-1, 0, 0))),
                BlockPredicate.not(BlockPredicate.solid(new Vec3i(1, 0, 0))),
                BlockPredicate.not(BlockPredicate.solid(new Vec3i(0, 0, -1))),
                BlockPredicate.not(BlockPredicate.solid(new Vec3i(0, 0, 1))),
                BlockPredicate.not(BlockPredicate.solid(new Vec3i(0, -1, 0))),
                BlockPredicate.not(BlockPredicate.solid(new Vec3i(0, 1, 0)))
        ));

        private Shape shape;
        private int range = 1;
        private BlockPredicate filter = BlockPredicate.alwaysTrue();
        private boolean continuesPick = false;
        private boolean shuffle = false;
        private String setBlocksListTarget = "blocks";
        private String centerBlockTarget = "actor";

        public Builder(Shape shape) {
            this.shape = shape;
        }

        public Builder shape(Shape shape) {
            this.shape = shape;
            return this;
        }

        public Builder range(int range) {
            this.range = range;
            return this;
        }

        public Builder filter(@NotNull BlockPredicate filter) {
            this.filter = filter;
            return this;
        }

        public Builder continuesPick(boolean continuesPick) {
            this.continuesPick = continuesPick;
            return this;
        }

        public Builder shuffle(boolean shuffle) {
            this.shuffle = shuffle;
            return this;
        }

        public Builder setBlocksListTarget(@NotNull String setBlocksListTarget) {
            this.setBlocksListTarget = setBlocksListTarget;
            return this;
        }

        public Builder filterSurface() {
            this.filter = filter == null ? SURFACE_FILTER : BlockPredicate.allOf(filter, SURFACE_FILTER);
            return this;
        }

        public Builder centerBlockTarget(String centerBlockTarget) {
            this.centerBlockTarget = centerBlockTarget;
            return this;
        }

        public ShapeBlockTarget build() {
            return new ShapeBlockTarget(shape, range, filter, continuesPick, shuffle, setBlocksListTarget, centerBlockTarget);
        }
    }
}
