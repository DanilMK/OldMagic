package net.smok.om.client.data;

import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricDynamicRegistryProvider;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderSet;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.data.worldgen.features.VegetationFeatures;
import net.minecraft.data.worldgen.placement.VegetationPlacements;
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
import net.smok.om.Values;
import net.smok.om.spell.ProjectileProps;
import net.smok.om.spell.SpellAction;
import net.smok.om.spell.block.action.BlockTransformation;
import net.smok.om.spell.block.action.BonemealAction;
import net.smok.om.spell.block.action.BreakAction;
import net.smok.om.spell.block.action.FeatureAction;
import net.smok.om.spell.block.target.RandomBlockPredicate;
import net.smok.om.spell.block.target.ShapeBlockTarget;
import net.smok.om.spell.entity.action.*;
import net.smok.om.spell.entity.target.NearbyEntitiesTarget;
import net.smok.om.spell.logic.ChainListAction;
import net.smok.om.spell.logic.FirstEnterAction;
import net.smok.om.spell.logic.ListAction;
import net.smok.om.spell.logic.RandomSelectAction;
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


        Holder<SpellAction> basic = add("basic", list(
                damageMagic(3),
                Holder.direct(new MoveEntityAction(MoveEntityAction.Direction.KNOCKBACK, 1, false))
        ));

        Holder.Reference<SpellAction> basicBroken = add("basic_broken", randomSelect(
                damageMagic(2),
                damageMagic(2),
                damageMagic(2),
                new MoveEntityAction(MoveEntityAction.Direction.KNOCKBACK, 1, false).inline(),
                new MoveEntityAction(MoveEntityAction.Direction.KNOCKBACK, 1, false).inline(),
                new MoveEntityAction(MoveEntityAction.Direction.KNOCKBACK, 1, false).inline(),
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

        Holder<SpellAction> damage3 = add("damage_3", list(
                damageMagic(3)
        ));

        Holder<SpellAction> fireSphere = add("fire_sphere", list(
                chain(
                        shapeSphere(3, true, false),
                        transform(new RandomBlockMatchTest(Blocks.AIR, 0.2f), Blocks.FIRE.defaultBlockState())
                ),
                chain(
                        targetNear(3, FLAMEABLE_ENTITIES),
                        damageMagic(2),
                        fireControl(20)
                )
        ));

        Holder<SpellAction> iceSphere = add("ice_sphere", list(
                list(
                        shapeSphere(3, true, false),
                        transform(
                                new BlockMatchTest(Blocks.WATER), Blocks.FROSTED_ICE.defaultBlockState(),
                                new BlockMatchTest(Blocks.FIRE), Blocks.AIR.defaultBlockState()
                        ),
                        new BreakAction(new BlockMatchTest(Blocks.TORCH), true).inline(),
                        new BreakAction(new BlockMatchTest(Blocks.WALL_TORCH), true).inline(),
                        new BreakAction(new BlockMatchTest(Blocks.COPPER_TORCH), true).inline(),
                        new BreakAction(new BlockMatchTest(Blocks.COPPER_WALL_TORCH), true).inline()
                ),
                chain(
                        targetNear(3, FLAMEABLE_ENTITIES),
                        damageMagic(4),
                        fireControl(-1)
                )
        ));

        Holder<SpellAction> growPlants = add("grow_plants", firstEnter(
                Holder.direct(BonemealAction.INSTANCE),
                chain(
                        shapeSphere(3, false, false, BlockPredicate.matchesTag(GROWABLE_PLANTS)),
                        Holder.direct(BonemealAction.INSTANCE))
        ));

        Holder<SpellAction> servingPlants = add("serving_plants", chain(
                shapeSphere(3, false, false),
                Holder.direct(new BreakAction(new TagMatchTest(SEVERING_PLANTS), true))
        ));

        Holder.Reference<SpellAction> pull = add("pull", new MoveEntityAction(MoveEntityAction.Direction.PULL_TO_CASTER, 1, false).inline());
        Holder.Reference<SpellAction> push = add("push", new MoveEntityAction(MoveEntityAction.Direction.PUSH_FROM_CASTER, 1, false).inline());

        Holder.Reference<SpellAction> puddle = add("puddle", chain(
                shapeSphere(1, true, false),
                transform(
                        new TagMatchTest(BlockTags.CONVERTABLE_TO_MUD), Blocks.MUD.defaultBlockState(),
                        new BlockMatchTest(Blocks.FARMLAND), Blocks.FARMLAND.defaultBlockState().setValue(FarmlandBlock.MOISTURE, FarmlandBlock.MAX_MOISTURE),
                        new TagMatchTest(BlockTags.SAND), Blocks.CLAY.defaultBlockState(),
                        new BlockMatchTest(Blocks.SPONGE), Blocks.WET_SPONGE.defaultBlockState()
                )
        ));

        Holder.Reference<SpellAction> flowerField = add("flower_field", chain(
                shapeSphere(3, false, false, new RandomBlockPredicate(0.33f, BlockPredicate.matchesBlocks(Blocks.AIR))),
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

        add("collect_items", chain(
                Holder.direct(new NearbyEntitiesTarget(7, EntityType.ITEM)),
                Holder.direct(new MoveEntityAction(MoveEntityAction.Direction.PULL_TO_CASTER, 2, false))
        ));
        add("projectile_basic", projectileHit(propsEmpty().setParticleOptions(ParticleTypes.ELECTRIC_SPARK).build(), null, basic));
        add("projectile_basic_broken", projectileHit(propsEmpty().setParticleOptions(ParticleTypes.ASH).build(), basicBroken, basicBroken));
        add("projectile_fire_sphere", projectileHit(propsWithItem(Items.FIRE_CHARGE).build(), fireSphere, fireSphere));
        add("projectile_ice_sphere", projectileHit(propsWithItem(Items.ICE).setParticleOptions(ParticleTypes.BUBBLE).build(), iceSphere, iceSphere));
        add("projectile_grow_plants", projectileHit(propsEmpty().setParticleOptions(ParticleTypes.COMPOSTER).build(), growPlants, null));
        add("projectile_serving_plants", projectileHit(propsEmpty().setParticleOptions(ParticleTypes.COMPOSTER).setDiscardOnHitEntity(false).build(), servingPlants, null));
        add("projectile_pull", projectileHit(propsEmpty().setParticleOptions(ParticleTypes.COMPOSTER).build(), null, pull));
        add("projectile_push", projectileHit(propsEmpty().setParticleOptions(ParticleTypes.COMPOSTER).build(), null, push));
        add("projectile_puddle", projectileHit(propsEmpty().setParticleOptions(ParticleTypes.DRIPPING_WATER).setDiscardOnHitEntity(false).build(), puddle, null));
        add("projectile_flower_field", projectileHit(propsEmpty().setParticleOptions(ParticleTypes.COMPOSTER).setDiscardOnHitEntity(false).build(), flowerField, null));
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



    private Holder.Reference<SpellAction> add(String path, Holder<SpellAction> action) {
        Holder.Reference<SpellAction> value = context.register(ResourceKey.create(Values.SPELL_ACTIONS_KEY, Values.id(path)), action.value());
        values.add(value);
        return value;
    }



    public static ProjectileProps propsDefault() {
        return ProjectileProps.DEFAULT;
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

    public static Holder<SpellAction> transform(RuleTest ruleTest, BlockState blockState, RuleTest ruleTest1, BlockState blockState1) {
        return Holder.direct(new BlockTransformation(
                new BlockTransformation.TransformEntry(ruleTest, blockState),
                new BlockTransformation.TransformEntry(ruleTest1, blockState1)
        ));
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
        return Holder.direct(new ShapeBlockTarget(ShapeBlockTarget.Shape.SPHERE, range, maskSurface, maskSolid));
    }

    public static Holder<SpellAction> cubeSphere(int range, boolean maskSurface, boolean maskSolid) {
        return Holder.direct(new ShapeBlockTarget(ShapeBlockTarget.Shape.CUBE, range, maskSurface, maskSolid));
    }

    public static Holder<SpellAction> diamondSphere(int range, boolean maskSurface, boolean maskSolid) {
        return Holder.direct(new ShapeBlockTarget(ShapeBlockTarget.Shape.DIAMOND, range, maskSurface, maskSolid));
    }

    public static Holder<SpellAction> shapeSphere(int range, boolean maskSurface, boolean maskSolid, BlockPredicate filter) {
        return Holder.direct(new ShapeBlockTarget(ShapeBlockTarget.Shape.SPHERE, range, maskSurface, maskSolid, filter));
    }

    public static Holder<SpellAction> cubeSphere(int range, boolean maskSurface, boolean maskSolid, BlockPredicate filter) {
        return Holder.direct(new ShapeBlockTarget(ShapeBlockTarget.Shape.CUBE, range, maskSurface, maskSolid, filter));
    }

    public static Holder<SpellAction> diamondSphere(int range, boolean maskSurface, boolean maskSolid, BlockPredicate filter) {
        return Holder.direct(new ShapeBlockTarget(ShapeBlockTarget.Shape.DIAMOND, range, maskSurface, maskSolid, filter));
    }

    public static Holder<SpellAction> targetNear(float range, EntityType<?> entityType) {
        return new NearbyEntitiesTarget(range, entityType).inline();
    }

    public static Holder<SpellAction> targetNear(float range, EntityType<?>... entityTypes) {
        return new NearbyEntitiesTarget(range, entityTypes).inline();
    }

    public static Holder<SpellAction> targetNear(float range, HolderSet<EntityType<?>> entityTypes) {
        return new NearbyEntitiesTarget(range, entityTypes).inline();
    }

    public Holder<SpellAction> targetNear(float range, TagKey<EntityType<?>> entityTypes) {
        return new NearbyEntitiesTarget(range, context.lookup(Registries.ENTITY_TYPE).getOrThrow(entityTypes)).inline();
    }

    public static @NotNull Holder<SpellAction> damageMagic(int damageAmount) {
        return Holder.direct(new DamageEntityAction(DamageTypes.MAGIC, damageAmount));
    }

    public static @NotNull Holder<SpellAction> fireControl(int fireTicks) {
        return Holder.direct(new FireControlAction(fireTicks));
    }

}
