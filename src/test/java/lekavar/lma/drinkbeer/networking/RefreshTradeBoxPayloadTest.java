package lekavar.lma.drinkbeer.networking;

import io.netty.buffer.Unpooled;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RefreshTradeBoxPayloadTest {
    @Test
    void positionRoundTripsThroughTheForgeEraPacketCodec() {
        FriendlyByteBuf buffer = new FriendlyByteBuf(Unpooled.buffer());
        try {
            RefreshTradeBoxPayload original = new RefreshTradeBoxPayload(new BlockPos(-12_345, 255, 67_890));
            RefreshTradeBoxPayload.encode(original, buffer);

            assertEquals(original, RefreshTradeBoxPayload.decode(buffer));
            assertEquals(0, buffer.readableBytes());
        } finally {
            buffer.release();
        }
    }
}
