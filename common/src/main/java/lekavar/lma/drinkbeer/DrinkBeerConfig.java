package lekavar.lma.drinkbeer;

import java.util.Objects;

public final class DrinkBeerConfig {
    public static final double DEFAULT_BEER_SATURATION_MODIFIER = 0.1D;
    public static final boolean DEFAULT_ENABLE_WORLD_CHANGING_FLAVOR_EFFECTS = true;
    public static final int DEFAULT_MAX_WORLD_CHANGES_PER_DRINK = 4096;

    private static final Values DEFAULTS = new Values(
            DEFAULT_BEER_SATURATION_MODIFIER,
            DEFAULT_ENABLE_WORLD_CHANGING_FLAVOR_EFFECTS,
            DEFAULT_MAX_WORLD_CHANGES_PER_DRINK
    );

    private static volatile ConfigAccess access = () -> DEFAULTS;

    public static void install(ConfigAccess configAccess) {
        access = Objects.requireNonNull(configAccess, "configAccess");
    }

    public static Values defaults() {
        return DEFAULTS;
    }

    public static float beerSaturationModifier() {
        return (float) access.values().beerSaturationModifier();
    }

    public static boolean enableWorldChangingFlavorEffects() {
        return access.values().enableWorldChangingFlavorEffects();
    }

    public static int maxWorldChangesPerDrink() {
        return access.values().maxWorldChangesPerDrink();
    }

    @FunctionalInterface
    public interface ConfigAccess {
        Values values();
    }

    public record Values(
            double beerSaturationModifier,
            boolean enableWorldChangingFlavorEffects,
            int maxWorldChangesPerDrink
    ) {
        public Values {
            beerSaturationModifier = Double.isFinite(beerSaturationModifier)
                    ? Math.clamp(beerSaturationModifier, 0.0D, 1.0D)
                    : DEFAULT_BEER_SATURATION_MODIFIER;
            maxWorldChangesPerDrink = Math.clamp(maxWorldChangesPerDrink, 0, 32768);
        }
    }

    private DrinkBeerConfig() {
    }
}
