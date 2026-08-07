package lekavar.lma.drinkbeer.gametest;

import lekavar.lma.drinkbeer.DrinkBeer;
import lekavar.lma.drinkbeer.blockentities.BartendingTableBlockEntity;
import lekavar.lma.drinkbeer.managers.MixedBeerManager;
import lekavar.lma.drinkbeer.registries.BlockRegistry;
import lekavar.lma.drinkbeer.registries.DrinkBeerTags;
import lekavar.lma.drinkbeer.registries.ItemRegistry;
import lekavar.lma.drinkbeer.registries.MobEffectRegistry;
import lekavar.lma.drinkbeer.utils.beer.Beers;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DoublePlantBlock;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import java.util.List;
import java.util.Set;

@GameTestHolder(DrinkBeer.MOD_ID)
@PrefixGameTestTemplate(false)
public final class HolidayGameTests {
    private static final BlockPos TEST_POS = new BlockPos(2, 1, 2);
    private static final Set<Item> COLORED_GIFTS = Set.of(
            ItemRegistry.GIFT_RED.get(),
            ItemRegistry.GIFT_BLUE.get(),
            ItemRegistry.GIFT_GREEN.get(),
            ItemRegistry.GIFT_WHITE.get()
    );

    @GameTest(template = "empty", timeoutTicks = 40)
    public static void giftOpensOnTheServerExactlyOnce(GameTestHelper helper) {
        helper.setBlock(TEST_POS, BlockRegistry.GIFT_RED.get());
        Player player = helper.makeMockPlayer();

        helper.useBlock(TEST_POS, player);

        helper.assertBlockPresent(Blocks.AIR, TEST_POS);
        helper.assertTrue(nonEmptyInventoryStacks(player) == 1, "Opening one gift must produce exactly one reward stack");
        helper.succeed();
    }

    @GameTest(template = "empty", timeoutTicks = 40)
    public static void ordinaryAndMixedEggnogEachGiveOneColoredGift(GameTestHelper helper) {
        Player ordinaryPlayer = helper.makeMockPlayer();
        ItemStack ordinaryEggnog = new ItemStack(ItemRegistry.BEER_MUG_FROTHY_PINK_EGGNOG.get());
        ordinaryEggnog.finishUsingItem(helper.getLevel(), ordinaryPlayer);

        Player mixedPlayer = helper.makeMockPlayer();
        ItemStack mixedEggnog = MixedBeerManager.genMixedBeerItemStack(8, List.of());
        mixedEggnog.finishUsingItem(helper.getLevel(), mixedPlayer);

        helper.assertTrue(coloredGiftCount(ordinaryPlayer) == 1, "Ordinary eggnog must give one colored gift");
        helper.assertTrue(coloredGiftCount(mixedPlayer) == 1, "Mixed eggnog must give one colored gift");
        helper.succeed();
    }

    @GameTest(template = "empty", timeoutTicks = 60)
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

        helper.getLevel().destroyBlock(helper.absolutePos(TEST_POS.above()), true, helper.makeMockPlayer());
        helper.runAfterDelay(1, () -> {
            helper.assertBlockPresent(Blocks.AIR, TEST_POS);
            helper.assertBlockPresent(Blocks.AIR, TEST_POS.above());
            helper.assertItemEntityCountIs(ItemRegistry.HORSE_MODEL_1.get(), TEST_POS, 3.0D, 1);
            helper.succeed();
        });
    }

    @GameTest(template = "empty", timeoutTicks = 40)
    public static void beerTagFeedsBartendingValidation(GameTestHelper helper) {
        for (Beers beer : Beers.values()) {
            ItemStack stack = new ItemStack(beer.getBeerItem());
            helper.assertTrue(stack.is(DrinkBeerTags.BEERS), beer.name() + " must belong to drinkbeer:beers");
        }

        ItemStack mixedBeer = MixedBeerManager.genMixedBeerItemStack(1, List.of());
        helper.assertTrue(mixedBeer.is(DrinkBeerTags.BEERS), "Mixed beer must belong to drinkbeer:beers");

        helper.setBlock(TEST_POS, BlockRegistry.BARTENDING_TABLE.get());
        var blockEntity = helper.getLevel().getBlockEntity(helper.absolutePos(TEST_POS));
        helper.assertTrue(blockEntity instanceof BartendingTableBlockEntity,
                "A placed bartending table must provide its block entity");
        var table = (BartendingTableBlockEntity) blockEntity;
        helper.assertTrue(table.placeBeer(new ItemStack(ItemRegistry.BEER_MUG.get()))
                        == BartendingTableBlockEntity.TableActionResult.SUCCESS,
                "Bartending validation must accept a tagged beer");
        helper.succeed();
    }

    @GameTest(template = "empty", timeoutTicks = 40)
    public static void ordinaryAndMixedHaarsEachAddTwoDrunkStages(GameTestHelper helper) {
        Player ordinaryPlayer = helper.makeMockPlayer();
        new ItemStack(ItemRegistry.BEER_MUG_HAARS_ICEY_PALE_LAGER.get())
                .finishUsingItem(helper.getLevel(), ordinaryPlayer);

        Player mixedPlayer = helper.makeMockPlayer();
        MixedBeerManager.genMixedBeerItemStack(6, List.of())
                .finishUsingItem(helper.getLevel(), mixedPlayer);

        var ordinaryDrunk = ordinaryPlayer.getEffect(MobEffectRegistry.DRUNK.get());
        var mixedDrunk = mixedPlayer.getEffect(MobEffectRegistry.DRUNK.get());
        helper.assertTrue(ordinaryDrunk != null && ordinaryDrunk.getAmplifier() == 1,
                "Ordinary Haar's must advance intoxication by two stages");
        helper.assertTrue(mixedDrunk != null && mixedDrunk.getAmplifier() == 1,
                "Mixed Haar's must advance intoxication by two stages");
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

    private HolidayGameTests() {
    }
}
