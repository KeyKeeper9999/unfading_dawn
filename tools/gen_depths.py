#!/usr/bin/env python3
"""The Sky Beneath: the map's eighth place and its last with anything to fight in it, built into the world's Map by
gen_world.py (it has no plan of its own).

It is under the ground: forty blocks under the floor of the White Pit's sump, in the stone the world itself is made of
(the plan's layers under zero: see gen_world.UNDER). That is where the day that was not let begin has been kept, and
that day has an hour, the one after sunset, and a sky. The place is caves: thin dark ways of black rock with a roof of
rock, wide ones that wind, and great round hollows - and only the great hollows have the sky. Their walls lean in to a
neck, and over the neck there is no roof: sheets of mist with stars in them, one over another (the block sky_veil),
and over the last of them the sky itself (sky_lid, drawn as the game draws the gate to its End). Here and there the
floor is gone and the sky is under it: whoever steps into a pit is gone too. The only light is the crystals', violet.

The way in is the Lower Grate, in the south wall of the quarry's sump (a hearth); its key lies in the granary behind the
Hollow Miller.

    the Lower Grate -> a drift that goes down: flights of stairs south, a cross leg east (the hour is turned in it),
    more flights south, and the cut stone gives out -> a hearth in the last of it -> the Brink: a ledge in the wall of
    the first great hollow -> down to the Still Water: its floor, ridges, crystals, the wheels of a clock as big as a
    hall, two pits; a side chamber across a crack that is jumped -> a neck -> the Works: the round hall the pendulum
    swings in (the Bolt; a seal behind it) -> a stair up -> a hearth behind the pendulum -> the Causeways, which are
    ways now: a thin one, a wide winding one west, round chambers (a gut's, a spire, a cache over a pit by one cell
    of rock), a wide winding one east -> a hearth by the Tear -> the Tear: a round hollow of still water (the Coming,
    in the middle of it; a seal behind it) -> the room beyond, where the clock's own works are, for what comes later.

Levels: the Still Water and the Works are PLATE, the Brink BRINK, the Causeways and the Tear HIGH; a pit's bottom is
VOID. Nothing here is a way up or out: it is rock on every side, and over the three skies more rock.

How it is built: first every floor is laid (`lay`: a column, the layer of its floor, what roof it has), and the pits
(`pit`); then all of it is hollowed at once (`carve`: how high a column is open follows from how far it is from the
wall); then the drift is cut, the skies are hung (`skies`), everything is dressed, and a skin of rock is put round
whatever was opened (`skin`: under the ground the plan holds only what differs from the world's stone)."""
import math

from gen_map import BARS, hsh, stairs
from gen_structures import AIR, MOD

SKY = MOD + ':under_sky[tone=%d]'
VEIL = MOD + ':sky_veil[mist=%d]'
LID = MOD + ':sky_lid'
FILM = MOD + ':night_shallows'
DOWN = -51                                   # how far under the layers it was first built at the place lies
VOID = 1 + DOWN                              # the bottom of a pit: sky
PLATE = 10 + DOWN                            # the floor block of the Still Water and of the Works
BRINK = 14 + DOWN                            # of the ledge the drift comes out on
HIGH = 16 + DOWN                             # of the Causeways and the Tear
ROOMS = {'water': PLATE + 28, 'works': PLATE + 30, 'tear': HIGH + 28}     # the hollows that have a sky: the layer of its lid
VEILS = 4                                    # sheets of mist under a lid
MISTS = 6                                    # and how many thicknesses mist has (SkyVeilBlock.MISTS)
VEIL_STEP = 3                                # and how far apart they hang
FLARE = 12                                   # so far under its lid a hollow is as wide as itself again, and wider: its sky has no walls to see
LID_TILE = 17                                # a lid draws a square this wide round itself (SkyLidRenderer.REACH * 2 + 1)
GATE = (17, 540)                             # the Lower Grate: its middle cell, in the south wall of the sump
SUMP_FLOOR = 3                               # (Quarry.LOW)
TURN_Z = 577                                 # the drift's cross leg
LEG_X = 30                                   # and the line its last leg runs south on
RAW_Z = 591                                  # south of this nobody cut it
ROOM_Z = 602                                 # the hearth in the last of the rock: its middle
MOUTH_Z = 610                                # where the drift ends
LEDGE = (30, 617)                            # the Brink
WORKS = (78, 706)                            # the Works: the middle of the floor the pendulum swings over
WORKS_R = 15
BEHIND = (78, 740)                           # the hearth behind the pendulum
TEAR = (62, 784)                             # the Tear: its middle
TEAR_R = 18
LAST = (62, 757)                             # the hearth by the Tear
END = (62, 808)                              # the room beyond it
# the Still Water: round these its floor lies (x, z, how far), and here the floor is gone
WATER = ((44, 632, 9), (58, 640, 15), (80, 648, 17), (104, 654, 14), (118, 664, 9), (92, 668, 13), (72, 664, 11), (78, 678, 8), (40, 627, 6))
PITS = ((68, 652, 3.6), (99, 661, 3.0))
ISLE = (128, 646, 5.5)                       # the chamber off its east side: jumped to, over a crack
# the Causeways: the chambers (x, z, how far, how high) and the ways between them
ISLETS = {'mid': (56, 739, 4.2, 7), 'west': (31, 744, 7.5, 8), 'cache': (20, 758, 3.4, 5), 'spire': (48, 758, 5.2, 9), 'wheel': (96, 754, 7.5, 9)}
SIDE_PIT = (79, 765, 3.0)                    # beside the long way east


def smooth(x, z, scale, salt):
    fx, fz = x / scale, z / scale
    x0, z0 = math.floor(fx), math.floor(fz)
    tx, tz = fx - x0, fz - z0
    tx, tz = tx * tx * (3 - 2 * tx), tz * tz * (3 - 2 * tz)
    a, b_ = hsh(x0, 0, z0, salt), hsh(x0 + 1, 0, z0, salt)
    c, d = hsh(x0, 0, z0 + 1, salt), hsh(x0 + 1, 0, z0 + 1, salt)
    return (a * (1 - tx) + b_ * tx) * (1 - tz) + (c * (1 - tx) + d * tx) * tz


def black_rock(x, y, z):
    """The rock of the place, wherever it is bare."""
    n = hsh(x, y, z, 1300)
    if smooth(x + y, z - y, 6, 1301) > 0.62:
        return 'minecraft:blackstone' if n < 0.72 else 'minecraft:basalt[axis=y]'
    return 'minecraft:blackstone' if n < 0.38 else ('minecraft:deepslate' if n < 0.74 else ('minecraft:cobbled_deepslate' if n < 0.9 else 'minecraft:smooth_basalt'))


def glass_rock(x, y, z):
    """The floor under the still water: as black as there is, so that the water over it is a mirror."""
    n = hsh(x, y, z, 1302)
    return 'minecraft:black_concrete' if n < 0.6 else ('minecraft:obsidian' if n < 0.84 else 'minecraft:blackstone')


def cut_rock(x, y, z):
    """The drift where it is still a drift: the quarry's own dark stone."""
    n = hsh(x, y, z, 1303)
    return 'minecraft:deepslate' if n < 0.5 else ('minecraft:cobbled_deepslate' if n < 0.76 else ('minecraft:tuff' if n < 0.9 else 'minecraft:stone'))


def works_floor(x, y, z):
    """The floor of the Works: black iron and stone, and a little of the Founder's copper."""
    n = hsh(x, y, z, 1304)
    return 'minecraft:polished_blackstone' if n < 0.5 else ('minecraft:polished_blackstone_bricks' if n < 0.78 else ('minecraft:deepslate_tiles' if n < 0.94 else 'minecraft:netherite_block'))


FOUR = ((1, 0), (-1, 0), (0, 1), (0, -1))


