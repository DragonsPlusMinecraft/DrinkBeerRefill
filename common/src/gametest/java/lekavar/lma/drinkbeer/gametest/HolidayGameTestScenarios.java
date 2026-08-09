package lekavar.lma.drinkbeer.gametest;

import lekavar.lma.drinkbeer.DrinkBeerConfig;
import lekavar.lma.drinkbeer.blockentities.BartendingTableBlockEntity;
import lekavar.lma.drinkbeer.blockentities.BeerBarrelBlockEntity;
import lekavar.lma.drinkbeer.blockentities.MixedBeerBlockEntity;
import lekavar.lma.drinkbeer.blockentities.TradeBoxBlockEntity;
import lekavar.lma.drinkbeer.gui.TradeBoxMenu;
import lekavar.lma.drinkbeer.managers.MixedBeerManager;
import lekavar.lma.drinkbeer.managers.TradeBoxManager;
import lekavar.lma.drinkbeer.networking.RefreshTradeBoxPayload;
import lekavar.lma.drinkbeer.networking.client.ServerPayloadHandler;
import lekavar.lma.drinkbeer.platform.ItemHandlerView;
import lekavar.lma.drinkbeer.registries.BlockEntityRegistry;
import lekavar.lma.drinkbeer.registries.BlockRegistry;
import lekavar.lma.drinkbeer.registries.DrinkBeerTags;
import lekavar.lma.drinkbeer.registries.ItemRegistry;
import lekavar.lma.drinkbeer.registries.MobEffectRegistry;
import lekavar.lma.drinkbeer.utils.beer.Beers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.TypedEntityData;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DoublePlantBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.phys.AABB;

import java.util.List;
import java.util.Set;

public final class HolidayGameTestScenarios {
    private static final BlockPos TEST_POS = new BlockPos(2, 1, 2);
    private static final Set<Item> COLORED_GIFTS = Set.of(
            ItemRegistry.GIFT_RED.get(),
            ItemRegistry.GIFT_BLUE.get(),
            ItemRegistry.GIFT_GREEN.get(),
            ItemRegistry.GIFT_WHITE.get()
    );

    public static void giftOpensOnTheServerExactlyOnce(GameTestHelper helper) {
        helper.setBlock(TEST_POS, BlockRegistry.GIFT_RED.get());
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);

        helper.useBlock(TEST_POS, player);

