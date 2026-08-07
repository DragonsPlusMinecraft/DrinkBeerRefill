package lekavar.lma.drinkbeer.recipes;

import com.google.common.collect.Lists;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lekavar.lma.drinkbeer.registries.RecipeRegistry;
import lekavar.lma.drinkbeer.utils.DrinkBeerCodes;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;

import java.util.List;

public class BrewingRecipe implements Recipe<IBrewingInventory> {
    private final NonNullList<Ingredient> input;
    private final ItemStack cup;
    private final int brewingTime;
    private final ItemStack result;

    public BrewingRecipe(NonNullList<Ingredient> input, ItemStack cup, int brewingTime, ItemStack result) {
        this.input = input;
        this.cup = cup;
        this.brewingTime = brewingTime;
        this.result = result;
    }

    @Deprecated
    public NonNullList<Ingredient> getIngredient() {
        NonNullList<Ingredient> result = NonNullList.create();
        result.addAll(input);
        return result;
    }

    @Override
    public NonNullList<Ingredient> getIngredients() {
        NonNullList<Ingredient> result = NonNullList.create();
        result.addAll(input);
        return result;
    }

    @Deprecated
    public ItemStack geBeerCup() {
        return cup.copy();
    }

    public ItemStack getBeerCup() {
        return cup.copy();
    }

    @Override
    public boolean matches(IBrewingInventory pContainer, Level pLevel) {
        List<Ingredient> testTarget = Lists.newArrayList(input);
        List<ItemStack> tested = pContainer.getIngredients();
        if (tested.size() != input.size()) return false;
        for (ItemStack itemStack : tested) {
            int i = getLatestMatched(testTarget, itemStack);
            if (i == -1) return false;
            else testTarget.remove(i);
        }
        return testTarget.isEmpty();
    }

    @Override
    public ItemStack assemble(IBrewingInventory iBrewingInventory, HolderLookup.Provider provider) {
        return result.copy();
    }


    private int getLatestMatched(List<Ingredient> testTarget, ItemStack tested) {
        for (int i = 0; i < testTarget.size(); i++) {
            if (testTarget.get(i).test(tested)) return i;
        }
        return -1;
    }

    // Can Craft at any dimension
    @Override
    public boolean canCraftInDimensions(int p_194133_1_, int p_194133_2_) {
        return true;
    }

    /**
     * Get the result of this recipe, usually for display purposes (e.g. recipe book).
     * If your recipe has more than one possible result (e.g. it's dynamic and depends on its inputs),
     * then return an empty stack.
     */
    @Override
    public ItemStack getResultItem(HolderLookup.Provider provider) {
        //For Safety, I use #copy
        return result.copy();
    }


    // For JEI Addon.
    // See JEIBrewingRecipe#setRecipe
    public ItemStack getResultItemNoRegistryAccess() {
        //For Safety, I use #copy
        return result.copy();
    }

    @Override
    public boolean isSpecial() {
        return true;
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
        return ItemStack.isSameItemSameComponents(suppliedCup, cup) && suppliedCup.getCount() >= cup.getCount();
    }

    public int getBrewingTime() {
        return brewingTime;
    }

    public static class Serializer implements RecipeSerializer<BrewingRecipe> {
        public static final MapCodec<BrewingRecipe> CODEC = RecordCodecBuilder.mapCodec(ins-> ins.group(
                DrinkBeerCodes.NON_NULL_LIST_4_INGREDIENT_CODEC.fieldOf("ingredients").forGetter(BrewingRecipe::getIngredients),
                ItemStack.CODEC.fieldOf("cup").forGetter(BrewingRecipe::getBeerCup),
                Codec.intRange(1, Integer.MAX_VALUE).fieldOf("brewing_time").forGetter(BrewingRecipe::getBrewingTime),
                ItemStack.CODEC.fieldOf("result").forGetter(BrewingRecipe::getResultItemNoRegistryAccess)
                ).apply(ins,BrewingRecipe::new));

        public static final StreamCodec<RegistryFriendlyByteBuf,BrewingRecipe> STREAM_CODEC = StreamCodec.of(Serializer::toNetwork,Serializer::fromNetwork);


        public static BrewingRecipe fromNetwork(RegistryFriendlyByteBuf packetBuffer) {
            int i = packetBuffer.readVarInt();
            NonNullList<Ingredient> ingredients = NonNullList.withSize(i, Ingredient.EMPTY);
            ingredients.replaceAll((_it) -> Ingredient.CONTENTS_STREAM_CODEC.decode(packetBuffer));
            ItemStack cup = ItemStack.STREAM_CODEC.decode(packetBuffer);
            int brewingTime = packetBuffer.readVarInt();
            ItemStack result = ItemStack.STREAM_CODEC.decode(packetBuffer);
            return new BrewingRecipe(ingredients, cup, brewingTime, result);
        }

        public static void toNetwork(RegistryFriendlyByteBuf packetBuffer, BrewingRecipe brewingRecipe) {
            packetBuffer.writeVarInt(brewingRecipe.input.size());
            for (Ingredient ingredient : brewingRecipe.input) {
                Ingredient.CONTENTS_STREAM_CODEC.encode(packetBuffer, ingredient);
            }
            ItemStack.STREAM_CODEC.encode(packetBuffer, brewingRecipe.cup);
            packetBuffer.writeVarInt(brewingRecipe.brewingTime);
            ItemStack.STREAM_CODEC.encode(packetBuffer, brewingRecipe.result);

        }

        @Override
        public MapCodec<BrewingRecipe> codec() {
            return CODEC;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, BrewingRecipe> streamCodec() {
            return STREAM_CODEC;
        }
    }
}
