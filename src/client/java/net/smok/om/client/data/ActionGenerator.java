package net.smok.om.client.data;

import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricDynamicRegistryProvider;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Vec3i;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.data.worldgen.features.VegetationFeatures;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.levelgen.blockpredicates.BlockPredicate;
import net.smok.om.OMTags;
import net.smok.om.entity.ProjectileProps;
import net.smok.om.spell.actions.SpellAction;
import net.smok.om.spell.actions.SpellActionType;
import net.smok.om.spell.actions.SpellActions;
import net.smok.om.spell.actions.StaticActionType;
import net.smok.om.spell.actions.block.*;
import net.smok.om.spell.actions.entity.*;
import net.smok.om.spell.actions.logic.ListAction;
import net.smok.om.spell.actions.logic.RandomSelectAction;
import net.smok.om.spell.actions.logic.WaitAction;
import net.smok.om.spell.actions.misc.ParticleAction;
import net.smok.om.spell.actions.misc.SoundAction;
import net.smok.om.utils.*;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.UnaryOperator;

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
                DamageEntityAction.damage(3, "hit").inline(),
                KnockbackEntity.defaultKnockback("hit").inline(),
                ParticleAction.builder("hit", ParticleTypes.CRIT, builder -> builder.setMovement(BlockParticleTemplate.MoveRule.FROM_CENTER, 0.2, 0.1).setCount(20)).inline(),
                SoundAction.create(Identifier.withDefaultNamespace("entity.firework_rocket.twinkle"), "caster", builder -> builder.setVolume(0.8f).setPitch(2f)).inline()
        ));

        BlockPredicate filter2 = BlockPredicate.anyOf(
                BlockPredicate.matchesBlocks(Blocks.GRASS_BLOCK),
                BlockPredicate.matchesBlocks(Blocks.STONE),
                BlockPredicate.matchesBlocks(Blocks.GRAVEL),
                BlockPredicate.matchesBlocks(Blocks.SAND)
        );
        Holder.Reference<SpellAction> basicBroken = add(SpellActions.BASIC_BROKEN, randomSelect(
                damageMagic(2),
                damageMagic(2),
                damageMagic(2),
                KnockbackEntity.knockback(2, false).inline(),
                KnockbackEntity.knockback(2, false).inline(),
                KnockbackEntity.knockback(2, false).inline(),
                chain(
                        ShapeBlockTarget.sphere(builder -> builder.range(2).filterSurface()).inline(),
                        list(
                                BlockPlaceAction.place(Blocks.BROWN_MUSHROOM.defaultBlockState(),
                                        BlockPredicate.allOf(
                                                new RandomBlockPredicate(0.3f),
                                                BlockPredicate.matchesTag(BlockTags.AIR),
                                                BlockPredicate.not(BlockPredicate.matchesTag(new Vec3i(0, -1, 0), BlockTags.AIR)
                                                )
                                        )
                                ).inline(),
                                BlockPlaceAction.place(Blocks.RED_MUSHROOM.defaultBlockState(),
                                        BlockPredicate.allOf(
                                                new RandomBlockPredicate(0.3f),
                                                BlockPredicate.matchesTag(BlockTags.AIR),
                                                BlockPredicate.not(BlockPredicate.matchesTag(new Vec3i(0, -1, 0), BlockTags.AIR)
                                                )
                                        )
                                ).inline()
                        )
                ),
                chain(
                        ShapeBlockTarget.sphere(builder5 -> builder5.range(2).filter(filter2)).inline(),
                        forEachBlock(1, 1,
                                list(
                                        randomSelect(
                                                BlockTransform.transform(Blocks.WARPED_NYLIUM, false, BlockPredicate.matchesBlocks(Blocks.GRASS_BLOCK)).inline(),
                                                BlockTransform.transform(Blocks.CRIMSON_NYLIUM, false, BlockPredicate.matchesBlocks(Blocks.GRASS_BLOCK)).inline()
                                        ),
                                        BlockTransform.transform(Blocks.NETHERRACK, false, BlockPredicate.matchesBlocks(Blocks.STONE)).inline(),
                                        BlockTransform.transform(Blocks.SOUL_SAND, false, BlockPredicate.matchesBlocks(Blocks.SAND)).inline(),
                                        BlockTransform.transform(Blocks.SOUL_SOIL, false, BlockPredicate.matchesBlocks(Blocks.GRAVEL)).inline()
                                )
                        )
                )
        ));

        add(SpellActions.FIRE_LOCK, randomSelect(
                BlockPlaceAction.place(Blocks.FIRE.defaultBlockState(), BlockPredicate.ONLY_IN_AIR_PREDICATE).inline(),
                BlockPlaceAction.place(Blocks.FIRE.defaultBlockState(), BlockPredicate.ONLY_IN_AIR_PREDICATE).inline(),
                BlockPlaceAction.place(Blocks.FIRE.defaultBlockState(), BlockPredicate.ONLY_IN_AIR_PREDICATE).inline(),
                FireControlAction.fire(10, "caster").inline(),
                FireControlAction.fire(10, "caster").inline(),
                FireControlAction.fire(10, "caster").inline(),
                chain(
                        ShapeBlockTarget.cube(builder -> builder.range(2)
                                .filter(BlockPredicate.allOf(BlockPredicate.solid(new Vec3i(0, -1, 0)), BlockPredicate.ONLY_IN_AIR_PREDICATE))
                                .shuffle(true)).inline(),
                        ForEachBlockTarget.target(BlockPlaceAction.place(Blocks.FIRE.defaultBlockState(), new RandomBlockPredicate(0.5f)).inline(), 5, 1).inline()
                )
        ));

        Holder<SpellAction> fireSphere = add(SpellActions.FIRE_SPHERE, firstEnter(
                chain(
                        TargetHitBlock.hit(BlockPredicate.matchesTag(BlockTags.CAMPFIRES)).inline(),
                        transformBlockState(BlockStateTransform.builder().set(CampfireBlock.LIT, true))
                ),
                list(
                        chain(
                                ShapeBlockTarget.sphere(builder4 -> builder4.range(3).filter(BlockPredicate.allOf(
                                        BlockPredicate.matchesTag(new Vec3i(0, -1, 0), BlockTags.PLANKS),
                                        new RandomBlockPredicate(0.5f)
                                ))).inline(),
                                forEachBlock(2, 4, BlockPlaceAction.placeOnAir(Blocks.FIRE.defaultBlockState()).inline())
                        ),
                        chain(
                                targetNear(3, FLAMEABLE_ENTITIES),
                                damageMagic(2),
                                FireControlAction.fire(20).inline()
                        )
                )
        ));

        Holder<SpellAction> fireRingBlockActions = ForEachBlockTarget.target(list(
                transformBlockState(BlockStateTransform.builder().set(CampfireBlock.LIT, true).setFilter(BlockPredicate.matchesTag(BlockTags.CAMPFIRES))),
                ParticleAction.builder("block", ParticleTypes.FLAME, builder -> builder.setCount(5).setMovement(BlockParticleTemplate.MoveRule.FROM_CENTER, 0.1, 0.1)).inline()
        )).inline();

        Holder<SpellAction> fireRingEntityAction = ForEachEntityTarget.target(list(
                damageMagic(2),
                FireControlAction.fire(60).inline()
        )).inline();

        add(SpellActions.FIRE_RING, list(
                SoundAction.create(Identifier.withDefaultNamespace("entity.blaze.shoot"), "actor", builder -> builder.setVolume(0.3f).setPitch(0.9f)).inline(),
                ShapeBlockTarget.sphere(builder -> builder.range(1).setMode(SelectionMode.REPLACE_WITHOUT_PREVIOUS)).inline(),
                ShapeBlockTarget.sphere(builder -> builder.range(2).setMode(SelectionMode.REPLACE_WITHOUT_PREVIOUS)).inline(),
                fireRingBlockActions,
                NearbyEntitiesTarget.builder(builder -> builder.shape(Shape.SPHERE).filter(getByTag(OMTags.FLAMEABLE_ENTITIES)).range(2).setMode(SelectionMode.REPLACE_WITHOUT_PREVIOUS)).inline(),
                fireRingEntityAction,
                new WaitAction(4).inline(),
                SoundAction.create(Identifier.withDefaultNamespace("entity.blaze.shoot"), "actor", builder -> builder.setVolume(0.4f).setPitch(0.8f)).inline(),
                ShapeBlockTarget.sphere(builder -> builder.range(3).setMode(SelectionMode.REPLACE_WITHOUT_PREVIOUS)).inline(),
                fireRingEntityAction,
                NearbyEntitiesTarget.builder(builder -> builder.shape(Shape.SPHERE).filter(getByTag(OMTags.FLAMEABLE_ENTITIES)).range(3).setMode(SelectionMode.REPLACE_WITHOUT_PREVIOUS)).inline(),
                fireRingBlockActions,
                new WaitAction(4).inline(),
                SoundAction.create(Identifier.withDefaultNamespace("entity.blaze.shoot"), "actor", builder -> builder.setVolume(0.6f).setPitch(0.7f)).inline(),
                ShapeBlockTarget.sphere(builder -> builder.range(4).setMode(SelectionMode.REPLACE_WITHOUT_PREVIOUS)).inline(),
                fireRingEntityAction,
                NearbyEntitiesTarget.builder(builder -> builder.shape(Shape.SPHERE).filter(getByTag(OMTags.FLAMEABLE_ENTITIES)).range(4).setMode(SelectionMode.REPLACE_WITHOUT_PREVIOUS)).inline(),
                fireRingBlockActions
        ));

        Holder<SpellAction> iceSphereBlockActions = ForEachBlockTarget.target(chain(
                firstEnter(
                        transformBlock(Blocks.FROSTED_ICE, false, BlockPredicate.allOf(BlockPredicate.matchesBlocks(Blocks.WATER), BlockPredicate.matchesBlocks(new Vec3i(0, 1, 0), Blocks.AIR))),
                        BreakAction.breakBlock(BlockPredicate.matchesBlocks(Blocks.TORCH), true).inline(),
                        BreakAction.breakBlock(BlockPredicate.matchesBlocks(Blocks.WALL_TORCH), true).inline(),
                        BreakAction.breakBlock(BlockPredicate.matchesBlocks(Blocks.COPPER_TORCH), true).inline(),
                        BreakAction.breakBlock(BlockPredicate.matchesBlocks(Blocks.COPPER_WALL_TORCH), true).inline(),
                        BreakAction.breakBlock(BlockPredicate.matchesBlocks(Blocks.FIRE), false).inline(),
                        transformBlockState(BlockStateTransform.builder().set(CampfireBlock.LIT, false))
                ),
                ParticleAction.builder("block", ParticleTypes.SNOWFLAKE, builder4 -> builder4.setOutline(0).setCount(50)).inline(),
                SoundAction.create(Identifier.withDefaultNamespace("entity.creaking.freeze"), "actor", UnaryOperator.identity()).inline()
        )).inline();

        Holder<SpellAction> iceSphereEntityActions = ForEachEntityTarget.target(list(
                damageMagic(4),
                FireControlAction.extinguish().inline()
        )).inline();
        Holder<SpellAction> iceSphere = add(SpellActions.ICE_SPHERE, list(
                ShapeBlockTarget.sphere(builder3 -> builder3.range(1).setMode(SelectionMode.REPLACE_WITHOUT_PREVIOUS)).inline(),
                iceSphereBlockActions,
                NearbyEntitiesTarget.builder(builder -> builder.shape(Shape.SPHERE).filter(getByTag(OMTags.FLAMEABLE_ENTITIES)).range(1).setMode(SelectionMode.REPLACE_WITHOUT_PREVIOUS)).inline(),
                iceSphereEntityActions,
                new WaitAction(4).inline(),
                ShapeBlockTarget.sphere(builder3 -> builder3.range(2).setMode(SelectionMode.REPLACE_WITHOUT_PREVIOUS)).inline(),
                iceSphereBlockActions,
                NearbyEntitiesTarget.builder(builder -> builder.shape(Shape.SPHERE).filter(getByTag(OMTags.FLAMEABLE_ENTITIES)).range(2).setMode(SelectionMode.REPLACE_WITHOUT_PREVIOUS)).inline(),
                iceSphereEntityActions,
                new WaitAction(4).inline(),
                ShapeBlockTarget.sphere(builder3 -> builder3.range(3).setMode(SelectionMode.REPLACE_WITHOUT_PREVIOUS)).inline(),
                iceSphereBlockActions,
                NearbyEntitiesTarget.builder(builder -> builder.shape(Shape.SPHERE).filter(getByTag(OMTags.FLAMEABLE_ENTITIES)).range(3).setMode(SelectionMode.REPLACE_WITHOUT_PREVIOUS)).inline(),
                iceSphereEntityActions
        ));

        Holder.Reference<SpellAction> iceSphereInWater = add(SpellActions.ICE_SPHERE_IN_WATER, chain(
                ShapeBlockTarget.cube(builder -> builder.range(1).filter(BlockPredicate.matchesBlocks(Blocks.WATER))).inline(),
                iceSphere,
                StaticActionType.DISCARD_PROJECTILE.action().inline()
        ));

        Holder<SpellAction> growPlants = add(SpellActions.GROW_PLANTS, firstEnter(
                new BonemealAction("hit", BlockPredicate.alwaysTrue()).inline(),
                chain(
                        ShapeBlockTarget.sphere(builder3 -> builder3.range(3).filter(BlockPredicate.matchesTag(OMTags.GROWABLE_PLANTS))).inline(),
                        Holder.direct(BonemealAction.INSTANCE))
        ));

        Holder<SpellAction> servingPlants = add(SpellActions.SERVING_PLANTS, chain(
                ShapeBlockTarget.sphere(builder2 -> builder2.range(3)).inline(),
                forEachBlock(0, 0, BreakAction.breakBlock(BlockPredicate.matchesTag(SEVERING_PLANTS), true).inline()),
                SpellActionType.DISCARD_PROJECTILE.action().inline()
        ));

        Holder.Reference<SpellAction> pull = add(SpellActions.PULL, MoveEntityToPosition.move(1, false, "entity", "caster").inline()); // new MoveEntityAction(MoveEntityAction.Direction.PULL_TO_CASTER, 1, false).inline());
        Holder.Reference<SpellAction> push = add(SpellActions.PUSH, MoveEntityToPosition.move(-1, false, "entity", "caster").inline()); // new MoveEntityAction(MoveEntityAction.Direction.PUSH_FROM_CASTER, 1, false).inline());

        Holder.Reference<SpellAction> puddle = add(SpellActions.PUDDLE, chain(
                ShapeBlockTarget.sphere(builder1 -> builder1.range(2).filterSurface()).inline(),
                forEachBlock(0, 0, chain(
                        firstEnter(
                                transformBlock(Blocks.MUD, false, BlockPredicate.matchesTag(BlockTags.CONVERTABLE_TO_MUD)),
                                transformBlockState(BlockStateTransform.builder().set(FarmlandBlock.MOISTURE, FarmlandBlock.MAX_MOISTURE)),
                                transformBlock(Blocks.CLAY, false, BlockPredicate.matchesTag(BlockTags.SAND)),
                                transformBlock(Blocks.WET_SPONGE, false, BlockPredicate.matchesBlocks(Blocks.SPONGE))
                        ),
                        ParticleAction.builder("block", ParticleTypes.DRIPPING_WATER, builder -> builder.setOutline(0).setCount(50)).inline()
                ))
        ));

        Holder.Reference<SpellAction> flowerField = add(SpellActions.FLOWER_FIELD, chain(
                ShapeBlockTarget.sphere(builder1 -> builder1.range(3).filter(BlockPredicate.allOf(new RandomBlockPredicate(0.33f), BlockPredicate.matchesBlocks(Blocks.AIR), BlockPredicate.matchesTag(new Vec3i(0, -1, 0), BlockTags.DIRT)))).inline(),
                forEachBlock(4, 1, chain(
                        randomSelect(
                                new FeatureAction(VegetationFeatures.FLOWER_DEFAULT).inline(),
                                new FeatureAction(VegetationFeatures.FLOWER_CHERRY).inline(),
                                new FeatureAction(VegetationFeatures.FLOWER_FLOWER_FOREST).inline(),
                                new FeatureAction(VegetationFeatures.FLOWER_MEADOW).inline()
                        ),
                        ParticleAction.builder("block", ParticleTypes.COMPOSTER,
                                builder -> builder.setCount(10).setMovement(BlockParticleTemplate.MoveRule.TO_CENTER, 0.1, 0.1)).inline()
                ))
        ));

        add(SpellActions.INVISIBILITY, list(
                new MobEffectAction(MobEffects.INVISIBILITY, 15*20, 0, false, "caster", "caster").inline()
        ));

        /* // todo надо заменить на готовые массивы стекла, шерсти и тд.
        ArrayList<Holder<SpellAction>> changeColorTagList = new ArrayList<>();
        changeColorFillTag(BlockTags.WOOL, changeColorTagList);
        changeColorFillTag(BlockTags.CONCRETE_POWDER, changeColorTagList);
        changeColorFillTag(BlockTags.TERRACOTTA, changeColorTagList);

        Holder<SpellAction> changeColor = add("change_color", new FirstEnterAction(changeColorTagList).inline());*/

        //add("invisible", new EffectAction(Potions.INVISIBILITY, 1))

        add(SpellActions.COLLECT_ITEMS, chain(
                NearbyEntitiesTarget.builder(builder -> builder.range(7).filter(EntityType.ITEM).shape(Shape.SPHERE)).inline(),
                MoveEntityToPosition.move(2, true, "entity", "caster").inline()
        ));
        add(SpellActions.PROJECTILE_BASIC, list(
                projectileHit(propsEmpty().setParticleOptions(ParticleTypes.ELECTRIC_SPARK).build(), null, basic),
                SoundAction.create(Identifier.withDefaultNamespace("block.copper_bulb.hit"), "caster", builder -> builder.setVolume(0.8f).setPitch(1.5f)).inline()
        ));
        add(SpellActions.PROJECTILE_BASIC_BROKEN, projectileHit(propsEmpty().setParticleOptions(ParticleTypes.ASH).build(), basicBroken, basicBroken));
        add(SpellActions.PROJECTILE_FIRE_SPHERE, projectileHit(propsWithItem(Items.FIRE_CHARGE).build(), fireSphere, fireSphere));
        add(SpellActions.PROJECTILE_ICE_SPHERE, projectile(propsWithItem(Items.ICE).setParticleOptions(ParticleTypes.BUBBLE).build(), iceSphereInWater, iceSphere, iceSphere, 2));
        add(SpellActions.PROJECTILE_GROW_PLANTS, projectileHit(propsEmpty().setParticleOptions(ParticleTypes.COMPOSTER).build(), growPlants, null));
        add(SpellActions.PROJECTILE_SERVING_PLANTS, projectileTick(propsEmpty().setParticleOptions(ParticleTypes.COMPOSTER).setDiscardOnHitEntity(false).build(), servingPlants, 4));
        add(SpellActions.PROJECTILE_PULL, projectileHit(propsEmpty().setParticleOptions(ParticleTypes.COMPOSTER).build(), null, pull));
        add(SpellActions.PROJECTILE_PUSH, projectileHit(propsEmpty().setParticleOptions(ParticleTypes.COMPOSTER).build(), null, push));
        add(SpellActions.PROJECTILE_PUDDLE, projectileHit(propsEmpty().setParticleOptions(ParticleTypes.DRIPPING_WATER).setDiscardOnHitEntity(false).build(), puddle, null));
        add(SpellActions.PROJECTILE_FLOWER_FIELD, projectileHit(propsEmpty().setParticleOptions(ParticleTypes.COMPOSTER).setDiscardOnHitEntity(false).build(), flowerField, null));
        //add("projectile_change_color", projectileHit(propsEmpty().setDiscardOnHitEntity(false).build(), changeColor, null));

    }

    private void changeColorFillTag(TagKey<Block> tag, List<Holder<SpellAction>> collector, Block[] blocks) {
        List<Holder<SpellAction>> transformations = new ArrayList<>();
        for (Block block : blocks) {
            //transformations.add(transform(new TagMatchTest(tag), block.defaultBlockState()));
        }

        collector.add(chain(
                ShapeBlockTarget.sphere(builder -> builder.range(2).filter(BlockPredicate.matchesTag(tag))).inline(),
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
        return ListAction.chain(actions).inline();
    }

    @SafeVarargs
    public static Holder<SpellAction> firstEnter(Holder<SpellAction>... actions) {
        return ListAction.firstEnter(actions).inline();
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

    public static Holder<SpellAction> forEachBlock(int delay, int combine, Holder<SpellAction> action) {
        return ForEachBlockTarget.target(action, delay, combine).inline();
    }

    public Holder<SpellAction> targetNear(float range, TagKey<EntityType<?>> entityTypes) {
        return NearbyEntitiesTarget.builder(builder -> builder.filter(getByTag(entityTypes)).range(range)).inline();
    }

    private <T> @NotNull HolderSet.Named<T> getByTag(@NotNull TagKey<T> entityTypes) {
        return context.lookup(entityTypes.registry()).getOrThrow(entityTypes);
    }


    public static @NotNull Holder<SpellAction> damageMagic(int damageAmount) {
        return DamageEntityAction.damage(damageAmount).inline();
    }

    public static @NotNull Holder<SpellAction> transformBlock(Block block, boolean keepProperties, @Nullable BlockPredicate filter) {
        return BlockTransform.transform(block, keepProperties, Objects.requireNonNullElse(filter, BlockPredicate.alwaysTrue())).inline();
    }

    public static @NotNull Holder<SpellAction> transformBlockState(BlockStateTransform.Builder blockStateBuilder) {
        return blockStateBuilder.build().inline();
    }
}
