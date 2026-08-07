package lekavar.lma.drinkbeer.registries;

import lekavar.lma.drinkbeer.blockentities.BartendingTableBlockEntity;
import lekavar.lma.drinkbeer.blockentities.BeerBarrelBlockEntity;
import lekavar.lma.drinkbeer.blockentities.MixedBeerBlockEntity;
import lekavar.lma.drinkbeer.blockentities.TradeBoxBlockEntity;
import lekavar.lma.drinkbeer.platform.Registration;
import net.minecraft.world.level.block.entity.BlockEntityType;

import java.util.function.Supplier;

public class BlockEntityRegistry {
    public static final Supplier<BlockEntityType<BeerBarrelBlockEntity>> BEER_BARREL_TILEENTITY = Registration.registerBlockEntityType(
            "beer_barrel_blockentity", BeerBarrelBlockEntity::new, BlockRegistry.BEER_BARREL);
    public static final Supplier<BlockEntityType<BartendingTableBlockEntity>> BARTENDING_TABLE_TILEENTITY = Registration.registerBlockEntityType(
            "bartending_table_normal_blockentity", BartendingTableBlockEntity::new, BlockRegistry.BARTENDING_TABLE);
    public static final Supplier<BlockEntityType<TradeBoxBlockEntity>> TRADE_BOX_TILEENTITY = Registration.registerBlockEntityType(
            "trade_box_normal_blockentity", TradeBoxBlockEntity::new, BlockRegistry.TRADE_BOX);
    public static final Supplier<BlockEntityType<MixedBeerBlockEntity>> MIXED_BEER_TILEENTITY = Registration.registerBlockEntityType(
            "mixed_beer_blockentity", MixedBeerBlockEntity::new, BlockRegistry.MIXED_BEER);

    public static void init() {
    }
}
