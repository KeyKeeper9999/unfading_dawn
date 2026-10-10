#!/usr/bin/env python3
"""The Coming - what was to arrive on the day that was not let begin: mesh for ComingModel.java, its texture and what
shines of it; and what it reaches into the hall with (TendrilModel.java: an arm of the same night, out of a hole in
the floor).

It is in the Tear - a crack in the wall of the last hollow of the Sky Beneath, from the floor up, with the sky of some
other place behind it - and it is coming out of it: a breast as broad as a gate, on which it crawls, and long arms it
drags itself by; from the breast a neck like a tower, of the night itself, cracked, with light in the cracks; at the
top of the neck, where a head would be, a black disc with a rim of light - a sun that has not risen, and nothing in the
middle of it. The Founder's rite nailed a mask of gold over the disc. Masked (its first phase) it has got no further
than its neck: the breast is still in the rock, and it looks out of the crack and down. With the mask off (its second)
it comes out to the breast and rears; a second pair of arms comes with it, the arms folded behind the disc open into a
crown round it, the front of the neck opens - and behind it, in the crack, an eye opens that is as tall as the crack.

'body' is all of it that comes and goes: masked, the model draws it BACK_IN further back, which is in the rock. 'eye'
stays in the crack. The face of the rock is WALL behind where it stands.
Run from the repo root; pass a file name to also get a preview sheet (and a second argument to leave the Java alone)."""
import math
import sys

import numpy as np

from duskkit import (BLUE, BLUE_L, GOLD, GOLD_D, GOLD_L, NAIL, NIGHT, NIGHT_D, NIGHT_L, PALE, PALE_D, PALE_DD, RAW, RAW_D, STAR, STAR_D, TOOTH, TOOTH_D,
                     VIOLET, VIOLET_D, VIOLET_L, cracks, hand_up, night, skin, starry)
from modelkit import Box, Model, Painter, blotch, mix, rot_matrix, shade, sheet

A = 'src/main/resources/assets/unfading_dawn'
JAVA = 'src/main/java/com/keykeeper/dushi/client/ComingModel.java'
TENDRIL_JAVA = 'src/main/java/com/keykeeper/dushi/client/TendrilModel.java'
SCALE = 2.4                     # must match MODEL_SCALE in ComingModel.java
CROWN = 8                       # the arms round the disc
CROWN_R = 10.0
WALL = 10                       # the face of the rock, so far behind where it stands (a block and a half)
BACK_IN = 24                    # masked, it is so far back in the crack (ComingModel.BACK_IN)
BACK = (26, 28, 44)

M = Model(256, 256, SCALE)
M.part('all', None, (0, 24, 0))
M.part('eye', 'all', (0, -52, WALL - 0.5), boxes=[Box(-17, -26, 0, 34, 52, 1, 'eye', 'eye')])
M.part('body', 'all', (0, 0, 0), boxes=[Box(-8, -19, 14, 16, 17, 22, 'hide', 'hind')])
M.part('chest', 'body', (0, -13, 2), boxes=[Box(-10, -11, -8, 20, 22, 22, 'hide', 'chest'), Box(-13, -12, -10, 26, 9, 11, 'hide', 'shoulders'),
                                            Box(-7, -3, -13, 14, 14, 6, 'hide', 'brisket'), Box(-0.5, -16, 1, 1, 4, 13, 'ridge', 'ridge0')])
M.part('neck1', 'chest', (0, -9, -7), (0.15, 0, 0), boxes=[Box(-6, -18, -6, 12, 18, 12, 'hide', 'neck1'), Box(-0.5, -18, 6, 1, 18, 4, 'ridge', 'ridge1')])
M.part('neck2', 'neck1', (0, -18, 0), (0.2, 0, 0), boxes=[Box(-5, -16, -5, 10, 16, 10, 'hide', 'neck2'), Box(-0.5, -16, 5, 1, 16, 4, 'ridge', 'ridge2')])
M.part('neck3', 'neck2', (0, -16, 0), (0.25, 0, 0), boxes=[Box(-4, -14, -4, 8, 14, 8, 'hide', 'neck3'), Box(-0.5, -14, 4, 1, 14, 3, 'ridge', 'ridge3'),
                                                           Box(-3, -12, -4.5, 6, 10, 1, 'throat', 'throat')])
