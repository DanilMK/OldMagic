package net.smok.om.client.render.hud;

import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElement;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.core.Holder;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.smok.om.Values;
import net.smok.om.client.render.RenderUtils;
import net.smok.om.item.OMDataComponents;
import net.smok.om.item.WandContents;
import net.smok.om.spell.*;
import net.smok.om.spell.cast.CastHolder;
import net.smok.om.spell.cast.PlayerCaster;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.atomic.AtomicInteger;


public record WandSpellsRenderer(Minecraft minecraft) implements HudElement {

    private static final Identifier BACKGROUND = Values.id("textures/gui/spell_slot.png");
    private static final int BACKGROUND_SIZE = 20;


    // todo make changeable properties
    @Override
    public void extractRenderState(GuiGraphicsExtractor guiGraphicsExtractor, DeltaTracker deltaTracker) {

        if (minecraft.player instanceof PlayerCaster caster) {
            caster.forWandItem((player, hand, _) -> {
                ItemStack itemStack = player.getItemInHand(hand);


                WandContents spellStacks = itemStack.get(OMDataComponents.WAND_CONTENTS);
                if (spellStacks != null) {
                    int x = guiGraphicsExtractor.guiWidth() / 2 - 200 - spellStacks.spells().size();
                    int y = guiGraphicsExtractor.guiHeight() - BACKGROUND_SIZE - 1;
                    renderSpellList(guiGraphicsExtractor, x, y, spellStacks, caster.oldMagic$getCastHolder(), deltaTracker.getGameTimeDeltaPartialTick(false));
                }
            });
        }
    }

    private void renderSpellList(GuiGraphicsExtractor graphics, int x, int y, WandContents spellStacks, CastHolder castHolder, float partialTick) {
        graphics.outline(x - 1, y - 1, BACKGROUND_SIZE * spellStacks.spells().size() + 2, BACKGROUND_SIZE + 2, 0xFF000000);
        AtomicInteger atomicX = new AtomicInteger(x);
        int prevButtons = 0; // to prevent draw cast percent on slots with same buttons
        for (int i = 0; i < spellStacks.spells().size(); i++) {
            int button = spellStacks.spells().get(i).button();
            renderSpell(graphics, atomicX.get(), y, (prevButtons & 1 << button) > 0 ? -1 : button, spellStacks.spells().get(i).spell(), castHolder, partialTick);
            prevButtons = prevButtons | 1 << button;
            atomicX.set(atomicX.get() + BACKGROUND_SIZE);
        }
    }


    private static void renderSpell(GuiGraphicsExtractor graphics, int x, int y, int slotButton, @Nullable Holder<Spell> spellStack, CastHolder castHolder, float partialTick) {
        graphics.blit(RenderPipelines.GUI_TEXTURED, BACKGROUND, x, y, 0, 0, BACKGROUND_SIZE, BACKGROUND_SIZE, BACKGROUND_SIZE, BACKGROUND_SIZE);
        if (spellStack != null) {
            int x1 = x + BACKGROUND_SIZE / 2 - 8;
            int y1 = y + BACKGROUND_SIZE / 2 - 8;

            RenderUtils.renderSpell(graphics, spellStack, x1, y1);

            float cooldownPercent = castHolder.getCastPercent(slotButton, partialTick) - castHolder.getCooldownPercent(spellStack, partialTick);
            if (cooldownPercent > 0) {
                graphics.fill(x1, (int) (y1 + cooldownPercent * 16), x1 + 16, y1 + 16, 0x77FFFFFF);
            }
        }
    }

}
