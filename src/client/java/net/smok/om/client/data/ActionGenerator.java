package net.smok.om.client.data;

import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricDynamicRegistryProvider;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Vec3i;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.data.worldgen.features.VegetationFeatures;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.blockpredicates.BlockPredicate;
import net.minecraft.world.level.levelgen.structure.templatesystem.*;
import net.smok.om.entity.ProjectileProps;
import net.smok.om.spell.actions.SpellAction;
import net.smok.om.spell.actions.SpellActions;
import net.smok.om.spell.actions.block.*;
import net.smok.om.spell.actions.entity.*;
import net.smok.om.spell.actions.logic.ChainListAction;
import net.smok.om.spell.actions.logic.FirstEnterAction;
import net.smok.om.spell.actions.logic.ListAction;
import net.smok.om.spell.actions.logic.RandomSelectAction;
import net.smok.om.utils.Shape;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.CompletableFuture;

import static net.smok.om.OMTags.*;


public class ActionGenerator extends FabricDynamicRegistryProvider {

    private static final Block[] TERRACOTTA = new Block[] {
            Blocks.WHITE_TERRACOTTA,
            Blocks.LIGHT_GRAY_TERRACOTTA,
            Blocks.GRAY_TERRACOTTA,
            Blocks.BLACK_TERRACOTTA,
            Blocks.RED_TERRACOTTA,
            Blocks.ORANGE_TERRACOTTA,
            Blocks.YELLOW_TERRACOTTA,
            Blocks.BROWN_TERRACOTTA,
            Blocks.GREEN_TERRACOTTA,
            Blocks.LIME_TERRACOTTA,
            Blocks.CYAN_TERRACOTTA,
            Blocks.BLUE_TERRACOTTA,
            Blocks.LIGHT_BLUE_TERRACOTTA,
            Blocks.MAGENTA_TERRACOTTA,
            Blocks.PURPLE_TERRACOTTA,
            Blocks.PINK_TERRACOTTA
    };
    
    private static final Block[] GLAZED_TERRACOTTA = new Block[] {
            Blocks.WHITE_GLAZED_TERRACOTTA,
            Blocks.LIGHT_GRAY_GLAZED_TERRACOTTA,
            Blocks.GRAY_GLAZED_TERRACOTTA,
            Blocks.BLACK_GLAZED_TERRACOTTA,
            Blocks.RED_GLAZED_TERRACOTTA,
            Blocks.ORANGE_GLAZED_TERRACOTTA,
            Blocks.YELLOW_GLAZED_TERRACOTTA,
            Blocks.BROWN_GLAZED_TERRACOTTA,
            Blocks.GREEN_GLAZED_TERRACOTTA,
            Blocks.LIME_GLAZED_TERRACOTTA,
            Blocks.CYAN_GLAZED_TERRACOTTA,
            Blocks.BLUE_GLAZED_TERRACOTTA,
            Blocks.LIGHT_BLUE_GLAZED_TERRACOTTA,
            Blocks.MAGENTA_GLAZED_TERRACOTTA,
            Blocks.PURPLE_GLAZED_TERRACOTTA,
            Blocks.PINK_GLAZED_TERRACOTTA
    };
    
    private static final Block[] WOOLS = new Block[] {
            Blocks.WHITE_WOOL,
            Blocks.LIGHT_GRAY_WOOL,
            Blocks.GRAY_WOOL,
            Blocks.BLACK_WOOL,
            Blocks.RED_WOOL,
            Blocks.ORANGE_WOOL,
            Blocks.YELLOW_WOOL,
            Blocks.BROWN_WOOL,
            Blocks.GREEN_WOOL,
            Blocks.LIME_WOOL,
            Blocks.CYAN_WOOL,
            Blocks.BLUE_WOOL,
            Blocks.LIGHT_BLUE_WOOL,
            Blocks.MAGENTA_WOOL,
            Blocks.PURPLE_WOOL,
            Blocks.PINK_WOOL
    };
    
