package lekavar.lma.drinkbeer.registries;

import lekavar.lma.drinkbeer.items.BeerMugItem;
import lekavar.lma.drinkbeer.items.MixedBeerBlockItem;
import lekavar.lma.drinkbeer.items.SpiceBlockItem;
import lekavar.lma.drinkbeer.platform.Registration;
import lekavar.lma.drinkbeer.platform.RegistryProvider;
import lekavar.lma.drinkbeer.utils.beer.BeerDefinitions;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

import java.util.function.Supplier;

public class ItemRegistry {
    private static final RegistryProvider<Item> ITEMS = Registration.provider(BuiltInRegistries.ITEM);

    //general
    public static final Supplier<Item> BEER_BARREL = ITEMS.register("beer_barrel", () -> new BlockItem(BlockRegistry.BEER_BARREL.get(), itemProperties(BlockRegistry.BEER_BARREL.get())));
    public static final Supplier<Item> BARTENDING_TABLE = ITEMS.register("bartending_table_normal", () -> new BlockItem(BlockRegistry.BARTENDING_TABLE.get(), itemProperties(BlockRegistry.BARTENDING_TABLE.get())));
    public static final Supplier<Item> TRADE_BOX = ITEMS.register("trade_box_normal", () -> new BlockItem(BlockRegistry.TRADE_BOX.get(), itemProperties(BlockRegistry.TRADE_BOX.get())));
    public static final Supplier<Item> EMPTY_BEER_MUG = ITEMS.register("empty_beer_mug", () -> new BlockItem(BlockRegistry.EMPTY_BEER_MUG.get(), itemProperties(BlockRegistry.EMPTY_BEER_MUG.get())));

    public static final Supplier<Item> IRON_CALL_BELL = ITEMS.register("iron_call_bell", () -> new BlockItem(BlockRegistry.IRON_CALL_BELL.get(), itemProperties(BlockRegistry.IRON_CALL_BELL.get())));
    public static final Supplier<Item> GOLDEN_CALL_BELL = ITEMS.register("golden_call_bell", () -> new BlockItem(BlockRegistry.GOLDEN_CALL_BELL.get(), itemProperties(BlockRegistry.GOLDEN_CALL_BELL.get())));
    public static final Supplier<Item> LEKAS_CALL_BELL = ITEMS.register("lekas_call_bell", () -> new BlockItem(BlockRegistry.LEKAS_CALL_BELL.get(), itemProperties(BlockRegistry.LEKAS_CALL_BELL.get())));

    // Authorized upstream 4.0 holiday content
    public static final Supplier<Item> GIFT_RED = ITEMS.register("gift_red", () -> new BlockItem(BlockRegistry.GIFT_RED.get(), itemProperties(BlockRegistry.GIFT_RED.get())));
    public static final Supplier<Item> GIFT_BLUE = ITEMS.register("gift_blue", () -> new BlockItem(BlockRegistry.GIFT_BLUE.get(), itemProperties(BlockRegistry.GIFT_BLUE.get())));
    public static final Supplier<Item> GIFT_GREEN = ITEMS.register("gift_green", () -> new BlockItem(BlockRegistry.GIFT_GREEN.get(), itemProperties(BlockRegistry.GIFT_GREEN.get())));
    public static final Supplier<Item> GIFT_WHITE = ITEMS.register("gift_white", () -> new BlockItem(BlockRegistry.GIFT_WHITE.get(), itemProperties(BlockRegistry.GIFT_WHITE.get())));
    public static final Supplier<Item> COLORED_LIGHTS = ITEMS.register("colored_lights", () -> new BlockItem(BlockRegistry.COLORED_LIGHTS.get(), itemProperties(BlockRegistry.COLORED_LIGHTS.get())));
    public static final Supplier<Item> SIDE_COLORED_LIGHTS = ITEMS.register("side_colored_lights", () -> new BlockItem(BlockRegistry.SIDE_COLORED_LIGHTS.get(), itemProperties(BlockRegistry.SIDE_COLORED_LIGHTS.get())));
    public static final Supplier<Item> STAR_OF_BETHLEHEM = ITEMS.register("star_of_bethlehem", () -> new BlockItem(BlockRegistry.STAR_OF_BETHLEHEM.get(), itemProperties(BlockRegistry.STAR_OF_BETHLEHEM.get()).fireResistant()));
    public static final Supplier<Item> THE_GREAT_STAR_OF_BETHLEHEM = ITEMS.register("the_great_star_of_bethlehem", () -> new BlockItem(BlockRegistry.THE_GREAT_STAR_OF_BETHLEHEM.get(), itemProperties(BlockRegistry.THE_GREAT_STAR_OF_BETHLEHEM.get()).fireResistant()));
    public static final Supplier<Item> HORSE_MODEL_1 = ITEMS.register("horse_model_1", () -> new BlockItem(BlockRegistry.HORSE_MODEL_1.get(), itemProperties(BlockRegistry.HORSE_MODEL_1.get())));
    public static final Supplier<Item> HORSE_MODEL_2 = ITEMS.register("horse_model_2", () -> new BlockItem(BlockRegistry.HORSE_MODEL_2.get(), itemProperties(BlockRegistry.HORSE_MODEL_2.get())));
    public static final Supplier<Item> HORSE_MODEL_3 = ITEMS.register("horse_model_3", () -> new BlockItem(BlockRegistry.HORSE_MODEL_3.get(), itemProperties(BlockRegistry.HORSE_MODEL_3.get())));

