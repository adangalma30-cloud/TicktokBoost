#!/usr/bin/env python3
"""
TickTokBoost v0.0.8 — TB EMBLEM designer.

Four genuinely distinct brand concepts, constructed from raw geometry
(NO typed letters). The winning emblem becomes the single brand asset:
adaptive icon (foreground/background/monochrome), legacy PNGs, and the
in-app/splash symbol (mirrored in TbEmblem.kt — keep the constants in sync).

All coordinates live in the 108-unit icon viewport; the emblem sits inside
the 66dp adaptive safe zone.
"""
import os, math
import cairosvg
from PIL import Image

CYAN, PINK, WHITE = "#25F4EE", "#FE2C55", "#F5F7FF"
BG_TOP, BG_BOT = "#10101B", "#07070D"
GHOST = 3.0  # duotone offset, 108-units (matched in TbEmblem.kt)

# ── path primitives ──────────────────────────────────────────────────────────
def rrect(x0, y0, x1, y1, r):
    return (f"M{x0+r:.2f},{y0:.2f} H{x1-r:.2f} A{r:.2f},{r:.2f} 0 0 1 {x1:.2f},{y0+r:.2f} "
            f"V{y1-r:.2f} A{r:.2f},{r:.2f} 0 0 1 {x1-r:.2f},{y1:.2f} H{x0+r:.2f} "
            f"A{r:.2f},{r:.2f} 0 0 1 {x0:.2f},{y1-r:.2f} V{y0+r:.2f} "
            f"A{r:.2f},{r:.2f} 0 0 1 {x0+r:.2f},{y0:.2f} Z")

def rect(x0, y0, x1, y1):
    return f"M{x0:.2f},{y0:.2f} H{x1:.2f} V{y1:.2f} H{x0:.2f} Z"

def quad(p1, p2, p3, p4):
    return f"M{p1[0]:.2f},{p1[1]:.2f} L{p2[0]:.2f},{p2[1]:.2f} L{p3[0]:.2f},{p3[1]:.2f} L{p4[0]:.2f},{p4[1]:.2f} Z"

def tri(p1, p2, p3):
    return f"M{p1[0]:.2f},{p1[1]:.2f} L{p2[0]:.2f},{p2[1]:.2f} L{p3[0]:.2f},{p3[1]:.2f} Z"

# ═══ CONCEPT A — "MOTION LOCK TB" ═════════════════════════════════════════════
# T's crossbar (rising 3° to the right = boost lift) shares its stem with the
# B's spine — the two letters physically lock into ONE emblem. Duotone layers
# supply the motion. Fused monogram: bar + shared spine + two B bowls.
A_PATHS = [
    quad((27, 32), (77, 29), (77, 37), (27, 40)),   # rising T crossbar
    rect(48, 29, 56, 79),                            # shared spine (T stem = B spine)
    rrect(56, 36, 81, 57, 10),                       # B top bowl (outer)
    rrect(63, 41, 74, 52, 5),                        # B top bowl (counter/hole)
    rrect(56, 57, 83, 79, 11),                       # B bottom bowl (outer)
    rrect(63, 62, 76, 74, 5.5),                      # B bottom bowl (counter/hole)
]
A_D = " ".join(A_PATHS)

# ═══ CONCEPT B — "PLAY-BOWL TB" ═══════════════════════════════════════════════
# The B's lower bowl becomes a solid play wedge pointing forward — social-video
# cue. Upper bowl stays a ring so it still reads as a B.
B_D = " ".join([
    quad((27, 32), (77, 29), (77, 37), (27, 40)),
    rect(48, 29, 56, 79),
    rrect(56, 36, 81, 57, 10),
    rrect(63, 41, 74, 52, 5),
    tri((56, 56), (85, 68), (56, 80)),               # play wedge = lower bowl
])

# ═══ CONCEPT C — "BOOST ASCENT TB" ════════════════════════════════════════════
# Whole mark leans forward; the T's crossbar climbs into an arrowhead —
# explicit upward boost motion.
C_D = " ".join([
    quad((24, 40), (80, 26), (80, 34), (24, 48)),    # climbing crossbar
    tri((80, 19), (93, 31), (79, 39)),               # arrowhead at bar tip
    quad((47, 80), (55, 79), (59, 35), (51, 36)),    # leaning spine
    rrect(58, 36, 83, 57, 10),
    rrect(65, 41, 76, 52, 5),
    rrect(58, 57, 85, 79, 11),
    rrect(65, 62, 78, 74, 5.5),
])

