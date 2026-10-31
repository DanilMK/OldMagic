package net.smok.om.client.data;

import com.mojang.serialization.Lifecycle;
import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import net.minecraft.core.RegistrySetBuilder;
import net.smok.om.Values;

public class OldMagicDataGenerator implements DataGeneratorEntrypoint {

    private ActionGenerator actionGenerator;
    private SpellGenerator spellGenerator;

    @Override
    public void onInitializeDataGenerator(FabricDataGenerator fabricDataGenerator) {
        FabricDataGenerator.Pack pack = fabricDataGenerator.createPack();
        pack.addProvider(BlockTagsGenerator::new);
        pack.addProvider(EntityTagGenerator::new);
        actionGenerator = pack.addProvider(ActionGenerator::new);
        spellGenerator = pack.addProvider(SpellGenerator::new);
        pack.addProvider(RecipeGenerator::new);
        pack.addProvider(AdvancementGenerator::new);
        pack.addProvider(LootGenerator::new);

        pack.addProvider(ModelProvider::new);

    }

    @Override
    public void buildRegistry(RegistrySetBuilder registryBuilder) {
        registryBuilder.add(Values.SPELL_ACTIONS_KEY, Lifecycle.stable(), context -> actionGenerator.bootStrap(context));
        registryBuilder.add(Values.SPELLS_KEY, Lifecycle.stable(), context -> spellGenerator.bootStrap(context));
    }
}
