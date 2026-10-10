"""What the creatures of the Sky Beneath are painted with (gen_dusk.py, gen_clock.py, gen_coming.py): the colours of the
place, and the surfaces they share - a body cut out of the night sky, with stars in it; the pale hands; cracks that
shine; amber and black iron. The place's accent is violet and a dark blue, and there is little of either.
"""
import math
import random

from modelkit import blotch, mix, shade

NIGHT = (15, 15, 27)             # what the grown ones are made of: not black, the blue hour with the light gone out of it
NIGHT_L = (30, 30, 52)
NIGHT_D = (7, 7, 14)
STAR = (226, 230, 255)
STAR_D = (150, 160, 215)
VIOLET = (126, 86, 196)
VIOLET_D = (62, 42, 112)
VIOLET_L = (176, 142, 236)
BLUE = (64, 96, 214)
BLUE_L = (120, 160, 255)
PALE = (176, 166, 182)           # the hands: skin that has never been in any light
PALE_D = (126, 116, 138)
PALE_DD = (84, 74, 98)
NAIL = (44, 38, 56)
RAW = (128, 52, 112)             # the inside of a mouth
RAW_D = (74, 26, 70)
TOOTH = (214, 208, 196)
TOOTH_D = (150, 144, 138)
AMBER = (232, 152, 40)           # the Founder's seal: what was set here on purpose wears it
AMBER_L = (255, 206, 98)
AMBER_D = (150, 86, 22)
IRON = (62, 64, 74)
IRON_D = (36, 38, 46)
IRON_L = (104, 108, 122)
BRASS = (150, 122, 58)
BRASS_D = (98, 76, 36)
BRASS_L = (198, 170, 92)
GOLD = (212, 170, 70)            # the mask
GOLD_D = (140, 104, 38)
GOLD_L = (250, 222, 130)


def night(seed, stars=0.035, light=1.0):
    """A body of night: near black, lighter along its upper edges, and here and there a star (the stars are returned by
    `starry` below, which paints them into the glow as well)."""
    def fn(face, px, py, fw, fh, rnd):
        f = {'top': 1.25, 'bottom': 0.6}.get(face, 1.05 - 0.3 * (py / max(1, fh - 1)))
        c = NIGHT_L if blotch(f'{seed}{face}', px, py, 3.0) > 0.62 else NIGHT
        if min(fw, fh) > 2 and (px in (0, fw - 1) or py in (0, fh - 1)):
            f *= 0.8
        return shade(c, f * light * rnd.choice((0.9, 1.0, 1.0, 1.1)))
    return fn


def starry(p, box, seed, density=0.035, faces=('top', 'bottom', 'right', 'front', 'left', 'back')):
    """Stars on a box already painted: a few texels that shine, most of them faint, never two together."""
    rnd = random.Random(f'stars:{seed}:{box.part}:{box.x}:{box.y}:{box.z}')
    for face in faces:
        fx, fy, fw, fh = box.faces()[face]
        for y in range(fh):
            for x in range(fw):
                if p.tex.getpixel((fx + x, fy + y))[3] == 0:
                    continue
                if rnd.random() < density:
                    n = rnd.random()
                    c = STAR if n < 0.3 else (STAR_D if n < 0.75 else (VIOLET_L if n < 0.9 else BLUE_L))
                    p.px(fx + x, fy + y, c, glow=True)


