#!/usr/bin/env python3
"""The Sky Beneath: the map's eighth place and its last with anything to fight in it, built into the world's Map by
gen_world.py (it has no plan of its own).

South of the White Pit, between it and the sea, a hill of black rock goes down into the water as a cape. It is hollow.
Inside it is where the day that was not let begin has been kept - and that day has an hour, the one after sunset, and a
sky: the roof and the far walls of the hollow are blocks that are drawn as a sky (UnderSkyBlock), black overhead with
stars, a dark blue low down; and so is its bottom. What can be stood on hangs between the two: plates of black rock
with a hand's depth of still water on them, stars in the water, and nothing under their edges. Whoever steps off is
gone. The only light is the crystals, and it is violet.

The way in is the Lower Grate, in the south wall of the quarry's sump (a hearth); its key lies in the granary behind the
Hollow Miller. (The two barred ways the quarry had before lead north, under the fields, where the ground is sixteen
blocks deep: there is no room for a sky there.)

    the Lower Grate -> a drift that climbs, turns twice (the hour is turned in its cross leg) and stops being a drift
    -> a hearth in the last of the rock -> the Brink: a ledge over all of it -> down to the Still Water, the great
    plate: ridges, crystals, the wheels of a clock as big as a hall, half sunk; a lesser plate off its east side that
    is jumped to -> a neck -> the Works: the round floor the pendulum swings over (the Bolt; a seal behind it) -> a
    stair up -> a hearth behind the pendulum -> the Causeways: narrow ways over the sky from islet to islet, west,
    round and back east -> a hearth by the Tear -> the Tear: a bowl of black rock and still water (the Coming, in
    the middle of it; a seal behind it) -> the ledge beyond, where the clock's own works are, for what comes later.

Levels: the bottom of the sky is layer 1; the Still Water and the Works are PLATE, the Brink BRINK, the Causeways and
the Tear HIGH. Nothing here is a way up or out: the plates have nothing round them but air, the rims of the two lords'
floors are higher than a thrown man goes, and the hill outside is sheer on every side and in the sea on three."""
import math

from gen_map import BARS, hsh, stairs
from gen_structures import AIR, MOD

SKY = MOD + ':under_sky[tone=%d]'
FILM = MOD + ':night_shallows'
TONES = 12                                   # see UnderSkyBlock
VOID = 1                                     # the bottom of the sky
PLATE = 10                                   # the floor block of the Still Water and of the Works
BRINK = 14                                   # of the ledge the drift comes out on
HIGH = 16                                    # of the Causeways and the Tear
HORIZON = 15                                 # the layer the sky is lightest at
WALL = 7                                     # how much rock there is between the hollow and the outside, at the least
GATE = (17, 540)                             # the Lower Grate: its middle cell, in the south wall of the sump
SUMP_FLOOR = 3                               # (Quarry.LOW)
TURN_Z = 577                                 # the drift's cross leg
LEG_X = 30                                   # and the line its last leg runs south on
ROOM_Z = 602                                 # the hearth in the last of the rock: its middle
MOUTH_Z = 610                                # where the drift ends
LEDGE = (30, 617)                            # the Brink
WORKS = (78, 706)                            # the Works: the middle of the floor the pendulum swings over
WORKS_R = 15
BEHIND = (78, 740)                           # the hearth behind the pendulum
TEAR = (62, 784)                             # the Tear: its middle
TEAR_R = 18
LAST = (62, 757)                             # the hearth by the Tear
END = (62, 808)                              # the ledge beyond it
# the Still Water: round these the great plate lies (x, z, how far), and here it has holes
WATER = ((44, 632, 9), (58, 640, 15), (80, 648, 17), (104, 654, 14), (118, 664, 9), (92, 668, 13), (72, 664, 11), (78, 678, 8))
HOLES = ((68, 652, 3.6), (99, 661, 3.0), (52, 646, 2.4), (86, 672, 2.2))
ISLE = (128, 646, 5.5)                       # the lesser plate off its east side: jumped to
# the Causeways: the islets (x, z, how far) and the ways between them (from, to, half-width)
ISLETS = {'mid': (56, 739, 4.2), 'west': (31, 744, 7.5), 'cache': (20, 758, 3.4), 'spire': (48, 758, 5.2), 'wheel': (96, 754, 7.5)}


def smooth(x, z, scale, salt):
    fx, fz = x / scale, z / scale
    x0, z0 = math.floor(fx), math.floor(fz)
    tx, tz = fx - x0, fz - z0
    tx, tz = tx * tx * (3 - 2 * tx), tz * tz * (3 - 2 * tz)
    a, b_ = hsh(x0, 0, z0, salt), hsh(x0 + 1, 0, z0, salt)
    c, d = hsh(x0, 0, z0 + 1, salt), hsh(x0 + 1, 0, z0 + 1, salt)
    return (a * (1 - tx) + b_ * tx) * (1 - tz) + (c * (1 - tx) + d * tx) * tz


def black_rock(x, y, z):
    """The hill, and whatever of the place is bare rock."""
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


def massif(x, z):
    """How far inside the black hill a column is, roughly (negative: outside it). The land part of it is a block with
    the corners off; the cape is a long round."""
    wob = (smooth(x, z, 13, 1310) - 0.5) * 7
    land = -max(7 - x, x - 150, 599 - z, z - 730)
    cape = (1.0 - math.hypot((x - 62) / 60.0, (z - 742) / 78.0)) * 60.0
    return max(land, cape) + wob


def sky_tone(y):
    """Which piece of sky a wall is at a height: lightest at the horizon, black two dozen layers over it and a dozen under."""
    return int(max(0, min(TONES - 1, round(TONES - 1 - abs(y - HORIZON) * (0.5 if y > HORIZON else 0.85)))))


def roof_tone(x, z):
    """Overhead it is black, but for one band across it that is a little lighter: as the sky has."""
    band = math.exp(-((x * 0.8 + (z - 700) * 0.55 - 78) / 20.0) ** 2) * (0.35 + 0.9 * smooth(x, z, 8, 1320))
    return int(max(0, min(3, round(band * 3.0))))


