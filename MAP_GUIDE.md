# The Evil Wrath of Zargon - Visual Map Guide

This guide shows ASCII visualizations of key game maps with item locations marked.

## Legend

### Terrain Tiles
- `T` / `t` = Trees (not walkable)
- `R` / `r` = Rocks (not walkable)
- `w` = Deep Water (not walkable)
- `a` = Shallow Water (not walkable)
- `1` = Grass (walkable, encounters)
- `2` = Sand (walkable, encounters)
- `0` = Floor (walkable, fewer encounters)
- `D` = Decorated Floor (walkable)
- `G` = Grave (not walkable)

### Special Locations
- `h` = Hut (NPC location)
- `H` = Healer
- `W` = Weapon Shop
- `C` = Castle

### Items
- `*` = Item spawn location (overlaid on terrain)

---

## Complete World Map Overview

The game world consists of 16 maps (4×4 grid) forming an 80×40 tile landscape. Below is a simplified overview showing major terrain types and key locations.

### World Layout (4×4 Map Grid)

```
    MAP 11          MAP 21          MAP 31          MAP 41
  (Northwest)     (North-Mid)    (Northeast)   (Necromancer)
 ┌─────────────┬─────────────┬─────────────┬─────────────┐
 │ Castle (C)  │  Forest &   │   Water &   │ Graveyard & │
 │  Fountain   │   Grass     │   River     │  Necromancer│
 │  (Healing)  │  (Monsters) │  (Shallow)  │   (Hut-h)   │
 └─────────────┼─────────────┼─────────────┼─────────────┤
    MAP 12          MAP 22          MAP 32          MAP 42
   (West-Mid)     (Forest-Mid)   (River-Mid)      (East-Mid)
 ┌─────────────┼─────────────┼─────────────┼─────────────┐
 │ Mixed       │ **WOOD**    │  Fountain & │   Rocky     │
 │ Forest &    │  (10, 4)    │   River     │  Mountains  │
 │  Grass      │   Trees     │  (Healing)  │  (Monsters) │
 └─────────────┼─────────────┼─────────────┼─────────────┤
    MAP 13          MAP 23          MAP 33          MAP 43
   (Southwest)    (Forest-South)  (River-South)   (Mountain)
 ┌─────────────┼─────────────┼─────────────┼─────────────┐
 │ **CLOTH**   │   Dense     │    River    │ Mtn. Jack   │
 │  (7, 6)     │   Forest    │   & Water   │   (Hut-h)   │
 │  Trees      │  (Monsters) │  (Shallow)  │ (Story 4.0+)│
 └─────────────┼─────────────┼─────────────┼─────────────┤
    MAP 14          MAP 24          MAP 34          MAP 44
  (Sandman)       (START)        (River-South)    (Dynamite)
 ┌─────────────┼─────────────┼─────────────┼─────────────┐
 │ **DEADWOOD**│ **RUTTER**  │    River    │ **DYNAMITE**│
 │  (3, 8)     │  (9, 1)     │   & Water   │  (14, 6)    │
 │ Sandman-h   │ Boatman-h   │  (Shallow)  │ Old Man-h   │
 │  Desert     │ Healer-H    │             │   Rocks     │
 │             │ Weapon-W    │             │             │
 └─────────────┴─────────────┴─────────────┴─────────────┘

  Y-Axis: Row 1 (top) → Row 4 (bottom)
  X-Axis: Column 1 (left) → Column 4 (right)
```

### Terrain Distribution

**Northwest Quadrant (Maps 11-14):**
- Mix of forest, grass, sand, and castle
- **Map 11**: Castle with fountain (healing/save point)
- **Map 12**: Mixed forest and grass terrain
- **Map 13**: Dense forest with **Cloth** item
- **Map 14**: Sandy desert with **Dead Wood**, Sandman's hut

**North-Mid Quadrant (Maps 21-24):**
- Heavy forest coverage with river
- **Map 21**: Forest and grass, monster encounters
- **Map 22**: Forest with **Wood** item for boat
- **Map 23**: Dense forest, high encounter rate
- **Map 24**: **STARTING AREA** - Boatman (trapped), Healer, Weapon Shop, **Rutter**

