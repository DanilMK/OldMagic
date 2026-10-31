package net.smok.om.spell;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.smok.om.network.CastSpellPayload;
import net.smok.om.network.SpellCastPayload;
import net.smok.om.network.SpellCooldownPayload;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.Collectors;

public class CastHolder {


    private final Map<Identifier, CooldownInstance> cooldowns;
    private final Map<Integer, CastInstance> used;
    private final Set<Holder<Spell>> unlockedSpells;
    private int tickCount;

    public CastHolder() {
        cooldowns = new HashMap<>();
        used = new HashMap<>();
        unlockedSpells = new HashSet<>();
    }

    private CastHolder(Map<Identifier, CooldownInstance> cooldowns, Map<Integer, CastInstance> used, Set<Holder<Spell>> unlockedSpells) {
        this.cooldowns = cooldowns;
        this.used = used;
        this.unlockedSpells = unlockedSpells;
    }

    @Contract(" -> new")
    public static @NotNull CastHolder createClient() {
        return new CastHolder();
    }

    @Contract("_ -> new")
    public static @NotNull ServerCastHolder createServer(ServerPlayer serverPlayer) {
        return new ServerCastHolder(serverPlayer);
    }

    public boolean isCasting(int slot) {
        return used.containsKey(slot);
    }

    public float getCooldownPercent(Holder<Spell> spellHolder, float partialTick) {
        return spellHolder.unwrapKey().map(key -> getCooldownPercent(key.identifier(), partialTick)).orElse(0f);
    }

    public float getCooldownPercent(Identifier id, float partialTick) {
        CooldownInstance cooldown = cooldowns.get(id);
        if (cooldown != null) {
            float duration = cooldown.endTime - cooldown.startTime;
            float remaining = cooldown.endTime - (this.tickCount + partialTick);
            return Mth.clamp(remaining / duration, 0.0F, 1.0F);
        } else {
            return 0.0F;
        }
    }

    public float getCastPercent(int slot, float partialTick) {
        if (!used.containsKey(slot)) return 1;
        CastInstance castInstance = used.get(slot);

        float duration = castInstance.endTime - castInstance.startTime;
        float remaining = castInstance.endTime - (this.tickCount + partialTick);
        return Mth.clamp(remaining / duration, 0.0F, 1.0F);
    }

    public void tick(Player player) {
        tickCount++;
        if (!cooldowns.isEmpty()) {
            Iterator<Map.Entry<Identifier, CooldownInstance>> iterator = cooldowns.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry<Identifier, CooldownInstance> next = iterator.next();
                if (next.getValue().endTime <= tickCount) {
                    iterator.remove();
                    onCooldownEnded(next.getKey());
                }
            }
        }

