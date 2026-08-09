package lekavar.lma.drinkbeer.fabric;

import lekavar.lma.drinkbeer.DrinkBeer;
import lekavar.lma.drinkbeer.registries.RecipeRegistry;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.recipe.v1.sync.RecipeSynchronization;

public final class FabricDrinkBeer implements ModInitializer {
    @Override
    public void onInitialize() {
        DrinkBeer.initialize(new FabricPlatform());
        RecipeSynchronization.synchronizeRecipeSerializer(
                RecipeRegistry.RECIPE_SERIALIZER_BREWING.get()
        );
    }
}
