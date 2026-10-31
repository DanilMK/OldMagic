package net.smok.om.item;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipProvider;
import net.minecraft.world.level.Level;
import net.smok.om.spell.CastHolder;
import net.smok.om.spell.PlayerCaster;
import net.smok.om.spell.Spell;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

public record Scroll(@NotNull Holder<Spell> spell, boolean unlockFirstTime,
                     boolean alwaysUnlock) implements TooltipProvider {

    public static final Codec<Scroll> SPELL_CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Spell.HOLDER_CODEC.fieldOf("spell").forGetter(Scroll::spell),
            Codec.BOOL.fieldOf("unlockFirstTime").forGetter(Scroll::unlockFirstTime),
            Codec.BOOL.fieldOf("alwaysUnlock").forGetter(Scroll::alwaysUnlock)
    ).apply(instance, Scroll::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, Scroll> SPELL_STREAM_CODEC =
            StreamCodec.composite(
                    Spell.HOLDER_STREAM_CODEC, Scroll::spell,
                    ByteBufCodecs.BOOL, Scroll::unlockFirstTime,
                    ByteBufCodecs.BOOL, Scroll::alwaysUnlock,
                    Scroll::new
            );

    public Scroll(Holder.Reference<Spell> spell) {
        this(spell, false, false);
    }

    @Override
    public void addToTooltip(Item.TooltipContext context, Consumer<Component> consumer, TooltipFlag flag, DataComponentGetter components) {
        consumer.accept(Component.translatable(Spell.languageKey(spell)));
    }

    public boolean startCast(Level level, Player player, ItemStack itemStack, Runnable onStart, int slotButton) {

        CastHolder castHolder = ((PlayerCaster) player).oldMagic$getCastHolder();
        if (unlockFirstTime) castHolder.unlockSpell(spell);

        if (castHolder.cantUse(spell)) return false;
        castHolder.startCast(level, player, itemStack, onStart, spell, slotButton, alwaysUnlock);
        return true;
    }

    public @Nullable String getSpellName() {
        if (spell instanceof Holder.Reference<Spell> reference) return reference.key().identifier().getPath();
        return null;
    }

}
