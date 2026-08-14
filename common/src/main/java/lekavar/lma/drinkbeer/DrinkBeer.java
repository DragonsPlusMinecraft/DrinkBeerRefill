package lekavar.lma.drinkbeer;

import com.mojang.logging.LogUtils;
import lekavar.lma.drinkbeer.platform.Platform;
import lekavar.lma.drinkbeer.platform.PlatformHooks;
import lekavar.lma.drinkbeer.registries.BlockEntityRegistry;
import lekavar.lma.drinkbeer.registries.BlockRegistry;
import lekavar.lma.drinkbeer.registries.CreativeTabRegistry;
import lekavar.lma.drinkbeer.registries.DataComponentTypeRegistry;
import lekavar.lma.drinkbeer.registries.FluidRegistry;
import lekavar.lma.drinkbeer.registries.ItemRegistry;
import lekavar.lma.drinkbeer.registries.MenuTypeRegistry;
import lekavar.lma.drinkbeer.registries.MobEffectRegistry;
import lekavar.lma.drinkbeer.registries.ParticleTypeRegistry;
import lekavar.lma.drinkbeer.registries.RecipeRegistry;
import lekavar.lma.drinkbeer.registries.SoundEventRegistry;
import org.slf4j.Logger;

public final class DrinkBeer {
    public static final String MOD_ID = "drinkbeer";
    public static final Logger LOGGER = LogUtils.getLogger();

    public static void initialize(PlatformHooks hooks) {
        Platform.install(hooks);

        DataComponentTypeRegistry.init();
        MobEffectRegistry.init();
        SoundEventRegistry.init();
        ParticleTypeRegistry.init();
        BlockRegistry.init();
        ItemRegistry.init();
        FluidRegistry.init();
        BlockEntityRegistry.init();
        MenuTypeRegistry.init();
        RecipeRegistry.init();
        CreativeTabRegistry.init();

        hooks.registerNetworking();
        hooks.registerConfig();
    }

    private DrinkBeer() {
    }
}
