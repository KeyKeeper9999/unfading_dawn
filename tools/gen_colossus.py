#!/usr/bin/env python3
"""The Coming on the horizon - what stands up out of the sea when the bell is struck (ColossusEntity, ColossusModel.java).

It is the whole of what the Coming in the Sky Beneath was a part of, and it is drawn after it: a knot of the night
breaking the water, three necks of it twisted into one rope that stands up out of the knot, on the end of the rope a
black disc with a rim of amber - the sun that did not rise - a broken ring of light behind the disc and a crown of
twelve arms round that, like its rays. Four arms brace the knot in the sea. Round it the sea heaves in rings of foam.

One unit of the model is one block (the scale is 16): it stands about two hundred and twenty blocks high and the crown
is two hundred wide. The animation is by the hand (ColossusModel.setupAnim): the knot comes up out of the water, the arms
brace, the rope uncoils ring by ring, the head lifts and turns to look, the crown opens. What shines (the amber, the
cracks, the eyes) is in the glow texture.

Run from the repo root: python3 tools/gen_colossus.py [preview.png [ages, e.g. 40,300,600]]  (NOJAVA=1 leaves the Java alone)."""
import math
import os
import random
import sys

from duskkit import (AMBER, AMBER_D, AMBER_L, BLUE, BLUE_L, NIGHT, NIGHT_D, NIGHT_L, PALE, PALE_D, PALE_DD, STAR, STAR_D, VIOLET, VIOLET_D, VIOLET_L,
                     cracks, night, skin, starry)
from modelkit import Box, Model, Painter, blotch, mix, shade, sheet

A = 'src/main/resources/assets/unfading_dawn'
JAVA = 'src/main/java/com/keykeeper/dushi/client/ColossusModel.java'
SCALE = 12.0                    # must match MODEL_SCALE in ColossusModel.java: one unit of the model is three quarters of a block
RINGS = 8                       # the rope is so many rings, each a turn of the three necks
RING = 17                       # the height of one
KNOT_TOP = 64                   # the knot is so high; the rope grows from the top of it
TWIST = 0.9                     # how far the necks turn round the rope from one ring to the next
CROWN = 12                      # the arms round the disc
DISC = 80                       # the disc's width
HALO = 112                      # and the ring behind it
SEA = 168                       # the patch of heaving sea round its foot

C = Model(1024, 1024, SCALE)

# ---------------------------------------------------------------------------------------------------- the sea and the knot
C.part('sea', None, (0, 24, 0), boxes=[Box(-SEA / 2, -0.5, -SEA / 2, SEA, 0.5, SEA, 'sea', 'sea')])
C.part('knot', None, (0, 24, 0), boxes=[
    Box(-52, -30, -46, 104, 30, 92, 'hide', 'base'), Box(-40, -52, -36, 80, 22, 72, 'hide', 'mid'), Box(-27, -KNOT_TOP, -25, 54, KNOT_TOP - 52, 50, 'hide', 'crest'),
    Box(-66, -16, -14, 16, 16, 22, 'hide', 'lump'), Box(50, -22, 8, 18, 22, 20, 'hide', 'lump'), Box(-34, -24, 38, 28, 24, 16, 'hide', 'lump'),
    Box(18, -18, -60, 26, 18, 16, 'hide', 'lump'), Box(-58, -30, -38, 18, 30, 16, 'hide', 'lump'), Box(36, -44, -38, 12, 14, 10, 'hide', 'lump'),
    # eyes where eyes should not be: on the front of it, small, and too many
    Box(-30, -46, -37, 7, 7, 1, 'eye', 'e0'), Box(-14, -41, -37, 5, 5, 1, 'eye', 'e1'), Box(4, -47, -37, 8, 8, 1, 'eye', 'e2'), Box(22, -40, -37, 5, 5, 1, 'eye', 'e3'),
    Box(-6, -36, -37, 4, 4, 1, 'eye', 'e4'), Box(-28, -61, -12, 1, 6, 6, 'eye', 'e5'), Box(27, -59, -10, 1, 6, 6, 'eye', 'e6')])

# four arms brace the knot in the sea: upper arm, forearm, a claw planted in the water
ARMS = ((1, -1), (-1, -1), (1, 1), (-1, 1))             # side (+x is the model's left), front (-z) or back
for i, (side, fb) in enumerate(ARMS):
    C.part(f'arm{i}', 'knot', (side * 44, -40, fb * 22), rot=(0, 0, side * 1.7), boxes=[Box(-7, -40, -7, 14, 40, 14, 'limb', f'a{i}')])
    C.part(f'fore{i}', f'arm{i}', (0, -40, 0), rot=(0, 0, side * 1.05), boxes=[Box(-5, -46, -5, 10, 46, 10, 'limb', f'f{i}')])
    C.part(f'claw{i}', f'fore{i}', (0, -46, 0), boxes=[Box(-6, -8, -3, 12, 8, 6, 'hide', 'palm'), Box(-6, -22, -2, 2, 14, 4, 'talon', 'w0'), Box(-2, -26, -2, 2, 18, 4, 'talon', 'w1'),
                                                       Box(2, -26, -2, 2, 18, 4, 'talon', 'w2'), Box(5, -20, -2, 2, 12, 4, 'talon', 'w3')])

