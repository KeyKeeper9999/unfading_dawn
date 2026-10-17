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
- `ComingEntity`, the second lord - a weaving, not one thing: three necks twisted into a rope (`neck0..7`, three strands
  to a length), the disc on the end of the rope with its ring of light and crown of twelve arms, and the rope growing
  out of a knot of the same necks (`trunk`: a core under wound `coil`s, a mouth in its front) that has six arms a house
  long, a tail, two lesser necks and ends hanging under it. (It was a neck like a tower with the disc on top, then a
  mass of shoulders with the disc in its breast; the owner asked for the long neck and the mask back - as a chthonic
  weaving looking out of the wall, not a giraffe - and for it to come right out and walk in its second phase.)
  Masked, the knot is in the dark behind the face of the rock (`IN_ROCK` in the generator: every masked pose has the
  body up and back) and the rope hangs out of the crack over the hall with the mask on it; two arms are out to the
  floor; and `roots` - what else there is of it - has crept out along the rock round the crack. It neither walks nor
  turns: `AIM` (synced, radians from straight out) turns the model's `turn`, whose pivot is the foot of the crack
  (`rift()`: `FACE` behind its mark), and every blow of the phase is counted from there. `GAZE` (a line of rays from
  the mask to the floor), `SLAM` (the rope thrown down along the floor: the mask lies at the end of it until
  `SLAM_LAIN`, the rope itself strikes whoever is under it), `SWEEP` (an arm low), `CRUSH` (both arms at the foot of
  the crack), `REACH` (arms out of holes in the floor: `TendrilEntity`).
  At half health `EMERGE`: the mask goes at `EMERGE_BREAK`, both hands are thrown out into the hall, the knot is
  dragged out of the crack after them, it stands up, and at `EMERGE_EYES` the eyes open. From then it is `loose()`:
  its box is the knot's (`OUT`, five across), and it walks - not by the game's path-finding (a thing five blocks wide
  that starts in a wall finds no path) but by itself, in `roam`: it sets its own speed towards whoever it fights until
  it is `NEAR`, never further than `ROAM` from the middle of the hall, and turns `TURN` degrees a tick, so its blows,
  which go the way it faces, can be walked round. `ComingRenderer` draws it the way it faces (the game would turn a
  body after its head) and whether or not its box is in sight. Out of the crack it also has `STAMP` (whoever is under
  it: the knot let down on them, and it lies until `STAMP_LAIN`), `CROSS` (it goes to the far side of the hall from
  whoever it fights, over anybody in its way), `PULL` (draws the hall to the mouth in the knot, then bites), `RAIN`
  (the sky comes down in marked places), and `GLARE` - the eyes look: either the great one (a fan of `GLARE_RAYS` rays
  from the crack across the hall) or the lesser ones, one after another, each at where whoever it fights is going.
  `HIDE` 0.3 of 1000; double while it lies (`open()`). It leaves `coming_soul`.
- `FaceEntity` (`coming_face`) - the disc as something to strike: the disc is a dozen blocks from where the Coming
  stands, so the Coming keeps this box where its disc is (`discNow()`: the model's poses gone through as the model
  goes through them) and a blow on it is a blow on the Coming, for half as much again. Nothing is drawn of it. A swing
  at it costs stamina like any other (`SoulMechanics.onAttack`).
- `WallEyeEntity` (`wall_eye`; `client/WallEyeRenderer`, `WallEyeModel`, drawn by `tools/gen_coming.py`) - the eyes
  that open in the rock when it is out: the great one in the crack it left, and lesser ones the Coming looks for a
  place for along every spoke from the middle of its hall (`openEyes`: a face of rock to the hall with rock over and
  under it and nothing before it). An eye follows whoever looks at it with its ring; it shuts when the Coming is dead
  or back in its crack; summoned by hand it stays (`{Great:1b}`, `Size10`, `Roll`).
