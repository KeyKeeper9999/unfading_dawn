#!/usr/bin/env python3
"""The blind keykeeper: mesh extras for KeykeeperModel.java and the texture.
Run from the repo root; pass a file name to also get a preview sheet."""
import random
import sys

from humanoid import box_of, humanoid
from modelkit import Box, Painter, shade, sheet

A = 'src/main/resources/assets/vsratie_dushi'
JAVA = 'src/main/java/com/keykeeper/dushi/client/KeykeeperModel.java'

M = humanoid(64)
M.part('beard', 'head', (0, 0, 0), boxes=[Box(-2.5, -1.5, -4.7, 5, 8, 1, 'beard', 'beard')])
M.part('pendant', 'body', (0, 0, 0), boxes=[Box(-1, 3, -2.9, 2, 6, 1, 'key', 'pendant')])
M.part('key_ring', 'body', (3.2, 9.5, -2.3), boxes=[Box(-1.5, 0, -0.5, 3, 3, 1, 'key', 'ring'), Box(-1.5, 3, -0.5, 1, 4, 1, 'key', 'key'),
                                                    Box(-0.3, 3, -0.5, 1, 5, 1, 'key', 'key'), Box(0.9, 3, -0.5, 1, 3, 1, 'key', 'key')])
M.pack()

ROBE = (72, 76, 88)
ROBE_D = (52, 55, 66)
ROBE_L = (92, 97, 110)
SKIN = (178, 150, 126)
SKIN_D = (142, 116, 98)
BAND = (206, 198, 180)
BAND_D = (164, 156, 140)
BEARD = (196, 194, 186)
BEARD_D = (150, 148, 144)
ROPE = (112, 88, 54)
BRASS = (196, 160, 72)
BRASS_D = (138, 106, 44)
BRASS_L = (236, 208, 120)
IRON = (120, 120, 126)

FACE = ["rrrrrrrr",
        "rssssssr",
        "rsddddsr",
        "bbBbbBbb",
        "bBbbbbBb",
        "sssddsss",
        "swwwwwws",
        "swwwwwws"]
FACE_PAL = {'r': ROBE_D, 's': SKIN, 'd': SKIN_D, 'b': BAND, 'B': BAND_D, 'w': BEARD}


def robe(face, px, py, fw, fh, rnd):
    f = {'top': 1.12, 'bottom': 0.7}.get(face, 1.06 - 0.2 * py / max(1, fh - 1))
    c = ROBE_D if (px in (1, fw - 2) and py > 1 and fw > 3) else ROBE
    return shade(c, f * rnd.choice((0.97, 1.0, 1.0, 1.03)))


