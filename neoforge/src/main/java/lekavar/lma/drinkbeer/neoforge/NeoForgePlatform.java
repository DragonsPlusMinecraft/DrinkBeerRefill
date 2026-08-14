package lekavar.lma.drinkbeer.neoforge;

import lekavar.lma.drinkbeer.DrinkBeer;
import lekavar.lma.drinkbeer.networking.RefreshTradeBoxPayload;
import lekavar.lma.drinkbeer.networking.client.ServerPayloadHandler;
import lekavar.lma.drinkbeer.platform.BlockEntityFactory;
import lekavar.lma.drinkbeer.platform.ExtendedMenuFactory;
import lekavar.lma.drinkbeer.platform.PlatformHooks;
import lekavar.lma.drinkbeer.platform.PlatformHooks.FluidPair;
import lekavar.lma.drinkbeer.platform.RegistryHandle;
import lekavar.lma.drinkbeer.registries.BlockEntityRegistry;
import lekavar.lma.drinkbeer.registries.FluidRegistry;
import lekavar.lma.drinkbeer.registries.ItemRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.material.FlowingFluid;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.common.SoundActions;
import net.neoforged.neoforge.fluids.BaseFlowingFluid;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.IdentityHashMap;
import java.util.Map;
import java.util.function.Supplier;

public final class NeoForgePlatform implements PlatformHooks {
    private final IEventBus modEventBus;
    private final ModContainer modContainer;
    private final Map<Registry<?>, DeferredRegister<?>> registrars = new IdentityHashMap<>();

    NeoForgePlatform(IEventBus modEventBus, ModContainer modContainer) {
        this.modEventBus = modEventBus;
        this.modContainer = modContainer;
    }

    @Override
    public <T, R extends T> RegistryHandle<R> register(
            Registry<T> registry,
            String path,
            Supplier<? extends R> factory
    ) {
        DeferredHolder<T, R> holder = registrar(registry).register(path, factory);
        return new NeoForgeRegistryHandle<>(holder);
    }

    @SuppressWarnings("unchecked")
    private <T> DeferredRegister<T> registrar(Registry<T> registry) {
        DeferredRegister<?> existing = registrars.get(registry);
        if (existing != null) {
            return (DeferredRegister<T>) existing;
        }
        DeferredRegister<T> created = DeferredRegister.create(registry, DrinkBeer.MOD_ID);
        created.register(modEventBus);
        registrars.put(registry, created);
        return created;
    }

    @Override
    public <T extends AbstractContainerMenu> RegistryHandle<MenuType<T>> registerMenu(
            String path,
            ExtendedMenuFactory<T> factory
    ) {
        return register(BuiltInRegistries.MENU, path, () -> IMenuTypeExtension.create(
                (containerId, inventory, data) -> factory.create(containerId, inventory, data.readBlockPos())
        ));
    }

    @Override
    @SafeVarargs
    public final <T extends BlockEntity> RegistryHandle<BlockEntityType<T>> registerBlockEntityType(
            String path,
            BlockEntityFactory<T> factory,
            Supplier<? extends Block>... validBlocks
    ) {
        return register(BuiltInRegistries.BLOCK_ENTITY_TYPE, path, () -> {
            Block[] blocks = new Block[validBlocks.length];
            for (int i = 0; i < validBlocks.length; i++) {
                blocks[i] = validBlocks[i].get();
            }
            return BlockEntityType.Builder.of(factory::create, blocks).build(null);
        });
    }

    @Override
    public FluidPair registerFluidPair(String path) {
        RegistryHandle<FluidType> type = register(NeoForgeRegistries.FLUID_TYPES, path, () ->
                new FluidType(FluidType.Properties.create()
                        .sound(SoundActions.BUCKET_FILL, SoundEvents.BUCKET_FILL)
                        .sound(SoundActions.BUCKET_EMPTY, SoundEvents.BUCKET_EMPTY)
                        .sound(SoundActions.FLUID_VAPORIZE, SoundEvents.FIRE_EXTINGUISH))
        );

        FluidPair[] pair = new FluidPair[1];
        BaseFlowingFluid.Properties properties = new BaseFlowingFluid.Properties(
                type,
                () -> pair[0].source().get(),
                () -> pair[0].flowing().get()
        );
        RegistryHandle<? extends FlowingFluid> source = register(
                BuiltInRegistries.FLUID,
                path,
                () -> new BaseFlowingFluid.Source(properties)
        );
        RegistryHandle<? extends FlowingFluid> flowing = register(
                BuiltInRegistries.FLUID,
                "flowing_" + path,
                () -> new BaseFlowingFluid.Flowing(properties)
        );
        pair[0] = new FluidPair(source, flowing);
        return pair[0];
    }

    @Override
    public void registerNetworking() {
        modEventBus.addListener(this::registerPayloads);
        modEventBus.addListener(this::registerCapabilities);
    }

    private void registerPayloads(RegisterPayloadHandlersEvent event) {
        event.registrar("1").playToServer(
                RefreshTradeBoxPayload.TYPE,
                RefreshTradeBoxPayload.STREAM_CODEC,
                (payload, context) -> ServerPayloadHandler.handlePayload(payload, context.player())
        );
    }

    private void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(
                Capabilities.ItemHandler.BLOCK,
                BlockEntityRegistry.BARTENDING_TABLE_TILEENTITY.get(),
                (blockEntity, direction) -> new NeoForgeItemHandlerAdapter(blockEntity.getItemHandler(direction))
        );
        event.registerBlockEntity(
                Capabilities.ItemHandler.BLOCK,
                BlockEntityRegistry.BEER_BARREL_TILEENTITY.get(),
                (blockEntity, direction) -> new NeoForgeItemHandlerAdapter(blockEntity.getItemHandler(direction))
        );

        event.registerItem(
                Capabilities.FluidHandler.ITEM,
                (stack, ignored) -> new NeoForgeBeerMugFluidHandler(stack),
                ItemRegistry.EMPTY_BEER_MUG.get()
        );
        event.registerItem(
                Capabilities.FluidHandler.ITEM,
                (stack, ignored) -> new NeoForgeBeerMugFluidHandler(stack),
                FluidRegistry.beers().stream().map(FluidRegistry.BeerFluid::filledMug).toArray(Item[]::new)
        );
    }

    @Override
    public void registerConfig() {
        DrinkBeerConfigNeoForge.install();
        modContainer.registerConfig(ModConfig.Type.SERVER, DrinkBeerConfigNeoForge.SPEC, "drinkbeer-server.toml");
    }

    @Override
    public void openMenu(Player player, MenuProvider provider, BlockPos pos) {
        if (player instanceof ServerPlayer serverPlayer) {
            serverPlayer.openMenu(provider, data -> data.writeBlockPos(pos));
        }
    }

    @Override
    public void sendRefreshTradeBox(BlockPos pos) {
        PacketDistributor.sendToServer(new RefreshTradeBoxPayload(pos));
    }
}
