#!/usr/bin/env python3
"""The Stubble: the map's seventh place, built into the world's Map by gen_world.py (it has no plan of its own).

The low country west of the town, at an hour of its own: the afternoon of a harvest day, the sun high and white and no
wind at all. The plateau ends behind the gate, and from there the fields go on as far as the haze lets one see: the
place is the part of them a fence keeps to (`fences`), and the rest is the open country (`country`) that nobody goes
into, with its balks and hedges, its stooks, its post mills and a belt of trees at its far edge. The corn of the place - standing, and cut - is grass in the colour its biome gives it
(worldgen/biome/stubble.json). The way in is the Reapers' Gate, a grate in the west cliff of the yard between the road
tunnel and the town's north gate, across from the Wood Wicket; its key hangs in the lamp room of the lighthouse.
Behind the grate a passage turns twice, and in the leg from which neither mouth is seen the hour is turned.

    gate -> the balk (a hearth, an anvil under a lean-to) -> the fields: a cart road west between cut land, where the
    stooks stand, and standing corn, where the reapers are; scarecrows on their stakes, some of them only that -> the
    Watched Field, a round cut field shut in by a hedge (the Scorched Warden on its cross; lean-tos of straw the sun
    does not reach under; a seal behind it) -> the threshing yard (a hearth in the drying barn) -> the floor where
    they threshed, the barns, the standing corn north of the mill -> the Great Mill (the Hollow Miller, on the
    grinding floor, with the stone going round the post; a seal behind him) -> the granary (a hearth), and a way on
    that is barred.

A door in the fence between the fields and the corn behind the Warden, opened from the corn's side, is the short way back.

Everything stands on the level ground. Nothing in it is a way up: stooks and lean-tos are two blocks and more, ricks
four, the barns' eaves five, and the mills stand on nothing that can be climbed. The fence is two fences high, its top
higher than one jumps from a chest or a barrel, and nothing stands within a cell of it."""
import math

import gen_village
from gen_map import FLOOR_LANTERN, LANTERN, hsh
from gen_structures import AIR, MOD

HAY = 'minecraft:hay_block[axis=%s]'
LOG = 'minecraft:oak_log[axis=%s]'
BARE = 'minecraft:stripped_oak_log[axis=%s]'
PLANK = 'minecraft:oak_planks'
DARK = 'minecraft:spruce_planks'
POLE = 'minecraft:oak_fence'
SLAB = 'minecraft:oak_slab[type=bottom,waterlogged=false]'
CLOTH = 'minecraft:white_wool'
WHITE = 'minecraft:calcite'

X0, X1, Z0, Z1 = 20, 192, 362, 474           # the box everything of the Stubble lies in
GATE = (211, 401)                            # the grate in the cliff of the yard: its middle cell
TURN = 196                                   # the passage runs west to here, north, and west again
ROAD_Z = 386                                 # the line it comes out on, and the cart road follows
FIELDS = (88, 186, 366, 397)                 # the fields before the Warden: x0, x1, z0, z1
FENCE_Z = 398                                # the fence between them and the corn behind the Warden: a row of the fields
NW_CORN = (88, 108, 348, 366)                # and the corn widens north-west, towards the open country
CUT_FROM = 150                               # east of this line they are cut; west of it the corn stands
BALK = (178, 380)                            # the hearth by the gate
WATCH = (58, 386)                            # the Watched Field: its middle,
WATCH_R = 17                                 # and how far it reaches
YARD = (24, 141, 418, 470)                   # the land behind the Warden: the threshing yard, and the mill's (its back is to the rock)
NORTH_CORN = (84, 144, 399, 418)             # and the corn that still stands north of the mill
KILN = (48, 428)                             # the drying barn: a hearth
FLOOR = (76, 446)                            # the threshing floor
FLOOR_R = 7
MILL = (116, 438)                            # the Great Mill: the post in the middle of its grinding floor
MILL_R = 13                                  # the outside of its wall; the wall is two thick
MILL_WALL = 11                               # how high the wall of the grinding floor stands; the gallery is over it
TOWER_R = (8.5, 5.0)                         # the tower on it: how wide at the gallery, and at the cap
TOWER_H = 22                                 # and how high
SAIL = 17                                    # how long a sail is, from the hub
DOOR = (100, FENCE_Z)                        # the short way back: a door in the fence, opened from the corn's side
OPEN_X, OPEN_Z = 104, 56                     # the open country: west of this line, and as far north as this
HEDGE = 'minecraft:oak_leaves[distance=7,persistent=true,waterlogged=false]'
FENCE = 'minecraft:spruce_fence'
CLEARING = (112, 372)                        # a patch somebody cut and left, in the standing corn: a chest
NOOK = (96, 393)                             # and another: a shard


def smooth(x, z, scale, salt):
    fx, fz = x / scale, z / scale
    x0, z0 = math.floor(fx), math.floor(fz)
    tx, tz = fx - x0, fz - z0
    tx, tz = tx * tx * (3 - 2 * tx), tz * tz * (3 - 2 * tz)
    a, b_ = hsh(x0, 0, z0, salt), hsh(x0 + 1, 0, z0, salt)
    c, d = hsh(x0, 0, z0 + 1, salt), hsh(x0 + 1, 0, z0 + 1, salt)
    return (a * (1 - tx) + b_ * tx) * (1 - tz) + (c * (1 - tx) + d * tx) * tz


def furrow(x, y, z):
    """Land that bears: turf, in rows."""
    n = hsh(x, 0, z, 920)
    return 'minecraft:grass_block' if n < 0.93 else 'minecraft:coarse_dirt'


def stubble(x, y, z):
    """Land that has been cut: the same turf, worn through to the earth in strips where the carts went."""
    n = hsh(x, 0, z, 921)
    if z % 4 == 0 and n < 0.6:
        return 'minecraft:coarse_dirt'
    return 'minecraft:grass_block' if n < 0.8 else ('minecraft:coarse_dirt' if n < 0.93 else 'minecraft:rooted_dirt')


def trodden(x, y, z):
    """A road, a yard: earth beaten hard."""
    n = hsh(x, 0, z, 922)
    return 'minecraft:dirt_path' if n < 0.55 else ('minecraft:coarse_dirt' if n < 0.82 else ('minecraft:packed_mud' if n < 0.94 else 'minecraft:gravel'))


def beaten(x, y, z):
    """A threshing floor: clay, swept."""
    n = hsh(x, 0, z, 923)
    return 'minecraft:packed_mud' if n < 0.7 else ('minecraft:smooth_sandstone' if n < 0.88 else 'minecraft:coarse_dirt')


def lining(x, y, z):
    n = hsh(x, y, z, 924)
    return 'minecraft:cobblestone' if n < 0.45 else ('minecraft:stone_bricks' if n < 0.72 else ('minecraft:andesite' if n < 0.88 else 'minecraft:mossy_cobblestone'))


def masonry(x, y, z):
    """The foot of the mill: field stone, laid dry."""
    n = hsh(x, y, z, 925)
    return 'minecraft:cobblestone' if n < 0.4 else ('minecraft:stone_bricks' if n < 0.66 else ('minecraft:andesite' if n < 0.84 else 'minecraft:cracked_stone_bricks'))


def limewash(x, y, z):
    """And the rest of it: whitened once, and white still - there has been no rain to take it off."""
    n = hsh(x, y, z, 926)
    return WHITE if n < 0.72 else ('minecraft:diorite' if n < 0.9 else 'minecraft:white_terracotta')


def tree(b, x, y, z, n):
    """A tree of the hedgerows and the belts between fields: an oak or a birch, on the cell (x, y - 1, z); `n` (0..1)
    picks which and how tall. Leaves only go where nothing is."""
    birch = n < 0.45
    log = 'minecraft:birch_log[axis=y]' if birch else 'minecraft:oak_log[axis=y]'
    leaves = 'minecraft:birch_leaves[distance=7,persistent=true,waterlogged=false]' if birch else HEDGE
    h = 4 + int(n * 97) % 3
    b.fill(x, y, z, x, y + h - 1, z, log)
    for dy in range(h - 2, h + 2):
        r = 1 if dy == h + 1 else 2
        for dx in range(-r, r + 1):
            for dz in range(-r, r + 1):
                if abs(dx) == r and abs(dz) == r and hsh(x + dx, y + dy, z + dz, 975) < 0.7:
                    continue
                if b.free(x + dx, y + dy, z + dz):
                    b.set(x + dx, y + dy, z + dz, leaves)


