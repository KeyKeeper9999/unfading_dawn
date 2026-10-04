#!/usr/bin/env python3
"""How the map plays out in numbers: what a player earns on the way to each boss, what level that buys, and how the two
fights look at that level. The numbers of the mobs and weapons are read from the Java sources, the enemies of each part
of the map from tools/gen_map.py. Run from the repo root: PYTHONPATH=tools python3 tools/balance.py

Nothing here is a test: it is the sum a designer would do on paper, kept next to the map so that it is redone whenever
the map or the mobs change."""
import math
import re

import gen_map

JAVA = 'src/main/java/com/keykeeper/dushi/'


def read(path):
    return open(JAVA + path).read()


def attributes(text, method=None):
    """MAX_HEALTH, ATTACK_DAMAGE and ARMOR of an attribute builder."""
    if method:
        text = text[text.index(method):]
    block = text[:text.index(';')]
    get = lambda name: float(re.search(name + r', ([0-9.]+)', block).group(1)) if re.search(name + r', ([0-9.]+)', block) else 0.0
    return {'health': get('MAX_HEALTH'), 'damage': get('ATTACK_DAMAGE'), 'armor': get('ARMOR')}


def mobs():
    out = {}
    for kind, hp, dmg, speed, armor, souls in re.findall(r'(\w+)\("withered_\w+", ([0-9.]+), ([0-9.]+), ([0-9.]+), ([0-9.]+), (\d+)\)', read('entity/WitheredEntity.java')):
        out[kind.lower()] = {'health': float(hp), 'damage': float(dmg), 'armor': float(armor), 'souls': int(souls)}
    knight = read('entity/RustKnightEntity.java')
    out['gilded'] = dict(attributes(knight, 'createGildedAttributes'), souls=int(re.search(r'gilded \? (\d+) : (\d+)', knight).group(1)))
    out['knight'] = dict(attributes(knight, 'createAttributes'), souls=int(re.search(r'gilded \? (\d+) : (\d+)', knight).group(2)))
    for name, path in (('overseer', 'entity/OverseerEntity.java'), ('chaplain', 'entity/ChaplainEntity.java')):
        text = read(path)
        out[name] = dict(attributes(text, 'createAttributes'), souls=int(re.search(r'soulReward\(\) \{\s+return (\d+);', text).group(1)))
    return out


def weapons():
    out = {}
    for name, dmg, speed, reach, stamina, strength, dex, back in re.findall(
            r'weapon\("(\w+)", ([0-9.]+)F, ([0-9.]+)F, (-?[0-9.]+)F, ([0-9.]+)F, ([0-9.]+)F, ([0-9.]+)F, ([0-9.]+)F', read('Dushi.java')):
        out[name] = {'damage': float(dmg), 'speed': float(speed), 'stamina': float(stamina), 'str': float(strength), 'dex': float(dex), 'back': float(back)}
    return out


def level_cost(level):
    return math.floor(100 * 1.1 ** (level - 1)) + 25 * (level - 1)


def level_for(souls):
    level = 1
    while souls >= level_cost(level):
        souls -= level_cost(level)
        level += 1
    return level


def after_armor(damage, armor, toughness=0.0):
    """The game's own rule for armour."""
    return damage * (1 - min(20.0, max(armor / 5.0, armor - 4.0 * damage / (toughness + 8.0))) / 25.0)


def hits(weapon, strength, dexterity, mob):
    blow = weapon['damage'] * (1 + WEAPON_BONUS * (weapon['str'] * strength + weapon['dex'] * dexterity))
    blow = after_armor(blow, mob['armor'])
    return math.ceil(mob['health'] / blow), blow


SOUL, GREAT = 200, 1000
PLAYER = read('PlayerSoul.java')
WEAPON_BONUS = float(re.search(r'WEAPON_BONUS = ([0-9.]+)F', PLAYER).group(1))
VITALITY = float(re.search(r'HEALTH_PER_VITALITY = ([0-9.]+)', PLAYER).group(1))
WRATH = float(re.search(r'WRATH_DAMAGE = ([0-9.]+)F', read('entity/ChaplainEntity.java')).group(1))
DROP = 1 / 14          # a nameless soul from any of the withered
KNIGHT_GREAT = 0.35

