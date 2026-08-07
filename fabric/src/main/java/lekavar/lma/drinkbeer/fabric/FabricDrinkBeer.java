package lekavar.lma.drinkbeer.fabric;

import lekavar.lma.drinkbeer.DrinkBeer;
import net.fabricmc.api.ModInitializer;

public final class FabricDrinkBeer implements ModInitializer {
    @Override
    public void onInitialize() {
        DrinkBeer.initialize(new FabricPlatform());
    }
}
