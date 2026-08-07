package lekavar.lma.drinkbeer.fabric;

import lekavar.lma.drinkbeer.DrinkBeer;
import lekavar.lma.drinkbeer.blockentities.BartendingTableBlockEntity;
import lekavar.lma.drinkbeer.blockentities.BeerBarrelBlockEntity;
import lekavar.lma.drinkbeer.networking.RefreshTradeBoxPayload;
import lekavar.lma.drinkbeer.networking.client.ServerPayloadHandler;
import lekavar.lma.drinkbeer.platform.BlockEntityFactory;
import lekavar.lma.drinkbeer.platform.ExtendedMenuFactory;
import lekavar.lma.drinkbeer.platform.PlatformHooks;
import lekavar.lma.drinkbeer.platform.RegistryHandle;
import lekavar.lma.drinkbeer.registries.BlockEntityRegistry;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerType;
import net.fabricmc.fabric.api.transfer.v1.item.ItemStorage;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;

import java.util.function.Consumer;
import java.util.function.Supplier;

public final class FabricPlatform implements PlatformHooks {
    private static Consumer<BlockPos> clientPacketSender = pos ->
            DrinkBeer.LOGGER.warn("Ignoring client packet send before the Fabric client initializer is ready");

    @Override
    public <T, R extends T> RegistryHandle<R> register(
            Registry<T> registry,
            String path,
            Supplier<? extends R> factory
    ) {
        ResourceLocation id = ResourceLocation.fromNamespaceAndPath(DrinkBeer.MOD_ID, path);
        R value = factory.get();
        Registry.register(registry, id, value);
        @SuppressWarnings("unchecked")
        Holder<R> holder = (Holder<R>) (Holder<?>) registry.wrapAsHolder(value);
        return new FabricRegistryHandle<>(id, value, holder);
    }

    @Override
    public <T extends AbstractContainerMenu> RegistryHandle<MenuType<T>> registerMenu(
            String path,
            ExtendedMenuFactory<T> factory
    ) {
        return register(BuiltInRegistries.MENU, path, () -> new ExtendedScreenHandlerType<>(
                (containerId, inventory, pos) -> factory.create(containerId, inventory, pos),
                BlockPos.STREAM_CODEC
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
    public void registerNetworking() {
        PayloadTypeRegistry.playC2S().register(RefreshTradeBoxPayload.TYPE, RefreshTradeBoxPayload.STREAM_CODEC);
        ServerPlayNetworking.registerGlobalReceiver(
                RefreshTradeBoxPayload.TYPE,
                (payload, context) -> ServerPayloadHandler.handlePayload(payload, context.player())
        );

        ItemStorage.SIDED.registerForBlockEntity(
                (blockEntity, direction) -> new FabricItemStorageAdapter(blockEntity.getItemHandler(direction)),
                BlockEntityRegistry.BARTENDING_TABLE_TILEENTITY.get()
        );
        ItemStorage.SIDED.registerForBlockEntity(
                (blockEntity, direction) -> new FabricItemStorageAdapter(blockEntity.getItemHandler(direction)),
                BlockEntityRegistry.BEER_BARREL_TILEENTITY.get()
        );
    }

    @Override
    public void registerConfig() {
        FabricServerConfig.register();
    }

    @Override
    public void openMenu(Player player, MenuProvider provider, BlockPos pos) {
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return;
        }
        serverPlayer.openMenu(new ExtendedScreenHandlerFactory<BlockPos>() {
            @Override
            public BlockPos getScreenOpeningData(ServerPlayer ignored) {
                return pos;
            }

            @Override
            public AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player menuPlayer) {
                return provider.createMenu(containerId, inventory, menuPlayer);
            }

            @Override
            public net.minecraft.network.chat.Component getDisplayName() {
                return provider.getDisplayName();
            }
        });
    }

    @Override
    public void sendRefreshTradeBox(BlockPos pos) {
        clientPacketSender.accept(pos);
    }

    public static void installClientPacketSender(Consumer<BlockPos> sender) {
        clientPacketSender = sender;
    }
}
