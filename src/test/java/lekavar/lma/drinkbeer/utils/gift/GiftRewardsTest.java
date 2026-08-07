package lekavar.lma.drinkbeer.utils.gift;

import lekavar.lma.drinkbeer.registries.ItemRegistry;
import lekavar.lma.drinkbeer.utils.beer.Beers;
import lekavar.lma.drinkbeer.utils.mixedbeer.Spices;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class GiftRewardsTest {
    @Test
    void rewardPoolMatchesAllTwentySevenUpstreamEntries() {
        assertEquals(27, GiftRewards.rewardCount());

        for (int i = 0; i < Beers.values().length; i++) {
            ItemStack reward = GiftRewards.rewardAt(i);
            assertEquals(Beers.values()[i].getBeerItem(), reward.getItem());
            assertEquals(1, reward.getCount());
        }
        for (int i = 0; i < Spices.values().length; i++) {
            ItemStack reward = GiftRewards.rewardAt(9 + i);
            assertEquals(Spices.values()[i].getSpiceItem(), reward.getItem());
            assertEquals(2, reward.getCount());
        }

        List<Item> horses = List.of(ItemRegistry.HORSE_MODEL_1.get(), ItemRegistry.HORSE_MODEL_2.get(), ItemRegistry.HORSE_MODEL_3.get());
        for (int i = 0; i < horses.size(); i++) {
            ItemStack reward = GiftRewards.rewardAt(24 + i);
            assertEquals(horses.get(i), reward.getItem());
            assertEquals(1, reward.getCount());
        }
    }

    @Test
    void coloredGiftPoolContainsEachColorExactlyOnce() {
        assertEquals(4, GiftRewards.coloredGiftCount());
        assertEquals(
                List.of(ItemRegistry.GIFT_RED.get(), ItemRegistry.GIFT_BLUE.get(), ItemRegistry.GIFT_GREEN.get(), ItemRegistry.GIFT_WHITE.get()),
                List.of(0, 1, 2, 3).stream().map(index -> GiftRewards.coloredGiftAt(index).getItem()).toList()
        );
    }

    @Test
    void invalidRewardIndexesAreRejected() {
        assertThrows(IndexOutOfBoundsException.class, () -> GiftRewards.rewardAt(-1));
        assertThrows(IndexOutOfBoundsException.class, () -> GiftRewards.rewardAt(27));
        assertThrows(IndexOutOfBoundsException.class, () -> GiftRewards.coloredGiftAt(4));
    }
}
