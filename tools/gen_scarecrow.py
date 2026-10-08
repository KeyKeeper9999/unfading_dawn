#!/usr/bin/env python3
"""The scarecrows of the Stubble - the one that gets down off its stake, and the Scorched Warden, which is the same
thing three men high: one mesh for ScarecrowModel.java (the Warden shows the poles it has for arms; the model is drawn
larger for it), a texture for each, and the glow of the Warden alight.
Run from the repo root; pass a file name to also get a preview sheet."""
import math
import sys

from modelkit import Box, Model, Painter, blotch, mix, shade, sheet

A = 'src/main/resources/assets/unfading_dawn'
JAVA = 'src/main/java/com/keykeeper/dushi/client/ScarecrowModel.java'

M = Model(128, 64)
# A coat stuffed with straw on a cross of two poles: the upright goes down its back and (while it is still on it) into
# the ground, the crossbar through its sleeves - its arms are bound out along it and do not bend. A sack for a head,
# somebody's hat. Everything hangs from 'all', at the hips; on the stake the whole of it is drawn higher (see the model).
HIP = 14
M.part('all', None, (0, HIP, 0))
M.part('stake', 'all', (0, 0, 0), boxes=[Box(-1, 0, 1.5, 2, 15, 2, 'pole', 'stake')])
M.part('right_leg', 'all', (-2, 0, 0), boxes=[Box(-1.5, 0, -1.5, 3, 9, 3, 'trouser', 'leg'), Box(-2, 8, -2, 4, 2, 4, 'straw', 'foot')])
M.mirror('right_leg', 'left_leg', parent='all')
M.part('torso', 'all', (0, 0, 0), boxes=[Box(-4, -12, -2.5, 8, 12, 5, 'coat', 'chest'), Box(-1, -17, 2, 2, 17, 2, 'pole', 'post'), Box(-14, -11, 1.5, 28, 2, 2, 'pole', 'bar')])
M.part('skirt', 'torso', (0, -1, 0), boxes=[Box(-4.5, 0, -3, 9, 5, 6, 'coat', 'skirt')])
M.part('head', 'torso', (0, -12, 0), boxes=[Box(-3.5, -7, -3.5, 7, 7, 7, 'sackhead', 'head'), Box(-4, -9, -4, 8, 2, 8, 'hat', 'crown'),
                                             Box(-6, -7.5, -6, 12, 1, 12, 'hat', 'brim')])
M.part('right_arm', 'torso', (-4, -10, 0), boxes=[Box(-9, -1.5, -1.5, 9, 3, 3, 'coat', 'sleeve')])
M.part('right_hand', 'right_arm', (-9, 0, 0), boxes=[Box(-6, -1, -1, 6, 2, 2, 'rods', 'rods'), Box(-9, -2, -0.5, 4, 1, 1, 'rods', 'twig_a'), Box(-9, 1, -0.5, 4, 1, 1, 'rods', 'twig_b'),
                                                     Box(-8, -0.5, 1, 3, 1, 1, 'rods', 'twig_c'), Box(-2, 1, -1.5, 2, 4, 3, 'coat', 'cuff')])
M.mirror('right_arm', 'left_arm', parent='torso')
M.mirror('right_hand', 'left_hand', parent='left_arm')
# --- the Warden's arms: a pole lashed along each, with what was on the end of it when it was set up - a rake
M.part('right_pole', 'right_arm', (-9, 0, 0), boxes=[Box(-17, -0.5, -0.5, 19, 1, 1, 'pole', 'pole'), Box(-18, -0.5, -3.5, 1, 1, 7, 'pole', 'rake'),
                                                     Box(-18, 0.5, -3.5, 1, 3, 1, 'iron', 'tine_a'), Box(-18, 0.5, -1.5, 1, 3, 1, 'iron', 'tine_b'),
                                                     Box(-18, 0.5, 0.5, 1, 3, 1, 'iron', 'tine_c'), Box(-18, 0.5, 2.5, 1, 3, 1, 'iron', 'tine_d')])
M.mirror('right_pole', 'left_pole', parent='left_arm')
M.pack()

KINDS = ('scarecrow', 'scorched_warden')

