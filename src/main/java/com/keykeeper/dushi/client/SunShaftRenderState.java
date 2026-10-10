package com.keykeeper.dushi.client;

import net.minecraft.client.renderer.entity.state.EntityRenderState;

/** How wide a shaft of sunlight stands just now, in blocks; nothing is drawn when it is none. */
public class SunShaftRenderState extends EntityRenderState {
    public float width;
    /** The violet of the Sky Beneath, not the sun's own light. */
    public boolean dusk;
}
