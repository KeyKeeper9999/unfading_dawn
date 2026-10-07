#!/usr/bin/env python3
"""The Gravedigger of the borough: mesh for GravediggerModel.java, his texture and its glow layer.
Run from the repo root; pass a file name to also get a preview sheet."""
import random
import sys

from modelkit import Box, Model, Painter, blotch, mix, shade, sheet

A = 'src/main/resources/assets/unfading_dawn'
JAVA = 'src/main/java/com/keykeeper/dushi/client/GravediggerModel.java'
SCALE = 1.3           # must match MODEL_SCALE in GravediggerModel.java

M = Model(128, 128, SCALE)
# gaunt and long: narrow hips, a long coat, a bent back with a coffin strapped to it, a wide hat over a sack hood
M.part('body', None, (0, 7, 0), boxes=[Box(-5.5, -4, -3.5, 11, 6, 7, 'coat', 'hips')])
M.part('skirt_front', 'body', (0, 2, -3.5), (-0.05, 0, 0), boxes=[Box(-5.5, 0, -0.5, 11, 12, 1, 'coat', 'skirt_f')])
M.part('skirt_back', 'body', (0, 2, 3.5), (0.08, 0, 0), boxes=[Box(-5.5, 0, -0.5, 11, 13, 1, 'coat', 'skirt_b')])
M.part('lantern', 'body', (6.5, -1, -1), boxes=[Box(-0.5, 0, -0.5, 1, 3, 1, 'iron', 'hook'), Box(-2, 3, -2, 4, 6, 4, 'lantern', 'lantern')])
M.part('chest', 'body', (0, -4, 0), (0.32, 0, 0), boxes=[Box(-6.5, -13, -4, 13, 13, 8, 'coat', 'torso'), Box(-5, -15, -1, 10, 2, 6, 'coat', 'hump')])
M.part('coffin', 'chest', (0, -13, 4.5), (-0.12, 0, 0), boxes=[Box(-4.5, -8, 0, 9, 24, 4, 'wood', 'coffin'), Box(-5.5, -2, 0, 11, 8, 4, 'wood', 'coffin_shoulder')])
M.part('head', 'chest', (0, -13, -2), (-0.2, 0, 0), boxes=[Box(-3.5, -7, -3.5, 7, 7, 7, 'sack', 'head')])
M.part('hat', 'head', (0, -5.5, 0), (-0.06, 0, 0), boxes=[Box(-7.5, -1, -7.5, 15, 1, 15, 'felt', 'brim'), Box(-4, -5, -4, 8, 4, 8, 'felt', 'crown')])
M.part('right_arm', 'chest', (-6.5, -11, 0), boxes=[Box(-5, -2, -2.5, 5, 11, 5, 'coat', 'sleeve')])
M.part('right_forearm', 'right_arm', (-2.5, 9, 0), boxes=[Box(-2, 0, -2, 4, 11, 4, 'skin', 'forearm'), Box(-2.5, 10, -2.5, 5, 4, 5, 'skin', 'hand')])
M.part('spade', 'right_forearm', (0, 12.5, 0), (1.5, 0, 0), boxes=[
    Box(-1, -24, -1, 2, 32, 2, 'wood', 'shaft'), Box(-3, 8, -1, 6, 2, 2, 'wood', 'grip'),
    Box(-4.5, -35, -0.5, 9, 11, 1, 'steel', 'blade'), Box(-1.5, -26, -1.5, 3, 3, 3, 'steel', 'socket')])
M.mirror('right_arm', 'left_arm')
M.mirror('right_forearm', 'left_forearm')
M.part('right_leg', None, (-3, 9, 0), boxes=[Box(-2.5, 0, -2.5, 5, 8, 5, 'cloth', 'thigh')])
M.part('right_shin', 'right_leg', (0, 8, 0), boxes=[Box(-2, 0, -2, 4, 7, 4, 'cloth', 'shin'), Box(-2.5, 4, -4.5, 5, 3, 7, 'boot', 'boot')])
M.mirror('right_leg', 'left_leg')
M.mirror('right_shin', 'left_shin')
M.pack()

