#!/usr/bin/env python3
"""The four shields: box models with painted textures, held upright and raised when blocking.
Run from the repo root; pass a file name for a preview."""
import json
import math
import os
import sys

from jsonmodel import JsonModel
from modelkit import blotch, mix, shade, sheet

A = 'src/main/resources/assets/vsratie_dushi'
MOD = 'vsratie_dushi'

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

# How the shield sits in the hand. The decorated side of the model looks south; these turn it to face away from the body.
DISPLAY = {
    'thirdperson_righthand': {'rotation': [0, 90, 0], 'translation': [3, 1, -1], 'scale': [1.0, 1.0, 1.0]},
    'thirdperson_lefthand': {'rotation': [0, 90, 0], 'translation': [3, 1, -1], 'scale': [1.0, 1.0, 1.0]},
    'firstperson_righthand': {'rotation': [0, 100, 0], 'translation': [3, 0, -2], 'scale': [1.0, 1.0, 1.0]},
    'firstperson_lefthand': {'rotation': [0, 100, 0], 'translation': [3, 0, -2], 'scale': [1.0, 1.0, 1.0]},
    'gui': {'rotation': [0, 205, 0], 'translation': [0, 0, 0], 'scale': [0.9, 0.9, 0.9]},
    'ground': {'rotation': [0, 0, 0], 'translation': [0, 3, 0], 'scale': [0.5, 0.5, 0.5]},
    'fixed': {'rotation': [0, 180, 0], 'translation': [0, 0, 0], 'scale': [1.0, 1.0, 1.0]},
}
BLOCKING = dict(DISPLAY)
BLOCKING.update({
    'thirdperson_righthand': {'rotation': [0, 60, 0], 'translation': [2, 3, 2], 'scale': [1.0, 1.0, 1.0]},
    'thirdperson_lefthand': {'rotation': [0, 60, 0], 'translation': [2, 3, 2], 'scale': [1.0, 1.0, 1.0]},
    'firstperson_righthand': {'rotation': [0, 150, 0], 'translation': [-4, 2, -3], 'scale': [1.1, 1.1, 1.1]},
    'firstperson_lefthand': {'rotation': [0, 150, 0], 'translation': [-4, 2, -3], 'scale': [1.1, 1.1, 1.1]},
})


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


if __name__ == '__main__':
    shots = []
    for name, build in (('plank_shield', plank_shield), ('round_shield', round_shield), ('guard_shield', guard_shield), ('tower_shield', tower_shield)):
        m = build()
        m.display = DISPLAY
        m.save(A, f'item/{name}', f'item/{name}')
        # the same boxes with another position in the hand while blocking
        write_json(f'{A}/models/item/{name}_blocking.json', {'parent': f'{MOD}:item/{name}', 'display': BLOCKING})
        write_json(f'{A}/items/{name}.json', {'model': {
            'type': 'minecraft:condition', 'property': 'minecraft:using_item',
            'on_true': {'type': 'minecraft:model', 'model': f'{MOD}:item/{name}_blocking'},
            'on_false': {'type': 'minecraft:model', 'model': f'{MOD}:item/{name}'}}})
        shots.append(m.render(size=(220, 300), zoom=11, yaw=math.radians(180 + 25), pitch=0.1))
        shots.append(m.render(size=(220, 300), zoom=11, yaw=math.radians(-30), pitch=0.1))
    if len(sys.argv) > 1:
        sheet(shots, 8).save(sys.argv[1])
