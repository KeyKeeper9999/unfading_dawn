#!/usr/bin/env python3
"""Item icons, item models and item definitions. Run from the repo root; pass a file name to also get a preview sheet."""
import json
import os
import sys

from PIL import Image

from sprite import Sprite, diagonal_blade, hilt, mix, shade

A = 'src/main/resources/assets/vsratie_dushi'
MOD = 'vsratie_dushi'

O = (30, 26, 28)
S, s, d, D = (226, 230, 234), (174, 180, 190), (118, 124, 136), (78, 82, 94)
R, r, q = (164, 98, 54), (120, 70, 42), (84, 48, 32)
W, w, b = (152, 110, 66), (112, 78, 48), (74, 50, 32)
L, l = (116, 80, 50), (82, 56, 38)
G, g, h = (238, 202, 102), (186, 144, 62), (128, 96, 40)
C, c, k = (210, 202, 180), (164, 156, 136), (112, 104, 90)
F, f, e = (255, 220, 120), (255, 152, 52), (222, 82, 32)
B, a, u = (190, 236, 255), (110, 186, 246), (56, 110, 206)
P, p = (206, 170, 255), (140, 98, 224)
M, m = (92, 92, 102), (60, 60, 70)
X, x = (66, 48, 60), (44, 30, 40)
N, n = (138, 50, 46), (94, 32, 32)
V, v = (98, 146, 80), (62, 98, 54)
PAL = dict(o=O, S=S, s=s, d=d, D=D, R=R, r=r, q=q, W=W, w=w, b=b, L=L, l=l, G=G, g=g, h=h, C=C, c=c, k=k, F=F, f=f, e=e, B=B, a=a, u=u, P=P, p=p,
           M=M, m=m, X=X, x=x, N=N, n=n, V=V, v=v)

ICONS = {}          # name -> Sprite
HANDHELD = set()    # held like a tool
BIG = set()         # 32 x 32 icons held like a long weapon


def icon(name, size=16, handheld=False, big=False):
    sp = Sprite(size)
    ICONS[name] = sp
    if handheld:
        HANDHELD.add(name)
    if big:
        BIG.add(name)
    return sp


def ascii_icon(name, rows, extra=None, handheld=False, lock=''):
    sp = icon(name, len(rows), handheld)
    pal = dict(PAL)
    if extra:
        pal.update(extra)
    sp.art(rows, pal, lock=lock)
    return sp


# ---------------------------------------------------------------------------------------------------- weapons

