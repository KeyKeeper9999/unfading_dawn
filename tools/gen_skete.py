#!/usr/bin/env python3
"""The Black Firwood and the skete of the Unsleeping in it: the map's fifth place, built into the world's Map by
gen_world.py (it has no plan of its own).

A hollow of the plateau north-east of the town, full of old firs, at an hour of its own: the one before a dawn that
does not come. The way in is the Wood Wicket, a grate in the east cliff of the yard between the road tunnel and the
town's north gate; its key lies in the grove behind the Weeping Mother. Behind the grate a passage turns twice, and in
the leg from which neither mouth is seen the hour is turned.

    wicket -> the old fir (a hearth) -> the Firwood: a track among the trunks, moths that are taken for rags, sleepers
    walking with their stakes on their backs; a glade of stakes to the west, a wayside shrine to the east where two
    kneel and a waker walks -> the Shatun's Hollow (the Shatun; a seal behind him) -> the skete's edge (a hearth, an
    anvil) -> the skete: white tents, and in the middle of them the Vigil - three rows on their knees with their
    faces to where the sun is to come up, and their backs to whoever comes in -> the wax yard to the east -> the White
    Tent (the Elder; a seal behind him) -> the candle store behind it (a hearth), fenced off by a palisade.

Those who kneel see what is before them and feel who walks at their backs; whoever creeps behind them they do not
know of. The wakers look about, and what a waker sees he wakes the others for. So the skete is gone round, behind the
tents, or through, creeping - or fought, all of it at once.

Everything stands on the level floor of the hollow. Nothing in it is a way up: the firs are bare to five blocks and
more, the tents' walls are three high and sheer, the heaps of wax low, and the palisade either side of the White Tent
five high with nothing beside it."""
import math

import gen_village
from gen_map import FLOOR_LANTERN, LANTERN, hsh
from gen_structures import AIR, MOD

LEAF = 'minecraft:spruce_leaves[persistent=true,distance=1]'
LOG = 'minecraft:spruce_log[axis=%s]'
BARE = 'minecraft:stripped_spruce_log[axis=%s]'
BEARD = 'minecraft:pale_hanging_moss[tip=%s]'
WOOL = 'minecraft:white_wool'
STAIN = 'minecraft:light_gray_wool'
THREAD = 'minecraft:red_wool'
WAX = 'minecraft:mushroom_stem[down=true,east=true,north=true,south=true,up=true,west=true]'
POLE = 'minecraft:spruce_fence'
CHAIN = 'minecraft:iron_chain[axis=%s]'
PLANK = 'minecraft:spruce_planks'


def candles(n, lit=True):
    return f'minecraft:candle[candles={n},lit={"true" if lit else "false"},waterlogged=false]'


X0, X1, Z0, Z1 = 226, 474, 262, 448          # the box everything of the Firwood lies in
WICKET = (226, 402)                          # the grate in the cliff of the yard: its middle cell
TURN = 250                                   # the passage runs east to here, north, and east again
OLD_FIR = (286, 388)
TRACK = (((286, 388), 10), ((306, 374), 14), ((324, 354), 16), ((340, 336), 14), ((354, 329), 9), ((364, 329), 8))
STAKES = (310, 348)                          # the glade of stakes, west of the track
SHRINE = (346, 357)                          # the wayside shrine, east of it
DEN = (364, 295)
DEN_R = 15
LAIR = (364, 277)                            # the nook in the cliff behind the hollow, under the fallen fir
BEYOND = (((396, 295), 4), ((402, 306), 6), ((406, 320), 9), ((409, 335), 8))   # from the Shatun's seal to the skete's edge, and into it
EDGE = (406, 320)
CAMP = (427, 364, 36, 34)                    # the skete: its middle and its half-widths
FIELD_X = (419, 423, 427, 431, 435)          # where they kneel: five to a row,
FIELD_Z = (352, 358, 364)                    # three rows, the last of them the front one
ALTAR = (427, 371)
YARD = (460, 372)                            # the wax yard
TENT = (427, 410)                            # the White Tent
TENT_R = 13
TENT_WALL = 6                                # how high its wall stands before the roof begins
COURT = 22                                   # the ground round it
STORE = (427, 439)                           # the candle store behind it


def smooth(x, z, scale, salt):
    fx, fz = x / scale, z / scale
    x0, z0 = math.floor(fx), math.floor(fz)
    tx, tz = fx - x0, fz - z0
    tx, tz = tx * tx * (3 - 2 * tx), tz * tz * (3 - 2 * tz)
    a, b_ = hsh(x0, 0, z0, salt), hsh(x0 + 1, 0, z0, salt)
    c, d = hsh(x0, 0, z0 + 1, salt), hsh(x0 + 1, 0, z0 + 1, salt)
    return (a * (1 - tx) + b_ * tx) * (1 - tz) + (c * (1 - tx) + d * tx) * tz


def along(x, z, line):
    """How far (x, z) is inside a way drawn as points with a half-width each: negative outside."""
    best = -99.0
    for ((ax, az), ar), ((bx, bz), br) in zip(line, line[1:]):
        dx, dz = bx - ax, bz - az
        t = max(0.0, min(1.0, ((x - ax) * dx + (z - az) * dz) / (dx * dx + dz * dz)))
        best = max(best, ar + (br - ar) * t - math.hypot(x - ax - dx * t, z - az - dz * t))
    return best


def needles(x, y, z):
    """The floor of the wood: a hand's depth of old needles, moss where it is wet, roots."""
    n = hsh(x, 0, z, 820)
    patch = smooth(x, z, 7, 821)
    if patch > 0.66:
        return 'minecraft:podzol' if n < 0.78 else 'minecraft:coarse_dirt'
    if patch < 0.3:
        return 'minecraft:moss_block' if n < 0.4 else ('minecraft:podzol' if n < 0.82 else 'minecraft:rooted_dirt')
    return 'minecraft:podzol' if n < 0.64 else ('minecraft:coarse_dirt' if n < 0.82 else ('minecraft:rooted_dirt' if n < 0.93 else 'minecraft:moss_block'))


def trodden(x, y, z):
    """Where many feet have been: the track, the ground of the skete."""
    n = hsh(x, 0, z, 822)
    return 'minecraft:coarse_dirt' if n < 0.46 else ('minecraft:packed_mud' if n < 0.7 else ('minecraft:gravel' if n < 0.84 else 'minecraft:podzol'))


def raked(x, y, z):
    """The Shatun's hollow: earth he has walked bare, and what he has dug up."""
    n = hsh(x, 0, z, 823)
    return 'minecraft:coarse_dirt' if n < 0.5 else ('minecraft:packed_mud' if n < 0.72 else ('minecraft:rooted_dirt' if n < 0.86 else 'minecraft:gravel'))


def boards(x, y, z):
    n = hsh(x, 0, z, 824)
    return PLANK if n < 0.8 else 'minecraft:stripped_spruce_wood[axis=y]'


