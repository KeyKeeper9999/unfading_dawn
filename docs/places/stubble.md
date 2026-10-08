# The Stubble

Part of the map "Вышний Острог". What is common to the whole map is in `docs/map.md`.

## The place

- The Stubble (`tools/gen_stubble.py`), three hollows of the plateau west of the town, at an hour of its own: the
  afternoon of a harvest day, the sun high and white (`PlaceNameBlock.AFTERNOON`, 8000). Its haze, its sky, its
  grasshoppers and the colour of its corn are its biome's (`worldgen/biome/stubble.json`): the corn, standing and cut,
  is the game's own grass (`tall_grass`, `short_dry_grass`, turf) in the biome's `grass_color`. The sky is bare and blue
  (`cloud_fog_end_distance` puts the clouds out of sight) and the light of it straw-coloured (`sky_light_color`). The plateau within ten
  blocks of the place is left without firs (`Stubble.near`, asked by `gen_world.woods`) and has the same biome.
  `tools/preview_stubble.py DIR` builds the place alone, in a second, for looking at.
- It is entered by the Reapers' Gate, a grate in the west cliff of the yard between the road tunnel and the town's north
  gate, across from the Wood Wicket (`locked_gate[lock=stubble]`); its key hangs in the lamp room of the lighthouse
  (the chest `lighthouse_lamp`). Gate -> a passage under the plateau (the clock is turned in its cross leg) -> the balk
  (hearth `balk`, an anvil) -> the fields: a cart road west, cut land with rows of stooks first, then standing corn
  (two cut patches off the road: a chest, a shard) -> a way through the rock (fog) -> the Watched Field, a round cut
  field (the Scorched Warden on a cross built of blocks two cells behind his mark; five lean-tos of straw: under their
  roofs the sun does not strike) -> a way through the rock (sealed) -> the threshing yard (hearth `kiln` in the drying
  barn; the threshing floor; two barns, a chest in one) -> the Great Mill, its back to the rock (fog in its west door;
  the Hollow Miller on the grinding floor; its east door sealed) -> the granary in the rock behind it (hearth
  `granary`, a chest, and a grate nothing opens: `Stubble.barred`).
- North of the mill the corn still stands, and a door in the rock (`gen_stubble.DOOR`, a lever on the yard's side)
  is the short way from the threshing yard back to the fields.
- Everything is on the valley's level. The two lords' marks are more than forty blocks apart either way, so that the
  Warden's death does not lift the Miller's seal.

## Its dead and its lords

- The fieldhands (`tools/gen_fieldhand.py`: the reaper, the mower, the sackman, the scorched and the Hollow Miller on
  one mesh, drawn at three sizes; on `client/QuarryRenderer`): `FieldhandEntity` is what they have in common.
  `ReaperEntity` cuts once or twice and springs from a few paces; a mark set with `hidden=True` leaves the corn
  standing round him. `MowerEntity` takes a swathe and, more often than not, the same swathe back (`BACK` follows
  `SWEEP` with no pause). `SackmanEntity` throws a `FlourSackEntity` from afar and swings one near.
  `ScorchedEntity` burns whoever he takes hold of and whoever strikes him from within arm's length, flares when
  crowded, and goes up a moment after he is killed (`tickDeath`, `PYRE`).
- Flour: a sack that bursts leaves a `FlourCloudEntity` - a thing with no shape of its own, all particles - which blinds
  whoever stands in it and goes up, after a fuse of fourteen ticks, at the first fire that touches it
  (`FlourCloudEntity.kindle`, called by everything that burns: the spells of fire, a tar pot, the scorched, the
  sungazer's patch, the Warden, the Miller's lantern; and by anybody alight walking into it). One cloud sets off the
  next. A lord is not hurt by it. The sack is also an item a player throws (`item/FlourSackItem`).
- `ScarecrowEntity` (`tools/gen_scarecrow.py`, with the Warden on the same mesh): on its stake (phase 1) until
  somebody within seven blocks is not looking at it - three in five of them (`restless`, chosen when it is raised);
  the others get down only if struck. Down, it stands still while its prey looks at it (`held`), unless it was struck
  a moment ago (`HEEDLESS`). Its arms do not bend: its blows are turns of the whole of it.
- `StookEntity` (`tools/gen_stook.py`): stands as a stook until somebody is near; falls on its prey and lies a moment
  (struck harder then); shakes out chaff that blinds. Fire does three times its hurt, and alight it runs.
- `SungazerEntity` (`tools/gen_sungazer.py`): a patch of light that creeps over the ground after its prey at less than
  a walk for three and a half seconds (`GAZE`), burning what it passes over; struck from behind its face it takes
  half as much again.
- `ScorchedWardenEntity`: on its cross (phase 1) it cannot walk (its speed is 0: `setPhase` sets it) and turns after
  its prey; the sun strikes for it (`strikeLater` from above, drawn as gathering light; nobody under a roof is hurt:
  `sunlit`), oftener the further off they stand. Under 55% it catches (`KINDLE`), comes down (phase 2, speed 0.29),
  burns whoever is close and burns down itself by a little every second. Lying after `FALL` it is `open()`.
  Left alone it goes back to its mark (`onReset`).
- `HollowMillerEntity` and `MillstoneEntity`: the stone on its beam is an entity of its own, a `SoulsMob` only so as
  to be drawn and moved as one - it cannot be hurt, has no egg, and rolls round the post (`RADIUS` 6) while the Miller
  has somebody to fight; `hasten` and `turnBack` are the Miller's `HEAVE` and `CHECK`. The post is the timber nearest
  to the Miller's mark (`HollowMillerEntity.post`: his mark is three cells before it). The model rolls the stone by
  the angle it has gone, which the entity keeps in `attackTime` (thousandths of a radian). Under half his health the
  Miller tears the stone off (`UNSHIP`, phase 2: the stone entity goes, the stone is a part of his model): he rolls it
  (`ROLL`; `open()` after it meets a wall or its run ends), drops it (`DROP`), kicks the sacks open (`SACKS`: four
  clouds) and lifts his lantern (`LANTERN`: every cloud near goes up).

## Its things

- `rye_straw`, `flour_sack`; the sickle, the scythe (two hands), the flail; the Warden's pole (of his soul; what it
  strikes burns, like the great candle) and the Miller's stone as a shield (of his soul). The kiln door is a shield
  behind which fire does a quarter of its hurt and nobody burns (`Gear.DOOR_FIRE`, in `SoulMechanics.onIncomingDamage`).
- A reaper's harvest clothes (`ArmorSet.REAPER`): fire hurts the wearer less, 12% a piece (`Gear.fireShare`).
- Spells: `DRY_WIND` (a jet that throws back, and hurts what is alight three times as much) and `SUN_PATCH`
  (`SunPatchEntity`: a second to gather where the caster looks, then it burns).
- The charm `twin_ear`: a gulp of the flask is not spent one time in four (`FlaskItem.finishUsingItem`).
