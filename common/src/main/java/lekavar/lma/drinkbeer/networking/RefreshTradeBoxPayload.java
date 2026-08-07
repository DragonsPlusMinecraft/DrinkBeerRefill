package lekavar.lma.drinkbeer.networking;

import io.netty.buffer.ByteBuf;
import lekavar.lma.drinkbeer.DrinkBeer;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record RefreshTradeBoxPayload(BlockPos pos) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<RefreshTradeBoxPayload> TYPE = new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(DrinkBeer.MOD_ID, "refreash_tradebox"));

    public static final StreamCodec<ByteBuf, RefreshTradeBoxPayload> STREAM_CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC,
            RefreshTradeBoxPayload::pos,
            RefreshTradeBoxPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
