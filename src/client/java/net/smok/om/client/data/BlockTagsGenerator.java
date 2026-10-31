package net.smok.om.client.data;

import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagsProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.Blocks;
import net.smok.om.OMTags;
import net.smok.om.block.OMBlocks;

import java.util.concurrent.CompletableFuture;

public class BlockTagsGenerator extends FabricTagsProvider.BlockTagsProvider {
    public BlockTagsGenerator(FabricPackOutput output, CompletableFuture<HolderLookup.Provider> registryLookupFuture) {
        super(output, registryLookupFuture);
    }

    @Override
    protected void addTags(HolderLookup.Provider registries) {
        valueLookupBuilder(OMTags.GROWABLE_PLANTS)
                .forceAddTag(BlockTags.CROPS)
                .forceAddTag(BlockTags.SAPLINGS);

        valueLookupBuilder(OMTags.SEVERING_PLANTS)
                .forceAddTag(BlockTags.SWORD_EFFICIENT)
                .forceAddTag(BlockTags.REPLACEABLE)
                .forceAddTag(BlockTags.FLOWERS)
                .add(Blocks.COBWEB);

        valueLookupBuilder(BlockTags.MINEABLE_WITH_AXE)
                .add(OMBlocks.WANDCRAFT_TABLE);
    }
}
