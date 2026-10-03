#!/usr/bin/env python3
"""Draw placeholder 16x16 side textures for the pine bark stages, from the existing ``pine_log.png``.

``peeled_pine_log.png`` is the bark's luminance remapped onto a red-orange ramp, with a few lighter vertical flake
streaks, so it keeps the bark's grain but reads as the thin red bark high on a Scots pine. ``peeling_pine_log.png``
is the transition: the original bark in the bottom rows and the peeled texture in the top rows, split along a jagged
line around row 8. Row 0 is the top of a vertical log, so the bark sits below and the red above, matching the logs
below and above it. The output is deterministic, so rerunning only changes the files if this script or
``pine_log.png`` changes.

Usage: python tools/gen_pine_bark_placeholder.py [texture_dir]
Defaults to src/main/resources/assets/nordic/textures/block, which is read from and written to.
"""
import os
import sys

from PIL import Image

SIZE = 16
PEELED_DARK = (110, 42, 24)
PEELED_LIGHT = (210, 110, 62)
FLAKE = (226, 140, 92)

# Flake streaks as (column, first row, last row)
FLAKES = [(2, 1, 5), (6, 9, 13), (11, 3, 6), (13, 10, 14)]

# Per-column shift of the bark/peeled cut from row 8, fixed so the seam looks torn rather than straight
SEAM = 8
SEAM_SHIFT = [0, 1, 3, 2, -1, -2, 0, 2, 1, -1, -3, -2, 0, 1, -1, 2]


def luminance(pixel):
    r, g, b = pixel[:3]
    return 0.299 * r + 0.587 * g + 0.114 * b


def lerp(a, b, t):
    return tuple(round(x + (y - x) * t) for x, y in zip(a, b))


def peeled(bark):
    """The bark's light and dark grain stretched over the red-orange ramp, plus the flake streaks."""
    values = [luminance(bark.getpixel((x, y))) for y in range(SIZE) for x in range(SIZE)]
    low, high = min(values), max(values)
    span = max(high - low, 1)
    img = Image.new("RGBA", (SIZE, SIZE))
    for y in range(SIZE):
        for x in range(SIZE):
            pixel = bark.getpixel((x, y))
            t = (luminance(pixel) - low) / span
            img.putpixel((x, y), lerp(PEELED_DARK, PEELED_LIGHT, t) + (pixel[3],))
    for x, y_from, y_to in FLAKES:
        for y in range(y_from, y_to + 1):
            img.putpixel((x, y), FLAKE + (255,))
    return img


def peeling(bark, red):
    """Red above the jagged seam, bark from the seam down."""
    img = bark.copy()
    for x in range(SIZE):
        cut = SEAM + SEAM_SHIFT[x]
        for y in range(cut):
            img.putpixel((x, y), red.getpixel((x, y)))
    return img


def main():
    out = sys.argv[1] if len(sys.argv) > 1 else os.path.join("src", "main", "resources", "assets", "nordic",
                                                              "textures", "block")
    bark = Image.open(os.path.join(out, "pine_log.png")).convert("RGBA")
    red = peeled(bark)
    red.save(os.path.join(out, "peeled_pine_log.png"))
    peeling(bark, red).save(os.path.join(out, "peeling_pine_log.png"))


if __name__ == "__main__":
    main()
