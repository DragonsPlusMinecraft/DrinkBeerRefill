package lekavar.lma.drinkbeer.registries;

import lekavar.lma.drinkbeer.DrinkBeer;
import lekavar.lma.drinkbeer.gui.BeerBarrelMenu;
import lekavar.lma.drinkbeer.gui.TradeBoxMenu;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.registries.DeferredRegister;

import java.util.function.Supplier;


public class MenuTypeRegistry {
    public static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(Registries.MENU, DrinkBeer.MOD_ID);
    public static final Supplier<MenuType<BeerBarrelMenu>> beerBarrelContainer = MENUS.register("beer_barrel_container", () -> IForgeMenuType.create(BeerBarrelMenu::new));
    public static final Supplier<MenuType<TradeBoxMenu>> tradeBoxContainer = MENUS.register("trade_box_normal_container", () -> IForgeMenuType.create(TradeBoxMenu::new));

}
