#!/usr/bin/env python3
"""The folk of the map (see Folk.java): the candle-woman, the debtor, the carver, the ferryman, the hangman, the cellarer
and the bellman - seven people on one humanoid mesh, each with the things only he wears (FolkModel shows a part only on
the one whose prefix it has) and a texture of his own. Writes the mesh of FolkModel.java and the seven textures.
Run from the repo root; pass a file name to also get a preview sheet (every one in his pose, from two sides)."""
import random
import sys

from humanoid import box_of, humanoid
from modelkit import Box, Painter, material, mix, shade, sheet

A = 'src/main/resources/assets/unfading_dawn'
JAVA = 'src/main/java/com/keykeeper/dushi/client/FolkModel.java'
FOLK = ('candlewife', 'debtor', 'carver', 'ferryman', 'hangman', 'cellarer', 'bellman')
PREFIX = {'cw': 'candlewife', 'db': 'debtor', 'cv': 'carver', 'fm': 'ferryman', 'hm': 'hangman', 'cl': 'cellarer', 'bm': 'bellman'}

# ---------------------------------------------------------------- mesh
M = humanoid(192)
# the candle-woman: a shawl over the shoulders, a long skirt, a church candle held in both hands (upright: the part
# turns back the arm's lift, see CANDLE_LIFT in FolkModel)
M.part('cw_shawl', 'body', (0, 0, 0), boxes=[Box(-4.5, -0.5, -2.5, 9, 6, 5, 'wool', 'shawl', inflate=0.3)])
M.part('cw_skirt', 'body', (0, 12, 0), boxes=[Box(-4.5, 0, -2.5, 9, 10, 5, 'cloth', 'skirt', inflate=0.2)])
M.part('cw_candle', 'right_arm', (-0.5, 9.5, -1.5), (0.95, 0, 0),
       boxes=[Box(-0.5, -5, -0.5, 1, 5, 1, 'wax', 'candle'), Box(-0.5, -6, -0.5, 1, 1, 1, 'flame', 'flame')])
# the debtor: a fetter on the ankle with a few links of the chain, and a beard that has had no razor since
M.part('db_fetter', 'right_leg', (0, 0, 0), boxes=[Box(-2.5, 9, -2.5, 5, 2, 5, 'iron', 'fetter'), Box(-0.5, 11, -4.5, 1, 1, 3, 'iron', 'link')])
M.part('db_beard', 'head', (0, 0, 0), boxes=[Box(-3.5, -3, -4.6, 7, 6, 1, 'hair', 'beard')])
# the carver: a felt cap, a leather apron to the knees, a mallet in the right hand and a chisel in the left
M.part('cv_cap', 'head', (0, 0, 0), boxes=[Box(-4.5, -9.2, -4.5, 9, 2, 9, 'felt', 'cap')])
M.part('cv_apron', 'body', (0, 0, 0), boxes=[Box(-4, 4, -2.8, 8, 13, 1, 'leather', 'apron')])
M.part('cv_mallet', 'right_arm', (-1, 9.5, 0), (0.7, 0, 0), boxes=[Box(-0.5, -0.5, -5, 1, 1, 6, 'wood', 'handle'), Box(-1.5, -1.5, -8, 3, 3, 3, 'wood', 'head')])
M.part('cv_chisel', 'left_arm', (1, 9.5, 0), boxes=[Box(-0.5, 0, -0.5, 1, 5, 1, 'iron', 'chisel')])
# the ferryman: a broad hat, the skirts of a long coat, and his boathook, held upright (see POLE_LIFT)
M.part('fm_hat', 'head', (0, 0, 0), boxes=[Box(-6.5, -8.6, -6.5, 13, 1, 13, 'felt', 'brim'), Box(-4.5, -11.6, -4.5, 9, 3, 9, 'felt', 'crown')])
M.part('fm_coat', 'body', (0, 0, 0), boxes=[Box(-4, 12, -2, 8, 8, 4, 'coat', 'skirts', inflate=0.45)])
M.part('fm_pole', 'right_arm', (-1, 9.5, 0), (0.45, 0, 0),
       boxes=[Box(-0.5, -28, -0.5, 1, 44, 1, 'wood', 'pole'), Box(-0.5, -30, -0.5, 1, 2, 1, 'iron', 'point'), Box(-0.5, -27, -2.5, 1, 1, 2, 'iron', 'hook')])
