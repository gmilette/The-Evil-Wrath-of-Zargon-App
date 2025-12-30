# Zargon Battle Balance Simulation System

A Monte Carlo battle simulation framework for balancing game difficulty by testing combat outcomes across all equipment, level, and enemy combinations.

## Overview

This simulation system allows you to:
- Test player survival rates for every weapon/armor/level/enemy combination
- Identify overpowered or underpowered scenarios
- Experiment with different enemy parameters for game balancing
- Generate detailed statistical reports (CSV, Markdown, Console output)

## Architecture

The simulation runs as **JUnit tests** in the test source set, meaning:
- ✅ Runs on JVM without Android emulator (fast execution)
- ✅ No Android dependencies - pure Kotlin battle logic
- ✅ Reuses actual game models (CharacterStats, MonsterStats, etc.)
- ✅ Easy to execute via `./gradlew test`

## Components

### Core Files

1. **BattleSimulator.kt** - Pure battle engine
   - Replicates exact combat formulas from BattleViewModel.kt
   - Player damage: `baseAP + weaponBonus`
   - Monster damage: `max(1, attackPower - armorBonus + random(0, scalingFactor))`
   - No Android/UI dependencies

2. **SimulationConfig.kt** - Configuration
   - Iterations per scenario (default: 1000)
   - Player levels (default: 1-15)
   - Weapon/armor combinations (all)
   - Monster types and scaling factors
   - Random seed for reproducible results

3. **SimulationResult.kt** - Result data classes
   - `ScenarioResult`: Win/loss rates, average turns, damage stats
   - `SimulationSummary`: Overall statistics across all scenarios

4. **ReportGenerator.kt** - Multi-format output
   - Console: Summary and high-risk scenarios
   - CSV: Machine-readable for Excel/Sheets analysis
   - Markdown: Human-readable report with tables

5. **BattleBalanceTest.kt** - Main JUnit test runner
   - `run full balance simulation`: Tests all 28,350 scenarios
   - `run quick simulation`: Subset for faster iteration
   - `simulate specific scenario`: Debug individual matchups

6. **RunSimulation.kt** - Standalone executable
   - Main function for running simulations outside tests
   - Demonstrates basic usage

## Usage

### Option 1: Run via JUnit Tests (Recommended)

```bash
# From app/ directory:

# Run all simulation tests
./gradlew :app:testDebugUnitTest

# The tests will output results to console and generate:
# - build/reports/balance_simulation.csv
# - build/reports/balance_simulation.md
```

### Option 2: Run Specific Tests from IDE

Open `BattleBalanceTest.kt` in Android Studio and run:
- **"run full balance simulation"** - Complete analysis (28,350 scenarios × 1000 iterations)
- **"run quick simulation"** - Subset for faster feedback
- **"simulate specific scenario"** - Test individual matchups

### Option 3: Modify and Experiment

Create custom tests in `BattleBalanceTest.kt`:

```kotlin
@Test
fun `experiment with buffed Necro`() {
    val simulator = BattleSimulator()

    // Create custom monster with modified stats
    val buffedNecro = MonsterStats(
        type = MonsterType.NECRO,
        attackPower = 20,  // Buffed from 13
        currentHP = 40,    // Buffed from 30
        maxHP = 40,
        scalingFactor = 1
    )

    val character = createCharacter(5, Weapon.LONG_SWORD, Armor.CHAIN_MAIL)

    val results = (1..1000).map {
        simulator.simulateBattle(character, buffedNecro)
    }

    val winRate = results.count { it.outcome == BattleSimulator.BattleOutcome.PlayerVictory } / 1000.0
    println("Level 5 vs buffed Necro: ${(winRate * 100).toInt()}% win rate")
}
```

## Simulation Parameters

### Current Configuration (SimulationConfig defaults)

- **Player Levels:** 1-15 (fixed stats per level, no random level-ups)
- **Weapons:** 7 options (DAGGER +2 → ATLANTEAN_SWORD +25)
- **Armor:** 6 options (CLOTH +5 → PLATEMAIL +50)
- **Monsters:** 9 types (SLIME → ZARGON)
- **Scaling Factors:** 1-5x
- **Iterations:** 1000 battles per scenario
- **Strategy:** Attack only (baseline)

