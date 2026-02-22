# Balance Tuner Agent Instructions

You are tuning the balance of "The Evil Wrath of Zargon", a tile-based RPG. Your job is to adjust numerical variables so the game feels challenging but fair across all phases of play.

## How the Game Works

The player (Joe) explores a tile map, encounters random monsters, fights turn-based battles, earns XP and gold, levels up, buys equipment, and progresses through a story. There is also a challenge/prestige system that adds difficulty modifiers for replayability.


## How to know if the game is too easy or too hard
- Is the game to easy? If the player is able to survive easily for more than 15 battles, in a row, then it is too easy. Make it harder to survive. If the player is dying before 10 battles it is too hard, make it easier to survive.

### Battle Flow
1. Player attacks: damage = `baseAP + weaponBonus` (no randomness on player side)
2. If monster survives, monster attacks using the **K-formula**:
   ```
   randomMultiplier = uniform(0.84, 1.16)   // ±16% variance
   rawDamage = monsterAP * K / (totalDefense + K) * randomMultiplier
   damage = max(1, rawDamage.toInt())
   ```
   where `K = 20.0` (constant in BattleViewModel.kt)
3. Repeat until one side is dead

`totalDefense = baseDP + armorBonus`

**Why K-formula, not flat subtraction:** The old formula `max(1, monsterAP - totalDefense + random)` creates a hard cliff — once totalDefense exceeds monsterAP, every hit floors to 1 (e.g. a monster with AP=10 vs defense=30 does 1 dmg, same as defense=11). The K-formula makes defense a *percentage reduction*, so every point of defense always matters. With K=20 and totalDefense=20, a monster deals 50% of raw damage. With totalDefense=60, it deals 25%. No cliff, just smooth diminishing returns.

**HP vs Defense are separate stats:**
- `baseDP` = damage absorption only (in the damage formula denominator)
- `maxHP` = hit point pool (how much damage Joe can take before dying)
- These were decoupled intentionally — previously `baseDP` acted as both, creating a double-scaling problem where leveling up made Joe both harder to kill AND dramatically reduced monster damage simultaneously.

### Monster Scaling
Each monster gets a `scalingFactor` based on player level:
```
scalingFactor = 1
for each level above 1:
    67% chance to increment scalingFactor (whatlev in 2..3 of nextInt(1,4))
    stop when scalingFactor >= 6  // hard cap
```
Monster AP and HP are both multiplied by scalingFactor.

The 67% probability (up from 50%) was chosen to make higher-level monsters feel genuinely threatening. The cap at 6 prevents encounters from becoming near-unwinnable at very high player levels — without it, expected scalingFactor at L10 would be 7+ and could spike higher.

### Level-Up
On level-up, the player gains:
- AP: `random(0..currentLevel) + 2`
- HP: `random(3..7)` — independent of defense
- DP (defense): `4` (flat, separate from HP gain)
- MP: `3 + random(0..currentLevel) + 1`

Full HP/MP restore on level-up.

### XP Thresholds
Starting at 30 XP for level 2. Each subsequent level: `nextLevelXP = nextLevelXP + (nextLevelXP + currentLevel * 30)`. This grows very fast — roughly doubling each level.

---

## All Tunable Variables and Where to Find Them

### Character Starting Stats
**File:** `app/app/app/src/main/java/com/greenopal/zargon/data/models/CharacterStats.kt`
| Variable | Default | Description |
|---|---|---|
| `baseAP` | 5 | Starting attack power |
| `baseDP` | 20 | Starting defense (damage absorption only, NOT hit points) |
| `maxHP` | 20 | Starting maximum hit points (separate from defense) |
| `currentHP` | 20 | Starting current HP |
| `baseMP` | 10 | Starting magic points |
| `currentMP` | 10 | Starting current MP |

