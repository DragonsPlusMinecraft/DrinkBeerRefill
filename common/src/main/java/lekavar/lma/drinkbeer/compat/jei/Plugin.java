package lekavar.lma.drinkbeer.compat.jei;

import lekavar.lma.drinkbeer.DrinkBeer;
import lekavar.lma.drinkbeer.gui.BeerBarrelMenu;
import lekavar.lma.drinkbeer.gui.BeerBarrelScreen;
import lekavar.lma.drinkbeer.registries.BlockRegistry;
import lekavar.lma.drinkbeer.registries.MenuTypeRegistry;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.registration.*;
import mezz.jei.api.runtime.IJeiRuntime;
import net.minecraft.resources.Identifier;

@JeiPlugin
public class Plugin implements IModPlugin {
    private static volatile IJeiRuntime runtime;

    @Override
    public Identifier getPluginUid() {
        return Identifier.fromNamespaceAndPath(DrinkBeer.MOD_ID, "jei_plugin");
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        registration.addRecipeCategories(new JEIBrewingRecipeCategory(registration.getJeiHelpers().getGuiHelper()));
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        registration.addRecipes(JEIBrewingRecipeCategory.TYPE, JeiRecipeSource.getBrewingRecipes());
    }

    @Override
    public void registerRecipeTransferHandlers(IRecipeTransferRegistration registration) {
        registration.addRecipeTransferHandler(BeerBarrelMenu.class, MenuTypeRegistry.beerBarrelContainer.get(), JEIBrewingRecipeCategory.TYPE, 36, 4, 0, 36);
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        registration.addCraftingStation(JEIBrewingRecipeCategory.TYPE, BlockRegistry.BEER_BARREL.get());
    }

    @Override
    public void registerGuiHandlers(IGuiHandlerRegistration registration) {
        registration.addRecipeClickArea(BeerBarrelScreen.class, 90, 31, 37, 22, JEIBrewingRecipeCategory.TYPE);
    }

    @Override
    public void onRuntimeAvailable(IJeiRuntime jeiRuntime) {
        runtime = jeiRuntime;
        DrinkBeer.LOGGER.info("Drink Beer Refill JEI runtime is available");
    }

    @Override
    public void onRuntimeUnavailable() {
        runtime = null;
        DrinkBeer.LOGGER.info("Drink Beer Refill JEI runtime is unavailable");
    }

    static IJeiRuntime runtimeForTesting() {
        return runtime;
    }
}
