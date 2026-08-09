package lekavar.lma.drinkbeer.managers;

import lekavar.lma.drinkbeer.blockentities.MixedBeerBlockEntity;
import lekavar.lma.drinkbeer.registries.BlockRegistry;
import lekavar.lma.drinkbeer.registries.BlockEntityRegistry;
import lekavar.lma.drinkbeer.registries.ItemRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.TypedEntityData;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MixedBeerManagerTest {
    @Test
    void modernComponentsRoundTripAndRejectInvalidSpices() {
        ItemStack stack = MixedBeerManager.genMixedBeerItemStack(4, List.of(1, 7, 15, 99));

        assertEquals(4, MixedBeerManager.getBeerId(stack));
        assertEquals(List.of(1, 7, 15), MixedBeerManager.getSpiceList(stack));
    }

    @Test
    void legacyBlockEntityTagSurvivesComponentApplicationDuringPlacement() {
        CompoundTag descriptor = new CompoundTag();
        descriptor.putInt("beerId", 6);
        descriptor.putIntArray("spiceList", new int[]{4, 8});

        CompoundTag legacyBlockEntity = new CompoundTag();
        legacyBlockEntity.put("MixedBeer", descriptor);
        CompoundTag legacyRoot = new CompoundTag();
        legacyRoot.put("BlockEntityTag", legacyBlockEntity);

        ItemStack legacyStack = new ItemStack(ItemRegistry.MIXED_BEER.get());
        legacyStack.set(
                DataComponents.BLOCK_ENTITY_DATA,
                TypedEntityData.of(BlockEntityRegistry.MIXED_BEER_TILEENTITY.get(), legacyRoot)
        );

        assertEquals(6, MixedBeerManager.getBeerId(legacyStack));
        assertEquals(List.of(4, 8), MixedBeerManager.getSpiceList(legacyStack));

        MixedBeerBlockEntity blockEntity = new MixedBeerBlockEntity(
                BlockPos.ZERO,
                BlockRegistry.MIXED_BEER.get().defaultBlockState()
        );
        blockEntity.applyComponentsFromItemStack(legacyStack);

        assertEquals(6, blockEntity.getBeerId());
        assertEquals(List.of(4, 8), blockEntity.getSpiceList());
    }
}
