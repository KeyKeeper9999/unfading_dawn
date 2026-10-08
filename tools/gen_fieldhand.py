#!/usr/bin/env python3
"""The people of the Stubble - the reaper, the mower, the sackman, the scorched and the Hollow Miller: one mesh for
FieldhandModel.java (each kind shows its own parts of it; the model is drawn larger for some), a texture for each kind
and the glow of the scorched's cracks and of the Miller's lantern.
Run from the repo root; pass a file name to also get a preview sheet."""
import math
import sys

from modelkit import Box, Model, Painter, blotch, mix, shade, sheet

A = 'src/main/resources/assets/unfading_dawn'
JAVA = 'src/main/java/com/keykeeper/dushi/client/FieldhandModel.java'

M = Model(128, 128)
# Those who were sent out to get the corn in before sunset, and are still at it: the sun has not moved, and what is
# cut stands again. Dried by it to leather, in linen bleached white. The trunk turns about the hips; the hem of the
# shirt hangs from the waist and is kept hanging whatever the trunk does; arms and legs have a joint each.
HIP = 11
M.part('right_leg', None, (-2, HIP, 0), boxes=[Box(-1.5, 0, -1.5, 3, 7, 3, 'breeches', 'thigh')])
M.part('right_shin', 'right_leg', (0, 7, 0), boxes=[Box(-1.5, 0, -1.5, 3, 6, 3, 'wraps', 'shin'), Box(-1.5, 5, -3, 3, 1, 2, 'bast', 'foot')])
M.mirror('right_leg', 'left_leg', parent=None)
M.mirror('right_shin', 'left_shin')
M.part('torso', None, (0, HIP, 0), (0.1, 0, 0), boxes=[Box(-4, -12, -2, 8, 12, 4, 'shirt', 'chest')])
M.part('tail', 'torso', (0, -1, 0), (-0.1, 0, 0), boxes=[Box(-4.5, 0, -2.5, 9, 6, 5, 'shirt', 'tail')])
M.part('head', 'torso', (0, -12, 0), (-0.05, 0, 0), boxes=[Box(-4, -8, -4, 8, 8, 8, 'head', 'head')])
M.part('right_arm', 'torso', (-5.5, -10.5, 0), boxes=[Box(-1.5, -1.5, -1.5, 3, 7, 3, 'sleeve', 'upper')])
M.part('right_forearm', 'right_arm', (0, 5.5, 0), (-0.15, 0, 0), boxes=[Box(-1.5, 0, -1.5, 3, 7, 3, 'sleeve', 'fore')])
M.mirror('right_arm', 'left_arm')
M.mirror('right_forearm', 'left_forearm')
# --- the reaper: a kerchief tied over the head against the sun; a sheaf bound on the back; the sickle. A held thing
# lies across the arm, pointing forward when the arm hangs: it is drawn standing and turned a quarter.
M.part('kerchief', 'head', (0, 0, 0), boxes=[Box(-4.5, -8.5, -4.5, 9, 6, 9, 'kerchief', 'kerchief')])
M.part('sheaf', 'torso', (0, 0, 0), (0, 0, 0.18), boxes=[Box(-2.5, -13, 2, 5, 12, 3, 'straw', 'sheaf'), Box(-3.5, -18, 1.5, 7, 5, 4, 'ears', 'ears')])
M.part('sickle', 'right_forearm', (0, 6, 0), (math.pi / 2, 0, 0), boxes=[
    Box(-0.5, -3, -0.5, 1, 5, 1, 'haft', 'grip'), Box(-0.5, -6, -0.5, 1, 3, 1, 'iron', 'tang'), Box(0.5, -8, -0.5, 2, 2, 1, 'iron', 'bend_a'),
    Box(2.5, -9, -0.5, 3, 1, 1, 'iron', 'crown'), Box(5.5, -8, -0.5, 1, 2, 1, 'iron', 'bend_b'), Box(6.5, -6, -0.5, 1, 2, 1, 'iron', 'point')])
