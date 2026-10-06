#!/usr/bin/env python3
"""The White Pit: the quarry the High Stockade was built from, under a hill of pale stone west of the Dead Borough. It is
the map's third place and is built into the world's Map by gen_world.py (it has no plan of its own).

The way in is the Quarrymen's Gate at the west end of Low Street, which is locked: its key lies in the treasury of the
Stockade, behind the Chaplain. Behind the gate a lane is cut down through the plateau into an open pit; in the pit's
west face the masons began the head of the Founder, as high as a tower and never finished, and the way into the hill is
through its open mouth.

    gate -> lane -> the pit -> the Maw (a hearth and an anvil behind the teeth) -> the main drift -> the pillar field
    -> a stair up to the Carvers' Shop (statues, some of which are not) -> the upper gallery (east: the room behind
    the Eyes; west: the gallery of the Wheel Hall) -> the cage shaft down (its door at the main drift opens from inside:
    the short way from the hearth) -> the floor of the Wheel Hall: the Taskmaster -> the sump (a hearth) -> the lower
    workings, flooded and dark, where the crawlers listen -> the Breach: the Stone-Eater -> the crack beyond it.

Three levels: TOP eight over the ground of the valley, MID six under it, LOW thirteen under it (the flat world has
sixteen blocks of ground). Everything under the ground is cut out of the world's own stone: the plan holds the hollows
and a skin of rock round them, nothing more.

For what comes later: a barred drift runs north from the lower workings, towards the Stockade's rock; the crack behind
the Breach goes on down; and the hill has two more faces."""
import math

import gen_village
from gen_map import BARS, CHAIN, FLOOR_LANTERN, LANTERN, SOUL_LANTERN, hsh, stairs
from gen_structures import AIR, MOD

HILL = (26, 99, 512, 574)            # the flat top of the hill: x0, x1, z0, z1
HILL_HEIGHT = 24
LOG = 'minecraft:spruce_log[axis=%s]'
PLANK = 'minecraft:spruce_planks'
FENCE = 'minecraft:spruce_fence'
WATER = 'minecraft:water[level=0]'
RAIL_X = 'minecraft:rail[shape=east_west]'


def smooth(x, z, scale, salt):
    fx, fz = x / scale, z / scale
    x0, z0 = math.floor(fx), math.floor(fz)
    tx, tz = fx - x0, fz - z0
    tx, tz = tx * tx * (3 - 2 * tx), tz * tz * (3 - 2 * tz)
    a, b_ = hsh(x0, 0, z0, salt), hsh(x0 + 1, 0, z0, salt)
    c, d = hsh(x0, 0, z0 + 1, salt), hsh(x0 + 1, 0, z0 + 1, salt)
    return (a * (1 - tx) + b_ * tx) * (1 - tz) + (c * (1 - tx) + d * tx) * tz


def hill(x, z):
    """How high the white hill stands over the ground here (0 far from it): a table with broken sides."""
    x0, x1, z0, z1 = HILL
    d = math.hypot(max(x0 - x, x - x1, 0), max(z0 - z, z - z1, 0))
    if d > 34:
        return 0
    d = max(0.0, d - smooth(x, z, 14, 414) * 12)             # its foot runs out further in some places than in others
    return HILL_HEIGHT + smooth(x, z, 9, 412) * 4 - d * 1.35 - (smooth(x, z, 4, 413) * 3 if d else 0)


def chalk(x, y, z):
    n = hsh(x, y, z, 400)
    return 'minecraft:calcite' if n < 0.54 else ('minecraft:diorite' if n < 0.82 else ('minecraft:stone' if n < 0.93 else 'minecraft:andesite'))


def worked(x, y, z):
    n = hsh(x, y, z, 401)
    return 'minecraft:polished_diorite' if n < 0.66 else ('minecraft:calcite' if n < 0.88 else 'minecraft:diorite')


def deep(x, y, z):
    n = hsh(x, y, z, 402)
    return 'minecraft:deepslate' if n < 0.52 else ('minecraft:tuff' if n < 0.76 else ('minecraft:cobbled_deepslate' if n < 0.92 else 'minecraft:calcite'))


def pit_floor(x, y, z):
    n = hsh(x, 0, z, 403)
    return 'minecraft:gravel' if n < 0.3 else ('minecraft:calcite' if n < 0.58 else ('minecraft:diorite' if n < 0.8 else ('minecraft:andesite' if n < 0.92 else 'minecraft:coarse_dirt')))


def drift_floor(x, y, z):
    n = hsh(x, 0, z, 404)
    return 'minecraft:gravel' if n < 0.24 else ('minecraft:tuff' if n < 0.5 else ('minecraft:calcite' if n < 0.74 else 'minecraft:andesite'))


def low_floor(x, y, z):
    n = hsh(x, 0, z, 405)
    return 'minecraft:deepslate' if n < 0.4 else ('minecraft:cobbled_deepslate' if n < 0.66 else ('minecraft:tuff' if n < 0.86 else 'minecraft:gravel'))


def hall_floor(x, y, z):
    n = hsh(x, 0, z, 406)
    return 'minecraft:polished_deepslate' if n < 0.42 else ('minecraft:deepslate_tiles' if n < 0.68 else ('minecraft:cobbled_deepslate' if n < 0.86 else 'minecraft:tuff'))


def shop_floor(x, y, z):
    n = hsh(x, 0, z, 407)
    return 'minecraft:polished_diorite' if (x + z) % 2 and n < 0.8 else ('minecraft:polished_andesite' if n < 0.75 else 'minecraft:calcite')


