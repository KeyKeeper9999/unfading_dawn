#!/usr/bin/env python3
"""The Capsized - the lord of the shoal under the lighthouse: mesh for CapsizedModel.java, its texture and the glow of
its eyes. Run from the repo root; pass a file name to also get a preview sheet."""
import math
import sys

from modelkit import Box, Model, Painter, blotch, mix, shade, sheet

A = 'src/main/resources/assets/vsratie_dushi'
JAVA = 'src/main/java/com/keykeeper/dushi/client/CapsizedModel.java'

M = Model(256, 256)
# A hermit crab that has outgrown every shell in the sea and taken a fishing boat: it lies on the shoal keel up, as
# boats are left for the winter, and is a boat until somebody comes to it. Then the boat gets up on six legs, and from
# under its bow come two eyes on stalks, a small claw and a great one. Everything hangs from 'all' (the ground is at
# its 0); the hull turns about the edge of its stern, so that it can rear and come down.
STAND = 8               # how high the gunwale is carried off the ground
M.part('all', None, (0, 24, 0))
M.part('hull', 'all', (0, -STAND, 30), boxes=[
    Box(-16, -6, -60, 32, 6, 60, 'plank', 'strake_a'), Box(-13, -11, -58, 26, 5, 56, 'plank', 'strake_b'), Box(-9, -15, -55, 18, 4, 50, 'plank', 'strake_c'),
    Box(-1, -17, -62, 2, 2, 62, 'keel', 'keel'), Box(-11, -6, -64, 22, 6, 4, 'plank', 'bow_a'), Box(-6, -6, -67, 12, 6, 3, 'plank', 'bow_b'),
    Box(-1, -17, -68, 2, 17, 2, 'keel', 'stem')])
M.part('body', 'all', (0, -5, 0), boxes=[Box(-9, -6, -12, 18, 8, 26, 'flesh', 'body')])
M.part('eye_r', 'body', (-4, -3, -12), (0.9, 0, -0.15), boxes=[Box(-1, -12, -1, 2, 12, 2, 'stalk', 'stalk'), Box(-1.5, -15, -1.5, 3, 3, 3, 'eye', 'eye')])
M.mirror('eye_r', 'eye_l', parent='body')
# the great claw, on its right: an arm forward out from under the bow, a hand like a chest, a thumb that opens upwards
M.part('claw_r', 'body', (-10, 0, -10), (0, 0.25, 0), boxes=[Box(-3, -3, -14, 6, 6, 14, 'shell', 'arm')])
M.part('hand_r', 'claw_r', (0, 0, -14), (0, -0.25, 0), boxes=[Box(-5, -6, -16, 10, 11, 16, 'claw', 'palm'), Box(-4, 1, -27, 8, 4, 11, 'claw', 'finger')])
M.part('thumb_r', 'hand_r', (0, -3, -15), boxes=[Box(-4, -3, -12, 8, 5, 12, 'claw', 'thumb')])
M.part('claw_l', 'body', (10, 0, -10), (0, -0.3, 0), boxes=[Box(-2, -2, -9, 4, 4, 9, 'shell', 'arm')])
M.part('hand_l', 'claw_l', (0, 0, -9), (0, 0.3, 0), boxes=[Box(-3, -3, -9, 6, 6, 9, 'claw', 'palm'), Box(-2, 0, -14, 4, 2, 5, 'claw', 'finger')])
for i, z in enumerate((-6, 5, 16)):
    M.part(f'leg_r{i + 1}', 'body', (-9, 0, z), (0, (i - 1) * 0.3, 0.5), boxes=[Box(-13, -2, -2, 13, 4, 4, 'shell', 'thigh')])
    M.part(f'shin_r{i + 1}', f'leg_r{i + 1}', (-13, 0, 0), (0, 0, -0.5), boxes=[Box(-2, -1, -2, 4, 13, 4, 'shell', 'shin'), Box(-1, 12, -1, 2, 2, 2, 'claw', 'tip')])
    M.mirror(f'leg_r{i + 1}', f'leg_l{i + 1}', parent='body')
    M.mirror(f'shin_r{i + 1}', f'shin_l{i + 1}', parent=f'leg_l{i + 1}')
M.pack()

