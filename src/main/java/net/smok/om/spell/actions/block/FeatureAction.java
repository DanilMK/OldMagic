package net.smok.om.spell.actions.block;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.smok.om.spell.actions.SpellAction;
import net.smok.om.spell.actions.SpellActionType;
import net.smok.om.spell.SpellRunner;
import net.smok.om.utils.StoredValueType;

public record FeatureAction(ResourceKey<ConfiguredFeature<?, ?>> feature) implements SpellAction {

    public static final MapCodec<FeatureAction> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            ResourceKey.codec(Registries.CONFIGURED_FEATURE).fieldOf("feature").forGetter(FeatureAction::feature)
    ).apply(instance, FeatureAction::new));


    @Override
    public SpellActionType<?> actionType() {
        return SpellActionType.PLACE_FEATURE;
    }

    @Override
    public SpellRunner.ProcessStatus run(SpellRunner spellRunner) {
        return spellRunner.executeFor("block", StoredValueType.BLOCK, (level, blockPos) -> level.holderLookup(Registries.CONFIGURED_FEATURE).get(feature)
                .map(Holder.Reference::value)
                .map(feature1 -> feature1.place(level, level.getChunkSource().getGenerator(), level.getRandom(), blockPos))
                .orElse(false));
    }
}
