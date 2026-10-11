#!/usr/bin/env python3
"""A shaft of sunlight: mesh for SunShaftModel.java and its texture. A box a block across and a block high with a
narrower one in it; the sides of both fade to nothing at their edges and towards the top, so that, stretched to its
width and its height by SunShaftRenderer and drawn see-through, it is a soft column of light coming down from the sky.
Run from the repo root; pass a file name to also get a preview of the texture."""
import sys

from modelkit import Box, Model, Painter

A = 'src/main/resources/assets/unfading_dawn'
JAVA = 'src/main/java/com/keykeeper/dushi/client/SunShaftModel.java'

M = Model(64, 64, 1.0)
M.part('shaft', None, (0, 24, 0), boxes=[Box(-8, -16, -8, 16, 16, 16, 'glow', 'outer'), Box(-3, -16, -3, 6, 16, 6, 'glow', 'core')])
M.pack()

LIGHT = (255, 238, 178)
CORE = (255, 250, 222)


DUSK = (150, 112, 232)          # the light of the Sky Beneath (SunShaftEntity.dusk): a sheath round a beacon's beam, see make_sheath
DUSK_CORE = (214, 204, 255)
AMBER = (255, 170, 60)          # and in the amber of the Founder's seals (SunShaftEntity.amber): what the stopped clock strikes with
AMBER_CORE = (255, 232, 170)


def make(light=LIGHT, core=CORE):
    p = Painter(M, 1)
    for _, _, b in M.boxes():
        strength, colour = (190, light) if b.tag == 'outer' else (240, core)
        for face, (fx, fy, fw, fh) in b.faces().items():
            for py in range(fh):
                for px in range(fw):
                    if face in ('top', 'bottom'):
                        p.clear(fx + px, fy + py)
                        continue
                    u = (px + 0.5) / fw
                    across = max(0.0, 1.0 - abs(2.0 * u - 1.0)) ** 1.4
                    v = py / max(1, fh - 1)                      # 0 at the top of the side, 1 at the ground
                    up = min(1.0, v / 0.5) ** 1.5
                    a = int(strength * across * (0.25 + 0.75 * up))
                    if a < 4:
                        p.clear(fx + px, fy + py)
                    else:
                        p.px(fx + px, fy + py, colour, alpha=a)
    return p.tex


def make_sheath(light=DUSK, core=DUSK_CORE):
    """The same two boxes as a sheath of light round the beam of a beacon (SunShaftRenderer draws the game's own beam in
    the middle of it): the same from end to end, strongest at the beam and gone at its own edges - so that the beam
    looks to shine into the air round it."""
    p = Painter(M, 1)
    for _, _, b in M.boxes():
        outer = b.tag == 'outer'
        for face, (fx, fy, fw, fh) in b.faces().items():
            for py in range(fh):
                for px in range(fw):
                    if face in ('top', 'bottom'):
                        p.clear(fx + px, fy + py)
                        continue
                    across = max(0.0, 1.0 - abs(2.0 * (px + 0.5) / fw - 1.0))
                    a = int(120 * across ** 1.6) if outer else int(215 * across ** 0.8)
                    if a < 4:
                        p.clear(fx + px, fy + py)
                    else:
                        p.px(fx + px, fy + py, light if outer else core, alpha=a)
    return p.tex


if __name__ == '__main__':
    M.write_java(JAVA)
    tex = make()
    tex.save(f'{A}/textures/entity/sun_shaft.png')
    make_sheath().save(f'{A}/textures/entity/dusk_beam.png')
    make(AMBER, AMBER_CORE).save(f'{A}/textures/entity/amber_shaft.png')
    if len(sys.argv) > 1:
        tex.resize((512, 512), 0).save(sys.argv[1])
