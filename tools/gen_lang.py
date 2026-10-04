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
t(f'block.{M}.forge', 'Старая наковальня', 'Old Anvil')
t(f'block.{M}.mob_mark', 'Метка восставшего', 'Mark of the Risen')

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
        ('dark_flame_bolt', 'Чёрное пламя', 'Dark Flame'),
        ('prisoner_hood', 'Мешок узника', "Prisoner's Sack"), ('prisoner_rags', 'Рубище узника', "Prisoner's Rags"),
        ('prisoner_breeches', 'Портки узника', "Prisoner's Breeches"), ('prisoner_wraps', 'Обмотки узника', "Prisoner's Wraps"),
        ('guard_hat', 'Шапель стражника', "Guard's Kettle Hat"), ('guard_hauberk', 'Кольчуга стражника', "Guard's Hauberk"),
        ('guard_leggings', 'Поножи стражника', "Guard's Leggings"), ('guard_boots', 'Сапоги стражника', "Guard's Boots"),
        ('rust_helm', 'Ржавый шлем', 'Rust Helm'), ('rust_cuirass', 'Ржавая кираса', 'Rust Cuirass'),
        ('rust_tassets', 'Ржавые набедренники', 'Rust Tassets'), ('rust_sabatons', 'Ржавые сабатоны', 'Rust Sabatons'),
        ('chaplain_mitre', 'Митра капеллана', "Chaplain's Mitre"), ('chaplain_cassock', 'Ряса капеллана', "Chaplain's Cassock"),
        ('chaplain_skirt', 'Подрясник капеллана', "Chaplain's Skirt"), ('chaplain_shoes', 'Туфли капеллана', "Chaplain's Shoes"),
        ('pitchfork', 'Вилы', 'Pitchfork'), ('cleaver', 'Тесак мясника', "Butcher's Cleaver"), ('grave_spade', 'Заступ могильщика', "Gravedigger's Spade"),
        ('gravedigger_soul', 'Душа Могильщика', 'Soul of the Gravedigger'),
        ('tattered_cloth', 'Ветошь', 'Tattered Cloth'), ('rust_scrap', 'Ржавый лом', 'Rust Scrap')):
    t(f'item.{M}.{key}', ru, en)

