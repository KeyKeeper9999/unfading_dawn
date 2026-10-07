#!/usr/bin/env python3
"""The hearth and the fog gate: models, textures, block states, loot. Run from the repo root; pass a file name for a preview."""
import json
import os
import random
import sys

from PIL import Image

from jsonmodel import JsonModel
from modelkit import blotch, mix, shade, sheet

A = 'src/main/resources/assets/unfading_dawn'
DATA = 'src/main/resources/data/unfading_dawn'
MOD = 'unfading_dawn'

STONE = (112, 110, 108)
STONE_D = (84, 82, 82)
STONE_L = (138, 136, 132)
MOSS = (92, 108, 74)
ASH = (72, 68, 66)
ASH_L = (104, 100, 96)
IRON = (74, 74, 82)
IRON_D = (50, 50, 58)
IRON_L = (112, 112, 122)
RUST = (120, 72, 46)
COAL = (34, 30, 30)
COAL_L = (58, 52, 50)
LOG = (70, 50, 34)
LOG_D = (46, 32, 24)
EMBER = (255, 132, 36)
EMBER_L = (255, 214, 110)
EMBER_D = (196, 62, 24)


def write_json(path, data):
    os.makedirs(os.path.dirname(path), exist_ok=True)
    with open(path, 'w') as f:
        json.dump(data, f, indent=2)
        f.write('\n')


def hearth(lit):
    m = JsonModel(64, 'hearth')
    # a ring of rough stones round a bed of ash
    m.add(2, 0, 1, 14, 3, 4, 'stone', 'ring_n')
    m.add(2, 0, 12, 14, 3, 15, 'stone', 'ring_s')
    m.add(1, 0, 3, 4, 3, 13, 'stone', 'ring_w')
    m.add(12, 0, 3, 15, 3, 13, 'stone', 'ring_e')
    m.add(4, 0, 4, 12, 1, 12, 'ash', 'ash', faces=('up',))
    # an iron brazier on four legs
    for i, (x, z) in enumerate(((4, 4), (11, 4), (4, 11), (11, 11))):
        m.add(x, 1, z, x + 1, 6, z + 1, 'iron', f'leg{i}', faces=('north', 'south', 'west', 'east'))
    m.add(3, 6, 3, 13, 7, 13, 'iron', 'pan')
    m.add(3, 7, 3, 13, 10, 4, 'iron', 'wall_n')
    m.add(3, 7, 12, 13, 10, 13, 'iron', 'wall_s')
    m.add(3, 7, 4, 4, 10, 12, 'iron', 'wall_w', faces=('west', 'east', 'up'))
    m.add(12, 7, 4, 13, 10, 12, 'iron', 'wall_e', faces=('west', 'east', 'up'))
    m.add(4, 7, 4, 12, 9, 12, 'coal', 'coals', faces=('up',))
    m.add(5, 9, 7, 11, 10, 9, 'log', 'log_a')
    m.add(7, 9, 5, 9, 10, 11, 'log', 'log_b')
    if lit:
        # two crossed sheets of flame; they glow and are not shaded
        for (x0, z0, x1, z1, faces) in ((4, 8, 12, 8, ('north', 'south')), (8, 4, 8, 12, ('west', 'east'))):
            e = m.add(x0, 9, z0, x1, 17, z1, 'fire', 'fire', shade=False, light=15, faces=faces)
            for d in faces:
                e.fixed[d] = ('fire', [0, 0, 16, 16])
        m.extra_textures['fire'] = 'minecraft:block/campfire_fire'
    m.pack()

    def paint(e, d, px, py, w, h, rnd):
        if e.mat == 'stone':
            amount = blotch(f'{e.tag}{d}', px, py, 2.2)
            c = STONE_L if amount > 0.66 else (STONE_D if amount < 0.36 else STONE)
            if d != 'up' and py == h - 1 and rnd.random() < 0.5:
                c = MOSS
            if d == 'up' and rnd.random() < 0.1:
                c = ASH_L
            # joints between the stones
            if d in ('north', 'south', 'west', 'east') and px % 4 == 3 and py != 0:
                c = shade(STONE_D, 0.8)
            if d == 'up' and (px % 5 == 4):
                c = shade(STONE_D, 0.8)
            return c
        if e.mat == 'ash':
            return ASH_L if blotch('ash', px, py, 1.8) > 0.6 else ASH
        if e.mat == 'iron':
            c = RUST if blotch(f'{e.tag}{d}', px, py, 2.0) > 0.72 else IRON
            if d in ('north', 'south', 'west', 'east') and h >= 3:
                if py == 0:
                    c = IRON_L
                elif py == h - 1:
                    c = IRON_D
                elif w >= 8 and px % 3 == 1 and py == 1:
                    # slots in the wall of the brazier: the fire shows through them
                    c = EMBER if lit else COAL
            if d == 'up':
                c = IRON_L if rnd.random() < 0.3 else IRON
            if d == 'down':
                c = IRON_D
            return c
        if e.mat == 'coal':
            amount = blotch('coal', px, py, 1.5)
            if lit:
                return EMBER_L if amount > 0.72 else (EMBER if amount > 0.5 else (EMBER_D if amount > 0.3 else COAL))
            return COAL_L if amount > 0.6 else COAL
        if e.mat == 'log':
            c = LOG_D if (px + py) % 3 == 0 else LOG
            if lit and d != 'up' and py == h - 1:
                c = EMBER
            if lit and d == 'up' and rnd.random() < 0.2:
                c = EMBER_D
            if not lit and rnd.random() < 0.3:
                c = COAL_L
            return c
        return (255, 0, 255)
    m.paint(paint, seed=5)
    return m


