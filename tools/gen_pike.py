#!/usr/bin/env python3
"""The bone pike of the Mire: mesh for BonePikeModel.java, its texture and the glow of its eyes.
Run from the repo root; pass a file name to also get a preview sheet."""
import sys

from modelkit import Box, Model, Painter, blotch, mix, shade, sheet

A = 'src/main/resources/assets/unfading_dawn'
JAVA = 'src/main/java/com/keykeeper/dushi/client/BonePikeModel.java'

M = Model(128, 64)
# a fish as long as a man, of the kind that was old when the bog was a lake: the head and the shoulders are one shield
# of bone, the jaws are two blades of the same bone and need no teeth, the rest is dark scale going to a tall ragged
# tail. Everything hangs from 'all', which is its middle: it pitches about that when it leaps and lies on it when it
# is thrown up on the boards.
M.part('all', None, (0, 18, 0))
M.part('body', 'all', (0, 0, 0), boxes=[Box(-3.5, -4.5, -7, 7, 9, 12, 'scale', 'trunk'), Box(-4, -5.5, -7, 8, 2, 5, 'plate', 'plate_a'),
                                        Box(-3.5, -5.5, -1, 7, 2, 5, 'plate', 'plate_b'), Box(-0.5, -10.5, -2, 1, 5, 7, 'fin', 'dorsal')])
M.part('head', 'all', (0, 0, -7), boxes=[Box(-4, -4.5, -7, 8, 8, 7, 'plate', 'skull'), Box(-3, -2.5, -13, 6, 4, 6, 'plate', 'snout')])
M.part('jaw', 'head', (0, 2.5, -1), (0.1, 0, 0), boxes=[Box(-3, 0, -12, 6, 2, 12, 'plate', 'jaw'), Box(-3, -2, -12.5, 6, 2, 1, 'tooth', 'fangs')])
M.part('right_fin', 'body', (-3.5, 2.5, -4), (0, 0.2, 0.45), boxes=[Box(-6, 0, -2, 6, 1, 5, 'fin', 'pectoral')])
M.mirror('right_fin', 'left_fin', parent='body')
M.part('tail', 'all', (0, 0, 5), boxes=[Box(-2.5, -3.5, 0, 5, 7, 8, 'scale', 'tail_a'), Box(-2, -4.5, 1, 4, 1, 5, 'plate', 'plate_c')])
M.part('tail_tip', 'tail', (0, 0, 8), boxes=[Box(-1.5, -2.5, 0, 3, 5, 6, 'scale', 'tail_b'), Box(-0.5, -7, 4, 1, 14, 7, 'fin', 'caudal')])
M.pack()

SCALE = (58, 70, 52)
SCALE_D = (38, 48, 38)
SCALE_L = (84, 98, 66)
BELLY = (150, 150, 112)
BONE = (196, 188, 158)
BONE_D = (140, 132, 108)
BONE_DD = (92, 86, 72)
BONE_L = (226, 220, 194)
FIN = (74, 92, 70)
FIN_D = (44, 58, 48)
MOUTH = (88, 32, 40)
EYE = (214, 255, 190)
BLACK = (18, 20, 16)


def scale(face, px, py, fw, fh, rnd):
    f = {'top': 1.1, 'bottom': 1.0}.get(face, 1.08 - 0.22 * py / max(1, fh - 1))
    amount = blotch(f'scale{fw}{fh}{face}', px, py, 2.6)
    c = SCALE_D if amount > 0.62 else (SCALE_L if amount < 0.22 else SCALE)
    if (px + py * 2) % 4 == 0:
        c = shade(c, 0.82)                       # the rows of scales
    if face == 'bottom' or (face in ('right', 'left') and py >= fh - 3):
        c = mix(c, BELLY, 0.7 if face == 'bottom' else 0.25 * (py - fh + 4))
    return shade(c, f)


def bone(tag):
    def fn(face, px, py, fw, fh, rnd):
        f = {'top': 1.1, 'bottom': 0.72}.get(face, 1.02 - 0.16 * py / max(1, fh - 1))
        amount = blotch(f'bone{tag}{face}', px, py, 2.4)
        c = BONE_D if amount > 0.64 else (BONE_L if amount < 0.2 else BONE)
        if abs(blotch(f'seam{tag}{face}', px, py, 3.0) - 0.5) < 0.04:
            c = BONE_DD                          # the seams between the plates, and where one has cracked
        if min(fw, fh) > 2 and (px in (0, fw - 1) or py in (0, fh - 1)):
            c = shade(c, 0.84)
        return shade(c, f)
    return fn


