package lekavar.lma.drinkbeer.networking;

import lekavar.lma.drinkbeer.DrinkBeer;
import lekavar.lma.drinkbeer.blockentities.TradeBoxBlockEntity;
import lekavar.lma.drinkbeer.gui.TradeBoxMenu;
import lekavar.lma.drinkbeer.registries.BlockRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.common.util.FakePlayerFactory;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(DrinkBeer.MOD_ID)
@PrefixGameTestTemplate(false)
public final class TradeBoxNetworkGameTests {
    private static final BlockPos TEST_POS = new BlockPos(2, 1, 2);

    @GameTest(template = "empty", timeoutTicks = 40)
    public static void refreshRequiresTheExactOpenTradingMenuAndBlockEntity(GameTestHelper helper) {
        helper.setBlock(TEST_POS, BlockRegistry.TRADE_BOX.get());
        BlockPos absolutePos = helper.absolutePos(TEST_POS);
        Object candidate = helper.getLevel().getBlockEntity(absolutePos);
        helper.assertTrue(candidate instanceof TradeBoxBlockEntity, "TradeBox block entity was not created");
        TradeBoxBlockEntity tradeBox = (TradeBoxBlockEntity) candidate;

        ServerPlayer player = FakePlayerFactory.getMinecraft(helper.getLevel());
        player.setPos(absolutePos.getX() + 0.5D, absolutePos.getY() + 0.5D, absolutePos.getZ() + 0.5D);
        TradeBoxMenu menu = (TradeBoxMenu) tradeBox.createMenu(1, player.getInventory(), player);
        player.containerMenu = menu;
        tradeBox.syncData.set(3, TradeBoxBlockEntity.PROCESS_TRADING);

        helper.assertTrue(ServerPayloadHandler.isValidRequest(player, absolutePos),
                "Exact open trading menu should accept refresh");
        helper.assertTrue(!ServerPayloadHandler.isValidRequest(player, absolutePos.offset(1, 0, 0)),
                "A remote or mismatched position must be rejected");

        tradeBox.syncData.set(3, TradeBoxBlockEntity.PROCESS_COOLING);
        helper.assertTrue(!ServerPayloadHandler.isValidRequest(player, absolutePos),
                "A cooling TradeBox must reject refresh");
        player.containerMenu = player.inventoryMenu;
        helper.assertTrue(!ServerPayloadHandler.isValidRequest(player, absolutePos),
                "An unrelated menu must reject refresh");
        helper.succeed();
    }

    private TradeBoxNetworkGameTests() {
    }
}
