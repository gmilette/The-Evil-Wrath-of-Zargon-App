#!/usr/bin/env python3
"""
Extract sprites from Zargon's bomb.sht text-based sprite sheet format.

Format:
- Line 1: Width
- Line 2: Height
- Line 3: Sprite name in quotes (e.g., "flor")
- Following lines: Space-separated color indices (one row per line)
- Pattern repeats for each sprite
"""

import os
import re
from PIL import Image

# Zargon palette from ZARGON.BAS (same as used in tiles.wad)
def ega_palette_to_rgb(value):
    """
    Convert EGA 6-bit palette value to RGB.

    EGA bit order is rgbRGB (from ModdingWiki):
    - bit 5 = r (red low intensity 1/3)
    - bit 4 = g (green low intensity 1/3)
    - bit 3 = b (blue low intensity 1/3)
    - bit 2 = R (red high intensity 2/3)
    - bit 1 = G (green high intensity 2/3)
    - bit 0 = B (blue high intensity 2/3)

    Formula from https://moddingwiki.shikadi.net/wiki/EGA_Palette
    """
    r = 85 * (((value >> 1) & 2) | ((value >> 5) & 1))
    g = 85 * ((value & 2) | ((value >> 4) & 1))
    b = 85 * (((value << 1) & 2) | ((value >> 3) & 1))
    return (r, g, b)

ZARGON_PALETTE_VALUES = [0, 4, 48, 2, 6, 54, 10, 38, 46, 5, 25, 7, 57, 63, 9, 59]
ZARGON_PALETTE = [ega_palette_to_rgb(v) for v in ZARGON_PALETTE_VALUES]

def extract_sht_sprites(sht_path, output_dir, scale=1):
    """
    Extract all sprites from a .sht file.

    Args:
        sht_path: Path to .sht file
        output_dir: Directory to save extracted PNGs
        scale: Scale factor for output images
    """
    os.makedirs(output_dir, exist_ok=True)

    with open(sht_path, 'r') as f:
        content = f.read()

    # Split into lines
    lines = content.strip().split('\n')

    sprites = []
    i = 0

    while i < len(lines):
        # Try to parse a sprite
        try:
            # Read width
            width = int(lines[i].strip())
            i += 1

            # Read height
            height = int(lines[i].strip())
            i += 1

            # Read name (in quotes)
            name_line = lines[i].strip()
            name_match = re.match(r'"([^"]+)"', name_line)
            if not name_match:
                print(f"Warning: Expected sprite name at line {i+1}, got: {name_line}")
                i += 1
                continue
            name = name_match.group(1)
            i += 1

            # Read pixel data
            pixels = []
            for y in range(height):
                if i >= len(lines):
                    break
                row_line = lines[i].strip()
                i += 1

                # Parse space-separated color indices
                row = []
                for val in row_line.split():
                    try:
                        row.append(int(val))
                    except ValueError:
                        row.append(0)

                # Pad row if needed
                while len(row) < width:
                    row.append(0)
                row = row[:width]  # Trim if too long
                pixels.append(row)

            # Pad if not enough rows
            while len(pixels) < height:
                pixels.append([0] * width)

            sprites.append((name, width, height, pixels))
            print(f"Found sprite: '{name}' ({width}x{height})")

        except ValueError as e:
            # Not a valid sprite header, skip line
            i += 1
            continue

    print(f"\nExtracted {len(sprites)} sprites")

    # Save each sprite as PNG
    for name, width, height, pixels in sprites:
        img = Image.new('RGBA', (width, height))
        img_pixels = img.load()

        for y in range(height):
            for x in range(width):
                color_idx = pixels[y][x] % 16
                r, g, b = ZARGON_PALETTE[color_idx]

                # Color 0 (black) could be transparent background
                # For most sprites, black (0) is background
                alpha = 255 if color_idx != 0 else 0

                img_pixels[x, y] = (r, g, b, alpha)

        # Scale if requested
        if scale > 1:
            img = img.resize((width * scale, height * scale), Image.NEAREST)

        # Save
        safe_name = name.lower().replace('-', '_').replace(' ', '_')
        output_path = os.path.join(output_dir, f'{safe_name}.png')
        img.save(output_path, 'PNG')
        print(f"  Saved: {output_path}")

    return sprites

def create_sprite_sheet(sprites, output_path, tiles_per_row=8, scale=1):
    """Create a sprite sheet from extracted sprites."""
    if not sprites:
        return

    # Process sprites into images
    images = []
    max_width = 0
    max_height = 0

    for name, width, height, pixels in sprites:
        img = Image.new('RGBA', (width, height))
        img_pixels = img.load()

        for y in range(height):
            for x in range(width):
                color_idx = pixels[y][x] % 16
                r, g, b = ZARGON_PALETTE[color_idx]
                alpha = 255 if color_idx != 0 else 0
                img_pixels[x, y] = (r, g, b, alpha)

        if scale > 1:
            img = img.resize((width * scale, height * scale), Image.NEAREST)

        images.append((name, img))
        max_width = max(max_width, img.width)
        max_height = max(max_height, img.height)

    # Create sheet
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

    sheet.save(output_path, 'PNG')
    print(f"Created sprite sheet: {output_path} ({sheet_width}x{sheet_height})")

def main():
    import argparse

    parser = argparse.ArgumentParser(description='Extract sprites from Zargon .sht file')
    parser.add_argument('sht_file', help='Path to .sht file')
    parser.add_argument('-o', '--output', default='extracted_sprites', help='Output directory')
    parser.add_argument('-s', '--scale', type=int, default=1, help='Scale factor (default: 1)')
    parser.add_argument('--sheet', action='store_true', help='Also create a sprite sheet')
    parser.add_argument('--opaque', action='store_true', help='Keep black pixels opaque (no transparency)')

    args = parser.parse_args()

    sprites = extract_sht_sprites(args.sht_file, args.output, args.scale)

    if args.sheet and sprites:
        sheet_path = os.path.join(args.output, 'sprite_sheet.png')
        create_sprite_sheet(sprites, sheet_path, scale=args.scale)

if __name__ == '__main__':
    main()
