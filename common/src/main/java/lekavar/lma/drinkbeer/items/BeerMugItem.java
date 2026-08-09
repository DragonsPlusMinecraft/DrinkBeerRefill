package lekavar.lma.drinkbeer.items;

import lekavar.lma.drinkbeer.DrinkBeerConfig;
import lekavar.lma.drinkbeer.effects.DrunkStatusEffect;
import lekavar.lma.drinkbeer.utils.beer.BeerConsumptionEffects;
import lekavar.lma.drinkbeer.utils.beer.BeerDefinition;
import lekavar.lma.drinkbeer.utils.beer.BeerSpecialAction;
import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class BeerMugItem extends BeerBlockItem {
    private final static double MAX_PLACE_DISTANCE = 2.0D;
    /**
     * @deprecated Use {@link DrinkBeerConfig#beerSaturationModifier()} so the server configuration is respected.
     */
    @Deprecated(forRemoval = false)
    public static final float SATURATION_MODIFIER = (float) DrinkBeerConfig.DEFAULT_BEER_SATURATION_MODIFIER;
    private final BeerDefinition definition;
    private final boolean hasExtraTooltip;

    public BeerMugItem(Block block, BeerDefinition definition) {
        super(block, propertiesFor(block).stacksTo(16)
                .food(definition.foodProperties((float) DrinkBeerConfig.DEFAULT_BEER_SATURATION_MODIFIER), definition.consumable()));
        this.definition = definition;
        this.hasExtraTooltip = definition.hasEffectTooltip();
    }

    /**
     * @deprecated Registered beers use centralized {@link BeerDefinition} instances.
     */
    @Deprecated(forRemoval = false)
    public BeerMugItem(Block block, int nutrition, boolean hasExtraTooltip) {
        this(block, new BeerDefinition(1, nutrition, null, hasExtraTooltip, BeerSpecialAction.NONE));
    }

    /**
     * @deprecated Registered beers use centralized {@link BeerDefinition} instances.
     */
    @Deprecated(forRemoval = false)
    public BeerMugItem(Block block, @Nullable MobEffectInstance statusEffectInstance, int nutrition, boolean hasExtraTooltip) {
        this(block, new BeerDefinition(1, nutrition,
                statusEffectInstance == null ? null : () -> statusEffectInstance,
                hasExtraTooltip, BeerSpecialAction.NONE));
    }

    /**
     * @deprecated Registered beers use centralized {@link BeerDefinition} instances.
     */
    @Deprecated(forRemoval = false)
    public BeerMugItem(Block block, Supplier<MobEffectInstance> statusEffectInstance, int nutrition, boolean hasExtraTooltip) {
        this(block, new BeerDefinition(1, nutrition, statusEffectInstance,
                hasExtraTooltip, BeerSpecialAction.NONE));
    }

    public FoodProperties getConfiguredFoodProperties() {
        return definition.foodProperties(DrinkBeerConfig.beerSaturationModifier());
    }

    public BeerDefinition getDefinition() {
        return definition;
    }

    @Override
    protected boolean canPlace(BlockPlaceContext context, BlockState state) {
        if ((context.getClickLocation().distanceTo(context.getPlayer().position()) > MAX_PLACE_DISTANCE))
            return false;
        else {
            return super.canPlace(context, state);
        }
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay tooltipDisplay,
                                Consumer<Component> tooltipAdder, TooltipFlag tooltipFlag) {
        String name = BuiltInRegistries.ITEM.getKey(stack.getItem()).getPath();
        if (hasEffectNoticeTooltip()) {
            tooltipAdder.accept(Component.translatable("item.drinkbeer." + name + ".tooltip").setStyle(Style.EMPTY.applyFormat(ChatFormatting.BLUE)));
        }
        FoodProperties foodProperties = getConfiguredFoodProperties();
        String hunger = String.valueOf(foodProperties.nutrition());
        tooltipAdder.accept(Component.translatable("drinkbeer.restores_hunger").setStyle(Style.EMPTY.applyFormat(ChatFormatting.BLUE)).append(hunger));
        tooltipAdder.accept(Component.translatable("drinkbeer.restores_saturation").setStyle(Style.EMPTY.applyFormat(ChatFormatting.BLUE))
                .append(String.format(java.util.Locale.ROOT, "%.1f", foodProperties.saturation())));
    }

    private boolean hasEffectNoticeTooltip() {
        return this.hasExtraTooltip;
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level world, LivingEntity user) {
        // FOOD is a data component in 1.21.11. Refresh it at use time so the server config remains authoritative.
        stack.set(net.minecraft.core.component.DataComponents.FOOD, getConfiguredFoodProperties());
        ItemStack result = super.finishUsingItem(stack, world, user);
        if (!world.isClientSide()) {
            // Give Drunk status effect.
            DrunkStatusEffect.addStatusEffect(user);
            BeerConsumptionEffects.finishStandardDrink(definition, world, user);
            // Inventory mutations belong on the logical server to avoid client-side ghost mugs.
            giveEmptyMugBack(user);
        }

        return result;
    }
}
