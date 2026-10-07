#!/usr/bin/env python3
"""The Morning Wharf: the map's sixth place, built into the world's Map by gen_world.py (it has no plan of its own).

South of the town the plateau falls into the sea, and under its cliff lies the port the Stockade was fed through. Its
hour is the one the country is named for and has nowhere else: the sun just clear of the sea, and a mist on the water.
The ships that were to come in on the morning the day was stopped are still at anchor in the roads; the one that was to
go out and bring them in is still at the end of the long pier. The way in is the Sea Gate, the old south gate of the
yard of the first hearth; its key lies in the candle store behind the Elder. Behind the gate a passage goes down under
the plateau, and in its cross leg, from which neither mouth is seen, the hour is turned.

    Sea Gate -> the port (a hearth in the harbour office) -> the long pier -> the koch "Daybreak" (the Helmsman, on her
    deck; a seal on the gangway over her other side) -> the old mole, breached in two places -> the Long Spit: sand,
    flat and wet, a long way out and round (a hearth under an upturned karbas at its root) -> the boulder strand ->
    the shoal in its ring of stacks (the Capsized; a seal on the arch behind it) -> a causeway of rock -> the
    lighthouse (the Pilot at its foot, a hearth inside, the lamp at the top of the stair).

The sea is waded and not swum: where it is deeper than the knee it takes whoever is in it (SoulMechanics.undertow, by
the biome), and the check of the map knows the same (Map.undertow). So the ways are the quay, the piers, the mole, the
sand and the stones, and nothing else.

Levels: the port, the quay, the piers and dry sand are the valley's ground (g); the sea's top layer of water is g - 1;
wet sand lies in that layer with a film of water on it, where the sea is waded there is sand a layer lower, and
where it is deep, two. Nothing here is a way up: the cliffs are sheer, the ships' rails are a plank and a fence, and
the stacks round the shoal stand four blocks out of the water and more."""
import math

import gen_village
from gen_map import BARS, FLOOR_LANTERN, LANTERN, hsh, stairs
from gen_structures import AIR, MOD

T = gen_village.T
WATER = 'minecraft:water[level=0]'
FILM = f'{MOD}:sea_shallows'
PLANK = 'minecraft:spruce_planks'
TARRED = 'minecraft:dark_oak_planks'
POST = 'minecraft:spruce_log[axis=y]'
DARK_POST = 'minecraft:dark_oak_log[axis=y]'
FENCE = 'minecraft:spruce_fence'
DARK_FENCE = 'minecraft:dark_oak_fence'
SAIL = 'minecraft:white_wool'
SAIL_SHADE = 'minecraft:light_gray_wool'
CHAIN = 'minecraft:iron_chain[axis=%s]'
SLAB = 'minecraft:spruce_slab[type=%s]'

Z0 = 630                                     # everything of the shore lies south of this line
GATE = (218, 558)                            # the passage begins here, under the yard's south wall
BEND = 600                                   # its cross leg
MOUTH = 236                                  # and the line it comes out on: Sea Lane, and the long pier
PX, PZ = 176, 647                            # the corner of the port's grid of tiles
PCOLS, PROWS = 40, 15
QUAY = (692, 695)                            # the quay: these rows; the sea begins after them
PIER = (236, 240, 760)                       # the long pier: from x to x, out to z
SHIP = (249, 722)                            # the Daybreak: her middle line and her stern
SHIP_HALF = (4, 5, 6, 6)                     # her half-width at the stern, row by row; then 6 to the bow's taper
SHIP_BOW = (6, 6, 5, 5, 5, 4, 4, 3, 3, 2, 2, 1, 0)
SHIP_LENGTH = 46
CABIN = 9                                    # the rows of her stern that are the cabin
GANGWAY = 753                                # where she is come aboard, from the pier
OFF = 735                                    # and left, on to the mole
MOLE = (257, 347, 735)                       # the old mole: from x to x along this z, three wide
GAP = (280, 281)                             # where the sea has taken two blocks of it
SLUMP = (304, 310)                           # and where it has slumped to the water
SPIT = (((372, 700), 18), ((362, 736), 16), ((356, 790), 12), ((340, 832), 10), ((306, 858), 9), ((262, 866), 8), ((232, 858), 7))
KARBAS = (366, 722)                          # the upturned boat at the spit's root: a hearth
WRECK = (350, 812)
STRAND = (((232, 858), 8), ((216, 848), 10), ((208, 836), 9), ((206, 826), 3.5))
SHOAL = (190, 825)                           # the middle of the shoal
SHOAL_R = 12
RING = 17                                    # how far out its stacks stand
CAUSEWAY = (((190, 809), 2.5), ((183, 803), 2.5), ((176, 797), 3))
LIGHT = (166, 796)                           # the lighthouse: the middle of its tower
ROCK_R = 13
TOWER_R = 4.5
TOWER_H = 24
ROADS = ((298, 772, 40, 0), (206, 748, 34, 1), (270, 800, 38, 2))   # the ships at anchor: middle line, stern, length, a number
COAST = ((0, 728), (150, 716), (164, 702), (171, 650), (175, 646), (297, 646), (301, 650), (307, 700), (314, 716), (326, 714), (334, 696), (340, 688),
         (420, 688), (430, 700), (440, 744), (491, 772))


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


def coast(x):
    """Where the cliff's foot is, going south, at this x."""
    for (ax, az), (bx, bz) in zip(COAST, COAST[1:]):
        if ax <= x <= bx:
            return az + (bz - az) * (x - ax) / max(1, bx - ax)
    return COAST[-1][1]


# ---------------------------------------------------------------------------------------------------- materials

def sand(x, y, z):
    n = hsh(x, 0, z, 900)
    if smooth(x, z, 6, 901) > 0.74:
        return 'minecraft:gravel' if n < 0.5 else 'minecraft:sand'
    return 'minecraft:sand' if n < 0.94 else 'minecraft:sandstone'


def shingle(x, y, z):
    """The strand: stones the size of a fist to the size of a head, and sand between them."""
    n = hsh(x, 0, z, 902)
    return 'minecraft:gravel' if n < 0.4 else ('minecraft:cobblestone' if n < 0.6 else ('minecraft:andesite' if n < 0.76 else ('minecraft:stone' if n < 0.88 else 'minecraft:sand')))


def seabed(x, y, z):
    n = hsh(x, 0, z, 903)
    return 'minecraft:sand' if n < 0.6 else ('minecraft:gravel' if n < 0.9 else 'minecraft:clay')


def stack(x, y, z):
    """Sea rock: dark, wet at the foot, a little green."""
    n = hsh(x, y, z, 904)
    if smooth(x + y, z - y, 5, 905) > 0.68:
        return 'minecraft:tuff' if n < 0.6 else 'minecraft:deepslate'
    return 'minecraft:stone' if n < 0.4 else ('minecraft:andesite' if n < 0.62 else ('minecraft:cobblestone' if n < 0.78 else ('minecraft:mossy_cobblestone' if n < 0.9 else 'minecraft:tuff')))


def quay_stone(x, y, z):
    n = hsh(x, y, z, 906)
    return 'minecraft:stone_bricks' if n < 0.5 else ('minecraft:cobblestone' if n < 0.72 else ('minecraft:mossy_stone_bricks' if n < 0.86 else 'minecraft:andesite'))


def mole_stone(x, y, z):
    n = hsh(x, y, z, 907)
    return 'minecraft:cobbled_deepslate' if n < 0.34 else ('minecraft:cobblestone' if n < 0.58 else ('minecraft:mossy_cobblestone' if n < 0.78 else ('minecraft:andesite' if n < 0.9 else 'minecraft:stone')))


def deck(x, y, z):
    n = hsh(x, 0, z, 908)
    return PLANK if n < 0.86 else 'minecraft:stripped_spruce_wood[axis=z]'


def port_ground(x, y, z):
    """Between the houses, where no cobbles were ever laid: sand trodden hard, gravel, a little turf under the cliff."""
    n = hsh(x, 0, z, 909)
    return 'minecraft:coarse_dirt' if n < 0.3 else ('minecraft:gravel' if n < 0.55 else ('minecraft:sand' if n < 0.85 else 'minecraft:packed_mud'))


def lining(x, y, z):
    n = hsh(x, y, z, 910)
    return 'minecraft:stone_bricks' if n < 0.45 else ('minecraft:cobblestone' if n < 0.62 else ('minecraft:mossy_stone_bricks' if n < 0.8 else 'minecraft:mossy_cobblestone'))


def port_tiles():
    """The port as a grid of characters, like the town's (gen_village.tiles): . a way, , a yard, # a lot, : built by hand."""
    g = [['^'] * gen_village.COLS for _ in range(gen_village.ROWS)]

    def rect(c0, r0, c1, r1, ch):
        for r in range(r0, r1 + 1):
            for c in range(c0, c1 + 1):
                g[r][c] = ch
    rect(0, 0, PCOLS - 1, PROWS - 1, ',')
    rect(1, 1, PCOLS - 2, 12, '#')
    rect(0, 13, PCOLS - 1, 14, '.')              # the waterfront
    rect(20, 0, 21, 14, '.')                     # Sea Lane, from the passage to the long pier
    rect(1, 6, PCOLS - 2, 6, '.')                # the back street
    rect(9, 1, 9, 12, '.')
    rect(31, 1, 31, 12, '.')
    rect(4, 9, 5, 10, ',')
    rect(34, 2, 36, 3, ',')
    rect(26, 1, 29, 4, ',')                      # the drying yard
    for ch, (c0, r0, c1, r1) in SPECIAL.items():
        rect(c0, r0, c1, r1, ch)
    return g


