package lekavar.lma.drinkbeer.registries;

import lekavar.lma.drinkbeer.blocks.*;
import lekavar.lma.drinkbeer.platform.Registration;
import lekavar.lma.drinkbeer.platform.RegistryProvider;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;

import java.util.function.Supplier;

public class BlockRegistry {
    private static final RegistryProvider<Block> BLOCKS = Registration.provider(BuiltInRegistries.BLOCK);

    // General
    public static final Supplier<Block> BEER_BARREL = BLOCKS.registerWithKey("beer_barrel", key -> new BeerBarrelBlock(woodMachine(key)));
    public static final Supplier<Block> BARTENDING_TABLE = BLOCKS.registerWithKey("bartending_table_normal", key -> new BartendingTableBlock(woodMachine(key)));
    public static final Supplier<Block> TRADE_BOX = BLOCKS.registerWithKey("trade_box_normal", key -> new TradeboxBlock(woodMachine(key)));
    public static final Supplier<Block> EMPTY_BEER_MUG = BLOCKS.registerWithKey("empty_beer_mug", key -> new BeerMugBlock(beerMug(key)));
    public static final Supplier<Block> IRON_CALL_BELL = BLOCKS.registerWithKey("iron_call_bell", key -> new CallBellBlock(callBell(key)));
    public static final Supplier<Block> GOLDEN_CALL_BELL = BLOCKS.registerWithKey("golden_call_bell", key -> new CallBellBlock(callBell(key)));
    public static final Supplier<Block> LEKAS_CALL_BELL = BLOCKS.registerWithKey("lekas_call_bell", key -> new CallBellBlock(callBell(key)));

    // Authorized upstream 4.0 holiday content
    public static final Supplier<Block> GIFT_RED = BLOCKS.registerWithKey("gift_red", key -> new GiftBlock(gift(key)));
    public static final Supplier<Block> GIFT_BLUE = BLOCKS.registerWithKey("gift_blue", key -> new GiftBlock(gift(key)));
    public static final Supplier<Block> GIFT_GREEN = BLOCKS.registerWithKey("gift_green", key -> new GiftBlock(gift(key)));
    public static final Supplier<Block> GIFT_WHITE = BLOCKS.registerWithKey("gift_white", key -> new GiftBlock(gift(key)));
    public static final Supplier<Block> COLORED_LIGHTS = BLOCKS.registerWithKey("colored_lights", key -> new ColoredLightsBlock(coloredLights(key), false));
    public static final Supplier<Block> SIDE_COLORED_LIGHTS = BLOCKS.registerWithKey("side_colored_lights", key -> new ColoredLightsBlock(coloredLights(key), true));
    public static final Supplier<Block> STAR_OF_BETHLEHEM = BLOCKS.registerWithKey("star_of_bethlehem", key -> new StarOfBethlehemBlock(star(key), false));
    public static final Supplier<Block> THE_GREAT_STAR_OF_BETHLEHEM = BLOCKS.registerWithKey("the_great_star_of_bethlehem", key -> new StarOfBethlehemBlock(star(key), true));
    public static final Supplier<Block> HORSE_MODEL_1 = BLOCKS.registerWithKey("horse_model_1", key -> new HorseModelBlock(horseModel(key), false));
    public static final Supplier<Block> HORSE_MODEL_2 = BLOCKS.registerWithKey("horse_model_2", key -> new HorseModelBlock(horseModel(key), false));
    public static final Supplier<Block> HORSE_MODEL_3 = BLOCKS.registerWithKey("horse_model_3", key -> new HorseModelBlock(horseModel(key), true));

