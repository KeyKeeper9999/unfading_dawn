#!/usr/bin/env python3
"""The rust knight: mesh extras for RustKnightModel.java and the texture.
Run from the repo root; pass a file name to also get a preview sheet."""
import random
import sys

from humanoid import box_of, humanoid
from modelkit import Box, Painter, blotch, mix, shade, sheet

A = 'src/main/resources/assets/vsratie_dushi'
JAVA = 'src/main/java/com/keykeeper/dushi/client/RustKnightModel.java'

M = humanoid(128)
M.part('great_helm', 'head', (0, 0, 0), boxes=[Box(-4.5, -8.7, -4.5, 9, 9, 9, 'plate', 'helm'), Box(-0.5, -11.7, -4, 1, 3, 8, 'plate', 'crest')])
M.part('breastplate', 'body', (0, 0, 0), boxes=[Box(-4.5, -0.3, -2.5, 9, 8, 5, 'plate', 'breast'), Box(-4.5, 8.2, -2.5, 9, 5, 5, 'plate', 'tassets')])
M.part('cape', 'body', (0, 0.5, 2.6), (0.08, 0, 0), boxes=[Box(-4, 0, 0, 8, 17, 1, 'cape', 'cape')])
M.part('right_pauldron', 'right_arm', (0, 0, 0), boxes=[Box(-4, -3.2, -3, 5, 4, 6, 'plate', 'pauldron'), Box(-3.5, 5.5, -2.5, 5, 5, 5, 'plate', 'gauntlet')])
M.part('left_pauldron', 'left_arm', (0, 0, 0), boxes=[Box(-1, -3.2, -3, 5, 4, 6, 'plate', 'pauldron'), Box(-1.5, 5.5, -2.5, 5, 5, 5, 'plate', 'gauntlet')])
M.part('right_greave', 'right_leg', (0, 0, 0), boxes=[Box(-2.5, 5.5, -2.5, 5, 7, 5, 'plate', 'greave')])
M.part('left_greave', 'left_leg', (0, 0, 0), boxes=[Box(-2.5, 5.5, -2.5, 5, 7, 5, 'plate', 'greave')])
M.pack()

RUST = (128, 76, 46)
RUST_D = (92, 54, 36)
RUST_DD = (62, 38, 28)
RUST_L = (160, 100, 58)
STEEL = (112, 110, 114)
IRON_D = (84, 76, 76)
IRON_DD = (64, 58, 60)
STEEL_L = (150, 148, 150)
UNDER = (52, 46, 46)
UNDER_D = (38, 33, 34)
CAPE = (78, 38, 36)
CAPE_D = (54, 26, 28)
EMBER = (255, 140, 40)


def plate(face, px, py, fw, fh, rnd):
    """Dark iron eaten by rust in patches: lit from above, dark seams along the edges, a little bare steel on the ridges."""
    f = {'top': 1.18, 'bottom': 0.66}.get(face, 1.08 - 0.24 * py / max(1, fh - 1))
    key = rnd.random()                      # one draw per texel keeps the pattern stable
    amount = blotch(f'{face}{fw}{fh}', px + len(face) * 5, py, 2.6)
    if amount > 0.62:
        c = RUST_L if key < 0.3 else RUST
    elif amount > 0.45:
        c = RUST_D
    else:
        c = IRON_D if key < 0.75 else IRON_DD
    c = shade(c, f)
    if min(fw, fh) > 2:
        if py == 0 and face not in ('top', 'bottom'):
            c = mix(c, STEEL_L, 0.45)
        elif px in (0, fw - 1) or py == fh - 1:
            c = shade(c, 0.72)
    return c


def under(face, px, py, fw, fh, rnd):
    return shade(UNDER if (px + py) % 2 else UNDER_D, {'top': 1.1, 'bottom': 0.7}.get(face, 1.0))


