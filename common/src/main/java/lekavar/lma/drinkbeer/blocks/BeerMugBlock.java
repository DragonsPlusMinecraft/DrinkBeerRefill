package lekavar.lma.drinkbeer.blocks;

import lekavar.lma.drinkbeer.utils.ItemStackHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import javax.annotation.Nullable;

public class BeerMugBlock extends Block {
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final IntegerProperty AMOUNT = IntegerProperty.create("amount", 1, 3);
    protected static final VoxelShape[] SHAPE_BY_AMOUNT = new VoxelShape[]{
            Block.box(0, 0, 0, 16, 16, 16),
            Block.box(4, 0, 4, 12, 6, 12),
            Block.box(2, 0, 2, 14, 6, 14),
            Block.box(1, 0, 1, 15, 6, 15)
    };

    public BeerMugBlock() {
        super(Properties.of().ignitedByLava().mapColor(MapColor.WOOD).strength(1.0f).noOcclusion().pushReaction(PushReaction.DESTROY));
        this.registerDefaultState(
                this.defaultBlockState().setValue(FACING, Direction.NORTH).setValue(AMOUNT, 1)
        );
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE_BY_AMOUNT[state.getValue(AMOUNT)];
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(AMOUNT, FACING);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection());
    }

    @Override
    public BlockState updateShape(BlockState state, Direction direction, BlockState neighborState, LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
        return direction == Direction.DOWN && !state.canSurvive(level, pos) ? Blocks.AIR.defaultBlockState() : super.updateShape(state, direction, neighborState, level, pos, neighborPos);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level world, BlockPos pos, Player player, BlockHitResult hitResult) {
        if(!world.isClientSide()){
            ItemStack takeBackBeer = state.getBlock().asItem().getDefaultInstance();
            ItemStackHelper.giveToPlayer(player, takeBackBeer);
            int amount = state.getValue(AMOUNT);
            switch (amount) {
                case 3:
                case 2:
                    world.setBlockAndUpdate(pos, state.getBlock().defaultBlockState().setValue(AMOUNT, amount - 1).setValue(FACING, state.getValue(FACING)));
                    world.playSound(null, pos, SoundEvents.WOOD_PLACE, SoundSource.AMBIENT, 0.5f, 0.5f);
                    break;
                default:
                    world.setBlockAndUpdate(pos, Blocks.AIR.defaultBlockState());
                    world.playSound(null, pos, SoundEvents.WOOD_PLACE, SoundSource.AMBIENT, 0.5f, 0.5f);
            }
        }
        return InteractionResult.sidedSuccess(world.isClientSide);
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level world, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        ItemStack itemStack = player.getItemInHand(hand);
        // Placing Beer
        if (itemStack.getItem() == state.getBlock().asItem()) {
            if (!world.isClientSide()) {
                int amount = state.getValue(AMOUNT);
                int mugInHandCount = player.getItemInHand(hand).getCount();
                boolean isCreative = player.isCreative();
                switch (amount) {
                    case 1: {
                        world.setBlockAndUpdate(pos, state.getBlock().defaultBlockState().setValue(AMOUNT, 2).setValue(FACING, state.getValue(FACING)));
                        if (!isCreative) {
                            player.getItemInHand(hand).setCount(mugInHandCount - 1);
                        }
                        world.playSound(null, pos, SoundEvents.WOOD_PLACE, SoundSource.BLOCKS, 1f, 1f);
                        break;
                    }
                    case 2: {
                        world.setBlockAndUpdate(pos, state.getBlock().defaultBlockState().setValue(AMOUNT, 3).setValue(FACING, state.getValue(FACING)));
                        if (!isCreative) {
                            player.getItemInHand(hand).setCount(mugInHandCount - 1);
                        }
                        world.playSound(null, pos, SoundEvents.WOOD_PLACE, SoundSource.BLOCKS, 1f, 1f);
                        break;
                    }
                    default: {}
                }
            }
            return ItemInteractionResult.sidedSuccess(world.isClientSide);
        }
        return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }


    @Override
    public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        if (level.getBlockState(pos.below()).getBlock() instanceof BeerMugBlock) return false;
        return Block.canSupportCenter(level, pos.below(), Direction.UP);
    }
}
