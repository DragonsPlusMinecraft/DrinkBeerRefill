package lekavar.lma.drinkbeer.fabric.client;

import lekavar.lma.drinkbeer.DrinkBeerClient;
import net.fabricmc.api.ClientModInitializer;

public final class FabricDrinkBeerClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        DrinkBeerClient.initialize(new FabricClientPlatform());
    }
}