class Depths:
    def __init__(self, b, gy):
        self.b, self.g = b, gy
        self.mask = {}                   # every column of the hill: how deep inside it it is, in columns (1 at its edge)
        cols = {(x, z) for x in range(4, 160) for z in range(590, 826) if massif(x, z) > 0}
        ring = [c for c in cols if any((c[0] + dx, c[1] + dz) not in cols for dx in (-1, 0, 1) for dz in (-1, 0, 1))]
        for c in ring:
            self.mask[c] = 1
        d = 1
        while ring:
            d += 1
            grown = []
            for (x, z) in ring:
                for dx in (-1, 0, 1):
                    for dz in (-1, 0, 1):
                        n = (x + dx, z + dz)
                        if n in cols and n not in self.mask:
                            self.mask[n] = d
                            grown.append(n)
            ring = grown
        self.hollow = {c for c, d in self.mask.items() if d > WALL}
        self.ground = {}                 # every column that can be stood on: the layer of its floor block
        self.wet = set()                 # and those of them the still water lies on
        self.taken = set()               # columns something stands on or a way runs over: nothing more is put there
        self.lit = []                    # where the place's own light is (crystals): for the pictures
        self.mobs = True

    # ------------------------------------------------------------------ the hill

    def inside(self, x, z):
        return (x, z) in self.mask

    def top(self, x, z):
        """How high the hill stands at a column: a sheer face at its edge, then up to a back."""
        d = self.mask[(x, z)]
        return int(self.g + 14 + min(d, 13) * 2.2 + smooth(x, z, 9, 1311) * 4 + (smooth(x, z, 4, 1312) - 0.5) * 2)

    def ceiling(self, x, z):
        """The layer of the sky over a column of the hollow."""
        return self.top(x, z) - 5

    def hill(self, height):
        """The hill itself, solid from the bottom of the world's stone to its back; then the hollow in it, which is
        sky at the bottom, sky at the top and sky round the sides."""
        b = self.b
        b.undertow = {c for c in b.undertow if (c[0], c[2]) not in self.mask}     # (the cape stands where the sea was)
        for (x, z), d in self.mask.items():
            top = self.top(x, z)
            if (x, z) in self.hollow:
                # only what will be seen from outside, and a skin over the sky: the rest is taken out below
                roof = self.ceiling(x, z)
                b.set(x, VOID, z, SKY % 0)
                for y in range(VOID + 1, roof):
                    b.set(x, y, z, AIR)
                # (the sky is five thick: where the roof steps up, what is seen in the step is more sky, not the rock over it)
                for y in range(roof, top):
                    b.set(x, y, z, SKY % roof_tone(x, z))
                b.set(x, top, z, black_rock(x, top, z))
            else:
                wall = d >= WALL - 1                               # the two columns of rock next the hollow are sky on their inner side
                for y in range(1, top + 1):
                    b.set(x, y, z, SKY % (sky_tone(y) if y <= top - 5 else roof_tone(x, z)) if wall and y < top else black_rock(x, y, z))
            # its back: bare, a little gravel in the hollows of it
            if hsh(x, 0, z, 1313) < 0.16:
                b.set(x, top, z, 'minecraft:gravel' if hsh(x, 1, z, 1313) < 0.6 else 'minecraft:coarse_dirt')
            if hsh(x, 2, z, 1314) < 0.012 and d > 2:
                b.set(x, top + 1, z, 'minecraft:dead_bush')
            height[(x, z)] = 0                                    # (nothing of the world's grows on it: see gen_world.woods)

    # ------------------------------------------------------------------ what is stood on

    def plate(self, cells, y, mat=None, wet=True, thick=5):
        """A plate of rock hanging in the hollow, its top at layer y: thin at its edge and thicker inwards, like the
        underside of a floe. `cells` is a set of columns; `mat(x, y, z)` its top (the black of the still water if none)."""
        b = self.b
        cells = {c for c in cells if c in self.hollow}
        edge = {c: 1 for c in cells if any((c[0] + dx, c[1] + dz) not in cells for dx, dz in ((1, 0), (-1, 0), (0, 1), (0, -1)))}
        ring = list(edge)
        d = 1
        while ring and d < thick:
            d += 1
            grown = []
            for (x, z) in ring:
                for dx, dz in ((1, 0), (-1, 0), (0, 1), (0, -1)):
                    n = (x + dx, z + dz)
                    if n in cells and n not in edge:
                        edge[n] = d
                        grown.append(n)
            ring = grown
        for (x, z) in cells:
            deep = edge.get((x, z), thick) + (1 if hsh(x, 3, z, 1330) < 0.4 else 0)
            for yy in range(max(VOID + 2, y - deep), y):
                b.set(x, yy, z, black_rock(x, yy, z))
            b.set(x, y, z, (mat or glass_rock)(x, y, z))
            for yy in range(y + 1, y + 4):
                if b.get(x, yy, z) not in (None, AIR) and 'under_sky' not in str(b.get(x, yy, z)):
                    continue
                b.set(x, yy, z, AIR)
            self.ground[(x, z)] = y
            if wet:
                self.wet.add((x, z))
            else:
                self.wet.discard((x, z))
        return cells

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
                if not all(self.b.free(c[0], self.ground[c] + h, c[1]) for h in range(1, height + 1)):
                    return False
        return True

    def mob(self, kind, x, z, facing='south', tall=2):
        b = self.b
        if (x, z) not in self.ground:
            raise ValueError(f'the {kind} at {(x, z)} would stand on nothing')
        y = self.ground[(x, z)]
        if not all(b.free(x, y + h, z) for h in range(1, tall + 1)):
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
        if not all(b.free(x, y + h, z) for h in range(1, tall + 3)):
            return False
        b.set(x, y + 1, z, 'minecraft:calcite' if hsh(x, 4, z, 1350 + salt) < 0.5 else 'minecraft:smooth_basalt')
        for h in range(2, tall + 1):
            b.set(x, y + h, z, 'minecraft:amethyst_block')
        b.set(x, y + tall + 1, z, 'minecraft:amethyst_cluster[facing=up,waterlogged=false]')
        b.set(x, y + tall + 2, z, 'minecraft:light[level=10,waterlogged=false]')
        for dx, dz, facing in ((1, 0, 'east'), (-1, 0, 'west'), (0, 1, 'south'), (0, -1, 'north')):
            if tall >= 2 and hsh(x + dx, 5, z + dz, 1351 + salt) < 0.6 and b.free(x + dx, y + 2, z + dz) and (x + dx, z + dz) not in self.taken:
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
                if b.get(x, yy, z) not in (None, AIR) and 'night_shallows' not in str(b.get(x, yy, z)):
                    continue
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
                    if b.free(c[0], y + h, c[1]):
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
                    if not b.free(c[0], y + h, c[1]) and 'mob_mark' not in str(b.get(c[0], y + h, c[1])):
                        b.set(c[0], y + h, c[1], AIR)
                self.taken.add(c)

    def lamp_grid(self, cells, level, up=4, every=4):
        """Light without a lamp, a little of it, over a floor: the dark of the place is to be seen in."""
        b = self.b
        for (x, z) in cells:
            if x % every == 0 and z % every == 0 and (x, z) in self.ground:
                y = self.ground[(x, z)] + up
                if b.free(x, y, z):
                    b.set(x, y, z, f'minecraft:light[level={level},waterlogged=false]')

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

    # ------------------------------------------------------------------ the way in

    def drift(self, height):
        """From the Lower Grate: south, climbing; a cross leg east, in which the hour is turned; south again, and the
        cut stone gives out. A room with a hearth in the last of the rock, and the mouth."""
        b = self.b
        gx, gz = GATE
        # the middle line of it, with the layer of its floor at every cell
        path = []
        y = SUMP_FLOOR
        for z in range(gz, TURN_Z + 1):
            if z >= gz + 9 and (z - gz) % 3 == 0 and y < 12:
                y += 1
            path.append((gx, y, z, 'south'))
        for x in range(gx + 1, LEG_X + 1):
            path.append((x, y, TURN_Z, 'east'))
        for z in range(TURN_Z + 1, MOUTH_Z + 1):
            if z >= TURN_Z + 5 and (z - TURN_Z) % 4 == 1 and y < BRINK:
                y += 1
            path.append((LEG_X, y, z, 'south'))
        floor = {}
        for i, (x, y, z, way) in enumerate(path):
            for d in (-1, 0, 1):
                c = (x + d, z) if way == 'south' else (x, z + d)
                floor.setdefault(c, (y, way, i))
            if i and path[i - 1][3] != way or (i + 1 < len(path) and path[i + 1][3] != way):   # the corners, filled out
                for dx in (-1, 0, 1):
                    for dz in (-1, 0, 1):
                        floor.setdefault((x + dx, z + dz), (y, way, i))
        raw_from = TURN_Z + 14                                     # south of this nobody cut it
        met = [(x, y + h, z) for (x, z), (y, way, i) in floor.items() for h in range(0, 4)
               if z > gz + 1 and z < 596 and y + h <= self.g and b.get(x, y + h, z) is not None]      # (over the ground it is in the plateau's own rock)
        if met:
            print(f'  PROBLEM: the drift to the Sky Beneath cuts through {len(met)} cells of something else, as {met[0]}: {b.get(*met[0])}')
        for (x, z), (y, way, i) in floor.items():
            if z <= gz:
                continue
            rock = cut_rock if z < raw_from else black_rock
            prev = path[i - 1][1] if i else y
            b.set(x, y, z, rock(x, y, z))
            if y > prev and (x, z) == (path[i][0] + (0 if way != 'south' else x - path[i][0]), path[i][2]) and way == 'south':
                b.set(x, y, z, stairs('cobbled_deepslate', 'south'))
                b.flights.append((x, y, z))
            high = 3 if z < raw_from else 3 + int(smooth(x, z, 3, 1380) * 2)
            for h in range(1, high + 1):
                b.set(x, y + h, z, AIR)
            for h in range(high + 1, high + 3):
                if b.get(x, y + h, z) in (None,) or 'under_sky' in str(b.get(x, y + h, z)):
                    b.set(x, y + h, z, rock(x, y + h, z))
            if b.get(x, y - 1, z) is None:
                b.set(x, y - 1, z, rock(x, y - 1, z))
        # rock round it wherever the plan has none (under the ground it holds only what was cut; the check must see walls)
        for (x, z), (y, way, i) in floor.items():
            for dx in (-1, 0, 1):
                for dz in (-1, 0, 1):
                    n = (x + dx, z + dz)
                    if n in floor or n[1] < gz:
                        continue
                    for yy in range(y - 2, y + 7):
                        if b.get(n[0], yy, n[1]) is None or 'under_sky' in str(b.get(n[0], yy, n[1])):
                            b.set(n[0], yy, n[1], (cut_rock if n[1] < raw_from else black_rock)(n[0], yy, n[1]))
        # the grate: three wide, three high, in the sump's wall; a lamp over it on the sump's side
        gate = [(x, y, gz) for x in (gx - 1, gx, gx + 1) for y in (SUMP_FLOOR + 1, SUMP_FLOOR + 2, SUMP_FLOOR + 3)]
        for cell in gate:
            b.set(*cell, f'{MOD}:locked_gate[axis=x,lock=depths]')
        for x in (gx - 1, gx, gx + 1):
            b.set(x, SUMP_FLOOR, gz, cut_rock(x, SUMP_FLOOR, gz))
            b.set(x, SUMP_FLOOR + 4, gz, cut_rock(x, SUMP_FLOOR + 4, gz))
        b.seals['depths_key'] = gate
        # timber while it is a drift, a lamp now and then; after that crystals, small, and far apart
        for i, (x, y, z, way) in enumerate(path):
            if z <= gz + 1 or z >= raw_from:
                continue
            flat = 0 < i < len(path) - 1 and path[i - 1][1] == y == path[i + 1][1]
            if i % 7 in (3, 4) and flat and not (i % 7 == 4 and 0 < i - 1 and path[i - 2][1] == path[i - 1][1] == y):
                for d in (-1, 1):
                    c = (x + d, z) if way == 'south' else (x, z + d)
                    b.set(c[0], y + 1, c[1], 'minecraft:spruce_log[axis=y]')
                    b.set(c[0], y + 2, c[1], 'minecraft:spruce_log[axis=y]')
                for d in (-1, 0, 1):
                    c = (x + d, z) if way == 'south' else (x, z + d)
                    b.set(c[0], y + 3, c[1], 'minecraft:spruce_log[axis=%s]' % ('x' if way == 'south' else 'z'))
            if i % 14 == 9 and b.free(x, y + 2, z):
                b.set(x, y + 2, z, 'minecraft:soul_lantern[hanging=true]') if b.get(x, y + 3, z) not in (None, AIR) else None
        for i, (x, y, z, way) in enumerate(path):
            if z > gz + 1 and i % 5 == 2 and b.free(x, y + 3, z):
                b.set(x, y + 3, z, 'minecraft:light[level=11,waterlogged=false]')
        for z in range(raw_from + 2, MOUTH_Z - 9, 6):
            side = -1 if (z // 6) % 2 else 1
            y = floor[(LEG_X + side, z)][0]
            b.set(LEG_X + side, y + 1, z, 'minecraft:amethyst_cluster[facing=up,waterlogged=false]')
            b.set(LEG_X + side, y + 2, z, 'minecraft:light[level=7,waterlogged=false]')
        # the hour: towards the quarry night, towards the hollow its own
        b.threshold('to_night', gx - 1, floor[(gx, gz + 6)][0] + 1, gz + 6, gx + 1, gz + 7)
        tz = TURN_Z
        b.threshold('to_blue_hour', gx + 6, floor[(gx + 6, tz)][0] + 1, tz - 1, gx + 7, tz + 1)
        # the room: the last place with a wall on every side
        ry = floor[(LEG_X, ROOM_Z)][0]
        for x in range(LEG_X - 4, LEG_X + 5):
            for z in range(ROOM_Z - 3, ROOM_Z + 4):
                edge = abs(x - LEG_X) == 4 or abs(z - ROOM_Z) == 3
                if edge and hsh(x, 7, z, 1381) < 0.5 and not abs(x - LEG_X) <= 1:
                    continue
                y = floor.get((x, z), (ry,))[0]
                if abs(x - LEG_X) > 1:
                    y = ry
                    b.set(x, y, z, black_rock(x, y, z))
                    if b.get(x, y - 1, z) is None:
                        b.set(x, y - 1, z, black_rock(x, y - 1, z))
                for h in range(1, 5):
                    b.set(x, y + h, z, AIR)
                for h in (5, 6):
                    if 'under_sky' in str(b.get(x, y + h, z)) or b.get(x, y + h, z) is None:
                        b.set(x, y + h, z, black_rock(x, y + h, z))
        for x in range(LEG_X - 6, LEG_X + 7):                      # (and rock, not sky, round the room)
            for z in range(ROOM_Z - 5, ROOM_Z + 6):
                for yy in range(ry - 1, ry + 8):
                    if 'under_sky' in str(b.get(x, yy, z)) or b.get(x, yy, z) is None:
                        b.set(x, yy, z, black_rock(x, yy, z))
        b.hearth(LEG_X - 3, ry + 1, ROOM_Z, 'brink')
        b.loot(LEG_X + 3, ry + 1, ROOM_Z + 1, 'depths_brink', 'west')
        b.set(LEG_X + 3, ry + 1, ROOM_Z - 2, 'minecraft:amethyst_cluster[facing=up,waterlogged=false]')
        b.set(LEG_X + 3, ry + 3, ROOM_Z - 2, 'minecraft:light[level=12,waterlogged=false]')
        b.set(LEG_X - 3, ry + 4, ROOM_Z + 2, 'minecraft:light[level=12,waterlogged=false]')
        self.path = path
        self.drift_floor = floor
        return floor[(LEG_X, MOUTH_Z)][0]

    # ------------------------------------------------------------------ the Brink and the Still Water

    def brink(self, y_mouth):
        """Where the drift comes out: a ledge of the rock with all of the hollow before it, and steps down its east
        side to the water."""
        b = self.b
        lx, lz = LEDGE
        # the last of the drift is open to the sky of the place: its walls end
        ledge = self.blob(((lx, lz, 7.5), (lx, MOUTH_Z + 1, 3.5)), 1, 1.0)
        ledge = {c for c in ledge if c[1] >= MOUTH_Z - 1}
        self.plate(ledge, BRINK, black_rock, wet=False, thick=7)
        # (whatever the drift's mouth left between itself and the ledge)
        for x in range(lx - 2, lx + 3):
            for z in range(MOUTH_Z - 6, MOUTH_Z + 2):
                if (x, z) in self.hollow and (x, z) not in self.ground and abs(x - lx) <= 1:
                    self.plate({(x, z)}, BRINK, black_rock, wet=False, thick=7)
        b.threshold('under_sky', lx - 1, BRINK + 1, MOUTH_Z + 1, lx + 1, MOUTH_Z + 2)
        self.keep(lx - 1, MOUTH_Z - 2, lx + 1, lz + 2)
        # the steps: east off the ledge and down, a flight of rock on a spur of its own
        x, y = lx + 7, BRINK
        z = lz + 2
        spur = set()
        steps = []
        while y > PLATE:
            for _ in range(2):
                x += 1
                steps.append((x, y, z))
            y -= 1
        steps.append((x + 1, y, z))
        for (sx, sy, sz) in steps:
            for dz in (-1, 0, 1):
                self.plate({(sx, sz + dz)}, sy, black_rock, wet=False, thick=4)
                spur.add((sx, sz + dz))
        for c in spur:
            self.taken.add(c)
        self.steps_end = (steps[-1][0], steps[-1][2])
        self.crystal(lx - 5, lz + 1, 3, 1)
        self.crystal(lx + 4, lz - 4, 2, 2)
        self.lamp_grid(ledge | spur, 13)
        return ledge

    def water(self):
        """The Still Water: the great plate. Ridges across it, crystals, three wheels of the clock; the ways through
        it are between these, and there is more than one."""
        b = self.b
        b.zone = 'still_water'
        sx, sz = self.steps_end
        cells = self.blob(WATER + ((sx + 4, sz, 4.5),), 2)
        for hx, hz, hr in HOLES:
            cells -= self.blob(((hx, hz, hr),), 3, 0.8)
        cells -= {c for c in cells if c in self.ground}             # (not over the steps)
        self.plate(cells, PLATE)
        self.plate(self.line((sx, sz), (sx + 3, sz), 1), PLATE)
        b.threshold('still_water', sx + 1, PLATE + 1, sz - 1, sx + 2, sz + 1)
        self.keep(sx, sz - 1, sx + 4, sz + 1)
        # the lesser plate east of it: three cells of nothing between, and a chest for whoever jumps them
        ix, iz, ir = ISLE
        isle = self.blob(((ix, iz, ir),), 4, 0.7)
        gap_x = min(x for (x, z) in isle if z == iz) - 1
        isle -= {c for c in isle if c[0] <= gap_x}
        self.plate(isle, PLATE)
        # (the main plate is brought up square to the jump, three cells off)
        self.plate({(x, iz + dz) for x in range(gap_x - 8, gap_x - 2) for dz in (-1, 0, 1)}, PLATE)
        for x in range(gap_x - 2, gap_x + 1):
            for dz in (-1, 0, 1):
                self.ground.pop((x, iz + dz), None)
        self.jump = (gap_x - 3, iz)
        b.loot(ix + 2, PLATE + 1, iz, 'depths_island', 'west')
        self.keep(ix + 1, iz - 1, ix + 3, iz + 1)
        self.crystal(ix, iz - 3, 3, 3)
        self.crystal(ix - 1, iz + 3, 2, 4)
        if self.free(ix - 2, iz + 1):
            self.mob('stray_shadow', ix - 2, iz + 1, 'west')
        # the neck to the Works, and the doorway of fog in it
        wx, wz = WORKS
        neck = self.line((wx, 682), (wx, wz - WORKS_R - 1), 2)
        self.plate(neck, PLATE, works_floor, wet=False, thick=3)
        self.neck = neck
        return cells | isle

    def water_dress(self, cells):
        b = self.b
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
            if not self.free(x, z, 0, tall):
                # (a mark is moved to the nearest free floor rather than left out)
                found = None
                for r in range(1, 5):
                    for dx in range(-r, r + 1):
                        for dz in range(-r, r + 1):
                            if found is None and self.free(x + dx, z + dz, 0, tall):
                                found = (x + dx, z + dz)
                if found is None:
                    raise ValueError(f'no floor near {(x, z)} for the {kind}')
                x, z = found
            self.mob(kind, x, z, facing, tall)
        # what is to be found
        for (x, z, table, facing) in ((119, 669, 'depths_water', 'north'),):
            for r in range(0, 4):
                spot = next(((x + dx, z + dz) for dx in range(-r, r + 1) for dz in range(-r, r + 1) if self.free(x + dx, z + dz, 1)), None)
                if spot:
                    b.loot(spot[0], PLATE + 1, spot[1], table, facing)
                    self.keep(spot[0] - 1, spot[1] - 1, spot[0] + 1, spot[1] + 1)
                    break
            else:
                raise ValueError('no place for the chest of the Still Water')
        for (x, z) in ((58, 652),):
            spot = next(((x + dx, z + dz) for r in range(0, 4) for dx in range(-r, r + 1) for dz in range(-r, r + 1) if self.free(x + dx, z + dz)), None)
            b.shard(spot[0], PLATE + 1, spot[1])
            self.taken.add(spot)
        # crystals: by chance, but never in a way, and more of them towards the middle of the plate
        for (x, z) in sorted(cells):
            if hsh(x, 8, z, 1390) < 0.016 and self.free(x, z, 1):
                self.crystal(x, z, 1 + int(hsh(x, 9, z, 1391) * 3), 5)
        self.lamp_grid(cells | self.neck, 13)

    # ------------------------------------------------------------------ the Works

    def works(self):
        """The Works: the floor of the clock, round, with a rim like the rim of a wheel - and it is one. The pendulum
        swings across it from east to west, from an axle that lies over it north to south. Fog in the north; in the
        south, behind the Bolt, a seal."""
        b = self.b
        b.zone = 'clockworks'
        cx, cz = WORKS
        R = WORKS_R
        floor = {(x, z) for x in range(cx - R - 2, cx + R + 3) for z in range(cz - R - 2, cz + R + 3) if math.hypot(x - cx, z - cz) <= R + 0.3}
        self.plate(floor, PLATE, works_floor, wet=False, thick=4)
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
        # the ways in and out: the neck from the north, and a like one south; an arch for the fog in each
        out = self.line((cx, cz + R + 1), (cx, cz + R + 3), 2)
        self.plate(out, PLATE, works_floor, wet=False, thick=3)
        self.arch(cx - 1, cx + 1, cz - R - 1, PLATE, 'bolt_fog')
        self.arch(cx - 1, cx + 1, cz + R + 1, PLATE, 'bolt', sealed=True)
        b.threshold('clockworks', cx - 1, PLATE + 1, cz - R - 4, cx + 1, cz - R - 3)
        # the axle over it, on two frames at the rim: what the pendulum hangs from
        top = PLATE + 13
        for z in range(cz - R - 1, cz + R + 2):
            b.set(cx, top, z, 'minecraft:netherite_block' if (z - cz) % 4 else 'minecraft:waxed_cut_copper')
            if abs(z - cz) <= 1:
                for dx in (-1, 1):
                    b.set(cx + dx, top, z, 'minecraft:polished_blackstone')
        for sz in (cz - R - 1, cz + R + 1):
            for dx in (-2, 2):
                for y in range(PLATE + 6, top + 1):
                    b.set(cx + dx, y, sz, 'minecraft:polished_blackstone_bricks')
            for dx in (-1, 0, 1):
                b.set(cx + dx, top, sz, 'minecraft:polished_blackstone')
        # chains out of the dark, with nothing on most of them
        for (dx, dz, low) in ((-9, -6, 7), (8, -8, 9), (-7, 9, 6), (10, 5, 8), (-11, 2, 10), (4, 11, 7)):
            x, z = cx + dx, cz + dz
            for y in range(PLATE + low, min(self.ceiling(x, z), PLATE + 30)):
                b.set(x, y, z, 'minecraft:iron_chain[axis=y,waterlogged=false]')
            b.set(x, PLATE + low - 1, z, 'minecraft:netherite_block' if low % 2 else 'minecraft:polished_blackstone')
        # two great wheels standing behind the rim, east and west: seen across the floor
        self.wheel(cx - R - 3, cz, 8, 'x', 0.35, y=PLATE - 3, salt=4)
        self.wheel(cx + R + 3, cz, 7, 'x', 0.3, y=PLATE - 2, salt=5)
        self.mob('bolt', cx, cz, 'south', 3)
        self.lamp_grid(floor | out, 15, up=5)
        self.keep(cx - 2, cz - R - 2, cx + 2, cz + R + 4)
        return (cx, cz + R + 3)

    def behind(self, start):
        """A stair up from the Works, in the open, and the plate behind the pendulum: a hearth, an anvil, what the Bolt kept."""
        b = self.b
        cx, cz, _ = BEHIND + (0,)
        x, z = start
        y = PLATE
        flight = []
        while y < HIGH:
            z += 1
            y += 1
            flight.append((x, y, z))
            z += 1
            flight.append((x, y, z))
        for (fx, fy, fz) in flight:
            for dx in (-1, 0, 1):
                self.plate({(fx + dx, fz)}, fy, works_floor, wet=False, thick=3)
                self.taken.add((fx + dx, fz))
        plate = self.blob(((cx, cz, 6.2),), 6, 0.8) | self.line((x, z), (cx, cz - 5), 1)
        plate -= {c for c in plate if c in self.ground}
        self.plate(plate, HIGH, black_rock, wet=False)
        b.zone = 'clockworks'
        b.hearth(cx - 3, HIGH + 1, cz, 'works')
        b.set(cx - 3, HIGH + 1, cz + 2, f'{MOD}:forge')
        b.loot(cx + 3, HIGH + 1, cz - 1, 'depths_works', 'west')
        self.keep(cx - 4, cz - 1, cx - 2, cz + 3)
        self.keep(cx + 2, cz - 2, cx + 4, cz)
        self.crystal(cx + 3, cz + 3, 3, 6)
        self.crystal(cx - 1, cz + 5, 2, 7)
        self.lamp_grid(plate | {(f[0], f[2]) for f in flight}, 13, every=3)
        self.keep(cx - 1, start[1], cx + 1, cz - 4)
        return plate

    # ------------------------------------------------------------------ the Causeways

    def causeways(self):
        """From the plate behind the pendulum west, round and back east to the Tear: ways three wide, and less, from
        islet to islet, with the sky under them."""
        b = self.b
        b.zone = 'causeways'
        bx, bz = BEHIND
        islets = {name: self.blob(((x, z, r),), 10 + i, 0.7) for i, (name, (x, z, r)) in enumerate(ISLETS.items())}
        last = self.blob(((LAST[0], LAST[1], 5.2),), 20, 0.6)
        ways = (self.line((bx - 5, bz), (ISLETS['mid'][0], ISLETS['mid'][1]), 1), self.line((ISLETS['mid'][0], ISLETS['mid'][1]), (ISLETS['west'][0] + 5, ISLETS['west'][1] - 2), 1),
                self.line((ISLETS['west'][0] + 2, ISLETS['west'][1] + 5), (ISLETS['spire'][0] - 3, ISLETS['spire'][1]), 1),
                self.line((ISLETS['west'][0] - 4, ISLETS['west'][1] + 5), (ISLETS['cache'][0], ISLETS['cache'][1] - 2), 0),
                self.line((ISLETS['spire'][0] + 3, ISLETS['spire'][1]), (LAST[0] - 3, LAST[1]), 1),
                self.line((LAST[0] + 3, LAST[1]), (ISLETS['wheel'][0] - 5, ISLETS['wheel'][1] + 1), 1))
        cells = set().union(*islets.values()) | last | set().union(*ways)
        cells -= {c for c in cells if c in self.ground}
        self.plate(cells, HIGH, black_rock, wet=False, thick=4)
        for way in ways:
            for c in way:
                self.taken.add(c)
        b.threshold('causeways', bx - 8, HIGH + 1, bz - 1, bx - 7, bz + 1)
        # the middle islet: two of the clock's own, at a wheel
        mx, mz, _ = ISLETS['mid']
        self.wheel(mx, mz - 4, 3, 'z', 0.3, salt=6)
        self.mob('clockhand', mx - 2, mz + 2, 'east', 3)
        self.mob('clockhand', mx + 2, mz + 2, 'east', 3)
        # the west islet: a gut's
        wx, wz, _ = ISLETS['west']
        self.ridge(((wx - 5, wz - 4), (wx - 2, wz - 6)), 5, 9)
        self.mob('gut', wx - 1, wz + 1, 'east', 3)
        self.crystal(wx + 3, wz - 5, 3, 8)
        self.crystal(wx - 5, wz + 2, 2, 9)
        if self.free(wx + 4, wz + 3):
            self.mob('stray_shadow', wx + 4, wz + 3, 'north')
        # the cache: one cell wide to get to, and worth it
        kx, kz, _ = ISLETS['cache']
        b.loot(kx, HIGH + 1, kz + 1, 'depths_causeway', 'north')
        self.keep(kx - 1, kz, kx + 1, kz + 2)
        self.crystal(kx - 2, kz - 1, 2, 10)
        b.shard(kx + 2, HIGH + 1, kz)
        self.taken.add((kx + 2, kz))
        # the spire: a tooth of rock on its islet, and something in it
        sx, sz, _ = ISLETS['spire']
        self.ridge(((sx - 1, sz - 4), (sx + 1, sz - 3)), 8, 10, half=1)
        self.niche(sx, sz - 2, 'south')
        self.mob('handwalker', sx + 3, sz + 2, 'west')
        self.mob('deepling', sx - 3, sz + 3, 'east', 3)
        # the hearth by the Tear
        lx, lz = LAST
        b.zone = 'tear'
        b.hearth(lx - 3, HIGH + 1, lz - 2, 'tear')
        self.keep(lx - 4, lz - 3, lx - 2, lz - 1)
        self.crystal(lx + 3, lz - 3, 3, 11)
        b.zone = 'causeways'
        # the wheel islet, east: one of the clock's own keeping a wheel that does not turn; what it kept
        ex, ez, _ = ISLETS['wheel']
        self.wheel(ex + 2, ez - 2, 5, 'x', 0.4, salt=7)
        for (x, z, facing) in ((ex - 3, ez - 1, 'west'),):
            spot = next(((x + dx, z + dz) for r in range(0, 4) for dx in range(-r, r + 1) for dz in range(-r, r + 1) if self.free(x + dx, z + dz, 0, 3)), None)
            self.mob('clockhand', spot[0], spot[1], facing, 3)
        spot = next(((ex + dx, ez + 5 + dz) for r in range(0, 4) for dx in range(-r, r + 1) for dz in range(-r, r + 1) if self.free(ex + dx, ez + 5 + dz, 1)), None)
        b.loot(spot[0], HIGH + 1, spot[1], 'depths_ledge', 'north')
        self.keep(spot[0] - 1, spot[1] - 1, spot[0] + 1, spot[1] + 1)
        # on the long ways: one that walks them, one that waits at the turn, and lights over the sky beside them
        for kind, x, z, facing, tall in (('usher', 76, 757, 'west', 4), ('handwalker', 42, 740, 'east', 2), ('stray_shadow', 84, 756, 'west', 2)):
            spot = next(((x + dx, z + dz) for r in range(0, 5) for dx in range(-r, r + 1) for dz in range(-r, r + 1)
                         if (x + dx, z + dz) in self.ground and self.ground[(x + dx, z + dz)] == HIGH
                         and all(b.free(x + dx, HIGH + h, z + dz) for h in range(1, tall + 1))), None)
            self.mob(kind, spot[0], spot[1], facing, tall)
        for (x, z, facing) in ((40, 751, 'east'), (72, 761, 'north')):
            # a stub of rock off the way, for a deepling to hang over
            stub = {(x, z)}
            near = min(((abs(x - c[0]) + abs(z - c[1])), c) for c in cells)[1]
            stub |= self.line((x, z), near, 0)
            stub -= {c for c in stub if c in self.ground}
            self.plate(stub, HIGH, black_rock, wet=False, thick=2)
            if all(b.free(x, HIGH + h, z) for h in (1, 2, 3)):
                self.mob('deepling', x, z, facing, 3)
        self.lamp_grid(cells, 13, every=3)
        return cells

    # ------------------------------------------------------------------ the Tear

    def tear(self):
        """The Tear: a bowl of black rock, still water in it, and in the middle of the water what has come through.
        Fog in the north; in the south, behind the Coming, a seal, and the ledge beyond."""
        b = self.b
        b.zone = 'tear'
        cx, cz = TEAR
        R = TEAR_R
        floor = {(x, z) for x in range(cx - R - 3, cx + R + 4) for z in range(cz - R - 3, cz + R + 4) if math.hypot(x - cx, z - cz) <= R + 2.2}
        self.plate(floor, HIGH, glass_rock, wet=True, thick=6)
        for (x, z) in floor:
            far = math.hypot(x - cx, z - cz)
            if far > R:
                if abs(x - cx) <= 1 and (z < cz or z > cz):
                    if far <= R + 2.5 and abs(x - cx) <= 1:
                        self.wet.discard((x, z))
                        b.set(x, HIGH, z, black_rock(x, HIGH, z))
                        continue                                   # (the two ways through the rim)
                # the rim: black rock in teeth, three high and more
                top = 3 + int(smooth(x, z, 2, 1410) * 3) + (1 if far > R + 1.2 else 0)
                for h in range(1, top + 1):
                    b.set(x, HIGH + h, z, black_rock(x, HIGH + h, z) if hsh(x, h, z, 1411) < 0.93 else 'minecraft:crying_obsidian')
                self.taken.add((x, z))
                self.wet.discard((x, z))
            elif far < 3.2:
                # the tear itself: under where it stands the floor is the same black, and wet with something
                b.set(x, HIGH, z, 'minecraft:crying_obsidian' if far > 2.0 and hsh(x, 0, z, 1412) < 0.6 else 'minecraft:obsidian')
        # ribs: the rock of the rim stands up round the bowl and leans in over it, as over something held
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
                        if b.free(x, HIGH + h, z):
                            b.set(x, HIGH + h, z, black_rock(x, HIGH + h, z) if h < tall - 1 or dx or dz else 'minecraft:crying_obsidian')
        # the ways in and out, and an arch for the fog in each
        north = self.line((cx, cz - R - 3), (cx, LAST[1] + 4), 1)
        north -= {c for c in north if c in self.ground}
        self.plate(north, HIGH, black_rock, wet=False, thick=3)
        south = self.line((cx, cz + R + 3), (cx, END[1] - 3), 1)
        south -= {c for c in south if c in self.ground}
        self.plate(south, HIGH, black_rock, wet=False, thick=3)
        self.arch(cx - 1, cx + 1, cz - R - 1, HIGH, 'coming_fog', height=4)
        self.arch(cx - 1, cx + 1, cz + R + 1, HIGH, 'coming', sealed=True, height=4)
        b.threshold('tear', cx - 1, HIGH + 1, cz - R - 5, cx + 1, cz - R - 4)
        self.mob('coming', cx, cz, 'north', 8)
        # crystals inside the rim, far apart: the light it is fought by
        for k in range(8):
            a = k * math.pi / 4 + 0.39
            self.crystal(int(round(cx + math.cos(a) * (R - 1.5))), int(round(cz + math.sin(a) * (R - 1.5))), 3, 20 + k)
        self.lamp_grid({c for c in floor if math.hypot(c[0] - cx, c[1] - cz) < R}, 15, up=6)
        # the ledge beyond: the works of the clock itself, lying where they were left. (What is done with them comes later.)
        ex, ez = END
        ledge = self.blob(((ex, ez, 5.0),), 30, 0.6)
        ledge -= {c for c in ledge if c in self.ground}
        self.plate(ledge, HIGH, works_floor, wet=False, thick=4)
        for dx in range(-2, 3):
            for dz in range(-2, 3):
                far = math.hypot(dx, dz)
                if far <= 2.4:
                    b.set(ex + dx, HIGH, ez + 1 + dz, 'minecraft:waxed_cut_copper' if far > 1.5 else ('minecraft:ochre_froglight[axis=y]' if far < 0.5 else 'minecraft:netherite_block'))
        b.loot(ex + 3, HIGH + 1, ez, 'depths_tear', 'west')
        b.shard(ex - 3, HIGH + 1, ez + 1)
        self.keep(ex - 4, ez - 3, ex + 4, ez + 4)
        self.lamp_grid(ledge | north | south, 13, every=3)
        return floor

    def far(self):
        """What is not come to: rocks that hang in the sky as the plates do, a crystal on each; wheels of the clock
        that came to rest in nothing; chains down out of the stars with a weight at the end of each, or without."""
        b = self.b
        def clear(x, z, r):
            return all((x + dx, z + dz) in self.hollow and (x + dx, z + dz) not in self.ground
                       for dx in range(-r, r + 1) for dz in range(-r, r + 1))
        for i, (x, z, y, r) in enumerate(((112, 624, 13, 3.2), (136, 676, 8, 2.6), (122, 704, 15, 4.0), (104, 728, 11, 2.4), (34, 668, 12, 3.4), (26, 700, 17, 2.8),
                                          (46, 712, 9, 2.2), (112, 756, 19, 3.0), (100, 778, 12, 2.4), (28, 782, 14, 2.6), (128, 632, 22, 2.0), (22, 644, 20, 2.2),
                                          (140, 706, 24, 3.0), (118, 688, 26, 1.8), (40, 690, 24, 2.0), (90, 796, 22, 2.2), (132, 660, 30, 2.4), (60, 700, 30, 1.6))):
            if not clear(x, z, int(r) + 8):
                continue
            for (cx, cz) in self.blob(((x, z, r),), 40 + i, 0.6):
                d = math.hypot(cx - x, cz - z)
                deep = int((r - d + 1) * 2.2 + hsh(cx, 0, cz, 1420) * 2)
                for yy in range(y - max(1, deep), y + 1):
                    b.set(cx, yy, cz, black_rock(cx, yy, cz))
            tall = 1 + int(hsh(x, 1, z, 1421) * 3)
            for h in range(1, tall + 1):
                b.set(x, y + h, z, 'minecraft:amethyst_block' if h > 1 or tall == 1 else 'minecraft:calcite')
            b.set(x, y + tall + 1, z, 'minecraft:amethyst_cluster[facing=up,waterlogged=false]')
            b.set(x, y + tall + 2, z, 'minecraft:light[level=11,waterlogged=false]')
        for (x, z, r, axis, hub) in ((128, 700, 10, 'z', 12), (30, 676, 8, 'x', 20), (104, 742, 6, 'x', 8), (140, 640, 7, 'z', 26)):
            if clear(x, z, 2) and all(clear(*((x, z + d) if axis == 'x' else (x + d, z)), 0) for d in range(-r - 6, r + 7)):
                self.wheel(x, z, r, axis, 0.0, y=hub - 2 - r, salt=20 + x)
        for i, (x, z) in enumerate(((120, 640), (134, 690), (110, 712), (96, 690), (40, 660), (24, 716), (50, 722), (116, 744), (104, 770), (22, 770),
                                    (34, 800), (126, 668), (58, 690), (142, 722), (86, 728))):
            if not clear(x, z, 3):
                continue
            low = 14 + int(hsh(x, 2, z, 1422) * 18)
            for y in range(low, self.ceiling(x, z)):
                b.set(x, y, z, 'minecraft:iron_chain[axis=y,waterlogged=false]')
            kind = int(hsh(x, 3, z, 1423) * 4)
            if kind == 0:
                b.set(x, low - 1, z, 'minecraft:netherite_block')
                b.set(x, low - 2, z, 'minecraft:netherite_block')
            elif kind == 1:
                for dx in (-1, 0, 1):
                    for dz in (-1, 0, 1):
                        b.set(x + dx, low - 1, z + dz, 'minecraft:polished_blackstone' if dx or dz else 'minecraft:netherite_block')
            elif kind == 2:
                b.set(x, low - 1, z, 'minecraft:amethyst_block')
                b.set(x, low - 2, z, 'minecraft:amethyst_cluster[facing=down,waterlogged=false]')
                b.set(x + 1, low - 1, z, 'minecraft:light[level=9,waterlogged=false]')

    def film(self):
        """The still water, wherever a plate is to have it and nothing stands."""
        b = self.b
        for (x, z) in self.wet:
            y = self.ground[(x, z)]
            if b.free(x, y + 1, z):
                b.set(x, y + 1, z, FILM)

    # ------------------------------------------------------------------ all of it

    def build(self, height):
        b = self.b
        b.zone = 'under_sky'
        self.hill(height)
        y = self.drift(height)
        self.brink(y)
        cells = self.water()
        self.water_dress(cells)
        way = self.works()
        self.behind(way)
        self.causeways()
        self.tear()
        self.far()
        self.film()

    def biome_columns(self):
        """The columns that get the place's biome: the hill, a little round it, and the last of the drift."""
        cols = set()
        for (x, z) in self.mask:
            for dx in (-2, 0, 2):
                for dz in (-2, 0, 2):
                    cols.add((x + dx, z + dz))
        for x in range(GATE[0] + 6, LEG_X + 5):
            for z in range(TURN_Z - 4, 600):
                cols.add((x, z))
        return {c for c in cols if c[0] >= 0 and c[1] >= 0}

    def views(self):
        """Pictures of the place for the client test: name, eye, what it looks at (plan coordinates)."""
        gx, gz = GATE
        lx, lz = LEDGE
        wx, wz = WORKS
        bx, bz = BEHIND
        tx, tz = TEAR
        p, h = PLATE, HIGH
        return [
            ('depths_grate_from_the_sump', (gx + 0.5, SUMP_FLOOR + 2.6, gz - 5.5), (gx + 0.5, SUMP_FLOOR + 2.6, gz + 2)),
            ('depths_drift', (gx + 0.5, SUMP_FLOOR + 2.6, gz + 5.5), (gx + 0.5, SUMP_FLOOR + 6, gz + 24)),
            ('depths_cross_leg', (gx + 2.5, 14.6, TURN_Z + 0.5), (LEG_X, 14.6, TURN_Z + 0.5)),
            ('depths_raw_rock', (LEG_X + 0.5, 15.2, TURN_Z + 8.5), (LEG_X + 0.5, 16, ROOM_Z)),
            ('depths_hearth_room', (LEG_X + 0.5, BRINK + 2.6, ROOM_Z - 2.5), (LEG_X - 2, BRINK + 1.4, ROOM_Z + 1)),
            ('depths_out_of_the_drift', (LEG_X + 0.5, BRINK + 2.6, MOUTH_Z - 2.5), (LEG_X + 6, BRINK + 2, lz + 30)),
            ('depths_from_the_brink', (lx + 0.5, BRINK + 2.6, lz + 3.5), (wx, p + 2, wz - 40)),
            ('depths_brink_east', (lx + 2.5, BRINK + 2.6, lz + 0.5), (120, p + 6, 650)),
            ('depths_brink_up', (lx + 0.5, BRINK + 2.6, lz + 2.5), (lx + 20, BRINK + 34, lz + 26)),
            ('depths_brink_down', (lx + 4.5, BRINK + 2.6, lz + 5.5), (lx + 12, 2, lz + 12)),
            ('depths_steps', (self.steps_end[0] + 3.5, p + 2.6, self.steps_end[1] + 0.5), (lx, BRINK + 2, lz)),
            ('depths_still_water', (61.5, p + 2.6, 629.5), (84, p + 3, 652)),
            ('depths_first_wheel', (66.5, p + 2.6, 640.5), (76, p + 5, 646)),
            ('depths_water_middle', (78.5, p + 2.6, 654.5), (104, p + 3, 660)),
            ('depths_water_from_above', (60, p + 30, 690), (84, p, 652)),
            ('depths_the_jump', (self.jump[0] - 2.5, p + 2.6, self.jump[1] + 0.5), (ISLE[0], p + 2, ISLE[1])),
            ('depths_edge_of_the_plate', (106.5, p + 2.6, 668.5), (130, p - 6, 690)),
            ('depths_to_the_works', (wx + 0.5, p + 2.6, 680.5), (wx, p + 8, wz)),
            ('depths_works_from_the_door', (wx + 0.5, p + 2.6, wz - WORKS_R + 2.5), (wx, p + 6, wz + 4)),
            ('depths_works_from_above', (wx - 20, p + 26, wz - 22), (wx, p + 2, wz)),
            ('depths_works_axle', (wx + 8.5, p + 2.6, wz + 9.5), (wx, p + 13, wz)),
            ('depths_behind_the_pendulum', (bx + 0.5, h + 2.6, bz - 4.5), (bx - 3, h + 1.5, bz + 1)),
            ('depths_causeway_west', (bx - 8.5, h + 2.6, bz + 0.5), (30, h + 3, 744)),
            ('depths_causeways_from_above', (bx + 22, h + 30, bz - 14), (56, h, 750)),
            ('depths_west_islet', (38.5, h + 2.6, 742.5), (26, h + 2, 746)),
            ('depths_way_to_the_cache', (27.5, h + 2.6, 750.5), (20, h + 1.5, 759)),
            ('depths_spire', (40.5, h + 2.6, 754.5), (48, h + 5, 756)),
            ('depths_long_causeway', (LAST[0] + 4.5, h + 2.6, LAST[1] + 0.5), (96, h + 3, 754)),
            ('depths_hearth_by_the_tear', (LAST[0] + 2.5, h + 2.6, LAST[1] + 2.5), (LAST[0] - 3, h + 1.5, LAST[1] - 2)),
            ('depths_before_the_tear', (tx + 0.5, h + 2.6, tz - TEAR_R - 5.5), (tx, h + 6, tz)),
            ('depths_tear_from_the_door', (tx + 0.5, h + 2.6, tz - TEAR_R + 1.5), (tx, h + 7, tz)),
            ('depths_tear_from_above', (tx + 24, h + 28, tz - 20), (tx, h + 2, tz)),
            ('depths_tear_up', (tx + 8.5, h + 2.6, tz + 9.5), (tx - 6, h + 30, tz - 6)),
            ('depths_beyond', (END[0] + 0.5, h + 2.6, END[1] - 3.5), (END[0], h + 1, END[1] + 2)),
            ('depths_hill_from_the_sea', (150, self.g + 18, 800), (60, self.g + 24, 740)),
            ('depths_hill_from_the_north', (120, self.g + 60, 560), (70, self.g + 30, 660)),
        ]

    def probes(self):
        return [('depths_drift', (GATE[0], SUMP_FLOOR + 1, GATE[1] + 4)), ('depths_room', (LEG_X, self.drift_floor[(LEG_X, ROOM_Z)][0] + 1, ROOM_Z + 1)),
                ('depths_brink', (LEDGE[0], BRINK + 1, LEDGE[1] + 2)), ('depths_water', (64, PLATE + 1, 644)), ('depths_works', (WORKS[0] + 5, PLATE + 1, WORKS[1] + 5)),
                ('depths_behind', (BEHIND[0], HIGH + 1, BEHIND[1])), ('depths_causeway', (44, HIGH + 1, 742)), ('depths_tear', (TEAR[0] + 6, HIGH + 1, TEAR[1] + 6))]
