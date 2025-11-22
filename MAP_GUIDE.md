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
 │  (7, 5)     │   Forest    │   & Water   │   (Hut-h)   │
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
   - Map 13: Cloth (7, 5)
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

Below is the entire game world showing all 16 maps stitched together (80 columns × 40 rows). This massive visualization shows the complete playable area.

### Full World Visualization

```
         MAP 11              MAP 21              MAP 31              MAP 41
      (Northwest)         (North-Mid)        (Northeast)        (Necromancer)
TTTTTTTTTTTTTTTTT TTTTTTTTTTTTTTTTTTTTTTTTtttttttRrrrrrrrrrwwwrrrrrrrrrrrr
T1T111TTT1TT111T T1T11111TTT1T1tT111T111TTTT11111tttttrrrrrrwrrrrrrrrrrrrrr
T1T1T1111T1T11TT TTTTTTTTTTTTTTTTtttttttt1111111Trwwr0000000000000rrrrr
T1T11TTT111T11TT T1T1tT11tT1tT1ttT111tttTTTTT111rGwwwrGGGGGGGGGGGrr
T1T1111111T11TTT T1T11T11T1tT1Ttt1111TT11111TT1T1111tr0000000rrr
T1TT1TT11TT1T1TT T1t1TTT1111T1tT1111111111T1111111111t11tG00000000
TT1T1TTTTT1T1TTT 1T11111TT1tT1t11TT1111t111111111G00000000rrrr
T1TTT1111T1TT1TT T111111TT111t1111TT11tt1111Gwwwwwr000000000rrrr
T1T1hT1T1tT1TTTTT 1111TTtttt111tt1111t111TT1TTT11r1222222r2000000rr
TTTTTTTTTtt1T11TtttTTTT111t22222r2222222www11112222222222222waawr2222Rrrrrrrrrrrr
      MAP 12              MAP 22              MAP 32              MAP 42
     (West-Mid)         (Forest-Mid)       (River-Mid)          (East-Mid)
TTTTTTTTTTTTTTTTTTTTtttT111TT1T1T1ttt111112222222wwaa wrrrrrrGGGGGGGGGr
T11111111TTT1T1T1TT111111111111111111tttt11112222222wwaar2rhrrrrr0000000Ghr
T1T1tT11T1T1T1TttT111TTTT1tt111tt111t2www22222waarrRrrr2rr0000rrr000R
T1T11111111111111T1tTtttT111ttTtT11tw22222www2CCC CrrrrrrRRRr00rrr000r
T1t1T1111tT1tT1Ttt1tT1111rt1tTt1t2r112222waCCC C22rr000rRRRRrr00000Rr
T1t1T11tTT1tT1TT11T111tt12222rw22Ct1122221waCC CCwrRr002000rRRR000RR
T1t1TTtT111ttT111tttttr22222w CC C11r22RRRrrrRRRRRRrr0000R0R
T1t11TttT1TT1tT1t12222wa wa w1tt222Rt22tt1r1rwwaawr0rRRRR000RR
T111TTtt1tt00002waaaw2wh wtt112222222waaaaa w2RrrrrrrRRRR00RRR
tTT1Tt1TTttttt22222wa awtrrrrrr22222222wwwaawaarrrr0000rrrrrrrRRRRRRRRR
      MAP 13              MAP 23              MAP 33              MAP 43
    (Southwest)        (Forest-South)     (River-South)         (Mountain)
TTTtttttTTt11tttTttt1TT11tt11t11111ttt22tw222222t22wwaarrrr000000rRRRRRRRRRR
T11111111111111111tt111111111t111tt211222222wwwwwwaa2rrrR0rrrR0rr0RRR000000R
T1TTtttTtT1ttttttt1ttt111tt1t11t221212222rwaaaaw222rrrr0rrrrr0rrr0rrrrr0
T11111111111111111111111111t1t11222r2222222waaaw2222rRrr0rrrr00000rrrr0Rr
T1TTTtttTT1tttt11tT111ttt1112222222222222w22222rrr000000000000000RRrrrr0
t1TTTt1t1ttttt1ttttt11t222222rrrr222222waaaaa2222222r000000rrr000000R00r
t111111111111111t1TTTTt122222222222222w222rrrr000000000rrrr0000R0r
t1TTTTtttttttt1tTTT111tt222222r2222rwaaawtt2222wwr000rr00rr0rR000rr0R
t1111111111111111tt12222222r2222r22222222waaaaa11222222t2wwaaawRRRRRRRRRRR
ttttttt222ttt222tt1t222222w222wwwrr000000000200aaaww12222222www000 R
      MAP 14              MAP 24              MAP 34              MAP 44
     (Sandman)           (START)          (River-South)        (Dynamite)
rrrrrrrr222ttt222tt2ttt22222222r2rrr222waaawrr222222000r000R000rwwrR00000r0Dr
r222222222222222ttt222222222ttt2r22www220r2222r22t220r000000r0r00000R0r00000R0R
r22r22222222r222r222222rrrr22222rwwwwww22r222222222r0000r0r00rrr00rR00rrrrrr
r2222222222222r222222r22r2222rrwaa aaa222222www20r0000000r000000rRrrrrrrrrrr
r222222222r22222222222222222rwwwwaaaw0000000000r000rrrr000r000R000rr0R0rrrr0r0
r22r22222222r222r222222rrr2222rwaaaa22t2w00r0rrr00r00rr000000R0R0000rrRrr0r0r
r2222222222222r222222r22r222rrww22rrrr000rr00r0000000000R0R00rR0rr00Rr0000R000R
rtt222222222r222r22222222r02200000wrrrwrrr0000000000rrrr000000RRRR0rrrr0Rrr00rrrr
rrrrrrrrrrrrrrr22222r222000000wrrrwaaa22Rtt1222t2w00rrr000000R00rr00000R00R00r0h0R
rrrrrrrrrrrrrrr1t2222wwwwwwww222222wwaa22tttt1222w0r000rr000000R000R000RRR
```