def shard_heap():
    """A heap of ash with a shard of a flask standing in it, still glowing."""
    m = JsonModel(32, 'shard_heap')
    m.add(3, 0, 3, 13, 1, 13, 'ash', 'heap0')
    m.add(4, 1, 4, 12, 2, 12, 'ash', 'heap1')
    m.add(6, 2, 5, 11, 3, 10, 'ash', 'heap2')
    m.add(7, 2, 7, 10, 6, 9, 'shard', 'shard', shade=False, light=15)
    m.add(8, 6, 7, 10, 8, 9, 'shard', 'tip', shade=False, light=15)
    m.add(5, 2, 9, 7, 4, 11, 'shard', 'chip', shade=False, light=12)
    m.pack()

    def paint(e, d, px, py, w, h, rnd):
        if e.mat == 'ash':
            amount = blotch(f'{e.tag}{d}', px, py, 1.6)
            c = ASH_L if amount > 0.62 else (COAL_L if amount < 0.3 else ASH)
            if d == 'up' and rnd.random() < 0.08:
                c = EMBER_D
            return c
        amount = blotch(f'{e.tag}{d}', px, py, 1.3)
        c = EMBER_L if amount > 0.6 else EMBER
        if px in (0, w - 1) and h > 2:
            c = EMBER_D
        if d == 'up':
            c = (255, 240, 190)
        return c
    m.paint(paint, seed=9)
    return m


def forge():
    """The old anvil: a block of oak, an iron anvil on it, a hammer left lying across the face."""
    m = JsonModel(64, 'forge')
    m.add(3, 0, 3, 13, 6, 13, 'stump', 'stump')
    m.add(4, 6, 5, 12, 7, 11, 'iron', 'foot')
    m.add(6, 7, 6, 10, 10, 10, 'iron', 'waist')
    m.add(2, 10, 5, 14, 13, 11, 'iron', 'face')
    m.add(0, 11, 6, 2, 12.5, 10, 'iron', 'horn')
    m.add(7, 13, 3, 8, 14, 12, 'handle', 'handle')
    m.add(5.5, 13, 10, 9.5, 15, 12, 'head', 'hammer')
    m.pack()

    def paint(e, d, px, py, w, h, rnd):
        if e.mat == 'stump':
            if d in ('up', 'down'):
                ring = int(((px - w / 2 + 0.5) ** 2 + (py - h / 2 + 0.5) ** 2) ** 0.5)
                return LOG if ring % 2 else (112, 84, 54)
            return LOG_D if px % 3 == 0 else shade(LOG, 1.1 - 0.2 * py / max(1, h - 1))
        if e.mat == 'handle':
            return (128, 96, 60) if (px + py) % 2 else (104, 78, 50)
        if e.mat == 'head':
            return IRON_L if d == 'up' else (IRON if py == 0 else IRON_D)
        c = RUST if blotch(f'{e.tag}{d}', px, py, 2.4) > 0.78 else IRON
        if d == 'up':
            c = IRON_L if e.tag == 'face' and blotch('face', px, py, 1.6) > 0.35 else c
        elif d == 'down':
            c = IRON_D
        elif py == 0:
            c = IRON_L
        elif py == h - 1:
            c = IRON_D
        return c
    m.paint(paint, seed=9)
    return m


