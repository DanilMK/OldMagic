package net.smok.om.spell.actions.block;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.blockpredicates.BlockPredicate;
import net.smok.om.spell.actions.SpellAction;
import net.smok.om.spell.actions.SpellActionType;
import net.smok.om.spell.SpellRunner;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public record ShapeBlockTarget(Shape shape, int range, boolean maskSurface, boolean maskSolid, BlockPredicate filter) implements SpellAction {

    public static final Codec<Shape> SHAPE_CODEC = StringRepresentable.fromEnum(Shape::values);

    public static final MapCodec<ShapeBlockTarget> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            SHAPE_CODEC.fieldOf("shape").forGetter(ShapeBlockTarget::shape),
            Codec.INT.fieldOf("range").forGetter(ShapeBlockTarget::range),
            Codec.BOOL.fieldOf("maskSurface").forGetter(ShapeBlockTarget::maskSurface),
            Codec.BOOL.fieldOf("maskSolid").forGetter(ShapeBlockTarget::maskSolid),
            BlockPredicate.CODEC.optionalFieldOf("available_blocks", BlockPredicate.alwaysTrue()).forGetter(ShapeBlockTarget::filter)
    ).apply(instance, ShapeBlockTarget::new));

    public ShapeBlockTarget(Shape shape, int range, boolean maskSurface) {
        this(shape, range, maskSurface, false, BlockPredicate.alwaysTrue());
    }

    public ShapeBlockTarget(Shape shape, int range, boolean maskSurface, boolean maskSolid) {
        this(shape, range, maskSurface, maskSolid, BlockPredicate.alwaysTrue());
    }

    @Override
    public SpellActionType<?> actionType() {
        return SpellActionType.TARGET_BY_SHAPE_BLOCKS;
    }

    @Override
    public SpellRunner.ProcessStatus run(SpellRunner spellRunner) {
        return spellRunner.execute(level -> {
            spellRunner.addBlockTargets(collectBlocks(level, spellRunner.getActor().blockPosition()));
            return !spellRunner.getBlockTargets().isEmpty();
        });
    }

    public @NotNull Set<BlockPos> collectBlocks(ServerLevel level, BlockPos center) {
        Set<BlockPos> result = new HashSet<>();
        Queue<BlockPos> queue = new ArrayDeque<>();
        queue.add(center);
        for (int i = 0; i < 1000000 && !queue.isEmpty(); i++) {
            BlockPos next = queue.poll();
            if (result.contains(next) || !isInRange(center, next)) continue;
            boolean solid = isSolid(level, next);
            if (solid && maskSurface && !isSurface(level, next)) continue;
            if (!solid && maskSolid) continue;

            if (!filter.test(level, next)) continue;

            result.add(next);
            for (Direction dir : Direction.values()) {
                queue.add(next.relative(dir));
            }
        }
        return result;
    }

    private boolean isSolid(Level level, BlockPos blockPos) {
        return level.getBlockState(blockPos).isSolid();
    }

    private boolean isSurface(Level level, BlockPos blockPos) {
        for (Direction dir : Direction.values()) {
            BlockState blockState = level.getBlockState(blockPos.relative(dir));
            if (blockState.is(BlockTags.AIR) || blockState.is(BlockTags.REPLACEABLE)) return true;
        }
        return false;
    }

    public boolean isInRange(BlockPos center, BlockPos blockPos) {
        switch (shape) {
            case SPHERE -> {
                return center.distSqr(blockPos) < range * range;
            }
            case DIAMOND -> {
                return (double) center.distManhattan(blockPos) < range;
            }
            default -> {
                BlockPos dist = blockPos.subtract(center);
                return Math.abs(dist.getX()) < range && Math.abs(dist.getY()) < range && Math.abs(dist.getZ()) < range;
            }
        }
    }


    public enum Shape implements StringRepresentable {
        SPHERE("sphere"), CUBE("cube"), DIAMOND("diamond");

        public final String name;

        Shape(String name) {
            this.name = name;
        }

        @Override
        public String getSerializedName() {
            return name;
        }
    }
}
