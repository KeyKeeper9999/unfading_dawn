# Всратые души (vsratie_dushi)

A souls-like inside Minecraft: NeoForge mod for Minecraft 26.3. The owner writes in Russian; texts exist in Russian and English.

The mod is a souls-like "in the spirit of", with its own world, names and designs: it does not copy the characters, bosses,
items or names of any existing game, and every sound is synthesised from scratch. Keep it that way when adding content.

## Building and checking

Nothing can be built on a machine without access to the NeoForge and Mojang servers, so the build runs in GitHub Actions
(`.github/workflows/build.yml`) on every push to `main`. The workflow publishes its results to the `ci-out` branch:

- `build.log` - compiler output;
- `vsratie_dushi.jar` - the mod (the jar never has a version in its name, so it can simply be overwritten);
- `server.log` - a dedicated server started with `DUSHI_SELFTEST=1`: `SelfTest.java` spawns every mob, makes them fight iron
  golems, drills every move of every mob, drives a stand-in player through souls, levelling, the flask, spells, the hearth,
  the shop and death, prints `DUSHI SELFTEST ...` lines and stops the server;
- `shots/*.png` and `client.log` - only if the file `.ci/client` exists: the real client is started under xvfb with
  `DUSHI_CLIENTTEST=1`; `client/ClientSelfTest.java` makes a world, lines up mobs, blocks and items, takes screenshots
  and quits. This is how the look of the mod is checked. It adds about five minutes to the run. The client renders in
  software there, a few frames a second, so a picture of a move is taken with the move frozen at a chosen tick
  (`attack(type, id, tick)` in the script) - never by timing. The script also presses the real roll key, dies, respawns
  and walks into the stain, and prints what it measured as `DUSHI CLIENTTEST ...` lines in `client.log`.

The server test ends with `WorldTest.java`: gear numbers, recipes and chest loot as loaded by the game, both buildings set
down from their plans and counted, the marks raising, losing and raising their mobs, and both buildings looked up in the
world the server generated. The client test makes an ordinary world, works on a stone stage at y = 180, and at the end
flies to the nearest outpost and stockade and takes pictures from fixed places of their plans (`view(...)`).

`git fetch origin ci-out` and read the files; the commit message of `ci-out` names the commit of `main` it was built from.

The map (see "The map" below) is built by the same workflow when `.ci/map` exists: a dedicated server is started in a
flat world of forest (the `generator-settings` in the workflow: its top layer is at y = -47) with
`DUSHI_BUILDMAP=map/high_stockade.plan.txt`; `MapBuilder.java` fells the forest where the map goes, sets the plan down,
works the light out again, sets the rules of the world, saves and stops; the world folder is zipped. `ci-out` then also has:

- `map.log` - `DUSHI MAP ...` lines: the size of the plan, what stands in the world (a count of block entities), and the
  light read out at the places listed in `map/light.txt`;
- `High_Stockade.zip` - the map as players get it (unpack into `saves`); the workflow gives the world its shown name
  (`tools/name_world.py`), `map/icon.png` and `map/readme.txt`;
- `mapclient.log` and `shots/m*.png` - only if `.ci/mapshots` exists: the client opens the saved world the way a player
  would (`client/MapSelfTest.java`, `DUSHI_MAPTEST=map/views.txt`), says how a newcomer starts (`DUSHI MAPTEST ...`),
  flies to every view of `map/views.txt` and takes a picture, then throws every lever and kills the bosses and says
  what the doors and the seals did.

`.ci/skip_server` skips the server self-test (to save minutes while only the map is worked on); delete it and restore
`.ci/client` before finishing, so that everything is checked once together.

Other switches: `.ci/dump_api` makes the workflow write `javap` signatures of the whole game to `ci-out/api/` (useful when
moving to a new Minecraft version: names change a lot between versions); `.ci/ref.txt` lists class files to copy out.

## Layout

- `src/main/java/com/keykeeper/dushi/` - `Dushi` (all registries), `PlayerSoul` (souls, stats, stamina of a player; a data
  attachment synced to the player's own client), `SoulMechanics` (the rules: souls for kills, the stain on death, stamina,
  rolling through blows, backstabs, guard break), `Net` (messages), `Spell`, `Shop`.
- `Forge` (the recipes of the old anvil: parts and souls, each on a shelf: weapons, shields, armour, magic, supplies),
  `block/ForgeBlock`, `client/ForgeScreen` (a tab for every shelf).