**Northeast Quadrant (Maps 31-34):**
- River system running north-south
- **Map 31**: Shallow water and river areas
- **Map 32**: Fountain (healing/save point) and river
- **Map 33**: River and water passages
- **Map 34**: Southern river, shallow water areas

**East Quadrant (Maps 41-44):**
- Rocky mountains and graveyard
- **Map 41**: Graveyard with **Necromancer** (story 4.0+)
- **Map 42**: Rocky mountains, monster territory
- **Map 43**: Mountains with **Mountain Jack** (story 4.0+)
- **Map 44**: Rocky area with **Dynamite** and Old Man

### Critical Path Through World

1. **Start**: Map 24 (2, 4) - Between Healer and Weapon Shop
2. **Northwest**: Map 14 (1, 4) - Visit Sandman to learn about dynamite
3. **Southeast**: Map 44 (4, 4) - Find Dynamite at (14, 6)
4. **Return**: Map 24 - Use dynamite on Boatman
5. **Collect Items**:
   - Map 24: Rutter (9, 1)
   - Map 22: Wood (10, 4)
   - Map 14: Dead Wood (3, 8)
   - Map 13: Cloth (7, 6)
6. **Late Game**: Map 41 - Necromancer for boat quest
7. **Castle**: Map 11 - Final area (requires boat/ship)

### Save & Healing Locations

| Location | Map | Type | Notes |
|----------|-----|------|-------|
| Healer | Map 24 (2, 4) | Paid healing + Save | Starting area |
| Fountain | Map 11 (1, 1) | Free healing + Save | Northwest |
| Fountain | Map 32 (3, 2) | Free healing + Save | River area |

### Shop Locations

| Shop | Map | Coordinates | Services |
|------|-----|-------------|----------|
| Weapon Shop | Map 24 | (2, 7) | Weapons & Armor, prices vary by Gothox's mood |

---

## Complete World Map (All 16 Maps)

The game world consists of 16 interconnected maps arranged in a 4×4 grid. Each map is 20×10 tiles. Below is a table showing the complete world layout with key features.

### Full World Visualization

#### MAP 11 (Northwest) - Castle Area
- Fountain (h) - Healing/Save
- Dense forest (T,t)
- Grass clearings (1)
- Castle in northeast
- _Spawn: (17,7)_

#### MAP 21 (North-Mid) - Forest
- Heavy tree coverage
- Grass paths
- Monster encounters
- Sandy patches south
- _Spawn: (0,0)_

#### MAP 31 (Northeast) - River North
- Shallow water (a)
- Deep water (w)
- Rocks (R,r)
- Graves (G)
- _Spawn: (19,1)_

#### MAP 41 (Necromancer) - Graveyard
- **Necromancer (h)**
- Many graves (G)
- Rocky terrain
- Water on west edge
- _Spawn: (1,7)_

---

#### MAP 12 (West-Mid) - Mixed Forest
- Forest & grass mix
- Small trees (t)
- Monster encounters
- _Spawn: (0,0)_

#### MAP 22 (Forest-Mid) - Wood Location
- **WOOD (10,4)**
- Dense forest
- Grass clearings
- Trees & small trees
- _Spawn: (0,0)_

#### MAP 32 (River-Mid) - Castle Island
- **Fountain (h)**
- Castle (C) 4×4
- River surrounds
- Shallow water
- _Spawn: (19,8)_

#### MAP 42 (East-Mid) - Rocky Mountains
- Two huts (h)
- Large rocks (R)
- Small rocks (r)
- Graves (G)
- _Spawn: (17,2)_

---

#### MAP 13 (Southwest) - Cloth Location
- **CLOTH (7,6)**
- Dense forest
- Grass paths
- Sandy areas south
- _Spawn: (0,0)_

#### MAP 23 (Forest-South) - Dense Forest
- Heavy trees
- High encounter rate
- Grass paths
- Sand transitions
- _Spawn: (0,0)_

#### MAP 33 (River-South) - River Crossing
- Deep water (w)
- Shallow water (a)
- Sandy banks (2)
- Rocky outcrops
- _Spawn: (0,0)_

