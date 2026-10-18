#!/usr/bin/env python3
"""The clock tower of the Dead Borough: a plan, written into data/unfading_dawn/plan/ (read by world/Plan.java, set down layer
by layer by Clock.java, not by world generation). (What rises over the sea when the day is ended is not built of blocks: it is
an entity, tools/gen_colossus.py.)

The tower comes up out of the borough's market when the seven keys are in the clock under the Sky Beneath. It is drawn
the way the borough's plan is (south down the page; the door in the south wall), and Clock sets it down turned as the
map is, so that what is drawn here is what stands there. Its plan has a place of its own: the market's middle, thirteen
blocks square (gen_village.py leaves it free of stalls and says where it is: `market_centre`).

    ground: a plinth, a door in the south face, a floor of planks;
    the shaft: nine blocks square, hollow, with a stair going round its inside wall (a block up for every cell) and
        a rail on the inner side of it; windows of bars; in the middle the pendulum, a chain and a bob of copper;
    the clock: on all four faces a dial, copper rim and amber marks, the hands at five;
    the belfry: arches on four sides with a low wall across each, the great bell hung in the middle (its rope, a block
        of the mod's own, hangs in the bell's mouth) and, on the west, the Founder's seat; the stair comes out at the
        north-east corner;
    the roof: a pyramid of dark tiles with a lantern at its point.

Run from the repo root: PYTHONPATH=tools python3 tools/gen_tower.py [preview dir]"""
import math
import sys

from gen_structures import AIR, BARS, CHAIN, MOD, Build, brick, noise, stairs

W = 13
H = 56                                       # (the roof's point is at layer 54)
CENTRE = 6
FEET = 1                                       # the layer the feet stand in (layer 0 is the floor)
LANTERN_UP = 'minecraft:lantern[hanging=false]'
LANTERN_DOWN = 'minecraft:lantern[hanging=true]'
FENCE = 'minecraft:dark_oak_fence'
COPPER = 'minecraft:waxed_cut_copper'
COPPER_OLD = 'minecraft:waxed_exposed_cut_copper'
BELL = 'minecraft:waxed_exposed_copper'
STEPS = 36                                     # the stair climbs this many blocks, to the belfry's floor at layer 36
FLOOR = 36

# the cells of the stair, round the inside of the wall (the interior is x, z in 3..9): west side northwards, north side
# eastwards, east side southwards, south side westwards. A step takes one cell and rises one block.
RING = ([(3, z) for z in range(9, 2, -1)] + [(x, 3) for x in range(4, 10)] + [(9, z) for z in range(4, 10)] + [(x, 9) for x in range(8, 3, -1)])
assert len(RING) == 24


def wall_block(x, y, z):
    n = noise(x, y, z, 77)
    if y <= 9:
        return 'minecraft:deepslate_bricks' if n < 0.8 else 'minecraft:cracked_deepslate_bricks'
    if n < 0.12:
        return 'minecraft:cracked_stone_bricks'
    if n < 0.2 and y < 18:
        return 'minecraft:mossy_stone_bricks'
    return 'minecraft:stone_bricks'


def facing_of(a, b):
    dx, dz = b[0] - a[0], b[1] - a[1]
    return 'east' if dx > 0 else ('west' if dx < 0 else ('south' if dz > 0 else 'north'))


def dial(dx, dy):
    """What a dial is made of at (dx, dy) from its middle, or None outside it."""
    d = math.hypot(dx, dy)
    if d > 3.8:
        return None
    if d > 3.0:
        return COPPER
    for hour in range(12):
        a = math.radians(30 * hour)
        if (round(2.5 * math.sin(a)), round(2.5 * math.cos(a))) == (dx, dy):
            return 'minecraft:ochre_froglight[axis=y]' if hour % 3 == 0 else COPPER_OLD
    if (dx, dy) == (0, 0):
        return 'minecraft:gold_block'
    if dx == 0 and dy in (1, 2):
        return COPPER_OLD                                          # the minute hand, at twelve
    a = math.radians(150)                                          # the hour hand, at five
    for t in (1, 2):
        if (math.floor(t * 1.4 * math.sin(a) + 0.5), math.floor(t * 1.4 * math.cos(a) + 0.5)) == (dx, dy):
            return COPPER_OLD
    return 'minecraft:polished_blackstone'