def make():
    rnd = random.Random(7)
    p = Painter(M, 7)
    for name in ('head', 'body', 'right_arm', 'left_arm', 'right_leg', 'left_leg'):
        p.fill_box(box_of(M, name), under)
    # boots: the feet below the greaves
    for name in ('right_leg', 'left_leg'):
        b = box_of(M, name)
        for face in ('right', 'front', 'left', 'back'):
            fx, fy, fw, fh = b.faces()[face]
            for x in range(fw):
                for y in range(10, 12):
                    p.put(b, face, x, y, shade(RUST_DD, 1.0 if y == 11 else 1.2))
    for name, part in M.parts.items():
        for b in part['boxes']:
            if b.mat == 'plate':
                p.fill_box(b, plate)
    helm, crest = M.parts['great_helm']['boxes']
    # the visor: a slit with two sparks behind it, breathing holes below, a seam down the middle
    for x in range(1, 8):
        p.put(helm, 'front', x, 3, (14, 10, 10))
    p.put(helm, 'front', 2, 3, EMBER, glow=True)
    p.put(helm, 'front', 6, 3, EMBER, glow=True)
    for y in range(0, 9):
        if y != 3:
            p.put(helm, 'front', 4, y, mix(RUST, STEEL_L, 0.55) if y < 3 else shade(RUST_D, 0.9))
    for x, y in ((2, 5), (2, 7), (6, 5), (6, 7), (3, 6), (5, 6)):
        p.put(helm, 'front', x, y, RUST_DD)
    p.band(helm, 2, shade(RUST_DD, 1.1), faces=('right', 'left', 'back'))
    for face in ('right', 'left'):
        for x in (1, 4, 7):
            p.put(helm, face, x, 1, STEEL_L)
    breast, tassets = M.parts['breastplate']['boxes']
    # a ridge down the middle of the breastplate, a rivet at each corner, the stockade's key scratched off
    for y in range(1, 8):
        p.put(breast, 'front', 4, y, mix(RUST, STEEL_L, 0.5))
    for x, y in ((1, 1), (7, 1), (1, 6), (7, 6)):
        p.put(breast, 'front', x, y, STEEL_L)
        p.put(breast, 'back', x, y, STEEL_L)
    for x, y in ((2, 3), (3, 4), (5, 3), (6, 4), (2, 5), (6, 5)):
        p.put(breast, 'front', x, y, RUST_DD)
    # tassets: three hanging plates, front and back
    for face in ('front', 'back'):
        for x in (3, 6):
            for y in range(0, 5):
                p.put(tassets, face, x - 1 if face == 'front' else x - 1, y, RUST_DD)
        for x in range(9):
            if x % 3 != 2:
                p.put(tassets, face, x, 4, shade(RUST, 0.8))
    p.band(tassets, 0, mix(RUST, STEEL_L, 0.4))
    for name in ('right_pauldron', 'left_pauldron'):
        pauldron, gauntlet = M.parts[name]['boxes']
        p.band(pauldron, 3, RUST_DD)
        p.band(gauntlet, 0, mix(RUST, STEEL_L, 0.45))
        p.band(gauntlet, 3, RUST_DD)
        for face in ('front', 'back'):
            p.put(pauldron, face, 2, 1, STEEL_L)
    for name in ('right_greave', 'left_greave'):
        g = M.parts[name]['boxes'][0]
        p.band(g, 0, mix(RUST, STEEL_L, 0.45))
        for y in range(1, 7):
            p.put(g, 'front', 2, y, mix(RUST, STEEL_L, 0.35))
        p.band(g, 6, RUST_DD)
    # the cape: a torn rag, darker towards the hem, with holes
    cape = M.parts['cape']['boxes'][0]

    def cloth(face, px, py, fw, fh, r):
        if face in ('top', 'bottom', 'right', 'left'):
            return CAPE_D
        if py > 11 and r.random() < (py - 11) * 0.11:
            return None
        if py == fh - 1 and px % 2:
            return None
        c = CAPE_D if px in (1, 4, 6) and py > 2 else CAPE
        return shade(c, 1.08 - 0.3 * py / fh)
    p.fill_box(cape, cloth)
    return p.tex


REST = {'cape': (0.08, 0, 0)}
CHOP = {'right_arm': (-3.0, 0, 0.2), 'body': (-0.18, 0, 0), 'head': (-0.15, 0, 0), 'cape': (0.3, 0, 0)}
SWEEP = {'right_arm': (-1.45, -1.1, 0), 'body': (0, -0.55, 0), 'cape': (0.2, 0, 0)}

if __name__ == '__main__':
    M.write_java(JAVA)
    tex = make()
    tex.save(f'{A}/textures/entity/rust_knight.png')
    if len(sys.argv) > 1:
        shots = [M.render(tex, yaw=yaw, pose=pose, size=(260, 380), zoom=9.5, centre=(0, 7))
                 for yaw, pose in ((-0.45, REST), (0.9, REST), (3.0, REST), (-0.6, CHOP), (-0.3, SWEEP))]
        sheet(shots, 5).save(sys.argv[1])