### Map Labels Key

The full map above shows:
- **Top Row (Y=1)**: Maps 11, 21, 31, 41 - Castle area, forests, river/graves, necromancer
- **Row 2 (Y=2)**: Maps 12, 22, 32, 42 - Forests, **Wood** location, river/castle, rocky mountains
- **Row 3 (Y=3)**: Maps 13, 23, 33, 43 - **Cloth** location, dense forest, river, Mountain Jack
- **Bottom Row (Y=4)**: Maps 14, 24, 34, 44 - Sandman/deadwood, **START**/rutter/shops, river, **Dynamite**/Old Man

### Key Item Markers in Full Map

Looking at the combined map above, you can navigate to find:
- **Map 11** (top-left quadrant): Castle with fountain `h`
- **Map 13** (third row, left): Cloth at approximately column 7, row 25
- **Map 14** (bottom-left): Dead Wood at approximately column 3, row 38 + Sandman hut `h`
- **Map 22** (row 2, mid-left): Wood at approximately column 30, row 14
- **Map 24** (bottom row, mid-left): Starting position - Boatman `h`, Healer `H`, Weapon Shop `W`
- **Map 41** (top-right): Necromancer hut `h` in graveyard (`G` tiles)
- **Map 43** (third row, right): Mountain Jack hut `h`
- **Map 44** (bottom-right): Dynamite at decorated floor `D`, Old Man hut `h`

### Navigation Notes

- **River System**: Runs north-south through maps 31-34 (columns 40-59) marked by `w` (water) and `a` (shallow)
- **Forest Zones**: Heavy tree coverage (`T`,`t`) in maps 11-13, 21-23
- **Desert/Sand**: Concentrated in maps 14, 23, 24 (marked by `2`)
- **Rocky Mountains**: Eastern maps 41-44 (marked by `R`, `r`)
- **Graveyard**: Map 41 (top-right) marked with `G` graves

---

## Detailed Map Sections

Below are detailed ASCII maps for key locations with item spawn points.

---

## Map 13 - Cloth Location

**World Coordinates:** (1, 3)
**Item:** Cloth at position (7, 5)
**Spawn Position:** (0, 0)

```
     0  1  2  3  4  5  6  7  8  9 10 11 12 13 14 15 16 17 18 19
  0: T  T  T  t  t  t  t  T  T  t  1  1  t  t  T  t  1  t  t  t
  1: T  1  1  1  1  1  1  1  1  1  1  1  1  1  1  1  1  1  1  1
  2: T  1  T  T  t  t  t  T  t  t  T  t  t  t  t  t  t  t  1  1
  3: T  1  1  1  1  1  1  1  1  1  1  1  1  1  1  1  1  1  1  1
  4: T  1  T  T  T  t  t  t  T  T  1  t  t  t  t  t  t  t  1  1
  5: t  1  T  T  T  t  1 [*] t  t  t  t  t  t  1  t  t  t  1  1
  6: t  1  1  1  1  1  1  1  1  1  1  1  1  1  1  1  1  1  1  1
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
| Map 13 | (1, 3) | **Cloth** at (7, 5) | Forest area with trees |
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
   - Cloth (Map 13 at 7,5)
   - Rutter (Map 24 at 9,1)
5. **Give materials to Boatman** → Receive boat plans
6. **Continue main quest** → See STORY_PROGRESSION.md for full walkthrough

---

For complete story progression details, see [STORY_PROGRESSION.md](STORY_PROGRESSION.md)
