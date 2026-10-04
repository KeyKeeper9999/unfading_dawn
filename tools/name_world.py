#!/usr/bin/env python3
"""Gives a saved world the name it is shown under in the list of worlds: python3 tools/name_world.py LEVEL_DAT NAME.
The name is one string in level.dat (gzipped NBT); nothing else is touched."""
import gzip
import struct
import sys

path, name = sys.argv[1], sys.argv[2]
data = gzip.open(path, 'rb').read()
key = b'\x08' + struct.pack('>H', len(b'LevelName')) + b'LevelName'
at = data.index(key) + len(key)
length = struct.unpack('>H', data[at:at + 2])[0]
old = data[at + 2:at + 2 + length].decode('utf-8')
new = name.encode('utf-8')
data = data[:at] + struct.pack('>H', len(new)) + new + data[at + 2 + length:]
with gzip.open(path, 'wb') as f:
    f.write(data)
print(f'world name: {old} -> {name}')
