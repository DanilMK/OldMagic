package net.smok.om.spell.actions.entity;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.LivingEntity;
import net.smok.om.spell.actions.SpellAction;
import net.smok.om.spell.actions.SpellActionType;
import net.smok.om.spell.SpellRunner;
import net.smok.om.utils.StoredValueType;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public record DamageEntityAction(ResourceKey<DamageType> damageType, float damageAmount, String target) implements SpellAction {

    public static final MapCodec<DamageEntityAction> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            ResourceKey.codec(Registries.DAMAGE_TYPE).fieldOf("damage_type").forGetter(DamageEntityAction::damageType),
            Codec.FLOAT.fieldOf("amount").forGetter(DamageEntityAction::damageAmount),
            Codec.STRING.optionalFieldOf("target", "entity").forGetter(DamageEntityAction::target)
    ).apply(instance, DamageEntityAction::new));

    @Contract("_, _, _ -> new")
    public static @NotNull DamageEntityAction damage(ResourceKey<DamageType> damageType, float damageAmount, String target) {
        return new DamageEntityAction(damageType, damageAmount, target);
    }

    @Contract("_, _ -> new")
    public static @NotNull DamageEntityAction damage(ResourceKey<DamageType> damageType, float damageAmount) {
        return new DamageEntityAction(damageType, damageAmount, "entity");
    }

    @Contract("_ -> new")
    public static @NotNull DamageEntityAction damage(float damageAmount) {
        return new DamageEntityAction(DamageTypes.MAGIC, damageAmount, "entity");
    }

    @Contract("_, _ -> new")
    public static @NotNull DamageEntityAction damage(float damageAmount, String target) {
        return new DamageEntityAction(DamageTypes.MAGIC, damageAmount, target);
    }

    @Override
    public SpellActionType<?> actionType() {
        return SpellActionType.DAMAGE_ENTITIES;
    }

    @Override
    public SpellRunner.ProcessStatus run(SpellRunner spellRunner) {
        return spellRunner.executeFor(target, StoredValueType.ENTITY, (level, entityHolder) -> {
            if (entityHolder.getEntity() instanceof LivingEntity livingEntity)
                return livingEntity.hurtServer(level, level.damageSources().source(damageType, spellRunner.getActor(), spellRunner.caster()), damageAmount);
            return false;
        });
    }
}
