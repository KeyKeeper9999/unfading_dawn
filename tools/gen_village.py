#!/usr/bin/env python3
"""The Dead Borough: the village where the game begins, built into the world's plan by gen_world.py. A walled town of
small houses under one long night - a yard with the first hearth, a tangle of lanes, a market, the butchers' row and
the churchyard where the Gravedigger works. It is the mod's own place and a copy of none.

The town is drawn on a grid of tiles of three blocks: walls, streets and the few places that are built by hand are
named here; every other tile inside the walls is a lot, and the lots are cut into houses by `lots`. Coordinates as
everywhere: x east, z south; the way through the town leads north, to the churchyard and the road to the Stockade."""
from gen_map import BARS, CHAIN, FLOOR_LANTERN, LANTERN, OPPOSITE, SOUL_LANTERN, STEP, hsh, stairs
from gen_structures import AIR, MOD

T = 3                       # blocks in a tile
COLS, ROWS = 42, 50
OPEN = '.,:%'               # tiles with nothing built on them: a street, a yard, a place built by hand, a barricade


def tiles():
    """The town as a grid of characters: ^ outside, = wall, . street, , yard, : built by hand, # a lot; small letters
    are the houses that can be entered."""
    g = [['^'] * COLS for _ in range(ROWS)]

    def rect(c0, r0, c1, r1, ch):
        for r in range(r0, r1 + 1):
            for c in range(c0, c1 + 1):
                g[r][c] = ch
    rect(13, 0, 28, 11, '=')                  # the churchyard and its wall
    rect(14, 1, 27, 10, ':')
    rect(7, 11, 34, 20, '=')                  # the butchers' quarter
    rect(8, 12, 33, 20, '#')
    rect(2, 20, 39, 43, '=')                  # the market and the borough
    rect(3, 21, 38, 42, '#')
    rect(8, 20, 33, 20, '#')
    rect(15, 43, 26, 49, '=')                 # the yard of the first hearth
    rect(16, 44, 25, 48, ':')
    for c0, r0, c1, r1 in STREETS:
        rect(c0, r0, c1, r1, '.')
    rect(13, 22, 28, 27, ':')                 # the market
    for c0, r0, c1, r1 in YARDS:
        rect(c0, r0, c1, r1, ',')
    for ch, (c0, r0, c1, r1) in SPECIAL.items():
        rect(c0, r0, c1, r1, ch)
    for c, r in ((20, 0), (21, 0), (20, 11), (21, 11), (20, 43), (21, 43), QUARRY_GATE, MIRE_GATE):
        g[r][c] = ':'                         # the three gates, and the two that are locked
    for c, r in BARRICADES:
        g[r][c] = '%'
    return g


STREETS = (
    (8, 12, 33, 12),                                           # Church Lane, under the churchyard wall
    (20, 13, 21, 20),                                          # the Shambles
    (12, 13, 12, 20), (29, 13, 29, 20), (12, 18, 19, 18), (22, 18, 29, 18),
    (3, 21, 38, 21), (3, 28, 38, 28), (7, 22, 7, 27), (34, 22, 34, 27),
    (20, 29, 21, 42),                                          # the high street
    (3, 42, 38, 42),                                           # Low Street, along the yard's wall
    (9, 36, 9, 41), (9, 36, 14, 36), (14, 31, 14, 36), (5, 31, 14, 31), (5, 29, 5, 31),          # the western way round
    (31, 38, 31, 41), (26, 38, 31, 38), (26, 33, 26, 38), (26, 33, 35, 33), (35, 29, 35, 33),    # the eastern one
    (14, 34, 19, 34), (22, 35, 26, 35),                        # across to the high street
    (3, 39, 8, 39), (22, 30, 30, 30), (16, 39, 16, 41), (35, 23, 38, 23), (3, 25, 6, 25),      # blind alleys
)
YARDS = ((35, 39, 37, 40), (13, 14, 15, 16), (30, 15, 32, 16), (17, 39, 18, 40), (29, 29, 30, 29))
# the houses a player can walk into: tiles, then where their doors are (side, tile along that side)
SPECIAL = {
    'b': (16, 13, 19, 17),        # the butchers' shop: in from the street, out by the back onto Church Lane
    'l': (23, 13, 25, 14),        # the sexton's lodge: a hearth
    'p': (10, 38, 12, 40),        # a house in the borough
    'q': (32, 39, 34, 40),        # a house with a back door to a yard
}
BARRICADES = ((20, 15), (21, 15), (29, 14))
QUARRY_GATE = (2, 42)                                          # the Quarrymen's Gate, at the west end of Low Street
MIRE_GATE = (39, 42)                                           # the Mire Gate, at its east end


def lots(g):
    """Cuts every run of lots into houses of two to four tiles a side; what is left over one tile wide is a walled
    garden. Returns the houses as (c0, r0, c1, r1)."""
    taken = [[False] * COLS for _ in range(ROWS)]
    out = []
    for r in range(ROWS):
        for c in range(COLS):
            if g[r][c] != '#' or taken[r][c]:
                continue
            run = 0
            while c + run < COLS and g[r][c + run] == '#' and not taken[r][c + run]:
                run += 1
            w = min(run, (2, 3, 2, 2, 3, 3)[int(hsh(c, 1, r, 200) * 6)])
            if run - w == 1:
                w = w + 1 if w < 3 else w - 1
            deep = 0
            while r + deep < ROWS and all(g[r + deep][c + i] == '#' and not taken[r + deep][c + i] for i in range(w)):
                deep += 1
            h = min(deep, (2, 3, 2, 3, 2, 3)[int(hsh(c, 2, r, 201) * 6)])
            if deep - h == 1:
                h = h + 1 if h < 3 else h - 1
            for dr in range(h):
                for dc in range(w):
                    taken[r + dr][c + dc] = True
            out.append((c, r, c + w - 1, r + h - 1))
    return out


# ---------------------------------------------------------------------------------------------------- materials

def cobble(x, y, z):
    n = hsh(x, 0, z, 210)
    return 'minecraft:cobblestone' if n < 0.5 else ('minecraft:mossy_cobblestone' if n < 0.72 else ('minecraft:gravel' if n < 0.86 else 'minecraft:andesite'))


def mud(x, y, z):
    n = hsh(x, 0, z, 211)
    return 'minecraft:packed_mud' if n < 0.4 else ('minecraft:coarse_dirt' if n < 0.72 else ('minecraft:rooted_dirt' if n < 0.86 else 'minecraft:podzol'))


def town_wall(x, y, z):
    n = hsh(x, y, z, 212)
    return 'minecraft:cobblestone' if n < 0.42 else ('minecraft:mossy_cobblestone' if n < 0.66 else ('minecraft:stone_bricks' if n < 0.86 else 'minecraft:mossy_stone_bricks'))


