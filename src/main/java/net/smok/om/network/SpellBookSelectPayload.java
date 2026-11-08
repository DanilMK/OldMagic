package net.smok.om.network;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.item.ItemStack;
import net.smok.om.Values;
import net.smok.om.item.BookContents;
import net.smok.om.item.OMDataComponents;

public record SpellBookSelectPayload(int slot, int spell) implements CustomPacketPayload {

    public static final Type<SpellBookSelectPayload> TYPE = new Type<>(Values.id("spell_book_select"));

    public static final StreamCodec<RegistryFriendlyByteBuf, SpellBookSelectPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.INT, SpellBookSelectPayload::slot,
            ByteBufCodecs.INT, SpellBookSelectPayload::spell,
            SpellBookSelectPayload::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public void receive(ServerPlayNetworking.Context context) {
        ServerPlayer player = context.player();
        context.server().execute(() -> {
            SlotAccess playerSlot = player.getInventory().getSlot(slot);
            if (playerSlot != null) {
                ItemStack itemStack = playerSlot.get();
                BookContents bookContents = itemStack.get(OMDataComponents.BOOK_CONTENTS);
                if (bookContents != null) itemStack.set(OMDataComponents.BOOK_CONTENTS, bookContents.setSelected(spell));
            }
        });
    }
}
