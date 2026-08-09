package lekavar.lma.drinkbeer.compat.jei;

import lekavar.lma.drinkbeer.recipes.BrewingRecipe;

import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

/**
 * Loader bridge for JEI's 1.21.11 server-synchronized recipe map.
 * This class is only reached by JEI's optional plugin entry point.
 */
public final class JeiRecipeSource {
    private static Supplier<List<BrewingRecipe>> source = List::of;

    public static synchronized void install(Supplier<List<BrewingRecipe>> recipeSource) {
        source = Objects.requireNonNull(recipeSource, "recipeSource");
    }

    public static List<BrewingRecipe> getBrewingRecipes() {
        return List.copyOf(source.get());
    }

    private JeiRecipeSource() {
    }
}
