#!/usr/bin/env python3
"""The Coming - what was to arrive on the day that was not let begin, or as much of it as had got through when the day
was stopped: mesh for ComingModel.java, its texture and what shines of it.

It stands in a tear in the floor of the Sky Beneath and does not leave it: a neck as thick as a tower, of the night
itself, cracked, with light in the cracks; at the top of the neck, where a head would be, a black disc with a rim of
light - a sun that has not risen; and two arms out of the same tear. The Founder's rite nailed a mask of gold over the
disc (its first phase). With the mask off (its second) the arms folded behind the disc open into a crown round it,
and the front of the neck opens too.
Run from the repo root; pass a file name to also get a preview sheet (and a second argument to leave the Java alone)."""
import math
import sys

from duskkit import (BLUE, BLUE_L, GOLD, GOLD_D, GOLD_L, NAIL, NIGHT, NIGHT_D, NIGHT_L, PALE, PALE_D, PALE_DD, RAW, RAW_D, STAR, STAR_D, TOOTH, TOOTH_D,
                     VIOLET, VIOLET_D, VIOLET_L, cracks, hand_up, night, skin, starry)
from modelkit import Box, Model, Painter, blotch, mix, shade, sheet

A = 'src/main/resources/assets/unfading_dawn'
JAVA = 'src/main/java/com/keykeeper/dushi/client/ComingModel.java'
SCALE = 2.0                     # must match MODEL_SCALE in ComingModel.java
CROWN = 6                       # the arms round the disc
CROWN_R = 8.0
BACK = (26, 28, 44)

M = Model(256, 128, SCALE)
M.part('all', None, (0, 24, 0))
M.part('rift', 'all', (0, 0, 0), boxes=[Box(-16, -1, -16, 32, 1, 32, 'rift', 'rift')])
M.part('neck1', 'all', (0, -1, 3), (-0.12, 0, 0), boxes=[Box(-6, -18, -6, 12, 18, 12, 'hide', 'neck1'), Box(-0.5, -18, 6, 1, 18, 4, 'ridge', 'ridge1')])
M.part('neck2', 'neck1', (0, -18, 0), (0.18, 0, 0), boxes=[Box(-5, -16, -5, 10, 16, 10, 'hide', 'neck2'), Box(-0.5, -16, 5, 1, 16, 4, 'ridge', 'ridge2')])
M.part('neck3', 'neck2', (0, -16, 0), (0.28, 0, 0), boxes=[Box(-4, -14, -4, 8, 14, 8, 'hide', 'neck3'), Box(-0.5, -14, 4, 1, 14, 3, 'ridge', 'ridge3'),
                                                           Box(-3, -12, -4.5, 6, 10, 1, 'throat', 'throat')])
M.part('maw_r', 'neck3', (-4, -12.5, -5), boxes=[Box(0, 0, -1, 4, 11, 1, 'lip', 'lip')])
M.mirror('maw_r', 'maw_l', parent='neck3')
M.part('head', 'neck3', (0, -14, 0), (0.5, 0, 0), boxes=[Box(-9, -9, -4, 18, 18, 3, 'disc', 'disc'), Box(-2, -2, -1, 4, 4, 2, 'hide', 'stem')])
M.part('mask', 'head', (0, 0, -4), boxes=[Box(-7, -9, -1.5, 14, 17, 1, 'mask', 'mask'), Box(-13, -20, -1, 26, 12, 1, 'horn', 'horn')])
for i in range(CROWN):
    ang = (i + 0.5) * 2 * math.pi / CROWN
    M.part(f'crown{i}', 'head', (round(math.sin(ang) * CROWN_R, 2), round(-math.cos(ang) * CROWN_R, 2), -1), (-2.5, 0, round(ang, 4)),
           boxes=[Box(-1, -11, -1, 2, 11, 2, 'limb', 'crown_upper')])
    M.part(f'crownf{i}', f'crown{i}', (0, -11, 0), (2.4, 0, 0), boxes=[Box(-0.5, -10, -0.5, 1, 10, 1, 'limb', 'crown_fore'), Box(-2.5, -17, -0.5, 5, 8, 1, 'claw', 'crown_claw')])
