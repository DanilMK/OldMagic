package net.smok.om.spell.actions.logic;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.smok.om.spell.actions.SpellAction;
import net.smok.om.spell.actions.SpellActionType;
import net.smok.om.spell.SpellRunner;

import java.util.List;

@Deprecated
public record ChainListAction(List<Holder<SpellAction>> actionList) implements SpellAction {

    public static final MapCodec<ChainListAction> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            SpellAction.HOLDER_CODEC.listOf().fieldOf("action_list").forGetter(ChainListAction::actionList)
    ).apply(instance, ChainListAction::new));

    @SafeVarargs
    public ChainListAction(Holder<SpellAction>... actions) {
        this(List.of(actions));
    }

    @Override
    public SpellActionType<?> actionType() {
        return SpellActionType.CHAIN;
    }

    @Override
    public SpellRunner.ProcessStatus run(SpellRunner spellRunner) {
        for (Holder<SpellAction> action : actionList) {
            SpellRunner.ProcessStatus run = action.value().run(spellRunner);
            if (run == SpellRunner.ProcessStatus.RUNNING) return SpellRunner.ProcessStatus.RUNNING;
            if (run == SpellRunner.ProcessStatus.FAILURE) return SpellRunner.ProcessStatus.FAILURE;
        }
        return SpellRunner.ProcessStatus.SUCCESS;
    }
}
