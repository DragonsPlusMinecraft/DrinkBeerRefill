package lekavar.lma.drinkbeer.registries;

import lekavar.lma.drinkbeer.DrinkBeer;
import lekavar.lma.drinkbeer.recipes.BrewingRecipe;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraftforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class RecipeRegistry {
    public static final DeferredRegister<RecipeType<?>> RECIPE_TYPES = DeferredRegister.create(Registries.RECIPE_TYPE, DrinkBeer.MOD_ID);
    public static final Supplier<RecipeType<BrewingRecipe>> RECIPE_TYPE_BREWING = RECIPE_TYPES.register("brewing", () -> RecipeType.simple(new ResourceLocation(DrinkBeer.MOD_ID,"brewing")));
    public static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZERS = DeferredRegister.create(Registries.RECIPE_SERIALIZER, DrinkBeer.MOD_ID);
    public static final Supplier<RecipeSerializer<BrewingRecipe>> RECIPE_SERIALIZER_BREWING = RECIPE_SERIALIZERS.register("brewing", BrewingRecipe.Serializer::new);
}
