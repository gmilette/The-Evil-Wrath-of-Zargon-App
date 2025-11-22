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

<table>
<tr>
<th>MAP 11<br/>(Northwest)</th>
<th>MAP 21<br/>(North-Mid)</th>
<th>MAP 31<br/>(Northeast)</th>
<th>MAP 41<br/>(Necromancer)</th>
</tr>
<tr>
<td>
<strong>Castle Area</strong><br/>
• Fountain (h) - Healing/Save<br/>
• Dense forest (T,t)<br/>
• Grass clearings (1)<br/>
• Castle in northeast<br/>
<br/>
<em>Spawn: (17,7)</em>
</td>
<td>
<strong>Forest</strong><br/>
• Heavy tree coverage<br/>
• Grass paths<br/>
• Monster encounters<br/>
• Sandy patches south<br/>
<br/>
<em>Spawn: (0,0)</em>
</td>
<td>
<strong>River North</strong><br/>
• Shallow water (a)<br/>
• Deep water (w)<br/>
• Rocks (R,r)<br/>
• Graves (G)<br/>
<br/>
<em>Spawn: (19,1)</em>
</td>
<td>
<strong>Graveyard</strong><br/>
• <strong>Necromancer (h)</strong><br/>
• Many graves (G)<br/>
• Rocky terrain<br/>
• Water on west edge<br/>
<br/>
<em>Spawn: (1,7)</em>
</td>
</tr>

<tr>
<th>MAP 12<br/>(West-Mid)</th>
<th>MAP 22<br/>(Forest-Mid)</th>
<th>MAP 32<br/>(River-Mid)</th>
<th>MAP 42<br/>(East-Mid)</th>
</tr>
<tr>
<td>
<strong>Mixed Forest</strong><br/>
• Forest & grass mix<br/>
• Small trees (t)<br/>
• Monster encounters<br/>
<br/>
<br/>
<em>Spawn: (0,0)</em>
</td>
<td>
<strong>Wood Location</strong><br/>
• <strong>WOOD (10,4)</strong><br/>
• Dense forest<br/>
• Grass clearings<br/>
• Trees & small trees<br/>
<br/>
<em>Spawn: (0,0)</em>
</td>
<td>
<strong>Castle Island</strong><br/>
• <strong>Fountain (h)</strong><br/>
• Castle (C) 4×4<br/>
• River surrounds<br/>
• Shallow water<br/>
<br/>
<em>Spawn: (19,8)</em>
</td>
<td>
<strong>Rocky Mountains</strong><br/>
• Two huts (h)<br/>
• Large rocks (R)<br/>
• Small rocks (r)<br/>
• Graves (G)<br/>
<br/>
<em>Spawn: (17,2)</em>
</td>
</tr>

<tr>
<th>MAP 13<br/>(Southwest)</th>
<th>MAP 23<br/>(Forest-South)</th>
<th>MAP 33<br/>(River-South)</th>
<th>MAP 43<br/>(Mountain)</th>
</tr>
<tr>
<td>
<strong>Cloth Location</strong><br/>
• <strong>CLOTH (7,6)</strong><br/>
• Dense forest<br/>
• Grass paths<br/>
• Sandy areas south<br/>
<br/>
<em>Spawn: (0,0)</em>
</td>
<td>
<strong>Dense Forest</strong><br/>
• Heavy trees<br/>
• High encounter rate<br/>
• Grass paths<br/>
• Sand transitions<br/>
<br/>
<em>Spawn: (0,0)</em>
</td>
<td>
<strong>River Crossing</strong><br/>
• Deep water (w)<br/>
• Shallow water (a)<br/>
• Sandy banks (2)<br/>
• Rocky outcrops<br/>
<br/>
<em>Spawn: (0,0)</em>
</td>
<td>
<strong>Mountain Jack</strong><br/>
• <strong>Mountain Jack (h)</strong><br/>
• Rocky mountains<br/>
• Large boulders<br/>
• Floors (0)<br/>
<br/>
<em>Spawn: (17,2)</em>
</td>
</tr>

<tr>
<th>MAP 14<br/>(Sandman)</th>
<th>MAP 24<br/>(START)</th>
<th>MAP 34<br/>(River-South)</th>
<th>MAP 44<br/>(Dynamite)</th>
</tr>
<tr>
<td>
<strong>Desert</strong><br/>
• <strong>Sandman (h)</strong><br/>
• <strong>DEAD WOOD (3,8)</strong><br/>
• Sandy desert (2)<br/>
• Small rocks (r)<br/>
<br/>
<em>Spawn: (11,4)</em>
</td>
<td>
<strong>Starting Area</strong><br/>
• <strong>Boatman (h)</strong><br/>
• <strong>RUTTER (9,1)</strong><br/>
• Healer (H)<br/>
• Weapon Shop (W)<br/>
• River on east<br/>
<em>Spawn: (17,5)</em>
</td>
<td>
<strong>River South</strong><br/>
• Deep water<br/>
• Shallow water<br/>
• Sand & rocks<br/>
• Floor areas (0)<br/>
<br/>
<em>Spawn: (2,1)</em>
</td>
<td>
<strong>Rocky Fortress</strong><br/>
• <strong>Old Man (h)</strong><br/>
• <strong>DYNAMITE (14,6)</strong><br/>
• Large rocks (R)<br/>
• Floor passages<br/>
<br/>
<em>Spawn: (19,6)</em>
</td>
</tr>
</table>

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
