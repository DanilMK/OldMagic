package net.smok.om.utils;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.phys.Vec3;

public enum Shape implements StringRepresentable {
    SPHERE("sphere"), CUBE("cube"), DIAMOND("diamond");

    public static final Codec<Shape> SHAPE_CODEC = StringRepresentable.fromEnum(Shape::values);
    public final String name;

    Shape(String name) {
        this.name = name;
    }

    public boolean isInRange(BlockPos center, BlockPos blockPos, float range) {
        switch (this) {
            case SPHERE -> {
                return center.distSqr(blockPos) < range * range;
            }
            case DIAMOND -> {
                return (float) center.distManhattan(blockPos) < range;
            }
            default -> {
                BlockPos dist = blockPos.subtract(center);
                return Math.abs(dist.getX()) < range && Math.abs(dist.getY()) < range && Math.abs(dist.getZ()) < range;
            }
        }
    }

    public boolean isInRange(Vec3 center, Vec3 position, float range) {
        switch (this) {
            case SPHERE -> {
                return center.distanceToSqr(position) < range * range;
            }
            case DIAMOND -> {
                return Math.abs(position.x - center.x) + Math.abs(position.y - center.y) + Math.abs(position.z - center.z) < range;
            }
            default -> {
                Vec3 dist = position.subtract(center);
                return Math.abs(dist.x()) < range && Math.abs(dist.y()) < range && Math.abs(dist.z()) < range;
            }
        }
    }

    @Override
    public String getSerializedName() {
        return name;
    }
}
