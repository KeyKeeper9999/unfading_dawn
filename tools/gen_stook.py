#!/usr/bin/env python3
"""The stook of the Stubble - sheaves stood together to dry, which have begun to walk: mesh for StookModel.java and its
texture; and the runner stone of the great mill on its beam: mesh for MillstoneModel.java and its texture.
Run from the repo root; pass a file name to also get a preview sheet."""
import math
import sys

from modelkit import Box, Model, Painter, blotch, mix, shade, sheet

A = 'src/main/resources/assets/unfading_dawn'
JAVA = 'src/main/java/com/keykeeper/dushi/client/StookModel.java'
STONE_JAVA = 'src/main/java/com/keykeeper/dushi/client/MillstoneModel.java'
SCALE = 1.25          # must match MODEL_SCALE in StookModel.java

M = Model(128, 64, SCALE)
# Four sheaves stood butt down and leaning in to one another, their heads together, and a fifth opened out and laid
# over them for a cap against the rain that will not come. Standing, it is that. Walking, the four are its legs: it
# goes on their butts, two and two, rocking. Everything hangs from 'all'.
M.part('all', None, (0, 24, 0))
M.part('body', 'all', (0, -19, 0), boxes=[Box(-3.5, 0, -3.5, 7, 13, 7, 'straw', 'core')])
M.part('sheaf_f', 'body', (0, 1, -2), (-0.27, 0, 0), boxes=[Box(-3, 0, -3, 6, 19, 5, 'straw', 'sheaf'), Box(-3.5, -2, -3.5, 7, 3, 6, 'ears', 'ears')])
M.part('sheaf_b', 'body', (0, 1, 2), (0.27, 0, 0), boxes=[Box(-3, 0, -2, 6, 19, 5, 'straw', 'sheaf'), Box(-3.5, -2, -2.5, 7, 3, 6, 'ears', 'ears')])
M.part('sheaf_r', 'body', (-2, 1, 0), (0, 0, 0.27), boxes=[Box(-3, 0, -3, 5, 19, 6, 'straw', 'sheaf'), Box(-3.5, -2, -3.5, 6, 3, 7, 'ears', 'ears')])
M.part('sheaf_l', 'body', (2, 1, 0), (0, 0, -0.27), boxes=[Box(-2, 0, -3, 5, 19, 6, 'straw', 'sheaf'), Box(-2.5, -2, -3.5, 6, 3, 7, 'ears', 'ears')])
M.part('cap', 'body', (0, 0, 0), boxes=[Box(-2.5, -6, -2.5, 5, 4, 5, 'straw', 'knot'), Box(-6, -2.5, -6, 12, 4, 12, 'thatch', 'cap'), Box(-7, 1.5, -7, 14, 2, 14, 'ears', 'eaves')])
M.pack()

STRAW = (214, 180, 88)
STRAW_D = (166, 132, 58)
STRAW_DD = (112, 86, 40)
STRAW_L = (240, 214, 130)
EAR = (228, 190, 80)
EAR_D = (176, 138, 50)
BAND = (120, 98, 62)
BLACK = (22, 16, 10)
EYE = (236, 226, 190)


def lit(face, py, fh, top=1.1, bottom=0.7, fall=0.2):
    return {'top': top, 'bottom': bottom}.get(face, 1.04 - fall * py / max(1, fh - 1))


