package net.smok.om.network;

import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;

public interface NetworkHandler {

    static void init() {
        PayloadTypeRegistry.serverboundPlay().register(SpellBookSelectPayload.TYPE, SpellBookSelectPayload.STREAM_CODEC);
        ServerPlayNetworking.registerGlobalReceiver(SpellBookSelectPayload.TYPE, SpellBookSelectPayload::receive);

        PayloadTypeRegistry.serverboundPlay().register(CastSpellPayload.TYPE, CastSpellPayload.STREAM_CODEC);
        PayloadTypeRegistry.clientboundPlay().register(CastSpellPayload.TYPE, CastSpellPayload.STREAM_CODEC);
        ServerPlayNetworking.registerGlobalReceiver(CastSpellPayload.TYPE, CastSpellPayload::receive);

        PayloadTypeRegistry.clientboundPlay().register(SpellCooldownPayload.TYPE, SpellCooldownPayload.STREAM_CODEC);
        PayloadTypeRegistry.clientboundPlay().register(SpellCastPayload.TYPE, SpellCastPayload.STREAM_CODEC);

    }
}
