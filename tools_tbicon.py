#!/usr/bin/env python3
"""
TickTokBoost v0.0.7 launcher icon — "TB" monogram, TikTok-style duotone.

Design: bold TB initials (outlines extracted from the app's bundled Roboto
Black) rendered three times — cyan ghost offset up-left, pink ghost offset
down-right, solid white face on top — over a near-black rounded square with
faint corner glows. Original TickTokBoost design; no TikTok assets.

One geometry source -> adaptive foreground/background/monochrome vectors +
legacy PNGs (all densities) + previews, all within the 66dp adaptive safe zone.
"""
import os, math
from fontTools.ttLib import TTFont
from fontTools.pens.svgPathPen import SVGPathPen
import cairosvg

FONT = "app/src/main/res/font/roboto_black.ttf"
CYAN, PINK, WHITE = "#25F4EE", "#FE2C55", "#F5F7FF"
BG_TOP, BG_BOT = "#10101B", "#07070D"
GHOST = 3.0          # duotone offset (108-viewport units)
CAP_HEIGHT = 34.0    # glyph cap height; TB block stays inside the 66dp safe circle
LETTER_SPACING = -1.0

# ── extract T & B outlines from the bundled font ────────────────────────────
font = TTFont(FONT)
upm = font["head"].unitsPerEm
glyph_set = font.getGlyphSet()
cmap = font.getBestCmap()

def glyph_path(ch):
    name = cmap[ord(ch)]
    pen = SVGPathPen(glyph_set)
    glyph_set[name].draw(pen)
    return pen.getCommands()

def glyph_bbox(ch):
    from fontTools.pens.boundsPen import BoundsPen
    name = cmap[ord(ch)]
    bp = BoundsPen(glyph_set)
    glyph_set[name].draw(bp)
    return bp.bounds  # (xMin, yMin, xMax, yMax) or None

def transform_path(d, scale, dx, dy):
    """font-space -> 108-space: y-flip, scale, translate. Token-wise transform."""
    import re
    out, i = [], 0
    for tok in re.split(r"([MmLlHhVvCcSsQqTtAaZz ,\-0-9.]+)", d):
        pass
    # simpler: regex over commands with numeric args
    def repl(m):
        cmd, args = m.group(1), [float(v) for v in re.findall(r"-?\d+\.?\d*", m.group(2))]
        if not args:
            return cmd
        pairs = []
        for k in range(0, len(args), 2):
            if k + 1 < len(args) or cmd in "HhVv":
                pairs.append(args[k])
                if k + 1 < len(args):
                    pairs.append(args[k + 1])
        nums = []
        idx = 0
        while idx < len(args):
            x = args[idx]
            if cmd in "Hh" and idx < len(args):
                nx = dx + x * scale; nums.append(f"{nx:.2f}"); idx += 1; continue
            if cmd in "Vv" and idx < len(args):
                ny = dy - x * scale; nums.append(f"{ny:.2f}"); idx += 1; continue
            if idx + 1 < len(args):
                nx = dx + x * scale
                ny = dy - args[idx + 1] * scale
                nums.append(f"{nx:.2f}"); nums.append(f"{ny:.2f}")
                idx += 2
            else:
                nx = dx + x * scale
                nums.append(f"{nx:.2f}")
                idx += 1
        return f"{cmd} " + " ".join(nums)
    return re.sub(r"([A-Za-z])\s*((?:-?\d+\.?\d*\s*)+)", repl, d)

# compose the TB block centered at (54, 54)
t_bbox = glyph_bbox("T"); b_bbox = glyph_bbox("B")
cap_units = t_bbox[3] - t_bbox[1]          # real cap height in font units (T spans 0..cap)
scale = CAP_HEIGHT / cap_units             # scale by ACTUAL cap height, not em size
t_w = (t_bbox[2] - t_bbox[0]) * scale
b_w = (b_bbox[2] - b_bbox[0]) * scale
t_xmin = t_bbox[0] * scale
b_xmin = b_bbox[0] * scale
total_w = t_w + LETTER_SPACING + b_w
x0 = 54 - total_w / 2
y_base = 54 + CAP_HEIGHT / 2   # baseline so cap block is vertically centered

