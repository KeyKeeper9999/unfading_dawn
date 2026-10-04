#!/usr/bin/env python3
"""Every text of the mod in one place: key -> (Russian, English). Writes ru_ru.json and en_us.json. Run from the repo root."""
import json
import os

A = 'src/main/resources/assets'
M = 'vsratie_dushi'
T = {}


def t(key, ru, en):
    T[key] = (ru, en)


t(f'itemGroup.{M}', 'Всратые души', 'Vsratie Dushi')
t(f'key.category.{M}.main', 'Всратые души', 'Vsratie Dushi')
t(f'key.{M}.roll', 'Перекат', 'Roll')

t(f'block.{M}.hearth', 'Очаг', 'Hearth')
t(f'block.{M}.fog_gate', 'Туманная завеса', 'Fog Gate')

for key, ru, en in (
        ('broken_sword', 'Обломок меча', 'Broken Sword'), ('shiv', 'Заточка', 'Shiv'), ('dagger', 'Кинжал', 'Dagger'),
        ('shortsword', 'Короткий меч', 'Shortsword'), ('longsword', 'Длинный меч', 'Longsword'), ('broadsword', 'Палаш', 'Broadsword'),
        ('sabre', 'Сабля', 'Sabre'), ('hatchet', 'Топорик', 'Hatchet'), ('battle_axe', 'Боевой топор', 'Battle Axe'), ('club', 'Дубина', 'Club'),
        ('flanged_mace', 'Шестопёр', 'Flanged Mace'), ('rust_greatsword', 'Ржавый двуручник', 'Rust Greatsword'),
        ('great_key', 'Великий ключ', 'Great Key'), ('censer_flail', 'Кадило на цепи', 'Censer Flail'), ('short_bow', 'Короткий лук', 'Short Bow'),
        ('plank_shield', 'Щит из досок', 'Plank Shield'), ('round_shield', 'Круглый щит', 'Round Shield'),
        ('guard_shield', 'Щит стражника', "Guard's Shield"), ('tower_shield', 'Башенный щит', 'Tower Shield'),
        ('scholar_staff', 'Посох школяра', "Scholar's Staff"), ('prayer_beads', 'Чётки', 'Prayer Beads'),
        ('ember_glove', 'Тлеющая рукавица', 'Ember Glove'),
        ('soul_shard', 'Осколок души', 'Soul Shard'), ('heavy_soul_shard', 'Тяжёлый осколок души', 'Heavy Soul Shard'), ('mend', 'Исцеление', 'Mend'),
        ('rebuke', 'Отпор', 'Rebuke'), ('fire_orb', 'Огненный ком', 'Fire Orb'), ('flare', 'Вспышка', 'Flare'),
        ('warmth_flask', 'Фляга тепла', 'Flask of Warmth'), ('flask_shard', 'Осколок фляги', 'Flask Shard'),
        ('nameless_soul', 'Душа безымянного', 'Soul of a Nameless One'), ('great_soul', 'Большая душа', 'Great Soul'),
        ('overseer_soul', 'Душа Надзирателя', 'Soul of the Overseer'), ('chaplain_soul', 'Душа Капеллана', 'Soul of the Chaplain'),
        ('return_ember', 'Уголёк возврата', 'Ember of Return'), ('tar_pot', 'Горшок смолы', 'Tar Pot'), ('soul_stain', 'Пятно душ', 'Soul Stain'),
        ('shard_bolt', 'Осколок души', 'Soul Shard'), ('heavy_shard_bolt', 'Тяжёлый осколок души', 'Heavy Soul Shard'),
        ('fire_orb_bolt', 'Огненный ком', 'Fire Orb'), ('light_spear_bolt', 'Копьё света', 'Spear of Light'),
        ('dark_flame_bolt', 'Чёрное пламя', 'Dark Flame')):
    t(f'item.{M}.{key}', ru, en)

MOBS = (('withered_prisoner', 'Истлевший узник', 'Withered Prisoner'), ('withered_guard', 'Истлевший стражник', 'Withered Guard'),
        ('withered_archer', 'Истлевший лучник', 'Withered Archer'), ('rust_knight', 'Ржавый рыцарь', 'Rust Knight'),
        ('keykeeper', 'Слепой ключник', 'Blind Keykeeper'), ('overseer', 'Надзиратель Острога', 'Overseer of the Stockade'),
        ('chaplain', 'Капеллан Острога', 'Chaplain of the Stockade'))
