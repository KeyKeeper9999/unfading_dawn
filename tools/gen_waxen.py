#!/usr/bin/env python3
"""The waxen of the Black Firwood: mesh for WaxenModel.java, its texture and the glow of the candles that burn on it.
Run from the repo root; pass a file name to also get a preview sheet."""
import sys

from modelkit import Box, Model, Painter, blotch, mix, shade, sheet

A = 'src/main/resources/assets/vsratie_dushi'
JAVA = 'src/main/java/com/keykeeper/dushi/client/WaxenModel.java'
SCALE = 1.3           # must match MODEL_SCALE in WaxenModel.java

M = Model(128, 64, SCALE)
# What the wax of all those candles ran together into: a heap as high as a man that has grown a head and two arms,
# with the stubs still burning where they stuck in it. It has no legs; it creeps on its own foot like something
# poured. A head with two pits and a jaw that sags open; arms as thick as posts, a joint each, that end in drips.
M.part('all', None, (0, 24, 0))
M.part('foot', 'all', (0, 0, 0), boxes=[Box(-9, -2, -9, 18, 2, 18, 'wax', 'puddle'), Box(-7.5, -7, -7.5, 15, 5, 15, 'wax', 'foot')])
M.part('body', 'all', (0, -7, 0), boxes=[Box(-6, -10, -6, 12, 10, 12, 'wax', 'body')])
M.part('head', 'body', (0, -10, 0), (0.12, 0, 0), boxes=[Box(-4, -7, -4, 8, 7, 8, 'wax', 'head')])
M.part('jaw', 'head', (0, -2.5, -4), boxes=[Box(-3, 0, -1, 6, 3, 2, 'wax', 'jaw')])
M.part('right_arm', 'body', (-6, -8, 0), (0, 0, 0.36), boxes=[Box(-4, -1.5, -2, 4, 12, 4, 'wax', 'upper')])
M.part('right_forearm', 'right_arm', (-2, 10.5, 0), (-0.3, 0, -0.2), boxes=[Box(-2, 0, -2, 4, 9, 4, 'wax', 'fore'), Box(-1.5, 9, -1.5, 1, 3, 1, 'wax', 'drip_a'),
                                                                            Box(0.5, 9, 0.5, 1, 2, 1, 'wax', 'drip_b')])
M.mirror('right_arm', 'left_arm')
M.mirror('right_forearm', 'left_forearm')
# the candles: on the crown of the head, on both shoulders, down the back
STUBS = {'head': ((-2.5, -7, -1.5, 3), (1.5, -7, 0.5, 5), (-0.5, -7, 2, 2)),
         'body': ((-6, -10, -3, 4), (-5, -10, 2, 2), (4, -10, -2, 3), (5, -10, 3, 5), (-2, -10, 4.5, 3), (1.5, -10, 5, 2))}
for part, stubs in STUBS.items():
    boxes = []
    for i, (x, y, z, h) in enumerate(stubs):
        boxes.append(Box(x, y - h, z, 1, h, 1, 'stub', f'{part}_stub{i}'))
        boxes.append(Box(x, y - h - 2, z, 1, 2, 1, 'flame', f'{part}_flame{i}'))
    M.part(f'{part}_candles', part, (0, 0, 0), boxes=boxes)
M.pack()

WAX = (228, 214, 172)
WAX_L = (244, 236, 204)
WAX_D = (190, 170, 124)
WAX_DD = (140, 120, 84)
SOOT = (74, 66, 58)
FLAME = (255, 214, 120)
FLAME_L = (255, 246, 208)
FLAME_D = (255, 150, 52)
BLACK = (24, 18, 14)


