package lekavar.lma.drinkbeer.client.renderers;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import lekavar.lma.drinkbeer.blockentities.MixedBeerBlockEntity;
import lekavar.lma.drinkbeer.registries.ItemRegistry;
import lekavar.lma.drinkbeer.utils.beer.Beers;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public class MixedBeerBlockEntityRenderer implements BlockEntityRenderer<MixedBeerBlockEntity, MixedBeerBlockEntityRenderer.RenderState> {
    private final ItemModelResolver itemModelResolver;

    public MixedBeerBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
        this.itemModelResolver = context.itemModelResolver();
    }

    @Override
    public RenderState createRenderState() {
        return new RenderState();
    }

    @Override
    public void extractRenderState(MixedBeerBlockEntity blockEntity, RenderState renderState, float partialTick,
                                   Vec3 cameraPosition, ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress) {
        BlockEntityRenderer.super.extractRenderState(blockEntity, renderState, partialTick, cameraPosition, breakProgress);
        ItemStack beerStack = getBeerStack(blockEntity.getBeerId());
        this.itemModelResolver.updateForTopItem(renderState.beer, beerStack, ItemDisplayContext.GROUND,
                blockEntity.getLevel(), null, blockEntity.getBlockPos().hashCode());
        renderState.angle = getRandomAngleByPos(blockEntity.getBlockPos());
        if (blockEntity.getLevel() != null) {
            renderState.lightCoords = LightCoordsUtil.getLightCoords(blockEntity.getLevel(), blockEntity.getBlockPos().above());
        }
    }

    @Override
    public void submit(RenderState renderState, PoseStack poseStack, SubmitNodeCollector nodeCollector,
                       CameraRenderState cameraRenderState) {
        if (renderState.beer.isEmpty()) {
            return;
        }
        poseStack.pushPose();
        poseStack.translate(0.5, 0.25, 0.5);
        poseStack.mulPose(Axis.YP.rotationDegrees(renderState.angle));
        renderState.beer.submit(poseStack, nodeCollector, renderState.lightCoords, OverlayTexture.NO_OVERLAY, 0);
        poseStack.popPose();
    }

    private ItemStack getBeerStack(int beerId) {
        ItemStack itemStack;
        if (beerId > Beers.EMPTY_BEER_ID) {
            Beers beer = Beers.byId(beerId);
            Item item = beer.getBeerItem();
            itemStack = new ItemStack(item, 1);
        } else {
            itemStack = new ItemStack(ItemRegistry.MIXED_BEER.get().asItem(), 1);
        }
        return itemStack;
    }

    private static float getRandomAngleByPos(BlockPos pos) {
        float angle = 0f;
        int x = pos.getX();
        int y = pos.getY();
        int z = pos.getZ();
        int sum = Math.abs(x) + Math.abs(z) + Math.abs(y);
        angle = 360 * ((float) sum % 8 / 8);

        return angle;
    }

    public static class RenderState extends BlockEntityRenderState {
        final ItemStackRenderState beer = new ItemStackRenderState();
        float angle;
    }
}
