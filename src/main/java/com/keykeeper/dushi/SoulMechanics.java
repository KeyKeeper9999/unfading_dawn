package com.keykeeper.dushi;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.keykeeper.dushi.block.MobMarkBlockEntity;
import com.keykeeper.dushi.block.PlaceNameBlock;
import com.keykeeper.dushi.entity.SoulStainEntity;
import com.keykeeper.dushi.entity.SoulsMob;
import com.keykeeper.dushi.item.FlaskItem;
import com.keykeeper.dushi.item.SpellItem;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.storage.LevelData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.BlocksAttacks;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.level.levelgen.Heightmap;
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
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
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
    private static final Identifier OVERBURDENED = Dushi.id("overburdened");

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

    /** Whether a hearth stands within a few steps of the player: there the dead do not take notice of them. */
    static boolean nearHearth(Player player) {
        BlockPos centre = player.blockPosition();
        for (BlockPos pos : BlockPos.betweenClosed(centre.offset(-HEARTH_PEACE, -2, -HEARTH_PEACE), centre.offset(HEARTH_PEACE, 2, HEARTH_PEACE))) {
            if (player.level().getBlockState(pos).is(Dushi.HEARTH.get())) {
                return true;
            }
        }
        return false;
    }

    private static final int HEARTH_PEACE = 6;

    /** Souls for killing {@code victim}. */
    public static int soulsFor(LivingEntity victim) {
        if (victim instanceof SoulsMob mob) {
            return mob.reward();
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
        MobMarkBlockEntity.mobDied(victim);
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
            SoulStainEntity.drop(level, player, deathPlace(player), soul.souls, soul.stainGeneration);
            soul.souls = 0;
        }
        soul.rollTicks = 0;
        player.setForcedPose(null);
        player.syncData(Dushi.PLAYER_SOUL);
        PacketDistributor.sendToPlayer(player, new Net.Banner("", Net.Banner.RED, "ui.death"));
        // The enemies around are whole again at once, and the dead among them rise: the next attempt starts from the beginning,
        // wherever the player comes back to.
        MobMarkBlockEntity.resetAround(level, BlockPos.containing(deathPlace(player)), MobMarkBlockEntity.RESET_RADIUS);
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

    /**
     * Who may not build (a player of an adventure map) may not put things into a painted pot either: there would be no
     * getting them out again without breaking it.
     */
    @SubscribeEvent
    static void onUsePot(PlayerInteractEvent.RightClickBlock event) {
        if (!event.getEntity().getAbilities().mayBuild && event.getLevel().getBlockState(event.getPos()).is(Blocks.DECORATED_POT)) {
            event.setCanceled(true);
        }
    }

    // ------------------------------------------------------------------ joining and respawning

    @SubscribeEvent
    static void onLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            PlayerSoul soul = PlayerSoul.of(player);
            soul.applyAttributes(player);
            player.syncData(Dushi.PLAYER_SOUL);
            settle(player);
        }
    }

    /**
     * In a world whose game mode is adventure the game does not look for a place around the world's spawn: it takes the spawn
     * itself and lets the player sink to the floor under it. It does so before that place is loaded, finds no floor at all
     * and leaves the player at the bottom of the world, which on a map built in the void is a fall to death. A player
     * found in the column of the spawn, under its floor, is put back on it.
     */
    private static void settle(ServerPlayer player) {
        if (!(player.level() instanceof ServerLevel level)) {
            return;
        }
        LevelData.RespawnData spawn = level.getRespawnData();
        BlockPos pos = spawn.pos();
        BlockPos at = player.blockPosition();
        if (spawn.dimension() == level.dimension() && at.getX() == pos.getX() && at.getZ() == pos.getZ() && at.getY() < pos.getY()
                && !level.getBlockState(pos.below()).getCollisionShape(level, pos.below()).isEmpty()) {
            player.connection.teleport(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, spawn.yaw(), spawn.pitch());
            player.fallDistance = 0.0;
            PlayerSoul.of(player).hasGround = false;
        }
    }

    @SubscribeEvent
    static void onRespawn(PlayerEvent.PlayerRespawnEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            PlayerSoul soul = PlayerSoul.of(player);
            soul.applyAttributes(player);
            settle(player);
            player.setHealth(player.getMaxHealth());
            soul.stamina = soul.maxStamina();
            soul.regenPause = 0;
            player.syncData(Dushi.PLAYER_SOUL);
            refill(player);
            // Dying is a rest of a kind: the dead around the place the player comes back to rise again too.
            if (player.level() instanceof ServerLevel level) {
                MobMarkBlockEntity.resetAround(level, player.blockPosition(), MobMarkBlockEntity.RESET_RADIUS);
                // the player comes back to the hearth they rested at, and to its hour; a newcomer's first place is the world's own
                hour(level, soul.hearthHour);
                soul.place = -1;
            }
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

    public static boolean hasFlask(Player player) {
        for (int slot = 0; slot < player.getInventory().getContainerSize(); slot++) {
            if (player.getInventory().getItem(slot).getItem() instanceof FlaskItem) {
                return true;
            }
        }
        return false;
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
            soul.tickStamina(false, false, 1.0F);
        } else {
            soul.tickStamina(player.isSprinting() && !player.isPassenger(), player.isBlocking(), Gear.staminaRegen(player));
            if (soul.stamina <= 0.0F && player.isSprinting()) {
                player.setSprinting(false);
            }
        }
        if (soul.rollTicks == 0 && (wasRolling || player.getForcedPose() == Pose.SWIMMING)) {
            player.setForcedPose(null);
        }
        if (player instanceof ServerPlayer serverPlayer) {
            if (player.tickCount % 10 == 0) {
                weighDown(serverPlayer);
            }
            ground(serverPlayer, soul);
            if (player.tickCount % 10 == 0) {
                soul.atHearth = nearHearth(serverPlayer);
            }
            // A threshold crossed: the name of the place, once, until another place has been named.
            BlockState at = serverPlayer.level().getBlockState(serverPlayer.blockPosition());
            if (at.getBlock() instanceof PlaceNameBlock) {
                PlaceNameBlock.Place place = at.getValue(PlaceNameBlock.PLACE);
                if (place.named && soul.place != place.ordinal()) {
                    soul.place = place.ordinal();
                    PacketDistributor.sendToPlayer(serverPlayer, new Net.Banner("place.vsratie_dushi." + place.id, Net.Banner.PALE, "ui.place"));
                }
                // Every place has its hour; time stands still there, so the clock is simply set.
                hour(serverPlayer.level(), place.hour);
            }
            // The client counts by itself between these corrections.
            if (player.tickCount % 40 == 0) {
                serverPlayer.syncData(Dushi.PLAYER_SOUL);
            }
        }
    }

    /**
     * Sets the hour of a world whose time stands still (an adventure map: there every place has its own hour, and the clock
     * is turned in the passages between them, where no sky is seen). In a world where time moves nothing is touched.
     */
    public static void hour(ServerLevel level, int hour) {
        if (hour < 0 || level.getGameRules().get(GameRules.ADVANCE_TIME) || Math.floorMod(level.getDefaultClockTime(), 24000L) == hour) {
            return;
        }
        level.getServer().getCommands().performPrefixedCommand(level.getServer().createCommandSourceStack().withSuppressedOutput(), "time set " + hour);
    }

    /** The hour as a hearth remembers it; -1 where time moves. */
    public static int hourNow(ServerLevel level) {
        return level.getGameRules().get(GameRules.ADVANCE_TIME) ? -1 : (int) Math.floorMod(level.getDefaultClockTime(), 24000L);
    }

    /** A fall this long over nothing at all is not waited out to the bottom of the world. */
    private static final double HOPELESS_FALL = 24.0;

    /**
     * Remembers where the player last stood; ends a fall into the void early; and, in a world where health does not come
     * back by itself (an adventure map of this kind), keeps hunger out of the game.
     */
    private static void ground(ServerPlayer player, PlayerSoul soul) {
        ServerLevel level = player.level();
        if (player.onGround() && !player.isSpectator()) {
            soul.groundX = player.getX();
            soul.groundY = player.getY();
            soul.groundZ = player.getZ();
            soul.hasGround = true;
        } else if (plays(player) && player.isAlive() && player.fallDistance > HOPELESS_FALL
                && level.getHeight(Heightmap.Types.MOTION_BLOCKING, player.getBlockX(), player.getBlockZ()) <= level.getMinY()) {
            player.hurtServer(level, level.damageSources().fellOutOfWorld(), Float.MAX_VALUE);
        }
        if (player.tickCount % 20 == 0 && !level.getGameRules().get(GameRules.NATURAL_HEALTH_REGENERATION)) {
            player.getFoodData().setFoodLevel(20);
            player.getFoodData().setSaturation(5.0F);
        }
    }

    /** Where a stain or a reset should be centred for a player who has just died: where they last stood, if they fell. */
    public static Vec3 deathPlace(ServerPlayer player) {
        PlayerSoul soul = PlayerSoul.of(player);
        if (soul.hasGround && (player.getY() < soul.groundY - 4.0 || player.getY() < player.level().getMinY() + 2)) {
            return new Vec3(soul.groundX, soul.groundY, soul.groundZ);
        }
        return player.position();
    }

    /** Someone carrying more than they can bear walks slowly. */
    private static void weighDown(ServerPlayer player) {
        AttributeInstance speed = player.getAttribute(Attributes.MOVEMENT_SPEED);
        if (speed == null) {
            return;
        }
        boolean over = plays(player) && Gear.burden(player) == Gear.Burden.OVER;
        if (over != speed.hasModifier(OVERBURDENED)) {
            speed.removeModifier(OVERBURDENED);
            if (over) {
                speed.addTransientModifier(new AttributeModifier(OVERBURDENED, -0.35, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
                player.sendOverlayMessage(Component.translatable("message.vsratie_dushi.overburdened"));
            }
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
        Gear.Weapon weapon = Gear.weapon(player.getMainHandItem());
        float cost = weapon != null ? weapon.stamina() : DEFAULT_SWING_COST;
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
        // The dead of a harder place hit harder, whatever they hit with.
        if (event.getSource().getEntity() instanceof SoulsMob attacker && attacker.tier() > 0) {
            event.setAmount(event.getAmount() * attacker.damageScale());
        }
        // Rolling through a blow.
        if (victim instanceof Player player && PlayerSoul.of(player).rollTicks > Net.ROLL_TICKS - Gear.burden(player).safeTicks
                && !event.getSource().is(DamageTypeTags.BYPASSES_INVULNERABILITY)) {
            event.setCanceled(true);
            return;
        }
        if (event.getSource().getEntity() instanceof Player attacker && event.getSource().getDirectEntity() == attacker
                && event.getSource().is(DamageTypeTags.IS_PLAYER_ATTACK)) {
            PlayerSoul soul = PlayerSoul.of(attacker);
            float amount = event.getAmount();
            Gear.Weapon weapon = Gear.weapon(attacker.getMainHandItem());
            if (weapon != null) {
                amount *= soul.weaponBonus(weapon.strength(), weapon.dexterity());
                if (isBehind(attacker, victim) && victim instanceof Mob) {
                    amount *= weapon.backstab();
                    level.playSound(null, victim.getX(), victim.getY(), victim.getZ(), Dushi.sound("player.backstab"), SoundSource.PLAYERS, 1.0F, 1.0F);
                    level.sendParticles(ParticleTypes.CRIT, victim.getX(), victim.getY() + victim.getBbHeight() * 0.6, victim.getZ(), 14, 0.3, 0.3, 0.3, 0.3);
                }
            }
            if (soul.exhaustedSwing && plays(attacker)) {
                amount *= EXHAUSTED_DAMAGE;
            }
            event.setAmount(amount);
        } else if (event.getSource().getEntity() instanceof Player archer && event.getSource().getDirectEntity() instanceof AbstractArrow) {
            // An arrow flies truer from a deft hand, whatever bow sent it.
            event.setAmount(event.getAmount() * (1.0F + Gear.ARROW_DEXTERITY * PlayerSoul.of(archer).stats[PlayerSoul.DEXTERITY]));
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
        float stability = shield != null ? Math.max(0.0F, Gear.stability(shield)) : Gear.DEFAULT_STABILITY;
        PlayerSoul soul = PlayerSoul.of(player);
        float cost = Math.max(4.0F, event.getBlockedDamage() * 5.0F * (1.0F - stability) * Gear.blockCost(player));
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
