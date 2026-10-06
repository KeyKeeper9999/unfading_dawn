#!/usr/bin/env python3
"""The Shatun of the Black Firwood: mesh for ShatunModel.java, its texture and the glow of its eyes.
Run from the repo root; pass a file name to also get a preview sheet."""
import sys

from modelkit import Box, Model, Painter, blotch, mix, shade, sheet

A = 'src/main/resources/assets/vsratie_dushi'
JAVA = 'src/main/java/com/keykeeper/dushi/client/ShatunModel.java'
SCALE = 1.5           # must match MODEL_SCALE in ShatunModel.java

M = Model(128, 128, SCALE)
# A shatun is a bear that did not lie down for the winter: what is left of one walks, gaunt and out of its mind,
# until it drops. This one the skete kept on a chain at its gate and beat awake every time it began to nod, year
# after year. The chain is broken; the collar is still on it. A bear with the flesh gone off its frame: a high hump of
# shoulder, a barrel of ribs, a waist one could span, the hide hanging and worn bald in patches.
# The trunk hangs from the hips ('body'), so that it can rear: the chest, the head and the fore legs go up with it,
# the hind legs stay where they are.
M.part('all', None, (0, 0, 0))
M.part('body', 'all', (0, 6, 9), boxes=[Box(-6, -8, -6, 12, 13, 11, 'fur', 'rump'), Box(-1.5, -6, 5, 3, 3, 2, 'fur', 'tail')])
M.part('chest', 'body', (0, -2, -6), boxes=[Box(-7.5, -8, -15, 15, 15, 15, 'fur', 'chest'), Box(-5, -11, -13, 10, 3, 9, 'fur', 'hump'),
                                            Box(-6, -6, -17, 12, 11, 2, 'iron', 'collar')])
M.part('chain', 'chest', (-3, 5, -16), (0.15, 0, 0.12), boxes=[Box(-1, 0, -1, 2, 11, 2, 'chain', 'chain')])
M.part('head', 'chest', (0, -1, -15), (0.25, 0, 0), boxes=[Box(-4.5, -5, -8, 9, 9, 8, 'fur', 'skull'), Box(-2.5, -0.5, -13, 5, 4, 5, 'snout', 'snout'),
                                                           Box(-5, -7, -3, 3, 3, 2, 'fur', 'ear_r'), Box(2, -7, -3, 3, 3, 2, 'fur', 'ear_l')])
M.part('jaw', 'head', (0, 3.5, -8), (0.1, 0, 0), boxes=[Box(-2, 0, -5, 4, 2, 5, 'jaw', 'jaw'), Box(-2, -1, -5, 4, 1, 1, 'tooth', 'teeth')])
M.part('right_arm', 'chest', (-5, 1, -10), boxes=[Box(-3, -1, -3, 5, 11, 6, 'fur', 'upper')])
M.part('right_forearm', 'right_arm', (-0.5, 10, 0), boxes=[Box(-2.5, 0, -2.5, 5, 8, 5, 'fur', 'fore'), Box(-3, 7, -5.5, 6, 2, 7, 'paw', 'paw'),
                                                           Box(-3, 8, -7.5, 6, 1, 2, 'claw', 'claws')])
M.mirror('right_arm', 'left_arm')
M.mirror('right_forearm', 'left_forearm')
M.part('right_leg', 'all', (-5, 7, 11), boxes=[Box(-3, -2, -4, 5, 12, 8, 'fur', 'thigh')])
M.part('right_shin', 'right_leg', (-0.5, 10, 1), boxes=[Box(-2, 0, -2, 4, 7, 4, 'fur', 'shin'), Box(-2.5, 5, -5, 5, 2, 7, 'paw', 'foot')])
M.mirror('right_leg', 'left_leg', parent='all')
M.mirror('right_shin', 'left_shin')
M.pack()

FUR = (74, 60, 48)
FUR_D = (50, 40, 34)
FUR_DD = (32, 26, 24)
FUR_L = (104, 86, 66)
BALD = (132, 112, 102)
BALD_D = (98, 80, 76)
SCAR = (150, 96, 92)
SNOUT = (120, 100, 84)
NOSE = (22, 18, 18)
IRON = (84, 82, 88)
IRON_D = (52, 50, 58)
RUST = (126, 76, 46)
CLAW = (206, 196, 168)
CLAW_D = (140, 128, 106)
MOUTH = (110, 34, 40)
TOOTH = (226, 218, 190)
EYE = (255, 176, 72)
EYE_D = (150, 60, 40)
BLACK = (14, 12, 12)


