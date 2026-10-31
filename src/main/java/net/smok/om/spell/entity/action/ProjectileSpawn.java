package net.smok.om.spell.entity.action;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.world.entity.LivingEntity;
import net.smok.om.entity.SpellProjectile;
import net.smok.om.spell.*;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public record ProjectileSpawn(ProjectileProps props, @Nullable Holder<SpellAction> onTick, @Nullable Holder<SpellAction> onBlockHit, @Nullable Holder<SpellAction> onEntityHit, int onTickDelay) implements SpellAction {

    public static final MapCodec<ProjectileSpawn> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            ProjectileProps.CODEC.fieldOf("properties").forGetter(ProjectileSpawn::props),
            SpellAction.HOLDER_CODEC.optionalFieldOf("onTick").forGetter((ProjectileSpawn t) -> Optional.ofNullable(t.onTick)),
            SpellAction.HOLDER_CODEC.optionalFieldOf("onBlockHit").forGetter(projectileSpawn -> Optional.ofNullable(projectileSpawn.onBlockHit())),
            SpellAction.HOLDER_CODEC.optionalFieldOf("onEntityHit").forGetter(projectileSpawn -> Optional.ofNullable(projectileSpawn.onEntityHit())),
            Codec.intRange(1, 1200).optionalFieldOf("onTickDelay", 1).forGetter(ProjectileSpawn::onTickDelay)
    ).apply(instance,
            (ProjectileProps props1, Optional<Holder<SpellAction>> onTick1, Optional<Holder<SpellAction>> onBlockHit1, Optional<Holder<SpellAction>> onEntityHit1, Integer onTickDelay) ->
            new ProjectileSpawn(props1, onTick1.orElse(null), onBlockHit1.orElse(null), onEntityHit1.orElse(null), onTickDelay)));

    @Override
    public SpellActionType<?> getType() {
        return SpellActionType.PROJECTILE;
    }

    @Override
    public SpellRunner.ProcessStatus run(SpellRunner spellRunner) {
        return spellRunner.execute(level -> {
            if (spellRunner.caster() instanceof LivingEntity caster) {
                SpellProjectile.cast(level, caster, spellRunner.getWand(), this, props);
                return true;
            }
            return false;
        });
    }
}
