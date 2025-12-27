#!/usr/bin/env python3
"""
Extract tile graphics from Zargon's tiles.wad file.

The WAD format structure:
- 4 bytes: Number of tile records (little-endian long)
- For each record:
  - 15 bytes: Tile name (ASCII, padded)
  - 4 bytes: File location pointer (little-endian long, 0-indexed)
- At each file location:
  - 2 bytes: Width (little-endian short)
  - 2 bytes: Height (little-endian short)
  - 2 bytes: Data length in bytes (little-endian short)
  - Data bytes: QBASIC GET/PUT format (16-bit integers, row-interleaved planes)

QBASIC SCREEN 9 = EGA 640x350, 16 colors (4 bit planes)
The game uses custom palette colors defined in ZARGON.BAS

Data format notes:
- QBASIC stores GET/PUT arrays as 16-bit integers
- First 2 integers (4 bytes) in data are width/height again (skip them)
- For each row, all 4 bit planes are stored sequentially
- Each plane's row is padded to integer (2-byte) boundary
"""

import struct
import os
from PIL import Image

# EGA palette from ZARGON.BAS displayTile/pall subroutine:
# PALETTE 0, 0: PALETTE 1, 4: PALETTE 2, 48: PALETTE 3, 2
# PALETTE 4, 6: PALETTE 5, 54: PALETTE 6, 10: PALETTE 7, 38
# PALETTE 8, 46: PALETTE 9, 5: PALETTE 10, 25: PALETTE 11, 7
# PALETTE 12, 57: PALETTE 13, 63: PALETTE 14, 9: PALETTE 15, 59

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

    Formula from https://moddingwiki.shikadi.net/wiki/EGA_Palette:
    red = 85 * (((ega >> 1) & 2) | (ega >> 5) & 1)
    green = 85 * ((ega & 2) | (ega >> 4) & 1)
    blue = 85 * (((ega << 1) & 2) | (ega >> 3) & 1)
    """
    r = 85 * (((value >> 1) & 2) | ((value >> 5) & 1))
    g = 85 * ((value & 2) | ((value >> 4) & 1))
    b = 85 * (((value << 1) & 2) | ((value >> 3) & 1))
    return (r, g, b)

# Build palette from ZARGON.BAS values
ZARGON_PALETTE_VALUES = [0, 4, 48, 2, 6, 54, 10, 38, 46, 5, 25, 7, 57, 63, 9, 59]
ZARGON_PALETTE = [ega_palette_to_rgb(v) for v in ZARGON_PALETTE_VALUES]

# Let's verify and print the palette
def print_palette():
    """Print the palette colors for debugging."""
    for i, (r, g, b) in enumerate(ZARGON_PALETTE):
        print(f"Color {i:2d}: palette value {ZARGON_PALETTE_VALUES[i]:2d} -> RGB({r:3d}, {g:3d}, {b:3d})")

def decode_ega_image(data, width, height):
    """
    Decode QBASIC GET/PUT EGA format image data.

    QBASIC SCREEN 9 uses 4 bit planes (EGA mode).
    The data is stored as 16-bit integers with:
    - First 2 integers (4 bytes): width and height (redundant, skip them)
    - For each row: all 4 bit planes stored sequentially
    - Each plane's row data is padded to 16-bit integer boundary

    Args:
        data: Raw byte data from WAD file
        width: Width in pixels
        height: Height in pixels

    Returns:
        PIL Image
    """
    # Parse data as 16-bit little-endian integers (QBASIC's internal format)
    int_array = []
    for i in range(0, len(data) - 1, 2):
        low = data[i]
        high = data[i + 1]
        int_array.append(low | (high << 8))

    bytes_per_row = (width + 7) // 8

    # Create image
    img = Image.new('RGBA', (width, height))
    pixels = img.load()

    # Skip header (first 2 integers are width and height embedded in data)
    data_idx = 2

    # Decode each scan line
    for y in range(height):
        # Read 4 bit planes for this row
        plane_data = [bytearray(bytes_per_row) for _ in range(4)]

        for plane in range(4):
            # Read bytes for this plane's scan line from integer array
            for byte_idx in range(bytes_per_row):
                if data_idx < len(int_array):
                    if byte_idx % 2 == 0:
                        # Low byte of current integer
                        plane_data[plane][byte_idx] = int_array[data_idx] & 0xFF
                    else:
                        # High byte of current integer
                        plane_data[plane][byte_idx] = (int_array[data_idx] >> 8) & 0xFF
                        data_idx += 1

            # If odd number of bytes per row, advance to next integer
            if bytes_per_row % 2 == 1:
                data_idx += 1

        # Convert planar data to pixels for this row
        for x in range(width):
            byte_idx = x // 8
            bit_idx = 7 - (x % 8)

            # Combine bits from all 4 planes to get color index
            color_index = 0
            for plane in range(4):
                if byte_idx < len(plane_data[plane]):
                    bit = (plane_data[plane][byte_idx] >> bit_idx) & 1
                    color_index |= (bit << plane)

            # Look up color in palette
            r, g, b = ZARGON_PALETTE[color_index % 16]
            pixels[x, y] = (r, g, b, 255)

    return img

def extract_tiles(wad_path, output_dir, scale=1):
    """
    Extract all tiles from a WAD file.

    Args:
        wad_path: Path to tiles.wad
        output_dir: Directory to save extracted PNGs
        scale: Scale factor for output images (default 1)
    """
    os.makedirs(output_dir, exist_ok=True)

    with open(wad_path, 'rb') as f:
        # Read number of records
        num_records = struct.unpack('<I', f.read(4))[0]
        print(f"Number of tile records: {num_records}")

        # Read all record headers
        tiles = []
        for i in range(num_records):
            name_bytes = f.read(15)
            name = name_bytes.decode('ascii', errors='ignore').strip()
            file_loc = struct.unpack('<I', f.read(4))[0]
            tiles.append((name, file_loc))
            print(f"  Tile {i}: '{name}' at offset {file_loc}")

        # Extract each tile
        for name, file_loc in tiles:
            f.seek(file_loc)

            # Read image header
            img_width = struct.unpack('<H', f.read(2))[0]
            img_height = struct.unpack('<H', f.read(2))[0]
            data_length = struct.unpack('<H', f.read(2))[0]

            print(f"\nExtracting '{name}': {img_width}x{img_height}, {data_length} bytes")

            # Read pixel data
            pixel_data = f.read(data_length)

            # Decode the image
            img = decode_ega_image(pixel_data, img_width, img_height)

            # Scale if requested
            if scale > 1:
                img = img.resize((img.width * scale, img.height * scale), Image.NEAREST)

            # Save as PNG
            safe_name = name.lower().replace('-', '_').replace(' ', '_')
            output_path = os.path.join(output_dir, f'{safe_name}.png')
            img.save(output_path, 'PNG')
            print(f"  Saved: {output_path}")

    print(f"\nExtracted {len(tiles)} tiles to {output_dir}")
    return tiles

def create_tile_sheet(wad_path, output_path, tiles_per_row=8, scale=1):
    """
    Create a sprite sheet containing all tiles.

    Args:
        wad_path: Path to tiles.wad
        output_path: Path for output sprite sheet
        tiles_per_row: Number of tiles per row in the sheet
        scale: Scale factor
    """
    with open(wad_path, 'rb') as f:
        num_records = struct.unpack('<I', f.read(4))[0]

        # Read headers
        tiles = []
        for _ in range(num_records):
            name = f.read(15).decode('ascii', errors='ignore').strip()
            file_loc = struct.unpack('<I', f.read(4))[0]
            tiles.append((name, file_loc))

        # Extract all tile images
        images = []
        max_width = 0
        max_height = 0

        for name, file_loc in tiles:
            f.seek(file_loc)
            img_width = struct.unpack('<H', f.read(2))[0]
            img_height = struct.unpack('<H', f.read(2))[0]
            data_length = struct.unpack('<H', f.read(2))[0]
            pixel_data = f.read(data_length)

            img = decode_ega_image(pixel_data, img_width, img_height)
            if scale > 1:
                img = img.resize((img.width * scale, img.height * scale), Image.NEAREST)

            images.append((name, img))
            max_width = max(max_width, img.width)
            max_height = max(max_height, img.height)

        # Create sprite sheet
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
        print(f"Created tile sheet: {output_path} ({sheet_width}x{sheet_height})")

def main():
    import argparse

    parser = argparse.ArgumentParser(description='Extract tiles from Zargon WAD file')
    parser.add_argument('wad_file', help='Path to tiles.wad')
    parser.add_argument('-o', '--output', default='extracted_tiles', help='Output directory')
    parser.add_argument('-s', '--scale', type=int, default=1, help='Scale factor (default: 1)')
    parser.add_argument('--sheet', action='store_true', help='Also create a tile sheet')
    parser.add_argument('--palette', action='store_true', help='Print palette colors')

    args = parser.parse_args()

    if args.palette:
        print("Zargon color palette:")
        print_palette()
        print()

    tiles = extract_tiles(args.wad_file, args.output, args.scale)

    if args.sheet:
        sheet_path = os.path.join(args.output, 'tile_sheet.png')
        create_tile_sheet(args.wad_file, sheet_path, scale=args.scale)

if __name__ == '__main__':
    main()
