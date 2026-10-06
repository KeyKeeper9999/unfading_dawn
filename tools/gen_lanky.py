#!/usr/bin/env python3
"""The lanky of the Mire (a zherdyai: the thin one that walks the bog at night and looks in at windows): mesh for
LankyModel.java, its texture and the glow of its eyes.
Run from the repo root; pass a file name to also get a preview sheet."""
import sys

from modelkit import Box, Model, Painter, blotch, mix, shade, sheet

A = 'src/main/resources/assets/vsratie_dushi'
JAVA = 'src/main/java/com/keykeeper/dushi/client/LankyModel.java'

M = Model(64, 64)
# twice a man's height and no wider than his arm: legs like poles, a narrow cage of ribs carried stooping, a small head
# under a mat of weed, and arms that hang to its shins - two joints each, and a hand that is all finger.
HIP = -4                                   # the legs are 28 long
M.part('right_leg', None, (-1.5, HIP, 0), boxes=[Box(-1, 0, -1, 2, 14, 2, 'bark', 'thigh')])
M.part('right_shin', 'right_leg', (0, 14, 0), boxes=[Box(-1, 0, -1, 2, 14, 2, 'bark', 'shin'), Box(-1.5, 13, -3, 3, 1, 4, 'bark', 'foot')])
M.mirror('right_leg', 'left_leg', parent=None)
M.mirror('right_shin', 'left_shin')
M.part('torso', None, (0, HIP, 0), (0.22, 0, 0), boxes=[Box(-2.5, -6, -1.5, 5, 6, 3, 'bark', 'loins'), Box(-3.5, -16, -2, 7, 10, 4, 'rib', 'ribs')])
M.part('head', 'torso', (0, -16, 0), (0.1, 0, 0), boxes=[Box(-2.5, -6, -2.5, 5, 6, 5, 'face', 'skull'), Box(-3, -6.5, -3, 6, 9, 6, 'weed', 'weed', inflate=0.25),
                                                        Box(-0.5, -11, 0.5, 1, 5, 1, 'reed', 'reed_a'), Box(1.5, -9, -1, 1, 3, 1, 'reed', 'reed_b')])
M.part('right_arm', 'torso', (-4, -15, 0), (0.05, 0, 0.1), boxes=[Box(-1, -1, -1, 2, 16, 2, 'bark', 'upper')])
M.part('right_forearm', 'right_arm', (0, 15, 0), (-0.28, 0, 0), boxes=[Box(-1, 0, -1, 2, 16, 2, 'bark', 'fore'), Box(-1.5, 16, -1.5, 3, 6, 3, 'claw', 'hand')])
M.mirror('right_arm', 'left_arm')
M.mirror('right_forearm', 'left_forearm')
M.pack()

BARK = (62, 66, 54)
BARK_D = (40, 44, 38)
BARK_L = (92, 96, 74)
MOSS = (72, 104, 48)
MOSS_L = (112, 140, 62)
RIB = (120, 118, 96)
FACE = (148, 150, 124)
FACE_D = (100, 104, 86)
WEED = (52, 78, 44)
WEED_D = (34, 52, 34)
WEED_L = (86, 116, 56)
CLAW = (186, 180, 150)
EYE = (190, 255, 150)
BLACK = (14, 16, 12)


def bark(face, px, py, fw, fh, rnd):
    f = {'top': 1.1, 'bottom': 0.7}.get(face, 1.0)
    amount = blotch(f'bark{fw}{fh}{face}', px, py * 0.5, 1.6)
    c = BARK_D if amount > 0.6 else (BARK_L if amount < 0.24 else BARK)
    if blotch(f'moss{fw}{fh}{face}', px, py, 3.2) > 0.68:
        c = MOSS if rnd.random() < 0.7 else MOSS_L
    if py % 7 == 3 and fh > 8:
        c = shade(c, 0.78)                       # the knots of a jointed thing
    return shade(c, f)


