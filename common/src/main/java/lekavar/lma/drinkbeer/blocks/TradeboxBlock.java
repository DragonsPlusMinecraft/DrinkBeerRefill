package lekavar.lma.drinkbeer.blocks;

import com.mojang.serialization.MapCodec;
import lekavar.lma.drinkbeer.blockentities.TradeBoxBlockEntity;
import lekavar.lma.drinkbeer.managers.TradeBoxManager;
import lekavar.lma.drinkbeer.platform.Platform;
import lekavar.lma.drinkbeer.registries.SoundEventRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import javax.annotation.Nullable;

public class TradeboxBlock extends BaseEntityBlock {
    public static final MapCodec<TradeboxBlock> CODEC = simpleCodec(pro->new TradeboxBlock());
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final VoxelShape SHAPE = Block.box(0, 0.01, 0, 16, 16, 16);

    public TradeboxBlock() {
        super(Properties.of().ignitedByLava().mapColor(MapColor.WOOD).strength(2.0f).noOcclusion());
        this.registerDefaultState(this.defaultBlockState()
                .setValue(FACING, Direction.NORTH));
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
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

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState blockState, BlockEntityType<T> blockEntity) {
        if (level != null && level.isClientSide()) {
            return null;
        } else {
            return (theLevel, pos, state, tile) -> {
                if (tile instanceof TradeBoxBlockEntity tradeBoxBlockEntity) {
                    TradeBoxBlockEntity.tick(theLevel, pos, state, tradeBoxBlockEntity);
                }
            };
        }
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos blockPos, BlockState blockState) {
        return new TradeBoxBlockEntity(TradeBoxManager.COOLING_TIME_ON_PLACE, blockPos, blockState);
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (!level.isClientSide) {
            BlockEntity blockentity = level.getBlockEntity(pos);
            if (blockentity instanceof TradeBoxBlockEntity tradeBoxBlockEntity) {
                level.playSound(null, pos, SoundEventRegistry.TRADEBOX_OPEN.get(), SoundSource.BLOCKS, 0.6f, 1f);
                Platform.hooks().openMenu(player, tradeBoxBlockEntity, tradeBoxBlockEntity.getBlockPos());
            }

            return InteractionResult.CONSUME;
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    public RenderShape getRenderShape(BlockState p_49232_) {
        return RenderShape.MODEL;
    }
}