#### MAP 43 (Mountain) - Mountain Jack
- **Mountain Jack (h)**
- Rocky mountains
- Large boulders
- Floors (0)
- _Spawn: (17,2)_

---

#### MAP 14 (Sandman) - Desert
- **Sandman (h)**
- **DEAD WOOD (3,8)**
- Sandy desert (2)
- Small rocks (r)
- _Spawn: (11,4)_

#### MAP 24 (START) - Starting Area
- **Boatman (h)**
- **RUTTER (9,1)**
- Healer (H)
- Weapon Shop (W)
- River on east
- _Spawn: (17,5)_

#### MAP 34 (River-South) - River South
- Deep water
- Shallow water
- Sand & rocks
- Floor areas (0)
- _Spawn: (2,1)_

#### MAP 44 (Dynamite) - Rocky Fortress
- **Old Man (h)**
- **DYNAMITE (14,6)**
- Large rocks (R)
- Floor passages
- _Spawn: (19,6)_

### Quest Items Summary

| Item | Map | Coordinates | Description |
|------|-----|-------------|-------------|
| **Dynamite** | Map 44 (4,4) | (14, 6) | Blast rocks to free Boatman |
| **Rutter** | Map 24 (2,4) | (9, 1) | Navigation tool for ship |
| **Wood** | Map 22 (2,2) | (10, 4) | Ship hull material |
| **Dead Wood** | Map 14 (1,4) | (3, 8) | Boat construction material |
| **Cloth** | Map 13 (1,3) | (7, 6) | Sail material |

### NPC Locations Summary

| NPC | Map | World Coords | Story Requirement |
|-----|-----|--------------|-------------------|
| **Sandman** | Map 14 | (1, 4) | Available from start - tells about dynamite |
| **Boatman** | Map 24 | (2, 4) | Starting area - trapped under rocks |
| **Necromancer** | Map 41 | (4, 1) | Story 4.0+ - resurrects Boatman |
| **Mountain Jack** | Map 43 | (4, 3) | Story 4.0+ - tells about soul |
| **Old Man** | Map 44 | (4, 4) | Available from start - airship game |

---

## Detailed Map Sections

Below are detailed ASCII maps for key locations with item spawn points.

---

## Map 13 - Cloth Location

**World Coordinates:** (1, 3)
**Item:** Cloth at position (7, 6)
**Spawn Position:** (0, 0)

```
     0  1  2  3  4  5  6  7  8  9 10 11 12 13 14 15 16 17 18 19
  0: T  T  T  t  t  t  t  T  T  t  1  1  t  t  T  t  1  t  t  t
  1: T  1  1  1  1  1  1  1  1  1  1  1  1  1  1  1  1  1  1  1
  2: T  1  T  T  t  t  t  T  t  t  T  t  t  t  t  t  t  t  1  1
  3: T  1  1  1  1  1  1  1  1  1  1  1  1  1  1  1  1  1  1  1
  4: T  1  T  T  T  t  t  t  T  T  1  t  t  t  t  t  t  t  1  1
  5: t  1  T  T  T  t  1  t  t  t  t  t  t  t  1  t  t  t  1  1
  6: t  1  1  1  1  1  1 [*] 1  1  1  1  1  1  1  1  1  1  1  1
  7: t  1  T  T  T  T  t  t  t  t  t  t  t  t  t  t  t  t  t  t
  8: t  1  1  1  1  1  1  1  1  1  1  1  1  1  1  1  1  1  1  1
  9: t  t  t  t  t  t  2  2  t  t  t  2  2  t  t  t  2  2  t  t
```

**Item Details:**
- **Cloth** - Sail material needed for boat construction

---

## Map 14 - Dead Wood Location & Sandman

**World Coordinates:** (1, 4)
**Item:** Dead Wood at position (3, 8)
**NPC:** Sandman at hut (10, 4)
**Spawn Position:** (11, 4)

