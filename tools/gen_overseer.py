#!/usr/bin/env python3
"""The Overseer of the Stockade: mesh for OverseerModel.java, the textures of both phases and their glow layers.
Run from the repo root; pass a file name to also get a preview sheet."""
import random
import sys

from modelkit import Box, Model, Painter, blotch, mix, shade, sheet

A = 'src/main/resources/assets/vsratie_dushi'
JAVA = 'src/main/java/com/keykeeper/dushi/client/OverseerModel.java'
SCALE = 1.35          # must match MODEL_SCALE in OverseerModel.java

M = Model(128, 128, SCALE)
M.part('body', None, (0, 6, 0), boxes=[Box(-8, -5, -5, 16, 7, 10, 'cloth', 'pelvis')])
M.part('apron', 'body', (0, -2, -5.2), (-0.06, 0, 0), boxes=[Box(-6, 0, -1, 12, 14, 1, 'leather', 'apron')])
M.part('chest', 'body', (0, -5, 0), (0.22, 0, 0), boxes=[Box(-10, -15, -6, 20, 15, 12, 'skin', 'torso'), Box(-8, -17, -3, 16, 2, 9, 'skin', 'hump')])
M.part('head', 'chest', (0, -15, -2), (-0.12, 0, 0), boxes=[Box(-4, -8, -4, 8, 8, 8, 'skin', 'head')])
M.part('cage', 'head', (0, 0, 0), boxes=[Box(-5.5, -10, -5.5, 11, 11, 11, 'cage', 'cage'), Box(-1.5, -12, -1.5, 3, 2, 3, 'iron', 'cage_ring')])
M.part('right_arm', 'chest', (-10, -12, 0), boxes=[Box(-7, -3, -4, 8, 6, 8, 'iron', 'pauldron'), Box(-6, 3, -3, 6, 9, 6, 'skin', 'upper_arm')])
M.part('right_forearm', 'right_arm', (-3, 12, 0), boxes=[Box(-3.5, 0, -3.5, 7, 11, 7, 'skin', 'forearm'), Box(-4, 11, -4, 8, 5, 8, 'skin', 'fist')])
M.part('key', 'right_forearm', (0, 13.5, 0), (1.5, 0, 0), boxes=[
    Box(-1, -26, -1, 2, 30, 2, 'key', 'shaft'), Box(-3, 4, -1, 6, 6, 2, 'key', 'bow'),
    Box(1, -25, -1, 6, 4, 2, 'key', 'bit'), Box(1, -19, -1, 4, 3, 2, 'key', 'bit2'), Box(-2, -28, -2, 4, 3, 4, 'key', 'cap')])
M.mirror('right_arm', 'left_arm')
M.mirror('right_forearm', 'left_forearm')
M.part('chain', 'left_forearm', (0, 15, 0), boxes=[Box(-0.5, 0, -0.5, 1, 6, 1, 'iron', 'chain')])
M.part('lantern', 'chain', (0, 6, 0), boxes=[Box(-3, 0, -3, 6, 7, 6, 'lantern', 'lantern'), Box(-2, -1, -2, 4, 1, 4, 'iron', 'lantern_cap')])
M.part('right_leg', None, (-5, 8, 0), boxes=[Box(-4, 0, -4, 8, 9, 8, 'cloth', 'thigh')])
M.part('right_shin', 'right_leg', (0, 9, 0), boxes=[Box(-3.5, 0, -3.5, 7, 7, 7, 'cloth', 'shin'), Box(-4, 4, -6, 8, 3, 10, 'boot', 'boot')])
M.mirror('right_leg', 'left_leg')
M.mirror('right_shin', 'left_shin')
M.pack()

