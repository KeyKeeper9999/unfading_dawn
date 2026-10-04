"""Tiny pixel-art kit for item icons: shapes are drawn in flat colours, then every icon gets the dark outline
and the simple light-from-the-upper-left shading that Minecraft's own item icons have."""
from PIL import Image

T = (0, 0, 0, 0)


def rgb(c):
    return tuple(c[:3])


def shade(c, f):
    return tuple(max(0, min(255, int(round(v * f)))) for v in c[:3])


def mix(a, b, t):
    return tuple(int(round(x * (1 - t) + y * t)) for x, y in zip(a[:3], b[:3]))


class Sprite:
    def __init__(self, size=16):
        self.size = size
        self.img = Image.new('RGBA', (size, size), T)
        self.locked = set()       # pixels whose colour the outline pass must not touch (eyes, sparks, highlights)

    def px(self, x, y, c, lock=False):
        if 0 <= x < self.size and 0 <= y < self.size:
            self.img.putpixel((x, y), rgb(c) + (255,))
            if lock:
                self.locked.add((x, y))
            else:
                self.locked.discard((x, y))

    def clear(self, x, y):
        if 0 <= x < self.size and 0 <= y < self.size:
            self.img.putpixel((x, y), T)

    def get(self, x, y):
        if 0 <= x < self.size and 0 <= y < self.size:
            return self.img.getpixel((x, y))
        return T

    def solid(self, x, y):
        return self.get(x, y)[3] > 0

    def pts(self, points, c, lock=False):
        for x, y in points:
            self.px(x, y, c, lock)

    def rect(self, x0, y0, x1, y1, c):
        for x in range(x0, x1 + 1):
            for y in range(y0, y1 + 1):
                self.px(x, y, c)

    def line(self, x0, y0, x1, y1, c, lock=False):
        steps = max(abs(x1 - x0), abs(y1 - y0))
        for i in range(steps + 1):
            t = i / steps if steps else 0
            self.px(round(x0 + (x1 - x0) * t), round(y0 + (y1 - y0) * t), c, lock)

    def disc(self, cx, cy, r, c):
        for x in range(self.size):
            for y in range(self.size):
                if (x - cx) ** 2 + (y - cy) ** 2 <= r * r + 0.3:
                    self.px(x, y, c)

    def art(self, rows, palette, ox=0, oy=0, lock=''):
        for y, row in enumerate(rows):
            for x, ch in enumerate(row):
                if ch in '. ':
                    continue
                self.px(ox + x, oy + y, palette[ch], ch in lock)

    def outline(self, strength=0.5, inner=True):
        """Darkens every pixel on the edge of the shape, a little more on the lower right than on the upper left."""
        src = self.img.copy()

        def solid(x, y):
            return 0 <= x < self.size and 0 <= y < self.size and src.getpixel((x, y))[3] > 0
        for x in range(self.size):
            for y in range(self.size):
                if not solid(x, y) or (x, y) in self.locked:
                    continue
                c = src.getpixel((x, y))
                lower = not solid(x + 1, y) or not solid(x, y + 1)
                upper = not solid(x - 1, y) or not solid(x, y - 1)
                if lower:
                    self.img.putpixel((x, y), shade(c, strength) + (255,))
                elif upper and inner:
                    self.img.putpixel((x, y), shade(c, strength + 0.22) + (255,))
        return self

    def save(self, path):
        self.img.save(path)


def diagonal_blade(s, base_x, base_y, length, width, light, mid, dark, tip=True, broken=False):
    """A straight blade rising to the upper right from (base_x, base_y), `width` pixels wide: light edge on the upper left."""
    for i in range(length):
        x, y = base_x + i, base_y - i
        for k in range(width):
            c = light if k == 0 else (mid if k < width - 1 or width == 2 else dark)
            if width >= 3 and k == width - 1:
                c = dark
            s.px(x + k, y, c)
        # the step that keeps the diagonal solid
        s.px(x + width, y, dark if width > 1 else mid)
    if tip and not broken:
        x, y = base_x + length, base_y - length
        s.px(x, y, light)
        if width > 2:
            s.px(x + 1, y, mid)
    if broken:
        x, y = base_x + length, base_y - length
        s.px(x + 1, y + 1 - 1, mid)


def hilt(s, x, y, grip, grip_c, grip_d, guard, guard_c, pommel_c):
    """A grip running to the lower left from (x, y), a pommel at its end and a cross guard of half-length `guard` at (x, y)."""
    for i in range(1, grip + 1):
        s.px(x - i, y + i, grip_c if i % 2 else grip_d)
        s.px(x - i + 1, y + i, grip_d)
    s.px(x - grip - 1, y + grip + 1, pommel_c)
    s.px(x - grip, y + grip + 1, shade(pommel_c, 0.8))
    for k in range(-guard, guard + 1):
        s.px(x + k, y + k, guard_c)
        s.px(x + k + 1, y + k, shade(guard_c, 0.8))
