#!/usr/bin/env python3
"""Render NookScreen launcher assets from the transparent mark. Requires Pillow."""
from pathlib import Path
from xml.etree import ElementTree

from PIL import Image, ImageDraw, ImageFont, ImageOps

ROOT = Path(__file__).resolve().parents[1]
BRAND = ROOT / "docs/brand"
RES = ROOT / "core/src/main/res"
FONT = RES / "font/inter.ttf"
BACKGROUND_XML = RES / "drawable/ic_launcher_background.xml"
ANDROID_COLOR = "{http://schemas.android.com/apk/res/android}color"
BACKGROUND = ElementTree.parse(BACKGROUND_XML).getroot().find("solid").attrib[ANDROID_COLOR]
DENSITIES = {"mdpi": 80, "hdpi": 120, "xhdpi": 160, "xxhdpi": 240, "xxxhdpi": 320}


def place_mark(canvas, mark, width, height, center):
    logo = ImageOps.contain(mark, (width, height), Image.Resampling.LANCZOS)
    origin = (round(center[0] - logo.width / 2), round(center[1] - logo.height / 2))
    canvas.alpha_composite(logo, origin)


def adaptive_foreground(mark):
    # Android lays out the layers on 108 dp, keeping the central mark inside the system mask.
    foreground = Image.new("RGBA", (432, 432))
    place_mark(foreground, mark, 54 * 4, 48 * 4, (216, 216))
    return foreground


def legacy_icon(mark, size):
    icon = Image.new("RGBA", (size, size), BACKGROUND)
    place_mark(icon, mark, round(size * 54 / 72), round(size * 48 / 72), (size / 2, size / 2))
    mask = Image.new("L", (size * 4, size * 4))
    ImageDraw.Draw(mask).rounded_rectangle((0, 0, size * 4 - 1, size * 4 - 1), radius=round(size * .22 * 4), fill=255)
    icon.putalpha(mask.resize((size, size), Image.Resampling.LANCZOS))
    return icon


def tv_banner(mark, width):
    scale = width / 320
    height = round(180 * scale)
    banner = Image.new("RGBA", (width, height), BACKGROUND)
    place_mark(banner, mark, round(70 * scale), round(62 * scale), (66 * scale, height / 2))
    font = ImageFont.truetype(FONT, round(29 * scale))
    font.set_variation_by_axes([32, 650])
    ImageDraw.Draw(banner).text((117 * scale, height / 2), "NookScreen", font=font, fill="#F7F0DD", anchor="lm")
    return banner.convert("RGB")


def save(image, path):
    path.parent.mkdir(parents=True, exist_ok=True)
    image.save(path, optimize=True)


def main():
    with Image.open(BRAND / "nookscreen-mark.png") as source:
        mark = source.convert("RGBA")
    mark = mark.crop(mark.getbbox())
    save(adaptive_foreground(mark), RES / "drawable-xxxhdpi/ic_launcher_foreground.png")
    save(legacy_icon(mark, 256), BRAND / "icon.png")
    for density, size in DENSITIES.items():
        save(legacy_icon(mark, size), RES / f"mipmap-{density}/ic_launcher.png")
    for density, width in (("xhdpi", 320), ("xxhdpi", 480)):
        save(tv_banner(mark, width), RES / f"drawable-{density}/banner.png")


if __name__ == "__main__":
    main()