M.part('arm_r', 'all', (-12, -1, 2), (0.2, 0, -0.45), boxes=[Box(-2.5, -18, -2.5, 5, 18, 5, 'hide', 'arm')])
M.part('fore_r', 'arm_r', (0, -18, 0), (-1.0, 0, 0.35), boxes=[Box(-2, -18, -2, 4, 18, 4, 'hide', 'fore')])
M.part('hand_r', 'fore_r', (0, -18, 0), (-0.5, 0, 0), boxes=[Box(-3, -5, -1.5, 6, 5, 3, 'hide', 'palm'), Box(-3, -13, -0.5, 1, 8, 1, 'talon', 't0'),
                                                            Box(-1, -14, -0.5, 1, 9, 1, 'talon', 't1'), Box(1, -14, -0.5, 1, 9, 1, 'talon', 't2'),
                                                            Box(3, -12, -0.5, 1, 7, 1, 'talon', 't3'), Box(-5, -6, -0.5, 2, 1, 1, 'talon', 'thumb')])
M.mirror('arm_r', 'arm_l', parent='all')
M.mirror('fore_r', 'fore_l', parent='arm_l')
M.mirror('hand_r', 'hand_l', parent='fore_l')
M.pack()

CORONA = (236, 226, 255)
FLAME = VIOLET                  # the ridge down its back
FLAME_L = VIOLET_L


