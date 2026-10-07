"""Renames the mod id everywhere: text of every tracked file and the names of files and folders.
Usage: python3 tools/rename_mod.py OLD NEW   (run from the repo root; the repository's own name, OLD followed by a digit, is left)"""
import re, subprocess, sys, os
old, new = sys.argv[1], sys.argv[2]
pat = re.compile(re.escape(old).encode() + rb'(?![0-9])')
files = subprocess.check_output(['git', 'ls-files', '-z']).decode().split('\0')[:-1]
changed = hits = 0
for f in files:
    if f == 'tools/rename_mod.py' or os.path.islink(f): continue
    data = open(f, 'rb').read()
    if b'\0' in data[:8000]: continue          # binary
    out, n = pat.subn(new.encode(), data)
    if n:
        open(f, 'wb').write(out); changed += 1; hits += n
moved = 0
for f in subprocess.check_output(['git', 'ls-files', '-z']).decode().split('\0')[:-1]:
    g = re.sub(re.escape(old) + r'(?![0-9])', new, f)
    if g != f:
        os.makedirs(os.path.dirname(g) or '.', exist_ok=True)
        subprocess.check_call(['git', 'mv', '-k', f, g]); moved += 1
print(f'{hits} replacements in {changed} files, {moved} files moved')