def make():
    p = Painter(M, 601)
    front = None
    for name, _, b in M.boxes():
        if b.mat == 'straw':
            def straw(face, px, py, fw, fh, rnd, tag=b.tag, part=name):
                c = STRAW_D if px % 2 == 0 else (STRAW_L if rnd.random() < 0.18 else STRAW)
                if blotch(f'st{part}{face}', px * 2.0, py * 0.3, 1.6) > 0.7:
                    c = STRAW_DD                                 # the shadow between the stalks
                if tag == 'sheaf' and face not in ('top', 'bottom'):
                    if py in (7, 8):
                        c = BAND if px % 2 else shade(BAND, 0.8)  # the band it is bound with
                    if py >= fh - 2 and (px * 3 + len(face) + py) % 3 == 0:
                        return None                              # the butts, cut uneven
                return shade(c, lit(face, py, fh))
            p.fill_box(b, straw)
            if name == 'sheaf_f' and b.tag == 'sheaf':
                front = b
        elif b.mat == 'ears':
            def ears(face, px, py, fw, fh, rnd, tag=b.tag):
                if tag == 'eaves':
                    if face in ('top', 'bottom'):
                        if 2 <= px <= fw - 3 and 2 <= py <= fh - 3:
                            return None
                    elif py == fh - 1 and (px + len(face)) % 2 == 0:
                        return None
                elif face == 'bottom' or (face != 'top' and py == 0 and (px + len(face)) % 2 == 0):
                    return None
                c = EAR_D if (px + py) % 2 == 0 else (STRAW_L if rnd.random() < 0.2 else EAR)
                return shade(c, lit(face, py, fh, 1.12, 0.8, 0.08))
            p.fill_box(b, ears)
        elif b.mat == 'thatch':
            def thatch(face, px, py, fw, fh, rnd):
                if face == 'top':
                    cx, cy = px - (fw - 1) / 2, py - (fh - 1) / 2
                    ring = int(max(abs(cx), abs(cy)))
                    c = STRAW_D if ring % 2 == 0 else STRAW
                    return shade(c, 1.12)
                c = STRAW_D if px % 2 == 0 else STRAW
                return shade(c, lit(face, py, fh, 1.1, 0.72, 0.16))
            p.fill_box(b, thatch)
    # whatever nested in it looks out between the stalks of the sheaf it goes with first
    for x, y in ((1, 3), (4, 3)):
        p.put(front, 'front', x, y, BLACK)
        p.put(front, 'front', x, y + 1, BLACK)
    p.put(front, 'front', 1, 4, EYE)
    p.put(front, 'front', 4, 4, EYE)
    return p.tex


# ---------------------------------------------------------------------------------------------- the runner stone
S = Model(256, 64)
# A millstone on edge, as high as a man's chest, with the beam it runs on through its eye: the beam goes off to the
# stone's right, six blocks, to the post in the middle of the floor. The stone turns on the beam (see MillstoneModel).
S.part('all', None, (0, 24, 0))
S.part('beam', 'all', (0, -11, 0), boxes=[Box(-96, -1.5, -1.5, 100, 3, 3, 'beam', 'beam'), Box(4, -2, -2, 2, 4, 4, 'iron', 'pin')])
S.part('wheel', 'all', (0, -11, 0), boxes=[Box(-3, -11, -6, 6, 22, 12, 'stone', 'rim_a'), Box(-3, -6, -11, 6, 12, 22, 'stone', 'rim_b'),
                                           Box(-3.2, -9, -9, 6.4, 18, 18, 'stone', 'face'), Box(-4, -2.5, -2.5, 8, 5, 5, 'iron', 'eye')])
S.pack()

GRIT = (150, 146, 136)
GRIT_D = (108, 104, 98)
GRIT_L = (188, 184, 172)
FLOUR = (238, 234, 222)
OAK = (132, 104, 70)
OAK_D = (94, 72, 48)
OAK_L = (168, 140, 98)
IRON = (86, 84, 86)
RUST = (126, 78, 50)


