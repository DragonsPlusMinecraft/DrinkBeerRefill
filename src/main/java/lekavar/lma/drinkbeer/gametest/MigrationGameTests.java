package lekavar.lma.drinkbeer.gametest;

import lekavar.lma.drinkbeer.DrinkBeer;
import lekavar.lma.drinkbeer.blockentities.BartendingTableBlockEntity;
import lekavar.lma.drinkbeer.blockentities.BeerBarrelBlockEntity;
import lekavar.lma.drinkbeer.blockentities.MixedBeerBlockEntity;
import lekavar.lma.drinkbeer.blockentities.TradeBoxBlockEntity;
import lekavar.lma.drinkbeer.managers.MixedBeerManager;
import lekavar.lma.drinkbeer.managers.SpiceAndFlavorManager;
import lekavar.lma.drinkbeer.registries.BlockRegistry;
import lekavar.lma.drinkbeer.registries.DamageRegistry;
import lekavar.lma.drinkbeer.registries.DrinkBeerTags;
import lekavar.lma.drinkbeer.registries.ItemRegistry;
import lekavar.lma.drinkbeer.registries.MobEffectRegistry;
import lekavar.lma.drinkbeer.registries.RecipeRegistry;
import lekavar.lma.drinkbeer.registries.SoundEventRegistry;
import lekavar.lma.drinkbeer.utils.ContainerNbtHelper;
import lekavar.lma.drinkbeer.utils.beer.BeerDefinitions;
import lekavar.lma.drinkbeer.utils.beer.Beers;
import lekavar.lma.drinkbeer.utils.gift.GiftRewards;
import lekavar.lma.drinkbeer.utils.mixedbeer.Flavors;
import lekavar.lma.drinkbeer.utils.mixedbeer.MixedBeerOnUsing;
import lekavar.lma.drinkbeer.utils.mixedbeer.Spices;
import lekavar.lma.drinkbeer.recipes.BrewingRecipe;
import lekavar.lma.drinkbeer.recipes.IBrewingInventory;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;
import net.minecraftforge.items.IItemHandler;
import org.apache.commons.lang3.tuple.Pair;

import java.util.List;
import java.util.Set;
import java.util.Arrays;
import java.util.stream.Collectors;

@GameTestHolder(DrinkBeer.MOD_ID)
@PrefixGameTestTemplate(false)
public final class MigrationGameTests {
    private static final BlockPos TEST_POS = new BlockPos(2, 1, 2);
    private static final Set<String> EXPECTED_BLOCK_AND_ITEM_IDS = Set.of(
            "bartending_table_normal", "beer_barrel", "beer_mug", "beer_mug_apple_lambic",
            "beer_mug_blaze_milk_stout", "beer_mug_blaze_stout", "beer_mug_frothy_pink_eggnog",
            "beer_mug_haars_icey_pale_lager", "beer_mug_night_howl_kvass", "beer_mug_pumpkin_kvass",
            "beer_mug_sweet_berry_kriek", "colored_lights", "empty_beer_mug", "gift_blue", "gift_green",
            "gift_red", "gift_white", "golden_call_bell", "horse_model_1", "horse_model_2",
            "horse_model_3", "iron_call_bell", "lekas_call_bell", "mixed_beer", "recipe_board_beer_mug",
            "recipe_board_beer_mug_apple_lambic", "recipe_board_beer_mug_blaze_milk_stout",
            "recipe_board_beer_mug_blaze_stout", "recipe_board_beer_mug_frothy_pink_eggnog",
            "recipe_board_beer_mug_haars_icey_pale_lager", "recipe_board_beer_mug_night_howl_kvass",
            "recipe_board_beer_mug_pumpkin_kvass", "recipe_board_beer_mug_sweet_berry_kriek",
            "recipe_board_package", "side_colored_lights", "spice_amethyst_nigella_seeds",
            "spice_blaze_paprika", "spice_citrine_nigella_seeds", "spice_dried_eglia_bud",
            "spice_dried_selaginella", "spice_frozen_persimmon", "spice_glace_goji_berries",
            "spice_golden_cinnamon_powder", "spice_ice_mint", "spice_ice_patchouli",
            "spice_roasted_pecans", "spice_roasted_red_pine_nuts", "spice_silver_needle_white_tea",
            "spice_smoked_eglia_bud", "spice_storm_shards", "star_of_bethlehem",
            "the_great_star_of_bethlehem", "trade_box_normal"
    );
    private static final Set<String> EXPECTED_RECIPE_IDS = Set.of(
            "bartending_table_normal", "beer_barrel", "beer_mug", "beer_mug_apple_lambic",
            "beer_mug_blaze_milk_stout", "beer_mug_blaze_stout", "beer_mug_frothy_pink_eggnog",
            "beer_mug_haars_icey_pale_lager", "beer_mug_night_howl_kvass", "beer_mug_pumpkin_kvass",
            "beer_mug_sweet_berry_kriek", "colored_lights", "empty_beer_mug", "golden_call_bell",
            "iron_call_bell", "recipe_board_package", "side_colored_lights", "star_of_bethlehem",
            "the_great_star_of_bethlehem", "trade_box_normal"
    );

