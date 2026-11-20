package net.smok.om.client.mixin;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.Holder;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.smok.om.spell.Spell;
import net.smok.om.utils.ScreenOpener;
import net.smok.om.client.render.screens.Screens;
import net.smok.om.network.CastSpellPayload;
import net.smok.om.spell.cast.CastHolder;
import net.smok.om.spell.cast.PlayerCaster;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LocalPlayer.class)
public class PlayerMixin implements ScreenOpener, PlayerCaster {

    @Unique private final CastHolder castHolder = CastHolder.createClient();

    @Override
    public void old_magic$openScreen(String key, ItemStack itemStack, InteractionHand hand) {
        Screens.open(key, (LocalPlayer) (Object) this, itemStack, hand);
    }


    @Override
    public void oldMagic$releaseCast(int slot) {
        ClientPlayNetworking.send(new CastSpellPayload(slot, false));
    }

    @Override
    public void oldMagic$startCast(int slot) {
        PlayerCaster.super.forWandItem((player, hand, wandItem) -> {
            InteractionResult interactionResult = wandItem.castSpell(player.level(), player, hand, slot, () -> {});
            if (interactionResult.consumesAction())
                ClientPlayNetworking.send(new CastSpellPayload(slot, true));
        });
    }

    @Override
    public Player oldMagic$getAsPlayer() {
        return (Player) (Object) this;
    }

    @Override
    public CastHolder oldMagic$getCastHolder() {
        return castHolder;
    }

    @Override
    public boolean oldMagic$isToggled(Holder<Spell> spell) {
        return castHolder.isToggled(spell);
    }

    @Inject(method = "tick", at = @At("TAIL"))
    private void tickInjection(CallbackInfo ci) {
        castHolder.tick(oldMagic$getAsPlayer());
    }

    @Override
    public void oldMagic$enableToggle(@NotNull Holder<Spell> spell, int tickDelay, @NotNull Holder<Item> wand) {
        castHolder.enableToggle(spell, tickDelay, wand);
    }

    @Override
    public void oldMagic$disableToggle(@Nullable Holder<Spell> spell) {
        castHolder.disableToggle(spell);
    }
}
