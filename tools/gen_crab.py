#!/usr/bin/env python3
"""The sand crab of the Long Spit: mesh for CrabModel.java and its texture.
Run from the repo root; pass a file name to also get a preview sheet."""
import sys

from modelkit import Box, Model, Painter, blotch, mix, shade, sheet

A = 'src/main/resources/assets/unfading_dawn'
JAVA = 'src/main/java/com/keykeeper/dushi/client/CrabModel.java'
SCALE = 1.5           # must match MODEL_SCALE in CrabModel.java

M = Model(64, 32, SCALE)
# A crab the size of a dog, the colour of wet sand: it lies under the sand with its eyes out and is taken for two
# pebbles. A broad flat shell, eyes on stalks, one claw twice the size of the other, three legs a side. Everything
# hangs from 'all', which goes down when it buries itself.
M.part('all', None, (0, 20, 0))
M.part('body', 'all', (0, 0, 0), boxes=[Box(-5, -3, -4, 10, 4, 8, 'shell', 'shell'), Box(-4, -4, -3, 8, 1, 6, 'shell', 'hump')])
M.part('eye_r', 'body', (-2, -3, -3.5), boxes=[Box(-0.5, -3, -0.5, 1, 3, 1, 'stalk', 'stalk'), Box(-1, -5, -1, 2, 2, 2, 'eye', 'eye')])
M.mirror('eye_r', 'eye_l', parent='body')
# the great claw, on its right: an arm out to the side and forward, the hand at the end of it, and a thumb that opens
M.part('claw_r', 'body', (-5, -1, -2.5), (0, -0.5, 0), boxes=[Box(-4, -1, -1, 4, 2, 2, 'limb', 'arm')])
M.part('hand_r', 'claw_r', (-4, 0, 0), (0, -0.6, 0), boxes=[Box(-1.5, -2, -6, 3, 4, 6, 'claw', 'palm'), Box(-1, 0.5, -9, 2, 1.5, 3, 'claw', 'finger')])
M.part('thumb_r', 'hand_r', (0, -1.5, -5.5), boxes=[Box(-1, -1, -3.5, 2, 1.5, 3.5, 'claw', 'thumb')])
M.part('claw_l', 'body', (5, -1, -2.5), (0, 0.5, 0), boxes=[Box(0, -0.5, -0.5, 3, 1, 1, 'limb', 'arm')])
M.part('hand_l', 'claw_l', (3, 0, 0), (0, 0.6, 0), boxes=[Box(-1, -1, -3, 2, 2, 3, 'claw', 'palm'), Box(-0.5, -0.5, -5, 1, 1, 2, 'claw', 'finger')])
for i, z in enumerate((-1.5, 1, 3.5)):
    M.part(f'leg_r{i + 1}', 'body', (-5, 0, z), (0, (i - 1) * 0.35, 0), boxes=[Box(-4, -0.5, -0.5, 4, 1, 1, 'limb', 'thigh'), Box(-5, -0.5, -0.5, 1, 4.5, 1, 'limb', 'shin')])
    M.mirror(f'leg_r{i + 1}', f'leg_l{i + 1}', parent='body')
M.pack()

SAND = (196, 176, 132)
SAND_D = (160, 140, 102)
SAND_L = (222, 206, 164)
RUSTY = (168, 106, 72)
RUSTY_D = (124, 74, 52)
PALE = (228, 216, 190)
BLACK = (18, 16, 18)
WEED = (72, 104, 74)


def shell(face, px, py, fw, fh, rnd):
    c = SAND_D if blotch(f'sh{face}{fw}', px, py, 1.9) > 0.58 else (SAND_L if rnd.random() < 0.12 else SAND)
    if face == 'top':
        # a mark like a face on its back, as such crabs have
        cx = abs(px - (fw - 1) / 2)
        if (py in (2, 3) and 1.0 < cx < 3.0) or (py == 5 and cx < 2.0):
            c = RUSTY_D
        if blotch('bw', px, py, 1.4) > 0.8:
            c = mix(c, WEED, 0.5)
    if face == 'bottom':
        c = PALE
    if face in ('front', 'back', 'left', 'right') and py == fh - 1:
        c = shade(c, 0.8)
    return shade(c, {'top': 1.08, 'bottom': 0.8}.get(face, 1.0 - 0.05 * py))


def limb(face, px, py, fw, fh, rnd):
    c = RUSTY_D if (px + py) % 3 == 0 else RUSTY
    return shade(c, {'top': 1.1, 'bottom': 0.75}.get(face, 1.0))


def claw(face, px, py, fw, fh, rnd):
    c = RUSTY if blotch(f'cl{face}{fw}', px, py, 1.6) < 0.6 else RUSTY_D
    if face in ('left', 'right', 'top') and px < 2 or face == 'front':
        c = PALE if (px + py) % 2 else shade(PALE, 0.85)          # the pale tips and edges of the pincers
    return shade(c, {'top': 1.1, 'bottom': 0.75}.get(face, 1.0))


def make():
    p = Painter(M, 311)
    for name, _, b in M.boxes():
        if b.mat == 'shell':
            p.fill_box(b, shell)
        elif b.mat == 'limb':
            p.fill_box(b, limb)
        elif b.mat == 'claw':
            p.fill_box(b, claw)
        elif b.mat == 'stalk':
            p.fill_box(b, lambda face, px, py, fw, fh, r: shade(SAND_D, 1.0 - 0.1 * py))
        elif b.mat == 'eye':
            p.fill_box(b, lambda face, px, py, fw, fh, r: BLACK if face != 'top' or (px + py) % 2 else (60, 56, 60))
    return p.tex


# The poses of CrabModel.setupAnim: walking with the great claw up; buried; the pinch drawn and struck
UP = {'claw_r': (0, 0, 0.5), 'hand_r': (0, 0.2, 0)}
BURIED = {'all': {'off': (0, 7.0, 0)}, 'claw_r': (0, 0.9, -0.2), 'hand_r': (0, 0.9, 0), 'claw_l': (0, -0.8, 0), 'leg_r1': (0, 0, -0.6), 'leg_r2': (0, 0, -0.6), 'leg_r3': (0, 0, -0.6),
          'leg_l1': (0, 0, 0.6), 'leg_l2': (0, 0, 0.6), 'leg_l3': (0, 0, 0.6)}
DRAWN = {'body': (0, 0.4, 0), 'claw_r': (0, -0.6, 0.6), 'hand_r': (0, -0.3, 0), 'thumb_r': (-0.9, 0, 0)}
STRUCK = {'body': (0, -0.3, 0), 'claw_r': (0, 0.75, 0.1), 'hand_r': (0, 0.5, 0), 'thumb_r': (0.05, 0, 0)}

if __name__ == '__main__':
    M.write_java(JAVA) if len(sys.argv) < 3 else None
    tex = make()
    tex.save(f'{A}/textures/entity/sand_crab.png')
    if len(sys.argv) > 1:
        shots = [M.render(tex, yaw=yaw, pitch=pitch, pose=pose, size=(270, 220), zoom=9.0, centre=(0, 18), background=(190, 174, 136))
                 for yaw, pitch, pose in ((-0.5, 0.3, UP), (0.0, 0.3, UP), (2.6, 0.3, UP), (-0.4, 0.9, UP), (-0.5, 0.4, BURIED), (-0.4, 0.9, BURIED), (-0.5, 0.3, DRAWN), (-0.5, 0.3, STRUCK))]
        sheet(shots, 8).save(sys.argv[1])
