#!/usr/bin/env python3
"""Every text of the mod in one place: key -> (Russian, English). Writes ru_ru.json and en_us.json. Run from the repo root."""
import json
import os

A = 'src/main/resources/assets'
M = 'unfading_dawn'
T = {}


def t(key, ru, en):
    T[key] = (ru, en)


t(f'itemGroup.{M}', 'Немеркнущий рассвет', 'The Unfading Dawn')
t(f'key.category.{M}.main', 'Немеркнущий рассвет', 'The Unfading Dawn')
t(f'key.{M}.roll', 'Перекат', 'Roll')

t(f'block.{M}.hearth', 'Очаг', 'Hearth')
t(f'block.{M}.fog_gate', 'Туманная завеса', 'Fog Gate')
t(f'block.{M}.forge', 'Старая наковальня', 'Old Anvil')
t(f'block.{M}.mob_mark', 'Метка восставшего', 'Mark of the Risen')
t(f'block.{M}.locked_gate', 'Запертая решётка', 'Locked Grate')
t(f'block.{M}.clock_socket', 'Гнездо часов', 'Socket of the Clock')
t(f'block.{M}.tower_end', 'Конец дня', 'The End of the Day')

for key, ru, en in (
        ('broken_sword', 'Обломок меча', 'Broken Sword'), ('shiv', 'Заточка', 'Shiv'), ('dagger', 'Кинжал', 'Dagger'),
        ('shortsword', 'Короткий меч', 'Shortsword'), ('longsword', 'Длинный меч', 'Longsword'), ('broadsword', 'Палаш', 'Broadsword'),
        ('sabre', 'Сабля', 'Sabre'), ('hatchet', 'Топорик', 'Hatchet'), ('battle_axe', 'Боевой топор', 'Battle Axe'), ('club', 'Дубина', 'Club'),
        ('flanged_mace', 'Шестопёр', 'Flanged Mace'), ('rust_greatsword', 'Ржавый двуручник', 'Rust Greatsword'),
        ('great_key', 'Великий ключ', 'Great Key'), ('censer_flail', 'Кадило на цепи', 'Censer Flail'), ('short_bow', 'Короткий лук', 'Short Bow'),
        ('plank_shield', 'Щит из досок', 'Plank Shield'), ('round_shield', 'Круглый щит', 'Round Shield'),
        ('guard_shield', 'Щит стражника', "Guard's Shield"), ('tower_shield', 'Башенный щит', 'Tower Shield'),
        ('carver_buckler', 'Кулачный щит тёсчика', "Carver's Buckler"), ('scale_shield', 'Щит из чешуи', 'Scale Shield'),
        ('waker_board', 'Било будильщика', "Waker's Board"), ('kiln_door', 'Заслонка овина', 'Kiln Door'),
        ('millstone_shield', 'Жёрнов Пустомола', "The Hollow Miller's Stone"),
        ('scholar_staff', 'Посох школяра', "Scholar's Staff"), ('prayer_beads', 'Чётки', 'Prayer Beads'),
        ('ember_glove', 'Тлеющая рукавица', 'Ember Glove'),
        ('soul_shard', 'Осколок души', 'Soul Shard'), ('heavy_soul_shard', 'Тяжёлый осколок души', 'Heavy Soul Shard'), ('mend', 'Исцеление', 'Mend'),
        ('rebuke', 'Отпор', 'Rebuke'), ('fire_orb', 'Огненный ком', 'Fire Orb'), ('flare', 'Огненная ладонь', 'Burning Palm'),
        ('soul_scatter', 'Россыпь душ', 'Soul Scatter'), ('root_snare', 'Корень из-под ног', 'Root from Underfoot'),
        ('marsh_fire', 'Болотный огонь', 'Marsh Fire'), ('hot_wax', 'Горячий воск', 'Hot Wax'),
        ('dry_wind', 'Суховей', 'Dry Wind'), ('sun_patch', 'Солнечное пятно', 'Patch of Sun'),
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
        ('quarry_pick', 'Кирка ломщика', "Quarryman's Pick"), ('great_maul', 'Кувалда Десятника', "Taskmaster's Maul"),
        ('eater_claw', 'Коготь Камнееда', "Stone-Eater's Claw"), ('iron_wedge', 'Железный клин', 'Iron Wedge'),
        ('powder_pot', 'Пороховой горшок', 'Powder Pot'), ('quarry_key', 'Ключ от Ломовых ворот', "Key to the Quarrymen's Gate"),
        ('taskmaster_soul', 'Душа Десятника', 'Soul of the Taskmaster'), ('stone_eater_soul', 'Душа Камнееда', 'Soul of the Stone-Eater'),
        ('mire_key', 'Ключ от Топких ворот', 'Key to the Mire Gate'),
        ('boat_hook', 'Багор перевозчика', "Ferryman's Boat Hook"), ('wallower_fang', 'Зуб Омутника', "Wallower's Fang"),
        ('withe_scourge', 'Плакучая плеть', 'Weeping Scourge'), ('bone_scale', 'Костяная чешуя', 'Bone Scale'), ('withe', 'Ивовая лоза', 'Withe'),
        ('mud_glob', 'Ком тины', 'Clot of Sludge'), ('wisp_flame_bolt', 'Болотный огонь', 'Marsh Fire'),
        ('wallower_soul', 'Душа Омутника', 'Soul of the Wallower'), ('mother_soul', 'Душа Плакучей', 'Soul of the Weeping Mother'),
        ('firwood_key', 'Ключ от Лесной калитки', 'Key to the Wood Wicket'),
        ('bleached_linen', 'Белёный холст', 'Bleached Linen'), ('candle_wax', 'Свечной воск', 'Candle Wax'),
        ('shatun_hide', 'Шкура шатуна', "Shatun's Hide"),
        ('vigil_staff', 'Посох бдящего', 'Staff of the Wakeful'), ('waker_mallet', 'Колотушка будильщика', "Waker's Mallet"),
        ('bear_spear', 'Рогатина', 'Bear Spear'), ('great_candle', 'Большая свеча Старца', "The Elder's Great Candle"),
        ('shatun_soul', 'Душа Шатуна', 'Soul of the Shatun'), ('elder_soul', 'Душа Старца', 'Soul of the Elder'),
        ('vigil_cowl', 'Куколь неусыпающего', 'Cowl of the Unsleeping'), ('vigil_shift', 'Рубаха неусыпающего', 'Shift of the Unsleeping'),
        ('vigil_skirt', 'Подол неусыпающего', 'Skirt of the Unsleeping'), ('vigil_wraps', 'Онучи неусыпающего', 'Footwraps of the Unsleeping'),
        ('stone_hammer', 'Молот каменотёса', "Stonecutter's Hammer"), ('fish_knife', 'Рыбацкий нож', "Fisher's Knife"),
        ('mason_cap', 'Шапка каменотёса', "Stonecutter's Cap"), ('mason_apron', 'Фартук каменотёса', "Stonecutter's Apron"),
        ('mason_breeches', 'Порты каменотёса', "Stonecutter's Breeches"), ('mason_boots', 'Сапоги каменотёса', "Stonecutter's Boots"),
        ('scale_hood', 'Чешуйчатый куколь', 'Scale Hood'), ('scale_coat', 'Чешуйчатая рубаха', 'Scale Coat'),
        ('scale_leggings', 'Чешуйчатые порты', 'Scale Leggings'), ('scale_waders', 'Бродни', 'Waders'),
        ('shore_key', 'Ключ от Морских ворот', 'Key to the Sea Gate'), ('stubble_key', 'Ключ от Жнецких ворот', "Key to the Reapers' Gate"),
        ('sailcloth', 'Парусина', 'Sailcloth'), ('tarred_rope', 'Смолёный канат', 'Tarred Rope'), ('crab_shell', 'Панцирь клешняка', "Sand Crab's Shell"),
        ('cargo_hook', 'Крюк крючника', "Dockhand's Hook"), ('harpoon', 'Гарпун', 'Harpoon'), ('oar', 'Весло карбаса', "Karbas Oar"),
        ('lead_line', 'Лот лоцмана', "Pilot's Sounding Lead"), ('ship_anchor', 'Якорь «Зари»', 'Anchor of the Daybreak'),
        ('capsized_claw', 'Клешня Перевёртыша', 'Claw of the Capsized'),
        ('helmsman_soul', 'Душа Кормчего', 'Soul of the Helmsman'), ('capsized_soul', 'Душа Перевёртыша', 'Soul of the Capsized'),
        ('rye_straw', 'Ржаная солома', 'Rye Straw'), ('flour_sack', 'Мешок муки', 'Sack of Flour'),
        ('reaper_sickle', 'Серп жнеца', "Reaper's Sickle"), ('scythe', 'Коса', 'Scythe'), ('thresh_flail', 'Цеп', 'Threshing Flail'),
        ('warden_pole', 'Жердь Сторожа', "The Warden's Pole"),
        ('reaper_hat', 'Соломенная шляпа', 'Straw Hat'), ('reaper_shirt', 'Страдная рубаха', 'Harvest Shirt'),
        ('reaper_breeches', 'Порты жнеца', "Reaper's Breeches"), ('reaper_bast', 'Лапти', 'Bast Shoes'),
        ('warden_soul', 'Душа Палёного сторожа', 'Soul of the Scorched Warden'), ('miller_soul', 'Душа Пустомола', 'Soul of the Hollow Miller'),
        ('bolt_soul', 'Душа Затвора', 'Soul of the Bolt'), ('coming_soul', 'Душа Грядущего', 'Soul of the Coming'),
        ('depths_key', 'Ключ от Нижней решётки', 'Key to the Lower Grate'),
        ('clock_key', 'Ключ часов', 'Key of the Clock'),
        ('oilskin_souwester', 'Зюйдвестка', "Sou'wester"), ('oilskin_smock', 'Промасленная роба', 'Oilskin Smock'),
        ('oilskin_trousers', 'Промасленные порты', 'Oilskin Trousers'), ('oilskin_boots', 'Бахилы', 'Sea Boots'),
        ('tattered_cloth', 'Ветошь', 'Tattered Cloth'), ('rust_scrap', 'Ржавый лом', 'Rust Scrap')):
    t(f'item.{M}.{key}', ru, en)