    private static final Block[] GLASSES = new Block[] {
            Blocks.WHITE_STAINED_GLASS,
            Blocks.LIGHT_GRAY_STAINED_GLASS,
            Blocks.GRAY_STAINED_GLASS,
            Blocks.BLACK_STAINED_GLASS,
            Blocks.RED_STAINED_GLASS,
            Blocks.ORANGE_STAINED_GLASS,
            Blocks.YELLOW_STAINED_GLASS,
            Blocks.BROWN_STAINED_GLASS,
            Blocks.GREEN_STAINED_GLASS,
            Blocks.LIME_STAINED_GLASS,
            Blocks.CYAN_STAINED_GLASS,
            Blocks.BLUE_STAINED_GLASS,
            Blocks.LIGHT_BLUE_STAINED_GLASS,
            Blocks.MAGENTA_STAINED_GLASS,
            Blocks.PURPLE_STAINED_GLASS,
            Blocks.PINK_STAINED_GLASS
    };
    
    private static final Block[] GLASS_PANES = new Block[] {
            Blocks.WHITE_STAINED_GLASS_PANE,
            Blocks.LIGHT_GRAY_STAINED_GLASS_PANE,
            Blocks.GRAY_STAINED_GLASS_PANE,
            Blocks.BLACK_STAINED_GLASS_PANE,
            Blocks.RED_STAINED_GLASS_PANE,
            Blocks.ORANGE_STAINED_GLASS_PANE,
            Blocks.YELLOW_STAINED_GLASS_PANE,
            Blocks.BROWN_STAINED_GLASS_PANE,
            Blocks.GREEN_STAINED_GLASS_PANE,
            Blocks.LIME_STAINED_GLASS_PANE,
            Blocks.CYAN_STAINED_GLASS_PANE,
            Blocks.BLUE_STAINED_GLASS_PANE,
            Blocks.LIGHT_BLUE_STAINED_GLASS_PANE,
            Blocks.MAGENTA_STAINED_GLASS_PANE,
            Blocks.PURPLE_STAINED_GLASS_PANE,
            Blocks.PINK_STAINED_GLASS_PANE
    };

    private final List<Holder.Reference<SpellAction>> values = new ArrayList<>();



    public ActionGenerator(FabricPackOutput output, CompletableFuture<HolderLookup.Provider> registriesFuture) {
        super(output, registriesFuture);
    }


    private BootstrapContext<SpellAction> context;

    @Override
    protected void configure(HolderLookup.Provider provider, Entries entries) {
        values.forEach(entries::add);
    }

    public void bootStrap(BootstrapContext<SpellAction> context) {
        this.context = context;
        addEntries();
        this.context = null;
    }

