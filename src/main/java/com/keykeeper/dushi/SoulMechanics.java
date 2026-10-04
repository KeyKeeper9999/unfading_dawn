package com.keykeeper.dushi;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.keykeeper.dushi.entity.SoulStainEntity;
import com.keykeeper.dushi.entity.SoulsMob;
import com.keykeeper.dushi.item.FlaskItem;
import com.keykeeper.dushi.item.GuardShieldItem;
import com.keykeeper.dushi.item.SpellItem;
import com.keykeeper.dushi.item.WeaponItem;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.BlocksAttacks;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.living.LivingDropsEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingShieldBlockEvent;
import net.neoforged.neoforge.event.entity.player.AttackEntityEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;

/** The rules of the game that are not tied to one item or mob: souls, stamina, rolling, blows in the back. */
@EventBusSubscriber(modid = Dushi.MODID)
public final class SoulMechanics {
    /** Stamina a swing costs with anything that is not one of the mod's weapons. */
    private static final float DEFAULT_SWING_COST = 6.0F;
    /** A blow struck without stamina does this share of its damage. */
    private static final float EXHAUSTED_DAMAGE = 0.4F;
    /** Bosses reward every player this close. */
    private static final double BOSS_REWARD_RANGE = 48.0;

    private SoulMechanics() {}

    /** Whether stamina and the like apply: not in creative or spectator mode. */
    public static boolean plays(Player player) {
        return !player.isCreative() && !player.isSpectator();
    }

    // ------------------------------------------------------------------ souls

    public static void giveSouls(ServerPlayer player, int amount) {
        if (amount <= 0) {
            return;
        }
        PlayerSoul soul = PlayerSoul.of(player);
        soul.souls += amount;
        player.syncData(Dushi.PLAYER_SOUL);
        PacketDistributor.sendToPlayer(player, new Net.Banner("", 0, "ui.souls_gain"));
    }

    /** Souls for killing {@code victim}. */
    public static int soulsFor(LivingEntity victim) {
        if (victim instanceof SoulsMob mob) {
            return mob.soulReward();
        }
        if (victim instanceof Enemy) {
            return Math.max(5, Math.round(victim.getMaxHealth() * 1.5F));
        }
        return 0;
    }

    @SubscribeEvent
    static void onDeath(LivingDeathEvent event) {
        LivingEntity victim = event.getEntity();
        if (!(victim.level() instanceof ServerLevel level)) {
            return;
        }
        if (victim instanceof ServerPlayer player) {
            onPlayerDeath(level, player);
            return;
        }
        int souls = soulsFor(victim);
        if (souls <= 0) {
            return;
        }
        if (victim instanceof SoulsMob mob && mob.isBoss()) {
            for (ServerPlayer player : level.players()) {
                if (player.isAlive() && player.distanceToSqr(victim) <= BOSS_REWARD_RANGE * BOSS_REWARD_RANGE) {
                    giveSouls(player, souls);
                    PacketDistributor.sendToPlayer(player, new Net.Banner("banner.vsratie_dushi.boss_defeated", Net.Banner.GOLD, "ui.boss_defeated"));
                }
            }
        } else if (victim.getKillCredit() instanceof ServerPlayer player) {
            giveSouls(player, souls);
            level.sendParticles(ParticleTypes.SOUL, victim.getX(), victim.getY() + victim.getBbHeight() * 0.5, victim.getZ(), 5, 0.2, 0.3, 0.2, 0.02);
        }
    }

    /** The souls carried stay where the player fell; an older stain of theirs, if any, is lost for good. */
    public static void onPlayerDeath(ServerLevel level, ServerPlayer player) {
        PlayerSoul soul = PlayerSoul.of(player);
        soul.stainGeneration++;
        soul.lostSouls = soul.souls;
        if (soul.souls > 0) {
            SoulStainEntity.drop(level, player, soul.souls, soul.stainGeneration);
            soul.souls = 0;
        }
        soul.rollTicks = 0;
        player.setForcedPose(null);
        player.syncData(Dushi.PLAYER_SOUL);
        PacketDistributor.sendToPlayer(player, new Net.Banner("", Net.Banner.RED, "ui.death"));
        // Remember what was carried and where: death takes the souls, not the belongings.
        Inventory inventory = player.getInventory();
        List<ItemStack> kept = new ArrayList<>(inventory.getContainerSize());
        for (int slot = 0; slot < inventory.getContainerSize(); slot++) {
            kept.add(inventory.getItem(slot).copy());
        }
        KEPT.put(player.getUUID(), kept);
    }

