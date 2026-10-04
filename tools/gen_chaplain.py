#!/usr/bin/env python3
"""The Chaplain of the Stockade: mesh for ChaplainModel.java, the textures of both phases and their glow layers.
Run from the repo root; pass a file name to also get a preview sheet."""
import random
import sys

from modelkit import Box, Model, Painter, blotch, mix, shade, sheet

A = 'src/main/resources/assets/vsratie_dushi'
JAVA = 'src/main/java/com/keykeeper/dushi/client/ChaplainModel.java'

M = Model(64, 96)
M.part('robe', None, (0, 9, 0), boxes=[Box(-5, 0, -4, 10, 9, 8, 'robe', 'skirt'), Box(-6, 9, -5, 12, 6, 10, 'robe', 'hem')])
M.part('torso', None, (0, 9, 0), boxes=[Box(-4.5, -15, -3, 9, 15, 6, 'robe', 'torso'), Box(-2, -15, -3.7, 4, 22, 1, 'stole', 'stole')])
M.part('head', 'torso', (0, -15, 0), boxes=[Box(-3.5, -8, -3.5, 7, 8, 7, 'skull', 'head'), Box(-4, -11, -4, 8, 3, 8, 'mitre', 'mitre_band'),
                                           Box(-3.5, -17, -2, 7, 6, 4, 'mitre', 'mitre_peak'), Box(-4, -8, 3.5, 8, 11, 1, 'veil', 'veil')])
M.part('right_arm', 'torso', (-5.5, -13, 0), boxes=[Box(-3, -1, -2, 3, 10, 4, 'robe', 'sleeve'), Box(-3.5, 9, -2.5, 4, 5, 5, 'robe', 'cuff'),
                                                    Box(-2.5, 14, -1, 2, 3, 2, 'bone', 'hand')])
M.mirror('right_arm', 'left_arm')
M.part('censer_chain', 'right_arm', (-1.5, 16.5, 0), boxes=[Box(-0.5, 0, -0.5, 1, 8, 1, 'gold', 'chain')])
M.part('censer', 'censer_chain', (0, 8, 0), boxes=[Box(-2.5, 1, -2.5, 5, 4, 5, 'gold', 'bowl'), Box(-1.5, -1, -1.5, 3, 2, 3, 'gold', 'lid'),
                                                  Box(-1, 5, -1, 2, 1, 2, 'gold', 'foot')])
M.pack()

ROBE = (60, 44, 56)
ROBE_D = (42, 30, 40)
ROBE_L = (82, 62, 74)
TRIM = (150, 124, 62)
STOLE = (176, 164, 140)
STOLE_D = (132, 120, 100)
GOLD = (196, 160, 72)
GOLD_D = (130, 100, 44)
GOLD_L = (240, 214, 124)
BONE = (206, 198, 176)
BONE_D = (150, 142, 124)
BONE_DD = (96, 88, 78)
VEIL = (40, 30, 38)
LIGHT = (255, 244, 190)
DARK_FIRE = (150, 80, 220)
DARK_FIRE_L = (214, 170, 255)

FACE = ["bbbbbbb",
        "bbbbbbb",
        "bDDbDDb",
        "bKEbEKb",
        "bbdNdbb",
        "bdbbbdb",
        "dTtTtTd",
        "bdddddb"]


def robe(face, px, py, fw, fh, rnd):
    f = {'top': 1.14, 'bottom': 0.68}.get(face, 1.08 - 0.26 * py / max(1, fh - 1))
    c = ROBE_D if (fw > 4 and px % 3 == 1 and py > 0) else ROBE
    if blotch(f'robe{face}{fw}', px, py, 2.8) > 0.74:
        c = ROBE_D
    return shade(c, f)


def gold(face, px, py, fw, fh, rnd):
    f = {'top': 1.15, 'bottom': 0.7}.get(face, 1.0)
    c = GOLD_D if (px in (0, fw - 1) and fw > 2) or rnd.random() < 0.15 else GOLD
    return shade(c, f)


