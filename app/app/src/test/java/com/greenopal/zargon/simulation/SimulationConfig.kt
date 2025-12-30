package com.greenopal.zargon.simulation

import com.greenopal.zargon.data.models.MonsterType
import com.greenopal.zargon.ui.screens.Weapon
import com.greenopal.zargon.ui.screens.Armor

data class SimulationConfig(
    val iterationsPerScenario: Int = 1000,
    val playerLevels: List<Int> = (1..15).toList(),
    val weapons: List<Weapon> = Weapon.values().toList(),
    val armors: List<Armor> = Armor.values().toList(),
    val monsterTypes: List<MonsterType> = MonsterType.values().toList(),
    val monsterScalingFactors: List<Int> = (1..10).toList(),
    val randomSeed: Long? = null
)
