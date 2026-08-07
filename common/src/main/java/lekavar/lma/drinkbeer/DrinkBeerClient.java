package lekavar.lma.drinkbeer;

import lekavar.lma.drinkbeer.platform.ClientPlatformHooks;

public final class DrinkBeerClient {
    public static void initialize(ClientPlatformHooks hooks) {
        hooks.initializeClient();
    }

    private DrinkBeerClient() {
    }
}