COAT = (58, 54, 50)
COAT_D = (40, 37, 35)
COAT_L = (78, 72, 64)
MUD = (86, 70, 50)
SACK = (148, 132, 100)
SACK_D = (110, 96, 72)
SACK_DD = (78, 66, 50)
FELT = (40, 36, 38)
FELT_D = (26, 23, 26)
SKIN = (128, 118, 104)
SKIN_D = (96, 88, 78)
SKIN_DD = (68, 60, 54)
WOOD = (92, 70, 48)
WOOD_D = (64, 48, 34)
WOOD_L = (118, 92, 62)
STEEL = (120, 122, 128)
STEEL_D = (80, 82, 90)
STEEL_L = (166, 168, 172)
RUST = (126, 76, 48)
IRON = (74, 74, 82)
BOOT = (38, 32, 30)
GLASS = (150, 220, 150)
GLASS_L = (214, 255, 200)
ROPE = (120, 98, 60)
EYE = (190, 255, 170)


def coat(face, px, py, fw, fh, rnd):
    f = {'top': 1.1, 'bottom': 0.7}.get(face, 1.06 - 0.2 * py / max(1, fh - 1))
    amount = blotch(f'coat{fw}{fh}{face}', px, py, 3.4)
    c = COAT_D if amount > 0.66 else (COAT_L if amount < 0.2 else COAT)
    # mud climbs up the hem of anything long
    if fh >= 11 and face not in ('top', 'bottom') and py >= fh - 3 and blotch(f'mud{face}', px, py, 1.6) > 0.35:
        c = mix(c, MUD, 0.7)
    if min(fw, fh) > 2 and (px in (0, fw - 1)):
        c = shade(c, 0.86)
    return shade(c, f * rnd.choice((0.97, 1.0, 1.0, 1.03)))


def sack(face, px, py, fw, fh, rnd):
    f = {'top': 1.1, 'bottom': 0.7}.get(face, 1.04 - 0.16 * py / max(1, fh - 1))
    c = SACK_D if (px + py) % 2 else SACK
    if blotch(f'sack{face}', px, py, 2.4) > 0.7:
        c = SACK_DD
    return shade(c, f)


def felt(face, px, py, fw, fh, rnd):
    f = {'top': 1.15, 'bottom': 0.6}.get(face, 1.0)
    c = FELT_D if blotch(f'felt{face}{fw}', px, py, 3.0) > 0.6 else FELT
    return shade(c, f)


def skin(face, px, py, fw, fh, rnd):
    f = {'top': 1.1, 'bottom': 0.7}.get(face, 1.05 - 0.2 * py / max(1, fh - 1))
    c = SKIN_D if blotch(f'skin{face}{fw}{fh}', px, py, 2.6) > 0.62 else SKIN
    return shade(c, f)


def wood(face, px, py, fw, fh, rnd):
    f = {'top': 1.12, 'bottom': 0.7}.get(face, 1.0)
    long_side = fh >= fw
    grain = px if long_side else py
    c = WOOD_D if grain % 3 == 1 else (WOOD_L if rnd.random() < 0.08 else WOOD)
    return shade(c, f)


def steel(face, px, py, fw, fh, rnd):
    f = {'top': 1.15, 'bottom': 0.7}.get(face, 1.0)
    amount = blotch(f'steel{face}{fw}{fh}', px, py, 2.2)
    c = RUST if amount > 0.66 else (STEEL_D if amount < 0.25 else STEEL)
    return shade(c, f)


def cloth(face, px, py, fw, fh, rnd):
    f = {'top': 1.1, 'bottom': 0.7}.get(face, 1.05 - 0.2 * py / max(1, fh - 1))
    return shade(COAT_D if px % 3 == 1 else (48, 46, 50), f)


