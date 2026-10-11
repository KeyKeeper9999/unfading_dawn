#!/usr/bin/env python3
"""The Coming - what was to arrive on the day that was not let begin: mesh for ComingModel.java, its poses, its texture
and what shines of it; what it reaches into the hall with (TendrilModel.java: an arm of the same night, out of a hole
in the floor); and the eyes that open in the rock when it is out (WallEyeModel.java).

It is in the Tear - a crack in the wall of the last hollow of the Sky Beneath, from the floor up, with the sky of some
other place behind it. It is not one thing but a weaving of several: three necks of the night twisted round one another
into a rope as thick as a tree, loose at the root and tight at the end, and on the end of the rope a black disc with a
rim of light - a sun that has not risen, and nothing in the middle of it - a broken ring of light round the disc and a
crown of arms round that, like the rays of it. Small arms come loose from the rope all along it. The Founder's rite
nailed a mask of gold over the disc.

Masked (its first phase) it looks out of the crack: the rope comes out of the dark over the hall and the mask hangs on
it and looks down; two arms a house long are out to the floor either side; and round the crack what else there is of
it has crept out along the rock ('roots'). The knot the rope grows from is still in the dark.
With the mask off (its second) all of it comes out: the knot - a tangle of the same necks, with a mouth in it - on four
arms a house long that it walks on, the rope standing up out of the knot with the disc on top, the crown open, and a
tail of the weaving dragging behind. It goes about the hall. What is left in the crack is an eye.

'turn' is turned by ComingModel to whoever it fights while it is in the crack (its pivot is the middle of the crack's
foot: the face of the rock is WALL behind where it stands); 'body' is all of it that comes and goes, and a pose says
where the body is ('off': up, and back into the rock).
Run from the repo root; pass a file name to also get a preview sheet (and a second argument to leave the Java alone)."""
import math
import sys

import numpy as np

from duskkit import (BLUE, BLUE_L, GOLD, GOLD_D, GOLD_L, NAIL, NIGHT, NIGHT_D, NIGHT_L, PALE, PALE_D, PALE_DD, RAW, RAW_D, STAR, STAR_D, TOOTH, TOOTH_D,
                     VIOLET, VIOLET_D, VIOLET_L, cracks, hand_up, night, skin, starry)
from modelkit import Box, Model, Painter, blotch, mix, rot_matrix, shade, sheet

A = 'src/main/resources/assets/unfading_dawn'
JAVA = 'src/main/java/com/keykeeper/dushi/client/ComingModel.java'
ENTITY_JAVA = 'src/main/java/com/keykeeper/dushi/entity/ComingEntity.java'
TENDRIL_JAVA = 'src/main/java/com/keykeeper/dushi/client/TendrilModel.java'
EYE_JAVA = 'src/main/java/com/keykeeper/dushi/client/WallEyeModel.java'
SCALE = 2.4                     # must match MODEL_SCALE in ComingModel.java
BLOCK = 16.0 / SCALE            # so many of the model's units to a block
CROWN = 12                      # the arms round the disc
DISC_R = 20                     # the disc is so far from its middle to its rim
HALO_R = 28                     # and the ring of light round it
WALL = 10                       # the face of the rock, so far behind where it stands (a block and a half)
NECK = 8                        # the lengths of the rope
NECK_L = 12
TAIL = 4
TRUNK_H = 34                    # the middle of the knot is so far over the floor when it stands on its arms
# its six arms, a pair at a time from the fore one back: the names of the three parts of one, where its shoulder is on the knot, its two lengths
LIMBS = (('arm', 'fore', 'hand', (-13, -6, -11), (30, 50)), ('mid', 'midf', 'midh', (-14, -4, 1), (28, 46)), ('leg', 'shin', 'foot', (-12, -4, 12), (28, 48)))
LENGTHS = {upper: lengths for upper, _, _, _, lengths in LIMBS}
LESSER = (('lesser_r', (-9, -11, -1), 5, 11), ('lesser_l', (8, -11, 3), 4, 12))    # the two lesser necks beside the rope: where on the knot, how many lengths, how long each
IN_ROCK = (0, -12, 36)          # masked, the body is so far up and back (the knot is in the dark behind the face of the rock)

M = Model(512, 512, SCALE)
M.part('all', None, (0, 24, 0))


def euler(R, near=None):
    """The turns about x, y and z (in the order the game makes them: z, then y, then x) that make the matrix R; of the
    two ways of writing them, and of whole turns, the one nearest to `near`."""
    ry = -math.asin(max(-1.0, min(1.0, R[2, 0])))
    rx = math.atan2(R[2, 1], R[2, 2])
    rz = math.atan2(R[1, 0], R[0, 0])
    ways = [(rx, ry, rz), (rx + math.pi, math.pi - ry, rz + math.pi)]
    near = near or (0.0, 0.0, 0.0)
    best = None
    for way in ways:
        fit = tuple(v - 2 * math.pi * round((v - n) / (2 * math.pi)) for v, n in zip(way, near))
        far = sum((v - n) ** 2 for v, n in zip(fit, near))
        if best is None or far < best[0]:
            best = (far, fit)
    return best[1]


def frame_along(d, hint=None):
    """A frame whose -y runs along `d`: for a box that runs up from its pivot."""
    y = -np.array(d, float) / np.linalg.norm(d)
    if hint is None:
        hint = min(((1, 0, 0), (0, 0, 1), (0, 1, 0)), key=lambda h: abs(float(np.dot(h, y))))
    x = np.array(hint, float) - float(np.dot(hint, y)) * y
    x /= np.linalg.norm(x)
    return np.column_stack([x, y, np.cross(x, y)])