    private void addEntries() {


        Holder<SpellAction> basic = add(SpellActions.BASIC, list(
                damageMagic(3),
                Holder.direct(new KnockbackEntity(1, false))
        ));

        Holder.Reference<SpellAction> basicBroken = add(SpellActions.BASIC_BROKEN, randomSelect(
                damageMagic(2),
                damageMagic(2),
                damageMagic(2),
                new KnockbackEntity(2, false).inline(),
                new KnockbackEntity(2, false).inline(),
                new KnockbackEntity(2, false).inline(),
                chain(
                        shapeSphere(2, true, false),
                        list(
                                transform(new RandomBlockMatchTest(Blocks.AIR, 0.3f), Blocks.BROWN_MUSHROOM.defaultBlockState()),
                                transform(new RandomBlockMatchTest(Blocks.AIR, 0.3f), Blocks.RED_MUSHROOM.defaultBlockState())
                        )
                ),
                chain(
                        shapeSphere(2, true, true),
                        transform(
                                new RandomBlockMatchTest(Blocks.GRASS_BLOCK, 0.3f), Blocks.WARPED_NYLIUM.defaultBlockState(),
                                new RandomBlockMatchTest(Blocks.GRASS_BLOCK, 0.3f), Blocks.CRIMSON_NYLIUM.defaultBlockState(),
                                new RandomBlockMatchTest(Blocks.STONE, 0.3f), Blocks.NETHERRACK.defaultBlockState(),
                                new RandomBlockMatchTest(Blocks.SAND, 0.3f), Blocks.SOUL_SAND.defaultBlockState(),
                                new RandomBlockMatchTest(Blocks.GRAVEL, 0.3f), Blocks.SOUL_SOIL.defaultBlockState()
                        )
                )
        ));

        Holder<SpellAction> fireSphere = add(SpellActions.FIRE_SPHERE, firstEnter(
                chain(
                        targetHitBlock(BlockPredicate.matchesTag(BlockTags.CAMPFIRES)),
                        transformBlockState(BlockStateTransform.builder().set(CampfireBlock.LIT, true))
                ),
                list(
                        chain(
                                shapeSphere(3, false, false, BlockPredicate.allOf(
                                        BlockPredicate.matchesTag(new Vec3i(0, -1, 0), BlockTags.PLANKS),
                                        new RandomBlockPredicate(0.5f)
                                )),
                                forEachBlock(2, 4, placeBlock(Blocks.FIRE.defaultBlockState(), BlockPredicate.matchesBlocks(Blocks.AIR)))
                        ),
                        chain(
                                targetNear(3, FLAMEABLE_ENTITIES),
                                damageMagic(2),
                                fireControl(20)
                        )
                )
        ));

        Holder<SpellAction> iceSphere = add(SpellActions.ICE_SPHERE, list(
                list(
                        shapeSphere(3, true, false),
                        transformBlock(Blocks.FROSTED_ICE, false, BlockPredicate.matchesBlocks(Blocks.WATER)),
                        new BreakAction(new BlockMatchTest(Blocks.TORCH), true).inline(),
                        new BreakAction(new BlockMatchTest(Blocks.WALL_TORCH), true).inline(),
                        new BreakAction(new BlockMatchTest(Blocks.COPPER_TORCH), true).inline(),
                        new BreakAction(new BlockMatchTest(Blocks.COPPER_WALL_TORCH), true).inline(),
                        new BreakAction(new BlockMatchTest(Blocks.FIRE), false).inline(),
                        transformBlockState(BlockStateTransform.builder().set(CampfireBlock.LIT, false))
                ),
                chain(
                        targetNear(3, FLAMEABLE_ENTITIES),
                        damageMagic(4),
                        fireControl(-1)
                )
        ));

        Holder<SpellAction> growPlants = add(SpellActions.GROW_PLANTS, firstEnter(
                Holder.direct(BonemealAction.INSTANCE),
                chain(
                        shapeSphere(3, false, false, BlockPredicate.matchesTag(GROWABLE_PLANTS)),
                        Holder.direct(BonemealAction.INSTANCE))
        ));

        Holder<SpellAction> servingPlants = add(SpellActions.SERVING_PLANTS, chain(
                shapeSphere(3, false, false),
                Holder.direct(new BreakAction(new TagMatchTest(SEVERING_PLANTS), true))
        ));

        Holder.Reference<SpellAction> pull = add(SpellActions.PULL, new MoveEntityAction(MoveEntityAction.Direction.PULL_TO_CASTER, 1, false).inline());
        Holder.Reference<SpellAction> push = add(SpellActions.PUSH, new MoveEntityAction(MoveEntityAction.Direction.PUSH_FROM_CASTER, 1, false).inline());

        Holder.Reference<SpellAction> puddle = add(SpellActions.PUDDLE, chain(
                shapeSphere(1, true, false),
                forEachBlock(0, 0, list(
                        transformBlock(Blocks.MUD, false, BlockPredicate.matchesTag(BlockTags.CONVERTABLE_TO_MUD)),
                        transformBlockState(BlockStateTransform.builder().set(FarmlandBlock.MOISTURE, FarmlandBlock.MAX_MOISTURE)),
                        transformBlock(Blocks.CLAY, false, BlockPredicate.matchesTag(BlockTags.SAND)),
                        transformBlock(Blocks.WET_SPONGE, false, BlockPredicate.matchesBlocks(Blocks.SPONGE))
                ))
        ));

        Holder.Reference<SpellAction> flowerField = add(SpellActions.FLOWER_FIELD, chain(
                shapeSphere(3, false, false, BlockPredicate.allOf(new RandomBlockPredicate(0.33f), BlockPredicate.matchesBlocks(Blocks.AIR))),
                randomSelect(
                        new FeatureAction(VegetationFeatures.FLOWER_DEFAULT).inline(),
                        new FeatureAction(VegetationFeatures.FLOWER_CHERRY).inline(),
                        new FeatureAction(VegetationFeatures.FLOWER_FLOWER_FOREST).inline(),
                        new FeatureAction(VegetationFeatures.FLOWER_MEADOW).inline()
                )
        ));

        /* // todo надо заменить на готовые массивы стекла, шерсти и тд.
        ArrayList<Holder<SpellAction>> changeColorTagList = new ArrayList<>();
        changeColorFillTag(BlockTags.WOOL, changeColorTagList);
        changeColorFillTag(BlockTags.CONCRETE_POWDER, changeColorTagList);
        changeColorFillTag(BlockTags.TERRACOTTA, changeColorTagList);

        Holder<SpellAction> changeColor = add("change_color", new FirstEnterAction(changeColorTagList).inline());*/

        //add("invisible", new EffectAction(Potions.INVISIBILITY, 1))

        add(SpellActions.COLLECT_ITEMS, chain(
                Holder.direct(new NearbyEntitiesTarget(7, EntityType.ITEM, Shape.SPHERE)),
                Holder.direct(new MoveEntityAction(MoveEntityAction.Direction.PULL_TO_CASTER, 2, false))
        ));
        add(SpellActions.PROJECTILE_BASIC, projectileHit(propsEmpty().setParticleOptions(ParticleTypes.ELECTRIC_SPARK).build(), null, basic));
        add(SpellActions.PROJECTILE_BASIC_BROKEN, projectileHit(propsEmpty().setParticleOptions(ParticleTypes.ASH).build(), basicBroken, basicBroken));
        add(SpellActions.PROJECTILE_FIRE_SPHERE, projectileHit(propsWithItem(Items.FIRE_CHARGE).build(), fireSphere, fireSphere));
        add(SpellActions.PROJECTILE_ICE_SPHERE, projectileHit(propsWithItem(Items.ICE).setParticleOptions(ParticleTypes.BUBBLE).build(), iceSphere, iceSphere));
        add(SpellActions.PROJECTILE_GROW_PLANTS, projectileHit(propsEmpty().setParticleOptions(ParticleTypes.COMPOSTER).build(), growPlants, null));
        add(SpellActions.PROJECTILE_SERVING_PLANTS, projectileHit(propsEmpty().setParticleOptions(ParticleTypes.COMPOSTER).setDiscardOnHitEntity(false).build(), servingPlants, null));
        add(SpellActions.PROJECTILE_PULL, projectileHit(propsEmpty().setParticleOptions(ParticleTypes.COMPOSTER).build(), null, pull));
        add(SpellActions.PROJECTILE_PUSH, projectileHit(propsEmpty().setParticleOptions(ParticleTypes.COMPOSTER).build(), null, push));
        add(SpellActions.PROJECTILE_PUDDLE, projectileHit(propsEmpty().setParticleOptions(ParticleTypes.DRIPPING_WATER).setDiscardOnHitEntity(false).build(), puddle, null));
        add(SpellActions.PROJECTILE_FLOWER_FIELD, projectileHit(propsEmpty().setParticleOptions(ParticleTypes.COMPOSTER).setDiscardOnHitEntity(false).build(), flowerField, null));
        //add("projectile_change_color", projectileHit(propsEmpty().setDiscardOnHitEntity(false).build(), changeColor, null));

    }

