#!/usr/bin/env python3
"""The Taskmaster of the quarry: mesh for TaskmasterModel.java, his texture and its glow layer.
Run from the repo root; pass a file name to also get a preview sheet."""
import sys

from modelkit import Box, Model, Painter, blotch, mix, shade, sheet

A = 'src/main/resources/assets/vsratie_dushi'
JAVA = 'src/main/java/com/keykeeper/dushi/client/TaskmasterModel.java'
SCALE = 1.25          # must match MODEL_SCALE in TaskmasterModel.java

M = Model(128, 128, SCALE)
# broad and bent: short thick legs, a barrel of a trunk under a leather apron, a bald head sunk between the shoulders
# with a cloth tied over the mouth against the dust and a tallow lamp strapped to the forehead; a belt hung with wedges;
# a splitting maul as long as himself
M.part('body', None, (0, 9, 0), boxes=[Box(-7, -4, -4.5, 14, 6, 9, 'leather', 'hips')])
M.part('apron', 'body', (0, 2, -4.5), (-0.06, 0, 0), boxes=[Box(-6, 0, -0.5, 12, 11, 1, 'leather', 'apron')])
M.part('wedges', 'body', (0, 1, 0), boxes=[Box(-8.5, -1, -2, 2, 5, 2, 'iron', 'wedge_a'), Box(-8.5, -1, 1, 2, 4, 2, 'iron', 'wedge_b'),
                                           Box(6.5, -1, -3, 2, 5, 2, 'iron', 'wedge_c'), Box(6.5, -1, 0, 2, 4, 2, 'iron', 'wedge_d')])
M.part('chest', 'body', (0, -4, 0), (0.3, 0, 0), boxes=[Box(-8.5, -13, -5, 17, 13, 10, 'skin', 'torso'), Box(-6.5, -15.5, -2, 13, 3, 8, 'skin', 'hump')])
M.part('head', 'chest', (0, -12.5, -3.5), (-0.22, 0, 0), boxes=[Box(-4, -8, -4.5, 8, 8, 8, 'skin', 'head'), Box(-1.5, -9, -6.5, 3, 3, 2, 'lamp', 'lamp')])
M.part('right_arm', 'chest', (-8.5, -11, 0), boxes=[Box(-6, -2.5, -3, 6, 12, 6, 'skin', 'upper')])
M.part('right_forearm', 'right_arm', (-3, 9.5, 0), boxes=[Box(-2.5, 0, -2.5, 5, 11, 5, 'wrap', 'forearm'), Box(-3, 10, -3, 6, 4, 6, 'skin', 'hand')])
M.part('maul', 'right_forearm', (0, 12, 0), (1.5, 0, 0), boxes=[
    Box(-1, -30, -1, 2, 40, 2, 'wood', 'shaft'), Box(-3.5, -41, -5.5, 7, 11, 11, 'iron', 'maul_head'), Box(-1.5, -31, -1.5, 3, 2, 3, 'iron', 'collar')])
M.mirror('right_arm', 'left_arm')
M.mirror('right_forearm', 'left_forearm')
M.part('right_leg', None, (-4, 11, 0), boxes=[Box(-3, 0, -3, 6, 7, 6, 'cloth', 'thigh')])
M.part('right_shin', 'right_leg', (0, 7, 0), boxes=[Box(-2.5, 0, -2.5, 5, 6, 5, 'cloth', 'shin'), Box(-3, 3, -5, 6, 3, 8, 'boot', 'boot')])
M.mirror('right_leg', 'left_leg')
M.mirror('right_shin', 'left_shin')
M.pack()

