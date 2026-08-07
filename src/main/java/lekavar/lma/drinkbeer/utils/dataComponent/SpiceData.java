package lekavar.lma.drinkbeer.utils.dataComponent;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

import java.util.List;

public record SpiceData(int spiceA, int spiceB, int spiceC) {
    public static final int MAX_SPICES = 3;

    public static final Codec<SpiceData> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.INT.fieldOf("spiceA").forGetter(SpiceData::spiceA),
                    Codec.INT.fieldOf("spiceB").forGetter(SpiceData::spiceB),
                    Codec.INT.fieldOf("spiceC").forGetter(SpiceData::spiceC)
            ).apply(instance, SpiceData::new)
    );
    public static final StreamCodec<ByteBuf, SpiceData> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.INT, SpiceData::spiceA,
            ByteBufCodecs.INT, SpiceData::spiceB,
            ByteBufCodecs.INT, SpiceData::spiceC,
            SpiceData::new
    );

    public static SpiceData fromSpiceList(List<Integer> spiceList) {
        return new SpiceData(
                getOrEmpty(spiceList, 0),
                getOrEmpty(spiceList, 1),
                getOrEmpty(spiceList, 2)
        );
    }

    public List<Integer> toSpiceList() {
        return java.util.stream.Stream.of(spiceA, spiceB, spiceC)
                .filter(spiceId -> spiceId > 0)
                .toList();
    }

    private static int getOrEmpty(List<Integer> spiceList, int index) {
        if (spiceList == null || index < 0 || index >= spiceList.size()) {
            return 0;
        }
        Integer spiceId = spiceList.get(index);
        return spiceId == null ? 0 : Math.max(0, spiceId);
    }

}
