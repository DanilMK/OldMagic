package net.smok.om.block;

import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.smok.om.Values;
import net.smok.om.block.entity.WandCraftTableEntity;

import java.util.function.Function;

public interface OMBlocks {

    Block WANDCRAFT_TABLE = register("wandcraft_table", WandCraftTable::new, BlockBehaviour.Properties.of().sound(SoundType.WOOD), true);

    BlockEntityType<WandCraftTableEntity> WANDCRARFT_TABLE_ENTITY = register("wandcraft_table", WandCraftTableEntity::new, WANDCRAFT_TABLE);

    static void init() {}

    private static <T extends BlockEntity> BlockEntityType<T> register(String path, FabricBlockEntityTypeBuilder.Factory<? extends T> factory, Block... blocks) {
        Identifier id = Values.id(path);
        return Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, id, FabricBlockEntityTypeBuilder.<T>create(factory, blocks).build());
    }

    private static Block register(String path, Function<BlockBehaviour.Properties, Block> blockFactory, BlockBehaviour.Properties settings, boolean shouldRegisterItem) {
        ResourceKey<Block> blockKey = ResourceKey.create(Registries.BLOCK, Values.id(path));
        Block block = blockFactory.apply(settings.setId(blockKey));

        if (shouldRegisterItem) {
            ResourceKey<Item> itemKey = ResourceKey.create(Registries.ITEM, Values.id(path));
            BlockItem blockItem = new BlockItem(block, new Item.Properties().setId(itemKey).useBlockDescriptionPrefix());
            Registry.register(BuiltInRegistries.ITEM, itemKey, blockItem);
        }

        return Registry.register(BuiltInRegistries.BLOCK, blockKey, block);
    }
}
