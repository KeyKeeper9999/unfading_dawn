#!/usr/bin/env python3
"""The Unhewn, a statue the masons never finished: mesh for UnhewnModel.java, its texture and the glow of its eyes.
Run from the repo root; pass a file name to also get a preview sheet."""
import sys

from modelkit import Box, Model, Painter, blotch, mix, shade, sheet

A = 'src/main/resources/assets/unfading_dawn'
JAVA = 'src/main/java/com/keykeeper/dushi/client/UnhewnModel.java'
SCALE = 1.1           # must match MODEL_SCALE in UnhewnModel.java

M = Model(128, 128, SCALE)
# a saint in the rough: the head, the halo's right half and the blessing right arm are finished work; the left arm is
# the squared block it was to be cut from, and below the waist there is only the quarry bed it stands on
M.part('base', None, (0, 24, 0), boxes=[Box(-8, -9, -7, 16, 9, 14, 'rough', 'plinth'), Box(-7, -15, -6, 14, 6, 12, 'rough', 'waist')])
M.part('torso', 'base', (0, -15, 0), boxes=[Box(-7, -14, -5, 14, 14, 10, 'carved', 'torso')])
M.part('head', 'torso', (0, -14, 0), (0.12, 0, 0), boxes=[Box(-4, -9, -4.5, 8, 9, 9, 'carved', 'head'), Box(-7, -13, 4, 14, 14, 1, 'carved', 'halo')])
M.part('right_arm', 'torso', (-7, -12, 0), (-0.25, 0, 0.08), boxes=[Box(-4, -1.5, -2.5, 4, 11, 5, 'carved', 'upper')])
M.part('right_forearm', 'right_arm', (-2, 9, 0), (-2.1, 0, 0), boxes=[Box(-1.5, 0, -1.5, 3, 8, 3, 'carved', 'fore'), Box(-2, 8, -1, 4, 5, 2, 'carved', 'hand')])
M.part('left_arm', 'torso', (7, -12, 0), (0, 0, -0.06), boxes=[Box(0, -2.5, -3.5, 7, 23, 7, 'rough', 'block')])
M.pack()

STONE = (206, 202, 190)
STONE_D = (170, 166, 156)
STONE_DD = (126, 122, 116)
STONE_L = (228, 226, 216)
MOSS = (120, 134, 96)
EYE = (255, 214, 130)


def carved(face, px, py, fw, fh, rnd):
    f = {'top': 1.08, 'bottom': 0.7}.get(face, 1.03 - 0.14 * py / max(1, fh - 1))
    amount = blotch(f'carved{fw}{fh}{face}', px, py, 4.0)
    c = STONE_D if amount > 0.72 else (STONE_L if amount < 0.25 else STONE)
    if min(fw, fh) > 2 and (px in (0, fw - 1) or py in (0, fh - 1)):
        c = shade(c, 0.9)
    return shade(c, f)