    @GameTest(template = "empty", timeoutTicks = 40)
    public static void registryIdsAndUpstreamContentRemainStable(GameTestHelper helper) {
        Set<String> blockIds = BuiltInRegistries.BLOCK.keySet().stream()
                .filter(id -> id.getNamespace().equals(DrinkBeer.MOD_ID))
                .map(id -> id.getPath())
                .collect(Collectors.toSet());
        Set<String> itemIds = BuiltInRegistries.ITEM.keySet().stream()
                .filter(id -> id.getNamespace().equals(DrinkBeer.MOD_ID))
                .map(id -> id.getPath())
                .collect(Collectors.toSet());
        helper.assertTrue(blockIds.equals(EXPECTED_BLOCK_AND_ITEM_IDS), "Block registry IDs changed: " + blockIds);
        helper.assertTrue(itemIds.equals(EXPECTED_BLOCK_AND_ITEM_IDS), "Item registry IDs changed: " + itemIds);
        helper.assertTrue(new ItemStack(ItemRegistry.BEER_MUG.get()).is(DrinkBeerTags.BEERS)
                        && new ItemStack(ItemRegistry.MIXED_BEER.get()).is(DrinkBeerTags.BEERS),
                "Beer item tag did not load");
        helper.assertTrue(helper.getLevel().registryAccess().registryOrThrow(Registries.DAMAGE_TYPE)
                        .containsKey(DamageRegistry.ALCOHOL.location()),
                "Alcohol damage type did not load");

        helper.assertTrue(BlockRegistry.GIFT_RED.get().getStateDefinition().getPossibleStates().size() == 16,
                "Gift random appearance state count changed");
        helper.assertTrue(BlockRegistry.COLORED_LIGHTS.get().getStateDefinition().getPossibleStates().size() == 32,
                "Center light state count changed");
        helper.assertTrue(BlockRegistry.SIDE_COLORED_LIGHTS.get().getStateDefinition().getPossibleStates().size() == 32,
                "Side light state count changed");
        helper.assertTrue(BlockRegistry.HORSE_MODEL_1.get().getStateDefinition().getPossibleStates().size() == 8,
                "Horse state count changed");
        BlockPos absoluteTestPos = helper.absolutePos(TEST_POS);
        var centerLights = BlockRegistry.COLORED_LIGHTS.get().defaultBlockState();
        var sideLights = BlockRegistry.SIDE_COLORED_LIGHTS.get().defaultBlockState();
        helper.assertTrue(centerLights.getLightEmission(helper.getLevel(), absoluteTestPos) == 15
                        && sideLights.getLightEmission(helper.getLevel(), absoluteTestPos) == 15
                        && centerLights.getCollisionShape(helper.getLevel(), absoluteTestPos).isEmpty()
                        && sideLights.getCollisionShape(helper.getLevel(), absoluteTestPos).isEmpty(),
                "Colored light physical properties changed");
        helper.assertTrue(BlockRegistry.STAR_OF_BETHLEHEM.get().defaultBlockState()
                        .getLightEmission(helper.getLevel(), absoluteTestPos) == 15
                        && BlockRegistry.THE_GREAT_STAR_OF_BETHLEHEM.get().defaultBlockState()
                        .getLightEmission(helper.getLevel(), absoluteTestPos) == 15,
                "Star light levels changed");
        helper.assertTrue(ItemRegistry.STAR_OF_BETHLEHEM.get().isFireResistant(),
                "Small star must remain fire resistant");
        helper.assertTrue(ItemRegistry.THE_GREAT_STAR_OF_BETHLEHEM.get().isFireResistant(),
                "Great star must remain fire resistant");

        List<String> holidaySoundIds = List.of(
                SoundEventRegistry.GIFT_OPEN.get(), SoundEventRegistry.NEIGH_1.get(),
                SoundEventRegistry.NEIGH_2.get(), SoundEventRegistry.BELL.get()
        ).stream().map(BuiltInRegistries.SOUND_EVENT::getKey).map(id -> id.getPath()).toList();
        helper.assertTrue(holidaySoundIds.equals(List.of(
                "gift_open_sound", "neigh1_sound", "neigh2_sound", "bell_sound")),
                "Holiday sound IDs changed: " + holidaySoundIds);

        for (Beers beer : Beers.values()) {
            helper.assertTrue(beer.getBeerItem().getMaxStackSize() == 16, beer.name() + " stack size changed");
        }
        helper.assertTrue(Beers.byRecipeBoardBlock(BlockRegistry.RECIPE_BOARD_BEER_MUG.get()) == Beers.BEER_MUG,
                "Base recipe board mapping changed");
        helper.assertTrue(Beers.byRecipeBoardBlock(BlockRegistry.RECIPE_BOARD_BEER_MUG_APPLE_LAMBIC.get())
                        == Beers.BEER_MUG_APPLE_LAMBIC,
                "Apple Lambic recipe board mapping changed");

        helper.assertTrue(GiftRewards.rewardCount() == 27, "Gift reward pool size changed");
        for (int index = 0; index < Beers.values().length; index++) {
            ItemStack reward = GiftRewards.rewardAt(index);
            helper.assertTrue(reward.is(Beers.values()[index].getBeerItem()) && reward.getCount() == 1,
                    "Beer reward changed at index " + index);
        }
        for (int index = 0; index < Spices.values().length; index++) {
            ItemStack reward = GiftRewards.rewardAt(9 + index);
            helper.assertTrue(reward.is(Spices.values()[index].getSpiceItem()) && reward.getCount() == 2,
                    "Spice reward changed at index " + (9 + index));
        }
        List<Item> horses = List.of(
                ItemRegistry.HORSE_MODEL_1.get(), ItemRegistry.HORSE_MODEL_2.get(), ItemRegistry.HORSE_MODEL_3.get());
        for (int index = 0; index < horses.size(); index++) {
            ItemStack reward = GiftRewards.rewardAt(24 + index);
            helper.assertTrue(reward.is(horses.get(index)) && reward.getCount() == 1,
                    "Horse reward changed at index " + (24 + index));
        }
        helper.assertTrue(GiftRewards.coloredGiftCount() == 4, "Colored gift pool size changed");

        var frostEffect = BeerDefinitions.HAARS_ICEY_PALE_LAGER.effectFactory().get();
        helper.assertTrue(frostEffect.getEffect() == MobEffectRegistry.DRUNK_FROST_WALKER.get()
                        && frostEffect.getDuration() == 1200,
                "Haar's custom effect definition changed");
        helper.succeed();
    }

