#!/usr/bin/env python3
"""The grown ones of the Sky Beneath - what a day kept too long in the dark turned into. Six creatures, a mesh, a texture
and a glow each: the usher (UsherModel.java), the stray shadow (StrayShadowModel.java), the long-armed
(LongarmModel.java), the handwalker (HandwalkerModel.java), the deepling (DeeplingModel.java) and the gut
(GutModel.java).
Run from the repo root; pass a file name to also get a preview sheet (and a second argument to leave the Java alone)."""
import math
import sys

from duskkit import (BLUE, BLUE_L, NAIL, NIGHT, NIGHT_D, NIGHT_L, PALE, PALE_D, PALE_DD, RAW, RAW_D, STAR, STAR_D, TOOTH, TOOTH_D, VIOLET, VIOLET_D,
                     VIOLET_L, cracks, hand_flat, hand_up, night, skin, starry)
from modelkit import Box, Model, Painter, blotch, mix, shade, sheet

A = 'src/main/resources/assets/unfading_dawn'
J = 'src/main/java/com/keykeeper/dushi/client'
BACK = (26, 28, 44)              # what the previews are drawn against: about as dark as the place


def flip(fn):
    """The same face painted upside down (a hand that hangs has its fingers at the bottom)."""
    return lambda face, px, py, fw, fh, rnd: fn(face, px, fh - 1 - py, fw, fh, rnd)


# ---------------------------------------------------------------------------------------------------- the usher
# Three men and a half high and as thin as a post, in a coat of the same night as the roof of the place, with stars in
# it. Its arms hang to its shins and end in hands that are a man's, and pale. Where its face should be are more hands,
# folded over it; when it means to take hold of somebody they open, and there is nothing behind them but a ring of
# light round a dark middle. Everything hangs from 'all', on the ground.
USHER_SCALE = 1.4               # must match MODEL_SCALE in UsherModel.java


def usher():
    M = Model(64, 64, USHER_SCALE)
    M.part('all', None, (0, 24, 0))
    M.part('right_leg', 'all', (-1.5, -18, 0), boxes=[Box(-1, 0, -1, 2, 9, 2, 'dark', 'thigh')])
    M.part('right_shin', 'right_leg', (0, 9, 0), boxes=[Box(-1, 0, -1, 2, 9, 2, 'dark', 'shin'), Box(-1, 8, -3, 2, 1, 2, 'dark', 'foot')])
    M.mirror('right_leg', 'left_leg', parent='all')
    M.mirror('right_shin', 'left_shin', parent='left_leg')
    M.part('body', 'all', (0, -18, 0), (0.1, 0, 0), boxes=[Box(-2.5, -3, -1.5, 5, 3, 3, 'coat', 'hips'), Box(-3.5, -3, -2, 7, 12, 4, 'skirt', 'skirt')])
    M.part('chest', 'body', (0, -3, 0), (0.12, 0, 0), boxes=[Box(-3, -10, -1.5, 6, 10, 3, 'coat', 'chest'), Box(-4, -10.5, -2, 8, 2, 4, 'coat', 'yoke')])
    M.part('neck', 'chest', (0, -10, 0), (-0.1, 0, 0), boxes=[Box(-1, -4, -1, 2, 4, 2, 'limb', 'neck')])
    M.part('head', 'neck', (0, -4, 0), (0.05, 0, 0), boxes=[Box(-2.5, -6, -2.5, 5, 6, 5, 'void', 'head')])
    # the hands over the face: one from either side, one over the crown from behind
    M.part('palm_r', 'head', (-3, -2.5, -3.2), (0, 0.12, 0), boxes=[Box(0, -5, -0.5, 4, 8, 1, 'palm', 'palm_r')])
    M.part('palm_l', 'head', (3, -2.5, -3.2), (0, -0.12, 0), boxes=[Box(-4, -5, -0.5, 4, 8, 1, 'palm', 'palm_l')])
    M.part('crown', 'head', (0, -6.3, 2.5), (0.15, 0, 0), boxes=[Box(-2, -0.5, -7, 4, 1, 7, 'crown', 'crown')])
    M.part('right_arm', 'chest', (-4.2, -9, 0), (0, 0, 0.06), boxes=[Box(-1, 0, -1, 2, 12, 2, 'coat', 'sleeve')])
    M.part('right_fore', 'right_arm', (0, 12, 0), (-0.12, 0, 0), boxes=[Box(-1, 0, -1, 2, 12, 2, 'limb', 'fore')])
    M.part('right_hand', 'right_fore', (0, 12, 0), (0, 0, 0), boxes=[Box(-2.5, 0, -0.5, 5, 8, 1, 'hang', 'hand_r')])
    M.mirror('right_arm', 'left_arm', parent='chest')
    M.mirror('right_fore', 'left_fore', parent='left_arm')
    M.mirror('right_hand', 'left_hand', parent='left_fore')
    M.pack()
    return M