SPECIAL = {
    'o': (22, 8, 25, 11),         # the harbour office: a hearth
    'n': (14, 9, 18, 12),         # the net loft
    's': (2, 9, 3, 12),           # the boat shed, by the slip
}


class Port(gen_village.Village):
    """The port's houses are the town's own, on a grid of their own."""

    def __init__(self, b, gy):
        self.b, self.vx, self.vz, self.gy = b, PX, PZ, gy
        self.g = port_tiles()
        self.houses = gen_village.lots(self.g)
        self.kept = set()

    def inside(self, x, z):
        return PX <= x < PX + PCOLS * T and PZ <= z < PZ + PROWS * T


class Shore:
    def __init__(self, b, gy, depth, width):
        self.b, self.g, self.d, self.w = b, gy, depth, width
        self.sea = gy - 1                    # the layer the top of the sea is in
        self.port = Port(b, gy)
        self.kind = {}
        self.taken = set()                   # columns something stands on: nothing more is strewn there
        self.mobs = True
        for x in range(width):
            c = coast(x)
            flat = 172 <= x <= 300
            for z in range(Z0, depth):
                wob = 0.0 if flat and z < 652 else (smooth(x, z, 9, 911) - 0.5) * 4.0
                if z >= c + wob:
                    self.kind[(x, z)] = self._kind(x, z)

    # ------------------------------------------------------------------ the shape of it

    def _kind(self, x, z):
        """What a column of the shore is: the port's ground, sand (dry), wet (sand in the sea's own layer), stones (the
        same of the strand), wade (knee-deep), deep."""
        if PX - 4 <= x <= PX + PCOLS * T + 3 and z < QUAY[0]:
            return 'port'
        if PX - 4 <= x <= PX + PCOLS * T + 3 and z <= QUAY[1]:
            return 'quay'
        wob = (smooth(x, z, 7, 912) - 0.5) * 2.4
        land = along(x, z, SPIT) + wob
        if 336 <= x <= 428:
            land = max(land, coast(x) + 9 - z + wob)               # the beach under the cliff of the east bay
        if 174 <= x <= 190:
            land = max(land, min(QUAY[1] + 6 - z, x - 171, 191 - x) + 2.5 + wob * 0.4)   # the slip, at the west end of the quay
        stones = max(along(x, z, STRAND), along(x, z, CAUSEWAY)) + wob * 0.5
        if math.hypot(x - SHOAL[0], z - SHOAL[1]) <= RING + 0.5:
            return 'shoal'
        if math.hypot(x - LIGHT[0], z - LIGHT[1]) <= ROCK_R + wob * 0.6:
            return 'rock'
        if land > 6:
            return 'sand'
        if land > 0.8:
            return 'wet'
        if stones > 1.0:
            return 'stones'
        if land > -2.2 or stones > -1.4:
            return 'wade'
        return 'deep'

    def inside(self, x, z):
        return (x, z) in self.kind

    def keep(self, x0, z0, x1, z1):
        for x in range(min(x0, x1), max(x0, x1) + 1):
            for z in range(min(z0, z1), max(z0, z1) + 1):
                self.taken.add((x, z))

    def ground(self):
        b, g, sea = self.b, self.g, self.sea
        for (x, z), kind in self.kind.items():
            if kind == 'port':
                b.set(x, g, z, port_ground(x, g, z))
            elif kind == 'quay':
                b.set(x, g, z, quay_stone(x, g, z))
                b.set(x, g - 1, z, quay_stone(x, g - 1, z))
                b.set(x, g - 2, z, quay_stone(x, g - 2, z))
            elif kind == 'sand':
                b.set(x, g, z, sand(x, g, z))
                b.set(x, g - 1, z, 'minecraft:sandstone')
            elif kind in ('wet', 'stones'):
                b.set(x, g, z, FILM if hsh(x, 1, z, 913) < (0.8 if kind == 'wet' else 0.45) else AIR)
                b.set(x, sea, z, sand(x, sea, z) if kind == 'wet' else shingle(x, sea, z))
                b.set(x, sea - 1, z, 'minecraft:sandstone')
            elif kind == 'wade':
                b.set(x, g, z, AIR)
                b.set(x, sea, z, WATER)
                b.set(x, sea - 1, z, seabed(x, sea - 1, z))
            elif kind == 'deep':
                b.set(x, g, z, AIR)
                b.set(x, sea, z, WATER)
                b.set(x, sea - 1, z, WATER)
                b.set(x, sea - 2, z, seabed(x, sea - 2, z))
            elif kind == 'shoal':
                b.set(x, g, z, FILM)
                b.set(x, sea, z, sand(x, sea, z))
                b.set(x, sea - 1, z, 'minecraft:sandstone')
            elif kind == 'rock':
                b.set(x, g, z, stack(x, g, z))
                b.set(x, sea, z, stack(x, sea, z))
                b.set(x, sea - 1, z, stack(x, sea - 1, z))

    def undertow(self):
        """The cells of the sea that take whoever is in them: water with water under it or over it."""
        b = self.b
        out = set()
        for (x, z), kind in self.kind.items():
            if kind in ('deep', 'wade', 'quay'):
                for y in (self.sea, self.sea - 1):
                    if 'minecraft:water' in str(b.get(x, y, z)) and ('minecraft:water' in str(b.get(x, y - 1, z)) or 'minecraft:water' in str(b.get(x, y + 1, z))):
                        out.add((x, y, z))
        return out

    def bare(self, x, y, z):
        """Takes the film of water off a cell that something is to be set in."""
        if FILM in str(self.b.get(x, y, z)):
            self.b.set(x, y, z, AIR)

    def mob(self, kind, x, y, z, facing='south'):
        b = self.b
        self.bare(x, y, z)
        if not (b.free(x, y, z) and b.free(x, y + 1, z)) or b.get(x, y - 1, z) in (None, AIR):
            raise ValueError(f'no room for {kind} at {(x, y, z)}: {b.get(x, y, z)} over {b.get(x, y - 1, z)}')
        if self.mobs:
            b.mob(kind, x, y, z, facing)
        self.keep(x - 1, z - 1, x + 1, z + 1)

    def name(self, place, x0, z0, x1, z1):
        """A threshold over ground that is not level: in every column, the cell one stands in."""
        b = self.b
        for x in range(x0, x1 + 1):
            for z in range(z0, z1 + 1):
                y = self.on(x, z)
                self.bare(x, y, z)
                if b.free(x, y, z) and b.get(x, y - 1, z) not in (None, AIR) and 'water' not in str(b.get(x, y - 1, z)):
                    b.set(x, y, z, f'{MOD}:place_name[place={place}]')

    def ways(self):
        """The middle of every way over sand and stones is kept: nothing is strewn there."""
        for line in (SPIT, STRAND, CAUSEWAY):
            for ((ax, az), _), ((bx, bz), _) in zip(line, line[1:]):
                steps = max(abs(bx - ax), abs(bz - az), 1)
                for i in range(steps + 1):
                    px, pz = round(ax + (bx - ax) * i / steps), round(az + (bz - az) * i / steps)
                    self.keep(px - 1, pz - 1, px + 1, pz + 1)

    def on(self, x, z):
        """The layer one stands in at this column of the shore (the first free cell over its ground)."""
        b = self.b
        for y in range(self.sea, self.g + 8):
            s = b.get(x, y, z)
            if s in (None, AIR) or FILM in str(s) or 'place_name' in str(s):
                return y
        raise ValueError(f'nothing to stand on at {(x, z)}')

    # ------------------------------------------------------------------ the passage

    def passage(self, height):
        """From the Sea Gate south under the plateau, a turn east, a turn south again, and out at the head of Sea Lane.
        In the cross leg the hour is turned."""
        b, g = self.b, self.g
        gx, gz = GATE
        end = int(coast(MOUTH)) + 1
        path = [(gx, z) for z in range(gz, BEND + 1)] + [(x, BEND) for x in range(gx, MOUTH + 1)] + [(MOUTH, z) for z in range(BEND, end + 1)]
        middle = set(path)
        cells = {(px + dx, pz + dz) for (px, pz) in path for dx in (-2, -1, 0, 1, 2) for dz in (-2, -1, 0, 1, 2) if pz + dz >= gz} - set(self.kind)
        shell = {(x + dx, z + dz) for (x, z) in cells for dx in (-1, 0, 1) for dz in (-1, 0, 1) if z + dz >= gz} - cells - set(self.kind)
        for (x, z) in cells:
            if height.get((x, z), 0) <= 0:
                raise ValueError(f'the passage to the sea comes out into the open at {(x, z)}')
            off = min(max(abs(x - px), abs(z - pz)) for (px, pz) in path if abs(x - px) <= 2 and abs(z - pz) <= 2)
            top = g + (5 if off <= 1 else 4)
            b.set(x, g, z, lining(x, g, z) if hsh(x, 0, z, 914) < 0.6 else ('minecraft:gravel' if hsh(x, 1, z, 914) < 0.6 else 'minecraft:sand'))
            b.fill(x, g + 1, z, x, top, z, AIR)
            b.set(x, top + 1, z, lining(x, top + 1, z))
            for y in range(top + 2, g + max(height[(x, z)], 8) + 1):
                if b.get(x, y, z) in (None, AIR):
                    b.set(x, y, z, 'minecraft:stone')
        for (x, z) in shell:
            if height.get((x, z), 0) > 0:
                b.fill(x, g, z, x, g + 6, z, lining)
                for y in range(g + 7, g + max(height[(x, z)], 8) + 1):
                    if b.get(x, y, z) in (None, AIR):
                        b.set(x, y, z, 'minecraft:stone')
        for i, (px, pz) in enumerate(path):
            if i % 9 == 5 and (px, pz) in cells:
                north_south = i + 1 < len(path) and path[i + 1][0] == px
                sx, sz = (px + (2 if (i // 9) % 2 else -2), pz) if north_south else (px, pz + (2 if (i // 9) % 2 else -2))
                if (sx, sz) in cells and b.free(sx, g + 3, sz) and b.free(sx, g + 4, sz):
                    b.set(sx, g + 4, sz, FENCE)
                    b.set(sx, g + 3, sz, LANTERN)
        # sand blown in at the seaward end, and the wrack the sea leaves
        for (x, z) in cells:
            if z > end - 14 and hsh(x, 2, z, 915) < 0.25 + 0.05 * (z - end + 14):
                b.set(x, g, z, 'minecraft:sand')
        b.threshold('to_night', gx + 3, g + 1, BEND - 2, gx + 4, BEND + 2)
        b.threshold('to_sunrise', MOUTH - 4, g + 1, BEND - 2, MOUTH - 3, BEND + 2)
        b.threshold('wharf', MOUTH - 2, g + 1, end - 3, MOUTH + 2, end - 2)
        self.path = path
        self.end = end

    # ------------------------------------------------------------------ the port

    def town(self):
        b, g, p = self.b, self.g, self.port
        for r in range(PROWS):
            for c in range(PCOLS):
                ch = p.g[r][c]
                x0, z0 = p.at(c, r, 0, 0)
                for x in range(x0, x0 + T):
                    for z in range(z0, z0 + T):
                        if ch == '.':
                            b.set(x, g, z, gen_village.cobble(x, g, z) if hsh(x, 3, z, 916) < 0.8 else 'minecraft:sand')
                        elif ch == ',':
                            b.set(x, g, z, port_ground(x, g, z))
        for i, (c0, r0, c1, r1) in enumerate(p.houses):
            seed = 600 + i * 11
            if c1 == c0 or r1 == r0:
                self.shed(c0, r0, c1, r1, seed)
            else:
                # (boards on a frame, or whitewash: what stands by the sea; no house here is of stone)
                p.house(c0, r0, c1, r1, seed, style=(3, 0, 3, 4, 3, 5)[int(hsh(seed, 0, 0, 917) * 6)])
        # the harbour office: the only house of two storeys with a light in every window - and a hearth
        c0, r0, c1, r1 = SPECIAL['o']
        x0, z0, x1, z1, top = p.house(c0, r0, c1, r1, 931, hollow=True, doors=(('w', 5),), storeys=2, style=4, door_height=3)
        cx, cz = (x0 + x1) // 2, (z0 + z1) // 2
        b.hearth(cx + 1, g + 1, cz, 'harbour')
        b.set(cx + 1, g, cz, 'minecraft:stone_bricks')
        b.loot(x1 - 1, g + 1, z0 + 1, 'wharf_office', 'west')
        b.set(x1 - 1, g + 1, z1 - 1, 'minecraft:lectern[facing=west,has_book=false,powered=false]')
        b.set(x1 - 1, g + 1, z1 - 2, 'minecraft:barrel[facing=up]')
        b.set(x0 + 1, g + 1, z1 - 1, f'{MOD}:forge')
        b.set(x0 + 1, g, z1 - 1, 'minecraft:stone_bricks')
        for lx, lz in ((x0 + 3, z0 + 3), (x1 - 3, z1 - 3)):
            b.set(lx, g + 4, lz, LANTERN)
        self.office = (cx, cz)
        # the net loft: a long shed open to the quay, nets hung from its beams, a floor above them
        c0, r0, c1, r1 = SPECIAL['n']
        x0, z0, x1, z1, top = p.house(c0, r0, c1, r1, 932, hollow=True, doors=(('s', 4), ('s', 5), ('s', 9), ('s', 10)), storeys=1, style=3, door_height=3)
        for x in range(x0 + 2, x1 - 1, 3):
            for z in range(z0 + 2, z1 - 1, 2):
                if hsh(x, 4, z, 918) < 0.7:
                    b.set(x, g + 4, z, 'minecraft:cobweb')
                    if hsh(x, 5, z, 918) < 0.5:
                        b.set(x, g + 3, z, 'minecraft:cobweb')
        b.loot(x0 + 1, g + 1, z0 + 1, 'wharf_loft', 'south')
        b.barrels([(x1 - 1, g + 1, z0 + 1), (x1 - 1, g + 2, z0 + 1), (x1 - 2, g + 1, z0 + 1)])
        for lx in range(x0 + 3, x1 - 1, 4):
            for lz in (z0 + 3, z1 - 3):
                b.set(lx, g + 4, lz, LANTERN)
        self.loft = (x0, z0, x1, z1)
        # the boat shed by the slip: a boat on the stocks that was never finished
        c0, r0, c1, r1 = SPECIAL['s']
        x0, z0, x1, z1, top = p.house(c0, r0, c1, r1, 933, hollow=True, doors=(('s', 2), ('s', 3)), storeys=1, style=3, door_height=3)
        self.boat(x0 + 2, g + 1, z0 + 3, 5, afloat=False, ribs=True)
        b.loot(x1 - 1, g + 1, z0 + 1, 'wharf_shed', 'west')
        b.set(x0 + 1, g + 4, z1 - 2, LANTERN)
        self.shed_at = (x0, z0, x1, z1)
        p.street_dressing()

    def shed(self, c0, r0, c1, r1, seed):
        """A strip too narrow for a house: racks for drying fish, or a stack of what came ashore last."""
        b, g, p = self.b, self.g, self.port
        x0, z0 = p.at(c0, r0, 0, 0)
        x1, z1 = p.at(c1, r1, 2, 2)
        for x in range(x0, x1 + 1):
            for z in range(z0, z1 + 1):
                b.set(x, g, z, port_ground(x, g, z))
        if hsh(seed, 1, 0, 919) < 0.5:
            self.racks(x0, z0, x1, z1, seed)
        else:
            for x in range(x0, x1 + 1):
                for z in range(z0, z1 + 1):
                    n = hsh(x, 6, z, 920 + seed % 5)
                    if n < 0.5:
                        b.set(x, g + 1, z, 'minecraft:barrel[facing=up]' if n < 0.3 else ('minecraft:hay_block[axis=x]' if n < 0.38 else PLANK))
                        if n < 0.14:
                            b.set(x, g + 2, z, 'minecraft:barrel[facing=up]')

    def racks(self, x0, z0, x1, z1, seed):
        """Fish hung out to dry on poles, and never taken in."""
        b, g = self.b, self.g
        longways = x1 - x0 >= z1 - z0
        for k in range(0, (x1 - x0 if longways else z1 - z0) + 1):
            x, z = (x0 + k, (z0 + z1) // 2) if longways else ((x0 + x1) // 2, z0 + k)
            if not b.free(x, g + 1, z):
                continue
            if k % 4 == 0:
                b.fill(x, g + 1, z, x, g + 3, z, FENCE)
            else:
                b.set(x, g + 3, z, FENCE)
                if hsh(x, 7, z, 921 + seed % 7) < 0.6:
                    b.set(x, g + 2, z, CHAIN % 'y' if hsh(x, 8, z, 921) < 0.4 else 'minecraft:cobweb')

    def waterfront(self):
        """The quay: dressed stone, bollards, a crane, what was landed and never carried up; the slip at its west end."""
        b, g = self.b, self.g
        z0, z1 = QUAY
        x0, x1 = PX - 3, PX + PCOLS * T + 2
        for x in range(x0, x1 + 1):
            if self.kind.get((x, z1)) != 'quay':
                continue
            if x % 7 == 3 and not PIER[0] - 2 <= x <= PIER[1] + 2:
                b.set(x, g + 1, z1, 'minecraft:cobblestone_wall')                     # a bollard
                self.keep(x, z1, x, z1)
            if x % 21 == 10 and not PIER[0] - 4 <= x <= PIER[1] + 4:
                gen_village.lamp_post(b, x, g + 1, z0)
                self.keep(x, z0, x, z0)
        # the crane: a post, a jib out over the water, a hook on its chain
        cx = PIER[1] + 9
        b.fill(cx, g + 1, z1 - 1, cx, g + 6, z1 - 1, DARK_POST)
        for k in range(-2, 5):
            b.set(cx, g + 6, z1 - 1 + k, 'minecraft:dark_oak_log[axis=z]')
        b.set(cx, g + 5, z1, DARK_FENCE)
        b.set(cx, g + 5, z1 - 2, DARK_FENCE)
        for y in range(g + 3, g + 6):
            b.set(cx, y, z1 + 3, CHAIN % 'y')
        b.set(cx, g + 2, z1 + 3, 'minecraft:barrel[facing=up]')
        self.keep(cx - 1, z1 - 3, cx + 1, z1)
        # stacks of what was landed
        for sx, n_ in ((PIER[0] - 12, 5), (PIER[1] + 16, 4), (PX + 40, 4), (PX + 96, 6)):
            for k in range(n_):
                x, z = sx + k % 3, z0 + 1 + k // 3
                if b.free(x, g + 1, z):
                    b.set(x, g + 1, z, 'minecraft:barrel[facing=up]' if hsh(x, 9, z, 922) < 0.6 else PLANK)
                    self.keep(x, z, x, z)
        # boats drawn up on the hard, either side of the boat shed's doors
        self.boat(178, g + 1, 687, 5, afloat=False)
        self.boat(191, g + 1, 687, 5, afloat=False, keel_up=True)
        self.keep(176, 686, 180, 692)
        self.keep(189, 686, 193, 692)

    def boat(self, x, y, z, length, afloat=True, keel_up=False, ribs=False, axis='z'):
        """A karbas: a boat of five strakes, a man and a half long. Afloat it lies in the sea's own layer - a low hull
        of tarred wood, her thwarts showing; ashore she stands on her keel, or lies bottom up; `ribs` is one on the
        stocks, not yet planked."""
        b = self.b

        def at(k, s):
            return (x + s, z + k) if axis == 'z' else (x + k, z + s)
        for k in range(length):
            half = 0 if k in (0, length - 1) else 1
            for s in range(-half, half + 1):
                px, pz = at(k, s)
                self.taken.add((px, pz))
                if afloat:
                    b.set(px, y, pz, TARRED if abs(s) == half or half == 0 else PLANK)
                    if half and s == 0 and k % 2 == 0:
                        b.set(px, y + 1, pz, 'minecraft:spruce_trapdoor[facing=north,half=bottom,open=false,powered=false,waterlogged=false]')
                elif keel_up:
                    b.set(px, y, pz, TARRED)
                    if s == 0 and half:
                        b.set(px, y + 1, pz, 'minecraft:dark_oak_slab[type=bottom,waterlogged=false]')
                elif ribs:
                    if s == 0:
                        b.set(px, y, pz, 'minecraft:stripped_spruce_log[axis=%s]' % axis)
                    elif k % 2 == 1:
                        b.set(px, y, pz, FENCE)
                        b.set(px, y + 1, pz, FENCE)
                elif half == 0:
                    b.set(px, y, pz, TARRED)
                elif s == 0:
                    b.set(px, y, pz, SLAB % 'bottom')
                else:
                    side = ('east' if s < 0 else 'west') if axis == 'z' else ('south' if s < 0 else 'north')
                    b.set(px, y, pz, f'minecraft:dark_oak_stairs[facing={side},half=top,shape=straight,waterlogged=false]')

    def piers(self):
        """The long pier, and two jetties for the boats."""
        b, g, sea = self.b, self.g, self.sea
        x0, x1, zend = PIER
        for z in range(QUAY[1] + 1, zend + 1):
            for x in range(x0, x1 + 1):
                b.set(x, g, z, deck(x, g, z))
                self.taken.add((x, z))
            if (z - QUAY[1]) % 4 == 1 or z == zend:
                for x in (x0, x1):
                    b.fill(x, sea - 1, z, x, g, z, POST)
        # a rail either side: posts and a bar, with gaps where boats lay and at the gangway
        gaps = {x0: (range(711, 717),), x1: (range(GANGWAY - 1, GANGWAY + 2), range(723, 729))}
        for x in (x0, x1):
            for z in range(QUAY[1] + 2, zend + 1):
                if any(z in gap for gap in gaps[x]):
                    continue
                b.set(x, g + 1, z, FENCE)
        for x in range(x0, x1 + 1):
            b.set(x, g + 1, zend, FENCE)
        for z in range(QUAY[1] + 6, zend, 12):                   # lamps on the rail
            for x in (x0, x1) if (z // 12) % 2 else (x1, x0):
                if 'fence' in str(b.get(x, g + 1, z)):
                    b.set(x, g + 2, z, FENCE)
                    b.set(x, g + 3, z, FLOOR_LANTERN)
                    break
        b.barrels([(x0 + 1, g + 1, 720), (x0 + 1, g + 2, 720), (x0 + 1, g + 1, 721), (x1 - 1, g + 1, 742), (x0 + 1, g + 1, zend - 1)])
        b.set(x1 - 1, g + 1, 741, 'minecraft:hay_block[axis=x]')
        self.boat(x0 - 2, sea, 711, 6)
        self.boat(x1 + 2, sea, 723, 6)
        # the jetties
        for jx, jz in ((200, 716), (270, 712)):
            for z in range(QUAY[1] + 1, jz + 1):
                for x in (jx, jx + 1, jx + 2):
                    b.set(x, g, z, deck(x, g, z))
                    self.taken.add((x, z))
                if (z - QUAY[1]) % 4 == 1 or z == jz:
                    for x in (jx, jx + 2):
                        b.fill(x, sea - 1, z, x, g, z, POST)
                        if z == jz or (z - QUAY[1]) % 8 == 1:
                            b.set(x, g + 1, z, FENCE)
            self.boat(jx - 2, sea, jz - 9, 6)
            self.boat(jx + 4, sea, jz - 13, 6)
            self.boat(jx + 4, sea, jz - 5, 5)
        self.jetties = ((200, 716), (270, 712))

    # ------------------------------------------------------------------ ships

    def hull(self, cx, z0, length, half_at):
        """The body of a ship from below the water to her deck: tarred, a pale strake along the sheer. Returns the
        deck's layer."""
        b, g, sea = self.b, self.g, self.sea
        top = g + 2
        for dz in range(length):
            half = half_at(dz)
            z = z0 + dz
            for s in range(-half, half + 1):
                x = cx + s
                self.taken.add((x, z))
                edge = abs(s) == half or half_at(dz - 1) < abs(s) or half_at(dz + 1) < abs(s)
                for y in range(sea - 1, top + 1):
                    if y <= sea and abs(s) == half and half > 1 and not (half_at(dz - 1) < half or half_at(dz + 1) < half):
                        b.set(x, y, z, WATER)                        # she is narrower in the water than at the rail
                        continue
                    if y == top:
                        b.set(x, y, z, deck(x, y, z) if not edge else 'minecraft:stripped_dark_oak_log[axis=z]')
                    else:
                        b.set(x, y, z, TARRED if edge or y < top - 1 else PLANK)
        return top

    def rail(self, cx, z0, length, half_at, top, gaps=()):
        """Round her deck: a plank and a rail on it - what nobody gets over."""
        b = self.b
        for dz in range(length):
            half = half_at(dz)
            z = z0 + dz
            for s in range(-half, half + 1):
                x = cx + s
                edge = abs(s) == half or half_at(dz - 1) < abs(s) or half_at(dz + 1) < abs(s)
                if edge and (x, z) not in gaps:
                    b.set(x, top + 1, z, TARRED)
                    b.set(x, top + 2, z, DARK_FENCE)

    def mast(self, x, z, top, height, sail):
        """A mast with one yard; the sail on it furled, or hanging as it was let fall on a morning with no wind."""
        b = self.b
        b.fill(x, top + 1, z, x, top + height, z, POST)
        b.set(x, top + height + 1, z, FENCE)
        yard = top + height - 3
        wide = 7 if sail == 'hanging' else 5
        for s in range(-wide, wide + 1):
            if s:
                b.set(x + s, yard, z, 'minecraft:spruce_log[axis=x]' if abs(s) < wide else FENCE)
        if sail == 'furled':
            for s in range(-4, 5):
                if s:
                    b.set(x + s, yard - 1, z, SAIL if hsh(x + s, 0, z, 923) < 0.8 else SAIL_SHADE)
        else:
            for s in range(-6, 7):                               # let fall, and never sheeted home: it hangs straight down, its foot uneven
                drop = 11 - (1 if abs(s) == 6 else 0) - int(hsh(x + s, 1, z, 924) * 2.4)
                for k in range(1, drop + 1):
                    b.set(x + s, yard - k, z + 1, SAIL if hsh(x + s, k // 2, z, 925) < 0.8 else SAIL_SHADE)
        # a top to stand in, and the stay's end
        for dx, dz in ((1, 0), (-1, 0), (0, 1), (0, -1)):
            b.set(x + dx, top + height - 1, z + dz, 'minecraft:spruce_trapdoor[facing=%s,half=top,open=false,powered=false,waterlogged=false]'
                  % {(1, 0): 'east', (-1, 0): 'west', (0, 1): 'south', (0, -1): 'north'}[(dx, dz)])

    def daybreak(self):
        """The koch "Daybreak", made fast at the end of the long pier with her head to the sea. Her deck is the Helmsman's:
        one level from the cabin's bulkhead to the stem, the mast in the middle of it, the rail all round."""
        b, g, sea = self.b, self.g, self.sea
        cx, z0 = SHIP

        def half_at(dz):
            if dz < 0 or dz >= SHIP_LENGTH:
                return -1
            if dz < len(SHIP_HALF):
                return SHIP_HALF[dz]
            bow = dz - (SHIP_LENGTH - len(SHIP_BOW))
            return SHIP_BOW[bow] if bow >= 0 else 6
        top = self.hull(cx, z0, SHIP_LENGTH, half_at)
        d = top + 1                                              # the layer one stands in on her deck
        west, east = cx - 6, cx + 6
        port_in = {(west, z) for z in (GANGWAY - 1, GANGWAY, GANGWAY + 1)}
        port_out = {(east, z) for z in (OFF - 1, OFF, OFF + 1)}
        self.rail(cx, z0, SHIP_LENGTH, half_at, top, gaps=port_in | port_out)
        # the cabin: the whole of her stern, its roof a deck nobody walks (there is no way up to it)
        zc = z0 + CABIN - 1                                      # the bulkhead
        for dz in range(CABIN):
            half = half_at(dz)
            z = z0 + dz
            for s in range(-half, half + 1):
                x = cx + s
                edge = abs(s) == half or half_at(dz - 1) < abs(s) or dz == CABIN - 1
                for y in range(d, d + 3):
                    b.set(x, y, z, (TARRED if y < d + 2 else 'minecraft:stripped_dark_oak_log[axis=x]') if edge else AIR)
                b.set(x, d + 3, z, PLANK if not edge else 'minecraft:dark_oak_slab[type=bottom,waterlogged=false]')
                if not edge:
                    b.set(x, top, z, PLANK)
        b.fill(cx, d, zc, cx, d + 1, zc, AIR)                    # its door
        b.set(cx - 2, d + 1, zc, 'minecraft:glass_pane')
        b.set(cx + 2, d + 1, zc, 'minecraft:glass_pane')
        b.loot(cx - 3, d, z0 + 3, 'ship_hold', 'east')
        b.set(cx + 3, d, z0 + 3, 'minecraft:red_bed[facing=north,part=foot]')
        b.set(cx + 3, d, z0 + 2, 'minecraft:red_bed[facing=north,part=head]')
        b.set(cx, d, z0 + 2, 'minecraft:lectern[facing=south,has_book=false,powered=false]')
        b.set(cx, d + 2, z0 + 4, LANTERN)
        b.set(cx, d + 4, z0 + 1, DARK_FENCE)                     # the stern lantern
        b.set(cx, d + 5, z0 + 1, FLOOR_LANTERN)
        # the helm before the cabin door, to one side: he never left it far
        b.set(cx + 3, d, zc + 2, 'minecraft:grindstone[face=floor,facing=north]')
        # the gangways: three wide, a post either side and a beam over, so that what is set across them fills them
        for (x, zz, out) in ((west, GANGWAY, -1), (east, OFF, 1)):
            for z in (zz - 2, zz + 2):
                b.fill(x, d, z, x, d + 2, z, DARK_POST)
            for z in range(zz - 2, zz + 3):
                b.set(x, d + 3, z, 'minecraft:dark_oak_log[axis=z]')
        # from the pier: two steps up to her
        for z in (GANGWAY - 1, GANGWAY, GANGWAY + 1):
            b.set(west - 2, g + 1, z, stairs('spruce', 'east'))
            b.set(west - 1, g + 1, z, PLANK)
            b.set(west - 1, g + 2, z, stairs('spruce', 'east'))
            b.set(west - 2, g, z, PLANK)
            self.taken.add((west - 1, z))
            self.taken.add((west - 2, z))
        b.fog('helmsman_fog', west, d, GANGWAY - 1, west, d + 2, GANGWAY + 1, 'z')
        # and off her, on to the mole: a step down
        for z in (OFF - 1, OFF, OFF + 1):
            b.set(east + 1, top, z, stairs('spruce', 'west'))
            self.taken.add((east + 1, z))
        b.fog('helmsman', east, d, OFF - 1, east, d + 2, OFF + 1, 'z', sealed=True)
        b.threshold('ship', west + 1, d, GANGWAY - 1, west + 2, GANGWAY + 1)
        # her deck: the mast, a hatch with its grating on, the windlass forward, a little of her cargo - none of it by the rail
        mz = z0 + 23
        self.mast(cx, mz, top, 26, 'hanging')
        for x in range(cx - 1, cx + 2):
            for z in range(mz + 5, mz + 8):
                b.set(x, top, z, 'minecraft:spruce_trapdoor[facing=north,half=top,open=false,powered=false,waterlogged=false]')
        wz = z0 + SHIP_LENGTH - 9
        b.set(cx - 1, d, wz, 'minecraft:spruce_log[axis=x]')
        b.set(cx, d, wz, 'minecraft:spruce_log[axis=x]')
        b.set(cx + 1, d, wz, 'minecraft:spruce_log[axis=x]')
        for y in range(sea, d + 1):                              # the cable, over the bow and down: there is nothing on its end
            b.set(cx, y, z0 + SHIP_LENGTH, CHAIN % 'y')
        b.fill(cx, d, z0 + SHIP_LENGTH - 1, cx, d + 3, z0 + SHIP_LENGTH - 1, DARK_POST)      # the stem, and the sternpost
        b.fill(cx, d + 4, z0, cx, d + 5, z0, DARK_POST)
        for k in range(1, 6):                                    # the bowsprit
            b.set(cx, d + 1 + k // 2, z0 + SHIP_LENGTH - 2 + k, 'minecraft:spruce_log[axis=z]' if k < 5 else FENCE)
        b.barrels([(cx - 3, d, mz - 6), (cx - 2, d, mz - 6), (cx + 3, d, mz + 9)])
        b.set(cx + 2, d, mz + 9, 'minecraft:hay_block[axis=z]')
        for z in (zc + 4, mz, mz + 10):
            for x in (west, east):
                if 'fence' in str(b.get(x, d + 1, z)):
                    b.set(x, d + 2, z, FLOOR_LANTERN)
        self.mob('helmsman', cx, d, zc + 4, 'south')
        self.deck_layer = d
        self.mast_at = (cx, mz)

    def at_anchor(self, cx, z0, length, n):
        """A ship in the roads: come as far as the tide let her on that morning, and no further since."""
        b, g, sea = self.b, self.g, self.sea
        taper = (5, 5, 4, 4, 3, 3, 2, 1, 0)

        def half_at(dz):
            if dz < 0 or dz >= length:
                return -1
            if dz < 3:
                return 3 + dz // 2 + (1 if dz == 2 else 0)
            bow = dz - (length - len(taper))
            return taper[bow] if bow >= 0 else 5
        top = self.hull(cx, z0, length, half_at)
        self.rail(cx, z0, length, half_at, top)
        d = top + 1
        for dz in range(7):                                      # a low cabin aft
            half = half_at(dz)
            for s in range(-half + 1, half):
                b.set(cx + s, d, z0 + dz, TARRED)
                b.set(cx + s, d + 1, z0 + dz, TARRED if dz < 6 else 'minecraft:stripped_dark_oak_log[axis=x]')
                b.set(cx + s, d + 2, z0 + dz, 'minecraft:dark_oak_slab[type=bottom,waterlogged=false]')
        self.mast(cx, z0 + length // 2, top, 18 + 2 * (n % 2), 'furled')
        if length > 36:
            self.mast(cx, z0 + length - 12, top, 13, 'furled')
        b.set(cx, d + 3, z0 + 1, DARK_FENCE)
        b.set(cx, d + 4, z0 + 1, FLOOR_LANTERN)
        for y in range(sea - 1, d + 1):                          # she lies to her anchor
            b.set(cx + 1, y, z0 + length, CHAIN % 'y')

    # ------------------------------------------------------------------ the mole

    def mole(self):
        """The old mole, from the Daybreak's other side to the sand of the spit: big stone, weed on it; the sea has
        taken two blocks of it in one place and let it slump to the water in another."""
        b, g, sea = self.b, self.g, self.sea
        x0, x1, mz = MOLE
        for x in range(x0, x1 + 1):
            if GAP[0] <= x <= GAP[1]:
                continue
            low = SLUMP[0] <= x <= SLUMP[1]
            step = x in (SLUMP[0] - 1, SLUMP[1] + 1)
            topy = sea if low else (g if step else g + 1)
            if x > x1 - 3:
                topy = g + 1 - (x - (x1 - 3))                    # down to the sand, a step at a time
            for z in range(mz - 1, mz + 2):
                if self.kind.get((x, z)) in ('wet', 'sand'):
                    continue
                wide = low or hsh(x, 0, z, 926) < 0.9
                for y in range(sea - 1, topy + 1):
                    b.set(x, y, z, mole_stone(x, y, z) if wide or y < topy else AIR)
                for y in range(topy + 1, g + 2):
                    if b.get(x, y, z) is None and y <= g:
                        b.set(x, y, z, AIR)
                if low and hsh(x, 1, z, 927) < 0.5:
                    b.set(x, sea + 1, z, FILM)
                self.taken.add((x, z))
        b.threshold('mole', x0 + 2, g + 2, mz - 1, x0 + 3, mz + 1)
        # a lamp post near its head, long out
        b.set(x0 + 6, g + 1, mz + 1, mole_stone(x0 + 6, g + 1, mz + 1))
        b.fill(x0 + 6, g + 2, mz + 1, x0 + 6, g + 4, mz + 1, FENCE)
        b.set(x0 + 6, g + 5, mz + 1, FLOOR_LANTERN)

    # ------------------------------------------------------------------ sand and stones

    def strew(self):
        """What the sea leaves on sand: driftwood, weed, a stone here and there; and boulders on the strand."""
        b, g, sea = self.b, self.g, self.sea
        for (x, z), kind in sorted(self.kind.items()):
            if (x, z) in self.taken:
                continue
            n = hsh(x, 10, z, 929)
            if kind == 'sand':
                if n < 0.004 and all(self.kind.get((x + k, z)) == 'sand' and (x + k, z) not in self.taken for k in (1, 2)):
                    for k in range(3):                               # a log come ashore
                        b.set(x + k, g + 1, z, 'minecraft:stripped_spruce_log[axis=x]')
                        self.taken.add((x + k, z))
                elif n < 0.012:
                    b.set(x, g + 1, z, 'minecraft:dead_bush')
                elif n < 0.02 and coast(x) + 6 > z:
                    b.set(x, g + 1, z, 'minecraft:short_grass')
                    b.set(x, g, z, 'minecraft:coarse_dirt')
            elif kind == 'wade' and n < 0.06:
                b.set(x, sea, z, 'minecraft:seagrass')
            elif kind == 'stones':
                self.boulder(x, z, n)
            elif kind == 'deep' and n < 0.0006 and 8 <= x < self.w - 8 and z < self.d - 8 and self.clear_of_ways(x, z, 9):
                self.skerry(x, z, 2 + int(hsh(x, 11, z, 930) * 4))

    def no_pits(self):
        """Among the boulders nothing may be a pit: a place on the strand that is not come to from its way, a step up or
        down at a time, is filled with stone to the height of what stands round it."""
        b, sea = self.b, self.sea
        ground = {c for c, kind in self.kind.items() if kind in ('stones', 'wet', 'wade', 'sand')}

        def top(c):
            for y in range(sea + 8, sea - 1, -1):
                s = b.get(c[0], y, c[1])
                if s not in (None, AIR) and FILM not in str(s) and 'water' not in str(s) and 'place_name' not in str(s) and 'mob_mark' not in str(s) \
                        and 'seagrass' not in str(s) and 'shard' not in str(s):
                    return y
            return sea - 1
        start = (STRAND[0][0][0], STRAND[0][0][1])
        seen = {start}
        queue = [start]
        while queue:
            c = queue.pop()
            for dx, dz in ((1, 0), (-1, 0), (0, 1), (0, -1)):
                n = (c[0] + dx, c[1] + dz)
                if n in ground and n not in seen and abs(top(n) - top(c)) <= 1:
                    seen.add(n)
                    queue.append(n)
        filled = 0
        for c in sorted(ground - seen):
            if self.kind[c] != 'stones' or top(c) > sea + 1:
                continue
            around = [top((c[0] + dx, c[1] + dz)) for dx, dz in ((1, 0), (-1, 0), (0, 1), (0, -1)) if (c[0] + dx, c[1] + dz) in ground]
            for y in range(sea + 1, max(around + [sea + 2]) + 1):
                b.set(c[0], y, c[1], stack(c[0], y, c[1]))
            filled += 1
        return filled

    def clear_of_ways(self, x, z, r):
        return all(self.kind.get((x + dx, z + dz), 'deep') == 'deep' and (x + dx, z + dz) not in self.taken
                   for dx in range(-r, r + 1, 3) for dz in range(-r, r + 1, 3))

    def boulder(self, x, z, n):
        """On the strand: stones as big as a barrel, and some as big as a shed. The way between them is kept (`taken`);
        the big ones stand four high and sheer, so that none of them is a step to anything."""
        b, sea = self.b, self.sea
        if n > 0.085 or (x, z) in self.taken:
            return
        r = 1 if n > 0.05 else (2 if n > 0.02 else 3)
        high = (1, 3, 5)[r - 1] + int(hsh(x, 12, z, 931) * (1.99 if r > 1 else 0.99))
        for dx in range(-r, r + 1):
            for dz in range(-r, r + 1):
                cx, cz = x + dx, z + dz
                kind = self.kind.get((cx, cz))
                if kind not in ('stones', 'wade', 'deep') or (cx, cz) in self.taken:
                    continue
                d = math.hypot(dx, dz) + (hsh(cx, 13, cz, 932) - 0.5) * 0.8
                if d > r + 0.2:
                    continue
                h = max(1, round(high * (1 - (d / (r + 0.9)) ** 2)))
                for y in range(sea - 1, sea + 1 + h):
                    if y > sea or kind != 'stones':
                        b.set(cx, y, cz, stack(cx, y, cz))

    def skerry(self, x, z, high):
        """A rock out in the water, for the mist to stand behind."""
        b, sea = self.b, self.sea
        r = 1 + high // 2
        for dx in range(-r, r + 1):
            for dz in range(-r, r + 1):
                d = math.hypot(dx, dz) + (hsh(x + dx, 14, z + dz, 933) - 0.5) * 0.8
                if d > r:
                    continue
                h = round((high + 1) * (1 - d / (r + 0.6)))
                for y in range(sea - 1, sea + 1 + h):
                    b.set(x + dx, y, z + dz, stack(x + dx, y, z + dz))
                self.taken.add((x + dx, z + dz))

    def karbas(self):
        """The spit's root: an upturned karbas on four posts, and a fire under it."""
        b, g = self.b, self.g
        kx, kz = KARBAS
        for dz in range(-4, 5):
            half = 0 if abs(dz) == 4 else (1 if abs(dz) == 3 else 2)
            for dx in range(-half, half + 1):
                x, z = kx + dx, kz + dz
                b.set(x, g + 3 if abs(dx) == half and half else g + 4, z, TARRED)
                if dx == 0 and half == 2:
                    b.set(x, g + 5, z, 'minecraft:dark_oak_slab[type=bottom,waterlogged=false]')
                self.taken.add((x, z))
        for dx, dz in ((-2, -2), (2, -2), (-2, 2), (2, 2)):
            b.fill(kx + dx, g + 1, kz + dz, kx + dx, g + 2, kz + dz, FENCE)
        b.hearth(kx, g + 1, kz, 'karbas')
        b.set(kx, g, kz, 'minecraft:cobblestone')
        b.set(kx + 1, g + 3, kz + 1, LANTERN)
        b.set(kx - 1, g + 1, kz + 2, 'minecraft:barrel[facing=up]')
        b.set(kx + 1, g + 1, kz - 3, 'minecraft:stripped_spruce_log[axis=x]')
        b.set(kx + 2, g + 1, kz - 3, 'minecraft:stripped_spruce_log[axis=x]')
        self.keep(kx - 3, kz - 5, kx + 3, kz + 5)
        self.name('spit', 350, MOLE[2] - 3, 351, MOLE[2] + 3)

    def wreck(self):
        """Half-way down the spit: a boat's ribs standing out of the sand, and what was in her."""
        b, g = self.b, self.g
        wx, wz = WRECK
        y = self.on(wx, wz)
        for k in range(-5, 6):
            z = wz + k
            b.set(wx, y, z, 'minecraft:stripped_dark_oak_log[axis=z]' if abs(k) < 5 else TARRED)
            self.taken.add((wx, z))
            if k % 2 == 0 and abs(k) < 5:
                for s, lean in ((-1, 2), (1, 3)):
                    for h in range(lean - (1 if abs(k) == 4 else 0)):
                        b.set(wx + s * (1 if h < 2 else 2), y + h, z, DARK_FENCE)
                    self.taken.add((wx + s, z))
                    self.taken.add((wx + 2 * s, z))
        b.loot(wx + 1, y, wz + 1, 'spit_wreck', 'east')
        self.keep(wx - 3, wz - 6, wx + 3, wz + 6)

    # ------------------------------------------------------------------ the shoal

    def shoal(self):
        """The shoal under the lighthouse: a floor of sand under a hand's depth of water, stacks of rock all round it,
        an arch through them on the east and another on the north. Boats lie on it bottom up, as boats are left; one
        of them is not a boat."""
        b, g, sea = self.b, self.g, self.sea
        sx, sz = SHOAL
        east = sx + SHOAL_R + 2                                  # the plane of the arch the strand comes to
        north = sz - SHOAL_R - 2
        for dx in range(-RING - 1, RING + 2):
            for dz in range(-RING - 1, RING + 2):
                x, z = sx + dx, sz + dz
                r = math.hypot(dx, dz)
                if r > RING + 0.5:
                    continue
                self.taken.add((x, z))
                if r <= SHOAL_R + 0.3:
                    continue
                arch_e = abs(dz) <= 1 and dx > 0
                arch_n = abs(dx) <= 1 and dz < 0
                high = 4 + int(smooth(x, z, 3, 934) * 4.5) + (1 if r < SHOAL_R + 2 else 0)
                if arch_e or arch_n:
                    for y in range(sea + 1, sea + 5):
                        b.set(x, y, z, AIR)
                    for y in range(sea + 5, sea + 5 + 2):
                        b.set(x, y, z, stack(x, y, z))
                    b.set(x, sea, z, shingle(x, sea, z))
                    continue
                for y in range(sea, sea + 1 + high):
                    b.set(x, y, z, stack(x, y, z))
        b.fog('capsized_fog', east, sea + 1, sz - 1, east, sea + 4, sz + 1, 'z')
        b.fog('capsized', sx - 1, sea + 1, north, sx + 1, sea + 4, north, 'x', sealed=True)
        self.name('shoal', east + 4, sz - 2, east + 5, sz + 2)
        # the boats that are boats
        for bx, bz, axis in ((sx - 6, sz + 4, 'x'), (sx + 5, sz + 6, 'z'), (sx - 7, sz - 5, 'z')):
            self.boat(bx, sea + 1, bz, 5, afloat=False, keel_up=True, axis=axis)
        self.mob('capsized', sx + 1, sea + 1, sz - 4, 'east')
        self.arch = (east, north)

    # ------------------------------------------------------------------ the lighthouse

    def lighthouse(self):
        """The rock, and the tower on it: round, banded white and red once, a door to the east where the causeway comes
        up, a stair round the inside of the wall, the lamp room as wide as the tower and a gallery outside it."""
        b, g, sea = self.b, self.g, self.sea
        lx, lz = LIGHT
        base = g + 2                                             # the top block of the rock under the tower
        for dx in range(-ROCK_R - 3, ROCK_R + 4):
            for dz in range(-ROCK_R - 3, ROCK_R + 4):
                x, z = lx + dx, lz + dz
                if self.kind.get((x, z)) != 'rock':
                    continue
                r = math.hypot(dx, dz) + (smooth(x, z, 4, 935) - 0.5) * 1.6
                top = base if r <= 9.2 else (g + 1 if r <= 10.4 else (g if r <= 11.6 else sea))
                if dx >= 5 and abs(dz) <= 2:                     # the landing: a terrace before the door, and steps down to the causeway
                    top = base - max(0, dx - 7) if dx <= 10 else sea
                for y in range(sea - 1, top + 1):
                    b.set(x, y, z, stack(x, y, z))
                for y in range(top + 1, g + 1):
                    b.set(x, y, z, AIR)
                self.taken.add((x, z))

        def wall(x, y, z):
            n = hsh(x, y, z, 936)
            if (y - base) // 6 % 2 == 1:
                return 'minecraft:bricks' if n < 0.8 else 'minecraft:granite'
            return 'minecraft:calcite' if n < 0.5 else ('minecraft:diorite' if n < 0.8 else 'minecraft:white_terracotta')
        floor = base + 1                                         # the layer one stands in at the foot of the tower
        lamp = base + TOWER_H                                    # the floor block of the lamp room
        for dx in range(-7, 8):
            for dz in range(-7, 8):
                x, z = lx + dx, lz + dz
                r = math.hypot(dx, dz)
                shell = TOWER_R - 1.1 < r <= TOWER_R
                if r <= TOWER_R:
                    b.set(x, base, z, 'minecraft:stone_bricks')
                    for y in range(floor, lamp):
                        b.set(x, y, z, wall(x, y, z) if shell else AIR)
                    b.set(x, lamp, z, 'minecraft:stone_bricks' if shell else PLANK)
                    # the lamp room: a low wall, glass over it, a roof like a cap
                    for y in range(lamp + 1, lamp + 5):
                        b.set(x, y, z, ('minecraft:stone_bricks' if y == lamp + 1 or (dx == 0 or dz == 0 or abs(dx) == abs(dz)) and y == lamp + 4 else 'minecraft:glass')
                              if shell else AIR)
                elif r <= TOWER_R + 1.9:                         # the gallery
                    b.set(x, lamp, z, 'minecraft:stone_brick_slab[type=top,waterlogged=false]')
                    if r > TOWER_R + 1.0:
                        b.set(x, lamp + 1, z, BARS)
                for k, rr in enumerate((5.2, 4.0, 2.8, 1.5)):
                    if r <= rr:
                        b.set(x, lamp + 5 + k, z, 'minecraft:deepslate_tiles')
        b.set(lx, lamp + 9, lz, 'minecraft:lightning_rod[facing=up,powered=false,waterlogged=false]')
        # the lamp itself
        b.set(lx, lamp + 1, lz, 'minecraft:polished_deepslate')
        b.set(lx, lamp + 2, lz, 'minecraft:ochre_froglight[axis=y]')
        b.set(lx, lamp + 3, lz, 'minecraft:ochre_froglight[axis=y]')
        # the stair: round a square of five inside the wall, a step to a cell, a turn and a half to the top; the last
        # step lies in the lamp room's floor
        ring = [(2, k) for k in range(-1, 3)] + [(k, 2) for k in range(1, -3, -1)] + [(-2, k) for k in range(1, -3, -1)] + [(k, -2) for k in range(-1, 3)]
        facing_of = {(0, 1): 'south', (0, -1): 'north', (1, 0): 'east', (-1, 0): 'west'}
        steps = lamp - floor + 1
        for i in range(steps):
            dx, dz = ring[(i + 1) % len(ring)]
            nx, nz = ring[(i + 2) % len(ring)]
            x, y, z = lx + dx, floor + i, lz + dz
            b.set(x, y, z, stairs('spruce', facing_of[(nx - dx, nz - dz)]))
            if y - 1 >= floor:
                b.set(x, y - 1, z, PLANK)
            if y < lamp:
                for h in (1, 2, 3):                              # room for the head, through the lamp room's floor too
                    if y + h == lamp:
                        b.set(x, lamp, z, AIR)
            b.flights.append((x, y, z))
        # the door, east: three high; the Pilot stands outside it
        for y in range(floor, floor + 3):
            b.set(lx + 4, y, lz, AIR)
        # a door from the lamp room on to the gallery, towards the sunrise
        for y in (lamp + 1, lamp + 2):
            b.set(lx, y, lz + 4, AIR)
        # the foot of the tower: the hearth in the middle of the floor, and what the keeper kept
        b.hearth(lx, floor, lz, 'lighthouse')
        b.loot(lx - 3, floor, lz, 'lighthouse_cellar', 'east')
        b.barrels([(lx - 3, floor, lz - 1), (lx - 3, floor, lz + 1), (lx - 1, floor, lz + 3)])
        b.loot(lx - 2, lamp + 1, lz - 2, 'lighthouse_lamp', 'south')
        for k, y in enumerate(range(floor + 4, lamp - 2, 5)):    # windows up the tower, and a light by each
            if k % 2:
                b.set(lx - 4, y, lz, 'minecraft:glass_pane')
                b.set(lx - 3, y + 1, lz, 'minecraft:wall_torch[facing=east]')
            else:
                b.set(lx, y, lz - 4, 'minecraft:glass_pane')
                b.set(lx, y + 1, lz - 3, 'minecraft:wall_torch[facing=south]')
        b.set(lx + 3, floor + 3, lz - 1, 'minecraft:wall_torch[facing=west]')
        b.set(lx + 5, floor + 2, lz - 1, 'minecraft:wall_torch[facing=east]')
        self.floor, self.lamp = floor, lamp
        self.name('lighthouse', lx + 8, lz - 2, lx + 9, lz + 2)
        self.mob('pilot', lx + 6, floor, lz + 2, 'east')

    # ------------------------------------------------------------------ who is there, and what lies there

    def people(self):
        b, g, sea, p = self.b, self.g, self.sea, self.port

        def t(c, r, dx=1, dz=1):
            return p.at(c, r, dx, dz)
        b.zone = 'wharf'
        for kind, c, r, facing in (('dockhand', 20, 3, 'north'), ('dockhand', 21, 9, 'north'), ('harpooner', 20, 12, 'north'), ('dockhand', 12, 6, 'east'),
                                   ('dockhand', 28, 6, 'west'), ('oarsman', 6, 6, 'east'), ('dockhand', 9, 10, 'north'), ('harpooner', 31, 3, 'south'),
                                   ('dockhand', 12, 13, 'east'), ('oarsman', 27, 13, 'west'), ('dockhand', 34, 14, 'west'), ('skua', 16, 14, 'south'),
                                   ('skua', 36, 13, 'south'), ('dockhand', 27, 2, 'south'), ('skua', 28, 3, 'west'), ('dockhand', 15, 6, 'east'),
                                   ('dockhand', 25, 6, 'west'), ('dockhand', 31, 10, 'north'), ('dockhand', 4, 13, 'east'), ('oarsman', 38, 6, 'west'),
                                   ('harpooner', 38, 14, 'west')):
            x, z = t(c, r)
            self.mob(kind, x, g + 1, z, facing)
        self.mob('sand_crab', 181, self.on(181, QUAY[1] + 2), QUAY[1] + 2, 'north')
        self.mob('sand_crab', 186, self.on(186, QUAY[1] + 3), QUAY[1] + 3, 'north')
        x0, x1, zend = PIER
        for kind, x, z, facing in (('dockhand', x0 + 2, 708, 'north'), ('skua', x0 + 1, 716, 'north'), ('harpooner', x0 + 3, 730, 'north'),
                                   ('dockhand', x0 + 1, 738, 'north'), ('oarsman', x0 + 2, 747, 'north'), ('skua', x0 + 3, zend - 2, 'north')):
            self.mob(kind, x, g + 1, z, facing)
        for (jx, jz) in self.jetties:
            self.mob('skua', jx + 1, g + 1, jz - 4, 'north')
        self.mob('harpooner', self.jetties[1][0] + 1, g + 1, self.jetties[1][1] - 8, 'north')
        b.zone = 'spit'
        mz = MOLE[2]
        for kind, x, facing in (('skua', 268, 'west'), ('harpooner', 286, 'west'), ('dockhand', 294, 'west'), ('sand_crab', 306, 'west'), ('sand_crab', 309, 'west'),
                                ('oarsman', 322, 'west'), ('skua', 334, 'west')):
            self.mob(kind, x, self.on(x, mz), mz, facing)
        for kind, x, z, facing in (('sand_crab', 360, 748, 'north'), ('sand_crab', 352, 760, 'north'), ('dockhand', 362, 766, 'north'), ('sand_crab', 364, 778, 'north'),
                                   ('skua', 356, 784, 'north'), ('sand_crab', 349, 796, 'north'), ('skua', 352, 818, 'north'),
                                   ('sand_crab', 346, 808, 'north'), ('sand_crab', 344, 826, 'north'), ('sand_crab', 334, 838, 'east'), ('skua', 326, 846, 'east'),
                                   ('sand_crab', 316, 852, 'east'), ('oarsman', 304, 858, 'east'), ('sand_crab', 292, 862, 'east'), ('dockhand', 280, 864, 'east'),
                                   ('sand_crab', 270, 868, 'east'), ('harpooner', 258, 866, 'east'), ('skua', 250, 862, 'east'), ('sand_crab', 244, 860, 'east'),
                                   ('sand_crab', 226, 853, 'east'), ('sand_crab', 220, 848, 'east'), ('sand_crab', 212, 842, 'south'), ('oarsman', 209, 834, 'south'),
                                   ('skua', 214, 838, 'south')):
            self.mob(kind, x, self.on(x, z), z, facing)
        b.zone = 'lighthouse'

    def finds(self):
        b, g, sea = self.b, self.g, self.sea
        x0, x1, zend = PIER
        # a shard in the ash of a watch fire at the head of the east jetty, one where the mole has slumped, one at the far end of the sand
        jx, jz = self.jetties[0]
        b.shard(jx + 1, g + 1, jz - 1)
        for x, z in ((SLUMP[0] + 3, MOLE[2] - 1), (238, 860)):
            y = self.on(x, z)
            self.bare(x, y, z)
            b.shard(x, y, z)
            self.keep(x, z, x, z)
        cx, cz = MOLE[0] + 20, MOLE[2] + 1
        b.set(cx, g + 1, cz, mole_stone(cx, g + 1, cz))
        b.loot(cx, g + 2, cz, 'mole_end', 'north')
        bx, bz = 213, 852
        y = self.on(bx, bz)
        self.bare(bx, y, bz)
        b.loot(bx, y, bz, 'boulders_cache', 'east')
        self.keep(bx - 1, bz - 1, bx + 1, bz + 1)

    def names(self):
        b = self.b
        self.name('boulders', 229, 852, 230, 860)

    def build(self, height):
        b = self.b
        self.ground()
        self.passage(height)
        b.zone = 'wharf'
        self.town()
        self.waterfront()
        self.piers()
        self.daybreak()
        for cx, z0, length, n in ROADS:
            self.at_anchor(cx, z0, length, n)
        b.zone = 'spit'
        self.mole()
        self.karbas()
        self.wreck()
        self.shoal()
        b.zone = 'lighthouse'
        self.lighthouse()
        self.people()
        self.finds()
        self.ways()
        self.strew()
        self.no_pits()
        self.names()
        b.undertow = self.undertow()

    # ------------------------------------------------------------------ for the test

    def biome_columns(self):
        """Everything south of the cliff, and the seaward end of the passage: the mist begins under the rock."""
        return [(x, z) for x in range(0, self.w, 4) for z in range(int(coast(x)) - 14, self.d, 4)]

    def views(self):
        g, sea = self.g, self.sea
        gx, gz = GATE
        x0, x1, zend = PIER
        cx, z0 = SHIP
        d = self.deck_layer
        sx, sz = SHOAL
        lx, lz = LIGHT
        return [
            ('shore_sea_gate_from_the_yard', (gx + 0.5, g + 2.6, gz - 12), (gx + 0.5, g + 2.6, gz)),
            ('shore_passage_bend', (gx + 6, g + 2.6, BEND + 0.5), (MOUTH, g + 2.6, BEND + 0.5)),
            ('shore_out_of_the_passage', (MOUTH + 0.5, g + 2.6, self.end - 6), (MOUTH + 0.5, g + 3, QUAY[0])),
            ('shore_sea_lane', (MOUTH + 2.5, g + 2.6, PZ + 8), (MOUTH + 2.5, g + 3, zend)),
            ('shore_back_street', (PX + 30, g + 2.6, PZ + 19.5), (PX + 70, g + 3, PZ + 19.5)),
            ('shore_quay_east', (PX + 44, g + 2.6, QUAY[0] - 2), (PX + 100, g + 4, QUAY[0] + 6)),
            ('shore_quay_west', (PX + 84, g + 2.6, QUAY[0] - 2), (PX + 20, g + 4, QUAY[0] + 8)),
            ('shore_port_from_above', (x1 + 60, g + 44, QUAY[1] + 50), (x0, g + 2, PZ + 24)),
            ('shore_harbour_office', (self.office[0] - 4, g + 2.6, self.office[1] + 0.5), (self.office[0] + 3, g + 2, self.office[1] + 0.5)),
            ('shore_long_pier', (x0 + 2.5, g + 2.6, QUAY[1] + 2), (x0 + 2.5, g + 4, zend)),
            ('shore_pier_looking_back', (x0 + 2.5, g + 2.6, zend - 12), (x0 + 2.5, g + 6, QUAY[0] - 10)),
            ('shore_the_daybreak_from_the_pier', (x0 + 2.5, g + 2.6, GANGWAY - 16), (cx, g + 10, z0 + 24)),
            ('shore_the_daybreak_gangway', (x1 + 0.5, g + 2.6, GANGWAY + 0.5), (cx, d + 2, GANGWAY - 6)),
            ('shore_the_daybreak_deck', (cx - 3.5, d + 1.6, z0 + SHIP_LENGTH - 12), (cx, d + 2, z0 + CABIN)),
            ('shore_the_daybreak_deck_to_the_bow', (cx + 2.5, d + 1.6, z0 + CABIN + 2), (cx, d + 4, z0 + SHIP_LENGTH)),
            ('shore_the_daybreak_from_above', (cx + 34, g + 34, z0 + 54), (cx, g + 4, z0 + 20)),
            ('shore_roads', (x0 + 2.5, g + 2.6, zend - 3), (ROADS[2][0], g + 8, ROADS[2][1] + 10)),
            ('shore_mole', (MOLE[0] + 4.5, g + 3.6, MOLE[2] + 0.5), (MOLE[1], g + 3, MOLE[2] + 0.5)),
            ('shore_mole_gap', (GAP[0] - 5.5, g + 3.6, MOLE[2] + 0.5), (GAP[1] + 12, g + 2.5, MOLE[2] + 0.5)),
            ('shore_karbas', (KARBAS[0] - 8, g + 2.6, KARBAS[1] + 6), (KARBAS[0], g + 2.5, KARBAS[1])),
            ('shore_spit_from_its_root', (364.5, g + 2.6, 744.5), (354, g + 2, 800)),
            ('shore_spit_wet_sand', (352.5, sea + 2.6, 800.5), (336, sea + 1.5, 836)),
            ('shore_spit_from_above', (396, g + 50, 866), (330, g, 810)),
            ('shore_spit_far_arm', (300.5, sea + 2.6, 858.5), (236, sea + 2, 858)),
            ('shore_strand', (228.5, sea + 2.6, 856.5), (208, sea + 2.5, 834)),
            ('shore_strand_to_the_arch', (209.5, sea + 2.6, 832.5), (sx + SHOAL_R, sea + 3, sz)),
            ('shore_shoal', (sx + SHOAL_R - 1.5, sea + 2.6, sz + 0.5), (sx - 4, sea + 2, sz - 3)),
            ('shore_shoal_from_above', (sx + 22, g + 30, sz + 26), (sx, sea, sz)),
            ('shore_causeway', (sx + 0.5, sea + 2.6, sz - SHOAL_R - 4), (lx, g + 12, lz)),
            ('shore_lighthouse_foot', (lx + 9.5, self.floor + 1.6, lz + 3.5), (lx, self.floor + 8, lz)),
            ('shore_lighthouse_inside', (lx + 2.5, self.floor + 1.6, lz + 0.5), (lx - 2, self.floor + 6, lz - 1)),
            ('shore_lamp_room', (lx + 1.5, self.lamp + 2.6, lz + 1.5), (lx - 1, self.lamp + 2.5, lz - 1)),
            ('shore_from_the_gallery', (lx + 0.5, self.lamp + 2.6, lz + 4.6), (lx + 30, g + 2, lz + 60)),
            ('shore_from_the_gallery_to_the_port', (lx + 4.6, self.lamp + 2.6, lz - 2.5), (x0, g + 2, QUAY[1])),
            ('shore_lighthouse_from_the_pier', (x0 + 1.5, g + 2.6, zend - 1.5), (lx, g + 14, lz)),
            ('shore_whole_from_above', (300, g + 110, 930), (270, g, 780)),
        ]

    def probes(self):
        g = self.g
        return [('shore_passage', (GATE[0], g + 1, BEND - 8)), ('shore_sea_lane', (MOUTH + 2, g + 1, PZ + 20)), ('shore_pier', (PIER[0] + 2, g + 1, 730)),
                ('shore_deck', (SHIP[0] - 2, self.deck_layer, SHIP[1] + 30)), ('shore_spit', (356, g + 1, 790)), ('shore_shoal', (SHOAL[0] + 3, g, SHOAL[1] + 3)),
                ('shore_lighthouse', (LIGHT[0] + 1, self.floor, LIGHT[1] + 1))]