# the hangman: a barrel of a chest, the shoulders of a man who hauls on ropes, the peak of his hood, and the noose
M.part('hm_bulk', 'body', (0, 0, 0), boxes=[Box(-5, 0, -3, 10, 12, 6, 'leather', 'bulk', inflate=0.25)])
M.part('hm_peak', 'head', (0, 0, 0), boxes=[Box(-2, -10.5, -1.5, 4, 3, 4, 'hood', 'peak')])
M.part('hm_brawn_r', 'right_arm', (0, 0, 0), boxes=[Box(-3.5, -2.5, -2.5, 5, 8, 5, 'hood', 'brawn')])
M.part('hm_brawn_l', 'left_arm', (0, 0, 0), boxes=[Box(-1.5, -2.5, -2.5, 5, 8, 5, 'hood', 'brawn')])
M.part('hm_noose', 'left_arm', (1, 10, 0), boxes=[Box(-2, 0, -0.5, 4, 5, 1, 'rope', 'loop'), Box(-0.5, 5, -0.5, 1, 6, 1, 'rope', 'tail')])
# the cellarer: stubs of candles stuck to the head, the way the Unsleeping wear them, and the book of the count in the lap
M.part('cl_stubs', 'head', (0, 0, 0), boxes=[Box(-3, -10, -1, 1, 2, 1, 'wax', 'stub'), Box(-0.5, -11, -2, 1, 3, 1, 'wax', 'stub'),
                                             Box(2, -10, 0.5, 1, 2, 1, 'wax', 'stub')])
M.part('cl_book', None, (0, 19.4, -5.5), boxes=[Box(-4, -0.5, -2.5, 8, 1, 5, 'page', 'book')])
# the bellman: a white beard, and a coil of bell rope worn across the chest like a sash
M.part('bm_beard', 'head', (0, 0, 0), boxes=[Box(-3, -2.5, -4.7, 6, 7, 1, 'hair', 'beard')])
M.part('bm_coil', 'body', (0, 5, 0), (0, 0, 0.7), boxes=[Box(-1, -8, -2.7, 2, 16, 5.4, 'rope', 'sash')])
M.pack(start_y=0)

# ---------------------------------------------------------------- poses (the same as FolkModel.setupAnim, for the preview)
LIMBS = ('head', 'body', 'right_arm', 'left_arm', 'right_leg', 'left_leg')


def sitting(**arms):
    p = {n: {'off': (0, 10, 0)} for n in LIMBS}
    p['right_leg'] = {'off': (0, 10, 0), 'rot': (-1.5, 0.2, 0)}
    p['left_leg'] = {'off': (0, 10, 0), 'rot': (-1.45, -0.18, 0)}
    p['body'] = {'off': (0, 10, 0), 'rot': (0.12, 0, 0)}
    p['head'] = {'off': (0, 10, -0.8), 'rot': (0.25, 0, 0)}
    for name, (rot, off) in arms.items():
        p[name] = {'off': (off[0], 10 + off[1], off[2]), 'rot': rot}
    return p


POSES = {
    'candlewife': {'right_arm': (-0.95, -0.35, 0.1), 'left_arm': (-0.85, 0.45, -0.1)},
    'debtor': sitting(right_arm=((-0.85, 0, -0.25), (0, 0, -1.2)), left_arm=((-0.8, 0, 0.25), (0, 0, -1.2))),
    'carver': {'right_arm': (-1.05, -0.1, 0), 'left_arm': (-1.0, 0.25, 0), 'head': (0.35, 0, 0)},
    'ferryman': {'right_arm': (-0.45, 0, 0.05), 'left_arm': (0.08, 0, -0.06)},
    'hangman': {'right_arm': (0.05, 0, 0.08), 'left_arm': (-0.25, 0, -0.08)},
    'cellarer': sitting(right_arm=((-1.15, 0.15, -0.1), (0, 0, -1.0)), left_arm=((-1.1, -0.2, 0.1), (0, 0, -1.0))),
    'bellman': sitting(right_arm=((-0.85, 0, -0.25), (0, 0, -1.2)), left_arm=((-2.6, -0.3, -0.35), (0, 0, 0))),
}

