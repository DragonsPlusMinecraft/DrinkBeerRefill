package lekavar.lma.drinkbeer.recipes;

import lekavar.lma.drinkbeer.RegistryComponentTest;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BrewingRecipeTest extends RegistryComponentTest {
    private static BrewingRecipe recipe() {
        NonNullList<Ingredient> ingredients = NonNullList.create();
        ingredients.add(Ingredient.of(Items.WHEAT));
        ingredients.add(Ingredient.of(Items.WHEAT));
        ingredients.add(Ingredient.of(Items.APPLE));
        ingredients.add(Ingredient.of(Items.WATER_BUCKET));
        return new BrewingRecipe(
                ingredients,
                new ItemStack(Items.GLASS_BOTTLE, 4),
                20,
                new ItemStack(Items.POTION, 4)
        );
    }

    @Test
    void matchesIngredientsInAnySlotOrder() {
        IBrewingInventory inventory = inventory(
                List.of(
                        new ItemStack(Items.WATER_BUCKET),
                        new ItemStack(Items.WHEAT, 32),
                        new ItemStack(Items.APPLE),
                        new ItemStack(Items.WHEAT, 16)
                ),
                new ItemStack(Items.GLASS_BOTTLE, 4)
        );

        assertTrue(recipe().matches(inventory, null));
    }

    @Test
    void rejectsMissingOrUnexpectedIngredients() {
        assertFalse(recipe().matches(inventory(
                List.of(new ItemStack(Items.WHEAT), new ItemStack(Items.APPLE), new ItemStack(Items.WATER_BUCKET)),
                new ItemStack(Items.GLASS_BOTTLE, 4)
        ), null));
        assertFalse(recipe().matches(inventory(
                List.of(
                        new ItemStack(Items.WHEAT),
                        new ItemStack(Items.WHEAT),
                        new ItemStack(Items.CARROT),
                        new ItemStack(Items.WATER_BUCKET)
                ),
                new ItemStack(Items.GLASS_BOTTLE, 4)
        ), null));
    }

    @Test
    void requiresTheConfiguredCupAndCount() {
        assertFalse(recipe().isCupQualified(inventory(List.of(), new ItemStack(Items.GLASS_BOTTLE, 3))));
        assertFalse(recipe().isCupQualified(inventory(List.of(), new ItemStack(Items.BOWL, 4))));
        assertTrue(recipe().isCupQualified(inventory(List.of(), new ItemStack(Items.GLASS_BOTTLE, 4))));
    }

    private static IBrewingInventory inventory(List<ItemStack> ingredients, ItemStack cup) {
        return new IBrewingInventory() {
            @Override
            public List<ItemStack> getIngredients() {
                return ingredients.stream().map(ItemStack::copy).toList();
            }

            @Override
            public ItemStack getCup() {
                return cup.copy();
            }

            @Override
            public ItemStack getItem(int index) {
                if (index >= 0 && index < ingredients.size()) {
                    return ingredients.get(index);
                }
                return index == 4 ? cup : ItemStack.EMPTY;
            }

            @Override
            public int size() {
                return 5;
            }
        };
    }
}
