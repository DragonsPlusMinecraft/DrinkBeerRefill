package lekavar.lma.drinkbeer.gametest;

import lekavar.lma.drinkbeer.DrinkBeer;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.FunctionGameTestInstance;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.gametest.framework.GameTestInstance;
import net.minecraft.gametest.framework.TestData;
import net.minecraft.gametest.framework.TestEnvironmentDefinition;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.block.Rotation;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterGameTestsEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.List;
import java.util.function.Consumer;

@EventBusSubscriber(modid = DrinkBeer.MOD_ID)
public final class NeoForgeHolidayGameTests {
    private static final Identifier ENVIRONMENT = id("default_environment");
    private static final Identifier STRUCTURE = id("empty");
    private static final DeferredRegister<Consumer<GameTestHelper>> TEST_FUNCTIONS =
            DeferredRegister.create(BuiltInRegistries.TEST_FUNCTION, DrinkBeer.MOD_ID);

    private static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> GIFT_OPENS = TEST_FUNCTIONS.register(
            "gift_opens_on_the_server_exactly_once",
            () -> HolidayGameTestScenarios::giftOpensOnTheServerExactlyOnce
    );
    private static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> EGGNOG_GIFTS = TEST_FUNCTIONS.register(
            "ordinary_and_mixed_eggnog_each_give_one_colored_gift",
            () -> HolidayGameTestScenarios::ordinaryAndMixedEggnogEachGiveOneColoredGift
    );
    private static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> HORSE_MODEL = TEST_FUNCTIONS.register(
            "horse_model_places_two_halves_and_drops_only_once",
            () -> HolidayGameTestScenarios::horseModelPlacesTwoHalvesAndDropsOnlyOnce
    );
    private static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> BEER_TAG = TEST_FUNCTIONS.register(
            "beer_tag_feeds_bartending_validation",
            () -> HolidayGameTestScenarios::beerTagFeedsBartendingValidation
    );
    private static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> HAARS_DRUNK = TEST_FUNCTIONS.register(
            "ordinary_and_mixed_haars_each_add_two_drunk_stages",
            () -> HolidayGameTestScenarios::ordinaryAndMixedHaarsEachAddTwoDrunkStages
    );
    private static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> BARREL_LIFECYCLE = TEST_FUNCTIONS.register(
            "beer_barrel_completes_lifecycle_with_sided_automation",
            () -> HolidayGameTestScenarios::beerBarrelCompletesLifecycleWithSidedAutomation
    );
    private static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> BARTENDING_PERSISTENCE = TEST_FUNCTIONS.register(
            "bartending_mixing_persists_and_drops_its_result",
            () -> HolidayGameTestScenarios::bartendingMixingPersistsAndDropsItsResult
    );
    private static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> MIXED_BEER_DATA = TEST_FUNCTIONS.register(
            "mixed_beer_legacy_and_modern_data_round_trip_in_world",
            () -> HolidayGameTestScenarios::mixedBeerLegacyAndModernDataRoundTripInWorld
    );
    private static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> TRADE_PAYLOAD = TEST_FUNCTIONS.register(
            "trade_box_payload_rejects_duplicates_and_malformed_targets",
            () -> HolidayGameTestScenarios::tradeBoxPayloadRejectsDuplicatesAndMalformedTargets
    );
    private static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> CONFIG_BOUNDARIES = TEST_FUNCTIONS.register(
            "config_and_empty_inventory_boundaries_are_safe",
            () -> HolidayGameTestScenarios::configAndEmptyInventoryBoundariesAreSafe
    );

    public static void registerFunctions(IEventBus modEventBus) {
        TEST_FUNCTIONS.register(modEventBus);
    }

    @SubscribeEvent
    public static void registerGameTests(RegisterGameTestsEvent event) {
        Holder<TestEnvironmentDefinition> environment = event.registerEnvironment(
                ENVIRONMENT,
                new TestEnvironmentDefinition.AllOf(List.of())
        );
        registerTest(event, environment, GIFT_OPENS, 40);
        registerTest(event, environment, EGGNOG_GIFTS, 40);
        registerTest(event, environment, HORSE_MODEL, 60);
        registerTest(event, environment, BEER_TAG, 40);
        registerTest(event, environment, HAARS_DRUNK, 40);
        registerTest(event, environment, BARREL_LIFECYCLE, 80);
        registerTest(event, environment, BARTENDING_PERSISTENCE, 80);
        registerTest(event, environment, MIXED_BEER_DATA, 60);
        registerTest(event, environment, TRADE_PAYLOAD, 60);
        registerTest(event, environment, CONFIG_BOUNDARIES, 40);
    }

    private static void registerTest(
            RegisterGameTestsEvent event,
            Holder<TestEnvironmentDefinition> environment,
            DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> function,
            int maxTicks
    ) {
        TestData<Holder<TestEnvironmentDefinition>> data = new TestData<>(
                environment,
                STRUCTURE,
                maxTicks,
                0,
                true,
                Rotation.NONE
        );
        GameTestInstance instance = new FunctionGameTestInstance(function.getKey(), data);
        event.registerTest(function.getId(), instance);
    }

    private static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(DrinkBeer.MOD_ID, path);
    }

    private NeoForgeHolidayGameTests() {
    }
}
