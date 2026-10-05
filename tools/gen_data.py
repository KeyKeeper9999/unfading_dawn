#!/usr/bin/env python3
"""Everything under data/ that is not a building plan: recipes, what the chests of the buildings hold, item tags,
and the worldgen files that make the buildings appear. Run from the repo root."""
import glob
import json
import os

DATA = 'src/main/resources/data'
MOD = 'vsratie_dushi'
M = f'{MOD}:'
MC = 'minecraft:'


def write_json(path, data):
    os.makedirs(os.path.dirname(path), exist_ok=True)
    with open(path, 'w') as f:
        json.dump(data, f, indent=2)
        f.write('\n')


# ---------------------------------------------------------------------------------------------------- recipes

RECIPES = {}


def shaped(name, pattern, key, count=1, category='equipment', result=None):
    RECIPES[name] = {'type': 'minecraft:crafting_shaped', 'category': category, 'key': key, 'pattern': pattern,
                     'result': {'id': result or M + name, **({'count': count} if count > 1 else {})}}


def shapeless(name, ingredients, count=1, category='misc', result=None):
    RECIPES[name] = {'type': 'minecraft:crafting_shapeless', 'category': category, 'ingredients': ingredients,
                     'result': {'id': result or M + name, **({'count': count} if count > 1 else {})}}


I, N, S, L = MC + 'iron_ingot', MC + 'iron_nugget', MC + 'stick', MC + 'leather'
P, LOG, STRING = '#minecraft:planks', '#minecraft:logs', MC + 'string'
CLOTH, SCRAP, SOUL, GREAT = M + 'tattered_cloth', M + 'rust_scrap', M + 'nameless_soul', M + 'great_soul'
GOLD = MC + 'gold_nugget'

# the hearth: a campfire in an iron brazier on stone, lit with a soul
shaped('hearth', [' N ', 'ICI', 'BBB'], {'N': SOUL, 'I': I, 'C': MC + 'campfire', 'B': MC + 'stone_bricks'}, category='building')
shaped('forge', ['N', 'A', 'L'], {'N': SOUL, 'A': MC + 'anvil', 'L': LOG}, category='building')
# weapons
shaped('club', ['W', 'W', 'S'], {'W': LOG, 'S': S})
shaped('shiv', ['N', 'S'], {'N': N, 'S': S})
shaped('dagger', ['I', 'L'], {'I': I, 'L': L})
shaped('shortsword', ['I', 'I', 'L'], {'I': I, 'L': L})
shaped('longsword', [' I ', ' I ', 'NLN'], {'I': I, 'N': N, 'L': L})
shaped('broadsword', ['II', 'II', ' L'], {'I': I, 'L': L})
shaped('sabre', ['  I', ' I ', 'L  '], {'I': I, 'L': L})
shaped('hatchet', ['IL', 'S '], {'I': I, 'L': L, 'S': S})
shaped('battle_axe', ['III', 'ISI', ' S '], {'I': I, 'S': S})
shaped('flanged_mace', [' I ', 'III', ' S '], {'I': I, 'S': S})
shaped('short_bow', ['S#', 'S#'], {'S': S, '#': STRING})
# shields
shaped('plank_shield', ['PSP', 'PPP'], {'P': P, 'S': S})
shaped('round_shield', [' P ', 'PIP', ' P '], {'P': P, 'I': I})
shaped('guard_shield', ['IPI', 'ILI', ' I '], {'P': P, 'I': I, 'L': L})
shaped('tower_shield', ['RRR', 'RLR', 'RRR'], {'R': SCRAP, 'L': L})
# catalysts
shaped('scholar_staff', ['  A', ' S ', 'S  '], {'A': MC + 'amethyst_shard', 'S': S})
shaped('prayer_beads', [' # ', '#G#', ' # '], {'#': STRING, 'G': GOLD})
shapeless('ember_glove', [L, L, '#minecraft:coals', MC + 'flint'], category='equipment')
# spells: a sheet of paper, a soul to write with, and something of the spell's nature
for name, soul, extra in (('soul_shard', SOUL, MC + 'lapis_lazuli'), ('heavy_soul_shard', GREAT, MC + 'lapis_lazuli'),
                          ('mend', SOUL, MC + 'glow_berries'), ('rebuke', SOUL, MC + 'feather'),
                          ('flare', SOUL, MC + 'gunpowder'), ('fire_orb', GREAT, MC + 'gunpowder')):
    shapeless(name, [MC + 'paper', soul, extra])
