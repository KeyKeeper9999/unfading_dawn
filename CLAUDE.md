# The Unfading Dawn (unfading_dawn)

A souls-like inside Minecraft: NeoForge mod for Minecraft 26.3. The mod is shown as "The Unfading Dawn" (in Russian «Немеркнущий
рассвет») and its id is `unfading_dawn`: namespace of every block, item and text, the folders of assets and data, the jar.
Its first, working name survives only where the game never shows it: the repository (`vsratie_dushi1`) and, in half, the
Java package `com.keykeeper.dushi`, the class `Dushi` and the `DUSHI_*` switches of the tests. Never bring it back into
anything a player can see. `tools/rename_mod.py OLD NEW` is how the id was changed (text and paths of every tracked file).
The owner writes in Russian; texts exist in Russian and English.

The mod is a souls-like "in the spirit of", with its own world, names and designs: it does not copy the characters, bosses,
items or names of any existing game, and every sound is synthesised from scratch. Keep it that way when adding content.

The story the map tells, the traders and items agreed for the next round, and the places thought of after that are in
`docs/story.md` (in Russian). Read it before adding content or writing any text of the game, and keep it up to date.

## Where the rest is

This file holds what is common to the whole mod. Read the others only for the work in hand, and write new knowledge
into the file of its place or topic - here only what every chat needs:

- `docs/places/borough.md`, `stockade.md`, `quarry.md`, `mire.md`, `firwood.md`, `wharf.md`, `stubble.md` - a place: its way through,
  its levels, what is left for later, and its own dead and lords (their classes and what is particular about each);
- `docs/map.md` - how the map is generated, set down and checked, and the things learnt the hard way about it;
- `docs/animation.md` - poses, held things, moves: the rules learnt;
- `docs/balance.md` - souls, levels, what each place pays;
- `docs/story.md` - the story, what is built, what is agreed and what comes next (in Russian).

A new place gets a new file in `docs/places/`.

## Building and checking

Nothing can be built on a machine without access to the NeoForge and Mojang servers, so the build runs in GitHub Actions
(`.github/workflows/build.yml`) on every push to `main`. A push cancels the run before it, so changes are gathered and pushed together, and nothing is
pushed while a run is awaited. The workflow publishes its results to the `ci-out` branch:

- `summary.txt` - the run in a page (`tools/ci_summary.py`): `RESULT: OK` or `RESULT: LOOK` with what to look at; whether
  each test reached its end, mobs spawned, drills, missing resources, errors of the game, what the map's test did, and
  what changed against the run before (`counts.json`). Read this first and open a log only where it points;
- `sheets/*.jpg` and `sheets/index.txt` - the pictures nine to a sheet, by group, each under its name
  (`tools/ci_sheets.py`: `mobs_<place>`, `gear_held`, `items_player`, `buildings`, `map_<place>`, `map_lords`,
  `map_screens`). Look at the sheets of what was changed, all of them after a change that goes through everything;

- `build.log` - compiler output;
- `unfading_dawn.jar` - the mod (the jar never has a version in its name, so it can simply be overwritten);
- `server.log` - a dedicated server started with `DUSHI_SELFTEST=1`: `SelfTest.java` spawns every mob, makes them fight iron
  golems, drills every move of every mob, drives a stand-in player through souls, levelling, the flask, spells, the hearth,
  the shop and death, prints `DUSHI SELFTEST ...` lines and stops the server;
- pictures and `client.log` - only if the file `.ci/client` exists: the real client is started under xvfb with
  `DUSHI_CLIENTTEST=1`; `client/ClientSelfTest.java` makes a world, lines up mobs, blocks and items, takes screenshots
  and quits. This is how the look of the mod is checked. It adds about five minutes to the run. The client renders in
  software there, a few frames a second, so a picture of a move is taken with the move frozen at a chosen tick
  (`attack(type, id, tick)` in the script) - never by timing. The script also presses the real roll key, dies, respawns
  and walks into the stain, and prints what it measured as `DUSHI CLIENTTEST ...` lines in `client.log`.