def fog_texture(frames=16, tint=(1.0, 1.0, 1.0), thick=0):
    """Slow white fog: thick in the middle, thinner towards the top, drifting sideways. One frame under another.
    `tint` colours it (the seal is amber), `thick` makes it more opaque."""
    size = 16
    img = Image.new('RGBA', (size, size * frames), (0, 0, 0, 0))
    for f in range(frames):
        phase = f / frames
        for x in range(size):
            for y in range(size):
                # two layers of noise sliding against each other; both wrap so the animation and the tiling have no seams
                def wrapped(seed, shift, cell):
                    xs = (x + shift * size) % size
                    a = blotch(seed, xs, y, cell)
                    b = blotch(seed, xs - size, y, cell)
                    t = xs / size
                    return a * (1 - t) + b * t
                n = 0.6 * wrapped('fog1', phase, 4.0) + 0.4 * wrapped('fog2', 1 - phase, 2.5)
                alpha = int(150 + 90 * n)
                light = int(214 + 40 * n)
                img.putpixel((x, f * size + y), (int(light * tint[0]), int(light * tint[1]), int(min(255, light + 6) * tint[2]), min(255, alpha + thick)))
    return img


def shallows_texture(frames=16, sea=False):
    """Still water with something growing in it: dark green, lighter where the light catches a ripple, the ripples
    drifting. It tiles and its animation closes on itself."""
    size = 16
    img = Image.new('RGBA', (size, size * frames), (0, 0, 0, 0))
    for f in range(frames):
        phase = f / frames
        for x in range(size):
            for y in range(size):
                def wrapped(seed, sx, sy, cell):
                    xs, ys = (x + sx * size) % size, (y + sy * size) % size
                    tx, ty = xs / size, ys / size
                    a = blotch(seed, xs, ys, cell) * (1 - tx) + blotch(seed, xs - size, ys, cell) * tx
                    b = blotch(seed, xs, ys - size, cell) * (1 - tx) + blotch(seed, xs - size, ys - size, cell) * tx
                    return a * (1 - ty) + b * ty
                n = 0.55 * wrapped('film1', phase, 0.0, 4.0) + 0.45 * wrapped('film2', 0.0, 1 - phase, 2.6)
                ripple = max(0.0, n - 0.58) * 3.2
                if sea:
                    foam = max(0.0, n - 0.7) * 6.0
                    c = (int(min(255, 112 + 90 * ripple + 60 * foam)), int(min(255, 150 + 70 * ripple + 50 * foam)), int(min(255, 158 + 60 * ripple + 44 * foam)))
                    img.putpixel((x, f * size + y), c + (int(min(255, 96 + 70 * ripple + 80 * foam)),))
                    continue
                c = (int(58 + 70 * ripple), int(92 + 80 * ripple), int(66 + 60 * ripple))
                img.putpixel((x, f * size + y), c + (int(150 + 60 * ripple),))
    return img


def sludge_texture(frames=8):
    """The bog's own sludge: nearly black, a green skin on it, bubbles coming up through it and bursting."""
    size = 16
    img = Image.new('RGBA', (size, size * frames), (0, 0, 0, 0))
    rnd = random.Random(77)
    bubbles = [(rnd.randrange(size), rnd.randrange(size), rnd.randrange(frames)) for _ in range(7)]
    for f in range(frames):
        for x in range(size):
            for y in range(size):
                n = 0.6 * blotch('sludge1', x, y, 3.0) + 0.4 * blotch('sludge2', x, y, 1.6)
                c = mix((30, 34, 24), (66, 74, 44), n)
                if n > 0.62:
                    c = mix(c, (96, 112, 58), (n - 0.62) * 2.2)
                edge = min(x, y, size - 1 - x, size - 1 - y)
                alpha = 255 if edge > 0 or blotch('sludge3', x, y, 1.2) > 0.4 else 0
                img.putpixel((x, f * size + y), c + (alpha,))
        for bx, by, at in bubbles:
            age = (f - at) % frames
            if age == 0:
                img.putpixel((bx, f * size + by), (120, 138, 76, 255))
            elif age == 1:
                for dx, dy in ((1, 0), (-1, 0), (0, 1), (0, -1)):
                    if 0 <= bx + dx < size and 0 <= by + dy < size:
                        img.putpixel((bx + dx, f * size + by + dy), (104, 120, 66, 255))
                img.putpixel((bx, f * size + by), (22, 26, 18, 255))
    return img


