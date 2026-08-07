package lekavar.lma.drinkbeer.blocks;

import lekavar.lma.drinkbeer.registries.SoundEventRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DoublePlantBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

public class HorseModelBlock extends DoublePlantBlock {
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    private static final VoxelShape SHAPE = Shapes.block();

    private final boolean includesBell;

    public HorseModelBlock(boolean includesBell) {
        super(Properties.of()
                .mapColor(MapColor.WOOD)
                .strength(1.0F)
                .sound(SoundType.WOOD)
                .noOcclusion()
                .pushReaction(PushReaction.DESTROY));
        this.includesBell = includesBell;
        registerDefaultState(stateDefinition.any()
                .setValue(HALF, DoubleBlockHalf.LOWER)
                .setValue(FACING, Direction.NORTH));
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockState state = super.getStateForPlacement(context);
        return state == null ? null : state.setValue(FACING, context.getHorizontalDirection());
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        BlockPos upperPos = pos.above();
        boolean placedUpper = level.setBlock(upperPos, defaultBlockState()
                .setValue(HALF, DoubleBlockHalf.UPPER)
                .setValue(FACING, state.getValue(FACING)), Block.UPDATE_ALL);
        if (!placedUpper) {
            level.removeBlock(pos, false);
        }
    }

    @Override
    protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        if (state.getValue(HALF) == DoubleBlockHalf.UPPER) {
            BlockState lower = level.getBlockState(pos.below());
            return lower.is(this) && lower.getValue(HALF) == DoubleBlockHalf.LOWER;
        }
        return Block.canSupportCenter(level, pos.below(), Direction.UP);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        playHorseSound(level, pos);
        return InteractionResult.sidedSuccess(level.isClientSide());
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
                                              Player player, InteractionHand hand, BlockHitResult hitResult) {
        if (hand != InteractionHand.MAIN_HAND) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }
        playHorseSound(level, pos);
        return ItemInteractionResult.sidedSuccess(level.isClientSide());
    }

    private void playHorseSound(Level level, BlockPos pos) {
        if (level.isClientSide()) {
            return;
        }

        int choice = level.getRandom().nextInt(includesBell ? 3 : 2);
        SoundEvent sound = choice == 0 && includesBell
                ? SoundEventRegistry.BELL.get()
                : choice == (includesBell ? 1 : 0)
                ? SoundEventRegistry.NEIGH_1.get()
                : SoundEventRegistry.NEIGH_2.get();
        float volume = 0.8F + level.getRandom().nextFloat() * 0.2F;
        float pitch = sound == SoundEventRegistry.BELL.get()
                ? 0.95F + level.getRandom().nextFloat() * 0.05F
                : 0.8F + level.getRandom().nextFloat() * 0.2F;
        level.playSound(null, pos, sound, SoundSource.BLOCKS, volume, pitch);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(FACING);
    }
}
