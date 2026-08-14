package lekavar.lma.drinkbeer.neoforge.compat.create.jei.mixin;

import lekavar.lma.drinkbeer.neoforge.compat.create.jei.CreateJeiFillAmountCompat;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.IFluidHandlerItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/** Limits the patch to Create's dynamically generated JEI spout recipes. */
@Pseudo
@Mixin(targets = "com.simibubi.create.compat.jei.category.SpoutCategory", remap = false)
abstract class SpoutCategoryMixin {
    @Redirect(
            method = "consumeRecipes",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/neoforged/neoforge/fluids/capability/IFluidHandlerItem;fill(Lnet/neoforged/neoforge/fluids/FluidStack;Lnet/neoforged/neoforge/fluids/capability/IFluidHandler$FluidAction;)I",
                    remap = false
            ),
            require = 0,
            remap = false
    )
    private static int drinkbeer$useActualFillAmount(
            IFluidHandlerItem handler,
            FluidStack resource,
            IFluidHandler.FluidAction action
    ) {
        return CreateJeiFillAmountCompat.fillForDisplay(handler, resource, action);
    }
}
