package lekavar.lma.drinkbeer.neoforge;

import lekavar.lma.drinkbeer.registries.FluidRegistry;
import lekavar.lma.drinkbeer.registries.ItemRegistry;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandlerItem;

final class NeoForgeBeerMugFluidHandler implements IFluidHandlerItem {
    private ItemStack container;
    private FluidRegistry.BeerFluid beer;

    NeoForgeBeerMugFluidHandler(ItemStack container) {
        this.container = container;
        this.beer = FluidRegistry.byFilledMug(container.getItem()).orElse(null);
    }

    @Override
    public ItemStack getContainer() {
        return container;
    }

    @Override
    public int getTanks() {
        return 1;
    }

    @Override
    public FluidStack getFluidInTank(int tank) {
        if (tank != 0 || beer == null) {
            return FluidStack.EMPTY;
        }
        return new FluidStack(beer.source(), FluidRegistry.SERVING_MILLIBUCKETS);
    }

    @Override
    public int getTankCapacity(int tank) {
        return tank == 0 ? FluidRegistry.SERVING_MILLIBUCKETS : 0;
    }

    @Override
    public boolean isFluidValid(int tank, FluidStack stack) {
        if (tank != 0 || stack.isEmpty()) {
            return false;
        }
        return beer == null
                ? FluidRegistry.byFluid(stack.getFluid()).isPresent()
                : beer.matches(stack.getFluid());
    }

    @Override
    public int fill(FluidStack resource, FluidAction action) {
        if (container.getCount() != 1 || beer != null || resource.isEmpty()
                || resource.getAmount() < FluidRegistry.SERVING_MILLIBUCKETS) {
            return 0;
        }

        FluidRegistry.BeerFluid inserted = FluidRegistry.byFluid(resource.getFluid()).orElse(null);
        if (inserted == null) {
            return 0;
        }

        if (action.execute()) {
            replaceContainer(inserted.filledMug());
            beer = inserted;
        }
        return FluidRegistry.SERVING_MILLIBUCKETS;
    }

    @Override
    public FluidStack drain(FluidStack resource, FluidAction action) {
        if (resource.isEmpty() || beer == null) {
            return FluidStack.EMPTY;
        }
        FluidStack contained = getFluidInTank(0);
        if (!FluidStack.isSameFluidSameComponents(resource, contained)) {
            return FluidStack.EMPTY;
        }
        return drain(resource.getAmount(), action);
    }

    @Override
    public FluidStack drain(int maxDrain, FluidAction action) {
        if (container.getCount() != 1 || beer == null || maxDrain < FluidRegistry.SERVING_MILLIBUCKETS) {
            return FluidStack.EMPTY;
        }

        FluidStack drained = new FluidStack(beer.source(), FluidRegistry.SERVING_MILLIBUCKETS);
        if (action.execute()) {
            replaceContainer(ItemRegistry.EMPTY_BEER_MUG.get());
            beer = null;
        }
        return drained;
    }

    private void replaceContainer(Item item) {
        ItemStack replacement = new ItemStack(item);
        replacement.applyComponents(container.getComponentsPatch());
        container = replacement;
    }
}