def rough(face, px, py, fw, fh, rnd):
    """Stone as the pick left it: rows of slanting strokes, a darker joint now and then, chips out of the edges."""
    f = {'top': 1.05, 'bottom': 0.66}.get(face, 1.0 - 0.14 * py / max(1, fh - 1))
    stroke = (px + py // 2 * 3) % 6
    c = shade(STONE_D, 0.88) if stroke == 0 else (STONE_D if stroke == 3 else shade(STONE, 0.95))
    if blotch(f'rough{fw}{fh}{face}', px, py, 3.2) > 0.7:
        c = shade(c, 0.86)
    if py % 7 == 6 and face not in ('top', 'bottom'):
        c = shade(STONE_DD, 1.05)
    return shade(c, f * rnd.choice((0.97, 1.0, 1.0, 1.03)))


FACE = ["........",
        "........",
        ".dd..dd.",
        "dEEddEEd",
        "...dd...",
        "...dd...",
        "..dDDd..",
        ".d....d.",
        "..dddd.."]


def make():
    p = Painter(M, 71)
    boxes = {b.tag: b for _, _, b in M.boxes()}
    for _, _, b in M.boxes():
        p.fill_box(b, carved if b.mat == 'carved' else rough)
    # the face: lids closed over the eyes (they shine through when it wakes), a straight nose, a small severe mouth,
    # hair in grooves; a crack down from the left temple
    head, halo = boxes['head'], boxes['halo']
    p.art(head, 'front', FACE, {'d': STONE_D, 'D': STONE_DD, 'E': STONE_DD})
    for x, y in ((1, 3), (2, 3), (5, 3), (6, 3)):
        fx, fy, fw, fh = head.faces()['front']
        p.glow.putpixel((fx + x, fy + y), EYE + (255,))
    for face in ('right', 'left', 'back', 'top'):
        fx, fy, fw, fh = head.faces()[face]
        for x in range(fw):
            for y in range(fh if face == 'top' else 5):
                if (x + (y // 2)) % 2 == 0:
                    p.put(head, face, x, y, STONE_D)
    for x, y in ((6, 0), (6, 1), (7, 2), (7, 3), (6, 4), (7, 5)):
        p.put(head, 'front', x, y, STONE_DD)
    # the halo: a disc, its left half still square and marked out for cutting
    for face in ('front', 'back'):
        fx, fy, fw, fh = halo.faces()[face]
        for x in range(fw):
            for y in range(fh):
                sx = x if face == 'front' else fw - 1 - x          # sx 0 = the statue's right
                d = ((sx - 6.5) ** 2 + (y - 6.5) ** 2) ** 0.5
                if sx < 7:
                    if d > 7.2:
                        p.clear(fx + x, fy + y)
                    elif d > 5.6:
                        p.px(fx + x, fy + y, STONE_L if (sx + y) % 3 else STONE)
                    elif d > 4.6:
                        p.px(fx + x, fy + y, STONE_D)
                else:
                    p.px(fx + x, fy + y, rough(face, x, y, fw, fh, __import__('random').Random(x * 31 + y)))
                    if abs(d - 6.4) < 0.5:
                        p.px(fx + x, fy + y, (84, 60, 50))             # the mason's red line
    for face in ('right', 'top', 'bottom'):
        fx, fy, fw, fh = halo.faces()[face]
        for x in range(fw):
            for y in range(fh):
                if face == 'right' or x < 7:
                    p.clear(fx + x, fy + y)
    # the torso: a robe in deep straight folds, a girdle, the right shoulder finished and the left only blocked out
    torso = boxes['torso']
    for face in ('front', 'back'):
        fx, fy, fw, fh = torso.faces()[face]
        for x in range(fw):
            sx = x if face == 'front' else fw - 1 - x
            for y in range(fh):
                if sx >= 9:
                    p.px(fx + x, fy + y, rough(face, x, y, fw, fh, __import__('random').Random(x * 17 + y)))
                elif sx in (2, 5, 7) and y > 2:
                    p.px(fx + x, fy + y, STONE_DD if y % 5 else STONE_D)
                elif sx in (3, 6) and y > 2:
                    p.px(fx + x, fy + y, STONE_L)
            if sx < 9:
                p.px(fx + x, fy + 9, STONE_DD)
                p.px(fx + x, fy + 10, STONE_L)
        if face == 'front':
            for x, y in ((3, 0), (4, 1), (5, 1), (6, 0), (4, 2), (5, 2)):
                p.px(fx + x, fy + y, STONE_D)                                 # the neck of the robe
    # moss in the lee of the plinth, and the wedge holes along its top edge where it was to be split from the bed
    plinth, waist = boxes['plinth'], boxes['waist']
    for face in ('right', 'front', 'left', 'back'):
        fx, fy, fw, fh = plinth.faces()[face]
        for x in range(fw):
            if blotch(f'moss{face}', x, 0, 2.0) > 0.5:
                for y in range(fh - 1 - int(blotch(f'mossh{face}', x, 3, 1.5) * 4), fh):
                    p.px(fx + x, fy + y, shade(MOSS, 0.85 + 0.3 * blotch('m', x, y, 1.0)))
            if x % 4 == 1:
                p.px(fx + x, fy + 1, (60, 58, 56))
                p.px(fx + x, fy + 2, STONE_DD)
    # the hand of the finished arm: fingers parted, two raised in blessing
    hand = boxes['hand']
    for face in ('front', 'back'):
        for y in range(2, 5):
            p.put(hand, face, 1, y, STONE_DD)
            p.put(hand, face, 2, y, STONE_L)
        p.put(hand, face, 3, 4, STONE_DD)
    upper = boxes['upper']
    p.band(upper, 8, STONE_DD)
    p.band(upper, 9, STONE_L)
    # the block arm: drill holes in a row on its outer face, a painted number
    block = boxes['block']
    for y in range(3, 21, 4):
        p.put(block, 'left', 3, y, (58, 56, 54))
    for x, y in ((2, 9), (3, 9), (4, 9), (3, 10), (3, 11), (2, 12), (3, 12), (4, 12)):
        p.put(block, 'front', x, y, (84, 60, 50))
    return p.tex, p.glow


REST = {}
ROCK = {'base': (0, 0, 0.13), 'torso': (0, 0, -0.1), 'left_arm': (0.3, 0, 0), 'right_arm': (0.2, 0, 0), 'head': (0, 0.3, 0)}
SMASH_UP = {'left_arm': (-2.85, 0, 0.1), 'torso': (-0.25, 0.2, 0), 'head': (-0.2, 0, 0), 'right_arm': (0.5, 0, 0)}
SMASH_DOWN = {'left_arm': (-1.0, 0, 0.1), 'torso': (0.5, -0.1, 0), 'head': (0.1, 0, 0), 'right_arm': (0.6, 0, 0), 'base': (0.08, 0, 0)}
SHOVE = {'right_arm': (-1.3, 0, 0), 'right_forearm': (1.9, 0, 0), 'torso': (0.2, 0.35, 0), 'left_arm': (0.4, 0, 0)}

if __name__ == '__main__':
    M.write_java(JAVA) if len(sys.argv) < 3 else None
    tex, glow = make()
    tex.save(f'{A}/textures/entity/unhewn.png')
    glow.save(f'{A}/textures/entity/unhewn_glow.png')
    if len(sys.argv) > 1:
        shots = [M.render(tex, yaw=yaw, pose=pose, size=(300, 400), zoom=7.2, centre=(0, 2), glow=g)
                 for yaw, pose, g in ((-0.5, REST, None), (0.0, REST, None), (0.8, REST, glow), (3.0, REST, None), (-0.4, ROCK, glow), (-0.9, SMASH_UP, glow),
                                      (-0.9, SMASH_DOWN, glow), (0.9, SHOVE, glow))]
        sheet(shots, 4).save(sys.argv[1])
