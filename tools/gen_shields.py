#!/usr/bin/env python3
"""The four shields: box models with painted textures, held upright and raised when blocking.
Run from the repo root; pass a file name for a preview."""
import json
import math
import os
import sys

import numpy as np

from jsonmodel import JsonModel
from modelkit import blotch, mix, shade, sheet

A = 'src/main/resources/assets/unfading_dawn'
MOD = 'unfading_dawn'

WOOD = (132, 94, 58)
WOOD_D = (98, 68, 42)
WOOD_L = (160, 118, 74)
IRON = (104, 104, 112)
IRON_D = (70, 70, 78)
IRON_L = (150, 150, 158)
RUST = (130, 78, 48)
RED = (124, 46, 42)
RED_D = (88, 30, 30)
GOLD = (204, 184, 128)
LEATHER = (92, 62, 40)

# ---------------------------------------------------------------- how the shield sits in the hand
#
# The game draws a held item like this (entity model space: 16 units per block, y down, the front of the body is -z,
# the right arm is at -x):
#     arm pose . rotate x -90 . rotate y 180 . move to the hand . [display: move, rotate x-y-z, scale] . model centred
# For the left hand the display's x move and its y and z rotations are negated, which mirrors a symmetrical model.
# Below, the wanted result is written down in body terms (which way is the shield's top, which way its face) and the
# display entry is worked out from that, for an arm hanging down and for the arm raised the way the game raises it to block.

def RX(deg):
    a = math.radians(deg)
    return np.array([[1, 0, 0], [0, math.cos(a), -math.sin(a)], [0, math.sin(a), math.cos(a)]])


def RY(deg):
    a = math.radians(deg)
    return np.array([[math.cos(a), 0, math.sin(a)], [0, 1, 0], [-math.sin(a), 0, math.cos(a)]])


def RZ(deg):
    a = math.radians(deg)
    return np.array([[math.cos(a), -math.sin(a), 0], [math.sin(a), math.cos(a), 0], [0, 0, 1]])


def euler_xyz(r):
    """Angles (degrees) a, b, c with RX(a) RY(b) RZ(c) = r, the order the display entry uses."""
    b = math.asin(max(-1.0, min(1.0, r[0][2])))
    a = math.atan2(-r[1][2], r[2][2])
    c = math.atan2(-r[0][1], r[0][0])
    return [round(math.degrees(v), 2) for v in (a, b, c)]


def frame(up, face):
    """Rotation that takes the model's +y to `up` and its decorated side (+z) to `face`."""
    up = np.array(up, float) / np.linalg.norm(up)
    face = np.array(face, float)
    face = face - up * face.dot(up)
    face /= np.linalg.norm(face)
    return np.column_stack([np.cross(up, face), up, face])


HAND = RX(-90) @ RY(180)                  # from the item's space to the arm's
ARM_DOWN = np.eye(3)
ARM_BLOCK = RY(-30) @ RX(-54)             # the right arm when blocking: raised forward and turned inward
GRIP = 1.15                               # the grip is this far behind the middle of the plate

UP = (0, -1, 0)
FORWARD = np.array((0, 0, -1.0))
OUTWARD = np.array((-1.0, 0, 0))          # for the right arm


def turned(deg):
    """Facing forward, turned outward (away from the body) by so many degrees."""
    a = math.radians(deg)
    return FORWARD * math.cos(a) + OUTWARD * math.sin(a)


def third_person(arm, face, shift, scale=1.0):
    """Display entry for the right hand: top up, face towards `face`, grip in the hand, then moved by `shift` (body space)."""
    r = HAND.T @ arm.T @ frame(UP, face)
    offset = GRIP * np.array(face) + np.array(shift, float)
    t = HAND.T @ arm.T @ offset
    return {'rotation': euler_xyz(r), 'translation': [round(float(v), 2) for v in t], 'scale': [scale] * 3}


def first_person(turn, centre, scale):
    """The right hand in first person: the item starts at (0.56, -0.52, -0.72) from the eye (x right, y up, -z ahead);
    `centre` is where the middle of the shield should be instead. The back of the shield faces the eye."""
    t = (np.array(centre) - np.array((0.56, -0.52, -0.72))) * 16
    return {'rotation': [0, 180 - turn, 0], 'translation': [round(float(v), 2) for v in t], 'scale': [scale] * 3}


