package net.smok.om.spell.actions.block;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.level.levelgen.blockpredicates.BlockPredicate;
import net.minecraft.world.level.levelgen.structure.templatesystem.AlwaysTrueTest;
import net.smok.om.spell.SpellRunner;
import net.smok.om.spell.actions.SpellAction;
import net.smok.om.spell.actions.SpellActionType;
import net.smok.om.utils.StoredValueType;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public record TargetHitBlock(String hitTarget, BlockPredicate filter, int forwardOffset) implements SpellAction {
    public static final TargetHitBlock ANY = new TargetHitBlock("block", BlockPredicate.alwaysTrue(), 0);

    public static final MapCodec<TargetHitBlock> WITH_FILTER = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codec.STRING.optionalFieldOf("hitTarget", "block").forGetter(TargetHitBlock::hitTarget),
            BlockPredicate.CODEC.fieldOf("filter").forGetter(TargetHitBlock::filter),
            Codec.INT.fieldOf("forwardOffset").forGetter(TargetHitBlock::forwardOffset)
    ).apply(instance, TargetHitBlock::new));


    public static final MapCodec<TargetHitBlock> MAP_CODEC = Codec.mapEither(WITH_FILTER, MapCodec.unit(ANY)).xmap(Either::orThrow, targetHitBlock -> {
        if (targetHitBlock.filter instanceof AlwaysTrueTest) return Either.right(ANY);
        return Either.left(targetHitBlock);
    });

    @Contract("_, _, _ -> new")
    public static @NotNull TargetHitBlock hit(String hitTarget, BlockPredicate filter, int forwardOffset) {
        return new TargetHitBlock(hitTarget, filter, forwardOffset);
    }

    @Contract("_, _ -> new")
    public static @NotNull TargetHitBlock hit(String hitTarget, BlockPredicate filter) {
        return new TargetHitBlock(hitTarget, filter, 0);
    }

    @Contract("_ -> new")
    public static @NotNull TargetHitBlock hit(String hitTarget) {
        return new TargetHitBlock(hitTarget, BlockPredicate.alwaysTrue(), 0);
    }

    @Contract("_, _ -> new")
    public static @NotNull TargetHitBlock hit(String hitTarget, int forwardOffset) {
        return new TargetHitBlock(hitTarget, BlockPredicate.alwaysTrue(), forwardOffset);
    }

    @Contract("_, _ -> new")
    public static @NotNull TargetHitBlock hit(BlockPredicate filter, int forwardOffset) {
        return new TargetHitBlock("block", filter, forwardOffset);
    }

    @Contract("_ -> new")
    public static @NotNull TargetHitBlock hit(BlockPredicate filter) {
        return new TargetHitBlock("block", filter, 0);
    }

    public static TargetHitBlock hit() {
        return ANY;
    }

    @Contract("_ -> new")
    public static @NotNull TargetHitBlock hit(int forwardOffset) {
        return new TargetHitBlock("block", BlockPredicate.alwaysTrue(), forwardOffset);
    }


    @Override
    public SpellActionType<?> actionType() {
        return SpellActionType.TARGET_HIT_BLOCK;
    }

    @Override
    public SpellRunner.ProcessStatus run(SpellRunner spellRunner) {
        return spellRunner.executeFor("hit", StoredValueType.HIT, (level, hitHolder) -> {
            if (filter.test(level, hitHolder.blockPos())) {
                spellRunner.put("block", StoredValueType.BLOCK,
                        hitHolder.hitDirection() != null && forwardOffset != 0 ?
                                hitHolder.blockPos().relative(hitHolder.hitDirection(), forwardOffset) :
                                hitHolder.blockPos());
                return true;
            }
            return false;
        });
    }
}
