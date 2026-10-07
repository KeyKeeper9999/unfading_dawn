#!/usr/bin/env python3
"""The Unsleeping of the Black Firwood - the wakeful, the waker and the sleeper: one mesh for SectModel.java (each kind
shows its own parts of it), a texture for each kind and the glow of what burns on two of them.
Run from the repo root; pass a file name to also get a preview sheet."""
import math
import sys

from modelkit import Box, Model, Painter, blotch, mix, shade, sheet

A = 'src/main/resources/assets/unfading_dawn'
JAVA = 'src/main/java/com/keykeeper/dushi/client/SectModel.java'

M = Model(128, 64)
# People who swore not to sleep until morning, and the morning did not come. Gaunt, barefoot, in a long shift of white
# linen that hangs to the shins. The trunk turns about the hips, where the legs begin; the skirt of the shift hangs
# from the waist and is kept hanging whatever the trunk does; arms and legs have a joint each.
HIP = 11
M.part('right_leg', None, (-2, HIP, 0), boxes=[Box(-1.5, 0, -1.5, 3, 7, 3, 'skin', 'thigh')])
M.part('right_shin', 'right_leg', (0, 7, 0), boxes=[Box(-1.5, 0, -1.5, 3, 6, 3, 'skin', 'shin'), Box(-1.5, 5, -3.5, 3, 1, 2, 'skin', 'foot')])
M.mirror('right_leg', 'left_leg', parent=None)
M.mirror('right_shin', 'left_shin')
M.part('torso', None, (0, HIP, 0), (0.12, 0, 0), boxes=[Box(-4, -12, -2, 8, 12, 4, 'shift', 'chest')])
M.part('skirt', 'torso', (0, -1, 0), (-0.12, 0, 0), boxes=[Box(-4.5, 0, -2.5, 9, 10, 5, 'shift', 'skirt')])
M.part('head', 'torso', (0, -12, 0), (-0.06, 0, 0), boxes=[Box(-4, -8, -4, 8, 8, 8, 'head', 'head')])
M.part('right_arm', 'torso', (-5.5, -10.5, 0), boxes=[Box(-1.5, -1.5, -1.5, 3, 7, 3, 'sleeve', 'upper')])
M.part('right_forearm', 'right_arm', (0, 5.5, 0), (-0.15, 0, 0), boxes=[Box(-1.5, 0, -1.5, 3, 7, 3, 'sleeve', 'fore')])
M.mirror('right_arm', 'left_arm')
M.mirror('right_forearm', 'left_forearm')
# --- the wakeful: a head-cloth to the shoulders, a ring of candle ends burning on the crown of it, and in the right
# hand a candlestick as long as a spear with a candle alight at the far end. A held thing lies across the arm, pointing
# forward when the arm hangs: the stick is drawn standing and turned a quarter.
M.part('cloth', 'head', (0, 0, 0), boxes=[Box(-4.5, -8.5, -4.5, 9, 12, 9, 'cloth', 'cloth')])
M.part('crown', 'head', (0, -8.5, 0), boxes=[Box(-0.5, -4, -1.5, 1, 4, 1, 'wax', 'stub_a'), Box(-3.5, -3, 0.5, 1, 3, 1, 'wax', 'stub_b'), Box(2.5, -2, -0.5, 1, 2, 1, 'wax', 'stub_c'),
                                               Box(-0.5, -6, -1.5, 1, 2, 1, 'flame', 'flame_a'), Box(-3.5, -5, 0.5, 1, 2, 1, 'flame', 'flame_b'),
                                               Box(2.5, -4, -0.5, 1, 2, 1, 'flame', 'flame_c')])
M.part('stick', 'right_forearm', (0, 6, 0), (math.pi / 2, 0, 0), boxes=[Box(-0.5, -19, -0.5, 1, 25, 1, 'iron', 'shaft'), Box(-1.5, -20, -1.5, 3, 1, 3, 'iron', 'pan'),
                                                                        Box(-1, -24, -1, 2, 4, 2, 'wax', 'candle'), Box(-0.5, -26, -0.5, 1, 2, 1, 'flame', 'flame_s')])
