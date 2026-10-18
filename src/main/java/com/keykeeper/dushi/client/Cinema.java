package com.keykeeper.dushi.client;

import com.keykeeper.dushi.ClockState;
import com.keykeeper.dushi.Dushi;
import com.keykeeper.dushi.Net;
import com.keykeeper.dushi.entity.ColossusEntity;
import com.keykeeper.dushi.entity.DollyEntity;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.fog.FogData;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.FogType;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.ViewportEvent;

/**
 * The end of the day as it is seen: what stands up out of the sea, and the camera that watches it do so.
 *
 * <p><b>The figure.</b> Too far from the tower for the server to send it to anybody, so it is the client's: the server only says where it
 * stands and how long ago it began to rise ({@link Net.Colossus}), and the client makes a {@link ColossusEntity} that reads its own age
 * off the game's clock. The sounds it makes (the sea heaving under it, three roars) are played here at the ticks of its rising, to
 * everyone in the world and from nowhere, because it is heard wherever one is, and must be loud. While it rises everybody's screen
 * shakes, a little. Its fog is lifted for as long as anyone is near enough to see it: nothing else would show it at that distance.
 *
 * <p><b>The cutscene.</b> For the ending the camera is taken from the player ({@link Net.Cutscene}): it is a {@link DollyEntity}, which the
 * game is made to look through, and moves along a path made of the ending's own places. The bell: the player's own eyes on the great
 * bell as it tolls, a cut to black, and the camera out beside the tower looking at the sea as the figure rises out of it; the seat: up
 * out of the tower, over it, and over the horizon of the hour. Controls are locked by a transparent screen ({@link CutsceneScreen}) that
 * takes a key to skip it; the hud is hidden and the render distance is raised for as long as it lasts; at its end the screen of the
 * ending ({@link EndingScreen}) follows, out of black.
 */
@EventBusSubscriber(modid = Dushi.MODID, value = Dist.CLIENT)
public final class Cinema {
    /** Ticks of the bell's cutscene before the cut, and of the black that hides it. */
    private static final int CUT = 162;
    /** The same for the seat's: the player's own eyes turn to the horizon, and then the camera is out and high over the tower (it could not rise through the bell and the roof). */
    private static final int SEAT_CUT = 90;
    /** Fog, in blocks, while the figure can be seen. */
    private static final float FOG_START = 1500.0F;
    private static final float FOG_END = 2400.0F;
    /** The farthest a player can be from the figure for its fog to be lifted. */
    private static final double FOG_REACH = 900.0;
    /** The render distance, in chunks, the cutscene of the bell asks for if the player has less: the sea it stands in is far. */
    private static final int WANTED_DISTANCE = 32;
    /** The ticks of its rising at which each sound begins: the sea heaving, a roar, the great roar, a roar. */
    private static final int[] SOUND_AT = {10, 270, 380, 490};
    private static final String[] SOUND = {"entity.colossus.rumble", "entity.colossus.roar_short", "entity.colossus.roar", "entity.colossus.roar_short"};
    private static final float[] SOUND_PITCH = {1.0F, 1.0F, 1.0F, 0.88F};

    /** The numbers the client gives its own entities (the server never heard of them), far above any the server counts up to. */
    private static int nextId = 1_000_000_000;

    private static ColossusEntity colossus;
    private static Vec3 figureAt;
    private static long lastAge;
    private static Scene scene;
    private static boolean shown;

    private Cinema() {}

    /** Where the camera is, and which way it looks (degrees: yaw as the game counts it, pitch positive downwards). */
    private record Look(Vec3 pos, float yaw, float pitch) {}

    private static final class Scene {
        int ending;
        Vec3 rope;
        Vec3 eye;
        float yaw0;
        float pitch0;
        float sunYaw;
        long start;
        DollyEntity dolly;
        float yaw;
        int renderDistance;
        boolean hudWasHidden;
    }

    // ------------------------------------------------------------------ the figure

    static void colossus(Net.Colossus packet) {
        Minecraft minecraft = Minecraft.getInstance();
        ClientLevel level = minecraft.level;
        if (level == null || level.dimension() != Level.OVERWORLD) {
            return;
        }
        dropFigure();
        figureAt = new Vec3(packet.x() + 0.5, packet.y() + 1.0, packet.z() + 0.5);
        ColossusEntity entity = new ColossusEntity(Dushi.COLOSSUS.get(), level);
        Vec3 beside = minecraft.getCameraEntity() != null ? minecraft.getCameraEntity().position() : figureAt;
        entity.snapTo(beside.x, beside.y, beside.z, packet.yaw(), 0.0F);
        entity.anchorAt(figureAt);
        entity.begin(level.getGameTime() - packet.age());
        add(level, entity);
        colossus = entity;
        lastAge = packet.age();
    }

