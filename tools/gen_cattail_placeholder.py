#!/usr/bin/env python3
"""Draw placeholder 16x16 cattail textures (``cattail_bottom.png`` and ``cattail_top.png``) for the cross models.

The bottom half has green stalks and leaf blades. The top half continues the stalks, with a brown seed head about
5 px tall on the main stalk and a thin spike above it. The output is deterministic, so rerunning only changes the
files if this script changes.

Usage: python tools/gen_cattail_placeholder.py [output_dir]
Defaults to src/main/resources/assets/nordic/textures/block.
"""
import os
import sys

from PIL import Image

SIZE = 16
STALK = (88, 128, 58, 255)
STALK_DARK = (66, 100, 44, 255)
LEAF = (106, 150, 70, 255)
LEAF_DARK = (80, 118, 52, 255)
HEAD = (104, 64, 36, 255)
HEAD_LIGHT = (128, 82, 46, 255)
HEAD_DARK = (76, 46, 26, 255)
SPIKE = (164, 152, 112, 255)

# Stalk columns, shared by both halves so they line up at the seam
MAIN_STALK = 7
SIDE_STALK = 10


def column(img, x, y_from, y_to, colour):
    for y in range(y_from, y_to + 1):
        img.putpixel((x, y), colour)


def blade(img, points, colour, dark):
    """A leaf blade through the given (x, y) points, with a darker edge pixel beside each."""
    for x, y in points:
        img.putpixel((x, y), colour)
        if 0 <= x + 1 < SIZE and img.getpixel((x + 1, y))[3] == 0:
            img.putpixel((x + 1, y), dark)


def bottom():
    img = Image.new("RGBA", (SIZE, SIZE), (0, 0, 0, 0))
    column(img, MAIN_STALK, 0, 15, STALK)
    column(img, SIDE_STALK, 0, 15, STALK_DARK)
    # Leaf blades leaning out from the base
    blade(img, [(4, 15), (4, 14), (3, 13), (3, 12), (3, 11), (2, 10), (2, 9), (2, 8), (1, 7), (1, 6)], LEAF, LEAF_DARK)
    blade(img, [(5, 15), (5, 14), (5, 13), (5, 12), (4, 11), (4, 10), (4, 9), (4, 8), (4, 7), (4, 6), (3, 5),
                (3, 4), (3, 3), (3, 2), (3, 1), (3, 0)], LEAF, LEAF_DARK)
    blade(img, [(12, 15), (12, 14), (12, 13), (13, 12), (13, 11), (13, 10), (13, 9), (14, 8), (14, 7), (14, 6)],
          LEAF, LEAF_DARK)
    blade(img, [(9, 15), (9, 14), (9, 13), (9, 12), (9, 11), (8, 10), (8, 9), (8, 8), (8, 7), (8, 6), (8, 5),
                (8, 4)], LEAF_DARK, STALK_DARK)
    return img


def top():
    img = Image.new("RGBA", (SIZE, SIZE), (0, 0, 0, 0))
    # Stalk tips
    column(img, MAIN_STALK, 9, 15, STALK)
    column(img, SIDE_STALK, 11, 15, STALK_DARK)
    img.putpixel((SIDE_STALK, 10), LEAF)
    # Leaf tip continuing the long blade from the bottom half
    blade(img, [(3, 15), (3, 14), (3, 13), (2, 12), (2, 11), (2, 10), (2, 9)], LEAF, LEAF_DARK)
    # Seed head, 2 px wide and 5 px tall, shaded left to right
    for y in range(4, 9):
        img.putpixel((MAIN_STALK, y), HEAD_LIGHT if y < 7 else HEAD)
        img.putpixel((MAIN_STALK + 1, y), HEAD if y < 7 else HEAD_DARK)
    img.putpixel((MAIN_STALK, 4), HEAD)
    img.putpixel((MAIN_STALK + 1, 8), HEAD_DARK)
    # Thin spike above the head
    column(img, MAIN_STALK, 0, 3, SPIKE)
    return img


def main():
    out = sys.argv[1] if len(sys.argv) > 1 else os.path.join("src", "main", "resources", "assets", "nordic",
                                                              "textures", "block")
    os.makedirs(out, exist_ok=True)
    bottom().save(os.path.join(out, "cattail_bottom.png"))
    top().save(os.path.join(out, "cattail_top.png"))


if __name__ == "__main__":
    main()
