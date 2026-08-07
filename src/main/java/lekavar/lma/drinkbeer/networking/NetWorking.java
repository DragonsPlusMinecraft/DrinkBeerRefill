package lekavar.lma.drinkbeer.networking;

import lekavar.lma.drinkbeer.DrinkBeer;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

import java.util.Optional;

public final class NetWorking {
    private static final String PROTOCOL_VERSION = "1";
    private static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(DrinkBeer.MOD_ID, "main"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );
    private static boolean initialized;

    public static synchronized void init() {
        if (initialized) {
            return;
        }
        initialized = true;
        CHANNEL.registerMessage(
                0,
                RefreshTradeBoxPayload.class,
                RefreshTradeBoxPayload::encode,
                RefreshTradeBoxPayload::decode,
                ServerPayloadHandler::handle,
                Optional.of(NetworkDirection.PLAY_TO_SERVER)
        );
    }

    public static void sendRefreshTradebox(BlockPos pos) {
        CHANNEL.sendToServer(new RefreshTradeBoxPayload(pos));
    }

    private NetWorking() {
    }
}
