package net.smok.om.spell.actions.entity;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.smok.om.spell.SpellRunner;
import net.smok.om.spell.actions.SpellAction;
import net.smok.om.spell.actions.SpellActionType;
import net.smok.om.utils.EntityHolder;
import net.smok.om.utils.StoredValueType;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public record ForEachEntityTarget(Holder<SpellAction> action, int delay, int combineAmount, String entitiesListTarget, String setEntityTarget) implements SpellAction {

    public static final MapCodec<ForEachEntityTarget> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            SpellAction.HOLDER_CODEC.fieldOf("action").forGetter(ForEachEntityTarget::action),
            Codec.INT.fieldOf("delay").forGetter(ForEachEntityTarget::delay),
            Codec.INT.fieldOf("combine_amount").forGetter(ForEachEntityTarget::combineAmount),
            Codec.STRING.optionalFieldOf("entities_list_target", "entities").forGetter(ForEachEntityTarget::entitiesListTarget),
            Codec.STRING.optionalFieldOf("set_entity_target", "entity").forGetter(ForEachEntityTarget::setEntityTarget)
    ).apply(instance, ForEachEntityTarget::new));


    @Contract("_, _, _, _, _ -> new")
    public static @NotNull ForEachEntityTarget target(Holder<SpellAction> action, int delay, int combineAmount, String entitiesListTarget, String setEntityTarget) {
        return new ForEachEntityTarget(action, delay, combineAmount, entitiesListTarget, setEntityTarget);
    }

    @Contract("_, _, _ -> new")
    public static @NotNull ForEachEntityTarget target(Holder<SpellAction> action, int delay, int combineAmount) {
        return new ForEachEntityTarget(action, delay, combineAmount, "entities", "entity");
    }

    @Contract("_, _, _ -> new")
    public static @NotNull ForEachEntityTarget target(Holder<SpellAction> action, String entitiesListTarget, String setEntityTarget) {
        return new ForEachEntityTarget(action, 0, 0, entitiesListTarget, setEntityTarget);
    }

    @Contract("_ -> new")
    public static @NotNull ForEachEntityTarget target(Holder<SpellAction> action) {
        return new ForEachEntityTarget(action, 0, 0, "entities", "entity");
    }

    @Override
    public SpellActionType<?> actionType() {
        return SpellActionType.FOR_EACH_ENTITY_TARGET;
    }

    @Override
    public SpellRunner.ProcessStatus run(SpellRunner spellRunner) {
        int combined = 0;
        List<EntityHolder> entityTargets = spellRunner.getOrCreateList(entitiesListTarget, StoredValueType.ENTITY);
        for (EntityHolder entityTarget : entityTargets) {

            if (delay > 0 && combined > combineAmount) {
                combined -= combineAmount;
                if (spellRunner.executeWait(delay)) return SpellRunner.ProcessStatus.RUNNING;
            }

            spellRunner.put(setEntityTarget, StoredValueType.ENTITY, entityTarget);
            SpellRunner.ProcessStatus run = action.value().run(spellRunner);
            if (run == SpellRunner.ProcessStatus.SUCCESS) combined++;

            if (run == SpellRunner.ProcessStatus.RUNNING) return SpellRunner.ProcessStatus.RUNNING;
        }
        return SpellRunner.ProcessStatus.SUCCESS;
    }
}
