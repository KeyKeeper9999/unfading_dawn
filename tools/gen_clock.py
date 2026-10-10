#!/usr/bin/env python3
"""What was set in the Sky Beneath on purpose, when the day was stopped: the clockhand (ClockhandModel.java), which
tends a clock that does not go, the Bolt (BoltModel.java), the lock on tomorrow - the bob of the great pendulum,
and what it is when it has torn itself off its rod - and the works of that clock, which are the hall the Bolt hangs in
and as much its body as the lens is (GearModel.java: a wheel, a weight on its chain). All wear amber, the colour of
the Founder's seals. A mesh, a texture and a glow each.
Run from the repo root; pass a file name to also get a preview sheet (and a second argument to leave the Java alone)."""
import math
import sys

from duskkit import AMBER, AMBER_D, AMBER_L, BRASS, BRASS_D, BRASS_L, IRON, IRON_D, IRON_L, NIGHT, NIGHT_D, iron
from modelkit import Box, Model, Painter, blotch, mix, shade, sheet

A = 'src/main/resources/assets/unfading_dawn'
J = 'src/main/java/com/keykeeper/dushi/client'
BACK = (26, 28, 44)
ENAMEL = (22, 22, 38)
IVORY = (206, 198, 176)
IVORY_D = (160, 150, 128)

# ---------------------------------------------------------------------------------------------------- the clockhand
# A man's height and a half of clock: a dial for a chest, a lesser, blank dial for a face with a winding key in the
# top of it, legs that are rods on springs, and for arms the two hands of a clock - the long one a blade as long as
# itself, the short one a spade. It moves as a clock's hand does: not at all, and then all at once.
CLOCKHAND_SCALE = 1.15          # must match MODEL_SCALE in ClockhandModel.java


def clockhand():
    M = Model(64, 64, CLOCKHAND_SCALE)
    M.part('all', None, (0, 24, 0))
    M.part('right_leg', 'all', (-2, -14, 0), boxes=[Box(-0.5, 0, -0.5, 1, 7, 1, 'rod', 'thigh'), Box(-1, 1, -1, 2, 5, 2, 'spring', 'spring')])
    M.part('right_shin', 'right_leg', (0, 7, 0), boxes=[Box(-0.5, 0, -0.5, 1, 7, 1, 'rod', 'shin'), Box(-1, 6, -3, 2, 1, 4, 'iron', 'foot')])
    M.mirror('right_leg', 'left_leg', parent='all')
    M.mirror('right_shin', 'left_shin', parent='left_leg')
    M.part('body', 'all', (0, -14, 0), boxes=[Box(-2, -3, -1.5, 4, 3, 3, 'iron', 'hub')])
    M.part('chest', 'body', (0, -3, 0), boxes=[Box(-5.5, -11, -1, 11, 11, 2, 'dial', 'dial'), Box(-3, -8.5, 1, 6, 6, 2, 'works', 'works')])
    M.part('head', 'chest', (0, -11, 0), boxes=[Box(-0.5, -2, -0.5, 1, 2, 1, 'rod', 'neck'), Box(-3, -8, -1, 6, 6, 2, 'face', 'face')])
    M.part('key', 'head', (0, -8, 0), boxes=[Box(-0.5, -2, -0.5, 1, 2, 1, 'brass', 'stem'), Box(-3, -5, -0.5, 6, 3, 1, 'key', 'key')])
    M.part('right_arm', 'chest', (-6, -8.5, 0), (-0.25, 0, 0.3), boxes=[Box(-1, -1, -1, 2, 2, 2, 'brass', 'boss'), Box(-0.5, 1, -0.5, 1, 6, 1, 'rod', 'arm')])
    M.part('right_blade', 'right_arm', (0, 7, 0), (-0.2, 0, 0), boxes=[Box(-1.5, 0, -0.5, 3, 22, 1, 'minute', 'blade')])
    M.part('left_arm', 'chest', (6, -8.5, 0), (-0.2, 0, -0.3), boxes=[Box(-1, -1, -1, 2, 2, 2, 'brass', 'boss'), Box(-0.5, 1, -0.5, 1, 4, 1, 'rod', 'arm')])
    M.part('left_blade', 'left_arm', (0, 5, 0), (-0.15, 0, 0), boxes=[Box(-2.5, 0, -0.5, 5, 13, 1, 'hour', 'blade')])
    M.pack()
    return M


