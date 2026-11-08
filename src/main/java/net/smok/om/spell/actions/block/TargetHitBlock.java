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

public record TargetHitBlock(BlockPredicate filter) implements SpellAction {
    public static final TargetHitBlock ANY = new TargetHitBlock(BlockPredicate.alwaysTrue());

    public static final MapCodec<TargetHitBlock> WITH_FILTER = RecordCodecBuilder.mapCodec(instance -> instance.group(
            BlockPredicate.CODEC.fieldOf("filter").forGetter(TargetHitBlock::filter)
    ).apply(instance, TargetHitBlock::new));

    public static final MapCodec<TargetHitBlock> MAP_CODEC = Codec.mapEither(WITH_FILTER, MapCodec.unit(ANY)).xmap(Either::orThrow, targetHitBlock -> {
        if (targetHitBlock.filter instanceof AlwaysTrueTest) return Either.right(ANY);
        return Either.left(targetHitBlock);
    });


    @Override
    public SpellActionType<?> actionType() {
        return SpellActionType.TARGET_HIT_BLOCK;
    }

    @Override
    public SpellRunner.ProcessStatus run(SpellRunner spellRunner) {
        return spellRunner.executeFor(StoredValueType.HIT, (level, hitHolder) -> {
            if (filter.test(level, hitHolder.blockPos())) {
                spellRunner.put(StoredValueType.BLOCK, hitHolder.blockPos());
                return true;
            }
            return false;
        });
    }
}
