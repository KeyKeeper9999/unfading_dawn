#!/usr/bin/env python3
"""The Wallower of the Weir: mesh for WallowerModel.java, its texture and the glow of its eyes and sores.
Run from the repo root; pass a file name to also get a preview sheet."""
import sys

from modelkit import Box, Model, Painter, blotch, mix, shade, sheet

A = 'src/main/resources/assets/unfading_dawn'
JAVA = 'src/main/java/com/keykeeper/dushi/client/WallowerModel.java'
SCALE = 1.8           # must match MODEL_SCALE in WallowerModel.java

M = Model(128, 128, SCALE)
# what grew in the mill pond when nobody fished it any more: half sheatfish and half newt, as long as a barge and as flat
# as one - a head that is one wide mouth with four barbels to it, eyes like two blisters on top, short arms and legs
# with webbed hands that it rows the mud with, a tail that is a third of it and ends in a paddle.
# Everything hangs from 'all', so that the whole of it can go down under the water.
M.part('all', None, (0, 0, 0))
M.part('body', 'all', (0, 17, 0), boxes=[Box(-8, -6, -12, 16, 11, 20, 'hide', 'trunk'), Box(-6, -7, -11, 12, 1, 9, 'hide', 'hump'),
                                         Box(-1, -9, -8, 2, 3, 15, 'fin', 'ridge')])
M.part('head', 'body', (0, -1, -12), boxes=[Box(-9, -4, -10, 18, 7, 10, 'hide', 'skull'), Box(-7, -5, -8, 3, 1, 3, 'eye', 'eye_r'), Box(4, -5, -8, 3, 1, 3, 'eye', 'eye_l')])
M.part('jaw', 'head', (0, 3, 0), (0.08, 0, 0), boxes=[Box(-9, 0, -10, 18, 3, 10, 'hide', 'jaw')])
M.part('right_whisker', 'head', (-9, 1, -9), (0.35, 0, 1.3), boxes=[Box(-0.5, 0, -0.5, 1, 14, 1, 'barbel', 'whisker')])
M.mirror('right_whisker', 'left_whisker', parent='head')
M.part('right_barbel', 'jaw', (-6, 3, -9), (0.9, 0, 0.5), boxes=[Box(-0.5, 0, -0.5, 1, 6, 1, 'barbel', 'barbel')])
M.mirror('right_barbel', 'left_barbel', parent='jaw')
M.part('right_arm', 'body', (-8, 2, -8), (0, 0.2, 0.5), boxes=[Box(-5, -1.5, -2, 5, 4, 5, 'hide', 'arm'), Box(-10, 0, -4, 6, 2, 8, 'web', 'hand')])
M.mirror('right_arm', 'left_arm', parent='body')
M.part('right_leg', 'body', (-8, 2, 5), (0, -0.3, 0.5), boxes=[Box(-4, -1.5, -2, 4, 4, 4, 'hide', 'leg'), Box(-8, 0, -3, 5, 2, 6, 'web', 'foot')])
M.mirror('right_leg', 'left_leg', parent='body')
M.part('tail', 'body', (0, -1, 8), boxes=[Box(-5, -4.5, 0, 10, 9, 10, 'hide', 'tail_a'), Box(-1, -8, 0, 2, 4, 10, 'fin', 'fin_a')])
M.part('tail_mid', 'tail', (0, 0, 10), boxes=[Box(-3, -4, 0, 6, 8, 10, 'hide', 'tail_b'), Box(-1, -8, 0, 2, 4, 10, 'fin', 'fin_b')])
M.part('tail_tip', 'tail_mid', (0, 0, 10), boxes=[Box(-1, -7, 0, 2, 14, 11, 'fin', 'paddle')])
M.pack()

HIDE = (66, 70, 46)
HIDE_D = (42, 46, 34)
HIDE_DD = (26, 30, 24)
HIDE_L = (98, 100, 60)
BELLY = (168, 162, 118)
BELLY_D = (130, 124, 92)
SORE = (150, 255, 130)
SORE_D = (70, 150, 70)
MOUTH = (150, 110, 110)
MOUTH_D = (96, 60, 66)
TOOTH = (220, 214, 180)
EYE = (220, 255, 200)
BARBEL = (176, 150, 130)
FIN = (52, 66, 48)
FIN_L = (86, 104, 66)
BLACK = (14, 16, 12)


def hide(lit):
    def fn(face, px, py, fw, fh, rnd):
        f = {'top': 1.08, 'bottom': 1.0}.get(face, 1.06 - 0.18 * py / max(1, fh - 1))
        amount = blotch(f'wal{fw}{fh}{face}', px, py, 3.6)
        c = HIDE_D if amount > 0.6 else (HIDE_L if amount < 0.2 else HIDE)
        if blotch(f'spot{fw}{fh}{face}', px, py, 1.7) > 0.74:
            c = HIDE_DD                          # the spots of a thing that lies on the bottom
        if face == 'bottom':
            c = BELLY if blotch(f'belly{fw}{fh}', px, py, 2.5) < 0.7 else BELLY_D
        elif face not in ('top',) and fh > 4 and py >= fh - 3:
            c = mix(c, BELLY, 0.3 * (py - fh + 4))
        return shade(c, f)
    return fn