STRAW = (214, 180, 88)
STRAW_D = (166, 132, 58)
STRAW_L = (240, 214, 130)
SACK = (176, 160, 124)
SACK_D = (132, 118, 88)
SACK_DD = (92, 80, 60)
COAT = (96, 78, 70)             # somebody's coat, once brown, bleached on the side the sun is
COAT_D = (68, 54, 50)
COAT_L = (134, 112, 96)
PATCH = (132, 62, 48)
TROUSER = (104, 100, 92)
TROUSER_D = (74, 72, 68)
HAT = (62, 52, 46)
HAT_D = (42, 36, 34)
WOOD = (150, 132, 108)          # a pole that has stood in the weather: grey
WOOD_D = (108, 94, 78)
WOOD_L = (184, 168, 142)
ROD = (112, 84, 60)
ROD_D = (78, 58, 42)
IRON = (104, 100, 98)
RUST = (136, 84, 52)
BLACK = (16, 12, 12)
CHAR = (38, 30, 28)
CHAR_D = (22, 16, 16)
CHAR_L = (66, 54, 46)
EMBER = (255, 132, 40)
EMBER_L = (255, 210, 110)
THREAD = (58, 44, 40)


def lit(face, py, fh, top=1.1, bottom=0.7, fall=0.16):
    return {'top': top, 'bottom': bottom}.get(face, 1.03 - fall * py / max(1, fh - 1))


def scorch(c, seed, face, px, py, fw, fh, rnd, glow):
    """The Warden: what it is made of is burnt black wherever the sun has stood on it, split, and red in the splits."""
    burnt = blotch(f'sc{seed}{face}', px, py, 2.6)
    if burnt < 0.36:
        return shade(c, 0.8), False
    crack = blotch(f'crk{seed}{face}', px * 1.7, py * 0.55, 1.5)
    if 0.47 < crack < 0.54:
        return (EMBER_L if rnd.random() < 0.3 else EMBER), True
    k = CHAR_D if blotch(f'ch{seed}{face}', px, py, 1.9) > 0.55 else (CHAR_L if rnd.random() < 0.12 else CHAR)
    return mix(c, k, min(1.0, (burnt - 0.36) * 4.0)), False