# the charms: one is worn at a time (Charm.java); the number in the line is the charm's own
CHARMS = (('coffin_nail', 'Гробовой гвоздь', 'Coffin Nail', 'Мёртвые оставляют на %s%% больше душ', 'The dead leave %s%% more souls'),
          ('ash_knot', 'Узелок золы', 'Knot of Ash', 'Глоток из фляги лечит на %s%% больше', 'A gulp of the flask heals %s%% more'),
          ('fetter_link', 'Кандальное звено', 'Fetter Link', 'Посильная ноша больше на %s', 'You can carry %s more'),
          ('candle_stub', 'Соборный огарок', 'Cathedral Candle Stub', 'Заклинания сильнее на %s%%', 'Spells are %s%% stronger'),
          ('wedge_cord', 'Клин на гайтане', 'Wedge on a Cord', 'Блок щитом стоит на %s%% меньше сил', 'Blocking costs %s%% less stamina'),
          ('pike_tooth', 'Щучий зуб', "Pike's Tooth", 'Удар в спину сильнее на %s%%', 'A blow in the back is %s%% harder'),
          ('weeping_herb', 'Плакун-трава', 'Weeping Herb', 'Силы возвращаются на %s%% быстрее', 'Stamina comes back %s%% faster'),
          ('vigil_wax', 'Воск бдения', 'Wax of the Vigil', 'Перекат стоит на %s%% меньше сил', 'A roll costs %s%% less stamina'),
          ('shatun_claw', 'Коготь шатуна', "Shatun's Claw", 'Здоровья больше на %s сердца', '%s hearts more'),
          ('sea_knot', 'Морской узел', "Seaman's Knot", 'Удар оружием стоит на %s%% меньше сил', 'A swing costs %s%% less stamina'),
          ('tide_pearl', 'Жемчужина стоячей воды', 'Pearl of the Slack Tide', 'Здоровье возвращается само: полсердца раз в %s с',
           'Health comes back by itself: half a heart every %s s'),
          ('twin_ear', 'Двойной колос', 'Twin Ear', 'Глоток из фляги не тратится в %s%% случаев', 'A gulp of the flask is not spent %s%% of the time'))
for key, ru, en, ru_d, en_d in CHARMS:
    t(f'item.{M}.{key}', ru, en)
    t(f'tooltip.{M}.charm.{key}', ru_d, en_d)
t(f'tooltip.{M}.charm_use', 'Оберег. Правая кнопка — надеть; носят один', 'A charm. Right click to put it on; one is worn at a time')
t(f'message.{M}.charm_on', 'Надет оберег: %s', 'Charm put on: %s')
t(f'message.{M}.charm_off', 'Оберег снят: %s', 'Charm taken off: %s')
t(f'screen.{M}.hearth.charm.none', 'Оберег не надет', 'No charm is worn')
t(f'screen.{M}.hearth.charm.hint', 'Возьми оберег в руку и нажми правую кнопку', 'Take a charm in your hand and right click')
t(f'screen.{M}.hearth.charm.off', 'Снять', 'Take off')
t(f'screen.{M}.cutscene.skip', 'Пробел - пропустить', 'Space - skip')

MOBS = (('withered_prisoner', 'Истлевший узник', 'Withered Prisoner'), ('withered_guard', 'Истлевший стражник', 'Withered Guard'),
        ('withered_archer', 'Истлевший лучник', 'Withered Archer'), ('withered_acolyte', 'Истлевший послушник', 'Withered Acolyte'),
        ('withered_townsman', 'Истлевший посадский', 'Withered Townsman'), ('withered_butcher', 'Истлевший мясник', 'Withered Butcher'),
        ('withered_watchman', 'Истлевший сторож', 'Withered Watchman'), ('carrion_hound', 'Трупный пёс', 'Carrion Hound'),
        ('gravedigger', 'Могильщик Посада', 'Gravedigger of the Borough'),
        ('withered_digger', 'Истлевший ломщик', 'Withered Digger'), ('withered_barrowman', 'Истлевший тачечник', 'Withered Barrowman'),
        ('withered_sapper', 'Истлевший подрывник', 'Withered Sapper'), ('blind_crawler', 'Слепыш', 'Blind Crawler'), ('unhewn', 'Недотёс', 'The Unhewn'),
        ('taskmaster', 'Десятник Каменоломни', 'Taskmaster of the Quarry'), ('stone_eater', 'Камнеед', 'The Stone-Eater'),
        ('withered_drowned', 'Утопленник', 'The Drowned'), ('bone_pike', 'Костяная щука', 'Bone Pike'), ('lanky', 'Жердяй', 'The Lanky One'),
        ('wisp', 'Болотный огонёк', 'Marsh Wisp'), ('wallower', 'Омутник', 'The Wallower'), ('weeping_mother', 'Плакучая', 'The Weeping Mother'),
        ('wakeful', 'Бдящий', 'The Wakeful'), ('waker', 'Будильщик', 'The Waker'), ('sleeper', 'Задремавший', 'The Dozer'),
        ('waxen', 'Восковик', 'The Waxen'), ('sleep_moth', 'Сонница', 'Sleep Moth'), ('shatun', 'Шатун', 'The Shatun'),
        ('elder', 'Старец Неусыпающих', 'Elder of the Unsleeping'),
        ('dockhand', 'Крючник', 'The Dockhand'), ('harpooner', 'Гарпунщик', 'The Harpooner'), ('oarsman', 'Вёсельник', 'The Oarsman'),
        ('pilot', 'Лоцман', 'The Pilot'), ('sand_crab', 'Клешняк', 'Sand Crab'), ('skua', 'Поморник', 'Skua'),
        ('helmsman', 'Кормчий «Зари»', 'Helmsman of the Daybreak'), ('capsized', 'Перевёртыш', 'The Capsized'),
        ('reaper', 'Жнец', 'The Reaper'), ('mower', 'Косарь', 'The Mower'), ('sackman', 'Мешочник', 'The Sackman'),
        ('scorched', 'Палёный', 'The Scorched'), ('scarecrow', 'Пугало', 'Scarecrow'), ('stook', 'Суслон', 'The Stook'),
        ('sungazer', 'Солнцегляд', 'Sungazer'), ('scorched_warden', 'Палёный сторож', 'The Scorched Warden'),
        ('hollow_miller', 'Пустомол', 'The Hollow Miller'), ('millstone', 'Жёрнов', 'Millstone'),
        ('usher', 'Провожатый', 'The Usher'), ('stray_shadow', 'Отставшая тень', 'Stray Shadow'), ('longarm', 'Долгорукий', 'The Long-Armed'),
        ('handwalker', 'Рукоход', 'Handwalker'), ('deepling', 'Глубинница', 'Deepling'), ('gut', 'Утроба', 'The Gut'),
        ('clockhand', 'Стрелочник', 'The Clockhand'), ('bolt', 'Затвор', 'The Bolt'), ('coming', 'Грядущее', 'The Coming'),
        ('thrown_flour_sack', 'Мешок муки', 'Sack of Flour'), ('flour_cloud', 'Мучная пыль', 'Flour Dust'),
        ('gilded_knight', 'Золочёный рыцарь', 'Gilded Knight'), ('rust_knight', 'Ржавый рыцарь', 'Rust Knight'),
        ('keykeeper', 'Слепой ключник', 'Blind Keykeeper'), ('overseer', 'Надзиратель Острога', 'Overseer of the Stockade'),
        ('chaplain', 'Капеллан Острога', 'Chaplain of the Stockade'))
