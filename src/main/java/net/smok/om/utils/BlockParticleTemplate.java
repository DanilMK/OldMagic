package net.smok.om.utils;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.smok.om.Debug;
import net.smok.om.Values;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public record BlockParticleTemplate(
        @NotNull ParticleOptions particle,
        @NotNull Vec3 position,
        @NotNull Vec3 size,
        int count,
        @NotNull Appearing appearing,
        @NotNull Movement movement
) implements CustomPacketPayload {

    public static final Codec<BlockParticleTemplate> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        ParticleTypes.CODEC.fieldOf("particle").forGetter(BlockParticleTemplate::particle),
        Vec3.CODEC.fieldOf("centerOffset").forGetter(BlockParticleTemplate::position),
        Vec3.CODEC.fieldOf("size").forGetter(BlockParticleTemplate::size),
        Codec.INT.fieldOf("count").forGetter(BlockParticleTemplate::count),
        Appearing.CODEC.fieldOf("appearing").forGetter(BlockParticleTemplate::appearing),
        Movement.CODEC.fieldOf("move").forGetter(BlockParticleTemplate::movement)
    ).apply(instance, BlockParticleTemplate::new));
    
    public static final StreamCodec<RegistryFriendlyByteBuf, BlockParticleTemplate> STREAM_CODEC = StreamCodec.composite(
            ParticleTypes.STREAM_CODEC, BlockParticleTemplate::particle,
            Vec3.STREAM_CODEC, BlockParticleTemplate::position,
            Vec3.STREAM_CODEC, BlockParticleTemplate::size,
            ByteBufCodecs.INT, BlockParticleTemplate::count,
            Appearing.STREAM_CODEC, BlockParticleTemplate::appearing,
            Movement.STREAM_CODEC, BlockParticleTemplate::movement,
            BlockParticleTemplate::new
    );
    
    public static final CustomPacketPayload.Type<BlockParticleTemplate> TYPE = new Type<>(Values.id("particle_template"));
    
    public boolean addParticlesServer(ServerLevel level, ServerPlayer player, Vec3 position) {
        position = position.add(this.position);

        if (player.level() != level) {
            return false;
        } else {
            BlockPos pos = player.blockPosition();
            if (pos.closerToCenterThan(position, 32.0)) {
                ServerPlayNetworking.send(player, new BlockParticleTemplate(particle, position, size, count, appearing, movement));
                return true;
            } else {
                return false;
            }
        }
    }
    
    public void addParticleClient(Level level) {
        addRandomParticle(level, particle, position, size, count, movement, appearing);
    }

    public static void addRandomParticle(Level level, ParticleOptions particle, Vec3 center, Vec3 size, int count, Movement movement, Appearing appearing) {
        for (int i = 0; i < count; i++) {
            Vec3 position = appearing.calculatePosition(center, size, level.getRandom());
            Vec3 motion = movement.calculateMotion(position, center, level.getRandom());

            try {
                level
                        .addParticle(
                                particle,
                                false,
                                false,
                                position.x(),
                                position.y(),
                                position.z(),
                                motion.x,
                                motion.y,
                                motion.z
                        );
            } catch (Throwable ignored) {
                Debug.warn("Could not spawn particle effect {}", particle);
                return;
            }
        }
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }


    public interface Appearing {
        Codec<Appearing> CODEC = Codec.STRING.dispatch(appearing -> {
            if (appearing instanceof FacesAppearing) return "faces";
            if (appearing instanceof OutlineAppearing) return "outline";
            return "solid";
        }, type -> switch (type) {
            case "faces" -> FacesAppearing.MAP_CODEC;
            case "outline" -> OutlineAppearing.MAP_CODEC;
            case "solid" -> MapCodec.unit(SOLID_APPEARING);
            default -> throw new IllegalStateException("Unexpected value: " + type);
        });
        
        StreamCodec<ByteBuf, Appearing> STREAM_CODEC = ByteBufCodecs.STRING_UTF8.dispatch(appearing -> {
            if (appearing instanceof FacesAppearing) return "faces";
            if (appearing instanceof OutlineAppearing) return "outline";
            return "solid";
        }, type -> switch (type) {
            case "faces" -> FacesAppearing.STREAM_CODEC;
            case "outline" -> OutlineAppearing.STREAM_CODEC;
            case "solid" -> StreamCodec.unit(SOLID_APPEARING);
            default -> throw new IllegalStateException("Unexpected value: " + type);
        });
        
        Vec3 calculatePosition(Vec3 center, Vec3 size, RandomSource random);
    }

    public static Appearing SOLID_APPEARING = (center, size, random) -> new Vec3(
            center.x + random.nextGaussian() * size.x() / 2,
            center.y + random.nextGaussian() * size.y() / 2,
            center.z + random.nextGaussian() * size.z() / 2
    );

    public record FacesAppearing(float thickness) implements Appearing {
        
        public static final MapCodec<FacesAppearing> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                Codec.floatRange(0, 1).fieldOf("thickness").forGetter(FacesAppearing::thickness)
        ).apply(instance, FacesAppearing::new));
        
        public static final StreamCodec<ByteBuf, FacesAppearing> STREAM_CODEC = StreamCodec.composite(ByteBufCodecs.FLOAT, FacesAppearing::thickness, FacesAppearing::new);

        @Override
        public Vec3 calculatePosition(Vec3 center, Vec3 size, RandomSource random) {
            Direction face = Direction.values()[random.nextInt(6)];
            Vec3 facePoint = center.add(face.getUnitVec3().multiply(size));
            Vec3 shift = SOLID_APPEARING.calculatePosition(Vec3.ZERO, size, random)
                    .multiply(
                            face.getAxis() == Direction.Axis.X ? thickness : 1,
                            face.getAxis() == Direction.Axis.Y ? thickness : 1,
                            face.getAxis() == Direction.Axis.Z ? thickness : 1
                            );
            return facePoint.add(shift);
        }
    }

    public record OutlineAppearing(float thickness) implements Appearing {

        public static final MapCodec<OutlineAppearing> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                Codec.floatRange(0, 1).fieldOf("thickness").forGetter(OutlineAppearing::thickness)
        ).apply(instance, OutlineAppearing::new));
        
        public static final StreamCodec<ByteBuf, OutlineAppearing> STREAM_CODEC = StreamCodec.composite(ByteBufCodecs.FLOAT, OutlineAppearing::thickness, OutlineAppearing::new);
        
        private static final Vec3 EDGE_N = new Vec3(0, -1, -1);
        private static final Vec3 EDGE_S = new Vec3(0, -1, 1);
        private static final Vec3 EDGE_W = new Vec3(-1, -1, 0);
        private static final Vec3 EDGE_E = new Vec3(1, -1, 0);

        private static final Vec3 EDGE_UP_N = new Vec3(0, 1, -1);
        private static final Vec3 EDGE_UP_S = new Vec3(0, 1, 1);
        private static final Vec3 EDGE_UP_W = new Vec3(-1, 1, 0);
        private static final Vec3 EDGE_UP_E = new Vec3(1, 1, 0);

        private static final Vec3 EDGE_NW = new Vec3(-1, 0, -1);
        private static final Vec3 EDGE_NE = new Vec3(1, 0, -1);
        private static final Vec3 EDGE_SW = new Vec3(-1, 0, 1);
        private static final Vec3 EDGE_SE = new Vec3(1, 0, 1);

        private static final List<Vec3> EDGES = List.of(EDGE_N, EDGE_S, EDGE_W, EDGE_E, EDGE_UP_N, EDGE_UP_S, EDGE_UP_W, EDGE_UP_E, EDGE_NW, EDGE_NE, EDGE_SW, EDGE_SE);

        @Override
        public Vec3 calculatePosition(Vec3 center, Vec3 size, RandomSource random) {
            Vec3 edge = EDGES.get(random.nextInt(EDGES.size()));
            Vec3 edgePoint = center.add(edge.multiply(size));
            Vec3 shift = SOLID_APPEARING.calculatePosition(Vec3.ZERO, size, random)
                    .multiply(
                            edge.x == 0 ? 1 : thickness,
                            edge.y == 0 ? 1 : thickness,
                            edge.z == 0 ? 1 : thickness
                    );
            return edgePoint.add(shift);
        }
    }


    public record Movement(MoveRule moveRule, double speed, double randomness) {

        public static final Codec<Movement> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            MoveRule.CODEC.fieldOf("moveRule").forGetter(Movement::moveRule),
            Codec.DOUBLE.fieldOf("speed").forGetter(Movement::speed),
            Codec.DOUBLE.fieldOf("randomness").forGetter(Movement::randomness)
        ).apply(instance, Movement::new));

        public static final StreamCodec<ByteBuf, Movement> STREAM_CODEC = StreamCodec.composite(
                MoveRule.STREAM_CODEC, Movement::moveRule,
                ByteBufCodecs.DOUBLE, Movement::speed,
                ByteBufCodecs.DOUBLE, Movement::randomness,
                Movement::new
        );


        Vec3 calculateMotion(Vec3 position, Vec3 center, RandomSource random) {
            return switch (moveRule) {
                case STATIC, VORTEX -> randomness > 0 ? new Vec3(
                        random.nextGaussian() * speed * randomness,
                        random.nextGaussian() * speed * randomness,
                        random.nextGaussian() * speed * randomness
                ) : Vec3.ZERO;
                case FROM_CENTER -> {
                    Vec3 direction = position.subtract(center).normalize().scale(speed);
                    if (randomness > 0) yield direction.add(
                            random.nextGaussian() * speed * randomness,
                            random.nextGaussian() * speed * randomness,
                            random.nextGaussian() * speed * randomness
                    );
                    yield direction;
                }
                case TO_CENTER -> {
                    Vec3 direction = center.subtract(position).normalize().scale(speed);
                    if (randomness > 0) yield direction.add(
                            random.nextGaussian() * speed * randomness,
                            random.nextGaussian() * speed * randomness,
                            random.nextGaussian() * speed * randomness
                    );
                    yield direction;
                }
            };
        }
    }

    public enum MoveRule implements StringRepresentable {
        STATIC("static", (byte) 0), FROM_CENTER("from_center", (byte) 1), TO_CENTER("to_center", (byte) 2), VORTEX("vortex", (byte) 3);
        
        public static final Codec<MoveRule> CODEC = StringRepresentable.fromEnum(MoveRule::values);
        public static final StreamCodec<ByteBuf, MoveRule> STREAM_CODEC = ByteBufCodecs.BYTE.map(aByte -> switch (aByte) {
            case 0 -> STATIC;
            case 1 -> FROM_CENTER;
            case 2 -> TO_CENTER;
            case 3 -> VORTEX;
            default -> throw new IllegalStateException("Unexpected value: " + aByte);
        }, rule -> rule.number);

        private final String name;
        private final byte number;

        MoveRule(String name, byte number) {
            this.name = name;
            this.number = number;
        }

        @Override
        public String getSerializedName() {
            return name;
        }
    }


    public static class Builder {

        public static final Vec3 HALF_BLOCK = new Vec3(0.5, 0.5, 0.5);
        public static final Movement STATIC = new Movement(MoveRule.STATIC, 0, 0);
        private final ParticleOptions particle;
        private Vec3 position = Vec3.ZERO;
        private Vec3 size = HALF_BLOCK;
        private int count;
        private Appearing appearing = SOLID_APPEARING;
        private Movement movement = STATIC;


        public Builder(ParticleOptions particle) {
            this.particle = particle;
        }

        public Builder setPosition(Vec3 position) {
            this.position = position;
            return this;
        }

        public Builder setSize(Vec3 size) {
            this.size = size;
            return this;
        }

        public Builder setSize(double x, double y, double z) {
            this.size = new Vec3(x, y, z);
            return this;
        }

        public Builder setCount(int count) {
            this.count = count;
            return this;
        }

        public Builder setSolid() {
            return setAppearing(SOLID_APPEARING);
        }

        public Builder setFaces(float thickness) {
            return setAppearing(new FacesAppearing(thickness));
        }

        public Builder setOutline(float thickness) {
            return setAppearing(new OutlineAppearing(thickness));
        }

        public Builder setAppearing(@NotNull Appearing appearing) {
            this.appearing = appearing;
            return this;
        }

        public Builder setMovement(@NotNull MoveRule rule, double speed, double randomness) {
            return this.setMovement(new Movement(rule, speed, randomness));
        }

        public Builder setMovement(@NotNull Movement movement) {
            this.movement = movement;
            return this;
        }

        public BlockParticleTemplate build() {
            return new BlockParticleTemplate(particle, position, size, count, appearing, movement);
        }
    }
}
