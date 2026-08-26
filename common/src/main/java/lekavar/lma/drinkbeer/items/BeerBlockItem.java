package lekavar.lma.drinkbeer.items;

import lekavar.lma.drinkbeer.registries.ItemRegistry;
import lekavar.lma.drinkbeer.registries.SoundEventRegistry;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.Consumable;
import net.minecraft.world.level.block.Block;
import org.joml.Vector3d;

import static java.lang.Math.pow;
import static net.minecraft.util.Mth.sqrt;


public class BeerBlockItem extends BlockItem implements Consumable.OverrideConsumeSound {
    protected final static float MAX_PLACE_DISTANCE = (float) 2;

    public BeerBlockItem(Block block, Properties properties) {
        super(block, properties);
    }

    protected static Properties propertiesFor(Block block) {
        Identifier blockId = BuiltInRegistries.BLOCK.getKey(block);
        if (blockId == null) {
            throw new IllegalStateException("Block must be registered before its item is constructed");
        }
        return new Properties().setId(ResourceKey.create(Registries.ITEM, blockId)).useBlockDescriptionPrefix();
    }

    public SoundEvent getEatingSound() {
        return SoundEventRegistry.DRINKING_BEER.get();
    }

    @Override
    public SoundEvent getConsumeSound(ItemStack stack) {
        return getEatingSound();
    }

    public float getDistance(Vector3d p1, Vector3d p2) {
        return sqrt((float) (pow(p1.x - p2.x, 2) + pow(p1.y - p2.y, 2) + pow(p1.z - p2.z, 2)));
    }

    public void giveEmptyMugBack(LivingEntity user) {
        if (!(user instanceof Player) || !((Player) user).isCreative()) {
            ItemStack emptyMugItemStack = new ItemStack(ItemRegistry.EMPTY_BEER_MUG.get(), 1);
            if (user instanceof Player) {
                if (!((Player) user).addItem(emptyMugItemStack))
                    ((Player) user).drop(emptyMugItemStack, false);
            } else if (user.level() instanceof ServerLevel serverLevel) {
                user.spawnAtLocation(serverLevel, emptyMugItemStack);
            }
        }
    }
}
