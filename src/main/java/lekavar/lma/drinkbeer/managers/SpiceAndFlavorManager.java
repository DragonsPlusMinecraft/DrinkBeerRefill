package lekavar.lma.drinkbeer.managers;

import lekavar.lma.drinkbeer.DrinkBeerConfig;
import lekavar.lma.drinkbeer.utils.mixedbeer.FlavorCombinations;
import lekavar.lma.drinkbeer.utils.mixedbeer.Flavors;
import lekavar.lma.drinkbeer.utils.mixedbeer.MixedBeerOnUsing;
import lekavar.lma.drinkbeer.utils.mixedbeer.Spices;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SpiceAndFlavorManager {
    public static String getSpiceToolTipTranslationKey() {
        return "item.drinkbeer.spice.tooltip";
    }

    public static String getNoFlavorToolTipTranslationKey() {
        return "item.drinkbeer.spice.tooltip_no_flavor";
    }

    public static String getFlavorToolTipTranslationKey() {
        return "item.drinkbeer.spice.tooltip_flavor";
    }

    public static String getFlavorTranslationKey(int flavorId) {
        return getFlavorTranslationKey(Flavors.byId(flavorId));
    }

    public static String getFlavorTranslationKey(Flavors flavor) {
        return "drinkbeer.flavor." + flavor.getName();
    }

    public static String getFlavorToolTipTranslationKey(int flavorId) {
        return getFlavorTranslationKey(flavorId) + ".tooltip";
    }

    public static SimpleParticleType getLastSpiceFlavorParticle(List<Integer> spiceList) {
        if (spiceList != null) {
            if (!spiceList.isEmpty()) {
                int lastSpiceId = spiceList.get(spiceList.size() - 1);
                return Spices.byId(lastSpiceId).getFlavor().getParticle();
            }
        }
        return (SimpleParticleType) Flavors.DEFAULT_PARTICLE;
    }

    private static List<Flavors> spiceListToFlavorList(@NotNull List<Integer> spiceList) {
        List<Flavors> flavorList = new ArrayList<>();
        if (!spiceList.isEmpty()) {
            for (int spiceId : spiceList) {
                flavorList.add(Spices.byId(spiceId).getFlavor());
            }
        }
        return flavorList;
    }

    public static Flavors getCombinedFlavor(List<Integer> spiceList) {
        FlavorCombinations flavorCombination = matchFlavorCombination(spiceListToFlavorList(spiceList));
        return flavorCombination != null ? flavorCombination.getCombinedFlavor() : null;
    }

    private static FlavorCombinations matchFlavorCombination(List<Flavors> flavorList) {
        if (flavorList == null)
            return null;
        //Trans flavor to it's father flavor(if exists)
        for (int i = 0; i < flavorList.size(); i++) {
            Flavors fatherFlavor = flavorList.get(i).getFatherFlavor();
            if (fatherFlavor != null) {
                flavorList.set(i, fatherFlavor);
            }
        }
        for (FlavorCombinations flavorCombination : FlavorCombinations.getFlavorCombinationList()) {
            List<Pair<List<Flavors>, Boolean>> flavorCombinationList = flavorCombination.getFlavorCombination().getFlavorCombinationList();
            for (Pair<List<Flavors>, Boolean> flavorCombinationPair : flavorCombinationList) {
                //Check flavors in order
                if (flavorCombinationPair.getValue()) {
                    if (flavorCombinationPair.getKey().equals(flavorList)) {
                        return flavorCombination;
                    }
                }
                //Check flavors out of order
                else {
                    List<Flavors> tempFlavorList1 = new ArrayList<>(flavorList);
                    List<Flavors> tempFlavorList2 = new ArrayList<>(flavorCombinationPair.getKey());

                    Collections.sort(tempFlavorList1);
                    Collections.sort(tempFlavorList2);
                    if (tempFlavorList1.equals(tempFlavorList2)) {
                        return flavorCombination;
                    }
                }
            }
        }
        return null;
    }

    public static void applyFlavorValue(MixedBeerOnUsing mixedBeerOnUsing) {
        if (!mixedBeerOnUsing.getFlavorList().isEmpty()) {
            for (Flavors flavor : mixedBeerOnUsing.getFlavorList()) {
                switch (flavor) {
                    case SPICY, FIERY -> applySpicyFlavorValue(mixedBeerOnUsing, flavor);
                    case AROMATIC, AROMATIC1 -> applyAromiticFlavorValue(mixedBeerOnUsing, flavor);
                    case REFRESHING, REFRESHING1 -> applyRefreshingFlavorValue(mixedBeerOnUsing, flavor);
                    case NUTTY, NUTTY1 -> applyNuttyFlavorAction(mixedBeerOnUsing, flavor);
                    case SWEET, CLOYING, LUSCIOUS -> applySweetFlavorAction(mixedBeerOnUsing, flavor);
                    case MELLOW -> applyMellowFlavorAction(mixedBeerOnUsing, flavor);
                    default -> mixedBeerOnUsing.addAction(flavor);
                }
            }
        }
    }

    public static void applyFlavorAction(MixedBeerOnUsing mixedBeerOnUsing, Level world, LivingEntity user) {
        List<Flavors> actionList = mixedBeerOnUsing.getActionList();
        if (!actionList.isEmpty()) {
            for (int i = 0; i < actionList.size(); i++) {
                Flavors flavor = actionList.get(i);
                switch (flavor) {
                    case SOOOOO_SPICY -> applySoooooSpicyFlavorAction(user);
                    case STORMY -> {
                        if (!MixedBeerManager.hasActionAfter(i, Flavors.STORMY, mixedBeerOnUsing.getActionList())
                                && !MixedBeerManager.hasActionAfter(i, Flavors.THE_FALL_OF_THE_GIANT, mixedBeerOnUsing.getActionList()))
                            applyStormyFlavorAction(MixedBeerManager.getActionedTimes(i, Flavors.STORMY, mixedBeerOnUsing.getActionList()), world, user);
                    }
                    case THE_FALL_OF_THE_GIANT -> applyStormyFlavorAction(3, world, user);
                    case DRYING -> {
                        if (!MixedBeerManager.hasActionAfter(i, Flavors.DRYING, mixedBeerOnUsing.getActionList())) {
                            applyDryingFlavorAction(MixedBeerManager.getActionedTimes(i, Flavors.DRYING, mixedBeerOnUsing.getActionList()), world, user);
                        }
                    }
                }
            }
        }
    }

    public static void applySpicyFlavorValue(MixedBeerOnUsing mixedBeerOnUsing, Flavors flavor) {
        switch (flavor) {
            case SPICY -> {
                mixedBeerOnUsing.addHealth(-3);
                mixedBeerOnUsing.multiplyAllStatusEffectDuration(1.8f);
            }
            case FIERY -> {
                mixedBeerOnUsing.addHealth(-4);
                mixedBeerOnUsing.multiplyAllStatusEffectDuration(2f);
            }
        }
    }

    public static void applySoooooSpicyFlavorAction(LivingEntity user) {
        int fireTicks = 100;
        if (!user.isOnFire()) {
            user.setRemainingFireTicks(fireTicks);
        } else {
            user.setRemainingFireTicks(user.getRemainingFireTicks() + fireTicks);
        }
    }

    public static void applyAromiticFlavorValue(MixedBeerOnUsing mixedBeerOnUsing, Flavors flavor) {
        switch (flavor) {
            case AROMATIC -> mixedBeerOnUsing.addAllStatusEffectDuration(800);
            case AROMATIC1 -> mixedBeerOnUsing.addAllStatusEffectDuration(1200);
        }
    }

    public static void applyRefreshingFlavorValue(MixedBeerOnUsing mixedBeerOnUsing, Flavors flavor) {
        switch (flavor) {
            case REFRESHING -> mixedBeerOnUsing.addDrunkValue(-1);
            case REFRESHING1 -> mixedBeerOnUsing.addDrunkValue(-2);
        }
    }

    public static void applyStormyFlavorAction(int actionedTimes, Level world, LivingEntity user) {
        if (!canChangeWorld(world)) {
            return;
        }
        int range = 9 + (actionedTimes == 3 ? 22 : actionedTimes * 4);
        int halfRange = (range - 1) / 2;
        int xStart = 0;
        int xEnd = 0;
        int zStart = 0;
        int zEnd = 0;
        Direction direction = user.getMotionDirection();

        switch (direction) {
            case NORTH -> {
                xStart = -halfRange;
                xEnd = halfRange + 1;
                zStart = -range;
            }
            case SOUTH -> {
                xStart = -halfRange;
                xEnd = halfRange + 1;
                zEnd = range;
            }
            case EAST -> {
                zStart = -halfRange;
                zEnd = halfRange + 1;
                xEnd = range;
            }
            case WEST -> {
                zStart = -halfRange;
                zEnd = halfRange + 1;
                xStart = -range;
            }
        }
        int remainingChanges = DrinkBeerConfig.MAX_WORLD_CHANGES_PER_DRINK.get();
        for (int x = xStart; x < xEnd && remainingChanges > 0; x++) {
            for (int y = 0; y < range; y++) {
                for (int z = zStart; z < zEnd; z++) {
                    BlockPos pos = user.blockPosition().offset(x, y, z);
                    if (!world.isLoaded(pos)) {
                        continue;
                    }
                    BlockState blockState = world.getBlockState(pos);
                    if ((blockState.is(BlockTags.LOGS) || blockState.is(BlockTags.LEAVES))
                            && world.destroyBlock(pos, true)
                            && --remainingChanges == 0) {
                        return;
                    }
                }
            }
        }
    }

    public static void applyNuttyFlavorAction(MixedBeerOnUsing mixedBeerOnUsing, Flavors flavor) {
        switch (flavor) {
            case NUTTY -> mixedBeerOnUsing.addHunger(4);
            case NUTTY1 -> mixedBeerOnUsing.addHunger(5);
        }
    }

    public static void applySweetFlavorAction(MixedBeerOnUsing mixedBeerOnUsing, Flavors flavor) {
        switch (flavor) {
            case SWEET -> mixedBeerOnUsing.addHealth(3);
            case LUSCIOUS -> mixedBeerOnUsing.addHealth(4);
            case CLOYING -> mixedBeerOnUsing.addHealth(1);
        }
    }

    public static void applyMellowFlavorAction(MixedBeerOnUsing mixedBeerOnUsing, Flavors flavor) {
        switch (flavor) {
            case MELLOW -> mixedBeerOnUsing.addSpecificStatusEffectDuration(MobEffects.DAMAGE_RESISTANCE, 1600);
        }
    }

    public static void applyDryingFlavorAction(int actionedTimes, Level world, LivingEntity user) {
        if (!canChangeWorld(world)) {
            return;
        }
        int range = 17 + actionedTimes * 4;
        int halfRange = (range - 1) / 2;
        int xStart = 0;
        int xEnd = 0;
        int zStart = 0;
        int zEnd = 0;
        Direction direction = user.getMotionDirection();

        switch (direction) {
            case NORTH -> {
                xStart = -halfRange;
                xEnd = halfRange + 1;
                zStart = -range;
            }
            case SOUTH -> {
                xStart = -halfRange;
                xEnd = halfRange + 1;
                zEnd = range;
            }
            case EAST -> {
                zStart = -halfRange;
                zEnd = halfRange + 1;
                xEnd = range;
            }
            case WEST -> {
                zStart = -halfRange;
                zEnd = halfRange + 1;
                xStart = -range;
            }
        }
        int remainingChanges = DrinkBeerConfig.MAX_WORLD_CHANGES_PER_DRINK.get();
        for (int x = xStart; x < xEnd && remainingChanges > 0; x++) {
            for (int y = 0; y < range; y++) {
                for (int z = zStart; z < zEnd; z++) {
                    BlockPos pos = user.blockPosition().offset(x, y, z);
                    if (!world.isLoaded(pos)) {
                        continue;
                    }
                    BlockState blockState = world.getBlockState(pos);
                    if (blockState.is(Blocks.WATER)
                            && world.setBlockAndUpdate(pos, Blocks.AIR.defaultBlockState())
                            && --remainingChanges == 0) {
                        return;
                    }
                }
            }
        }
    }

    private static boolean canChangeWorld(Level world) {
        return !world.isClientSide()
                && DrinkBeerConfig.ENABLE_WORLD_CHANGING_FLAVOR_EFFECTS.get()
                && DrinkBeerConfig.MAX_WORLD_CHANGES_PER_DRINK.get() > 0;
    }
}