    private void changeColorFillTag(TagKey<Block> tag, List<Holder<SpellAction>> collector, Block[] blocks) {
        List<Holder<SpellAction>> transformations = new ArrayList<>();
        for (Block block : blocks) {
            transformations.add(transform(new TagMatchTest(tag), block.defaultBlockState()));
        }

        collector.add(chain(
                shapeSphere(2, true, false, BlockPredicate.matchesTag(tag)),
                new RandomSelectAction(transformations).inline()
        ));
    }


    @Override
    public String getName() {
        return "Spell Action Generator";
    }



    private Holder.Reference<SpellAction> add(ResourceKey<SpellAction> key, Holder<SpellAction> action) {
        Holder.Reference<SpellAction> value = context.register(key, action.value());
        values.add(value);
        return value;
    }




    @Contract(" -> new")
    public static ProjectileProps.@NotNull Builder propsEmpty() {
        return ProjectileProps.Builder.empty();
    }

    @Contract("_ -> new")
    public static ProjectileProps.@NotNull Builder propsWithItem(Item item) {
        return ProjectileProps.Builder.withItem(item);
    }


    @SafeVarargs
    public static Holder<SpellAction> list(Holder<SpellAction>... actions) {
        return Holder.direct(new ListAction(actions));
    }

    @SafeVarargs
    public static Holder<SpellAction> chain(Holder<SpellAction>... actions) {
        return Holder.direct(new ChainListAction(actions));
    }