def grave_earth(x, y, z):
    """Old turf in broad patches, bare where it has been dug."""
    n = hsh(x, 0, z, 213)
    patch = hsh(x // 3, 1, z // 3, 214) * 0.6 + hsh((x + 1) // 4, 2, (z + 2) // 4, 215) * 0.4
    if patch < 0.4:
        return 'minecraft:podzol' if n < 0.88 else 'minecraft:rooted_dirt'
    if patch < 0.75:
        return 'minecraft:grass_block' if n < 0.9 else 'minecraft:moss_block'
    return 'minecraft:coarse_dirt' if n < 0.85 else 'minecraft:podzol'


STYLES = (
    # posts, beams, infill, second kind of infill, roof
    ('spruce_log', 'stripped_spruce_log', 'minecraft:white_terracotta', 'minecraft:light_gray_terracotta', 'dark_oak'),
    ('dark_oak_log', 'stripped_dark_oak_log', 'minecraft:mud_bricks', 'minecraft:packed_mud', 'spruce'),
    (None, 'stripped_spruce_log', 'minecraft:cobblestone', 'minecraft:mossy_cobblestone', 'deepslate_tile'),
    ('dark_oak_log', 'stripped_dark_oak_log', 'minecraft:spruce_planks', 'minecraft:spruce_planks', 'cobbled_deepslate'),
    ('spruce_log', 'stripped_spruce_log', 'minecraft:white_terracotta', 'minecraft:white_terracotta', 'deepslate_tile'),
    ('dark_oak_log', 'stripped_dark_oak_log', 'minecraft:light_gray_terracotta', 'minecraft:mud_bricks', 'dark_oak'),
)
WARM = 'minecraft:orange_stained_glass'


class Village:
    def __init__(self, b, vx, vz, gy):
        self.b, self.vx, self.vz, self.gy = b, vx, vz, gy
        self.g = tiles()
        self.houses = lots(self.g)
        self.kept = set()              # cells before the doors, which nothing may be put in

    # ------------------------------------------------------------------ the grid

    def at(self, c, r, dx=1, dz=1):
        """The block of a tile (its middle, unless told otherwise)."""
        return self.vx + c * T + dx, self.vz + r * T + dz

    def tile(self, c, r):
        return self.g[r][c] if 0 <= r < ROWS and 0 <= c < COLS else '^'

    def inside(self, x, z):
        """Is this block within the town (walls included)?"""
        c, r = (x - self.vx) // T, (z - self.vz) // T
        return self.tile(c, r) != '^'

    def is_open(self, c, r):
        return self.tile(c, r) in OPEN

    # ------------------------------------------------------------------ houses

    def roof(self, x0, z0, x1, z1, top, kind, ridge, end):
        """A steep roof with eaves one block out, where there is room for them; `end` fills the gables."""
        b = self.b
        along = 'z' if ridge == 'z' else 'x'
        a0, a1 = (x0, x1) if along == 'z' else (z0, z1)
        l0, l1 = (z0, z1) if along == 'z' else (x0, x1)
        span = a1 - a0 + 3
        for i in range((span + 1) // 2):
            lo, hi = a0 - 1 + i, a1 + 1 - i
            y = top + i
            for l in range(l0 - 1, l1 + 2):
                over = l < l0 or l > l1
                for a, facing in ((lo, 'east' if along == 'z' else 'south'), (hi, 'west' if along == 'z' else 'north')):
                    x, z = (a, l) if along == 'z' else (l, a)
                    if (over or i == 0) and not b.free(x, y, z):
                        continue
                    if (over or i == 0) and not self.is_open((x - self.vx) // T, (z - self.vz) // T):
                        continue                              # no eaves into a neighbour
                    b.set(x, y, z, f'minecraft:{kind}_slab[type=bottom]' if lo == hi else stairs(kind, facing))
            if i > 0:
                for l in (l0, l1):
                    for a in range(lo + 1, hi):
                        x, z = (a, l) if along == 'z' else (l, a)
                        b.set(x, y, z, end(x, y, z))
        return top + (span + 1) // 2 - 1

    def house(self, c0, r0, c1, r1, seed, hollow=False, doors=(), storeys=None, style=None, door_height=2):
        """A house on the lot: a stone foot, a frame of posts and beams filled in, one or two storeys, windows and a door
        on the sides that look onto a street. `doors` (side, block along that side) are open doorways of a house that can
        be entered; any other house gets a door that opens on boards."""
        b, gy = self.b, self.gy
        x0, z0 = self.at(c0, r0, 0, 0)
        x1, z1 = self.at(c1, r1, 2, 2)
        w, d = x1 - x0 + 1, z1 - z0 + 1
        posts, beams, fill_a, fill_b, roof_kind = STYLES[int(hsh(seed, 3, 0, 220) * len(STYLES)) if style is None else style]
        if storeys is None:
            storeys = 2 if hsh(seed, 4, 0, 221) < 0.45 else 1
        top = gy + 1 + 4 * storeys

        def infill(x, y, z):
            return fill_a if hsh(x, y, z, 222 + seed % 7) < 0.82 else fill_b

        def skin(x, y, z):
            if y == gy + 1:
                return town_wall(x, y, z)
            corner = (x in (x0, x1)) and (z in (z0, z1))
            post = corner or (x in (x0, x1) and (z - z0) % 3 == 0) or (z in (z0, z1) and (x - x0) % 3 == 0)
            if posts and post:
                return f'minecraft:{posts}[axis=y]'
            if y == gy + 5 and storeys == 2 or y == top:
                return f'minecraft:{beams}[axis={"z" if x in (x0, x1) else "x"}]'
            return infill(x, y, z)
        for x in range(x0, x1 + 1):
            for z in range(z0, z1 + 1):
                edge = x in (x0, x1) or z in (z0, z1)
                for y in range(gy + 1, top + 1):
                    if edge:
                        b.set(x, y, z, skin(x, y, z))
                    elif hollow:
                        b.set(x, y, z, AIR)
                    else:
                        b.set(x, y, z, 'minecraft:stone')
                if hollow:
                    b.set(x, gy, z, 'minecraft:spruce_planks' if hsh(x, 0, z, 223) < 0.85 else 'minecraft:dark_oak_planks')
                    if not edge:
                        b.set(x, gy + 5, z, 'minecraft:spruce_planks' if (x + z) % 4 else f'minecraft:{beams}[axis=x]')
        ridge = 'z' if d > w or (d == w and hsh(seed, 5, 0, 224) < 0.5) else 'x'
        peak = self.roof(x0, z0, x1, z1, top, roof_kind, ridge, infill)
        # the sides that look onto open ground, with the tiles they look at
        faces = []
        for side, cells in (('n', [(c, r0 - 1) for c in range(c0, c1 + 1)]), ('s', [(c, r1 + 1) for c in range(c0, c1 + 1)]),
                            ('w', [(c0 - 1, r) for r in range(r0, r1 + 1)]), ('e', [(c1 + 1, r) for r in range(r0, r1 + 1)])):
            seen = [self.is_open(c, r) for c, r in cells]
            if any(seen):
                faces.append((side, seen))

        def wall_cell(side, a):
            """The block of the wall on `side`, `a` blocks along it, and the step outwards."""
            if side == 'n':
                return x0 + a, z0, (0, -1)
            if side == 's':
                return x0 + a, z1, (0, 1)
            if side == 'w':
                return x0, z0 + a, (-1, 0)
            return x1, z0 + a, (1, 0)
        door_at = {}
        for side, a in doors:
            door_at[side] = a
        street_faces = [f for f in faces if f[0] not in door_at]
        if not doors and faces:
            # a door for show, on the side with most street before it
            side, seen = max(faces, key=lambda f: (sum(f[1]), hsh(seed, 6, ord(f[0]), 225)))
            first = seen.index(True)
            a = first * T + 1
            x, z, (ox, oz) = wall_cell(side, a)
            facing = {'n': 'north', 's': 'south', 'w': 'west', 'e': 'east'}[side]
            b.set(x, gy + 1, z, f'minecraft:spruce_door[facing={OPPOSITE[facing]},half=lower,hinge=left,open=false,powered=false]')
            b.set(x, gy + 2, z, f'minecraft:spruce_door[facing={OPPOSITE[facing]},half=upper,hinge=left,open=false,powered=false]')
            b.set(x - ox, gy + 1, z - oz, 'minecraft:spruce_planks')
            b.set(x - ox, gy + 2, z - oz, 'minecraft:spruce_planks')
            door_at[side] = a
            if hsh(seed, 7, 0, 226) < 0.4 and b.free(x + ox, gy + 4, z + oz) and b.free(x + ox, gy + 3, z + oz):
                b.set(x + ox, gy + 4, z + oz, 'minecraft:spruce_fence')
                b.set(x + ox, gy + 3, z + oz, LANTERN)
        for side, a in doors:
            x, z, (ox, oz) = wall_cell(side, a)
            b.fill(x, gy + 1, z, x, gy + door_height, z, AIR)
            b.set(x, gy, z, 'minecraft:spruce_planks')
            for k in (1, 2):
                for j in (-1, 0, 1):
                    self.kept.add((x + ox * k + oz * j, z + oz * k + ox * j))
        # windows: two blocks high, on the sides that are seen; some lit from inside, some dark, most walls blind
        for side, seen in faces:
            length = (c1 - c0 + 1 if side in 'ns' else r1 - r0 + 1) * T
            for a in range(1, length - 1):
                if a % 3 != 1 or not seen[a // T] or abs(a - door_at.get(side, -9)) < 2:
                    continue
                for floor in range(storeys):
                    n = hsh(seed * 31 + a, floor, ord(side), 227)
                    y = gy + 2 + 4 * floor + (1 if floor else 0)
                    x, z, (ox, oz) = wall_cell(side, a)
                    if hollow and floor == 0:
                        if n < 0.5:
                            b.set(x, y + 1, z, BARS)
                        continue
                    if n < (0.10 if floor == 0 else 0.2):
                        for h in (0, 1):
                            b.set(x, y + h, z, WARM)
                            b.set(x - ox, y + h, z - oz, 'minecraft:ochre_froglight[axis=y]')
                    elif n < 0.55:
                        for h in (0, 1):
                            b.set(x, y + h, z, 'minecraft:black_stained_glass')
                            b.set(x - ox, y + h, z - oz, 'minecraft:black_wool')
        # a chimney on some
        if hsh(seed, 8, 0, 229) < 0.3:
            cx, cz = (x0 + 1, z0 + 1 + int(hsh(seed, 9, 0, 230) * (d - 2))) if ridge == 'z' else (x0 + 1 + int(hsh(seed, 9, 0, 230) * (w - 2)), z0 + 1)
            for y in range(top, peak + 2):
                b.set(cx, y, cz, town_wall(cx, y, cz))
            if hsh(seed, 10, 0, 231) < 0.5:
                b.set(cx, peak + 2, cz, 'minecraft:campfire[lit=true,signal_fire=false]')
        return x0, z0, x1, z1, top

    def garden(self, c0, r0, c1, r1, seed):
        """A strip too narrow for a house: a walled garden gone wild, or a shed."""
        b, gy = self.b, self.gy
        x0, z0 = self.at(c0, r0, 0, 0)
        x1, z1 = self.at(c1, r1, 2, 2)
        if hsh(seed, 11, 0, 232) < 0.35:
            self.house(c0, r0, c1, r1, seed, storeys=1, style=3)
            return
        for x in range(x0, x1 + 1):
            for z in range(z0, z1 + 1):
                b.set(x, gy, z, mud(x, gy, z))
                if x in (x0, x1) or z in (z0, z1):
                    b.fill(x, gy + 1, z, x, gy + 2, z, town_wall)
                    b.set(x, gy + 3, z, 'minecraft:cobblestone_wall' if hsh(x, 0, z, 233) < 0.8 else 'minecraft:mossy_cobblestone_wall')
                elif hsh(x, 1, z, 234) < 0.3:
                    b.set(x, gy + 1, z, 'minecraft:dead_bush' if hsh(x, 2, z, 235) < 0.5 else 'minecraft:fern')
        cx, cz = (x0 + x1) // 2, (z0 + z1) // 2
        if hsh(seed, 12, 0, 236) < 0.7:
            dead_tree(b, cx, gy + 1, cz, 4 + int(hsh(seed, 13, 0, 237) * 3), seed)

    # ------------------------------------------------------------------ ground, walls, streets

    def ground(self):
        b, gy = self.b, self.gy
        for r in range(ROWS):
            for c in range(COLS):
                ch = self.g[r][c]
                if ch == '^':
                    continue
                x0, z0 = self.at(c, r, 0, 0)
                for x in range(x0, x0 + T):
                    for z in range(z0, z0 + T):
                        if ch == '=':
                            b.set(x, gy, z, town_wall(x, gy, z))
                        elif ch in '.%':
                            b.set(x, gy, z, cobble(x, gy, z))
                        else:
                            b.set(x, gy, z, mud(x, gy, z))

    def walls(self):
        """The town's wall, three thick and seven high, with a walk nobody walks; the churchyard's is lower, with iron on top."""
        b, gy = self.b, self.gy
        for r in range(ROWS):
            for c in range(COLS):
                if self.g[r][c] != '=':
                    continue
                x0, z0 = self.at(c, r, 0, 0)
                church = r <= 11 and 13 <= c <= 28
                for x in range(x0, x0 + T):
                    for z in range(z0, z0 + T):
                        if church:
                            b.fill(x, gy + 1, z, x, gy + 5, z, town_wall)
                            if (x + z) % 3 == 0:
                                b.set(x, gy + 6, z, 'minecraft:iron_bars')
                        else:
                            b.fill(x, gy + 1, z, x, gy + 7, z, town_wall)
                            if (x + z) % 2 == 0 and self.edge_out(x, z):
                                b.set(x, gy + 8, z, town_wall(x, gy + 8, z))

    def edge_out(self, x, z):
        return any(not self.inside(x + dx, z + dz) or self.tile((x + dx - self.vx) // T, (z + dz - self.vz) // T) != '=' for dx, dz in ((1, 0), (-1, 0), (0, 1), (0, -1)))

    def street_dressing(self):
        """What lies in the lanes: barrels and bales against the walls, never in the middle of the way; lamps on posts at
        the corners; an arch here and there where two houses lean together."""
        b, gy = self.b, self.gy
        for r in range(ROWS):
            for c in range(COLS):
                ch = self.g[r][c]
                if ch not in '.,':
                    continue
                x, z = self.at(c, r)
                n = hsh(c, 5, r, 240)
                side = [(dc, dr) for dc, dr in ((1, 0), (-1, 0), (0, 1), (0, -1)) if not self.is_open(c + dc, r + dr)]
                ways = 4 - len(side)
                if ways >= 3 and ch == '.' and (c + r) % 2 == 0 and side:
                    dc, dr = side[0]
                    if (x + dc, z + dr) not in self.kept:
                        lamp_post(b, x + dc, gy + 1, z + dr)    # a lamp where lanes meet
                    continue
                if not side:
                    continue
                if any(self.tile(c + dc, r + dr) == '%' for dc in (-1, 0, 1) for dr in (-1, 0, 1)):
                    continue                              # nothing to climb a barricade by
                dc, dr = side[int(n * 97) % len(side)]
                px, pz = x + dc, z + dr
                if not b.free(px, gy + 1, pz) or (px, pz) in self.kept:
                    continue
                if n < 0.10:
                    b.set(px, gy + 1, pz, 'minecraft:barrel[facing=up]')
                    if n < 0.04:
                        b.set(px, gy + 2, pz, 'minecraft:barrel[facing=up]')
                elif n < 0.14:
                    b.set(px, gy + 1, pz, 'minecraft:hay_block[axis=y]')
                elif n < 0.18:
                    b.set(px, gy + 1, pz, 'minecraft:cobweb')
                elif n < 0.22 and ch == ',':
                    b.set(px, gy + 1, pz, 'minecraft:dead_bush')
                elif n < 0.30 and ways == 2 and len(side) == 2 and side[0][0] == -side[1][0] and side[0][1] == -side[1][1] and ch == '.':
                    self.arch(c, r, side[0])
                elif n < 0.36:
                    wall_lamp(b, px, gy, pz, (-dc, -dr))

    def arch(self, c, r, across):
        """A beam and a floor of boards between the two houses that face each other over this tile of a lane."""
        b, gy = self.b, self.gy
        x, z = self.at(c, r)
        dc, dr = across
        for k in (-1, 0, 1):
            ax, az = x + dc * k, z + dr * k
            if not all(b.free(ax, gy + h, az) for h in (5, 6)):
                return
        for k in (-1, 0, 1):
            ax, az = x + dc * k, z + dr * k
            b.set(ax, gy + 5, az, f'minecraft:stripped_dark_oak_log[axis={"x" if dc else "z"}]')
            b.set(ax, gy + 6, az, 'minecraft:spruce_planks')
            b.set(ax, gy + 7, az, 'minecraft:spruce_slab[type=bottom]')
        b.set(x, gy + 4, z, LANTERN)

    def barricades(self):
        b, gy = self.b, self.gy
        for c, r in BARRICADES:
            x0, z0 = self.at(c, r, 0, 0)
            for x in range(x0, x0 + T):
                for z in range(z0, z0 + T):
                    n = hsh(x, 3, z, 245)
                    high = 3 if n < 0.6 else 4
                    for y in range(gy + 1, gy + 1 + high + (1 if n < 0.3 else 0)):
                        m = hsh(x, y, z, 246)
                        b.set(x, y, z, 'minecraft:barrel[facing=up]' if m < 0.35 else ('minecraft:spruce_planks' if m < 0.6 else
                                       ('minecraft:hay_block[axis=x]' if m < 0.75 else ('minecraft:dark_oak_log[axis=x]' if m < 0.9 else 'minecraft:cobblestone'))))
                    b.set(x, gy + 1 + high + (1 if n < 0.3 else 0), z, 'minecraft:spruce_fence' if n > 0.5 else 'minecraft:cobweb')

    # ------------------------------------------------------------------ the places built by hand

    def hub(self):
        """The yard where the newcomer wakes: the old south gate - the Sea Gate, an iron grate across it and its key far
        away; a hearth; the blind keykeeper under his lean-to; an anvil; and, by the way to the fire, a chest somebody
        left."""
        b, gy = self.b, self.gy
        b.zone = 'yard'
        x0, z0 = self.at(16, 44, 0, 0)
        x1, z1 = self.at(25, 48, 2, 2)
        cx = (x0 + x1) // 2
        for x in range(x0, x1 + 1):
            for z in range(z0, z1 + 1):
                b.set(x, gy, z, cobble(x, gy, z) if abs(x - cx) <= 2 or hsh(x, 0, z, 250) < 0.35 else mud(x, gy, z))
        # the south gate: an arch in the wall three wide, and the grate across the middle of it (the key of the shore:
        # it lies in the candle store of the skete). The way behind it goes down under the plateau to the sea.
        gz = z1 + 1
        for x in range(cx - 1, cx + 2):
            for z in range(gz, gz + T):
                b.fill(x, gy + 1, z, x, gy + 4, z, AIR)
                b.set(x, gy, z, cobble(x, gy, z))
        for z in range(gz, gz + T):
            b.set(cx - 1, gy + 4, z, stairs('stone_brick', 'east', 'top'))
            b.set(cx + 1, gy + 4, z, stairs('stone_brick', 'west', 'top'))
        grate = [(x, y, gz + 1) for x in range(cx - 1, cx + 2) for y in range(gy + 1, gy + 5) if b.free(x, y, gz + 1)]
        for cell in grate:
            b.set(*cell, 'unfading_dawn:locked_gate[axis=x,lock=shore]')
        b.seals['shore_key'] = grate
        for x in (cx - 2, cx + 2):                               # a lamp either side of it, on the yard's face of the wall
            b.set(x, gy + 4, gz - 1, 'minecraft:spruce_fence')
            b.set(x, gy + 3, gz - 1, LANTERN)
        self.spawn = (cx, gy + 1, z1 - 2)
        # the gate to the town, open: an arch four wide through the wall
        gx0, gzn = self.at(20, 43, 1, 0)
        for x in range(gx0 - 1, gx0 + 5):
            for z in range(gzn, gzn + T):
                b.fill(x, gy + 1, z, x, gy + 7, z, town_wall)
                if (x + z) % 2 == 0 and z != gzn + 1:
                    b.set(x, gy + 8, z, town_wall(x, gy + 8, z))
        for x in range(gx0, gx0 + 4):
            for z in range(gzn, gzn + T):
                b.fill(x, gy + 1, z, x, gy + 4, z, AIR)
                b.set(x, gy, z, cobble(x, gy, z))
        for z in range(gzn, gzn + T):
            b.set(gx0, gy + 4, z, stairs('stone_brick', 'east', 'top'))
            b.set(gx0 + 3, gy + 4, z, stairs('stone_brick', 'west', 'top'))
        b.set(gx0 + 1, gy + 4, gzn + 1, LANTERN)
        b.threshold('posad', gx0, gy + 1, gzn, gx0 + 3, gzn + 1)
        # the hearth, in the middle of the yard, with a ring of stones round it and a log to sit on
        hx, hz = cx, z0 + 5
        b.hearth(hx, gy + 1, hz, 'yard')
        for dx, dz in ((-2, 0), (2, 0), (0, -2), (-2, -1), (2, 1)):
            b.set(hx + dx, gy, hz + dz, 'minecraft:mossy_cobblestone')
        b.set(hx - 2, gy + 1, hz + 2, 'minecraft:spruce_log[axis=x]')
        b.set(hx - 3, gy + 1, hz + 2, 'minecraft:spruce_log[axis=x]')
        # the chest by the way from the gate to the fire
        b.loot(cx + 2, gy + 1, z1 - 5, 'start', 'west')
        b.set(cx + 2, gy + 1, z1 - 4, 'minecraft:barrel[facing=up]')
        b.set(cx + 3, gy + 1, z1 - 5, FLOOR_LANTERN)
        # the keykeeper's lean-to against the east wall
        for x in range(x1 - 5, x1 + 1):
            for z in range(z0 + 3, z0 + 9):
                b.set(x, gy, z, 'minecraft:spruce_planks')
                b.set(x, gy + 4, z, 'minecraft:spruce_slab[type=bottom]' if x > x1 - 5 else stairs('spruce', 'east'))
        for z in (z0 + 3, z0 + 8):
            b.fill(x1 - 5, gy + 1, z, x1 - 5, gy + 3, z, 'minecraft:spruce_fence')
        b.mob('keykeeper', x1 - 2, gy + 1, z0 + 6, 'west')
        b.barrels([(x1, gy + 1, z0 + 3), (x1, gy + 2, z0 + 3), (x1 - 1, gy + 1, z0 + 3), (x1, gy + 1, z0 + 8)])
        b.set(x1 - 3, gy + 3, z0 + 5, LANTERN)
        b.set(x1, gy + 1, z0 + 5, 'minecraft:chest[facing=west]')
        b.set(x1, gy + 1, z0 + 6, 'minecraft:barrel[facing=west]')
        # the anvil under a roof against the west wall
        for x in range(x0, x0 + 5):
            for z in range(z0 + 3, z0 + 8):
                b.set(x, gy, z, 'minecraft:stone_bricks' if hsh(x, 0, z, 253) < 0.7 else 'minecraft:cobblestone')
                b.set(x, gy + 4, z, 'minecraft:spruce_slab[type=bottom]' if x < x0 + 4 else stairs('spruce', 'west'))
        for z in (z0 + 3, z0 + 7):
            b.fill(x0 + 4, gy + 1, z, x0 + 4, gy + 3, z, 'minecraft:spruce_fence')
        b.set(x0 + 2, gy + 1, z0 + 5, f'{MOD}:forge')
        b.set(x0, gy + 1, z0 + 4, 'minecraft:blast_furnace[facing=east,lit=false]')
        b.set(x0, gy + 1, z0 + 6, 'minecraft:cauldron')
        b.set(x0 + 2, gy + 3, z0 + 5, LANTERN)
        b.barrels([(x0, gy + 1, z0 + 7)])
        # lamps, a cart, what travellers leave
        for x, z in ((x0 + 1, z1 - 1), (x1 - 1, z1 - 1), (x0 + 1, z0 + 1), (x1 - 1, z0 + 1)):
            lamp_post(b, x, gy + 1, z)
        b.cart(x0 + 3, gy + 1, z1 - 4, load=((0, 0, 'minecraft:hay_block[axis=x]'), (1, 1, 'minecraft:barrel[facing=up]')))
        dead_tree(b, x1 - 3, gy + 1, z1 - 3, 5, 3)
        # lamps on the walls, bales and barrels along them: somebody held out here for a while
        for z in range(z0 + 1, z1, 4):
            wall_lamp(b, x0, gy, z, (-1, 0))
            wall_lamp(b, x1, gy, z, (1, 0))
        for x in (cx - 8, cx + 7):
            wall_lamp(b, x, gy, z0, (0, -1))
        for x, z, state in ((x0, z1, 'minecraft:hay_block[axis=x]'), (x0 + 1, z1, 'minecraft:hay_block[axis=z]'), (x0, z1 - 1, 'minecraft:hay_block[axis=y]'),
                            (cx - 5, z0, 'minecraft:barrel[facing=up]'), (cx - 6, z0, 'minecraft:barrel[facing=up]'), (cx + 6, z0, 'minecraft:barrel[facing=up]'),
                            (x1 - 6, z1, 'minecraft:barrel[facing=up]'), (x1 - 7, z1, 'minecraft:composter')):
            if b.free(x, gy + 1, z):
                b.set(x, gy + 1, z, state)
        b.set(x0, gy + 2, z1, 'minecraft:hay_block[axis=x]')

    def market(self):
        b, gy = self.b, self.gy
        b.zone = 'market'
        x0, z0 = self.at(13, 22, 0, 0)
        x1, z1 = self.at(28, 27, 2, 2)
        cx, cz = (x0 + x1) // 2, (z0 + z1) // 2
        self.market_centre = (cx, cz)             # (where the clock tower comes up out of it: gen_tower.py, Clock.java)
        for x in range(x0, x1 + 1):
            for z in range(z0, z1 + 1):
                b.set(x, gy, z, cobble(x, gy, z) if hsh(x, 0, z, 260) < 0.8 else 'minecraft:stone_bricks')
        # the well in the middle
        for dx in range(-1, 2):
            for dz in range(-1, 2):
                if dx or dz:
                    b.set(cx + dx, gy + 1, cz + dz, 'minecraft:mossy_stone_bricks' if hsh(dx, 0, dz, 261) < 0.5 else 'minecraft:stone_bricks')
                else:
                    b.set(cx, gy, cz, 'minecraft:water')
                    b.set(cx, gy - 1, cz, 'minecraft:stone_bricks')
                    # a grate over its mouth: whoever climbed in could not climb out (the rim is a block over the water)
                    b.set(cx, gy + 1, cz, 'minecraft:iron_bars')
        for dx, dz in ((-1, -1), (1, 1), (-1, 1), (1, -1)):
            b.fill(cx + dx, gy + 2, cz + dz, cx + dx, gy + 3, cz + dz, 'minecraft:spruce_fence')
        for dx in range(-1, 2):
            for dz in range(-1, 2):
                b.set(cx + dx, gy + 4, cz + dz, 'minecraft:spruce_slab[type=bottom]')
        b.set(cx, gy + 3, cz, CHAIN)
        b.set(cx, gy + 2, cz, LANTERN)
        # stalls: a counter of barrels under a cloth on four posts
        cloths = ('minecraft:red_wool', 'minecraft:brown_wool', 'minecraft:green_wool', 'minecraft:gray_wool')
        for k, (sx, sz) in enumerate(((x0 + 4, z0 + 2), (x0 + 13, z0 + 2), (x1 - 16, z0 + 2), (x1 - 7, z0 + 2), (x0 + 4, z1 - 4), (x1 - 8, z1 - 4), (x0 + 12, z1 - 4))):
            torn = hsh(k, 0, 0, 262) < 0.4
            for dx in (0, 3):
                for dz in (0, 2):
                    b.fill(sx + dx, gy + 1, sz + dz, sx + dx, gy + 2, sz + dz, 'minecraft:spruce_fence')
            for dx in range(4):
                for dz in range(3):
                    if not (torn and hsh(sx + dx, 1, sz + dz, 263) < 0.35):
                        b.set(sx + dx, gy + 3, sz + dz, cloths[k % 4] if (dx + k) % 2 else cloths[(k + 1) % 4])
            b.set(sx + 1, gy + 1, sz + 1, 'minecraft:barrel[facing=up]')
            b.set(sx + 2, gy + 1, sz + 1, 'minecraft:spruce_slab[type=top]' if k % 2 else 'minecraft:barrel[facing=up]')
            if k in (1, 4):
                b.set(sx + 1, gy + 2, sz + 1, FLOOR_LANTERN)
        b.loot(x1 - 6, gy + 1, z1 - 3, 'market', 'north')
        # a gibbet with its cage, for whoever sold short weight
        gx, gz = x0 + 9, cz + 1
        b.fill(gx, gy + 1, gz, gx, gy + 6, gz, 'minecraft:dark_oak_log[axis=y]')
        b.fill(gx + 1, gy + 6, gz, gx + 2, gy + 6, gz, 'minecraft:dark_oak_log[axis=x]')
        b.set(gx + 1, gy + 5, gz, 'minecraft:dark_oak_fence')
        b.set(gx + 2, gy + 5, gz, CHAIN)
        b.fill(gx + 2, gy + 3, gz, gx + 2, gy + 4, gz, BARS)
        b.set(gx + 2, gy + 2, gz, 'minecraft:skeleton_skull[rotation=6]')
        b.cart(x1 - 14, gy + 1, cz + 3, load=((0, 0, 'minecraft:hay_block[axis=x]'), (1, 0, 'minecraft:hay_block[axis=z]'), (2, 1, 'minecraft:barrel[facing=up]')))
        for x, z in ((x0 + 1, z0 + 1), (x1 - 1, z0 + 1), (x0 + 1, z1 - 1), (x1 - 1, z1 - 1), (cx - 8, cz), (cx + 9, cz - 1)):
            lamp_post(b, x, gy + 1, z)
        for kind, x, z, facing in (('watchman', cx + 4, cz + 4, 'south'), ('townsman', cx - 6, cz - 3, 'south'), ('townsman', x0 + 6, z1 - 2, 'east'),
                                   ('townsman', x1 - 4, z0 + 6, 'west'), ('butcher', cx + 12, cz - 4, 'south'), ('hound', gx + 1, gz + 3, 'south'),
                                   ('townsman', cx - 13, cz + 5, 'east')):
            b.mob(kind, x, gy + 1, z, facing)

    def churchyard(self):
        """The Gravedigger's ground: graves in rows along the walls, the middle trodden bare, a wooden chapel fallen in at
        the north-east corner, his cart with the coffins he has not got to yet."""
        b, gy = self.b, self.gy
        b.zone = 'churchyard'
        x0, z0 = self.at(14, 1, 0, 0)
        x1, z1 = self.at(27, 10, 2, 2)
        cx, cz = (x0 + x1) // 2, (z0 + z1) // 2
        for x in range(x0, x1 + 1):
            for z in range(z0, z1 + 1):
                edge = ((x - cx) / 13.0) ** 2 + ((z - cz) / 9.0) ** 2 + (hsh(x // 2, 0, z // 2, 269) - 0.5) * 0.5
                worn = edge < 1.0 or abs(x - cx - 1) <= 1
                b.set(x, gy, z, ('minecraft:coarse_dirt' if hsh(x, 0, z, 270) < 0.86 else 'minecraft:gravel') if worn else grave_earth(x, gy, z))
        # graves along the west, east and south walls: a mound, a stone at its head
        stones = ('minecraft:cobblestone_wall', 'minecraft:mossy_cobblestone_wall', 'minecraft:stone_brick_wall', 'minecraft:andesite_wall')
        k = 0
        for gx in list(range(x0 + 2, x0 + 9, 3)) + list(range(x1 - 8, x1 - 1, 3)):
            for gz in range(z0 + 3, z1 - 2, 4):
                k += 1
                n = hsh(gx, 0, gz, 271)
                if n < 0.12:
                    continue
                b.set(gx, gy + 1, gz, stones[int(n * 40) % 4] if n > 0.25 else 'minecraft:mossy_stone_brick_slab[type=bottom]')
                for dz in (1, 2):
                    b.set(gx, gy, gz + dz, 'minecraft:podzol' if n < 0.7 else 'minecraft:rooted_dirt')
                if n > 0.9:
                    b.set(gx, gy + 1, gz + 1, 'minecraft:skeleton_skull[rotation=%d]' % int(n * 160 % 16))
                elif n > 0.8:
                    b.set(gx + 1, gy + 1, gz, 'minecraft:candle[candles=2,lit=true]')
        # the chapel: a wooden box with half its roof gone, and what the dead gave for their burial
        px0, pz0, px1, pz1 = x1 - 9, z0 + 1, x1 - 1, z0 + 8
        for x in range(px0, px1 + 1):
            for z in range(pz0, pz1 + 1):
                edge = x in (px0, px1) or z in (pz0, pz1)
                b.set(x, gy, z, 'minecraft:spruce_planks' if hsh(x, 0, z, 272) < 0.8 else 'minecraft:coarse_dirt')
                if edge:
                    high = 4 if hsh(x, 1, z, 273) < 0.75 or z == pz0 else 2
                    for y in range(gy + 1, gy + 1 + high):
                        corner = (x in (px0, px1)) and (z in (pz0, pz1))
                        b.set(x, y, z, 'minecraft:dark_oak_log[axis=y]' if corner else ('minecraft:spruce_planks' if hsh(x, y, z, 274) < 0.85 else 'minecraft:cobweb'))
        for x in range(px0 - 1, px1 + 2):                      # the half of the roof that still holds
            for i in range(0, 5):
                z = pz0 - 1 + i
                if hsh(x, 2, z, 275) < 0.85 and b.free(x, gy + 5 + i // 1, z):
                    b.set(x, gy + 5 + i, z, stairs('dark_oak', 'south'))
        mx = (px0 + px1) // 2
        b.fill(mx, gy + 1, pz1, mx, gy + 2, pz1, AIR)          # the door, south
        b.set(mx, gy + 3, pz0 + 1, LANTERN)
        b.fill(mx, gy + 1, pz0 + 1, mx, gy + 1, pz0 + 1, 'minecraft:mossy_stone_bricks')
        b.set(mx, gy + 2, pz0 + 1, 'minecraft:candle[candles=3,lit=true]')
        b.loot(px0 + 1, gy + 1, pz0 + 1, 'chapel', 'south')
        # the cart of coffins, a heap of earth, a grave still open (one deep: he does not take long over them)
        b.cart(x0 + 12, gy + 1, z0 + 3, load=((0, 0, 'minecraft:dark_oak_trapdoor[facing=north,half=bottom,open=false]'), (1, 0, 'minecraft:dark_oak_trapdoor[facing=north,half=bottom,open=false]'),
                                              (1, 1, 'minecraft:dark_oak_trapdoor[facing=north,half=bottom,open=false]')))
        for x, z, h in ((cx - 9, cz + 6, 2), (cx - 8, cz + 6, 1), (cx - 9, cz + 7, 1), (cx + 8, cz + 5, 1)):
            b.fill(x, gy + 1, z, x, gy + h, z, 'minecraft:coarse_dirt')
        for x in range(cx - 7, cx - 5):
            for z in range(cz + 5, cz + 8):
                b.set(x, gy, z, AIR)
                b.set(x, gy - 1, z, 'minecraft:podzol')
        for x, z, h in ((x0 + 6, z0 + 1, 6), (x1 - 12, z1 - 2, 5), (x0 + 11, z1 - 1, 7), (x1 - 2, z1 - 6, 6), (x0 + 1, z1 - 1, 5)):
            dead_tree(b, x, gy + 1, z, h, x)
        for x, z in ((cx - 12, cz - 9), (cx + 6, cz + 11), (cx - 12, cz + 11), (cx - 3, cz - 12), (cx + 14, cz + 2), (cx - 16, cz)):
            if b.free(x, gy + 1, z):
                b.set(x, gy + 1, z, 'minecraft:cobblestone_wall')
                b.set(x, gy + 2, z, 'minecraft:soul_lantern[hanging=false]')
        b.mob('gravedigger', cx, gy + 1, cz - 3, 'south')
        # the lych-gate in the south wall, roofed, with the fog in it; the gate to the road in the north wall, sealed
        for name, (c, r), sealed in (('gravedigger_fog', (20, 11), False), ('gravedigger', (20, 0), True)):
            gx, gz = self.at(c, r, 1, 0)
            for x in range(gx - 1, gx + 5):
                for z in range(gz, gz + T):
                    b.set(x, gy, z, cobble(x, gy, z))
                    if x in (gx - 1, gx + 4):
                        b.fill(x, gy + 1, z, x, gy + 5, z, town_wall)
                    else:
                        b.fill(x, gy + 1, z, x, gy + 4, z, AIR)
                        b.set(x, gy + 5, z, 'minecraft:stripped_dark_oak_log[axis=x]')
            for z, facing in ((gz - 1, 'south'), (gz + T, 'north')):
                for x in range(gx - 2, gx + 6):
                    if b.free(x, gy + 5, z):
                        b.set(x, gy + 5, z, stairs('dark_oak', facing))
            for x in range(gx - 2, gx + 6):
                for z in range(gz, gz + T):
                    b.set(x, gy + 6, z, 'minecraft:dark_oak_slab[type=bottom]' if z != gz + 1 else 'minecraft:dark_oak_planks')
            b.fog(name, gx, gy + 1, gz + 1, gx + 3, gy + 4, gz + 1, 'x', sealed=sealed)
        gx, gz = self.at(20, 11, 1, 0)
        b.threshold('churchyard', gx, gy + 1, gz - 2, gx + 3, gz - 1)
        b.threshold('shambles', gx, gy + 1, gz + T + 1, gx + 3, gz + T + 2)
        gx, gz = self.at(20, 0, 1, 0)
        b.threshold('churchyard', gx, gy + 1, gz + T + 1, gx + 3, gz + T + 2)
        self.north_gate = (gx, gz)

    def shop(self):
        """The butchers' shop: the front room on the street, the cutting room behind, the back door onto Church Lane."""
        b, gy = self.b, self.gy
        b.zone = 'shambles'
        c0, r0, c1, r1 = SPECIAL['b']
        x0, z0, x1, z1, top = self.house(c0, r0, c1, r1, 901, hollow=True, doors=(('e', 13), ('n', 4)), storeys=2, style=1, door_height=3)   # the butchers are big men
        mid = z0 + 8
        for x in range(x0 + 1, x1):                              # the wall between the rooms, with a way through
            if x not in (x0 + 5, x0 + 6):
                b.fill(x, gy + 1, mid, x, gy + 4, mid, 'minecraft:mud_bricks')
        # hooks from the beams, blocks, tubs
        for x, z in ((x0 + 3, z0 + 3), (x0 + 7, z0 + 4), (x0 + 9, z0 + 2), (x0 + 3, mid + 3), (x0 + 8, mid + 2), (x0 + 2, mid + 1), (x0 + 9, z0 + 6)):
            b.set(x, gy + 4, z, CHAIN)
            b.set(x, gy + 3, z, 'minecraft:hanging_roots' if hsh(x, 0, z, 280) < 0.6 else CHAIN)
        for x, z in ((x0 + 2, z0 + 6), (x0 + 6, z0 + 2), (x0 + 9, mid + 4)):                    # the blocks, and what is on them
            b.set(x, gy + 1, z, 'minecraft:stripped_dark_oak_log[axis=y]')
            b.set(x, gy + 2, z, 'minecraft:red_carpet')
        for x, z in ((x0 + 4, z0 + 6), (x0 + 7, z0 + 2), (x0 + 8, mid + 5), (x0 + 5, mid + 2)):  # and what ran onto the floor
            b.set(x, gy, z, 'minecraft:red_terracotta' if hsh(x, 1, z, 281) < 0.6 else 'minecraft:mangrove_planks')
        b.set(x0 + 1, gy + 1, z0 + 1, 'minecraft:cauldron')
        b.set(x0 + 1, gy + 1, mid + 5, 'minecraft:barrel[facing=up]')
        b.set(x0 + 5, gy + 4, z0 + 4, LANTERN)
        b.set(x0 + 5, gy + 4, mid + 3, LANTERN)
        b.loot(x0 + 1, gy + 1, z0 + 5, 'butchers', 'east')
        b.mob('butcher', x0 + 6, gy + 1, z0 + 5, 'south')
        b.mob('butcher', x0 + 4, gy + 1, mid + 3, 'east')
        b.mob('hound', x0 + 8, gy + 1, mid + 4, 'east')

    def lodge(self):
        b, gy = self.b, self.gy
        c0, r0, c1, r1 = SPECIAL['l']
        x0, z0, x1, z1, top = self.house(c0, r0, c1, r1, 902, hollow=True, doors=(('n', 4),), storeys=1, style=2)
        b.hearth(x0 + 4, gy + 1, z0 + 3, 'lodge')
        b.set(x0 + 1, gy + 1, z1 - 1, 'minecraft:barrel[facing=up]')
        b.set(x1 - 1, gy + 1, z1 - 1, 'minecraft:hay_block[axis=x]')
        b.set(x1 - 2, gy + 1, z1 - 1, 'minecraft:hay_block[axis=x]')
        b.set(x0 + 2, gy + 4, z0 + 2, LANTERN)
        b.set(x0 + 4, gy + 3, z0 - 1, 'minecraft:spruce_fence')
        b.set(x0 + 4, gy + 2, z0 - 1, AIR)

    def homes(self):
        b, gy = self.b, self.gy
        b.zone = 'borough'
        c0, r0, c1, r1 = SPECIAL['p']
        x0, z0, x1, z1, top = self.house(c0, r0, c1, r1, 903, hollow=True, doors=(('w', 4),), storeys=1, style=0)
        b.loot(x1 - 1, gy + 1, z0 + 1, 'borough_house', 'west')
        b.set(x1 - 1, gy + 1, z1 - 1, 'minecraft:red_bed[facing=south,part=foot]')
        b.set(x1 - 1, gy + 1, z1 - 2, 'minecraft:barrel[facing=up]')
        b.set(x0 + 4, gy + 4, z0 + 4, LANTERN)
        b.mob('townsman', x0 + 5, gy + 1, z0 + 5, 'west')
        b.mob('townsman', x0 + 2, gy + 1, z1 - 2, 'north')
        c0, r0, c1, r1 = SPECIAL['q']
        x0, z0, x1, z1, top = self.house(c0, r0, c1, r1, 904, hollow=True, doors=(('w', 2), ('e', 3)), storeys=2, style=5)
        b.set(x0 + 4, gy + 4, z0 + 3, LANTERN)
        b.set(x0 + 1, gy + 1, z1 - 1, 'minecraft:crafting_table')
        b.set(x1 - 1, gy + 1, z0 + 1, 'minecraft:barrel[facing=up]')
        b.mob('watchman', x0 + 5, gy + 1, z0 + 2, 'west')
        # the yard behind it, where something glows in the ash of an old fire
        yx, yz = self.at(36, 39)
        b.shard(yx + 1, gy + 1, yz + 2)
        b.set(yx + 1, gy, yz + 2, 'minecraft:coarse_dirt')
        dead_tree(b, yx + 3, gy + 1, yz, 5, 7)
        b.mob('hound', yx - 1, gy + 1, yz + 3, 'west')

    def high_gate(self):
        """The gate across the high street: shut, and opened from the town's side, after which the yard of the first
        hearth is two minutes from the market."""
        b, gy = self.b, self.gy
        x0, z = self.at(20, 40, 0, 0)
        for x in range(x0, x0 + 2 * T):
            b.fill(x, gy + 1, z, x, gy + 5, z, town_wall)
            b.set(x, gy + 6, z, 'minecraft:stone_brick_slab[type=bottom]')
        dx = x0 + 2
        b.door(dx, gy + 1, z, 'north', (dx + 1, gy + 2, z - 1), 'north')
        b.set(dx - 1, gy + 3, z - 1, 'minecraft:spruce_fence')
        b.set(dx - 1, gy + 2, z - 1, LANTERN)
        b.set(dx + 2, gy + 3, z + 1, 'minecraft:spruce_fence')
        b.set(dx + 2, gy + 2, z + 1, LANTERN)

    def quarry_gate(self):
        """The Quarrymen's Gate at the west end of Low Street: a low arch through the wall, shut by an iron grate whose
        key is not in the town (see LockedGateBlock; the key lies in the treasury of the Stockade). The stone of the
        fortress came in by it; two blocks of that stone still lie on a broken sledge before it."""
        b, gy = self.b, self.gy
        x0, z0 = self.at(*QUARRY_GATE, 0, 0)
        for x in range(x0, x0 + T):
            for z in range(z0, z0 + T):
                b.set(x, gy, z, cobble(x, gy, z))
                b.fill(x, gy + 1, z, x, gy + 4, z, 'minecraft:air')
                b.fill(x, gy + 5, z, x, gy + 7, z, town_wall)
                if (x + z) % 2 == 0 and x != x0 + 1:
                    b.set(x, gy + 8, z, town_wall(x, gy + 8, z))
        for x in range(x0, x0 + T):                              # the corners of the arch
            b.set(x, gy + 4, z0, 'minecraft:stone_brick_stairs[facing=north,half=top]')
            b.set(x, gy + 4, z0 + 2, 'minecraft:stone_brick_stairs[facing=south,half=top]')
        cells = [(x0 + 1, y, z) for z in range(z0, z0 + T) for y in range(gy + 1, gy + 5)]
        for cell in cells:
            b.set(*cell, 'unfading_dawn:locked_gate[axis=z,lock=quarry]')
        b.set(x0 + 1, gy + 4, z0, 'minecraft:stone_brick_stairs[facing=north,half=top]')
        b.set(x0 + 1, gy + 4, z0 + 2, 'minecraft:stone_brick_stairs[facing=south,half=top]')
        cells = [c for c in cells if 'locked_gate' in b.get(*c)]
        b.seals['quarry_key'] = cells
        self.quarry_gate_cells = cells
        # a lamp either side of it on the town's face of the wall, and what was left where the hauling stopped
        for z in (z0 - 1, z0 + T):
            b.set(x0 + T, gy + 4, z, 'minecraft:spruce_fence')
            b.set(x0 + T, gy + 3, z, LANTERN)
        sx, sz = x0 + T + 3, z0 + T
        if all(b.free(sx + dx, gy + 1, sz) for dx in range(3)):
            b.set(sx, gy + 1, sz, 'minecraft:spruce_slab[type=bottom]')
            b.set(sx + 1, gy + 1, sz, 'minecraft:calcite')
            b.set(sx + 2, gy + 1, sz, 'minecraft:polished_diorite')
            self.kept.add((sx, sz))

    def mire_gate(self):
        """The Mire Gate at the east end of Low Street: the same low arch and the same kind of grate as the Quarrymen's,
        with its own key (which the Stone-Eater keeps). The street beyond it went under the bog long ago; the wall
        here is green to twice a man's height, and the water has been through the arch more than once."""
        b, gy = self.b, self.gy
        x0, z0 = self.at(*MIRE_GATE, 0, 0)
        for x in range(x0, x0 + T):
            for z in range(z0, z0 + T):
                b.set(x, gy, z, 'minecraft:mossy_cobblestone' if hsh(x, 0, z, 291) < 0.7 else 'minecraft:mud')
                b.fill(x, gy + 1, z, x, gy + 4, z, 'minecraft:air')
                b.fill(x, gy + 5, z, x, gy + 7, z, town_wall)
                if (x + z) % 2 == 0 and x != x0 + 1:
                    b.set(x, gy + 8, z, town_wall(x, gy + 8, z))
        for x in range(x0, x0 + T):
            b.set(x, gy + 4, z0, 'minecraft:stone_brick_stairs[facing=north,half=top]')
            b.set(x, gy + 4, z0 + 2, 'minecraft:stone_brick_stairs[facing=south,half=top]')
        cells = [(x0 + 1, y, z) for z in range(z0, z0 + T) for y in range(gy + 1, gy + 5)]
        for cell in cells:
            b.set(*cell, 'unfading_dawn:locked_gate[axis=z,lock=mire]')
        b.set(x0 + 1, gy + 4, z0, 'minecraft:stone_brick_stairs[facing=north,half=top]')
        b.set(x0 + 1, gy + 4, z0 + 2, 'minecraft:stone_brick_stairs[facing=south,half=top]')
        cells = [c for c in cells if 'locked_gate' in b.get(*c)]
        b.seals['mire_key'] = cells
        # the last lamp of the town, on the town's face of the wall; moss on the stones before the arch
        for z in (z0 - 1, z0 + T):
            b.set(x0 - 1, gy + 4, z, 'minecraft:spruce_fence')
            b.set(x0 - 1, gy + 3, z, LANTERN)
        for x in range(x0 - 4, x0):
            for z in range(z0, z0 + T):
                if hsh(x, 1, z, 292) < 0.2 + 0.15 * (x - x0 + 4):
                    b.set(x, gy, z, 'minecraft:mossy_cobblestone' if hsh(x, 2, z, 293) < 0.75 else 'minecraft:mud')
                self.kept.add((x, z))

    def people(self):
        """Who walks the lanes. (tile column, tile row, kind, facing)"""
        b, gy = self.b, self.gy
        folk = {
            'borough': (
                (12, 42, 'townsman', 'east'), (9, 40, 'townsman', 'south'), (5, 39, 'hound', 'east'), (11, 36, 'townsman', 'west'),
                (14, 33, 'townsman', 'south'), (17, 34, 'hound', 'west'), (9, 31, 'watchman', 'east'), (5, 30, 'townsman', 'south'),
                (16, 40, 'townsman', 'north'),
                (28, 42, 'townsman', 'west'), (31, 39, 'hound', 'south'), (28, 38, 'townsman', 'east'), (26, 35, 'townsman', 'south'),
                (31, 33, 'watchman', 'west'), (35, 31, 'townsman', 'south'), (27, 30, 'hound', 'west'),
                (29, 30, 'hound', 'west'), (20, 37, 'townsman', 'south'), (21, 32, 'townsman', 'south'), (20, 31, 'butcher', 'south'),
            ),
            'market': (
                (7, 24, 'townsman', 'south'), (4, 25, 'hound', 'east'), (34, 25, 'townsman', 'north'), (38, 23, 'townsman', 'west'),
                (10, 21, 'townsman', 'east'),
            ),
            'shambles': (
                (20, 19, 'butcher', 'south'), (21, 17, 'townsman', 'south'), (15, 18, 'hound', 'east'), (12, 16, 'butcher', 'south'),
                (25, 18, 'townsman', 'west'), (29, 17, 'hound', 'south'), (31, 16, 'butcher', 'west'),
                (12, 13, 'watchman', 'south'), (27, 12, 'townsman', 'west'), (21, 13, 'butcher', 'south'),
            ),
        }
        for zone, who in folk.items():
            b.zone = zone
            for c, r, kind, facing in who:
                x, z = self.at(c, r)
                if not b.free(x, gy + 1, z):
                    raise ValueError(f'no room for a {kind} at tile {(c, r)}: {b.get(x, gy + 1, z)}')
                b.mob(kind, x, gy + 1, z, facing)

    def finds(self):
        b, gy = self.b, self.gy
        for (c, r, dx, dz), table, facing in (((3, 39, 0, 1), 'borough_nook', 'east'), ((30, 30, 2, 1), 'borough_yard', 'west'), ((16, 39, 1, 0), 'borough_alley', 'south'),
                                              ((3, 25, 0, 1), 'tanners', 'east'), ((31, 15, 1, 1), 'shambles_yard', 'west')):
            x, z = self.at(c, r, dx, dz)
            if not b.free(x, gy + 1, z):
                b.set(x, gy + 1, z, AIR)
            b.loot(x, gy + 1, z, table, facing)

    def names(self):
        """The thresholds between the parts of the town (each named on its own side of the line)."""
        b, gy = self.b, self.gy
        def strip(place, c0, r0, c1, r1, dz0=0, dz1=2, dx0=0, dx1=2):
            xa, za = self.at(c0, r0, dx0, dz0)
            xb, zb = self.at(c1, r1, dx1, dz1)
            b.threshold(place, xa, gy + 1, za, xb, zb)
        strip('market', 13, 28, 28, 28, 0, 1)                    # along the south side of the market
        strip('market', 5, 28, 5, 28)
        strip('market', 35, 28, 35, 28)
        strip('posad', 20, 30, 21, 30, 0, 1)
        strip('posad', 5, 30, 5, 30, 0, 1)
        strip('posad', 35, 30, 35, 30, 0, 1)
        strip('market', 3, 21, 38, 21, 1, 2)                     # along Tanners' Row
        strip('shambles', 20, 19, 21, 19, 0, 1)
        strip('shambles', 12, 19, 12, 19, 0, 1)
        strip('shambles', 29, 19, 29, 19, 0, 1)

    def build(self):
        b = self.b
        self.ground()
        self.walls()
        b.zone = 'borough'
        for i, (c0, r0, c1, r1) in enumerate(self.houses):
            if c1 == c0 or r1 == r0:
                self.garden(c0, r0, c1, r1, i * 7 + 3)
            else:
                self.house(c0, r0, c1, r1, i * 7 + 3)
        self.hub()
        self.market()
        self.churchyard()
        self.shop()
        self.lodge()
        self.homes()
        self.high_gate()
        self.quarry_gate()
        self.mire_gate()
        self.barricades()
        self.street_dressing()
        self.people()
        self.finds()
        self.names()


# ---------------------------------------------------------------------------------------------------- small things

def lamp_post(b, x, y, z):
    """A lamp on a post: the post two high, an arm, the lamp on top."""
    if not all(b.free(x, y + h, z) for h in range(4)):
        return
    b.set(x, y, z, 'minecraft:cobblestone_wall')
    b.fill(x, y + 1, z, x, y + 2, z, 'minecraft:spruce_fence')
    b.set(x, y + 3, z, FLOOR_LANTERN)


def wall_lamp(b, x, y, z, inward):
    """A lamp hanging from an arm out of the wall behind this cell (`y` is the ground block)."""
    if b.free(x, y + 4, z) and b.free(x, y + 3, z) and b.get(x - inward[0], y + 4, z - inward[1]) not in (None, AIR):
        b.set(x, y + 4, z, 'minecraft:spruce_fence')
        b.set(x, y + 3, z, LANTERN)


def dead_tree(b, x, y, z, height, salt=0):
    """A tree with no leaf left on it: a trunk and a few crooked boughs."""
    if not all(b.free(x, y + h, z) for h in range(height)):
        return
    b.fill(x, y, z, x, y + height - 1, z, 'minecraft:dark_oak_log[axis=y]')
    for k, (dx, dz) in enumerate(((1, 0), (-1, 0), (0, 1), (0, -1))):
        n = hsh(x + dx, k, z + dz, 290 + salt)
        if n < 0.7:
            h = y + height - 1 - int(n * 6) % 3
            axis = 'x' if dx else 'z'
            if b.free(x + dx, h, z + dz):
                b.set(x + dx, h, z + dz, f'minecraft:dark_oak_log[axis={axis}]')
                if n < 0.35 and b.free(x + 2 * dx, h + 1, z + 2 * dz):
                    b.set(x + 2 * dx, h + 1, z + 2 * dz, 'minecraft:dark_oak_fence')
    if b.free(x, y + height, z):
        b.set(x, y + height, z, 'minecraft:dark_oak_fence')