# --- the waker: a dark cowl; the board he beats - a semantron, a plank as long as he is tall - on his left shoulder;
# the mallet he beats it with in his right hand; a lantern at his belt.
M.part('cowl', 'head', (0, 0, 0), boxes=[Box(-4.5, -8.5, -4.5, 9, 10, 9, 'cowl', 'cowl', inflate=0.2)])
M.part('board', 'torso', (4.5, -13.2, 0), (math.pi / 2 - 0.16, 0, 0), boxes=[Box(-1, -11, -1.5, 2, 24, 3, 'board', 'board')])
M.part('mallet', 'right_forearm', (0, 6, 0), (math.pi / 2, 0, 0), boxes=[Box(-0.5, -12, -0.5, 1, 15, 1, 'haft', 'haft'), Box(-1.5, -15, -2.5, 3, 3, 5, 'board', 'mallet')])
M.part('lantern', 'torso', (-3, -1, -2.5), (-0.12, 0, 0), boxes=[Box(-1.5, 0, -1.5, 3, 4, 3, 'lantern', 'lantern'), Box(-0.5, -1, -0.5, 1, 1, 1, 'iron', 'ring')])
# --- the sleeper: one who fell asleep. They tied such to a stake to keep them standing; this one tore the stake out
# of the ground and walks with it still lashed along his back.
M.part('stake', 'torso', (0, 0, 2.6), (0.05, 0, 0.04), boxes=[Box(-1, -19, 0, 2, 23, 2, 'stake', 'stake')])
M.part('ropes', 'torso', (0, 0, 0), boxes=[Box(-4.5, -10, -2.5, 9, 2, 7, 'rope', 'rope_a'), Box(-4.5, -4, -2.5, 9, 1, 7, 'rope', 'rope_b')])
M.pack()

KINDS = ('wakeful', 'waker', 'sleeper')
PARTS = {'wakeful': ('cloth', 'crown', 'stick'), 'waker': ('cowl', 'board', 'mallet', 'lantern'), 'sleeper': ('stake', 'ropes')}

SKIN = (150, 142, 128)
SKIN_D = (112, 104, 94)
SKIN_DD = (78, 70, 66)
LINEN = (222, 216, 200)
LINEN_D = (180, 172, 156)
LINEN_DD = (138, 130, 118)
SOIL = (92, 82, 66)
WAXC = (236, 226, 190)
WAX_D = (196, 180, 136)
FLAME = (255, 214, 120)
FLAME_L = (255, 246, 208)
FLAME_D = (255, 150, 52)
IRON = (74, 74, 82)
IRON_D = (46, 46, 54)
IRON_L = (112, 112, 122)
COAT = (56, 54, 58)
COAT_D = (36, 34, 40)
COAT_L = (80, 78, 82)
WOOD = (124, 96, 62)
WOOD_D = (88, 66, 42)
WOOD_L = (160, 130, 88)
ROPE = (118, 96, 60)
ROPE_D = (84, 66, 42)
BLACK = (16, 14, 16)
EYE = (236, 240, 232)
GLINT = (255, 208, 130)


