package com.keykeeper.dushi.block;

import com.keykeeper.dushi.Dushi;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.InsideBlockEffectApplier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * A hand's depth of standing water over a floor: it looks like water and is not. Nothing is slowed by it, one walks and
 * rolls in it as on the floor under it; whatever moves through it splashes. A map's builder lays it over the floor of a
 * place that is to be fought in "in the water" - the game's own water would stop the roll and halve the pace. It is
 * seen and removed in creative with a mob mark in hand, like the other things a builder sets.
 */
public class ShallowsBlock extends Block {
    private static final VoxelShape SHAPE = Block.box(0.0, 0.0, 0.0, 16.0, 2.0, 16.0);

    public ShallowsBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return context.isHoldingItem(Dushi.MOB_MARK_ITEM.get()) ? SHAPE : Shapes.empty();
    }

    @Override
    protected boolean propagatesSkylightDown(BlockState state) {
        return true;
    }

    @Override
    protected void entityInside(BlockState state, Level level, BlockPos pos, Entity entity, InsideBlockEffectApplier effects, boolean precise) {
        if (level instanceof ServerLevel server && entity instanceof LivingEntity && entity.onGround()
                && (entity.tickCount + entity.getId()) % 3 == 0 && entity.getKnownMovement().horizontalDistanceSqr() > 0.002) {
            server.sendParticles(ParticleTypes.SPLASH, entity.getX(), pos.getY() + 0.15, entity.getZ(), 3, entity.getBbWidth() * 0.3, 0.0,
                    entity.getBbWidth() * 0.3, 0.0);
        }
    }
}
