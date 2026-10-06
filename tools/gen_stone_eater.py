#!/usr/bin/env python3
"""The Stone-Eater of the Breach: mesh for StoneEaterModel.java, its texture and the glow of the lichen on its plates.
Run from the repo root; pass a file name to also get a preview sheet."""
import sys

from modelkit import Box, Model, Painter, blotch, mix, shade, sheet

A = 'src/main/resources/assets/vsratie_dushi'
JAVA = 'src/main/java/com/keykeeper/dushi/client/StoneEaterModel.java'
SCALE = 1.9           # must match MODEL_SCALE in StoneEaterModel.java

M = Model(128, 128, SCALE)
# a burrower the size of a wagon: a low heavy trunk under plates of stone, a blunt eyeless head that is mostly jaw, two
# digging arms that end in three blades each, short hind legs, a tail with a knot of rock at its end.
# Everything hangs from 'all', so that the whole of it can sink into the floor.
M.part('all', None, (0, 0, 0))
M.part('body', 'all', (0, 12, 0), boxes=[Box(-9, -7, -12, 18, 13, 22, 'hide', 'trunk'),
                                         Box(-7, -10, -11, 14, 3, 7, 'plate', 'plate_a'), Box(-8, -11, -4, 16, 4, 8, 'plate', 'plate_b'),
                                         Box(-6, -9, 4, 12, 2, 6, 'plate', 'plate_c')])
M.part('head', 'body', (0, -1, -12), (0.1, 0, 0), boxes=[Box(-6, -5, -9, 12, 8, 9, 'hide', 'skull'), Box(-4.5, -3, -14, 9, 6, 5, 'hide', 'snout'),
                                                         Box(-5, -7, -8, 10, 2, 8, 'plate', 'crest')])
M.part('jaw', 'head', (0, 3, -1), (0.12, 0, 0), boxes=[Box(-5, 0, -12, 10, 3, 12, 'hide', 'jaw'), Box(-4.5, -2, -12.5, 9, 2, 1, 'tooth', 'teeth')])
M.part('right_arm', 'body', (-9, -3, -8), (0.15, 0, 0.35), boxes=[Box(-5, -2, -3.5, 5, 11, 7, 'hide', 'shoulder')])
M.part('right_forearm', 'right_arm', (-2.5, 8.5, 0), (-0.5, 0, -0.3), boxes=[Box(-2.5, 0, -2.5, 5, 9, 5, 'hide', 'fore')])
M.part('right_claw', 'right_forearm', (0, 9, 0), (0.55, 0, 0), boxes=[Box(-3.5, -1, -3, 7, 3, 5, 'hide', 'paw'),
                                                                       Box(-3.5, 0, -12, 2, 2, 9, 'claw', 'blade_a'), Box(-1, 0, -13, 2, 2, 10, 'claw', 'blade_b'),
                                                                       Box(1.5, 0, -12, 2, 2, 9, 'claw', 'blade_c')])
M.mirror('right_arm', 'left_arm')
M.mirror('right_forearm', 'left_forearm')
M.mirror('right_claw', 'left_claw')
M.part('right_leg', 'all', (-8, 14, 7), boxes=[Box(-3, -2, -3.5, 5, 9, 7, 'hide', 'thigh'), Box(-3, 7, -5, 5, 3, 7, 'hide', 'foot')])
M.mirror('right_leg', 'left_leg', parent='all')
M.part('tail', 'body', (0, -1, 10), (-0.25, 0, 0), boxes=[Box(-3, -3, 0, 6, 6, 10, 'hide', 'tail_a')])
M.part('tail_tip', 'tail', (0, 0, 10), (0.2, 0, 0), boxes=[Box(-2, -2, 0, 4, 4, 9, 'hide', 'tail_b'), Box(-4, -4, 8, 8, 8, 7, 'plate', 'club')])
M.pack()

HIDE = (96, 90, 96)
HIDE_D = (70, 64, 72)
HIDE_DD = (46, 42, 50)
HIDE_L = (124, 118, 120)
BELLY = (150, 138, 128)
PLATE = (146, 146, 140)
PLATE_D = (104, 104, 102)
PLATE_L = (182, 182, 172)
LICHEN = (120, 220, 190)
LICHEN_L = (190, 255, 230)
CLAW = (214, 206, 180)
CLAW_D = (150, 140, 118)
MOUTH = (96, 30, 40)
TOOTH = (226, 220, 196)
BLACK = (18, 16, 20)


def hide(face, px, py, fw, fh, rnd):
    f = {'top': 1.1, 'bottom': 0.72}.get(face, 1.06 - 0.24 * py / max(1, fh - 1))
    amount = blotch(f'hide{fw}{fh}{face}', px, py, 3.4)
    c = HIDE_D if amount > 0.62 else (HIDE_L if amount < 0.2 else HIDE)
    if (px * 2 + py) % 7 == 0:
        c = shade(c, 0.85)                       # the folds of a thick skin
    if face == 'bottom' or face not in ('top',) and py >= fh - 2 and fh > 6:
        c = mix(c, BELLY, 0.45)
    if min(fw, fh) > 3 and (px in (0, fw - 1)):
        c = shade(c, 0.86)
    return shade(c, f)


