package lekavar.lma.drinkbeer.networking;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;

public record RefreshTradeBoxPayload(BlockPos pos) {
    public static RefreshTradeBoxPayload decode(FriendlyByteBuf buffer) {
        return new RefreshTradeBoxPayload(buffer.readBlockPos());
    }

    public static void encode(RefreshTradeBoxPayload payload, FriendlyByteBuf buffer) {
        buffer.writeBlockPos(payload.pos());
    }
}
