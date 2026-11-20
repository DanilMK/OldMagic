package net.smok.om.network;

import net.minecraft.core.Holder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.item.Item;
import net.smok.om.Values;
import net.smok.om.spell.Spell;
import org.jetbrains.annotations.NotNull;

public record EnableToggleSpellPayload(@NotNull Holder<Spell> spellHolder, Holder<Item> item, int delay) implements CustomPacketPayload {
    public static final Type<EnableToggleSpellPayload> TYPE = new Type<>(Values.id("enable_toggle_spell"));

    public static final StreamCodec<RegistryFriendlyByteBuf, EnableToggleSpellPayload> STREAM_CODEC = StreamCodec.composite(
            Spell.HOLDER_STREAM_CODEC, EnableToggleSpellPayload::spellHolder,
            Item.STREAM_CODEC, EnableToggleSpellPayload::item,
            ByteBufCodecs.INT, EnableToggleSpellPayload::delay,
            EnableToggleSpellPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
