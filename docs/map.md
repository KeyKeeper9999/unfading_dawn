# The map: how it is built and checked

The places themselves are in `docs/places/`, one file each.

`gen_world.py` writes `map/high_stockade.plan.txt` (the same format as the buildings; about 4.9 million blocks),
`map/views.txt`, `map/light.txt`, `map/census.json` (for `balance.py`) and `map/biomes.txt` (the boxes of the world that
get a biome of their own - the Mire's, the shore's: `MapBuilder.biomes` fills them, and says in `map.log` how many cells
it changed). The plan's origin is the place where the newcomer wakes; the game puts it at
0, 10, 0 and turns the plan a quarter, so the plan's north is the world's west and the way through the map leads into the
sunset. The dead of the town are of tier 0, of the lower fortress tier 1, of the upper city tier 2 (`World.tiers`);
the quarry's are of tier 0 and strong by birth. The valley's ground is the plan's layer 16 (`GY`): the flat world has
73 blocks of ground under its grass (bedrock, 69 of stone, 3 of dirt: the workflow), of which the plan can write the
72 over the bedrock (`gen_world.UNDER`) - its layers go under zero, down to `Map.low` (-56), and the file is written
from there (`size` is the whole height, `origin` counted from the lowest layer). The quarry uses thirteen of them, the
Sky Beneath the rest.

A new place is a new file like `gen_village.py`: it builds into the world's `Map` at an offset, and `gen_world.py` gives
it a piece of the gorge (`in_gorge`), a road, and - if its hour differs - a passage where the clock is turned. The yard
of the first hearth is the hub the roads start from.

The names of the places are thresholds across the ways in, each on its own side of the line between two places. Views
whose name begins with `as_player_` are walked to by the test as the newcomer is, before the camera is set free.

`gen_world.py` ends with a check of the map as a place to play, and every change to the map must leave it clean:

- it walks the map the way a player can (steps, jumps, drops, ladders, levers, seals lifted lord by lord and locked
  gates opened when their key is in reach: `gen_map.STAGES` is the order) and reports what is
  reached at each stage, chests, shards and mobs nobody can reach, places that are traps, that nobody gets out of the
  gorge (no place at the edge of the plan is reached), and that the cathedral cannot be entered except by the covered walk;
- it works out the light like the game does (its numbers matched the game's at every probe) and reports walkable
  places under a roof that are too dark, and how much of the town's streets the lamps reach;
- previews: `world_ground.png`, `town_ground.png` (a dot on every reachable place), `town_roofs.png`, `town_iso.png`.

Things learnt the hard way:

- In a world whose game mode is adventure the game drops a newcomer through the floor of the spawn if the place is not
  loaded yet (they fell into the void). `SoulMechanics.settle` puts them back on it at login and respawn.
- Whatever a player can stand on is a stair: a boulder beside a fir let the check climb the needles onto the plateau,
  and the low plinth of a wall let it walk over the corner. In the gorge trees stand clear of rock and walls and are
  bare to four blocks, barricades are three high with nothing beside them, walls have no ledges.
- Nothing may be put in front of a door (`Village.kept`): a barrel there shut a house with a chest in it.
- A gate's tiles are "built by hand" and get no wall from `walls`: whoever builds the gate builds the wall over it
  (the yard's arch once hung in the air).
- Blocks set one by one into an empty world leave the light wrong (whole storeys black, or everything fully lit).
  `MapBuilder` therefore relights every chunk after building (`initializeLight`, then `lightChunk`).
- Rock is put only under what stands on the ground (`bedrock`), straight down: corbels and other overhangs must not grow rock.
- `chiseled_sandstone` has a creeper's face on it; the city's stone is smooth and cut sandstone.
- A flat world's sea is at y = -63 and the game cools a biome with height above its sea: with the ground at y = 63 the
  forest outside the plan grew up under snow, which the plan's own forest did not have. The ground is at y = 9 (it was
  at -47 until the Sky Beneath needed stone under the map): the cooling starts 17 over the sea, is a twentieth of a
  degree to forty blocks with a noise of eight blocks either way, and the taiga's 0.25 goes under 0.15 - snow, and ice
  on open water - about y = 26. Firs on the ground at 9 stay under that; water on a height of the map would not.
- With natural regeneration switched off (as the map does) hunger is held full. The game's own mobs do not spawn
  (`SPAWN_MOBS`): the night would fill with them.
- A block with a direction of its own must turn with the plan (`rotate`): the fog gate did not, and stood edge-on in
  every doorway of the map. What is built late (corbels, lamps) can land in a stair: the check counts headroom.
- Fog fills its opening or it is walked round: the fog before the Stone-Eater stood where the cave had already eaten
  the drift's wall. `b.fog` fills the free cells of its box (so the box can take in the point of an arch), and
  `gen_world.py` reports any fog, seal or grate with a free cell beside it in its own plane.
- A gallery over a lord's hall has its rail from wall to wall: boards that stop short are a way down to him.
- A lord's death lifts every seal in a box of forty blocks either way round his mark (`FogGateBlock.lift`): two lords
  of one place must stand further apart than that, or the first opens the way past the second.
- Under the ground the plan holds only what differs from the world's stone: the hollows, and a skin of rock round them
  so that the check sees walls (`Quarry.skin`, `Depths.skin`). The check drops and the lamps' light is followed down
  to `Map.low`; the sky's light is not followed under layer 1. Above the ground everything is in the plan; a place cut open from
  above gives its columns to `open_sky`, or firs are planted in the air over it.
- Painted pots swallow what is put into them; a player who may not build cannot use them (`SoulMechanics.onUsePot`).
- The check knows water (`gen_map.is_water`): it is stood in and swum, left only onto what is level with its top,
  and nothing is jumped from it. It found the market's well to be a trap (a grate lies over it now).
- The colour of a place is its biome's (`worldgen/biome/mire.json`: fog, sky, water, leaves, and how much light the
  sky gives and of what colour), laid over the place by the map's builder. But the game darkens a biome's fog and
  sky by the hour and paints them orange towards a low sun, and no biome can take that away: a place with a colour
  of its own has an hour of full day (`PlaceNameBlock.MURK`: noon, so that the sun is overhead and not in the
  eyes) and makes its own gloom. Clouds stay white whatever the biome: its `cloud_fog_end_distance` hides them.
