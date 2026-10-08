#!/usr/bin/env python3
"""The sungazer of the Stubble - a sunflower as tall as a man, that walks on its roots and burns what it looks at:
mesh for SungazerModel.java, its texture and the glow of its face.
Run from the repo root; pass a file name to also get a preview sheet."""
import math
import sys

from modelkit import Box, Model, Painter, blotch, mix, shade, sheet

A = 'src/main/resources/assets/unfading_dawn'
JAVA = 'src/main/java/com/keykeeper/dushi/client/SungazerModel.java'
SCALE = 1.1           # must match MODEL_SCALE in SungazerModel.java

M = Model(64, 64, SCALE)
# Three roots it has pulled out of the ground and goes on; a stalk as thick as a wrist, hairy; two great leaves it
# beats with and two lesser; a neck that bows under the head; and the head - a disc of seed a foot and a half across
# in a ring of petals, which is too bright to look into. Everything hangs from 'all'. It faces -z.
M.part('all', None, (0, 24, 0))
M.part('root_f', 'all', (0, -4, -1), (-0.6, 0, 0), boxes=[Box(-1, 0, -1, 2, 6, 2, 'root', 'root')])
M.part('root_r', 'all', (-1, -4, 1), (0.45, 0, 0.6), boxes=[Box(-1, 0, -1, 2, 6, 2, 'root', 'root')])
M.part('root_l', 'all', (1, -4, 1), (0.45, 0, -0.6), boxes=[Box(-1, 0, -1, 2, 6, 2, 'root', 'root')])
M.part('stem', 'all', (0, -3, 0), boxes=[Box(-1, -15, -1, 2, 15, 2, 'stalk', 'stem')])
M.part('leaf_r', 'stem', (-1, -9, 0), (0, 0, -0.35), boxes=[Box(-9, -0.5, -3, 9, 1, 6, 'leaf', 'leaf')])
M.mirror('leaf_r', 'leaf_l', parent='stem')
M.part('small_r', 'stem', (-1, -4, 0), (0, 0.5, -0.5), boxes=[Box(-6, -0.5, -2, 6, 1, 4, 'leaf', 'small')])
M.mirror('small_r', 'small_l', parent='stem')
M.part('neck', 'stem', (0, -15, 0), (0.35, 0, 0), boxes=[Box(-1, -8, -1, 2, 8, 2, 'stalk', 'neck')])
M.part('head', 'neck', (0, -8, 0), (-0.2, 0, 0), boxes=[Box(-5, -5, -2.5, 10, 10, 3, 'disc', 'disc'), Box(-9, -9, -1.5, 18, 18, 1, 'petals', 'petals'),
                                                        Box(-3, -3, 0.5, 6, 6, 2, 'calyx', 'calyx')])
M.pack()

GREEN = (96, 124, 54)
GREEN_D = (64, 88, 40)
GREEN_L = (134, 158, 76)
DRY = (150, 132, 70)            # the edges of everything, burnt by the sun it looks at
ROOT = (112, 88, 60)
ROOT_D = (78, 60, 42)
EARTH = (70, 52, 38)
PETAL = (244, 190, 44)
PETAL_D = (206, 140, 30)
PETAL_L = (255, 226, 110)
SEED = (70, 46, 28)
SEED_D = (44, 28, 18)
GLARE = (255, 244, 190)
GLARE_D = (255, 196, 90)


