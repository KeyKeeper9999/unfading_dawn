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
  half in the rock either side, and in the rim eight housings of copper where the wheels of the wall stand (the Bolt
  hangs them; fog in the north arch, a seal in the south) -> a stair -> the chamber behind
  the pendulum (hearth `works`, an anvil) -> the Causeways, which are ways now: a thin one to a chamber of two
  clockhands, a wide winding one west to the gut's chamber, the cache (`ISLETS`), a thin one to the spire, another to
  hearth `tear`, and a wide one east to a wheel that does not turn (a chest) -> the Tear: a round hollow of still
  water in a rim of black teeth with ribs of rock leaning in over it; its south side, opposite the way in, is cut
  back to a face of rock (`BAY` either side of the middle) with the crack in it from the floor up (`Depths.rift`:
  seven cells wide, nine at the height of the Coming's shoulders; `under_sky`, black in its middle and the horizon's
  tone along its edges; the Coming stands before it; fog north,
  a seal in the east) -> the room beyond, east of it, where the works of the clock lie (for what comes later:
  nothing is done there yet).
- Levels: `PLATE` the Still Water and the Works, `BRINK` four over it, `HIGH` the Causeways and the Tear, `VOID` the
  bottom of a pit. The two lords' marks are 78 blocks apart, so the Bolt's death does not lift the Coming's seal.
- A stair of the drift lies on the floor of its cell (the floor is one lower than the cell before it), so a flight is
  walked, not jumped, both ways. No beam stands over the landing cell a flight is climbed from: the check jumps where
  the game walks up a stair.
- `tools/preview_depths.py DIR` builds the place alone in a few seconds, walks it with `gen_map.analyse` from the
  sump, counts its dark places, and draws it: `depths_plan.png` (from above with the rock taken away; the necks edged
  in blue, a dot on every place a player can be) and `depths_cut_*.png` (cut through from top to bottom).
- Learnt: a camera of `views()` set on the line between two cells or beside a wall takes a picture of the inside of a
  block (a post of the drift's timber, a ridge): `Depths.eye` gives the middle of a free cell with free cells on all
  four sides, `Depths.sky_eye` a place in a hollow's neck clear of its lip; in the drift the cameras are given whole
  numbers (`gen_world.world` adds the half).
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
  is whole while risen: the time to strike) or snatches at the ankles from the floor (`SNATCH`). Its box lies with
  it: `getDefaultDimensions` gives `LYING` (1.5 by 0.3) unless `risen()`, and `tick` calls `refreshDimensions` on
  both sides when that changes (the move and its clock are known on both).
- `LongarmEntity` - a stain of dark on a wall with arms out of it; no body. The mob stands in a niche
  (`Depths.niche`) and neither comes out nor turns (`homeYaw`, `facing()` is `outward()`); the model's stain
  (`pool`) is a plate half a cell and a little before the mob's middle, so that it lies on the rock round the niche
  and hides it - the niche keeps three cells of rock face, four high, clear before it for that. The long arms lie
  folded in the niche; `GRAB` and `RAKE` both show the hands first. `GRAB` draws whoever is before it in to the
  wall, `RAKE` takes the width of the way. Speed 0.
- `HandwalkerEntity` - the fastest thing here; `LUNGE` along its length, `WHIP` of the hind end round it.
- `DeeplingEntity` - flies as the sleep moth does; hangs still until somebody is near (phase 1, `wake`); `GULP`, and
  `GLARE`: a cone of darkness from the eyes in its mouth.
- `UsherEntity` - three and a half blocks tall, walks and does not run; darkens the sight of whoever is within nine
  blocks; `GRASP` lifts and throws (over an edge, if there is one), `REACH` is a long blow down.
- `GutEntity` - the slow tank of the place: a mound in a shell of two halves, carried on rows of hands (speed
  0.14). Shut, half of a blow is lost on the shell (`SHELL`); while `open()` a blow tells for `SOFT` - and it has to
  open to do anything. `HEAVE`: gathers itself and throws its weight forward a few paces (no shield holds it; into
  a wall it ends in `STAGGER`, open); `SLAM`: rears and comes down, and stands open until `SLAM_SHUT`; `GAPE`: for
  whoever is beside or behind it - the shell opens, the arms of its mouth come out over it, take and slow. It does
  not turn once a move has begun (`trackUntil`).