M.part('maw_r', 'neck3', (-4, -12.5, -5), boxes=[Box(0, 0, -1, 4, 11, 1, 'lip', 'lip')])
M.mirror('maw_r', 'maw_l', parent='neck3')
M.part('head', 'neck3', (0, -14, 0), (0.3, 0, 0), boxes=[Box(-11, -11, -4, 22, 22, 3, 'disc', 'disc'), Box(-2, -2, -1, 4, 4, 2, 'hide', 'stem')])
M.part('mask', 'head', (0, 0, -4), boxes=[Box(-8, -11, -1.5, 16, 20, 1, 'mask', 'mask'), Box(-15, -24, -1, 30, 14, 1, 'horn', 'horn')])
for i in range(CROWN):
    ang = (i + 0.5) * 2 * math.pi / CROWN
    M.part(f'crown{i}', 'head', (round(math.sin(ang) * CROWN_R, 2), round(-math.cos(ang) * CROWN_R, 2), -1), (-2.5, 0, round(ang, 4)),
           boxes=[Box(-1, -11, -1, 2, 11, 2, 'limb', 'crown_upper')])
    M.part(f'crownf{i}', f'crown{i}', (0, -11, 0), (2.4, 0, 0), boxes=[Box(-0.5, -10, -0.5, 1, 10, 1, 'limb', 'crown_fore'), Box(-2.5, -17, -0.5, 5, 8, 1, 'claw', 'crown_claw')])
# the arms it drags itself by: from the shoulders of the breast, elbows out, the hands on the floor before it
M.part('arm_r', 'chest', (-13, -8, -6), boxes=[Box(-2.5, -20, -2.5, 5, 20, 5, 'hide', 'arm')])
M.part('fore_r', 'arm_r', (0, -20, 0), boxes=[Box(-2, -20, -2, 4, 20, 4, 'hide', 'fore')])
M.part('hand_r', 'fore_r', (0, -20, 0), boxes=[Box(-3, -5, -1.5, 6, 5, 3, 'hide', 'palm'), Box(-3, -13, -0.5, 1, 8, 1, 'talon', 't0'),
                                               Box(-1, -14, -0.5, 1, 9, 1, 'talon', 't1'), Box(1, -14, -0.5, 1, 9, 1, 'talon', 't2'),
                                               Box(3, -12, -0.5, 1, 7, 1, 'talon', 't3'), Box(-5, -6, -0.5, 2, 1, 1, 'talon', 'thumb')])
# the second pair, thinner, from under the first: seen only when the breast is out
M.part('arm2_r', 'chest', (-10, 3, 2), boxes=[Box(-1.5, -18, -1.5, 3, 18, 3, 'hide', 'arm2')])
M.part('fore2_r', 'arm2_r', (0, -18, 0), boxes=[Box(-1, -18, -1, 2, 18, 2, 'limb', 'fore2'), Box(-2.5, -26, -0.5, 5, 8, 1, 'claw', 'claw2')])
for src, dst, parent in (('arm_r', 'arm_l', 'chest'), ('fore_r', 'fore_l', 'arm_l'), ('hand_r', 'hand_l', 'fore_l'), ('arm2_r', 'arm2_l', 'chest'),
                         ('fore2_r', 'fore2_l', 'arm2_l')):
    M.mirror(src, dst, parent=parent)
M.pack()

CORONA = (236, 226, 255)
FLAME = VIOLET                  # the ridge down its back
FLAME_L = VIOLET_L
ARMS = ('arm', 'fore', 'hand', 'arm2', 'fore2')


def aim(pose, part, to):
    """Adds to `pose` the turn of `part` (a box that runs up from its pivot) that points it along `to` in the model's
    own space - x to its left, y down, z back - with what `pose` has for its parents; a turn about x, then about z."""
    parent = M.parts[part]['parent']
    pm, _ = M.transform(parent, pose)
    d = pm.T @ (np.array(to, float) / np.linalg.norm(to))
    rx = -math.asin(max(-1.0, min(1.0, d[2])))
    rz = math.atan2(d[0], -d[1])
    rest = M.parts[part]['rot']
    pose[part] = (round(rx - rest[0], 3), round(-rest[1], 3), round(rz - rest[2], 3))
    return pose


