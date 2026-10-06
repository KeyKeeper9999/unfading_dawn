#!/usr/bin/env python3
"""The sleep-moth of the Black Firwood: mesh for MothModel.java, its texture and the glow of the eyes on its wings.
Run from the repo root; pass a file name to also get a preview sheet."""
import math
import sys

from modelkit import Box, Model, Painter, blotch, mix, shade, sheet

A = 'src/main/resources/assets/vsratie_dushi'
JAVA = 'src/main/java/com/keykeeper/dushi/client/MothModel.java'
SCALE = 1.5           # must match MODEL_SCALE in MothModel.java

M = Model(64, 64, SCALE)
# A moth the size of a dog, the colour of old linen: it sits on the ground or a trunk with its wings spread and is
# taken for a rag until it goes up. On each fore wing there is an eye - a ring of dark and a pale pupil - that looks
# wide open. A furred body, a small head with two combs for feelers, four wings. Everything hangs from 'all'.
M.part('all', None, (0, 16, 0))
M.part('body', 'all', (0, 0, 0), boxes=[Box(-1.5, -1.5, -2, 3, 3, 4, 'fur', 'thorax'), Box(-1, -1, 2, 2, 2, 6, 'fur', 'tail')])
M.part('head', 'body', (0, -0.5, -2), boxes=[Box(-1.5, -1.5, -2, 3, 3, 2, 'fur', 'head')])
M.part('feeler_r', 'head', (-1, -1.5, -1.5), (-0.5, 0, -0.5), boxes=[Box(-1, -5, 0, 2, 5, 1, 'comb', 'feeler')])
M.mirror('feeler_r', 'feeler_l', parent='head')
M.part('fore_r', 'body', (-1.5, -1, -1), boxes=[Box(-11, 0, -4, 11, 1, 8, 'wing', 'fore')])
M.part('hind_r', 'body', (-1.5, -1, 2), (0, 0.25, 0), boxes=[Box(-7, 0, -1, 7, 1, 7, 'wing', 'hind')])
M.mirror('fore_r', 'fore_l', parent='body')
M.mirror('hind_r', 'hind_l', parent='body')
M.pack()

LINEN = (226, 220, 202)
LINEN_D = (192, 184, 164)
LINEN_DD = (122, 112, 98)
FUR = (150, 140, 124)
FUR_D = (104, 94, 84)
DARK = (44, 38, 40)
PUPIL = (236, 240, 220)
IRIS = (120, 150, 150)


def wing(tag, mirrored):
    def fn(face, px, py, fw, fh, rnd):
        if face not in ('top', 'bottom'):
            return shade(LINEN_DD, 0.9)
        # px runs along the wing from the body outwards on the right wing, inwards on the left: make it the distance out
        out = px if not mirrored else fw - 1 - px
        if face == 'bottom':
            out = fw - 1 - out
        out = fw - 1 - out                                     # the box's x grows towards the body on a right wing
        # the outline of a wing: the far corners are cut away
        edge = fh - 1 - py if tag == 'fore' else py
        if out >= fw - 2 and (py == 0 or py == fh - 1):
            return None
        if out == fw - 1 and (py <= 1 or py >= fh - 2):
            return None
        c = LINEN if blotch(f'w{tag}', out, py, 2.4) < 0.6 else LINEN_D
        if out >= fw - 1 or (tag == 'hind' and py >= fh - 1) or (tag == 'fore' and py == 0):
            c = LINEN_D                                         # the dusty border
        if (out + py * 2) % 5 == 0 and out > 1:
            c = mix(c, LINEN_DD, 0.22)                           # veins
        if tag == 'fore' and face == 'top':
            cx, cy = fw - 5, fh // 2
            d = math.hypot(out - cx, (py - cy) * 1.1)
            if d < 1.0:
                return PUPIL
            if d < 2.2:
                return IRIS if d < 1.7 else DARK
            if d < 3.0:
                return LINEN_DD
        return shade(c, 1.0 if face == 'top' else 0.8)
    return fn


def make():
    p = Painter(M, 161)
    for name, _, b in M.boxes():
        if b.mat == 'fur':
            p.fill_box(b, lambda face, px, py, fw, fh, r: shade(FUR_D if (px + py) % 3 == 0 or blotch(f'f{face}', px, py, 1.6) > 0.66 else FUR,
                                                                {'top': 1.12, 'bottom': 0.7}.get(face, 1.0)))
        elif b.mat == 'comb':
            def comb(face, px, py, fw, fh, rnd):
                if face not in ('front', 'back'):
                    return None
                return LINEN_DD if px == 0 or py % 2 == 0 else None
            p.fill_box(b, comb)
        elif b.mat == 'wing':
            p.fill_box(b, wing(b.tag, name.endswith('_l')))
    head = next(b for _, _, b in M.boxes() if b.tag == 'head')
    p.art(head, 'front', ["fdf", "BfB", "fdf"], {'f': FUR, 'd': FUR_D, 'B': DARK})
    # the eyes on the wings give a little light, so that in the dark of the wood they are what is seen
    for x in range(M.tex_w):
        for y in range(M.tex_h):
            c = p.tex.getpixel((x, y))
            if c[3] and c[:3] in (PUPIL, IRIS):
                p.glow.putpixel((x, y), (c[0], c[1], c[2], 255) if c[:3] == PUPIL else (60, 80, 80, 255))
    return p.tex, p.glow


SPREAD = {}
UP = {'fore_r': (0, 0, 0.75), 'fore_l': (0, 0, -0.75), 'hind_r': (0, 0, 0.6), 'hind_l': (0, 0, -0.6)}
DOWN = {'fore_r': (0, 0, -0.6), 'fore_l': (0, 0, 0.6), 'hind_r': (0, 0, -0.45), 'hind_l': (0, 0, 0.45)}
SHED = {'body': (-0.7, 0, 0), 'fore_r': (0, 0, 0.3), 'fore_l': (0, 0, -0.3), 'hind_r': (0, 0, 0.5), 'hind_l': (0, 0, -0.5)}

if __name__ == '__main__':
    M.write_java(JAVA) if len(sys.argv) < 3 else None
    tex, glow = make()
    tex.save(f'{A}/textures/entity/sleep_moth.png')
    glow.save(f'{A}/textures/entity/sleep_moth_glow.png')
    if len(sys.argv) > 1:
        shots = [M.render(tex, yaw=yaw, pitch=pitch, pose=pose, size=(300, 240), zoom=9.0, centre=(0, 14), glow=glow, background=(52, 62, 60))
                 for yaw, pitch, pose in ((-0.5, 0.5, SPREAD), (0.0, 1.2, SPREAD), (0.0, 0.2, UP), (-0.6, 0.3, DOWN), (2.6, 0.5, SPREAD), (-1.2, 0.2, SHED))]
        sheet(shots, 6).save(sys.argv[1])
