#!/bin/sh
# Regenerates every generated file of the mod: textures, models, mesh code, sounds, texts. Run from the repo root.
# Pass a directory to also get preview pictures of everything in it.
set -e
export PYTHONPATH=tools
P="$1"
[ -n "$P" ] && mkdir -p "$P"
python3 tools/gen_lang.py
python3 tools/gen_withered.py ${P:+$P/withered.png}
python3 tools/gen_knight.py ${P:+$P/knight.png}
python3 tools/gen_keykeeper.py ${P:+$P/keykeeper.png}
python3 tools/gen_overseer.py ${P:+$P/overseer.png}
python3 tools/gen_chaplain.py ${P:+$P/chaplain.png}
python3 tools/gen_hound.py ${P:+$P/hound.png}
python3 tools/gen_gravedigger.py ${P:+$P/gravedigger.png}
python3 tools/gen_crawler.py ${P:+$P/crawler.png}
python3 tools/gen_unhewn.py ${P:+$P/unhewn.png}
python3 tools/gen_taskmaster.py ${P:+$P/taskmaster.png}
python3 tools/gen_stone_eater.py ${P:+$P/stone_eater.png}
python3 tools/gen_pike.py ${P:+$P/pike.png}
python3 tools/gen_lanky.py ${P:+$P/lanky.png}
python3 tools/gen_wisp.py ${P:+$P/wisp.png}
python3 tools/gen_wallower.py ${P:+$P/wallower.png}
python3 tools/gen_mother.py ${P:+$P/mother.png}
python3 tools/gen_root.py ${P:+$P/root.png}
python3 tools/gen_anchor.py ${P:+$P/anchor.png}
python3 tools/gen_beam.py ${P:+$P/beam.png}
python3 tools/gen_sect.py ${P:+$P/sect.png}
python3 tools/gen_waxen.py ${P:+$P/waxen.png}
python3 tools/gen_moth.py ${P:+$P/moth.png}
python3 tools/gen_shatun.py ${P:+$P/shatun.png}
python3 tools/gen_elder.py ${P:+$P/elder.png}
python3 tools/gen_sailor.py ${P:+$P/sailor.png}
python3 tools/gen_crab.py ${P:+$P/crab.png}
python3 tools/gen_skua.py ${P:+$P/skua.png}
python3 tools/gen_capsized.py ${P:+$P/capsized.png}
python3 tools/gen_fieldhand.py ${P:+$P/fieldhand.png}
python3 tools/gen_scarecrow.py ${P:+$P/scarecrow.png}
python3 tools/gen_stook.py ${P:+$P/stook.png}
python3 tools/gen_sungazer.py ${P:+$P/sungazer.png}
python3 tools/gen_items.py ${P:+$P/items.png}
python3 tools/gen_shields.py ${P:+$P/shields.png}
python3 tools/gen_armor.py ${P:+$P/armor.png}
python3 tools/gen_blocks.py ${P:+$P/blocks.png}
python3 tools/gen_structures.py ${P:+$P/structures}
python3 tools/gen_data.py
# only what has no file yet is written: delete a sound's file to have it made again (or unset SOUNDS_NEW for all of them)
SOUNDS_NEW=1 python3 tools/gen_sounds.py $P
