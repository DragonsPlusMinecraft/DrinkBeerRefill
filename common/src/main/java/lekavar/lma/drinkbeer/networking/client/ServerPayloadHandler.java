package lekavar.lma.drinkbeer.networking.client;

import lekavar.lma.drinkbeer.blockentities.TradeBoxBlockEntity;
import lekavar.lma.drinkbeer.gui.TradeBoxMenu;
import lekavar.lma.drinkbeer.networking.RefreshTradeBoxPayload;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

public class ServerPayloadHandler {

    public static void handlePayload(final RefreshTradeBoxPayload data, final Player contextPlayer) {
        if (!(contextPlayer instanceof ServerPlayer player)) {
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
