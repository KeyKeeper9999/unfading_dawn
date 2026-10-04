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
}