for key, ru, en in MOBS:
    t(f'entity.{M}.{key}', ru, en)
    t(f'item.{M}.{key}_spawn_egg', f'Яйцо призыва: {ru[0].lower() + ru[1:]}', f'{en} Spawn Egg')
t(f'entity.{M}.soul_stain', 'Пятно душ', 'Soul Stain')
t(f'entity.{M}.spell_bolt', 'Заклинание', 'Spell')
t(f'entity.{M}.tar_pot', 'Горшок смолы', 'Tar Pot')

t(f'tooltip.{M}.scaling', 'Сила: %s   Ловкость: %s', 'Strength: %s   Dexterity: %s')
t(f'tooltip.{M}.stamina', 'Расход сил: %s', 'Stamina cost: %s')
t(f'tooltip.{M}.backstab', 'Удар в спину: ×%s', 'Backstab: ×%s')
t(f'tooltip.{M}.stability', 'Устойчивость: %s%%', 'Stability: %s%%')
t(f'tooltip.{M}.catalyst.sorcery', 'Катализатор чародейства', 'Catalyst of sorcery')
t(f'tooltip.{M}.catalyst.miracle', 'Катализатор чудес', 'Catalyst of miracles')
t(f'tooltip.{M}.catalyst.pyromancy', 'Катализатор пиромантии', 'Catalyst of pyromancy')
t(f'tooltip.{M}.catalyst_use', 'Держи в одной руке, заклинание — в другой', 'Hold it in one hand and a spell in the other')
t(f'tooltip.{M}.school.sorcery', 'Чародейство', 'Sorcery')
t(f'tooltip.{M}.school.miracle', 'Чудо', 'Miracle')
t(f'tooltip.{M}.school.pyromancy', 'Пиромантия', 'Pyromancy')
t(f'tooltip.{M}.casts', 'Зарядов: %s / %s', 'Casts: %s / %s')
t(f'tooltip.{M}.flask', 'Глотков: %s / %s', 'Gulps: %s / %s')
t(f'tooltip.{M}.flask_hint', 'Наполняется у очага', 'Refills at a hearth')
t(f'tooltip.{M}.soul_item', 'Раздави в руке: +%s душ', 'Crush it in your hand: +%s souls')
t(f'tooltip.{M}.return_ember', 'Подержи в руке — вернёшься к последнему очагу', 'Hold it to return to the last hearth')
t(f'tooltip.{M}.flask_shard', 'Фляга вместит на глоток больше', 'The flask will hold one more gulp')

t(f'spell.{M}.soul_shard.desc', 'Быстрый сгусток бледного света', 'A quick bolt of pale light')
t(f'spell.{M}.heavy_soul_shard.desc', 'Медленный и тяжёлый: бьёт вдвое больнее', 'Slow and heavy: hits twice as hard')
t(f'spell.{M}.mend.desc', 'Лечит после короткой молитвы', 'Heals after a short prayer')
t(f'spell.{M}.rebuke.desc', 'Отбрасывает всех, кто рядом', 'Throws back everyone nearby')
t(f'spell.{M}.fire_orb.desc', 'Ком огня, рвётся там, где упадёт', 'A ball of fire that bursts where it lands')
t(f'spell.{M}.flare.desc', 'Язык пламени прямо из ладони', 'A tongue of flame straight from the palm')

t(f'message.{M}.need_catalyst.sorcery', 'Нужен посох в другой руке', 'You need a staff in the other hand')
t(f'message.{M}.need_catalyst.miracle', 'Нужны чётки в другой руке', 'You need prayer beads in the other hand')
t(f'message.{M}.need_catalyst.pyromancy', 'Нужна тлеющая рукавица в другой руке', 'You need the ember glove in the other hand')
t(f'message.{M}.flask_empty', 'Фляга пуста. Отдохни у очага', 'The flask is empty. Rest at a hearth')
t(f'message.{M}.flask_enlarged', 'Теперь во фляге помещается глотков: %s', 'The flask now holds %s gulps')
t(f'message.{M}.flask_shard_useless', 'Нет фляги, или она уже больше некуда', 'No flask, or it cannot grow any more')
t(f'message.{M}.souls_left', 'На месте гибели осталось душ: %s', 'Souls left where you fell: %s')

t(f'banner.{M}.boss_defeated', 'ГАД ПОВЕРЖЕН', 'FIEND FELLED')
t(f'banner.{M}.hearth_lit', 'ОЧАГ РАЗОЖЖЁН', 'HEARTH LIT')
t(f'banner.{M}.souls_retrieved', 'ДУШИ ВОЗВРАЩЕНЫ', 'SOULS RETRIEVED')
t(f'hud.{M}.lost', 'потеряно: %s', 'lost: %s')

