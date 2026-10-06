#!/usr/bin/env python3
"""Checks that every registered thing has its files: item definitions, models, textures, sounds, names in both languages.
Run from the repo root; exits with 1 if something is missing."""
import glob
import json
import os
import re
import sys

A = 'src/main/resources/assets/vsratie_dushi'
J = 'src/main/java/com/keykeeper/dushi'
M = 'vsratie_dushi'
problems = []
main = open(f'{J}/Dushi.java').read()
items = set(re.findall(r'(?:weapon|shield|registerItem|registerSimpleItem)\("(\w+)"', main))
spells = re.findall(r'^\s+[A-Z_]+\("(\w+)", SpellSchool', open(f'{J}/Spell.java').read(), re.M)
bolts = re.findall(r'^\s+[A-Z_]+\("(\w+)", [\d.]+, [\d.]+\)', open(f'{J}/entity/SpellBoltEntity.java').read(), re.M)
kinds = re.findall(r'^\s+[A-Z_]+\("(withered_\w+)"', open(f'{J}/entity/WitheredEntity.java').read(), re.M)
mobs = kinds + ['rust_knight', 'gilded_knight', 'keykeeper', 'overseer', 'chaplain', 'carrion_hound', 'gravedigger', 'blind_crawler', 'unhewn', 'taskmaster',
               'stone_eater', 'bone_pike', 'lanky', 'wisp', 'wallower', 'weeping_mother', 'wakeful', 'waker', 'sleeper', 'waxen', 'sleep_moth', 'shatun', 'elder']
charms = re.findall(r'^\s+[A-Z_]+\("(\w+)", \d+\)', open(f'{J}/Charm.java').read(), re.M)
armour = re.findall(r'"((?:prisoner|guard|rust|chaplain|vigil|mason|scale)_\w+)"', open(f'{J}/ArmorSet.java').read())
items |= set(armour)
items |= set(charms) | set(spells) | {b + '_bolt' for b in bolts} | {m + '_spawn_egg' for m in mobs} | {'hearth', 'fog_gate', 'mob_mark', 'forge'}
blocks = {'hearth', 'fog_gate', 'mob_mark', 'forge', 'place_name', 'shard_heap', 'locked_gate', 'shallows', 'sludge'}
entities = set(mobs) | {'soul_stain', 'spell_bolt', 'tar_pot', 'powder_pot', 'thrown_wedge', 'mud_glob', 'root'}
lang = {l: json.load(open(f'{A}/lang/{l}.json', encoding='utf-8')) for l in ('ru_ru', 'en_us')}


def need(path, what):
    if not os.path.exists(path):
        problems.append(f'{what}: missing {path}')
        return False
    return True


def model_ok(ref, seen=()):
    ns, path = ref.split(':')
    if ns != M:
        return
    file = f'{A}/models/{path}.json'
    if not need(file, f'model {ref}'):
        return
    data = json.load(open(file))
    for tex in data.get('textures', {}).values():
        if tex.startswith(M + ':'):
            need(f'{A}/textures/{tex.split(":")[1]}.png', f'texture of {ref}')
    if 'parent' in data and data['parent'] not in seen:
        model_ok(data['parent'], seen + (ref,))


def walk(node):
    if isinstance(node, dict):
        if node.get('type') == 'minecraft:model':
            model_ok(node['model'])
        for v in node.values():
            walk(v)
    elif isinstance(node, list):
        for v in node:
            walk(v)


for name in sorted(items):
    if need(f'{A}/items/{name}.json', f'item {name}'):
        walk(json.load(open(f'{A}/items/{name}.json')))
    key = f'block.{M}.{name}' if name in blocks else f'item.{M}.{name}'
    for l, d in lang.items():
        if key not in d:
            problems.append(f'{l}: no name for {key}')