WEAPON_ART = {
    'longsword': [
        "..............oo",
        ".............oSo",
        "............oSso",
        "...........oSsdo",
        "..........oSsdo.",
        ".........oSsdo..",
        "........oSsdo...",
        ".oo....oSsdo....",
        ".oGo..oSsdo.....",
        "..oGooSsdo......",
        "...oGgsdo.......",
        "....oGgo........",
        "...oLogGo.......",
        "..oLlo.oGo......",
        ".oGlo...oo......",
        ".ooo............"],
    'shortsword': [
        "................",
        "................",
        "...........ooo..",
        "..........oSSo..",
        ".........oSsdo..",
        "........oSsdo...",
        ".......oSsdo....",
        "..oo..oSsdo.....",
        "..oMooSsdo......",
        "...oMmsdo.......",
        "....oMmo........",
        "...oLomMo.......",
        "..oLlo.oo.......",
        ".oMlo...........",
        ".ooo............",
        "................"],
    'broadsword': [
        ".............ooo",
        "...........ooSSo",
        "..........oSSsdo",
        ".........oSSsdo.",
        "........oSSsdo..",
        ".......oSSsdo...",
        "......oSSsdo....",
        ".oo..oSSsdo.....",
        ".oMooSSsdo......",
        "..oMmSsdo.......",
        "...oMmdo........",
        "...oLoMmo.......",
        "..oLlo.oMo......",
        ".oDlo...oo......",
        ".ooo............",
        "................"],
    'broken_sword': [
        "................",
        "................",
        "................",
        "................",
        ".........o.oo...",
        "........oRosdo..",
        ".......oSsRdo...",
        "..oo..oSsdo.....",
        "..oMooSrdo......",
        "...oMmsdo.......",
        "....oMmo........",
        "...oLomMo.......",
        "..oLlo.oo.......",
        ".omlo...........",
        ".ooo............",
        "................"],
    'dagger': [
        "................",
        "................",
        "................",
        "...........oo...",
        "..........oSo...",
        ".........oSso...",
        "........oSsdo...",
        "...oo..oSsdo....",
        "...oGooSsdo.....",
        "....oGgsdo......",
        ".....oGgo.......",
        "....oLogo.......",
        "...oLlo.........",
        "..oGo...........",
        "..oo............",
        "................"],
    'shiv': [
        "................",
        "................",
        "................",
        "............o...",
        "...........oso..",
        "..........oSdo..",
        ".........oSdo...",
        "........oSdo....",
        ".......osdo.....",
        "......oCdo......",
        ".....oCco.......",
        "....ocCo........",
        "...oCco.........",
        "...oco..........",
        "....o...........",
        "................"],
    'sabre': [
        "...........oooo.",
        "..........oSSso.",
        ".........oSsdo..",
        ".........oSsdo..",
        "........oSsdo...",
        "........oSsdo...",
        ".......oSsdo....",
        "......oSsdo.....",
        "..oo.oSsdo......",
        "..oGoSsdo.......",
        "...oGgdo........",
        "..oGoGgo........",
        ".oGoLlo.........",
        ".oGLlo..........",
        "..oGo...........",
        "...o............"],
    'hatchet': [
        "................",
        "........ooo.....",
        ".......oSSsoo...",
        "......oSSsWwo...",
        "......oSsdWwo...",
        "......oSdoWwo...",
        ".......oooWo....",
        "........oWwo....",
        ".......oWwo.....",
        "......oWwo......",
        ".....oWwo.......",
        "....oWwo........",
        "...oWwo.........",
        "..oWwo..........",
        "..owo...........",
        "...o............"],
    'battle_axe': [
        ".....oo...oo....",
        "....oSsooosSo...",
        "...oSssWwsSSso..",
        "...oSsdWwdsSdo..",
        "...oSddWwddsdo..",
        "....oooWwoooo...",
        "......oWwo......",
        "......oWwo......",
        ".....oWwo.......",
        ".....oWwo.......",
        "....oWwo........",
        "....oWwo........",
        "...oWwo.........",
        "...oLlo.........",
        "..oLlo..........",
        "..ooo..........."],
    'club': [
        "................",
        "..........ooo...",
        ".........oWWwo..",
        "........oWWbwwo.",
        "........oWwwwbo.",
        ".......oWbwwwo..",
        ".......oWwwbo...",
        "......oWwwo.....",
        ".....oWwwo......",
        "....oWwo........",
        "...oWwo.........",
        "..oLwo..........",
        ".oLlo...........",
        ".olo............",
        "..o.............",
        "................"],
    'flanged_mace': [
        "................",
        "..........oo....",
        ".........oSso...",
        ".......ooSsdoo..",
        "......oSsMmdsdo.",
        "......osdmmddo..",
        ".......oodmdoo..",
        "........osdo....",
        ".......odDo.....",
        "......odDo......",
        ".....odDo.......",
        "....odDo........",
        "...oLlo.........",
        "..oLlo..........",
        ".oMlo...........",
        ".ooo............"],
}
for weapon_name, weapon_rows in WEAPON_ART.items():
    ascii_icon(weapon_name, weapon_rows, handheld=True)

# censer flail: a rod, a chain and the chaplain's censer burning at the end of it
sp = icon('censer_flail', handheld=True)
for i in range(7):
    sp.px(1 + i, 14 - i, X if i % 2 else x)
    sp.px(2 + i, 14 - i, x)
sp.pts([(8, 6), (9, 5), (10, 5), (11, 4)], G)
sp.pts([(9, 6), (11, 5)], h)
sp.art([".gGg.",
        "gGfGh",
        "GfFfh",
        "gGfhh",
        ".hhh."], PAL, ox=10, oy=5, lock='fF')
sp.px(12, 4, g)
sp.outline()

# scholar's staff: a crooked stick with a pale crystal bound to its top
sp = icon('scholar_staff', handheld=True)
for i in range(10):
    sp.px(2 + i, 13 - i, W if i % 4 else w)
    sp.px(3 + i, 13 - i, w)
sp.art(["..aB.",
        ".aBBa",
        "wBBau",
        "wwau.",
        ".w..."], PAL, ox=10, oy=0, lock='B')
sp.pts([(10, 5), (9, 6)], L)
sp.outline()

# ---------------------------------------------------------------------------------------------------- big weapons, 32 x 32

