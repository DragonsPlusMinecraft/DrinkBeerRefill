package lekavar.lma.drinkbeer.blockentities;

import lekavar.lma.drinkbeer.managers.MixedBeerManager;
import lekavar.lma.drinkbeer.registries.BlockEntityRegistry;
import lekavar.lma.drinkbeer.registries.DataComponentTypeRegistry;
import lekavar.lma.drinkbeer.utils.beer.Beers;
import lekavar.lma.drinkbeer.utils.dataComponent.SpiceData;
import lekavar.lma.drinkbeer.utils.mixedbeer.Spices;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.TypedEntityData;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

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
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        ValueOutput descriptor = output.child("MixedBeer");
        descriptor.putInt("beerId", getBeerId());
        descriptor.putIntArray("spiceList", getSpiceList().stream().mapToInt(Integer::intValue).toArray());
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        input.child("MixedBeer").ifPresent(descriptor -> {
            this.beerId = MixedBeerManager.sanitizeBeerId(descriptor.getIntOr("beerId", Beers.DEFAULT_BEER_ID));
            this.spiceList = new ArrayList<>(MixedBeerManager.sanitizeSpiceIds(
                    java.util.Arrays.stream(descriptor.getIntArray("spiceList").orElseGet(() -> new int[0])).boxed().toList()
            ));
        });
    }

    @Override
    protected void applyImplicitComponents(DataComponentGetter componentInput) {
        super.applyImplicitComponents(componentInput);

        // Minecraft converts old BlockEntityTag data into BLOCK_ENTITY_DATA. BlockItem first loads that NBT and
        // then applies item components, whose prototype defaults would otherwise overwrite the migrated values.
        TypedEntityData<?> legacyData = componentInput.get(DataComponents.BLOCK_ENTITY_DATA);
        if (legacyData != null) {
            CompoundTag descriptorTag = MixedBeerManager.findLegacyDescriptor(legacyData.copyTagWithoutId());
            if (descriptorTag != null) {
                this.beerId = MixedBeerManager.sanitizeBeerId(descriptorTag.getIntOr("beerId", Beers.DEFAULT_BEER_ID));
                this.spiceList = new ArrayList<>(MixedBeerManager.sanitizeSpiceIds(
                        java.util.Arrays.stream(descriptorTag.getIntArray("spiceList").orElseGet(() -> new int[0])).boxed().toList()
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
    public void removeComponentsFromTag(ValueOutput output) {
        super.removeComponentsFromTag(output);
        output.discard("MixedBeer");
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return saveCustomOnly(registries);
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
