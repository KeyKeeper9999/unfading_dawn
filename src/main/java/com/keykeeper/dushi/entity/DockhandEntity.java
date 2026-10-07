package com.keykeeper.dushi.entity;

import com.keykeeper.dushi.Dushi;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
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
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/**
 * A dockhand of the Morning Wharf: one of those who carried the cargoes up from the ships on their backs, a sack over
 * head and shoulders and a pad for the load, and in his hand the hook the bales are taken with. There has been
 * nothing to carry since that morning. He is quick, and he does what he knows.
 * <p>
 * The hook comes over his shoulder into whatever is before him - and whatever it takes is hauled in to him like a
 * bale, into the reach of the next. Close to, he rips with it across: once, or there and back.
 */
public class DockhandEntity extends ShoremanEntity {
    public static final int HOOK = 1;
    public static final int HOOK_DURATION = 38;
    public static final int HOOK_IMPACT = 16;
    /** By this tick the one hooked has been hauled in. */
    public static final int HOOK_PULLED = 24;
    public static final int RIP = 2;
    public static final int RIP_DURATION = 30;
    public static final int RIP_IMPACT = 12;
    public static final int RIPS = 3;
    public static final int RIPS_DURATION = 46;
    public static final int RIPS_FIRST = 12;
    public static final int RIPS_SECOND = 26;
    /** How far the hook reaches: further than a rip. */
    public static final double HOOK_REACH = 3.6;

    public DockhandEntity(EntityType<? extends DockhandEntity> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes().add(Attributes.MAX_HEALTH, 170.0).add(Attributes.ATTACK_DAMAGE, 20.0)
                .add(Attributes.MOVEMENT_SPEED, 0.3).add(Attributes.ARMOR, 2.0).add(Attributes.FOLLOW_RANGE, 22.0);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(2, new SoulsCombatGoal(this, 1.0));
        this.goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 0.6));
        this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 10.0F));
        this.goalSelector.addGoal(7, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    @Override
    public int soulReward() {
        return 800;
    }

    @Override
    protected int attackDuration(int id) {
        return id == HOOK ? HOOK_DURATION : id == RIP ? RIP_DURATION : RIPS_DURATION;
    }

    @Override
    protected int trackUntil(int id) {
        return id == HOOK ? HOOK_IMPACT - 6 : id == RIP ? RIP_IMPACT - 4 : RIPS_SECOND - 4;
    }

    @Override
    protected int attackPause(int finishedId) {
        return finishedId == HOOK ? 6 : 14 + this.random.nextInt(10);
    }

    @Override
    public double stopDistance() {
        return 1.6;
    }

    @Override
    public int chooseAttack(LivingEntity target, double distance) {
        if (distance < 2.2) {
            return this.random.nextInt(3) == 0 ? RIPS : RIP;
        }
        if (distance < HOOK_REACH - 0.3 && this.random.nextInt(4) != 0) {
            return HOOK;
        }
        return 0;
    }

    @Override
    protected void struck(ServerLevel level, LivingEntity victim) {
        if (this.attackId() == HOOK) {
            // hauled in: thrown towards him, to land at his feet
            Vec3 to = new Vec3(this.getX() - victim.getX(), 0.0, this.getZ() - victim.getZ());
            double far = to.length();
            if (far > 1.2) {
                shove(victim, to.scale(Math.min(0.9, far * 0.28) / far).add(0.0, 0.26, 0.0));
            }
        }
    }

    @Override
    protected void attackTick(ServerLevel level, int id, int time) {
        float damage = (float) this.getAttributeValue(Attributes.ATTACK_DAMAGE);
        if (id == HOOK) {
            if (time == HOOK_IMPACT - 3) {
                this.playSound(Dushi.sound("player.weapon_swing_heavy"), 0.9F, 1.3F);
            }
            if (time == HOOK_IMPACT && this.hitArc(level, HOOK_REACH, 24.0, damage * 0.8F, 0.0) > 0) {
                this.playSound(Dushi.sound("entity.sailor.hook"), 1.2F, 1.0F);
            }
        } else if (time == RIP_IMPACT && id == RIP || id == RIPS && (time == RIPS_FIRST || time == RIPS_SECOND)) {
            this.playSound(Dushi.sound("player.weapon_swing_heavy"), 0.9F, 1.2F);
            this.hitArc(level, 2.5, 70.0, damage * (id == RIPS ? 0.8F : 1.0F), 0.5);
        }
    }

    @Override
    protected void dropCustomDeathLoot(ServerLevel level, DamageSource source, boolean killedByPlayer) {
        super.dropCustomDeathLoot(level, source, killedByPlayer);
        this.leaveMaybe(level, Dushi.CARGO_HOOK.get(), 0.05F);
    }
}
