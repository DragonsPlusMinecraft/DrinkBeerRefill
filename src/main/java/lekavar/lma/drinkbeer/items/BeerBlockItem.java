package lekavar.lma.drinkbeer.items;

import lekavar.lma.drinkbeer.registries.ItemRegistry;
import lekavar.lma.drinkbeer.registries.SoundEventRegistry;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.Block;


public class BeerBlockItem extends BlockItem {
    protected static final double MAX_PLACE_DISTANCE = 2.0D;

    public BeerBlockItem(Block block, Properties properties) {
        super(block, properties);
    }

    @Override
    public SoundEvent getEatingSound() {
        return SoundEventRegistry.DRINKING_BEER.get();
    }

    protected boolean isWithinPlaceDistance(BlockPlaceContext context) {
        Player player = context.getPlayer();
        return player == null || context.getClickLocation().distanceTo(player.position()) <= MAX_PLACE_DISTANCE;
    }

    public void giveEmptyMugBack(LivingEntity user) {
        if (!(user instanceof Player) || !((Player) user).isCreative()) {
            ItemStack emptyMugItemStack = new ItemStack(ItemRegistry.EMPTY_BEER_MUG.get(), 1);
            if (user instanceof Player) {
                if (!((Player) user).addItem(emptyMugItemStack))
                    ((Player) user).drop(emptyMugItemStack, false);
            } else {
                user.spawnAtLocation(emptyMugItemStack);
            }
        }
    }
}
