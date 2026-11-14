package net.smok.om.utils;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public record HitHolder(Vec3 position, BlockPos blockPos, @Nullable EntityHolder entity, @Nullable Direction hitDirection) {

    public static final Codec<HitHolder> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        Vec3.CODEC.fieldOf("position").forGetter(HitHolder::position),
        BlockPos.CODEC.fieldOf("block").forGetter(HitHolder::blockPos),
        EntityHolder.CODEC.optionalFieldOf("entity").forGetter(hitHolder -> Optional.ofNullable(hitHolder.entity)),
            Direction.CODEC.optionalFieldOf("block_hit_direction").forGetter(hitHolder -> Optional.ofNullable(hitHolder.hitDirection))
    ).apply(instance, (vec3, blockPos1, entityHolder, hitDirection) -> new HitHolder(vec3, blockPos1, entityHolder.orElse(null), hitDirection.orElse(null))));

    public void init(ServerLevel level) {
        if (entity != null) entity.init(level);
    }

    public static HitHolder byHit(HitResult hitResult) {
        if (hitResult instanceof EntityHitResult entityHitResult)
            return new HitHolder(
                    entityHitResult.getLocation(),
                    new BlockPos(
                            (int) entityHitResult.getLocation().x,
                            (int) entityHitResult.getLocation().y,
                            (int) entityHitResult.getLocation().z
                    ),
                    new EntityHolder(entityHitResult.getEntity()),
                    null
            );
        if (hitResult instanceof BlockHitResult blockHitResult)
            return new HitHolder(blockHitResult.getLocation(), blockHitResult.getBlockPos(), null, blockHitResult.getDirection());
        return new HitHolder(hitResult.getLocation(), new BlockPos(
                            (int) hitResult.getLocation().x,
                            (int) hitResult.getLocation().y,
                            (int) hitResult.getLocation().z
                        ), null, null);
    }


}
