package lekavar.lma.drinkbeer.fabric;

import lekavar.lma.drinkbeer.registries.FluidRegistry;
import lekavar.lma.drinkbeer.registries.ItemRegistry;
import net.fabricmc.fabric.api.transfer.v1.context.ContainerItemContext;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.item.InventoryStorage;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.level.material.Fluids;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;

class FabricFluidCompatibilityTest {
    private static final long SERVING = FluidConstants.BUCKET / 4;

    @Test
    void drainsAndRefillsOneMugThroughTheTransferApi() {
        FluidRegistry.BeerFluid beer = FluidRegistry.beers().getFirst();
        SimpleContainer inventory = new SimpleContainer(1);
        ItemStack mug = new ItemStack(beer.filledMug());
        mug.set(DataComponents.CUSTOM_NAME, Component.literal("Cellar reserve"));
        inventory.setItem(0, mug);
        ContainerItemContext context = context(inventory);

        Storage<FluidVariant> fullStorage = context.find(FluidStorage.ITEM);
        assertNotNull(fullStorage);
        try (Transaction transaction = Transaction.openOuter()) {
            assertEquals(SERVING, fullStorage.extract(FluidVariant.of(beer.source()), SERVING, transaction));
            transaction.commit();
        }

        assertSame(ItemRegistry.EMPTY_BEER_MUG.get(), inventory.getItem(0).getItem());
        assertEquals(Component.literal("Cellar reserve"), inventory.getItem(0).get(DataComponents.CUSTOM_NAME));

        Storage<FluidVariant> emptyStorage = context.find(FluidStorage.ITEM);
        assertNotNull(emptyStorage);
        try (Transaction transaction = Transaction.openOuter()) {
            assertEquals(SERVING, emptyStorage.insert(FluidVariant.of(beer.source()), SERVING, transaction));
            transaction.commit();
        }

        assertSame(beer.filledMug(), inventory.getItem(0).getItem());
        assertEquals(Component.literal("Cellar reserve"), inventory.getItem(0).get(DataComponents.CUSTOM_NAME));
    }

    @Test
    void rejectsWrongFluidAndPartialTransfersAndRollsBackSimulation() {
        FluidRegistry.BeerFluid beer = FluidRegistry.beers().getFirst();
        SimpleContainer inventory = new SimpleContainer(new ItemStack(beer.filledMug()));
        ContainerItemContext context = context(inventory);
        Storage<FluidVariant> storage = context.find(FluidStorage.ITEM);
        assertNotNull(storage);

        try (Transaction transaction = Transaction.openOuter()) {
            assertEquals(0, storage.extract(FluidVariant.of(Fluids.WATER), SERVING, transaction));
            assertEquals(0, storage.extract(FluidVariant.of(beer.source()), SERVING - 1, transaction));
            assertEquals(SERVING, storage.extract(FluidVariant.of(beer.source()), SERVING, transaction));
        }

        assertSame(beer.filledMug(), inventory.getItem(0).getItem());
    }

    private static ContainerItemContext context(SimpleContainer inventory) {
        return ContainerItemContext.ofSingleSlot(InventoryStorage.of(inventory, null).getSlot(0));
    }
}
