package lekavar.lma.drinkbeer.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import javax.annotation.Nullable;

public class RecipeBoardBlock extends Block {
    private final boolean acquirableViaPackage;

    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;

    public final static VoxelShape NORTH_SHAPE = Block.box(1, 0, 0, 15, 16, 1.5);
    public final static VoxelShape SOUTH_SHAPE = Block.box(1, 0, 14.5, 15, 16, 16);
    public final static VoxelShape EAST_SHAPE = Block.box(14.5, 0, 1, 16, 16, 15);
    public final static VoxelShape WEST_SHAPE = Block.box(0, 0, 1, 1.5, 16, 15);

    public RecipeBoardBlock(boolean acquirableViaPackage) {
        super(Properties.of().ignitedByLava().mapColor(MapColor.WOOD).strength(1.0f).noOcclusion());
        this.registerDefaultState(this.defaultBlockState().setValue(FACING, Direction.NORTH));
        this.acquirableViaPackage = acquirableViaPackage;
    }

    public boolean isAcquirableViaPackage() {
        return this.acquirableViaPackage;
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection());
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        Direction dir = state.getValue(FACING);
        switch (dir) {
            case NORTH:
                return NORTH_SHAPE;
            case SOUTH:
                return SOUTH_SHAPE;
            case EAST:
                return EAST_SHAPE;
            default:
                return WEST_SHAPE;
        }
    }
}
