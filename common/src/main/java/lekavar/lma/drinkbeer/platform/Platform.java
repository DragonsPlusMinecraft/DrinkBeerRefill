package lekavar.lma.drinkbeer.platform;

import java.util.Objects;

public final class Platform {
    private static PlatformHooks hooks;

    public static synchronized void install(PlatformHooks platformHooks) {
        Objects.requireNonNull(platformHooks, "platformHooks");
        if (hooks != null) {
            throw new IllegalStateException("Drink Beer platform hooks are already installed");
        }
        hooks = platformHooks;
    }

    public static PlatformHooks hooks() {
        PlatformHooks current = hooks;
        if (current == null) {
            throw new IllegalStateException("Drink Beer platform hooks have not been installed");
        }
        return current;
    }

    private Platform() {
    }
}