# ---------------------------------------------------------------- textures
SKIN = (176, 146, 120)
SKIN_D = (140, 112, 92)
SKIN_PALE = (168, 150, 138)
SKIN_PALE_D = (132, 116, 108)
EYE = (34, 28, 26)
IRON = (104, 104, 110)
IRON_D = (70, 70, 76)
ROPE = (150, 124, 82)
ROPE_D = (112, 90, 58)
WAX = (226, 214, 176)
WAX_D = (190, 176, 136)
FLAME = (255, 214, 120)
WOOD = (118, 86, 56)
WOOD_D = (86, 62, 40)


def face(p, rows, pal):
    p.art(box_of(M, 'head'), 'front', rows, pal)


def limbs(p, sleeve, hand, trousers, foot, hand_rows=2, foot_rows=2, cuff=None):
    """Sleeves with the hands at their ends; legs with the feet at their ends."""
    for name in ('right_arm', 'left_arm'):
        b = box_of(M, name)
        p.fill_box(b, material(sleeve))
        for f in ('right', 'front', 'left', 'back'):
            fx, fy, fw, fh = b.faces()[f]
            for x in range(fw):
                if cuff:
                    p.put(b, f, x, fh - hand_rows - 1, cuff)
                for y in range(fh - hand_rows, fh):
                    p.put(b, f, x, y, hand if (x + y) % 3 else shade(hand, 0.85))
        for x in range(4):
            for z in range(4):
                p.put(b, 'bottom', x, z, shade(hand, 0.8))
    for name in ('right_leg', 'left_leg'):
        b = box_of(M, name)
        p.fill_box(b, material(trousers))
        for f in ('right', 'front', 'left', 'back'):
            fx, fy, fw, fh = b.faces()[f]
            for x in range(fw):
                for y in range(fh - foot_rows, fh):
                    p.put(b, f, x, y, foot if (x + y) % 4 else shade(foot, 0.85))
        for x in range(4):
            for z in range(4):
                p.put(b, 'bottom', x, z, shade(foot, 0.7))


def head_of(p, skin, hair, hair_rows=2, sides=True):
    """Skin all round, hair on the top and the back, and on the sides down to hair_rows."""
    h = box_of(M, 'head')
    p.fill_box(h, material(skin, edge=1.0, gradient=0.1))
    for f in ('top',):
        fx, fy, fw, fh = h.faces()[f]
        for x in range(fw):
            for y in range(fh):
                p.put(h, f, x, y, shade(hair, random.Random(x * 9 + y).choice((0.92, 1.0, 1.06))))
    fx, fy, fw, fh = h.faces()['back']
    for x in range(fw):
        for y in range(fh - 1):
            p.put(h, 'back', x, y, shade(hair, 0.92 + 0.08 * ((x + y) % 2)))
    if sides:
        for f in ('right', 'left'):
            for x in range(8):
                for y in range(hair_rows if x < 6 else 7):
                    p.put(h, f, x, y, shade(hair, 0.95))
    for x in range(8):
        for y in range(hair_rows):
            p.put(h, 'front', x, y, shade(hair, 0.95 + 0.05 * (x % 2)))


def no_hat(p):
    p.fill_box(box_of(M, 'hat'), lambda *a: None)


def extras(p, prefix, fns):
    for name in M.order:
        if name.startswith(prefix + '_'):
            for b in M.parts[name]['boxes']:
                p.fill_box(b, fns[b.tag] if b.tag in fns else fns['*'])


def clear_others(p, mine):
    for name in M.order:
        pre = name.split('_')[0]
        if pre in PREFIX and pre != mine:
            for b in M.parts[name]['boxes']:
                p.fill_box(b, lambda *a: None)