- `block/FogGateBlock` - fog before a boss; `sealed=true` is the amber seal that lets nobody through until the boss of
  that room dies (`lift` clears the nearest fog and every seal within reach of the boss's mark).
- `block/PlaceNameBlock` - a threshold nobody sees or feels: a player who steps through it is shown the name of the
  place (`place.vsratie_dushi.<id>` in the texts; the list of places is the enum in the block). Every place has its
  hour, and in a world whose time stands still the threshold sets the clock (`SoulMechanics.hour`); a name is shown
  once, when the player first comes to the place (`PlayerSoul.placesSeen`, by id, saved with the player); `to_night` and
  `to_dusk` have no name and only do that. No item: a map's builder sets it; with a mob mark in hand it can be seen
  and removed in creative. A hearth remembers the hour it was rested at, for those who come back to it by dying.
- `block/HearthBlock` - rest, level up, and travel: a player can go from the hearth they sit by to any other they have
  rested at (`HearthBlock.travel`, `client/TravelScreen`; the list is `PlayerSoul.rests`, saved with the player, and is
  sent to the client when the hearth's screen opens). A map's hearth has a name, `site=` in its block state
  (`HearthBlock.Site`, text `hearth.vsratie_dushi.<id>`): a new hearth of a map needs a new entry there and its text.
  Arriving is resting: the hour of the place comes back with it.
- `block/LockedGateBlock` (`locked_gate`) - an iron grate across a way that a map keeps shut until a key is found
  somewhere else: whoever touches it with the key among their things opens the whole gate, for good (the key is kept).
  `lock=` in the state says which key (`LockedGateBlock.Lock`): a new gate is a new entry there and a new key item.
  No item: maps set it.
- `block/ShardBlock` (`shard_heap`) - a flask shard glowing in a heap of ash: taken with a click, once. Flask shards
  are not put in chests: they lie in such heaps or are left by the lords, about two to a part of a map.
- `MapBuilder` (build only) and `client/MapSelfTest` (build only) - see "The map".
- `Gear` - what the rules know about any piece of gear, the game's own included: stamina cost, scaling and backstab of
  a weapon (by item tag for vanilla ones), stability of a shield, weight of everything worn or held, and the burden that
  follows from the weight (it decides the roll). `ArmorSet` - the mod's four suits and the gift of each.
- `block/MobMarkBlock` + `MobMarkBlockEntity` - a ring of ash where a mob of a building rises: when a player comes near,
  and again after any rest at a hearth or death within 96 blocks. A slain boss stays slain; a living one goes back and heals.
  A mark has a `tier` (0-3): the same mob with more health, harder blows and more souls (`SoulsMob.TIER_*`), so that a
  later place can use the dead of an earlier one.
- A player within six blocks of a hearth is not noticed by the dead (`PlayerSoul.atHearth`, `SoulsMob.setTarget`); one
  they are already fighting is not let off by running to it.
- `world/` - `Plan` reads a building from `data/vsratie_dushi/plan/NAME.txt`, `PlanStructure` picks a level, dry place
  and one of four turns, `PlanPiece` sets the blocks. The worldgen JSON only names the plan.
- `entity/` - `SoulsMob` is the base of every enemy: an attack is a "move" with a wind-up, an impact tick and a recovery,
  known on both sides so the model can show it. `SoulsCombatGoal` walks the mob to its target and lets it choose moves.
  What moves are made of: `hitArc` (a cone before the mob, or in any direction - a tail - and, if asked, through a
  raised shield, which it knocks down), `hitAround`, `shove`, and `strikeLater` - a blow that lands on a place on the
  ground after a delay, the place marked until then (dust sifting down for a falling rock, chips for a crack running
  along the floor). The dead do not turn on each other: `setTarget` ignores another `SoulsMob`.
- The quarry's own: `BlindCrawlerEntity` hears instead of seeing (`hears`: a walker from 13 blocks, somebody sneaking
  only from 3.5) and calls the others; `UnhewnEntity` is a statue (phase 1, nothing of it moves) until somebody comes
  within 4.5 blocks or strikes it; `TaskmasterEntity` and `StoneEaterEntity` are the lords (the second digs itself
  under the floor: its model's root part `all` goes down, nothing reaches it there); `PowderPotEntity` (its `blast`
  hurts the dead too) and `WedgeEntity` are what is thrown. These four mobs share `client/QuarryRenderer`.
- `client/` - models, renderers, `SoulsHud`, `HearthScreen`, `ShopScreen`, the roll key.
- `tools/` - everything generated is generated from here; run `sh tools/build_assets.sh [preview dir]` from the repo root:
  - `modelkit.py` - entity models described once in Python: texture layout, the mesh part of the Java model class (written
    between the `// <generated mesh>` markers) and a software preview render;
  - `gen_withered.py` (ten kinds on one model: prisoner, guard, archer, acolyte, the town's townsman, butcher and
    watchman, the quarry's digger, barrowman and sapper), `gen_knight.py`, `gen_keykeeper.py`, `gen_overseer.py`,
    `gen_chaplain.py`, `gen_hound.py`, `gen_gravedigger.py`, `gen_crawler.py`, `gen_unhewn.py`, `gen_taskmaster.py`,
    `gen_stone_eater.py` - the mobs;
  - `gen_items.py` (icons), `gen_shields.py` and `gen_blocks.py` (box models), `gen_sounds.py`, `gen_lang.py` (all texts);
  - `gen_armor.py` - worn textures (64x32, the game's armour layout), icons and equipment definitions of the suits;
  - `gen_structures.py` - the buildings, block by block, with top-down and isometric previews; bars, fences and walls
    are written already joined to their neighbours, because nothing joins them during world generation;
  - `gen_data.py` - recipes, chest loot, tags, structure and structure-set JSON;
  - `check_assets.py` - checks that everything registered has its files and names;
  - `gen_world.py` - the map (not run by `build_assets.sh`: `PYTHONPATH=tools python3 tools/gen_world.py [preview dir]`);
    it puts together the land, `gen_village.py` (the Dead Borough), `gen_map.py` (the High Stockade) and
    `gen_quarry.py` (the White Pit);
  - `balance.py` - the sums of the map's progression (`PYTHONPATH=tools python3 tools/balance.py`).

  Never edit generated textures, models, `sounds.json` or the lang files by hand: change the generator and run it.
  Animations (`setupAnim`) are hand-written Java; when a rest pose changes, update the pose in the generator's preview too.

## Conventions

- Item icons are 16x16 in the style of the game's own; 32x32 only for the two-handed weapons. Mobs and blocks are models.
- A weapon's damage, speed and reach are ordinary item attributes; stamina cost, scaling and backstab live in `WeaponItem`.
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
  is the sum of the arm's, the forearm's and its own, and the move is thought out in that sum (see `TaskmasterModel`).
- What a mob carries that is not a weapon is part of its model too and takes part in its moves: the barrowman's
  barrow hangs from the model's root, his hands are set on its handles whatever else he does.
- A mob that is meant to be feared looks it: the butcher has his own bulk on the withered's bones (parts shown only
  on him), is drawn a tenth bigger and has a box to match; doorways he must pass are three high.
- In this version a loot entry's functions are written under the key `modifier` and a pool's conditions under
  `condition` (see `gen_data.py`); `functions`/`conditions` are silently ignored.
- The floor of a yard must not be dirt or grass: trees and flowers are planted after structures and grow through it.
- A boss's bar is shown to players it fights or who stand within 12 blocks, not to everyone who can see the building.
- Everything a player needs has a source: a recipe, a drop, a chest or the keykeeper. When adding an item, add at least
  one of these in `gen_data.py`, the mob's `dropCustomDeathLoot` or `Shop`.
- Dying costs the souls carried and nothing else: belongings are put back in their slots (`SoulMechanics.onPlayerDrops`).
- Flask gulps and spell casts are kept as item wear whose last point is never spent ("broken" in the item definition
  means empty).
- Before adding content, look at the previews; after adding, check `server.log` and the screenshots.

## The map

"Вышний Острог" (the High Stockade) is an adventure map for the mod, one world that grows place by place. It is the
mod's own country, in the spirit of the genre and a copy of nothing in it. The land is a wooded plateau with a gorge cut
into it (`tools/gen_world.py`); the country outside the plan is the flat forest the world itself is made of.

- The Dead Borough (`tools/gen_village.py`) in the south of the gorge: a walled town of small houses under a night that
  never ends, where the game begins. It is drawn on a grid of tiles of three blocks: walls, streets and the places built
  by hand are named in the file, every other tile is a lot and the lots are cut into houses.
  Yard of the first hearth (keykeeper, anvil, the starter chest; no enemy sees it) -> Low Street -> the lanes of the
  borough, west or east about (the high street is shut by a gate that opens from the town's side) -> market -> the
  Shambles (the street is barricaded: through the butchers' shop or round by the back alley) -> Church Lane (the
  sexton's lodge: a hearth) -> the churchyard (the Gravedigger; a seal on the gate to the road behind him).
- The road: from the churchyard's north gate under a spur of the plateau by a tunnel with a bend in it. In the bend,
  where neither mouth is seen, lie the thresholds that turn the clock: evening towards the fortress, night towards the
  town. Then along the gorge and up a causeway to the fortress's bridge.
- The High Stockade (`tools/gen_map.py`, in its own coordinates; `gen_world.py` sets it down on the floor of the gorge):
  a lower fortress and an upper city on two crags, at an hour of evening that never ends. South gate (hearth, anvil) ->
  ward, cells, walls -> bridge tower (its ground door is opened from inside) -> great bridge -> gate hall (the Overseer;
  a seal behind him) -> square (hearth, anvil) -> cloister and chapter room / bell tower -> covered walk -> the
  cathedral's east gallery -> nave (postern and west door open shortcuts; sacristy hearth) -> crypt under the nave
  (optional, comes out in the alley behind the chapter room) -> choir (the Chaplain) -> treasury and terrace.
  Whoever falls from its walls lands on the floor of the gorge and walks back round to the causeway.

- The White Pit (`tools/gen_quarry.py`), under a hill of pale stone west of the town, in the town's night: the quarry
  the Stockade was built from. It is entered by the Quarrymen's Gate at the west end of Low Street, which is locked
  (`locked_gate`); its key lies in the Stockade's treasury, behind the Chaplain - a place can be reached from the hub
  and still come after another. Gate -> a lane cut down through the plateau -> the open pit -> the unfinished head of
  the Founder in the pit's west face: the way in is its mouth -> the Maw (hearth, anvil) -> the main drift -> the
  pillar field -> a stair up to the Carvers' Shop (statues, four of them the Unhewn) -> the upper gallery (east: the
  room behind the eyes; west: the gallery of the Wheel Hall) -> the cage shaft down (its door at the main drift opens
  from inside: the short way from the hearth) -> the Wheel Hall (the Taskmaster; a seal behind him) -> the sump
  (hearth) -> the lower workings (crawlers: creep past them along the far wall) -> the Breach (the Stone-Eater) ->
  the crack. Three levels: `TOP` eight over the valley's ground, `MID` six under it, `LOW` thirteen under it.
  Left for later: a barred drift north out of the lower workings, the fissure behind the Breach.

`gen_world.py` writes `map/high_stockade.plan.txt` (the same format as the buildings; about 2.2 million blocks),
`map/views.txt` and `map/light.txt`. The plan's origin is the place where the newcomer wakes; the game puts it at
0, -46, 0 and turns the plan a quarter, so the plan's north is the world's west and the way through the map leads into the
sunset. The dead of the town are of tier 0, of the lower fortress tier 1, of the upper city tier 2 (`World.tiers`);
the quarry's are of tier 0 and strong by birth. The valley's ground is the plan's layer 16 (`GY`): the flat world has
sixteen blocks of ground under its grass, and the quarry uses thirteen.

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
  forest outside the plan grew up under snow, which the plan's own forest did not have. The ground is at y = -47.
- With natural regeneration switched off (as the map does) hunger is held full. The game's own mobs do not spawn
  (`SPAWN_MOBS`): the night would fill with them.
- A block with a direction of its own must turn with the plan (`rotate`): the fog gate did not, and stood edge-on in
  every doorway of the map. What is built late (corbels, lamps) can land in a stair: the check counts headroom.
- A lord's death lifts every seal in a box of forty blocks either way round his mark (`FogGateBlock.lift`): two lords
  of one place must stand further apart than that, or the first opens the way past the second.
- Under the ground the plan holds only what differs from the world's stone: the hollows, and a skin of rock round them
  so that the check sees walls (`Quarry.skin`). Above the ground everything is in the plan; a place cut open from
  above gives its columns to `open_sky`, or firs are planted in the air over it.
- Painted pots swallow what is put into them; a player who may not build cannot use them (`SoulMechanics.onUsePot`).
- Time does not move; the hours (`PlaceNameBlock.DUSK`, `NIGHT`) were chosen by looking at pictures. `time set` is
  for the whole world: with several players in different places the last threshold crossed wins.

Balance (see `tools/balance.py`): one clearing of the town brings a player to about level 10-12 before the Gravedigger;
the lower fortress to about 18 before the Overseer; the upper city to about 29 before the Chaplain; the quarry to
about 35 before the Taskmaster and 38 before the Stone-Eater. The fortress was
first tuned for a newcomer; its tiers make up for the levels a player now brings to it. When mobs, weapons, tiers or
the map change, run it again.
