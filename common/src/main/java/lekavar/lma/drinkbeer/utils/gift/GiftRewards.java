package lekavar.lma.drinkbeer.utils.gift;

import lekavar.lma.drinkbeer.registries.ItemRegistry;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.function.Supplier;

public final class GiftRewards {
    private static final List<Supplier<ItemStack>> REWARDS = List.of(
            () -> new ItemStack(ItemRegistry.BEER_MUG.get()),
            () -> new ItemStack(ItemRegistry.BEER_MUG_BLAZE_STOUT.get()),
            () -> new ItemStack(ItemRegistry.BEER_MUG_BLAZE_MILK_STOUT.get()),
            () -> new ItemStack(ItemRegistry.BEER_MUG_APPLE_LAMBIC.get()),
            () -> new ItemStack(ItemRegistry.BEER_MUG_SWEET_BERRY_KRIEK.get()),
            () -> new ItemStack(ItemRegistry.BEER_MUG_HAARS_ICEY_PALE_LAGER.get()),
            () -> new ItemStack(ItemRegistry.BEER_MUG_PUMPKIN_KVASS.get()),
            () -> new ItemStack(ItemRegistry.BEER_MUG_FROTHY_PINK_EGGNOG.get()),
            () -> new ItemStack(ItemRegistry.BEER_MUG_NIGHT_HOWL_KVASS.get()),
            () -> new ItemStack(ItemRegistry.SPICE_BLAZE_PAPRIKA.get(), 2),
            () -> new ItemStack(ItemRegistry.SPICE_DRIED_EGLIA_BUD.get(), 2),
            () -> new ItemStack(ItemRegistry.SPICE_SMOKED_EGLIA_BUD.get(), 2),
            () -> new ItemStack(ItemRegistry.SPICE_AMETHYST_NIGELLA_SEEDS.get(), 2),
            () -> new ItemStack(ItemRegistry.SPICE_CITRINE_NIGELLA_SEEDS.get(), 2),
            () -> new ItemStack(ItemRegistry.SPICE_ICE_MINT.get(), 2),
            () -> new ItemStack(ItemRegistry.SPICE_ICE_PATCHOULI.get(), 2),
            () -> new ItemStack(ItemRegistry.SPICE_STORM_SHARDS.get(), 2),
            () -> new ItemStack(ItemRegistry.SPICE_ROASTED_RED_PINE_NUTS.get(), 2),
            () -> new ItemStack(ItemRegistry.SPICE_GLACE_GOJI_BERRIES.get(), 2),
            () -> new ItemStack(ItemRegistry.SPICE_FROZEN_PERSIMMON.get(), 2),
            () -> new ItemStack(ItemRegistry.SPICE_ROASTED_PECANS.get(), 2),
            () -> new ItemStack(ItemRegistry.SPICE_SILVER_NEEDLE_WHITE_TEA.get(), 2),
            () -> new ItemStack(ItemRegistry.SPICE_GOLDEN_CINNAMON_POWDER.get(), 2),
            () -> new ItemStack(ItemRegistry.SPICE_DRIED_SELAGINELLA.get(), 2),
            () -> new ItemStack(ItemRegistry.HORSE_MODEL_1.get()),
            () -> new ItemStack(ItemRegistry.HORSE_MODEL_2.get()),
            () -> new ItemStack(ItemRegistry.HORSE_MODEL_3.get())
    );

    private static final List<Supplier<ItemStack>> COLORED_GIFTS = List.of(
            () -> new ItemStack(ItemRegistry.GIFT_RED.get()),
            () -> new ItemStack(ItemRegistry.GIFT_BLUE.get()),
            () -> new ItemStack(ItemRegistry.GIFT_GREEN.get()),
            () -> new ItemStack(ItemRegistry.GIFT_WHITE.get())
    );

    public static int rewardCount() {
        return REWARDS.size();
    }

    public static ItemStack rewardAt(int index) {
        if (index < 0 || index >= REWARDS.size()) {
            throw new IndexOutOfBoundsException("Gift reward index: " + index);
        }
        return REWARDS.get(index).get();
    }

    public static ItemStack randomReward(RandomSource random) {
        return rewardAt(random.nextInt(REWARDS.size()));
    }

    public static ItemStack randomColoredGift(RandomSource random) {
        return coloredGiftAt(random.nextInt(COLORED_GIFTS.size()));
    }

    public static int coloredGiftCount() {
        return COLORED_GIFTS.size();
    }

    public static ItemStack coloredGiftAt(int index) {
        if (index < 0 || index >= COLORED_GIFTS.size()) {
            throw new IndexOutOfBoundsException("Colored gift index: " + index);
        }
        return COLORED_GIFTS.get(index).get();
    }

    public static void giveOrDrop(Player player, ItemStack stack) {
        player.getInventory().add(stack);
        if (!stack.isEmpty()) {
            player.drop(stack, false);
        }
    }

    private GiftRewards() {
    }
}
