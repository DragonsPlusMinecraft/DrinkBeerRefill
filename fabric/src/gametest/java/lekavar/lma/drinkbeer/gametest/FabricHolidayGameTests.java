package lekavar.lma.drinkbeer.gametest;

import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;

public final class FabricHolidayGameTests {
    @GameTest(maxTicks = 40)
    public void giftOpensOnTheServerExactlyOnce(GameTestHelper helper) {
        HolidayGameTestScenarios.giftOpensOnTheServerExactlyOnce(helper);
    }

    @GameTest(maxTicks = 40)
    public void ordinaryAndMixedEggnogEachGiveOneColoredGift(GameTestHelper helper) {
        HolidayGameTestScenarios.ordinaryAndMixedEggnogEachGiveOneColoredGift(helper);
    }

    @GameTest(maxTicks = 60)
    public void horseModelPlacesTwoHalvesAndDropsOnlyOnce(GameTestHelper helper) {
        HolidayGameTestScenarios.horseModelPlacesTwoHalvesAndDropsOnlyOnce(helper);
    }

    @GameTest(maxTicks = 40)
    public void beerTagFeedsBartendingValidation(GameTestHelper helper) {
        HolidayGameTestScenarios.beerTagFeedsBartendingValidation(helper);
    }

    @GameTest(maxTicks = 40)
    public void ordinaryAndMixedHaarsEachAddTwoDrunkStages(GameTestHelper helper) {
        HolidayGameTestScenarios.ordinaryAndMixedHaarsEachAddTwoDrunkStages(helper);
    }

    @GameTest(maxTicks = 80)
    public void beerBarrelCompletesLifecycleWithSidedAutomation(GameTestHelper helper) {
        HolidayGameTestScenarios.beerBarrelCompletesLifecycleWithSidedAutomation(helper);
    }

    @GameTest(maxTicks = 80)
    public void bartendingMixingPersistsAndDropsItsResult(GameTestHelper helper) {
        HolidayGameTestScenarios.bartendingMixingPersistsAndDropsItsResult(helper);
    }

    @GameTest(maxTicks = 60)
    public void mixedBeerLegacyAndModernDataRoundTripInWorld(GameTestHelper helper) {
        HolidayGameTestScenarios.mixedBeerLegacyAndModernDataRoundTripInWorld(helper);
    }

    @GameTest(maxTicks = 60)
    public void tradeBoxPayloadRejectsDuplicatesAndMalformedTargets(GameTestHelper helper) {
        HolidayGameTestScenarios.tradeBoxPayloadRejectsDuplicatesAndMalformedTargets(helper);
    }

    @GameTest(maxTicks = 40)
    public void configAndEmptyInventoryBoundariesAreSafe(GameTestHelper helper) {
        HolidayGameTestScenarios.configAndEmptyInventoryBoundariesAreSafe(helper);
    }
}
