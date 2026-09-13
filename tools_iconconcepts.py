#!/usr/bin/env python3
"""
TickTokBoost v0.0.10 — ICON REDESIGN: six original concepts.

All rendered in the app's duotone language (cyan ghost ↖, pink ghost ↘,
white face) on near-black, 108-unit viewport, inside the 66dp safe zone.
The winner becomes the single brand asset (adaptive icon + TbEmblem.kt).
"""
import os, math
import cairosvg
from PIL import Image

CYAN, PINK, WHITE = "#25F4EE", "#FE2C55", "#F5F7FF"
BG_TOP, BG_BOT = "#10101B", "#07070D"
GHOST = 3.0

def rpoly(points, r):
    """Rounded polygon path."""
    n = len(points)
    d = []
    for i, (x, y) in enumerate(points):
        px, py = points[i - 1]
        nx, ny = points[(i + 1) % n]
        # unit vectors
        v1 = (x - px, y - py); l1 = math.hypot(*v1); v1 = (v1[0]/l1, v1[1]/l1)
        v2 = (nx - x, ny - y); l2 = math.hypot(*v2); v2 = (v2[0]/l2, v2[1]/l2)
        rr = min(r, l1/2 - 0.01, l2/2 - 0.01)
        a = (x - v1[0]*rr, y - v1[1]*rr)
        b = (x + v2[0]*rr, y + v2[1]*rr)
        d.append(("L" if i else "M") + f"{a[0]:.2f},{a[1]:.2f}")
        d.append(f"Q{x:.2f},{y:.2f} {b[0]:.2f},{b[1]:.2f}")
    d.append("Z")
    return "".join(d)

def circle(cx, cy, r):
    return f"M{cx-r:.2f},{cy} a{r},{r} 0 1 0 {2*r:.2f},0 a{r},{r} 0 1 0 {-2*r:.2f},0 Z"

def rrect(x0, y0, x1, y1, r):
    return (f"M{x0+r},{y0} H{x1-r} A{r},{r} 0 0 1 {x1},{y0+r} V{y1-r} "
            f"A{r},{r} 0 0 1 {x1-r},{y1} H{x0+r} A{r},{r} 0 0 1 {x0},{y1-r} "
            f"V{y0+r} A{r},{r} 0 0 1 {x0+r},{y0} Z")

def bar(x0, y0, x1, y1, r=4):
    return rrect(min(x0,x1), min(y0,y1), max(x0,x1), max(y0,y1), r)

# ── CONCEPT 1 · BOOST+PLAY "Playrise" ────────────────────────────────────────
# A play triangle whose right edge opens into a forward arrow: video + boost.
C1 = rpoly([(36,30),(58,42),(58,30),(88,54),(58,78),(58,66),(36,78)], 5)

# ── CONCEPT 2 · CREATOR+GROWTH "Creator Rise" ────────────────────────────────
# Creator bust + three ascending growth bars.
C2 = " ".join([
    circle(37, 37, 10.5),
    rrect(22, 52, 52, 79, 11),
    rrect(58, 57, 67, 79, 4.5),
    rrect(70, 45, 79, 79, 4.5),
    rrect(82, 32, 91, 79, 4.5),
])

# ── CONCEPT 3 · ABSTRACT TB "Double Play B" ──────────────────────────────────
# T crossbar + shared spine; the B's bowls are two PLAY WEDGES pointing right.
C3 = " ".join([
    bar(26, 30, 66, 40, 5),           # T crossbar
    bar(48, 30, 56, 80, 4),           # spine
    rpoly([(58,34),(84,45),(58,56)], 4),   # B bowl 1 = play wedge
    rpoly([(58,56),(90,68),(58,80)], 5),   # B bowl 2 = play wedge
])

# ── CONCEPT 4 · SOCIAL CONNECTION "Ascending Network" ────────────────────────
# Three creator nodes linked upward; the top node is the biggest.
C4 = " ".join([
    bar(30, 66, 48, 48, 5),           # link 1 (thick diagonal)
    bar(48, 48, 68, 30, 5),           # link 2
    circle(32, 74, 9),                # node 1
    circle(50, 50, 9),                # node 2
    circle(72, 28, 12),               # node 3 (biggest, top)
])

