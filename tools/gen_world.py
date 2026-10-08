#!/usr/bin/env python3
"""The world of the map, put together: a wooded plateau with a gorge cut into it; in the south of the gorge the Dead
Borough (tools/gen_village.py), where the game begins at night; in the north the High Stockade on its two crags
(tools/gen_map.py), where the sun never quite sets; between them the road, which passes under a spur of the plateau by
a tunnel with a bend in it - in the bend, where no sky is seen, the hour changes.

Writes map/high_stockade.plan.txt (set down by MapBuilder.java in a flat world of forest whose ground is the plan's
layer GY), map/views.txt and map/light.txt, and, given a directory, preview pictures. Ends with the check of the map as
a place to play (see gen_map.analyse). Every further place of the map gets its own file and is joined on here.

Run from the repo root: PYTHONPATH=tools python3 tools/gen_world.py [preview dir]"""
import json
import math
import os
import sys
from collections import deque

import gen_map as castle
import gen_quarry
import gen_shore
import gen_skete
import gen_stubble
import gen_swamp
import gen_village
from gen_map import (BARS, BELL, CY, FLOOR_LANTERN, GAL, L0, L1, L2, LANTERN, LC, PASSABLE, VAULT, Map, analyse, cobbles, dark_places, hsh, stairs, valid_blocks)
from gen_structures import AIR, COLOURS, MOD

OUT = 'map/high_stockade.plan.txt'
GY = 16                             # the ground block of the valley; the flat world's own top layer is put at this height (and has this much stone
                                    # under it: the quarry goes down thirteen)
OX, OZ = 138, 42                    # where the fortress's own plan lies in this one
DY = GY - castle.GROUND             # and how much lower: its ground is the valley's
W, H, D = 492, 138, 1012                # (grown east for the Mire, and far south for the sea; the origin is the place of waking, so nothing moved)
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


