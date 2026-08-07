package lekavar.lma.drinkbeer.blocks;

import com.mojang.serialization.MapCodec;
import lekavar.lma.drinkbeer.blockentities.BartendingTableBlockEntity;
import lekavar.lma.drinkbeer.items.SpiceBlockItem;
import lekavar.lma.drinkbeer.registries.DrinkBeerTags;
import lekavar.lma.drinkbeer.registries.SoundEventRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.items.ItemHandlerHelper;

import javax.annotation.Nullable;

public class BartendingTableBlock extends BaseEntityBlock {
    public static final MapCodec<BartendingTableBlock> CODEC = simpleCodec(pro->new BartendingTableBlock());
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final BooleanProperty OPENED = BooleanProperty.create("opened");
    public static final IntegerProperty TYPE = IntegerProperty.create("type", 1, 2);

    public final static VoxelShape SHAPE = Block.box(0, 0.01, 0, 16, 16, 16);

    public BartendingTableBlock() {
        super(BlockBehaviour.Properties.of().ignitedByLava().mapColor(MapColor.WOOD).strength(2.0f).noOcclusion());
        this.registerDefaultState(this.defaultBlockState()
                .setValue(FACING, Direction.NORTH)
                .setValue(OPENED, true)
                .setValue(TYPE, 1));
    }

    @Override
    public VoxelShape getShape(BlockState p_220053_1_, BlockGetter p_220053_2_, BlockPos p_220053_3_, CollisionContext p_220053_4_) {
        return SHAPE;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING).add(OPENED).add(TYPE);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection());
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos blockPos, BlockState blockState) {
        return new BartendingTableBlockEntity(blockPos, blockState);
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level world, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (world.isClientSide()) {
            return InteractionResult.SUCCESS;
        }
        if (world.getBlockEntity(pos) instanceof BartendingTableBlockEntity bartendingTableBlockEntity) {
            if (isDrawerHit(state, hitResult) || player.isShiftKeyDown()) {
                toggleDrawer(state, world, pos);
            } else {
                ItemStack beer = bartendingTableBlockEntity.takeBeer(false);
                if (beer.isEmpty()) {
                    player.displayClientMessage(Component.translatable("message.drinkbeer.bartending_table.no_beer"), true);
                } else {
                    ItemHandlerHelper.giveItemToPlayer(player, beer);
                    world.playSound(null, pos, SoundEvents.ITEM_PICKUP, SoundSource.PLAYERS, 0.4f, 1.0f);
                }
            }
        }
        return InteractionResult.CONSUME;
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level world, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        if (player.isShiftKeyDown()) {
            if (!world.isClientSide()) {
                toggleDrawer(state, world, pos);
            }
            return ItemInteractionResult.sidedSuccess(world.isClientSide());
        }

        ItemStack itemStack = player.getItemInHand(hand);
        boolean isBeer = itemStack.is(DrinkBeerTags.BEERS);
        boolean isSpice = itemStack.getItem() instanceof SpiceBlockItem;
        if (!isBeer && !isSpice) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }
        if (world.isClientSide()) {
            return ItemInteractionResult.SUCCESS;
        }
        if (world.getBlockEntity(pos) instanceof BartendingTableBlockEntity bartendingTableBlockEntity) {
            BartendingTableBlockEntity.TableActionResult result;
            if (isBeer) {
                result = bartendingTableBlockEntity.placeBeer(itemStack);
            } else if (!state.getValue(OPENED)) {
                player.displayClientMessage(Component.translatable("message.drinkbeer.bartending_table.drawer_closed"), true);
                return ItemInteractionResult.CONSUME;
            } else {
                result = bartendingTableBlockEntity.putSpice(itemStack);
            }

            if (result == BartendingTableBlockEntity.TableActionResult.SUCCESS) {
                world.playSound(null, pos, SoundEvents.WOOD_PLACE, SoundSource.BLOCKS, 1f, 1f);
                if (!player.getAbilities().instabuild) {
                    itemStack.shrink(1);
                }
            } else {
                showActionFailure(player, result);
            }
        }
        return ItemInteractionResult.CONSUME;
    }

    private static boolean isDrawerHit(BlockState state, BlockHitResult hitResult) {
        return hitResult.getDirection() == state.getValue(FACING).getOpposite();
    }

    private static void toggleDrawer(BlockState state, Level world, BlockPos pos) {
        boolean currentOpenedState = state.getValue(OPENED);
        world.playSound(null, pos,
                currentOpenedState ? SoundEventRegistry.BARTENDING_TABLE_CLOSE.get() : SoundEventRegistry.BARTENDING_TABLE_OPEN.get(),
                SoundSource.BLOCKS, 1f, 1f);
        world.setBlockAndUpdate(pos, state.setValue(OPENED, !currentOpenedState));
    }

    private static void showActionFailure(Player player, BartendingTableBlockEntity.TableActionResult result) {
        String translationKey = switch (result) {
            case OCCUPIED -> "message.drinkbeer.bartending_table.occupied";
            case NO_BEER -> "message.drinkbeer.bartending_table.no_beer";
            case SPICE_FULL -> "message.drinkbeer.bartending_table.spice_full";
            default -> "message.drinkbeer.bartending_table.invalid_item";
        };
        player.displayClientMessage(Component.translatable(translationKey), true);
    }

    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (!state.is(newState.getBlock()) && level.getBlockEntity(pos) instanceof BartendingTableBlockEntity blockEntity) {
            Containers.dropContents(level, pos, blockEntity.getInventory());
            level.updateNeighbourForOutputSignal(pos, this);
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }

    @Override
    public RenderShape getRenderShape(BlockState p_49232_) {
        return RenderShape.MODEL;
    }
}
