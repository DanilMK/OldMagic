package net.smok.om.network;

import net.minecraft.core.Holder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.item.Item;
import net.smok.om.Values;
import net.smok.om.spell.Spell;

public record SpellCastPayload(Holder<Spell> spell, Holder<Item> item, int slot, int duration) implements CustomPacketPayload {

    public static final Type<SpellCastPayload> TYPE = new Type<>(Values.id("spell_cast_server"));

    public static final StreamCodec<RegistryFriendlyByteBuf, SpellCastPayload> STREAM_CODEC = StreamCodec.composite(
            Spell.HOLDER_STREAM_CODEC, SpellCastPayload::spell,
            Item.STREAM_CODEC, SpellCastPayload::item,
            ByteBufCodecs.VAR_INT, SpellCastPayload::slot,
            ByteBufCodecs.INT, SpellCastPayload::duration,
            SpellCastPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