def bare(x, y, z):
    """The top of the white hill: stone with a little thin turf on it."""
    n = hsh(x, 0, z, 307)
    if smooth(x, z, 6, 308) > 0.62:
        return 'minecraft:grass_block' if n < 0.75 else 'minecraft:coarse_dirt'
    return 'minecraft:calcite' if n < 0.42 else ('minecraft:diorite' if n < 0.62 else ('minecraft:gravel' if n < 0.76 else ('minecraft:coarse_dirt' if n < 0.9 else 'minecraft:grass_block')))


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
        self.quarry = gen_quarry.Quarry(self.b, GY, self.village)
        self.swamp = gen_swamp.Swamp(self.b, GY, self.village)
        self.skete = gen_skete.Skete(self.b, GY)
        self.shore = gen_shore.Shore(self.b, GY, D, W)
        self.stubble = gen_stubble.Stubble(self.b, GY)
        self.keep = set()                # columns of the valley where nothing may grow: roads, the feet of walls

    # ------------------------------------------------------------------ the shape of the gorge

    def in_gorge(self, x, z):
        """The floor of the gorge: round the fortress, the yard before the tunnel, and the town."""
        if self.village.inside(x, z) or self.swamp.inside(x, z) or self.skete.inside(x, z) or self.shore.inside(x, z) or self.stubble.inside(x, z):
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
        self.white = set()
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
            white = gen_quarry.hill(x, z)                         # the hill over the quarry, west of the town
            if border < 13:
                white = min(white, border * 0.85)
            if white > h:
                h = white
                self.white.add((x, z))
            self.height[(x, z)] = int(h)
        for (x, z), h in self.height.items():
            if h == 0:
                if (x, z) in inside and not self.village.inside(x, z):
                    b.set(x, GY, z, turf(x, GY, z))
                continue
            for y in range(GY + 1, GY + h + 1):
                exposed = y > GY + h - 2 and all(self.height.get((x + dx, z + dz), 0) >= y - GY - 1 for dx, dz in ((1, 0), (-1, 0), (0, 1), (0, -1)))
                if (x, z) in self.white:
                    b.set(x, y, z, gen_quarry.chalk(x, y, z) if not exposed or y < GY + h else bare(x, y, z))
                    continue
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
            if self.village.inside(x, z) or self.swamp.inside(x, z) or self.skete.inside(x, z) or self.shore.inside(x, z) or self.stubble.inside(x, z):
                continue                                         # (the Mire, the Firwood and the Stubble plant their own; nothing grows on the shore)
            hard = (x, z) in self.keep or any(self.height.get((x + dx, z + dz), 1) > 0 for dx, dz in ((1, 0), (-1, 0), (0, 1), (0, -1)))
            if hard:
                far[(x, z)] = 0
                queue.append((x, z))
        while queue:
            x, z = queue.popleft()
            for dx in (-1, 0, 1):
                for dz in (-1, 0, 1):
                    n = (x + dx, z + dz)
                    if n in self.inside and n not in far and not self.village.inside(*n) and not self.swamp.inside(*n) and not self.skete.inside(*n) \
                            and not self.shore.inside(*n) and not self.stubble.inside(*n):
                        far[n] = far[(x, z)] + 1
                        queue.append(n)
        trees = []
        for (x, z), h in sorted(self.height.items()):
            n = hsh(x, 7, z, 320)
            if h >= 4 and self.dist[(x, z)] >= 2 and self.stubble.near(x, z):
                # round the Stubble the plateau is fallow: no fir, a little dry grass
                if n > 0.9 and b.free(x, GY + h + 1, z) and 'grass' in str(b.get(x, GY + h, z)):
                    b.set(x, GY + h + 1, z, 'minecraft:short_dry_grass' if n < 0.97 else 'minecraft:dead_bush')
            elif h >= 4 and self.dist[(x, z)] >= 2:
                if (x, z) in self.white and (n > 0.006 or 'grass' not in str(b.get(x, GY + h, z))):
                    if n > 0.94 and b.free(x, GY + h + 1, z) and 'grass' in str(b.get(x, GY + h, z)):
                        b.set(x, GY + h + 1, z, 'minecraft:short_grass' if n < 0.98 else 'minecraft:dead_bush')
                    continue                                     # the white hill is all but bare
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
            if kind == 'keykeeper' or z >= OZ + 272 or not OX - 20 <= x <= OX + 150:
                continue                                         # (only the fortress has tiers: what lies east of its gorge is the Firwood)
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
        b.zone = 'quarry'
        self.quarry.build()
        for column in self.quarry.open_sky:                      # nothing grows in the air over the pit
            self.height[column] = 0
        b.zone = 'mire'
        self.swamp.build()
        b.zone = 'firwood'
        self.skete.build(self.height)
        b.zone = 'wharf'
        self.shore.build(self.height)
        b.zone = 'stubble'
        self.stubble.build(self.height)
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
    out.append(('time', 18000))
    out += w.quarry.views()
    mire = w.swamp.views()
    out += mire[:1]                                              # the gate, from the town's side, in the town's night
    out.append(('time', MURK))
    out += mire[1:]
    for hour in MURK_TRIED:                                      # the same two places at other hours, to choose by
        out.append(('time', hour))
        out += [(f'{name}_at_{hour}', eye, look) for name, eye, look in mire if name in ('mire_drowned_end', 'mire_log_road_north')]
    out.append(('time', 18000))
    fir = w.skete.views()
    out += fir[:1]                                               # the wicket, from the yard, in the town's night
    out.append(('time', FOREDAWN))
    out += fir[1:]
    for hour in FOREDAWN_TRIED:                                  # the same places at other hours, to choose by
        out.append(('time', hour))
        out += [(f'{name}_at_{hour}', eye, look) for name, eye, look in fir if name in ('firwood_track', 'skete_vigil_from_behind', 'skete_white_tent_from_above')]
    out.append(('time', 18000))
    sea = w.shore.views()
    out += sea[:1]                                               # the Sea Gate, from the yard, in the town's night
    out.append(('time', SUNRISE))
    out += sea[1:]
    for hour in SUNRISE_TRIED:                                   # the same places at other hours, to choose by
        out.append(('time', hour))
        out += [(f'{name}_at_{hour}', eye, look) for name, eye, look in sea if name in ('shore_long_pier', 'shore_spit_from_its_root', 'shore_quay_east')]
    out.append(('time', 18000))
    corn = w.stubble.views()
    out += corn[:1]                                              # the Reapers' Gate, from the yard, in the town's night
    out.append(('time', AFTERNOON))
    out += corn[1:]
    for hour in AFTERNOON_TRIED:                                 # the same places at other hours, to choose by
        out.append(('time', hour))
        out += [(f'{name}_at_{hour}', eye, look) for name, eye, look in corn if name in ('stubble_cut_land', 'stubble_watched_field', 'stubble_mill_from_the_floor')]
    out.append(('time', 18000))
    return out