    public static final Supplier<Item> RECIPE_BOARD_BEER_MUG = ITEMS.register("recipe_board_beer_mug", () -> new BlockItem(BlockRegistry.RECIPE_BOARD_BEER_MUG.get(), itemProperties(BlockRegistry.RECIPE_BOARD_BEER_MUG.get()).stacksTo(1)));
    public static final Supplier<Item> RECIPE_BOARD_BEER_MUG_BLAZE_STOUT = ITEMS.register("recipe_board_beer_mug_blaze_stout", () -> new BlockItem(BlockRegistry.RECIPE_BOARD_BEER_MUG_BLAZE_STOUT.get(), itemProperties(BlockRegistry.RECIPE_BOARD_BEER_MUG_BLAZE_STOUT.get()).stacksTo(1)));
    public static final Supplier<Item> RECIPE_BOARD_BEER_MUG_BLAZE_MILK_STOUT = ITEMS.register("recipe_board_beer_mug_blaze_milk_stout", () -> new BlockItem(BlockRegistry.RECIPE_BOARD_BEER_MUG_BLAZE_MILK_STOUT.get(), itemProperties(BlockRegistry.RECIPE_BOARD_BEER_MUG_BLAZE_MILK_STOUT.get()).stacksTo(1)));
    public static final Supplier<Item> RECIPE_BOARD_BEER_MUG_APPLE_LAMBIC = ITEMS.register("recipe_board_beer_mug_apple_lambic", () -> new BlockItem(BlockRegistry.RECIPE_BOARD_BEER_MUG_APPLE_LAMBIC.get(), itemProperties(BlockRegistry.RECIPE_BOARD_BEER_MUG_APPLE_LAMBIC.get()).stacksTo(1)));
    public static final Supplier<Item> RECIPE_BOARD_BEER_MUG_SWEET_BERRY_KRIEK = ITEMS.register("recipe_board_beer_mug_sweet_berry_kriek", () -> new BlockItem(BlockRegistry.RECIPE_BOARD_BEER_MUG_SWEET_BERRY_KRIEK.get(), itemProperties(BlockRegistry.RECIPE_BOARD_BEER_MUG_SWEET_BERRY_KRIEK.get()).stacksTo(1)));
    public static final Supplier<Item> RECIPE_BOARD_BEER_MUG_HAARS_ICEY_PALE_LAGER = ITEMS.register("recipe_board_beer_mug_haars_icey_pale_lager", () -> new BlockItem(BlockRegistry.RECIPE_BOARD_BEER_MUG_HAARS_ICEY_PALE_LAGER.get(), itemProperties(BlockRegistry.RECIPE_BOARD_BEER_MUG_HAARS_ICEY_PALE_LAGER.get()).stacksTo(1)));
    public static final Supplier<Item> RECIPE_BOARD_BEER_MUG_PUMPKIN_KVASS = ITEMS.register("recipe_board_beer_mug_pumpkin_kvass", () -> new BlockItem(BlockRegistry.RECIPE_BOARD_BEER_MUG_PUMPKIN_KVASS.get(), itemProperties(BlockRegistry.RECIPE_BOARD_BEER_MUG_PUMPKIN_KVASS.get()).stacksTo(1)));
    public static final Supplier<Item> RECIPE_BOARD_BEER_MUG_NIGHT_HOWL_KVASS = ITEMS.register("recipe_board_beer_mug_night_howl_kvass", () -> new BlockItem(BlockRegistry.RECIPE_BOARD_BEER_MUG_NIGHT_HOWL_KVASS.get(), itemProperties(BlockRegistry.RECIPE_BOARD_BEER_MUG_NIGHT_HOWL_KVASS.get()).stacksTo(1)));
    public static final Supplier<Item> RECIPE_BOARD_BEER_MUG_FROTHY_PINK_EGGNOG = ITEMS.register("recipe_board_beer_mug_frothy_pink_eggnog", () -> new BlockItem(BlockRegistry.RECIPE_BOARD_BEER_MUG_FROTHY_PINK_EGGNOG.get(), itemProperties(BlockRegistry.RECIPE_BOARD_BEER_MUG_FROTHY_PINK_EGGNOG.get()).stacksTo(1)));

