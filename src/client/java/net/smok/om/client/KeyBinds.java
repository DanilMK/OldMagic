package net.smok.om.client;

import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.smok.om.Values;
import net.smok.om.spell.cast.PlayerCaster;

public class KeyBinds implements ClientTickEvents.EndTick {


    // todo localize
    public static final KeyMapping.Category CATEGORY = KeyMapping.Category.register(Values.id("main"));
    public static final SpellCastKeyMapping SLOT_0 = (SpellCastKeyMapping) KeyMappingHelper.registerKeyMapping(new SpellCastKeyMapping(
            "key.spell_slot_0",
            InputConstants.Type.KEYSYM,
            InputConstants.KEY_G,
            CATEGORY,
            0
    ));

    public static final SpellCastKeyMapping SLOT_1 = (SpellCastKeyMapping) KeyMappingHelper.registerKeyMapping(new SpellCastKeyMapping(
            "key.spell_slot_1",
            InputConstants.Type.KEYSYM,
            InputConstants.KEY_H,
            CATEGORY,
            1
    ));

    public static final SpellCastKeyMapping SLOT_2 = (SpellCastKeyMapping) KeyMappingHelper.registerKeyMapping(new SpellCastKeyMapping(
            "key.spell_slot_2",
            InputConstants.Type.KEYSYM,
            InputConstants.KEY_J,
            CATEGORY,
            2
    ));

    public static final SpellCastKeyMapping SLOT_3 = (SpellCastKeyMapping) KeyMappingHelper.registerKeyMapping(new SpellCastKeyMapping(
            "key.spell_slot_3",
            InputConstants.Type.KEYSYM,
            InputConstants.KEY_K,
            CATEGORY,
            3
    ));

    public static void init() {

    }


    @Override
    public void onEndTick(Minecraft client) {
        if (client.player == null || client.gameMode == null) return;
        if (client.player instanceof PlayerCaster caster) {
            SLOT_0.onTick(caster);
            SLOT_1.onTick(caster);
            SLOT_2.onTick(caster);
            SLOT_3.onTick(caster);
        }
    }

    public static class SpellCastKeyMapping extends KeyMapping {

        private final int slot;


        public SpellCastKeyMapping(String name, InputConstants.Type type, int value, Category category, int slot) {
            super(name, type, value, category);
            this.slot = slot;
        }

        public void onTick(PlayerCaster caster) {
            if (caster.oldMagic$isCasting(slot)) {
                if (!isDown()) caster.oldMagic$releaseCast(slot);
                //noinspection StatementWithEmptyBody
                while (consumeClick()) {}
            } else {
                while (consumeClick()) caster.oldMagic$startCast(slot);
                if (isDown()) caster.oldMagic$startCast(slot);
            }
        }
    }
}