def displays(blocking):
    if blocking:
        third = third_person(ARM_BLOCK, turned(8), (0, -2, 0))
        first = first_person(4, (0.30, -0.40, -0.85), 0.85)
    else:
        third = third_person(ARM_DOWN, turned(28), (-2.0, -1.0, -1.0))
        first = first_person(12, (0.78, -0.58, -0.92), 0.75)
    return {
        'thirdperson_righthand': third, 'thirdperson_lefthand': third,
        'firstperson_righthand': first, 'firstperson_lefthand': first,
        'gui': {'rotation': [0, 205, 0], 'translation': [0, 0, 0], 'scale': [0.9, 0.9, 0.9]},
        'ground': {'rotation': [0, 0, 0], 'translation': [0, 3, 0], 'scale': [0.5, 0.5, 0.5]},
        'fixed': {'rotation': [0, 180, 0], 'translation': [0, 0, 0], 'scale': [1.0, 1.0, 1.0]},
    }


DISPLAY = displays(False)
BLOCKING = displays(True)


# ---------------------------------------------------------------- a picture of a figure holding the shield

def held_preview(model, blocking, left, view_yaw, size=(220, 300), zoom=7.0):
    """A plain figure with the shield in the hand, placed by the same arithmetic as the game's (see above)."""
    from PIL import Image, ImageDraw
    entry = (BLOCKING if blocking else DISPLAY)['thirdperson_righthand']
    k = -1 if left else 1
    rot = entry['rotation']
    r = RX(rot[0]) @ RY(k * rot[1]) @ RZ(k * rot[2])
    t = np.array(entry['translation'], float) * (k, 1, 1)
    arm = (ARM_BLOCK if blocking else ARM_DOWN)
    if left:
        mirror = np.diag([-1.0, 1.0, 1.0])
        arm = mirror @ arm @ mirror
    pivot = np.array((5.0 * (1 if left else -1), 2.0, 0.0))
    hand = np.array((k * 1.0, 2.0, -10.0))          # (k/16, 0.125, -0.625) blocks

    quads = []

    def box(x0, y0, z0, x1, y1, z1, colour, place):
        c = [(x, y, z) for x in (x0, x1) for y in (y0, y1) for z in (z0, z1)]
        for idx in ((0, 1, 3, 2), (4, 5, 7, 6), (0, 1, 5, 4), (2, 3, 7, 6), (0, 2, 6, 4), (1, 3, 7, 5)):
            quads.append(([place(np.array(c[i], float)) for i in idx], colour))

    body = lambda p: p
    box(-4, -8, -4, 4, 0, 4, (200, 170, 140), body)
    box(-4, 0, -2, 4, 12, 2, (90, 110, 150), body)
    box(-4, 12, -2, 0, 24, 2, (70, 70, 100), body)
    box(0, 12, -2, 4, 24, 2, (60, 60, 90), body)
    for side in (-1, 1):
        a = arm if (side == 1) == left else (ARM_DOWN if not blocking else RX(-10))
        pv = np.array((5.0 * side, 2.0, 0.0))
        x0, x1 = (-3, 1) if side == -1 else (-1, 3)
        box(x0, -2, -2, x1, 10, 2, (170, 140, 120), lambda p, a=a, pv=pv: pv + a @ p)
    for e in model.elements:
        colour = {'wood': WOOD, 'iron': IRON, 'leather': LEATHER, 'face': RED, 'plate': (84, 78, 78), 'rib': IRON_L}.get(e.mat, IRON)
        box(*e.frm, *e.to, colour, lambda p: pivot + arm @ HAND @ (hand + t + r @ (p - 8.0)))

    view = RY(view_yaw)
    img = Image.new('RGB', size, (139, 139, 139))
    draw = ImageDraw.Draw(img)
    light = np.array((0.3, -0.8, -0.5))
    light /= np.linalg.norm(light)
    out = []
    for pts, colour in quads:
        cam = [view @ p for p in pts]
        n = np.cross(cam[1] - cam[0], cam[3] - cam[0])
        n = n / (np.linalg.norm(n) + 1e-9)
        shade_k = 0.55 + 0.45 * abs(n.dot(view @ light))
        depth = sum(c[2] for c in cam) / 4
        out.append((depth, [(size[0] / 2 + c[0] * zoom, size[1] * 0.42 + c[1] * zoom) for c in cam], tuple(int(v * shade_k) for v in colour)))
    # the viewer stands in front of the figure (at -z), so the farthest faces have the largest z
    for depth, poly, colour in sorted(out, key=lambda q: -q[0]):
        draw.polygon(poly, fill=colour, outline=(30, 30, 30))
    return img