def cracks(p, box, seed, count=2, faces=('front', 'back', 'left', 'right'), colour=BLUE, bright=BLUE_L):
    """Cracks in a hide, with light behind them: they wander down a face, fork once, and shine."""
    rnd = random.Random(f'cracks:{seed}:{box.part}:{box.x}:{box.y}')
    for face in faces:
        fx, fy, fw, fh = box.faces()[face]
        if fw < 3 or fh < 4:
            continue
        for _ in range(count):
            x, y = rnd.randrange(fw), rnd.randrange(max(1, fh // 3))
            steps = rnd.randrange(fh // 2, fh)
            for i in range(steps):
                if not (0 <= x < fw and 0 <= y < fh):
                    break
                if p.tex.getpixel((fx + x, fy + y))[3]:
                    p.px(fx + x, fy + y, bright if i % 3 == 0 else colour, glow=True)
                y += 1 if rnd.random() < 0.75 else 0
                x += rnd.choice((-1, 0, 0, 1))
                if i == steps // 2 and rnd.random() < 0.6:           # a fork
                    bx, by = x, y
                    for _j in range(steps // 3):
                        bx += rnd.choice((-1, 1))
                        by += rnd.choice((0, 1))
                        if 0 <= bx < fw and 0 <= by < fh and p.tex.getpixel((fx + bx, fy + by))[3]:
                            p.px(fx + bx, fy + by, colour, glow=True)


def skin(seed, base=PALE, dark=PALE_D):
    """The hands, and what else is skin: pale, mottled, darker towards the ends."""
    def fn(face, px, py, fw, fh, rnd):
        c = dark if blotch(f'{seed}{face}', px, py, 2.2) > 0.6 else base
        f = {'top': 1.1, 'bottom': 0.72}.get(face, 1.0 - 0.14 * (py / max(1, fh - 1)))
        return shade(c, f * rnd.choice((0.94, 1.0, 1.0, 1.05)))
    return fn


def hand_flat(mirrored=False, tip='z0'):
    """A flat box painted as an open hand seen from above: a palm and four fingers and a thumb, cut out of it. The
    fingers point to -z (`tip='z0'`) or +z; a mirrored hand has its thumb on the other side. For boxes about 5x1x6."""
    def fn(face, px, py, fw, fh, rnd):
        if face not in ('top', 'bottom'):
            return None
        # on the top face row 0 is the back (z1) and the last row the front (z0); the bottom face is the other way up
        row = (fh - 1 - py) if face == 'top' else py                  # 0 at z0
        if tip != 'z0':
            row = fh - 1 - row
        col = (fw - 1 - px) if mirrored else px
        palm = fh // 2
        if row >= fh - palm:                                          # the palm: all of the width but the thumb's cell
            if col == 0 and row < fh - 1:
                return shade(PALE_D, 0.95) if row == fh - palm else None  # the thumb, lying out sideways
            return shade(PALE if (col + row) % 3 else PALE_D, 1.08 if face == 'top' else 0.8)
        if col == 0:
            return None
        if (col % 2 == 0 and fw > 4) or (fw <= 4 and col == 2):
            return None if fw > 4 else shade(PALE, 1.0)               # the gaps between fingers
        if row == 0:
            return shade(NAIL, 1.0) if (col * 7 + fw) % 4 != 0 else None      # nails; one finger shorter than the rest
        return shade(PALE if row % 2 else PALE_D, 1.05 if face == 'top' else 0.8)
    return fn


def hand_up(mirrored=False):
    """The same hand on an upright plate (fingers up, the palm towards -z): for hands held before a face. Boxes about 5x7x1."""
    def fn(face, px, py, fw, fh, rnd):
        if face not in ('front', 'back'):
            return None
        col = px if face == 'front' else fw - 1 - px
        if mirrored:
            col = fw - 1 - col
        palm = fh // 2
        if py >= fh - palm:
            if col == 0 and py < fh - 1:
                return shade(PALE_D, 0.95) if py == fh - palm else None
            c = PALE if (col + py) % 3 else PALE_D
            return shade(c, 1.0 if face == 'front' else 0.82)
        if col == 0 or col % 2 == 0:
            return None
        if py == 0:
            return shade(NAIL, 1.0) if face == 'back' and (col * 5) % 3 else (shade(PALE_D, 0.9) if (col * 7 + fw) % 4 else None)
        return shade(PALE if py % 2 else PALE_D, 1.0 if face == 'front' else 0.82)
    return fn


def iron(seed, base=IRON, dark=IRON_D, light=IRON_L):
    def fn(face, px, py, fw, fh, rnd):
        c = dark if blotch(f'{seed}{face}', px, py, 2.6) > 0.64 else base
        f = {'top': 1.2, 'bottom': 0.65}.get(face, 1.0 - 0.16 * (py / max(1, fh - 1)))
        if min(fw, fh) > 2 and (px == 0 or py == 0):
            return shade(light, f * 0.9)
        if min(fw, fh) > 2 and (px == fw - 1 or py == fh - 1):
            f *= 0.75
        return shade(c, f * rnd.choice((0.93, 1.0, 1.0, 1.06)))
    return fn


def disc(inner, rim, rim_wide=1.5, back=None, glow_inner=False):
    """A round plate on the front and back faces of a box, the rest of the box not drawn: `inner(r, ang)` and
    `rim(r, ang)` give the colours, r from 0 at the middle to 1 at the edge."""
    def fn(face, px, py, fw, fh, rnd):
        if face not in ('front', 'back'):
            return None
        cx, cy = px - (fw - 1) / 2, py - (fh - 1) / 2
        r = math.hypot(cx, cy)
        R = min(fw, fh) / 2
        if r > R - 0.2:
            return None
        ang = math.atan2(cy, cx)
        if face == 'back' and back is not None:
            return back(r / R, ang)
        return rim(r / R, ang) if r > R - rim_wide - 0.2 else inner(r / R, ang)
    return fn
