package net.smok.om.spell.logic;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.smok.om.spell.SpellAction;
import net.smok.om.spell.SpellActionType;
import net.smok.om.spell.SpellRunner;

import java.util.List;

public record RandomSelectAction(List<Holder<SpellAction>> actionList) implements SpellAction{

    public static final MapCodec<RandomSelectAction> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            SpellAction.HOLDER_CODEC.listOf().fieldOf("action_list").forGetter(RandomSelectAction::actionList)
    ).apply(instance, RandomSelectAction::new));

    @SafeVarargs
    public RandomSelectAction(Holder<SpellAction>... actionList) {
        this(List.of(actionList));
    }

    @Override
    public SpellActionType<?> getType() {
        return SpellActionType.RANDOM_SELECT;
    }

    @Override
    public SpellRunner.ProcessStatus run(SpellRunner spellRunner) {
        int index = spellRunner.getRandom().nextInt(actionList.size());
        return actionList.get(index).value().run(spellRunner);
    }
}
