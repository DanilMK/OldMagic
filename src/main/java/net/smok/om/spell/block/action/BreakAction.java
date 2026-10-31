package net.smok.om.spell.block.action;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.level.levelgen.structure.templatesystem.RuleTest;
import net.smok.om.spell.SpellAction;
import net.smok.om.spell.SpellActionType;
import net.smok.om.spell.SpellRunner;

public record BreakAction(RuleTest breakable, boolean dropResources) implements SpellAction {

    public static final MapCodec<BreakAction> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            RuleTest.CODEC.fieldOf("blocks").forGetter(BreakAction::breakable),
            Codec.BOOL.fieldOf("dropResources").forGetter(BreakAction::dropResources)
    ).apply(instance, BreakAction::new));


    @Override
    public SpellActionType<?> getType() {
        return SpellActionType.BLOCKS_BREAK;
    }

    @Override
    public SpellRunner.ProcessStatus run(SpellRunner spellRunner) {
        return spellRunner.executeForAnyBlock((level, blockPos, blockState) -> {
            if (breakable.test(blockState, level.getRandom())) {
                level.destroyBlock(blockPos, dropResources, spellRunner.caster());
                return true;
            }
            return false;
        });
    }
}