- Nothing has more than 1024 health: the game cuts the attribute off there (the Wallower was born with 1300 and had
  1024). A lord who needs more has a hide (`HIDE` in the entity: the share of a blow that gets through), and
  `balance.py` counts it.
- A willow is a stair too: nothing of one that can be stood on is lower than five blocks over the ground.
- Time does not move; the hours (`PlaceNameBlock.DUSK`, `NIGHT`, `MURK`, `FOREDAWN`) were chosen by looking at pictures
  (`*_TRIED` in `gen_world.py` takes the same views at other hours). `time set` is for the whole world: with several
  players in different places the last threshold crossed wins. Before sunrise the game lights the edge of the sky from
  about 22500 and shows the sun itself from about 22900: 22600 is a glow with no sun in it, 22200 is plain night.
- A camera set down in a wood looks at bark, a lamp or the back of a tent: the views of such a place stand on its ways
  (the cells a place keeps free), and a lord is not looked at across the post in the middle of his hollow.
- A lord whose charge ends against what does not give needs something in his arena that does not give: the Shatun's
  hollow was a bare ring, with nothing but its walls to lead him into.
- The world's own ground is grass over three of dirt over stone, and `MapBuilder` clears only what is over it: a place
  lower than the valley (the sea) writes air into the ground's own layer and whatever it wants below.
- A fence or a wall is not stepped up on from the ground beside it (`rail` in `gen_map.analyse`); from anything a block
  high beside it, it is - so a fence that is to keep a place in has nothing a block high within a cell of it.
- A fence is a block to the check and a block and a half to the game: a deck one step higher than another, with the
  lower one's rail beside it, is a way overboard to the check. The Daybreak's deck is one level from end to end, her
  cabin's roof has no way up, and nothing on her deck stands within two cells of the rail.
- Fog in the open needs a doorway built for it: posts and a beam on a ship, an arch of rock on a shoal.
- What the game does to a biome's colours by the hour is in its `data/minecraft/timeline/day.json`: every hour multiplies
  fog, sky and the sky's light (at 23600 by about 0.7, 0.7 and 0.55). `.ci/ref.txt` lists files of the game's jar to copy
  into `ci-out/ref/` - that is how its own biomes were read for the shape of `minecraft:audio/ambient_sounds`.
- A place in the open may want its own grass: a biome's `grass_color` paints turf, `short_grass` and `tall_grass`
  alike, so a field of corn is tall grass in a biome whose grass is the colour of rye (the Stubble) - no block of the
  mod's, nothing for a player to trample. The check walks through it (`PASSABLE_MORE`, `NO_FOOTING`).
- The picture numbers of a run (`m137_...`) are the line numbers of `map/views.txt`: a new place's views shift every
  number after them, and `MAPS` in `tools/ci_sheets.py` is cut by those numbers.
- Boulders strewn by chance make pits: `Shore.no_pits` fills whatever on the strand is not come to from its way.
- The views that play the endings of the clock (`clock_rope`, `clock_scene_*`, `clock_end`, `clock_figure_*`, `clock_seat_*`) are the very last of
  `map/views.txt` (`gen_world.ending_views`): the bell lays down everything of the mod's that lived (and lets nothing join the world after
  it), and the screen of an ending stays open over every picture after it - put before the lords they photographed an empty world and black
  screens. `MapSelfTest` closes the screen of an ending before any view but `clock_end`, and before the travelling.
