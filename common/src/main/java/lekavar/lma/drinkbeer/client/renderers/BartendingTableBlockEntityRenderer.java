package lekavar.lma.drinkbeer.client.renderers;

import com.mojang.blaze3d.vertex.PoseStack;
import lekavar.lma.drinkbeer.blockentities.BartendingTableBlockEntity;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public class BartendingTableBlockEntityRenderer implements BlockEntityRenderer<BartendingTableBlockEntity, BartendingTableBlockEntityRenderer.RenderState> {
    private final ItemModelResolver itemModelResolver;

    public BartendingTableBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
        this.itemModelResolver = context.itemModelResolver();
    }

    @Override
    public RenderState createRenderState() {
        return new RenderState();
    }

    @Override
    public void extractRenderState(BartendingTableBlockEntity blockEntity, RenderState renderState, float partialTick,
                                   Vec3 cameraPosition, ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress) {
        BlockEntityRenderer.super.extractRenderState(blockEntity, renderState, partialTick, cameraPosition, breakProgress);
        ItemStack beerStack = blockEntity.takeBeer(true);
        this.itemModelResolver.updateForTopItem(renderState.beer, beerStack, ItemDisplayContext.GROUND,
                blockEntity.getLevel(), null, blockEntity.getBlockPos().hashCode());
        if (blockEntity.getLevel() != null) {
            renderState.lightCoords = LevelRenderer.getLightCoords(blockEntity.getLevel(), blockEntity.getBlockPos().above());
        }
    }

    @Override
    public void submit(RenderState renderState, PoseStack poseStack, SubmitNodeCollector nodeCollector,
                       CameraRenderState cameraRenderState) {
        if (renderState.beer.isEmpty()) {
            return;
        }
        poseStack.pushPose();
        poseStack.translate(0.5, 1.25, 0.5);
        renderState.beer.submit(poseStack, nodeCollector, renderState.lightCoords, OverlayTexture.NO_OVERLAY, 0);
        poseStack.popPose();
    }

    public static class RenderState extends BlockEntityRenderState {
        final ItemStackRenderState beer = new ItemStackRenderState();
    }
}
