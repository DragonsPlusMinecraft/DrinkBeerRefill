package lekavar.lma.drinkbeer.recipes;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeInput;

import javax.annotation.Nonnull;
import java.util.List;

public interface IBrewingInventory extends RecipeInput {
    /**
     * Must return copy of itemstack in Ingredient Slots
     */
    @Nonnull
    List<ItemStack> getIngredients();

    /**
     * Must return copy of itemstack in Cup Slots
     */
    @Nonnull
    ItemStack getCup();
}
