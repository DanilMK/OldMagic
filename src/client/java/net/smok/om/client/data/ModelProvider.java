package net.smok.om.client.data;

import net.fabricmc.fabric.api.client.datagen.v1.provider.FabricModelProvider;
import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.ItemModelGenerators;
import net.minecraft.client.data.models.model.ItemModelUtils;
import net.minecraft.client.data.models.model.ModelLocationUtils;
import net.minecraft.client.data.models.model.ModelTemplates;
import net.minecraft.client.data.models.model.TextureMapping;
import net.minecraft.client.renderer.item.ItemModel;
import net.minecraft.client.renderer.item.SelectItemModel;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.smok.om.client.render.SpellProperty;
import net.smok.om.item.OMItems;
import net.smok.om.spell.Spell;
import net.smok.om.spell.Spells;

import java.util.ArrayList;
import java.util.List;

public class ModelProvider extends FabricModelProvider {




    public ModelProvider(FabricPackOutput output) {
        super(output);
    }


    @Override
    public void generateBlockStateModels(BlockModelGenerators blockModelGenerators) {

    }

    @Override
    public void generateItemModels(ItemModelGenerators itemModelGenerators) {

        List<SelectItemModel.SwitchCase<ResourceKey<Spell>>> cases = new ArrayList<>(Spells.ALL.size());

        for (ResourceKey<Spell> key : Spells.ALL) {
            Identifier location = key.identifier().withPrefix("item/spell/");
            Material material = new Material(location);
            ModelTemplates.FLAT_ITEM.create(location, TextureMapping.layer0(material), itemModelGenerators.modelOutput);
            ItemModel.Unbaked model = ItemModelUtils.plainModel(location);
            cases.add(ItemModelUtils.when(key, model));
        }

        itemModelGenerators.itemModelOutput.accept(OMItems.SPELL_PLACEHOLDER,
                ItemModelUtils.select(new SpellProperty(), cases));

        generateItemWithSpellSelect(itemModelGenerators, OMItems.SCROLL);
        generateItemWithSpellSelect(itemModelGenerators, OMItems.SPELL_BOOK);
    }

    private void generateItemWithSpellSelect(ItemModelGenerators itemModelGenerators, Item item) {

        Identifier modelLocation = ModelLocationUtils.getModelLocation(item);
        Material itemTexture = TextureMapping.getItemTexture(item);

        List<SelectItemModel.SwitchCase<ResourceKey<Spell>>> cases = new ArrayList<>(Spells.ALL.size());
        for (ResourceKey<Spell> key : Spells.ALL) {
            Identifier itemModelLocation = modelLocation.withSuffix("/" + key.identifier().getPath());
            Identifier spellModelLocation = key.identifier().withPrefix("item/spell/");
            Material material = new Material(spellModelLocation);

            itemModelGenerators.generateLayeredItem(itemModelLocation, itemTexture, material);
            ItemModel.Unbaked model = ItemModelUtils.plainModel(itemModelLocation);
            cases.add(ItemModelUtils.when(key, model));
        }

        ModelTemplates.FLAT_ITEM.create(modelLocation, TextureMapping.layer0(itemTexture), itemModelGenerators.modelOutput);
        itemModelGenerators.itemModelOutput.accept(item, ItemModelUtils.select(new SpellProperty(), ItemModelUtils.plainModel(modelLocation), cases));
    }

    @Override
    public String getName() {
        return "Old Magic Model Generator";
    }
}