def paint_usher(M):
    p = Painter(M, 811)
    for name, _, b in M.boxes():
        if b.mat == 'coat':
            p.fill_box(b, night('uc'))
            starry(p, b, 'u', 0.022)
        elif b.mat == 'dark':
            p.fill_box(b, night('ud', light=0.8))
        elif b.mat == 'skirt':
            base = night('us')

            def skirt(face, px, py, fw, fh, rnd):
                if face in ('top', 'bottom'):
                    return None
                # the hem hangs in tatters: longer here, shorter there
                rag = int(blotch(f'hem{face}', px, 0, 1.3) * 5)
                if py >= fh - rag:
                    return None
                return base(face, px, py, fw, fh, rnd)
            p.fill_box(b, skirt)
            starry(p, b, 'us', 0.025)
        elif b.mat == 'limb':
            p.fill_box(b, skin('ul', PALE_D, PALE_DD))
        elif b.mat == 'void':
            def void(face, px, py, fw, fh, rnd):
                return shade(NIGHT_D, 1.0 if face != 'top' else 1.4)
            p.fill_box(b, void)
            # what is behind the hands: a ring of light round nothing
            fx, fy, fw, fh = b.faces()['front']
            for x in range(fw):
                for y in range(fh):
                    r = math.hypot(x - (fw - 1) / 2, y - (fh - 1) / 2)
                    if 1.3 <= r < 2.3:
                        p.px(fx + x, fy + y, STAR if r < 1.9 else VIOLET_L, glow=True)
        elif b.mat == 'palm':
            p.fill_box(b, hand_up(mirrored=name.endswith('_l')))
        elif b.mat == 'crown':
            p.fill_box(b, hand_flat(tip='z0'))
        elif b.mat == 'hang':
            p.fill_box(b, flip(hand_up(mirrored=name.startswith('left'))))
    return p.tex, p.glow


# the poses of UsherModel.setupAnim: walking; the hands off its face and its arms out; the lift; the arm brought down
USHER_POSES = (
    ('stand', {}),
    ('step', {'right_leg': (-0.5, 0, 0), 'right_shin': (0.5, 0, 0), 'left_leg': (0.4, 0, 0), 'right_arm': (0.25, 0, 0), 'left_arm': (-0.25, 0, 0)}),
    ('open', {'palm_r': (0, 1.9, 0), 'palm_l': (0, -1.9, 0), 'crown': (-1.3, 0, 0), 'chest': (0.25, 0, 0), 'right_arm': (-1.2, 0, 0.25), 'left_arm': (-1.2, 0, -0.25),
              'right_fore': (-0.5, 0, 0), 'left_fore': (-0.5, 0, 0)}),
    ('lift', {'palm_r': (0, 1.9, 0), 'palm_l': (0, -1.9, 0), 'crown': (-1.3, 0, 0), 'chest': (-0.15, 0, 0), 'right_arm': (-2.7, 0, 0.2), 'left_arm': (-2.7, 0, -0.2)}),
    ('reach_up', {'chest': (-0.1, 0.3, 0), 'right_arm': (-3.0, 0, 0.1), 'right_fore': (0.3, 0, 0)}),
    ('reach_down', {'chest': (0.5, -0.3, 0), 'body': (0.25, 0, 0), 'right_arm': (-1.0, 0, 0.1), 'right_fore': (-0.1, 0, 0)}),
)

# ---------------------------------------------------------------------------------------------------- the stray shadow
# A shadow that has outlived whoever cast it: it lies on the floor, flat, in a pool of itself, and goes about there; to
# strike it stands up out of the floor - a man's outline, a hand's breadth thick, with fingers a good deal too long -
# and then it can be struck. The pool stays on the floor; 'figure' is the part that lies (its rest) and stands.
SHADOW_SCALE = 1.2              # must match MODEL_SCALE in StrayShadowModel.java


def shadow():
    M = Model(64, 64, SHADOW_SCALE)
    M.part('all', None, (0, 24, 0))
    M.part('pool', 'all', (0, 0, 0), boxes=[Box(-7, -1, -7, 14, 1, 14, 'pool', 'pool')])
    M.part('figure', 'all', (0, -1, 0), (1.5, 0, 0), boxes=[Box(-3.5, -13, -0.5, 3, 13, 1, 'shade', 'leg_r'), Box(0.5, -13, -0.5, 3, 13, 1, 'shade', 'leg_l')])
    M.part('torso', 'figure', (0, -13, 0), boxes=[Box(-4.5, -12, -0.5, 9, 12, 1, 'shade', 'torso')])
    M.part('head', 'torso', (0, -12, 0), boxes=[Box(-3, -8, -0.5, 6, 8, 1, 'shade', 'head')])
    M.part('right_arm', 'torso', (-4.5, -11, 0), (0, 0, 2.7), boxes=[Box(-2, 0, -0.5, 2, 11, 1, 'shade', 'arm')])
    M.part('right_fore', 'right_arm', (-1, 11, 0), (0, 0, -0.3), boxes=[Box(-1, 0, -0.5, 2, 10, 1, 'shade', 'fore'), Box(-2.5, 9, -0.5, 5, 7, 1, 'shade', 'claw')])
    M.mirror('right_arm', 'left_arm', parent='torso')
    M.mirror('right_fore', 'left_fore', parent='left_arm')
    M.pack()
    return M


