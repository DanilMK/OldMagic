package net.smok.om.spell.actions.logic;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.smok.om.spell.actions.SpellAction;
import net.smok.om.spell.actions.SpellActionType;
import net.smok.om.spell.SpellRunner;

import java.util.List;

@Deprecated
public record FirstEnterAction(List<Holder<SpellAction>> actionList) implements SpellAction {

    public static final MapCodec<FirstEnterAction> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            SpellAction.HOLDER_CODEC.listOf().fieldOf("action_list").forGetter(FirstEnterAction::actionList)
    ).apply(instance, FirstEnterAction::new));

    @SafeVarargs
    public FirstEnterAction(Holder<SpellAction>... actions) {
        this(List.of(actions));
    }


    @Override
    public SpellActionType<?> actionType() {
        return SpellActionType.FIRST_ENTER;
    }

    @Override
    public SpellRunner.ProcessStatus run(SpellRunner spellRunner) {
        for (Holder<SpellAction> action : actionList) {
            SpellRunner.ProcessStatus run = action.value().run(spellRunner);
            if (run == SpellRunner.ProcessStatus.RUNNING) return SpellRunner.ProcessStatus.RUNNING;
            if (run == SpellRunner.ProcessStatus.SUCCESS) return SpellRunner.ProcessStatus.SUCCESS;
        }
        return SpellRunner.ProcessStatus.FAILURE;
    }
}
