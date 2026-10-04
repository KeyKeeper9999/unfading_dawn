#!/usr/bin/env python3
"""The buildings of the mod, block by block: the stockade and the outpost. Writes one plan per building into
data/vsratie_dushi/plan/ (read by world/Plan.java). Run from the repo root; pass a directory for preview pictures.

Coordinates: x grows east, z grows south, y grows up; layer 0 is the floor the feet stand on, so things stand at y = 1.
North is the top of the top-down pictures. A building is drawn facing south; the game turns it one of four ways."""
import math
import os
import random
import sys

from PIL import Image, ImageDraw

DATA = 'src/main/resources/data/vsratie_dushi'
MOD = 'vsratie_dushi'
AIR = 'minecraft:air'


def noise(x, y, z, salt=0):
    return random.Random(f'{x},{y},{z},{salt}').random()


class Build:
    def __init__(self, name, w, h, d):
        self.name, self.w, self.h, self.d = name, w, h, d
        self.cells = {}                     # (x, y, z) -> block state string, or ('chest', loot table, state)
        self.marks = []                     # (kind, x, y, z) for the report

    def inside(self, x, y, z):
        return 0 <= x < self.w and 0 <= y < self.h and 0 <= z < self.d

    def set(self, x, y, z, state):
        if not self.inside(x, y, z):
            raise ValueError(f'{self.name}: ({x}, {y}, {z}) is outside {self.w}x{self.h}x{self.d}')
        self.cells[(x, y, z)] = state

    def get(self, x, y, z):
        return self.cells.get((x, y, z))

    def fill(self, x0, y0, z0, x1, y1, z1, state):
        for x in range(min(x0, x1), max(x0, x1) + 1):
            for y in range(min(y0, y1), max(y0, y1) + 1):
                for z in range(min(z0, z1), max(z0, z1) + 1):
                    self.set(x, y, z, state(x, y, z) if callable(state) else state)

    def box(self, x0, y0, z0, x1, y1, z1, wall, inside=AIR):
        """A hollow box: walls of `wall` (a state or a function of the position), the inside filled with `inside`."""
        self.fill(x0, y0, z0, x1, y1, z1, wall)
        if x1 - x0 > 1 and z1 - z0 > 1 and inside is not None:
            self.fill(x0 + 1, y0, z0 + 1, x1 - 1, y1, z1 - 1, inside)

    def chest(self, x, y, z, loot, facing):
        self.set(x, y, z, ('chest', f'{MOD}:chests/{loot}', f'minecraft:chest[facing={facing}]'))

    def mark(self, kind, x, y, z, facing='south'):
        self.set(x, y, z, f'{MOD}:mob_mark[mob={kind},facing={facing}]')
        self.marks.append((kind, x, y, z))

    # ------------------------------------------------------------------ writing

    def join(self):
        """Bars, fences and walls get the shape their neighbours give them (the game would only work it out when a
        player places them): each joins its own kind and anything solid beside it."""
        def name(state):
            return state[2] if isinstance(state, tuple) else state

        def solid(state):
            if state is None or state == AIR or isinstance(state, tuple):
                return False
            return not any(k in state for k in THIN) and 'stairs' not in state and 'carpet' not in state and 'cauldron' not in state

        sides = (('north', 0, -1), ('south', 0, 1), ('west', -1, 0), ('east', 1, 0))
        changed = {}
        for (x, y, z), state in self.cells.items():
            if not isinstance(state, str) or '[' in state:
                continue
            kind = 'wall' if state.endswith('_wall') else ('bars' if state.endswith(('iron_bars', '_fence')) else None)
            if kind is None:
                continue
            props = []
            for side, dx, dz in sides:
                other = self.cells.get((x + dx, y, z + dz))
                joined = solid(other) or (other is not None and name(other).split('[')[0] == state)
                props.append(f'{side}={("low" if joined else "none") if kind == "wall" else str(joined).lower()}')
            if kind == 'wall':
                props.append('up=true')
            changed[(x, y, z)] = state + '[' + ','.join(sorted(props)) + ']'
        self.cells.update(changed)

    def save(self, origin):
        self.join()
        palette = {}
        order = []
        tokens = []
        run_value, run = None, 0

        def flush():
            if run:
                tokens.append(f'{run}*{run_value}' if run > 1 else str(run_value))
        for y in range(self.h):
            for z in range(self.d):
                for x in range(self.w):
                    state = self.cells.get((x, y, z))
                    if state is None:
                        value = '_'
                    else:
                        if state not in palette:
                            palette[state] = len(order)
                            order.append(state)
                        value = palette[state]
                    if value == run_value:
                        run += 1
                    else:
                        flush()
                        run_value, run = value, 1
        flush()
        os.makedirs(f'{DATA}/plan', exist_ok=True)
        with open(f'{DATA}/plan/{self.name}.txt', 'w') as f:
            f.write(f'size {self.w} {self.h} {self.d}\n')
            f.write('origin {} {} {}\n'.format(*origin))
            f.write(f'palette {len(order)}\n')
            for state in order:
                f.write((f'chest {state[1]} {state[2]}' if isinstance(state, tuple) else state) + '\n')
            for i in range(0, len(tokens), 40):
                f.write(' '.join(tokens[i:i + 40]) + '\n')
        return len(order)

    # ------------------------------------------------------------------ pictures

    def colour(self, state):
        if isinstance(state, tuple):
            return (168, 116, 44)
        name = state.split('[')[0].split(':')[1]
        for key, c in COLOURS:
            if key in name:
                return c
        return (200, 60, 200)

    def top_down(self, top, scale=10):
        """What is seen from above when everything above layer `top` is taken away."""
        img = Image.new('RGB', (self.w * scale, self.d * scale), (40, 44, 52))
        draw = ImageDraw.Draw(img)
        for x in range(self.w):
            for z in range(self.d):
                for y in range(min(top, self.h - 1), -1, -1):
                    state = self.cells.get((x, y, z))
                    if state is None or state == AIR:
                        continue
                    c = self.colour(state)
                    k = 0.55 + 0.45 * (y + 1) / (top + 1)
                    draw.rectangle([x * scale, z * scale, (x + 1) * scale - 1, (z + 1) * scale - 1], fill=tuple(int(v * k) for v in c))
                    if isinstance(state, str) and 'mob_mark' in state:
                        kind = state.split('mob=')[1].split(',')[0]
                        draw.text((x * scale + 1, z * scale - 1), kind[0].upper() if kind not in ('knight', 'keykeeper') else kind[:2], fill=(255, 255, 255))
                    break
        return img

    def iso(self, top=None, scale=7):
        """A view from the south-east and above; `top` cuts everything above that layer off."""
        top = self.h - 1 if top is None else top
        solid = {}
        for (x, y, z), state in self.cells.items():
            if state != AIR and y <= top:
                solid[(x, y, z)] = self.colour(state), (isinstance(state, str) and any(k in state for k in THIN))
        span = self.w + self.d
        img = Image.new('RGB', (int(span * scale * 0.9) + 40, int(span * scale * 0.5 + self.h * scale) + 60), (118, 142, 172))
        draw = ImageDraw.Draw(img)
        ox, oy = int(self.d * scale * 0.87) + 20, int(self.h * scale) + 30

        def project(x, y, z):
            return ox + (x - z) * scale * 0.87, oy + (x + z) * scale * 0.5 - y * scale
        for key in sorted(solid, key=lambda k: (k[0] + k[2], k[1])):
            x, y, z = key
            c, thin = solid[key]
            shrink = 0.3 if thin else 0.0
            x0, x1, z0, z1 = x + shrink, x + 1 - shrink, z + shrink, z + 1 - shrink
            y1 = y + 1
            if (x, y + 1, z) not in solid or thin:
                draw.polygon([project(x0, y1, z0), project(x1, y1, z0), project(x1, y1, z1), project(x0, y1, z1)], fill=c)
            if (x, y, z + 1) not in solid or thin:
                draw.polygon([project(x0, y1, z1), project(x1, y1, z1), project(x1, y, z1), project(x0, y, z1)], fill=tuple(int(v * 0.78) for v in c))
            if (x + 1, y, z) not in solid or thin:
                draw.polygon([project(x1, y1, z0), project(x1, y1, z1), project(x1, y, z1), project(x1, y, z0)], fill=tuple(int(v * 0.6) for v in c))
        return img