def paint_shadow(M):
    p = Painter(M, 822)
    INK = (9, 9, 17)
    INK_L = (22, 22, 40)
    for name, _, b in M.boxes():
        if b.mat == 'pool':
            def pool(face, px, py, fw, fh, rnd):
                if face != 'top':
                    return None
                r = math.hypot(px - (fw - 1) / 2, py - (fh - 1) / 2) / (fw / 2)
                edge = 0.72 + (blotch('pool', px, py, 2.2) - 0.5) * 0.5
                if r > edge:
                    return None
                return INK_L if r > edge - 0.16 else INK
            p.fill_box(b, pool)
            continue

        def ink(face, px, py, fw, fh, rnd, tag=b.tag, left=name.startswith('left')):
            if face not in ('front', 'back'):
                return None
            x = px if face == 'front' else fw - 1 - px
            if left:
                x = fw - 1 - x
            mid = (fw - 1) / 2
            if tag == 'head':
                # long, with no neck to speak of
                rx, ry = (x - mid) / (fw / 2), (py - (fh - 1) / 2) / (fh / 2)
                if rx * rx + ry * ry > 1.05:
                    return None
            elif tag == 'torso':
                # wide at the shoulders, drawn in at the waist; the edges are not straight
                half = fw / 2 - 1.6 * (py / (fh - 1)) + (blotch('tor', x, py, 1.5) - 0.5) * 0.8
                if abs(x - mid) > half:
                    return None
            elif tag in ('leg_r', 'leg_l'):
                half = 1.6 - 0.7 * (1 - py / (fh - 1)) * 0 + (blotch(tag, x, py, 1.4) - 0.5) * 0.7
                if abs(x - mid) > half:
                    return None
            elif tag == 'claw':
                # five fingers, each a line, and too long
                if py < 2:
                    return INK if abs(x - mid) < 2.4 else None
                if x % 2 == 1 and fw > 3:
                    return None
                if py > fh - 3 and x in (0, fw - 1):
                    return None
            edge = min(x, fw - 1 - x) == 0 and tag not in ('claw', 'arm', 'fore')
            return INK_L if edge else INK
        p.fill_box(b, ink)
        if b.tag == 'head':
            for x, y in ((1, 3), (4, 3)):
                p.put(b, 'front', x, y, STAR, glow=True)
            p.put(b, 'front', 1, 4, STAR_D, glow=True)
            p.put(b, 'front', 4, 4, STAR_D, glow=True)
    return p.tex, p.glow


# lying (its rest), half up, up with its arms spread, the claws through
SHADOW_POSES = (
    ('lying', {}),
    ('crawl', {'right_arm': (0, 0, 0.3), 'left_arm': (0, 0, 0.2), 'torso': (0, 0, 0.12)}),
    ('rising', {'figure': (-0.8, 0, 0), 'right_arm': (0, 0, -1.0), 'left_arm': (0, 0, 1.0)}),
    ('up', {'figure': (-1.5, 0, 0), 'right_arm': (0, 0, -1.4), 'left_arm': (0, 0, 1.4), 'torso': (-0.2, 0, 0)}),
    ('claws', {'figure': (-1.4, 0, 0), 'torso': (0.45, 0, 0), 'right_arm': (-1.5, 0, -2.4), 'left_arm': (-1.5, 0, 2.4), 'right_fore': (0, 0, 0.3), 'left_fore': (0, 0, -0.3)}),
)


# ---------------------------------------------------------------------------------------------------- the long-armed
# Something that is in the wall: a hump of it shows in a niche, without a head, and two arms of three joints each that
# hang folded before it like a mantis's - and are four paces long when they are not folded. Lesser hands hold the edge
# of the niche. It does not come out. It faces -z, out of the wall; the hump is behind the cell's middle.
LONGARM_SCALE = 1.5             # must match MODEL_SCALE in LongarmModel.java


def longarm():
    M = Model(64, 64, LONGARM_SCALE)
    M.part('all', None, (0, 24, 0))
    M.part('hump', 'all', (0, -12, 3), boxes=[Box(-6, -13, -3, 12, 13, 8, 'hump', 'hump'), Box(-5, 0, -1, 10, 12, 5, 'rags', 'rags')])
    M.part('right_arm', 'hump', (-5, -10, -3), (-0.9, 0, 0.12), boxes=[Box(-1.5, 0, -1.5, 3, 13, 3, 'arm', 'upper')])
    M.part('right_fore', 'right_arm', (0, 13, 0), (2.25, 0, 0), boxes=[Box(-1, 0, -1, 2, 13, 2, 'arm', 'fore')])
    M.part('right_wrist', 'right_fore', (0, 13, 0), (-2.3, 0, 0), boxes=[Box(-1, 0, -1, 2, 12, 2, 'limb', 'wrist')])
    M.part('right_hand', 'right_wrist', (0, 12, 0), (0.7, 0, 0), boxes=[Box(-3, 0, -0.5, 6, 9, 1, 'hang', 'hand')])
    for src in ('right_arm', 'right_fore', 'right_wrist', 'right_hand'):
        dst = src.replace('right', 'left')
        M.mirror(src, dst, parent='hump' if src == 'right_arm' else {'right_fore': 'left_arm', 'right_wrist': 'left_fore', 'right_hand': 'left_wrist'}[src])
    # the hands on the edge of the niche
    M.part('cling_r', 'hump', (-6.5, -7, -3), (0, 0, 0.5), boxes=[Box(-0.5, 0, -0.5, 1, 6, 1, 'limb', 'cling'), Box(-1.5, 6, -0.5, 3, 4, 1, 'hang', 'cling_hand')])
    M.part('cling_l', 'hump', (6.5, -3, -3), (0, 0, -0.9), boxes=[Box(-0.5, 0, -0.5, 1, 6, 1, 'limb', 'cling'), Box(-1.5, 6, -0.5, 3, 4, 1, 'hang', 'cling_hand')])
    M.part('cling_t', 'hump', (2, -13, -3), (0, 0, 2.6), boxes=[Box(-0.5, 0, -0.5, 1, 5, 1, 'limb', 'cling'), Box(-1.5, 5, -0.5, 3, 4, 1, 'hang', 'cling_hand')])
    M.pack()
    return M


