package com.keykeeper.dushi.entity;

import com.keykeeper.dushi.ArmorSet;
import com.keykeeper.dushi.Dushi;
import com.keykeeper.dushi.Spell;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.level.ServerLevelAccessor;

/**
 * The withered: prisoners of the stockade whose minds burned out long ago. Three kinds share this class:
 * the prisoner hits with bare hands or a broken blade, the guard hides behind a shield and opens up only to strike,
 * the archer keeps away and shoots.
 */
public class WitheredEntity extends SoulsMob {
    public enum Kind {
        PRISONER("withered_prisoner", 14.0, 3.0, 0.27, 0.0, 20),
        GUARD("withered_guard", 24.0, 5.0, 0.25, 4.0, 60),
        ARCHER("withered_archer", 16.0, 2.0, 0.26, 0.0, 50),
        /** A novice of the chapel in what is left of his habit: keeps away and throws slow spears of light. */
        ACOLYTE("withered_acolyte", 20.0, 2.0, 0.25, 0.0, 90),
        /** One of the townsfolk of the borough: bare hands, a broken blade or a pitchfork. */
        TOWNSMAN("withered_townsman", 12.0, 3.0, 0.26, 0.0, 15),
        /** A butcher of the shambles: a big man, slow to come, whose cleaver goes round level and clears the lane before him. */
        BUTCHER("withered_butcher", 28.0, 4.0, 0.24, 2.0, 70),
        /** The night watch: a lantern, a pitchfork held like a pike, and a rattle that brings the street down on whoever he sees. */
        WATCHMAN("withered_watchman", 20.0, 4.0, 0.25, 2.0, 55);

        public final String id;
        final double health;
        final double damage;
        final double speed;
        final double armor;
        final int souls;

        Kind(String id, double health, double damage, double speed, double armor, int souls) {
            this.id = id;
            this.health = health;
            this.damage = damage;
            this.speed = speed;
            this.armor = armor;
            this.souls = souls;
        }
    }

    /** The swing: the arm goes up for nine ticks, the blow lands on the tenth. */
    public static final int SWING = 1;
    public static final int SWING_DURATION = 20;
    private static final int SWING_IMPACT = 10;
    /** The archer's shot: draws for a second, then lets go. */
    public static final int SHOOT = 2;
    public static final int SHOOT_DURATION = 34;
    private static final int SHOOT_RELEASE = 24;
    /** The acolyte's spear of light: the hand goes up, glows, and throws. */
    public static final int CAST = 3;
    public static final int CAST_DURATION = 44;
    private static final int CAST_RELEASE = 26;
    /** The butcher's cut: drawn far back, then round level - wide, heavy, and a long time coming and going. */
    public static final int CLEAVE = 4;
    public static final int CLEAVE_DURATION = 38;
    public static final int CLEAVE_IMPACT = 16;
    /** A thrust with a pitchfork: longer than an arm, narrow, easy to step round. */
    public static final int THRUST = 5;
    public static final int THRUST_DURATION = 28;
    public static final int THRUST_IMPACT = 15;
    /** How much bigger than the others the butcher is drawn (his box is made to match where it is registered). */
    public static final float BUTCHER_SIZE = 1.1F;
    /** The watchman's rattle: everyone of the dead within earshot comes. */
    public static final int ALARM = 6;
    public static final int ALARM_DURATION = 34;
    private static final int ALARM_SOUND = 12;
    private static final double ALARM_RANGE = 18.0;
    /** Blows the guard's shield takes before his arm drops. */
    private static final int GUARD_BREAK_HITS = 3;

    private final Kind kind;
    /** A watchman sounds his rattle once for every time he takes notice of somebody. */
    private boolean alarmed;

    public WitheredEntity(EntityType<? extends WitheredEntity> type, Level level, Kind kind) {
        super(type, level);
        this.kind = kind;
        this.xpReward = 5;
    }

    public Kind kind() {
        return this.kind;
    }