The server test ends with `WorldTest.java`: gear numbers, recipes and chest loot as loaded by the game, both buildings set
down from their plans and counted, the marks raising, losing and raising their mobs, and both buildings looked up in the
world the server generated. The client test makes an ordinary world, works on a stone stage at y = 180, and at the end
flies to the nearest outpost and stockade and takes pictures from fixed places of their plans (`view(...)`).

The pictures in full are in the branch `ci-shots` (`shots/NAME.png`), which is large: do not fetch it, take one picture:
`gh api -H "Accept: application/vnd.github.raw" "repos/KeyKeeper9999/vsratie_dushi1/contents/shots/NAME.png?ref=ci-shots" > NAME.png`.

`git fetch origin +ci-out:refs/remotes/origin/ci-out` and read the files (the branch is written anew every time: without
the `+` a fetch into an existing ref is refused and yesterday's files are read); the commit message of `ci-out` names the
commit of `main` it was built from. A whole run - build, server, client, map, the map's pictures - takes about forty minutes.
If `client.log` stops after the resources are loaded and there are no pictures, look in it for `Failed to create model`:
a model class that asks for a part its mesh does not have (see "mirror" below) stops the client at its loading screen,
and would stop a player's too.

The map (see "The map" below) is built by the same workflow when `.ci/map` exists: a dedicated server is started in a
flat world of forest (the `generator-settings` in the workflow: its top layer is at y = -47) with
`DUSHI_BUILDMAP=map/high_stockade.plan.txt`; `MapBuilder.java` fells the forest where the map goes, sets the plan down,
works the light out again, sets the rules of the world, saves and stops; the world folder is zipped. `ci-out` then also has:

- `map.log` - `DUSHI MAP ...` lines: the size of the plan, what stands in the world (a count of block entities), and the
  light read out at the places listed in `map/light.txt`;
- `High_Stockade.zip` - the map as players get it (unpack into `saves`); the workflow gives the world its shown name
  (`tools/name_world.py`), `map/icon.png` and `map/readme.txt`;
- `mapclient.log` and the pictures `m*` - only if `.ci/mapshots` exists: the client opens the saved world the way a player
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
  place (`place.unfading_dawn.<id>` in the texts; the list of places is the enum in the block). Every place has its
  hour, and in a world whose time stands still the threshold sets the clock (`SoulMechanics.hour`); a name is shown
  once, when the player first comes to the place (`PlayerSoul.placesSeen`, by id, saved with the player); `to_night`,
  `to_dusk` and `to_murk` have no name and only do that. No item: a map's builder sets it; with a mob mark in hand it can be seen
  and removed in creative. A hearth remembers the hour it was rested at, for those who come back to it by dying.
- `block/HearthBlock` - rest, level up, and travel: a player can go from the hearth they sit by to any other they have
  rested at (`HearthBlock.travel`, `client/TravelScreen`; the list is `PlayerSoul.rests`, saved with the player, and is
  sent to the client when the hearth's screen opens). A map's hearth has a name, `site=` in its block state
  (`HearthBlock.Site`, text `hearth.unfading_dawn.<id>`): a new hearth of a map needs a new entry there and its text.
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
  follows from the weight (it decides the roll). `ArmorSet` - the mod's nine suits (the last a reaper's harvest clothes) and the gift of each (the prisoner's,
  the guard's, the rusted plate, the chaplain's, the linen of the Unsleeping, a stonecutter's felt and leather, the
  Mire's coat of scales, a seaman's oilskins). A gift is a function of `Gear` asked where the thing happens (`staminaRegen`,
  `blockCost`, `drowse`, `blastShare`, `wading`, `fireShare`, `thrown`: how far a blow throws - asked by `SoulsMob.shove` and by both
  of its hits); a blow that must be known for what it is says so while it lands (`SoulsMob.rockfall`).
- `Charm` + `item/CharmItem` - the charms: twelve small things, one or two to a place, one worn at a time (`PlayerSoul.charm`,
  kept through death). A charm is put on by using it and the one worn before comes back into the hand; it is taken off at
  a hearth (`HearthScreen`) and shown beside the flask (`SoulsHud`). What each gives is asked of the enum where the thing
  happens (`Charm.X.more(player)`, `.less(player)`, `.on(player)`). A new charm is an entry there, an icon, its two texts
  and a source: a chest of its place (`gen_data.py`), a lord's drop, or the keykeeper.
- `Spell` - every spell: what it costs, and in `cast` what it does. What pours from a palm (`FLARE`, `HOT_WAX`) is a jet:
  `jetTouches` asks, a quarter of a pace at a time along it, for the nearest point of a thing's box - not for its middle,
  which for a thing as wide as a wagon is out of reach when its side is in one's face - and `lick` takes its shield down
  and its moment of grace after another blow away, so that the flame lands on what it visibly covers. (The moment of grace is `LivingEntity.damageCooldownTime` in this version; `setInvulnerableTime` is something else and changes nothing about it.) `ROOT_SNARE` is a
  `RootEntity` called by a player (`aimed` picks whom the eyes are on); `MARSH_FIRE` is a wisp's flame that, thrown by a
  player, bends towards what is near its way (`SpellBoltEntity.seek`) and clings. A new spell is an entry, its `cast`,
  a glyph in `gen_items.py`, two texts, a recipe and a place it is found.
- Shields are `item/GuardShieldItem` (`Dushi.shield`): how much of a blow they take, how much of it costs no stamina, and,
  for those that are more than their numbers, a weight of their own and a trait - the carver's buckler does not slow the
  return of stamina while it is up (`Gear.tiresTheArm`), the scale shield weighs half of what its stability would make
  it, a waker's board throws back whoever struck it (`SoulMechanics.onShieldBlock`).
- `block/MobMarkBlock` + `MobMarkBlockEntity` - a ring of ash where a mob of a building rises: when a player comes near,
  and again after any rest at a hearth or death within 96 blocks. A slain boss stays slain; a living one goes back and heals.
  A mark has a `tier` (0-3): the same mob with more health, harder blows and more souls (`SoulsMob.TIER_*`), so that a
  later place can use the dead of an earlier one.
- A player within six blocks of a hearth is not noticed by the dead (`PlayerSoul.atHearth`, `SoulsMob.setTarget`); one
  they are already fighting is not let off by running to it.
- `world/` - `Plan` reads a building from `data/unfading_dawn/plan/NAME.txt`, `PlanStructure` picks a level, dry place
  and one of four turns, `PlanPiece` sets the blocks. The worldgen JSON only names the plan.
- `entity/` - `SoulsMob` is the base of every enemy: an attack is a "move" with a wind-up, an impact tick and a recovery,
  known on both sides so the model can show it. `SoulsCombatGoal` walks the mob to its target and lets it choose moves.
  What moves are made of: `hitArc` (a cone before the mob, or in any direction - a tail - and, if asked, through a
  raised shield, which it knocks down), `hitAround`, `shove`, and `strikeLater` - a blow that lands on a place on the
  ground after a delay, the place marked until then (dust sifting down for a falling rock, chips for a crack running
  along the floor). The dead do not turn on each other: `setTarget` ignores another `SoulsMob`.
- `block/ShallowsBlock` (`shallows`; `sea_shallows` is the same thing in the sea's grey, for the Wharf's sands) - a
  hand's depth of water over a floor, which is a floor: no fluid in it, so nobody is slowed and the roll works. `block/SludgeBlock` (`sludge`) lies some seconds where a clot burst, slows
  whoever is not of the dead (`makeStuckInBlock`: the roll too), and leaves the shallows again if it lay in them.
  A mob mark can stand in water (`waterlogged`).
- `client/` - models, renderers, `SoulsHud`, `HearthScreen`, `ShopScreen`, the roll key.
- `tools/` - everything generated is generated from here; run `sh tools/build_assets.sh [preview dir]` from the repo root:
  - `modelkit.py` - entity models described once in Python: texture layout, the mesh part of the Java model class (written
    between the `// <generated mesh>` markers) and a software preview render;
  - `gen_withered.py` (ten kinds on one model: prisoner, guard, archer, acolyte, the town's townsman, butcher and
    watchman, the quarry's digger, barrowman and sapper), `gen_knight.py`, `gen_keykeeper.py`, `gen_overseer.py`,
    `gen_chaplain.py`, `gen_hound.py`, `gen_gravedigger.py`, `gen_crawler.py`, `gen_unhewn.py`, `gen_taskmaster.py`,
    `gen_stone_eater.py`, `gen_pike.py`, `gen_lanky.py`, `gen_wisp.py`, `gen_wallower.py`, `gen_mother.py`, `gen_root.py`,
    `gen_sect.py` (the wakeful, the waker and the sleeper on one model), `gen_waxen.py`, `gen_moth.py`, `gen_shatun.py`,
    `gen_elder.py`, `gen_sailor.py` (five seamen on one mesh), `gen_crab.py`, `gen_skua.py`, `gen_capsized.py`,
    `gen_fieldhand.py` (five fieldhands on one mesh), `gen_scarecrow.py`, `gen_stook.py` (and the millstone), `gen_sungazer.py` - the mobs
    (the withered are eleven kinds now: the Mire's drowned too). `Model.mirror` gives a mirrored part the parent of its
    source with "right" turned to "left" in the name: a part whose parent is named otherwise (`leg_r1`, `wing_r`) must be
    given its `parent=`, or the left shin hangs from the right leg - which the preview shows only if one looks, and the
    game refuses outright;
  - `gen_items.py` (icons), `gen_shields.py` and `gen_blocks.py` (box models), `gen_sounds.py`, `gen_lang.py` (all texts);
  - `gen_armor.py` - worn textures (64x32, the game's armour layout), icons and equipment definitions of the suits;
  - `gen_structures.py` - the buildings, block by block, with top-down and isometric previews; bars, fences and walls
    are written already joined to their neighbours, because nothing joins them during world generation;
  - `gen_data.py` - recipes, chest loot, tags, structure and structure-set JSON;
  - `check_assets.py` - checks that everything registered has its files and names;
  - `gen_world.py` - the map (not run by `build_assets.sh`: `PYTHONPATH=tools python3 tools/gen_world.py [preview dir]`);
    it puts together the land, `gen_village.py` (the Dead Borough), `gen_map.py` (the High Stockade),
    `gen_quarry.py` (the White Pit), `gen_swamp.py` (the Mire), `gen_skete.py` (the Black Firwood), `gen_shore.py`
    (the Morning Wharf) and `gen_stubble.py` (the Stubble; `preview_stubble.py DIR` builds that place alone, in a second); without a preview directory it takes about two and a half minutes, with one about five;
  - `balance.py` - the sums of the map's progression (`PYTHONPATH=tools python3 tools/balance.py`): it reads
    `map/census.json`, which `gen_world.py` writes (what comes into reach before each lord), so it takes a second -
    run it after `gen_world.py` when the map changed, and by itself when only numbers in the Java changed.

  Never edit generated textures, models, `sounds.json` or the lang files by hand: change the generator and run it.
  Animations (`setupAnim`) are hand-written Java; when a rest pose changes, update the pose in the generator's preview too.

## Conventions

- Item icons are 16x16 in the style of the game's own; 32x32 only for the two-handed weapons. Mobs and blocks are models.
- A weapon's damage, speed and reach are ordinary item attributes; stamina cost, scaling and backstab live in `WeaponItem`.
- A block state property needs two values at least (the game refuses to register a block otherwise): the second
  `Lock` is the gate nothing opens.
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

The places, in the order they are played: the Dead Borough (`gen_village.py`; the hub: the yard of the first hearth) ->
the High Stockade (`gen_map.py`) -> the White Pit (`gen_quarry.py`) -> the Mire (`gen_swamp.py`) -> the Black Firwood
(`gen_skete.py`) -> the Morning Wharf (`gen_shore.py`) -> the Stubble (`gen_stubble.py`). Each is behind a locked gate of the hub whose key lies behind
the second lord of the place before it. Each has a file in `docs/places/`; how the map is built and checked, and what
went wrong before, is in `docs/map.md`; what each place pays is in `docs/balance.md`.