def paint_longarm(M):
    p = Painter(M, 833)
    for name, _, b in M.boxes():
        if b.mat == 'hump':
            p.fill_box(b, night('lh'))
            starry(p, b, 'lh', 0.02)
            cracks(p, b, 'lh', 1, faces=('front',), colour=VIOLET_D, bright=VIOLET)
        elif b.mat == 'rags':
            base = night('lr', light=0.85)

            def rags(face, px, py, fw, fh, rnd):
                if face in ('top', 'bottom'):
                    return None
                if px % 3 == 2 and py > 1 or py >= fh - int(blotch(f'rag{face}', px // 3, 0, 1.1) * 7):
                    return None
                return base(face, px, py, fw, fh, rnd)
            p.fill_box(b, rags)
        elif b.mat == 'arm':
            # night at the shoulder, skin by the elbow: it pales towards what it holds with
            dark = night('la', light=0.9)
            pale = skin('la', PALE_D, PALE_DD)

            def arm(face, px, py, fw, fh, rnd, tag=b.tag):
                t = py / max(1, fh - 1) if face not in ('top', 'bottom') else (0.0 if face == 'top' else 1.0)
                if tag == 'upper':
                    t = t * 0.4
                else:
                    t = 0.4 + t * 0.6
                a, c = dark(face, px, py, fw, fh, rnd), pale(face, px, py, fw, fh, rnd)
                return mix(a, c, min(1.0, max(0.0, t + (blotch('lat', px, py, 2.0) - 0.5) * 0.3)))
            p.fill_box(b, arm)
        elif b.mat == 'limb':
            p.fill_box(b, skin('ll'))
        elif b.mat == 'hang':
            p.fill_box(b, flip(hand_up(mirrored=name.startswith('left') or name == 'cling_l')))
    return p.tex, p.glow


# folded (its rest); the fingers working; drawn back; out at full length; both arms wide, raking
LONGARM_POSES = (
    ('folded', {}),
    ('twitch', {'right_hand': (0.5, 0, 0), 'left_wrist': (0.3, 0, 0)}),
    ('drawn', {'right_arm': (-0.5, 0, 0), 'left_arm': (-0.5, 0, 0), 'right_fore': (0.4, 0, 0), 'left_fore': (0.4, 0, 0)}),
    ('grab', {'right_arm': (-0.62, 0, -0.1), 'left_arm': (-0.62, 0, 0.1), 'right_fore': (-2.2, 0, 0), 'left_fore': (-2.2, 0, 0), 'right_wrist': (2.3, 0, 0),
              'left_wrist': (2.3, 0, 0), 'right_hand': (-0.9, 0, 0), 'left_hand': (-0.9, 0, 0)}),
    ('rake', {'right_arm': (-0.55, 0.9, 0), 'left_arm': (-0.55, -0.9, 0), 'right_fore': (-2.2, 0, 0), 'left_fore': (-2.2, 0, 0), 'right_wrist': (2.0, 0, 0),
              'left_wrist': (2.0, 0, 0), 'right_hand': (-0.7, 0, 0), 'left_hand': (-0.7, 0, 0)}),
)


# ---------------------------------------------------------------------------------------------------- the handwalker
# A thing of many joints, four paces long, that runs on hands: a man's hands, a pair to every joint, flat on the
# floor. Its jaws are two more. It hangs from 'mid', the joint in the middle of it; 'front1', 'front0' and the head go
# forward from there, 'rear0'..'rear3' and the tail back.
WALKER_SCALE = 1.15             # must match MODEL_SCALE in HandwalkerModel.java
WALKER_FRONT = ('front1', 'front0')
WALKER_REAR = ('rear0', 'rear1', 'rear2', 'rear3')


def walker():
    M = Model(128, 64, WALKER_SCALE)
    M.part('all', None, (0, 24, 0))
    M.part('mid', 'all', (0, -6, 0), boxes=[Box(-3.5, -2.5, -3.5, 7, 5, 7, 'shell', 'mid')])

    def legs(seg, z, n):
        M.part(f'leg_r_{n}', seg, (-3, 0.5, z), (0, 0, -0.6), boxes=[Box(-5, -1, -1, 5, 2, 2, 'shell', 'leg')])
        M.part(f'hand_r_{n}', f'leg_r_{n}', (-5, 0, 0), (0, 0, 0.6), boxes=[Box(-1, 0, -1, 2, 3, 2, 'limb', 'wrist'), Box(-2.5, 2.5, -5, 5, 1, 6, 'hand', 'hand')])
        M.mirror(f'leg_r_{n}', f'leg_l_{n}', parent=seg)
        M.mirror(f'hand_r_{n}', f'hand_l_{n}', parent=f'leg_l_{n}')
    legs('mid', 0, 2)
    M.part('front1', 'mid', (0, 0, -3.5), boxes=[Box(-3.5, -2.5, -7, 7, 5, 7, 'shell', 'front1')])
    legs('front1', -3.5, 1)
    M.part('front0', 'front1', (0, 0, -7), boxes=[Box(-3, -2.5, -7, 6, 5, 7, 'shell', 'front0')])
    legs('front0', -3.5, 0)
    M.part('head', 'front0', (0, 0, -7), boxes=[Box(-2.5, -2, -5, 5, 4, 5, 'head', 'head'), Box(-2, -3.5, -9, 1, 1, 7, 'feeler', 'feeler_r'), Box(1, -3.5, -9, 1, 1, 7, 'feeler', 'feeler_l')])
    M.part('jaw_r', 'head', (-2, 1.5, -5), (0, 0.45, 0), boxes=[Box(-2.5, -0.5, -7, 5, 1, 7, 'hand', 'jaw')])
    M.mirror('jaw_r', 'jaw_l', parent='head')
    prev, width = 'mid', (6, 6, 5, 4)
    for i, name in enumerate(WALKER_REAR):
        w = width[i]
        M.part(name, prev, (0, 0, 3.5 if prev == 'mid' else 7), boxes=[Box(-w / 2, -2 if i else -2.5, 0, w, 4 if i else 5, 7, 'shell', name)])
        if i < 3:
            legs(name, 3.5, 3 + i)
        prev = name
    M.part('tail', prev, (0, 0, 7), boxes=[Box(-1, -1, 0, 2, 2, 7, 'shell', 'tail'), Box(-2.5, -0.5, 6, 5, 1, 7, 'tailhand', 'tailhand')])
    M.pack()
    return M


def paint_walker(M):
    p = Painter(M, 844)
    for name, _, b in M.boxes():
        if b.mat == 'shell':
            base = night('ws', light=1.15)

            def shell(face, px, py, fw, fh, rnd, tag=b.tag):
                c = base(face, px, py, fw, fh, rnd)
                if tag != 'leg' and face == 'top' and py == fh - 1:
                    return shade(VIOLET_D, 0.9)                       # the edge of a plate over the next
                if tag != 'leg' and face in ('left', 'right') and py == fh - 1:
                    return shade(PALE_DD, 0.9)                        # and the belly under it
                if face == 'bottom':
                    return shade(PALE_DD, 0.8)
                return c
            p.fill_box(b, shell)
            if b.tag != 'leg':
                starry(p, b, 'w', 0.02, faces=('top', 'left', 'right'))
        elif b.mat == 'head':
            p.fill_box(b, night('wh', light=1.3))
            # no eyes: two rows of pits where they were
            for x in (0, 1, 3, 4):
                p.put(b, 'front', x, 1, NIGHT_D)
            p.put(b, 'front', 2, 3, RAW_D)
        elif b.mat == 'feeler':
            p.fill_box(b, skin('wf', PALE_D, PALE_DD))
        elif b.mat == 'limb':
            p.fill_box(b, skin('wl', PALE_D, PALE_DD))
        elif b.mat == 'hand':
            p.fill_box(b, hand_flat(mirrored=name.endswith('_l') or '_l_' in name, tip='z0'))
        elif b.mat == 'tailhand':
            p.fill_box(b, hand_flat(tip='z1'))
    return p.tex, p.glow


def walker_wave(k, lift=0.0):
    """The body in a wave: every joint turned a little against the one before."""
    pose = {}
    for i, name in enumerate(WALKER_FRONT + WALKER_REAR + ('tail',)):
        pose[name] = (lift if name in WALKER_FRONT else 0, math.sin(i * 1.1 + k) * 0.22, 0)
    return pose


WALKER_POSES = (
    ('stand', {}),
    ('run', walker_wave(0.6)),
    ('reared', {'front1': (-0.55, 0, 0), 'front0': (-0.45, 0, 0), 'head': (0.5, 0, 0), 'jaw_r': (0, -0.6, 0), 'jaw_l': (0, 0.6, 0)}),
    ('lunge', {'front1': (0.1, 0, 0), 'front0': (0.05, 0, 0), 'jaw_r': (0, 0.3, 0), 'jaw_l': (0, -0.3, 0)}),
    ('coil', {'rear0': (0, 0.55, 0), 'rear1': (0, 0.6, 0), 'rear2': (0, 0.6, 0), 'rear3': (0, 0.6, 0), 'tail': (-0.3, 0.6, 0), 'front1': (0, -0.3, 0)}),
    ('whip', {'rear0': (0, -0.5, 0), 'rear1': (0, -0.55, 0), 'rear2': (0, -0.55, 0), 'rear3': (0, -0.5, 0), 'tail': (0, -0.5, 0)}),
)


# ---------------------------------------------------------------------------------------------------- the deepling
# A fish of the bottom of a sea, blind and the colour of something kept in spirit, swimming in the air of the place as
# if the place were water - it is the bottom of a sky. Over its head it carries a light on a stalk, which is violet
# and looks like one of the crystals; under the light is the mouth, and in the mouth, where a throat should be, eyes.
DEEPLING_SCALE = 1.5            # must match MODEL_SCALE in DeeplingModel.java


def deepling():
    M = Model(128, 64, DEEPLING_SCALE)
    M.part('all', None, (0, 24, 0))
    M.part('body', 'all', (0, -8, 0), boxes=[Box(-5, -5, -5, 10, 10, 11, 'hide', 'body'), Box(-4, -7, -4, 8, 2, 8, 'hide', 'hump'),
                                             Box(-5, -5, -9, 10, 5, 4, 'hide', 'snout'), Box(-4, -1.5, -8.5, 8, 5, 1, 'gullet', 'gullet')])
    M.part('fangs', 'body', (0, 0, -9), boxes=[Box(-5, 0, 0, 10, 4, 1, 'fangs_down', 'fangs_front'), Box(-5, 0, 1, 1, 3, 3, 'fangs_down', 'fangs_r'),
                                              Box(4, 0, 1, 1, 3, 3, 'fangs_down', 'fangs_l')])
    M.part('jaw', 'body', (0, 2, -5), (0.25, 0, 0), boxes=[Box(-5, 0, -5, 10, 3, 6, 'hide', 'jaw'), Box(-5, -4, -5, 10, 4, 1, 'fangs_up', 'jaw_front'),
                                                           Box(-5, -3, -4, 1, 3, 4, 'fangs_up', 'jaw_r'), Box(4, -3, -4, 1, 3, 4, 'fangs_up', 'jaw_l')])
    M.part('stalk', 'body', (0, -7, -2), (-0.7, 0, 0), boxes=[Box(-0.5, -7, -0.5, 1, 7, 1, 'stalk', 'stalk')])
    M.part('stalk2', 'stalk', (0, -7, 0), (-1.1, 0, 0), boxes=[Box(-0.5, -6, -0.5, 1, 6, 1, 'stalk', 'stalk2')])
    M.part('lure', 'stalk2', (0, -6, 0), (-1.2, 0, 0), boxes=[Box(-1, -5, -1, 2, 5, 2, 'lure', 'lure'), Box(-1.5, -3, -1.5, 3, 2, 3, 'lure', 'lure_wide')])
    M.part('fin_r', 'body', (-5, 0, 0), (0, 0.5, 0.3), boxes=[Box(-7, -3, 0, 7, 7, 1, 'fin', 'fin')])
    M.mirror('fin_r', 'fin_l', parent='body')
    M.part('tail', 'body', (0, 0, 6), boxes=[Box(-3.5, -3.5, 0, 7, 7, 5, 'hide', 'tail')])
    M.part('tail2', 'tail', (0, 0, 5), boxes=[Box(-2, -2.5, 0, 4, 5, 5, 'hide', 'tail2')])
    M.part('fluke', 'tail2', (0, 0, 5), boxes=[Box(-0.5, -7, 0, 1, 14, 7, 'fluke', 'fluke')])
    M.part('barbel_r', 'jaw', (-3, 3, -1), (0.7, 0, 0.15), boxes=[Box(-0.5, 0, -0.5, 1, 4, 1, 'stalk', 'barbel')])
    M.mirror('barbel_r', 'barbel_l', parent='jaw')
    M.pack()
    return M


def paint_deepling(M):
    p = Painter(M, 855)
    HIDE = (104, 96, 122)
    HIDE_D = (66, 60, 86)
    BELLY = (150, 142, 160)
    for name, _, b in M.boxes():
        if b.mat == 'hide':
            def hide(face, px, py, fw, fh, rnd, tag=b.tag):
                c = HIDE_D if blotch(f'dh{tag}{face}', px, py, 2.6) > 0.55 else HIDE
                if face == 'bottom' or (face not in ('top',) and py >= fh - 2 and tag in ('body', 'jaw', 'tail')):
                    c = mix(c, BELLY, 0.6)
                f = {'top': 0.85, 'bottom': 0.8}.get(face, 1.0)
                return shade(c, f * rnd.choice((0.94, 1.0, 1.0, 1.05)))
            p.fill_box(b, hide)
            if b.tag == 'snout':
                # where eyes were: two healed places
                for x in (1, 2, 7, 8):
                    p.put(b, 'front', x, 1, HIDE_D)
        elif b.mat == 'gullet':
            def gullet(face, px, py, fw, fh, rnd):
                if face != 'front':
                    return shade(RAW_D, 0.5)
                return shade(RAW_D, 0.6 + 0.3 * blotch('gul', px, py, 2.0))
            p.fill_box(b, gullet)
            for x, y in ((1, 1), (3, 2), (5, 1), (6, 3), (2, 3), (4, 0)):
                p.put(b, 'front', x, y, STAR, glow=True)
                p.put(b, 'front', x, y + 1, NIGHT_D)
        elif b.mat in ('fangs_down', 'fangs_up'):
            def fangs(face, px, py, fw, fh, rnd, down=b.mat == 'fangs_down', tag=b.tag):
                if face in ('top', 'bottom'):
                    return None
                # needles: every other texel, of two lengths
                long = fh if (px // 2) % 2 == 0 else fh - 1
                y = py if down else fh - 1 - py
                if px % 2 == 1 or y >= long:
                    return None
                if tag in ('fangs_front', 'jaw_front') and face in ('left', 'right'):
                    return None
                if tag not in ('fangs_front', 'jaw_front') and face in ('front', 'back'):
                    return None
                return TOOTH if y < long - 1 else TOOTH_D
            p.fill_box(b, fangs)
        elif b.mat == 'stalk':
            p.fill_box(b, lambda face, px, py, fw, fh, rnd: shade(HIDE_D, rnd.choice((0.8, 1.0))))
        elif b.mat == 'lure':
            def lure(face, px, py, fw, fh, rnd):
                return VIOLET_L if (px + py) % 3 == 0 else (VIOLET if rnd.random() < 0.7 else STAR)
            p.fill_box(b, lure)
            for face, (fx, fy, fw, fh) in b.faces().items():
                for x in range(fw):
                    for y in range(fh):
                        c = p.tex.getpixel((fx + x, fy + y))
                        p.px(fx + x, fy + y, c, glow=True)
        elif b.mat in ('fin', 'fluke'):
            def fin(face, px, py, fw, fh, rnd, fluke=b.mat == 'fluke', left=name.endswith('_l')):
                if fluke:
                    if face not in ('left', 'right'):
                        return None
                    x = px if face == 'right' else fw - 1 - px                 # 0 at the root
                    x = fw - 1 - x
                else:
                    if face not in ('front', 'back'):
                        return None
                    x = px if face == 'front' else fw - 1 - px
                    if left:
                        x = fw - 1 - x
                    x = fw - 1 - x                                            # 0 at the root
                # a web between rays, torn at the edge
                half = (fh / 2) * (0.35 + 0.65 * (x / (fw - 1))) + (blotch('fin', x, py, 1.3) - 0.5) * 1.4
                if abs(py - (fh - 1) / 2) > half:
                    return None
                if x == fw - 1 and py % 2:
                    return None
                return shade(HIDE_D if py % 2 == 0 else HIDE, 0.9)
            p.fill_box(b, fin)
    return p.tex, p.glow


DEEPLING_POSES = (
    ('hang', {}),
    ('swim', {'tail': (0, 0.35, 0), 'tail2': (0, 0.4, 0), 'fluke': (0, 0.3, 0), 'fin_r': (0, 0.3, 0), 'fin_l': (0, -0.3, 0)}),
    ('gape', {'jaw': (0.75, 0, 0), 'body': (-0.3, 0, 0), 'stalk': (-0.5, 0, 0)}),
    ('bite', {'jaw': (-0.2, 0, 0), 'body': (0.15, 0, 0)}),
    ('glare', {'jaw': (1.0, 0, 0), 'body': (-0.15, 0, 0), 'fin_r': (0, -0.6, 0), 'fin_l': (0, 0.6, 0)}),
)


# ---------------------------------------------------------------------------------------------------- the gut
# A beast the size of a bull with no head: its neck ends in a healed stump. Its back opens from end to end, and that
# is the mouth - two lips of hide with teeth along them, and eyes down in the red of it. It stands on four fists.
# 'body' hangs at the hind quarters, so that it can rear; the hind legs stay on the ground.
GUT_SCALE = 1.5                 # must match MODEL_SCALE in GutModel.java


def gut():
    M = Model(128, 128, GUT_SCALE)
    M.part('all', None, (0, 24, 0))
    M.part('body', 'all', (0, -17, 7), boxes=[Box(-6, -5, -18, 12, 10, 21, 'hide', 'trunk'), Box(-5, -5.5, -17, 10, 1, 19, 'maw', 'maw')])
    M.part('stump', 'body', (0, -1, -18), (0.2, 0, 0), boxes=[Box(-4, -4, -4, 8, 8, 4, 'hide', 'stump'), Box(-3, -3, -5, 6, 6, 1, 'scar', 'scar')])
    M.part('lip_r', 'body', (-6, -5, 0), boxes=[Box(0, -3, -17.5, 6, 3, 20, 'hide', 'lip'), Box(5, -1, -17, 1, 4, 19, 'teeth', 'teeth')])
    M.mirror('lip_r', 'lip_l', parent='body')
    M.part('fore_r', 'body', (-4.5, 4, -14), boxes=[Box(-2.5, 0, -2.5, 5, 7, 5, 'hide', 'thigh')])
    M.part('fore_shin_r', 'fore_r', (0, 7, 0), boxes=[Box(-2, 0, -2, 4, 4, 4, 'hide', 'shin'), Box(-2.5, 4, -3.5, 5, 2, 6, 'fist', 'fist')])
    M.mirror('fore_r', 'fore_l', parent='body')
    M.mirror('fore_shin_r', 'fore_shin_l', parent='fore_l')
    M.part('hind_r', 'all', (-4.5, -13, 7), boxes=[Box(-2.5, 0, -2.5, 5, 7, 5, 'hide', 'thigh')])
    M.part('hind_shin_r', 'hind_r', (0, 7, 0), boxes=[Box(-2, 0, -2, 4, 4, 4, 'hide', 'shin'), Box(-2.5, 4, -3.5, 5, 2, 6, 'fist', 'fist')])
    M.mirror('hind_r', 'hind_l', parent='all')
    M.mirror('hind_shin_r', 'hind_shin_l', parent='hind_l')
    M.part('tail', 'body', (0, -2, 3), (0.9, 0, 0), boxes=[Box(-1, 0, -1, 2, 9, 2, 'hide', 'tail'), Box(-2, 9, -0.5, 4, 5, 1, 'hang', 'tail_hand')])
    M.pack()
    return M


def paint_gut(M):
    p = Painter(M, 866)
    HIDE = (40, 34, 52)
    HIDE_D = (24, 20, 34)
    HIDE_L = (62, 54, 78)
    for name, _, b in M.boxes():
        if b.mat == 'hide':
            def hide(face, px, py, fw, fh, rnd, tag=b.tag):
                c = HIDE_D if blotch(f'gh{tag}{face}', px, py, 3.0) > 0.58 else HIDE
                if face == 'top':
                    c = mix(c, HIDE_L, 0.5)
                f = {'top': 1.1, 'bottom': 0.7}.get(face, 1.02 - 0.2 * (py / max(1, fh - 1)))
                # the hide in folds down its flanks
                if tag == 'trunk' and face in ('left', 'right') and px % 5 == 2 and py > 1:
                    f *= 0.72
                return shade(c, f * rnd.choice((0.93, 1.0, 1.0, 1.06)))
            p.fill_box(b, hide)
            if b.tag == 'trunk':
                cracks(p, b, 'gut', 2, faces=('left', 'right'), colour=VIOLET_D, bright=VIOLET)
            if b.tag == 'lip':
                # the inner side of a lip is raw
                fx, fy, fw, fh = b.faces()['left' if not name.endswith('_l') else 'right']
                for x in range(fw):
                    for y in range(fh):
                        p.px(fx + x, fy + y, shade(RAW, 0.8 + 0.2 * ((x + y) % 2)))
                fx, fy, fw, fh = b.faces()['bottom']
                for x in range(fw):
                    for y in range(fh):
                        p.px(fx + x, fy + y, shade(RAW_D, 0.9 + 0.2 * ((x + y) % 2)))
        elif b.mat == 'maw':
            def maw(face, px, py, fw, fh, rnd):
                if face != 'top':
                    return None
                return shade(RAW if blotch('maw', px, py, 2.0) > 0.5 else RAW_D, 0.75)
            p.fill_box(b, maw)
            rnd_eyes = ((2, 3), (6, 5), (3, 8), (7, 11), (2, 13), (5, 15), (7, 2), (4, 17))
            for x, y in rnd_eyes:
                p.put(b, 'top', x, y, STAR, glow=True)
                p.put(b, 'top', x, y + 1, NIGHT_D)
        elif b.mat == 'scar':
            def scar(face, px, py, fw, fh, rnd):
                if face != 'front':
                    return None
                # where the neck was: a seam, drawn together long ago
                mid = (fw - 1) / 2
                if abs(px - mid) < 0.6 and 0 < py < fh - 1:
                    return RAW_D if py % 2 else PALE_DD
                if abs(px - mid) < 1.6 and py % 2 == 1 and 0 < py < fh - 1:
                    return PALE_DD
                return None
            p.fill_box(b, scar)
        elif b.mat == 'teeth':
            def teeth(face, px, py, fw, fh, rnd):
                if face not in ('left', 'right'):
                    return None
                long = fh if (px // 2) % 3 == 0 else fh - 1
                if px % 2 == 1 or py >= long:
                    return None
                return TOOTH if py < long - 1 else TOOTH_D
            p.fill_box(b, teeth)
        elif b.mat == 'fist':
            p.fill_box(b, skin('gf', PALE_D, PALE_DD))
            for x in (1, 3):
                p.put(b, 'front', x, 0, NAIL)
                p.put(b, 'top', x, b.td - 1, PALE_DD)
        elif b.mat == 'hang':
            p.fill_box(b, flip(hand_up()))
    return p.tex, p.glow


# standing; its back open; pawing before a charge; reared; come down
GUT_POSES = (
    ('stand', {}),
    ('gape', {'lip_r': (0, 0, -1.15), 'lip_l': (0, 0, 1.15)}),
    ('paw', {'body': (0.12, 0, 0), 'fore_r': (-0.7, 0, 0), 'fore_shin_r': (1.0, 0, 0), 'lip_r': (0, 0, -0.3), 'lip_l': (0, 0, 0.3)}),
    ('reared', {'body': (-0.75, 0, 0), 'fore_r': (-0.5, 0, 0), 'fore_l': (-0.9, 0, 0), 'fore_shin_r': (0.8, 0, 0), 'fore_shin_l': (0.6, 0, 0),
                'lip_r': (0, 0, -0.9), 'lip_l': (0, 0, 0.9)}),
    ('down', {'body': (0.2, 0, 0), 'fore_r': (-0.35, 0, 0), 'fore_l': (-0.35, 0, 0), 'lip_r': (0, 0, -1.2), 'lip_l': (0, 0, 1.2)}),
)

MODELS = {
    'usher': (usher, paint_usher, USHER_POSES, 'UsherModel', dict(zoom=4.6, centre=(0, -4))),
    'stray_shadow': (shadow, paint_shadow, SHADOW_POSES, 'StrayShadowModel', dict(zoom=5.0, centre=(0, 6))),
    'longarm': (longarm, paint_longarm, LONGARM_POSES, 'LongarmModel', dict(zoom=4.4, centre=(0, 2))),
    'handwalker': (walker, paint_walker, WALKER_POSES, 'HandwalkerModel', dict(zoom=5.6, centre=(0, 14))),
    'deepling': (deepling, paint_deepling, DEEPLING_POSES, 'DeeplingModel', dict(zoom=6.5, centre=(0, 8))),
    'gut': (gut, paint_gut, GUT_POSES, 'GutModel', dict(zoom=5.0, centre=(0, 4))),
}

if __name__ == '__main__':
    only = sys.argv[3].split(',') if len(sys.argv) > 3 else None
    rows = []
    for name, (build, paint, poses, java, cam) in MODELS.items():
        if only and name not in only:
            continue
        M = build()
        if len(sys.argv) < 3:
            M.write_java(f'{J}/{java}.java')
        tex, glow = paint(M)
        tex.save(f'{A}/textures/entity/{name}.png')
        glow.save(f'{A}/textures/entity/{name}_glow.png')
        if len(sys.argv) > 1:
            shots = [M.render(tex, yaw=yaw, pitch=0.12, pose=pose, size=(250, 340), glow=glow, background=BACK, **cam)
                     for yaw, pose in [(-0.5, poses[0][1]), (0.0, poses[0][1]), (2.6, poses[0][1]), (-1.35, poses[0][1])] + [(-0.6, pose) for _, pose in poses[1:]]]
            rows.append(sheet(shots, len(shots), labels=[name] + [''] * 3 + [n for n, _ in poses[1:]]))
    if len(sys.argv) > 1 and rows:
        from PIL import Image
        out = Image.new('RGB', (max(r.width for r in rows), sum(r.height for r in rows)), (40, 44, 52))
        y = 0
        for r in rows:
            out.paste(r, (0, y))
            y += r.height
        out.save(sys.argv[1])
