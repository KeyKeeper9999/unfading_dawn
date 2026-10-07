# The Black Firwood

Part of the map "Вышний Острог". What is common to the whole map is in `docs/map.md`.

## The place

- The Black Firwood (`tools/gen_skete.py`), a hollow of the plateau north-east of the town full of old firs, at an hour
  of its own: the one before a dawn that does not come (`PlaceNameBlock.FOREDAWN`: the sky has begun to pale over the
  trees and there is no sun). No fog and no biome of its own: its dark is the firs'. It is entered by the Wood Wicket, a
  grate in the cliff of the yard between the road tunnel and the town's north gate (`locked_gate[lock=firwood]`); its
  key lies in the grove behind the Weeping Mother. Wicket -> a passage that turns twice (the clock is turned in the leg
  from which neither mouth is seen) -> the old fir (hearth) -> the track through the wood (moths, sleepers, a glade of
  stakes to the west, a wayside shrine to the east) -> the Shatun's Hollow (the Shatun, and dead firs to put between him
  and oneself; a seal behind him) -> the skete's edge (hearth, anvil) -> the skete: white tents round the Vigil, three
  rows of the wakeful on their knees with their backs to the way in and their faces to the east, wakers on their rounds,
  the wax yard -> the White Tent (the Elder; a seal behind him) -> the candle store (hearth, anvil). The skete is gone
  round behind the tents, or crept through, or fought all at once. Left for later: a barred way east out of the
  court behind the White Tent.

## Its dead and its lords

- The Firwood's own (`tools/gen_sect.py`, `gen_waxen.py`, `gen_moth.py`, `gen_shatun.py`, `gen_elder.py`; they share
  `client/QuarryRenderer`): `UnsleepingEntity` is what the people of the skete have in common. `WakefulEntity` kneels
  (phase 1) facing the way its mark faces and does not look about: `sees` is the whole rule of its eyes (a cone of 55
  degrees to ten blocks, five for whoever creeps; it feels a walker within 2.2 at its back), and one struck on his knees
  wakes those kneeling within `BESIDE` of him. `WakerEntity` looks about like anybody; what he sees he beats his board
  for (`KNOCK`), and at the third stroke everything of the skete within `HEARD` blocks is up. `SleeperEntity` knows of
  nobody until they are near, and one leap in a few ends flat on its face (`LUNGE_FALL`, a move of its own). `WaxenEntity`
  holds a rope of hot wax on its prey and bursts when killed; `SleepMothEntity` lies on the ground like a rag until
  somebody comes near, then hangs over them and sheds dust (`Gear.drowse`: darkness and slowness, shortened by the
  linen of the Unsleeping). The lords: `ShatunEntity`, a bear (its charge cannot turn and ends in `CRASH` against
  anything that does not give - for as long as that lasts its hide lets everything through), and `ElderEntity`, who
  calls the kneeling round his tent's wall and, hurt enough, falls asleep on his feet (phase 2: other moves, and
  `DREAM`, which is rolled through like a blow). `SoulsMob.struck` is what a blow does besides hurting (setting alight,
  slowing); `strikeLands`/`strikeComing` are how a marked place is struck and shown, for a kind that drops something
  other than rock.