def locked_gate_texture():
    """A grate of square iron bars riveted to two flat bands; see-through between them."""
    size = 16
    img = Image.new('RGBA', (size, size), (0, 0, 0, 0))
    for y in range(size):
        for x in range(size):
            bar = x % 4 in (1, 2)
            band = y in (2, 3, 12, 13)
            if not (bar or band):
                continue
            c = IRON
            if bar and not band:
                c = IRON_L if x % 4 == 1 else IRON_D
            elif band:
                c = IRON_L if y in (2, 12) else IRON
                if bar and x % 4 == 1:
                    c = (150, 150, 160)                    # a rivet head
            if blotch('grate', x, y, 2.2) > 0.72:
                c = mix(c, RUST, 0.6)
            img.putpixel((x, y), tuple(c) + (255,))
    return img


def mob_mark_texture():
    """A broken ring of ash, see-through elsewhere."""
    size = 16
    img = Image.new('RGBA', (size, size), (0, 0, 0, 0))
    rnd = random.Random(31)
    for y in range(size):
        for x in range(size):
            d = ((x - 7.5) ** 2 + (y - 7.5) ** 2) ** 0.5
            n = blotch('mark', x, y, 2.0)
            if 4.6 <= d <= 6.9 and n > 0.22:
                c = ASH_L if n > 0.62 else ASH
                img.putpixel((x, y), tuple(c) + (235,))
            elif d < 4.6 and n > 0.72:
                img.putpixel((x, y), tuple(shade(ASH, 0.8)) + (150,))
    for (x, y) in ((3, 6), (11, 3), (12, 10), (5, 12), (8, 2)):
        img.putpixel((x, y), tuple(EMBER if rnd.random() < 0.6 else EMBER_D) + (255,))
    return img