    @GameTest(template = "empty", timeoutTicks = 40)
    public static void beerDefinitionsAndFlavorRulesRemainStable(GameTestHelper helper) {
        helper.assertTrue(BeerDefinitions.ALL.stream().map(definition -> definition.id()).toList()
                        .equals(List.of(1, 2, 3, 4, 5, 6, 7, 8, 9)),
                "Beer IDs changed");
        helper.assertTrue(BeerDefinitions.ALL.stream().map(definition -> definition.nutrition()).toList()
                        .equals(List.of(2, 1, 1, 1, 1, 1, 9, 2, 4)),
                "Beer nutrition changed");
        List<Integer> expectedDurations = List.of(1200, 1800, 2400, 300, 400, 1200, 0, 2400, 0);
        for (int index = 0; index < BeerDefinitions.ALL.size(); index++) {
            var definition = BeerDefinitions.ALL.get(index);
            var effect = definition.effectFactory() == null ? null : definition.effectFactory().get();
            if (expectedDurations.get(index) == 0) {
                helper.assertTrue(effect == null, "Unexpected effect for beer ID " + definition.id());
            } else {
                helper.assertTrue(effect != null && effect.getDuration() == expectedDurations.get(index)
                                && effect.getAmplifier() == 0,
                        "Effect changed for beer ID " + definition.id());
            }

            FoodProperties exact = definition.foodProperties(0.0F);
            FoodProperties improved = definition.foodProperties(0.1F);
            helper.assertTrue(exact.getSaturationModifier() == 0.0F
                            && improved.getSaturationModifier() == 0.1F
                            && improved.canAlwaysEat(),
                    "Food properties changed for beer ID " + definition.id());
        }

        List<Flavors> flavorMapping = Arrays.stream(Spices.values()).map(Spices::getFlavor).toList();
        helper.assertTrue(flavorMapping.equals(List.of(
                        Flavors.FIERY, Flavors.SPICY, Flavors.FIERY,
                        Flavors.AROMATIC, Flavors.AROMATIC1,
                        Flavors.REFRESHING, Flavors.REFRESHING1,
                        Flavors.STORMY, Flavors.NUTTY, Flavors.SWEET,
                        Flavors.LUSCIOUS, Flavors.NUTTY1, Flavors.MELLOW,
                        Flavors.SWEET, Flavors.DRYING)),
                "Spice-to-flavor mapping changed");

        MixedBeerOnUsing aromaticThenSpicy = drinkWithEffect(100);
        aromaticThenSpicy.setSpiceList(List.of(4, 2));
        SpiceAndFlavorManager.applyFlavorValue(aromaticThenSpicy);
        MixedBeerOnUsing spicyThenAromatic = drinkWithEffect(100);
        spicyThenAromatic.setSpiceList(List.of(2, 4));
        SpiceAndFlavorManager.applyFlavorValue(spicyThenAromatic);
        helper.assertTrue(aromaticThenSpicy.getStatusEffectList().get(0).getValue() == 1620
                        && spicyThenAromatic.getStatusEffectList().get(0).getValue() == 980,
                "Aromatic/spicy order-sensitive durations changed");

        MixedBeerOnUsing values = drinkWithEffect(100);
        values.setSpiceList(List.of(3, 5, 7, 12, 11, 13));
        SpiceAndFlavorManager.applyFlavorValue(values);
        int hasteDuration = values.getStatusEffectList().stream()
                .filter(effect -> effect.getKey() == MobEffects.DIG_SPEED)
                .findFirst().orElseThrow().getValue();
        int resistanceDuration = values.getStatusEffectList().stream()
                .filter(effect -> effect.getKey() == MobEffects.DAMAGE_RESISTANCE)
                .findFirst().orElseThrow().getValue();
        helper.assertTrue(values.getDrunkValue() == -1 && values.getHunger() == 5
                        && values.getHealth() == 0.0F && hasteDuration == 1400 && resistanceDuration == 1600,
                "Value-changing flavor behavior changed");
        helper.assertTrue(SpiceAndFlavorManager.getCombinedFlavor(List.of(1, 2, 3)) == Flavors.SOOOOO_SPICY
                        && SpiceAndFlavorManager.getCombinedFlavor(List.of(8, 8, 8)) == Flavors.THE_FALL_OF_THE_GIANT
                        && SpiceAndFlavorManager.getCombinedFlavor(List.of(10, 11, 14)) == Flavors.CLOYING,
                "Combined flavor normalization changed");
        helper.succeed();
    }

