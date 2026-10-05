#!/usr/bin/env python3
"""The withered (prisoner, guard, archer, acolyte; townsman, butcher, watchman of the village; digger, barrowman,
sapper of the quarry): mesh extras for WitheredModel.java and their textures.
Run from the repo root; pass a file name to also get a preview sheet."""
import random
import sys

from humanoid import box_of, humanoid
from modelkit import Box, Painter, mix, shade, sheet

A = 'src/main/resources/assets/vsratie_dushi'
JAVA = 'src/main/java/com/keykeeper/dushi/client/WitheredModel.java'

# ---------------------------------------------------------------- mesh
M = humanoid(192, slim_arms=True)
# prisoner: an iron collar and a shackle with a few links of chain still on the left wrist
M.part('collar', 'body', (0, 0, 0), boxes=[Box(-4.5, -0.6, -2.5, 9, 2, 5, 'iron', 'collar')])
M.part('shackle', 'left_arm', (0.5, 7, 0), boxes=[Box(-2, 0, -2.5, 4, 2, 5, 'iron', 'cuff'), Box(-0.5, 2, -3.3, 1, 6, 1, 'iron', 'chain')])
# guard: a kettle hat and a plate on the sword shoulder
M.part('kettle_hat', 'head', (0, 0, 0), boxes=[Box(-6, -6.6, -6, 12, 1, 12, 'iron', 'brim'), Box(-4.5, -9.6, -4.5, 9, 3, 9, 'iron', 'dome')])
M.part('pauldron', 'right_arm', (0, 0, 0), boxes=[Box(-3, -3, -3, 4, 3, 6, 'iron', 'pauldron')])
# archer: the tail of the hood and a quiver
M.part('hood_tail', 'head', (0, -6.5, 4), (0.45, 0, 0), boxes=[Box(-2, -1, -1, 4, 4, 3, 'hood', 'tail'), Box(-1, 3, 0.5, 2, 4, 1, 'hood', 'tip')])
M.part('quiver', 'body', (2.2, 1, 2), (0, 0, 0.32), boxes=[Box(-1.5, 0, 0, 3, 9, 2, 'leather', 'quiver'), Box(-1, -2, 0.5, 2, 2, 1, 'fletch', 'fletch')])
# townsman: a flat felt cap pulled down on one side
M.part('cap', 'head', (0, 0, 0), (0, 0, -0.07), boxes=[Box(-4.5, -9.4, -4.5, 9, 2, 9, 'felt', 'crown'), Box(-3.5, -8.0, -6.5, 7, 1, 2, 'felt', 'peak')])
# butcher: twice the man the others are - a barrel of a trunk over the thin one, a gut, the skirt of his apron hanging
# from under it (it hangs straight though he stoops), and the shoulders of somebody who has swung a cleaver all his life
M.part('bulk', 'body', (0, 0, 0), boxes=[Box(-5, 0, -3, 10, 12, 6, 'flesh', 'bulk', inflate=0.25)])
M.part('belly', 'body', (0, 0, 0), boxes=[Box(-4, 4, -5, 8, 7, 2, 'apron', 'belly')])
M.part('apron', 'body', (0, 11, -3.8), (-0.24, 0, 0), boxes=[Box(-4, 0, -0.5, 8, 7, 1, 'apron', 'skirt')])
M.part('brawn_r', 'right_arm', (0, 0, 0), boxes=[Box(-3, -3, -3, 5, 8, 6, 'flesh', 'brawn')])
M.part('brawn_l', 'left_arm', (0, 0, 0), boxes=[Box(-2, -3, -3, 5, 8, 6, 'flesh', 'brawn')])
# watchman: the skirts of a long coat
M.part('coat_tail', 'body', (0, 0, 0), boxes=[Box(-4, 12, -2, 8, 6, 4, 'coat', 'skirts', inflate=0.45)])
# the quarry. A digger: a leather skullcap with a tallow candle stuck to its front
M.part('candle_cap', 'head', (0, 0, 0), boxes=[Box(-4.5, -9.2, -4.5, 9, 3, 9, 'leather', 'skullcap'), Box(-1.5, -7.6, -5.5, 3, 1, 1, 'iron', 'sconce'),
                                               Box(-0.5, -10.6, -5.5, 1, 3, 1, 'tallow', 'candle')])
# a barrowman: his barrow, loaded with cut stone. It hangs from the model's root in front of him and turns about its axle
M.part('barrow', None, (0, 20, -24), boxes=[
    Box(-1, -4, -4, 2, 8, 8, 'wheel', 'wheel'), Box(-6, -9, 3, 12, 6, 14, 'plank', 'tray'), Box(-4.5, -12, 5, 9, 3, 10, 'stone', 'load'),
    Box(-6.5, -8.5, 17, 1, 1, 8, 'plank', 'handle_r'), Box(5.5, -8.5, 17, 1, 1, 8, 'plank', 'handle_l'),
    Box(-5.5, -3, 14, 1, 6, 1, 'plank', 'leg_r'), Box(4.5, -3, 14, 1, 6, 1, 'plank', 'leg_l'),
    Box(-2.5, -1, -1, 1, 2, 5, 'plank', 'fork_r'), Box(1.5, -1, -1, 1, 2, 5, 'plank', 'fork_l')])
# a sapper: a keg of blasting powder on his back, the match cord coiled over it
M.part('keg', 'body', (0, 0, 0), boxes=[Box(-3, 1, 2, 6, 8, 5, 'keg', 'keg'), Box(0.5, -2, 4, 1, 3, 1, 'fuse', 'fuse')])
M.pack(start_y=0)

