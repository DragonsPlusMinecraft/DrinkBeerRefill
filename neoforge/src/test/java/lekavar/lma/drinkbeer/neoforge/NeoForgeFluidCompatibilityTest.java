package lekavar.lma.drinkbeer.neoforge;

import lekavar.lma.drinkbeer.registries.FluidRegistry;
import lekavar.lma.drinkbeer.registries.ItemRegistry;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.IFluidHandlerItem;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class NeoForgeFluidCompatibilityTest {
    @Test
    void exposesCapabilitiesAndRoundTripsOneMug() {
        FluidRegistry.BeerFluid beer = FluidRegistry.beers().getFirst();
        ItemStack mug = new ItemStack(beer.filledMug());
        mug.set(DataComponents.CUSTOM_NAME, Component.literal("Cellar reserve"));
        IFluidHandlerItem fullHandler = mug.getCapability(Capabilities.FluidHandler.ITEM);
        assertNotNull(fullHandler);

        FluidStack simulated = fullHandler.drain(FluidRegistry.SERVING_MILLIBUCKETS, IFluidHandler.FluidAction.SIMULATE);
        assertEquals(FluidRegistry.SERVING_MILLIBUCKETS, simulated.getAmount());
        assertSame(beer.filledMug(), fullHandler.getContainer().getItem());

        FluidStack drained = fullHandler.drain(FluidRegistry.SERVING_MILLIBUCKETS, IFluidHandler.FluidAction.EXECUTE);
        assertEquals(FluidRegistry.SERVING_MILLIBUCKETS, drained.getAmount());
        assertSame(ItemRegistry.EMPTY_BEER_MUG.get(), fullHandler.getContainer().getItem());
        assertEquals(Component.literal("Cellar reserve"), fullHandler.getContainer().get(DataComponents.CUSTOM_NAME));

        IFluidHandlerItem emptyHandler = fullHandler.getContainer().getCapability(Capabilities.FluidHandler.ITEM);
        assertNotNull(emptyHandler);
        assertEquals(
                FluidRegistry.SERVING_MILLIBUCKETS,
                emptyHandler.fill(drained, IFluidHandler.FluidAction.EXECUTE)
        );
        assertSame(beer.filledMug(), emptyHandler.getContainer().getItem());
        assertEquals(Component.literal("Cellar reserve"), emptyHandler.getContainer().get(DataComponents.CUSTOM_NAME));
    }

    @Test
    void rejectsWrongFluidPartialTransfersAndStackedContainers() {
        FluidRegistry.BeerFluid beer = FluidRegistry.beers().getFirst();
        IFluidHandlerItem fullHandler = new ItemStack(beer.filledMug()).getCapability(Capabilities.FluidHandler.ITEM);
        assertNotNull(fullHandler);

        assertTrue(fullHandler.drain(FluidRegistry.SERVING_MILLIBUCKETS - 1, IFluidHandler.FluidAction.EXECUTE).isEmpty());
        assertTrue(fullHandler.drain(new FluidStack(Fluids.WATER, 1000), IFluidHandler.FluidAction.EXECUTE).isEmpty());

        ItemStack stackedMugs = new ItemStack(beer.filledMug(), 2);
        IFluidHandlerItem stackedHandler = stackedMugs.getCapability(Capabilities.FluidHandler.ITEM);
        assertNotNull(stackedHandler);
        assertTrue(stackedHandler.drain(FluidRegistry.SERVING_MILLIBUCKETS, IFluidHandler.FluidAction.EXECUTE).isEmpty());

        ItemStack stackedEmptyMugs = new ItemStack(ItemRegistry.EMPTY_BEER_MUG.get(), 2);
        IFluidHandlerItem stackedEmptyHandler = stackedEmptyMugs.getCapability(Capabilities.FluidHandler.ITEM);
        assertNotNull(stackedEmptyHandler);
        assertEquals(
                0,
                stackedEmptyHandler.fill(
                        new FluidStack(beer.source(), FluidRegistry.SERVING_MILLIBUCKETS),
                        IFluidHandler.FluidAction.EXECUTE
                )
        );
    }
}
