#!/usr/bin/env python3
"""The Mire: the east end of the Dead Borough, which the bog took, and the bog itself. It is the map's fourth place and
is built into the world's Map by gen_world.py (it has no plan of its own).

The way in is the Mire Gate at the east end of Low Street, which is locked: its key lies in the crack behind the
Stone-Eater. The gate is a tower with a passage that turns twice, and in the turn the hour changes: the town has its
night, the Mire a green dusk that never ends (the green is the biome's: see data/.../worldgen/biome/mire.json and
MapBuilder, which lays that biome over the columns listed in map/biomes.txt).

    gate -> the Drowned End (the street goes under; the houses stand in the water to their sills, to their eaves, to
    their ridges; willows have come up through some of them; a walk of planks runs along what is left) -> the
    ferryman's hut (a hearth and an anvil) -> the Log Roads, across open marsh -> the Weir: the Wallower, in a hand's
    depth of water -> the mill (a hearth) -> the Withy Wood (a door in the ridge opens the short way back to the hut)
    -> the island of the Weeping Mother -> the grove behind her.

The water of the marsh is the game's own, a block deep: whoever wades is slow and cannot roll. The log roads lie level
with it, so one can always climb back out; the plank walk of the Drowned End stands a block over it. The Weir is
floored and covered with the mod's `shallows`, which looks like water and is a floor.

Two basins: the marsh in the west, the wood in the east, and between them a ridge of the plateau that only the Weir
and one door pass. For what comes later: a barred path leaves the grove southwards."""
import math

import gen_village
from gen_map import FLOOR_LANTERN, LANTERN, hsh, stairs
from gen_structures import AIR, MOD

WATER = 'minecraft:water[level=0]'
LEAF = 'minecraft:oak_leaves[persistent=true,distance=1]'
BEARD = 'minecraft:pale_hanging_moss[tip=%s]'
BARK = 'minecraft:dark_oak_log[axis=%s]'
WOOD = 'minecraft:dark_oak_wood[axis=y]'
LOG = 'minecraft:spruce_log[axis=%s]'
PLANK = 'minecraft:spruce_planks'
DECK = 'minecraft:spruce_slab[type=top]'
GLOW = 'minecraft:verdant_froglight[axis=y]'
SHALLOWS = f'{MOD}:shallows'

X0, X1, Z0, Z1 = 276, 470, 472, 618          # the box everything of the Mire lies in
TOWER = (276, 290, 520, 540)                 # the gate tower outside the town's wall
STREET = 525                                 # the line of the drowned street
HUT = (343, 522)                             # the north-west corner of the ferryman's hut
WEIR = (404, 497)
WEIR_R = 13
MILL = (420, 491)
ISLE = (440, 578)
ISLE_R = 12
GROVE = (440, 608)
DOOR = (403, 572)                            # the door in the ridge


def smooth(x, z, scale, salt):
    fx, fz = x / scale, z / scale
    x0, z0 = math.floor(fx), math.floor(fz)
    tx, tz = fx - x0, fz - z0
    tx, tz = tx * tx * (3 - 2 * tx), tz * tz * (3 - 2 * tz)
    a, b_ = hsh(x0, 0, z0, salt), hsh(x0 + 1, 0, z0, salt)
    c, d = hsh(x0, 0, z0 + 1, salt), hsh(x0 + 1, 0, z0 + 1, salt)
    return (a * (1 - tx) + b_ * tx) * (1 - tz) + (c * (1 - tx) + d * tx) * tz


def bottom(x, y, z):
    n = hsh(x, 0, z, 620)
    return 'minecraft:mud' if n < 0.62 else ('minecraft:clay' if n < 0.74 else ('minecraft:gravel' if n < 0.86 else 'minecraft:rooted_dirt'))


def bank(x, y, z):
    n = hsh(x, 0, z, 621)
    patch = smooth(x, z, 7, 622)
    if patch < 0.38:
        return 'minecraft:mud' if n < 0.8 else 'minecraft:rooted_dirt'
    if patch > 0.7:
        return 'minecraft:moss_block' if n < 0.6 else 'minecraft:grass_block'
    return 'minecraft:grass_block' if n < 0.62 else ('minecraft:mud' if n < 0.82 else ('minecraft:podzol' if n < 0.92 else 'minecraft:moss_block'))


def street_bed(x, y, z):
    n = hsh(x, 0, z, 623)
    return 'minecraft:cobblestone' if n < 0.4 else ('minecraft:mossy_cobblestone' if n < 0.66 else ('minecraft:gravel' if n < 0.84 else 'minecraft:mud'))


def weir_floor(x, y, z):
    n = hsh(x, 0, z, 624)
    return 'minecraft:mud_bricks' if n < 0.36 else ('minecraft:packed_mud' if n < 0.6 else ('minecraft:mossy_cobblestone' if n < 0.8 else
                                                                                         ('minecraft:cobblestone' if n < 0.92 else 'minecraft:mossy_stone_bricks')))


def isle_floor(x, y, z):
    n = hsh(x, 0, z, 625)
    return 'minecraft:rooted_dirt' if n < 0.34 else ('minecraft:podzol' if n < 0.58 else ('minecraft:moss_block' if n < 0.8 else 'minecraft:mud'))


class Lot(gen_village.Village):
    """One lot of the drowned quarter: the town's own house builder, set to another level. The lot is tile (1, 1) of a
    grid of its own in which everything round it is open ground, so the house has windows on every side."""
    def __init__(self, b, x, z, gy):
        self.b, self.vx, self.vz, self.gy = b, x - gen_village.T, z - gen_village.T, gy
        self.g = [['.'] * gen_village.COLS for _ in range(gen_village.ROWS)]
        self.kept = set()


