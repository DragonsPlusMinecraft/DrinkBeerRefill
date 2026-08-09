package lekavar.lma.drinkbeer;

import net.minecraft.SharedConstants;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.registries.VanillaRegistries;
import net.minecraft.world.item.Items;

/**
 * Supplies the resource-reload component binding omitted by loader-backed
 * headless unit tests on Minecraft 26.1 and later.
 */
public abstract class RegistryComponentTest {
    private static boolean initialized;

    protected RegistryComponentTest() {
        bindDataComponents();
    }

    private static synchronized void bindDataComponents() {
        if (initialized || componentsAreBound()) {
            initialized = true;
            return;
        }
        // NeoForge's IDE-only component validator rejects the direct holder
        // sets intentionally produced by VanillaRegistries' datagen lookup.
        // A real resource reload does not use this synthetic lookup, so disable
        // only that diagnostic while completing this test-only lifecycle step.
        boolean runningInIde = SharedConstants.IS_RUNNING_IN_IDE;
        try {
            SharedConstants.IS_RUNNING_IN_IDE = false;
            BuiltInRegistries.DATA_COMPONENT_INITIALIZERS
                    .build(VanillaRegistries.createLookup())
                    .forEach(components -> components.apply());
        } finally {
            SharedConstants.IS_RUNNING_IN_IDE = runningInIde;
        }
        initialized = true;
    }

    private static boolean componentsAreBound() {
        try {
            Items.AIR.builtInRegistryHolder().components();
            return true;
        } catch (NullPointerException ignored) {
            return false;
        }
    }
}
