#!/usr/bin/env python3
"""The Weeping Mother of the Mire: mesh for WeepingMotherModel.java, her texture and the glow of her tears and of what
she keeps in her breast.
Run from the repo root; pass a file name to also get a preview sheet."""
import sys

from modelkit import Box, Model, Painter, blotch, mix, shade, sheet

A = 'src/main/resources/assets/vsratie_dushi'
JAVA = 'src/main/java/com/keykeeper/dushi/client/WeepingMotherModel.java'
SCALE = 2.0           # must match MODEL_SCALE in WeepingMotherModel.java

M = Model(128, 128, SCALE)
# a willow that is a woman from the waist up, or a woman who is a willow from the waist down: she stands in her own
# roots and does not leave them. A bole, a waist, a narrow breast split open down the middle with a light in the split;
# a small bowed head with a face like a mask of pale wood; hair that is the tree's withes, to the ground; two arms as
# long as the rest of her, a joint in each, hands that are bundles of twigs. Everything hangs from 'all'.
M.part('all', None, (0, 0, 0))
M.part('roots', 'all', (0, 24, 0), boxes=[Box(-8, -4, -8, 16, 4, 16, 'bark', 'mound'), Box(-15, -2, -2, 7, 2, 4, 'bark', 'root_r'), Box(8, -2, -3, 7, 2, 4, 'bark', 'root_l'),
                                          Box(-3, -2, -15, 4, 2, 7, 'bark', 'root_f'), Box(-1, -2, 8, 4, 2, 7, 'bark', 'root_b')])
M.part('trunk', 'all', (0, 20, 0), (0.06, 0, 0), boxes=[Box(-5, -9, -4, 10, 9, 8, 'bark', 'bole'), Box(-4, -18, -3, 8, 9, 6, 'bark', 'waist')])
M.part('torso', 'trunk', (0, -18, 0), (0.12, 0, 0), boxes=[Box(-6, -13, -3, 12, 13, 6, 'breast', 'breast')])
M.part('head', 'torso', (0, -13, 0), (0.22, 0, 0), boxes=[Box(-3.5, -8, -3.5, 7, 8, 7, 'face', 'skull'), Box(-1, -13, -1, 2, 5, 2, 'twig', 'crown_a'),
                                                          Box(-4, -11, 0, 2, 4, 1, 'twig', 'crown_b'), Box(2, -12, -1, 1, 5, 2, 'twig', 'crown_c')])
M.part('hair_back', 'head', (0, -8, 3.5), (0.06, 0, 0), boxes=[Box(-5.5, 0, 0, 11, 28, 1, 'withe', 'hair_back')])
M.part('hair_right', 'head', (-3.5, -8, 0), (0, 0, 0.07), boxes=[Box(-1, 0, -4, 1, 25, 8, 'withe', 'hair_side')])
M.mirror('hair_right', 'hair_left', parent='head')
M.part('hair_front', 'head', (0, -8, -3.5), (-0.07, 0, 0), boxes=[Box(-5.5, 0, -1, 3, 19, 1, 'withe', 'lock_r'), Box(2.5, 0, -1, 3, 16, 1, 'withe', 'lock_l')])
M.part('right_arm', 'torso', (-7, -11, 0), (0.08, 0, 0.14), boxes=[Box(-2, -1.5, -1.5, 3, 17, 3, 'bark', 'upper')])
M.part('right_forearm', 'right_arm', (-0.5, 15.5, 0), (-0.3, 0, 0), boxes=[Box(-1.5, 0, -1.5, 3, 16, 3, 'bark', 'fore'), Box(-2.5, 16, -2, 5, 9, 4, 'twig', 'hand'),
                                                                           Box(-0.5, 2, 1.5, 1, 13, 4, 'withe', 'sleeve')])
M.mirror('right_arm', 'left_arm')
M.mirror('right_forearm', 'left_forearm')
M.pack()

