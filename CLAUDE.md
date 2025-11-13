# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

"The Evil Wrath of Zargon" is a legacy QBASIC game originally developed by high-school students in 1998-1999 (Snappahed Software 98). This is a nostalgia project that preserves the original codebase with minimal modifications. The game is a tile-based RPG adventure set in the land of GEF.

## Architecture

### Core Components

- **ZARGON.BAS** (`zargon/ZARGON.BAS`): The main game file (~3000+ lines of QBASIC code)
  - Contains all game logic, graphics, battle system, and story progression
  - Uses COMMON SHARED variables extensively for state management
  - Implements a tile-based rendering system with custom WAD file format for graphics
  - Notable systems:
    - Battle system (procedures: `battleset`, `dbattleset`, `Hitback`, `HitBeast`, `WinBattle`)
    - Character stats and leveling (`charstatz`, `CheckLevel`, `Magix`)
    - Map/world navigation (tile-based with `ground()` and `donttouch()` arrays)
    - Story progression (`storycheck`, `storystatus` variable)
    - Save/load system (`savgam`, `opengam`)

### File Structure

```
zargon/
  ZARGON.BAS       - Main game code
  zargon.bat       - Launcher batch file
  *.lvl            - Level/map files (mapXY.lvl format, where X=world, Y=quadrant)
  tiles.wad        - Graphics data file
  bomb.sht         - Sheet graphics data
  ghost.bmp        - Ghost sprite
  gef.ini          - Game configuration
```

### Data Files

- **Map Files**: `mapXY.lvl` format where X is the world number (1-4) and Y is the quadrant (1-4)
- **Graphics**: Custom WAD format (`tiles.wad`) containing tile graphics loaded via `displayTile` procedure
- **Save Files**: Binary format managed by `savgam`/`opengam` procedures

## Development Workflow

### Prerequisites

- DOSBox emulator
- QBASIC (from Microsoft's olddos.exe - not included due to licensing)
- For distribution builds: Perl with Text::Markdown module

When editing ZARGON.BAS:
- The code uses QBASIC syntax (not QuickBASIC 4.5 - the compiler chokes on it)
- Heavy use of DECLARE SUB/FUNCTION at the top of the file
- Many duplicate DECLARE statements (intentional, part of original code)
- Uses COMMON SHARED for nearly all global state
- Graphics stored in integer arrays loaded via GET/PUT operations
- Coordinate system: `mapx`, `mapy` for world position; `cx`, `cy` for character position within map

### Known Technical Debt

- "Spaghetti code" (acknowledged by original developers in comments)
- Stack overflow issues
- UI corruption issues that cause "Redo from start" prompts
- No error handling in many procedures
- Global state management via COMMON SHARED
- Won't compile with QB 4.5 compiler (only works in QBASIC interpreter)