class Stubble:
    def __init__(self, b, gy):
        self.b, self.g = b, gy
        self.mask = {(x, z) for x in range(X0, X1 + 1) for z in range(Z0, Z1 + 1) if self._in(x, z)}
        # how far every column round the place and the open country is from them, to sixteen: the plateau falls to
        # them in a slope of grass (gen_world.land asks `slope`), except behind the gate and the granary, where it is rock
        self.soft = {}
        ring = [c for c in self.mask] + [(x, z) for x in range(0, OPEN_X + 1) for z in range(OPEN_Z, 482) if self.lowland(x, z)]
        seen = set(ring)
        for far in range(1, 17):
            grown = []
            for (x, z) in ring:
                for n in ((x + 1, z), (x - 1, z), (x, z + 1), (x, z - 1)):
                    if n not in seen and n[0] >= 0 and n[1] >= 0:
                        seen.add(n)
                        if not self.rock(*n):
                            self.soft[n] = far
                            grown.append(n)
            ring = grown
        # the cells of the place a fence may stand on: on its edge, with open ground or a slope beyond
        self.edge = {(x, z) for (x, z) in self.mask
                     if any(n not in self.mask and (n in self.soft or self.lowland(*n)) for n in ((x + 1, z), (x - 1, z), (x, z + 1), (x, z - 1)))}
        self.edge |= {(x, z) for x in range(NORTH_CORN[0] - 3, NORTH_CORN[1] + 4) for z in (FENCE_Z,) if (x, z) in self.mask and (x, z + 1) in self.mask}
        self.taken = set()               # columns something stands on or a way runs over: nothing is planted there
        self.bare = set()                # and columns where the ground is not a field's (a road, a yard, a floor)
        self.hedge = set()               # the hedge round the Watched Field
        self.mobs = True

    # ------------------------------------------------------------------ the shape of it

    def _in(self, x, z):
        w = (smooth(x, z, 11, 900) - 0.5) * 3.0 + (smooth(x, z, 4, 901) - 0.5) * 1.0       # the edge of the Watched Field wanders
        # the ways into the Watched Field and out of it: lanes between fences, as wide as the fog in them
        if ROAD_Z + 1 <= z <= ROAD_Z + 3 and WATCH[0] + WATCH_R - 1 <= x <= FIELDS[0] + 3:
            return True
        if WATCH[0] - 1 <= x <= WATCH[0] + 1 and WATCH[1] + WATCH_R - 1 <= z <= YARD[2] + 3:
            return True
        if math.hypot(x - WATCH[0], z - WATCH[1]) <= WATCH_R + w * 0.3:
            return True
        # (a field is fenced in a straight line)
        x0, x1, z0, z1 = FIELDS
        if x0 <= x <= x1 and z0 <= z <= FENCE_Z:
            return True
        x0, x1, z0, z1 = NW_CORN
        if x0 <= x <= x1 and z0 <= z <= z1:
            return True
        x0, x1, z0, z1 = YARD
        if x0 <= x <= x1 and z0 <= z <= z1:
            return True
        x0, x1, z0, z1 = NORTH_CORN
        if x0 <= x <= x1 and FENCE_Z < z <= z1:
            return True
        return False

    def inside(self, x, z):
        return (x, z) in self.mask

    def lowland(self, x, z):
        """The open country: the fields beyond the fence, level with the place, to the edge of the map and the haze."""
        return 0 <= x <= OPEN_X and OPEN_Z <= z <= (481 if x < 22 else 471) and (x, z) not in self.mask

    def owns(self, x, z):
        """The place and its open country: the floor of the land here (see gen_world.in_gorge), and planted by this."""
        return (x, z) in self.mask or self.lowland(x, z)

    @staticmethod
    def rock(x, z):
        """Where the plateau stays a face of rock: over the passage from the town's yard, and behind the granary."""
        return x >= 178 or (x >= 142 and MILL[1] - 13 <= z <= MILL[1] + 13)

    def slope(self, x, z):
        """How high the plateau may stand here: it rises from the place and the open country a block in four thirds."""
        d = self.soft.get((x, z))
        return 999 if d is None else 1.0 + 0.75 * (d - 1)

    def near(self, x, z):
        """On the slopes round the place: grass and hedgerow, and no firs (see gen_world.woods)."""
        return (x, z) in self.soft

    def clear_of_fences(self, x0, z0, x1, z1):
        """Nothing a block high stands within a cell of where a fence may stand: it would be a step over it."""
        return not any((x, z) in self.edge for x in range(min(x0, x1) - 1, max(x0, x1) + 2) for z in range(min(z0, z1) - 1, max(z0, z1) + 2))

    def zone(self, x, z):
        """What a column belongs to: the Warden's field, the cut land, the standing corn, the yard."""
        if math.hypot(x - WATCH[0], z - WATCH[1]) <= WATCH_R + 3:
            return 'watch'
        if z <= FIELDS[3] + 4 and x >= FIELDS[0] - 4:
            return 'cut' if x >= CUT_FROM + (smooth(x, z, 6, 902) - 0.5) * 6 else 'corn'
        if z < NORTH_CORN[3] + 4 and x >= NORTH_CORN[0] - 4 and z >= NORTH_CORN[2] - 4:
            return 'corn'
        # the yard is the ground round what is built in it; the rest of that land is cut
        built = min(math.hypot(x - ax, z - az) for ax, az in (KILN, FLOOR, (34, 442), (36, 458), (WATCH[0], YARD[2] + 6)))
        return 'yard' if built < 13 + (smooth(x, z, 5, 903) - 0.5) * 6 else 'cut'

    def keep(self, x0, z0, x1, z1):
        for x in range(min(x0, x1), max(x0, x1) + 1):
            for z in range(min(z0, z1), max(z0, z1) + 1):
                self.taken.add((x, z))

    def ground(self):
        b, g = self.b, self.g
        for (x, z) in self.mask:
            zone = self.zone(x, z)
            if zone == 'yard':
                b.set(x, g, z, trodden(x, g, z) if hsh(x, 1, z, 927) < 0.75 else stubble(x, g, z))
                self.bare.add((x, z))
            else:
                b.set(x, g, z, furrow(x, g, z) if zone == 'corn' else stubble(x, g, z))

    def way(self, points, half=1, salt=0):
        """A cart road."""
        b, g = self.b, self.g
        for (ax, az), (bx, bz) in zip(points, points[1:]):
            steps = max(abs(bx - ax), abs(bz - az), 1)
            for i in range(steps + 1):
                px, pz = round(ax + (bx - ax) * i / steps), round(az + (bz - az) * i / steps)
                for dx in range(-half - 1, half + 2):
                    for dz in range(-half - 1, half + 2):
                        x, z = px + dx, pz + dz
                        if (x, z) not in self.mask:
                            continue
                        core = max(abs(dx), abs(dz)) <= half
                        if core or hsh(x, 0, z, 930 + salt) < 0.35:
                            b.set(x, g, z, trodden(x, g, z))
                            self.bare.add((x, z))
                        if core:
                            self.taken.add((x, z))

    def patch(self, cx, cz, r, salt=0):
        """A patch cut out of the standing corn."""
        for dx in range(-r - 1, r + 2):
            for dz in range(-r - 1, r + 2):
                x, z = cx + dx, cz + dz
                if (x, z) in self.mask and math.hypot(dx, dz) <= r + (hsh(x, 2, z, 931 + salt) - 0.5) * 1.6:
                    self.b.set(x, self.g, z, stubble(x, self.g, z))
                    self.bare.add((x, z))

    def mob(self, kind, x, z, facing='south', hidden=False):
        """`hidden`: it stands in the corn, and the corn is left standing round it."""
        b, g = self.b, self.g
        tall = 3 if kind in ('scorched', 'sungazer') else 2                # (those two stand three blocks high)
        if (x, z) not in self.mask or not all(b.free(x, g + h, z) for h in range(1, tall + 1)):
            raise ValueError(f'no room for {kind} at {(x, z)}: {b.get(x, g + 1, z)}')
        if self.mobs:
            b.mob(kind, x, g + 1, z, facing)
        if hidden:
            self.taken.add((x, z))
        else:
            self.keep(x - 1, z - 1, x + 1, z + 1)

    # ------------------------------------------------------------------ what stands in a field

    def stook(self, x, z):
        """Sheaves stood together to dry, as they stand all over cut land."""
        b, g = self.b, self.g
        if (x, z) not in self.mask or (x, z) in self.taken or not b.free(x, g + 1, z) or not self.clear_of_fences(x, z, x, z):
            return
        b.fill(x, g + 1, z, x, g + 2, z, HAY % 'y')
        self.keep(x - 1, z - 1, x + 1, z + 1)

    def rick(self, x0, z0, x1, z1, height=4):
        """A rick: straw stacked the length of a field's end and thatched with more of it."""
        b, g = self.b, self.g
        if not self.clear_of_fences(x0, z0, x1, z1):
            return
        long_x = x1 - x0 >= z1 - z0
        for x in range(x0, x1 + 1):
            for z in range(z0, z1 + 1):
                if (x, z) not in self.mask:
                    continue
                end = (x in (x0, x1)) if long_x else (z in (z0, z1))
                side = (z in (z0, z1)) if long_x else (x in (x0, x1))
                top = height - (1 if end else 0) - (1 if side and (z1 - z0 if long_x else x1 - x0) >= 2 else 0)
                for y in range(g + 1, g + 1 + max(3, top)):
                    if b.free(x, y, z):
                        b.set(x, y, z, HAY % ('x' if long_x else 'z'))
        self.keep(x0 - 1, z0 - 1, x1 + 1, z1 + 1)

    def lean_to(self, x, z):
        """Four poles and a roof of straw, such as reapers eat under at noon: the one thing in a cut field the sun
        does not reach under."""
        b, g = self.b, self.g
        if not self.clear_of_fences(x - 1, z - 1, x + 1, z + 1):
            return
        for dx in (-1, 1):
            for dz in (-1, 1):
                b.fill(x + dx, g + 1, z + dz, x + dx, g + 2, z + dz, POLE)
        b.fill(x - 1, g + 3, z - 1, x + 1, g + 3, z + 1, HAY % 'x')
        self.keep(x - 2, z - 2, x + 2, z + 2)

    def sunflowers(self, cells):
        b, g = self.b, self.g
        for x, z in cells:
            if (x, z) in self.mask and (x, z) not in self.taken and b.free(x, g + 1, z) and b.free(x, g + 2, z) and 'grass_block' in str(b.get(x, g, z)):
                b.set(x, g + 1, z, 'minecraft:sunflower[half=lower]')
                b.set(x, g + 2, z, 'minecraft:sunflower[half=upper]')
                self.taken.add((x, z))

    def cart(self, x, z, along_x=True):
        """A harvest cart left where it stood, its load still on it."""
        b, g = self.b, self.g
        cells = [(x + i, z + j) if along_x else (x + j, z + i) for i in range(3) for j in range(2)]
        if not all(c in self.mask and b.free(c[0], g + 1, c[1]) for c in cells) or not self.clear_of_fences(*cells[0], *cells[-1]):
            return
        for cx, cz in cells:
            b.set(cx, g + 1, cz, 'minecraft:spruce_slab[type=top,waterlogged=false]')
            b.set(cx, g + 2, cz, HAY % ('x' if along_x else 'z'))
        for cx, cz in cells[::2]:
            if hsh(cx, 3, cz, 932) < 0.8:
                b.set(cx, g + 3, cz, HAY % ('x' if along_x else 'z'))
        self.keep(min(c[0] for c in cells) - 1, min(c[1] for c in cells) - 1, max(c[0] for c in cells) + 1, max(c[1] for c in cells) + 1)

    # ------------------------------------------------------------------ the gate and the passage

    def passage(self, height):
        """From the yard before the town's north gate: west under the plateau, a turn north, a turn west again, and out
        on the balk. In the leg that runs north, from which neither mouth is seen, the hour is turned."""
        b, g = self.b, self.g
        gx, gz = GATE
        nz = ROAD_Z
        end = max(x for (x, z) in self.mask if z == nz)
        path = [(x, gz) for x in range(gx, TURN - 1, -1)] + [(TURN, z) for z in range(gz, nz - 1, -1)] + [(x, nz) for x in range(TURN, end, -1)]
        middle = set(path)
        cells = {(px + dx, pz + dz) for (px, pz) in path for dx in (-1, 0, 1) for dz in (-1, 0, 1) if px + dx <= gx} - self.mask
        shell = {(x + dx, z + dz) for (x, z) in cells for dx in (-1, 0, 1) for dz in (-1, 0, 1)} - cells - self.mask
        for (x, z) in cells:
            if height.get((x, z), 0) <= 0:
                raise ValueError(f'the passage to the Stubble comes out into the open at {(x, z)}')
            top = g + (4 if (x, z) in middle and x < gx - 1 else 3)
            b.set(x, g, z, lining(x, g, z) if hsh(x, 0, z, 935) < 0.6 else 'minecraft:coarse_dirt')
            b.fill(x, g + 1, z, x, top, z, AIR)
            b.set(x, top + 1, z, lining(x, top + 1, z))
            for y in range(top + 2, g + max(height[(x, z)], 7) + 1):
                if b.get(x, y, z) in (None, AIR):
                    b.set(x, y, z, 'minecraft:stone')
        for (x, z) in shell:
            if height.get((x, z), 0) > 0:
                b.fill(x, g, z, x, g + 5, z, lining)
                for y in range(g + 6, g + max(height[(x, z)], 7) + 1):
                    if b.get(x, y, z) in (None, AIR):
                        b.set(x, y, z, 'minecraft:stone')
        # the grate: three wide, three high, and nothing round it but rock
        gate = [(gx, y, z) for y in (g + 1, g + 2, g + 3) for z in (gz - 1, gz, gz + 1)]
        for cell in gate:
            b.set(*cell, f'{MOD}:locked_gate[axis=z,lock=stubble]')
        b.seals['stubble_key'] = gate
        for z in (gz - 2, gz + 2):                               # a lamp either side of it, on the yard's side
            gen_village.wall_lamp(b, gx + 1, g, z, (1, 0))
        # lamps in the passage, far apart; and straw that has blown in from the far end, more of it the nearer one comes
        for i, (px, pz) in enumerate(path):
            if i % 13 == 6 and (px, pz) in cells and b.free(px, g + 4, pz):
                b.set(px, g + 4, pz, LANTERN)
        for (x, z) in cells:
            if x < TURN and hsh(x, 5, z, 936) < 0.5:
                b.set(x, g, z, HAY % 'y' if hsh(x, 6, z, 937) < 0.3 else 'minecraft:coarse_dirt')
        b.threshold('to_night', TURN - 1, g + 1, gz - 4, TURN + 1, gz - 3)
        b.threshold('to_afternoon', TURN - 1, g + 1, nz + 3, TURN + 1, nz + 4)
        b.threshold('stubble', end - 2, g + 1, nz - 1, end - 1, nz + 1)
        self.path = path
        self.mouth = end

    # ------------------------------------------------------------------ the fields

    def balk(self):
        """Where the road comes out: a lean-to with a hearth under it, an anvil, a cart, a rick."""
        b, g = self.b, self.g
        b.zone = 'stubble'
        hx, hz = BALK
        self.way([(self.mouth, ROAD_Z), (160, ROAD_Z), (140, ROAD_Z - 1), (120, ROAD_Z + 1), (FIELDS[0], ROAD_Z + 2)], 1, 1)
        self.way([(hx, ROAD_Z - 1), (hx, hz + 1)], 1, 2)
        for dx in (-2, 2):
            for dz in (-2, 1):
                b.fill(hx + dx, g + 1, hz + dz, hx + dx, g + 3, hz + dz, POLE)
        for x in range(hx - 2, hx + 3):
            for z in range(hz - 2, hz + 2):
                b.set(x, g + 4, z, HAY % 'x')
                b.set(x, g, z, trodden(x, g, z))
                self.bare.add((x, z))
        b.hearth(hx, g + 1, hz, 'balk')
        b.set(hx - 1, g + 1, hz - 1, f'{MOD}:forge')
        b.set(hx + 1, g + 3, hz - 2, LANTERN)
        self.keep(hx - 3, hz - 3, hx + 3, hz + 2)
        self.cart(hx - 9, hz - 3)
        self.rick(self.mouth - 6, FIELDS[2] + 1, self.mouth - 2, FIELDS[2] + 3)
        self.sunflowers([(x, ROAD_Z - 3) for x in range(150, 176, 3)] + [(x, ROAD_Z + 3) for x in range(152, 176, 4)])

    def fields(self):
        """Cut land east, where the stooks stand in their rows; standing corn west, with the road through it."""
        b, g = self.b, self.g
        b.zone = 'stubble'
        x0, x1, z0, z1 = FIELDS
        walkers = {(162, 372), (154, 394), (146, 376)}
        for wx, wz in sorted(walkers):
            self.mob('stook', wx, wz, 'east')
        # what is met on the road and beside it
        self.mob('scarecrow', 166, ROAD_Z + 5, 'north')
        self.mob('scarecrow', 156, ROAD_Z - 6, 'south')
        self.mob('reaper', 148, ROAD_Z + 6, 'east')
        self.mob('reaper', 152, ROAD_Z - 9, 'east')
        # the standing corn: two patches somebody cut and left, a path to each
        cx, cz = CLEARING
        self.patch(cx, cz, 4, 1)
        self.way([(cx + 3, ROAD_Z - 2), (cx + 1, cz + 3)], 0, 3)
        b.loot(cx, g + 1, cz - 2, 'stubble_field', 'south')
        self.keep(cx - 1, cz - 3, cx + 1, cz - 1)
        self.mob('scarecrow', cx - 3, cz, 'east')
        self.mob('sungazer', cx + 3, cz - 2, 'south')
        nx, nz = NOOK
        self.patch(nx, nz, 3, 2)
        self.way([(nx + 6, ROAD_Z + 3), (nx + 1, nz - 1)], 0, 4)
        b.shard(nx, g + 1, nz)
        self.keep(nx - 1, nz - 1, nx + 1, nz + 1)
        self.mob('sungazer', nx - 2, nz - 2, 'east')
        # in the corn itself, either side of the road: they are not seen until the corn moves
        for x, z, facing in ((138, ROAD_Z - 5, 'south'), (130, ROAD_Z + 6, 'north'), (124, ROAD_Z - 8, 'south'), (108, ROAD_Z + 6, 'north')):
            self.mob('reaper', x, z, facing, hidden=True)
        self.mob('mower', 118, ROAD_Z, 'east')
        self.mob('scarecrow', 134, ROAD_Z - 3, 'south')
        self.mob('scarecrow', 104, ROAD_Z + 4, 'north')
        self.mob('scarecrow', 126, ROAD_Z + 4, 'north')
        self.cart(141, ROAD_Z + 4)
        # before the Warden's door: one who went too near
        self.mob('scorched', x0 + 4, ROAD_Z + 2, 'east')
        self.sunflowers([(x, ROAD_Z - 2 - (x // 7) % 2) for x in range(92, 146, 5)])
        # the stooks, in rows across the cut land; some of them are not what they are
        for x in range(CUT_FROM + 2, x1 - 10, 4):
            for z in list(range(z0 + 4, ROAD_Z - 4, 5)) + list(range(ROAD_Z + 6, z1 - 1, 5)):
                at = (x + (z // 5) % 2, z)
                if at in walkers or any(abs(at[0] - wx) <= 1 and abs(at[1] - wz) <= 1 for wx, wz in walkers):
                    continue
                if hsh(x, 4, z, 940) < 0.8:
                    self.stook(*at)

    def watch(self):
        """The Watched Field: a round cut field shut in by a hedge higher than a man; in the middle of it the cross, and
        the Warden on it, whose head is seen over the hedge from the fields. In from the east, out to the south."""
        b, g = self.b, self.g
        b.zone = 'watch_field'
        cx, cz = WATCH
        for (x, z) in self.mask:
            if self.zone(x, z) == 'watch':
                self.taken.add((x, z))
        # the hedge: thorn and hazel grown thick, four high, three through, an old trunk in it here and there
        for dx in range(-WATCH_R - 4, WATCH_R + 5):
            for dz in range(-WATCH_R - 4, WATCH_R + 5):
                x, z = cx + dx, cz + dz
                r = math.hypot(dx, dz)
                if (x, z) in self.mask or not WATCH_R - 1 <= r <= WATCH_R + 2.6 or not self.lowland(x, z):
                    continue
                top = g + 4 + (1 if hsh(x, 7, z, 941) < 0.3 else 0)
                b.fill(x, g + 1, z, x, top, z, HEDGE)
                if hsh(x, 8, z, 942) < 0.06:
                    b.fill(x, g + 1, z, x, top - 1, z, LOG % 'y')
                self.hedge.add((x, z))
        # the two ways through it: a gate of timber in each, roofed with straw, which the fog fills
        xf = cx + WATCH_R + 4
        for z in (ROAD_Z, ROAD_Z + 4):
            b.fill(xf, g + 1, z, xf, g + 4, z, LOG % 'y')
        b.fill(xf, g + 5, ROAD_Z, xf, g + 5, ROAD_Z + 4, LOG % 'z')
        b.fill(xf - 1, g + 6, ROAD_Z - 1, xf + 1, g + 6, ROAD_Z + 5, HAY % 'z')
        b.fog('scorched_warden_fog', xf, g + 1, ROAD_Z + 1, xf, g + 4, ROAD_Z + 3, 'z')
        for x in range(cx + WATCH_R - 1, FIELDS[0] + 4):
            for z in range(ROAD_Z + 1, ROAD_Z + 4):
                b.set(x, g, z, trodden(x, g, z))
                self.taken.add((x, z))
                self.bare.add((x, z))
        zf = cz + WATCH_R + 5
        for x in (cx - 2, cx + 2):
            b.fill(x, g + 1, zf, x, g + 4, zf, LOG % 'y')
        b.fill(cx - 2, g + 5, zf, cx + 2, g + 5, zf, LOG % 'x')
        b.fill(cx - 3, g + 6, zf - 1, cx + 3, g + 6, zf + 1, HAY % 'x')
        b.fog('scorched_warden', cx - 1, g + 1, zf, cx + 1, g + 4, zf, 'x', sealed=True)
        for z in range(cz + WATCH_R - 1, YARD[2] + 4):
            for x in range(cx - 1, cx + 2):
                b.set(x, g, z, trodden(x, g, z))
                self.taken.add((x, z))
                self.bare.add((x, z))
        b.threshold('watch_field', cx + WATCH_R - 3, g + 1, ROAD_Z + 1, cx + WATCH_R - 2, ROAD_Z + 3)
        # the cross: the upright two cells behind the Warden, so that it hangs before it and not in it
        b.fill(cx - 2, g + 1, cz, cx - 2, g + 9, cz, BARE % 'y')
        b.fill(cx - 2, g + 7, cz - 5, cx - 2, g + 7, cz + 5, BARE % 'z')
        b.set(cx - 2, g + 10, cz, 'minecraft:skeleton_skull[rotation=4,powered=false]')
        # lean-tos round it, and what was left standing when they stopped coming here
        for a in (25, 100, 170, 250, 320):
            x, z = round(cx + math.cos(math.radians(a)) * 10.5), round(cz + math.sin(math.radians(a)) * 10.5)
            self.lean_to(x, z)
        for a, r in ((60, 14), (135, 13), (205, 14), (285, 13), (350, 14), (215, 6), (40, 5)):
            x, z = round(cx + math.cos(math.radians(a)) * r), round(cz + math.sin(math.radians(a)) * r)
            if b.free(x, g + 1, z):
                b.fill(x, g + 1, z, x, g + 2, z, HAY % 'y')
        for a in (150, 195, 235, 300):                            # those the sun took here
            x, z = round(cx + math.cos(math.radians(a)) * 8), round(cz + math.sin(math.radians(a)) * 8)
            if b.free(x, g + 1, z):
                b.set(x, g + 1, z, 'minecraft:skeleton_skull[rotation=%d,powered=false]' % ((x * 3 + z) % 16))
        self.mob('scorched_warden', cx, cz, 'east')

    # ------------------------------------------------------------------ behind the Warden

    def thatch(self, x0, z0, x1, z1, y, ridge='x', eave='x'):
        """A roof of straw: pitched, a row narrower each course."""
        b = self.b
        k = 0
        while True:
            if ridge == 'x':
                a0, a1 = z0 + k, z1 - k
                if a0 > a1:
                    break
                for z in (a0, a1):
                    b.fill(x0 - 1, y + k, z, x1 + 1, y + k, z, HAY % 'x')
                if a1 - a0 <= 1:
                    break
                for x in (x0, x1):                                # the gable ends, boarded
                    b.fill(x, y + k, a0 + 1, x, y + k, a1 - 1, PLANK)
            else:
                a0, a1 = x0 + k, x1 - k
                if a0 > a1:
                    break
                for x in (a0, a1):
                    b.fill(x, y + k, z0 - 1, x, y + k, z1 + 1, HAY % 'z')
                if a1 - a0 <= 1:
                    break
                for z in (z0, z1):
                    b.fill(a0 + 1, y + k, z, a1 - 1, y + k, z, PLANK)
            k += 1

    def barn(self, x0, z0, x1, z1, door, ridge='x', lamp=True):
        """A barn of logs: walls four high, a roof of straw, a door two wide and three high at `door` (a cell of the
        wall; the door runs from it along the wall). Returns nothing; its floor is boards."""
        b, g = self.b, self.g
        for x in range(x0, x1 + 1):
            for z in range(z0, z1 + 1):
                edge = x in (x0, x1) or z in (z0, z1)
                corner = x in (x0, x1) and z in (z0, z1)
                b.set(x, g, z, PLANK if not edge else 'minecraft:cobblestone')
                self.bare.add((x, z))
                if edge:
                    for y in range(g + 1, g + 5):
                        b.set(x, y, z, LOG % 'y' if corner else (LOG % ('x' if z in (z0, z1) else 'z') if y % 2 else DARK))
        dx, dz = door
        along = (1, 0) if dz in (z0, z1) else (0, 1)
        for i in (0, 1):
            b.fill(dx + along[0] * i, g + 1, dz + along[1] * i, dx + along[0] * i, g + 3, dz + along[1] * i, AIR)
        self.thatch(x0, z0, x1, z1, g + 5, ridge)
        if lamp:
            mx, mz = (x0 + x1) // 2, (z0 + z1) // 2
            half = ((z1 - z0) if ridge == 'x' else (x1 - x0)) // 2
            b.fill(mx, g + 5, mz, mx, g + 4 + half, mz, POLE)
            b.set(mx, g + 4, mz, LANTERN)
        self.keep(x0 - 2, z0 - 2, x1 + 2, z1 + 2)

    def yard(self):
        """The threshing yard: the drying barn with a hearth in it, the floor where they threshed, two barns, ricks."""
        b, g = self.b, self.g
        b.zone = 'threshing'
        wx = WATCH[0]
        kx, kz = KILN
        b.threshold('threshing', wx - 1, g + 1, YARD[2] + 1, wx + 1, YARD[2] + 2)
        self.way([(wx, YARD[2] + 3), (wx, kz + 6), (FLOOR[0] - 4, FLOOR[1] - 3), (FLOOR[0] + FLOOR_R + 6, FLOOR[1] - 6), (MILL[0] - MILL_R - 3, MILL[1])], 1, 5)
        # the drying barn: open to the east, the hearth inside it
        self.barn(kx - 5, kz - 3, kx + 2, kz + 3, (kx + 2, kz - 1), 'x', lamp=False)
        b.fill(kx + 2, g + 1, kz - 1, kx + 2, g + 3, kz + 1, AIR)
        b.hearth(kx - 2, g + 1, kz, 'kiln')
        b.set(kx - 4, g + 3, kz - 2, LANTERN)
        b.fill(kx - 4, g + 4, kz - 2, kx - 4, g + 5, kz - 2, POLE)
        self.way([(kx + 3, kz), (wx + 1, kz + 2)], 1, 6)
        # the floor: clay, swept, in a ring of posts; the ones who threshed on it in the sun
        fx, fz = FLOOR
        for dx in range(-FLOOR_R - 1, FLOOR_R + 2):
            for dz in range(-FLOOR_R - 1, FLOOR_R + 2):
                d = math.hypot(dx, dz)
                x, z = fx + dx, fz + dz
                if d <= FLOOR_R and (x, z) in self.mask:
                    b.set(x, g, z, beaten(x, g, z))
                    self.taken.add((x, z))
                    self.bare.add((x, z))
        for a in range(0, 360, 45):
            x, z = round(fx + math.cos(math.radians(a + 22)) * (FLOOR_R + 1)), round(fz + math.sin(math.radians(a + 22)) * (FLOOR_R + 1))
            if (x, z) in self.mask and b.free(x, g + 1, z):
                b.fill(x, g + 1, z, x, g + 2, z, POLE)
                self.taken.add((x, z))
        self.mob('scorched', fx - 2, fz + 1, 'north')
        self.mob('scorched', fx + 3, fz - 2, 'west')
        self.mob('scorched', fx + 1, fz + 4, 'north')
        # the barns west of it: in one of them a chest, and a sackman who has not put his sack down
        self.barn(28, 438, 40, 446, (40, 441), 'x')
        b.loot(30, g + 1, 442, 'stubble_barn', 'east')
        self.mob('sackman', 35, 442, 'east')
        self.barn(30, 454, 42, 462, (36, 454), 'x')
        self.way([(41, 442), (FLOOR[0] - FLOOR_R - 2, FLOOR[1])], 1, 7)
        self.way([(36, 453), (44, 448)], 0, 8)
        self.mob('mower', 52, 448, 'north')
        self.mob('reaper', 46, 458, 'east')
        self.mob('reaper', 60, 462, 'north')
        # ricks along the south, a cart, stooks; a shard in the ash of somebody's dinner fire
        self.rick(50, 466, 62, 468)
        self.rick(70, 464, 84, 466)
        self.cart(64, 434)
        for x, z in ((88, 458), (92, 462), (96, 456), (66, 456), (88, 430), (92, 426)):
            self.stook(x, z)
        self.mob('stook', 92, 458, 'west')
        self.mob('stook', 90, 428, 'west')
        b.shard(28, g + 1, 430)
        self.keep(27, 429, 29, 431)
        self.mob('scarecrow', 32, 426, 'east')
        self.mob('scarecrow', 86, 440, 'west')
        self.mob('sackman', 96, 440, 'west')
        self.mob('mower', 98, 448, 'west')

    def north_corn(self):
        """The corn that still stands north of the mill, and the short way back to the fields: a door in the fence."""
        b, g = self.b, self.g
        b.zone = 'threshing'
        dx, dz = DOOR
        # the door: iron, between two posts under a beam, with its lever on the corn's side
        for x in (dx - 1, dx + 1):
            b.fill(x, g + 1, dz, x, g + 3, dz, LOG % 'y')
        b.fill(dx - 1, g + 4, dz, dx + 1, g + 4, dz, LOG % 'x')
        b.door(dx, g + 1, dz, 'north', (dx + 1, g + 2, dz + 1), 'south')
        for z in range(FIELDS[3] - 2, NORTH_CORN[2] + 3):
            for x in range(dx - 1, dx + 2):
                b.set(x, g, z, trodden(x, g, z))
                self.taken.add((x, z))
                self.bare.add((x, z))
        self.way([(dx, NORTH_CORN[2] + 2), (dx - 2, NORTH_CORN[3] + 2), (MILL[0] - MILL_R - 6, MILL[1] - 8), (MILL[0] - MILL_R - 3, MILL[1])], 1, 9)
        self.way([(dx, FIELDS[3] - 1), (dx + 2, ROAD_Z + 3)], 0, 10)
        self.patch(128, 411, 2, 3)
        self.patch(139, 413, 2, 4)
        self.mob('reaper', 110, 411, 'west', hidden=True)
        self.mob('reaper', 119, 414, 'west', hidden=True)
        self.mob('sungazer', 128, 411, 'west')
        self.mob('sungazer', 139, 413, 'west')
        self.sunflowers([(x, 420 + (x // 3) % 3) for x in range(104, 140, 4)])

    # ------------------------------------------------------------------ the mill

    def mill(self):
        """The Great Mill: a tower mill. Its foot is a round of field stone, whitened above, as wide as a threshing
        floor, and the whole of it is the grinding floor: the post in the middle, the track the stone goes round it on,
        and room either side of the track. On it, over a gallery with a rail, the tower: boards on ribs, narrowing to
        the cap; and on the cap, facing the yard, four sails that have not turned since the wind stopped. In by the west
        door; the door east, to the granary, is sealed until the Miller is down."""
        b, g = self.b, self.g
        b.zone = 'great_mill'
        cx, cz = MILL
        R = MILL_R
        top = g + MILL_WALL
        for dx in range(-R - 3, R + 4):
            for dz in range(-R - 3, R + 4):
                d = math.hypot(dx, dz)
                x, z = cx + dx, cz + dz
                if d <= R + 0.5:
                    self.taken.add((x, z))
                    self.bare.add((x, z))
                    if d > R - 1.5:
                        b.set(x, g, z, 'minecraft:cobblestone')
                        for y in range(g + 1, top + 1):
                            b.set(x, y, z, masonry(x, y, z) if y <= g + 4 + (hsh(x, 0, z, 950) < 0.4) else limewash(x, y, z))
                    else:
                        # the bed: stone where the runner goes, boards either side of it
                        track = 4.6 <= d <= 7.4
                        b.set(x, g, z, 'minecraft:smooth_stone' if track else (PLANK if hsh(x, 0, z, 951) < 0.85 else DARK))
                        for y in range(g + 1, top + 1):
                            b.set(x, y, z, AIR)
                    # the boards over the grinding floor, which outside the tower are the gallery
                    b.set(x, top + 1, z, DARK if (dx * 3 + dz) % 7 else 'minecraft:stripped_spruce_log[axis=x]')
                elif d <= R + 2.3 and (x, z) in self.mask and not (dx > R and abs(dz) <= 7):
                    b.set(x, top + 1, z, PLANK)                   # the gallery, on brackets, and its rail (not over the granary)
                    if d > R + 1.3:
                        b.set(x, top + 2, z, POLE)
                    if d < R + 1.5 and int(math.degrees(math.atan2(dz, dx)) + 360) % 30 < 6:
                        b.set(x, top, z, DARK)
        # the tower: boards on eight ribs, a band of logs every few courses, narrowing to the cap; hollow inside
        r0, r1 = TOWER_R
        base = top + 2
        ribs = [math.radians(22.5 + 45 * k) for k in range(8)]
        for k in range(TOWER_H):
            y = base + k
            r = r0 + (r1 - r0) * k / (TOWER_H - 1)
            for dx in range(-int(r) - 2, int(r) + 3):
                for dz in range(-int(r) - 2, int(r) + 3):
                    d = math.hypot(dx, dz)
                    if not r - 1.25 < d <= r + 0.25:
                        continue
                    a = math.atan2(dz, dx)
                    rib = min(abs((a - rb + math.pi) % (2 * math.pi) - math.pi) for rb in ribs) * d < 0.75
                    if rib:
                        state = 'minecraft:dark_oak_log[axis=y]'
                    elif k % 6 == 0:
                        state = 'minecraft:stripped_spruce_log[axis=y]'
                    else:
                        state = 'minecraft:spruce_planks' if hsh(cx + dx, y, cz + dz, 953) < 0.8 else 'minecraft:dark_oak_planks'
                    b.set(cx + dx, y, cz + dz, state)
            if k in (5, 12):                                      # a window either side, small
                for sx in (-1, 1):
                    b.set(cx + sx * round(r - 0.5), y, cz, AIR)
                    b.set(cx + sx * round(r - 0.5), y + 1, cz, AIR)
        # the cap: a hood of boards, a ring narrower each course
        yc = base + TOWER_H
        for k in range(6):
            rr = r1 + 0.9 - k * 1.1
            for dx in range(-int(r1) - 2, int(r1) + 3):
                for dz in range(-int(r1) - 2, int(r1) + 3):
                    d = math.hypot(dx, dz)
                    if d <= rr and (d > rr - 1.6 or k == 5 or rr < 1.6):
                        b.set(cx + dx, yc + k, cz + dz, 'minecraft:dark_oak_planks' if (dx * 3 + dz + k) % 5 else 'minecraft:dark_oak_log[axis=y]')
        b.fill(cx, yc + 6, cz, cx, yc + 7, cz, POLE)
        # the windshaft out of the cap over the yard, and four sails on it: a stock and a lattice to one side of it, the
        # cloth still on two of them; at rest in a cross, as a mill is left
        hy = yc + 1
        hx = cx - int(r0) - 1
        b.fill(hx + 1, hy, cz, cx - int(r1) + 1, hy, cz, LOG % 'x')
        b.set(hx, hy, cz, BARE % 'x')
        for arm, (dy, dz) in enumerate(((1, 0), (0, 1), (-1, 0), (0, -1))):
            ly, lz = -dz, dy                                      # the side the lattice is on: all four turn the same way
            cloth = arm % 2 == 0
            for i in range(1, SAIL + 1):
                y, z = hy + dy * i, cz + dz * i
                b.set(hx, y, z, 'minecraft:stripped_spruce_log[axis=%s]' % ('y' if dy else 'z'))
                if i < 3:
                    continue
                for j in (1, 2, 3, 4):
                    yy, zz = y + ly * j, z + lz * j
                    frame = j == 4 or i % 3 == 0 or i == SAIL
                    if frame:
                        b.set(hx, yy, zz, POLE)
                    elif cloth and hsh(i, j, arm, 954) < 0.92:
                        b.set(hx, yy, zz, CLOTH)
        # the post, from the floor to the boards, and the great beams that held the gear
        b.fill(cx, g + 1, cz, cx, top, cz, BARE % 'y')
        for dx in range(-R + 2, R - 1):
            if dx:
                b.set(cx + dx, g + 9, cz, LOG % 'x')
                b.set(cx, g + 9, cz + dx, LOG % 'z')
        # light: windows in the wall, and lamps on posts against it
        for a in range(0, 360, 45):
            ca, sa = math.cos(math.radians(a + 22.5)), math.sin(math.radians(a + 22.5))
            for r in (R - 1, R):
                x, z = round(cx + ca * r), round(cz + sa * r)
                b.fill(x, g + 6, z, x, g + 7, z, AIR)
            x, z = round(cx + ca * (R - 2.6)), round(cz + sa * (R - 2.6))
            if b.free(x, g + 1, z):
                b.set(x, g + 1, z, POLE)
                b.set(x, g + 2, z, FLOOR_LANTERN)
        # and four hung from the beams inside the track, where the post would stand in its own shadow
        for dx, dz in ((3, 0), (-3, 0), (0, 3), (0, -3)):
            b.fill(cx + dx, g + 6, cz + dz, cx + dx, g + 8, cz + dz, 'minecraft:iron_chain[axis=y]')
            b.set(cx + dx, g + 5, cz + dz, LANTERN)
        # what stands against the wall: sacks, a few of them split
        for a in (70, 110, 250, 290, 200, 160):
            x, z = round(cx + math.cos(math.radians(a)) * (R - 2.6)), round(cz + math.sin(math.radians(a)) * (R - 2.6))
            if b.free(x, g + 1, z):
                b.set(x, g + 1, z, CLOTH)
                if hsh(x, 1, z, 952) < 0.5 and b.free(x, g + 2, z):
                    b.set(x, g + 2, z, CLOTH)
        # the west door: three wide, four high; fog in it. The east door, to the granary: sealed
        for x in (cx - R, cx - R + 1):
            b.fill(x, g + 1, cz - 1, x, g + 4, cz + 1, AIR)
            b.fill(x, g, cz - 1, x, g, cz + 1, 'minecraft:stone_bricks')
        b.fill(cx - R, g + 5, cz - 2, cx - R, g + 5, cz + 2, LOG % 'z')
        b.fog('hollow_miller_fog', cx - R, g + 1, cz - 1, cx - R, g + 4, cz + 1, 'z')
        for x in (cx + R - 1, cx + R):
            b.fill(x, g + 1, cz - 1, x, g + 3, cz + 1, AIR)
            b.fill(x, g, cz - 1, x, g, cz + 1, 'minecraft:stone_bricks')
        b.fog('hollow_miller', cx + R, g + 1, cz - 1, cx + R, g + 3, cz + 1, 'z', sealed=True)
        b.threshold('great_mill', cx - R - 3, g + 1, cz - 1, cx - R - 2, cz + 1)
        for x in range(cx - R - 4, cx - R):
            for z in range(cz - 2, cz + 3):
                if (x, z) in self.mask:
                    b.set(x, g, z, trodden(x, g, z))
                    self.taken.add((x, z))
                    self.bare.add((x, z))
        # the Miller: before the post, his face to the door
        self.mob('hollow_miller', cx - 3, cz, 'west')

    def granary(self, height):
        """Against the rock behind the mill, the granary: a house of field stone under straw, which has no door but the
        mill's. Bins, a hearth, a chest - and a way on, into the rock, that is barred."""
        b, g = self.b, self.g
        b.zone = 'great_mill'
        cx, cz = MILL
        x0, x1, z0, z1 = cx + MILL_R + 1, cx + MILL_R + 12, cz - 6, cz + 6
        for x in range(x0, x1 + 1):
            for z in range(z0, z1 + 1):
                edge = x in (x0, x1) or z in (z0, z1)
                if x == x0 and cz - 1 <= z <= cz + 1:
                    edge = False                                  # the door from the mill
                self.taken.add((x, z))
                self.bare.add((x, z))
                b.set(x, g, z, PLANK)
                if edge:
                    for y in range(g + 1, g + 6):
                        b.set(x, y, z, masonry(x, y, z) if y <= g + 2 else limewash(x, y, z))
                else:
                    b.fill(x, g + 1, z, x, g + 4, z, AIR)
                    b.set(x, g + 5, z, DARK if (x + z) % 4 else LOG % 'x')
        self.thatch(x0 + 1, z0, x1 - 1, z1, g + 6, 'x')
        for z in range(z0 + 1, z1):                               # (the straw's gable ends, at the mill and at the rock)
            for y in range(g + 6, g + 12):
                if b.free(x1, y, z) and abs(z - cz) <= 6 - (y - g - 6):
                    b.set(x1, y, z, PLANK)
        b.hearth(x0 + 5, g + 1, cz, 'granary')
        b.loot(x0 + 8, g + 1, cz - 3, 'stubble_mill', 'west')       # (in from the walls: the yard is the other side of them)
        for x, z in ((x0 + 2, z0 + 1), (x0 + 3, z0 + 1), (x0 + 2, z1 - 1), (x0 + 6, z1 - 1), (x0 + 7, z1 - 1)):
            b.set(x, g + 1, z, 'minecraft:barrel[facing=up]')
        b.set(x0 + 3, g + 1, z1 - 1, CLOTH)
        b.set(x0 + 4, g + 1, z1 - 1, CLOTH)
        for x, z in ((x0 + 3, cz - 3), (x0 + 8, cz + 3)):
            b.set(x, g + 4, z, LANTERN)
        # the way on, barred (a gate nothing in the map opens): east out of the granary, into the rock
        for y in (g + 1, g + 2, g + 3):
            b.set(x1, y, cz, f'{MOD}:locked_gate[axis=z,lock=none]')
        b.fill(x1 + 1, g + 1, cz, x1 + 2, g + 3, cz, AIR)       # a little of the way behind it, to be seen through the bars
        b.fill(x1 + 1, g, cz, x1 + 2, g, cz, lining)
        b.fill(x1 + 1, g + 4, cz, x1 + 2, g + 4, cz, lining)
        self.barred = (x1, cz)

    def windmill(self, x, z, turn=0):
        """A post mill, such as stand all along the edge of the fields: a box of boards on one great post, under a
        roof of straw, with four sails. None of them turns."""
        b, g = self.b, self.g
        if not all((x + dx, z + dz) in self.mask and b.free(x + dx, g + 1, z + dz) for dx in (-1, 0, 1) for dz in (-1, 0, 1)) \
                or not self.clear_of_fences(x - 2, z - 2, x + 2, z + 2):
            return
        b.fill(x, g + 1, z, x, g + 3, z, LOG % 'y')
        b.fill(x - 1, g + 4, z - 1, x + 1, g + 8, z + 1, PLANK)
        for dx, dz in ((-1, -1), (-1, 1), (1, -1), (1, 1)):
            b.fill(x + dx, g + 4, z + dz, x + dx, g + 8, z + dz, LOG % 'y')
        b.fill(x - 1, g + 9, z - 1, x + 1, g + 9, z + 1, HAY % 'x')
        b.set(x, g + 10, z, HAY % 'x')
        fx = x - 2 if not turn else x + 2
        b.set(fx, g + 7, z, BARE % 'x')
        for sy, sz in ((1, 1), (1, -1), (-1, 1), (-1, -1)):
            for i in range(1, 5):
                b.set(fx, g + 7 + sy * i, z + sz * i, POLE)
        self.keep(x - 2, z - 5, x + 2, z + 5)

    def far_mill(self, x, z):
        """A post mill out in the open country: the same as those in the place, on ground nobody walks."""
        b, g = self.b, self.g
        if not all(self.lowland(x + dx, z + dz) and b.free(x + dx, g + 1, z + dz) for dx in (-2, -1, 0, 1, 2) for dz in range(-5, 6)):
            return
        b.fill(x, g + 1, z, x, g + 3, z, LOG % 'y')
        b.fill(x - 1, g + 4, z - 1, x + 1, g + 8, z + 1, PLANK)
        for dx, dz in ((-1, -1), (-1, 1), (1, -1), (1, 1)):
            b.fill(x + dx, g + 4, z + dz, x + dx, g + 8, z + dz, LOG % 'y')
        b.fill(x - 1, g + 9, z - 1, x + 1, g + 9, z + 1, HAY % 'x')
        b.set(x, g + 10, z, HAY % 'x')
        b.set(x + 2, g + 7, z, BARE % 'x')
        for sy, sz in ((1, 1), (1, -1), (-1, 1), (-1, -1)):
            for i in range(1, 5):
                b.set(x + 2, g + 7 + sy * i, z + sz * i, POLE)
        for dx in (-2, -1, 0, 1, 2):
            for dz in range(-5, 6):
                self.taken.add((x + dx, z + dz))

    # ------------------------------------------------------------------ the fence, and the open country

    def fences(self, height):
        """The fence round the place: wattle on posts, wherever the place ends in open ground or in a slope that could
        be walked up; where it ends in rock, the rock is enough. Then every turn of it that two lengths make by touching
        at a corner gets a stake more, or one would slip between them."""
        b, g = self.b, self.g
        b.zone = 'stubble'
        placed = set()
        for (x, z) in sorted(self.edge):
            if not b.free(x, g + 1, z):
                continue
            outs = [n for n in ((x + 1, z), (x - 1, z), (x, z + 1), (x, z - 1)) if n not in self.mask]
            row = z == FENCE_Z and (x, z + 1) in self.mask
            if row or any(height.get(n, 0) <= 2 and b.free(n[0], g + 1 + height.get(n, 0), n[1]) for n in outs):
                # (two high, and so higher than a jump from a chest or a barrel lands: see the docstring of the module)
                post = (x * 7 + z * 3) % 6 == 0
                b.fill(x, g + 1, z, x, g + (3 if post else 2), z, 'minecraft:stripped_oak_log[axis=y]' if post else FENCE)
                placed.add((x, z))
                self.taken.add((x, z))
        for (x, z) in sorted(placed):
            for dx, dz in ((1, 1), (1, -1), (-1, 1), (-1, -1)):
                if (x + dx, z + dz) not in placed:
                    continue
                corner = [(x + dx, z), (x, z + dz)]
                if all(b.free(c[0], g + 1, c[1]) for c in corner):
                    c = next((c for c in corner if c not in self.mask), corner[0])
                    b.fill(c[0], g + 1, c[1], c[0], g + 2, c[1], FENCE)
                    self.taken.add(c)
        self.fenced = placed

    def country(self):
        """The open country: the fields beyond the fence, as far as the haze lets one see. Strips of corn standing,
        of stubble with its stooks, of fallow, a balk of grass between each two and a hedge on some; cornflowers in the
        rye; a cart track north into the haze; post mills here and there; and along the edge of the map a belt of trees,
        and the forest behind it."""
        b, g = self.b, self.g
        b.zone = 'stubble'

        # the land lies in long strips, as open fields do: furlongs fifty deep, each cut into strips of its own widths
        cuts = {}

        def plot(x, z):
            row = (z - OPEN_Z) // 50
            if row not in cuts:
                at, edges = -int(hsh(row, 0, 0, 970) * 12), []
                while at <= OPEN_X + 20:
                    at += 7 + int(hsh(row, len(edges), 0, 978) * 12)
                    edges.append(at)
                cuts[row] = edges
            return next(i for i, e in enumerate(cuts[row]) if x < e), row

        kinds = {}

        def kind(c):
            if c not in kinds:
                n = hsh(c[0], 1, c[1], 971)
                kinds[c] = 'corn' if n < 0.45 else ('cut' if n < 0.82 else 'fallow')
            return kinds[c]

        for at in ((40, 252), (74, 168), (26, 104), (62, 80), (88, 300)):
            self.far_mill(*at)
        track = set()
        for z in range(OPEN_Z, YARD[2] - 12):
            tx = 15 + round(5 * math.sin(z / 31.0) + 2 * math.sin(z / 11.0))
            track |= {(tx + dx, z) for dx in (-1, 0, 1)}
        for x in range(10, YARD[0]):                             # (it ends at the yard's fence, which is shut across it)
            track |= {(x, z) for z in (443, 444, 445)}
        hedges = set()
        for z in range(OPEN_Z, 482):
            for x in range(0, OPEN_X + 1):
                if not self.lowland(x, z) or (x, z) in self.hedge or (x, z) in self.taken or not b.free(x, g + 1, z):
                    continue                                     # (and nothing is planted where a post or a gate stands)
                if x <= 6:
                    b.set(x, g, z, 'minecraft:grass_block')
                    continue
                if (x, z) in track:
                    b.set(x, g, z, trodden(x, g, z))
                    continue
                c = plot(x, z)
                n = hsh(x, 6, z, 972)
                balk = any(plot(x + dx, z + dz) != c for dx, dz in ((1, 0), (-1, 0), (0, 1), (0, -1)))
                if balk:
                    b.set(x, g, z, 'minecraft:grass_block')
                    side = tuple(sorted((c, plot(x + 1, z) if plot(x + 1, z) != c else plot(x, z + 1))))
                    if hsh(side[0][0] * 31 + side[1][0], side[0][1], side[1][1], 973) < 0.3:
                        hedges.add((x, z))
                    elif n < 0.4:
                        b.set(x, g + 1, z, 'minecraft:short_grass')
                    continue
                k = kind(c)
                if k == 'corn':
                    b.set(x, g, z, furrow(x, g, z))
                    if 'grass_block' in b.get(x, g, z) and n < 0.9:
                        if n < 0.025:
                            b.set(x, g + 1, z, 'minecraft:cornflower')
                        else:
                            b.set(x, g + 1, z, 'minecraft:tall_grass[half=lower]')
                            b.set(x, g + 2, z, 'minecraft:tall_grass[half=upper]')
                elif k == 'cut':
                    b.set(x, g, z, stubble(x, g, z))
                    if x % 4 == 1 and z % 5 == 2 and n < 0.8:
                        b.fill(x, g + 1, z, x, g + 2, z, HAY % 'y')
                    elif n < 0.3:
                        b.set(x, g + 1, z, 'minecraft:short_dry_grass')
                else:
                    b.set(x, g, z, 'minecraft:grass_block')
                    if n < 0.3:
                        b.set(x, g + 1, z, 'minecraft:short_grass')
                    elif n < 0.34:
                        b.set(x, g + 1, z, 'minecraft:short_dry_grass')
                    elif n < 0.36:
                        b.set(x, g + 1, z, 'minecraft:cornflower')
        for (x, z) in sorted(hedges):
            if hsh(x, 9, z, 974) < 0.07:
                tree(b, x, g + 1, z, hsh(x, 10, z, 976))
            elif b.free(x, g + 1, z):
                b.fill(x, g + 1, z, x, g + 2, z, HEDGE)
        # the belt along the edge of the map: two rows of trees, oak and birch, and the forest behind it
        for z in range(OPEN_Z, 482, 2):
            for x in (2, 5):
                if (z + x) % 4 == 0 and self.lowland(x, z) and b.free(x, g + 1, z):
                    tree(b, x, g + 1, z, hsh(x, 11, z, 977))

    # ------------------------------------------------------------------ what grows

    def crops(self):
        """The corn, standing and cut, on every column nothing else has taken."""
        b, g = self.b, self.g
        for (x, z) in sorted(self.mask):
            if (x, z) in self.taken or (x, z) in self.bare or not b.free(x, g + 1, z):
                continue
            ground = str(b.get(x, g, z))
            if 'grass_block' not in ground and 'coarse_dirt' not in ground and 'rooted_dirt' not in ground:
                continue
            zone = self.zone(x, z)
            n = hsh(x, 6, z, 960)
            # (corn does not stand hard against the rock or a wall: its edge is cut, as a field's is)
            edge = any((x + dx, z + dz) not in self.mask for dx, dz in ((1, 0), (-1, 0), (0, 1), (0, -1)))
            if zone == 'corn' and not edge and 'grass_block' in ground:
                if n < 0.9 and b.free(x, g + 2, z):
                    b.set(x, g + 1, z, 'minecraft:tall_grass[half=lower]')
                    b.set(x, g + 2, z, 'minecraft:tall_grass[half=upper]')
                elif n < 0.97:
                    b.set(x, g + 1, z, 'minecraft:short_grass')
            elif zone in ('cut', 'watch') or edge:
                if n < 0.3:
                    b.set(x, g + 1, z, 'minecraft:short_dry_grass')
                elif n < 0.36 and 'grass_block' in ground:
                    b.set(x, g + 1, z, 'minecraft:short_grass')
                elif n < 0.39:
                    b.set(x, g + 1, z, 'minecraft:tall_dry_grass')
            elif n < 0.08:
                b.set(x, g + 1, z, 'minecraft:short_dry_grass' if n < 0.06 else 'minecraft:dead_bush')

    def biome_columns(self):
        """The columns that get the Stubble's biome: the place, the open country, and the slopes round them."""
        cols = {c for c in self.mask | set(self.soft) if not (c[0] > 150 and c[1] > 402)}
        cols |= {(x, z) for x in range(0, OPEN_X + 1) for z in range(OPEN_Z, 482) if self.lowland(x, z)}
        return cols | {(x, z) for x in range(X1 - 8, TURN + 2) for z in range(ROAD_Z - 3, ROAD_Z + 4)}

    def build(self, height):
        self.ground()
        self.passage(height)
        self.windmill(168, 372)
        self.windmill(84, 424)
        self.windmill(122, 464, 1)
        self.balk()
        self.fields()
        self.watch()
        self.yard()
        self.north_corn()
        self.mill()
        self.granary(height)
        self.fences(height)
        self.country()
        self.crops()

    def views(self):
        """Pictures of the place for the client test: name, eye, what it looks at (plan coordinates)."""
        g = self.g
        gx, gz = GATE
        wx, wz = WATCH
        mx, mz = MILL
        return [
            ('stubble_gate_from_the_yard', (gx + 9, g + 2.6, gz + 3.5), (gx, g + 2.6, gz + 0.5)),
            ('stubble_passage', (gx - 4.5, g + 2.6, gz + 0.5), (TURN, g + 2.6, gz + 0.5)),
            ('stubble_balk', (self.mouth - 1.5, g + 2.6, ROAD_Z + 0.5), (150, g + 3, ROAD_Z - 2)),
            ('stubble_cut_land', (170.5, g + 2.6, ROAD_Z + 0.5), (150, g + 2, ROAD_Z + 8)),
            ('stubble_road_into_the_corn', (152.5, g + 2.6, ROAD_Z + 0.5), (120, g + 2.6, ROAD_Z)),
            ('stubble_in_the_corn', (128.5, g + 2.6, ROAD_Z + 0.5), (104, g + 2.6, ROAD_Z + 2)),
            ('stubble_clearing', (CLEARING[0] + 3.5, g + 2.6, CLEARING[1] + 4.5), (CLEARING[0] - 1, g + 2, CLEARING[1] - 2)),
            ('stubble_fields_from_above', (164, g + 40, ROAD_Z + 30), (124, g + 1, ROAD_Z - 2)),
            ('stubble_open_country', (100.5, g + 2.6, NW_CORN[2] + 3.5), (40, g + 3, 250)),
            ('stubble_fence_and_mill', (112.5, g + 2.6, FENCE_Z - 3.5), (mx, g + 20, mz)),
            ('stubble_country_from_above', (150, g + 70, 470), (50, g, 250)),
            ('stubble_wardens_door', (FIELDS[0] + 6.5, g + 2.6, ROAD_Z + 2.5), (wx + WATCH_R + 4, g + 3, ROAD_Z + 2.5)),
            ('stubble_watched_field', (wx + WATCH_R - 2.5, g + 2.6, ROAD_Z + 2.5), (wx, g + 5, wz)),
            ('stubble_watched_field_from_above', (wx + 20, g + 30, wz + 24), (wx, g + 2, wz)),
            ('stubble_threshing_yard', (wx + 0.5, g + 2.6, YARD[2] + 4.5), (FLOOR[0], g + 2, FLOOR[1])),
            ('stubble_kiln', (KILN[0] + 6.5, g + 2.6, KILN[1] + 0.5), (KILN[0] - 2, g + 2, KILN[1])),
            ('stubble_floor', (FLOOR[0] - 10.5, g + 2.6, FLOOR[1] - 6.5), (FLOOR[0], g + 1.5, FLOOR[1])),
            ('stubble_mill_from_the_floor', (FLOOR[0] + 4.5, g + 2.6, FLOOR[1] - 4.5), (mx, g + 24, mz)),
            ('stubble_mill_door', (mx - MILL_R - 9.5, g + 2.6, mz + 0.5), (mx - MILL_R, g + 6, mz)),
            ('stubble_yard_from_above', (FLOOR[0] - 30, g + 44, FLOOR[1] + 34), (FLOOR[0] + 14, g + 2, FLOOR[1] - 6)),
            ('stubble_in_the_mill', (mx - MILL_R + 2.5, g + 2.6, mz + 0.5), (mx + 4, g + 3, mz + 2)),
            ('stubble_mill_up', (mx - 5.5, g + 2.6, mz - 5.5), (mx + 2, g + 11, mz + 2)),
            ('stubble_mill_from_above', (mx - 44, g + 52, mz - 30), (mx, g + 22, mz)),
            ('stubble_granary', (mx + MILL_R + 2.5, g + 2.6, mz + 0.5), (mx + MILL_R + 11, g + 2.4, mz)),
            ('stubble_north_corn', (DOOR[0] + 0.5, g + 2.6, NORTH_CORN[2] + 3.5), (126, g + 2.4, 414)),
        ]

    def probes(self):
        g = self.g
        return [('stubble_passage', (TURN, g + 1, GATE[1] - 8)), ('stubble_road', (140, g + 1, ROAD_Z)), ('stubble_watch', (WATCH[0] + 6, g + 1, WATCH[1] + 5)),
                ('stubble_mill', (MILL[0] + 9, g + 1, MILL[1] + 2)), ('stubble_granary', (MILL[0] + MILL_R + 4, g + 1, MILL[1] + 2))]