    public static final Supplier<Block> RECIPE_BOARD_BEER_MUG = recipeBoard("recipe_board_beer_mug");
    public static final Supplier<Block> RECIPE_BOARD_BEER_MUG_BLAZE_STOUT = recipeBoard("recipe_board_beer_mug_blaze_stout");
    public static final Supplier<Block> RECIPE_BOARD_BEER_MUG_BLAZE_MILK_STOUT = recipeBoard("recipe_board_beer_mug_blaze_milk_stout");
    public static final Supplier<Block> RECIPE_BOARD_BEER_MUG_APPLE_LAMBIC = recipeBoard("recipe_board_beer_mug_apple_lambic");
    public static final Supplier<Block> RECIPE_BOARD_BEER_MUG_SWEET_BERRY_KRIEK = recipeBoard("recipe_board_beer_mug_sweet_berry_kriek");
    public static final Supplier<Block> RECIPE_BOARD_BEER_MUG_HAARS_ICEY_PALE_LAGER = recipeBoard("recipe_board_beer_mug_haars_icey_pale_lager");
    public static final Supplier<Block> RECIPE_BOARD_BEER_MUG_PUMPKIN_KVASS = recipeBoard("recipe_board_beer_mug_pumpkin_kvass");
    public static final Supplier<Block> RECIPE_BOARD_BEER_MUG_NIGHT_HOWL_KVASS = recipeBoard("recipe_board_beer_mug_night_howl_kvass");
    public static final Supplier<Block> RECIPE_BOARD_BEER_MUG_FROTHY_PINK_EGGNOG = recipeBoard("recipe_board_beer_mug_frothy_pink_eggnog");
    public static final Supplier<Block> RECIPE_BOARD_PACKAGE = BLOCKS.registerWithKey("recipe_board_package", key -> new RecipeBoardPackageBlock(recipePackage(key)));

    // Beer
    public static final Supplier<Block> BEER_MUG = beerMugBlock("beer_mug");
    public static final Supplier<Block> BEER_MUG_BLAZE_STOUT = beerMugBlock("beer_mug_blaze_stout");
    public static final Supplier<Block> BEER_MUG_BLAZE_MILK_STOUT = beerMugBlock("beer_mug_blaze_milk_stout");
    public static final Supplier<Block> BEER_MUG_APPLE_LAMBIC = beerMugBlock("beer_mug_apple_lambic");
    public static final Supplier<Block> BEER_MUG_SWEET_BERRY_KRIEK = beerMugBlock("beer_mug_sweet_berry_kriek");
    public static final Supplier<Block> BEER_MUG_HAARS_ICEY_PALE_LAGER = beerMugBlock("beer_mug_haars_icey_pale_lager");
    public static final Supplier<Block> BEER_MUG_PUMPKIN_KVASS = beerMugBlock("beer_mug_pumpkin_kvass");
    public static final Supplier<Block> BEER_MUG_NIGHT_HOWL_KVASS = beerMugBlock("beer_mug_night_howl_kvass");
    public static final Supplier<Block> BEER_MUG_FROTHY_PINK_EGGNOG = beerMugBlock("beer_mug_frothy_pink_eggnog");
    public static final Supplier<Block> MIXED_BEER = BLOCKS.registerWithKey("mixed_beer", key -> new MixedBeerBlock(beerMug(key)));

    // Spices
    public static final Supplier<Block> SPICE_BLAZE_PAPRIKA = spice("spice_blaze_paprika");
    public static final Supplier<Block> SPICE_DRIED_EGLIA_BUD = spice("spice_dried_eglia_bud");
    public static final Supplier<Block> SPICE_SMOKED_EGLIA_BUD = spice("spice_smoked_eglia_bud");
    public static final Supplier<Block> SPICE_AMETHYST_NIGELLA_SEEDS = spice("spice_amethyst_nigella_seeds");
    public static final Supplier<Block> SPICE_CITRINE_NIGELLA_SEEDS = spice("spice_citrine_nigella_seeds");
    public static final Supplier<Block> SPICE_ICE_MINT = spice("spice_ice_mint");
    public static final Supplier<Block> SPICE_ICE_PATCHOULI = spice("spice_ice_patchouli");
    public static final Supplier<Block> SPICE_STORM_SHARDS = spice("spice_storm_shards");
    public static final Supplier<Block> SPICE_ROASTED_RED_PINE_NUTS = spice("spice_roasted_red_pine_nuts");
    public static final Supplier<Block> SPICE_GLACE_GOJI_BERRIES = spice("spice_glace_goji_berries");
    public static final Supplier<Block> SPICE_FROZEN_PERSIMMON = spice("spice_frozen_persimmon", SpiceBlock.SPICE_FROZEN_PERSIMMON_SHAPE);
    public static final Supplier<Block> SPICE_ROASTED_PECANS = spice("spice_roasted_pecans");
    public static final Supplier<Block> SPICE_SILVER_NEEDLE_WHITE_TEA = spice("spice_silver_needle_white_tea");
    public static final Supplier<Block> SPICE_GOLDEN_CINNAMON_POWDER = spice("spice_golden_cinnamon_powder");
    public static final Supplier<Block> SPICE_DRIED_SELAGINELLA = spice("spice_dried_selaginella", SpiceBlock.SPICE_DRIED_SELAGINELLA);