def make():
    p = Painter(M, 83)
    boxes = {b.tag: b for _, _, b in M.boxes()}
    for name, _, b in M.boxes():
        if b.mat == 'bark':
            p.fill_box(b, bark)
        elif b.mat == 'rib':
            def rib(face, px, py, fw, fh, r):
                if face in ('top', 'bottom'):
                    return shade(BARK_D, 0.9)
                gap = py % 2 == 1 and 0 < py < fh - 1 and not (face in ('front', 'back') and px == fw // 2)
                if gap:
                    return BLACK if (face in ('front', 'back') and 0 < px < fw - 1) or face in ('right', 'left') else BARK_D
                c = RIB if blotch(f'rib{face}', px, py, 2.0) < 0.7 else MOSS
                return shade(c, 1.05 - 0.03 * py)
            p.fill_box(b, rib)
        elif b.mat == 'face':
            p.fill_box(b, lambda face, px, py, fw, fh, r: shade(FACE if blotch(f'face{face}', px, py, 2.2) < 0.62 else FACE_D, {'top': 1.05, 'bottom': 0.7}.get(face, 1.0 - 0.04 * py)))
        elif b.mat == 'weed':
            def weed(face, px, py, fw, fh, r):
                if face == 'bottom':
                    return None
                if face == 'top':
                    return shade(WEED if (px + py) % 3 else WEED_L, 1.05)
                long = 3 + int(blotch(f'weed{face}', px, 0, 1.0) * (fh - 2))
                if face == 'front' and 1 <= px <= fw - 2:
                    long = 2                                         # the face looks out from under the fringe, between two hanks
                if py >= long:
                    return None
                c = WEED_D if px % 2 else (WEED_L if blotch(f'weedc{face}', px, py, 1.4) > 0.7 else WEED)
                return shade(c, 1.05 - 0.05 * py)
            p.fill_box(b, weed)
        elif b.mat == 'reed':
            p.fill_box(b, lambda face, px, py, fw, fh, r: shade((126, 118, 70) if py > 1 else (84, 60, 40), 1.0))
        elif b.mat == 'claw':
            def claw(face, px, py, fw, fh, r):
                if face in ('top',):
                    return shade(BARK_D, 1.0)
                if face == 'bottom':
                    return None
                finger = px % 2 == 0
                if not finger and py > 0:
                    return None
                return shade(CLAW if py > 2 else BARK_L, 1.0 - 0.04 * py) if finger else shade(BARK, 0.9)
            p.fill_box(b, claw)
    # the face: no nose, no mouth to speak of - two pits with a light far back in them, and a long seam below
    skull = boxes['skull']
    p.art(skull, 'front', ["fdfdf",
                           "BfdfB",
                           "EfdfE",
                           "BdfdB",
                           "fdBdf",
                           "ddBdd"], {'f': FACE, 'd': FACE_D, 'B': BLACK, 'E': EYE}, glow_chars='E')
    return p.tex, p.glow


STAND = {}
WALK = {'right_leg': (-0.55, 0, 0), 'right_shin': (0.35, 0, 0), 'left_leg': (0.45, 0, 0), 'left_shin': (0.3, 0, 0), 'right_arm': (0.3, 0, 0), 'left_arm': (-0.3, 0, 0)}
REACH_BACK = {'torso': (0.1, 0.5, 0), 'right_arm': (0.9, 0, 0.2), 'right_forearm': (-1.6, 0, 0), 'head': (0, -0.4, 0)}
REACH = {'torso': (0.55, -0.35, 0), 'right_arm': (-1.9, 0, 0), 'right_forearm': (0.25, 0, 0), 'head': (-0.3, 0.3, 0), 'left_arm': (0.5, 0, 0)}
SLAM_UP = {'torso': (-0.2, 0, 0), 'right_arm': (-3.0, 0, 0.15), 'left_arm': (-3.0, 0, -0.15), 'right_forearm': (0.3, 0, 0), 'left_forearm': (0.3, 0, 0), 'head': (-0.3, 0, 0)}
SLAM_DOWN = {'torso': (0.75, 0, 0), 'right_arm': (-1.5, 0, 0.1), 'left_arm': (-1.5, 0, -0.1), 'right_forearm': (0.25, 0, 0), 'left_forearm': (0.25, 0, 0),
             'right_leg': (-0.3, 0, 0), 'left_leg': (0.3, 0, 0)}
SWEEP = {'torso': (0.5, 0.7, 0), 'right_arm': (-1.3, 0.9, 0.4), 'left_arm': (-1.2, 0.2, -0.9), 'right_forearm': (0.2, 0, 0), 'left_forearm': (0.2, 0, 0)}

if __name__ == '__main__':
    M.write_java(JAVA) if len(sys.argv) < 3 else None
    tex, glow = make()
    tex.save(f'{A}/textures/entity/lanky.png')
    glow.save(f'{A}/textures/entity/lanky_glow.png')
    if len(sys.argv) > 1:
        shots = [M.render(tex, yaw=yaw, pose=pose, size=(300, 460), zoom=7.0, centre=(0, -3), glow=glow)
                 for yaw, pose in ((-0.5, STAND), (0.0, STAND), (1.5, WALK), (2.7, STAND), (-1.2, REACH_BACK), (-1.3, REACH), (-1.2, SLAM_UP), (-1.3, SLAM_DOWN), (-0.5, SWEEP))]
        sheet(shots, 5).save(sys.argv[1])