def arms(pose, upper, fore, hand=None, sides='rl', pair=''):
    """Both arms of a pair (or one): where the upper arm and the forearm point, given for the right one (x is mirrored
    for the left), and how the hand is turned on the wrist."""
    for side in sides:
        m = np.array([1, 1, 1]) if side == 'r' else np.array([-1, 1, 1])
        aim(pose, f'arm{pair}_{side}', np.array(upper) * m)
        aim(pose, f'fore{pair}_{side}', np.array(fore) * m)
        if hand is not None and not pair:
            pose[f'hand_{side}'] = hand
    return pose


def crown_open(spread=0.0, curl=0.6):
    pose = {}
    for i in range(CROWN):
        pose[f'crown{i}'] = (2.5 + 0.25 + spread, 0, 0.35)
        pose[f'crownf{i}'] = (-2.4 - curl, 0, 0)
    return pose


def neck(pose, n1=0.0, n2=0.0, n3=0.0, head=0.0, yaw=0.0):
    pose.update(neck1=(n1, yaw, 0), neck2=(n2, 0, 0), neck3=(n3, 0, 0), head=(head, 0, 0))
    return pose


def out(extra=None):
    """Its second phase, at rest: the breast out of the rock, both pairs of arms down, the crown open (the rest pose
    of the mesh has the arms straight up: every pose of the sheet says where they are)."""
    pose = dict(crown_open())
    arms(pose, (-0.62, 0.55, -0.56), (-0.1, 0.62, -0.78), (0.9, 0, 0))
    arms(pose, (-0.8, 0.2, -0.56), (-0.25, 0.9, -0.36), pair='2')
    pose.update(extra or {})
    return pose


def masked(extra=None):
    """Its first phase, at rest: back in the crack to the neck, which comes out of it and down; the hands on the lips of
    the crack."""
    pose = {'body': {'off': (0, 0, BACK_IN)}}
    neck(pose, 0.8, 0.25, -0.3, -0.8)
    arms(pose, (-0.53, 0.34, -0.78), (0.59, 0.55, -0.59), (0.9, 0, 0))
    arms(pose, (0, 0.2, 1.0), (0, 0.1, 1.0), pair='2')
    pose.update(extra or {})
    return pose


def with_neck(pose, *a, **k):
    return neck(dict(pose), *a, **k)


def with_arms(pose, *a, **k):
    return arms(dict(pose), *a, **k)