### Level-Up Gains
**File:** `app/app/app/src/main/java/com/greenopal/zargon/domain/progression/ProgressionSystem.kt`
| Variable | Formula | Description |
|---|---|---|
| `apGain` | `random(0..level) + 2` | AP gained per level |
| `hpGain` | `random(3..7)` | HP gained per level (3-7, independent of defense) |
| `dpGain` | `4` | Defense gained per level — flat, does NOT affect HP |
| `mpGain` | `3 + random(0..level) + 1` | MP gained per level |

**Key insight:** HP and defense grow independently. This prevents the double-scaling problem where growing defense simultaneously reduces monster damage AND increases total health.

### Damage Formula Constant
**File:** `app/app/app/src/main/java/com/greenopal/zargon/ui/viewmodels/BattleViewModel.kt`
| Constant | Value | Effect |
|---|---|---|
| `DAMAGE_K` | 20.0 | Lower K = defense more powerful. At K=20, totalDefense=20 halves raw damage, totalDefense=60 reduces to 25% |
| variance range | 0.84–1.16 | ±16% random multiplier on each monster hit |

### XP Thresholds
**File:** `app/app/app/src/main/java/com/greenopal/zargon/domain/progression/ProgressionSystem.kt`
| Variable | Value | Description |
|---|---|---|
| Initial nextLevelXP | 30 | XP needed for level 2 |
| Growth formula | `nextLev + (nextLev + level * 30)` | XP for subsequent levels |

### Weapons (price and attack bonus)
**File:** `app/app/app/src/main/java/com/greenopal/zargon/ui/screens/WeaponShopScreen.kt`

Power law curve: `bonus ≈ cost^0.55` — large gains per gold on cheap items, strong diminishing returns on expensive ones.

| Weapon | Price | Attack Bonus |
|---|---|---|
| Dagger | 20 | 5 |
| Short Sword | 45 | 8 |
| Long Sword | 100 | 13 |
| Sword of Thorns | 175 | 18 |
| Broad Sword | 280 | 23 |
| Two-Handed Sword | 400 | 28 |
| Atlantean Sword | 600 | 35 |

### Armor (price and defense bonus)
**File:** `app/app/app/src/main/java/com/greenopal/zargon/ui/screens/WeaponShopScreen.kt`

Power law curve: `bonus ≈ cost^0.55` — same principle as weapons.

| Armor | Price | Defense Bonus |
|---|---|---|
| Cloth | 15 | 5 |
| Leather | 35 | 8 |
| Plated Leather | 80 | 15 |
| Spiked Leather | 160 | 20 |
| Chain Mail | 300 | 30 |
| Platemail | 550 | 42 |

### Monster Types (base stats)
**File:** `app/app/app/src/main/java/com/greenopal/zargon/data/models/MonsterType.kt`
| Monster | Base AP | Base HP | Min Level | Notes |
|---|---|---|---|---|
| Slime | 1 | 5 | 1 | Easiest |
| Bat | 2 | 10 | 1 | |
| Babble | 5 | 12 | 1 | |
| Spook | 7 | 14 | 1 | |
| Beleth | 8 | 16 | 2 | |
| Skander Snake | 12 | 20 | 5 | |
| Necro | 13 | 30 | 6 | |
| Kraken | 25 | 60 | — | Ship boss, always scalingFactor=1 |
| ZARGON | 100 | 400 | — | Final boss, always scalingFactor=1 |

Base stats are multiplied by `scalingFactor` (random, see Monster Scaling above).

### Monster Encounter Rates
**File:** `app/app/app/src/main/java/com/greenopal/zargon/domain/battle/MonsterSelector.kt`

Roll 1-21, equal distribution (~14% each):
- 1-3: Slime (14%)
- 4-6: Bat (14%)
- 7-9: Babble (14%)
- 10-12: Spook (14%)
- 13-15: Beleth (14%, requires level 2+, re-rolls if locked)
- 16-18: Skander Snake (14%, requires level 5+, re-rolls if locked)
- 19-21: Necro (14%, requires level 6+, re-rolls if locked)

At low levels, locked monsters cause re-rolls, so early game is dominated by Slime, Bat, Babble, and Spook.