What was set (amber and brass on them: the Founder's):

- `ClockhandEntity` - moves in beats (`SECOND`: stands, then is a step further); `STAB`, `SWEEP`, and `WIND`, in
  which it winds itself and is open.
- `BoltEntity`, the first lord - the pendulum, and its hall, which is the rest of the clock and its body. Phase 1:
  it swings across the floor on its own left-right axis (`AMPLITUDE`, `PERIOD`, the swing is synced as `SWING` and
  drawn through `SoulsMob.renderExtra` -> `BossRenderState.extra`), runs down whoever is in the way, tolls (`TOLL`:
  a ring that widens), and the hall does three things, never the same twice running (`nextWorks`): `WEIGHTS` (a
  weight seen coming down on its chain over every marked place), `GEARS` (`letGo`: wheels run across the floor from
  one wheel of the wall to one about opposite, the first along the line nearest whoever it fights) and `HANDS`
  (`strikeHour`/`handsTick`: two opposite hands of amber light go half round the floor, `HAND_STEPS` steps, every
  place of a step lit `HAND_DELAY` ticks before it is struck). At half health it breaks from the rod (`BREAK`) and
  walks on four hands of the clock: `STAB`, `SWEEP` (twice round), `LEAP` - and the hall goes on by itself
  (`worksCooldown`, oftener under a quarter). `HIDE` 0.38 of 1000.
- `GearEntity` (`gear`; `client/GearRenderer`, `GearModel`, drawn by `tools/gen_clock.py`) - the works: not a mob,
  cannot be struck, is not saved. `MOUNT`: one of the eight wheels in the wall (`BoltEntity.tendMounts` hangs them
  again whenever one is missing, `MOUNT_FAR` from the mark, and says how fast they turn; they go when it dies);
  `ROLL`: a wheel let go - it winds where it stands for its warning, its line marked in sparks, then runs and
  strikes once whoever is in its breadth; `WEIGHT`: only seen (the blow is the Bolt's `strikeLater`).
- Learnt: the turn about x is what sends a limb of the Bolt forward. The thrust had it negative and the hand went
  behind it; the pose sheet (`stab`) is drawn from the same numbers, so look at it from the side.
- `ComingEntity`, the second lord - in the crack of the Tear's south face, its back to the rock. No head and no neck
  (it was a neck with the disc on top twice, and the owner saw a giraffe): a mass of shoulders with the disc set in
  its breast, a ring of light and a crown of twelve arms round the disc, two arms a house long, lesser ones down its
  flanks, a belly that opens. It neither walks nor turns (`homeYaw`): it turns the mass on its waist (`AIM`, synced,
  radians from straight out; `facing()` is turned by it, `renderExtra` gives it to the model). Its mark is a cell
  and a half before the face (`WALL` in the generator); the middle of the hall is `CENTRE` straight out, and nothing
  is struck outside `HALL` of that (`inHall`). Masked, all of it but the disc and the two arms is back in the rock
  (every masked pose says how far: `BACK_*` in the model): `GAZE` (a line of rays from the hole in the disc to the
  floor, out through whoever it looks at: `SunShaftEntity.ray`), `SLAM` (the whole mass out of the crack and down on
  its face, `SLAM_FAR` before it; it lies until `SLAM_LAIN` and takes double there), `SWEEP` (an arm low), `CRUSH`
  (both arms at the foot of the crack), `REACH` (its hands in the floor: arms come up out of holes under and round
  whoever it fights - `TendrilEntity`). At half health `EMERGE`: the mask goes, it comes out to the waist
  (`OUT_FRONT`), the crown opens, the ring shows, and the eye in the crack behind it opens. Then everything from
  further out and oftener, `GAZE` in three lines, an arm out of the floor now and again whatever else it does
  (`armClock`), `PULL` (draws the whole hall to the mouth in its belly, then bites), `RAIN` (the sky comes down in
  marked places) and `GLARE` - the eye looks: a fan of `GLARE_RAYS` rays from the crack, one after another across
  the hall from one side to the other. `HIDE` 0.3 of 1000. It leaves `coming_soul`.
- `TendrilEntity` (`tendril`; `client/TendrilRenderer`, `TendrilModel`, drawn by `tools/gen_coming.py`) - not a mob:
  a hole opens (its warning), an arm comes straight up through whoever stands on it, stands (`stands()`: synced, long
  for one summoned by hand), turns to whoever is nearest and comes down `LENGTH` along the floor through any shield.
- `SunShaftEntity` has three lights (`SUN`, `DUSK`, `AMBER`) and may be a ray from a place (`FROM_*`): the renderer
  turns the same column so that its top is where the ray comes from.
- The Coming's poses are not written in the model by hand: `gen_coming.py` has them by name (`POSES`: how far the
  waist and the mass are bowed and where the arms point - `aim` turns a direction into the turns of a part) and
  writes them into `ComingModel` between `// <generated poses>`; the animation goes from one to another.
  `gen_coming.py - x` prints where the middle of the disc is in each (the rays and `SLAM_FAR` are taken from that).
- Learnt: a thing that stands in a wall must not turn with the mob's yaw - everything of it that is in the wall
  swings out of it. Fix the yaw and turn a part.

Left for later: the gear of the place (weapons, a shield, a spell, a charm, armour; the mask from the Coming's soul),
anyone to talk to, the mechanism on the ledge beyond the Tear and the tower it raises. The souls of the two lords
have no recipes yet.
