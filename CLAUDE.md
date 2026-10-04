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

The map (see "The map" below) is built by the same workflow when `.ci/map` exists: a dedicated server is started in an
empty world with `DUSHI_BUILDMAP=map/high_stockade.plan.txt`, `MapBuilder.java` sets the plan down, works the light out
again, sets the rules of the world, saves and stops; the world folder is zipped. `ci-out` then also has:

- `map.log` - `DUSHI MAP ...` lines: the size of the plan, what stands in the world (a count of block entities), and the
  light read out at the places listed in `map/light.txt`;
- `High_Stockade.zip` - the map as players get it (unpack into `saves`); the workflow gives the world its shown name
  (`tools/name_world.py`), `map/icon.png` and `map/readme.txt`;
- `mapclient.log` and `shots/m*.png` - only if `.ci/mapshots` exists: the client opens the saved world the way a player
  would (`client/MapSelfTest.java`, `DUSHI_MAPTEST=map/views.txt`), says how a newcomer starts (`DUSHI MAPTEST ...`),
  flies to every view of `map/views.txt` and takes a picture, then throws every lever and kills both bosses and says
  what the doors and the seals did.

`.ci/skip_server` skips the server self-test (to save minutes while only the map is worked on); delete it and restore
`.ci/client` before finishing, so that everything is checked once together.

Other switches: `.ci/dump_api` makes the workflow write `javap` signatures of the whole game to `ci-out/api/` (useful when
moving to a new Minecraft version: names change a lot between versions); `.ci/ref.txt` lists class files to copy out.

## Layout

- `src/main/java/com/keykeeper/dushi/` - `Dushi` (all registries), `PlayerSoul` (souls, stats, stamina of a player; a data
  attachment synced to the player's own client), `SoulMechanics` (the rules: souls for kills, the stain on death, stamina,
  rolling through blows, backstabs, guard break), `Net` (messages), `Spell`, `Shop`.