def candlewife(p):
    DRESS = (58, 52, 60)
    SHAWL = (112, 92, 76)
    SCARF = (34, 32, 38)
    head_of(p, SKIN_PALE, SCARF, hair_rows=3)
    face(p, ["kkkkkkkk",
             "kkkkkkkk",
             "kkssssk.",
             "ks.ss.sk",
             "ksEssEsk",
             "kssddssk",
             "ksssssskk",
             "kksmmsskk"], {'k': SCARF, 's': SKIN_PALE, 'd': SKIN_PALE_D, 'E': EYE, 'm': SKIN_PALE_D, '.': SKIN_PALE})
    # the kerchief: the hat layer over the hair, tied under the chin, and its knot hanging at the back
    hat = box_of(M, 'hat')

    def kerchief(f, px, py, fw, fh, r):
        if f == 'bottom':
            return None
        if f == 'front':
            return shade(SCARF, 1.1) if py < 2 or px in (0, fw - 1) else None
        if f in ('right', 'left'):
            return shade(SCARF, 1.0 - 0.1 * (py > 5)) if py < 7 or px > 4 else None
        return shade(SCARF, r.choice((0.94, 1.0, 1.08)))
    p.fill_box(hat, kerchief)
    p.fill_box(box_of(M, 'body'), material(DRESS))
    limbs(p, DRESS, SKIN_PALE, DRESS, (40, 34, 30), cuff=shade(DRESS, 0.8))
    extras(p, 'cw', {'shawl': lambda f, px, py, fw, fh, r: None if f == 'bottom' or (f == 'front' and 2 <= px <= fw - 3 and py >= 3) else
                     shade(SHAWL, (1.08 if (px + py) % 4 == 0 else 0.96) * (0.9 if py == fh - 1 else 1.0)),
                     'skirt': material(shade(DRESS, 0.9), gradient=0.3),
                     'candle': material(WAX, edge=1.0), 'flame': lambda *a: FLAME})
    # the fringe of the shawl
    for b in M.parts['cw_shawl']['boxes']:
        p.band(b, -1, shade(SHAWL, 0.7), every=2)


def debtor(p):
    SHIRT = (124, 112, 90)
    HAIR = (66, 58, 50)
    head_of(p, SKIN_D, HAIR, hair_rows=2)
    face(p, ["hhhhhhhh",
             "hhhhhhhh",
             "hssssssh",
             "sddssdds",
             "sEdssdEs",
             "sssddsss",
             "........",
             "........"], {'h': HAIR, 's': SKIN_D, 'd': shade(SKIN_D, 0.85), 'E': EYE})
    no_hat(p)
    body = box_of(M, 'body')
    p.fill_box(body, lambda f, px, py, fw, fh, r: None if False else shade(SHIRT, r.choice((0.86, 0.95, 1.0, 1.05)) * (0.8 if (px * 3 + py) % 11 == 0 else 1.0)))
    limbs(p, SHIRT, SKIN_D, (84, 76, 64), SKIN_D, foot_rows=2)
    extras(p, 'db', {'fetter': material(IRON, rim=shade(IRON, 1.2)), 'link': material(IRON_D, edge=1.0),
                     'beard': lambda f, px, py, fw, fh, r: None if (f == 'front' and py == fh - 1 and px % 2) else shade(HAIR, r.choice((0.85, 1.0, 1.1)))})


def carver(p):
    SHIRT = (150, 146, 136)
    DUST = (214, 210, 200)
    LEATHER = (104, 78, 54)
    HAIR = (110, 104, 96)
    head_of(p, SKIN, HAIR)
    face(p, ["hhhhhhhh",
             "hhhhhhhh",
             "ssssssss",
             "sbbssbbs",
             "sEssssEs",
             "sssddsss",
             "ssmmmmss",
             "ssssssss"], {'h': HAIR, 's': SKIN, 'd': SKIN_D, 'E': EYE, 'b': shade(HAIR, 0.8), 'm': shade(SKIN_D, 0.9)})
    no_hat(p)

    def dusty(base):
        fn = material(base)

        def f(face_, px, py, fw, fh, r):
            c = fn(face_, px, py, fw, fh, r)
            return mix(c, DUST, 0.35) if r.random() < 0.25 else c
        return f
    p.fill_box(box_of(M, 'body'), dusty(SHIRT))
    limbs(p, SHIRT, SKIN, (92, 88, 80), (60, 50, 40), cuff=shade(SHIRT, 0.8))
    for name in ('right_arm', 'left_arm'):            # the sleeves rolled to the elbow
        b = box_of(M, name)
        for f in ('right', 'front', 'left', 'back'):
            for x in range(4):
                p.put(b, f, x, 5, shade(SHIRT, 0.8))
                for y in range(6, 10):
                    p.put(b, f, x, y, SKIN if (x + y) % 3 else SKIN_D)
    extras(p, 'cv', {'cap': material((76, 70, 64)), 'apron': dusty(LEATHER),
                     'handle': material(WOOD, edge=1.0), 'head': material(WOOD_D), 'chisel': material(IRON, edge=1.0)})


