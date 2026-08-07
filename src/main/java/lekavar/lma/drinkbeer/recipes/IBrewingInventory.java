package lekavar.lma.drinkbeer.recipes;

import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nonnull;
import java.util.List;

public interface IBrewingInventory extends Container {
    @Nonnull
    List<ItemStack> getIngredients();

    @Nonnull
    ItemStack getCup();
}