- `TendrilEntity` (`tendril`; `client/TendrilRenderer`, `TendrilModel`, drawn by `tools/gen_coming.py`) - not a mob:
  a hole opens (its warning), an arm comes straight up through whoever stands on it, stands (`stands()`: synced, long
  for one summoned by hand), turns to whoever is nearest and comes down `LENGTH` along the floor through any shield.
- `SunShaftEntity` has three lights (`SUN`, `DUSK`, `AMBER`) and may be a ray from a place (`FROM_*`): the renderer
  turns the same column so that its top is where the ray comes from. `DUSK` - the Coming's - is drawn as the beam of
  a beacon (the game's own `BeaconRenderer.submitBeaconBeam`, laid along the ray and stretched to its length) in a
  sheath of light (`dusk_beam.png`, `gen_beam.make_sheath`); the other two are the soft shafts they were.
- The Coming's poses are not written in the model by hand: `gen_coming.py` has them by name (`build()`: where the body
  is, how the knot is tipped, a turn forward for each length of the rope, and where each of its six hands is - `limb`
  finds the elbow) and writes them into `ComingModel` between `// <generated poses>`, a row of three turns for every
  part of `POSED`; the animation goes from one to another, and the walk is four of them (`walk0`, `walkb`, `walk1`,
  `walka`) gone through by how far it has gone. Where the disc is in the poses the entity strikes or looks from is
  written into `ComingEntity` between `// <generated places>` (`KNOWN`); `gen_coming.py - x` prints all of them.
  The preview draws the masked poses in a face of rock with the crack in it (`pv_wall`, not in the game).
- Learnt: the game looks for an entity in the box it is struck by, and does not draw what is not in sight: anything
  much bigger than its box (the Coming, a ray that leans) needs `affectedByCulling` false in its renderer.
- How the hall is looked at in the map's test (`map/views.txt`, written by `gen_world.py`; `MapSelfTest`): a view whose
  name ends in `_rouse` is not a picture - the lords near it are hurt to under half (the Coming then comes out; left
  alone it heals but stays out); one that ends in `_stray` sets them down at its second place. With nobody to fight
  the Coming stands in its crack, squarely before the great eye, and hides all of it (an afternoon went on looking for
  an eye that was there): `depths_tear_strayed` and `depths_tear_great_eye` are taken with it set down in the hall.
  The test also says how many eyes are open and where the great one is (`... eyes in the rock`).
- The full pictures in `ci-shots` can be a run behind (that push may fail, and a picture asked for by the branch's name
  may come from a cache): ask for them by the commit of the branch, and trust the sheets in `ci-out` first.
- Learnt: a thing that stands in a wall must not turn with the mob's yaw - everything of it that is in the wall
  swings out of it. Fix the yaw and turn a part.

Left for later: the gear of the place (weapons, a shield, a spell, a charm, armour; the mask from the Coming's soul),
anyone to talk to, the mechanism on the ledge beyond the Tear and the tower it raises. The souls of the two lords
have no recipes yet.

## The hall of the clock (2026-10-18)

East of the Tear (`gen_depths.py`, `END`, `HALL_R`, `SOCKET_R`, the threshold `clock_hall`): a round hall with a dial of copper
and froglight in its floor and seven `clock_socket` blocks on a ring round it (angles -135 degrees + 45 each, a copper spoke
to each); a chest (`depths_tear`) and a flask shard beside the way in. Each socket carries the sign of its place
(`ClockSocketBlock.Socket`: quarry, mire, firwood, shore, stubble, depths, keeper); whoever touches an empty one with the
matching key among their things puts it in for good, and when all seven are in `Clock` starts the clock (see
`docs/story.md`, "Часы, башня и два конца"). The seventh key, `clock_key`, is given by the keykeeper once the Coming has
fallen (`KeykeeperEntity.mobInteract`); the six others are the keys of the six gates. Views: `depths_clock_hall`,
`depths_clock_sockets`. Not checked in play: how the hall reads at the hour of the place, and the sockets' glow.

