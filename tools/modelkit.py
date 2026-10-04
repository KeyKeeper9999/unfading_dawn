"""Shared kit for the mod's entity models: boxes -> texture layout, Java mesh code and a software preview.

A model is described once, here in Python. From that description come
  * the layout of the texture (every box gets its own unwrap, packed automatically unless a UV is given),
  * the mesh part of the Java model class (written between the "generated mesh" markers),
  * a preview picture rendered without the game, to judge proportions, textures and poses.

Coordinates are Minecraft model space: units of 1/16 block, y points DOWN, the mob faces -z, the ground is at y = 24.
"""
import math
import random

import numpy as np
from PIL import Image

FACES = ('top', 'bottom', 'right', 'front', 'left', 'back')


def rot_matrix(rx, ry, rz):
    cx, sx, cy, sy, cz, sz = math.cos(rx), math.sin(rx), math.cos(ry), math.sin(ry), math.cos(rz), math.sin(rz)
    mx = np.array([[1, 0, 0], [0, cx, -sx], [0, sx, cx]])
    my = np.array([[cy, 0, sy], [0, 1, 0], [-sy, 0, cy]])
    mz = np.array([[cz, -sz, 0], [sz, cz, 0], [0, 0, 1]])
    return mz @ my @ mx


class Box:
    def __init__(self, x, y, z, w, h, d, mat='', tag='', inflate=0.0, uv=None, part=None):
        self.x, self.y, self.z, self.w, self.h, self.d = x, y, z, w, h, d
        self.mat, self.tag, self.inflate, self.uv, self.part = mat, tag, inflate, uv, part

    @property
    def tw(self):
        return int(math.ceil(self.w))

    @property
    def th(self):
        return int(math.ceil(self.h))

    @property
    def td(self):
        return int(math.ceil(self.d))

    def faces(self):
        """Texture rectangle (u, v, w, h) of every face, in the standard Minecraft box unwrap."""
        u, v = self.uv
        w, h, d = self.tw, self.th, self.td
        return {'top': (u + d, v, w, d), 'bottom': (u + d + w, v, w, d), 'right': (u, v + d, d, h), 'front': (u + d, v + d, w, h),
                'left': (u + d + w, v + d, d, h), 'back': (u + d + w + d, v + d, w, h)}

    def footprint(self):
        return 2 * (self.tw + self.td), self.td + self.th

    def corners(self):
        """The four corners of every face in part space, ordered to match the texture: top-left, top-right, bottom-right, bottom-left."""
        g = self.inflate
        x0, y0, z0 = self.x - g, self.y - g, self.z - g
        x1, y1, z1 = self.x + self.w + g, self.y + self.h + g, self.z + self.d + g
        return {
            'front': [(x0, y0, z0), (x1, y0, z0), (x1, y1, z0), (x0, y1, z0)],
            'back': [(x1, y0, z1), (x0, y0, z1), (x0, y1, z1), (x1, y1, z1)],
            'right': [(x0, y0, z1), (x0, y0, z0), (x0, y1, z0), (x0, y1, z1)],
            'left': [(x1, y0, z0), (x1, y0, z1), (x1, y1, z1), (x1, y1, z0)],
            'top': [(x0, y0, z1), (x1, y0, z1), (x1, y0, z0), (x0, y0, z0)],
            'bottom': [(x0, y1, z0), (x1, y1, z0), (x1, y1, z1), (x0, y1, z1)],
        }