# --- the mower: a straw hat as wide as his shoulders; the scythe in both hands, the blade out to his left
M.part('straw_hat', 'head', (0, 0, 0), boxes=[Box(-4.5, -10, -4.5, 9, 3, 9, 'strawhat', 'crown'), Box(-7, -7.5, -7, 14, 1, 14, 'strawhat', 'brim')])
M.part('scythe', 'right_forearm', (0, 6, 0), (math.pi / 2, 0, 0), boxes=[
    Box(-0.5, -26, -0.5, 1, 36, 1, 'haft', 'snath'), Box(-2.5, -1, -0.5, 2, 1, 1, 'haft', 'nib_a'), Box(-2.5, -12, -0.5, 2, 1, 1, 'haft', 'nib_b'),
    Box(0.5, -27, -0.5, 13, 2, 1, 'iron', 'blade'), Box(13.5, -26.5, -0.5, 3, 1, 1, 'iron', 'tip')])
# --- the sackman: a flat cap white with flour; a great sack on his back, bound to him; a small one in his hand to throw
M.part('flat_cap', 'head', (0, 0, 0), boxes=[Box(-4.5, -9.5, -5.5, 9, 2, 10, 'felt', 'cap')])
M.part('bigsack', 'torso', (0, 0, 0), (0.12, 0, 0), boxes=[Box(-5, -15, 2, 10, 12, 6, 'sack', 'bigsack'), Box(-2, -17, 3.5, 4, 2, 3, 'sack', 'neck')])
M.part('bag', 'right_forearm', (0, 7, 0), boxes=[Box(-2, 0, -2, 4, 5, 4, 'sack', 'bag'), Box(-1, -1, -1, 2, 1, 2, 'sack', 'bagneck')])
# --- the Hollow Miller (drawn half as large again): a beard white with it, a leather apron to the shins, a lantern
# in his left hand; the bar he levers the stone with; and, when he has torn it off its axle, the stone itself, on edge
# before him with his hands on it (it stands on the floor and hangs from nothing: see FieldhandModel)
M.part('beard', 'head', (0, 0, 0), boxes=[Box(-3.5, -2.5, -4.8, 7, 6, 1, 'hair', 'beard')])
M.part('bulk', 'torso', (0, 0, 0), boxes=[Box(-6, -12.8, -3, 12, 6, 6, 'shirt', 'shoulders'), Box(-4.8, -6.8, -2.8, 9.6, 6, 5.6, 'shirt', 'belly')])
M.part('apron', 'torso', (0, -8, 0), (-0.1, 0, 0), boxes=[Box(-5, 0, -3.4, 10, 17, 1, 'leather', 'apron')])
M.part('lamp', 'left_forearm', (0, 7, 0), boxes=[Box(-1.5, 1, -1.5, 3, 4, 3, 'lantern', 'lamp'), Box(-0.5, 0, -0.5, 1, 1, 1, 'iron', 'ring')])
M.part('lever', 'right_forearm', (0, 6, 0), (math.pi / 2, 0, 0), boxes=[Box(-1, -30, -1, 2, 40, 2, 'haft', 'bar'), Box(-1.5, -34, -1.5, 3, 4, 3, 'iron', 'shoe')])
M.part('stone', None, (0, 15, -12), boxes=[Box(-2, -9, -5, 4, 18, 10, 'stone', 'rim_a'), Box(-2, -5, -9, 4, 10, 18, 'stone', 'rim_b'),
                                           Box(-2.2, -7.5, -7.5, 4.4, 15, 15, 'stone', 'face'), Box(-2.6, -1.5, -1.5, 5.2, 3, 3, 'iron', 'eye')])
M.pack()

KINDS = ('reaper', 'mower', 'sackman', 'scorched', 'hollow_miller')
PARTS = {'reaper': ('kerchief', 'sheaf', 'sickle'), 'mower': ('straw_hat', 'scythe'), 'sackman': ('flat_cap', 'bigsack', 'bag'), 'scorched': ('sickle',),
         'hollow_miller': ('flat_cap', 'beard', 'bulk', 'apron', 'lamp', 'lever', 'stone')}
GLOWS = ('scorched', 'hollow_miller')

