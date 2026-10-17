# The Dead Borough and the road

Part of the map "Вышний Острог". What is common to the whole map is in `docs/map.md`.

## The place

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

## The clock tower (2026-10-18)

In the middle of the market stands nothing but a free square, thirteen blocks wide (`gen_village.py`: `market_centre`);
when the clock under the Sky Beneath runs and a player comes within 90 blocks, the tower of `plan/clock_tower.txt`
(`tools/gen_tower.py`: plinth, shaft nine blocks square with a stair of 36 steps round its inside wall, a copper pendulum,
four dials, a belfry with the great bell, its rope and the Founder's seat, a roof of dark tiles) is set down layer by layer
by `Clock.java` with a long rumble and a shaking screen (`Net.Quake`). The two ends of the day are at the top, see
`docs/story.md`. Where the tower and the figure over the sea stand is in `data/unfading_dawn/clock_sites.txt`, which
`gen_world.py` writes. The map's test plays it (`clock_*` views of `map/views.txt`, `.ci/mapshots` word `clock`).

