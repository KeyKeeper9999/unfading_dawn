#!/usr/bin/env python3
"""The blind crawler of the lower workings: mesh for BlindCrawlerModel.java and its texture.
Run from the repo root; pass a file name to also get a preview sheet."""
import sys

from modelkit import Box, Model, Painter, blotch, mix, shade, sheet

A = 'src/main/resources/assets/vsratie_dushi'
JAVA = 'src/main/java/com/keykeeper/dushi/client/BlindCrawlerModel.java'

M = Model(64, 64)
# what is left of a man after an age in the dark: on all fours, the arms grown long, the legs folded under, the head
# hung forward with nothing where the eyes were and ears like a bat's
M.part('body', None, (0, 13, 1), (0.16, 0, 0), boxes=[Box(-4, -4, -8, 8, 7, 9, 'skin', 'ribs'), Box(-3, -3.5, 1, 6, 5, 7, 'skin', 'loin'),
                                                       Box(-0.5, -5, -7, 1, 1, 14, 'bone', 'spine')])
M.part('head', 'body', (0, -1, -8), (0.1, 0, 0), boxes=[Box(-3.5, -4, -6, 7, 6, 6, 'skin', 'skull')])
M.part('jaw', 'head', (0, 2, -1), (0.15, 0, 0), boxes=[Box(-3, 0, -5, 6, 2, 5, 'skin', 'jaw')])
M.part('right_ear', 'head', (-3.5, -3, -1.5), (0, 0.5, -0.35), boxes=[Box(-5, -6, 0, 5, 7, 1, 'ear', 'ear')])
M.mirror('right_ear', 'left_ear', parent='head')
M.part('right_arm', 'body', (-4, -2, -6), (0.2, 0, 0.75), boxes=[Box(-1.5, -1, -1.5, 3, 9, 3, 'skin', 'upper')])
M.part('right_forearm', 'right_arm', (0, 8, 0), (-0.75, 0, -0.75), boxes=[Box(-1.5, 0, -1.5, 3, 10, 3, 'skin', 'fore'), Box(-2, 10, -4, 4, 1, 5, 'claw', 'hand')])
M.mirror('right_arm', 'left_arm')
M.mirror('right_forearm', 'left_forearm')
M.part('right_leg', 'body', (-3, 0, 6), (-1.25, 0, 0.3), boxes=[Box(-1.5, -1, -1.5, 3, 8, 3, 'skin', 'thigh')])
M.part('right_shin', 'right_leg', (0, 7, 0), (1.95, 0, 0), boxes=[Box(-1.5, 0, -1.5, 3, 7, 3, 'skin', 'shin'), Box(-1.5, 6, -3.5, 3, 1, 3, 'claw', 'foot')])
M.mirror('right_leg', 'left_leg')
M.mirror('right_shin', 'left_shin')
M.pack()

SKIN = (188, 180, 170)
SKIN_D = (150, 140, 134)
SKIN_DD = (112, 100, 100)
SKIN_L = (212, 206, 196)
VEIN = (134, 128, 150)
BONE = (224, 216, 196)
MOUTH = (70, 28, 34)
EAR_IN = (176, 128, 128)
CLAW = (64, 56, 52)
BLACK = (24, 18, 20)


def skin(face, px, py, fw, fh, rnd):
    f = {'top': 1.08, 'bottom': 0.72}.get(face, 1.04 - 0.2 * py / max(1, fh - 1))
    amount = blotch(f'skin{fw}{fh}{face}', px, py, 2.8)
    c = SKIN_D if amount > 0.66 else (SKIN_L if amount < 0.2 else SKIN)
    if rnd.random() < 0.05:
        c = VEIN
    if min(fw, fh) > 2 and (px in (0, fw - 1) or py == fh - 1):
        c = shade(c, 0.86)
    return shade(c, f)


