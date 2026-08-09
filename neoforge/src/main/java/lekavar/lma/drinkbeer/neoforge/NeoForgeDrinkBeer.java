package lekavar.lma.drinkbeer.neoforge;

import lekavar.lma.drinkbeer.DrinkBeer;
import lekavar.lma.drinkbeer.registries.RecipeRegistry;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.OnDatapackSyncEvent;

@Mod(DrinkBeer.MOD_ID)
public final class NeoForgeDrinkBeer {
    public NeoForgeDrinkBeer(IEventBus modEventBus, ModContainer modContainer) {
        registerDevelopmentGameTests(modEventBus);
        DrinkBeer.initialize(new NeoForgePlatform(modEventBus, modContainer));
        NeoForge.EVENT_BUS.addListener(NeoForgeDrinkBeer::sendBrewingRecipes);
    }

    /**
     * NeoForge only sends complete custom recipes when their types are requested
     * during datapack synchronization. JEI observes the resulting client event,
     * while the mod remains fully usable when JEI is not installed.
     */
    private static void sendBrewingRecipes(OnDatapackSyncEvent event) {
        event.sendRecipes(RecipeRegistry.RECIPE_TYPE_BREWING.get());
    }

    /**
     * The implementation lives exclusively in the gameTest source set, so it is
     * absent from release jars. Loading it here lets its deferred test-function
     * registry join the mod bus before registry events are fired.
     */
    private static void registerDevelopmentGameTests(IEventBus modEventBus) {
        try {
            Class<?> bootstrap = Class.forName("lekavar.lma.drinkbeer.gametest.NeoForgeHolidayGameTests");
            bootstrap.getMethod("registerFunctions", IEventBus.class).invoke(null, modEventBus);
        } catch (ClassNotFoundException ignored) {
            // Normal production runtime: the gameTest source set is not packaged.
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException("Unable to register DrinkBeer development GameTests", exception);
        }
    }
}
