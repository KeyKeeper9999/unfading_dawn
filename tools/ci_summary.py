"""Writes summary.txt and counts.json of a CI run: what the logs say in a page, so that the logs are opened only on trouble.
Usage: python3 tools/ci_summary.py OUT_DIR [PREVIOUS_counts.json]   (OUT_DIR holds build.log, server.log, client.log,
map.log, mapclient.log, the jar, High_Stockade.zip, shots/; run from the repo root, where .ci/ is)"""
import json, os, re, sys, glob
out = sys.argv[1]
prev = {}
if len(sys.argv) > 2:
    try: prev = json.load(open(sys.argv[2]))
    except Exception: prev = {}
sw = lambda n: os.path.exists(os.path.join('.ci', n))
def read(n):
    p = os.path.join(out, n)
    return open(p, encoding='utf-8', errors='replace').read().splitlines() if os.path.exists(p) else None
NOISE = re.compile(r'OpenAL|flite|swapchain|BackendCreationException|ClosedChannelException|Exception caught in connection'
                   r'|Failed to create backend OpenGL|Error starting SoundSystem|Ambiguity between arguments|Narrator|realms|Realms|authlib|Failed to retrieve profile key|yggdrasil', re.I)
BAD = re.compile(r'\b(fail\w*|missing|problem\w*|not found|cannot|could not|unreachable|wrong|no such)\b', re.I)
FINE = re.compile(r'\b0 failed|forced=null')
flags, body, counts = [], [], {}
def flag(s): flags.append(s)
def section(title, lines): body.append(''); body.append('== ' + title); body.extend(lines)
def cut(s, n=200): return s if len(s) <= n else s[:n] + ' ...'
def dushi(lines, tag):
    r = []
    for l in lines:
        i = l.find('DUSHI ' + tag)
        if i >= 0: r.append(l[i + 7 + len(tag):])
    return r
def game_errors(lines, name):
    seen, r = {}, []
    for l in lines:
        if ('/ERROR]' in l or 'Exception' in l or 'Caused by' in l or ('/WARN]' in l and 'unfading_dawn' in l)) and not NOISE.search(l):
            k = re.sub(r'\d+', 'N', re.sub(r'^\[[^\]]*\] ', '', l))[:140]
            seen[k] = seen.get(k, 0) + 1
    for k, n in sorted(seen.items(), key=lambda x: -x[1])[:10]: r.append(f'  {n}x {k}')
    if seen: flag(f'{name}: {len(seen)} kinds of error lines of the game')
    return (['game errors:'] + r) if r else ['game errors: none']
def topics(ls, key):
    for l in ls:
        t = re.match(r'[A-Za-z_]+', l)
        k = key + ':' + (t.group(0) if t else '?')
        counts[k] = counts.get(k, 0) + 1
def odd(ls, skip=None):
    r = [l for l in ls if BAD.search(l) and not FINE.search(l) and not (skip and skip.search(l))]
    return r

# build
b = read('build.log')
jars = glob.glob(os.path.join(out, '*.jar'))
if b is None: flag('no build.log'); section('BUILD', ['no build.log'])
else:
    ok = any('BUILD SUCCESSFUL' in l for l in b)
    errs = [l for l in b if re.search(r'error:|FAILED|What went wrong', l)]
    if not ok: flag('build failed')
    section('BUILD', [('ok' if ok else 'FAILED') + ', ' + (', '.join(f'{os.path.basename(j)} {os.path.getsize(j)//1024} KB' for j in jars) or 'NO JAR')]
            + [cut('  ' + l) for l in errs[:25]])
if not jars: flag('no jar')

# server
s = read('server.log')
if sw('skip_server'): section('SERVER', ['skipped (.ci/skip_server)'])
elif not s: flag('no server.log'); section('SERVER', ['no server.log'])
else:
    d = dushi(s, 'SELFTEST'); topics(d, 'server'); r = [f'{len(d)} test lines']
    done = any(l.startswith('world done') for l in d)
    if not done: flag('server test did not reach its end')
    r.append('reached its end: ' + ('yes' if done else 'NO; last line: ' + cut(d[-1] if d else (s[-1] if s else ''))))
    for l in d:
        m = re.match(r'spawned (\d+) of (\d+) mobs', l)
        if m:
            r.append(f'spawned {m.group(1)} of {m.group(2)} mobs')
            if m.group(1) != m.group(2): flag('not every mob spawned')
        if l.startswith('spawn eggs'): r.append(l)
    mobs = [l for l in d if re.match(r'[a-z_]+: alive=', l)]
    r.append(f'mobs fought: {len(mobs)}; dead after the fight: ' + (', '.join(l.split(':')[0] for l in mobs if 'alive=false' in l) or 'none')
             + '; made no move: ' + (', '.join(l.split(':')[0] for l in mobs if 'moves={}' in l) or 'none'))
    drills = [l for l in d if l.startswith('drill ') and 'finished moves' in l]
    short = []
    for l in drills:
        m = re.search(r'drill (\S+): finished moves \[([^\]]*)\] of (\d+)', l)
        if m and len(m.group(2).split()) != int(m.group(3)): short.append(f'{m.group(1)} {len(m.group(2).split())}/{m.group(3)}')
    r.append(f'drills: {len(drills)}; moves not finished: ' + (', '.join(short) or 'none'))
    counts['server#drills not finished'] = sorted(short)
    new = set(short) - set(prev.get('server#drills not finished', short))
    if new: flag('drill: moves not finished that were finished before: ' + ', '.join(sorted(new)))
    counts['server#checks'] = sorted({re.sub(r'-?\d+(\.\d+)?', 'N', l)[:300] for l in d if not re.match(r'[a-z_]+: alive=', l) and not l.startswith('drill ')})
    o = odd(d)
    if o: flag(f'server: {len(o)} suspicious test lines'); r += ['suspicious lines:'] + [cut('  ' + l) for l in o[:15]]
    section('SERVER', r + game_errors(s, 'server'))

