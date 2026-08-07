package lekavar.lma.drinkbeer.blocks;

import lekavar.lma.drinkbeer.registries.SoundEventRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

public class RecipeBoardPackageBlock extends Block {
    private static final Random RNG = new Random();
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;

    public final static VoxelShape N_S_SHAPE = Block.box(0, 1, 1, 16, 10, 15);
    public final static VoxelShape E_W_SHAPE = Block.box(1, 0, 0, 15, 10, 16);

    public RecipeBoardPackageBlock() {
        super(Properties.of().mapColor(MapColor.METAL).strength(1.0f).noOcclusion());
        this.registerDefaultState(this.defaultBlockState().setValue(FACING, Direction.NORTH));
    }

    private ItemStack getRecipeBoardDrop() {
        var all = BuiltInRegistries.BLOCK.entrySet().stream().filter(entry -> {
            var block = entry.getValue();
            if (block instanceof RecipeBoardBlock) {
                return ((RecipeBoardBlock) block).isAcquirableViaPackage();
            } else return false;
        }).map(entry -> entry.getValue().asItem().getDefaultInstance()).toList();
        return all.get(RNG.nextInt(all.size()));
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
    protected InteractionResult useWithoutItem(BlockState state, Level world, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (!world.isClientSide()) {
            world.playSound(null, pos, SoundEventRegistry.UNPACKING.get(), SoundSource.BLOCKS, 0.8f, 1f);
            Containers.dropItemStack(world, pos.getX(), pos.getY(), pos.getZ(), getRecipeBoardDrop());
            world.setBlock(pos, Blocks.AIR.defaultBlockState(), 1);
        }
        return InteractionResult.sidedSuccess(world.isClientSide);
    }

    @Override
    public VoxelShape getShape(BlockState p_220053_1_, BlockGetter p_220053_2_, BlockPos p_220053_3_, CollisionContext p_220053_4_) {
        Direction dir = p_220053_1_.getValue(FACING);
        switch (dir) {
            case NORTH:
            case SOUTH:
                return N_S_SHAPE;
            default:
                return E_W_SHAPE;
        }
    }
}
