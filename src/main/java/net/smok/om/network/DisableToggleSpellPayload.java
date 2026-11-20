package net.smok.om.network;

import net.minecraft.core.Holder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.smok.om.Values;
import net.smok.om.spell.Spell;
import org.jetbrains.annotations.NotNull;

public record DisableToggleSpellPayload(@NotNull Holder<Spell> spellHolder) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<DisableToggleSpellPayload> TYPE = new CustomPacketPayload.Type<>(Values.id("disable_toggle_spell"));

    public static final StreamCodec<RegistryFriendlyByteBuf, DisableToggleSpellPayload> STREAM_CODEC = StreamCodec.composite(
            Spell.HOLDER_STREAM_CODEC, DisableToggleSpellPayload::spellHolder,
            DisableToggleSpellPayload::new
    );

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

}