```
     0  1  2  3  4  5  6  7  8  9 10 11 12 13 14 15 16 17 18 19
  0: r  r  r  r  r  r  2  2  t  t  t  2  2  t  t  t  2  2  t  t
  1: r  2  2  2  2  2  2  2  2  2  2  2  2  2  2  2  2  2  2  2
  2: r  2  2  r  2  2  2  2  r  2  2  2  2  r  2  2  2  r  2  2
  3: r  2  2  2  2  2  2  2  2  2  2  2  2  2  2  2  2  2  2  2
  4: r  2  2  2  2  2  2  2  2  2 [h] 2  2  2  2  2  2  2  2  2
  5: r  2  2  2  2  2  2  2  2  2  2  2  2  2  2  2  2  2  2  2
  6: r  2  2  r  2  2  2  2  r  2  2  2  2  r  2  2  2  r  2  2
  7: r  2  2  2  2  2  2  2  2  2  2  2  2  2  2  2  2  2  2  2
  8: r  t  2 [*] 2  2  2  2  r  2  2  2  2  r  2  2  2  r  2  2
  9: r  r  r  r  r  r  r  r  r  r  r  r  r  r  r  r  r  r  r  r
```

**Key Locations:**
- **[h]** - Sandman's Hut at (10, 4) - Ask about dynamite here
- **[*]** - Dead Wood at (3, 8) - Boat construction material

---

## Map 22 - Wood Location

**World Coordinates:** (2, 2)
**Item:** Wood at position (10, 4)
**Spawn Position:** (0, 0)

```
     0  1  2  3  4  5  6  7  8  9 10 11 12 13 14 15 16 17 18 19
  0: T  1  1  1  T  T  T  t  1  1  1  1  t  t  t  t  1  1  1  t
  1: 1  1  1  1  1  1  1  1  1  1  1  1  1  1  1  1  1  1  1  1
  2: T  1  T  T  T  T  t  t  t  1  1  t  t  t  t  t  t  t  1  1
  3: T  1  T  T  T  T  T  T  t  1  1  t  t  t  t  t  t  1  1  1
  4: 1  1  T  T  T  T  t  t  T  1 [*] t  t  T  t  t  1  1  0  0
  5: T  1  T  T  T  T  t  T  t  1  1  t  t  t  t  1  1  t  0  0
  6: T  1  t  T  t  t  t  t  t  1  1  t  T  t  1  1  t  t  0  0
  7: 1  1  t  t  t  T  t  t  t  1  1  t  t  1  1  t  t  0  0  0
  8: t  1  1  1  1  1  1  1  1  1  1  1  1  1  0  0  0  0  0  t
  9: t  1  T  T  1  1  T  T  1  1  1  1  t  t  0  0  0  0  0  t
```

**Item Details:**
- **Wood** - Ship hull material at (10, 4)

---

## Map 24 - Rutter Location & Boatman

**World Coordinates:** (2, 4)
**Item:** Rutter at position (9, 1)
**NPC:** Boatman at hut (16, 5) - trapped under rocks initially
**Spawn Position:** (17, 5)

```
     0  1  2  3  4  5  6  7  8  9 10 11 12 13 14 15 16 17 18 19
  0: t  t  2  2  2  2  2  2  2  2  2  2  2  2  r  2  2  w  a  a
  1: 2  2  2  t  t  t  t  t  2  2[*] r  2  2  2  2  w  a  a  w
  2: 2  2  2  r  r  r  r  r  2  2  2  2  2  w  w  a  w  2  t  2
  3: 2  2  2  2  2  2  2  2  r  2  2  w  w  a  w  2  2  2  t  2
  4: r  r  2  2  r  2  r  2  2  2  2  w  w  a  w  2  2  2  2  2
  5: 2  2  2  2  2  r  r  r  2  2  2  2  r  r  w [h] 0  w  R  2
  6: R  R  R  2  2  2  2  2  2  r  r  r  2  2  2  0  0  0  0  2
  7: 2  R  W  R  2  r  2  r  2  r  H  r  2  2  2  0  0  0  0  2
  8: 2  R  2  2  2  2  2  2  2  2  2  r  2  r  r  w  w  w  w  R
  9: r  r  r  r  r  r  r  r  r  r  r  r  r  r  r  w  w  w  w  r
```

**Key Locations:**
- **[*]** - Rutter at (9, 1) - Navigation tool for ship
- **[h]** - Boatman's Hut at (16, 5) - Trapped under rocks (R) until freed with dynamite
- **[H]** - Healer at (10, 7)
- **[W]** - Weapon Shop at (2, 7)