# what ComingModel asks for by name: (the pose, whether the mask is on). The numbers it has are printed by `python3
# tools/gen_coming.py - x` (every part's turn from the rest of the mesh)
POSES = {
    'masked': masked(),
    # the head lifted and held on whoever it looks at: the rays come out of the middle of it
    'gaze': with_neck(masked(), 0.5, 0.1, -0.3, -0.8),
    # the neck drawn up and back into the crack, and down full length before it
    'slam_back': with_neck(masked(), 0.3, -0.25, -0.4, -0.7),
    'slam': with_neck(masked(), 1.25, 0.25, 0.1, -0.8),
    # an arm let go of the rock and brought round, low
    'sweep_back': with_arms(masked(), (-0.85, 0.2, -0.5), (-0.9, 0.3, -0.3), (0.4, 0, 0), sides='r'),
    'sweep': with_arms(masked(), (0.45, 0.35, -0.82), (0.75, 0.4, -0.53), (0.4, 0, 0), sides='r'),
    # both arms up, and down on whoever is at the foot of the crack
    'crush_up': with_arms(with_neck(masked(), 0.6, 0.2, -0.2, -0.2), (-0.35, -0.7, -0.6), (-0.1, -0.8, -0.6), (0.3, 0, 0)),
    'crush': with_arms(masked(), (-0.35, 0.4, -0.85), (0.2, 0.75, -0.63), (1.2, 0, 0)),
    # its hands in the floor of the hall: what it reaches with comes up elsewhere
    'reach': with_arms(with_neck(masked(), 0.95, 0.35, -0.1, -0.5), (-0.5, 0.3, -0.81), (0.3, 0.9, -0.3), (1.5, 0, 0)),
    'bare': out(),
    'bare_gaze': with_neck(out(), -0.1, -0.1, -0.2, -0.25),
    'bare_slam_back': with_neck(out(), -0.4, -0.3, -0.3, -0.5),
    'bare_slam': with_neck(out(), 1.08, 0.42, 0.2, -0.75),
    'bare_sweep_back': with_arms(out(), (-0.9, 0.15, -0.4), (-0.9, 0.3, -0.3), (0.4, 0, 0), sides='r'),
    'bare_sweep': with_arms(out(), (0.75, 0.3, -0.6), (0.6, 0.35, -0.7), (0.4, 0, 0), sides='r'),
    'bare_crush_up': with_arms(out(), (-0.5, -0.75, -0.4), (-0.2, -0.85, -0.5), (0.3, 0, 0)),
    'bare_crush': with_arms(out(), (-0.45, 0.6, -0.66), (-0.05, 0.92, -0.4), (1.2, 0, 0)),
    'bare_reach': with_arms(with_neck(out(), 0.5, 0.4, 0.2, -0.3), (-0.5, 0.5, -0.7), (0.1, 0.95, -0.3), (1.5, 0, 0)),
    'pull': with_neck(dict(out(crown_open(0.2)), maw_r=(0, 1.35, 0), maw_l=(0, -1.35, 0)), 0.25, 0.25, 0.2, -0.7),
    'bite': with_neck(dict(out(), maw_r=(0, 0.2, 0), maw_l=(0, -0.2, 0)), 0.75, 0.45, 0.3, -0.6),
    # the sky called down: the second pair up, the head thrown back
    'rain': with_neck(arms(out(crown_open(0.35, 0.2)), (-0.55, -0.75, -0.35), (-0.2, -0.95, -0.2), pair='2'), -0.25, 0.0, 0.0, -0.9),
    # the eye behind it looks: it lies low, out of the way of it
    'glare': with_neck(out(crown_open(-0.3, 1.0)), 0.6, 0.5, 0.35, -0.2),
}
MASK_ON = ('masked', 'gaze', 'slam_back', 'slam', 'sweep_back', 'sweep', 'crush_up', 'crush', 'reach')
# the parts a pose turns, in the order ComingModel keeps them
POSED = ('neck1', 'neck2', 'neck3', 'head', 'arm_r', 'fore_r', 'hand_r', 'arm_l', 'fore_l', 'hand_l', 'arm2_r', 'fore2_r', 'arm2_l', 'fore2_l', 'maw_r', 'maw_l')


def write_poses(path, indent='    '):
    """The poses as ComingModel has them: a row of three turns for every part of POSED, between the markers."""
    lines = []
    for label, pose in POSES.items():
        rows = []
        for part in POSED:
            v = pose.get(part, (0, 0, 0))
            v = v.get('rot', (0, 0, 0)) if isinstance(v, dict) else v
            rows.append('{' + ', '.join(f'{float(x):.3f}F' for x in v) + '}')
        lines.append(f'{indent}private static final float[][] {label.upper()} = {{' + ', '.join(rows) + '};')
    src = open(path, encoding='utf-8').read()
    a = src.index(indent + '// <generated poses>')
    b = src.index(indent + '// </generated poses>')
    open(path, 'w', encoding='utf-8').write(src[:a] + indent + '// <generated poses>\n' + '\n'.join(lines) + '\n' + src[b:])