### Monster Scaling Formula
**File:** `app/app/app/src/main/java/com/greenopal/zargon/domain/battle/MonsterSelector.kt`
```
scalingFactor = 1
for each level above 1:
    if scalingFactor >= 6: break  // hard cap
    roll = nextInt(1, 4)  // 1, 2, or 3
    if roll >= 2: scalingFactor++  // 67% chance
```
Scaled stats: `AP = baseAP * scalingFactor`, `HP = baseHP * scalingFactor`

### XP Rewards Per Monster
**File:** `app/app/app/src/main/java/com/greenopal/zargon/domain/progression/ProgressionSystem.kt`
| Monster | Base XP | Scales with factor? |
|---|---|---|
| Slime | 5 | yes |
| Bat | 8 | yes |
| Babble | 12 | yes |
| Spook | 15 | yes |
| Beleth | 20 | yes |
| Skander Snake | 30 | yes |
| Necro | 50 | yes |
| Kraken | 100 | no |
| ZARGON | 1000 | no |

XP reward = `baseXP * scalingFactor` (except bosses)

### Gold Rewards Per Monster
**File:** `app/app/app/src/main/java/com/greenopal/zargon/domain/progression/ProgressionSystem.kt`
| Monster | Base Gold |
|---|---|
| Slime | 3 |
| Bat | 5 |
| Babble | 10 |
| Spook | 15 |
| Beleth | 20 |
| Skander Snake | 30 |
| Necro | 45 |
| Kraken | 100 |
| ZARGON | 0 |

Gold reward = `baseGold + (scalingFactor * 3)`

### Spells (damage/healing, MP cost, level required)
**File:** `app/app/app/src/main/java/com/greenopal/zargon/domain/battle/Spell.kt`

Effect formula: `baseDamage + random(1..randomBonus) + playerLevel`
For healing: result is capped at `maxHP` by `heal()`.

| Spell | MP Cost | Level | Base | Random Bonus | Type | Notes |
|---|---|---|---|---|---|---|
| Flame | 3 | 1 | 8 | 1-12 | damage | 10-21 at L1 |
| Cure | 4 | 2 | 15 | 1-5 | healing | 18-22 heal at L2 |
| Water | 8 | 3 | 18 | 1-15 | damage | 22-36 at L3 |
| Lightning | 12 | 4 | 28 | 1-18 | damage | 33-50 at L4 |
| BubbleBlast | 25 | 5 | 52 | 1-25 | damage | 58-82 at L5 |
| Restore | 15 | 6 | 500 | 0 | healing | Full HP reset (capped by heal()) |
| Firestorm | 35 | 7 | 72 | 1-30 | damage | 80-109 at L7 |
| Divine Light | 50 | 10 | 125 | 1-40 | damage | 136-175 at L10 |

**Restore design intent:** baseDamage=500 guarantees it always heals to full HP regardless of level or maxHP. This is intentional — Restore is the key tool for surviving the ZARGON boss fight at L10.

### Challenge Modifiers
**File:** `app/app/app/src/main/java/com/greenopal/zargon/domain/challenges/ChallengeModifiers.kt`
| Challenge | Effect | Multiplier |
|---|---|---|
| WEAK_WEAPONS | Weapon bonus halved | 0.5x |
| WEAK_ARMOR | Armor bonus halved | 0.5x |
| STRONG_ENEMIES | Monster AP and HP doubled | 2.0x |
| STRONGER_ENEMIES | Monster AP and HP 3.2x | 3.2x |
| NO_MAGIC | Cannot cast spells | — |
| ONE_DEATH | Permadeath | — |
| IMPOSSIBLE_MISSION | Permadeath + strong enemies (2.0x) | 2.0x |
| MAGE_QUEST | Weak weapons + weak armor (both 0.5x) | 0.5x |
| WARRIOR_MODE | No magic + stronger enemies (3.2x) + weapon bonus (1.25x) | 3.2x/1.25x |

