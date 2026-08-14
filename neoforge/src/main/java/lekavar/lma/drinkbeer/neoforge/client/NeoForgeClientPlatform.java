package lekavar.lma.drinkbeer.neoforge.client;

import lekavar.lma.drinkbeer.client.renderers.BartendingTableBlockEntityRenderer;
import lekavar.lma.drinkbeer.client.renderers.MixedBeerBlockEntityRenderer;
import lekavar.lma.drinkbeer.gui.BeerBarrelScreen;
import lekavar.lma.drinkbeer.gui.TradeBoxScreen;
import lekavar.lma.drinkbeer.managers.MixedBeerManager;
import lekavar.lma.drinkbeer.platform.ClientPlatformHooks;
import lekavar.lma.drinkbeer.registries.BlockEntityRegistry;
import lekavar.lma.drinkbeer.registries.FluidRegistry;
import lekavar.lma.drinkbeer.registries.ItemRegistry;
import lekavar.lma.drinkbeer.registries.MenuTypeRegistry;
import lekavar.lma.drinkbeer.registries.ParticleTypeRegistry;
import net.minecraft.client.particle.FlameParticle;
import net.minecraft.client.particle.HeartParticle;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.client.event.RegisterParticleProvidersEvent;

public final class NeoForgeClientPlatform implements ClientPlatformHooks {
    private final IEventBus modEventBus;

    NeoForgeClientPlatform(IEventBus modEventBus) {
        this.modEventBus = modEventBus;
    }

    @Override
    public void initializeClient() {
        modEventBus.addListener(CreateMilkTextureCompat::addPackFinders);
        modEventBus.addListener(this::registerFluidRenderers);
        modEventBus.addListener(this::registerRenderers);
        modEventBus.addListener(this::registerParticles);
        modEventBus.addListener(this::registerScreens);
        modEventBus.addListener(this::clientSetup);
    }

    private void registerFluidRenderers(RegisterClientExtensionsEvent event) {
        ResourceLocation waterStill = ResourceLocation.withDefaultNamespace("block/water_still");
        ResourceLocation waterFlowing = ResourceLocation.withDefaultNamespace("block/water_flow");
        ResourceLocation milkStill = ResourceLocation.fromNamespaceAndPath("create", "fluid/milk_still");
        ResourceLocation milkFlowing = ResourceLocation.fromNamespaceAndPath("create", "fluid/milk_flow");
        boolean useCreateMilkTextures = CreateMilkTextureCompat.canUseCreateMilkTextures();

        for (FluidRegistry.BeerFluid beer : FluidRegistry.beers()) {
            boolean milky = useCreateMilkTextures && beer.appearance() == FluidRegistry.Appearance.MILKY;
            event.registerFluidType(new IClientFluidTypeExtensions() {
                @Override
                public ResourceLocation getStillTexture() {
                    return milky ? milkStill : waterStill;
                }

                @Override
                public ResourceLocation getFlowingTexture() {
                    return milky ? milkFlowing : waterFlowing;
                }

                @Override
                public int getTintColor() {
                    return beer.tintColor();
                }
            }, beer.source().getFluidType());
        }
    }

    private void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(
                BlockEntityRegistry.MIXED_BEER_TILEENTITY.get(),
                MixedBeerBlockEntityRenderer::new
        );
        event.registerBlockEntityRenderer(
                BlockEntityRegistry.BARTENDING_TABLE_TILEENTITY.get(),
                BartendingTableBlockEntityRenderer::new
        );
    }

    private void registerParticles(RegisterParticleProvidersEvent event) {
        event.registerSpriteSet(ParticleTypeRegistry.MIXED_BEER_DEFAULT.get(), FlameParticle.Provider::new);
        event.registerSpriteSet(
                ParticleTypeRegistry.CALL_BELL_TINKLE_PAW.get(),
                HeartParticle.AngryVillagerProvider::new
        );
    }

    private void registerScreens(RegisterMenuScreensEvent event) {
        event.register(MenuTypeRegistry.beerBarrelContainer.get(), BeerBarrelScreen::new);
        event.register(MenuTypeRegistry.tradeBoxContainer.get(), TradeBoxScreen::new);
    }

    private void clientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> ItemProperties.register(
                ItemRegistry.MIXED_BEER.get(),
                ResourceLocation.withDefaultNamespace("beer_id"),
                (stack, level, living, seed) -> MixedBeerManager.getBeerId(stack) / 100.0F
        ));
    }
}
