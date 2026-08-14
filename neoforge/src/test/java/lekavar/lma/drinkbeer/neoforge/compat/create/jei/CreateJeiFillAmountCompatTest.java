package lekavar.lma.drinkbeer.neoforge.compat.create.jei;

import lekavar.lma.drinkbeer.registries.FluidRegistry;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.IFluidHandlerItem;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

class CreateJeiFillAmountCompatTest {
    @Test
    void replacesCreateDisplayAmountForDrinkBeerFluids() {
        FluidStack requested = new FluidStack(FluidRegistry.beers().getFirst().source(), 1000);
        RecordingHandler handler = new RecordingHandler(FluidRegistry.SERVING_MILLIBUCKETS);

        int filled = CreateJeiFillAmountCompat.fillForDisplay(
                handler,
                requested,
                IFluidHandler.FluidAction.EXECUTE
        );

        assertEquals(FluidRegistry.SERVING_MILLIBUCKETS, filled);
        assertEquals(FluidRegistry.SERVING_MILLIBUCKETS, requested.getAmount());
        assertSame(requested, handler.received);
        assertSame(IFluidHandler.FluidAction.EXECUTE, handler.action);
        assertSame(handler.originalContainer, handler.getContainer());
    }

    @Test
    void leavesOtherModsFluidsUntouched() {
        FluidStack requested = new FluidStack(Fluids.WATER, 1000);
        RecordingHandler handler = new RecordingHandler(FluidRegistry.SERVING_MILLIBUCKETS);

        assertEquals(
                FluidRegistry.SERVING_MILLIBUCKETS,
                CreateJeiFillAmountCompat.fillForDisplay(
                        handler,
                        requested,
                        IFluidHandler.FluidAction.SIMULATE
                )
        );
        assertEquals(1000, requested.getAmount());
        assertSame(IFluidHandler.FluidAction.SIMULATE, handler.action);
    }

    @Test
    void leavesRejectedFillsUntouched() {
        FluidStack requested = new FluidStack(FluidRegistry.beers().getFirst().source(), 1000);
        RecordingHandler handler = new RecordingHandler(0);

        assertEquals(
                0,
                CreateJeiFillAmountCompat.fillForDisplay(
                        handler,
                        requested,
                        IFluidHandler.FluidAction.EXECUTE
                )
        );
        assertEquals(1000, requested.getAmount());
    }

    private static final class RecordingHandler implements IFluidHandlerItem {
        private final int fillResult;
        private final ItemStack originalContainer = new ItemStack(Items.GLASS_BOTTLE);
        private FluidStack received;
        private FluidAction action;

        private RecordingHandler(int fillResult) {
            this.fillResult = fillResult;
        }

        @Override
        public ItemStack getContainer() {
            return originalContainer;
        }

        @Override
        public int getTanks() {
            return 0;
        }

        @Override
        public FluidStack getFluidInTank(int tank) {
            return FluidStack.EMPTY;
        }

        @Override
        public int getTankCapacity(int tank) {
            return 0;
        }

        @Override
        public boolean isFluidValid(int tank, FluidStack stack) {
            return false;
        }

        @Override
        public int fill(FluidStack resource, FluidAction action) {
            this.received = resource;
            this.action = action;
            return fillResult;
        }

        @Override
        public FluidStack drain(FluidStack resource, FluidAction action) {
            return FluidStack.EMPTY;
        }

        @Override
        public FluidStack drain(int maxDrain, FluidAction action) {
            return FluidStack.EMPTY;
        }
    }
}
