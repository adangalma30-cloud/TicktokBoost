#!/usr/bin/env python3
"""
TikTokBoost v0.0.2 icon generator — original 'Ascent Trio' mark.

One geometry definition -> every Android icon surface:
  * adaptive icon vector drawables (foreground w/ gradient, background, monochrome)
  * legacy launcher PNGs (square + round, all densities)
  * splash/marketing renders

The mark: three ascending creators (community) rising along a bold
boost-arrow (growth) on a clean light background. 100% original geometry.
"""
import os, math
import cairosvg

# ---------------------------------------------------------------- geometry (108 viewport)
FIGURES = [  # (head_cx, head_cy, head_r, torso_x, torso_w, torso_y, torso_h, torso_r)
    (39.0, 47.5, 6.2, 32.4, 13.2, 55.5, 12.5, 4.4),
    (52.5, 44.5, 7.4, 44.6, 15.8, 53.6, 14.4, 5.1),
    (67.5, 41.5, 8.8, 57.9, 19.2, 49.0, 19.0, 6.2),
]
FIG_COLORS = ["#9C9CB0", "#4DE0DC", "#FF7A96"]

A, B     = (27.0, 60.0), (64.0, 32.0)   # arrow shaft
HALF     = 4.2
TIP      = (69.0, 29.5)
WING     = 8.2

ARROW_FROM, ARROW_TO = "#25F4EE", "#FE2C55"
BG_FROM, BG_TO       = "#101018", "#06060C"
MIDNIGHT             = "#0A0A12"

class X:
    """coordinate transformer: 108-viewport -> output space"""
    def __init__(self, scale=1.0, ox=0.0, oy=0.0):
        self.s, self.ox, self.oy = scale, ox, oy
    def p(self, x, y):
        return (x*self.s + self.ox, y*self.s + self.oy)
    def n(self, v):
        return v*self.s

def circle_path(t, cx, cy, r):
    cx, cy = t.p(cx, cy); r = t.n(r)
    return f"M {cx-r:.2f},{cy:.2f} a {r:.2f},{r:.2f} 0 1 0 {2*r:.2f},0 a {r:.2f},{r:.2f} 0 1 0 {-2*r:.2f},0 Z"

def rrect_path(t, x, y, w, h, r):
    x0, y0 = t.p(x, y); x1, y1 = t.p(x+w, y+h); r = t.n(r)
    return (f"M {x0+r:.2f},{y0:.2f} H {x1-r:.2f} A {r:.2f},{r:.2f} 0 0 1 {x1:.2f},{y0+r:.2f} "
            f"V {y1-r:.2f} A {r:.2f},{r:.2f} 0 0 1 {x1-r:.2f},{y1:.2f} H {x0+r:.2f} "
            f"A {r:.2f},{r:.2f} 0 0 1 {x0:.2f},{y1-r:.2f} V {y0+r:.2f} A {r:.2f},{r:.2f} 0 0 1 {x0+r:.2f},{y0:.2f} Z")

def arrow_paths(t):
    dx, dy = B[0]-A[0], B[1]-A[1]
    L = math.hypot(dx, dy)
    ux, uy = dx/L, dy/L
    px, py = -uy, ux
    def off(pt, k): return t.p(pt[0]+px*k, pt[1]+py*k)
    c1, c2 = off(A, HALF), off(A, -HALF)
    c3, c4 = off(B, -HALF), off(B, HALF)
    shaft = f"M {c1[0]:.2f},{c1[1]:.2f} L {c4[0]:.2f},{c4[1]:.2f} L {c3[0]:.2f},{c3[1]:.2f} L {c2[0]:.2f},{c2[1]:.2f} Z"
    w1, w2 = off(B, WING), off(B, -WING)
    tip = t.p(*TIP)
    head = f"M {tip[0]:.2f},{tip[1]:.2f} L {w1[0]:.2f},{w1[1]:.2f} L {w2[0]:.2f},{w2[1]:.2f} Z"
    return shaft, head

def figure_paths(t):
    return [(circle_path(t, hx, hy, hr), rrect_path(t, tx, ty, tw, th, tr)) for hx,hy,hr,tx,tw,ty,th,tr in FIGURES]