**Total:** 15 × 7 × 6 × 9 × 5 = **28,350 scenarios** × 1000 = **28,350,000 battles**

### Player Stats by Level

Player stats use average/reasonable values (no random level-up simulation):
- Base AP: `5 + (level - 1) × 3`
- Base HP: `20 + (level - 1) × 6`
- Base MP: `10 + (level - 1) × 4`

Example:
- Level 1: AP=5, HP=20, MP=10
- Level 5: AP=17, HP=44, MP=26
- Level 10: AP=32, HP=74, MP=46
- Level 15: AP=47, HP=104, MP=66

## Output Formats

### Console Output

```
=== ZARGON BATTLE BALANCE SIMULATION ===
Total scenarios: 28350
Total battles: 28350000
Execution time: 45230ms

=== DANGEROUS SCENARIOS (>50% death rate) ===
Lvl 1 + dagger + cloth vs NECROx1: 12% win
Lvl 2 + short_sword + leather vs SKANDERx2: 23% win
...

=== IMPOSSIBLE SCENARIOS (0% win rate) ===
...

=== VERY EASY SCENARIOS (>95% win rate) ===
...
```

### CSV Output

Machine-readable format for spreadsheet analysis:

```csv
Level,Weapon,WeaponBonus,Armor,ArmorBonus,Monster,ScaleFactor,Iterations,Wins,Losses,WinRate,AvgTurnsWin,AvgTurnsLose,AvgPlayerHPRemaining,AvgDamageDealt,AvgDamageTaken
1,dagger,2,cloth,5,SLIME,1,1000,982,18,0.982,3.2,2.1,15.4,15.7,4.3
...
```

### Markdown Report

Human-readable tables with:
- Summary statistics
- High-risk combinations (<50% win rate)
- Impossible scenarios (0% win rate)
- Win rate analysis by player level

## Customizing the Simulation

### Change Iterations

Edit `SimulationConfig`:
```kotlin
val config = SimulationConfig(
    iterationsPerScenario = 100,  // Faster for quick tests
    // iterationsPerScenario = 10000,  // Higher precision
)
```

### Test Specific Levels

```kotlin
val config = SimulationConfig(
    playerLevels = listOf(1, 5, 10, 15),  // Only test these levels
)
```

### Test Specific Monsters

```kotlin
val config = SimulationConfig(
    monsterTypes = listOf(MonsterType.NECRO, MonsterType.ZARGON),
    monsterScalingFactors = listOf(1, 2, 3),
)
```

### Use Custom Random Seed

```kotlin
val config = SimulationConfig(
    randomSeed = 12345L  // Reproducible results
)
```

## Performance

On a typical development machine:
- **Quick simulation** (100 iterations, subset): < 1 minute
- **Standard simulation** (1000 iterations, all scenarios): 3-5 minutes
- **High precision** (10000 iterations, all scenarios): 30-60 minutes

## Future Enhancements

Potential additions:
- Magic strategy simulation (spell usage patterns)
- Flee strategy simulation
- Level progression simulation (random stat gains)
- Parallel execution via coroutines
- Graphical output (charts, heatmaps)
- External config file (JSON/YAML)

## Files Created

```
app/app/src/test/java/com/greenopal/zargon/simulation/
├── BattleSimulator.kt       # Pure battle engine
├── SimulationConfig.kt      # Configuration
├── SimulationResult.kt      # Result data classes
├── BattleBalanceTest.kt     # JUnit test runner
├── RunSimulation.kt         # Standalone main function
├── README.md                # This file
└── reports/
    └── ReportGenerator.kt   # CSV/Markdown/Console output
```

## Example Output

Sample dangerous scenario analysis:

```
Lvl 1 + dagger + cloth vs NECRO x1: 8% win
→ Player: AP=7, HP=20, Defense=5
→ Monster: AP=13, HP=30
→ Monster damage per hit: 8-9 (kills player in 2-3 hits)
→ Player damage per hit: 7 (needs 5 hits to kill)
→ Conclusion: Severely underpowered, should avoid or buff player/nerf enemy
```

## Credits

Extracts combat formulas from:
- `BattleViewModel.kt` (lines 97-160)
- Uses game models: `CharacterStats`, `MonsterStats`, `MonsterType`
- Weapon/Armor definitions from `WeaponShopScreen.kt`