def arc(a, b):
    """The shortest turn that takes the direction a to b."""
    a = np.array(a, float) / np.linalg.norm(a)
    b = np.array(b, float) / np.linalg.norm(b)
    v = np.cross(a, b)
    c = float(np.dot(a, b))
    if c < -0.9999:
        return frame_along(a) @ np.diag([1.0, -1.0, -1.0]) @ frame_along(a).T
    k = np.array([[0, -v[2], v[1]], [v[2], 0, -v[0]], [-v[1], v[0], 0]])
    return np.eye(3) + k + k @ k * (1.0 / (1.0 + c))


# ---------------------------------------------------------------------------------------------------- the mesh
# what of it has crept out of the crack along the rock: only while it is in the crack (ComingModel draws them back into it)
ROOTS = ((-22, 6, 1.95, 18, 14, 0.5), (-23, 25, 1.45, 20, 14, -0.45), (-29, 42, 1.2, 18, 16, 0.5), (-29, 58, 0.85, 20, 14, -0.55),
         (-16, 73, 0.5, 18, 12, 0.6), (-7, 85, 0.12, 14, 10, -0.5))
M.part('roots', 'all', (0, 0, WALL - 1.6))
for k, (x, h, lean, long, fore, bend) in enumerate(ROOTS):
    M.part(f'root_r{k}', 'roots', (x, -h, 0), (0, 0, -lean), boxes=[Box(-1.5, -long, -1.5, 3, long, 3, 'limb', 'root')])
    M.part(f'rootf_r{k}', f'root_r{k}', (0, -long, 0), (0, 0, bend), boxes=[Box(-1, -fore, -1, 2, fore, 2, 'limb', 'root_fore'), Box(-2.5, -fore - 7, -0.5, 5, 8, 1, 'claw', 'root_claw')])
    M.mirror(f'root_r{k}', f'root_l{k}', parent='roots')
    M.mirror(f'rootf_r{k}', f'rootf_l{k}', parent=f'root_l{k}')
# (the rock it is in, for the pictures only: the face of it, with the crack)
PREVIEW = []
for cx in range(-8, 9):
    for h in range(0, 16):
        half = 3 if h <= 4 else (4 if h <= 9 else (2 if h <= 11 else (1 if h == 12 else -1)))
        open_ = abs(cx) <= half
        PREVIEW.append(Box((cx - 0.5) * BLOCK, -(h + 1) * BLOCK, 4 if open_ else 0, BLOCK, BLOCK, BLOCK, 'pv_sky' if open_ else 'pv_rock', 'pv', uv=(0, 484) if open_ else (30, 484)))
M.part('pv_wall', 'all', (0, 0, WALL), boxes=PREVIEW, emit=False)
M.part('turn', 'all', (0, 0, WALL))
M.part('body', 'turn', (0, 0, -WALL))
# the knot: a core nobody sees for what is wound round it, a ridge of light along the top of it, and a mouth in the front
M.part('trunk', 'body', (0, -TRUNK_H, 0), boxes=[Box(-12, -11, -14, 24, 22, 28, 'hide', 'core'), Box(-0.5, -16, -12, 1, 5, 24, 'ridge', 'ridge0'),
                                                 Box(-8, -2, -15.2, 16, 12, 1, 'throat', 'throat')])
COILS = (((0, -5, 2), (0.5, 0.3, 0.9), 38, 10, 's0'), ((0, 4, -4), (-0.4, -0.5, -0.8), 36, 9, 's1'), ((0, -9, 6), (0.2, 1.2, 0.3), 40, 9, 's2'),
         ((0, 6, 9), (-0.9, 0.4, 0.2), 34, 10, 's0'), ((9, -1, -3), (0.1, -0.2, 1.45), 34, 9, 's1'), ((-9, 0, 3), (-0.2, 0.25, -1.4), 34, 9, 's2'),
         ((0, 9, -1), (0.15, 0.1, 0.12), 40, 8, 's1'), ((0, -1, 13), (0.9, 1.45, 0.0), 36, 9, 's0'))
for i, (at, rot, long, wide, mat) in enumerate(COILS):
    M.part(f'coil{i}', 'trunk', at, rot, boxes=[Box(-long / 2, -wide / 2, -wide / 2, long, wide, wide, mat, 'coil')])
M.part('maw_r', 'trunk', (-8, -2, -15.6), boxes=[Box(0, 0, -1, 8, 12, 1, 'lip', 'lip')])
M.mirror('maw_r', 'maw_l', parent='trunk')
# what hangs under the knot: ends of the weaving that found nothing to be woven into, each with a hand on it
DANGLE = ((-7, -8, 13, 11, 0.25), (6, -3, 15, 9, -0.3), (-3, 4, 11, 12, 0.2), (8, 9, 14, 8, 0.35), (-8, 10, 10, 10, -0.25), (1, -11, 9, 9, 0.3))
for k, (x, z, long, fore, bend) in enumerate(DANGLE):
    M.part(f'dangle{k}', 'trunk', (x, 10, z), (math.pi + 0.12 * (k % 3 - 1), 0, 0.1 * (k % 2 * 2 - 1)), boxes=[Box(-1, -long, -1, 2, long, 2, 'limb', 'root')])
    M.part(f'danglef{k}', f'dangle{k}', (0, -long, 0), (bend, 0, 0), boxes=[Box(-0.75, -fore, -0.75, 1.5, fore, 1.5, 'limb', 'root_fore'), Box(-2.5, -fore - 7, -0.5, 5, 8, 1, 'claw', 'root_claw')])
