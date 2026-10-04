package com.keykeeper.dushi.entity;

import java.util.Optional;
import java.util.UUID;

import com.keykeeper.dushi.Dushi;
import com.keykeeper.dushi.Net;
import com.keykeeper.dushi.PlayerSoul;

import net.minecraft.core.UUIDUtil;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ItemSupplier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.network.PacketDistributor;

/**
 * The souls a player dropped by dying: a pale wisp hanging where they fell. Only its owner can take it back by walking into it,
 * and only until they die again: a newer stain makes this one fade.
 */
public class SoulStainEntity extends Entity implements ItemSupplier {
    private static final double TOUCH_RANGE = 1.7;

    private UUID owner;
    private int souls;
    private int generation;

    public SoulStainEntity(EntityType<? extends SoulStainEntity> type, Level level) {
        super(type, level);
        this.noPhysics = true;
        this.setNoGravity(true);
    }

    /** @param at where the stain is left: the place of death, or the last ground under a player who fell */
    public static void drop(ServerLevel level, ServerPlayer player, Vec3 at, int souls, int generation) {
        SoulStainEntity stain = new SoulStainEntity(Dushi.SOUL_STAIN.get(), level);
        stain.owner = player.getUUID();
        stain.souls = souls;
        stain.generation = generation;
        // Never below the world: a fall into the void leaves the stain at the bottom edge.
        double y = Math.max(at.y, level.getMinY() + 1.0);
        stain.setPos(at.x, y + 0.6, at.z);
        level.addFreshEntity(stain);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
    }

    @Override
    public void tick() {
        super.tick();
        if (!(this.level() instanceof ServerLevel level)) {
            return;
        }
        if (this.owner == null || this.souls <= 0) {
            this.discard();
            return;
        }
        if (this.tickCount % 6 == 0) {
            level.sendParticles(ParticleTypes.SOUL, this.getX(), this.getY() + 0.2, this.getZ(), 1, 0.15, 0.15, 0.15, 0.01);
        }
        Player player = level.getPlayerByUUID(this.owner);
        if (!(player instanceof ServerPlayer serverPlayer) || !player.isAlive()) {
            return;
        }
        PlayerSoul soul = PlayerSoul.of(player);
        if (soul.stainGeneration != this.generation) {
            level.sendParticles(ParticleTypes.SMOKE, this.getX(), this.getY() + 0.2, this.getZ(), 12, 0.2, 0.2, 0.2, 0.01);
            this.discard();
            return;
        }
        if (player.distanceToSqr(this.getX(), this.getY() - 0.6, this.getZ()) <= TOUCH_RANGE * TOUCH_RANGE && !player.isSpectator()) {
            soul.souls += this.souls;
            soul.lostSouls = 0;
            serverPlayer.syncData(Dushi.PLAYER_SOUL);
            PacketDistributor.sendToPlayer(serverPlayer, new Net.Banner("banner.vsratie_dushi.souls_retrieved", Net.Banner.PALE, "ui.souls_retrieve"));
            level.sendParticles(ParticleTypes.SOUL, this.getX(), this.getY() + 0.2, this.getZ(), 30, 0.3, 0.4, 0.3, 0.06);
            this.discard();
        }
    }

    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float damage) {
        return false;
    }

    @Override
    public boolean isPickable() {
        return false;
    }

    @Override
    public boolean isPushable() {
        return false;
    }

    @Override
    public boolean isAttackable() {
        return false;
    }

    @Override
    public ItemStack getItem() {
        return new ItemStack(Dushi.SOUL_STAIN_ITEM.get());
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        output.storeNullable("Owner", UUIDUtil.CODEC, this.owner);
        output.putInt("Souls", this.souls);
        output.putInt("Generation", this.generation);
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        Optional<UUID> owner = input.read("Owner", UUIDUtil.CODEC);
        this.owner = owner.orElse(null);
        this.souls = input.getIntOr("Souls", 0);
        this.generation = input.getIntOr("Generation", 0);
    }
}
