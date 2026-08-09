package lekavar.lma.drinkbeer.neoforge.client;

import lekavar.lma.drinkbeer.client.renderers.BartendingTableBlockEntityRenderer;
import lekavar.lma.drinkbeer.client.renderers.MixedBeerBlockEntityRenderer;
import lekavar.lma.drinkbeer.compat.jei.JeiRecipeSource;
import lekavar.lma.drinkbeer.gui.BeerBarrelScreen;
import lekavar.lma.drinkbeer.gui.TradeBoxScreen;
import lekavar.lma.drinkbeer.platform.ClientPlatformHooks;
import lekavar.lma.drinkbeer.registries.BlockEntityRegistry;
import lekavar.lma.drinkbeer.registries.MenuTypeRegistry;
import lekavar.lma.drinkbeer.registries.ParticleTypeRegistry;
import lekavar.lma.drinkbeer.registries.RecipeRegistry;
import mezz.jei.common.Internal;
import net.minecraft.client.particle.FlameParticle;
import net.minecraft.client.particle.HeartParticle;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.client.event.RegisterParticleProvidersEvent;

public final class NeoForgeClientPlatform implements ClientPlatformHooks {
    private final IEventBus modEventBus;

    NeoForgeClientPlatform(IEventBus modEventBus) {
        this.modEventBus = modEventBus;
    }

    @Override
    public void initializeClient() {
        JeiRecipeSource.install(() -> Internal.getClientSyncedRecipes()
                .byType(RecipeRegistry.RECIPE_TYPE_BREWING.get())
                .stream()
                .map(RecipeHolder::value)
                .toList());

        modEventBus.addListener(this::registerRenderers);
        modEventBus.addListener(this::registerParticles);
        modEventBus.addListener(this::registerScreens);
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

}
