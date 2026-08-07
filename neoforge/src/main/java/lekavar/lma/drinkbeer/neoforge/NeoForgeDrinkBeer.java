package lekavar.lma.drinkbeer.neoforge;

import lekavar.lma.drinkbeer.DrinkBeer;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;

@Mod(DrinkBeer.MOD_ID)
public final class NeoForgeDrinkBeer {
    public NeoForgeDrinkBeer(IEventBus modEventBus, ModContainer modContainer) {
        DrinkBeer.initialize(new NeoForgePlatform(modEventBus, modContainer));
    }
}
