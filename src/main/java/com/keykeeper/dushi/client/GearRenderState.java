package com.keykeeper.dushi.client;

import net.minecraft.client.renderer.entity.state.EntityRenderState;

/** What is drawn of a piece of the clock's works (GearEntity): which piece, how it is turned, and where on its way it is. */
public class GearRenderState extends EntityRenderState {
    public int kind;
    /** Ticks since it began, with the partial tick added. */
    public float age;
    public float size = 1.0F;
    /** Which way it is turned about the upright axis, in degrees. */
    public float yRot;
    /** How far a wheel has turned about its axle, in radians. */
    public float spin;
    /** How high over where it will stand a weight hangs, in blocks. */
    public float hang;
}