t(f'screen.{M}.shop', 'Товары ключника', "The Keykeeper's Wares")
t(f'screen.{M}.shop.leave', 'Уйти', 'Leave')
t(f'screen.{M}.hearth', 'Очаг', 'Hearth')
t(f'screen.{M}.hearth.leave', 'Встать', 'Get up')
t(f'screen.{M}.hearth.level', 'Уровень: %s', 'Level: %s')
t(f'screen.{M}.hearth.souls', 'Души: %s', 'Souls: %s')
t(f'screen.{M}.hearth.cost', 'Следующее очко стоит: %s', 'The next point costs: %s')
t(f'screen.{M}.hearth.hint', 'Наведи на характеристику, чтобы узнать, что она даёт', 'Point at a stat to see what it does')
for key, ru, en, ru_d, en_d in (
        ('vitality', 'Живучесть', 'Vitality', 'Одно сердце за каждое очко', 'One heart per point'),
        ('endurance', 'Выносливость', 'Endurance', 'Больше запас сил: на удары, перекаты, бег и щит', 'More stamina for blows, rolls, running and the shield'),
        ('strength', 'Сила', 'Strength', 'Урон оружия с буквой у Силы', 'Damage of weapons with a Strength grade'),
        ('dexterity', 'Ловкость', 'Dexterity', 'Урон оружия с буквой у Ловкости', 'Damage of weapons with a Dexterity grade'),
        ('intellect', 'Разум', 'Intellect', 'Сила чародейства и, вполовину, пиромантии', 'Power of sorcery and, by half, of pyromancy'),
        ('faith', 'Вера', 'Faith', 'Сила чудес и, вполовину, пиромантии', 'Power of miracles and, by half, of pyromancy')):
    t(f'stat.{M}.{key}', ru, en)
    t(f'stat.{M}.{key}.desc', ru_d, en_d)

t(f'dialogue.{M}.line', '%s: %s', '%s: %s')
for i, (ru, en) in enumerate((
        ('Шаги... Живые шаги. Давно тут никто не ходил на своих двоих.', 'Steps... Living steps. Nobody has walked here on their own two feet for a long time.'),
        ('Я слеп, но ключи помню на ощупь. Вот этот — от твоей удачи. Шучу. Держи лучше флягу.',
         'I am blind, but I know my keys by touch. This one opens your luck. A joke. Take the flask instead.'),
        ('Пей, когда прижмёт. Опустеет — посиди у очага, она наполнится сама.',
         'Drink when things get bad. When it is empty, sit by a hearth and it will fill itself.'),
        ('И не геройствуй: души, что носишь с собой, остаются там, где упадёшь. Успеешь вернуться — подберёшь.',
         'And do not play the hero: the souls you carry stay where you fall. Come back in time and they are yours again.'))):
    t(f'dialogue.{M}.keykeeper.first.{i}', ru, en)
for i, (ru, en) in enumerate((
        ('Надзиратель всё ходит наверху. Слышишь, как ключ волочится?', 'The Overseer still walks up there. Hear the key dragging?'),
        ('Стражники прячутся за щитом. Жди, пока ударят сами, или заходи со спины.',
         'The guards hide behind their shields. Wait for them to strike, or get behind them.'),
        ('Перекатывайся. В мои годы уже не выходит, а тебе — в самый раз.', 'Roll. At my age it does not work any more, but you are just right for it.'),
        ('Капеллан всё ещё служит в часовне. Только паства у него теперь... не та.',
         'The Chaplain still holds his services in the chapel. Only his flock is... not what it was.'),
        ('Потратил бы ты души у очага. Мёртвым они ни к чему.', 'Spend your souls at a hearth. The dead have no use for them.'))):
    t(f'dialogue.{M}.keykeeper.idle.{i}', ru, en)

