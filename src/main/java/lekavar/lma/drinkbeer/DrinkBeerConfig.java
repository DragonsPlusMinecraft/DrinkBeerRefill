package lekavar.lma.drinkbeer;

import net.minecraftforge.common.ForgeConfigSpec;

public final class DrinkBeerConfig {
    private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();

    public static final double DEFAULT_BEER_SATURATION_MODIFIER = 0.1D;

    public static final ForgeConfigSpec.DoubleValue BEER_SATURATION_MODIFIER = BUILDER
            .comment(
                    "Saturation modifier used by ordinary and mixed beer.",
                    "The improved default is 0.1. Set to 0.0 for exact upstream 4.0 no-saturation behavior."
            )
            .defineInRange("beerSaturationModifier", DEFAULT_BEER_SATURATION_MODIFIER, 0.0D, 1.0D);

    public static final ForgeConfigSpec.BooleanValue ENABLE_WORLD_CHANGING_FLAVOR_EFFECTS = BUILDER
            .comment(
                    "Allow Stormy and Drying mixed-beer flavors to modify blocks in the world.",
                    "Disable this on protected or adventure-focused servers to prevent flavor-based griefing."
            )
            .define("enableWorldChangingFlavorEffects", true);

    public static final ForgeConfigSpec.IntValue MAX_WORLD_CHANGES_PER_DRINK = BUILDER
            .comment(
                    "Maximum blocks that one mixed beer may destroy or remove.",
                    "Unloaded chunks are never loaded by a flavor effect. Set to 0 to suppress block changes."
            )
            .defineInRange("maxWorldChangesPerDrink", 4096, 0, 32768);

    public static final ForgeConfigSpec SPEC = BUILDER.build();

    public static float beerSaturationModifier() {
        try {
            return BEER_SATURATION_MODIFIER.get().floatValue();
        } catch (IllegalStateException ignored) {
            return (float) DEFAULT_BEER_SATURATION_MODIFIER;
        }
    }

    private DrinkBeerConfig() {
    }
}