# things to throw and to use up
shapeless('tar_pot', [MC + 'clay_ball', '#minecraft:coals', MC + 'gunpowder'], count=2)
shapeless('return_ember', [SOUL, '#minecraft:coals'], count=2)
shapeless('iron_from_scrap', [SCRAP, SCRAP], count=3, result=MC + 'iron_nugget')
# armour: rags of cloth, mail of scrap, plate hammered over the mail, vestments of cloth trimmed with gold
ARMOUR_SHAPES = (['XXX', 'X X'], ['X X', 'XXX', 'XXX'], ['XXX', 'X X', 'X X'], ['X X', 'X X'])
TRIMMED = (['XGX', 'X X'], ['X X', 'XGX', 'XXX'], ['XGX', 'X X', 'X X'], ['X X', 'G G'])
for i, (rags, mail, plate, vestment) in enumerate(zip(
        ('prisoner_hood', 'prisoner_rags', 'prisoner_breeches', 'prisoner_wraps'),
        ('guard_hat', 'guard_hauberk', 'guard_leggings', 'guard_boots'),
        ('rust_helm', 'rust_cuirass', 'rust_tassets', 'rust_sabatons'),
        ('chaplain_mitre', 'chaplain_cassock', 'chaplain_skirt', 'chaplain_shoes'))):
    shaped(rags, ARMOUR_SHAPES[i], {'X': CLOTH})
    shaped(mail, ARMOUR_SHAPES[i], {'X': SCRAP})
    shaped(plate, [' R ', 'RAR', ' I '], {'R': SCRAP, 'A': M + mail, 'I': I})
    shaped(vestment, TRIMMED[i], {'X': CLOTH, 'G': GOLD})


# ---------------------------------------------------------------------------------------------------- chests

def item(name, weight=1, count=None):
    entry = {'type': 'minecraft:item', 'name': name, 'weight': weight}
    if count:
        entry['modifier'] = [{'type': 'minecraft:set_count', 'count': {'type': 'minecraft:uniform', 'min': count[0], 'max': count[1]}}]
    return entry


def nothing(weight):
    return {'type': 'minecraft:empty', 'weight': weight}


def pool(*entries, rolls=1):
    return {'rolls': rolls, 'entries': list(entries)}


CHESTS = {
    # by the keeper's hearth: enough for someone who arrives with empty hands
    'stockade_gate': [pool(item(M + 'plank_shield')), pool(item(M + 'club'), item(M + 'shiv')), pool(item(M + 'tar_pot', count=(2, 3))),
                      pool(item(MC + 'bread', count=(2, 4))), pool(item(M + 'return_ember'))],
    'stockade_cell': [pool(item(CLOTH, count=(2, 4))), pool(item(M + 'shiv'), item(M + 'prisoner_hood'), item(M + 'prisoner_rags'), item(SOUL)),
                      pool(item(MC + 'bread', count=(1, 2)), item(MC + 'bone', count=(1, 3)))],
    'stockade_hall': [pool(item(GREAT, count=(1, 2))), pool(item(SCRAP, count=(3, 6))),
                      pool(item(M + 'broadsword'), item(M + 'battle_axe'), item(M + 'flanged_mace'), item(M + 'tower_shield')),
                      pool(item(I, count=(2, 5))), pool(item(M + 'guard_hat'), item(M + 'guard_hauberk'), item(M + 'guard_leggings'), item(M + 'guard_boots'))],
    'stockade_chapel': [pool(item(GREAT)), pool(item(M + 'mend'), item(M + 'rebuke'), item(M + 'soul_shard'), item(M + 'flare')),
                        pool(item(M + 'chaplain_skirt'), item(M + 'chaplain_shoes')), pool(item(MC + 'gold_ingot', count=(1, 3))),
                        pool(item(M + 'prayer_beads'), item(M + 'scholar_staff'), item(M + 'ember_glove'))],
    # on the roof of the cell block, past the archers
    'stockade_treasure': [pool(item(M + 'flask_shard')), pool(item(GREAT)), pool(item(M + 'return_ember', count=(2, 3))), pool(item(M + 'short_bow'))],
    'outpost': [pool(item(SOUL, count=(1, 2))), pool(item(CLOTH, count=(1, 3)), item(SCRAP, count=(1, 2))), pool(item(MC + 'bread', count=(1, 3))),
                pool(item(M + 'club'), item(M + 'dagger'), item(M + 'plank_shield'), item(M + 'short_bow'), item(M + 'tar_pot', count=(2, 2)), nothing(2)),
                pool(item(M + 'flask_shard'), nothing(9)), pool(item(MC + 'arrow', count=(4, 8)), nothing(1))],
}

