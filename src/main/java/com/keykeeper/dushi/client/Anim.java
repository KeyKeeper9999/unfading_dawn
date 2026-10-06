package com.keykeeper.dushi.client;

import net.minecraft.util.Mth;

/** Small helpers for hand-written model animation. */
final class Anim {
    private Anim() {}

    /** 0 below {@code from}, 1 above {@code to}, smooth in between. */
    static float ramp(float t, float from, float to) {
        float x = Mth.clamp((t - from) / (to - from), 0.0F, 1.0F);
        return x * x * (3.0F - 2.0F * x);
    }

    /** Rises from 0 to 1 between {@code up0} and {@code up1}, stays, and falls back between {@code down0} and {@code down1}. */
    static float pulse(float t, float up0, float up1, float down0, float down1) {
        return ramp(t, up0, up1) * (1.0F - ramp(t, down0, down1));
    }

    static float lerp(float amount, float from, float to) {
        return from + (to - from) * amount;
    }

    /**
     * Moves {@code part} {@code amount} of the way from where it is now to its rest pose turned by ({@code x}, {@code y},
     * {@code z}): the numbers are those of the pose sheets the generators draw (rotations added to the rest pose).
     */
    static void pose(net.minecraft.client.model.geom.ModelPart part, float amount, float x, float y, float z) {
        net.minecraft.client.model.geom.PartPose rest = part.getInitialPose();
        part.xRot = lerp(amount, part.xRot, rest.xRot() + x);
        part.yRot = lerp(amount, part.yRot, rest.yRot() + y);
        part.zRot = lerp(amount, part.zRot, rest.zRot() + z);
    }

    /** The same for one axis. */
    static void pitch(net.minecraft.client.model.geom.ModelPart part, float amount, float x) {
        part.xRot = lerp(amount, part.xRot, part.getInitialPose().xRot() + x);
    }
}