# what a block looks like from far away, first match wins
COLOURS = [
    ('mob_mark', (230, 60, 50)), ('hearth', (255, 150, 40)), ('fog_gate', (235, 235, 245)),
    ('mossy', (104, 122, 98)), ('cracked_stone', (112, 112, 112)), ('stone_brick', (128, 128, 128)), ('cobblestone', (116, 112, 108)),
    ('deepslate', (72, 72, 78)), ('polished_andesite', (140, 142, 142)), ('andesite', (130, 132, 132)), ('gravel', (128, 122, 120)),
    ('coarse_dirt', (120, 86, 60)), ('dirt', (130, 94, 66)), ('iron_bars', (90, 92, 98)), ('chain', (70, 72, 80)), ('lantern', (255, 214, 110)),
    ('hay', (190, 160, 50)), ('planks', (160, 128, 78)), ('oak', (140, 110, 66)), ('spruce', (104, 78, 46)), ('barrel', (128, 96, 56)),
    ('cobweb', (220, 220, 224)), ('blackstone', (44, 40, 48)), ('candle', (240, 220, 160)), ('ladder', (150, 120, 70)), ('skull', (210, 210, 200)),
    ('cauldron', (60, 60, 64)), ('glass', (150, 60, 60)), ('carpet', (120, 44, 40)), ('wool', (120, 44, 40)), ('bone', (220, 216, 200)),
    ('tuff', (108, 110, 102)), ('grass', (96, 140, 70)), ('stone', (124, 124, 124)),
]
THIN = ('iron_bars', 'chain', 'lantern', 'mob_mark', 'fence', 'ladder', 'candle', 'cobweb', 'skull', 'wall[', '_wall')