    @GameTest(template = "empty", timeoutTicks = 40)
    public static void brewingRecipesLoadAndMatchFourUnorderedInputs(GameTestHelper helper) {
        for (String recipeId : EXPECTED_RECIPE_IDS) {
            ResourceLocation id = new ResourceLocation(DrinkBeer.MOD_ID, recipeId);
            helper.assertTrue(helper.getLevel().getRecipeManager().byKey(id).isPresent(),
                    "Recipe did not load: " + id);
        }
        List<BrewingRecipe> recipes = helper.getLevel().getRecipeManager()
                .getAllRecipesFor(RecipeRegistry.RECIPE_TYPE_BREWING.get());
        helper.assertTrue(recipes.size() == 9, "Expected nine brewing recipes but found " + recipes.size());
        for (BrewingRecipe recipe : recipes) {
            helper.assertTrue(recipe.getIngredients().size() == BrewingRecipe.INPUT_SIZE,
                    recipe.getId() + " does not have four inputs");
            helper.assertTrue(recipe.getBrewingTime() > 0
                            && recipe.getRequiredCupCount() == recipe.getResultItemNoRegistryAccess().getCount(),
                    recipe.getId() + " has invalid time or output balance");
        }

        BrewingRecipe baseRecipe = recipes.stream()
                .filter(recipe -> recipe.getId().getPath().equals("beer_mug"))
                .findFirst().orElseThrow();
        IBrewingInventory matching = brewingInventory(List.of(
                new ItemStack(Items.WATER_BUCKET), new ItemStack(Items.WHEAT, 32),
                new ItemStack(Items.WHEAT), new ItemStack(Items.WHEAT, 16)
        ), new ItemStack(ItemRegistry.EMPTY_BEER_MUG.get(), 4));
        helper.assertTrue(baseRecipe.matches(matching, helper.getLevel()),
                "Brewing recipe must accept inputs in any slot order");
        helper.assertTrue(baseRecipe.isCupQualified(matching), "Configured cup/count must be accepted");

        IBrewingInventory missing = brewingInventory(List.of(
                new ItemStack(Items.WHEAT), new ItemStack(Items.WHEAT), new ItemStack(Items.WATER_BUCKET)
        ), new ItemStack(ItemRegistry.EMPTY_BEER_MUG.get(), 4));
        IBrewingInventory unexpected = brewingInventory(List.of(
                new ItemStack(Items.WHEAT), new ItemStack(Items.WHEAT),
                new ItemStack(Items.CARROT), new ItemStack(Items.WATER_BUCKET)
        ), new ItemStack(ItemRegistry.EMPTY_BEER_MUG.get(), 4));
        IBrewingInventory shortCups = brewingInventory(List.of(),
                new ItemStack(ItemRegistry.EMPTY_BEER_MUG.get(), 3));
        helper.assertTrue(!baseRecipe.matches(missing, helper.getLevel())
                        && !baseRecipe.matches(unexpected, helper.getLevel())
                        && !baseRecipe.isCupQualified(shortCups),
                "Brewing recipe accepted incomplete, unexpected, or under-counted inputs");
        helper.succeed();
    }

