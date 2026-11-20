package net.smok.om.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.renderer.entity.EntityRenderers;
import net.minecraft.client.renderer.item.properties.select.SelectItemModelProperties;
import net.smok.om.Values;
import net.smok.om.client.render.SpellProperty;
import net.smok.om.client.render.entity.SpellProjectileRenderer;
import net.smok.om.client.render.hud.WandSpellsRenderer;
import net.smok.om.client.render.screens.Screens;
import net.smok.om.client.render.screens.WandCraftTableScreen;
import net.smok.om.entity.OMEntities;
import net.smok.om.menu.OMMenus;
import net.smok.om.network.*;
import net.smok.om.spell.cast.PlayerCaster;
import net.smok.om.utils.BlockParticleTemplate;

public class OldMagicClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {

        KeyBinds.init();

        ClientTickEvents.END_CLIENT_TICK.register(new KeyBinds());
        HudElementRegistry.addFirst(Values.id("wand_spells"), new WandSpellsRenderer(Minecraft.getInstance()));

        EntityRenderers.register(OMEntities.SPELL_PROJECTILE, SpellProjectileRenderer::new);

        Screens.init(Minecraft.getInstance());

        MenuScreens.register(OMMenus.WANDCRAFT_TABLE, WandCraftTableScreen::new);
        SelectItemModelProperties.ID_MAPPER.put(Values.id("spell"), SpellProperty.TYPE);



        ClientPlayNetworking.registerGlobalReceiver(SpellCooldownPayload.TYPE, (spellCooldownPayload, context) -> {
            if (context.player() instanceof PlayerCaster caster) {
                if (spellCooldownPayload.duration() > 0)
                    caster.oldMagic$getCastHolder().addCooldown(spellCooldownPayload.identifier(), spellCooldownPayload.duration());
                else caster.oldMagic$getCastHolder().removeCooldown(spellCooldownPayload.identifier());
            }
        });

        ClientPlayNetworking.registerGlobalReceiver(SpellCastPayload.TYPE, (payload, context) -> {
            if (context.player() instanceof PlayerCaster caster) {
                caster.oldMagic$getCastHolder().addUsed(payload.spell(), payload.item(), false, payload.slot(), payload.duration());
            }
        });

        ClientPlayNetworking.registerGlobalReceiver(CastSpellPayload.TYPE, (payload, context) -> {
            if (context.player() instanceof PlayerCaster caster) {
                if (!payload.startOrRelease()) caster.oldMagic$getCastHolder().removeUsed(payload.spellSlot());
            }
        });

        ClientPlayNetworking.registerGlobalReceiver(BlockParticleTemplate.TYPE,
                (blockParticleTemplate, context) ->
                        blockParticleTemplate.addParticleClient(context.client().level));


        ClientPlayNetworking.registerGlobalReceiver(EnableToggleSpellPayload.TYPE, (enableToggleSpellPayload, context) -> {
            if (context.player() instanceof PlayerCaster caster)
                caster.oldMagic$enableToggle(enableToggleSpellPayload.spellHolder(), enableToggleSpellPayload.delay(), enableToggleSpellPayload.item());
        });

        ClientPlayNetworking.registerGlobalReceiver(DisableToggleSpellPayload.TYPE, (toggleSpellPayload, context) -> {
            if (context.player() instanceof PlayerCaster caster)
                caster.oldMagic$disableToggle(toggleSpellPayload.spellHolder());
        });

	}
}