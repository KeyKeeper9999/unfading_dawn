package com.keykeeper.dushi.entity;

import java.util.EnumSet;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;

/** Walks a {@link SoulsMob} up to its target and lets it pick its moves. */
public class SoulsCombatGoal extends Goal {
    private final SoulsMob mob;
    private final double speed;
    private int repath;

    public SoulsCombatGoal(SoulsMob mob, double speed) {
        this.mob = mob;
        this.speed = speed;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        LivingEntity target = this.mob.getTarget();
        return target != null && target.isAlive();
    }

    @Override
    public boolean canContinueToUse() {
        return this.canUse() || this.mob.attackId() != 0;
    }

    @Override
    public void start() {
        this.repath = 0;
        this.mob.setAggressive(true);
    }

    @Override
    public void stop() {
        this.mob.setAggressive(false);
        this.mob.getNavigation().stop();
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    @Override
    public void tick() {
        LivingEntity target = this.mob.getTarget();
        if (target == null) {
            return;
        }
        if (this.mob.attackId() != 0) {
            return;
        }
        this.mob.getLookControl().setLookAt(target, 30.0F, 30.0F);
        double distance = Math.max(0.0, this.mob.distanceTo(target) - target.getBbWidth() * 0.5 - this.mob.getBbWidth() * 0.5);
        if (distance <= this.mob.stopDistance() && this.mob.hasLineOfSight(target)) {
            this.mob.getNavigation().stop();
        } else if (--this.repath <= 0) {
            this.repath = 5 + this.mob.getRandom().nextInt(4);
            this.mob.getNavigation().moveTo(target, this.speed);
        }
        if (this.mob.canStartAttack()) {
            int id = this.mob.chooseAttack(target, distance);
            if (id != 0) {
                this.mob.startAttack(id);
            }
        }
    }
}
