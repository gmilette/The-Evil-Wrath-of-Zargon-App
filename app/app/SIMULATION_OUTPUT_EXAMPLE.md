# Zargon Battle Simulation - Example Output

## What the simulation now does:

1. Tests **monster scaling from 1 to 10** (was 1-5)
2. Shows **win rates by monster type and scaling factor**
3. Removed "dangerous" and "impossible" scenario messages
4. Total scenarios: **56,700** (15 levels × 7 weapons × 6 armors × 9 monsters × 10 scales)

## How to run:

```bash
cd app
./gradlew :app:testDebugUnitTest
```

Or run specific tests from Android Studio by opening any test file in:
`app/app/src/test/java/com/greenopal/zargon/simulation/`

## Example Console Output:

```
=== ZARGON BATTLE BALANCE SIMULATION ===
Total scenarios: 56700
Total battles: 56700000
Execution time: 180000ms (180.0s)

=== WIN RATE BY MONSTER TYPE AND SCALING ===

Monster: BAT
Scale | Win Rate | Sample Size
------|----------|------------
  1   |   98.5%  | 630 battles
  2   |   94.2%  | 630 battles
  3   |   87.3%  | 630 battles
  4   |   78.1%  | 630 battles
  5   |   67.4%  | 630 battles
  6   |   55.2%  | 630 battles
  7   |   43.8%  | 630 battles
  8   |   32.1%  | 630 battles
  9   |   22.5%  | 630 battles
  10  |   15.3%  | 630 battles

Monster: BABBLE
Scale | Win Rate | Sample Size
------|----------|------------
  1   |   96.8%  | 630 battles
  2   |   89.4%  | 630 battles
  3   |   79.2%  | 630 battles
  4   |   68.7%  | 630 battles
  5   |   56.3%  | 630 battles
  6   |   44.9%  | 630 battles
  7   |   34.2%  | 630 battles
  8   |   25.1%  | 630 battles
  9   |   17.8%  | 630 battles
  10  |   11.4%  | 630 battles

Monster: BELETH
Scale | Win Rate | Sample Size
------|----------|------------
  1   |   94.2%  | 630 battles
  2   |   84.6%  | 630 battles
  3   |   72.8%  | 630 battles
  4   |   60.1%  | 630 battles
  5   |   48.5%  | 630 battles
  6   |   37.2%  | 630 battles
  7   |   27.6%  | 630 battles
  8   |   19.3%  | 630 battles
  9   |   12.8%  | 630 battles
  10  |    7.5%  | 630 battles

Monster: KRAKEN
Scale | Win Rate | Sample Size
------|----------|------------
  1   |   45.2%  | 630 battles
  2   |   18.4%  | 630 battles
  3   |    5.3%  | 630 battles
  4   |    1.2%  | 630 battles
  5   |    0.2%  | 630 battles
  6   |    0.0%  | 630 battles
  7   |    0.0%  | 630 battles
  8   |    0.0%  | 630 battles
  9   |    0.0%  | 630 battles
  10  |    0.0%  | 630 battles

Monster: NECRO
Scale | Win Rate | Sample Size
------|----------|------------
  1   |   82.3%  | 630 battles
  2   |   65.7%  | 630 battles
  3   |   49.8%  | 630 battles
  4   |   35.2%  | 630 battles
  5   |   23.4%  | 630 battles
  6   |   14.8%  | 630 battles
  7   |    8.7%  | 630 battles
  8   |    4.3%  | 630 battles
  9   |    1.9%  | 630 battles
  10  |    0.6%  | 630 battles

Monster: SKANDER_SNAKE
Scale | Win Rate | Sample Size
------|----------|------------
  1   |   78.9%  | 630 battles
  2   |   58.2%  | 630 battles
  3   |   40.1%  | 630 battles
  4   |   26.5%  | 630 battles
  5   |   16.3%  | 630 battles
  6   |    9.2%  | 630 battles
  7   |    4.7%  | 630 battles
  8   |    2.1%  | 630 battles
  9   |    0.8%  | 630 battles
  10  |    0.2%  | 630 battles

Monster: SLIME
Scale | Win Rate | Sample Size
------|----------|------------
  1   |   99.8%  | 630 battles
  2   |   98.9%  | 630 battles
  3   |   96.7%  | 630 battles
  4   |   93.2%  | 630 battles
  5   |   88.4%  | 630 battles
  6   |   82.1%  | 630 battles
  7   |   74.6%  | 630 battles
  8   |   66.3%  | 630 battles
  9   |   57.8%  | 630 battles
  10  |   49.2%  | 630 battles

Monster: SPOOK
Scale | Win Rate | Sample Size
------|----------|------------
  1   |   95.4%  | 630 battles
  2   |   86.7%  | 630 battles
  3   |   75.3%  | 630 battles
  4   |   62.8%  | 630 battles
  5   |   50.4%  | 630 battles
  6   |   39.1%  | 630 battles
  7   |   29.2%  | 630 battles
  8   |   20.8%  | 630 battles
  9   |   14.1%  | 630 battles
  10  |    9.2%  | 630 battles

Monster: ZARGON
Scale | Win Rate | Sample Size
------|----------|------------
  1   |   15.2%  | 630 battles
  2   |    2.3%  | 630 battles
  3   |    0.3%  | 630 battles
  4   |    0.0%  | 630 battles
  5   |    0.0%  | 630 battles
  6   |    0.0%  | 630 battles
  7   |    0.0%  | 630 battles
  8   |    0.0%  | 630 battles
  9   |    0.0%  | 630 battles
  10  |    0.0%  | 630 battles
```

## CSV Output

The simulation also generates `balance_simulation.csv` with detailed data:

```csv
Level,Weapon,WeaponBonus,Armor,ArmorBonus,Monster,ScaleFactor,Iterations,Wins,Losses,WinRate,AvgTurnsWin,AvgTurnsLose,AvgPlayerHPRemaining,AvgDamageDealt,AvgDamageTaken
1,dagger,2,cloth,5,SLIME,1,1000,998,2,0.998,2.3,1.5,18.2,11.5,1.8
1,dagger,2,cloth,5,SLIME,2,1000,985,15,0.985,3.1,2.1,16.4,23.1,3.6
1,dagger,2,cloth,5,SLIME,3,1000,947,53,0.947,4.2,2.8,14.3,34.6,5.4
...
15,Atlantean Sword,25,platemail,50,ZARGON,10,1000,0,1000,0.000,0.0,45.2,0.0,27000.0,21600.0
```

## Available Test Files

1. **BattleBalanceTest.kt** - Full simulation (all 56,700 scenarios)
2. **QuickWinRateTest.kt** - Faster version with fewer iterations
3. **DemoWinRates.kt** - Small demo showing specific examples

## Demo Tests

The `DemoWinRates.kt` file contains quick examples you can run:

- `demo win rates for level 5 character` - Shows Level 5 vs all monsters at scales 1-10
- `demo win rates across levels vs NECRO` - Shows how different levels fare against NECRO

These run 100 iterations per scenario and complete in seconds.
