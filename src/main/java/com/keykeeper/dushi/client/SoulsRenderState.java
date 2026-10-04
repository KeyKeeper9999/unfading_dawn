package com.keykeeper.dushi.client;

import net.minecraft.client.renderer.entity.state.HumanoidRenderState;

/** What the models of the mod's humanoid mobs need besides the usual: which move is running and how far along it is. */
public class SoulsRenderState extends HumanoidRenderState {
    /** 0 when no move is running. */
    public int attackId;
    /** Ticks since the move began, with the partial tick added. */
    public float attackTime;
    public int phase = 1;
    /** Whether the main hand holds something; a bare fist punches differently from a blade. */
    public boolean armed;
}