    @GameTest(template = "empty", timeoutTicks = 40)
    public static void mixedBeerAcceptsLegacyNbtAndWritesCanonicalNbt(GameTestHelper helper) {
        CompoundTag descriptor = mixedBeerDescriptor(6, 4, 0, 99, 8, 9, 10);

        ItemStack nested = new ItemStack(ItemRegistry.MIXED_BEER.get());
        nested.getOrCreateTagElement("BlockEntityTag").put("MixedBeer", descriptor.copy());
        assertMixedBeer(helper, nested, 6, List.of(4, 8, 9));

        ItemStack rootMixedBeer = new ItemStack(ItemRegistry.MIXED_BEER.get());
        rootMixedBeer.getOrCreateTag().put("MixedBeer", descriptor.copy());
        assertMixedBeer(helper, rootMixedBeer, 6, List.of(4, 8, 9));

        ItemStack direct = new ItemStack(ItemRegistry.MIXED_BEER.get());
        direct.setTag(descriptor.copy());
        assertMixedBeer(helper, direct, 6, List.of(4, 8, 9));

        ItemStack emptyBeer = MixedBeerManager.genMixedBeerItemStack(0, List.of());
        helper.assertTrue(MixedBeerManager.getBeerId(emptyBeer) == 0, "Beer ID 0 must remain valid");
        ItemStack untaggedBeer = new ItemStack(ItemRegistry.MIXED_BEER.get());
        helper.assertTrue(MixedBeerManager.getBeerId(untaggedBeer) == 0,
                "An untagged mixed beer must retain the empty beer ID");
        ItemStack sanitized = MixedBeerManager.genMixedBeerItemStack(99, List.of(1, 0, 99, 2, 3, 4));
        assertMixedBeer(helper, sanitized, Beers.DEFAULT_BEER_ID, List.of(1, 2, 3));
        CompoundTag canonicalBlockEntityTag = sanitized.getTagElement("BlockEntityTag");
        helper.assertTrue(canonicalBlockEntityTag != null
                        && canonicalBlockEntityTag.contains("MixedBeer", Tag.TAG_COMPOUND),
                "Mixed beer item must write BlockEntityTag.MixedBeer");

        helper.setBlock(TEST_POS, BlockRegistry.MIXED_BEER.get());
        MixedBeerBlockEntity blockEntity = blockEntity(helper, TEST_POS, MixedBeerBlockEntity.class);
        CompoundTag oldBlockEntityTag = new CompoundTag();
        oldBlockEntityTag.put("MixedBeer", descriptor.copy());
        blockEntity.load(oldBlockEntityTag);
        helper.assertTrue(blockEntity.getBeerId() == 6 && blockEntity.getSpiceList().equals(List.of(4, 8, 9)),
                "Mixed beer block entity failed to import old NBT");
        assertMixedBeer(helper, blockEntity.getPickStack(), 6, List.of(4, 8, 9));
        helper.succeed();
    }