def make():
    p = Painter(M, 103)
    boxes = {b.tag: b for _, _, b in M.boxes()}
    for _, _, b in M.boxes():
        if b.mat == 'hide':
            p.fill_box(b, hide(False))
        elif b.mat == 'eye':
            p.fill_box(b, lambda face, px, py, fw, fh, r: shade(HIDE_L, 1.1) if face != 'top' else (HIDE_L if px in (0, fw - 1) or py in (0, fh - 1) else EYE))
            fx, fy, fw, fh = b.faces()['top']
            p.glow.putpixel((fx + 1, fy + 1), EYE + (255,))
        elif b.mat == 'barbel':
            p.fill_box(b, lambda face, px, py, fw, fh, r: shade(BARBEL, 1.05 - 0.05 * py) if face not in ('top', 'bottom') else BARBEL)
        elif b.mat == 'web':
            def web(face, px, py, fw, fh, r, tag=b.tag):
                if face in ('top', 'bottom'):
                    finger = py % 3 == 0
                    out = px if face == 'top' else px
                    if not finger and px in (0, fw - 1):
                        return None                    # the web is cut back between the fingers at either edge
                    return shade(HIDE_L if finger else mix(HIDE, BELLY, 0.35), 1.05 if face == 'top' else 0.8)
                return shade(HIDE_D, 0.9)
            p.fill_box(b, web)
        elif b.mat == 'fin':
            def fin(face, px, py, fw, fh, r, tag=b.tag):
                if face in ('right', 'left'):
                    ray = px % 2 == 0
                    if tag == 'paddle':
                        mid = abs(py - (fh - 1) / 2)
                        if mid > 2.5 + px * 0.6 or (px >= fw - 2 and not ray):
                            return None
                    elif py < (1 if ray else 2) and blotch(f'fin{tag}', px, 0, 1.3) > 0.4:
                        return None
                    return shade(FIN_L if ray else FIN, 1.0)
                return shade(FIN, 0.85)
            p.fill_box(b, fin)
    skull, jaw, trunk = boxes['skull'], boxes['jaw'], boxes['trunk']
    # the mouth: the whole width of the head, a pale lip above and below, small teeth like a rasp, a grey-red gullet
    for face in ('front', 'right', 'left'):
        fx, fy, fw, fh = skull.faces()[face]
        for x in range(fw):
            p.px(fx + x, fy + fh - 1, BELLY if x % 4 else BELLY_D)
            p.px(fx + x, fy + fh - 2, HIDE_DD)
        fx, fy, fw, fh = jaw.faces()[face]
        for x in range(fw):
            p.px(fx + x, fy, BELLY if x % 4 else BELLY_D)
    for b, face in ((jaw, 'top'), (skull, 'bottom')):
        fx, fy, fw, fh = b.faces()[face]
        for x in range(fw):
            for y in range(fh):
                rim = x in (0, fw - 1) or (y == 0 if face == 'top' else y == 0)
                inner = x in (1, fw - 2) or y == 1
                c = BELLY if rim else (TOOTH if inner and (x + y) % 2 == 0 else (MOUTH_D if blotch('gullet', x, y, 2.5) > 0.5 or y > fh - 3 else MOUTH))
                p.px(fx + x, fy + y, c)
    # nostrils, far apart on the lip
    for x in (4, 13):
        p.put(skull, 'front', x, 3, BLACK)
    # sores along the flanks and the hump, in which something of the bog's own light has settled
    for face, spots in (('right', ((3, 3), (9, 5), (15, 2), (17, 6))), ('left', ((2, 4), (8, 2), (12, 6), (18, 3))), ('top', ((3, 5), (11, 12), (6, 16)))):
        for x, y in spots:
            for dx, dy in ((0, 0), (1, 0), (0, 1)):
                p.put(trunk, face, x + dx, y + dy, SORE if (dx, dy) == (0, 0) else SORE_D, glow=True)
            p.put(trunk, face, x - 1, y, HIDE_DD)
            p.put(trunk, face, x, y - 1, HIDE_DD)
    return p.tex, p.glow


STAND = {}
SNAP = {'body': {'off': (0, 0, -4)}, 'head': (-0.25, 0, 0), 'jaw': (0.9, 0, 0), 'right_whisker': (0, 0, 0.5), 'left_whisker': (0, 0, -0.5)}
SPIT = {'body': (-0.2, 0, 0), 'head': (-0.5, 0, 0), 'jaw': (0.55, 0, 0), 'tail': (0.3, 0, 0)}
GULP = {'body': (-0.12, 0, 0), 'head': (-0.45, 0, 0), 'jaw': (1.25, 0, 0), 'right_arm': (0, -0.5, 0), 'left_arm': (0, 0.5, 0)}
TAIL = {'tail': (0, 1.0, 0), 'tail_mid': (0, 0.8, 0), 'tail_tip': (0, 0.6, 0), 'body': (0, -0.3, 0), 'head': (0, 0.4, 0)}
SINK = {'all': {'off': (0, 9, 0)}, 'head': (0.2, 0, 0)}
BREACH = {'all': {'off': (0, -10, 0)}, 'body': (-0.9, 0, 0), 'head': (-0.3, 0, 0), 'jaw': (1.1, 0, 0), 'tail': (0.6, 0, 0), 'tail_mid': (0.5, 0, 0),
          'right_arm': (0, 0, -0.9), 'left_arm': (0, 0, 0.9)}

if __name__ == '__main__':
    M.write_java(JAVA) if len(sys.argv) < 3 else None
    tex, glow = make()
    tex.save(f'{A}/textures/entity/wallower.png')
    glow.save(f'{A}/textures/entity/wallower_glow.png')
    if len(sys.argv) > 1:
        shots = [M.render(tex, yaw=yaw, pose=pose, size=(460, 320), zoom=3.3, centre=(0, 6), glow=glow)
                 for yaw, pose in ((-0.6, STAND), (0.0, STAND), (1.5, STAND), (2.6, STAND), (-0.7, SNAP), (-0.9, SPIT), (-0.5, GULP), (2.4, TAIL),
                                   (-0.6, SINK), (-1.2, BREACH), (-1.57, STAND), (0.0, GULP))]
        sheet(shots, 4).save(sys.argv[1])