PLANK = (112, 102, 88)
PLANK_D = (78, 70, 62)
PLANK_L = (146, 136, 118)
TAR = (40, 36, 36)
WEED = (62, 98, 70)
WEED_D = (40, 70, 52)
BARNACLE = (206, 200, 180)
BARNACLE_D = (150, 144, 128)
RUSTC = (132, 80, 50)
SHELL = (160, 96, 70)
SHELL_D = (112, 62, 50)
SHELL_L = (198, 130, 92)
PALE = (232, 216, 188)
FLESH = (196, 150, 140)
FLESH_D = (150, 106, 104)
BLACK = (14, 12, 14)
EYE = (255, 196, 96)
EYE_L = (255, 240, 190)


def plank(tag):
    def fn(face, px, py, fw, fh, rnd):
        if face == 'bottom':
            return shade(PLANK_D, 0.5)                           # the inside of the boat: dark
        # the strakes run the length of her: along z on the sides and the top, so along x on the texture of 'right'/'left'
        if face in ('right', 'left'):
            along, across = px, py
        elif face == 'top':
            along, across = py, px
        else:
            along, across = px * 3, py
        c = PLANK_D if blotch(f'pl{tag}{face}', along * 0.35, across, 1.4) > 0.62 else (PLANK_L if rnd.random() < 0.08 else PLANK)
        if face in ('right', 'left') and (py == fh - 1 or py == 0):
            c = TAR                                              # the seam between two strakes, pitched
        if face == 'top' and px in (0, fw - 1):
            c = TAR
        if face in ('right', 'left', 'top') and along % 15 == 7:
            c = RUSTC if across % 2 == 0 else c                  # a row of clenched nails at every rib
        # what the sea has put on her: weed along the gunwale (which is down now), barnacles in patches
        if tag in ('strake_a', 'bow_a', 'bow_b') and face in ('right', 'left', 'front', 'back') and py >= fh - 3 and blotch(f'wd{tag}{face}', px, py, 2.0) > 0.4:
            c = WEED if (px + py) % 2 else WEED_D
        if blotch(f'bn{tag}{face}', px, py, 2.6) > 0.78:
            c = BARNACLE if (px * 3 + py) % 3 else BARNACLE_D
        return shade(c, {'top': 1.1}.get(face, 1.02 - 0.06 * py / max(1, fh - 1)))
    return fn


def keel(face, px, py, fw, fh, rnd):
    c = PLANK_D if (px + py) % 4 else TAR
    if blotch(f'kb{face}', px, py, 2.0) > 0.75:
        c = BARNACLE_D
    return shade(c, {'top': 1.15, 'bottom': 0.6}.get(face, 1.0))


def shell(face, px, py, fw, fh, rnd):
    c = SHELL_D if blotch(f'sh{fw}{fh}{face}', px, py, 1.8) > 0.58 else (SHELL_L if rnd.random() < 0.1 else SHELL)
    if (px * 2 + py) % 7 == 0:
        c = shade(c, 0.8)                                        # the spines of it, in rows
    return shade(c, {'top': 1.1, 'bottom': 0.72}.get(face, 1.0 - 0.04 * py / max(1, fh / 6)))


def claw(tag):
    def fn(face, px, py, fw, fh, rnd):
        c = SHELL_D if blotch(f'cl{tag}{face}', px, py, 2.2) > 0.6 else SHELL
        if tag in ('finger', 'thumb', 'tip'):
            # the pincers are pale along their edges, with a row of blunt teeth where they meet
            edge = (face in ('right', 'left') and (py == (fh - 1 if tag == 'thumb' else 0))) or face == 'front'
            if edge:
                c = PALE if px % 2 else shade(PALE, 0.8)
            elif (face == 'bottom' and tag == 'thumb') or (face == 'top' and tag == 'finger'):
                c = PALE if (px + py) % 2 else shade(PALE, 0.86)
        if blotch(f'cb{tag}{face}', px, py, 2.4) > 0.8:
            c = BARNACLE_D
        return shade(c, {'top': 1.1, 'bottom': 0.72}.get(face, 1.0))
    return fn


def flesh(face, px, py, fw, fh, rnd):
    c = FLESH_D if blotch(f'fl{face}', px, py, 2.6) > 0.55 else FLESH
    return shade(c, {'top': 0.8, 'bottom': 0.7}.get(face, 0.9))