# ---------------------------------------------------------------------------------------------------- the rope
# three necks to a ring, turning round one another a little more at every ring and thinning as they go up
parent, at = 'knot', (0, -KNOT_TOP, 0)
for i in range(RINGS):
    w = round(15 - 1.1 * i)
    r = 8.5 - 0.55 * i
    boxes = []
    for k in range(3):
        th = TWIST * i + k * 2 * math.pi / 3
        boxes.append(Box(round(r * math.cos(th) - w / 2, 1), -RING, round(r * math.sin(th) - w / 2, 1), w, RING, w, f's{k}', f'r{i}k{k}'))
    C.part(f'ring{i}', parent, at, boxes=boxes)
    parent, at = f'ring{i}', (0, -RING, 0)
C.part('head', parent, at, boxes=[Box(-9, -8, -9, 18, 8, 18, 'hide', 'nape')])
# the disc: a sun that did not rise, upright on the end of the rope, looking out (-z); behind it the broken ring; round that the crown
C.part('disc', 'head', (0, -6, -6), boxes=[Box(-DISC / 2, -DISC - 6, -3, DISC, DISC, 6, 'disc', 'disc')])
C.part('halo', 'disc', (0, -DISC / 2 - 6, 4), boxes=[Box(-HALO / 2, -HALO / 2, 0, HALO, HALO, 2, 'halo', 'halo')])
for j in range(CROWN):
    phi = j * 2 * math.pi / CROWN
    # an arm of the crown is jointed at the ring (radius 54), and bends there: folded, they close behind the disc like a flower
    C.part(f'crown{j}', 'halo', (round(54 * math.sin(phi), 3), round(-54 * math.cos(phi), 3), 0), rot=(0, 0, phi), boxes=[Box(-3, -24, -2, 6, 24, 4, 'ray', f'c{j}')])
    C.part(f'crown{j}b', f'crown{j}', (0, -24, 0), boxes=[Box(-3, -26, -2, 6, 26, 4, 'ray', f'cb{j}')])
    C.part(f'crown{j}c', f'crown{j}b', (0, -26, 0), boxes=[Box(-4, -8, -1.5, 8, 8, 3, 'hide', 'palm'), Box(-4, -17, -1, 1, 9, 2, 'talon', 'w0'), Box(-1, -19, -1, 1, 11, 2, 'talon', 'w1'),
                                                           Box(2, -19, -1, 1, 11, 2, 'talon', 'w2'), Box(4, -15, -1, 1, 7, 2, 'talon', 'w3')])
C.pack()

STRAND = {'s0': (1.05, VIOLET_D, VIOLET), 's1': (1.4, BLUE, BLUE_L), 's2': (0.0, None, None)}


