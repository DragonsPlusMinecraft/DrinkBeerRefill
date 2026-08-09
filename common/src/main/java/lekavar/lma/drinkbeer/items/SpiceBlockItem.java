package lekavar.lma.drinkbeer.items;

import lekavar.lma.drinkbeer.managers.SpiceAndFlavorManager;
import lekavar.lma.drinkbeer.utils.mixedbeer.Flavors;
import lekavar.lma.drinkbeer.utils.mixedbeer.Spices;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.Consumable;
import net.minecraft.world.item.component.Consumables;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.consume_effects.ApplyStatusEffectsConsumeEffect;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.block.Block;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Consumer;

public class SpiceBlockItem extends BlockItem {
    public SpiceBlockItem(Block block, @Nullable MobEffectInstance statusEffectInstance, int hunger) {
        super(block, createProperties(block, statusEffectInstance, hunger));
    }

    private static Item.Properties createProperties(
            Block block,
            @Nullable MobEffectInstance statusEffectInstance,
            int hunger
    ) {
        FoodProperties food = new FoodProperties.Builder().nutrition(hunger).alwaysEdible().build();
        Consumable.Builder consumable = Consumables.defaultFood();
        if (statusEffectInstance != null) {
            consumable.onConsume(new ApplyStatusEffectsConsumeEffect(statusEffectInstance, 1.0F));
        }
        return itemProperties(block).stacksTo(64).food(food, consumable.build());
    }

    private static Item.Properties itemProperties(Block block) {
        Identifier blockId = BuiltInRegistries.BLOCK.getKey(block);
        if (blockId == null) {
            throw new IllegalStateException("Block must be registered before its item is constructed");
        }
        return new Item.Properties().setId(ResourceKey.create(Registries.ITEM, blockId));
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay tooltipDisplay,
                                Consumer<Component> tooltipAdder, TooltipFlag tooltipFlag) {
        //Spice title
        tooltipAdder.accept(Component.translatable(SpiceAndFlavorManager.getSpiceToolTipTranslationKey()).setStyle(Style.EMPTY.applyFormat(ChatFormatting.YELLOW)));
        //Flavor title
        tooltipAdder.accept(Component.translatable(SpiceAndFlavorManager.getFlavorToolTipTranslationKey()).append(":").setStyle(Style.EMPTY.applyFormat(ChatFormatting.WHITE)));
        //Flavor and tooltip
        Flavors flavors = Spices.byItem(this.asItem()).getFlavor();
        tooltipAdder.accept(Component.translatable(SpiceAndFlavorManager.getFlavorTranslationKey(flavors.getId()))
                .append("(")
                .append(Component.translatable(SpiceAndFlavorManager.getFlavorToolTipTranslationKey(flavors.getId())))
                .append(")")
                .setStyle(Style.EMPTY.applyFormat(ChatFormatting.RED)));
    }
}