def ferryman(p):
    COAT = (54, 62, 58)
    HAIR = (78, 70, 60)
    FELT = (52, 46, 40)
    head_of(p, SKIN, HAIR)
    face(p, ["hhhhhhhh",
             "hhhhhhhh",
             "ssssssss",
             "sssssssS",
             "sEssssEs",
             "sssddsss",
             "shhhhhhs",
             "shhhhhhs"], {'h': HAIR, 's': SKIN, 'S': SKIN_D, 'd': SKIN_D, 'E': EYE})
    no_hat(p)
    p.fill_box(box_of(M, 'body'), material(COAT))
    body = box_of(M, 'body')
    for y in range(12):                               # the front of the coat and its buttons
        p.put(body, 'front', 3, y, shade(COAT, 0.75))
        if y % 3 == 1:
            p.put(body, 'front', 4, y, (150, 130, 90))
    p.band(body, 8, (70, 52, 36))
    limbs(p, COAT, SKIN, (60, 56, 50), (44, 38, 32), foot_rows=4)
    extras(p, 'fm', {'brim': material(FELT, edge=0.9), 'crown': material(FELT, rim=(84, 70, 52)), 'skirts': material(shade(COAT, 0.95), gradient=0.3),
                     'pole': material(WOOD, edge=1.0, noise=(0.9, 1.0, 1.1)), 'point': material(IRON, edge=1.0), 'hook': material(IRON, edge=1.0)})


def hangman(p):
    HOOD = (70, 24, 22)
    HOOD_D = (46, 16, 16)
    LEATHER = (66, 50, 38)
    h = box_of(M, 'head')
    p.fill_box(h, material(HOOD, edge=0.9))
    face(p, ["hhhhhhhh",
             "hhhhhhhh",
             "hhhhhhhh",
             "hkkhhkkh",
             "hkkhhkkh",
             "hhhhhhhh",
             "hhhhhhhh",
             "hhhhhhhh"], {'h': HOOD, 'k': (14, 10, 10)})
    no_hat(p)
    # bare arms, a leather vest
    p.fill_box(box_of(M, 'body'), material(LEATHER))
    limbs(p, SKIN_D, SKIN_D, (48, 40, 34), (34, 28, 24), foot_rows=3)
    extras(p, 'hm', {'bulk': material(LEATHER, rim=HOOD_D), 'peak': material(HOOD),
                     'brawn': lambda f, px, py, fw, fh, r: shade(SKIN_D, r.choice((0.92, 1.0, 1.05)) * (0.85 if py == fh - 1 else 1.0)),
                     'loop': lambda f, px, py, fw, fh, r: None if (f in ('front', 'back') and 1 <= px <= fw - 2 and 1 <= py <= fh - 2) else
                     (ROPE if (px + py) % 2 else ROPE_D),
                     'tail': lambda f, px, py, fw, fh, r: ROPE if (px + py) % 2 else ROPE_D})
    # the hood's skirt over the shoulders, painted on the vest's top
    body = box_of(M, 'body')
    for f in ('front', 'back', 'right', 'left'):
        fx, fy, fw, fh = body.faces()[f]
        for x in range(fw):
            p.put(body, f, x, 0, HOOD_D)
            p.put(body, f, x, 1, HOOD if x % 3 else HOOD_D)
    for b in M.parts['hm_bulk']['boxes']:
        for f in ('front', 'back', 'right', 'left'):
            fx, fy, fw, fh = b.faces()[f]
            for x in range(fw):
                p.put(b, f, x, 0, HOOD_D)
                p.put(b, f, x, 1, HOOD)
                p.put(b, f, x, 2, HOOD_D if x % 2 else HOOD)