for name in blocks:
    if need(f'{A}/blockstates/{name}.json', f'block {name}'):
        for variant in json.load(open(f'{A}/blockstates/{name}.json'))['variants'].values():
            model_ok(variant['model'])
for name in entities:
    for l, d in lang.items():
        if f'entity.{M}.{name}' not in d:
            problems.append(f'{l}: no name for entity {name}')
for tex in re.findall(r'"(textures/entity/[\w/]+\.png)"', ''.join(open(f).read() for f in glob.glob(f'{J}/client/*.java'))):
    need(f'{A}/{tex}', 'entity texture')
for kind in kinds + ['blind_crawler', 'unhewn', 'unhewn_glow', 'taskmaster', 'taskmaster_glow', 'stone_eater', 'stone_eater_glow', 'bone_pike',
                     'bone_pike_glow', 'lanky', 'lanky_glow', 'wisp', 'wisp_glow', 'wallower', 'wallower_glow', 'weeping_mother', 'weeping_mother_glow', 'wakeful', 'wakeful_glow', 'waker', 'waker_glow', 'sleeper',
                     'waxen', 'waxen_glow', 'sleep_moth', 'sleep_moth_glow', 'shatun', 'shatun_glow', 'elder', 'elder_glow', 'elder_asleep_glow']:
    need(f'{A}/textures/entity/{kind}.png', 'entity texture')
sounds = json.load(open(f'{A}/sounds.json'))
registered = set(re.findall(r'"((?:entity|ui|block|item|player|music)\.[\w.]+)"', main[main.index('SOUND_NAMES'):main.index('static {', main.index('SOUND_NAMES'))]))
used = set(re.findall(r'sound\("([\w.]+)"\)', ''.join(open(f).read() for f in glob.glob(f'{J}/**/*.java', recursive=True))))
used |= set(re.findall(r'new Net\.Banner\("[\w.]*", [\w.]+, "([\w.]+)"\)', ''.join(open(f).read() for f in glob.glob(f'{J}/**/*.java', recursive=True))))
for name in registered ^ set(sounds):
    problems.append(f'sound {name} is {"not in sounds.json" if name in registered else "in sounds.json but not registered"}')
for name in used - registered:
    problems.append(f'sound {name} is played but not registered')
for name, entry in sounds.items():
    for s in entry['sounds']:
        ref = s if isinstance(s, str) else s['name']
        need(f'{A}/sounds/{ref.split(":")[1]}.ogg', f'sound {name}')
    for l, d in lang.items():
        if entry['subtitle'] not in d:
            problems.append(f'{l}: no subtitle for {name}')
for k in set(lang['ru_ru']) ^ set(lang['en_us']):
    problems.append(f'text {k} is not in both languages')
# recipes, chest loot and plans may only name items and loot tables that exist
DATA = 'src/main/resources/data/vsratie_dushi'
names = set()
for path in glob.glob(f'{DATA}/recipe/*.json') + glob.glob(f'{DATA}/loot_table/chests/**/*.json', recursive=True):
    for ref in re.findall(M + r':(\w+)', open(path).read()):
        if ref not in items:
            problems.append(f'{path}: no such item {ref}')
for path in glob.glob(f'{DATA}/plan/*.txt') + glob.glob('map/*.plan.txt'):
    for ref in re.findall(r'^chest ' + M + r':chests/([\w/]+)', open(path).read(), re.M):
        need(f'{DATA}/loot_table/chests/{ref}.json', f'loot table of {path}')
for name in armour:
    suit = name.split('_')[0]
    for layer in ('humanoid', 'humanoid_leggings'):
        need(f'{A}/textures/entity/equipment/{layer}/{suit}.png', f'worn texture of {name}')
    need(f'{A}/equipment/{suit}.json', f'equipment definition of {name}')
for p in problems:
    print(p)
print(f'{len(items)} items, {len(entities)} entities, {len(sounds)} sounds: {len(problems)} problem(s)')
sys.exit(1 if problems else 0)