class Quarry:
    def __init__(self, b, gy, village):
        self.b, self.g, self.village = b, gy, village
        self.MID, self.LOW, self.TOP = gy - 6, gy - 13, gy + 8
        gx, _ = village.at(*gen_village.QUARRY_GATE, 0, 0)
        _, zc = village.at(*gen_village.QUARRY_GATE, 1, 1)
        if (gx, zc) != (162, 535):
            raise ValueError(f'the quarry is laid out from a gate at (162, 535); the town puts it at {(gx, zc)}')
        self.zc = zc
        self.open_sky = set()           # columns cut open from above: nothing grows over them

    # ------------------------------------------------------------------ tools

    def rock(self, x, y, z):
        """The stone of the hill: pale above, dark at the bottom of the workings, mixed between."""
        if y >= self.MID - 1:
            return chalk(x, y, z)
        if y <= self.LOW + 2:
            return deep(x, y, z)
        return chalk(x, y, z) if hsh(x, y, z, 408) < (y - self.LOW - 2) / (self.MID - self.LOW - 3) else deep(x, y, z)

    def dig(self, x0, y0, z0, x1, y1, z1):
        self.b.fill(x0, y0, z0, x1, y1, z1, AIR)

    def floor(self, x0, z0, x1, z1, y, mat):
        for x in range(min(x0, x1), max(x0, x1) + 1):
            for z in range(min(z0, z1), max(z0, z1) + 1):
                self.b.set(x, y, z, mat(x, y, z))

    def room(self, x0, z0, x1, z1, y, height, mat, salt=0, ragged=True):
        """A chamber cut square: its floor block at y, `height` of air over it; the roof left uneven and a knob of
        rock here and there in the corners, as picks leave it."""
        b = self.b
        self.floor(x0, z0, x1, z1, y, mat)
        self.dig(x0, y + 1, z0, x1, y + height, z1)
        if not ragged:
            return
        for x in range(x0, x1 + 1):
            for z in range(z0, z1 + 1):
                edge = x in (x0, x1) or z in (z0, z1)
                n = hsh(x, y, z, 420 + salt)
                if height > 3 and n < (0.45 if edge else 0.14):
                    b.set(x, y + height, z, self.rock(x, y + height, z))
                    if edge and height > 4 and n < 0.15:
                        b.set(x, y + height - 1, z, self.rock(x, y + height - 1, z))

    def frame(self, x, z0, z1, y0, y1, lamp=False):
        """A set of timber across a drift running along x: a post against either wall and a cap over them."""
        b = self.b
        for z in (z0, z1):
            b.fill(x, y0, z, x, y1 - 1, z, LOG % 'y')
        b.fill(x, y1, z0, x, y1, z1, LOG % 'z')
        if lamp:
            b.set(x, y1 - 1, (z0 + z1) // 2, LANTERN)

    def frame_z(self, z, x0, x1, y0, y1, lamp=False):
        b = self.b
        for x in (x0, x1):
            b.fill(x, y0, z, x, y1 - 1, z, LOG % 'y')
        b.fill(x0, y1, z, x1, y1, z, LOG % 'x')
        if lamp:
            b.set((x0 + x1) // 2, y1 - 1, z, LANTERN)

    def lamp(self, x, y, z, soul=False):
        """A lantern on the floor."""
        if self.b.free(x, y, z):
            self.b.set(x, y, z, 'minecraft:soul_lantern[hanging=false]' if soul else FLOOR_LANTERN)

    def hang(self, x, y, z, soul=False):
        """A lantern under the roof: y is the highest free cell."""
        if self.b.free(x, y, z):
            self.b.set(x, y, z, SOUL_LANTERN if soul else LANTERN)

    def blocks(self, x, y, z, shape, salt=0):
        """Cut stone waiting to be hauled: `shape` is a list of (dx, dz, height)."""
        for dx, dz, high in shape:
            for h in range(high):
                if self.b.free(x + dx, y + h, z + dz):
                    n = hsh(x + dx, y + h, z + dz, 430 + salt)
                    self.b.set(x + dx, y + h, z + dz, 'minecraft:polished_diorite' if n < 0.5 else ('minecraft:calcite' if n < 0.8 else 'minecraft:smooth_quartz'))

    def skin(self, before):
        """Rock round everything that was cut under the ground. (Above the ground the land is in the plan already;
        under it the plan holds only what differs from the world's own stone, and the check must see the walls.)"""
        b = self.b
        new = [c for c in b.cells if c not in before]
        count = 0
        for (x, y, z) in new:
            for dx in (-1, 0, 1):
                for dy in (-1, 0, 1):
                    for dz in (-1, 0, 1):
                        n = (x + dx, y + dy, z + dz)
                        if 1 <= n[1] <= self.g and n not in b.cells and b.inside(*n):
                            b.cells[n] = self.rock(*n)
                            count += 1
        return count

    # ------------------------------------------------------------------ from the gate to the pit

    def lane(self):
        """Behind the gate: the hauling road, cut down through the plateau to the floor of the pit. Its walls are the
        height of the cut; nothing in it can be climbed."""
        b, g, zc, mid = self.b, self.g, self.zc, self.MID
        b.zone = 'quarry'
        for x in range(139, 162):
            for z in range(zc - 1, zc + 2):
                self.open_sky.add((x, z))
        # the level stretch by the gate
        for x in range(152, 162):
            for z in range(zc - 1, zc + 2):
                b.set(x, g, z, 'minecraft:cobblestone' if hsh(x, 0, z, 440) < 0.6 else 'minecraft:gravel')
                self.dig(x, g + 1, z, x, g + 30, z)
        # the ramp: a step every block, six down (the last of them on to the floor below)
        for i, x in enumerate(range(151, 146, -1)):
            for z in range(zc - 1, zc + 2):
                b.set(x, g - 1 - i, z, stairs('polished_diorite' if z == zc else 'diorite', 'east'))
                b.flights.append((x, g - 1 - i, z))
                self.dig(x, g - i, z, x, g + 30, z)
        # and the level stretch into the pit
        for x in range(139, 147):
            for z in range(zc - 1, zc + 2):
                b.set(x, mid, z, pit_floor(x, mid, z))
                self.dig(x, mid + 1, z, x, g + 30, z)
        gen_village.lamp_post(b, 159, g + 1, zc - 1)
        self.lamp(142, mid + 1, zc + 1)
        b.threshold('quarry', 154, g + 1, zc - 1, 155, zc + 1)
        # a sledge left on the ramp's head, loaded
        b.set(157, g + 1, zc + 1, 'minecraft:spruce_slab[type=bottom]')
        b.set(156, g + 1, zc + 1, 'minecraft:polished_diorite')

    def pit(self):
        """The open working: forty blocks by thirty-seven, its floor at MID, benches along the north and south faces,
        a derrick, sheds, stone cut and never hauled."""
        b, g, zc, mid = self.b, self.g, self.zc, self.MID
        x0, x1, z0, z1 = 100, 138, zc - 18, zc + 18
        self.pit_box = (x0, x1, z0, z1)
        for x in range(x0, x1 + 1):
            for z in range(z0, z1 + 1):
                self.open_sky.add((x, z))
                b.set(x, mid, z, pit_floor(x, mid, z))
                self.dig(x, mid + 1, z, x, g + 34, z)
        # the benches: one step of three blocks along the north face and the south, four deep, cut clean
        for x in range(x0, x1 + 1):
            for z in list(range(z0, z0 + 4)) + list(range(z1 - 3, z1 + 1)):
                if z1 - 3 <= z and x > x1 - 14:
                    continue                                     # the south bench stops short of the lane's corner
                for y in range(mid + 1, mid + 4):
                    b.set(x, y, z, worked(x, y, z) if y == mid + 3 or z in (z0 + 3, z1 - 3) else chalk(x, y, z))
        # the way up on to the north bench: three steps at its east end
        for i in range(3):
            for z in (z0 + 4, z0 + 5):
                b.set(x1 - 3 - i, mid + 1 + i, z, stairs('diorite', 'west'))
                b.flights.append((x1 - 3 - i, mid + 1 + i, z))
                for y in range(mid + 1, mid + 1 + i):
                    b.set(x1 - 3 - i, y, z, chalk(x1 - 3 - i, y, z))
        for z in (z0 + 4, z0 + 5):                               # the head of the steps, level with the bench
            for y in range(mid + 1, mid + 4):
                b.set(x1 - 6, y, z, worked(x1 - 6, y, z))
            b.set(x1 - 7, mid + 3, z, worked(x1 - 7, mid + 3, z))
            b.fill(x1 - 7, mid + 1, z, x1 - 7, mid + 2, z, chalk)
        # a second bench above the first on the north side, two deep, that nobody reaches: half-cut blocks on it
        for x in range(x0 + 6, x1 - 8):
            for z in (z0, z0 + 1):
                for y in range(mid + 4, mid + 7):
                    b.set(x, y, z, worked(x, y, z) if y == mid + 6 else chalk(x, y, z))
        # the derrick: a mast, a jib, a block of stone still in the air
        mx, mz = 128, zc + 11                                    # (south of the lane's line: the head must be seen whole from the lane)
        b.fill(mx, mid + 1, mz, mx, mid + 11, mz, LOG % 'y')
        b.fill(mx - 1, mid + 11, mz, mx - 6, mid + 11, mz, LOG % 'x')
        for i in range(1, 4):
            b.set(mx - i, mid + 10 - i + 1, mz, FENCE)           # the brace under the jib
        b.fill(mx - 6, mid + 6, mz, mx - 6, mid + 10, mz, CHAIN)
        b.fill(mx - 7, mid + 4, mz - 1, mx - 5, mid + 5, mz + 1, lambda x, y, z: 'minecraft:polished_diorite' if hsh(x, y, z, 441) < 0.7 else 'minecraft:calcite')
        for dx, dz in ((1, 1), (1, -1)):                         # its stays
            b.set(mx + dx, mid + 1, mz + dz, LOG % 'y')
        # cut stone on the floor, well clear of every face
        self.blocks(110, mid + 1, zc - 7, ((0, 0, 2), (1, 0, 2), (0, 1, 1), (1, 1, 2), (2, 0, 1)), 1)
        self.blocks(125, mid + 1, zc + 4, ((0, 0, 1), (1, 0, 2), (0, 1, 2), (1, 1, 1)), 2)
        self.blocks(114, mid + 1, zc + 2, ((0, 0, 1), (0, 1, 1)), 3)
        self.blocks(131, mid + 1, zc - 9, ((0, 0, 2), (1, 0, 1), (0, 1, 1)), 4)
        self.blocks(106, mid + 1, zc + 10, ((0, 0, 1), (1, 0, 1), (2, 0, 2), (2, 1, 1)), 5)
        b.cart(122, mid + 1, zc - 11, load=((0, 0, 'minecraft:calcite'), (1, 0, 'minecraft:polished_diorite'), (1, 1, 'minecraft:calcite')))
        b.cart(104, mid + 1, zc - 11)
        # the powder shed, against the east face north of the lane: timber, open to the west
        sx, sz = x1 - 4, zc - 8
        for x in range(sx, x1 + 1):
            for z in range(sz, sz + 5):
                b.set(x, mid + 4, z, 'minecraft:spruce_slab[type=bottom]')
        for x, z in ((sx, sz), (sx, sz + 4)):
            b.fill(x, mid + 1, z, x, mid + 3, z, LOG % 'y')
        for x in range(sx + 1, x1 + 1):
            b.fill(x, mid + 1, sz, x, mid + 3, sz, PLANK)
            b.fill(x, mid + 1, sz + 4, x, mid + 3, sz + 4, PLANK)
        b.loot(x1, mid + 1, sz + 2, 'quarry_shed', 'west')
        b.barrels(((x1, mid + 1, sz + 1), (x1, mid + 1, sz + 3), (x1, mid + 2, sz + 3), (x1 - 1, mid + 1, sz + 1)))
        b.set(sx + 2, mid + 3, sz + 2, LANTERN)
        # lamps
        for x, z in ((104, zc - 13), (104, zc + 13), (116, zc + 13), (130, zc - 3), (116, zc - 4), (134, zc + 6)):
            gen_village.lamp_post(b, x, mid + 1, z)
        for x in (108, 122):
            self.lamp(x, mid + 4, z0 + 3)
        # who is still at work
        b.mob('digger', 127, mid + 1, zc - 4, 'east')
        b.mob('digger', 112, mid + 1, zc + 12, 'east')
        b.mob('digger', 107, mid + 1, zc - 4, 'east')
        b.mob('barrowman', 120, mid + 1, zc, 'east')
        b.mob('barrowman', 109, mid + 1, zc + 6, 'east')
        b.mob('sapper', 116, mid + 4, z0 + 2, 'south')
        b.mob('sapper', 103, mid + 1, zc + 9, 'east')

    def face(self):
        """The head of the Founder in the west face of the pit, from the lower lip up: twenty-nine blocks across and
        thirty high. The north half is finished work; the south half is only blocked out, with the masons' staging
        still against it. The mouth is the way in; behind the eyes there is a room, and lamps burn in them."""
        b, zc, mid, top = self.b, self.zc, self.MID, self.TOP
        base = mid + 1
        for dz in range(-15, 16):
            for dy in range(0, 31):
                z, y = zc + dz, base + dy
                inside = (dz / 14.6) ** 2 + ((dy - 12) / 19.5) ** 2 <= 1.0
                if not inside:
                    continue
                rough = dz >= 3 and not (abs(dz) <= 5 and dy <= 7)       # the unfinished side (the mouth is done all round)
                depth = 1
                if 17 <= dy <= 19 and abs(dz) <= 12:
                    depth = 3                                             # the brow
                if 21 <= dy <= 22:
                    depth = 2                                             # the band of the crown
                if 6 <= dy <= 12 and 5 <= abs(dz) <= 10:
                    depth = 2 if abs(dz) in (5, 10) or dy in (6, 12) else 3     # the cheeks
                if dy == 6 and abs(dz) <= 5:
                    depth = 3                                             # the upper lip
                if 7 <= dy <= 16 and abs(dz) <= (2 if dy <= 11 else 1):
                    depth = 3 + (16 - dy) // 3                            # the nose
                eye = 14 <= dy <= 16 and 4 <= abs(dz) <= 8
                mouth = dy <= 5 and abs(dz) <= 4
                if rough and not eye and not mouth:
                    depth = max(1, depth - 1) + (1 if hsh(dz, dy, 0, 450) < 0.3 else 0)
                mat = chalk if rough else worked
                if eye or mouth:
                    for x in range(96, 100 + 3):
                        b.set(x, y, z, AIR)
                    continue
                for x in (97, 98, 99):                                    # (the hill behind the head, where its side falls away)
                    if b.free(x, y, z):
                        b.set(x, y, z, chalk(x, y, z))
                for x in range(100, 100 + depth):
                    b.set(x, y, z, mat(x, y, z))
        # the teeth, hanging from the upper lip; the corners of the mouth
        for dz in range(-4, 5):
            if dz % 2 == 0:
                b.set(100, base + 5, zc + dz, 'minecraft:pointed_dripstone[thickness=frustum,vertical_direction=down]')
                b.set(100, base + 4, zc + dz, 'minecraft:pointed_dripstone[thickness=tip,vertical_direction=down]')
                b.set(100, base + 6, zc + dz, worked(100, base + 6, zc + dz))
        # the pupils: a lamp hung deep in either socket
        for dz in (-6, 6):
            b.set(97, base + 16, zc + dz, LANTERN)
        # a jewel in the band of the crown
        b.set(102, base + 22, zc, 'minecraft:chiseled_quartz_block')
        b.set(102, base + 21, zc, 'minecraft:chiseled_quartz_block')
        # the staging against the unfinished half: poles and two floors of boards, high out of reach
        for dz in (10, 14):
            b.fill(105, base, zc + dz, 105, base + 21, zc + dz, FENCE)
        for level in (9, 15, 21):
            for dz in range(10, 15):
                for x in (102, 103, 104, 105):
                    if b.free(x, base + level, zc + dz) and (dz not in (10, 14) or x != 105):
                        b.set(x, base + level, zc + dz, 'minecraft:spruce_slab[type=top]')
        b.set(104, base + 16, zc + 12, FLOOR_LANTERN)
        b.set(103, base + 10, zc + 11, 'minecraft:barrel[facing=up]')

    def maw(self):
        """Behind the teeth: the throat, and the room the quarrymen kept their fire in. No enemy looks into it."""
        b, zc, mid = self.b, self.zc, self.MID
        b.zone = 'drifts'
        # the throat narrows from the mouth to the room's door
        for x in range(96, 100):
            half = 4 if x >= 98 else 3
            self.floor(x, zc - half, x, zc + half, mid, lambda x_, y, z: worked(x_, y, z))
            self.dig(x, mid + 1, zc - half, x, mid + (6 if x >= 98 else 5), zc + half)
        self.room(84, zc - 7, 95, zc + 7, mid, 6, lambda x, y, z: 'minecraft:polished_andesite' if (x + z) % 3 else 'minecraft:polished_diorite', salt=1)
        b.hearth(90, mid + 1, zc - 4, 'maw')
        b.set(93, mid + 1, zc + 5, f'{MOD}:forge')
        self.blocks(86, mid + 1, zc + 5, ((0, 0, 1), (1, 0, 1), (0, 1, 1)), 6)
        b.barrels(((94, mid + 1, zc - 6), (95, mid + 1, zc - 6), (95, mid + 2, zc - 6)))
        for x, z in ((85, zc - 6), (95, zc + 6), (85, zc + 6)):
            self.lamp(x, mid + 1, z)
        self.hang(90, mid + 5, zc)
        self.hang(97, mid + 5, zc)

    def drift(self):
        """The main drift: straight west from the Maw to the wall of the Wheel Hall, a tramway down its middle. At its
        end there is a grating to look through and, to the north, the door of the cage shaft - barred from inside."""
        b, zc, mid = self.b, self.zc, self.MID
        self.floor(60, zc - 2, 83, zc + 2, mid, drift_floor)
        self.dig(60, mid + 1, zc - 2, 83, mid + 4, zc + 2)
        for x in range(61, 83):
            b.set(x, mid + 1, zc, RAIL_X)
        for i, x in enumerate(range(63, 83, 6)):
            self.frame(x, zc - 2, zc + 2, mid + 1, mid + 4, lamp=i % 2 == 0)
        b.threshold('drifts', 81, mid + 1, zc - 2, 82, zc + 2)
        # the grating over the hall
        for z in range(zc - 1, zc + 2):
            for y in (mid + 2, mid + 3):
                b.set(59, y, z, BARS)
                b.set(58, y, z, AIR)
        # a tub left on the rails, and what fell off it
        b.set(66, mid + 1, zc, 'minecraft:cauldron')
        self.blocks(74, mid + 1, zc + 2, ((0, 0, 1),), 7)
        # the door to the cage shaft, north of the drift's end
        self.dig(61, mid + 1, zc - 5, 63, mid + 3, zc - 4)
        self.floor(61, zc - 5, 63, zc - 3, mid, drift_floor)
        b.set(62, mid + 3, zc - 3, self.rock(62, mid + 3, zc - 3))
        b.door(62, mid + 1, zc - 3, 'north', (63, mid + 2, zc - 4), 'north')
        # the way south into the pillar field
        self.floor(69, zc + 3, 71, zc + 5, mid, drift_floor)
        self.dig(69, mid + 1, zc + 3, 71, mid + 3, zc + 5)
        b.mob('digger', 77, mid + 1, zc - 1, 'east')
        b.mob('digger', 64, mid + 1, zc + 1, 'east')

    def field(self):
        """The pillar field: the stone taken out in rooms, with pillars of it left standing to hold the hill up."""
        b, zc, mid = self.b, self.zc, self.MID
        x0, x1, z0, z1 = 62, 82, zc + 6, zc + 31
        self.floor(x0, z0, x1, z1, mid, drift_floor)
        for x in range(x0, x1 + 1):
            for z in range(z0, z1 + 1):
                pillar = (x - x0) % 6 in (3, 4, 5) and (z - z0) % 7 in (3, 4, 5, 6) and x < x1 and z < z1 - 1
                if pillar:
                    continue
                self.dig(x, mid + 1, z, x, mid + 4, z)
                if hsh(x, 1, z, 460) < 0.18:
                    b.set(x, mid + 4, z, self.rock(x, mid + 4, z))
        # lamps on the corners of some pillars, tools, a heap of spoil
        for x, z in ((64, z0 + 2), (70, z0 + 9), (76, z0 + 2), (64, z0 + 16), (76, z0 + 16), (70, z0 + 23), (81, z0 + 9), (82, z0 + 23)):
            self.lamp(x, mid + 1, z)
        self.blocks(68, mid + 1, z0 + 15, ((0, 0, 1), (1, 0, 1)), 8)
        b.loot(62, mid + 1, z0 + 24, 'quarry_field', 'east')
        b.mob('digger', 66, mid + 1, z0 + 8, 'north')
        b.mob('digger', 73, mid + 1, z0 + 15, 'north')
        b.mob('digger', 79, mid + 1, z0 + 21, 'north')
        b.mob('barrowman', 68, mid + 1, z0 + 22, 'north')
        b.mob('sapper', 80, mid + 1, z0 + 1, 'west')
        # the stair up to the Carvers' Shop, east of the field: fourteen steps north, a landing at either end
        sx0, sx1 = 85, 87
        zb = z1 - 1                                               # the bottom landing
        self.floor(83, zb, sx1, zb + 2, mid, drift_floor)
        self.dig(83, mid + 1, zb, sx1, mid + 4, zb + 2)
        for i in range(14):
            z = zb - 1 - i
            for x in range(sx0, sx1 + 1):
                b.set(x, mid + 1 + i, z, stairs('polished_diorite' if x == 86 else 'diorite', 'north'))
                b.flights.append((x, mid + 1 + i, z))
                self.dig(x, mid + 2 + i, z, x, mid + 5 + i, z)
        zt = zb - 15                                              # the top landing: three deep
        self.floor(sx0, zt - 2, sx1, zt, self.TOP, shop_floor)
        self.dig(sx0, self.TOP + 1, zt - 2, sx1, self.TOP + 4, zt)
        self.hang(86, mid + 9, zb - 5)
        self.lamp(87, self.TOP + 1, zt - 2)
        self.stair_top = (sx0, zt)

    def shop(self):
        """The Carvers' Shop, over the pillar field: where the saints for the cathedral were cut. Two rows of them
        stand as they were left. Some are only stone."""
        b, zc, top = self.b, self.zc, self.TOP
        x0, x1, z0, z1 = 62, 82, zc + 11, zc + 31
        sx0, zt = self.stair_top
        self.room(x0, z0, x1, z1, top, 6, shop_floor, salt=2)
        # in from the stair, through the east wall
        self.floor(83, zt - 2, 84, zt, top, shop_floor)
        self.dig(83, top + 1, zt - 2, 84, top + 3, zt)
        b.threshold('carvers', 83, top + 1, zt - 2, 84, zt)
        # the rows: every other plinth holds one of the Unhewn; the rest are blocks in every state of the work
        alive = {(66, 0): 'east', (78, 1): 'west', (66, 2): 'east', (78, 3): 'west'}
        for i, z in enumerate(range(z0 + 3, z1 - 1, 4)):
            for x in (66, 78):
                if (x, i) in alive:
                    b.mob('unhewn', x, top + 1, z, alive[(x, i)])
                    continue
                n = hsh(x, i, z, 470)
                b.set(x, top + 1, z, 'minecraft:polished_diorite')
                if n < 0.75:
                    b.set(x, top + 2, z, 'minecraft:calcite' if n < 0.4 else 'minecraft:quartz_pillar[axis=y]')
                if n < 0.4:
                    b.set(x, top + 3, z, 'minecraft:diorite_wall' if n < 0.2 else 'minecraft:chiseled_quartz_block')
        # benches of the trade along the walls, a grindstone, lamps on stands down the middle
        for z in (z0 + 5, z0 + 13):
            b.set(x0, top + 1, z, 'minecraft:spruce_slab[type=top]')
            b.set(x0, top + 1, z + 1, 'minecraft:spruce_slab[type=top]')
            b.set(x1, top + 1, z + 2, 'minecraft:spruce_slab[type=top]')
        b.set(x1, top + 1, z0 + 9, 'minecraft:grindstone[face=floor,facing=west]')
        for z in (z0 + 5, z0 + 13):
            b.stand(72, top + 1, z, height=2, candles=4, post='minecraft:diorite_wall')
        for x, z in ((x0, z0), (x1, z0), (x0, z1), (x1, z1)):
            self.lamp(x, top + 1, z)
        b.ambient((64, 68, 72, 76, 80), top + 4, (z0 + 3, z0 + 8, z0 + 13, z0 + 18), 10)
        b.loot(72, top + 1, z1, 'quarry_shop', 'north')
        b.mob('digger', 72, top + 1, z1 - 3, 'north')
        # out by the north door, to the upper gallery
        self.floor(71, zc + 3, 73, z0 - 1, top, shop_floor)
        self.dig(71, top + 1, zc + 3, 73, top + 3, z0 - 1)
        self.hang(72, top + 3, zc + 6)

    def gallery(self):
        """The upper gallery, over the main drift: east to the room behind the Founder's eyes, west to the Wheel Hall."""
        b, zc, top = self.b, self.zc, self.TOP
        self.floor(60, zc - 2, 89, zc + 2, top, drift_floor)
        self.dig(60, top + 1, zc - 2, 89, top + 4, zc + 2)
        for i, x in enumerate(range(64, 88, 6)):
            self.frame(x, zc - 2, zc + 2, top + 1, top + 4, lamp=i % 2 == 1)
        b.mob('sapper', 80, top + 1, zc, 'west')
        # behind the eyes: the masons' loft
        self.room(90, zc - 10, 95, zc + 10, top, 5, shop_floor, salt=3)
        for dz in (-6, 6):                                       # the sockets open through to it
            self.floor(96, zc + dz - 2, 99, zc + dz + 2, top, worked)
        b.loot(92, top + 1, zc + 10, 'quarry_eyes', 'north')
        b.shard(92, top + 1, zc - 10)
        b.mob('digger', 93, top + 1, zc, 'west')
        self.lamp(90, top + 1, zc - 10)
        self.lamp(90, top + 1, zc + 10)
        # north from its west end, to the head of the cage shaft
        self.floor(61, zc - 5, 63, zc - 3, top, drift_floor)
        self.dig(61, top + 1, zc - 5, 63, top + 3, zc - 3)

    def shaft(self):
        """The cage shaft: from the upper gallery down past the main drift to the floor of the Wheel Hall, three
        flights of timber stair round the well the cage hung in. At the bottom, the fog."""
        b, zc, top, mid, low = self.b, self.zc, self.TOP, self.MID, self.LOW
        x0, x1, z0, z1 = 60, 64, zc - 16, zc - 6
        b.zone = 'wheel'
        self.floor(x0, z0, x1, z1, low, hall_floor)
        self.dig(x0, low + 1, z0, x1, top + 5, z1)

        def landing(y, za, zb):
            for x in range(x0, x1 + 1):
                for z in range(za, zb + 1):
                    b.set(x, y, z, PLANK)

        def flight(xa, y_first, towards):
            """Seven steps down from just under a landing: north from the south landing or south from the north one."""
            zs = range(z1 - 2, z0 + 1, -1) if towards == 'north' else range(z0 + 2, z1 - 1)
            for i, z in enumerate(zs):
                for x in (xa, xa + 1):
                    b.set(x, y_first - i, z, stairs('spruce', 'south' if towards == 'north' else 'north'))
                    b.flights.append((x, y_first - i, z))
        landing(top, z1 - 1, z1)
        flight(63, top - 1, 'north')
        landing(top - 7, z0, z0 + 1)
        flight(60, top - 8, 'south')
        landing(mid, z1 - 1, z1)
        flight(63, mid - 1, 'north')
        # the well: two posts the whole height, the chain, and the cage at the bottom of it
        for z in (z0 + 3, z1 - 3):
            b.fill(62, low + 1, z, 62, top + 4, z, LOG % 'y')
        b.fill(62, low + 4, zc - 11, 62, top + 5, zc - 11, CHAIN)
        for dx, dz in ((0, 0),):
            b.set(62, low + 3, zc - 11, 'minecraft:spruce_slab[type=top]')
        b.set(62, low + 1, zc - 11, 'minecraft:spruce_slab[type=bottom]')
        for y in (top - 6, mid + 1, low + 1):
            self.lamp(x0, y, z0 if y == top - 6 else z1)
        self.hang(62, top + 5, z1)
        b.threshold('wheel', 61, top + 1, zc - 5, 63, zc - 4)
        # the fog, in the wall between the shaft's foot and the hall
        self.dig(58, low + 1, zc - 12, 59, low + 4, zc - 10)
        self.floor(58, zc - 12, 59, zc - 10, low, hall_floor)
        b.fog('taskmaster_fog', 59, low + 1, zc - 12, 59, low + 4, zc - 10, 'z')

    def hall(self):
        """The Wheel Hall: the chamber the best stone came out of, twenty-eight high, with the great wheel that
        lifted it still on the wall and a block still hanging from its chain. The Taskmaster keeps the floor."""
        b, zc, top, low = self.b, self.zc, self.TOP, self.LOW
        x0, x1, z0, z1 = 30, 57, zc - 14, zc + 14
        roof = top + 7
        self.floor(x0, z0, x1, z1, low, hall_floor)
        for x in range(x0, x1 + 1):
            for z in range(z0, z1 + 1):
                # the roof is a low vault: highest along the middle
                edge = min(x - x0, x1 - x, z - z0, z1 - z)
                high = roof - (3 if edge == 0 else 2 if edge == 1 else 1 if edge < 4 else 0) - (1 if hsh(x, 2, z, 480) < 0.2 else 0)
                self.dig(x, low + 1, z, x, high, z)
        # four pillars of living rock
        for px, pz in ((38, zc - 8), (38, zc + 8), (49, zc - 8), (49, zc + 8)):
            for x in range(px, px + 3):
                for z in range(pz - 1, pz + 2):
                    for y in range(low + 1, roof + 1):
                        b.set(x, y, z, self.rock(x, y, z) if hsh(x, y, z, 481) < 0.85 else 'minecraft:polished_diorite')
            for dx, dz in ((-1, 0), (3, 0)):
                b.set(px + dx, low + 5, pz, 'minecraft:diorite_wall')
                b.set(px + dx, low + 4, pz, LANTERN)
        # the gallery along the east wall, at the level of the upper workings: boards on posts, a rail. Both run from
        # wall to wall: where the boards stopped short of the walls, whoever walked to an end of them stepped off it
        # onto the Taskmaster's floor
        for z in range(z0, z1 + 1):
            for x in range(x1 - 2, x1 + 1):
                b.set(x, top, z, 'minecraft:spruce_slab[type=top]')
            b.set(x1 - 2, top + 1, z, FENCE)
        for z in range(z0 + 6, z1 - 1, 6):
            b.fill(x1 - 2, low + 1, z, x1 - 2, top - 1, z, LOG % 'y')
        for x in (58, 59):                                       # in from the upper gallery
            for z in range(zc - 1, zc + 2):
                b.set(x, top, z, drift_floor(x, top, z))
                self.dig(x, top + 1, z, x, top + 3, z)
        b.loot(x1, top + 1, z1 - 2, 'quarry_rim', 'north')
        b.mob('digger', x1, top + 1, z0 + 4, 'south')
        self.hang(x1, top + 4, zc - 6)
        self.hang(x1, top + 4, zc + 8)
        # the wheel, flat against the north wall: a rim of boards five across, spokes, an axle
        cx, cy = 43, top - 2
        for dx in range(-5, 6):
            for dy in range(-5, 6):
                d = math.hypot(dx, dy)
                if 4.3 <= d <= 5.4:
                    b.set(cx + dx, cy + dy, z0, PLANK)
                    b.set(cx + dx, cy + dy, z0 + 1, PLANK)
                elif (dx == 0 or dy == 0 or abs(dx) == abs(dy)) and 0 < d < 4.3:
                    b.set(cx + dx, cy + dy, z0, FENCE if dx == 0 else 'minecraft:spruce_trapdoor[facing=north,half=bottom,open=true]')
        b.fill(cx, cy, z0, cx, cy, z0 + 2, LOG % 'z')
        # the beam under the roof, the chain over it, and the block that was on its way up
        b.fill(cx, roof - 1, z0 + 2, cx, roof - 1, zc + 1, 'minecraft:dark_oak_log[axis=z]')
        b.fill(cx, cy + 1, z0 + 2, cx, roof - 2, z0 + 2, CHAIN)
        b.fill(cx, low + 14, zc, cx, roof - 2, zc, CHAIN)
        b.fill(cx - 1, low + 11, zc - 1, cx + 1, low + 13, zc + 1, lambda x, y, z: 'minecraft:polished_diorite' if hsh(x, y, z, 482) < 0.75 else 'minecraft:calcite')
        # light: the lamps on the pillars, braziers in two corners, and what the dust in the air gives back
        for x, z in ((x0 + 1, z0 + 1), (x0 + 1, z1 - 1)):
            b.set(x, low + 1, z, 'minecraft:campfire[lit=true]')
        b.ambient(range(x0 + 2, x1 + 1, 5), low + 5, range(z0 + 2, z1 + 1, 5), 13)
        b.ambient(range(x0 + 4, x1 - 2, 6), top + 3, range(z0 + 4, z1 - 2, 6), 9)
        # stone sorted and stacked along the south wall; the tubs it came in
        self.blocks(34, low + 1, z1 - 2, ((0, 0, 2), (1, 0, 2), (2, 0, 1), (0, 1, 1)), 9)
        self.blocks(45, low + 1, z1 - 1, ((0, 0, 1), (1, 0, 2), (2, 0, 2)), 10)
        b.mob('taskmaster', 43, low + 1, zc, 'east')
        # the way on, west: sealed while he lives
        self.dig(28, low + 1, zc - 1, 29, low + 4, zc + 1)
        self.floor(28, zc - 1, 29, zc + 1, low, low_floor)
        b.fog('taskmaster', 29, low + 1, zc - 1, 29, low + 4, zc + 1, 'z', sealed=True)

    def workings(self):
        """The lower workings: the sump, where there is a hearth, and north of it a long gallery half under water
        with stalls either side, where nothing has carried a lamp for a long time. The crawlers there hear whoever
        walks; whoever creeps along the far wall passes them. For that there must be a far wall: a crawler hears a
        creeper on the block it is on and the eight round it, so the gallery is seven wide, five between the posts
        of its timber, and nothing stands in it but the posts."""
        b, zc, low = self.b, self.zc, self.LOW
        b.zone = 'workings'
        # from the hall to the sump
        self.floor(21, zc - 1, 27, zc + 1, low, low_floor)
        self.dig(21, low + 1, zc - 1, 27, low + 3, zc + 1)
        self.room(13, zc - 4, 20, zc + 4, low, 4, low_floor, salt=4)
        b.hearth(16, low + 1, zc + 2, 'sump')
        b.barrels(((13, low + 1, zc + 4), (14, low + 1, zc + 4)))
        self.lamp(20, low + 1, zc - 4)
        self.lamp(13, low + 1, zc - 4)
        b.threshold('workings', 23, low + 1, zc - 1, 24, zc + 1)
        # the gallery, north from the sump
        gx0, gx1, gz0, gz1 = 15, 21, zc - 49, zc - 5
        self.floor(gx0, gz0, gx1, gz1, low, low_floor)
        self.dig(gx0, low + 1, gz0, gx1, low + 3, gz1)
        for x in range(gx0, gx1 + 1):
            for z in range(gz0, gz1 + 1):
                if hsh(x, 3, z, 490) < 0.2:
                    b.set(x, low + 3, z, self.rock(x, low + 3, z))
        # water in its middle stretch: a block deep along the west side, shallower nowhere
        for z in range(zc - 40, zc - 14):
            for x in range(gx0, gx0 + 3):
                if smooth(x, z, 4, 491) > 0.3:
                    b.set(x, low, z, WATER)
        for i, z in enumerate(range(zc - 8, gz0, -7)):
            self.frame_z(z, gx0, gx1, low + 1, low + 3)
            if i % 2 == 0:                                       # (on the floor by the wall, between two sets: not in the way)
                b.set(gx1, low + 1, z + 2, 'minecraft:soul_lantern[hanging=false]')
        # the stalls. West, near the sump: a nest
        self.room(8, zc - 16, 13, zc - 10, low, 3, low_floor, salt=5)
        self.dig(14, low + 1, zc - 14, 14, low + 2, zc - 12)
        b.set(14, low, zc - 13, low_floor(14, low, zc - 13))
        b.set(14, low, zc - 12, low_floor(14, low, zc - 12))
        b.set(14, low, zc - 14, low_floor(14, low, zc - 14))
        b.loot(8, low + 1, zc - 13, 'quarry_workings', 'east')
        b.mob('crawler', 10, low + 1, zc - 15, 'east')
        b.mob('crawler', 11, low + 1, zc - 11, 'east')
        # east, half way: the drift that was being driven north-east when the work stopped - barred, and going on
        self.room(22, zc - 30, 27, zc - 24, low, 3, low_floor, salt=6)
        for y in (low + 1, low + 2, low + 3):                   # (a gate nothing in the map opens: see LockedGateBlock)
            for z in range(zc - 28, zc - 25):
                b.set(28, y, z, f'{MOD}:locked_gate[axis=z,lock=none]')
        self.floor(29, zc - 28, 36, zc - 26, low, low_floor)
        self.dig(29, low + 1, zc - 28, 36, low + 3, zc - 26)
        b.set(33, low + 1, zc - 27, 'minecraft:soul_lantern[hanging=false]')
        for x in range(35, 37):                                  # the fall that closed it
            for z in range(zc - 28, zc - 25):
                for y in range(low + 1, low + 4):
                    if hsh(x, y, z, 492) < (0.9 if x == 36 else 0.5):
                        b.set(x, y, z, 'minecraft:cobbled_deepslate' if hsh(x, y, z, 493) < 0.6 else 'minecraft:gravel')
        b.shard(26, low + 1, zc - 24)
        b.mob('crawler', 24, low + 1, zc - 27, 'west')
        # west, further on: somebody dragged a statue down here
        self.room(8, zc - 44, 13, zc - 36, low, 4, low_floor, salt=7)
        self.dig(14, low + 1, zc - 41, 14, low + 2, zc - 39)
        for z in range(zc - 41, zc - 38):
            b.set(14, low, z, low_floor(14, low, z))
        b.mob('unhewn', 10, low + 1, zc - 40, 'east')
        b.loot(8, low + 1, zc - 43, 'quarry_stall', 'east')
        b.set(9, low + 1, zc - 37, 'minecraft:soul_lantern[hanging=false]')
        # in the gallery itself (each on a dry place)
        for x, z in ((gx0, zc - 21), (gx0 + 1, zc - 45), (gx0 + 1, zc - 21), (gx0, zc - 45)):
            b.set(x, low, z, low_floor(x, low, z))
        b.mob('crawler', gx0, low + 1, zc - 21, 'south')
        b.mob('crawler', gx1, low + 1, zc - 33, 'south')
        b.mob('crawler', gx0 + 1, low + 1, zc - 45, 'south')
        b.mob('digger', gx1 - 1, low + 1, zc - 47, 'south')
        # light enough to see shapes by
        # (three rows of it, between the sets of timber, and no knob of rock left where one goes: one row under a
        # ragged roof left whole stretches black)
        rows = [z for z in range(gz0 + 1, gz1) if (z - (zc - 8)) % 7 in (2, 5)]
        for x in (16, 18, 20):
            for z in rows:
                b.set(x, low + 3, z, AIR)
        b.ambient((16, 18, 20), low + 3, rows, 9)
        for x, z in ((9, zc - 15), (12, zc - 11), (9, zc - 11), (12, zc - 15), (22, zc - 29), (26, zc - 29), (25, zc - 25), (22, zc - 25), (9, zc - 43), (12, zc - 37),
                     (9, zc - 38), (12, zc - 42), (23, zc - 47), (27, zc - 47), (31, zc - 47)):
            b.ambient((x,), low + 3, (z,), 9)
        # the end of the gallery turns east, to where the drift broke through
        self.floor(22, zc - 49, 33, zc - 45, low, low_floor)
        self.dig(22, low + 1, zc - 49, 33, low + 4, zc - 45)
        for x in (22, 27):                                       # the last sets of timber, pushed over
            b.set(x, low + 1, zc - 49, LOG % 'x')
            b.set(x + 1, low + 1, zc - 49, LOG % 'x')
        b.set(21, low + 1, zc - 45, 'minecraft:soul_lantern[hanging=false]')
        # the fog stands in the drift, where there is rock all round it: three blocks further on the cave has eaten
        # the drift's wall and roof, and fog put there left a way round its corner
        b.fog('stone_eater_fog', 25, low + 1, zc - 49, 25, low + 4, zc - 45, 'z')

    def breach(self):
        """The Breach: the cave the lowest drift broke into. Nothing here was cut by a pick."""
        b, zc, low = self.b, self.zc, self.LOW
        b.zone = 'breach'
        cx, cz, rx, rz = 39, zc - 58, 14, 15
        self.breach_at = (cx, cz)
        for x in range(cx - rx - 2, cx + rx + 3):
            for z in range(cz - rz - 2, cz + rz + 3):
                r2 = ((x - cx) / rx) ** 2 + ((z - cz) / rz) ** 2 + (smooth(x, z, 5, 500) - 0.5) * 0.3
                if r2 >= 1.0:
                    continue
                high = low + 3 + int(8.5 * math.sqrt(max(0.04, 1.0 - r2)) + smooth(x, z, 3, 501) * 2.5)
                high = min(high, self.g - 2)
                n = hsh(x, 4, z, 502)
                b.set(x, low, z, 'minecraft:moss_block' if smooth(x, z, 4, 503) > 0.74 else ('minecraft:dripstone_block' if n < 0.25 else low_floor(x, low, z)))
                self.dig(x, low + 1, z, x, high, z)
                if r2 > 0.55 and smooth(x, z, 4, 504) < 0.22:
                    b.set(x, low, z, WATER)                      # pools round the edge
                    continue
                if high - low > 6 and n < 0.08:                  # dripstone from the roof
                    b.set(x, high + 1, z, 'minecraft:dripstone_block')
                    long = 3 if n < 0.03 else 2 if n < 0.055 else 1
                    for i, thick in enumerate(('base', 'frustum', 'tip')[3 - long:]):
                        b.set(x, high - i, z, f'minecraft:pointed_dripstone[thickness={thick},vertical_direction=down]')
                elif r2 > 0.62 and n > 0.95:                     # and from the floor, at the edges
                    b.set(x, low, z, 'minecraft:dripstone_block')
                    b.set(x, low + 1, z, 'minecraft:pointed_dripstone[thickness=frustum,vertical_direction=up]')
                    b.set(x, low + 2, z, 'minecraft:pointed_dripstone[thickness=tip,vertical_direction=up]')
                elif 0.9 < n <= 0.95 and r2 > 0.2:
                    b.set(x, low + 1, z, 'minecraft:glow_lichen[down=true]')
        b.ambient(range(cx - 14, cx + 15, 4), low + 4, range(cz - 15, cz + 16, 4), 11)
        b.ambient((cx - 3, cx + 3), low + 3, (cz - 26, cz - 21), 8)
        b.mob('stone_eater', cx, low + 1, cz, 'south')
        b.threshold('breach', 26, low + 1, zc - 49, 27, zc - 45)
        # the crack beyond: sealed while it lives. What it kept, and a fissure that goes on down
        nz = cz - rz
        self.floor(cx - 1, nz - 7, cx + 1, nz + 2, low, low_floor)
        self.dig(cx - 1, low + 1, nz - 7, cx + 1, low + 3, nz + 2)
        b.fog('stone_eater', cx - 1, low + 1, nz - 4, cx + 1, low + 3, nz - 4, 'x', sealed=True)
        self.room(cx - 5, nz - 15, cx + 5, nz - 8, low, 4, low_floor, salt=8)
        b.loot(cx - 5, low + 1, nz - 12, 'quarry_breach', 'east')
        b.shard(cx + 4, low + 1, nz - 14)
        self.lamp(cx + 5, low + 1, nz - 8, soul=True)
        self.lamp(cx - 5, low + 1, nz - 15, soul=True)
        self.dig(cx, low + 1, nz - 22, cx, low + 3, nz - 16)     # the fissure: a body wide, barred where it drops
        self.floor(cx, nz - 22, cx, nz - 16, low, low_floor)
        for y in (low + 1, low + 2, low + 3):
            b.set(cx, y, nz - 18, f'{MOD}:locked_gate[axis=x,lock=none]')
        b.set(cx, low + 1, nz - 21, 'minecraft:soul_lantern[hanging=false]')
        b.set(cx, low + 2, nz - 20, 'minecraft:glow_lichen[west=true]')

    # ------------------------------------------------------------------ all of it

    def build(self):
        before = set(self.b.cells)
        self.lane()
        self.pit()
        self.face()
        self.maw()
        self.drift()
        self.field()
        self.shop()
        self.gallery()
        self.shaft()
        self.hall()
        self.workings()
        self.breach()
        return self.skin(before)

    def views(self):
        """Pictures of the place for the client test: name, eye, what it looks at (plan coordinates)."""
        zc, g, mid, low, top = self.zc, self.g, self.MID, self.LOW, self.TOP
        cx, cz = self.breach_at
        return [
            ('quarry_gate_from_the_street', (171, g + 2.6, zc + 0.5), (162, g + 3, zc + 0.5)),
            ('quarry_lane', (160, g + 2.6, zc + 0.5), (140, mid + 6, zc + 0.5)),
            ('quarry_pit_from_the_lane', (139, mid + 2.6, zc + 0.5), (100, mid + 14, zc + 0.5)),
            ('quarry_face', (124, mid + 6, zc + 0.5), (100, mid + 15, zc + 0.5)),
            ('quarry_face_close', (110, mid + 2.6, zc - 3), (100, mid + 12, zc + 1)),
            ('quarry_pit_from_above', (140, g + 26, zc + 30), (112, mid + 2, zc)),
            ('quarry_hill_from_the_town', (172, g + 30, zc - 40), (100, g + 10, zc)),
            ('quarry_maw', (95, mid + 2.6, zc + 5), (86, mid + 2, zc - 5)),
            ('quarry_out_of_the_mouth', (93, mid + 2.6, zc + 0.5), (110, mid + 4, zc + 0.5)),
            ('quarry_drift', (82, mid + 2.6, zc + 0.5), (60, mid + 2.4, zc + 0.5)),
            ('quarry_grating', (61, mid + 2.6, zc + 0.5), (43, low + 6, zc + 0.5)),
            ('quarry_field', (63, mid + 2.6, zc + 7), (80, mid + 2.4, zc + 28)),
            ('quarry_stair', (86.5, mid + 2.6, zc + 31.5), (86.5, mid + 12, zc + 18)),
            ('quarry_shop', (81, top + 2.6, zc + 13), (64, top + 2.4, zc + 29)),
            ('quarry_shop_row', (72.5, top + 2.6, zc + 12), (72.5, top + 2.4, zc + 30)),
            ('quarry_gallery', (88, top + 2.6, zc + 0.5), (60, top + 2.4, zc + 0.5)),
            ('quarry_eyes', (91, top + 2.6, zc + 0.5), (98, top + 3, zc - 6)),
            ('quarry_from_the_eye', (95.5, top + 3.6, zc - 5.5), (130, mid + 2, zc + 4)),
            ('quarry_hall_from_the_gallery', (55.6, top + 3.4, zc + 0.5), (42, low + 3, zc + 0.5)),
            ('quarry_wheel', (55, top + 2.6, zc + 8), (43, top + 2, zc - 14)),
            ('quarry_gallery_end', (56.5, top + 2.6, zc + 5), (56, top + 1.2, zc + 14)),
            ('quarry_shaft_down', (62.5, top + 2.6, zc - 6.5), (62.5, low + 2, zc - 13)),
            ('quarry_shaft_foot', (63.5, low + 2.6, zc - 7.5), (59, low + 2.5, zc - 11)),
            ('quarry_hall_floor', (56, low + 2.6, zc - 11), (36, low + 8, zc + 4)),
            ('quarry_hall_up', (32, low + 2.6, zc + 12), (46, top + 4, zc - 10)),
            ('quarry_sump', (19, low + 2.6, zc - 3), (15, low + 2, zc + 3)),
            ('quarry_workings', (17.5, low + 2.6, zc - 6), (17.5, low + 2.4, zc - 45)),
            ('quarry_barred_drift', (22, low + 2.6, zc - 26.5), (34, low + 2.4, zc - 26.5)),
            ('quarry_breach_door', (19.5, low + 2.6, zc - 45.5), (25, low + 2.6, zc - 47)),
            ('quarry_breach_door_from_inside', (32, low + 2.8, zc - 48), (25, low + 2.6, zc - 46.5)),
            ('quarry_breach', (31, low + 2.6, zc - 47), (cx + 4, low + 5, cz - 4)),
            ('quarry_breach_from_the_north', (cx, low + 3, cz - 12), (cx - 2, low + 3, cz + 10)),
            ('quarry_crack', (cx + 3, low + 2.6, cz - 24), (cx, low + 2.4, cz - 36)),
        ]

    def probes(self):
        zc, mid, low, top = self.zc, self.MID, self.LOW, self.TOP
        return [('quarry_pit', (120, mid + 1, zc + 3)), ('quarry_maw', (90, mid + 1, zc)), ('quarry_drift', (72, mid + 1, zc + 1)),
                ('quarry_shop', (72, top + 1, zc + 20)), ('quarry_hall', (44, low + 1, zc + 5)), ('quarry_workings', (17, low + 1, zc - 30)),
                ('quarry_breach', (self.breach_at[0] + 3, low + 1, self.breach_at[1] + 3))]
