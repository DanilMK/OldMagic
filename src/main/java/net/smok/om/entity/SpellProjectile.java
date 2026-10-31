package net.smok.om.entity;

import net.minecraft.core.Holder;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.projectile.ItemSupplier;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.hurtingprojectile.AbstractHurtingProjectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.*;
import net.smok.om.spell.ProjectileProps;
import net.smok.om.spell.SpellAction;
import net.smok.om.spell.entity.action.ProjectileSpawn;
import org.jetbrains.annotations.Nullable;

public class SpellProjectile extends AbstractHurtingProjectile implements ItemSupplier {


    private static final EntityDataAccessor<ProjectileProps> PROJ_PROPS = SynchedEntityData.defineId(SpellProjectile.class, ProjectileProps.DATA_SERIALIZER);

    private int liveTime = 0;
    private int onTickDelay = 1;

    private ItemStack wand;

    @Nullable private Holder<SpellAction> onTick;
    @Nullable private Holder<SpellAction> onEntityHit;
    @Nullable private Holder<SpellAction> onBlockHit;

    public SpellProjectile(EntityType<? extends SpellProjectile> type, Level level) {
        super(type, level);
    }


    public static void cast(ServerLevel level, LivingEntity caster, ItemStack wand, ProjectileSpawn action, ProjectileProps props) {
        Projectile.spawnProjectileFromRotation(SpellProjectile::new, level, wand, caster, 0, 1.5f, 1).setActions(props, 1, action.onTick(), action.onEntityHit(), action.onBlockHit());
    }


    public ProjectileProps getProps() {
        return getEntityData().get(PROJ_PROPS);
    }

    private void setProps(ProjectileProps projectileProps) {
        getEntityData().set(PROJ_PROPS, projectileProps);
    }

    public void setActions(ProjectileProps props, int onTickDelay, @Nullable Holder<SpellAction> onTick, @Nullable Holder<SpellAction> onEntityHit, @Nullable Holder<SpellAction> onBlockHit) {
        setProps(props);
        reapplyPosition();
        refreshDimensions();
        this.onTickDelay = Math.clamp(onTickDelay, 1, 1200);
        this.onTick = onTick;
        this.onEntityHit = onEntityHit;
        this.onBlockHit = onBlockHit;
    }

    @Override
    public void applyOnProjectileSpawned(ServerLevel serverLevel, ItemStack pickupItemStack) {
        super.applyOnProjectileSpawned(serverLevel, pickupItemStack);
        wand = pickupItemStack;
    }

    public SpellProjectile(ServerLevel serverLevel, LivingEntity livingEntity, ItemStack itemStack) {
        Vec3 eyePosition = livingEntity.getEyePosition();
        super(OMEntities.SPELL_PROJECTILE, eyePosition.x, eyePosition.y, eyePosition.z, serverLevel);
        setOwner(livingEntity);
    }

    @Override
    protected boolean shouldBurn() {
        return false;
    }

    @Override
    protected double getDefaultGravity() {
        return getProps().gravity();
    }

    @Override
    protected @Nullable ParticleOptions getTrailParticle() {
        return getProps().particleOptions();
    }

    @Override
    public void tick() {
        if (!level().isClientSide()) {
            if (liveTime % onTickDelay == 0 && onTick != null) {
                SpellAction.run((ServerLevel) level(), onTick, this, wand);
            }
            if (liveTime++ > getProps().lifetime()) {
                discard();
                return;
            }
        }

        super.tick();
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        output.putInt("live_time", liveTime);
        output.store("props", ProjectileProps.CODEC, getProps());
        output.store("wand", ItemStack.CODEC, wand);
        output.putInt("onTickDelay", onTickDelay);
        if (onTick != null) output.store("onTick", SpellAction.HOLDER_CODEC, onTick);
        if (onEntityHit != null) output.store("onEntityHit", SpellAction.HOLDER_CODEC, onEntityHit);
        if (onBlockHit != null) output.store("onBlockHit", SpellAction.HOLDER_CODEC, onBlockHit);
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        liveTime = input.getIntOr("live_time", 0);
        input.read("props", ProjectileProps.CODEC).ifPresent(this::setProps);
        wand = input.read("wand", ItemStack.CODEC).orElse(ItemStack.EMPTY);
        onTickDelay = input.getIntOr("onTickDelay", 1);
        onTick = input.read("onTick", SpellAction.HOLDER_CODEC).orElse(null);
        onEntityHit = input.read("onEntityHit", SpellAction.HOLDER_CODEC).orElse(null);
        onBlockHit = input.read("onBlockHit", SpellAction.HOLDER_CODEC).orElse(null);
    }

    @Override
    public ItemStack getItem() {
        return getProps().itemRender().value().getDefaultInstance();
    }

    @Override
    protected void onHitEntity(EntityHitResult hitResult) {
        super.onHitEntity(hitResult);
        if (onEntityHit != null && level() instanceof ServerLevel level) {
            SpellAction.run(level, onEntityHit, this, wand, hitResult);
            //spell.value().runActions(new SpellContext(level, this, hitResult));
        }
        if (getProps().discardOnHitEntity()) discard();
    }

    @Override
    protected void onHitBlock(BlockHitResult hitResult) {
        super.onHitBlock(hitResult);
        if (onBlockHit != null && level() instanceof ServerLevel level) {
            SpellAction.run(level, onBlockHit, this, wand, hitResult);
            //spell.value().runActions(new SpellContext(level, this, hitResult));
        }
        if (getProps().discardOnHitBlock()) discard();
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder entityData) {
        super.defineSynchedData(entityData);
        entityData.define(PROJ_PROPS, ProjectileProps.DEFAULT);
    }

    @Override
    public EntityDimensions getDimensions(Pose pose) {
        return super.getDimensions(pose).scale(getProps().width(), getProps().height()); // todo re test this
    }

    public float getItemRenderScale() {
        return getProps().itemScale();
    }

}
