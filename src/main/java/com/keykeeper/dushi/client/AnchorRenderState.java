package com.keykeeper.dushi.client;

import net.minecraft.client.renderer.entity.state.EntityRenderState;

/** Which way the cast anchor flies, and how far it has turned over on the way. */
public class AnchorRenderState extends EntityRenderState {
    public float yaw;
    public float pitch;
    public float spin;
}