    @SafeVarargs
    public static Holder<SpellAction> firstEnter(Holder<SpellAction>... actions) {
        return Holder.direct(new FirstEnterAction(actions));
    }

    @SafeVarargs
    public static Holder<SpellAction> randomSelect(Holder<SpellAction>... actions) {
        return new RandomSelectAction(actions).inline();
    }



    public static Holder<SpellAction> projectileHit(ProjectileProps props, @Nullable Holder<SpellAction> onBlockHit, @Nullable Holder<SpellAction> onEntityHit) {
        return Holder.direct(new ProjectileSpawn(props, null, onBlockHit, onEntityHit, 1));
    }

    public static Holder<SpellAction> projectileTick(ProjectileProps props, @NotNull Holder<SpellAction> onTick, int delay) {
        return Holder.direct(new ProjectileSpawn(props, onTick, null, null, delay));
    }

    public static Holder<SpellAction> projectile(ProjectileProps props, @Nullable Holder<SpellAction> onTick, @Nullable Holder<SpellAction> onBlockHit, @org.jspecify.annotations.Nullable Holder<SpellAction> onEntityHit, int delay) {
        return Holder.direct(new ProjectileSpawn(props, onTick, onBlockHit, onEntityHit, delay));
    }

    public static Holder<SpellAction> transform(RuleTest ruleTest, BlockState blockState) {
        return Holder.direct(new BlockTransformation(new BlockTransformation.TransformEntry(ruleTest, blockState)));
    }

    public static Holder<SpellAction> placeBlock(BlockState blockState, BlockPredicate filter) {
        return new BlockPlaceAction(blockState, filter).inline();
    }

    public static Holder<SpellAction> transform(
            RuleTest ruleTest, BlockState blockState,
            RuleTest ruleTest1, BlockState blockState1,
            RuleTest ruleTest2, BlockState blockState2
    ) {
        return Holder.direct(new BlockTransformation(
                new BlockTransformation.TransformEntry(ruleTest, blockState),
                new BlockTransformation.TransformEntry(ruleTest1, blockState1),
                new BlockTransformation.TransformEntry(ruleTest2, blockState2)
        ));
    }

    public static Holder<SpellAction> transform(
            RuleTest ruleTest, BlockState blockState,
            RuleTest ruleTest1, BlockState blockState1,
            RuleTest ruleTest2, BlockState blockState2,
            RuleTest ruleTest3, BlockState blockState3
    ) {
        return Holder.direct(new BlockTransformation(
                new BlockTransformation.TransformEntry(ruleTest, blockState),
                new BlockTransformation.TransformEntry(ruleTest1, blockState1),
                new BlockTransformation.TransformEntry(ruleTest2, blockState2),
                new BlockTransformation.TransformEntry(ruleTest3, blockState3)
        ));
    }

    public static Holder<SpellAction> transform(
            RuleTest ruleTest, BlockState blockState,
            RuleTest ruleTest1, BlockState blockState1,
            RuleTest ruleTest2, BlockState blockState2,
            RuleTest ruleTest3, BlockState blockState3,
            RuleTest ruleTest4, BlockState blockState4
    ) {
        return Holder.direct(new BlockTransformation(
                new BlockTransformation.TransformEntry(ruleTest, blockState),
                new BlockTransformation.TransformEntry(ruleTest1, blockState1),
                new BlockTransformation.TransformEntry(ruleTest2, blockState2),
                new BlockTransformation.TransformEntry(ruleTest3, blockState3),
                new BlockTransformation.TransformEntry(ruleTest4, blockState4)
        ));
    }

