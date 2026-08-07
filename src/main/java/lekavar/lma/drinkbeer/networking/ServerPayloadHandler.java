package lekavar.lma.drinkbeer.networking;

import lekavar.lma.drinkbeer.blockentities.TradeBoxBlockEntity;
import lekavar.lma.drinkbeer.gui.TradeBoxMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public final class ServerPayloadHandler {
    public static void handle(RefreshTradeBoxPayload payload, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player == null) {
                return;
            }

            BlockPos pos = payload.pos();
            if (!isValidRequest(player, pos)) {
                return;
            }

            TradeBoxMenu menu = (TradeBoxMenu) player.containerMenu;
            TradeBoxBlockEntity tradeBox = (TradeBoxBlockEntity) player.level().getBlockEntity(pos);
            menu.setTradeboxCooling();
            tradeBox.setChanged();
        });
        context.setPacketHandled(true);
    }

    static boolean isValidRequest(ServerPlayer player, BlockPos pos) {
        return player.containerMenu instanceof TradeBoxMenu menu
                && menu.isBoundTo(pos)
                && player.level().hasChunkAt(pos)
                && menu.stillValid(player)
                && menu.isTrading()
                && player.level().getBlockEntity(pos) instanceof TradeBoxBlockEntity tradeBox
                && tradeBox.getBlockPos().equals(pos);
    }

    private ServerPayloadHandler() {
    }
}
