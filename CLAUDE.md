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
  `DUSHI_CLIENTTEST=1`; `client/ClientSelfTest.java` makes a flat world, lines up mobs, blocks and items, takes screenshots
  and quits. This is how the look of the mod is checked. It adds about five minutes to the run. The client renders in
  software there, a few frames a second, so a picture of a move is taken with the move frozen at a chosen tick
  (`attack(type, id, tick)` in the script) - never by timing. The script also presses the real roll key, dies, respawns
  and walks into the stain, and prints what it measured as `DUSHI CLIENTTEST ...` lines in `client.log`.

The server test ends with `WorldTest.java`: gear numbers, recipes and chest loot as loaded by the game, both buildings set
down from their plans and counted, the marks raising, losing and raising their mobs, and both buildings looked up in the
world the server generated. The client test makes an ordinary world, works on a stone stage at y = 180, and at the end
flies to the nearest outpost and stockade and takes pictures from fixed places of their plans (`view(...)`).

`git fetch origin ci-out` and read the files; the commit message of `ci-out` names the commit of `main` it was built from.

Other switches: `.ci/dump_api` makes the workflow write `javap` signatures of the whole game to `ci-out/api/` (useful when
moving to a new Minecraft version: names change a lot between versions); `.ci/ref.txt` lists class files to copy out.

## Layout

- `src/main/java/com/keykeeper/dushi/` - `Dushi` (all registries), `PlayerSoul` (souls, stats, stamina of a player; a data
  attachment synced to the player's own client), `SoulMechanics` (the rules: souls for kills, the stain on death, stamina,
  rolling through blows, backstabs, guard break), `Net` (messages), `Spell`, `Shop`.
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
  - `check_assets.py` - checks that everything registered has its files and names.

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