SKIN = (150, 138, 132)
SKIN_D = (116, 104, 100)
SKIN_DD = (88, 78, 76)
SKIN_L = (172, 160, 152)
SCAR = (120, 72, 70)
LEATHER = (82, 56, 40)
LEATHER_D = (58, 40, 30)
LEATHER_L = (106, 76, 52)
STAIN = (74, 34, 30)
CLOTH = (58, 54, 62)
CLOTH_D = (42, 39, 46)
IRON = (86, 86, 94)
IRON_D = (56, 56, 64)
IRON_L = (132, 132, 140)
RUST = (126, 76, 48)
BOOT = (40, 34, 32)
KEY = (120, 104, 70)
KEY_D = (84, 70, 48)
KEY_L = (176, 154, 100)
GLASS = (236, 170, 70)
GLASS_L = (255, 226, 140)
EMBER = (255, 120, 30)
EMBER_L = (255, 210, 110)
HOT = (232, 90, 34)


def skin(face, px, py, fw, fh, rnd):
    f = {'top': 1.12, 'bottom': 0.7}.get(face, 1.06 - 0.2 * py / max(1, fh - 1))
    key = rnd.random()
    amount = blotch(f'skin{fw}{fh}{face}', px, py, 3.2)
    c = SKIN_D if amount > 0.66 else SKIN
    if key < 0.04:
        c = SKIN_L
    if min(fw, fh) > 3 and (px in (0, fw - 1) or py == fh - 1):
        c = shade(c, 0.86)
    return shade(c, f)


def iron(face, px, py, fw, fh, rnd):
    f = {'top': 1.18, 'bottom': 0.68}.get(face, 1.05 - 0.2 * py / max(1, fh - 1))
    key = rnd.random()
    c = RUST if blotch(f'iron{fw}{fh}{face}', px, py, 2.4) > 0.68 else (IRON if key > 0.2 else IRON_D)
    if min(fw, fh) > 2:
        if py == 0 and face not in ('top', 'bottom'):
            c = IRON_L
        elif px in (0, fw - 1) or py == fh - 1:
            c = shade(c, 0.74)
    return shade(c, f)


def cloth(face, px, py, fw, fh, rnd):
    f = {'top': 1.1, 'bottom': 0.7}.get(face, 1.05 - 0.2 * py / max(1, fh - 1))
    c = CLOTH_D if px % 4 == 1 and fw > 4 else CLOTH
    return shade(c, f * rnd.choice((0.97, 1.0, 1.03)))


def leather(face, px, py, fw, fh, rnd):
    f = {'top': 1.12, 'bottom': 0.7}.get(face, 1.06 - 0.16 * py / max(1, fh - 1))
    amount = blotch(f'leather{fw}{fh}{face}', px, py, 3.0)
    c = STAIN if amount > 0.7 else (LEATHER_D if amount < 0.3 else LEATHER)
    return shade(c, f)


def boot(face, px, py, fw, fh, rnd):
    return shade(BOOT, {'top': 1.25, 'bottom': 0.7}.get(face, 1.0) * rnd.choice((0.95, 1.0, 1.08)))


def key_metal(face, px, py, fw, fh, rnd):
    f = {'top': 1.15, 'bottom': 0.72}.get(face, 1.0)
    c = KEY_D if rnd.random() < 0.25 else KEY
    return shade(c, f)


MATERIALS = {'skin': skin, 'iron': iron, 'cloth': cloth, 'leather': leather, 'boot': boot, 'key': key_metal}

# the bars of the cage on an 11 x 11 face: '#' iron, '.' nothing
CAGE = ["###########",
        "#.#..#..#.#",
        "#.#..#..#.#",
        "#.#..#..#.#",
        "#.#..#..#.#",
        "###########",
        "#.#..#..#.#",
        "#.#..#..#.#",
        "#.#..#..#.#",
        "#.#..#..#.#",
        "###########"]
CAGE_TOP = ["###########",
            "#....#....#",
            "#....#....#",
            "#....#....#",
            "#...###...#",
            "#####.#####",
            "#...###...#",
            "#....#....#",
            "#....#....#",
            "#....#....#",
            "###########"]
FACE = ["ssssssss",
        "sLsssLss",
        "DDDssDDD",
        "sKEdsEKs",
        "ssdddsss",
        "sdsssdss",
        "xXxXxXxX",
        "sddsddss"]