BARK = (70, 60, 46)
BARK_D = (46, 40, 32)
BARK_DD = (28, 24, 22)
BARK_L = (104, 92, 66)
MOSS = (74, 100, 50)
MASK = (206, 198, 170)
MASK_D = (156, 148, 122)
MASK_DD = (104, 96, 82)
TEAR = (150, 255, 130)
TEAR_L = (220, 255, 200)
WITHE = (88, 112, 50)
WITHE_D = (56, 78, 38)
WITHE_L = (128, 150, 66)
BEARD = (150, 158, 132)
BLACK = (12, 14, 10)


def bark(face, px, py, fw, fh, rnd):
    f = {'top': 1.1, 'bottom': 0.7}.get(face, 1.0)
    amount = blotch(f'mbark{fw}{fh}{face}', px, py * 0.4, 1.7)
    c = BARK_D if amount > 0.58 else (BARK_L if amount < 0.22 else BARK)
    if abs(blotch(f'mfur{fw}{fh}{face}', px, py * 0.25, 1.4) - 0.5) < 0.05:
        c = BARK_DD                              # the furrows of old bark, running up
    if blotch(f'mmoss{fw}{fh}{face}', px, py, 3.4) > 0.72 and face != 'bottom':
        c = MOSS
    return shade(c, f)


def withe(tag):
    def fn(face, px, py, fw, fh, rnd):
        broad = face in (('front', 'back') if tag != 'hair_side' else ('right', 'left'))
        if not broad:
            return shade(WITHE_D, 0.9) if py < 3 else None
        long = int(fh * (0.55 + 0.45 * blotch(f'len{tag}{face}', px, 0, 0.9)))
        if py >= long or (px % 3 == 2 and py > 2):
            return None                          # strands, with air between them below the crown
        c = WITHE_D if px % 3 == 1 else (WITHE_L if blotch(f'wc{tag}{face}', px, py, 2.0) > 0.66 else WITHE)
        if py >= long - 2:
            c = BEARD                            # the ends gone grey
        return shade(c, 1.06 - 0.012 * py)
    return fn


