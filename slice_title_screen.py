#!/usr/bin/env python3
"""
Slice the title screen mockup into individual image assets for the Zargon Android app.
"""

from PIL import Image
import os

def slice_title_screen(input_path, output_dir):
    """
    Slice the title screen image into component assets.

    Args:
        input_path: Path to the full title screen image
        output_dir: Directory to save the sliced assets
    """
    # Create output directory if it doesn't exist
    os.makedirs(output_dir, exist_ok=True)

    # Load the image
    img = Image.open(input_path)
    width, height = img.size
    print(f"Image size: {width}x{height}")

    # Define regions for each asset (x, y, x2, y2)
    # These coordinates are approximate and may need fine-tuning
    regions = {
        # Wall torches (left and right)
        'torch_left': (20, 150, 120, 400),
        'torch_right': (width-120, 150, width-20, 400),

        # Demon head at top
        'demon_head': (width//2 - 100, 10, width//2 + 100, 120),

        # Title frame/banner (the ornate frame around the title)
        'title_frame': (120, 80, width-120, 280),

        # Parchment scroll banner ("Select Save Slot:")
        'parchment_banner': (200, 290, width-200, 380),

        # Single slot frame (we'll extract one and reuse it)
        'slot_frame': (150, 390, width-150, 530),

        # Bottom decorations
        'candle_left': (20, 980, 120, height-20),
        'candle_right': (width-120, 1080, width-20, height-20),

        # Coin pile with bowl (bottom left)
        'coin_pile': (0, height-250, 150, height),

        # Chains and shackles (right side)
        'chains': (width-180, 950, width, height-100),

        # Sword (bottom right)
        'sword': (width-200, height-180, width-20, height),

        # Stone pillar textures
        'pillar_left': (0, 200, 120, 600),
        'pillar_right': (width-120, 200, width, 600),

        # Stone wall background sample
        'stone_wall_bg': (width//2 - 100, 700, width//2 + 100, 900),
    }

    # Extract and save each region
    for name, (x1, y1, x2, y2) in regions.items():
        try:
            cropped = img.crop((x1, y1, x2, y2))
            output_path = os.path.join(output_dir, f'{name}.png')
            cropped.save(output_path, 'PNG')
            print(f"✓ Saved {name}.png ({x2-x1}x{y2-y1})")
        except Exception as e:
            print(f"✗ Failed to save {name}: {e}")

    print(f"\n✓ All assets saved to {output_dir}")
    print("\nNote: You may need to manually adjust and clean up some assets:")
    print("  - Remove backgrounds from transparent elements")
    print("  - Fine-tune the torch flames for animation")
    print("  - Adjust the demon head if edges are cut off")

if __name__ == '__main__':
    import sys

    if len(sys.argv) < 2:
        print("Usage: python slice_title_screen.py <input_image> [output_dir]")
        print("\nExample:")
        print("  python slice_title_screen.py title_screen_mockup.png assets/")
        sys.exit(1)

    input_image = sys.argv[1]
    output_directory = sys.argv[2] if len(sys.argv) > 2 else 'sliced_assets'

    if not os.path.exists(input_image):
        print(f"Error: Input image '{input_image}' not found")
        sys.exit(1)

    slice_title_screen(input_image, output_directory)