# ---------------------------------------------------------------- colours
SKIN = (104, 84, 68)
SKIN_D = (78, 62, 51)
SKIN_DD = (56, 44, 37)
SKIN_L = (126, 104, 85)
BLACK = (22, 17, 15)
EMBER = (255, 150, 44)
BONE = (208, 194, 164)
SACK = (132, 118, 90)
SACK_D = (98, 86, 64)
ROPE = (94, 72, 44)
IRON = (104, 104, 110)
IRON_D = (70, 70, 76)
IRON_L = (150, 150, 156)
RUST = (134, 78, 46)
MAIL = (98, 97, 102)
MAIL_D = (80, 79, 86)
TABARD = (112, 44, 40)
TABARD_D = (80, 30, 30)
TABARD_L = (140, 62, 52)
LEATHER = (94, 62, 38)
LEATHER_D = (66, 44, 28)
HOOD = (62, 72, 52)
HOOD_D = (44, 52, 38)
HOOD_L = (82, 94, 68)
BOOT = (52, 40, 32)
ROBE = (66, 46, 62)
ROBE_D = (44, 30, 42)
ROBE_L = (88, 62, 80)
GOLD = (214, 184, 106)
GOLD_D = (158, 128, 62)
LINEN = (120, 112, 96)
LINEN_D = (92, 86, 74)
WOOL = (84, 70, 58)
WOOL_D = (62, 52, 44)
FELT = (70, 56, 48)
FELT_D = (50, 40, 36)
APRON = (214, 206, 190)
APRON_D = (176, 168, 152)
GORE = (104, 26, 22)
GORE_D = (70, 16, 16)
COAT = (50, 56, 70)
COAT_D = (36, 40, 52)
COAT_L = (68, 76, 92)
BRASS = (176, 146, 78)

HEAD = {
    'front': ["hhshhhsh",
              "ssssssss",
              "sDDssDDs",
              "dKEddEKd",
              "ssdNNdss",
              "sdssssds",
              "dTtTTtTd",
              "sddDDdds"],
    'right': ["hhhshhhh", "sssssssh", "ssssssss", "sdssssss", "ssDsssss", "sssdssss", "ssssddss", "sssssdds"],
    'left': ["hhhhshhh", "hsssssss", "ssssssss", "ssssssds", "sssssDss", "ssssdsss", "ssddssss", "sddsssss"],
    'back': ["hhhhhhhh", "hshhhhsh", "sshhshss", "ssssssss", "ssdsssss", "sssssdss", "ssssssss", "sddssdds"],
    'top': ["hhhhhhhh", "hhshhhhh", "hhhhhshh", "hshhhhhh", "hhhhhhhh", "hhhhshhh", "hshhhhhh", "hhhhhhhh"],
    'bottom': ["dddddddd"] * 8,
}
HEAD_PAL = {'s': SKIN, 'd': SKIN_D, 'D': SKIN_DD, 'K': BLACK, 'E': EMBER, 'N': (40, 28, 24), 'T': BONE, 't': (70, 56, 46), 'h': (70, 58, 50)}

TORSO_FRONT = ["sdssssds",
               "ssdLLdss",
               "sLsddsLs",
               "dsdssdsd",
               "sLsddsLs",
               "dsdssdsd",
               "sLsddsLs",
               "ssdssdss",
               "sdsDDsds",
               "ssdDDdss",
               "sssddsss",
               "dssssssd"]
TORSO_BACK = ["ssssssss",
              "sLsddsLs",
              "ssdDDdss",
              "sLsDDsLs",
              "dssDDssd",
              "sLsDDsLs",
              "ssdDDdss",
              "sdsDDsds",
              "sssDDsss",
              "ssdddsss",
              "sssddsss",
              "dssssssd"]
BODY_PAL = {'s': SKIN, 'd': SKIN_D, 'D': SKIN_DD, 'L': SKIN_L}


def flesh(face, px, py, fw, fh, rnd):
    f = {'top': 1.08, 'bottom': 0.72}.get(face, 1.02 - 0.12 * py / max(1, fh - 1))
    f *= rnd.choice((0.97, 1.0, 1.0, 1.0, 1.03))
    return shade(SKIN, f)


def limb(p, box, top_rows=0, hand=True, foot=False):
    """Bare withered limb: darker joints in the middle, dark dirty ends."""
    p.fill_box(box, flesh)
    for face in ('right', 'front', 'left', 'back'):
        fx, fy, fw, fh = box.faces()[face]
        for x in range(fw):
            # the bone shows down the middle of the front
            if face == 'front' and x == fw // 2:
                for y in (2, 3, 8):
                    p.put(box, face, x, y, SKIN_L)
            p.put(box, face, x, 5, shade(SKIN_D, 1.0 if (x + (face == 'left')) % 2 else 1.1))
            if x in (0, fw - 1):
                for y in range(fh):
                    c = p.get(box, face, x, y)
                    p.put(box, face, x, y, shade(c, 0.86))
            for y in range(fh - (3 if foot else 2), fh):
                p.put(box, face, x, y, shade(SKIN_DD, 1.0 + 0.12 * ((x + y) % 2)))
    for x in range(box.tw):
        for y in range(box.td):
            p.put(box, 'bottom', x, y, SKIN_DD)


def paint_base(p, m, rnd):
    head = box_of(m, 'head')
    for face, rows in HEAD.items():
        p.art(head, face, rows, HEAD_PAL)
        fx, fy, fw, fh = head.faces()[face]
        for x in range(fw):
            for y in range(fh):
                c = p.get(head, face, x, y)
                if c[:3] in (SKIN, SKIN_D, (70, 58, 50)):
                    p.put(head, face, x, y, shade(c, rnd.choice((0.97, 1.0, 1.0, 1.03))))
    body = box_of(m, 'body')
    p.fill_box(body, flesh)
    p.art(body, 'front', TORSO_FRONT, BODY_PAL)
    p.art(body, 'back', TORSO_BACK, BODY_PAL)
    for side in ('right', 'left'):
        for y in range(2, 9, 2):
            p.put(body, side, 1, y, SKIN_D)
            p.put(body, side, 2, y, SKIN_D)
    limb(p, box_of(m, 'right_arm'))
    limb(p, box_of(m, 'left_arm'))
    limb(p, box_of(m, 'right_leg'), foot=True)
    limb(p, box_of(m, 'left_leg'), foot=True)