def lining(x, y, z):
    n = hsh(x, y, z, 825)
    return 'minecraft:cobblestone' if n < 0.4 else ('minecraft:mossy_cobblestone' if n < 0.66 else ('minecraft:stone_bricks' if n < 0.84 else 'minecraft:mossy_stone_bricks'))


class Skete:
    def __init__(self, b, gy):
        self.b, self.g = b, gy
        self.mask = {(x, z) for x in range(X0, X1 + 1) for z in range(Z0, Z1 + 1) if self._in(x, z)}
        self.taken = set()               # columns something stands on or a way runs over: nothing is planted there
        self.trees = []
        self.lamps = []
        self.mobs = True

    # ------------------------------------------------------------------ the shape of it

    def _in(self, x, z):
        w = (smooth(x, z, 11, 800) - 0.5) * 3.0 + (smooth(x, z, 4, 801) - 0.5) * 1.0       # the foot of the cliff wanders: two blocks either way
        # the ways through the rock are as wide as they are and no wider: fog fills them
        if DEN[0] - 1 <= x <= DEN[0] + 1 and DEN[1] + DEN_R - 1 <= z <= TRACK[-1][0][1] - 6:
            return True
        if DEN[1] - 1 <= z <= DEN[1] + 1 and DEN[0] + DEN_R - 1 <= x <= BEYOND[0][0][0] - 2:
            return True
        if along(x, z, TRACK) + w > 0 or along(x, z, BEYOND) + w * 0.6 > 0:
            # (neither may widen the two ways just named)
            if not (DEN[1] + DEN_R + 1 <= z <= TRACK[-1][0][1] - 9 and abs(x - DEN[0]) < 12) and not (DEN[0] + DEN_R + 1 <= x <= BEYOND[0][0][0] - 5 and abs(z - DEN[1]) < 12):
                return True
        for (cx, cz), r in ((STAKES, 9.5), (SHRINE, 10)):
            if math.hypot(x - cx, z - cz) <= r + w:
                return True
        if math.hypot(x - DEN[0], z - DEN[1]) <= DEN_R + 0.5 + w * 0.3:
            return True
        if math.hypot(x - LAIR[0], z - LAIR[1]) <= 5.5 + w * 0.3 or (abs(x - LAIR[0]) <= 3 and LAIR[1] <= z <= DEN[1] - DEN_R + 2):
            return True
        cx, cz, rx, rz = CAMP
        if ((x - cx) / rx) ** 4 + ((z - cz) / rz) ** 4 <= 1.0 + w * 0.06:
            return True
        if math.hypot(x - YARD[0], z - YARD[1]) <= 10 + w:
            return True
        if math.hypot(x - TENT[0], z - TENT[1]) <= COURT + w:
            return True
        return math.hypot(x - STORE[0], z - STORE[1]) <= 7.5 + w * 0.5

    def inside(self, x, z):
        return (x, z) in self.mask

    def zone(self, x, z):
        """What a column of the hollow belongs to: the wood, the Shatun's hollow, the skete, the court of the tent."""
        if math.hypot(x - DEN[0], z - DEN[1]) <= DEN_R + 3 or (z < DEN[1] and abs(x - LAIR[0]) < 9):
            return 'den'
        if x < DEN[0] + DEN_R + 3 and z > DEN[1]:
            return 'wood'
        if z >= TENT[1] - TENT_R - 3:
            return 'court'
        return 'camp' if z >= 330 else 'wood'

    def keep(self, x0, z0, x1, z1):
        for x in range(min(x0, x1), max(x0, x1) + 1):
            for z in range(min(z0, z1), max(z0, z1) + 1):
                self.taken.add((x, z))

    def ground(self):
        """The floor of the hollow, everywhere: needles in the wood, trodden earth in the skete."""
        b, g = self.b, self.g
        for (x, z) in self.mask:
            zone = self.zone(x, z)
            b.set(x, g, z, raked(x, g, z) if zone == 'den' else (trodden(x, g, z) if zone in ('camp', 'court') and hsh(x, 1, z, 826) < 0.8 else needles(x, g, z)))

    def way(self, points, half=1, salt=0):
        """A track trodden through the wood."""
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
                        if core or hsh(x, 0, z, 830 + salt) < 0.4:
                            b.set(x, g, z, trodden(x, g, z))
                        if core:
                            self.taken.add((x, z))

    def lamp(self, x, z):
        """A lamp of the skete's: a pole, and a lantern on it."""
        b, g = self.b, self.g
        if (x, z) not in self.mask or not all(b.free(x, g + h, z) for h in (1, 2, 3, 4)):
            return
        b.set(x, g + 1, z, BARE % 'y')
        b.fill(x, g + 2, z, x, g + 3, z, POLE)
        b.set(x, g + 4, z, FLOOR_LANTERN)
        self.keep(x - 1, z - 1, x + 1, z + 1)
        self.lamps.append((x, z))

    def mob(self, kind, x, z, facing='south'):
        b, g = self.b, self.g
        if (x, z) not in self.mask or not (b.free(x, g + 1, z) and b.free(x, g + 2, z)):
            raise ValueError(f'no room for {kind} at {(x, z)}: {b.get(x, g + 1, z)}')
        if self.mobs:
            b.mob(kind, x, g + 1, z, facing)
        self.keep(x - 1, z - 1, x + 1, z + 1)

    def stake(self, x, z, height=3, bound=False):
        """A stake such as they tied the sleepers to; on some the rope is still there."""
        b, g = self.b, self.g
        if (x, z) not in self.mask or not b.free(x, g + 1, z):
            return
        b.fill(x, g + 1, z, x, g + height, z, POLE)
        if bound:
            b.set(x, g + height + 1, z, 'minecraft:skeleton_skull[rotation=%d,powered=false]' % int(hsh(x, 0, z, 831) * 16))
        self.keep(x - 1, z - 1, x + 1, z + 1)

    def heap(self, x, z, r, salt=0, lit=0.5):
        """Wax that ran down and set: a low heap, the stubs still standing in it and some of them alight."""
        b, g = self.b, self.g
        for dx in range(-r, r + 1):
            for dz in range(-r, r + 1):
                d = math.hypot(dx, dz) + (hsh(x + dx, 2, z + dz, 832 + salt) - 0.5) * 1.2
                cx, cz = x + dx, z + dz
                if d > r or (cx, cz) not in self.mask or not b.free(cx, g + 1, cz):
                    continue
                self.taken.add((cx, cz))
                n = hsh(cx, 3, cz, 833 + salt)
                if d < r - 1.2 or n < 0.45:
                    b.set(cx, g + 1, cz, WAX)
                    if n > 0.5 and b.free(cx, g + 2, cz):
                        b.set(cx, g + 2, cz, candles(1 + int(n * 40) % 4, hsh(cx, 4, cz, 834) < lit))
                elif n < 0.8:
                    b.set(cx, g + 1, cz, candles(1 + int(n * 30) % 3, hsh(cx, 4, cz, 834) < lit))

    # ------------------------------------------------------------------ the wicket and the passage

    def passage(self, height):
        """From the yard before the town's north gate: east under the plateau, a turn north, a turn east again, and out
        under the old fir. In the leg that runs north, from which neither mouth is seen, the hour is turned."""
        b, g = self.b, self.g
        wx, wz = WICKET
        nz = OLD_FIR[1]
        path = [(x, wz) for x in range(wx, TURN + 1)] + [(TURN, z) for z in range(wz, nz - 1, -1)] + [(x, nz) for x in range(TURN, OLD_FIR[0] - 4)]
        middle = set(path)
        cells = {(px + dx, pz + dz) for (px, pz) in path for dx in (-1, 0, 1) for dz in (-1, 0, 1) if px + dx >= wx} - self.mask
        shell = {(x + dx, z + dz) for (x, z) in cells for dx in (-1, 0, 1) for dz in (-1, 0, 1)} - cells - self.mask
        for (x, z) in cells:
            if height.get((x, z), 0) <= 0:
                raise ValueError(f'the passage to the Firwood comes out into the open at {(x, z)}')
            top = g + (4 if (x, z) in middle and x > wx + 1 else 3)
            b.set(x, g, z, lining(x, g, z) if hsh(x, 0, z, 835) < 0.7 else 'minecraft:gravel')
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
        gate = [(wx, y, z) for y in (g + 1, g + 2, g + 3) for z in (wz - 1, wz, wz + 1)]
        for cell in gate:
            b.set(*cell, f'{MOD}:locked_gate[axis=z,lock=firwood]')
        b.seals['firwood_key'] = gate
        for z in (wz - 2, wz + 2):                               # a lamp either side of it, on the yard's side
            gen_village.wall_lamp(b, wx - 1, g, z, (-1, 0))
        # lamps in the passage, far apart: it is a way few took
        for i, (px, pz) in enumerate(path):
            if i % 13 == 6 and (px, pz) in cells and b.free(px, g + 4, pz):
                b.set(px, g + 4, pz, LANTERN)
        b.threshold('to_night', TURN - 1, g + 1, wz - 5, TURN + 1, wz - 4)
        b.threshold('to_foredawn', TURN - 1, g + 1, nz + 4, TURN + 1, nz + 5)
        self.path = path

    # ------------------------------------------------------------------ the wood

    def fir(self, x, z, height, foot, wide=3, salt=0, base=None):
        """A fir of the hollow: a long bare trunk - nothing of it that can be stood on is lower than `foot` over the
        ground - and above that rings of needles, wider downwards; grey beards hang from the lowest of them."""
        b, g = self.b, self.g
        base = g if base is None else base
        if not all(b.free(x, base + h, z) or 'spruce_leaves' in str(b.get(x, base + h, z)) or 'hanging_moss' in str(b.get(x, base + h, z))
                   for h in range(1, height + 3)):
            return False                                         # (it may come up through another's needles, but through nothing else)
        b.fill(x, base + 1, z, x, base + height, z, LOG % 'y')
        top = base + height
        b.set(x, top + 1, z, LEAF)
        b.set(x, top + 2, z, LEAF)
        radius, lowest = 1, {}
        for y in range(top, base + foot, -1):
            r = radius if (top - y) % 3 != 2 else max(1, radius - 1)
            for dx in range(-r, r + 1):
                for dz in range(-r, r + 1):
                    if not (dx or dz) or dx * dx + dz * dz > r * r + r * 0.6:
                        continue
                    c = (x + dx, y, z + dz)
                    if hsh(c[0], y, c[2], 840 + salt) < 0.9 and b.inside(*c) and b.free(*c):
                        b.set(*c, LEAF)
                        lowest[(c[0], c[2])] = y
            if (top - y) % 3 == 2 and radius < wide:
                radius += 1
        for (cx, cz), y in lowest.items():                       # the beards
            n = hsh(cx, 5, cz, 841 + salt)
            if n < 0.3:
                low = None
                for k in range(1, 2 + int(n * 20) % 3):
                    if y - k < base + 4 or not b.free(cx, y - k, cz):
                        break
                    b.set(cx, y - k, cz, BEARD % 'false')
                    low = y - k
                if low is not None:
                    b.set(cx, low, cz, BEARD % 'true')
        self.trees.append((x, z))
        self.taken.add((x, z))
        return True

    def old_fir(self):
        """The first clearing: a fir older than the skete, four trunks thick, and a hearth at its foot."""
        b, g = self.b, self.g
        b.zone = 'firwood'
        cx, cz = OLD_FIR
        tx, tz = cx + 3, cz - 5
        for dx in (0, 1):
            for dz in (0, 1):
                b.fill(tx + dx, g + 1, tz + dz, tx + dx, g + 27 - (dx + dz) * 2, tz + dz, LOG % 'y')
        for y in range(g + 30, g + 10, -1):
            r = min(7, 1 + (g + 30 - y) // 3) - (1 if (g + 30 - y) % 3 == 2 else 0)
            for dx in range(-r, r + 2):
                for dz in range(-r, r + 2):
                    d = math.hypot(dx - 0.5, dz - 0.5)
                    c = (tx + dx, y, tz + dz)
                    if d <= r + 0.3 and hsh(c[0], y, c[2], 845) < 0.9 and b.inside(*c) and b.free(*c):
                        b.set(*c, LEAF)
        for dx, dz, k in ((-1, 0, 2), (2, 1, 3), (0, 2, 2), (1, -1, 2), (-1, 1, 1), (2, 0, 1)):     # roots like knees
            if b.free(tx + dx, g + 1, tz + dz):
                b.fill(tx + dx, g + 1, tz + dz, tx + dx, g + min(k, 1), tz + dz, 'minecraft:spruce_wood[axis=y]')
        self.keep(tx - 3, tz - 3, tx + 4, tz + 4)
        self.trees.append((tx, tz))
        b.hearth(cx - 2, g + 1, cz + 1, 'old_fir')
        self.keep(cx - 5, cz - 2, cx + 1, cz + 4)
        for x, z in ((cx - 5, cz - 3), (cx + 2, cz + 5)):
            self.lamp(x, z)
        # somebody's pack by the hearth, and a stake driven in beside the way out: the first of many
        b.set(cx - 4, g + 1, cz + 3, 'minecraft:barrel[facing=up]')
        self.stake(cx + 6, cz - 1, 3, bound=True)
        b.threshold('firwood', OLD_FIR[0] - 8, g + 1, cz - 2, OLD_FIR[0] - 7, cz + 2)

    def wood(self):
        """The Firwood: the track from the old fir north-east to the Shatun's hollow, lamps along it far apart; the
        glade of stakes; the wayside shrine; what walks and what sits."""
        b, g = self.b, self.g
        b.zone = 'firwood'
        points = [p for p, r in TRACK]
        self.way(points, 1, 1)
        self.way([points[-1], (DEN[0], DEN[1] + DEN_R)], 1, 2)
        steps = []
        for (ax, az), (bx, bz) in zip(points, points[1:]):
            n = max(abs(bx - ax), abs(bz - az))
            steps += [(round(ax + (bx - ax) * i / n), round(az + (bz - az) * i / n), bx - ax, bz - az) for i in range(n)]
        for i, (x, z, dx, dz) in enumerate(steps):
            if i % 14 == 9:
                side = 1 if (i // 14) % 2 else -1
                length = math.hypot(dx, dz)
                self.lamp(round(x - dz / length * 3 * side), round(z + dx / length * 3 * side))
        # the glade of stakes: here they stood those who slept. Most of the stakes are empty; the ones that were on
        # them walk the wood
        sx, sz = STAKES
        self.way([(318, 360), (sx + 4, sz + 4), (sx, sz)], 0, 3)
        for i in range(7):
            a = math.radians(200 + i * 26)
            x, z = round(sx + math.cos(a) * 6), round(sz + math.sin(a) * 6)
            self.stake(x, z, 3, bound=i in (1, 4))
        b.loot(sx - 1, g + 1, sz - 3, 'firwood_stakes', 'south')
        self.keep(sx - 2, sz - 4, sx, sz - 2)
        b.shard(sx + 2, g + 1, sz - 2)
        self.keep(sx + 1, sz - 3, sx + 3, sz - 1)
        self.lamp(sx - 3, sz + 2)
        # the wayside shrine: a stump as wide as a table, grown over with wax; two on their knees before it with
        # their faces to where the sun will come up, and one who sees to it that they do not sleep
        hx, hz = SHRINE
        self.way([(334, 344), (hx - 3, hz - 5), (hx, hz - 2)], 0, 4)
        for dx in (0, 1):
            for dz in (0, 1):
                b.set(hx + dx, g + 1, hz + 2 + dz, LOG % 'y')
                b.set(hx + dx, g + 2, hz + 2 + dz, candles(2 + (dx + dz * 2) % 3))
        self.keep(hx - 1, hz + 1, hx + 2, hz + 4)
        self.heap(hx - 3, hz + 3, 2, 1)
        self.heap(hx + 4, hz + 4, 2, 2)
        b.loot(hx + 3, g + 1, hz + 1, 'firwood_shrine', 'west')
        self.keep(hx + 2, hz, hx + 4, hz + 2)
        for x in (hx - 2, hx + 3):
            self.mob('wakeful', x, hz - 2, 'south')
        self.mob('waker', hx + 6, hz - 4, 'west')
        # what is met on the way
        for x, z in ((296, 380), (311, 366), (318, 344), (330, 352), (336, 328), (352, 334)):
            self.mob('moth', x, z, 'south')
        for x, z in ((302, 366), (313, 352), (305, 344), (328, 338), (347, 325)):
            self.mob('sleeper', x, z, ('south', 'west', 'east', 'north')[(x + z) % 4])
        self.heap(332, 322, 2, 3)
        self.mob('waxen', 335, 324, 'south')
        self.mob('waxen', 358, 332, 'west')
        # stakes by the way, one here and one there
        for x, z in ((299, 371), (320, 362), (327, 346), (343, 338), (359, 325)):
            self.stake(x, z, 3, bound=(x + z) % 2 == 0)

    # ------------------------------------------------------------------ the Shatun's hollow

    def den(self):
        """A round hollow shut in by the cliff: the post he was chained to in the middle of it, the chain lying where
        it broke; behind, a nook under a fir that fell across the cleft. In from the south, out to the east."""
        b, g = self.b, self.g
        b.zone = 'den'
        cx, cz = DEN
        for (x, z) in self.mask:
            if self.zone(x, z) == 'den':
                self.taken.add((x, z))
        # the two ways through the rock: a frame of logs in each, which the fog fills
        zf = cz + DEN_R + 4
        for z in range(cz + DEN_R - 1, TRACK[-1][0][1] - 5):
            self.keep(cx - 1, z, cx + 1, z)
        b.fill(cx - 2, g + 5, zf, cx + 2, g + 5, zf, LOG % 'x')
        b.fog('shatun_fog', cx - 1, g + 1, zf, cx + 1, g + 4, zf, 'x')
        xf = cx + DEN_R + 6
        for x in range(cx + DEN_R - 1, BEYOND[0][0][0] - 1):
            self.keep(x, cz - 1, x, cz + 1)
        b.fill(xf, g + 5, cz - 2, xf, g + 5, cz + 2, LOG % 'z')
        b.fog('shatun', xf, g + 1, cz - 1, xf, g + 4, cz + 1, 'z', sealed=True)
        b.threshold('den', cx - 1, g + 1, cz + DEN_R, cx + 1, cz + DEN_R + 1)
        # the post, and the chain
        for dx in (0, 1):
            for dz in (0, 1):
                b.fill(cx + dx, g + 1, cz + 2 + dz, cx + dx, g + 4 - (dx ^ dz), cz + 2 + dz, BARE % 'y')
        b.set(cx - 1, g + 3, cz + 2, CHAIN % 'x')
        for i in range(1, 6):
            if hsh(i, 0, 0, 850) < 0.8:
                b.set(cx - 1 - i, g + 1, cz + 2 + (i // 3), CHAIN % 'x')
        # what he has left lying
        for x, z in ((cx - 9, cz - 6), (cx + 8, cz + 7), (cx + 10, cz - 5), (cx - 6, cz + 9)):
            b.set(x, g + 1, z, 'minecraft:bone_block[axis=%s]' % ('x' if (x + z) % 2 else 'z'))
            if b.free(x + 1, g + 1, z):
                b.set(x + 1, g + 1, z, 'minecraft:skeleton_skull[rotation=%d,powered=false]' % ((x * 3 + z) % 16))
        # lamps the skete set round him, to keep him from the dark he wanted
        for a in (40, 140, 220, 320):
            x, z = round(cx + math.cos(math.radians(a)) * (DEN_R - 2)), round(cz + math.sin(math.radians(a)) * (DEN_R - 2))
            gen_village.lamp_post(b, x, g + 1, z)
        # the nook, and the fir across it
        lx, lz = LAIR
        for z in range(lz - 4, cz - DEN_R + 4):
            if (lx + 2, z) in self.mask or (lx + 1, z) in self.mask:
                for x in (lx + 1, lx + 2):
                    b.set(x, g + 5, z, LOG % 'z')
        for x in (lx + 1, lx + 2):
            for z in (lz - 5, lz - 6):
                if (x, z) not in self.mask:
                    b.set(x, g + 5, z, LOG % 'z')
        b.loot(lx - 2, g + 1, lz - 2, 'firwood_den', 'south')
        b.set(lx - 3, g + 1, lz, 'minecraft:bone_block[axis=x]')
        self.mob('shatun', cx, cz - 6, 'south')

    # ------------------------------------------------------------------ the skete

    def tent(self, cx, cz, r, door, salt=0, lamp=True):
        """A white tent: a round wall three high and a roof drawn up to a point. `door` is the side its way in is on."""
        b, g = self.b, self.g
        wall = [(dx, dz) for dx in range(-r, r + 1) for dz in range(-r, r + 1) if r - 0.75 < math.hypot(dx, dz) <= r + 0.45]
        if any((cx + dx, cz + dz) not in self.mask or not b.free(cx + dx, g + 1, cz + dz) for dx, dz in wall):
            raise ValueError(f'no room for a tent at {(cx, cz)}')
        for dx in range(-r - 1, r + 2):
            for dz in range(-r - 1, r + 2):
                self.taken.add((cx + dx, cz + dz))
                if math.hypot(dx, dz) <= r - 0.75:
                    b.set(cx + dx, g, cz + dz, trodden(cx + dx, g, cz + dz))
        for dx, dz in wall:
            for y in range(g + 1, g + 4):
                b.set(cx + dx, y, cz + dz, STAIN if y == g + 1 and hsh(cx + dx, y, cz + dz, 860 + salt) < 0.3 else WOOL)
        for k in range(r + 1):                                   # the roof: every course a block further in
            y, rr = g + 4 + k, r - k
            for dx in range(-rr, rr + 1):
                for dz in range(-rr, rr + 1):
                    d = math.hypot(dx, dz)
                    if d <= rr + 0.45 and (d > rr - 1.5 or k == r) and b.free(cx + dx, y, cz + dz):
                        b.set(cx + dx, y, cz + dz, WOOL)
        b.set(cx, g + 5 + r, cz, POLE)
        sx, sz = {'n': (0, -1), 's': (0, 1), 'w': (-1, 0), 'e': (1, 0)}[door]
        b.fill(cx + sx * r, g + 1, cz + sz * r, cx + sx * r, g + 2, cz + sz * r, AIR)
        b.set(cx + sx * r, g + 3, cz + sz * r, THREAD)                   # the red thread, over the way in
        self.keep(cx + sx * (r + 1) - 1, cz + sz * (r + 1) - 1, cx + sx * (r + 2) + 1, cz + sz * (r + 2) + 1)
        if lamp:
            top = g + 4 + (r - 1)
            for y in range(top - 1, g + 3, -1):
                b.set(cx, y, cz, CHAIN % 'y')
            b.set(cx, g + 3, cz, LANTERN)
        for dx, dz in ((-1, 1), (1, -1), (-1, -1)):              # where they would lie, if they lay
            if (dx * (r - 2), dz * (r - 2)) != (sx * (r - 2), sz * (r - 2)) and hsh(cx + dx, 0, cz + dz, 861 + salt) < 0.75:
                b.set(cx + dx * (r - 2), g + 1, cz + dz * (r - 2), 'minecraft:white_carpet' if hsh(cx, dx, cz + dz, 862) < 0.6 else 'minecraft:light_gray_carpet')

    def edge(self):
        """Behind the Shatun's seal: a way east and south to the skete's edge, where a lean-to stands over a hearth
        and somebody's anvil."""
        b, g = self.b, self.g
        b.zone = 'skete'
        points = [(DEN[0] + DEN_R + 9, DEN[1])] + [p for p, r in BEYOND[:3]]
        self.way(points, 1, 5)
        ex, ez = EDGE
        # the lean-to: four posts, a roof of boards sloping to the cliff
        for x in (ex - 6, ex - 2):
            for z in (ez - 3, ez + 1):
                b.fill(x, g + 1, z, x, g + 3, z, BARE % 'y')
        for x in range(ex - 7, ex):
            for z in range(ez - 4, ez + 3):
                b.set(x, g + 4, z, 'minecraft:spruce_slab[type=bottom,waterlogged=false]')
        b.hearth(ex - 4, g + 1, ez - 1, 'skete')
        b.set(ex - 5, g + 1, ez + 1, f'{MOD}:forge')
        b.set(ex - 3, g + 3, ez - 2, LANTERN)
        self.keep(ex - 8, ez - 5, ex + 1, ez + 4)
        self.lamp(ex + 4, ez + 5)
        self.lamp(DEN[0] + DEN_R + 14, DEN[1] + 4)
        b.threshold('skete', points[1][0] - 1, g + 1, points[1][1] - 6, points[1][0], points[1][1] + 6)

    def camp(self):
        """The skete: tents round a field where the wakeful kneel in three rows with their faces to the south-east -
        on the plan to the south, which in the world is the east - and their backs to the way in. Lanes behind the
        tents on either side; the wax yard off the east lane; and before the first row the altar, and beyond it the
        White Tent."""
        b, g = self.b, self.g
        b.zone = 'skete'
        cx, cz, rx, rz = CAMP
        # the tents: a row at the back of the field, one down either side
        for x, z, r, door in ((414, 339, 3, 's'), (427, 337, 3, 's'), (440, 339, 3, 's'),
                              (403, 348, 3, 'e'), (402, 360, 3, 'e'), (403, 372, 3, 'e'), (404, 384, 2, 'e'),
                              (451, 348, 3, 'w'), (452, 360, 2, 'w'), (451, 386, 3, 'w')):
            self.tent(x, z, r, door, x + z)
        b.loot(403, g + 1, 374, 'skete_tent', 'north')
        # the field: every one of them with a candle before him, and stands of candles at its corners
        for z in FIELD_Z:
            for x in FIELD_X:
                self.mob('wakeful', x, z, 'south')
                b.set(x, g + 1, z + 2, candles(1 + (x + z) % 3))
                self.taken.add((x, z + 2))
        for x in range(FIELD_X[0] - 3, FIELD_X[-1] + 4):
            for z in range(FIELD_Z[0] - 3, FIELD_Z[-1] + 5):
                self.taken.add((x, z))
                if hsh(x, 6, z, 870) < 0.05 and b.free(x, g + 1, z) and (x, z) not in {(fx, fz) for fx in FIELD_X for fz in FIELD_Z}:
                    b.set(x, g + 1, z, candles(1, hsh(x, 7, z, 871) < 0.7))
        for x in (FIELD_X[0] - 3, FIELD_X[-1] + 3):
            for z in (FIELD_Z[0] - 3, FIELD_Z[-1] + 3):
                b.fill(x, g + 1, z, x, g + 2, z, POLE)
                b.set(x, g + 3, z, candles(4))
        # the altar: a board on two stumps, what was laid on it, and what somebody left under it
        ax, az = ALTAR
        for x in (ax - 1, ax + 1):
            b.set(x, g + 1, az, 'minecraft:spruce_slab[type=top,waterlogged=false]')
        b.loot(ax, g + 1, az, 'skete_altar', 'north')
        for x in (ax - 1, ax + 1):
            b.set(x, g + 2, az, candles(3 if x < ax else 4))
        self.heap(ax - 4, az + 2, 1, 5, lit=0.8)
        self.heap(ax + 4, az + 2, 1, 6, lit=0.8)
        self.keep(ax - 6, az - 2, ax + 6, az + 4)
        b.threshold('vigil', cx - 34, g + 1, 331, cx - 8, 332)
        # the wax yard: vats, racks of dipped candles, and the heaps of what ran over
        yx, yz = YARD
        for x, z in ((yx + 2, yz - 4), (yx + 4, yz - 1), (yx + 3, yz + 3)):
            b.set(x, g + 1, z, 'minecraft:cauldron')
            self.keep(x - 1, z - 1, x + 1, z + 1)
        for z in (yz - 6, yz + 6):
            for x in (yx - 2, yx + 1):
                b.fill(x, g + 1, z, x, g + 2, z, POLE)
            for x in range(yx - 2, yx + 2):
                b.set(x, g + 3, z, 'minecraft:spruce_trapdoor[facing=north,half=bottom,open=false,powered=false,waterlogged=false]')
            self.keep(yx - 3, z - 1, yx + 2, z + 1)
        self.heap(yx - 3, yz + 2, 2, 7)
        self.heap(yx + 6, yz - 5, 2, 8)
        self.heap(yx + 1, yz - 1, 1, 9)
        b.loot(yx + 6, g + 1, yz + 2, 'skete_wax_yard', 'west')
        self.keep(yx + 5, yz + 1, yx + 7, yz + 3)
        for x, z in ((yx - 2, yz - 2), (yx + 4, yz + 5), (yx + 6, yz - 1)):
            self.mob('waxen', x, z, 'west')
        # lamps down the lanes and between the tents
        for x, z in ((395, 342), (396, 366), (397, 390), (409, 354), (409, 378), (445, 354), (445, 378), (458, 344), (459, 358), (457, 392),
                     (420, 344), (434, 344), (413, 394), (441, 394)):
            self.lamp(x, z)
        # who is about: the wakers on their rounds, the sleepers they put out, moths that sit on the trodden ground
        self.mob('waker', 411, 347, 'north')
        self.mob('waker', 443, 362, 'north')
        self.mob('waker', 396, 372, 'north')
        self.mob('waker', 458, 384, 'north')
        for x, z in ((396, 352), (397, 382), (421, 334), (459, 350), (446, 392)):
            self.mob('sleeper', x, z, ('south', 'west', 'east', 'north')[(x + z) % 4])
        for x, z in ((433, 345), (408, 366), (446, 370)):
            self.mob('moth', x, z, 'south')
        # stakes at the back of the field, where whoever nodded was stood for the rest to see
        for x in (417, 421, 433, 437):
            self.stake(x, 346, 3, bound=x in (421, 433))

    def white_tent(self):
        """The White Tent: a round wall of white cloth with the red thread run through it and a roof drawn up to a
        point as high as the firs. One way in, from the field; one out, at the back, into the court where the skete
        keeps its candles - and a palisade from the tent to the cliff either side, so that there is no other."""
        b, g = self.b, self.g
        b.zone = 'white_tent'
        cx, cz = TENT
        R = TENT_R
        for dx in range(-R - 2, R + 3):
            for dz in range(-R - 2, R + 3):
                d = math.hypot(dx, dz)
                x, z = cx + dx, cz + dz
                if d > R + 1.6:
                    continue
                self.taken.add((x, z))
                if d <= R - 0.6:
                    b.set(x, g, z, boards(x, g, z) if d > 3.5 or hsh(x, 0, z, 880) < 0.5 else 'minecraft:white_wool')
                elif d <= R + 0.4:
                    for y in range(g + 1, g + TENT_WALL + 1):
                        b.set(x, y, z, THREAD if y == g + 4 else (STAIN if y == g + 1 and hsh(x, y, z, 881) < 0.25 else WOOL))
        for k in range(R + 1):                                   # the roof: a cone, hollow
            y, rr = g + TENT_WALL + 1 + k, R - k
            for dx in range(-rr - 1, rr + 2):
                for dz in range(-rr - 1, rr + 2):
                    d = math.hypot(dx, dz)
                    if rr - 1.6 < d <= rr + 0.4 or (k == R and d < 1.5):
                        b.set(cx + dx, y, cz + dz, THREAD if k >= R - 1 else WOOL)
        b.fill(cx, g + TENT_WALL + R + 2, cz, cx, g + TENT_WALL + R + 4, cz, POLE)
        b.set(cx, g + TENT_WALL + R + 5, cz, FLOOR_LANTERN)
        # the two ways through the cloth
        for z, name, sealed in ((cz - R, 'elder_fog', False), (cz + R, 'elder', True)):
            b.fill(cx - 1, g + 1, z, cx + 1, g + 4, z, AIR)
            b.fog(name, cx - 1, g + 1, z, cx + 1, g + 4, z, 'x', sealed=sealed)
            self.keep(cx - 3, z - 3, cx + 3, z + 3)
        b.threshold('white_tent', cx - 1, g + 1, cz - R + 1, cx + 1, cz - R + 2)
        # lamps: one on a long chain from the point of the roof, with four round it; four more half way to the wall;
        # candles round the wall
        for y in range(g + TENT_WALL + R, g + 8, -1):
            if b.free(cx, y, cz):
                b.set(cx, y, cz, CHAIN % 'y')
        b.set(cx, g + 8, cz, 'minecraft:spruce_planks')
        for dx, dz in ((0, 0), (1, 0), (-1, 0), (0, 1), (0, -1)):
            if dx or dz:
                b.set(cx + dx, g + 8, cz + dz, 'minecraft:spruce_slab[type=top,waterlogged=false]')
            b.set(cx + dx, g + 7, cz + dz, LANTERN)
        for dx, dz in ((7, 0), (-7, 0), (0, 7), (0, -7), (5, 5), (-5, 5), (5, -5), (-5, -5)):
            top = g + TENT_WALL + 1 + R - int(math.hypot(dx, dz) + 0.5)
            for y in range(top, g + 6, -1):
                if b.free(cx + dx, y, cz + dz):
                    b.set(cx + dx, y, cz + dz, CHAIN % 'y')
            b.set(cx + dx, g + 6, cz + dz, LANTERN)
        for a in range(0, 360, 20):
            x, z = round(cx + math.cos(math.radians(a + 10)) * (R - 1.6)), round(cz + math.sin(math.radians(a + 10)) * (R - 1.6))
            if abs(x - cx) > 3 and b.free(x, g + 1, z):
                b.set(x, g + 1, z, candles(1 + (a // 20) % 4))
        # those who kneel round the wall, with their faces to it: he calls them when he needs them
        for a in (30, 90, 150, 210, 270, 330):
            if a in (90, 270):
                continue
            x, z = round(cx + math.cos(math.radians(a)) * (R - 3.5)), round(cz + math.sin(math.radians(a)) * (R - 3.5))
            self.mob('wakeful', x, z, 'east' if x > cx else 'west')
        for x in (cx - 9, cx + 9):
            self.mob('wakeful', x, cz, 'east' if x > cx else 'west')
        self.mob('elder', cx, cz + 4, 'north')
        # the palisade: from the cloth to the rock, either side, two logs thick and five high
        for x in range(cx - COURT - 4, cx + COURT + 5):
            if abs(x - cx) <= R:
                continue
            for z in (cz, cz + 1):
                if (x, z) in self.mask:
                    high = g + 5 + (1 if hsh(x, 0, z, 882) < 0.5 else 0)
                    b.fill(x, g + 1, z, x, high, z, LOG % 'y')
                    b.set(x, high + 1, z, POLE)
                    self.keep(x - 1, z - 2, x + 1, z + 2)
        # before the tent: the two who keep its door, two who kneel in sight of them, lamps
        b.zone = 'skete'
        self.mob('waker', cx - 4, cz - R - 4, 'north')
        self.mob('waker', cx + 4, cz - R - 4, 'north')
        self.mob('wakeful', cx - 7, cz - R - 8, 'south')
        self.mob('wakeful', cx + 7, cz - R - 8, 'south')
        for x in (cx - 3, cx + 3):
            b.fill(x, g + 1, cz - R - 1, x, g + 3, cz - R - 1, POLE)
            b.set(x, g + 4, cz - R - 1, candles(4))
            self.keep(x - 1, cz - R - 2, x + 1, cz - R)

    def store(self):
        """Behind the White Tent: the court where the candles are kept, racks of them, a hearth under a roof of
        boards, and a way on through the rock that is barred."""
        b, g = self.b, self.g
        b.zone = 'white_tent'
        cx, cz = STORE
        tz = TENT[1] + TENT_R
        self.way([(cx, tz + 1), (cx, cz - 2)], 1, 6)
        for x in (cx - 3, cx + 2):
            for z in (cz - 1, cz + 3):
                b.fill(x, g + 1, z, x, g + 3, z, BARE % 'y')
        for x in range(cx - 4, cx + 4):
            for z in range(cz - 2, cz + 5):
                if (x, z) in self.mask or b.get(x, g + 4, z) is None:
                    b.set(x, g + 4, z, 'minecraft:spruce_slab[type=bottom,waterlogged=false]')
        b.hearth(cx, g + 1, cz + 1, 'reliquary')
        b.set(cx - 2, g + 1, cz + 2, f'{MOD}:forge')
        b.loot(cx + 1, g + 1, cz + 3, 'skete_store', 'north')
        b.set(cx - 1, g + 3, cz, LANTERN)
        self.keep(cx - 5, cz - 3, cx + 4, cz + 5)
        # racks either side of the way, heaps, a shard in the ash of somebody's fire
        for side in (-1, 1):
            x, z = cx + side * 6, tz + 4
            run = [x + side * i for i in range(4)]
            if all((rx, z) in self.mask and b.free(rx, g + 1, z) for rx in run):
                for rx in (run[0], run[-1]):
                    b.fill(rx, g + 1, z, rx, g + 2, z, POLE)
                for rx in run:
                    b.set(rx, g + 3, z, 'minecraft:spruce_trapdoor[facing=north,half=bottom,open=false,powered=false,waterlogged=false]')
                self.keep(min(run) - 1, z - 1, max(run) + 1, z + 1)
        self.heap(cx - 12, tz + 3, 2, 10, lit=0.7)
        self.heap(cx + 11, tz + 2, 2, 11, lit=0.7)
        b.shard(cx + 4, g + 1, tz + 5)
        self.keep(cx + 3, tz + 4, cx + 5, tz + 6)
        self.lamp(cx - 5, tz + 4)
        self.lamp(cx + 6, tz + 8)
        # the way on, barred (a gate nothing in the map opens): east out of the court
        gx = max(x for (x, z) in self.mask if z == TENT[1] + 8)
        for x in range(gx - 2, gx + 4):
            for z in range(TENT[1] + 7, TENT[1] + 10):
                if (x, z) in self.mask:
                    self.taken.add((x, z))
        for y in (g + 1, g + 2, g + 3):
            b.set(gx + 1, y, TENT[1] + 8, f'{MOD}:locked_gate[axis=z,lock=none]')
        for z in (TENT[1] + 7, TENT[1] + 9):
            b.fill(gx + 1, g + 1, z, gx + 1, g + 4, z, lining)
        b.set(gx + 1, g + 4, TENT[1] + 8, lining(gx + 1, g + 4, TENT[1] + 8))
        b.fill(gx + 2, g + 1, TENT[1] + 8, gx + 3, g + 3, TENT[1] + 8, AIR)       # a little of the way behind it, to be seen through the bars
        self.barred = (gx + 1, TENT[1] + 8)

    # ------------------------------------------------------------------ what grows

    def firs(self):
        """The firs: close in the wood, so that the sky is seen only in pieces; fewer in the skete, along its edges
        and between the tents' backs and the cliff; none in the Shatun's hollow. Under them fern, moss, fungus, fallen
        trunks, and here and there a knoll."""
        b, g = self.b, self.g
        far = {}                                                 # how far every place is from the foot of the cliff
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
        knolls = set()
        for (x, z) in sorted(self.mask):                         # the knolls first: a block high, nowhere near anything built
            if self.zone(x, z) == 'wood' and far[(x, z)] >= 3 and smooth(x, z, 5, 890) > 0.78 \
                    and not any((x + dx, z + dz) in built for dx in range(-2, 3) for dz in range(-2, 3)):
                knolls.add((x, z))
                b.set(x, g + 1, z, needles(x, g + 1, z))
                b.set(x, g, z, 'minecraft:dirt')
        for (x, z) in sorted(self.mask):
            if (x, z) in self.taken:
                continue
            zone = self.zone(x, z)
            if zone == 'den':
                continue
            n = hsh(x, 6, z, 891)
            base = g + (1 if (x, z) in knolls else 0)
            near_built = any((x + dx, z + dz) in built for dx in range(-2, 3) for dz in range(-2, 3))
            beside_built = any((x + dx, z + dz) in built for dx in (-1, 0, 1) for dz in (-1, 0, 1))
            crowd, apart = (0.45, 5) if zone == 'wood' else ((0.3, 6) if far[(x, z)] <= 7 else (0.012, 9))
            if n < crowd and far[(x, z)] >= 2 and not beside_built and all(abs(x - tx) + abs(z - tz) >= apart for tx, tz in self.trees) \
                    and all(((x + dx, z + dz) in knolls) == ((x, z) in knolls) for dx, dz in ((1, 0), (-1, 0), (0, 1), (0, -1))):
                tall = 13 + int(hsh(x, 8, z, 892) * 10)
                if self.fir(x, z, tall, 6 + int(hsh(x, 9, z, 893) * 3), 3 + int(hsh(x, 10, z, 894) * 2), x * 5 + z, base):
                    continue
            y = base + 1
            if not b.free(x, y, z):
                continue
            ground = str(b.get(x, y - 1, z))
            if zone == 'wood' and 0.5 < n < 0.508 and not near_built and (x, z) not in knolls:
                # a fallen trunk, three or four blocks of it
                dx, dz = ((1, 0), (0, 1))[int(hsh(x, 11, z, 895) * 2)]
                run = [(x + dx * i, z + dz * i) for i in range(3 + int(hsh(x, 12, z, 896) * 2))]
                if all(c in self.mask and c not in self.taken and c not in knolls and b.free(c[0], g + 1, c[1]) and far[c] >= 3
                       and not any((c[0] + ex, c[1] + ez) in built for ex in range(-2, 3) for ez in range(-2, 3)) for c in run):
                    for c in run:
                        b.set(c[0], g + 1, c[1], LOG % ('x' if dx else 'z'))
                        self.taken.add(c)
                    continue
            if 'podzol' in ground or 'moss' in ground or 'rooted' in ground:
                if n > 0.86 and zone == 'wood':
                    b.set(x, y, z, 'minecraft:fern')
                elif 0.8 < n <= 0.83 and zone == 'wood' and b.free(x, y + 1, z):
                    b.set(x, y, z, 'minecraft:large_fern[half=lower]')
                    b.set(x, y + 1, z, 'minecraft:large_fern[half=upper]')
                elif 0.76 < n <= 0.775:
                    b.set(x, y, z, 'minecraft:brown_mushroom')
                elif 0.775 < n <= 0.782:
                    b.set(x, y, z, 'minecraft:red_mushroom')
                elif 0.7 < n <= 0.74 and 'moss' in ground:
                    b.set(x, y, z, 'minecraft:moss_carpet')
                elif 0.66 < n <= 0.68:
                    b.set(x, y, z, 'minecraft:dead_bush')
            elif zone != 'wood' and 0.9 < n and far[(x, z)] <= 4:
                b.set(x, y, z, 'minecraft:fern' if n < 0.96 else 'minecraft:dead_bush')

    # ------------------------------------------------------------------ all of it

    def build(self, height):
        self.ground()
        self.passage(height)
        self.old_fir()
        self.wood()
        self.den()
        self.edge()
        self.camp()
        self.white_tent()
        self.store()
        self.firs()

    def views(self):
        """Pictures of the place for the client test: name, eye, what it looks at (plan coordinates)."""
        g = self.g
        wx, wz = WICKET
        ox, oz = OLD_FIR
        dx, dz = DEN
        cx, cz = TENT
        return [
            ('firwood_wicket_from_the_yard', (wx - 9, g + 2.6, wz + 4.5), (wx, g + 2.6, wz + 0.5)),
            ('firwood_passage', (wx + 4.5, g + 2.6, wz + 0.5), (TURN, g + 2.6, wz + 0.5)),
            ('firwood_old_fir', (ox - 8, g + 2.6, oz + 0.5), (ox + 3, g + 5, oz - 4)),
            ('firwood_old_fir_up', (ox - 3, g + 2.6, oz + 4), (ox + 4, g + 26, oz - 4)),
            ('firwood_track', (ox + 6, g + 2.6, oz - 4), (306, g + 3, 374)),
            ('firwood_under_the_firs', (312, g + 2.6, 366), (324, g + 5, 352)),
            ('firwood_stakes', (316, g + 2.6, 356), (STAKES[0], g + 2.6, STAKES[1])),
            ('firwood_shrine', (SHRINE[0] - 2, g + 2.6, SHRINE[1] - 9), (SHRINE[0] + 0.5, g + 2, SHRINE[1] + 2)),
            ('firwood_from_above', (296, g + 46, 396), (326, g + 2, 352)),
            ('firwood_den_door', (dx + 0.5, g + 2.6, dz + DEN_R + 12), (dx + 0.5, g + 3, dz + DEN_R)),
            ('firwood_den', (dx + 0.5, g + 2.6, dz + DEN_R - 3), (dx + 0.5, g + 3, dz - 8)),
            ('firwood_den_from_above', (dx - 16, g + 24, dz + 18), (dx, g + 1, dz)),
            ('skete_edge', (EDGE[0] + 5, g + 2.6, EDGE[1] - 8), (EDGE[0] - 4, g + 2, EDGE[1] - 1)),
            ('skete_vigil_from_behind', (CAMP[0] - 8, g + 2.6, 333), (CAMP[0], g + 2, FIELD_Z[1])),
            ('skete_vigil_from_the_side', (FIELD_X[0] - 9, g + 2.6, FIELD_Z[1] + 2), (FIELD_X[-1], g + 2, FIELD_Z[1])),
            ('skete_vigil_faces', (ALTAR[0] + 0.5, g + 2.6, ALTAR[1] + 5), (ALTAR[0] + 0.5, g + 2, FIELD_Z[0])),
            ('skete_west_lane', (396, g + 2.6, 340), (397, g + 3, 388)),
            ('skete_wax_yard', (YARD[0] - 9, g + 2.6, YARD[1] - 1), (YARD[0] + 4, g + 2, YARD[1])),
            ('skete_from_above', (CAMP[0] - 34, g + 40, 326), (CAMP[0], g + 1, 372)),
            ('skete_white_tent', (cx + 0.5, g + 2.6, ALTAR[1] + 6), (cx + 0.5, g + 9, cz - TENT_R)),
            ('skete_white_tent_from_above', (cx + 30, g + 36, cz - 34), (cx, g + 8, cz)),
            ('skete_in_the_white_tent', (cx + 0.5, g + 2.6, cz - TENT_R + 2), (cx + 0.5, g + 4, cz + 6)),
            ('skete_white_tent_roof', (cx - 4, g + 2.6, cz - 5), (cx + 2, g + 18, cz + 2)),
            ('skete_candle_store', (cx + 0.5, g + 2.6, cz + TENT_R + 3), (cx + 0.5, g + 2.6, STORE[1])),
        ]

    def probes(self):
        g = self.g
        return [('firwood_passage', (TURN, g + 1, WICKET[1] - 7)), ('firwood_track', (306, g + 1, 374)), ('firwood_den', (DEN[0] + 4, g + 1, DEN[1] + 6)),
                ('skete_field', (FIELD_X[2], g + 1, FIELD_Z[1] + 3)), ('skete_white_tent', (TENT[0] + 3, g + 1, TENT[1] - 3))]
