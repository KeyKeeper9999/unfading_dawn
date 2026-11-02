package com.keykeeper.dushi.entity;

import com.keykeeper.dushi.Clock;
import com.keykeeper.dushi.ClockState;
import com.keykeeper.dushi.Dushi;
import com.keykeeper.dushi.Folk;
import com.keykeeper.dushi.PlayerSoul;
import com.keykeeper.dushi.block.MobMarkBlockEntity;

import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

/**
 * One of the folk ({@link Folk}): somebody who stays where the map put him and talks. A click is a topic; see
 * {@link Folk#next}. Like the keykeeper, he can be struck down, and like everybody in the stopped day he is back at his
 * mark after the next rest. The hangman, once he has said his last, goes for good: the world remembers it among the
 * fallen ({@link Clock#slain}), and his mark raises nobody after that.
 */
public class FolkEntity extends PathfinderMob {
    /** Ticks between the hangman's last word and his going. */
    private static final int LEAVING = 80;

    private final Folk who;
    private int leaving;
    /** Whether he has looked, on his first tick, if he is one who has gone. (Not tickCount: the level counts it, not tick().) */
    private boolean checked;

    public FolkEntity(EntityType<? extends FolkEntity> type, Level level, Folk who) {
        super(type, level);
        this.who = who;
        this.setPersistenceRequired();
    }

    public Folk who() {
        return this.who;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return PathfinderMob.createMobAttributes().add(Attributes.MAX_HEALTH, 30.0).add(Attributes.MOVEMENT_SPEED, 0.0)
                .add(Attributes.KNOCKBACK_RESISTANCE, 1.0);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new LookAtPlayerGoal(this, Player.class, 6.0F, 1.0F));
    }

    @Override
    protected InteractionResult mobInteract(Player player, InteractionHand hand) {
        if (hand != InteractionHand.MAIN_HAND) {
            return InteractionResult.PASS;
        }
        if (this.level() instanceof ServerLevel level && player instanceof ServerPlayer serverPlayer && this.leaving == 0) {
            this.talk(level, serverPlayer);
        }
        return InteractionResult.SUCCESS;
    }

    /** Says the next topic this player has not heard, or a line of idle talk. Returns the topic said, or null for idle talk. */
    public Folk.Topic talk(ServerLevel level, ServerPlayer player) {
        ClockState clock = Clock.state(level);
        PlayerSoul soul = PlayerSoul.of(player);
        Folk.Topic topic = this.who.next(clock, soul.heard);
        if (topic != null) {
            for (int n = 0; n < topic.lines(); n++) {
                this.say(player, this.who.line(topic, n));
            }
            soul.heard.add(this.who.key(topic));
            if (this.who.leaves(topic)) {
                this.leaving = LEAVING;
            }
        } else {
            this.say(player, this.who.idleLine(this.random.nextInt(this.who.idle)));
        }
        this.playSound(this.getAmbientSound(), 0.8F, 1.0F);
        return topic;
    }

    private void say(ServerPlayer player, String key) {
        player.sendSystemMessage(Component.translatable("dialogue.unfading_dawn.line",
                this.getDisplayName().copy().withStyle(ChatFormatting.GOLD), Component.translatable(key)));
    }

    @Override
    public void tick() {
        super.tick();
        if (!(this.level() instanceof ServerLevel level)) {
            return;
        }
        // One who has gone does not come back with the rest of the borough.
        if (!this.checked) {
            this.checked = true;
            if (Clock.slain(level, this.who.id)) {
                this.discard();
                return;
            }
        }
        if (this.leaving > 0 && --this.leaving == 0) {
            this.go(level);
        }
    }

    /** He has nothing left to stay for: he is gone, and stays gone. */
    public void go(ServerLevel level) {
        Clock.state(level).slain.add(this.who.id);
        level.sendParticles(ParticleTypes.LARGE_SMOKE, this.getX(), this.getY() + 1.0, this.getZ(), 24, 0.3, 0.6, 0.3, 0.01);
        MobMarkBlockEntity.mobDied(this);
        this.discard();
    }

    public boolean isLeaving() {
        return this.leaving > 0;
    }

    @Override
    public boolean removeWhenFarAway(double distSqr) {
        return false;
    }

    @Override
    public boolean isPushable() {
        return false;
    }

    @Override
    public int getAmbientSoundInterval() {
        return 300;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return Dushi.sound("entity.folk." + this.who.id);
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return Dushi.sound("entity.folk." + this.who.id);
    }

    @Override
    protected SoundEvent getDeathSound() {
        return Dushi.sound("entity.withered.death");
    }
}
