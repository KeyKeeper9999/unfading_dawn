# Poses and animation: rules learnt

Read before writing or changing a `setupAnim`, a held item's pose or a mob's moves.

- How a shield sits in the hand is not guessed: `gen_shields.py` states the wanted pose in body terms (top up, face
  forward, arm hanging or raised to block) and works the item-display numbers out from the game's own chain of
  transforms; `shields_held.png` in the preview directory shows the result on a dummy. The 32x32 weapons get their
  grip brought into the hand the same way (`big_display` in `gen_items.py`).
- A held item always points at right angles to the arm, forward when the arm hangs. For a level thrust the arm is
  raised sideways (zRot), not forwards (see the knight's lunge); a long weapon carried at the hip is thrust by
  drawing the hanging arm back and driving it forward (the pitchfork). A level cut is the arm raised forwards and
  turned (yRot). A move with its own animation does not also call `swingArm`: the game's swing is laid over it and
  all that is seen is a wave of the arm.
- A weapon that is part of a mob's mesh (the spade, the maul) is posed by where its iron is: the pitch of the part
  is the sum of the trunk's, the arm's, the forearm's and its own, and the move is thought out in that sum (see
  `TaskmasterModel`). The part's own rotation in the mesh is already in its `xRot` when `setupAnim` begins: a move
  that sets a pitch outright must add it (`HELD`), or the weapon lies back along the arm. And a trunk bent far
  forward takes the hands down with it: for a blow that ends before the mob, the arms must still reach forward.
- A mob that does not walk is judged by its reach, and its reach is read, not guessed: the generator's sheet
  shows the poses the animation uses (`gen_mother.py`: the same numbers `setupAnim` adds), and where the hands are
  in each pose is worked out from the mesh. The entity's ranges follow from that. Parts that must not turn with a
  rooted mob are turned back by its body's yaw (`roots.yRot = -bodyRot`); hair hangs because the bends of
  everything above it are taken off it at the end of `setupAnim`.
- What a mob carries that is not a weapon is part of its model too and takes part in its moves: the barrowman's
  barrow hangs from the model's root, his hands are set on its handles whatever else he does.
- The game's humanoid turns its trunk about the neck: bent forward, the trunk leaves the legs behind. `WitheredModel.hips`
  runs last in `setupAnim` and takes whatever a move bent the trunk by, beyond the stoop, about the hips instead (neck,
  head and shoulders go forward and down). A move only sets `body.xRot`; it does not move the head or the arms to follow.
  The barrowman's hands are solved after that (`handles`): the barrow's tip gives the height of its handles, the arms
  reach to that height, and the barrow stands where the hands are.
- A blow that is sometimes punished is two moves, not one with a random tail: the digger's `PICK` and `PICK_STUCK`
  (about one in three) share the wind-up and the impact, and only the second leaves him bent over a pick fast in the
  floor. A held thing points across the arm: to bury its point the hands go back, towards the shins.
- A mob that is meant to be feared looks it: the butcher has his own bulk on the withered's bones (parts shown only
  on him), is drawn a tenth bigger and has a box to match; doorways he must pass are three high.