sp = icon('rust_greatsword', 32, big=True)
# a wide blade pitted with rust, a long grip for two hands, a heavy straight guard
for i in range(19):
    xx, yy = 8 + i, 21 - i
    for kk, col in enumerate((O, S, s, s, d, D, O)):
        sp.px(xx + kk, yy, col)
    sp.px(xx + 6, yy + 1, O)
for (px_, py_) in ((11, 20), (14, 17), (16, 14), (20, 11), (13, 18), (22, 8), (24, 7), (19, 12), (25, 5), (17, 15), (23, 9)):
    sp.px(px_, py_, R)
    sp.px(px_ + 1, py_, r)
for kk, col in enumerate((O, S, s, d, O)):
    sp.px(27 + kk, 2, col)
for kk, col in enumerate((O, S, d, O)):
    sp.px(28 + kk, 1, col)
sp.pts([(29, 0), (30, 0), (31, 1), (31, 2)], O)
for i in range(7):
    xx, yy = 7 - i, 24 + i
    sp.px(xx - 1, yy, O)
    sp.px(xx, yy, L if i % 2 else l)
    sp.px(xx + 1, yy, l)
    sp.px(xx + 2, yy, O)
for kk in range(-5, 6):
    sp.px(9 + kk, 22 + kk, M)
    sp.px(10 + kk, 22 + kk, m)
    sp.px(9 + kk, 23 + kk, O)
    sp.px(10 + kk, 21 + kk, O)
sp.pts([(3, 16), (4, 16), (15, 28), (15, 27)], O)
for xx, yy, col in ((0, 30, M), (1, 30, m), (0, 31, m), (1, 31, O), (2, 31, O), (2, 30, O), (0, 29, O)):
    sp.px(xx, yy, col)

sp = icon('great_key', 32, big=True)
KEYC, KEYD, KEYL = (124, 108, 72), (86, 72, 50), (180, 158, 104)
# the bow: a heavy ring at the lower left
for xx in range(0, 11):
    for yy in range(21, 32):
        dist = ((xx - 5) ** 2 + (yy - 26) ** 2) ** 0.5
        if 2.6 < dist <= 5.2:
            sp.px(xx, yy, KEYL if xx + yy < 30 else KEYC)
# the shaft
for i in range(20):
    xx, yy = 8 + i, 22 - i
    sp.px(xx, yy, KEYL)
    sp.px(xx + 1, yy, KEYC)
    sp.px(xx + 2, yy, KEYD)
    sp.px(xx + 1, yy + 1, KEYD)
# collars on the shaft
for cx_, cy_ in ((11, 19), (13, 17)):
    sp.pts([(cx_ - 1, cy_ - 1), (cx_ + 3, cy_ + 2), (cx_ + 2, cy_ + 2), (cx_ - 1, cy_)], KEYL)