def cloth_rows(p, box, rows, base, dark, rnd, ragged=True, faces=('right', 'front', 'left', 'back')):
    """Covers texel rows `rows` of the sides of a box with cloth; the last row is torn."""
    rows = list(rows)
    for face in faces:
        fx, fy, fw, fh = box.faces()[face]
        for x in range(fw):
            for y in rows:
                if ragged and y == rows[-1] and rnd.random() < 0.45:
                    continue
                c = shade(base, rnd.choice((0.96, 1.0, 1.0, 1.04)) * (1.04 - 0.1 * (y - rows[0]) / max(1, len(rows) - 1)))
                if (x * 3 + y * 2) % 7 == 0:
                    c = shade(dark, 1.0)
                p.put(box, face, x, y, c)


def iron(face, px, py, fw, fh, rnd):
    f = {'top': 1.15, 'bottom': 0.7}.get(face, 1.0)
    c = shade(IRON, f * rnd.choice((0.9, 1.0, 1.0, 1.08)))
    if rnd.random() < 0.1:
        c = mix(c, RUST, 0.5)
    if min(fw, fh) > 2 and (px in (0, fw - 1) or py in (0, fh - 1)):
        c = shade(c, 0.8)
    return c


QUARRY = ('digger', 'barrowman', 'sapper')
DUST = (196, 192, 180)
TALLOW = (226, 214, 176)
FLAME = (255, 214, 110)
PLANK = (112, 88, 60)
PLANK_D = (80, 62, 44)
PLANK_L = (140, 112, 78)
STONE = (190, 188, 180)
STONE_D = (128, 126, 122)
STONE_L = (220, 218, 208)


def blotch_(seed, x, y, cell=2.4):
    from modelkit import blotch
    return blotch(seed, x, y, cell)


def dust(p, boxes, rnd):
    """The white dust of the quarry on everything: thick on what faces up and on the feet, in streaks elsewhere."""
    for b in boxes:
        for face, (fx, fy, fw, fh) in b.faces().items():
            for x in range(fw):
                for y in range(fh):
                    c = p.tex.getpixel((fx + x, fy + y))
                    if c[3] == 0 or c[:3] in (BLACK, EMBER):
                        continue
                    amount = 0.55 if face == 'top' else 0.1 + 0.45 * blotch_(f'dust{b.part}{face}', x, y)
                    if face not in ('top', 'bottom') and y >= fh - 2:
                        amount += 0.2
                    p.px(fx + x, fy + y, mix(c, DUST, min(0.75, amount)))