def write_json(path, data):
    os.makedirs(os.path.dirname(path), exist_ok=True)
    with open(path, 'w') as f:
        json.dump(data, f, indent=2)
        f.write('\n')


def iron(e, d, px, py, w, h, rnd):
    c = RUST if blotch(f'{e.tag}{d}', px, py, 2.2) > 0.7 else IRON
    if d in ('south', 'north') and min(w, h) > 2 and (px in (0, w - 1) or py in (0, h - 1)):
        c = IRON_D
    return c


def planks(e, d, px, py, w, h, rnd, base=WOOD, dark=WOOD_D, light=WOOD_L):
    if d in ('south', 'north'):
        c = dark if px % 3 == 2 else (light if (px * 7 + py * 3) % 11 == 0 else base)
        return shade(c, 1.06 - 0.14 * py / max(1, h - 1))
    return dark


def plank_shield():
    m = JsonModel(64, 'plank_shield')
    m.add(3, 1, 7.5, 13, 15, 8.5, 'wood', 'plate')
    m.add(2.5, 3, 7.2, 13.5, 5, 8.8, 'iron', 'band_a')
    m.add(2.5, 11, 7.2, 13.5, 13, 8.8, 'iron', 'band_b')
    m.add(7, 6, 6.2, 9, 10, 7.5, 'leather', 'grip')
    m.pack()

    def paint(e, d, px, py, w, h, rnd):
        if e.mat == 'wood':
            c = planks(e, d, px, py, w, h, rnd)
            # a split plank and a few arrow scars on the front
            if d == 'south' and (px, py) in ((2, 0), (2, 1), (7, 13), (5, 6), (6, 7), (8, 3)):
                c = shade(WOOD_D, 0.7)
            return c
        if e.mat == 'iron':
            c = iron(e, d, px, py, w, h, rnd)
            if d == 'south' and px in (1, 5, 9) and py == 0:
                c = IRON_L
            return c
        return LEATHER
    m.paint(paint, 3)
    return m


def round_shield():
    m = JsonModel(64, 'round_shield')
    # an octagon out of three overlapping slabs, an iron boss in the middle and a rim
    m.add(5, 1, 7.5, 11, 15, 8.5, 'wood', 'tall')
    m.add(3, 3, 7.5, 13, 13, 8.5, 'wood', 'mid')
    m.add(1, 5, 7.5, 15, 11, 8.5, 'wood', 'wide')
    m.add(6, 6, 8.5, 10, 10, 9.7, 'iron', 'boss')
    m.add(7, 6, 6.2, 9, 10, 7.5, 'leather', 'grip')
    m.pack()

    def paint(e, d, px, py, w, h, rnd):
        if e.mat == 'wood':
            if d not in ('south', 'north'):
                return IRON_D
            x0, y0 = e.frm[0], e.frm[1]
            # position on the whole shield, so that the three slabs carry one picture
            gx = (x0 + px) if d == 'south' else (e.to[0] - 1 - px)
            gy = 15 - (e.to[1] - 16 + 16 - 1 - py) if False else (16 - e.to[1]) + py
            dist = math.hypot(gx - 7.5, gy - 7.5)
            if dist > 6.2:
                return IRON if (gx + gy) % 3 else IRON_D
            quarter = (gx < 8) != (gy < 8)
            c = RED if quarter else WOOD
            if abs(gx - 7.5) < 1 or abs(gy - 7.5) < 1:
                c = shade(c, 0.8)
            return shade(c, 1.05 - 0.12 * gy / 15)
        if e.mat == 'iron':
            c = IRON_L if d == 'south' and 1 <= px <= 2 and 1 <= py <= 2 else IRON
            return c
        return LEATHER
    m.paint(paint, 4)
    return m


def guard_shield():
    m = JsonModel(64, 'guard_shield')
    # a heater shield: straight on top, narrowing to a point
    m.add(2, 6, 7.5, 14, 15, 8.5, 'face', 'top')
    m.add(3, 4, 7.5, 13, 6, 8.5, 'face', 'a')
    m.add(4, 2, 7.5, 12, 4, 8.5, 'face', 'b')
    m.add(6, 0.5, 7.5, 10, 2, 8.5, 'face', 'c')
    m.add(7, 7, 6.2, 9, 12, 7.5, 'leather', 'grip')
    m.pack()
    key = [".KK.", "K..K", "K..K", ".KK.", ".K..", ".KK.", ".K..", ".KK."]

    def paint(e, d, px, py, w, h, rnd):
        if e.mat == 'face':
            if d not in ('south', 'north'):
                return IRON_D
            gx = (e.frm[0] + px) if d == 'south' else (e.to[0] - 1 - px)
            gy = (16 - e.to[1]) + py
            rim = px in (0, w - 1) or (e.tag == 'top' and py == 0) or (e.tag == 'c' and py == h - 1)
            if rim:
                return IRON if (gx + gy) % 4 else IRON_L
            if d == 'north':
                return shade(WOOD_D, 1.0 if gx % 3 else 0.85)
            c = RED if blotch('tabard', gx, gy, 3.0) < 0.75 else RED_D
            kx, ky = gx - 6, gy - 3
            if 0 <= ky < len(key) and 0 <= kx < 4 and key[ky][kx] == 'K':
                c = GOLD
            return shade(c, 1.08 - 0.16 * gy / 15)
        return LEATHER
    m.paint(paint, 5)
    return m


