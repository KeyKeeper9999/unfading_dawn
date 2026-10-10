#!/usr/bin/env python3
"""The Coming - what was to arrive on the day that was not let begin: mesh for ComingModel.java, its texture and what
shines of it; and what it reaches into the hall with (TendrilModel.java: an arm of the same night, out of a hole in
the floor).

It is in the Tear - a crack in the wall of the last hollow of the Sky Beneath, from the floor up, with the sky of some
other place behind it - and it is coming out of it. It has no head and no neck, and nothing of a beast about it: it
is a mass of shoulders as wide as a gate, of the night itself, cracked, with light in the cracks; and set in the
breast of that mass, as tall as the breast, a black disc with a rim of light - a sun that has not risen, and nothing in
the middle of it - with a broken ring of light round it and a crown of arms round that, like the rays of it. Two arms
a house long are what it drags itself by; lesser ones grow down its flanks; under the disc its belly opens. The
Founder's rite nailed a mask of gold over the disc. Masked (its first phase) all of it but the disc and its two arms
is still in the rock: the mask looks out of the crack. With the mask off (its second) it comes out to the waist and
hangs over the hall; the crown opens round the disc - and behind it, in the crack, an eye opens that is as tall as
the crack.

'body' is all of it that comes and goes: masked, the model draws it BACK_IN further back, which is in the rock. 'eye'
stays in the crack. The face of the rock is WALL behind where it stands. (It was a neck like a tower with the disc on
top of it, twice; the owner saw a giraffe in the second. Nothing is to stand on a stalk.)
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
CROWN = 12                      # the arms round the disc
DISC_R = 20                     # the disc is so far from its middle to its rim
HALO_R = 28                     # and the ring of light round it
WALL = 10                       # the face of the rock, so far behind where it stands (a block and a half)
BACK_IN = 14                    # masked, it is so far back in the crack (ComingModel.BACK_IN)
OUT_FRONT = 8                   # bare, it is so far out before where it stands (ComingModel.OUT_FRONT)
BACK = (26, 28, 44)

M = Model(512, 256, SCALE)
M.part('all', None, (0, 24, 0))
M.part('eye', 'all', (0, -56, WALL - 0.5), boxes=[Box(-19, -34, 0, 38, 68, 1, 'eye', 'eye')])
M.part('body', 'all', (0, 0, 0), boxes=[Box(-9, -16, 10, 18, 16, 26, 'hide', 'hind')])
# the waist: what joins the mass to what of it is still in the crack; its belly opens (two lips of hide over the red)
M.part('waist', 'body', (0, -6, 8), (0.2, 0, 0), boxes=[Box(-12, -24, -8, 24, 32, 16, 'hide', 'waist'), Box(-8, -21, -8.6, 16, 19, 1, 'throat', 'throat')])
M.part('maw_r', 'waist', (-8, -21, -9), boxes=[Box(0, 0, -1, 8, 19, 1, 'lip', 'lip')])
M.mirror('maw_r', 'maw_l', parent='waist')
# the mass: shoulders over a breast, a hump where a head would be
M.part('torso', 'waist', (0, -24, 0), (0.25, 0, 0), boxes=[Box(-26, -36, -11, 52, 21, 22, 'hide', 'shoulders'), Box(-18, -15, -9, 36, 17, 18, 'hide', 'chest'),
                                                           Box(-14, -42, -7, 28, 6, 15, 'hide', 'hump'), Box(-0.5, -46, -5, 1, 4, 14, 'ridge', 'ridge0'),
                                                           Box(-0.5, -38, 11, 1, 36, 4, 'ridge', 'ridge1')])
# the disc in the breast of it, the ring round the disc, the mask over it
M.part('disc', 'torso', (0, -20, -12), boxes=[Box(-DISC_R, -DISC_R, -3, 2 * DISC_R, 2 * DISC_R, 3, 'disc', 'disc')])
M.part('halo', 'disc', (0, 0, -0.5), boxes=[Box(-HALO_R, -HALO_R, 0, 2 * HALO_R, 2 * HALO_R, 1, 'ring', 'ring')])
M.part('mask', 'disc', (0, 0, -3), boxes=[Box(-15, -DISC_R, -1.5, 30, 38, 1, 'mask', 'mask'), Box(-24, -39, -1, 48, 23, 1, 'horn', 'horn')])
for i in range(CROWN):
    ang = (i + 0.5) * 2 * math.pi / CROWN
    M.part(f'crown{i}', 'disc', (round(math.sin(ang) * (DISC_R - 2), 2), round(-math.cos(ang) * (DISC_R - 2), 2), -1), (-2.5, 0, round(ang, 4)),
           boxes=[Box(-1, -16, -1, 2, 16, 2, 'limb', 'crown_upper')])
    M.part(f'crownf{i}', f'crown{i}', (0, -16, 0), (2.4, 0, 0), boxes=[Box(-0.5, -14, -0.5, 1, 14, 1, 'limb', 'crown_fore'), Box(-2.5, -21, -0.5, 5, 8, 1, 'claw', 'crown_claw')])
# the two arms it drags itself by, from the ends of the shoulders: each a house long, a hand on it that would hold a man
M.part('arm_r', 'torso', (-25, -27, 0), boxes=[Box(-4, -30, -4, 8, 30, 8, 'hide', 'arm')])
M.part('fore_r', 'arm_r', (0, -30, 0), boxes=[Box(-3.5, -30, -3.5, 7, 30, 7, 'hide', 'fore')])
M.part('hand_r', 'fore_r', (0, -30, 0), boxes=[Box(-6, -8, -2, 12, 8, 4, 'hide', 'palm'), Box(-6, -18, -1, 2, 10, 2, 'talon', 't0'), Box(-2.7, -21, -1, 2, 13, 2, 'talon', 't1'),
                                               Box(0.7, -21, -1, 2, 13, 2, 'talon', 't2'), Box(4, -18, -1, 2, 10, 2, 'talon', 't3'), Box(-9, -6, -1, 3, 2, 2, 'talon', 'thumb')])
for src, dst, parent in (('arm_r', 'arm_l', 'torso'), ('fore_r', 'fore_l', 'arm_l'), ('hand_r', 'hand_l', 'fore_l')):
    M.mirror(src, dst, parent=parent)
# the lesser arms down its flanks: they hang, and work
RIBS = 5
for k in range(RIBS):
    under = 'torso' if k < 3 else 'waist'
    y = (-12 + k * 6) if k < 3 else (-20 + (k - 3) * 9)
    M.part(f'rib_r{k}', under, (-18 if k < 3 else -12, y, -1), (-0.7, 0, -1.9 + 0.12 * k), boxes=[Box(-0.75, -9, -0.75, 1.5, 9, 1.5, 'limb', 'rib')])
    M.part(f'ribf_r{k}', f'rib_r{k}', (0, -9, 0), (-0.9, 0, 0), boxes=[Box(-0.5, -8, -0.5, 1, 8, 1, 'limb', 'rib_fore'), Box(-2, -13, -0.5, 4, 6, 1, 'claw', 'rib_claw')])
    M.mirror(f'rib_r{k}', f'rib_l{k}', parent=under)
    M.mirror(f'ribf_r{k}', f'ribf_l{k}', parent=f'rib_l{k}')
M.pack()

CORONA = (236, 226, 255)
FLAME = VIOLET                  # the ridge down its back
FLAME_L = VIOLET_L


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


def arms(pose, upper, fore, hand=(0.0, 0, 0), sides='rl'):
    """Both great arms (or one): where the upper arm and the forearm point, given for the right one (x is mirrored for
    the left), and how the hand is turned on the wrist. Said after the lean of the pose: the arms hang from the mass."""
    for side in sides:
        m = np.array([1, 1, 1]) if side == 'r' else np.array([-1, 1, 1])
        aim(pose, f'arm_{side}', np.array(upper) * m)
        aim(pose, f'fore_{side}', np.array(fore) * m)
        pose[f'hand_{side}'] = hand
    return pose


def crown_open(spread=0.0, curl=0.6):
    pose = {}
    for i in range(CROWN):
        pose[f'crown{i}'] = (2.5 - 0.35 + spread, 0, 0)
        pose[f'crownf{i}'] = (-2.4 + 0.5 - curl, 0, 0)
    return pose


def lean(pose, waist=0.0, torso=0.0, disc=0.0, off=None):
    """How far forward the waist and the mass on it are bowed, and the disc turned in the breast (added to their rest)."""
    pose.update(waist=(waist, 0, 0), torso=(torso, 0, 0), disc=(disc, 0, 0))
    if off is not None:
        pose['body'] = {'off': off}
    return pose


PLANTED = ((-0.5, 0.78, -0.38), (0.1, 0.88, -0.46), (1.1, 0, 0))      # the hands on the floor before it, elbows out
GRIP = ((-0.3, 0.55, -0.78), (0.42, 0.85, -0.32), (1.1, 0, 0))         # masked: out of the crack and down, the hands at its foot


def out(waist=0.0, torso=0.0, disc=0.0, hands=PLANTED, crown=None, **extra):
    """Its second phase: the mass out of the rock and hanging over the hall, the crown open (the rest pose of the mesh
    has the arms straight up: every pose says where they are)."""
    pose = dict(crown or crown_open())
    lean(pose, waist, torso, disc, (0, 0, -OUT_FRONT))
    arms(pose, *hands)
    pose.update(extra)
    return pose


def masked(waist=-0.2, torso=-0.25, disc=0.0, hands=GRIP, off=(0, 0, BACK_IN), **extra):
    """Its first phase: back in the crack, upright, so that the mask stands in it and looks out; the two arms out and down."""
    pose = {}
    lean(pose, waist, torso, disc, off)
    arms(pose, *hands)
    pose.update(extra)
    return pose


def one(pose, upper, fore, hand=(0.4, 0, 0)):
    return arms(dict(pose), upper, fore, hand, sides='r')


SWEEP_BACK = ((-0.9, 0.1, -0.42), (-0.85, 0.35, -0.4))
SWEEP_ROUND = ((0.55, 0.45, -0.7), (0.8, 0.4, -0.45))
UP = ((-0.45, -0.75, -0.48), (-0.15, -0.9, -0.4), (0.3, 0, 0))
DOWN = ((-0.25, 0.6, -0.76), (0.1, 0.92, -0.38), (1.4, 0, 0))
SUNK = ((-0.5, 0.7, -0.5), (0.25, 0.95, -0.2), (1.5, 0, 0))
# what ComingModel asks for by name. `python3 tools/gen_coming.py - x` prints where the middle of the disc is in each
POSES = {
    'masked': masked(),
    # the mask thrust out of the crack and tipped down at whoever it looks at: the rays come out of its slits
    'gaze': masked(-0.05, -0.05, 0.3, off=(0, 0, BACK_IN - 8)),
    # drawn back into the dark, and then the whole mass of it out and down on its face before the crack
    'slam_back': masked(-0.4, -0.4, -0.2, off=(0, 0, BACK_IN + 8)),
    'slam': masked(0.75, 0.45, 0.0, hands=((-0.75, 0.2, -0.63), (-0.3, 0.6, -0.74), (0.6, 0, 0)), off=(0, 0, 0)),
    # an arm let go of the floor and brought round, low
    'sweep_back': one(masked(), *SWEEP_BACK),
    'sweep': one(masked(), *SWEEP_ROUND),
    # both arms up, and down on whoever is at the foot of the crack
    'crush_up': masked(-0.3, -0.3, hands=UP),
    'crush': masked(-0.05, 0.0, hands=DOWN),
    # its hands in the floor of the hall: what it reaches with comes up elsewhere
    'reach': masked(0.0, 0.1, 0.2, hands=SUNK, off=(0, 0, BACK_IN - 6)),
    'bare': out(),
    'bare_gaze': out(-0.15, -0.15, 0.2),
    'bare_slam_back': out(-0.3, -0.35, -0.2, hands=((-0.8, 0.45, -0.4), (-0.2, 0.75, -0.63), (0.8, 0, 0))),
    'bare_slam': out(0.75, 0.42, 0.0, hands=((-0.8, 0.25, -0.55), (-0.35, 0.55, -0.76), (0.6, 0, 0))),
    'bare_sweep_back': one(out(), *SWEEP_BACK),
    'bare_sweep': one(out(), *SWEEP_ROUND),
    'bare_crush_up': out(-0.2, -0.2, hands=UP),
    'bare_crush': out(0.1, 0.1, hands=DOWN),
    'bare_reach': out(0.2, 0.15, 0.1, hands=SUNK),
    'pull': out(0.0, -0.2, 0.1, crown=crown_open(0.25), maw_r=(0, 1.35, 0), maw_l=(0, -1.35, 0)),
    'bite': out(0.55, 0.3, 0.0, maw_r=(0, 0.2, 0), maw_l=(0, -0.2, 0)),
    # the sky called down: both arms up, the mass thrown back, the crown wide
    'rain': out(-0.2, -0.3, -0.35, hands=UP, crown=crown_open(0.35, 0.2)),
    # the eye behind it looks: it bows down out of the way of it, the crown shut over the disc
    'glare': out(0.6, 0.45, 0.1, crown=crown_open(-0.9, 1.2)),
}
MASK_ON = ('masked', 'gaze', 'slam_back', 'slam', 'sweep_back', 'sweep', 'crush_up', 'crush', 'reach')
# the parts a pose turns, in the order ComingModel keeps them; and how far back in the crack a pose is (masked only)
POSED = ('waist', 'torso', 'disc', 'arm_r', 'fore_r', 'hand_r', 'arm_l', 'fore_l', 'hand_l', 'maw_r', 'maw_l')


def back_of(pose):
    return float(pose.get('body', {}).get('off', (0, 0, 0))[2])


def write_poses(path, indent='    '):
    """The poses as ComingModel has them: a row of three turns for every part of POSED, between the markers; and for
    the masked ones, how far back in the crack the whole of it is."""
    lines = []
    for label, pose in POSES.items():
        rows = []
        for part in POSED:
            v = pose.get(part, (0, 0, 0))
            v = v.get('rot', (0, 0, 0)) if isinstance(v, dict) else v
            rows.append('{' + ', '.join(f'{float(x):.3f}F' for x in v) + '}')
        lines.append(f'{indent}private static final float[][] {label.upper()} = {{' + ', '.join(rows) + '};')
    for label in MASK_ON:
        lines.append(f'{indent}private static final float BACK_{label.upper()} = {back_of(POSES[label]):.1f}F;')
    lines.append(f'{indent}private static final float OUT_FRONT = {float(OUT_FRONT):.1f}F;')
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
            if b.tag in ('shoulders', 'chest', 'waist'):
                cracks(p, b, 'c' + b.tag, 2, colour=BLUE, bright=BLUE_L)
            elif b.tag in ('arm', 'fore'):
                cracks(p, b, 'cv', 1, colour=VIOLET_D, bright=VIOLET)
            if b.tag == 'hump':
                # eyes along the hump where a head would be, small, and too many
                for x in range(2, b.tw - 2, 3):
                    p.put(b, 'front', x, 2 + (x // 3) % 2, STAR, glow=True)
                    p.put(b, 'front', x, 3 + (x // 3) % 2, NIGHT_D)
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
                iris = fw * 0.31
                if abs(cx) < 1.0 + 2.0 * max(0.0, 1.0 - (cy / (fh * 0.29)) ** 2) and abs(cy) < fh * 0.29:
                    return (2, 2, 6)                                  # the slit
                if r < iris:
                    spoke = blotch('iris', math.atan2(cy, cx) * 5.0, r, 0.9)
                    return mix(VIOLET_D, VIOLET_L, min(1.0, max(0.0, (r - 3.0) / (iris - 3.0))) * (0.6 + 0.4 * spoke))
                if r < iris + 1.2:
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
                R = fw / 2
                ray = R - 1.0 + 1.0 * math.cos(ang * 11) * math.cos(ang * 4 + 1.0)
                if r > ray:
                    return None
                if face == 'back':
                    return shade(NIGHT, 0.8)
                return (2, 2, 6) if r < R * 0.72 else CORONA
            p.fill_box(b, disc)
            fx, fy, fw, fh = b.faces()['front']
            for x in range(fw):
                for y in range(fh):
                    c = p.tex.getpixel((fx + x, fy + y))
                    if c[3] and c[0] > 100:
                        r = math.hypot(x - (fw - 1) / 2, y - (fh - 1) / 2)
                        col = CORONA if r < fw / 2 * 0.86 else VIOLET_L
                        p.px(fx + x, fy + y, col, glow=True)
        elif b.mat == 'ring':
            def ring(face, px, py, fw, fh, rnd):
                if face not in ('front', 'back'):
                    return None
                # a ring of light round the disc, broken in places: it does not close
                R = fw / 2
                r = math.hypot(px - (fw - 1) / 2, py - (fh - 1) / 2)
                ang = math.atan2(py - (fh - 1) / 2, px - (fw - 1) / 2)
                if not (R - 3.4 < r < R - 0.4) or math.cos(ang * 7 + 0.6) < -0.5:
                    return None
                return VIOLET_L if r > R - 1.5 else (VIOLET if r > R - 2.5 else VIOLET_D)
            p.fill_box(b, ring)
            for face in ('front', 'back'):
                fx, fy, fw, fh = b.faces()[face]
                for x in range(fw):
                    for y in range(fh):
                        c = p.tex.getpixel((fx + x, fy + y))
                        if c[3]:
                            p.glow.putpixel((fx + x, fy + y), c[:3] + (255,))
        elif b.mat == 'mask':
            def mask(face, px, py, fw, fh, rnd):
                if face not in ('front', 'back'):
                    return None
                mid = (fw - 1) / 2
                # a long face: wide at the brow, drawn in to a chin
                brow = int(fh * 0.3)
                half = fw / 2 - (0 if py < brow else (py - brow) * 0.42)
                if abs(px - mid) > half:
                    return None
                c = GOLD_L if blotch('mask', px, py, 3.0) > 0.6 else GOLD
                if abs(px - mid) < 1.1 and py > brow + 2:
                    c = GOLD_D                                        # a ridge where a nose would be
                if py in (0, 1) or abs(abs(px - mid) - half) < 1.5:
                    c = GOLD_D
                return shade(c, (1.0 - 0.12 * (py / fh)) * (1.0 if face == 'front' else 0.55))
            p.fill_box(b, mask)
            # the slits it looks through: narrow, slanting to the middle, and something behind them
            for side in (-1, 1):
                for k in range(9):
                    x = int(round((b.tw - 1) / 2 + side * (2.5 + k)))
                    y = int(b.th * 0.3) + 1 - (k // 3)
                    p.put(b, 'front', x, y, STAR if 1 <= k <= 6 else VIOLET_L, glow=True)
                    p.put(b, 'front', x, y + 1, GOLD_D)
            # nails: where it was fixed on
            for x, y in ((2, 2), (b.tw - 3, 2), (int((b.tw - 1) / 2), b.th - 3), (5, 16), (b.tw - 6, 16), (9, 26), (b.tw - 10, 26)):
                for dx, dy in ((0, 0), (1, 0), (0, 1), (1, 1)):
                    p.put(b, 'front', x + dx, y + dy, NIGHT)
        elif b.mat == 'horn':
            def horn(face, px, py, fw, fh, rnd):
                if face not in ('front', 'back'):
                    return None
                x = px if face == 'front' else fw - 1 - px
                # a crescent lying on its back over the brow, one tip longer than the other
                cx = (fw - 1) / 2
                R = fw / 2
                r = math.hypot(x - cx, py + 1.5)
                inner = math.hypot(x - cx - R * 0.12, py + R * 0.36)
                if r > R - 0.1 or inner < R * 0.95:
                    return None
                return shade(GOLD_L if r > R - 1.3 else GOLD, 1.0 if face == 'front' else 0.6)
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
            for x, y in ((2, 2), (9, 3), (5, 5), (12, 7), (3, 9), (8, 11), (13, 13), (4, 15), (10, 16), (6, 1)):
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


def disc_at(pose):
    """Where the middle of the disc is in a pose, in blocks: (out before where it stands, up)."""
    m, t = M.transform('disc', pose)
    return round(float(-t[2] * SCALE / 16.0), 2), round(float((24.0 - t[1]) * SCALE / 16.0), 2)


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
        # where the middle of the disc is in every pose: ComingEntity takes where its rays come from and where it falls from this
        for label, pose in POSES.items():
            print(label, 'disc (out, up):', disc_at(pose))
    elif len(sys.argv) > 1:
        cam = dict(size=(330, 400), zoom=1.3, centre=(0, -50), glow=glow, background=(40, 40, 50))
        shots, labels = [], []
        for label, pose in POSES.items():
            hidden = ('eye', 'halo') if label in MASK_ON else ('mask',)
            for yaw in ((-0.55, 0.0, -1.45) if label in ('masked', 'bare') else (-0.55,)):
                shots.append(M.render(tex, yaw=yaw, pitch=0.12, pose=pose, hidden=hidden, **cam))
                labels.append(label)
        tcam = dict(size=(330, 400), zoom=3.2, centre=(0, -30), glow=tglow, background=(40, 40, 50))
        for label, pose in TENDRIL_POSES:
            shots.append(T.render(ttex, yaw=-1.2, pitch=0.2, pose=pose, **tcam))
            labels.append('tendril ' + label)
        sheet(shots, 6, labels=labels).save(sys.argv[1])
