"""Lays the pictures of a CI run out on sheets of nine, by group, each picture under its name.
Usage: python3 tools/ci_sheets.py SHOTS_DIR SHEETS_DIR   -> SHEETS_DIR/<group>_<n>.jpg and index.txt (sheet: its pictures)"""
import os, re, sys
from PIL import Image, ImageDraw
src, dst = sys.argv[1], sys.argv[2]
os.makedirs(dst, exist_ok=True)
W, H, BAR, COLS, ROWS = 640, 360, 18, 3, 3
LETTERS = {'v': 'mobs_borough', 'q': 'mobs_quarry', 's': 'mobs_mire', 'f': 'mobs_firwood', 'g': 'mobs_firwood', 'h': 'mobs_wharf', 'j': 'mobs_stubble', 'k': 'mobs_depths'}
# a view of the map goes by the place its name begins with; those of the town and the fortress, which have no such word, by their number
PLACES = (('quarry', 'map_quarry'), ('mire', 'map_mire'), ('firwood', 'map_firwood'), ('skete', 'map_firwood'), ('shore', 'map_wharf'), ('stubble', 'map_stubble'),
          ('depths', 'map_depths'), ('near_the', 'map_lords'), ('after_the', 'map_lords'))
def group(n):
    m = re.match(r'm(\d+)_(.*)', n)
    if m:
        if re.search(r'screen|after_travel', m.group(2)): return 'map_screens'
        k = int(m.group(1))
        return next((g for word, g in PLACES if m.group(2).startswith(word)), 'map_borough' if k < 26 else 'map_stockade' if k < 190 else 'map_lords')
    m = re.match(r'(\d+)_', n)
    if m:
        k = int(m.group(1))
        return 'mobs_stockade' if k <= 21 else 'gear_held' if k in (31, 32) else 'items_player' if k <= 38 else 'buildings'
    return LETTERS.get(n[0], 'other')
def order(n):
    m = re.match(r'm(\d+)_', n)
    return (int(m.group(1)) if m else 0, n)
groups = {}
for f in sorted(os.listdir(src)):
    if f.endswith('.png'): groups.setdefault(group(f[:-4]), []).append(f[:-4])
index = []
for g in sorted(groups):
    names = sorted(groups[g], key=order)
    for i in range(0, len(names), COLS * ROWS):
        part = names[i:i + COLS * ROWS]
        rows = (len(part) + COLS - 1) // COLS
        sheet = Image.new('RGB', (W * COLS, (H + BAR) * rows), (20, 20, 20))
        d = ImageDraw.Draw(sheet)
        for j, n in enumerate(part):
            x, y = (j % COLS) * W, (j // COLS) * (H + BAR)
            try: sheet.paste(Image.open(os.path.join(src, n + '.png')).convert('RGB').resize((W, H), Image.LANCZOS), (x, y + BAR))
            except Exception as e: d.text((x + 4, y + BAR + 4), 'unreadable: ' + str(e)[:60], fill=(255, 80, 80))
            d.text((x + 4, y + 3), n, fill=(235, 235, 235))
        name = f'{g}_{i // (COLS * ROWS) + 1:02d}.jpg'
        sheet.save(os.path.join(dst, name), quality=82)
        index.append(name + ': ' + ' '.join(part))
open(os.path.join(dst, 'index.txt'), 'w').write('\n'.join(index) + '\n')
print(len(index), 'sheets of', sum(len(v) for v in groups.values()), 'pictures')