# ---- subtitles of the sounds (the events themselves are listed in tools/gen_sounds.py)
SUBTITLES = {
    'entity.withered.ambient': ('Истлевший хрипит', 'Withered rasps'), 'entity.withered.hurt': ('Истлевший вскрикивает', 'Withered cries out'),
    'entity.withered.death': ('Истлевший затихает', 'Withered falls silent'),
    'entity.rust_knight.ambient': ('Рыцарь дышит в шлем', 'Knight breathes in his helm'), 'entity.rust_knight.hurt': ('Латы гремят', 'Plate rings'),
    'entity.rust_knight.death': ('Рыцарь рушится', 'Knight collapses'), 'entity.rust_knight.step': ('Латы лязгают', 'Plate clanks'),
    'entity.overseer.ambient': ('Надзиратель сопит', 'Overseer wheezes'), 'entity.overseer.hurt': ('Надзиратель рычит', 'Overseer growls'),
    'entity.overseer.death': ('Надзиратель падает', 'Overseer falls'), 'entity.overseer.roar': ('Надзиратель ревёт', 'Overseer roars'),
    'entity.overseer.slam': ('Удар сотрясает пол', 'A blow shakes the floor'), 'entity.overseer.swing': ('Ключ рассекает воздух', 'The key cuts the air'),
    'entity.overseer.chain': ('Гремит цепь', 'A chain rattles'),
    'entity.chaplain.ambient': ('Капеллан бормочет молитву', 'Chaplain mutters a prayer'), 'entity.chaplain.hurt': ('Капеллан шипит', 'Chaplain hisses'),
    'entity.chaplain.death': ('Капеллан рассыпается', 'Chaplain crumbles'), 'entity.chaplain.cast': ('Капеллан мечет копьё', 'Chaplain hurls a spear'),
    'entity.chaplain.bell': ('Звонит колокол', 'A bell tolls'), 'entity.chaplain.blink': ('Капеллан исчезает', 'Chaplain vanishes'),
    'entity.keykeeper.ambient': ('Ключник кашляет', 'Keykeeper coughs'), 'entity.keykeeper.give': ('Звенят ключи', 'Keys jingle'),
    'ui.death': ('Ты помер', 'You croaked'), 'ui.boss_defeated': ('Гад повержен', 'Fiend felled'), 'ui.hearth_lit': ('Очаг разгорается', 'Hearth flares up'),
    'ui.level_up': ('Сила прибывает', 'Strength grows'), 'ui.souls_gain': ('Души стекаются', 'Souls gather'),
    'ui.souls_retrieve': ('Души возвращаются', 'Souls return'),
    'block.hearth.rest': ('Очаг греет', 'Hearth warms'), 'block.fog_gate.pass': ('Туман расступается', 'Fog parts'),
    'item.flask.drink': ('Глоток из фляги', 'A gulp from the flask'), 'item.flask.empty': ('Фляга пуста', 'Flask is empty'),
    'item.spell.shard': ('Осколок души летит', 'Soul shard flies'), 'item.spell.shard_hit': ('Осколок души бьёт', 'Soul shard strikes'),
    'item.spell.mend': ('Чудо исцеляет', 'A miracle heals'), 'item.spell.rebuke': ('Волна отбрасывает', 'A wave throws back'),
    'item.spell.fire': ('Летит огонь', 'Fire flies'), 'item.spell.fire_burst': ('Огонь рвётся', 'Fire bursts'),
    'item.spell.fizzle': ('Заклинание не выходит', 'A spell fizzles'),
    'player.roll': ('Перекат', 'A roll'), 'player.backstab': ('Удар в спину', 'A stab in the back'), 'player.guard_break': ('Защита пробита', 'Guard broken'),
    'player.weapon_swing_heavy': ('Тяжёлый замах', 'A heavy swing'),
    'music.boss': ('Музыка: бой с боссом', 'Music: boss fight'),
}
for event, (ru, en) in SUBTITLES.items():
    t(f'subtitles.{M}.{event}', ru, en)

if __name__ == '__main__':
    for lang, index in (('ru_ru', 0), ('en_us', 1)):
        path = f'{A}/{M}/lang/{lang}.json'
        os.makedirs(os.path.dirname(path), exist_ok=True)
        with open(path, 'w', encoding='utf-8') as f:
            json.dump({k: v[index] for k, v in T.items()}, f, ensure_ascii=False, indent=2)
            f.write('\n')
    # The words on the death screen are the game's own text: replace them in both languages.
    for lang, text in (('ru_ru', 'ТЫ ПОМЕР'), ('en_us', 'YOU CROAKED')):
        path = f'{A}/minecraft/lang/{lang}.json'
        os.makedirs(os.path.dirname(path), exist_ok=True)
        with open(path, 'w', encoding='utf-8') as f:
            json.dump({'deathScreen.title': text, 'deathScreen.title.hardcore': text}, f, ensure_ascii=False, indent=2)
            f.write('\n')
    print(len(T), 'texts')
