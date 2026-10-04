#!/usr/bin/env python3
"""The carrion hound: mesh for CarrionHoundModel.java and its texture.
Run from the repo root; pass a file name to also get a preview sheet."""
import random
import sys

from modelkit import Box, Model, Painter, blotch, mix, shade, sheet

A = 'src/main/resources/assets/vsratie_dushi'
JAVA = 'src/main/java/com/keykeeper/dushi/client/CarrionHoundModel.java'

M = Model(64, 64)
# a deep chest on thin loins: a dog that has not eaten for a long time
M.part('body', None, (0, 13, 0), boxes=[Box(-4, -4, -8, 8, 8, 8, 'hide', 'chest'), Box(-3, -3, 0, 6, 6, 9, 'hide', 'loin'),
                                        Box(-0.5, -5, -7, 1, 1, 15, 'bone', 'spine')])
M.part('head', 'body', (0, -2, -8), boxes=[Box(-3, -3, -5, 6, 6, 5, 'hide', 'skull'), Box(-1.5, -0.5, -9, 3, 2, 4, 'hide', 'snout'),
                                           Box(-3, -5, -2, 2, 2, 1, 'hide', 'ear_r'), Box(1, -5, -2, 2, 2, 1, 'hide', 'ear_l')])
M.part('jaw', 'head', (0, 1.5, -5), boxes=[Box(-1.5, 0, -4, 3, 1, 4, 'hide', 'jaw')])
M.part('tail', 'body', (0, -2, 9), (-1.0, 0, 0), boxes=[Box(-1, -1, 0, 2, 2, 8, 'hide', 'tail')])
M.part('front_right_leg', None, (-2.5, 15, -5.5), boxes=[Box(-1.5, 0, -1.5, 3, 9, 3, 'hide', 'leg')])
M.part('front_left_leg', None, (2.5, 15, -5.5), boxes=[Box(-1.5, 0, -1.5, 3, 9, 3, 'hide', 'leg')])
M.part('hind_right_leg', None, (-2.5, 15, 6.5), boxes=[Box(-1.5, 0, -1.5, 3, 9, 3, 'hide', 'leg')])
M.part('hind_left_leg', None, (2.5, 15, 6.5), boxes=[Box(-1.5, 0, -1.5, 3, 9, 3, 'hide', 'leg')])
M.pack()

HIDE = (78, 68, 62)
HIDE_D = (54, 46, 43)
HIDE_DD = (36, 30, 29)
HIDE_L = (100, 88, 78)
RAW = (120, 62, 58)
BONE = (206, 194, 166)
BONE_D = (150, 140, 118)
EMBER = (255, 150, 44)
BLACK = (20, 16, 15)


def hide(face, px, py, fw, fh, rnd):
    f = {'top': 1.1, 'bottom': 0.68}.get(face, 1.05 - 0.22 * py / max(1, fh - 1))
    amount = blotch(f'hide{fw}{fh}{face}', px, py, 2.6)
    c = HIDE_D if amount > 0.64 else (HIDE_L if amount < 0.22 else HIDE)
    if rnd.random() < 0.04:
        c = RAW                                   # mange
    if min(fw, fh) > 2 and (px in (0, fw - 1) or py == fh - 1):
        c = shade(c, 0.84)
    return shade(c, f)


def make():
    p = Painter(M, 31)
    boxes = {b.tag: b for _, _, b in M.boxes()}
    for _, _, b in M.boxes():
        p.fill_box(b, (lambda face, px, py, fw, fh, r: shade(BONE if (px + py) % 3 else BONE_D, 1.0 if face == 'top' else 0.8)) if b.mat == 'bone' else hide)
    # ribs showing through both flanks, a raw patch on the loin
    chest, loin = boxes['chest'], boxes['loin']
    for side in ('right', 'left'):
        for x in (1, 3, 5):
            for y in range(2, 7):
                p.put(chest, side, x, y, HIDE_L if y % 2 else shade(HIDE_L, 0.9))
                p.put(chest, side, x + 1, y, HIDE_DD)
        for x, y in ((2, 2), (3, 2), (3, 3), (4, 3), (6, 4)):
            p.put(loin, side, x, y, RAW)
    # the head: two embers deep in the skull, a black nose, teeth that no lip covers any more
    skull, snout, jaw = boxes['skull'], boxes['snout'], boxes['jaw']
    p.art(skull, 'front', ["dhhhhd", "hDhhDh", "KEhhEK", "hDhhDh", "hhhhhh", "dhhhhd"],
          {'h': HIDE, 'd': HIDE_D, 'D': HIDE_DD, 'K': BLACK, 'E': EMBER}, glow_chars='E')
    for x in range(3):
        p.put(snout, 'front', x, 0, BLACK)
    for side in ('right', 'left'):
        for x in range(4):
            p.put(snout, side, x, 1, BONE if x % 2 else HIDE_DD)
            p.put(jaw, side, x, 0, BONE_D if x % 2 else HIDE_DD)
    for face in ('right', 'left'):
        p.put(skull, face, 1 if face == 'right' else 3, 2, EMBER, glow=True)
    for tag in ('ear_r', 'ear_l'):
        p.put(boxes[tag], 'front', 0, 0, HIDE_DD)
        p.put(boxes[tag], 'front', 1, 1, RAW)
    # paws darker, the tail ends in bare bone
    for name in ('front_right_leg', 'front_left_leg', 'hind_right_leg', 'hind_left_leg'):
        leg = M.parts[name]['boxes'][0]
        p.band(leg, -1, HIDE_DD)
        p.band(leg, -2, HIDE_D)
        p.band(leg, 4, HIDE_D)
    tail = boxes['tail']
    for x in range(2):
        for z in range(6, 8):
            p.put(tail, 'top', x, z, BONE_D)
    return p.tex, p.glow


STAND = {}
CROUCH = {'body': {'off': (0, 3, 0), 'rot': (0.25, 0, 0)}, 'front_right_leg': (-0.9, 0, 0), 'front_left_leg': (-0.9, 0, 0),
          'hind_right_leg': {'off': (0, 2, 0), 'rot': (0.5, 0, 0)}, 'hind_left_leg': {'off': (0, 2, 0), 'rot': (0.5, 0, 0)}, 'head': (-0.3, 0, 0)}
LEAP = {'body': {'off': (0, -3, 0), 'rot': (-0.3, 0, 0)}, 'front_right_leg': {'off': (0, -5, 0), 'rot': (-1.2, 0, 0)},
        'front_left_leg': {'off': (0, -5, 0), 'rot': (-1.2, 0, 0)}, 'hind_right_leg': {'off': (0, -1, 0), 'rot': (1.1, 0, 0)},
        'hind_left_leg': {'off': (0, -1, 0), 'rot': (1.1, 0, 0)}, 'jaw': (0.7, 0, 0), 'tail': (0.9, 0, 0)}
BITE = {'head': {'off': (0, 0, -2), 'rot': (0.1, 0, 0)}, 'jaw': (0.8, 0, 0)}

if __name__ == '__main__':
    M.write_java(JAVA)
    tex, glow = make()
    tex.save(f'{A}/textures/entity/carrion_hound.png')
    if len(sys.argv) > 1:
        shots = [M.render(tex, yaw=yaw, pose=pose, size=(300, 260), zoom=9.0, centre=(0, 12), glow=glow)
                 for yaw, pose in ((-0.6, STAND), (1.2, STAND), (2.6, STAND), (-0.6, CROUCH), (-1.1, LEAP), (-0.9, BITE))]
        sheet(shots, 6).save(sys.argv[1])