# ---- the folk (Folk.java): what each says, a topic at a time, as the world allows; the story behind it is in docs/story.md.
# Who says "dawn" (рассвет) speaks for the Founder or from fear or habit; who says "sunset" (закат) speaks against him; the
# Unsleeping say neither and wait for "morning"; the bellman cannot hear what anybody calls it, and asks.
FOLK = {
    'candlewife': ('Свечница', 'The Candlewife', {
        'first': (('Не топчи, тут лежат. Лежали. Я их отпеваю, а они встают. Ну так я снова.',
                   'Mind your feet, they lie here. They did lie. I sing them to their rest and they get up. So I sing again.'),
                  ('Видал, над Острогом рассвет? Основатель его держит, чтоб ночь до нас не дошла. А у нас, внизу, ночь. Значит, не заслужили ещё.',
                   'Have you seen the dawn over the Stockade? The Founder holds it, so the night cannot reach us. And down here it is night. So we have not earned it yet.'),
                  ('Свечу поставить хочешь — ставь. Огарок, он как душа: горит тише, а горит.',
                   'If you want to set a candle, set one. A stub is like a soul: it burns lower, but it burns.')),
        'gravedigger': (('Отпела я Могильщика. Первый за всё время, кто после отпевания лёг и лежит.',
                         'I sang the Gravedigger to his rest. The first in all this time who lay down after and stays down.'),
                        ('Он ведь не хоронил, он поднимал. Кто теперь поднимет? ...Прости. Не знаю, радоваться ли.',
                         'He did not bury, you know, he raised. Who will raise them now? ...Forgive me. I do not know whether to be glad.')),
        'chaplain': (('Капеллан мне свечи давал из собора, от Последнего Света. Говорил: рассвет будет, только стой.',
                      'The Chaplain gave me candles from the cathedral, lit from the Last Light. He said: the dawn will come, only keep standing.'),
                     ('Ты его положил? Ну. Теперь, выходит, я одна стою.', 'You laid him down? Well. Then I am the only one standing now.')),
        'mother': (('С Топи её сняли, говорят. Ту, что сказала: день должен кончиться. Правильно повесили — так все говорили.',
                    'They say she was taken down, out on the Mire. The one who said the day must end. Rightly hanged, everybody said so.'),
                   ('Я свечу ставила, что повесили. За упокой не ставила: она ж не упокоилась. Теперь не знаю, за что ставить.',
                    'I set a candle because she was hanged. Not for her rest: she had none. Now I do not know what to set it for.')),
        'elder': (('Старец лёг? Неусыпающие ко мне ходили за свечами, пока не ушли в ельник. Брали больше, чем сжечь за ночь.',
                   'The Elder lies down? The Unsleeping used to come to me for candles, before they went off to the firwood. They took more than a night can burn.'),
                  ('Я ещё подумала тогда: какую же ночь они собрались ждать.', 'I wondered at the time what night they meant to wait out.')),
        'coming': (('Под землёй, говоришь, что-то убил? Не говори. Не хочу знать, что там под нами.',
                    'You killed something under the ground, you say? Do not tell me. I do not want to know what is under us.'),
                   ('Под нами погост.', 'Under us is the churchyard.')),
        'started': (('Слышишь? Под землёй стучит. Будто пальцем в гроб изнутри... Нет. Ровнее. Как часы.',
                     'Do you hear? Something knocks under the ground. Like a finger on a coffin lid from inside... No. Steadier. Like a clock.'),),
        'raised': (('Башня встала прямо на торгу. Я туда не пойду. Там колокол.',
                    'A tower has stood up in the middle of the market. I will not go in. There is a bell up there.'),
                   ('Колокол — это отпевание всем разом. Я одна столько не отпою.',
                    'A bell is a burial song for everyone at once. I could never sing so many on my own.')),
        'seat': (('Рассвет как стоял, так и стоит. Значит, он там, наверху, опять сидит. Или кто-то за него.',
                  'The dawn stands as it stood. So he sits up there again. Or someone in his place.'),
                 ('Ты не знаешь, кто?', 'You would not know who?')),
        'idle': (('Отпела сегодня троих. Двое уже встали.', 'I sang three to their rest today. Two are up already.'),
                 ('Если у тебя кто здесь лежит — скажи имя. Я тут всех помню. Своих только не помню.',
                  'If someone of yours lies here, tell me the name. I remember everyone here. Only not my own.'),
                 ('Ночь у нас, потому что рассвет весь наверху. На всех не хватило.', 'We have night because all the dawn is up there. There was not enough for everyone.'),
                 ('Ключник слепой, а видит больше нашего. Сказать только не хочет.', 'The keykeeper is blind, and sees more than we do. He just will not say.'),
                 ('Погаснет свеча — от очага не зажигай. Там огонь живой, а им живой ни к чему.',
                  'If a candle goes out, do not light it from a hearth. That fire is alive, and they have no use for living things.'))}),
    'debtor': ('Должник', 'The Debtor', {
        'first': (('Дверь открыта, знаю. Не выйду. Мне дали сто дней.', 'The door is open, I know. I will not go out. They gave me a hundred days.'),
                  ('Сто дней — это сто закатов. А закат тут один, и тот не кончается. Сколько сижу? Ни дня.',
                   'A hundred days is a hundred sunsets. And there is only the one sunset here, and it does not end. How long have I sat here? Not a day.'),
                  ('Должен я. Кому — не скажу, он помер. Хотя тут никто толком не помер.',
                   'I am in debt. To whom, I will not say: he died. Though nobody here has properly died.')),
        'overseer': (('Надзиратель? Он мне всё: «Сиди до рассвета». А я ему: какого рассвета, глаза-то разуй.',
                      'The Overseer? He always told me: "Sit till dawn." And I told him: what dawn, open your eyes.'),
                     ('Ну вот. Не досидел он.', 'Well. He did not sit it out.')),
        'chaplain': (('Капеллан к нам ходил, учил закат рассветом звать. Кто выучил, тем хлеба давали.',
                      'The Chaplain used to come down here and teach us to call the sunset dawn. Those who learnt it got bread.'),
                     ('Я до сих пор голодный.', 'I am hungry still.')),
        'mother': (('Ту, что на Топи повесили, сюда сперва привели. В соседнюю камеру. Говорила громко, на весь двор: «День должен кончиться».',
                    'The one they hanged on the Mire, they brought her here first. The next cell. She said it loud, for the whole ward to hear: "The day must end."'),
                   ('Я молчал. Я вообще молчу. У меня срок.', 'I kept quiet. I keep quiet altogether. I have my term.')),
        'wharf': (('Внизу у моря, говорят, утро настоящее. Не верю. Утро — это когда потом бывает день.',
                   'Down by the sea, they say, there is a real morning. I do not believe it. Morning is when a day comes after it.'),),
        'coming': (('Говорят, он день остановил, чтоб завтра не пришло. Что там должно было прийти — тебе виднее, ты ходил.',
                    'They say he stopped the day so that tomorrow would not come. What was meant to come, you know better: you went there.'),
                   ('А по мне, остановил, чтоб долги не платить. Завтра — это когда с тебя спрашивают.',
                    'To my mind he stopped it so as not to pay his debts. Tomorrow is when they come to collect.')),
        'started': (('Часы пошли? Значит, и мой срок пошёл.', 'The clock runs? Then so does my term.'),
                    ('Сто дней. Ничего. Я привычный.', 'A hundred days. Never mind. I am used to it.')),
        'seat': (('Опять закат стоит. А я уж считать собрался.', 'The sunset stands again. And I had just started counting.'),
                 ('Видно, кто-то сел на его место. Мне-то что. Я и так сижу.', 'Somebody has taken his seat, it seems. What is it to me. I sit anyway.')),
        'idle': (('Дверь не трогай. Не я её открыл, не мне и закрывать.', 'Leave the door. I did not open it, and it is not mine to close.'),
                 ('С последним возом камня привезли и ключ от Ломовых ворот. Отнесли наверх, в город. Туда всё лучшее носят.',
                  "The key to the Quarrymen's Gate came up with the last load of stone. They took it on up, to the city. Everything good goes up there."),
                 ('Закат. Я говорю — закат. Пусть слышат.', 'Sunset. I say sunset. Let them hear.'),
                 ('Кормили, пока было чем. Потом перестали. А голод не прошёл. Тоже застрял.',
                  'They fed us while there was something to feed us. Then they stopped. But the hunger did not pass. It is stuck too.'),
                 ('Ты тоже чей-то должник, по глазам видно. Только не знаешь, чей.', 'You owe someone too, I can see it in your eyes. You just do not know whom.'))}),
    'carver': ('Тёсчик', 'The Carver', {
        'first': (('Не заслоняй. Свету и так никакого.', 'Do not stand in my light. There is little enough of it.'),
                  ('Лицо режу. Основателево. Велено к рассвету. Так говорилось — «к рассвету». Ну, к рассвету и режу.',
                   'I am carving a face. The Founder\'s. To be done by dawn, the order was. That is how they said it, "by dawn". So by dawn I am carving it.'),
                  ('Нос помню. Нос был. Остальное камень помнит лучше меня, да не скажет.',
                   'The nose I remember. There was a nose. The rest the stone remembers better than I do, and it will not tell.')),
        'taskmaster': (('Десятника нет больше? Он клинья бил, чтоб мы не спали. Хороший был десятник.',
                        'No more Taskmaster? He drove wedges so we would not sleep. A good taskmaster he was.'),
                       ('Лица тоже не помнил. Всё кричал: «Похоже! Похоже!»', 'He did not remember the face either. He kept shouting: "That is it! That is him!"')),
        'stone_eater': (('Камнеед лёг. Он белый камень жрал, тот, что на голову шёл. Мы думали — вредит.',
                         'The Stone-Eater lies down. He ate the white stone, the stone for the head. We thought he meant harm.'),
                        ('А может, просто есть хотел. Тут все чего-то хотят.', 'Or maybe he was just hungry. Everyone here wants something.')),
        'coming': (('Под карьером, говоришь, был? Мы туда штрек вели, за камнем почище. И дорубились.',
                    'You have been under the pit, you say? We drove a gallery down there, after cleaner stone. And we cut through.'),
                   ('Внизу небо, понимаешь? Под землёй — небо. Десятник велел заложить решёткой и молчать.',
                    'There is sky down there, do you understand? Sky, under the ground. The Taskmaster had it barred with a grate and told us to keep quiet.'),
                   ('Я и молчу. Тебе вот только сказал.', 'And I keep quiet. I have only told you.')),
        'started': (('Стучит под ногами. Ровно, как молоток у хорошего мастера. Я так и не научился.',
                     'Something knocks underfoot. Steady, like the mallet of a good mason. I never learnt to.'),),
        'raised': (('Башню видел? Ровная, ни одного скола. Не мы рубили.', 'Have you seen the tower? True as a plumb line, not a chip on it. We did not cut it.'),
                   ('Кто ж её рубил-то?', 'Then who did?')),
        'seat': (('Опять рассвет? Ну и ладно. Значит, ещё не поздно дотесать.', 'Dawn again? Good enough. Then it is not too late to finish.'),),
        'idle': (('Голову в стене видел? Это мы. Сто человек сто дней. Дней, правда, не было.',
                  'Have you seen the head in the wall? That is us. A hundred men, a hundred days. Only there were no days.'),
                 ('Порох в карьере держи подальше от огня. Да ты знаешь.', 'Keep the powder in the pit away from fire. You know that.'),
                 ('Не помнишь лица — режь своё. Никто не заметит. Я раз так... Нет. Не скажу.',
                  'If you cannot remember a face, carve your own. Nobody will notice. Once I... No. I will not say.'),
                 ('Белый камень мягкий, пока свежий. А свежее у нас всё. Ничего не стареет.',
                  'White stone is soft while it is fresh. And everything is fresh here. Nothing gets old.'),
                 ('Слепыши в штольнях слушают. Ступай мягко, а лучше вовсе не ступай.',
                  'The blind ones in the drifts listen. Tread softly, or better, do not tread at all.'))}),
    'ferryman': ('Перевозчик', 'The Ferryman', {
        'first': (('Перевезти? Куда, через что? Вода стоит, лодку не держит, людей нет. Садись, раз пришёл.',
                   'Take you across? Where to, over what? The water stands, it will not hold a boat, and there is nobody. Sit, since you came.'),
                  ('Я на тот берег возил. Тот берег теперь там же, где этот.', 'I used to take people to the far bank. The far bank is where this one is now.'),
                  ('Тут полдень. Солнце над виселицей встало и не сходит. В Остроге им закат, а нам — вот.',
                   'It is noon here. The sun stopped over the gallows and will not move. The Stockade got the sunset, and we got this.')),
        'wallower': (('Омутник лёг. Он воду держал, чтоб не спала. А может, вода его.', 'The Wallower lies down. He held the water so it would not sleep. Or it held him.'),
                     ('Глядишь, опустится теперь на палец.', 'It may sink a finger now, you will see.')),
        'mother': (('Сняли её? Я видел, как вешали. Палач руки тёр: не идёт, говорит, верёвка. Не затягивается.',
                    'She is down? I saw them hang her. The hangman kept rubbing his hands: the rope will not go, he said. It will not tighten.'),
                   ('Полдень встал — и она так и осталась. Ни живая, ни как надо.', 'Noon stopped, and she stayed as she was. Not alive, and not the other thing either.'),
                   ('Она одно говорила: день должен кончиться. Я так же думаю. Только громко не говорю — у меня лодка.',
                    'She said only one thing: the day must end. I think so too. I just do not say it loud: I have a boat to think of.')),
        'elder': (('Неусыпающие на казнь приходили. Стояли на гати, смотрели. Один ключ от своей калитки нёс — чтоб, значит, никто за ними не пошёл.',
                   'The Unsleeping came to the hanging. They stood on the log road and watched. One of them carried the key to their wicket, so nobody would follow them.'),
                  ('Обронил в роще, а вернуться побоялся. Они полудня боятся: в полдень спать тянет.',
                   'He dropped it in the grove and was afraid to go back. They fear noon: noon makes you drowsy.')),
        'helmsman': (('Кормчий «Зари»? Знавал. Звал меня в море. Я ему: я по реке. А он: какая разница, вода везде стоит.',
                      'The helmsman of the Daybreak? I knew him. He asked me out to sea. I said: I am a river man. And he said: what is the difference, the water stands everywhere.'),
                     ('Прав был, выходит.', 'He was right, it seems.')),
        'coming': (('Что ты под землёй сделал — не знаю. А вода сегодня дрогнула. Первый раз с того полудня.',
                    'Whatever you did under the ground, I do not know. But the water stirred today. The first time since that noon.'),),
        'started': (('Часы, говоришь? Значит, скоро вечер. Пойду лодку гляну.', 'A clock, you say? Then evening is near. I will go and look at the boat.'),),
        'seat': (('Опять полдень. А я уж вёсла достал. Ну, положу обратно.', 'Noon again. And I had the oars out already. Well, I will put them back.'),),
        'idle': (('Лодку видишь? Привязана. Отвязывать не стану: уплывёт, а куда? Некуда.',
                  'You see the boat? Tied up. I will not untie it: it would drift off, and where to? Nowhere.'),
                 ('Гати держат, пока по ним идёшь. Не стой на месте.', 'The log roads hold while you keep walking. Do not stand still.'),
                 ('Огоньки на болоте — не люди. Люди сюда не ходят.', 'The lights on the marsh are not people. People do not come here.'),
                 ('Взял бы с тебя душу за перевоз. Да перевоза нет. Тут все переправы даром, потому что никуда.',
                  'I would take a soul from you for the crossing. Only there is no crossing. All the ferries are free here, because they go nowhere.'),
                 ('В Остроге, говорят, рассвет. Я там бывал. Закат там. Красный, как вода в омуте.',
                  'They say there is a dawn at the Stockade. I have been there. It is a sunset. Red as the water in a pool.'))}),
    'hangman': ('Палач', 'The Hangman', {
        'first': (('К верёвке не подходи. Казнь идёт.', 'Keep away from the rope. There is a hanging on.'),
                  ('С полудня идёт. Велено было: до заката повесить. А заката нет, рассвет — так велено звать. Значит, не кончил.',
                   'It has been on since noon. The order was: hanged by sundown. But there is no sundown, it is dawn, that is what we are to call it. So I am not done.'),
                  ('Она там, на своём острове. Корнями обросла. Я жду. Палач всегда ждёт, пока кончится.',
                   'She is out there on her island. Grown over with roots. I wait. A hangman always waits for it to be over.')),
        'mother': (('Кончилась?.. Ну вот. Казнь окончена.', 'It is over?.. Well then. The hanging is done.'),
                   ('Знаешь, что она мне последним сказала? «Ты увидишь закат». Вот, вижу.', 'Do you know the last thing she said to me? "You will see the sunset." There. I see it.'),
                   ('Пойду. Нечего мне тут больше. Верёвку не трогай. Она теперь просто верёвка.',
                    'I am going. There is nothing for me here any more. Do not touch the rope. It is only a rope now.')),
        'idle': (('Она сказала: день должен кончиться. Я сказал: не мне решать. Вот и не решаю.',
                  'She said the day must end. I said it was not for me to decide. So I do not decide.'),
                 ('Верёвку я менял. Трижды. Не держит её никакая.', 'I changed the rope. Three times. No rope will hold her.'),
                 ('Свечница в посаде за неё свечу ставит. За то, что повесили. Добрая женщина.',
                  'The candlewife in the borough sets a candle for her. For her being hanged. A kind woman.'),
                 ('Рассвет. Я говорю — рассвет. Мне так сказано.', 'Dawn. I say dawn. That is what I was told to say.'))}),
    'cellarer': ('Келарь', 'The Cellarer', {
        'first': (('Тихо. Считаю.', 'Quiet. I am counting.'),
                  ('Я веду счёт дням. Сколько свечей сожгли — столько ночей. Выходит три тысячи с лишним. А над елями ни разу не посветлело.',
                   'I keep the count of the days. As many candles burnt, so many nights. That makes three thousand and more. And not once has it grown light over the firs.'),
                  ('Не сходится. Ты не спишь? Хорошо. Здесь нельзя.', 'It does not add up. You are not asleep? Good. Here one must not.')),
        'shatun': (('Шатуна нет? Его у ворот на цепи держали, чтоб и он не спал. Раз мы не спим — не спит ничто. Записал: зверь. Одна строка.',
                    'No more Shatun? They kept him chained at the gate so that he would not sleep either. If we do not sleep, nothing sleeps. I have written: the beast. One line.'),),
        'mother': (('На казнь в Топь наших ходило трое. Старец послал: поглядеть, кончится ли день. Не кончился.',
                    'Three of ours went to the hanging on the Mire. The Elder sent them to see whether the day would end. It did not.'),
                   ('Один ключ потерял, от калитки. Я его записал в убыль.', 'One of them lost a key, the key of the wicket. I wrote it off as lost.')),
        'elder': (('Старец уснул?.. Записываю: Старец уснул. Первая запись, после которой хочется закрыть книгу.',
                   'The Elder has fallen asleep?.. I write: the Elder has fallen asleep. The first entry after which I want to close the book.'),
                  ('Обет он с нас брал до утра. Утро не пришло, а обет есть. Обет без утра — просто бессонница.',
                   'He took our vow until the morning. The morning did not come, but the vow is still here. A vow without a morning is only sleeplessness.')),
        'wharf': (('Ключ от Морских ворот у нас в свечной лежал. Принесли из порта, на сохранение: где не спят, там и стерегут.',
                   'The key to the Sea Gate lay here in the candle store. They brought it from the port for safekeeping: where nobody sleeps, there is the best watch.'),
                  ('Ты ходил туда. Что там?.. Нет. Не говори. Запишу, что не знаю.', 'You have been there. What is there?.. No. Do not tell me. I will write that I do not know.')),
        'coming': (('Ты убил то, что должно было прийти. Значит, утро могло прийти? Или оно и было тем, что придёт? Пишу вопросом.',
                    'You have killed what was meant to come. Then could the morning have come? Or was the morning what was coming? I am writing it as a question.'),),
        'started': (('Часы пошли. Я сверил: мой счёт с ними расходится на три тысячи дней. Кто-то из нас врёт.',
                     'The clock runs. I have checked: my count and theirs differ by three thousand days. One of us lies.'),),
        'seat': (('Над елями так и не светлеет. Что ж. Двенадцать свечей на ночь. Пиши дальше.',
                  'It does not grow light over the firs after all. So. Twelve candles a night. Write on.'),),
        'idle': (('Воск берём у тех, кто уже не встанет. Они не против. Они вообще ни против чего.',
                  'We take the wax from those who will not get up again. They do not mind. They do not mind anything.'),
                 ('Сонниц руками не трогай. Пыль у них — сон, а сон здесь — та же смерть, только стыдная.',
                  'Do not touch the sleep moths. Their dust is sleep, and sleep here is the same as death, only shameful.'),
                 ('Будильщики бьют всякого, кто голову клонит. Меня ни разу. Я голову в книге держу.',
                  'The wakers strike anyone who lets his head droop. Me, never. I keep my head in the book.'),
                 ('Сколько ты здесь? Скажи, запишу. У нас каждый день на счету, хоть их и нет.',
                  'How long have you been here? Tell me, and I will write it down. Every day counts with us, though there are none.'))}),
    'bellman': ('Звонарь', 'The Bellman', {
        'first': (('А? Громче говори. Звонарь я. Глухой. Все звонари глухие, кто долго звонит.',
                   'Eh? Speak up. I am the bellman. Deaf. All bellmen go deaf who ring long enough.'),
                  ('Колокол этот не бил с того дня. Велено ждать. Скажут бить — ударю. Ты сказать пришёл?',
                   'That bell has not struck since that day. I was told to wait. If they tell me to strike, I strike. Have you come to tell me?'),
                  ('Что там в небе-то? Мне отсюда не видно.', 'What is in the sky out there? I cannot see it from here.')),
        'seat': (('Сел кто-то в кресло? Я почуял: камень дрогнул. Ну, значит, не бить.', 'Has somebody sat in the seat? I felt it: the stone shook. Well, then I do not strike.'),
                 ('Что там в небе-то?', 'What is in the sky out there?')),
        'idle': (('Что там в небе-то?', 'What is in the sky out there?'),
                 ('Верёвка наверху. Тяни сам, коли велено. Мне никто ничего не велит. Не слышу.',
                  'The rope is up there. Pull it yourself if you were told to. Nobody tells me anything. I cannot hear.'),
                 ('Он наверху сидел, в кресле. Может, и сейчас сидит. Я ведь не слышу.', 'He used to sit up there, in the seat. Maybe he still does. I would not hear.'),
                 ('Ударишь — я почую. Звон, его ногами слышно.', 'If you strike it, I will feel it. You hear a bell with your feet.'),
                 ('Раньше колокол к вечерне звал. Вечерни нет, а колокол есть.', 'The bell used to call to evensong. There is no evensong now, but there is the bell.'))}),
}
for who, (ru_name, en_name, talk) in FOLK.items():
    MOBS += ((who, ru_name, en_name),)
    for topic, lines in talk.items():
        for i, (ru, en) in enumerate(lines):
            t(f'dialogue.{M}.{who}.{topic}.{i}', ru, en)

