package com.greenopal.zargon.simulation

import com.greenopal.zargon.data.models.CharacterStats
import com.greenopal.zargon.data.models.MonsterStats
import com.greenopal.zargon.data.models.MonsterType
import org.junit.Test
import kotlin.math.pow
import kotlin.random.Random

class BalanceTuner {

    data class CoefficientSet(
        val priceA: Double,
        val priceB: Double,
        val bonusC: Double,
        val bonusD: Double
    ) {
        fun price(tier: Int): Int = (priceA * tier.toDouble().pow(priceB)).toInt()
        fun bonus(tier: Int): Int = (bonusC * tier.toDouble().pow(bonusD)).toInt()
    }

    data class BalanceResult(
        val coefficients: CoefficientSet,
        val avgWinRate: Double,
        val winRateByLevel: Map<Int, Double>,
        val inTargetRange: Boolean
    )

    private val candidateSets = listOf(
        CoefficientSet(priceA = 15.0, priceB = 2.0, bonusC = 2.0, bonusD = 1.5),
        CoefficientSet(priceA = 20.0, priceB = 1.8, bonusC = 1.5, bonusD = 1.8),
        CoefficientSet(priceA = 10.0, priceB = 2.2, bonusC = 3.0, bonusD = 1.3),
        CoefficientSet(priceA = 25.0, priceB = 1.6, bonusC = 2.5, bonusD = 1.4),
        CoefficientSet(priceA = 12.0, priceB = 2.1, bonusC = 1.8, bonusD = 1.6),
        CoefficientSet(priceA = 18.0, priceB = 1.9, bonusC = 2.2, bonusD = 1.5)
    )

    private val testLevels = listOf(1, 3, 5, 7, 10)
    private val iterationsPerLevel = 1000

    private fun createCharacter(level: Int, weaponBonus: Int): CharacterStats {
        val baseAP = 5 + (level - 1) * 3
        val baseDP = 20 + (level - 1) * 6
        val baseMP = 10 + (level - 1) * 4
        val armorBonus = (level * 5).coerceAtMost(50)
        return CharacterStats(
            baseAP = baseAP,
            baseDP = baseDP,
            maxHP = baseDP,
            currentHP = baseDP,
            baseMP = baseMP,
            currentMP = baseMP,
            level = level,
            weaponBonus = weaponBonus,
            armorBonus = armorBonus
        )
    }

    private fun createMonster(type: MonsterType, scalingFactor: Int): MonsterStats {
        return MonsterStats(
            type = type,
            attackPower = type.baseAP * scalingFactor,
            currentHP = type.baseDP * scalingFactor,
            maxHP = type.baseDP * scalingFactor,
            scalingFactor = scalingFactor
        )
    }

    private fun getAppropriateMonsters(level: Int): List<MonsterType> {
        return MonsterType.values().filter {
            it.minLevel <= level && it != MonsterType.ZARGON && it != MonsterType.KRAKEN
        }
    }

    private fun evaluateCoefficientSet(
        coefficients: CoefficientSet,
        simulator: BattleSimulator,
        random: Random
    ): BalanceResult {
        val winRateByLevel = mutableMapOf<Int, Double>()

        for (level in testLevels) {
            val tier = when {
                level <= 1 -> 1
                level <= 3 -> 2
                level <= 5 -> 3
                level <= 7 -> 4
                else -> 5
            }
            val weaponBonus = coefficients.bonus(tier)
            val character = createCharacter(level, weaponBonus)
            val scalingFactor = level / 2 + 1
            val eligibleMonsters = getAppropriateMonsters(level)

            var wins = 0
            for (i in 1..iterationsPerLevel) {
                val monsterType = eligibleMonsters[random.nextInt(eligibleMonsters.size)]
                val monster = createMonster(monsterType, scalingFactor)
                val log = simulator.simulateBattle(character, monster)
                if (log.outcome == BattleSimulator.BattleOutcome.PlayerVictory) wins++
            }

            winRateByLevel[level] = wins.toDouble() / iterationsPerLevel
        }

        val avgWinRate = winRateByLevel.values.average()
        val inTargetRange = winRateByLevel.values.all { it in 0.70..0.80 }

        return BalanceResult(coefficients, avgWinRate, winRateByLevel, inTargetRange)
    }

