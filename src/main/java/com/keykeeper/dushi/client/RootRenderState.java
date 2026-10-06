package com.keykeeper.dushi.client;

import net.minecraft.client.renderer.entity.state.EntityRenderState;

/** What the model of a root needs: how old it is, how long the ground warns before it comes up, how long it stands. */
public class RootRenderState extends EntityRenderState {
    /** Ticks since it began, with the partial tick added. */
    public float age;
    public int warning;
    public int stand;
    /** Which way the bundle is turned, so that no two look the same. */
    public float yRot;
}