        helper.assertBlockPresent(Blocks.AIR, TEST_POS);
        helper.assertTrue(nonEmptyInventoryStacks(player) == 1,
                "Opening one gift must produce exactly one reward stack");
        helper.succeed();
    }

    public static void ordinaryAndMixedEggnogEachGiveOneColoredGift(GameTestHelper helper) {
        Player ordinaryPlayer = helper.makeMockPlayer(GameType.SURVIVAL);
        new ItemStack(ItemRegistry.BEER_MUG_FROTHY_PINK_EGGNOG.get())
                .finishUsingItem(helper.getLevel(), ordinaryPlayer);

        Player mixedPlayer = helper.makeMockPlayer(GameType.SURVIVAL);
        MixedBeerManager.genMixedBeerItemStack(8, List.of())
                .finishUsingItem(helper.getLevel(), mixedPlayer);

        helper.assertTrue(coloredGiftCount(ordinaryPlayer) == 1,
                "Ordinary eggnog must give one colored gift");
        helper.assertTrue(coloredGiftCount(mixedPlayer) == 1,
                "Mixed eggnog must give one colored gift");
        helper.succeed();
    }

    public static void horseModelPlacesTwoHalvesAndDropsOnlyOnce(GameTestHelper helper) {
        helper.setBlock(TEST_POS.below(), Blocks.STONE);
        var lowerState = BlockRegistry.HORSE_MODEL_1.get().defaultBlockState()
                .setValue(DoublePlantBlock.HALF, DoubleBlockHalf.LOWER);
        DoublePlantBlock.placeAt(helper.getLevel(), lowerState, helper.absolutePos(TEST_POS), 3);

        helper.assertBlockPresent(BlockRegistry.HORSE_MODEL_1.get(), TEST_POS);
        helper.assertBlockPresent(BlockRegistry.HORSE_MODEL_1.get(), TEST_POS.above());
        helper.assertTrue(helper.getBlockState(TEST_POS).getValue(DoublePlantBlock.HALF) == DoubleBlockHalf.LOWER,
                "The lower horse half must use half=lower");
        helper.assertTrue(helper.getBlockState(TEST_POS.above()).getValue(DoublePlantBlock.HALF) == DoubleBlockHalf.UPPER,
                "The upper horse half must use half=upper");

        helper.getLevel().destroyBlock(
                helper.absolutePos(TEST_POS.above()),
                true,
                helper.makeMockPlayer(GameType.SURVIVAL)
        );
        helper.runAfterDelay(1, () -> {
            helper.assertBlockPresent(Blocks.AIR, TEST_POS);
            helper.assertBlockPresent(Blocks.AIR, TEST_POS.above());
            helper.assertItemEntityCountIs(ItemRegistry.HORSE_MODEL_1.get(), TEST_POS, 3.0D, 1);
            helper.succeed();
        });
    }

    public static void beerTagFeedsBartendingValidation(GameTestHelper helper) {
        for (Beers beer : Beers.values()) {
            ItemStack stack = new ItemStack(beer.getBeerItem());
            helper.assertTrue(stack.is(DrinkBeerTags.BEERS),
                    beer.name() + " must belong to drinkbeer:beers");
        }

        ItemStack mixedBeer = MixedBeerManager.genMixedBeerItemStack(1, List.of());
        helper.assertTrue(mixedBeer.is(DrinkBeerTags.BEERS),
                "Mixed beer must belong to drinkbeer:beers");

        helper.setBlock(TEST_POS, BlockRegistry.BARTENDING_TABLE.get());
        var blockEntity = helper.getLevel().getBlockEntity(helper.absolutePos(TEST_POS));
        helper.assertTrue(blockEntity instanceof BartendingTableBlockEntity,
                "A placed bartending table must provide its block entity");
        var table = (BartendingTableBlockEntity) blockEntity;
        helper.assertTrue(
                table.placeBeer(new ItemStack(ItemRegistry.BEER_MUG.get()))
                        == BartendingTableBlockEntity.TableActionResult.SUCCESS,
                "Bartending validation must accept a tagged beer"
        );
        helper.succeed();
    }

    public static void ordinaryAndMixedHaarsEachAddTwoDrunkStages(GameTestHelper helper) {
        Player ordinaryPlayer = helper.makeMockPlayer(GameType.SURVIVAL);
        new ItemStack(ItemRegistry.BEER_MUG_HAARS_ICEY_PALE_LAGER.get())
                .finishUsingItem(helper.getLevel(), ordinaryPlayer);

        Player mixedPlayer = helper.makeMockPlayer(GameType.SURVIVAL);
        MixedBeerManager.genMixedBeerItemStack(6, List.of())
                .finishUsingItem(helper.getLevel(), mixedPlayer);

        var ordinaryDrunk = ordinaryPlayer.getEffect(MobEffectRegistry.DRUNK);
        var mixedDrunk = mixedPlayer.getEffect(MobEffectRegistry.DRUNK);
        helper.assertTrue(ordinaryDrunk != null && ordinaryDrunk.getAmplifier() == 1,
                "Ordinary Haar's must advance intoxication by two stages");
        helper.assertTrue(mixedDrunk != null && mixedDrunk.getAmplifier() == 1,
                "Mixed Haar's must advance intoxication by two stages");
        helper.succeed();
    }

    public static void beerBarrelCompletesLifecycleWithSidedAutomation(GameTestHelper helper) {
        helper.setBlock(TEST_POS, BlockRegistry.BEER_BARREL.get());
        BlockEntity blockEntity = helper.getLevel().getBlockEntity(helper.absolutePos(TEST_POS));
        helper.assertTrue(blockEntity instanceof BeerBarrelBlockEntity,
                "A placed beer barrel must provide its block entity");
        BeerBarrelBlockEntity barrel = (BeerBarrelBlockEntity) blockEntity;

        ItemHandlerView ingredients = barrel.getItemHandler(Direction.UP);
        ItemHandlerView cups = barrel.getItemHandler(Direction.NORTH);
        ItemHandlerView output = barrel.getItemHandler(Direction.DOWN);
        helper.assertTrue(ingredients.getSlots() == 4 && cups.getSlots() == 1 && output.getSlots() == 5,
                "Barrel faces must expose ingredient, cup, and output views");

        ItemStack simulatedWheat = new ItemStack(Items.WHEAT, 2);
        ItemStack simulatedRemainder = ingredients.insertItem(0, simulatedWheat, true);
        helper.assertTrue(simulatedRemainder.getCount() == 1 && ingredients.getStackInSlot(0).isEmpty(),
                "Simulated ingredient insertion must report a remainder without mutation");

        helper.assertTrue(ingredients.insertItem(0, new ItemStack(Items.WHEAT), false).isEmpty(),
                "First wheat must insert from above");
        helper.assertTrue(ingredients.insertItem(1, new ItemStack(Items.WHEAT), false).isEmpty(),
                "Second wheat must insert from above");
        helper.assertTrue(ingredients.insertItem(2, new ItemStack(Items.WHEAT), false).isEmpty(),
                "Third wheat must insert from above");
        helper.assertTrue(ingredients.insertItem(3, new ItemStack(Items.WATER_BUCKET), false).isEmpty(),
                "Water bucket must insert from above");
        helper.assertTrue(cups.insertItem(0, new ItemStack(ItemRegistry.EMPTY_BEER_MUG.get(), 4), false).isEmpty(),
                "Four empty mugs must insert from a horizontal face");

        barrel.tickServer();
        helper.assertTrue(barrel.syncData.get(1) == BeerBarrelBlockEntity.STATUS_BREWING
                        && barrel.syncData.get(0) > 0,
                "A complete input set must start brewing");
        helper.assertTrue(ingredients.insertItem(0, new ItemStack(Items.WHEAT), false).getCount() == 1,
                "Inputs must lock while brewing");

        CompoundTag brewingSave = barrel.saveWithFullMetadata(helper.getLevel().registryAccess());
        BlockEntity restored = BlockEntity.loadStatic(
                helper.absolutePos(TEST_POS),
                helper.getBlockState(TEST_POS),
                brewingSave,
                helper.getLevel().registryAccess()
        );
        helper.assertTrue(restored instanceof BeerBarrelBlockEntity
                        && ((BeerBarrelBlockEntity) restored).syncData.get(1) == BeerBarrelBlockEntity.STATUS_BREWING
                        && ((BeerBarrelBlockEntity) restored).syncData.get(0) == barrel.syncData.get(0),
                "A brewing barrel must preserve status and progress across save/load");

        barrel.syncData.set(0, 1);
        barrel.tickServer();
        helper.assertTrue(barrel.syncData.get(1) == BeerBarrelBlockEntity.STATUS_READY,
                "The final brewing tick must transition to ready");
        helper.assertTrue(barrel.getBrewingInventory().getItem(5).is(ItemRegistry.BEER_MUG.get())
                        && barrel.getBrewingInventory().getItem(5).getCount() == 4,
                "The completed recipe must produce four beers");
        helper.assertTrue(barrel.getBrewingInventory().getItem(3).is(Items.BUCKET),
                "The water bucket must be returned after brewing");

        ItemStack simulatedOutput = output.extractItem(4, 4, true);
        helper.assertTrue(simulatedOutput.getCount() == 4
                        && barrel.getBrewingInventory().getItem(5).getCount() == 4,
                "Simulated output extraction must not mutate the barrel");
        ItemStack extractedOutput = output.extractItem(4, 4, false);
        helper.assertTrue(extractedOutput.is(ItemRegistry.BEER_MUG.get()) && extractedOutput.getCount() == 4,
                "Finished beer must extract only from below");
        helper.assertTrue(output.extractItem(3, 1, false).is(Items.BUCKET),
                "Returned buckets must also extract from below");
        barrel.tickServer();
        helper.assertTrue(barrel.syncData.get(1) == BeerBarrelBlockEntity.STATUS_WAITING,
                "An emptied ready barrel must return to waiting");
        helper.succeed();
    }

    public static void bartendingMixingPersistsAndDropsItsResult(GameTestHelper helper) {
        helper.setBlock(TEST_POS, BlockRegistry.BARTENDING_TABLE.get());
        BlockEntity blockEntity = helper.getLevel().getBlockEntity(helper.absolutePos(TEST_POS));
        helper.assertTrue(blockEntity instanceof BartendingTableBlockEntity,
                "A placed bartending table must provide its block entity");
        BartendingTableBlockEntity table = (BartendingTableBlockEntity) blockEntity;
        ItemHandlerView beerInput = table.getItemHandler(Direction.UP);
        ItemHandlerView spiceInput = table.getItemHandler(Direction.NORTH);
        ItemHandlerView output = table.getItemHandler(Direction.DOWN);

        helper.assertTrue(beerInput.insertItem(0, new ItemStack(ItemRegistry.BEER_MUG.get()), true).isEmpty()
                        && table.getInventory().isEmpty(),
                "Simulated beer insertion must not mutate the table");
        helper.assertTrue(beerInput.insertItem(0, new ItemStack(ItemRegistry.BEER_MUG.get()), false).isEmpty(),
                "Beer must insert from above");
        helper.assertTrue(spiceInput.insertItem(0, new ItemStack(ItemRegistry.SPICE_BLAZE_PAPRIKA.get()), false).isEmpty(),
                "Spice must insert from a horizontal face");

        ItemStack mixed = output.extractItem(0, 1, true);
        helper.assertTrue(MixedBeerManager.getBeerId(mixed) == Beers.BEER_MUG.getId()
                        && MixedBeerManager.getSpiceList(mixed).equals(List.of(1)),
                "Mixing must preserve the base beer and spice IDs");
        helper.assertTrue(!table.getInventory().isEmpty(),
                "Simulated extraction must preserve table contents");

        CompoundTag saved = table.saveWithFullMetadata(helper.getLevel().registryAccess());
        BlockEntity restored = BlockEntity.loadStatic(
                helper.absolutePos(TEST_POS),
                helper.getBlockState(TEST_POS),
                saved,
                helper.getLevel().registryAccess()
        );
        helper.assertTrue(restored instanceof BartendingTableBlockEntity,
                "Saved bartending data must recreate its block entity");
        ItemStack restoredBeer = ((BartendingTableBlockEntity) restored).takeBeer(true);
        helper.assertTrue(MixedBeerManager.getBeerId(restoredBeer) == Beers.BEER_MUG.getId()
                        && MixedBeerManager.getSpiceList(restoredBeer).equals(List.of(1)),
                "Saved bartending contents must retain mixed-beer data");

        helper.getLevel().destroyBlock(
                helper.absolutePos(TEST_POS),
                true,
                helper.makeMockPlayer(GameType.SURVIVAL)
        );
        helper.runAfterDelay(1, () -> {
            AABB search = new AABB(helper.absolutePos(TEST_POS)).inflate(3.0D);
            List<ItemEntity> mixedDrops = helper.getLevel().getEntitiesOfClass(ItemEntity.class, search).stream()
                    .filter(entity -> entity.getItem().is(ItemRegistry.MIXED_BEER.get()))
                    .toList();
            helper.assertTrue(mixedDrops.size() == 1,
                    "Breaking a filled bartending table must drop one mixed beer");
            ItemStack dropped = mixedDrops.getFirst().getItem();
            helper.assertTrue(MixedBeerManager.getBeerId(dropped) == Beers.BEER_MUG.getId()
                            && MixedBeerManager.getSpiceList(dropped).equals(List.of(1)),
                    "The dropped mixed beer must retain its component data");
            helper.succeed();
        });
    }

    public static void mixedBeerLegacyAndModernDataRoundTripInWorld(GameTestHelper helper) {
        helper.setBlock(TEST_POS, BlockRegistry.MIXED_BEER.get());
        BlockEntity blockEntity = helper.getLevel().getBlockEntity(helper.absolutePos(TEST_POS));
        helper.assertTrue(blockEntity instanceof MixedBeerBlockEntity,
                "A placed mixed beer must provide its block entity");
        MixedBeerBlockEntity mixedBeer = (MixedBeerBlockEntity) blockEntity;

        CompoundTag descriptor = new CompoundTag();
        descriptor.putInt("beerId", 6);
        descriptor.putIntArray("spiceList", new int[]{4, 8});
        CompoundTag legacyBlockEntity = new CompoundTag();
        legacyBlockEntity.put("MixedBeer", descriptor);
        CompoundTag legacyRoot = new CompoundTag();
        legacyRoot.put("BlockEntityTag", legacyBlockEntity);
        ItemStack legacyStack = new ItemStack(ItemRegistry.MIXED_BEER.get());
        legacyStack.set(
                DataComponents.BLOCK_ENTITY_DATA,
                TypedEntityData.of(BlockEntityRegistry.MIXED_BEER_TILEENTITY.get(), legacyRoot)
        );
        mixedBeer.applyComponentsFromItemStack(legacyStack);
        helper.assertTrue(mixedBeer.getBeerId() == 6 && mixedBeer.getSpiceList().equals(List.of(4, 8)),
                "Legacy BlockEntityTag data must apply during placement");

        CompoundTag saved = mixedBeer.saveWithFullMetadata(helper.getLevel().registryAccess());
        BlockEntity restored = BlockEntity.loadStatic(
                helper.absolutePos(TEST_POS),
                helper.getBlockState(TEST_POS),
                saved,
                helper.getLevel().registryAccess()
        );
        helper.assertTrue(restored instanceof MixedBeerBlockEntity
                        && ((MixedBeerBlockEntity) restored).getBeerId() == 6
                        && ((MixedBeerBlockEntity) restored).getSpiceList().equals(List.of(4, 8)),
                "Mixed-beer block data must survive save/load");

        ItemStack recovered = mixedBeer.getPickStack();
        helper.assertTrue(MixedBeerManager.getBeerId(recovered) == 6
                        && MixedBeerManager.getSpiceList(recovered).equals(List.of(4, 8)),
                "Recovered mixed-beer items must use modern components without data loss");
        ItemStack sanitized = MixedBeerManager.genMixedBeerItemStack(4, List.of(1, 7, 15, 99));
        helper.assertTrue(MixedBeerManager.getBeerId(sanitized) == 4
                        && MixedBeerManager.getSpiceList(sanitized).equals(List.of(1, 7, 15)),
                "Modern mixed-beer components must cap and sanitize spice IDs");
        helper.succeed();
    }

    public static void tradeBoxPayloadRejectsDuplicatesAndMalformedTargets(GameTestHelper helper) {
        helper.setBlock(TEST_POS, BlockRegistry.TRADE_BOX.get());
        BlockPos absolutePos = helper.absolutePos(TEST_POS);
        BlockEntity blockEntity = helper.getLevel().getBlockEntity(absolutePos);
        helper.assertTrue(blockEntity instanceof TradeBoxBlockEntity,
                "A placed trade box must provide its block entity");
        TradeBoxBlockEntity tradeBox = (TradeBoxBlockEntity) blockEntity;
        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        player.setPos(absolutePos.getX() + 0.5D, absolutePos.getY() + 0.5D, absolutePos.getZ() + 0.5D);
        TradeBoxMenu menu = new TradeBoxMenu(
                1,
                tradeBox.goodInventory,
                tradeBox.syncData,
                player.getInventory(),
                tradeBox
        );
        player.containerMenu = menu;
        menu.setTradeboxTrading();
        helper.assertTrue(menu.isTrading(), "The test trade box must start in trading mode");

        ServerPayloadHandler.handlePayload(new RefreshTradeBoxPayload(absolutePos), player);
        helper.assertTrue(menu.isCooling() && menu.getCoolingTime() == TradeBoxManager.COOLING_TIME_ON_REFRESH,
                "A valid refresh payload must start the configured cooldown");
        int coolingAfterFirstPayload = menu.getCoolingTime();
        ServerPayloadHandler.handlePayload(new RefreshTradeBoxPayload(absolutePos), player);
        helper.assertTrue(menu.getCoolingTime() == coolingAfterFirstPayload,
                "A duplicate payload during cooldown must be ignored");

        BlockPos farAway = new BlockPos(1_000_000, 64, 1_000_000);
        int farChunkX = farAway.getX() >> 4;
        int farChunkZ = farAway.getZ() >> 4;
        helper.assertTrue(!helper.getLevel().getChunkSource().hasChunk(farChunkX, farChunkZ),
                "The malformed payload target chunk must begin unloaded");
        menu.setTradeboxTrading();
        ServerPayloadHandler.handlePayload(new RefreshTradeBoxPayload(farAway), player);
        helper.assertTrue(menu.isTrading(), "A payload for a different block position must be rejected");
        helper.assertTrue(!helper.getLevel().getChunkSource().hasChunk(farChunkX, farChunkZ),
                "Payload validation must never force-load its target chunk");
        helper.succeed();
    }

    public static void configAndEmptyInventoryBoundariesAreSafe(GameTestHelper helper) {
        DrinkBeerConfig.Values lower = new DrinkBeerConfig.Values(-5.0D, false, -1);
        DrinkBeerConfig.Values upper = new DrinkBeerConfig.Values(5.0D, true, 100_000);
        DrinkBeerConfig.Values nonFinite = new DrinkBeerConfig.Values(Double.NaN, true, 1);
        helper.assertTrue(lower.beerSaturationModifier() == 0.0D && lower.maxWorldChangesPerDrink() == 0,
                "Configuration values must clamp at their lower bounds");
        helper.assertTrue(upper.beerSaturationModifier() == 1.0D && upper.maxWorldChangesPerDrink() == 32_768,
                "Configuration values must clamp at their upper bounds");
        helper.assertTrue(nonFinite.beerSaturationModifier() == DrinkBeerConfig.DEFAULT_BEER_SATURATION_MODIFIER,
                "Non-finite saturation must fall back to the default");

        helper.setBlock(TEST_POS, BlockRegistry.BEER_BARREL.get());
        BeerBarrelBlockEntity barrel = (BeerBarrelBlockEntity) helper.getLevel()
                .getBlockEntity(helper.absolutePos(TEST_POS));
        helper.assertTrue(barrel.getItemHandler(Direction.DOWN).extractItem(4, 64, false).isEmpty(),
                "Extracting from an empty barrel must be safe");

        BlockPos tablePos = TEST_POS.east(2);
        helper.setBlock(tablePos, BlockRegistry.BARTENDING_TABLE.get());
        BartendingTableBlockEntity table = (BartendingTableBlockEntity) helper.getLevel()
                .getBlockEntity(helper.absolutePos(tablePos));
        helper.assertTrue(table.getItemHandler(Direction.DOWN).extractItem(0, 64, false).isEmpty(),
                "Extracting from an empty bartending table must be safe");
        helper.assertTrue(table.getItemHandler(Direction.NORTH)
                        .insertItem(0, new ItemStack(ItemRegistry.SPICE_BLAZE_PAPRIKA.get()), false)
                        .getCount() == 1,
                "A spice cannot enter an empty bartending table");
        helper.succeed();
    }

    private static int coloredGiftCount(Player player) {
        int count = 0;
        for (int slot = 0; slot < player.getInventory().getContainerSize(); slot++) {
            ItemStack stack = player.getInventory().getItem(slot);
            if (COLORED_GIFTS.contains(stack.getItem())) {
                count += stack.getCount();
            }
        }
        return count;
    }

    private static int nonEmptyInventoryStacks(Player player) {
        int count = 0;
        for (int slot = 0; slot < player.getInventory().getContainerSize(); slot++) {
            if (!player.getInventory().getItem(slot).isEmpty()) {
                count++;
            }
        }
        return count;
    }

    private HolidayGameTestScenarios() {
    }
}
