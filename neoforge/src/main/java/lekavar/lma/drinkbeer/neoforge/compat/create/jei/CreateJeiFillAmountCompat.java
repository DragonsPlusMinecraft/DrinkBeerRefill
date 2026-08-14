package lekavar.lma.drinkbeer.neoforge.compat.create.jei;

import lekavar.lma.drinkbeer.registries.FluidRegistry;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.IFluidHandlerItem;

/** Corrects Create's generated JEI ingredient amount without changing filling behavior. */
public final class CreateJeiFillAmountCompat {
    public static int fillForDisplay(
            IFluidHandlerItem handler,
            FluidStack resource,
            IFluidHandler.FluidAction action
    ) {
        int filled = handler.fill(resource, action);
        if (filled > 0 && FluidRegistry.byFluid(resource.getFluid()).isPresent()) {
            resource.setAmount(filled);
        }
        return filled;
    }

    private CreateJeiFillAmountCompat() {
    }
}
