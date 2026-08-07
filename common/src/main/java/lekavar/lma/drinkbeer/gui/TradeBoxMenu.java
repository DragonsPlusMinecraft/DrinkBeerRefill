package lekavar.lma.drinkbeer.gui;

import lekavar.lma.drinkbeer.blockentities.TradeBoxBlockEntity;
import lekavar.lma.drinkbeer.managers.TradeBoxManager;
import lekavar.lma.drinkbeer.registries.BlockRegistry;
import lekavar.lma.drinkbeer.registries.MenuTypeRegistry;
import lekavar.lma.drinkbeer.registries.SoundEventRegistry;
import lekavar.lma.drinkbeer.utils.tradebox.Good;
import lekavar.lma.drinkbeer.utils.tradebox.Locations;
import lekavar.lma.drinkbeer.utils.tradebox.Residents;
import lekavar.lma.drinkbeer.utils.tradebox.TradeMission;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

public class TradeBoxMenu extends AbstractContainerMenu {
    private final Container tradeboxInventory;
    private final List<Slot> tradeboxSlots;

    /**
     * goodSlots:
     * 0-3 goods to loacation
     * 4-7 goods from location
     */
    private final List<Slot> goodSlots;
    private final ContainerData syncData;

    private Runnable inventoryChangeListener;
    private final Player player;

    private final BlockPos pos;

    public TradeBoxMenu(int id, Inventory playerInventory, FriendlyByteBuf data) {
        this(id, playerInventory, data.readBlockPos());
    }

    public TradeBoxMenu(int id, Inventory playerInventory, BlockPos pos) {
        this(id, new SimpleContainer(8), new SimpleContainerData(4), playerInventory, pos, false);
    }

    public TradeBoxMenu(int id, Container goodInventory, ContainerData syncData, Inventory playerInventory, TradeBoxBlockEntity tradeBoxBlockEntity) {
        this(id, goodInventory, syncData, playerInventory, tradeBoxBlockEntity.getBlockPos(), true);
    }

    private TradeBoxMenu(int id, Container goodInventory, ContainerData syncData, Inventory playerInventory, BlockPos pos, boolean serverBacked) {
        super(MenuTypeRegistry.tradeBoxContainer.get(), id);
        this.syncData = syncData;
        this.pos = pos;

        this.inventoryChangeListener = () -> {
        };

        this.player = playerInventory.player;

        tradeboxSlots = new ArrayList<>();
        goodSlots = new ArrayList<>();

        this.tradeboxInventory = new SimpleContainer(4) {
            public void setChanged() {
                super.setChanged();
                TradeBoxMenu.this.slotsChanged(this);
                TradeBoxMenu.this.inventoryChangeListener.run();
            }
        };
        // check size
        if (goodInventory.getContainerSize() < 8) {
            throw new IllegalArgumentException("Container size " + goodInventory.getContainerSize() + " is smaller than expected " + 8);
        }

        AtomicInteger num = new AtomicInteger();
        //Init tradeboxSlots
        IntStream.range(0, 2).forEach(i ->
                IntStream.range(0, 2).forEach(j -> {
                    this.tradeboxSlots.add(this.addSlot(new InputSlot(tradeboxInventory, num.get(), 25 + i * 18, 26 + j * 18)));
                    num.getAndIncrement();
                }));
        //Init goodSlots
        num.set(0);
        IntStream.range(0, 2).forEach(j ->
                IntStream.range(0, 4).forEach(i -> {
                    this.goodSlots.add(this.addSlot(new OutputSlot(goodInventory, num.get(), 85 + i * 18, 26 + j * 18, syncData, this)));
                    num.getAndIncrement();
                }));

        // Player Inventory
        layoutPlayerInventorySlots(8, 84, playerInventory);

        //Generate trade mission if tradebox is in trading process but has illegal trade mission
        if (serverBacked && isTrading() && !hasLegalTradeMission()) {
            setTradeboxTrading();
        }

        //Tracking Data
        addDataSlots(syncData);
    }

    private int addSlotRange(Inventory inventory, int index, int x, int y, int amount, int dx) {
        for (int i = 0; i < amount; i++) {
            addSlot(new Slot(inventory, index, x, y));
            x += dx;
            index++;
        }
        return index;
    }

    private int addSlotBox(Inventory inventory, int index, int x, int y, int horAmount, int dx, int verAmount, int dy) {
        for (int j = 0; j < verAmount; j++) {
            index = addSlotRange(inventory, index, x, y, horAmount, dx);
            y += dy;
        }
        return index;
    }

    private void layoutPlayerInventorySlots(int leftCol, int topRow, Inventory playerInventory) {
        // Player inventory
        addSlotBox(playerInventory, 9, leftCol, topRow, 9, 18, 3, 18);

        // Hotbar
        topRow += 58;
        addSlotRange(playerInventory, 0, leftCol, topRow, 9, 18);
    }