# ── CONCEPT 5 · BOOST SPARK ──────────────────────────────────────────────────
# Bold four-point spark: pure boost energy.
C5 = rpoly([(54,18),(63,45),(90,54),(63,63),(54,90),(45,63),(18,54),(45,45)], 6)

# ── CONCEPT 6 · HYBRID "Ascent Play + Creator" ───────────────────────────────
# Play triangle rotated to point UP-RIGHT (video + growth direction), with a
# creator dot carved at its heart (negative space).
C6 = rpoly([(28,80),(80,28),(82,84)], 9) + " " + circle(60, 60, 7)

CONCEPTS = {
    "1_playrise":        C1,
    "2_creator_rise":    C2,
    "3_double_play_b":   C3,
    "4_network":         C4,
    "5_spark":           C5,
    "6_ascent_play":     C6,
}

def shift(d, dx, dy):
    import re
    def repl(m):
        cmd, args = m.group(1), [float(v) for v in re.findall(r"-?\d+\.?\d*", m.group(2))]
        if not args: return cmd
        nums, i = [], 0
        while i < len(args):
            x = args[i]
            if cmd in "Hh": nums.append(f"{x+dx:.2f}"); i += 1; continue
            if cmd in "Vv": nums.append(f"{x+dy:.2f}"); i += 1; continue
            if i + 1 < len(args):
                nums.append(f"{x+dx:.2f}"); nums.append(f"{args[i+1]+dy:.2f}"); i += 2
            else:
                nums.append(f"{x+dx:.2f}"); i += 1
        return f"{cmd} " + " ".join(nums)
    return re.sub(r"([A-Za-z])\s*((?:-?\d+\.?\d*\s*)+)", repl, d)

def svg(size, d, cx=54.0, cy=54.0, block=0.42):
    s = size * block / 60.0
    ox = size/2 - cx*s; oy = size/2 - cy*s
    r = size * 0.24
    return f'''<svg xmlns="http://www.w3.org/2000/svg" width="{size}" height="{size}" viewBox="0 0 {size} {size}">
<defs><linearGradient id="bg" x1="0" y1="0" x2="0" y2="{size}" gradientUnits="userSpaceOnUse">
<stop offset="0" stop-color="{BG_TOP}"/><stop offset="1" stop-color="{BG_BOT}"/></linearGradient></defs>
<rect width="{size}" height="{size}" rx="{r:.0f}" ry="{r:.0f}" fill="url(#bg)"/>
<g transform="translate({ox:.2f},{oy:.2f}) scale({s:.5f})">
  <path d="{shift(d, -GHOST, -GHOST)}" fill="{CYAN}" fill-rule="evenodd"/>
  <path d="{shift(d, GHOST, GHOST)}" fill="{PINK}" fill-rule="evenodd"/>
  <path d="{d}" fill="{WHITE}" fill-rule="evenodd"/>
</g></svg>'''

# optical centers (content barycenters) per concept
CENTERS = {
    "1_playrise": (62, 54), "2_creator_rise": (56.5, 52.75), "3_double_play_b": (57, 55),
    "4_network": (52, 51), "5_spark": (54, 54), "6_ascent_play": (61, 63),
}

def evaluate(d, cx, cy):
    png = "/tmp/eval.png"
    cairosvg.svg2png(bytestring=svg(48, d, cx, cy).encode(), write_to=png, output_width=48, output_height=48)
    img = Image.open(png).convert("RGBA")
    white = cyan = pink = 0
    xs, ys = [], []
    for y in range(48):
        for x in range(48):
            r, g, b, a = img.getpixel((x, y))
            if a > 200:
                lum = r + g + b
                if lum > 450: white += 1; xs.append(x); ys.append(y)
                elif g > 150 and b > 150 and r < 130: cyan += 1
                elif r > 150 and g < 110 and b < 150: pink += 1
    return white, cyan, pink, (max(xs)-min(xs) if xs else 0), (max(ys)-min(ys) if ys else 0)