def client(name, logname, tag, on, endmark, keep):
    c = read(logname)
    if not on: section(name, ['off']); return
    if not c: flag(f'no {logname}'); section(name, [f'no {logname}']); return
    d = dushi(c, tag); topics(d, name.lower())
    shots = [l[5:] for l in d if l.startswith('shot ')]
    counts[name.lower() + '#shots'] = sorted(shots)
    done = any(l.startswith(endmark) for l in d)
    if not done: flag(f'{name.lower()} test did not reach its end')
    r = [f'{len(d)} test lines, {len(shots)} pictures', 'reached its end: ' + ('yes' if done else 'NO; last line: ' + cut(d[-1] if d else ''))]
    if any('Failed to create model' in l for l in c): flag('Failed to create model'); r.append('FAILED TO CREATE MODEL: ' + cut(next(l for l in c if 'Failed to create model' in l)))
    res = {}
    for l in c:
        if re.search(r'Missing textures|Unable to load model|Missing model|Unable to resolve texture|Using missing texture|Couldn.t (load|parse)|Unknown (block|item|entity|registry)|Missing sound|File .* does not exist', l):
            k = re.sub(r'^\[[^\]]*\] ', '', l)[:150]; res[k] = res.get(k, 0) + 1
    if res: flag(f'{name.lower()}: {len(res)} kinds of missing resources'); r += ['missing resources:'] + [f'  {n}x {k}' for k, n in list(res.items())[:12]]
    else: r.append('missing resources: none')
    r += [cut('  ' + re.sub(r'Success\[.*?\]\]', 'Success', l), 300) for l in d if keep.match(l)]
    o = odd([l for l in d if not keep.match(l) and not l.startswith('shot ')])
    if o: flag(f'{name.lower()}: {len(o)} suspicious test lines'); r += ['suspicious lines:'] + [cut('  ' + l) for l in o[:15]]
    section(name, r + game_errors(c, name.lower()))

client('CLIENT', 'client.log', 'CLIENTTEST', sw('client'), 'done', re.compile(r'roll|after |burden|outpost|stockade|rest |sludge'))

# map
if not sw('map'): section('MAP', ['off'])
else:
    m = read('map.log')
    if not m: flag('no map.log'); section('MAP', ['no map.log'])
    else:
        d = dushi(m, 'MAP'); topics(d, 'map')
        saved = any(l.startswith('saved=true') for l in d)
        if not saved: flag('the map was not saved')
        z = os.path.join(out, 'High_Stockade.zip')
        r = ['saved: ' + ('yes' if saved else 'NO'), 'High_Stockade.zip: ' + (f'{os.path.getsize(z)//1024} KB' if os.path.exists(z) else 'MISSING')]
        if not os.path.exists(z): flag('no High_Stockade.zip')
        r += [cut('  ' + l, 260) for l in d if not l.startswith('light')]
        for l in d:
            f = re.search(r'(\d+) failed', l)
            if f and f.group(1) != '0': flag('map: ' + cut(l, 80))
        dark = [l for l in d if l.startswith('light') and re.search(r'\bDARK\b|too dark|wrong', l)]
        r.append(f'light probes: {sum(l.startswith("light") for l in d)}' + (f', {len(dark)} wrong' if dark else ''))
        section('MAP', r + game_errors(m, 'map'))
    client('MAPCLIENT', 'mapclient.log', 'MAPTEST', sw('mapshots'), 'done', re.compile(r'killed|lever|gate|travel|the world is|after a while|on the server|menu'))

# against the run before
ch = []
if prev:
    for k in sorted(set(prev) | set(counts)):
        a, c2 = prev.get(k), counts.get(k)
        if '#' in k:
            a, c2 = set(a or []), set(c2 or [])
            sep = ' ' if k.endswith('#shots') else '\n      '
            if a - c2: ch.append(f'  gone from {k}:{sep}' + cut(sep.join(sorted(a - c2)), 1200))
            if c2 - a: ch.append(f'  new in {k}:{sep}' + cut(sep.join(sorted(c2 - a)), 1200))
            if (a ^ c2) and k == 'server#checks': flag('server: what the checks say changed (see the end)')
        elif a != c2: ch.append(f'  {k}: {a or 0} -> {c2 or 0} lines')
section('AGAINST THE RUN BEFORE (lines by topic, pictures by name, the server checks with numbers left out)', ch or (['  no difference'] if prev else ['  nothing to compare with']))

head = [f'commit {os.environ.get("GITHUB_SHA", "?")}', 'switches: ' + (' '.join(sorted(os.listdir('.ci'))) if os.path.isdir('.ci') else 'none'),
        ('RESULT: OK' if not flags else f'RESULT: LOOK ({len(flags)})')] + ['  - ' + f for f in flags]
open(os.path.join(out, 'summary.txt'), 'w', encoding='utf-8').write('\n'.join(head + body) + '\n')
json.dump(counts, open(os.path.join(out, 'counts.json'), 'w'), sort_keys=True)
print('\n'.join(head))
