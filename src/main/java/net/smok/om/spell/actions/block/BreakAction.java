package net.smok.om.spell.actions.block;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.level.levelgen.blockpredicates.BlockPredicate;
import net.smok.om.spell.actions.SpellAction;
import net.smok.om.spell.actions.SpellActionType;
import net.smok.om.spell.SpellRunner;
import net.smok.om.utils.StoredValueType;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public record BreakAction(String blockTarget, BlockPredicate filter, boolean dropResources) implements SpellAction {

    public static final MapCodec<BreakAction> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codec.STRING.optionalFieldOf("block_target", "block").forGetter(BreakAction::blockTarget),
            BlockPredicate.CODEC.fieldOf("filter").forGetter(BreakAction::filter),
            Codec.BOOL.fieldOf("drop_resources").forGetter(BreakAction::dropResources)
    ).apply(instance, BreakAction::new));

    @Contract(" -> new")
    public static @NotNull BreakAction anyWithDrops() {
        return new BreakAction("block", BlockPredicate.alwaysTrue(), true);
    }

    @Contract(" -> new")
    public static @NotNull BreakAction anyWithoutDrop() {
        return new BreakAction("block", BlockPredicate.alwaysTrue(), false);
    }

    @Contract("_, _ -> new")
    public static @NotNull BreakAction breakBlock(BlockPredicate filter, boolean dropResources) {
        return new BreakAction("block", filter, dropResources);
    }

    @Contract("_, _, _ -> new")
    public static @NotNull BreakAction breakBlock(String target, BlockPredicate filter, boolean dropResources) {
        return new BreakAction(target, filter, dropResources);
    }


    @Override
    public SpellActionType<?> actionType() {
        return SpellActionType.BLOCKS_BREAK;
    }

    @Override
    public SpellRunner.ProcessStatus run(SpellRunner spellRunner) {
        return spellRunner.executeFor(blockTarget, StoredValueType.BLOCK, (level, blockPos) -> {
            if (filter.test(level, blockPos)) {
                level.destroyBlock(blockPos, dropResources, spellRunner.caster());
                return true;
            }
            return false;
        });
    }
}