    public static Holder<SpellAction> transform(
            RuleTest ruleTest, BlockState blockState,
            RuleTest ruleTest1, BlockState blockState1,
            RuleTest ruleTest2, BlockState blockState2,
            RuleTest ruleTest3, BlockState blockState3,
            RuleTest ruleTest4, BlockState blockState4,
            RuleTest ruleTest5, BlockState blockState5
    ) {
        return Holder.direct(new BlockTransformation(
                new BlockTransformation.TransformEntry(ruleTest, blockState),
                new BlockTransformation.TransformEntry(ruleTest1, blockState1),
                new BlockTransformation.TransformEntry(ruleTest2, blockState2),
                new BlockTransformation.TransformEntry(ruleTest3, blockState3),
                new BlockTransformation.TransformEntry(ruleTest4, blockState4),
                new BlockTransformation.TransformEntry(ruleTest5, blockState5)
        ));
    }

    public static Holder<SpellAction> shapeSphere(int range, boolean maskSurface, boolean maskSolid) {
        return Holder.direct(new ShapeBlockTarget(Shape.SPHERE, range, maskSurface, maskSolid));
    }

    public static Holder<SpellAction> cubeSphere(int range, boolean maskSurface, boolean maskSolid) {
        return Holder.direct(new ShapeBlockTarget(Shape.CUBE, range, maskSurface, maskSolid));
    }

    public static Holder<SpellAction> diamondSphere(int range, boolean maskSurface, boolean maskSolid) {
        return Holder.direct(new ShapeBlockTarget(Shape.DIAMOND, range, maskSurface, maskSolid));
    }

    public static Holder<SpellAction> shapeSphere(int range, boolean maskSurface, boolean maskSolid, BlockPredicate filter) {
        return Holder.direct(new ShapeBlockTarget(Shape.SPHERE, range, maskSurface, maskSolid, filter));
    }

    public static Holder<SpellAction> cubeSphere(int range, boolean maskSurface, boolean maskSolid, BlockPredicate filter) {
        return Holder.direct(new ShapeBlockTarget(Shape.CUBE, range, maskSurface, maskSolid, filter));
    }

    public static Holder<SpellAction> diamondSphere(int range, boolean maskSurface, boolean maskSolid, BlockPredicate filter) {
        return Holder.direct(new ShapeBlockTarget(Shape.DIAMOND, range, maskSurface, maskSolid, filter));
    }

    public static Holder<SpellAction> targetNear(float range, EntityType<?> entityType) {
        return new NearbyEntitiesTarget(range, entityType, Shape.SPHERE).inline();
    }

    public static Holder<SpellAction> targetNear(float range, EntityType<?>... entityTypes) {
        return new NearbyEntitiesTarget(range, Shape.SPHERE, entityTypes).inline();
    }

    public static Holder<SpellAction> targetNear(float range, HolderSet<EntityType<?>> entityTypes) {
        return new NearbyEntitiesTarget(range, entityTypes, Shape.SPHERE).inline();
    }

    public static Holder<SpellAction> forEachBlock(int delay, int combine, Holder<SpellAction> action) {
        return new ForEachBlockTarget(delay, combine, action).inline();
    }

    public Holder<SpellAction> targetNear(float range, TagKey<EntityType<?>> entityTypes) {
        return new NearbyEntitiesTarget(range, context.lookup(Registries.ENTITY_TYPE).getOrThrow(entityTypes), Shape.SPHERE).inline();
    }

    public static @NotNull Holder<SpellAction> damageMagic(int damageAmount) {
        return Holder.direct(new DamageEntityAction(DamageTypes.MAGIC, damageAmount));
    }

    public static @NotNull Holder<SpellAction> fireControl(int fireTicks) {
        return Holder.direct(new FireControlAction(fireTicks));
    }

    public static @NotNull Holder<SpellAction> targetHitBlock(@Nullable BlockPredicate filter) {
        if (filter == null) return TargetHitBlock.ANY.inline();
        return new TargetHitBlock(filter).inline();
    }

    public static @NotNull Holder<SpellAction> transformBlock(Block block, boolean keepProperties, @Nullable BlockPredicate filter) {
        return new BlockTransform(block, keepProperties, Objects.requireNonNullElse(filter, BlockPredicate.alwaysTrue())).inline();
    }

    public static @NotNull Holder<SpellAction> transformBlockState(BlockStateTransform.Builder blockStateBuilder) {
        return blockStateBuilder.build().inline();
    }
}