    /** What each player carried at the moment of death, until the game has finished scattering it. */
    private static final Map<UUID, List<ItemStack>> KEPT = new HashMap<>();

    /** The dead keep their belongings: what the game was about to scatter goes back to where it was. */
    @SubscribeEvent(priority = EventPriority.LOWEST)
    static void onPlayerDrops(LivingDropsEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }
        List<ItemStack> kept = KEPT.remove(player.getUUID());
        if (kept == null || event.getDrops().isEmpty()) {
            return;
        }
        Inventory inventory = player.getInventory();
        for (int slot = 0; slot < kept.size() && slot < inventory.getContainerSize(); slot++) {
            inventory.setItem(slot, kept.get(slot));
        }
        event.setCanceled(true);
    }

    @SubscribeEvent
    static void onClone(PlayerEvent.Clone event) {
        if (event.isWasDeath()) {
            event.getEntity().getInventory().replaceWith(event.getOriginal().getInventory());
        }
    }

    // ------------------------------------------------------------------ joining and respawning

    @SubscribeEvent
    static void onLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            PlayerSoul soul = PlayerSoul.of(player);
            soul.applyAttributes(player);
            player.syncData(Dushi.PLAYER_SOUL);
        }
    }

    @SubscribeEvent
    static void onRespawn(PlayerEvent.PlayerRespawnEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            PlayerSoul soul = PlayerSoul.of(player);
            soul.applyAttributes(player);
            player.setHealth(player.getMaxHealth());
            soul.stamina = soul.maxStamina();
            soul.regenPause = 0;
            player.syncData(Dushi.PLAYER_SOUL);
            refill(player);
            if (soul.lostSouls > 0) {
                player.sendOverlayMessage(Component.translatable("message.vsratie_dushi.souls_left", soul.lostSouls));
            }
        }
    }

    @SubscribeEvent
    static void onChangeDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            player.syncData(Dushi.PLAYER_SOUL);
        }
    }

    /** Fills every flask and restores every spell the player carries. */
    public static void refill(Player player) {
        for (int slot = 0; slot < player.getInventory().getContainerSize(); slot++) {
            ItemStack stack = player.getInventory().getItem(slot);
            if ((stack.getItem() instanceof FlaskItem || stack.getItem() instanceof SpellItem) && stack.getDamageValue() != 0) {
                stack.setDamageValue(0);
            }
        }
    }

    // ------------------------------------------------------------------ stamina

    @SubscribeEvent
    static void onPlayerTick(PlayerTickEvent.Post event) {
        Player player = event.getEntity();
        PlayerSoul soul = PlayerSoul.of(player);
        boolean wasRolling = soul.rollTicks > 0;
        if (!plays(player)) {
            soul.stamina = soul.maxStamina();
            soul.regenPause = 0;
            soul.tickStamina(false, false);
        } else {
            soul.tickStamina(player.isSprinting() && !player.isPassenger(), player.isBlocking());
            if (soul.stamina <= 0.0F && player.isSprinting()) {
                player.setSprinting(false);
            }
        }
        if (soul.rollTicks == 0 && (wasRolling || player.getForcedPose() == Pose.SWIMMING)) {
            player.setForcedPose(null);
        }
        // The client counts by itself between these corrections.
        if (player instanceof ServerPlayer serverPlayer && player.tickCount % 40 == 0) {
            serverPlayer.syncData(Dushi.PLAYER_SOUL);
        }
    }

    /** Every swing at something costs stamina; a swing without any is feeble. */
    @SubscribeEvent
    static void onAttack(AttackEntityEvent event) {
        Player player = event.getEntity();
        if (!plays(player) || !(event.getTarget() instanceof LivingEntity)) {
            return;
        }
        PlayerSoul soul = PlayerSoul.of(player);
        float cost = player.getMainHandItem().getItem() instanceof WeaponItem weapon ? weapon.staminaCost : DEFAULT_SWING_COST;
        // Only a full swing counts: tapping the button does not drain the bar.
        if (player.getAttackStrengthScale(0.5F) < 0.5F) {
            cost *= 0.35F;
        }
        soul.exhaustedSwing = !soul.spend(cost);
        if (player instanceof ServerPlayer serverPlayer) {
            serverPlayer.syncData(Dushi.PLAYER_SOUL);
        }
    }

    // ------------------------------------------------------------------ damage

    @SubscribeEvent
    static void onIncomingDamage(LivingIncomingDamageEvent event) {
        LivingEntity victim = event.getEntity();
        if (!(victim.level() instanceof ServerLevel level)) {
            return;
        }
        // Rolling through a blow.
        if (victim instanceof Player player && PlayerSoul.of(player).rollTicks > Net.ROLL_TICKS - Net.ROLL_SAFE_TICKS
                && !event.getSource().is(DamageTypeTags.BYPASSES_INVULNERABILITY)) {
            event.setCanceled(true);
            return;
        }
        if (event.getSource().getEntity() instanceof Player attacker && event.getSource().getDirectEntity() == attacker
                && event.getSource().is(DamageTypeTags.IS_PLAYER_ATTACK)) {
            PlayerSoul soul = PlayerSoul.of(attacker);
            float amount = event.getAmount();
            if (attacker.getMainHandItem().getItem() instanceof WeaponItem weapon) {
                amount *= soul.weaponBonus(weapon.strengthScaling, weapon.dexterityScaling);
                if (isBehind(attacker, victim) && victim instanceof Mob) {
                    amount *= weapon.backstab;
                    level.playSound(null, victim.getX(), victim.getY(), victim.getZ(), Dushi.sound("player.backstab"), SoundSource.PLAYERS, 1.0F, 1.0F);
                    level.sendParticles(ParticleTypes.CRIT, victim.getX(), victim.getY() + victim.getBbHeight() * 0.6, victim.getZ(), 14, 0.3, 0.3, 0.3, 0.3);
                }
            }
            if (soul.exhaustedSwing && plays(attacker)) {
                amount *= EXHAUSTED_DAMAGE;
            }
            event.setAmount(amount);
        }
    }

    /** True if {@code attacker} stands behind {@code victim}, judged by the way the victim's body faces. */
    public static boolean isBehind(LivingEntity attacker, LivingEntity victim) {
        Vec3 facing = Vec3.directionFromRotation(0.0F, victim.yBodyRot);
        Vec3 toAttacker = new Vec3(attacker.getX() - victim.getX(), 0.0, attacker.getZ() - victim.getZ());
        if (toAttacker.lengthSqr() < 1.0E-4) {
            return false;
        }
        return facing.dot(toAttacker.normalize()) < -0.5;
    }

    /** A blow caught on a shield costs stamina; with none left the guard breaks. */
    @SubscribeEvent
    static void onShieldBlock(LivingShieldBlockEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player) || !event.getBlocked() || !plays(player)) {
            return;
        }
        ItemStack shield = player.getItemBlockingWith();
        float stability = shield != null && shield.getItem() instanceof GuardShieldItem guard ? guard.stability : 0.5F;
        PlayerSoul soul = PlayerSoul.of(player);
        float cost = Math.max(4.0F, event.getBlockedDamage() * 5.0F * (1.0F - stability));
        boolean held = soul.spend(cost);
        player.syncData(Dushi.PLAYER_SOUL);
        if (!held && shield != null) {
            BlocksAttacks blocks = shield.get(DataComponents.BLOCKS_ATTACKS);
            if (blocks != null) {
                blocks.disable(player.level(), player, 3.0F, shield);
            }
            player.level().playSound(null, player.getX(), player.getY(), player.getZ(), Dushi.sound("player.guard_break"), SoundSource.PLAYERS, 1.0F, 1.0F);
        }
    }
}
