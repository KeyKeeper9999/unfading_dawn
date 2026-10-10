# The Sky Beneath

Part of the map "Вышний Острог". What is common to the whole map is in `docs/map.md`. The story of the place (the day
that was buried, the Coming, what is set and what has grown) is in `docs/story.md`.

## The place

- The Sky Beneath (`tools/gen_depths.py`), the last place with anything to fight in it. It is under the ground: its
  floors are 51-57 layers under the valley's (`DOWN`), in the world's own stone, of which there are 72 layers under the
  grass (`gen_world.UNDER`; the plan's layers go under zero: `Map.low`). First it was a hollow hill over the ground with
  plates hanging in a void; the owner had it remade into caves (2026-10-10), keeping the way through it.
- How it is built: every floor is laid first (`lay`: columns, the layer of the floor, how high the roof of rock, or the
  hollow it belongs to if that has a sky), and the pits (`pit`); then all of it is hollowed at once (`carve`: a column
  is open as high as it is far from the wall - three cells at the wall, two more with every column inwards up to the
  roof); then the drift is cut, the skies are hung (`skies`) and everything is dressed; last a skin of rock is put
  round every cell the place wrote (`skin`), because under the ground the plan holds only what differs from the world's
  stone and the check takes a cell the plan does not have for room. While the place builds, `b.set` is watched: writing
  into a cell somebody else set is reported as a PROBLEM.
- Under the ground a cell that is not in the plan is rock: the place asks `Depths.open` (air), never `b.free`.
- Three kinds of way: thin ones (three wide, three to five high), wide winding ones (`way`: half-width, how far it
  swings off the straight line), and round chambers with a roof of rock. Only the three great hollows have a sky
  (`ROOMS`: the Still Water with the Brink, the Works, the Tear): their walls lean in to a neck (three cells at the
  wall, three more with every column, the neck four to six columns in: `Depths.shaft`), and `FLARE` layers under the
  lid the hollow is as wide as itself again and one column wider, so that nothing of the sky's own walls is seen past
  the lip.
- The sky of such a hollow: four sheets of mist one over another, three layers apart (`sky_veil[mist=0..5]`,
  `block/SkyVeilBlock`: one flat picture in the middle of its cell, seen through, drawn at full brightness, nothing to
  the touch; each sheet has thin places, thick places and gaps of its own, from a slow noise), and over the last the
  lid (`sky_lid`, `block/SkyLidBlock` with a block entity, `client/SkyLidRenderer`): the block is nothing, its
  renderer draws a level square 17 cells wide round it with the game's own picture of the gate to its End
  (`RenderTypes.endPortal()`). A hollow's roof is tiled with these (`LID_TILE`); one whose middle falls in the rock
  stands in the rock - what of its square is inside rock is not seen. The renderer gives its own bounding box and
  draws off screen: the block itself is seldom in sight.
- Pits: no floor, and at layer `VOID` the old sky block (`under_sky[tone=0]`, which kills whoever comes to stand on it:
  `UnderSkyBlock.stepOn`, `SoulMechanics.ground`), with a breath of mist two over it. Two in the Still Water (`PITS`),
  the crack before the chamber with the chest (three cells: jumped), the pit under the cache's one cell of rock, one
  beside the long way east (`SIDE_PIT`). Nowhere else can one fall: there are walls.
- The rest of the look is the biome's (`worldgen/biome/under_sky.json`, `gen_data.UNDER_SKY`: fog of a dark blue from
  26 to 124 blocks, `ambient_light_color`, a little white ash in the air) and the hour's (`PlaceNameBlock.BLUE_HOUR`,
  noon, only because the game darkens fog at night). Its boxes in `map/biomes.txt` are between two heights (two more
  numbers on the line: `MapBuilder.biomes`), the bottom of the world and a little under the sump: other places lie
  over it.
- The light is the crystals' (amethyst, an unseen lamp in each), a grid of unseen lamps over the floors of the hollows
  (`lamp_grid`, 13; 15 over the two lords') and along the ways (`lamps`, 11-12).
- The way: the Lower Grate in the south wall of the quarry's sump (`locked_gate[lock=depths]`; its key lies in the
  granary behind the Hollow Miller, chest `stubble_mill`) -> the drift (`drift_plan`): flights of eight stairs south
  and down with a landing between, a cross leg east (the clock is turned in it), two more flights south; forty layers
  down in all; from `RAW_Z` it is raw black rock -> a room (hearth `brink`) -> the Brink, a ledge in the wall of the
  first great hollow, four over its floor -> steps of rock down -> the Still Water (`WATER`): ridges with the
  long-armed in them, wheels of the clock half sunk, crystals; east of it a chamber across a crack (`ISLE`: a chest)
  -> a neck -> the Works: a round floor with a rim like a wheel's, an axle over it from wall to wall, a great wheel
  half in the rock either side (the Bolt; fog in the north arch, a seal in the south) -> a stair -> the chamber behind
  the pendulum (hearth `works`, an anvil) -> the Causeways, which are ways now: a thin one to a chamber of two
  clockhands, a wide winding one west to the gut's chamber, the cache (`ISLETS`), a thin one to the spire, another to
  hearth `tear`, and a wide one east to a wheel that does not turn (a chest) -> the Tear: a round hollow of still
  water in a rim of black teeth with ribs of rock leaning in over it (the Coming, rooted in its middle; fog north, a
  seal south) -> the room beyond, where the works of the clock lie (for what comes later: nothing is done there yet).