# the rope: three necks wound round one another, loose where it grows out of the knot and tight under the disc
NECK_R = (8.5, 7.6, 6.7, 5.9, 5.2, 4.6, 4.0, 3.6, 3.3)
NECK_W = (7, 6, 6, 5, 5, 5, 4, 4)
TWIST = 1.05
parent, at = 'trunk', (0, -12, -8)
for i in range(NECK):
    M.part(f'neck{i}', parent, at)
    for k in range(3):
        a0 = k * 2 * math.pi / 3 + i * TWIST
        a1 = a0 + TWIST
        foot = np.array([NECK_R[i] * math.cos(a0), 0.0, NECK_R[i] * math.sin(a0)])
        top = np.array([NECK_R[i + 1] * math.cos(a1), -NECK_L, NECK_R[i + 1] * math.sin(a1)])
        long = float(np.linalg.norm(top - foot)) + 1.5
        w = NECK_W[i]
        M.part(f'ns{i}_{k}', f'neck{i}', tuple(round(float(v), 2) for v in foot), tuple(round(v, 4) for v in euler(frame_along(top - foot, (math.cos(a0), 0, math.sin(a0))))),
               boxes=[Box(-w / 2, -round(long), -w / 2, w, round(long), w, f's{k}', 'strand')])
    # an arm come loose from the rope
    side = -1 if i % 2 == 0 else 1
    M.part(f'fray{i}', f'neck{i}', (side * (NECK_R[i] + 2), -NECK_L * 0.5, 1 if i % 3 else -2), (-0.7 + 0.25 * (i % 3), 0, side * (1.9 - 0.12 * i)),
           boxes=[Box(-0.75, -10, -0.75, 1.5, 10, 1.5, 'limb', 'rib')])
    M.part(f'frayf{i}', f'fray{i}', (0, -10, 0), (-0.9, 0, 0), boxes=[Box(-0.5, -9, -0.5, 1, 9, 1, 'limb', 'rib_fore'), Box(-2, -14, -0.5, 4, 6, 1, 'claw', 'rib_claw')])
    parent, at = f'neck{i}', (0, -NECK_L, 0)
# the head of it: where the necks end in one, the disc on that, the ring round the disc, the mask over it
M.part('head', parent, at, (-math.pi / 2, 0, 0), boxes=[Box(-6, -6, -3, 12, 12, 8, 'hide', 'nape')])
M.part('disc', 'head', (0, 0, -4), boxes=[Box(-DISC_R, -DISC_R, -3, 2 * DISC_R, 2 * DISC_R, 3, 'disc', 'disc')])
M.part('halo', 'disc', (0, 0, -0.5), boxes=[Box(-HALO_R, -HALO_R, 0, 2 * HALO_R, 2 * HALO_R, 1, 'ring', 'ring')])
M.part('mask', 'disc', (0, 0, -3), boxes=[Box(-15, -DISC_R, -1.5, 30, 38, 1, 'mask', 'mask'), Box(-24, -39, -1, 48, 23, 1, 'horn', 'horn')])
for i in range(CROWN):
    ang = (i + 0.5) * 2 * math.pi / CROWN
    M.part(f'crown{i}', 'disc', (round(math.sin(ang) * (DISC_R - 2), 2), round(-math.cos(ang) * (DISC_R - 2), 2), -1), (-2.5, 0, round(ang, 4)),
           boxes=[Box(-1, -16, -1, 2, 16, 2, 'limb', 'crown_upper')])
    M.part(f'crownf{i}', f'crown{i}', (0, -16, 0), (2.4, 0, 0), boxes=[Box(-0.5, -14, -0.5, 1, 14, 1, 'limb', 'crown_fore'), Box(-2.5, -21, -0.5, 5, 8, 1, 'claw', 'crown_claw')])
# the four arms it walks on, each a house long, a hand on it that would hold a man
HAND = [Box(-6, -8, -2, 12, 8, 4, 'hide', 'palm'), Box(-6, -18, -1, 2, 10, 2, 'talon', 't0'), Box(-2.7, -21, -1, 2, 13, 2, 'talon', 't1'),
        Box(0.7, -21, -1, 2, 13, 2, 'talon', 't2'), Box(4, -18, -1, 2, 10, 2, 'talon', 't3'), Box(-9, -6, -1, 3, 2, 2, 'talon', 'thumb')]
for upper, lower, hand, at, (l1, l2) in LIMBS:
    M.part(f'{upper}_r', 'trunk', at, boxes=[Box(-3.5, -l1, -3.5, 7, l1, 7, 'hide', 'arm')])
    M.part(f'{lower}_r', f'{upper}_r', (0, -l1, 0), boxes=[Box(-2.5, -l2, -2.5, 5, l2, 5, 'hide', 'fore')])
    M.part(f'{hand}_r', f'{lower}_r', (0, -l2, 0), boxes=[Box(b.x, b.y, b.z, b.w, b.h, b.d, b.mat, b.tag) for b in HAND])
    for src, dst, under in ((f'{upper}_r', f'{upper}_l', 'trunk'), (f'{lower}_r', f'{lower}_l', f'{upper}_l'), (f'{hand}_r', f'{hand}_l', f'{lower}_l')):
        M.mirror(src, dst, parent=under)
# the tail: what is left of the weaving behind the knot, two necks of it, thinning, and coming apart at the end
TAIL_LIE = (-1.98, -2.42, -2.09, -1.62)
TAIL_R = (4.0, 3.2, 2.4, 1.8, 1.4)
TAIL_W = (5, 4, 3, 3)
parent, at, was = 'trunk', (0, 3, 12), 0.0
for i in range(TAIL):
    M.part(f'tail{i}', parent, at, (round(TAIL_LIE[i] - was, 3), 0, 0))
    for k in range(2):
        a0 = k * math.pi + i * 1.3
        a1 = a0 + 1.3
        foot = np.array([TAIL_R[i] * math.cos(a0), 0.0, TAIL_R[i] * math.sin(a0)])
        top = np.array([TAIL_R[i + 1] * math.cos(a1), -NECK_L, TAIL_R[i + 1] * math.sin(a1)])
        long = float(np.linalg.norm(top - foot)) + 1.5
        w = TAIL_W[i]
        M.part(f'ts{i}_{k}', f'tail{i}', tuple(round(float(v), 2) for v in foot), tuple(round(v, 4) for v in euler(frame_along(top - foot, (math.cos(a0), 0, math.sin(a0))))),
               boxes=[Box(-w / 2, -round(long), -w / 2, w, round(long), w, 's0' if k == 0 else 's2', 'strand')])
    parent, at, was = f'tail{i}', (0, -NECK_L, 0), TAIL_LIE[i]