def cellarer(p):
    ROBE = (40, 38, 44)
    HAIR = (90, 84, 80)
    head_of(p, SKIN_PALE, HAIR, hair_rows=1)
    face(p, ["hhhhhhhh",
             "ssssssss",
             "ssssssss",
             "sddssdds",
             "s-Esse-s",
             "sssddsss",
             "ssssssss",
             "ssmmmmss"], {'h': HAIR, 's': SKIN_PALE, 'd': SKIN_PALE_D, 'E': EYE, '-': shade(SKIN_PALE_D, 0.8), 'm': SKIN_PALE_D, 'e': EYE})
    no_hat(p)
    p.fill_box(box_of(M, 'body'), material(ROBE))
    p.band(box_of(M, 'body'), 8, ROPE)
    limbs(p, ROBE, SKIN_PALE, ROBE, (36, 30, 26), foot_rows=1)
    # wax dripped down the face and the hair from the stubs
    hd = box_of(M, 'head')
    for x, y in ((2, 0), (2, 1), (5, 0), (5, 1), (5, 2)):
        p.put(hd, 'front', x, y, WAX_D)

    def page(f, px, py, fw, fh, r):
        if f == 'top':
            return (196, 186, 160) if px not in (fw // 2,) else (150, 140, 116)
        return (96, 64, 44)
    extras(p, 'cl', {'stub': lambda f, px, py, fw, fh, r: FLAME if (f == 'top') else (WAX if py else WAX_D), 'book': page})
    book = M.parts['cl_book']['boxes'][0]
    for y in range(1, 4, 2):                          # lines of the count, in columns
        for x in (1, 2, 5, 6):
            p.put(book, 'top', x, y, (90, 80, 70))


def bellman(p):
    SMOCK = (96, 84, 70)
    WHITE = (210, 206, 196)
    head_of(p, SKIN, WHITE, hair_rows=0, sides=False)
    hd = box_of(M, 'head')
    for f in ('top',):                                 # bald on top
        fx, fy, fw, fh = hd.faces()[f]
        for x in range(fw):
            for y in range(fh):
                p.put(hd, f, x, y, shade(SKIN, 1.05))
    for f in ('right', 'left'):                         # a fringe of white over the ears
        for x in range(8):
            for y in (3, 4, 5):
                p.put(hd, f, x, y, WHITE if (x + y) % 3 else shade(WHITE, 0.85))
    fx, fy, fw, fh = hd.faces()['back']
    for x in range(fw):
        for y in range(fh):
            p.put(hd, 'back', x, y, shade(SKIN, 1.0) if y < 3 else (WHITE if (x + y) % 3 else shade(WHITE, 0.85)))
    face(p, ["ssssssss",
             "ssssssss",
             "wwssssww",
             "sEssssEs",
             "sssddsss",
             "........",
             "........",
             "........"], {'s': SKIN, 'd': SKIN_D, 'E': (90, 96, 104), 'w': WHITE})
    no_hat(p)
    p.fill_box(box_of(M, 'body'), material(SMOCK))
    limbs(p, SMOCK, SKIN, (70, 62, 54), (44, 38, 32))
    extras(p, 'bm', {'beard': lambda f, px, py, fw, fh, r: None if (f == 'front' and py == fh - 1 and px in (0, fw - 1)) else
                     (WHITE if (px + py) % 3 else shade(WHITE, 0.86)),
                     'sash': lambda f, px, py, fw, fh, r: ROPE if (py + px) % 3 else ROPE_D})


PAINT = {'candlewife': candlewife, 'debtor': debtor, 'carver': carver, 'ferryman': ferryman, 'hangman': hangman, 'cellarer': cellarer,
         'bellman': bellman}
MINE = {v: k for k, v in PREFIX.items()}


def make(who):
    p = Painter(M, 31)
    PAINT[who](p)
    clear_others(p, MINE[who])
    return p.tex


def hidden_for(who):
    return tuple(n for n in M.order if n.split('_')[0] in PREFIX and n.split('_')[0] != MINE[who])


if __name__ == '__main__':
    M.write_java(JAVA)
    shots = []
    for who in FOLK:
        tex = make(who)
        tex.save(f'{A}/textures/entity/{who}.png')
        if len(sys.argv) > 1:
            pose = POSES[who]
            for yaw in (-0.5, 2.6):
                shots.append(M.render(tex, yaw=yaw, pose=pose, size=(240, 330), zoom=8.0, centre=(0, 6), hidden=hidden_for(who)))
    if len(sys.argv) > 1:
        sheet(shots, 7).save(sys.argv[1])
