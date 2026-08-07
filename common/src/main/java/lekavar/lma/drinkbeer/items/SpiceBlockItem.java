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
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.block.Block;

import javax.annotation.Nullable;
import java.util.List;

public class SpiceBlockItem extends BlockItem {
    public SpiceBlockItem(Block block, @Nullable MobEffectInstance statusEffectInstance, int hunger) {
        super(block, new Item.Properties().stacksTo(64)
                .food(statusEffectInstance != null
                        ? new FoodProperties.Builder().nutrition(hunger).effect(statusEffectInstance, 1).alwaysEdible().build()
                        : new FoodProperties.Builder().nutrition(hunger).alwaysEdible().build())
        );
    }


    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        //Spice title
        tooltipComponents.add(Component.translatable(SpiceAndFlavorManager.getSpiceToolTipTranslationKey()).setStyle(Style.EMPTY.applyFormat(ChatFormatting.YELLOW)));
        //Flavor title
        tooltipComponents.add(Component.translatable(SpiceAndFlavorManager.getFlavorToolTipTranslationKey()).append(":").setStyle(Style.EMPTY.applyFormat(ChatFormatting.WHITE)));
        //Flavor and tooltip
        Flavors flavors = Spices.byItem(this.asItem()).getFlavor();
        tooltipComponents.add(Component.translatable(SpiceAndFlavorManager.getFlavorTranslationKey(flavors.getId()))
                .append("(")
                .append(Component.translatable(SpiceAndFlavorManager.getFlavorToolTipTranslationKey(flavors.getId())))
                .append(")")
                .setStyle(Style.EMPTY.applyFormat(ChatFormatting.RED)));
    }
}
