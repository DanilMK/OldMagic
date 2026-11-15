package net.smok.om.spell.actions;

import com.mojang.serialization.Codec;
import net.minecraft.core.Holder;
import net.minecraft.resources.RegistryFileCodec;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.smok.om.Values;
import net.smok.om.spell.SpellRunner;
import net.smok.om.spell.SpellTickManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface SpellAction {
    Codec<SpellAction> DIRECT_CODEC = Values.SPELL_ACTION_TYPES.byNameCodec().dispatch(SpellAction::actionType, SpellActionType::codec);
    Codec<Holder<SpellAction>> HOLDER_CODEC = RegistryFileCodec.create(Values.SPELL_ACTIONS_KEY, DIRECT_CODEC);


    static void run(@NotNull ServerLevel level, @NotNull Holder<SpellAction> action, @NotNull Entity actor, @NotNull ItemStack wand) {
        SpellTickManager.get(level).startSpell(new SpellRunner(action, actor, wand));
    }

    static void run(@NotNull ServerLevel level, @NotNull Holder<SpellAction> action, @NotNull Entity actor, @NotNull ItemStack wand, @Nullable HitResult hitResult) {
        SpellRunner spellRunner = new SpellRunner(action, actor, wand);
        if (hitResult != null) spellRunner.targetEntityByHit(hitResult);
        SpellTickManager.get(level).startSpell(spellRunner);
    }

    SpellActionType<?> actionType();

    SpellRunner.ProcessStatus run(SpellRunner spellRunner);

    default Holder<SpellAction> inline() {
        return Holder.direct(this);
    }
}