t_d = transform_path(glyph_path("T"), scale, x0 - t_xmin, y_base)
b_x = x0 + t_w + LETTER_SPACING - b_xmin
b_d = transform_path(glyph_path("B"), scale, b_x, y_base)
TB_D = t_d + " " + b_d

def shifted(d, dx, dy):
    import re
    def repl(m):
        cmd, args = m.group(1), [float(v) for v in re.findall(r"-?\d+\.?\d*", m.group(2))]
        if not args:
            return cmd
        nums = []
        idx = 0
        while idx < len(args):
            x = args[idx]
            if cmd in "Hh" and idx < len(args):
                nums.append(f"{x + dx:.2f}"); idx += 1; continue
            if cmd in "Vv" and idx < len(args):
                nums.append(f"{x + dy:.2f}"); idx += 1; continue
            if idx + 1 < len(args):
                nums.append(f"{x + dx:.2f}"); nums.append(f"{args[idx + 1] + dy:.2f}")
                idx += 2
            else:
                nums.append(f"{x + dx:.2f}"); idx += 1
        return f"{cmd} " + " ".join(nums)
    return re.sub(r"([A-Za-z])\s*((?:-?\d+\.?\d*\s*)+)", repl, d)

# ── adaptive vector drawables ────────────────────────────────────────────────
def write_foreground(path):
    xml = f'''<?xml version="1.0" encoding="utf-8"?>
<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="108dp"
    android:height="108dp"
    android:viewportWidth="108"
    android:viewportHeight="108">
    <!-- TickTokBoost "TB" monogram: cyan ghost, pink ghost, white face -->
    <path android:fillColor="{CYAN}" android:pathData="{shifted(TB_D, -GHOST, -GHOST)}" />
    <path android:fillColor="{PINK}" android:pathData="{shifted(TB_D, GHOST, GHOST)}" />
    <path android:fillColor="{WHITE}" android:pathData="{TB_D}" />
</vector>
'''
    open(path, "w").write(xml)

def write_background(path):
    xml = f'''<?xml version="1.0" encoding="utf-8"?>
<vector xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:aapt="http://schemas.android.com/aapt"
    android:width="108dp"
    android:height="108dp"
    android:viewportWidth="108"
    android:viewportHeight="108">
    <!-- near-black base -->
    <path android:pathData="M0,0 H108 V108 H0 Z">
        <aapt:attr name="android:fillColor">
            <gradient android:startX="0" android:startY="0" android:endX="0" android:endY="108" android:type="linear">
                <item android:offset="0.0" android:color="{BG_TOP}" />
                <item android:offset="1.0" android:color="{BG_BOT}" />
            </gradient>
        </aapt:attr>
    </path>
    <!-- faint neon glows, top-left cyan / bottom-right pink -->
    <path android:fillColor="#25F4EE" android:fillAlpha="0.07"
        android:pathData="M0,0 m-40,-40 a58,58 0 1 0 116,0 a58,58 0 1 0 -116,0" />
    <path android:fillColor="#FE2C55" android:fillAlpha="0.07"
        android:pathData="M108,108 m-40,-40 a58,58 0 1 0 116,0 a58,58 0 1 0 -116,0" />
</vector>
'''
    open(path, "w").write(xml)

def write_monochrome(path):
    xml = f'''<?xml version="1.0" encoding="utf-8"?>
<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="108dp"
    android:height="108dp"
    android:viewportWidth="108"
    android:viewportHeight="108">
    <path android:fillColor="#FFFFFFFF" android:pathData="{TB_D}" />
</vector>
'''
    open(path, "w").write(xml)

