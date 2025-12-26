# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

**Blank** is an Android application built with modern Android development practices using Jetpack Compose, Hilt for dependency injection, and Kotlin 2.1.0. This app is a port of "The Evil Wrath of Zargon", a legacy QBASIC game from 1998-1999.

- **Package**: `com.greenopal.blank`
- **Application ID**: `com.greenopal.blank`
- **Min SDK**: 24
- **Target SDK**: 35

### Zargon Game Port

This Android app is a port of the original QBASIC game "The Evil Wrath of Zargon". The original QBASIC source code (ZARGON.BAS) is located in `/Users/greg/dev/zargon/app/zargon/ZARGON.BAS` but is **READ-ONLY** and should **NEVER BE EDITED**. It serves only as reference material for understanding the original game logic. All game logic modifications should be made to the Kotlin/Android implementation files.

## Build Commands

### Build the project
```bash
./gradlew build
```

### Build debug APK
```bash
./gradlew assembleDebug
```

### Build release APK
```bash
./gradlew assembleRelease
```

### Run tests
```bash
# Run all unit tests
./gradlew test

# Run unit tests for debug variant
./gradlew testDebugUnitTest

# Run instrumented tests
./gradlew connectedAndroidTest

# Run a specific test class
./gradlew test --tests "com.greenopal.blank.YourTestClass"

# Run a specific test method
./gradlew test --tests "com.greenopal.blank.YourTestClass.yourTestMethod"
```

### Clean build
```bash
./gradlew clean
```

## Architecture

### Dependency Injection
- **Hilt** is used for dependency injection throughout the application
- `BlankApplication` is annotated with `@HiltAndroidApp` - this is the entry point for Hilt DI
- `MainActivity` uses `@AndroidEntryPoint` to enable dependency injection in the activity
- When creating new components that need DI (Activities, Fragments, ViewModels), annotate them with `@AndroidEntryPoint` or `@HiltViewModel` as appropriate

### UI Layer
- **Jetpack Compose** is used for all UI components
- `MainActivity` extends `ComponentActivity` and uses `setContent {}` for Compose UI
- Material3 is available for UI components
- Material Icons (core and extended) are available

### Data Layer
- **Room** is configured for local database operations
- **DataStore** (preferences) is available for key-value storage
- **Kotlinx Serialization** is configured for JSON serialization

### Navigation
- **Navigation Compose** is available for screen navigation
- Hilt Navigation Compose is integrated for ViewModel injection in navigation graphs

## Dependency Management

All dependencies are managed via **version catalogs** in `gradle/libs.versions.toml`:
- When adding new dependencies, add them to `libs.versions.toml` first
- Use `libs.` references in `app/build.gradle.kts` (never hardcode versions)
- Versions are centralized in the `[versions]` section
- Libraries are defined in the `[libraries]` section
- Plugins are defined in the `[plugins]` section

## Testing

The project includes:
- **JUnit** for unit testing
- **Mockito** (Core and Kotlin) for mocking
- **Kotest Assertions** for expressive assertions
- **Kotlinx Coroutines Test** for testing coroutines
- **Espresso** for UI testing
- **Compose UI Test** for testing Compose UI

Use Kotest assertions for more expressive test assertions when writing new tests.

## Kotlin Configuration

- **Kotlin 2.1.0** is in use
- **Compose Compiler Plugin** (`kotlin-compose-compiler`) is required and configured
- **KAPT** is used for annotation processing (Hilt, Room)
- **JVM Target**: 17
- Note: KAPT support for Kotlin 2.0+ is in Alpha and falls back to 1.9 (this is expected)

## Important Notes

- The project uses the modern Kotlin Compose Compiler plugin (not the deprecated `composeOptions.kotlinCompilerExtensionVersion`)
- All activities and components that need DI must be annotated with `@AndroidEntryPoint`
- The application class (`BlankApplication`) is the Hilt entry point

## Coding conventions
Do not add comments 
Do not change any code in any .BAS files.


## Claude execution notes
Do not run any commandline commands