class Depths:
    def __init__(self, b, gy):
        self.b, self.g = b, gy
        self.ground = {}                 # every column that can be stood on: the layer of its floor block
        self.mat = {}                    # what that block is: a function of the position
        self.kind = {}                   # for every column laid: (how high its roof of rock is, the hollow it is of - if that has a sky)
        self.pits = {}                   # the columns that have no floor: the layer of the floor beside them
        self.cut = {}                    # the drift's own columns: (floor, the way it runs, where on the path, whether a stair)
        self.top = {}                    # the highest open layer of every column
        self.open_sky = {}               # the columns that have a sky over them: the layer of its lid
        self.shaft = set()               # and those of them that are open all the way up from the floor: the neck
        self.wet = set()                 # the columns the still water lies on
        self.taken = set()               # columns something stands on or a way runs over: nothing more is put there
        self.lit = []                    # where the place's own light is (crystals): for the pictures
        self.own = set()                 # every cell the place wrote
        self.met = []                    # and those of them that were somebody else's already
        self.mobs = True

    def open(self, x, y, z):
        """Whether a cell is air. (Under the ground a cell the plan does not have is rock, not room.)"""
        return self.b.get(x, y, z) == AIR

    # ------------------------------------------------------------------ the hollows

    def lay(self, cells, y, mat=None, wet=False, roof=5, sky=None):
        """Floor at layer y under these columns, for all of them that have none yet. `roof`: how many cells are open
        over it in its middle (less at its walls); `sky`: the hollow it belongs to, if that has a sky instead."""
        new = set()
        for c in cells:
            if c in self.ground or c in self.pits or c in self.cut:
                continue
            self.ground[c] = y
            self.mat[c] = mat or (glass_rock if wet else black_rock)
            self.kind[c] = (roof, sky)
            if wet:
                self.wet.add(c)
            new.add(c)
        return new

    def pit(self, cells, rim, roof=6, sky=None):
        """No floor under these columns: the sky is down there. `rim` is the layer of the floor beside them."""
        new = set()
        for c in cells:
            if c in self.ground or c in self.pits or c in self.cut:
                continue
            self.pits[c] = rim
            self.kind[c] = (roof, sky)
            new.add(c)
        return new

    def carve(self):
        """Everything laid is hollowed out. A column is open as high as it is far from the wall: under a roof of rock
        three cells at the wall and two more with every column inwards, up to the roof; in a hollow with a sky three
        and three more, and from the neck inwards all the way up to the lid. Over the neck such a hollow opens out
        again, wider than its floor: from below, nothing of the sky's own walls is seen past the lip."""
        b = self.b
        inside = set(self.ground) | set(self.pits)
        whole = inside | set(self.cut)
        groups = {}
        for c in inside:
            groups.setdefault(self.kind[c][1], set()).add(c)
        dist = {}
        for room, cols in groups.items():
            within = whole if room is None else cols              # (the wall of a hollow with a sky goes all the way round it: ways come in under it)
            ring = [c for c in cols if any((c[0] + dx, c[1] + dz) not in within for dx, dz in FOUR)]
            for c in ring:
                dist[c] = 1
            d = 1
            while ring and d < 9:
                d += 1
                grown = []
                for (x, z) in ring:
                    for dx, dz in FOUR:
                        n = (x + dx, z + dz)
                        if n in cols and n not in dist:
                            dist[n] = d
                            grown.append(n)
                ring = grown
        for c in sorted(inside):
            x, z = c
            roof, room = self.kind[c]
            d = dist.get(c, 9)
            base = self.pits[c] if c in self.pits else self.ground[c]
            if room is not None and d >= 4 + int(smooth(x, z, 7, 1321) * 2.4):
                top = ROOMS[room]
                self.shaft.add(c)
            elif room is not None:
                top = base + 3 + (d - 1) * 3 + int(hsh(x, 10, z, 1322) * 2)
            else:
                top = base + min(roof + int(smooth(x, z, 3, 1323) * 1.6), 3 + (d - 1) * 2 + (1 if hsh(x, 11, z, 1324) < 0.3 else 0))
            if c in self.pits:
                b.set(x, VOID, z, SKY % 0)
                low = VOID + 1
            else:
                b.set(x, base, z, self.mat[c](x, base, z))
                low = base + 1
            for y in range(low, top + 1):
                b.set(x, y, z, AIR)
            if c in self.pits:
                b.set(x, VOID + 2, z, VEIL % 1)                   # (a breath of mist over the stars down there: the blocks are not seen for blocks)
            self.top[c] = top
        for room, lid in ROOMS.items():
            cols = groups[room]
            for (x, z) in cols | {(x + dx, z + dz) for (x, z) in cols for dx in (-1, 0, 1) for dz in (-1, 0, 1)}:
                self.open_sky[(x, z)] = lid
                self.kind.setdefault((x, z), (0, room))
                for y in range(lid - FLARE, lid + 1):
                    if b.get(x, y, z) is None:
                        b.set(x, y, z, AIR)

    def skies(self):
        """Over every hollow that has one: sheets of mist one over another, each with thin places, thick places and
        gaps of its own (so the eye goes through them unevenly, as through cloud), thicker upwards; and the lid over
        the last."""
        b = self.b
        for (x, z), lid in self.open_sky.items():
            for k in range(VEILS):                                 # (k = 0 is the highest)
                n = smooth(x, z, 9, 1326 + k * 2) * 0.62 + smooth(x, z, 4, 1327 + k * 2) * 0.38
                mist = int((n - 0.3 + (VEILS - 1 - k) * -0.035) * 13.0)
                if mist >= 0:
                    b.set(x, lid - 2 - k * VEIL_STEP, z, VEIL % min(MISTS - 1, mist))
        for room, lid in ROOMS.items():
            cols = [c for c, top in self.open_sky.items() if self.kind[c][1] == room]
            x0, z0 = min(c[0] for c in cols), min(c[1] for c in cols)
            tiles = {((x - x0) // LID_TILE, (z - z0) // LID_TILE) for (x, z) in cols}
            for (i, j) in sorted(tiles):
                # (the lid stands in the middle of the square it draws; where that is in the rock it is in the rock)
                b.set(x0 + i * LID_TILE + LID_TILE // 2, lid, z0 + j * LID_TILE + LID_TILE // 2, LID)

    def skin(self):
        """Rock round everything the place opened or set, wherever the plan has nothing. (The check takes a cell the
        plan does not have for room; so does the reckoning of the light.)"""
        b = self.b
        cells = b.cells
        count = 0
        for (x, y, z) in list(self.own):
            for dx in (-1, 0, 1):
                for dy in (-1, 0, 1):
                    for dz in (-1, 0, 1):
                        if dx and dy and dz:
                            continue
                        n = (x + dx, y + dy, z + dz)
                        if n not in cells and n[1] <= self.g and n[2] > GATE[1] and b.inside(*n):
                            cells[n] = (cut_rock if n[2] < RAW_Z else black_rock)(*n)
                            count += 1
        return count

    @staticmethod
    def blob(circles, salt=0, rough=1.6):
        """The columns within `how far` of any of the points, the edge of it wandering."""
        out = set()
        for cx, cz, r in circles:
            for x in range(int(cx - r - 3), int(cx + r + 4)):
                for z in range(int(cz - r - 3), int(cz + r + 4)):
                    if math.hypot(x - cx, z - cz) <= r + (smooth(x, z, 4, 1340 + salt) - 0.5) * rough * 2:
                        out.add((x, z))
        return out

    @staticmethod
    def line(a, c, half):
        """The columns of a straight way from a to c, `half` either side of its middle. Its middle line is one that
        can be walked: every cell of it has the next beside it, not at its corner."""
        out = set()
        steps = max(abs(c[0] - a[0]), abs(c[1] - a[1]), 1)
        last = None
        for i in range(steps + 1):
            px, pz = round(a[0] + (c[0] - a[0]) * i / steps), round(a[1] + (c[1] - a[1]) * i / steps)
            mid = [(px, pz)]
            if last is not None and last[0] != px and last[1] != pz:
                mid.append((px, last[1]))
            for (mx, mz) in mid:
                for dx in range(-half, half + 1):
                    for dz in range(-half, half + 1):
                        out.add((mx + dx, mz + dz))
            last = (px, pz)
        return out

    def keep(self, x0, z0, x1, z1):
        for x in range(min(x0, x1), max(x0, x1) + 1):
            for z in range(min(z0, z1), max(z0, z1) + 1):
                self.taken.add((x, z))

    def free(self, x, z, r=0, height=3):
        """Whether the columns within r of this one are floor with nothing on them."""
        for dx in range(-r, r + 1):
            for dz in range(-r, r + 1):
                c = (x + dx, z + dz)
                if c not in self.ground or c in self.taken or self.ground[c] != self.ground.get((x, z)):
                    return False
                if not all(self.open(c[0], self.ground[c] + h, c[1]) for h in range(1, height + 1)):
                    return False
        return True

    def mob(self, kind, x, z, facing='south', tall=2):
        b = self.b
        if (x, z) not in self.ground:
            raise ValueError(f'the {kind} at {(x, z)} would stand on nothing')
        y = self.ground[(x, z)]
        if not all(self.open(x, y + h, z) for h in range(1, tall + 1)):
            raise ValueError(f'no room for the {kind} at {(x, z)}: {[b.get(x, y + h, z) for h in range(1, tall + 1)]}')
        if self.mobs:
            b.mob(kind, x, y + 1, z, facing)
        self.keep(x - 1, z - 1, x + 1, z + 1)

    # ------------------------------------------------------------------ what stands on it

    def crystal(self, x, z, tall=2, salt=0):
        """Crystals: a foot of pale rock, a spike of them, lesser ones round it - and the light of the place, which
        comes from them (the game's own gives too little: an unseen lamp is hung in each)."""
        b = self.b
        if (x, z) not in self.ground or (x, z) in self.taken:
            return False
        y = self.ground[(x, z)]
        if not all(self.open(x, y + h, z) for h in range(1, tall + 3)):
            return False
        b.set(x, y + 1, z, 'minecraft:calcite' if hsh(x, 4, z, 1350 + salt) < 0.5 else 'minecraft:smooth_basalt')
        for h in range(2, tall + 1):
            b.set(x, y + h, z, 'minecraft:amethyst_block')
        b.set(x, y + tall + 1, z, 'minecraft:amethyst_cluster[facing=up,waterlogged=false]')
        b.set(x, y + tall + 2, z, 'minecraft:light[level=10,waterlogged=false]')
        for dx, dz, facing in ((1, 0, 'east'), (-1, 0, 'west'), (0, 1, 'south'), (0, -1, 'north')):
            if tall >= 2 and hsh(x + dx, 5, z + dz, 1351 + salt) < 0.6 and self.open(x + dx, y + 2, z + dz) and (x + dx, z + dz) not in self.taken:
                b.set(x + dx, y + 2, z + dz, f'minecraft:amethyst_cluster[facing={facing},waterlogged=false]')
        self.keep(x - 1, z - 1, x + 1, z + 1)
        self.lit.append((x, y + tall + 1, z))
        return True

    def ridge(self, points, height=5, salt=0, half=1):
        """A ridge of rock across a plate: something to go round, and to have one's back to."""
        b = self.b
        for a, c in zip(points, points[1:]):
            for (x, z) in self.line(a, c, half):
                if (x, z) not in self.ground or (x, z) in self.taken:
                    continue
                y = self.ground[(x, z)]
                core = (x, z) in self.line(a, c, 0)
                top = height - (0 if core else 1 + int(hsh(x, 6, z, 1360 + salt) * 2)) + int(smooth(x, z, 3, 1361 + salt) * 2)
                if top < 3:
                    top = 3                                       # (nothing of it is a step)
                for h in range(1, top + 1):
                    b.set(x, y + h, z, black_rock(x, y + h, z))
                self.wet.discard((x, z))
        for a, c in zip(points, points[1:]):
            for (x, z) in self.line(a, c, half):
                self.taken.add((x, z))

    def wheel(self, cx, cz, r, axis, sunk=0.4, y=None, salt=0):
        """A wheel of the clock, standing on its edge, part of it under the floor: a rim with teeth, a hub, four
        spokes. `axis` is the way its axle runs ('x': the wheel is seen from east and west)."""
        b = self.b
        y0 = (self.ground.get((cx, cz), PLATE) if y is None else y)
        hub = y0 + 1 + int(round(r * (1 - 2 * sunk)))
        for du in range(-r - 1, r + 2):
            for dv in range(-r - 1, r + 2):
                far = math.hypot(du, dv)
                ang = math.atan2(dv, du)
                tooth = far <= r + 1.2 and far > r - 0.2 and int((ang + math.pi) / (2 * math.pi) * (r * 2)) % 2 == 0
                rim = r - 1.6 < far <= r + 0.3
                spoke = far <= r and (abs(du) < 0.6 or abs(dv) < 0.6)
                boss = far <= 1.6
                if not (tooth or rim or spoke or boss):
                    continue
                x, z = (cx, cz + du) if axis == 'x' else (cx + du, cz)
                yy = hub - dv
                if yy <= y0 or not b.inside(x, yy, z):
                    continue
                if b.get(x, yy, z) != AIR and 'night_shallows' not in str(b.get(x, yy, z)):
                    continue                                       # (whatever of it is not in the open is in the rock)
                n = hsh(x, yy, z, 1370 + salt)
                state = 'minecraft:netherite_block' if boss else ('minecraft:waxed_cut_copper' if tooth and n < 0.3 else
                                                                 ('minecraft:polished_blackstone' if n < 0.7 else 'minecraft:polished_blackstone_bricks'))
                b.set(x, yy, z, state)
                if (x, z) in self.ground and yy <= self.ground[(x, z)] + 3:
                    self.taken.add((x, z))
                    self.wet.discard((x, z))
        for du in range(-r - 2, r + 3):
            c = (cx, cz + du) if axis == 'x' else (cx + du, cz)
            for side in (-1, 0, 1):
                self.taken.add((c[0] + (side if axis == 'x' else 0), c[1] + (0 if axis == 'x' else side)))

    def niche(self, x, z, facing):
        """A long-armed one in the face of a ridge: the cell it sits in, shut in on three sides and overhead, open
        the way it faces."""
        b = self.b
        if (x, z) not in self.ground:
            raise ValueError(f'no floor for a niche at {(x, z)}')
        y = self.ground[(x, z)]
        fx, fz = {'north': (0, -1), 'south': (0, 1), 'east': (1, 0), 'west': (-1, 0)}[facing]
        for dx in (-1, 0, 1):
            for dz in (-1, 0, 1):
                c = (x + dx, z + dz)
                if (dx, dz) == (0, 0) or (dx, dz) == (fx, fz) or c not in self.ground:
                    continue
                if dx * fx + dz * fz > 0:
                    continue                                       # (the two cells beside the way out stay as they are)
                for h in range(1, 5):
                    if self.open(c[0], y + h, c[1]):
                        b.set(c[0], y + h, c[1], black_rock(c[0], y + h, c[1]))
                self.taken.add(c)
                self.wet.discard(c)
        for h in (1, 2, 3):
            b.set(x, y + h, z, AIR)
        b.set(x, y + 4, z, black_rock(x, y + 4, z))
        self.wet.discard((x, z))
        if self.mobs:
            b.mob('longarm', x, y + 1, z, facing)
        self.taken.add((x, z))
        for k in (1, 2, 3):                                        # (and the way out of it, through whatever of the ridge is before it)
            c = (x + fx * k, z + fz * k)
            if c in self.ground:
                for h in (1, 2, 3):
                    if not self.open(c[0], y + h, c[1]) and 'mob_mark' not in str(b.get(c[0], y + h, c[1])):
                        b.set(c[0], y + h, c[1], AIR)
                self.taken.add(c)

    def lamp_grid(self, cells, level, up=4, every=4):
        """Light without a lamp, a little of it, over a floor: the dark of the place is to be seen in."""
        b = self.b
        for (x, z) in cells:
            if x % every == 0 and z % every == 0 and (x, z) in self.ground:
                for h in range(up, 1, -1):                         # (lower under a low roof)
                    if self.open(x, self.ground[(x, z)] + h, z):
                        b.set(x, self.ground[(x, z)] + h, z, f'minecraft:light[level={level},waterlogged=false]')
                        break

    def arch(self, x0, x1, z, y, name, sealed=False, axis='x', height=4):
        """A doorway for fog where there is no wall: two posts and a beam of black stone across a way that runs along z
        (axis 'x': the fog is a sheet along x) or along x."""
        b = self.b
        def at(u, yy):
            return (u, yy, z) if axis == 'x' else (z, yy, u)
        for u in (x0 - 1, x1 + 1):
            for yy in range(y + 1, y + height + 2):
                b.set(*at(u, yy), 'minecraft:polished_blackstone_bricks' if yy % 3 else 'minecraft:chiseled_polished_blackstone')
        for u in range(x0 - 1, x1 + 2):
            b.set(*at(u, y + height + 1), 'minecraft:polished_blackstone')
        for u in range(x0, x1 + 1):
            for yy in range(y + 1, y + height + 1):
                if b.get(*at(u, yy)) not in (None, AIR):
                    b.set(*at(u, yy), AIR)
        c0, c1 = at(x0, y + 1), at(x1, y + height)
        b.fog(name, min(c0[0], c1[0]), y + 1, min(c0[2], c1[2]), max(c0[0], c1[0]), y + height, max(c0[2], c1[2]), axis, sealed=sealed)
        for u in range(x0 - 1, x1 + 2):
            c = at(u, 0)
            self.taken.add((c[0], c[2]))
            self.wet.discard((c[0], c[2]))

    @staticmethod
    def way(a, c, half, bend=0.0, salt=0):
        """The columns of a way that winds from a to c: it swings `bend` columns off the straight line on its way, and is
        `half` either side of its middle, a little more or less as it goes."""
        out = set()
        length = math.hypot(c[0] - a[0], c[1] - a[1]) or 1.0
        nx, nz = -(c[1] - a[1]) / length, (c[0] - a[0]) / length
        mx, mz = (a[0] + c[0]) / 2 + nx * bend * 2, (a[1] + c[1]) / 2 + nz * bend * 2
        steps = int(length * 3) + 2
        last = None
        for i in range(steps + 1):
            t = i / steps
            px = round((1 - t) ** 2 * a[0] + 2 * t * (1 - t) * mx + t * t * c[0])
            pz = round((1 - t) ** 2 * a[1] + 2 * t * (1 - t) * mz + t * t * c[1])
            if (px, pz) == last:
                continue
            mid = [(px, pz)]
            if last is not None and last[0] != px and last[1] != pz:
                mid.append((px, last[1]))
            r = half + (smooth(px, pz, 5, 1345 + salt) - 0.4) * (1.4 if half > 1 else 0.0)
            for (ux, uz) in mid:
                k = int(r + 1)
                for dx in range(-k, k + 1):
                    for dz in range(-k, k + 1):
                        if max(abs(dx), abs(dz)) <= half if half <= 1 else math.hypot(dx, dz) <= r + 0.3:
                            out.add((ux + dx, uz + dz))
            last = (px, pz)
        return out

    def lamps(self, cells, level, every=3):
        """Light without a lamp along a way with a roof of rock: under the roof, wherever that is."""
        b = self.b
        for (x, z) in cells:
            if (x + 2 * z) % (every * 2 + 1) == 0 and (x, z) in self.ground:
                y = self.ground[(x, z)]
                for up in (4, 3, 2):
                    if self.open(x, y + up, z):
                        b.set(x, y + up, z, f'minecraft:light[level={level},waterlogged=false]')
                        break

    def near(self, x, z, r=0, tall=3, reach=5):
        """The floor nearest to a column that is free `r` round and `tall` up."""
        for k in range(0, reach + 1):
            for dx in range(-k, k + 1):
                for dz in range(-k, k + 1):
                    if max(abs(dx), abs(dz)) == k and self.free(x + dx, z + dz, r, tall):
                        return (x + dx, z + dz)
        raise ValueError(f'no free floor near {(x, z)}')

    # ------------------------------------------------------------------ the way in

    def drift_plan(self):
        """The line of the drift, with the layer of its floor at every cell: from the Lower Grate south and down by
        flights of eight with a landing between, a cross leg east, south and down again. A cell of a flight has its
        floor one lower than the cell before it and a stair lying on that floor."""
        gx, gz = GATE
        path = []
        y, z = SUMP_FLOOR, gz
        for kind, n in (('flat', 3), ('down', 8), ('flat', 2), ('down', 8), ('flat', 2), ('down', 8), ('flat', 2), ('down', 4), ('flat', 1)):
            for _ in range(n):
                y -= kind == 'down'
                path.append((gx, y, z, 'south', kind == 'down'))
                z += 1
        assert z - 1 == TURN_Z
        for x in range(gx + 1, LEG_X + 1):
            path.append((x, y, TURN_Z, 'east', False))
        z = TURN_Z + 1
        for kind, n in (('flat', 2), ('down', 8), ('flat', 2), ('down', 4), ('flat', MOUTH_Z - TURN_Z - 16)):
            for _ in range(n):
                y -= kind == 'down'
                path.append((LEG_X, y, z, 'south', kind == 'down'))
                z += 1
        assert y == BRINK and z - 1 == MOUTH_Z, (y, z)
        floor = {}
        for i, (x, y, z, way, stair) in enumerate(path):
            for d in (-1, 0, 1):
                c = (x + d, z) if way == 'south' else (x, z + d)
                floor.setdefault(c, (y, way, i, stair))
            if i and path[i - 1][3] != way or (i + 1 < len(path) and path[i + 1][3] != way):   # the corners, filled out
                for dx in (-1, 0, 1):
                    for dz in (-1, 0, 1):
                        floor.setdefault((x + dx, z + dz), (y, way, i, False))
        self.path = path
        self.cut = floor
        # the room: the last place with a wall on every side
        self.room = {}
        for x in range(LEG_X - 4, LEG_X + 5):
            for z in range(ROOM_Z - 3, ROOM_Z + 4):
                edge = abs(x - LEG_X) == 4 or abs(z - ROOM_Z) == 3
                if (x, z) in floor or (edge and hsh(x, 7, z, 1381) < 0.5):
                    continue
                self.room[(x, z)] = BRINK
        for c in self.room:
            self.cut[c] = (BRINK, 'south', 0, False)

    def drift(self):
        """The drift is cut: stone and stairs, timber on its landings while it is a drift, lamps; then raw rock and
        crystals. A room with a hearth in the last of the rock, and the mouth."""
        b = self.b
        gx, gz = GATE
        path = self.path
        for (x, z), (y, way, i, stair) in self.cut.items():
            if z <= gz:
                continue
            rock = cut_rock if z < RAW_Z else black_rock
            b.set(x, y, z, rock(x, y, z))
            top = y
            if stair:
                top = y + 1
                b.set(x, top, z, stairs('cobbled_deepslate' if z < RAW_Z else 'blackstone', 'north'))
                b.flights.append((x, top, z))
            high = 3 if z < RAW_Z else 3 + int(smooth(x, z, 3, 1380) * 2)
            if (x, z) in self.room or abs(z - ROOM_Z) <= 3:
                high = 4
            for h in range(1, high + 1):
                b.set(x, top + h, z, AIR)
        # the grate: three wide, three high, in the sump's wall
        gate = [(x, y, gz) for x in (gx - 1, gx, gx + 1) for y in (SUMP_FLOOR + 1, SUMP_FLOOR + 2, SUMP_FLOOR + 3)]
        for cell in gate:
            b.set(*cell, f'{MOD}:locked_gate[axis=x,lock=depths]')
        for x in (gx - 1, gx, gx + 1):
            b.set(x, SUMP_FLOOR, gz, cut_rock(x, SUMP_FLOOR, gz))
            b.set(x, SUMP_FLOOR + 4, gz, cut_rock(x, SUMP_FLOOR + 4, gz))
        b.seals['depths_key'] = gate
        # timber on the landings (never over the cell a flight is climbed from: the check jumps where the game walks
        # up a stair), a lamp over every flight; light without a lamp all the way
        for i, (x, y, z, way, stair) in enumerate(path):
            if z <= gz + 1 or z >= RAW_Z or stair or i + 1 >= len(path):
                continue
            if not path[i - 1][4] and (path[i + 1][4] or i % 5 == 0) and path[i - 1][3] == way == path[i + 1][3]:
                for d in (-1, 1):
                    c = (x + d, z) if way == 'south' else (x, z + d)
                    b.set(c[0], y + 1, c[1], 'minecraft:spruce_log[axis=y]')
                    b.set(c[0], y + 2, c[1], 'minecraft:spruce_log[axis=y]')
                for d in (-1, 0, 1):
                    c = (x + d, z) if way == 'south' else (x, z + d)
                    b.set(c[0], y + 3, c[1], 'minecraft:spruce_log[axis=%s]' % ('x' if way == 'south' else 'z'))
        run = 0
        for i, (x, y, z, way, stair) in enumerate(path):
            run = run + 1 if stair else 0
            if run == 4 and z < RAW_Z:
                b.set(x, y + 4, z, 'minecraft:soul_lantern[hanging=true]')
            if z > gz + 1 and i % 5 == 2:
                for up in (y + (4 if stair else 3), y + (3 if stair else 2)):       # (under a beam, if there is one)
                    if self.open(x, up, z):
                        b.set(x, up, z, 'minecraft:light[level=11,waterlogged=false]')
                        break
        for z in range(RAW_Z + 4, MOUTH_Z - 9, 5):
            side = -1 if (z // 5) % 2 else 1
            y, _, _, stair = self.cut[(LEG_X + side, z)]
            if not stair and self.open(LEG_X + side, y + 1, z):
                b.set(LEG_X + side, y + 1, z, 'minecraft:amethyst_cluster[facing=up,waterlogged=false]')
                b.set(LEG_X + side, y + 2, z, 'minecraft:light[level=7,waterlogged=false]')
        # the hour: towards the quarry night, towards the place its own
        b.threshold('to_night', gx - 1, SUMP_FLOOR + 1, gz + 1, gx + 1, gz + 2)
        ty = self.cut[(gx + 6, TURN_Z)][0]
        b.threshold('to_blue_hour', gx + 6, ty + 1, TURN_Z - 1, gx + 7, TURN_Z + 1)
        b.hearth(LEG_X - 3, BRINK + 1, ROOM_Z, 'brink')
        b.loot(LEG_X + 3, BRINK + 1, ROOM_Z + 1, 'depths_brink', 'west')
        b.set(LEG_X + 3, BRINK + 1, ROOM_Z - 2, 'minecraft:amethyst_cluster[facing=up,waterlogged=false]')
        b.set(LEG_X + 3, BRINK + 3, ROOM_Z - 2, 'minecraft:light[level=12,waterlogged=false]')
        b.set(LEG_X - 3, BRINK + 4, ROOM_Z + 2, 'minecraft:light[level=12,waterlogged=false]')

    # ------------------------------------------------------------------ every floor of the place

    def layout(self):
        lx, lz = LEDGE
        wx, wz = WORKS
        bx, bz = BEHIND
        tx, tz = TEAR
        # the Brink: a ledge in the wall of the first hollow, and steps of rock down its east side
        ledge = self.blob(((lx, lz, 7.5), (lx, MOUTH_Z + 1, 3.5)), 1, 1.0)
        self.ledge = self.lay({c for c in ledge if c[1] >= MOUTH_Z - 1}, BRINK, black_rock, sky='water')
        x, y, z = lx + 7, BRINK, lz + 2
        steps = []
        while y > PLATE:
            for _ in range(2):
                x += 1
                steps.append((x, y, z))
            y -= 1
        steps.append((x + 1, y, z))
        self.spur = set()
        for (sx, sy, sz) in steps:
            self.spur |= self.lay({(sx, sz + dz) for dz in (-1, 0, 1)}, sy, black_rock, sky='water')
        self.steps_end = (steps[-1][0], steps[-1][2])
        # the Still Water: its pits, the chamber east of it over a crack, and the floor itself
        for hx, hz, hr in PITS:
            self.pit(self.blob(((hx, hz, hr),), 3, 0.8), PLATE, sky='water')
        ix, iz, ir = ISLE
        isle = self.blob(((ix, iz, ir),), 4, 0.7)
        gap_x = min(x for (x, z) in isle if z == iz) - 1
        isle -= {c for c in isle if c[0] <= gap_x}
        self.pit({(x, iz + dz) for x in range(gap_x - 2, gap_x + 1) for dz in range(-5, 6)}, PLATE, sky='water')
        self.isle = self.lay(isle, PLATE, wet=True, sky='water')
        self.lay({(x, iz + dz) for x in range(gap_x - 8, gap_x - 2) for dz in (-1, 0, 1)}, PLATE, wet=True, sky='water')
        self.jump = (gap_x - 3, iz)
        sx, sz = self.steps_end
        self.water_cells = self.lay(self.blob(WATER + ((sx + 4, sz, 4.5),), 2), PLATE, wet=True, sky='water')
        self.neck = self.lay(self.line((wx, 682), (wx, wz - WORKS_R - 1), 2), PLATE, works_floor, roof=5)
        # the Works: the round floor, a recess either side of it for a great wheel, the way out south
        R = WORKS_R
        self.works_floor = self.lay({(x, z) for x in range(wx - R - 2, wx + R + 3) for z in range(wz - R - 2, wz + R + 3) if math.hypot(x - wx, z - wz) <= R + 0.3},
                                    PLATE, works_floor, sky='works')
        self.lay({(x, z) for x in range(wx - R - 8, wx + R + 9) for z in range(wz - 11, wz + 12)
                  if math.hypot((abs(x - wx) - R - 2) / 5.4, (z - wz) / 10.6) <= 1.0 + (smooth(x, z, 3, 1342) - 0.5) * 0.3}, PLATE, black_rock, sky='works')
        # a stair up (laid before the way out of the Works, which is wider than it is long), and the chamber behind the pendulum
        x, z, y = wx, wz + R + 3, PLATE
        self.flight = []
        while y < HIGH:
            z += 1
            y += 1
            self.flight.append((x, y, z))
            z += 1
            self.flight.append((x, y, z))
        for (fx, fy, fz) in self.flight:
            self.lay({(fx + dx, fz) for dx in (-1, 0, 1)}, fy, works_floor, roof=4)
        self.works_out = self.lay(self.line((wx, wz + R + 1), (wx, wz + R + 3), 2), PLATE, works_floor, roof=5)
        self.behind_cells = self.lay(self.blob(((bx, bz, 6.2),), 6, 0.8) | self.line((x, z), (bx, bz - 5), 1), HIGH, black_rock, roof=6)
        # the Causeways: the chambers first, then the cache's one cell of rock over its pit, then the ways
        rooms = {name: self.lay(self.blob(((x, z, r),), 10 + i, 0.7), HIGH, black_rock, roof=high) for i, (name, (x, z, r, high)) in enumerate(ISLETS.items())}
        last = self.lay(self.blob(((LAST[0], LAST[1], 5.2),), 20, 0.6), HIGH, black_rock, roof=6)
        mid, west, cache, spire, wheel = (ISLETS[k] for k in ('mid', 'west', 'cache', 'spire', 'wheel'))
        a, c = (west[0] - 4, west[1] + 5), (cache[0], cache[1] - 2)
        self.bridge = self.lay(self.line(a, c, 0), HIGH, black_rock, roof=6)
        self.pit(self.blob((((a[0] + c[0]) / 2, (a[1] + c[1]) / 2, 4.2),), 31, 0.6), HIGH, roof=6)
        self.ways = [self.way((bx - 5, bz), (mid[0], mid[1]), 1, 1.5, 1), self.way((mid[0], mid[1]), (west[0] + 5, west[1] - 2), 2, -2.5, 2),
                     self.way((west[0] + 2, west[1] + 5), (spire[0] - 3, spire[1]), 1, 2.0, 3),
                     self.way((spire[0] + 3, spire[1]), (LAST[0] - 3, LAST[1]), 1, -1.0, 4),
                     self.way((LAST[0] + 3, LAST[1]), (wheel[0] - 5, wheel[1] + 1), 3, 2.6, 5)]
        cells = set().union(*rooms.values()) | last | self.bridge
        for way, high in zip(self.ways, (4, 6, 4, 4, 6)):
            cells |= self.lay(way, HIGH, black_rock, roof=high)
        self.pit(self.blob((SIDE_PIT,), 32, 0.6), HIGH, roof=6)
        for (x, z) in ((40, 751), (72, 761)):                       # a stub of rock off a way, for a deepling to hang over
            near = min(((abs(x - c[0]) + abs(z - c[1])), c) for c in cells)[1]
            cells |= self.lay({(x, z)} | self.line((x, z), near, 0), HIGH, black_rock, roof=5)
        self.causeway_cells = cells
        # the Tear, the way to it and the room beyond
        R = TEAR_R
        self.tear_north = self.lay(self.line((tx, tz - R - 3), (tx, LAST[1] + 4), 1), HIGH, black_rock, roof=5)
        self.tear_floor = self.lay({(x, z) for x in range(tx - R - 3, tx + R + 4) for z in range(tz - R - 3, tz + R + 4) if math.hypot(x - tx, z - tz) <= R + 2.2},
                                   HIGH, glass_rock, wet=True, sky='tear')
        self.tear_south = self.lay(self.line((tx, tz + R + 3), (tx, END[1] - 3), 1), HIGH, black_rock, roof=5)
        self.end = self.lay(self.blob(((END[0], END[1], 5.0),), 30, 0.6), HIGH, works_floor, roof=6)

    # ------------------------------------------------------------------ the Brink and the Still Water

    def brink(self):
        b = self.b
        lx, lz = LEDGE
        b.threshold('under_sky', lx - 1, BRINK + 1, MOUTH_Z + 1, lx + 1, MOUTH_Z + 2)
        self.keep(lx - 1, MOUTH_Z - 2, lx + 1, lz + 2)
        for c in self.spur:
            self.taken.add(c)
        self.crystal(lx - 5, lz + 1, 3, 1)
        self.crystal(lx + 4, lz - 4, 2, 2)
        self.lamp_grid(self.ledge | self.spur, 13)

    def water(self):
        """The Still Water: ridges across it, crystals, three wheels of the clock; the ways through it are between
        these, and there is more than one."""
        b = self.b
        b.zone = 'still_water'
        sx, sz = self.steps_end
        cells = self.water_cells
        b.threshold('still_water', sx + 1, PLATE + 1, sz - 1, sx + 2, sz + 1)
        self.keep(sx, sz - 1, sx + 4, sz + 1)
        # the chamber east of it: three cells of nothing between, and a chest for whoever jumps them
        ix, iz, ir = ISLE
        b.loot(ix + 2, PLATE + 1, iz, 'depths_island', 'west')
        self.keep(ix + 1, iz - 1, ix + 3, iz + 1)
        self.keep(self.jump[0] - 4, iz - 1, self.jump[0] + 5, iz + 1)
        self.crystal(ix, iz - 3, 3, 3)
        self.crystal(ix - 1, iz + 3, 2, 4)
        if self.free(ix - 2, iz + 1):
            self.mob('stray_shadow', ix - 2, iz + 1, 'west')
        wx, wz = WORKS
        self.keep(wx - 2, 680, wx + 2, wz - WORKS_R)
        # ridges: each with a way round both its ends
        self.ridge(((50, 636), (56, 644), (55, 650)), 5, 1)
        self.ridge(((66, 633), (74, 638)), 4, 2)
        self.ridge(((84, 640), (90, 650), (88, 656)), 6, 3)
        self.ridge(((104, 646), (112, 652)), 5, 4)
        self.ridge(((60, 660), (68, 664), (74, 660)), 5, 5)
        self.ridge(((96, 670), (104, 668)), 4, 6)
        self.ridge(((66, 672), (70, 678)), 5, 7)
        self.ridge(((88, 678), (86, 684)), 4, 8)
        # the wheels of the clock, where they came down
        self.wheel(76, 646, 6, 'z', 0.42, salt=1)
        self.wheel(110, 660, 5, 'x', 0.5, salt=2)
        self.wheel(84, 664, 4, 'z', 0.55, salt=3)
        # those in the walls of the ridges, at the narrow places
        self.niche(55, 648, 'east')
        self.niche(89, 653, 'west')
        self.niche(67, 663, 'south')
        # the dead of it
        for kind, x, z, facing, tall in (('stray_shadow', 46, 640, 'west', 2), ('stray_shadow', 62, 648, 'north', 2), ('stray_shadow', 96, 646, 'west', 2),
                                         ('stray_shadow', 78, 670, 'north', 2), ('stray_shadow', 112, 668, 'west', 2),
                                         ('deepling', 60, 632, 'south', 3), ('deepling', 82, 636, 'west', 3), ('deepling', 100, 652, 'west', 3),
                                         ('deepling', 116, 658, 'north', 3), ('deepling', 76, 656, 'north', 3),
                                         ('handwalker', 70, 644, 'west', 2), ('handwalker', 94, 660, 'north', 2), ('handwalker', 108, 666, 'west', 2),
                                         ('handwalker', 80, 676, 'north', 2),
                                         ('usher', 100, 640, 'west', 4), ('gut', 120, 664, 'west', 3),
                                         ('clockhand', 75, 683, 'north', 3), ('clockhand', 81, 683, 'north', 3)):
            x, z = self.near(x, z, 0, tall)                        # (a mark is moved to the nearest free floor rather than left out)
            self.mob(kind, x, z, facing, tall)
        # what is to be found
        spot = self.near(119, 669, 1, 3, 4)
        b.loot(spot[0], PLATE + 1, spot[1], 'depths_water', 'north')
        self.keep(spot[0] - 1, spot[1] - 1, spot[0] + 1, spot[1] + 1)
        spot = self.near(58, 652, 0, 3, 4)
        b.shard(spot[0], PLATE + 1, spot[1])
        self.taken.add(spot)
        # crystals: by chance, but never in a way, and more of them towards the middle of the floor
        for (x, z) in sorted(cells):
            if hsh(x, 8, z, 1390) < 0.016 and self.free(x, z, 1):
                self.crystal(x, z, 1 + int(hsh(x, 9, z, 1391) * 3), 5)
        self.lamp_grid(cells | self.isle, 13)
        self.lamps(self.neck, 11)

    # ------------------------------------------------------------------ the Works

    def works(self):
        """The Works: the floor of the clock, round, with a rim like the rim of a wheel - and it is one. The pendulum
        swings across it from east to west, from an axle that lies over it north to south, its ends in the rock. Fog
        in the north; in the south, behind the Bolt, a seal."""
        b = self.b
        b.zone = 'clockworks'
        cx, cz = WORKS
        R = WORKS_R
        floor = self.works_floor
        for (x, z) in floor:
            far = math.hypot(x - cx, z - cz)
            ang = math.atan2(z - cz, x - cx)
            # rings and spokes in the floor, of the Founder's copper; and the line the pendulum keeps to
            if abs(far - 6.5) < 0.5 or abs(far - 11.5) < 0.5:
                b.set(x, PLATE, z, 'minecraft:waxed_cut_copper' if hsh(x, 0, z, 1400) < 0.8 else 'minecraft:waxed_exposed_cut_copper')
            elif far > 2 and far < R - 1 and abs(math.sin(ang * 4)) < 0.11 / max(1.0, far / 6):
                b.set(x, PLATE, z, 'minecraft:netherite_block' if hsh(x, 0, z, 1400) < 0.7 else 'minecraft:polished_deepslate')
            if z == cz and abs(x - cx) <= 9:
                b.set(x, PLATE, z, 'minecraft:netherite_block' if abs(x - cx) % 3 else 'minecraft:waxed_copper_grate')
            # the rim: two high, and a tooth higher at every third step round it
            if far > R - 1.2:
                if abs(x - cx) <= 1 and (z < cz - R + 3 or z > cz + R - 3):
                    continue                                       # (the two ways through it)
                tooth = int((ang + math.pi) / (2 * math.pi) * 30) % 3 == 0
                for h in range(1, 4 if tooth else 3):
                    b.set(x, PLATE + h, z, 'minecraft:polished_blackstone_bricks' if h < 3 else 'minecraft:polished_blackstone')
                self.taken.add((x, z))
        b.set(cx, PLATE, cz, 'minecraft:ochre_froglight[axis=y]')    # the seal under where it hangs
        self.arch(cx - 1, cx + 1, cz - R - 1, PLATE, 'bolt_fog')
        self.arch(cx - 1, cx + 1, cz + R + 1, PLATE, 'bolt', sealed=True)
        b.threshold('clockworks', cx - 1, PLATE + 1, cz - R - 4, cx + 1, cz - R - 3)
        # the axle over it, from wall to wall: what the pendulum hangs from
        top = PLATE + 13
        for z in range(cz - R - 1, cz + R + 2):
            if self.open(cx, top, z):
                b.set(cx, top, z, 'minecraft:netherite_block' if (z - cz) % 4 else 'minecraft:waxed_cut_copper')
            if abs(z - cz) <= 1:
                for dx in (-1, 1):
                    b.set(cx + dx, top, z, 'minecraft:polished_blackstone')
        # chains down out of the sky, with nothing on most of them
        for (dx, dz, low) in ((-9, -6, 7), (8, -8, 9), (-7, 9, 6), (10, 5, 8), (-11, 2, 10), (4, 11, 7)):
            x, z = cx + dx, cz + dz
            if (x, z) not in self.shaft:
                continue
            for y in range(PLATE + low, self.open_sky[(x, z)]):
                b.set(x, y, z, 'minecraft:iron_chain[axis=y,waterlogged=false]')
            b.set(x, PLATE + low - 1, z, 'minecraft:netherite_block' if low % 2 else 'minecraft:polished_blackstone')
        # two great wheels standing behind the rim, east and west, half in the rock: seen across the floor
        self.wheel(cx - R - 3, cz, 8, 'x', 0.35, y=PLATE - 3, salt=4)
        self.wheel(cx + R + 3, cz, 7, 'x', 0.3, y=PLATE - 2, salt=5)
        self.mob('bolt', cx, cz, 'south', 3)
        self.lamp_grid(floor, 15, up=5)
        self.lamps(self.works_out, 11)
        self.keep(cx - 2, cz - R - 2, cx + 2, cz + R + 4)

    def behind(self):
        """The chamber behind the pendulum: a hearth, an anvil, what the Bolt kept."""
        b = self.b
        cx, cz = BEHIND
        b.zone = 'clockworks'
        for (fx, fy, fz) in self.flight:
            self.keep(fx - 1, fz, fx + 1, fz)
        b.hearth(cx - 3, HIGH + 1, cz, 'works')
        b.set(cx - 3, HIGH + 1, cz + 2, f'{MOD}:forge')
        b.loot(cx + 3, HIGH + 1, cz - 1, 'depths_works', 'west')
        self.keep(cx - 4, cz - 1, cx - 2, cz + 3)
        self.keep(cx + 2, cz - 2, cx + 4, cz)
        self.crystal(cx + 3, cz + 3, 3, 6)
        self.crystal(cx - 1, cz + 5, 2, 7)
        self.lamp_grid(self.behind_cells, 13, every=3)
        self.lamps({(f[0] + dx, f[2]) for f in self.flight for dx in (-1, 0, 1)}, 11, every=2)
        self.keep(cx - 1, self.flight[-1][2], cx + 1, cz - 4)
        self.keep(cx - 6, cz - 1, cx - 4, cz + 1)

    # ------------------------------------------------------------------ the Causeways

    def causeways(self):
        """From the chamber behind the pendulum west, round and back east to the Tear: a thin way, a wide winding one,
        round chambers of rock, and the long way east."""
        b = self.b
        b.zone = 'causeways'
        bx, bz = BEHIND
        cells = self.causeway_cells
        for way, (a, c, bend, salt) in zip(self.ways, (((bx - 5, bz), ISLETS['mid'][:2], 1.5, 1), (ISLETS['mid'][:2], (ISLETS['west'][0] + 5, ISLETS['west'][1] - 2), -2.5, 2),
                                                       ((ISLETS['west'][0] + 2, ISLETS['west'][1] + 5), (ISLETS['spire'][0] - 3, ISLETS['spire'][1]), 2.0, 3),
                                                       ((ISLETS['spire'][0] + 3, ISLETS['spire'][1]), (LAST[0] - 3, LAST[1]), -1.0, 4),
                                                       ((LAST[0] + 3, LAST[1]), (ISLETS['wheel'][0] - 5, ISLETS['wheel'][1] + 1), 2.6, 5))):
            self.taken |= self.way(a, c, 0, bend, salt) & way       # (the middle of every way is kept clear)
        self.taken |= self.bridge
        b.threshold('causeways', bx - 8, HIGH + 1, bz - 1, bx - 7, bz + 1)
        # the middle chamber: two of the clock's own, at a wheel
        mx, mz = ISLETS['mid'][:2]
        self.wheel(mx, mz - 4, 3, 'z', 0.3, salt=6)
        for dx in (-2, 2):
            self.mob('clockhand', *self.near(mx + dx, mz + 2, 0, 3), 'east', 3)
        # the west chamber: a gut's
        wx, wz = ISLETS['west'][:2]
        self.ridge(((wx - 5, wz - 4), (wx - 2, wz - 6)), 5, 9)
        self.mob('gut', *self.near(wx - 1, wz + 1, 0, 3), 'east', 3)
        self.crystal(wx + 3, wz - 5, 3, 8)
        self.crystal(wx - 5, wz + 2, 2, 9)
        if self.free(wx + 4, wz + 3):
            self.mob('stray_shadow', wx + 4, wz + 3, 'north')
        # the cache: one cell of rock over a pit to get to, and worth it
        kx, kz = ISLETS['cache'][:2]
        b.loot(kx, HIGH + 1, kz + 1, 'depths_causeway', 'north')
        self.keep(kx - 1, kz, kx + 1, kz + 2)
        self.crystal(kx - 2, kz - 1, 2, 10)
        b.shard(kx + 2, HIGH + 1, kz)
        self.taken.add((kx + 2, kz))
        # the spire: a tooth of rock in its chamber, and something in it
        sx, sz = ISLETS['spire'][:2]
        self.ridge(((sx - 1, sz - 4), (sx + 1, sz - 3)), 8, 10, half=1)
        self.niche(sx, sz - 2, 'south')
        self.mob('handwalker', *self.near(sx + 3, sz + 2, 0, 2), 'west')
        self.mob('deepling', *self.near(sx - 3, sz + 3, 0, 3), 'east', 3)
        # the hearth by the Tear
        lx, lz = LAST
        b.zone = 'tear'
        b.hearth(lx - 3, HIGH + 1, lz - 2, 'tear')
        self.keep(lx - 4, lz - 3, lx - 2, lz - 1)
        self.crystal(lx + 3, lz - 3, 3, 11)
        b.zone = 'causeways'
        # the east chamber: one of the clock's own keeping a wheel that does not turn; what it kept
        ex, ez = ISLETS['wheel'][:2]
        self.wheel(ex + 2, ez - 2, 5, 'x', 0.4, salt=7)
        self.mob('clockhand', *self.near(ex - 3, ez - 1, 0, 3), 'west', 3)
        spot = self.near(ex, ez + 5, 1, 3, 4)
        b.loot(spot[0], HIGH + 1, spot[1], 'depths_ledge', 'north')
        self.keep(spot[0] - 1, spot[1] - 1, spot[0] + 1, spot[1] + 1)
        # on the long ways: one that walks them, one that waits at the turn; and those that hang over the stubs
        for kind, x, z, facing, tall in (('usher', 76, 759, 'west', 4), ('handwalker', 44, 738, 'east', 2), ('stray_shadow', 84, 757, 'west', 2),
                                         ('deepling', 40, 751, 'east', 3), ('deepling', 72, 761, 'north', 3)):
            self.mob(kind, *self.near(x, z, 0, tall), facing, tall)
        for (x, z) in sorted(cells):                                # crystals: few, small, in the chambers and at the walls of the wide ways
            if hsh(x, 8, z, 1392) < 0.012 and self.free(x, z, 1):
                self.crystal(x, z, 1 + int(hsh(x, 9, z, 1393) * 2), 12)
        self.lamps(cells, 12, every=2)

    # ------------------------------------------------------------------ the Tear

    def tear(self):
        """The Tear: a round hollow of black rock, still water in it, and in the middle of the water what has come
        through. Fog in the north; in the south, behind the Coming, a seal, and the room beyond."""
        b = self.b
        b.zone = 'tear'
        cx, cz = TEAR
        R = TEAR_R
        floor = self.tear_floor
        for (x, z) in floor:
            far = math.hypot(x - cx, z - cz)
            if far > R:
                if abs(x - cx) <= 1:
                    self.wet.discard((x, z))
                    b.set(x, HIGH, z, black_rock(x, HIGH, z))
                    continue                                       # (the two ways through the rim)
                # the rim: black rock in teeth, three high and more
                top = 3 + int(smooth(x, z, 2, 1410) * 3) + (1 if far > R + 1.2 else 0)
                for h in range(1, top + 1):
                    b.set(x, HIGH + h, z, black_rock(x, HIGH + h, z) if hsh(x, h, z, 1411) < 0.93 else 'minecraft:crying_obsidian')
                self.taken.add((x, z))
                self.wet.discard((x, z))
            elif far < 3.2:
                # the tear itself: under where it stands the floor is the same black, and wet with something
                b.set(x, HIGH, z, 'minecraft:crying_obsidian' if far > 2.0 and hsh(x, 0, z, 1412) < 0.6 else 'minecraft:obsidian')
        # ribs: the rock of the rim stands up round the hollow and leans in over it, as over something held
        for k in range(10):
            a = k * math.pi / 5 + math.pi / 10
            if abs(math.cos(a)) < 0.35:
                continue                                           # (not over the two ways in)
            tall = 13 + int(hsh(k, 0, 0, 1413) * 5)
            for h in range(4, tall + 1):
                lean = (h / tall) ** 2 * 6.5
                far = R + 1.4 - lean
                wide = 1 if h < tall - 3 else 0
                px, pz = cx + math.cos(a) * far, cz + math.sin(a) * far
                for dx in range(-wide, wide + 1):
                    for dz in range(-wide, wide + 1):
                        x, z = int(round(px)) + dx, int(round(pz)) + dz
                        if self.open(x, HIGH + h, z):
                            b.set(x, HIGH + h, z, black_rock(x, HIGH + h, z) if h < tall - 1 or dx or dz else 'minecraft:crying_obsidian')
        self.arch(cx - 1, cx + 1, cz - R - 1, HIGH, 'coming_fog', height=4)
        self.arch(cx - 1, cx + 1, cz + R + 1, HIGH, 'coming', sealed=True, height=4)
        b.threshold('tear', cx - 1, HIGH + 1, cz - R - 5, cx + 1, cz - R - 4)
        self.mob('coming', cx, cz, 'north', 8)
        # crystals inside the rim, far apart: the light it is fought by
        for k in range(8):
            a = k * math.pi / 4 + 0.39
            self.crystal(int(round(cx + math.cos(a) * (R - 1.5))), int(round(cz + math.sin(a) * (R - 1.5))), 3, 20 + k)
        self.lamp_grid({c for c in floor if math.hypot(c[0] - cx, c[1] - cz) < R}, 15, up=6)
        # the room beyond: the works of the clock itself, lying where they were left. (What is done with them comes later.)
        ex, ez = END
        for dx in range(-2, 3):
            for dz in range(-2, 3):
                far = math.hypot(dx, dz)
                if far <= 2.4:
                    b.set(ex + dx, HIGH, ez + 1 + dz, 'minecraft:waxed_cut_copper' if far > 1.5 else ('minecraft:ochre_froglight[axis=y]' if far < 0.5 else 'minecraft:netherite_block'))
        b.loot(ex + 3, HIGH + 1, ez, 'depths_tear', 'west')
        b.shard(ex - 3, HIGH + 1, ez + 1)
        self.keep(ex - 4, ez - 3, ex + 4, ez + 4)
        self.lamp_grid(self.end, 13, every=3)
        self.lamps(self.tear_north | self.tear_south, 11, every=2)

    def film(self):
        """The still water, wherever a floor is to have it and nothing stands."""
        b = self.b
        for (x, z) in self.wet:
            y = self.ground[(x, z)]
            if self.open(x, y + 1, z):
                b.set(x, y + 1, z, FILM)

    # ------------------------------------------------------------------ all of it

    def build(self, height=None):
        b = self.b
        b.zone = 'under_sky'
        plain = b.set

        def record(x, y, z, state):
            c = (x, y, z)
            if c not in self.own:
                if b.cells.get(c) is not None and z > GATE[1] + 1:
                    self.met.append(c)
                self.own.add(c)
            plain(x, y, z, state)
        b.set = record                                             # (everything the place writes is known: for its skin, and for what it runs into)
        try:
            self.drift_plan()
            self.layout()
            self.carve()
            self.drift()
            self.skies()
            self.brink()
            self.water()
            self.works()
            self.behind()
            self.causeways()
            self.tear()
            self.film()
        finally:
            del b.set
        if self.met:
            print(f'  PROBLEM: the Sky Beneath cuts through {len(self.met)} cells of something else, as {self.met[0]}: {b.get(*self.met[0])}')
        self.skin()

    def biome_columns(self):
        """The columns that get the place's biome (under the ground only: gen_world.py gives the layers): everything
        that is open, a little round it, and the drift from its cross leg on."""
        cols = set()
        for (x, z) in list(self.ground) + list(self.pits) + [c for c in self.cut if c[1] >= TURN_Z - 2]:
            for dx in (-6, 0, 6):
                for dz in (-6, 0, 6):
                    cols.add((x + dx, z + dz))
        return {c for c in cols if c[0] >= 0 and c[1] >= 0}

    def views(self):
        """Pictures of the place for the client test: name, eye, what it looks at (plan coordinates)."""
        gx, gz = GATE
        lx, lz = LEDGE
        wx, wz = WORKS
        bx, bz = BEHIND
        tx, tz = TEAR
        p, h = PLATE, HIGH
        cross = self.cut[(gx + 6, TURN_Z)][0]
        return [
            ('depths_grate_from_the_sump', (gx + 0.5, SUMP_FLOOR + 2.6, gz - 5.5), (gx + 0.5, SUMP_FLOOR + 2.6, gz + 2)),
            ('depths_first_flight', (gx + 0.5, SUMP_FLOOR + 2.6, gz + 2.5), (gx + 0.5, SUMP_FLOOR - 8, gz + 14)),
            ('depths_flight_from_below', (gx + 0.5, self.cut[(gx, gz + 22)][0] + 2.6, gz + 22.5), (gx + 0.5, SUMP_FLOOR - 4, gz + 8)),
            ('depths_cross_leg', (gx + 2.5, cross + 2.6, TURN_Z + 0.5), (LEG_X, cross + 2.6, TURN_Z + 0.5)),
            ('depths_raw_rock', (LEG_X + 0.5, self.cut[(LEG_X, RAW_Z - 3)][0] + 2.6, RAW_Z - 2.5), (LEG_X + 0.5, BRINK + 2, ROOM_Z)),
            ('depths_hearth_room', (LEG_X + 0.5, BRINK + 2.6, ROOM_Z - 2.5), (LEG_X - 2, BRINK + 1.4, ROOM_Z + 1)),
            ('depths_out_of_the_drift', (LEG_X + 0.5, BRINK + 2.6, MOUTH_Z - 2.5), (LEG_X + 6, BRINK + 2, lz + 30)),
            ('depths_from_the_brink', (lx + 0.5, BRINK + 2.6, lz + 3.5), (wx, p + 2, wz - 40)),
            ('depths_brink_east', (lx + 2.5, BRINK + 2.6, lz + 0.5), (120, p + 6, 650)),
            ('depths_brink_up', (lx + 0.5, BRINK + 2.6, lz + 2.5), (lx + 20, BRINK + 34, lz + 26)),
            ('depths_steps', (self.steps_end[0] + 3.5, p + 2.6, self.steps_end[1] + 0.5), (lx, BRINK + 2, lz)),
            ('depths_still_water', (61.5, p + 2.6, 629.5), (84, p + 3, 652)),
            ('depths_first_wheel', (66.5, p + 2.6, 640.5), (76, p + 5, 646)),
            ('depths_water_middle', (78.5, p + 2.6, 654.5), (104, p + 3, 660)),
            ('depths_water_sky', (80.5, p + 2.6, 652.5), (86, p + 30, 656)),
            ('depths_water_sky_aslant', (60.5, p + 2.6, 640.5), (100, p + 24, 660)),
            ('depths_water_from_above', (62, p + 14, 672), (84, p, 652)),
            ('depths_pit', (64.5, p + 2.6, 648.5), (68, p - 8, 652)),
            ('depths_the_jump', (self.jump[0] - 2.5, p + 2.6, self.jump[1] + 0.5), (ISLE[0], p + 2, ISLE[1])),
            ('depths_water_wall', (106.5, p + 2.6, 668.5), (124, p + 6, 672)),
            ('depths_to_the_works', (wx + 0.5, p + 2.6, 680.5), (wx, p + 3, wz)),
            ('depths_works_from_the_door', (wx + 0.5, p + 2.6, wz - WORKS_R + 2.5), (wx, p + 6, wz + 4)),
            ('depths_works_from_above', (wx - 8, p + 14, wz - 8), (wx + 2, p + 1, wz + 3)),
            ('depths_works_axle', (wx + 8.5, p + 2.6, wz + 9.5), (wx, p + 13, wz)),
            ('depths_works_sky', (wx + 6.5, p + 2.6, wz - 6.5), (wx - 2, p + 30, wz + 2)),
            ('depths_works_wheel', (wx - 4.5, p + 2.6, wz + 0.5), (wx - WORKS_R - 3, p + 4, wz)),
            ('depths_stair_behind', (wx + 0.5, p + 2.6, wz + WORKS_R + 3.5), (bx, h + 2, bz)),
            ('depths_behind_the_pendulum', (bx + 0.5, h + 2.6, bz - 4.5), (bx - 3, h + 1.5, bz + 1)),
            ('depths_thin_way', (bx - 5.5, h + 2.6, bz + 0.5), (56, h + 2, 739)),
            ('depths_winding_way_west', (53.5, h + 2.6, 739.5), (36, h + 2, 742)),
            ('depths_west_chamber', (36.5, h + 2.6, 742.5), (26, h + 2, 746)),
            ('depths_way_to_the_cache', (27.5, h + 2.6, 748.5), (20, h - 1, 757)),
            ('depths_spire', (41.5, h + 2.6, 756.5), (48, h + 5, 756)),
            ('depths_long_way_east', (LAST[0] + 4.5, h + 2.6, LAST[1] + 0.5), (90, h + 2.5, 757)),
            ('depths_side_pit', (76.5, h + 2.6, 760.5), (SIDE_PIT[0], h - 6, SIDE_PIT[1])),
            ('depths_east_chamber', (90.5, h + 2.6, 755.5), (98, h + 4, 752)),
            ('depths_hearth_by_the_tear', (LAST[0] + 2.5, h + 2.6, LAST[1] + 2.5), (LAST[0] - 3, h + 1.5, LAST[1] - 2)),
            ('depths_before_the_tear', (tx + 0.5, h + 2.6, tz - TEAR_R - 5.5), (tx, h + 6, tz)),
            ('depths_tear_from_the_door', (tx + 0.5, h + 2.6, tz - TEAR_R + 1.5), (tx, h + 7, tz)),
            ('depths_tear_from_above', (tx + 11, h + 15, tz - 11), (tx - 2, h + 2, tz + 2)),
            ('depths_tear_up', (tx + 8.5, h + 2.6, tz + 9.5), (tx - 6, h + 30, tz - 6)),
            ('depths_beyond', (END[0] + 0.5, h + 2.6, END[1] - 3.5), (END[0], h + 1, END[1] + 2)),
        ]

    def probes(self):
        return [('depths_drift', (GATE[0], SUMP_FLOOR + 1, GATE[1] + 2)), ('depths_room', (LEG_X, BRINK + 1, ROOM_Z + 1)),
                ('depths_brink', (LEDGE[0], BRINK + 1, LEDGE[1] + 2)), ('depths_water', (64, PLATE + 1, 644)), ('depths_works', (WORKS[0] + 5, PLATE + 1, WORKS[1] + 5)),
                ('depths_behind', (BEHIND[0], HIGH + 1, BEHIND[1])), ('depths_way', (46, HIGH + 1, 739)), ('depths_tear', (TEAR[0] + 6, HIGH + 1, TEAR[1] + 6)),
                ('depths_veil', (WORKS[0], ROOMS['works'] - 2, WORKS[1])), ('depths_under_the_lid', (WORKS[0], ROOMS['works'] - 1, WORKS[1]))]
