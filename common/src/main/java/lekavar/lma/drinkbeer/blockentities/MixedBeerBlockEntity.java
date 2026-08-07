package lekavar.lma.drinkbeer.blockentities;

import lekavar.lma.drinkbeer.managers.MixedBeerManager;
import lekavar.lma.drinkbeer.registries.BlockEntityRegistry;
import lekavar.lma.drinkbeer.registries.DataComponentTypeRegistry;
import lekavar.lma.drinkbeer.utils.beer.Beers;
import lekavar.lma.drinkbeer.utils.dataComponent.SpiceData;
import lekavar.lma.drinkbeer.utils.mixedbeer.Spices;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
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
    public void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag,registries);

        CompoundTag descriptorTag = new CompoundTag();
        descriptorTag.putInt("beerId", getBeerId());
        descriptorTag.putIntArray("spiceList", getSpiceList());

        tag.put("MixedBeer", descriptorTag);
    }

    @Override
    public void loadAdditional(@Nonnull CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag,registries);

        if (tag.contains("MixedBeer", Tag.TAG_COMPOUND)) {
            CompoundTag descriptorTag = tag.getCompound("MixedBeer");
            this.beerId = MixedBeerManager.sanitizeBeerId(descriptorTag.getInt("beerId"));
            this.spiceList = new ArrayList<>(MixedBeerManager.sanitizeSpiceIds(
                    java.util.Arrays.stream(descriptorTag.getIntArray("spiceList")).boxed().toList()
            ));
        }
    }

    @Override
    protected void applyImplicitComponents(DataComponentInput componentInput) {
        super.applyImplicitComponents(componentInput);

        // Minecraft converts old BlockEntityTag data into BLOCK_ENTITY_DATA. BlockItem first loads that NBT and
        // then applies item components, whose prototype defaults would otherwise overwrite the migrated values.
        CustomData legacyData = componentInput.get(DataComponents.BLOCK_ENTITY_DATA);
        if (legacyData != null) {
            CompoundTag descriptorTag = MixedBeerManager.findLegacyDescriptor(legacyData.copyTag());
            if (descriptorTag != null) {
                this.beerId = MixedBeerManager.sanitizeBeerId(descriptorTag.getInt("beerId"));
                this.spiceList = new ArrayList<>(MixedBeerManager.sanitizeSpiceIds(
                        java.util.Arrays.stream(descriptorTag.getIntArray("spiceList")).boxed().toList()
                ));
                return;
            }
        }

        this.beerId = MixedBeerManager.sanitizeBeerId(
                componentInput.getOrDefault(DataComponentTypeRegistry.BEER_ID_COMPONENT.get(), Beers.DEFAULT_BEER_ID)
        );
        SpiceData spiceData = componentInput.getOrDefault(
                DataComponentTypeRegistry.SPICE_COMPONENT.get(),
                new SpiceData(Spices.EMPTY_SPICE_ID, Spices.EMPTY_SPICE_ID, Spices.EMPTY_SPICE_ID)
        );
        this.spiceList = new ArrayList<>(MixedBeerManager.sanitizeSpiceIds(spiceData.toSpiceList()));
    }

    @Override
    protected void collectImplicitComponents(DataComponentMap.Builder components) {
        super.collectImplicitComponents(components);
        components.set(DataComponentTypeRegistry.BEER_ID_COMPONENT.get(), this.beerId);
        components.set(DataComponentTypeRegistry.SPICE_COMPONENT.get(), SpiceData.fromSpiceList(this.spiceList));
    }

    @Override
    @SuppressWarnings("deprecation")
    public void removeComponentsFromTag(CompoundTag tag) {
        super.removeComponentsFromTag(tag);
        tag.remove("MixedBeer");
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        CompoundTag tag = super.getUpdateTag(registries);
        saveAdditional(tag,registries);

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

    @Nullable
    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

}
