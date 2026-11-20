package net.smok.om.mixin;

import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.smok.om.spell.Spell;
import net.smok.om.spell.actions.SpellAction;
import net.smok.om.spell.cast.CastHolder;
import net.smok.om.spell.cast.PlayerCaster;
import net.smok.om.utils.StoredValues;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayer.class)
public class PlayerMixin implements PlayerCaster {

    @Unique private final CastHolder castHolder = CastHolder.createServer((ServerPlayer) (Object) this);

    @Override
    public void oldMagic$releaseCast(int slot) {
        castHolder.removeUsed(slot);
    }

    @Override
    public void oldMagic$startCast(int slot) {
        PlayerCaster.super.forWandItem((player, hand, wandItem) -> {
            wandItem.castSpell(player.level(), player, hand, slot, () -> {});

        });

    }

    @Inject(method = "tick", at = @At("TAIL"))
    private void tickInjection(CallbackInfo ci) {
        castHolder.tick(oldMagic$getAsPlayer());
    }

    @Inject(method = "readAdditionalSaveData", at = @At("TAIL"))
    private void readInjection(ValueInput input, CallbackInfo ci) {
        input.child("cast_holder").ifPresent(castHolder::readData);
    }

    @Inject(method = "addAdditionalSaveData", at = @At("TAIL"))
    private void writeInjection(ValueOutput output, CallbackInfo ci) {
        castHolder.writeData(output.child("cast_holder"));
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
    public boolean oldMagic$enableToggleServer(
            @NotNull Holder<Item> wand,
            @Nullable Holder<Spell> spell,
            @Nullable Holder<SpellAction> onEnd,
            @Nullable Holder<SpellAction> onTick,
            int tickDelay,
            @Nullable StoredValues storedValues
            ) {
        return castHolder.enableToggle(wand, spell, onEnd, onTick, tickDelay, storedValues);
    }

    @Override
    public void oldMagic$enableToggle(@NotNull Holder<Spell> spell, int tickDelay, @NotNull Holder<Item> wand) {
        castHolder.enableToggle(spell, tickDelay, wand);
    }

    @Override
    public void oldMagic$disableToggle(@Nullable Holder<Spell> spell) {
        castHolder.disableToggle(spell);
    }

    @Override
    public boolean oldMagic$isToggled(Holder<Spell> spell) {
        return castHolder.isToggled(spell);
    }

}