# ---------------------------------------------------------------- adaptive vector drawables
def write_vector_foreground(path):
    t = X()
    shaft, head = arrow_paths(t)
    figs = figure_paths(t)
    ax, ay = t.p(*A); tx, ty = t.p(*TIP)
    p = ['<?xml version="1.0" encoding="utf-8"?>',
         '<vector xmlns:android="http://schemas.android.com/apk/res/android"',
         '    xmlns:aapt="http://schemas.android.com/aapt"',
         '    android:width="108dp"',
         '    android:height="108dp"',
         '    android:viewportWidth="108"',
         '    android:viewportHeight="108">',
         '    <!-- TikTokBoost "Ascent Trio" mark: creators rising along a boost arrow -->',
         f'    <path android:pathData="{shaft}">',
         '        <aapt:attr name="android:fillColor">',
         f'            <gradient android:startX="{ax}" android:startY="{ay}" android:endX="{tx}" android:endY="{ty}" android:type="linear">',
         f'                <item android:offset="0.0" android:color="{ARROW_FROM}" />',
         f'                <item android:offset="1.0" android:color="{ARROW_TO}" />',
         '            </gradient>',
         '        </aapt:attr>',
         '    </path>',
         f'    <path android:fillColor="{ARROW_TO}" android:pathData="{head}" />']
    for (headp, torsop), col in zip(figs, FIG_COLORS):
        p.append(f'        <path android:fillColor="{col}" android:pathData="{headp}" />')
        p.append(f'        <path android:fillColor="{col}" android:pathData="{torsop}" />')
    p += ['</vector>', '']
    open(path, "w").write("\n".join(p))

def write_vector_background(path):
    p = ['<?xml version="1.0" encoding="utf-8"?>',
         '<vector xmlns:android="http://schemas.android.com/apk/res/android"',
         '    xmlns:aapt="http://schemas.android.com/aapt"',
         '    android:width="108dp" android:height="108dp"',
         '    android:viewportWidth="108" android:viewportHeight="108">',
         '    <path android:pathData="M0,0 H108 V108 H0 Z">',
         '        <aapt:attr name="android:fillColor">',
         '            <gradient android:startX="0" android:startY="0" android:endX="108" android:endY="108" android:type="linear">',
         f'                <item android:offset="0.0" android:color="{BG_FROM}" />',
         f'                <item android:offset="1.0" android:color="{BG_TO}" />',
         '            </gradient>',
         '        </aapt:attr>',
         '    </path>',
         '</vector>', '']
    open(path, "w").write("\n".join(p))

def write_vector_monochrome(path):
    t = X()
    shaft, head = arrow_paths(t)
    figs = figure_paths(t)
    p = ['<?xml version="1.0" encoding="utf-8"?>',
         '<vector xmlns:android="http://schemas.android.com/apk/res/android"',
         '    android:width="108dp" android:height="108dp"',
         '    android:viewportWidth="108" android:viewportHeight="108">',
         f'    <path android:fillColor="#FFFFFFFF" android:pathData="{shaft}" />',
         f'    <path android:fillColor="#FFFFFFFF" android:pathData="{head}" />']
    for headp, torsop in figs:
        p.append(f'    <path android:fillColor="#FFFFFFFF" android:pathData="{headp}" />')
        p.append(f'    <path android:fillColor="#FFFFFFFF" android:pathData="{torsop}" />')
    p += ['</vector>', '']
    open(path, "w").write("\n".join(p))

