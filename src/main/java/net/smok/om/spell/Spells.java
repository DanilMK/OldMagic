package net.smok.om.spell;

import net.minecraft.resources.ResourceKey;
import net.smok.om.Values;

import java.util.List;

public interface Spells {

    ResourceKey<Spell> BASIC = register("basic");
    ResourceKey<Spell> FIRE = register("fire");
    ResourceKey<Spell> ICE = register("ice");
    ResourceKey<Spell> COLLECT_ITEMS = register("collect_items");
    ResourceKey<Spell> PULL = register("pull");
    ResourceKey<Spell> PUSH = register("push");
    ResourceKey<Spell> GROW_PLANTS = register("grow_plants");
    ResourceKey<Spell> SEVERING_PLANTS = register("severing_plants");
    ResourceKey<Spell> LEVITATION = register("levitation");
    ResourceKey<Spell> PUDDLE = register("puddle");
    ResourceKey<Spell> FLOWER_FIELD = register("flower_field");
    ResourceKey<Spell> CHANGE_COLOR = register("change_color");

    List<ResourceKey<Spell>> ALL = List.of(
            BASIC,
            FIRE,
            ICE,
            COLLECT_ITEMS,
            PULL,
            PUSH,
            GROW_PLANTS,
            SEVERING_PLANTS,
            PUDDLE,
            FLOWER_FIELD
    );

    private static ResourceKey<Spell> register(String name) {
        return ResourceKey.create(Values.SPELLS_KEY, Values.id(name));
    }
}