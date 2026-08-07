package lekavar.lma.drinkbeer.neoforge;

import lekavar.lma.drinkbeer.DrinkBeerConfig;
import net.neoforged.neoforge.common.ModConfigSpec;

final class DrinkBeerConfigNeoForge {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    private static final ModConfigSpec.DoubleValue BEER_SATURATION_MODIFIER = BUILDER
            .comment(
                    "Saturation modifier used by ordinary and mixed beer.",
                    "The improved default is 0.1. Set to 0.0 for exact upstream 4.0 no-saturation behavior."
            )
            .defineInRange(
                    "beerSaturationModifier",
                    DrinkBeerConfig.DEFAULT_BEER_SATURATION_MODIFIER,
                    0.0D,
                    1.0D
            );

    private static final ModConfigSpec.BooleanValue ENABLE_WORLD_CHANGING_FLAVOR_EFFECTS = BUILDER
            .comment(
                    "Allow Stormy and Drying mixed-beer flavors to modify blocks in the world.",
                    "Disable this on protected or adventure-focused servers to prevent flavor-based griefing."
            )
            .define(
                    "enableWorldChangingFlavorEffects",
                    DrinkBeerConfig.DEFAULT_ENABLE_WORLD_CHANGING_FLAVOR_EFFECTS
            );

    private static final ModConfigSpec.IntValue MAX_WORLD_CHANGES_PER_DRINK = BUILDER
            .comment(
                    "Maximum blocks that one mixed beer may destroy or remove.",
                    "Unloaded chunks are never loaded by a flavor effect. Set to 0 to suppress block changes."
            )
            .defineInRange(
                    "maxWorldChangesPerDrink",
                    DrinkBeerConfig.DEFAULT_MAX_WORLD_CHANGES_PER_DRINK,
                    0,
                    32768
            );

    static final ModConfigSpec SPEC = BUILDER.build();

    static void install() {
        DrinkBeerConfig.install(DrinkBeerConfigNeoForge::values);
    }

    private static DrinkBeerConfig.Values values() {
        try {
            return new DrinkBeerConfig.Values(
                    BEER_SATURATION_MODIFIER.get(),
                    ENABLE_WORLD_CHANGING_FLAVOR_EFFECTS.get(),
                    MAX_WORLD_CHANGES_PER_DRINK.get()
            );
        } catch (IllegalStateException exception) {
            return DrinkBeerConfig.defaults();
        }
    }

    private DrinkBeerConfigNeoForge() {
    }
}