# ---------------------------------------------------------------------------------------------------- materials

def brick(x, y, z):
    """Old masonry: mostly stone bricks, some cracked, some mossy low down, a cobble here and there."""
    n = noise(x, y, z)
    if n < 0.14:
        return 'minecraft:cracked_stone_bricks'
    if n < 0.14 + (0.22 if y <= 2 else 0.07):
        return 'minecraft:mossy_stone_bricks'
    if n > 0.955:
        return 'minecraft:cobblestone'
    return 'minecraft:stone_bricks'


def yard_ground(x, y, z):
    n = noise(x, 0, z, 3)
    return 'minecraft:coarse_dirt' if n < 0.55 else ('minecraft:gravel' if n < 0.8 else 'minecraft:cobblestone')


def path_ground(x, y, z):
    n = noise(x, 0, z, 4)
    return 'minecraft:cobblestone' if n < 0.6 else ('minecraft:mossy_cobblestone' if n < 0.8 else 'minecraft:gravel')


def hall_floor(x, y, z):
    return 'minecraft:deepslate_tiles' if (x + z) % 2 else ('minecraft:cracked_deepslate_tiles' if noise(x, 0, z, 5) < 0.25 else 'minecraft:polished_deepslate')


def room_floor(x, y, z):
    return 'minecraft:stone_bricks' if noise(x, 0, z, 6) < 0.7 else 'minecraft:cobblestone'


def stairs(kind, facing, half='bottom'):
    return f'minecraft:{kind}_stairs[facing={facing},half={half}]'


LANTERN = 'minecraft:lantern[hanging=true]'
SOUL_LANTERN = 'minecraft:soul_lantern[hanging=true]'
CHAIN = 'minecraft:iron_chain[axis=y]'
BARS = 'minecraft:iron_bars'