    @Test
    fun `evaluate balance with power functions`() {
        val random = Random(12345)
        val simulator = BattleSimulator(random)

        println("=== BALANCE TUNER SIMULATION ===")
        println("Target win rate: 70-80%")
        println("Iterations per level: $iterationsPerLevel")
        println("Weapon price(tier) = a * tier^b")
        println("Weapon bonus(tier) = c * tier^d")
        println()

        val results = candidateSets.map { coefficients ->
            evaluateCoefficientSet(coefficients, simulator, random)
        }

        println(String.format(
            "%-6s %-8s %-8s %-8s %-8s | %-8s %-8s %-8s %-8s %-8s | %-8s %-10s",
            "Set#", "a", "b", "c", "d",
            "Lv1", "Lv3", "Lv5", "Lv7", "Lv10",
            "Avg", "InTarget"
        ))
        println("-".repeat(120))

        results.forEachIndexed { idx, result ->
            val c = result.coefficients
            println(String.format(
                "%-6d %-8.1f %-8.1f %-8.1f %-8.1f | %-8.1f %-8.1f %-8.1f %-8.1f %-8.1f | %-8.1f %-10s",
                idx + 1, c.priceA, c.priceB, c.bonusC, c.bonusD,
                (result.winRateByLevel[1] ?: 0.0) * 100,
                (result.winRateByLevel[3] ?: 0.0) * 100,
                (result.winRateByLevel[5] ?: 0.0) * 100,
                (result.winRateByLevel[7] ?: 0.0) * 100,
                (result.winRateByLevel[10] ?: 0.0) * 100,
                result.avgWinRate * 100,
                if (result.inTargetRange) "YES" else "no"
            ))
        }

        println()
        println("=== Generated Weapon Tables ===")
        println()

        for ((idx, coefficients) in candidateSets.withIndex()) {
            println("Set ${idx + 1}: price = ${coefficients.priceA} * tier^${coefficients.priceB}, bonus = ${coefficients.bonusC} * tier^${coefficients.bonusD}")
            println(String.format("  %-6s %-10s %-10s", "Tier", "Price", "Bonus"))
            for (tier in 1..7) {
                println(String.format("  %-6d %-10d %-10d", tier, coefficients.price(tier), coefficients.bonus(tier)))
            }
            println()
        }

        val bestResult = results.filter { it.inTargetRange }.minByOrNull {
            it.winRateByLevel.values.let { rates ->
                val mean = rates.average()
                rates.sumOf { r -> (r - mean).pow(2) }
            }
        }

        if (bestResult != null) {
            println("BEST SET: a=${bestResult.coefficients.priceA}, b=${bestResult.coefficients.priceB}, " +
                    "c=${bestResult.coefficients.bonusC}, d=${bestResult.coefficients.bonusD}")
            println("Average win rate: ${String.format("%.1f", bestResult.avgWinRate * 100)}%")
        } else {
            println("No coefficient set achieved 70-80% win rate across all levels.")
            val closest = results.minByOrNull { result ->
                result.winRateByLevel.values.sumOf { (it - 0.75).pow(2) }
            }!!
            println("CLOSEST SET: a=${closest.coefficients.priceA}, b=${closest.coefficients.priceB}, " +
                    "c=${closest.coefficients.bonusC}, d=${closest.coefficients.bonusD}")
            println("Average win rate: ${String.format("%.1f", closest.avgWinRate * 100)}%")
        }

        println()
        println("Simulation complete.")
    }
}
