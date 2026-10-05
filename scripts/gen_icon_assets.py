# -*- coding: utf-8 -*-
"""Generate ShizuSU launcher icon assets from 平视胸像 source image."""
import os
from PIL import Image, ImageDraw

SRC = r"I:\文档\sukisuultra\ShizuSU_app_icon_平视胸像.png"
RES = r"I:\文档\sukisuultra\manager\app\src\main\res"
NODPI = os.path.join(RES, "drawable-nodpi")
os.makedirs(NODPI, exist_ok=True)

img = Image.open(SRC).convert("RGBA")
print("source size:", img.size)

# ---- edge color (for adaptive background) ----
W, H = img.size
edge = img.crop((0, 0, W // 4, H // 4)).resize((1, 1))
bg = edge.getpixel((0, 0))
print("corner avg color (RGBA):", bg)
bg_rgb = "#%02X%02X%02X" % bg[:3]

# ---- 1. legacy square + round webp at each density ----
densities = {"mdpi": 48, "hdpi": 72, "xhdpi": 96, "xxhdpi": 144, "xxxhdpi": 192}
for d, size in densities.items():
    sq = img.resize((size, size), Image.LANCZOS)
    sq_path = os.path.join(RES, "mipmap-%s" % d, "ic_launcher.webp")
    sq.save(sq_path, "WEBP", quality=90, method=6)
    print("wrote", sq_path)

    # round: circular alpha mask with ~1.5% margin
    mask = Image.new("L", (size, size), 0)
    dr = ImageDraw.Draw(mask)
    r = size * 0.485
    dr.ellipse((size / 2 - r, size / 2 - r, size / 2 + r, size / 2 + r), fill=255)
    rnd = sq.copy()
    rnd.putalpha(mask)
    rnd_path = os.path.join(RES, "mipmap-%s" % d, "ic_launcher_round.webp")
    rnd.save(rnd_path, "WEBP", quality=90, method=6)
    print("wrote", rnd_path)

# ---- 2. adaptive foreground (1024 canvas, content at 66% safe zone) ----
FG = 1024
safe = int(FG * 0.66)
fg = Image.new("RGBA", (FG, FG), (0, 0, 0, 0))
content = img.resize((safe, safe), Image.LANCZOS)
fg.paste(content, ((FG - safe) // 2, (FG - safe) // 2), content)
fg_path = os.path.join(NODPI, "ic_launcher_foreground.png")
fg.save(fg_path, "PNG")
print("wrote", fg_path)

alt_path = os.path.join(NODPI, "ic_launcher_foreground_alt.png")
fg.save(alt_path, "PNG")
print("wrote", alt_path)

# ---- 3. print background color for colors.xml ----
print("SUGGESTED_BG=" + bg_rgb)
