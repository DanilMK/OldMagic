package net.smok.om.spell.actions;

import net.minecraft.resources.ResourceKey;
import net.smok.om.Values;

public interface SpellActions {


    ResourceKey<SpellAction> BASIC = register("basic");
    ResourceKey<SpellAction> BASIC_BROKEN = register("basic_broken");
    ResourceKey<SpellAction> FIRE_SPHERE = register("fire_sphere");
    ResourceKey<SpellAction> ICE_SPHERE = register("ice_sphere");
    ResourceKey<SpellAction> ICE_SPHERE_IN_WATER = register("ice_sphere_in_water");
    ResourceKey<SpellAction> GROW_PLANTS = register("grow_plants");
    ResourceKey<SpellAction> SERVING_PLANTS = register("serving_plants");
    ResourceKey<SpellAction> PULL = register("pull");
    ResourceKey<SpellAction> PUSH = register("push");
    ResourceKey<SpellAction> PUDDLE = register("puddle");
    ResourceKey<SpellAction> COLLECT_ITEMS = register("collect_items");
    ResourceKey<SpellAction> CHANGE_COLOR = register("change_color");
    ResourceKey<SpellAction> FLOWER_FIELD = register("flower_field");
    ResourceKey<SpellAction> FIRE_RING = register("fire_ring");
    ResourceKey<SpellAction> FIRE_LOCK = register("fire_lock");

    ResourceKey<SpellAction> PROJECTILE_BASIC = register("projectile_basic");
    ResourceKey<SpellAction> PROJECTILE_BASIC_BROKEN = register("projectile_basic_broken");
    ResourceKey<SpellAction> PROJECTILE_FIRE_SPHERE = register("projectile_fire_sphere");
    ResourceKey<SpellAction> PROJECTILE_ICE_SPHERE = register("projectile_ice_sphere");
    ResourceKey<SpellAction> PROJECTILE_GROW_PLANTS = register("projectile_grow_plants");
    ResourceKey<SpellAction> PROJECTILE_SERVING_PLANTS = register("projectile_serving_plants");
    ResourceKey<SpellAction> PROJECTILE_PULL = register("projectile_pull");
    ResourceKey<SpellAction> PROJECTILE_PUSH = register("projectile_push");
    ResourceKey<SpellAction> PROJECTILE_PUDDLE = register("projectile_puddle");
    ResourceKey<SpellAction> PROJECTILE_FLOWER_FIELD = register("projectile_flower_field");
    ResourceKey<SpellAction> PROJECTILE_CHANGE_COLOR = register("projectile_change_color");

    private static ResourceKey<SpellAction> register(String name) {
        return ResourceKey.create(Values.SPELL_ACTIONS_KEY, Values.id(name));
    }
}
