#!/usr/bin/env python3
"""The Sky Beneath by itself, without the rest of the world: for looking at while it is being built (the whole map takes
minutes; this takes seconds). It builds the place, walks it with the map's own check from the sump (what is reached with
the key, after the Bolt, after the Coming), and draws it:

    depths_plan.png   from above with the rock over it taken away: floors, what stands on them, pits, the columns that
                      are open to a sky (a blue edge), and a dot on every place a player can be;
    depths_cut_*.png  cut through from top to bottom: rock, room, the sheets of mist and the lids.

Run from the repo root: PYTHONPATH=tools python3 tools/preview_depths.py DIR"""
import sys

from PIL import Image, ImageDraw

import gen_depths
import gen_map as castle
import gen_world
from gen_depths import HIGH, PLATE
from gen_map import Map
from gen_structures import AIR, COLOURS

X0, X1, Z0, Z1 = 0, 164, 532, 830
Y0, Y1 = gen_world.GY - gen_world.UNDER, 12
UNSEEN = ('minecraft:light', 'place_name')
SCENERY = ('sky_veil', 'sky_lid')


def plan(b, place, scale, dots=()):
    img = Image.new('RGB', ((X1 - X0) * scale, (Z1 - Z0) * scale), (58, 58, 64))
    draw = ImageDraw.Draw(img)

    def cell(x, z, fill, inset=0):
        draw.rectangle(((x - X0) * scale + inset, (z - Z0) * scale + inset, (x - X0 + 1) * scale - 1 - inset, (z - Z0 + 1) * scale - 1 - inset), fill=fill)
    for (x, z), (y, way, i, stair) in place.cut.items():
        cell(x, z, (120, 110, 96) if stair else (96, 96, 100))
    for (x, z) in place.pits:
        cell(x, z, (10, 12, 40))
    for (x, z), y in place.ground.items():
        c = None
        for yy in range(y + 4, y - 1, -1):
            s = b.cells.get((x, yy, z))
            if s is None or s == AIR or any(k in str(s) for k in UNSEEN):
                continue
            c = b.colour(s)
            k = 0.7 + 0.1 * (yy - y)
            c = tuple(min(255, int(v * k)) for v in c)
            break
        cell(x, z, c or (0, 0, 0))
        if (x, z) in place.shaft and any((x + dx, z + dz) not in place.shaft for dx, dz in gen_depths.FOUR):
            draw.rectangle(((x - X0) * scale, (z - Z0) * scale, (x - X0 + 1) * scale - 1, (z - Z0 + 1) * scale - 1), outline=(70, 110, 255))
    for (x, y, z), s in b.cells.items():
        if isinstance(s, str) and 'mob_mark' in s and y < 0:
            draw.text(((x - X0) * scale, (z - Z0) * scale - 2), s.split('mob=')[1][:2], fill=(255, 255, 255))
    for (x, z, c) in dots:
        draw.rectangle(((x - X0) * scale + scale // 2 - 1, (z - Z0) * scale + scale // 2 - 1, (x - X0) * scale + scale // 2, (z - Z0) * scale + scale // 2), fill=c)
    for z in range(Z0 - Z0 % 20 + 20, Z1, 20):
        draw.text((2, (z - Z0) * scale), str(z), fill=(255, 255, 255))
    for x in range(20, X1, 20):
        draw.text(((x - X0) * scale, 2), str(x), fill=(255, 255, 255))
    return img


def cut(b, fixed, axis, scale=5):
    """The place cut through along a line of x ('x': seen from the east) or of z."""
    span = (Z0, Z1) if axis == 'x' else (X0, X1)
    img = Image.new('RGB', ((span[1] - span[0]) * scale, (Y1 - Y0) * scale), (70, 70, 76))
    draw = ImageDraw.Draw(img)
    for u in range(*span):
        for y in range(Y0, Y1):
            s = b.cells.get((fixed, y, u) if axis == 'x' else (u, y, fixed))
            if s is None:
                continue
            c = (8, 10, 24) if s == AIR or any(k in str(s) for k in UNSEEN) else ((90, 60, 200) if 'sky_veil' in str(s) else ((255, 255, 255) if 'sky_lid' in str(s) else b.colour(s)))
            draw.rectangle(((u - span[0]) * scale, (Y1 - 1 - y) * scale, (u - span[0] + 1) * scale - 1, (Y1 - y) * scale - 1), fill=c)
    for u in range(span[0] - span[0] % 20 + 20, span[1], 20):
        draw.text(((u - span[0]) * scale, 2), str(u), fill=(255, 255, 255))
    for y in range(Y0 - Y0 % 10, Y1, 10):
        draw.text((2, (Y1 - 1 - y) * scale), str(y), fill=(255, 255, 255))
    return img


def build():
    b = Map('depths', gen_world.W, gen_world.H, gen_world.D)
    b.low = gen_world.GY - gen_world.UNDER
    place = gen_depths.Depths(b, gen_world.GY)
    # (the sump the drift starts in: a floor to stand on before the grate)
    for x in range(gen_depths.GATE[0] - 2, gen_depths.GATE[0] + 3):
        for z in range(gen_depths.GATE[1] - 4, gen_depths.GATE[1]):
            b.set(x, gen_depths.SUMP_FLOOR, z, 'minecraft:stone')
    place.build()
    return b, place


if __name__ == '__main__':
    out = sys.argv[1]
    COLOURS[0:0] = gen_world.EXTRA_COLOURS
    b, place = build()
    for zone, kinds in b.census.items():
        print(zone, kinds)
    print('hearths', b.hearths)
    print('chests', [c[0] for c in b.chests], 'shards', len(b.shards))
    print('fogs', {k: len(v) for k, v in b.fogs.items()}, 'seals', {k: len(v) for k, v in b.seals.items()})
    print('cells', len(b.cells), 'open to a sky', len(place.open_sky), 'lids', sum(1 for s in b.cells.values() if s == gen_depths.LID),
          'sheets of mist', sum(1 for s in b.cells.values() if isinstance(s, str) and 'sky_veil' in s))
    castle.NO_FOOTING = castle.NO_FOOTING + ('under_sky',) + SCENERY
    castle.PASSABLE = castle.PASSABLE + SCENERY
    castle.CLEAR = castle.CLEAR + SCENERY + ('shard_heap',)
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
    dark, table = castle.dark_places(b, seen)
    print(f'  dark places: {len(dark)} of {len(seen)}', sorted(dark)[:8])
    plan(b, place, 6, [(x, z, (255, 60, 60)) for (x, y, z) in seen]).save(f'{out}/depths_plan.png')
    for name, fixed, axis in (('ns_drift', gen_depths.LEG_X, 'x'), ('ns_gate', gen_depths.GATE[0], 'x'), ('ns_works', gen_depths.WORKS[0], 'x'),
                              ('ns_tear', gen_depths.TEAR[0], 'x'), ('ew_water', 652, 'z'), ('ew_works', gen_depths.WORKS[1], 'z'), ('ew_ways', 757, 'z')):
        cut(b, fixed, axis).save(f'{out}/depths_cut_{name}.png')