MATERIALS = {'coat': coat, 'sack': sack, 'felt': felt, 'skin': skin, 'wood': wood, 'steel': steel, 'cloth': cloth,
             'boot': lambda face, px, py, fw, fh, rnd: shade(BOOT, {'top': 1.25, 'bottom': 0.7}.get(face, 1.0) * rnd.choice((0.95, 1.0, 1.08))),
             'iron': lambda face, px, py, fw, fh, rnd: shade(IRON, {'top': 1.2, 'bottom': 0.7}.get(face, 1.0))}

FACE = ["sSsSsSs",
        "SsSsSsS",
        "sKKsKKs",
        "sKEsEKs",
        "SsSsSsS",
        "sxXxXxs",
        "SsSsSsS"]


def make():
    p = Painter(M, 41)
    boxes = {b.tag: b for _, _, b in M.boxes()}
    for _, _, b in M.boxes():
        if b.mat in MATERIALS:
            p.fill_box(b, MATERIALS[b.mat])
    # the hood: two holes cut for the eyes with a cold light in each, a mouth stitched with twine, a rope round the neck
    head = boxes['head']
    p.art(head, 'front', FACE, {'s': SACK, 'S': SACK_D, 'K': (16, 14, 14), 'E': EYE, 'x': SACK_DD, 'X': ROPE}, glow_chars='E')
    p.band(head, 6, ROPE)
    # the hat: a band, a notch out of the brim
    crown, brim = boxes['crown'], boxes['brim']
    p.band(crown, 3, (90, 72, 50))
    for x in range(15):
        for z in range(15):
            if 3 <= x <= 11 and 3 <= z <= 11:
                p.put(brim, 'top', x, z, FELT_D)
    for x, z in ((0, 3), (1, 3), (0, 4), (13, 14), (14, 14), (14, 13)):
        fx, fy, fw, fh = brim.faces()['top']
        p.clear(fx + x, fy + z)
        fx, fy, fw, fh = brim.faces()['bottom']
        p.clear(fx + x, fy + z)
    # the coat: a row of buttons, a rope for a belt, the two straps of the coffin across the chest
    torso, hips = boxes['torso'], boxes['hips']
    for y in range(1, 13, 3):
        p.put(torso, 'front', 6, y, (150, 140, 110))
    for i in range(13):
        p.put(torso, 'front', min(12, i), i, ROPE if i % 2 else shade(ROPE, 0.8))
        p.put(torso, 'front', max(0, 12 - i), i, ROPE if i % 2 else shade(ROPE, 0.8))
    p.band(hips, 0, ROPE)
    p.band(hips, 1, shade(ROPE, 0.75))
    for b in (boxes['skirt_f'], boxes['skirt_b']):
        fx, fy, fw, fh = b.faces()['front']
        for x in range(fw):
            if (x * 7) % 5 < 2:
                for face in ('front', 'back'):
                    ffx, ffy, _, _ = b.faces()[face]
                    p.clear(ffx + x, ffy + fh - 1)
                    if (x * 3) % 4 == 0:
                        p.clear(ffx + x, ffy + fh - 2)
    # the coffin: planks, a pale cross of nails on its lid (the side away from his back), iron at the corners
    coffin = boxes['coffin']
    for y in range(24):
        for x in (2, 5):
            p.put(coffin, 'back', x, y, WOOD_D)
    for y in range(4, 18):
        p.put(coffin, 'back', 4, y, (176, 168, 140))
    for x in range(2, 7):
        p.put(coffin, 'back', x, 8, (176, 168, 140))
    for (x, y) in ((0, 0), (8, 0), (0, 23), (8, 23)):
        p.put(coffin, 'back', x, y, IRON)
    # hands: long fingers, earth under the nails
    for part in ('right_forearm', 'left_forearm'):
        forearm, hand = M.parts[part]['boxes']
        p.band(forearm, 0, COAT_D)
        p.band(forearm, 1, COAT)
        for face in ('front', 'back', 'right', 'left'):
            fx, fy, fw, fh = hand.faces()[face]
            for x in range(1, fw, 2):
                p.put(hand, face, x, fh - 1, SKIN_DD)
                p.put(hand, face, x, fh - 2, SKIN_D)
    # the spade: a worn edge, earth on the blade
    blade = boxes['blade']
    for face in ('front', 'back'):
        for x in range(9):
            p.put(blade, face, x, 0, STEEL_L)
            if x in (0, 8):
                for y in range(0, 3):
                    fx, fy, fw, fh = blade.faces()[face]
                    p.clear(fx + x, fy + y)
        for x, y in ((2, 2), (3, 2), (3, 3), (5, 4), (6, 3), (4, 6), (2, 7)):
            p.put(blade, face, x, y, MUD)
    # the lantern at his belt: a corpse-light, green and cold
    lantern = boxes['lantern']

    def pane(face, px, py, fw, fh, r):
        if face in ('top', 'bottom') or px in (0, fw - 1) or py in (0, fh - 1):
            return IRON
        return GLASS_L if py in (2, 3) and px in (1, 2) else GLASS
    p.fill_box(lantern, pane)
    for face in ('right', 'front', 'left', 'back'):
        fx, fy, fw, fh = lantern.faces()[face]
        for x in range(1, fw - 1):
            for y in range(1, fh - 1):
                p.glow.putpixel((fx + x, fy + y), p.get(lantern, face, x, y)[:3] + (255,))
    return p.tex, p.glow