### Prestige Bonuses
**File:** `app/app/app/src/main/java/com/greenopal/zargon/domain/challenges/ChallengeModifiers.kt`
| Bonus | Effect | Multiplier |
|---|---|---|
| GREAT_WEAPONS | Weapon bonus +150% | 2.5x |
| GREATER_ARMOR | Armor bonus +100% | 2.0x |
| MASTER_SPELLBOOK | Spell effects +50% | 1.5x |
| XP_BOOST | +10% XP | 1.1x |
| GOLD_BOOST | +10% gold | 1.1x |
| STARTING_GOLD | +100 gold at game start | flat |

---

## Simulation Framework

Run simulations from `app/app/` with: `./runsim.sh`

Or individually:
```bash
./gradlew testDebugUnitTest --tests "com.greenopal.zargon.simulation.SurvivalSimulation"
./gradlew testDebugUnitTest --tests "com.greenopal.zargon.simulation.ProgressionSimulation"
./gradlew testDebugUnitTest --tests "com.greenopal.zargon.simulation.ChallengeDifficultySimulation"
```

Output is in: `app/app/app/build/reports/tests/testDebugUnitTest/index.html`
Or stdout in: `app/app/app/build/test-results/testDebugUnitTest/` (XML files, look for `<system-out>`)

### Canonical Simulation Loadouts

All simulations test across 5 equipment tiers representing game phases:
| Phase | Level | Weapon Bonus | Armor Bonus | Notes |
|---|---|---|---|---|
| Early | 1 | +5 (Dagger, 20g) | +5 (Cloth, 15g) | Starting tier |
| Early-Mid | 3 | +8 (Short Sword, 45g) | +8 (Leather, 35g) | After a few shop visits |
| Mid | 5 | +13 (Long Sword, 100g) | +15 (Plated Leather, 80g) | Affordable mid-game |
| Late-Mid | 7 | +23 (Broad Sword, 280g) | +30 (Chain Mail, 300g) | Serious investment |
| End | 10 | +28 (Two-Handed Sword, 400g) | +42 (Platemail, 550g) | Best attainable gear |

Character stats at each tier (baseDP = 20 + (level-1)*4, maxHP = 20 + (level-1)*5):
| Phase | Level | baseAP | baseDP | maxHP |
|---|---|---|---|---|
| Early | 1 | 5 | 20 | 20 |
| Early-Mid | 3 | 9 | 28 | 30 |
| Mid | 5 | 13 | 36 | 40 |
| Late-Mid | 7 | 17 | 44 | 50 |
| End | 10 | 23 | 56 | 65 |

**SurvivalSimulation** — How many consecutive battles the player survives (no healing between battles). Reports avg, P50, P80 per tier.

**ProgressionSimulation** — How many battles to gain 2 levels from each starting tier. Reports avg battles, P50, P80, win rate.

**ChallengeDifficultySimulation** — For each challenge modifier × each tier, how many battles survived. Also shows impact of each prestige bonus. Reports a matrix of challenge × bonus → avg survival.

### Simulation Source Files
All in `app/app/app/src/test/java/com/greenopal/zargon/simulation/`:
- `BattleSimulator.kt` — Core battle loop, uses real `ChallengeModifiers` class
- `SimulationHelpers.kt` — Monster selection (real encounter table), random scaling, XP/gold calc
- `SurvivalSimulation.kt`
- `ProgressionSimulation.kt`
- `ChallengeDifficultySimulation.kt`

**Important:** The simulations use the real `ChallengeModifiers` class from the game code, not reimplemented formulas. If you change a modifier value, the simulations will automatically reflect it.

**Important:** Character model in simulations must use decoupled HP/defense:
```kotlin
CharacterStats(
    baseAP = ...,
    baseDP = 20 + (level-1)*4,   // defense only
    maxHP = 20 + (level-1)*5,    // separate HP pool
    currentHP = maxHP,
    ...
)
```

---

## Known Balance Issues and Lessons Learned