def make(kind, seed):
    rnd = random.Random(seed)
    p = Painter(M, seed)
    paint_base(p, M, rnd)
    body = box_of(M, 'body')
    rl, ll = box_of(M, 'right_leg'), box_of(M, 'left_leg')
    ra, la = box_of(M, 'right_arm'), box_of(M, 'left_arm')
    head = box_of(M, 'head')
    if kind == 'prisoner':
        # a sackcloth wrap round the hips, held by a rope; a rag across one shoulder
        cloth_rows(p, body, range(9, 12), SACK, SACK_D, rnd, ragged=False)
        cloth_rows(p, rl, range(0, 5), SACK, SACK_D, rnd)
        cloth_rows(p, ll, range(0, 4), SACK, SACK_D, rnd)
        p.band(body, 9, ROPE)
        p.put(body, 'front', 5, 10, ROPE)
        p.put(body, 'front', 5, 11, shade(ROPE, 0.8))
        for i in range(6):
            p.put(body, 'front', i, i // 2 + 1 if i < 6 else 3, SACK_D if i % 2 else SACK)
            p.put(body, 'back', 7 - i, i // 2 + 1, SACK_D if i % 2 else SACK)
        for z in range(4):
            p.put(body, 'top', 1, z, SACK)
        # welts where the shackles were, on both wrists
        for box in (ra, la):
            p.band(box, 8, shade(SKIN_DD, 0.85))
    elif kind == 'guard':
        # a mail shirt under a faded red tabard, leather boots and gloves
        for b, rows in ((body, range(0, 12)), (ra, range(0, 8)), (la, range(0, 8)), (rl, range(0, 4)), (ll, range(0, 4))):
            for face in ('right', 'front', 'left', 'back'):
                fx, fy, fw, fh = b.faces()[face]
                for x in range(fw):
                    for y in rows:
                        c = MAIL if (x + y) % 2 else MAIL_D
                        if rnd.random() < 0.05:
                            c = mix(c, RUST, 0.45)
                        p.put(b, face, x, y, c)
        for face in ('front', 'back'):
            for x in range(1, 7):
                for y in range(1, 12):
                    c = TABARD if (x + y) % 3 else TABARD_D
                    if x in (1, 6):
                        c = TABARD_D
                    if y == 11 and x % 2:
                        continue
                    p.put(body, face, x, y, c)
        # a pale key on the chest: the badge of the stockade
        for y, row in enumerate((".KK.", "K..K", "K..K", ".KK.", ".K..", ".KK.", ".K..", ".KK.")):
            for x, ch in enumerate(row):
                if ch == 'K':
                    p.put(body, 'front', 2 + x, 1 + y, (204, 184, 128))
        p.band(body, 9, LEATHER_D)
        p.put(body, 'front', 3, 9, (170, 150, 90))
        for b in (rl, ll):
            cloth_rows(p, b, range(7, 12), BOOT, shade(BOOT, 0.8), rnd, ragged=False)
            p.band(b, 7, LEATHER)
        for b in (ra, la):
            cloth_rows(p, b, range(9, 12), LEATHER, LEATHER_D, rnd, ragged=False)
        for x in range(8):
            for z in range(4):
                p.put(body, 'top', x, z, MAIL_D if (x + z) % 2 else MAIL)
    elif kind == 'archer':
        # a hooded cape, a leather jerkin, wrapped shins
        for face in ('right', 'front', 'left', 'back'):
            fx, fy, fw, fh = body.faces()[face]
            for x in range(fw):
                for y in range(0, 10):
                    c = LEATHER_D if y in (4, 5) and x in (0, fw - 1) else LEATHER
                    p.put(body, face, x, y, shade(c, 1.06 - 0.16 * y / 9))
        for y in range(1, 9):
            p.put(body, 'front', 4, y, LEATHER_D)
            if y % 2:
                p.put(body, 'front', 3, y, (150, 130, 96))
        # the cape: over the shoulders and down the back
        for x in range(8):
            for y in range(0, 11):
                if y == 10 and x % 3 == 0:
                    continue
                p.put(body, 'back', x, y, shade(HOOD_D if x in (2, 5) and y > 1 else HOOD, 1.05 - 0.15 * y / 10))
            for y in range(0, 2):
                p.put(body, 'front', x, y, HOOD if x not in (3, 4) else HOOD_D)
            for z in range(4):
                p.put(body, 'top', x, z, HOOD)
        for side in ('right', 'left'):
            for x in range(4):
                for y in range(0, 3):
                    p.put(body, side, x, y, HOOD_D if y == 2 else HOOD)
        cloth_rows(p, body, range(10, 12), SACK_D, shade(SACK_D, 0.8), rnd, ragged=False)
        p.band(body, 9, LEATHER_D)
        for b in (rl, ll):
            cloth_rows(p, b, range(0, 5), SACK_D, shade(SACK_D, 0.8), rnd)
            for y in (6, 8, 10):
                p.band(b, y, SACK)
        # bracer on the bow arm, shoulder of the cape on both
        cloth_rows(p, la, range(6, 10), LEATHER, LEATHER_D, rnd, ragged=False)
        for b in (ra, la):
            cloth_rows(p, b, range(0, 3), HOOD, HOOD_D, rnd)
            for x in range(b.tw):
                for z in range(b.td):
                    p.put(b, 'top', x, z, HOOD)

    elif kind == 'acolyte':
        # what is left of a habit: dark cloth to the ankles, a gold stripe down the front, a rope for a belt
        for b, rows in ((body, range(0, 12)), (rl, range(0, 10)), (ll, range(0, 9)), (ra, range(0, 9)), (la, range(0, 8))):
            cloth_rows(p, b, rows, ROBE, ROBE_D, rnd, ragged=b is not body)
        for y in range(0, 12):
            p.put(body, 'front', 3, y, shade(GOLD_D, 1.0 - 0.03 * y))
            p.put(body, 'front', 4, y, shade(GOLD if y % 3 == 1 else GOLD_D, 1.0 - 0.03 * y))
        p.band(body, 8, ROPE)
        p.put(body, 'front', 5, 9, ROPE)
        p.put(body, 'front', 5, 10, shade(ROPE, 0.8))
        for x in range(8):
            for z in range(4):
                p.put(body, 'top', x, z, ROBE)
        for b in (ra, la):
            for x in range(b.tw):
                for z in range(b.td):
                    p.put(b, 'top', x, z, ROBE)

    elif kind == 'townsman':
        # what a poor man was buried in: a linen shirt gone grey, wool breeches, bare feet
        cloth_rows(p, body, range(0, 12), LINEN, LINEN_D, rnd, ragged=False)
        for b, rows in ((ra, range(0, 7)), (la, range(0, 5))):
            cloth_rows(p, b, rows, LINEN, LINEN_D, rnd)
        for b, rows in ((rl, range(0, 8)), (ll, range(0, 9))):
            cloth_rows(p, b, rows, WOOL, WOOL_D, rnd)
        # the neck of the shirt lies open on the ribs, one lace still in it
        for x, y in ((3, 0), (4, 0), (3, 1), (4, 1), (4, 2), (3, 2), (4, 3)):
            p.put(body, 'front', x, y, SKIN_D if (x + y) % 2 else SKIN)
        p.put(body, 'front', 2, 1, LINEN_D)
        p.put(body, 'front', 5, 2, LINEN_D)
        p.band(body, 9, ROPE)
        p.put(body, 'front', 2, 10, ROPE)
        for x, y in ((6, 5), (6, 6), (1, 7), (5, 3)):
            p.put(body, 'front', x, y, WOOL_D)          # a patch and a few holes
        for x in range(8):
            for z in range(4):
                p.put(body, 'top', x, z, LINEN)
    elif kind == 'butcher':
        # bare to the waist under a white apron gone red; wool breeches, boots (the trunk itself is under the bulk)
        for b in (rl, ll):
            cloth_rows(p, b, range(0, 9), WOOL, WOOL_D, rnd)
            cloth_rows(p, b, range(8, 12), BOOT, shade(BOOT, 0.8), rnd, ragged=False)
        # the working arm is red to the elbow
        for face in ('right', 'front', 'left', 'back'):
            for x in range(3):
                for y in range(5, 12):
                    if rnd.random() < 0.35 + 0.1 * (y - 5):
                        p.put(ra, face, x, y, GORE if y < 10 else GORE_D)
        cloth_rows(p, la, range(7, 10), LEATHER, LEATHER_D, rnd, ragged=False)
    elif kind == 'digger':
        # stripped to the waist for the heat of the drift: sackcloth breeches tied with rope, rags bound round the
        # hands, a strap across the chest for the wedges
        for b in (rl, ll):
            cloth_rows(p, b, range(0, 9), SACK, SACK_D, rnd)
        cloth_rows(p, body, range(9, 12), SACK, SACK_D, rnd, ragged=False)
        p.band(body, 9, ROPE)
        for b in (ra, la):
            cloth_rows(p, b, range(8, 11), LINEN, LINEN_D, rnd, ragged=False)
        for i in range(8):
            for face in ('front', 'back'):
                p.put(body, face, i, min(8, i + 1), LEATHER_D)
                p.put(body, face, i, min(8, i + 2), LEATHER)
        for x, y in ((2, 4), (4, 6)):
            p.put(body, 'front', x, y, IRON_L)
            p.put(body, 'front', x, y + 1, IRON_D)
    elif kind == 'barrowman':
        # a sack with holes for the head and arms, girt with rope; the yoke strap of the barrow over both shoulders
        cloth_rows(p, body, range(0, 12), SACK, SACK_D, rnd, ragged=False)
        for b in (rl, ll):
            cloth_rows(p, b, range(0, 6), SACK, SACK_D, rnd)
            cloth_rows(p, b, range(9, 12), LINEN_D, shade(LINEN_D, 0.8), rnd, ragged=False)
        p.band(body, 8, ROPE)
        for face in ('front', 'back'):
            for y in range(0, 8):
                p.put(body, face, 1, y, LEATHER if y % 3 else LEATHER_D)
                p.put(body, face, 6, y, LEATHER if y % 3 else LEATHER_D)
        for x in range(8):
            for z in range(4):
                p.put(body, 'top', x, z, LEATHER_D if x in (1, 6) else SACK)
        for b in (ra, la):
            cloth_rows(p, b, range(8, 11), LEATHER, LEATHER_D, rnd, ragged=False)
    elif kind == 'sapper':
        # a long leather coat buttoned to the neck against the sparks, black to the elbows with powder
        for b, rows in ((body, range(0, 12)), (ra, range(0, 9)), (la, range(0, 9))):
            cloth_rows(p, b, rows, LEATHER, LEATHER_D, rnd, ragged=False)
        for b in (rl, ll):
            cloth_rows(p, b, range(0, 5), LEATHER, LEATHER_D, rnd, ragged=False)
            cloth_rows(p, b, range(5, 12), BOOT, shade(BOOT, 0.8), rnd, ragged=False)
        for y in range(0, 12):
            p.put(body, 'front', 4, y, LEATHER_D)
            if y % 3 == 1:
                p.put(body, 'front', 3, y, BRASS)
        p.band(body, 8, shade(LEATHER_D, 0.8))
        # the straps of the keg, and a slow match wound round the left arm
        for face in ('front',):
            for y in range(0, 8):
                p.put(body, face, 1, y, ROPE)
                p.put(body, face, 6, y, ROPE)
        for y in (2, 4, 6):
            p.band(la, y, (190, 170, 120))
        for b in (ra, la):
            for face in ('right', 'front', 'left', 'back'):
                for x in range(3):
                    for y in range(9, 12):
                        p.put(b, face, x, y, shade(BLACK, 1.2 + 0.5 * (y == 9)))
        for x in range(8):
            for z in range(4):
                p.put(body, 'top', x, z, LEATHER)
    elif kind == 'watchman':
        # the night watch: a long dark coat with brass buttons, a wide belt, tall boots, gloves
        for b, rows in ((body, range(0, 12)), (ra, range(0, 9)), (la, range(0, 9)), (rl, range(0, 6)), (ll, range(0, 6))):
            cloth_rows(p, b, rows, COAT, COAT_D, rnd, ragged=False)
        for y in range(0, 12):
            p.put(body, 'front', 4, y, COAT_D)
            if y % 3 == 1 and y < 9:
                p.put(body, 'front', 3, y, BRASS)
                p.put(body, 'front', 5, y, shade(BRASS, 0.8))
        for x in (0, 1, 6, 7):
            p.put(body, 'front', x, 0, COAT_L)          # the collar turned up
            p.put(body, 'back', x, 0, COAT_L)
        for x in range(2, 6):
            p.put(body, 'back', x, 0, COAT_L)
        p.band(body, 8, LEATHER_D)
        p.band(body, 9, LEATHER)
        p.put(body, 'front', 3, 8, BRASS)
        p.put(body, 'front', 3, 9, BRASS)
        for b in (rl, ll):
            cloth_rows(p, b, range(6, 12), BOOT, shade(BOOT, 0.8), rnd, ragged=False)
            p.band(b, 6, LEATHER)
        for b in (ra, la):
            cloth_rows(p, b, range(9, 12), LEATHER_D, shade(LEATHER_D, 0.8), rnd, ragged=False)
            p.band(b, 8, COAT_L)
            for x in range(b.tw):
                for z in range(b.td):
                    p.put(b, 'top', x, z, COAT_L)
        for x in range(8):
            for z in range(4):
                p.put(body, 'top', x, z, COAT)

    if kind in QUARRY:
        dust(p, [body, ra, la, rl, ll, head], rnd)
    hood_c, hood_d, hood_l = (ROBE, ROBE_D, ROBE_L) if kind == 'acolyte' else (LEATHER, LEATHER_D, shade(LEATHER, 1.2)) if kind == 'sapper' else (HOOD, HOOD_D, HOOD_L)
    # ---- the second layer of the head: scraps of hair for the prisoner, nothing for the guard, a hood for the archer
    hat = box_of(M, 'hat')
    if kind in ('archer', 'acolyte', 'sapper'):
        def hood(face, px, py, fw, fh, r):
            if face == 'bottom':
                return None
            if face == 'front':
                # the face stays open: a frame of cloth around it and a shadow under the peak
                if 1 <= px <= 6 and py >= 2:
                    return shade(hood_d, 0.55) if py == 2 else None
                return hood_d if py == 1 or px in (0, 7) and py > 4 else hood_c
            if py == fh - 1 and face != 'top' and px % 3 == 1:
                return None
            if face == 'top':
                return hood_l if 2 <= px <= 5 and 2 <= py <= 5 else hood_c
            # soft folds running down from the crown
            c = hood_d if px in (2, 5) and py > 1 else hood_c
            return shade(c, 1.05 - 0.2 * py / max(1, fh - 1))
        p.fill_box(hat, hood)
    elif kind == 'prisoner':
        # a few last strands hanging at the back of the head
        for x, top, length in ((2, 3, 4), (5, 2, 5)):
            for y in range(top, top + length):
                p.put(hat, 'back', x, y, (58, 50, 46) if y % 2 else (46, 40, 37))

    elif kind in ('butcher', 'barrowman'):
        # a rag tied over the skull, stiff with the same blood (the barrowman's is only grey with dust)
        def wrap(face, px, py, fw, fh, r):
            if face == 'bottom':
                return None
            if face == 'top':
                return shade(LINEN if (px + py) % 3 else LINEN_D, 1.08)
            if py > 2 or py == 2 and (px + (face == 'left')) % 3 == 0:
                return None
            c = LINEN if (px * 2 + py) % 5 else LINEN_D
            if kind == 'butcher' and face in ('front', 'right') and r.random() < 0.3:
                c = GORE
            return c
        p.fill_box(hat, wrap)
        for y in range(3, 7):
            p.put(hat, 'back', 5, y, LINEN_D if y % 2 else LINEN)   # the end of the knot

    # ---- the extra parts
    for name in ('collar', 'shackle', 'kettle_hat', 'pauldron'):
        for b in M.parts[name]['boxes']:
            p.fill_box(b, iron)
    brim, dome = M.parts['kettle_hat']['boxes']
    if kind == 'watchman':
        # the same shape in felt: the wide hat of the watch, with a band and a brass badge
        def felt(face, px, py, fw, fh, r):
            c = COAT_D if (px + py) % 4 else shade(COAT_D, 0.85)
            return shade(c, {'top': 1.2, 'bottom': 0.7}.get(face, 1.0))
        p.fill_box(brim, felt)
        p.fill_box(dome, felt)
        p.band(dome, 2, LEATHER_D)
        p.put(dome, 'front', 4, 2, BRASS)
        p.put(dome, 'front', 4, 1, shade(BRASS, 0.8))
    else:
        p.band(dome, 2, IRON_D)
        for x in range(0, 9, 2):
            p.put(dome, 'front', x, 1, IRON_L)
            p.put(dome, 'back', x, 1, IRON_L)
        for x in range(12):
            for z in range(12):
                if 2 <= x <= 9 and 2 <= z <= 9:
                    p.put(brim, 'top', x, z, IRON_D)
    crown, peak = M.parts['cap']['boxes']

    def felt_cap(face, px, py, fw, fh, r):
        c = FELT if (px * 3 + py) % 5 else FELT_D
        if face not in ('top', 'bottom') and py == fh - 1:
            c = FELT_D
        return shade(c, {'top': 1.15, 'bottom': 0.7}.get(face, 1.0))
    p.fill_box(crown, felt_cap)
    p.fill_box(peak, lambda face, px, py, fw, fh, r: shade(FELT_D, 1.1 if face == 'top' else 0.85))
    bulk = M.parts['bulk']['boxes'][0]
    belly = M.parts['belly']['boxes'][0]
    flap = M.parts['apron']['boxes'][0]

    def linen(x, y, wide, high, r, low):
        """The apron: off-white, darker in its folds; old blood thick at the hem and where the hands are wiped, spatter above."""
        c = APRON if (x * 2 + y) % 5 else APRON_D
        if x in (0, wide - 1):
            c = APRON_D
        blood = 0.04 + 0.6 * max(0.0, (y - high * low) / max(1.0, high * (1 - low))) ** 1.5
        if abs(x - wide * 0.3) < 1.1 and abs(y - high * 0.45) < 1.6:
            blood = 0.75                                          # where he wipes his hand
        if r.random() < blood:
            c = GORE if r.random() < 0.65 else GORE_D
        return c

    def bulk_paint(face, px, py, fw, fh, r):
        if face in ('top', 'bottom'):
            return shade(SKIN, 1.06 if face == 'top' else 0.7)
        if face == 'front':
            if py >= 2 and 1 <= px <= fw - 2:
                return linen(px - 1, py - 2, fw - 2, fh - 2, r, 0.75)
            if py < 2 and px in (2, fw - 3):
                return LEATHER_D                                 # the straps
        if face == 'back':
            if px in (2, fw - 3) and py < 8 or py in (7, 8) and 2 <= px <= fw - 3:
                return LEATHER_D
        if py in (8, 9) and face in ('left', 'right'):
            return APRON_D if py == 8 else shade(APRON_D, 0.85)   # the apron's string round his middle
        c = flesh(face, px, py, fw, fh, r)
        if (px + py * 2) % 9 == 0:
            c = shade(SKIN_D, 1.0)
        if py >= fh - 2:
            c = shade(WOOL if (px + py) % 3 else WOOL_D, 1.0)   # the top of his breeches
        return c
    p.fill_box(bulk, bulk_paint)

    def belly_paint(face, px, py, fw, fh, r):
        if face == 'bottom':
            return shade(APRON_D, 0.7)
        if face == 'front':
            return linen(px, py + 3, fw, fh + 6, r, 0.5)
        return shade(APRON if (px + py) % 3 else APRON_D, {'top': 1.05}.get(face, 0.92))
    p.fill_box(belly, belly_paint)

    def flap_paint(face, px, py, fw, fh, r):
        if face in ('top', 'bottom'):
            return GORE_D
        if py == fh - 1 and px % 3 == 1:
            return None
        if face in ('front', 'back'):
            return linen(px, py + 7, fw, fh + 7, r, 0.4)
        return GORE_D if py > 2 else APRON_D
    p.fill_box(flap, flap_paint)
    for name in ('brawn_r', 'brawn_l'):
        box = M.parts[name]['boxes'][0]

        def brawn(face, px, py, fw, fh, r, name=name):
            c = flesh(face, px, py, fw, fh, r)
            if face not in ('top', 'bottom'):
                if py == fh - 1:
                    c = shade(SKIN_D, 0.9)
                if (px * 3 + py) % 7 == 0:
                    c = shade(SKIN_D, 1.05)
                if kind == 'butcher' and name == 'brawn_r' and py >= 4 and r.random() < 0.12 * (py - 3):
                    c = GORE
                if kind == 'barrowman':
                    c = mix(c, DUST, 0.15 + 0.4 * blotch_(name + face, px, py))
            return c
        p.fill_box(box, brawn)
    skirts = M.parts['coat_tail']['boxes'][0]

    def skirt(face, px, py, fw, fh, r):
        if face in ('top', 'bottom'):
            return COAT_D
        if py == fh - 1 and px % 3 == 1:
            return None
        dark, base = (LEATHER_D, LEATHER) if kind == 'sapper' else (COAT_D, COAT)
        c = dark if px in (0, fw - 1) or face == 'front' and px == 4 or face == 'back' and px in (3, 4) else base
        return shade(c, 1.0 - 0.1 * py / max(1, fh - 1))
    p.fill_box(skirts, skirt)
    pauldron = M.parts['pauldron']['boxes'][0]
    p.band(pauldron, 2, IRON_D)
    p.put(pauldron, 'right', 2, 1, IRON_L)
    p.put(pauldron, 'right', 3, 1, IRON_L)
    cuff, chain = M.parts['shackle']['boxes']
    for face in ('right', 'front', 'left', 'back'):
        for y in range(6):
            p.put(chain, face, 0, y, IRON_L if y % 2 else IRON_D)
    collar = M.parts['collar']['boxes'][0]
    p.put(collar, 'front', 4, 0, IRON_L)
    p.put(collar, 'front', 4, 1, IRON_D)

    def hood_cloth(face, px, py, fw, fh, r):
        return shade(hood_c if (px + py) % 3 else hood_d, {'top': 1.1, 'bottom': 0.75}.get(face, 1.0))
    for b in M.parts['hood_tail']['boxes']:
        p.fill_box(b, hood_cloth)
    quiver, fletch = M.parts['quiver']['boxes']

    def leather(face, px, py, fw, fh, r):
        c = LEATHER if (px + py * 2) % 4 else LEATHER_D
        return shade(c, {'top': 0.6, 'bottom': 0.7}.get(face, 1.0))
    p.fill_box(quiver, leather)
    p.band(quiver, 1, (150, 130, 96))
    p.band(quiver, 7, LEATHER_D)
    p.fill_box(fletch, lambda face, px, py, fw, fh, r: (200, 196, 184) if (px + py) % 2 else (150, 60, 50))
    # ---- the quarry's things
    skullcap, sconce, candle = M.parts['candle_cap']['boxes']

    def cap_leather(face, px, py, fw, fh, r):
        c = LEATHER if (px * 2 + py * 3) % 7 else LEATHER_D
        if face not in ('top', 'bottom') and py == fh - 1:
            c = LEATHER_D
        return mix(shade(c, {'top': 1.15, 'bottom': 0.7}.get(face, 1.0)), DUST, 0.3 if face == 'top' else 0.12)
    p.fill_box(skullcap, cap_leather)
    p.fill_box(sconce, iron)
    p.fill_box(candle, lambda face, px, py, fw, fh, r: FLAME if face == 'top' or py == 0 else (TALLOW if py > 1 else (250, 226, 150)))
    wheel, tray, load, handle_r, handle_l, leg_r, leg_l, fork_r, fork_l = M.parts['barrow']['boxes']

    def plank(face, px, py, fw, fh, r):
        along = px if fw >= fh else py
        across = py if fw >= fh else px
        c = PLANK_D if across % 3 == 2 or along % 6 == 5 else (PLANK_L if r.random() < 0.1 else PLANK)
        return mix(shade(c, {'top': 1.12, 'bottom': 0.68}.get(face, 1.0)), DUST, 0.18)
    for b in (tray, handle_r, handle_l, leg_r, leg_l, fork_r, fork_l):
        p.fill_box(b, plank)
    # the tray is open: its top shows the boards of its floor in shadow round the load
    fx, fy, fw, fh = tray.faces()['top']
    for x in range(fw):
        for y in range(fh):
            edge = x in (0, fw - 1) or y in (0, fh - 1)
            p.put(tray, 'top', x, y, shade(PLANK, 1.1) if edge else shade(PLANK_D, 0.6))
    p.band(tray, 0, IRON_D, every=1)
    for face in ('right', 'left'):
        for y in range(6):
            p.put(tray, face, 3, y, IRON_D)
            p.put(tray, face, 10, y, IRON_D)

    def wheel_paint(face, px, py, fw, fh, r):
        if face in ('right', 'left'):
            cx, cy = px - 3.5, py - 3.5
            d = (cx * cx + cy * cy) ** 0.5
            if d > 4.3:
                return None
            if d > 3.0:
                return IRON_D if (px + py) % 2 else IRON
            return IRON_L if d < 0.8 else (PLANK_D if abs(cx) < 0.6 or abs(cy) < 0.6 else PLANK)
        return IRON_D if (px + py) % 3 else IRON
    p.fill_box(wheel, wheel_paint)

    def stone(face, px, py, fw, fh, r):
        c = STONE_L if blotch_(f'load{face}', px, py) < 0.4 else STONE
        if face == 'top' and (px == 4 or py in (3, 6)):
            c = STONE_D                                      # the joints between the blocks
        if face not in ('top', 'bottom') and px % 4 == 3:
            c = STONE_D
        return shade(c, {'top': 1.1, 'bottom': 0.7}.get(face, 0.95))
    p.fill_box(load, stone)
    keg, fuse = M.parts['keg']['boxes']

    def keg_paint(face, px, py, fw, fh, r):
        if face in ('top', 'bottom'):
            return shade(PLANK_D if (px + py) % 2 else PLANK, 0.9 if face == 'top' else 0.6)
        if py in (1, fh - 2):
            return IRON_D if px % 2 else IRON
        c = PLANK_D if px % 2 else PLANK
        if face == 'back' and 2 <= px <= 3 and 3 <= py <= 4:
            c = BLACK                                        # the mark of the powder mill, burnt in
        return shade(c, 1.0 - 0.1 * py / fh)
    p.fill_box(keg, keg_paint)
    p.fill_box(fuse, lambda face, px, py, fw, fh, r: (255, 190, 80) if py == 0 and face != 'bottom' else ((190, 170, 120) if py % 2 else (150, 130, 90)))
    return p.tex


EXTRAS = ('collar', 'shackle', 'kettle_hat', 'pauldron', 'hood_tail', 'quiver', 'cap', 'bulk', 'belly', 'apron', 'brawn_r', 'brawn_l', 'coat_tail',
          'candle_cap', 'barrow', 'keg')
KIND_SHOW = {
    'prisoner': ('collar', 'shackle'),
    'guard': ('kettle_hat', 'pauldron'),
    'archer': ('hood_tail', 'quiver'),
    'acolyte': ('hood_tail',),
    'townsman': ('cap',),
    'butcher': ('bulk', 'belly', 'apron', 'brawn_r', 'brawn_l'),
    'watchman': ('kettle_hat', 'coat_tail'),
    'digger': ('candle_cap',),
    'barrowman': ('barrow', 'brawn_r', 'brawn_l'),
    'sapper': ('hood_tail', 'keg', 'coat_tail'),
}
KIND_HIDE = {kind: tuple(e for e in EXTRAS if e not in shown) for kind, shown in KIND_SHOW.items()}
STOOP = {'body': (0.24, 0, 0), 'right_leg': {'off': (0, -0.4, 2.9)}, 'left_leg': {'off': (0, -0.4, 2.9)}, 'head': (0.18, 0, 0),
         'right_arm': (-0.22, 0, 0.06), 'left_arm': (-0.22, 0, -0.06)}
SWING = dict(STOOP, right_arm=(-2.75, 0, 0.4), body=(0.12, 0.35, 0), head=(-0.07, 0, 0))
# the butcher carries himself straighter, his arms set wide of his bulk (see WitheredModel.setupAnim)
BULKY = {'body': (0.12, 0, 0), 'right_leg': {'off': (0, -0.1, 1.45)}, 'left_leg': {'off': (0, -0.1, 1.45)}, 'head': (0.14, 0, 0),
         'right_arm': {'rot': (-0.15, 0, 0.16), 'off': (-1.5, 0, 0)}, 'left_arm': {'rot': (-0.15, 0, -0.16), 'off': (1.5, 0, 0)}}
CLEAVE_DRAW = dict(BULKY, right_arm={'rot': (-1.35, -1.2, 0), 'off': (-1.5, 0, 0)}, body=(0.06, -0.6, 0))
CLEAVE_CUT = dict(BULKY, right_arm={'rot': (-1.35, 0.9, 0), 'off': (-1.5, 0, 0)}, body=(0.3, 0.6, 0))

PUSH = dict(STOOP, right_arm={'rot': (-0.5, 0, 0.0), 'off': (-1.5, 0, 0)}, left_arm={'rot': (-0.5, 0, 0.0), 'off': (1.5, 0, 0)}, body=(0.32, 0, 0))
PICK_UP = dict(STOOP, right_arm=(-2.7, 0, -0.35), left_arm=(-2.5, 0, 0.45), body=(0.0, 0, 0), head=(-0.2, 0, 0))
PICK_DOWN = dict(STOOP, right_arm=(-0.9, 0, -0.3), left_arm=(-0.8, 0, 0.4), body=(0.6, 0, 0), head=(0.1, 0, 0))


if __name__ == '__main__':
    M.write_java(JAVA)
    shots = []
    for i, kind in enumerate(('prisoner', 'guard', 'archer', 'acolyte', 'townsman', 'butcher', 'watchman', 'digger', 'barrowman', 'sapper')):
        tex = make(kind, 100 + i)
        tex.save(f'{A}/textures/entity/withered_{kind}.png')
        hidden = KIND_HIDE[kind]
        poses = ((-0.45, BULKY), (0.9, BULKY), (3.0, BULKY), (-0.6, CLEAVE_DRAW)) if kind == 'butcher' else ((-0.45, STOOP), (0.9, STOOP), (3.0, STOOP), (-0.6, SWING))
        poses = {'barrowman': ((-0.6, PUSH), (1.2, PUSH), (2.6, PUSH), (-1.2, PUSH)), 'digger': ((-0.45, STOOP), (0.9, STOOP), (-0.8, PICK_UP), (-1.2, PICK_DOWN))}.get(kind, poses)
        for yaw, pose in poses:
            shots.append(M.render(tex, yaw=yaw, pose=pose, size=(230, 330), zoom=8.5, centre=(0, 8), hidden=hidden))
    if len(sys.argv) > 1:
        sheet(shots, 4).save(sys.argv[1])
        tex = make('prisoner', 100)
        tex.resize((384, 1152), 0).save(sys.argv[1].replace('.png', '_tex.png'))