def wax(tag):
    def fn(face, px, py, fw, fh, rnd):
        f = {'top': 1.08, 'bottom': 0.66}.get(face, 1.02 - 0.16 * py / max(1, fh - 1))
        c = WAX
        # runs of it, down every side: lighter ridges with a shadow beside each
        if face not in ('top', 'bottom'):
            run = blotch(f'run{tag}{face}', px, 0, 1.0)
            long = int(fh * (0.35 + 0.65 * blotch(f'len{tag}{face}', px, 3, 1.0)))
            if run > 0.62 and py <= long:
                c = WAX_L if py < long else WAX_D
            elif run < 0.3:
                c = WAX_D
        else:
            c = WAX_L if blotch(f'top{tag}', px, py, 2.0) > 0.55 else WAX
        if blotch(f'soot{tag}{face}', px, py, 3.0) > 0.78:
            c = mix(c, SOOT, 0.5)
        if tag == 'puddle' and face != 'top' and (px + py) % 3 == 0:
            return None
        return shade(c, f)
    return fn


def make():
    p = Painter(M, 151)
    boxes = {b.tag: b for _, _, b in M.boxes()}
    for _, _, b in M.boxes():
        if b.mat == 'wax':
            p.fill_box(b, wax(b.tag))
        elif b.mat == 'stub':
            p.fill_box(b, lambda face, px, py, fw, fh, r: shade(WAX_L if py else SOOT, 1.0))
        elif b.mat == 'flame':
            p.fill_box(b, lambda face, px, py, fw, fh, r: FLAME_D if face == 'bottom' else (FLAME_L if py == fh - 1 and face != 'top' else FLAME))
    # the face: two pits where the wax fell in, and under the jaw's edge the dark of what would be a mouth
    head, jaw = boxes['head'], boxes['jaw']
    p.art(head, 'front', ["........",
                          ".DD..DD.",
                          ".BB..BB.",
                          ".BD..DB.",
                          "...dd...",
                          ".BBBBBB.",
                          ".BBBBBB."], {'D': WAX_DD, 'B': BLACK, 'd': WAX_D})
    for x in range(6):
        p.put(jaw, 'front', x, 0, WAX_DD if x % 2 else WAX_D)
    for _, _, b in M.boxes():
        if b.mat == 'flame':
            for face, (fx, fy, fw, fh) in b.faces().items():
                for x in range(fw):
                    for y in range(fh):
                        r_, g_, b_, a_ = p.tex.getpixel((fx + x, fy + y))
                        if a_:
                            p.glow.putpixel((fx + x, fy + y), (r_, g_, b_, 255))
    return p.tex, p.glow


STAND = {}
CREEP = {'body': (0.25, 0, 0), 'head': (0.1, 0, 0), 'right_arm': (-0.3, 0, 0.1), 'left_arm': (0.2, 0, -0.1)}
SLAM_UP = {'body': (-0.25, 0, 0), 'head': (-0.3, 0, 0), 'right_arm': (-2.6, 0, 0.2), 'left_arm': (-2.6, 0, -0.2), 'right_forearm': (0.5, 0, 0.2), 'left_forearm': (0.5, 0, -0.2)}
SLAM_DOWN = {'body': (0.55, 0, 0), 'head': (0.2, 0, 0), 'right_arm': (-1.2, 0, -0.1), 'left_arm': (-1.2, 0, 0.1), 'right_forearm': (0.3, 0, 0.2), 'left_forearm': (0.3, 0, -0.2)}
SPEW = {'body': (-0.15, 0, 0), 'head': (-0.35, 0, 0), 'jaw': (0.9, 0, 0), 'right_arm': (0.4, 0, 0.3), 'left_arm': (0.4, 0, -0.3)}

if __name__ == '__main__':
    M.write_java(JAVA) if len(sys.argv) < 3 else None
    tex, glow = make()
    tex.save(f'{A}/textures/entity/waxen.png')
    glow.save(f'{A}/textures/entity/waxen_glow.png')
    if len(sys.argv) > 1:
        shots = [M.render(tex, yaw=yaw, pose=pose, size=(300, 340), zoom=7.0, centre=(0, 6), glow=glow, background=(52, 62, 60))
                 for yaw, pose in ((-0.5, STAND), (0.0, STAND), (1.5, STAND), (2.7, STAND), (-0.9, CREEP), (-1.1, SLAM_UP), (-1.3, SLAM_DOWN), (-1.0, SPEW))]
        sheet(shots, 8).save(sys.argv[1])
