package lekavar.lma.drinkbeer.neoforge;

import lekavar.lma.drinkbeer.DrinkBeer;
import lekavar.lma.drinkbeer.networking.RefreshTradeBoxPayload;
import lekavar.lma.drinkbeer.networking.client.ServerPayloadHandler;
import lekavar.lma.drinkbeer.platform.BlockEntityFactory;
import lekavar.lma.drinkbeer.platform.ExtendedMenuFactory;
import lekavar.lma.drinkbeer.platform.PlatformHooks;
import lekavar.lma.drinkbeer.platform.RegistryHandle;
import lekavar.lma.drinkbeer.registries.BlockEntityRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.IdentityHashMap;
import java.util.Map;
import java.util.function.Supplier;
import java.util.function.Function;

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
            Function<ResourceKey<T>, ? extends R> factory
    ) {
        DeferredHolder<T, R> holder = registrar(registry).register(path, id ->
                factory.apply(ResourceKey.create(registry.key(), id))
        );
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
        return register(BuiltInRegistries.MENU, path, ignored -> IMenuTypeExtension.create(
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
        return register(BuiltInRegistries.BLOCK_ENTITY_TYPE, path, ignored -> {
            Block[] blocks = new Block[validBlocks.length];
            for (int i = 0; i < validBlocks.length; i++) {
                blocks[i] = validBlocks[i].get();
            }
            return new BlockEntityType<>(factory::create, blocks);
        });
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
                Capabilities.Item.BLOCK,
                BlockEntityRegistry.BARTENDING_TABLE_TILEENTITY.get(),
                (blockEntity, direction) -> new NeoForgeItemHandlerAdapter(blockEntity.getItemHandler(direction))
        );
        event.registerBlockEntity(
                Capabilities.Item.BLOCK,
                BlockEntityRegistry.BEER_BARREL_TILEENTITY.get(),
                (blockEntity, direction) -> new NeoForgeItemHandlerAdapter(blockEntity.getItemHandler(direction))
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
        ClientPacketDistributor.sendToServer(new RefreshTradeBoxPayload(pos));
    }
}
