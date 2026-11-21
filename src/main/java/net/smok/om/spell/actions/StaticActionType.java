package net.smok.om.spell.actions;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.Holder;
import net.smok.om.spell.SpellRunner;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.function.Function;

public final class StaticActionType implements SpellActionType<StaticActionType.Action> {
    private final Action action;
    private final MapCodec<Action> codec;


    public StaticActionType(Function<SpellRunner, SpellRunner.ProcessStatus> action) {
        this.action = new Action(action);
        this.codec = MapCodec.unit(this.action);
    }

    public Action action() {
        return action;
    }

    @Contract(value = " -> new", pure = true)
    public @NotNull Holder<SpellAction> holder() {
        return Holder.direct(action);
    }

    @Override
    public MapCodec<Action> codec() {
        return codec;
    }


    public class Action implements SpellAction {

        private final Function<SpellRunner, SpellRunner.ProcessStatus> action;

        public Action(Function<SpellRunner, SpellRunner.ProcessStatus> action) {
            this.action = action;
        }

        @Override
        public SpellActionType<?> actionType() {
            return StaticActionType.this;
        }

        @Override
        public SpellRunner.ProcessStatus run(SpellRunner spellRunner) {
            return action.apply(spellRunner);
        }
    }
}