    public static final Supplier<Item> RECIPE_BOARD_PACKAGE = ITEMS.register("recipe_board_package", () -> new BlockItem(BlockRegistry.RECIPE_BOARD_PACKAGE.get(), itemProperties(BlockRegistry.RECIPE_BOARD_PACKAGE.get()).stacksTo(1)));

    //beer
    public static final Supplier<Item> BEER_MUG = ITEMS.register("beer_mug", () -> new BeerMugItem(BlockRegistry.BEER_MUG.get(), BeerDefinitions.BEER_MUG));
    public static final Supplier<Item> BEER_MUG_BLAZE_STOUT = ITEMS.register("beer_mug_blaze_stout", () -> new BeerMugItem(BlockRegistry.BEER_MUG_BLAZE_STOUT.get(), BeerDefinitions.BLAZE_STOUT));
    public static final Supplier<Item> BEER_MUG_BLAZE_MILK_STOUT = ITEMS.register("beer_mug_blaze_milk_stout", () -> new BeerMugItem(BlockRegistry.BEER_MUG_BLAZE_MILK_STOUT.get(), BeerDefinitions.BLAZE_MILK_STOUT));
    public static final Supplier<Item> BEER_MUG_APPLE_LAMBIC = ITEMS.register("beer_mug_apple_lambic", () -> new BeerMugItem(BlockRegistry.BEER_MUG_APPLE_LAMBIC.get(), BeerDefinitions.APPLE_LAMBIC));
    public static final Supplier<Item> BEER_MUG_SWEET_BERRY_KRIEK = ITEMS.register("beer_mug_sweet_berry_kriek", () -> new BeerMugItem(BlockRegistry.BEER_MUG_SWEET_BERRY_KRIEK.get(), BeerDefinitions.SWEET_BERRY_KRIEK));
    public static final Supplier<Item> BEER_MUG_HAARS_ICEY_PALE_LAGER = ITEMS.register("beer_mug_haars_icey_pale_lager", () -> new BeerMugItem(BlockRegistry.BEER_MUG_HAARS_ICEY_PALE_LAGER.get(), BeerDefinitions.HAARS_ICEY_PALE_LAGER));
    public static final Supplier<Item> BEER_MUG_PUMPKIN_KVASS = ITEMS.register("beer_mug_pumpkin_kvass", () -> new BeerMugItem(BlockRegistry.BEER_MUG_PUMPKIN_KVASS.get(), BeerDefinitions.PUMPKIN_KVASS));
    public static final Supplier<Item> BEER_MUG_NIGHT_HOWL_KVASS = ITEMS.register("beer_mug_night_howl_kvass", () -> new BeerMugItem(BlockRegistry.BEER_MUG_NIGHT_HOWL_KVASS.get(), BeerDefinitions.NIGHT_HOWL_KVASS));
    public static final Supplier<Item> BEER_MUG_FROTHY_PINK_EGGNOG = ITEMS.register("beer_mug_frothy_pink_eggnog", () -> new BeerMugItem(BlockRegistry.BEER_MUG_FROTHY_PINK_EGGNOG.get(), BeerDefinitions.FROTHY_PINK_EGGNOG));
    public static final Supplier<Item> MIXED_BEER = ITEMS.register("mixed_beer", () -> new MixedBeerBlockItem(BlockRegistry.MIXED_BEER.get()));

