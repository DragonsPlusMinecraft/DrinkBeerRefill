package lekavar.lma.drinkbeer.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

public class ColoredLightsBlock extends Block {
    public static final EnumProperty<Direction> FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final IntegerProperty TYPE = IntegerProperty.create("type", 1, 8);

    private static final VoxelShape CENTER_NORTH_SOUTH = box(0.0, 10.5, 7.0, 16.0, 16.0, 9.0);
    private static final VoxelShape CENTER_EAST_WEST = box(7.0, 10.5, 0.0, 9.0, 16.0, 16.0);
    private static final VoxelShape SIDE_NORTH = box(0.0, 10.5, 0.0, 16.0, 16.0, 2.0);
    private static final VoxelShape SIDE_SOUTH = box(0.0, 10.5, 14.0, 16.0, 16.0, 16.0);
    private static final VoxelShape SIDE_EAST = box(14.0, 10.5, 0.0, 16.0, 16.0, 16.0);
    private static final VoxelShape SIDE_WEST = box(0.0, 10.5, 0.0, 2.0, 16.0, 16.0);

    private final boolean sideMounted;

    public ColoredLightsBlock(Properties properties, boolean sideMounted) {
        super(properties);
        this.sideMounted = sideMounted;
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(TYPE, 1));
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState().setValue(FACING, context.getHorizontalDirection());
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        if (!level.isClientSide()) {
            level.setBlock(pos, state.setValue(TYPE, level.getRandom().nextInt(8) + 1), Block.UPDATE_CLIENTS);
        }
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        Direction facing = state.getValue(FACING);
        if (!sideMounted) {
            return facing.getAxis() == Direction.Axis.Z ? CENTER_NORTH_SOUTH : CENTER_EAST_WEST;
        }
        return switch (facing) {
            case NORTH -> SIDE_NORTH;
            case SOUTH -> SIDE_SOUTH;
            case EAST -> SIDE_EAST;
            case WEST -> SIDE_WEST;
            default -> SIDE_NORTH;
        };
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, TYPE);
    }
}
