#!/usr/bin/env python3
"""The Stubble: the map's seventh place, built into the world's Map by gen_world.py (it has no plan of its own).

Three hollows of the plateau west of the town, at an hour of their own: the afternoon of a harvest day, the sun high and
white and no wind at all. The corn of the place - standing, and cut - is grass in the colour its biome gives it
(worldgen/biome/stubble.json). The way in is the Reapers' Gate, a grate in the west cliff of the yard between the road
tunnel and the town's north gate, across from the Wood Wicket; its key hangs in the lamp room of the lighthouse.
Behind the grate a passage turns twice, and in the leg from which neither mouth is seen the hour is turned.

    gate -> the balk (a hearth, an anvil under a lean-to) -> the fields: a cart road west between cut land, where the
    stooks stand, and standing corn, where the reapers are; scarecrows on their stakes, some of them only that -> the
    Watched Field, a round cut field shut in by the rock (the Scorched Warden on its cross; lean-tos of straw the sun
    does not reach under; a seal behind it) -> the threshing yard (a hearth in the drying barn) -> the floor where
    they threshed, the barns, the standing corn north of the mill -> the Great Mill (the Hollow Miller, on the
    grinding floor, with the stone going round the post; a seal behind him) -> the granary (a hearth), and a way on
    that is barred.

A door in the rock between the threshing yard and the fields, opened from the yard's side, is the short way back.

Everything stands on the level floor of the hollows. Nothing in it is a way up: stooks and lean-tos are two blocks and
more, ricks four, the barns' eaves five, and the mills stand on nothing that can be climbed."""
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
FIELDS = (88, 186, 366, 396)                 # the fields before the Warden: x0, x1, z0, z1
CUT_FROM = 150                               # east of this line they are cut; west of it the corn stands
BALK = (178, 380)                            # the hearth by the gate
WATCH = (58, 386)                            # the Watched Field: its middle,
WATCH_R = 17                                 # and how far it reaches
YARD = (24, 130, 418, 470)                   # the land behind the Warden: the threshing yard, and the mill's (its back is to the rock)
NORTH_CORN = (84, 144, 406, 418)             # and the corn that still stands north of the mill
KILN = (48, 428)                             # the drying barn: a hearth
FLOOR = (76, 446)                            # the threshing floor
FLOOR_R = 7
MILL = (116, 438)                            # the Great Mill: the post in the middle of its grinding floor
MILL_R = 13                                  # the outside of its wall; the wall is two thick
MILL_WALL = 17                               # how high the wall stands before the cap begins
DOOR = (100, 401)                            # the short way back: a door in the rock, opened from the yard's side
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


