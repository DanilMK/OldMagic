package net.smok.om.spell.actions;

import com.mojang.serialization.Codec;
import net.minecraft.core.Holder;
import net.minecraft.resources.RegistryFileCodec;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.smok.om.Values;
import net.smok.om.spell.Spell;
import net.smok.om.spell.SpellRunner;
import net.smok.om.spell.SpellTickManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface SpellAction {
    Codec<SpellAction> DIRECT_CODEC = Values.SPELL_ACTION_TYPES.byNameCodec().dispatch(SpellAction::actionType, SpellActionType::codec);
    Codec<Holder<SpellAction>> HOLDER_CODEC = RegistryFileCodec.create(Values.SPELL_ACTIONS_KEY, DIRECT_CODEC);


    static @NotNull SpellRunner run(@NotNull ServerLevel level, @Nullable Holder<Spell> spell, @NotNull Holder<SpellAction> action, @NotNull Entity actor, @NotNull ItemStack wand) {
        SpellRunner runner = new SpellRunner(spell, action, actor, wand);
        SpellTickManager.get(level).startSpell(runner);
        return runner;
    }


    SpellActionType<?> actionType();

    SpellRunner.ProcessStatus run(SpellRunner spellRunner);

    default Holder<SpellAction> inline() {
        return Holder.direct(this);
    }
}
