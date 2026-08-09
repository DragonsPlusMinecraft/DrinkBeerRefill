package lekavar.lma.drinkbeer.recipes;

import com.google.common.collect.Lists;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lekavar.lma.drinkbeer.registries.RecipeRegistry;
import lekavar.lma.drinkbeer.registries.BlockRegistry;
import lekavar.lma.drinkbeer.utils.DrinkBeerCodes;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.PlacementInfo;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeBookCategories;
import net.minecraft.world.item.crafting.RecipeBookCategory;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.display.RecipeDisplay;
import net.minecraft.world.item.crafting.display.ShapelessCraftingRecipeDisplay;
import net.minecraft.world.item.crafting.display.SlotDisplay;
import net.minecraft.world.level.Level;

import java.util.List;

public class BrewingRecipe implements Recipe<IBrewingInventory> {
    private final NonNullList<Ingredient> input;
    private final ItemStackTemplate cup;
    private final int brewingTime;
    private final ItemStackTemplate result;

    public BrewingRecipe(NonNullList<Ingredient> input, ItemStack cup, int brewingTime, ItemStack result) {
        this(input, ItemStackTemplate.fromNonEmptyStack(cup), brewingTime,
                ItemStackTemplate.fromNonEmptyStack(result));
    }

    private BrewingRecipe(NonNullList<Ingredient> input, ItemStackTemplate cup,
                          int brewingTime, ItemStackTemplate result) {
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

    public NonNullList<Ingredient> getIngredients() {
        NonNullList<Ingredient> result = NonNullList.create();
        result.addAll(input);
        return result;
    }

    @Deprecated
    public ItemStack geBeerCup() {
        return cup.create();
    }

    public ItemStack getBeerCup() {
        return cup.create();
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
    public ItemStack assemble(IBrewingInventory iBrewingInventory) {
        return result.create();
    }


    private int getLatestMatched(List<Ingredient> testTarget, ItemStack tested) {
        for (int i = 0; i < testTarget.size(); i++) {
            if (testTarget.get(i).test(tested)) return i;
        }
        return -1;
    }

    // Can Craft at any dimension
    public boolean canCraftInDimensions(int p_194133_1_, int p_194133_2_) {
        return true;
    }

    /**
     * Get the result of this recipe, usually for display purposes (e.g. recipe book).
     * If your recipe has more than one possible result (e.g. it's dynamic and depends on its inputs),
     * then return an empty stack.
     */
    public ItemStack getResultItem(HolderLookup.Provider provider) {
        //For Safety, I use #copy
        return result.create();
    }


    // For JEI Addon.
    // See JEIBrewingRecipe#setRecipe
    public ItemStack getResultItemNoRegistryAccess() {
        //For Safety, I use #copy
        return result.create();
    }

    @Override
    public boolean isSpecial() {
        return true;
    }

    @Override
    public boolean showNotification() {
        return false;
    }

    @Override
    public String group() {
        return "";
    }

    @Override
    public RecipeSerializer<BrewingRecipe> getSerializer() {
        return RecipeRegistry.RECIPE_SERIALIZER_BREWING.get();
    }

    @Override
    public RecipeType<BrewingRecipe> getType() {
        return RecipeRegistry.RECIPE_TYPE_BREWING.get();
    }

    @Override
    public PlacementInfo placementInfo() {
        return PlacementInfo.NOT_PLACEABLE;
    }

    @Override
    public RecipeBookCategory recipeBookCategory() {
        return RecipeBookCategories.CRAFTING_MISC;
    }

    @Override
    public List<RecipeDisplay> display() {
        List<SlotDisplay> ingredients = new java.util.ArrayList<>(input.stream().map(Ingredient::display).toList());
        ingredients.add(new SlotDisplay.ItemStackSlotDisplay(cup));
        return List.of(new ShapelessCraftingRecipeDisplay(
                ingredients,
                new SlotDisplay.ItemStackSlotDisplay(result),
                new SlotDisplay.ItemSlotDisplay(BlockRegistry.BEER_BARREL.get().asItem())
        ));
    }

    public int getRequiredCupCount() {
        return cup.count();
    }

    public boolean isCupQualified(IBrewingInventory inventory) {
        ItemStack suppliedCup = inventory.getCup();
        ItemStack requiredCup = cup.create();
        return ItemStack.isSameItemSameComponents(suppliedCup, requiredCup)
                && suppliedCup.getCount() >= cup.count();
    }

    public int getBrewingTime() {
        return brewingTime;
    }

    private ItemStackTemplate getBeerCupTemplate() {
        return cup;
    }

    private ItemStackTemplate getResultTemplate() {
        return result;
    }

    public static final class Serializer {
        public static final MapCodec<BrewingRecipe> CODEC = RecordCodecBuilder.mapCodec(ins-> ins.group(
                DrinkBeerCodes.NON_NULL_LIST_4_INGREDIENT_CODEC.fieldOf("ingredients").forGetter(BrewingRecipe::getIngredients),
                ItemStackTemplate.CODEC.fieldOf("cup").forGetter(BrewingRecipe::getBeerCupTemplate),
                Codec.intRange(1, Integer.MAX_VALUE).fieldOf("brewing_time").forGetter(BrewingRecipe::getBrewingTime),
                ItemStackTemplate.CODEC.fieldOf("result").forGetter(BrewingRecipe::getResultTemplate)
                ).apply(ins,BrewingRecipe::new));

        public static final StreamCodec<RegistryFriendlyByteBuf,BrewingRecipe> STREAM_CODEC = StreamCodec.of(Serializer::toNetwork,Serializer::fromNetwork);


        public static BrewingRecipe fromNetwork(RegistryFriendlyByteBuf packetBuffer) {
            int i = packetBuffer.readVarInt();
            NonNullList<Ingredient> ingredients = NonNullList.createWithCapacity(i);
            for (int ingredientIndex = 0; ingredientIndex < i; ingredientIndex++) {
                ingredients.add(Ingredient.CONTENTS_STREAM_CODEC.decode(packetBuffer));
            }
            ItemStackTemplate cup = ItemStackTemplate.STREAM_CODEC.decode(packetBuffer);
            int brewingTime = packetBuffer.readVarInt();
            ItemStackTemplate result = ItemStackTemplate.STREAM_CODEC.decode(packetBuffer);
            return new BrewingRecipe(ingredients, cup, brewingTime, result);
        }

        public static void toNetwork(RegistryFriendlyByteBuf packetBuffer, BrewingRecipe brewingRecipe) {
            packetBuffer.writeVarInt(brewingRecipe.input.size());
            for (Ingredient ingredient : brewingRecipe.input) {
                Ingredient.CONTENTS_STREAM_CODEC.encode(packetBuffer, ingredient);
            }
            ItemStackTemplate.STREAM_CODEC.encode(packetBuffer, brewingRecipe.cup);
            packetBuffer.writeVarInt(brewingRecipe.brewingTime);
            ItemStackTemplate.STREAM_CODEC.encode(packetBuffer, brewingRecipe.result);

        }

        private Serializer() {
        }
    }
}
