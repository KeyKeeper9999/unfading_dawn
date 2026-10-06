package com.keykeeper.dushi.entity;

import com.keykeeper.dushi.Dushi;

import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

/** Every flying spell of players and bosses: the kind decides the look, the gravity and what happens on impact. */
public class SpellBoltEntity extends ThrowableItemProjectile {
    public enum Kind {
        SHARD("shard", 0.0, 0.0),
        HEAVY_SHARD("heavy_shard", 0.0, 0.0),
        FIRE_ORB("fire_orb", 0.045, 3.0),
        /** The chaplain's spear of light. */
        LIGHT_SPEAR("light_spear", 0.0, 0.0),
        /** The chaplain's second phase: black fire that bursts. */
        DARK_FLAME("dark_flame", 0.0, 2.2),
        /** What a wisp of the Mire throws: a slow tongue of its own green fire. */
        WISP_FLAME("wisp_flame", 0.0, 0.0);

        public final String id;
        public final double gravity;
        /** Radius of the burst on impact, 0 for a single target. */
        public final double burst;

        Kind(String id, double gravity, double burst) {
            this.id = id;
            this.gravity = gravity;
            this.burst = burst;
        }
    }

    private static final EntityDataAccessor<Integer> KIND = SynchedEntityData.defineId(SpellBoltEntity.class, EntityDataSerializers.INT);
    private static final int LIFETIME = 80;

    private float damage = 4.0F;

    public SpellBoltEntity(EntityType<? extends SpellBoltEntity> type, Level level) {
        super(type, level);
    }

    private SpellBoltEntity(Level level, LivingEntity owner, Kind kind, float damage) {
        super(Dushi.SPELL_BOLT.get(), owner, level, new ItemStack(Dushi.BOLT_ITEMS.get(kind).get()));
        this.entityData.set(KIND, kind.ordinal());
        this.damage = damage;
    }

    /** Fires a bolt from the eyes of {@code owner} along {@code direction}. */
    public static SpellBoltEntity shoot(ServerLevel level, LivingEntity owner, Kind kind, Vec3 direction, float speed, float damage) {
        SpellBoltEntity bolt = new SpellBoltEntity(level, owner, kind, damage);
        Vec3 dir = direction.normalize();
        bolt.setPos(owner.getX() + dir.x * 0.6, owner.getEyeY() - 0.15 + dir.y * 0.6, owner.getZ() + dir.z * 0.6);
        bolt.shoot(dir.x, dir.y, dir.z, speed, 0.0F);
        level.addFreshEntity(bolt);
        return bolt;
    }

    public Kind kind() {
        int index = this.entityData.get(KIND);
        return Kind.values()[Math.floorMod(index, Kind.values().length)];
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(KIND, 0);
    }

    @Override
    protected Item getDefaultItem() {
        return Dushi.BOLT_ITEMS.get(Kind.SHARD).get();
    }

    @Override
    protected double getDefaultGravity() {
        return this.kind().gravity;
    }

    /** Magic does not slow down in the air. */
    @Override
    protected float getAirDrag() {
        return 1.0F;
    }

    @Override
    public void tick() {
        super.tick();
        if (this.level() instanceof ServerLevel level) {
            if (this.tickCount > LIFETIME) {
                this.discard();
                return;
            }
            Kind kind = this.kind();
            ParticleOptions trail = switch (kind) {
                case SHARD, HEAVY_SHARD -> ParticleTypes.SOUL_FIRE_FLAME;
                case FIRE_ORB -> ParticleTypes.FLAME;
                case LIGHT_SPEAR -> ParticleTypes.END_ROD;
                case DARK_FLAME -> ParticleTypes.LARGE_SMOKE;
                case WISP_FLAME -> ParticleTypes.GLOW;
            };
            level.sendParticles(trail, this.getX(), this.getY() + 0.15, this.getZ(), kind == Kind.HEAVY_SHARD ? 3 : 1, 0.05, 0.05, 0.05, 0.0);
        }
    }

    @Override
    protected boolean canHitEntity(Entity entity) {
        // A boss's bolts fly through its own summoned helpers.
        if (this.getOwner() instanceof SoulsMob && entity instanceof SoulsMob) {
            return false;
        }
        return super.canHitEntity(entity) && !(entity instanceof SpellBoltEntity);
    }

    @Override
    protected void onHitEntity(EntityHitResult hitResult) {
        super.onHitEntity(hitResult);
        if (this.level() instanceof ServerLevel level && this.kind().burst == 0.0) {
            this.strike(level, hitResult.getEntity(), this.damage);
        }
    }

    private void strike(ServerLevel level, Entity target, float amount) {
        Entity owner = this.getOwner();
        DamageSource source = this.damageSources().indirectMagic(this, owner);
        Kind kind = this.kind();
        if (target.hurtServer(level, source, amount) && (kind == Kind.FIRE_ORB || kind == Kind.DARK_FLAME)) {
            target.igniteForSeconds(4.0F);
        }
    }

    @Override
    protected void onHit(HitResult hitResult) {
        super.onHit(hitResult);
        if (!(this.level() instanceof ServerLevel level)) {
            return;
        }
        Vec3 at = hitResult.getLocation();
        Kind kind = this.kind();
        if (kind.burst > 0.0) {
            Entity owner = this.getOwner();
            for (LivingEntity target : level.getEntitiesOfClass(LivingEntity.class, this.getBoundingBox().inflate(kind.burst),
                    e -> e != owner && e.isAlive() && !(e instanceof ArmorStand) && e.distanceToSqr(at) <= (kind.burst + 0.5) * (kind.burst + 0.5))) {
                if (owner instanceof SoulsMob && target instanceof SoulsMob) {
                    continue;
                }
                if (owner instanceof Player player && target instanceof Player other && !player.canHarmPlayer(other)) {
                    continue;
                }
                this.strike(level, target, this.damage);
            }
            level.sendParticles(kind == Kind.FIRE_ORB ? ParticleTypes.FLAME : ParticleTypes.LARGE_SMOKE, at.x, at.y + 0.2, at.z, 60, kind.burst * 0.4,
                    0.4, kind.burst * 0.4, 0.06);
            level.sendParticles(ParticleTypes.LAVA, at.x, at.y + 0.2, at.z, 10, kind.burst * 0.3, 0.2, kind.burst * 0.3, 0.0);
            level.playSound(null, at.x, at.y, at.z, Dushi.sound("item.spell.fire_burst"), SoundSource.NEUTRAL, 1.4F, kind == Kind.DARK_FLAME ? 0.7F : 1.0F);
        } else {
            level.sendParticles(kind == Kind.LIGHT_SPEAR ? ParticleTypes.END_ROD : kind == Kind.WISP_FLAME ? ParticleTypes.GLOW : ParticleTypes.SOUL_FIRE_FLAME,
                    at.x, at.y, at.z, 12, 0.15, 0.15, 0.15, 0.05);
            level.playSound(null, at.x, at.y, at.z, Dushi.sound("item.spell.shard_hit"), SoundSource.NEUTRAL, 0.9F, kind == Kind.HEAVY_SHARD ? 0.7F : 1.1F);
        }
        this.discard();
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        output.putInt("Kind", this.entityData.get(KIND));
        output.putFloat("Damage", this.damage);
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        this.entityData.set(KIND, input.getIntOr("Kind", 0));
        this.damage = input.getFloatOr("Damage", 4.0F);
    }
}
