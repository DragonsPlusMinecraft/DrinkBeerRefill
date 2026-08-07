package lekavar.lma.drinkbeer.blocks;

import com.mojang.serialization.MapCodec;
import lekavar.lma.drinkbeer.blockentities.MixedBeerBlockEntity;
import lekavar.lma.drinkbeer.managers.SpiceAndFlavorManager;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.items.ItemHandlerHelper;

import javax.annotation.Nullable;
import java.util.List;

public class MixedBeerBlock extends BaseEntityBlock {
    public final static MapCodec<MixedBeerBlock> CODEC = simpleCodec(pro->new MixedBeerBlock());
    public final static VoxelShape ONE_MUG_SHAPE = Block.box(4, 0, 4, 12, 6, 12);

    public MixedBeerBlock() {
        super(Properties.of().ignitedByLava().mapColor(MapColor.WOOD).strength(1.0f).noOcclusion().pushReaction(PushReaction.DESTROY));
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return ONE_MUG_SHAPE;
    }

    @Override
    public BlockState updateShape(BlockState state, Direction direction, BlockState neighborState, LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
        return direction == Direction.DOWN && !state.canSurvive(level, pos) ? Blocks.AIR.defaultBlockState() : super.updateShape(state, direction, neighborState, level, pos, neighborPos);
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        if (level.getBlockState(pos.below()).getBlock() == Blocks.AIR) return false;
        return Block.canSupportCenter(level, pos.below(), Direction.UP);
    }

    @Override
    protected List<ItemStack> getDrops(BlockState state, LootParams.Builder params) {
        BlockEntity blockEntity = params.getOptionalParameter(LootContextParams.BLOCK_ENTITY);
        if (blockEntity instanceof MixedBeerBlockEntity mixedBeerBlockEntity) {
            return List.of(mixedBeerBlockEntity.getPickStack());
        }
        return List.of();
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (!level.isClientSide) {
            if (level.getBlockEntity(pos) instanceof MixedBeerBlockEntity mixedBeerBlockEntity) {
                ItemHandlerHelper.giveItemToPlayer(player, mixedBeerBlockEntity.getPickStack());
                level.removeBlock(pos, false);
            }
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos blockPos, BlockState blockState) {
        return new MixedBeerBlockEntity(blockPos, blockState);
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    public RenderShape getRenderShape(BlockState p_49232_) {
        return RenderShape.MODEL;
    }

    @Override
    public void animateTick(BlockState state, Level world, BlockPos pos, RandomSource random) {
        if (world.isClientSide()) {
            super.animateTick(state, world, pos, random);
            if (random.nextInt(5) == 0) {
                if (world.getBlockEntity(pos) instanceof MixedBeerBlockEntity entity && random.nextInt(5) == 0) {
                    SimpleParticleType particle = SpiceAndFlavorManager.getLastSpiceFlavorParticle(entity.getSpiceList());
                    double x = (double) pos.getX() + 0.5D;
                    double y = (double) pos.getY() + 0.5D + random.nextDouble() / 4;
                    double z = (double) pos.getZ() + 0.5D;
                    world.addParticle(particle, x, y, z, 0.0D, 0.0D, 0.0D);
                }
            }
        }
    }

}