    @GameTest(template = "empty", timeoutTicks = 40)
    public static void bartendingTableImportsLegacySlotsAndRoundTripsCanonicalItems(GameTestHelper helper) {
        helper.setBlock(TEST_POS, BlockRegistry.BARTENDING_TABLE.get());
        BartendingTableBlockEntity table = blockEntity(helper, TEST_POS, BartendingTableBlockEntity.class);

        CompoundTag legacy = new CompoundTag();
        legacy.put("input", new ItemStack(ItemRegistry.BEER_MUG.get()).save(new CompoundTag()));
        legacy.put("output", MixedBeerManager.genMixedBeerItemStack(4, List.of(1, 7)).save(new CompoundTag()));
        table.load(legacy);
        helper.assertTrue(table.getInventory().getItem(0).is(ItemRegistry.BEER_MUG.get()),
                "Legacy bartending input was not imported");
        assertMixedBeer(helper, table.getInventory().getItem(1), 4, List.of(1, 7));

        CompoundTag canonical = table.saveWithFullMetadata();
        helper.assertTrue(canonical.contains("Items", Tag.TAG_LIST), "Bartending table must save canonical Items");
        helper.assertTrue(!canonical.contains("input") && !canonical.contains("output"),
                "Bartending table must not rewrite legacy slot keys");

        table.getInventory().clearContent();
        table.load(canonical);
        helper.assertTrue(table.getInventory().getItem(0).is(ItemRegistry.BEER_MUG.get()),
                "Canonical bartending input failed to round-trip");
        assertMixedBeer(helper, table.getInventory().getItem(1), 4, List.of(1, 7));
        helper.succeed();
    }

    @GameTest(template = "empty", timeoutTicks = 40)
    public static void beerBarrelImportsLegacyInventoryAndShortState(GameTestHelper helper) {
        helper.setBlock(TEST_POS, BlockRegistry.BEER_BARREL.get());
        BeerBarrelBlockEntity barrel = blockEntity(helper, TEST_POS, BeerBarrelBlockEntity.class);

        ListTag oldInventory = new ListTag();
        List<ItemStack> oldStacks = List.of(
                new ItemStack(Items.WHEAT), new ItemStack(Items.APPLE),
                new ItemStack(Items.SUGAR), new ItemStack(Items.WATER_BUCKET),
                new ItemStack(ItemRegistry.EMPTY_BEER_MUG.get(), 4),
                new ItemStack(ItemRegistry.BEER_MUG.get(), 4)
        );
        for (ItemStack stack : oldStacks) {
            oldInventory.add(stack.save(new CompoundTag()));
        }
        CompoundTag legacy = new CompoundTag();
        legacy.put("inv", oldInventory);
        legacy.putShort("RemainingBrewTime", (short) 321);
        legacy.putShort("statusCode", (short) BeerBarrelBlockEntity.STATUS_BREWING);
        barrel.load(legacy);

        for (int slot = 0; slot < oldStacks.size(); slot++) {
            helper.assertTrue(ItemStack.isSameItemSameTags(
                            barrel.getBrewingInventory().getItem(slot), oldStacks.get(slot)),
                    "Legacy barrel slot " + slot + " was not imported");
        }
        helper.assertTrue(barrel.syncData.get(0) == 321
                        && barrel.syncData.get(1) == BeerBarrelBlockEntity.STATUS_BREWING,
                "Legacy barrel short state was not preserved");

        CompoundTag canonical = barrel.saveWithFullMetadata();
        helper.assertTrue(canonical.contains("Items", Tag.TAG_LIST) && !canonical.contains("inv"),
                "Barrel must save canonical Items instead of inv");
        helper.assertTrue(canonical.contains("RemainingBrewTime", Tag.TAG_INT)
                        && canonical.contains("statusCode", Tag.TAG_INT),
                "Barrel state must be normalized to int tags");

        barrel.getBrewingInventory().clearContent();
        barrel.syncData.set(0, 0);
        barrel.syncData.set(1, BeerBarrelBlockEntity.STATUS_WAITING);
        barrel.load(canonical);
        helper.assertTrue(barrel.syncData.get(0) == 321
                        && barrel.syncData.get(1) == BeerBarrelBlockEntity.STATUS_BREWING,
                "Canonical barrel state failed to round-trip");
        helper.assertTrue(barrel.getBrewingInventory().getItem(5).getCount() == 4,
                "Canonical barrel inventory failed to round-trip");
        helper.succeed();
    }

