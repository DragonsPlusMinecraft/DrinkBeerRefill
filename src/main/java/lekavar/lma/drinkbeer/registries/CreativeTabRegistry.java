package lekavar.lma.drinkbeer.registries;

import lekavar.lma.drinkbeer.DrinkBeer;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

import java.util.function.Supplier;

public class CreativeTabRegistry {
    public static final DeferredRegister<CreativeModeTab> TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, DrinkBeer.MOD_ID);
    public static final RegistryObject<CreativeModeTab> GENERAL = TABS.register("drinkbeer.general", () -> CreativeModeTab.builder()
            .title(Component.translatable("itemGroup.drinkbeer.general"))
            .icon(() -> new ItemStack(BlockRegistry.BEER_BARREL.get()))
            .withTabsBefore(CreativeModeTabs.SPAWN_EGGS)
            .displayItems((parameters, output) -> {
                output.accept(ItemRegistry.BEER_BARREL.get());
                output.accept(ItemRegistry.BARTENDING_TABLE.get());
                output.accept(ItemRegistry.TRADE_BOX.get());
                output.accept(ItemRegistry.EMPTY_BEER_MUG.get());
                output.accept(ItemRegistry.IRON_CALL_BELL.get());
                output.accept(ItemRegistry.GOLDEN_CALL_BELL.get());
                output.accept(ItemRegistry.LEKAS_CALL_BELL.get());
                output.accept(ItemRegistry.GIFT_RED.get());
                output.accept(ItemRegistry.GIFT_BLUE.get());
                output.accept(ItemRegistry.GIFT_GREEN.get());
                output.accept(ItemRegistry.GIFT_WHITE.get());
                output.accept(ItemRegistry.COLORED_LIGHTS.get());
                output.accept(ItemRegistry.SIDE_COLORED_LIGHTS.get());
                output.accept(ItemRegistry.STAR_OF_BETHLEHEM.get());
                output.accept(ItemRegistry.THE_GREAT_STAR_OF_BETHLEHEM.get());
                output.accept(ItemRegistry.HORSE_MODEL_1.get());
                output.accept(ItemRegistry.HORSE_MODEL_2.get());
                output.accept(ItemRegistry.HORSE_MODEL_3.get());
                output.accept(ItemRegistry.RECIPE_BOARD_PACKAGE.get());
                output.accept(ItemRegistry.RECIPE_BOARD_BEER_MUG.get());
                output.accept(ItemRegistry.RECIPE_BOARD_BEER_MUG_BLAZE_STOUT.get());
                output.accept(ItemRegistry.RECIPE_BOARD_BEER_MUG_BLAZE_MILK_STOUT.get());
                output.accept(ItemRegistry.RECIPE_BOARD_BEER_MUG_APPLE_LAMBIC.get());
                output.accept(ItemRegistry.RECIPE_BOARD_BEER_MUG_SWEET_BERRY_KRIEK.get());
                output.accept(ItemRegistry.RECIPE_BOARD_BEER_MUG_HAARS_ICEY_PALE_LAGER.get());
                output.accept(ItemRegistry.RECIPE_BOARD_BEER_MUG_PUMPKIN_KVASS.get());
                output.accept(ItemRegistry.RECIPE_BOARD_BEER_MUG_NIGHT_HOWL_KVASS.get());
                output.accept(ItemRegistry.RECIPE_BOARD_BEER_MUG_FROTHY_PINK_EGGNOG.get());
                output.accept(ItemRegistry.SPICE_BLAZE_PAPRIKA.get());
                output.accept(ItemRegistry.SPICE_DRIED_EGLIA_BUD.get());
                output.accept(ItemRegistry.SPICE_SMOKED_EGLIA_BUD.get());
                output.accept(ItemRegistry.SPICE_AMETHYST_NIGELLA_SEEDS.get());
                output.accept(ItemRegistry.SPICE_CITRINE_NIGELLA_SEEDS.get());
                output.accept(ItemRegistry.SPICE_ICE_MINT.get());
                output.accept(ItemRegistry.SPICE_ICE_PATCHOULI.get());
                output.accept(ItemRegistry.SPICE_STORM_SHARDS.get());
                output.accept(ItemRegistry.SPICE_ROASTED_RED_PINE_NUTS.get());
                output.accept(ItemRegistry.SPICE_GLACE_GOJI_BERRIES.get());
                output.accept(ItemRegistry.SPICE_FROZEN_PERSIMMON.get());
                output.accept(ItemRegistry.SPICE_ROASTED_PECANS.get());
                output.accept(ItemRegistry.SPICE_SILVER_NEEDLE_WHITE_TEA.get());
                output.accept(ItemRegistry.SPICE_GOLDEN_CINNAMON_POWDER.get());
                output.accept(ItemRegistry.SPICE_DRIED_SELAGINELLA.get());
            }).build());

    public static final RegistryObject<CreativeModeTab> BEER = TABS.register("drinkbeer.beer", () -> CreativeModeTab.builder()
            .title(Component.translatable("itemGroup.drinkbeer.beer"))
            .icon(() -> new ItemStack(BlockRegistry.BEER_BARREL.get()))
            .withTabsBefore(GENERAL.getId())
            .displayItems((parameters, output) -> {
                output.accept(ItemRegistry.BEER_MUG.get());
                output.accept(ItemRegistry.BEER_MUG_BLAZE_STOUT.get());
                output.accept(ItemRegistry.BEER_MUG_BLAZE_MILK_STOUT.get());
                output.accept(ItemRegistry.BEER_MUG_APPLE_LAMBIC.get());
                output.accept(ItemRegistry.BEER_MUG_SWEET_BERRY_KRIEK.get());
                output.accept(ItemRegistry.BEER_MUG_HAARS_ICEY_PALE_LAGER.get());
                output.accept(ItemRegistry.BEER_MUG_PUMPKIN_KVASS.get());
                output.accept(ItemRegistry.BEER_MUG_NIGHT_HOWL_KVASS.get());
                output.accept(ItemRegistry.BEER_MUG_FROTHY_PINK_EGGNOG.get());
            }).build());
}