- Levels: `PLATE` the Still Water and the Works, `BRINK` four over it, `HIGH` the Causeways and the Tear, `VOID` the
  bottom of a pit. The two lords' marks are 78 blocks apart, so the Bolt's death does not lift the Coming's seal.
- A stair of the drift lies on the floor of its cell (the floor is one lower than the cell before it), so a flight is
  walked, not jumped, both ways. No beam stands over the landing cell a flight is climbed from: the check jumps where
  the game walks up a stair.
- `tools/preview_depths.py DIR` builds the place alone in a few seconds, walks it with `gen_map.analyse` from the
  sump, counts its dark places, and draws it: `depths_plan.png` (from above with the rock taken away; the necks edged
  in blue, a dot on every place a player can be) and `depths_cut_*.png` (cut through from top to bottom).
- Learnt: a roof of sky blocks is seen for what it is (the owner: "looks odd") - hence the sheets and the lid.
- Learnt: `Depths.line` paints a square round every cell of its line, so a short wide way is longer than it is: what
  must keep its own floor beside one (the stair behind the Works) is laid before it.
- Learnt (world): with the ground at y = 9 the firs of the world's own forest are clear of snow (see
  `MapBuilder.ORIGIN`); the map has no water higher than its valley, so nothing freezes either. A place that puts
  water high up in the taiga's own biome would have it freeze over about y = 26.

## Its dead and its lords

All nine are on `client/QuarryRenderer`, with a glow layer. Six are drawn by `tools/gen_dusk.py` and two by
`tools/gen_clock.py`, the Coming by `tools/gen_coming.py`; the palette and the painters they share are
`tools/duskkit.py` (night with stars, cracks of light, pale skin, flat hands, iron). The accent - violet and dark
blue - is kept small on purpose: in the glow textures and the crystals, nowhere else.

What has grown:

- `StrayShadowEntity` - lies on the floor and takes three tenths of any blow there (`FLAT`); rises for `RISE` (and
  is whole while risen: the time to strike) or snatches at the ankles from the floor (`SNATCH`).
- `LongarmEntity` - sits in a niche (`Depths.niche`) and neither comes out nor turns (`homeYaw`, `facing()` is
  `outward()`); `GRAB` draws whoever is before it in to the wall, `RAKE` takes the width of the way. Speed 0.
- `HandwalkerEntity` - the fastest thing here; `LUNGE` along its length, `WHIP` of the hind end round it.
- `DeeplingEntity` - flies as the sleep moth does; hangs still until somebody is near (phase 1, `wake`); `GULP`, and
  `GLARE`: a cone of darkness from the eyes in its mouth.
- `UsherEntity` - three and a half blocks tall, walks and does not run; darkens the sight of whoever is within nine
  blocks; `GRASP` lifts and throws (over an edge, if there is one), `REACH` is a long blow down.
- `GutEntity` - `CHARGE` in a straight line (it will go over an edge), `SLAM`, `GAPE`; having run into something it
  stands open (`STAGGER`) and takes half as much again.

What was set (amber and brass on them: the Founder's):

- `ClockhandEntity` - moves in beats (`SECOND`: stands, then is a step further); `STAB`, `SWEEP`, and `WIND`, in
  which it winds itself and is open.
- `BoltEntity`, the first lord - the pendulum. Phase 1: it swings across the floor on its own left-right axis
  (`AMPLITUDE`, `PERIOD`, the swing is synced as `SWING` and drawn through `SoulsMob.renderExtra` ->
  `BossRenderState.extra`), runs down whoever is in the way, tolls (`TOLL`: a ring that widens) and drops weights
  (`WEIGHTS`: `strikeLater`). At half health it breaks from the rod (`BREAK`) and walks on four hands of the clock:
  `STAB`, `SWEEP` (twice round), `LEAP`. `HIDE` 0.38 of 1000.
- `ComingEntity`, the second lord - rooted where it came through. Masked: `GAZE` (lines of violet shafts along the
  floor: `SunShaftEntity.dusk`, the sunshaft in another hue), `SLAM` (the head, the whole length of the neck; it lies
  until tick 84 and takes double there), `SWEEP` (an arm low), `CRUSH`. At half health `UNMASK`: the crown of arms
  opens round the disc; then also `PULL` (draws everyone to the maw, then bites) and `RAIN` (the sky comes down in
  marked places), and `GAZE` in three lines. `HIDE` 0.3 of 1000. It leaves `coming_soul`.

Left for the next rounds of the remaking (`docs/story.md`): the stray shadow's lying box, the long-armed as a stain of dark
on the wall, the gut as a slow tank; the Bolt's hall as his body, the Coming in the wall opposite the way in.

Left for later: the gear of the place (weapons, a shield, a spell, a charm, armour; the mask from the Coming's soul),
anyone to talk to, the mechanism on the ledge beyond the Tear and the tower it raises. The souls of the two lords
have no recipes yet.
