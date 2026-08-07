package lekavar.lma.drinkbeer;

import lekavar.lma.drinkbeer.networking.NetWorking;
import lekavar.lma.drinkbeer.registries.*;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(DrinkBeer.MOD_ID)
public class DrinkBeer {

    public static final String MOD_ID = "drinkbeer";

    public DrinkBeer() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        MobEffectRegistry.STATUS_EFFECTS.register(modEventBus);
        ItemRegistry.ITEMS.register(modEventBus);
        BlockRegistry.BLOCKS.register(modEventBus);
        BlockEntityRegistry.BLOKC_ENTITIES.register(modEventBus);
        SoundEventRegistry.SOUNDS.register(modEventBus);
        MenuTypeRegistry.MENUS.register(modEventBus);
        RecipeRegistry.RECIPE_TYPES.register(modEventBus);
        RecipeRegistry.RECIPE_SERIALIZERS.register(modEventBus);
        ParticleTypeRegistry.PARTICLES.register(modEventBus);
        CreativeTabRegistry.TABS.register(modEventBus);
        NetWorking.init();
        ModLoadingContext.get().registerConfig(ModConfig.Type.SERVER, DrinkBeerConfig.SPEC);
    }

}
