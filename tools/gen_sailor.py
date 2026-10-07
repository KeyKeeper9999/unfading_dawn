#!/usr/bin/env python3
"""The people of the Morning Wharf - the dockhand, the harpooner, the oarsman, the pilot and the Helmsman of the
Daybreak: one mesh for SailorModel.java (each kind shows its own parts of it; the model is drawn larger for some), a
texture for each kind and the glow of the pilot's lamp and of the Helmsman's eyes.
Run from the repo root; pass a file name to also get a preview sheet."""
import math
import sys

from modelkit import Box, Model, Painter, blotch, mix, shade, sheet

A = 'src/main/resources/assets/unfading_dawn'
JAVA = 'src/main/java/com/keykeeper/dushi/client/SailorModel.java'

M = Model(128, 128)
# Those who went down to the wharf to meet the ships that morning, and are still there: the ships did not come in.
# Drowned-pale, in tarred smocks and the high soft boots of the coast. The trunk turns about the hips; the tail of
# the smock hangs from the waist and is kept hanging whatever the trunk does; arms and legs have a joint each.
HIP = 11
M.part('right_leg', None, (-2, HIP, 0), boxes=[Box(-1.5, 0, -1.5, 3, 7, 3, 'breeches', 'thigh')])
M.part('right_shin', 'right_leg', (0, 7, 0), boxes=[Box(-2, -1, -2, 4, 7, 4, 'boot', 'shin'), Box(-2, 5, -4, 4, 1, 2, 'boot', 'foot')])
M.mirror('right_leg', 'left_leg', parent=None)
M.mirror('right_shin', 'left_shin')
M.part('torso', None, (0, HIP, 0), (0.1, 0, 0), boxes=[Box(-4, -12, -2, 8, 12, 4, 'smock', 'chest')])
M.part('tail', 'torso', (0, -1, 0), (-0.1, 0, 0), boxes=[Box(-4.5, 0, -2.5, 9, 5, 5, 'smock', 'tail')])
M.part('head', 'torso', (0, -12, 0), (-0.05, 0, 0), boxes=[Box(-4, -8, -4, 8, 8, 8, 'head', 'head')])
M.part('right_arm', 'torso', (-5.5, -10.5, 0), boxes=[Box(-1.5, -1.5, -1.5, 3, 7, 3, 'sleeve', 'upper')])
M.part('right_forearm', 'right_arm', (0, 5.5, 0), (-0.15, 0, 0), boxes=[Box(-1.5, 0, -1.5, 3, 7, 3, 'sleeve', 'fore')])
M.mirror('right_arm', 'left_arm')
M.mirror('right_forearm', 'left_forearm')
# --- the dockhand: a sack over the head and shoulders, as those wear who carry on their backs; the pad the load
# rides on; and the hook the bales are taken with, in the right hand. A held thing lies across the arm, pointing forward
# when the arm hangs: it is drawn standing and turned a quarter.
M.part('sack', 'head', (0, 0, 0), boxes=[Box(-4.5, -8.5, -4.5, 9, 11, 9, 'sack', 'sack')])
M.part('pad', 'torso', (0, 0, 0), boxes=[Box(-3.5, -11.5, 2, 7, 7, 3, 'sack', 'pad')])
M.part('hook', 'right_forearm', (0, 6, 0), (math.pi / 2, 0, 0), boxes=[Box(-2.5, -0.5, -0.5, 5, 1, 1, 'haft', 'grip'), Box(-0.5, -8, -0.5, 1, 8, 1, 'iron', 'shank'),
                                                                       Box(-0.5, -9, -3.5, 1, 1, 4, 'iron', 'bend'), Box(-0.5, -8, -3.5, 1, 3, 1, 'iron', 'point')])
# --- the harpooner: a sou'wester; spare irons on his back and a coil of line at his hip; the harpoon in his right hand
M.part('souwester', 'head', (0, 0, 0), boxes=[Box(-4.5, -9, -4.5, 9, 3, 9, 'oilskin', 'crown'), Box(-6, -6.5, -6, 12, 1, 14, 'oilskin', 'brim')])
M.part('irons', 'torso', (0, 0, 0), (0, 0, 0.12), boxes=[Box(1, -17, 2.2, 1, 16, 1, 'haft', 'iron_a'), Box(-2.5, -16, 2.4, 1, 15, 1, 'haft', 'iron_b'),
                                                         Box(0.5, -20, 2.2, 2, 3, 1, 'iron', 'head_a'), Box(-3, -19, 2.4, 2, 3, 1, 'iron', 'head_b')])
