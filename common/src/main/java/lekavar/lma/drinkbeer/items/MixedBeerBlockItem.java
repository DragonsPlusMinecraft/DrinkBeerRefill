package lekavar.lma.drinkbeer.items;

import lekavar.lma.drinkbeer.managers.MixedBeerManager;
import lekavar.lma.drinkbeer.managers.SpiceAndFlavorManager;
import lekavar.lma.drinkbeer.registries.DataComponentTypeRegistry;
import lekavar.lma.drinkbeer.utils.beer.Beers;
import lekavar.lma.drinkbeer.utils.dataComponent.SpiceData;
import lekavar.lma.drinkbeer.utils.mixedbeer.Flavors;
import lekavar.lma.drinkbeer.utils.mixedbeer.Spices;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;
import java.util.Locale;

public class MixedBeerBlockItem extends BeerBlockItem {
    public MixedBeerBlockItem(Block block) {
        super(block, new Item.Properties().stacksTo(1)
                .food(new FoodProperties.Builder().alwaysEdible().build())
                .component(DataComponentTypeRegistry.BEER_ID_COMPONENT.get(), 1)
                .component(DataComponentTypeRegistry.SPICE_COMPONENT.get(), new SpiceData(Spices.EMPTY_SPICE_ID,Spices.EMPTY_SPICE_ID,Spices.EMPTY_SPICE_ID)));
    }

    public void appendMixedBeerTooltip(ItemStack stack, List<Component> tooltip) {
        //Base title
        tooltip.add(Component.translatable(MixedBeerManager.getBaseBeerToolTipTranslationKey()).append(":").setStyle(Style.EMPTY.applyFormat(ChatFormatting.WHITE)));
        //Base beer
        int beerId = MixedBeerManager.getBeerId(stack);
        Item beerItem = Beers.byId(beerId).getBeerItem();
        String beerName = beerId > Beers.EMPTY_BEER_ID ? "block.drinkbeer." + beerItem.toString()
                : MixedBeerManager.getUnmixedToolTipTranslationKey();
        String beerTooltip = beerId > Beers.EMPTY_BEER_ID ? "item.drinkbeer." + beerItem + ".tooltip"
                : "";

        tooltip.add(Component.translatable(beerName).setStyle(Style.EMPTY.applyFormat(ChatFormatting.BLUE)));
        //Base status effect tooltip
        if (beerId > Beers.EMPTY_BEER_ID) {
            if (Beers.byId(beerId).getHasStatusEffectTooltip()) {
                tooltip.add(Component.translatable(beerTooltip).setStyle(Style.EMPTY.applyFormat(ChatFormatting.BLUE)));
            }
        }
        //Base food level
        if (beerId > Beers.EMPTY_BEER_ID) {
            FoodProperties foodProperties = Beers.byId(beerId).getDefinition()
                    .foodProperties(lekavar.lma.drinkbeer.DrinkBeerConfig.beerSaturationModifier());
            String hunger = Integer.toString(foodProperties.nutrition());
            tooltip.add(Component.translatable("drinkbeer.restores_hunger").setStyle(Style.EMPTY.applyFormat(ChatFormatting.BLUE)).append(hunger));
            tooltip.add(Component.translatable("drinkbeer.restores_saturation").setStyle(Style.EMPTY.applyFormat(ChatFormatting.BLUE))
                    .append(String.format(Locale.ROOT, "%.1f", foodProperties.saturation())));
        }

        //Flavor title
        tooltip.add(Component.translatable(SpiceAndFlavorManager.getFlavorToolTipTranslationKey()).append(":").setStyle(Style.EMPTY.applyFormat(ChatFormatting.WHITE)));
        //Flavor
        List<Integer> spiceList = MixedBeerManager.getSpiceList(stack);
        if (!spiceList.isEmpty()) {
            for (int spiceId : spiceList) {
                Flavors flavor = Spices.byId(spiceId).getFlavor();
                tooltip.add(Component.translatable(SpiceAndFlavorManager.getFlavorTranslationKey(flavor.getId()))
                        .append("(")
                        .append(Component.translatable(SpiceAndFlavorManager.getFlavorToolTipTranslationKey(flavor.getId())))
                        .append(")")
                        .setStyle(Style.EMPTY.applyFormat(ChatFormatting.RED)));
            }
        } else {
            tooltip.add(Component.translatable(SpiceAndFlavorManager.getNoFlavorToolTipTranslationKey()).setStyle(Style.EMPTY.applyFormat(ChatFormatting.RED)));
        }
        //Flavor combination(if exists)
        Flavors combinedFlavor = SpiceAndFlavorManager.getCombinedFlavor(spiceList);
        if (combinedFlavor != null) {
            tooltip.add(Component.literal("\"")
                    .append(Component.translatable(SpiceAndFlavorManager.getFlavorTranslationKey(combinedFlavor.getId())))
                    .append("\"")
                    .setStyle(Style.EMPTY.applyFormat(ChatFormatting.DARK_RED)));
        }
    }

    @Override
    public Component getName(ItemStack stack) {
        return getMixedBeerName(stack);
    }


    public Component getMixedBeerName(ItemStack stack) {
        int beerId = MixedBeerManager.getBeerId(stack);
        Item beerItem = Beers.byId(beerId).getBeerItem();
        String beerName = beerId > Beers.EMPTY_BEER_ID ? "block.drinkbeer." + beerItem.toString() : "block.drinkbeer.empty_beer_mug";
        Component name = Component.translatable(beerName).append(Component.translatable("block.drinkbeer." + MixedBeerManager.getMixedBeerTranslationKey())).setStyle(Style.EMPTY.applyFormat(ChatFormatting.YELLOW));
        return name;
    }

    @Override
    protected boolean placeBlock(BlockPlaceContext context, BlockState state) {
        if (super.placeBlock(context, state)) {
            return true;
        }
        return false;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        appendMixedBeerTooltip(stack, tooltipComponents);
    }

    @Override
    protected boolean canPlace(BlockPlaceContext context, BlockState state) {
        if (context.getClickLocation().distanceTo(context.getPlayer().position()) > MAX_PLACE_DISTANCE)
            return false;
        else {
            return super.canPlace(context, state);
        }
    }

    public static int getBeerId(ItemStack itemStack) {
        return MixedBeerManager.getBeerId(itemStack);
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level world, LivingEntity user) {
        // Vanilla consumption may clear the live stack before our custom data is read.
        ItemStack consumedBeer = stack.copy();
        ItemStack result = super.finishUsingItem(stack, world, user);
        if (!world.isClientSide()) {
            // Apply mixed beer and mutate the inventory only on the logical server.
            MixedBeerManager.useMixedBeer(consumedBeer, world, user);
            giveEmptyMugBack(user);
        }

        return result;
    }


}
