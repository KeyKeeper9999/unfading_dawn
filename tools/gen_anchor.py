#!/usr/bin/env python3
"""The Helmsman's anchor as it flies when he casts it: mesh for AnchorModel.java and its texture. It is the anchor of his
hand (gen_sailor.py), with its middle at the model's origin, so that the renderer turns it about that.
Run from the repo root; pass a file name to also get a preview sheet."""
import sys

from modelkit import Box, Model, Painter, blotch, shade, sheet

A = 'src/main/resources/assets/unfading_dawn'
JAVA = 'src/main/java/com/keykeeper/dushi/client/AnchorModel.java'
SCALE = 1.5           # the Helmsman is drawn half as large again (SailorModel), and his anchor with him: AnchorModel.MODEL_SCALE

M = Model(64, 32, SCALE)
# (gen_sailor's boxes moved down by thirteen: the ring is at the bottom, the crown and the flukes at the top)
M.part('anchor', None, (0, 0, 0), boxes=[
    Box(-1.5, 10, -0.5, 3, 3, 1, 'iron', 'ring'), Box(-0.5, 8, -5, 1, 1, 10, 'haft', 'stock'), Box(-1, -11, -1, 2, 21, 2, 'iron', 'shank'),
    Box(-7, -13, -1, 14, 2, 2, 'iron', 'crown'), Box(-9, -13, -1, 2, 7, 2, 'iron', 'arm_a'), Box(7, -13, -1, 2, 7, 2, 'iron', 'arm_b'),
    Box(-10, -8, -1.5, 3, 3, 3, 'iron', 'fluke_a'), Box(7, -8, -1.5, 3, 3, 3, 'iron', 'fluke_b'), Box(-0.5, 13, -0.5, 1, 6, 1, 'link', 'links')])
M.pack()

IRON = (86, 88, 96)
IRON_D = (54, 56, 64)
IRON_L = (132, 136, 146)
RUST = (122, 74, 44)
RUST_D = (88, 52, 32)
WOOD = (104, 78, 50)
WOOD_D = (72, 52, 34)


def iron(face, px, py, fw, fh, rnd, tag=''):
    lit = {'top': 1.12, 'bottom': 0.72}.get(face, 1.0 - 0.18 * py / max(1, fh - 1))
    c = IRON
    if blotch(f'rust{tag}{face}', px, py, 2.4) > 0.62:
        c = RUST if blotch(f'rd{tag}{face}', px, py, 1.6) < 0.55 else RUST_D
    elif rnd.random() < 0.08:
        c = IRON_L
    elif min(fw, fh) > 2 and (px in (0, fw - 1) or py in (0, fh - 1)):
        c = IRON_D
    return shade(c, lit * rnd.choice((0.94, 1.0, 1.0, 1.05)))


def wood(face, px, py, fw, fh, rnd):
    c = WOOD_D if (px + py * 3) % 5 == 0 else WOOD
    return shade(c, {'top': 1.1, 'bottom': 0.75}.get(face, 1.0) * rnd.choice((0.94, 1.0, 1.04)))


def link(face, px, py, fw, fh, rnd):
    return IRON_D if py % 2 else IRON


def make():
    p = Painter(M, 761)
    for _, _, b in M.boxes():
        if b.mat == 'iron':
            p.fill_box(b, lambda face, px, py, fw, fh, rnd, tag=b.tag: iron(face, px, py, fw, fh, rnd, tag))
        elif b.mat == 'haft':
            p.fill_box(b, wood)
        else:
            p.fill_box(b, link)
    return p.tex


if __name__ == '__main__':
    M.write_java(JAVA)
    tex = make()
    tex.save(f'{A}/textures/entity/thrown_anchor.png')
    if len(sys.argv) > 1:
        shots = [M.render(tex, yaw=yaw, pitch=0.2, size=(300, 360), zoom=7.0, centre=(0, 0)) for yaw in (-0.5, 0.4, 1.57, 2.6)]
        sheet(shots, 4).save(sys.argv[1])
