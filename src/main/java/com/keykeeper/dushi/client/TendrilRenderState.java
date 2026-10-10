package com.keykeeper.dushi.client;

import net.minecraft.client.renderer.entity.state.EntityRenderState;

/** What the model of the Coming's arm out of the floor needs: how old it is, how long the hole warns, which way it falls. */
public class TendrilRenderState extends EntityRenderState {
    /** Ticks since it came up, with the partial tick added; under nothing while the hole is only warning. */
    public float time;
    /** Ticks since the hole began to open. */
    public float age;
    public int warning;
    /** How long it stands before it comes down, in ticks. */
    public int stands;
    /** The way it comes down, in degrees. */
    public float yRot;
}