M.part('coil', 'torso', (0, 0, 0), boxes=[Box(3.6, -4, -2, 2, 5, 4, 'rope', 'coil')])
M.part('harpoon', 'right_forearm', (0, 6, 0), (math.pi / 2, 0, 0), boxes=[Box(-0.5, -22, -0.5, 1, 30, 1, 'haft', 'shaft'), Box(-1, -27, -0.5, 2, 5, 1, 'iron', 'blade'),
                                                                          Box(-2, -24, -0.5, 1, 2, 1, 'iron', 'barb_a'), Box(1, -24, -0.5, 1, 2, 1, 'iron', 'barb_b')])
# --- the oarsman: half as broad again; a knitted cap, a beard like a mat; the oar in both hands
M.part('cap', 'head', (0, 0, 0), boxes=[Box(-4.5, -8.7, -4.5, 9, 4, 9, 'wool', 'cap')])
M.part('beard', 'head', (0, 0, 0), boxes=[Box(-3.5, -2.5, -4.8, 7, 5, 1, 'hair', 'beard')])
M.part('bulk', 'torso', (0, 0, 0), boxes=[Box(-6, -12.8, -3, 12, 6, 6, 'smock', 'shoulders'), Box(-4.8, -6.8, -2.8, 9.6, 6, 5.6, 'smock', 'belly')])
M.part('oar', 'right_forearm', (0, 6, 0), (math.pi / 2, 0, 0), boxes=[Box(-0.5, -28, -0.5, 1, 38, 1, 'haft', 'loom'), Box(-2, -42, -0.5, 4, 14, 1, 'blade', 'blade')])
# --- the pilot: he was to bring the ships in. A long oilskin coat to the shins; a lamp at his belt, alight; and in
# his right hand the lead he sounded the channel with, on a fathom of line. What hangs is drawn hanging.
M.part('coat', 'torso', (0, -1, 0), (-0.1, 0, 0), boxes=[Box(-5, 0, -3, 10, 11, 6, 'oilskin', 'coat')])
M.part('collar', 'torso', (0, 0, 0), boxes=[Box(-5, -13, -3, 10, 3, 6, 'oilskin', 'collar')])
M.part('lamp', 'torso', (-3.5, -1, -3), (-0.1, 0, 0), boxes=[Box(-1.5, 0, -1.5, 3, 4, 3, 'lantern', 'lamp'), Box(-0.5, -1, -0.5, 1, 1, 1, 'iron', 'ring')])
M.part('line', 'right_forearm', (0, 6.5, 0), boxes=[Box(-0.5, 0, -0.5, 1, 14, 1, 'rope', 'line'), Box(-1.5, 14, -1.5, 3, 5, 3, 'lead', 'lead')])
# --- the Helmsman: the master of the ship that could not cast off. The anchor would not come up, so he tore it out of
# the bottom, and carries it: an iron as long as he is tall, held by the ring, the chain of it across his chest. A
# master's cap with fur round it. (He is drawn half as large again as the others: see SailorModel.)
M.part('hat', 'head', (0, 0, 0), boxes=[Box(-4.5, -10, -4.5, 9, 3, 9, 'felt', 'hat'), Box(-5, -7.5, -5, 10, 2, 10, 'fur', 'trim')])
M.part('chains', 'torso', (0, 0, 0), boxes=[Box(-4.5, -12.5, -2.6, 9, 12.5, 5.2, 'chain', 'chains')])
M.part('anchor', 'right_forearm', (0, 6, 0), (math.pi / 2, 0, 0), boxes=[
    Box(-1.5, -3, -0.5, 3, 3, 1, 'iron', 'ring'), Box(-0.5, -5, -5, 1, 1, 10, 'haft', 'stock'), Box(-1, -24, -1, 2, 21, 2, 'iron', 'shank'),
    Box(-7, -26, -1, 14, 2, 2, 'iron', 'crown'), Box(-9, -26, -1, 2, 7, 2, 'iron', 'arm_a'), Box(7, -26, -1, 2, 7, 2, 'iron', 'arm_b'),
    Box(-10, -21, -1.5, 3, 3, 3, 'iron', 'fluke_a'), Box(7, -21, -1.5, 3, 3, 3, 'iron', 'fluke_b'), Box(-0.5, 0, -0.5, 1, 7, 1, 'link', 'links')])
M.pack()

KINDS = ('dockhand', 'harpooner', 'oarsman', 'pilot', 'helmsman')
PARTS = {'dockhand': ('sack', 'pad', 'hook'), 'harpooner': ('souwester', 'irons', 'coil', 'harpoon'), 'oarsman': ('cap', 'beard', 'bulk', 'oar'),
         'pilot': ('souwester', 'coat', 'collar', 'lamp', 'line'), 'helmsman': ('hat', 'beard', 'bulk', 'coat', 'collar', 'chains', 'anchor')}