    // Spices
    public static final Supplier<Item> SPICE_BLAZE_PAPRIKA = ITEMS.register("spice_blaze_paprika", () -> new SpiceBlockItem(BlockRegistry.SPICE_BLAZE_PAPRIKA.get(), null, 1));
    public static final Supplier<Item> SPICE_DRIED_EGLIA_BUD = ITEMS.register("spice_dried_eglia_bud", () -> new SpiceBlockItem(BlockRegistry.SPICE_DRIED_EGLIA_BUD.get(), null, 1));
    public static final Supplier<Item> SPICE_SMOKED_EGLIA_BUD = ITEMS.register("spice_smoked_eglia_bud", () -> new SpiceBlockItem(BlockRegistry.SPICE_SMOKED_EGLIA_BUD.get(), null, 1));
    public static final Supplier<Item> SPICE_AMETHYST_NIGELLA_SEEDS = ITEMS.register("spice_amethyst_nigella_seeds", () -> new SpiceBlockItem(BlockRegistry.SPICE_AMETHYST_NIGELLA_SEEDS.get(), null, 1));
    public static final Supplier<Item> SPICE_CITRINE_NIGELLA_SEEDS = ITEMS.register("spice_citrine_nigella_seeds", () -> new SpiceBlockItem(BlockRegistry.SPICE_CITRINE_NIGELLA_SEEDS.get(), null, 1));
    public static final Supplier<Item> SPICE_ICE_MINT = ITEMS.register("spice_ice_mint", () -> new SpiceBlockItem(BlockRegistry.SPICE_ICE_MINT.get(), null, 1));
    public static final Supplier<Item> SPICE_ICE_PATCHOULI = ITEMS.register("spice_ice_patchouli", () -> new SpiceBlockItem(BlockRegistry.SPICE_ICE_PATCHOULI.get(), null, 1));
    public static final Supplier<Item> SPICE_STORM_SHARDS = ITEMS.register("spice_storm_shards", () -> new SpiceBlockItem(BlockRegistry.SPICE_STORM_SHARDS.get(), null, 1));
    public static final Supplier<Item> SPICE_ROASTED_RED_PINE_NUTS = ITEMS.register("spice_roasted_red_pine_nuts", () -> new SpiceBlockItem(BlockRegistry.SPICE_ROASTED_RED_PINE_NUTS.get(), null, 2));
    public static final Supplier<Item> SPICE_GLACE_GOJI_BERRIES = ITEMS.register("spice_glace_goji_berries", () -> new SpiceBlockItem(BlockRegistry.SPICE_GLACE_GOJI_BERRIES.get(), null, 1));
    public static final Supplier<Item> SPICE_FROZEN_PERSIMMON = ITEMS.register("spice_frozen_persimmon", () -> new SpiceBlockItem(BlockRegistry.SPICE_FROZEN_PERSIMMON.get(), null, 1));
    public static final Supplier<Item> SPICE_ROASTED_PECANS = ITEMS.register("spice_roasted_pecans", () -> new SpiceBlockItem(BlockRegistry.SPICE_ROASTED_PECANS.get(), null, 2));
    public static final Supplier<Item> SPICE_SILVER_NEEDLE_WHITE_TEA = ITEMS.register("spice_silver_needle_white_tea", () -> new SpiceBlockItem(BlockRegistry.SPICE_SILVER_NEEDLE_WHITE_TEA.get(), null, 2));
    public static final Supplier<Item> SPICE_GOLDEN_CINNAMON_POWDER = ITEMS.register("spice_golden_cinnamon_powder", () -> new SpiceBlockItem(BlockRegistry.SPICE_GOLDEN_CINNAMON_POWDER.get(), null, 2));
    public static final Supplier<Item> SPICE_DRIED_SELAGINELLA = ITEMS.register("spice_dried_selaginella", () -> new SpiceBlockItem(BlockRegistry.SPICE_DRIED_SELAGINELLA.get(), null, 2));

    private static Item.Properties itemProperties(Block block) {
        Identifier blockId = BuiltInRegistries.BLOCK.getKey(block);
        if (blockId == null) {
            throw new IllegalStateException("Block must be registered before its item is constructed");
        }
        return new Item.Properties().setId(ResourceKey.create(Registries.ITEM, blockId));
    }

    public static void init() {
    }
}