def fur(tag):
    def fn(face, px, py, fw, fh, rnd):
        f = {'top': 1.1, 'bottom': 0.68}.get(face, 1.05 - 0.22 * py / max(1, fh - 1))
        amount = blotch(f'fur{tag}{face}', px, py * 0.6, 2.6)
        c = FUR_D if amount > 0.6 else (FUR_L if amount < 0.2 else FUR)
        if (px + py * 2) % 5 == 0:
            c = shade(c, 0.86)                                   # the lie of the hair
        # where the hide is worn through to the skin: flanks, rump, the joints
        if tag in ('chest', 'rump', 'thigh', 'upper') and face in ('right', 'left', 'top') and blotch(f'bald{tag}{face}', px, py, 3.2) > 0.76:
            c = BALD if blotch(f'bald2{tag}{face}', px, py, 1.4) < 0.6 else BALD_D
        # the ribs showing through the flank
        if tag == 'chest' and face in ('right', 'left') and 3 <= py <= fh - 4 and px % 3 == 1 and 2 <= px <= fw - 3:
            c = shade(c, 0.72)
        if tag == 'rump' and face in ('right', 'left') and py > fh - 5:
            c = shade(c, 0.85)                                   # the hollow of the belly
        if tag in ('chest', 'hump') and face == 'top' and blotch('scar', px, py, 2.0) > 0.82:
            c = SCAR                                             # where they beat it
        return shade(c, f)
    return fn