for k, (rx, rz) in enumerate(((0.3, 0.5), (-0.2, -0.45), (0.5, -0.1))):
    M.part(f'tailend{k}', parent, at, (rx, 0, rz), boxes=[Box(-0.75, -10, -0.75, 1.5, 10, 1.5, 'limb', 'rib'), Box(-2, -15, -0.5, 4, 6, 1, 'claw', 'rib_claw')])
# the lesser necks: two more of the weaving that did not go into the rope, one either side of it, with no head but a fist of hands
for label, at, count, long in LESSER:
    parent = 'trunk'
    for i in range(count):
        w = 5 - i if i < 3 else 2
        curl = 0.0 if i == 0 else (0.28 if label.endswith('r') else -0.22) * (1 if i % 2 else -1) + 0.3
        M.part(f'{label}{i}', parent, at, (round(curl, 3), 0, 0), boxes=[Box(-w / 2, -long - 1, -w / 2, w, long + 1, w, 's1' if label.endswith('r') else 's2', 'strand')])
        parent, at = f'{label}{i}', (0, -long, 0)
    for k, (rx, rz) in enumerate(((0.5, 0.0), (-0.3, 0.6), (-0.3, -0.6))):
        M.part(f'{label}_end{k}', parent, at, (rx, 0, rz), boxes=[Box(-0.75, -7, -0.75, 1.5, 7, 1.5, 'limb', 'rib'), Box(-2.5, -14, -0.5, 5, 8, 1, 'claw', 'crown_claw')])
M.pack()

CORONA = (236, 226, 255)
FLAME = VIOLET                  # the ridge along its back
FLAME_L = VIOLET_L
# the parts a pose turns, in the order ComingModel keeps them
POSED = (('trunk',) + tuple(f'neck{i}' for i in range(NECK)) + ('head',) + tuple(f'{part}_{side}' for upper, lower, hand, _, _ in LIMBS for side in 'rl' for part in (upper, lower, hand))
         + ('lesser_r0', 'lesser_l0', 'maw_r', 'maw_l'))


def path(part):
    """The names from 'body' down to a part."""
    names = []
    while part != 'body':
        names.append(part)
        part = M.parts[part]['parent']
    return names[::-1]


# ---------------------------------------------------------------------------------------------------- the poses
def G(x, h, z):
    """A place in the model's space: x to its left, h over the floor, z back."""
    return np.array([x, 24.0 - h, z], float)


def turn_to(pose, part, R, ref):
    """Says in `pose` how `part` is turned so that its frame in the model's space is R."""
    pm, _ = M.transform(M.parts[part]['parent'], pose)
    rest = M.parts[part]['rot']
    near = tuple(a + b for a, b in zip(rest, ref[part])) if ref is not None and part in ref else None
    e = euler(pm.T @ R, near)
    pose[part] = tuple(round(a - b, 3) for a, b in zip(e, rest))
    pose['_R'][part] = R


def point(pose, part, d, ref):
    """Turns `part` (a box that runs up from its pivot) to run along `d`: with as little turn about its own length
    from how it is in `ref` as may be, so that it goes from the one to the other without rolling over."""
    if ref is not None and part in ref['_R']:
        was = ref['_R'][part]
        R = arc(-was[:, 1], d) @ was
    else:
        R = frame_along(d)
    turn_to(pose, part, R, ref)


def limb(pose, names, to, pole, fingers, ref, lift=0.0):
    """An arm of two lengths from where its shoulder is to `to` (a place over the floor), its elbow out the way of
    `pole`; the hand on it flat on the floor, fingers the way of `fingers` (or, off the floor, hanging from the wrist)."""
    upper, lower, hand = names
    l1, l2 = LENGTHS[upper[:-2]]
    _, s = M.transform(upper, pose)
    d = np.array(to, float) - s
    far = min(float(np.linalg.norm(d)), l1 + l2 - 0.5)
    dn = d / np.linalg.norm(d)
    a = (l1 * l1 - l2 * l2 + far * far) / (2 * far)
    h = math.sqrt(max(0.0, l1 * l1 - a * a))
    p = np.array(pole, float)
    p = p - float(np.dot(p, dn)) * dn
    p /= np.linalg.norm(p)
    elbow = s + a * dn + h * p
    point(pose, upper, elbow - s, ref)
    point(pose, lower, s + far * dn - elbow, ref)
    f = np.array(fingers, float)
    f /= np.linalg.norm(f)
    n = np.array([0.0, 1.0, 0.0])                                   # the palm down
    n = n - float(np.dot(n, f)) * f
    n /= np.linalg.norm(n)
    y = -f
    turn_to(pose, hand, np.column_stack([np.cross(y, n), y, n]), ref)


