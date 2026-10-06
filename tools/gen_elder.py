#!/usr/bin/env python3
"""The Elder of the Unsleeping: mesh for ElderModel.java, his texture and two glows - what shines while he keeps his
vigil (his eyes, the candles of his crown, the great candle) and what is left when he sleeps (the great candle).
Run from the repo root; pass a file name to also get a preview sheet."""
import math
import sys

from modelkit import Box, Model, Painter, blotch, mix, shade, sheet

A = 'src/main/resources/assets/vsratie_dushi'
JAVA = 'src/main/java/com/keykeeper/dushi/client/ElderModel.java'
SCALE = 1.35          # must match MODEL_SCALE in ElderModel.java

M = Model(128, 128, SCALE)
# The one who made them swear. Half as tall again as a man and no wider: a white robe to the ground with nothing to be
# seen under it, sleeves that hang past his hands, white hair to the shoulders and a beard to the waist; on his head
# an iron ring with six candles alight in it; in his right hand a candlestick taller than himself with a candle as
# thick as an arm burning at the top. The trunk turns about the hips; the skirt of the robe stands on the ground and
# does not turn with it. A held thing lies across the forearm and points forward when the arm hangs.
HIP = 8
M.part('skirt', None, (0, HIP, 0), boxes=[Box(-4.5, 0, -3, 9, 16, 6, 'robe', 'skirt')])
M.part('torso', None, (0, HIP, 0), (0.08, 0, 0), boxes=[Box(-4, -13, -2, 8, 13, 4, 'robe', 'chest')])
M.part('head', 'torso', (0, -13, 0), (-0.04, 0, 0), boxes=[Box(-3.5, -8, -3.5, 7, 8, 7, 'head', 'head'), Box(-4, -8.5, -4, 8, 13, 8, 'hair', 'hair')])
M.part('beard', 'head', (0, -1.5, -3.6), boxes=[Box(-2.5, 0, -1, 5, 11, 1, 'beard', 'beard')])
crown = [Box(-4.5, -1, -4.5, 9, 1, 9, 'iron', 'ring')]
for i, (x, z, h) in enumerate(((-4.5, -4.5, 4), (3.5, -4.5, 3), (-4.5, 3.5, 3), (3.5, 3.5, 5), (-0.5, -4.5, 5), (-0.5, 3.5, 4))):
    crown.append(Box(x, -1 - h, z, 1, h, 1, 'wax', f'crown_stub{i}'))
    crown.append(Box(x, -3 - h, z, 1, 2, 1, 'crown_flame', f'crown_flame{i}'))
M.part('crown', 'head', (0, -8, 0), boxes=crown)
M.part('right_arm', 'torso', (-5.5, -11.5, 0), boxes=[Box(-1.5, -1.5, -1.5, 3, 9, 3, 'robe', 'upper')])
M.part('right_forearm', 'right_arm', (0, 7.5, 0), (-0.12, 0, 0), boxes=[Box(-1.5, 0, -1.5, 3, 9, 3, 'robe', 'fore'), Box(-2, 3, -1.5, 4, 5, 5, 'robe', 'cuff')])
M.mirror('right_arm', 'left_arm')
M.mirror('right_forearm', 'left_forearm')
M.part('staff', 'right_forearm', (0, 8, 0), (math.pi / 2, 0, 0), boxes=[Box(-0.5, -26, -0.5, 1, 35, 1, 'iron', 'shaft'), Box(-2, -27, -2, 4, 1, 4, 'iron', 'pan'),
                                                                        Box(-1.5, -36, -1.5, 3, 9, 3, 'wax', 'great_candle'),
                                                                        Box(-1, -39, -1, 2, 3, 2, 'flame', 'great_flame')])
M.pack()

ROBE = (226, 220, 204)
ROBE_D = (184, 176, 160)
ROBE_DD = (140, 132, 120)
SKIN = (156, 146, 130)
SKIN_D = (116, 106, 94)
SKIN_DD = (80, 72, 66)
HAIR = (206, 204, 196)
HAIR_D = (158, 156, 150)
IRON = (74, 74, 82)
IRON_D = (46, 46, 54)
WAXC = (236, 226, 190)
WAX_D = (196, 180, 136)
SOOT = (58, 52, 48)
FLAME = (255, 214, 120)
FLAME_L = (255, 246, 208)
FLAME_D = (255, 150, 52)
EYE = (240, 244, 236)
THREAD = (150, 44, 40)
BLACK = (16, 14, 16)


