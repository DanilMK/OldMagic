package net.smok.om.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.smok.om.Values;

public record SpellCooldownPayload(Identifier identifier, int duration) implements CustomPacketPayload {

    public static final Type<SpellCooldownPayload> TYPE = new Type<>(Values.id("spell_cooldown"));

    public static final StreamCodec<RegistryFriendlyByteBuf, SpellCooldownPayload> STREAM_CODEC = StreamCodec.composite(
            Identifier.STREAM_CODEC, SpellCooldownPayload::identifier,
            ByteBufCodecs.INT, SpellCooldownPayload::duration,
            SpellCooldownPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

}