def make():
    p = Painter(M, 701)
    for name, _, b in M.boxes():
        if b.mat == 'stalk':
            def stalk(face, px, py, fw, fh, rnd):
                c = GREEN_D if blotch(f'sk{face}', px, py * 0.5, 1.6) > 0.55 else (GREEN_L if rnd.random() < 0.15 else GREEN)
                if rnd.random() < 0.08:
                    c = DRY
                return shade(c, {'top': 1.1, 'bottom': 0.7}.get(face, 1.0 if face in ('front', 'right') else 0.85))
            p.fill_box(b, stalk)
        elif b.mat == 'root':
            def root(face, px, py, fw, fh, rnd):
                c = ROOT_D if (py + px) % 3 == 0 else ROOT
                if py >= fh - 2:
                    c = mix(c, EARTH, 0.6)                       # the earth it came out of, still on it
                return shade(c, {'top': 1.1, 'bottom': 0.7}.get(face, 1.0))
            p.fill_box(b, root)
        elif b.mat == 'leaf':
            def leaf(face, px, py, fw, fh, rnd, mirrored=name.endswith('_l')):
                if face not in ('top', 'bottom'):
                    return None
                out = (fw - 1 - px) if not mirrored else px
                if face == 'bottom':
                    out = fw - 1 - out
                # a heart: broad by the stalk, drawn to a point
                half = (fh / 2) * (1.0 - (out / fw) ** 1.6) + 0.3
                if abs(py - (fh - 1) / 2) > half:
                    return None
                c = GREEN_D if py in ((fh - 1) // 2, fh // 2) or (out + abs(py - fh // 2)) % 3 == 0 else GREEN
                if abs(py - (fh - 1) / 2) > half - 1.0 or out >= fw - 2:
                    c = DRY                                      # scorched at the edge
                return shade(c, 1.08 if face == 'top' else 0.8)
            p.fill_box(b, leaf)
        elif b.mat == 'petals':
            def petals(face, px, py, fw, fh, rnd):
                if face not in ('front', 'back'):
                    return None
                cx, cy = px - (fw - 1) / 2, py - (fh - 1) / 2
                r = math.hypot(cx, cy)
                ang = math.atan2(cy, cx)
                reach = 6.2 + 2.6 * abs(math.cos(ang * 8))       # sixteen petals
                if r > reach or r < 3.5:
                    return None
                c = PETAL_D if abs(math.cos(ang * 8)) < 0.45 or r < 5.2 else (PETAL_L if r > reach - 1.2 else PETAL)
                if face == 'back':
                    c = shade(mix(c, GREEN, 0.35), 0.8)
                return c
            p.fill_box(b, petals)
        elif b.mat == 'disc':
            def disc(face, px, py, fw, fh, rnd):
                if face in ('front', 'back'):
                    cx, cy = px - (fw - 1) / 2, py - (fh - 1) / 2
                    r = math.hypot(cx, cy)
                    if r > fw / 2 - 0.3:
                        return None
                    if face == 'back':
                        return shade(GREEN_D, 0.9)
                    # seed in its rings, and in the middle of it the afternoon
                    if r < 2.6:
                        return GLARE if r < 1.6 else GLARE_D
                    return SEED_D if int(r * 1.4 + math.atan2(cy, cx) * 2.5) % 2 == 0 else SEED
                # (the rim of the box is not drawn: the disc is round)
                return None
            p.fill_box(b, disc)
        elif b.mat == 'calyx':
            p.fill_box(b, lambda face, px, py, fw, fh, r: shade(GREEN_D if (px + py) % 2 else GREEN, 0.9))
    # what shines: the middle of the face, and the rim of the petals a little
    for name, _, b in M.boxes():
        if b.mat in ('disc', 'petals'):
            fx, fy, fw, fh = b.faces()['front']
            for x in range(fw):
                for y in range(fh):
                    r_, g_, b_, a_ = p.tex.getpixel((fx + x, fy + y))
                    if a_ and r_ > 250 and g_ > 190:
                        p.glow.putpixel((fx + x, fy + y), (r_, g_, b_, 255))
    return p.tex, p.glow


# The poses of SungazerModel.setupAnim: standing, the head bowed to whoever is before it; the head thrown up to gaze;
# a leaf drawn back; the leaf through
STAND = {}
GAZE = {'neck': (-0.75, 0, 0), 'head': (0.25, 0, 0), 'leaf_r': (0, 0, 0.5), 'leaf_l': (0, 0, -0.5), 'stem': (-0.08, 0, 0)}
LASH_BACK = {'stem': (0, 0.9, 0), 'leaf_r': (0, 0.5, 0.2), 'neck': (0, -0.7, 0)}
LASH_THROUGH = {'stem': (0.15, -1.0, 0), 'leaf_r': (0, -0.4, 0.3), 'neck': (0, 0.7, 0)}
STEP = {'root_f': (-0.5, 0, 0), 'root_r': (0.3, 0, 0), 'all': (0, 0, 0.06)}

if __name__ == '__main__':
    M.write_java(JAVA) if len(sys.argv) < 3 else None
    tex, glow = make()
    tex.save(f'{A}/textures/entity/sungazer.png')
    glow.save(f'{A}/textures/entity/sungazer_glow.png')
    if len(sys.argv) > 1:
        shots = [M.render(tex, yaw=yaw, pitch=0.12, pose=pose, size=(270, 360), zoom=7.0, centre=(0, 4), glow=glow, background=(196, 176, 120))
                 for yaw, pose in ((-0.5, STAND), (0.0, STAND), (2.6, STAND), (-1.3, STAND), (-0.6, GAZE), (-1.3, GAZE), (-0.4, LASH_BACK), (-0.4, LASH_THROUGH), (-0.5, STEP))]
        sheet(shots, 9).save(sys.argv[1])
