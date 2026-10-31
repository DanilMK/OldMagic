package net.smok.om.entity;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.smok.om.Values;


public class OMEntities {

    public static void init() {

    }

    public static final EntityType<SpellProjectile> SPELL_PROJECTILE = registerEntity("spell_projectile", EntityType.Builder.<SpellProjectile>of(SpellProjectile::new, MobCategory.MISC).noLootTable().sized(0.25f, 0.25f).clientTrackingRange(4).updateInterval(10));

    private static <E extends Entity> EntityType<E> registerEntity(String id, EntityType.Builder<E> builder) {
        ResourceKey<EntityType<?>> key = ResourceKey.create(Registries.ENTITY_TYPE, Identifier.fromNamespaceAndPath(Values.MOD_ID, id));
        return Registry.register(BuiltInRegistries.ENTITY_TYPE, key, builder.build(key));
    }
}
