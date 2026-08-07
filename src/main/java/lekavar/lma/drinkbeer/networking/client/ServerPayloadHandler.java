package lekavar.lma.drinkbeer.networking.client;

import lekavar.lma.drinkbeer.blockentities.TradeBoxBlockEntity;
import lekavar.lma.drinkbeer.gui.TradeBoxMenu;
import lekavar.lma.drinkbeer.networking.RefreshTradeBoxPayload;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class ServerPayloadHandler {

    public static void handlePayload(final RefreshTradeBoxPayload data, final IPayloadContext ctx) {
        if (!(ctx.player() instanceof ServerPlayer player)) {
            return;
        }
        BlockPos pos = data.pos();
        if (!(player.containerMenu instanceof TradeBoxMenu menu)
                || !menu.isBoundTo(pos)
                || !menu.stillValid(player)
                || !menu.isTrading()
                || !player.level().isLoaded(pos)
                || !(player.level().getBlockEntity(pos) instanceof TradeBoxBlockEntity tradeboxEntity)) {
            return;
        }
        menu.setTradeboxCooling();
        tradeboxEntity.setChanged();
    }
}