for key, ru, en in MOBS:
    t(f'entity.{M}.{key}', ru, en)
    t(f'item.{M}.{key}_spawn_egg', f'Яйцо призыва: {ru[0].lower() + ru[1:]}', f'{en} Spawn Egg')
t(f'entity.{M}.soul_stain', 'Пятно душ', 'Soul Stain')
t(f'entity.{M}.spell_bolt', 'Заклинание', 'Spell')
t(f'entity.{M}.tar_pot', 'Горшок смолы', 'Tar Pot')
t(f'entity.{M}.powder_pot', 'Пороховой горшок', 'Powder Pot')
t(f'entity.{M}.thrown_wedge', 'Железный клин', 'Iron Wedge')
t(f'entity.{M}.thrown_harpoon', 'Гарпун', 'Harpoon')
t(f'entity.{M}.thrown_anchor', 'Якорь «Зари»', 'Anchor of the Daybreak')
t(f'entity.{M}.gear', 'Шестерня', 'Clock Wheel')
t(f'entity.{M}.tendril', 'Рука Грядущего', 'Arm of the Coming')
t(f'entity.{M}.coming_face', 'Лик Грядущего', 'Face of the Coming')
t(f'entity.{M}.colossus', 'Явившееся над морем', 'What Stood Up Out of the Sea')
t(f'entity.{M}.dolly', 'Глаз', 'Eye')
t(f'entity.{M}.wall_eye', 'Око в скале', 'Eye in the Rock')

t(f'tooltip.{M}.scaling', 'Сила: %s   Ловкость: %s', 'Strength: %s   Dexterity: %s')
t(f'tooltip.{M}.stamina', 'Расход сил: %s', 'Stamina cost: %s')
t(f'tooltip.{M}.backstab', 'Удар в спину: ×%s', 'Backstab: ×%s')
t(f'tooltip.{M}.stability', 'Устойчивость: %s%%', 'Stability: %s%%')
t(f'tooltip.{M}.shield.buckler', 'Поднятый, не мешает силам возвращаться', 'Held up, it does not slow the return of stamina')
t(f'tooltip.{M}.shield.light', 'Лёгкий: вдвое легче щита стражника', "Light: half the weight of a guard's shield")
t(f'tooltip.{M}.shield.board', 'Отбрасывает того, кто по нему ударил', 'Throws back whoever strikes it')
t(f'tooltip.{M}.shield.door', 'Поднятая, держит огонь: за ней не горят', 'Held up, it keeps fire off: nobody burns behind it')
t(f'tooltip.{M}.shield.stone', 'Камень: держит любой удар и весит, как жёрнов', 'Stone: it holds any blow, and weighs what a millstone weighs')
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
t(f'tooltip.{M}.set.vigil', 'Сон и морок отпускают раньше: −%s%% за вещь', 'Sleep and murk let go sooner: −%s%% per piece')
t(f'tooltip.{M}.set.mason', 'Взрыв и падающий камень бьют слабее: −%s%% за вещь', 'Blasts and falling rock hurt less: −%s%% per piece')
t(f'tooltip.{M}.set.scale', 'Вода меньше держит ноги: −%s%% за вещь', 'Water holds the legs less: −%s%% per piece')
t(f'tooltip.{M}.set.oilskin', 'Удар отбрасывает слабее: −%s%% за вещь', 'A blow throws you less far: −%s%% per piece')
t(f'tooltip.{M}.set.reaper', 'Огонь жжёт слабее: −%s%% за вещь', 'Fire hurts less: −%s%% per piece')
t(f'tooltip.{M}.great_candle', 'Горит: кого ударит, тот в огне %s с', 'It is alight: what it strikes burns for %s s')