class Stubble:
    def __init__(self, b, gy):
        self.b, self.g = b, gy
        self.mask = {(x, z) for x in range(X0, X1 + 1) for z in range(Z0, Z1 + 1) if self._in(x, z)}
        # how far every column round the place is from it, to ten blocks: the plateau over it is bare, and has its sky
        self.margin = {c: 0 for c in self.mask}
        ring = list(self.mask)
        for far in range(1, 11):
            grown = []
            for (x, z) in ring:
                for n in ((x + 1, z), (x - 1, z), (x, z + 1), (x, z - 1)):
                    if n not in self.margin:
                        self.margin[n] = far
                        grown.append(n)
            ring = grown
        self.taken = set()               # columns something stands on or a way runs over: nothing is planted there
        self.bare = set()                # and columns where the ground is not a field's (a road, a yard, a floor)
        self.mobs = True

    # ------------------------------------------------------------------ the shape of it

    def _in(self, x, z):
        w = (smooth(x, z, 11, 900) - 0.5) * 3.0 + (smooth(x, z, 4, 901) - 0.5) * 1.0       # the foot of the cliff wanders: two blocks either way
        # the ways through the rock are as wide as they are and no wider: fog fills them
        if ROAD_Z + 1 <= z <= ROAD_Z + 3 and WATCH[0] + WATCH_R - 1 <= x <= FIELDS[0] + 3:
            return True                                           # from the fields into the Watched Field
        if WATCH[0] - 1 <= x <= WATCH[0] + 1 and WATCH[1] + WATCH_R - 1 <= z <= YARD[2] + 3:
            return True                                           # and out of it to the threshing yard
        if DOOR[0] - 1 <= x <= DOOR[0] + 1 and FIELDS[3] - 2 <= z <= NORTH_CORN[2] + 2:
            return True                                           # the short way back
        if math.hypot(x - WATCH[0], z - WATCH[1]) <= WATCH_R + w * 0.3:
            return True
        x0, x1, z0, z1 = FIELDS
        if x0 - w <= x <= x1 + w and z0 - w <= z <= z1 + w:
            return True
        x0, x1, z0, z1 = YARD
        if x0 - w <= x <= x1 + w and z0 - w <= z <= z1 + w:
            return True
        x0, x1, z0, z1 = NORTH_CORN
        if x0 - w <= x <= x1 + w and z0 - w <= z <= z1:
            return True
        # the granary behind the mill is a building in the rock: its floor, and no more
        if MILL[0] + MILL_R <= x <= MILL[0] + MILL_R + 12 and MILL[1] - 6 <= z <= MILL[1] + 6:
            return True
        return False

    def inside(self, x, z):
        return (x, z) in self.mask

    def near(self, x, z):
        """Within ten blocks of the place: the plateau over it is bare (see gen_world.woods)."""
        return (x, z) in self.margin

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
        if (x, z) not in self.mask or not (b.free(x, g + 1, z) and b.free(x, g + 2, z)):
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
        if (x, z) not in self.mask or (x, z) in self.taken or not b.free(x, g + 1, z):
            return
        b.fill(x, g + 1, z, x, g + 2, z, HAY % 'y')
        self.keep(x - 1, z - 1, x + 1, z + 1)

    def rick(self, x0, z0, x1, z1, height=4):
        """A rick: straw stacked the length of a field's end and thatched with more of it."""
        b, g = self.b, self.g
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
        if not all(c in self.mask and b.free(c[0], g + 1, c[1]) for c in cells):
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
        """The Watched Field: a round cut field shut in by the rock; in the middle of it the cross, and the Warden on
        it. In from the east, out to the south."""
        b, g = self.b, self.g
        b.zone = 'watch_field'
        cx, cz = WATCH
        for (x, z) in self.mask:
            if self.zone(x, z) == 'watch':
                self.taken.add((x, z))
        # the two ways through the rock: a frame of timber in each, which the fog fills
        xf = cx + WATCH_R + 4
        b.fill(xf, g + 5, ROAD_Z, xf, g + 5, ROAD_Z + 4, LOG % 'z')
        b.fog('scorched_warden_fog', xf, g + 1, ROAD_Z + 1, xf, g + 4, ROAD_Z + 3, 'z')
        for x in range(cx + WATCH_R - 1, FIELDS[0] + 4):
            for z in range(ROAD_Z + 1, ROAD_Z + 4):
                b.set(x, g, z, trodden(x, g, z))
                self.taken.add((x, z))
                self.bare.add((x, z))
        zf = cz + WATCH_R + 5
        b.fill(cx - 2, g + 5, zf, cx + 2, g + 5, zf, LOG % 'x')
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
        """The corn that still stands north of the mill, and the short way back to the fields through the rock."""
        b, g = self.b, self.g
        b.zone = 'threshing'
        dx, dz = DOOR
        # the door: iron, in a frame of the rock, with its lever on the yard's side
        for x in (dx - 1, dx + 1):
            b.fill(x, g + 1, dz, x, g + 3, dz, lining)
        b.fill(dx - 1, g + 3, dz, dx + 1, g + 3, dz, lining)
        b.set(dx, g + 4, dz, lining(dx, g + 4, dz))
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
        """The Great Mill: a round tower of field stone, whitened above, under a cap of boards; four sails that have not
        turned since the wind stopped. The whole of its ground floor is the grinding floor: the post in the middle,
        the track the stone goes round it on, and room either side of the track. In by the west door; the door east,
        to the granary, is sealed until the Miller is down."""
        b, g = self.b, self.g
        b.zone = 'great_mill'
        cx, cz = MILL
        R = MILL_R
        top = g + MILL_WALL
        for dx in range(-R - 1, R + 2):
            for dz in range(-R - 1, R + 2):
                d = math.hypot(dx, dz)
                x, z = cx + dx, cz + dz
                if d > R + 0.5:
                    continue
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
        # the cap: boards, a ring narrower each course
        for k in range(10):
            r = R + 1.0 - k * 1.45
            y = top + 1 + k
            for dx in range(-R - 2, R + 3):
                for dz in range(-R - 2, R + 3):
                    d = math.hypot(dx, dz)
                    if d <= r and (d > r - 2.0 or k == 9):
                        b.set(cx + dx, y, cz + dz, DARK if (dx + dz + k) % 5 else 'minecraft:spruce_wood[axis=y]')
        # the post, from the floor to the cap, and the great beams that held the gear
        b.fill(cx, g + 1, cz, cx, top + 9, cz, BARE % 'y')
        for dx in range(-R + 2, R - 1):
            if dx:
                b.set(cx + dx, g + 9, cz, LOG % 'x')
                b.set(cx, g + 9, cz + dx, LOG % 'z')
                b.set(cx + dx, g + 15, cz, LOG % 'x')
        # light: windows high in the wall, and lamps on posts against it
        for a in range(0, 360, 45):
            ca, sa = math.cos(math.radians(a + 22.5)), math.sin(math.radians(a + 22.5))
            for r in (R - 1, R):
                x, z = round(cx + ca * r), round(cz + sa * r)
                b.fill(x, g + 6, z, x, g + 7, z, AIR)
                b.fill(x, g + 12, z, x, g + 13, z, AIR)
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
        # the sails, on the face over the door: four arms on a windshaft, their cloth still on two of them
        hy = g + 21
        hx = cx - R - 2
        b.fill(cx - R - 1, hy, cz, cx - 8, hy, cz, LOG % 'x')
        b.set(hx, hy, cz, BARE % 'x')
        for sy, sz in ((1, 1), (1, -1), (-1, 1), (-1, -1)):
            for i in range(1, 12):
                b.set(hx, hy + sy * i, cz + sz * i, POLE if i > 1 else DARK)
                if i >= 3 and (sy * sz > 0 or i % 3) and b.free(hx, hy + sy * i, cz + sz * i - sy):
                    b.set(hx, hy + sy * i, cz + sz * i - sy, CLOTH if (sy, sz) in ((1, 1), (-1, -1)) else POLE)
        # the Miller: before the post, his face to the door
        self.mob('hollow_miller', cx - 3, cz, 'west')

    def granary(self, height):
        """Behind the mill, in the rock: bins, a hearth, a chest - and a way on that is barred."""
        b, g = self.b, self.g
        b.zone = 'great_mill'
        cx, cz = MILL
        x0, x1, z0, z1 = cx + MILL_R + 1, cx + MILL_R + 12, cz - 6, cz + 6
        over = max(6, max(height.get((x, z), 0) for x in range(x0 - 2, x1 + 3) for z in (z0 - 2, z1 + 2)))
        for x in range(x0, x1 + 1):
            for z in range(z0, z1 + 1):
                edge = x in (x0, x1) or z in (z0, z1)
                if x == x0 and cz - 1 <= z <= cz + 1:
                    edge = False                                  # the door from the mill
                b.fill(x, g + 6, z, x, g + over - 1, z, 'minecraft:stone')
                b.set(x, g + over, z, 'minecraft:grass_block')
                self.taken.add((x, z))
                self.bare.add((x, z))
                b.set(x, g, z, PLANK)
                if edge:
                    b.fill(x, g + 1, z, x, g + 5, z, lining)
                else:
                    b.fill(x, g + 1, z, x, g + 4, z, AIR)
                    b.set(x, g + 5, z, DARK if (x + z) % 4 else LOG % 'x')
        b.hearth(x0 + 5, g + 1, cz, 'granary')
        b.loot(x0 + 9, g + 1, z0 + 1, 'stubble_mill', 'south')
        for x, z in ((x0 + 2, z0 + 1), (x0 + 3, z0 + 1), (x0 + 2, z1 - 1), (x0 + 6, z1 - 1), (x0 + 7, z1 - 1)):
            b.set(x, g + 1, z, 'minecraft:barrel[facing=up]')
        b.set(x0 + 3, g + 1, z1 - 1, CLOTH)
        b.set(x0 + 4, g + 1, z1 - 1, CLOTH)
        for x, z in ((x0 + 3, cz - 3), (x0 + 8, cz + 3)):
            b.set(x, g + 4, z, LANTERN)
        # the way on, barred (a gate nothing in the map opens): east out of the granary
        for y in (g + 1, g + 2, g + 3):
            b.set(x1, y, cz, f'{MOD}:locked_gate[axis=z,lock=none]')
        b.fill(x1 + 1, g + 1, cz, x1 + 2, g + 3, cz, AIR)       # a little of the way behind it, to be seen through the bars
        b.fill(x1 + 1, g, cz, x1 + 2, g, cz, lining)
        self.barred = (x1, cz)

    def windmill(self, x, z, turn=0):
        """A post mill, such as stand all along the edge of the fields: a box of boards on one great post, under a
        roof of straw, with four sails. None of them turns."""
        b, g = self.b, self.g
        if not all((x + dx, z + dz) in self.mask and b.free(x + dx, g + 1, z + dz) for dx in (-1, 0, 1) for dz in (-1, 0, 1)):
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
        """The columns that get the Stubble's biome: the place and the plateau round it, as far as is seen from in it."""
        return {(x, z) for (x, z) in self.margin if not (x > 150 and z > 402)} | {(x, z) for x in range(X1 - 8, TURN + 2) for z in range(ROAD_Z - 3, ROAD_Z + 4)}

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
            ('stubble_wardens_door', (FIELDS[0] + 6.5, g + 2.6, ROAD_Z + 2.5), (wx + WATCH_R + 4, g + 3, ROAD_Z + 2.5)),
            ('stubble_watched_field', (wx + WATCH_R - 2.5, g + 2.6, ROAD_Z + 2.5), (wx, g + 5, wz)),
            ('stubble_watched_field_from_above', (wx + 20, g + 30, wz + 24), (wx, g + 2, wz)),
            ('stubble_threshing_yard', (wx + 0.5, g + 2.6, YARD[2] + 4.5), (FLOOR[0], g + 2, FLOOR[1])),
            ('stubble_kiln', (KILN[0] + 6.5, g + 2.6, KILN[1] + 0.5), (KILN[0] - 2, g + 2, KILN[1])),
            ('stubble_floor', (FLOOR[0] - 10.5, g + 2.6, FLOOR[1] - 6.5), (FLOOR[0], g + 1.5, FLOOR[1])),
            ('stubble_mill_from_the_floor', (FLOOR[0] + 4.5, g + 2.6, FLOOR[1] - 4.5), (mx, g + 11, mz)),
            ('stubble_mill_door', (mx - MILL_R - 9.5, g + 2.6, mz + 0.5), (mx - MILL_R, g + 6, mz)),
            ('stubble_yard_from_above', (FLOOR[0] - 30, g + 44, FLOOR[1] + 34), (FLOOR[0] + 14, g + 2, FLOOR[1] - 6)),
            ('stubble_in_the_mill', (mx - MILL_R + 2.5, g + 2.6, mz + 0.5), (mx + 4, g + 3, mz + 2)),
            ('stubble_mill_up', (mx - 5.5, g + 2.6, mz - 5.5), (mx + 2, g + 16, mz + 2)),
            ('stubble_mill_from_above', (mx - 30, g + 34, mz - 26), (mx, g + 8, mz)),
            ('stubble_granary', (mx + MILL_R + 2.5, g + 2.6, mz + 0.5), (mx + MILL_R + 11, g + 2.4, mz)),
            ('stubble_north_corn', (DOOR[0] + 0.5, g + 2.6, NORTH_CORN[2] + 3.5), (126, g + 2.4, 414)),
        ]

    def probes(self):
        g = self.g
        return [('stubble_passage', (TURN, g + 1, GATE[1] - 8)), ('stubble_road', (140, g + 1, ROAD_Z)), ('stubble_watch', (WATCH[0] + 6, g + 1, WATCH[1] + 5)),
                ('stubble_mill', (MILL[0] + 9, g + 1, MILL[1] + 2)), ('stubble_granary', (MILL[0] + MILL_R + 4, g + 1, MILL[1] + 2))]