    @GameTest(template = "empty", timeoutTicks = 40)
    public static void tradeBoxRoundTripsAndSanitizesPersistentState(GameTestHelper helper) {
        helper.setBlock(TEST_POS, BlockRegistry.TRADE_BOX.get());
        TradeBoxBlockEntity tradeBox = blockEntity(helper, TEST_POS, TradeBoxBlockEntity.class);

        SimpleContainer goods = new SimpleContainer(8);
        goods.setItem(0, new ItemStack(Items.WHEAT, 3));
        goods.setItem(4, new ItemStack(Items.EMERALD, 2));
        CompoundTag oldCanonical = new CompoundTag();
        ContainerNbtHelper.saveAllItems(oldCanonical, goods);
        oldCanonical.putShort("CoolingTime", (short) 77);
        oldCanonical.putShort("LocationId", (short) 1);
        oldCanonical.putShort("ResidentId", (short) 1);
        oldCanonical.putShort("Process", (short) TradeBoxBlockEntity.PROCESS_TRADING);
        tradeBox.load(oldCanonical);
        helper.assertTrue(tradeBox.goodInventory.getItem(0).getCount() == 3
                        && tradeBox.goodInventory.getItem(4).getCount() == 2,
                "TradeBox Items were not imported");
        helper.assertTrue(tradeBox.syncData.get(0) == 77 && tradeBox.syncData.get(1) == 1
                        && tradeBox.syncData.get(2) == 1
                        && tradeBox.syncData.get(3) == TradeBoxBlockEntity.PROCESS_TRADING,
                "TradeBox short state was not imported");

        CompoundTag canonical = tradeBox.saveWithFullMetadata();
        helper.assertTrue(canonical.contains("Items", Tag.TAG_LIST), "TradeBox must retain canonical Items");
        for (String key : List.of("CoolingTime", "LocationId", "ResidentId", "Process")) {
            helper.assertTrue(canonical.contains(key, Tag.TAG_INT), "TradeBox must normalize " + key + " to int");
        }

        CompoundTag invalid = new CompoundTag();
        invalid.put("Items", new ListTag());
        invalid.putInt("CoolingTime", -10);
        invalid.putInt("LocationId", 99);
        invalid.putInt("ResidentId", 99);
        invalid.putInt("Process", 99);
        tradeBox.load(invalid);
        helper.assertTrue(tradeBox.syncData.get(0) == 0 && tradeBox.syncData.get(1) == 0
                        && tradeBox.syncData.get(2) == 0
                        && tradeBox.syncData.get(3) == TradeBoxBlockEntity.PROCESS_COOLING,
                "TradeBox invalid state was not sanitized");
        helper.succeed();
    }

    @GameTest(template = "empty", timeoutTicks = 40)
    public static void automationCapabilitiesKeepDirectionalAndVirtualSlotRules(GameTestHelper helper) {
        helper.setBlock(TEST_POS, BlockRegistry.BARTENDING_TABLE.get());
        BartendingTableBlockEntity table = blockEntity(helper, TEST_POS, BartendingTableBlockEntity.class);
        helper.assertTrue(table.getCapability(ForgeCapabilities.ITEM_HANDLER, Direction.UP).isPresent(),
                "Bartending table must expose an upper item capability");

        IItemHandler combined = table.getItemHandler(null);
        IItemHandler beerInput = table.getItemHandler(Direction.UP);
        IItemHandler spiceInput = table.getItemHandler(Direction.NORTH);
        IItemHandler output = table.getItemHandler(Direction.DOWN);
        helper.assertTrue(combined.getSlots() == 2 && beerInput.getSlots() == 1
                        && spiceInput.getSlots() == 1 && output.getSlots() == 1,
                "Bartending sided slot counts changed");
        helper.assertTrue(combined.getStackInSlot(2).isEmpty()
                        && combined.getSlotLimit(2) == 0
                        && !combined.isItemValid(2, new ItemStack(Items.STONE)),
                "Bartending virtual/invalid slot access must stay safe");

        ItemStack beerRemainder = beerInput.insertItem(0, new ItemStack(ItemRegistry.BEER_MUG.get(), 2), false);
        helper.assertTrue(beerRemainder.getCount() == 1, "Upper automation must insert one beer");
        ItemStack spiceRemainder = spiceInput.insertItem(
                0, new ItemStack(ItemRegistry.SPICE_BLAZE_PAPRIKA.get()), false);
        helper.assertTrue(spiceRemainder.isEmpty(), "Side automation must insert one spice");
        helper.assertTrue(spiceInput.insertItem(
                        0, new ItemStack(ItemRegistry.SPICE_DRIED_EGLIA_BUD.get()), false).isEmpty()
                        && spiceInput.insertItem(
                        0, new ItemStack(ItemRegistry.SPICE_SMOKED_EGLIA_BUD.get()), false).isEmpty(),
                "Bartending automation must safely append a second and third spice");
        ItemStack fourthSpice = spiceInput.insertItem(
                0, new ItemStack(ItemRegistry.SPICE_AMETHYST_NIGELLA_SEEDS.get()), false);
        helper.assertTrue(fourthSpice.getCount() == 1, "Bartending automation must reject a fourth spice");
        ItemStack extracted = output.extractItem(0, 1, false);
        helper.assertTrue(extracted.is(ItemRegistry.MIXED_BEER.get()),
                "Bottom automation must extract the mixed beer");
        helper.assertTrue(MixedBeerManager.getSpiceList(extracted).size() == 3,
                "Extracted mixed beer must retain all three spices");

        BlockPos barrelPos = TEST_POS.offset(2, 0, 0);
        helper.setBlock(barrelPos, BlockRegistry.BEER_BARREL.get());
        BeerBarrelBlockEntity barrel = blockEntity(helper, barrelPos, BeerBarrelBlockEntity.class);
        IItemHandler barrelCombined = barrel.getItemHandler(null);
        IItemHandler ingredients = barrel.getItemHandler(Direction.UP);
        IItemHandler cups = barrel.getItemHandler(Direction.NORTH);
        IItemHandler barrelOutput = barrel.getItemHandler(Direction.DOWN);
        helper.assertTrue(barrelCombined.getSlots() == 6 && ingredients.getSlots() == 4
                        && cups.getSlots() == 1 && barrelOutput.getSlots() == 5,
                "Barrel sided slot counts changed");
        helper.assertTrue(barrelCombined.getStackInSlot(99).isEmpty()
                        && barrelCombined.getSlotLimit(99) == 0,
                "Barrel invalid slot access must stay safe");
        ItemStack rejected = barrelOutput.insertItem(0, new ItemStack(Items.WHEAT), false);
        helper.assertTrue(rejected.getCount() == 1, "Barrel bottom capability must reject insertion");
        helper.succeed();
    }

