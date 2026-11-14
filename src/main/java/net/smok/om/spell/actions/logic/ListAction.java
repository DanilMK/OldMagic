package net.smok.om.spell.actions.logic;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.smok.om.spell.actions.SpellAction;
import net.smok.om.spell.actions.SpellActionType;
import net.smok.om.spell.SpellRunner;

import java.util.List;

public record ListAction(List<Holder<SpellAction>> actionList, SpellRunner.StatusHandle handleStatus) implements SpellAction {

    public static final MapCodec<ListAction> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            SpellAction.HOLDER_CODEC.listOf().fieldOf("action_list").forGetter(ListAction::actionList),
            SpellRunner.StatusHandle.CODEC.optionalFieldOf("handle_status", SpellRunner.StatusHandle.IGNORE).forGetter(ListAction::handleStatus)
    ).apply(instance, ListAction::new));

    @SafeVarargs
    public ListAction(Holder<SpellAction>... actions) {
        this(List.of(actions), SpellRunner.StatusHandle.IGNORE);
    }

    @SafeVarargs
    public static ListAction list(Holder<SpellAction>... actions) {
        return new ListAction(List.of(actions), SpellRunner.StatusHandle.IGNORE);
    }

    public static ListAction list(List<Holder<SpellAction>> actions) {
        return new ListAction(actions, SpellRunner.StatusHandle.IGNORE);
    }

    @SafeVarargs
    public static ListAction chain(Holder<SpellAction>... actions) {
        return new ListAction(List.of(actions), SpellRunner.StatusHandle.CHAIN);
    }

    public static ListAction chain(List<Holder<SpellAction>> actions) {
        return new ListAction(actions, SpellRunner.StatusHandle.CHAIN);
    }

    @SafeVarargs
    public static ListAction firstEnter(Holder<SpellAction>... actions) {
        return new ListAction(List.of(actions), SpellRunner.StatusHandle.FIRST_ENTER);
    }

    public static ListAction firstEnter(List<Holder<SpellAction>> actions) {
        return new ListAction(actions, SpellRunner.StatusHandle.FIRST_ENTER);
    }

    @Override
    public SpellActionType<?> actionType() {
        return SpellActionType.LIST;
    }

    @Override
    public SpellRunner.ProcessStatus run(SpellRunner spellRunner) {
        for (Holder<SpellAction> action : actionList) {
            SpellRunner.ProcessStatus run = action.value().run(spellRunner);
            if (run == SpellRunner.ProcessStatus.RUNNING) return SpellRunner.ProcessStatus.RUNNING;
            SpellRunner.ProcessStatus result = handleStatus.handle(run);
            if (result != SpellRunner.ProcessStatus.RUNNING) return result;
        }

        return handleStatus.getOnEnd();
    }
}
