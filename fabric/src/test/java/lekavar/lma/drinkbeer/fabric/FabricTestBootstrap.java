package lekavar.lma.drinkbeer.fabric;

import net.fabricmc.loader.impl.launch.FabricLauncherBase;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;

/**
 * Starts the minimal Minecraft and mod lifecycle required by the shared unit
 * tests. Fabric Loader JUnit supplies the transformed Knot class path, but it
 * intentionally does not invoke Minecraft's main method or mod entrypoints.
 */
public final class FabricTestBootstrap implements BeforeAllCallback {
    private static boolean initialized;

    @Override
    public void beforeAll(ExtensionContext context) throws Exception {
        synchronized (FabricTestBootstrap.class) {
            if (initialized) {
                return;
            }

            ClassLoader knotLoader = FabricLauncherBase.getLauncher().getTargetClassLoader();
            Class<?> sharedConstants = Class.forName("net.minecraft.SharedConstants", true, knotLoader);
            Class<?> bootstrap = Class.forName("net.minecraft.server.Bootstrap", true, knotLoader);
            sharedConstants.getMethod("tryDetectVersion").invoke(null);

            // Loading a vanilla registry checks this flag, while bootStrap()
            // freezes registries before Fabric's entrypoint could populate
            // them. Mirror the normal Fabric launch ordering for this small
            // headless test harness, then let vanilla finish and freeze them.
            setBootstrapFlag(bootstrap, true);
            try {
                Class<?> entrypoint = Class.forName(
                        "lekavar.lma.drinkbeer.fabric.FabricDrinkBeer", true, knotLoader);
                Object initializer = entrypoint.getDeclaredConstructor().newInstance();
                entrypoint.getMethod("onInitialize").invoke(initializer);
            } finally {
                setBootstrapFlag(bootstrap, false);
            }
            Method bootStrap = bootstrap.getMethod("bootStrap");
            bootStrap.invoke(null);
            bindDataComponents(knotLoader);
            initialized = true;
        }
    }

    /**
     * Since 26.1, item defaults are bound during resource loading rather than
     * registry construction. Loader-backed unit tests do not start a resource
     * reload, so reproduce that lifecycle step with the vanilla registry
     * lookup after both vanilla and mod entries have been registered.
     */
    private static void bindDataComponents(ClassLoader knotLoader) throws ReflectiveOperationException {
        Class<?> vanillaRegistries = Class.forName(
                "net.minecraft.data.registries.VanillaRegistries", true, knotLoader);
        Object lookup = vanillaRegistries.getMethod("createLookup").invoke(null);
        Class<?> provider = Class.forName("net.minecraft.core.HolderLookup$Provider", true, knotLoader);
        Class<?> builtInRegistries = Class.forName(
                "net.minecraft.core.registries.BuiltInRegistries", true, knotLoader);
        Object initializers = builtInRegistries.getField("DATA_COMPONENT_INITIALIZERS").get(null);
        List<?> pending = (List<?>) initializers.getClass().getMethod("build", provider)
                .invoke(initializers, lookup);
        Class<?> pendingComponents = Class.forName(
                "net.minecraft.core.component.DataComponentInitializers$PendingComponents", true, knotLoader);
        Method apply = pendingComponents.getMethod("apply");
        for (Object components : pending) {
            apply.invoke(components);
        }
    }

    private static void setBootstrapFlag(Class<?> bootstrap, boolean value) throws ReflectiveOperationException {
        Field field = bootstrap.getDeclaredField("isBootstrapped");
        field.setAccessible(true);
        field.setBoolean(null, value);
    }
}