# The chests of the map "High Stockade" (tools/gen_map.py): each holds exactly what is listed, the same for everyone.
MAP_CHESTS = {
    # the Dead Borough: what a newcomer starts with is poor on purpose; the town's blind alleys make up for it
    'start': [(M + 'broken_sword', 1), (M + 'plank_shield', 1), (M + 'tar_pot', 2)],
    'borough_alley': [(M + 'club', 1), (CLOTH, 2)],
    'borough_nook': [(SCRAP, 3), (CLOTH, 2)],
    'borough_house': [(M + 'prisoner_rags', 1), (M + 'return_ember', 1), (SOUL, 1)],
    'borough_yard': [(M + 'hatchet', 1), (M + 'tar_pot', 2)],
    'tanners': [(M + 'prisoner_wraps', 1), (CLOTH, 4), (SCRAP, 2)],
    'market': [(M + 'round_shield', 1), (SOUL, 1), (SCRAP, 2)],
    'shambles_yard': [(M + 'dagger', 1), (SCRAP, 3)],
    'butchers': [(SOUL, 1), (M + 'tar_pot', 3), (CLOTH, 2)],
    'chapel': [(GREAT, 1), (SCRAP, 4), (M + 'return_ember', 2)],
    # the High Stockade (flask shards are not in chests: they lie in the ash, see ShardBlock, or are left by the lords)
    'gatehouse': [(SCRAP, 3), (M + 'tar_pot', 2), (SOUL, 1)],
    'cells': [(CLOTH, 3), (SOUL, 1), (M + 'shiv', 1)],
    'store': [(SCRAP, 4), (M + 'tar_pot', 3), (MC + 'arrow', 12)],
    'far_tower': [(SOUL, 2), (SCRAP, 2)],
    'bridge_tower': [(M + 'short_bow', 1), (MC + 'arrow', 16), (M + 'return_ember', 1)],
    'smithy': [(SCRAP, 4), (CLOTH, 4), (SOUL, 1)],
    'chapter': [(M + 'rebuke', 1), (M + 'prayer_beads', 1), (GREAT, 1)],
    'chapter_arms': [(M + 'guard_hauberk', 1), (SOUL, 2), (SCRAP, 3)],
    'tower_nook': [(M + 'tar_pot', 4), (M + 'return_ember', 2)],
    'belfry': [(GREAT, 1), (M + 'return_ember', 1)],
    'west_gallery': [(M + 'guard_shield', 1), (GREAT, 1), (SCRAP, 4)],
    'sacristy': [(M + 'mend', 1), (M + 'return_ember', 2), (SOUL, 2)],
    'crypt': [(GREAT, 1), (M + 'rust_cuirass', 1)],
    'crypt_side': [(SCRAP, 6), (M + 'heavy_soul_shard', 1), (SOUL, 2)],
    'treasury': [(GREAT, 3), (M + 'quarry_key', 1)],          # the key of the Quarrymen's Gate: the way to the White Pit
    'treasury_arms': [(M + 'chaplain_skirt', 1), (M + 'chaplain_shoes', 1), (M + 'tower_shield', 1)],
    # the White Pit (tools/gen_quarry.py)
    'quarry_shed': [(M + 'powder_pot', 4), (M + 'iron_wedge', 2), (SCRAP, 3)],
    'quarry_field': [(M + 'iron_wedge', 3), (SOUL, 2), (M + 'return_ember', 1)],
    'quarry_shop': [(GREAT, 1), (M + 'iron_wedge', 2), (CLOTH, 4)],
    'quarry_eyes': [(GREAT, 2), (M + 'powder_pot', 3)],
    'quarry_rim': [(M + 'powder_pot', 4), (M + 'return_ember', 2), (SOUL, 2)],
    'quarry_workings': [(GREAT, 2), (M + 'iron_wedge', 3)],
    'quarry_stall': [(GREAT, 1), (M + 'rust_helm', 1), (SCRAP, 6)],
    'quarry_breach': [(GREAT, 4), (M + 'return_ember', 3), (M + 'iron_wedge', 4)],
}
for _name, _items in MAP_CHESTS.items():
    CHESTS['map/' + _name] = [pool(item(_id, count=(_n, _n)) if _n > 1 else item(_id)) for _id, _n in _items]