SKIN = (150, 110, 78)           # dried by the sun: leather
SKIN_D = (112, 78, 54)
SKIN_DD = (74, 50, 36)
LINEN = (212, 202, 176)
LINEN_D = (170, 158, 130)
LINEN_L = (236, 228, 206)
DUST = (190, 164, 110)
STRAW = (214, 180, 88)
STRAW_D = (166, 132, 58)
STRAW_L = (240, 214, 130)
EAR = (228, 190, 80)
EAR_D = (176, 138, 50)
RED = (150, 52, 40)
RED_D = (106, 36, 32)
BAST = (168, 140, 92)
BAST_D = (120, 98, 62)
HOMESPUN = (120, 104, 82)
HOMESPUN_D = (88, 76, 60)
IRON = (120, 122, 126)
IRON_D = (72, 74, 80)
IRON_L = (188, 192, 196)
RUST = (136, 84, 52)
WOOD = (140, 108, 70)
WOOD_D = (98, 74, 48)
WOOD_L = (176, 146, 100)
SACK = (176, 160, 124)
SACK_D = (132, 118, 88)
FLOUR = (238, 234, 222)
FLOUR_D = (206, 200, 186)
LEATHER = (104, 70, 44)
LEATHER_D = (70, 46, 32)
HAIR = (226, 222, 210)
HAIR_D = (176, 172, 160)
FELT = (96, 88, 80)
STONE = (150, 146, 136)
STONE_D = (108, 104, 98)
STONE_L = (188, 184, 172)
CHAR = (36, 30, 28)             # the scorched: charcoal, split, and red in the splits
CHAR_D = (20, 16, 16)
CHAR_L = (62, 52, 46)
ASH = (128, 122, 116)
EMBER = (255, 132, 40)
EMBER_L = (255, 210, 110)
BLACK = (14, 12, 12)
EYE = (236, 226, 190)
FLAME = (255, 214, 120)


def lit(face, py, fh, top=1.1, bottom=0.7, fall=0.18):
    return {'top': top, 'bottom': bottom}.get(face, 1.03 - fall * py / max(1, fh - 1))


def dusty(c, seed, face, px, py, fh, amount=0.35):
    """Whatever it is, it has been in the field all day: the dust of the road on the lower part of it, chaff caught in it."""
    if face in ('top', 'bottom'):
        return c
    if py >= fh * (1 - amount) and blotch(f'dust{seed}{face}', px, py, 1.7) > 0.4:
        c = mix(c, DUST, 0.45)
    if blotch(f'chaff{seed}{face}', px, py, 1.2) > 0.84:
        c = mix(c, STRAW, 0.7)
    return c


def charred(seed, face, px, py, fw, fh, rnd, p=None):
    """Burnt through: black, split along the grain, and what is in the splits is still alight."""
    crack = blotch(f'crk{seed}{face}', px * 1.7, py * 0.55, 1.5)
    if 0.47 < crack < 0.55:
        return EMBER_L if rnd.random() < 0.3 else EMBER
    c = CHAR_D if blotch(f'ch{seed}{face}', px, py, 1.9) > 0.55 else (CHAR_L if rnd.random() < 0.12 else CHAR)
    if rnd.random() < 0.05:
        c = ASH
    return shade(c, lit(face, py, fh, 1.15, 0.75, 0.1))


