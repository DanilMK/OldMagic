package net.smok.om.spell.cast;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record Casting(int rechargeTime, int castTime, boolean cancelable, boolean singleUse) {
    public static final Codec<Casting> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.INT.fieldOf("recharge_ticks").forGetter(Casting::rechargeTime),
            Codec.INT.fieldOf("cast_ticks").forGetter(Casting::castTime),
            Codec.BOOL.fieldOf("cancelable").forGetter(Casting::cancelable),
            Codec.BOOL.fieldOf("single_use").forGetter(Casting::singleUse)
    ).apply(instance, Casting::new));


}