def robe(tag):
    def fn(face, px, py, fw, fh, rnd):
        f = {'top': 1.08, 'bottom': 0.7}.get(face, 1.04 - 0.16 * py / max(1, fh - 1))
        c = ROBE_D if blotch(f'robe{tag}{face}', px, py * 0.4, 2.0) > 0.6 else ROBE
        if face not in ('top', 'bottom') and tag in ('skirt', 'cuff') and px % 3 == 1:
            c = mix(c, ROBE_DD, 0.4)                             # the folds of it
        if tag == 'skirt' and face not in ('top', 'bottom') and py >= fh - 3 and blotch(f'hem{face}', px, py, 1.5) > 0.45:
            c = mix(c, (96, 86, 70), 0.6)                        # the hem has swept the ground a long time
        if tag in ('chest', 'skirt') and face == 'front':
            mid = fw / 2 - 0.5
            if abs(px - mid) < 1.6 and (tag == 'chest' or py < fh - 3):
                # the band down the front of it, and on the band the sign of the skete worked in red thread: an open eye
                c = ROBE_DD if abs(px - mid) > 1.0 else ROBE_D
                row = py % 5
                if abs(px - mid) < 1.0 and row in (1, 2):
                    c = THREAD
        return shade(c, f)
    return fn


def make():
    p = Painter(M, 181)
    boxes = {b.tag: b for _, _, b in M.boxes()}
    for name, _, b in M.boxes():
        if b.mat == 'robe':
            p.fill_box(b, robe(b.tag))
        elif b.mat == 'head':
            p.fill_box(b, lambda face, px, py, fw, fh, r: shade(SKIN_D if blotch(f'sk{face}', px, py, 1.7) > 0.6 else SKIN, {'top': 1.05, 'bottom': 0.7}.get(face, 1.0 - 0.02 * py)))
        elif b.mat == 'hair':
            def hair(face, px, py, fw, fh, rnd):
                if face == 'bottom':
                    return None
                if face == 'front' and (1 <= px <= fw - 2 and py >= 2 or py >= 9):
                    return None                                  # the face, and the beard's place under it
                long = fh - int(blotch(f'hl{face}', px, 0, 1.0) * 3)
                if face != 'top' and py >= long:
                    return None
                c = HAIR_D if px % 2 else HAIR
                return shade(c, {'top': 1.1}.get(face, 1.04 - 0.02 * py))
            p.fill_box(b, hair)
        elif b.mat == 'beard':
            def beard(face, px, py, fw, fh, rnd):
                if face in ('top', 'bottom'):
                    return HAIR_D
                narrow = int(py * 0.22)
                if face in ('front', 'back') and (px < narrow or px >= fw - narrow):
                    return None
                return shade(HAIR_D if px % 2 == py % 3 % 2 else HAIR, 1.02 - 0.02 * py)
            p.fill_box(b, beard)
        elif b.mat == 'iron':
            p.fill_box(b, lambda face, px, py, fw, fh, r: shade(IRON_D if (py % 7 == 3 and fh > 8) else IRON, {'top': 1.15, 'bottom': 0.7}.get(face, 1.0)))
        elif b.mat == 'wax':
            p.fill_box(b, lambda face, px, py, fw, fh, r: shade(WAXC if blotch(f'wx{fw}{fh}{face}', px, py * 0.4, 1.3) < 0.62 else WAX_D, {'top': 1.05, 'bottom': 0.8}.get(face, 1.0 - 0.02 * py)))
        elif b.mat in ('flame', 'crown_flame'):
            # (in the picture itself a flame is a dead wick: it is the glow that lights it, and only while it burns)
            p.fill_box(b, lambda face, px, py, fw, fh, r: SOOT if (px + py) % 2 else shade(SOOT, 1.4))
    # the face: under the lids. (His open eyes are in the glow: the lids are what is seen when it is gone.)
    head = boxes['head']
    p.art(head, 'front', ["sdsssds",
                          "dssssss",
                          "sDDsDDs",
                          "sBBdBBs",
                          "sdDsDds",
                          "ssdddss",
                          "sdBBBds",
                          "ddsssdd"], {'s': SKIN, 'd': SKIN_D, 'D': SKIN_DD, 'B': BLACK})
    awake = p.glow.copy()
    asleep = p.glow.copy()
    fx, fy, fw, fh = head.faces()['front']
    for x, y, c in ((1, 3, EYE), (2, 3, EYE), (4, 3, EYE), (5, 3, EYE)):
        awake.putpixel((fx + x, fy + y), c + (255,))
    for _, _, b in M.boxes():
        if b.mat in ('flame', 'crown_flame'):
            for face, (bx, by, bw, bh) in b.faces().items():
                for x in range(bw):
                    for y in range(bh):
                        c = FLAME_D if face == 'bottom' else (FLAME_L if y >= bh - 1 and face != 'top' else FLAME)
                        awake.putpixel((bx + x, by + y), c + (255,))
                        if b.mat == 'flame':
                            asleep.putpixel((bx + x, by + y), shade(c, 0.8) + (255,))
    return p.tex, awake, asleep