# ═══ CONCEPT D — "OVERPRINT PLANES" ═══════════════════════════════════════════
# Minimal premium: solid cyan T-plane and solid pink B-plane overlapping with
# an overprint effect; letterforms live in the negative space between planes.
D_T = " ".join([rrect(24, 30, 78, 42, 6), rect(46, 30, 56, 80)])
D_B = " ".join([rect(50, 34, 58, 80), rrect(56, 36, 82, 57, 10.5), rrect(56, 57, 84, 79, 11.5)])

# ── rendering ────────────────────────────────────────────────────────────────
def shift(d, dx, dy):
    import re
    def repl(m):
        cmd, args = m.group(1), [float(v) for v in re.findall(r"-?\d+\.?\d*", m.group(2))]
        if not args: return cmd
        nums, i = [], 0
        while i < len(args):
            x = args[i]
            if cmd in "Hh" and i < len(args): nums.append(f"{x+dx:.2f}"); i += 1; continue
            if cmd in "Vv" and i < len(args): nums.append(f"{x+dy:.2f}"); i += 1; continue
            if i + 1 < len(args):
                nums.append(f"{x+dx:.2f}"); nums.append(f"{args[i+1]+dy:.2f}"); i += 2
            else:
                nums.append(f"{x+dx:.2f}"); i += 1
        return f"{cmd} " + " ".join(nums)
    return re.sub(r"([A-Za-z])\s*((?:-?\d+\.?\d*\s*)+)", repl, d)

def svg_duotone(size, d, filltype="evenodd", rounded=True):
    block = 0.58
    s = size * block / 60.0            # emblem width ≈ 60 units incl. ghosts
    ox = size / 2 - 55 * s             # optical center of the A emblem is (55,54)
    oy = size / 2 - 54 * s
    r = size * 0.24 if rounded else 0
    return f'''<svg xmlns="http://www.w3.org/2000/svg" width="{size}" height="{size}" viewBox="0 0 {size} {size}">
<defs><linearGradient id="bg" x1="0" y1="0" x2="0" y2="{size}" gradientUnits="userSpaceOnUse">
<stop offset="0" stop-color="{BG_TOP}"/><stop offset="1" stop-color="{BG_BOT}"/></linearGradient></defs>
<rect width="{size}" height="{size}" rx="{r:.0f}" ry="{r:.0f}" fill="url(#bg)"/>
<g transform="translate({ox:.2f},{oy:.2f}) scale({s:.5f})">
  <path d="{shift(d, -GHOST, -GHOST)}" fill="{CYAN}" fill-rule="{filltype}"/>
  <path d="{shift(d, GHOST, GHOST)}" fill="{PINK}" fill-rule="{filltype}"/>
  <path d="{d}" fill="{WHITE}" fill-rule="{filltype}"/>
</g></svg>'''

def svg_concept_D(size, rounded=True):
    block = 0.58
    s = size * block / 60.0
    ox = size / 2 - 55 * s; oy = size / 2 - 54 * s
    r = size * 0.24 if rounded else 0
    return f'''<svg xmlns="http://www.w3.org/2000/svg" width="{size}" height="{size}" viewBox="0 0 {size} {size}">
<defs><linearGradient id="bg" x1="0" y1="0" x2="0" y2="{size}" gradientUnits="userSpaceOnUse">
<stop offset="0" stop-color="{BG_TOP}"/><stop offset="1" stop-color="{BG_BOT}"/></linearGradient></defs>
<rect width="{size}" height="{size}" rx="{r:.0f}" ry="{r:.0f}" fill="url(#bg)"/>
<g transform="translate({ox:.2f},{oy:.2f}) scale({s:.5f})">
  <path d="{shift(D_T, -2.5, -2.5)}" fill="{CYAN}" fill-rule="nonzero"/>
  <path d="{shift(D_B, 2.5, 2.5)}" fill="{PINK}" fill-opacity="0.92" fill-rule="nonzero"/>
  <path d="{rect(50, 34, 58, 80)}" fill="{WHITE}" fill-opacity="0.9"/>
</g></svg>'''

