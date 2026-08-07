package lekavar.lma.drinkbeer.blocks;

import lekavar.lma.drinkbeer.registries.BlockRegistry;
import lekavar.lma.drinkbeer.utils.mixedbeer.Spices;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.HalfTransparentBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import javax.annotation.Nullable;
public class SpiceBlock extends HalfTransparentBlock {
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    public final static VoxelShape DEFAULT_SHAPE = box(5.5, 0, 5.5, 10.5, 2, 10.5);
    public final static VoxelShape SPICE_FROZEN_PERSIMMON_SHAPE = box(5.5, 0, 5.5, 10.5, 3.5, 10.5);
    public final static VoxelShape SPICE_DRIED_SELAGINELLA = box(5.5, 0, 5.5, 10.5, 4.5, 10.5);

    public SpiceBlock() {
        super(BlockBehaviour.Properties.of().ignitedByLava().mapColor(MapColor.WOOD).strength(1.0f).pushReaction(PushReaction.DESTROY));
        this.registerDefaultState(this.defaultBlockState().setValue(FACING, Direction.NORTH));
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter view, BlockPos pos, CollisionContext context) {
        if (this.equals(BlockRegistry.SPICE_FROZEN_PERSIMMON.get())) {
            return SPICE_FROZEN_PERSIMMON_SHAPE;
        }
        if (this.equals(BlockRegistry.SPICE_DRIED_SELAGINELLA.get())) {
            return SPICE_DRIED_SELAGINELLA;
        }
        return DEFAULT_SHAPE;
    }

    @Override
    public BlockState updateShape(BlockState state, Direction direction, BlockState neighborState, LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
        return direction == Direction.DOWN && !state.canSurvive(level, pos) ? Blocks.AIR.defaultBlockState() : super.updateShape(state, direction, neighborState, level, pos, neighborPos);
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        if (level.isClientSide) {
            SimpleParticleType particle = Spices.byItem(this.asItem()).getFlavor().getParticle();
            double x = (double) pos.getX() + 0.5D;
            double y = (double) pos.getY() + 0.3D + level.getRandom().nextDouble() / 4;
            double z = (double) pos.getZ() + 0.5D;
            if (particle != null) {
                level.addParticle(particle, x, y, z, 0.0D, 0.0D, 0.0D);
            }
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection());
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        if (level.getBlockState(pos.below()).getBlock() == Blocks.AIR) return false;
        return Block.canSupportCenter(level, pos.below(), Direction.UP);
    }

}