STAND = {'right_arm': (-0.75, 0, -0.08), 'right_forearm': (-0.68, 0, 0)}
# the poses of ElderModel.setupAnim (the staff is carried upright unless a pose says otherwise)
SWEEP_BACK = {'torso': (0.1, 0.8, 0), 'right_arm': (-0.35, 0.5, 0.15), 'right_forearm': (0.12, 0, 0), 'left_arm': (-0.6, 0, -0.3)}
SWEEP = {'torso': (0.2, -0.8, 0), 'right_arm': (-0.45, -0.5, 0.15), 'right_forearm': (0.12, 0, 0), 'left_arm': (0.4, 0, -0.5)}
THRUST_BACK = {'torso': (0.0, 0.45, 0), 'right_arm': (0.7, 0, 0.1), 'right_forearm': (0.12, 0, 0), 'left_arm': (-0.9, 0, 0)}
THRUST = {'torso': (0.35, -0.35, 0), 'right_arm': (-0.43, 0, 0.1), 'right_forearm': (0.12, 0, 0), 'left_arm': (0.5, 0, -0.2)}
RAIN = {'torso': (-0.15, 0, 0), 'head': (-0.4, 0, 0), 'right_arm': (-2.2, 0, -0.2), 'right_forearm': (0.8, 0, 0), 'left_arm': (-2.6, 0, 0.35), 'left_forearm': (-0.2, 0, 0)}
CALL = {'torso': (0.1, 0, 0), 'head': (-0.3, 0, 0), 'right_arm': (-1.1, 0, -0.1), 'right_forearm': (-0.35, 0, 0), 'left_arm': (-1.9, 0, 0.9), 'left_forearm': (-0.6, 0, 0)}
ASLEEP = {'torso': (0.3, 0, 0.08), 'head': (0.4, 0.15, 0.3), 'right_arm': (-0.5, 0, 0.1), 'right_forearm': (-0.3, 0, 0), 'left_arm': (0.1, 0, -0.1)}
DREAM = {'torso': (0.36, 0, 0.0), 'head': (0.5, 0, 0.35), 'right_arm': (-0.85, 0, -0.5), 'right_forearm': (-0.75, 0, 0), 'left_arm': (-0.9, 0, 0.5), 'left_forearm': (-0.9, 0, 0)}

if __name__ == '__main__':
    M.write_java(JAVA) if len(sys.argv) < 3 else None
    tex, awake, asleep = make()
    tex.save(f'{A}/textures/entity/elder.png')
    awake.save(f'{A}/textures/entity/elder_glow.png')
    asleep.save(f'{A}/textures/entity/elder_asleep_glow.png')
    if len(sys.argv) > 1:
        shots = [M.render(tex, yaw=yaw, pose=pose, size=(330, 470), zoom=5.6, centre=(0, -8), glow=glow, background=(52, 62, 60))
                 for yaw, pose, glow in ((-0.5, STAND, awake), (0.0, STAND, awake), (1.5, STAND, awake), (2.7, STAND, awake), (-0.6, SWEEP_BACK, awake), (-0.6, SWEEP, awake),
                                         (-1.3, THRUST_BACK, awake), (-1.3, THRUST, awake), (-0.8, RAIN, awake), (-0.6, CALL, awake), (-0.5, ASLEEP, asleep),
                                         (-0.6, DREAM, asleep))]
        sheet(shots, 6).save(sys.argv[1])
