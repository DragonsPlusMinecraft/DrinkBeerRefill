package lekavar.lma.drinkbeer.networking;

import lekavar.lma.drinkbeer.networking.client.ServerPayloadHandler;
import net.minecraft.core.BlockPos;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.MainThreadPayloadHandler;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

public class NetWorking {

    public static void init(RegisterPayloadHandlersEvent event) {
        // Sets the current network version
        final PayloadRegistrar registrar = event.registrar("1");
        registrar.playToServer(
                RefreshTradeBoxPayload.TYPE,
                RefreshTradeBoxPayload.STREAM_CODEC,
                new MainThreadPayloadHandler<>(
                        ServerPayloadHandler::handlePayload
                )
        );
    }

    public static void sendRefreshTradebox(BlockPos pos) {
        PacketDistributor.sendToServer(
                new RefreshTradeBoxPayload(pos)
        );
    }
}
