package lekavar.lma.drinkbeer.registries;

import com.mojang.serialization.Codec;
import lekavar.lma.drinkbeer.platform.Registration;
import lekavar.lma.drinkbeer.platform.RegistryProvider;
import lekavar.lma.drinkbeer.utils.dataComponent.SpiceData;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.codec.ByteBufCodecs;

import java.util.function.Supplier;

public class DataComponentTypeRegistry {
    private static final RegistryProvider<DataComponentType<?>> DATA_COMPONENTS = Registration.provider(BuiltInRegistries.DATA_COMPONENT_TYPE);

    public static final Supplier<DataComponentType<Integer>> BEER_ID_COMPONENT = DATA_COMPONENTS.register(
            "beer_id", () -> DataComponentType.<Integer>builder()
                    .persistent(Codec.INT)
                    .networkSynchronized(ByteBufCodecs.INT)
                    .build()
    );

    public static final Supplier<DataComponentType<SpiceData>> SPICE_COMPONENT = DATA_COMPONENTS.register(
            "spice", () -> DataComponentType.<SpiceData>builder()
                    .persistent(SpiceData.CODEC)
                    .networkSynchronized(SpiceData.STREAM_CODEC)
                    .build()
    );

    public static void init() {
    }
}
