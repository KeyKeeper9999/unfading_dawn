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
python3 tools/gen_items.py ${P:+$P/items.png}
python3 tools/gen_shields.py ${P:+$P/shields.png}
python3 tools/gen_armor.py ${P:+$P/armor.png}
python3 tools/gen_blocks.py ${P:+$P/blocks.png}
python3 tools/gen_structures.py ${P:+$P/structures}
python3 tools/gen_data.py
python3 tools/gen_sounds.py $P
