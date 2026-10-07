#!/usr/bin/env python3
"""The eight suits of armour: the textures worn on the body (in the layout the game's own armour uses), the icons,
the item models and the equipment definitions. Run from the repo root; pass a file name for a preview sheet."""
import json
import math
import os
import random
import sys

from PIL import Image

from modelkit import Box, Model, blotch, mix, shade, sheet
from sprite import Sprite

A = 'src/main/resources/assets/vsratie_dushi'
MOD = 'vsratie_dushi'

# name of the suit -> the four item names: helmet, chest, legs, boots (the same as in ArmorSet.java)
SUITS = {
    'prisoner': ('prisoner_hood', 'prisoner_rags', 'prisoner_breeches', 'prisoner_wraps'),
    'guard': ('guard_hat', 'guard_hauberk', 'guard_leggings', 'guard_boots'),
    'rust': ('rust_helm', 'rust_cuirass', 'rust_tassets', 'rust_sabatons'),
    'chaplain': ('chaplain_mitre', 'chaplain_cassock', 'chaplain_skirt', 'chaplain_shoes'),
    'vigil': ('vigil_cowl', 'vigil_shift', 'vigil_skirt', 'vigil_wraps'),
    'mason': ('mason_cap', 'mason_apron', 'mason_breeches', 'mason_boots'),
    'scale': ('scale_hood', 'scale_coat', 'scale_leggings', 'scale_waders'),
    'oilskin': ('oilskin_souwester', 'oilskin_smock', 'oilskin_trousers', 'oilskin_boots'),
}

# ---------------------------------------------------------------------------------------------------- the worn textures
#
# The layout is the old 64x32 skin: head at (0,0), the second head layer at (32,0), body at (16,16), arm at (40,16),
# leg at (0,16); the left limbs mirror the right ones. The first texture is drawn on the helmet, the chest piece and
# the boots, the second on the leggings. Whatever a piece does not cover stays see-through.

PARTS = {'head': (0, 0, 8, 8, 8), 'hat': (32, 0, 8, 8, 8), 'body': (16, 16, 8, 12, 4), 'arm': (40, 16, 4, 12, 4), 'leg': (0, 16, 4, 12, 4)}
SIDES = ('right', 'front', 'left', 'back')


def rects(part):
    u, v, w, h, d = PARTS[part]
    return {'top': (u + d, v, w, d), 'bottom': (u + d + w, v, w, d), 'right': (u, v + d, d, h), 'front': (u + d, v + d, w, h),
            'left': (u + d + w, v + d, d, h), 'back': (u + d + w + d, v + d, w, h)}


class Worn:
    def __init__(self):
        self.img = Image.new('RGBA', (64, 32), (0, 0, 0, 0))

    def put(self, part, face, px, py, c, alpha=255):
        u, v, w, h = rects(part)[face]
        if 0 <= px < w and 0 <= py < h:
            self.img.putpixel((u + px, v + py), tuple(int(x) for x in c[:3]) + (alpha,))

    def clear(self, part, face, px, py):
        u, v, w, h = rects(part)[face]
        if 0 <= px < w and 0 <= py < h:
            self.img.putpixel((u + px, v + py), (0, 0, 0, 0))

    def fill(self, part, fn, faces=('top', 'bottom') + SIDES, rows=None):
        """fn(face, px, py, w, h) -> colour or None. `rows` limits the side faces to a range of rows (top to bottom)."""
        for face in faces:
            u, v, w, h = rects(part)[face]
            for py in range(h):
                if rows is not None and face in SIDES and not rows[0] <= py <= rows[1]:
                    continue
                for px in range(w):
                    c = fn(face, px, py, w, h)
                    if c is not None:
                        self.put(part, face, px, py, c)

    def art(self, part, face, rows, palette, ox=0, oy=0):
        for y, row in enumerate(rows):
            for x, ch in enumerate(row):
                if ch == '.':
                    continue
                if ch == '_':
                    self.clear(part, face, ox + x, oy + y)
                else:
                    self.put(part, face, ox + x, oy + y, palette[ch])


def light(face, py, h):
    """Lit from above: tops bright, undersides dark, the sides darker towards the bottom."""
    return {'top': 1.14, 'bottom': 0.68}.get(face, 1.06 - 0.2 * py / max(1, h - 1))


def weave(base, dark, seed, cell=2.2, amount=0.5):
    """Coarse cloth: blotchy, with darker threads."""
    def fn(face, px, py, w, h):
        n = blotch(f'{seed}{face}', px, py, cell)
        c = mix(base, dark, amount if n > 0.58 else 0.0)
        if (px + py * 3) % 5 == 0:
            c = shade(c, 0.9)
        return shade(c, light(face, py, h))
    return fn


def mail(base, dark):
    """Rings: a checker of two greys."""
    def fn(face, px, py, w, h):
        return shade(base if (px + py) % 2 else dark, light(face, py, h))
    return fn


def plate(seed, iron=(84, 76, 76), iron_d=(64, 58, 60), rust=(128, 76, 46), rust_d=(92, 54, 36), rust_l=(160, 100, 58), steel=(150, 148, 150)):
    """Dark iron eaten by rust in patches, a bare ridge along the top, dark seams along the other edges."""
    def fn(face, px, py, w, h):
        n = blotch(f'{seed}{face}{w}{h}', px + len(face) * 5, py, 2.6)
        k = blotch(f'k{seed}{face}', px, py, 1.0)
        if n > 0.62:
            c = rust_l if k < 0.3 else rust
        elif n > 0.45:
            c = rust_d
        else:
            c = iron if k < 0.75 else iron_d
        c = shade(c, light(face, py, h) + 0.04)
        if face in SIDES and min(w, h) > 2:
            if py == 0:
                c = mix(c, steel, 0.4)
            elif px in (0, w - 1):
                c = shade(c, 0.76)
        return c
    return fn


def ragged(worn, part, bottom_row, seed, depth=2):
    """Tears the lower edge of what is painted on the sides of a part: some columns end a row or two early."""
    rnd = random.Random(seed)
    for face in SIDES:
        u, v, w, h = rects(part)[face]
        for px in range(w):
            for k in range(rnd.choice((0, 0, 1, 1, 2)[:2 + depth])):
                worn.clear(part, face, px, bottom_row - k)


SACK = (132, 112, 86)
SACK_D = (96, 80, 62)
ROPE = (164, 140, 96)
HOLE = (24, 20, 20)
STEEL = (142, 142, 150)
STEEL_D = (100, 100, 110)
STEEL_L = (184, 186, 194)
MAIL = (126, 126, 134)
MAIL_D = (84, 84, 94)
TABARD = (124, 46, 42)
TABARD_D = (90, 32, 32)
GOLD = (214, 184, 106)
GOLD_D = (158, 128, 62)
LEATHER = (98, 66, 42)
LEATHER_D = (68, 46, 32)
HOSE = (104, 88, 70)
HOSE_D = (78, 64, 52)
ROBE = (66, 46, 62)
ROBE_D = (44, 30, 42)
ROBE_L = (88, 62, 80)
BLACK = (34, 28, 32)
EMBER = (255, 150, 44)
LINEN = (226, 220, 204)
LINEN_D = (184, 176, 158)
LINEN_L = (244, 240, 228)
THREAD = (150, 40, 36)
THREAD_D = (108, 30, 30)
FELT = (124, 120, 112)
FELT_D = (90, 86, 82)
FELT_L = (152, 148, 138)
APRON = (120, 84, 54)
APRON_D = (84, 58, 40)
APRON_L = (150, 110, 72)
DUST = (208, 202, 188)
BONE = (198, 192, 166)
BONE_D = (140, 134, 112)
BONE_L = (230, 226, 204)
HIDE = (72, 80, 62)
HIDE_D = (48, 56, 44)
HIDE_L = (94, 104, 78)
WITHE = (118, 128, 72)


