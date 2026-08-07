package lekavar.lma.drinkbeer.blockentities;

import lekavar.lma.drinkbeer.gui.TradeBoxMenu;
import lekavar.lma.drinkbeer.managers.TradeBoxManager;
import lekavar.lma.drinkbeer.registries.BlockEntityRegistry;
import lekavar.lma.drinkbeer.utils.tradebox.Good;
import lekavar.lma.drinkbeer.utils.tradebox.Locations;
import lekavar.lma.drinkbeer.utils.tradebox.Residents;
import lekavar.lma.drinkbeer.utils.tradebox.TradeMission;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class TradeBoxBlockEntity extends BlockEntity implements MenuProvider {
    public final SimpleContainer goodInventory = new SimpleContainer(8) {
        @Override
        public void setChanged() {
            super.setChanged();
            TradeBoxBlockEntity.this.setChanged();
        }
    };
    private int coolingTime;
    private int locationId;
    private int residentId;
    private int process;
    public static final int PROCESS_COOLING = 0;
    public static final int PROCESS_TRADING = 1;

    public TradeBoxBlockEntity(BlockPos pos, BlockState state) {
        super(BlockEntityRegistry.TRADE_BOX_TILEENTITY.get(), pos, state);
        this.coolingTime = TradeBoxManager.COOLING_TIME_ON_PLACE;
        this.locationId = Locations.EMPTY_LOCATION.getId();
        this.residentId = Residents.EMPTY_RESIDENT.getId();
        this.process = PROCESS_COOLING;
    }

    public TradeBoxBlockEntity(int coolingTime, BlockPos pos, BlockState state) {
        this(pos, state);
        this.coolingTime = Math.max(0, coolingTime);
    }

    public final ContainerData syncData = new ContainerData() {
        @Override
        public int get(int index) {
            switch (index) {
                case 0:
                    return coolingTime;
                case 1:
                    return locationId;
                case 2:
                    return residentId;
                case 3:
                    return process;
                default:
                    return 0;
            }
        }

        @Override
        public void set(int index, int value) {
            switch (index) {
                case 0:
                    coolingTime = value;
                    break;
                case 1:
                    locationId = value;
                    break;
                case 2:
                    residentId = value;
                    break;
                case 3:
                    process = value;
                    break;
            }
            setChanged();
        }

        @Override
        public int getCount() {
            return 4;
        }
    };

    @Override
    public @NotNull Component getDisplayName() {
        return Component.translatable("block.drinkbeer.trade_box_normal");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int id, @NotNull Inventory inventory, @NotNull Player player) {
        return new TradeBoxMenu(id, this.goodInventory, syncData, inventory, this);
    }

    @Override
    public void saveAdditional(@NotNull CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag,registries);
        ContainerHelper.saveAllItems(tag, this.goodInventory.getItems(), registries);
        tag.putInt("CoolingTime", this.coolingTime);
        tag.putInt("LocationId", this.locationId);
        tag.putInt("ResidentId", this.residentId);
        tag.putInt("Process", this.process);
    }

    @Override
    public void loadAdditional(@Nonnull CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag,registries);
        ContainerHelper.loadAllItems(tag, this.goodInventory.getItems(), registries);
        this.coolingTime = Math.max(0, tag.getInt("CoolingTime"));
        this.locationId = Locations.byId(tag.getInt("LocationId")).getId();
        this.residentId = Residents.byId(tag.getInt("ResidentId")).getId();
        this.process = tag.getInt("Process") == PROCESS_TRADING ? PROCESS_TRADING : PROCESS_COOLING;
    }

    public static void tick(Level world, BlockPos pos, BlockState state, TradeBoxBlockEntity tradeboxEntity) {
        if (world.isClientSide() || tradeboxEntity.process != PROCESS_COOLING) {
            return;
        }

        if (tradeboxEntity.coolingTime > 0) {
            tradeboxEntity.coolingTime--;
            if (tradeboxEntity.coolingTime % 20 == 0) {
                tradeboxEntity.setChanged();
            }
        }
        if (tradeboxEntity.coolingTime == 0) {
            tradeboxEntity.startTrading();
        }
    }

    private void startTrading() {
        TradeMission tradeMission = TradeMission.genRandomTradeMission();
        goodInventory.clearContent();

        int toLocationCount = Math.min(4, tradeMission.getGoodToLocationList().size());
        for (int slot = 0; slot < toLocationCount; slot++) {
            Good good = tradeMission.getGoodToLocationList().get(slot);
            goodInventory.setItem(slot, new ItemStack(good.getGoodItem(), good.getCount()));
        }

        int fromLocationCount = Math.min(4, tradeMission.getGoodFromLocationList().size());
        for (int slot = 0; slot < fromLocationCount; slot++) {
            Good good = tradeMission.getGoodFromLocationList().get(slot);
            goodInventory.setItem(4 + slot, new ItemStack(good.getGoodItem(), good.getCount()));
        }

        locationId = tradeMission.getLocationId();
        residentId = tradeMission.getResidentId();
        process = PROCESS_TRADING;
        setChanged();
    }
}
