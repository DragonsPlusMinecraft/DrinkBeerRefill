package lekavar.lma.drinkbeer.managers;

import lekavar.lma.drinkbeer.DrinkBeerConfig;
import lekavar.lma.drinkbeer.effects.DrunkStatusEffect;
import lekavar.lma.drinkbeer.effects.NightHowlStatusEffect;
import lekavar.lma.drinkbeer.items.MixedBeerBlockItem;
import lekavar.lma.drinkbeer.registries.DamageRegistry;
import lekavar.lma.drinkbeer.registries.ItemRegistry;
import lekavar.lma.drinkbeer.utils.beer.Beers;
import lekavar.lma.drinkbeer.utils.beer.BeerConsumptionEffects;
import lekavar.lma.drinkbeer.utils.mixedbeer.Flavors;
import lekavar.lma.drinkbeer.utils.mixedbeer.MixedBeerOnUsing;
import lekavar.lma.drinkbeer.utils.mixedbeer.Spices;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class MixedBeerManager {
    public static final int MAX_SPICES_COUNT = 3;

    public static ItemStack genMixedBeerItemStack(int beerId, int... spiceIds) {
        List<Integer> spiceList = new ArrayList<>();
        for (int spiceId : spiceIds) {
            spiceList.add(spiceId);
        }
        return genMixedBeerItemStack(beerId, spiceList);
    }

    public static ItemStack genMixedBeerItemStack(int beerId, List<Integer> spiceList) {
        ItemStack resultStack = new ItemStack(ItemRegistry.MIXED_BEER.get(), 1);
        CompoundTag descriptor = new CompoundTag();
        descriptor.putInt("beerId", sanitizeBeerId(beerId));
        descriptor.putIntArray("spiceList", sanitizeSpiceIds(spiceList));
        resultStack.getOrCreateTagElement("BlockEntityTag").put("MixedBeer", descriptor);
        return resultStack;
    }

    public static List<Integer> sanitizeSpiceIds(List<Integer> spiceList) {
        if (spiceList == null) {
            return List.of();
        }
        return spiceList.stream()
                .filter(Objects::nonNull)
                .filter(value -> (value.compareTo(Spices.EMPTY_SPICE_ID) > 0) && (value.compareTo(Spices.size()) <= 0))
                .limit(MAX_SPICES_COUNT)
                .collect(Collectors.toList());
    }

    public static int getBeerId(ItemStack itemStack) {
        if (itemStack.getItem() instanceof MixedBeerBlockItem) {
            CompoundTag descriptor = normalizeAndGetDescriptor(itemStack);
            if (descriptor != null && descriptor.contains("beerId", Tag.TAG_ANY_NUMERIC)) {
                return descriptor.getInt("beerId");
            }
            return Beers.EMPTY_BEER_ID;
        }
        return Beers.EMPTY_BEER_ID;
    }

    public static List<Integer> getSpiceList(ItemStack itemStack) {
        if (itemStack.getItem() instanceof MixedBeerBlockItem) {
            CompoundTag descriptor = normalizeAndGetDescriptor(itemStack);
            if (descriptor != null && descriptor.contains("spiceList", Tag.TAG_INT_ARRAY)) {
                return new ArrayList<>(java.util.Arrays.stream(descriptor.getIntArray("spiceList")).boxed().toList());
            }
            return new ArrayList<>();
        }
        return new ArrayList<>();
    }

    public static int sanitizeBeerId(int beerId) {
        if (beerId == Beers.EMPTY_BEER_ID) {
            return beerId;
        }
        return beerId > Beers.EMPTY_BEER_ID && beerId <= Beers.size() ? beerId : Beers.DEFAULT_BEER_ID;
    }

    @Nullable
    private static CompoundTag getLegacyDescriptor(ItemStack itemStack) {
        CompoundTag descriptor = findLegacyDescriptor(itemStack.getTagElement("BlockEntityTag"));
        if (descriptor != null) {
            return descriptor;
        }
        return findLegacyDescriptor(itemStack.getTag());
    }

    @Nullable
    private static CompoundTag normalizeAndGetDescriptor(ItemStack itemStack) {
        CompoundTag source = getLegacyDescriptor(itemStack);
        if (source == null) {
            return null;
        }

        CompoundTag canonical = new CompoundTag();
        canonical.putInt("beerId", sanitizeBeerId(source.getInt("beerId")));
        List<Integer> spices = source.contains("spiceList", Tag.TAG_INT_ARRAY)
                ? java.util.Arrays.stream(source.getIntArray("spiceList")).boxed().toList()
                : List.of();
        canonical.putIntArray("spiceList", sanitizeSpiceIds(spices));

        CompoundTag root = itemStack.getOrCreateTag();
        root.remove("beerId");
        root.remove("spiceList");
        root.remove("MixedBeer");
        CompoundTag blockEntityTag = itemStack.getOrCreateTagElement("BlockEntityTag");
        blockEntityTag.remove("beerId");
        blockEntityTag.remove("spiceList");
        blockEntityTag.put("MixedBeer", canonical);
        return canonical;
    }

    @Nullable
    public static CompoundTag findLegacyDescriptor(CompoundTag tag) {
        if (tag == null) {
            return null;
        }
        if (tag.contains("beerId", Tag.TAG_ANY_NUMERIC) || tag.contains("spiceList", Tag.TAG_INT_ARRAY)) {
            return tag;
        }
        if (tag.contains("MixedBeer", Tag.TAG_COMPOUND)) {
            return tag.getCompound("MixedBeer");
        }
        if (tag.contains("BlockEntityTag", Tag.TAG_COMPOUND)) {
            CompoundTag blockEntityTag = tag.getCompound("BlockEntityTag");
            if (blockEntityTag.contains("MixedBeer", Tag.TAG_COMPOUND)) {
                return blockEntityTag.getCompound("MixedBeer");
            }
        }
        return null;
    }

    public static String getMixedBeerTranslationKey() {
        return ItemRegistry.MIXED_BEER.get().asItem().toString();
    }

    public static String getBaseBeerToolTipTranslationKey() {
        return "item.drinkbeer.mixed_beer.tooltip_base";
    }

    public static String getUnmixedToolTipTranslationKey() {
        return "item.drinkbeer.mixed_beer.tooltip_unmixed";
    }

    public static void useMixedBeer(ItemStack stack, Level world, LivingEntity user) {
        /*Initialize properties!*/
        /*------------------------------------------------------------------------------------------------------------------*/
        MixedBeerOnUsing mixedBeerOnUsing = new MixedBeerOnUsing();
        //Initialize beer
        mixedBeerOnUsing.setBeer(Beers.byId(getBeerId(stack)));
        //Initialize food level
        mixedBeerOnUsing.addHunger(Objects.requireNonNull(mixedBeerOnUsing.getBeerItem().getFoodProperties()).getNutrition());
        //Initialize spices and flavors
        List<Integer> spiceList = getSpiceList(stack);
        mixedBeerOnUsing.setSpiceList(spiceList);
        Flavors combinedFlavor = SpiceAndFlavorManager.getCombinedFlavor(spiceList);
        if (combinedFlavor != null) {
            mixedBeerOnUsing.addFlavor(combinedFlavor);
        }

        /*Deal with properties!*/
        /*------------------------------------------------------------------------------------------------------------------*/
        //Add base beer status effect
        mixedBeerOnUsing.addStatusEffect(getBeerStatusEffectList(mixedBeerOnUsing.getBeerItem().getDefaultInstance(), world));
        //Deal with flavors
        SpiceAndFlavorManager.applyFlavorValue(mixedBeerOnUsing);

        /*Apply properties!*/
        /*------------------------------------------------------------------------------------------------------------------*/
        //Apply Drunk status effect
        DrunkStatusEffect.addStatusEffect(user, mixedBeerOnUsing.getDrunkValue());
        //Apply food level
        if (user instanceof Player && !((Player) user).isCreative()) {
            ((Player) user).getFoodData().eat(mixedBeerOnUsing.getHunger(), DrinkBeerConfig.beerSaturationModifier());
        }
        //Apply health
        if (user instanceof Player) {
            if (!((Player) user).isCreative()) {
                if (mixedBeerOnUsing.getHealth() < 0) {
                    user.hurt(DamageRegistry.alcohol(world.registryAccess()), Math.abs(mixedBeerOnUsing.getHealth()));
                } else {
                    user.heal(mixedBeerOnUsing.getHealth());
                }
            }
        } else {
            user.setHealth(user.getHealth() + mixedBeerOnUsing.getHealth());
        }
        //Apply status effects
        for (Pair<MobEffect, Integer> statusEffectPair : mixedBeerOnUsing.getStatusEffectList()) {
            user.addEffect(new MobEffectInstance(statusEffectPair.getKey(), statusEffectPair.getValue()));
        }
        //Apply flavor actions
        SpiceAndFlavorManager.applyFlavorAction(mixedBeerOnUsing, world, user);
        BeerConsumptionEffects.finishMixedDrink(mixedBeerOnUsing.getBeer().getDefinition(), world, user);
    }

    private static List<Pair<MobEffect, Integer>> getBeerStatusEffectList(ItemStack stack, Level world) {
        List<Pair<MobEffect, Integer>> resultStatusEffectList = new ArrayList<>();
        List<com.mojang.datafixers.util.Pair<MobEffectInstance, Float>> possibleEffects = stack.getItem().getFoodProperties().getEffects();
        if (possibleEffects != null && !possibleEffects.isEmpty()) {
            for (com.mojang.datafixers.util.Pair<MobEffectInstance, Float> possibleEffect : possibleEffects) {
                MobEffectInstance effect = possibleEffect.getFirst();
                resultStatusEffectList.add(Pair.of(effect.getEffect(), effect.getDuration()));
            }
        }
        if (stack.getItem().equals(Beers.BEER_MUG_NIGHT_HOWL_KVASS.getBeerItem())) {
            Pair<MobEffect, Integer> nightHowlStatusEffectPair = NightHowlStatusEffect.getStatusEffectPair(world);
            resultStatusEffectList.add(nightHowlStatusEffectPair);
        }
        return resultStatusEffectList;
    }

    /**
     * Get the number of the target action occurrences before current action.
     *
     * @param index        Current action's index in actionList
     * @param targetAction Which action to find
     * @param actionList   Current mixed beer's actionList
     * @return Number of the target action occurrences before current action
     */
    public static int getActionedTimes(int index, Flavors targetAction, List<Flavors> actionList) {
        if (index == 0)
            return 0;
        int actionTime = 0;
        for (int i = 0; i < index; i++) {
            if (actionList.get(i).equals(targetAction)) {
                actionTime++;
            }
        }
        return actionTime;
    }

    /**
     * Whether the target action exists before the current action.
     *
     * @param index        Current action's index in actionList
     * @param targetAction Which action to find
     * @param actionList   Current mixed beer's actionList
     * @return
     */
    public static boolean hasActionedBefore(int index, Flavors targetAction, List<Flavors> actionList) {
        return getActionedTimes(index, targetAction, actionList) != 0;
    }

    /**
     * Whether the target action exists after current action.
     *
     * @param index        Current action's index in actionList
     * @param targetAction Which action to find
     * @param actionList   Current mixed beer's actionList
     * @return
     */
    public static boolean hasActionAfter(int index, Flavors targetAction, List<Flavors> actionList) {
        if (actionList.size() - 1 == index) {
            return false;
        } else {
            for (int i = index + 1; i < actionList.size(); i++) {
                if (actionList.get(i).equals(targetAction)) {
                    return true;
                }
            }
        }
        return false;
    }
}