def tower_shield():
    m = JsonModel(64, 'tower_shield')
    # a slab of iron almost as tall as a man, with a rib down the middle and a slot to look through
    m.add(2.5, -4, 7.3, 13.5, 19, 8.7, 'plate', 'plate')
    m.add(7, -3, 8.7, 9, 18, 9.4, 'rib', 'rib')
    m.add(2.5, 19, 7.3, 13.5, 20, 8.7, 'rib', 'cap')
    m.add(7, 5, 6.0, 9, 11, 7.3, 'leather', 'grip')
    m.pack()

    def paint(e, d, px, py, w, h, rnd):
        if e.mat == 'plate':
            amount = blotch(f'tower{d}', px, py, 3.0)
            c = RUST if amount > 0.64 else ((84, 78, 78) if amount > 0.4 else (66, 62, 64))
            if d in ('south', 'north'):
                if px in (0, w - 1) or py in (0, h - 1):
                    c = IRON_D
                elif d == 'south' and py in (4, 5) and 2 <= px <= 8 and px not in (4, 5, 6):
                    c = (18, 14, 14)          # the slot, either side of the rib
                elif d == 'south' and px in (2, 8) and py % 5 == 2:
                    c = IRON_L                # rivets
                c = shade(c, 1.1 - 0.22 * py / h)
            return c
        if e.mat == 'rib':
            return IRON_L if (d == 'south' and px == 0) else IRON
        return LEATHER
    m.paint(paint, 6)
    return m


DUST = (204, 198, 184)
BONE, BONE_D, BONE_L = (198, 192, 166), (140, 134, 112), (230, 226, 204)
HIDE, HIDE_D = (72, 80, 62), (48, 56, 44)
WITHE = (118, 128, 72)
SPRUCE, SPRUCE_D, SPRUCE_L = (112, 80, 52), (80, 56, 38), (150, 112, 74)


def whole(e, d, px, py):
    """Where a pixel of a slab's front or back is on the whole shield, so that several slabs carry one picture."""
    gx = (e.frm[0] + px) if d == 'south' else (e.to[0] - 1 - px)
    return gx, (16 - e.to[1]) + py


def carver_buckler():
    m = JsonModel(64, 'carver_buckler')
    # a carver's buckler: a plate of iron no bigger than a dish, a boss over the fist, the pit's dust in every dent
    m.add(6, 4, 7.6, 10, 12, 8.4, 'plate', 'tall')
    m.add(5, 5, 7.6, 11, 11, 8.4, 'plate', 'mid')
    m.add(4, 6, 7.6, 12, 10, 8.4, 'plate', 'wide')
    m.add(7, 7, 8.4, 9, 9, 9.5, 'boss', 'boss')
    m.add(7.4, 6, 6.4, 8.6, 10, 7.6, 'leather', 'grip')
    m.pack()

    def paint(e, d, px, py, w, h, rnd):
        if e.mat == 'plate':
            if d not in ('south', 'north'):
                return IRON_D
            gx, gy = whole(e, d, px, py)
            dist = math.hypot(gx - 7.5, gy - 7.5)
            if d == 'north':
                return shade(IRON_D, 1.0 if (gx + gy) % 2 else 0.88)
            if dist > 3.4:
                return IRON_L if (gx + gy) % 3 == 0 else IRON
            c = IRON if blotch('buckler', gx, gy, 2.0) < 0.6 else IRON_D
            if blotch('bdust', gx, gy, 1.3) < 0.22:
                c = mix(c, DUST, 0.6)
            return shade(c, 1.08 - 0.14 * gy / 15)
        if e.mat == 'boss':
            return IRON_L if d == 'south' and (px, py) in ((0, 0), (1, 0), (0, 1)) else (IRON if d == 'south' else IRON_D)
        return LEATHER
    m.paint(paint, 7)
    return m


