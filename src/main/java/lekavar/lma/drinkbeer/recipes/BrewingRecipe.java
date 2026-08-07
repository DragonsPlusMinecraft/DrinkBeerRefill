package lekavar.lma.drinkbeer.recipes;

import com.google.common.collect.Lists;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import lekavar.lma.drinkbeer.registries.RecipeRegistry;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.crafting.CraftingHelper;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.List;

public class BrewingRecipe implements Recipe<IBrewingInventory> {
    public static final int INPUT_SIZE = 4;

    private final ResourceLocation id;
    private final NonNullList<Ingredient> input;
    private final ItemStack cup;
    private final int brewingTime;
    private final ItemStack result;

    public BrewingRecipe(ResourceLocation id, NonNullList<Ingredient> input, ItemStack cup, int brewingTime, ItemStack result) {
        this.id = id;
        this.input = input;
        this.cup = cup;
        this.brewingTime = brewingTime;
        this.result = result;
    }

    @Deprecated
    public NonNullList<Ingredient> getIngredient() {
        return getIngredients();
    }

    @Override
    public NonNullList<Ingredient> getIngredients() {
        NonNullList<Ingredient> ingredients = NonNullList.create();
        ingredients.addAll(input);
        return ingredients;
    }

    @Deprecated
    public ItemStack geBeerCup() {
        return getBeerCup();
    }

    public ItemStack getBeerCup() {
        return cup.copy();
    }

    @Override
    public boolean matches(IBrewingInventory inventory, Level level) {
        List<Ingredient> remaining = Lists.newArrayList(input);
        List<ItemStack> supplied = inventory.getIngredients();
        if (supplied.size() != input.size()) {
            return false;
        }
        for (ItemStack stack : supplied) {
            int matched = getLatestMatched(remaining, stack);
            if (matched < 0) {
                return false;
            }
            remaining.remove(matched);
        }
        return remaining.isEmpty();
    }

    @Override
    public ItemStack assemble(@NotNull IBrewingInventory inventory, @NotNull RegistryAccess registryAccess) {
        return result.copy();
    }

    private int getLatestMatched(List<Ingredient> candidates, ItemStack stack) {
        for (int index = 0; index < candidates.size(); index++) {
            if (candidates.get(index).test(stack)) {
                return index;
            }
        }
        return -1;
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return true;
    }

    @Override
    public ItemStack getResultItem(@NotNull RegistryAccess registryAccess) {
        return result.copy();
    }

    public ItemStack getResultItemNoRegistryAccess() {
        return result.copy();
    }

    @Override
    public boolean isSpecial() {
        return true;
    }

    @Override
    public ResourceLocation getId() {
        return id;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return RecipeRegistry.RECIPE_SERIALIZER_BREWING.get();
    }

    @Override
    public RecipeType<?> getType() {
        return RecipeRegistry.RECIPE_TYPE_BREWING.get();
    }

    public int getRequiredCupCount() {
        return cup.getCount();
    }

    public boolean isCupQualified(IBrewingInventory inventory) {
        ItemStack suppliedCup = inventory.getCup();
        return ItemStack.isSameItemSameTags(suppliedCup, cup) && suppliedCup.getCount() >= cup.getCount();
    }

    public int getBrewingTime() {
        return brewingTime;
    }

    public static class Serializer implements RecipeSerializer<BrewingRecipe> {
        @Override
        public BrewingRecipe fromJson(ResourceLocation id, JsonObject json) {
            JsonArray ingredientJson = GsonHelper.getAsJsonArray(json, "ingredients");
            if (ingredientJson.size() != INPUT_SIZE) {
                throw new JsonParseException("DrinkBeer brewing recipes require exactly " + INPUT_SIZE + " ingredients");
            }

            NonNullList<Ingredient> ingredients = NonNullList.create();
            for (int index = 0; index < ingredientJson.size(); index++) {
                Ingredient ingredient = Ingredient.fromJson(ingredientJson.get(index));
                if (ingredient.isEmpty()) {
                    throw new JsonParseException("DrinkBeer brewing ingredients may not be empty");
                }
                ingredients.add(ingredient);
            }

            ItemStack cup = CraftingHelper.getItemStack(GsonHelper.getAsJsonObject(json, "cup"), true);
            ItemStack result = CraftingHelper.getItemStack(GsonHelper.getAsJsonObject(json, "result"), true);
            int brewingTime = GsonHelper.getAsInt(json, "brewing_time");
            if (cup.isEmpty() || result.isEmpty() || brewingTime < 1) {
                throw new JsonParseException("DrinkBeer brewing cup/result must be non-empty and brewing_time must be positive");
            }
            return new BrewingRecipe(id, ingredients, cup, brewingTime, result);
        }

        @Nullable
        @Override
        public BrewingRecipe fromNetwork(ResourceLocation id, FriendlyByteBuf buffer) {
            int ingredientCount = buffer.readVarInt();
            if (ingredientCount != INPUT_SIZE) {
                throw new IllegalArgumentException("Invalid DrinkBeer brewing ingredient count: " + ingredientCount);
            }
            NonNullList<Ingredient> ingredients = NonNullList.withSize(ingredientCount, Ingredient.EMPTY);
            for (int index = 0; index < ingredientCount; index++) {
                ingredients.set(index, Ingredient.fromNetwork(buffer));
            }
            ItemStack cup = buffer.readItem();
            int brewingTime = buffer.readVarInt();
            ItemStack result = buffer.readItem();
            if (cup.isEmpty() || result.isEmpty() || brewingTime < 1) {
                throw new IllegalArgumentException("Invalid DrinkBeer brewing recipe payload");
            }
            return new BrewingRecipe(id, ingredients, cup, brewingTime, result);
        }

        @Override
        public void toNetwork(FriendlyByteBuf buffer, BrewingRecipe recipe) {
            buffer.writeVarInt(recipe.input.size());
            for (Ingredient ingredient : recipe.input) {
                ingredient.toNetwork(buffer);
            }
            buffer.writeItem(recipe.cup);
            buffer.writeVarInt(recipe.brewingTime);
            buffer.writeItem(recipe.result);
        }
    }
}