def battlements(b, x0, z0, x1, z1, y, outer):
    """Merlons on every other block of the outer ring of a wall top."""
    for x in range(x0, x1 + 1):
        for z in range(z0, z1 + 1):
            if outer(x, z) and (x + z) % 2 == 0:
                b.set(x, y, z, brick(x, y, z))


# ---------------------------------------------------------------------------------------------------- the stockade

def stockade():
    W, H, D = 45, 15, 57
    b = Build('stockade', W, H, D)
    # everything inside the footprint is cleared, so that no hill or tree stands in the yard
    b.fill(0, 1, 0, W - 1, H - 1, D - 1, AIR)
    b.fill(0, 0, 0, W - 1, 0, D - 1, yard_ground)
    for x in list(range(0, 8)) + list(range(37, W)):           # beside the hall, outside the walls, the land stays as it is
        for z in range(0, 16):
            for y in range(H):
                del b.cells[(x, y, z)]

    # ---- the curtain wall round the yard, two blocks thick, with a walk on top
    for (x0, z0, x1, z1) in ((0, 16, 44, 17), (0, 55, 44, 56), (0, 16, 1, 56), (43, 16, 44, 56)):
        b.fill(x0, 0, z0, x1, 6, z1, brick)
    battlements(b, 0, 16, 44, 56, 7, lambda x, z: x in (0, 44) or z in (16, 56))
    # corner towers, solid, three blocks higher than the wall
    for (tx, tz) in ((0, 16), (40, 16), (0, 52), (40, 52)):
        b.fill(tx, 0, tz, tx + 4, 9, tz + 4, brick)
        battlements(b, tx, tz, tx + 4, tz + 4, 10, lambda x, z, tx=tx, tz=tz: x in (tx, tx + 4) or z in (tz, tz + 4))

    # ---- the great hall in the north: the overseer's
    b.box(8, 0, 0, 36, 10, 17, brick)
    b.fill(9, 0, 1, 35, 0, 15, hall_floor)
    b.fill(8, 0, 16, 36, 10, 17, brick)                       # the south wall is as thick as the curtain wall
    b.fill(8, 10, 0, 36, 10, 17, lambda x, y, z: 'minecraft:deepslate_bricks' if noise(x, y, z, 7) < 0.8 else 'minecraft:cracked_deepslate_bricks')
    battlements(b, 8, 0, 36, 17, 11, lambda x, z: x in (8, 36) or z in (0, 17))
    b.fill(20, 1, 16, 24, 6, 17, AIR)                         # the doorway
    b.fill(20, 1, 16, 24, 6, 16, f'{MOD}:fog_gate[axis=x]')
    b.fill(20, 0, 16, 24, 0, 17, hall_floor)
    for px in (14, 30):                                       # two rows of pillars
        for pz in (4, 8, 12):
            b.fill(px, 1, pz, px, 9, pz, 'minecraft:polished_deepslate')
            b.set(px, 1, pz, 'minecraft:chiseled_deepslate')
            b.set(px, 9, pz, 'minecraft:chiseled_deepslate')
    for (lx, lz) in ((14, 6), (30, 6), (14, 10), (30, 10), (22, 3), (22, 13)):
        b.fill(lx, 7, lz, lx, 9, lz, CHAIN)
        b.set(lx, 6, lz, LANTERN)
    # a seat at the far end, and what the overseer kept behind it
    b.fill(20, 1, 1, 24, 1, 2, 'minecraft:polished_deepslate')
    b.set(22, 2, 1, stairs('polished_deepslate', 'south'))
    b.set(21, 2, 1, 'minecraft:polished_deepslate_wall')
    b.set(23, 2, 1, 'minecraft:polished_deepslate_wall')
    b.chest(34, 1, 1, 'stockade_hall', 'south')
    b.chest(10, 1, 1, 'stockade_hall', 'south')
    for (cx, cz) in ((9, 14), (35, 14), (9, 2), (35, 2)):
        b.set(cx, 1, cz, 'minecraft:cobweb')
    b.mark('overseer', 22, 1, 6, 'south')

    # ---- the gatehouse in the south wall, with the keeper's room beside the passage
    b.fill(18, 0, 49, 26, 8, 56, brick)
    battlements(b, 18, 49, 26, 56, 9, lambda x, z: x in (18, 26) or z in (49, 56))
    b.fill(20, 1, 49, 24, 5, 56, AIR)                         # the passage
    b.fill(20, 0, 49, 24, 0, 56, path_ground)
    for x in (20, 24):                                        # the corners of the arch
        b.set(x, 5, 49, stairs('stone_brick', 'east' if x == 20 else 'west', 'top'))
        b.set(x, 5, 56, stairs('stone_brick', 'east' if x == 20 else 'west', 'top'))
    b.fill(20, 4, 54, 24, 5, 54, BARS)                        # the portcullis, drawn up
    b.set(22, 5, 51, LANTERN)
    # the keeper's room: a hearth, the old man, a chest for whoever comes with nothing
    b.fill(8, 0, 49, 17, 5, 55, brick)
    b.fill(9, 1, 50, 17, 4, 54, AIR)
    b.fill(9, 0, 50, 17, 0, 54, room_floor)
    b.fill(18, 1, 51, 19, 3, 52, AIR)                         # the door into the passage
    b.fill(18, 0, 51, 19, 0, 52, room_floor)
    b.set(13, 1, 52, f'{MOD}:hearth[lit=false]')
    b.mark('keykeeper', 10, 1, 53, 'east')
    b.chest(16, 1, 50, 'stockade_gate', 'south')
    b.set(9, 1, 50, 'minecraft:hay_block[axis=y]')
    b.set(10, 1, 50, 'minecraft:hay_block[axis=x]')
    b.set(9, 2, 50, 'minecraft:hay_block[axis=z]')
    b.set(12, 4, 53, LANTERN)
    b.set(16, 1, 54, 'minecraft:barrel[facing=up]')

    # ---- the path from the gate to the hall
    b.fill(21, 0, 18, 23, 0, 48, path_ground)

    # ---- the cell block along the west wall
    b.box(2, 0, 20, 13, 5, 44, brick)
    b.fill(3, 0, 21, 12, 0, 43, room_floor)
    b.fill(2, 5, 20, 13, 5, 44, brick)                        # a flat roof to walk on
    b.fill(13, 1, 31, 13, 3, 32, AIR)                         # the door from the yard
    b.fill(13, 0, 31, 13, 0, 32, room_floor)
    cells = ((21, 24), (26, 29), (31, 34), (36, 39), (41, 43))
    for i, (z0, z1) in enumerate(cells):
        if i:
            b.fill(3, 1, z0 - 1, 8, 4, z0 - 1, brick)         # the wall between two cells
        b.fill(8, 1, z0, 8, 4, z1, BARS)
        b.fill(8, 1, z0, 8, 2, z0, AIR)                       # the cell door, long since rusted off
        b.set(3, 1, z1, 'minecraft:hay_block[axis=x]')
    b.mark('prisoner', 5, 1, 22, 'east')
    b.mark('prisoner', 5, 1, 32, 'east')
    b.mark('prisoner', 6, 1, 38, 'east')
    b.mark('guard', 10, 1, 26, 'south')
    b.set(4, 1, 27, 'minecraft:skeleton_skull[rotation=6]')
    b.set(3, 2, 26, 'minecraft:cobweb')
    b.set(5, 4, 28, CHAIN)
    b.set(5, 3, 28, CHAIN)
    b.chest(3, 1, 42, 'stockade_cell', 'east')
    for lz in (23, 33, 42):
        b.set(10, 4, lz, LANTERN)
    # stairs up to the roof along the east side, and what is kept up there
    for i in range(5):
        b.fill(14, 1, 43 - i, 14, i, 43 - i, brick) if i else None
        b.set(14, 1 + i, 43 - i, stairs('stone_brick', 'north'))
    b.fill(14, 1, 38, 14, 5, 38, brick)
    b.mark('archer', 6, 6, 24, 'east')
    b.mark('archer', 9, 6, 40, 'east')
    b.chest(3, 6, 21, 'stockade_treasure', 'east')
    for x in range(2, 14):                                    # a low parapet on the yard side only at the far end
        if x % 2 == 0:
            b.set(x, 6, 20, brick(x, 6, 20))

    # ---- the chapel along the east wall: the chaplain's
    b.box(31, 0, 22, 42, 9, 46, brick)
    b.fill(32, 0, 23, 41, 0, 45, lambda x, y, z: 'minecraft:polished_andesite' if (x + z) % 2 else 'minecraft:stone_bricks')
    b.fill(31, 9, 22, 42, 9, 46, lambda x, y, z: 'minecraft:deepslate_bricks' if noise(x, y, z, 8) < 0.85 else 'minecraft:cracked_deepslate_bricks')
    b.fill(33, 10, 24, 40, 10, 44, 'minecraft:deepslate_bricks')      # a stepped roof
    b.fill(35, 11, 26, 38, 11, 42, 'minecraft:deepslate_tiles')
    b.fill(31, 1, 33, 31, 4, 35, f'{MOD}:fog_gate[axis=z]')           # the door from the yard
    b.fill(31, 0, 33, 31, 0, 35, 'minecraft:stone_bricks')
    for wz in (26, 30, 38, 42):                               # narrow barred windows high in the walls
        b.fill(42, 4, wz, 42, 6, wz, BARS)
        b.fill(31, 5, wz, 31, 6, wz, BARS)
    # the dais and the altar at the north end
    b.fill(32, 1, 23, 41, 1, 26, 'minecraft:polished_blackstone_bricks')
    b.fill(35, 2, 23, 38, 2, 23, 'minecraft:polished_blackstone')
    b.set(35, 3, 23, 'minecraft:candle[candles=3,lit=true]')
    b.set(38, 3, 23, 'minecraft:candle[candles=3,lit=true]')
    for x in range(32, 42):
        b.set(x, 1, 27, stairs('polished_blackstone_brick', 'north'))
    b.fill(36, 1, 28, 37, 1, 44, 'minecraft:red_carpet')
    for pz in range(31, 44, 3):                               # pews
        for px in (33, 34, 39, 40):
            b.set(px, 1, pz, stairs('spruce', 'south'))
    for (lx, lz) in ((34, 30), (39, 30), (34, 38), (39, 38), (36, 25)):
        b.fill(lx, 7, lz, lx, 8, lz, CHAIN)
        b.set(lx, 6, lz, SOUL_LANTERN)
    b.chest(41, 2, 23, 'stockade_chapel', 'west')
    b.mark('chaplain', 36, 2, 25, 'south')

    # ---- the yard: the gallows, a dry well, and those who walk it
    for gx in (16, 19):
        b.fill(gx, 1, 36, gx, 4, 36, 'minecraft:spruce_fence')
    b.fill(16, 5, 36, 19, 5, 36, 'minecraft:spruce_log[axis=x]')
    b.set(17, 4, 36, CHAIN)
    b.set(18, 4, 36, CHAIN)
    b.set(18, 3, 36, CHAIN)
    b.fill(15, 0, 35, 20, 0, 37, 'minecraft:spruce_planks')
    for (wx, wz) in ((26, 39), (27, 39), (28, 39), (26, 41), (27, 41), (28, 41), (26, 40), (28, 40)):
        b.set(wx, 1, wz, 'minecraft:cobblestone_wall')
    b.set(27, 1, 40, 'minecraft:cauldron')
    b.set(27, 0, 40, 'minecraft:cobblestone')
    for (bx, bz) in ((28, 19), (29, 19), (29, 20), (15, 19), (39, 48), (38, 48)):
        b.set(bx, 1, bz, 'minecraft:barrel[facing=up]')
    b.set(29, 2, 19, 'minecraft:barrel[facing=up]')
    b.mark('guard', 22, 1, 45, 'south')
    b.mark('prisoner', 17, 1, 40, 'south')
    b.mark('guard', 23, 1, 30, 'south')
    b.mark('prisoner', 27, 1, 27, 'south')
    b.mark('knight', 22, 1, 21, 'south')
    return b, (22, 1, 28)


