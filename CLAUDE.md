# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

"The Evil Wrath of Zargon" is a tile-based RPG adventure set in the land of GEF, originally developed by high-school students in 1998-1999 (Snappahed Software 98) as a QBASIC game. This repository contains both the original QBASIC code (`zargon/`) and a modern Android port (`app/`).

Active development is on the **Android app**. The QBASIC code is preserved for reference.

## Android App Architecture

**Stack**: Kotlin, Jetpack Compose, Hilt DI, Room, DataStore, MVVM + Clean Architecture

**Package**: `com.greenopal.zargon`
**Source root**: `app/app/src/main/java/com/greenopal/zargon/`

### Layer Structure

```
com.greenopal.zargon/
├── data/
│   ├── models/          — Data classes (CharacterStats, MonsterStats, MonsterType, GameState, Item, etc.)
│   └── repository/      — Persistence (SaveGameRepository, PrestigeRepository)
├── domain/
│   ├── battle/          — BattleState, BattleAction, Spell, MonsterSelector
│   ├── progression/     — ProgressionSystem (RewardSystem, LevelingSystem)
│   ├── challenges/      — ChallengeModifiers, ChallengeTimer, PrestigeSystem
│   ├── story/           — StoryProgressionChecker, NpcDialogProvider
│   ├── map/             — MapParser, TileType
│   ├── graphics/        — TileParser, SpriteParser, EGAPalette, TileBitmapCache
│   └── world/           — WorldSpell
├── ui/
│   ├── screens/         — 15 screen files (MapScreen, BattleScreen, WeaponShopScreen, etc.)
│   ├── viewmodels/      — GameViewModel, BattleViewModel, MapViewModel, ChallengeViewModel
│   ├── components/      — StatBar, StatsCard, MonsterStatsBox, BattleEffects, MagicMenu
│   └── theme/           — Theme, Typography (medieval color scheme)
├── BlankApplication.kt  — Hilt entry point
└── MainActivity.kt      — Compose activity entry
```

### Key Game Systems

**Battle System** (`domain/battle/`, `ui/viewmodels/BattleViewModel.kt`):
- Turn-based: player attacks first, then monster counterattacks (if alive)
- Player damage = `totalAP` (baseAP + weaponBonus)
- Monster damage = `attackPower - totalDefense + random(0..scalingFactor)`, minimum 1
- 8 spells: FLAME, CURE, WATER, LIGHTNING, BUBBLE_BLAST, RESTORE, FIRESTORM, DIVINE_LIGHT
- Spell damage = `baseDamage + random(1..randomBonus) + playerLevel`

**Character Stats** (`data/models/CharacterStats.kt`):
- Base stats: baseAP, baseDP (defense/HP), baseMP
- Equipment: weaponBonus, armorBonus
- Effective: totalAP = baseAP + weaponBonus, totalDefense = baseDP + armorBonus

**Monster Scaling** (`data/models/MonsterStats.kt`):
- scalingFactor = `playerLevel / 2 + 1` (min 1)
- scaledAP = baseAP * scalingFactor, scaledDP = baseDP * scalingFactor
- 9 types: SLIME, BAT, BABBLE, SPOOK, BELETH, SKANDER_SNAKE, NECRO, KRAKEN, ZARGON

**Equipment** (defined in `WeaponShopScreen.kt`):
- Weapons: DAGGER(2) → SHORT_SWORD(3) → LONG_SWORD(5) → SWORD_OF_THORNS(7) → BROAD_SWORD(9) → TWOHANDED_SWORD(15) → ATLANTEAN_SWORD(25)
- Armor: CLOTH(5) → LEATHER(10) → PLATED_LEATHER(18) → SPIKED_LEATHER(20) → CHAIN_MAIL(28) → PLATEMAIL(50)

**Leveling** (`domain/progression/ProgressionSystem.kt`):
- Random stat gains on level-up: AP = `0..currentLevel + 2`, DP = `4 + 0..currentLevel + 1`, MP = `3 + 0..currentLevel + 1`
- Full HP/MP restore on level-up
- XP rewards: 2-100 base per monster type × scalingFactor
- Gold rewards: baseGold + (scalingFactor × 3)

**World**: 4 worlds × 4 quadrants each (mapXY.lvl files)

### Simulation Framework

**Location**: `app/app/src/test/java/com/greenopal/zargon/simulation/`

- `BattleSimulator.kt` — Monte Carlo battle simulator, runs complete battle sequences
- `BattleBalanceTest.kt` — JUnit test class (1000 iterations/scenario, configurable)
- `SimulationConfig.kt` — config: iterations, player levels, weapons, armors, monster types, scaling
- `SimulationResult.kt` — aggregated results: win rate, avg turns, avg HP remaining, damage dealt/taken
- `ReportGenerator.kt` — console, CSV, and Markdown report output

Run simulations: `./gradlew test` from `app/app/` directory

### Build & Run

- Gradle 8.7.3, Kotlin 2.3.0, Compose BOM 2024.12.01
- `./gradlew assembleDebug` from `app/app/`
- Min SDK 24, Target SDK 35

## Agent Team

Agent definitions live in `.claude/agents/`. The team is designed for game design exploration:
- **team-lead** — orchestrates ideation → evaluation → simulation loop
- **ideator** — proposes feature ideas grounded in the actual codebase
- **evaluator** — assesses balance, simplicity, and fit; analyzes simulation data
- **simulator** — validates mechanics using the Kotlin simulation framework

## Legacy QBASIC Code

The original game source is in `zargon/ZARGON.BAS` (~3000+ lines). Preserved for reference only — do not modify. See map files (`mapXY.lvl`), graphics (`tiles.wad`), and config (`gef.ini`) in the `zargon/` directory.