def make(wall=False):
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
            elif b.tag == 'chest':
                cracks(p, b, 'cc', 2, faces=('left', 'right'), colour=BLUE, bright=BLUE_L)
            elif b.tag == 'shoulders':
                cracks(p, b, 'cs', 1, faces=('front',), colour=VIOLET_D, bright=VIOLET)
            elif b.tag == 'brisket':
                # eyes in the breast of it, low, where no eyes should be: they are what looks at whoever is under it
                for x, y in ((2, 3), (5, 5), (9, 4), (11, 2), (4, 9), (8, 8), (10, 11), (6, 1)):
                    p.put(b, 'front', x, y, STAR, glow=True)
                    p.put(b, 'front', x, y + 1, NIGHT_D)
            elif b.tag in ('arm', 'arm2'):
                cracks(p, b, 'cv', 1, faces=('front',), colour=VIOLET_D, bright=VIOLET)
        elif b.mat == 'ridge':
            def ridge(face, px, py, fw, fh, rnd, lying=b.tag == 'ridge0'):
                if lying:
                    if face not in ('left', 'right'):
                        return None
                    # along the breast the plates stand up out of it
                    reach = 1 + (2 if px % 4 in (1, 2) else 0) + (1 if px % 4 == 1 else 0)
                    if fh - 1 - py >= reach:
                        return None
                    return FLAME_L if py == fh - 1 else FLAME
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
        elif b.mat == 'eye':
            def eye(face, px, py, fw, fh, rnd):
                if face != 'front':
                    return None
                # an eye standing on end, as tall as the crack: lids of the night, a white that is not white, a ring, a slit
                cx, cy = px - (fw - 1) / 2, py - (fh - 1) / 2
                lid = (1.0 - (cy / (fh / 2)) ** 2)
                if lid <= 0.0:
                    return None
                half = (fw / 2 - 0.5) * lid ** 0.8
                if abs(cx) > half:
                    return None
                if abs(cx) > half - 1.2:
                    return NIGHT_D
                r = math.hypot(cx, cy * 0.55)
                if abs(cx) < 0.9 + 1.6 * max(0.0, 1.0 - (cy / 15.0) ** 2) and abs(cy) < 15.0:
                    return (2, 2, 6)                                  # the slit
                if r < 10.5:
                    spoke = blotch('iris', math.atan2(cy, cx) * 5.0, r, 0.9)
                    return mix(VIOLET_D, VIOLET_L, min(1.0, max(0.0, (r - 3.0) / 7.5)) * (0.6 + 0.4 * spoke))
                if r < 11.6:
                    return CORONA
                return mix(NIGHT, (58, 44, 96), blotch('white', px, py, 3.0))
            p.fill_box(b, eye)
            fx, fy, fw, fh = b.faces()['front']
            for x in range(fw):
                for y in range(fh):
                    c = p.tex.getpixel((fx + x, fy + y))
                    if c[3] and (c[0] > 90 or c[2] > 110):
                        p.px(fx + x, fy + y, c, glow=True)
                    elif c[3] and c[2] > 60 and blotch('vein', x, y, 1.6) > 0.72:
                        p.px(fx + x, fy + y, VIOLET_D, glow=True)       # veins in the white of it
        elif b.mat == 'disc':
            def disc(face, px, py, fw, fh, rnd):
                if face not in ('front', 'back'):
                    return None
                r = math.hypot(px - (fw - 1) / 2, py - (fh - 1) / 2)
                ang = math.atan2(py - (fh - 1) / 2, px - (fw - 1) / 2)
                ray = 10.2 + 0.9 * math.cos(ang * 9) * math.cos(ang * 4 + 1.0)
                if r > ray:
                    return None
                if face == 'back':
                    return shade(NIGHT, 0.8)
                return (2, 2, 6) if r < 7.8 else CORONA
            p.fill_box(b, disc)
            fx, fy, fw, fh = b.faces()['front']
            for x in range(fw):
                for y in range(fh):
                    c = p.tex.getpixel((fx + x, fy + y))
                    if c[3] and c[0] > 100:
                        r = math.hypot(x - (fw - 1) / 2, y - (fh - 1) / 2)
                        col = CORONA if r < 9.0 else VIOLET_L
                        p.px(fx + x, fy + y, col, glow=True)
        elif b.mat == 'mask':
            def mask(face, px, py, fw, fh, rnd):
                if face not in ('front', 'back'):
                    return None
                mid = (fw - 1) / 2
                # a long face: wide at the brow, drawn in to a chin
                half = fw / 2 - (0 if py < 6 else (py - 6) * 0.42)
                if abs(px - mid) > half:
                    return None
                c = GOLD_L if blotch('mask', px, py, 3.0) > 0.6 else GOLD
                if abs(px - mid) < 0.6 and py > 7:
                    c = GOLD_D                                        # a ridge where a nose would be
                if py in (0, 1) or abs(abs(px - mid) - half) < 1.0:
                    c = GOLD_D
                return shade(c, (1.0 - 0.12 * (py / fh)) * (1.0 if face == 'front' else 0.55))
            p.fill_box(b, mask)
            # the slits it looks through: narrow, slanting to the middle, and something behind them
            for side in (-1, 1):
                for k in range(5):
                    x = int(round((b.tw - 1) / 2 + side * (1.5 + k)))
                    y = 6 - (k // 2)
                    p.put(b, 'front', x, y, STAR if k in (1, 2, 3) else VIOLET_L, glow=True)
            # nails: where it was fixed on
            for x, y in ((1, 1), (b.tw - 2, 1), (int((b.tw - 1) / 2), b.th - 2), (3, 9), (b.tw - 4, 9)):
                p.put(b, 'front', x, y, NIGHT)
        elif b.mat == 'horn':
            def horn(face, px, py, fw, fh, rnd):
                if face not in ('front', 'back'):
                    return None
                x = px if face == 'front' else fw - 1 - px
                # a crescent lying on its back over the brow, one tip longer than the other
                cx = (fw - 1) / 2
                r = math.hypot(x - cx, py + 1.5)
                inner = math.hypot(x - cx - 1.8, py + 5.2)
                if r > 14.9 or inner < 14.2:
                    return None
                return shade(GOLD_L if r > 13.9 else GOLD, 1.0 if face == 'front' else 0.6)
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


# ---------------------------------------------------------------------------------------------------- what it reaches with
# A hole in the floor, round, of the same night, with a rim of light; and out of the hole an arm of four lengths, as
# tall as a house, thinning to a wrist, with a hand on it that is pale and a man's but for the length of its fingers.
# It comes straight up, stands, and comes down full length along the floor, the way the model faces (-z).
TENDRIL_SCALE = 2.0             # must match MODEL_SCALE in TendrilModel.java
TENDRIL_SEGS = ((5, 12), (4, 12), (3, 11), (2, 10))

T = Model(128, 64, TENDRIL_SCALE)
T.part('portal', None, (0, 24, 0), boxes=[Box(-13, -0.5, -13, 26, 0.5, 26, 'portal', 'portal')])
parent, at = None, (0, 24, 0)
for i, (wide, long) in enumerate(TENDRIL_SEGS):
    T.part(f'seg{i}', parent, at, boxes=[Box(-wide / 2, -long, -wide / 2, wide, long, wide, 'limb' if i < 3 else 'wrist', f'seg{i}')])
    parent, at = f'seg{i}', (0, -long, 0)
T.part('hand', parent, at, boxes=[Box(-3, -4, -1, 6, 4, 2, 'palm', 'palm'), Box(-3, -10, -0.5, 1, 6, 1, 'talon', 't0'), Box(-1, -12, -0.5, 1, 8, 1, 'talon', 't1'),
                                  Box(1, -12, -0.5, 1, 8, 1, 'talon', 't2'), Box(3, -9, -0.5, 1, 5, 1, 'talon', 't3'), Box(-5, -4, -0.5, 2, 1, 1, 'talon', 'thumb')])
T.pack()
TENDRIL_HEIGHT = sum(long for _, long in TENDRIL_SEGS) + 12

TENDRIL_POSES = (
    ('up', {}),
    ('back', {'seg0': (-0.3, 0, 0), 'seg1': (-0.15, 0, 0), 'seg2': (0.1, 0, 0), 'hand': (0.5, 0, 0)}),
    ('down', {'seg0': (1.38, 0, 0), 'seg1': (0.14, 0, 0), 'seg2': (0.05, 0, 0), 'hand': (-0.25, 0, 0)}),
)


def make_tendril():
    p = Painter(T, 911)
    for name, _, b in T.boxes():
        if b.mat == 'portal':
            def portal(face, px, py, fw, fh, rnd):
                if face != 'top':
                    return None
                r = math.hypot(px - (fw - 1) / 2, py - (fh - 1) / 2) / (fw / 2)
                edge = 0.88 + (blotch('portal', px, py, 2.6) - 0.5) * 0.2
                if r > edge:
                    return None
                return (2, 2, 6)
            p.fill_box(b, portal)
            fx, fy, fw, fh = b.faces()['top']
            for x in range(fw):
                for y in range(fh):
                    if p.tex.getpixel((fx + x, fy + y))[3] == 0:
                        continue
                    r = math.hypot(x - (fw - 1) / 2, y - (fh - 1) / 2) / (fw / 2)
                    edge = 0.88 + (blotch('portal', x, y, 2.6) - 0.5) * 0.2
                    if r > edge - 0.08:
                        p.px(fx + x, fy + y, VIOLET_L if (x + y) % 3 else STAR, glow=True)
                    elif r > edge - 0.18:
                        p.px(fx + x, fy + y, VIOLET_D, glow=True)
                    elif blotch('ps', x * 7.3, y * 5.1, 0.6) > 0.93:
                        p.px(fx + x, fy + y, STAR_D, glow=True)
        elif b.mat == 'limb':
            p.fill_box(b, night(f't{b.tag}', light=1.15))
            starry(p, b, 't', 0.012)
            if b.tag in ('seg0', 'seg1'):
                cracks(p, b, 't', 1, colour=VIOLET_D, bright=VIOLET)
        elif b.mat == 'wrist':
            dark = night('tw', light=1.15)
            pale = skin('tw', PALE_D, PALE_DD)
            # (it runs up from its pivot: the top of the picture is the hand's end)
            p.fill_box(b, lambda face, px, py, fw, fh, rnd: mix(pale(face, px, py, fw, fh, rnd), dark(face, px, py, fw, fh, rnd),
                                                                 (py / max(1, fh - 1)) if face not in ('top', 'bottom') else (0.0 if face == 'top' else 1.0)))
        elif b.mat == 'palm':
            p.fill_box(b, skin('tp'))
        elif b.mat == 'talon':
            p.fill_box(b, lambda face, px, py, fw, fh, rnd: shade(NAIL if py == 0 else (PALE_D if py < fh // 2 else PALE), 1.0 if face != 'back' else 0.8))
    return p.tex, p.glow


def head_at(pose):
    """Where the middle of the disc is in a pose, in blocks: (out before where it stands, up)."""
    m, t = M.transform('head', pose)
    return round(-t[2] * SCALE / 16.0, 2), round((24.0 - t[1]) * SCALE / 16.0, 2)


if __name__ == '__main__':
    if len(sys.argv) < 3:
        M.write_java(JAVA)
        write_poses(JAVA)
        T.write_java(TENDRIL_JAVA)
    tex, glow = make()
    tex.save(f'{A}/textures/entity/coming.png')
    glow.save(f'{A}/textures/entity/coming_glow.png')
    ttex, tglow = make_tendril()
    ttex.save(f'{A}/textures/entity/tendril.png')
    tglow.save(f'{A}/textures/entity/tendril_glow.png')
    if len(sys.argv) > 1 and sys.argv[1] == '-':
        # the numbers ComingModel has: every part's turn in every pose, and where the head is
        for label, pose in POSES.items():
            print(label, 'head', head_at(pose))
            print('   ', {k: v for k, v in pose.items() if not k.startswith('crown')})
    elif len(sys.argv) > 1:
        cam = dict(size=(330, 440), zoom=1.5, centre=(0, -50), glow=glow, background=(58, 58, 66))
        shots, labels = [], []
        for label, pose in POSES.items():
            hidden = ('eye',) if label in MASK_ON else ('mask',)
            for yaw in ((-0.5, -1.45) if label in ('masked', 'bare') else (-0.5,)):
                shots.append(M.render(tex, yaw=yaw, pitch=0.12, pose=pose, hidden=hidden, **cam))
                labels.append(label)
        shots.append(M.render(tex, yaw=0.0, pitch=0.12, pose=POSES['masked'], hidden=('eye',), **cam))
        shots.append(M.render(tex, yaw=0.0, pitch=0.12, pose=POSES['bare'], hidden=('mask',), **cam))
        labels += ['masked', 'bare']
        tcam = dict(size=(330, 440), zoom=3.4, centre=(0, -30), glow=tglow, background=(58, 58, 66))
        for label, pose in TENDRIL_POSES:
            shots.append(T.render(ttex, yaw=-1.2, pitch=0.2, pose=pose, **tcam))
            labels.append('tendril ' + label)
        sheet(shots, 6, labels=labels).save(sys.argv[1])
