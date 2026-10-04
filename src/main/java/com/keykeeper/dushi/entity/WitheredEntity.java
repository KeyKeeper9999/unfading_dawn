package com.keykeeper.dushi.entity;

import com.keykeeper.dushi.Dushi;

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
        ARCHER("withered_archer", 16.0, 2.0, 0.26, 0.0, 50);

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
    /** Blows the guard's shield takes before his arm drops. */
    private static final int GUARD_BREAK_HITS = 3;

    private final Kind kind;

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
                    this.equip(EquipmentSlot.MAINHAND, new ItemStack(Dushi.BROKEN_SWORD.get()), 0.08F);
                }
            }
            case GUARD -> {
                this.equip(EquipmentSlot.MAINHAND, new ItemStack(Dushi.SHORTSWORD.get()), 0.06F);
                this.equip(EquipmentSlot.OFFHAND, new ItemStack(this.random.nextInt(3) == 0 ? Dushi.GUARD_SHIELD.get() : Dushi.PLANK_SHIELD.get()), 0.06F);
            }
            case ARCHER -> this.equip(EquipmentSlot.MAINHAND, new ItemStack(Dushi.SHORT_BOW.get()), 0.06F);
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
        return id == SHOOT ? SHOOT_DURATION : SWING_DURATION;
    }

    @Override
    protected int trackUntil(int id) {
        return id == SHOOT ? SHOOT_RELEASE : SWING_IMPACT - 3;
    }

    @Override
    protected int attackPause(int finishedId) {
        return finishedId == SHOOT ? 25 + this.random.nextInt(20) : 14 + this.random.nextInt(10);
    }

    @Override
    public double stopDistance() {
        return this.kind == Kind.ARCHER ? 9.0 : 1.4;
    }

    @Override
    public int chooseAttack(LivingEntity target, double distance) {
        if (this.kind == Kind.ARCHER && distance > 3.0) {
            return distance < 20.0 && this.hasLineOfSight(target) ? SHOOT : 0;
        }
        return distance < 2.1 ? SWING : 0;
    }

    @Override
    public void startAttack(int id) {
        super.startAttack(id);
        this.stopUsingItem();
        if (id == SWING) {
            this.playSound(Dushi.sound("entity.withered.ambient"), 0.8F, 1.3F);
        }
        if (id == SHOOT) {
            this.startUsingItem(InteractionHand.MAIN_HAND);
        }
    }

    @Override
    protected void attackTick(ServerLevel level, int id, int time) {
        if (id == SWING && time == SWING_IMPACT) {
            this.swingArm();
            this.hitArc(level, 2.4, 55.0, (float) this.getAttributeValue(Attributes.ATTACK_DAMAGE)
                    + (this.getMainHandItem().isEmpty() ? 0.0F : 2.0F), 0.4);
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
        }
    }

    @Override
    protected int guardBreakHits() {
        return this.kind == Kind.GUARD ? GUARD_BREAK_HITS : 0;
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