def tower():
    b = Build('clock_tower', W, H, W)
    c = CENTRE
    # clear the market where it will stand: the well, its roof and fences, whatever was set down there (thirteen square, six high)
    b.fill(0, 1, 0, W - 1, 8, W - 1, AIR)
    # the floor
    for x in range(1, 12):
        for z in range(1, 12):
            b.set(x, 0, z, brick(x, 0, z) if noise(x, 0, z, 5) < 0.8 else 'minecraft:cobblestone')
    # the plinth, three blocks high, with a cap of stairs sloping away; the porch before the door is left open
    for x in range(1, 12):
        for z in range(1, 12):
            if x in (1, 11) or z in (1, 11):
                porch = z == 11 and 5 <= x <= 7
                for y in range(1, 4):
                    b.set(x, y, z, AIR if porch else wall_block(x, y, z))
                inward = 'north' if z == 11 else ('south' if z == 1 else ('west' if x == 11 else 'east'))
                b.set(x, 4, z, AIR if porch else stairs('stone_brick', inward))
    # the shaft: a ring of wall, nine square, up to the belfry's floor; corners and bands in darker stone
    for y in range(1, FLOOR + 1):
        for x in range(2, 11):
            for z in range(2, 11):
                if x in (2, 10) or z in (2, 10):
                    corner = x in (2, 10) and z in (2, 10)
                    band = y in (9, 19, 35, 36)
                    if band:
                        b.set(x, y, z, 'minecraft:polished_blackstone_bricks' if not corner else 'minecraft:chiseled_stone_bricks')
                    elif corner:
                        b.set(x, y, z, 'minecraft:deepslate_bricks')
                    else:
                        b.set(x, y, z, wall_block(x, y, z))
                elif y < FLOOR:
                    b.set(x, y, z, AIR)
    # a door in the south face, three high, and slits of bars in every face between
    b.fill(c, 1, 10, c, 3, 10, AIR)
    b.set(c - 1, 3, 10, stairs('stone_brick', 'east', 'top'))
    b.set(c + 1, 3, 10, stairs('stone_brick', 'west', 'top'))
    b.fill(c - 1, 1, 10, c - 1, 2, 10, 'minecraft:chiseled_stone_bricks')
    b.fill(c + 1, 1, 10, c + 1, 2, 10, 'minecraft:chiseled_stone_bricks')
    b.set(c - 2, 4, 11, 'minecraft:stone_brick_wall')
    b.set(c + 2, 4, 11, 'minecraft:stone_brick_wall')
    b.set(c - 2, 5, 11, LANTERN_UP)
    b.set(c + 2, 5, 11, LANTERN_UP)
    for y0 in (6, 14, 22):
        for y in (y0, y0 + 1):
            b.set(c, y, 2, BARS)
            b.set(c, y, 10, BARS)
            b.set(2, y, c, BARS)
            b.set(10, y, c, BARS)
    # the floor of the ground room: planks; a lantern on a post by the stair's foot
    for x in range(3, 10):
        for z in range(3, 10):
            b.set(x, 0, z, 'minecraft:dark_oak_planks' if noise(x, 0, z, 9) < 0.8 else 'minecraft:spruce_planks')
    # the stair, and a rail on its inner side, a lantern on every fourth post
    for k in range(STEPS):
        cell = RING[k % 24]
        nxt = RING[(k + 1) % 24]
        b.set(cell[0], 1 + k, cell[1], stairs('stone_brick', facing_of(cell, nxt)))
        # inward neighbours: for a side cell the one towards the middle; for a corner the diagonal one
        ix = 4 if cell[0] == 3 else (8 if cell[0] == 9 else cell[0])
        iz = 4 if cell[1] == 3 else (8 if cell[1] == 9 else cell[1])
        b.set(ix, 2 + k, iz, FENCE)
        if k % 4 == 1:
            b.set(ix, 3 + k, iz, LANTERN_UP)
    # the belfry's floor: the whole inside but where the last three steps climb
    hole = {RING[9], RING[10], RING[11]}
    for x in range(3, 10):
        for z in range(3, 10):
            if (x, z) not in hole:
                b.set(x, FLOOR, z, 'minecraft:polished_deepslate' if (x + z) % 2 else 'minecraft:deepslate_tiles')
    # the pendulum: a chain from the floor of the belfry down the middle, and a bob of copper
    b.fill(c, 18, c, c, FLOOR - 1, c, CHAIN)
    for dx in range(-1, 2):
        for dz in range(-1, 2):
            b.set(c + dx, 16, c + dz, COPPER)
            b.set(c + dx, 17, c + dz, COPPER_OLD if dx or dz else 'minecraft:ochre_froglight[axis=y]')
    b.set(c, 15, c, 'minecraft:waxed_oxidized_cut_copper')
    # the four dials, at layers 26 to 34: the south one is read from the south, and so on
    for dx in range(-4, 5):
        for dy in range(-4, 5):
            state = dial(dx, dy)
            if state is None:
                continue
            y = 30 + dy
            b.set(c + dx, y, 10, state)                                # south face, read left to right
            b.set(c - dx, y, 2, state)                                 # north face, read from the north
            b.set(2, y, c + dx, state)                                 # west face
            b.set(10, y, c - dx, state)                                # east face
    # the belfry, layers 37 to 44: arches five wide and six tall on the four faces, a low wall across each
    for y in range(FLOOR + 1, FLOOR + 9):
        for x in range(2, 11):
            for z in range(2, 11):
                if x in (2, 10) or z in (2, 10):
                    span = z if x in (2, 10) else x
                    opening = 4 <= span <= 8 and y <= FLOOR + 6
                    if not opening:
                        b.set(x, y, z, wall_block(x, y, z) if y < FLOOR + 8 else 'minecraft:chiseled_stone_bricks')
                    elif y == FLOOR + 1:
                        b.set(x, y, z, 'minecraft:stone_brick_wall')
    # the great bell: hung from the roof by a chain, a crown, a shoulder, a body with its mouth down, and a lip round
    # the mouth; its clapper is a block of copper, and the rope, which is the mod's own (TowerEndBlock), hangs from it
    top = FLOOR + 9
    b.set(c, top - 1, c, CHAIN)
    b.set(c, top - 2, c, BELL)
    for dx in range(-1, 2):
        for dz in range(-1, 2):
            b.set(c + dx, top - 3, c + dz, BELL)
            if dx or dz:
                b.set(c + dx, top - 4, c + dz, 'minecraft:waxed_exposed_copper')
    b.set(c, top - 4, c, COPPER)
    for dx in range(-2, 3):
        for dz in range(-2, 3):
            if max(abs(dx), abs(dz)) == 2:
                b.set(c + dx, top - 5, c + dz, COPPER_OLD)
    for y in range(FLOOR + 2, FLOOR + 5):
        b.set(c, y, c, f'{MOD}:tower_end[kind=rope]')
    # the Founder's seat, against the west wall: a back of stone, arms, and the seat itself
    for y in range(FLOOR + 1, FLOOR + 5):
        for z in range(c - 1, c + 2):
            b.set(3, y, z, 'minecraft:polished_blackstone_bricks' if y < FLOOR + 4 else 'minecraft:chiseled_stone_bricks')
    b.set(3, FLOOR + 3, c, 'minecraft:gold_block')
    b.set(4, FLOOR + 1, c - 1, 'minecraft:polished_blackstone_slab[type=bottom]')
    b.set(4, FLOOR + 1, c + 1, 'minecraft:polished_blackstone_slab[type=bottom]')
    b.set(4, FLOOR + 1, c, f'{MOD}:tower_end[kind=seat]')
    # lanterns hung from the roof at the four corners
    for x, z in ((4, 4), (8, 4), (4, 8), (8, 8)):
        b.set(x, top - 1, z, LANTERN_DOWN)
    # the roof: a pyramid of dark tiles, a stair round each layer sloping up to the middle
    for i in range(6):
        lo, hi = 1 + i, 11 - i
        y = top + i
        for x in range(lo, hi + 1):
            for z in range(lo, hi + 1):
                if x in (lo, hi) or z in (lo, hi):
                    inward = 'north' if z == hi else ('south' if z == lo else ('west' if x == hi else 'east'))
                    b.set(x, y, z, f'minecraft:deepslate_tile_stairs[facing={inward},half=bottom]' if i < 5 else 'minecraft:deepslate_tiles')
                else:
                    b.set(x, y, z, 'minecraft:deepslate_tiles')
    for y in range(top + 6, top + 9):
        b.set(c, y, c, FENCE)
    b.set(c, top + 9, c, LANTERN_UP)
    return b


def main():
    out = sys.argv[1] if len(sys.argv) > 1 else None
    t = tower()
    n1 = t.save((CENTRE, FEET, CENTRE))
    print(f'clock_tower: {len(t.cells)} blocks, {n1} states')
    if out:
        import os
        os.makedirs(out, exist_ok=True)
        t.iso(scale=9).save(f'{out}/clock_tower_iso.png')
        t.top_down(FLOOR + 2).save(f'{out}/clock_tower_top.png')


if __name__ == '__main__':
    main()
