#!/usr/bin/env python3
"""The world of the map, put together: a wooded plateau with a gorge cut into it; in the south of the gorge the Dead
Borough (tools/gen_village.py), where the game begins at night; in the north the High Stockade on its two crags
(tools/gen_map.py), where the sun never quite sets; between them the road, which passes under a spur of the plateau by
a tunnel with a bend in it - in the bend, where no sky is seen, the hour changes.

Writes map/high_stockade.plan.txt (set down by MapBuilder.java in a flat world of forest whose ground is the plan's
layer GY), map/views.txt and map/light.txt, and, given a directory, preview pictures. Ends with the check of the map as
a place to play (see gen_map.analyse). Every further place of the map gets its own file and is joined on here.

Run from the repo root: PYTHONPATH=tools python3 tools/gen_world.py [preview dir]"""
import math
import os
import sys
from collections import deque

import gen_map as castle
import gen_village
from gen_map import (BARS, BELL, CY, FLOOR_LANTERN, GAL, L0, L1, L2, LANTERN, LC, PASSABLE, VAULT, Map, analyse, cobbles, dark_places, hsh, stairs, valid_blocks)
from gen_structures import AIR, COLOURS, MOD

OUT = 'map/high_stockade.plan.txt'
GY = 8                              # the ground block of the valley; the flat world's own top layer is put at this height
OX, OZ = 42, 42                     # where the fortress's own plan lies in this one
DY = GY - castle.GROUND             # and how much lower: its ground is the valley's
W, H, D = 212, 128, 586
AX = OX + castle.CX                 # the axis of the fortress: its bridge, the causeway, the road
TX = AX + 16                        # the tunnel comes out this much further east: the bend
VX, VZ = TX - 62, 408               # the corner of the village's grid of tiles; its north gate is on the tunnel's line
SPUR = (354, 402)                   # the spur of the plateau between the two halves of the gorge
WORLD_GROUND = -47                  # the y of the flat world's top layer (see the workflow; low, so that no snow lies on its forest)


def C(x, y, z):
    """A point of the fortress's plan in this one."""
    return x + OX, y + DY, z + OZ


def smooth(x, z, scale, salt):
    """Noise that changes slowly: 0..1."""
    fx, fz = x / scale, z / scale
    x0, z0 = math.floor(fx), math.floor(fz)
    tx, tz = fx - x0, fz - z0
    tx, tz = tx * tx * (3 - 2 * tx), tz * tz * (3 - 2 * tz)
    a, b_ = hsh(x0, 0, z0, salt), hsh(x0 + 1, 0, z0, salt)
    c, d = hsh(x0, 0, z0 + 1, salt), hsh(x0 + 1, 0, z0 + 1, salt)
    return (a * (1 - tx) + b_ * tx) * (1 - tz) + (c * (1 - tx) + d * tx) * tz


# ---------------------------------------------------------------------------------------------------- the land

def turf(x, y, z):
    n = hsh(x, 0, z, 300)
    patch = smooth(x, z, 9, 301)
    if patch > 0.72:
        return 'minecraft:podzol' if n < 0.7 else 'minecraft:coarse_dirt'
    if patch < 0.2:
        return 'minecraft:coarse_dirt' if n < 0.5 else ('minecraft:gravel' if n < 0.7 else 'minecraft:grass_block')
    return 'minecraft:grass_block' if n < 0.9 else ('minecraft:moss_block' if n < 0.95 else 'minecraft:podzol')


def cliff(x, y, z):
    n = hsh(x, y, z, 302)
    if smooth(x + y, z - y, 7, 303) > 0.7:
        return 'minecraft:tuff' if n < 0.6 else 'minecraft:andesite'
    return 'minecraft:stone' if n < 0.5 else ('minecraft:andesite' if n < 0.72 else ('minecraft:cobblestone' if n < 0.84 else ('minecraft:mossy_cobblestone' if n < 0.93 else 'minecraft:tuff')))


def road(x, y, z):
    n = hsh(x, 0, z, 304)
    return 'minecraft:cobblestone' if n < 0.42 else ('minecraft:gravel' if n < 0.66 else ('minecraft:mossy_cobblestone' if n < 0.84 else ('minecraft:coarse_dirt' if n < 0.94 else 'minecraft:dirt_path')))


def paving(x, y, z):
    """The floor of the tunnel: the same road, dry and whole."""
    n = hsh(x, 0, z, 306)
    return 'minecraft:cobblestone' if n < 0.5 else ('minecraft:stone_bricks' if n < 0.72 else ('minecraft:mossy_cobblestone' if n < 0.86 else 'minecraft:gravel'))


