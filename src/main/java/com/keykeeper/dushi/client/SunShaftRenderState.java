package com.keykeeper.dushi.client;

import net.minecraft.client.renderer.entity.state.EntityRenderState;

/** How wide a shaft of sunlight stands just now, in blocks; nothing is drawn when it is none. */
public class SunShaftRenderState extends EntityRenderState {
    public float width;
    /** Whose light it is (SunShaftEntity.SUN, DUSK, AMBER). */
    public int hue;
    /** How long it is, in blocks, and how it is turned from standing upright: about the upright axis, then over. */
    public float length = 48.0F;
    public float yaw;
    public float pitch;
}