t(f'spell.{M}.soul_shard.desc', 'Быстрый сгусток бледного света', 'A quick bolt of pale light')
t(f'spell.{M}.heavy_soul_shard.desc', 'Медленный и тяжёлый: бьёт вдвое больнее', 'Slow and heavy: hits twice as hard')
t(f'spell.{M}.mend.desc', 'Лечит после короткой молитвы', 'Heals after a short prayer')
t(f'spell.{M}.rebuke.desc', 'Отбрасывает всех, кто рядом', 'Throws back everyone nearby')
t(f'spell.{M}.fire_orb.desc', 'Ком огня, рвётся там, где упадёт', 'A ball of fire that bursts where it lands')
t(f'spell.{M}.flare.desc', 'Язык пламени из ладони на три шага; обходит щит', 'A tongue of flame three paces from the palm; it goes round a shield')
t(f'spell.{M}.soul_scatter.desc', 'Пять осколков веером: вблизи в цель идут все', 'Five shards in a fan: up close all of them land')
t(f'spell.{M}.root_snare.desc', 'Корень выходит под тем, на кого смотришь: бьёт и держит', 'A root comes up under whoever you look at: it strikes and holds')
t(f'spell.{M}.marsh_fire.desc', 'Медленный огонёк: сам сворачивает к врагу и долго горит на нём', 'A slow flame: it bends its own way to an enemy and burns on it for long')
t(f'spell.{M}.dry_wind.desc', 'Сухой ветер из ладони: бьёт слабо, зато отбрасывает и раздувает то, что горит', 'A dry wind from the palm: it hurts little, but it throws back what it meets and fans what burns')
t(f'spell.{M}.sun_patch.desc', 'Пятно света там, куда смотришь: миг собирается и жжёт всех, кто в нём остался', 'A patch of light where you look: it gathers for a moment, and burns whoever is still in it')
t(f'spell.{M}.hot_wax.desc', 'Струя воска из ладони: жжёт слабо, зато схватывается на ногах', 'A gush of wax from the palm: it burns little, but sets on the legs')

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
# (the Founder's word for it: whoever walks in under this name sees the sun going down)
t(f'place.{M}.city', 'РАССВЕТНЫЙ ГОРОД', 'THE DAWN CITY')
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
t(f'place.{M}.quarry', 'БЕЛЫЙ КАРЬЕР', 'THE WHITE PIT')
t(f'place.{M}.drifts', 'ШТОЛЬНИ', 'THE DRIFTS')
t(f'place.{M}.carvers', 'МАСТЕРСКАЯ ТЁСЧИКОВ', "THE CARVERS' SHOP")
t(f'place.{M}.wheel', 'КОЛЁСНЫЙ ЗАЛ', 'THE WHEEL HALL')
t(f'place.{M}.workings', 'НИЖНИЕ ВЫРАБОТКИ', 'THE LOWER WORKINGS')
t(f'place.{M}.breach', 'ПРОЛОМ', 'THE BREACH')
t(f'message.{M}.gate_locked.quarry', 'Заперто. Камень отсюда возили в Острог — там и ключ.', 'Locked. The stone from here went to the Stockade; so did the key.')
t(f'message.{M}.gate_opened.quarry', 'Ломовые ворота отперты', "The Quarrymen's Gate is unlocked")
t(f'message.{M}.gate_locked.mire', 'Заперто. Из-под решётки тянет тиной. Ключ унесло туда, где глубже некуда.',
  'Locked. It smells of the bog under the grate. The key went where nothing lies deeper.')
t(f'message.{M}.gate_opened.mire', 'Топкие ворота отперты', 'The Mire Gate is unlocked')
t(f'place.{M}.mire', 'ТОПЬ', 'THE MIRE')
t(f'place.{M}.log_roads', 'ГАТИ', 'THE LOG ROADS')
t(f'place.{M}.weir', 'ЗАПРУДА', 'THE WEIR')
t(f'place.{M}.withy', 'ИВНЯК', 'THE WITHY WOOD')
t(f'place.{M}.isle', 'ОСТРОВ ПЛАКУЧЕЙ', 'THE ISLE OF THE WEEPING ONE')
# the Black Firwood, north-east of the fortress road: the hour just before a dawn that does not come
t(f'place.{M}.firwood', 'ЧЁРНЫЙ ЕЛЬНИК', 'THE BLACK FIRWOOD')
t(f'place.{M}.den', 'ШАТУНОВА ПАДЬ', "THE SHATUN'S HOLLOW")
t(f'place.{M}.skete', 'СКИТ НЕУСЫПАЮЩИХ', 'THE SKETE OF THE UNSLEEPING')
t(f'place.{M}.vigil', 'БДЕНИЕ', 'THE VIGIL')
t(f'place.{M}.white_tent', 'БЕЛЫЙ ШАТЁР', 'THE WHITE TENT')
t(f'message.{M}.gate_locked.firwood', 'Заперто. Из-за решётки тянет хвоей и воском. Ключ унесли на казнь — и назад не принесли.',
  'Locked. It smells of fir and wax beyond the grate. The key was carried off to a hanging, and never brought back.')
t(f'message.{M}.gate_opened.firwood', 'Лесная калитка отперта', 'The Wood Wicket is unlocked')
# the Morning Wharf, south of the yard of the first hearth: the sun just clear of the sea, and it does not climb. (The
# one dawn of this country that is one - nobody at the Stockade calls it that.)
t(f'place.{M}.wharf', 'УТРЕННЯЯ ПРИСТАНЬ', 'THE MORNING WHARF')
t(f'place.{M}.ship', 'КОЧ «ЗАРЯ»', 'THE COG "DAYBREAK"')
t(f'place.{M}.mole', 'СТАРЫЙ МОЛ', 'THE OLD MOLE')
t(f'place.{M}.spit', 'ДОЛГАЯ КОСА', 'THE LONG SPIT')
t(f'place.{M}.boulders', 'КАМЕННАЯ ГРЯДА', 'THE BOULDER STRAND')
t(f'place.{M}.shoal', 'ОТМЕЛЬ', 'THE SHOAL')
t(f'place.{M}.lighthouse', 'МАЯК', 'THE LIGHTHOUSE')
t(f'message.{M}.gate_locked.shore', 'Заперто. Из-под створок тянет солью и смолой. Ключ хранили там, где не спят.',
  'Locked. Salt and tar on the draught under the leaves. The key was kept where nobody sleeps.')
t(f'message.{M}.gate_opened.shore', 'Морские ворота отперты', 'The Sea Gate is unlocked')
# the Stubble, west of the borough's wall: the afternoon of a harvest that is never in
t(f'place.{M}.stubble', 'ЖНИВЬЁ', 'THE STUBBLE')
t(f'place.{M}.watch_field', 'СТОРОЖЕВОЕ ПОЛЕ', 'THE WATCHED FIELD')
t(f'place.{M}.threshing', 'ГУМНО', 'THE THRESHING YARD')
t(f'place.{M}.great_mill', 'БОЛЬШАЯ МЕЛЬНИЦА', 'THE GREAT MILL')
# the Sky Beneath
t(f'place.{M}.under_sky', 'ИСПОДНЕЕ НЕБО', 'THE SKY BENEATH')
t(f'place.{M}.still_water', 'СТОЯЧАЯ ВОДА', 'THE STILL WATER')
t(f'place.{M}.clockworks', 'НУТРО ЧАСОВ', 'THE WORKS OF THE CLOCK')
t(f'place.{M}.causeways', 'ГАТИ НАД НЕБОМ', 'THE CAUSEWAYS OVER THE SKY')
t(f'place.{M}.tear', 'РАЗРЫВ', 'THE TEAR')
t(f'message.{M}.gate_locked.stubble', 'Заперто. Из-за решётки тянет сухим теплом и соломой. Ключ унесли к морю — встречать тех, кому везли хлеб.',
  'Locked. Dry heat and straw on the draught through the grate. The key went down to the sea, to meet those the bread was for.')
t(f'message.{M}.gate_opened.stubble', 'Жнецкие ворота отперты', "The Reapers' Gate is unlocked")
t(f'message.{M}.undertow', 'Тянет на дно', 'The sea is taking you under')
t(f'block.{M}.shallows', 'Мелкая вода', 'Shallows')
t(f'block.{M}.sea_shallows', 'Вода на песке', 'Water on the Sand')
t(f'block.{M}.night_shallows', 'Стоячая вода', 'Still Water')
t(f'block.{M}.under_sky', 'Исподнее небо', 'Sky Beneath')
t(f'block.{M}.sky_veil', 'Звёздная мгла', 'Starry Mist')
t(f'block.{M}.sky_lid', 'Исподнее небо', 'Sky Beneath')
t(f'block.{M}.sludge', 'Тина', 'Sludge')
t(f'entity.{M}.mud_glob', 'Ком тины', 'Clot of Sludge')
t(f'entity.{M}.root', 'Корень Плакучей', 'Root of the Weeping Mother')
t(f'message.{M}.gate_locked.none', 'Заперто. Ключа от этого замка здесь нет.', 'Locked. The key to this lock is not in these parts.')
t(f'message.{M}.gate_opened.none', 'Решётка поддалась', 'The grate gives way')
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
t(f'screen.{M}.hearth.travel', 'Очаги…', 'Hearths…')
t(f'screen.{M}.travel', 'От очага к очагу', 'From Hearth to Hearth')
t(f'screen.{M}.travel.go', 'В путь', 'Go')
t(f'screen.{M}.travel.here', 'ты здесь', 'you are here')
t(f'screen.{M}.travel.back', 'Назад', 'Back')
t(f'screen.{M}.travel.hint', 'Очаги, у которых ты отдыхал', 'The hearths you have rested at')
t(f'message.{M}.hearth_gone', 'Того очага больше нет.', 'That hearth is no more.')
for key, ru, en in (('none', 'Очаг (%s, %s, %s)', 'A hearth (%s, %s, %s)'), ('yard', 'Двор первого очага', 'The Yard of the First Hearth'),
                    ('lodge', 'Сторожка у погоста', "The Sexton's Lodge"), ('keeper', 'Привратницкая Острога', 'The Stockade Gate'),
                    ('smithy', 'Кузня на площади', 'The Smithy on the Square'), ('sacristy', 'Ризница собора', 'The Cathedral Sacristy'),
                    ('terrace', 'Терраса над долиной', 'The Terrace above the Vale'), ('maw', 'Под Зевом', 'Under the Maw'),
                    ('sump', 'Зумпф', 'The Sump'), ('ferry', 'Изба перевозчика', "The Ferryman's Hut"), ('mill', 'Мельница на запруде', 'The Mill on the Weir'),
                    ('old_fir', 'Под старой елью', 'Under the Old Fir'), ('skete', 'Околица скита', "The Skete's Edge"),
                    ('harbour', 'Под обрывом', 'Under the Cliff'), ('karbas', 'Под перевёрнутым карбасом', 'Under the Upturned Karbas'),
                    ('lighthouse', 'Маяк', 'The Lighthouse'),
                    ('balk', 'У межи', 'By the Balk'), ('kiln', 'Овин', 'The Drying Barn'), ('granary', 'Мельничный амбар', "The Miller's Granary"),
                    ('brink', 'Над обрывом', 'Over the Brink'), ('works', 'За маятником', 'Behind the Pendulum'), ('tear', 'У Разрыва', 'By the Tear'),
                    ('reliquary', 'Свечная кладовая', 'The Candle Store')):
    t(f'hearth.{M}.{key}', ru, en)
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
        ('Потратил бы ты души у очага. Мёртвым они ни к чему.', 'Spend your souls at a hearth. The dead have no use for them.'),
        ('Ломовые ворота, что в конце Нижней улицы? Заперты с тех пор, как камень возить перестали. Ключ ушёл в Острог с последним возом. Не мой это ключ.',
         "The Quarrymen's Gate, at the end of Low Street? Shut since the stone stopped coming. Its key went up to the Stockade with the last load. Not one of mine."),
        ('В каменоломне, говорят, есть такие, что глаз не имеют. Те слушают. Мимо них — крадучись.',
         'They say there are some in the quarry that have no eyes. Those listen. Go past them creeping.'))):
    t(f'dialogue.{M}.keykeeper.idle.{i}', ru, en)