def make_stone():
    p = Painter(S, 611)
    for name, _, b in S.boxes():
        if b.mat == 'stone':
            def stone(face, px, py, fw, fh, rnd, tag=b.tag):
                if tag == 'face':
                    if face not in ('right', 'left'):
                        return None
                    cx, cy = px - (fw - 1) / 2, py - (fh - 1) / 2
                    r = math.hypot(cx, cy)
                    if r > fw / 2 + 0.3:
                        return None
                    c = GRIT_D if blotch(f'sf{face}', px, py, 2.2) > 0.6 else (GRIT_L if rnd.random() < 0.1 else GRIT)
                    ang = math.atan2(cy, cx)
                    if r > 3.0 and int((ang + math.pi) / (math.pi / 4) * 2.0 + r * 0.9) % 3 == 0:
                        c = shade(c, 0.8)                        # the furrows it was dressed with, worn almost flat
                    if blotch(f'sfl{face}', px, py, 2.0) > 0.62:
                        c = mix(c, FLOUR, 0.5)
                    return shade(c, 1.02 - 0.12 * py / fh)
                c = GRIT_D if (px + py) % 4 == 0 or blotch(f'sr{tag}{face}', px, py, 2.0) > 0.62 else GRIT
                if face in ('front', 'back', 'top', 'bottom') and blotch(f'srf{tag}{face}', px, py, 1.6) > 0.7:
                    c = mix(c, FLOUR, 0.5)                       # the tread of it, white
                return shade(c, {'top': 1.12, 'bottom': 0.66}.get(face, 1.03 - 0.2 * py / max(1, fh - 1)))
            p.fill_box(b, stone)
        elif b.mat == 'beam':
            def beam(face, px, py, fw, fh, rnd):
                c = OAK_D if (px + py * 3) % 9 == 0 or blotch(f'bm{face}', px * 0.3, py, 1.5) > 0.66 else (OAK_L if rnd.random() < 0.08 else OAK)
                if face in ('front', 'back', 'top', 'bottom') and px % 24 in (0, 1):
                    c = IRON                                     # hooped with iron
                return shade(c, {'top': 1.1, 'bottom': 0.7}.get(face, 1.0))
            p.fill_box(b, beam)
        else:
            p.fill_box(b, lambda face, px, py, fw, fh, r: RUST if r.random() < 0.3 else IRON)
    return p.tex


# The poses of StookModel.setupAnim: standing; walking, rocked onto one pair of sheaves and then the other; fallen; shaking
STAND = {}
ROCK_A = {'all': (0, 0, 0.14), 'sheaf_r': (0, 0, 0.3), 'sheaf_l': (0, 0, 0.1), 'sheaf_f': (-0.35, 0, 0), 'sheaf_b': (0.2, 0, 0)}
ROCK_B = {'all': (0, 0, -0.14), 'sheaf_r': (0, 0, -0.1), 'sheaf_l': (0, 0, -0.3), 'sheaf_f': (0.2, 0, 0), 'sheaf_b': (-0.35, 0, 0)}
REARED = {'all': (-0.5, 0, 0), 'sheaf_f': (-0.5, 0, 0), 'sheaf_b': (0.3, 0, 0), 'cap': (-0.2, 0, 0)}
FALLEN = {'all': {'rot': (1.4, 0, 0), 'off': (0, -2, -4)}, 'sheaf_f': (0.15, 0, 0), 'sheaf_r': (0, 0, 0.25), 'sheaf_l': (0, 0, -0.25), 'cap': (0.2, 0, 0)}
SHAKE = {'all': (0, 0.5, 0), 'sheaf_f': (-0.45, 0, 0), 'sheaf_b': (0.45, 0, 0), 'sheaf_r': (0, 0, 0.45), 'sheaf_l': (0, 0, -0.45), 'cap': {'off': (0, -3, 0)}}

if __name__ == '__main__':
    if len(sys.argv) < 3:
        M.write_java(JAVA)
        S.write_java(STONE_JAVA)
    tex = make()
    tex.save(f'{A}/textures/entity/stook.png')
    stone_tex = make_stone()
    stone_tex.save(f'{A}/textures/entity/millstone.png')
    if len(sys.argv) > 1:
        shots = [M.render(tex, yaw=yaw, pitch=0.18, pose=pose, size=(300, 330), zoom=6.0, centre=(0, 8), background=(196, 176, 120))
                 for yaw, pose in ((-0.5, STAND), (0.0, STAND), (-0.5, ROCK_A), (-0.5, ROCK_B), (-1.3, REARED), (-1.3, FALLEN), (-0.5, SHAKE))]
        shots.append(S.render(stone_tex, yaw=-0.7, pitch=0.25, pose={}, size=(300, 330), zoom=2.4, centre=(-40, 10), background=(120, 110, 96)))
        shots.append(S.render(stone_tex, yaw=-1.2, pitch=0.2, pose={'wheel': (0.6, 0, 0)}, size=(300, 330), zoom=7.0, centre=(0, 12), background=(120, 110, 96)))
        sheet(shots, 9).save(sys.argv[1])