### 1. Defense has diminishing returns (by design)
With K=20, totalDefense matters but has diminishing returns. Going from defense=0 to defense=20 halves monster damage. Going from defense=20 to defense=60 halves it again. This is the intended behavior — defense never becomes irrelevant, but it also never makes you truly invincible.

**What to watch for:** If win rates at end-game are above 95% per battle (not per run), defense may be too high. Adjust K downward to make defense less impactful, or increase monster AP.

### 2. The bi-modal difficulty cliff still exists (softer now)
Even with the K-formula, there's still a noticeable difficulty jump when players reach a new scaling tier. A scalingFactor=3 Spook vs scalingFactor=4 Spook isn't a smooth ramp. This is accepted as a design property — the cap at 6 prevents it from becoming punishing.

### 3. Player damage has no randomness
Player attacks always do `baseAP + weaponBonus` — completely deterministic. This means battles have predictable turn counts against weaker enemies. Consider adding a random component if battles feel too mechanical.

### 4. Equipment bonus scaling is flat, not proportional
WEAK_WEAPONS/WEAK_ARMOR halve the equipment BONUS, not total attack/defense. At level 5 with baseAP ~13 and weaponBonus 13, halving the weapon bonus to 6 reduces total damage from 26 to 19 — a 27% reduction. More significant than before the power law redesign, but still not the 50% the player might expect.

### 5. Spell damage doesn't scale much with level
Spell formula: `baseDamage + random(1..randomBonus) + playerLevel`. The `+ playerLevel` component is small compared to baseDamage. Spells jump in power by tier (Flame→Water→BubbleBlast→Firestorm→Divine Light) but individual spells don't grow dramatically with level. This is intentional — the spell system rewards unlocking new spells, not grinding levels with old ones.

### 6. Restore is deliberately overpowered for boss fights
Restore's baseDamage=500 ensures it always fully heals, which makes it the key tool for ZARGON boss encounters at L10. Without Restore, L10 vs ZARGON is near-impossible. With Restore, win rate is ~99.7%. This asymmetry is a design choice — players who made it to L10 and unlocked Restore deserve a winnable boss fight.

### 7. Scaling cap at 6 was intentional
Without the cap, expected scalingFactor at L10 with 67% probability over 9 levels = ~7. You'd regularly encounter scalingFactor 8-9 which are near-unwinnable against most monsters. The cap at 6 keeps scaling threatening but survivable with good equipment.

### 8. Healing between battles
The simulations currently do NOT heal between battles. If you want to test with healing, you'd need to add that back. In the real game, players can use Cure/Restore between battles if they have MP.

---

## Balance Goals

Aim for these targets:

- **Normal gameplay (no challenges):** Player should survive 5-15 battles per tier before dying. Win rate should be 70-90% per individual battle.
- **Challenges should be noticeably harder:** WEAK_ARMOR and WEAK_WEAPONS should reduce survival by at least 30-50%. STRONG_ENEMIES and STRONGER_ENEMIES should be dramatically harder.
- **Equipment should matter:** Upgrading weapons/armor should produce a clear improvement in simulation results. The gap between tiers should be visible.
- **Prestige bonuses should help but not trivialize challenges:** A prestige bonus active during a challenge should improve survival by 10-30%, not erase the difficulty entirely.
- **Progression pacing:** Gaining 2 levels should take roughly 20-50 battles at each tier. Not so fast it feels trivial, not so slow it feels like a grind.

---

## Tuning Workflow

1. Run `./runsim.sh` to get baseline numbers
2. Identify which tiers/challenges are out of range
3. Adjust variables in the code files listed above
4. Run simulations again to verify the change had the intended effect
5. Check for knock-on effects (e.g., changing monster AP affects both normal and challenge mode)
6. Repeat until all targets are met

Always change one variable at a time and re-simulate. Small changes compound — a 20% buff to monster AP combined with a 20% nerf to armor can result in dramatically different outcomes. With the K-formula, defense changes have more predictable, proportional effects than they did with flat subtraction.