# ---- the clock under the Sky Beneath, the tower it raises, and the two ends of the day
t(f'place.{M}.clock_hall', 'ЗАЛ ЧАСОВ', 'THE CLOCK HALL')
t(f'place.{M}.clock_tower', 'ЧАСОВАЯ БАШНЯ', 'THE CLOCK TOWER')
t(f'banner.{M}.clock_started', 'ЧАСЫ ПОШЛИ', 'THE CLOCK RUNS')
t(f'message.{M}.clock.filled', 'В гнезде уже лежит ключ', 'A key already lies in this socket')
t(f'message.{M}.clock.empty.quarry', 'Гнездо со знаком каменоломни. Ему нужен ключ от Ломовых ворот', "The socket bears the quarry's sign. It wants the key to the Quarrymen's Gate")
t(f'message.{M}.clock.empty.mire', 'Гнездо со знаком топи. Ему нужен ключ от Топких ворот', 'The socket bears the sign of the mire. It wants the key to the Mire Gate')
t(f'message.{M}.clock.empty.firwood', 'Гнездо со знаком ельника. Ему нужен ключ от Лесной калитки', 'The socket bears the sign of the firwood. It wants the key to the Wood Wicket')
t(f'message.{M}.clock.empty.shore', 'Гнездо со знаком пристани. Ему нужен ключ от Морских ворот', 'The socket bears the sign of the wharf. It wants the key to the Sea Gate')
t(f'message.{M}.clock.empty.stubble', 'Гнездо со знаком жнивья. Ему нужен ключ от Жнецких ворот', "The socket bears the sign of the stubble. It wants the key to the Reapers' Gate")
t(f'message.{M}.clock.empty.depths', 'Гнездо со знаком глубин. Ему нужен ключ от Нижней решётки', 'The socket bears the sign of the depths. It wants the key to the Lower Grate')
t(f'message.{M}.clock.empty.keeper', 'Гнездо без знака, только ключ вырезан. Такой ключ хранят в посаде', 'The socket has no sign, only the cut of a key. Such a key is kept in the borough')
t(f'message.{M}.clock.set', 'Ключ встал в гнездо: %s из %s', 'The key sets into its socket: %s of %s')
t(f'message.{M}.clock.started', 'Под землёй что-то сдвинулось и пошло. Наверху, на посаде, поднимается башня.', 'Something under the ground gave way and began to move. Up in the borough, a tower is rising.')
t(f'message.{M}.clock.rope_slack', 'Верёвка висит без дела', 'The rope hangs slack')
t(f'message.{M}.clock.rope', 'Верёвка идёт тяжело, как будто её тянут из-под земли.', 'The rope comes heavy, as if pulled up from under the ground.')
t(f'message.{M}.clock.seat_taken', 'Кресло остыло и пусто', 'The seat has gone cold and empty')
t(f'message.{M}.clock.seat', 'Ты садишься в кресло Основателя. Камень тёплый.', "You sit in the Founder's seat. The stone is warm.")
t(f'message.{M}.clock.silence', 'Стало тихо. Так тихо, что слышно, как не ходят мёртвые.', 'It has gone quiet. So quiet that you can hear the dead not walking.')
t(f'screen.{M}.ending.continue', 'Дальше', 'Go on')
t(f'ending.{M}.1.0', 'Верёвка идёт тяжело, и колокол над головой нехотя трогается.', 'The rope comes heavy, and the bell overhead stirs unwillingly.')
t(f'ending.{M}.1.1', 'Он не бил с того дня, как Основатель сел в своё кресло. Теперь бьёт.', 'It has not struck since the day the Founder sat in his seat. It strikes now.')
t(f'ending.{M}.1.2', 'Раз. Закат, что стоял над Острогом, не гаснет, а осыпается, как зола.', 'Once. The sunset that stood over the Stockade does not fade; it crumbles like ash.')
t(f'ending.{M}.1.3', 'Два. Те, кто держался на ногах одним упрямством, ложатся там, где стояли.', 'Twice. Those who held to their feet by stubbornness alone lie down where they stood.')
t(f'ending.{M}.1.4', 'Три. Звонарь внизу поднимает голову. «Слышу, — говорит он. — Вот и всё».', 'Thrice. The Bellman below lifts his head. "I hear it," he says. "That is all, then."')
t(f'ending.{M}.1.5', 'День кончился. Не сразу и не больно: просто больше нечему идти.', 'The day is over. Not at once and not painfully: there is simply nothing left to go on.')
t(f'ending.{M}.1.6', 'А над морем, где ничего не было, встаёт чёрный круг с янтарным ободом и венцом рук.', 'And over the sea, where there was nothing, a black disc rises with an amber rim and a crown of hands.')
t(f'ending.{M}.1.7', 'Руки тянутся вниз. Не к тебе. К тому, что осталось.', 'The hands reach down. Not for you. For what is left.')
t(f'ending.{M}.1.title', 'КОНЕЦ ДНЯ', 'THE END OF THE DAY')
t(f'ending.{M}.1.subtitle', 'Закат погас.', 'The sunset is out.')
t(f'ending.{M}.2.0', 'Кресло каменное, а тёплое. Как будто только что встали.', 'The seat is stone, and warm. As if someone had just got up.')
t(f'ending.{M}.2.1', 'Ты садишься. Колокол над головой не качнётся.', 'You sit. The bell overhead does not sway.')
t(f'ending.{M}.2.2', 'Свет тот же: тот же закат за зубцами, та же тишина на лестнице.', 'The light is the same: the same sunset behind the battlements, the same hush on the stair.')
t(f'ending.{M}.2.3', 'Внизу всё как было: мёртвые ходят, живые боятся, часы стучат под землёй.', 'Below, all is as it was: the dead walk, the living fear, the clock ticks under the ground.')
t(f'ending.{M}.2.4', 'С лестницы Звонарь спрашивает, что там в небе. Он глух и ответа не услышит.', 'From the stair the Bellman asks what is in the sky. He is deaf and will not hear an answer.')
t(f'ending.{M}.2.5', '«Рассвет», — говоришь ты всё равно.', '"Dawn," you say anyway.')
t(f'ending.{M}.2.6', 'Делать больше нечего, только сидеть. Часы идут, а завтра не придёт.', 'There is nothing more to do but sit. The clock runs, and tomorrow will not come.')
t(f'ending.{M}.2.title', 'РАССВЕТ', 'DAWN')
t(f'ending.{M}.2.subtitle', 'Немеркнущий.', 'Unfading.')