    private static Supplier<Block> recipeBoard(String path) {
        return BLOCKS.registerWithKey(path, key -> new RecipeBoardBlock(recipeBoard(key), true));
    }

    private static Supplier<Block> beerMugBlock(String path) {
        return BLOCKS.registerWithKey(path, key -> new BeerMugBlock(beerMug(key)));
    }

    private static Supplier<Block> spice(String path) {
        return spice(path, SpiceBlock.DEFAULT_SHAPE);
    }

    private static Supplier<Block> spice(String path, net.minecraft.world.phys.shapes.VoxelShape shape) {
        return BLOCKS.registerWithKey(path, key -> new SpiceBlock(spice(key), shape));
    }

    private static BlockBehaviour.Properties base(ResourceKey<Block> key) {
        return BlockBehaviour.Properties.of().setId(key);
    }

    private static BlockBehaviour.Properties woodMachine(ResourceKey<Block> key) {
        return base(key).ignitedByLava().mapColor(MapColor.WOOD).strength(2.0F).noOcclusion();
    }

    private static BlockBehaviour.Properties beerMug(ResourceKey<Block> key) {
        return base(key).ignitedByLava().mapColor(MapColor.WOOD).strength(1.0F).noOcclusion().pushReaction(PushReaction.DESTROY);
    }

    private static BlockBehaviour.Properties callBell(ResourceKey<Block> key) {
        return base(key).mapColor(MapColor.METAL).strength(1.0F).pushReaction(PushReaction.DESTROY);
    }

    private static BlockBehaviour.Properties gift(ResourceKey<Block> key) {
        return base(key).mapColor(MapColor.WOOD).strength(3.0F).sound(SoundType.WOOD).noOcclusion();
    }

    private static BlockBehaviour.Properties coloredLights(ResourceKey<Block> key) {
        return base(key).mapColor(MapColor.WOOD).strength(1.0F).sound(SoundType.WOOD)
                .lightLevel(state -> 15).noCollision().noOcclusion().pushReaction(PushReaction.DESTROY);
    }

    private static BlockBehaviour.Properties star(ResourceKey<Block> key) {
        return base(key).mapColor(MapColor.METAL).strength(3.0F).sound(SoundType.METAL)
                .lightLevel(state -> 15).noOcclusion();
    }

    private static BlockBehaviour.Properties horseModel(ResourceKey<Block> key) {
        return base(key).mapColor(MapColor.WOOD).strength(1.0F).sound(SoundType.WOOD)
                .noOcclusion().pushReaction(PushReaction.DESTROY);
    }

    private static BlockBehaviour.Properties recipeBoard(ResourceKey<Block> key) {
        return base(key).ignitedByLava().mapColor(MapColor.WOOD).strength(1.0F).noOcclusion();
    }

    private static BlockBehaviour.Properties recipePackage(ResourceKey<Block> key) {
        return base(key).mapColor(MapColor.METAL).strength(1.0F).noOcclusion();
    }

    private static BlockBehaviour.Properties spice(ResourceKey<Block> key) {
        return base(key).ignitedByLava().mapColor(MapColor.WOOD).strength(1.0F).pushReaction(PushReaction.DESTROY);
    }

    public static void init() {
    }
}