SKIN = (150, 138, 124)
SKIN_D = (116, 104, 94)
SKIN_DD = (84, 72, 66)
SKIN_L = (176, 166, 150)
DUST = (206, 202, 190)
LEATHER = (92, 64, 42)
LEATHER_D = (64, 44, 30)
LEATHER_L = (120, 88, 58)
IRON = (96, 96, 104)
IRON_D = (62, 62, 70)
IRON_L = (150, 150, 158)
WOOD = (104, 80, 54)
WOOD_D = (74, 56, 38)
CLOTH = (98, 92, 84)
CLOTH_D = (70, 66, 62)
BOOT = (44, 36, 32)
WRAP = (168, 158, 136)
WRAP_D = (126, 116, 98)
FLAME = (255, 220, 120)
FLAME_L = (255, 246, 190)
BLACK = (22, 18, 16)
EMBER = (255, 170, 60)


def dusted(c, face, px, py, seed):
    amount = 0.5 if face == 'top' else 0.08 + 0.3 * blotch(seed, px, py, 2.6)
    return mix(c, DUST, amount)


def skin(face, px, py, fw, fh, rnd):
    f = {'top': 1.08, 'bottom': 0.7}.get(face, 1.05 - 0.2 * py / max(1, fh - 1))
    amount = blotch(f'skin{fw}{fh}{face}', px, py, 3.0)
    c = SKIN_D if amount > 0.64 else (SKIN_L if amount < 0.2 else SKIN)
    if min(fw, fh) > 2 and px in (0, fw - 1):
        c = shade(c, 0.88)
    return dusted(shade(c, f), face, px, py, f'd{fw}{fh}{face}')


def leather(face, px, py, fw, fh, rnd):
    f = {'top': 1.1, 'bottom': 0.7}.get(face, 1.04 - 0.16 * py / max(1, fh - 1))
    amount = blotch(f'lea{fw}{fh}{face}', px, py, 3.0)
    c = LEATHER_D if amount > 0.62 else (LEATHER_L if amount < 0.18 else LEATHER)
    return dusted(shade(c, f), face, px, py, f'dl{fw}{fh}{face}')


def iron(face, px, py, fw, fh, rnd):
    f = {'top': 1.2, 'bottom': 0.7}.get(face, 1.0)
    amount = blotch(f'iron{fw}{fh}{face}', px, py, 2.4)
    c = IRON_D if amount > 0.62 else (IRON_L if amount < 0.16 else IRON)
    if min(fw, fh) > 3 and (px in (0, fw - 1) or py in (0, fh - 1)):
        c = shade(c, 0.8)
    return shade(c, f)


def wood(face, px, py, fw, fh, rnd):
    grain = px if fh >= fw else py
    return shade(WOOD_D if grain % 2 else WOOD, {'top': 1.1, 'bottom': 0.7}.get(face, 1.0))


def cloth(face, px, py, fw, fh, rnd):
    f = {'top': 1.1, 'bottom': 0.7}.get(face, 1.05 - 0.2 * py / max(1, fh - 1))
    return dusted(shade(CLOTH_D if px % 3 == 1 else CLOTH, f), face, px, py, f'dc{fw}{fh}{face}')


