package com.keykeeper.dushi.entity;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import com.keykeeper.dushi.Dushi;

import net.minecraft.ChatFormatting;
import net.minecraft.core.UUIDUtil;
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
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

/**
 * The blind keykeeper: an old man hung with keys who sits where he was left and cannot get up any more.
 * He gives every newcomer a flask of warmth and a word of advice, once; after that he only mutters.
 */
public class KeykeeperEntity extends PathfinderMob {
    private static final int FIRST_LINES = 4;
    private static final int IDLE_LINES = 5;

    private final Set<UUID> given = new HashSet<>();

    public KeykeeperEntity(EntityType<? extends KeykeeperEntity> type, Level level) {
        super(type, level);
        this.setPersistenceRequired();
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
        if (this.level() instanceof ServerLevel level && player instanceof ServerPlayer serverPlayer) {
            if (this.given.add(player.getUUID())) {
                for (int line = 0; line < FIRST_LINES; line++) {
                    this.say(serverPlayer, "dialogue.vsratie_dushi.keykeeper.first." + line);
                }
                this.give(serverPlayer, new ItemStack(Dushi.WARMTH_FLASK.get()));
                this.give(serverPlayer, new ItemStack(Dushi.RETURN_EMBER.get(), 2));
                this.playSound(Dushi.sound("entity.keykeeper.give"), 1.0F, 1.0F);
                level.sendParticles(ParticleTypes.FLAME, this.getX(), this.getY() + 1.0, this.getZ(), 8, 0.3, 0.2, 0.3, 0.01);
            } else {
                this.say(serverPlayer, "dialogue.vsratie_dushi.keykeeper.idle." + this.random.nextInt(IDLE_LINES));
                this.playSound(Dushi.sound("entity.keykeeper.ambient"), 1.0F, 1.0F);
            }
        }
        return InteractionResult.SUCCESS;
    }

    private void say(ServerPlayer player, String key) {
        player.sendSystemMessage(Component.translatable("dialogue.vsratie_dushi.line",
                this.getDisplayName().copy().withStyle(ChatFormatting.GOLD), Component.translatable(key)));
    }

    private void give(ServerPlayer player, ItemStack stack) {
        if (!player.getInventory().add(stack) && this.level() instanceof ServerLevel level) {
            player.spawnAtLocation(level, stack);
        }
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
        return 260;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return Dushi.sound("entity.keykeeper.ambient");
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return Dushi.sound("entity.keykeeper.ambient");
    }

    @Override
    protected SoundEvent getDeathSound() {
        return Dushi.sound("entity.withered.death");
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        ValueOutput.TypedOutputList<UUID> list = output.list("Given", UUIDUtil.CODEC);
        this.given.forEach(list::add);
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        this.given.clear();
        input.listOrEmpty("Given", UUIDUtil.CODEC).forEach(this.given::add);
    }
}
