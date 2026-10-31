package net.smok.om.menu;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.smok.om.Values;

public interface OMMenus {

    MenuType<WandCraftTableMenu> WANDCRAFT_TABLE = register("wandcraft_table", WandCraftTableMenu::new);


    private static <T extends AbstractContainerMenu> MenuType<T> register(String path, MenuType.MenuSupplier<T> factory) {
        return Registry.register(BuiltInRegistries.MENU, Values.id(path),  new MenuType<>(factory, FeatureFlagSet.of()));
    }

    static void init() {}
}
