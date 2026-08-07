package lekavar.lma.drinkbeer;

import lekavar.lma.drinkbeer.client.renderers.BartendingTableBlockEntityRenderer;
import lekavar.lma.drinkbeer.client.renderers.MixedBeerBlockEntityRenderer;
import lekavar.lma.drinkbeer.gui.BeerBarrelScreen;
import lekavar.lma.drinkbeer.gui.TradeBoxScreen;
import lekavar.lma.drinkbeer.managers.MixedBeerManager;
import lekavar.lma.drinkbeer.registries.BlockEntityRegistry;
import lekavar.lma.drinkbeer.registries.BlockRegistry;
import lekavar.lma.drinkbeer.registries.ItemRegistry;
import lekavar.lma.drinkbeer.registries.MenuTypeRegistry;
import lekavar.lma.drinkbeer.registries.ParticleTypeRegistry;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.particle.FlameParticle;
import net.minecraft.client.particle.HeartParticle;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.RegisterParticleProvidersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

@Mod.EventBusSubscriber(modid = DrinkBeer.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class DrinkBeerClient {

    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(BlockEntityRegistry.MIXED_BEER_TILEENTITY.get(), MixedBeerBlockEntityRenderer::new);
        event.registerBlockEntityRenderer(BlockEntityRegistry.BARTENDING_TABLE_TILEENTITY.get(), BartendingTableBlockEntityRenderer::new);
    }

    @SubscribeEvent
    public static void registerParticleProviders(RegisterParticleProvidersEvent event) {
        event.registerSpriteSet(ParticleTypeRegistry.MIXED_BEER_DEFAULT.get(), FlameParticle.Provider::new);
        event.registerSpriteSet(ParticleTypeRegistry.CALL_BELL_TINKLE_PAW.get(), HeartParticle.AngryVillagerProvider::new);
    }

    @SubscribeEvent
    public static void registerScreens(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            MenuScreens.register(MenuTypeRegistry.beerBarrelContainer.get(), BeerBarrelScreen::new);
            MenuScreens.register(MenuTypeRegistry.tradeBoxContainer.get(), TradeBoxScreen::new);
        });
    }

    @SubscribeEvent
    public static void registerItemPropertiesOverride(FMLClientSetupEvent event){
        event.enqueueWork(()->{
            ItemProperties.register(ItemRegistry.MIXED_BEER.get(), new ResourceLocation("minecraft", "beer_id"), (stack, level, living, id)
                    -> MixedBeerManager.getBeerId(stack) / 100.0f);

            RenderType cutout = RenderType.cutout();
            ItemBlockRenderTypes.setRenderLayer(BlockRegistry.EMPTY_BEER_MUG.get(), cutout);
            ItemBlockRenderTypes.setRenderLayer(BlockRegistry.BEER_MUG.get(), cutout);
            ItemBlockRenderTypes.setRenderLayer(BlockRegistry.BEER_MUG_BLAZE_STOUT.get(), cutout);
            ItemBlockRenderTypes.setRenderLayer(BlockRegistry.BEER_MUG_BLAZE_MILK_STOUT.get(), cutout);
            ItemBlockRenderTypes.setRenderLayer(BlockRegistry.BEER_MUG_APPLE_LAMBIC.get(), cutout);
            ItemBlockRenderTypes.setRenderLayer(BlockRegistry.BEER_MUG_SWEET_BERRY_KRIEK.get(), cutout);
            ItemBlockRenderTypes.setRenderLayer(BlockRegistry.BEER_MUG_HAARS_ICEY_PALE_LAGER.get(), cutout);
            ItemBlockRenderTypes.setRenderLayer(BlockRegistry.BEER_MUG_PUMPKIN_KVASS.get(), cutout);
            ItemBlockRenderTypes.setRenderLayer(BlockRegistry.BEER_MUG_NIGHT_HOWL_KVASS.get(), cutout);
            ItemBlockRenderTypes.setRenderLayer(BlockRegistry.BEER_MUG_FROTHY_PINK_EGGNOG.get(), cutout);
            ItemBlockRenderTypes.setRenderLayer(BlockRegistry.MIXED_BEER.get(), cutout);
            ItemBlockRenderTypes.setRenderLayer(BlockRegistry.COLORED_LIGHTS.get(), cutout);
            ItemBlockRenderTypes.setRenderLayer(BlockRegistry.SIDE_COLORED_LIGHTS.get(), cutout);
            ItemBlockRenderTypes.setRenderLayer(BlockRegistry.STAR_OF_BETHLEHEM.get(), cutout);
            ItemBlockRenderTypes.setRenderLayer(BlockRegistry.THE_GREAT_STAR_OF_BETHLEHEM.get(), cutout);
            ItemBlockRenderTypes.setRenderLayer(BlockRegistry.HORSE_MODEL_1.get(), cutout);
            ItemBlockRenderTypes.setRenderLayer(BlockRegistry.HORSE_MODEL_2.get(), cutout);
            ItemBlockRenderTypes.setRenderLayer(BlockRegistry.HORSE_MODEL_3.get(), cutout);
        });
    }

    private DrinkBeerClient() {
    }

}
