#!/usr/bin/env python3
"""The withered (prisoner, guard, archer): mesh extras for WitheredModel.java and the three textures.
Run from the repo root; pass a file name to also get a preview sheet."""
import random
import sys

from humanoid import box_of, humanoid
from modelkit import Box, Painter, mix, shade, sheet

A = 'src/main/resources/assets/vsratie_dushi'
JAVA = 'src/main/java/com/keykeeper/dushi/client/WitheredModel.java'

# ---------------------------------------------------------------- mesh
M = humanoid(96, slim_arms=True)
# prisoner: an iron collar and a shackle with a few links of chain still on the left wrist
M.part('collar', 'body', (0, 0, 0), boxes=[Box(-4.5, -0.6, -2.5, 9, 2, 5, 'iron', 'collar')])
M.part('shackle', 'left_arm', (0.5, 7, 0), boxes=[Box(-2, 0, -2.5, 4, 2, 5, 'iron', 'cuff'), Box(-0.5, 2, -3.3, 1, 6, 1, 'iron', 'chain')])
# guard: a kettle hat and a plate on the sword shoulder
M.part('kettle_hat', 'head', (0, 0, 0), boxes=[Box(-6, -6.6, -6, 12, 1, 12, 'iron', 'brim'), Box(-4.5, -9.6, -4.5, 9, 3, 9, 'iron', 'dome')])
M.part('pauldron', 'right_arm', (0, 0, 0), boxes=[Box(-3, -3, -3, 4, 3, 6, 'iron', 'pauldron')])
# archer: the tail of the hood and a quiver
M.part('hood_tail', 'head', (0, -6.5, 4), (0.45, 0, 0), boxes=[Box(-2, -1, -1, 4, 4, 3, 'hood', 'tail'), Box(-1, 3, 0.5, 2, 4, 1, 'hood', 'tip')])
M.part('quiver', 'body', (2.2, 1, 2), (0, 0, 0.32), boxes=[Box(-1.5, 0, 0, 3, 9, 2, 'leather', 'quiver'), Box(-1, -2, 0.5, 2, 2, 1, 'fletch', 'fletch')])
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

    # ---- the second layer of the head: scraps of hair for the prisoner, nothing for the guard, a hood for the archer
    hat = box_of(M, 'hat')
    if kind == 'archer':
        def hood(face, px, py, fw, fh, r):
            if face == 'bottom':
                return None
            if face == 'front':
                # the face stays open: a frame of cloth around it and a shadow under the peak
                if 1 <= px <= 6 and py >= 2:
                    return shade(HOOD_D, 0.55) if py == 2 else None
                return HOOD_D if py == 1 or px in (0, 7) and py > 4 else HOOD
            if py == fh - 1 and face != 'top' and px % 3 == 1:
                return None
            if face == 'top':
                return HOOD_L if 2 <= px <= 5 and 2 <= py <= 5 else HOOD
            # soft folds running down from the crown
            c = HOOD_D if px in (2, 5) and py > 1 else HOOD
            return shade(c, 1.05 - 0.2 * py / max(1, fh - 1))
        p.fill_box(hat, hood)
    elif kind == 'prisoner':
        # a few last strands hanging at the back of the head
        for x, top, length in ((2, 3, 4), (5, 2, 5)):
            for y in range(top, top + length):
                p.put(hat, 'back', x, y, (58, 50, 46) if y % 2 else (46, 40, 37))

    # ---- the extra parts
    for name in ('collar', 'shackle', 'kettle_hat', 'pauldron'):
        for b in M.parts[name]['boxes']:
            p.fill_box(b, iron)
    brim, dome = M.parts['kettle_hat']['boxes']
    p.band(dome, 2, IRON_D)
    for x in range(0, 9, 2):
        p.put(dome, 'front', x, 1, IRON_L)
        p.put(dome, 'back', x, 1, IRON_L)
    for x in range(12):
        for z in range(12):
            if 2 <= x <= 9 and 2 <= z <= 9:
                p.put(brim, 'top', x, z, IRON_D)
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
        return shade(HOOD if (px + py) % 3 else HOOD_D, {'top': 1.1, 'bottom': 0.75}.get(face, 1.0))
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
    return p.tex


KIND_HIDE = {
    'prisoner': ('kettle_hat', 'pauldron', 'hood_tail', 'quiver'),
    'guard': ('collar', 'shackle', 'hood_tail', 'quiver'),
    'archer': ('collar', 'shackle', 'kettle_hat', 'pauldron'),
}
STOOP = {'body': (0.24, 0, 0), 'right_leg': {'off': (0, -0.4, 2.9)}, 'left_leg': {'off': (0, -0.4, 2.9)}, 'head': (0.18, 0, 0),
         'right_arm': (-0.22, 0, 0.06), 'left_arm': (-0.22, 0, -0.06)}
SWING = dict(STOOP, right_arm=(-2.75, 0, 0.4), body=(0.12, 0.35, 0), head=(-0.07, 0, 0))


if __name__ == '__main__':
    M.write_java(JAVA)
    shots = []
    for i, kind in enumerate(('prisoner', 'guard', 'archer')):
        tex = make(kind, 100 + i)
        tex.save(f'{A}/textures/entity/withered_{kind}.png')
        hidden = KIND_HIDE[kind]
        for yaw, pose in ((-0.45, STOOP), (0.9, STOOP), (3.0, STOOP), (-0.6, SWING)):
            shots.append(M.render(tex, yaw=yaw, pose=pose, size=(230, 330), zoom=8.5, centre=(0, 8), hidden=hidden))
    if len(sys.argv) > 1:
        sheet(shots, 4).save(sys.argv[1])
        tex = make('prisoner', 100)
        tex.resize((512, 768), 0).save(sys.argv[1].replace('.png', '_tex.png'))
