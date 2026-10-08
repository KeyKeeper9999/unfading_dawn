#!/usr/bin/env python3
"""The Stubble by itself, without the rest of the world: for looking at while it is being built (the whole map takes
minutes; this takes seconds). The plateau round it is a slab of stone of one height. Nothing is checked here but that
it builds: the check of the map as a place to play is gen_world.py's.

Run from the repo root: PYTHONPATH=tools python3 tools/preview_stubble.py DIR"""
import sys

import gen_map as castle
import gen_stubble
import gen_world
from gen_map import Map
from gen_structures import AIR, COLOURS

if __name__ == '__main__':
    out = sys.argv[1]
    COLOURS[0:0] = gen_world.EXTRA_COLOURS
    b = Map('stubble', gen_world.W, gen_world.H, gen_world.D)
    g = gen_world.GY
    place = gen_stubble.Stubble(b, g)
    x0, x1, z0, z1 = gen_stubble.X0 - 12, gen_stubble.GATE[0] + 6, gen_stubble.Z0 - 12, gen_stubble.Z1 + 12
    height = {}
    for x in range(x0, x1 + 1):
        for z in range(z0, z1 + 1):
            if not place.inside(x, z) and not (x > gen_stubble.GATE[0]):
                height[(x, z)] = 7
                b.fill(x, g + 1, z, x, g + 7, z, 'minecraft:stone')
            b.set(x, g, z, 'minecraft:stone') if b.get(x, g, z) is None else None
    b.zone = 'stubble'
    place.build(height)
    for zone, kinds in b.census.items():
        print(zone, kinds)
    print('hearths', b.hearths, 'chests', [c[0] for c in b.chests], 'shards', len(b.shards))
    img = b.top_down(g + 3, scale=6).crop((x0 * 6, z0 * 6, (x1 + 1) * 6, (z1 + 1) * 6))
    img.save(f'{out}/stubble_ground.png')
    b.top_down(gen_world.H - 1, scale=6).crop((x0 * 6, z0 * 6, (x1 + 1) * 6, (z1 + 1) * 6)).save(f'{out}/stubble_roofs.png')
    sub = Map('part', gen_world.W, gen_world.H, gen_world.D)
    sub.cells = {c: s for c, s in b.cells.items() if c[1] >= g and place.inside(c[0], c[2]) and s != AIR}
    sub.iso(scale=5).save(f'{out}/stubble_iso.png')
    cx, cz = gen_stubble.MILL
    sub.cells = {c: s for c, s in b.cells.items() if c[1] >= g and abs(c[0] - cx) <= 30 and abs(c[2] - cz) <= 22 and place.inside(c[0], c[2]) and s != AIR}
    sub.iso(scale=9).save(f'{out}/stubble_mill.png')
    sub.cells = {c: s for c, s in b.cells.items() if c[1] >= g and abs(c[0] - cx) <= 14 and c[2] <= cz and c[2] >= cz - 14 and s != AIR and c[1] <= g + 12}
    sub.iso(scale=12).save(f'{out}/stubble_mill_cut.png')
