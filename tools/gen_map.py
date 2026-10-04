#!/usr/bin/env python3
"""The map "Вышний Острог" (the High Stockade), block by block: a fortress on two rocks above the clouds, joined by a
bridge - the lower ward with its cells, the gate hall where the Overseer waits, the upper city with its cloister and
bell tower, and the cathedral where the Chaplain serves. Writes map/high_stockade.plan.txt (set down in an empty world
by MapBuilder.java) and, given a directory, preview pictures. Run from the repo root. Ends with a check of the map as
a place to play: what can be reached from where, and that nothing reachable is a trap.

Coordinates as in gen_structures.py: x east, z south, y up. The way through the map leads north (to the top of the
top-down pictures); the game turns the plan so that north becomes west, into the sunset."""
import os
import sys
from collections import deque

from gen_structures import AIR, COLOURS, MOD, THIN, Build

OUT = 'map/high_stockade.plan.txt'
W, H, D = 129, 126, 272
CX = 64
L0 = 42                         # the floor block of the lower ward (everything below it is rock)
L1, L2 = L0 + 8, L0 + 14        # of the walls and the bridge; of the upper city
LC = L2 + 3                     # the cathedral's floor
GAL = L2 + 12                   # its galleries, and the covered walk that leads to them from the bell tower
CLR = GAL + 7                   # the roof over the galleries
VAULT = CLR + 7                 # the top of the nave's walls
BELL = L2 + 24                  # the bell chamber
SPAWN = (CX, L0 + 1, 263)


def hsh(x, y, z, s=0):
    n = (x * 73856093) ^ (y * 19349663) ^ (z * 83492791) ^ (s * 40503)
    n = ((n ^ (n >> 13)) * 1274126177) & 0xFFFFFFFF
    return ((n ^ (n >> 16)) & 0xFFFF) / 65536.0


# ---------------------------------------------------------------------------------------------------- materials

def grey(x, y, z):
    """The lower fortress: old stone bricks, cracked, mossy near the ground."""
    n = hsh(x, y, z)
    if n < 0.14:
        return 'minecraft:cracked_stone_bricks'
    if n < 0.14 + (0.2 if y <= L0 + 3 else 0.06):
        return 'minecraft:mossy_stone_bricks'
    if n > 0.955:
        return 'minecraft:cobblestone'
    return 'minecraft:stone_bricks'


def pale(x, y, z):
    """The upper city: warm pale stone that takes the colour of the evening."""
    n = hsh(x, y, z, 1)
    if n < 0.72:
        return 'minecraft:end_stone_bricks'
    if n < 0.84:
        return 'minecraft:smooth_sandstone'
    if n < 0.94:
        return 'minecraft:sandstone'
    return 'minecraft:cut_sandstone'


def white(x, y, z):
    """The cathedral: white stone, weathered here and there."""
    n = hsh(x, y, z, 2)
    if n < 0.68:
        return 'minecraft:quartz_bricks'
    if n < 0.86:
        return 'minecraft:calcite'
    return 'minecraft:smooth_quartz'


def yard(x, y, z):
    n = hsh(x, 0, z, 3)
    return 'minecraft:packed_mud' if n < 0.5 else ('minecraft:gravel' if n < 0.72 else ('minecraft:cobblestone' if n < 0.9 else 'minecraft:mossy_cobblestone'))


def cobbles(x, y, z):
    n = hsh(x, 0, z, 4)
    return 'minecraft:cobblestone' if n < 0.55 else ('minecraft:mossy_cobblestone' if n < 0.75 else ('minecraft:stone_bricks' if n < 0.9 else 'minecraft:gravel'))


def flagstones(x, y, z):
    return 'minecraft:stone_bricks' if hsh(x, 0, z, 5) < 0.75 else ('minecraft:cracked_stone_bricks' if hsh(x, 1, z, 5) < 0.6 else 'minecraft:cobblestone')