# the bit: two blocks of wards hanging off the lower right side of the shaft near the top
for (ox_, oy_, ww, hh) in ((23, 9, 6, 4), (20, 13, 4, 3)):
    for i in range(ww):
        for j in range(hh):
            sp.px(ox_ + i + j, oy_ + j + i // 3, KEYC if (i + j) % 3 else KEYD)
sp.pts([(27, 12), (28, 13), (25, 11)], (34, 28, 24))
sp.pts([(27, 2), (28, 1), (29, 1), (28, 2), (29, 2), (29, 3), (30, 2)], KEYL)
sp.outline(0.45, inner=False)

# ---------------------------------------------------------------------------------------------------- bow

def bow(name, pull):
    sp = icon(name)
    limb = [(13, 1), (12, 1), (11, 2), (10, 2), (9, 3), (8, 4), (7, 5), (6, 6), (5, 7), (4, 8), (3, 9), (2, 10), (2, 11), (1, 12), (1, 13)]
    for i, (xx, yy) in enumerate(limb):
        sp.px(xx, yy, W if i % 3 else w)
        sp.px(xx, yy + 1, w)
    sp.pts([(6, 6), (7, 6), (6, 7)], L)       # the grip
    # the string from tip to tip, drawn back by `pull` pixels
    top, bottom = (14, 2), (2, 14)
    mid = (8 + pull, 8 + pull)
    sp.line(*top, *mid, C, lock=True)
    sp.line(*mid, *bottom, C, lock=True)
    if pull:
        # the nocked arrow
        tip = (mid[0] - 7 - pull, mid[1] - 7 - pull)
        sp.line(mid[0], mid[1], max(0, tip[0] + 1), max(0, tip[1] + 1), w, lock=True)
        sp.px(max(0, tip[0]), max(0, tip[1]), S, lock=True)
        sp.px(mid[0] + 1, mid[1], C, lock=True)
        sp.px(mid[0], mid[1] + 1, C, lock=True)
    sp.outline()
    return sp


bow('short_bow', 0)
bow('short_bow_pulling_0', 1)
bow('short_bow_pulling_1', 2)
bow('short_bow_pulling_2', 3)

# ---------------------------------------------------------------------------------------------------- magic

ascii_icon('prayer_beads', [
    "................",
    "......hGh.......",
    "....Gh...hG.....",
    "...h.......h....",
    "..G.........G...",
    "..h.........h...",
    "..G.........G...",
    "...h.......h....",
    "....Gh...hG.....",
    "......hGh.......",
    ".......G........",
    "......CCC.......",
    ".......C........",
    ".......C........",
    ".......c........",
    "................"]).outline(inner=False)

ascii_icon('ember_glove', [
    "................",
    "......f..F......",
    ".....fF.fe......",
    "....eFf.Ff.e....",
    "....lLlLlLfF....",
    "...lLLlLLlLe....",
    "...lLLLLLLLl.l..",
    "...lLfLLfLLlLl..",
    "...lLLefLLLLLl..",
    "...lLLfFeLLLl...",
    "....lLLeLLLl....",
    "....lLLLLLl.....",
    ".....lllll......",
    ".....bLLLb......",
    ".....bbbbb......",
    "................"], lock='fFe').outline()


def scroll(name, seal, glyph, spent=False):
    """A rolled parchment with a wax seal in the colour of the school and the sign of the spell on it."""
    rows = [
        "................",
        "...kCCCCCCCCk...",
        "..kCcCCCCCCcCk..",
        "..kkcccccccckk..",
        "...CCCCCCCCCC...",
        "...CCCCCCCCCc...",
        "...CCCCCCCCCc...",
        "...CCCCCCCCCc...",
        "...CCCCCCCCCc...",
        "...CCCCCCCCCc...",
        "...CCCCCCCCCc...",
        "...ccccccccck...",
        "..kCcCCCCCCcCk..",
        "..kkcccccccckk..",
        "................",
        "................"]
    sp = ascii_icon(name, rows)
    ink = seal if not spent else k
    for yy, row in enumerate(glyph):
        for xx, ch in enumerate(row):
            if ch == '#':
                sp.px(5 + xx, 5 + yy, ink, lock=True)
            elif ch == '+':
                sp.px(5 + xx, 5 + yy, mix(ink, C, 0.5), lock=True)
    if not spent:
        sp.pts([(11, 11), (12, 11), (11, 12), (12, 12)], seal)
        sp.px(12, 12, shade(seal, 0.7))
    sp.outline(inner=False)
    return sp


GLYPHS = {
    'soul_shard': (a, ["...#..", "..##..", ".###..", "..###.", "..##..", "..#..."]),
    'heavy_soul_shard': (u, [".####.", "######", "######", ".####.", "..##..", "..#..."]),
    'mend': (G, ["..##..", "..##..", "######", "######", "..##..", "..##.."]),
    'rebuke': (g, ["#.##.#", ".#..#.", "#.++.#", "#.++.#", ".#..#.", "#.##.#"]),
    'fire_orb': (f, ["..#...", ".###..", ".####.", "######", "######", ".####."]),
    'flare': (e, ["#....#", ".#..#.", "..##..", ".####.", "##++##", "#.++.#"]),
}
for name, (col, glyph) in GLYPHS.items():
    scroll(name, col, glyph)
    scroll(name + '_spent', col, glyph, spent=True)

# ---------------------------------------------------------------------------------------------------- flask, souls, embers

FLASK = [
    "................",
    "......bWb.......",
    "......bwb.......",
    ".....mMMMm......",
    "......M1M.......",
    "......M1M.......",
    ".....M111M......",
    "....M11211M.....",
    "...M1122211M....",
    "...M1222221M....",
    "...M1222221m....",
    "...M1122211m....",
    "...mM11111mm....",
    "....mMMMMmm.....",
    ".....mmmmm......",
    "................"]
ascii_icon('warmth_flask', FLASK, {'1': f, '2': F}, lock='12').outline(inner=False)
ascii_icon('warmth_flask_empty', FLASK, {'1': (52, 50, 58), '2': (70, 68, 78)}).outline(inner=False)
ascii_icon('flask_shard', [
    "................",
    "................",
    "........M.......",
    ".......MMm......",
    "......MfFM......",
    ".....MffFMm.....",
    "....MMfFFFM.....",
    "....mMfFFMm.....",
    ".....mMFFM......",
    "......mMFMm.....",
    ".......mMm......",
    "........m.......",
    "................",
    "................",
    "................",
    "................"], lock='fF').outline(inner=False)

SOUL = [
    "................",
    ".......B........",
    "......aB........",
    "......aBa.......",
    ".....aBBa..a....",
    ".....aBBBa.B....",
    "....aBBBBaaB....",
    "....aBWWBBBa....",
    "...uaBWWWBBa....",
    "...uaBBWWBBau...",
    "...uaaBBBBBau...",
    "....uaaBBBau....",
    "....uuaaaauu....",
    ".....uuuuuu.....",
    "................",
    "................"]
WHITE = (240, 250, 255)
ascii_icon('nameless_soul', SOUL, {'W': WHITE}, lock='BaWu')
ascii_icon('soul_stain', SOUL, {'W': WHITE, 'B': (196, 255, 226), 'a': (120, 226, 180), 'u': (60, 150, 130)}, lock='BaWu')
ascii_icon('great_soul', [
    ".......B........",
    "......aBa..a....",
    "..a...aBBa.B....",
    "..B..aBBBaaB....",
    "..Ba.aBBBBBa....",
    ".aBBaBBWWBBa.a..",
    ".aBBBBWWWWBBaB..",
    ".aBBBWWWWWBBBa..",
    "uaBBBWWWWWBBBau.",
    "uaBBBBWWWBBBBau.",
    "uaaBBBBBBBBBaau.",
    ".uaaBBBBBBBaau..",
    ".uuaaaBBBaaauu..",
    "..uuuaaaaauuu...",
    "....uuuuuuu.....",
    "................"], {'W': WHITE}, lock='BaWu')
ascii_icon('overseer_soul', [
    ".......F........",
    "......fFf..f....",
    "..f...fFFf.F....",
    "..F..fFFFffF....",
    "..Ff.fFFFFFf....",
    ".fFFfFMMMMMf.f..",
    ".fFFFMM.M.MMfF..",
    ".fFFFM.FMF.MFf..",
    "efFFFMMMMMMMFfe.",
    "efFFFM.M.M.MFfe.",
    "effFFM.M.M.Mffe.",
    ".effFMMMMMMMfe..",
    ".eeffFFFFFFfee..",
    "..eeeffffeeee...",
    "....eeeeeee.....",
    "................"], lock='FfeM')
ascii_icon('chaplain_soul', [
    ".......P........",
    "......pPp..p....",
    "..p...pPPp.P....",
    "..P..pPPPppP....",
    "..Pp.pPPGPPp....",
    ".pPPpPPPGPPp.p..",
    ".pPPPPGGGGGPpP..",
    ".pPPPPPPGPPPPp..",
    "xpPPPPPPGPPPPpx.",
    "xpPPPPPPGPPPPpx.",
    "xppPPPPPGPPPppx.",
    ".xppPPPPPPPppx..",
    ".xxpppPPPpppxx..",
    "..xxxpppppxxx...",
    "....xxxxxxx.....",
    "................"], lock='PpGx')
ascii_icon('return_ember', [
    "................",
    "................",
    ".......f........",
    "......fF.e......",
    ".....eFFf.......",
    ".....fFFFe......",
    "....mMfFfMm.....",
    "...mMefFfeMm....",
    "...mMMefeMMm....",
    "...mmMeeMMmm....",
    "....mmMMMmm.....",
    ".....mmmmm......",
    "................",
    "................",
    "................",
    "................"], lock='fFe').outline(inner=False)
ascii_icon('tar_pot', [
    "................",
    ".........f......",
    "........fF......",
    ".......cf.......",
    "......kCk.......",
    ".....qRRRq......",
    "....qRrrrRq.....",
    "...qRRRRRRRq....",
    "...qRmmmmmRq....",
    "...qRmMMmmRq....",
    "...qRRmmmRRq....",
    "...qrRRRRRrq....",
    "....qrrrrrq.....",
    ".....qqqqq......",
    "................",
    "................"], lock='fF').outline(inner=False)

# ---------------------------------------------------------------------------------------------------- flying spells

def bolt(name, core, mid, edge, rows):
    ascii_icon(name, rows, {'1': core, '2': mid, '3': edge}, lock='123')


bolt('shard_bolt', WHITE, B, a, [
    "................", "................", "................", "................",
    ".......3........", "......323.......", ".....32123......", "....3211123.....",
    ".....32123......", "......323.......", ".......3........", "................",
    "................", "................", "................", "................"])
bolt('heavy_shard_bolt', WHITE, a, u, [
    "................", "................", ".......3........", "......323.......",
    ".....32223......", "....3221223.....", "...322111223....", "..32211111223...",
    "...322111223....", "....3221223.....", ".....32223......", "......323.......",
    ".......3........", "................", "................", "................"])
bolt('fire_orb_bolt', F, f, e, [
    "................", "................", "......3..3......", ".....33333......",
    "....3322233.....", "...332222233....", "...322111223....", "..3322111223....",
    "...322111223....", "...332222233....", "....3322233.....", ".....33333......",
    "......3.........", "................", "................", "................"])
bolt('light_spear_bolt', WHITE, F, G, [
    "................", "..............3.", ".............32.", "............321.",
    "...........321..", "..........321...", ".........321....", "........321.....",
    ".......321......", "......321.......", ".....321........", "....321.........",
    "...321..........", "..32............", ".3..............", "................"])
bolt('dark_flame_bolt', P, p, (60, 34, 96), [
    "................", "................", "......3..3......", ".....33.33......",
    "....3322233.....", "...332222233....", "...322111223....", "..3322111223....",
    "...322111223....", "...332222233....", "....3322233.....", ".....33333......",
    ".......3........", "................", "................", "................"])

# ---------------------------------------------------------------------------------------------------- spawn eggs

EGG = [
    "................",
    "......1111......",
    ".....111111.....",
    "....11111111....",
    "....11111111....",
    "...1111111111...",
    "...1111111111...",
    "...1111111111...",
    "..111111111111..",
    "..111111111111..",
    "..111111111111..",
    "..111111111111..",
    "...1111111111...",
    "...1111111111...",
    "....11111111....",
    "......1111......"]


def egg(name, base, spots, marks=()):
    """An egg in the colour of the mob with its spots; `marks` are a few pixels that hint at the mob itself."""
    sp = icon(name + '_spawn_egg')
    for yy, row in enumerate(EGG):
        for xx, ch in enumerate(row):
            if ch == '1':
                light = 1.12 - 0.035 * yy - 0.02 * xx + 0.2
                sp.px(xx, yy, shade(base, light))
    for (xx, yy) in ((5, 4), (9, 3), (10, 7), (4, 9), (7, 10), (11, 11), (6, 13), (8, 6), (9, 12), (3, 7)):
        if sp.solid(xx, yy):
            sp.px(xx, yy, spots)
            if sp.solid(xx + 1, yy):
                sp.px(xx + 1, yy, shade(spots, 0.85))
    for (xx, yy, col) in marks:
        sp.px(xx, yy, col, lock=True)
    sp.outline(0.55, inner=False)
    return sp


EMB = (255, 150, 44)
egg('withered_prisoner', (104, 84, 68), (132, 118, 90), ((6, 8, EMB), (9, 8, EMB)))
egg('withered_guard', (98, 97, 102), (112, 44, 40), ((6, 8, EMB), (9, 8, EMB)))
egg('withered_archer', (62, 72, 52), (94, 62, 38), ((6, 8, EMB), (9, 8, EMB)))
egg('rust_knight', (84, 76, 76), (150, 90, 52), ((5, 8, O), (6, 8, EMB), (7, 8, O), (8, 8, O), (9, 8, EMB), (10, 8, O)))
egg('keykeeper', (72, 76, 88), (196, 160, 72), ((5, 8, C), (6, 8, C), (7, 8, C), (8, 8, C), (9, 8, C), (10, 8, C)))
egg('overseer', (150, 138, 132), (82, 56, 40), ((5, 6, M), (7, 6, M), (9, 6, M), (5, 8, M), (7, 8, F), (9, 8, M), (5, 10, M), (7, 10, M), (9, 10, M),
                                               (6, 6, M), (8, 6, M), (10, 6, M), (10, 8, M), (10, 10, M), (6, 10, M), (8, 10, M)))
egg('chaplain', (60, 44, 56), (196, 160, 72), ((7, 5, G), (7, 6, G), (7, 7, G), (7, 8, G), (7, 9, G), (6, 6, G), (8, 6, G)))

# ---------------------------------------------------------------------------------------------------- write everything

def write_json(path, data):
    os.makedirs(os.path.dirname(path), exist_ok=True)
    json.dump(data, open(path, 'w'), indent=2)
    open(path, 'a').write('\n')


def simple_definition(name, model=None):
    write_json(f'{A}/items/{name}.json', {'model': {'type': 'minecraft:model', 'model': f'{MOD}:item/{model or name}'}})


BIG_DISPLAY = {
    'thirdperson_righthand': {'rotation': [0, -90, 55], 'translation': [0, 9.5, 1.5], 'scale': [1.7, 1.7, 0.85]},
    'thirdperson_lefthand': {'rotation': [0, 90, -55], 'translation': [0, 9.5, 1.5], 'scale': [1.7, 1.7, 0.85]},
    'firstperson_righthand': {'rotation': [0, -90, 25], 'translation': [1.13, 6.5, 1.13], 'scale': [1.3, 1.3, 0.68]},
    'firstperson_lefthand': {'rotation': [0, 90, -25], 'translation': [1.13, 6.5, 1.13], 'scale': [1.3, 1.3, 0.68]},
}

if __name__ == '__main__':
    os.makedirs(f'{A}/textures/item', exist_ok=True)
    for name, sp in ICONS.items():
        sp.save(f'{A}/textures/item/{name}.png')
        model = {'parent': 'minecraft:item/handheld' if name in HANDHELD or name in BIG else 'minecraft:item/generated',
                 'textures': {'layer0': f'{MOD}:item/{name}'}}
        if name in BIG:
            model['display'] = BIG_DISPLAY
        if name.startswith('short_bow'):
            model['parent'] = 'minecraft:item/bow'
        write_json(f'{A}/models/item/{name}.json', model)
    plain = [n for n in ICONS if not n.endswith(('_spent', '_empty')) and not n.startswith('short_bow') and n != 'warmth_flask' and n not in GLYPHS]
    for name in plain:
        simple_definition(name)
    # items that look different when used up: the last point of wear is never spent, so "broken" means empty
    for name, empty in [('warmth_flask', 'warmth_flask_empty')] + [(n, n + '_spent') for n in GLYPHS]:
        write_json(f'{A}/items/{name}.json', {'model': {
            'type': 'minecraft:condition', 'property': 'minecraft:broken',
            'on_true': {'type': 'minecraft:model', 'model': f'{MOD}:item/{empty}'},
            'on_false': {'type': 'minecraft:model', 'model': f'{MOD}:item/{name}'}}})
    # the bow, drawn half again as fast as the vanilla one
    write_json(f'{A}/items/short_bow.json', {'model': {
        'type': 'minecraft:condition', 'property': 'minecraft:using_item',
        'on_false': {'type': 'minecraft:model', 'model': f'{MOD}:item/short_bow'},
        'on_true': {'type': 'minecraft:range_dispatch', 'property': 'minecraft:use_duration', 'scale': 0.075,
                    'entries': [{'threshold': 0.65, 'model': {'type': 'minecraft:model', 'model': f'{MOD}:item/short_bow_pulling_1'}},
                                {'threshold': 0.9, 'model': {'type': 'minecraft:model', 'model': f'{MOD}:item/short_bow_pulling_2'}}],
                    'fallback': {'type': 'minecraft:model', 'model': f'{MOD}:item/short_bow_pulling_0'}}}})
    if len(sys.argv) > 1:
        names = list(ICONS)
        cell = 16 * 6 + 8
        cols = 10
        rows = (len(names) + cols - 1) // cols
        out = Image.new('RGB', (cols * cell + 8, rows * cell + 8), (198, 198, 198))
        for i, name in enumerate(names):
            im = ICONS[name].img
            scale = 6 if im.width == 16 else 3
            big = im.resize((im.width * scale, im.height * scale), 0)
            # the slot colour of the inventory behind every icon
            tile = Image.new('RGBA', (96, 96), (139, 139, 139, 255))
            tile.alpha_composite(big)
            out.paste(tile.convert('RGB'), (8 + (i % cols) * cell, 8 + (i // cols) * cell))
        out.save(sys.argv[1])
