
# Zargon
Note the pixellab MCP

# Battles survived before dying (per equipment tier)
./gradlew testDebugUnitTest --tests "com.greenopal.zargon.simulation.SurvivalSimulation"

# Time to reach level 10
./gradlew testDebugUnitTest --tests "com.greenopal.zargon.simulation.ProgressionSimulation"

# Challenge difficulty matrix (each challenge × each prestige bonus)
./gradlew testDebugUnitTest --tests "com.greenopal.zargon.simulation.ChallengeDifficultySimulation"

# Balance tuner (power-function coefficient evaluation for weapon pricing/bonuses)
./gradlew testDebugUnitTest --tests "com.greenopal.zargon.simulation.BalanceTuner"

There are also the older simulations that were already in the project:

./gradlew testDebugUnitTest --tests "com.greenopal.zargon.simulation.BattleBalanceTest"
./gradlew testDebugUnitTest --tests "com.greenopal.zargon.simulation.QuickWinRateTest"

Output goes to XML files in app/app/app/build/test-results/testDebugUnitTest/. Or run all of them at once:

./gradlew testDebugUnitTest --tests "com.greenopal.zargon.simulation.*"

