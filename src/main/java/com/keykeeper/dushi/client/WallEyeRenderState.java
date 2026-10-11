package com.keykeeper.dushi.client;

import net.minecraft.client.renderer.entity.state.EntityRenderState;

/** What the model of an eye in the rock needs: which kind, how big, how it is turned, how far open, and where it looks. */
public class WallEyeRenderState extends EntityRenderState {
    public boolean great;
    public float size = 1.0F;
    public float yRot;
    public float roll;
    /** How far open: nothing, to wide open; and how far wider than that for a ray just gone out of it. */
    public float lid;
    public float flare;
    /** Where what it looks at is, across it and up it, each from -1 to 1. */
    public float lookX;
    public float lookY;
}