def make():
    p = Painter(M, 171)
    boxes = {b.tag: b for _, _, b in M.boxes()}
    for name, _, b in M.boxes():
        if b.mat == 'fur':
            p.fill_box(b, fur(b.tag))
        elif b.mat == 'snout':
            p.fill_box(b, lambda face, px, py, fw, fh, r: shade(SNOUT if blotch(f'sn{face}', px, py, 1.8) < 0.65 else FUR_L, {'top': 1.1, 'bottom': 0.7}.get(face, 1.0 - 0.05 * py)))
        elif b.mat == 'jaw':
            p.fill_box(b, lambda face, px, py, fw, fh, r: MOUTH if face == 'top' else shade(FUR_D, {'bottom': 0.7}.get(face, 0.95)))
        elif b.mat == 'tooth':
            p.fill_box(b, lambda face, px, py, fw, fh, r: (TOOTH if px % 2 == 0 else None) if face in ('front', 'back') else (TOOTH if face in ('right', 'left') else None))
        elif b.mat == 'paw':
            p.fill_box(b, lambda face, px, py, fw, fh, r: shade(FUR_DD if face != 'top' else FUR_D, 1.0))
        elif b.mat == 'claw':
            p.fill_box(b, lambda face, px, py, fw, fh, r: (CLAW if px % 2 == 0 else None) if face in ('top', 'bottom', 'front') else (CLAW_D if face in ('right', 'left') else None))
        elif b.mat == 'iron':
            def collar(face, px, py, fw, fh, rnd):
                if face in ('front', 'back') and 2 <= px <= fw - 3 and 2 <= py <= fh - 3:
                    return None                                  # a ring: the neck goes through it
                return shade(RUST if blotch(f'col{face}', px, py, 1.6) > 0.62 else (IRON_D if (px + py) % 4 == 0 else IRON), {'top': 1.15, 'bottom': 0.7}.get(face, 1.0))
            p.fill_box(b, collar)
        elif b.mat == 'chain':
            def chain(face, px, py, fw, fh, rnd):
                if face in ('top', 'bottom'):
                    return IRON_D
                link = (py // 2 + (0 if face in ('front', 'back') else 1)) % 2
                return (IRON if px == link else None) if py % 2 == 0 else (IRON if rnd.random() < 0.3 else (IRON_D if px == link else RUST))
            p.fill_box(b, chain)
    # the face: small eyes far apart, sunk and rimmed with raw red, that do not blink; the skull worn bald between them
    skull, snout = boxes['skull'], boxes['snout']
    p.art(skull, 'front', ["fdfffdffd",
                           "dfbbfbbfd",
                           "fbbffbbbf",
                           "fRRfffRRf",
                           "REBRfRBER",
                           "fRRfffRRf",
                           "ffdfffdff",
                           "dffffffdf",
                           "ddfffffdd"], {'f': FUR, 'd': FUR_D, 'b': BALD_D, 'R': EYE_D, 'E': EYE, 'B': BLACK}, glow_chars='E')
    p.art(snout, 'front', ["snnns", "snnns", "ssdss", "sBBBs"], {'s': SNOUT, 'n': NOSE, 'd': FUR_D, 'B': BLACK})
    for x in range(5):
        p.put(snout, 'bottom', x, 0, MOUTH)
    return p.tex, p.glow


STAND = {}
WALK = {'right_arm': (-0.5, 0, 0), 'left_arm': (0.45, 0, 0), 'right_leg': (0.4, 0, 0), 'left_leg': (-0.45, 0, 0), 'right_forearm': (0.3, 0, 0), 'left_shin': (0.3, 0, 0),
        'head': (0.1, 0.2, 0)}
# the poses of ShatunModel.setupAnim
SWIPE_BACK = {'body': (-0.25, 0.3, 0), 'chest': (0, 0.2, -0.15), 'right_arm': (-1.9, 0, 0.7), 'right_forearm': (-0.5, 0, 0), 'head': (0.1, -0.5, 0), 'jaw': (0.5, 0, 0)}
SWIPE = {'body': (0.0, -0.3, 0), 'chest': (0.1, -0.3, 0.15), 'right_arm': (-0.9, 0, -0.6), 'right_forearm': (-0.3, 0, 0), 'head': (0.2, 0.3, 0), 'jaw': (0.3, 0, 0)}
BITE_BACK = {'chest': (-0.1, 0, 0), 'head': (-0.4, 0, 0), 'jaw': (0.2, 0, 0), 'right_arm': (0.3, 0, 0), 'left_arm': (0.3, 0, 0)}
BITE = {'chest': (0.15, 0, 0), 'head': (-0.3, 0, 0), 'jaw': (1.0, 0, 0), 'right_arm': (-0.5, 0, 0), 'left_arm': (-0.5, 0, 0), 'right_leg': (0.5, 0, 0), 'left_leg': (0.5, 0, 0)}
REAR = {'body': (-1.05, 0, 0), 'chest': (-0.15, 0, 0), 'head': (0.75, 0, 0), 'jaw': (0.7, 0, 0), 'right_arm': (-1.3, 0, 0.5), 'left_arm': (-1.3, 0, -0.5),
        'right_forearm': (-0.6, 0, 0), 'left_forearm': (-0.6, 0, 0), 'right_leg': (0.25, 0, 0), 'left_leg': (0.25, 0, 0)}
SLAM = {'body': (0.14, 0, 0), 'chest': (0.1, 0, 0), 'head': (0.2, 0, 0), 'right_arm': (-0.7, 0, 0.1), 'left_arm': (-0.7, 0, -0.1), 'right_forearm': (0.2, 0, 0),
        'left_forearm': (0.2, 0, 0)}
ROAR = {'body': (-0.3, 0, 0), 'chest': (-0.1, 0, 0), 'head': (-0.8, 0, 0), 'jaw': (1.1, 0, 0), 'right_arm': (0.25, 0, 0.15), 'left_arm': (0.25, 0, -0.15)}
CHARGE = {'body': (0.08, 0, 0), 'head': (0.3, 0, 0), 'right_arm': (-0.9, 0, 0), 'left_arm': (-0.7, 0, 0), 'right_forearm': (0.5, 0, 0), 'left_forearm': (0.4, 0, 0),
          'right_leg': (0.7, 0, 0), 'left_leg': (0.6, 0, 0), 'right_shin': (-0.3, 0, 0), 'left_shin': (-0.3, 0, 0)}

if __name__ == '__main__':
    M.write_java(JAVA) if len(sys.argv) < 3 else None
    tex, glow = make()
    tex.save(f'{A}/textures/entity/shatun.png')
    glow.save(f'{A}/textures/entity/shatun_glow.png')
    if len(sys.argv) > 1:
        shots = [M.render(tex, yaw=yaw, pose=pose, size=(420, 400), zoom=4.6, centre=(0, -6), glow=glow, background=(52, 62, 60))
                 for yaw, pose in ((-0.6, STAND), (0.0, STAND), (-1.57, STAND), (2.5, STAND), (-1.57, WALK), (-0.8, SWIPE_BACK), (-0.8, SWIPE), (-1.57, BITE_BACK),
                                   (-1.57, BITE), (-1.57, REAR), (-0.7, REAR), (-1.57, SLAM), (-1.2, ROAR), (-1.57, CHARGE))]
        sheet(shots, 7).save(sys.argv[1])