IDLE = {'right_arm': (-0.35, 0, 0.1), 'left_arm': (0.1, 0, -0.1), 'right_forearm': (-1.1, 0, 0), 'left_forearm': (-0.25, 0, 0), 'spade': (0.5, 0, 0)}
RAISE = dict(IDLE, right_arm=(-2.9, 0, 0.1), right_forearm=(-0.2, 0, 0), chest=(-0.3, 0, 0), head=(-0.2, 0, 0), spade=(-0.2, 0, 0),
             left_arm=(-2.6, 0, -0.3), left_forearm=(-0.3, 0, 0))
STRIKE = dict(IDLE, right_arm=(-1.0, 0, 0.1), right_forearm=(-0.2, 0, 0), chest=(0.5, 0, 0), head=(0.2, 0, 0), spade=(0.45, 0, 0),
              left_arm=(-0.9, 0, -0.3))
SWEEP = dict(IDLE, right_arm=(-1.45, -1.0, 0.1), right_forearm=(-0.1, 0, 0), chest=(0.12, -0.7, 0), spade=(0.3, 0, 0))
RUSH = dict(IDLE, chest=(0.75, 0.5, 0), head=(-0.5, 0, 0), right_arm=(0.5, 0, 0.3), left_arm=(-0.8, 0, -0.2), right_leg=(-0.8, 0, 0), left_leg=(0.7, 0, 0))

if __name__ == '__main__':
    M.write_java(JAVA)
    tex, glow = make()
    tex.save(f'{A}/textures/entity/gravedigger.png')
    glow.save(f'{A}/textures/entity/gravedigger_glow.png')
    if len(sys.argv) > 1:
        shots = [M.render(tex, yaw=yaw, pose=pose, size=(330, 430), zoom=5.6, centre=(0, -12), glow=glow)
                 for yaw, pose in ((-0.5, IDLE), (0.9, IDLE), (3.0, IDLE), (-0.5, RAISE), (-0.7, STRIKE), (-0.3, SWEEP), (-1.1, RUSH))]
        sheet(shots, 7).save(sys.argv[1])
        tex.resize((512, 512), 0).save(sys.argv[1].replace('.png', '_tex.png'))
