package net.smok.om.spell.actions;

import com.mojang.serialization.MapCodec;
import net.smok.om.spell.SpellRunner;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.function.Function;

public record UnitAction(Function<SpellRunner, SpellRunner.ProcessStatus> action) implements SpellAction {



    @Contract(value = " -> new", pure = true)
    @Override
    public @NotNull Type actionType() {
        return new Type();
    }

    @Override
    public SpellRunner.ProcessStatus run(SpellRunner spellRunner) {
        return action.apply(spellRunner);
    }


    public class Type implements SpellActionType<UnitAction> {
        @Override
        public MapCodec<UnitAction> codec() {
            return MapCodec.unit(UnitAction.this);
        }

        public UnitAction instance() {
            return UnitAction.this;
        }
    }
}
