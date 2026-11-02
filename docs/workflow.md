# How the work is done (the owner's standing rules)

These were kept in the claude.ai Project's instructions; they live here so that a chat outside the Project works the same way.
The owner is Key (GitHub KeyKeeper9999). Answer him in Russian, short and to the point. Commit messages: one English phrase
saying what changed in the game.

## Limits (the owner is very sensitive to them)

Limits go on what is read and written, not on time spent waiting. One long chat costs several times what a few short ones do.

- A new place is made in 2-3 chats: (1) its dead and lords, (2) its map, (3) its items, balance and the check of everything.
  At the end of each stage: a commit and a line in `docs/story.md` about what is ready and what is left. At the end of a
  stage, tell the owner to start a new chat; do not begin the next stage yourself.
- Do not read big files whole: search, then read the piece. Do not read or redo what is not changing. Find a pattern for new
  content in an existing thing of the same kind (mob, item, place), not by surveying the project.
- Repetitive actions: write a script in `tools/` and run it, not the same edit file after file.
- After a sweeping edit check the end result where it could matter, do not re-read the project.
- A place's map is looked at with that place's own preview script; the full `gen_world.py` runs when the place is built, not after every edit.
- Do not retell the steps or ask what can be decided alone. If you see another way to save limits, say so at the start of the chat.

## Runs and waiting

- Nothing builds locally. The build is GitHub Actions on every push to `main`, written to `ci-out` (see CLAUDE.md). The repository must
  stay public (free minutes; the private limit of 2000 a month has run out once).
- Do not wait for a run in a command: schedule a check in the chat (a delayed message to yourself, ~10 minutes for a one-place run,
  ~40 for a full one) and stay quiet. While a run goes, do not push to `main`: a push cancels it.
- Gather edits and push once; small things (texts, numbers, icons) in a batch. One run checks everything gathered; no run for a single mob.
- Photograph only what is worked on: `.ci/client` and `.ci/mapshots` (empty = everything). A one-place run is 10-15 minutes. One full
  run (both empty, `.ci/map` present, no `.ci/skip_server`) at the end of a stage; then put the next place back into both files.
- Read `ci-out/summary.txt` and `times.log`; open full logs only where the summary points. The workflow says "success" even when a step
  failed: trust the summary and logs.
- Check the game's names against signatures (`.ci/dump_api`), not memory, before pushing.

## Pictures

- The look is checked by eye and redone if bad. Open the sheets by group (a place's mobs, items, a place's views), only of what
  changed; after sweeping changes, all of them. A single frame in full size only where a sheet shows trouble or a small detail matters.
- What was not opened goes to "not checked by eye" in the report.
- Lesson: a map test that plays an ending empties the world for everything after it (see `docs/map.md`).

## Checks before the end

- After map changes `gen_world.py` must pass without `PROBLEM` lines; after changes of mobs, weapons or the map, rerun `balance.py`.
- Before ending work on a place: one full run, then the next place back into `.ci/client` and `.ci/mapshots`.

## Making the mod right

- Own world, "in the spirit of", no copies of characters, bosses, items, names or levels of existing games; every sound synthesised.
- Generated files (textures, models, `sounds.json`, lang, the map's plan) are never edited by hand: change the generator. Animations are hand-written.
- A place has its own hour and look, its own dead (unique creatures welcome), two lords, hearths, and the key to the next place behind the second lord.
- A mob has a spawn egg, sounds, subtitles, a name in Russian and English. A lord has a bar, phases, several moves with a readable wind-up.
- Every item has a source (recipe, drop, chest, trader). Items have no durability. Death costs only souls. The jar has no version in its name; no libraries but NeoForge.
- NPCs, traders and full story texts wait until the map is mostly built: a new place gets only the names of places, mobs, items and gate inscriptions; the story accumulates in `docs/story.md`.
- New content goes into every place at once: registries, renderers, generators, `check_assets.py`, tests, `balance.py`. Search for mentions of an existing thing of the same kind so nothing is forgotten.

## Handing work over

- Send `unfading_dawn.jar` and `High_Stockade.zip` from `ci-out` (names as there) and the sheets, as files.
- Report: what was done, what was checked and with what, what was not checked. The author cannot be played for: the feel of combat,
  sound by ear and balance in practice are always "not checked".
- The author likes options and details thought out for him. Big forks (a new place, a new mechanic): propose a list first.
- Where to go next is in `docs/story.md` (now: stage 2 - NPCs and the Bellman), not here.