def make():
    p = Painter(C, 4401)
    for name, _, b in C.boxes():
        if b.mat == 'hide':
            base = night('cc', light=1.1)

            def hide(face, px, py, fw, fh, rnd):
                c = base(face, px, py, fw, fh, rnd)
                if face not in ('top', 'bottom') and (py % 3 == 2) and ((px + (py // 3) * 2) % 4 != 0):
                    return shade(c, 1.35)                  # scales: rows of arcs, hardly to be seen
                return c
            p.fill_box(b, hide)
            starry(p, b, 'c', 0.006)
            if b.tag in ('base', 'mid', 'crest', 'lump'):
                cracks(p, b, 'cck', 3 if b.tag in ('base', 'mid') else 1, colour=VIOLET_D, bright=VIOLET)
        elif b.mat in STRAND:
            light, seam, seam_l = STRAND[b.mat]
            if seam is None:
                # the third neck is not of the night: skin that has never been in any light, with the night coming up it
                pale = skin('sp', PALE_D, PALE_DD)
                dark = night('sd', light=1.2)

                def strand(face, px, py, fw, fh, rnd):
                    c = mix(pale(face, px, py, fw, fh, rnd), dark(face, px, py, fw, fh, rnd), 0.25 + 0.5 * blotch(f'sm{b.x}{b.y}', px, py, 4.0))
                    if face not in ('top', 'bottom') and py % 4 == 3:
                        return shade(c, 0.72)              # rings round it, like a windpipe
                    return c
                p.fill_box(b, strand)
            else:
                base = night(b.mat, light=light)
                p.fill_box(b, lambda face, px, py, fw, fh, rnd, base=base: base(face, px, py, fw, fh, rnd))
                starry(p, b, b.mat, 0.01)
                rnd = random.Random(f'seam:{name}:{b.mat}:{b.tag}')
                for face in ('front', 'back'):
                    fx, fy, fw, fh = b.faces()[face]
                    at = fw // 2
                    for y in range(fh):
                        if rnd.random() < 0.22:
                            at = max(1, min(fw - 2, at + rnd.choice((-1, 1))))
                        if rnd.random() < 0.2:
                            continue
                        p.px(fx + at, fy + y, seam_l if y % 4 == 0 else seam, glow=True)       # a seam of light, broken
        elif b.mat == 'limb':
            p.fill_box(b, night('cl', light=1.25))
            starry(p, b, 'l', 0.01)
            cracks(p, b, 'lk', 1, colour=VIOLET_D, bright=VIOLET)
        elif b.mat == 'ray':
            p.fill_box(b, night('cr', light=1.3))
            starry(p, b, 'r', 0.008)
            # a band of amber round it, where the ray was made: it is what the crown is seen by from far
            p.band(b, 3, AMBER_D, glow=True)
            p.band(b, 4, AMBER, glow=True)
            p.band(b, b.th - 5, AMBER_D, glow=True)
        elif b.mat == 'talon':
            p.fill_box(b, lambda face, px, py, fw, fh, rnd: shade(PALE_D if py > fh // 2 else PALE, 1.0 if face != 'back' else 0.8))
        elif b.mat == 'eye':
            def eye(face, px, py, fw, fh, rnd):
                if face not in ('front', 'left', 'right'):
                    return None
                cx, cy = px - (fw - 1) / 2, py - (fh - 1) / 2
                lid = 1.0 - (cy / (fh / 2 + 0.3)) ** 2
                if lid <= 0.0 or abs(cx) > (fw / 2) * lid ** 0.7:
                    return None
                if abs(cx) < 0.6 + 0.3 * (fw > 6):
                    return (4, 2, 8)                                    # the slit
                return AMBER_L if abs(cx) < fw * 0.2 else AMBER
            p.fill_box(b, eye)
            for face in ('front', 'left', 'right'):
                fx, fy, fw, fh = b.faces()[face]
                for x in range(fw):
                    for y in range(fh):
                        c = p.tex.getpixel((fx + x, fy + y))
                        if c[3] and c[0] > 100:
                            p.glow.putpixel((fx + x, fy + y), c[:3] + (255,))
        elif b.mat == 'disc':
            def disc(face, px, py, fw, fh, rnd):
                if face not in ('front', 'back'):
                    return None
                cx, cy = px - (fw - 1) / 2, py - (fh - 1) / 2
                r, ang, R = math.hypot(cx, cy), math.atan2(cy, cx), fw / 2
                if r > R - 0.8 + 1.5 * math.cos(ang * 11) * math.cos(ang * 4 + 1.0):
                    return None                                          # the rim is not even: rays
                if face == 'back':
                    return shade(NIGHT, 0.7)
                if r > R * 0.9:
                    return AMBER_L
                if r > R * 0.82:
                    return AMBER
                if r > R * 0.77:
                    return AMBER_D
                if r > R * 0.7:
                    return VIOLET_D if (px + py) % 3 else NIGHT_D        # the dark has a ring of the night in it
                return (2, 2, 6)
            p.fill_box(b, disc)
            fx, fy, fw, fh = b.faces()['front']
            for x in range(fw):
                for y in range(fh):
                    c = p.tex.getpixel((fx + x, fy + y))
                    if c[3] and c[0] > 100:
                        p.px(fx + x, fy + y, c, glow=True)
        elif b.mat == 'halo':
            def halo(face, px, py, fw, fh, rnd):
                if face not in ('front', 'back'):
                    return None
                cx, cy = px - (fw - 1) / 2, py - (fh - 1) / 2
                r, ang, R = math.hypot(cx, cy), math.atan2(cy, cx), fw / 2
                # a ring of light round the disc, broken in places: it does not close
                if not (R - 7 < r < R - 1) or math.cos(ang * 7 + 0.6) < -0.35:
                    return None
                return AMBER_L if r > R - 2.6 else (AMBER if r > R - 4.6 else VIOLET)
            p.fill_box(b, halo)
            for face in ('front', 'back'):
                fx, fy, fw, fh = b.faces()[face]
                for x in range(fw):
                    for y in range(fh):
                        c = p.tex.getpixel((fx + x, fy + y))
                        if c[3]:
                            p.glow.putpixel((fx + x, fy + y), c[:3] + (255,))
        elif b.mat == 'sea':
            def sea(face, px, py, fw, fh, rnd):
                if face != 'top':
                    return None
                cx, cy = px - (fw - 1) / 2, py - (fh - 1) / 2
                r = math.hypot(cx, cy) / (fw / 2)
                if r > 0.98 - 0.07 * blotch('sea_edge', px, py, 9.0):
                    return None
                c = mix((5, 9, 20), (12, 22, 42), blotch('swell', px, py, 7.0))
                foam = blotch('foam', px, py, 3.0)
                ring = abs(math.sin(r * 15.0 + 2.5 * blotch('foam_ring', px, py, 12.0)))
                if r < 0.5 and foam > 0.46:
                    return mix(c, (176, 188, 202), 0.55 + 0.4 * foam)
                if r >= 0.5 and ring > 0.8 and foam > 0.4:
                    return mix(c, (150, 164, 182), 0.35 + 0.4 * foam)
                return c
            p.fill_box(b, sea)
    return p.tex, p.glow


# ---------------------------------------------------------------------------------------------------- the animation
# ColossusModel.setupAnim does this by hand, by the entity's age in ticks (negative: not yet risen; RISE is the last tick of the rising):
# the plate of sea comes up, the knot out of the water, the arms thrown up come down on the sea, the rope that was coiled
# on itself straightens ring by ring from the foot, the head bowed lifts, the crown that was folded behind the disc opens, and
# three times it roars: its head thrown back. Whatever is changed here is changed there: this is the same sum on the preview.
RISE = 560
ROARS = ((285, 295, 325, 340), (395, 408, 470, 490), (495, 505, 530, 545))


def ramp(t, a, b):
    x = min(1.0, max(0.0, (t - a) / (b - a)))
    return x * x * (3 - 2 * x)


def roar_at(age):
    return sum(ramp(age, a, b) * (1 - ramp(age, c, d)) for a, b, c, d in ROARS)


ARM_SIDE = [s for s, _ in ARMS]


def pose_at(age):
    pose = {}
    roar = roar_at(age)
    sink = 76.0 * (1.0 - ramp(age, 30.0, 210.0))
    pose['knot'] = {'off': (math.sin(age * 2.3) * 0.5 * roar, sink + math.sin(age * 0.09) * 0.8 * (1 - ramp(age, 210.0, 300.0)), 0)}
    slam = ramp(age, 190.0, 226.0)
    for i, side in enumerate(ARM_SIDE):
        pose[f'arm{i}'] = {'rot': (0, 0, -side * 1.4 * (1 - slam))}
        pose[f'fore{i}'] = {'rot': (0, 0, -side * 0.95 * (1 - slam))}
    stand = ramp(age, 330.0, 520.0)
    for i in range(RINGS):
        bend = 0.30 * (1.0 - ramp(age, 150.0 + 16.0 * i, 330.0 + 16.0 * i))
        pose[f'ring{i}'] = {'rot': (bend + math.cos(age * 0.04 - i * 0.4) * 0.02 * stand, 0, math.sin(age * 0.05 - i * 0.5) * 0.025 * stand)}
    pose['head'] = {'rot': (1.25 * (1.0 - ramp(age, 300.0, 470.0)) - 0.5 * roar, 0, 0)}
    open_ = ramp(age, 400.0, 560.0)
    pose['halo'] = {'rot': (0, 0, age * 0.0025 * open_)}
    for j in range(CROWN):
        bloom = ramp(age, 400.0 + 5 * j, 530.0 + 5 * j)
        pose[f'crown{j}'] = {'rot': (-1.45 * (1 - bloom) + 0.3 * roar * bloom, 0, 0)}
        pose[f'crown{j}b'] = {'rot': (-1.2 * (1 - bloom) + math.sin(age * 0.07 + j * 0.9) * 0.06 * bloom, 0, 0)}
        pose[f'crown{j}c'] = {'rot': (-0.9 * (1 - bloom) + math.sin(age * 0.09 + j * 1.3) * 0.1 * bloom, 0, 0)}
    return pose


# ---------------------------------------------------------------------------------------------------- previews
if __name__ == '__main__':
    tex, glow = make()
    tex.save(f'{A}/textures/entity/colossus.png')
    glow.save(f'{A}/textures/entity/colossus_glow.png')
    if not os.environ.get('NOJAVA'):
        C.write_java(JAVA)
    if len(sys.argv) > 1:
        cam = dict(size=(420, 560), zoom=0.1, centre=(0, 24 - 180 * SCALE), glow=glow, background=(18, 20, 34))
        ages = [int(a) for a in sys.argv[2].split(',')] if len(sys.argv) > 2 and sys.argv[2] != '-' else [40, 110, 170, 215, 300, 380, 440, 600]
        shots = [C.render(tex, yaw=-0.5, pitch=0.05, pose=pose_at(a), **cam) for a in ages]
        sheet(shots, 4, labels=[f'age {a}' for a in ages]).save(sys.argv[1])