**Water Areas:**
- `w` = Deep water (river) - requires ship to cross
- `a` = Shallow water - still not walkable without ship

---

## Map 44 - Dynamite Location & Old Man

**World Coordinates:** (4, 4)
**Item:** Dynamite at position (14, 6) - marked as D (decorated floor)
**NPC:** Old Man at hut (18, 6)
**Spawn Position:** (19, 6)

```
     0  1  2  3  4  5  6  7  8  9 10 11 12 13 14 15 16 17 18 19
  0: R  0  0  0  0  r  R  R  R  R  R  R  R  R  R  R  R  R  0  R
  1: 0  0  0  0  0  0  0  0  0  0  0  0  0  0  0  0  0  R  0  R
  2: R  0  r  r  r  r  r  0  0  r  r  r  r  r  0  0  0  R  0  R
  3: 0  0  r  R  R  R  r  0  0  r  R  R  R  r  0  0  0  R  0  R
  4: 0  0  r  R  R  R  r  0  0  r  R  R  R  r  0  0  0  R  0  R
  5: R  0  r  R  R  R  r  0  0  r  R  R  R  r  0  R  0  R  0  R
  6: 0  0  r  r  r  r  r  0  0  r  r  r  r  r [D] 0  0  R [h] R
  7: R  0  0  0  0  0  0  0  0  0  0  0  0  0  R  R  0  R  R  R
  8: R  0  0  0  0  0  0  0  0  0  0  0  0  0  0  0  0  0  0  R
  9: R  R  R  R  R  R  R  R  R  R  R  R  R  R  R  R  R  R  R  R
```

**Key Locations:**
- **[D]** - Dynamite at (14, 6) on decorated floor tile
- **[h]** - Old Man's Hut at (18, 6) - Offers airship mini-game

**Note:** The dynamite is hidden at the decorated floor tile "D" - search this location to find it!

---

## Map Navigation

### World Map Grid
```
        Column 1    Column 2    Column 3    Column 4
Row 1:  Map 11      Map 21      Map 31      Map 41
Row 2:  Map 12      Map 22      Map 32      Map 42
Row 3:  Map 13      Map 23      Map 33      Map 43
Row 4:  Map 14      Map 24      Map 34      Map 44
```

### Key Locations Summary

| Map | World Coords | Contains | Notes |
|-----|--------------|----------|-------|
| Map 13 | (1, 3) | **Cloth** at (7, 6) | Forest area with trees |
| Map 14 | (1, 4) | **Dead Wood** at (3, 8)<br>**Sandman** hut | Northwest corner, sandy desert |
| Map 22 | (2, 2) | **Wood** at (10, 4) | Forest between two great forests |
| Map 24 | (2, 4) | **Rutter** at (9, 1)<br>**Boatman** hut<br>Healer, Weapon Shop | Starting area, river access |
| Map 44 | (4, 4) | **Dynamite** at (14, 6)<br>**Old Man** hut | East side of "Two Great Rocks" |

---

## Tips for Finding Items

1. **Use the SEARCH command** when standing on or adjacent to item locations
2. Items marked with `[D]` (decorated floor) or `[*]` are at those exact positions
3. Check the coordinate display in-game: position (x, y) on each map
4. Some items are hidden and require careful exploration
5. Save your game at healers or fountains before long item hunts

---

## Quest Item Collection Order

**Recommended progression:**

1. **Talk to Sandman** (Map 14) → Learn about dynamite location
2. **Find Dynamite** (Map 44 at 14,6) → Required to free Boatman
3. **Free Boatman** (Map 24) → Use dynamite in dialog
4. **Collect Boat Materials:**
   - Wood (Map 22 at 10,4)
   - Dead Wood (Map 14 at 3,8)
   - Cloth (Map 13 at 7,6)
   - Rutter (Map 24 at 9,1)
5. **Give materials to Boatman** → Receive boat plans
6. **Continue main quest** → See STORY_PROGRESSION.md for full walkthrough

---

For complete story progression details, see [STORY_PROGRESSION.md](STORY_PROGRESSION.md)