# ---------------------------------------------------------------------------------------------------- tags and worldgen

TAGS = {
    f'{MOD}/tags/item/repairs_cloth': [CLOTH],
    f'{MOD}/tags/item/repairs_scrap': [SCRAP],
    # where the buildings may stand: any land of the overworld (water and cliffs are ruled out by the structure itself)
    f'{MOD}/tags/worldgen/biome/has_structure': ['#minecraft:is_overworld'],
    'minecraft/tags/item/head_armor': [M + n for n in ('prisoner_hood', 'guard_hat', 'rust_helm', 'chaplain_mitre')],
    'minecraft/tags/item/chest_armor': [M + n for n in ('prisoner_rags', 'guard_hauberk', 'rust_cuirass', 'chaplain_cassock')],
    'minecraft/tags/item/leg_armor': [M + n for n in ('prisoner_breeches', 'guard_leggings', 'rust_tassets', 'chaplain_skirt')],
    'minecraft/tags/item/foot_armor': [M + n for n in ('prisoner_wraps', 'guard_boots', 'rust_sabatons', 'chaplain_shoes')],
}


def structure(plan):
    # no monsters of the game's own spawn inside: the dead of the place are put there by its marks
    return {'type': f'{MOD}:plan', 'plan': plan, 'biomes': f'#{MOD}:has_structure', 'step': 'surface_structures',
            'terrain_adaptation': 'beard_thin', 'spawn_overrides': {'monster': {'bounding_box': 'full', 'spawns': []}}}


STRUCTURE_SETS = {
    # a stockade every few hundred blocks where the land allows (many places are too steep or too wet), outposts far more often but never right beside a stockade
    'stockades': {'placement': {'type': 'minecraft:random_spread', 'salt': 918273645, 'spacing': 24, 'separation': 10},
                  'structures': [{'structure': f'{MOD}:stockade', 'weight': 1}]},
    'outposts': {'placement': {'type': 'minecraft:random_spread', 'salt': 564738291, 'spacing': 14, 'separation': 6,
                               'exclusion_zone': {'other_set': f'{MOD}:stockades', 'chunk_count': 6}},
                 'structures': [{'structure': f'{MOD}:outpost', 'weight': 1}]},
}

if __name__ == '__main__':
    for old in glob.glob(f'{DATA}/{MOD}/recipe/*.json'):
        os.remove(old)
    for name, recipe in RECIPES.items():
        write_json(f'{DATA}/{MOD}/recipe/{name}.json', recipe)
    for name, pools in CHESTS.items():
        write_json(f'{DATA}/{MOD}/loot_table/chests/{name}.json', {'type': 'minecraft:chest', 'pools': pools})
    for path, values in TAGS.items():
        write_json(f'{DATA}/{path}.json', {'values': values})
    for plan in ('stockade', 'outpost'):
        write_json(f'{DATA}/{MOD}/worldgen/structure/{plan}.json', structure(plan))
    for name, data in STRUCTURE_SETS.items():
        write_json(f'{DATA}/{MOD}/worldgen/structure_set/{name}.json', data)
    print(f'{len(RECIPES)} recipes, {len(CHESTS)} chests, {len(TAGS)} tags')
