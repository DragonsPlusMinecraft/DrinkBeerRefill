package lekavar.lma.drinkbeer.registries;

import lekavar.lma.drinkbeer.DrinkBeer;
import lekavar.lma.drinkbeer.platform.Platform;
import lekavar.lma.drinkbeer.platform.PlatformHooks.FluidPair;
import lekavar.lma.drinkbeer.utils.beer.Beers;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluid;

import java.util.List;
import java.util.Optional;

public final class FluidRegistry {
    /** One mug is one quarter of a bucket, matching the established drink-fluid convention. */
    public static final int SERVING_MILLIBUCKETS = 250;

    private static final List<BeerFluid> BEERS = List.of(
            new BeerFluid("beer", 0xFFF2B233, Appearance.WATERY, Beers.BEER_MUG),
            new BeerFluid("blaze_stout", 0xFF6B260F, Appearance.WATERY, Beers.BEER_MUG_BLAZE_STOUT),
            new BeerFluid("blaze_milk_stout", 0xFFB56B55, Appearance.MILKY, Beers.BEER_MUG_BLAZE_MILK_STOUT),
            new BeerFluid("apple_lambic", 0xFFB56516, Appearance.WATERY, Beers.BEER_MUG_APPLE_LAMBIC),
            new BeerFluid("sweet_berry_kriek", 0xFFB8434B, Appearance.WATERY, Beers.BEER_MUG_SWEET_BERRY_KRIEK),
            new BeerFluid("haars_icey_pale_lager", 0xFFD1A142, Appearance.WATERY, Beers.BEER_MUG_HAARS_ICEY_PALE_LAGER),
            new BeerFluid("pumpkin_kvass", 0xFFC96A0B, Appearance.WATERY, Beers.BEER_MUG_PUMPKIN_KVASS),
            new BeerFluid("frothy_pink_eggnog", 0xFFF2B8B2, Appearance.MILKY, Beers.BEER_MUG_FROTHY_PINK_EGGNOG),
            new BeerFluid("night_howl_kvass", 0xFF80615D, Appearance.WATERY, Beers.BEER_MUG_NIGHT_HOWL_KVASS)
    );

    public static void init() {
        BEERS.forEach(BeerFluid::register);
    }

    public static List<BeerFluid> beers() {
        return BEERS;
    }

    public static Optional<BeerFluid> byFluid(Fluid fluid) {
        return BEERS.stream().filter(beer -> beer.matches(fluid)).findFirst();
    }

    public static Optional<BeerFluid> byFilledMug(Item item) {
        return BEERS.stream().filter(beer -> beer.filledMug() == item).findFirst();
    }

    public enum Appearance {
        WATERY,
        MILKY
    }

    public static final class BeerFluid {
        private final String path;
        private final int tintColor;
        private final Appearance appearance;
        private final Beers beer;
        private FluidPair registration;

        private BeerFluid(String path, int tintColor, Appearance appearance, Beers beer) {
            this.path = path;
            this.tintColor = tintColor;
            this.appearance = appearance;
            this.beer = beer;
        }

        private void register() {
            if (registration != null) {
                throw new IllegalStateException("Fluid already registered: " + path);
            }
            registration = Platform.hooks().registerFluidPair(path);
        }

        public String path() {
            return path;
        }

        public String flowingPath() {
            return "flowing_" + path;
        }

        public String translationKey() {
            return "fluid_type." + DrinkBeer.MOD_ID + "." + path;
        }

        public int tintColor() {
            return tintColor;
        }

        public Appearance appearance() {
            return appearance;
        }

        public Item filledMug() {
            return beer.getBeerItem();
        }

        public FlowingFluid source() {
            return registered().source().get();
        }

        public FlowingFluid flowing() {
            return registered().flowing().get();
        }

        public boolean matches(Fluid fluid) {
            return fluid == source() || fluid == flowing();
        }

        private FluidPair registered() {
            if (registration == null) {
                throw new IllegalStateException("Fluid has not been registered: " + path);
            }
            return registration;
        }
    }

    private FluidRegistry() {
    }
}
