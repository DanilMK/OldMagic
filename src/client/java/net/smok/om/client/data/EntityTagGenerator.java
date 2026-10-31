package net.smok.om.client.data;

import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagsProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.tags.TagAppender;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.smok.om.OMTags;

import java.util.concurrent.CompletableFuture;

public class EntityTagGenerator extends FabricTagsProvider.EntityTypeTagsProvider {
    public EntityTagGenerator(FabricPackOutput output, CompletableFuture<HolderLookup.Provider> registryLookupFuture) {
        super(output, registryLookupFuture);
    }


    @Override
    protected void addTags(HolderLookup.Provider registries) {
        TagAppender<EntityType<?>, EntityType<?>> flameableEntities = valueLookupBuilder(OMTags.FLAMEABLE_ENTITIES);
        registries.lookupOrThrow(Registries.ENTITY_TYPE).listElements()
                .filter(ref -> !ref.value().fireImmune() &&
                        !ref.value().getCategory().equals(MobCategory.MISC))
                .forEach(entityTypeReference -> flameableEntities.add(entityTypeReference.value()));
    }
}
