package lekavar.lma.drinkbeer.gametest;

import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;

public final class FabricHolidayGameTests implements FabricGameTest {
    @GameTest(template = FabricGameTest.EMPTY_STRUCTURE, timeoutTicks = 40)
    public void giftOpensOnTheServerExactlyOnce(GameTestHelper helper) {
        HolidayGameTestScenarios.giftOpensOnTheServerExactlyOnce(helper);
    }

    @GameTest(template = FabricGameTest.EMPTY_STRUCTURE, timeoutTicks = 40)
    public void ordinaryAndMixedEggnogEachGiveOneColoredGift(GameTestHelper helper) {
        HolidayGameTestScenarios.ordinaryAndMixedEggnogEachGiveOneColoredGift(helper);
    }

    @GameTest(template = FabricGameTest.EMPTY_STRUCTURE, timeoutTicks = 60)
    public void horseModelPlacesTwoHalvesAndDropsOnlyOnce(GameTestHelper helper) {
        HolidayGameTestScenarios.horseModelPlacesTwoHalvesAndDropsOnlyOnce(helper);
    }

    @GameTest(template = FabricGameTest.EMPTY_STRUCTURE, timeoutTicks = 40)
    public void beerTagFeedsBartendingValidation(GameTestHelper helper) {
        HolidayGameTestScenarios.beerTagFeedsBartendingValidation(helper);
    }

    @GameTest(template = FabricGameTest.EMPTY_STRUCTURE, timeoutTicks = 40)
    public void ordinaryAndMixedHaarsEachAddTwoDrunkStages(GameTestHelper helper) {
        HolidayGameTestScenarios.ordinaryAndMixedHaarsEachAddTwoDrunkStages(helper);
    }
}
