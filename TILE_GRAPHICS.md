# Zargon Tile Graphics Documentation

This document describes the tile graphics extracted from the original QBASIC game.

## Source Files

### tiles.wad (Binary WAD Format)
Contains 14 tiles stored in QBASIC GET/PUT planar EGA format.

**Format Structure:**
- 4 bytes: Number of records (little-endian long)
- For each record:
  - 15 bytes: Tile name (ASCII, space-padded)
  - 4 bytes: File offset (little-endian long)
- At each offset:
  - 2 bytes: Width (little-endian short)
  - 2 bytes: Height (little-endian short)
  - 2 bytes: Data length (little-endian short)
  - N bytes: EGA planar bitmap data (4 bit planes)

**Tiles in tiles.wad:**
| Index | Name | Size | Description |
|-------|------|------|-------------|
| 0 | Dude-Back1 | 30x30 | Player character facing away (frame 1) |
| 1 | Dude-Back2 | 30x30 | Player character facing away (frame 2) |
| 2 | Dude-Front1 | 30x30 | Player character facing forward (frame 1) |
| 3 | Dude-Front2 | 30x30 | Player character facing forward (frame 2) |
| 4 | Dude-SideL | 30x30 | Player character facing left |
| 5 | Dude-SideR | 30x30 | Player character facing right |
| 6 | Grass | 30x30 | Grass terrain tile |
| 7 | Rock-1 | 30x30 | Mountain/rock obstacle |
| 8 | Rock-2 | 30x30 | Rubble/small rocks |
| 9 | Sand | 30x30 | Sand terrain tile |
| 10 | Trees1 | 30x30 | Tree obstacle (type 1) |
| 11 | Trees2 | 30x30 | Tree obstacle (type 2) |
| 12 | Water | 30x30 | Water tile (not walkable) |
| 13 | Gravestone | 30x30 | Gravestone obstacle |

### bomb.sht (ASCII Text Format)
Contains 17 sprites stored in a text-based format with color indices.

**Format Structure:**
```
width
height
"sprite_name"
[height rows of space-separated color indices]
```

**Sprites in bomb.sht:**
| Name | Size | Description |
|------|------|-------------|
| flor | 30x30 | Floor tile (indoor) |
| rock1 | 30x30 | Mountain rock (same as tiles.wad Rock-1) |
| rock2 | 30x30 | Rubble (same as tiles.wad Rock-2) |
| tree2 | 30x30 | Tree type 2 |
| tree1 | 30x30 | Tree type 1 |
| water | 30x30 | Water tile |
| florwd | 30x30 | Floor with door/marking |
| fboat | 30x30 | Boat sprite |
| cast | 30x30 | Castle tile |
| huts | 30x30 | Hut/building tile |
| cflor | 30x30 | Cave floor tile |
| oldman | 29x29 | Old man NPC (boatmaker) |
| ZARGON | 57x50 | Final boss Zargon |
| necro | 32x34 | Necromancer monster |
| kraken | 56x56 | Kraken water monster |
| snake | 42x54 | Skander Snake monster |
| demon | 30x22 | Demon sprite |

### DATA Statements in ZARGON.BAS
Monster sprites defined inline in the QBASIC code:

| Name | Size | Lines | Description |
|------|------|-------|-------------|
| joe | 18x30 | 262-291 | "Biggy Joe" player sprite |
| bat | 17x15 | 294-308 | Bat monster (called "drake" in code) |
| slime | 17x15 | 316-330 | Slime monster |
| ghost | 27x26 | 333-358 | Ghost monster |
| joeattax | 21x30 | 361-390 | Player attacking animation |
| flame | 80x4 | 393-396 | Flame effect (horizontal strip) |
| beleth | 39x32 | 400-431 | Beleth demon monster |
| babble | 27x10 | 434-444 | Babble monster (slime variant) |
| spook | 25x26 | 446+ | Spook ghost monster |

## Color Palette

The game uses EGA SCREEN 9 mode (16 colors) with a custom palette:

| Index | Palette Value | RGB | Description |
|-------|---------------|-----|-------------|
| 0 | 0 | (0, 0, 0) | Black |
| 1 | 4 | (85, 0, 0) | Dark Red |
| 2 | 48 | (170, 170, 0) | Yellow/Olive |
| 3 | 2 | (0, 85, 0) | Dark Green |
| 4 | 6 | (85, 85, 0) | Brown |
| 5 | 54 | (255, 255, 0) | Bright Yellow |
| 6 | 10 | (0, 85, 170) | Blue/Cyan |
| 7 | 38 | (255, 85, 0) | Orange |
| 8 | 46 | (255, 85, 170) | Pink |
| 9 | 5 | (85, 0, 85) | Dark Magenta |
| 10 | 25 | (0, 170, 255) | Light Blue |
| 11 | 7 | (85, 85, 85) | Gray |
| 12 | 57 | (170, 170, 255) | Light Purple |
| 13 | 63 | (255, 255, 255) | White |
| 14 | 9 | (0, 0, 255) | Blue |
| 15 | 59 | (170, 255, 255) | Cyan |

## Map Tile Codes

From ZARGON.BAS crossroad procedure (lines 914-959):

| Code | Tile Type | Walkable | Description |
|------|-----------|----------|-------------|
| 1 | Grass | Yes | Grassy terrain |
| 2 | Sand | Yes | Sandy terrain |
| 0 | Floor | Yes | Indoor floor |
| D | Floor Decorated | Yes | Indoor floor with decoration |
| 4 | Shallow Water | No | Water (walkable with boat) |
| w | Water | No | Deep water |
| T | Trees | No | Tree obstacle |
| t | Trees2 | No | Tree obstacle (variant) |
| R | Rock | No | Mountain/rock |
| r | Rock2 | No | Rubble |
| G | Gravestone | No | Gravestone |
| h | Hut | Yes | NPC building |
| H | Healer | Yes | Healer location |
| W | Weapon Shop | Yes | Weapon shop |
| C | Castle | Yes | Castle entrance |

## Extraction Tools

Three Python scripts are provided for extracting graphics:

### extract_tiles.py
Extracts tiles from tiles.wad binary format.
```bash
python3 extract_tiles.py zargon/tiles.wad -o extracted_tiles --sheet
```

### extract_sheets.py
Extracts sprites from bomb.sht text format.
```bash
python3 extract_sheets.py zargon/bomb.sht -o extracted_sprites --sheet
```

### extract_data_sprites.py
Extracts monster sprites from ZARGON.BAS DATA statements.
```bash
python3 extract_data_sprites.py zargon/ZARGON.BAS -o extracted_monsters --sheet
```

## Android Integration

Extracted PNG files are placed in:
`app/app/src/main/res/drawable-nodpi/`

The Android app also includes:
- `TileParser.kt` - Parses tiles.wad at runtime
- `SpriteParser.kt` - Parses bomb.sht at runtime
- `EGAPalette.kt` - Color palette definitions

For new sprites, either:
1. Add source file to `assets/` and use parser
2. Use pre-extracted PNG from `drawable-nodpi/`