# where its hands are when it stands (x for the right one: the left is the same across), and which way the elbows go
FORE_AT = (-44, 2, -34)
MID_AT = (-56, 2, 4)
HIND_AT = (-40, 2, 42)
POLES = {'f': (-0.55, -1, -0.1), 'm': (-0.6, -1, 0.0), 'h': (-0.55, -1, 0.25)}
FINGERS = {'f': (-0.5, 0, -0.85), 'm': (-1, 0, -0.1), 'h': (-0.65, 0, 0.75)}
GRIP_AT = (-20, 2, -13)                    # masked: out of the crack and down, the hands at its foot
TUCK_MID = (-26, 30, 46)                   # and the others folded in the dark
TUCK_HIND = (-22, 26, 64)
NECK_UP = (-0.55, -0.15, 0.1, 0.35, 0.5, 0.6, 0.6, 0.5)     # out of the knot, back, up and over: it looks down from the top of it
NECK_OUT = (0.5, 0.25, 0.25, 0.35, 0.4, 0.35, 0.2, -0.1)    # out of the crack, up over the hall and down again: the mask hangs and looks into it
LIMB_NAMES = {key + side: tuple(f'{part}_{side}' for part in (upper, lower, hand)) for key, (upper, lower, hand, _, _) in zip('fmh', LIMBS) for side in 'rl'}
LESSER_UP = ((-0.5, 0, -0.45), (-0.35, 0, 0.5))             # the lesser necks: up and out to a side of the rope
LESSER_OUT = ((1.0, 0, -0.5), (0.8, 0, 0.55))               # masked: out of the crack either side of it


def across(v):
    return (-v[0], v[1], v[2])


def make_pose(ref, off=(0, 0, 0), trunk=(0, 0, 0), neck=NECK_UP, head=0.0, fore=FORE_AT, mid=MID_AT, hind=HIND_AT, fr=None, fl=None, mr=None, ml=None, hr=None, hl=None,
              poles=None, fingers=None, maw=0.0, sway=0.0, lesser=LESSER_UP):
    """A pose: where the body is, how the knot is tipped, how the rope bends (a turn forward for each length of it, and
    `sway` to a side along all of it), where each hand is (`fore`/`mid`/`hind` for both of a pair, or one by one)."""
    pose = {'_R': {}, 'body': {'off': tuple(off)}, 'trunk': tuple(trunk)}
    for i, a in enumerate(neck):
        pose[f'neck{i}'] = (round(a, 3), 0, round(sway / NECK, 3))
    pose['head'] = (round(head, 3), 0, 0)
    pose['lesser_r0'], pose['lesser_l0'] = lesser
    places = {'fr': fr or fore, 'fl': fl or across(fore), 'mr': mr or mid, 'ml': ml or across(mid), 'hr': hr or hind, 'hl': hl or across(hind)}
    poles = dict(POLES, **(poles or {}))
    fingers = dict(FINGERS, **(fingers or {}))
    for key, names in LIMB_NAMES.items():
        left = key.endswith('l')
        pole, way = poles[key[0]], fingers[key[0]]
        x, h, z = places[key]
        limb(pose, names, G(x, h, z), across(pole) if left else pole, across(way) if left else way, ref)
    pose['maw_r'] = (0, round(maw, 3), 0)
    pose['maw_l'] = (0, round(-maw, 3), 0)
    return pose


def masked(ref, **kw):
    kw.setdefault('off', IN_ROCK)
    kw.setdefault('neck', NECK_OUT)
    kw.setdefault('fore', GRIP_AT)
    kw.setdefault('mid', TUCK_MID)
    kw.setdefault('hind', TUCK_HIND)
    kw.setdefault('poles', {'f': (-0.7, -1, 0.0), 'm': (-0.4, -1, 0.0), 'h': (-0.4, -1, 0.0)})
    kw.setdefault('fingers', {'f': (-0.35, 0, -0.95)})
    kw.setdefault('lesser', LESSER_OUT)
    return make_pose(ref, **kw)