CONCEPTS = {
    "A_motion_lock":  lambda sz: svg_duotone(sz, A_D),
    "B_play_bowl":    lambda sz: svg_duotone(sz, B_D),
    "C_boost_ascent": lambda sz: svg_duotone(sz, C_D),
    "D_overprint":    lambda sz: svg_concept_D(sz),
}

# ── evaluation at 48px ───────────────────────────────────────────────────────
def evaluate(svg):
    png = "/tmp/eval48.png"
    cairosvg.svg2png(bytestring=svg.encode(), write_to=png, output_width=48, output_height=48)
    img = Image.open(png).convert("RGBA")
    bright = cyan = pink = 0
    xs, ys = [], []
    for y in range(48):
        for x in range(48):
            r, g, b, a = img.getpixel((x, y))
            if a > 200:
                lum = r + g + b
                if lum > 450: bright += 1; xs.append(x); ys.append(y)
                elif g > 150 and b > 150 and r < 130: cyan += 1
                elif r > 150 and g < 110 and b < 150: pink += 1
    span_x = (max(xs) - min(xs)) if xs else 0
    span_y = (max(ys) - min(ys)) if ys else 0
    return {"white": bright, "cyan": cyan, "pink": pink,
            "span_x": span_x, "span_y": span_y}

# ── ship the winner: CONCEPT A → all Android brand assets ───────────────────
def write_android_assets():
    res = "app/src/main/res"
    fg = f'''<?xml version="1.0" encoding="utf-8"?>
<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="108dp"
    android:height="108dp"
    android:viewportWidth="108"
    android:viewportHeight="108">
    <!-- TickTokBoost "Motion Lock" TB emblem: cyan ghost, pink ghost, white face -->
    <path android:fillColor="{CYAN}" android:fillType="evenOdd"
        android:pathData="{shift(A_D, -GHOST, -GHOST)}" />
    <path android:fillColor="{PINK}" android:fillType="evenOdd"
        android:pathData="{shift(A_D, GHOST, GHOST)}" />
    <path android:fillColor="{WHITE}" android:fillType="evenOdd"
        android:pathData="{A_D}" />
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
    <path android:fillColor="#FFFFFFFF" android:fillType="evenOdd" android:pathData="{A_D}" />
</vector>
'''
    open(f"{res}/drawable/ic_launcher_monochrome.xml", "w").write(mono)

    densities = {"mdpi": 48, "hdpi": 72, "xhdpi": 96, "xxhdpi": 144, "xxxhdpi": 192}
    for d, px in densities.items():
        out = f"{res}/mipmap-{d}"
        os.makedirs(out, exist_ok=True)
        svg = svg_duotone(px, A_D)
        cairosvg.svg2png(bytestring=svg.encode(), write_to=f"{out}/ic_launcher.png", output_width=px, output_height=px)
        svg_round = svg.replace(f'rx="{int(px*0.24)}" ry="{int(px*0.24)}"', f'rx="{px/2}" ry="{px/2}"')
        cairosvg.svg2png(bytestring=svg_round.encode(), write_to=f"{out}/ic_launcher_round.png", output_width=px, output_height=px)

    cairosvg.svg2png(bytestring=svg_duotone(512, A_D).encode(), write_to="art/emblem-512.png", output_width=512, output_height=512)
    cairosvg.svg2png(bytestring=svg_duotone(48, A_D).encode(), write_to="art/emblem-48.png", output_width=48, output_height=48)
    print("android brand assets shipped (foreground/background/monochrome + PNGs + previews)")

if __name__ == "__main__":
    os.makedirs("art/concepts", exist_ok=True)
    print(f"{'concept':16} {'white px':>8} {'cyan':>6} {'pink':>6} {'span x':>7} {'span y':>7}")
    for name, fn in CONCEPTS.items():
        cairosvg.svg2png(bytestring=fn(512).encode(), write_to=f"art/concepts/{name}_512.png",
                         output_width=512, output_height=512)
        cairosvg.svg2png(bytestring=fn(48).encode(), write_to=f"art/concepts/{name}_48.png",
                         output_width=48, output_height=48)
        m = evaluate(fn(48))
        print(f"{name:16} {m['white']:>8} {m['cyan']:>6} {m['pink']:>6} {m['span_x']:>7} {m['span_y']:>7}")
    print("concepts rendered to art/concepts/")
    if os.environ.get("SHIP") == "1":
        write_android_assets()