def brass(seed):
    return iron(seed, BRASS, BRASS_D, BRASS_L)


def dial_face(p, b, marks=True, big=True):
    """A clock's face on the front of a flat box: a brass rim, dark enamel, the hours in amber, and no hands - they
    are elsewhere."""
    def face(f, px, py, fw, fh, rnd):
        if f not in ('front', 'back'):
            return None
        cx, cy = px - (fw - 1) / 2, py - (fh - 1) / 2
        r = math.hypot(cx, cy)
        R = fw / 2
        if r > R - 0.1:
            return None
        if f == 'back':
            return shade(IRON_D if (px + py) % 3 else IRON, 0.9)
        if r > R - 1.4:
            return shade(BRASS if (px + py) % 2 else BRASS_L, 0.95)
        return shade(ENAMEL, 0.9 + 0.2 * blotch('dial', px, py, 3.0))
    p.fill_box(b, face)
    fx, fy, fw, fh = b.faces()['front']
    R = fw / 2
    if marks:
        for hour in range(12):
            a = hour * math.pi / 6
            x, y = (fw - 1) / 2 + math.sin(a) * (R - 2.6), (fh - 1) / 2 - math.cos(a) * (R - 2.6)
            if hour % 3 == 0 or big:
                p.px(fx + int(round(x)), fy + int(round(y)), AMBER_L if hour % 3 == 0 else AMBER, glow=True)
    mid = (fw - 1) // 2
    for x, y in ((mid, (fh - 1) // 2), (mid + (fw + 1) % 2, (fh - 1) // 2)):
        p.px(fx + x, fy + y, AMBER_L, glow=True)


def paint_clockhand(M):
    p = Painter(M, 877)
    for name, _, b in M.boxes():
        if b.mat == 'rod':
            p.fill_box(b, iron('cr'))
        elif b.mat == 'iron':
            p.fill_box(b, iron('ci'))
        elif b.mat == 'brass':
            p.fill_box(b, brass('cb'))
        elif b.mat == 'spring':
            def spring(face, px, py, fw, fh, rnd):
                if face in ('top', 'bottom') or py % 2 == 0:
                    return None
                return shade(BRASS if face in ('front', 'right') else BRASS_D, 1.0)
            p.fill_box(b, spring)
        elif b.mat == 'dial':
            dial_face(p, b)
        elif b.mat == 'face':
            dial_face(p, b, marks=False)
            # a keyhole where a face would be, and nothing else
            fx, fy, fw, fh = b.faces()['front']
            for x, y in ((2, 2), (3, 2), (2, 3), (3, 3)):
                p.px(fx + x, fy + y, AMBER_L if y == 2 else AMBER, glow=True)
        elif b.mat == 'works':
            def works(face, px, py, fw, fh, rnd):
                if face == 'front':
                    return None
                cx, cy = px - (fw - 1) / 2, py - (fh - 1) / 2
                r, ang = math.hypot(cx, cy), math.atan2(cy, cx)
                if face == 'back':
                    if r > 3.0 + 0.5 * (math.cos(ang * 8) > 0):
                        return None
                    return shade(BRASS_D if r < 1.2 else BRASS, 0.9 if (px + py) % 2 else 1.05)
                return shade(BRASS_D, 0.8)
            p.fill_box(b, works)
        elif b.mat == 'key':
            def key(face, px, py, fw, fh, rnd):
                if face not in ('front', 'back'):
                    return None
                # the two wings of a winding key
                if px in (2, 3):
                    return shade(BRASS_D, 1.0) if py == 1 else None
                if (px in (0, 5) and py in (0, 2)) or (px in (1, 4) and py == 1):
                    return None
                return shade(BRASS_L if py == 0 else BRASS, 1.0)
            p.fill_box(b, key)
        elif b.mat in ('minute', 'hour'):
            def blade(face, px, py, fw, fh, rnd, hour=b.mat == 'hour'):
                if face not in ('front', 'back'):
                    return None
                mid = (fw - 1) / 2
                if hour:
                    # a stem, a spade, a point
                    if py < 4:
                        half = 0.6
                    elif py < fh - 3:
                        half = 0.6 + 1.9 * math.sin((py - 4) / (fh - 7) * math.pi) ** 0.6
                    else:
                        half = 0.6 * (fh - py) / 3 + 0.3
                else:
                    # a ring by the boss, then a blade that narrows all its length
                    if 2 <= py <= 4:
                        half = 1.5 if py != 3 or abs(px - mid) > 0.3 else 1.5
                        if py == 3 and abs(px - mid) < 0.6:
                            return None
                    else:
                        half = 1.3 * (1 - py / fh) + 0.35
                if abs(px - mid) > half:
                    return None
                return shade(IRON_L if px - mid < -half + 1.0 and py > 4 else IRON, 1.0 if face == 'front' else 0.8)
            p.fill_box(b, blade)
    return p.tex, p.glow


# standing; a step (it is on one foot for the whole of a second); the long hand back, and through; the short hand round
CLOCKHAND_POSES = (
    ('stand', {}),
    ('step', {'right_leg': (-0.6, 0, 0), 'right_shin': (0.6, 0, 0), 'left_leg': (0.3, 0, 0)}),
    ('stab_back', {'chest': (0, 0.5, 0), 'right_arm': (0.6, 0, 0.2), 'right_blade': (-1.4, 0, 0)}),
    ('stab', {'chest': (0.15, -0.4, 0), 'body': (0.2, 0, 0), 'right_arm': (-1.25, 0, -0.2), 'right_blade': (0.15, 0, 0)}),
    ('sweep_back', {'chest': (0, -0.7, 0), 'left_arm': (-1.3, 0, -0.9), 'left_blade': (0, 0, 0)}),
    ('sweep', {'chest': (0, 0.8, 0), 'left_arm': (-1.3, 0, 0.6), 'left_blade': (0, 0, 0)}),
    ('wind', {'key': (0, 1.2, 0), 'chest': (0.25, 0, 0), 'right_arm': (0.3, 0, 0.5), 'left_arm': (0.3, 0, -0.5)}),
)


# ---------------------------------------------------------------------------------------------------- the Bolt
# The bob of the pendulum of the clock that was stopped: a lens of amber glass as wide as two men are tall, in a ring of
# black iron, a keyhole through the middle of it, hung on a rod that goes up out of sight. Four of the clock's hands
# lie folded up along the rod. When the rod parts they come down, and are its legs. 'bob' is the whole of it (it leans
# as the pendulum swings: the rod goes with it); 'rod' is drawn while it hangs and 'stub' when it does not.
BOLT_SCALE = 2.2                # must match MODEL_SCALE in BoltModel.java
BOLT_LEGS = (('leg_fr', -1, -1), ('leg_fl', 1, -1), ('leg_br', -1, 1), ('leg_bl', 1, 1))


def bolt():
    M = Model(128, 128, BOLT_SCALE)
    M.part('all', None, (0, 24, 0))
    ring = [Box(-5, -9, -3, 10, 2, 6, 'ring', 'top'), Box(-5, 7, -3, 10, 2, 6, 'ring', 'bottom'), Box(-9, -5, -3, 2, 10, 6, 'ring', 'side_r'),
            Box(7, -5, -3, 2, 10, 6, 'ring', 'side_l'), Box(-8, -8, -3, 3, 3, 6, 'ring', 'corner'), Box(5, -8, -3, 3, 3, 6, 'ring', 'corner'),
            Box(-8, 5, -3, 3, 3, 6, 'ring', 'corner'), Box(5, 5, -3, 3, 3, 6, 'ring', 'corner')]
    M.part('bob', 'all', (0, -10, 0), boxes=ring + [Box(-7, -7, -1.5, 14, 14, 3, 'lens', 'lens')])
    M.part('rod', 'bob', (0, -9, 0), boxes=[Box(-3, -3, -1.5, 6, 3, 3, 'ring', 'yoke')] + [Box(-1, -3 - 16 * (i + 1), -1, 2, 16, 2, 'rod', f'rod{i}') for i in range(5)])
    M.part('stub', 'bob', (0, -9, 0), boxes=[Box(-3, -3, -1.5, 6, 3, 3, 'ring', 'yoke'), Box(-1, -9, -1, 2, 6, 2, 'rod', 'stub'), Box(-1.5, -11, -0.5, 3, 2, 1, 'torn', 'torn')])
    for name, side, end in BOLT_LEGS:
        M.part(name, 'bob', (side * 8.5, 1, end * 2), (0, 0, side * 0.1), boxes=[Box(-1, -13, -1, 2, 13, 2, 'limb', 'upper'), Box(-1.5, -1.5, -1.5, 3, 3, 3, 'brass', 'boss')])
        M.part(name.replace('leg', 'shin'), name, (0, -13, 0), boxes=[Box(-1.5, -16, -0.5, 3, 16, 1, 'pointer', 'pointer'), Box(-1, -1, -1, 2, 2, 2, 'brass', 'knee')])
    M.pack()
    return M


def paint_bolt(M):
    p = Painter(M, 888)
    for name, _, b in M.boxes():
        if b.mat == 'ring':
            p.fill_box(b, iron('br'))
            # rivets of brass along it
            for face in ('front', 'back'):
                fx, fy, fw, fh = b.faces()[face]
                for x in range(1, fw - 1, 3):
                    p.px(fx + x, fy + fh // 2, BRASS_L if face == 'front' else BRASS)
        elif b.mat == 'lens':
            def lens(face, px, py, fw, fh, rnd):
                if face not in ('front', 'back'):
                    return None
                cx, cy = px - (fw - 1) / 2, py - (fh - 1) / 2
                r = math.hypot(cx, cy)
                if r > 7.0:
                    return None
                # the keyhole: a round and a slot under it
                if math.hypot(cx, cy + 1.5) < 2.3 or (abs(cx) < 0.6 + 0.35 * max(0.0, cy) and 0 <= cy <= 4.2):
                    return NIGHT_D
                t = r / 7.0
                c = mix(AMBER_L, AMBER, min(1.0, t * 1.5)) if t < 0.66 else mix(AMBER, AMBER_D, (t - 0.66) / 0.34)
                return shade(c, 0.92 + 0.16 * blotch('lens', px, py, 2.4))
            p.fill_box(b, lens)
            for face in ('front', 'back'):
                fx, fy, fw, fh = b.faces()[face]
                for x in range(fw):
                    for y in range(fh):
                        c = p.tex.getpixel((fx + x, fy + y))
                        if c[3] and c[0] > 100:
                            p.glow.putpixel((fx + x, fy + y), c[:3] + (255,))
        elif b.mat == 'rod':
            base = iron('bo')

            def rod(face, px, py, fw, fh, rnd):
                if face not in ('top', 'bottom') and py % 8 in (3, 4):
                    return shade(BRASS if py % 8 == 3 else BRASS_D, 1.0)
                return base(face, px, py, fw, fh, rnd)
            p.fill_box(b, rod)
        elif b.mat == 'torn':
            def torn(face, px, py, fw, fh, rnd):
                if face not in ('front', 'back') or (px + py) % 2 == 0:
                    return None
                return IRON_L
            p.fill_box(b, torn)
        elif b.mat == 'limb':
            p.fill_box(b, iron('bl', IRON_D, (20, 22, 28), IRON))
        elif b.mat == 'brass':
            p.fill_box(b, brass('bb'))
        elif b.mat == 'pointer':
            def pointer(face, px, py, fw, fh, rnd):
                if face not in ('front', 'back'):
                    return None
                # the hand of a clock: it widens a little way from its boss, and comes to a point
                y = fh - 1 - py                                       # 0 at the knee
                mid = (fw - 1) / 2
                half = 0.6 if y < 3 else (1.5 if y < 7 else 1.5 * (fh - y) / (fh - 7) + 0.2)
                if abs(px - mid) > half:
                    return None
                return shade(IRON_L if px - mid < -half + 1.0 and y > 2 else IRON, 1.0 if face == 'front' else 0.8)
            p.fill_box(b, pointer)
    return p.tex, p.glow


def bolt_down(extra=None):
    """Off its rod and on its four hands: the pose of its second phase (BoltModel has the same numbers)."""
    pose = {'bob': {'off': (0, -11, 0)}}
    for name, side, end in BOLT_LEGS:
        pose[name] = (0, -end * side * 0.6, side * 1.9 - side * 0.1)
        pose[name.replace('leg', 'shin')] = (0, 0, side * 1.04)
    for k, v in (extra or {}).items():
        if isinstance(v, dict):
            pose[k] = v
        elif k in pose and isinstance(pose[k], dict):
            pose[k] = dict(pose[k], rot=v)
        elif k in pose and isinstance(v, tuple):
            pose[k] = tuple(a + b_ for a, b_ in zip(pose[k], v))
        else:
            pose[k] = v
    return pose


BOLT_POSES = (
    ('hung', {}),
    ('swung', {'bob': (0, 0, 0.28)}),
    ('toll', {'bob': (0.2, 0, 0), 'leg_fr': (0, 0, -0.5), 'leg_fl': (0, 0, 0.5), 'leg_br': (0, 0, -0.5), 'leg_bl': (0, 0, 0.5)}),
    ('down', bolt_down()),
    ('stab_back', bolt_down({'bob': (-0.25, 0, 0), 'leg_fr': (0, 0, 0.9), 'shin_fr': (0, 0, -1.6)})),
    ('stab', bolt_down({'bob': (0.3, 0, 0), 'leg_fr': (1.45, 0.6, 0.2), 'shin_fr': (0, 0, 1.04)})),
    ('crash', bolt_down({'bob': {'off': (0, -3, 0), 'rot': (0.1, 0, 0)}, 'leg_fr': (0, 0, 0.5), 'leg_fl': (0, 0, -0.5), 'leg_br': (0, 0, 0.5), 'leg_bl': (0, 0, -0.5)})),
)

# ---------------------------------------------------------------------------------------------------- the works
# The pieces of the stopped clock that still move (GearEntity, GearModel.java): a wheel standing on its edge - teeth,
# a rim, four spokes, a boss of brass with the Founder's amber in it - and one of the clock's weights on its chain.
# One mesh holds both; the model shows the one it is asked for. The wheel is two faces cut out of flat plates, with
# a block of iron between them for its rim; 'wheel' turns about its axle (x), and is GEAR_R model units to the tips
# of its teeth (GearEntity.RADIUS blocks).
GEAR_SCALE = 2.0                # must match MODEL_SCALE in GearModel.java
GEAR_R = 13
GEAR_TEETH = 12
GEAR_CHAIN = 5


def gear():
    M = Model(128, 128, GEAR_SCALE)
    R = GEAR_R
    M.part('cog', None, (0, 24, 0))
    M.part('wheel', 'cog', (0, -R, 0), boxes=[Box(-2, -R, -R, 1, 2 * R, 2 * R, 'cogface', 'face_r'), Box(1, -R, -R, 1, 2 * R, 2 * R, 'cogface', 'face_l'),
                                              Box(-1.5, -9.5, -4, 3, 19, 8, 'rim', 'rim'), Box(-1.5, -4, -9.5, 3, 8, 19, 'rim', 'rim'),
                                              Box(-1.5, -7, -7, 3, 14, 14, 'rim', 'rim'), Box(-3, -2.5, -2.5, 6, 5, 5, 'brass', 'boss')])
    chain = [Box(-1, -19 - 16 * (i + 1), -1, 2, 16, 2, 'chain', f'chain{i}') for i in range(GEAR_CHAIN)]
    M.part('weight', None, (0, 24, 0), boxes=[Box(-4, -14, -4, 8, 14, 8, 'weight', 'weight'), Box(-2.5, -17, -2.5, 5, 3, 5, 'brass', 'cap'),
                                              Box(-1.5, -19, -0.5, 3, 2, 1, 'brass', 'eye')] + chain)
    M.pack()
    return M


def cog_face(px, py, fw, fh):
    """What a texel of a wheel's face is: None (cut away), 'tooth', 'rim', 'spoke', 'boss' or 'stud'."""
    cx, cy = px - (fw - 1) / 2, py - (fh - 1) / 2
    r = math.hypot(cx, cy)
    ang = math.atan2(cy, cx)
    if r < 3.4:
        return 'stud' if r < 1.2 else 'boss'
    if r <= 10.6:
        if r > 7.4:
            return 'rim'
        # four spokes, each widening to the rim
        turn = (ang + math.pi / 4) % (math.pi / 2) - math.pi / 4
        if abs(math.sin(turn)) * r < 1.2 + 0.08 * r:
            return 'spoke'
        return None
    if r <= GEAR_R + 0.3:
        tooth = (ang / (2 * math.pi) * GEAR_TEETH) % 1.0
        if 0.2 < tooth < 0.8 and r <= GEAR_R - 0.2:
            return 'tooth'
        if r <= 11.3:
            return 'rim'
    return None


def paint_gear(M):
    p = Painter(M, 877)
    metal = iron('gw', IRON_D, (20, 22, 28), IRON)
    for name, _, b in M.boxes():
        if b.mat == 'cogface':
            def face(f, px, py, fw, fh, rnd):
                if f not in ('left', 'right'):
                    return None
                kind = cog_face(px, py, fw, fh)
                if kind is None:
                    return None
                r = math.hypot(px - (fw - 1) / 2, py - (fh - 1) / 2)
                if kind == 'tooth':
                    return shade(IRON if r < GEAR_R - 1.4 else IRON_L, rnd.choice((0.92, 1.0, 1.06)))
                if kind == 'rim':
                    if abs(r - 9.0) < 0.55:
                        return shade(BRASS if int(math.atan2(py - (fh - 1) / 2, px - (fw - 1) / 2) * 6) % 2 else BRASS_D, 1.0)
                    return shade(IRON_D if blotch('cogrim', px, py, 2.6) > 0.6 else IRON, rnd.choice((0.92, 1.0, 1.06)))
                if kind == 'spoke':
                    return shade(IRON_D, rnd.choice((0.95, 1.0, 1.1)))
                if kind == 'stud':
                    return AMBER_L
                return shade(BRASS_L if r < 2.4 else BRASS, 1.0)
            p.fill_box(b, face)
            for f in ('left', 'right'):
                fx, fy, fw, fh = b.faces()[f]
                for x in range(fw):
                    for y in range(fh):
                        if cog_face(x, y, fw, fh) == 'stud':
                            p.px(fx + x, fy + y, AMBER_L if (x + y) % 2 else AMBER, glow=True)
        elif b.mat == 'rim':
            p.fill_box(b, metal)
        elif b.mat == 'brass':
            p.fill_box(b, brass('gb'))
            if b.tag == 'boss':
                for f in ('left', 'right'):
                    fx, fy, fw, fh = b.faces()[f]
                    for x in range(1, fw - 1):
                        for y in range(1, fh - 1):
                            p.px(fx + x, fy + y, AMBER_L if (x, y) == (fw // 2, fh // 2) else AMBER, glow=True)
        elif b.mat == 'weight':
            base = iron('gt', IRON, IRON_D, IRON_L)

            def weight(f, px, py, fw, fh, rnd):
                if f not in ('top', 'bottom') and py in (2, 3, fh - 4, fh - 3):
                    return shade(BRASS if py in (2, fh - 4) else BRASS_D, 1.0)
                return base(f, px, py, fw, fh, rnd)
            p.fill_box(b, weight)
            # the seal on it: the keyhole of the Bolt's own lens, in amber, on every side
            for f in ('front', 'back', 'left', 'right'):
                for x, y, c in ((3, 6, AMBER_L), (4, 6, AMBER_L), (3, 7, AMBER), (4, 7, AMBER), (3, 8, AMBER_D), (4, 8, AMBER_D)):
                    p.put(b, f, x, y, c, glow=True)
        elif b.mat == 'chain':
            def chain(f, px, py, fw, fh, rnd):
                if f in ('top', 'bottom'):
                    return shade(IRON_D, 0.9)
                # links: one seen flat, the next edge on
                link = (py // 2) % 2
                if link == 0:
                    return shade(IRON if px == 0 else IRON_L, 0.95) if py % 2 == 0 or True else None
                return shade(IRON_D, 0.85) if px == (fw - 1) // 2 or fw < 2 else None
            p.fill_box(b, chain)
    return p.tex, p.glow


GEAR_POSES = (
    ('wheel', {}),
    ('turned', {'wheel': (0.26, 0, 0)}),
)

MODELS = {
    'clockhand': (clockhand, paint_clockhand, CLOCKHAND_POSES, 'ClockhandModel', dict(zoom=6.4, centre=(0, 4)), ()),
    'bolt': (bolt, paint_bolt, BOLT_POSES, 'BoltModel', dict(zoom=2.3, centre=(0, -30), size=(300, 420)), ('stub',)),
    'gear': (gear, paint_gear, GEAR_POSES, 'GearModel', dict(zoom=3.4, centre=(0, -6)), ('weight',)),
}

if __name__ == '__main__':
    rows = []
    for name, (build, paint, poses, java, cam, hidden) in MODELS.items():
        M = build()
        if len(sys.argv) < 3:
            M.write_java(f'{J}/{java}.java')
        tex, glow = paint(M)
        tex.save(f'{A}/textures/entity/{name}.png')
        glow.save(f'{A}/textures/entity/{name}_glow.png')
        if len(sys.argv) > 1:
            cam = dict(dict(size=(250, 340)), **cam)
            views = [(-0.5, poses[0]), (0.0, poses[0]), (2.6, poses[0]), (-1.35, poses[0])] + [(-0.6, pose) for pose in poses[1:]]
            shots = []
            for yaw, (label, pose) in views:
                hide = hidden if label in ('hung', 'swung', 'toll', 'stand') or name != 'bolt' else ('rod',)
                shots.append(M.render(tex, yaw=yaw, pitch=0.12, pose=pose, glow=glow, background=BACK, hidden=hide, **cam))
            rows.append(sheet(shots, len(shots), labels=[name] + [''] * 3 + [n for n, _ in poses[1:]]))
    if len(sys.argv) > 1 and rows:
        from PIL import Image
        out = Image.new('RGB', (max(r.width for r in rows), sum(r.height for r in rows)), (40, 44, 52))
        y = 0
        for r in rows:
            out.paste(r, (0, y))
            y += r.height
        out.save(sys.argv[1])
