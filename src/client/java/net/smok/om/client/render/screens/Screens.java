package net.smok.om.client.render.screens;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;

public class Screens {

    private static final HashMap<String, PlayerScreenFactory> SCREENS_IN_ITEM = new HashMap<>();
    private static Minecraft MINECRAFT;

    public static void init(Minecraft minecraft) {
        SCREENS_IN_ITEM.put("spell_book", SpellBookScreen::new);
        MINECRAFT = minecraft;
    }

    public static void open(String key, Player player, ItemStack itemStack, InteractionHand hand) {
        if (SCREENS_IN_ITEM.containsKey(key)) MINECRAFT.setScreen(SCREENS_IN_ITEM.get(key).openScreen(player, itemStack, hand));
    }

    public interface PlayerScreenFactory {

        @Contract @NotNull
        Screen openScreen(final Player player, final ItemStack itemStack, final InteractionHand hand);
    }
}