    private static CompoundTag mixedBeerDescriptor(int beerId, int... spices) {
        CompoundTag descriptor = new CompoundTag();
        descriptor.putInt("beerId", beerId);
        descriptor.putIntArray("spiceList", spices);
        return descriptor;
    }

    private static MixedBeerOnUsing drinkWithEffect(int duration) {
        MixedBeerOnUsing drink = new MixedBeerOnUsing();
        drink.addStatusEffect(List.of(Pair.of(MobEffects.DIG_SPEED, duration)));
        return drink;
    }

    private static IBrewingInventory brewingInventory(List<ItemStack> ingredients, ItemStack cup) {
        return new TestBrewingInventory(ingredients, cup);
    }

    private static void assertMixedBeer(GameTestHelper helper, ItemStack stack, int beerId, List<Integer> spices) {
        helper.assertTrue(MixedBeerManager.getBeerId(stack) == beerId,
                "Expected beer ID " + beerId + " but got " + MixedBeerManager.getBeerId(stack));
        helper.assertTrue(MixedBeerManager.getSpiceList(stack).equals(spices),
                "Expected spices " + spices + " but got " + MixedBeerManager.getSpiceList(stack));
    }

    private static <T> T blockEntity(GameTestHelper helper, BlockPos relativePos, Class<T> type) {
        Object blockEntity = helper.getLevel().getBlockEntity(helper.absolutePos(relativePos));
        helper.assertTrue(type.isInstance(blockEntity), "Expected " + type.getSimpleName() + " at " + relativePos);
        return type.cast(blockEntity);
    }

    private static final class TestBrewingInventory extends SimpleContainer implements IBrewingInventory {
        private TestBrewingInventory(List<ItemStack> ingredients, ItemStack cup) {
            super(5);
            for (int slot = 0; slot < Math.min(BrewingRecipe.INPUT_SIZE, ingredients.size()); slot++) {
                setItem(slot, ingredients.get(slot).copy());
            }
            setItem(4, cup.copy());
        }

        @Override
        public List<ItemStack> getIngredients() {
            return java.util.stream.IntStream.range(0, BrewingRecipe.INPUT_SIZE)
                    .mapToObj(this::getItem)
                    .filter(stack -> !stack.isEmpty())
                    .map(ItemStack::copy)
                    .toList();
        }

        @Override
        public ItemStack getCup() {
            return getItem(4).copy();
        }
    }

    private MigrationGameTests() {
    }
}