def make():
    p = Painter(M, 61)
    boxes = {b.tag: b for _, _, b in M.boxes()}
    for name, _, b in M.boxes():
        if b.mat == 'bone':
            p.fill_box(b, lambda face, px, py, fw, fh, r: shade(BONE if (px + py) % 3 else SKIN_D, 1.0 if face == 'top' else 0.8))
        elif b.mat == 'claw':
            p.fill_box(b, lambda face, px, py, fw, fh, r: (shade(CLAW, 1.2 if face == 'top' else 0.9) if (px % 2 == 0 or face not in ('top', 'bottom')) else None))
        elif b.mat == 'ear':
            def ear(face, px, py, fw, fh, r, mirrored=name.startswith('left')):
                x = fw - 1 - px if mirrored != (face == 'back') else px
                # a torn triangle: wide at the skull, pointed above
                if face in ('front', 'back'):
                    if py < 2 and x < 2 or py < 1 and x < 4 or py > 4 and x < 2 or py == fh - 1 and x < 3 or (x, py) in ((0, 3), (1, 2)):
                        return None
                    inner = EAR_IN if face == 'front' else SKIN_D
                    return shade(inner if 1 < py < 6 and x > 1 else SKIN_D, 1.0 - 0.06 * py)
                return None
            p.fill_box(b, ear)
        else:
            p.fill_box(b, skin)
    # the ribs through both flanks
    ribs = boxes['ribs']
    for side in ('right', 'left'):
        for x in (1, 3, 5, 7):
            for y in range(1, 6):
                p.put(ribs, side, x, y, SKIN_L if y % 2 else SKIN)
                if x < 8:
                    p.put(ribs, side, x + 1, y, SKIN_DD)
    # the head: skin grown smooth over the sockets, two slits for a nose, a mouth too wide and full of needles
    skull, jaw = boxes['skull'], boxes['jaw']
    p.art(skull, 'front', ["sssssss",
                           "sdsssds",
                           "dDdsdDd",
                           "sdsssds",
                           "ssNsNss",
                           "MtMtMtM"], {'s': SKIN, 'd': SKIN_D, 'D': SKIN_DD, 'N': BLACK, 'M': MOUTH, 't': BONE})
    for side in ('right', 'left'):
        for x in range(6):
            p.put(skull, side, x, 5, BONE if x % 2 else MOUTH)
            p.put(jaw, side, x, 0, MOUTH if x % 2 else BONE)
    for x in range(6):
        p.put(jaw, 'front', x, 0, BONE if x % 2 else MOUTH)
        for z in range(5):
            p.put(jaw, 'top', x, z, MOUTH if 0 < x < 5 and z > 0 else shade(BONE, 0.9))
    # knuckles and knees worn dark from the floor
    for name in ('right_forearm', 'left_forearm', 'right_shin', 'left_shin'):
        b = M.parts[name]['boxes'][0]
        p.band(b, -1, SKIN_DD)
        p.band(b, -2, SKIN_D)
        p.band(b, 0, SKIN_D)
    return p.tex, p.glow


STAND = {}
LISTEN = {'head': (-0.35, 0.5, 0.2), 'right_ear': (0, -0.3, 0), 'left_ear': (0, 0.3, 0)}
SHRIEK = {'body': (-0.35, 0, 0), 'head': (-0.6, 0, 0), 'jaw': (0.9, 0, 0), 'right_ear': (0, -0.5, 0), 'left_ear': (0, 0.5, 0),
          'right_arm': (0.3, 0, 0), 'left_arm': (0.3, 0, 0)}
RAKE = {'body': (-0.1, 0.3, 0), 'right_arm': (-1.9, 0, -0.3), 'right_forearm': (0.5, 0, 0.5), 'jaw': (0.5, 0, 0)}
LEAP = {'body': {'rot': (-0.3, 0, 0), 'off': (0, -3, 0)}, 'right_arm': (-1.7, 0, -0.4), 'left_arm': (-1.7, 0, 0.4), 'right_forearm': (0.6, 0, 0.6),
        'left_forearm': (0.6, 0, -0.6), 'right_leg': (1.3, 0, 0), 'left_leg': (1.3, 0, 0), 'right_shin': (-1.2, 0, 0), 'left_shin': (-1.2, 0, 0), 'jaw': (0.8, 0, 0)}

if __name__ == '__main__':
    M.write_java(JAVA) if len(sys.argv) < 3 else None
    tex, glow = make()
    tex.save(f'{A}/textures/entity/blind_crawler.png')
    if len(sys.argv) > 1:
        shots = [M.render(tex, yaw=yaw, pose=pose, size=(300, 280), zoom=8.5, centre=(0, 12))
                 for yaw, pose in ((-0.6, STAND), (0.0, STAND), (1.4, STAND), (2.7, STAND), (-0.7, LISTEN), (-0.8, SHRIEK), (-0.6, RAKE), (-1.2, LEAP))]
        sheet(shots, 4).save(sys.argv[1])