def plaza(x, y, z):
    """Paving in squares of four with darker joints."""
    if x % 5 == 0 or z % 5 == 0:
        return 'minecraft:polished_andesite' if hsh(x, 0, z, 6) < 0.85 else 'minecraft:andesite'
    return 'minecraft:smooth_stone' if hsh(x // 5, 0, z // 5, 7) < 0.8 else 'minecraft:polished_diorite'


def nave_floor(x, y, z):
    return 'minecraft:polished_deepslate' if (x + z) % 2 else ('minecraft:smooth_quartz' if hsh(x, 0, z, 8) < 0.9 else 'minecraft:calcite')


def aisle_floor(x, y, z):
    return 'minecraft:polished_andesite' if (x // 2 + z // 2) % 2 else 'minecraft:smooth_stone'


def hall_floor(x, y, z):
    return 'minecraft:deepslate_tiles' if (x + z) % 2 else ('minecraft:cracked_deepslate_tiles' if hsh(x, 0, z, 9) < 0.2 else 'minecraft:polished_deepslate')


def rock(x, y, z, depth):
    n = hsh(x, y, z, 10)
    if depth > 14:
        return 'minecraft:deepslate' if n < 0.6 else ('minecraft:tuff' if n < 0.85 else 'minecraft:cobbled_deepslate')
    return 'minecraft:stone' if n < 0.55 else ('minecraft:andesite' if n < 0.78 else ('minecraft:tuff' if n < 0.9 else 'minecraft:cobblestone'))


TILES = 'minecraft:deepslate_tiles'
TILE_STAIRS = 'minecraft:deepslate_tile_stairs'
LANTERN = 'minecraft:lantern[hanging=true]'
SOUL_LANTERN = 'minecraft:soul_lantern[hanging=true]'
FLOOR_LANTERN = 'minecraft:lantern[hanging=false]'
CHAIN = 'minecraft:iron_chain[axis=y]'
BARS = 'minecraft:iron_bars'
GLASS = ('minecraft:orange_stained_glass', 'minecraft:yellow_stained_glass', 'minecraft:red_stained_glass', 'minecraft:white_stained_glass')
OPPOSITE = {'north': 'south', 'south': 'north', 'east': 'west', 'west': 'east'}
STEP = {'north': (0, -1), 'south': (0, 1), 'east': (1, 0), 'west': (-1, 0)}


def stairs(kind, facing, half='bottom'):
    return f'minecraft:{kind}_stairs[facing={facing},half={half}]'


def stair_of(mat):
    """The stairs that go with a wall material."""
    return {'grey': 'stone_brick', 'pale': 'end_stone_brick', 'white': 'quartz', 'dark': 'polished_deepslate'}[mat]


MATS = {'grey': grey, 'pale': pale, 'white': white}


class Map(Build):
    def __init__(self):
        super().__init__('high_stockade', W, H, D)
        self.bridge = set()            # columns that hang in the air: no rock under them
        self.hearths = []
        self.chests = []
        self.doors = []                # (door cells, lever cell): shut until the lever is thrown
        self.seals = {}                # name -> cells
        self.fogs = {}
        self.census = {}               # zone -> {kind: count}
        self.zone = '?'

    # ------------------------------------------------------------------ building blocks

    def clear(self, x0, y0, z0, x1, y1, z1):
        self.fill(x0, y0, z0, x1, y1, z1, AIR)

    def shell(self, x0, z0, x1, z1, y0, y1, mat, thick=1):
        """Four walls, `thick` deep, from y0 to y1."""
        for t in range(thick):
            self.fill(x0 + t, y0, z0 + t, x1 - t, y1, z0 + t, mat)
            self.fill(x0 + t, y0, z1 - t, x1 - t, y1, z1 - t, mat)
            self.fill(x0 + t, y0, z0 + t, x0 + t, y1, z1 - t, mat)
            self.fill(x1 - t, y0, z0 + t, x1 - t, y1, z1 - t, mat)

    def opening(self, x0, y0, z0, x1, y1, z1, mat=None):
        """A doorway or an arch: cleared, and with its upper corners rounded if it is wide enough and `mat` is given."""
        self.clear(x0, y0, z0, x1, y1, z1)
        if mat is None:
            return
        if x1 - x0 >= 2 and x1 - x0 >= z1 - z0:
            for z in range(z0, z1 + 1):
                self.set(x0, y1, z, stairs(stair_of(mat), 'west', 'top'))
                self.set(x1, y1, z, stairs(stair_of(mat), 'east', 'top'))
        elif z1 - z0 >= 2:
            for x in range(x0, x1 + 1):
                self.set(x, y1, z0, stairs(stair_of(mat), 'north', 'top'))
                self.set(x, y1, z1, stairs(stair_of(mat), 'south', 'top'))

    def merlons(self, x0, z0, x1, z1, y, mat, sides='nsew'):
        """A parapet with merlons along the chosen sides of a rectangle: a low wall at y, teeth at y + 1."""
        for x in range(x0, x1 + 1):
            for z in range(z0, z1 + 1):
                if ('n' in sides and z == z0) or ('s' in sides and z == z1) or ('w' in sides and x == x0) or ('e' in sides and x == x1):
                    self.set(x, y, z, mat(x, y, z))
                    if (x + z) % 2 == 0:
                        self.set(x, y + 1, z, mat(x, y + 1, z))

    def steps(self, x0, x1, z0, z1, y, facing, mat, fill=None, head=4):
        """A flight of stairs over the rectangle, rising in direction `facing` from top level y + 1 at the first row.
        Under the steps it is solid down to `fill` (a y) if given. `head` cells above every step are cleared."""
        dx, dz = STEP[facing]
        along = range(z1, z0 - 1, -1) if facing == 'north' else range(z0, z1 + 1) if facing == 'south' else \
            range(x0, x1 + 1) if facing == 'east' else range(x1, x0 - 1, -1)
        for i, a in enumerate(along):
            across = range(x0, x1 + 1) if dz else range(z0, z1 + 1)
            for c in across:
                x, z = (c, a) if dz else (a, c)
                self.set(x, y + 1 + i, z, stairs(stair_of(mat), facing))
                if fill is not None:
                    for yy in range(fill, y + 1 + i):
                        self.set(x, yy, z, MATS[mat](x, yy, z))
                else:
                    self.set(x, y + i, z, MATS[mat](x, y + i, z))
                for yy in range(y + 2 + i, y + 2 + i + head):
                    self.set(x, yy, z, AIR)
        return y + len(along)

    def gable(self, x0, x1, z0, z1, y, ridge='z', end=None, rise=None):
        """A pitched roof of dark tiles over the rectangle, starting at y; the ridge runs along `ridge`.
        `end` (a material) closes the two gable ends below the tiles."""
        if ridge == 'z':
            span = x1 - x0 + 1
            for i in range((span + 1) // 2):
                if rise is not None and i > rise:
                    break
                for z in range(z0, z1 + 1):
                    if x0 + i == x1 - i:
                        self.set(x0 + i, y + i, z, 'minecraft:deepslate_tile_slab[type=bottom]')
                    else:
                        self.set(x0 + i, y + i, z, stairs('deepslate_tile', 'east'))
                        self.set(x1 - i, y + i, z, stairs('deepslate_tile', 'west'))
                if end is not None:
                    for z in (z0, z1):
                        for x in range(x0 + i + 1, x1 - i):
                            self.set(x, y + i, z, end(x, y + i, z))
        else:
            span = z1 - z0 + 1
            for i in range((span + 1) // 2):
                if rise is not None and i > rise:
                    break
                for x in range(x0, x1 + 1):
                    if z0 + i == z1 - i:
                        self.set(x, y + i, z0 + i, 'minecraft:deepslate_tile_slab[type=bottom]')
                    else:
                        self.set(x, y + i, z0 + i, stairs('deepslate_tile', 'south'))
                        self.set(x, y + i, z1 - i, stairs('deepslate_tile', 'north'))
                if end is not None:
                    for x in (x0, x1):
                        for z in range(z0 + i + 1, z1 - i):
                            self.set(x, y + i, z, end(x, y + i, z))

    def spire(self, x0, z0, x1, z1, y, height):
        """A pyramid of tiles narrowing by one every `height / half-width` layers, with a gold tip."""
        half = (x1 - x0 + 1) // 2
        for i in range(height):
            inset = min(half, int(i * half / height))
            xa, xb, za, zb = x0 + inset, x1 - inset, z0 + inset, z1 - inset
            if xa > xb or za > zb:
                break
            for x in range(xa, xb + 1):
                for z in range(za, zb + 1):
                    if x in (xa, xb) or z in (za, zb):
                        self.set(x, y + i, z, TILES)
        mx, mz = (x0 + x1) // 2, (z0 + z1) // 2
        self.fill(mx, y + height, mz, mx, y + height + 1, mz, 'minecraft:gold_block')
        self.set(mx, y + height + 2, mz, 'minecraft:end_rod[facing=up]')

    def pillar(self, x0, z0, x1, z1, y0, y1, body='minecraft:quartz_pillar[axis=y]', cap='minecraft:chiseled_quartz_block'):
        self.fill(x0, y0, z0, x1, y1, z1, body)
        self.fill(x0, y0, z0, x1, y0, z1, cap)
        self.fill(x0, y1, z0, x1, y1, z1, cap)

    def chandelier(self, x, z, ceiling, drop, lantern=LANTERN, wide=True):
        """Hangs from the block at `ceiling`: a chain, and a lantern (four on a cross of bars if `wide`)."""
        self.fill(x, ceiling - drop, z, x, ceiling - 1, z, CHAIN)
        y = ceiling - drop - 1
        if wide:
            self.set(x, y + 1, z, 'minecraft:polished_deepslate_wall')
            for dx, dz in ((1, 0), (-1, 0), (0, 1), (0, -1)):
                self.set(x + dx, y + 1, z + dz, 'minecraft:polished_deepslate_wall')
                self.set(x + dx, y, z + dz, lantern)
        self.set(x, y, z, lantern)

    def window(self, axis, fixed, a0, a1, y0, y1, salt=0, bars=False):
        """A tall window in a wall: stained glass in warm colours with a pointed top, or bars."""
        for a in range(a0, a1 + 1):
            for y in range(y0, y1 + 1):
                if y == y1 and a in (a0, a1) and a1 - a0 >= 2:
                    continue
                x, z = (a, fixed) if axis == 'x' else (fixed, a)
                if bars:
                    self.set(x, y, z, BARS)
                else:
                    n = hsh(a, y, fixed, 20 + salt)
                    self.set(x, y, z, GLASS[0] if n < 0.42 else GLASS[1] if n < 0.74 else GLASS[2] if n < 0.9 else GLASS[3])

    def door(self, x, y, z, facing, lever, lever_facing):
        """An iron door (two blocks from y), shut; `lever` is the cell of the lever that opens it, hanging on the block
        behind it (opposite to `lever_facing`), which must touch the door."""
        self.set(x, y, z, f'minecraft:iron_door[facing={facing},half=lower,hinge=left,open=false,powered=false]')
        self.set(x, y + 1, z, f'minecraft:iron_door[facing={facing},half=upper,hinge=left,open=false,powered=false]')
        self.set(*lever, f'minecraft:lever[face=wall,facing={lever_facing},powered=false]')
        self.doors.append((((x, y, z), (x, y + 1, z)), lever))

    def hearth(self, x, y, z, name):
        self.set(x, y, z, f'{MOD}:hearth[lit=false]')
        self.hearths.append((name, x, y, z))

    def loot(self, x, y, z, table, facing):
        self.chest(x, y, z, f'map/{table}', facing)
        self.chests.append((table, x, y, z))

    def mob(self, kind, x, y, z, facing='south'):
        self.mark(kind, x, y, z, facing)
        self.census.setdefault(self.zone, {})
        self.census[self.zone][kind] = self.census[self.zone].get(kind, 0) + 1

    def fog(self, name, x0, y0, z0, x1, y1, z1, axis, sealed=False):
        state = f'{MOD}:fog_gate[axis={axis},sealed={"true" if sealed else "false"}]'
        self.fill(x0, y0, z0, x1, y1, z1, state)
        cells = [(x, y, z) for x in range(x0, x1 + 1) for y in range(y0, y1 + 1) for z in range(z0, z1 + 1)]
        (self.seals if sealed else self.fogs)[name] = cells

    def barrels(self, cells):
        for x, y, z in cells:
            self.set(x, y, z, 'minecraft:barrel[facing=up]')


# ---------------------------------------------------------------------------------------------------- the lower fortress

def arrival(b):
    """The stub of a bridge that once came from somewhere: the player wakes on it, with the gate ahead."""
    b.zone = 'arrival'
    for z in range(250, 272):
        for x in range(60, 69):
            broken = z >= 266 and hsh(x, 0, z, 30) < (z - 264) * 0.16
            if broken:
                continue
            b.set(x, L0, z, cobbles(x, L0, z) if 61 <= x <= 67 else grey(x, L0, z))
            b.set(x, L0 - 1, z, grey(x, L0 - 1, z))
            if x in (60, 68) and hsh(x, 1, z, 31) < 0.8 and z < 268:
                b.set(x, L0 + 1, z, grey(x, L0 + 1, z))
            b.bridge.add((x, z))
    for z in (254, 262):                                     # two piers going down into the cloud
        for x in range(60, 69):
            for y in range(L0 - 16, L0 - 1):
                taper = (L0 - 2 - y) // 5
                if 60 + taper <= x <= 68 - taper:
                    b.set(x, y, z, grey(x, y, z))
                    b.set(x, y, z + 1, grey(x, y, z + 1))
        for x in range(60, 69):                               # arches under the deck between the piers
            b.set(x, L0 - 2, z - 1, stairs('stone_brick', 'south', 'top'))
            b.set(x, L0 - 2, z + 2, stairs('stone_brick', 'north', 'top'))
    b.set(60, L0 + 2, 252, FLOOR_LANTERN)
    b.set(68, L0 + 2, 258, FLOOR_LANTERN)


def lower_walls(b):
    b.zone = 'ward'
    top = L1                                                  # the walk on top of the walls
    # the ground of the ward
    b.fill(22, L0, 192, 106, L0, 244, yard)
    b.fill(61, L0, 196, 67, L0, 244, cobbles)                # the road from gate to gate
    # curtain walls, three thick, with a walk on top and a toothed parapet on the outside
    for (x0, z0, x1, z1, side) in ((19, 189, 21, 247, 'w'), (107, 189, 109, 247, 'e'), (19, 189, 109, 191, 'n'), (19, 245, 109, 247, 's')):
        b.fill(x0, L0 - 3, z0, x1, top, z1, grey)
        b.merlons(x0, z0, x1, z1, top + 1, grey, side)
    # corner towers, with the walk passing through them
    for (tx, tz) in ((16, 186), (106, 186), (16, 244), (106, 244)):
        b.fill(tx, L0 - 3, tz, tx + 6, top + 5, tz + 6, grey)
        b.merlons(tx, tz, tx + 6, tz + 6, top + 6, grey)
        b.clear(tx + 1, top + 1, tz + 1, tx + 5, top + 3, tz + 5)
        b.set(tx + 3, top + 3, tz + 3, LANTERN)
    # the ways in and out of the tower rooms, along the walks
    for (x0, z0, x1, z1) in ((20, 193, 20, 193), (23, 190, 23, 190), (105, 190, 105, 190), (108, 193, 108, 193),
                             (20, 243, 20, 243), (23, 246, 23, 246), (105, 246, 105, 246), (108, 243, 108, 243)):
        b.clear(min(x0, x1) - (1 if z0 in (190, 246) else 0), top + 1, min(z0, z1) - (1 if x0 in (20, 108) else 0),
                max(x0, x1) + (1 if z0 in (190, 246) else 0), top + 3, max(z0, z1) + (1 if x0 in (20, 108) else 0))


def south_gate(b):
    b.zone = 'gate'
    b.fill(57, L0 - 3, 240, 71, L1 + 4, 249, grey)
    b.merlons(57, 240, 71, 249, L1 + 5, grey)
    b.opening(62, L0 + 1, 240, 66, L0 + 5, 249, 'grey')
    b.fill(62, L0, 240, 66, L0, 249, cobbles)
    b.fill(62, L0 + 4, 248, 66, L0 + 5, 248, BARS)           # the portcullis, drawn up
    b.set(64, L0 + 5, 244, LANTERN)
    # the keeper's room, east of the gate, inside the ward
    b.fill(72, L0, 235, 83, L0 + 5, 244, grey)
    b.clear(73, L0 + 1, 236, 82, L0 + 4, 243)
    b.fill(73, L0, 236, 82, L0, 243, flagstones)
    b.opening(72, L0 + 1, 237, 72, L0 + 3, 238)
    b.hearth(77, L0 + 1, 240, 'keeper')
    b.mob('keykeeper', 81, L0 + 1, 242, 'west')
    b.loot(74, L0 + 1, 243, 'start', 'north')
    b.set(82, L0 + 1, 236, 'minecraft:hay_block[axis=y]')
    b.set(81, L0 + 1, 236, 'minecraft:hay_block[axis=x]')
    b.set(82, L0 + 2, 236, 'minecraft:hay_block[axis=z]')
    b.barrels([(73, L0 + 1, 236), (82, L0 + 1, 243)])
    b.set(77, L0 + 4, 238, LANTERN)
    b.window('z', 83, 239, 240, L0 + 2, L0 + 3, bars=True)


def cell_block(b):
    """West of the ward: a row of cells, and the stair that leads up to the walls."""
    b.zone = 'cells'
    b.fill(22, L0, 200, 34, L1, 232, grey)                   # a solid block; the rooms are cut out of it
    b.clear(23, L0 + 1, 201, 33, L0 + 4, 231)
    b.fill(23, L0, 201, 33, L0, 231, flagstones)
    b.opening(34, L0 + 1, 215, 34, L0 + 3, 216)
    for z0, z1 in ((201, 206), (208, 212), (214, 219)):
        b.fill(28, L0 + 1, z0, 28, L0 + 4, z1, BARS)
        b.clear(28, L0 + 1, (z0 + z1) // 2, 28, L0 + 2, (z0 + z1) // 2)
        b.set(23, L0 + 1, z1, 'minecraft:hay_block[axis=x]')
    for z in (207, 213, 220):
        b.fill(23, L0 + 1, z, 28, L0 + 4, z, grey)
    b.mob('prisoner', 25, L0 + 1, 203, 'east')
    b.mob('prisoner', 25, L0 + 1, 210, 'east')
    b.mob('prisoner', 26, L0 + 1, 217, 'east')
    b.mob('guard', 31, L0 + 1, 206, 'south')
    b.set(24, L0 + 1, 209, 'minecraft:skeleton_skull[rotation=6]')
    b.fill(26, L0 + 3, 211, 26, L0 + 4, 211, CHAIN)
    for z in (203, 211, 218, 226):
        b.set(31, L0 + 4, z, LANTERN)
    # the back room and the stair to the roof
    b.loot(24, L0 + 1, 230, 'cells', 'east')
    b.barrels([(23, L0 + 1, 222), (23, L0 + 1, 223), (24, L0 + 1, 222), (23, L0 + 2, 222)])
    b.steps(32, 33, 221, 228, L0, 'north', 'grey', fill=L0 + 1)
    b.clear(32, L0 + 5, 229, 33, L1 + 3, 230)
    # the roof: a terrace level with the west wall's walk, with a low wall towards the ward
    b.merlons(22, 200, 34, 232, L1 + 1, grey, 'nes')
    b.clear(22, L1 + 1, 200, 22, L1 + 2, 232)
    b.mob('archer', 27, L1 + 1, 206, 'east')
    b.mob('guard', 26, L1 + 1, 222, 'south')


def storehouse(b):
    b.zone = 'ward'
    b.fill(94, L0, 204, 106, L0 + 6, 228, grey)
    b.clear(95, L0 + 1, 205, 105, L0 + 5, 227)
    b.fill(95, L0, 205, 105, L0, 227, flagstones)
    b.opening(94, L0 + 1, 215, 94, L0 + 3, 216)
    b.barrels([(105, L0 + 1, z) for z in (205, 206, 207, 210, 221, 226, 227)] + [(104, L0 + 1, 205), (105, L0 + 2, 205), (105, L0 + 2, 227), (104, L0 + 1, 227)])
    for x, z in ((96, 205), (97, 205), (96, 206), (96, 227), (97, 227)):
        b.set(x, L0 + 1, z, 'minecraft:hay_block[axis=y]')
    b.set(96, L0 + 2, 205, 'minecraft:hay_block[axis=x]')
    b.loot(105, L0 + 1, 216, 'store', 'west')
    b.mob('prisoner', 100, L0 + 1, 209, 'south')
    b.mob('prisoner', 99, L0 + 1, 222, 'north')
    b.mob('guard', 101, L0 + 1, 216, 'west')
    for z in (210, 222):
        b.set(100, L0 + 5, z, LANTERN)
    b.window('z', 94, 209, 210, L0 + 3, L0 + 4, bars=True)
    b.window('z', 94, 221, 222, L0 + 3, L0 + 4, bars=True)


def ward(b):
    """The yard itself and what stands in it."""
    b.zone = 'ward'
    # a dry well
    for x, z in ((49, 213), (50, 213), (51, 213), (49, 215), (50, 215), (51, 215), (49, 214), (51, 214)):
        b.set(x, L0 + 1, z, 'minecraft:cobblestone_wall')
    b.set(50, L0, 214, 'minecraft:cobblestone')
    b.set(50, L0 + 1, 214, 'minecraft:cauldron')
    for x in (49, 51):
        b.fill(x, L0 + 2, 214, x, L0 + 3, 214, 'minecraft:spruce_fence')
    b.fill(49, L0 + 4, 214, 51, L0 + 4, 214, 'minecraft:spruce_slab[type=bottom]')
    # the gallows
    b.fill(77, L0, 222, 82, L0, 224, 'minecraft:spruce_planks')
    for x in (78, 81):
        b.fill(x, L0 + 1, 223, x, L0 + 4, 223, 'minecraft:spruce_fence')
    b.fill(78, L0 + 5, 223, 81, L0 + 5, 223, 'minecraft:spruce_log[axis=x]')
    b.fill(79, L0 + 3, 223, 79, L0 + 4, 223, CHAIN)
    b.set(80, L0 + 4, 223, CHAIN)
    # a cart's worth of barrels, a pile of rubble, braziers along the road
    b.barrels([(44, L0 + 1, 236), (45, L0 + 1, 236), (44, L0 + 1, 237), (44, L0 + 2, 236), (86, L0 + 1, 197), (87, L0 + 1, 197), (87, L0 + 2, 197)])
    for x, y, z in ((40, 1, 198), (41, 1, 198), (40, 1, 199), (41, 2, 198), (39, 1, 199)):
        b.set(x, L0 + y, z, 'minecraft:cobblestone' if hsh(x, y, z, 33) < 0.6 else 'minecraft:mossy_cobblestone')
    for z in (204, 222, 236):
        for x in (59, 69):
            b.set(x, L0 + 1, z, 'minecraft:cobblestone_wall')
            b.set(x, L0 + 2, z, FLOOR_LANTERN)
    b.mob('prisoner', 56, L0 + 1, 230, 'south')
    b.mob('prisoner', 72, L0 + 1, 226, 'south')
    b.mob('prisoner', 46, L0 + 1, 218, 'east')
    b.mob('prisoner', 84, L0 + 1, 208, 'west')
    b.mob('guard', 64, L0 + 1, 212, 'south')
    b.mob('guard', 62, L0 + 1, 199, 'south')
    b.mob('prisoner', 66, L0 + 1, 199, 'south')


def wall_walks(b):
    """Those who keep the walls, and what is hidden in the far tower."""
    b.zone = 'walls'
    b.mob('archer', 20, L1 + 1, 196, 'south')
    b.mob('guard', 38, L1 + 1, 190, 'west')
    b.mob('prisoner', 46, L1 + 1, 190, 'west')
    b.mob('archer', 86, L1 + 1, 190, 'west')
    b.mob('guard', 96, L1 + 1, 190, 'west')
    b.mob('archer', 108, L1 + 1, 214, 'north')
    b.mob('guard', 108, L1 + 1, 232, 'north')
    b.mob('prisoner', 108, L1 + 1, 224, 'north')
    b.loot(110, L1 + 1, 248, 'far_tower', 'west')
    b.mob('archer', 40, L1 + 1, 246, 'east')


def bridge_tower(b):
    """The north gate of the lower fortress. Its ground door is barred from the inside: the first way to it is over the
    walls, and from then on the door stands open."""
    b.zone = 'bridge tower'
    b.fill(55, L0 - 3, 184, 73, L1 + 6, 195, grey)
    b.merlons(55, 184, 73, 195, L1 + 7, grey)
    # the ground floor
    b.clear(57, L0 + 1, 186, 71, L0 + 7, 193)
    b.fill(57, L0, 186, 71, L0, 193, flagstones)
    b.clear(64, L0 + 1, 195, 64, L0 + 2, 195)
    b.door(64, L0 + 1, 194, 'south', (65, L0 + 2, 193), 'north')
    b.loot(58, L0 + 1, 187, 'bridge_tower', 'east')
    b.barrels([(57, L0 + 1, 193), (58, L0 + 1, 193), (57, L0 + 2, 193)])
    b.set(64, L0 + 7, 190, LANTERN)
    b.mob('guard', 61, L0 + 1, 190, 'south')
    # the upper floor, level with the walls and the bridge
    b.clear(57, L1 + 1, 186, 71, L1 + 5, 193)
    b.fill(57, L1, 186, 71, L1, 193, flagstones)
    b.steps(70, 71, 186, 193, L0, 'north', 'grey', fill=L0 + 1, head=5)
    b.clear(70, L0 + 2, 193, 71, L0 + 7, 193)
    b.opening(55, L1 + 1, 189, 56, L1 + 3, 191)
    b.opening(72, L1 + 1, 189, 73, L1 + 3, 191)
    b.opening(62, L1 + 1, 184, 66, L1 + 4, 185, 'grey')
    for x in (58, 69):
        b.set(x, L1 + 5, 189, LANTERN)
    b.mob('guard', 62, L1 + 1, 189, 'north')
    for x in (59, 60, 67, 68):
        b.window('x', 195, x, x, L1 + 2, L1 + 3, bars=True)


def great_bridge(b):
    b.zone = 'bridge'
    for z in range(151, 184):
        for x in range(59, 70):
            b.set(x, L1, z, cobbles(x, L1, z) if 60 <= x <= 68 else grey(x, L1, z))
            b.set(x, L1 - 1, z, grey(x, L1 - 1, z))
            b.bridge.add((x, z))
        for x in (59, 69):
            if hsh(x, 2, z, 34) < 0.86:
                b.set(x, L1 + 1, z, grey(x, L1 + 1, z))
                if z % 6 == 1:
                    b.set(x, L1 + 2, z, 'minecraft:stone_brick_wall')
                    b.set(x, L1 + 3, z, FLOOR_LANTERN)
    for z in (160, 173):
        for x in range(59, 70):
            for y in range(L1 - 22, L1 - 1):
                taper = (L1 - 2 - y) // 6
                if 59 + taper <= x <= 69 - taper:
                    b.fill(x, y, z, x, y, z + 1, grey)
        for x in range(59, 70):
            b.set(x, L1 - 2, z - 1, stairs('stone_brick', 'south', 'top'))
            b.set(x, L1 - 2, z + 2, stairs('stone_brick', 'north', 'top'))
    # what was left on it: a broken cart, and those who hold it
    b.barrels([(61, L1 + 1, 170), (61, L1 + 1, 171), (62, L1 + 1, 171)])
    b.set(67, L1 + 1, 163, 'minecraft:cobblestone_wall')
    b.set(66, L1 + 1, 163, 'minecraft:cobblestone_wall')
    b.set(62, L1 + 1, 156, 'minecraft:cobblestone_wall')
    b.set(61, L1 + 1, 156, 'minecraft:cobblestone_wall')
    b.mob('knight', 64, L1 + 1, 167, 'south')
    b.mob('archer', 61, L1 + 1, 154, 'south')
    b.mob('archer', 67, L1 + 1, 154, 'south')
    b.mob('guard', 64, L1 + 1, 177, 'south')


# ---------------------------------------------------------------------------------------------------- the upper city

def gate_hall(b):
    """The gate of the upper city: one great hall, and the Overseer in it."""
    b.zone = 'gate hall'
    b.fill(44, L1 - 3, 116, 84, L1 + 15, 150, pale)
    b.clear(46, L1 + 1, 118, 82, L1 + 14, 148)
    b.fill(46, L1, 118, 82, L1, 148, hall_floor)
    b.fill(44, L1 + 15, 116, 84, L1 + 15, 150, TILES)
    b.merlons(44, 116, 84, 150, L1 + 16, pale)
    # a base course of darker stone and a band under the roof
    b.shell(44, 116, 84, 150, L1 - 3, L1 + 1, grey, 1)
    b.shell(44, 116, 84, 150, L1 + 13, L1 + 13, lambda x, y, z: 'minecraft:chiseled_sandstone', 1)
    for x in (54, 74):
        for z in (123, 130, 137, 144):
            b.pillar(x, z, x + 1, z + 1, L1 + 1, L1 + 14, 'minecraft:polished_deepslate', 'minecraft:chiseled_deepslate')
    for z in (126, 133, 140):
        b.window('z', 44, z, z + 2, L1 + 6, L1 + 11, 1)
        b.window('z', 84, z, z + 2, L1 + 6, L1 + 11, 2)
    for x, z in ((64, 124), (64, 134), (64, 144), (50, 134), (78, 134)):
        b.chandelier(x, z, L1 + 15, 5)
    # in from the bridge through the fog, out through the seal that lifts when he falls
    b.opening(62, L1 + 1, 149, 66, L1 + 6, 150, 'pale')
    b.fog('overseer', 62, L1 + 1, 150, 66, L1 + 5, 150, 'x')
    b.opening(62, L1 + 1, 116, 66, L1 + 6, 117, 'pale')
    b.fog('overseer', 62, L1 + 1, 117, 66, L1 + 5, 117, 'x', sealed=True)
    # his seat
    b.fill(61, L1 + 1, 119, 67, L1 + 1, 121, 'minecraft:polished_deepslate')
    b.set(64, L1 + 2, 119, stairs('polished_deepslate', 'south'))
    for x in (63, 65):
        b.set(x, L1 + 2, 119, 'minecraft:polished_deepslate_wall')
    for x, z in ((46, 118), (82, 118), (46, 148), (82, 148)):
        b.set(x, L1 + 1, z, 'minecraft:cobweb')
    b.mob('overseer', 64, L1 + 1, 127, 'south')


def upper_plaza(b):
    b.zone = 'plaza'
    b.fill(34, L2 - 3, 77, 94, L2, 112, pale)                # the ground under the square
    b.fill(36, L2, 80, 92, L2, 109, plaza)
    # the walls round it where no building stands
    b.fill(34, L2 + 1, 110, 94, L2 + 8, 112, pale)
    b.merlons(34, 110, 94, 112, L2 + 9, pale, 's')
    b.fill(93, L2 + 1, 77, 94, L2 + 9, 109, pale)
    b.fill(33, L2 + 1, 66, 35, L2 + 9, 112, pale)
    b.fill(36, L2 + 1, 77, 39, L2 + 9, 79, pale)
    b.fill(89, L2 + 1, 77, 92, L2 + 9, 79, pale)
    # a dry fountain in the middle: a ring, and a pillar with a light on it
    for x in range(60, 69):
        for z in range(90, 99):
            d = max(abs(x - 64), abs(z - 94))
            if d == 4 and not (abs(x - 64) == 4 and abs(z - 94) == 4):
                b.set(x, L2 + 1, z, 'minecraft:quartz_slab[type=bottom]')
            elif d <= 3:
                b.set(x, L2, z, 'minecraft:smooth_quartz')
    b.pillar(64, 94, 64, 94, L2 + 1, L2 + 5)
    b.set(64, L2 + 6, 94, 'minecraft:gold_block')
    b.set(64, L2 + 7, 94, 'minecraft:end_rod[facing=up]')
    # lamp posts
    for x, z in ((44, 86), (84, 86), (44, 104), (84, 104)):
        b.fill(x, L2 + 1, z, x, L2 + 3, z, 'minecraft:polished_deepslate_wall')
        b.set(x, L2 + 4, z, FLOOR_LANTERN)
    # the smithy: an awning against the east wall, with the anvil and a hearth
    b.clear(93, L2 + 1, 98, 99, L2 + 4, 106)
    b.fill(93, L2 + 1, 97, 100, L2 + 5, 97, pale)
    b.fill(93, L2 + 1, 107, 100, L2 + 5, 107, pale)
    b.fill(100, L2 + 1, 97, 100, L2 + 5, 107, pale)
    b.fill(93, L2, 98, 99, L2, 106, flagstones)
    b.fill(91, L2 + 5, 97, 100, L2 + 5, 107, 'minecraft:spruce_slab[type=bottom]')
    for z in (97, 107):
        b.fill(91, L2 + 1, z, 91, L2 + 4, z, 'minecraft:spruce_fence')
    b.hearth(96, L2 + 1, 104, 'smithy')
    b.set(97, L2 + 1, 99, f'{MOD}:forge')
    b.loot(99, L2 + 1, 102, 'smithy', 'west')
    b.barrels([(99, L2 + 1, 98), (99, L2 + 1, 106), (98, L2 + 1, 106)])
    b.set(95, L2 + 4, 101, LANTERN)
    # those who stand before the cathedral
    b.mob('guard', 58, L2 + 1, 86, 'south')
    b.mob('guard', 70, L2 + 1, 86, 'south')
    b.mob('acolyte', 64, LC + 1, 80, 'south')


def hall_stair(b):
    """From the gate hall up into the city: built after the square, through its south wall."""
    b.zone = 'plaza'
    b.fill(58, L1 - 3, 113, 70, L2 + 8, 115, pale)
    b.clear(60, L1 + 1, 110, 68, L2 + 6, 115)
    b.steps(60, 68, 110, 115, L1, 'north', 'pale', fill=L1 - 2, head=6)
    for z in (110, 112):
        b.set(60, L2 + 6, z, stairs('end_stone_brick', 'west', 'top'))
        b.set(68, L2 + 6, z, stairs('end_stone_brick', 'east', 'top'))
    b.set(64, L2 + 6, 114, LANTERN)


def cloister(b):
    """West of the square: a walk round a dead garden, the chapter room beyond it, and a passage to the cathedral's side door."""
    b.zone = 'cloister'
    b.fill(9, L2 - 3, 55, 35, L2, 112, pale)
    b.fill(9, L2 + 1, 67, 10, L2 + 7, 111, pale)
    b.fill(9, L2 + 1, 110, 35, L2 + 7, 111, pale)
    b.fill(9, L2 + 1, 67, 35, L2 + 7, 68, pale)
    b.fill(11, L2, 69, 32, L2, 109, aisle_floor)
    b.fill(15, L2, 73, 28, L2, 105, lambda x, y, z: 'minecraft:coarse_dirt' if hsh(x, 0, z, 40) < 0.7 else 'minecraft:gravel')
    # the roof of the walk and the arcade that carries it
    for x in range(11, 33):
        for z in range(69, 110):
            ring = not (16 <= x <= 27 and 74 <= z <= 104)
            if ring:
                b.set(x, L2 + 5, z, 'minecraft:smooth_sandstone')
                edge = x in (15, 28) or z in (73, 105)
                if edge and 15 <= x <= 28 and 73 <= z <= 105:
                    on_pillar = (x in (15, 28) and (z - 73) % 4 == 0) or (z in (73, 105) and (x - 15) % 13 % 4 == 0 and x not in (15, 28)) or \
                                (x in (15, 28) and z in (73, 105))
                    if on_pillar:
                        b.pillar(x, z, x, z, L2 + 1, L2 + 4)
                    else:
                        b.set(x, L2 + 4, z, 'minecraft:quartz_slab[type=top]')
    b.merlons(9, 67, 35, 111, L2 + 8, pale, 'nsw')
    # the garden: a dead tree and a stone bench or two
    for y in range(1, 6):
        b.set(21, L2 + y, 89, 'minecraft:spruce_log[axis=y]')
    for x, y, z, axis in ((20, 4, 89, 'x'), (19, 5, 89, 'x'), (22, 5, 89, 'x'), (21, 4, 88, 'z'), (21, 5, 87, 'z'), (21, 6, 90, 'z'), (23, 6, 89, 'x')):
        b.set(x, L2 + y, z, f'minecraft:spruce_log[axis={axis}]')
    for x, z in ((17, 80), (25, 98), (18, 96), (26, 78), (22, 101)):
        b.set(x, L2 + 1, z, 'minecraft:dead_bush')
    for z in (82, 96):
        b.fill(17, L2 + 1, z, 18, L2 + 1, z, 'minecraft:quartz_slab[type=bottom]')
    # the way in from the square
    b.opening(33, L2 + 1, 93, 35, L2 + 4, 95, 'pale')
    for x, z in ((13, 71), (30, 71), (13, 107), (30, 107), (13, 89), (30, 82)):
        b.set(x, L2 + 4, z, LANTERN)
    b.mob('knight', 21, L2 + 1, 84, 'east')
    b.mob('guard', 30, L2 + 1, 100, 'north')
    b.mob('guard', 13, L2 + 1, 78, 'south')
    b.mob('acolyte', 12, L2 + 1, 98, 'east')
    b.mob('acolyte', 22, L2 + 1, 71, 'south')
    # the chapter room
    b.fill(12, L2 + 1, 56, 30, L2 + 8, 66, pale)
    b.clear(13, L2 + 1, 57, 29, L2 + 6, 66)
    b.fill(13, L2, 57, 29, L2, 66, nave_floor)
    b.fill(12, L2 + 8, 56, 30, L2 + 8, 68, TILES)
    b.opening(21, L2 + 1, 67, 22, L2 + 3, 68)
    b.window('x', 56, 16, 18, L2 + 3, L2 + 6, 3)
    b.window('x', 56, 24, 26, L2 + 3, L2 + 6, 4)
    for x in range(15, 28, 3):
        b.set(x, L2 + 1, 58, stairs('spruce', 'north'))
        b.set(x, L2 + 1, 64, stairs('spruce', 'south'))
    b.fill(19, L2 + 1, 60, 23, L2 + 1, 62, 'minecraft:dark_oak_planks')
    b.set(21, L2 + 2, 61, 'minecraft:candle[candles=3,lit=true]')
    b.chandelier(21, 61, L2 + 7, 1, wide=False)
    b.loot(13, L2 + 1, 57, 'chapter', 'south')
    b.loot(29, L2 + 1, 57, 'chapter_arms', 'south')
    b.mob('acolyte', 17, L2 + 1, 61, 'east')
    b.mob('acolyte', 25, L2 + 1, 61, 'west')
    b.mob('acolyte', 21, L2 + 1, 58, 'south')
    # the passage to the cathedral's west door: three steps up, and a door barred from the far side
    b.fill(36, L2 - 3, 68, 39, LC + 5, 72, pale)
    b.clear(33, L2 + 1, 70, 39, LC + 3, 70)
    b.clear(36, L2 + 1, 69, 39, LC + 3, 71)
    b.steps(37, 39, 69, 71, L2, 'east', 'pale', fill=L2 - 2, head=3)
    b.set(36, LC + 3, 70, LANTERN)


def bell_tower(b):
    """East of the square: two flights to the covered walk that leads to the cathedral's galleries, two more to the bell."""
    b.zone = 'bell tower'
    b.fill(94, L2 - 3, 82, 108, BELL + 6, 96, pale)
    b.clear(96, L2 + 1, 84, 106, BELL + 5, 94)
    b.fill(96, L2, 84, 106, L2, 94, flagstones)
    b.fill(98, L2 + 1, 87, 103, BELL, 91, pale)              # the core the stairs wind round
    b.opening(93, L2 + 1, 89, 95, L2 + 3, 90)
    # four flights: south lane rising east, north lane rising west, twice
    for base in (L2, L2 + 12):
        b.steps(98, 103, 92, 94, base, 'east', 'pale', fill=L2 + 1 if base == L2 else None)
        b.fill(104, base + 6, 84, 106, base + 6, 94, flagstones)        # the east landing
        b.steps(98, 103, 84, 86, base + 6, 'west', 'pale')
        b.fill(96, base + 12, 84, 97, base + 12, 94, flagstones)        # the west landing
    # the bell chamber: a floor over everything but the last flight, open arches with bars, the bell
    for x in range(96, 107):
        for z in range(84, 95):
            if not (98 <= x <= 103 and 84 <= z <= 86):
                b.set(x, BELL, z, flagstones(x, BELL, z))
    for a in (99, 100, 101, 102, 103):
        for (x, z) in ((a, 82), (a, 83), (a, 95), (a, 96)):
            b.clear(x, BELL + 1, z, x, BELL + 4, z)
        for (x, z) in ((94, a - 12), (95, a - 12), (107, a - 12), (108, a - 12)):
            b.clear(x, BELL + 1, z, x, BELL + 4, z)
    for a in (99, 100, 101, 102, 103):
        for (x, z) in ((a, 82), (a, 96), (94, a - 12), (108, a - 12)):
            b.fill(x, BELL + 1, z, x, BELL + 2, z, BARS)
    b.fill(94, BELL + 6, 82, 108, BELL + 6, 96, TILES)
    b.spire(94, 82, 108, 96, BELL + 7, 16)
    b.set(101, BELL + 5, 91, 'minecraft:bell[attachment=ceiling,facing=north]')
    b.set(101, BELL + 5, 87, LANTERN)
    b.loot(105, BELL + 1, 93, 'belfry', 'west')
    b.mob('knight', 101, BELL + 1, 92, 'west')
    # lights and narrow windows on the way up
    for y in (L2 + 4, L2 + 10, L2 + 16, L2 + 22):
        b.set(96, y, 89, 'minecraft:lantern[hanging=false]') if False else None
    for y in (L2 + 8, L2 + 20):
        b.window('z', 108, 88, 89, y, y + 2, bars=True)
        b.window('x', 96, 101, 102, y - 4, y - 2, bars=True)
    for (x, y, z) in ((105, L2 + 5, 89), (96, L2 + 11, 89), (105, L2 + 17, 89), (96, L2 + 23, 89), (97, L2 + 5, 85)):
        b.set(x, y, z, LANTERN)
        b.set(x, y + 1, z, 'minecraft:end_stone_bricks') if b.get(x, y + 1, z) in (None, AIR) else None
    b.loot(101, L2 + 1, 85, 'tower_nook', 'south')
    b.mob('guard', 105, L2 + 7, 89, 'west')
    b.mob('archer', 96, L2 + 13, 92, 'east')
    b.mob('acolyte', 105, L2 + 19, 89, 'west')
    # the covered walk north to the cathedral: out of the tower at the first west landing
    b.zone = 'walk'
    b.opening(96, GAL + 1, 82, 97, GAL + 3, 83)
    for (x0, z0, x1, z1) in ((95, 69, 98, 81), (89, 69, 98, 72)):
        b.fill(x0, GAL, z0, x1, GAL + 4, z1, pale)
    b.clear(96, GAL + 1, 70, 97, GAL + 3, 81)
    b.clear(89, GAL + 1, 70, 97, GAL + 3, 71)
    for z in (73, 76, 79):
        b.window('z', 95, z, z, GAL + 2, GAL + 3, bars=True)
        b.window('z', 98, z, z, GAL + 2, GAL + 3, bars=True)
    for x in (91, 94):
        b.window('x', 69, x, x, GAL + 2, GAL + 3, bars=True)
        b.window('x', 72, x, x, GAL + 2, GAL + 3, bars=True)
    b.gable(95, 98, 69, 81, GAL + 5, 'z')
    b.gable(89, 94, 69, 72, GAL + 5, 'x')
    # the piers that carry it, down to the ground of the yard behind the houses
    for (x, z) in ((95, 78), (98, 78), (95, 72), (98, 72), (98, 69), (92, 69), (92, 72)):
        b.fill(x, L2 + 1, z, x, GAL - 1, z, pale)
    b.set(96, GAL + 3, 75, LANTERN)
    b.mob('archer', 96, GAL + 1, 74, 'south')


def cathedral(b):
    b.zone = 'cathedral'
    X0, X1, Z0, Z1 = 40, 88, 8, 78
    # the ground it stands on, and the shell: two-thick walls to the galleries' roof, the nave's own walls higher
    b.fill(X0, L2 - 3, Z0, X1, LC, Z1, white)
    b.fill(X0, LC + 1, Z0, X1, CLR, Z1, white)
    b.clear(42, LC + 1, 10, 86, CLR, 76)
    b.fill(42, LC, 10, 86, LC, 76, aisle_floor)
    b.fill(52, LC, 10, 76, LC, 76, nave_floor)
    b.fill(63, LC, 35, 65, LC, 76, 'minecraft:red_terracotta')
    # a dark base course outside
    b.shell(X0, Z0, X1, Z1, LC - 2, LC + 1, lambda x, y, z: 'minecraft:polished_andesite', 1)
    # ---- the nave: two rows of pillars, an aisle and a gallery behind each, clerestory walls above
    rows = (72, 66, 60, 54, 48, 42, 36)
    for xa, xb in ((50, 51), (77, 78)):
        b.fill(xa, CLR + 1, 10, xb, VAULT, 76, white)        # the wall above the arcades
        for z in rows:
            b.pillar(xa, z, xb, z + 1, LC + 1, CLR)
        for z in (27, 19):                                    # in the choir the pillars stand free
            b.pillar(xa, z, xb, z + 1, LC + 1, CLR)
        for z in range(13, 74, 6):
            b.window('z', xa if xa == 50 else xb, z, z + 2, CLR + 2, VAULT - 1, z)
            b.window('z', xb if xa == 50 else xa, z, z + 2, CLR + 2, VAULT - 1, z)
    for (xa, xb, edge) in ((42, 51, 51), (77, 86, 77)):
        # the gallery floor (the aisle's ceiling) and its roof
        b.fill(xa, GAL, 35, xb, GAL, 76, white)
        b.fill(xa, GAL, 35, xb, GAL, 76, lambda x, y, z: 'minecraft:smooth_quartz' if (x + z) % 3 else 'minecraft:chiseled_quartz_block')
        b.fill(xa, CLR, 10, xb, CLR, 76, white)
        # a balustrade towards the nave, between the pillars
        for z in range(36, 76):
            if b.get(edge, GAL + 1, z) == AIR:
                b.set(edge, GAL + 1, z, 'minecraft:quartz_slab[type=bottom]' if z % 2 else 'minecraft:chiseled_quartz_block')
        # arches between the pillars, on both storeys
        for z in rows[:-1]:
            for top in (GAL - 1, CLR - 1):
                b.set(edge, top, z - 1, stairs('quartz', 'south', 'top'))
                b.set(edge, top, z - 4, stairs('quartz', 'north', 'top'))
        b.fill(xa, GAL + 1, 35, xb, CLR - 1, 35, white)      # the galleries end in a wall: no way down into the choir
    # the vault: three steps in from each side
    for i, (xa, xb) in enumerate(((52, 76), (54, 74), (57, 71))):
        b.fill(xa, VAULT + 1 + i, 10, xb, VAULT + 1 + i, 76, white)
        if i < 2:
            nx = (54, 74) if i == 0 else (57, 71)
            b.clear(nx[0], VAULT + 1 + i, 10, nx[1], VAULT + 1 + i, 76)
    for z in (70, 58, 46):
        b.chandelier(64, z, VAULT + 3, 9)
    # the roof outside
    b.gable(49, 79, Z0, Z1, VAULT + 1, 'z', end=white)
    for (xa, xb) in ((X0, 48), (80, X1)):
        b.merlons(xa, Z0, xb, Z1, CLR + 1, white, 'nsw' if xa == X0 else 'nse')
    # ---- the west front: the great doors that will not open, a postern beside them, a round window above
    b.fill(61, LC + 1, 77, 67, LC + 9, 78, AIR)
    for x in range(61, 68):
        for y in range(LC + 1, LC + 10):
            if y == LC + 9 and x in (61, 67):
                b.set(x, y, 78, white(x, y, 78))
                b.set(x, y, 77, white(x, y, 77))
                continue
            b.set(x, y, 77, 'minecraft:dark_oak_planks' if x != 64 and y % 4 != 2 else 'minecraft:polished_blackstone')
    b.set(63, LC + 5, 78, 'minecraft:gold_block')
    b.set(65, LC + 5, 78, 'minecraft:gold_block')
    for x in range(56, 73):
        for y in range(LC + 11, LC + 22):
            d = ((x - 64) ** 2 + (y - (LC + 16)) ** 2) ** 0.5
            if d <= 5.2:
                spoke = x == 64 or y == LC + 16 or abs(x - 64) == abs(y - (LC + 16))
                state = 'minecraft:smooth_quartz' if (spoke and d > 1.5) or 4.4 < d else (GLASS[1] if d < 1.6 else GLASS[0] if (x + y) % 2 else GLASS[2])
                b.set(x, y, 77, state)
                b.set(x, y, 78, state if 'glass' in state else AIR if d <= 4.4 else state)
    b.clear(70, LC + 1, 78, 70, LC + 2, 78)
    b.door(70, LC + 1, 77, 'south', (71, LC + 2, 76), 'north')
    # the front's two towers
    for (xa, xb) in ((X0, 50), (78, X1)):
        b.shell(xa, 68, xb, Z1, CLR + 1, CLR + 24, white, 1)
        for y in (CLR + 6, CLR + 15):
            for x in range(xa + 4, xb - 3):
                b.window('x', Z1, x, x, y, y + 4, x)
                b.window('x', 68, x, x, y, y + 4, x + 1)
            for z in range(72, 75):
                b.window('z', xa, z, z, y, y + 4, z)
                b.window('z', xb, z, z, y, y + 4, z + 1)
        b.fill(xa, CLR + 25, 68, xb, CLR + 25, Z1, TILES)
        b.spire(xa, 68, xb, Z1, CLR + 26, 14)
    # steps up from the square
    b.fill(55, L2 + 1, 79, 73, LC, 80, white)
    b.fill(55, L2 + 1, 81, 73, LC - 1, 81, white)
    b.fill(55, L2 + 1, 82, 73, LC - 2, 82, white)
    for x in range(56, 73):
        b.set(x, LC - 0, 81, AIR)
        b.set(x, LC - 1, 82, AIR)
        b.set(x, LC, 81, stairs('quartz', 'north')) if False else None
    b.fill(56, LC, 81, 72, LC, 81, AIR)
    for x in range(56, 73):
        b.set(x, LC, 81, stairs('quartz', 'north'))
        b.set(x, LC - 1, 82, stairs('quartz', 'north'))
        b.set(x, LC - 2, 83, stairs('quartz', 'north'))
        b.set(x, LC - 2, 82, white(x, LC - 2, 82))
    for x in (55, 73):
        b.fill(x, LC + 1, 79, x, LC + 2, 80, white)
        b.set(x, LC + 3, 80, FLOOR_LANTERN)
    # ---- the aisles: side windows, lights, the doors
    for z in range(38, 74, 6):
        b.window('z', X0, z, z + 1, LC + 3, LC + 6, z)
        b.window('z', X0 + 1, z, z + 1, LC + 3, LC + 6, z)
        b.window('z', X1, z, z + 1, LC + 3, LC + 6, z + 1)
        b.window('z', X1 - 1, z, z + 1, LC + 3, LC + 6, z + 1)
        b.window('z', X0, z, z + 1, GAL + 2, GAL + 5, z + 2, bars=True)
        b.window('z', X0 + 1, z, z + 1, GAL + 2, GAL + 5, z + 2)
        b.window('z', X1, z, z + 1, GAL + 2, GAL + 5, z + 3, bars=True)
        b.window('z', X1 - 1, z, z + 1, GAL + 2, GAL + 5, z + 3)
    for z in (40, 52, 64):
        for x in (46, 82):
            b.set(x, GAL - 1, z, LANTERN)
            b.set(x, CLR - 1, z + 3, LANTERN)
    # the west door, from the cloister's passage: barred on this side
    b.clear(40, LC + 1, 70, 40, LC + 2, 70)
    b.door(41, LC + 1, 70, 'west', (42, LC + 2, 69), 'east')
    # the way in from the covered walk, into the east gallery
    b.opening(87, GAL + 1, 70, 88, GAL + 3, 71)
    # stairs from each gallery down into its aisle, against the outer wall
    for xa in (42, 85):
        b.clear(xa, GAL, 50, xa + 1, GAL, 57)
        b.steps(xa, xa + 1, 50, 57, LC, 'south', 'white', fill=LC + 1, head=4)
        rail = xa + 2 if xa == 42 else xa - 1
        for z in range(49, 58):
            b.set(rail, GAL + 1, z, 'minecraft:quartz_slab[type=bottom]')
        b.fill(xa, GAL + 1, 49, xa + 1, GAL + 1, 49, 'minecraft:quartz_slab[type=bottom]')
    # pews, and those who keep the nave
    for z in range(40, 72, 3):
        for x in (54, 55, 56, 57, 58, 59, 60, 68, 69, 70, 71, 72, 73, 74):
            if hsh(x, 0, z, 50) < 0.9:
                b.set(x, LC + 1, z, stairs('spruce', 'south'))
    b.mob('gilded', 64, LC + 1, 62, 'south')
    b.mob('gilded', 64, LC + 1, 44, 'south')
    b.mob('guard', 46, LC + 1, 58, 'south')
    b.mob('guard', 82, LC + 1, 58, 'south')
    b.mob('acolyte', 82, GAL + 1, 62, 'west')
    b.mob('acolyte', 82, GAL + 1, 44, 'west')
    b.mob('acolyte', 46, GAL + 1, 60, 'east')
    b.mob('acolyte', 46, GAL + 1, 42, 'east')
    b.loot(44, GAL + 1, 75, 'west_gallery', 'north')
    # ---- the sacristy off the west aisle: the last hearth
    b.zone = 'sacristy'
    b.fill(28, L2 - 3, 38, 39, LC + 6, 50, white)
    b.clear(30, LC + 1, 40, 39, LC + 5, 48)
    b.fill(30, LC, 40, 39, LC, 48, aisle_floor)
    b.opening(40, LC + 1, 44, 41, LC + 3, 45)
    b.gable(28, 39, 38, 50, LC + 7, 'x', end=white)
    b.hearth(34, LC + 1, 44, 'sacristy')
    b.loot(30, LC + 1, 41, 'sacristy', 'east')
    b.fill(30, LC + 1, 47, 31, LC + 2, 48, 'minecraft:bookshelf')
    b.fill(33, LC + 1, 48, 36, LC + 3, 48, 'minecraft:bookshelf')
    b.set(34, LC + 5, 42, LANTERN)
    b.window('x', 38, 33, 35, LC + 3, LC + 5, 9)
    # ---- the screen before the choir, and the choir itself: the Chaplain's
    b.zone = 'choir'
    b.fill(42, LC + 1, 33, 86, LC + 11, 34, white)
    for x in range(44, 85, 2):
        if not 60 <= x <= 68:
            b.fill(x, LC + 6, 33, x, LC + 9, 34, BARS)
    b.opening(62, LC + 1, 33, 66, LC + 6, 34, 'white')
    b.fog('chaplain', 62, LC + 1, 34, 66, LC + 5, 34, 'x')
    b.fill(63, LC, 10, 65, LC, 34, 'minecraft:red_terracotta')
    b.fill(42, CLR + 1, 10, 49, CLR + 1, 34, white)
    # a dais, the altar on it, candles
    b.fill(54, LC + 1, 10, 74, LC + 1, 14, 'minecraft:polished_blackstone_bricks')
    for x in range(54, 75):
        b.set(x, LC + 1, 15, stairs('polished_blackstone_brick', 'north'))
    b.fill(61, LC + 2, 11, 67, LC + 2, 12, 'minecraft:polished_blackstone')
    b.set(64, LC + 3, 11, 'minecraft:gold_block')
    for x in (61, 67):
        b.set(x, LC + 3, 11, 'minecraft:candle[candles=4,lit=true]')
    # the great window behind the altar: it faces the sunset
    for x in range(53, 76):
        for y in range(LC + 6, VAULT - 1):
            top = VAULT - 2 - abs(x - 64) // 3
            if y > top:
                continue
            mullion = (x - 53) % 4 == 3 or y == LC + 14
            for z in (8, 9):
                if mullion:
                    b.set(x, y, z, 'minecraft:smooth_quartz')
                else:
                    d = ((x - 64) ** 2 + (y - (LC + 15)) ** 2) ** 0.5
                    b.set(x, y, z, GLASS[1] if d < 4 else GLASS[0] if d < 8 else GLASS[2] if d < 11 else GLASS[3])
    for z in (16, 24):
        for x in (56, 72):
            b.chandelier(x, z, VAULT + 1, 12, SOUL_LANTERN, wide=False)
    for x in (45, 83):
        for z in (14, 22, 30):
            b.window('z', X0 if x == 45 else X1, z, z + 1, LC + 4, LC + 10, z + x)
            b.window('z', X0 + 1 if x == 45 else X1 - 1, z, z + 1, LC + 4, LC + 10, z + x)
    b.mob('chaplain', 64, LC + 2, 13, 'south')
    # ---- beyond the seal: the treasury, and a terrace over the edge of the world
    b.zone = 'treasury'
    b.fill(89, L2 - 3, 2, 104, LC + 7, 22, white)
    b.clear(90, LC + 1, 10, 100, LC + 5, 20)
    b.fill(90, LC, 10, 100, LC, 20, nave_floor)
    b.clear(87, LC + 1, 14, 89, LC + 3, 15)
    b.fog('chaplain', 88, LC + 1, 14, 88, LC + 3, 15, 'z', sealed=True)
    b.loot(100, LC + 1, 12, 'treasury', 'west')
    b.loot(100, LC + 1, 18, 'treasury_arms', 'west')
    b.set(95, LC + 5, 15, LANTERN)
    b.fill(96, LC + 1, 20, 99, LC + 1, 20, 'minecraft:gold_block')
    b.gable(89, 104, 9, 22, LC + 8, 'x', end=white)
    b.opening(94, LC + 1, 8, 96, LC + 4, 9, 'white')
    b.clear(90, LC + 1, 3, 103, LC + 7, 7)
    b.fill(90, LC, 3, 103, LC, 7, plaza)
    b.clear(90, LC + 1, 2, 103, LC + 7, 2)
    for x in range(89, 105):
        b.set(x, LC + 1, 2, 'minecraft:quartz_slab[type=bottom]' if x % 2 else 'minecraft:chiseled_quartz_block')
    for z in range(3, 8):
        b.set(104, LC + 1, z, 'minecraft:chiseled_quartz_block')
        b.clear(104, LC + 2, z, 104, LC + 7, z)
        b.set(89, LC + 1, z, 'minecraft:chiseled_quartz_block')
        b.clear(89, LC + 2, z, 89, LC + 7, z)
    b.clear(89, LC + 2, 2, 104, LC + 7, 2)
    b.hearth(97, LC + 1, 5, 'terrace')
    for x in (91, 102):
        b.set(x, LC + 2, 2, FLOOR_LANTERN)


def backdrop(b):
    """What closes the upper city in where the player never goes: houses with their backs to the square."""
    b.zone = 'backdrop'
    # behind the east wall of the square, south of the bell tower, and the yard under the covered walk
    b.fill(95, L2 - 3, 56, 112, L2, 112, pale)
    for (x0, z0, x1, z1, height) in ((101, 98, 111, 111, 9), (99, 58, 111, 67, 11), (103, 70, 111, 80, 8)):
        b.fill(x0, L2 + 1, z0, x1, L2 + height, z1, pale)
        b.gable(x0 - 1, x1 + 1, z0, z1, L2 + height + 1, 'z' if x1 - x0 < z1 - z0 else 'x', end=pale)
        for y in (L2 + 3, L2 + 7):
            if y < L2 + height:
                for z in range(z0 + 2, z1 - 1, 3):
                    b.set(x1, y, z, GLASS[1])
    b.fill(89, L2 + 1, 56, 112, L2 + 9, 57, pale)            # the yard's north wall
    b.fill(111, L2 + 1, 56, 112, L2 + 9, 112, pale)
    b.fill(95, L2 + 1, 97, 112, L2 + 9, 97, pale)
    # west of the cathedral, between the cloister and the sacristy
    b.fill(9, L2 - 3, 36, 39, L2, 55, pale)
    b.fill(9, L2 + 1, 36, 10, L2 + 7, 66, pale)
    b.fill(9, L2 + 1, 36, 39, L2 + 7, 37, pale)
    b.fill(31, L2 + 1, 51, 39, L2 + 7, 67, pale)
    for (x0, z0, x1, z1, height) in ((12, 39, 25, 52, 9),):
        b.fill(x0, L2 + 1, z0, x1, L2 + height, z1, pale)
        b.gable(x0 - 1, x1 + 1, z0, z1, L2 + height + 1, 'x', end=pale)
    # buttresses along the cathedral's sides
    for z in range(13, 76, 6):
        for (xa, xb) in ((38, 39), (89, 90)):
            if b.get(xa, LC + 4, z) is None and b.get(xb, LC + 4, z) is None and b.get(xa, GAL + 2, z) is None:
                b.fill(xa, L2 - 3, z, xb, GAL + 2, z, white)
                b.set(xa if xa == 38 else xb, GAL + 3, z, 'minecraft:quartz_slab[type=bottom]')
                b.fill(xb if xa == 38 else xa, GAL + 3, z, xb if xa == 38 else xa, CLR - 2, z, white)
    b.fill(36, L2 - 3, 8, 104, L2, 60, pale) if False else None


# ---------------------------------------------------------------------------------------------------- the rock

def bedrock(b):
    """Rock under everything that stands (not under what hangs): deepest under the middle of a mass, tapering to its edges,
    so that the fortress sits on two crags."""
    lowest = {}
    for (x, y, z), state in b.cells.items():
        if state != AIR and (x, z) not in b.bridge:
            if y < lowest.get((x, z), 10 ** 6):
                lowest[(x, z)] = y
    # a pier's foot counts as hanging too; only wide masses get rock
    dist = {}
    queue = deque()
    for (x, z) in lowest:
        if any((x + dx, z + dz) not in lowest for dx, dz in ((1, 0), (-1, 0), (0, 1), (0, -1))):
            dist[(x, z)] = 1
            queue.append((x, z))
    while queue:
        x, z = queue.popleft()
        for dx, dz in ((1, 0), (-1, 0), (0, 1), (0, -1)):
            n = (x + dx, z + dz)
            if n in lowest and n not in dist:
                dist[n] = dist[(x, z)] + 1
                queue.append(n)
    count = 0
    for (x, z), y in lowest.items():
        depth = int(min(36, 3 + dist[(x, z)] * 2.4 + hsh(x // 3, 0, z // 3, 60) * 6))
        for i in range(1, depth + 1):
            if y - i < 1:
                break
            b.set(x, y - i, z, rock(x, y - i, z, i))
            count += 1
    return count


# ---------------------------------------------------------------------------------------------------- the check

FROM = {}          # for every place reached, the place it was first reached from: lets a route be traced


def route(target):
    path = [target]
    while path[-1] in FROM and len(path) < 5000:
        path.append(FROM[path[-1]])
    return path[::-1]


PASSABLE = ('mob_mark', 'carpet', 'lantern', 'candle', 'dead_bush', 'cobweb', 'lever', 'end_rod', 'skull', 'chain')


def analyse(b, blocked=frozenset(), quiet=False):
    """Walks the map the way a player can: step, jump one block up, drop any distance, jump a gap of up to three. Reports
    what is reached at each stage of the game, and any place that can be reached but not left towards a hearth."""
    cells = b.cells
    doors = {c for door, lever in b.doors for c in door}
    seals = {name: set(c) for name, c in b.seals.items()}
    all_seals = set().union(*seals.values()) if seals else set()

    def solid(c, open_doors, open_seals):
        if c in blocked:
            return True
        s = cells.get(c)
        if s is None or s == AIR:
            return False
        name = s[2] if isinstance(s, tuple) else s
        if c in doors:
            return c not in open_doors
        if 'fog_gate' in name:
            return c in all_seals and c not in open_seals
        return not any(k in name for k in PASSABLE)

    def stands(c, od, os_):
        x, y, z = c
        below = cells.get((x, y - 1, z))
        if below is None or below == AIR:
            return False
        name = below[2] if isinstance(below, tuple) else below
        if any(k in name for k in ('mob_mark', 'lantern', 'candle', 'dead_bush', 'cobweb', 'lever', 'end_rod', 'chain', 'fog_gate', 'iron_bars', 'ladder')):
            return False
        if (x, y - 1, z) in doors:
            return False
        return not solid(c, od, os_) and not solid((x, y + 1, z), od, os_)

    def neighbours(c, od, os_):
        x, y, z = c
        for dx, dz in ((1, 0), (-1, 0), (0, 1), (0, -1)):
            for reach in (1, 2, 3, 4):
                nx, nz = x + dx * reach, z + dz * reach
                if not (0 <= nx < W and 0 <= nz < D):
                    break
                # the cells flown through must be free at head height
                if reach > 1 and any(solid((x + dx * r, y + h, z + dz * r), od, os_) for r in range(1, reach) for h in (0, 1, 2)):
                    break
                if reach == 1:
                    if stands((nx, y + 1, nz), od, os_) and not solid((x, y + 2, z), od, os_):
                        yield (nx, y + 1, nz)
                if solid((nx, y, nz), od, os_) or solid((nx, y + 1, nz), od, os_):
                    break
                if stands((nx, y, nz), od, os_):
                    yield (nx, y, nz)
                    break
                if reach == 1:
                    for ny in range(y - 1, 0, -1):          # a drop
                        if solid((nx, ny, nz), od, os_):
                            break
                        if stands((nx, ny, nz), od, os_):
                            yield (nx, ny, nz)
                            break
                elif reach > 1 and stands((nx, y - 1, nz), od, os_):
                    yield (nx, y - 1, nz)

    def flood(start, od, os_):
        seen = {start}
        edges = {}
        queue = deque([start])
        while queue:
            c = queue.popleft()
            for n in neighbours(c, od, os_):
                edges.setdefault(n, []).append(c)
                if n not in seen:
                    seen.add(n)
                    FROM.setdefault(n, c)
                    queue.append(n)
        return seen, edges

    def near(seen, x, y, z, r=2):
        return any((x + dx, y + dy, z + dz) in seen for dx in range(-r, r + 1) for dy in (-1, 0, 1) for dz in range(-r, r + 1))

    report = []
    od, os_ = set(), set()
    stages = (('at the start', None), ('after the Overseer', 'overseer'), ('after the Chaplain', 'chaplain'))
    marks = [(k, x, y, z) for k, x, y, z in b.marks]
    problems = []
    final = None
    for label, opened in stages:
        if opened:
            os_ |= seals[opened]
        # throw every lever that can be reached, until no more doors open
        while True:
            seen, edges = flood(SPAWN, od, os_)
            newly = [door for door, lever in b.doors if door[0] not in od and near(seen, *lever, r=1)]
            if not newly:
                break
            for door in newly:
                od |= set(door)
        hearths = [n for n, x, y, z in b.hearths if near(seen, x, y, z)]
        chests = [n for n, x, y, z in b.chests if near(seen, x, y, z)]
        mobs = {}
        for k, x, y, z in marks:
            if (x, y, z) in seen:
                mobs[k] = mobs.get(k, 0) + 1
        doors_open = sum(1 for door, lever in b.doors if door[0] in od)
        report.append(f'{label}: {len(seen)} places, hearths {hearths}, chests {len(chests)}/{len(b.chests)}, doors opened {doors_open}/{len(b.doors)}, '
                      f'enemies in reach {dict(sorted(mobs.items()))}')
        final = (seen, edges, chests)
    seen, edges, chests = final
    for n, x, y, z in b.chests:
        if n not in chests:
            problems.append(f'chest {n} at {(x, y, z)} is never reached')
    for k, x, y, z in marks:
        if (x, y, z) not in seen:
            problems.append(f'{k} at {(x, y, z)} stands where no player comes')
    # traps: places from which no hearth can be reached again
    home = set()
    queue = deque()
    for n, x, y, z in b.hearths:
        for c in seen:
            if abs(c[0] - x) <= 2 and abs(c[2] - z) <= 2 and abs(c[1] - y) <= 1:
                home.add(c)
                queue.append(c)
    while queue:
        c = queue.popleft()
        for p in edges.get(c, ()):
            if p not in home:
                home.add(p)
                queue.append(p)
    traps = seen - home
    if traps:
        sample = sorted(traps)[:: max(1, len(traps) // 12)][:12]
        problems.append(f'{len(traps)} places are traps (no way back to a hearth), e.g. {sample}')
    # the edge: nobody should be able to stand on the bare rock
    on_rock = [c for c in seen if isinstance(cells.get((c[0], c[1] - 1, c[2])), str) and cells[(c[0], c[1] - 1, c[2])] in ROCKS and c[1] < L0]
    if on_rock:
        problems.append(f'{len(on_rock)} places on the bare rock can be reached, e.g. {sorted(on_rock)[:6]}')
    return report, problems, seen


ROCKS = ('minecraft:stone', 'minecraft:andesite', 'minecraft:tuff', 'minecraft:deepslate', 'minecraft:cobbled_deepslate')


def build():
    b = Map()
    arrival(b)
    lower_walls(b)
    south_gate(b)
    cell_block(b)
    storehouse(b)
    bridge_tower(b)
    ward(b)
    wall_walks(b)
    great_bridge(b)
    gate_hall(b)
    upper_plaza(b)
    hall_stair(b)
    cloister(b)
    bell_tower(b)
    cathedral(b)
    backdrop(b)
    return b


def valid_blocks():
    """The names of every block the game has, from the list the build wrote out."""
    path = os.path.expanduser('~/api/resources.txt')
    if not os.path.exists(path):
        return None
    names = set()
    for line in open(path):
        if 'assets/minecraft/blockstates/' in line:
            names.add(line.strip().split('/')[-1][:-5])
    return names


def world(x, y, z):
    """Where a point of the plan is in the finished world (see MapBuilder.java: the plan's origin cell is put at
    0, 200, 0 and the plan is turned a quarter counter-clockwise)."""
    return (z - SPAWN[2] + 0.5, 200 + y - SPAWN[1], -(x - SPAWN[0]) + 0.5)


# pictures the client test takes of the finished world: name, the eye, what it looks at (in the plan's coordinates)
VIEWS = (
    ('first_steps', (64, L0 + 2.6, 264), (64, L0 + 4, 240)),
    ('arrival_from_outside', (36, L0 + 22, 284), (64, L0, 232)),
    ('ward_from_the_gate', (64, L1 + 7, 243), (64, L0, 200)),
    ('keeper_room', (74, L0 + 2.6, 237.5), (80, L0 + 1.5, 242)),
    ('cells', (31, L0 + 2.6, 229), (29, L0 + 2, 204)),
    ('west_wall_walk', (20, L1 + 2.6, 236), (24, L1 + 1, 196)),
    ('north_walk', (28, L1 + 2.6, 190), (56, L1 + 2, 190)),
    ('great_bridge', (64, L1 + 3, 183), (64, L1 + 5, 150)),
    ('bridge_from_the_side', (104, L1 + 16, 186), (64, L1 - 4, 162)),
    ('gate_hall', (64, L1 + 3, 146.5), (64, L1 + 4, 120)),
    ('into_the_city', (64, L2 + 2.6, 107), (64, L2 + 16, 78)),
    ('smithy', (84, L2 + 2.6, 96), (97, L2 + 1.5, 103)),
    ('square_looking_south', (64, LC + 6, 81), (64, L2, 112)),
    ('cloister', (30, L2 + 2.6, 106), (17, L2 + 2, 76)),
    ('chapter_room', (21, L2 + 2.6, 65.5), (21, L2 + 2.5, 57)),
    ('bell_tower_stairs', (96.5, L2 + 2.6, 93), (104, L2 + 6, 88)),
    ('covered_walk', (96.5, GAL + 2.6, 80.5), (96.5, GAL + 2.2, 70)),
    ('east_gallery', (82, GAL + 2.6, 73), (80, GAL + 1.5, 40)),
    ('nave', (64, LC + 3, 75), (64, LC + 10, 34)),
    ('nave_from_the_gallery', (79.5, GAL + 2.6, 64), (52, LC + 3, 48)),
    ('choir', (64, LC + 3, 31), (64, LC + 10, 9)),
    ('sacristy', (38, LC + 2.6, 47), (32, LC + 1.5, 43)),
    ('terrace', (92, LC + 2.6, 6.5), (99, LC + 2.4, -40)),
    ('belfry', (97.5, BELL + 2.6, 93), (104, BELL + 2.4, 87)),
    ('from_the_belfry', (101, BELL + 3, 84.5), (70, L2, 40)),
    ('whole_from_south_east', (170, L2 + 70, 330), (64, L2, 130)),
    ('upper_city_from_south_west', (-30, L2 + 50, 150), (60, L2 + 10, 60)),
    ('cathedral_from_north_east', (140, VAULT + 12, -30), (64, LC + 12, 44)),
    ('great_window_outside', (64, LC + 16, -34), (64, LC + 14, 8)),
    ('lower_fortress_from_the_bridge', (64, L1 + 3, 153), (64, L1 + 3, 190)),
)

EXTRA_COLOURS = [
    ('end_stone', (222, 214, 160)), ('sandstone', (216, 202, 152)), ('quartz', (236, 230, 222)), ('calcite', (224, 224, 220)),
    ('packed_mud', (140, 104, 78)), ('polished_diorite', (196, 196, 198)), ('smooth_stone', (160, 160, 160)), ('terracotta', (142, 60, 46)),
    ('gold_block', (246, 208, 62)), ('stained_glass', (236, 150, 56)), ('bookshelf', (118, 92, 56)), ('dark_oak', (66, 44, 22)),
    ('forge', (90, 90, 100)), ('iron_door', (200, 200, 205)), ('lever', (255, 60, 255)), ('end_rod', (255, 250, 220)), ('bell', (250, 210, 70)),
]

if __name__ == '__main__':
    COLOURS[0:0] = EXTRA_COLOURS
    b = build()
    built = len([1 for s in b.cells.values() if s != AIR])
    rocks = bedrock(b)
    known = valid_blocks()
    if known is not None:
        used = {(s[2] if isinstance(s, tuple) else s).split('[')[0] for s in b.cells.values()}
        unknown = sorted(n for n in used if n.startswith('minecraft:') and n.split(':')[1] not in known)
        if unknown:
            print('UNKNOWN BLOCKS:', unknown)
    report, problems, seen = analyse(b)
    states = b.save(SPAWN, OUT, skip_air=True)
    with open('map/views.txt', 'w') as f:
        for name, eye, look in VIEWS:
            f.write(name + ' ' + ' '.join(f'{v:.2f}' for v in world(*eye) + world(*look)) + '\n')
    print(f'{b.name}: {b.w}x{b.h}x{b.d}, {built} blocks built, {rocks} of rock, {states} block states')
    total = {}
    for zone, kinds in b.census.items():
        print(f'  {zone}: {kinds}')
        for k, n in kinds.items():
            total[k] = total.get(k, 0) + n
    print('  all:', total)
    for line in report:
        print(' ', line)
    for p in problems:
        print('  PROBLEM:', p)
    # the cathedral must be entered by the covered walk the first time: with the walk shut, nothing beyond it is reached
    shut = frozenset((x, y, 76) for x in (96, 97) for y in range(GAL + 1, GAL + 4))
    without, _, seen_shut = analyse(b, blocked=shut)
    inside = (CX, LC + 1, 60) in seen_shut
    print('  with the covered walk shut the nave is ' + ('REACHED: there is another way in. PROBLEM' if inside else 'not reached: good'))
    if len(sys.argv) > 1:
        out = sys.argv[1]
        os.makedirs(out, exist_ok=True)
        b.iso(scale=5).save(f'{out}/map_iso.png')
        from PIL import ImageDraw
        for name, top, low in (('ward', L0 + 4, L0), ('walls', L1 + 4, L1), ('city', L2 + 4, L2), ('cathedral', LC + 6, LC), ('galleries', GAL + 4, GAL),
                               ('belfry', BELL + 4, BELL - 8)):
            img = b.top_down(top, scale=6)
            draw = ImageDraw.Draw(img)
            for (x, y, z) in seen:                         # a green dot wherever a player can stand on this storey
                if low <= y <= top + 1:
                    draw.rectangle([x * 6 + 2, z * 6 + 2, x * 6 + 3, z * 6 + 3], fill=(60, 255, 90))
            img.save(f'{out}/map_{name}.png')
