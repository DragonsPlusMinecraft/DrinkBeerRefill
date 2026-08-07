package lekavar.lma.drinkbeer.gametest;

import lekavar.lma.drinkbeer.DrinkBeer;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(DrinkBeer.MOD_ID)
@PrefixGameTestTemplate(false)
public final class NeoForgeHolidayGameTests {
    @GameTest(template = "empty", timeoutTicks = 40)
    public static void giftOpensOnTheServerExactlyOnce(GameTestHelper helper) {
        HolidayGameTestScenarios.giftOpensOnTheServerExactlyOnce(helper);
    }

    @GameTest(template = "empty", timeoutTicks = 40)
    public static void ordinaryAndMixedEggnogEachGiveOneColoredGift(GameTestHelper helper) {
        HolidayGameTestScenarios.ordinaryAndMixedEggnogEachGiveOneColoredGift(helper);
    }

    @GameTest(template = "empty", timeoutTicks = 60)
    public static void horseModelPlacesTwoHalvesAndDropsOnlyOnce(GameTestHelper helper) {
        HolidayGameTestScenarios.horseModelPlacesTwoHalvesAndDropsOnlyOnce(helper);
    }

    @GameTest(template = "empty", timeoutTicks = 40)
    public static void beerTagFeedsBartendingValidation(GameTestHelper helper) {
        HolidayGameTestScenarios.beerTagFeedsBartendingValidation(helper);
    }

    @GameTest(template = "empty", timeoutTicks = 40)
    public static void ordinaryAndMixedHaarsEachAddTwoDrunkStages(GameTestHelper helper) {
        HolidayGameTestScenarios.ordinaryAndMixedHaarsEachAddTwoDrunkStages(helper);
    }

    private NeoForgeHolidayGameTests() {
    }
}