def make():
    rnd = random.Random(11)
    p = Painter(M, 11)
    for name in ('head', 'body', 'right_arm', 'left_arm', 'right_leg', 'left_leg'):
        p.fill_box(box_of(M, name), robe)
    head = box_of(M, 'head')
    p.art(head, 'front', FACE, FACE_PAL)
    # the blindfold runs all the way round, knotted at the back; ears of skin below it
    for face in ('right', 'left', 'back'):
        for x in range(8):
            p.put(head, face, x, 3, BAND if x % 3 else BAND_D)
            p.put(head, face, x, 4, BAND_D if x % 3 == 1 else BAND)
    p.put(head, 'back', 3, 5, BAND)
    p.put(head, 'back', 4, 5, BAND_D)
    p.put(head, 'back', 4, 6, BAND)
    # the hood: second layer of the head, open at the front
    hat = box_of(M, 'hat')

    def hood(face, px, py, fw, fh, r):
        if face == 'bottom':
            return None
        if face == 'front':
            if 1 <= px <= 6 and py >= 2:
                return shade(ROBE_D, 0.6) if py == 2 else None
            return ROBE_L if py == 0 else ROBE
        if face == 'top':
            return ROBE_L if 2 <= px <= 5 and 2 <= py <= 5 else ROBE
        if py == fh - 1 and px % 3 == 1:
            return None
        return shade(ROBE_D if px in (2, 5) and py > 1 else ROBE, 1.06 - 0.2 * py / fh)
    p.fill_box(hat, hood)
    # hands, and bare feet sticking out of the robe
    for name in ('right_arm', 'left_arm'):
        b = box_of(M, name)
        for face in ('right', 'front', 'left', 'back'):
            fx, fy, fw, fh = b.faces()[face]
            for x in range(fw):
                p.put(b, face, x, 9, ROBE_L)
                for y in (10, 11):
                    p.put(b, face, x, y, SKIN if (x + y) % 3 else SKIN_D)
        for x in range(4):
            for z in range(4):
                p.put(b, 'bottom', x, z, SKIN_D)
    for name in ('right_leg', 'left_leg'):
        b = box_of(M, name)
        for face in ('right', 'front', 'left', 'back'):
            fx, fy, fw, fh = b.faces()[face]
            for x in range(fw):
                if (x + (name == 'left_leg')) % 2:
                    p.put(b, face, x, 9, ROBE_D)      # the torn hem
                else:
                    p.put(b, face, x, 9, ROBE)
                for y in (10, 11):
                    p.put(b, face, x, y, SKIN if (x + y) % 3 else SKIN_D)
        for x in range(4):
            for z in range(4):
                p.put(b, 'bottom', x, z, SKIN_D)
    body = box_of(M, 'body')
    p.band(body, 9, ROPE)
    p.band(body, 10, shade(ROPE, 0.75), faces=('front',), every=3)
    # the cord of the pendant
    for x, y in ((2, 0), (2, 1), (3, 2), (5, 0), (5, 1), (4, 2)):
        p.put(body, 'front', x, y, ROPE)

    def brass(face, px, py, fw, fh, r):
        return shade(BRASS, {'top': 1.15, 'bottom': 0.7}.get(face, 1.0))

    def white(face, px, py, fw, fh, r):
        if face in ('front', 'back'):
            if py == fh - 1 and px in (0, fw - 1):
                return None
            if py == fh - 2 and px == 0 and face == 'front':
                return BEARD_D
            return BEARD_D if (px == 2 and py > 2) or (px + py) % 4 == 0 else BEARD
        return BEARD_D
    p.fill_box(M.parts['beard']['boxes'][0], white)
    pendant = M.parts['pendant']['boxes'][0]
    p.fill_box(pendant, brass)
    # a key: a ring with a hole, a shaft, two teeth
    p.art(pendant, 'front', ["LL", "L~", "LL", "bL", "bL", "LL"], {'L': BRASS_L, 'b': BRASS_D})
    ring, *keys = M.parts['key_ring']['boxes']
    p.fill_box(ring, lambda face, px, py, fw, fh, r: IRON)
    p.art(ring, 'front', ["iii", "i~i", "iii"], {'i': IRON})
    p.art(ring, 'back', ["iii", "i~i", "iii"], {'i': IRON})
    for i, k in enumerate(keys):
        p.fill_box(k, lambda face, px, py, fw, fh, r, i=i: (BRASS, IRON, BRASS_D)[i] if py < fh - 1 else (BRASS_L, (160, 160, 166), BRASS)[i])
    return p.tex


SIT = {n: {'off': (0, 10, 0)} for n in ('head', 'body', 'right_arm', 'left_arm', 'right_leg', 'left_leg')}
SIT['right_leg'] = {'off': (0, 10, 0), 'rot': (-1.5, 0.22, 0)}
SIT['left_leg'] = {'off': (0, 10, 0), 'rot': (-1.45, -0.18, 0)}
SIT['body'] = {'off': (0, 10, 0), 'rot': (0.16, 0, 0)}
SIT['head'] = {'off': (0, 10.3, -1.6), 'rot': (0.42, 0, 0)}
SIT['right_arm'] = {'off': (0, 10, -1.2), 'rot': (-0.85, 0, -0.28)}
SIT['left_arm'] = {'off': (0, 10, -1.2), 'rot': (-0.8, 0, 0.28)}

if __name__ == '__main__':
    M.write_java(JAVA)
    tex = make()
    tex.save(f'{A}/textures/entity/keykeeper.png')
    if len(sys.argv) > 1:
        shots = [M.render(tex, yaw=yaw, pose=pose, size=(260, 330), zoom=9.5, centre=(0, 10))
                 for yaw, pose in ((-0.45, SIT), (0.9, SIT), (3.0, SIT), (-0.4, {}))]
        sheet(shots, 4).save(sys.argv[1])
