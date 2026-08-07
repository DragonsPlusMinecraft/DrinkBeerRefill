package lekavar.lma.drinkbeer.networking;

import lekavar.lma.drinkbeer.platform.Platform;
import net.minecraft.core.BlockPos;

public final class NetWorking {
    public static void sendRefreshTradebox(BlockPos pos) {
        Platform.hooks().sendRefreshTradeBox(pos);
    }

    private NetWorking() {
    }
}
