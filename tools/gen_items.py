#!/usr/bin/env python3
"""Item icons, item models and item definitions. Run from the repo root; pass a file name to also get a preview sheet."""
import json
import math
import os
import sys

from PIL import Image

from sprite import Sprite, diagonal_blade, hilt, mix, shade

A = 'src/main/resources/assets/unfading_dawn'
MOD = 'unfading_dawn'

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
    'cleaver': [
        "................",
        "................",
        "........oooo....",
        ".......oSSSSo...",
        "......oSSsssNo..",
        ".....oSSsssdno..",
        "....oSSsssddo...",
        "....oSsssddo....",
        ".....osddoo.....",
        "....oWooo.......",
        "...oWwo.........",
        "..oWwo..........",
        ".oLwo...........",
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
    # a stonecutter's hammer: a square head of iron, one face flat for the chisel, the other drawn to an edge
    'stone_hammer': [
        "......ooo.......",
        ".....oSSso......",
        "....oSSssdo.....",
        "....oSssddDo....",
        ".....osssddDo...",
        "......osMddDDo..",
        ".......oWdddDDo.",
        "......oWwodDDo..",
        ".....oWwo.oDo...",
        "....oWwo...o....",
        "...oWwo.........",
        "..oWwo..........",
        ".oLwo...........",
        "oLlo............",
        ".oo.............",
        "................"],
    # a fisher's knife: a long narrow blade ground thin, a grip of bone with a ring for the cord
    'fish_knife': [
        "................",
        "................",
        "............oo..",
        "...........oSo..",
        "..........oSso..",
        ".........oSsdo..",
        "........oSsdo...",
        ".......oSsdo....",
        "......oSsdo.....",
        ".....oosdo......",
        "....oCcoo.......",
        "...oCcko........",
        "..oCcko.........",
        ".oMcko..........",
        ".oMoo...........",
        "..o............."],
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

# the pitchfork: a shaft as long as a man, a bar across its end and three tines; held low like a pike
sp = icon('pitchfork', 32, big=True)
for i in range(20):
    xx, yy = 2 + i, 29 - i
    sp.px(xx, yy, W if i % 6 else w)
    sp.px(xx + 1, yy, w)
    sp.px(xx + 1, yy + 1, b)
for kk in range(-4, 5):                                  # the bar, across the shaft
    sp.px(22 + kk, 9 + kk, s if kk % 3 else S)
    sp.px(22 + kk, 10 + kk, d)
for off in (-4, 0, 4):                                   # the tines, along it
    for i in range(1, 8 if off else 9):
        xx, yy = 22 + off + i, 9 + off - i
        if 0 <= xx < 32 and 0 <= yy < 32:
            sp.px(xx, yy, S if i > 5 else s)
            if xx + 1 < 32 and i < 6:
                sp.px(xx + 1, yy, d)
sp.pts([(21, 10), (22, 11), (20, 11)], R)
sp.outline(0.45, inner=False)

sp = icon('grave_spade', 32, big=True)
# the gravedigger's spade: a long worn shaft with a crossbar for the hands, a wide blade eaten by rust
for i in range(17):
    xx, yy = 5 + i, 26 - i
    sp.px(xx, yy, W if i % 5 else w)
    sp.px(xx + 1, yy, w)
    sp.px(xx + 1, yy + 1, b)
for kk in range(-3, 4):
    sp.px(4 + kk, 27 + kk, W if kk % 2 else w)
    sp.px(5 + kk, 27 + kk, b)
for uu in range(9):
    half = 4 if uu < 5 else 8 - uu
    for vv in range(-half, half + 1):
        xx, yy = 20 + uu + vv, 11 - uu + vv
        if 0 <= xx < 32 and 0 <= yy < 32:
            col = S if vv == -half else (d if vv == half else s)
            if (uu * 5 + vv * 3) % 11 == 0:
                col = R
            if (uu * 3 + vv * 7) % 13 == 0:
                col = r
            sp.px(xx, yy, col)
            if vv < half and xx + 1 < 32:
                sp.px(xx + 1, yy, col if col in (s, R, r) else s)
sp.pts([(20, 11), (21, 12), (22, 11), (21, 10)], D)
sp.outline(0.45, inner=False)

# the quarry's pick: a long haft and a bar of iron across its end drawn out to a point either way
sp = icon('quarry_pick', 32, big=True)
for i in range(19):
    xx, yy = 4 + i, 27 - i
    sp.px(xx, yy, W if i % 5 else w)
    sp.px(xx + 1, yy, w)
    sp.px(xx + 1, yy + 1, b)
for kk in range(-9, 10):
    bend = (abs(kk) * abs(kk)) // 22                     # the points droop towards the hands
    xx, yy = 22 + kk - bend, 9 + kk + bend
    thick = 2 if abs(kk) < 6 else (1 if abs(kk) < 9 else 0)
    for tt in range(-1, thick):
        col = S if tt == thick - 1 else (d if tt == -1 else s)
        if 0 <= xx + tt < 31 and 0 <= yy - tt < 32:
            sp.px(xx + tt + 1, yy - tt, d if tt == -1 else s)
            sp.px(xx + tt, yy - tt, col)
    if (kk * 7) % 11 == 0 and abs(kk) < 8:
        sp.px(xx, yy, R)
sp.pts([(21, 9), (22, 10), (23, 9), (22, 8), (21, 10), (23, 8)], D)
sp.outline(0.45, inner=False)

# the Taskmaster's maul: a block of iron on a haft as long as a man
sp = icon('great_maul', 32, big=True)
for i in range(19):
    xx, yy = 3 + i, 28 - i
    sp.px(xx, yy, W if i % 6 else w)
    sp.px(xx + 1, yy, w)
    sp.px(xx + 1, yy + 1, b)
for i in range(4):                                       # the grip, wound with rag
    sp.px(4 + i * 2, 27 - i * 2, C)
    sp.px(5 + i * 2, 27 - i * 2, c)
for uu in range(-3, 5):                                  # along the haft
    for vv in range(-7, 8):                              # across it
        xx, yy = 23 + uu + vv, 9 - uu + vv
        if 0 <= xx < 31 and 0 <= yy < 32:
            col = S if uu == 4 or vv == -7 else (D if uu == -3 or vv == 7 else (d if (uu + vv * 2) % 5 == 0 else s))
            if abs(vv) >= 6:
                col = M if col == s else col             # the struck faces, darker
            if (uu * 5 + vv * 3) % 13 == 0:
                col = r
            sp.px(xx, yy, col)
            sp.px(xx + 1, yy, col if col != S else s)
sp.outline(0.45, inner=False)

# the ferrymen's boat hook: a pole as long as a pike, an iron spike at its end and a hook turned back beside it
sp = icon('boat_hook', 32, big=True)
for i in range(22):
    xx, yy = 2 + i, 29 - i
    sp.px(xx, yy, W if i % 7 else w)
    sp.px(xx + 1, yy, w)
    sp.px(xx + 1, yy + 1, b)
for i in range(3):                                       # the socket, green with the water
    sp.px(21 + i, 10 - i, (88, 128, 92))
    sp.px(22 + i, 10 - i, (56, 86, 66))
for i in range(6):                                       # the spike
    sp.px(24 + i, 7 - i, S if i > 2 else s)
    if i < 4:
        sp.px(25 + i, 7 - i, d)
for xx, yy, col in ((25, 8, s), (26, 9, s), (27, 10, s), (28, 10, d), (29, 9, s), (29, 8, S), (28, 7, S), (27, 11, d), (28, 11, D)):
    sp.px(xx, yy, col)                                   # the hook, out to the side and back
sp.pts([(23, 9), (24, 9)], R)
sp.pts([(6, 26), (8, 24), (10, 22)], C)                  # a turn of cord where the hands go
sp.outline(0.45, inner=False)

# ---- the Black Firwood
WAXL, WAXM, WAXD = (244, 236, 204), (214, 200, 156), (164, 146, 104)
LINL, LINM, LIND = (236, 232, 220), (198, 192, 176), (150, 144, 130)
THREAD = (150, 40, 36)

# the staff of a wakeful one: a peeled pole, and on its end the candle he keeps his eyes open by
sp = icon('vigil_staff', 32, big=True)
for i in range(21):
    xx, yy = 3 + i, 28 - i
    sp.px(xx, yy, C if i % 6 else c)
    sp.px(xx + 1, yy, c)
    sp.px(xx + 1, yy + 1, k)
for i in range(3):                                       # a turn of linen where the hands go
    sp.px(8 + i * 2, 23 - i * 2, LINL)
    sp.px(9 + i * 2, 23 - i * 2, LINM)
for i in range(3):                                       # an iron cup the candle stands in
    sp.px(22 + i, 8 + i, M)
    sp.px(23 + i, 7 + i, m)
for i in range(4):                                       # the candle, leaning the way the pole does
    for kk, col in enumerate((WAXL, WAXM, WAXD)):
        sp.px(23 + i + kk, 6 - i, col)
sp.pts([(22, 7), (24, 9)], WAXM)                         # a run of wax down the cup
sp.pts([(28, 2)], b, lock=True)
sp.pts([(29, 0), (30, 0)], F, lock=True)
sp.pts([(29, 1), (28, 1)], f, lock=True)
sp.pts([(30, 1)], e, lock=True)
sp.outline(0.45, inner=False)

# the spear the bear was kept awake with: a stout shaft, a cross-bar so that he could not come up it, a blade like a leaf
sp = icon('bear_spear', 32, big=True)
for i in range(21):
    xx, yy = 2 + i, 29 - i
    sp.px(xx, yy, W if i % 6 else w)
    sp.px(xx + 1, yy, w)
    sp.px(xx + 1, yy + 1, b)
for i in range(4):                                       # strips of his hide, wound where the hands go
    sp.px(6 + i * 2, 25 - i * 2, (122, 96, 70))
    sp.px(7 + i * 2, 25 - i * 2, (84, 62, 46))
for kk in range(-3, 4):                                  # the cross-bar
    sp.px(21 + kk, 11 + kk, s if kk % 2 else S)
    sp.px(21 + kk, 12 + kk, d)
for uu, half in enumerate((1, 2, 2, 2, 2, 1, 1, 0, 0)):   # the blade: along it, and across
    for vv in range(-half, half + 1):
        xx, yy = 23 + uu + vv, 8 - uu + vv
        sp.px(xx, yy, S if vv < 0 else (s if vv == 0 else d))
        if xx + 1 < 32:
            sp.px(xx + 1, yy, s if vv < 0 else (d if vv < half else D))
sp.pts([(22, 9), (23, 9), (22, 10)], D)
sp.pts([(26, 6), (28, 4)], N)                            # what has not been washed off it
sp.outline(0.45, inner=False)

# the Elder's great candle: an iron pole, a pan for what runs down, and a candle as thick as an arm, alight
sp = icon('great_candle', 32, big=True)
for i in range(19):
    xx, yy = 2 + i, 29 - i
    sp.px(xx, yy, M if i % 5 else d)
    sp.px(xx + 1, yy, m)
    sp.px(xx + 1, yy + 1, O)
for i in range(3):
    sp.px(5 + i * 3, 26 - i * 3, D)
for kk in range(-4, 5):                                  # the pan
    sp.px(20 + kk, 11 + kk, s if abs(kk) < 4 else d)
    sp.px(20 + kk, 12 + kk, D)
for uu in range(8):                                      # the candle: along it, and across
    for vv in range(-1, 2):
        xx, yy = 22 + uu + vv, 9 - uu + vv
        col = WAXL if vv < 0 else (WAXM if vv == 0 else WAXD)
        if col == WAXL and uu % 3 == 1:
            col = WAXM
        sp.px(xx, yy, col)
        sp.px(xx + 1, yy, WAXM if vv < 0 else WAXD)
for xx, yy in ((19, 10), (18, 11), (25, 11), (26, 12), (26, 13), (17, 12)):   # what has run down and set
    sp.px(xx, yy, WAXM)
for xx, yy, col in ((30, 1, b), (30, 0, f), (31, 0, F), (31, 1, e)):
    sp.px(xx, yy, col, lock=True)
sp.outline(0.45, inner=False)

# ---- the Morning Wharf
TAR, TARD = (58, 48, 44), (36, 30, 30)
CANVAS, CANVASM, CANVASD = (214, 202, 170), (176, 162, 130), (128, 116, 92)
SHELL, SHELLM, SHELLD = (190, 88, 60), (142, 58, 44), (92, 38, 34)

# an oar of a karbas: ash, as long as the boat is wide twice over, the blade worn pale and split at the tip
sp = icon('oar', 32, big=True)
for i in range(18):
    xx, yy = 2 + i, 29 - i
    sp.px(xx, yy, W if i % 6 else w)
    sp.px(xx + 1, yy, w)
    sp.px(xx + 1, yy + 1, b)
for i in range(3):                                       # the leather where it lay in the thole
    sp.px(11 + i, 20 - i, R)
    sp.px(12 + i, 20 - i, r)
    sp.px(12 + i, 21 - i, q)
for uu, half in enumerate((1, 2, 2, 3, 3, 3, 3, 3, 3, 2, 1)):   # the blade: along it, and across
    for vv in range(-half, half + 1):
        xx, yy = 20 + uu + vv, 11 - uu + vv
        col = C if vv < -1 else (c if vv < 1 else (W if vv < half else w))
        if uu > 7 and vv == 0:
            col = b                                      # the split
        sp.px(xx, yy, col)
        if xx + 1 < 32:
            sp.px(xx + 1, yy, c if vv < 0 else (w if vv < half else b))
sp.pts([(4, 27), (6, 25)], C)                            # where the hands have polished it
sp.outline(0.45, inner=False)

# a scythe: a long bent snath with its two grips, and the blade out at a right angle from its end
sp = icon('scythe', 32, big=True)
for i in range(21):
    xx, yy = 3 + i, 29 - i + (1 if 8 <= i <= 13 else 0)      # (the snath is not straight: it is bent to the mower)
    sp.px(xx, yy, W if i % 5 else w)
    sp.px(xx + 1, yy, w)
    sp.px(xx + 1, yy + 1, b)
for gx, gy in ((7, 24), (13, 18)):                           # the grips
    sp.pts([(gx - 1, gy - 1), (gx - 2, gy - 2)], w)
    sp.px(gx - 2, gy - 1, b)
for i in range(17):                                         # the blade: from the heel at the snath's end, out and down to the point
    xx = 24 - i
    yy = 8 - int(round(5.0 * math.sin(i / 16.0 * math.pi * 0.62)))
    wide = 3 if i < 5 else (2 if i < 12 else 1)
    for tt in range(wide):
        sp.px(xx, yy + tt, S if tt == 0 else (s if tt < wide - 1 or wide == 1 else d))
sp.pts([(24, 9), (25, 9), (25, 8)], M)                       # the ring that holds it
sp.outline(0.45, inner=False)

# the Warden's pole: a long pole burnt black, alight in its splits, with the rake that was lashed to its end
sp = icon('warden_pole', 32, big=True)
CHARRED, CHARRED_D = (58, 46, 42), (34, 26, 26)
for i in range(22):
    xx, yy = 3 + i, 29 - i
    hot = i % 5 == 2
    sp.px(xx, yy, f if hot else CHARRED)
    sp.px(xx + 1, yy, F if hot and i % 2 == 0 else CHARRED_D)
    sp.px(xx + 1, yy + 1, CHARRED_D)
for tt in range(-4, 5):                                      # the head of the rake, across the end of it
    sp.px(25 + tt, 7 + tt, CHARRED if tt % 2 else w)
    sp.px(26 + tt, 7 + tt, CHARRED_D)
for tt in (-4, -2, 0, 2, 4):                                 # and its teeth
    sp.pts([(26 + tt, 6 + tt), (27 + tt, 5 + tt), (28 + tt, 4 + tt)], M if tt % 4 else m)
sp.pts([(22, 11), (23, 10), (23, 11)], R)                    # the lashing
sp.outline(0.45, inner=False)

# a harpoon: a long shaft, an iron foreshaft, a head with one barb turned back, and a becket for the line
sp = icon('harpoon', 32, big=True)
for i in range(19):
    xx, yy = 2 + i, 29 - i
    sp.px(xx, yy, W if i % 7 else w)
    sp.px(xx + 1, yy, w)
    sp.px(xx + 1, yy + 1, b)
for i in range(3):                                       # turns of tarred line where the iron is seized on
    sp.px(19 + i, 12 - i, TAR)
    sp.px(20 + i, 12 - i, TARD)
for i in range(6):                                       # the foreshaft
    sp.px(22 + i, 9 - i, s if i % 2 else S)
    sp.px(23 + i, 9 - i, d)
for xx, yy, col in ((28, 3, S), (29, 2, S), (30, 1, S), (29, 3, s), (30, 2, s), (31, 0, S), (30, 3, d), (31, 1, s),
                    (28, 5, s), (28, 6, d), (29, 6, d), (29, 5, s), (30, 4, d)):   # the head, and the barb back from it
    sp.px(xx, yy, col)
sp.pts([(18, 14), (17, 16), (18, 16), (19, 15)], CANVASM)   # the becket
sp.pts([(24, 8), (26, 6)], R)
sp.outline(0.45, inner=False)

# the anchor of the Daybreak: carried by its ring, the stock across under the hands, the arms and their flukes at the far end
sp = icon('ship_anchor', 32, big=True)


def anchor_px(au, av, col):
    """A point of the anchor: au along the shank from the ring, av across it (to the lower right)."""
    xx, yy = 4 + au + av, 27 - au + av
    if not sp.solid(xx, yy + 1):
        sp.px(xx, yy + 1, shade(col, 0.62))
    sp.px(xx, yy, col)
    sp.px(xx + 1, yy, shade(col, 0.8))


for au in range(1, 18):                                  # the shank
    anchor_px(au, 0, s if au % 5 else d)
    anchor_px(au, 1, M)
for av in range(-5, 6):                                  # the stock: oak, banded with iron
    anchor_px(3, av, M if abs(av) in (2, 5) else (W if av < 0 else w))
ARMS = ((17, 1), (17, 2), (16, 3), (16, 4), (15, 5), (14, 6), (13, 7), (12, 7))   # out from the crown and back towards the ring
for au, av in ARMS:
    anchor_px(au, -av, s)
    anchor_px(au - 1, -av, d)
    anchor_px(au, av, d)
    anchor_px(au - 1, av, M)
for au, av, col in ((11, 7, s), (11, 6, s), (12, 6, S), (12, 8, d), (11, 8, d), (10, 7, d), (13, 8, d)):   # the flukes
    anchor_px(au, -av, col)
    anchor_px(au, av, shade(col, 0.75))
anchor_px(18, 0, s)                                      # the crown
anchor_px(18, 1, d)
for xx, yy, col in ((4, 28, d), (3, 28, s), (2, 29, s), (2, 30, d), (3, 31, d), (4, 31, M), (5, 30, M), (5, 29, d)):   # the ring
    sp.px(xx, yy, col)
sp.pts([pt for pt in ((11, 21), (16, 16), (20, 12), (26, 15), (18, 5)) if sp.solid(*pt)], R)   # rust
sp.pts([(13, 19), (19, 13)], (88, 128, 92))                    # and weed
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
    'soul_scatter': (a, ["#.#.#.", "#.#.#.", ".###..", ".###..", "..#...", "..#..."]),
    'root_snare': (v, ["..#...", ".##.#.", "..###.", ".##...", "#.##.#", "######"]),
    'marsh_fire': ((70, 150, 70), ["...#..", "..##..", ".##+#.", ".#++#.", ".#++#.", "..##.."]),
    'hot_wax': ((176, 130, 60), [".####.", ".####.", "..##..", ".#.#..", ".#..#.", "....#."]),
    'dry_wind': ((206, 184, 120), ["###.#.", "....#.", "#####.", "......", "####.#", "....##"]),
    'sun_patch': ((255, 206, 90), ["#.##.#", ".#++#.", "#++++#", "#++++#", ".#++#.", "#.##.#"]),
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
ascii_icon('gravedigger_soul', [
    ".......V........",
    "......vVv..v....",
    "..v...vVVv.V....",
    "..V..vVVVvvV....",
    "..Vv.vVVVVVv....",
    ".vVVvVVCCCVv.v..",
    ".vVVVVCCCCCVvV..",
    ".vVVVCCkCkCCVv..",
    "kvVVVCCCCCCCVvk.",
    "kvVVVVCCkCCVVvk.",
    "kvvVVVCkCkCVvvk.",
    ".kvvVVVCCCVVvk..",
    ".kkvvVVVVVVvkk..",
    "..kkkvvvvkkkk...",
    "....kkkkkkk.....",
    "................"], lock='VvCk')
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

ascii_icon('eater_claw', [
    "...........oo...",
    "..........oCCo..",
    ".........oCCco..",
    "........oCCcko..",
    ".......oCCcko...",
    "......oCCcko....",
    ".....oCCcko.....",
    "....oCCcko......",
    "....oCcko.......",
    "...oLCko........",
    "..oLlLo.........",
    ".oLlLo..........",
    "oWlLo...........",
    "oWwo............",
    ".oo.............",
    "................"], handheld=True)
# what is made of the Wallower: one tooth of it, on a grip of its own hide
ascii_icon('wallower_fang', [
    "............oo..",
    "...........o1o..",
    "..........o12o..",
    ".........o112o..",
    "........o1123o..",
    ".......o11223o..",
    "......o11223o...",
    ".....o112233o...",
    "....o1122v3o....",
    "....o122vVo.....",
    "...oLvvVvo......",
    "..oLlvVo........",
    ".oLlLoo.........",
    "oWlLo...........",
    "oWwo............",
    ".oo............."], {'1': (232, 226, 190), '2': (196, 186, 140), '3': (142, 134, 96)}, handheld=True)
# what is made of the Weeping Mother: a scourge of her own withes, plaited, with the leaves still on the ends
ascii_icon('withe_scourge', [
    ".....ooooo......",
    "....oVvVvVoo....",
    "...oVo...ovVo...",
    "...ovo....oVvo..",
    "...oVo.....ovo..",
    "....oVo....oVo..",
    ".....ovoo.oVvo..",
    "......oVvoVvo...",
    ".......ooVvo....",
    "........oVvo.1o.",
    ".......oLVo.o1o.",
    "......oLlo..o1..",
    ".....oLlo.......",
    "....oWlo........",
    "...oWwo.........",
    "....oo.........."], {'1': (150, 190, 96)}, handheld=True)
ascii_icon('bone_scale', [
    "................",
    "................",
    "......oooo......",
    "....oo1111oo....",
    "...o11111122o...",
    "..o1111112222o..",
    "..o1112112223o..",
    "..o1121122233o..",
    "..o1211222233o..",
    "..o2112222333o..",
    "...o22223333o...",
    "...o2223333vo...",
    "....o23333vo....",
    ".....o333vo.....",
    "......oooo......",
    "................"], {'1': (214, 216, 196), '2': (168, 176, 152), '3': (116, 128, 110)})
ascii_icon('withe', [
    "................",
    ".............oo.",
    "...........ooVo.",
    "..........oVvo..",
    ".....oo..oVvo...",
    "....o1o.oVvo.oo.",
    "....o11oVvo.oVo.",
    ".....ooVvooVvo..",
    "......oVvoVvo...",
    ".....oVvCcvo....",
    "....oVvoCcoo1o..",
    "...oVvoVvo.o11o.",
    "..oVvooVo...oo..",
    ".oVvo.oo........",
    ".ovo............",
    "..o............."], {'1': (150, 190, 96)})
ascii_icon('mud_glob', [
    "................",
    "................",
    "................",
    ".......22.......",
    ".....223322.....",
    "....23333332....",
    "...2333113332...",
    "...2331133332...",
    "..233313333332..",
    "..233333333332..",
    "...2333333v32...",
    "...233v333332...",
    "....23333332....",
    ".....222222.....",
    "................",
    "................"], {'1': (124, 132, 92), '2': (38, 44, 32), '3': (70, 78, 52)}, lock='123v')
ascii_icon('iron_wedge', [
    "................",
    "................",
    ".....oooooo.....",
    "....oSSSSSSo....",
    "....oDddddDo....",
    "....osssssdo....",
    ".....osssdo.....",
    ".....osssdo.....",
    ".....osssdo.....",
    "......ossdo.....",
    "......osRdo.....",
    "......osdo......",
    ".......odo......",
    ".......oo.......",
    "................",
    "................"])
ascii_icon('powder_pot', [
    "................",
    "..........F.....",
    ".........fC.....",
    "........cc......",
    ".......kCk......",
    ".....oMMMMo.....",
    "....oMmmmmMo....",
    "...oMMMMMMMMo...",
    "...oMCCCCCCMo...",
    "...oMCmCCmCMo...",
    "...oMCCmmCCMo...",
    "...oMMMMMMMMo...",
    "....oMmmmmMo....",
    ".....oooooo.....",
    "................",
    "................"], lock='Ff').outline(inner=False)
ascii_icon('quarry_key', [
    "................",
    "..........ooo...",
    ".........oSsdo..",
    "........oSo.odo.",
    "........oso.odo.",
    "........oSdodo..",
    ".......oSsddo...",
    "......oSsdoo....",
    ".....oSsdo......",
    "....oSsdo.......",
    "...oSsdoo.......",
    "..oSsdoso.......",
    ".oSsdosdo.......",
    ".osdo.oo........",
    "..oo............",
    "................"])
# the key of the Mire Gate: the same make, gone green in the water, a thread of weed still on it
ascii_icon('mire_key', [
    "................",
    "..........ooo...",
    ".........oSsdo..",
    "........oSo.odo.",
    "........oso.odo.",
    "........oSdodo..",
    ".......oSsddo...",
    "......oSsdoo....",
    ".....oSsdo......",
    "....oSsdo.wo....",
    "...oSsdoo.wo....",
    "..oSsdosowo.....",
    ".oSsdosdowo.....",
    ".osdo.oo.o......",
    "..oo............",
    "................"], extra={'S': (126, 168, 120), 's': (88, 128, 92), 'd': (56, 86, 66), 'w': (66, 110, 52)})
ascii_icon('taskmaster_soul', [
    ".......C........",
    "......cCc..c....",
    "..c...cCCc.C....",
    "..C..cCCCccC....",
    "..Cc.cCCCCCc....",
    ".cCCcCCMMMCc.c..",
    ".cCCCCMMMMMCcC..",
    ".cCCCMMFMFMMCc..",
    "kcCCCMMMMMMMCck.",
    "kcCCCCMMmMMCCck.",
    "kccCCCMmMmMCcck.",
    ".kccCCCMMMCCck..",
    ".kkccCCCCCCckk..",
    "..kkkcccckkkk...",
    "....kkkkkkk.....",
    "................"], lock='CcMFk')
ascii_icon('stone_eater_soul', [
    ".......1........",
    "......212..2....",
    "..2...2112.1....",
    "..1..2111221....",
    "..12.2111112....",
    ".211211ddd12.2..",
    ".21111ddddd121..",
    ".2111ddMdMdd12..",
    "D2111ddddddd12D.",
    "D21111ddMdd112D.",
    "D221111MdMd122D.",
    ".D22111ddd112D..",
    ".DD2211111122DD.",
    "..DDD2222DDDD...",
    "....DDDDDDD.....",
    "................"], {'1': (150, 240, 214), '2': (84, 170, 150)}, lock='12dMD')

SOUL_ROWS = [
    ".......1........",
    "......212..2....",
    "..2...2112.1....",
    "..1..2111221....",
    "..12.2111112....",
    ".211211ddd12.2..",
    ".21111ddddd121..",
    ".2111ddMdMdd12..",
    "D2111ddddddd12D.",
    "D21111ddMdd112D.",
    "D221111MdMd122D.",
    ".D22111ddd112D..",
    ".DD2211111122DD.",
    "..DDD2222DDDD...",
    "....DDDDDDD.....",
    "................"]
# the Wallower's: the colour of pond water with the sun in it; the Weeping Mother's: the green of her tears
ascii_icon('wallower_soul', SOUL_ROWS, {'1': (206, 214, 120), '2': (120, 142, 70), 'd': (70, 84, 52), 'M': (34, 42, 30), 'D': (52, 62, 40)}, lock='12dMD')
ascii_icon('mother_soul', SOUL_ROWS, {'1': (214, 255, 196), '2': (120, 214, 110), 'd': (62, 120, 70), 'M': (22, 48, 30), 'D': (40, 80, 50)}, lock='12dMD')

# ---- the Black Firwood: what its dead leave, what is made of them, and the key to it
ascii_icon('waker_mallet', [
    "................",
    ".........ooo....",
    "........oWWwo...",
    ".......oWWwwbo..",
    "......o1Wwwbbo..",
    ".....o121wbbo...",
    ".....oW121bo....",
    "......owW1o.....",
    ".....oLloo......",
    "....oLlo........",
    "...oLlo.........",
    "..oLlo..........",
    ".oWlo...........",
    "oWwo............",
    ".oo.............",
    "................"], {'1': (236, 232, 220), '2': (198, 192, 176)}, handheld=True)
ascii_icon('bleached_linen', [
    "................",
    "................",
    "...oooooooooo...",
    "..o1111111112o..",
    "..o1111111122o..",
    "..oNnNnNnNnN2o..",
    "..o1111111122o..",
    "..o1111111223o..",
    "..o2222222233o..",
    "..o1111111122o..",
    "..o1111112223o..",
    "..o2222222333o..",
    "...oooooooooo...",
    "................",
    "................",
    "................"], {'1': (236, 232, 220), '2': (198, 192, 176), '3': (150, 144, 130), 'N': (150, 40, 36), 'n': (196, 150, 140)})
ascii_icon('candle_wax', [
    "................",
    "................",
    "................",
    ".......ooo......",
    "......o112o.....",
    ".....o11122o....",
    "....oo112223o...",
    "...o11o22233o...",
    "..o11122o233oo..",
    "..o1122223o112o.",
    "..o1222233o1223o",
    "...o22333o12233o",
    "....oooooo.oooo.",
    "................",
    "................",
    "................"], {'1': (244, 236, 204), '2': (214, 200, 156), '3': (164, 146, 104)})
ascii_icon('shatun_hide', [
    "................",
    "..oo........oo..",
    ".o12o......o12o.",
    ".o122oooooo223o.",
    "..o1212122223o..",
    "..o1221212233o..",
    "...o21212223o...",
    "...o12122233o...",
    "...o21222223o...",
    "..o1212222333o..",
    "..o1222223233o..",
    ".o122oo33oo233o.",
    ".o23o..oo..o33o.",
    "..oo........oo..",
    "................",
    "................"], {'1': (122, 96, 70), '2': (84, 62, 46), '3': (52, 38, 30)})
# the key of the Wood Wicket: the same make as the others, and somebody held a candle over it for a long time
ascii_icon('firwood_key', [
    "................",
    "..........ooo...",
    ".........oSsdo..",
    "........oSo.odo.",
    "........oso.odo.",
    "........o1dodo..",
    ".......o112do...",
    "......oS12oo....",
    ".....oSs2o......",
    "....oSs2o.......",
    "...oSsdoo.......",
    "..oSsdoso.......",
    ".oSsdos2o.......",
    ".osdo.oo........",
    "..oo............",
    "................"], {'1': (244, 236, 204), '2': (214, 200, 156)})
# the Shatun's: the colour of his eyes; the Elder's: the colour of a candle seen through linen
ascii_icon('shatun_soul', SOUL_ROWS, {'1': (255, 196, 116), '2': (206, 122, 56), 'd': (122, 66, 38), 'M': (40, 22, 16), 'D': (84, 48, 32)}, lock='12dMD')
ascii_icon('elder_soul', SOUL_ROWS, {'1': (255, 248, 222), '2': (228, 204, 140), 'd': (156, 130, 84), 'M': (52, 40, 26), 'D': (110, 92, 62)}, lock='12dMD')


# ---- the Morning Wharf: what its dead leave, what is made of them, the key to it, and its two charms
ascii_icon('cargo_hook', [
    "................",
    ".........oooo...",
    "........oSssdo..",
    ".......oso..odo.",
    ".......oo...odo.",
    "...........osdo.",
    "..........osdo..",
    ".........osdo...",
    "........osdo....",
    "....o..osdo.....",
    "...oWoosdo......",
    "..oWwWodo.......",
    "...owWwo........",
    "....owWwo.......",
    ".....owbo.......",
    "......oo........"], handheld=True)
ascii_icon('capsized_claw', [
    "................",
    "..........oo....",
    ".........o1Co...",
    "........o12Co...",
    ".......o112co.oo",
    ".......o122ooCco",
    "......o1122o1Co.",
    "......o12231120.",
    ".....o112222230.",
    ".....o12222330..",
    "....o11222330...",
    "...oTo122300....",
    "..oTtTo330......",
    ".oTtTto.o.......",
    ".otTto..........",
    "..ooo..........."], {'1': SHELL, '2': SHELLM, '3': SHELLD, '0': O, 'T': (96, 80, 66), 't': TAR}, handheld=True)
ascii_icon('lead_line', [
    "................",
    "...oooooo.......",
    "..o1212121o.....",
    ".o1o....o2o.....",
    ".o2o....o1o.....",
    ".o1o....o2o.....",
    "..o2121212oo....",
    "...oooooo21o....",
    ".........o12o...",
    "..........o1o...",
    "..........oMo...",
    ".........oMsMo..",
    ".........oMsmo..",
    "........oMssmmo.",
    "........oMmmmmo.",
    ".........ooooo.."], {'1': CANVAS, '2': CANVASD}, handheld=True)
ascii_icon('sailcloth', [
    "................",
    "................",
    "..ooooooooooo...",
    ".o11111111112o..",
    ".o1G111111122o..",
    ".o11111111122o..",
    ".o3232323232233o",
    ".o11111111122o3o",
    ".o11111112223oo.",
    ".o22222222233o..",
    ".o11111111122o..",
    ".o1111112G223o..",
    ".o22222222333o..",
    "..ooooooooooo...",
    "................",
    "................"], {'1': CANVAS, '2': CANVASM, '3': CANVASD})
ascii_icon('tarred_rope', [
    "................",
    "................",
    "....oooooo......",
    "...o121212o.....",
    "..o12oooo21o....",
    ".o21o....o12o...",
    ".o12o....o21o...",
    ".o21o....o12o...",
    ".o12oooooo21o...",
    "..o21212121ooo..",
    "...ooooooo2121o.",
    "..........oo212o",
    "............o12o",
    ".............oo.",
    "................",
    "................"], {'1': (104, 84, 66), '2': TARD})
ascii_icon('crab_shell', [
    "................",
    "................",
    "....o.oooo.o....",
    "...o1o1111o1o...",
    "..o1111C1111o...",
    ".o111C11111122o.",
    ".o11111111C122o.",
    "o1111C111112223o",
    "o11111111122223o",
    ".o1111C1122223o.",
    ".o222111222233o.",
    "..o3222222333o..",
    "...oo333333oo...",
    ".....oooooo.....",
    "................",
    "................"], {'1': SHELL, '2': SHELLM, '3': SHELLD})
# the key of the Sea Gate: the same make as the others; it lay among candles, and before that long in salt air
ascii_icon('shore_key', [
    "................",
    "..........ooo...",
    ".........oSsdo..",
    "........oSo.odo.",
    "........oso.odo.",
    "........o1dodo..",
    ".......o112do...",
    "......oS12oo....",
    ".....oSs2o......",
    "....oSs2o.......",
    "...oSsdoo.......",
    "..oSsdoso.......",
    ".oSsdos2o.......",
    ".osdo.oo........",
    "..oo............",
    "................"], {'1': (132, 196, 176), '2': (78, 132, 122)})
ascii_icon('stubble_key', [
    "................",
    "..........ooo...",
    ".........oSsdo..",
    "........oSo.odo.",
    "........oso.odo.",
    "........o1dodo..",
    ".......o112do...",
    "......oS12oo....",
    ".....oSs2o......",
    "....oSs2o.......",
    "...oSsdoo.......",
    "..oSsdoso.......",
    ".oSsdos2o.......",
    ".osdo.oo........",
    "..oo............",
    "................"], {'1': (240, 208, 110), '2': (176, 138, 58)})
# the key of the Lower Grate: black iron, and an amber eye in its bow - the Founder's seal
ascii_icon('depths_key', [
    "................",
    "..........ooo...",
    ".........oSsdo..",
    "........oSo.odo.",
    "........oso.odo.",
    "........o1dodo..",
    ".......o112do...",
    "......oS12oo....",
    ".....oSs2o......",
    "....oSs2o.......",
    "...oSsdoo.......",
    "..oSsdoso.......",
    ".oSsdos2o.......",
    ".osdo.oo........",
    "..oo............",
    "................"], {'1': (255, 196, 90), '2': (214, 132, 36), 'S': (86, 88, 100), 's': (62, 64, 74), 'd': (38, 40, 48)})
# the Bolt's: amber, with its keyhole in it; the Coming's: the night, a rim of light round it
ascii_icon('bolt_soul', SOUL_ROWS, {'1': (255, 226, 140), '2': (236, 156, 44), 'd': (150, 86, 22), 'M': (20, 18, 26), 'D': (62, 64, 74)}, lock='12dMD')
ascii_icon('coming_soul', SOUL_ROWS, {'1': (236, 226, 255), '2': (126, 96, 206), 'd': (44, 40, 92), 'M': (6, 6, 14), 'D': (18, 18, 36)}, lock='12dMD')
# the Helmsman's: the green of deep water with the morning on it; the Capsized's: the red of a boiled shell
ascii_icon('helmsman_soul', SOUL_ROWS, {'1': (196, 244, 228), '2': (92, 176, 164), 'd': (48, 102, 106), 'M': (18, 36, 44), 'D': (34, 66, 74)}, lock='12dMD')
ascii_icon('capsized_soul', SOUL_ROWS, {'1': (255, 196, 164), '2': (216, 100, 72), 'd': (132, 54, 46), 'M': (44, 20, 20), 'D': (86, 36, 32)}, lock='12dMD')

# ---- the Stubble: what the harvest was got in with
ascii_icon('reaper_sickle', [
    "................",
    ".......oooo.....",
    ".....ooSSSso....",
    "....oSSsooodo...",
    "...oSsoo..oso...",
    "...oSso....oo...",
    "...oSso.........",
    "...oSsdo........",
    "....oSsdo.......",
    ".....oSsdo......",
    "......oWwoo.....",
    ".......oWwo.....",
    "........oWwo....",
    ".........oWbo...",
    "..........oo....",
    "................"], handheld=True)
ascii_icon('thresh_flail', [
    "................",
    "............oo..",
    "...........oWwo.",
    "..........oWwo..",
    ".........oWwo...",
    "........oWwo....",
    ".......oWwoRo...",
    "......oWwo.oRo..",
    ".....oWwo..oWo..",
    "....oWwo...oWwo.",
    "...oWwo....oWwo.",
    "..oWwo.....oWwo.",
    ".oWwo......oWbo.",
    ".oWbo.......oo..",
    "..oo............",
    "................"], handheld=True)
ascii_icon('twin_ear', [
    "................",
    "....o.....o.....",
    "...o1o...o1o....",
    "..o121o.o121o...",
    "..o212o.o212o...",
    "..o121o.o121o...",
    "..o212o.o212o...",
    "...o1o...o1o....",
    "....o3o.o3o.....",
    ".....o3o3o......",
    "......o3o.......",
    "......o3o.......",
    "......o3o.......",
    "......o3o.......",
    ".......o........",
    "................"], {'1': (240, 208, 110), '2': (196, 158, 70), '3': (150, 132, 70)})
# ---- the Stubble: what its dead leave, and the souls of its two lords
STRAW_Y, STRAW_M, STRAW_K = (232, 198, 100), (196, 158, 70), (140, 108, 48)
ascii_icon('rye_straw', [
    "................",
    "..........o.o...",
    ".........o1o1o..",
    "......o.o1o12o..",
    ".....o1o12o23o..",
    "....o1212o23o...",
    "....o12o123o....",
    ".....o1223o.....",
    "....oBB23Bo.....",
    "....o1BBB3o.....",
    "...o12o223o.....",
    "..o12o1o23o.....",
    "..o1o12o23o.....",
    "..oo12o.o3o.....",
    "...oo2o..o......",
    ".....o.........."], {'1': STRAW_Y, '2': STRAW_M, '3': STRAW_K, 'B': (120, 98, 62)})
ascii_icon('flour_sack', [
    "................",
    "......oooo......",
    ".....o1221o.....",
    "......oBBo......",
    ".....o1BB2o.....",
    "....o111222o....",
    "...o11W11222o...",
    "...o1WWW1223o...",
    "..o11WWW12223o..",
    "..o111W112223o..",
    "..o1111122233o..",
    "..o1111222233o..",
    "..o1112222333o..",
    "...o22223333o...",
    "....oooooooo....",
    "................"], {'1': (196, 180, 142), '2': (160, 144, 110), '3': (118, 104, 78), 'B': (104, 84, 52), 'W': (244, 240, 228)})
# the Warden's: the white of the afternoon with fire under it; the Miller's: flour, and the grey of the stone
ascii_icon('warden_soul', SOUL_ROWS, {'1': (255, 244, 196), '2': (255, 176, 64), 'd': (204, 96, 30), 'M': (48, 22, 14), 'D': (112, 50, 22)}, lock='12dMD')
ascii_icon('miller_soul', SOUL_ROWS, {'1': (248, 244, 232), '2': (188, 182, 168), 'd': (120, 116, 110), 'M': (30, 28, 30), 'D': (70, 68, 68)}, lock='12dMD')

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

bolt('wisp_flame_bolt', (232, 255, 214), (150, 255, 130), (60, 150, 70), [
    "................", "................", ".......3........", "......33..3.....",
    ".....3323.3.....", "....3322233.....", "...33221223.....", "...322111223....",
    "...322111223....", "...332212233....", "....3322233.....", ".....33333......",
    "................", "................", "................", "................"])

# ---------------------------------------------------------------------------------------------------- spawn eggs

# ---------------------------------------------------------------------------------------------------- charms

# the charms (Charm.java): small things on a loop of cord
ascii_icon('coffin_nail', [
    "................",
    "......ollo......",
    ".....ol..lo.....",
    ".....ol..lo.....",
    "......olLo......",
    ".....oMMMMo.....",
    ".....oMmmMo.....",
    "......oSdo......",
    "......oSdo......",
    "......oRdo......",
    "......oSro......",
    "......osdo......",
    "......ordo......",
    ".......odo......",
    ".......oo.......",
    "................"])
ascii_icon('ash_knot', [
    "................",
    "......ollo......",
    ".....ol..lo.....",
    "......olLo......",
    ".....oLlLlo.....",
    "......o12o......",
    ".....o1221o.....",
    "....o122221o....",
    "....o1223221o...",
    "...o122322321o..",
    "...o123222331o..",
    "...o223223331o..",
    "....o2333333o...",
    ".....oooooo.....",
    "................",
    "................"], {'1': (176, 168, 150), '2': (128, 122, 110), '3': (84, 80, 76)})
ascii_icon('fetter_link', [
    "................",
    "......ollo......",
    ".....ol..lo.....",
    "......olLo......",
    "......oMMo......",
    ".....oMooMo.....",
    "....oMo..oMo....",
    "....oSo..oMo....",
    "....oMo..omo....",
    "....oMo..oro....",
    "....oRo..omo....",
    ".....oMoomo.....",
    "......ommo......",
    ".......oo.......",
    "................",
    "................"])
ascii_icon('candle_stub', [
    "................",
    ".......F........",
    "......FfF.......",
    "......feF.......",
    ".......b........",
    ".....o1111o.....",
    "....oo1221o.....",
    "....o112221o....",
    "....o1o2223o....",
    ".....oo2223oo...",
    ".....o122231o...",
    ".....o12223oo...",
    ".....o12233o....",
    ".....oooooo.....",
    "................",
    "................"], {'1': (240, 232, 200), '2': (212, 198, 156), '3': (166, 148, 108)}, lock='Ff')
ascii_icon('wedge_cord', [
    "................",
    "......ollo......",
    ".....ol..lo.....",
    ".....ol..lo.....",
    "......olLo......",
    "....oSSSSSSo....",
    "....oDddddDo....",
    ".....osssdo.....",
    ".....osssdo.....",
    ".....osssdo.....",
    "......ossdo.....",
    "......osdo......",
    ".......odo......",
    ".......oo.......",
    "................",
    "................"])
ascii_icon('pike_tooth', [
    "................",
    "......ollo......",
    ".....ol..lo.....",
    "......olLo......",
    ".....oCCCo......",
    ".....oCCcco.....",
    ".....oCCcko.....",
    "......oCcko.....",
    "......oCccko....",
    ".......oCcko....",
    ".......oCcko....",
    "........oCko....",
    "........oCko....",
    ".........oo.....",
    "................",
    "................"])
ascii_icon('weeping_herb', [
    "................",
    ".........oo.....",
    "....oo..o1o.....",
    "...oVo.o12o.oo..",
    "...oVvooVo.oVo..",
    "....oVvVvooVvo..",
    ".....oVVvoVvo...",
    "......oVvVvo....",
    "......oVvvo.....",
    ".....oLlLlo.....",
    "......oVvo......",
    "......oVvo......",
    ".....oVo.ovo....",
    ".....oo...oo....",
    "................",
    "................"], {'1': (214, 255, 196), '2': (150, 220, 150)}, lock='12')
ascii_icon('vigil_wax', [
    "................",
    "......ollo......",
    ".....ol..lo.....",
    "......olLo......",
    ".....o1111o.....",
    "....o111112o....",
    "...o11211222o...",
    "...o11212122o...",
    "...o11122212o...",
    "...o12121223o...",
    "...o2222223o....",
    "....o222233o....",
    ".....o3333o.....",
    "......oooo......",
    "................",
    "................"], {'1': (244, 236, 204), '2': (214, 200, 156), '3': (164, 146, 104)})
ascii_icon('shatun_claw', [
    "................",
    "......ollo......",
    ".....ol..lo.....",
    "......olLo......",
    "....oWwWwWo.....",
    "....owWwWwo.....",
    ".....o122o......",
    ".....o1223o.....",
    "......o123o.....",
    "......o1223o....",
    ".......o123o....",
    "........o23o....",
    "........o13o....",
    ".........o3o....",
    ".........oo.....",
    "................"], {'1': (122, 110, 104), '2': (74, 64, 62), '3': (44, 36, 38)})

ascii_icon('sea_knot', [
    "................",
    "......ollo......",
    ".....ol..lo.....",
    "......olLo......",
    "......o12o......",
    "...ooo1221ooo...",
    "..o1212oo2121o..",
    ".o12oo1221oo21o.",
    ".o21o12oo21o12o.",
    ".o12oo2112oo21o.",
    "..o2121oo1212o..",
    "...ooo1221ooo...",
    ".....o1oo2o.....",
    ".....o2o.o1o....",
    "......o...o.....",
    "................"], {'1': CANVAS, '2': CANVASD})
ascii_icon('tide_pearl', [
    "................",
    "................",
    "................",
    "....oooooooo....",
    "...o22323232o...",
    "..o2323232323o..",
    ".o233o1111o332o.",
    ".o23o1WW111o32o.",
    ".o23o1W1112o32o.",
    ".o32o111122o23o.",
    "..o33o1222o33o..",
    "...o33oooo33o...",
    "....o333333o....",
    ".....oooooo.....",
    "................",
    "................"], {'1': (236, 240, 244), '2': (176, 186, 204), '3': (96, 104, 122), 'W': (255, 255, 255)}, lock='W')

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
egg('withered_acolyte', (66, 46, 62), (214, 184, 106), ((6, 8, EMB), (9, 8, EMB)))
egg('withered_townsman', (150, 140, 118), (98, 72, 48), ((6, 8, EMB), (9, 8, EMB)))
egg('withered_butcher', (126, 104, 85), (124, 40, 36), ((6, 8, EMB), (9, 8, EMB)))
egg('withered_watchman', (44, 50, 66), (214, 170, 80), ((6, 8, EMB), (9, 8, EMB)))
egg('carrion_hound', (74, 64, 58), (38, 30, 28), ((5, 7, EMB), (10, 7, EMB), (7, 10, C), (8, 10, C)))
egg('gravedigger', (86, 78, 66), (46, 40, 36), ((6, 7, F), (9, 7, F), (6, 10, C), (7, 10, C), (8, 10, C), (9, 10, C)))
DUSTY = (190, 186, 174)
egg('withered_digger', DUSTY, (94, 62, 38), ((6, 8, EMB), (9, 8, EMB), (7, 4, F), (7, 5, C)))
egg('withered_barrowman', DUSTY, (112, 88, 60), ((6, 8, EMB), (9, 8, EMB), (6, 11, M), (7, 11, M), (8, 11, M), (9, 11, M)))
egg('withered_sapper', (94, 62, 38), (40, 34, 32), ((6, 8, EMB), (9, 8, EMB), (8, 3, F)))
egg('blind_crawler', (188, 180, 170), (134, 128, 150), ((6, 9, N), (7, 9, C), (8, 9, N), (9, 9, C), (5, 9, C)))
egg('unhewn', (206, 202, 190), (126, 122, 116), ((6, 7, F), (9, 7, F), (7, 10, k), (8, 10, k)))
egg('taskmaster', (150, 138, 124), (92, 64, 42), ((7, 5, F), (8, 5, F), (6, 8, EMB), (9, 8, EMB), (6, 10, C), (7, 10, C), (8, 10, C), (9, 10, C)))
egg('stone_eater', (96, 90, 96), (120, 220, 190), ((5, 10, C), (6, 10, n), (7, 10, C), (8, 10, n), (9, 10, C), (10, 10, n)))
MIRE_GREEN = (150, 255, 130)
egg('withered_drowned', (86, 100, 78), (52, 74, 40), ((6, 8, EMB), (9, 8, EMB), (5, 5, v), (6, 6, v), (10, 5, v)))
egg('bone_pike', (150, 156, 132), (70, 84, 60), ((4, 9, C), (5, 9, O), (6, 9, C), (7, 9, O), (8, 9, C), (10, 7, MIRE_GREEN)))
egg('lanky', (84, 78, 56), (68, 96, 48), ((7, 5, MIRE_GREEN), (8, 5, MIRE_GREEN), (7, 8, O), (7, 9, O), (7, 10, O), (7, 11, O)))
egg('wisp', (46, 70, 50), MIRE_GREEN, ((7, 7, C), (8, 7, C), (7, 8, O), (8, 8, C), (7, 5, MIRE_GREEN), (8, 4, MIRE_GREEN)))
egg('wallower', (92, 96, 62), (176, 170, 118), ((4, 10, O), (5, 10, C), (6, 10, O), (7, 10, C), (8, 10, O), (9, 10, C), (10, 10, O), (11, 10, C),
                                                (5, 7, F), (10, 7, F)))
egg('weeping_mother', (70, 60, 46), (88, 112, 50), ((6, 6, C), (7, 6, C), (8, 6, C), (9, 6, C), (6, 7, MIRE_GREEN), (9, 7, MIRE_GREEN),
                                                    (6, 8, MIRE_GREEN), (9, 8, MIRE_GREEN), (6, 9, MIRE_GREEN), (9, 9, MIRE_GREEN)))
SECT_WHITE = (222, 216, 200)
egg('wakeful', SECT_WHITE, (150, 40, 36), ((6, 8, F), (9, 8, F), (6, 9, O), (9, 9, O), (7, 2, F), (7, 3, C)))
egg('waker', (60, 56, 62), (152, 110, 66), ((6, 8, F), (9, 8, F), (11, 10, f), (11, 11, F)))
egg('sleeper', (190, 182, 160), (140, 110, 70), ((5, 8, O), (6, 8, O), (9, 8, O), (10, 8, O), (7, 11, O), (8, 11, O)))
egg('waxen', (236, 226, 190), (196, 176, 120), ((6, 2, F), (4, 4, F), (10, 3, F), (6, 9, O), (7, 9, O), (8, 9, O), (9, 9, O), (7, 10, O), (8, 10, O)))
egg('sleep_moth', (120, 108, 100), (220, 214, 200), ((5, 7, F), (5, 8, O), (10, 7, F), (10, 8, O), (7, 5, O), (8, 5, O)))
egg('shatun', (70, 52, 40), (110, 84, 60), ((6, 7, EMB), (9, 7, EMB), (6, 10, C), (7, 11, C), (8, 11, C), (9, 10, C), (4, 12, M), (5, 13, M)))
egg('elder', (214, 208, 192), (128, 124, 128), ((5, 3, F), (7, 2, F), (10, 3, F), (6, 7, F), (9, 7, F), (7, 9, k), (8, 9, k), (7, 10, k), (8, 10, k),
                                                 (7, 11, k), (8, 11, k)))
SALT_BLUE = (150, 196, 204)
egg('dockhand', (150, 134, 104), (58, 48, 44), ((6, 8, SALT_BLUE), (9, 8, SALT_BLUE), (10, 11, s), (11, 10, s), (11, 9, s)))
egg('harpooner', (196, 170, 80), (58, 48, 44), ((6, 8, SALT_BLUE), (9, 8, SALT_BLUE), (11, 5, s), (11, 6, W), (11, 7, W), (11, 8, W)))
egg('oarsman', (124, 100, 78), (176, 162, 130), ((6, 8, SALT_BLUE), (9, 8, SALT_BLUE), (6, 10, C), (7, 11, C), (8, 11, C), (9, 10, C)))
egg('pilot', (206, 176, 70), (44, 50, 66), ((6, 7, SALT_BLUE), (9, 7, SALT_BLUE), (7, 11, F), (8, 11, f), (7, 12, f)))
egg('sand_crab', (214, 196, 150), (190, 88, 60), ((6, 6, O), (9, 6, O), (4, 9, (142, 58, 44)), (11, 9, (142, 58, 44)), (3, 8, (190, 88, 60)), (12, 8, (190, 88, 60))))
egg('skua', (92, 78, 68), (226, 222, 210), ((7, 6, O), (8, 7, F), (9, 7, f)))
egg('helmsman', (44, 52, 70), (176, 140, 62), ((6, 7, SALT_BLUE), (9, 7, SALT_BLUE), (5, 10, s), (6, 11, s), (7, 11, d), (8, 11, s), (9, 11, d), (10, 10, s)))
egg('capsized', (110, 104, 96), (190, 88, 60), ((4, 8, M), (5, 8, M), (6, 8, M), (7, 8, M), (8, 8, M), (9, 8, M), (10, 8, M), (11, 8, M), (6, 11, F), (9, 11, F)))
SUNLIT = (255, 226, 130)
egg('reaper', (212, 202, 176), (150, 52, 40), ((6, 8, O), (9, 8, O), (10, 10, W), (11, 9, W), (11, 8, W)))
egg('mower', (214, 180, 88), (166, 132, 58), ((6, 8, O), (9, 8, O), (4, 11, W), (5, 11, W), (6, 11, W), (7, 11, W)))
egg('sackman', (238, 234, 222), (176, 160, 124), ((6, 8, O), (9, 8, O), (7, 10, O), (8, 10, O)))
egg('scorched', (36, 30, 28), (255, 132, 40), ((6, 8, F), (9, 8, F), (7, 11, f), (8, 11, f)))
egg('scarecrow', (96, 78, 70), (176, 160, 124), ((5, 7, O), (7, 7, O), (6, 8, O), (5, 9, O), (7, 9, O), (9, 7, O), (11, 7, O), (10, 8, O), (9, 9, O), (11, 9, O)))
egg('stook', (214, 180, 88), (120, 98, 62), ((6, 7, O), (9, 7, O), (6, 8, SUNLIT), (9, 8, SUNLIT)))
egg('sungazer', (244, 190, 44), (70, 46, 28), ((7, 8, SUNLIT), (8, 8, SUNLIT), (7, 9, SUNLIT), (8, 9, SUNLIT)))
egg('scorched_warden', (38, 30, 28), (214, 180, 88), ((5, 7, F), (6, 7, f), (9, 7, f), (10, 7, F), (6, 10, f), (7, 11, F), (8, 11, F), (9, 10, f)))
STARLIT = (226, 230, 255)
VIO = (150, 112, 232)
AMB = (255, 196, 90)
HAND = (176, 166, 182)
egg('usher', (20, 20, 36), (176, 166, 182), ((6, 6, HAND), (7, 6, HAND), (8, 6, HAND), (9, 6, HAND), (6, 7, HAND), (9, 7, HAND), (7, 8, STARLIT), (8, 8, STARLIT)))
egg('stray_shadow', (10, 10, 18), (26, 26, 46), ((6, 7, STARLIT), (9, 7, STARLIT)))
egg('longarm', (22, 22, 40), (126, 116, 138), ((4, 9, HAND), (5, 10, HAND), (10, 10, HAND), (11, 9, HAND), (7, 6, VIO)))
egg('handwalker', (26, 26, 46), (176, 166, 182), ((4, 8, HAND), (6, 8, HAND), (8, 8, HAND), (10, 8, HAND), (5, 11, HAND), (7, 11, HAND), (9, 11, HAND)))
egg('deepling', (104, 96, 122), (66, 60, 86), ((7, 4, VIO), (8, 4, VIO), (7, 5, STARLIT), (6, 10, (214, 208, 196)), (8, 10, (214, 208, 196)), (10, 10, (214, 208, 196))))
egg('gut', (40, 34, 52), (128, 52, 112), ((6, 7, (214, 208, 196)), (8, 7, (214, 208, 196)), (10, 7, (214, 208, 196)), (7, 8, (128, 52, 112)), (9, 8, (128, 52, 112))))
egg('clockhand', (62, 64, 74), (150, 122, 58), ((7, 7, AMB), (8, 7, AMB), (7, 8, AMB), (8, 8, AMB), (7, 4, (198, 170, 92)), (8, 4, (198, 170, 92))))
egg('bolt', (36, 38, 46), (150, 122, 58), ((6, 6, AMB), (7, 6, AMB), (8, 6, AMB), (9, 6, AMB), (6, 7, AMB), (9, 7, AMB), (6, 8, AMB), (7, 8, (20, 18, 26)), (8, 8, (20, 18, 26)),
                                         (9, 8, AMB), (6, 9, AMB), (7, 9, AMB), (8, 9, AMB), (9, 9, AMB)))
egg('coming', (12, 12, 24), (64, 96, 214), ((6, 5, STARLIT), (7, 5, STARLIT), (8, 5, STARLIT), (9, 5, STARLIT), (5, 6, STARLIT), (10, 6, STARLIT), (5, 7, STARLIT), (10, 7, STARLIT),
                                           (6, 8, STARLIT), (7, 8, STARLIT), (8, 8, STARLIT), (9, 8, STARLIT), (6, 6, (4, 4, 10)), (7, 6, (4, 4, 10)), (8, 6, (4, 4, 10)),
                                           (9, 6, (4, 4, 10)), (6, 7, (4, 4, 10)), (7, 7, (4, 4, 10)), (8, 7, (4, 4, 10)), (9, 7, (4, 4, 10))))
egg('hollow_miller', (206, 200, 186), (104, 70, 44), ((6, 7, O), (9, 7, O), (6, 10, M), (7, 10, M), (8, 10, M), (9, 10, M), (7, 11, M), (8, 11, M)))
egg('gilded_knight', (176, 176, 186), (214, 184, 106), ((5, 8, O), (6, 8, EMB), (7, 8, O), (8, 8, O), (9, 8, EMB), (10, 8, O)))
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


def big_display(grip):
    """How a 32x32 weapon is held. It is drawn at twice the size of an ordinary one, so the place where the hand closes
    (`grip`, in texture pixels from the bottom left corner) has to be brought back into the hand: the game's own
    "handheld" entry assumes the grip of a 16x16 sword."""
    out = {}
    for view, tilt, move, scale, plain in (('thirdperson', 55, (0, 4.0, 0.5), 1.7, 0.85), ('firstperson', 25, (1.13, 3.2, 1.13), 1.0, 0.68)):
        ours = (grip / 2.0 - 8.0) * scale          # the grip, from the middle of the item, in sixteenths of a block
        vanilla = (2.5 - 8.0) * plain
        d = vanilla - ours
        a = math.radians(tilt)
        along, up = d * (math.cos(a) - math.sin(a)), d * (math.sin(a) + math.cos(a))
        t = [round(move[0], 2), round(move[1] + up, 2), round(move[2] + along, 2)]
        for hand, sign in (('righthand', 1), ('lefthand', -1)):
            out[f'{view}_{hand}'] = {'rotation': [0, -90 * sign, tilt * sign], 'translation': t, 'scale': [scale, scale, plain]}
    return out


BIG_GRIP = {'rust_greatsword': 5.5, 'great_key': 10.5, 'grave_spade': 8.0, 'pitchfork': 10.0, 'quarry_pick': 9.0, 'great_maul': 8.0, 'boat_hook': 10.0,
            'vigil_staff': 10.0, 'bear_spear': 10.0, 'great_candle': 9.0,
            'oar': 9.0, 'harpoon': 10.0, 'ship_anchor': 6.0, 'scythe': 9.0, 'warden_pole': 9.0}

if __name__ == '__main__':
    os.makedirs(f'{A}/textures/item', exist_ok=True)
    for name, sp in ICONS.items():
        sp.save(f'{A}/textures/item/{name}.png')
        model = {'parent': 'minecraft:item/handheld' if name in HANDHELD or name in BIG else 'minecraft:item/generated',
                 'textures': {'layer0': f'{MOD}:item/{name}'}}
        if name in BIG:
            model['display'] = big_display(BIG_GRIP[name])
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
