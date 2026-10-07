# The Mire

Part of the map "Вышний Острог". What is common to the whole map is in `docs/map.md`.

## The place

- The Mire (`tools/gen_swamp.py`), east of the town, at an hour of its own: a green noon that does not pass. It is entered by the Mire Gate at the east end of Low Street (`locked_gate[lock=mire]`); its key lies in
  the crack behind the Stone-Eater. Gate tower (hearth; the thresholds that turn the clock lie in its bend) -> the
  Drowned End: the town's own houses sunk to their eaves, willows grown through some, a walk of planks along them ->
  the ferryman's hut (hearth, anvil) -> the log roads over the open marsh, north to the Weir and south to a door
  in the ridge that opens from the other side -> the Weir (the Wallower: a floor under the shallows; a seal behind
  him) -> the mill (hearth, anvil) -> the withy wood -> the island (the Weeping Mother) -> the grove behind her.
  Water to the knee slows and stops the roll, deep water is swum: the roads are what a player keeps to, and what
  lives in the water waits beside them. Left for later: a barred way out of the grove.

## Its dead and its lords

- The Mire's own: `BonePikeEntity` keeps to water (its goal swims it straight at its prey only while there is water
  ahead, it leaps at whoever stands on the boards, and one that lands on them flounders and is hurt half as much
  again); `LankyEntity` wades and reaches; `WispEntity` flies a ring round its prey and throws slow fire
  (`called()` makes one that burns out by itself and leaves nothing: the Mother's); `WallowerEntity` (it dives:
  the model's root part goes down through the floor, and nothing hurts it there; `MudGlobEntity` is what it spits
  and `block/SludgeBlock` what that leaves) and `WeepingMotherEntity` (rooted: she does not walk, turn fast, or get
  pushed, and keeps her own list of roots coming up) are the lords. The drowned are a kind of the withered that
  walks the bottom. A mob that lives in water has no `FloatGoal`, is not `isAffectedByFluids` and has the path
  malus of water at 0: it then walks the bottom at its own pace. These share `client/QuarryRenderer` too.
- `entity/RootEntity` (+ `client/RootModel`, `RootRenderer`, `tools/gen_root.py`) - a root of the Weeping Mother: a thing
  in the world, not a puff of particles. It is set on the ground under whoever she is after (`RootEntity.ground` looks
  down for a floor: nothing comes up in the air or over nothing), shows its tips through broken earth for the length of
  the warning, comes up, strikes once whoever is in its ring and no higher than it reaches, and sinks. She sends them one
  after another (`ROOTS_EVERY`) and whoever one has thrown is let alone by the next for `ROOT_GRACE` ticks.