def spruce(b, x, y, z, height, salt=0, foot=1):
    """A fir: a bare foot (`foot` blocks of it over the first), then rings of needles narrowing to the top. Stands on
    the block at y - 1. In the gorge the foot is high: needles are a stair to whoever can reach them."""
    leaf = 'minecraft:spruce_leaves[persistent=true,distance=1]'
    for h in range(height):
        if not b.free(x, y + h, z):
            return
    b.fill(x, y, z, x, y + height - 1, z, 'minecraft:spruce_log[axis=y]')
    top = y + height
    b.set(x, top, z, leaf)
    b.set(x, top + 1, z, leaf)
    radius = 1
    for h in range(top - 1, y + foot, -1):
        wide = radius if (top - h) % 2 else max(1, radius - 1)
        for dx in range(-wide, wide + 1):
            for dz in range(-wide, wide + 1):
                if (dx or dz) and abs(dx) + abs(dz) <= wide + (1 if wide > 1 else 0) and hsh(x + dx, h, z + dz, 305 + salt) < 0.93 \
                        and b.inside(x + dx, h, z + dz) and b.free(x + dx, h, z + dz):
                    b.set(x + dx, h, z + dz, leaf)
        if (top - h) % 2 == 0 and radius < 3:
            radius += 1


class World:
    def __init__(self):
        self.b = Map('high_stockade', W, H, D)
        self.village = gen_village.Village(self.b, VX, VZ, GY)
        self.keep = set()                # columns of the valley where nothing may grow: roads, the feet of walls

    # ------------------------------------------------------------------ the shape of the gorge

    def in_gorge(self, x, z):
        """The floor of the gorge: round the fortress, the yard before the tunnel, and the town."""
        if self.village.inside(x, z):
            return True
        wob = (smooth(x, z, 13, 310) - 0.5) * 8
        if OX - 15 + wob <= x <= OX + 128 + 15 - wob and OZ - 15 + wob <= z <= SPUR[0] - wob * 0.5:
            return True
        if TX - 6 <= x <= TX + 7 and SPUR[1] - 2 <= z <= VZ + 2:      # the yard between the tunnel and the town's north gate
            return True
        return False

    def land(self):
        """The floor of the gorge, and the plateau round it: a sheer face at the edge, then rising a little, and falling
        to the level of the country outside at the edge of the plan."""
        b = self.b
        inside = set()
        for x in range(W):
            for z in range(D):
                if self.in_gorge(x, z):
                    inside.add((x, z))
        dist = {c: 0 for c in inside}
        queue = deque(inside)
        while queue:
            x, z = queue.popleft()
            for dx in (-1, 0, 1):
                for dz in (-1, 0, 1):
                    n = (x + dx, z + dz)
                    if (dx or dz) and 0 <= n[0] < W and 0 <= n[1] < D and n not in dist:
                        dist[n] = dist[(x, z)] + 1
                        queue.append(n)
        self.height = {}
        for (x, z), d in dist.items():
            if d == 0:
                self.height[(x, z)] = 0
                continue
            h = 5 + min(d, 5) + smooth(x, z, 11, 311) * 3.5 + (1.5 if d > 8 else 0)
            border = min(x, W - 1 - x, z, D - 1 - z)
            if border < 13:
                h = min(h, border * 0.85)
            if d <= 2:
                h = max(h, 6)
            self.height[(x, z)] = int(h)
        for (x, z), h in self.height.items():
            if h == 0:
                if (x, z) in inside and not self.village.inside(x, z):
                    b.set(x, GY, z, turf(x, GY, z))
                continue
            for y in range(GY + 1, GY + h + 1):
                exposed = y > GY + h - 2 and all(self.height.get((x + dx, z + dz), 0) >= y - GY - 1 for dx, dz in ((1, 0), (-1, 0), (0, 1), (0, -1)))
                b.set(x, y, z, cliff(x, y, z) if not exposed else ('minecraft:dirt' if y < GY + h else turf(x, y, z)))
        self.inside = inside
        self.dist = dist

    def woods(self):
        """Firs on the plateau, thick; a few in the gorge round the fortress, with boulders and brush. Nothing in the
        gorge may be a way up: its trees stand clear of every wall and face of rock, bare to twice a man's height, and
        no boulder lies near one."""
        b = self.b
        # how far every place of the gorge is from rock, masonry and the road
        far = {}
        queue = deque()
        for (x, z) in self.inside:
            if self.village.inside(x, z):
                continue
            hard = (x, z) in self.keep or any(self.height.get((x + dx, z + dz), 1) > 0 for dx, dz in ((1, 0), (-1, 0), (0, 1), (0, -1)))
            if hard:
                far[(x, z)] = 0
                queue.append((x, z))
        while queue:
            x, z = queue.popleft()
            for dx in (-1, 0, 1):
                for dz in (-1, 0, 1):
                    n = (x + dx, z + dz)
                    if n in self.inside and n not in far and not self.village.inside(*n):
                        far[n] = far[(x, z)] + 1
                        queue.append(n)
        trees = []
        for (x, z), h in sorted(self.height.items()):
            n = hsh(x, 7, z, 320)
            if h >= 4 and self.dist[(x, z)] >= 2:
                if n < 0.035 and min(x, W - 1 - x, z, D - 1 - z) > 3:
                    b.set(x, GY + h, z, 'minecraft:podzol')
                    spruce(b, x, GY + h + 1, z, 5 + int(hsh(x, 8, z, 321) * 6), x * 7 + z)
                elif n < 0.09 and b.free(x, GY + h + 1, z):
                    b.set(x, GY + h + 1, z, 'minecraft:fern' if n < 0.07 else 'minecraft:short_grass')
            elif h == 0 and far.get((x, z), 0) > 0 and b.free(x, GY + 1, z):
                ground = b.get(x, GY, z)
                if ground in (None, AIR) or 'grass_block' not in ground and 'podzol' not in ground:
                    continue
                d = far[(x, z)]
                if n < 0.02 and d >= 7 and all(abs(x - tx) + abs(z - tz) > 6 for tx, tz in trees):
                    b.set(x, GY, z, 'minecraft:podzol')
                    spruce(b, x, GY + 1, z, 8 + int(hsh(x, 8, z, 321) * 5), x * 7 + z, foot=4)
                    trees.append((x, z))
                elif 0.02 <= n < 0.026 and 3 <= d <= 5:
                    for dx, dz, hh in ((0, 0, 2), (1, 0, 1), (0, 1, 1), (-1, 0, 1)):      # a boulder
                        if hsh(x + dx, 9, z + dz, 322) < 0.8 and far.get((x + dx, z + dz), 0) > 1:
                            for y in range(GY + 1, GY + 1 + hh):
                                if b.free(x + dx, y, z + dz):
                                    b.set(x + dx, y, z + dz, 'minecraft:mossy_cobblestone' if hsh(x + dx, y, z + dz, 323) < 0.5 else 'minecraft:andesite')
                elif 0.03 <= n < 0.14:
                    b.set(x, GY + 1, z, 'minecraft:short_grass' if n < 0.1 else ('minecraft:fern' if n < 0.13 else 'minecraft:dead_bush'))

    # ------------------------------------------------------------------ the road

    def causeway(self):
        """From the end of the fortress's bridge down to the floor of the gorge: a ramp of masonry, a step every third block."""
        b = self.b
        z_top = OZ + 272
        deck = L0 + DY                                            # the level of the bridge
        for i in range(deck - GY):
            for dz in range(3):
                z = z_top + i * 3 + dz
                y = deck - i - (1 if dz else 0)
                for x in range(AX - 4, AX + 5):
                    for yy in range(GY + 1, y + 1):
                        b.set(x, yy, z, castle.grey(x - OX, yy - DY, z - OZ))
                    edge = x in (AX - 4, AX + 4)
                    if dz == 0:
                        b.set(x, y, z, castle.grey(x - OX, y - DY, z - OZ) if edge else stairs('stone_brick', 'north'))
                    elif y > GY:
                        b.set(x, y, z, castle.grey(x - OX, y - DY, z - OZ) if edge else cobbles(x, y, z))
                    if edge and y + 1 > GY + 1:
                        b.set(x, y + 1, z, castle.grey(x - OX, y + 1 - DY, z - OZ))
                    self.keep.add((x, z))
        end = z_top + (deck - GY) * 3
        for x in (AX - 4, AX + 4):
            b.fill(x, GY + 1, end, x, GY + 2, end, 'minecraft:stone_brick_wall')
            b.set(x, GY + 3, end, FLOOR_LANTERN)
        return end

    def highway(self, z0):
        """The road along the floor of the gorge, from the foot of the causeway to the mouth of the tunnel."""
        b = self.b
        for z in range(z0, SPUR[0] + 6):
            for x in range(AX - 3, AX + 4):
                if abs(x - AX) <= 2 or hsh(x, 0, z, 330) < 0.4:
                    if self.height.get((x, z), 0) == 0:
                        b.set(x, GY, z, road(x, GY, z))
                self.keep.add((x, z))
            if z % 9 == 4 and z < SPUR[0] - 2:
                side = AX - 4 if (z // 9) % 2 else AX + 4
                gen_village.lamp_post(b, side, GY + 1, z)
                self.keep.add((side, z))
        for x in range(AX - 10, AX + 11):                         # nothing grows right under the walls of the causeway
            for z in range(OZ + 250, z0 + 3):
                self.keep.add((x, z))

    def tunnel(self):
        """Under the spur: in from the north on the fortress's line, a turn east, a turn south, out before the town's
        gate. In the cross passage, out of sight of both mouths, the hour is changed."""
        b = self.b
        zc = (SPUR[0] + SPUR[1]) // 2 - 6                         # the cross passage
        path = [(AX, z) for z in range(SPUR[0] - 6, zc + 1)] + [(x, zc) for x in range(AX, TX + 1)] + [(TX, z) for z in range(zc, SPUR[1] + 3)]
        cells = set()
        for (px, pz) in path:
            for dx in range(-2, 3):
                for dz in range(-2, 3):
                    cells.add((px + dx, pz + dz))
        shell = set()
        for (x, z) in cells:
            for dx in range(-1, 2):
                for dz in range(-1, 2):
                    if (x + dx, z + dz) not in cells:
                        shell.add((x + dx, z + dz))

        def lining(x, y, z):
            n = hsh(x, y, z, 340)
            return 'minecraft:stone_bricks' if n < 0.45 else ('minecraft:cracked_stone_bricks' if n < 0.6 else ('minecraft:mossy_stone_bricks' if n < 0.78 else 'minecraft:cobblestone'))
        for (x, z) in cells:
            under = self.height.get((x, z), 0) > 0
            b.set(x, GY, z, paving(x, GY, z) if under else road(x, GY, z))
            self.keep.add((x, z))
            if not under:
                continue
            # how far from the middle of the way: the vault is highest there
            off = min(max(abs(x - px), abs(z - pz)) for (px, pz) in path if abs(x - px) <= 2 and abs(z - pz) <= 2)
            top = GY + (5 if off <= 1 else 4)
            for y in range(GY + 1, top + 1):
                b.set(x, y, z, AIR)
            b.set(x, top + 1, z, lining(x, top + 1, z))
            for y in range(top + 2, GY + max(self.height.get((x, z), 0), 8) + 1):
                if b.get(x, y, z) is None:
                    b.set(x, y, z, cliff(x, y, z))
        for (x, z) in shell:
            if self.height.get((x, z), 0) > 0:
                for y in range(GY, GY + 7):
                    b.set(x, y, z, lining(x, y, z))
                for y in range(GY + 7, GY + max(self.height.get((x, z), 0), 8) + 1):
                    if b.get(x, y, z) is None:
                        b.set(x, y, z, cliff(x, y, z))
        # lamps along it, on alternate sides, and props of timber
        for i, (px, pz) in enumerate(path):
            if i % 7 == 3 and self.height.get((px, pz), 0) > 0:
                north_south = i + 1 < len(path) and path[i + 1][0] == px
                sx, sz = (px + (2 if (i // 7) % 2 else -2), pz) if north_south else (px, pz + (2 if (i // 7) % 2 else -2))
                if b.free(sx, GY + 3, sz):
                    b.set(sx, GY + 4, sz, 'minecraft:spruce_fence')
                    b.set(sx, GY + 3, sz, LANTERN)
        # the mouths: an arch of dressed stone, a lamp either side
        for mx, mz, step in ((AX, SPUR[0], -1), (TX, SPUR[1], 1)):
            z = mz
            while self.height.get((mx, z), 0) > 0 and abs(z - mz) < 12:
                z += step
            for dx in (-3, 3):
                gen_village.lamp_post(b, mx + dx, GY + 1, z + step)
                self.keep.add((mx + dx, z + step))
        # the hour: towards the fortress evening, towards the town night
        b.threshold('to_dusk', AX + 4, GY + 1, zc - 2, AX + 5, zc + 2)
        b.threshold('to_night', TX - 5, GY + 1, zc - 2, TX - 4, zc + 2)
        self.cross = zc
        # the yard between the tunnel and the town: the road, and the name of it
        for z in range(SPUR[1] - 2, VZ + 1):
            for x in range(TX - 6, TX + 8):
                if self.height.get((x, z), 0) == 0 and not self.village.inside(x, z):
                    b.set(x, GY, z, road(x, GY, z) if abs(x - TX) <= 2 or hsh(x, 0, z, 341) < 0.3 else turf(x, GY, z))
                    self.keep.add((x, z))
        b.threshold('road', TX - 2, GY + 1, VZ - 3, TX + 2, VZ - 2)

    # ------------------------------------------------------------------ the fortress

    def fortress(self):
        """Sets the fortress down on the floor of the gorge, and takes over what it knows of itself."""
        b = self.b
        c = castle.build()
        castle.bedrock(c)
        for (x, y, z), state in c.cells.items():
            if y >= castle.GROUND - 2:
                b.cells[(x + OX, y + DY, z + OZ)] = state
        for kind, x, y, z in c.marks:
            b.marks.append((kind,) + C(x, y, z))
        for name, x, y, z in c.hearths:
            b.hearths.append((name,) + C(x, y, z))
        for name, x, y, z in c.chests:
            b.chests.append((name,) + C(x, y, z))
        b.shards += [C(*s) for s in c.shards]
        b.flights += [C(*s) for s in c.flights]
        for door, lever in c.doors:
            b.doors.append((tuple(C(*cell) for cell in door), C(*lever)))
        for name, cells in c.seals.items():
            b.seals[name] = [C(*cell) for cell in cells]
        for name, cells in c.fogs.items():
            b.fogs[name] = [C(*cell) for cell in cells]
        for zone, kinds in c.census.items():
            b.census[zone] = dict(kinds)
        for x in range(OX - 4, OX + 133):
            for z in range(OZ - 4, OZ + 276):
                if any(b.get(x + dx, GY + 1, z + dz) not in (None, AIR) or b.get(x + dx, GY + 3, z + dz) not in (None, AIR) for dx in (-1, 0, 1) for dz in (-1, 0, 1)):
                    self.keep.add((x, z))
        return c

    def tiers(self):
        """How strong the dead of each place are: the town's are as they were born, the lower fortress's a step above,
        the upper city's two (see SoulsMob.setTier)."""
        b = self.b
        line = OZ + 151                                           # the north end of the great bridge
        for kind, x, y, z in b.marks:
            if kind == 'keykeeper' or z >= OZ + 272:
                continue
            tier = 1 if z >= line or kind == 'overseer' else 2
            state = b.cells[(x, y, z)]
            b.cells[(x, y, z)] = state.replace(']', f',tier={tier}]')

    def build(self):
        b = self.b
        self.land()
        self.village.build()
        b.spawn = self.village.spawn
        self.castle = self.fortress()
        end = self.causeway()
        self.highway(end)
        self.tunnel()
        self.woods()
        self.tiers()
        return b


def world(x, y, z, spawn):
    """Where a point of the plan is in the finished world (see MapBuilder.java: the plan's origin cell - the place where
    the newcomer wakes - is put at 0, -46, 0 and the plan is turned a quarter counter-clockwise)."""
    return (z - spawn[2] + 0.5, WORLD_GROUND + 1 + y - spawn[1], -(x - spawn[0]) + 0.5)


def views(w):
    """Pictures the client test takes of the finished world: name, the eye, what it looks at; ('time', hour) sets the
    hour for the pictures that follow. The first are walked to as the newcomer is."""
    v = w.village
    sx, sy, sz = v.spawn
    g = GY

    def t(c, r, dx=1, dz=1):
        return v.at(c, r, dx, dz)
    out = [('as_player_waking', (sx, sy + 1.62, sz), (sx, sy + 1.5, sz - 12)),
           ('as_player_at_the_gate', (t(20, 43)[0] + 1.5, g + 2.62, t(20, 43)[1] + 3.5), (t(20, 43)[0] + 1.5, g + 2.6, t(20, 43)[1] - 9)),
           ('time', 18000),
           ('yard_from_above', (sx + 12, g + 14, sz + 2), (sx, g + 1, sz - 8)),
           ('yard_gate', (sx - 3, g + 2.6, sz - 6), (t(20, 43)[0] + 1.5, g + 4, t(20, 43)[1])),
           ('low_street_west', (t(19, 42)[0], g + 2.6, t(19, 42)[1]), (t(8, 42)[0], g + 3, t(8, 42)[1])),
           ('low_street_east', (t(22, 42)[0], g + 2.6, t(22, 42)[1]), (t(34, 42)[0], g + 3, t(34, 42)[1])),
           ('west_alley', (t(9, 41)[0], g + 2.6, t(9, 41)[1]), (t(9, 36)[0], g + 3.5, t(9, 36)[1])),
           ('alley_turn', (t(10, 36)[0], g + 2.6, t(10, 36)[1]), (t(14, 36)[0], g + 3.5, t(14, 36)[1])),
           ('house_inside', (t(10, 39)[0] + 1, g + 2.6, t(10, 39)[1]), (t(12, 38)[0], g + 2, t(12, 38)[1])),
           ('high_street_gate', (t(20, 36)[0] + 1.5, g + 2.6, t(20, 36)[1]), (t(20, 40)[0] + 1.5, g + 3, t(20, 40)[1])),
           ('high_street_north', (t(20, 38)[0] + 1.5, g + 2.6, t(20, 38)[1]), (t(20, 29)[0] + 1.5, g + 4, t(20, 29)[1])),
           ('east_alley', (t(31, 41)[0], g + 2.6, t(31, 41)[1]), (t(31, 38)[0], g + 3.5, t(31, 38)[1])),
           ('shard_yard', (t(35, 40)[0], g + 2.6, t(35, 40)[1]), (t(37, 39)[0], g + 1.5, t(37, 39)[1] + 1)),
           ('market_from_the_south', (t(20, 28)[0] + 1.5, g + 3.2, t(20, 28)[1] + 1), (t(20, 24)[0] + 1.5, g + 3, t(20, 24)[1])),
           ('market_from_above', (t(30, 30)[0], g + 20, t(30, 30)[1]), (t(20, 24)[0], g + 1, t(20, 24)[1])),
           ('shambles', (t(20, 20)[0] + 1.5, g + 2.6, t(20, 20)[1] + 1), (t(20, 15)[0] + 1.5, g + 3.5, t(20, 15)[1])),
           ('butchers_shop', (t(18, 17)[0], g + 2.6, t(18, 17)[1]), (t(17, 14)[0], g + 2.5, t(17, 14)[1])),
           ('church_lane', (t(13, 12)[0], g + 2.6, t(13, 12)[1]), (t(21, 11)[0], g + 3.5, t(21, 11)[1])),
           ('lodge', (t(24, 13)[0], g + 2.6, t(24, 13)[1] - 0.5), (t(24, 14)[0], g + 1.8, t(24, 14)[1])),
           ('lych_gate', (t(20, 12)[0] + 1.5, g + 2.6, t(20, 12)[1] + 1), (t(20, 10)[0] + 1.5, g + 3, t(20, 10)[1])),
           ('churchyard', (t(20, 10)[0] + 1.5, g + 3, t(20, 10)[1]), (t(20, 4)[0] + 1.5, g + 3, t(20, 4)[1])),
           ('churchyard_from_above', (t(29, 12)[0], g + 18, t(29, 12)[1]), (t(20, 5)[0], g + 1, t(20, 5)[1])),
           ('town_from_above_south', (sx + 30, g + 46, sz + 26), (t(20, 30)[0], g + 2, t(20, 30)[1])),
           ('town_from_above_north', (t(34, 2)[0], g + 44, t(34, 2)[1] - 20), (t(20, 26)[0], g + 2, t(20, 26)[1])),
           ('road_to_the_tunnel', (TX, g + 2.6, VZ - 1), (TX, g + 3.5, SPUR[1] - 6)),
           ('tunnel_south_leg', (TX, g + 2.6, SPUR[1] - 6), (TX, g + 2.6, w.cross)),
           ('tunnel_bend', (TX - 2, g + 2.6, w.cross), (AX, g + 2.6, w.cross)),
           ('time', 12350),
           ('tunnel_north_leg', (AX, g + 2.6, w.cross - 3), (AX, g + 2.6, SPUR[0] - 8)),
           ('out_of_the_tunnel', (AX, g + 2.6, SPUR[0] - 5), (AX, g + 14, OZ + 250)),
           ('causeway', (AX, g + 2.6, OZ + 300), (AX, g + 12, OZ + 250))]
    for name, eye, look in castle.VIEWS:
        if name not in KEPT_VIEWS:
            continue
        if name == 'arrival_from_outside':
            eye = (30, L0 + 16, 300)
        if name == 'whole_from_south_east':
            eye = (150, L2 + 60, 320)
        if name == 'upper_city_from_south_west':
            eye = (-22, L2 + 40, 150)
        if name == 'cathedral_from_north_east':
            eye = (138, VAULT + 10, -24)
        if name == 'great_window_outside':
            eye = (64, LC + 16, -26)
        if name == 'lower_fortress_from_the_west':
            eye = (-22, L1 + 12, 250)
        out.append((name, C(*eye), C(*look)))
    out.append(('gorge_under_the_bridge', C(40, castle.GROUND + 2.6, 168), C(64, L1, 168)))
    out.append(('fortress_from_the_gorge', C(100, castle.GROUND + 2.6, 290), C(64, L1 + 6, 240)))
    out.append(('valley_from_the_terrace', C(96, LC + 2.6, 4), C(150, LC - 6, -30)))
    return out


# the pictures of the fortress that are still taken (it was looked at room by room when it was built)
KEPT_VIEWS = ('arrival_from_outside', 'ward_from_the_gate', 'keeper_room', 'great_bridge', 'bridge_from_the_side', 'into_the_city', 'skyline_from_the_square',
              'nave', 'crypt_hall', 'terrace', 'from_the_belfry', 'whole_from_south_east', 'upper_city_from_south_west', 'cathedral_from_north_east',
              'great_window_outside', 'lower_fortress_from_the_bridge', 'lower_fortress_from_the_west')


def probes(w):
    v = w.village
    sx, sy, sz = v.spawn
    out = [('yard', (sx, sy, sz - 4)), ('low_street', v.at(18, 42)[:1] + (GY + 1,) + v.at(18, 42)[1:]), ('market', v.at(20, 25)[:1] + (GY + 1,) + v.at(20, 25)[1:]),
           ('shop', v.at(18, 15)[:1] + (GY + 1,) + v.at(18, 15)[1:]), ('lodge', (v.at(24, 13)[0], GY + 1, v.at(24, 13)[1] + 1)),
           ('churchyard', v.at(20, 6)[:1] + (GY + 1,) + v.at(20, 6)[1:]), ('tunnel_bend', (AX + 8, GY + 1, w.cross)), ('tunnel_south', (TX, GY + 1, SPUR[1] - 10)),
           ('road', (AX, GY + 1, OZ + 300))]
    out += [(name, C(*at)) for name, at in castle.PROBES]
    return out


EXTRA_COLOURS = castle.EXTRA_COLOURS + [
    ('shard_heap', (120, 255, 255)), ('spruce_leaves', (44, 70, 46)), ('podzol', (90, 66, 36)), ('moss', (88, 110, 44)), ('water', (50, 80, 200)),
    ('white_terracotta', (210, 178, 160)), ('light_gray_terracotta', (135, 107, 98)), ('mud_bricks', (137, 104, 79)), ('froglight', (250, 220, 120)),
    ('black_stained', (30, 30, 36)), ('black_wool', (20, 20, 24)), ('dark_oak', (66, 44, 22)), ('place_name', (255, 0, 255)), ('fern', (70, 110, 60)),
    ('short_grass', (90, 130, 70)), ('dirt_path', (148, 122, 65)),
]

if __name__ == '__main__':
    COLOURS[0:0] = EXTRA_COLOURS
    PASSABLE_MORE = ('fern', 'short_grass', 'hanging_roots')
    castle.PASSABLE = castle.PASSABLE + PASSABLE_MORE
    castle.CLEAR = castle.CLEAR + ('fern', 'short_grass', 'leaves', 'shard_heap', 'spruce_door', 'water', 'hanging_roots')
    w = World()
    b = w.build()
    built = len([1 for s in b.cells.values() if s != AIR])
    known = valid_blocks()
    if known is not None:
        used = {(s[2] if isinstance(s, tuple) else s).split('[')[0] for s in b.cells.values()}
        unknown = sorted(n for n in used if n.startswith('minecraft:') and n.split(':')[1] not in known)
        if unknown:
            print('UNKNOWN BLOCKS:', unknown)
    report, problems, seen = analyse(b)
    spawn = b.spawn
    for kind, x, y, z in b.marks:
        for h in (0, 1) if kind not in ('gravedigger', 'overseer', 'chaplain', 'knight', 'gilded') else (0, 1, 2):
            above = b.get(x, y + h, z)
            if h and above not in (None, AIR) and not any(k in str(above) for k in castle.PASSABLE):
                problems.append(f'the {kind} at {(x, y, z)} has {above} over its head')
    states = b.save(spawn, OUT, skip_air=False)
    with open('map/views.txt', 'w') as f:
        for view in views(w):
            if view[0] == 'time':
                f.write(f'time {view[1]}\n')
            else:
                name, eye, look = view
                f.write(name + ' ' + ' '.join(f'{v:.2f}' for v in world(*eye, spawn) + world(*look, spawn)) + '\n')
        # the hearths, by name: the test rests at two of them and travels from one to the other
        for name, x, y, z in b.hearths:
            f.write(f'hearth {name} ' + ' '.join(f'{v_:.2f}' for v_ in world(x, y, z, spawn)) + '\n')
        # after the pictures the test throws every lever and fells the lords, and says what the doors and the seals did
        for door, lever in b.doors:
            f.write('lever ' + ' '.join(f'{v:.2f}' for v in world(*lever, spawn) + world(*door[0], spawn)) + '\n')
        v = w.village
        gx, gz = v.at(20, 6)
        for boss, eye, look in (('gravedigger', (gx + 1.5, GY + 3, v.at(20, 10)[1]), (gx + 1.5, GY + 3, v.at(20, 3)[1])),
                                ('overseer', C(64, L1 + 3, 146.5), C(64, L1 + 4, 120)), ('chaplain', C(64, LC + 3, 31), C(64, LC + 10, 9))):
            f.write(f'near_the_{boss} ' + ' '.join(f'{v_:.2f}' for v_ in world(*eye, spawn) + world(*look, spawn)) + '\n')
            f.write(f'kill {MOD}:{boss} ' + ' '.join(f'{v_:.2f}' for v_ in world(*b.seals[boss][0], spawn)) + '\n')
    with open('map/light.txt', 'w') as f:
        for name, at in probes(w):
            if b.get(*at) not in (None, AIR) and not any(k in str(b.get(*at)) for k in castle.PASSABLE):
                print(f'  PROBLEM: the light probe {name} is inside {b.get(*at)}')
            wx, wy, wz = world(*at, spawn)
            f.write(f'{name} {math.floor(wx)} {wy} {math.floor(wz)}\n')
    print(f'{b.name}: {b.w}x{b.h}x{b.d}, {built} blocks, {states} block states; the newcomer wakes at {spawn}; {len(w.village.houses)} houses')
    total = {}
    for zone, kinds in b.census.items():
        print(f'  {zone}: {kinds}')
        for k, n in kinds.items():
            total[k] = total.get(k, 0) + n
    print('  all:', total, sum(total.values()))
    for line in report:
        print(' ', line)
    for p in problems:
        print('  PROBLEM:', p)
    # the cathedral must be entered by the covered walk the first time: with the walk shut, nothing beyond it is reached
    shut = frozenset(C(x, y, 76) for x in (96, 97) for y in range(GAL + 1, GAL + 4))
    without, _, seen_shut = analyse(b, blocked=shut)
    inside = C(castle.CX, LC + 1, 60) in seen_shut
    # nothing built later may stand in a stair: three clear cells over every step
    for (x, y, z) in b.flights:
        if 'stairs' in str(b.get(x, y, z)):
            for h in (1, 2, 3):
                above = b.get(x, y + h, z)
                if above not in (None, AIR) and not any(k in str(above) for k in castle.PASSABLE):
                    print(f'  PROBLEM: the stair at {(x, y, z)} is shut by {above} {h} above it')
    dark, light_table = dark_places(b, seen)
    groups = {}
    for (x, y, z) in dark:
        groups.setdefault((x // 10, y // 6, z // 10), []).append((x, y, z))
    print(f'  dark places: {len(dark)} of {len(seen)}' + (': ' + ', '.join(f'{len(g)} near {sorted(g)[len(g) // 2]}' for g in sorted(groups.values(), key=len, reverse=True)[:24]) if dark else ''))
    print('  with the covered walk shut the nave is ' + ('REACHED: there is another way in. PROBLEM' if inside else 'not reached: good'))
    # how light the town's streets are by their lamps (the sky gives them nothing at night)
    street = [c for c in seen if w.village.inside(c[0], c[2]) and c[1] == GY + 1]
    lit = sum(1 for c in street if light_table[c][1] >= 4)
    print(f'  the town: {len(street)} places to stand, {lit * 100 // max(1, len(street))}% of them with lamp light of 4 or more')
    if len(sys.argv) > 1:
        out = sys.argv[1]
        os.makedirs(out, exist_ok=True)
        from PIL import ImageDraw
        for name, top, low, scale in (('world_ground', GY + 5, GY, 4), ('world_roofs', H - 1, -1, 4)):
            img = b.top_down(top, scale=scale)
            draw = ImageDraw.Draw(img)
            for (x, y, z) in seen:
                if low <= y <= top + 1:
                    sk, bl = light_table[(x, y, z)]
                    draw.point([x * scale + 1, z * scale + 1], fill=(255, 40, 40) if bl < 4 and sk < 7 else (60, 255, 90))
            img.save(f'{out}/{name}.png')
        # the town, larger: the ground with dots where a player can stand (yellow where a lamp reaches), and from above
        vx0, vz0, vx1, vz1 = VX, VZ - 60, VX + gen_village.COLS * 3, VZ + gen_village.ROWS * 3
        for name, top in (('town_ground', GY + 3), ('town_roofs', H - 1)):
            img = b.top_down(top, scale=8).crop((vx0 * 8, vz0 * 8, vx1 * 8, vz1 * 8))
            if name == 'town_ground':
                draw = ImageDraw.Draw(img)
                for (x, y, z) in seen:
                    if vx0 <= x < vx1 and vz0 <= z < vz1 and GY <= y <= GY + 3:
                        bl = light_table[(x, y, z)][1]
                        px, pz = (x - vx0) * 8 + 3, (z - vz0) * 8 + 3
                        draw.rectangle([px, pz, px + 1, pz + 1], fill=(255, 230, 80) if bl >= 8 else ((200, 150, 60) if bl >= 4 else (70, 110, 255)))
            img.save(f'{out}/{name}.png')
        sub = Map('part', W, H, D)
        sub.cells = {c: s for c, s in b.cells.items() if vx0 <= c[0] < vx1 and VZ - 4 <= c[2] < vz1 and c[1] >= GY}
        sub.iso(scale=5).save(f'{out}/town_iso.png')