def shirt(kind, tag):
    def fn(face, px, py, fw, fh, rnd):
        if kind == 'scorched':
            if tag == 'tail' and face not in ('top', 'bottom') and py >= fh - 3 and blotch(f'burn{face}', px, 0, 1.0) > 0.35 + 0.15 * (fh - 1 - py):
                return None                                      # what is left of the hem
            return charred(tag, face, px, py, fw, fh, rnd)
        base, dark, light = (FLOUR_D, SACK_D, FLOUR) if kind in ('sackman', 'hollow_miller') else (LINEN, LINEN_D, LINEN_L)
        c = dark if blotch(f'sh{kind}{tag}{face}', px, py * 0.6, 2.2) > 0.62 else (light if rnd.random() < 0.1 else base)
        if tag == 'chest' and face == 'front' and px in (fw // 2 - 1, fw // 2) and py < 5:
            c = SKIN_D if py < 3 else dark                       # open at the neck
        if tag in ('chest', 'tail', 'fore') and face not in ('top', 'bottom') and kind in ('reaper', 'mower'):
            # a band of red stitching at the hem and the cuffs, as a shirt for the harvest has
            if (tag == 'tail' and py == fh - 2) or (tag == 'fore' and py == fh - 2):
                c = RED if (px + (0 if face in ('front', 'back') else 1)) % 2 else RED_D
        if tag == 'chest' and py == fh - 2 and face not in ('top', 'bottom'):
            c = BAST if (px + (0 if face in ('front', 'back') else 1)) % 2 else BAST_D     # a twist of straw for a belt
        if tag == 'tail' and face not in ('top', 'bottom') and py == fh - 1 and (px * 5 + len(face)) % 4 == 0:
            return None
        c = dusty(c, kind + tag, face, px, py, fh, 0.5 if tag in ('tail', 'fore') else 0.25)
        return shade(c, lit(face, py, fh))
    return fn


def breeches(kind):
    def fn(face, px, py, fw, fh, rnd):
        if kind == 'scorched':
            return charred('br', face, px, py, fw, fh, rnd)
        c = HOMESPUN_D if (px + py * 2) % 3 == 0 or blotch(f'br{face}', px, py, 2.0) > 0.6 else HOMESPUN
        return shade(dusty(c, 'br', face, px, py, fh, 0.6), lit(face, py, fh))
    return fn


def wraps(kind, tag):
    def fn(face, px, py, fw, fh, rnd):
        if kind == 'scorched':
            return charred('wr' + tag, face, px, py, fw, fh, rnd)
        if tag == 'foot':
            return shade(BAST if (px + py) % 2 else BAST_D, lit(face, py, fh))          # bast shoes
        c = LINEN_D if (py + px // 2) % 2 else LINEN                                     # leg wraps, wound
        if (py * 3 + px) % 7 == 0:
            c = BAST_D                                                                   # and the cord over them
        return shade(dusty(c, 'wr', face, px, py, fh, 0.7), lit(face, py, fh))
    return fn


def skin(kind):
    def fn(face, px, py, fw, fh, rnd):
        if kind == 'scorched':
            return charred('skin', face, px, py, fw, fh, rnd)
        c = SKIN_D if blotch(f'skin{fw}{fh}{face}', px, py * 0.6, 1.8) > 0.6 else SKIN
        return shade(c, lit(face, py, fh, 1.08, 0.7, 0.1))
    return fn


def wood(face, px, py, fw, fh, rnd):
    c = WOOD_D if (py + px // 2) % 5 == 0 or blotch(f'wood{fw}{fh}{face}', px, py * 0.3, 1.5) > 0.66 else (WOOD_L if rnd.random() < 0.1 else WOOD)
    return shade(c, {'top': 1.1, 'bottom': 0.72}.get(face, 1.0))


def iron(tag):
    def fn(face, px, py, fw, fh, rnd):
        c = RUST if blotch(f'iron{tag}{face}', px, py, 1.6) > 0.74 else (IRON_L if rnd.random() < 0.2 else (IRON_D if rnd.random() < 0.15 else IRON))
        if tag in ('blade', 'tip') and face in ('front', 'back') and py == fh - 1:
            c = IRON_L                                           # the edge, whetted every hour of a day that has one hour
        return shade(c, {'top': 1.15, 'bottom': 0.7}.get(face, 1.0))
    return fn


def straw(tag):
    def fn(face, px, py, fw, fh, rnd):
        if tag == 'ears':
            if face == 'bottom' or (face != 'top' and py == 0 and (px + len(face)) % 2 == 0):
                return None
            c = EAR_D if (px + py) % 2 == 0 else (STRAW_L if rnd.random() < 0.2 else EAR)
            return shade(c, lit(face, py, fh, 1.12, 0.8, 0.08))
        c = STRAW_D if px % 2 == 0 else (STRAW_L if rnd.random() < 0.15 else STRAW)
        if face not in ('top', 'bottom') and py in (fh // 2, fh // 2 + 1):
            c = BAST_D                                           # the band
        return shade(c, lit(face, py, fh, 1.1, 0.75, 0.1))
    return fn


def strawhat(tag):
    def fn(face, px, py, fw, fh, rnd):
        if tag == 'brim':
            if face not in ('top', 'bottom'):
                return None
            cx, cy = px - (fw - 1) / 2, py - (fh - 1) / 2
            r = (cx / (fw / 2)) ** 2 + (cy / (fh / 2)) ** 2
            if r > 1.02 or (r > 0.8 and blotch('brimfray', px, py, 1.0) > 0.7):
                return None                                      # round, and gone at the edge
            ring = int(math.hypot(cx, cy))
            c = STRAW_D if ring % 2 == 0 else STRAW
            return shade(c, 1.08 if face == 'top' else 0.7)
        if face == 'bottom':
            return None
        c = STRAW_D if (py + (px if face != 'top' else 0)) % 2 == 0 else (STRAW_L if rnd.random() < 0.15 else STRAW)
        if face != 'top' and py == fh - 1:
            c = RED_D                                            # a band round the crown
        return shade(c, lit(face, py, fh, 1.12, 0.7, 0.08))
    return fn


def sack(tag):
    def fn(face, px, py, fw, fh, rnd):
        c = SACK_D if (px + py) % 2 == 0 and blotch(f'sk{tag}{face}', px, py, 2.0) > 0.4 else SACK
        if blotch(f'fl{tag}{face}', px, py, 2.4) > 0.5:
            c = mix(c, FLOUR, 0.7)                               # white with what is in it
        if tag in ('neck', 'bagneck'):
            c = BAST_D if py == fh - 1 else c
        if tag == 'bigsack' and face == 'back' and px in (2, fw - 3):
            c = BAST_D                                           # the cords he is bound to it with
        return shade(c, lit(face, py, fh, 1.1, 0.72, 0.14))
    return fn


def stone(tag):
    def fn(face, px, py, fw, fh, rnd):
        if tag == 'face':
            if face not in ('right', 'left'):
                return None
            cx, cy = px - (fw - 1) / 2, py - (fh - 1) / 2
            r = math.hypot(cx, cy)
            if r > fw / 2 + 0.2:
                return None
            c = STONE_D if blotch(f'st{face}', px, py, 2.2) > 0.6 else (STONE_L if rnd.random() < 0.1 else STONE)
            # the furrows a stone is dressed with, in quarters, worn almost flat: it has ground nothing for a long time
            ang = math.atan2(cy, cx)
            if r > 2.2 and int((ang + math.pi) / (math.pi / 4) * 2.0 + r * 0.9) % 3 == 0:
                c = shade(c, 0.8)
            if blotch(f'fls{face}', px, py, 2.0) > 0.62:
                c = mix(c, FLOUR, 0.55)
            return shade(c, 1.0 - 0.1 * py / fh)
        c = STONE_D if (px + py) % 4 == 0 or blotch(f'sr{tag}{face}', px, py, 2.0) > 0.62 else STONE
        return shade(c, lit(face, py, fh, 1.12, 0.66, 0.2))
    return fn


def make(kind):
    p = Painter(M, 401 + KINDS.index(kind))
    boxes = {b.tag: b for _, _, b in M.boxes()}
    for name, _, b in M.boxes():
        if b.mat in ('shirt', 'sleeve'):
            p.fill_box(b, shirt(kind, b.tag))
        elif b.mat == 'breeches':
            p.fill_box(b, breeches(kind))
        elif b.mat in ('wraps', 'bast'):
            p.fill_box(b, wraps(kind, b.tag))
        elif b.mat == 'head':
            p.fill_box(b, skin(kind))
        elif b.mat == 'kerchief':
            def kerchief(face, px, py, fw, fh, rnd):
                if face == 'bottom':
                    return None
                if face == 'front' and 1 <= px <= fw - 2 and py >= 2:
                    return None                                  # the face looks out of it
                if face != 'top' and py == fh - 1 and face != 'back' and (px + len(face)) % 3 == 0:
                    return None
                c = LINEN_D if blotch(f'kf{face}', px, py, 2.0) > 0.6 else LINEN
                if face != 'top' and py == 1:
                    c = RED if px % 2 else RED_D
                return shade(c, lit(face, py, fh, 1.1, 0.72, 0.12))
            p.fill_box(b, kerchief)
        elif b.mat in ('straw', 'ears'):
            p.fill_box(b, straw(b.tag))
        elif b.mat == 'strawhat':
            p.fill_box(b, strawhat(b.tag))
        elif b.mat == 'felt':
            def felt(face, px, py, fw, fh, rnd):
                if face == 'bottom':
                    return None
                c = FELT if blotch(f'ft{face}', px, py, 2.0) < 0.55 else mix(FELT, FLOUR, 0.6)
                return shade(c, lit(face, py, fh, 1.12, 0.7, 0.1))
            p.fill_box(b, felt)
        elif b.mat == 'sack':
            p.fill_box(b, sack(b.tag))
        elif b.mat == 'hair':
            def hair(face, px, py, fw, fh, rnd):
                if face in ('front', 'back') and py >= fh - 2 and (px in (0, fw - 1) or (px + py) % 3 == 0):
                    return None
                return shade(HAIR_D if (px * 3 + py) % 4 == 0 else HAIR, 1.0 - 0.04 * py)
            p.fill_box(b, hair)
        elif b.mat == 'leather':
            def leather(face, px, py, fw, fh, rnd):
                if face in ('front', 'back') and py < 3 and (px < 2 or px > fw - 3):
                    return None                                  # the bib of it
                c = LEATHER_D if blotch(f'lt{face}', px, py * 0.6, 2.2) > 0.58 else LEATHER
                if blotch(f'ltf{face}', px, py, 2.6) > 0.6:
                    c = mix(c, FLOUR, 0.45)
                if face in ('front', 'back') and py == fh - 1 and (px * 3) % 4 == 0:
                    return None
                return shade(c, lit(face, py, fh, 1.1, 0.7, 0.14))
            p.fill_box(b, leather)
        elif b.mat == 'iron':
            p.fill_box(b, iron(b.tag))
        elif b.mat == 'haft':
            p.fill_box(b, wood)
        elif b.mat == 'stone':
            p.fill_box(b, stone(b.tag))
        elif b.mat == 'lantern':
            def lantern(face, px, py, fw, fh, rnd):
                if face in ('top', 'bottom') or py in (0, fh - 1) or px in (0, fw - 1):
                    return IRON if face != 'bottom' else IRON_D
                return FLAME
            p.fill_box(b, lantern)
    head = boxes['head']
    pal = {'s': SKIN, 'd': SKIN_D, 'D': SKIN_DD, 'B': BLACK, 'E': EYE, 'G': EMBER_L, 'g': EMBER, 'c': CHAR, 'C': CHAR_D, 'h': HAIR_D, 'H': HAIR, 'f': FLOUR}
    faces = {
        # eyes screwed up against a sun that has stood in them for years: two slits
        'reaper': ["dddddddd", "dssssssd", "sdDssDds", "sBBddBBs", "sdDssDds", "ssdDDdss", "sdBBBBds", "ddssssdd"],
        'mower': ["DDDDDDDD", "dDDDDDDd", "sDDssDDs", "sBEddEBs", "sdDssDds", "ssdDDdss", "sdBBBBds", "ddsDDsdd"],
        # white with flour, all but the eyes and the mouth
        'sackman': ["ffffffff", "fsfsffsf", "fDDffDDf", "fBEffEBf", "fDDffDDf", "ffsDDsff", "fdBBBBdf", "ffffffff"],
        # no face: a cinder, with what is alight in it showing where the eyes and the mouth were
        'scorched': ["CCcCCcCC", "CccccccC", "cCCccCCc", "CGgCCgGC", "cCCccCCc", "ccCggCcc", "cCgGGgCc", "CccCCccC"],
        'hollow_miller': ["ffffffff", "fsdssdsf", "DDDssDDD", "DEBddBED", "sDDssDDs", "HdsDDsdH", "HHBBBBHH", "HHHHHHHH"],
    }[kind]
    p.art(head, 'front', faces, pal, glow_chars='Gg')
    if kind != 'scorched':
        for face in ('back', 'right', 'left', 'top'):
            fx, fy, fw, fh = head.faces()[face]
            for x in range(fw):
                for y in range(fh if face in ('top', 'back') else 4):
                    if face == 'top' or y < 3 + (x * 7) % 3:
                        p.px(fx + x, fy + y, shade(HAIR_D if kind in ('hollow_miller', 'sackman') else STRAW_D, 0.75 + 0.3 * blotch(f'hr{kind}{face}', x, y, 1.4)))
    # what is alight goes to the glow picture: the scorched's cracks all over, the lantern
    if kind in GLOWS:
        shown = set(PARTS[kind]) | {'right_leg', 'right_shin', 'left_leg', 'left_shin', 'torso', 'tail', 'head', 'right_arm', 'right_forearm', 'left_arm', 'left_forearm'}
        for name, _, b in M.boxes():
            if name not in shown or (kind == 'scorched' and b.mat in ('iron', 'haft')):
                continue
            if kind == 'hollow_miller' and b.mat != 'lantern':
                continue
            for face, (fx, fy, fw, fh) in b.faces().items():
                for x in range(fw):
                    for y in range(fh):
                        r_, g_, b_, a_ = p.tex.getpixel((fx + x, fy + y))
                        if a_ and r_ > 240 and g_ > 120 and b_ < 140:
                            p.glow.putpixel((fx + x, fy + y), (r_, g_, b_, 255))
    return p.tex, p.glow


def hidden(kind, extra=()):
    every = {part for parts in PARTS.values() for part in parts}
    return tuple((every - set(PARTS[kind])) | set(extra))


# The poses of FieldhandModel.setupAnim, as it sets them (rotations added to the rest pose).
# -- the reaper: bent as one who cuts low all day, the sickle before him; drawn back across; cut; the lunge
REAP_STAND = {'torso': (0.5, 0, 0), 'tail': (-0.5, 0, 0), 'head': (-0.4, 0, 0), 'right_arm': (-0.9, 0, -0.1), 'right_forearm': (-0.5, 0, 0), 'left_arm': (-0.5, 0, 0.1),
              'left_forearm': (-0.6, 0, 0)}
REAP_DRAW = {'torso': (0.45, 0.6, 0), 'tail': (-0.45, 0, 0), 'head': (-0.4, -0.4, 0), 'right_arm': (-1.3, 0.9, 0), 'right_forearm': (-0.3, 0, 0), 'left_arm': (-0.9, 0, 0.2)}
REAP_CUT = {'torso': (0.55, -0.7, 0), 'tail': (-0.55, 0, 0), 'head': (-0.4, 0.5, 0), 'right_arm': (-1.3, -0.9, 0), 'right_forearm': (-0.3, 0, 0), 'left_arm': (0.2, 0, 0.3)}
REAP_LUNGE = {'torso': (0.8, -0.2, 0), 'tail': (-0.8, 0, 0), 'head': (-0.7, 0, 0), 'right_arm': (-1.9, 0, 0), 'right_forearm': (-0.1, 0, 0), 'left_arm': (0.6, 0, 0.3),
              'right_leg': (-0.7, 0, 0), 'left_leg': (0.6, 0, 0)}
# -- the mower: the scythe carried low across him; drawn round; swept
MOW_STAND = {'torso': (0.15, 0.25, 0), 'tail': (-0.15, 0, 0), 'right_arm': (-0.35, 0.3, 0), 'right_forearm': (-0.2, 0, 0), 'left_arm': (-0.9, 0.5, 0), 'left_forearm': (-0.5, 0, 0)}
MOW_DRAWN = {'torso': (0.2, 1.0, 0), 'tail': (-0.2, 0, 0), 'right_arm': (-0.1, 0.6, 0), 'right_forearm': (0.1, 0, 0), 'left_arm': (-0.9, 0.3, 0), 'left_forearm': (-0.8, 0, 0)}
MOW_SWEPT = {'torso': (0.3, -0.9, 0), 'tail': (-0.3, 0, 0), 'right_arm': (-0.1, -0.5, 0), 'right_forearm': (0.1, 0, 0), 'left_arm': (-0.7, -0.4, 0), 'left_forearm': (-0.9, 0, 0),
             'right_leg': (-0.3, 0, 0), 'left_leg': (0.3, 0, 0)}
MOW_UP = {'torso': (-0.25, 0, 0), 'tail': (0.25, 0, 0), 'right_arm': (-2.8, 0, 0), 'right_forearm': (-0.3, 0, 0), 'left_arm': (-2.6, 0, 0.3), 'left_forearm': (-0.5, 0, 0)}
# -- the sackman: bowed under it, the small sack hanging from his hand; swung back; thrown; the sack swung round as a club
SACK_STAND = {'torso': (0.4, 0, 0), 'tail': (-0.4, 0, 0), 'head': (-0.3, 0, 0), 'right_arm': (-0.1, 0, -0.1), 'left_arm': (-0.8, 0, 0.1), 'left_forearm': (-1.2, 0, 0)}
SACK_BACK = {'torso': (0.2, 0.6, 0), 'tail': (-0.2, 0, 0), 'right_arm': (1.0, 0, -0.2), 'right_forearm': (-0.2, 0, 0), 'left_arm': (-1.2, 0, 0.2)}
SACK_THROWN = {'torso': (0.6, -0.4, 0), 'tail': (-0.6, 0, 0), 'right_arm': (-2.0, -0.2, 0), 'right_forearm': (-0.1, 0, 0), 'left_arm': (0.5, 0, 0.3), 'right_leg': (-0.5, 0, 0),
               'left_leg': (0.4, 0, 0)}
# -- the scorched: upright, stiff, the arms held off him; reaching with both
SCORCH_STAND = {'torso': (-0.05, 0, 0), 'tail': (0.05, 0, 0), 'right_arm': (-0.3, 0, -0.35), 'right_forearm': (-0.5, 0, 0), 'left_arm': (-0.3, 0, 0.35), 'left_forearm': (-0.5, 0, 0)}
SCORCH_GRASP = {'torso': (0.3, 0, 0), 'tail': (-0.3, 0, 0), 'right_arm': (-1.5, 0.3, 0), 'right_forearm': (-0.2, 0, 0), 'left_arm': (-1.5, -0.3, 0), 'left_forearm': (-0.2, 0, 0),
                'right_leg': (-0.4, 0, 0), 'left_leg': (0.3, 0, 0)}
# -- the Miller: the bar on his shoulder; his blows are the mower's, with the bar; both hands on the stone
MILL_STAND = {'torso': (0.06, 0, 0), 'apron': (-0.06, 0, 0), 'right_arm': (-2.2, 0, -0.1), 'right_forearm': (-0.9, 0, 0), 'left_arm': (0.1, 0, 0.12)}
MILL_ROLL = {'torso': (0.45, 0, 0), 'apron': (-0.45, 0, 0), 'tail': (-0.45, 0, 0), 'right_arm': (-1.25, 0.25, 0), 'right_forearm': (-0.2, 0, 0), 'left_arm': (-1.25, -0.25, 0),
             'left_forearm': (-0.2, 0, 0), 'right_leg': (0.5, 0, 0), 'left_leg': (-0.4, 0, 0)}

if __name__ == '__main__':
    M.write_java(JAVA) if len(sys.argv) < 3 else None
    shots = []
    for kind in KINDS:
        tex, glow = make(kind)
        tex.save(f'{A}/textures/entity/{kind}.png')
        if kind in GLOWS:
            glow.save(f'{A}/textures/entity/{kind}_glow.png')
        poses = {'reaper': ((-0.5, REAP_STAND), (0.0, REAP_STAND), (2.6, REAP_STAND), (-1.3, REAP_STAND), (-0.4, REAP_DRAW), (-0.4, REAP_CUT), (-1.3, REAP_LUNGE), (-0.5, REAP_LUNGE)),
                 'mower': ((-0.5, MOW_STAND), (0.0, MOW_STAND), (2.6, MOW_STAND), (-1.3, MOW_STAND), (-0.4, MOW_DRAWN), (-0.4, MOW_SWEPT), (-1.3, MOW_UP), (0.0, MOW_SWEPT)),
                 'sackman': ((-0.5, SACK_STAND), (0.0, SACK_STAND), (2.6, SACK_STAND), (-1.3, SACK_STAND), (-1.2, SACK_BACK), (-1.3, SACK_THROWN), (-0.4, MOW_DRAWN), (-0.4, MOW_SWEPT)),
                 'scorched': ((-0.5, SCORCH_STAND), (0.0, SCORCH_STAND), (2.6, SCORCH_STAND), (-1.3, SCORCH_STAND), (-0.5, SCORCH_GRASP), (-1.3, SCORCH_GRASP), (0.0, SCORCH_GRASP),
                              (-0.4, REAP_CUT)),
                 'hollow_miller': ((-0.5, MILL_STAND), (0.0, MILL_STAND), (2.6, MILL_STAND), (-0.4, MOW_DRAWN), (-0.4, MOW_SWEPT), (-1.3, MOW_UP), (-1.3, MILL_ROLL), (-0.5, MILL_ROLL))}[kind]
        if len(sys.argv) > 1:
            for n, (yaw, pose) in enumerate(poses):
                rolling = kind == 'hollow_miller' and pose is MILL_ROLL
                extra = () if kind != 'hollow_miller' else (('lever',) if rolling else ('stone',))
                shots.append(M.render(tex, yaw=yaw, pose=pose, size=(270, 360), zoom=6.0, centre=(0, 4), glow=glow if kind in GLOWS else None, hidden=hidden(kind, extra),
                                      background=(196, 176, 120)))
    if len(sys.argv) > 1:
        sheet(shots, 8).save(sys.argv[1])
