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
    'entity.gut.ambient': ('Утроба сопит спиной', 'Gut breathes through its back'),
    'entity.gut.hurt': ('Утроба ревёт', 'Gut bellows'),
    'entity.gut.death': ('Утроба валится', 'Gut goes down'),
    'entity.gut.bellow': ('Утроба трубит', 'Gut trumpets'),
    'entity.gut.step': ('Тяжёлый топот', 'Heavy tread'),
    'entity.gut.crash': ('Утроба врезается', 'Gut crashes'),
    'entity.gut.slam': ('Кулаки бьют оземь', 'Fists strike the ground'),
    'entity.gut.gape': ('Спина раскрывается', 'A back opens'),
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
    'entity.coming.death': ('Грядущее уходит в разрыв', 'The Coming goes back through'),
    'entity.coming.gaze': ('Грядущее смотрит', 'The Coming looks'),
    'entity.coming.rear': ('Шея выгибается', 'A neck rears'),
    'entity.coming.slam': ('Рушится голова', 'A head comes down'),
    'entity.coming.sweep': ('Идёт рука', 'An arm comes round'),
    'entity.coming.strain': ('Трещит золото', 'Gold cracks'),
    'entity.coming.unmask': ('Маска спадает', 'A mask falls'),
    'entity.coming.pull': ('Тянет к пасти', 'Something draws in'),
    'entity.coming.bite': ('Смыкается пасть', 'A maw shuts'),
    'entity.coming.star': ('Свет встаёт на воде', 'Light stands on the water'),
    'entity.coming.burst': ('Падает звезда', 'A star falls'),
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