# ── SHIP: winner -> all Android brand assets ────────────────────────────────
def write_android_assets(winner="2_creator_rise"):
    d = CONCEPTS[winner]
    cx, cy = CENTERS[winner]
    res = "app/src/main/res"
    fg = f'''<?xml version="1.0" encoding="utf-8"?>
<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="108dp"
    android:height="108dp"
    android:viewportWidth="108"
    android:viewportHeight="108">
    <!-- TickTokBoost "Creator Rise": creator + ascending growth, duotone.
         Scaled to 60% around the optical center so the artwork sits well
         inside the 66dp adaptive safe zone (no launcher-mask cropping). -->
    <group android:scaleX="0.60" android:scaleY="0.60" android:pivotX="55" android:pivotY="53">
        <path android:fillColor="{CYAN}" android:fillType="evenOdd"
            android:pathData="{shift(d, -GHOST, -GHOST)}" />
        <path android:fillColor="{PINK}" android:fillType="evenOdd"
            android:pathData="{shift(d, GHOST, GHOST)}" />
        <path android:fillColor="{WHITE}" android:fillType="evenOdd"
            android:pathData="{d}" />
    </group>
</vector>
'''
    open(f"{res}/drawable/ic_launcher_foreground.xml", "w").write(fg)

    bg = f'''<?xml version="1.0" encoding="utf-8"?>
<vector xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:aapt="http://schemas.android.com/aapt"
    android:width="108dp"
    android:height="108dp"
    android:viewportWidth="108"
    android:viewportHeight="108">
    <path android:pathData="M0,0 H108 V108 H0 Z">
        <aapt:attr name="android:fillColor">
            <gradient android:startX="0" android:startY="0" android:endX="0" android:endY="108" android:type="linear">
                <item android:offset="0.0" android:color="{BG_TOP}" />
                <item android:offset="1.0" android:color="{BG_BOT}" />
            </gradient>
        </aapt:attr>
    </path>
</vector>
'''
    open(f"{res}/drawable/ic_launcher_background.xml", "w").write(bg)

    mono = f'''<?xml version="1.0" encoding="utf-8"?>
<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="108dp"
    android:height="108dp"
    android:viewportWidth="108"
    android:viewportHeight="108">
    <group android:scaleX="0.60" android:scaleY="0.60" android:pivotX="55" android:pivotY="53">
        <path android:fillColor="#FFFFFFFF" android:fillType="evenOdd" android:pathData="{d}" />
    </group>
</vector>
'''
    open(f"{res}/drawable/ic_launcher_monochrome.xml", "w").write(mono)

    densities = {"mdpi": 48, "hdpi": 72, "xhdpi": 96, "xxhdpi": 144, "xxxhdpi": 192}
    for den, px in densities.items():
        out = f"{res}/mipmap-{den}"
        os.makedirs(out, exist_ok=True)
        base = svg(px, d, cx, cy)
        cairosvg.svg2png(bytestring=base.encode(), write_to=f"{out}/ic_launcher.png", output_width=px, output_height=px)
        rnd = base.replace(f'rx="{int(px*0.24)}" ry="{int(px*0.24)}"', f'rx="{px/2}" ry="{px/2}"')
        cairosvg.svg2png(bytestring=rnd.encode(), write_to=f"{out}/ic_launcher_round.png", output_width=px, output_height=px)

    cairosvg.svg2png(bytestring=svg(512, d, cx, cy).encode(), write_to="art/winner-512.png", output_width=512, output_height=512)
    cairosvg.svg2png(bytestring=svg(48, d, cx, cy).encode(), write_to="art/winner-48.png", output_width=48, output_height=48)
    print(f"shipped {winner} to all Android assets")

if __name__ == "__main__":
    os.makedirs("art/iconconcepts", exist_ok=True)
    print(f"{'concept':18} {'white':>6} {'cyan':>5} {'pink':>5} {'spanx':>6} {'spany':>6}")
    for name, d in CONCEPTS.items():
        cx, cy = CENTERS[name]
        cairosvg.svg2png(bytestring=svg(512, d, cx, cy).encode(),
                         write_to=f"art/iconconcepts/{name}_512.png", output_width=512, output_height=512)
        cairosvg.svg2png(bytestring=svg(48, d, cx, cy).encode(),
                         write_to=f"art/iconconcepts/{name}_48.png", output_width=48, output_height=48)
        w, c, p, sx, sy = evaluate(d, cx, cy)
        print(f"{name:18} {w:>6} {c:>5} {p:>5} {sx:>6} {sy:>6}")
    print("rendered to art/iconconcepts/")
    if os.environ.get("SHIP") == "1":
        write_android_assets()