def make():
    p = Painter(M, 351)
    for name, _, b in M.boxes():
        if b.mat == 'plank':
            p.fill_box(b, plank(b.tag))
        elif b.mat == 'keel':
            p.fill_box(b, keel)
        elif b.mat == 'shell':
            p.fill_box(b, shell)
        elif b.mat == 'claw':
            p.fill_box(b, claw(b.tag))
        elif b.mat == 'flesh':
            p.fill_box(b, flesh)
        elif b.mat == 'stalk':
            p.fill_box(b, lambda face, px, py, fw, fh, r: shade(FLESH_D, 1.0 - 0.03 * py))
        elif b.mat == 'eye':
            def eye(face, px, py, fw, fh, rnd):
                if face in ('front', 'top') and px == 1 and py == 1:
                    return BLACK
                return EYE if (px + py) % 2 else EYE_L
            p.fill_box(b, eye)
            for face, (fx, fy, fw, fh) in b.faces().items():
                for x in range(fw):
                    for y in range(fh):
                        r_, g_, b_, a_ = p.tex.getpixel((fx + x, fy + y))
                        if a_ and r_ > 200:
                            p.glow.putpixel((fx + x, fy + y), (r_, g_, b_, 255))
    return p.tex, p.glow


LEGS = ('leg_r1', 'leg_r2', 'leg_r3', 'leg_l1', 'leg_l2', 'leg_l3')
UNDER = LEGS + ('eye_r', 'eye_l', 'claw_r', 'claw_l')        # what is not seen when it is a boat
# The poses of CapsizedModel.setupAnim: a boat on the sand; up and walking; the great claw drawn and shot; reared; down; tucked
BOAT = {'all': {'off': (0, STAND, 0)}}
WALK = {'hull': (-0.08, 0, 0), 'claw_r': (0, 0, 0.25), 'thumb_r': (-0.4, 0, 0)}
SNAP_DRAWN = {'hull': (-0.12, 0, 0), 'body': (0, 0.2, 0), 'claw_r': (0, 0.7, 0.3), 'hand_r': (0, 0.5, 0), 'thumb_r': (-0.9, 0, 0)}
SNAPPED = {'hull': (-0.05, 0, 0), 'body': (0, -0.15, 0), 'claw_r': (-0.1, -0.2, 0), 'hand_r': (0, 0.2, 0), 'thumb_r': (0.05, 0, 0), 'all': {'off': (0, 0, -8)}}
SWEPT = {'hull': (-0.08, -0.25, 0), 'body': (0, -0.35, 0), 'claw_r': (0, -1.0, 0.1), 'hand_r': (0, -0.2, 0), 'thumb_r': (-0.5, 0, 0)}
REARED = {'hull': (-0.75, 0, 0), 'body': (-0.3, 0, 0), 'claw_r': (-0.5, 0.3, 0.4), 'thumb_r': (-0.8, 0, 0), 'eye_r': (-0.5, 0, 0), 'eye_l': (-0.5, 0, 0)}
SLAMMED = {'hull': (0.14, 0, 0), 'all': {'off': (0, 3, -6)}, 'claw_r': (0.2, 0.5, 0), 'eye_r': (0.4, 0, 0), 'eye_l': (0.4, 0, 0)}

if __name__ == '__main__':
    M.write_java(JAVA) if len(sys.argv) < 3 else None
    tex, glow = make()
    tex.save(f'{A}/textures/entity/capsized.png')
    glow.save(f'{A}/textures/entity/capsized_glow.png')
    if len(sys.argv) > 1:
        shots = [M.render(tex, yaw=yaw, pitch=pitch, pose=pose, size=(420, 300), zoom=3.6, centre=(0, 12), glow=glow, hidden=hid, background=(176, 170, 150))
                 for yaw, pitch, pose, hid in ((-0.6, 0.25, BOAT, UNDER), (-1.57, 0.1, BOAT, UNDER), (-0.6, 0.25, WALK, ()), (0.0, 0.15, WALK, ()), (-1.57, 0.1, WALK, ()),
                                               (2.5, 0.3, WALK, ()), (-0.9, 0.25, SNAP_DRAWN, ()), (-0.9, 0.25, SNAPPED, ()), (-0.6, 0.35, SWEPT, ()), (-1.57, 0.1, REARED, ()),
                                               (-1.57, 0.1, SLAMMED, ()), (-0.6, 0.6, WALK, ()))]
        sheet(shots, 6).save(sys.argv[1])
