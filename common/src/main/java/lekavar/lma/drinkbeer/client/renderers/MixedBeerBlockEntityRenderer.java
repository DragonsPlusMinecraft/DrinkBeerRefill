package lekavar.lma.drinkbeer.client.renderers;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import lekavar.lma.drinkbeer.blockentities.MixedBeerBlockEntity;
import lekavar.lma.drinkbeer.registries.ItemRegistry;
import lekavar.lma.drinkbeer.utils.beer.Beers;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

public class MixedBeerBlockEntityRenderer implements BlockEntityRenderer<MixedBeerBlockEntity> {
    public MixedBeerBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(MixedBeerBlockEntity blockEntity, float tickDelta, PoseStack matrices, MultiBufferSource vertexConsumers, int light, int overlay) {
        matrices.pushPose();

        int beerId = blockEntity.getBeerId();
        ItemStack beerStack = getBeerStack(beerId);
        BlockPos pos = blockEntity.getBlockPos();

        //Move beer
        matrices.translate(0.5, 0.25, 0.5);
        //Rotate beer
        matrices.mulPose(Axis.YP.rotationDegrees(getRandomAngleByPos(pos)));
        //Get light at the beer block
        int lightAbove = LevelRenderer.getLightColor(blockEntity.getLevel(), blockEntity.getBlockPos().above());
        //Render beer
        Minecraft.getInstance().getItemRenderer().renderStatic(beerStack, ItemDisplayContext.GROUND, lightAbove, overlay, matrices, vertexConsumers, blockEntity.getLevel(), 0);

        matrices.popPose();
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

    private float getRandomAngleByPos(BlockPos pos) {
        float angle = 0f;
        int x = pos.getX();
        int y = pos.getY();
        int z = pos.getZ();
        int sum = Math.abs(x) + Math.abs(z) + Math.abs(y);
        angle = 360 * ((float) sum % 8 / 8);

        return angle;
    }
}