# The Evil Wrath of Zargon - Story Progression Guide

## Story Status Overview

The game uses a `storyStatus` float value to track progression. This guide shows all story stages, triggers, and requirements.

---

## Story Stages

### 1.0 - Game Start (Default)

**State:**
- Beginning of the game
- Boatman is trapped under rocks
- No knowledge of dynamite location

**Available Actions:**
- Talk to Sandman → Ask about dynamite (question 3)

**Trigger:** Sandman dialog - "(ask him about the dynamite)"
**Result:** Advances to **1.5**, Sandman tells you dynamite location

---

### 1.5 - Learned About Dynamite

**State:**
- Sandman told you where to find dynamite
- Dynamite location revealed: Map 44 at position (14, 6)

**Requirements to Progress:**
- Story status = 1.5
- Have "dynamite" in inventory (find at Map 44)

**Available Actions:**
- Find dynamite on Map 44
- Return to Boatman → Use dynamite (question 3)

**Trigger:** Boatman dialog - "(use the dynamite)"
**Result:** Advances to **2.0**, removes dynamite, frees Boatman

---

### 2.0 - Boatman Freed

**State:**
- Boatman is alive and free from rocks
- Boatman can help build a ship but needs materials

**Materials Needed:**
- Dead Wood (Map 14 at 3, 8)
- Wood (Map 22 at 10, 4)
- Cloth (Map 13 at 7, 5)
- Rutter (Map 24 at 9, 1)

**Requirements to Progress:**
- Story status = 2.5 (after giving initial wood)
- Have "wood" item

**Available Actions:**
- Collect boat materials
- Give wood to Boatman (question 3)

**Trigger:** Boatman dialog - "(give him the wood)"
**Result:** Receive "boat plans", advances to **2.5**

---

### 2.5 - Gave Wood to Boatman

**State:**
- Boatman working on boat plans
- Continue collecting other materials

**Next Steps:**
- Collect remaining materials (dead wood, cloth, rutter)
- Give all materials to Boatman
- Boatman dies mysteriously (advances to **3.0**)

---

### 3.0 - Boatman Dies

**State:**
- Boatman has died after receiving materials
- Boat plans are on his body

**Available Actions:**
- Search Boatman's body → Get boat plans
- Talk to Sandman → Learn about Necromancer

**Sandman Dialog (3.0+):**
- Tells you to seek the Necromancer in the northeast
- Necromancer can resurrect the dead

**Result:** Advances to **4.0** when you search the body

---

### 4.0 - Need Necromancer

**State:**
- Boatman is dead
- Need to find Necromancer to resurrect him
- Need a "trapped soul" as payment

**Available NPCs:**
- Mountain Jack (Map 43) - Appears at story 4.0+
- Tells you where to find a trapped soul (graveyard in northeast)

**Requirements to Progress:**
- Story status = 4.3
- Have "soul" item (found at graveyard)

**Available Actions:**
- Find trapped soul at graveyard
- Take soul to Necromancer (Map 41)

**Trigger:** Necromancer dialog - "(give him the soul)"
**Result:** Boatman resurrected, advances to **5.0**

---

### 4.3 - Have Soul for Necromancer

**State:**
- You have the trapped soul
- Ready to trade with Necromancer

**Trigger:** Give soul to Necromancer
**Result:** Boatman resurrected, advances to **5.0**

---

### 5.0 - Boatman Resurrected

**State:**
- Boatman is alive again
- Can build the ship if you have boat plans

**Requirements to Progress:**
- Have "boat plans" item

**Available Actions:**
- Give boat plans to Boatman (question 3)

**Trigger:** Boatman dialog - "(give boat plans)"
**Result:** Receive "ship" item, advances to **5.5**

---

### 5.5 - Have Ship

**State:**
- You have the ship
- Can travel the river
- Can reach the castle island

**Available Actions:**
- Navigate river using ship
- Reach castle for final confrontation
- Battle Zargon

---

### 6.0+ - Endgame

**State:**
- Castle accessible
- Final battle with Zargon
- Game completion

---

## Key Item Locations

| Item | Map | Position (x, y) | Description |
|------|-----|----------------|-------------|
| Dynamite | Map 44 | (14, 6) | Used to blast through rocks trapping Boatman |
| Dead Wood | Map 14 | (3, 8) | Boat construction material |
| Rutter | Map 24 | (9, 1) | Navigation tool for ship |
| Cloth | Map 13 | (7, 6) | Sail material for ship |
| Wood | Map 22 | (10, 4) | Ship hull material |
| Soul | Graveyard (northeast) | TBD | Payment for Necromancer |

---

## NPC Locations

| NPC | Map | World Coords | Notes |
|-----|-----|--------------|-------|
| Boatman | Map 24 | (2, 4) | Starting area, trapped under rocks |
| Sandman | Map 14 | (1, 4) | Northwest corner, bottom row |
| Necromancer | Map 41 | (4, 1) | Northeast area, graveyard |
| Mountain Jack | Map 43 | (4, 3) | Appears at story 4.0+ |
| Old Man | Map 44 | (4, 4) | Southeast corner |
| Fountain | Map 11 or Map 32 | (1,1) or (3,2) | Healing and save points |

---

## Quest Flow Summary

1. **Talk to Sandman** → Learn about dynamite → Status: 1.5
2. **Find Dynamite** (Map 44) → Add to inventory
3. **Use Dynamite on Boatman** → Free him → Status: 2.0
4. **Collect Boat Materials** → Dead wood, wood, cloth, rutter
5. **Give Materials to Boatman** → Receive boat plans → Status: 2.5-3.0
6. **Boatman Dies** → Search body for plans → Status: 3.0-4.0
7. **Talk to Mountain Jack** → Learn about trapped soul
8. **Find Trapped Soul** → Graveyard in northeast
9. **Give Soul to Necromancer** → Resurrect Boatman → Status: 5.0
10. **Give Boat Plans to Boatman** → Receive ship → Status: 5.5
11. **Navigate River to Castle** → Final confrontation
12. **Defeat Zargon** → Win the game

---

## Debug Tips

When interacting with NPCs, check the Android logcat for:

```
NpcDialogProvider: [NPC] dialog - Story status: X.X
MainActivity: Advancing story from X.X to Y.Y
SaveGameRepository: Story Status: X.X
```

These logs will help you verify that story progression is working correctly.