def plate(p, b, lit):
    """A slab of stone grown into the back: cracked, its edges broken, lichen in the cracks - and the lichen shines."""
    def fn(face, px, py, fw, fh, rnd):
        f = {'top': 1.12, 'bottom': 0.68}.get(face, 0.96)
        amount = blotch(f'plate{b.tag}{face}', px, py, 2.8)
        c = PLATE_D if amount > 0.64 else (PLATE_L if amount < 0.22 else PLATE)
        return shade(c, f)
    p.fill_box(b, fn)
    for face, (fx, fy, fw, fh) in b.faces().items():
        if face == 'bottom':
            continue
        for x in range(fw):
            for y in range(fh):
                crack = abs(blotch(f'crack{b.tag}{face}', x, y, 2.2) - 0.5) < 0.045
                if crack:
                    c = LICHEN_L if blotch(f'l{b.tag}{face}', x, y, 1.2) > 0.6 else LICHEN
                    p.px(fx + x, fy + y, c)
                    if lit:
                        p.glow.putpixel((fx + x, fy + y), c + (255,))


def make():
    p = Painter(M, 97)
    boxes = {b.tag: b for _, _, b in M.boxes()}
    for _, _, b in M.boxes():
        if b.mat == 'hide':
            p.fill_box(b, hide)
        elif b.mat == 'plate':
            plate(p, b, True)
        elif b.mat == 'claw':
            p.fill_box(b, lambda face, px, py, fw, fh, r: shade(CLAW_D if (face in ('top', 'bottom') and py >= fh - 2) or (face in ('right', 'left') and px >= fw - 2
                                                                           if face == 'right' else px < 2 and face == 'left') else CLAW,
                                                                {'top': 1.1, 'bottom': 0.7}.get(face, 0.95)))
        elif b.mat == 'tooth':
            p.fill_box(b, lambda face, px, py, fw, fh, r: (TOOTH if px % 2 == 0 else None) if face in ('front', 'back') else (TOOTH if face in ('right', 'left') else None))
    # the head: no eyes and no place for them, not so much as a nostril to be taken for one; the lips drawn back off the teeth
    skull, snout, jaw = boxes['skull'], boxes['snout'], boxes['jaw']
    for side in ('right', 'left'):
        for x in range(5):
            p.put(snout, side, x, 5, TOOTH if x % 2 else MOUTH)
            p.put(snout, side, x, 4, HIDE_DD)
        for x in range(12):
            p.put(jaw, side, x, 0, MOUTH if x % 2 else TOOTH)
    for x in range(9):
        p.put(snout, 'front', x, 5, TOOTH if x % 2 else MOUTH)
        p.put(snout, 'front', x, 4, HIDE_DD)
    fx, fy, fw, fh = jaw.faces()['top']
    for x in range(fw):
        for y in range(fh):
            p.px(fx + x, fy + y, MOUTH if 0 < x < fw - 1 and y > 0 else shade(TOOTH, 0.9 if (x + y) % 2 else 0.7))
    fx, fy, fw, fh = snout.faces()['bottom']
    for x in range(fw):
        for y in range(fh):
            p.px(fx + x, fy + y, shade(MOUTH, 0.8))
    # scars across the flank where the drift's timbers fell on it
    trunk = boxes['trunk']
    for i in range(7):
        p.put(trunk, 'right', 6 + i, 4 + i // 2, HIDE_DD)
        p.put(trunk, 'right', 7 + i, 4 + i // 2, BELLY)
        p.put(trunk, 'left', 14 - i, 5 + i // 3, HIDE_DD)
    return p.tex, p.glow


STAND = {}
CLAW_UP = {'right_arm': (-1.5, 0, 0.9), 'right_forearm': (0.3, 0, 0.2), 'right_claw': (-0.4, 0, 0), 'body': (-0.12, 0.25, 0), 'jaw': (0.5, 0, 0)}
CLAW_DOWN = {'right_arm': (-0.5, 0.2, -0.4), 'right_forearm': (0.5, 0, 0.3), 'body': (0.1, -0.3, 0), 'jaw': (0.3, 0, 0)}
ROAR = {'body': {'rot': (-0.5, 0, 0), 'off': (0, -4, 0)}, 'head': (-0.35, 0, 0), 'jaw': (0.9, 0, 0), 'right_arm': (-0.6, 0, 0.9), 'left_arm': (-0.6, 0, -0.9),
        'tail': (0.5, 0, 0)}
LUNGE = {'body': {'rot': (0.08, 0, 0), 'off': (0, 1, -4)}, 'head': (-0.2, 0, 0), 'jaw': (1.0, 0, 0), 'right_arm': (0.9, 0, 0.3), 'left_arm': (0.9, 0, -0.3)}
TAIL = {'tail': (0.25, 1.2, 0), 'tail_tip': (0, 0.7, 0), 'body': (0, -0.35, 0), 'head': (0, 0.5, 0)}
SINK = {'all': {'off': (0, 14, 0)}, 'right_arm': (-1.2, 0, 0.5), 'left_arm': (0.4, 0, -0.5), 'body': (0.25, 0, 0)}

if __name__ == '__main__':
    M.write_java(JAVA) if len(sys.argv) < 3 else None
    tex, glow = make()
    tex.save(f'{A}/textures/entity/stone_eater.png')
    glow.save(f'{A}/textures/entity/stone_eater_glow.png')
    if len(sys.argv) > 1:
        shots = [M.render(tex, yaw=yaw, pose=pose, size=(420, 330), zoom=3.7, centre=(0, 2), glow=glow)
                 for yaw, pose in ((-0.6, STAND), (0.0, STAND), (1.5, STAND), (2.7, STAND), (-0.7, CLAW_UP), (-0.7, CLAW_DOWN), (-0.9, ROAR), (-1.2, LUNGE),
                                   (2.4, TAIL), (-0.6, SINK))]
        sheet(shots, 4).save(sys.argv[1])
