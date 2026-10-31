package net.smok.om.spell.block.action;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.smok.om.Debug;
import net.smok.om.spell.SpellAction;
import net.smok.om.spell.SpellActionType;
import net.smok.om.spell.SpellRunner;

import java.util.Optional;

public record FeatureAction(ResourceKey<ConfiguredFeature<?, ?>> feature) implements SpellAction {

    public static final MapCodec<FeatureAction> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            ResourceKey.codec(Registries.CONFIGURED_FEATURE).fieldOf("feature").forGetter(FeatureAction::feature)
    ).apply(instance, FeatureAction::new));


    @Override
    public SpellActionType<?> getType() {
        return SpellActionType.PLACE_FEATURE;
    }

    @Override
    public SpellRunner.ProcessStatus run(SpellRunner spellRunner) {
        return spellRunner.executeForAnyBlock((level, blockPos, _) -> level.holderLookup(Registries.CONFIGURED_FEATURE).get(feature)
                .map(Holder.Reference::value)
                .map(feature1 -> {
                    return feature1.place(level, level.getChunkSource().getGenerator(), level.getRandom(), blockPos);
                })
                .orElse(false));
    }
}