# ── legacy PNGs + previews (content ~66% of canvas, centered) ───────────────
def icon_svg(size, rounded=True):
    block_w = total_w + 2 * GHOST          # TB block incl. ghosts, 108-units
    s = 0.58 * size / block_w              # block spans 58% of the canvas
    ox = size / 2 - 54 * s                 # center the 108-viewport content
    oy = ox
    r = size * 0.24 if rounded else 0
    ghosts = f'''
    <g transform="translate({ox:.2f},{oy:.2f}) scale({s:.5f})">
        <path d="{shifted(TB_D, -GHOST, -GHOST)}" fill="{CYAN}"/>
        <path d="{shifted(TB_D, GHOST, GHOST)}" fill="{PINK}"/>
        <path d="{TB_D}" fill="{WHITE}"/>
    </g>'''
    return f'''<svg xmlns="http://www.w3.org/2000/svg" width="{size}" height="{size}" viewBox="0 0 {size} {size}">
<defs><linearGradient id="bg" x1="0" y1="0" x2="0" y2="{size}" gradientUnits="userSpaceOnUse">
<stop offset="0" stop-color="{BG_TOP}"/><stop offset="1" stop-color="{BG_BOT}"/></linearGradient>
<radialGradient id="gc" cx="0.12" cy="0.08" r="0.42"><stop offset="0" stop-color="{CYAN}" stop-opacity="0.10"/><stop offset="1" stop-color="{CYAN}" stop-opacity="0"/></radialGradient>
<radialGradient id="gp" cx="0.88" cy="0.92" r="0.42"><stop offset="0" stop-color="{PINK}" stop-opacity="0.10"/><stop offset="1" stop-color="{PINK}" stop-opacity="0"/></radialGradient></defs>
<rect width="{size}" height="{size}" rx="{r:.0f}" ry="{r:.0f}" fill="url(#bg)"/>
<rect width="{size}" height="{size}" rx="{r:.0f}" ry="{r:.0f}" fill="url(#gc)"/>
<rect width="{size}" height="{size}" rx="{r:.0f}" ry="{r:.0f}" fill="url(#gp)"/>
{ghosts}
</svg>'''

def round_svg(size):
    return icon_svg(size, rounded=True).replace(
        f'<rect width="{size}" height="{size}" rx="{int(size*0.24)}" ry="{int(size*0.24)}"',
        f'<rect width="{size}" height="{size}" rx="{size/2}" ry="{size/2}"', 2)

if __name__ == "__main__":
    res = "app/src/main/res"
    write_foreground(f"{res}/drawable/ic_launcher_foreground.xml")
    write_background(f"{res}/drawable/ic_launcher_background.xml")
    write_monochrome(f"{res}/drawable/ic_launcher_monochrome.xml")

    densities = {"mdpi": 48, "hdpi": 72, "xhdpi": 96, "xxhdpi": 144, "xxxhdpi": 192}
    for d, px in densities.items():
        out = f"{res}/mipmap-{d}"
        os.makedirs(out, exist_ok=True)
        cairosvg.svg2png(bytestring=icon_svg(px).encode(), write_to=f"{out}/ic_launcher.png", output_width=px, output_height=px)
        # round variant: same art, circular mask
        svg = icon_svg(px)
        svg = svg.replace("<svg ", "<svg ", 1).replace(
            f'rx="{int(px*0.24)}" ry="{int(px*0.24)}"', f'rx="{px/2}" ry="{px/2}"')
        cairosvg.svg2png(bytestring=svg.encode(), write_to=f"{out}/ic_launcher_round.png", output_width=px, output_height=px)

    os.makedirs("art", exist_ok=True)
    cairosvg.svg2png(bytestring=icon_svg(512).encode(), write_to="art/icon-512.png", output_width=512, output_height=512)
    cairosvg.svg2png(bytestring=icon_svg(48).encode(), write_to="art/icon-48.png", output_width=48, output_height=48)
    print("TB icon generated")