def scale_shield():
    m = JsonModel(64, 'scale_shield')
    # the Mire's: an oval frame of withe, the pike's hide stretched on it and its scales sewn on in rows
    m.add(5, 0, 7.5, 11, 16, 8.5, 'face', 'tall')
    m.add(4, 2, 7.5, 12, 14, 8.5, 'face', 'mid')
    m.add(3, 4, 7.5, 13, 12, 8.5, 'face', 'wide')
    m.add(7, 6, 6.2, 9, 10, 7.5, 'leather', 'grip')
    m.pack()

    def paint(e, d, px, py, w, h, rnd):
        if e.mat == 'face':
            if d not in ('south', 'north'):
                return shade(WITHE, 0.8)
            gx, gy = whole(e, d, px, py)
            edge = ((gx - 7.5) / 5.0) ** 2 + ((gy - 7.5) / 8.0) ** 2 > 0.72
            if d == 'north':
                return shade(WITHE, 0.9) if edge else (HIDE if blotch('sback', gx, gy, 2.2) < 0.65 else HIDE_D)
            if edge:
                return shade(WITHE, 1.0 if (gx + gy) % 2 else 0.78)
            row = gy // 2
            c = BONE_L if gy % 2 == 0 else (BONE if (gx + row) % 2 == 0 else BONE_D)
            return shade(c, 1.06 - 0.16 * gy / 15)
        return LEATHER
    m.paint(paint, 8)
    return m


def waker_board():
    m = JsonModel(64, 'waker_board')
    # a waker's board: a plank as long as an arm and a half, bound with iron at both ends, beaten pale in the middle
    m.add(5.5, -3, 7.4, 10.5, 19, 8.6, 'wood', 'plank')
    m.add(5.2, -2, 7.2, 10.8, -0.8, 8.8, 'iron', 'band_a')
    m.add(5.2, 16.8, 7.2, 10.8, 18, 8.8, 'iron', 'band_b')
    m.add(7, 5, 6.2, 9, 11, 7.4, 'leather', 'grip')
    m.pack()

    def paint(e, d, px, py, w, h, rnd):
        if e.mat == 'wood':
            if d not in ('south', 'north'):
                return SPRUCE_D
            c = SPRUCE_D if px == 2 else (SPRUCE_L if (px * 5 + py * 3) % 13 == 0 else SPRUCE)
            if d == 'south' and 8 <= py <= 14 and 1 <= px <= 3 and blotch('beaten', px, py, 1.4) < 0.7:
                c = mix(SPRUCE_L, (214, 190, 150), 0.5)      # where the mallet lands
            return shade(c, 1.06 - 0.14 * py / max(1, h - 1))
        if e.mat == 'iron':
            return iron(e, d, px, py, w, h, rnd)
        return LEATHER
    m.paint(paint, 9)
    return m


if __name__ == '__main__':
    shots = []
    held = []
    for name, build in (('plank_shield', plank_shield), ('round_shield', round_shield), ('guard_shield', guard_shield), ('tower_shield', tower_shield),
                        ('carver_buckler', carver_buckler), ('scale_shield', scale_shield), ('waker_board', waker_board)):
        m = build()
        m.display = DISPLAY
        m.gui_light = 'front'
        m.save(A, f'item/{name}', f'item/{name}')
        # the same boxes with another position in the hand while blocking
        write_json(f'{A}/models/item/{name}_blocking.json', {'parent': f'{MOD}:item/{name}', 'display': BLOCKING})
        write_json(f'{A}/items/{name}.json', {'model': {
            'type': 'minecraft:condition', 'property': 'minecraft:using_item',
            'on_true': {'type': 'minecraft:model', 'model': f'{MOD}:item/{name}_blocking'},
            'on_false': {'type': 'minecraft:model', 'model': f'{MOD}:item/{name}'}}})
        shots.append(m.render(size=(220, 300), zoom=11, yaw=math.radians(180 + 25), pitch=0.1))
        shots.append(m.render(size=(220, 300), zoom=11, yaw=math.radians(-30), pitch=0.1))
        held.extend([held_preview(m, False, True, 0), held_preview(m, False, True, 55), held_preview(m, True, True, 0), held_preview(m, True, True, -60)])
    if len(sys.argv) > 1:
        sheet(shots, 8).save(sys.argv[1])
        sheet(held, 8).save(sys.argv[1].replace('.png', '_held.png'))