AFTERNOON = 8000                    # the hour of the Stubble: see PlaceNameBlock.AFTERNOON
AFTERNOON_TRIED = ()
SUNRISE = 23600                     # the hour of the Morning Wharf: see PlaceNameBlock.SUNRISE
SUNRISE_TRIED = ()                  # (23250: the sun half out and the land still dark; 23950 and 450: day already, the mist gone white)


FOREDAWN = 22600                    # the hour of the Firwood: see PlaceNameBlock.FOREDAWN
FOREDAWN_TRIED = ()                 # (22200: no glow at all; 22900: the sun is on the horizon - that is the shore's hour, not this one's)
MURK = 6000                         # the hour of the Mire: see PlaceNameBlock.MURK
MURK_TRIED = ()


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
    out += w.quarry.probes()
    out += w.swamp.probes()
    out += w.skete.probes()
    out += w.shore.probes()
    out += w.stubble.probes()
    return out


EXTRA_COLOURS = castle.EXTRA_COLOURS + [
    ('shard_heap', (120, 255, 255)), ('spruce_leaves', (44, 70, 46)), ('podzol', (90, 66, 36)), ('moss', (88, 110, 44)), ('water', (50, 80, 200)),
    ('white_terracotta', (210, 178, 160)), ('light_gray_terracotta', (135, 107, 98)), ('mud_bricks', (137, 104, 79)), ('froglight', (250, 220, 120)),
    ('black_stained', (30, 30, 36)), ('black_wool', (20, 20, 24)), ('dark_oak', (66, 44, 22)), ('place_name', (255, 0, 255)), ('fern', (70, 110, 60)),
    ('short_grass', (90, 130, 70)), ('dirt_path', (148, 122, 65)), ('locked_gate', (255, 120, 0)), ('calcite', (222, 222, 214)), ('diorite', (196, 196, 196)),
    ('dripstone', (132, 104, 88)), ('glow_lichen', (110, 220, 190)), ('rail', (150, 120, 80)), ('spruce', (104, 78, 48)),
    ('white_wool', (232, 232, 226)), ('light_gray_wool', (176, 176, 170)), ('red_wool', (160, 40, 36)), ('mushroom_stem', (226, 216, 180)),
    ('candle', (255, 214, 120)), ('packed_mud', (140, 106, 80)), ('rooted_dirt', (124, 90, 62)), ('coarse_dirt', (112, 80, 54)), ('bone_block', (220, 214, 190)),
    ('sea_shallows', (150, 196, 204)), ('sandstone', (204, 190, 146)), ('sand', (222, 208, 162)), ('clay', (150, 156, 168)), ('seagrass', (60, 120, 90)),
    ('bricks', (150, 90, 74)), ('granite', (150, 104, 86)), ('glass', (190, 220, 230)), ('froglight', (250, 220, 120)),
    ('hay_block', (206, 170, 60)), ('tall_grass', (224, 190, 80)), ('dry_grass', (190, 160, 90)), ('sunflower', (250, 210, 40)), ('smooth_stone', (160, 160, 160)),
    ('oak_log', (110, 86, 52)), ('oak_planks', (162, 130, 78)), ('oak_fence', (150, 120, 70)), ('oak_slab', (162, 130, 78)),
]