def fin(tag):
    def fn(face, px, py, fw, fh, rnd):
        if face not in ('right', 'left') and tag != 'pectoral':
            return shade(FIN_D, 0.9) if (px + py) % 2 == 0 else None
        if tag == 'pectoral' and face not in ('top', 'bottom'):
            return shade(FIN_D, 0.9)
        # rays, and between them a web that is torn at the edge
        along, across, long = (px, py, fw) if tag != 'pectoral' else (py, px, fh)
        ray = along % 2 == 0
        if tag == 'dorsal':
            if across < (2 if along % 4 else 0) + (1 if blotch('dorsal', along, 0, 1.5) > 0.6 else 0):
                return None
        elif tag == 'caudal':
            mid = abs(across - (fh - 1) / 2)
            if mid < 1.5 and along > long - 4:
                return None                      # the notch of the tail
            if mid > 3 + along * 0.7 or (not ray and along == long - 1):
                return None
        else:
            if across < 1 and not ray:
                return None
        return shade(FIN if ray else mix(FIN, SCALE_L, 0.35), 1.0 if ray else 0.9)
    return fn


def make():
    p = Painter(M, 71)
    boxes = {b.tag: b for _, _, b in M.boxes()}
    for _, _, b in M.boxes():
        if b.mat == 'scale':
            p.fill_box(b, scale)
        elif b.mat == 'plate':
            p.fill_box(b, bone(b.tag))
        elif b.mat == 'fin':
            p.fill_box(b, fin(b.tag))
        elif b.mat == 'tooth':
            p.fill_box(b, lambda face, px, py, fw, fh, r: (shade(BONE_L, 1.0 if py == 0 else 0.85) if (px % 2 == 0 and py <= (1 if px in (0, fw - 1) else 0)) or py == 0 and face in ('right', 'left') else None)
                       if face in ('front', 'back', 'right', 'left') else None)
    skull, snout, jaw = boxes['skull'], boxes['snout'], boxes['jaw']
    # the eye: a pit in the shield, far forward, with a pale light at the bottom of it
    for side in ('right', 'left'):
        ex = 1 if side == 'right' else 5
        for dx in (-1, 0, 1):
            for dy in (-1, 0, 1):
                p.put(skull, side, ex + dx, 2 + dy, BLACK if abs(dx) + abs(dy) < 2 else BONE_DD)
        p.put(skull, side, ex, 2, EYE, glow=True)
        # the edge of the upper blade, and the dark of the mouth behind it
        for x in range(6):
            p.put(snout, side, x, 3, BONE_L if x % 3 else BONE_DD)
        for x in range(12):
            p.put(jaw, side, x, 0, BONE_L if x % 3 else BONE_DD)
    for x in range(6):
        p.put(snout, 'front', x, 3, BONE_L)
        p.put(jaw, 'front', x, 0, BONE_L)
    fx, fy, fw, fh = jaw.faces()['top']
    for x in range(fw):
        for y in range(fh):
            p.px(fx + x, fy + y, MOUTH if 0 < x < fw - 1 and y > 0 else BONE_L)
    fx, fy, fw, fh = snout.faces()['bottom']
    for x in range(fw):
        for y in range(fh):
            p.px(fx + x, fy + y, shade(MOUTH, 0.8) if 0 < x < fw - 1 else BONE_L)
    # gill slits behind the shield, an old hook still in the flank
    trunk = boxes['trunk']
    for side in ('right', 'left'):
        for i in (0, 2):
            x = i if side == 'right' else 11 - i
            for y in range(2, 7):
                p.put(trunk, side, x, y, SCALE_D if y % 2 else BLACK)
    for x, y in ((7, 3), (8, 3), (8, 4), (8, 5), (7, 5)):
        p.put(trunk, 'right', x, y, (120, 112, 104))
    return p.tex, p.glow


STAND = {}
SWIM = {'tail': (0, 0.4, 0), 'tail_tip': (0, 0.5, 0), 'head': (0, -0.1, 0)}
BITE = {'jaw': (0.9, 0, 0), 'all': {'off': (0, 0, -4)}, 'head': (-0.2, 0, 0)}
LEAP = {'all': {'rot': (-0.7, 0, 0), 'off': (0, -8, 0)}, 'jaw': (0.9, 0, 0), 'tail': (0.3, 0, 0), 'right_fin': (0, 0, 0.5), 'left_fin': (0, 0, -0.5)}
FLOP = {'all': {'rot': (0, 0, 1.45), 'off': (0, 2.5, 0)}, 'tail': (0, 0.8, 0), 'tail_tip': (0, 0.6, 0), 'head': (0, -0.4, 0), 'jaw': (0.5, 0, 0)}

if __name__ == '__main__':
    M.write_java(JAVA) if len(sys.argv) < 3 else None
    tex, glow = make()
    tex.save(f'{A}/textures/entity/bone_pike.png')
    glow.save(f'{A}/textures/entity/bone_pike_glow.png')
    if len(sys.argv) > 1:
        shots = [M.render(tex, yaw=yaw, pose=pose, size=(320, 240), zoom=8.0, centre=(0, 16), glow=glow)
                 for yaw, pose in ((-0.7, STAND), (0.0, STAND), (1.5, SWIM), (2.6, STAND), (-0.8, BITE), (-1.2, LEAP), (-0.9, FLOP), (-1.57, STAND))]
        sheet(shots, 4).save(sys.argv[1])
