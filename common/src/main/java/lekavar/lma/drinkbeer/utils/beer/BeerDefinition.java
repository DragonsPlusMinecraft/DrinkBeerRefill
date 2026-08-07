package lekavar.lma.drinkbeer.utils.beer;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.food.FoodProperties;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

public record BeerDefinition(
        int id,
        int nutrition,
        @Nullable Supplier<MobEffectInstance> effectFactory,
        boolean hasEffectTooltip,
        BeerSpecialAction specialAction
) {
    public BeerDefinition {
        if (id <= 0) {
            throw new IllegalArgumentException("Beer id must be positive");
        }
        if (nutrition < 0) {
            throw new IllegalArgumentException("Beer nutrition cannot be negative");
        }
    }

    public FoodProperties foodProperties(float saturationModifier) {
        FoodProperties.Builder builder = new FoodProperties.Builder()
                .nutrition(nutrition)
                .saturationModifier(saturationModifier)
                .alwaysEdible();
        if (effectFactory != null) {
            builder.effect(effectFactory.get(), 1.0F);
        }
        return builder.build();
    }
}