def make(kind):
    p = Painter(M, 501 + KINDS.index(kind))
    warden = kind == 'scorched_warden'

    def paint(b, base_fn):
        def fn(face, px, py, fw, fh, rnd):
            c = base_fn(face, px, py, fw, fh, rnd)
            if c is None or not warden or b.mat == 'iron':
                return c
            c2, hot = scorch(c, b.tag, face, px, py, fw, fh, rnd, p)
            if hot:
                fx, fy, _, _ = b.faces()[face]
                p.glow.putpixel((fx + px, fy + py), tuple(c2) + (255,))
            return c2
        p.fill_box(b, fn)

    for name, _, b in M.boxes():
        if b.mat == 'coat':
            def coat(face, px, py, fw, fh, rnd, tag=b.tag):
                c = COAT_D if blotch(f'ct{tag}{face}', px, py * 0.7, 2.3) > 0.58 else (COAT_L if rnd.random() < 0.08 else COAT)
                if face in ('top',) or (face == 'back' and tag == 'chest'):
                    c = mix(c, COAT_L, 0.4)                      # bleached where the sun is
                if tag == 'chest' and face == 'front':
                    if px in (fw // 2 - 1, fw // 2):
                        c = COAT_D
                        if py % 3 == 1 and px == fw // 2:
                            c = THREAD
                    if 1 <= px <= 2 and 6 <= py <= 8:
                        c = PATCH
                    if py >= fh - 3 and (px * 3 + py) % 5 == 0:
                        c = STRAW                                # straw coming out between the buttons
                if tag in ('skirt', 'cuff', 'sleeve') and face not in ('top', 'bottom'):
                    edge = py == fh - 1 if tag != 'sleeve' else False
                    if edge and (px * 5 + len(face)) % 3 == 0:
                        return None
                    if tag == 'cuff' and py >= fh - 2 and (px + py) % 2 == 0:
                        return STRAW_D
                if tag == 'sleeve' and face in ('right', 'left'):
                    c = STRAW if (px + py) % 2 else STRAW_D      # the open end of the sleeve: straw
                return shade(c, lit(face, py, fh))
            paint(b, coat)
        elif b.mat == 'trouser':
            def trouser(face, px, py, fw, fh, rnd):
                c = TROUSER_D if blotch(f'tr{face}', px, py * 0.6, 2.0) > 0.58 else TROUSER
                if py == fh - 2 and face not in ('top', 'bottom'):
                    c = THREAD                                   # tied at the ankle to keep the straw in
                return shade(c, lit(face, py, fh))
            paint(b, trouser)
        elif b.mat == 'straw':
            def straw(face, px, py, fw, fh, rnd):
                if face not in ('top', 'bottom') and py == fh - 1 and (px + len(face)) % 2 == 0:
                    return None
                return shade(STRAW_D if px % 2 == 0 else (STRAW_L if rnd.random() < 0.15 else STRAW), lit(face, py, fh, 1.1, 0.75, 0.1))
            paint(b, straw)
        elif b.mat == 'sackhead':
            def sackhead(face, px, py, fw, fh, rnd):
                c = SACK_D if (px + py) % 2 == 0 and blotch(f'hd{face}', px, py, 2.0) > 0.42 else SACK
                if face != 'top' and face != 'bottom' and py == fh - 1:
                    c = THREAD if px % 2 else SACK_DD            # tied round the neck
                return shade(c, lit(face, py, fh, 1.1, 0.72, 0.12))
            paint(b, sackhead)
        elif b.mat == 'hat':
            def hat(face, px, py, fw, fh, rnd, tag=b.tag):
                if tag == 'brim':
                    if face not in ('top', 'bottom'):
                        return None
                    cx, cy = px - (fw - 1) / 2, py - (fh - 1) / 2
                    r = (cx / (fw / 2)) ** 2 + (cy / (fh / 2)) ** 2
                    if r > 1.03 or (r > 0.75 and blotch('hatfray', px, py, 1.1) > 0.66):
                        return None
                    return shade(HAT_D if blotch('brim', px, py, 2.0) > 0.5 else HAT, 1.1 if face == 'top' else 0.7)
                if face == 'bottom':
                    return None
                return shade(HAT_D if blotch(f'crown{face}', px, py, 1.8) > 0.55 else HAT, lit(face, py, fh, 1.15, 0.7, 0.1))
            paint(b, hat)
        elif b.mat == 'pole':
            def pole(face, px, py, fw, fh, rnd):
                long = px if fw > fh else py
                c = WOOD_D if (long + (py if fw > fh else px)) % 6 == 0 or blotch(f'pl{fw}{fh}{face}', px * 0.4, py * 0.4, 1.5) > 0.66 else (WOOD_L if rnd.random() < 0.1 else WOOD)
                return shade(c, {'top': 1.1, 'bottom': 0.72}.get(face, 1.0))
            paint(b, pole)
        elif b.mat == 'rods':
            def rods(face, px, py, fw, fh, rnd, tag=b.tag):
                c = ROD_D if (py + px // 3) % 2 == 0 else ROD
                if tag == 'rods' and px in (fw - 2, fw - 3) and face not in ('right', 'left'):
                    c = THREAD                                   # the binding
                return shade(c, {'top': 1.1, 'bottom': 0.75}.get(face, 1.0))
            paint(b, rods)
        elif b.mat == 'iron':
            p.fill_box(b, lambda face, px, py, fw, fh, r: RUST if r.random() < 0.4 else IRON)
    head = next(b for _, _, b in M.boxes() if b.tag == 'head')
    pal = {'B': BLACK, 't': THREAD, 'g': EMBER, 'G': EMBER_L, 'd': SACK_DD}
    if warden:
        # holes burnt through the sack, and it is alight behind them
        rows = [".......", ".dd.dd.", "dGgdgGd", ".dd.dd.", "...d...", "tgGgGgt", ".ttttt."]
    else:
        # a face somebody drew with a burnt stick and then sewed over: crosses for eyes, a mouth in stitches
        rows = [".......", "t.t.t.t", ".t...t.", "t.t.t.t", "...d...", "t.t.t.t", ".ttttt."]
    p.art(head, 'front', rows, pal, glow_chars='Gg')
    return p.tex, p.glow


def hidden(kind, extra=()):
    return tuple((('right_pole', 'left_pole') if kind == 'scarecrow' else ()) + tuple(extra))


# The poses of ScarecrowModel.setupAnim (rotations added to the rest pose; 'off' moves a part).
# -- on the stake: hung, the head down, the hands hanging from the bar, the feet clear of the ground
HUNG = {'all': {'off': (0, -5, 0)}, 'head': (0.5, 0, 0.15), 'right_hand': (0, 0, -0.5), 'left_hand': (0, 0, 0.5), 'right_leg': (0.06, 0, 0.05), 'left_leg': (0.02, 0, -0.05)}
# -- down: stooped under the bar, the head up
DOWN = {'torso': (0.25, 0, 0), 'head': (-0.2, 0, 0), 'right_hand': (0, 0.4, 0), 'left_hand': (0, -0.4, 0), 'stake': {'off': (0, 60, 0)}}
TURN_BACK = {'all': (0, -0.4, 0), 'torso': (0.2, 0, 0.3), 'head': (-0.2, 0.4, 0), 'stake': {'off': (0, 60, 0)}}
TURN_THROUGH = {'all': (0, 2.6, 0), 'torso': (0.2, 0, 0.3), 'head': (-0.2, -0.6, 0), 'right_leg': (-0.4, 0, 0), 'left_leg': (0.4, 0, 0), 'stake': {'off': (0, 60, 0)}}
# -- the Warden on its cross: it turns on its stake with the bar tipped, so that one pole goes round near the ground
# and the other in the air (a turn about the stake is 'all'; the tip of the bar is the trunk's)
RAKE_BACK = {'all': {'off': (0, -5, 0), 'rot': (0, -0.3, 0)}, 'torso': (0.1, 0, 0.6), 'head': (0.1, 0.3, 0)}
RAKE_THROUGH = {'all': {'off': (0, -5, 0), 'rot': (0, 2.8, 0)}, 'torso': (0.1, 0, 0.6), 'head': (0.1, -0.6, 0)}
THRESH_UP = {'all': {'off': (0, -5, 0), 'rot': (0, 1.45, 0)}, 'torso': (0, 0, -0.7), 'head': (0, -1.0, 0)}
THRESH_DOWN = {'all': {'off': (0, -5, 0), 'rot': (0, 1.45, 0)}, 'torso': (0, 0, 0.75), 'head': (0, -1.0, 0)}
FALLEN = {'all': {'rot': (1.45, 0, 0), 'off': (0, 8, -4)}, 'right_arm': (0, 1.2, 0), 'left_arm': (0, -1.2, 0), 'stake': {'off': (0, 60, 0)}}

if __name__ == '__main__':
    M.write_java(JAVA) if len(sys.argv) < 3 else None
    shots = []
    for kind in KINDS:
        tex, glow = make(kind)
        tex.save(f'{A}/textures/entity/{kind}.png')
        if kind == 'scorched_warden':
            glow.save(f'{A}/textures/entity/{kind}_glow.png')
        poses = {'scarecrow': ((-0.5, HUNG), (0.0, HUNG), (2.6, HUNG), (-0.5, DOWN), (0.0, DOWN), (-0.3, TURN_BACK), (-0.3, TURN_THROUGH), (-1.3, DOWN)),
                 'scorched_warden': ((-0.5, HUNG), (0.0, HUNG), (-0.3, RAKE_BACK), (-0.3, RAKE_THROUGH), (-0.4, THRESH_UP), (-0.4, THRESH_DOWN), (-0.5, DOWN), (-1.3, FALLEN))}[kind]
        if len(sys.argv) > 1:
            shots += [M.render(tex, yaw=yaw, pose=pose, size=(330, 330), zoom=5.6, centre=(0, 4), glow=glow if kind == 'scorched_warden' else None, hidden=hidden(kind),
                               background=(196, 176, 120)) for yaw, pose in poses]
    if len(sys.argv) > 1:
        sheet(shots, 8).save(sys.argv[1])