def prisoner():
    one, two = Worn(), Worn()
    # the hood: a sack over the head with two holes cut for the eyes, tied with rope at the neck
    one.fill('head', weave(SACK, SACK_D, 'hood'))
    for px in (1, 2, 5, 6):
        one.put('head', 'front', px, 3, HOLE)
    one.put('head', 'front', 2, 4, HOLE)
    one.put('head', 'front', 5, 4, HOLE)
    for face in SIDES:
        for px in range(8):
            one.put('head', face, px, 7, shade(ROPE, 1.0 if px % 2 else 0.8))
    for py in range(1, 7):                                  # the seam down the back
        one.put('head', 'back', 4, py, shade(SACK_D, 0.8))
    # the rags: a sleeveless tunic, torn along the bottom, a patch on the chest
    one.fill('body', weave(SACK, SACK_D, 'rags'))
    one.fill('arm', weave(SACK, SACK_D, 'sleeve'), faces=('top',) + SIDES, rows=(0, 3))
    ragged(one, 'arm', 3, 11)
    ragged(one, 'body', 11, 12, depth=3)
    one.art('body', 'front', ["pp.", "pqp", ".pp"], {'p': shade(SACK_D, 0.85), 'q': SACK_D}, 4, 3)
    for px in range(8):
        for face in ('front', 'back'):
            one.put('body', face, px, 7, shade(ROPE, 0.95 if px % 2 else 0.78))
    for px in range(4):
        for face in ('right', 'left'):
            one.put('body', face, px, 7, shade(ROPE, 0.8))
    # the wraps: strips of cloth round the feet and ankles
    for face in SIDES:
        for px in range(4):
            for py in range(7, 12):
                # strips wound one over another, every other one a little off to the side
                c = SACK if (py + (px // 2 if py % 2 else 0)) % 2 else SACK_D
                one.put('leg', face, px, py, shade(c, 1.05 - 0.05 * (py - 7)))
    one.fill('leg', lambda face, px, py, w, h: shade(SACK_D, 0.7), faces=('bottom',))
    # the breeches: down to the knee, in tatters
    two.fill('body', weave(SACK_D, shade(SACK_D, 0.75), 'waist'), faces=SIDES, rows=(8, 11))
    two.fill('leg', weave(SACK_D, shade(SACK_D, 0.75), 'breeches'), faces=SIDES, rows=(0, 7))
    ragged(two, 'leg', 7, 13, depth=3)
    return one, two


def guard():
    one, two = Worn(), Worn()
    # the kettle hat: an iron bowl with a rim, and a hood of mail round the neck that leaves the face open
    cap = lambda face, px, py, w, h: shade(STEEL if blotch('cap' + face, px, py, 2.5) < 0.7 else (140, 100, 76), light(face, py, h))
    one.fill('head', cap, faces=('top',))
    one.fill('head', cap, faces=SIDES, rows=(0, 2))
    one.fill('head', mail(MAIL, MAIL_D), faces=SIDES, rows=(3, 7))
    for face in SIDES:
        for px in range(8):
            one.put('head', face, px, 2, shade(STEEL_L if px % 3 else STEEL, 1.0))
            one.put('head', face, px, 0, shade(STEEL_D, 1.0))
    for py in range(3, 7):                                  # the face
        for px in range(1, 7):
            one.clear('head', 'front', px, py)
    one.fill('hat', lambda face, px, py, w, h: shade(STEEL_D if (px + py) % 4 == 0 else STEEL, 1.0), faces=SIDES, rows=(2, 2))
    # the hauberk: mail to the elbows under a red tabard with the stockade's key
    one.fill('body', mail(MAIL, MAIL_D))
    one.fill('arm', mail(MAIL, MAIL_D), faces=('top',) + SIDES, rows=(0, 7))
    for face in ('front', 'back'):
        for py in range(0, 12):
            for px in range(2, 6):
                c = TABARD if blotch('tab' + face, px, py, 3.0) < 0.75 else TABARD_D
                one.put('body', face, px, py, shade(c, 1.08 - 0.2 * py / 11))
    one.art('body', 'front', [".GG.", "G..G", "G..G", ".GG.", ".G..", ".GG.", ".G..", ".GG."], {'G': GOLD}, 2, 0)
    for face, w in (('front', 8), ('back', 8), ('right', 4), ('left', 4)):
        for px in range(w):
            one.put('body', face, px, 9, shade(LEATHER, 1.0 if px % 3 else 1.25))
    one.put('body', 'front', 4, 9, GOLD_D)
    for face in SIDES:                                      # a leather cuff at the elbow
        for px in range(4):
            one.put('arm', face, px, 7, LEATHER_D)
    # the boots: leather to the shin, an iron cap on the toe
    boot = lambda face, px, py, w, h: shade(LEATHER if blotch('boot' + face, px, py, 2.0) < 0.6 else LEATHER_D, light(face, py, h) + 0.05)
    one.fill('leg', boot, faces=SIDES, rows=(7, 11))
    one.fill('leg', lambda face, px, py, w, h: shade(LEATHER_D, 0.7), faces=('bottom',))
    for face in SIDES:
        for px in range(4):
            one.put('leg', face, px, 7, LEATHER_D)
    for px in range(4):
        one.put('leg', 'front', px, 11, STEEL)
        one.put('leg', 'front', px, 10, STEEL_D if px in (0, 3) else STEEL_L)
    # the leggings: a skirt of mail at the hips, padded hose below, a strap under the knee
    two.fill('body', mail(MAIL, MAIL_D), faces=SIDES, rows=(8, 11))
    two.fill('leg', mail(MAIL, MAIL_D), faces=SIDES, rows=(0, 2))
    two.fill('leg', weave(HOSE, HOSE_D, 'hose', 2.0, 0.6), faces=SIDES, rows=(3, 9))
    for face in SIDES:
        for px in range(4):
            two.put('leg', face, px, 6, LEATHER_D)
    return one, two


def rust():
    one, two = Worn(), Worn()
    # the helm: a closed barrel with a slit to see through, holes to breathe through and a ridge over the top
    one.fill('head', plate('helm'))
    for px in range(1, 7):
        one.put('head', 'front', px, 3, HOLE)
    one.put('head', 'front', 2, 3, EMBER)
    one.put('head', 'front', 5, 3, EMBER)
    for (px, py) in ((2, 5), (5, 5), (2, 6), (5, 6), (3, 6), (4, 5)):
        one.put('head', 'front', px, py, (40, 34, 34))
    for py in range(0, 8):
        one.put('head', 'front', 3 + (py == 3) * 0, py, shade((150, 148, 150), 0.9)) if py not in (3,) else None
    for pz in range(8):
        one.put('head', 'top', 3, pz, (150, 148, 150))
        one.put('head', 'top', 4, pz, (112, 110, 114))
    # the cuirass: a breastplate with a ridge, broad plates on the shoulders, gauntlets
    one.fill('body', plate('cuirass'))
    for py in range(1, 9):
        one.put('body', 'front', 3, py, (132, 128, 130))
        one.put('body', 'front', 4, py, (70, 62, 62))
    for face, w in (('front', 8), ('back', 8), ('right', 4), ('left', 4)):
        for px in range(w):
            one.put('body', face, px, 9, (52, 46, 46))
    one.fill('arm', plate('pauldron'), faces=('top',) + SIDES, rows=(0, 4))
    one.fill('arm', plate('gauntlet'), faces=SIDES + ('bottom',), rows=(7, 11))
    for face in SIDES:
        for px in range(4):
            one.put('arm', face, px, 4, (52, 46, 46))
            one.put('arm', face, px, 7, (150, 148, 150))
            for py in (5, 6):                               # mail at the elbow, between the plates
                one.put('arm', face, px, py, shade(MAIL_D if (px + py) % 2 else (60, 58, 64), 1.0))
    # the sabatons: plate from the shin down
    one.fill('leg', plate('sabaton'), faces=SIDES + ('bottom',), rows=(6, 11))
    for face in SIDES:
        for px in range(4):
            one.put('leg', face, px, 9, (52, 46, 46))
    # the tassets: plates over the hips and thighs, a cop on the knee
    two.fill('body', plate('fauld'), faces=SIDES, rows=(7, 11))
    two.fill('leg', plate('cuisse'), faces=SIDES + ('top',), rows=(0, 8))
    for face in SIDES:
        for px in range(4):
            two.put('leg', face, px, 5, (52, 46, 46))
            two.put('leg', face, px, 6, (150, 148, 150) if px in (1, 2) else (112, 110, 114))
    return one, two


def chaplain():
    one, two = Worn(), Worn()
    cloth = weave(ROBE, ROBE_D, 'robe', 3.0, 0.7)
    # the mitre: a dark cap with a gold band and a cross on the brow
    one.fill('head', cloth, faces=('top',))
    one.fill('head', cloth, faces=SIDES, rows=(0, 3))
    for face in SIDES:
        for px in range(8):
            one.put('head', face, px, 3, GOLD if px % 2 else GOLD_D)
    one.art('head', 'front', [".G.", "GGG", ".G."], {'G': GOLD}, 3 - 0, 0)
    one.fill('hat', cloth, faces=('top',))
    one.fill('hat', cloth, faces=SIDES, rows=(0, 1))
    # the cassock: dark cloth with a gold stripe and buttons down the front, sleeves to the wrist with a gold cuff
    one.fill('body', cloth)
    for py in range(0, 12):
        one.put('body', 'front', 3, py, shade(GOLD_D, 1.0 - 0.02 * py))
        one.put('body', 'front', 4, py, shade(GOLD if py % 3 == 1 else GOLD_D, 1.0 - 0.02 * py))
    for face, w in (('front', 8), ('back', 8), ('right', 4), ('left', 4)):
        for px in range(w):
            one.put('body', face, px, 8, shade(BLACK, 1.0))
    one.put('body', 'front', 3, 8, GOLD)
    one.put('body', 'front', 4, 8, GOLD)
    for px in range(8):                                    # a pale collar
        one.put('body', 'front', px, 0, (172, 164, 150) if 2 <= px <= 5 else shade(ROBE_L, 1.0))
    one.fill('arm', cloth, faces=('top',) + SIDES, rows=(0, 10))
    for face in SIDES:
        for px in range(4):
            one.put('arm', face, px, 10, GOLD if px % 2 else GOLD_D)
    # the shoes: plain and black
    one.fill('leg', lambda face, px, py, w, h: shade(BLACK, 1.2 if py == 9 else 1.0), faces=SIDES + ('bottom',), rows=(9, 11))
    # the skirt of the cassock: to the ankle, a gold hem
    two.fill('body', cloth, faces=SIDES, rows=(8, 11))
    two.fill('leg', cloth, faces=SIDES, rows=(0, 9))
    for face in SIDES:
        for px in range(4):
            two.put('leg', face, px, 9, GOLD if px % 2 else GOLD_D)
            if px == 1 and face in ('front', 'back'):
                for py in range(0, 9):
                    two.put('leg', face, px, py, shade(ROBE_D, 0.85))
    return one, two


def vigil():
    """The white of the Unsleeping: bleached linen, and one red thread run through all of it."""
    one, two = Worn(), Worn()
    cloth = weave(LINEN, LINEN_D, 'vigil', 3.0, 0.55)
    # the cowl: a cloth over the head and down on the shoulders, the face left open; the red thread round the brow
    one.fill('head', cloth)
    for py in range(2, 7):
        for px in range(1, 7):
            one.clear('head', 'front', px, py)
    for face in SIDES:
        for px in range(8):
            one.put('head', face, px, 1, THREAD if px % 2 else THREAD_D)
    one.fill('hat', cloth, faces=('top',))
    one.fill('hat', cloth, faces=('right', 'left', 'back'), rows=(0, 7))
    one.fill('hat', cloth, faces=('front',), rows=(0, 1))
    for face in SIDES:
        for px in range(8):
            one.put('hat', face, px, 1, THREAD if px % 2 else THREAD_D)
    for py in range(2, 8):                                  # the folds beside the face
        one.put('hat', 'front', 0, py, shade(LINEN_D, 0.95))
        one.put('hat', 'front', 7, py, shade(LINEN_D, 0.95))
    # the shift: to the wrists, the thread down the breast, a cord of twisted linen for a belt
    one.fill('body', cloth)
    for py in range(0, 12):
        if py % 3 != 2:
            one.put('body', 'front', 4, py, shade(THREAD, 1.0 - 0.02 * py))
    for face, w in (('front', 8), ('back', 8), ('right', 4), ('left', 4)):
        for px in range(w):
            one.put('body', face, px, 8, shade(LINEN_D, 0.82 if px % 2 else 0.7))
    one.put('body', 'front', 2, 9, shade(LINEN_D, 0.75))     # the ends of the cord
    one.put('body', 'front', 2, 10, shade(LINEN_D, 0.75))
    one.fill('arm', cloth, faces=('top',) + SIDES, rows=(0, 10))
    for face in SIDES:
        for px in range(4):
            one.put('arm', face, px, 10, THREAD if px % 2 else THREAD_D)
    # the wraps: strips of the same linen round the feet
    for face in SIDES:
        for px in range(4):
            for py in range(8, 12):
                c = LINEN if (py + (px // 2 if py % 2 else 0)) % 2 else LINEN_D
                one.put('leg', face, px, py, shade(c, 1.02 - 0.05 * (py - 8)))
    one.fill('leg', lambda face, px, py, w, h: shade(LINEN_D, 0.7), faces=('bottom',))
    # the skirt of the shift: to the ankle, the thread along the hem, grey with the forest floor below it
    two.fill('body', cloth, faces=SIDES, rows=(8, 11))
    two.fill('leg', cloth, faces=SIDES, rows=(0, 9))
    for face in SIDES:
        for px in range(4):
            two.put('leg', face, px, 8, THREAD if px % 2 else THREAD_D)
            two.put('leg', face, px, 9, shade(LINEN_D, 0.8))
    return one, two


def dusty(fn, seed, amount=0.16):
    """Stone dust settled on whatever `fn` paints: more of it on what faces up."""
    def out(face, px, py, w, h):
        c = fn(face, px, py, w, h)
        if c is not None and blotch(f'dust{seed}{face}', px, py, 1.3) < (amount * 2.2 if face == 'top' else amount):
            c = mix(c, DUST, 0.55)
        return c
    return out


def mason():
    """A stonecutter's working gear: quilted felt against the chips, and over it the leather a man splits rock in - a
    cap with a flap down the neck, an apron to the knee, cuffs, knee-pads, nailed boots. All of it grey with the pit's dust."""
    one, two = Worn(), Worn()
    felt = dusty(weave(FELT, FELT_D, 'mason', 2.0, 0.6), 'felt')
    hide = dusty(lambda face, px, py, w, h: shade(APRON if blotch('apron' + face, px, py, 2.4) < 0.66 else APRON_D, light(face, py, h) + 0.04), 'hide', 0.12)
    # the cap: felt to the brow, a leather band, and a flap of leather over the neck and ears
    one.fill('head', felt, faces=('top',))
    one.fill('head', felt, faces=SIDES, rows=(0, 2))
    for face in SIDES:
        for px in range(8):
            one.put('head', face, px, 2, shade(APRON_D if px % 3 else APRON, 1.0))
    one.fill('head', hide, faces=('right', 'left', 'back'), rows=(3, 7))
    one.fill('hat', felt, faces=('top',))
    one.fill('hat', felt, faces=SIDES, rows=(0, 1))
    # the apron: a quilted coat to the wrists; over the front of it leather from the breast down, on two straps
    one.fill('body', felt)
    for py in range(0, 12):                                 # the quilting: seams down the coat
        for face, w in (('back', 8), ('right', 4), ('left', 4)):
            for px in range(w):
                if px % 3 == 1:
                    one.put('body', face, px, py, shade(FELT_D, 0.92 - 0.01 * py))
    for py in range(2, 12):
        for px in range(1, 7):
            one.put('body', 'front', px, py, hide('front', px, py, 8, 12))
    for px in (1, 6):                                       # the straps
        for py in (0, 1):
            one.put('body', 'front', px, py, APRON_D)
        for py in range(0, 5):
            one.put('body', 'back', px, py, APRON_D)
    for face, w in (('back', 8), ('right', 4), ('left', 4)):   # the apron's string, round the waist
        for px in range(w):
            one.put('body', face, px, 7, shade(APRON_D, 0.95 if px % 2 else 0.8))
    for px in range(1, 7):                                  # worn pale where the stone rests against it
        if px in (2, 3, 4):
            one.put('body', 'front', px, 6, APRON_L)
        one.put('body', 'front', px, 11, shade(APRON_D, 0.8))
    one.put('body', 'front', 5, 4, shade(APRON_D, 0.7))      # a pocket for the wedges
    one.put('body', 'front', 5, 5, shade(APRON_D, 0.7))
    one.put('body', 'front', 4, 4, shade(APRON_D, 0.7))
    one.fill('arm', felt, faces=('top',) + SIDES, rows=(0, 7))
    one.fill('arm', hide, faces=SIDES + ('bottom',), rows=(8, 11))     # cuffs to the knuckles
    for face in SIDES:
        for px in range(4):
            one.put('arm', face, px, 8, shade(APRON_D, 0.8))
            if px % 3 == 1:
                for py in range(0, 8):
                    one.put('arm', face, px, py, shade(FELT_D, 0.92))
    # the boots: leather to the shin, an iron cap on the toe, nails in the sole
    one.fill('leg', hide, faces=SIDES, rows=(6, 11))
    one.fill('leg', lambda face, px, py, w, h: STEEL_D if (px + py) % 2 else shade(APRON_D, 0.7), faces=('bottom',))
    for face in SIDES:
        for px in range(4):
            one.put('leg', face, px, 6, shade(APRON_D, 0.8))
            one.put('leg', face, px, 11, shade(APRON_D, 0.62))
    for px in range(4):
        one.put('leg', 'front', px, 10, STEEL if px in (1, 2) else STEEL_D)
    # the breeches: quilted to below the knee, a pad of leather on each knee
    two.fill('body', felt, faces=SIDES, rows=(8, 11))
    two.fill('leg', felt, faces=SIDES, rows=(0, 7))
    for px in range(4):
        for py in (4, 5):
            two.put('leg', 'front', px, py, shade(APRON if px in (1, 2) else APRON_D, 1.0 if py == 4 else 0.85))
    for face in SIDES:
        for px in range(4):
            two.put('leg', face, px, 7, shade(FELT_D, 0.8))
    return one, two


def scaled(seed):
    """Scales of bone sewn in rows, each row lapping the next: lit on its upper edge, dark where it laps."""
    def fn(face, px, py, w, h):
        row = py // 2
        if py % 2 == 0:                                      # the upper edge of a row of scales, lit
            c = BONE_L
        else:                                                # their lower ends, round, with the dark of the lap between them
            c = BONE if (px + row) % 2 == 0 else BONE_D
        if blotch(f'{seed}{face}', px, py, 1.5) > 0.86:
            c = shade(c, 0.86)
        return shade(c, light(face, py, h))
    return fn


def scale():
    """What the Mire's fishers wore, as far as the pike let them: a coat of the fish's own hide with its bone scales sewn
    on in rows, a hood of the same, and waders to the thigh bound with withe."""
    one, two = Worn(), Worn()
    hide = lambda face, px, py, w, h: shade(HIDE if blotch('fish' + face, px, py, 2.2) < 0.68 else HIDE_D, light(face, py, h) + 0.03)
    # the hood: hide over the head and neck, the face open, a row of scales over the brow and down the crown
    one.fill('head', hide)
    for py in range(2, 7):
        for px in range(1, 7):
            one.clear('head', 'front', px, py)
    one.fill('head', scaled('crown'), faces=('top',))
    for face in SIDES:
        for px in range(8):
            for py in (0, 1):
                one.put('head', face, px, py, scaled('brow')(face, px, py, 8, 8))
    one.fill('hat', hide, faces=('right', 'left', 'back'), rows=(4, 7))
    for py in range(2, 8):
        one.put('hat', 'front', 0, py, HIDE_D)
        one.put('hat', 'front', 7, py, HIDE_D)
    # the coat: scales over the breast, the back and the shoulders; bare hide at the sides, where the arms rub
    one.fill('body', hide)
    one.fill('body', scaled('coat'), faces=('front', 'back'), rows=(0, 8))
    one.fill('body', hide, faces=('right', 'left'))
    for face, w in (('front', 8), ('back', 8), ('right', 4), ('left', 4)):
        for px in range(w):
            one.put('body', face, px, 9, shade(WITHE, 0.95 if px % 2 else 0.75))    # a belt plaited of withe
    one.fill('arm', scaled('shoulder'), faces=('top',) + SIDES, rows=(0, 3))
    one.fill('arm', hide, faces=SIDES, rows=(4, 8))
    for face in SIDES:
        for px in range(4):
            one.put('arm', face, px, 8, shade(WITHE, 0.9 if px % 2 else 0.72))
    # the waders: hide to the knee, bound twice with withe, the foot stiff with pitch
    one.fill('leg', hide, faces=SIDES, rows=(4, 11))
    one.fill('leg', lambda face, px, py, w, h: (30, 30, 28), faces=('bottom',))
    for face in SIDES:
        for px in range(4):
            one.put('leg', face, px, 4, shade(WITHE, 0.9 if px % 2 else 0.72))
            one.put('leg', face, px, 8, shade(WITHE, 0.85 if px % 2 else 0.68))
            one.put('leg', face, px, 11, (36, 36, 32))
            one.put('leg', face, px, 10, shade(HIDE_D, 0.8))
    # the leggings: hide, a skirt of scales over the hips and the thighs
    two.fill('body', scaled('skirt'), faces=SIDES, rows=(8, 11))
    two.fill('leg', hide, faces=SIDES, rows=(0, 9))
    two.fill('leg', scaled('thigh'), faces=('front', 'right', 'left'), rows=(0, 3))
    return one, two


OIL, OIL_D, OIL_L = (196, 164, 72), (146, 116, 50), (226, 198, 104)
PITCH, PITCH_L = (44, 40, 40), (72, 66, 62)
HEMP = (150, 132, 98)


def oilskin():
    """A seaman's foul-weather gear: sailcloth boiled in oil until it is the colour of old butter - a sou'wester with its
    brim long behind, a smock that goes on over the head, trousers of the same, and sea boots tarred black to the thigh."""
    one, two = Worn(), Worn()
    cloth = lambda face, px, py, w, h: shade(OIL if blotch('oil' + face, px, py, 2.4) < 0.7 else OIL_D, light(face, py, h) + 0.03)
    dull = lambda face, px, py, w, h: shade(OIL_D if blotch('oilt' + face, px, py, 2.4) < 0.7 else shade(OIL_D, 0.84), light(face, py, h) + 0.03)
    tar = lambda face, px, py, w, h: shade(PITCH if blotch('pitch' + face, px, py, 2.0) < 0.75 else PITCH_L, light(face, py, h) + 0.05)
    # the sou'wester: close on the crown, open before, the seams of its gores showing; over it (the outer layer) the brim:
    # turned up over the brow, and down over the ears and the neck
    one.fill('head', cloth, faces=('top',) + SIDES, rows=(0, 3))
    for face in ('top',) + SIDES:
        for px in range(8):
            if px in (3, 4):
                continue
            if face == 'top':
                one.put('head', face, px, 3, shade(OIL_D, 1.0))
                one.put('head', face, px, 4, shade(OIL_D, 1.0))
    for py in range(8):
        one.put('head', 'top', 3, py, shade(OIL_D, 1.05))
        one.put('head', 'top', 4, py, shade(OIL_L, 1.05))
    for py in (2, 3):
        for px in range(1, 7):
            one.clear('head', 'front', px, py)
    one.fill('hat', cloth, faces=('right', 'left', 'back'), rows=(2, 6))
    one.fill('hat', cloth, faces=('front',), rows=(1, 2))
    for px in range(8):
        one.put('hat', 'front', px, 2, shade(OIL_D, 0.92))             # the brim's turned edge
        one.put('hat', 'back', px, 6, shade(OIL_D, 0.86))
        one.put('hat', 'back', px, 7, shade(OIL_D, 0.74))
    for face in ('right', 'left'):
        for px in range(8):
            one.put('hat', face, px, 6, shade(OIL_D, 0.86))
        for px in (2, 3, 4, 5):                                        # the flap over the ear, and the tie under the chin
            one.put('hat', face, px, 7, shade(OIL_D, 0.8))
    for px in (0, 7):
        for py in (3, 4, 5, 6, 7):
            one.put('hat', 'front', px, py, shade(HEMP, 0.9 if py % 2 else 0.74))
    # the smock: one piece, a yoke doubled over the shoulders, a flap buttoned across the throat, a rope for a belt
    one.fill('body', cloth)
    for face, w in (('front', 8), ('back', 8), ('right', 4), ('left', 4)):
        for px in range(w):
            one.put('body', face, px, 3, shade(OIL_D, 0.95))           # the edge of the yoke
            one.put('body', face, px, 9, shade(HEMP, 0.95 if px % 2 else 0.72))
            one.put('body', face, px, 11, shade(OIL_D, 0.8))
    for px, py in ((3, 0), (4, 0), (3, 1), (4, 1), (4, 2)):
        one.put('body', 'front', px, py, shade(OIL_D, 0.9))
    one.put('body', 'front', 5, 1, PITCH_L)                             # a horn button
    one.put('body', 'front', 6, 10, shade(HEMP, 0.8))                   # the end of the rope
    one.put('body', 'front', 6, 11, shade(HEMP, 0.7))
    one.fill('arm', cloth, faces=('top',) + SIDES, rows=(0, 9))
    for face in SIDES:
        for px in range(4):
            one.put('arm', face, px, 3, shade(OIL_D, 0.95))
            one.put('arm', face, px, 9, shade(HEMP, 0.9 if px % 2 else 0.72))    # the cuff, tied against the water
    # the sea boots: tarred leather to the top of the thigh, turned down in a cuff, the sole thick
    one.fill('leg', tar, faces=SIDES, rows=(2, 11))
    one.fill('leg', lambda face, px, py, w, h: (26, 24, 24), faces=('bottom',))
    for face in SIDES:
        for px in range(4):
            one.put('leg', face, px, 2, PITCH_L)
            one.put('leg', face, px, 3, shade(PITCH_L, 0.85))
            one.put('leg', face, px, 11, (28, 26, 26))
    for py in (5, 8):
        one.put('leg', 'front', 1, py, shade(PITCH_L, 1.15))            # the wet on them
    # the trousers: the same cloth, darker with wear, wide, the seat doubled
    two.fill('body', dull, faces=SIDES, rows=(7, 11))
    two.fill('leg', dull, faces=SIDES, rows=(0, 8))
    for face in SIDES:
        for px in range(4):
            two.put('leg', face, px, 4, shade(OIL_D, 0.74))             # a patch across the knee
    for px in range(8):
        two.put('body', 'back', px, 9, shade(OIL_D, 0.78))
    return one, two


WORN = {'prisoner': prisoner, 'guard': guard, 'rust': rust, 'chaplain': chaplain, 'vigil': vigil, 'mason': mason, 'scale': scale, 'oilskin': oilskin}

# ---------------------------------------------------------------------------------------------------- the icons

o = (30, 26, 28)

HELMETS = {
    'prisoner': ([
        "................",
        "................",
        "....oooooooo....",
        "...oAAAAABBBo...",
        "..oAAABBBBBBCo..",
        "..oABBBBBBBBCo..",
        "..oABooBBooBCo..",
        "..oABooBBooBCo..",
        "..oBBBBBBBBBCo..",
        "..oBBBBCBBBBCo..",
        "..oBBBCBBBBCCo..",
        "..oRrRrRrRrRro..",
        "...oBBCBBCBCo...",
        "....oo.oo.oo....",
        "................",
        "................"], dict(A=shade(SACK, 1.2), B=SACK, C=SACK_D, R=ROPE, r=shade(ROPE, 0.75))),
    'guard': ([
        "................",
        "................",
        "......oooo......",
        "....ooLLSSoo....",
        "...oLLSSSSSDo...",
        "...oLSSSSSDDo...",
        ".ooooooooooooo..",
        "oLLSSSSSSSSDDDo.",
        ".oooooooooooooo.",
        "..oMmo....oMmo..",
        "..omMo....omMo..",
        "..oMmMo..oMmMo..",
        "...omMmoomMmo...",
        "....oooooooo....",
        "................",
        "................"], dict(L=STEEL_L, S=STEEL, D=STEEL_D, M=MAIL, m=MAIL_D)),
    'rust': ([
        "................",
        "................",
        "....oooooooo....",
        "...oLIIRRIIDo...",
        "..oLIIRRRIIIDo..",
        "..oIIRRIIIIRDo..",
        "..oIoooooooIDo..",
        "..oIoEooooEIDo..",
        "..oIIIILDIIRDo..",
        "..oIRIILDIIRDo..",
        "..oIRRoLDoIIDo..",
        "..oIIRILDIoIDo..",
        "..oDIIILDIIDDo..",
        "...oooooooooo...",
        "................",
        "................"], dict(L=(150, 148, 150), I=(96, 88, 88), R=(140, 84, 50), D=(62, 56, 58), E=EMBER)),
    'chaplain': ([
        "................",
        ".......oo.......",
        "......oLBo......",
        ".....oLBBDo.....",
        "....oLBGBBDo....",
        "....oBGGGBDo....",
        "...oLBBGBBBDo...",
        "...oBBBGBBBDo...",
        "...oBBBBBBBDo...",
        "..oGgGgGgGgGgo..",
        "..oBBBBBBBBBDo..",
        "..oBBo....oBDo..",
        "..oooo....oooo..",
        "................",
        "................",
        "................"], dict(L=ROBE_L, B=ROBE, D=ROBE_D, G=GOLD, g=GOLD_D)),
    'vigil': ([
        "................",
        "................",
        "....oooooooo....",
        "...oLLLLBBBBo...",
        "..oLLBBBBBBBDo..",
        "..oNnNnNnNnNno..",
        "..oLBooooooBDo..",
        "..oLBo....oBDo..",
        "..oBBo....oBDo..",
        "..oBBo....oBDo..",
        "..oBBBo..oBBDo..",
        ".oLBBBDooBBBDDo.",
        ".oBBBDo..oBBDDo.",
        "..oooo....oooo..",
        "................",
        "................"], dict(L=LINEN_L, B=LINEN, D=LINEN_D, N=THREAD, n=THREAD_D)),
    'mason': ([
        "................",
        "................",
        "................",
        ".....oooooo.....",
        "....oAAABBBo....",
        "...oAAWBBBBCo...",
        "...oABBBWBBCo...",
        "..oABBBBBBBBCo..",
        "..olLlLlLlLlLo..",
        "..oLlo....oLlo..",
        "..oLlo....oLlo..",
        "..oLlo....ollo..",
        "...oo......oo...",
        "................",
        "................",
        "................"], dict(A=FELT_L, B=FELT, C=FELT_D, L=APRON, l=APRON_D, W=DUST)),
    'scale': ([
        "................",
        "................",
        "....oooooooo....",
        "...oLLLLLLLLo...",
        "..oBDBDBDBDBDo..",
        "..oLLLLLLLLLLo..",
        "..oHHooooooHho..",
        "..oHHo....oHho..",
        "..oHHo....oHho..",
        "..oHho....oHho..",
        "..oHhho..oHhho..",
        ".oHHhhhoohHhhho.",
        ".oHhhho..ohhhho.",
        "..oooo....oooo..",
        "................",
        "................"], dict(L=BONE_L, B=BONE, D=BONE_D, H=HIDE, h=HIDE_D)),
    'oilskin': ([
        "................",
        "................",
        "......oooo......",
        "....ooLLBBoo....",
        "...oLLBBDBBBo...",
        "...oLBBBDBBDo...",
        "..oLBBBBDBBBDo..",
        ".ooooooooooooo..",
        "oLLBBBBBBBBBDDo.",
        ".ooBBo....oBDDo.",
        "..oBDo....oDDDoo",
        "..oHo......oDDDo",
        "..oho.......oooo",
        "...o............",
        "................",
        "................"], dict(L=OIL_L, B=OIL, D=OIL_D, H=HEMP, h=shade(HEMP, 0.74))),
}

CHEST = [
    "................",
    "..ooo......ooo..",
    ".oAABo....oBAAo.",
    ".oABBBooooBBBAo.",
    ".oABBBBBBBBBBCo.",
    ".oBBoBBBBBBoBCo.",
    "..oooBBBBBBooo..",
    "...oBBBBBBBCo...",
    "...oBBBBBBBCo...",
    "...oBBBBBBBCo...",
    "...oBBBBBBBCo...",
    "...oBBBBBBCCo...",
    "...oCCCCCCCCo...",
    "...oooooooooo...",
    "................",
    "................"]
LEGS = [
    "................",
    "...oooooooooo...",
    "...oAAAAAAABo...",
    "...oABBBBBBCo...",
    "...oBBBBBBBCo...",
    "...oBBBooBBCo...",
    "...oBBo..oBCo...",
    "...oBBo..oBCo...",
    "...oBBo..oBCo...",
    "...oBBo..oBCo...",
    "...oBBo..oBCo...",
    "...oBCo..oBCo...",
    "...oCCo..oCCo...",
    "...oooo..oooo...",
    "................",
    "................"]
BOOTS = [
    "................",
    "................",
    "................",
    "................",
    "..oooo....oooo..",
    "..oABo....oABo..",
    "..oABo....oABo..",
    "..oBBo....oBBo..",
    "..oBBo....oBCo..",
    "..oBBoo..ooBCo..",
    ".oABBBo..oBBBCo.",
    ".oBBBCo..oBBCCo.",
    ".oooooo..oooooo.",
    "................",
    "................",
    "................"]

# overlays: (x, y, rows) drawn over the shape of a piece, in the suit's palette
OVER = {
    'prisoner_rags': [(5, 6, ["pp", "pq"]), (3, 9, ["RrRrRrRrR"]), (4, 12, ["o.C.oo.C"]), (2, 4, ["o"]), (13, 4, ["o"])],
    'prisoner_breeches': [(4, 11, ["o.o"]), (10, 12, ["o.o"]), (5, 12, ["o"]), (4, 4, ["p"]), (9, 8, ["p"])],
    'prisoner_wraps': [(3, 6, ["RR"]), (11, 6, ["RR"]), (2, 10, ["RrR"]), (10, 10, ["rRr"]), (3, 8, ["rr"]), (11, 8, ["rr"])],
    'guard_hauberk': [(6, 4, ["TTTT", "TGGT", "GttG", "TGGT", "TGtT", "TGGT", "TGtT", "TGGt"]), (3, 10, ["lllllllll"])],
    'guard_leggings': [(4, 2, ["MmMmMmM", "mMmMmMm"]), (4, 9, ["ll"]), (10, 9, ["ll"])],
    'guard_boots': [(2, 11, ["SS"]), (11, 11, ["SS"]), (3, 5, ["l"]), (11, 5, ["l"])],
    'rust_cuirass': [(7, 4, ["LD", "LD", "LD", "LD", "LD", "LD"]), (4, 7, ["R"]), (10, 9, ["RR"]), (2, 2, ["R"]), (5, 10, ["R"]), (12, 3, ["R"])],
    'rust_tassets': [(4, 2, ["RA"]), (4, 8, ["LL"]), (10, 8, ["LL"]), (5, 5, ["R"]), (10, 4, ["R"]), (10, 10, ["R"])],
    'rust_sabatons': [(3, 7, ["R"]), (11, 6, ["R"]), (2, 10, ["LL"]), (10, 10, ["LL"])],
    'chaplain_cassock': [(7, 4, ["Gg", "gG", "Gg", "gG", "Gg", "gG", "Gg", "gG"]), (2, 5, ["G"]), (13, 5, ["G"]), (6, 2, ["W"]), (9, 2, ["W"])],
    'chaplain_skirt': [(4, 12, ["Gg"]), (10, 12, ["gG"]), (4, 2, ["GgGgGgGg"])],
    'chaplain_shoes': [],
    'vigil_shift': [(7, 4, ["N", "N", ".", "N", "N", ".", "N", "N"]), (3, 9, ["CRCRCRCRC"]), (5, 10, ["R"]), (2, 5, ["N"]), (13, 5, ["N"])],
    'vigil_skirt': [(4, 11, ["Nn"]), (10, 11, ["nN"]), (4, 2, ["CRCRCRCR"])],
    'vigil_wraps': [(3, 6, ["CC"]), (11, 6, ["CC"]), (2, 10, ["CAC"]), (10, 10, ["ACA"]), (3, 8, ["CC"]), (11, 8, ["CC"])],
    'mason_apron': [(5, 5, ["LLLLLL", "LLLLLL", "LLPPLL", "LLLLLL", "LLLLlL", "lLLLLl", "LLLLLL"]), (5, 12, ["llllll"]), (5, 3, ["l"]), (10, 3, ["l"]),
                    (5, 4, ["l"]), (10, 4, ["l"]), (3, 10, ["W"]), (12, 8, ["W"]), (2, 3, ["W"])],
    'mason_breeches': [(4, 2, ["lLlLlLl"]), (4, 8, ["LL"]), (10, 8, ["LL"]), (4, 9, ["ll"]), (10, 9, ["ll"]), (5, 5, ["W"]), (10, 11, ["W"])],
    'mason_boots': [(2, 11, ["SS"]), (11, 11, ["SS"]), (3, 5, ["C"]), (11, 5, ["C"]), (3, 8, ["W"]), (12, 9, ["W"])],
    'scale_coat': [(4, 5, ["LLLLLLL", "EDEDEDE", "LLLLLLL", "DEDEDED", "LLLLLLL", "EDEDEDE"]), (4, 11, ["VvVvVvV"]),
                   (2, 3, ["LL"]), (12, 3, ["LL"]), (2, 4, ["ED"]), (12, 4, ["DE"])],
    'scale_leggings': [(4, 2, ["LLLLLLL", "EDEDEDE", "LLLLLLL", "DED.DED"]), (4, 6, ["LL"]), (10, 6, ["LL"]), (4, 7, ["ED"]), (10, 7, ["DE"])],
    'scale_waders': [(3, 5, ["VV"]), (11, 5, ["VV"]), (3, 8, ["Vv"]), (11, 8, ["vV"]), (2, 11, ["PPP"]), (10, 11, ["PPP"])],
    'oilskin_smock': [(4, 5, ["CCCCCCC"]), (7, 3, ["C", "P"]), (3, 10, ["HhHhHhHhH"]), (10, 11, ["H", "h"]), (2, 5, ["h"]), (13, 5, ["h"])],
    'oilskin_trousers': [(4, 2, ["hHhHhHh"]), (4, 8, ["CC"]), (10, 8, ["CC"]), (7, 3, ["C"])],
    'oilskin_boots': [(3, 5, ["LL"]), (11, 5, ["LL"]), (3, 6, ["ll"]), (11, 6, ["ll"]), (2, 12, ["PPP"]), (10, 12, ["PPP"]), (3, 8, ["L"]), (12, 9, ["L"])],
}

PALETTES = {
    'prisoner': dict(A=shade(SACK, 1.2), B=SACK, C=SACK_D, p=shade(SACK_D, 0.85), q=SACK_D, R=ROPE, r=shade(ROPE, 0.75)),
    'guard': dict(A=(160, 160, 168), B=MAIL, C=MAIL_D, T=TABARD, t=TABARD_D, G=GOLD, l=LEATHER, M=(150, 150, 158), m=MAIL_D, S=STEEL_L),
    'rust': dict(A=(128, 120, 120), B=(96, 88, 88), C=(62, 56, 58), R=(140, 84, 50), L=(150, 148, 150), D=(62, 56, 58)),
    'chaplain': dict(A=ROBE_L, B=ROBE, C=ROBE_D, G=GOLD, g=GOLD_D, W=(172, 164, 150)),
    'vigil': dict(A=LINEN_L, B=LINEN, C=LINEN_D, N=THREAD, n=THREAD_D, R=shade(LINEN_D, 0.78)),
    'mason': dict(A=FELT_L, B=FELT, C=FELT_D, L=APRON, l=APRON_D, P=APRON_L, W=DUST, S=STEEL),
    'scale': dict(A=HIDE_L, B=HIDE, C=HIDE_D, L=BONE_L, E=BONE, D=BONE_D, V=WITHE, v=shade(WITHE, 0.72), P=(36, 36, 32)),
    'oilskin': dict(A=OIL_L, B=OIL, C=OIL_D, H=HEMP, h=shade(HEMP, 0.74), P=(26, 24, 24), L=PITCH_L, l=PITCH),
}
# pieces whose base colours differ from the suit's
PIECE_PALETTE = {
    'prisoner_breeches': dict(A=SACK, B=SACK_D, C=shade(SACK_D, 0.75)),
    'guard_leggings': dict(A=shade(HOSE, 1.15), B=HOSE, C=HOSE_D),
    'guard_boots': dict(A=shade(LEATHER, 1.25), B=LEATHER, C=LEATHER_D),
    'chaplain_shoes': dict(A=(64, 56, 62), B=BLACK, C=(22, 18, 22)),
    'mason_boots': dict(A=APRON_L, B=APRON, C=APRON_D),
    'oilskin_trousers': dict(A=OIL, B=OIL_D, C=shade(OIL_D, 0.78)),
    'oilskin_boots': dict(A=PITCH_L, B=PITCH, C=(28, 26, 26)),
}


MATERIALS = {
    # what the dead leave behind: a strip of sackcloth, a handful of rusted iron
    'tattered_cloth': ([
        "................",
        "................",
        "....ooo.........",
        "...oAABoo.......",
        "...oABBBBoo.....",
        "....oBBCBBBo....",
        "....oBBBBCBBo...",
        ".....oBCBBBBo...",
        ".....oBBBBBCBo..",
        "....oABBCBBBBo..",
        "....oBBBBBBCo...",
        "...oBBoBBCBo....",
        "...oCo.oBBo.o...",
        "....o...oCo.....",
        ".........o......",
        "................"], dict(A=shade(SACK, 1.2), B=SACK, C=SACK_D)),
    'rust_scrap': ([
        "................",
        "................",
        "................",
        "......oooo......",
        "....ooLIIRo.....",
        "...oLIIRRIoo....",
        "...oIIRRIIIDo...",
        "..oLIRRIIoIIDo..",
        "..oIIRIIoooRDo..",
        "..oIIIIRIoIRDo..",
        "...oIRRRIIIDo...",
        "...oDIRIIDDo....",
        "....ooDDDoo.....",
        "......ooo.......",
        "................",
        "................"], dict(L=(150, 148, 150), I=(96, 88, 88), R=(150, 90, 52), D=(62, 56, 58))),
}


def icons():
    out = {}
    for name, (rows, pal) in MATERIALS.items():
        sp = Sprite(16)
        sp.art(rows, dict(pal, o=o))
        out[name] = sp
    for suit, names in SUITS.items():
        pal = dict(PALETTES[suit], o=o)
        rows, helmet_pal = HELMETS[suit]
        sp = Sprite(16)
        sp.art(rows, dict(helmet_pal, o=o))
        out[names[0]] = sp
        for name, shape in zip(names[1:], (CHEST, LEGS, BOOTS)):
            p = dict(pal)
            p.update(PIECE_PALETTE.get(name, {}))
            sp = Sprite(16)
            sp.art(shape, p)
            for x, y, art in OVER.get(name, []):
                for yy, row in enumerate(art):
                    for xx, ch in enumerate(row):
                        if ch == '.':
                            continue
                        # an overlay only touches the piece itself, and 'o' cuts a notch out of its edge
                        if sp.solid(x + xx, y + yy):
                            sp.px(x + xx, y + yy, p[ch])
            out[name] = sp
    return out


# ---------------------------------------------------------------------------------------------------- preview

def preview_model():
    """A figure with both armour textures on it. The atlas is the helmet-chest-boots texture, the leggings texture and a
    plain skin, one under the other."""
    m = Model(64, 96)
    parts = (('head', (0, 0, 0), (-4, -8, -4, 8, 8, 8), 'head'), ('body', (0, 0, 0), (-4, 0, -2, 8, 12, 4), 'body'),
             ('right_arm', (-5, 2, 0), (-3, -2, -2, 4, 12, 4), 'arm'), ('left_arm', (5, 2, 0), (-1, -2, -2, 4, 12, 4), 'arm'),
             ('right_leg', (-1.9, 12, 0), (-2, 0, -2, 4, 12, 4), 'leg'), ('left_leg', (1.9, 12, 0), (-2, 0, -2, 4, 12, 4), 'leg'))
    for name, pivot, (x, y, z, w, h, d), tex in parts:
        u, v = PARTS[tex][:2]
        boxes = [Box(x, y, z, w, h, d, 'skin', uv=(u, v + 64))]
        if name not in ('head',):
            if not name.endswith('leg') and name != 'x':
                pass
        boxes.append(Box(x, y, z, w, h, d, 'one', inflate=1.0, uv=(u, v)))
        if name in ('body', 'right_leg', 'left_leg'):
            boxes.append(Box(x, y, z, w, h, d, 'two', inflate=0.5, uv=(u, v + 32)))
        if name == 'head':
            boxes.append(Box(x, y, z, w, h, d, 'hat', inflate=1.5, uv=(32, 0)))
        m.part(name, None, pivot, boxes=boxes, emit=False)
    return m


def skin():
    img = Image.new('RGBA', (64, 32), (0, 0, 0, 0))
    w = Worn()
    w.img = img
    w.fill('head', lambda face, px, py, fw, fh: shade((176, 136, 108), light(face, py, fh)))
    for px in (2, 5):
        w.put('head', 'front', px, 4, (40, 30, 30))
    w.fill('body', lambda face, px, py, fw, fh: shade((110, 110, 116), light(face, py, fh)))
    w.fill('arm', lambda face, px, py, fw, fh: shade((176, 136, 108) if py > 3 else (110, 110, 116), light(face, py, fh)))
    w.fill('leg', lambda face, px, py, fw, fh: shade((70, 74, 96), light(face, py, fh)))
    return img


def write_json(path, data):
    os.makedirs(os.path.dirname(path), exist_ok=True)
    with open(path, 'w') as f:
        json.dump(data, f, indent=2)
        f.write('\n')


if __name__ == '__main__':
    shots = []
    model = preview_model()
    for suit, make in WORN.items():
        one, two = make()
        for layer, img in (('humanoid', one), ('humanoid_leggings', two)):
            path = f'{A}/textures/entity/equipment/{layer}/{suit}.png'
            os.makedirs(os.path.dirname(path), exist_ok=True)
            img.img.save(path)
        write_json(f'{A}/equipment/{suit}.json', {'layers': {'humanoid': [{'texture': f'{MOD}:{suit}'}],
                                                             'humanoid_leggings': [{'texture': f'{MOD}:{suit}'}]}})
        atlas = Image.new('RGBA', (64, 96), (0, 0, 0, 0))
        atlas.paste(one.img, (0, 0))
        atlas.paste(two.img, (0, 32))
        atlas.paste(skin(), (0, 64))
        for yaw in (-0.5, 0.0, 2.6):
            shots.append(model.render(atlas, yaw=yaw, size=(240, 330), zoom=8.5, centre=(0, 8)))
    made = icons()
    os.makedirs(f'{A}/textures/item', exist_ok=True)
    for name, sp in made.items():
        sp.save(f'{A}/textures/item/{name}.png')
        write_json(f'{A}/models/item/{name}.json', {'parent': 'minecraft:item/generated', 'textures': {'layer0': f'{MOD}:item/{name}'}})
        write_json(f'{A}/items/{name}.json', {'model': {'type': 'minecraft:model', 'model': f'{MOD}:item/{name}'}})
    if len(sys.argv) > 1:
        sheet(shots, 6).save(sys.argv[1])
        cell = 104
        out = Image.new('RGB', (4 * cell + 8, ((len(made) + 3) // 4) * cell + 8), (198, 198, 198))
        for i, name in enumerate(made):
            tile = Image.new('RGBA', (96, 96), (139, 139, 139, 255))
            tile.alpha_composite(made[name].img.resize((96, 96), 0))
            out.paste(tile.convert('RGB'), (8 + (i % 4) * cell, 8 + (i // 4) * cell))
        out.save(sys.argv[1].replace('.png', '_icons.png'))