    /** What the figure and the cutscene are doing, for the test's log: the figure's age, and the time of the scene. */
    static String describe() {
        Minecraft minecraft = Minecraft.getInstance();
        StringBuilder text = new StringBuilder();
        if (minecraft.level != null && colossus != null) {
            text.append("figure age ").append(minecraft.level.getGameTime() - colossus.begun()).append(" at ").append(figureAt);
        }
        if (minecraft.level != null && scene != null) {
            text.append(text.length() > 0 ? "; " : "").append("scene ").append(scene.ending).append(" at ").append(minecraft.level.getGameTime() - scene.start).append(" ticks");
        }
        return text.toString();
    }

    /** The figure taken down (the test's other ending is played in the same world). */
    static void clearFigure() {
        dropFigure();
        figureAt = null;
    }

    /** Whether the figure is up (the test asks, to put one up if the bell has not been struck in its run). */
    static boolean hasFigure() {
        return colossus != null && !colossus.isRemoved();
    }

    /** Puts an entity of the client's own into its world: the game wants it numbered first. */
    private static void add(ClientLevel level, Entity entity) {
        entity.setId(nextId++);
        level.addEntity(entity);
    }

    private static void dropFigure() {
        if (colossus != null && !colossus.isRemoved()) {
            colossus.discard();
        }
        colossus = null;
    }

    private static void play(Minecraft minecraft, String sound, float pitch) {
        minecraft.getSoundManager().play(SimpleSoundInstance.forUI(Dushi.sound(sound), pitch, 1.0F));
    }

    private static float burst(long age, int from, int to, float strength) {
        return age >= from && age <= to ? strength : 0.0F;
    }

    private static void tickFigure(Minecraft minecraft, ClientLevel level) {
        long age = level.getGameTime() - colossus.begun();
        for (int i = 0; i < SOUND_AT.length; i++) {
            if (lastAge < SOUND_AT[i] && SOUND_AT[i] <= age) {
                play(minecraft, SOUND[i], SOUND_PITCH[i]);
            }
        }
        lastAge = age;
        float strength = 0.0F;
        if (age >= 20 && age <= ColossusEntity.RISE) {
            strength = 0.05F + 0.10F * Anim.ramp(age, 40.0F, 200.0F);
        }
        strength = Math.max(strength, Math.max(burst(age, 190, 240, 0.55F), burst(age, 285, 345, 0.40F)));
        strength = Math.max(strength, Math.max(burst(age, 395, 490, 0.65F), burst(age, 495, 545, 0.45F)));
        if (strength > 0.0F) {
            DushiClient.shake(3, scene != null ? strength : strength * 0.5F);
        }
    }

    // ------------------------------------------------------------------ the cutscene

    /** The tick of a cutscene at which the camera is cut from the player's own eyes to its path outside. */
    private static int cutAt(int ending) {
        return ending == ClockState.SEAT ? SEAT_CUT : CUT;
    }

    private static int length(int ending) {
        return ending == ClockState.SEAT ? com.keykeeper.dushi.Clock.SEAT_LENGTH : com.keykeeper.dushi.Clock.BELL_LENGTH;
    }

    static void cutscene(Net.Cutscene packet) {
        Minecraft minecraft = Minecraft.getInstance();
        ClientLevel level = minecraft.level;
        if (scene != null || level == null || minecraft.player == null) {
            return;
        }
        Scene s = new Scene();
        s.ending = packet.ending();
        s.rope = new Vec3(packet.x() + 0.5, packet.y() + 0.5, packet.z() + 0.5);
        s.eye = minecraft.player.getEyePosition();
        s.yaw0 = minecraft.player.getYRot();
        s.yaw = s.yaw0;
        s.pitch0 = minecraft.player.getXRot();
        s.start = level.getGameTime();
        // the hour decides which way the seat's cutscene looks: where the sun is, or has just been
        long hour = Math.floorMod(level.getOverworldClockTime(), 24000L);
        s.sunYaw = hour >= 3000L && hour < 15000L ? 90.0F : -90.0F;
        s.dolly = new DollyEntity(Dushi.DOLLY.get(), level);
        s.dolly.snapTo(s.eye.x, s.eye.y - s.dolly.getEyeHeight(), s.eye.z, s.yaw0, s.pitch0);
        add(level, s.dolly);
        minecraft.setCameraEntity(s.dolly);
        s.hudWasHidden = minecraft.gui.hud.isHidden();
        if (!s.hudWasHidden) {
            minecraft.gui.hud.toggle();
        }
        s.renderDistance = minecraft.options.renderDistance().get();
        if (s.ending == ClockState.BELL && s.renderDistance < WANTED_DISTANCE) {
            minecraft.options.renderDistance().set(WANTED_DISTANCE);
        }
        scene = s;
        minecraft.gui.setScreen(new CutsceneScreen());
    }

