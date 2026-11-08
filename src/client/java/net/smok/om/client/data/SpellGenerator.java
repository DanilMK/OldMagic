package net.smok.om.client.data;

import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricDynamicRegistryProvider;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.smok.om.Values;
import net.smok.om.spell.*;
import net.smok.om.spell.actions.SpellAction;
import net.smok.om.spell.actions.SpellActions;
import net.smok.om.spell.cast.Casting;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class SpellGenerator extends FabricDynamicRegistryProvider {


    private final List<Holder.Reference<Spell>> values = new ArrayList<>();
    private BootstrapContext<Spell> context;

    public SpellGenerator(FabricPackOutput output, CompletableFuture<HolderLookup.Provider> registriesFuture) {
        super(output, registriesFuture);
    }


    public void bootStrap(BootstrapContext<Spell> context) {
        this.context = context;
        addEntries();
        this.context = null;
    }

    @Override
    protected void configure(HolderLookup.Provider registries, Entries entries) {
        values.forEach(entries::add);
    }

    private void addEntries() {

        add(Spells.BASIC, new Spell(new Casting(4, 0, false, false), action(SpellActions.PROJECTILE_BASIC), action(SpellActions.PROJECTILE_BASIC_BROKEN)));
        add(Spells.FIRE, new Spell(new Casting(4, 20, false, false), action(SpellActions.PROJECTILE_FIRE_SPHERE), action(SpellActions.PROJECTILE_BASIC_BROKEN)));
        add(Spells.ICE, new Spell(new Casting(20, 4, false, false), action(SpellActions.PROJECTILE_ICE_SPHERE), action(SpellActions.PROJECTILE_BASIC_BROKEN)));
        add(Spells.GROW_PLANTS, new Spell(new Casting(4, 8, false, false), action(SpellActions.PROJECTILE_GROW_PLANTS), action(SpellActions.PROJECTILE_BASIC_BROKEN)));
        add(Spells.SEVERING_PLANTS, new Spell(new Casting(4, 8, false, false), action(SpellActions.PROJECTILE_SERVING_PLANTS), action(SpellActions.PROJECTILE_BASIC_BROKEN)));
        add(Spells.COLLECT_ITEMS, new Spell(new Casting(20, 0, false, false), action(SpellActions.COLLECT_ITEMS), action(SpellActions.PROJECTILE_BASIC_BROKEN)));
        add(Spells.PULL, new Spell(new Casting(10, 0, false, false), action(SpellActions.PROJECTILE_PULL), action(SpellActions.PROJECTILE_BASIC_BROKEN)));
        add(Spells.PUSH, new Spell(new Casting(10, 0, false, false), action(SpellActions.PROJECTILE_PUSH), action(SpellActions.PROJECTILE_BASIC_BROKEN)));
        add(Spells.PUDDLE, new Spell(new Casting(20, 0, false, false), action(SpellActions.PROJECTILE_PUDDLE), action(SpellActions.PROJECTILE_BASIC_BROKEN)));
        add(Spells.FLOWER_FIELD, new Spell(new Casting(20, 0, false, false), action(SpellActions.PROJECTILE_FLOWER_FIELD), action(SpellActions.PROJECTILE_BASIC_BROKEN)));
        //add(Spells.CHANGE_COLOR, new Spell(new Casting(20, 0, false, false), action(SpellActions.PROJECTILE_CHANGE_CLOR), action(SpellActions.PROJECTILE_BASIC_BROKEN)));
    }

    private Holder.Reference<SpellAction> action(ResourceKey<SpellAction> key) {
        return context.lookup(Values.SPELL_ACTIONS_KEY).getOrThrow(key);
    }

    private @NotNull Holder.Reference<Spell> add(ResourceKey<Spell> resourceKey, Spell spell) {
        Holder.Reference<Spell> value = context.register(resourceKey, spell);
        values.add(value);
        return value;
    }

    @Override
    public String getName() {
        return "Spells Generator";
    }
}
