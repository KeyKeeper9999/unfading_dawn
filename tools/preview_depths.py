#!/usr/bin/env python3
"""The Sky Beneath by itself, without the rest of the world: for looking at while it is being built (the whole map takes
minutes; this takes seconds). Nothing is checked here but that it builds and that its ways are whole: the check of the
map as a place to play is gen_world.py's.

Run from the repo root: PYTHONPATH=tools python3 tools/preview_depths.py DIR"""
import sys
from collections import deque

from PIL import Image, ImageChops, ImageDraw

import gen_depths
import gen_map as castle
import gen_world
from gen_depths import HIGH, PLATE, VOID
from gen_map import Map
from gen_structures import AIR, COLOURS

X0, X1, Z0, Z1 = 0, 164, 532, 830
UNSEEN = ('minecraft:light', 'place_name')


def top_down(b, top, scale, low=0, dots=()):
    img = Image.new('RGB', ((X1 - X0) * scale, (Z1 - Z0) * scale), (40, 44, 52))
    draw = ImageDraw.Draw(img)
    for x in range(X0, X1):
        for z in range(Z0, Z1):
            for y in range(top, low - 1, -1):
                s = b.cells.get((x, y, z))
                if s is None or s == AIR or any(k in s for k in UNSEEN):
                    continue
                c = b.colour(s)
                k = 0.55 + 0.45 * (y - low) / max(1, top - low)
                draw.rectangle(((x - X0) * scale, (z - Z0) * scale, (x - X0 + 1) * scale - 1, (z - Z0 + 1) * scale - 1), fill=tuple(int(v * k) for v in c))
                break
    for (x, z, c) in dots:
        draw.rectangle(((x - X0) * scale + 1, (z - Z0) * scale + 1, (x - X0 + 1) * scale - 2, (z - Z0 + 1) * scale - 2), fill=c)
    for z in range(Z0 - Z0 % 20 + 20, Z1, 20):
        draw.text((2, (z - Z0) * scale), str(z), fill=(255, 255, 255))
    for x in range(20, X1, 20):
        draw.text(((x - X0) * scale, 2), str(x), fill=(255, 255, 255))
    return img


def cropped(img):
    """A picture without the empty country round what is drawn in it."""
    box = ImageChops.difference(img, Image.new('RGB', img.size, img.getpixel((0, 0)))).getbbox()
    return img.crop(box) if box else img


def walk(b, place, start):
    """Every floor cell that can be walked to from `start` by steps of one block up or down, fog and gates being open:
    a quick look at whether the ways are whole. (Jumps are not made: the lesser plate is not reached.)"""
    def stand(x, y, z):
        s = b.cells.get((x, y, z))
        solid = s not in (None, AIR) and not any(k in s for k in ('light', 'place_name', 'night_shallows', 'mob_mark', 'fog_gate', 'locked_gate', 'cluster', 'shard', 'lantern'))
        def open_(yy):
            t = b.cells.get((x, yy, z))
            return t in (None, AIR) or any(k in t for k in ('light', 'place_name', 'night_shallows', 'mob_mark', 'fog_gate', 'locked_gate', 'shard'))
        return solid and 'under_sky' not in s and open_(y + 1) and open_(y + 2)
    seen = {start}
    queue = deque([start])
    while queue:
        x, y, z = queue.popleft()
        for dx, dz in ((1, 0), (-1, 0), (0, 1), (0, -1)):
            for dy in (0, 1, -1):
                n = (x + dx, y + dy, z + dz)
                if n not in seen and stand(*n):
                    seen.add(n)
                    queue.append(n)
                    break
    return seen


if __name__ == '__main__':
    out = sys.argv[1]
    COLOURS[0:0] = gen_world.EXTRA_COLOURS
    b = Map('depths', gen_world.W, gen_world.H, gen_world.D)
    g = gen_world.GY
    place = gen_depths.Depths(b, g)
    # (the rock the drift is cut through, and the sump it starts in: a floor to stand on before the grate)
    for x in range(gen_depths.GATE[0] - 2, gen_depths.GATE[0] + 3):
        for z in range(gen_depths.GATE[1] - 4, gen_depths.GATE[1]):
            b.set(x, gen_depths.SUMP_FLOOR, z, 'minecraft:stone')
    place.build({})
    for zone, kinds in b.census.items():
        print(zone, kinds)
    print('hearths', b.hearths)
    print('chests', [c[0] for c in b.chests], 'shards', len(b.shards))
    print('fogs', {k: len(v) for k, v in b.fogs.items()}, 'seals', {k: len(v) for k, v in b.seals.items()})
    # the walk of gen_map.py, from the sump: what is reached with the key, after the Bolt, after the Coming
    castle.NO_FOOTING = castle.NO_FOOTING + ('under_sky',)
    b.spawn = (gen_depths.GATE[0], gen_depths.SUMP_FLOOR + 1, gen_depths.GATE[1] - 2)
    report, problems, seen = castle.analyse(b)
    for line in report:
        print(' ', line[:230])
    for p in problems:
        print('  PROBLEM:', p)
    for (x, y, z) in b.flights:
        if 'stairs' in str(b.get(x, y, z)):
            for h in (1, 2, 3):
                above = b.get(x, y + h, z)
                if above not in (None, AIR) and not any(k in str(above) for k in castle.PASSABLE):
                    print(f'  PROBLEM: the stair at {(x, y, z)} is shut by {above} {h} above it')
    dots = [(x, z, (255, 255, 255)) for (x, y, z) in seen]
    top_down(b, HIGH + 12, 5, VOID).save(f'{out}/depths_plan.png')
    top_down(b, HIGH + 12, 5, VOID, dots).save(f'{out}/depths_walk.png')
    top_down(b, gen_world.H - 1, 3).save(f'{out}/depths_hill.png')
    sub = Map('part', gen_world.W, gen_world.H, gen_world.D)
    sub.cells = {c: s for c, s in b.cells.items() if (c[0], c[2]) in place.hollow and VOID < c[1] < place.ceiling(c[0], c[2]) and s != AIR
                 and 'under_sky' not in s and not any(k in s for k in UNSEEN)}
    cropped(sub.iso(scale=5)).save(f'{out}/depths_iso.png')
    for name, (cx, cz, r) in (('water', (84, 656, 50)), ('works', (78, 712, 34)), ('causeways', (60, 750, 46)), ('tear', (62, 786, 30))):
        part = Map('part', gen_world.W, gen_world.H, gen_world.D)
        part.cells = {c: s for c, s in sub.cells.items() if abs(c[0] - cx) <= r and abs(c[2] - cz) <= r * 0.8}
        cropped(part.iso(scale=9)).save(f'{out}/depths_{name}.png')
    # a cut through the hill from north to south along the drift's last leg, and one from west to east through the Works
    for name, fixed, axis in (('cut_ns', gen_depths.LEG_X, 'x'), ('cut_ns_mid', 62, 'x'), ('cut_ew', gen_depths.WORKS[1], 'z')):
        span = (Z0, Z1) if axis == 'x' else (X0, X1)
        img = Image.new('RGB', ((span[1] - span[0]) * 4, 80 * 4), (40, 44, 52))
        draw = ImageDraw.Draw(img)
        for u in range(*span):
            for y in range(80):
                s = b.cells.get((fixed, y, u) if axis == 'x' else (u, y, fixed))
                if s is None or s == AIR or any(k in s for k in UNSEEN):
                    continue
                draw.rectangle(((u - span[0]) * 4, (79 - y) * 4, (u - span[0]) * 4 + 3, (79 - y) * 4 + 3), fill=b.colour(s))
        img.save(f'{out}/depths_{name}.png')