    /** A key was pressed that skips the cutscene: the screen of the ending follows at once. */
    static void skip() {
        if (scene != null) {
            finish(Minecraft.getInstance());
        }
    }

    /** The screen of the ending, from the server: not shown twice, and not over a cutscene (which shows it itself when it is done). */
    static void endScreen(int ending) {
        if (scene != null || shown) {
            return;
        }
        shown = true;
        Minecraft.getInstance().gui.setScreen(new EndingScreen(ending));
    }

    private static void finish(Minecraft minecraft) {
        Scene s = scene;
        scene = null;
        restore(minecraft, s);
        shown = true;
        minecraft.gui.setScreen(new EndingScreen(s.ending));
    }

    private static void restore(Minecraft minecraft, Scene s) {
        if (minecraft.player != null && minecraft.getCameraEntity() == s.dolly) {
            minecraft.setCameraEntity(minecraft.player);
        }
        if (!s.dolly.isRemoved()) {
            s.dolly.discard();
        }
        if (!s.hudWasHidden && minecraft.gui.hud.isHidden()) {
            minecraft.gui.hud.toggle();
        }
        if (minecraft.options.renderDistance().get() != s.renderDistance) {
            minecraft.options.renderDistance().set(s.renderDistance);
        }
    }

    /** How black the screen is, 0 to 1: the cut of the bell's cutscene, and the end of both. */
    static float fade(float partial) {
        Minecraft minecraft = Minecraft.getInstance();
        if (scene == null || minecraft.level == null) {
            return 0.0F;
        }
        float t = minecraft.level.getGameTime() - scene.start + partial;
        int length = length(scene.ending);
        float black = Anim.ramp(t, length - 50.0F, length);
        int cut = cutAt(scene.ending);
        black = Math.max(black, Anim.ramp(t, cut - 12.0F, cut) * (1.0F - Anim.ramp(t, cut + 1.0F, cut + 20.0F)));
        return black;
    }

    /** How plainly the way to skip is shown, 0 to 1: at the beginning only. */
    static float hint(float partial) {
        Minecraft minecraft = Minecraft.getInstance();
        if (scene == null || minecraft.level == null) {
            return 0.0F;
        }
        float t = minecraft.level.getGameTime() - scene.start + partial;
        return 1.0F - Anim.ramp(t, 110.0F, 140.0F);
    }

    private static float yawTo(Vec3 from, Vec3 to) {
        return (float) (Mth.atan2(to.z - from.z, to.x - from.x) * Mth.RAD_TO_DEG) - 90.0F;
    }

    private static float pitchTo(Vec3 from, Vec3 to) {
        double across = Math.sqrt((to.x - from.x) * (to.x - from.x) + (to.z - from.z) * (to.z - from.z));
        return (float) -(Mth.atan2(to.y - from.y, across) * Mth.RAD_TO_DEG);
    }

    private static Look bellPath(Scene s, long t) {
        Vec3 figure = figureAt != null ? figureAt : s.rope.add(400.0, 0.0, 0.0);
        if (t < CUT) {
            // the player's own eyes, lifted a little, drawn up to the bell that tolls over the rope
            Vec3 pos = s.eye.add(0.0, 0.6 * Anim.ramp(t, 0.0F, 150.0F), 0.0);
            Vec3 bell = s.rope.add(0.0, 2.2, 0.0);
            float e = Anim.ramp(t, 8.0F, 70.0F);
            float yaw = s.yaw0 + Mth.wrapDegrees(yawTo(pos, bell) - s.yaw0) * e;
            return new Look(pos, yaw, Anim.lerp(e, s.pitch0, pitchTo(pos, bell)));
        }
        // out beside the tower, over its roof's height, looking at the sea; the camera drifts up and back while it rises
        Vec3 along = new Vec3(figure.x - s.rope.x, 0.0, figure.z - s.rope.z).normalize();
        Vec3 first = s.rope.add(along.scale(26.0)).add(0.0, 12.0, 0.0);
        float u = Anim.ramp(t, CUT, length(ClockState.BELL));
        Vec3 pos = first.add(0.0, 6.0 * u, 0.0).subtract(along.scale(8.0 * u));
        float age = t - com.keykeeper.dushi.Clock.FIGURE_AT;
        Vec3 look = figure.add(0.0, 14.0 + 190.0 * Anim.ramp(age, 0.0F, 500.0F), 0.0);
        float yaw = s.yaw + Mth.wrapDegrees(yawTo(pos, look) - s.yaw);
        return new Look(pos, yaw, pitchTo(pos, look));
    }