class Model:
    def __init__(self, tex_w, tex_h, scale=1.0):
        self.tex_w, self.tex_h, self.scale = tex_w, tex_h, scale
        self.parts = {}          # name -> dict(parent, pivot, rot, boxes, emit)
        self.order = []

    def part(self, name, parent=None, pivot=(0, 0, 0), rot=(0, 0, 0), boxes=(), emit=True):
        """emit=False: the part exists in the game already (a vanilla humanoid part); it is only here for layout and preview."""
        bl = []
        for b in boxes:
            b = b if isinstance(b, Box) else Box(*b)
            b.part = name
            bl.append(b)
        self.parts[name] = dict(parent=parent, pivot=tuple(pivot), rot=tuple(rot), boxes=bl, emit=emit)
        self.order.append(name)
        return self.parts[name]

    def mirror(self, src, dst, parent=None):
        """A copy of part src mirrored across x; right_* parents become left_*."""
        p = self.parts[src]
        px, py, pz = p['pivot']
        rx, ry, rz = p['rot']
        new_parent = parent if parent is not None else (p['parent'].replace('right', 'left') if p['parent'] else None)
        boxes = [Box(-(b.x + b.w), b.y, b.z, b.w, b.h, b.d, b.mat, b.tag, b.inflate, None) for b in p['boxes']]
        return self.part(dst, new_parent, (-px, py, pz), (rx, -ry, -rz), boxes, p['emit'])

    def boxes(self):
        for name in self.order:
            for i, b in enumerate(self.parts[name]['boxes']):
                yield name, i, b

    # ------------------------------------------------------------------ texture layout

    def pack(self, start_y=0, reserved=()):
        """Gives every box without a UV a free place in the texture (shelves, tallest first)."""
        used = [tuple(r) for r in reserved]
        for _, _, b in self.boxes():
            if b.uv is not None:
                fw, fh = b.footprint()
                used.append((b.uv[0], b.uv[1], fw, fh))

        def free(x, y, w, h):
            if x + w > self.tex_w or y + h > self.tex_h:
                return False
            return all(x + w <= ux or ux + uw <= x or y + h <= uy or uy + uh <= y for ux, uy, uw, uh in used)

        todo = [b for _, _, b in self.boxes() if b.uv is None]
        todo.sort(key=lambda b: (-b.footprint()[1], -b.footprint()[0]))
        for b in todo:
            fw, fh = b.footprint()
            placed = False
            for y in range(start_y, self.tex_h - fh + 1):
                for x in range(0, self.tex_w - fw + 1):
                    if free(x, y, fw, fh):
                        b.uv = (x, y)
                        used.append((x, y, fw, fh))
                        placed = True
                        break
                if placed:
                    break
            if not placed:
                raise ValueError(f'texture {self.tex_w}x{self.tex_h} is too small for the box {b.part} {b.w}x{b.h}x{b.d}')
        return max(uy + uh for ux, uy, uw, uh in used)

    # ------------------------------------------------------------------ Java

    @staticmethod
    def _f(v):
        return f'{v:.1f}F' if abs(v * 10 - round(v * 10)) < 1e-6 else f'{round(v, 4)}F'

    def emit_java(self, indent='        '):
        f = self._f
        lines = []
        for name in self.order:
            p = self.parts[name]
            if not p['emit']:
                continue
            cubes = 'CubeListBuilder.create()'
            for b in p['boxes']:
                deform = f', new CubeDeformation({f(b.inflate)})' if b.inflate else ''
                cubes += (f'\n{indent}        .texOffs({b.uv[0]}, {b.uv[1]}).addBox({f(b.x)}, {f(b.y)}, {f(b.z)}, '
                          f'{f(b.tw)}, {f(b.th)}, {f(b.td)}{deform})')
            piv, rot = p['pivot'], p['rot']
            if any(rot):
                pose = f'PartPose.offsetAndRotation({f(piv[0])}, {f(piv[1])}, {f(piv[2])}, {f(rot[0])}, {f(rot[1])}, {f(rot[2])})'
            elif any(piv):
                pose = f'PartPose.offset({f(piv[0])}, {f(piv[1])}, {f(piv[2])})'
            else:
                pose = 'PartPose.ZERO'
            parent = p['parent'] or 'root'
            lines.append(f'{indent}PartDefinition {name} = {parent}.addOrReplaceChild("{name}", {cubes},\n{indent}        {pose});')
        return '\n'.join(lines)

    def write_java(self, path, indent='        '):
        src = open(path, encoding='utf-8').read()
        a = src.index(indent + '// <generated mesh>')
        b = src.index(indent + '// </generated mesh>')
        open(path, 'w', encoding='utf-8').write(src[:a] + indent + '// <generated mesh>\n' + self.emit_java(indent) + '\n' + src[b:])

    # ------------------------------------------------------------------ preview

    def transform(self, name, pose):
        """Rotation and translation taking part space to model space, with the extra pose rotations/offsets applied."""
        p = self.parts[name]
        extra = pose.get(name, {})
        if isinstance(extra, (tuple, list)):
            extra = {'rot': extra}
        er = extra.get('rot', (0, 0, 0))
        eo = extra.get('off', (0, 0, 0))
        if extra.get('abs'):
            r = er
        else:
            r = tuple(a + b for a, b in zip(p['rot'], er))
        m = rot_matrix(*r)
        t = np.array(p['pivot'], float) + np.array(eo, float)
        if p['parent']:
            pm, pt = self.transform(p['parent'], pose)
            return pm @ m, pt + pm @ t
        return m, t

    def render(self, tex, yaw=-0.5, pitch=0.12, pose=None, size=(360, 480), zoom=8.0, centre=(0, 4), background=(118, 142, 172), glow=None,
               hidden=()):
        """Draws the model with texture tex (PIL RGBA). The camera looks at the mob's front; yaw turns the mob."""
        pose = pose or {}
        width, height = size
        color = np.zeros((height, width, 3), np.float32)
        color[:] = background
        depth = np.full((height, width), 1e9, np.float32)
        texels = np.asarray(tex.convert('RGBA'), np.float32)
        glow_px = np.asarray(glow.convert('RGBA'), np.float32) if glow is not None else None
        view = rot_matrix(pitch, 0, 0) @ rot_matrix(0, yaw, 0)
        s = self.scale
        for name, _, b in self.boxes():
            if name in hidden or any(self._ancestor_hidden(name, hidden)):
                continue
            m, t = self.transform(name, pose)
            rects = b.faces()
            for face, pts in b.corners().items():
                fu, fv, fw, fh = rects[face]
                if fw == 0 or fh == 0:
                    continue
                world = [m @ np.array(p, float) + t for p in pts]
                world = [np.array([w[0] * s, w[1] * s + 24.0 * (1 - s), w[2] * s]) for w in world]
                cam = [view @ (w - np.array([centre[0], centre[1], 0.0])) for w in world]
                normal = np.cross(cam[1] - cam[0], cam[3] - cam[0])
                n = normal / (np.linalg.norm(normal) + 1e-9)
                # Minecraft-like lighting: faces turned up are bright, down are dark, sides in between.
                shade = 0.62 + 0.22 * max(0.0, -n[1]) + 0.16 * abs(n[2]) - 0.12 * max(0.0, n[1])
                scr = [(width / 2 + c[0] * zoom, height / 2 + c[1] * zoom, c[2]) for c in cam]
                self._raster(color, depth, scr, (fu, fv, fw, fh), texels, glow_px, shade)
        return Image.fromarray(np.clip(color, 0, 255).astype(np.uint8), 'RGB')

    def _ancestor_hidden(self, name, hidden):
        p = self.parts[name]['parent']
        while p:
            yield p in hidden
            p = self.parts[p]['parent']

    @staticmethod
    def _raster(color, depth, scr, rect, texels, glow_px, shade):
        fu, fv, fw, fh = rect
        height, width = depth.shape
        xs = [p[0] for p in scr]
        ys = [p[1] for p in scr]
        x0, x1 = max(0, int(math.floor(min(xs)))), min(width - 1, int(math.ceil(max(xs))))
        y0, y1 = max(0, int(math.floor(min(ys)))), min(height - 1, int(math.ceil(max(ys))))
        if x0 > x1 or y0 > y1:
            return
        gx, gy = np.meshgrid(np.arange(x0, x1 + 1) + 0.5, np.arange(y0, y1 + 1) + 0.5)
        # corner texture coordinates: tl, tr, br, bl
        uvs = [(0.0, 0.0), (1.0, 0.0), (1.0, 1.0), (0.0, 1.0)]
        for tri in ((0, 1, 2), (0, 2, 3)):
            a, b, c = (scr[i] for i in tri)
            den = (b[1] - c[1]) * (a[0] - c[0]) + (c[0] - b[0]) * (a[1] - c[1])
            if abs(den) < 1e-9:
                continue
            w0 = ((b[1] - c[1]) * (gx - c[0]) + (c[0] - b[0]) * (gy - c[1])) / den
            w1 = ((c[1] - a[1]) * (gx - c[0]) + (a[0] - c[0]) * (gy - c[1])) / den
            w2 = 1.0 - w0 - w1
            inside = (w0 >= -1e-4) & (w1 >= -1e-4) & (w2 >= -1e-4)
            if not inside.any():
                continue
            z = w0 * a[2] + w1 * b[2] + w2 * c[2]
            ua, ub, uc = (uvs[i] for i in tri)
            u = w0 * ua[0] + w1 * ub[0] + w2 * uc[0]
            v = w0 * ua[1] + w1 * ub[1] + w2 * uc[1]
            tx = np.clip((fu + u * fw).astype(int), fu, fu + fw - 1)
            ty = np.clip((fv + v * fh).astype(int), fv, fv + fh - 1)
            tx = np.clip(tx, 0, texels.shape[1] - 1)
            ty = np.clip(ty, 0, texels.shape[0] - 1)
            sample = texels[ty, tx]
            sub = depth[y0:y1 + 1, x0:x1 + 1]
            ok = inside & (sample[..., 3] > 25) & (z < sub)
            if not ok.any():
                continue
            rgb = sample[..., :3] * shade
            if glow_px is not None:
                g = glow_px[ty, tx]
                lit = g[..., 3:4] > 25
                rgb = np.where(lit, np.maximum(rgb, g[..., :3]), rgb)
            col = color[y0:y1 + 1, x0:x1 + 1]
            col[ok] = rgb[ok]
            sub[ok] = z[ok]


