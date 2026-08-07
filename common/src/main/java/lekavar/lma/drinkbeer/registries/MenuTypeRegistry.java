package lekavar.lma.drinkbeer.registries;

import lekavar.lma.drinkbeer.gui.BeerBarrelMenu;
import lekavar.lma.drinkbeer.gui.TradeBoxMenu;
import lekavar.lma.drinkbeer.platform.Registration;
import net.minecraft.world.inventory.MenuType;

import java.util.function.Supplier;

public final class MenuTypeRegistry {
    public static final Supplier<MenuType<BeerBarrelMenu>> beerBarrelContainer = Registration.registerMenu(
            "beer_barrel_container",
            BeerBarrelMenu::new
    );
    public static final Supplier<MenuType<TradeBoxMenu>> tradeBoxContainer = Registration.registerMenu(
            "trade_box_normal_container",
            TradeBoxMenu::new
    );

    public static void init() {
    }

    private MenuTypeRegistry() {
    }
}