    @Override
    public boolean stillValid(Player player) {
        return player.level().getBlockEntity(pos) instanceof TradeBoxBlockEntity
                && player.distanceToSqr(pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D) <= 64.0D;
    }

    public boolean isBoundTo(BlockPos blockPos) {
        return pos.equals(blockPos);
    }

    public BlockPos getBlockPos() {
        return pos;
    }

    public int getCoolingTime() {
        return syncData.get(0);
    }

    public int getProcess() {
        return syncData.get(3);
    }

    // @Dist(EnvType.CLIENT)
    public void setInventoryChangeListener(Runnable inventoryChangeListener) {
        this.inventoryChangeListener = inventoryChangeListener;
    }

    @Override
    public void slotsChanged(Container inventory) {
        if (!player.level().isClientSide()) {
            checkTradeMission();
        }
        this.broadcastChanges();
    }

    public void checkTradeMission() {
        if (!isTrading())
            return;

        //Test whether the inputted goods meet required goods
        Map<Item, Integer> inputGoodMap = goodSlotListToGoodMap(tradeboxSlots);
        Map<Item, Integer> neededGoodMap = goodSlotListToGoodMap(getToLocationGoodSlots());
        if (!TradeBoxManager.test(inputGoodMap, neededGoodMap)) {
            return;
        }

        //Set process cooling first so that does't trigger this method by itself
        setProcess(TradeBoxBlockEntity.PROCESS_COOLING);
        //Consume input goods
        consumeInputGood(neededGoodMap);
        //Drop extra input goods to player
        clearContainer(player, this.tradeboxInventory);

        //Set goods from location into tradeboxSlots
        int goodNum = 0;
        for (Slot slot : getFromLocationGoodSlots()) {
            if (slot.hasItem()) {
                this.tradeboxSlots.get(goodNum).set(slot.getItem());
                goodNum++;
            }
        }

        setTradeboxCooling();
    }

    public void consumeInputGood(Map<Item, Integer> targetGoodMap) {
        for (Map.Entry<Item, Integer> targetGood : targetGoodMap.entrySet()) {
            int consumedCount = 0;
            int requiredCount = targetGood.getValue();
            for (Slot slot : tradeboxSlots) {
                if (targetGood.getKey().equals(slot.getItem().getItem())) {
                    int amount = Math.min(slot.getItem().getCount(), requiredCount - consumedCount);
                    slot.remove(amount);
                    consumedCount += amount;
                }
                if (consumedCount >= requiredCount) {
                    break;
                }
            }
        }
    }

    public boolean isCooling() {
        return getProcess() == TradeBoxBlockEntity.PROCESS_COOLING;
    }

    public boolean isTrading() {
        return getProcess() == TradeBoxBlockEntity.PROCESS_TRADING;
    }

    public void setProcess(int process) {
        this.syncData.set(3, process);
    }

    public void setCoolingTime(int coolingTime) {
        this.syncData.set(0, coolingTime);
    }

    public void setLocationId(int locationId) {
        this.syncData.set(1, locationId);
    }

    public void setResidentId(int residentId) {
        this.syncData.set(2, residentId);
    }

    public int getLocationId() {
        return syncData.get(1);
    }

    public int getResidentId() {
        return syncData.get(2);
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        if (!player.level().isClientSide()) {
            // Return Item to Player;
            clearContainer(player, this.tradeboxInventory);
        } else {
            player.level().playSound(null, player.blockPosition(), SoundEventRegistry.TRADEBOX_CLOSE.get(), SoundSource.BLOCKS, 0.6f, 1f);
        }
    }