def build():
    P = {}
    P['masked'] = masked(None)
    m = P['masked']
    # the mask let down and thrust out at whoever it looks at: the rays come out of its slits
    P['gaze'] = masked(m, neck=(0.7, 0.3, 0.3, 0.3, 0.3, 0.2, 0.1, 0.2), head=0.15)
    # the rope thrown up against the top of the crack - and down, all its length along the floor, the mask lying at the end of it
    P['slam_back'] = masked(m, neck=(0.45, 0.05, 0.1, 0.2, 0.3, 0.4, 0.4, 0.3), head=-0.2)
    P['slam'] = masked(m, neck=(1.6, 0.35, 0.25, 0.1, -0.1, -0.2, -0.2, -0.15), head=-0.1)
    # an arm let go of the floor and brought round, low
    P['sweep_back'] = masked(m, fr=(-48, 9, 0), sway=0.5)
    P['sweep'] = masked(m, fr=(26, 6, -40), sway=-0.4)
    # both arms up, and down on whoever is at the foot of the crack
    P['crush_up'] = masked(m, fore=(-24, 74, -16), neck=(0.3, 0.2, 0.25, 0.35, 0.4, 0.35, 0.2, 0.0))
    P['crush'] = masked(m, fore=(-12, 2, -28), neck=(0.6, 0.25, 0.25, 0.35, 0.4, 0.35, 0.2, 0.0))
    # its hands in the floor of the hall: what it reaches with comes up elsewhere
    P['reach'] = masked(m, fore=(-18, -4, -16), neck=(0.6, 0.3, 0.3, 0.35, 0.4, 0.35, 0.2, -0.1), head=0.1)
    # the mask goes: it has both hands as far out into the hall as they go, and the rope up - and drags the knot out after
    P['emerge_grip'] = masked(m, fore=(-32, 2, -24), neck=(0.2, 0.0, 0.1, 0.2, 0.4, 0.5, 0.5, 0.4), lesser=LESSER_UP)
    P['bare'] = make_pose(m)
    b = P['bare']
    P['bare_gaze'] = make_pose(b, neck=(-0.3, 0.1, 0.3, 0.45, 0.5, 0.5, 0.45, 0.4), head=0.15, trunk=(0.08, 0, 0))
    P['bare_slam_back'] = make_pose(b, neck=(-0.9, -0.2, 0.0, 0.2, 0.4, 0.5, 0.5, 0.4), trunk=(-0.22, 0, 0), head=-0.2)
    P['bare_slam'] = make_pose(b, neck=(-0.1, 0.6, 0.95, 0.9, 0.5, -0.3, -0.55, -0.4), trunk=(0.18, 0, 0), head=-0.1)
    P['bare_sweep_back'] = make_pose(b, fr=(-58, 12, -4), sway=0.4)
    P['bare_sweep'] = make_pose(b, fr=(30, 6, -56), sway=-0.4, trunk=(0.05, 0.25, 0))
    P['bare_crush_up'] = make_pose(b, trunk=(-0.5, 0, 0), fore=(-26, 100, -26), neck=(-0.2, 0.0, 0.2, 0.35, 0.5, 0.6, 0.6, 0.5))
    P['bare_crush'] = make_pose(b, trunk=(0.15, 0, 0), fore=(-14, 2, -54), neck=(-0.45, 0.0, 0.2, 0.35, 0.5, 0.6, 0.6, 0.5))
    P['bare_reach'] = make_pose(b, off=(0, 5, 0), fore=(-34, -4, -30), neck=(-0.4, 0.0, 0.2, 0.4, 0.55, 0.6, 0.6, 0.5), head=0.1)
    # reared, the mouth in the knot open to the hall; and down on whatever came to it
    P['pull'] = make_pose(b, trunk=(-0.7, 0, 0), fore=(-48, 50, -20), neck=(0.2, 0.0, 0.1, 0.25, 0.4, 0.5, 0.5, 0.4), maw=1.35)
    P['bite'] = make_pose(b, off=(0, 8, -8), trunk=(0.35, 0, 0), fore=(-44, 2, -36), neck=(-1.0, -0.2, 0.1, 0.35, 0.5, 0.6, 0.6, 0.5), maw=0.2)
    # the sky called down: both arms up, the rope straight up, the disc turned to the roof, the crown wide
    P['rain'] = make_pose(b, trunk=(-0.3, 0, 0), fore=(-34, 104, -8), neck=(0.15, 0.0, 0.05, 0.05, 0.1, 0.1, 0.1, 0.1), head=-0.2)
    # the eyes in the rock look: it bows down out of the way of them, the crown shut over the disc
    P['glare'] = make_pose(b, off=(0, 10, 0), trunk=(0.25, 0, 0), neck=(0.2, 0.5, 0.5, 0.4, 0.2, 0.0, -0.2, -0.2), head=0.1, fore=(-44, 2, -22))
    # whoever is under it: it stands up off them on all six - and lets the knot down on them, and lies so a moment
    P['stamp_up'] = make_pose(b, off=(0, -9, 0), fore=(-38, 2, -30), mid=(-48, 2, 4), hind=(-34, 2, 38))
    P['stamp'] = make_pose(b, off=(0, 21, 0), fore=(-54, 2, -38), mid=(-66, 2, 6), hind=(-50, 2, 46), neck=(-0.2, -0.1, 0.15, 0.4, 0.55, 0.6, 0.6, 0.5), trunk=(0.06, 0, 0))
    # walking: three hands go together - a fore one, the middle one across from it, the hind one behind it. Set down fore
    # and aft; and one three in the air as it passes the other
    s, up = 11, 14
    fa, ma, ha = FORE_AT, MID_AT, HIND_AT

    def gait(a, b, lift_a=0.0, lift_b=0.0):
        return make_pose(P['bare'], fr=(fa[0], fa[1] + lift_a, fa[2] + a), ml=(-ma[0], ma[1] + lift_a, ma[2] + a), hr=(ha[0], ha[1] + lift_a, ha[2] + a),
                         fl=(-fa[0], fa[1] + lift_b, fa[2] + b), mr=(ma[0], ma[1] + lift_b, ma[2] + b), hl=(-ha[0], ha[1] + lift_b, ha[2] + b))
    P['walk0'] = gait(-s, s)
    P['walkb'] = gait(0, 0, lift_b=up)
    P['walk1'] = gait(s, -s)
    P['walka'] = gait(0, 0, lift_a=up)
    return P


POSES = build()
MASK_ON = ('masked', 'gaze', 'slam_back', 'slam', 'sweep_back', 'sweep', 'crush_up', 'crush', 'reach', 'emerge_grip')


def off_of(pose):
    return tuple(float(v) for v in pose.get('body', {}).get('off', (0, 0, 0)))


def disc_at(pose, turn=0.0):
    """Where the middle of the disc is in a pose, in blocks: (out before where it stands, up)."""
    _, t = M.transform('disc', pose)
    return round(float(-t[2] / BLOCK), 2), round(float((24.0 - t[1]) / BLOCK), 2)


def hand_at(pose, hand='hand_r'):
    _, t = M.transform(hand, pose)
    return round(float(-t[0] / BLOCK), 2), round(float(-t[2] / BLOCK), 2)


def write_poses(path_, indent='    '):
    """The poses as ComingModel has them: a row of three turns for every part of POSED and where the body is, between
    the markers."""
    lines = [f'{indent}private static final String[][] POSED = {{' + ', '.join('{' + ', '.join(f'"{n}"' for n in path(part)) + '}' for part in POSED) + '};']
    for label, pose in POSES.items():
        rows = []
        for part in POSED:
            v = pose.get(part, (0, 0, 0))
            rows.append('{' + ', '.join(f'{float(x):.3f}F' for x in v) + '}')
        oy, oz = off_of(pose)[1:]
        rows.append(f'{{0.000F, {oy:.1f}F, {oz:.1f}F}}')
        lines.append(f'{indent}private static final float[][] {label.upper()} = {{' + ', '.join(rows) + '};')
    src = open(path_, encoding='utf-8').read()
    a = src.index(indent + '// <generated poses>')
    b = src.index(indent + '// </generated poses>')
    open(path_, 'w', encoding='utf-8').write(src[:a] + indent + '// <generated poses>\n' + '\n'.join(lines) + '\n' + src[b:])