        if (!used.isEmpty()) {
            Iterator<Map.Entry<Integer, CastInstance>> iterator = used.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry<Integer, CastInstance> next = iterator.next();

                if (player.getItemInHand(InteractionHand.MAIN_HAND).is(next.getValue().item())) {
                    if (next.getValue().endTime <= tickCount) {
                        iterator.remove();
                        onUseFinish(next.getKey(), next.getValue().spell(), player.getItemInHand(InteractionHand.MAIN_HAND), next.getValue().forceLocked);
                    }
                } else if (player.getItemInHand(InteractionHand.OFF_HAND).is(next.getValue().item())) {
                    if (next.getValue().endTime <= tickCount) {
                        iterator.remove();
                        onUseFinish(next.getKey(), next.getValue().spell(), player.getItemInHand(InteractionHand.OFF_HAND), next.getValue().forceLocked);
                    }
                } else {
                    iterator.remove();
                    onUseStop(next.getKey());
                }
            }
        }

    }

    public void addCooldown(Identifier id, int time) {
        cooldowns.put(id, new CooldownInstance(tickCount, tickCount + time));
        onCooldownStart(id, time);
    }

    public void removeCooldown(Identifier id) {
        cooldowns.remove(id);
        onCooldownEnded(id);
    }


    public void addUsed(Holder<Spell> spell, Holder<Item> item, boolean forceOpen, int slot, int time) {
        used.put(slot, new CastInstance(item, spell, forceOpen, tickCount, tickCount + time));
        onUseStart(spell, item, slot, time);
    }

    public void removeUsed(int slot) {
        used.remove(slot);
        onUseStop(slot);
    }

    public void unlockSpell(Holder<Spell> spell) {
        unlockedSpells.add(spell);
    }

    public void lockSpell(Holder<Spell> spell) {
        unlockedSpells.remove(spell);
    }

    public void unlockSpells(Collection<? extends Holder<Spell>> spells) {
        unlockedSpells.addAll(spells);
    }

    public void lockAllSpells() {
        unlockedSpells.clear();
    }

    protected void onCooldownStart(Identifier key, int duration) {}

    protected void onCooldownEnded(Identifier key) {}

    protected void onUseStart(Holder<Spell> spell, Holder<Item> item, int slot, int duration) {}

    protected void onUseFinish(int slot, Holder<Spell> spell, ItemStack itemStack, boolean forceLocked) {}

    protected void onUseStop(int slot) {}

    public boolean isSpellOpened(Holder<Spell> spellHolder) {
        return unlockedSpells.contains(spellHolder);
    }

    private Map<Identifier, CooldownInstance> relativeCooldowns() {
        return cooldowns.entrySet().stream().collect(Collectors.toMap(
                Map.Entry::getKey,
                entry -> new CooldownInstance(
                        entry.getValue().startTime() - tickCount,
                        entry.getValue().endTime() - tickCount
                )));
    }

    private Map<Integer, CastInstance> relativeUsed() {
        return used.entrySet().stream().collect(Collectors.toMap(
                Map.Entry::getKey,
                entry -> new CastInstance(
                        entry.getValue().item(),
                        entry.getValue().spell(),
                        entry.getValue().forceLocked(),
                        entry.getValue().startTime() - tickCount,
                        entry.getValue().endTime() - tickCount
                )
        ));
    }



    public void startCast(@NotNull Level level, @NotNull Player player, @NotNull ItemStack itemStack, Runnable onStart, Holder<Spell> spell, int slotButton, boolean forceOpen) {
        if (spell.value().casting().castTime() <= 0) {
            if (level instanceof ServerLevel serverLevel) {
                castSpell(0, serverLevel, player, itemStack, spell, forceOpen);
            }
            return;
        }
        if (cantUse(spell) || used.containsKey(slotButton) || level.isClientSide()) return;
        addUsed(spell, BuiltInRegistries.ITEM.wrapAsHolder(itemStack.getItem()), forceOpen, slotButton, spell.value().casting().castTime());
        onStart.run();
        //return level instanceof ServerLevel && afterStartCasting();
    }


    public void castSpell(int tickRemains, ServerLevel level, LivingEntity caster, ItemStack wandItem, Holder<Spell> spell, boolean forceOpen) {
        if (canCast(tickRemains, spell)) {
            SpellAction.run(level, forceOpen || unlockedSpells.contains(spell) ? spell.value().main() : spell.value().broken(), caster, wandItem);
            afterCast(spell);
        }
    }

    private boolean canCast(int tickRemains, Holder<Spell> spell) {
        if ((spell.value().casting().singleUse()) || cantUse(spell)) return false;

        if (spell.value().casting().castTime() <= 0) return true;

        float castTimePercent = (float) tickRemains / spell.value().casting().castTime();
        return !(castTimePercent < 1);
    }

    public boolean cantUse(Holder<Spell> spell) {
        return spell.unwrapKey().map(key -> cooldowns.containsKey(key.identifier())).orElse(false);
    }

    protected void afterCast(Holder<Spell> spell) {
        if (spell.value().casting().rechargeTime() > 0) {
            spell.unwrapKey().ifPresent(key -> addCooldown(key.identifier(), spell.value().casting().rechargeTime()));
        }
    }

    public void readData(ValueInput input) {
        cooldowns.clear();
        used.clear();
        unlockedSpells.clear();
        tickCount = 0;
        input.read("cooldowns", COOLDOWN_MAP_CODEC).ifPresent(cooldowns::putAll);
        input.read("cast_used", CAST_MAP_CODEC).ifPresent(used::putAll);
        input.read("unlocked", UNLOCK_CODEC).ifPresent(unlockedSpells::addAll);
    }

    public void writeData(ValueOutput output) {
        output.store("cooldowns", COOLDOWN_MAP_CODEC, cooldowns);
        output.store("cast_used", CAST_MAP_CODEC, used);
        output.store("unlocked", UNLOCK_CODEC, unlockedSpells);
    }


    private record CooldownInstance(int startTime, int endTime) {}

    private record CastInstance(Holder<Item> item, Holder<Spell> spell, boolean forceLocked, int startTime, int endTime) {}

    private static final Codec<CooldownInstance> COOLDOWN_CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.INT.fieldOf("start_time").forGetter(CooldownInstance::startTime),
            Codec.INT.fieldOf("end_time").forGetter(CooldownInstance::endTime)
    ).apply(instance, CooldownInstance::new));

    private static final Codec<Map<Identifier, CooldownInstance>> COOLDOWN_MAP_CODEC = Codec.unboundedMap(Identifier.CODEC, COOLDOWN_CODEC);


    private static final Codec<CastInstance> CAST_CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Item.CODEC.fieldOf("item").forGetter(CastInstance::item),
            Spell.HOLDER_CODEC.fieldOf("spell").forGetter(CastInstance::spell),
            Codec.BOOL.fieldOf("foreLocked").forGetter(CastInstance::forceLocked),
            Codec.INT.fieldOf("start_time").forGetter(CastInstance::startTime),
            Codec.INT.fieldOf("end_time").forGetter(CastInstance::endTime)
    ).apply(instance, CastInstance::new));

    private static final Codec<Map<Integer, CastInstance>> CAST_MAP_CODEC = Codec.unboundedMap(Codec.INT, CAST_CODEC);

    private static final Codec<Set<Holder<Spell>>> UNLOCK_CODEC = Spell.HOLDER_CODEC.listOf().xmap(HashSet::new, ArrayList::new);

    public static final Codec<CastHolder> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            COOLDOWN_MAP_CODEC.fieldOf("cooldowns").forGetter(CastHolder::relativeCooldowns),
            CAST_MAP_CODEC.fieldOf("casts").forGetter(CastHolder::relativeUsed),
            UNLOCK_CODEC.fieldOf("unlocked").forGetter(ch -> ch.unlockedSpells)
    ).apply(instance, CastHolder::new));

    public static class ServerCastHolder extends CastHolder {
        private final ServerPlayer player;

        public ServerCastHolder(ServerPlayer player) {
            this.player = player;
        }

        @Override
        protected void onCooldownStart(Identifier key, int duration) {
            super.onCooldownStart(key, duration);
            ServerPlayNetworking.send(player, new SpellCooldownPayload(key, duration));
        }

        @Override
        protected void onCooldownEnded(Identifier key) {
            super.onCooldownEnded(key);
            ServerPlayNetworking.send(player, new SpellCooldownPayload(key, -1));
        }

        @Override
        protected void onUseStart(Holder<Spell> spell, Holder<Item> item, int slot, int duration) {
            super.onUseStart(spell, item, slot, duration);
            ServerPlayNetworking.send(player, new SpellCastPayload(spell, item, slot, duration));
        }

        @Override
        protected void onUseFinish(int slot, Holder<Spell> spell, ItemStack itemStack, boolean forceLocked) {
            super.onUseFinish(slot, spell, itemStack, forceLocked);/*
            spell.value().props()
                    .ifPresentOrElse(projectileProps -> SpellProjectile.cast(player.level(), player, itemStack, spell, projectileProps),
                            () -> SpellAction.run(player.level(), spell.value().spellAction(), player, itemStack)); //spell.value().runActions(new SpellContext(player.level(), player)));*/
            SpellAction.run(player.level(), forceLocked || isSpellOpened(spell) ? spell.value().main() : spell.value().broken(), player, itemStack);

            afterCast(spell);
            ServerPlayNetworking.send(player, new CastSpellPayload(slot, false));
        }

        @Override
        protected void onUseStop(int slot) {
            super.onUseStop(slot);
            ServerPlayNetworking.send(player, new CastSpellPayload(slot, false));
        }
    }
}
