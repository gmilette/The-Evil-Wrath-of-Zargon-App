package com.greenopal.zargon.domain.battle

import com.greenopal.zargon.data.models.ChallengeConfig
import com.greenopal.zargon.data.models.PrestigeData
import com.greenopal.zargon.domain.challenges.ChallengeModifiers
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.random.Random

/**
 * Pure domain logic for executing battle actions.
 * No Android dependencies, no coroutines, no repositories.
 * Delays and StateFlow updates are the ViewModel's responsibility.
 *
 * All methods take raw CharacterStats values from BattleState
 * and apply challenge/prestige modifiers at the point of use.
 */
@Singleton
class BattleUseCase @Inject constructor(
    private val battleEngine: BattleEngine,
    private val challengeModifiers: ChallengeModifiers
) {

    fun executePlayerAttack(
        state: BattleState,
        config: ChallengeConfig?,
        prestige: PrestigeData
    ): BattleState {
        val effectiveWeaponBonus = challengeModifiers.getEffectiveWeaponBonus(
            state.character.weaponBonus, config, prestige
        )
        val damage = battleEngine.calculatePlayerDamage(state.character, effectiveWeaponBonus)
        val newMonster = state.monster.takeDamage(damage)
        return state
            .updateMonster(newMonster)
            .addMessage("You hit ${state.monster.name} for $damage damage!")
            .checkBattleEnd()
    }

    fun executeMonsterCounterattack(
        state: BattleState,
        config: ChallengeConfig?,
        prestige: PrestigeData
    ): BattleState {
        val effectiveArmorBonus = challengeModifiers.getEffectiveArmorBonus(
            state.character.armorBonus, config, prestige
        )
        val damage = battleEngine.calculateMonsterDamage(state.monster, state.character, effectiveArmorBonus)
        val newCharacter = state.character.takeDamage(damage)
        return state
            .updateCharacter(newCharacter)
            .addMessage("${state.monster.name} hits you for $damage damage!")
            .checkBattleEnd()
    }

    fun executeSpell(
        state: BattleState,
        spell: Spell,
        config: ChallengeConfig?,
        prestige: PrestigeData
    ): BattleState {
        val spellMultiplier = challengeModifiers.getSpellEffectMultiplier(prestige)
        val newCharacter = state.character.useMagic(spell.mpCost)

        return if (spell.isHealing) {
            val healAmount = spell.calculateEffect(state.character.level, spellMultiplier)
            state
                .updateCharacter(newCharacter.heal(healAmount))
                .addMessage("${spell.name} restores $healAmount HP!")
        } else {
            val damage = spell.calculateEffect(state.character.level, spellMultiplier)
            val newMonster = state.monster.takeDamage(damage)
            state
                .updateCharacter(newCharacter)
                .updateMonster(newMonster)
                .addMessage("${spell.name} deals $damage damage!")
                .checkBattleEnd()
        }
    }

    /**
     * Returns a state with battleResult = Fled if escaped,
     * or a state with "Can't escape!" message if caught (caller should then execute counterattack).
     */
    fun attemptRun(state: BattleState, random: Random = Random): BattleState {
        val escaped = random.nextInt(1, 5) == 1
        return if (escaped) {
            state
                .addMessage("You run away!")
                .copy(battleResult = BattleResult.Fled)
        } else {
            state.addMessage("Can't escape!")
        }
    }

    fun canUseMagic(config: ChallengeConfig?): Boolean {
        return challengeModifiers.canUseMagic(config)
    }
}
