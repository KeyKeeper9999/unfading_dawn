#!/usr/bin/env python3
"""How the map plays out in numbers: what a player earns on the way to each lord, what level that buys, and how the
fights look at that level. The numbers of the mobs and weapons are read from the Java sources, the enemies of each part
of the map (and their tiers) from map/census.json, which tools/gen_world.py writes whenever it builds the map (so this
takes a second, and is run after gen_world.py when the map has changed). Run from the repo root:
PYTHONPATH=tools python3 tools/balance.py

Nothing here is a test: it is the sum a designer would do on paper, kept next to the map so that it is redone whenever
the map or the mobs change."""
import json
import math
import re

import gen_data

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
    for name, path in (('overseer', 'entity/OverseerEntity.java'), ('chaplain', 'entity/ChaplainEntity.java'), ('gravedigger', 'entity/GravediggerEntity.java'),
                       ('hound', 'entity/CarrionHoundEntity.java'), ('crawler', 'entity/BlindCrawlerEntity.java'), ('unhewn', 'entity/UnhewnEntity.java'),
                       ('taskmaster', 'entity/TaskmasterEntity.java'), ('stone_eater', 'entity/StoneEaterEntity.java'),
                       ('pike', 'entity/BonePikeEntity.java'), ('lanky', 'entity/LankyEntity.java'), ('wisp', 'entity/WispEntity.java'),
                       ('wallower', 'entity/WallowerEntity.java'), ('weeping_mother', 'entity/WeepingMotherEntity.java'),
                       ('wakeful', 'entity/WakefulEntity.java'), ('waker', 'entity/WakerEntity.java'), ('sleeper', 'entity/SleeperEntity.java'),
                       ('waxen', 'entity/WaxenEntity.java'), ('moth', 'entity/SleepMothEntity.java'), ('shatun', 'entity/ShatunEntity.java'),
                       ('elder', 'entity/ElderEntity.java'), ('dockhand', 'entity/DockhandEntity.java'), ('harpooner', 'entity/HarpoonerEntity.java'),
                       ('oarsman', 'entity/OarsmanEntity.java'), ('pilot', 'entity/PilotEntity.java'), ('sand_crab', 'entity/SandCrabEntity.java'),
                       ('skua', 'entity/SkuaEntity.java'), ('helmsman', 'entity/HelmsmanEntity.java'), ('capsized', 'entity/CapsizedEntity.java')):
        text = read(path)
        out[name] = dict(attributes(text, 'createAttributes'), souls=int(re.search(r'soulReward\(\) \{\s+return (?:[\w.]+(?: > 0)? \? 0 : )?(\d+);', text).group(1)))
        hide = re.search(r'HIDE = ([0-9.]+)F', text)
        if hide:                                             # (nothing has more than 1024 health: the rest is in its hide)
            out[name]['health'] = round(out[name]['health'] / float(hide.group(1)))
    return out


def weapons():
    out = {}
    for name, dmg, speed, reach, stamina, strength, dex, back in re.findall(
            r'weapon\("(\w+)", ([0-9.]+)F, ([0-9.]+)F, (-?[0-9.]+)F, ([0-9.]+)F, ([0-9.]+)F, ([0-9.]+)F, ([0-9.]+)F', read('Dushi.java')):
        out[name] = {'damage': float(dmg), 'speed': float(speed), 'stamina': float(stamina), 'str': float(strength), 'dex': float(dex), 'back': float(back)}
    return out


def tier_table(name):
    return [float(v) for v in re.search(name + r' = \{([^}]+)\}', read('entity/SoulsMob.java')).group(1).replace('F', '').split(',')]


def level_cost(level):
    return 100 + 20 * (level - 1) + 2 * (level - 1) ** 2      # PlayerSoul.levelCost


def level_for(souls):
    level = 1
    while souls >= level_cost(level):
        souls -= level_cost(level)
        level += 1
    return level


def after_armor(damage, armor, toughness=0.0):
    """The game's own rule for armour."""
    return damage * (1 - min(20.0, max(armor / 5.0, armor - 4.0 * damage / (toughness + 8.0))) / 25.0)


def hits(weapon, strength, dexterity, mob, health_scale=1.0):
    blow = weapon['damage'] * (1 + WEAPON_BONUS * (weapon['str'] * strength + weapon['dex'] * dexterity))
    blow = after_armor(blow, mob['armor'])
    return math.ceil(mob['health'] * health_scale / blow), blow


