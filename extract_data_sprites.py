#!/usr/bin/env python3
"""
Extract monster sprites from ZARGON.BAS DATA statements.

These sprites are defined inline in the QBASIC code as DATA statements
and read by the readjunk subroutine.
"""

import os
import re
from PIL import Image

# Zargon palette
def ega_palette_to_rgb(value):
    """Convert EGA 6-bit palette value to RGB."""
    r_low = (value >> 2) & 1
    g_low = (value >> 1) & 1
    b_low = value & 1
    r_hi = (value >> 5) & 1
    g_hi = (value >> 4) & 1
    b_hi = (value >> 3) & 1
    r = (r_hi * 2 + r_low) * 85
    g = (g_hi * 2 + g_low) * 85
    b = (b_hi * 2 + b_low) * 85
    return (r, g, b)

ZARGON_PALETTE_VALUES = [0, 4, 48, 2, 6, 54, 10, 38, 46, 5, 25, 7, 57, 63, 9, 59]
ZARGON_PALETTE = [ega_palette_to_rgb(v) for v in ZARGON_PALETTE_VALUES]

# Sprite definitions from readjunk subroutine in ZARGON.BAS
# Format: (name, width, height, start_line)
SPRITES = [
    ("joe", 18, 30, 262),        # Player character (biggy joe)
    ("bat", 17, 15, 294),        # Bat monster (drake)
    ("slime", 17, 15, 316),      # Slime monster
    ("ghost", 27, 26, 333),      # Ghost monster
    ("joeattax", 21, 30, 361),   # Player attacking
    ("flame", 80, 4, 393),       # Flame effect
    ("beleth", 39, 32, 400),     # Beleth demon
    ("babble", 27, 10, 434),     # Babble monster (slime2)
    ("spook", 25, 26, 446),      # Spook ghost
]

def parse_data_line(line):
    """Parse a DATA line and return list of values."""
    # Remove "DATA " prefix if present
    if line.upper().startswith('DATA'):
        line = line[4:].strip()

    values = []
    for val in line.split(','):
        val = val.strip()
        if val.lower() == 'x':
            values.append(None)  # Transparent
        else:
            try:
                values.append(int(val))
            except ValueError:
                values.append(None)
    return values

def extract_sprite_from_bas(bas_path, sprite_name, width, height, start_line):
    """Extract a sprite from QBASIC DATA statements."""
    with open(bas_path, 'r') as f:
        lines = f.readlines()

    pixels = []
    current_line = start_line - 1  # 0-indexed

    # Read enough data for width x height pixels
    total_pixels_needed = width * height
    all_values = []

    while len(all_values) < total_pixels_needed and current_line < len(lines):
        line = lines[current_line].strip()
        if line.upper().startswith('DATA'):
            values = parse_data_line(line)
            all_values.extend(values)
        elif line and not line.startswith("'"):
            # Non-DATA non-comment line, probably end of sprite
            if all_values:  # Only break if we've started reading data
                break
        current_line += 1

    # Convert to 2D array
    for y in range(height):
        row = []
        for x in range(width):
            idx = y * width + x
            if idx < len(all_values):
                row.append(all_values[idx])
            else:
                row.append(None)
        pixels.append(row)

    return pixels

def save_sprite(pixels, output_path, scale=1):
    """Save sprite pixels as PNG."""
    height = len(pixels)
    width = len(pixels[0]) if pixels else 0

    img = Image.new('RGBA', (width, height))
    img_pixels = img.load()

    for y in range(height):
        for x in range(width):
            val = pixels[y][x]
            if val is None:
                # Transparent
                img_pixels[x, y] = (0, 0, 0, 0)
            else:
                color_idx = val % 16
                r, g, b = ZARGON_PALETTE[color_idx]
                img_pixels[x, y] = (r, g, b, 255)

    if scale > 1:
        img = img.resize((width * scale, height * scale), Image.NEAREST)

    img.save(output_path, 'PNG')
    return img

def main():
    import argparse

    parser = argparse.ArgumentParser(description='Extract sprites from ZARGON.BAS DATA statements')
    parser.add_argument('bas_file', help='Path to ZARGON.BAS')
    parser.add_argument('-o', '--output', default='extracted_monsters', help='Output directory')
    parser.add_argument('-s', '--scale', type=int, default=1, help='Scale factor')
    parser.add_argument('--sheet', action='store_true', help='Create sprite sheet')

    args = parser.parse_args()

    os.makedirs(args.output, exist_ok=True)

    images = []
    for name, width, height, start_line in SPRITES:
        print(f"Extracting '{name}' ({width}x{height}) from line {start_line}...")
        pixels = extract_sprite_from_bas(args.bas_file, name, width, height, start_line)

        output_path = os.path.join(args.output, f'{name}.png')
        img = save_sprite(pixels, output_path, args.scale)
        images.append((name, img))
        print(f"  Saved: {output_path}")

    if args.sheet:
        # Create sprite sheet
        max_width = max(img.width for _, img in images)
        max_height = max(img.height for _, img in images)
        tiles_per_row = 4

        num_rows = (len(images) + tiles_per_row - 1) // tiles_per_row
        sheet_width = tiles_per_row * (max_width + 2) + 2
        sheet_height = num_rows * (max_height + 2) + 2

        sheet = Image.new('RGBA', (sheet_width, sheet_height), (64, 64, 64, 255))

        for i, (name, img) in enumerate(images):
            row = i // tiles_per_row
            col = i % tiles_per_row
            x = col * (max_width + 2) + 2
            y = row * (max_height + 2) + 2
            sheet.paste(img, (x, y))

        sheet_path = os.path.join(args.output, 'monster_sheet.png')
        sheet.save(sheet_path, 'PNG')
        print(f"\nCreated sprite sheet: {sheet_path}")

    print(f"\nExtracted {len(SPRITES)} sprites to {args.output}")

if __name__ == '__main__':
    main()
