# The Morning Wharf

Part of the map "Вышний Острог". What is common to the whole map is in `docs/map.md`.

## The place

- The Morning Wharf (`tools/gen_shore.py`), south of the town where the plateau falls into the sea, at an hour of its
  own: the sun just clear of the water (`PlaceNameBlock.SUNRISE`), in a mist that is its biome's
  (`worldgen/biome/shore.json`, which also gives it its surf and its gulls). It is entered by the Sea Gate, the old
  south gate of the yard of the first hearth (`locked_gate[lock=shore]`); its key lies in the candle store behind the
  Elder. Sea Gate -> a passage under the plateau (the clock is turned in its cross leg) -> the port: the town's own
  houses on a grid of their own (`gen_shore.Port` is a `gen_village.Village`), the harbour office (an anvil; the port's hearth `harbour`, «Под обрывом», is under the cliff where the passage comes out, beside Sea Lane, between two lamps: inside the office it was missed), the
  net loft, the boat shed -> the quay -> the long pier -> the koch "Daybreak" at its end (the Helmsman, on her deck:
  fog on the gangway from the pier, a seal on the one over her other side) -> the old mole (a gap to jump, a slump to
  the water) -> the Long Spit: flat sand, wet at its edges, a long hook round the roads where three ships lie at
  anchor that nobody reaches (a hearth under an upturned karbas at its root) -> the boulder strand -> the shoal in its
  ring of stacks (the Capsized; an arch in, a sealed arch out) -> a causeway -> the lighthouse on its rock (the Pilot
  at its foot, a hearth inside, a stair round the wall, the lamp room - kept empty of people for a keeper to come).
  Levels: quay, piers and dry sand are the valley's ground; the sea's top layer of water is one below it; wet sand
  lies in that layer under a film (`sea_shallows`); where the sea is waded there is sand a layer lower, where it is
  deep, two. Left for later: nothing barred yet (a way into the rock under the lighthouse is thought of).

## Its dead and its lords

- The Wharf's own (`tools/gen_sailor.py`: the dockhand, the harpooner, the oarsman, the Pilot and the Helmsman on one
  mesh, drawn at three sizes; `gen_crab.py`, `gen_skua.py`, `gen_capsized.py`; all on `client/QuarryRenderer`):
  `ShoremanEntity` is what the seamen have in common. `DockhandEntity` hooks and hauls in (`struck` is the hauling);
  `HarpoonerEntity` throws from afar (`HarpoonEntity`), stabs up close and hops back if there is ground behind him;
  `OarsmanEntity` sweeps an oar level (the arm down, the trunk turning: `SailorModel.sweep`). `PilotEntity` is not a
  lord but is met like one, at the lighthouse's foot. `SandCrabEntity` lies in the sand (phase 1) until somebody is
  near, and goes round its prey sideways; `called()` is one a lord raps up, which leaves nothing and goes back down.
  `SkuaEntity` sits, is startled, rings its prey and stoops along a line fixed when it starts to fall. The lords:
  `HelmsmanEntity` (the anchor is a thing thrown, `AnchorEntity`, aimed by flight time, not by `shoot`; while he hauls
  it back, and giddy after his whirl, he is `open()` and his hide lets twice as much through) and `CapsizedEntity` (a
  boat on the sand until woken; `housed()` - lying, or sat down under her - is when nothing but planks is struck).
- `SoulMechanics.undertow` - the sea of the Wharf is waded, not swum: a player in water two blocks deep or more, in the
  biome `unfading_dawn:shore`, is warned, held down and taken (a sixth of their health every half second after a moment's
  grace); the stain is left where they last stood. `waterDepth` counts the column. The map's check knows the same cells
  (`Map.undertow`, filled by `gen_shore.py`), so nothing is "reached" through deep water.
- The sea is heard as single waves and gulls (`ambient.shore.sea`, an addition of the biome about once in twenty
  seconds, waves three times as often as gulls: `gen_sounds.WEIGHTS`), not as a loop: a loop went on under the game's
  music, and was still heard after one had gone back up the passage.
- A gull lets whoever it hunts go, and flies back to its mark, when they are within `SkuaEntity.LORD_GROUND` of a
  living lord: it does not follow anybody onto the Daybreak or into the shoal.
- The Helmsman's cast anchor is drawn as the anchor itself (`AnchorRenderer`, `tools/gen_anchor.py`: the anchor of his
  hand, its crown leading the way it flies, turning slowly over its shank).