def bone(face, px, py, fw, fh, rnd):
    return shade(BONE if rnd.random() > 0.2 else BONE_D, {'top': 1.08, 'bottom': 0.75}.get(face, 1.0))


def make(dark):
    p = Painter(M, 31)
    boxes = {b.tag: b for _, _, b in M.boxes() if b.part != 'left_arm'}
    for _, _, b in M.boxes():
        p.fill_box(b, {'robe': robe, 'gold': gold, 'bone': bone, 'skull': bone, 'mitre': robe, 'stole': robe, 'veil': robe}[b.mat])
    eye = DARK_FIRE_L if dark else LIGHT
    head = boxes['head']
    p.art(head, 'front', FACE, {'b': BONE, 'D': BONE_DD, 'd': BONE_D, 'K': (20, 14, 18), 'E': eye, 'N': (40, 30, 30), 'T': BONE, 't': BONE_DD},
          glow_chars='E')
    for side in ('right', 'left'):
        for x, y in ((2, 3), (3, 3), (4, 4), (2, 6), (3, 6)):
            p.put(head, side, x, y, BONE_D)
    # ---- the mitre: a band with a gold border, a tall peak with a faded sign on it
    band, peak = boxes['mitre_band'], boxes['mitre_peak']
    p.band(band, 2, TRIM)
    p.band(band, 0, ROBE_L)
    for face in ('front', 'back'):
        fx, fy, fw, fh = peak.faces()[face]
        for x in range(fw):
            for y in range(fh):
                # a pointed top: the corners of the upper rows are cut away
                if y < 2 and (x < 2 - y or x > fw - 3 + y):
                    p.clear(fx + x, fy + y)
        for y in range(1, 6):
            p.put(peak, face, 3, y, TRIM)
        p.put(peak, face, 2, 3, TRIM)
        p.put(peak, face, 4, 3, TRIM)
        if dark:
            p.put(peak, face, 3, 3, DARK_FIRE_L, glow=True)
    for face in ('right', 'left'):
        fx, fy, fw, fh = peak.faces()[face]
        for x in range(fw):
            for y in range(2):
                p.clear(fx + x, fy + y)
    fx, fy, fw, fh = peak.faces()['top']
    for x in range(fw):
        for y in range(fh):
            if x < 2 or x > fw - 3:
                p.clear(fx + x, fy + y)
    # ---- the veil hangs from the mitre down the back, torn at the end
    veil = boxes['veil']

    def cloth(face, px, py, fw, fh, r):
        if face in ('front', 'back'):
            if py > 7 and (px + py) % 3 == 0:
                return None
            return shade(VEIL, 1.15 - 0.3 * py / fh)
        return VEIL
    p.fill_box(veil, cloth)
    # ---- the stole: a pale strip with gold crosses, fringed at the end
    stole = boxes['stole']

    def strip(face, px, py, fw, fh, r):
        if face == 'front':
            if py == fh - 1:
                return STOLE_D if px % 2 else None
            if px in (0, fw - 1):
                return TRIM
            return STOLE if (py // 2) % 2 else STOLE_D
        return STOLE_D
    p.fill_box(stole, strip)
    for y in (4, 12):
        p.put(stole, 'front', 1, y, GOLD_D)
        p.put(stole, 'front', 2, y, GOLD_D)
        p.put(stole, 'front', 1, y + 1, GOLD_L)
        p.put(stole, 'front', 2, y - 1, GOLD_L)
    # ---- robe trim: a gold hem, a rope at the waist, wide cuffs with a border
    hem = boxes['hem']
    p.band(hem, 5, TRIM, every=2)
    p.band(hem, 4, ROBE_L)
    fx, fy, fw, fh = hem.faces()['front']
    for face in ('right', 'front', 'left', 'back'):
        gx, gy, gw, gh = hem.faces()[face]
        for x in range(1, gw, 4):
            p.clear(gx + x, gy + gh - 1)      # the hem is torn
    skirt = boxes['skirt']
    p.band(skirt, 0, GOLD_D)
    for part in ('right_arm', 'left_arm'):
        sleeve, cuff, hand = M.parts[part]['boxes']
        p.band(cuff, 4, TRIM)
        p.band(cuff, 3, ROBE_L, every=2)
        p.band(sleeve, 0, ROBE_L)
        for x in range(4):
            for z in range(5):
                p.put(cuff, 'bottom', x, z, (18, 12, 16))
    torso = boxes['torso']
    p.band(torso, 0, ROBE_L, faces=('right', 'left', 'back'))
    # ---- the censer: pierced lid, a glow inside
    bowl, lid = boxes['bowl'], boxes['lid']
    fire, fire_l = (DARK_FIRE, DARK_FIRE_L) if dark else ((255, 150, 60), (255, 226, 150))
    for face in ('right', 'front', 'left', 'back'):
        p.put(bowl, face, 1, 1, fire, glow=True)
        p.put(bowl, face, 3, 1, fire, glow=True)
        p.put(bowl, face, 2, 2, fire_l, glow=True)
        p.put(lid, face, 1, 1, fire_l, glow=True)
    p.band(bowl, 0, GOLD_L)
    chain = boxes['chain']
    for face in ('right', 'front', 'left', 'back'):
        for y in range(8):
            p.put(chain, face, 0, y, GOLD_L if y % 2 else GOLD_D)
    if dark:
        # the black fire has crept into the robe: violet embers along the hem and the cuffs
        for face in ('right', 'front', 'left', 'back'):
            gx, gy, gw, gh = hem.faces()[face]
            for x in range(gw):
                if blotch(f'ember{face}', x, 0, 1.6) > 0.55:
                    p.put(hem, face, x, 4, DARK_FIRE, glow=True)
                    p.put(hem, face, x, 5, DARK_FIRE_L, glow=True)
        for part in ('right_arm', 'left_arm'):
            cuff = M.parts[part]['boxes'][1]
            p.band(cuff, 4, DARK_FIRE, every=2, glow=True)
    return p.tex, p.glow


FLOAT = {'robe': {'off': (0, -1.5, 0)}, 'torso': {'off': (0, -1.5, 0)}}
IDLE = dict(FLOAT, right_arm=(-0.35, 0, 0.3), left_arm=(-0.75, 0.5, 0), censer_chain=(0.35 + 0.35, 0, -0.3 - 0.3))
SPEARS = dict(IDLE, left_arm=(-1.6, 0.1, 0), torso={'off': (0, -1.5, 0), 'rot': (0, 0.2, 0)})
WRATH = dict(FLOAT, right_arm=(-2.8, 0, 0.6), left_arm=(-2.8, 0, -0.6), head=(-0.5, 0, 0), torso={'off': (0, -3.5, 0), 'rot': (-0.25, 0, 0)},
             robe={'off': (0, -3.5, 0)}, censer_chain=(2.8, 0, 0))
LITANY = dict(FLOAT, right_arm=(-1.25, -0.55, 0), left_arm=(-1.25, 0.55, 0), head=(0.45, 0, 0), censer_chain=(1.25, 0, 0))

if __name__ == '__main__':
    M.write_java(JAVA)
    shots = []
    for dark in (False, True):
        tex, glow = make(dark)
        suffix = '_dark' if dark else ''
        tex.save(f'{A}/textures/entity/chaplain{suffix}.png')
        glow.save(f'{A}/textures/entity/chaplain{suffix}_glow.png')
        for yaw, pose in ((-0.45, IDLE), (0.9, IDLE), (3.0, IDLE), (-0.5, SPEARS), (-0.4, WRATH), (-0.5, LITANY)):
            shots.append(M.render(tex, yaw=yaw, pose=pose, size=(260, 420), zoom=7.6, centre=(0, 0), glow=glow))
    if len(sys.argv) > 1:
        sheet(shots, 6).save(sys.argv[1])