SKIN = (146, 158, 150)          # drowned: grey with the green of the water in it
SKIN_D = (106, 120, 116)
SKIN_DD = (70, 84, 84)
TAR = (58, 54, 50)
TAR_D = (38, 36, 36)
TAR_L = (84, 78, 70)
CANVAS = (150, 138, 112)
CANVAS_D = (112, 102, 82)
CANVAS_L = (182, 170, 142)
OIL = (126, 112, 58)            # oilskin: a dull yellow gone brown
OIL_D = (88, 78, 44)
OIL_L = (160, 144, 80)
WOOLC = (112, 50, 46)
WOOL_D = (80, 36, 36)
HAIR = (150, 146, 136)
HAIR_D = (106, 102, 96)
LEATHER = (86, 62, 44)
LEATHER_D = (58, 42, 32)
IRON = (86, 88, 96)
IRON_D = (54, 56, 64)
IRON_L = (132, 136, 146)
RUST = (126, 78, 50)
WOOD = (132, 104, 70)
WOOD_D = (94, 72, 48)
WOOD_L = (168, 140, 98)
ROPE = (150, 128, 88)
ROPE_D = (108, 90, 60)
LEAD = (92, 96, 104)
LEAD_D = (60, 64, 72)
WEED = (62, 96, 70)
SALT = (206, 208, 200)
BLACK = (14, 16, 18)
NAVY = (46, 56, 84)             # a master's coat: blue once
NAVY_D = (30, 36, 58)
NAVY_L = (70, 82, 112)
FUR = (96, 82, 66)
FUR_D = (64, 54, 46)
BRASS = (176, 150, 84)
EYE = (214, 226, 220)
FLAME = (255, 214, 120)
FLAME_L = (255, 246, 208)


def lit(face, py, fh, top=1.1, bottom=0.7, fall=0.18):
    return {'top': top, 'bottom': bottom}.get(face, 1.03 - fall * py / max(1, fh - 1))


def wet(c, seed, face, px, py, fh, amount=0.35):
    """Whatever it is, it has been in the sea: darker towards the hem, with weed on it here and there and a line of
    salt where it dried."""
    if face in ('top', 'bottom'):
        return c
    if py >= fh * (1 - amount) and blotch(f'wet{seed}{face}', px, py, 1.7) > 0.35:
        c = shade(c, 0.78)
    if blotch(f'weed{seed}{face}', px, py, 1.4) > 0.8:
        c = mix(c, WEED, 0.6)
    if py == int(fh * (1 - amount)) - 1 and blotch(f'salt{seed}{face}', px, 0, 1.0) > 0.6:
        c = mix(c, SALT, 0.45)
    return c


