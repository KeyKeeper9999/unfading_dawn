package com.keykeeper.dushi.client;

import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;

/** Render state of the bosses: the move in progress, how far along it is, and the phase of the fight. */
public class BossRenderState extends LivingEntityRenderState {
    public int attackId;
    public float attackTime;
    public int phase = 1;
    public boolean onGround = true;
    /** Whatever else a kind of mob wants its model to know (SoulsMob.renderExtra): how far the Bolt has swung. */
    public float extra;
}