SOUL, GREAT = 200, 1000
PLAYER = read('PlayerSoul.java')
WEAPON_BONUS = float(re.search(r'WEAPON_BONUS = ([0-9.]+)F', PLAYER).group(1))
VITALITY = float(re.search(r'HEALTH_PER_VITALITY = ([0-9.]+)', PLAYER).group(1))
WRATH = float(re.search(r'WRATH_DAMAGE = ([0-9.]+)F', read('entity/ChaplainEntity.java')).group(1))
DROP = 1 / 14          # a nameless soul from any of the withered
KNIGHT_GREAT = 0.35
BOSSES = ('gravedigger', 'overseer', 'chaplain', 'taskmaster', 'stone_eater', 'wallower', 'weeping_mother', 'shatun', 'elder', 'helmsman', 'capsized')
WITHERED = ('prisoner', 'guard', 'archer', 'acolyte', 'townsman', 'butcher', 'watchman', 'digger', 'barrowman', 'sapper', 'drowned')
# those of the skete who kneel: nobody fights all of them (a clearing is counted with a third of them woken)
KNEELING = 1 / 3
# How a part of the map is played, on the way to its lord: three quarters of what is in it are fought, not all; they are
# fought more than once, because the player dies and comes back past them (a fresh place costs several vain tries); and
# a tenth of what was gathered is left lying in stains nobody got back to.
CLEARED, PASSES, LOST = 0.75, 1.65, 0.10
PLAYED = CLEARED * PASSES * (1 - LOST)                      # 1.11 clearings' worth of souls
FARM = 0.25                                                 # and a quarter of a clearing more, for the sake of it: the farming the sums count on
# What a player is meant to come to each lord with. The places pay for nine tenths of it played as above; the rest is a
# little farming (FARM: a quarter of a clearing of that part), or a level short.
TARGET = {'gravedigger': 12, 'overseer': 18, 'chaplain': 29, 'taskmaster': 35, 'stone_eater': 39, 'wallower': 46, 'weeping_mother': 50,
          'shatun': 55, 'elder': 60, 'helmsman': 66, 'capsized': 70}
# A lord leaves about two and a half times what a level costs at the level he is met at; the dead of his place are
# priced so that the sums above come out. A new place: ten levels, six before its first lord and four before its second.


def build_of(level):
    """How a fighter might have spent the points of this level: (vitality, endurance, strength)."""
    pts = level - 1
    vit, end = round(pts * 0.35), round(pts * 0.2)
    return vit, end, pts - vit - end