def smock(kind, tag):
    base, dark, light = {'dockhand': (CANVAS, CANVAS_D, CANVAS_L), 'harpooner': (TAR, TAR_D, TAR_L), 'oarsman': (TAR, TAR_D, TAR_L), 'pilot': (TAR, TAR_D, TAR_L), 'helmsman': (NAVY, NAVY_D, NAVY_L)}[kind]

    def fn(face, px, py, fw, fh, rnd):
        c = dark if blotch(f'sm{kind}{tag}{face}', px, py * 0.6, 2.2) > 0.6 else (light if rnd.random() < 0.07 else base)
        if tag == 'chest' and face == 'front' and px in (fw // 2 - 1, fw // 2) and py < 6:
            c = dark                                             # the slit at the neck, laced
            if py % 2 == 0:
                c = ROPE_D
        if tag == 'chest' and py == fh - 2 and face not in ('top', 'bottom'):
            c = ROPE if (px + (0 if face in ('front', 'back') else 1)) % 2 else ROPE_D     # a rope for a belt
        if tag == 'tail' and face not in ('top', 'bottom') and py == fh - 1 and (px * 5 + len(face)) % 4 == 0:
            return None                                          # the hem, frayed
        c = wet(c, kind + tag, face, px, py, fh, 0.5 if tag in ('tail', 'fore') else 0.25)
        return shade(c, lit(face, py, fh))
    return fn


def breeches(face, px, py, fw, fh, rnd):
    c = CANVAS_D if blotch(f'br{face}', px, py, 2.0) > 0.55 else shade(CANVAS_D, 0.85)
    return shade(c, lit(face, py, fh))


def boot(tag):
    def fn(face, px, py, fw, fh, rnd):
        c = LEATHER_D if blotch(f'bt{tag}{face}', px, py, 1.8) > 0.55 else LEATHER
        if tag == 'shin' and py == 0 and face not in ('top', 'bottom'):
            c = shade(LEATHER, 1.25)                             # the top of the boot, turned down
        if tag == 'shin' and py == 2 and face not in ('top', 'bottom'):
            c = ROPE_D                                           # tied under the knee
        c = wet(c, 'boot' + tag, face, px, py, fh, 0.6)
        return shade(c, lit(face, py, fh))
    return fn


def skin(face, px, py, fw, fh, rnd):
    c = SKIN_D if blotch(f'skin{fw}{fh}{face}', px, py * 0.6, 1.8) > 0.6 else SKIN
    return shade(c, lit(face, py, fh, 1.08, 0.7, 0.1))


def wood(face, px, py, fw, fh, rnd):
    c = WOOD_D if (py + px // 2) % 5 == 0 or blotch(f'wood{fw}{fh}{face}', px, py * 0.3, 1.5) > 0.66 else (WOOD_L if rnd.random() < 0.1 else WOOD)
    return shade(c, {'top': 1.1, 'bottom': 0.72}.get(face, 1.0))


def iron(face, px, py, fw, fh, rnd):
    c = RUST if blotch(f'iron{fw}{fh}{face}', px, py, 1.6) > 0.68 else (IRON_L if rnd.random() < 0.14 else (IRON_D if rnd.random() < 0.2 else IRON))
    return shade(c, {'top': 1.15, 'bottom': 0.7}.get(face, 1.0))


def oilskin(tag, kind=''):
    def fn(face, px, py, fw, fh, rnd):
        if kind == 'helmsman' and tag in ('coat', 'collar'):
            # his is cloth, not oilskin: the same blue as the rest of him, with a row of brass down the front
            if tag == 'collar' and (face in ('top', 'bottom') or face == 'front' and 2 <= px <= fw - 3):
                return None
            c = NAVY_D if blotch(f'nv{tag}{face}', px, py * 0.7, 2.4) > 0.58 else (NAVY_L if rnd.random() < 0.08 else NAVY)
            if tag == 'coat':
                if face == 'front' and px in (fw // 2 - 1, fw // 2):
                    c = NAVY_D
                if face == 'front' and px in (fw // 2 - 2, fw // 2 + 1) and py % 3 == 1:
                    c = BRASS
                if face not in ('top', 'bottom') and py == fh - 1 and (px * 3 + len(face)) % 4 == 0:
                    return None
                c = wet(c, 'hcoat', face, px, py, fh, 0.45)
            return shade(c, lit(face, py, fh, 1.14, 0.7, 0.12))
        if tag == 'brim' and face in ('top', 'bottom'):
            # the brim is a plate: round at the front, long over the neck behind
            cx, cy = px - (fw - 1) / 2, py - (fh - 1) / 2
            if (cx / (fw / 2)) ** 2 + (cy / (fh / 2)) ** 2 > 1.05:
                return None
        if tag == 'brim' and face not in ('top', 'bottom'):
            return None
        c = OIL_D if blotch(f'oil{tag}{face}', px, py * 0.7, 2.4) > 0.58 else (OIL_L if rnd.random() < 0.1 else OIL)
        if tag == 'coat':
            if face == 'front' and px in (fw // 2 - 1, fw // 2):
                c = OIL_D                                        # where it closes
                if py % 3 == 1 and px == fw // 2:
                    c = (60, 52, 40)                             # a toggle of wood
            if face not in ('top', 'bottom') and py == fh - 1 and (px * 3 + len(face)) % 5 == 0:
                return None
            c = wet(c, 'coat', face, px, py, fh, 0.4)
        if tag == 'collar' and face == 'front' and 2 <= px <= fw - 3:
            return None                                          # turned up round the neck, open before the face
        if tag == 'collar' and face in ('top', 'bottom'):
            return None
        return shade(c, lit(face, py, fh, 1.14, 0.7, 0.12))
    return fn


def make(kind):
    p = Painter(M, 211 + KINDS.index(kind))
    boxes = {b.tag: b for _, _, b in M.boxes()}
    for name, _, b in M.boxes():
        if b.mat in ('smock', 'sleeve'):
            p.fill_box(b, smock(kind, b.tag))
        elif b.mat == 'breeches':
            p.fill_box(b, breeches)
        elif b.mat == 'boot':
            p.fill_box(b, boot(b.tag))
        elif b.mat == 'head':
            p.fill_box(b, skin)
        elif b.mat == 'sack':
            def sack(face, px, py, fw, fh, rnd, tag=b.tag):
                if tag == 'sack':
                    if face == 'bottom':
                        return None
                    if face == 'front' and 1 <= px <= fw - 2 and 2 <= py <= 8:
                        return None                              # the face looks out of it
                    if face != 'top' and py >= fh - 1 and (px + (0 if face in ('front', 'back') else 1)) % 3 == 0:
                        return None
                c = CANVAS_D if (px + py) % 2 == 0 and blotch(f'sk{tag}{face}', px, py, 2.0) > 0.4 else CANVAS
                if tag == 'pad' and face == 'back' and (py in (1, fh - 2) or px in (1, fw - 2)):
                    c = shade(CANVAS_D, 0.8)                     # the stitching of the pad
                return shade(c, lit(face, py, fh, 1.1, 0.72, 0.1))
            p.fill_box(b, sack)
        elif b.mat == 'oilskin':
            p.fill_box(b, oilskin(b.tag, kind))
        elif b.mat == 'felt':
            p.fill_box(b, lambda face, px, py, fw, fh, r: None if face == 'bottom' else shade(NAVY_D if (px + py) % 3 else NAVY, lit(face, py, fh, 1.12, 0.7, 0.1)))
        elif b.mat == 'fur':
            def fur(face, px, py, fw, fh, rnd):
                if face in ('top', 'bottom') and 1 <= px <= fw - 2 and 1 <= py <= fh - 2:
                    return None                                  # a band round the cap, not a lid on it
                return shade(FUR_D if (px * 3 + py * 5) % 4 == 0 else FUR, 1.0 if rnd.random() > 0.15 else 1.2)
            p.fill_box(b, fur)
        elif b.mat == 'chain':
            def chain(face, px, py, fw, fh, rnd):
                # one turn of chain from the right shoulder to the left hip, before and behind; nothing else of the box
                if face in ('top', 'bottom'):
                    return None
                if face in ('front', 'back'):
                    along = px if face == 'front' else fw - 1 - px
                    on = abs(py - (1 + along * 1.25)) < 1.3
                elif face == 'right':
                    on = py <= 2
                else:
                    on = py >= fh - 3
                if not on:
                    return None
                return IRON_L if (px + py) % 3 == 0 else (IRON_D if (px + py) % 3 == 1 else IRON)
            p.fill_box(b, chain)
        elif b.mat == 'link':
            p.fill_box(b, lambda face, px, py, fw, fh, r: (IRON_L if py % 2 else IRON_D) if face not in ('top', 'bottom') else IRON)
        elif b.mat == 'wool':
            p.fill_box(b, lambda face, px, py, fw, fh, r: None if face == 'bottom' else shade(WOOLC if (px + py) % 2 else WOOL_D, lit(face, py, fh, 1.1, 0.7, 0.1) * (1.2 if py == fh - 1 and face != 'top' else 1.0)))
        elif b.mat == 'hair':
            def hair(face, px, py, fw, fh, rnd):
                if face in ('front', 'back') and py >= fh - 2 and (px in (0, fw - 1) or (px + py) % 3 == 0):
                    return None
                c = HAIR_D if (px * 3 + py) % 4 == 0 else HAIR
                if blotch(f'bw{face}', px, py, 1.3) > 0.72:
                    c = mix(c, WEED, 0.6)
                return shade(c, 1.0 - 0.04 * py)
            p.fill_box(b, hair)
        elif b.mat == 'iron':
            p.fill_box(b, iron)
        elif b.mat == 'haft':
            p.fill_box(b, wood)
        elif b.mat == 'blade':
            def blade(face, px, py, fw, fh, rnd):
                if face in ('front', 'back') and py < 2 and px in (0, fw - 1):
                    return None                                  # the corners of the blade, rounded
                c = WOOD_D if px in (0, fw - 1) and face in ('front', 'back') else (WOOD_L if (py + px) % 6 == 0 else WOOD)
                if face in ('front', 'back') and py < 4 and blotch(f'bl{face}', px, py, 1.5) > 0.5:
                    c = mix(c, WEED, 0.5)
                return shade(c, {'top': 1.1, 'bottom': 0.72}.get(face, 1.0 - 0.012 * py))
            p.fill_box(b, blade)
        elif b.mat == 'rope':
            p.fill_box(b, lambda face, px, py, fw, fh, r: shade(ROPE if (px + py) % 2 else ROPE_D, {'top': 1.1, 'bottom': 0.75}.get(face, 1.0)))
        elif b.mat == 'lead':
            def lead(face, px, py, fw, fh, rnd):
                c = LEAD_D if face == 'bottom' or px in (0, fw - 1) and py > fh - 3 else (LEAD if rnd.random() > 0.15 else IRON_L)
                return shade(c, {'top': 1.15, 'bottom': 0.8}.get(face, 1.02 - 0.05 * py))
            p.fill_box(b, lead)
        elif b.mat == 'lantern':
            def lantern(face, px, py, fw, fh, rnd):
                if face in ('top', 'bottom') or py in (0, fh - 1) or px in (0, fw - 1):
                    return IRON if face != 'bottom' else IRON_D
                return FLAME
            p.fill_box(b, lantern)
    head = boxes['head']
    pal = {'s': SKIN, 'd': SKIN_D, 'D': SKIN_DD, 'B': BLACK, 'E': EYE, 'G': FLAME, 'w': WEED, 'h': HAIR_D}
    faces = {
        # a sack over it: what is seen of him is two eyes in shadow
        'dockhand': ["DDDDDDDD", "DDDDDDDD", "dDDDDDDd", "dEBddBEd", "dDDddDDd", "ssdssdss", "sdBBBBds", "ddssssdd"],
        # one eye is gone, with a weed grown in where it was
        'harpooner': ["dddddddd", "dssssssd", "sDDsswws", "DEBDdwBw", "sDDsdwDs", "ssdssdss", "sdBBBBds", "ddssssdd"],
        'oarsman': ["hhhhhhhh", "hsssssss", "sDDssDDs", "sBEddEBs", "sDDssDDs", "ssdssdss", "hhBBBBhh", "hhhhhhhh"],
        # the pilot's eyes are lamps: he has looked for the ships a long time
        'pilot': ["DDDDDDDD", "DddddddD", "dDDddDDd", "DGBddBGD", "dDDddDDd", "ddDddDdd", "dDBBBBDd", "DddddddD"],
        # the Helmsman: a face like a figurehead's, weathered to the grain, and two eyes that still give orders
        'helmsman': ["dddddddd", "dsdssdsd", "DDDssDDD", "DGBddBGD", "sDDssDDs", "sdsDDsds", "hhBBBBhh", "hhhhhhhh"],
    }[kind]
    p.art(head, 'front', faces, pal, glow_chars='G')
    for face in ('back', 'right', 'left', 'top'):
        fx, fy, fw, fh = head.faces()[face]
        for x in range(fw):
            for y in range(fh if face in ('top', 'back') else 4):
                if face == 'top' or y < 3 + (x * 7) % 3:
                    p.px(fx + x, fy + y, shade(HAIR_D, 0.8 + 0.3 * blotch(f'hr{kind}{face}', x, y, 1.4)))
    if kind in ('pilot', 'helmsman'):
        for _, _, b in M.boxes():
            if b.mat == 'lantern':
                for face, (fx, fy, fw, fh) in b.faces().items():
                    for x in range(fw):
                        for y in range(fh):
                            r_, g_, b_, a_ = p.tex.getpixel((fx + x, fy + y))
                            if a_ and r_ > 200:
                                p.glow.putpixel((fx + x, fy + y), (r_, g_, b_, 255))
    return p.tex, p.glow


def hidden(kind):
    every = {part for parts in PARTS.values() for part in parts}
    return tuple(every - set(PARTS[kind]))


# The poses of SailorModel.setupAnim, as it sets them (rotations added to the rest pose).
# -- the dockhand: the hook carried low; drawn back over the shoulder; brought over and down, and pulled home
DOCK_STAND = {'torso': (0.18, 0, 0), 'tail': (-0.18, 0, 0), 'right_arm': (-0.25, 0, -0.1), 'right_forearm': (-0.5, 0, 0), 'left_arm': (0.15, 0, 0.1)}
HOOK_UP = {'torso': (-0.15, 0.3, 0), 'tail': (0.15, 0, 0), 'right_arm': (-2.6, 0, -0.2), 'right_forearm': (-0.7, 0, 0), 'left_arm': (-0.6, 0, 0.3)}
HOOK_DOWN = {'torso': (0.55, -0.2, 0), 'tail': (-0.55, 0, 0), 'right_arm': (-1.1, 0, 0), 'right_forearm': (-0.1, 0, 0), 'left_arm': (0.5, 0, 0.3), 'right_leg': (-0.5, 0, 0),
             'left_leg': (0.3, 0, 0)}
HOOK_PULL = {'torso': (-0.1, 0.3, 0), 'tail': (0.1, 0, 0), 'right_arm': (-0.2, 0, 0), 'right_forearm': (-1.5, 0, 0), 'left_arm': (-0.9, 0, 0.2)}
RIP = {'torso': (0.2, -0.7, 0), 'tail': (-0.2, 0, 0), 'right_arm': (-1.3, -0.9, 0), 'right_forearm': (-0.3, 0, 0), 'left_arm': (0.3, 0, 0.3)}
# -- the harpooner: the iron upright at his shoulder; drawn back to throw; thrown; the thrust
HARP_STAND = {'right_arm': (-0.9, 0, -0.1), 'right_forearm': (-0.8, 0, 0), 'left_arm': (0.1, 0, 0.05)}
THROW_BACK = {'torso': (-0.2, 0.6, 0), 'tail': (0.2, 0, 0), 'right_arm': (-2.5, 0.4, -0.3), 'right_forearm': (-1.1, 0, 0), 'left_arm': (-1.4, 0, 0.2), 'left_leg': (-0.4, 0, 0),
              'right_leg': (0.3, 0, 0)}
THROWN = {'torso': (0.55, -0.5, 0), 'tail': (-0.55, 0, 0), 'right_arm': (-1.5, -0.2, 0), 'right_forearm': (-0.1, 0, 0), 'left_arm': (0.7, 0, 0.3), 'right_leg': (-0.5, 0, 0),
          'left_leg': (0.4, 0, 0)}
STAB = {'torso': (0.3, -0.3, 0), 'tail': (-0.3, 0, 0), 'right_arm': (-0.45, 0, 0.1), 'right_forearm': (0.15, 0, 0), 'left_arm': (-0.8, 0, 0.2), 'right_leg': (0.4, 0, 0),
        'left_leg': (-0.5, 0, 0)}
# -- the oarsman: the oar across his body in both hands; drawn round; swept; up; down
OAR_STAND = {'torso': (0.1, 0.25, 0), 'tail': (-0.1, 0, 0), 'right_arm': (-0.7, 0.45, 0), 'right_forearm': (-0.5, 0, 0), 'left_arm': (-1.0, 0.5, 0), 'left_forearm': (-0.6, 0, 0)}
# (a long thing is swept level with the arm down and the whole trunk turning: a held thing points across the arm)
OAR_DRAWN = {'torso': (0.1, 1.0, 0), 'tail': (-0.1, 0, 0), 'coat': (-0.1, 0, 0), 'right_arm': (-0.1, 0.6, 0), 'right_forearm': (0.1, 0, 0), 'left_arm': (-0.9, 0.3, 0),
             'left_forearm': (-0.8, 0, 0)}
OAR_SWEPT = {'torso': (0.2, -0.9, 0), 'tail': (-0.2, 0, 0), 'coat': (-0.2, 0, 0), 'right_arm': (-0.1, -0.5, 0), 'right_forearm': (0.1, 0, 0), 'left_arm': (-0.7, -0.4, 0),
             'left_forearm': (-0.9, 0, 0), 'right_leg': (-0.3, 0, 0), 'left_leg': (0.3, 0, 0)}
OAR_UP = {'torso': (-0.3, 0, 0), 'tail': (0.3, 0, 0), 'right_arm': (-2.9, 0, 0), 'right_forearm': (-0.3, 0, 0), 'left_arm': (-2.7, 0, 0.3), 'left_forearm': (-0.5, 0, 0)}
OAR_DOWN = {'torso': (0.6, 0, 0), 'tail': (-0.6, 0, 0), 'right_arm': (-0.75, 0, 0), 'right_forearm': (0.1, 0, 0), 'left_arm': (-0.8, 0, 0.3), 'left_forearm': (-0.3, 0, 0),
            'right_leg': (-0.4, 0, 0), 'left_leg': (0.3, 0, 0)}
OAR_PROD = {'torso': (0.35, -0.3, 0), 'tail': (-0.35, 0, 0), 'right_arm': (-0.4, 0, 0.1), 'right_forearm': (0.15, 0, 0), 'left_arm': (-0.9, -0.3, 0), 'left_forearm': (-0.4, 0, 0),
            'right_leg': (0.4, 0, 0), 'left_leg': (-0.5, 0, 0)}
# -- the pilot: the lead hanging; whirled over his head; cast; let down before him
PILOT_STAND = {'right_arm': (-0.3, 0, -0.15), 'right_forearm': (-0.4, 0, 0), 'line': (0.85, 0, 0), 'left_arm': (0.1, 0, 0.05)}
WHIRL = {'torso': (-0.1, 0, 0), 'tail': (0.1, 0, 0), 'coat': (0.1, 0, 0), 'right_arm': (-2.8, 0, -0.2), 'right_forearm': (-0.3, 0, 0), 'line': (1.3, 0.9, 0), 'left_arm': (-0.4, 0, 0.5)}
CAST_BACK = {'torso': (-0.15, 0.6, 0), 'coat': (0.15, 0, 0), 'right_arm': (-2.2, 0.5, -0.3), 'right_forearm': (-0.8, 0, 0), 'line': (-0.6, 0, 0), 'left_arm': (-1.2, 0, 0.2)}
CAST = {'torso': (0.5, -0.4, 0), 'coat': (-0.5, 0, 0), 'right_arm': (-1.5, -0.2, 0), 'right_forearm': (-0.05, 0, 0), 'line': (0.0, 0, 0), 'left_arm': (0.6, 0, 0.3),
        'right_leg': (-0.5, 0, 0), 'left_leg': (0.4, 0, 0)}
SOUND_UP = {'torso': (-0.2, 0, 0), 'coat': (0.2, 0, 0), 'right_arm': (-2.9, 0, 0), 'right_forearm': (-0.2, 0, 0), 'line': (-0.3, 0, 0), 'left_arm': (-2.6, 0, 0.3)}
SOUND_DOWN = {'torso': (0.55, 0, 0), 'coat': (-0.55, 0, 0), 'right_arm': (-1.0, 0, 0), 'right_forearm': (0.0, 0, 0), 'line': (0.45, 0, 0), 'left_arm': (-0.9, 0, 0.3),
              'right_leg': (-0.4, 0, 0), 'left_leg': (0.3, 0, 0)}
# -- the Helmsman: the anchor on his shoulder; his blows are the oarsman's, with the anchor; hauling the chain in after a cast
HELM_STAND = {'torso': (0.06, 0, 0), 'coat': (-0.06, 0, 0), 'right_arm': (-2.2, 0, -0.1), 'right_forearm': (-0.9, 0, 0), 'left_arm': (0.1, 0, 0.12)}
HAUL = {'torso': (0.35, 0.3, 0), 'coat': (-0.35, 0, 0), 'right_arm': (-1.3, 0, 0.2), 'right_forearm': (-0.9, 0, 0), 'left_arm': (-1.0, 0, -0.2), 'left_forearm': (-0.3, 0, 0),
        'right_leg': (0.3, 0, 0), 'left_leg': (-0.4, 0, 0)}

if __name__ == '__main__':
    M.write_java(JAVA) if len(sys.argv) < 3 else None
    shots = []
    for kind in KINDS:
        tex, glow = make(kind)
        tex.save(f'{A}/textures/entity/{kind}.png')
        if kind in ('pilot', 'helmsman'):
            glow.save(f'{A}/textures/entity/{kind}_glow.png')
        poses = {'dockhand': ((-0.5, DOCK_STAND), (0.0, DOCK_STAND), (2.6, DOCK_STAND), (-1.3, DOCK_STAND), (-1.2, HOOK_UP), (-1.3, HOOK_DOWN), (-1.3, HOOK_PULL), (-0.5, RIP)),
                 'harpooner': ((-0.5, HARP_STAND), (0.0, HARP_STAND), (2.6, HARP_STAND), (-1.3, HARP_STAND), (-1.2, THROW_BACK), (-1.3, THROWN), (-1.3, STAB), (-0.5, STAB)),
                 'oarsman': ((-0.5, OAR_STAND), (0.0, OAR_STAND), (2.6, OAR_STAND), (-0.4, OAR_DRAWN), (-0.4, OAR_SWEPT), (-1.3, OAR_UP), (-1.3, OAR_DOWN), (-1.3, OAR_PROD)),
                 'helmsman': ((-0.5, HELM_STAND), (0.0, HELM_STAND), (2.6, HELM_STAND), (-0.4, OAR_DRAWN), (-0.4, OAR_SWEPT), (-1.3, OAR_UP), (-1.3, OAR_DOWN), (-1.2, HAUL)),
                 'pilot': ((-0.5, PILOT_STAND), (0.0, PILOT_STAND), (2.6, PILOT_STAND), (-0.6, WHIRL), (-1.2, CAST_BACK), (-1.3, CAST), (-1.3, SOUND_UP), (-1.3, SOUND_DOWN))}[kind]
        if len(sys.argv) > 1:
            shots += [M.render(tex, yaw=yaw, pose=pose, size=(270, 360), zoom=6.0, centre=(0, 4), glow=glow if kind in ('pilot', 'helmsman') else None, hidden=hidden(kind),
                               background=(150, 150, 146)) for yaw, pose in poses]
    if len(sys.argv) > 1:
        sheet(shots, 8).save(sys.argv[1])
