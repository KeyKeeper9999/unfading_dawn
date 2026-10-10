# The Sky Beneath

Part of the map "Вышний Острог". What is common to the whole map is in `docs/map.md`. The story of the place (the day
that was buried, the Coming, what is set and what has grown) is in `docs/story.md`.

## The place

- The Sky Beneath (`tools/gen_depths.py`), the last place with anything to fight in it. South of the White Pit a hill
  of black rock goes down into the sea as a cape (`massif`, `Depths.mask`); it is hollow (`Depths.hollow`: every
  column more than `WALL` columns inside it). The ground of the valley is only sixteen blocks deep, so the place could
  not go under the fields: it is over the ground, in a hill of its own, and reached from below.
- Its sky is a block. `block/UnderSkyBlock` (`under_sky[tone=0..11]`) is drawn at full brightness whatever the light
  (`emissiveRendering`, `getShadeBrightness` 1), so a wall of it has no faces and no corners to the eye: black with a
  few stars at tone 0, a dark blue with a violet seam at tone 11. The roof of the hollow is tones 0-3 (`roof_tone`: one
  lighter band across it), its sides are lightest at layer `HORIZON` and black a dozen layers over and under it
  (`sky_tone`), and its bottom, at layer `VOID`, is black again. The bottom is not a floor: whoever stands on the
  block is gone (`UnderSkyBlock.stepOn`, and `SoulMechanics.ground` for whoever comes to it otherwise); the stain is
  left where they last stood (`SoulMechanics.deathPlace`). The star textures are several to a tone, chosen by the game
  at random (`gen_blocks.py`).
- The rest of the look is the biome's (`worldgen/biome/under_sky.json`, `gen_data.UNDER_SKY`: fog of the sky's own
  blue from 26 to 124 blocks, `ambient_light_color`, a little white ash in the air) and the hour's: the place is kept
  at noon (`PlaceNameBlock.BLUE_HOUR`, 6000) only because the game darkens fog at night - nothing of the game's sky
  is seen. `map/biomes.txt` lists it last, so it lies over the shore's where the cape stands in the sea.
- What is stood on hangs between the two skies: plates of black rock (`Depths.plate`: thin at the edge, thicker
  inwards). On the wet ones lies `night_shallows` - the mod's shallows in a film that shines a little, over black
  concrete and obsidian: a mirror with stars in it. The light is the crystals' (amethyst, an unseen lamp in each) and
  a grid of unseen lamps over every floor (`lamp_grid`); nothing else gives any.
- The way: the Lower Grate in the south wall of the quarry's sump (`locked_gate[lock=depths]`; its key lies in the
  granary behind the Hollow Miller, chest `stubble_mill`) -> a drift that climbs under the plateau, turns east (the
  clock is turned in the cross leg) and south, and stops being cut stone -> a room in the last of the rock (hearth
  `brink`) -> the Brink, a ledge over all of it -> steps down a spur -> the Still Water, the great plate (`WATER`,
  with holes in it, `HOLES`): ridges with the long-armed in them, wheels of the clock half sunk, crystals; a lesser
  plate off its east side across three cells of nothing (`ISLE`: a chest) -> a neck -> the Works: a round floor with a
  rim like a wheel's, an axle over it on two frames, great wheels standing either side (the Bolt; fog in the north
  arch, a seal in the south) -> a stair in the open -> the plate behind the pendulum (hearth `works`, an anvil) -> the
  Causeways: ways three wide over the sky from islet to islet (`ISLETS`: west a gut's, a cache one cell wide to get
  to, a spire with something in it, east a wheel that does not turn) -> hearth `tear` -> the Tear: a bowl of still
  water in a rim of black teeth with ribs of rock leaning in over it (the Coming, rooted in its middle; fog north, a
  seal south) -> the ledge beyond, where the works of the clock lie (for what comes later: nothing is done there yet).
- Levels: the bottom of the sky 1, the Still Water and the Works `PLATE` (10), the Brink 14, the Causeways and the
  Tear `HIGH` (16). The two lords' marks are 78 blocks apart, so the Bolt's death does not lift the Coming's seal.
- What is not come to (`Depths.far`): rocks that hang as the plates do, each with a crystal; wheels of the clock
  standing in nothing; chains down out of the stars with a weight, a pan or a crystal at the end. Each is kept eight
  cells clear of every floor.
- There is no wall at the two doors of fog, so each stands in an arch of its own (`Depths.arch`).
- `tools/preview_depths.py DIR` builds the place alone in half a minute, walks it with `gen_map.analyse` from the
  sump, and draws it from above, from the side and cut through.
- Learnt: a roof of sky one block thick is seen for what it is - where it steps up, the side of the rock over the
  lower step shows, and the whole vault is a lattice of grey. The sky is the whole thickness of the roof but its top
  block (`Depths.hill`), and the two wall columns are sky to the same height. Light: an unseen lamp of 13 every third
  or fourth cell four over the floors, 15 over the two lords' floors, 11 along the drift; at 9 and 8 the pictures were
  black.
- Learnt: the sea's `undertow` cells stay in the Map after something is built over the sea - the hill takes its own
  columns out (`Depths.hill`), or nothing at the level of the sea is walked. A beam over the cell before a step shuts
  the step for the check (it jumps, where the game walks up a stair): timber frames of the drift keep off the steps.

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

Left for later: the gear of the place (weapons, a shield, a spell, a charm, armour; the mask from the Coming's soul),
anyone to talk to, the mechanism on the ledge beyond the Tear and the tower it raises. The souls of the two lords
have no recipes yet.
