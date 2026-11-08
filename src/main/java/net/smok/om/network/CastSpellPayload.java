package net.smok.om.network;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.smok.om.Values;
import net.smok.om.spell.cast.PlayerCaster;

public record CastSpellPayload(int spellSlot, boolean startOrRelease) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<CastSpellPayload> TYPE = new CustomPacketPayload.Type<>(Values.id("cast_spell"));

    public static final StreamCodec<RegistryFriendlyByteBuf, CastSpellPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.INT, CastSpellPayload::spellSlot,
            ByteBufCodecs.BOOL, CastSpellPayload::startOrRelease,
            CastSpellPayload::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public void receive(ServerPlayNetworking.Context context) {
        ServerPlayer player = context.player();
        context.server().execute(() -> {
            if (player instanceof PlayerCaster caster) {
                if (startOrRelease) caster.oldMagic$startCast(spellSlot);
                else caster.oldMagic$releaseCast(spellSlot);
            }
        });
    }

}
