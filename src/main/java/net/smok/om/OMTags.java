package net.smok.om;

import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.block.Block;

public interface OMTags {

    TagKey<Block> SEVERING_PLANTS = TagKey.create(Registries.BLOCK, Values.id("severing_plants"));
    TagKey<Block> GROWABLE_PLANTS = TagKey.create(Registries.BLOCK, Values.id("growable_plants"));

    TagKey<EntityType<?>> FLAMEABLE_ENTITIES = TagKey.create(Registries.ENTITY_TYPE, Values.id("flameable"));
    
    
}
