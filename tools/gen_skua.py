#!/usr/bin/env python3
"""The skua of the Morning Wharf: mesh for SkuaModel.java and its texture.
Run from the repo root; pass a file name to also get a preview sheet."""
import sys

from modelkit import Box, Model, Painter, blotch, mix, shade, sheet

A = 'src/main/resources/assets/vsratie_dushi'
JAVA = 'src/main/java/com/keykeeper/dushi/client/SkuaModel.java'
SCALE = 1.4           # must match MODEL_SCALE in SkuaModel.java

M = Model(64, 32, SCALE)
# A sea bird that lives on what others have caught, the colour of smoke, with a hooked beak and a white flash in each
# wing: it sits on a pile or a yard, hunched, and is taken for part of it. Everything hangs from 'all'.
M.part('all', None, (0, 19, 0))
M.part('body', 'all', (0, 0, 0), (-0.25, 0, 0), boxes=[Box(-2.5, -2.5, -4, 5, 4, 8, 'feather', 'body')])
M.part('tail', 'body', (0, -1, 4), (0.15, 0, 0), boxes=[Box(-2, -0.5, 0, 4, 1, 4, 'feather', 'tail')])
M.part('head', 'body', (0, -1.5, -4), (0.25, 0, 0), boxes=[Box(-1.5, -3, -2.5, 3, 3, 3, 'feather', 'head'), Box(-0.5, -1.8, -5.5, 1, 1, 3, 'beak', 'beak'),
                                                              Box(-0.5, -0.8, -5.5, 1, 1, 1, 'beak', 'hook')])
M.part('wing_r', 'body', (-2.5, -2, -1), boxes=[Box(-7, -0.5, -3, 7, 1, 6, 'wing', 'inner')])
M.part('tip_r', 'wing_r', (-7, 0, 0), boxes=[Box(-7, -0.5, -2.5, 7, 1, 5, 'wing', 'outer')])
M.mirror('wing_r', 'wing_l', parent='body')
M.mirror('tip_r', 'tip_l', parent='wing_l')
M.part('legs', 'all', (0, 1, 1), boxes=[Box(-1.5, 0, -0.5, 1, 4, 1, 'leg', 'leg_a'), Box(0.5, 0, -0.5, 1, 4, 1, 'leg', 'leg_b'), Box(-2, 3.5, -2, 2, 0.5, 2, 'leg', 'foot_a'),
                                         Box(0, 3.5, -2, 2, 0.5, 2, 'leg', 'foot_b')])
M.pack()

SMOKE = (92, 84, 78)
SMOKE_D = (62, 56, 54)
SMOKE_L = (126, 116, 104)
WHITE = (226, 222, 210)
BEAK = (50, 46, 48)
BEAK_L = (96, 88, 80)
LEG = (70, 70, 76)
EYE = (236, 196, 90)
BLACK = (14, 12, 14)


def feather(tag):
    def fn(face, px, py, fw, fh, rnd):
        c = SMOKE_D if blotch(f'f{tag}{face}', px, py, 1.7) > 0.58 else (SMOKE_L if rnd.random() < 0.1 else SMOKE)
        if tag == 'body' and face == 'bottom':
            c = SMOKE_L if (px + py) % 2 else shade(SMOKE_L, 0.9)   # paler underneath, barred
        if tag == 'head' and face == 'top':
            c = SMOKE_D                                          # a dark cap
        return shade(c, {'top': 1.08, 'bottom': 0.85}.get(face, 1.0 - 0.04 * py))
    return fn


def wing(tag, mirrored):
    def fn(face, px, py, fw, fh, rnd):
        if face not in ('top', 'bottom'):
            return SMOKE_D
        out = (fw - 1 - px) if not mirrored else px             # how far out along the wing
        if face == 'bottom':
            out = fw - 1 - out
        if tag == 'outer':
            # the hand of the wing: it narrows to a point, swept back
            if py < out * 0.45 - 0.5 or (out >= fw - 1 and py < fh - 1):
                return None
        c = SMOKE_D if (out + py) % 3 == 0 else SMOKE
        if py >= fh - 2:
            c = SMOKE_D                                          # the long feathers of the trailing edge
        if tag == 'outer' and out <= 2 and 1 <= py <= fh - 2:
            c = WHITE                                            # the flash
        return shade(c, 1.05 if face == 'top' else 0.85)
    return fn


def make():
    p = Painter(M, 331)
    boxes = {}
    for name, _, b in M.boxes():
        boxes[(name, b.tag)] = b
        if b.mat == 'feather':
            p.fill_box(b, feather(b.tag))
        elif b.mat == 'wing':
            p.fill_box(b, wing(b.tag, name.endswith('_l')))
        elif b.mat == 'beak':
            p.fill_box(b, lambda face, px, py, fw, fh, r: BEAK_L if face == 'top' else BEAK)
        elif b.mat == 'leg':
            p.fill_box(b, lambda face, px, py, fw, fh, r: LEG)
    head = boxes[('head', 'head')]
    for face in ('right', 'left'):
        p.put(head, face, 0 if face == 'right' else 2, 1, EYE)
        p.put(head, face, 1, 1, BLACK)
    return p.tex


# The poses of SkuaModel.setupAnim: perched with the wings folded; flying, the wings up and down; the stoop
FOLDED = {'wing_r': (0, 1.35, -0.35), 'tip_r': (0, 2.7, 0.08), 'wing_l': (0, -1.35, 0.35), 'tip_l': (0, -2.7, -0.08), 'head': (0.2, 0, 0)}
WINGS_UP = {'all': {'off': (0, -3, 0)}, 'body': (0.25, 0, 0), 'wing_r': (0, 0, 0.6), 'tip_r': (0, 0, 0.3), 'wing_l': (0, 0, -0.6), 'tip_l': (0, 0, -0.3), 'legs': (0.9, 0, 0)}
WINGS_DOWN = {'all': {'off': (0, -3, 0)}, 'body': (0.25, 0, 0), 'wing_r': (0, 0, -0.5), 'tip_r': (0, 0, -0.3), 'wing_l': (0, 0, 0.5), 'tip_l': (0, 0, 0.3), 'legs': (0.9, 0, 0)}
STOOP = {'all': {'off': (0, -3, 0), 'rot': (0.9, 0, 0)}, 'body': (0.25, 0, 0), 'wing_r': (0, 0.9, 0.15), 'tip_r': (0, 0.5, 0), 'wing_l': (0, -0.9, -0.15), 'tip_l': (0, -0.5, 0),
         'head': (-0.4, 0, 0), 'legs': (-0.6, 0, 0)}

if __name__ == '__main__':
    M.write_java(JAVA) if len(sys.argv) < 3 else None
    tex = make()
    tex.save(f'{A}/textures/entity/skua.png')
    if len(sys.argv) > 1:
        shots = [M.render(tex, yaw=yaw, pitch=pitch, pose=pose, size=(270, 220), zoom=8.0, centre=(0, 16), background=(168, 178, 186))
                 for yaw, pitch, pose in ((-0.6, 0.2, FOLDED), (0.0, 0.2, FOLDED), (-1.57, 0.1, FOLDED), (-0.5, 0.5, WINGS_UP), (0.0, 0.2, WINGS_UP), (-0.5, 0.5, WINGS_DOWN),
                                          (-1.3, 0.2, STOOP), (-0.5, 0.6, STOOP))]
        sheet(shots, 8).save(sys.argv[1])
