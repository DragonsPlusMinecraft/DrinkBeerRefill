package lekavar.lma.drinkbeer.utils.beer;

import lekavar.lma.drinkbeer.DrinkBeer;
import lekavar.lma.drinkbeer.registries.ItemRegistry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

import java.util.Arrays;
import java.util.Optional;
import java.util.function.Supplier;

public enum Beers {
    BEER_MUG(BeerDefinitions.BEER_MUG, ItemRegistry.BEER_MUG),
    BEER_MUG_BLAZE_STOUT(BeerDefinitions.BLAZE_STOUT, ItemRegistry.BEER_MUG_BLAZE_STOUT),
    BEER_MUG_BLAZE_MILK_STOUT(BeerDefinitions.BLAZE_MILK_STOUT, ItemRegistry.BEER_MUG_BLAZE_MILK_STOUT),
    BEER_MUG_APPLE_LAMBIC(BeerDefinitions.APPLE_LAMBIC, ItemRegistry.BEER_MUG_APPLE_LAMBIC),
    BEER_MUG_SWEET_BERRY_KRIEK(BeerDefinitions.SWEET_BERRY_KRIEK, ItemRegistry.BEER_MUG_SWEET_BERRY_KRIEK),
    BEER_MUG_HAARS_ICEY_PALE_LAGER(BeerDefinitions.HAARS_ICEY_PALE_LAGER, ItemRegistry.BEER_MUG_HAARS_ICEY_PALE_LAGER),
    BEER_MUG_PUMPKIN_KVASS(BeerDefinitions.PUMPKIN_KVASS, ItemRegistry.BEER_MUG_PUMPKIN_KVASS),
    BEER_MUG_FROTHY_PINK_EGGNOG(BeerDefinitions.FROTHY_PINK_EGGNOG, ItemRegistry.BEER_MUG_FROTHY_PINK_EGGNOG),
    BEER_MUG_NIGHT_HOWL_KVASS(BeerDefinitions.NIGHT_HOWL_KVASS, ItemRegistry.BEER_MUG_NIGHT_HOWL_KVASS);

    public static final int DEFAULT_BEER_ID = 1;
    public static final Beers DEFAULT_BEER = Beers.BEER_MUG;
    public static final int EMPTY_BEER_ID = 0;

    private final BeerDefinition definition;
    private final Supplier<Item> beerItem;

    Beers(BeerDefinition definition, Supplier<Item> beerItem) {
        this.definition = definition;
        this.beerItem = beerItem;
    }

    public int getId() {
        return definition.id();
    }

    public Item getBeerItem() {
        return beerItem.get();
    }

    public boolean getHasStatusEffectTooltip() {
        return definition.hasEffectTooltip();
    }

    public BeerDefinition getDefinition() {
        return definition;
    }

    public static Beers byId(int id) {
        Beers[] beers = values();
        for (Beers beer : beers) {
            if (beer.getId() == id) {
                return beer;
            }
        }
        return DEFAULT_BEER;
    }

    public static Beers byItem(Item beerItem) {
        Beers[] beers = values();
        for (Beers beer : beers) {
            if (beer.getBeerItem().equals(beerItem)) {
                return beer;
            }
        }
        return DEFAULT_BEER;
    }

    public static int size() {
        return values().length;
    }

    public static Beers byRecipeBoardBlock(Block recipeBoardBlock) {
        var boardId = BuiltInRegistries.BLOCK.getKey(recipeBoardBlock);
        String prefix = "recipe_board_";
        if (!boardId.getNamespace().equals(DrinkBeer.MOD_ID) || !boardId.getPath().startsWith(prefix)) {
            return null;
        }

        String beerPath = boardId.getPath().substring(prefix.length());
        Optional<Beers> matchedBeer = Arrays.stream(values())
                .filter(beer -> BuiltInRegistries.ITEM.getKey(beer.getBeerItem()).getPath().equals(beerPath))
                .findFirst();
        return matchedBeer.orElse(null);
    }

}
