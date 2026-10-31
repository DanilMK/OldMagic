package net.smok.om.client.data;

import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricBlockLootSubProvider;
import net.minecraft.core.HolderLookup;
import net.smok.om.block.OMBlocks;

import java.util.concurrent.CompletableFuture;

public class LootGenerator extends FabricBlockLootSubProvider {
    protected LootGenerator(FabricPackOutput packOutput, CompletableFuture<HolderLookup.Provider> registriesFuture) {
        super(packOutput, registriesFuture);
    }

    @Override
    public void generate() {
        dropSelf(OMBlocks.WANDCRAFT_TABLE);
    }
}
