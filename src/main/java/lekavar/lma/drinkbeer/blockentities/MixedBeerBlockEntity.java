package lekavar.lma.drinkbeer.blockentities;

import lekavar.lma.drinkbeer.managers.MixedBeerManager;
import lekavar.lma.drinkbeer.registries.BlockEntityRegistry;
import lekavar.lma.drinkbeer.utils.beer.Beers;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class MixedBeerBlockEntity extends BlockEntity {
    private int beerId = Beers.EMPTY_BEER_ID;
    private List<Integer> spiceList = new ArrayList<>();

    public MixedBeerBlockEntity(BlockPos pos, BlockState state) {
        super(BlockEntityRegistry.MIXED_BEER_TILEENTITY.get(), pos, state);
    }

    public MixedBeerBlockEntity(BlockPos pos, BlockState state, int beerId, List<Integer> spiceList) {
        super(BlockEntityRegistry.MIXED_BEER_TILEENTITY.get(), pos, state);
        this.beerId = MixedBeerManager.sanitizeBeerId(beerId);
        this.spiceList = new ArrayList<>(MixedBeerManager.sanitizeSpiceIds(spiceList));
    }

    /**
     * @see MixedBeerManager#genMixedBeerItemStack(int, List)
     */
    @Override
    public void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);

        CompoundTag descriptorTag = new CompoundTag();
        descriptorTag.putInt("beerId", getBeerId());
        descriptorTag.putIntArray("spiceList", getSpiceList());

        tag.put("MixedBeer", descriptorTag);
    }

    @Override
    public void load(@Nonnull CompoundTag tag) {
        super.load(tag);

        CompoundTag descriptorTag = MixedBeerManager.findLegacyDescriptor(tag);
        if (descriptorTag != null) {
            int rawBeerId = descriptorTag.getInt("beerId");
            List<Integer> rawSpices = java.util.Arrays.stream(descriptorTag.getIntArray("spiceList")).boxed().toList();
            int sanitizedBeerId = MixedBeerManager.sanitizeBeerId(rawBeerId);
            List<Integer> sanitizedSpices = MixedBeerManager.sanitizeSpiceIds(rawSpices);
            this.beerId = sanitizedBeerId;
            this.spiceList = new ArrayList<>(sanitizedSpices);
            boolean directLayout = !tag.contains("MixedBeer", Tag.TAG_COMPOUND);
            if (directLayout || rawBeerId != sanitizedBeerId || !rawSpices.equals(sanitizedSpices)) {
                setChanged();
            }
        }
    }

    public void setMixedBeerData(int beerId, List<Integer> spiceList) {
        this.beerId = MixedBeerManager.sanitizeBeerId(beerId);
        this.spiceList = new ArrayList<>(MixedBeerManager.sanitizeSpiceIds(spiceList));
        setChanged();
    }

    @Override
    public void handleUpdateTag(CompoundTag tag) {
        super.handleUpdateTag(tag);
    }

    @Override
    public CompoundTag getUpdateTag() {
        CompoundTag tag = super.getUpdateTag();
        saveAdditional(tag);

        return tag;
    }

    public ItemStack getPickStack() {
        return MixedBeerManager.genMixedBeerItemStack(this.beerId, this.spiceList);
    }

    public List<Integer> getSpiceList() {
        return List.copyOf(spiceList);
    }

    public int getBeerId() {
        return beerId;
    }

    @Override
    public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt) {
        if (pkt.getTag() != null) {
            handleUpdateTag(pkt.getTag());
        }
    }

    @Nullable
    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

}