    private static Look seatPath(Scene s, long t) {
        double forward = s.sunYaw * Mth.DEG_TO_RAD;
        Vec3 back = new Vec3(Mth.sin((float) forward), 0.0, -Mth.cos((float) forward));        // (behind where it looks)
        if (t < SEAT_CUT) {
            // the player's own eyes, lifted a little, turning to where the sun is, or was: out of the belfry's arch, at the horizon
            float e = Anim.ramp(t, 10.0F, 80.0F);
            Vec3 pos = s.eye.add(0.0, 0.6 * Anim.ramp(t, 0.0F, SEAT_CUT), 0.0);
            return new Look(pos, s.yaw0 + Mth.wrapDegrees(s.sunYaw - s.yaw0) * e, Anim.lerp(e, s.pitch0, 6.0F));
        }
        // out and high over the tower, rising over the horizon of the hour, with the sky coming into the view
        float u = Anim.ramp(t, SEAT_CUT, length(ClockState.SEAT));
        Vec3 pos = s.eye.add(0.0, 34.0 + 70.0 * u, 0.0).add(back.scale(16.0 + 20.0 * u));
        return new Look(pos, s.sunYaw, Anim.lerp(u, 8.0F, -8.0F));
    }

    private static void tickScene(Minecraft minecraft, ClientLevel level) {
        Scene s = scene;
        long t = level.getGameTime() - s.start;
        if (t >= length(s.ending) || s.dolly.isRemoved() || s.dolly.level() != level) {
            finish(minecraft);
            return;
        }
        Look look = s.ending == ClockState.SEAT ? seatPath(s, t) : bellPath(s, t);
        s.yaw = look.yaw();
        double y = look.pos().y - s.dolly.getEyeHeight();
        if (t == cutAt(s.ending)) {
            s.dolly.snapTo(look.pos().x, y, look.pos().z, look.yaw(), look.pitch());         // (the cut: nothing is carried across it)
        } else {
            s.dolly.setOldPosAndRot();
            s.dolly.setPos(look.pos().x, y, look.pos().z);
            s.dolly.setYRot(look.yaw());
            s.dolly.setXRot(look.pitch());
        }
    }

    // ------------------------------------------------------------------ the events

    @SubscribeEvent
    static void onTick(ClientTickEvent.Post event) {
        Minecraft minecraft = Minecraft.getInstance();
        ClientLevel level = minecraft.level;
        if (level == null || minecraft.player == null) {
            return;
        }
        if (colossus != null && (colossus.isRemoved() || colossus.level() != level)) {
            colossus = null;
        }
        if (colossus != null) {
            Entity eye = minecraft.getCameraEntity();
            if (eye != null) {
                colossus.setOldPosAndRot();
                colossus.setPos(eye.getX(), eye.getY(), eye.getZ());                          // (beside the camera: see ColossusEntity.anchorAt)
            }
            tickFigure(minecraft, level);
        }
        if (scene != null) {
            tickScene(minecraft, level);
        }
    }

    /** Nothing shows at the distance the figure stands at in the fog of the game's own sight: while it can be seen, the fog is moved out of the way. */
    @SubscribeEvent
    static void onFog(ViewportEvent.RenderFog event) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || event.getType() == FogType.LAVA || event.getType() == FogType.WATER || event.getType() == FogType.POWDER_SNOW) {
            return;
        }
        boolean far = scene != null;
        if (!far && colossus != null && figureAt != null && !colossus.isRemoved() && colossus.age(0.0F) > 0.0F) {
            far = minecraft.player.position().distanceToSqr(figureAt.x, minecraft.player.getY(), figureAt.z) < FOG_REACH * FOG_REACH;
        }
        if (!far) {
            return;
        }
        FogData fog = event.getFogData();
        fog.environmentalStart = Math.max(fog.environmentalStart, FOG_START);
        fog.environmentalEnd = Math.max(fog.environmentalEnd, FOG_END);
        fog.renderDistanceStart = Math.max(fog.renderDistanceStart, FOG_START);
        fog.renderDistanceEnd = Math.max(fog.renderDistanceEnd, FOG_END);
        fog.skyEnd = Math.max(fog.skyEnd, FOG_END);
        fog.cloudEnd = Math.max(fog.cloudEnd, FOG_END);
    }

    @SubscribeEvent
    static void onLogout(ClientPlayerNetworkEvent.LoggingOut event) {
        Minecraft minecraft = Minecraft.getInstance();
        if (scene != null) {
            restore(minecraft, scene);
            scene = null;
        }
        colossus = null;
        figureAt = null;
        shown = false;
    }
}
