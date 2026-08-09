package lekavar.lma.drinkbeer.utils;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.crafting.Ingredient;

public class DrinkBeerCodes {
    public final static Codec<NonNullList<Ingredient>> NON_NULL_LIST_INGREDIENT_CODEC = Ingredient.CODEC.listOf().comapFlatMap(list -> {
        NonNullList<Ingredient> ingredients = NonNullList.create();
        ingredients.addAll(list);
        return DataResult.success(ingredients);
    }, java.util.List::copyOf);

    public final static Codec<NonNullList<Ingredient>> NON_NULL_LIST_4_INGREDIENT_CODEC = Ingredient.CODEC.listOf().comapFlatMap(list -> {
        NonNullList<Ingredient> ingredients = NonNullList.create();
        ingredients.addAll(list);
        if (ingredients.size() != 4) {
            return DataResult.error(() -> "Must be 4 ingredients", ingredients);
        }
        return DataResult.success(ingredients);
    }, java.util.List::copyOf);
}