# ---------------------------------------------------------------- SVG renders
def mark_svg(scale=1.0, mono=False, ox=0.0, oy=0.0):
    t = X(scale, ox, oy)
    shaft, head = arrow_paths(t)
    figs = figure_paths(t)
    s = []
    if mono:
        s.append(f'<path d="{shaft} {head}" fill="#FFFFFF"/>')
        for hp, tp in figs:
            s.append(f'<path d="{hp} {tp}" fill="#FFFFFF"/>')
    else:
        ax, ay = t.p(*A); tx, ty = t.p(*TIP)
        s.append(f'<linearGradient id="ag" x1="{ax:.2f}" y1="{ay:.2f}" x2="{tx:.2f}" y2="{ty:.2f}" gradientUnits="userSpaceOnUse">'
                 f'<stop offset="0" stop-color="{ARROW_FROM}"/><stop offset="1" stop-color="{ARROW_TO}"/></linearGradient>')
        s.append(f'<path d="{shaft} {head}" fill="url(#ag)"/>')
        for (hp, tp), col in zip(figs, FIG_COLORS):
            s.append(f'<path d="{hp} {tp}" fill="{col}"/>')
    return "\n".join(s)

def full_icon_svg(size):
    r = size * 0.24
    return f'''<svg xmlns="http://www.w3.org/2000/svg" width="{size}" height="{size}" viewBox="0 0 {size} {size}">
<defs><linearGradient id="bg" x1="0" y1="0" x2="{size}" y2="{size}" gradientUnits="userSpaceOnUse">
<stop offset="0" stop-color="{BG_FROM}"/><stop offset="1" stop-color="{BG_TO}"/></linearGradient></defs>
<rect width="{size}" height="{size}" rx="{r:.0f}" ry="{r:.0f}" fill="url(#bg)"/>
{mark_svg(size/108.0)}
</svg>'''

def round_icon_svg(size):
    return f'''<svg xmlns="http://www.w3.org/2000/svg" width="{size}" height="{size}" viewBox="0 0 {size} {size}">
<defs><linearGradient id="bg" x1="0" y1="0" x2="{size}" y2="{size}" gradientUnits="userSpaceOnUse">
<stop offset="0" stop-color="{BG_FROM}"/><stop offset="1" stop-color="{BG_TO}"/></linearGradient></defs>
<circle cx="{size/2}" cy="{size/2}" r="{size/2}" fill="url(#bg)"/>
{mark_svg(size/108.0)}
</svg>'''

def mono_badge_svg(size, bg=MIDNIGHT):
    pad = size * 0.10
    r = size * 0.22
    return f'''<svg xmlns="http://www.w3.org/2000/svg" width="{size}" height="{size}" viewBox="0 0 {size} {size}">
<rect width="{size}" height="{size}" rx="{r:.0f}" ry="{r:.0f}" fill="{bg}"/>
{mark_svg((size-2*pad)/108.0, mono=True, ox=pad, oy=pad)}
</svg>'''

if __name__ == "__main__":
    res = "app/src/main/res"
    draw = f"{res}/drawable"
    os.makedirs(draw, exist_ok=True)

    write_vector_foreground(f"{draw}/ic_launcher_foreground.xml")
    write_vector_background(f"{draw}/ic_launcher_background.xml")
    write_vector_monochrome(f"{draw}/ic_launcher_monochrome.xml")

    densities = {"mdpi": 48, "hdpi": 72, "xhdpi": 96, "xxhdpi": 144, "xxxhdpi": 192}
    for d, px in densities.items():
        outdir = f"{res}/mipmap-{d}"
        os.makedirs(outdir, exist_ok=True)
        cairosvg.svg2png(bytestring=full_icon_svg(px).encode(), write_to=f"{outdir}/ic_launcher.png", output_width=px, output_height=px)
        cairosvg.svg2png(bytestring=round_icon_svg(px).encode(), write_to=f"{outdir}/ic_launcher_round.png", output_width=px, output_height=px)

    os.makedirs("art", exist_ok=True)
    cairosvg.svg2png(bytestring=full_icon_svg(1024).encode(), write_to="art/icon-1024.png", output_width=1024, output_height=1024)
    cairosvg.svg2png(bytestring=full_icon_svg(512).encode(), write_to="art/icon-preview.png", output_width=512, output_height=512)
    cairosvg.svg2png(bytestring=round_icon_svg(512).encode(), write_to="art/icon-round-preview.png", output_width=512, output_height=512)
    cairosvg.svg2png(bytestring=mono_badge_svg(512).encode(), write_to="art/icon-mono-preview.png", output_width=512, output_height=512)
    print("done")
