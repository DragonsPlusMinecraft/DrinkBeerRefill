package lekavar.lma.drinkbeer.fabric.client;

import lekavar.lma.drinkbeer.client.renderers.BartendingTableBlockEntityRenderer;
import lekavar.lma.drinkbeer.client.renderers.MixedBeerBlockEntityRenderer;
import lekavar.lma.drinkbeer.fabric.FabricPlatform;
import lekavar.lma.drinkbeer.gui.BeerBarrelScreen;
import lekavar.lma.drinkbeer.gui.TradeBoxScreen;
import lekavar.lma.drinkbeer.managers.MixedBeerManager;
import lekavar.lma.drinkbeer.networking.RefreshTradeBoxPayload;
import lekavar.lma.drinkbeer.platform.ClientPlatformHooks;
import lekavar.lma.drinkbeer.registries.BlockEntityRegistry;
import lekavar.lma.drinkbeer.registries.ItemRegistry;
import lekavar.lma.drinkbeer.registries.MenuTypeRegistry;
import lekavar.lma.drinkbeer.registries.ParticleTypeRegistry;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.particle.v1.ParticleFactoryRegistry;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.particle.FlameParticle;
import net.minecraft.client.particle.HeartParticle;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.resources.ResourceLocation;

public final class FabricClientPlatform implements ClientPlatformHooks {
    @Override
    public void initializeClient() {
        MenuScreens.register(MenuTypeRegistry.beerBarrelContainer.get(), BeerBarrelScreen::new);
        MenuScreens.register(MenuTypeRegistry.tradeBoxContainer.get(), TradeBoxScreen::new);

        BlockEntityRenderers.register(
                BlockEntityRegistry.MIXED_BEER_TILEENTITY.get(),
                MixedBeerBlockEntityRenderer::new
        );
        BlockEntityRenderers.register(
                BlockEntityRegistry.BARTENDING_TABLE_TILEENTITY.get(),
                BartendingTableBlockEntityRenderer::new
        );

        ParticleFactoryRegistry.getInstance().register(
                (SimpleParticleType) ParticleTypeRegistry.MIXED_BEER_DEFAULT.get(),
                FlameParticle.Provider::new
        );
        ParticleFactoryRegistry.getInstance().register(
                (SimpleParticleType) ParticleTypeRegistry.CALL_BELL_TINKLE_PAW.get(),
                HeartParticle.AngryVillagerProvider::new
        );

        ItemProperties.register(
                ItemRegistry.MIXED_BEER.get(),
                ResourceLocation.withDefaultNamespace("beer_id"),
                (stack, level, living, seed) -> MixedBeerManager.getBeerId(stack) / 100.0F
        );

        FabricPlatform.installClientPacketSender(pos ->
                ClientPlayNetworking.send(new RefreshTradeBoxPayload(pos))
        );
    }
}