for i, (ru, en) in enumerate((
        ('Грядущий лёг? Слышал, как земля вздохнула. Значит, и мне пора отдать, что держал.',
         'The Coming is down? I heard the ground sigh. Then it is time for me to give up what I kept.'),
        ('Этот ключ не от ворот. Он от часов, что идут под вашими сапогами. Седьмое гнездо — моё, остальные шесть вы сами собрали.',
         'This key opens no gate. It is for the clock that runs beneath your boots. The seventh socket is mine; the other six you gathered yourself.'),
        ('Вставишь — часы пойдут. Что они поднимут, не знаю. Я слеп, а башен не видел и зрячим.',
         'Set it, and the clock will run. What it raises I cannot say. I am blind, and I never saw towers even when I could.'))):
    t(f'dialogue.{M}.keykeeper.clock.{i}', ru, en)

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
    'entity.quarry.pick': ('Кирка бьёт в камень', 'A pick strikes stone'), 'entity.quarry.blast': ('Рвётся порох', 'Powder goes off'),
    'entity.quarry.fuse': ('Шипит фитиль', 'A fuse hisses'), 'entity.quarry.rockfall': ('Рушится камень', 'Rock comes down'),
    'entity.quarry.barrow': ('Гремит тачка', 'A barrow rattles'),
    'entity.crawler.ambient': ('Слепыш щёлкает', 'Crawler clicks'), 'entity.crawler.hurt': ('Слепыш шипит', 'Crawler hisses'),
    'entity.crawler.death': ('Слепыш затихает', 'Crawler goes still'), 'entity.crawler.screech': ('Слепыш визжит', 'Crawler shrieks'),
    'entity.unhewn.ambient': ('Камень скрежещет', 'Stone grinds'), 'entity.unhewn.hurt': ('Камень крошится', 'Stone chips'),
    'entity.unhewn.death': ('Статуя рассыпается', 'A statue falls apart'), 'entity.unhewn.wake': ('Статуя оживает', 'A statue wakes'),
    'entity.unhewn.slam': ('Глыба бьёт в пол', 'A block strikes the floor'), 'entity.unhewn.step': ('Камень переваливается', 'Stone lurches'),
    'entity.taskmaster.ambient': ('Десятник сопит', 'Taskmaster wheezes'), 'entity.taskmaster.hurt': ('Десятник рычит', 'Taskmaster growls'),
    'entity.taskmaster.death': ('Десятник падает', 'Taskmaster falls'), 'entity.taskmaster.shout': ('Десятник орёт на свод', 'Taskmaster bellows at the roof'),
    'entity.taskmaster.maul': ('Кувалда бьёт в пол', 'A maul strikes the floor'), 'entity.taskmaster.wedge': ('Звенит клин', 'A wedge rings'),
    'entity.taskmaster.crack': ('Трещит скала', 'The rock splits'),
    'entity.stone_eater.ambient': ('Камнеед ворчит', 'Stone-Eater rumbles'), 'entity.stone_eater.hurt': ('Камнеед ревёт от боли', 'Stone-Eater bellows in pain'),
    'entity.stone_eater.death': ('Камнеед издыхает', 'Stone-Eater dies'), 'entity.stone_eater.roar': ('Камнеед ревёт', 'Stone-Eater roars'),
    'entity.stone_eater.burrow': ('Что-то роет камень', 'Something digs through rock'), 'entity.stone_eater.erupt': ('Пол взрывается', 'The floor bursts'),
    'entity.stone_eater.bite': ('Лязгают челюсти', 'Jaws clash'),
    'entity.pike.ambient': ('Щёлкают костяные челюсти', 'Bone jaws click'), 'entity.pike.hurt': ('Щука бьётся', 'Pike thrashes'),
    'entity.pike.death': ('Щука всплывает брюхом', 'Pike goes belly up'), 'entity.pike.bite': ('Щука хватает', 'Pike snaps'),
    'entity.pike.leap': ('Щука прыгает из воды', 'Pike leaps from the water'), 'entity.pike.flop': ('Рыба бьётся о доски', 'A fish flops on the boards'),
    'entity.lanky.ambient': ('Жердяй скрипит', 'Lanky One creaks'), 'entity.lanky.hurt': ('Жердяй стонет', 'Lanky One groans'),
    'entity.lanky.death': ('Жердяй валится в воду', 'Lanky One topples into the water'), 'entity.lanky.slam': ('Длинная рука бьёт по воде', 'A long arm strikes the water'),
    'entity.lanky.step': ('Чавкает трясина', 'The mire squelches'),
    'entity.wisp.ambient': ('Огонёк вздыхает', 'Wisp sighs'), 'entity.wisp.hurt': ('Огонёк мечется', 'Wisp darts'),
    'entity.wisp.death': ('Огонёк гаснет', 'Wisp goes out'), 'entity.wisp.cast': ('Огонёк разгорается', 'Wisp flares'),
    'entity.wallower.ambient': ('Омутник булькает', 'Wallower gurgles'), 'entity.wallower.hurt': ('Омутник ревёт от боли', 'Wallower bellows in pain'),
    'entity.wallower.death': ('Омутник идёт ко дну', 'Wallower sinks'), 'entity.wallower.roar': ('Омутник ревёт', 'Wallower roars'),
    'entity.wallower.bite': ('Захлопывается пасть', 'A maw snaps shut'), 'entity.wallower.slap': ('Хвост бьёт по воде', 'A tail slaps the water'),
    'entity.wallower.spit': ('Омутник харкает', 'Wallower hawks'), 'entity.wallower.plop': ('Шлёпается тина', 'Sludge splatters'),
    'entity.wallower.dive': ('Омутник уходит под воду', 'Wallower goes under'), 'entity.wallower.wake': ('Рябь идёт по воде', 'A ripple crosses the water'),
    'entity.wallower.breach': ('Омутник вырывается из воды', 'Wallower bursts from the water'), 'entity.wallower.gulp': ('Омутник втягивает воздух', 'Wallower draws breath'),
    'entity.weeping_mother.ambient': ('Плакучая плачет', 'Weeping Mother weeps'), 'entity.weeping_mother.hurt': ('Плакучая вскрикивает', 'Weeping Mother cries out'),
    'entity.weeping_mother.death': ('Плакучая падает', 'Weeping Mother falls'), 'entity.weeping_mother.wail': ('Плакучая воет', 'Weeping Mother wails'),
    'entity.weeping_mother.lash': ('Свистит ветвь', 'A bough whistles'), 'entity.weeping_mother.slam': ('Ветви бьют в землю', 'Boughs strike the ground'),
    'entity.weeping_mother.creak': ('Скрипит ствол', 'A bole creaks'), 'entity.weeping_mother.root': ('Корень рвёт землю', 'A root tears the ground'),
    'entity.sect.ambient': ('Неусыпающий бормочет', 'One of the Unsleeping mutters'), 'entity.sect.hurt': ('Неусыпающий охает', 'One of the Unsleeping gasps'),
    'entity.sect.death': ('Неусыпающий засыпает', 'One of the Unsleeping falls asleep'),
    'entity.wakeful.rise': ('Бдящий встаёт с колен', 'Wakeful rises from his knees'), 'entity.wakeful.vigil': ('Бдящий подаёт голос', 'Wakeful raises his voice'),
    'entity.waker.knock': ('Стучит било', 'A board is beaten'),
    'entity.sleeper.cry': ('Задремавший вскрикивает', 'Dozer cries out'), 'entity.sleeper.fall': ('Задремавший валится', 'Dozer drops'),
    'entity.sleeper.snore': ('Кто-то храпит', 'Somebody snores'),
    'entity.waxen.ambient': ('Булькает воск', 'Wax gurgles'), 'entity.waxen.hurt': ('Восковик чавкает', 'Waxen squelches'),
    'entity.waxen.death': ('Восковик оплывает', 'Waxen runs down'), 'entity.waxen.slam': ('Шлёпает воск', 'Wax slaps down'),
    'entity.waxen.gather': ('Восковик набирает воска', 'Waxen draws up wax'), 'entity.waxen.spew': ('Льётся горячий воск', 'Hot wax pours'),
    'entity.sleep_moth.flutter': ('Шуршат крылья', 'Wings rustle'), 'entity.sleep_moth.hurt': ('Сонница мечется', 'Sleep moth darts'),
    'entity.sleep_moth.death': ('Сонница падает', 'Sleep moth drops'), 'entity.sleep_moth.dust': ('Сыплется сонная пыль', 'Sleeping dust sifts down'),
    'entity.fieldhand.ambient': ('Сухо дышит страдник', 'Fieldhand breathes dry'), 'entity.fieldhand.hurt': ('Страдник вскрикивает', 'Fieldhand cries out'),
    'entity.fieldhand.death': ('Страдник валится в стерню', 'Fieldhand falls in the stubble'), 'entity.fieldhand.sickle': ('Свистит серп', 'A sickle hisses'),
    'entity.fieldhand.scythe': ('Идёт коса', 'A scythe comes round'), 'entity.fieldhand.sack': ('Летит мешок', 'A sack flies'),
    'entity.scorched.ambient': ('Потрескивает палёный', 'Scorched one crackles'), 'entity.scorched.hurt': ('Шипит палёный', 'Scorched one hisses'),
    'entity.scorched.death': ('Палёный рассыпается', 'Scorched one falls to ash'), 'entity.scorched.flare': ('Палёный вспыхивает', 'Scorched one flares'),
    'entity.scarecrow.creak': ('Скрипит перекладина', 'A crossbar creaks'), 'entity.scarecrow.hurt': ('Сыплется солома', 'Straw spills'),
    'entity.scarecrow.death': ('Пугало разваливается', 'Scarecrow comes apart'), 'entity.scarecrow.wake': ('Пугало слезает с шеста', 'Scarecrow gets down'),
    'entity.scarecrow.spin': ('Пугало вертится', 'Scarecrow turns'),
    'entity.stook.rustle': ('Шуршит суслон', 'Stook rustles'), 'entity.stook.hurt': ('Суслон сыплется', 'Stook sheds'),
    'entity.stook.death': ('Суслон разваливается', 'Stook falls apart'), 'entity.stook.fall': ('Суслон валится', 'Stook topples'),
    'entity.stook.shed': ('Суслон трясёт мякину', 'Stook shakes out chaff'),
    'entity.sungazer.hum': ('Звенит зной', 'Heat hums'), 'entity.sungazer.hurt': ('Хрустит стебель', 'A stalk cracks'),
    'entity.sungazer.death': ('Солнцегляд ломается', 'Sungazer snaps'), 'entity.sungazer.gaze': ('Солнцегляд поднимает лицо', 'Sungazer lifts its face'),
    'entity.sungazer.lash': ('Хлещет лист', 'A leaf lashes'),
    'entity.warden.ambient': ('Скрипит крест', 'A cross creaks'), 'entity.warden.hurt': ('Сторож трещит', 'Warden cracks'),
    'entity.warden.death': ('Сторож догорает', 'Warden burns out'), 'entity.warden.sweep': ('Идёт жердь', 'A pole comes round'),
    'entity.warden.slam': ('Жердь бьёт оземь', 'A pole strikes the ground'), 'entity.warden.kindle': ('Сторож занимается', 'Warden catches fire'),
    'entity.warden.roar': ('Сторож ревёт', 'Warden roars'),
    'entity.sun.gather': ('Собирается свет', 'Light gathers'), 'entity.sun.burn': ('Жжёт солнце', 'The sun burns'),
    'entity.usher.ambient': ('Шуршат пальцы', 'Fingers rustle'),
    'entity.usher.hurt': ('Провожатый вздрагивает', 'Usher shudders'),
    'entity.usher.death': ('Провожатый складывается', 'Usher folds up'),
    'entity.usher.open': ('Ладони раскрываются', 'Palms open'),
    'entity.usher.take': ('Провожатый берёт', 'Usher takes hold'),
    'entity.usher.reach': ('Длинная рука идёт вниз', 'A long arm comes down'),
    'entity.usher.step': ('Неспешный шаг', 'An unhurried step'),
    'entity.shadow.ambient': ('Шепчет тень', 'A shadow whispers'),
    'entity.shadow.hurt': ('Тень рвётся', 'A shadow tears'),
    'entity.shadow.death': ('Тень уходит в пол', 'A shadow soaks away'),
    'entity.shadow.rise': ('Тень встаёт', 'A shadow stands up'),
    'entity.shadow.claw': ('Тень когтит', 'A shadow claws'),
    'entity.shadow.snatch': ('Тень хватает за ноги', 'A shadow snatches'),
    'entity.longarm.ambient': ('Скребётся в стене', 'Something scrapes in the wall'),
    'entity.longarm.hurt': ('Долгорукий дёргается', 'Long-armed one flinches'),
    'entity.longarm.death': ('Руки обвисают', 'Arms go slack'),
    'entity.longarm.reach': ('Руки расправляются', 'Arms unfold'),
    'entity.longarm.take': ('Долгорукий тянет', 'Long-armed one pulls'),
    'entity.handwalker.ambient': ('Шлёпают ладони', 'Palms patter'),
    'entity.handwalker.hurt': ('Рукоход шипит', 'Handwalker hisses'),
    'entity.handwalker.death': ('Рукоход сворачивается', 'Handwalker curls up'),
    'entity.handwalker.rear': ('Рукоход встаёт на дыбы', 'Handwalker rears'),
    'entity.handwalker.whip': ('Хлещет хвост', 'A tail whips'),
    'entity.handwalker.step': ('Топочут ладони', 'Palms run'),
    'entity.deepling.ambient': ('Булькает глубинница', 'Deepling gurgles'),
    'entity.deepling.hurt': ('Глубинница дёргается', 'Deepling jerks'),
    'entity.deepling.death': ('Глубинница всплывает брюхом', 'Deepling goes belly up'),
    'entity.deepling.wake': ('Под огоньком раскрывается пасть', 'A mouth opens under the light'),
    'entity.deepling.gulp': ('Клацает пасть', 'Jaws snap'),
    'entity.deepling.glare': ('Из пасти смотрят', 'Something looks out of a mouth'),
    'entity.gut.ambient': ('Утроба сопит под панцирем', 'Gut breathes under its shell'),
    'entity.gut.hurt': ('Утроба ревёт', 'Gut bellows'),
    'entity.gut.death': ('Утроба валится', 'Gut goes down'),
    'entity.gut.bellow': ('Утроба трубит', 'Gut trumpets'),
    'entity.gut.step': ('Шлёпают ладони', 'Palms slap the ground'),
    'entity.gut.crash': ('Утроба врезается', 'Gut crashes'),
    'entity.gut.slam': ('Утроба рушится оземь', 'Gut comes down'),
    'entity.gut.gape': ('Панцирь раскрывается', 'A shell opens'),
    'entity.gut.shell': ('Удар глохнет в панцире', 'A blow dies on a shell'),
    'entity.clockhand.hurt': ('Звенит пружина', 'A spring twangs'),
    'entity.clockhand.death': ('Стрелочник рассыпается', 'Clockhand comes apart'),
    'entity.clockhand.tick': ('Тик', 'Tick'),
    'entity.clockhand.stab': ('Колет минутная стрелка', 'A minute hand thrusts'),
    'entity.clockhand.sweep': ('Идёт часовая стрелка', 'An hour hand comes round'),
    'entity.clockhand.wind': ('Стрелочник заводится', 'Clockhand winds itself'),
    'entity.bolt.ambient': ('Гудит янтарь', 'Amber hums'),
    'entity.bolt.hurt': ('Звенит линза', 'A lens rings'),
    'entity.bolt.death': ('Затвор раскалывается', 'The Bolt splits'),
    'entity.bolt.tock': ('Так', 'Tock'),
    'entity.bolt.strike': ('Маятник сбивает', 'The pendulum strikes'),
    'entity.bolt.toll': ('Бьют часы', 'A clock tolls'),
    'entity.bolt.chain': ('Гремят цепи гирь', 'Weight chains rattle'),
    'entity.bolt.weight': ('Падает гиря', 'A weight falls'),
    'entity.bolt.strain': ('Стонет стержень', 'A rod groans'),
    'entity.bolt.snap': ('Стержень лопается', 'A rod parts'),
    'entity.bolt.stab': ('Колет стрелка', 'A hand thrusts'),
    'entity.bolt.sweep': ('Вертятся стрелки', 'Hands whirl'),
    'entity.bolt.land': ('Затвор рушится сверху', 'The Bolt comes down'),
    'entity.bolt.step': ('Цокают стрелки', 'Hands click on stone'),
    'entity.coming.ambient': ('Гудит исподнее небо', 'The sky beneath hums'),
    'entity.coming.hurt': ('Грядущее вздрагивает', 'The Coming shudders'),
    'entity.coming.death': ('Грядущее уходит в Разрыв', 'The Coming goes back through'),
    'entity.coming.gaze': ('Грядущее смотрит', 'The Coming looks'),
    'entity.coming.rear': ('Громада подаётся назад', 'A mass draws back'),
    'entity.coming.slam': ('Громада рушится оземь', 'A mass comes down'),
    'entity.coming.sweep': ('Идёт рука', 'An arm comes round'),
    'entity.coming.strain': ('Трещит золото', 'Gold cracks'),
    'entity.coming.unmask': ('Маска спадает', 'A mask falls'),
    'entity.coming.pull': ('Тянет к пасти', 'Something draws in'),
    'entity.coming.bite': ('Смыкается пасть', 'A maw shuts'),
    'entity.coming.star': ('Свет встаёт на воде', 'Light stands on the water'),
    'entity.coming.burst': ('Падает звезда', 'A star falls'),
    'entity.gear.wind': ('Заводится шестерня', 'A wheel winds up'),
    'entity.gear.roll': ('Катится шестерня', 'A wheel runs'),
    'entity.gear.catch': ('Шестерня встаёт на место', 'A wheel is caught'),
    'entity.tendril.open': ('В полу раскрывается дыра', 'A hole opens in the floor'),
    'entity.tendril.rise': ('Из дыры встаёт рука', 'An arm comes up out of a hole'),
    'entity.tendril.slap': ('Рука рушится оземь', 'An arm comes down'),
    'entity.colossus.rumble': ('Море вздымается, земля гудит', 'The sea heaves and the ground groans'),
    'entity.colossus.roar': ('Рык над морем', 'A roar over the sea'),
    'entity.colossus.roar_short': ('Рык над морем', 'A roar over the sea'),
    'entity.miller.ambient': ('Сипит Пустомол', 'Miller wheezes'), 'entity.miller.hurt': ('Пустомол кряхтит', 'Miller grunts'),
    'entity.miller.death': ('Пустомол оседает', 'Miller sinks down'), 'entity.miller.swing': ('Идёт вага', 'A bar comes round'),
    'entity.miller.slam': ('Вага бьёт в пол', 'A bar strikes the floor'), 'entity.miller.shout': ('Пустомол ухает', 'Miller heaves'),
    'entity.miller.lantern': ('Пустомол поднимает фонарь', 'Miller lifts his lantern'),
    'entity.millstone.rumble': ('Катится жёрнов', 'A millstone rolls'), 'entity.millstone.check': ('Жёрнов встаёт', 'A millstone is checked'),
    'entity.millstone.crash': ('Жёрнов бьёт', 'A millstone strikes'),
    'entity.flour.puff': ('Лопается мешок', 'A sack bursts'), 'entity.flour.blast': ('Вспыхивает мука', 'Flour goes up'),
    'entity.shatun.ambient': ('Шатун сопит', 'Shatun huffs'), 'entity.shatun.hurt': ('Шатун ревёт от боли', 'Shatun bellows in pain'),
    'entity.shatun.death': ('Шатун валится', 'Shatun goes down'), 'entity.shatun.roar': ('Шатун ревёт', 'Shatun roars'),
    'entity.shatun.swipe': ('Свистит лапа', 'A paw whistles'), 'entity.shatun.slam': ('Шатун бьёт оземь', 'Shatun strikes the ground'),
    'entity.shatun.bite': ('Клацают зубы', 'Teeth snap'), 'entity.shatun.step': ('Гремит цепь', 'A chain rattles'),
    'entity.shatun.crash': ('Шатун врезается', 'Shatun crashes'),
    'entity.elder.ambient': ('Старец читает', 'Elder recites'), 'entity.elder.hurt': ('Старец кряхтит', 'Elder grunts'),
    'entity.elder.death': ('Старец засыпает', 'Elder falls asleep'), 'entity.elder.sweep': ('Гудит пламя на посохе', 'A flame roars on a staff'),
    'entity.elder.chant': ('Старец возглашает', 'Elder intones'), 'entity.elder.knock': ('Посох бьёт в землю', 'A staff strikes the ground'),
    'entity.elder.call': ('Старец будит своих', 'Elder wakes his own'), 'entity.elder.slumber': ('Старец зевает', 'Elder yawns'),
    'entity.elder.dream': ('Наползает сон', 'A dream creeps in'), 'entity.elder.snore': ('Старец храпит', 'Elder snores'),
    'entity.sailor.ambient': ('Моряк хрипит', 'Seaman wheezes'), 'entity.sailor.hurt': ('Моряк охает', 'Seaman grunts'),
    'entity.sailor.death': ('Моряк идёт ко дну', 'Seaman goes under'), 'entity.sailor.hook': ('Цепляет крюк', 'A hook bites'),
    'entity.sailor.throw': ('Летит гарпун', 'A harpoon flies'), 'entity.sailor.oar': ('Свистит весло', 'An oar whistles'),
    'entity.harpoon.hit': ('Гарпун втыкается', 'A harpoon strikes'), 'entity.pilot.whirl': ('Гудит лотлинь', 'A lead line hums'),
    'entity.pilot.lead': ('Лот бьёт оземь', 'A sounding lead lands'),
    'entity.sand_crab.dig': ('Шевелится песок', 'Sand stirs'), 'entity.sand_crab.snap': ('Щёлкает клешня', 'A claw snaps'),
    'entity.sand_crab.click': ('Стучат ножки', 'Legs click'), 'entity.sand_crab.hurt': ('Трещит панцирь', 'A shell cracks'),
    'entity.sand_crab.death': ('Клешняк лопается', 'Sand crab bursts'),
    'entity.skua.cry': ('Кричит поморник', 'Skua cries'), 'entity.skua.stoop': ('Поморник падает камнем', 'Skua stoops'),
    'entity.skua.hurt': ('Поморник вскрикивает', 'Skua shrieks'), 'entity.skua.death': ('Поморник падает', 'Skua drops'),
    'entity.helmsman.ambient': ('Кормчий дышит', 'Helmsman breathes'), 'entity.helmsman.hurt': ('Кормчий рычит', 'Helmsman growls'),
    'entity.helmsman.death': ('Кормчий падает на палубу', 'Helmsman falls to the deck'), 'entity.helmsman.swing': ('Якорь идёт по кругу', 'An anchor comes round'),
    'entity.helmsman.anchor': ('Якорь бьёт в палубу', 'An anchor strikes the deck'), 'entity.helmsman.chain': ('Гремит якорная цепь', 'An anchor chain rattles'),
    'entity.helmsman.shout': ('Кормчий командует', 'Helmsman gives the order'),
    'entity.capsized.ambient': ('Скрипит старая лодка', 'An old boat creaks'), 'entity.capsized.hurt': ('Трещат доски', 'Planks crack'),
    'entity.capsized.death': ('Лодка разваливается', 'A boat falls apart'), 'entity.capsized.rise': ('Лодка встаёт на ноги', 'A boat gets up on legs'),
    'entity.capsized.snap': ('Клацает большая клешня', 'A great claw snaps'), 'entity.capsized.creak': ('Скрипит днище', 'A hull creaks'),
    'entity.capsized.slam': ('Лодка бьёт оземь', 'A boat comes down'), 'entity.capsized.knock': ('Стук по днищу', 'A knock on a hull'),
    'ambient.shore.sea': ('Шумит море', 'The sea'),
    'player.undertow': ('Тянет на дно', 'The sea pulls under'),
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
    'block.clock.set': ('Ключ встаёт в гнездо', 'A key sets into its socket'), 'block.clock.empty': ('Гнездо молчит', 'The socket stays silent'),
    'block.clock.start': ('Часы трогаются', 'The clock stirs'),
    'block.tower.rumble': ('Земля гудит', 'The ground drones'), 'block.tower.crack': ('Трескается камень', 'Stone cracks'),
    'block.tower.settle': ('Башня садится на место', 'The tower settles'), 'block.tower.toll': ('Бьёт великий колокол', 'The great bell tolls'),
    'block.tower.seat': ('Камень принимает тяжесть', 'Stone takes a weight'),
}
SUBTITLES.update({
    'entity.folk.candlewife': ('Свечница шепчет', 'Candlewife whispers'), 'entity.folk.debtor': ('Должник бормочет', 'Debtor mutters'),
    'entity.folk.carver': ('Стучит резец', 'Chisel taps'), 'entity.folk.ferryman': ('Перевозчик ворчит', 'Ferryman grumbles'),
    'entity.folk.hangman': ('Скрипит верёвка', 'Rope creaks'), 'entity.folk.cellarer': ('Келарь считает', 'Cellarer counts'),
    'entity.folk.bellman': ('Звонарь переспрашивает', 'Bellman asks again')})
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