if __name__ == '__main__':
    shots = []
    for lit in (False, True):
        m = hearth(lit)
        name = 'hearth_lit' if lit else 'hearth'
        m.save(A, f'block/{name}', f'block/{name}')
        shots.append(m.render(size=(300, 300), zoom=13))
        shots.append(m.render(size=(300, 300), zoom=13, yaw=2.4, pitch=0.5))
    write_json(f'{A}/blockstates/hearth.json', {'variants': {'lit=false': {'model': f'{MOD}:block/hearth'}, 'lit=true': {'model': f'{MOD}:block/hearth_lit'}}})
    write_json(f'{A}/models/item/hearth.json', {'parent': f'{MOD}:block/hearth_lit'})
    write_json(f'{A}/items/hearth.json', {'model': {'type': 'minecraft:model', 'model': f'{MOD}:item/hearth'}})

    fog = fog_texture()
    os.makedirs(f'{A}/textures/block', exist_ok=True)
    fog.save(f'{A}/textures/block/fog_gate.png')
    write_json(f'{A}/textures/block/fog_gate.png.mcmeta', {'animation': {'frametime': 4, 'interpolate': True}})
    face = {'uv': [0, 0, 16, 16], 'texture': '#t'}
    write_json(f'{A}/models/block/fog_gate.json', {
        'textures': {'t': f'{MOD}:block/fog_gate', 'particle': f'{MOD}:block/fog_gate'},
        'elements': [{'from': [0, 0, 7], 'to': [16, 16, 9], 'shade': False, 'light_emission': 8,
                      'faces': {'north': dict(face, cullface='north'), 'south': dict(face, cullface='south')}}]})
    seal = fog_texture(tint=(1.0, 0.78, 0.36), thick=60)
    seal.save(f'{A}/textures/block/fog_seal.png')
    write_json(f'{A}/textures/block/fog_seal.png.mcmeta', {'animation': {'frametime': 6, 'interpolate': True}})
    write_json(f'{A}/models/block/fog_seal.json', {'parent': f'{MOD}:block/fog_gate',
                                                   'textures': {'t': f'{MOD}:block/fog_seal', 'particle': f'{MOD}:block/fog_seal'}})
    write_json(f'{A}/blockstates/fog_gate.json', {'variants': {
        'axis=x,sealed=false': {'model': f'{MOD}:block/fog_gate'}, 'axis=z,sealed=false': {'model': f'{MOD}:block/fog_gate', 'y': 90},
        'axis=x,sealed=true': {'model': f'{MOD}:block/fog_seal'}, 'axis=z,sealed=true': {'model': f'{MOD}:block/fog_seal', 'y': 90}}})

    # the locked grate of a map: bars a hand's breadth thick, drawn from both sides
    grate = locked_gate_texture()
    grate.save(f'{A}/textures/block/locked_gate.png')
    write_json(f'{A}/models/block/locked_gate.json', {
        'textures': {'t': f'{MOD}:block/locked_gate', 'particle': f'{MOD}:block/locked_gate'},
        'elements': [{'from': [0, 0, 7], 'to': [16, 16, 9],
                      'faces': {'north': dict(face), 'south': dict(face), 'up': {'uv': [0, 2, 16, 4], 'texture': '#t'},
                                'down': {'uv': [0, 12, 16, 14], 'texture': '#t'}}}]})
    write_json(f'{A}/blockstates/locked_gate.json', {'variants': {
        f'axis={axis}': ({'model': f'{MOD}:block/locked_gate'} if axis == 'x' else {'model': f'{MOD}:block/locked_gate', 'y': 90})
        for axis in 'xz'}})                                      # (whatever its lock: a new gate needs nothing here)

    # a hand's depth of water over a floor (ShallowsBlock): a film as thick as two of a block's sixteen parts, murky
    # green, moving slowly. Its picture has see-through parts, which is what makes the game draw it like water.
    film = shallows_texture()
    film.save(f'{A}/textures/block/shallows.png')
    write_json(f'{A}/textures/block/shallows.png.mcmeta', {'animation': {'frametime': 5, 'interpolate': True}})
    write_json(f'{A}/models/block/shallows.json', {
        'textures': {'t': f'{MOD}:block/shallows', 'particle': f'{MOD}:block/shallows'},
        'elements': [{'from': [0, 0, 0], 'to': [16, 1.6, 16], 'shade': False,
                      'faces': {'up': {'uv': [0, 0, 16, 16], 'texture': '#t'},
                                'north': {'uv': [0, 0, 16, 1.6], 'texture': '#t', 'cullface': 'north'},
                                'south': {'uv': [0, 0, 16, 1.6], 'texture': '#t', 'cullface': 'south'},
                                'west': {'uv': [0, 0, 16, 1.6], 'texture': '#t', 'cullface': 'west'},
                                'east': {'uv': [0, 0, 16, 1.6], 'texture': '#t', 'cullface': 'east'}}}]})
    write_json(f'{A}/blockstates/shallows.json', {'variants': {'': {'model': f'{MOD}:block/shallows'}}})
    # the same film on the sands of the Morning Wharf: the sea's grey, thinner, with a little foam drifting on it
    film = shallows_texture(sea=True)
    film.save(f'{A}/textures/block/sea_shallows.png')
    write_json(f'{A}/textures/block/sea_shallows.png.mcmeta', {'animation': {'frametime': 5, 'interpolate': True}})
    model = json.load(open(f'{A}/models/block/shallows.json'))
    model['textures'] = {'t': f'{MOD}:block/sea_shallows', 'particle': f'{MOD}:block/sea_shallows'}
    # (only its top: between two cells of it the sides showed through as a grid of lines over the whole of a sand flat)
    model['elements'][0]['faces'] = {'up': model['elements'][0]['faces']['up']}
    write_json(f'{A}/models/block/sea_shallows.json', model)
    write_json(f'{A}/blockstates/sea_shallows.json', {'variants': {'': {'model': f'{MOD}:block/sea_shallows'}}})

    # a pool of sludge (SludgeBlock): thicker than the film it may lie in, so that it shows over it
    sludge = sludge_texture()
    sludge.save(f'{A}/textures/block/sludge.png')
    write_json(f'{A}/textures/block/sludge.png.mcmeta', {'animation': {'frametime': 6}})
    write_json(f'{A}/models/block/sludge.json', {
        'textures': {'t': f'{MOD}:block/sludge', 'particle': f'{MOD}:block/sludge'},
        'elements': [{'from': [0, 0, 0], 'to': [16, 2.6, 16], 'shade': False,
                      'faces': {'up': {'uv': [0, 0, 16, 16], 'texture': '#t'},
                                'north': {'uv': [0, 0, 16, 2.6], 'texture': '#t', 'cullface': 'north'},
                                'south': {'uv': [0, 0, 16, 2.6], 'texture': '#t', 'cullface': 'south'},
                                'west': {'uv': [0, 0, 16, 2.6], 'texture': '#t', 'cullface': 'west'},
                                'east': {'uv': [0, 0, 16, 2.6], 'texture': '#t', 'cullface': 'east'}}}]})
    write_json(f'{A}/blockstates/sludge.json', {'variants': {f'wet={wet}': {'model': f'{MOD}:block/sludge'} for wet in ('true', 'false')}})

    # a threshold that names a place: nothing is drawn, the game only needs to be told so
    write_json(f'{A}/models/block/place_name.json', {'textures': {'particle': 'minecraft:block/stone'}})
    write_json(f'{A}/blockstates/place_name.json', {'variants': {'': {'model': f'{MOD}:block/place_name'}}})

    heap = shard_heap()
    heap.save(A, 'block/shard_heap', 'block/shard_heap')
    shots.append(heap.render(size=(300, 300), zoom=13))
    write_json(f'{A}/blockstates/shard_heap.json', {'variants': {'': {'model': f'{MOD}:block/shard_heap'}}})

    anvil = forge()
    anvil.save(A, 'block/forge', 'block/forge')
    shots.append(anvil.render(size=(300, 300), zoom=13))
    shots.append(anvil.render(size=(300, 300), zoom=13, yaw=2.4, pitch=0.5))
    write_json(f'{A}/blockstates/forge.json', {'variants': {'': {'model': f'{MOD}:block/forge'}}})
    write_json(f'{A}/models/item/forge.json', {'parent': f'{MOD}:block/forge'})
    write_json(f'{A}/items/forge.json', {'model': {'type': 'minecraft:model', 'model': f'{MOD}:item/forge'}})
    write_json(f'{DATA}/loot_table/blocks/forge.json', {
        'type': 'minecraft:block', 'pools': [{'rolls': 1, 'entries': [{'type': 'minecraft:item', 'name': f'{MOD}:forge'}]}]})
    Image.open(f'{A}/textures/block/fog_gate.png').crop((0, 0, 16, 16)).save(f'{A}/textures/item/fog_gate.png')
    write_json(f'{A}/models/item/fog_gate.json', {'parent': 'minecraft:item/generated', 'textures': {'layer0': f'{MOD}:item/fog_gate'}})
    write_json(f'{A}/items/fog_gate.json', {'model': {'type': 'minecraft:model', 'model': f'{MOD}:item/fog_gate'}})

    # the mark where one of the dead rises: a ring of ash on the floor with a few embers still in it
    mark = mob_mark_texture()
    mark.save(f'{A}/textures/block/mob_mark.png')
    write_json(f'{A}/models/block/mob_mark.json', {
        'textures': {'t': f'{MOD}:block/mob_mark', 'particle': f'{MOD}:block/mob_mark'},
        'elements': [{'from': [0, 0.1, 0], 'to': [16, 0.1, 16], 'shade': False,
                      'faces': {'up': {'uv': [0, 0, 16, 16], 'texture': '#t'}, 'down': {'uv': [0, 16, 16, 0], 'texture': '#t'}}}]})
    write_json(f'{A}/blockstates/mob_mark.json', {'variants': {'': {'model': f'{MOD}:block/mob_mark'}}})
    mark.save(f'{A}/textures/item/mob_mark.png')
    write_json(f'{A}/models/item/mob_mark.json', {'parent': 'minecraft:item/generated', 'textures': {'layer0': f'{MOD}:item/mob_mark'}})
    write_json(f'{A}/items/mob_mark.json', {'model': {'type': 'minecraft:model', 'model': f'{MOD}:item/mob_mark'}})

    # the hearth drops itself; the fog gate and the mark drop nothing
    write_json(f'{DATA}/loot_table/blocks/hearth.json', {
        'type': 'minecraft:block', 'pools': [{'rolls': 1, 'entries': [{'type': 'minecraft:item', 'name': f'{MOD}:hearth'}]}]})
    write_json('src/main/resources/data/minecraft/tags/block/mineable/pickaxe.json', {'values': [f'{MOD}:hearth', f'{MOD}:forge']})
    if len(sys.argv) > 1:
        shots.append(fog.crop((0, 0, 16, 64)).resize((300, 1200), 0).crop((0, 0, 300, 300)))
        shots.append(mark.resize((300, 300), 0).convert('RGB'))
        sheet(shots, 5).save(sys.argv[1])
