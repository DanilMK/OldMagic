package net.smok.om.utils;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public record SoundTemplate(@NotNull Identifier sound, float fixedRange, Vec3 position, float volume, float pitch, float minVolume) {

    public static final Codec<SoundTemplate> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Identifier.CODEC.fieldOf("sound").forGetter(SoundTemplate::sound),
            Codec.FLOAT.optionalFieldOf("fixedRange", -1f).forGetter(SoundTemplate::fixedRange),
            Vec3.CODEC.fieldOf("position").forGetter(SoundTemplate::position),
            Codec.FLOAT.fieldOf("volume").forGetter(SoundTemplate::volume),
            Codec.FLOAT.fieldOf("pitch").forGetter(SoundTemplate::pitch),
            Codec.FLOAT.fieldOf("minVolume").forGetter(SoundTemplate::minVolume)
    ).apply(instance, SoundTemplate::new));

    public boolean playSound(ServerLevel level, Vec3 position, SoundSource source) {
        long seed = level.getRandom().nextLong();
        return level.players().stream().anyMatch(serverPlayer -> playSound(serverPlayer, position, seed, source));
    }

    public boolean playSound(ServerPlayer player, Vec3 position, long seed, SoundSource soundSource) {
        Vec3 localPosition = position.add(this.position);
        Vec3 delta = localPosition.subtract(player.position());
        float localVolume = volume;
        double distanceSqr = delta.lengthSqr();
        Holder<SoundEvent> soundEvent = fixedRange < 0 ? Holder.direct(SoundEvent.createVariableRangeEvent(sound)) : Holder.direct(SoundEvent.createFixedRangeEvent(sound, fixedRange));
        double maxDistSqr = Mth.square(soundEvent.value().getRange(volume));
        if (distanceSqr > maxDistSqr) {
            if (minVolume <= 0.0F) {
                return false;
            }

            double distance = Math.sqrt(distanceSqr);
            localPosition = new Vec3(player.getX() + delta.x / distance * 2.0, player.getY() + delta.y / distance * 2.0, player.getZ() + delta.z / distance * 2.0);
            localVolume = minVolume;
        }

        player.connection.send(new ClientboundSoundPacket(soundEvent, soundSource, localPosition.x(), localPosition.y(), localPosition.z(), localVolume, pitch, seed));
        return true;
    }


    @Contract(value = "_ -> new", pure = true)
    public @NotNull Builder builder(@NotNull Identifier sound) {
        return new Builder(sound);
    }

    public static class Builder {
        @NotNull private final Identifier sound;
        private float fixedRange = -1;
        private Vec3 position = Vec3.ZERO;
        private float volume = 1;
        private float pitch = 1;
        private float minVolume = 1;

        public Builder(@NotNull Identifier sound) {
            this.sound = sound;
        }

        public Builder setFixedRange(float fixedRange) {
            this.fixedRange = fixedRange;
            return this;
        }

        public Builder setPosition(Vec3 position) {
            this.position = position;
            return this;
        }

        public Builder setVolume(float volume) {
            this.volume = volume;
            return this;
        }

        public Builder setPitch(float pitch) {
            this.pitch = pitch;
            return this;
        }

        public Builder setMinVolume(float minVolume) {
            this.minVolume = minVolume;
            return this;
        }

        public SoundTemplate build() {
            return new SoundTemplate(sound, fixedRange, position, volume, pitch, minVolume);
        }
    }
}