MOBS = (('withered_prisoner', 'Истлевший узник', 'Withered Prisoner'), ('withered_guard', 'Истлевший стражник', 'Withered Guard'),
        ('withered_archer', 'Истлевший лучник', 'Withered Archer'), ('withered_acolyte', 'Истлевший послушник', 'Withered Acolyte'),
        ('withered_townsman', 'Истлевший посадский', 'Withered Townsman'), ('withered_butcher', 'Истлевший мясник', 'Withered Butcher'),
        ('withered_watchman', 'Истлевший сторож', 'Withered Watchman'), ('carrion_hound', 'Трупный пёс', 'Carrion Hound'),
        ('gravedigger', 'Могильщик Посада', 'Gravedigger of the Borough'),
        ('gilded_knight', 'Золочёный рыцарь', 'Gilded Knight'), ('rust_knight', 'Ржавый рыцарь', 'Rust Knight'),
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
t(f'tooltip.{M}.flask_shard', 'Правая кнопка, когда фляга в инвентаре: +1 глоток', 'Right click with the flask in your inventory: +1 gulp')
t(f'tooltip.{M}.flask_shard.hearth', 'Или отдохни у очага — вплавится сам', 'Or rest at a hearth: it melts in by itself')
t(f'tooltip.{M}.damage', 'Урон: %s, в твоих руках %s (в спину %s)', 'Damage: %s, in your hands %s (backstab %s)')
t(f'tooltip.{M}.bow.mine', 'Твои стрелы бьют сильнее на %s%%', 'Your arrows hit %s%% harder')
t(f'tooltip.{M}.weight', 'Вес: %s', 'Weight: %s')
t(f'tooltip.{M}.bow', 'Ловкость: %s (урон стрел)', 'Dexterity: %s (arrow damage)')
t(f'tooltip.{M}.set.prisoner', 'Силы восстанавливаются быстрее: +%s%% за вещь', 'Stamina recovers faster: +%s%% per piece')
t(f'tooltip.{M}.set.guard', 'Блок щитом стоит меньше сил: −%s%% за вещь', 'Blocking costs less stamina: −%s%% per piece')
t(f'tooltip.{M}.set.chaplain', 'Заклинания сильнее: +%s%% за вещь', 'Spells are stronger: +%s%% per piece')

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
t(f'message.{M}.flask_given', 'Очаг оставил тебе Флягу тепла', 'The hearth has left you a Flask of Warmth')
t(f'message.{M}.overburdened', 'Слишком тяжело: не перекатиться', 'Too heavy to roll')
t(f'message.{M}.mob_mark', 'Здесь восстаёт: %s', 'Rises here: %s')
t(f'message.{M}.souls_left', 'На месте гибели осталось душ: %s', 'Souls left where you fell: %s')

t(f'banner.{M}.boss_defeated', 'ГАД ПОВЕРЖЕН', 'FIEND FELLED')
t(f'banner.{M}.hearth_lit', 'ОЧАГ РАЗОЖЖЁН', 'HEARTH LIT')
t(f'banner.{M}.souls_retrieved', 'ДУШИ ВОЗВРАЩЕНЫ', 'SOULS RETRIEVED')
# the names of the places of the map, shown when a threshold is crossed
t(f'place.{M}.stockade', 'ВЫШНИЙ ОСТРОГ', 'THE HIGH STOCKADE')
t(f'place.{M}.ward', 'НИЖНИЙ ДВОР', 'THE LOWER WARD')
t(f'place.{M}.bridge', 'ДОЛГИЙ МОСТ', 'THE LONG BRIDGE')
t(f'place.{M}.hall', 'ПАЛАТА НАДЗИРАТЕЛЯ', "THE OVERSEER'S HALL")
t(f'place.{M}.city', 'ЗАКАТНЫЙ ГОРОД', 'THE SUNSET CITY')
t(f'place.{M}.garden', 'МЁРТВЫЙ САД', 'THE DEAD GARDEN')
t(f'place.{M}.belfry', 'ЗВОННИЦА', 'THE BELFRY')
t(f'place.{M}.cathedral', 'СОБОР ПОСЛЕДНЕГО СВЕТА', 'CATHEDRAL OF THE LAST LIGHT')
t(f'place.{M}.crypt', 'КРИПТА ОСНОВАТЕЛЯ', "THE FOUNDER'S CRYPT")
t(f'place.{M}.edge', 'ТЕРРАСА НАД ДОЛИНОЙ', 'THE TERRACE ABOVE THE VALE')
t(f'block.{M}.place_name', 'Порог с именем места', 'Named Threshold')
t(f'place.{M}.posad', 'МЁРТВЫЙ ПОСАД', 'THE DEAD BOROUGH')
t(f'place.{M}.market', 'ТОРГ', 'THE MARKET')
t(f'place.{M}.shambles', 'МЯСНОЙ РЯД', 'THE SHAMBLES')
t(f'place.{M}.churchyard', 'ПОГОСТ', 'THE CHURCHYARD')
t(f'place.{M}.road', 'ОСТРОЖНАЯ ДОРОГА', 'THE STOCKADE ROAD')
t(f'block.{M}.shard_heap', 'Тлеющий осколок', 'Glowing Shard')
t(f'message.{M}.shard_found', 'Осколок фляги. У очага он вплавится во флягу сам.', 'A flask shard. At a hearth it will melt into the flask by itself.')
t(f'screen.{M}.forge.tab.weapons', 'Оружие', 'Weapons')
t(f'screen.{M}.forge.tab.shields', 'Щиты', 'Shields')
t(f'screen.{M}.forge.tab.armour', 'Броня', 'Armour')
t(f'screen.{M}.forge.tab.magic', 'Чары', 'Magic')
t(f'screen.{M}.forge.tab.supplies', 'Припасы', 'Supplies')
t(f'hud.{M}.lost', 'потеряно: %s', 'lost: %s')

t(f'screen.{M}.shop', 'Товары ключника', "The Keykeeper's Wares")
t(f'screen.{M}.shop.leave', 'Уйти', 'Leave')
t(f'screen.{M}.forge', 'Старая наковальня', 'The Old Anvil')
t(f'screen.{M}.forge.make', 'Ковать', 'Make')
t(f'screen.{M}.hearth', 'Очаг', 'Hearth')
t(f'screen.{M}.hearth.leave', 'Встать', 'Get up')
t(f'screen.{M}.hearth.level', 'Уровень: %s', 'Level: %s')
t(f'screen.{M}.hearth.souls', 'Души: %s', 'Souls: %s')
t(f'screen.{M}.hearth.cost', 'Следующее очко стоит: %s', 'The next point costs: %s')
t(f'screen.{M}.hearth.load', 'Ноша: %s / %s — %s', 'Load: %s / %s, %s')
t(f'burden.{M}.light', 'лёгкая', 'light')
t(f'burden.{M}.medium', 'средняя', 'medium')
t(f'burden.{M}.heavy', 'тяжёлая', 'heavy')
t(f'burden.{M}.over', 'непосильная', 'too much')
t(f'screen.{M}.hearth.hint', 'Наведи на характеристику, чтобы узнать, что она даёт', 'Point at a stat to see what it does')
for key, ru, en, ru_d, en_d in (
        ('vitality', 'Живучесть', 'Vitality', 'Одно сердце за каждое очко', 'One heart per point'),
        ('endurance', 'Выносливость', 'Endurance', 'Больше запас сил и посильная ноша', 'More stamina, and more that can be carried'),
        ('strength', 'Сила', 'Strength', 'Урон оружия с буквой у Силы', 'Damage of weapons with a Strength grade'),
        ('dexterity', 'Ловкость', 'Dexterity', 'Урон оружия с буквой у Ловкости', 'Damage of weapons with a Dexterity grade'),
        ('intellect', 'Разум', 'Intellect', 'Сила чародейства и, вполовину, пиромантии', 'Power of sorcery and, by half, of pyromancy'),
        ('faith', 'Вера', 'Faith', 'Сила чудес и, вполовину, пиромантии', 'Power of miracles and, by half, of pyromancy')):
    t(f'stat.{M}.{key}', ru, en)
    t(f'stat.{M}.{key}.desc', ru_d, en_d)

t(f'dialogue.{M}.line', '%s: %s', '%s: %s')
for i, (ru, en) in enumerate((
        ('Шаги... Живые шаги. Давно тут никто не ходил на своих двоих.', 'Steps... Living steps. Nobody has walked here on their own two feet for a long time.'),
        ('Я слеп, но ключи помню на ощупь. Вот этот — от твоей удачи. Шучу. Держи лучше вот это.',
         'I am blind, but I know my keys by touch. This one opens your luck. A joke. Take this instead.'),
        ('Из фляги пей, когда прижмёт. Опустеет — посиди у очага, она наполнится сама.',
         'Drink from the flask when things get bad. When it is empty, sit by a hearth and it will fill itself.'),
        ('И не геройствуй: души, что носишь с собой, остаются там, где упадёшь. Успеешь вернуться — подберёшь.',
         'And do not play the hero: the souls you carry stay where you fall. Come back in time and they are yours again.'))):
    t(f'dialogue.{M}.keykeeper.first.{i}', ru, en)
for i, (ru, en) in enumerate((
        ('Могильщик всё копает на погосте. Весь посад уложил, а остановиться не может.',
         'The Gravedigger still digs in the churchyard. He has laid the whole borough down and cannot stop.'),
        ('Услышишь колокольчик сторожа — бей его первым. Иначе сбежится вся улица.',
         "If you hear a watchman's bell, strike him first. Otherwise the whole street comes running."),
        ('Псы быстрые, да хлипкие. Закройся щитом, дождись прыжка — и бей.',
         'The hounds are quick but frail. Raise your shield, wait for the leap, then strike.'),
        ('Стражники прячутся за щитом. Жди, пока ударят сами, или заходи со спины.',
         'The guards hide behind their shields. Wait for them to strike, or get behind them.'),
        ('Перекатывайся. В мои годы уже не выходит, а тебе — в самый раз.', 'Roll. At my age it does not work any more, but you are just right for it.'),
        ('Дорога за погостом ведёт к Острогу. Там солнце никак не сядет, а у нас никак не взойдёт.',
         'The road past the churchyard leads to the Stockade. Up there the sun will not set, and down here it will not rise.'),
        ('Видишь, тлеет в золе? Это осколок фляги. Подбери — глотком больше будет.',
         'See it glowing in the ash? That is a flask shard. Pick it up: one more gulp for you.'),
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
    'entity.hound.ambient': ('Пёс рычит', 'Hound growls'), 'entity.hound.hurt': ('Пёс взвизгивает', 'Hound yelps'),
    'entity.hound.death': ('Пёс скулит и стихает', 'Hound whines and stills'), 'entity.hound.bite': ('Щёлкают зубы', 'Jaws snap'),
    'entity.watchman.alarm': ('Сторож бьёт тревогу', 'Watchman raises the alarm'),
    'entity.gravedigger.ambient': ('Могильщик дышит сквозь мешок', 'Gravedigger breathes through his sack'),
    'entity.gravedigger.hurt': ('Могильщик глухо мычит', 'Gravedigger grunts'), 'entity.gravedigger.death': ('Могильщик ложится в землю', 'Gravedigger goes to ground'),
    'entity.gravedigger.dig': ('Заступ входит в землю', 'A spade bites the earth'), 'entity.gravedigger.raise': ('Могилы шевелятся', 'The graves stir'),
    'entity.keykeeper.ambient': ('Ключник кашляет', 'Keykeeper coughs'), 'entity.keykeeper.give': ('Звенят ключи', 'Keys jingle'),
    'ui.death': ('Ты помер', 'You croaked'), 'ui.boss_defeated': ('Гад повержен', 'Fiend felled'), 'ui.hearth_lit': ('Очаг разгорается', 'Hearth flares up'),
    'ui.level_up': ('Сила прибывает', 'Strength grows'), 'ui.souls_gain': ('Души стекаются', 'Souls gather'),
    'ui.souls_retrieve': ('Души возвращаются', 'Souls return'), 'ui.place': ('Новое место', 'A new place'),
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