# ---------------------------------------------------------------------------------------------------- the outpost

def outpost():
    W, H, D = 13, 13, 16
    b = Build('outpost', W, H, D)
    b.fill(0, 1, 0, W - 1, H - 1, D - 1, AIR)
    b.fill(0, 0, 0, W - 1, 0, D - 1, lambda x, y, z: yard_ground(x, y, z) if noise(x, 0, z, 9) < 0.8 else 'minecraft:grass_block')
    # a square watchtower, its top long since fallen in
    b.box(3, 0, 5, 9, 10, 11, brick)
    b.fill(4, 0, 6, 8, 0, 10, room_floor)
    for x in range(3, 10):
        for z in range(5, 12):
            if x in (3, 9) or z in (5, 11):
                ruin = 8 + int(noise(x, 0, z, 10) * 3.4)       # the height at which this stretch of wall ends now
                for y in range(ruin, 11):
                    b.set(x, y, z, AIR)
    b.fill(6, 1, 11, 6, 2, 11, AIR)                           # the door
    b.fill(4, 5, 6, 8, 5, 10, 'minecraft:spruce_planks')      # the upper floor
    b.fill(8, 1, 6, 8, 5, 6, 'minecraft:ladder[facing=south]')
    b.set(8, 5, 6, 'minecraft:ladder[facing=south]')
    for (wx, wz) in ((6, 5), (3, 8), (9, 8)):                 # arrow slits on the upper floor
        b.fill(wx, 7, wz, wx, 7, wz, AIR)
    b.set(5, 1, 6, 'minecraft:hay_block[axis=x]')
    b.set(4, 1, 6, 'minecraft:hay_block[axis=y]')
    b.set(4, 4, 8, LANTERN)
    b.chest(4, 1, 10, 'outpost', 'east')
    b.mark('guard', 6, 1, 8, 'south')
    b.mark('archer', 5, 6, 8, 'south')
    b.set(7, 6, 10, 'minecraft:barrel[facing=up]')
    # behind the tower, out of sight of it: a hearth among what is left of a wall
    b.set(6, 1, 2, f'{MOD}:hearth[lit=false]')
    for (x, z, top) in ((3, 1, 3), (3, 2, 2), (3, 3, 1), (4, 0, 2), (5, 0, 1), (9, 2, 2), (9, 1, 1), (8, 0, 1)):
        b.fill(x, 1, z, x, top, z, brick)
    b.set(8, 1, 3, 'minecraft:spruce_log[axis=x]')
    # two of the dead stand watch in front
    b.mark('prisoner', 4, 1, 14, 'south')
    b.mark('prisoner', 9, 1, 13, 'south')
    b.set(10, 1, 14, 'minecraft:skeleton_skull[rotation=3]')
    return b, (6, 1, 8)


BUILDINGS = (stockade, outpost)

if __name__ == '__main__':
    out = sys.argv[1] if len(sys.argv) > 1 else None
    for make in BUILDINGS:
        b, origin = make()
        states = b.save(origin)
        kinds = {}
        for kind, *_ in b.marks:
            kinds[kind] = kinds.get(kind, 0) + 1
        print(f'{b.name}: {b.w}x{b.h}x{b.d}, {states} block states, marks {kinds}')
        if out:
            os.makedirs(out, exist_ok=True)
            b.iso().save(f'{out}/{b.name}_iso.png')
            b.iso(top=4).save(f'{out}/{b.name}_iso_cut.png')
            b.top_down(4).save(f'{out}/{b.name}_floor.png')
            b.top_down(b.h - 1).save(f'{out}/{b.name}_roof.png')