if __name__ == '__main__':
    M, WPN = mobs(), weapons()
    b = gen_map.build()
    gen_map.bedrock(b)
    stages = {}
    od, known = None, set()
    # which marks are in reach before the Overseer falls, and which only after
    report, problems, seen = gen_map.analyse(b, quiet=True)
    seals = set(b.seals['overseer'])
    first, _, seen_first = gen_map.analyse(b, blocked=frozenset(seals))
    early, late = {}, {}
    for kind, x, y, z in b.marks:
        if kind in ('keykeeper', 'overseer', 'chaplain'):
            continue
        (early if (x, y, z) in seen_first else late).setdefault(kind, 0)
        (early if (x, y, z) in seen_first else late)[kind] += 1
    chest_souls = {'early': 0, 'late': 0, 'early_great': 0, 'late_great': 0}
    import gen_data
    for name, x, y, z in b.chests:
        where = 'early' if any((x + dx, y + dy, z + dz) in seen_first for dx in range(-2, 3) for dy in (-1, 0, 1) for dz in range(-2, 3)) else 'late'
        if name in ('treasury', 'treasury_arms'):
            continue                                          # behind the last boss
        for item, count in gen_data.MAP_CHESTS[name]:
            if item == gen_data.SOUL:
                chest_souls[where] += count * SOUL
            if item == gen_data.GREAT:
                chest_souls[where + '_great'] += count * GREAT

    def income(kinds):
        kills = sum(M[k]['souls'] * n for k, n in kinds.items())
        withered = sum(n for k, n in kinds.items() if k in ('prisoner', 'guard', 'archer', 'acolyte'))
        drops = withered * DROP * SOUL + kinds.get('acolyte', 0) * 0.2 * SOUL
        greats = (kinds.get('knight', 0) + kinds.get('gilded', 0)) * KNIGHT_GREAT * GREAT
        return kills, drops, greats

    print('levels: one more point costs', ', '.join(f'{lv}->{lv + 1}: {level_cost(lv)}' for lv in (1, 5, 10, 15, 20, 25, 30)))
    print()
    k1, d1, g1 = income(early)
    lower = k1 + d1 + chest_souls['early']
    print(f'before the Overseer: {dict(sorted(early.items()))}')
    print(f'  one clearing: {k1} for kills, about {d1:.0f} in souls dropped, {chest_souls["early"]} from chests (+{chest_souls["early_great"] + g1:.0f} in great souls, if not kept for the anvil)')
    spent = 300 + 150                                        # a shortsword and a club's worth of other things
    for label, clears in (('one clearing', 1.0), ('one and a half (the way back through the yard after a death or two)', 1.5)):
        souls = (k1 + d1) * clears + chest_souls['early'] - spent
        lv = level_for(souls)
        print(f'  {label}: {souls:.0f} souls for levels after {spent} spent on arms -> level {lv}')
    lv = level_for((k1 + d1) * 1.5 + chest_souls['early'] - spent)
    pts = lv - 1
    vit, end, st = round(pts * 0.35), round(pts * 0.2), pts - round(pts * 0.35) - round(pts * 0.2)
    print(f'  the Overseer ({M["overseer"]["health"]:.0f} health, armour {M["overseer"]["armor"]:.0f}, blow {M["overseer"]["damage"]:.0f}, heavy blow {M["overseer"]["damage"] * 1.2:.1f}) '
          f'against level {lv} (vitality {vit}, endurance {end}, strength {st}):')
    for w in ('club', 'shortsword', 'longsword', 'hatchet'):
        n, blow = hits(WPN[w], st, 0, M['overseer'])
        print(f'    {w}: {blow:.1f} a blow, {n} blows, {n / WPN[w]["speed"]:.0f} s of pure striking')
    health = 20 + VITALITY * vit
    for label, armor in (('rags (5)', 5), ('mail (12)', 12)):
        blow, heavy = after_armor(M['overseer']['damage'], armor), after_armor(M['overseer']['damage'] * 1.2, armor)
        gulp = max(10, health * 0.5)
        print(f'    in {label}: {health:.0f} health, a blow takes {blow:.1f}, a heavy one {heavy:.1f}; with 6 gulps of {gulp:.0f} that is '
              f'{(health + 6 * gulp) / heavy:.0f} heavy blows before the end')
    print()
    k2, d2, g2 = income(late)
    boss = M['overseer']['souls']
    print(f'between the bosses: {dict(sorted(late.items()))}')
    print(f'  one clearing: {k2} for kills, about {d2:.0f} dropped, {chest_souls["late"]} from chests, {boss} for the Overseer himself '
          f'(+{chest_souls["late_great"] + g2:.0f} in great souls, +5000 if his soul is crushed instead of forged)')
    gear = 400 + 1100 + 400 + 300                             # a longsword or the like, mail, a shield, pots and embers
    base = (k1 + d1) * 1.5 + chest_souls['early'] - spent
    for label, extra in (('one clearing of everything', 0), ('and one more round of the cathedral, the crypt and the cloister from the sacristy', None),
                         ('and two more rounds', None), ('and the Overseer\'s soul crushed', 5000)):
        if label.startswith('and one'):
            total += round_
        elif label.startswith('and two'):
            total += round_
        elif extra:
            total += extra
        else:
            total = base + k2 + d2 + chest_souls['late'] + boss + (chest_souls['late_great'] + chest_souls['early_great']) * 0.5 - gear
            near = {k: n for k, n in late.items()}
            round_ = sum(M[k]['souls'] * n for k, n in near.items() if k in ('gilded', 'acolyte', 'knight')) * 0.75
        print(f'  {label}: {total:.0f} souls for levels -> level {level_for(total)}')
    print()
    ch = M['chaplain']
    print(f'the Chaplain ({ch["health"]:.0f} health, armour {ch["armor"]:.0f}, blow {ch["damage"]:.0f}, wrath {WRATH:.0f} through armour):')
    for lv in (19, 22, 25, 28):
        pts = lv - 1
        vit, end = round(pts * 0.35), round(pts * 0.2)
        st = pts - vit - end
        health = 20 + VITALITY * vit
        gulp = max(10, health * 0.5)
        line = []
        for w in ('longsword', 'broadsword', 'rust_greatsword', 'great_key'):
            n, blow = hits(WPN[w], st, 0, ch)
            line.append(f'{w} {n} blows ({n / WPN[w]["speed"]:.0f} s)')
        melee = after_armor(ch['damage'], 12)
        print(f'  level {lv} (vitality {vit}, strength {st}): {health:.0f} health, wrath takes {WRATH / health * 100:.0f}% of it, the censer {melee:.1f} in mail; '
              f'8 gulps of {gulp:.0f}; ' + ', '.join(line))
