package lekavar.lma.drinkbeer.compat.jei;

import com.mojang.serialization.Codec;
import lekavar.lma.drinkbeer.DrinkBeer;
import lekavar.lma.drinkbeer.recipes.BrewingRecipe;
import lekavar.lma.drinkbeer.registries.ItemRegistry;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.builder.ITooltipBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.ICodecHelper;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.IRecipeManager;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;


public class JEIBrewingRecipeCategory implements IRecipeCategory<BrewingRecipe> {
    public static final RecipeType<BrewingRecipe> TYPE = RecipeType.create(DrinkBeer.MOD_ID, "brewing", BrewingRecipe.class);
    private static final String DRINK_BEER_YELLOW = "#F4D223";
    private static final String NIGHT_HOWL_CUP_HEX_COLOR = "#C69B82";
    private static final String PUMPKIN_DRINK_CUP_HEX_COLOR = "#AC6132";
    private final IDrawable background;
    private final IDrawable icon;
    private final IGuiHelper guiHelper;

    public JEIBrewingRecipeCategory(IGuiHelper helper) {
        guiHelper = helper;
        background = helper.createDrawable(ResourceLocation.fromNamespaceAndPath(DrinkBeer.MOD_ID, "textures/gui/jei/brewing_gui.png"),
                0, 0, 175, 69);
        icon = helper.createDrawableItemStack(new ItemStack(ItemRegistry.BEER_MUG.get()));
    }

    @Override
    public RecipeType<BrewingRecipe> getRecipeType() {
        return TYPE;
    }

    @Override
    public Component getTitle() {
        return Component.translatable("drinkbeer.jei.title.brewing");
    }

    @Override
    public int getWidth() {
        return 175;
    }

    @Override
    public int getHeight() {
        return 69;
    }

    @Override
    public IDrawable getIcon() {
        return icon;
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, BrewingRecipe recipe, IFocusGroup focuses) {
        var ingredients = recipe.getIngredients();
        var slotBuilder = builder.addSlot(RecipeIngredientRole.INPUT, 28, 16);
        slotBuilder.addIngredients(ingredients.get(0));

        slotBuilder = builder.addSlot(RecipeIngredientRole.INPUT, 46, 16);
        slotBuilder.addIngredients(ingredients.get(1));

        slotBuilder = builder.addSlot(RecipeIngredientRole.INPUT, 28, 34);
        slotBuilder.addIngredients(ingredients.get(2));

        slotBuilder = builder.addSlot(RecipeIngredientRole.INPUT, 46, 34);
        slotBuilder.addIngredients(ingredients.get(3));

        slotBuilder = builder.addSlot(RecipeIngredientRole.OUTPUT, 128, 24);
        slotBuilder.addItemStack(recipe.getResultItemNoRegistryAccess());

        builder.moveRecipeTransferButton(156, 50);
    }

    @Override
    public void draw(BrewingRecipe recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics guiGraphics, double mouseX, double mouseY) {
        background.draw(guiGraphics, 0, 0);
        IDrawable drawable = guiHelper.createDrawableItemStack(recipe.getBeerCup());
        drawable.draw(guiGraphics, 73, 40);
    }

    @Override
    public void getTooltip(ITooltipBuilder tooltip, BrewingRecipe recipe, IRecipeSlotsView recipeSlotsView, double mouseX, double mouseY) {
        if (!inTransferBottomRange(mouseX, mouseY)) {
            if (inCupSlotRange(mouseX, mouseY)) {
                tooltip.add(Component.translatable("drinkbeer.jei.tooltip.cup_slot")
                        .setStyle(Style.EMPTY.withColor(TextColor.parseColor(DRINK_BEER_YELLOW).getOrThrow())));
                tooltip.add(Component.translatable("drinkbeer.jei.tooltip.cup_1")
                        .setStyle(Style.EMPTY.withColor(TextColor.parseColor(NIGHT_HOWL_CUP_HEX_COLOR).getOrThrow()))
                        .append(Component.literal(recipe.getRequiredCupCount() + " ")
                                .withStyle(Style.EMPTY.withBold(true).withColor(TextColor.parseColor(DRINK_BEER_YELLOW).getOrThrow())))
                        .append(Component.translatable(recipe.getBeerCup().getItem().getDescriptionId())
                                .withStyle(ChatFormatting.WHITE))
                        .append(Component.translatable("drinkbeer.jei.tooltip.cup_2")
                                .setStyle(Style.EMPTY.withColor(TextColor.parseColor(NIGHT_HOWL_CUP_HEX_COLOR).getOrThrow()))));
            } else {
                int brewingTimeMin = (recipe.getBrewingTime() / 20) / 60;
                int brewingTimeSec = recipe.getBrewingTime() / 20 - brewingTimeMin * 60;
                tooltip.add(Component.translatable("drinkbeer.jei.tooltip.brewing")
                        .setStyle(Style.EMPTY.withColor(TextColor.parseColor(PUMPKIN_DRINK_CUP_HEX_COLOR).getOrThrow()))
                        .append(Component.literal(brewingTimeMin + ":" + (brewingTimeSec < 10 ? "0" + brewingTimeSec : brewingTimeSec))
                                .withStyle(Style.EMPTY.withBold(true).withColor(TextColor.parseColor(DRINK_BEER_YELLOW).getOrThrow()))));
            }
        }
    }

    @Override
    public Codec<BrewingRecipe> getCodec(ICodecHelper codecHelper, IRecipeManager recipeManager) {
        return BrewingRecipe.Serializer.CODEC.codec();
    }

    private boolean inCupSlotRange(double mouseX, double mouseY) {
        return mouseX >= 72 && mouseX < 90 && mouseY >= 39 && mouseY <= 57;
    }

    private boolean inTransferBottomRange(double mouseX, double mouseY) {
        return mouseX >= 156 && mouseX < 169 && mouseY >= 50 && mouseY < 63;
    }
}