class Swamp:
    def __init__(self, b, gy, village):
        self.b, self.g, self.village = b, gy, village
        gx, gz = village.at(*gen_village.MIRE_GATE, 0, 0)
        if (gx, gz) != (273, 534):
            raise ValueError(f'the Mire is laid out from a gate at (273, 534); the town puts it at {(gx, gz)}')
        self.mask = {(x, z) for x in range(X0, X1 + 1) for z in range(Z0, Z1 + 1) if self._in(x, z)}
        self.level = {}
        self.taken = set()               # columns something is built on: nothing is planted there
        self.trees = []
        self.mobs = True

    # ------------------------------------------------------------------ the shape of it

    def _in(self, x, z):
        w = (smooth(x, z, 13, 600) - 0.5) * 0.5 + (smooth(x, z, 5, 603) - 0.5) * 0.2      # the shore wanders
        tx0, tx1, tz0, tz1 = TOWER
        if tx0 <= x <= tx1 and tz0 <= z <= tz1:
            return True
        if 291 <= x <= 342 and 503 - (w + 0.35) * 8 <= z <= 549 + (0.35 - w) * 8:           # the Drowned End
            return True

        def pool(cx, cz, rx, rz):
            return ((x - cx) / rx) ** 2 + ((z - cz) / rz) ** 2 + w < 1.0
        if x <= 398 and (pool(367, 538, 34, 56) or pool(383, 502, 12, 12) or pool(388, 570, 12, 9) or pool(352, 496, 12, 11)):     # the marsh
            return True
        if math.hypot(x - WEIR[0], z - WEIR[1]) <= WEIR_R + 2.6:
            return True
        if 396 <= x <= 422 and DOOR[1] - 1 <= z <= DOOR[1] + 1:  # the cut through the ridge
            return True
        if x >= 411 and z <= ISLE[1] + 3 and (pool(439, 532, 28, 56) or pool(452, 520, 13, 13)):   # the wood
            return True
        if MILL[0] - 3 <= x <= MILL[0] + 15 and MILL[1] - 5 <= z <= MILL[1] + 16:
            return True
        if math.hypot(x - ISLE[0], z - ISLE[1]) <= ISLE_R + 2.8:
            return True
        if GROVE[0] - 1 <= x <= GROVE[0] + 1 and ISLE[1] + ISLE_R <= z <= GROVE[1]:
            return True
        return math.hypot(x - GROVE[0], z - GROVE[1]) <= 7.5

    def inside(self, x, z):
        return (x, z) in self.mask

    def zone(self, x, z):
        return 'end' if x < 338 else ('marsh' if x < 404 else 'wood')

    def lay(self, x, z, top=None):
        """Puts down the column of a place by its level: -2 deep water, -1 water to the knee, 0 ground level with the
        water, 1 a hummock."""
        b, g = self.b, self.g
        lv = self.level[(x, z)]
        if lv == -2:
            b.set(x, g - 2, z, bottom(x, g - 2, z))
            b.set(x, g - 1, z, WATER)
            b.set(x, g, z, WATER)
        elif lv == -1:
            b.set(x, g - 1, z, (top or bottom)(x, g - 1, z))
            b.set(x, g, z, WATER)
        elif lv == 0:
            b.set(x, g - 1, z, 'minecraft:dirt')
            b.set(x, g, z, (top or bank)(x, g, z))
        else:
            b.set(x, g, z, 'minecraft:dirt')
            b.set(x, g + 1, z, (top or bank)(x, g + 1, z))

    def ground(self, x, z, lv, top=None):
        if (x, z) not in self.mask:
            return
        self.level[(x, z)] = lv
        for y in (self.g - 2, self.g - 1, self.g, self.g + 1):
            self.b.cells.pop((x, y, z), None)
        self.lay(x, z, top)

    def disc(self, cx, cz, r, lv, top=None, salt=0, keep=True):
        for x in range(int(cx - r - 3), int(cx + r + 4)):
            for z in range(int(cz - r - 3), int(cz + r + 4)):
                if math.hypot(x - cx, z - cz) <= r + (smooth(x, z, 4, 630 + salt) - 0.5) * 3:
                    self.ground(x, z, lv, top)
                    if keep:
                        self.taken.add((x, z))

    def basin(self):
        for (x, z) in sorted(self.mask):
            n = 0.55 * smooth(x, z, 10, 610) + 0.45 * smooth(x, z, 4, 611)
            zone = self.zone(x, z)
            if zone == 'end':
                lv = -1 if n < 0.74 else 0
            elif zone == 'marsh':
                lv = -2 if n < 0.3 else (-1 if n < 0.62 else (0 if n < 0.9 else 1))
            else:
                lv = -2 if n < 0.22 else (-1 if n < 0.46 else (0 if n < 0.88 else 1))
            edge = any((x + dx, z + dz) not in self.mask for dx, dz in ((1, 0), (-1, 0), (0, 1), (0, -1)))
            if edge:
                lv = max(-1, min(0, lv))
            self.level[(x, z)] = lv
            self.lay(x, z)

    def road(self, points, half=1, salt=0, gaps=0.07):
        """A log road: logs laid across the way, level with the water, a gap here and there where one has sunk."""
        b, g = self.b, self.g
        for (ax, az), (bx, bz) in zip(points, points[1:]):
            steps = max(abs(bx - ax), abs(bz - az), 1)
            across = 'z' if abs(bx - ax) >= abs(bz - az) else 'x'
            for i in range(steps + 1):
                px, pz = round(ax + (bx - ax) * i / steps), round(az + (bz - az) * i / steps)
                for dx in range(-half, half + 1):
                    for dz in range(-half, half + 1):
                        x, z = px + dx, pz + dz
                        if (x, z) not in self.mask or (x, z) in self.taken:
                            continue
                        self.taken.add((x, z))
                        centre = dx == 0 and dz == 0
                        if not centre and hsh(x, 0, z, 640 + salt) < gaps:
                            self.ground(x, z, -1)
                            continue
                        self.ground(x, z, 0, lambda x_, y_, z_: (LOG % across) if hsh(x_, 1, z_, 641) < 0.8 else f'minecraft:stripped_spruce_log[axis={across}]')

    # ------------------------------------------------------------------ the gate

    def gate(self):
        """The tower outside the Mire Gate: the way through it turns north and then east again, and in the leg that
        runs north - from which neither mouth is seen - the hour is turned."""
        b, g = self.b, self.g
        wall = gen_village.town_wall
        x0, x1, z0, z1 = TOWER
        for x in range(x0, x1 + 1):
            for z in range(z0, z1 + 1):
                self.taken.add((x, z))
                self.level[(x, z)] = 0
                b.set(x, g, z, gen_village.cobble(x, g, z))
                b.fill(x, g + 1, z, x, g + 9, z, wall)
                if (x in (x0, x1) or z in (z0, z1)) and (x + z) % 2 == 0:
                    b.set(x, g + 10, z, wall(x, g + 10, z))
        for ax0, az0, ax1, az1 in ((x0, 534, 281, 536), (279, STREET - 1, 281, 536), (279, STREET - 1, x1, STREET + 1)):
            b.fill(ax0, g + 1, az0, ax1, g + 4, az1, AIR)
        for x, z in ((280, 535), (280, STREET)):
            b.set(x, g + 4, z, LANTERN)
        for z in (STREET - 1, STREET + 1):                       # the corners of the arch over the mouth
            b.set(x1, g + 4, z, stairs('stone_brick', 'north' if z < STREET else 'south', 'top'))
        b.threshold('to_night', 279, g + 1, 531, 281, 532)
        b.threshold('to_murk', 279, g + 1, 528, 281, 529)
        b.threshold('mire', 286, g + 1, STREET - 1, 287, STREET + 1)

    # ------------------------------------------------------------------ the Drowned End

    def sunk(self, x0, z0, cols, rows, depth, seed, storeys=1, hollow=False, style=None, tree=False, ruin=0.0):
        """A house of the town that has gone down `depth` blocks into the bog and been left: its doors and its lights
        are gone, its stone is green, its roof has holes or has fallen in at a corner, and a willow may have come up
        through it. Returns its box and the level of its eaves."""
        b, g = self.b, self.g
        lot = Lot(b, x0, z0, g - depth)
        hx0, hz0, hx1, hz1, top = lot.house(1, 1, cols, rows, seed, hollow=hollow, storeys=storeys, style=style)
        for x in range(hx0 - 1, hx1 + 2):
            for z in range(hz0 - 1, hz1 + 2):
                self.taken.add((x, z))
                inner = hx0 < x < hx1 and hz0 < z < hz1
                wall = hx0 <= x <= hx1 and hz0 <= z <= hz1 and not inner
                if hx0 <= x <= hx1 and hz0 <= z <= hz1:
                    self.level[(x, z)] = 0
                    b.set(x, g - depth - 1, z, 'minecraft:mud')
                for y in range(g - depth, top + 9):
                    s = b.get(x, y, z)
                    if s is None or s == AIR:
                        if inner and hollow and y <= g and s == AIR:
                            b.set(x, y, z, WATER)
                        continue
                    if not isinstance(s, str):
                        continue
                    n = hsh(x, y, z, 650 + seed)
                    if 'spruce_door' in s:
                        b.set(x, y, z, PLANK if y > g else 'minecraft:mossy_cobblestone')
                    elif 'orange_stained_glass' in s:
                        b.set(x, y, z, 'minecraft:black_stained_glass' if n < 0.5 or y <= g else AIR)
                    elif 'froglight' in s:
                        b.set(x, y, z, AIR if hollow else 'minecraft:black_wool')
                    elif 'campfire' in s or 'lantern' in s or (s == 'minecraft:spruce_fence' and not wall and not inner):
                        b.set(x, y, z, AIR)
                    elif s == 'minecraft:cobblestone' and n < 0.6:
                        b.set(x, y, z, 'minecraft:mossy_cobblestone')
                    elif s == 'minecraft:stone_bricks' and n < 0.6:
                        b.set(x, y, z, 'minecraft:mossy_stone_bricks')
                    elif s == 'minecraft:stone' and y == top:
                        b.set(x, y, z, PLANK)                      # the floor of the loft, where the roof lets one see in
                    elif y >= top and ('_stairs' in s or '_slab' in s) and ruin > 0 and smooth(x + y, z, 3, 651 + seed) < ruin:
                        b.set(x, y, z, AIR)                        # tiles gone
        # beards of moss under the eaves, a little green on the roof
        for x in range(hx0 - 1, hx1 + 2):
            for z in range(hz0 - 1, hz1 + 2):
                for y in range(top, top + 8):
                    s = b.get(x, y, z)
                    if isinstance(s, str) and ('_stairs' in s or '_slab' in s):
                        n = hsh(x, y, z, 652 + seed)
                        if n < 0.22 and b.free(x, y - 1, z) and y - 1 > g + 1:
                            b.set(x, y - 1, z, BEARD % 'true')
                        if smooth(x, z, 4, 653 + seed) > 0.68 and b.free(x, y + 1, z) and y + 1 >= g + 6:
                            b.set(x, y + 1, z, LEAF)
        if tree:
            cx, cz = (hx0 + hx1) // 2, (hz0 + hz1) // 2
            for y in range(top, top + 8):
                b.cells.pop((cx, y, cz), None)
            self.willow(cx, cz, top, 6 + seed % 3, 4, seed, clear=max(g + 5, top + 3))
        return hx0, hz0, hx1, hz1, top

    def drowned_end(self):
        b, g = self.b, self.g
        b.zone = 'drowned_end'
        zs = STREET
        # the street: dry for a few steps out of the gate, then under
        for x in range(291, 340):
            for z in range(zs - 4, zs + 5):
                dry = x < 296 or (x == 296 and hsh(x, 0, z, 660) < 0.5)
                self.ground(x, z, 0 if dry else -1, gen_village.cobble if dry else street_bed)
                self.taken.add((x, z))
        # the houses, deeper the further from the gate: (x, z, tiles east, tiles south, how deep, seed, storeys, ...)
        self.sunk(294, 512, 3, 3, 1, 11, storeys=2, style=0, ruin=0.12)
        self.sunk(305, 512, 2, 3, 2, 12, storeys=1, style=3, tree=True, ruin=0.3)
        n3 = self.sunk(313, 512, 3, 3, 4, 13, storeys=2, hollow=True, style=1, ruin=0.16)
        self.sunk(324, 509, 3, 4, 3, 14, storeys=2, style=5, ruin=0.34)
        self.sunk(293, 530, 2, 3, 1, 15, storeys=1, style=2, ruin=0.1)
        self.sunk(301, 530, 3, 3, 2, 16, storeys=2, style=4, tree=True, ruin=0.3)
        self.sunk(312, 530, 2, 2, 3, 17, storeys=1, style=1, ruin=0.4)
        s4 = self.sunk(320, 530, 3, 3, 4, 18, storeys=2, hollow=True, style=0, ruin=0.14)
        self.sunk(331, 530, 2, 2, 2, 19, storeys=1, style=3, ruin=0.45)
        self.sunk(299, 502, 2, 2, 2, 20, storeys=1, style=2, ruin=0.3)
        self.sunk(318, 500, 2, 2, 3, 21, storeys=1, style=4, tree=True, ruin=0.3)
        self.sunk(305, 542, 2, 2, 3, 22, storeys=1, style=5, ruin=0.5)
        self.sunk(322, 543, 3, 2, 2, 23, storeys=2, style=1, ruin=0.25)
        # the walk of planks along the north side of the street, a block over the water, on posts
        deck = g + 1
        for x in range(297, 338):
            gap = x in (308, 309, 327, 328)
            for z in (zs - 4, zs - 3):
                if not gap:
                    b.set(x, deck, z, DECK if hsh(x, 2, z, 661) < 0.9 else 'minecraft:dark_oak_slab[type=top]')
                if x % 5 == 0 and z == zs - 4:
                    b.set(x, g, z, LOG % 'y')
        for z in (zs - 4, zs - 3):
            b.set(296, deck, z, stairs('spruce', 'east'))        # up from the cobbles
            self.ground(296, z, 0, gen_village.cobble)
            b.set(338, deck, z, stairs('spruce', 'west'))        # and down to the ferryman's island
            b.flights += [(296, deck, z), (338, deck, z)]
        # into the upper room of the house that stands deepest on that side: a doorway from the walk, another out of
        # its east end onto a stage of planks
        hx0, hz0, hx1, hz1, top = n3
        b.fill(317, g + 2, hz1, 317, g + 3, hz1, AIR)
        b.loot(hx0 + 1, g + 2, hz0 + 1, 'mire_end', 'south')
        b.set(hx1 - 1, g + 2, hz0 + 1, 'minecraft:barrel[facing=up]')
        b.set(hx1 - 1, g + 2, hz0 + 2, 'minecraft:barrel[facing=up]')
        # a bridge of planks over the street to the house across it, and in at its north wall
        hx0, hz0, hx1, hz1, top = s4
        for z in range(zs - 2, hz0):
            if z != zs + 1:
                b.set(324, deck, z, DECK)
        b.set(324, g, zs + 2, LOG % 'y')
        b.fill(324, g + 2, hz0, 324, g + 3, hz0, AIR)
        b.loot(hx1 - 1, g + 2, hz1 - 1, 'mire_loft', 'north')
        b.shard(hx0 + 1, g + 2, hz1 - 1)
        # lamps that still burn, low and green, where the walk begins and ends; what the water left in the street
        for x, z in ((298, zs - 5), (336, zs - 5), (316, zs + 4)):
            if b.free(x, g + 1, z) and b.free(x, g + 2, z):
                b.set(x, g, z, LOG % 'y')
                b.set(x, g + 1, z, 'minecraft:spruce_fence')
                b.set(x, g + 2, z, GLOW)
        for x, z, s in ((302, zs + 1, 'minecraft:barrel[facing=up]'), (311, zs - 1, 'minecraft:spruce_trapdoor[facing=east,half=bottom,open=false]'),
                        (319, zs + 2, 'minecraft:barrel[facing=east]'), (330, zs, 'minecraft:spruce_trapdoor[facing=north,half=bottom,open=false]')):
            b.set(x, g + 1 if 'trapdoor' in s else g, z, s)
        if self.mobs:
            for x, z, f in ((303, zs, 'west'), (313, zs + 2, 'west'), (322, zs - 1, 'west'), (333, zs + 1, 'west')):
                b.set(x, g, z, f'{MOD}:mob_mark[mob=drowned,facing={f},waterlogged=true]')
                b.marks.append(('drowned', x, g, z))
            self.count('drowned', 4)
            b.mob('drowned', 318, g + 2, n3[1] + 4, 'south')
            b.mob('drowned', 322, g + 2, s4[1] + 4, 'north')
            self.fish(310, zs + 3)
            self.fish(329, zs - 1)

    def count(self, kind, n):
        b = self.b
        b.census.setdefault(b.zone, {})
        b.census[b.zone][kind] = b.census[b.zone].get(kind, 0) + n

    def fish(self, x, z, facing='west'):
        """A pike's mark, on the bottom of the water."""
        b, g = self.b, self.g
        y = g - 1 if self.level.get((x, z)) == -2 else g
        if 'water' not in str(b.get(x, y, z)):
            raise ValueError(f'no water for a pike at {(x, z)}: {b.get(x, y, z)}')
        b.set(x, y, z, f'{MOD}:mob_mark[mob=pike,facing={facing},waterlogged=true]')
        b.marks.append(('pike', x, y, z))
        self.count('pike', 1)

    def wade(self, kind, x, z, facing='west'):
        """A mark in water to the knee (or on the ground, if that is what is there)."""
        b, g = self.b, self.g
        if self.level.get((x, z)) == -1 and 'water' in str(b.get(x, g, z)):
            b.set(x, g, z, f'{MOD}:mob_mark[mob={kind},facing={facing},waterlogged=true]')
            b.marks.append((kind, x, g, z))
            self.count(kind, 1)
        else:
            y = g + 1 + max(0, self.level.get((x, z), 0))
            if not b.free(x, y, z):
                raise ValueError(f'no room for {kind} at {(x, y, z)}: {b.get(x, y, z)}')
            b.mob(kind, x, y, z, facing)

    # ------------------------------------------------------------------ the hut, the marsh

    def hut(self):
        """The ferryman's hut on its island at the end of the street: a hearth, an anvil. His boat is still tied up."""
        b, g = self.b, self.g
        b.zone = 'log_roads'
        hx, hz = HUT
        self.disc(hx + 3, hz + 3, 7.5, 0, salt=1)
        lot = Lot(b, hx, hz, g)
        x0, z0, x1, z1, top = lot.house(1, 1, 2, 2, 31, hollow=True, doors=(('w', 3),), storeys=1, style=3)
        for x in range(x0 - 1, x1 + 2):
            for z in range(z0 - 1, z1 + 2):
                self.taken.add((x, z))
        b.hearth(x0 + 3, g + 1, z0 + 2, 'ferry')
        b.set(x1 - 1, g + 1, z1 - 1, f'{MOD}:forge')
        b.set(x1 - 1, g + 1, z0 + 1, 'minecraft:barrel[facing=up]')
        b.threshold('log_roads', x1 + 2, g + 1, z0 - 1, x1 + 3, z1 + 1)

    def marsh(self):
        """The Log Roads: open water and mud, two roads of logs across it - north-east to the Weir, south-east to the
        door in the ridge, which is shut from the other side - and what stands off them on its own islands."""
        b, g = self.b, self.g
        b.zone = 'log_roads'
        hx, hz = HUT[0] + 3, HUT[1] + 3
        wx, wz = WEIR
        north = [(hx + 7, hz - 1), (362, 514), (374, 506), (384, 498), (wx - WEIR_R - 4, wz)]
        south = [(hx + 6, hz + 4), (358, 544), (366, 558), (378, 568), (390, DOOR[1]), (398, DOOR[1])]
        self.road(north, 1, 1)
        self.road(south, 1, 2)
        # the gallows island, between the roads
        self.disc(374, 534, 5.0, 0, salt=2)
        for x in (372, 376):
            b.fill(x, g + 1, 534, x, g + 5, 534, BARK % 'y')
        b.fill(372, g + 6, 534, 376, g + 6, 534, BARK % 'x')
        b.set(374, g + 5, 534, 'minecraft:iron_chain[axis=y]')
        b.loot(374, g + 1, 536, 'mire_gallows', 'north')
        self.road([(366, 534), (369, 534)], 0, 3, gaps=0.0)
        self.road([(364, 522), (366, 534)], 0, 3, gaps=0.3)
        # the fishers' stage in the south: racks, barrels, a net of cobweb gone grey
        self.disc(356, 582, 5.5, 0, salt=3)
        for x in (353, 355, 357):
            b.fill(x, g + 1, 584, x, g + 2, 584, 'minecraft:spruce_fence')
        b.fill(353, g + 3, 584, 357, g + 3, 584, 'minecraft:spruce_trapdoor[facing=north,half=top,open=false]')
        b.barrels(((358, g + 1, 580), (359, g + 1, 581)))
        b.loot(354, g + 1, 580, 'mire_stage', 'east')
        self.road([(359, 560), (357, 576)], 0, 4, gaps=0.25)
        # the cove in the north, where the dead trees stand close: a chest among them
        self.disc(354, 493, 6.0, 0, salt=4, keep=False)
        b.loot(352, g + 1, 491, 'mire_cove', 'south')
        self.taken |= {(352 + dx, 491 + dz) for dx in (-1, 0, 1) for dz in (-1, 0, 1)}
        for x, z, h in ((349, 495, 6), (356, 489, 7), (358, 496, 5), (351, 488, 5), (346, 491, 6)):
            self.snag(x, z, h)
        # the wreck of a barge off the south road, and what was in it
        self.disc(382, 545, 3.2, 0, top=lambda x, y, z: PLANK if hsh(x, 0, z, 662) < 0.8 else 'minecraft:mud', salt=5)
        for x in (379, 385):
            for z in range(543, 548):
                if (x, z) in self.mask and b.free(x, g + 1, z):
                    self.ground(x, z, 0, lambda x_, y_, z_: PLANK)
                    b.set(x, g + 1, z, stairs('spruce', 'east' if x == 379 else 'west', 'bottom'))
                    self.taken.add((x, z))
        b.shard(382, g + 1, 545)
        if self.mobs:
            for x, z in ((360, 517), (377, 502), (388, 505), (362, 551), (384, 566), (371, 580), (350, 566)):
                self.fish(*self.water_near(x, z))
            for x, z in ((370, 509), (387, 499), (369, 561), (356, 579), (375, 536)):
                self.wade('drowned', *self.near(x, z, (-1, 0)), 'west')
            for x, z in ((355, 492), (350, 490), (392, 580)):
                self.wade('lanky', *self.near(x, z, (-1, 0)), 'south')
            for x, z in ((366, 524), (380, 512), (364, 570), (386, 540)):
                self.wisp(x, z)

    def near(self, x, z, levels):
        """The nearest column to (x, z) of one of the given levels with nothing built on it."""
        for r in range(0, 9):
            for dx in range(-r, r + 1):
                for dz in range(-r, r + 1):
                    c = (x + dx, z + dz)
                    if max(abs(dx), abs(dz)) == r and self.level.get(c) in levels and c not in self.taken and c in self.mask \
                            and self.b.free(c[0], self.g + 1, c[1]) and self.b.free(c[0], self.g + 2, c[1]):
                        self.taken.add(c)
                        return c
        raise ValueError(f'no place of level {levels} near {(x, z)}')

    def water_near(self, x, z):
        return self.near(x, z, (-2, -1))

    def wisp(self, x, z):
        """A wisp's mark, on the nearest ground (it flies, but it rises from somewhere)."""
        x, z = self.near(x, z, (0,))
        self.b.mob('wisp', x, self.g + 1, z)

    # ------------------------------------------------------------------ the Weir and the mill

    def weir(self):
        """The Weir: the mill's pond, drained to a hand's depth when the sluice went - a floor of mud brick under a
        film of water, a fence of piles round it. The Wallower lies in it."""
        b, g = self.b, self.g
        b.zone = 'weir'
        cx, cz = WEIR
        for x in range(cx - WEIR_R - 3, cx + WEIR_R + 4):
            for z in range(cz - WEIR_R - 3, cz + WEIR_R + 4):
                d = math.hypot(x - cx, z - cz)
                if d > WEIR_R + 2.6 or (x, z) not in self.mask:
                    continue
                self.taken.add((x, z))
                if d <= WEIR_R:
                    self.ground(x, z, 0, weir_floor)
                    b.set(x, g + 1, z, SHALLOWS)
                else:
                    self.ground(x, z, 0, lambda x_, y_, z_: 'minecraft:mossy_cobblestone')
                    high = g + 5 + (1 if hsh(x, 0, z, 670) < 0.4 else 0)
                    for y in range(g + 1, high + 1):
                        n = hsh(x, y, z, 671)
                        b.set(x, y, z, 'minecraft:mossy_cobblestone' if y == g + 1 and n < 0.6 else (LOG % 'y' if n < 0.72 else BARK % 'y'))
        for side in (-1, 1):                                     # the two ways through the piles: west in, east out
            for x in range(cx + side * (WEIR_R + 1), cx + side * (WEIR_R + 4), side):
                for z in range(cz - 1, cz + 2):
                    if (x, z) in self.mask:
                        self.ground(x, z, 0, weir_floor)
                        b.fill(x, g + 1, z, x, g + 4, z, AIR)
                        b.set(x, g + 5, z, LOG % 'z')
                        self.taken.add((x, z))
        b.fog('wallower_fog', cx - WEIR_R - 2, g + 1, cz - 1, cx - WEIR_R - 2, g + 4, cz + 1, 'z')
        b.fog('wallower', cx + WEIR_R + 2, g + 1, cz - 1, cx + WEIR_R + 2, g + 4, cz + 1, 'z', sealed=True)
        b.threshold('weir', cx - WEIR_R, g + 1, cz - 1, cx - WEIR_R + 1, cz + 1)
        # piles that still stand in the pond, the green lamps of whoever kept it
        for dx, dz in ((-6, -6), (6, -5), (-5, 6), (6, 7)):
            b.fill(cx + dx, g + 1, cz + dz, cx + dx, g + 2 + (dx + dz) % 2, cz + dz, BARK % 'y')
        for a in range(0, 360, 45):
            x, z = round(cx + math.cos(math.radians(a + 22)) * (WEIR_R + 1.2)), round(cz + math.sin(math.radians(a + 22)) * (WEIR_R + 1.2))
            if b.get(x, g + 5, z) not in (None, AIR) and abs(z - cz) > 2:
                b.set(x, g + 4, z, GLOW)
        if self.mobs:
            b.mob('wallower', cx, g + 1, cz, 'west')

    def mill(self):
        """The mill on the east side of the Weir: its wheel fallen in the race, a hearth where the stones were."""
        b, g = self.b, self.g
        b.zone = 'withy_wood'
        mx, mz = MILL
        for x in range(mx - 2, mx + 14):
            for z in range(mz - 4, mz + 14):
                if (x, z) in self.mask and (x, z) not in self.taken:
                    self.ground(x, z, 0 if z >= mz - 1 else -2)
                    self.taken.add((x, z))
        lot = Lot(b, mx, mz, g)
        x0, z0, x1, z1, top = lot.house(1, 1, 4, 4, 41, hollow=True, doors=(('w', WEIR[1] - mz), ('s', 5)), storeys=2, style=2)
        b.fill(x0, g + 1, WEIR[1], x0, g + 2, WEIR[1], AIR)
        b.hearth(x0 + 5, g + 1, z0 + 5, 'mill')
        b.set(x1 - 2, g + 1, z0 + 2, f'{MOD}:forge')
        b.loot(x1 - 1, g + 1, z1 - 2, 'mire_mill', 'west')
        b.barrels(((x0 + 1, g + 1, z0 + 1), (x0 + 2, g + 1, z0 + 1), (x0 + 1, g + 2, z0 + 1)))
        for x, z in ((x0 + 2, z1 - 2), (x1 - 2, z0 + 6)):
            b.set(x, g + 4, z, LANTERN)
        # the wheel, lying against the north wall in the race
        wx, wy, wz = x0 + 6, g + 2, z0 - 2
        for dx in range(-4, 5):
            for dy in range(-4, 5):
                d = math.hypot(dx, dy)
                if 3.2 <= d <= 4.4 and wy + dy >= g - 1 and hsh(dx, dy, 0, 672) < 0.86:
                    b.set(wx + dx, wy + dy, wz, 'minecraft:dark_oak_planks')
                elif (dx == 0 or dy == 0) and 0 < d < 3.2 and wy + dy >= g - 1:
                    b.set(wx + dx, wy + dy, wz, 'minecraft:spruce_fence' if dx == 0 else 'minecraft:spruce_trapdoor[facing=north,half=bottom,open=true]')
        b.fill(wx, wy, wz, wx, wy, z0 - 1, LOG % 'z')
        b.threshold('withy', x0 + 4, g + 1, z1 + 1, x0 + 6, z1 + 2)

    # ------------------------------------------------------------------ the wood, the island, the grove

    def wood(self):
        """The Withy Wood: more ground than water, willows standing close. A road of logs south from the mill to the
        Mother's island; a branch west to the door in the ridge; what is off the road is off it."""
        b, g = self.b, self.g
        b.zone = 'withy_wood'
        mx, mz = MILL
        ix, iz = ISLE
        main = [(mx + 5, mz + 13), (428, 518), (422, 536), (430, 552), (ix, iz - ISLE_R - 4)]
        self.road(main, 1, 6)
        self.road([(422, 536), (414, 556), (413, DOOR[1])], 1, 7)
        # the door in the ridge: iron, shut, its lever on this side
        dx, dz = DOOR
        for x in range(396, 423):
            for z in range(dz - 1, dz + 2):
                self.ground(x, z, 0, lambda x_, y_, z_: (LOG % 'z') if abs(z_ - dz) < 1 or hsh(x_, 0, z_, 680) < 0.6 else 'minecraft:mud')
                self.taken.add((x, z))
        wall = gen_village.town_wall
        for z in range(dz - 1, dz + 2):
            b.fill(dx, g + 1, z, dx, g + 5, z, wall)
        b.door(dx, g + 1, dz, 'west', (dx + 1, g + 2, dz - 1), 'east')
        b.set(dx + 1, g + 3, dz + 1, 'minecraft:spruce_fence')
        b.set(dx + 1, g + 2, dz + 1, LANTERN)
        # the hollow in the east: the stump of a willow as wide as a house, and what somebody hid in it
        self.disc(452, 520, 6.0, 0, salt=8, keep=False)
        for x in range(450, 455):
            for z in range(518, 523):
                edge = x in (450, 454) or z in (518, 522)
                corner = x in (450, 454) and z in (518, 522)
                self.taken.add((x, z))
                if edge and not corner and not (x == 450 and z == 520):
                    b.fill(x, g + 1, z, x, g + 3 + int(hsh(x, 0, z, 681) * 3), z, WOOD)
        b.loot(453, g + 1, 520, 'mire_hollow', 'west')
        self.disc(458, 548, 3.0, 1, salt=9)
        b.shard(458, g + 2, 548)
        if self.mobs:
            for x, z in ((434, 512), (418, 546), (446, 556), (452, 526)):
                self.wade('lanky', *self.near(x, z, (-1, 0)), 'north')
            for x, z in ((426, 526), (416, 562), (438, 544), (456, 540), (447, 512)):
                self.wisp(x, z)
            for x, z in ((424, 544), (432, 558), (444, 530)):
                self.wade('drowned', *self.near(x, z, (-1, 0)), 'north')
            for x, z in ((417, 528), (438, 536), (450, 566)):
                self.fish(*self.water_near(x, z))

    def isle(self):
        """The island of the Weeping Mother: a ring of her own roots as high as a wall, a way in at the north, and -
        while she stands - none out at the south."""
        b, g = self.b, self.g
        b.zone = 'isle'
        cx, cz = ISLE
        for x in range(cx - ISLE_R - 4, cx + ISLE_R + 5):
            for z in range(cz - ISLE_R - 4, cz + ISLE_R + 5):
                d = math.hypot(x - cx, z - cz)
                if d > ISLE_R + 2.8 or (x, z) not in self.mask:
                    continue
                self.taken.add((x, z))
                self.ground(x, z, 0, isle_floor)
                if d > ISLE_R:
                    high = g + 4 + int(smooth(x, z, 3, 690) * 4)
                    for y in range(g + 1, high + 1):
                        b.set(x, y, z, WOOD if hsh(x, y, z, 691) < 0.8 else 'minecraft:mangrove_roots[waterlogged=false]')
                    if hsh(x, 0, z, 692) < 0.5:
                        b.set(x, high + 1, z, LEAF)
        for side in (-1, 1):
            for z in range(cz + side * (ISLE_R + 1), cz + side * (ISLE_R + 4), side):
                for x in range(cx - 1, cx + 2):
                    if (x, z) in self.mask:
                        self.ground(x, z, 0, isle_floor)
                        b.fill(x, g + 1, z, x, g + 4, z, AIR)
                        b.set(x, g + 5, z, BARK % 'x')
                        self.taken.add((x, z))
        b.fog('mother_fog', cx - 1, g + 1, cz - ISLE_R - 2, cx + 1, g + 4, cz - ISLE_R - 2, 'x')
        b.fog('weeping_mother', cx - 1, g + 1, cz + ISLE_R + 2, cx + 1, g + 4, cz + ISLE_R + 2, 'x', sealed=True)
        b.threshold('isle', cx - 1, g + 1, cz - ISLE_R, cx + 1, cz - ISLE_R + 1)
        for a in range(0, 360, 60):
            x, z = round(cx + math.cos(math.radians(a + 30)) * (ISLE_R + 1.3)), round(cz + math.sin(math.radians(a + 30)) * (ISLE_R + 1.3))
            if b.get(x, g + 4, z) not in (None, AIR) and abs(x - cx) > 2:
                b.set(x, g + 3, z, GLOW)
        if self.mobs:
            b.mob('weeping_mother', cx, g + 1, cz, 'north')
        # the neck of land south of her and the grove at the end of it
        gx, gz = GROVE
        for z in range(cz + ISLE_R + 3, gz - 5):
            for x in range(gx - 1, gx + 2):
                self.ground(x, z, 0, isle_floor)
                self.taken.add((x, z))
        self.disc(gx, gz, 6.5, 0, top=isle_floor, salt=10, keep=False)
        b.loot(gx - 3, g + 1, gz + 1, 'mire_grove', 'east')
        b.shard(gx + 3, g + 1, gz + 1)
        self.taken |= {(gx + dx, gz + 1 + dz) for dx in (-4, -3, -2, 2, 3, 4) for dz in (-1, 0, 1)}
        for y in (g + 1, g + 2, g + 3):                         # the way on, barred (a gate nothing in the map opens)
            b.set(gx, y, gz + 6, f'{MOD}:locked_gate[axis=x,lock=none]')
        for x in (gx - 1, gx + 1):
            b.fill(x, g + 1, gz + 6, x, g + 4, gz + 6, WOOD)
        b.set(gx, g + 4, gz + 6, BARK % 'x')
        self.taken |= {(gx + dx, gz + 6 + dz) for dx in (-1, 0, 1) for dz in (-1, 0, 1)}

    # ------------------------------------------------------------------ what grows

    def willow(self, x, z, base, height, radius, salt=0, clear=None):
        """A willow: a dark trunk with a bend in it, a low wide crown, and from the rim of the crown withes hanging -
        leaves, and below the leaves beards of grey moss. `base` is the block it stands on. Nothing of it that can be
        stood on comes lower than `clear` (five over the water): what hangs lower is moss, which is not felt."""
        b, g = self.b, self.g
        clear = g + 5 if clear is None else clear
        bend = ((1, 0), (-1, 0), (0, 1), (0, -1))[int(hsh(x, 3, z, 700 + salt) * 4)]
        cx, cz = x, z
        for h in range(1, height + 1):
            if h == height // 2 + 1:
                b.set(cx, base + h, cz, BARK % ('x' if bend[0] else 'z'))
                cx, cz = cx + bend[0], cz + bend[1]
            b.set(cx, base + h, cz, BARK % 'y')
        top = base + height
        for dx in range(-radius - 1, radius + 2):
            for dz in range(-radius - 1, radius + 2):
                d = math.hypot(dx, dz) + (hsh(cx + dx, 4, cz + dz, 701 + salt) - 0.5) * 1.2
                if d > radius + 0.4:
                    continue
                dome = int(math.sqrt(max(0.0, radius * radius - d * d)) * 0.5)
                for y in range(top - 1, top + dome + 1):
                    if y >= clear and b.free(cx + dx, y, cz + dz):
                        b.set(cx + dx, y, cz + dz, LEAF)
                if d > radius - 1.5:                             # the withes
                    n = hsh(cx + dx, 5, cz + dz, 702 + salt)
                    if n < 0.72:
                        y = top - 2
                        leaves = 1 + int(n * 5) % 3
                        while leaves > 0 and y >= clear and b.free(cx + dx, y, cz + dz):
                            b.set(cx + dx, y, cz + dz, LEAF)
                            y -= 1
                            leaves -= 1
                        strands = 1 + int(n * 17) % 4
                        low = None
                        while strands > 0 and y >= g + 3 and b.free(cx + dx, y, cz + dz) and b.get(cx + dx, y + 1, cz + dz) not in (None, AIR):
                            b.set(cx + dx, y, cz + dz, BEARD % 'false')
                            low = y
                            y -= 1
                            strands -= 1
                        if low is not None:
                            b.set(cx + dx, low, cz + dz, BEARD % 'true')
        self.trees.append((x, z))

    def snag(self, x, z, height):
        """A dead tree standing in the water or the mud: a bare trunk and a bough or two."""
        b, g = self.b, self.g
        if (x, z) not in self.mask or (x, z) in self.taken:
            return
        base = g if self.level[(x, z)] < 0 else g + 1 + max(0, self.level[(x, z)])
        if not all(b.free(x, y, z) or 'water' in str(b.get(x, y, z)) for y in range(base, base + height + 2)):
            return
        if self.level[(x, z)] == -2:
            b.set(x, g - 1, z, BARK % 'y')
        b.fill(x, base, z, x, base + height - 1, z, BARK % 'y')
        for k, (dx, dz) in enumerate(((1, 0), (-1, 0), (0, 1), (0, -1))):
            n = hsh(x + dx, k, z + dz, 705)
            y = base + height - 1 - int(n * 7) % 3
            if n < 0.6 and y >= g + 5 and b.free(x + dx, y, z + dz):
                b.set(x + dx, y, z + dz, BARK % ('x' if dx else 'z'))
        b.set(x, base + height, z, 'minecraft:dark_oak_fence')
        self.taken.add((x, z))

    def plants(self):
        """Willows - few on the open marsh, close in the wood, three or four among the houses - dead trees in the
        water, reeds along the banks, the leaves of lilies, grass and fern on what is dry."""
        b, g = self.b, self.g
        far = {}                                                 # how far every place is from the shore of the basin
        ring = [c for c in self.mask if any((c[0] + dx, c[1] + dz) not in self.mask for dx, dz in ((1, 0), (-1, 0), (0, 1), (0, -1)))]
        for c in ring:
            far[c] = 0
        queue = list(ring)
        while queue:
            nxt = []
            for (x, z) in queue:
                for dx, dz in ((1, 0), (-1, 0), (0, 1), (0, -1)):
                    n = (x + dx, z + dz)
                    if n in self.mask and n not in far:
                        far[n] = far[(x, z)] + 1
                        nxt.append(n)
            queue = nxt
        built = set(self.taken)
        for (x, z) in sorted(self.mask):
            if (x, z) in self.taken:
                continue
            lv = self.level[(x, z)]
            zone = self.zone(x, z)
            n = hsh(x, 6, z, 710)
            near_built = any((x + dx, z + dz) in built for dx in range(-3, 4) for dz in range(-3, 4))
            crowd = 0.05 if zone == 'wood' else (0.012 if zone == 'marsh' else 0.03)
            apart = 9 if zone == 'wood' else 11
            if lv >= -1 and n < crowd and far[(x, z)] >= 7 and not near_built and all(abs(x - tx) + abs(z - tz) >= apart for tx, tz in self.trees) \
                    and all(b.free(x, y, z) for y in range(g + 1 + max(0, lv), g + 14)):
                base = g + max(0, lv)
                if lv == -1:
                    b.set(x, g, z, WOOD)                         # its foot in the water
                    for dx, dz in ((1, 0), (-1, 0), (0, 1), (0, -1)):
                        if hsh(x + dx, 7, z + dz, 711) < 0.5 and self.level.get((x + dx, z + dz)) == -1 and (x + dx, z + dz) not in self.taken:
                            b.set(x + dx, g, z + dz, 'minecraft:mangrove_roots[waterlogged=true]')
                            self.taken.add((x + dx, z + dz))
                self.willow(x, z, base, 8 + int(hsh(x, 8, z, 712) * 3), 4 + int(hsh(x, 9, z, 713) * 2), x * 5 + z)
                self.taken.add((x, z))
                continue
            if lv < 0:
                if n > 0.992 and far[(x, z)] >= 3 and not near_built:
                    self.snag(x, z, 4 + int(hsh(x, 8, z, 714) * 4))
                elif lv == -1 and 0.5 < n < 0.56 and b.free(x, g + 1, z):
                    b.set(x, g + 1, z, 'minecraft:lily_pad')
                continue
            y = g + 1 + lv
            if not b.free(x, y, z):
                continue
            ground = str(b.get(x, y - 1, z))
            wet = any(self.level.get((x + dx, z + dz), 0) < 0 for dx, dz in ((1, 0), (-1, 0), (0, 1), (0, -1)))
            if wet and 0.2 < n < 0.34 and ('mud' in ground or 'grass' in ground or 'dirt' in ground or 'podzol' in ground) and not near_built and lv == 0:
                for h in range(1 + int(n * 50) % 3):
                    b.set(x, y + h, z, 'minecraft:sugar_cane[age=0]')
            elif 'grass_block' in ground or 'moss' in ground or 'podzol' in ground:
                if n < 0.2:
                    b.set(x, y, z, 'minecraft:short_grass')
                elif n < 0.27:
                    b.set(x, y, z, 'minecraft:fern')
                elif n < 0.285 and far[(x, z)] >= 2:
                    b.set(x, y, z, 'minecraft:firefly_bush')
                elif n > 0.95:
                    b.set(x, y, z, 'minecraft:moss_carpet')
            elif 'mud' in ground and n > 0.93:
                b.set(x, y, z, 'minecraft:dead_bush')

    # ------------------------------------------------------------------ all of it

    def build(self):
        self.basin()
        self.gate()
        self.drowned_end()
        self.hut()
        self.marsh()
        self.weir()
        self.mill()
        self.wood()
        self.isle()
        self.plants()

    def biome_columns(self):
        """The columns the Mire's biome is laid over: the basin and a few blocks of the land round it, east of the
        gate tower's turn (in which the hour, the sky and the fog change together)."""
        columns = set()
        for (x, z) in self.mask:
            for dx in range(-12, 13, 4):                         # (the game keeps a biome for every four blocks: this is enough)
                for dz in range(-12, 13, 4):
                    if x + dx >= 282:
                        columns.add((x + dx, z + dz))
        return sorted(columns)

    def views(self):
        """Pictures of the place for the client test: name, eye, what it looks at (plan coordinates)."""
        g = self.g
        wx, wz = WEIR
        ix, iz = ISLE
        return [
            ('mire_gate_from_the_street', (262, g + 2.6, 535.5), (274, g + 3, 535.5)),
            ('mire_out_of_the_gate', (289, g + 2.6, STREET + 0.5), (320, g + 3, STREET + 0.5)),
            ('mire_drowned_end', (298, g + 3.6, STREET - 3), (330, g + 3, STREET + 2)),
            ('mire_drowned_end_from_above', (290, g + 22, STREET + 22), (318, g + 2, STREET)),
            ('mire_street_from_the_water', (306, g + 1.4, STREET + 1.5), (330, g + 4, STREET - 2)),
            ('mire_houses_south', (312, g + 3.6, STREET - 3.5), (318, g + 4, STREET + 12)),
            ('mire_loft', (315, g + 3.6, 518), (319, g + 3, 514)),
            ('mire_hut', (333, g + 3.6, STREET - 3.5), (346, g + 3, STREET)),
            ('mire_log_road_north', (353, g + 2.6, 523.5), (384, g + 4, 498)),
            ('mire_marsh_from_above', (340, g + 34, 560), (372, g + 1, 534)),
            ('mire_gallows', (366, g + 2.6, 531), (374, g + 4, 534.5)),
            ('mire_log_road_south', (352, g + 2.6, 531), (374, g + 3, 566)),
            ('mire_weir_door', (384, g + 2.6, wz + 0.5), (wx, g + 4, wz + 0.5)),
            ('mire_weir', (wx - 10, g + 2.6, wz + 0.5), (wx + 8, g + 3, wz + 0.5)),
            ('mire_weir_from_above', (wx - 14, g + 20, wz + 12), (wx, g + 1, wz)),
            ('mire_mill', (424, g + 2.6, 500), (428, g + 2.4, 494)),
            ('mire_wood', (425.5, g + 2.6, 506), (424, g + 4, 536)),
            ('mire_wood_from_above', (410, g + 36, 560), (436, g + 1, 536)),
            ('mire_ridge_door', (409, g + 2.6, 572.5), (403, g + 2.6, 572.5)),
            ('mire_isle_door', (ix + 0.5, g + 2.6, iz - ISLE_R - 8), (ix + 0.5, g + 4, iz)),
            ('mire_isle', (ix + 0.5, g + 2.6, iz - ISLE_R + 2), (ix + 0.5, g + 5, iz + 4)),
            ('mire_grove', (ix + 0.5, g + 2.6, GROVE[1] - 6), (ix + 0.5, g + 2.6, GROVE[1] + 6)),
        ]

    def probes(self):
        g = self.g
        return [('mire_street', (300, g + 1, STREET)), ('mire_marsh', (362, g + 1, 514)), ('mire_weir', (WEIR[0] + 3, g + 1, WEIR[1] + 3)),
                ('mire_isle', (ISLE[0] + 3, g + 1, ISLE[1] + 3))]
