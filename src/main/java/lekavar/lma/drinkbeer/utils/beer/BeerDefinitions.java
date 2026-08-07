package lekavar.lma.drinkbeer.utils.beer;

import lekavar.lma.drinkbeer.registries.MobEffectRegistry;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;

import java.util.List;

public final class BeerDefinitions {
    public static final BeerDefinition BEER_MUG = new BeerDefinition(
            1, 2, () -> new MobEffectInstance(MobEffects.DIG_SPEED, 1200), true, BeerSpecialAction.NONE);
    public static final BeerDefinition BLAZE_STOUT = new BeerDefinition(
            2, 1, () -> new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 1800), true, BeerSpecialAction.NONE);
    public static final BeerDefinition BLAZE_MILK_STOUT = new BeerDefinition(
            3, 1, () -> new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 2400), true, BeerSpecialAction.NONE);
    public static final BeerDefinition APPLE_LAMBIC = new BeerDefinition(
            4, 1, () -> new MobEffectInstance(MobEffects.REGENERATION, 300), true, BeerSpecialAction.NONE);
    public static final BeerDefinition SWEET_BERRY_KRIEK = new BeerDefinition(
            5, 1, () -> new MobEffectInstance(MobEffects.REGENERATION, 400), true, BeerSpecialAction.NONE);
    public static final BeerDefinition HAARS_ICEY_PALE_LAGER = new BeerDefinition(
            6, 1, () -> new MobEffectInstance(MobEffectRegistry.DRUNK_FROST_WALKER.get(), 1200), true, BeerSpecialAction.NONE);
    public static final BeerDefinition PUMPKIN_KVASS = new BeerDefinition(
            7, 9, null, false, BeerSpecialAction.NONE);
    public static final BeerDefinition FROTHY_PINK_EGGNOG = new BeerDefinition(
            8, 2, () -> new MobEffectInstance(MobEffects.ABSORPTION, 2400), true, BeerSpecialAction.FROTHY_GIFT);
    public static final BeerDefinition NIGHT_HOWL_KVASS = new BeerDefinition(
            9, 4, null, true, BeerSpecialAction.NIGHT_HOWL);

    public static final List<BeerDefinition> ALL = List.of(
            BEER_MUG,
            BLAZE_STOUT,
            BLAZE_MILK_STOUT,
            APPLE_LAMBIC,
            SWEET_BERRY_KRIEK,
            HAARS_ICEY_PALE_LAGER,
            PUMPKIN_KVASS,
            FROTHY_PINK_EGGNOG,
            NIGHT_HOWL_KVASS
    );

    public static BeerDefinition byId(int id) {
        return ALL.stream().filter(definition -> definition.id() == id).findFirst().orElse(BEER_MUG);
    }

    private BeerDefinitions() {
    }
}
