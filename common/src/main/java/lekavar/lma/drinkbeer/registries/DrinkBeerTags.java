package lekavar.lma.drinkbeer.registries;

import lekavar.lma.drinkbeer.DrinkBeer;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

public final class DrinkBeerTags {
    public static final TagKey<Item> BEERS = TagKey.create(
            Registries.ITEM,
            ResourceLocation.fromNamespaceAndPath(DrinkBeer.MOD_ID, "beers")
    );

    private DrinkBeerTags() {
    }
}
