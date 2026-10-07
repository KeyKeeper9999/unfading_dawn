# The White Pit

Part of the map "Вышний Острог". What is common to the whole map is in `docs/map.md`.

## The place

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

## Its dead and its lords

- The quarry's own: `BlindCrawlerEntity` hears instead of seeing (`hears`: a walker from 13 blocks, somebody sneaking
  only on its own block and the eight round it) and calls the others - so a way that crawlers keep is at least five
  wide between whatever stands in it, or there is no creeping past them; `UnhewnEntity` is a statue (phase 1,
  nothing of it moves) until somebody comes within 4.5 blocks or strikes it; `TaskmasterEntity` and `StoneEaterEntity` are the lords (the second digs itself
  under the floor: its model's root part `all` goes down, nothing reaches it there); `PowderPotEntity` (its `blast`
  hurts the dead too) and `WedgeEntity` are what is thrown. These four mobs share `client/QuarryRenderer`.