def make():
    p = Painter(M, 113)
    boxes = {b.tag: b for _, _, b in M.boxes()}
    for _, _, b in M.boxes():
        if b.mat == 'bark':
            p.fill_box(b, bark)
        elif b.mat == 'withe':
            p.fill_box(b, withe(b.tag))
        elif b.mat == 'twig':
            def twig(face, px, py, fw, fh, r, tag=b.tag):
                if tag == 'hand':
                    if face == 'top':
                        return shade(BARK, 1.0)
                    if face == 'bottom':
                        return None
                    finger = px % 2 == 0
                    long = fh - int(blotch(f'tw{face}', px, 0, 1.0) * 3)
                    if not finger and py > 1 or py >= long:
                        return None
                    return shade(BARK_L if py < fh - 3 else MASK_D, 1.0 - 0.03 * py)
                return shade(BARK_L if (px + py) % 2 else BARK, 1.0)
            p.fill_box(b, twig)
        elif b.mat == 'face':
            p.fill_box(b, lambda face, px, py, fw, fh, r: shade(BARK if face != 'front' else MASK, {'top': 1.05, 'bottom': 0.7}.get(face, 1.0 - 0.02 * py)))
        elif b.mat == 'breast':
            p.fill_box(b, bark)
    # the face: a mask of pale wood, the eyes shut, and from under each lid a line of light running down to the chin
    skull = boxes['skull']
    p.art(skull, 'front', ["dmmmmmd",
                           "mmmmmmm",
                           "mDDmDDm",
                           "mTmmmTm",
                           "mTmdmTm",
                           "mtmmmtm",
                           "dtmBmtd",
                           "ddmmmdd"], {'m': MASK, 'd': MASK_D, 'D': MASK_DD, 'T': TEAR_L, 't': TEAR, 'B': BLACK}, glow_chars='Tt')
    # the breast: split from the throat down, the edges of the split like ribs, and in it the light she keeps
    breast = boxes['breast']
    fx, fy, fw, fh = breast.faces()['front']
    for y in range(2, fh - 1):
        half = 1 if y in (2, fh - 2) else 2
        for x in range(fw // 2 - half, fw // 2 + half):
            core = abs(x - (fw / 2 - 0.5)) < 1 and 3 < y < fh - 3
            c = TEAR_L if core else (TEAR if (x + y) % 2 else (60, 150, 70))
            p.px(fx + x, fy + y, c, glow=True)
        for x in (fw // 2 - half - 1, fw // 2 + half):
            p.px(fx + x, fy + y, MASK_D if y % 2 else BARK_DD)
    return p.tex, p.glow


STAND = {}
# the poses of WeepingMotherModel.setupAnim, as it adds them to the rest pose
LASH_BACK = {'trunk': (-0.1, 0.5, 0), 'torso': (-0.15, 0.45, 0), 'right_arm': (-1.38, 0.35, 0.36), 'right_forearm': (0.5, 0, 0)}
LASH = {'trunk': (0.24, -0.18, 0), 'torso': (0.48, -0.16, 0), 'right_arm': (-1.965, -0.13, 0), 'right_forearm': (0.3, 0, 0), 'left_arm': (-0.7, 0, 0)}
LASH_END = {'trunk': (0.24, -0.5, 0), 'torso': (0.48, -0.45, 0), 'right_arm': (-1.965, -0.35, 0), 'right_forearm': (0.3, 0, 0), 'left_arm': (-0.7, 0, 0)}
SLAM_UP = {'torso': (-0.37, 0, 0), 'right_arm': (-2.98, 0, 0.06), 'left_arm': (-2.98, 0, -0.06), 'right_forearm': (0.6, 0, 0), 'left_forearm': (0.6, 0, 0), 'head': (-0.62, 0, 0)}
SLAM_DOWN = {'trunk': (0.29, 0, 0), 'torso': (0.48, 0, 0), 'right_arm': (-2.0, 0, -0.1), 'left_arm': (-2.0, 0, 0.1), 'right_forearm': (0.3, 0, 0), 'left_forearm': (0.3, 0, 0),
             'head': (-0.5, 0, 0)}
ROOTS = {'trunk': (0.3, 0, 0), 'torso': (0.5, 0, 0), 'right_arm': (-0.5, 0, 0.3), 'left_arm': (-0.5, 0, -0.3), 'right_forearm': (0.6, 0, 0), 'left_forearm': (0.6, 0, 0)}
WAIL = {'torso': (-0.47, 0, 0), 'head': (-0.92, 0, 0), 'right_arm': (-0.48, 0, 0.96), 'left_arm': (-0.48, 0, -0.96), 'right_forearm': (0.9, 0, 0), 'left_forearm': (0.9, 0, 0),
        'hair_back': (0.5, 0, 0)}
WHIRL = {'trunk': (0.1, 1.2, 0), 'right_arm': (0, 0, 0.76), 'left_arm': (0, 0, -0.76), 'right_forearm': (0.3, 0, 0), 'left_forearm': (0.3, 0, 0),
         'hair_back': (0.7, 0, 0), 'hair_right': (0, 0, 0.7), 'hair_left': (0, 0, -0.7), 'hair_front': (-0.5, 0, 0)}

if __name__ == '__main__':
    M.write_java(JAVA) if len(sys.argv) < 3 else None
    tex, glow = make()
    tex.save(f'{A}/textures/entity/weeping_mother.png')
    glow.save(f'{A}/textures/entity/weeping_mother_glow.png')
    if len(sys.argv) > 1:
        shots = [M.render(tex, yaw=yaw, pose=pose, size=(520, 470), zoom=3.0, centre=(0, -24), glow=glow)
                 for yaw, pose in ((-0.5, STAND), (0.0, STAND), (1.5, STAND), (2.7, STAND), (-0.6, LASH_BACK), (-1.2, LASH), (-1.57, LASH), (-0.6, LASH_END),
                                   (-1.1, SLAM_UP), (-1.57, SLAM_DOWN), (-0.9, ROOTS), (-1.57, ROOTS), (-0.5, WAIL), (-0.4, WHIRL), (-1.57, WHIRL))]
        sheet(shots, 4).save(sys.argv[1])
