package lekavar.lma.drinkbeer.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class StarOfBethlehemBlock extends Block {
    private static final VoxelShape BASE = box(7.0, 0.0, 7.0, 9.0, 4.0, 9.0);
    private static final VoxelShape STAR = box(2.0, 4.0, 2.0, 14.0, 16.0, 14.0);
    private static final VoxelShape REGULAR_SHAPE = Shapes.or(BASE, STAR);
    private static final VoxelShape GREAT_SHAPE = Shapes.block();

    private final boolean great;

    public StarOfBethlehemBlock(Properties properties, boolean great) {
        super(properties);
        this.great = great;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return great ? GREAT_SHAPE : REGULAR_SHAPE;
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        double x = pos.getX() + random.nextDouble();
        double y = pos.getY() + 0.4D + random.nextDouble() / 2.0D;
        double z = pos.getZ() + random.nextDouble();
        level.addParticle(ParticleTypes.FIREWORK, x, y, z, 0.0D, 0.0D, 0.0D);
    }
}