def make(enraged):
    rnd = random.Random(21)
    p = Painter(M, 21)
    boxes = {b.tag: b for _, _, b in M.boxes()}
    by_part = lambda name: M.parts[name]['boxes']
    for _, _, b in M.boxes():
        if b.mat in MATERIALS:
            p.fill_box(b, MATERIALS[b.mat])

    def bars(face, px, py, fw, fh, r):
        rows = CAGE_TOP if face == 'top' else CAGE
        if face == 'bottom':
            return None
        if rows[py][px] != '#':
            return None
        c = IRON_L if (face != 'top' and py in (0, 5, 10) and px % 2) else IRON
        if enraged:
            # red-hot from the fire inside
            c = mix(c, HOT, 0.55 if py > 2 else 0.25)
        return c
    cage = boxes['cage']
    p.fill_box(cage, bars)
    if enraged:
        for face in ('front', 'back', 'right', 'left'):
            fx, fy, fw, fh = cage.faces()[face]
            for y in range(fh):
                for x in range(fw):
                    if CAGE[y][x] == '#' and y > 2:
                        c = p.get(cage, face, x, y)
                        p.glow.putpixel((fx + x, fy + y), shade(c, 0.8) + (255,))
    # the lock on the front of the cage
    p.art(cage, 'front', ["kkk", "kKk", "kkk"], {'k': KEY, 'K': (20, 16, 14)}, ox=4, oy=7)

    # ---- the face behind the bars: small sunken eyes, a mouth sewn shut
    head = boxes['head']
    eye = EMBER_L if enraged else (226, 214, 150)
    p.art(head, 'front', FACE, {'s': SKIN, 'L': SKIN_L, 'D': SKIN_DD, 'd': SKIN_D, 'K': (24, 18, 18), 'E': eye, 'x': SKIN_D, 'X': (60, 40, 40)},
          glow_chars='E')
    if enraged:
        # the head is burning: fire inside the cage on every side
        for face in ('front', 'back', 'right', 'left'):
            fx, fy, fw, fh = head.faces()[face]
            for x in range(fw):
                for y in range(fh):
                    if face == 'front' and 2 <= y <= 6:
                        continue
                    flame = blotch(f'fire{face}', x, y * 0.7, 1.7)
                    if flame > 0.42 and y < 7:
                        c = EMBER_L if flame > 0.7 else (EMBER if flame > 0.55 else HOT)
                        p.put(head, face, x, y, c, glow=True)
        for x in range(8):
            for z in range(8):
                c = EMBER_L if (x + z) % 3 == 0 else EMBER
                p.put(head, 'top', x, z, c, glow=True)

    # ---- torso: harness straps crossing the chest and the back, scars, ribs of fat at the belly
    torso = boxes['torso']
    for face in ('front', 'back'):
        for i in range(15):
            x = 2 + i
            for dx in (0, 1):
                p.put(torso, face, x + dx, i, LEATHER if dx == 0 else LEATHER_D)
                p.put(torso, face, 19 - x - dx, i, LEATHER if dx == 0 else LEATHER_D)
        # the iron ring where the straps cross
        p.art(torso, face, ["iii", "iKi", "iii"], {'i': IRON_L, 'K': IRON_D}, ox=8, oy=6)
        p.art(torso, face, [".i.", "iii", ".i."], {'i': IRON_L}, ox=9, oy=7)
    for x, y, n in ((4, 9, 3), (13, 11, 3), (15, 4, 2)):
        for i in range(n):
            p.put(torso, 'front', x + i, y + i, SCAR)
    for x in range(3, 17, 2):
        p.put(torso, 'front', x, 13, SKIN_D)
    for side in ('right', 'left'):
        for y in (4, 7, 10):
            for x in range(2, 10):
                p.put(torso, side, x, y, SKIN_D)
    # ---- the apron: a stitched hem and a brand of the key burned into it
    apron = boxes['apron']
    for x in range(12):
        p.put(apron, 'front', x, 13, LEATHER_D if x % 2 else LEATHER_L)
        p.put(apron, 'front', x, 0, LEATHER_L)
    for y in range(14):
        p.put(apron, 'front', 0, y, LEATHER_D)
        p.put(apron, 'front', 11, y, LEATHER_D)
    p.art(apron, 'front', [".kk.", "k..k", "k..k", ".kk.", ".k..", ".kk.", ".k..", ".kk."], {'k': (34, 24, 20)}, ox=4, oy=3)
    # ---- the belt
    pelvis = boxes['pelvis']
    p.band(pelvis, 1, LEATHER_L)
    p.band(pelvis, 2, LEATHER_D)
    p.art(pelvis, 'front', ["bbb", "b.b", "bbb"], {'b': IRON_L}, ox=7, oy=0)

    # ---- arms: shackles on the forearms with a stump of chain, knuckles on the fists
    for part in ('right_forearm', 'left_forearm'):
        forearm, fist = by_part(part)
        for row in (6, 7):
            p.band(forearm, row, IRON if row == 6 else IRON_D)
        p.band(forearm, 5, IRON_L)
        for face in ('front', 'back'):
            for x in (1, 3, 5):
                p.put(fist, face, x, 2, SKIN_DD)
                p.put(fist, face, x, 3, SKIN_D)
        p.band(fist, 0, SKIN_D)
    for part in ('right_arm', 'left_arm'):
        pauldron, upper = by_part(part)
        p.band(pauldron, 4, IRON_D)
        for face in ('front', 'back', 'right', 'left'):
            fx, fy, fw, fh = pauldron.faces()[face]
            for x in range(1, fw, 3):
                p.put(pauldron, face, x, 2, IRON_L)
        for x, y in ((1, 2), (2, 3), (3, 4)):
            p.put(upper, 'front', x, y, SCAR)
    if enraged:
        # the fire has got into him: glowing cracks down the chest and the arms
        cracks = [(torso, 'front', [(10, 0), (10, 1), (9, 2), (9, 3), (8, 4), (11, 2), (12, 3), (12, 4), (13, 5)]),
                  (torso, 'back', [(9, 0), (9, 1), (10, 2), (10, 3), (11, 4), (8, 2), (7, 3)]),
                  (by_part('right_arm')[1], 'front', [(3, 0), (3, 1), (2, 2), (2, 3)]),
                  (by_part('left_arm')[1], 'front', [(2, 0), (2, 1), (3, 2), (3, 3)]),
                  (boxes['hump'], 'front', [(6, 0), (7, 1), (8, 0), (9, 1)])]
        for box, face, pts in cracks:
            for i, (x, y) in enumerate(pts):
                p.put(box, face, x, y, EMBER_L if i % 3 == 0 else EMBER, glow=True)

    # ---- legs: patched trousers, iron-shod boots
    for part in ('right_shin', 'left_shin'):
        shin, bt = by_part(part)
        p.band(shin, 3, CLOTH_D)
        for x in range(8):
            p.put(bt, 'front', x, 2, IRON)
            p.put(bt, 'front', x, 1, IRON_L if x % 2 else IRON)
        for x in range(8):
            for z in range(0, 3):
                p.put(bt, 'top', x, 9 - z, IRON if z else IRON_L)
    for part in ('right_leg', 'left_leg'):
        thigh = by_part(part)[0]
        p.art(thigh, 'front', ["ppp", "pPp", "ppp"], {'p': (74, 66, 60), 'P': (58, 52, 48)}, ox=2 if part == 'right_leg' else 4, oy=3)

    # ---- the great key: a hole in the bow, wards cut into the bit
    bow = boxes['bow']
    for face in ('front', 'back'):
        p.art(bow, face, ["LkkkkL", "k~~~~k", "k~~~~k", "k~~~~k", "k~~~~k", "LkkkkL"], {'k': KEY, 'L': KEY_L})
    shaft = boxes['shaft']
    for face in ('right', 'front', 'left', 'back'):
        for y in range(30):
            p.put(shaft, face, 0, y, KEY_L if y % 7 == 3 else KEY)
            p.put(shaft, face, 1, y, KEY_D)
        for y in (26, 27):
            p.put(shaft, face, 0, y, LEATHER)
            p.put(shaft, face, 1, y, LEATHER_D)
    for tag in ('bit', 'bit2'):
        bit = boxes[tag]
        for face in ('front', 'back'):
            fx, fy, fw, fh = bit.faces()[face]
            for x in range(fw):
                p.put(bit, face, x, 0, KEY_L)
            for x in range(1, fw, 2):
                p.put(bit, face, x, fh - 1, KEY_D)
            p.put(bit, face, fw - 2, 1, (30, 24, 20))
    cap = boxes['cap']
    p.band(cap, 0, KEY_L)

    # ---- the lantern: iron frame, glass that glows (brighter once he is enraged)
    lantern = boxes['lantern']
    glass, glass_l = (EMBER, EMBER_L) if enraged else (GLASS, GLASS_L)

    def pane(face, px, py, fw, fh, r):
        if face in ('top', 'bottom'):
            return IRON_D
        if px in (0, fw - 1) or py in (0, fh - 1):
            return IRON
        return glass_l if (px in (2, 3) and py in (2, 3, 4)) else glass
    p.fill_box(lantern, pane)
    for face in ('right', 'front', 'left', 'back'):
        fx, fy, fw, fh = lantern.faces()[face]
        for x in range(1, fw - 1):
            for y in range(1, fh - 1):
                c = p.get(lantern, face, x, y)
                p.glow.putpixel((fx + x, fy + y), (shade(c, 1.0 if enraged else 0.8)) + (255,))
    chain = boxes['chain']
    for face in ('right', 'front', 'left', 'back'):
        for y in range(6):
            p.put(chain, face, 0, y, IRON_L if y % 2 else IRON_D)
    return p.tex, p.glow


