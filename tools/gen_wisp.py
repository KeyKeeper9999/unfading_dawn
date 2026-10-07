#!/usr/bin/env python3
"""The wisp of the Mire: mesh for WispModel.java, its texture and its glow (all of it glows).
Run from the repo root; pass a file name to also get a preview sheet."""
import sys

from modelkit import Box, Model, Painter, blotch, mix, shade, sheet

A = 'src/main/resources/assets/unfading_dawn'
JAVA = 'src/main/java/com/keykeeper/dushi/client/WispModel.java'

M = Model(64, 32)
# a light over the bog, and in the light the thing that carries it: the skull of something small, a candle-end of green
# flame standing in the crown of it, and under it three rags of the same flame trailing. It hangs at the height of a
# man's face. Everything hangs from 'all', which bobs.
M.part('all', None, (0, 14, 0))
M.part('skull', 'all', (0, 0, 0), boxes=[Box(-2.5, -2.5, -2.5, 5, 5, 5, 'bone', 'skull')])
M.part('jaw', 'skull', (0, 2, 0.5), boxes=[Box(-2, 0, -2.5, 4, 2, 4, 'bone', 'jaw')])
M.part('flame', 'all', (0, -2.5, 0), boxes=[Box(-1.5, -5, -1.5, 3, 5, 3, 'flame', 'flame'), Box(-0.5, -8, -0.5, 1, 3, 1, 'flame', 'tip')])
M.part('halo', 'all', (0, 0, 0), boxes=[Box(-4, -4, -4, 8, 8, 8, 'halo', 'halo')])
M.part('rag_a', 'all', (0, 3, 1.5), (0.3, 0, 0), boxes=[Box(-1.5, 0, 0, 3, 7, 1, 'rag', 'rag_a')])
M.part('rag_b', 'all', (-1.5, 3, -0.5), (0.2, 0, 0.35), boxes=[Box(-1, 0, 0, 2, 5, 1, 'rag', 'rag_b')])
M.part('rag_c', 'all', (1.5, 3, -0.5), (0.2, 0, -0.35), boxes=[Box(-1, 0, 0, 2, 6, 1, 'rag', 'rag_c')])
M.pack()

BONE = (188, 200, 170)
BONE_D = (120, 140, 116)
FLAME = (150, 255, 120)
FLAME_L = (224, 255, 196)
FLAME_D = (60, 170, 90)
BLACK = (10, 24, 14)


def make():
    p = Painter(M, 91)
    boxes = {b.tag: b for _, _, b in M.boxes()}
    for _, _, b in M.boxes():
        if b.mat == 'bone':
            p.fill_box(b, lambda face, px, py, fw, fh, r: shade(BONE if blotch(f'wb{face}', px, py, 1.8) < 0.65 else BONE_D, {'top': 1.1, 'bottom': 0.7}.get(face, 1.0)))
        elif b.mat == 'flame':
            def flame(face, px, py, fw, fh, r):
                if face in ('top', 'bottom'):
                    return FLAME_L
                edge = px in (0, fw - 1)
                if edge and py < 2 and fw > 1:
                    return None
                return FLAME_L if not edge and py >= fh - 3 else (FLAME if py >= 1 else FLAME_D)
            p.fill_box(b, flame)
        elif b.mat == 'halo':
            # a thin shell of light round it: a scatter of points, most of the shell empty
            p.fill_box(b, lambda face, px, py, fw, fh, r: (FLAME_D if r.random() < 0.5 else FLAME) if r.random() < 0.09 else None)
        elif b.mat == 'rag':
            def rag(face, px, py, fw, fh, r):
                if face not in ('front', 'back'):
                    return FLAME_D if py < fh - 2 else None
                if py > fh - 3 and (px + py) % 2:
                    return None
                return FLAME if py < 2 else (FLAME_D if (px + py) % 3 else FLAME)
            p.fill_box(b, rag)
    skull, jaw = boxes['skull'], boxes['jaw']
    p.art(skull, 'front', ["bbbbb",
                           "bEbEb",
                           "bEbEb",
                           "bbdbb",
                           "dBdBd"], {'b': BONE, 'd': BONE_D, 'E': FLAME_L, 'B': BLACK})
    for x in range(4):
        p.put(jaw, 'front', x, 0, BLACK if x % 2 else BONE)
    # all of it gives light: the glow is the picture itself, the bone a little dimmer than the flame
    for x in range(M.tex_w):
        for y in range(M.tex_h):
            r_, g_, b_, a_ = p.tex.getpixel((x, y))
            if a_:
                bone = abs(r_ - b_) < 40 and r_ > 100 and g_ < 230
                p.glow.putpixel((x, y), (int(r_ * 0.45), int(g_ * 0.6), int(b_ * 0.45), 255) if bone else (r_, g_, b_, 255))
    return p.tex, p.glow


STAND = {}
FLARE = {'flame': {'off': (0, -1, 0)}, 'jaw': (0.6, 0, 0), 'rag_a': (0.6, 0, 0), 'rag_b': (0.5, 0, 0.3), 'rag_c': (0.5, 0, -0.3)}

if __name__ == '__main__':
    M.write_java(JAVA) if len(sys.argv) < 3 else None
    tex, glow = make()
    tex.save(f'{A}/textures/entity/wisp.png')
    glow.save(f'{A}/textures/entity/wisp_glow.png')
    if len(sys.argv) > 1:
        shots = [M.render(tex, yaw=yaw, pose=pose, size=(240, 260), zoom=13.0, centre=(0, 13), glow=glow, background=(30, 40, 36))
                 for yaw, pose in ((-0.5, STAND), (0.0, STAND), (1.5, STAND), (2.7, STAND), (-0.6, FLARE))]
        sheet(shots, 5).save(sys.argv[1])
