package net.smok.om.entity;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.syncher.EntityDataSerializer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.NotNull;

public record ProjectileProps(float width, float height, float gravity, float itemScale, int lifetime,
                              boolean discardOnHitEntity, boolean discardOnHitBlock,
                              @NotNull Holder<Item> itemRender, @NotNull ParticleOptions particleOptions) {


    public static ProjectileProps DEFAULT = new ProjectileProps(1, 1, 0, 1, 20*3, true, true, BuiltInRegistries.ITEM.wrapAsHolder(Items.AIR), ParticleTypes.SMOKE);


    public static final Codec<ProjectileProps> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.FLOAT.optionalFieldOf("width", 1f).forGetter(ProjectileProps::width),
            Codec.FLOAT.optionalFieldOf("height", 1f).forGetter(ProjectileProps::height),
            Codec.FLOAT.optionalFieldOf("gravity", 0f).forGetter(ProjectileProps::gravity),
            Codec.FLOAT.optionalFieldOf("itemScale", 1f).forGetter(ProjectileProps::itemScale),
            Codec.INT.optionalFieldOf("lifetime", 2*30).forGetter(ProjectileProps::lifetime),
            Codec.BOOL.optionalFieldOf("discardOnHitEntity", true).forGetter(ProjectileProps::discardOnHitEntity),
            Codec.BOOL.optionalFieldOf("discardOnHitBlock", true).forGetter(ProjectileProps::discardOnHitBlock),
            BuiltInRegistries.ITEM.holderByNameCodec().fieldOf("itemRender").forGetter(ProjectileProps::itemRender),
            ParticleTypes.CODEC.optionalFieldOf("particle", ParticleTypes.SMOKE).forGetter(ProjectileProps::particleOptions)
    ).apply(instance, ProjectileProps::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, ProjectileProps> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.FLOAT, ProjectileProps::width,
            ByteBufCodecs.FLOAT, ProjectileProps::height,
            ByteBufCodecs.FLOAT, ProjectileProps::gravity,
            ByteBufCodecs.FLOAT, ProjectileProps::itemScale,
            ByteBufCodecs.INT, ProjectileProps::lifetime,
            ByteBufCodecs.BOOL, ProjectileProps::discardOnHitEntity,
            ByteBufCodecs.BOOL, ProjectileProps::discardOnHitBlock,
            Item.STREAM_CODEC, ProjectileProps::itemRender,
            ParticleTypes.STREAM_CODEC, ProjectileProps::particleOptions,
            ProjectileProps::new
    );

    public static final EntityDataSerializer<ProjectileProps> DATA_SERIALIZER = new EntityDataSerializer<ProjectileProps>() {
        @Override
        public StreamCodec<? super RegistryFriendlyByteBuf, ProjectileProps> codec() {
            return STREAM_CODEC;
        }

        @Override
        public ProjectileProps copy(ProjectileProps value) {
            return new ProjectileProps(value.width, value.height, value.gravity, value.itemScale, value.lifetime, value.discardOnHitEntity, value.discardOnHitBlock, value.itemRender, value.particleOptions);
        }
    };


    public static final class Builder {
        private float width;
        private float height;
        private float gravity;
        private float itemScale;
        private int lifetime;
        private boolean discardOnHitEntity;
        private boolean discardOnHitBlock;
        private Holder<Item> itemRender;
        private ParticleOptions particleOptions;

        public Builder(float width, float height, float gravity, float itemScale, int lifetime, boolean discardOnHitEntity, boolean discardOnHitBlock, @NotNull Holder<Item> itemRender, ParticleOptions particleOptions) {
            this.width = width;
            this.height = height;
            this.gravity = gravity;
            this.itemScale = itemScale;
            this.lifetime = lifetime;
            this.discardOnHitEntity = discardOnHitEntity;
            this.discardOnHitBlock = discardOnHitBlock;
            this.itemRender = itemRender;
            this.particleOptions = particleOptions;
        }

        public static Builder empty() {
            return new Builder(1, 1, 0, 1, 2*30, true, true, BuiltInRegistries.ITEM.wrapAsHolder(Items.AIR), ParticleTypes.SMOKE);
        }

        public static Builder withItem(Item item) {
            return new Builder(1, 1, 0, 1, 2*30, true, true, BuiltInRegistries.ITEM.wrapAsHolder(item), ParticleTypes.SMOKE);
        }

        public ProjectileProps build() {
            return new ProjectileProps(width, height, gravity, itemScale, lifetime, discardOnHitEntity, discardOnHitBlock, itemRender, particleOptions);
        }


        public Builder setLifetime(int lifetime) {
            this.lifetime = lifetime;
            return this;
        }


        public Builder setItemRender(@NotNull Holder<Item> itemRender) {
            this.itemRender = itemRender;
            return this;
        }

        public Builder setParticleOptions(ParticleOptions particleOptions) {
            this.particleOptions = particleOptions;
            return this;
        }

        public Builder setDiscardOnHitEntity(boolean discardOnHitEntity) {
            this.discardOnHitEntity = discardOnHitEntity;
            return this;
        }

        public Builder setDiscardOnHitBlock(boolean discardOnHitBlock) {
            this.discardOnHitBlock = discardOnHitBlock;
            return this;
        }
    }
}
