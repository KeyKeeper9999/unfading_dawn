"""Block and item models made of boxes (the JSON "elements" format), with their texture laid out and painted from code
and a software preview. Coordinates are the format's own: 0..16 per block, y up, north is -z."""
import json
import math
import os
import random

import numpy as np
from PIL import Image

from modelkit import Model, rot_matrix

DIRECTIONS = ('north', 'south', 'west', 'east', 'up', 'down')


class Element:
    def __init__(self, x0, y0, z0, x1, y1, z1, mat='', tag='', shade=True, light=0, faces=DIRECTIONS):
        self.frm = (x0, y0, z0)
        self.to = (x1, y1, z1)
        self.mat, self.tag, self.shade, self.light = mat, tag, shade, light
        self.face_names = tuple(faces)
        self.rects = {}          # direction -> (u, v, w, h) in texels
        self.fixed = {}          # direction -> (texture key, [u0, v0, u1, v1]) for faces that use another texture

    def size(self, direction):
        dx, dy, dz = (self.to[i] - self.frm[i] for i in range(3))
        w, h = {'north': (dx, dy), 'south': (dx, dy), 'west': (dz, dy), 'east': (dz, dy), 'up': (dx, dz), 'down': (dx, dz)}[direction]
        return max(1, int(math.ceil(w - 1e-6))), max(1, int(math.ceil(h - 1e-6)))

    def corners(self, direction):
        """Corners of a face in the order top-left, top-right, bottom-right, bottom-left of its texture, seen from outside."""
        x0, y0, z0 = self.frm
        x1, y1, z1 = self.to
        return {
            'north': [(x1, y1, z0), (x0, y1, z0), (x0, y0, z0), (x1, y0, z0)],
            'south': [(x0, y1, z1), (x1, y1, z1), (x1, y0, z1), (x0, y0, z1)],
            'west': [(x0, y1, z0), (x0, y1, z1), (x0, y0, z1), (x0, y0, z0)],
            'east': [(x1, y1, z1), (x1, y1, z0), (x1, y0, z0), (x1, y0, z1)],
            'up': [(x0, y1, z0), (x1, y1, z0), (x1, y1, z1), (x0, y1, z1)],
            'down': [(x0, y0, z1), (x1, y0, z1), (x1, y0, z0), (x0, y0, z0)],
        }[direction]


class JsonModel:
    def __init__(self, tex_size, texture_name):
        self.tex_size = tex_size
        self.texture_name = texture_name
        self.elements = []
        self.tex = Image.new('RGBA', (tex_size, tex_size), (0, 0, 0, 0))
        self.extra_textures = {}       # key -> resource location, for faces that use a vanilla texture
        self.display = None
        self.parent = None

    def add(self, *args, **kwargs):
        e = Element(*args, **kwargs)
        self.elements.append(e)
        return e

    def pack(self):
        """Every face of every element gets its own rectangle of the texture."""
        faces = [(e, d) for e in self.elements for d in e.face_names if d not in e.fixed]
        faces.sort(key=lambda f: (-f[0].size(f[1])[1], -f[0].size(f[1])[0]))
        x = y = shelf = 0
        for e, d in faces:
            w, h = e.size(d)
            if x + w > self.tex_size:
                x = 0
                y += shelf
                shelf = 0
            if y + h > self.tex_size:
                raise ValueError(f'texture {self.tex_size} is too small for {self.texture_name}')
            e.rects[d] = (x, y, w, h)
            x += w
            shelf = max(shelf, h)
        return y + shelf

    def paint(self, fn, seed=1):
        """fn(element, direction, px, py, w, h, rnd) -> colour, None for transparent, or (r, g, b, a)."""
        for e in self.elements:
            rnd = random.Random(f'{seed}:{e.tag}:{e.frm}:{e.to}')
            for d, (u, v, w, h) in e.rects.items():
                for py in range(h):
                    for px in range(w):
                        c = fn(e, d, px, py, w, h, rnd)
                        if c is None:
                            self.tex.putpixel((u + px, v + py), (0, 0, 0, 0))
                        else:
                            self.tex.putpixel((u + px, v + py), tuple(c[:3]) + ((c[3],) if len(c) > 3 else (255,)))

    def put(self, e, direction, px, py, c):
        u, v, w, h = e.rects[direction]
        if 0 <= px < w and 0 <= py < h:
            self.tex.putpixel((u + px, v + py), tuple(c[:3]) + (255,))

    def to_json(self, texture_ref, particle=None):
        k = 16.0 / self.tex_size
        elements = []
        for e in self.elements:
            faces = {}
            for d in e.face_names:
                if d in e.fixed:
                    key, uv = e.fixed[d]
                    faces[d] = {'uv': uv, 'texture': '#' + key}
                else:
                    u, v, w, h = e.rects[d]
                    faces[d] = {'uv': [round(u * k, 4), round(v * k, 4), round((u + w) * k, 4), round((v + h) * k, 4)], 'texture': '#t'}
            el = {'from': list(e.frm), 'to': list(e.to), 'faces': faces}
            if not e.shade:
                el['shade'] = False
            if e.light:
                el['light_emission'] = e.light
            elements.append(el)
        textures = {'t': texture_ref, 'particle': particle or texture_ref}
        textures.update(self.extra_textures)
        out = {}
        if self.parent:
            out['parent'] = self.parent
        out['textures'] = textures
        out['elements'] = elements
        if self.display:
            out['display'] = self.display
        return out

    def save(self, assets, model_path, texture_path, particle=None):
        """model_path like 'block/hearth', texture_path like 'block/hearth' (inside the mod's namespace)."""
        ns = os.path.basename(assets)
        os.makedirs(os.path.dirname(f'{assets}/textures/{texture_path}.png'), exist_ok=True)
        os.makedirs(os.path.dirname(f'{assets}/models/{model_path}.json'), exist_ok=True)
        self.tex.save(f'{assets}/textures/{texture_path}.png')
        with open(f'{assets}/models/{model_path}.json', 'w') as f:
            json.dump(self.to_json(f'{ns}:{texture_path}', particle), f, separators=(',', ':'))
            f.write('\n')

    def render(self, size=(256, 256), zoom=11.0, yaw=math.radians(225 - 180), pitch=math.radians(30), background=(139, 139, 139), other=None,
               centre=(8, 8, 8)):
        """Looks at the model the way an inventory slot shows a block (from above and to the side) unless told otherwise."""
        width, height = size
        color = np.zeros((height, width, 3), np.float32)
        color[:] = background
        depth = np.full((height, width), 1e9, np.float32)
        texels = np.asarray(self.tex.convert('RGBA'), np.float32)
        view = rot_matrix(pitch, 0, 0) @ rot_matrix(0, yaw, 0)
        for e in self.elements:
            for d in e.face_names:
                if d in e.fixed:
                    continue
                pts = e.corners(d)
                cam = []
                for (x, y, z) in pts:
                    # to the y-down space the rasteriser expects
                    p = np.array([x - centre[0], -(y - centre[1]), z - centre[2]], float)
                    cam.append(view @ p)
                shade = {'up': 1.0, 'down': 0.5, 'north': 0.8, 'south': 0.8, 'west': 0.6, 'east': 0.6}[d] if e.shade else 1.0
                scr = [(width / 2 + c[0] * zoom, height / 2 + c[1] * zoom, c[2]) for c in cam]
                Model._raster(color, depth, scr, e.rects[d], texels, None, shade)
        return Image.fromarray(np.clip(color, 0, 255).astype(np.uint8), 'RGB')
