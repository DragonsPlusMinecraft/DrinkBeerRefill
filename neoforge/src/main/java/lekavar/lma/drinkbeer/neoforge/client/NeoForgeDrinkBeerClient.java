package lekavar.lma.drinkbeer.neoforge.client;

import lekavar.lma.drinkbeer.DrinkBeer;
import lekavar.lma.drinkbeer.DrinkBeerClient;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;

@Mod(value = DrinkBeer.MOD_ID, dist = Dist.CLIENT)
public final class NeoForgeDrinkBeerClient {
    public NeoForgeDrinkBeerClient(IEventBus modEventBus) {
        DrinkBeerClient.initialize(new NeoForgeClientPlatform(modEventBus));
    }
}