IDLE = {'right_arm': (-0.55, 0, 0.12), 'left_arm': (0, 0, -0.12), 'right_forearm': (-1.25, 0, 0), 'left_forearm': (-1.2, 0, 0),
        'chain': (-0.22 + 1.2, 0, 0.12), 'key': (0.6, 0, 0)}
RAISE = dict(IDLE, right_arm=(-2.9, 0, 0.12), right_forearm=(-0.25, 0, 0), chest=(-0.32, 0, 0), head=(-0.25, 0, 0), left_arm=(-0.6, 0, -0.12),
             key=(-0.25, 0, 0))
STRIKE = dict(IDLE, right_arm=(-1.05, 0, 0.12), right_forearm=(-0.25, 0, 0), chest=(0.55, 0, 0), head=(0.2, 0, 0), left_arm=(0.5, 0, -0.12),
              key=(0.5, 0, 0), body={'off': (0, 2, 0)})
SWEEP = dict(IDLE, right_arm=(-1.5, -1.0, 0.12), right_forearm=(-0.15, 0, 0), chest=(0.15, -0.7, 0), key=(0.35, 0, 0), left_arm=(0, 0, -0.62))
ROAR = dict(IDLE, chest=(-0.4, 0, 0), head=(-0.5, 0, 0), right_arm=(-0.5, 0, 1.12), right_forearm=(-0.6, 0, 0), left_arm=(-0.5, 0, -1.12),
            left_forearm=(-1.6, 0, 0), key=(0, 0, 0))

if __name__ == '__main__':
    M.write_java(JAVA)
    shots = []
    for enraged in (False, True):
        tex, glow = make(enraged)
        suffix = '_enraged' if enraged else ''
        tex.save(f'{A}/textures/entity/overseer{suffix}.png')
        glow.save(f'{A}/textures/entity/overseer{suffix}_glow.png')
        views = ((-0.5, IDLE), (0.8, IDLE), (3.0, IDLE), (-0.5, RAISE), (-0.7, STRIKE), (-0.3, SWEEP)) if not enraged else \
            ((-0.5, IDLE), (0.8, ROAR), (3.0, IDLE), (-0.5, ROAR), (-0.7, STRIKE), (-1.4, IDLE))
        for yaw, pose in views:
            shots.append(M.render(tex, yaw=yaw, pose=pose, size=(330, 430), zoom=5.4, centre=(0, -12), glow=glow))
    if len(sys.argv) > 1:
        sheet(shots, 6).save(sys.argv[1])
        make(False)[0].resize((512, 512), 0).save(sys.argv[1].replace('.png', '_tex.png'))