def linen(kind, tag):
    """The shift: white once. A wakeful's is whole, with candle wax down the front; a waker wears a dark coat over
    his; a sleeper's is torn at the hem and brown with the ground he has lain on."""
    def fn(face, px, py, fw, fh, rnd):
        if kind == 'waker':
            f = {'top': 1.1, 'bottom': 0.7}.get(face, 1.02 - 0.2 * py / max(1, fh - 1))
            c = COAT_D if blotch(f'coat{tag}{face}', px, py * 0.6, 2.2) > 0.62 else (COAT_L if rnd.random() < 0.08 else COAT)
            if tag == 'skirt' and py >= fh - 2:
                c = LINEN_D if (px + py) % 2 else LINEN_DD       # the shift shows under the coat
            if tag == 'chest' and face == 'front' and px in (fw // 2 - 1, fw // 2):
                c = COAT_D                                       # where the coat closes
            return shade(c, f)
        f = {'top': 1.08, 'bottom': 0.72}.get(face, 1.03 - 0.14 * py / max(1, fh - 1))
        c = LINEN_D if blotch(f'lin{kind}{tag}{face}', px, py * 0.5, 2.0) > 0.6 else LINEN
        if px % 3 == 1 and face not in ('top', 'bottom') and tag == 'skirt':
            c = mix(c, LINEN_DD, 0.35)                           # the folds of it
        dirt = 0.42 if kind == 'sleeper' else 0.16
        if face not in ('top', 'bottom') and tag in ('skirt', 'fore') and py >= fh * (1 - dirt * 1.6) and blotch(f'soil{kind}{tag}{face}', px, py, 1.6) > 0.4:
            c = mix(c, SOIL, 0.7 if kind == 'sleeper' else 0.4)
        if kind == 'sleeper':
            if blotch(f'stain{tag}{face}', px, py, 2.6) > 0.66:
                c = mix(c, SOIL, 0.55)
            if tag == 'skirt' and face not in ('top', 'bottom') and py >= fh - 3 and blotch(f'tear{face}', px, 0, 1.0) > (0.72 - 0.16 * (py - fh + 3)):
                return None                                      # the hem in rags
        if kind == 'wakeful' and tag == 'chest' and face == 'front' and px in (2, 5) and py < 3 + (px % 3) * 2:
            c = WAXC                                             # wax run down from the crown
        return shade(c, f)
    return fn


def skin(face, px, py, fw, fh, rnd):
    f = {'top': 1.08, 'bottom': 0.7}.get(face, 1.0 - 0.1 * py / max(1, fh - 1))
    c = SKIN_D if blotch(f'skin{fw}{fh}{face}', px, py * 0.6, 1.8) > 0.6 else SKIN
    return shade(c, f)


def wood(face, px, py, fw, fh, rnd):
    c = WOOD_D if (py + px // 2) % 5 == 0 or blotch(f'wood{fw}{fh}{face}', px, py * 0.3, 1.5) > 0.66 else (WOOD_L if rnd.random() < 0.1 else WOOD)
    return shade(c, {'top': 1.1, 'bottom': 0.72}.get(face, 1.0))


def flame(face, px, py, fw, fh, rnd):
    if face == 'bottom':
        return FLAME_D
    return FLAME_L if py >= fh - 1 and face != 'top' else (FLAME if face != 'top' else FLAME_D)


def make(kind):
    p = Painter(M, 141 + KINDS.index(kind))
    boxes = {b.tag: b for _, _, b in M.boxes()}
    for name, _, b in M.boxes():
        if b.mat in ('shift', 'sleeve'):
            p.fill_box(b, linen(kind, b.tag))
        elif b.mat == 'skin':
            p.fill_box(b, skin)
        elif b.mat == 'head':
            p.fill_box(b, skin)
        elif b.mat == 'cloth':
            def cloth(face, px, py, fw, fh, rnd):
                if face == 'bottom':
                    return None
                if face == 'front' and 1 <= px <= fw - 2 and 2 <= py <= 8:
                    return None                                  # the face looks out of it
                if face != 'top' and py >= fh - 2 and (px + (0 if face in ('front', 'back') else 1)) % 3 == 0:
                    return None                                  # its edge hangs uneven
                c = LINEN_D if blotch(f'cl{face}', px, py * 0.5, 2.0) > 0.58 or (face != 'top' and px % 4 == 2) else LINEN
                return shade(c, {'top': 1.1}.get(face, 1.04 - 0.012 * py))
            p.fill_box(b, cloth)
        elif b.mat == 'cowl':
            def cowl(face, px, py, fw, fh, rnd):
                if face == 'bottom':
                    return None
                if face == 'front' and 1 <= px <= fw - 2 and 3 <= py <= 8:
                    return None                                  # the hollow of the cowl; the face is far back in it
                c = COAT_D if blotch(f'cw{face}', px, py * 0.5, 2.0) > 0.55 else COAT
                return shade(c, {'top': 1.12}.get(face, 1.0 - 0.012 * py))
            p.fill_box(b, cowl)
        elif b.mat == 'wax':
            p.fill_box(b, lambda face, px, py, fw, fh, r: shade(WAXC if (px + py) % 3 else WAX_D, {'top': 1.05, 'bottom': 0.8}.get(face, 1.0 - 0.03 * py)))
        elif b.mat == 'flame':
            p.fill_box(b, flame)
        elif b.mat == 'iron':
            p.fill_box(b, lambda face, px, py, fw, fh, r: shade(IRON_D if (py % 6 == 2 and fh > 6) else (IRON_L if r.random() < 0.12 else IRON), {'top': 1.15, 'bottom': 0.7}.get(face, 1.0)))
        elif b.mat in ('board', 'haft', 'stake'):
            p.fill_box(b, wood)
        elif b.mat == 'rope':
            def rope(face, px, py, fw, fh, rnd):
                if face in ('top', 'bottom'):
                    return None
                return shade(ROPE if (px + py) % 2 else ROPE_D, 1.0)
            p.fill_box(b, rope)
        elif b.mat == 'lantern':
            def lantern(face, px, py, fw, fh, rnd):
                if face in ('top', 'bottom') or py in (0, fh - 1) or px in (0, fw - 1):
                    return IRON if face != 'bottom' else IRON_D
                return FLAME
            p.fill_box(b, lantern)
    head = boxes['head']
    pal = {'s': SKIN, 'd': SKIN_D, 'D': SKIN_DD, 'B': BLACK, 'E': EYE, 'G': GLINT, 'h': (96, 90, 84)}
    if kind == 'wakeful':
        # the eyes do not shut: wide, lidless, ringed with the dark of every night since
        p.art(head, 'front', ["dddddddd",
                              "dssssssd",
                              "sDDssDDs",
                              "DEBDDBED",
                              "sDDssDDs",
                              "ssdssdss",
                              "sdBBBBds",
                              "ddssssdd"], pal, glow_chars='E')
    elif kind == 'waker':
        p.art(head, 'front', ["DDDDDDDD",
                              "DDDDDDDD",
                              "DDDDDDDD",
                              "DGBDDBGD",
                              "DDDDDDDD",
                              "dDDddDDd",
                              "sdBBBBds",
                              "sdssssds"], pal, glow_chars='G')
    else:
        # asleep: the lids down, the mouth fallen open, the hair grown out and matted
        p.art(head, 'front', ["hhhhhhhh",
                              "hhshhshh",
                              "hsssssss",
                              "sDDssDDs",
                              "sBBssBBs",
                              "ssdssdss",
                              "sdBBBBds",
                              "sdBBBBds"], pal)
        for face in ('top', 'back', 'right', 'left'):
            fx, fy, fw, fh = head.faces()[face]
            for x in range(fw):
                for y in range(fh if face in ('top', 'back') else 4):
                    if face == 'top' or y < 3 + (x * 7) % 4:
                        p.px(fx + x, fy + y, shade(pal['h'], 0.85 + 0.25 * blotch(f'hair{face}', x, y, 1.4)))
    # what gives light: every flame; a wakeful's eyes a little (they caught the light long ago and kept it)
    for _, _, b in M.boxes():
        if b.mat == 'flame' or b.mat == 'lantern':
            for face, (fx, fy, fw, fh) in b.faces().items():
                for x in range(fw):
                    for y in range(fh):
                        r_, g_, b_, a_ = p.tex.getpixel((fx + x, fy + y))
                        if a_ and r_ > 200:
                            p.glow.putpixel((fx + x, fy + y), (r_, g_, b_, 255))
    return p.tex, p.glow


def hidden(kind):
    return tuple(part for k, parts in PARTS.items() if k != kind for part in parts)


# The poses of SectModel.setupAnim, as it sets them (rotations added to the rest pose).
# -- the wakeful: kneeling at its vigil, the candlestick upright before it in both hands; standing; the blow from over
# the head; the thrust with the burning end
KNEEL = {'right_leg': {'off': (0, 7, 0)}, 'left_leg': {'off': (0, 7, 0)}, 'right_shin': (1.5, 0, 0), 'left_shin': (1.5, 0, 0), 'torso': {'off': (0, 7, 0), 'rot': (0.1, 0, 0)},
         'skirt': (-0.1, 0, 0), 'head': (0.3, 0, 0), 'right_arm': (-0.75, 0, -0.35), 'right_forearm': (-0.75, 0, 0), 'left_arm': (-0.7, 0, 0.5), 'left_forearm': (-0.9, 0, 0)}
WAKEFUL_STAND = {'right_arm': (-0.75, 0, -0.1), 'right_forearm': (-0.6, 0, 0), 'left_arm': (0.1, 0, 0)}
STRIKE_UP = {'torso': (-0.2, 0, 0), 'skirt': (0.2, 0, 0), 'right_arm': (-2.5, 0, -0.1), 'right_forearm': (-0.3, 0, 0), 'left_arm': (-2.3, 0, 0.3), 'left_forearm': (-0.5, 0, 0)}
STRIKE_DOWN = {'torso': (0.5, 0, 0), 'skirt': (-0.5, 0, 0), 'right_arm': (-0.5, 0, -0.1), 'right_forearm': (0.0, 0, 0), 'left_arm': (-0.6, 0, 0.3), 'left_forearm': (-0.3, 0, 0),
               'right_leg': (-0.4, 0, 0), 'left_leg': (0.3, 0, 0)}
THRUST = {'torso': (0.3, -0.3, 0), 'skirt': (-0.3, 0, 0), 'right_arm': (0.25, 0, 0.1), 'right_forearm': (-0.05, 0, 0), 'left_arm': (-0.9, 0, 0.2), 'right_leg': (0.4, 0, 0),
          'left_leg': (-0.5, 0, 0)}
# -- the waker: walking his round, the mallet hanging; beating the board; the mallet round level; the mallet down
WAKER_STAND = {'left_arm': (-2.3, 0, 0.25), 'left_forearm': (-0.7, 0, 0)}
KNOCK = {'left_arm': (-2.3, 0, 0.25), 'left_forearm': (-0.7, 0, 0), 'right_arm': (-2.2, 0.0, -0.45), 'right_forearm': (-0.9, 0, 0), 'head': (-0.2, 0, 0)}
SWING = {'left_arm': (-2.3, 0, 0.25), 'left_forearm': (-0.7, 0, 0), 'torso': (0.15, -0.6, 0), 'right_arm': (-1.3, -0.8, 0), 'right_forearm': (-0.2, 0, 0)}
SMASH_UP = {'left_arm': (-2.3, 0, 0.25), 'left_forearm': (-0.7, 0, 0), 'torso': (-0.25, 0, 0), 'skirt': (0.25, 0, 0), 'right_arm': (-2.9, 0, 0), 'right_forearm': (-0.5, 0, 0)}
# -- the sleeper: walking in his sleep; the flurry; fallen on his face
SLEEPWALK = {'torso': (0.25, 0, 0.06), 'skirt': (-0.25, 0, 0), 'head': (0.35, 0.2, 0.5), 'right_arm': (-1.2, 0, 0.1), 'left_arm': (-1.0, 0, -0.15), 'right_forearm': (-0.1, 0, 0),
             'left_forearm': (-0.3, 0, 0), 'right_leg': (-0.3, 0, 0), 'left_leg': (0.25, 0, 0)}
FLAIL = {'torso': (0.45, 0.5, 0), 'skirt': (-0.45, 0, 0), 'head': (0.2, -0.3, 0.5), 'right_arm': (-2.3, 0, 0.3), 'left_arm': (-0.6, 0, -0.5), 'right_forearm': (-0.6, 0, 0),
         'right_leg': (-0.6, 0, 0), 'left_leg': (0.5, 0, 0)}
FALLEN = {'torso': {'off': (0, 9, -4), 'rot': (1.35, 0, 0)}, 'skirt': (-0.2, 0, 0), 'head': (-0.5, 0.6, 0), 'right_arm': (-2.6, 0, 0.3), 'left_arm': (-2.4, 0, -0.4),
          'right_leg': {'off': (0, 9, -4), 'rot': (1.45, 0, 0.1)}, 'left_leg': {'off': (0, 9, -4), 'rot': (1.3, 0, -0.1)}}

if __name__ == '__main__':
    M.write_java(JAVA) if len(sys.argv) < 3 else None
    shots = []
    for kind in KINDS:
        tex, glow = make(kind)
        tex.save(f'{A}/textures/entity/{kind}.png')
        if kind != 'sleeper':
            glow.save(f'{A}/textures/entity/{kind}_glow.png')
        poses = {'wakeful': ((-0.5, WAKEFUL_STAND), (0.0, WAKEFUL_STAND), (2.6, WAKEFUL_STAND), (-0.6, KNEEL), (-1.4, KNEEL), (-1.2, STRIKE_UP), (-1.3, STRIKE_DOWN), (-1.3, THRUST)),
                 'waker': ((-0.5, WAKER_STAND), (0.0, WAKER_STAND), (2.6, WAKER_STAND), (1.4, WAKER_STAND), (-0.7, KNOCK), (-0.6, SWING), (-1.2, SMASH_UP), (-1.57, WAKER_STAND)),
                 'sleeper': ((-0.5, {}), (0.0, SLEEPWALK), (2.6, SLEEPWALK), (-1.3, SLEEPWALK), (-0.7, FLAIL), (-1.3, FLAIL), (-1.3, FALLEN), (-0.4, FALLEN))}[kind]
        if len(sys.argv) > 1:
            shots += [M.render(tex, yaw=yaw, pose=pose, size=(250, 330), zoom=7.0, centre=(0, 6), glow=glow, hidden=hidden(kind), background=(52, 62, 60))
                      for yaw, pose in poses]
    if len(sys.argv) > 1:
        sheet(shots, 8).save(sys.argv[1])
