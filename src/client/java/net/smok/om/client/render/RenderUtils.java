package net.smok.om.client.render;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.core.Holder;
import net.minecraft.world.item.ItemStack;
import net.smok.om.item.OMDataComponents;
import net.smok.om.item.OMItems;
import net.smok.om.spell.Spell;

public class RenderUtils {

    public static void renderSpell(GuiGraphicsExtractor graphics, Holder<Spell> spell, int x, int y) {
        if (spell == null) return;
        ItemStack instance = OMItems.SPELL_PLACEHOLDER.getDefaultInstance();
        instance.set(OMDataComponents.SPELL_HOLDER, spell);
        graphics.fakeItem(instance, x, y);
    }

}
