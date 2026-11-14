package net.smok.om.spell.actions.entity;

import com.mojang.serialization.MapCodec;
import net.smok.om.entity.SpellProjectile;
import net.smok.om.spell.actions.SpellAction;
import net.smok.om.spell.actions.SpellActionType;
import net.smok.om.spell.SpellRunner;

@Deprecated
public class DiscardProjectile implements SpellAction {

    public static final DiscardProjectile INSTANCE = new DiscardProjectile();
    public static final MapCodec<DiscardProjectile> MAP_CODEC = MapCodec.unit(INSTANCE);

    @Override
    public SpellActionType<?> actionType() {
        return SpellActionType.DISCARD_PROJECTILE;
    }

    @Override
    public SpellRunner.ProcessStatus run(SpellRunner spellRunner) {
        if (spellRunner.getActor() instanceof SpellProjectile spellProjectile) {
            spellProjectile.discard();
            return SpellRunner.ProcessStatus.SUCCESS;
        }
        return SpellRunner.ProcessStatus.FAILURE;
    }
}