# what ComingEntity knows of the poses: where the disc is in those it strikes or looks from (blocks out before where it stands, and up)
KNOWN = (('masked', 'REST'), ('gaze', 'GAZE'), ('slam', 'SLAM'), ('bare', 'BARE'), ('bare_gaze', 'BARE_GAZE'), ('bare_slam', 'BARE_SLAM'), ('glare', 'BOWED'),
         ('rain', 'RAIN'), ('stamp', 'STAMP'))


def write_known(path_, indent='    '):
    lines = []
    for label, name in KNOWN:
        out, up = disc_at(POSES[label])
        lines.append(f'{indent}private static final double[] DISC_{name} = {{{out}, {up}}};')
    src = open(path_, encoding='utf-8').read()
    a = src.index(indent + '// <generated places>')
    b = src.index(indent + '// </generated places>')
    open(path_, 'w', encoding='utf-8').write(src[:a] + indent + '// <generated places>\n' + '\n'.join(lines) + '\n' + src[b:])


# ---------------------------------------------------------------------------------------------------- the texture
STRAND = {'s0': (1.05, VIOLET_D, VIOLET), 's1': (1.45, BLUE, BLUE_L), 's2': (0.0, None, None)}


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
            if b.tag == 'core':
                cracks(p, b, 'ccore', 2, colour=BLUE, bright=BLUE_L)
            elif b.tag in ('arm', 'fore'):
                cracks(p, b, 'cv', 1, colour=VIOLET_D, bright=VIOLET)
            if b.tag == 'nape':
                # eyes round where the necks end, small, and too many
                for face in ('left', 'right', 'top', 'bottom'):
                    fx, fy, fw, fh = b.faces()[face]
                    for x in range(1, fw - 1, 3):
                        p.put(b, face, x, 2 + (x // 3) % 2, STAR, glow=True)
                        p.put(b, face, x, 3 + (x // 3) % 2, NIGHT_D)
        elif b.mat in STRAND:
            light, seam, seam_l = STRAND[b.mat]
            if seam is None:
                # the third neck is not of the night: skin that has never been in any light, grey, with the night coming up it
                pale = skin('sp', PALE_D, PALE_DD)
                dark = night('sd', light=1.2)

                def strand(face, px, py, fw, fh, rnd, coil=b.tag == 'coil'):
                    t = blotch(f'sm{b.x}{b.y}', px, py, 4.0)
                    c = mix(pale(face, px, py, fw, fh, rnd), dark(face, px, py, fw, fh, rnd), 0.25 + 0.5 * t)
                    along = px if coil else py
                    if face not in ('top', 'bottom') and along % 4 == 3:
                        return shade(c, 0.72)                          # rings round it, like a windpipe
                    return c
                p.fill_box(b, strand)
            else:
                base = night(b.mat, light=light)

                def strand(face, px, py, fw, fh, rnd):
                    c = base(face, px, py, fw, fh, rnd)
                    if face not in ('top', 'bottom') and (py % 3 == 2) and ((px + (py // 3) * 2) % 4 != 0):
                        return shade(c, 1.3)
                    return c
                p.fill_box(b, strand)
                starry(p, b, b.mat, 0.01)
                # a seam of light along it, broken: what the rope is seen by in the dark
                rnd = __import__('random').Random(f'seam:{name}:{b.mat}')
                for face in (('front', 'back') if b.tag != 'coil' else ('front', 'top')):
                    fx, fy, fw, fh = b.faces()[face]
                    long, wide = (fh, fw) if b.tag != 'coil' else (fw, fh)
                    at = wide // 2
                    for i in range(long):
                        if rnd.random() < 0.22:
                            at = max(1, min(wide - 2, at + rnd.choice((-1, 1))))
                        if rnd.random() < 0.18:
                            continue
                        x, y = (at, i) if b.tag != 'coil' else (i, at)
                        p.px(fx + x, fy + y, seam_l if i % 4 == 0 else seam, glow=True)
        elif b.mat == 'ridge':
            def ridge(face, px, py, fw, fh, rnd):
                if face not in ('left', 'right'):
                    return None
                # along the top of the knot the plates stand up out of it
                reach = 1 + (2 if px % 4 in (1, 2) else 0) + (1 if px % 4 == 1 else 0)
                if fh - 1 - py >= reach:
                    return None
                return FLAME_L if py == fh - 1 else FLAME
            p.fill_box(b, ridge)
            for face in ('left', 'right'):
                fx, fy, fw, fh = b.faces()[face]
                for x in range(fw):
                    for y in range(fh):
                        c = p.tex.getpixel((fx + x, fy + y))
                        if c[3]:
                            p.glow.putpixel((fx + x, fy + y), c[:3] + (255,))
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
            for x, y in ((2, 2), (9, 3), (5, 5), (12, 7), (3, 9), (8, 10), (13, 4), (6, 1)):
                p.put(b, 'front', x, y, STAR, glow=True)
                p.put(b, 'front', x, y + 1, NIGHT_D)
        elif b.mat in ('pv_rock', 'pv_sky'):
            p.fill_box(b, lambda face, px, py, fw, fh, rnd, rock=b.mat == 'pv_rock': shade((62, 60, 70) if rock else (10, 10, 22), 0.85 + 0.3 * blotch('pv', px, py, 2.0)))
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


# ---------------------------------------------------------------------------------------------------- the eyes in the rock
# When it is out of the crack, eyes open in the rock: one in the crack it came out of, as tall as the crack, and lesser
# ones all round the walls of the hall. An eye is two plates: the white of it between its lids, and before that the
# ring and the slit, which WallEyeModel moves to whoever the eye looks at. Standing on end, both kinds.
EYE_GREAT = (38, 68)
EYE_LESSER = (14, 24)
E = Model(256, 128, 1.0)
for label, (ew, eh) in (('great', EYE_GREAT), ('lesser', EYE_LESSER)):
    iw, ih = int(ew * 0.31 * 2) + 4, int(ew * 0.31 * 2 / 0.55) + 4
    E.part(label, None, (0, 24, 0), boxes=[Box(-ew / 2, -eh / 2, -0.5, ew, eh, 1, 'white', label)])
    E.part(f'{label}_iris', label, (0, 0, -0.6), boxes=[Box(-iw / 2, -ih / 2, -0.5, iw, ih, 1, 'iris', label)])
E.pack()


def make_eye():
    p = Painter(E, 923)
    for name, _, b in E.boxes():
        ew, eh = EYE_GREAT if b.tag == 'great' else EYE_LESSER
        iris = ew * 0.31
        if b.mat == 'white':
            def white(face, px, py, fw, fh, rnd):
                if face != 'front':
                    return None
                # lids of the night, and between them a white that is not white
                cx, cy = px - (fw - 1) / 2, py - (fh - 1) / 2
                lid = 1.0 - (cy / (fh / 2)) ** 2
                if lid <= 0.0:
                    return None
                half = (fw / 2 - 0.5) * lid ** 0.8
                if abs(cx) > half:
                    return None
                if abs(cx) > half - (1.2 if fw > 20 else 0.8):
                    return NIGHT_D
                return mix(NIGHT, (58, 44, 96), blotch('white', px, py, 3.0))
            p.fill_box(b, white)
            fx, fy, fw, fh = b.faces()['front']
            for x in range(fw):
                for y in range(fh):
                    c = p.tex.getpixel((fx + x, fy + y))
                    if not c[3] or c[2] < 40:
                        continue
                    if blotch('vein', x, y, 1.6) > 0.7:
                        p.px(fx + x, fy + y, VIOLET_D, glow=True)       # veins in the white of it
                    else:
                        p.glow.putpixel((fx + x, fy + y), shade(c, 0.75) + (255,))
        else:
            def ring(face, px, py, fw, fh, rnd):
                if face != 'front':
                    return None
                cx, cy = px - (fw - 1) / 2, py - (fh - 1) / 2
                r = math.hypot(cx, cy * 0.55)
                if abs(cx) < (1.0 if ew > 20 else 0.6) + (2.0 if ew > 20 else 0.9) * max(0.0, 1.0 - (cy / (eh * 0.29)) ** 2) and abs(cy) < eh * 0.29:
                    return (2, 2, 6)                                  # the slit
                if r < iris:
                    spoke = blotch('iris', math.atan2(cy, cx) * 5.0, r, 0.9)
                    return mix(VIOLET_D, VIOLET_L, min(1.0, max(0.0, (r - iris * 0.25) / (iris * 0.75))) * (0.6 + 0.4 * spoke))
                if r < iris + (1.2 if ew > 20 else 0.8):
                    return CORONA
                return None
            p.fill_box(b, ring)
            fx, fy, fw, fh = b.faces()['front']
            for x in range(fw):
                for y in range(fh):
                    c = p.tex.getpixel((fx + x, fy + y))
                    if c[3] and (c[0] > 40 or c[2] > 60):
                        p.px(fx + x, fy + y, c, glow=True)
    return p.tex, p.glow


if __name__ == '__main__':
    if len(sys.argv) < 3:
        M.write_java(JAVA)
        write_poses(JAVA)
        write_known(ENTITY_JAVA)
        T.write_java(TENDRIL_JAVA)
        E.write_java(EYE_JAVA)
    tex, glow = make()
    tex.save(f'{A}/textures/entity/coming.png')
    glow.save(f'{A}/textures/entity/coming_glow.png')
    ttex, tglow = make_tendril()
    ttex.save(f'{A}/textures/entity/tendril.png')
    tglow.save(f'{A}/textures/entity/tendril_glow.png')
    etex, eglow = make_eye()
    etex.save(f'{A}/textures/entity/wall_eye.png')
    eglow.save(f'{A}/textures/entity/wall_eye_glow.png')
    if len(sys.argv) > 1 and sys.argv[1] == '-':
        # where the middle of the disc is in every pose (ComingEntity is given those it needs: write_known), and its right hand
        for label, pose in POSES.items():
            print(f'{label:16} disc (out, up): {disc_at(pose)}   right hand (to its right, out): {hand_at(pose)}')
    elif len(sys.argv) > 1:
        only = sys.argv[3].split(',') if len(sys.argv) > 3 else None
        cam = dict(size=(430, 430), zoom=1.15, centre=(0, -100), glow=glow, background=(40, 40, 50))
        shots, labels = [], []
        for label, pose in POSES.items():
            if only and label not in only:
                continue
            hidden = ('halo',) if label in MASK_ON else ('mask', 'roots', 'pv_wall')
            for yaw in ((-0.55, 0.0, -1.45) if label in ('masked', 'bare') or only else (-0.55, -1.45)):
                shots.append(M.render(tex, yaw=yaw, pitch=0.12, pose=pose, hidden=hidden, **cam))
                labels.append(label)
        if not only:
            tcam = dict(size=(430, 430), zoom=3.2, centre=(0, -30), glow=tglow, background=(40, 40, 50))
            for label, pose in TENDRIL_POSES:
                shots.append(T.render(ttex, yaw=-1.2, pitch=0.2, pose=pose, **tcam))
                labels.append('tendril ' + label)
            ecam = dict(size=(430, 430), zoom=5.0, centre=(0, 24), glow=eglow, background=(40, 40, 50))
            shots.append(E.render(etex, yaw=-0.3, pitch=0.0, pose={'lesser': {'off': (40, 0, 0)}, 'great_iris': {'off': (3, -4, 0)}}, **ecam))
            labels.append('eyes')
        sheet(shots, 6, labels=labels).save(sys.argv[1])
