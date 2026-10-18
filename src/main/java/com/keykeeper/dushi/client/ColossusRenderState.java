package com.keykeeper.dushi.client;

import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.world.phys.Vec3;

/** What the model of the figure over the sea needs: how many ticks ago it began to rise (less than nothing before that), and which way it faces. */
public class ColossusRenderState extends EntityRenderState {
    /** Ticks since it began to rise, with the partial tick added; negative while it has not. */
    public float age;
    /** Which way it faces, in degrees. */
    public float yRot;
    /** From where the entity is (beside the camera) to where the figure stands: what the game is told to add to its place. */
    public Vec3 offset = Vec3.ZERO;
}