def sheet(images, columns=None, labels=None, pad=4, background=(40, 44, 52)):
    """Lays pictures out in a grid for looking at them all at once."""
    columns = columns or len(images)
    rows = (len(images) + columns - 1) // columns
    w = max(i.width for i in images)
    h = max(i.height for i in images)
    out = Image.new('RGB', (columns * (w + pad) + pad, rows * (h + pad) + pad), background)
    for n, im in enumerate(images):
        out.paste(im.convert('RGB'), (pad + (n % columns) * (w + pad), pad + (n // columns) * (h + pad)))
    if labels:
        from PIL import ImageDraw
        d = ImageDraw.Draw(out)
        for n, text in enumerate(labels):
            d.text((pad + (n % columns) * (w + pad) + 3, pad + (n // columns) * (h + pad) + 2), text, fill=(255, 255, 255))
    return out


# ---------------------------------------------------------------------- painting helpers

def shade(c, f):
    return tuple(max(0, min(255, int(round(v * f)))) for v in c[:3])


def mix(a, b, t):
    return tuple(int(round(x * (1 - t) + y * t)) for x, y in zip(a[:3], b[:3]))


class Painter:
    """Paints the faces of boxes. Colours are picked per material; details are added per tag afterwards."""

    def __init__(self, model, seed=1):
        self.model = model
        self.tex = Image.new('RGBA', (model.tex_w, model.tex_h), (0, 0, 0, 0))
        self.glow = Image.new('RGBA', (model.tex_w, model.tex_h), (0, 0, 0, 0))
        self.seed = seed

    def px(self, x, y, c, glow=False, alpha=255):
        if 0 <= x < self.tex.width and 0 <= y < self.tex.height:
            self.tex.putpixel((x, y), tuple(c[:3]) + (alpha,))
            if glow:
                self.glow.putpixel((x, y), tuple(c[:3]) + (255,))

    def clear(self, x, y):
        if 0 <= x < self.tex.width and 0 <= y < self.tex.height:
            self.tex.putpixel((x, y), (0, 0, 0, 0))

    def fill_box(self, box, fn):
        """fn(face, px, py, fw, fh, rnd) -> colour or None (transparent) for every texel of the box."""
        rnd = random.Random(f'{self.seed}:{box.part}:{box.x}:{box.y}:{box.z}:{box.w}:{box.h}:{box.d}')
        for face, (fx, fy, fw, fh) in box.faces().items():
            for py in range(fh):
                for px in range(fw):
                    c = fn(face, px, py, fw, fh, rnd)
                    if c is None:
                        self.clear(fx + px, fy + py)
                    else:
                        self.px(fx + px, fy + py, c)

    def face(self, box, face):
        return box.faces()[face]

    def put(self, box, face, px, py, c, glow=False):
        fx, fy, fw, fh = box.faces()[face]
        if 0 <= px < fw and 0 <= py < fh:
            self.px(fx + px, fy + py, c, glow)

    def get(self, box, face, px, py):
        fx, fy, fw, fh = box.faces()[face]
        return self.tex.getpixel((fx + px, fy + py))

    def art(self, box, face, rows, palette, ox=0, oy=0, glow_chars=''):
        """Draws ASCII pixel art on a face: '.' leaves the texel alone, ' ' too; other characters come from the palette."""
        for y, row in enumerate(rows):
            for x, ch in enumerate(row):
                if ch in '. ':
                    continue
                if ch == '~':
                    fx, fy, fw, fh = box.faces()[face]
                    self.clear(fx + ox + x, fy + oy + y)
                    continue
                self.put(box, face, ox + x, oy + y, palette[ch], ch in glow_chars)

    def band(self, box, row, c, faces=('right', 'front', 'left', 'back'), every=1, glow=False):
        """A horizontal line all around the box at texel row `row` (negative counts from the bottom)."""
        for face in faces:
            fx, fy, fw, fh = box.faces()[face]
            y = row if row >= 0 else fh + row
            for x in range(0, fw, every):
                self.put(box, face, x, y, c, glow)


def material(base, dark=0.72, light=1.16, noise=(0.93, 1.0, 1.0, 1.06), edge=0.82, top=1.12, bottom=0.7, gradient=0.22, rim=None):
    """A simple shaded material: lit from above, darker towards the bottom of each side, darker outline, speckled."""
    def fn(face, px, py, fw, fh, rnd):
        if face == 'top':
            f = top
        elif face == 'bottom':
            f = bottom
        else:
            f = 1.04 - gradient * (py / max(1, fh - 1))
        f *= rnd.choice(noise)
        if min(fw, fh) > 2 and (px in (0, fw - 1) or py in (0, fh - 1)):
            f *= edge
        if rim and py == 0 and face not in ('top', 'bottom') and fw > 2:
            return shade(rim, rnd.choice(noise))
        return shade(base, f)
    return fn