if __name__ == '__main__':
    M, WPN = mobs(), weapons()
    TH, TD, TS = tier_table('TIER_HEALTH'), tier_table('TIER_DAMAGE'), tier_table('TIER_SOULS')
    census = json.load(open('map/census.json'))
    print('levels: one more point costs', ', '.join(f'{lv}->{lv + 1}: {level_cost(lv)}' for lv in (1, 5, 10, 15, 20, 25, 30, 40, 50, 60, 70, 80)))
    print('tiers: health x', TH, ' damage x', TD, ' souls x', TS)
    total = 0.0
    gulps = 5
    for i, boss in enumerate(BOSSES):
        part = census[boss]
        kinds, souls, withered, knights = {}, 0.0, 0, 0
        for key, n in part['mobs'].items():
            kind, t = key.split(':')
            t = int(t)
            if kind in BOSSES or kind == 'keykeeper':
                continue
            kinds[(kind, t)] = n
            souls += n * M[kind]['souls'] * TS[t] * (KNEELING if kind == 'wakeful' else 1.0)
            withered += n * (kind in WITHERED)
            knights += n * (kind in ('knight', 'gilded'))
        chest_souls = chest_great = 0
        for name in part['chests']:
            for item, count in gen_data.MAP_CHESTS[name]:
                chest_souls += count * SOUL if item == gen_data.SOUL else 0
                chest_great += count * GREAT if item == gen_data.GREAT else 0
        shards = part['shards']
        drops = withered * DROP * SOUL + knights * KNIGHT_GREAT * GREAT * 0.5
        bt = part['tier']
        boss_mob = dict(M[boss], health=M[boss]['health'] * TH[bt], damage=M[boss]['damage'] * TD[bt])
        print()
        print(f'before the {boss.replace("_", "-").title()}: ' + ', '.join(f'{n} {k}' + (f' (tier {t})' if t else '') for (k, t), n in sorted(kinds.items())))
        print(f'  one clearing: {souls:.0f} for kills, about {drops:.0f} in souls dropped, {chest_souls} in chests (+{chest_great} in great souls, if not kept for the anvil)')
        spent = (350, 1500, 2500, 2500, 1500, 2000, 1500, 2500, 2000, 3000, 2000)[i]  # arms and armour bought or forged on the way
        played = total + (souls + drops) * PLAYED + chest_souls + chest_great * 0.5 - spent
        for label, have in (('one clean clearing', total + souls + drops + chest_souls + chest_great * 0.5 - spent), ('as it is played', played),
                            ('with a quarter of a clearing of farming', played + (souls + drops) * FARM)):
            print(f'  {label}: {have:.0f} souls for levels after {spent} more spent on gear -> level {level_for(have)}')
        need = sum(level_cost(l) for l in range(1, TARGET[boss])) - played
        print(f'  meant: level {TARGET[boss]}; played as above that wants {max(0, need):.0f} more: {max(0, need) / max(1, souls + drops):.2f} clearings of this part;'
              f' the lord leaves {M[boss]["souls"]} = {M[boss]["souls"] / level_cost(TARGET[boss]):.1f} levels there')
        lv = level_for(played + (souls + drops) * FARM)
        vit, end, st = build_of(lv)
        health = 20 + VITALITY * vit
        flask = gulps + shards
        gulp = max(10, health * 0.5)
        print(f'  the {boss.replace("_", "-").title()} (tier {bt}: {boss_mob["health"]:.0f} health, armour {boss_mob["armor"]:.0f}, blow {boss_mob["damage"]:.1f}) '
              f'against level {lv} (vitality {vit}, endurance {end}, strength {st}; {health:.0f} health, {flask} gulps of {gulp:.0f}):')
        arms = (('broken_sword', 'club', 'pitchfork', 'hatchet'), ('club', 'shortsword', 'longsword', 'cleaver', 'grave_spade'), ('longsword', 'broadsword', 'rust_greatsword', 'great_key'),
                ('longsword', 'broadsword', 'rust_greatsword', 'great_key', 'censer_flail'), ('broadsword', 'quarry_pick', 'great_key', 'censer_flail', 'great_maul'),
                ('broadsword', 'boat_hook', 'great_maul', 'eater_claw', 'censer_flail'), ('broadsword', 'boat_hook', 'great_maul', 'eater_claw', 'wallower_fang'),
                ('broadsword', 'vigil_staff', 'waker_mallet', 'great_maul', 'wallower_fang', 'withe_scourge'),
                ('broadsword', 'vigil_staff', 'waker_mallet', 'great_maul', 'wallower_fang', 'bear_spear'),
                ('broadsword', 'cargo_hook', 'harpoon', 'oar', 'great_maul', 'bear_spear', 'great_candle'),
                ('broadsword', 'cargo_hook', 'harpoon', 'oar', 'bear_spear', 'great_candle', 'ship_anchor'))[i]
        for wname in arms:
            n, blow = hits(WPN[wname], st, 0, boss_mob)
            print(f'    {wname}: {blow:.1f} a blow, {n} blows, {n / WPN[wname]["speed"]:.0f} s of pure striking')
        for label, armor in (('rags (5)', 5), ('mail (12)', 12), ('plate (18)', 18))[:2 if i < 2 else 3]:
            blow, heavy = after_armor(boss_mob['damage'], armor), after_armor(boss_mob['damage'] * 1.5, armor)
            print(f'    in {label}: a blow takes {blow:.1f}, the heaviest {heavy:.1f}; that is {(health + flask * gulp) / heavy:.0f} of the heaviest before the end')
        if boss == 'chaplain':
            print(f'    his wrath takes {WRATH * TD[bt]:.0f} through armour: {WRATH * TD[bt] / health * 100:.0f}% of that health')
        # the common dead of this part, for the feel of it
        for (kind, t), n in sorted(kinds.items()):
            mob = dict(M[kind], health=M[kind]['health'] * TH[t], damage=M[kind]['damage'] * TD[t])
            wname = arms[1]
            nh, blow = hits(WPN[wname], st, 0, mob)
            print(f'    a {kind}: {mob["health"]:.0f} health, hits for {after_armor(mob["damage"], 5 if i == 0 else 12):.1f}; falls to {nh} blows of a {wname}')
        total += (souls + drops) * (PLAYED + FARM) + chest_souls + chest_great * 0.5 - spent + M[boss]['souls'] * TS[bt]
        gulps = flask + 1                                     # each lord leaves a shard
