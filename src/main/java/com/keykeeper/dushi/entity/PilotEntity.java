package com.keykeeper.dushi.entity;

import com.keykeeper.dushi.Dushi;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/**
 * The Pilot: the one who was to bring the ships in. He stands at the foot of the lighthouse in his long oilskin, a
 * lamp at his belt, and in his hand the lead he sounded the channel with on a fathom of line - and looks out for
 * them, as he has since that morning. Not a lord of the place, but the best of its dead: there is one of him.
 * <p>
 * The lead goes round over his head on the whole of its line, three times, a step with each; it is cast straight,
 * to the end of the line, further than anything else here reaches; and it is lifted and let go straight down before
 * him, as one sounds a channel, which no shield holds.
 */
public class PilotEntity extends ShoremanEntity {
    public static final int WHIRL = 1;
    public static final int WHIRL_DURATION = 64;
    public static final int[] WHIRL_HITS = {22, 34, 46};
    public static final int CAST = 2;
    public static final int CAST_DURATION = 46;
    public static final int CAST_IMPACT = 22;
    public static final int SOUND = 3;
    public static final int SOUND_DURATION = 54;
    public static final int SOUND_IMPACT = 26;
    public static final double WHIRL_RADIUS = 3.7;
    public static final double CAST_REACH = 8.5;

    public PilotEntity(EntityType<? extends PilotEntity> type, Level level) {
        super(type, level);
        this.xpReward = 80;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes().add(Attributes.MAX_HEALTH, 900.0).add(Attributes.ATTACK_DAMAGE, 27.0)
                .add(Attributes.MOVEMENT_SPEED, 0.25).add(Attributes.ARMOR, 6.0).add(Attributes.KNOCKBACK_RESISTANCE, 0.8)
                .add(Attributes.FOLLOW_RANGE, 26.0);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(2, new SoulsCombatGoal(this, 1.0));
        this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 16.0F));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    @Override
    public int soulReward() {
        return 6000;
    }

    @Override
    protected int attackDuration(int id) {
        return id == WHIRL ? WHIRL_DURATION : id == CAST ? CAST_DURATION : SOUND_DURATION;
    }

    @Override
    protected int trackUntil(int id) {
        return id == WHIRL ? WHIRL_HITS[2] : id == CAST ? CAST_IMPACT - 7 : SOUND_IMPACT - 9;
    }

    @Override
    protected int attackPause(int finishedId) {
        return finishedId == WHIRL ? 26 + this.random.nextInt(10) : 14 + this.random.nextInt(12);
    }

    @Override
    public double stopDistance() {
        return 2.2;
    }

    @Override
    public int chooseAttack(LivingEntity target, double distance) {
        if (distance < 3.0) {
            int roll = this.random.nextInt(10);
            return roll < 5 ? WHIRL : roll < 8 ? SOUND : CAST;
        }
        if (distance < CAST_REACH - 0.8 && this.hasLineOfSight(target) && this.random.nextInt(3) != 0) {
            return CAST;
        }
        return 0;
    }

    @Override
    protected void attackTick(ServerLevel level, int id, int time) {
        float damage = (float) this.getAttributeValue(Attributes.ATTACK_DAMAGE);
        if (id == WHIRL) {
            if (time == 8) {
                this.playSound(Dushi.sound("entity.pilot.whirl"), 1.6F, 1.0F);
            }
            for (int hit : WHIRL_HITS) {
                if (time == hit - 5) {
                    Vec3 step = this.facing().scale(0.36);
                    this.setDeltaMovement(step.x, this.getDeltaMovement().y, step.z);
                }
                if (time == hit) {
                    this.hitAround(level, this.position(), WHIRL_RADIUS, damage * 0.8F, 0.9, this.damageSources().mobAttack(this));
                    Vec3 at = this.position().add(this.facing().scale(2.4));
                    level.sendParticles(ParticleTypes.SWEEP_ATTACK, at.x, at.y + 1.6, at.z, 1, 0.0, 0.0, 0.0, 0.0);
                }
            }
        } else if (id == CAST) {
            if (time == CAST_IMPACT - 3) {
                this.playSound(Dushi.sound("entity.sailor.throw"), 1.6F, 0.7F);
            }
            if (time == CAST_IMPACT) {
                this.hitArc(level, this.facing(), CAST_REACH, 8.0, damage * 1.3F, 0.6, false);
                Vec3 at = this.position().add(this.facing().scale(CAST_REACH - 1.0));
                this.rubble(level, at, 8, 0.2);
                level.playSound(null, at.x, at.y, at.z, Dushi.sound("entity.pilot.lead"), this.getSoundSource(), 1.4F, 1.2F);
            }
        } else {
            if (time == SOUND_IMPACT - 5) {
                this.playSound(Dushi.sound("player.weapon_swing_heavy"), 1.2F, 0.7F);
            }
            if (time == SOUND_IMPACT) {
                this.hitArc(level, this.facing(), 3.2, 26.0, damage * 1.6F, 0.5, true);
                Vec3 at = this.position().add(this.facing().scale(2.2));
                this.rubble(level, at, 16, 0.4);
                level.playSound(null, at.x, at.y, at.z, Dushi.sound("entity.pilot.lead"), this.getSoundSource(), 2.0F, 0.8F);
            }
        }
    }

    @Override
    protected void dropCustomDeathLoot(ServerLevel level, DamageSource source, boolean killedByPlayer) {
        super.dropCustomDeathLoot(level, source, killedByPlayer);
        // the lead and its line, which whoever felled him takes up
        this.spawnAtLocation(level, new ItemStack(Dushi.LEAD_LINE.get()));
        this.leave(level, Dushi.GREAT_SOUL.get(), 2, 3);
    }

    @Override
    public boolean removeWhenFarAway(double distSqr) {
        return false;
    }

    @Override
    public float getVoicePitch() {
        return 0.8F + this.random.nextFloat() * 0.06F;
    }
}