def make():
    p = Painter(M, 899)
    for name, _, b in M.boxes():
        if b.mat == 'hide':
            base = night('ch', light=1.1)

            def hide(face, px, py, fw, fh, rnd):
                c = base(face, px, py, fw, fh, rnd)
                # scales: rows of arcs, hardly to be seen
                if face not in ('top', 'bottom') and (py % 3 == 2) and ((px + (py // 3) * 2) % 4 != 0):
                    return shade(c, 1.35)
                return c
            p.fill_box(b, hide)
            starry(p, b, 'c', 0.008)
            if b.tag in ('neck1', 'neck2', 'neck3'):
                cracks(p, b, 'c', 1, colour=BLUE, bright=BLUE_L)
            elif b.tag == 'arm':
                cracks(p, b, 'cv', 1, faces=('front',), colour=VIOLET_D, bright=VIOLET)
        elif b.mat == 'ridge':
            def ridge(face, px, py, fw, fh, rnd):
                if face not in ('left', 'right'):
                    return None
                x = px if face == 'left' else fw - 1 - px                  # 0 at the neck
                # plates, each a tongue of light
                reach = 1 + (2 if py % 4 in (1, 2) else 0) + (1 if py % 4 == 1 and fw > 3 else 0)
                if x >= reach:
                    return None
                return FLAME_L if x == 0 else FLAME
            p.fill_box(b, ridge)
            for face in ('left', 'right'):
                fx, fy, fw, fh = b.faces()[face]
                for x in range(fw):
                    for y in range(fh):
                        c = p.tex.getpixel((fx + x, fy + y))
                        if c[3]:
                            p.glow.putpixel((fx + x, fy + y), c[:3] + (255,))
        elif b.mat == 'rift':
            def rift(face, px, py, fw, fh, rnd):
                if face != 'top':
                    return None
                r = math.hypot(px - (fw - 1) / 2, py - (fh - 1) / 2) / (fw / 2)
                edge = 0.86 + (blotch('rift', px, py, 2.6) - 0.5) * 0.22
                if r > edge:
                    return None
                return NIGHT_D
            p.fill_box(b, rift)
            fx, fy, fw, fh = b.faces()['top']
            for x in range(fw):
                for y in range(fh):
                    if p.tex.getpixel((fx + x, fy + y))[3] == 0:
                        continue
                    r = math.hypot(x - (fw - 1) / 2, y - (fh - 1) / 2) / (fw / 2)
                    edge = 0.86 + (blotch('rift', x, y, 2.6) - 0.5) * 0.22
                    if r > edge - 0.07:
                        p.px(fx + x, fy + y, VIOLET_L if (x + y) % 3 else STAR, glow=True)
                    elif r > edge - 0.16:
                        p.px(fx + x, fy + y, VIOLET_D, glow=True)
                    elif blotch('rs', x * 7.3, y * 5.1, 0.6) > 0.93:
                        p.px(fx + x, fy + y, STAR_D, glow=True)
        elif b.mat == 'disc':
            def disc(face, px, py, fw, fh, rnd):
                if face not in ('front', 'back'):
                    return None
                r = math.hypot(px - (fw - 1) / 2, py - (fh - 1) / 2)
                ang = math.atan2(py - (fh - 1) / 2, px - (fw - 1) / 2)
                ray = 8.2 + 0.9 * math.cos(ang * 9) * math.cos(ang * 4 + 1.0)
                if r > ray:
                    return None
                if face == 'back':
                    return shade(NIGHT, 0.8)
                return NIGHT_D if r < 6.2 else CORONA
            p.fill_box(b, disc)
            fx, fy, fw, fh = b.faces()['front']
            for x in range(fw):
                for y in range(fh):
                    c = p.tex.getpixel((fx + x, fy + y))
                    if c[3] and c[0] > 100:
                        r = math.hypot(x - (fw - 1) / 2, y - (fh - 1) / 2)
                        col = CORONA if r < 7.2 else VIOLET_L
                        p.px(fx + x, fy + y, col, glow=True)
        elif b.mat == 'mask':
            def mask(face, px, py, fw, fh, rnd):
                if face not in ('front', 'back'):
                    return None
                mid = (fw - 1) / 2
                # a long face: wide at the brow, drawn in to a chin
                half = fw / 2 - (0 if py < 5 else (py - 5) * 0.42)
                if abs(px - mid) > half:
                    return None
                c = GOLD_L if blotch('mask', px, py, 3.0) > 0.6 else GOLD
                if abs(px - mid) < 0.6 and py > 6:
                    c = GOLD_D                                        # a ridge where a nose would be
                if py in (0, 1) or abs(abs(px - mid) - half) < 1.0:
                    c = GOLD_D
                return shade(c, (1.0 - 0.12 * (py / fh)) * (1.0 if face == 'front' else 0.55))
            p.fill_box(b, mask)
            # the slits it looks through: narrow, slanting to the middle, and something behind them
            for side in (-1, 1):
                for k in range(4):
                    x = int(round((b.tw - 1) / 2 + side * (1.5 + k)))
                    y = 5 - (k // 2)
                    p.put(b, 'front', x, y, STAR if k in (1, 2) else VIOLET_L, glow=True)
            # nails: where it was fixed on
            for x, y in ((1, 1), (b.tw - 2, 1), (int((b.tw - 1) / 2), b.th - 2)):
                p.put(b, 'front', x, y, NIGHT)
        elif b.mat == 'horn':
            def horn(face, px, py, fw, fh, rnd):
                if face not in ('front', 'back'):
                    return None
                x = px if face == 'front' else fw - 1 - px
                # a crescent lying on its back over the brow, one tip longer than the other
                cx = (fw - 1) / 2
                r = math.hypot(x - cx, py + 1.5)
                inner = math.hypot(x - cx - 1.6, py + 4.6)
                if r > 12.9 or inner < 12.3:
                    return None
                return shade(GOLD_L if r > 11.9 else GOLD, 1.0 if face == 'front' else 0.6)
            p.fill_box(b, horn)
        elif b.mat == 'limb':
            p.fill_box(b, night('cl', light=1.25))
        elif b.mat == 'claw':
            p.fill_box(b, hand_up())
        elif b.mat == 'talon':
            p.fill_box(b, lambda face, px, py, fw, fh, rnd: shade(PALE_DD if py > fh // 2 else PALE_D, 1.0 if face != 'back' else 0.8))
        elif b.mat == 'throat':
            def throat(face, px, py, fw, fh, rnd):
                if face != 'front':
                    return shade(RAW_D, 0.5)
                return shade(RAW if blotch('thr', px, py, 2.0) > 0.55 else RAW_D, 0.8)
            p.fill_box(b, throat)
            for x, y in ((1, 1), (4, 2), (2, 4), (3, 6), (1, 7), (4, 8), (2, 2)):
                p.put(b, 'front', x, y, STAR, glow=True)
                p.put(b, 'front', x, y + 1, NIGHT_D)
        elif b.mat == 'lip':
            base = night('cp', light=1.1)

            def lip(face, px, py, fw, fh, rnd, left=name.endswith('_l')):
                if face == 'back':
                    return shade(RAW, 0.8)
                if face == 'front':
                    x = px if not left else fw - 1 - px
                    if x == fw - 1:
                        return TOOTH if py % 2 == 0 else None           # teeth along the edge that opens
                return base(face, px, py, fw, fh, rnd)
            p.fill_box(b, lip)
    return p.tex, p.glow


def crown_open(spread=0.0, curl=0.6):
    pose = {}
    for i in range(CROWN):
        pose[f'crown{i}'] = (2.5 + 0.25 + spread, 0, 0.35)
        pose[f'crownf{i}'] = (-2.4 - curl, 0, 0)
    return pose


# its first phase: at rest; the head lifted to look; the neck drawn back, and down full length; an arm round
MASKED = (
    ('masked', {}),
    ('gaze', {'neck2': (-0.15, 0, 0), 'neck3': (-0.3, 0, 0), 'head': (-0.25, 0, 0)}),
    ('slam_back', {'neck1': (-0.4, 0, 0), 'neck2': (-0.3, 0, 0), 'neck3': (-0.3, 0, 0), 'head': (-0.6, 0, 0)}),
    ('slam', {'neck1': (0.72, 0, 0), 'neck2': (0.42, 0, 0), 'neck3': (0.2, 0, 0), 'head': (-0.45, 0, 0)}),
    ('sweep', {'arm_r': (1.0, -0.6, -0.6), 'fore_r': (0.8, 0, -0.3), 'hand_r': (0.4, 0, 0), 'neck1': (0, 0.2, 0)}),
)
# its second: the mask gone and the crown open; the throat open; both arms up
BARE = (
    ('bare', dict(crown_open())),
    ('pull', dict(crown_open(0.2), maw_r=(0, 1.35, 0), maw_l=(0, -1.35, 0), neck2=(0.15, 0, 0), neck3=(0.2, 0, 0), head=(-0.7, 0, 0))),
    ('rain', dict(crown_open(0.35, 0.2), arm_r=(-0.1, 0, 0.2), fore_r=(0.9, 0, -0.3), arm_l=(-0.1, 0, -0.2), fore_l=(0.9, 0, 0.3), neck1=(-0.25, 0, 0),
                  head=(-0.9, 0, 0))),
)

if __name__ == '__main__':
    if len(sys.argv) < 3:
        M.write_java(JAVA)
    tex, glow = make()
    tex.save(f'{A}/textures/entity/coming.png')
    glow.save(f'{A}/textures/entity/coming_glow.png')
    if len(sys.argv) > 1:
        cam = dict(size=(330, 440), zoom=2.7, centre=(0, -32), glow=glow, background=BACK)
        shots = [M.render(tex, yaw=yaw, pitch=0.12, pose=pose, **cam) for yaw, pose in ((-0.5, {}), (0.0, {}), (2.6, {}), (-1.35, {}))]
        shots += [M.render(tex, yaw=-0.6, pitch=0.12, pose=pose, **cam) for _, pose in MASKED[1:]]
        shots += [M.render(tex, yaw=yaw, pitch=0.12, pose=pose, hidden=('mask',), **cam) for yaw, (_, pose) in ((-0.5, BARE[0]), (0.0, BARE[0]), (-0.6, BARE[1]), (-0.6, BARE[2]))]
        sheet(shots, 6, labels=['coming', '', '', ''] + [n for n, _ in MASKED[1:]] + ['bare', '', 'pull', 'rain']).save(sys.argv[1])
