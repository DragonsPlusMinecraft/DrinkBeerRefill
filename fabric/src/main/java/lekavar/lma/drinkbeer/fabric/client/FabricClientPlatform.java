package lekavar.lma.drinkbeer.fabric.client;

import lekavar.lma.drinkbeer.client.renderers.BartendingTableBlockEntityRenderer;
import lekavar.lma.drinkbeer.client.renderers.MixedBeerBlockEntityRenderer;
import lekavar.lma.drinkbeer.compat.jei.JeiRecipeSource;
import lekavar.lma.drinkbeer.fabric.FabricPlatform;
import lekavar.lma.drinkbeer.gui.BeerBarrelScreen;
import lekavar.lma.drinkbeer.gui.TradeBoxScreen;
import lekavar.lma.drinkbeer.networking.RefreshTradeBoxPayload;
import lekavar.lma.drinkbeer.platform.ClientPlatformHooks;
import lekavar.lma.drinkbeer.registries.BlockEntityRegistry;
import lekavar.lma.drinkbeer.registries.MenuTypeRegistry;
import lekavar.lma.drinkbeer.registries.ParticleTypeRegistry;
import lekavar.lma.drinkbeer.registries.RecipeRegistry;
import mezz.jei.common.Internal;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.particle.v1.ParticleProviderRegistry;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.particle.FlameParticle;
import net.minecraft.client.particle.HeartParticle;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.crafting.RecipeHolder;

public final class FabricClientPlatform implements ClientPlatformHooks {
    @Override
    public void initializeClient() {
        JeiRecipeSource.install(() -> Internal.getClientSyncedRecipes()
                .byType(RecipeRegistry.RECIPE_TYPE_BREWING.get())
                .stream()
                .map(RecipeHolder::value)
                .toList());

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

        ParticleProviderRegistry.getInstance().register(
                (SimpleParticleType) ParticleTypeRegistry.MIXED_BEER_DEFAULT.get(),
                FlameParticle.Provider::new
        );
        ParticleProviderRegistry.getInstance().register(
                (SimpleParticleType) ParticleTypeRegistry.CALL_BELL_TINKLE_PAW.get(),
                HeartParticle.AngryVillagerProvider::new
        );

        FabricPlatform.installClientPacketSender(pos ->
                ClientPlayNetworking.send(new RefreshTradeBoxPayload(pos))
        );
    }
}
