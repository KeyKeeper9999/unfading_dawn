#!/usr/bin/env python3
"""A root of the Weeping Mother, come up through the ground: mesh for RootModel.java and its texture.
Run from the repo root; pass a file name to also get a preview sheet."""
import sys

from modelkit import Box, Model, Painter, blotch, shade, sheet

A = 'src/main/resources/assets/unfading_dawn'
JAVA = 'src/main/java/com/keykeeper/dushi/client/RootModel.java'
SCALE = 1.5           # must match MODEL_SCALE in RootModel.java

M = Model(64, 64, SCALE)
# Five roots out of one place: a thick one in the middle, with two bends in it, and four thinner ones round it that
# lean outwards, so that the whole of the ring the blow reaches has wood in it. Every root is its own chain of parts
# hanging from the ground (y = 24): the animation pushes each of them down under it by its own height and lets them
# come up one a little after another. The clods of earth they broke through lie where they were thrown and do not
# move with them.
M.part('clods', None, (0, 24, 0), boxes=[Box(-9, -2, -8, 4, 2, 4, 'mud', 'clod'), Box(5, -1, -9, 3, 1, 4, 'mud', 'clod'), Box(-8, -1, 5, 4, 1, 3, 'mud', 'clod'),
                                         Box(4, -2, 4, 4, 2, 4, 'mud', 'clod'), Box(-2, -1, -11, 3, 1, 2, 'mud', 'clod'), Box(8, -1, -1, 3, 1, 3, 'mud', 'clod'),
                                         Box(-12, -1, -1, 3, 1, 3, 'mud', 'clod'), Box(-1, -1, 8, 3, 1, 3, 'mud', 'clod')])
M.part('main', None, (0, 24, 0), (0.07, 0, -0.05), boxes=[Box(-3, -11, -3, 6, 11, 6, 'bark', 'main')])
M.part('main_mid', 'main', (0, -11, 0), (-0.3, 0.3, 0.22), boxes=[Box(-2, -12, -2, 4, 12, 4, 'bark', 'main_mid'), Box(2, -8, -1, 3, 2, 2, 'bark', 'spur')])
M.part('main_tip', 'main_mid', (0, -12, 0), (0.5, 0, -0.34), boxes=[Box(-1, -10, -1, 2, 10, 2, 'tip', 'main_tip')])
for name, (px, pz), rot, bend in (('east', (8, 1), (0.1, 0, 0.42), (-0.15, 0, -0.62)), ('west', (-8, -2), (-0.12, 0, -0.46), (0.2, 0, 0.6)),
                                  ('south', (1, 8), (0.44, 0, 0.08), (-0.66, 0.2, 0)), ('north', (-2, -8), (-0.4, 0, -0.1), (0.6, -0.2, 0))):
    M.part(name, None, (px, 24, pz), rot, boxes=[Box(-1.5, -12, -1.5, 3, 12, 3, 'bark', name)])
    M.part(name + '_tip', name, (0, -12, 0), bend, boxes=[Box(-1, -9, -1, 2, 9, 2, 'tip', name + '_tip')])
M.pack()

BARK = (70, 60, 46)
BARK_D = (46, 40, 32)
BARK_DD = (28, 24, 22)
BARK_L = (104, 92, 66)
MOSS = (74, 100, 50)
PALE = (176, 164, 128)
PALE_D = (128, 116, 88)
MUD = (60, 48, 38)
MUD_D = (40, 32, 28)
MUD_L = (88, 72, 54)


def bark(face, px, py, fw, fh, rnd):
    f = {'top': 1.1, 'bottom': 0.7}.get(face, 1.0)
    amount = blotch(f'rbark{fw}{fh}{face}', px, py * 0.4, 1.7)
    c = BARK_D if amount > 0.58 else (BARK_L if amount < 0.22 else BARK)
    if abs(blotch(f'rfur{fw}{fh}{face}', px, py * 0.25, 1.4) - 0.5) < 0.06:
        c = BARK_DD                              # the furrows of the bark, running up
    if py >= fh - 3 and face not in ('top', 'bottom') and rnd.random() < 0.6:
        c = MUD if rnd.random() < 0.6 else MUD_D     # earth still on it, near the ground
    elif blotch(f'rmoss{fw}{fh}{face}', px, py, 3.0) > 0.76 and face != 'bottom':
        c = MOSS
    return shade(c, f)


def tip(face, px, py, fw, fh, rnd):
    """The end of a root: the bark thins to bare pale wood, and the point is sharp."""
    if face == 'bottom':
        return shade(BARK_D, 0.8)
    if face == 'top':
        return shade(PALE, 1.1)
    if py < 2:
        return shade(PALE, 1.05) if (px + py) % 2 == 0 or py == 0 else shade(PALE_D, 1.0)
    if py < 5:
        return shade(PALE_D if (px + py) % 2 else BARK_L, 1.0)
    return bark(face, px, py, fw, fh, rnd)


def mud(face, px, py, fw, fh, rnd):
    c = MUD_D if blotch(f'mud{fw}{fh}{face}', px, py, 1.5) > 0.6 else (MUD_L if rnd.random() < 0.18 else MUD)
    return shade(c, {'top': 1.12, 'bottom': 0.7}.get(face, 0.94))


def make():
    p = Painter(M, 131)
    for _, _, b in M.boxes():
        p.fill_box(b, {'bark': bark, 'tip': tip, 'mud': mud}[b.mat])
    return p.tex


def sunk(share):
    """The pose RootModel.setupAnim gives a root that is `share` of its way up."""
    out = {}
    for name, height in HEIGHTS.items():
        out[name] = {'off': (0, (1.0 - share) * height, 0)}
    return out


# how far each root reaches over the ground when it is out, in sixteenths: what the animation pushes it down by
HEIGHTS = {"main": 33, 'east': 20, 'west': 20, 'south': 20, 'north': 20}
UP = {}
POKE = sunk(0.12)
HALF = sunk(0.55)

if __name__ == '__main__':
    M.write_java(JAVA) if len(sys.argv) < 3 else None
    tex = make()
    tex.save(f'{A}/textures/entity/root.png')
    if len(sys.argv) > 1:
        # how high each root really stands, measured: the animation's numbers must not be less
        import numpy as np
        for name in HEIGHTS:
            top = 24.0
            for part in (name, name + '_mid', name + '_tip'):
                if part in M.parts:
                    m, t = M.transform(part, {})
                    for b in M.parts[part]['boxes']:
                        for pts in b.corners().values():
                            for c in pts:
                                top = min(top, (m @ np.array(c, float) + t)[1])
            print(f'root {name}: stands {24.0 - top:.1f} over the ground, the animation sinks it by {HEIGHTS[name]}')
        shots = [M.render(tex, yaw=yaw, pose=pose, size=(300, 330), zoom=6.0, centre=(0, 6))
                 for yaw, pose in ((-0.5, UP), (0.0, UP), (1.5, UP), (2.6, UP), (-0.5, POKE), (-0.5, HALF))]
        # (the preview has no ground: what is under the line y = 24 would be hidden by it)
        sheet(shots, 6).save(sys.argv[1])