    @Override
    public ItemStack quickMoveStack(Player player, int invSlot) {
        if (invSlot < 0 || invSlot >= this.slots.size()) {
            return ItemStack.EMPTY;
        }
        int inputEnd = this.tradeboxSlots.size();
        int offerEnd = inputEnd + this.goodSlots.size();
        if (invSlot >= inputEnd && invSlot < offerEnd) {
            // Offer slots are display-only and must never become a source for shift-click transfers.
            return ItemStack.EMPTY;
        }

        ItemStack newStack = ItemStack.EMPTY;
        Slot slot = this.slots.get(invSlot);
        if (slot != null && slot.hasItem()) {
            ItemStack originalStack = slot.getItem();
            newStack = originalStack.copy();
            if (invSlot < inputEnd) {
                if (!this.moveItemStackTo(originalStack, offerEnd, this.slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else if (!this.moveItemStackTo(originalStack, 0, inputEnd, false)) {
                return ItemStack.EMPTY;
            }

            if (originalStack.isEmpty()) {
                slot.set(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }
        }

        return newStack;
    }

    public void setTradeMission(TradeMission tradeMission) {
        clearGoodInventory();
        setLocationId(tradeMission.getLocationId());
        setResidentId(tradeMission.getResidentId());
        if (!tradeMission.getGoodToLocationList().isEmpty()) {
            IntStream.range(0, Math.min(4, tradeMission.getGoodToLocationList().size())).forEach(i -> {
                Good good = tradeMission.getGoodToLocationList().get(i);
                ItemStack goodItemStack = new ItemStack(good.getGoodItem(), good.getCount());
                goodSlots.get(i).set(goodItemStack);
            });
        }
        if (!tradeMission.getGoodFromLocationList().isEmpty()) {
            IntStream.range(0, Math.min(4, tradeMission.getGoodFromLocationList().size())).forEach(i -> {
                Good good = tradeMission.getGoodFromLocationList().get(i);
                ItemStack goodItemStack = new ItemStack(good.getGoodItem(), good.getCount());
                goodSlots.get(4 + i).set(goodItemStack);
            });
        }
    }

    public void setTradeboxCooling() {
        setProcess(TradeBoxBlockEntity.PROCESS_COOLING);
        setCoolingTime(TradeBoxManager.COOLING_TIME_ON_REFRESH);
        setLocationId(Locations.EMPTY_LOCATION.getId());
        setResidentId(Residents.EMPTY_RESIDENT.getId());
        clearGoodInventory();
    }

    public void setTradeboxTrading() {
        Block block = player.level().getBlockState(pos).getBlock();
        if (!block.equals(BlockRegistry.TRADE_BOX.get())) {
            return;
        }
        setTradeMission(TradeMission.genRandomTradeMission());
        setProcess(TradeBoxBlockEntity.PROCESS_TRADING);
    }

    public List<Slot> getToLocationGoodSlots() {
        return goodSlots.subList(0, 4);
    }

    public List<Slot> getFromLocationGoodSlots() {
        return goodSlots.subList(4, 8);
    }

    public boolean hasLegalTradeMission() {
        if (getLocationId() <= Locations.EMPTY_LOCATION.getId() || getLocationId() >= Locations.size())
            return false;
        if (getResidentId() <= Residents.EMPTY_RESIDENT.getId() || getResidentId() >= Residents.size())
            return false;
        if (Residents.byId(getResidentId()).getLocation().getId() != getLocationId())
            return false;
        if (getToLocationGoodSlots().stream().noneMatch(Slot::hasItem))
            return false;
        return getFromLocationGoodSlots().stream().anyMatch(Slot::hasItem);
    }

    private void clearGoodInventory() {
        goodSlots.forEach(slot -> slot.set(ItemStack.EMPTY));
    }

    private Map<Item, Integer> goodSlotListToGoodMap(List<Slot> goodSlots) {
        Map<Item, Integer> goodMap = new HashMap<>();
        IntStream.range(0, 4).forEach(i -> {
            if (!goodSlots.get(i).hasItem())
                return;

            ItemStack inputTradeboxSlotsItemStack = goodSlots.get(i).getItem();
            Item inputTradeboxSlotsItem = inputTradeboxSlotsItemStack.getItem();
            if (goodMap.containsKey(inputTradeboxSlotsItem)) {
                goodMap.replace(inputTradeboxSlotsItem, goodMap.get(inputTradeboxSlotsItem) + inputTradeboxSlotsItemStack.getCount());
            } else {
                goodMap.put(inputTradeboxSlotsItem, inputTradeboxSlotsItemStack.getCount());
            }
        });
        return goodMap;
    }

    static class InputSlot extends Slot {
        public InputSlot(Container p_i1824_1_, int p_i1824_2_, int p_i1824_3_, int p_i1824_4_) {
            super(p_i1824_1_, p_i1824_2_, p_i1824_3_, p_i1824_4_);
        }

        // Placing item on output slot is prohibited.
        @Override
        public boolean mayPlace(ItemStack p_75214_1_) {
            return true;
        }
    }

    static class OutputSlot extends Slot {
        private final ContainerData syncData;
        private final TradeBoxMenu tradeBoxMenu;

        public OutputSlot(Container p_i1824_1_, int p_i1824_2_, int p_i1824_3_, int p_i1824_4_, ContainerData syncData, TradeBoxMenu tradeBoxMenu) {
            super(p_i1824_1_, p_i1824_2_, p_i1824_3_, p_i1824_4_);
            this.syncData = syncData;
            this.tradeBoxMenu = tradeBoxMenu;
        }

        public boolean mayPlace(ItemStack stack) {
            return false;
        }

        public boolean mayPickup(Player playerEntity) {
            return false;
        }

        public boolean isActive() {
            return !tradeBoxMenu.isCooling();
        }
    }
}