    public static AttributeSupplier.Builder createAttributes(Kind kind) {
        return Monster.createMonsterAttributes().add(Attributes.MAX_HEALTH, kind.health).add(Attributes.ATTACK_DAMAGE, kind.damage)
                .add(Attributes.MOVEMENT_SPEED, kind.speed).add(Attributes.ARMOR, kind.armor).add(Attributes.FOLLOW_RANGE, 24.0);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(2, new SoulsCombatGoal(this, 1.0));
        this.goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 0.7));
        this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(7, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, EntitySpawnReason spawnReason, SpawnGroupData groupData) {
        SpawnGroupData data = super.finalizeSpawn(level, difficulty, spawnReason, groupData);
        switch (this.kind) {
            case PRISONER -> {
                if (this.random.nextBoolean()) {
                    this.equip(EquipmentSlot.MAINHAND, new ItemStack(Dushi.BROKEN_SWORD.get()), 0.15F);
                }
            }
            case GUARD -> {
                this.equip(EquipmentSlot.MAINHAND, new ItemStack(Dushi.SHORTSWORD.get()), 0.12F);
                this.equip(EquipmentSlot.OFFHAND, new ItemStack(this.random.nextInt(3) == 0 ? Dushi.GUARD_SHIELD.get() : Dushi.PLANK_SHIELD.get()), 0.12F);
            }
            case ARCHER -> this.equip(EquipmentSlot.MAINHAND, new ItemStack(Dushi.SHORT_BOW.get()), 0.1F);
            case ACOLYTE -> this.equip(EquipmentSlot.OFFHAND, new ItemStack(Dushi.PRAYER_BEADS.get()), 0.08F);
            case TOWNSMAN -> {
                int roll = this.random.nextInt(10);
                if (roll < 4) {
                    this.equip(EquipmentSlot.MAINHAND, new ItemStack(Dushi.PITCHFORK.get()), 0.06F);
                } else if (roll < 6) {
                    this.equip(EquipmentSlot.MAINHAND, new ItemStack(Dushi.BROKEN_SWORD.get()), 0.1F);
                }
            }
            case BUTCHER -> this.equip(EquipmentSlot.MAINHAND, new ItemStack(Dushi.CLEAVER.get()), 0.08F);
            case WATCHMAN -> {
                this.equip(EquipmentSlot.MAINHAND, new ItemStack(Dushi.PITCHFORK.get()), 0.06F);
                this.equip(EquipmentSlot.OFFHAND, new ItemStack(Items.LANTERN), 0.0F);
            }
        }
        return data;
    }

    private void equip(EquipmentSlot slot, ItemStack stack, float dropChance) {
        this.setItemSlot(slot, stack);
        this.setDropChance(slot, dropChance);
    }

    @Override
    public int soulReward() {
        return this.kind.souls;
    }

    @Override
    protected int attackDuration(int id) {
        return switch (id) {
            case SHOOT -> SHOOT_DURATION;
            case CAST -> CAST_DURATION;
            case CLEAVE -> CLEAVE_DURATION;
            case THRUST -> THRUST_DURATION;
            case ALARM -> ALARM_DURATION;
            default -> SWING_DURATION;
        };
    }

    @Override
    protected int trackUntil(int id) {
        return switch (id) {
            case SHOOT -> SHOOT_RELEASE;
            case CAST -> CAST_RELEASE;
            case CLEAVE -> CLEAVE_IMPACT - 6;
            case THRUST -> THRUST_IMPACT - 5;
            case ALARM -> ALARM_DURATION;
            default -> SWING_IMPACT - 3;
        };
    }

    @Override
    protected int attackPause(int finishedId) {
        if (finishedId == CLEAVE) {
            return 22 + this.random.nextInt(10);             // he has to gather himself after it
        }
        return finishedId == SHOOT || finishedId == CAST ? 25 + this.random.nextInt(20) : 14 + this.random.nextInt(10);
    }

    @Override
    public double stopDistance() {
        return this.kind == Kind.ARCHER ? 9.0 : this.kind == Kind.ACOLYTE ? 8.0 : this.hasPike() ? 2.3 : 1.4;
    }

    /** Whether it holds something to thrust with. */
    private boolean hasPike() {
        return this.getMainHandItem().is(Dushi.PITCHFORK.get());
    }

    @Override
    protected void onReset() {
        super.onReset();
        this.alarmed = false;
    }

    @Override
    public int chooseAttack(LivingEntity target, double distance) {
        if (this.kind == Kind.ARCHER && distance > 3.0) {
            return distance < 20.0 && this.hasLineOfSight(target) ? SHOOT : 0;
        }
        if (this.kind == Kind.ACOLYTE && distance > 3.0) {
            return distance < 18.0 && this.hasLineOfSight(target) ? CAST : 0;
        }
        if (this.kind == Kind.WATCHMAN && !this.alarmed && distance < 14.0 && this.hasLineOfSight(target)) {
            this.alarmed = true;
            return ALARM;
        }
        if (this.kind == Kind.BUTCHER) {
            return distance < 2.5 ? (this.random.nextInt(10) < 7 ? CLEAVE : SWING) : 0;
        }
        if (this.hasPike()) {
            return distance < 3.1 ? THRUST : 0;
        }
        return distance < 2.1 ? SWING : 0;
    }

    @Override
    public void startAttack(int id) {
        super.startAttack(id);
        this.stopUsingItem();
        if (id == SWING || id == CLEAVE || id == THRUST) {
            this.playSound(Dushi.sound("entity.withered.ambient"), 0.8F, this.kind == Kind.BUTCHER ? 0.8F : 1.3F);
        }
        if (id == SHOOT) {
            this.startUsingItem(InteractionHand.MAIN_HAND);
        }
        if (id == CAST) {
            this.playSound(Dushi.sound("entity.chaplain.ambient"), 0.9F, 1.5F);
        }
    }

    @Override
    protected void attackTick(ServerLevel level, int id, int time) {
        if (id == SWING && time == SWING_IMPACT) {
            this.swingArm();
            this.hitArc(level, 2.4, 55.0, (float) this.getAttributeValue(Attributes.ATTACK_DAMAGE)
                    + (this.getMainHandItem().isEmpty() ? 0.0F : 2.0F), 0.4);
        } else if (id == CLEAVE) {
            // (no swing of the game's own here or in the thrust: it would be laid over the move and blur it)
            if (time == CLEAVE_IMPACT - 2) {
                Vec3 step = this.facing().scale(0.3);
                this.setDeltaMovement(step.x, this.getDeltaMovement().y, step.z);
                this.playSound(Dushi.sound("player.weapon_swing_heavy"), 0.9F, 1.15F);
            }
            if (time == CLEAVE_IMPACT) {
                this.hitArc(level, 2.8, 85.0, (float) this.getAttributeValue(Attributes.ATTACK_DAMAGE) + 4.0F, 1.0);
            }
        } else if (id == THRUST) {
            if (time == THRUST_IMPACT - 2) {
                // the step that carries the thrust
                Vec3 step = this.facing().scale(0.45);
                this.setDeltaMovement(step.x, this.getDeltaMovement().y, step.z);
                this.playSound(Dushi.sound("player.weapon_swing_heavy"), 0.6F, 1.7F);
            }
            if (time == THRUST_IMPACT) {
                this.hitArc(level, 3.8, 17.0, (float) this.getAttributeValue(Attributes.ATTACK_DAMAGE) + 2.0F, 0.6);
            }
        } else if (id == ALARM && time == ALARM_SOUND) {
            this.playSound(Dushi.sound("entity.watchman.alarm"), 3.0F, 1.0F);
            level.sendParticles(ParticleTypes.NOTE, this.getX(), this.getY() + this.getBbHeight() + 0.6, this.getZ(), 4, 0.3, 0.2, 0.3, 0.0);
            LivingEntity target = this.getTarget();
            if (target != null) {
                for (SoulsMob other : level.getEntitiesOfClass(SoulsMob.class, this.getBoundingBox().inflate(ALARM_RANGE, 6.0, ALARM_RANGE),
                        mob -> mob != this && mob.isAlive() && !mob.isBoss() && mob.getTarget() == null)) {
                    other.setTarget(target);
                }
            }
        } else if (id == SHOOT && time == SHOOT_RELEASE) {
            this.stopUsingItem();
            LivingEntity target = this.getTarget();
            if (target != null) {
                ItemStack weapon = this.getMainHandItem();
                AbstractArrow arrow = ProjectileUtil.getMobArrow(this, new ItemStack(Items.ARROW), 0.9F, weapon);
                double dx = target.getX() - this.getX();
                double dy = target.getY(0.3333333333333333) - arrow.getY();
                double dz = target.getZ() - this.getZ();
                double horizontal = Math.sqrt(dx * dx + dz * dz);
                arrow.shoot(dx, dy + horizontal * 0.2, dz, 1.5F, 5.0F);
                arrow.setBaseDamage(2.5);
                level.addFreshEntity(arrow);
                this.playSound(SoundEvents.ARROW_SHOOT, 1.0F, 0.9F + this.random.nextFloat() * 0.2F);
            }
        } else if (id == CAST) {
            if (time < CAST_RELEASE && time % 2 == 0) {
                // the light gathers in the raised hand: the warning
                level.sendParticles(ParticleTypes.END_ROD, this.getX(), this.getY() + this.getBbHeight() + 0.5, this.getZ(), 1, 0.15, 0.15, 0.15, 0.0);
            }
            LivingEntity target = this.getTarget();
            if (time == CAST_RELEASE && target != null) {
                Vec3 from = this.getEyePosition();
                SpellBoltEntity.shoot(level, this, SpellBoltEntity.Kind.LIGHT_SPEAR, target.getBoundingBox().getCenter().subtract(from), 0.9F, 5.0F);
                this.playSound(Dushi.sound("entity.chaplain.cast"), 1.0F, 1.3F);
            }
        }
    }

    @Override
    protected int guardBreakHits() {
        return this.kind == Kind.GUARD ? GUARD_BREAK_HITS : 0;
    }

    @Override
    protected void dropCustomDeathLoot(ServerLevel level, DamageSource source, boolean killedByPlayer) {
        super.dropCustomDeathLoot(level, source, killedByPlayer);
        if (this.random.nextInt(14) == 0) {
            this.spawnAtLocation(level, new ItemStack(Dushi.NAMELESS_SOUL.get()));
        }
        switch (this.kind) {
            case PRISONER -> {
                this.leave(level, Dushi.TATTERED_CLOTH.get(), 0, 2);
                this.leavePiece(level, ArmorSet.PRISONER, 0.05F);
                this.leaveMaybe(level, Dushi.SHIV.get(), 0.05F);
                this.leaveMaybe(level, Dushi.DAGGER.get(), 0.03F);
            }
            case ACOLYTE -> {
                this.leave(level, Dushi.TATTERED_CLOTH.get(), 1, 2);
                this.leaveMaybe(level, Dushi.NAMELESS_SOUL.get(), 0.2F);
                this.leaveMaybe(level, Dushi.SPELLS.get(this.random.nextBoolean() ? Spell.REBUKE : Spell.SOUL_SHARD).get(), 0.05F);
                this.leavePiece(level, ArmorSet.CHAPLAIN, 0.03F);
            }
            case GUARD -> {
                this.leave(level, Dushi.RUST_SCRAP.get(), 0, 2);
                this.leavePiece(level, ArmorSet.GUARD, 0.08F);
                this.leaveMaybe(level, Dushi.HATCHET.get(), 0.04F);
                this.leaveMaybe(level, Dushi.LONGSWORD.get(), 0.03F);
                if (this.random.nextInt(10) == 0) {
                    this.spawnAtLocation(level, new ItemStack(Dushi.TAR_POT.get()));
                }
            }
            case TOWNSMAN -> {
                this.leave(level, Dushi.TATTERED_CLOTH.get(), 0, 2);
                this.leavePiece(level, ArmorSet.PRISONER, 0.05F);
            }
            case BUTCHER -> {
                this.leave(level, Dushi.RUST_SCRAP.get(), 0, 2);
                this.leave(level, Dushi.TATTERED_CLOTH.get(), 0, 1);
                this.leaveMaybe(level, Dushi.NAMELESS_SOUL.get(), 0.1F);
                this.leaveMaybe(level, Dushi.HATCHET.get(), 0.03F);
            }
            case WATCHMAN -> {
                this.leave(level, Dushi.RUST_SCRAP.get(), 0, 2);
                this.leavePiece(level, ArmorSet.GUARD, 0.05F);
                if (this.random.nextInt(12) == 0) {
                    this.spawnAtLocation(level, new ItemStack(Dushi.TAR_POT.get()));
                }
            }
            case ARCHER -> {
                this.leave(level, Items.ARROW, 0, 3);
                this.leave(level, Dushi.TATTERED_CLOTH.get(), 0, 1);
                this.leavePiece(level, ArmorSet.PRISONER, 0.03F);
            }
        }
    }

    @Override
    public float getVoicePitch() {
        return this.kind == Kind.BUTCHER ? 0.72F + this.random.nextFloat() * 0.1F : super.getVoicePitch();
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return Dushi.sound("entity.withered.ambient");
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return Dushi.sound("entity.withered.hurt");
    }

    @Override
    protected SoundEvent getDeathSound() {
        return Dushi.sound("entity.withered.death");
    }
}
