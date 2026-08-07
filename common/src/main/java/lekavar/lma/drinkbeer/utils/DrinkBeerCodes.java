package lekavar.lma.drinkbeer.utils;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.crafting.Ingredient;

public class DrinkBeerCodes {
    public final static Codec<NonNullList<Ingredient>> NON_NULL_LIST_INGREDIENT_CODEC = Ingredient.CODEC_NONEMPTY.listOf().comapFlatMap((list) -> {
        Ingredient[] allingredient = list.toArray(Ingredient[]::new);
        return DataResult.success(NonNullList.of(Ingredient.EMPTY, allingredient));
    }, nonNullList -> nonNullList);

    public final static Codec<NonNullList<Ingredient>> NON_NULL_LIST_4_INGREDIENT_CODEC = Ingredient.CODEC_NONEMPTY.listOf().comapFlatMap((list) -> {
        Ingredient[] allingredient = list.toArray(Ingredient[]::new);
        if(allingredient.length!=4) return DataResult.error(()->"Must be 4 ingredients", NonNullList.of(Ingredient.EMPTY, allingredient));
        return DataResult.success(NonNullList.of(Ingredient.EMPTY, allingredient));
    }, nonNullList -> nonNullList);
}