def wrap(face, px, py, fw, fh, rnd):
    c = WRAP_D if (py + px // 2) % 3 == 0 else WRAP
    return shade(c, {'top': 1.1, 'bottom': 0.7}.get(face, 1.02 - 0.12 * py / max(1, fh - 1)))


MATERIALS = {'skin': skin, 'leather': leather, 'iron': iron, 'wood': wood, 'cloth': cloth, 'wrap': wrap,
             'boot': lambda face, px, py, fw, fh, rnd: mix(shade(BOOT, {'top': 1.25, 'bottom': 0.7}.get(face, 1.0) * rnd.choice((0.95, 1.0, 1.08))), DUST,
                                                         0.35 if py >= fh - 1 or face == 'top' else 0.1)}

FACE = ["ssssssss",
        "sdssssds",
        "dKKddKKd",
        "sKEssEKs",
        "ccCccCcc",
        "cCccccCc",
        "ccCccCcc",
        "sccCCccs"]


def make():
    p = Painter(M, 83)
    boxes = {b.tag: b for _, _, b in M.boxes()}
    for _, _, b in M.boxes():
        if b.mat in MATERIALS:
            p.fill_box(b, MATERIALS[b.mat])
    # the head: small deep eyes with a spark in each, the kerchief over nose and mouth knotted behind, the lamp's strap
    head, lamp = boxes['head'], boxes['lamp']
    p.art(head, 'front', FACE, {'s': SKIN, 'd': SKIN_D, 'K': BLACK, 'E': EMBER, 'c': WRAP, 'C': WRAP_D}, glow_chars='E')
    for face in ('right', 'left', 'back'):
        fx, fy, fw, fh = head.faces()[face]
        for x in range(fw):
            for y in range(4, 7):
                p.put(head, face, x, y, WRAP_D if (x + y) % 3 == 0 else WRAP)
    for x, y in ((3, 7), (4, 7), (3, 8 - 1), (5, 6)):
        p.put(head, 'back', x, y, WRAP_D)
    p.band(head, 1, LEATHER_D)

    def lamp_paint(face, px, py, fw, fh, r):
        if face == 'front':
            return FLAME_L if (px, py) == (1, 1) else FLAME
        return IRON_D if face in ('top', 'bottom') or py == fh - 1 else IRON
    p.fill_box(lamp, lamp_paint)
    fx, fy, fw, fh = lamp.faces()['front']
    for x in range(fw):
        for y in range(fh):
            p.glow.putpixel((fx + x, fy + y), p.get(lamp, 'front', x, y)[:3] + (255,))
    # the trunk: the bib of the apron over a bare chest, its straps crossed on a back ridged with old weals
    torso, hips, apron = boxes['torso'], boxes['hips'], boxes['apron']
    fx, fy, fw, fh = torso.faces()['front']
    for x in range(4, 13):
        for y in range(3, 13):
            p.px(fx + x, fy + y, leather('front', x, y, fw, fh, None))
    for y in range(0, 4):
        p.put(torso, 'front', 4 + (3 - y) // 2, y, LEATHER_D)
        p.put(torso, 'front', 12 - (3 - y) // 2, y, LEATHER_D)
    for i in range(13):
        p.put(torso, 'back', min(16, 2 + i), i, LEATHER_D)
        p.put(torso, 'back', max(0, 14 - i), i, LEATHER_D)
    for x, y in ((3, 4), (4, 5), (5, 5), (10, 7), (11, 8), (12, 8), (6, 10), (7, 10)):
        p.put(torso, 'back', x, y, SKIN_DD)
    p.band(hips, 0, IRON_D)
    p.band(hips, 1, LEATHER_D)
    for face in ('front', 'back'):
        p.put(hips, face, 7, 0, IRON_L)
        p.put(hips, face, 7, 1, IRON)
    # the apron's skirt: scarred, its hem torn, white to the knee with dust
    fx, fy, fw, fh = apron.faces()['front']
    for x in range(fw):
        if (x * 5) % 7 < 2:
            for face in ('front', 'back'):
                ffx, ffy, _, _ = apron.faces()[face]
                p.clear(ffx + x, ffy + fh - 1)
    for x, y in ((2, 3), (3, 4), (4, 5), (8, 2), (8, 3), (9, 4)):
        p.put(apron, 'front', x, y, LEATHER_D)
    # the wedges at his belt: bright where the maul strikes them
    for tag in ('wedge_a', 'wedge_b', 'wedge_c', 'wedge_d'):
        b = boxes[tag]
        p.band(b, 0, IRON_L)
        p.band(b, -1, IRON_D)
    # the maul: a block of iron with a struck face at either end, bound to its shaft with a collar; the grip wound with rag
    mh, shaft = boxes['maul_head'], boxes['shaft']
    for face in ('front', 'back'):
        fx, fy, fw, fh = mh.faces()[face]
        for x in range(fw):
            for y in range(fh):
                edge = x in (0, fw - 1) or y in (0, fh - 1)
                p.px(fx + x, fy + y, IRON_D if edge else (IRON_L if blotch('face' + face, x, y, 1.6) > 0.45 else IRON))
    for face in ('right', 'left', 'top', 'bottom'):
        fx, fy, fw, fh = mh.faces()[face]
        for i in range(fh if face in ('right', 'left') else fw):
            if face in ('right', 'left'):
                p.px(fx + 1, fy + i, IRON_D)
                p.px(fx + fw - 2, fy + i, IRON_D)
            else:
                p.px(fx + i, fy + 1, IRON_D)
                p.px(fx + i, fy + fh - 2, IRON_D)
    for y in range(30, 40):
        p.band(shaft, y, WRAP_D if y % 2 else WRAP)
    # hands: knuckles like knots, the nails black
    for part in ('right_forearm', 'left_forearm'):
        forearm, hand = M.parts[part]['boxes']
        for face in ('front', 'back', 'right', 'left'):
            fx, fy, fw, fh = hand.faces()[face]
            for x in range(1, fw, 2):
                p.put(hand, face, x, fh - 1, SKIN_DD)
            for x in range(fw):
                p.put(hand, face, x, 0, SKIN_D)
    return p.tex, p.glow


# total pitch of the maul = arm + forearm + 1.5 + its own: 0 points it straight up, pi/2 forward, pi down
IDLE = {'right_arm': (-0.45, 0, 0.12), 'right_forearm': (-1.75, 0, 0), 'maul': (-0.1, 0, 0), 'left_arm': (0.1, 0, -0.12), 'left_forearm': (-0.3, 0, 0)}
SLAM_UP = dict(IDLE, right_arm=(-2.85, 0, 0.1), right_forearm=(-0.25, 0, 0), left_arm=(-2.6, 0, -0.35), left_forearm=(-0.4, 0, 0), chest=(-0.28, 0, 0),
               head=(-0.2, 0, 0), maul=(0.0, 0, 0))
SLAM_DOWN = dict(IDLE, right_arm=(-0.95, 0, 0.1), right_forearm=(-0.2, 0, 0), left_arm=(-0.9, 0, -0.35), left_forearm=(-0.3, 0, 0), chest=(0.55, 0, 0),
                 head=(0.2, 0, 0), maul=(0.75, 0, 0))
SWEEP = dict(IDLE, right_arm=(-1.35, -1.1, 0.1), right_forearm=(-0.15, 0, 0), chest=(0.1, -0.7, 0), maul=(1.1, 0, 0), left_arm=(-1.1, 0.5, -0.3))
WEDGE_SET = dict(IDLE, body={'off': (0, 5, 0)}, chest=(0.75, 0, 0), head=(-0.3, 0, 0), left_arm=(-0.9, 0, 0.2), left_forearm=(-0.5, 0, 0),
                 right_arm=(-2.3, 0, 0.1), right_forearm=(-0.6, 0, 0), maul=(0.2, 0, 0),
                 right_leg=(-1.3, 0, 0), right_shin=(1.5, 0, 0), left_leg=(-0.2, 0, 0), left_shin=(1.4, 0, 0))
THROW = dict(IDLE, left_arm=(-2.5, 0, -0.3), left_forearm=(-0.9, 0, 0), chest=(-0.1, 0.4, 0))

if __name__ == '__main__':
    M.write_java(JAVA) if len(sys.argv) < 3 else None
    tex, glow = make()
    tex.save(f'{A}/textures/entity/taskmaster.png')
    glow.save(f'{A}/textures/entity/taskmaster_glow.png')
    if len(sys.argv) > 1:
        shots = [M.render(tex, yaw=yaw, pose=pose, size=(340, 460), zoom=5.4, centre=(0, -12), glow=glow)
                 for yaw, pose in ((-0.5, IDLE), (0.9, IDLE), (3.0, IDLE), (-0.7, SLAM_UP), (-0.9, SLAM_DOWN), (-0.3, SWEEP), (-1.0, WEDGE_SET), (-0.5, THROW))]
        sheet(shots, 4).save(sys.argv[1])