if __name__ == '__main__':
    COLOURS[0:0] = EXTRA_COLOURS
    PASSABLE_MORE = ('fern', 'short_grass', 'hanging_roots', 'moss_carpet', 'tall_grass', 'dry_grass', 'sunflower')
    castle.PASSABLE = castle.PASSABLE + PASSABLE_MORE
    castle.CLEAR = castle.CLEAR + ('fern', 'short_grass', 'leaves', 'shard_heap', 'spruce_door', 'water', 'hanging_roots', 'waterlogged=true',
                                   'brown_mushroom', 'red_mushroom', 'tall_grass', 'dry_grass', 'sunflower')
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
    # what comes into reach before each lord falls - the dead (with their tiers), the chests, the shards: written down
    # for tools/balance.py, which then has no need to build the world again
    census, known_marks, known_chests, known_shards = {}, set(), set(), set()
    reached = {opened: stage_seen for opened, stage_seen in analyse.seen_at}
    names = [name for name, label in castle.STAGES if name in b.seals]
    for i, name in enumerate(names):
        if not any(kind == name for kind, x, y, z in b.marks):
            continue                                             # (a key, not a lord)
        before = reached[names[i - 1] if i else None]
        entry = {'mobs': {}, 'chests': [], 'shards': 0}
        for kind, x, y, z in b.marks:
            state = b.cells[(x, y, z)]
            tier = int(state.split('tier=')[1][0]) if 'tier=' in state else 0
            if kind == name:
                entry['tier'] = tier
            if (x, y, z) in before and (x, y, z) not in known_marks:
                known_marks.add((x, y, z))
                entry['mobs'][f'{kind}:{tier}'] = entry['mobs'].get(f'{kind}:{tier}', 0) + 1
        for chest, x, y, z in b.chests:
            if chest not in known_chests and any((x + dx, y + dy, z + dz) in before for dx in range(-2, 3) for dy in (-1, 0, 1) for dz in range(-2, 3)):
                known_chests.add(chest)
                entry['chests'].append(chest)
        for sx, sy, sz in b.shards:
            if (sx, sy, sz) not in known_shards and any((sx + dx, sy, sz + dz) in before for dx in (-1, 0, 1) for dz in (-1, 0, 1)):
                known_shards.add((sx, sy, sz))
                entry['shards'] += 1
        census[name] = entry
    with open('map/census.json', 'w') as f:
        json.dump(census, f, indent=1, sort_keys=True)
        f.write('\n')
    for kind, x, y, z in b.marks:
        tall = 6 if kind in ('weeping_mother', 'scorched_warden') else 4 if kind in ('lanky', 'elder') else 3 if kind in (
            'hollow_miller', 'mower', 'sackman', 'scarecrow', 'stook', 'sungazer',
            'gravedigger', 'overseer', 'chaplain', 'knight', 'gilded', 'unhewn', 'taskmaster', 'stone_eater', 'shatun', 'waxen', 'helmsman', 'capsized',
            'pilot', 'oarsman') else 2
        for h in range(tall):
            above = b.get(x, y + h, z)
            if h and above not in (None, AIR) and not any(k in str(above) for k in castle.PASSABLE) and not castle.is_water(above):
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
                                ('overseer', C(64, L1 + 3, 146.5), C(64, L1 + 4, 120)), ('chaplain', C(64, LC + 3, 31), C(64, LC + 10, 9)),
                                ('taskmaster', (55, GY - 10, 525), (43, GY - 10, 535)),
                                ('stone_eater', (31, GY - 10, 488), (39, GY - 10, 477)),
                                ('wallower', (gen_swamp.WEIR[0] - 9, GY + 4, gen_swamp.WEIR[1] + 3), (gen_swamp.WEIR[0], GY + 2, gen_swamp.WEIR[1])),
                                ('weeping_mother', (gen_swamp.ISLE[0] + 2, GY + 5, gen_swamp.ISLE[1] - 10), (gen_swamp.ISLE[0], GY + 4, gen_swamp.ISLE[1])),
                                ('shatun', (gen_skete.DEN[0] - 3, GY + 4, gen_skete.DEN[1] + 11), (gen_skete.DEN[0], GY + 2, gen_skete.DEN[1] - 6)),
                                ('elder', (gen_skete.TENT[0] + 1, GY + 4, gen_skete.TENT[1] - 9), (gen_skete.TENT[0], GY + 3, gen_skete.TENT[1] + 4)),
                                ('helmsman', (gen_shore.SHIP[0] - 3, GY + 5, gen_shore.SHIP[1] + 34), (gen_shore.SHIP[0], GY + 5, gen_shore.SHIP[1] + 12)),
                                ('capsized', (gen_shore.SHOAL[0] + 9, GY + 3, gen_shore.SHOAL[1] + 5), (gen_shore.SHOAL[0], GY + 1, gen_shore.SHOAL[1] - 4)),
                                ('scorched_warden', (gen_stubble.WATCH[0] + 13, GY + 4, gen_stubble.WATCH[1] + 6), (gen_stubble.WATCH[0], GY + 4, gen_stubble.WATCH[1])),
                                ('hollow_miller', (gen_stubble.MILL[0] - 9, GY + 3, gen_stubble.MILL[1] + 5), (gen_stubble.MILL[0], GY + 2, gen_stubble.MILL[1]))):
            f.write(f'near_the_{boss} ' + ' '.join(f'{v_:.2f}' for v_ in world(*eye, spawn) + world(*look, spawn)) + '\n')
            f.write(f'kill {MOD}:{boss} ' + ' '.join(f'{v_:.2f}' for v_ in world(*b.seals[boss][0], spawn)) + '\n')
        # and tries the locked gate without its key and with it
        for key in ('quarry_key', 'mire_key', 'firwood_key', 'shore_key', 'stubble_key'):
            f.write('gate ' + ' '.join(f'{v_:.2f}' for v_ in world(*sorted(b.seals[key])[0], spawn)) + '\n')
    # the columns that get a biome of their own (MapBuilder lays it): boxes of the world, four blocks wide - the game
    # keeps a biome for every four blocks each way - a line each
    with open('map/biomes.txt', 'w') as f:
        for biome, columns in (('mire', w.swamp.biome_columns()), ('shore', w.shore.biome_columns()), ('stubble', w.stubble.biome_columns())):
            quarts = {}
            for (x, z) in columns:
                wx, _, wz = world(x, GY, z, spawn)
                quarts.setdefault(math.floor(wx) >> 2, set()).add(math.floor(wz) >> 2)
            for qx in sorted(quarts):
                run = sorted(quarts[qx])
                start = prev = run[0]
                for qz in run[1:] + [None]:
                    if qz is None or qz != prev + 1:
                        f.write(f'{MOD}:{biome} {qx * 4} {start * 4} {qx * 4 + 3} {prev * 4 + 3}\n')
                        start = qz
                    prev = qz
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
    # fog, a seal or a grate shuts a way only if it fills it: beside every cell of one, in its own plane, there is
    # more of it or something solid (the fog before the Stone-Eater once stood where the cave had eaten the drift's
    # wall, and was walked round)
    for name, group in list(b.fogs.items()) + list(b.seals.items()):
        group = set(group)
        flat = [i for i in range(3) if len({c[i] for c in group}) == 1]
        if len(flat) != 1 or flat[0] == 1:
            print(f'  PROBLEM: {name} is not one upright sheet')
            continue
        for c in sorted(group):
            for i in range(3):
                for d in (-1, 1):
                    n = tuple(v + (d if j == i else 0) for j, v in enumerate(c))
                    if i == flat[0] or n in group:
                        continue
                    s = b.get(*n)
                    if (s is None and n[1] > GY) or s == AIR or (s is not None and any(k in str(s) for k in castle.PASSABLE)):
                        print(f'  PROBLEM: {name} at {c} leaves a way round it at {n}: {s}')
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
        # the quarry: what lies at each of its three levels (everything above taken off), and a cut through it from
        # the gate westwards along the line of the street
        q = w.quarry
        for name, top in (('quarry_low', q.LOW + 3), ('quarry_mid', q.MID + 4), ('quarry_top', q.TOP + 4), ('quarry_sky', H - 1)):
            b.top_down(top, scale=6).crop((0, 430 * 6, 175 * 6, D * 6)).save(f'{out}/{name}.png')
        from PIL import Image
        for name, zz in (('quarry_cut_street', q.zc), ('quarry_cut_shaft', q.zc - 11), ('quarry_cut_field', q.zc + 20)):
            img = Image.new('RGB', (175 * 6, 60 * 6), (16, 18, 24))
            draw = ImageDraw.Draw(img)
            for x in range(175):
                for y in range(60):
                    state = b.cells.get((x, y, zz))
                    if state is None or state == AIR:
                        if state == AIR:
                            draw.rectangle([x * 6, (59 - y) * 6, x * 6 + 5, (59 - y) * 6 + 5], fill=(70, 80, 96) if (x, y, zz) in seen or (x, y - 1, zz) in seen else (44, 50, 62))
                        continue
                    draw.rectangle([x * 6, (59 - y) * 6, x * 6 + 5, (59 - y) * 6 + 5], fill=b.colour(state))
            img.save(f'{out}/{name}.png')
        sub = Map('part', W, H, D)
        sub.cells = {c: s for c, s in b.cells.items() if vx0 <= c[0] < vx1 and VZ - 4 <= c[2] < vz1 and c[1] >= GY}
        sub.iso(scale=5).save(f'{out}/town_iso.png')
        # the Mire: its ground with a dot on every place a player can be (blue in the water), and from above
        mx0, mx1, mz0, mz1 = 266, W - 8, 462, 632
        for name, top in (('mire_ground', GY + 3), ('mire_roofs', H - 1)):
            img = b.top_down(top, scale=6).crop((mx0 * 6, mz0 * 6, mx1 * 6, mz1 * 6))
            if name == 'mire_ground':
                draw = ImageDraw.Draw(img)
                for (x, y, z) in seen:
                    if mx0 <= x < mx1 and mz0 <= z < mz1 and GY - 1 <= y <= GY + 3:
                        px, pz = (x - mx0) * 6 + 2, (z - mz0) * 6 + 2
                        draw.rectangle([px, pz, px + 1, pz + 1], fill=(120, 200, 255) if castle.is_water(b.get(x, y, z)) else (255, 235, 90))
            img.save(f'{out}/{name}.png')
        # the Firwood: its ground with a dot on every place a player can be, from above, and the skete from the side
        fx0, fx1, fz0, fz1 = 222, 478, 262, 452
        for name, top in (('firwood_ground', GY + 3), ('firwood_roofs', H - 1)):
            img = b.top_down(top, scale=6).crop((fx0 * 6, fz0 * 6, fx1 * 6, fz1 * 6))
            if name == 'firwood_ground':
                draw = ImageDraw.Draw(img)
                for (x, y, z) in seen:
                    if fx0 <= x < fx1 and fz0 <= z < fz1 and GY <= y <= GY + 3:
                        px, pz = (x - fx0) * 6 + 2, (z - fz0) * 6 + 2
                        draw.rectangle([px, pz, px + 1, pz + 1], fill=(255, 235, 90))
            img.save(f'{out}/{name}.png')
        # the shore: from above, with a dot on every place a player can be (blue where they wade)
        img = b.top_down(H - 1, scale=4).crop((120 * 4, 540 * 4, 440 * 4, 900 * 4))
        draw = ImageDraw.Draw(img)
        for (x, y, z) in seen:
            if 120 <= x < 440 and 540 <= z < 900:
                px, pz = (x - 120) * 4 + 1, (z - 540) * 4 + 1
                draw.rectangle([px, pz, px + 1, pz + 1], fill=(120, 200, 255) if castle.is_water(b.get(x, y, z)) else (255, 90, 60))
        img.save(f'{out}/shore_ground.png')
        # the Stubble: its ground with a dot on every place a player can be, and from above and the side
        tx0, tx1, tz0, tz1 = 8, 222, 352, 484
        for name, top in (('stubble_ground', GY + 3), ('stubble_roofs', H - 1)):
            img = b.top_down(top, scale=6).crop((tx0 * 6, tz0 * 6, tx1 * 6, tz1 * 6))
            if name == 'stubble_ground':
                draw = ImageDraw.Draw(img)
                for (x, y, z) in seen:
                    if tx0 <= x < tx1 and tz0 <= z < tz1 and GY <= y <= GY + 3:
                        px, pz = (x - tx0) * 6 + 2, (z - tz0) * 6 + 2
                        draw.rectangle([px, pz, px + 1, pz + 1], fill=(255, 60, 60))
            img.save(f'{out}/{name}.png')
        sub = Map('part', W, H, D)
        sub.cells = {c: s for c, s in b.cells.items() if tx0 <= c[0] < 200 and tz0 <= c[2] < tz1 and c[1] >= GY and gen_stubble.Stubble.inside(w.stubble, c[0], c[2])}
        sub.iso(scale=5).save(f'{out}/stubble_iso.png')
        sub = Map('part', W, H, D)
        sub.cells = {c: s for c, s in b.cells.items() if 388 <= c[0] < 474 and 326 <= c[2] < 450 and c[1] >= GY and 'spruce_leaves' not in str(s)}
        sub.iso(scale=5).save(f'{out}/skete_iso.png')
        sub.cells = {c: s for c, s in b.cells.items() if 270 <= c[0] < 386 and 268 <= c[2] < 402 and c[1] >= GY}
        sub.iso(scale=4).save(f'{out}/firwood_iso.png')
