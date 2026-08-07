package lekavar.lma.drinkbeer.registries;

import lekavar.lma.drinkbeer.DrinkBeer;
import lekavar.lma.drinkbeer.platform.Registration;
import lekavar.lma.drinkbeer.platform.RegistryProvider;
import lekavar.lma.drinkbeer.recipes.BrewingRecipe;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;

import java.util.function.Supplier;

public class RecipeRegistry {
    private static final RegistryProvider<RecipeType<?>> RECIPE_TYPES = Registration.provider(BuiltInRegistries.RECIPE_TYPE);
    public static final Supplier<RecipeType<BrewingRecipe>> RECIPE_TYPE_BREWING = RECIPE_TYPES.register("brewing", () -> new RecipeType<>() {
        @Override
        public String toString() {
            return DrinkBeer.MOD_ID + ":brewing";
        }
    });
    private static final RegistryProvider<RecipeSerializer<?>> RECIPE_SERIALIZERS = Registration.provider(BuiltInRegistries.RECIPE_SERIALIZER);
    public static final Supplier<RecipeSerializer<BrewingRecipe>> RECIPE_SERIALIZER_BREWING = RECIPE_SERIALIZERS.register("brewing", BrewingRecipe.Serializer::new);

    public static void init() {
    }
}