- `Forge` (the recipes of the old anvil: parts and souls), `block/ForgeBlock`, `client/ForgeScreen` (a plain list).
- `block/FogGateBlock` - fog before a boss; `sealed=true` is the amber seal that lets nobody through until the boss of
  that room dies (`lift` clears the nearest fog and every seal within reach of the boss's mark).
- `block/PlaceNameBlock` - a threshold nobody sees or feels: a player who steps through it is shown the name of the
  place (`place.vsratie_dushi.<id>` in the texts; the list of places is the enum in the block). No item: a map's builder
  sets it; with a mob mark in hand it can be seen and removed in creative.
- `MapBuilder` (build only) and `client/MapSelfTest` (build only) - see "The map".
- `Gear` - what the rules know about any piece of gear, the game's own included: stamina cost, scaling and backstab of
  a weapon (by item tag for vanilla ones), stability of a shield, weight of everything worn or held, and the burden that
  follows from the weight (it decides the roll). `ArmorSet` - the mod's four suits and the gift of each.
- `block/MobMarkBlock` + `MobMarkBlockEntity` - a ring of ash where a mob of a building rises: when a player comes near,
  and again after any rest at a hearth or death within 96 blocks. A slain boss stays slain; a living one goes back and heals.
- `world/` - `Plan` reads a building from `data/vsratie_dushi/plan/NAME.txt`, `PlanStructure` picks a level, dry place
  and one of four turns, `PlanPiece` sets the blocks. The worldgen JSON only names the plan.
- `entity/` - `SoulsMob` is the base of every enemy: an attack is a "move" with a wind-up, an impact tick and a recovery,
  known on both sides so the model can show it. `SoulsCombatGoal` walks the mob to its target and lets it choose moves.
- `client/` - models, renderers, `SoulsHud`, `HearthScreen`, `ShopScreen`, the roll key.
- `tools/` - everything generated is generated from here; run `sh tools/build_assets.sh [preview dir]` from the repo root:
  - `modelkit.py` - entity models described once in Python: texture layout, the mesh part of the Java model class (written
    between the `// <generated mesh>` markers) and a software preview render;
  - `gen_withered.py`, `gen_knight.py`, `gen_keykeeper.py`, `gen_overseer.py`, `gen_chaplain.py` - the mobs;
  - `gen_items.py` (icons), `gen_shields.py` and `gen_blocks.py` (box models), `gen_sounds.py`, `gen_lang.py` (all texts);
  - `gen_armor.py` - worn textures (64x32, the game's armour layout), icons and equipment definitions of the suits;
  - `gen_structures.py` - the buildings, block by block, with top-down and isometric previews; bars, fences and walls
    are written already joined to their neighbours, because nothing joins them during world generation;
  - `gen_data.py` - recipes, chest loot, tags, structure and structure-set JSON;
  - `check_assets.py` - checks that everything registered has its files and names;
  - `gen_map.py` - the map (not run by `build_assets.sh`: `PYTHONPATH=tools python3 tools/gen_map.py [preview dir]`);
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
  raised sideways (zRot), not forwards (see the knight's lunge).
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

"Вышний Острог" (the High Stockade) is an adventure map for the mod: a lower fortress and an upper city on two rocks above
the clouds, at an hour of evening that never ends. It is the mod's own place, in the spirit of the great sunlit cities of
the genre and a copy of none of them. `tools/gen_map.py` builds it block by block into `map/high_stockade.plan.txt`
(the same format as the buildings; about 650 thousand blocks) and writes `map/views.txt` and `map/light.txt`; the game
turns the plan a quarter, so the plan's north is the world's west and the way through the map leads into the sunset.

The way through: arrival bridge -> south gate (hearth, keykeeper, anvil) -> ward, cells, walls -> bridge tower (its
ground door is opened from inside) -> great bridge -> gate hall (the Overseer; a seal behind him) -> square (hearth,
anvil) -> cloister and chapter room / bell tower -> covered walk -> the cathedral's east gallery -> nave (postern and
west door open shortcuts; sacristy hearth) -> crypt under the nave (optional, comes out in the alley behind the chapter
room) -> choir (the Chaplain) -> treasury and terrace.

The names of the places are thresholds across the ways in (`thresholds` in `gen_map.py`). Views whose name begins
with `as_player_` are walked to by the test as the newcomer is, before the camera is set free.

`gen_map.py` ends with a check of the map as a place to play, and every change to the map must leave it clean:

- it walks the map the way a player can (steps, jumps, drops, levers, seals lifted boss by boss) and reports what is
  reached at each stage, chests and mobs nobody can reach, places that are traps (ledges whose only way out is the fall
  into the void are listed, not counted as traps), and that the cathedral cannot be entered except by the covered walk;
- it works out the light like the game does (its numbers matched the game's at every probe) and reports walkable
  places that are too dark; halls that windows would fill get the game's unseen `minecraft:light` blocks (`ambient`);
- previews: `map_iso.png`, and one top-down picture per storey with a dot on every reachable place (red where dark).

Things learnt the hard way:

- In a world whose game mode is adventure the game drops a newcomer through the floor of the spawn if the place is not
  loaded yet (they fell into the void). `SoulMechanics.settle` puts them back on it at login and respawn.
- Blocks set one by one into an empty world leave the light wrong (whole storeys black, or everything fully lit).
  `MapBuilder` therefore relights every chunk after building (`initializeLight`, then `lightChunk`).
- Rock is put only under what stands on the ground (`bedrock`): corbels and other overhangs must not grow rock.
- `chiseled_sandstone` has a creeper's face on it; the city's stone is smooth and cut sandstone.
- A fall of more than 24 blocks over an empty column kills at once (`SoulMechanics.ground`), so the edge of the map
  needs no walls; with natural regeneration switched off (as the map does) hunger is held full.
- A block with a direction of its own must turn with the plan (`rotate`): the fog gate did not, and stood edge-on in
  every doorway of the map. What is built late (corbels, lamps) can land in a stair: the check counts headroom.
- Painted pots swallow what is put into them; a player who may not build cannot use them (`SoulMechanics.onUsePot`).
- The hour is set once (`MapBuilder.SUNSET`) and time does not move; the hour was chosen by looking at pictures.

Balance (see `tools/balance.py`): one clearing of the lower fortress brings a player to about level 10-12 before the
Overseer; one clearing of everything to about level 24 before the Chaplain, who is tuned for 26-28 - one more round of
the cathedral and the crypt from the sacristy's hearth. When mobs, weapons or the map change, run it again.
